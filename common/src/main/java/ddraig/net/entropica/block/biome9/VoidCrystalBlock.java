package ddraig.net.entropica.block.biome9;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Void Crystal Block — resonant Null Materia crystal lattice native to Null-Fringe Bay.
 * Slowly buds Void Crystal Buds and matures them into full Void Crystal Clusters.
 */
public class VoidCrystalBlock extends Block {
    public static final MapCodec<VoidCrystalBlock> CODEC = simpleCodec(VoidCrystalBlock::new);

    public VoidCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }
        Direction dir = Direction.getRandom(random);
        BlockPos targetPos = pos.relative(dir);
        BlockState targetState = level.getBlockState(targetPos);

        if (canClusterGrowAtState(targetState)) {
            if (ModBlocks.VOID_CRYSTAL_BUD.isBound()) {
                boolean waterlogged = targetState.getFluidState().getType() == Fluids.WATER;
                BlockState budState = ModBlocks.VOID_CRYSTAL_BUD.get().defaultBlockState()
                        .setValue(VoidCrystalClusterBlock.FACING, dir)
                        .setValue(VoidCrystalClusterBlock.WATERLOGGED, waterlogged);
                level.setBlockAndUpdate(targetPos, budState);
            }
        } else if (ModBlocks.VOID_CRYSTAL_BUD.isBound()
                && targetState.is(ModBlocks.VOID_CRYSTAL_BUD.get())
                && targetState.getValue(VoidCrystalClusterBlock.FACING) == dir) {
            if (ModBlocks.VOID_CRYSTAL_CLUSTER.isBound()) {
                BlockState clusterState = ModBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState()
                        .setValue(VoidCrystalClusterBlock.FACING, dir)
                        .setValue(VoidCrystalClusterBlock.WATERLOGGED, targetState.getValue(VoidCrystalClusterBlock.WATERLOGGED));
                level.setBlockAndUpdate(targetPos, clusterState);
            }
        }
    }

    private static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || (state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8);
    }
}
