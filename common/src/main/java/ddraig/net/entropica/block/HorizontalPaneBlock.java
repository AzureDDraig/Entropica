package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HorizontalPaneBlock extends Block implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape PANEL_X = Block.box(0.0D, 7.0D, 7.0D, 16.0D, 9.0D, 9.0D);
    protected static final VoxelShape PANEL_Z = Block.box(7.0D, 7.0D, 0.0D, 9.0D, 9.0D, 16.0D);

    protected static final VoxelShape PANEL_ARM_NORTH = Block.box(0.0D, 7.0D, 0.0D, 16.0D, 9.0D, 7.0D);
    protected static final VoxelShape PANEL_ARM_SOUTH = Block.box(0.0D, 7.0D, 9.0D, 16.0D, 9.0D, 16.0D);
    protected static final VoxelShape PANEL_ARM_WEST = Block.box(0.0D, 7.0D, 0.0D, 7.0D, 9.0D, 16.0D);
    protected static final VoxelShape PANEL_ARM_EAST = Block.box(9.0D, 7.0D, 0.0D, 16.0D, 9.0D, 16.0D);

    protected static final VoxelShape PANEL_ARM_UP_X = Block.box(0.0D, 9.0D, 7.0D, 16.0D, 16.0D, 9.0D);
    protected static final VoxelShape PANEL_ARM_UP_Z = Block.box(7.0D, 9.0D, 0.0D, 9.0D, 16.0D, 16.0D);
    protected static final VoxelShape PANEL_ARM_DOWN_X = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 7.0D, 9.0D);
    protected static final VoxelShape PANEL_ARM_DOWN_Z = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 7.0D, 16.0D);

    public HorizontalPaneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean isXAxis = (facing == Direction.NORTH || facing == Direction.SOUTH);
        VoxelShape shape = isXAxis ? PANEL_X : PANEL_Z;

        if (isXAxis) {
            if (state.getValue(NORTH)) shape = Shapes.or(shape, PANEL_ARM_NORTH);
            if (state.getValue(SOUTH)) shape = Shapes.or(shape, PANEL_ARM_SOUTH);
            if (state.getValue(UP)) shape = Shapes.or(shape, PANEL_ARM_UP_X);
            if (state.getValue(DOWN)) shape = Shapes.or(shape, PANEL_ARM_DOWN_X);
        } else {
            if (state.getValue(EAST)) shape = Shapes.or(shape, PANEL_ARM_EAST);
            if (state.getValue(WEST)) shape = Shapes.or(shape, PANEL_ARM_WEST);
            if (state.getValue(UP)) shape = Shapes.or(shape, PANEL_ARM_UP_Z);
            if (state.getValue(DOWN)) shape = Shapes.or(shape, PANEL_ARM_DOWN_Z);
        }

        return shape;
    }

    public boolean canConnectTo(BlockState state, boolean sideSolid) {
        return state.getBlock() instanceof HorizontalPaneBlock || state.is(BlockTags.IMPERMEABLE) || sideSolid;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = level.getFluidState(pos);
        Direction face = context.getClickedFace();
        Direction playerFacing = context.getHorizontalDirection();

        Direction defaultFacing = face.getAxis().isHorizontal() ? face : playerFacing;

        BlockState nState = level.getBlockState(pos.north());
        BlockState sState = level.getBlockState(pos.south());
        BlockState eState = level.getBlockState(pos.east());
        BlockState wState = level.getBlockState(pos.west());
        BlockState uState = level.getBlockState(pos.above());
        BlockState dState = level.getBlockState(pos.below());

        return this.defaultBlockState()
                .setValue(FACING, defaultFacing)
                .setValue(NORTH, canConnectTo(nState, nState.isFaceSturdy(level, pos.north(), Direction.SOUTH)))
                .setValue(SOUTH, canConnectTo(sState, sState.isFaceSturdy(level, pos.south(), Direction.NORTH)))
                .setValue(EAST, canConnectTo(eState, eState.isFaceSturdy(level, pos.east(), Direction.WEST)))
                .setValue(WEST, canConnectTo(wState, wState.isFaceSturdy(level, pos.west(), Direction.EAST)))
                .setValue(UP, canConnectTo(uState, uState.isFaceSturdy(level, pos.above(), Direction.DOWN)))
                .setValue(DOWN, canConnectTo(dState, dState.isFaceSturdy(level, pos.below(), Direction.UP)))
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        boolean solid = neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite());
        boolean connect = canConnectTo(neighborState, solid);

        return switch (direction) {
            case NORTH -> state.setValue(NORTH, connect);
            case SOUTH -> state.setValue(SOUTH, connect);
            case EAST  -> state.setValue(EAST, connect);
            case WEST  -> state.setValue(WEST, connect);
            case UP    -> state.setValue(UP, connect);
            case DOWN  -> state.setValue(DOWN, connect);
        };
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
