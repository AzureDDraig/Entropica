package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.ManaReadoutBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a mana readout block that is capable of extracting and displaying mana-related
 * information from adjacent blocks within a specified range.
 *
 * The ManaReadoutBlock is registered as a block in the world and functions in conjunction
 * with its block entity, the {@link ManaReadoutBlockEntity}, to interact with blocks capable
 * of storing or manipulating mana. It facilitates directional placement and updates its
 * state accordingly when positioned in the game world.
 *
 * Features of the ManaReadoutBlock include:
 * - Orientation based on the player's placement angle.
 * - Integration with a rendering system to display mana levels or related information
 *   from a nearby entropic core or compatible blocks.
 * - Custom block state definition to handle its facing direction for dynamic orientation.
 *
 * This block relies on its associated BlockEntity for extended interaction beyond
 * its simple in-world placement, indicating a tightly coupled functionality for its intended purpose.
 */
public class ManaReadoutBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ManaReadoutBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaReadoutBlockEntity(pos, state);
    }
}