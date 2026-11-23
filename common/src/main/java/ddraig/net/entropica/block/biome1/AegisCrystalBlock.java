package ddraig.net.entropica.block.biome1;

import ddraig.net.entropica.block.BuddingCrystalBlock;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

/**
 * Solid Aegis Crystal block that slowly sprouts 2-stage Aegis Crystals on exposed faces:
 * Stage 1 ({@link ModBlocks#AEGIS_CRYSTAL_BUD}) -> Stage 2 ({@link ModBlocks#AEGIS_CRYSTAL_CLUSTER}).
 */
public class AegisCrystalBlock extends AmethystBlock {

    public AegisCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }
        Direction dir = Direction.values()[random.nextInt(Direction.values().length)];
        BlockPos targetPos = pos.relative(dir);
        BlockState targetState = level.getBlockState(targetPos);

        Block nextStage = null;
        if (BuddingCrystalBlock.canGrowInto(targetState)) {
            nextStage = ModBlocks.AEGIS_CRYSTAL_BUD.get();
        } else if (targetState.is(ModBlocks.AEGIS_CRYSTAL_BUD.get())
                && targetState.getValue(BlockStateProperties.FACING) == dir) {
            nextStage = ModBlocks.AEGIS_CRYSTAL_CLUSTER.get();
        }

        if (nextStage != null) {
            boolean waterlogged = targetState.getFluidState().getType() == Fluids.WATER;
            BlockState grown = nextStage.defaultBlockState()
                    .setValue(BlockStateProperties.FACING, dir)
                    .setValue(BlockStateProperties.WATERLOGGED, waterlogged);
            level.setBlockAndUpdate(targetPos, grown);
        }
    }
}
