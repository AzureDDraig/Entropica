package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.block.CelestialArmillaryControllerBlock;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class CelestialArmillaryControllerBlockEntity extends BlockEntity {

    private boolean structureValid = false;
    private boolean starlightActive = false;
    private int beamCount = 0;

    public float colureAngle = 0.0f;
    public float eclipticAngle = 0.0f;
    public float coreAngle = 0.0f;

    public CelestialArmillaryControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CELESTIAL_ARMILLARY_CONTROLLER_BE.get(), pos, blockState);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(64.0, 64.0, 64.0);
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public boolean isStarlightActive() {
        return starlightActive;
    }

    public int getBeamCount() {
        return beamCount;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CelestialArmillaryControllerBlockEntity be) {
        // Continuous Gimbal Kinematics
        be.colureAngle += 0.008f;
        be.eclipticAngle += 0.015f;
        be.coreAngle += 0.025f;

        if (level.isClientSide()) {
            if (be.starlightActive && level.random.nextFloat() < 0.4f) {
                level.addParticle(ParticleTypes.END_ROD,
                        pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6,
                        pos.getY() + 0.8 + level.random.nextDouble() * 0.5,
                        pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6,
                        0, 0.03, 0);
            }
            return;
        }

        // Server validation every 20 ticks
        if (level.getGameTime() % 20 == 0) {
            boolean wasValid = be.structureValid;
            be.structureValid = be.checkObservatoryStructure();

            boolean skyClear = level.canSeeSky(pos.above());
            long dayTime = level.getDayTime() % 24000L;
            boolean isNight = (dayTime >= 12500L && dayTime <= 23500L) && !level.isRaining();
            be.starlightActive = be.structureValid && skyClear && isNight;

            boolean changedBlockState = false;
            if (state.getValue(CelestialArmillaryControllerBlock.FORMED) != be.structureValid) {
                state = state.setValue(CelestialArmillaryControllerBlock.FORMED, be.structureValid);
                changedBlockState = true;
            }
            if (state.getValue(CelestialArmillaryControllerBlock.ACTIVE) != be.starlightActive) {
                state = state.setValue(CelestialArmillaryControllerBlock.ACTIVE, be.starlightActive);
                changedBlockState = true;
            }
            if (changedBlockState) {
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }

            if (!wasValid && be.structureValid) {
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.0f);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 50, 2.0, 1.0, 2.0, 0.1);
                }
            }
        }
    }

    public boolean checkObservatoryStructure() {
        if (level == null) return false;

        // LAYER 1 (Y = -1 relative to controller, 9x9 Foundation & 7x7 Mirror Pool):
        BlockPos base = worldPosition.below();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos checkPos = base.offset(dx, 0, dz);
                BlockState bs = level.getBlockState(checkPos);
                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);

                if (absX == 4 && absZ == 4) {
                    if (!bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE.get())) return false;
                } else if (absX == 4 || absZ == 4) {
                    if (!bs.is(ModBlocks.ENGRAVED_ASTRAL_SLATE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                } else {
                    if (!bs.is(ModBlocks.ASTRAL_MIRROR_BLOCK.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                }
            }
        }

        // LAYER 2 to 6: 4 Corner Starlight Pillars at (±4, ±4) from Y=0 to Y=+4
        for (int dy = 0; dy <= 4; dy++) {
            BlockPos[] pillarPos = {
                    worldPosition.offset(4, dy, 4),
                    worldPosition.offset(4, dy, -4),
                    worldPosition.offset(-4, dy, 4),
                    worldPosition.offset(-4, dy, -4)
            };
            for (BlockPos p : pillarPos) {
                BlockState bs = level.getBlockState(p);
                if (!bs.is(ModBlocks.STARLIGHT_PILLAR.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE.get())) {
                    return false;
                }
            }
        }

        // LAYER 9 (Y=+7): 4 Cardinal Refractive Lenses at (0, 7, ±4) and (±4, 7, 0)
        BlockPos[] lensPos = {
                worldPosition.offset(4, 7, 0),
                worldPosition.offset(-4, 7, 0),
                worldPosition.offset(0, 7, 4),
                worldPosition.offset(0, 7, -4)
        };
        int lensesFound = 0;
        for (BlockPos p : lensPos) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(ModBlocks.REFRACTIVE_ASTRAL_LENS.get()) || bs.is(ModBlocks.SECONDARY_ASTRAL_LENS.get()) || bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
                lensesFound++;
            }
        }
        this.beamCount = lensesFound;

        return lensesFound >= 4;
    }

    public void displayObservatoryStatus(Player player) {
        player.displayClientMessage(Component.literal("§b=== Grand Astral Observatory Status ==="), false);
        player.displayClientMessage(Component.literal("§7Multiblock Integrity: " + (structureValid ? "§a✓ Fully Assembled & Calibrated" : "§c✗ Incomplete (9x9x9 Blueprint Required)")), false);
        player.displayClientMessage(Component.literal("§7Celestial Starlight Focus: " + (starlightActive ? "§b✦ Active (Cosmic Resonance)" : "§8○ Inactive (Night Sky Line-of-Sight Required)")), false);
        player.displayClientMessage(Component.literal("§7Cardinal Optical Lenses: §e" + beamCount + " / 4 Aligned"), false);
        if (structureValid && starlightActive) {
            player.displayClientMessage(Component.literal("§d✦ Master & Transcendent Constellation Scribing Unlocked!"), false);
            player.displayClientMessage(Component.literal("§a✦ 32x Maximum Celestial Magnification & Refractive Ocular Sphere Online."), false);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("StructureValid", Codec.BOOL, this.structureValid);
        output.store("StarlightActive", Codec.BOOL, this.starlightActive);
        output.store("BeamCount", Codec.INT, this.beamCount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("StructureValid", Codec.BOOL).ifPresent(b -> this.structureValid = b);
        input.read("StarlightActive", Codec.BOOL).ifPresent(b -> this.starlightActive = b);
        input.read("BeamCount", Codec.INT).ifPresent(c -> this.beamCount = c);
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
