package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.block.TidalPulseResonatorBlock;
import ddraig.net.entropica.gravity.GravityField;
import ddraig.net.entropica.gravity.GravityFieldManager;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TidalPulseResonatorBlockEntity extends BlockEntity {

    private double radius = 16.0;
    private int cycleTicks = 0;
    private final ResourceLocation fieldId;

    // Animation & rendering states
    public float prongVibration = 0.0F;
    public float prevProngVibration = 0.0F;
    public float coreScale = 1.0F;
    public float prevCoreScale = 1.0F;
    public float coreSpin = 0.0F;
    public float prevCoreSpin = 0.0F;
    public float waveRingRadius = 0.0F;
    public float prevWaveRingRadius = 0.0F;
    public float waveRingAlpha = 0.0F;
    public float prevWaveRingAlpha = 0.0F;

    public TidalPulseResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIDAL_PULSE_RESONATOR_BE.get(), pos, state);
        this.fieldId = ResourceLocation.fromNamespaceAndPath("entropica", "tidal_resonator_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        boolean powered = state.getValue(TidalPulseResonatorBlock.POWERED);
        boolean active = state.getValue(TidalPulseResonatorBlock.ACTIVE) && !powered;

        if (level.isClientSide()) {
            prevProngVibration = prongVibration;
            prevCoreScale = coreScale;
            prevCoreSpin = coreSpin;
            prevWaveRingRadius = waveRingRadius;
            prevWaveRingAlpha = waveRingAlpha;

            if (active) {
                cycleTicks = (cycleTicks + 1) % 80;
                coreSpin += (cycleTicks >= 60) ? 12.0F : 4.0F;

                if (cycleTicks < 60) {
                    // Float phase (0..59 ticks): gentle harmonic oscillation
                    float floatProgress = cycleTicks / 60.0F;
                    prongVibration = 0.03F * Mth.sin(cycleTicks * 0.8F);
                    coreScale = 1.0F + 0.12F * Mth.sin(floatProgress * (float) Math.PI * 2.0F);
                    waveRingAlpha = 0.0F;

                    if (level.getRandom().nextFloat() < 0.2F) {
                        double px = pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.6;
                        double py = pos.getY() + 0.7 + level.getRandom().nextDouble() * 0.4;
                        double pz = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.6;
                        level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.03, 0);
                    }
                } else {
                    // Slam phase (60..79 ticks): violent shudder and expanding shockwave ring
                    int slamTick = cycleTicks - 60;
                    float slamProgress = slamTick / 20.0F;
                    prongVibration = 0.15F * Mth.sin(cycleTicks * 2.5F);
                    coreScale = 0.7F + 0.5F * (1.0F - slamProgress);
                    waveRingRadius = slamProgress * 2.0F;
                    waveRingAlpha = 1.0F - slamProgress;

                    if (level.getRandom().nextFloat() < 0.4F) {
                        double px = pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.2;
                        double py = pos.getY() + 0.1;
                        double pz = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.2;
                        level.addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0, -0.05, 0);
                    }
                }
            } else {
                prongVibration = 0.0F;
                coreScale = 0.8F;
                waveRingAlpha = 0.0F;
            }
        } else {
            // Server side field management & sound chimes
            if (active) {
                cycleTicks = (cycleTicks + 1) % 80;

                GravityField field = GravityFieldManager.getField(fieldId);
                if (field == null || field.getMode() != GravityField.Mode.TIDAL_PULSE) {
                    field = new GravityField(fieldId, level.dimension(), pos, radius, GravityField.Mode.TIDAL_PULSE);
                    field.setAgeTicks(this.cycleTicks);
                    GravityFieldManager.registerField(field);
                }

                // Phase transition sound events
                if (cycleTicks == 0) {
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.2F);
                } else if (cycleTicks == 60) {
                    level.playSound(null, pos, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.BLOCKS, 1.2F, 0.75F);
                }
            } else {
                GravityFieldManager.unregisterField(fieldId);
            }
        }
    }

    public void setActive(boolean active) {
        if (level != null && !level.isClientSide()) {
            if (active) {
                GravityField field = new GravityField(fieldId, level.dimension(), getBlockPos(), radius, GravityField.Mode.TIDAL_PULSE);
                field.setAgeTicks(this.cycleTicks);
                GravityFieldManager.registerField(field);
            } else {
                GravityFieldManager.unregisterField(fieldId);
            }
            setChanged();
        }
    }

    public float getInterpolatedProngVibration(float partialTick) {
        return Mth.lerp(partialTick, prevProngVibration, prongVibration);
    }

    public float getInterpolatedCoreScale(float partialTick) {
        return Mth.lerp(partialTick, prevCoreScale, coreScale);
    }

    public float getInterpolatedCoreSpin(float partialTick) {
        return Mth.rotLerp(partialTick, prevCoreSpin, coreSpin);
    }

    public float getInterpolatedWaveRadius(float partialTick) {
        return Mth.lerp(partialTick, prevWaveRingRadius, waveRingRadius);
    }

    public float getInterpolatedWaveAlpha(float partialTick) {
        return Mth.lerp(partialTick, prevWaveRingAlpha, waveRingAlpha);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        setChanged();
    }

    @Override
    public void setRemoved() {
        GravityFieldManager.unregisterField(fieldId);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putDouble("Radius", this.radius);
        output.putInt("CycleTicks", this.cycleTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.radius = input.getDoubleOr("Radius", 16.0);
        this.cycleTicks = input.getIntOr("CycleTicks", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
