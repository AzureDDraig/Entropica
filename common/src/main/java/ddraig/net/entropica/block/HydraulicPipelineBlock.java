package ddraig.net.entropica.block;

import ddraig.net.entropica.api.materia.ILiquidMateriaHandler;
import ddraig.net.entropica.api.materia.MateriaLiquidaStack;
import ddraig.net.entropica.block.entity.HydraulicPipelineBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class HydraulicPipelineBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST  = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST  = BlockStateProperties.WEST;
    public static final BooleanProperty UP    = BlockStateProperties.UP;
    public static final BooleanProperty DOWN  = BlockStateProperties.DOWN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape CORE     = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    private static final VoxelShape ARM_UP   = Block.box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape ARM_DOWN = Block.box(5.0D,  0.0D, 5.0D, 11.0D,  5.0D, 11.0D);
    private static final VoxelShape ARM_NORTH = Block.box(5.0D, 5.0D,  0.0D, 11.0D, 11.0D,  5.0D);
    private static final VoxelShape ARM_SOUTH = Block.box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D);
    private static final VoxelShape ARM_EAST  = Block.box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    private static final VoxelShape ARM_WEST  = Block.box( 0.0D, 5.0D, 5.0D,  5.0D, 11.0D, 11.0D);

    public HydraulicPipelineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST,  false)
                .setValue(SOUTH, false).setValue(WEST,  false)
                .setValue(UP,    false).setValue(DOWN,  false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HydraulicPipelineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null :
                (lvl, pos, st, blockEntity) -> {
                    if (blockEntity instanceof HydraulicPipelineBlockEntity pipe) {
                        pipe.tick(lvl, pos, st);
                    }
                };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        return this.defaultBlockState()
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER)
                .setValue(NORTH, canConnectTo(level.getBlockState(pos.north()), Direction.NORTH))
                .setValue(SOUTH, canConnectTo(level.getBlockState(pos.south()), Direction.SOUTH))
                .setValue(EAST,  canConnectTo(level.getBlockState(pos.east()),  Direction.EAST))
                .setValue(WEST,  canConnectTo(level.getBlockState(pos.west()),  Direction.WEST))
                .setValue(UP,    canConnectTo(level.getBlockState(pos.above()), Direction.UP))
                .setValue(DOWN,  canConnectTo(level.getBlockState(pos.below()), Direction.DOWN));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state.setValue(getDirectionProperty(direction), canConnectTo(neighborState, direction));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(UP))    shape = Shapes.or(shape, ARM_UP);
        if (state.getValue(DOWN))  shape = Shapes.or(shape, ARM_DOWN);
        if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_NORTH);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SOUTH);
        if (state.getValue(EAST))  shape = Shapes.or(shape, ARM_EAST);
        if (state.getValue(WEST))  shape = Shapes.or(shape, ARM_WEST);
        return shape;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof HydraulicPipelineBlockEntity pipe) {
                MateriaLiquidaStack stack = pipe.getLiquidInTank();
                if (stack.isEmpty()) {
                    player.displayClientMessage(Component.literal(
                            "§7Pipeline is Empty (0 / " + pipe.getCapacity() + " Mliq)"), true);
                } else {
                    player.displayClientMessage(Component.literal(
                            "§aPipeline contains: " + stack.getAmount() + " / " + pipe.getCapacity()
                            + " Mliq of " + stack.getType().getFormattedName()), true);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    protected boolean canConnectTo(BlockState neighborState, Direction dirToNeighbor) {
        return neighborState.getBlock() instanceof HydraulicPipelineBlock;
    }

    public static BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case UP    -> UP;
            case DOWN  -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST  -> EAST;
            case WEST  -> WEST;
        };
    }
}
