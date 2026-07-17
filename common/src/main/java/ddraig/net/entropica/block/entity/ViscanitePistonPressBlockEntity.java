package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;

public class ViscanitePistonPressBlockEntity extends BlockEntity {
    private boolean isActive = false;
    private int animationTick = 0;

    public ViscanitePistonPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VISCANITE_PISTON_PRESS_BE.get(), pos, state);
    }

    public boolean isActive() {
        return this.isActive;
    }

    public int getAnimationTick() {
        return this.animationTick;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setAnimationTick(int tick) {
        this.animationTick = tick;
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isActive = input.read("IsActive", Codec.BOOL).orElse(false);
        this.animationTick = input.read("AnimationTick", Codec.INT).orElse(0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsActive", Codec.BOOL, this.isActive);
        output.store("AnimationTick", Codec.INT, this.animationTick);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
