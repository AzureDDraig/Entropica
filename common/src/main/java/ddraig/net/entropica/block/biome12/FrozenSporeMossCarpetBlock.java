package ddraig.net.entropica.block.biome12;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.EntropicaFloraGroundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Frozen Spore Moss Carpet — thin 1px golden-frosted mycelial mat of 2.12 Spore-Glazed Pack.
 * Grows on sturdy soil, moss, wood, stone, or solid snow blocks, never on bare ice or thin snow layers.
 */
public class FrozenSporeMossCarpetBlock extends Block {
    public static final MapCodec<FrozenSporeMossCarpetBlock> CODEC = simpleCodec(FrozenSporeMossCarpetBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public FrozenSporeMossCarpetBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState bs = level.getBlockState(below);
        return !bs.isAir()
                && !EntropicaFloraGroundHelper.isIceOrSnowLayer(bs)
                && (bs.isFaceSturdy(level, below, Direction.UP) || EntropicaFloraGroundHelper.isSolidSnowBlock(bs));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }
}
