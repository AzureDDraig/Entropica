package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class AestheticGlassPaneBlock extends IronBarsBlock {

    public AestheticGlassPaneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockState neighbor = level.getBlockState(pos.relative(dir));
                if (neighbor.getBlock() instanceof HorizontalPaneBlock) {
                    BooleanProperty prop = getPropertyForDirection(dir);
                    if (prop != null) {
                        state = state.setValue(prop, true);
                    }
                }
            }
        }
        return state;
    }

    private BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> null;
        };
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (adjacentState.is(state.getBlock())) {
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }
}
