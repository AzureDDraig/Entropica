package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.AestheticGlassRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

public class AestheticGlassBlock extends TransparentBlock {

    public AestheticGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (AestheticGlassRegistry.isMatchingGlassBlock(state.getBlock(), adjacentState.getBlock())) {
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }
}
