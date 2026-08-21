package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AstralInfusionPedestalBlockEntity extends BlockEntity implements net.minecraft.world.Container {

    private ItemStack heldItem = ItemStack.EMPTY;
    private boolean isIrradiated = false;
    private String activeStarName = "Uncalibrated";
    private int irradiationTicksLeft = 0;
    private int infusionProgress = 0;
    private float itemRotation = 0.0f;
    private float prevItemRotation = 0.0f;

    public AstralInfusionPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASTRAL_INFUSION_PEDESTAL_BE.get(), pos, state);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack != null ? stack : ItemStack.EMPTY;
        this.infusionProgress = 0;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // --- net.minecraft.world.Container Implementation (Automated Hopper/Pipe Insertion) ---
    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return heldItem.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? heldItem : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot == 0 && !heldItem.isEmpty()) {
            ItemStack split = heldItem.split(amount);
            if (heldItem.isEmpty()) heldItem = ItemStack.EMPTY;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return split;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0) {
            ItemStack stack = heldItem;
            heldItem = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            setHeldItem(stack);
        }
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        setHeldItem(ItemStack.EMPTY);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && heldItem.isEmpty() && infusionProgress == 0;
    }

    public boolean isIrradiated() {
        return isIrradiated;
    }

    public float getInterpolatedRotation(float partialTick) {
        return prevItemRotation + (itemRotation - prevItemRotation) * partialTick;
    }

    public int getInfusionProgress() {
        return infusionProgress;
    }

    public void receiveIrradiation(String starName) {
        this.isIrradiated = true;
        this.activeStarName = starName != null ? starName : "Uncalibrated";
        this.irradiationTicksLeft = 10;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AstralInfusionPedestalBlockEntity be) {
        be.prevItemRotation = be.itemRotation;

        if (be.irradiationTicksLeft > 0) {
            be.irradiationTicksLeft--;
            be.isIrradiated = true;
            be.itemRotation = (be.itemRotation + 3.0f) % 360.0f;
        } else {
            be.isIrradiated = false;
            be.itemRotation = (be.itemRotation + 0.8f) % 360.0f;
        }

        if (level.isClientSide()) {
            return;
        }

        if (be.isIrradiated && !be.heldItem.isEmpty()) {
            be.infusionProgress++;

            if (level instanceof ServerLevel serverLevel && level.getGameTime() % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 2, 0.1, 0.1, 0.1, 0.02);
            }

            if (be.infusionProgress >= 100) { // 5 seconds of focused starlight
                ItemStack result = getTransmutationResult(be.heldItem, be.activeStarName);
                if (!result.isEmpty()) {
                    be.heldItem = result;
                    be.infusionProgress = 0;
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 1.5f);
                    level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.3f);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 16, 0.2, 0.2, 0.2, 0.05);
                        serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 8, 0.1, 0.1, 0.1, 0.02);
                    }

                    // Substrate Metamorphism: Transmute foundation stone or soil directly beneath pedestal
                    BlockPos belowPos = pos.below();
                    BlockState belowState = level.getBlockState(belowPos);
                    String lowerStar = (be.activeStarName != null) ? be.activeStarName.toLowerCase() : "";
                    if (belowState.is(Blocks.DIRT) || belowState.is(Blocks.GRASS_BLOCK)) {
                        if (lowerStar.contains("ignis") || lowerStar.contains("pyre") || lowerStar.contains("athanor")) {
                            level.setBlockAndUpdate(belowPos, ModBlocks.SOOTY_MARBLE.get().defaultBlockState());
                        } else if (lowerStar.contains("serpens") || lowerStar.contains("void") || lowerStar.contains("vorago") || lowerStar.contains("abyss")) {
                            level.setBlockAndUpdate(belowPos, ModBlocks.ENGRAVED_ASTRAL_SLATE.get().defaultBlockState());
                        } else if (lowerStar.contains("vitae") || lowerStar.contains("arbor") || lowerStar.contains("penna") || lowerStar.contains("aether")) {
                            level.setBlockAndUpdate(belowPos, ModBlocks.ASTRAL_MARBLE.get().defaultBlockState());
                        }
                    }

                    // Downward Container Auto-Ejection (Logistics QoL)
                    tryEjectDownward(level, pos, be);

                    be.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        } else {
            be.infusionProgress = Math.max(0, be.infusionProgress - 1);
        }
    }

    private static void tryEjectDownward(Level level, BlockPos pos, AstralInfusionPedestalBlockEntity be) {
        if (be.heldItem.isEmpty()) return;
        BlockPos belowPos = pos.below();
        BlockEntity belowBE = level.getBlockEntity(belowPos);
        if (belowBE instanceof net.minecraft.world.Container container) {
            ItemStack remaining = insertIntoContainer(container, be.heldItem, Direction.UP);
            be.heldItem = remaining;
            be.setChanged();
            level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3);
        }
    }

    private static ItemStack insertIntoContainer(net.minecraft.world.Container container, ItemStack stack, Direction side) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack toInsert = stack.copy();

        if (container instanceof net.minecraft.world.WorldlyContainer worldly) {
            int[] slots = worldly.getSlotsForFace(side != null ? side : Direction.UP);
            for (int slot : slots) {
                if (worldly.canPlaceItemThroughFace(slot, toInsert, side)) {
                    ItemStack slotStack = worldly.getItem(slot);
                    if (slotStack.isEmpty()) {
                        worldly.setItem(slot, toInsert);
                        return ItemStack.EMPTY;
                    } else if (ItemStack.isSameItemSameComponents(slotStack, toInsert)) {
                        int max = Math.min(worldly.getMaxStackSize(), slotStack.getMaxStackSize());
                        int transfer = Math.min(toInsert.getCount(), max - slotStack.getCount());
                        if (transfer > 0) {
                            slotStack.grow(transfer);
                            toInsert.shrink(transfer);
                            if (toInsert.isEmpty()) return ItemStack.EMPTY;
                        }
                    }
                }
            }
        } else {
            int size = container.getContainerSize();
            for (int slot = 0; slot < size; slot++) {
                if (container.canPlaceItem(slot, toInsert)) {
                    ItemStack slotStack = container.getItem(slot);
                    if (slotStack.isEmpty()) {
                        container.setItem(slot, toInsert);
                        return ItemStack.EMPTY;
                    } else if (ItemStack.isSameItemSameComponents(slotStack, toInsert)) {
                        int max = Math.min(container.getMaxStackSize(), slotStack.getMaxStackSize());
                        int transfer = Math.min(toInsert.getCount(), max - slotStack.getCount());
                        if (transfer > 0) {
                            slotStack.grow(transfer);
                            toInsert.shrink(transfer);
                            if (toInsert.isEmpty()) return ItemStack.EMPTY;
                        }
                    }
                }
            }
        }
        return toInsert;
    }

    public static ItemStack getTransmutationResult(ItemStack input, String starName) {
        if (input.isEmpty()) return ItemStack.EMPTY;

        if (input.is(Items.GLASS)) {
            return new ItemStack(ModBlocks.ASTRAL_MIRROR_BLOCK.get().asItem());
        } else if (input.is(Items.BIRCH_SAPLING)) {
            return new ItemStack(ModBlocks.STARLIGHT_AETHER_BIRCH_SAPLING.get().asItem());
        } else if (input.is(Items.BIRCH_LOG)) {
            return new ItemStack(ModBlocks.STARLIGHT_AETHER_BIRCH_LOG.get().asItem());
        } else if (input.is(Items.IRON_INGOT)) {
            return new ItemStack(ModItems.ARCANITE_INGOT.get());
        } else if (input.is(Items.GOLD_INGOT)) {
            return new ItemStack(ModItems.VISCANITE_INGOT.get());
        } else if (input.is(Items.OBSIDIAN)) {
            return new ItemStack(ModBlocks.SOOTY_MARBLE.get().asItem());
        } else if (input.is(ModBlocks.ASTRAL_MARBLE.get().asItem())) {
            return new ItemStack(ModBlocks.RUNED_ASTRAL_MARBLE.get().asItem());
        } else if (input.is(ModItems.ASTRAL_CRYSTAL.get())) {
            return new ItemStack(ModItems.ASTRAL_CRYSTAL_SEED.get(), 4);
        }

        // Optical Flora Swapping: Transmute base vanilla flowers into elemental orchids
        boolean isVanillaFlower = input.is(net.minecraft.tags.ItemTags.FLOWERS) ||
                                  input.is(Items.POPPY) || input.is(Items.DANDELION) ||
                                  input.is(Items.BLUE_ORCHID) || input.is(Items.ALLIUM) ||
                                  input.is(Items.AZURE_BLUET) || input.is(Items.RED_TULIP) ||
                                  input.is(Items.ORANGE_TULIP) || input.is(Items.WHITE_TULIP) ||
                                  input.is(Items.PINK_TULIP) || input.is(Items.OXEYE_DAISY) ||
                                  input.is(Items.CORNFLOWER) || input.is(Items.LILY_OF_THE_VALLEY);

        if (isVanillaFlower) {
            String lower = (starName != null) ? starName.toLowerCase() : "";
            if (lower.contains("ignis") || lower.contains("pyre") || lower.contains("athanor") || lower.contains("flame")) {
                return new ItemStack(ModBlocks.SOUL_FLAME_ORCHID.get().asItem());
            } else if (lower.contains("vitae") || lower.contains("arbor") || lower.contains("sylvan") || lower.contains("tree")) {
                return new ItemStack(ModBlocks.VITAE_ORCHID.get().asItem());
            } else if (lower.contains("scutum") || lower.contains("aegis") || lower.contains("shield")) {
                return new ItemStack(ModBlocks.AEGIS_SPIRE_ORCHID.get().asItem());
            } else if (lower.contains("serpens") || lower.contains("vorago") || lower.contains("abyss") || lower.contains("void") || lower.contains("shadow")) {
                return new ItemStack(ModBlocks.VOID_STALKER_ORCHID.get().asItem());
            } else {
                return new ItemStack(ModBlocks.VITAE_ORCHID.get().asItem());
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("HeldItem", ItemStack.CODEC, this.heldItem);
        output.store("IsIrradiated", Codec.BOOL, this.isIrradiated);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("IrradiationTicksLeft", Codec.INT, this.irradiationTicksLeft);
        output.store("InfusionProgress", Codec.INT, this.infusionProgress);
        output.store("ItemRotation", Codec.FLOAT, this.itemRotation);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("HeldItem", ItemStack.CODEC).ifPresent(stack -> this.heldItem = stack);
        input.read("IsIrradiated", Codec.BOOL).ifPresent(r -> this.isIrradiated = r);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("IrradiationTicksLeft", Codec.INT).ifPresent(t -> this.irradiationTicksLeft = t);
        input.read("InfusionProgress", Codec.INT).ifPresent(p -> this.infusionProgress = p);
        input.read("ItemRotation", Codec.FLOAT).ifPresent(r -> {
            this.itemRotation = r;
            this.prevItemRotation = r;
        });
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
