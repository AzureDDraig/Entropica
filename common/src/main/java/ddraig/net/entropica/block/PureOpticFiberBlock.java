package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class PureOpticFiberBlock extends Block {
    public static final MapCodec<PureOpticFiberBlock> CODEC = simpleCodec(PureOpticFiberBlock::new);

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST  = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST  = BlockStateProperties.WEST;
    public static final BooleanProperty UP    = BlockStateProperties.UP;
    public static final BooleanProperty DOWN  = BlockStateProperties.DOWN;

    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = new EnumMap<>(Direction.class);

    static {
        PROPERTY_BY_DIRECTION.put(Direction.NORTH, NORTH);
        PROPERTY_BY_DIRECTION.put(Direction.EAST,  EAST);
        PROPERTY_BY_DIRECTION.put(Direction.SOUTH, SOUTH);
        PROPERTY_BY_DIRECTION.put(Direction.WEST,  WEST);
        PROPERTY_BY_DIRECTION.put(Direction.UP,    UP);
        PROPERTY_BY_DIRECTION.put(Direction.DOWN,  DOWN);
    }

    private static final VoxelShape CORE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    private static final VoxelShape ARM_NORTH = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
    private static final VoxelShape ARM_SOUTH = Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
    private static final VoxelShape ARM_WEST  = Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
    private static final VoxelShape ARM_EAST  = Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    private static final VoxelShape ARM_DOWN  = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
    private static final VoxelShape ARM_UP    = Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);

    public static final net.minecraft.world.level.block.state.properties.IntegerProperty COLOR_INDEX = net.minecraft.world.level.block.state.properties.IntegerProperty.create("color_index", 0, 16);

    public PureOpticFiberBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(COLOR_INDEX, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, COLOR_INDEX);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction, BlockState currentState) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        Block block = neighborState.getBlock();

        if (block instanceof PureOpticFiberBlock) {
            int currentColor = currentState != null ? currentState.getValue(COLOR_INDEX) : 0;
            int neighborColor = neighborState.getValue(COLOR_INDEX);
            // If either is undyed (0), or colors match, connect! If distinct colors, isolate and do not connect.
            return currentColor == 0 || neighborColor == 0 || currentColor == neighborColor;
        }

        return block instanceof OpticalTransmitterPortBlock
                || block instanceof OpticalReceiverPortBlock
                || block instanceof OpticalBoosterAmplifierBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState defaultState = this.defaultBlockState();
        return defaultState
                .setValue(NORTH, canConnectTo(level, pos, Direction.NORTH, defaultState))
                .setValue(EAST,  canConnectTo(level, pos, Direction.EAST, defaultState))
                .setValue(SOUTH, canConnectTo(level, pos, Direction.SOUTH, defaultState))
                .setValue(WEST,  canConnectTo(level, pos, Direction.WEST, defaultState))
                .setValue(UP,    canConnectTo(level, pos, Direction.UP, defaultState))
                .setValue(DOWN,  canConnectTo(level, pos, Direction.DOWN, defaultState));
    }

    @Override
    protected net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (stack.getItem() instanceof net.minecraft.world.item.DyeItem dyeItem) {
            net.minecraft.world.item.DyeColor dyeColor = dyeItem.getDyeColor();
            int newColorIdx = dyeColor.getId() + 1;
            if (state.getValue(COLOR_INDEX) != newColorIdx) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                BlockState newState = state.setValue(COLOR_INDEX, newColorIdx);
                level.setBlock(pos, newState, 3);
                if (!level.isClientSide()) {
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.DYE_USE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b[Optic Fiber] §7Dyed conduit casing to §f" + dyeColor.getName().toUpperCase()), true);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        BooleanProperty prop = PROPERTY_BY_DIRECTION.get(direction);
        if (prop != null) {
            return state.setValue(prop, canConnectTo(level, currentPos, direction, state));
        }
        return super.updateShape(state, level, tickAccess, currentPos, direction, neighborPos, neighborState, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_NORTH);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SOUTH);
        if (state.getValue(WEST))  shape = Shapes.or(shape, ARM_WEST);
        if (state.getValue(EAST))  shape = Shapes.or(shape, ARM_EAST);
        if (state.getValue(UP))    shape = Shapes.or(shape, ARM_UP);
        if (state.getValue(DOWN))  shape = Shapes.or(shape, ARM_DOWN);
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
