package ddraig.net.entropica.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class AestheticGlassStairBlock extends StairBlock {

    public AestheticGlassStairBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        // Stairs have complex L-shaped geometry - never skip rendering
        // between adjacent stairs as faces are rarely fully covered
        return super.skipRendering(state, adjacentState, direction);
    }
}
