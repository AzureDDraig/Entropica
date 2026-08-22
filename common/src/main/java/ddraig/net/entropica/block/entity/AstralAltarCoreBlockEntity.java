package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.block.AstralAltarCoreBlock;
import ddraig.net.entropica.item.AstralCrystalItem;
import ddraig.net.entropica.item.CompletedStarChartItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class AstralAltarCoreBlockEntity extends BlockEntity {

    private ItemStack heldItem = ItemStack.EMPTY;
    private ItemStack starChart = ItemStack.EMPTY;

    private boolean structureValid = false;
    private boolean starlightActive = false;
    private int crystalGrowthTicks = 0;
    private int ritualProgress = 0;
    private boolean crafting = false;

    public float clientRotation = 0.0f;

    public AstralAltarCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ASTRAL_ALTAR_CORE_BE.get(), pos, blockState);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(64.0, 64.0, 64.0);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack;
        this.crystalGrowthTicks = 0;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public ItemStack getStarChart() {
        return starChart;
    }

    public void setStarChart(ItemStack chart) {
        this.starChart = chart;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public boolean isStarlightActive() {
        return starlightActive;
    }

    public int getRitualProgress() {
        return ritualProgress;
    }

    public Constellation getAttunedConstellation() {
        if (starChart.isEmpty()) return null;
        return CompletedStarChartItem.getConstellation(starChart);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AstralAltarCoreBlockEntity be) {
        if (level.isClientSide()) {
            be.clientRotation += 0.02f;
            if (be.starlightActive) {
                if (level.random.nextFloat() < 0.3f) {
                    level.addParticle(ParticleTypes.END_ROD,
                            pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8,
                            pos.getY() + 1.2 + level.random.nextDouble() * 0.5,
                            pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8,
                            0, 0.02, 0);
                }
            }
            return;
        }

        // Structure check every 20 ticks
        if (level.getGameTime() % 20 == 0) {
            boolean wasValid = be.structureValid;
            be.structureValid = be.checkMultiblockStructure();

            boolean skyClear = level.canSeeSky(pos.above());
            long dayTime = level.getDayTime() % 24000L;
            boolean isNight = (dayTime >= 12500L && dayTime <= 23500L) && !level.isRaining();
            be.starlightActive = be.structureValid && skyClear && isNight;

            boolean changedBlockState = false;
            if (state.getValue(AstralAltarCoreBlock.FORMED) != be.structureValid) {
                state = state.setValue(AstralAltarCoreBlock.FORMED, be.structureValid);
                changedBlockState = true;
            }
            if (state.getValue(AstralAltarCoreBlock.ACTIVE) != be.starlightActive) {
                state = state.setValue(AstralAltarCoreBlock.ACTIVE, be.starlightActive);
                changedBlockState = true;
            }
            if (changedBlockState) {
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }

            if (!wasValid && be.structureValid) {
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 30, 1.5, 0.5, 1.5, 0.1);
                }
            }
        }

        // Crystal Starlight Growth
        if (be.starlightActive && be.heldItem.getItem() instanceof AstralCrystalItem) {
            int size = AstralCrystalItem.getSize(be.heldItem);
            if (size < 5) {
                be.crystalGrowthTicks++;
                if (be.crystalGrowthTicks % 40 == 0 && level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.05);
                }
                if (be.crystalGrowthTicks >= 2400) { // 2 minutes of direct starlight
                    AstralCrystalItem.setSize(be.heldItem, size + 1);
                    be.crystalGrowthTicks = 0;
                    be.setChanged();
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 1.5f);
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 20, 0.3, 0.4, 0.3, 0.08);
                    }
                }
            }
        }

        // Drained Crystal Tool Starlight Restoration (30 seconds / 600 ticks)
        if (be.starlightActive && be.heldItem.getItem() instanceof ddraig.net.entropica.item.DrainedCrystalToolItem drained) {
            be.crystalGrowthTicks++;
            if (be.crystalGrowthTicks % 20 == 0 && level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.05);
            }
            if (be.crystalGrowthTicks >= 600) {
                be.heldItem = drained.getRestoredVariant(be.heldItem);
                be.crystalGrowthTicks = 0;
                be.setChanged();
                level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.5f);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.1);
                }
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
        }

        // Active Crafting Ritual
        if (be.crafting) {
            be.ritualProgress++;
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.02);
            }
            if (be.ritualProgress >= 100) {
                be.finishCrafting();
            }
        }
    }

    public void triggerWandInteraction(Player player) {
        if (!structureValid) {
            displayAltarStatus(player);
            return;
        }

        if (!starlightActive) {
            player.displayClientMessage(Component.literal("§c[Astral Altar] Starlight required. Await clear night skies or align focal beams."), true);
            return;
        }

        if (crafting) {
            player.displayClientMessage(Component.literal("§e[Astral Altar] Ritual already in progress (§b" + ritualProgress + "%§e)."), true);
            return;
        }

        if (tryStartCrafting()) {
            crafting = true;
            ritualProgress = 0;
            if (level != null) {
                level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 1.4f);
            }
            player.displayClientMessage(Component.literal("§d[Astral Altar] Celestial Infusion Ritual commenced!"), true);
        } else {
            player.displayClientMessage(Component.literal("§7[Astral Altar] No valid infusion pattern found on surrounding pedestals."), true);
        }
    }

    private boolean tryStartCrafting() {
        List<AttunementPedestalBlockEntity> pedestals = getSurroundingPedestals();
        if (pedestals.size() < 4) return false;

        List<ItemStack> pedestalItems = new ArrayList<>();
        for (AttunementPedestalBlockEntity p : pedestals) {
            if (!p.getHeldItem().isEmpty()) {
                pedestalItems.add(p.getHeldItem());
            }
        }

        if (heldItem.is(ModItems.ASTRAL_CRYSTAL.get()) && pedestalItems.size() == 4) {
            return true;
        }
        if (heldItem.is(ModItems.RESPLENDENT_PRISM.get()) && pedestalItems.size() == 4) {
            return true;
        }

        return false;
    }

    private void finishCrafting() {
        crafting = false;
        ritualProgress = 0;
        List<AttunementPedestalBlockEntity> pedestals = getSurroundingPedestals();
        for (AttunementPedestalBlockEntity p : pedestals) {
            ItemStack stack = p.getHeldItem();
            if (!stack.isEmpty()) {
                stack.shrink(1);
                p.setHeldItem(stack);
            }
        }

        if (heldItem.is(ModItems.ASTRAL_CRYSTAL.get())) {
            heldItem = new ItemStack(ModItems.RESPLENDENT_PRISM.get());
        } else if (heldItem.is(ModItems.RESPLENDENT_PRISM.get())) {
            heldItem = new ItemStack(ModItems.MANTLE_OF_THE_STARS.get());
        }

        setChanged();
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.2f, 1.2f);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5, worldPosition.getY() + 1.3, worldPosition.getZ() + 0.5, 40, 0.4, 0.4, 0.4, 0.1);
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    private List<AttunementPedestalBlockEntity> getSurroundingPedestals() {
        List<AttunementPedestalBlockEntity> list = new ArrayList<>();
        if (level == null) return list;

        BlockPos[] offsets = {
                worldPosition.offset(3, 0, 0),
                worldPosition.offset(-3, 0, 0),
                worldPosition.offset(0, 0, 3),
                worldPosition.offset(0, 0, -3),
                worldPosition.offset(2, 0, 2),
                worldPosition.offset(2, 0, -2),
                worldPosition.offset(-2, 0, 2),
                worldPosition.offset(-2, 0, -2)
        };

        for (BlockPos p : offsets) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof AttunementPedestalBlockEntity pedestal) {
                list.add(pedestal);
            }
        }
        return list;
    }

    public boolean checkMultiblockStructure() {
        if (level == null) return false;

        // LAYER 1 (Y = -1, 7x7 Foundation):
        BlockPos base = worldPosition.below();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos checkPos = base.offset(dx, 0, dz);
                BlockState bs = level.getBlockState(checkPos);
                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);

                if (absX == 3 && absZ == 3) {
                    if (!bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE.get())) return false;
                } else if (absX == 3 || absZ == 3) {
                    if (!bs.is(ModBlocks.ENGRAVED_ASTRAL_SLATE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                } else {
                    if (!bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE.get()) && !bs.is(ModBlocks.CHISELED_ASTRAL_MARBLE.get())) return false;
                }
            }
        }

        // LAYER 2 to 5 (Y = 0 to +3, 4 Corner Resonance Pylons at (±3, ±3)):
        for (int dy = 0; dy <= 3; dy++) {
            BlockPos[] pylonPositions = {
                    worldPosition.offset(3, dy, 3),
                    worldPosition.offset(3, dy, -3),
                    worldPosition.offset(-3, dy, 3),
                    worldPosition.offset(-3, dy, -3)
            };
            for (BlockPos p : pylonPositions) {
                BlockState bs = level.getBlockState(p);
                if (!bs.is(ModBlocks.RESONANCE_PYLON.get()) && !bs.is(ModBlocks.STARLIGHT_PILLAR.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
                    return false;
                }
            }
        }

        // LAYER 2: 8 Attunement Pedestals:
        BlockPos[] pedestalPositions = {
                worldPosition.offset(3, 0, 0),
                worldPosition.offset(-3, 0, 0),
                worldPosition.offset(0, 0, 3),
                worldPosition.offset(0, 0, -3),
                worldPosition.offset(2, 0, 2),
                worldPosition.offset(2, 0, -2),
                worldPosition.offset(-2, 0, 2),
                worldPosition.offset(-2, 0, -2)
        };
        int pedestalsFound = 0;
        for (BlockPos p : pedestalPositions) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(ModBlocks.ATTUNEMENT_PEDESTAL.get()) || bs.is(ModBlocks.ASTRAL_PEDESTAL.get()) || bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
                pedestalsFound++;
            }
        }

        return pedestalsFound >= 4; // Tolerant to at least 4 active pedestals, up to 8
    }

    public void displayAltarStatus(Player player) {
        player.displayClientMessage(Component.literal("§6=== Modular Astral Altar Status ==="), false);
        player.displayClientMessage(Component.literal("§7Structure Integrity: " + (structureValid ? "§a✓ Formed & Aligned" : "§c✗ Incomplete (Check Astrolabe Blueprint)")), false);
        player.displayClientMessage(Component.literal("§7Starlight Focus: " + (starlightActive ? "§b✦ Active (Channeling Cosmos)" : "§8○ Inactive (Night Sky / Clear Line Required)")), false);

        Constellation c = getAttunedConstellation();
        if (c != null) {
            player.displayClientMessage(Component.literal("§7Attuned Constellation: §d" + Component.translatable(c.getUnlocalizedName()).getString() + " §7(Tier " + c.getTier() + ")"), false);
        } else {
            player.displayClientMessage(Component.literal("§7Attuned Constellation: §8None (Insert Completed Star Chart)"), false);
        }

        if (!heldItem.isEmpty()) {
            player.displayClientMessage(Component.literal("§7Held Reagent: §e" + heldItem.getHoverName().getString()), false);
            if (heldItem.getItem() instanceof AstralCrystalItem) {
                int size = AstralCrystalItem.getSize(heldItem);
                int progress = (int) ((crystalGrowthTicks / 2400.0f) * 100);
                player.displayClientMessage(Component.literal("§7  Crystal Size Growth: §a" + progress + "% §8(Size " + size + "/5)"), false);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("HeldItem", ItemStack.OPTIONAL_CODEC, this.heldItem);
        output.store("StarChart", ItemStack.OPTIONAL_CODEC, this.starChart);
        output.store("StructureValid", Codec.BOOL, this.structureValid);
        output.store("StarlightActive", Codec.BOOL, this.starlightActive);
        output.store("GrowthTicks", Codec.INT, this.crystalGrowthTicks);
        output.store("RitualProgress", Codec.INT, this.ritualProgress);
        output.store("Crafting", Codec.BOOL, this.crafting);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("HeldItem", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> this.heldItem = stack);
        input.read("StarChart", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> this.starChart = stack);
        input.read("StructureValid", Codec.BOOL).ifPresent(b -> this.structureValid = b);
        input.read("StarlightActive", Codec.BOOL).ifPresent(b -> this.starlightActive = b);
        input.read("GrowthTicks", Codec.INT).ifPresent(i -> this.crystalGrowthTicks = i);
        input.read("RitualProgress", Codec.INT).ifPresent(i -> this.ritualProgress = i);
        input.read("Crafting", Codec.BOOL).ifPresent(b -> this.crafting = b);
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
