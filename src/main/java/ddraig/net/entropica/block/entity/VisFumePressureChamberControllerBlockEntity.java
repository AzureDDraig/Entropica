package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.IFumeMultiblockController;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.block.VisFumePressureChamberControllerBlock;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.recipe.PressureChamberRecipe;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VisFumePressureChamberControllerBlockEntity extends BlockEntity implements IFumeHandler, IFumeMultiblockController {

    private boolean isFormed = false;
    private int maxCapacity = 0;
    private VisFumeStack storedFume = VisFumeStack.EMPTY;
    private final Set<BlockPos> connectedBlocks = new HashSet<>();
    private BlockPos tablePos = null;

    private int tickCounter = 0;
    private int recheckDelay = -1;

    // Crafting State Machine
    private boolean isProcessing = false;
    private EssenceType targetFume = null;
    private boolean requiresSpecificFume = false;
    private int targetAmount = 0;
    private int progress = 0;
    private int maxProgress = 0;

    public VisFumePressureChamberControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        if (this.recheckDelay > 0) {
            this.recheckDelay--;
            if (this.recheckDelay == 0) {
                boolean wasFormed = this.isFormed;
                boolean success = this.attemptFormMultiblock();
                if (wasFormed != success) {
                    level.setBlock(pos, state.setValue(VisFumePressureChamberControllerBlock.FORMED, success), 3);
                }
            }
        }

        tickCounter++;
        if (tickCounter % 100 == 0 && this.isFormed) {
            this.attemptFormMultiblock();
        }

        if (this.isFormed) {
            if (this.isProcessing && this.tablePos != null) {
                processRecipe();
            }

            // FIXED: Respects the configured tick rate for network transfers
            int fumeTickRate = Math.max(1, EntropicaConfig.VIS_FUME_TICK_RATE.get());
            if (!this.storedFume.isEmpty() && tickCounter % fumeTickRate == 0) {
                autoExportFume();
            }
        }
    }

    private void autoExportFume() {
        if (this.level == null) return;

        // FIXED: Pulls the transfer rate from your global config
        int transferRate = EntropicaConfig.VIS_FUME_TRANSFER_RATE.get();

        for (BlockPos connectedPos : this.connectedBlocks) {
            BlockEntity connectedBE = this.level.getBlockEntity(connectedPos);

            if (connectedBE instanceof VisFumeVesselPortBlockEntity port && port.getMode() == VisFumeVesselPortBlockEntity.PortMode.OUTPUT) {

                for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                    BlockPos targetPos = connectedPos.relative(dir);
                    BlockEntity targetBE = this.level.getBlockEntity(targetPos);

                    if (this.connectedBlocks.contains(targetPos) || targetPos.equals(this.worldPosition)) continue;

                    if (targetBE instanceof IFumeHandler targetHandler) {
                        int amountToPush = Math.min(this.storedFume.getAmount(), transferRate);
                        VisFumeStack pushAttempt = new VisFumeStack(this.storedFume.getType(), amountToPush);

                        int acceptedAmount = targetHandler.fill(pushAttempt, true);

                        if (acceptedAmount > 0) {
                            VisFumeStack drained = this.drain(acceptedAmount, false);
                            targetHandler.fill(drained, false);

                            if (this.storedFume.isEmpty()) return;
                        }
                    }
                }
            }
        }
    }

    public void startPressurizing(Player player) {
        if (this.isProcessing || this.level == null || this.level.isClientSide()) return;

        BlockEntity tableBE = this.level.getBlockEntity(this.tablePos);
        if (!(tableBE instanceof EnrichmentTableBlockEntity table) || table.getHeldItem().isEmpty()) {
            player.displayClientMessage(Component.literal("No items on the enrichment table!"), true);
            return;
        }

        PressureChamberRecipe foundRecipe = null;
        if (this.level instanceof ServerLevel serverLevel) {
            for (RecipeHolder<PressureChamberRecipe> holder : serverLevel.recipeAccess().getRecipes().stream()
                    .filter(h -> h.value() instanceof PressureChamberRecipe)
                    .map(h -> (RecipeHolder<PressureChamberRecipe>) (RecipeHolder<?>) h)
                    .toList()) {
                if (holder.value().matches(new ddraig.net.entropica.recipe.PressureChamberRecipeInput(table.getHeldItem(), VisFumeStack.EMPTY), level)) {
                    foundRecipe = holder.value();
                    break;
                }
            }
        }

        if (foundRecipe != null) {
            if (table.getHeldItem().getCount() % foundRecipe.inputCount() != 0) {
                player.displayClientMessage(Component.literal("§cItem stack must be an exact multiple of " + foundRecipe.inputCount() + " for this recipe."), true);
                return;
            }

            this.isProcessing = true;
            this.requiresSpecificFume = foundRecipe.requiresSpecificFume();
            this.targetFume = foundRecipe.requiresSpecificFume() ? foundRecipe.requiredFume().orElse(null) : null;

            int crafts = table.getHeldItem().getCount() / foundRecipe.inputCount();
            this.targetAmount = foundRecipe.fumeAmount() * crafts;
            this.maxProgress = foundRecipe.processingTime();
            this.progress = 0;

            player.displayClientMessage(Component.literal("§aPressurizing chamber... awaiting exact gas input."), true);
            this.setChanged();

            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        } else {
            player.displayClientMessage(Component.literal("§cNo valid enrichment recipe for this item. Chamber refuses to pressurize."), true);
        }
    }

    private void processRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        BlockEntity tableBE = serverLevel.getBlockEntity(this.tablePos);
        if (!(tableBE instanceof EnrichmentTableBlockEntity table)) {
            cancelProcessing();
            return;
        }

        if (!this.storedFume.isEmpty() && this.requiresSpecificFume && this.targetFume != null && this.storedFume.getType() != this.targetFume) {
            if (this.tickCounter % 20 == 0) table.setProcessingGlow(0.6f, 0xFF0000);
            return;
        }

        if (this.storedFume.getAmount() < this.targetAmount) {
            if (this.tickCounter % 20 == 0) {
                int glowColor = (this.requiresSpecificFume && this.targetFume != null) ? this.targetFume.getColorInt() : 0xBBBBFF;
                table.setProcessingGlow(0.2f, glowColor);
            }
            return;
        }

        this.progress++;

        if (this.progress % 10 == 0) {
            float intensity = 0.2f + (0.8f * ((float) this.progress / this.maxProgress));
            int activeColor = (this.requiresSpecificFume && this.targetFume != null) ? this.targetFume.getColorInt() : this.storedFume.getType().getColorInt();
            table.setProcessingGlow(intensity, activeColor);
        }

        if (this.progress >= this.maxProgress) {
            PressureChamberRecipe activeRecipe = null;
            for (RecipeHolder<PressureChamberRecipe> holder : serverLevel.recipeAccess().getRecipes().stream()
                    .filter(h -> h.value() instanceof PressureChamberRecipe)
                    .map(h -> (RecipeHolder<PressureChamberRecipe>) (RecipeHolder<?>) h)
                    .toList()) {
                if (holder.value().matches(new ddraig.net.entropica.recipe.PressureChamberRecipeInput(table.getHeldItem(), VisFumeStack.EMPTY), level)) {
                    activeRecipe = holder.value();
                    break;
                }
            }

            if (activeRecipe != null) {
                int crafts = table.getHeldItem().getCount() / activeRecipe.inputCount();

                this.storedFume.shrink(this.targetAmount);
                if (this.storedFume.getAmount() <= 0) this.storedFume = VisFumeStack.EMPTY;

                ItemStack result = activeRecipe.output().copy();
                result.setCount(activeRecipe.output().getCount() * crafts);
                table.setHeldItem(result);

                int completionColor = (this.requiresSpecificFume && this.targetFume != null) ? this.targetFume.getColorInt() : 0xBBBBFF;
                table.triggerCompletionGlow(completionColor);

                BlockState tableState = serverLevel.getBlockState(this.tablePos);
                serverLevel.sendBlockUpdated(this.tablePos, tableState, tableState, 3);
            }
            cancelProcessing();
        }
    }

    private void cancelProcessing() {
        this.isProcessing = false;
        this.progress = 0;
        this.targetAmount = 0;
        this.requiresSpecificFume = false;
        this.targetFume = null;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean attemptFormMultiblock() {
        if (this.level == null || this.level.isClientSide()) return false;

        BlockPos foundTablePos = null;
        for (int dy = -3; dy <= 3; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = this.worldPosition.offset(dx, dy, dz);
                    if (this.level.getBlockState(checkPos).is(ModBlocks.ENRICHMENT_TABLE.get())) {
                        foundTablePos = checkPos;
                        break;
                    }
                }
                if (foundTablePos != null) break;
            }
            if (foundTablePos != null) break;
        }

        if (foundTablePos == null) return failFormationAndExplode();

        Set<BlockPos> validStructureBlocks = new HashSet<>();
        List<Block> foundGlass = new ArrayList<>();
        int portCount = 0;
        int controllerCount = 0;

        int baseX = foundTablePos.getX();
        int baseY = foundTablePos.getY() - 1;
        int baseZ = foundTablePos.getZ();

        for (int y = 0; y < 4; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos scanPos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                    BlockState state = this.level.getBlockState(scanPos);
                    Block block = state.getBlock();

                    if (y == 0) {
                        if (!state.is(ModBlocks.ARCANE_PLATING.get())) return failFormationAndExplode();
                        validStructureBlocks.add(scanPos);
                    } else if (y == 1) {
                        if (x == 0 && z == 0) {
                            if (!scanPos.equals(foundTablePos)) return failFormationAndExplode();
                        } else {
                            if (block == ModBlocks.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER.get()) {
                                controllerCount++;
                                validStructureBlocks.add(scanPos);
                            } else if (isGlass(block)) {
                                foundGlass.add(block);
                                validStructureBlocks.add(scanPos);
                            } else {
                                return failFormationAndExplode();
                            }
                        }
                    } else if (y == 2) {
                        if (x == 0 && z == 0) {
                            if (!state.isAir()) return failFormationAndExplode();
                        } else {
                            if (!isGlass(block)) return failFormationAndExplode();
                            foundGlass.add(block);
                            validStructureBlocks.add(scanPos);
                        }
                    } else if (y == 3) {
                        if (x == 0 && z == 0) {
                            if (block == ModBlocks.VIS_FUME_VESSEL_PORT.get()) {
                                portCount++;
                                validStructureBlocks.add(scanPos);
                            } else {
                                return failFormationAndExplode();
                            }
                        } else {
                            if (!isGlass(block)) return failFormationAndExplode();
                            foundGlass.add(block);
                            validStructureBlocks.add(scanPos);
                        }
                    }
                }
            }
        }

        if (controllerCount != 1 || portCount != 1) return failFormationAndExplode();

        for (BlockPos pos : validStructureBlocks) {
            if (pos.equals(this.worldPosition)) continue;
            BlockEntity be = this.level.getBlockEntity(pos);
            if (be instanceof ManaEnrichedGlassBlockEntity glass && glass.getControllerPos() != null && !glass.getControllerPos().equals(this.worldPosition)) return failFormationAndExplode();
            if (be instanceof VisFumeVesselPortBlockEntity port && port.getControllerPos() != null && !port.getControllerPos().equals(this.worldPosition)) return failFormationAndExplode();
        }

        if (this.isFormed) {
            clearOldConnections();
        }

        this.isFormed = true;
        this.tablePos = foundTablePos;
        this.connectedBlocks.clear();
        this.connectedBlocks.addAll(validStructureBlocks);
        this.connectedBlocks.remove(this.worldPosition);

        this.maxCapacity = calculateCapacity(foundGlass);

        if (!this.storedFume.isEmpty() && this.storedFume.getAmount() > this.maxCapacity) {
            this.storedFume.setAmount(this.maxCapacity);
        }

        for (BlockPos pos : this.connectedBlocks) {
            BlockEntity be = this.level.getBlockEntity(pos);
            if (be instanceof ManaEnrichedGlassBlockEntity glass) {
                glass.setControllerPos(this.worldPosition);
            } else if (be instanceof VisFumeVesselPortBlockEntity port) {
                port.setControllerPos(this.worldPosition);
            }
        }

        updateBlockState(true);
        this.setChanged();
        return true;
    }

    private boolean isGlass(Block block) {
        return block == ModBlocks.ESSENCE_ENRICHED_GLASS.get() ||
                block == ModBlocks.VIS_FUME_STRENGTHENED_GLASS.get() ||
                block == ModBlocks.VIS_ICHOR_ENRICHED_GLASS.get() ||
                block == ModBlocks.FRAGMENT_LATTICE_GLASS.get();
    }

    private int calculateCapacity(List<Block> glassBlocks) {
        int total = 0;
        for (Block block : glassBlocks) {
            if (block == ModBlocks.FRAGMENT_LATTICE_GLASS.get()) total += EntropicaConfig.VESSEL_GLASS_LATTICE_CAPACITY.get();
            else if (block == ModBlocks.VIS_ICHOR_ENRICHED_GLASS.get()) total += EntropicaConfig.VESSEL_GLASS_ICHOR_CAPACITY.get();
            else if (block == ModBlocks.VIS_FUME_STRENGTHENED_GLASS.get()) total += EntropicaConfig.VESSEL_GLASS_STRENGTHENED_CAPACITY.get();
            else total += EntropicaConfig.VESSEL_GLASS_BASE_CAPACITY.get();
        }
        return total;
    }

    private boolean failFormationAndExplode() {
        if (this.isFormed) {
            this.invalidate(false);
        } else {
            updateBlockState(false);
        }
        return false;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.invalidateMultiblock();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void invalidateMultiblock() {
        invalidate(true);
    }

    private void invalidate(boolean scheduleRecheck) {
        if (!this.isFormed) return;

        if ((this.isProcessing && this.progress > 0 && this.level != null) || (!this.storedFume.isEmpty() && this.level != null)) {
            float power = 1.0F;
            if (this.isProcessing && this.progress > 0) {
                power += 3.0F * ((float) this.progress / this.maxProgress);
            }
            if (!this.storedFume.isEmpty()) {
                power += 2.0F * ((float) this.storedFume.getAmount() / this.maxCapacity);
            }

            List<Player> players = this.level.getEntitiesOfClass(
                    Player.class,
                    new AABB(this.worldPosition).inflate(6.0D)
            );

            for (Player p : players) {
                Vec3 dir = p.position().subtract(this.worldPosition.getCenter()).normalize();
                p.push(dir.x * power, 0.5D + (power * 0.15D), dir.z * power);
                p.hurtMarked = true;
            }
            this.level.playSound(null, this.worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        this.isFormed = false;
        this.maxCapacity = 0;
        this.tablePos = null;

        this.storedFume = VisFumeStack.EMPTY;

        cancelProcessing();
        clearOldConnections();
        this.setChanged();

        if (scheduleRecheck) {
            this.recheckDelay = 5;
        }
    }

    private void clearOldConnections() {
        if (this.level != null) {
            for (BlockPos pos : this.connectedBlocks) {
                BlockEntity be = this.level.getBlockEntity(pos);
                if (be instanceof ManaEnrichedGlassBlockEntity glass) {
                    glass.setControllerPos(null);
                } else if (be instanceof VisFumeVesselPortBlockEntity port) {
                    port.setControllerPos(null);
                }
            }
        }
        this.connectedBlocks.clear();
    }

    private void updateBlockState(boolean formed) {
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(VisFumePressureChamberControllerBlock.FORMED) && state.getValue(VisFumePressureChamberControllerBlock.FORMED) != formed) {
                this.level.setBlock(this.worldPosition, state.setValue(VisFumePressureChamberControllerBlock.FORMED, formed), 3);
            }
        }
    }

    @Override public boolean isFormed() { return isFormed; }
    @Override public int getMaxCapacity() { return maxCapacity; }
    @Override public VisFumeStack getStoredFume() { return storedFume; }

    @Override
    public int fill(VisFumeStack resource, boolean simulate) {
        if (!isFormed || resource.isEmpty()) return 0;

        if (!this.storedFume.isEmpty() && this.storedFume.getType() != resource.getType()) return 0;

        int availableSpace = this.maxCapacity - this.storedFume.getAmount();
        if (availableSpace <= 0) return 0;

        int amountToFill = Math.min(resource.getAmount(), availableSpace);

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
        if (!isFormed || this.storedFume.isEmpty() || maxDrain <= 0) return VisFumeStack.EMPTY;

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

    @Override
    public VisFumeStack getFumeInTank() {
        return this.storedFume != null ? this.storedFume : VisFumeStack.EMPTY;
    }

    @Override
    public int getCapacity() {
        return this.maxCapacity;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsFormed", Codec.BOOL, this.isFormed);
        output.store("MaxCapacity", Codec.INT, this.maxCapacity);

        output.store("IsProcessing", Codec.BOOL, this.isProcessing);
        output.store("RequiresSpecificFume", Codec.BOOL, this.requiresSpecificFume);
        output.store("Progress", Codec.INT, this.progress);
        output.store("MaxProgress", Codec.INT, this.maxProgress);
        output.store("TargetAmount", Codec.INT, this.targetAmount);

        if (this.targetFume != null) {
            output.store("TargetFume", Codec.STRING, this.targetFume.name());
        }

        if (!this.storedFume.isEmpty()) {
            output.store("FumeType", Codec.STRING, this.storedFume.getType().name());
            output.store("FumeAmount", Codec.INT, this.storedFume.getAmount());
        }

        if (this.tablePos != null) {
            output.store("TablePos", Codec.LONG, this.tablePos.asLong());
        }

        output.store("ConnectedBlockCount", Codec.INT, this.connectedBlocks.size());
        int i = 0;
        for (BlockPos pos : this.connectedBlocks) {
            output.store("ConnectedBlock_" + i, Codec.LONG, pos.asLong());
            i++;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isFormed = input.read("IsFormed", Codec.BOOL).orElse(false);
        this.maxCapacity = input.read("MaxCapacity", Codec.INT).orElse(0);

        this.isProcessing = input.read("IsProcessing", Codec.BOOL).orElse(false);
        this.requiresSpecificFume = input.read("RequiresSpecificFume", Codec.BOOL).orElse(false);
        this.progress = input.read("Progress", Codec.INT).orElse(0);
        this.maxProgress = input.read("MaxProgress", Codec.INT).orElse(0);
        this.targetAmount = input.read("TargetAmount", Codec.INT).orElse(0);

        String targetFumeStr = input.read("TargetFume", Codec.STRING).orElse("");
        if (!targetFumeStr.isEmpty()) {
            try {
                this.targetFume = EssenceType.valueOf(targetFumeStr);
            } catch (IllegalArgumentException e) {
                this.targetFume = null;
            }
        } else {
            this.targetFume = null;
        }

        String fumeTypeStr = input.read("FumeType", Codec.STRING).orElse("");
        int fumeAmt = input.read("FumeAmount", Codec.INT).orElse(0);
        if (!fumeTypeStr.isEmpty() && fumeAmt > 0) {
            try {
                this.storedFume = new VisFumeStack(EssenceType.valueOf(fumeTypeStr), fumeAmt);
            } catch (IllegalArgumentException e) {
                this.storedFume = VisFumeStack.EMPTY;
            }
        } else {
            this.storedFume = VisFumeStack.EMPTY;
        }

        long tPos = input.read("TablePos", Codec.LONG).orElse(-1L);
        if (tPos != -1L) {
            this.tablePos = BlockPos.of(tPos);
        } else {
            this.tablePos = null;
        }

        this.connectedBlocks.clear();
        int count = input.read("ConnectedBlockCount", Codec.INT).orElse(0);
        for (int i = 0; i < count; i++) {
            long cPos = input.read("ConnectedBlock_" + i, Codec.LONG).orElse(-1L);
            if(cPos != -1L) {
                this.connectedBlocks.add(BlockPos.of(cPos));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}