package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RefractiveAstralLensBlockEntity extends BlockEntity {

    private float yaw = 0.0f;
    private float pitch = 45.0f;
    private float prevYaw = 0.0f;
    private float prevPitch = 45.0f;
    private String targetName = "Uncalibrated";
    private boolean isFocused = false;

    public RefractiveAstralLensBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFRACTIVE_ASTRAL_LENS_BE.get(), pos, state);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getTargetName() {
        return targetName;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocus(float yaw, float pitch, String targetName, boolean isFocused) {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.yaw = yaw;
        this.pitch = pitch;
        this.targetName = (targetName != null && !targetName.isBlank()) ? targetName : "Uncalibrated";
        this.isFocused = isFocused;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public float getInterpolatedYaw(float partialTick) {
        return Mth.rotLerp(partialTick, this.prevYaw, this.yaw);
    }

    public float getInterpolatedPitch(float partialTick) {
        return Mth.lerp(partialTick, this.prevPitch, this.pitch);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Yaw", Codec.FLOAT, this.yaw);
        output.store("Pitch", Codec.FLOAT, this.pitch);
        output.store("TargetName", Codec.STRING, this.targetName);
        output.store("IsFocused", Codec.BOOL, this.isFocused);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Yaw", Codec.FLOAT).ifPresent(y -> {
            this.yaw = y;
            this.prevYaw = y;
        });
        input.read("Pitch", Codec.FLOAT).ifPresent(p -> {
            this.pitch = p;
            this.prevPitch = p;
        });
        input.read("TargetName", Codec.STRING).ifPresent(tn -> this.targetName = tn);
        input.read("IsFocused", Codec.BOOL).ifPresent(f -> this.isFocused = f);
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
