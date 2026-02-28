package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a block entity for the Essence Readout block, responsible for displaying
 * and interacting with the essence values in the game world. This class serves as the
 * backend logic for the Essence Readout block, enabling it to interface with entities,
 * levels, and essence-related mechanics.
 *
 * This block entity manages its state and interacts with the surrounding environment
 * to retrieve or display information about essence availability or core connections.
 *
 * It is typically tied to the { EssenceReadoutRenderer}, which handles the rendering
 * of the readout display based on the block entity's internal state.
 *
 * Constructor:
 * - Initializes the block entity with its position and state in the level.
 */
public class EssenceReadoutBlockEntity extends BlockEntity {
    public EssenceReadoutBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENCE_READOUT_BE.get(), pos, state);
    }
}