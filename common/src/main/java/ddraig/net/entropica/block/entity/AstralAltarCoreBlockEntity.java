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

import ddraig.net.entropica.api.EssenceType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return new AABB(-30000000.0, -30000000.0, -30000000.0, 30000000.0, 30000000.0, 30000000.0);
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
                if (be.ritualProgress % 5 == 0) {
                    for (AttunementPedestalBlockEntity pedestal : be.getSurroundingPedestals()) {
                        BlockPos pPos = pedestal.getBlockPos();
                        double dx = (pos.getX() + 0.5) - (pPos.getX() + 0.5);
                        double dy = (pos.getY() + 1.2) - (pPos.getY() + 1.1);
                        double dz = (pos.getZ() + 0.5) - (pPos.getZ() + 0.5);
                        sl.sendParticles(ParticleTypes.END_ROD, pPos.getX() + 0.5, pPos.getY() + 1.1, pPos.getZ() + 0.5, 1, dx * 0.1, dy * 0.1 + 0.02, dz * 0.1, 0.08);
                    }
                }
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
            player.displayClientMessage(Component.literal("§e[Astral Altar] §7Ritual already in progress (§b" + ritualProgress + "%§7)."), true);
            return;
        }

        if (tryStartCrafting()) {
            crafting = true;
            ritualProgress = 0;
            if (level != null) {
                level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 1.4f);
            }
            player.displayClientMessage(Component.literal("§d[Astral Altar] §fCelestial Infusion Ritual commenced!"), true);
        } else {
            List<AttunementPedestalBlockEntity> pedestals = getSurroundingPedestals();
            List<ItemStack> pedestalStacks = new ArrayList<>();
            for (AttunementPedestalBlockEntity p : pedestals) {
                if (!p.getHeldItem().isEmpty()) {
                    pedestalStacks.add(p.getHeldItem());
                }
            }

            if (heldItem.isEmpty()) {
                player.displayClientMessage(Component.literal("§e[Astral Altar] §7Altar core is empty! Place an §fAstral Crystal §7or §fResplendent Prism §7on the center."), false);
            } else if (pedestals.size() < 4) {
                player.displayClientMessage(Component.literal("§e[Astral Altar] §7Only found §f" + pedestals.size() + "§7 pedestals nearby. Place at least 8 pedestals around the altar."), false);
            } else if (heldItem.is(ModItems.ASTRAL_CRYSTAL.get())) {
                int gold = 0;
                int glass = 0;
                for (ItemStack s : pedestalStacks) {
                    if (isGoldReagent(s)) gold++;
                    else if (isGlassReagent(s)) glass++;
                }
                player.displayClientMessage(Component.literal("§e[Astral Altar] §7Resplendent Prism requires §64 Gold §7and §b4 Glass §7reagents. (Found: §6" + gold + " Gold§7, §b" + glass + " Glass§7)."), false);
            } else if (heldItem.is(ModItems.RESPLENDENT_PRISM.get())) {
                int silk = 0;
                int thread = 0;
                int marble = 0;
                for (ItemStack s : pedestalStacks) {
                    if (s.is(ModItems.STARLIGHT_SILK.get())) silk++;
                    else if (s.is(ModItems.ASTRAL_CRYSTAL_THREAD.get())) thread++;
                    else if (isMarbleReagent(s)) marble++;
                }
                player.displayClientMessage(Component.literal("§e[Astral Altar] §7Mantle of the Stars requires §b5 Silk§7, §b2 Thread§7, §b1 Marble§7. (Found: §b" + silk + " Silk§7, §b" + thread + " Thread§7, §b" + marble + " Marble§7)."), false);
            } else {
                player.displayClientMessage(Component.literal("§e[Astral Altar] §7No matching infusion recipe for §f" + heldItem.getHoverName().getString() + " §7with " + pedestalStacks.size() + " reagents."), false);
            }
        }
    }

    public static boolean isGoldReagent(ItemStack s) {
        return s.is(net.minecraft.world.item.Items.GOLD_INGOT)
                || s.is(net.minecraft.world.item.Items.RAW_GOLD)
                || s.is(net.minecraft.world.item.Items.GOLD_BLOCK)
                || s.is(ModItems.VISCANITE_INGOT.get());
    }

    public static boolean isGlassReagent(ItemStack s) {
        return s.is(ModBlocks.ESSENCE_ENRICHED_GLASS.get().asItem())
                || s.is(ModBlocks.FRAGMENT_LATTICE_GLASS.get().asItem())
                || s.is(ModBlocks.ASTRAL_MIRROR_BLOCK.get().asItem())
                || s.is(ModBlocks.MATERIA_FUMUS_STRENGTHENED_GLASS.get().asItem())
                || s.is(ModBlocks.MATERIA_LIQUIDA_ENRICHED_GLASS.get().asItem())
                || s.is(net.minecraft.world.item.Items.GLASS)
                || s.is(net.minecraft.world.item.Items.TINTED_GLASS)
                || s.is(net.minecraft.tags.ItemTags.SMELTS_TO_GLASS);
    }

    public static boolean isMarbleReagent(ItemStack s) {
        return s.is(ModBlocks.RUNED_ASTRAL_MARBLE.get().asItem())
                || s.is(ModBlocks.ASTRAL_MARBLE.get().asItem())
                || s.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get().asItem())
                || s.is(ModBlocks.CHISELED_ASTRAL_MARBLE.get().asItem());
    }

    private boolean tryStartCrafting() {
        List<AttunementPedestalBlockEntity> pedestals = getSurroundingPedestals();
        if (pedestals.size() < 4) return false;

        List<ItemStack> pedestalStacks = new ArrayList<>();
        for (AttunementPedestalBlockEntity p : pedestals) {
            if (!p.getHeldItem().isEmpty()) {
                pedestalStacks.add(p.getHeldItem());
            }
        }
        if (pedestalStacks.size() != 8) return false;

        // Recipe 1: Resplendent Prism (Center: Astral Crystal + 4 Gold + 4 Glass)
        if (heldItem.is(ModItems.ASTRAL_CRYSTAL.get())) {
            int goldCount = 0;
            int glassCount = 0;
            for (ItemStack s : pedestalStacks) {
                if (isGoldReagent(s)) {
                    goldCount++;
                } else if (isGlassReagent(s)) {
                    glassCount++;
                }
            }
            return goldCount == 4 && glassCount == 4;
        }

        // Recipe 2: Mantle of the Stars (Center: Resplendent Prism + 5 Starlight Silk + 2 Astral Crystal Thread + 1 Marble)
        if (heldItem.is(ModItems.RESPLENDENT_PRISM.get())) {
            int silkCount = 0;
            int threadCount = 0;
            int marbleCount = 0;
            for (ItemStack s : pedestalStacks) {
                if (s.is(ModItems.STARLIGHT_SILK.get())) {
                    silkCount++;
                } else if (s.is(ModItems.ASTRAL_CRYSTAL_THREAD.get())) {
                    threadCount++;
                } else if (isMarbleReagent(s)) {
                    marbleCount++;
                }
            }
            return silkCount == 5 && threadCount == 2 && marbleCount == 1;
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
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5f, 1.8f);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5, worldPosition.getY() + 1.3, worldPosition.getZ() + 0.5, 50, 0.4, 0.4, 0.4, 0.1);
                sl.sendParticles(ParticleTypes.FIREWORK, worldPosition.getX() + 0.5, worldPosition.getY() + 1.3, worldPosition.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.08);
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    private List<AttunementPedestalBlockEntity> getSurroundingPedestals() {
        List<AttunementPedestalBlockEntity> list = new ArrayList<>();
        if (level == null) return list;

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (dx == 0 && dz == 0) continue;
                for (int dy = -1; dy <= 2; dy++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(p);
                    if (be instanceof AttunementPedestalBlockEntity pedestal) {
                        list.add(pedestal);
                    }
                }
            }
        }
        return list;
    }

    private int structureTier = 0; // 0 = Incomplete, 2 = Standard 7x7x7, 3 = Master 11x11x7
    private final Map<BlockPos, Integer> incomingCollectorBeams = new HashMap<>();
    private int collectorBeamsTimeout = 0;

    public int getStructureTier() {
        return structureTier;
    }

    public int getIncomingBeamCount() {
        return incomingCollectorBeams.size();
    }

    public int getTotalIncomingFlux() {
        return incomingCollectorBeams.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void receiveCollectorBeam(BlockPos fromPos, int flux, EssenceType incomingEssence) {
        this.incomingCollectorBeams.put(fromPos, flux);
        this.collectorBeamsTimeout = 25;
        this.starlightActive = true;
        setChanged();
    }

    public boolean checkMultiblockStructure() {
        if (level == null) return false;

        // 1. Check Master Astral Altar (11x11x7)
        if (checkMasterAltarStructure()) {
            this.structureTier = 3;
            return true;
        }

        // 2. Check Standard Modular Astral Altar (7x7x7)
        if (checkStandardAltarStructure()) {
            this.structureTier = 2;
            return true;
        }

        this.structureTier = 0;
        return false;
    }

    private boolean checkMasterAltarStructure() {
        // LAYER 1 (Y = -1, 11x11 Stepped Foundation Dais):
        BlockPos base = worldPosition.below();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                BlockPos checkPos = base.offset(dx, 0, dz);
                if (!level.hasChunkAt(checkPos)) return false;
                BlockState bs = level.getBlockState(checkPos);
                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);

                if (absX == 5 && absZ == 5) {
                    if (!bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE.get())) return false;
                } else if (absX == 5 || absZ == 5) {
                    if (!bs.is(ModBlocks.ENGRAVED_ASTRAL_SLATE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                } else {
                    if (!bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE.get()) && !bs.is(ModBlocks.CHISELED_ASTRAL_MARBLE.get())) return false;
                }
            }
        }

        // LAYER 2 to 6 (Y = 0 to +4, 4 Tall Corner Starlight Pillars at (±5, ±5)):
        for (int dy = 0; dy <= 4; dy++) {
            BlockPos[] pylonPositions = {
                    worldPosition.offset(5, dy, 5),
                    worldPosition.offset(5, dy, -5),
                    worldPosition.offset(-5, dy, 5),
                    worldPosition.offset(-5, dy, -5)
            };
            for (BlockPos p : pylonPositions) {
                if (!level.hasChunkAt(p)) return false;
                BlockState bs = level.getBlockState(p);
                if (!bs.is(ModBlocks.STARLIGHT_PILLAR.get()) && !bs.is(ModBlocks.RESONANCE_PYLON.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
                    return false;
                }
            }
        }

        // LAYER 7 (Y = +5, 4 Corner Receiver Lenses at (±5, 5, ±5) and Overhead Focal Mount at (0, 5, 0)):
        BlockPos[] lensPositions = {
                worldPosition.offset(5, 5, 5),
                worldPosition.offset(5, 5, -5),
                worldPosition.offset(-5, 5, 5),
                worldPosition.offset(-5, 5, -5)
        };
        for (BlockPos lp : lensPositions) {
            if (!level.hasChunkAt(lp)) return false;
            BlockState bs = level.getBlockState(lp);
            if (!bs.is(ModBlocks.REFRACTIVE_ASTRAL_LENS.get()) && !bs.is(ModBlocks.SECONDARY_ASTRAL_LENS.get()) && !bs.is(ModBlocks.FOCAL_LENS_MOUNT.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
                return false;
            }
        }

        BlockPos apexPos = worldPosition.offset(0, 5, 0);
        if (!level.hasChunkAt(apexPos)) return false;
        BlockState apexBs = level.getBlockState(apexPos);
        if (!apexBs.is(ModBlocks.FOCAL_LENS_MOUNT.get()) && !apexBs.is(ModBlocks.REFRACTIVE_ASTRAL_LENS.get()) && !apexBs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
            return false;
        }

        return true;
    }

    private boolean checkStandardAltarStructure() {
        // LAYER 1 (Y = -1, 7x7 Foundation):
        BlockPos base = worldPosition.below();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos checkPos = base.offset(dx, 0, dz);
                if (!level.hasChunkAt(checkPos)) return false;
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
                if (!level.hasChunkAt(p)) return false;
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
            if (!level.hasChunkAt(p)) return false;
            BlockState bs = level.getBlockState(p);
            if (bs.is(ModBlocks.ATTUNEMENT_PEDESTAL.get()) || bs.is(ModBlocks.ASTRAL_PEDESTAL.get()) || bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
                pedestalsFound++;
            }
        }

        return pedestalsFound >= 4; // Tolerant to at least 4 active pedestals, up to 8
    }

    public void displayAltarStatus(Player player) {
        player.displayClientMessage(Component.literal("§6=== Astral Altar Status ==="), false);
        String tierName = (structureTier == 3) ? "§d✦ Tier 3 (Master Astral Altar 11x11x7)" : ((structureTier == 2) ? "§b✦ Tier 2 (Modular Astral Altar 7x7x7)" : "§c✗ Incomplete");
        player.displayClientMessage(Component.literal("§7Structure Tier: " + tierName), false);
        player.displayClientMessage(Component.literal("§7Starlight Focus: " + (starlightActive ? "§b✦ Active (Channeling Cosmos)" : "§8○ Inactive (Night Sky / Clear Line Required)")), false);

        if (incomingCollectorBeams.size() > 0) {
            player.displayClientMessage(Component.literal("§7Connected Collection Beams: §a" + incomingCollectorBeams.size() + " Beams §7(+" + getTotalIncomingFlux() + " flux/s)"), false);
        }

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
        output.store("StructureTier", Codec.INT, this.structureTier);
        output.store("StarlightActive", Codec.BOOL, this.starlightActive);
        output.store("GrowthTicks", Codec.INT, this.crystalGrowthTicks);
        output.store("RitualProgress", Codec.INT, this.ritualProgress);
        output.store("Crafting", Codec.BOOL, this.crafting);
        output.store("IncomingFlux", Codec.INT, getTotalIncomingFlux());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("HeldItem", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> this.heldItem = stack);
        input.read("StarChart", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> this.starChart = stack);
        input.read("StructureValid", Codec.BOOL).ifPresent(b -> this.structureValid = b);
        input.read("StructureTier", Codec.INT).ifPresent(t -> this.structureTier = t);
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
