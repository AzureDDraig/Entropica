package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState result = super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
        // Also connect to HorizontalPaneBlock neighbors
        if (direction.getAxis().isHorizontal() && neighborState.getBlock() instanceof HorizontalPaneBlock) {
            BooleanProperty prop = getPropertyForDirection(direction);
            if (prop != null) {
                result = result.setValue(prop, true);
            }
        }
        return result;
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
