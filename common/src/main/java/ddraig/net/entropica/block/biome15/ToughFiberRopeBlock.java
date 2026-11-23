package ddraig.net.entropica.block.biome15;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Tough-Fiber Rope — braided Dry Fibers climbing line for scaling Dust-Crusted Bergs and canyons.
 * Hangs freely downward from ceilings, overhangs, fences, or leaves, and supports top-down rope payout
 * up to 32 blocks downward when right-clicked with additional Tough-Fiber Rope!
 */
public class ToughFiberRopeBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<ToughFiberRopeBlock> CODEC = simpleCodec(ToughFiberRopeBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final VoxelShape SHAPE = Block.box(5.75D, 0.0D, 5.75D, 10.25D, 16.0D, 10.25D);
    public static final int MAX_PAYOUT_DEPTH = 32;

    public ToughFiberRopeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        BlockState candidate = this.defaultBlockState()
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        return candidate.canSurvive(context.getLevel(), context.getClickedPos()) ? candidate : null;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public boolean isSupportedFromAbove(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState up = level.getBlockState(above);
        if (up.is(this)
                || up.isFaceSturdy(level, above, Direction.DOWN)
                || Block.canSupportCenter(level, above, Direction.DOWN)) {
            return true;
        }
        try {
            return up.is(BlockTags.FENCES) || up.is(BlockTags.LEAVES);
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (isSupportedFromAbove(level, pos)) {
            return true;
        }
        BlockPos below = pos.below();
        return !level.getBlockState(pos.above()).is(this)
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (!state.canSurvive(level, pos)) {
            tickAccess.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean holdingRope = stack.getItem() == this.asItem()
                || (ModItems.TOUGH_FIBER_ROPE_ITEM.isBound() && stack.is(ModItems.TOUGH_FIBER_ROPE_ITEM.get()));

        if (holdingRope) {
            for (int dy = 1; dy <= MAX_PAYOUT_DEPTH; dy++) {
                BlockPos check = pos.below(dy);
                if (check.getY() < level.getMinY()) {
                    break;
                }
                BlockState cs = level.getBlockState(check);
                if (cs.is(this)) {
                    continue;
                }
                if (cs.isAir() || cs.is(Blocks.WATER) || cs.canBeReplaced()) {
                    if (!level.isClientSide()) {
                        boolean waterlogged = cs.getFluidState().getType() == Fluids.WATER;
                        if (!cs.isAir() && !cs.is(Blocks.WATER)) {
                            level.destroyBlock(check, true);
                        }
                        level.setBlock(check, this.defaultBlockState().setValue(WATERLOGGED, waterlogged), 3);
                        level.playSound(null, check, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.9F, 0.95F);
                        level.playSound(null, check, SoundEvents.LEAD_TIED, SoundSource.BLOCKS, 0.65F, 1.1F);
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
                break;
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
