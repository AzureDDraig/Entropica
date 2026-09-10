package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.IlluminatedBalustradeBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class IlluminatedBalustradeBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {

    public enum BalustradeConnection implements net.minecraft.util.StringRepresentable {
        NONE("none"),
        FLAT("flat"),
        UP("up"),
        DOWN("down");

        private final String name;

        BalustradeConnection(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public boolean isConnected() {
            return this != NONE;
        }

        public boolean isSlanted() {
            return this == UP || this == DOWN;
        }
    }

    public static final net.minecraft.world.level.block.state.properties.EnumProperty<BalustradeConnection> NORTH = net.minecraft.world.level.block.state.properties.EnumProperty.create("north", BalustradeConnection.class);
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<BalustradeConnection> EAST  = net.minecraft.world.level.block.state.properties.EnumProperty.create("east",  BalustradeConnection.class);
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<BalustradeConnection> SOUTH = net.minecraft.world.level.block.state.properties.EnumProperty.create("south", BalustradeConnection.class);
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<BalustradeConnection> WEST  = net.minecraft.world.level.block.state.properties.EnumProperty.create("west",  BalustradeConnection.class);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape POST = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape NORTH_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 6.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0, 0.0, 10.0, 10.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE  = Block.box(0.0, 0.0, 6.0, 6.0, 16.0, 10.0);
    private static final VoxelShape EAST_SHAPE  = Block.box(10.0, 0.0, 6.0, 16.0, 16.0, 10.0);

    public IlluminatedBalustradeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, BalustradeConnection.NONE)
                .setValue(EAST, BalustradeConnection.NONE)
                .setValue(SOUTH, BalustradeConnection.NONE)
                .setValue(WEST, BalustradeConnection.NONE)
                .setValue(LIT, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, LIT, WATERLOGGED);
    }

    public static BalustradeConnection getConnection(LevelReader level, BlockPos pos, Direction dir) {
        BlockPos frontPos = pos.relative(dir);
        // 1. Stair going UP in front
        BlockState aboveState = level.getBlockState(frontPos.above());
        if (aboveState.getBlock() instanceof IlluminatedBalustradeBlock) {
            return BalustradeConnection.UP;
        }
        // 2. Stair going DOWN in front
        BlockState belowState = level.getBlockState(frontPos.below());
        if (belowState.getBlock() instanceof IlluminatedBalustradeBlock) {
            return BalustradeConnection.DOWN;
        }
        // 3. Horizontal connection
        BlockState flatState = level.getBlockState(frontPos);
        if (flatState.getBlock() instanceof IlluminatedBalustradeBlock || flatState.isFaceSturdy(level, frontPos, dir.getOpposite())) {
            return BalustradeConnection.FLAT;
        }
        return BalustradeConnection.NONE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = POST;
        if (state.getValue(NORTH).isConnected()) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(SOUTH).isConnected()) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(WEST).isConnected())  shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(EAST).isConnected())  shape = Shapes.or(shape, EAST_SHAPE);
        return shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;

        return defaultBlockState()
                .setValue(NORTH, getConnection(level, pos, Direction.NORTH))
                .setValue(SOUTH, getConnection(level, pos, Direction.SOUTH))
                .setValue(WEST,  getConnection(level, pos, Direction.WEST))
                .setValue(EAST,  getConnection(level, pos, Direction.EAST))
                .setValue(LIT, false)
                .setValue(WATERLOGGED, waterlogged);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state
                .setValue(NORTH, getConnection(level, pos, Direction.NORTH))
                .setValue(SOUTH, getConnection(level, pos, Direction.SOUTH))
                .setValue(WEST,  getConnection(level, pos, Direction.WEST))
                .setValue(EAST,  getConnection(level, pos, Direction.EAST));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IlluminatedBalustradeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (lvl, p, st, be) -> {
            if (be instanceof IlluminatedBalustradeBlockEntity balustrade) {
                IlluminatedBalustradeBlockEntity.tick(lvl, p, st, balustrade);
            }
        };
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
