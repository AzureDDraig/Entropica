package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.PureOpticFiberBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class OpticalTransmitterPortBlockEntity extends BlockEntity {

    private boolean isTransmitting = false;
    private String activeStarName = "Uncalibrated";
    private int transmittingTicksLeft = 0;

    public OpticalTransmitterPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPTICAL_TRANSMITTER_PORT_BE.get(), pos, state);
    }

    public boolean isTransmitting() {
        return isTransmitting;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public void receiveOpticalBeam(String starName) {
        this.isTransmitting = true;
        this.activeStarName = starName != null ? starName : "Uncalibrated";
        this.transmittingTicksLeft = 10;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OpticalTransmitterPortBlockEntity be) {
        if (be.transmittingTicksLeft > 0) {
            be.transmittingTicksLeft--;
            be.isTransmitting = true;
        } else {
            be.isTransmitting = false;
        }

        if (level.isClientSide()) {
            return;
        }

        if (be.isTransmitting && level.getGameTime() % 2 == 0) {
            propagateFiberNetwork(level, pos, be.activeStarName);
        }
    }

    public static void propagateFiberNetwork(Level level, BlockPos startPos, String starName) {
        record FiberNode(BlockPos pos, int distSinceBooster) {}
        Queue<FiberNode> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(new FiberNode(startPos, 0));
        visited.add(startPos);

        int maxTotalDistance = 64;

        while (!queue.isEmpty()) {
            FiberNode current = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.pos().relative(dir);
                if (visited.contains(neighbor)) continue;

                BlockState neighborState = level.getBlockState(neighbor);
                net.minecraft.world.level.block.Block neighborBlock = neighborState.getBlock();

                if (neighborBlock instanceof PureOpticFiberBlock) {
                    visited.add(neighbor);
                    if (visited.size() <= maxTotalDistance) {
                        queue.add(new FiberNode(neighbor, current.distSinceBooster() + 1));
                    }
                } else if (neighborBlock instanceof ddraig.net.entropica.block.OpticalBoosterAmplifierBlock) {
                    visited.add(neighbor);
                    if (visited.size() <= maxTotalDistance) {
                        queue.add(new FiberNode(neighbor, 0));
                    }
                } else if (neighborBlock instanceof ddraig.net.entropica.block.OpticalReceiverPortBlock) {
                    visited.add(neighbor);
                    BlockEntity receiverBE = level.getBlockEntity(neighbor);
                    if (receiverBE instanceof OpticalReceiverPortBlockEntity receiver) {
                        float signalQuality = Math.max(0.2f, 1.0f - (current.distSinceBooster() / 16.0f) * 0.01f);
                        receiver.receiveTransmittedSignal(starName, signalQuality);
                    }
                } else if (neighborBlock instanceof ddraig.net.entropica.block.AstralCollectorBlock) {
                    visited.add(neighbor);
                    BlockEntity collectorBE = level.getBlockEntity(neighbor);
                    if (collectorBE instanceof AstralCollectorBlockEntity collector) {
                        EssenceType starEssence = RefractiveAstralLensBlockEntity.resolveStarEssence(starName);
                        collector.receiveStarlightBeam(starName, starEssence);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsTransmitting", Codec.BOOL, this.isTransmitting);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("TransmittingTicksLeft", Codec.INT, this.transmittingTicksLeft);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("IsTransmitting", Codec.BOOL).ifPresent(t -> this.isTransmitting = t);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("TransmittingTicksLeft", Codec.INT).ifPresent(l -> this.transmittingTicksLeft = l);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        CompoundTag tag = super.getUpdateTag(p);
        tag.putBoolean("IsTransmitting", this.isTransmitting);
        tag.putString("ActiveStarName", this.activeStarName);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
