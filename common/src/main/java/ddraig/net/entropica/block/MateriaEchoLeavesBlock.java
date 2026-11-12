package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MateriaEchoLeavesBlock extends Block implements BonemealableBlock {

    public MateriaEchoLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);

        BlockPos belowPos = pos.below();
        if (level.isEmptyBlock(belowPos)) {
            if (random.nextInt(20) == 0) {
                level.setBlock(belowPos, ModBlocks.MATERIA_ECHO_FRUIT.get().defaultBlockState().setValue(MateriaEchoFruitBlock.AGE, 0), 2);
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.isEmptyBlock(pos.below());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos belowPos = pos.below();
        if (level.isEmptyBlock(belowPos)) {
            level.setBlock(belowPos, ModBlocks.MATERIA_ECHO_FRUIT.get().defaultBlockState().setValue(MateriaEchoFruitBlock.AGE, 0), 2);
        }
    }
}
