package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RefractiveAstralLensBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<RefractiveAstralLensBlock> CODEC = simpleCodec(RefractiveAstralLensBlock::new);

    private static final VoxelShape SHAPE_NS = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0),
            Block.box(6.0, 2.0, 6.0, 10.0, 4.0, 10.0),
            Block.box(2.0, 4.0, 7.0, 14.0, 13.0, 9.0)
    );

    private static final VoxelShape SHAPE_EW = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0),
            Block.box(6.0, 2.0, 6.0, 10.0, 4.0, 10.0),
            Block.box(7.0, 4.0, 2.0, 9.0, 13.0, 14.0)
    );

    public RefractiveAstralLensBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return (dir.getAxis() == Direction.Axis.X) ? SHAPE_EW : SHAPE_NS;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }
}
