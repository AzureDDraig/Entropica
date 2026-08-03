package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VerticalQuarterSlabBlock extends Block {

    // Bitmask LAYERS_MASK representing 4 vertical slices (0-4px, 4-8px, 8-12px, 12-16px along Z or X)
    public static final IntegerProperty LAYERS_MASK = IntegerProperty.create("layers", 1, 15);

    protected static final VoxelShape Z0_BOX = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    protected static final VoxelShape Z1_BOX = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape Z2_BOX = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape Z3_BOX = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);

    public VerticalQuarterSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_MASK, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_MASK);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = state.getValue(LAYERS_MASK);
        VoxelShape shape = Shapes.empty();
        if ((mask & 1) != 0) shape = Shapes.or(shape, Z0_BOX);
        if ((mask & 2) != 0) shape = Shapes.or(shape, Z1_BOX);
        if ((mask & 4) != 0) shape = Shapes.or(shape, Z2_BOX);
        if ((mask & 8) != 0) shape = Shapes.or(shape, Z3_BOX);
        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState existingState = context.getLevel().getBlockState(pos);

        double clickZ = context.getClickLocation().z - (double)pos.getZ();
        int layerBit;
        if (clickZ < 0.25D) layerBit = 1;
        else if (clickZ < 0.50D) layerBit = 2;
        else if (clickZ < 0.75D) layerBit = 4;
        else layerBit = 8;

        if (existingState.is(this)) {
            int currentMask = existingState.getValue(LAYERS_MASK);
            int newMask = currentMask | layerBit;
            return existingState.setValue(LAYERS_MASK, newMask);
        }

        return this.defaultBlockState().setValue(LAYERS_MASK, layerBit);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        ItemStack held = context.getItemInHand();
        int mask = state.getValue(LAYERS_MASK);
        if (mask != 15 && held.is(this.asItem())) {
            return true;
        }
        return false;
    }
}
