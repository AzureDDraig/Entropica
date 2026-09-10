package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GlowpegBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<GlowpegBlock> CODEC = simpleCodec(GlowpegBlock::new);

    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Floor & Ceiling: Centered vertical peg (fence-post scale)
    private static final VoxelShape SHAPE_VERTICAL = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);

    // Wall mounts (stays vertical, with standoff brackets against wall)
    // facing SOUTH -> attached to North wall (wall at Z=0)
    private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 9.0);
    // facing NORTH -> attached to South wall (wall at Z=16)
    private static final VoxelShape SHAPE_WALL_NORTH = Block.box(5.0, 0.0, 7.0, 11.0, 16.0, 16.0);
    // facing EAST -> attached to West wall (wall at X=0)
    private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 0.0, 5.0, 9.0, 16.0, 11.0);
    // facing WEST -> attached to East wall (wall at X=16)
    private static final VoxelShape SHAPE_WALL_WEST  = Block.box(7.0, 0.0, 5.0, 16.0, 16.0, 11.0);

    private final boolean isSoul;

    public GlowpegBlock(BlockBehaviour.Properties properties, boolean isSoul) {
        super(properties);
        this.isSoul = isSoul;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    public GlowpegBlock(BlockBehaviour.Properties properties) {
        this(properties, false);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = level.getFluidState(pos);
        Direction clickedFace = context.getClickedFace();

        AttachFace face;
        Direction facing;

        if (clickedFace == Direction.UP) {
            face = AttachFace.FLOOR;
            facing = context.getHorizontalDirection();
        } else if (clickedFace == Direction.DOWN) {
            face = AttachFace.CEILING;
            facing = context.getHorizontalDirection();
        } else {
            face = AttachFace.WALL;
            facing = clickedFace;
        }

        return this.defaultBlockState()
                .setValue(FACE, face)
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        AttachFace face = state.getValue(FACE);
        if (face == AttachFace.WALL) {
            return switch (state.getValue(FACING)) {
                case SOUTH -> SHAPE_WALL_SOUTH;
                case NORTH -> SHAPE_WALL_NORTH;
                case EAST  -> SHAPE_WALL_EAST;
                case WEST  -> SHAPE_WALL_WEST;
                default    -> SHAPE_VERTICAL;
            };
        }
        return SHAPE_VERTICAL;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Very rare gentle ambient glowstone sparkle
        if (random.nextInt(18) == 0) {
            AttachFace face = state.getValue(FACE);
            double cx = pos.getX() + 0.5;
            double cz = pos.getZ() + 0.5;
            if (face == AttachFace.WALL) {
                switch (state.getValue(FACING)) {
                    case SOUTH -> cz = pos.getZ() + 0.38;
                    case NORTH -> cz = pos.getZ() + 0.62;
                    case EAST  -> cx = pos.getX() + 0.38;
                    case WEST  -> cx = pos.getX() + 0.62;
                }
            }
            double px = cx + (random.nextDouble() - 0.5) * 0.15;
            double py = pos.getY() + 0.2 + random.nextDouble() * 0.6;
            double pz = cz + (random.nextDouble() - 0.5) * 0.15;
            if (isSoul) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0.0, 0.005, 0.0);
            } else {
                level.addParticle(ParticleTypes.GLOW, px, py, pz, 0.0, 0.005, 0.0);
            }
        }
    }
}
