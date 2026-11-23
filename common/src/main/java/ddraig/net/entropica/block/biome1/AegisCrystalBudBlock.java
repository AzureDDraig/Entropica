package ddraig.net.entropica.block.biome1;

import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Stage 1 young Aegis Crystal Bud. When anchored to Aegis Crystal or glacial ice,
 * slowly matures into a Stage 2 {@link ModBlocks#AEGIS_CRYSTAL_CLUSTER}.
 */
public class AegisCrystalBudBlock extends AmethystClusterBlock {

    public AegisCrystalBudBlock(float height, float aabbOffset, Properties properties) {
        super(height, aabbOffset, properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) != 0) {
            return;
        }
        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockState host = level.getBlockState(pos.relative(facing.getOpposite()));
        if (isGrowthHost(host)) {
            BlockState mature = ModBlocks.AEGIS_CRYSTAL_CLUSTER.get().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, facing)
                    .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
            if (mature.canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, mature);
            }
        }
    }

    private static boolean isGrowthHost(BlockState host) {
        return host.is(ModBlocks.AEGIS_CRYSTAL_BLOCK.get())
                || host.is(ModBlocks.RUNIC_BLUE_ICE.get())
                || host.is(Blocks.BLUE_ICE)
                || host.is(Blocks.PACKED_ICE);
    }
}
