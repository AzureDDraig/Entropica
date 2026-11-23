package ddraig.net.entropica.block.biome2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Static Redstone Coupler — low-profile copper and Charged Glow-Tentacle relay diode.
 * Collects redstone signals from any non-front face and outputs a full strength (15)
 * directional signal toward {@link #FACING}.
 */
public class StaticRedstoneCouplerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<StaticRedstoneCouplerBlock> CODEC = simpleCodec(StaticRedstoneCouplerBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
    private static final int RELAY_DELAY_TICKS = 2;

    public StaticRedstoneCouplerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        boolean powered = hasNonFrontSignal(context.getLevel(), context.getClickedPos(), facing);
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, powered);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            notifyOutputNeighbors(level, pos, state.getValue(FACING));
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, isMoving);
        if (!level.isClientSide()) {
            boolean shouldPower = hasNonFrontSignal(level, pos, state.getValue(FACING));
            if (shouldPower != state.getValue(POWERED) && !level.getBlockTicks().willTickThisTick(pos, this)) {
                level.scheduleTick(pos, this, RELAY_DELAY_TICKS);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        boolean shouldPower = hasNonFrontSignal(level, pos, facing);
        if (state.getValue(POWERED) != shouldPower) {
            level.setBlock(pos, state.setValue(POWERED, shouldPower), 3);
            notifyOutputNeighbors(level, pos, facing);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!isMoving && state.getValue(POWERED)) {
            notifyOutputNeighbors(level, pos, state.getValue(FACING));
        }
    }

    private static boolean hasNonFrontSignal(Level level, BlockPos pos, Direction front) {
        for (Direction dir : Direction.values()) {
            if (dir == front) {
                continue;
            }
            BlockPos adjPos = pos.relative(dir);
            if (level.getSignal(adjPos, dir) > 0) {
                return true;
            }
            BlockState adjState = level.getBlockState(adjPos);
            if (adjState.is(Blocks.REDSTONE_WIRE) && adjState.getValue(RedStoneWireBlock.POWER) > 0) {
                return true;
            }
        }
        return false;
    }

    private void notifyOutputNeighbors(Level level, BlockPos pos, Direction front) {
        BlockPos frontPos = pos.relative(front);
        level.neighborChanged(frontPos, this, null);
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(frontPos, this);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!state.getValue(POWERED)) {
            return 0;
        }
        return state.getValue(FACING).getOpposite() == direction ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(POWERED) && random.nextInt(4) == 0) {
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + 0.42D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(new DustParticleOptions(0x22D3EE, 0.7F), x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
