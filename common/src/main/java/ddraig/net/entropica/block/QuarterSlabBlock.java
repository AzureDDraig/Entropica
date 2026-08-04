package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class QuarterSlabBlock extends Block {

    // Bitmask LAYERS property from 1 to 15 (4 bits representing L0, L1, L2, L3)
    public static final IntegerProperty LAYERS_MASK = IntegerProperty.create("layers", 1, 15);

    protected static final VoxelShape L0_BOX = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    protected static final VoxelShape L1_BOX = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    protected static final VoxelShape L2_BOX = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    protected static final VoxelShape L3_BOX = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public QuarterSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_MASK, 1));
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (ddraig.net.entropica.registry.AestheticGlassRegistry.isMatchingGlassBlock(state.getBlock(), adjacentState.getBlock())) {
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_MASK);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = state.getValue(LAYERS_MASK);
        VoxelShape shape = Shapes.empty();
        if ((mask & 1) != 0) shape = Shapes.or(shape, L0_BOX);
        if ((mask & 2) != 0) shape = Shapes.or(shape, L1_BOX);
        if ((mask & 4) != 0) shape = Shapes.or(shape, L2_BOX);
        if ((mask & 8) != 0) shape = Shapes.or(shape, L3_BOX);
        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState existingState = context.getLevel().getBlockState(pos);

        double clickY = context.getClickLocation().y - (double)pos.getY();
        int layerBit;
        if (clickY < 0.25D) {
            layerBit = 1; // L0 (0-4px)
        } else if (clickY < 0.50D) {
            layerBit = 2; // L1 (4-8px)
        } else if (clickY < 0.75D) {
            layerBit = 4; // L2 (8-12px)
        } else {
            layerBit = 8; // L3 (12-16px)
        }

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
