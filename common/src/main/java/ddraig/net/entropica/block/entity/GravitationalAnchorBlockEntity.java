package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.block.GravitationalAnchorBlock;
import ddraig.net.entropica.gravity.GravityField;
import ddraig.net.entropica.gravity.GravityFieldManager;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GravitationalAnchorBlockEntity extends BlockEntity {

    private double radius = 10.0;
    private final ResourceLocation fieldId;

    public float gimbalRotationX = 0.0F;
    public float gimbalRotationY = 0.0F;
    public float coreSpin = 0.0F;

    public float prevGimbalX = 0.0F;
    public float prevGimbalY = 0.0F;
    public float prevCoreSpin = 0.0F;

    public GravitationalAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAVITATIONAL_ANCHOR_BE.get(), pos, state);
        this.fieldId = ResourceLocation.fromNamespaceAndPath("entropica", "anchor_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        boolean isPowered = state.getValue(GravitationalAnchorBlock.POWERED);
        boolean isActive = !isPowered;

        if (level.isClientSide()) {
            prevGimbalX = gimbalRotationX;
            prevGimbalY = gimbalRotationY;
            prevCoreSpin = coreSpin;

            if (isActive) {
                gimbalRotationX += 2.0F;
                gimbalRotationY += 3.5F;
                coreSpin += 5.0F;
            }
        } else {
            if (isActive) {
                GravityField.Mode mode = state.getValue(GravitationalAnchorBlock.MODE);
                GravityField field = GravityFieldManager.getField(fieldId);
                if (field == null || field.getMode() != mode) {
                    field = new GravityField(fieldId, level.dimension(), pos, radius, mode);
                    GravityFieldManager.registerField(field);
                }
            } else {
                GravityFieldManager.unregisterField(fieldId);
            }
        }
    }

    public void onModeChanged(GravityField.Mode newMode) {
        if (level != null && !level.isClientSide()) {
            boolean isPowered = getBlockState().getValue(GravitationalAnchorBlock.POWERED);
            if (!isPowered) {
                GravityField field = new GravityField(fieldId, level.dimension(), getBlockPos(), radius, newMode);
                GravityFieldManager.registerField(field);
            }
            setChanged();
        }
    }

    public float getInterpolatedGimbalX(float partialTick) {
        return Mth.rotLerp(partialTick, prevGimbalX, gimbalRotationX);
    }

    public float getInterpolatedGimbalY(float partialTick) {
        return Mth.rotLerp(partialTick, prevGimbalY, gimbalRotationY);
    }

    public float getInterpolatedCoreSpin(float partialTick) {
        return Mth.rotLerp(partialTick, prevCoreSpin, coreSpin);
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
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.radius = input.getDoubleOr("Radius", 10.0);
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