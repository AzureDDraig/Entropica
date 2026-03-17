package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.IFumeMultiblockController;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.api.ichor.IIchorMultiblock;
import ddraig.net.entropica.api.ichor.VisIchorStack;
import ddraig.net.entropica.block.AttunementPedestalBlock;
import ddraig.net.entropica.block.EidolicFocalPedestalBlock;
import ddraig.net.entropica.block.VisFumeInputPortBlock;
import ddraig.net.entropica.block.VisIchorInputPortBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class EidolicLatheBlockEntity extends BlockEntity implements IFumeHandler, IFumeMultiblockController, IIchorMultiblock {
    public final SimpleContainer inventory = new SimpleContainer(4) {
        @Override
        public void setChanged() {
            super.setChanged();
            EidolicLatheBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private boolean isFormed = false;
    private int recheckDelay = -1;
    private int tickCount = 0;

    public final List<BlockPos> connectedPedestals = new ArrayList<>();
    public BlockPos activePortPos = null;

    // Fuel Buffers
    private VisFumeStack storedFume = VisFumeStack.EMPTY;
    private VisIchorStack storedIchor = VisIchorStack.EMPTY;
    private static final int MAX_FLUID_CAPACITY = 8000;

    // Crafting State
    public boolean isCrafting = false;
    public int craftingProgress = 0;
    public int maxCraftingProgress = 500; // 25 seconds default
    public EssenceType craftingEssenceType = null;
    private boolean isIchorCraft = false;
    private int craftingFumeTotal = 0;
    private int craftingFumeDrained = 0;

    public EidolicLatheBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EIDOLIC_FOCAL_PEDESTAL_BE.get(), pos, state);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(15.0);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        tickCount++;

        if (this.isFormed && tickCount % 10 == 0) {
            this.attemptFormMultiblock();
        }

        if (this.recheckDelay > 0) {
            this.recheckDelay--;
            if (this.recheckDelay == 0) {
                boolean wasFormed = this.isFormed;
                boolean success = this.attemptFormMultiblock();
                if (wasFormed != success) {
                    level.setBlock(pos, state.setValue(EidolicFocalPedestalBlock.FORMED, success), 3);
                }
            }
        }

        // Processing the active crafting sequence
        if (this.isFormed && this.isCrafting) {
            int toDrainThisTick = this.craftingFumeTotal / this.maxCraftingProgress;

            // Clean up any remaining decimals on the very last tick
            if (this.craftingProgress == this.maxCraftingProgress - 1) {
                toDrainThisTick = this.craftingFumeTotal - this.craftingFumeDrained;
            }

            boolean drainedSuccessfully = false;

            if (this.isIchorCraft) {
                if (this.storedIchor.getAmount() >= toDrainThisTick && this.storedIchor.getType() == this.craftingEssenceType) {
                    this.storedIchor.shrink(toDrainThisTick);
                    if (this.storedIchor.getAmount() <= 0) this.storedIchor = VisIchorStack.EMPTY;
                    drainedSuccessfully = true;
                }
            } else {
                if (this.storedFume.getAmount() >= toDrainThisTick && this.storedFume.getType() == this.craftingEssenceType) {
                    this.storedFume.shrink(toDrainThisTick);
                    if (this.storedFume.getAmount() <= 0) this.storedFume = VisFumeStack.EMPTY;
                    drainedSuccessfully = true;
                }
            }

            if (drainedSuccessfully) {
                this.craftingFumeDrained += toDrainThisTick;
                this.craftingProgress++;

                // Sync frequently so the rendering beams pulse and grow smoothly
                if (this.craftingProgress % 5 == 0) {
                    this.setChanged();
                    this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                }

                if (this.craftingProgress >= this.maxCraftingProgress) {
                    finishCrafting();
                }
            } else {
                // The fuel network was severed or drained by another machine!
                this.isCrafting = false;
                this.setChanged();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                if (this.level instanceof ServerLevel sl) {
                    sl.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
    }

    // ==========================================
    // LATHE CRAFTING LOGIC
    // ==========================================

    public void attemptCraft(Player player) {
        if (!this.isFormed || this.level == null || this.level.isClientSide() || this.isCrafting) return;

        // Gather modifiers from all connected pedestals
        List<ItemStack> availableModifiers = new ArrayList<>();
        for (BlockPos pPos : this.connectedPedestals) {
            if (this.level.getBlockEntity(pPos) instanceof AttunementPedestalBlockEntity ped) {
                for (int i = 0; i < ped.inventory.getContainerSize(); i++) {
                    ItemStack stack = ped.inventory.getItem(i);
                    if (!stack.isEmpty()) availableModifiers.add(stack);
                }
            }
        }

        ddraig.net.entropica.recipe.EidolicLatheRecipe matchedRecipe = null;

        // Robust Recipe Matching (Order Agnostic & Slot Agnostic)
        for (ddraig.net.entropica.recipe.EidolicLatheRecipe recipe : ddraig.net.entropica.recipe.HardcodedRecipes.getLatheRecipes()) {
            ItemStack tempShape = ItemStack.EMPTY;
            ItemStack tempCore = ItemStack.EMPTY;

            for (int i = 0; i < 4; i++) {
                ItemStack s = this.inventory.getItem(i);
                if (!s.isEmpty() && recipe.etherealShape().test(s) && tempShape.isEmpty()) tempShape = s;
            }

            for (int i = 0; i < 4; i++) {
                ItemStack s = this.inventory.getItem(i);
                if (!s.isEmpty() && recipe.core().test(s) && s != tempShape && tempCore.isEmpty()) tempCore = s;
            }

            if (!tempShape.isEmpty() && !tempCore.isEmpty()) {
                if (recipe.modifiers().size() != availableModifiers.size()) continue;

                List<ItemStack> tempAvailable = new ArrayList<>(availableModifiers);
                boolean allMatched = true;

                for (net.minecraft.world.item.crafting.Ingredient ing : recipe.modifiers()) {
                    boolean found = false;
                    for (int i = 0; i < tempAvailable.size(); i++) {
                        if (ing.test(tempAvailable.get(i))) {
                            tempAvailable.remove(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allMatched = false;
                        break;
                    }
                }

                if (allMatched) {
                    matchedRecipe = recipe;
                    break;
                }
            }
        }

        if (matchedRecipe == null) {
            if (player != null) player.displayClientMessage(Component.literal("§cNo valid resonance found. Check modifiers and focal items."), true);
            return;
        }

        // Validate Fuel Availability (Requires at least 1 Vis)
        boolean hasFume = this.storedFume != null && !this.storedFume.isEmpty() && this.storedFume.getAmount() > 0;
        boolean hasIchor = this.storedIchor != null && !this.storedIchor.isEmpty() && this.storedIchor.getAmount() > 0;

        if (!hasFume && !hasIchor) {
            if (player != null) player.displayClientMessage(Component.literal("§cInsufficient Vis or Ichor in the network."), true);
            return;
        }

        // Lock in the crafting state
        this.isCrafting = true;
        this.craftingProgress = 0;
        this.maxCraftingProgress = 500; // 10 second ritual
        this.isIchorCraft = hasIchor;
        this.craftingEssenceType = hasIchor ? this.storedIchor.getType() : this.storedFume.getType();
        this.craftingFumeTotal = Math.min(8000, hasIchor ? this.storedIchor.getAmount() : this.storedFume.getAmount());
        this.craftingFumeDrained = 0;

        if (player != null) player.displayClientMessage(Component.literal("§aLathe activated. Commencing 10-second resonance sequence..."), true);

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 0.8f);
        }
    }

    private void finishCrafting() {
        this.isCrafting = false;

        // Gather all modifiers again to ensure they weren't removed
        List<AttunementPedestalBlockEntity> pedestals = new ArrayList<>();
        List<ItemStack> availableModifiers = new ArrayList<>();

        for (BlockPos pPos : this.connectedPedestals) {
            if (this.level.getBlockEntity(pPos) instanceof AttunementPedestalBlockEntity ped) {
                pedestals.add(ped);
                for (int i = 0; i < ped.inventory.getContainerSize(); i++) {
                    ItemStack stack = ped.inventory.getItem(i);
                    if (!stack.isEmpty()) availableModifiers.add(stack);
                }
            }
        }

        ddraig.net.entropica.recipe.EidolicLatheRecipe matchedRecipe = null;
        ItemStack foundShapeStack = ItemStack.EMPTY;
        ItemStack foundCoreStack = ItemStack.EMPTY;

        for (ddraig.net.entropica.recipe.EidolicLatheRecipe recipe : ddraig.net.entropica.recipe.HardcodedRecipes.getLatheRecipes()) {
            ItemStack tempShape = ItemStack.EMPTY;
            ItemStack tempCore = ItemStack.EMPTY;

            for (int i = 0; i < 4; i++) {
                ItemStack s = this.inventory.getItem(i);
                if (!s.isEmpty() && recipe.etherealShape().test(s) && tempShape.isEmpty()) tempShape = s;
            }

            for (int i = 0; i < 4; i++) {
                ItemStack s = this.inventory.getItem(i);
                if (!s.isEmpty() && recipe.core().test(s) && s != tempShape && tempCore.isEmpty()) tempCore = s;
            }

            if (!tempShape.isEmpty() && !tempCore.isEmpty()) {
                if (recipe.modifiers().size() != availableModifiers.size()) continue;

                List<ItemStack> tempAvailable = new ArrayList<>(availableModifiers);
                boolean allMatched = true;

                for (net.minecraft.world.item.crafting.Ingredient ing : recipe.modifiers()) {
                    boolean found = false;
                    for (int i = 0; i < tempAvailable.size(); i++) {
                        if (ing.test(tempAvailable.get(i))) {
                            tempAvailable.remove(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allMatched = false;
                        break;
                    }
                }

                if (allMatched) {
                    matchedRecipe = recipe;
                    foundShapeStack = tempShape;
                    foundCoreStack = tempCore;
                    break;
                }
            }
        }

        if (matchedRecipe == null) {
            // Player messed with the items mid-craft! Crafting fails, but the fuel was already drained.
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            return;
        }

        // Calculate Damage Scaling based on successfully drained fuel
        ItemStack result = matchedRecipe.result().copy();

        float multiplier = this.isIchorCraft ? 20.0f : 1.0f;
        float equivalentFumes = this.craftingFumeDrained * multiplier;
        float extraDamage = (Math.min(8000f, equivalentFumes) / 8000f) * 10.0f;

        if (ddraig.net.entropica.registry.ModDataComponents.VIS_WEAPON_STATE != null) {
            ddraig.net.entropica.component.VisWeaponState state = new ddraig.net.entropica.component.VisWeaponState.Builder(this.craftingEssenceType, extraDamage, 3).build();
            result.set(ddraig.net.entropica.registry.ModDataComponents.VIS_WEAPON_STATE.get(), state);
        }

        // Consume physical items
        foundShapeStack.shrink(1);
        foundCoreStack.shrink(1);
        for (AttunementPedestalBlockEntity ped : pedestals) {
            for (int i = 0; i < ped.inventory.getContainerSize(); i++) {
                if (!ped.inventory.getItem(i).isEmpty()) {
                    ped.inventory.getItem(i).shrink(1);
                }
            }
            ped.setChanged();
            this.level.sendBlockUpdated(ped.getBlockPos(), ped.getBlockState(), ped.getBlockState(), 3);
        }

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

        // Output Final Result into the world
        Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.2, this.worldPosition.getZ() + 0.5, result);

        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.2, this.worldPosition.getZ() + 0.5, 50, 0.2, 0.2, 0.2, 0.2);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.2, this.worldPosition.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.1);
            serverLevel.playSound(null, this.worldPosition, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0f, 1.5f);
        }
    }


    public boolean attemptFormMultiblock() {
        if (this.level == null || this.level.isClientSide()) return false;

        int baseX = this.worldPosition.getX();
        int baseY = this.worldPosition.getY() - 1;
        int baseZ = this.worldPosition.getZ();

        int attunementCount = 0;
        int portCount = 0;
        List<BlockPos> foundPedestals = new ArrayList<>();
        BlockPos foundPort = null;

        // Scan the 7x7x5 main structure
        for (int y = 0; y < 5; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos scanPos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                    BlockState scanState = this.level.getBlockState(scanPos);
                    Block block = scanState.getBlock();

                    boolean isPort = (block instanceof VisFumeInputPortBlock || block instanceof VisIchorInputPortBlock);
                    boolean isAirOrPort = scanState.isAir() || isPort;

                    if (isPort) {
                        portCount++;
                        if (foundPort == null) foundPort = scanPos;
                    }

                    if (y == 0) {
                        if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                    } else if (y == 1) {
                        if (x == 0 && z == 0) {
                            if (!scanPos.equals(this.worldPosition)) return failFormation();
                        } else if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (block instanceof AttunementPedestalBlock) {
                                attunementCount++;
                                foundPedestals.add(scanPos);
                            } else if (!isAirOrPort) {
                                return failFormation();
                            }
                        }
                    } else if (y == 2 || y == 3) {
                        if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (!isAirOrPort) return failFormation();
                        }
                    } else if (y == 4) {
                        if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (!isAirOrPort) return failFormation();
                        }
                    }
                }
            }
        }

        // Layer 6 (Air Check / Floating Ports Check exactly above the structure)
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos scanPos = new BlockPos(baseX + x, baseY + 5, baseZ + z);
                Block block = this.level.getBlockState(scanPos).getBlock();
                if (block instanceof VisFumeInputPortBlock || block instanceof VisIchorInputPortBlock) {
                    portCount++;
                    if (foundPort == null) foundPort = scanPos;
                }
            }
        }

        if (portCount < 1) return failFormation();
        if (attunementCount < 4 || attunementCount > 8) return failFormation();

        boolean pedestalsChanged = !this.connectedPedestals.equals(foundPedestals);
        boolean portChanged = (this.activePortPos == null && foundPort != null) || (this.activePortPos != null && !this.activePortPos.equals(foundPort));

        if (!this.isFormed || pedestalsChanged || portChanged) {
            this.connectedPedestals.clear();
            this.connectedPedestals.addAll(foundPedestals);
            this.activePortPos = foundPort;

            this.isFormed = true;
            updateBlockState(true);
            this.setChanged();

            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return true;
    }

    private boolean failFormation() {
        if (this.isFormed) {
            this.isFormed = false;
            this.isCrafting = false; // Interrupt any crafting
            updateBlockState(false);
            this.setChanged();

            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }

            this.recheckDelay = 5;
        }
        return false;
    }

    private void updateBlockState(boolean formed) {
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(EidolicFocalPedestalBlock.FORMED) && state.getValue(EidolicFocalPedestalBlock.FORMED) != formed) {
                this.level.setBlock(this.worldPosition, state.setValue(EidolicFocalPedestalBlock.FORMED, formed), 3);
            }
        }
    }

    public boolean isFormed() {
        return isFormed;
    }

    public boolean interactWithPlayer(Player player, InteractionHand hand) {
        if (this.isCrafting) {
            player.displayClientMessage(Component.literal("§cThe Lathe is actively forging!"), true);
            return false;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // Extract
        if (heldItem.isEmpty()) {
            for (int i = 3; i >= 0; i--) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (!stackInSlot.isEmpty()) {
                    player.setItemInHand(hand, stackInSlot.copy());
                    inventory.setItem(i, ItemStack.EMPTY);
                    return true;
                }
            }
        }
        // Insert
        else {
            for (int i = 0; i < 4; i++) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (stackInSlot.isEmpty()) {
                    inventory.setItem(i, heldItem.copyWithCount(1));
                    heldItem.shrink(1);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.inventory);
        }
    }

    // ==========================================
    // MULTIBLOCK FUME HANDLER IMPLEMENTATION
    // ==========================================

    @Override
    public void invalidateMultiblock() {
        this.failFormation();
    }

    @Override
    public int getSafeCapacity() {
        return this.isFormed ? MAX_FLUID_CAPACITY : 0;
    }

    @Override
    public int getAbsoluteCapacity() {
        return this.isFormed ? MAX_FLUID_CAPACITY : 0;
    }

    @Override
    public VisFumeStack getStoredFume() {
        return this.storedFume;
    }

    @Override
    public VisFumeStack getFumeInTank() {
        return this.storedFume != null ? this.storedFume : VisFumeStack.EMPTY;
    }

    @Override
    public int fill(VisFumeStack resource, boolean simulate) {
        if (!this.isFormed || resource.isEmpty()) return 0;
        if (!this.storedFume.isEmpty() && this.storedFume.getType() != resource.getType()) return 0;

        int space = getSafeCapacity() - this.storedFume.getAmount();
        if (space <= 0) return 0;

        int amountToFill = Math.min(resource.getAmount(), space);

        if (!simulate) {
            if (this.storedFume.isEmpty()) {
                this.storedFume = new VisFumeStack(resource.getType(), amountToFill);
            } else {
                this.storedFume.grow(amountToFill);
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return amountToFill;
    }

    @Override
    public VisFumeStack drain(int maxDrain, boolean simulate) {
        if (!this.isFormed || this.storedFume.isEmpty() || maxDrain <= 0) return VisFumeStack.EMPTY;

        int amountToDrain = Math.min(this.storedFume.getAmount(), maxDrain);
        VisFumeStack drained = new VisFumeStack(this.storedFume.getType(), amountToDrain);

        if (!simulate) {
            this.storedFume.shrink(amountToDrain);
            if (this.storedFume.getAmount() <= 0) {
                this.storedFume = VisFumeStack.EMPTY;
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return drained;
    }

    // ==========================================
    // MULTIBLOCK ICHOR HANDLER IMPLEMENTATION
    // ==========================================

    @Override
    public BlockPos getMasterPos() {
        return this.worldPosition;
    }

    @Override
    public boolean isMaster() {
        return true;
    }

    @Override
    public boolean canConnectIchor(Direction side) {
        return true; // The Lathe accepts connections globally via its floating ports
    }

    @Override
    public boolean isIchorValid(EssenceType type) {
        return true; // The Lathe accepts any Ichor type for crafting
    }

    @Override
    public int getCapacity() {
        return this.isFormed ? MAX_FLUID_CAPACITY : 0;
    }

    @Override
    public VisIchorStack getIchorInTank() {
        return this.storedIchor != null ? this.storedIchor : VisIchorStack.EMPTY;
    }

    @Override
    public int fill(VisIchorStack resource, boolean simulate) {
        if (!this.isFormed || resource.isEmpty()) return 0;
        if (!this.storedIchor.isEmpty() && this.storedIchor.getType() != resource.getType()) return 0;

        int space = getCapacity() - this.storedIchor.getAmount();
        if (space <= 0) return 0;

        int amountToFill = Math.min(resource.getAmount(), space);

        if (!simulate) {
            if (this.storedIchor.isEmpty()) {
                this.storedIchor = new VisIchorStack(resource.getType(), amountToFill);
            } else {
                this.storedIchor.grow(amountToFill);
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return amountToFill;
    }

    @Override
    public VisIchorStack drainIchor(int maxDrain, boolean simulate) {
        if (!this.isFormed || this.storedIchor.isEmpty() || maxDrain <= 0) return VisIchorStack.EMPTY;

        int amountToDrain = Math.min(this.storedIchor.getAmount(), maxDrain);
        VisIchorStack drained = new VisIchorStack(this.storedIchor.getType(), amountToDrain);

        if (!simulate) {
            this.storedIchor.shrink(amountToDrain);
            if (this.storedIchor.getAmount() <= 0) {
                this.storedIchor = VisIchorStack.EMPTY;
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return drained;
    }

    // ==========================================
    // SAVE / LOAD LOGIC
    // ==========================================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsFormed", Codec.BOOL, this.isFormed);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            output.store("LatheStack_" + i, ItemStack.OPTIONAL_CODEC, inventory.getItem(i));
        }
        output.store("ConnectedPedestals", BlockPos.CODEC.listOf(), this.connectedPedestals);

        if (this.activePortPos != null) {
            output.store("ActivePortPos", Codec.LONG, this.activePortPos.asLong());
        }

        output.store("IsCrafting", Codec.BOOL, this.isCrafting);
        output.store("CraftProgress", Codec.INT, this.craftingProgress);
        output.store("MaxCraftProgress", Codec.INT, this.maxCraftingProgress);
        output.store("IsIchor", Codec.BOOL, this.isIchorCraft);
        output.store("FumeTotal", Codec.INT, this.craftingFumeTotal);
        output.store("FumeDrained", Codec.INT, this.craftingFumeDrained);

        if (this.craftingEssenceType != null) {
            output.store("CraftType", Codec.STRING, this.craftingEssenceType.name());
        }

        if (!this.storedFume.isEmpty()) {
            output.store("FumeType", Codec.STRING, this.storedFume.getType().name());
            output.store("FumeAmount", Codec.INT, this.storedFume.getAmount());
        }
        if (!this.storedIchor.isEmpty()) {
            output.store("IchorType", Codec.STRING, this.storedIchor.getType().name());
            output.store("IchorAmount", Codec.INT, this.storedIchor.getAmount());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isFormed = input.read("IsFormed", Codec.BOOL).orElse(false);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int finalI = i;
            input.read("LatheStack_" + i, ItemStack.OPTIONAL_CODEC).ifPresent(stack -> inventory.setItem(finalI, stack));
        }

        this.connectedPedestals.clear();
        input.read("ConnectedPedestals", BlockPos.CODEC.listOf()).ifPresent(this.connectedPedestals::addAll);

        long pPos = input.read("ActivePortPos", Codec.LONG).orElse(-1L);
        if (pPos != -1L) this.activePortPos = BlockPos.of(pPos);

        this.isCrafting = input.read("IsCrafting", Codec.BOOL).orElse(false);
        this.craftingProgress = input.read("CraftProgress", Codec.INT).orElse(0);
        this.maxCraftingProgress = input.read("MaxCraftProgress", Codec.INT).orElse(1);
        this.isIchorCraft = input.read("IsIchor", Codec.BOOL).orElse(false);
        this.craftingFumeTotal = input.read("FumeTotal", Codec.INT).orElse(0);
        this.craftingFumeDrained = input.read("FumeDrained", Codec.INT).orElse(0);

        String cTypeStr = input.read("CraftType", Codec.STRING).orElse("");
        if (!cTypeStr.isEmpty()) {
            try { this.craftingEssenceType = EssenceType.valueOf(cTypeStr); }
            catch (IllegalArgumentException e) { this.craftingEssenceType = null; }
        }

        String fumeTypeStr = input.read("FumeType", Codec.STRING).orElse("");
        int fumeAmt = input.read("FumeAmount", Codec.INT).orElse(0);
        if (!fumeTypeStr.isEmpty() && fumeAmt > 0) {
            try { this.storedFume = new VisFumeStack(EssenceType.valueOf(fumeTypeStr), fumeAmt); }
            catch (IllegalArgumentException e) { this.storedFume = VisFumeStack.EMPTY; }
        } else { this.storedFume = VisFumeStack.EMPTY; }

        String ichorTypeStr = input.read("IchorType", Codec.STRING).orElse("");
        int ichorAmt = input.read("IchorAmount", Codec.INT).orElse(0);
        if (!ichorTypeStr.isEmpty() && ichorAmt > 0) {
            try { this.storedIchor = new VisIchorStack(EssenceType.valueOf(ichorTypeStr), ichorAmt); }
            catch (IllegalArgumentException e) { this.storedIchor = VisIchorStack.EMPTY; }
        } else { this.storedIchor = VisIchorStack.EMPTY; }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}