package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PureOpticFiberBlockEntity extends BlockEntity {

    private String activeStarName = "Astral";
    private int pulseTicksRemaining = 0;

    public PureOpticFiberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PURE_OPTIC_FIBER_BE.get(), pos, state);
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public boolean isPulsing() {
        return pulseTicksRemaining > 0;
    }

    public int getPulseTicksRemaining() {
        return pulseTicksRemaining;
    }

    public void receivePulse(String starName) {
        this.activeStarName = (starName != null && !starName.isEmpty()) ? starName : "Astral";
        this.pulseTicksRemaining = 25; // ~1.25 seconds pulse persistence buffer
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PureOpticFiberBlockEntity be) {
        if (be.pulseTicksRemaining > 0) {
            be.pulseTicksRemaining--;
            if (be.pulseTicksRemaining == 0) {
                be.setChanged();
                if (!level.isClientSide()) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("PulseTicksRemaining", Codec.INT, this.pulseTicksRemaining);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("PulseTicksRemaining", Codec.INT).ifPresent(p -> this.pulseTicksRemaining = p);
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
