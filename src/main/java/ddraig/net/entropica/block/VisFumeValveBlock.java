package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.VisFumeValveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VisFumeValveBlock extends VisFumePipeBlock implements EntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED; // Added Redstone Tracking
    // Defines whether the straight pipe runs along X, Y, or Z
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public VisFumeValveBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false)
                .setValue(WATERLOGGED, false)
                .setValue(OPEN, false) // Default to closed for better Redstone predictability!
                .setValue(POWERED, false)
                .setValue(AXIS, Direction.Axis.Y)); // Default to upright
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // Gets the 6 directions from the Pipe
        builder.add(OPEN, POWERED, AXIS);
    }

    // --- CONNECTION OVERRIDE FOR ALIGNMENT & DIVERTERS ---
    @Override
    protected boolean canConnectTo(BlockState neighborState, Direction direction) {
        Block neighborBlock = neighborState.getBlock();

        // FIX: If the neighbor is another Valve, it MUST share the same connection axis!
        if (neighborBlock instanceof VisFumeValveBlock) {
            return neighborState.getValue(AXIS) == direction.getAxis();
        }

        // Otherwise, allow connections to pipes and diverters
        return neighborBlock instanceof VisFumePipeBlock ||
                neighborBlock instanceof VisFumeDiverterBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // The face of the block the player clicked ON
        Direction clickedFace = context.getClickedFace();
        // The direction FROM the new Valve TO the block the player clicked against
        Direction toClickedBlock = clickedFace.getOpposite();

        // Scan all 3 axes for nearby valid connections
        boolean xConnect = canConnectTo(level.getBlockState(pos.east()), Direction.EAST) || canConnectTo(level.getBlockState(pos.west()), Direction.WEST);
        boolean yConnect = canConnectTo(level.getBlockState(pos.above()), Direction.UP)   || canConnectTo(level.getBlockState(pos.below()), Direction.DOWN);
        boolean zConnect = canConnectTo(level.getBlockState(pos.north()), Direction.NORTH)|| canConnectTo(level.getBlockState(pos.south()), Direction.SOUTH);

        Direction.Axis axis;

        // PRIORITY 1: Did the player explicitly click against a valid connection?
        if (canConnectTo(level.getBlockState(pos.relative(toClickedBlock)), toClickedBlock)) {
            axis = clickedFace.getAxis(); // Snap to the face they clicked
        }
        // PRIORITY 2 & 3: They clicked on the floor, wall, or empty space
        else {
            int connectionCount = (xConnect ? 1 : 0) + (yConnect ? 1 : 0) + (zConnect ? 1 : 0);

            if (connectionCount == 1) {
                // PRIORITY 2: Only one valid axis nearby, so smartly snap to it!
                if (xConnect) axis = Direction.Axis.X;
                else if (yConnect) axis = Direction.Axis.Y;
                else axis = Direction.Axis.Z;
            } else {
                // PRIORITY 3: 0 connections OR 2+ connections (junction). Fall back to where the player is looking.
                axis = context.getNearestLookingDirection().getAxis();
            }
        }

        // Check if Redstone is powering the spot we are placing the block
        boolean hasPower = level.hasNeighborSignal(pos);

        // Apply the calculated axis and physically connect the arms
        return this.defaultBlockState()
                .setValue(AXIS, axis)
                .setValue(OPEN, hasPower) // Snap open if placed next to Redstone!
                .setValue(POWERED, hasPower)
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == net.minecraft.world.level.material.Fluids.WATER)
                .setValue(UP, axis == Direction.Axis.Y && canConnectTo(level.getBlockState(pos.above()), Direction.UP))
                .setValue(DOWN, axis == Direction.Axis.Y && canConnectTo(level.getBlockState(pos.below()), Direction.DOWN))
                .setValue(NORTH, axis == Direction.Axis.Z && canConnectTo(level.getBlockState(pos.north()), Direction.NORTH))
                .setValue(SOUTH, axis == Direction.Axis.Z && canConnectTo(level.getBlockState(pos.south()), Direction.SOUTH))
                .setValue(EAST, axis == Direction.Axis.X && canConnectTo(level.getBlockState(pos.east()), Direction.EAST))
                .setValue(WEST, axis == Direction.Axis.X && canConnectTo(level.getBlockState(pos.west()), Direction.WEST));
    }

    // --- REDSTONE LOGIC ---
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean hasPower = level.hasNeighborSignal(pos);

            // Only fire if the redstone state actually changed from on to off, or off to on
            if (hasPower != state.getValue(POWERED)) {
                // Check if the physical valve matches the redstone state (so we don't spam sounds)
                if (state.getValue(OPEN) != hasPower) {
                    level.setBlock(pos, state.setValue(OPEN, hasPower).setValue(POWERED, hasPower), 3);
                    float pitch = hasPower ? 1.2f : 0.8f;
                    level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, pitch);
                } else {
                    // Update the powered tracking even if the valve was manually forced open/closed
                    level.setBlock(pos, state.setValue(POWERED, hasPower), 3);
                }
            }
        }
    }

    // --- DYNAMIC AUTO-SNAPPING LOGIC ---
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        Direction.Axis currentAxis = state.getValue(AXIS);

        // 1. Is the valve currently completely unconnected on its active axis?
        boolean isUnconnected = switch (currentAxis) {
            case X -> !state.getValue(EAST) && !state.getValue(WEST);
            case Y -> !state.getValue(UP) && !state.getValue(DOWN);
            case Z -> !state.getValue(NORTH) && !state.getValue(SOUTH);
        };

        // 2. If it is unconnected, and a new valid pipe appears on a different axis, seamlessly pivot to it!
        if (isUnconnected && currentAxis != direction.getAxis() && canConnectTo(neighborState, direction)) {
            state = state.setValue(AXIS, direction.getAxis());
            currentAxis = direction.getAxis(); // Update the local variable for the check below!
        }

        // 3. If a block updates on a side that DOES NOT match our active axis, aggressively ignore it
        if (currentAxis != direction.getAxis()) {
            return state.setValue(getDirectionProperty(direction), false);
        }

        // 4. Otherwise, run the normal pipe logic to connect
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    // --- MANUAL CLICK LOGIC ---
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            boolean wasOpen = state.getValue(OPEN);
            level.setBlock(pos, state.setValue(OPEN, !wasOpen), 3);

            float pitch = !wasOpen ? 1.2f : 0.8f;
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, pitch);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VisFumeValveBlockEntity(pos, state);
    }
}