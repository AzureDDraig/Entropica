package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.VaporPneumaticDiverterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;

public class VaporPneumaticDiverterBlock extends Block implements EntityBlock {

    // FACING represents where the INPUT is coming from (Internal name: "facing")
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    // CUSTOM PROPERTY: Remembers the player's perspective (Internal name: "horizontal_facing")
    // This prevents the "duplicate property: facing" registry crash!
    public static final EnumProperty<Direction> HORIZONTAL_FACING = EnumProperty.create(
            "horizontal_facing", Direction.class, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    );

    // The three possible outputs relative to the input
    public static final BooleanProperty LEFT_OPEN = BooleanProperty.create("left_open");
    public static final BooleanProperty RIGHT_OPEN = BooleanProperty.create("right_open");
    public static final BooleanProperty FORWARD_OPEN = BooleanProperty.create("forward_open");

    // --- CUSTOM HITBOX (VOXELSHAPE) ---
    private static final VoxelShape CORE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D);

    // Define all 6 possible arm shapes
    private static final VoxelShape ARM_UP = Block.box(5.0D, 12.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape ARM_DOWN = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 4.0D, 11.0D);
    private static final VoxelShape ARM_NORTH = Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 4.0D);
    private static final VoxelShape ARM_SOUTH = Block.box(5.0D, 5.0D, 12.0D, 11.0D, 11.0D, 16.0D);
    private static final VoxelShape ARM_WEST = Block.box(0.0D, 5.0D, 5.0D, 4.0D, 11.0D, 11.0D);
    private static final VoxelShape ARM_EAST = Block.box(12.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);

    private VoxelShape getArmShape(Direction dir) {
        return switch (dir) {
            case UP -> ARM_UP;
            case DOWN -> ARM_DOWN;
            case NORTH -> ARM_NORTH;
            case SOUTH -> ARM_SOUTH;
            case WEST -> ARM_WEST;
            case EAST -> ARM_EAST;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction inputFace = state.getValue(FACING);
        Direction playerPerspective = state.getValue(HORIZONTAL_FACING);

        Direction forwardFace = inputFace.getOpposite();
        Direction rightFace;
        Direction leftFace;

        // Use the exact same math we used for the clicking logic!
        if (inputFace.getAxis().isVertical()) {
            leftFace = playerPerspective.getCounterClockWise();
            rightFace = playerPerspective.getClockWise();
        } else {
            leftFace = inputFace.getClockWise();
            rightFace = inputFace.getCounterClockWise();
        }

        // Glue the core together with the 4 active arms based on placement state
        return Shapes.or(CORE,
                getArmShape(inputFace),
                getArmShape(forwardFace),
                getArmShape(leftFace),
                getArmShape(rightFace));
    }

    public VaporPneumaticDiverterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(LEFT_OPEN, false)
                .setValue(RIGHT_OPEN, false)
                .setValue(FORWARD_OPEN, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HORIZONTAL_FACING, LEFT_OPEN, RIGHT_OPEN, FORWARD_OPEN);
    }

    // --- THE SMART SNAP LOGIC ---
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        Direction clickedFace = context.getClickedFace();
        Direction toClickedBlock = clickedFace.getOpposite();

        Direction inputDirection;

        // PRIORITY 1: Did the player explicitly click against a valid gas connection?
        if (canConnectTo(level.getBlockState(pos.relative(toClickedBlock)))) {
            inputDirection = toClickedBlock;
        } else {
            // PRIORITY 2: Scan all 6 directions for nearby valid connections
            int connectionCount = 0;
            Direction lastFoundDir = null;

            for (Direction dir : Direction.values()) {
                if (canConnectTo(level.getBlockState(pos.relative(dir)))) {
                    connectionCount++;
                    lastFoundDir = dir;
                }
            }

            if (connectionCount == 1) {
                // Only one pipe nearby, smartly snap the input to it!
                inputDirection = lastFoundDir;
            } else {
                // PRIORITY 3: 0 connections OR 2+ connections (junction). Fall back to looking direction.
                inputDirection = context.getNearestLookingDirection().getOpposite();
            }
        }

        return this.defaultBlockState()
                .setValue(FACING, inputDirection)
                // Snap a picture of the player's horizontal perspective!
                .setValue(HORIZONTAL_FACING, context.getHorizontalDirection())
                .setValue(LEFT_OPEN, false)
                .setValue(RIGHT_OPEN, false)
                .setValue(FORWARD_OPEN, true);
    }

    // Helper to identify what the Diverter can connect to
    private boolean canConnectTo(BlockState state) {
        return state.getBlock() instanceof VaporPneumaticPipeBlock ||
                state.getBlock() instanceof VaporPneumaticValveBlock ||
                state.getBlock() instanceof VaporPneumaticDiverterBlock;
    }

    // --- THE SMART CLICKING LOGIC ---
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Direction inputFace = state.getValue(FACING);
        Direction playerPerspective = state.getValue(HORIZONTAL_FACING);

        Direction forwardFace = inputFace.getOpposite();
        Direction rightFace;
        Direction leftFace;

        if (inputFace.getAxis().isVertical()) {
            leftFace = playerPerspective.getCounterClockWise();
            rightFace = playerPerspective.getClockWise();
        } else {
            leftFace = inputFace.getClockWise();
            rightFace = inputFace.getCounterClockWise();
        }

        // 1. Calculate the exact X/Z offset of the crosshair from the absolute center of the block
        double dX = hitResult.getLocation().x - pos.getX() - 0.5;
        double dZ = hitResult.getLocation().z - pos.getZ() - 0.5;

        // 2. Determine which horizontal quadrant (slice of the pie) the click landed in
        Direction clickedQuadrant = null;
        double maxDot = -1.0;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            double dot = (dX * dir.getStepX()) + (dZ * dir.getStepZ());
            if (dot > maxDot) {
                maxDot = dot;
                clickedQuadrant = dir;
            }
        }

        // Count how many valves are currently open
        boolean isLeftOpen = state.getValue(LEFT_OPEN);
        boolean isRightOpen = state.getValue(RIGHT_OPEN);
        boolean isForwardOpen = state.getValue(FORWARD_OPEN);

        int openCount = (isLeftOpen ? 1 : 0) + (isRightOpen ? 1 : 0) + (isForwardOpen ? 1 : 0);
        boolean changed = false;

        // 3. Toggle the corresponding valve based on the clicked quadrant
        if (clickedQuadrant == forwardFace) {
            if (!isForwardOpen && openCount >= 2) return InteractionResult.PASS; // Reject!
            state = state.setValue(FORWARD_OPEN, !isForwardOpen);
            changed = true;
        } else if (clickedQuadrant == leftFace) {
            if (!isLeftOpen && openCount >= 2) return InteractionResult.PASS; // Reject!
            state = state.setValue(LEFT_OPEN, !isLeftOpen);
            changed = true;
        } else if (clickedQuadrant == rightFace) {
            if (!isRightOpen && openCount >= 2) return InteractionResult.PASS; // Reject!
            state = state.setValue(RIGHT_OPEN, !isRightOpen);
            changed = true;
        }

        if (changed) {
            level.setBlock(pos, state, 3);
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.2F);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS; // They clicked the Input arm half, do nothing!
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Linked the BlockEntity so it won't crash on placement!
        return new VaporPneumaticDiverterBlockEntity(pos, state);
    }
}