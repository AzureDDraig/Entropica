package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.block.GravLiftProjectorBlock;
import ddraig.net.entropica.gravity.GravityField;
import ddraig.net.entropica.gravity.GravityFieldManager;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GravLiftProjectorBlockEntity extends BlockEntity {

    private int beamRange = 24;
    private float beamStrength = 1.0F;
    private final ResourceLocation fieldId;

    public GravLiftProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAV_LIFT_PROJECTOR_BE.get(), pos, state);
        this.fieldId = ResourceLocation.fromNamespaceAndPath("entropica", "grav_lift_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        boolean isPowered = state.getValue(GravLiftProjectorBlock.POWERED);
        boolean isActive = !isPowered;

        if (level.isClientSide()) {
            if (isActive && level.getRandom().nextFloat() < 0.35F) {
                Direction facing = state.getValue(GravLiftProjectorBlock.FACING);
                double offsetDist = level.getRandom().nextDouble() * beamRange;
                double px = pos.getX() + 0.5 + (facing.getStepX() * offsetDist) + (level.getRandom().nextDouble() - 0.5) * 0.4;
                double py = pos.getY() + 0.5 + (facing.getStepY() * offsetDist) + (level.getRandom().nextDouble() - 0.5) * 0.4;
                double pz = pos.getZ() + 0.5 + (facing.getStepZ() * offsetDist) + (level.getRandom().nextDouble() - 0.5) * 0.4;

                double vx = facing.getStepX() * 0.2;
                double vy = facing.getStepY() * 0.2;
                double vz = facing.getStepZ() * 0.2;

                level.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, vx, vy, vz);
            }
        } else {
            if (isActive) {
                Direction facing = state.getValue(GravLiftProjectorBlock.FACING);
                GravityField field = GravityFieldManager.getField(fieldId);
                if (field == null || field.getLiftDirection() != facing || field.getRadius() != beamRange || field.getStrength() != beamStrength) {
                    field = new GravityField(fieldId, level.dimension(), pos, beamRange, GravityField.Mode.GRAV_LIFT);
                    field.setLiftDirection(facing);
                    field.setStrength(beamStrength);
                    GravityFieldManager.registerField(field);
                }
            } else {
                GravityFieldManager.unregisterField(fieldId);
            }
        }
    }

    public void onFacingChanged(Direction nextFacing) {
        if (level != null && !level.isClientSide()) {
            boolean isPowered = getBlockState().getValue(GravLiftProjectorBlock.POWERED);
            if (!isPowered) {
                GravityField field = new GravityField(fieldId, level.dimension(), getBlockPos(), beamRange, GravityField.Mode.GRAV_LIFT);
                field.setLiftDirection(nextFacing);
                field.setStrength(beamStrength);
                GravityFieldManager.registerField(field);
            }
            setChanged();
        }
    }

    public int getBeamRange() {
        return beamRange;
    }

    public void setBeamRange(int beamRange) {
        this.beamRange = beamRange;
        setChanged();
    }

    public float getBeamStrength() {
        return beamStrength;
    }

    public void setBeamStrength(float beamStrength) {
        this.beamStrength = beamStrength;
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
        output.putInt("BeamRange", this.beamRange);
        output.putFloat("BeamStrength", this.beamStrength);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.beamRange = input.getIntOr("BeamRange", 24);
        this.beamStrength = input.getFloatOr("BeamStrength", 1.0F);
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