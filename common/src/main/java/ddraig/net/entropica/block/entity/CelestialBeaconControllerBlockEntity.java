package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.block.CelestialBeaconControllerBlock;
import ddraig.net.entropica.item.CompletedStarChartItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CelestialBeaconControllerBlockEntity extends BlockEntity {

    private boolean structureValid = false;
    private boolean starlightActive = false;
    private ItemStack starChart = ItemStack.EMPTY;
    private float clientRotation = 0.0f;

    public CelestialBeaconControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CELESTIAL_BEACON_CONTROLLER_BE.get(), pos, state);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(64.0, 128.0, 64.0);
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public boolean isStarlightActive() {
        return starlightActive;
    }

    public float getClientRotation() {
        return clientRotation;
    }

    public ItemStack getStarChart() {
        return starChart;
    }

    public void setStarChart(ItemStack chart) {
        this.starChart = chart;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Nullable
    public Constellation getAttunedConstellation() {
        if (starChart.isEmpty()) return null;
        return CompletedStarChartItem.getConstellation(starChart);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CelestialBeaconControllerBlockEntity be) {
        if (level.isClientSide()) {
            be.clientRotation += 0.03f;
            if (be.starlightActive) {
                if (level.random.nextFloat() < 0.35f) {
                    level.addParticle(ParticleTypes.END_ROD,
                            pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 1.2,
                            pos.getY() + 1.5 + level.random.nextDouble() * 2.0,
                            pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 1.2,
                            0, 0.04, 0);
                }
            }
            return;
        }

        // Structure check every 20 ticks
        if (level.getGameTime() % 20 == 0) {
            boolean wasValid = be.structureValid;
            be.structureValid = be.checkBeaconStructure();

            boolean skyClear = level.canSeeSky(pos.above(4));
            long dayTime = level.getDayTime() % 24000L;
            boolean isNight = (dayTime >= 12500L && dayTime <= 23500L) && !level.isRaining();
            be.starlightActive = be.structureValid && skyClear && isNight;

            boolean changedBlockState = false;
            if (state.hasProperty(CelestialBeaconControllerBlock.FORMED) && state.getValue(CelestialBeaconControllerBlock.FORMED) != be.structureValid) {
                state = state.setValue(CelestialBeaconControllerBlock.FORMED, be.structureValid);
                changedBlockState = true;
            }
            if (state.hasProperty(CelestialBeaconControllerBlock.ACTIVE) && state.getValue(CelestialBeaconControllerBlock.ACTIVE) != be.starlightActive) {
                state = state.setValue(CelestialBeaconControllerBlock.ACTIVE, be.starlightActive);
                changedBlockState = true;
            }
            if (changedBlockState) {
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }

            if (!wasValid && be.structureValid) {
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.1f);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5, 50, 1.5, 1.0, 1.5, 0.15);
                }
            }
        }

        // Apply Beacon Area-of-Effect Auras (every 80 ticks = 4 seconds)
        if (be.starlightActive && level.getGameTime() % 80 == 0) {
            AABB area = new AABB(pos).inflate(64.0, 32.0, 64.0);
            List<Player> players = level.getEntitiesOfClass(Player.class, area);

            for (Player p : players) {
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 240, 1, true, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 0, true, false, true));
            }
        }
    }

    public boolean checkBeaconStructure() {
        if (level == null) return false;

        // LAYER 1 (Y = -1, 5x5 Stepped Plinth Foundation):
        BlockPos base = worldPosition.below();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos checkPos = base.offset(dx, 0, dz);
                BlockState bs = level.getBlockState(checkPos);
                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);

                if (absX == 2 && absZ == 2) {
                    if (!bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE.get())) return false;
                } else if (absX == 2 || absZ == 2) {
                    if (!bs.is(ModBlocks.ENGRAVED_ASTRAL_SLATE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                } else if (dx == 0 && dz == 0) {
                    if (!bs.is(ModBlocks.RUNED_ASTRAL_MARBLE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                } else {
                    if (!bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get()) && !bs.is(ModBlocks.RUNED_ASTRAL_MARBLE.get())) return false;
                }
            }
        }

        // LAYER 2 (Y = 0, Corner Chiseled & Cardinal Runed Marble):
        BlockPos[] chiseledCorners = {
                worldPosition.offset(2, 0, 2),
                worldPosition.offset(2, 0, -2),
                worldPosition.offset(-2, 0, 2),
                worldPosition.offset(-2, 0, -2)
        };
        for (BlockPos p : chiseledCorners) {
            BlockState bs = level.getBlockState(p);
            if (!bs.is(ModBlocks.CHISELED_ASTRAL_MARBLE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
        }

        BlockPos[] runedCardinals = {
                worldPosition.offset(2, 0, 0),
                worldPosition.offset(-2, 0, 0),
                worldPosition.offset(0, 0, 2),
                worldPosition.offset(0, 0, -2)
        };
        for (BlockPos p : runedCardinals) {
            BlockState bs = level.getBlockState(p);
            if (!bs.is(ModBlocks.RUNED_ASTRAL_MARBLE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
        }

        // LAYER 3 (Y = +1, 4 Corner Starlight Pillars):
        BlockPos[] pillarPos = {
                worldPosition.offset(2, 1, 2),
                worldPosition.offset(2, 1, -2),
                worldPosition.offset(-2, 1, 2),
                worldPosition.offset(-2, 1, -2)
        };
        for (BlockPos p : pillarPos) {
            BlockState bs = level.getBlockState(p);
            if (!bs.is(ModBlocks.STARLIGHT_PILLAR.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
        }

        // LAYER 4 (Y = +2, 4 Focus Crystal Brackets):
        BlockPos[] bracketPos = {
                worldPosition.offset(1, 2, 1),
                worldPosition.offset(1, 2, -1),
                worldPosition.offset(-1, 2, 1),
                worldPosition.offset(-1, 2, -1)
        };
        for (BlockPos p : bracketPos) {
            BlockState bs = level.getBlockState(p);
            if (!bs.is(ModBlocks.ASTRAL_CRYSTAL_BLOCK.get()) && !bs.is(ModBlocks.CHISELED_ASTRAL_MARBLE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
        }

        // LAYER 5 (Y = +3, Apex Crystal Crown):
        BlockPos apexPos = worldPosition.offset(0, 3, 0);
        BlockState apexBs = level.getBlockState(apexPos);
        return apexBs.is(ModBlocks.ASTRAL_CRYSTAL_BLOCK.get()) || apexBs.is(ModBlocks.ASTRAL_CRYSTAL_CLUSTER.get()) || apexBs.is(ModBlocks.CHISELED_ASTRAL_MARBLE.get());
    }

    public void displayBeaconStatus(Player player) {
        player.displayClientMessage(Component.literal("§d=== Celestial Beacon Sanctuary Status ==="), false);
        player.displayClientMessage(Component.literal("§7Structure Integrity: " + (structureValid ? "§a✓ Fully Formed & Resonant" : "§c✗ Incomplete (5x5x5 Blueprint Required)")), false);
        player.displayClientMessage(Component.literal("§7Celestial Starlight Focus: " + (starlightActive ? "§b✦ Active (Field Projection Online)" : "§8○ Inactive (Night Sky Line-of-Sight Required)")), false);
        if (structureValid && starlightActive) {
            player.displayClientMessage(Component.literal("§a✦ Wide-Area Singularity Wards & Cosmic Buffs Active (64-block radius)."), false);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("StructureValid", Codec.BOOL, this.structureValid);
        output.store("StarlightActive", Codec.BOOL, this.starlightActive);
        if (!this.starChart.isEmpty()) {
            output.store("StarChart", ItemStack.CODEC, this.starChart);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("StructureValid", Codec.BOOL).ifPresent(b -> this.structureValid = b);
        input.read("StarlightActive", Codec.BOOL).ifPresent(b -> this.starlightActive = b);
        input.read("StarChart", ItemStack.CODEC).ifPresent(s -> this.starChart = s);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("StructureValid", this.structureValid);
        tag.putBoolean("StarlightActive", this.starlightActive);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
