package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.EssenceReadoutBlockEntity;
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
 * Represents a block that displays essence-related information in the world. This block is capable of interfacing with
 * essence systems and related mechanics, typically via a connected block entity. Its facing direction can be adjusted
 * based on its placement in the world.
 *
 * Key Features:
 * - Supports directional placement, determined by the player's facing direction when the block is placed.
 * - Associates with an EssenceReadoutBlockEntity, which manages the logic and state related to essence visualization.
 * - Implements BlockState properties, allowing interaction with block-specific states like facing direction.
 *
 * Constructor:
 * - Initializes the block with its properties and sets a default facing direction.
 *
 * Methods:
 * - createBlockStateDefinition: Registers the block's state properties to enable dynamic configuration.
 * - getStateForPlacement: Determines the block's initial state based on the placement context, including its facing direction.
 * - newBlockEntity: Generates a new instance of EssenceReadoutBlockEntity to manage the block's backend logic and state.
 */
public class EssenceReadoutBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public EssenceReadoutBlock(Properties properties) {
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
        return new EssenceReadoutBlockEntity(pos, state);
    }
}