package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.AestheticGlassRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class AestheticGlassTrapdoorBlock extends TrapDoorBlock {

    public AestheticGlassTrapdoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (AestheticGlassRegistry.isMatchingGlassBlock(state.getBlock(), adjacentState.getBlock())) {
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }
}
