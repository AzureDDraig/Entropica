package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import java.util.function.Supplier;

public class BuddingCrystalBlock extends AmethystBlock {
    private final Supplier<Block> smallBud;
    private final Supplier<Block> mediumBud;
    private final Supplier<Block> largeBud;
    private final Supplier<Block> cluster;

    public BuddingCrystalBlock(Properties properties, Supplier<Block> smallBud, Supplier<Block> mediumBud, Supplier<Block> largeBud, Supplier<Block> cluster) {
        super(properties);
        this.smallBud = smallBud;
        this.mediumBud = mediumBud;
        this.largeBud = largeBud;
        this.cluster = cluster;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            Block block = null;
            if (canGrowInto(blockstate)) {
                block = smallBud.get();
            } else if (blockstate.is(smallBud.get()) && blockstate.getValue(BlockStateProperties.FACING) == direction) {
                block = mediumBud.get();
            } else if (blockstate.is(mediumBud.get()) && blockstate.getValue(BlockStateProperties.FACING) == direction) {
                block = largeBud.get();
            } else if (blockstate.is(largeBud.get()) && blockstate.getValue(BlockStateProperties.FACING) == direction) {
                block = cluster.get();
            }

            if (block != null) {
                BlockState blockstate1 = block.defaultBlockState()
                        .setValue(BlockStateProperties.FACING, direction)
                        .setValue(BlockStateProperties.WATERLOGGED, blockstate.getFluidState().getType() == Fluids.WATER);
                level.setBlockAndUpdate(blockpos, blockstate1);
            }
        }
    }

    public static boolean canGrowInto(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }
}
