package ddraig.net.entropica.block.biome5;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Blue Frozen Moss Carpet — 1/16-height pale-blue Glacial moss mat native to 2.5 Silent Fjord.
 * Cushions falls by 40% and sheds gentle snowflake motes.
 */
public class BlueFrozenMossCarpetBlock extends Block {
    public static final MapCodec<BlueFrozenMossCarpetBlock> CODEC = simpleCodec(BlueFrozenMossCarpetBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public BlueFrozenMossCarpetBlock(Properties properties) {
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
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.isAir()
                || ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(belowState)
                || belowState.is(net.minecraft.tags.BlockTags.LEAVES)) {
            return false;
        }
        return belowState.isFaceSturdy(level, belowPos, Direction.UP)
                || ddraig.net.entropica.block.EntropicaFloraGroundHelper.isSolidSnowBlock(belowState);
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

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 0.6F, level.damageSources().fall());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(14) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.1D;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
