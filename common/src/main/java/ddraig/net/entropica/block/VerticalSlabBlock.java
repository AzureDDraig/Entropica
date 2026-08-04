package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VerticalSlabBlock extends Block {

    public enum VerticalSlabType implements StringRepresentable {
        NORTH("north"),
        SOUTH("south"),
        WEST("west"),
        EAST("east"),
        DOUBLE_NS("double_ns"),
        DOUBLE_EW("double_ew");

        private final String name;
        VerticalSlabType(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public static final EnumProperty<VerticalSlabType> TYPE = EnumProperty.create("type", VerticalSlabType.class);

    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public VerticalSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, VerticalSlabType.NORTH));
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (adjacentState.is(state.getBlock())) {
            if (adjacentState.getBlock() instanceof VerticalSlabBlock) {
                VerticalSlabType type = state.getValue(TYPE);
                VerticalSlabType adjType = adjacentState.getValue(TYPE);
                // Double type is full block - always cull
                if (adjType == VerticalSlabType.DOUBLE_NS || adjType == VerticalSlabType.DOUBLE_EW
                        || type == VerticalSlabType.DOUBLE_NS || type == VerticalSlabType.DOUBLE_EW) {
                    return true;
                }
                // Same orientation - cull shared faces
                if (type == adjType) {
                    return true;
                }
                // Different orientation (90 degrees) - don't cull (partial overlap)
                return false;
            }
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(TYPE)) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
            case DOUBLE_NS, DOUBLE_EW -> Shapes.block();
        };
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        ItemStack itemstack = context.getItemInHand();
        VerticalSlabType type = state.getValue(TYPE);
        if (type != VerticalSlabType.DOUBLE_NS && type != VerticalSlabType.DOUBLE_EW && itemstack.is(this.asItem())) {
            if (context.replacingClickedOnBlock()) {
                Direction direction = context.getClickedFace();
                if (type == VerticalSlabType.NORTH && direction == Direction.SOUTH) return true;
                if (type == VerticalSlabType.SOUTH && direction == Direction.NORTH) return true;
                if (type == VerticalSlabType.EAST && direction == Direction.WEST) return true;
                if (type == VerticalSlabType.WEST && direction == Direction.EAST) return true;
            } else {
                return true;
            }
        }
        return super.canBeReplaced(state, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = context.getLevel().getBlockState(blockpos);
        if (blockstate.is(this)) {
            VerticalSlabType type = blockstate.getValue(TYPE);
            if (type == VerticalSlabType.NORTH || type == VerticalSlabType.SOUTH) {
                return blockstate.setValue(TYPE, VerticalSlabType.DOUBLE_NS);
            } else {
                return blockstate.setValue(TYPE, VerticalSlabType.DOUBLE_EW);
            }
        }

        Direction face = context.getClickedFace();
        if (face.getAxis().isHorizontal()) {
            return this.defaultBlockState().setValue(TYPE, switch (face) {
                case NORTH -> VerticalSlabType.SOUTH;
                case SOUTH -> VerticalSlabType.NORTH;
                case WEST -> VerticalSlabType.EAST;
                case EAST -> VerticalSlabType.WEST;
                default -> VerticalSlabType.NORTH;
            });
        } else {
            double clickX = context.getClickLocation().x - (double)blockpos.getX();
            double clickZ = context.getClickLocation().z - (double)blockpos.getZ();
            if (Math.abs(clickX - 0.5D) > Math.abs(clickZ - 0.5D)) {
                return this.defaultBlockState().setValue(TYPE, clickX < 0.5D ? VerticalSlabType.WEST : VerticalSlabType.EAST);
            } else {
                return this.defaultBlockState().setValue(TYPE, clickZ < 0.5D ? VerticalSlabType.NORTH : VerticalSlabType.SOUTH);
            }
        }
    }
}
