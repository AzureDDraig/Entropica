package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity for Graviton Bouncepad.
 * Simulates pneumatic piston compression, spring-damper recoil, and iridescent membrane shimmer.
 */
public class GravitonBouncepadBlockEntity extends BlockEntity {

    private float compression = 0.0F;
    private float prevCompression = 0.0F;
    private float compressionVelocity = 0.0F;
    private int bounceTimer = 0;

    public GravitonBouncepadBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GRAVITON_BOUNCEPAD_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GravitonBouncepadBlockEntity be) {
        be.prevCompression = be.compression;

        if (be.bounceTimer > 0) {
            be.bounceTimer--;
        }

        // Spring-damper recoil physics: target = 0.0
        float springForce = (0.0F - be.compression) * 0.40F;
        be.compressionVelocity += springForce;
        be.compressionVelocity *= 0.75F; // Damping
        be.compression += be.compressionVelocity;

        if (Math.abs(be.compression) < 0.002F && Math.abs(be.compressionVelocity) < 0.002F) {
            be.compression = 0.0F;
            be.compressionVelocity = 0.0F;
        }
    }

    public void triggerBounce() {
        this.compression = 1.0F;
        this.compressionVelocity = 0.15F;
        this.bounceTimer = 15;
        if (this.level != null && !this.level.isClientSide()) {
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public float getCompression(float partialTick) {
        return this.prevCompression + (this.compression - this.prevCompression) * partialTick;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
