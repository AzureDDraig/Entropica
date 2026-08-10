package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AuroralLilyPadBlock extends WaterlilyBlock {
    public static final MapCodec<AuroralLilyPadBlock> CODEC = simpleCodec(AuroralLilyPadBlock::new);
    protected static final VoxelShape AABB = box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);

    public AuroralLilyPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<WaterlilyBlock> codec() {
        return (MapCodec<WaterlilyBlock>) (MapCodec<?>) CODEC;
    }



    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABB;
    }
}
