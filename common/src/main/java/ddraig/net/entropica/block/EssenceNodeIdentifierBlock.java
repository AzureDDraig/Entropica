package ddraig.net.entropica.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

// Placed directly under the node to stabilize it and read its contents
public class EssenceNodeIdentifierBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public EssenceNodeIdentifierBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }
}