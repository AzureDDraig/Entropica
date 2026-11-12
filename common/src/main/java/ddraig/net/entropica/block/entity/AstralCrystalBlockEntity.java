package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.block.AstralCrystalClusterBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * AstralCrystalBlockEntity — Backs the procedural irregular Astral Crystal clusters & buds.
 * Completely inert, lightweight, and tick-free.
 */
public class AstralCrystalBlockEntity extends BlockEntity {

    private int stage = 4;

    public AstralCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASTRAL_CRYSTAL_BE.get(), pos, state);
        if (state.getBlock() instanceof AstralCrystalClusterBlock clusterBlock) {
            this.stage = clusterBlock.getStage();
        }
    }

    public AstralCrystalBlockEntity(BlockPos pos, BlockState state, int stage) {
        super(ModBlockEntities.ASTRAL_CRYSTAL_BE.get(), pos, state);
        this.stage = stage;
    }

    public int getStage() {
        if (getBlockState().getBlock() instanceof AstralCrystalClusterBlock clusterBlock) {
            return clusterBlock.getStage();
        }
        return this.stage;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
