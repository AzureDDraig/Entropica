package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.DecompressionCouplerBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DecompressionCouplerBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    private static final VoxelShape CORE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    private static final VoxelShape ARM_UP = Block.box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape ARM_DOWN = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D);
    private static final VoxelShape ARM_NORTH = Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 5.0D);
    private static final VoxelShape ARM_SOUTH = Block.box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D);
    private static final VoxelShape ARM_EAST = Block.box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    private static final VoxelShape ARM_WEST = Block.box(0.0D, 5.0D, 5.0D, 5.0D, 11.0D, 11.0D);

    private static final VoxelShape SHAPE_Y = Shapes.or(CORE, ARM_UP, ARM_DOWN);
    private static final VoxelShape SHAPE_X = Shapes.or(CORE, ARM_EAST, ARM_WEST);
    private static final VoxelShape SHAPE_Z = Shapes.or(CORE, ARM_NORTH, ARM_SOUTH);

    public DecompressionCouplerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(AXIS);
        return switch (axis) {
            case X -> SHAPE_X;
            case Z -> SHAPE_Z;
            default -> SHAPE_Y;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    private boolean isConnectablePipe(BlockState state) {
        Block block = state.getBlock();
        String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
        return name.contains("pipe") || 
               name.contains("pipeline") || 
               name.contains("conduit") || 
               name.contains("valve") || 
               name.contains("diverter") || 
               name.contains("port") ||
               name.contains("agitator");
    }

    private Direction.Axis getSmartAxis(LevelReader level, BlockPos pos, Direction.Axis fallbackAxis) {
        int xCount = 0;
        int yCount = 0;
        int zCount = 0;

        // Check X axis neighbors (EAST, WEST)
        if (isConnectablePipe(level.getBlockState(pos.east()))) xCount++;
        if (isConnectablePipe(level.getBlockState(pos.west()))) xCount++;

        // Check Y axis neighbors (UP, DOWN)
        if (isConnectablePipe(level.getBlockState(pos.above()))) yCount++;
        if (isConnectablePipe(level.getBlockState(pos.below()))) yCount++;

        // Check Z axis neighbors (NORTH, SOUTH)
        if (isConnectablePipe(level.getBlockState(pos.north()))) zCount++;
        if (isConnectablePipe(level.getBlockState(pos.south()))) zCount++;

        if (xCount > yCount && xCount > zCount) {
            return Direction.Axis.X;
        } else if (zCount > xCount && zCount > yCount) {
            return Direction.Axis.Z;
        } else if (yCount > xCount && yCount > zCount) {
            return Direction.Axis.Y;
        }
        
        // If there's a tie, try to prefer any non-zero axis first
        if (xCount > 0) return Direction.Axis.X;
        if (zCount > 0) return Direction.Axis.Z;
        if (yCount > 0) return Direction.Axis.Y;

        return fallbackAxis;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis fallbackAxis = context.getClickedFace().getAxis();
        Direction.Axis smartAxis = getSmartAxis(context.getLevel(), context.getClickedPos(), fallbackAxis);
        return this.defaultBlockState().setValue(AXIS, smartAxis);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        Direction.Axis currentAxis = state.getValue(AXIS);
        Direction.Axis smartAxis = getSmartAxis(level, pos, currentAxis);
        if (smartAxis != currentAxis) {
            return state.setValue(AXIS, smartAxis);
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecompressionCouplerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof DecompressionCouplerBlockEntity coupler) {
                    coupler.tick(lvl, pos, st);
                }
            };
        }
        return null;
    }
}
