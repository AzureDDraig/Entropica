package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.AestheticGlassRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public class AestheticGlassSlabBlock extends SlabBlock {

    public AestheticGlassSlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (AestheticGlassRegistry.isMatchingGlassBlock(state.getBlock(), adjacentState.getBlock())) {
            if (adjacentState.getBlock() instanceof SlabBlock) {
                SlabType type = state.getValue(TYPE);
                SlabType adjType = adjacentState.getValue(TYPE);
                if (adjType == SlabType.DOUBLE || type == SlabType.DOUBLE) {
                    return true;
                }
                if (type == adjType) {
                    return direction.getAxis().isHorizontal();
                }
                if (direction == Direction.UP && type == SlabType.BOTTOM && adjType == SlabType.TOP) {
                    return true;
                }
                if (direction == Direction.DOWN && type == SlabType.TOP && adjType == SlabType.BOTTOM) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return super.skipRendering(state, adjacentState, direction);
    }
}
