package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.block.OpticReceiverBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OpticReceiverBlockEntity extends BlockEntity {
    private String activeStarName = "";
    private int receivingTicksLeft = 0;

    public OpticReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPTIC_RECEIVER_BE.get(), pos, state);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(-30000000.0, -30000000.0, -30000000.0, 30000000.0, 30000000.0, 30000000.0);
    }

    public void receiveOpticalBeam(String starName) {
        this.activeStarName = starName != null ? starName : "";
        this.receivingTicksLeft = 10;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isReceiving() {
        return receivingTicksLeft > 0;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OpticReceiverBlockEntity be) {
        if (level.isClientSide()) {
            if (be.receivingTicksLeft > 0) {
                be.receivingTicksLeft--;
                if (level.random.nextFloat() < 0.35f) {
                    Direction facing = state.getValue(OpticReceiverBlock.FACING);
                    Vec3 center = Vec3.atCenterOf(pos).add(facing.getStepX() * 0.45, facing.getStepY() * 0.45, facing.getStepZ() * 0.45);
                    level.addParticle(ParticleTypes.END_ROD, center.x, center.y, center.z,
                            (level.random.nextDouble() - 0.5) * 0.04,
                            (level.random.nextDouble() - 0.5) * 0.04,
                            (level.random.nextDouble() - 0.5) * 0.04);
                }
            }
            return;
        }

        if (be.receivingTicksLeft > 0) {
            be.receivingTicksLeft--;
            if (level.getGameTime() % 2 == 0 && !be.activeStarName.isEmpty()) {
                OpticalTransmitterPortBlockEntity.propagateFiberNetwork(level, pos, be.activeStarName);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("ReceivingTicksLeft", Codec.INT, this.receivingTicksLeft);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("ReceivingTicksLeft", Codec.INT).ifPresent(t -> this.receivingTicksLeft = t);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
