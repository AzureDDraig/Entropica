package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFungalShelfBlock extends HorizontalDirectionalBlock {

    protected static final VoxelShape SHAPE_NORTH = Block.box(1.0, 6.0, 0.0, 15.0, 10.0, 12.0);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(1.0, 6.0, 4.0, 15.0, 10.0, 16.0);
    protected static final VoxelShape SHAPE_WEST  = Block.box(0.0, 6.0, 1.0, 12.0, 10.0, 15.0);
    protected static final VoxelShape SHAPE_EAST  = Block.box(4.0, 6.0, 1.0, 16.0, 10.0, 15.0);

    public AbstractFungalShelfBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos supportPos = pos.relative(direction.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);

        // Restricted to Logs, Crimson/Warped Stems, Hyphae, Wood, Mushroom Grow Blocks, or Sturdy Wooden Faces
        boolean isLogOrStem = supportState.is(BlockTags.LOGS) ||
                              supportState.is(BlockTags.WART_BLOCKS) ||
                              supportState.is(BlockTags.MUSHROOM_GROW_BLOCK);

        return isLogOrStem && supportState.isFaceSturdy(level, supportPos, direction);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction[] directions = context.getNearestLookingDirections();

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction opposite = direction.getOpposite();
                state = state.setValue(FACING, opposite);
                if (state.canSurvive(level, pos)) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide()) {
            applyTouchEffect(level, pos, entity);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!level.isClientSide()) {
            applyTouchEffect(level, pos, entity);
        }
    }

    protected abstract void applyTouchEffect(Level level, BlockPos pos, Entity entity);
}
