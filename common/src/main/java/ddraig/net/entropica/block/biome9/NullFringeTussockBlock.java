package ddraig.net.entropica.block.biome9;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
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
 * Null-Fringe Tussock — buoyant Void Materia sedge growing along fractured Null-Fringe Bay shores.
 * Cushions falls and grants brief Slow Falling when brushed through.
 */
public class NullFringeTussockBlock extends Block {
    public static final MapCodec<NullFringeTussockBlock> CODEC = simpleCodec(NullFringeTussockBlock::new);
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public NullFringeTussockBlock(Properties properties) {
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
        BlockState below = level.getBlockState(pos.below());
        if (ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(below)) {
            return false;
        }
        return (ModBlocks.VOID_SAND.isBound() && below.is(ModBlocks.VOID_SAND.get()))
                || (ModBlocks.VOID_SAND_TURF.isBound() && below.is(ModBlocks.VOID_SAND_TURF.get()))
                || (ModBlocks.VOID_STONE.isBound() && below.is(ModBlocks.VOID_STONE.get()))
                || ddraig.net.entropica.block.EntropicaFloraGroundHelper.isSolidSnowBlock(below)
                || below.is(Blocks.SAND)
                || below.is(Blocks.OBSIDIAN)
                || below.is(Blocks.END_STONE)
                || below.is(Blocks.DIRT)
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.COARSE_DIRT);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (level instanceof ServerLevel && entity instanceof LivingEntity living && living.isAlive() && !living.isSpectator()) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false, true));
            living.resetFallDistance();
        }
    }
}
