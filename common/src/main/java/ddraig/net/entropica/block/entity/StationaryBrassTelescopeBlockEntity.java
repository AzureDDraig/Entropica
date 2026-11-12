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

public class StationaryBrassTelescopeBlockEntity extends BlockEntity {

    private float yaw = 0.0f;
    private float pitch = 25.0f;
    private float prevYaw = 0.0f;
    private float prevPitch = 25.0f;

    public StationaryBrassTelescopeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STATIONARY_BRASS_TELESCOPE_BE.get(), pos, state);
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setAngles(float yaw, float pitch) {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.yaw = yaw;
        this.pitch = pitch;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public float getInterpolatedYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.prevYaw, this.yaw);
    }

    public float getInterpolatedPitch(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevPitch, this.pitch);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Yaw", Codec.FLOAT, this.yaw);
        output.store("Pitch", Codec.FLOAT, this.pitch);
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
