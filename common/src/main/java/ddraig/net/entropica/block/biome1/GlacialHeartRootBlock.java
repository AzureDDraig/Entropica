package ddraig.net.entropica.block.biome1;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
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
 * Ceiling-hanging crystalline root found beneath Blue-Glacier Shelf overhangs
 * and Frost-Veiled Glacial Runic Spire archways.
 */
public class GlacialHeartRootBlock extends Block {
    public static final MapCodec<GlacialHeartRootBlock> CODEC = simpleCodec(GlacialHeartRootBlock::new);
    protected static final VoxelShape SHAPE = Block.box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public GlacialHeartRootBlock(Properties properties) {
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
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        return aboveState.isFaceSturdy(level, abovePos, Direction.DOWN)
                || aboveState.is(BlockTags.ICE)
                || aboveState.is(Blocks.ICE)
                || aboveState.is(Blocks.PACKED_ICE)
                || aboveState.is(Blocks.BLUE_ICE)
                || aboveState.is(Blocks.BASALT)
                || aboveState.is(Blocks.SMOOTH_BASALT)
                || aboveState.is(Blocks.POLISHED_BASALT)
                || (ModBlocks.RUNIC_BLUE_ICE.isBound() && aboveState.is(ModBlocks.RUNIC_BLUE_ICE.get()))
                || (ModBlocks.RUNED_BASALT_PILLAR.isBound() && aboveState.is(ModBlocks.RUNED_BASALT_PILLAR.get()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(16) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.35D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, -0.015D, 0.0D);
        }
    }
}
