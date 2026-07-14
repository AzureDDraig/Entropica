package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a block entity for the Mana Readout block. This block entity is responsible
 * for interacting with nearby blocks, particularly those that can store mana, to gather
 * and display mana information via a renderer.
 *
 * The ManaReadoutBlockEntity is registered and instanced within the Mana Readout block
 * setup and operates within defined parameters of the Minecraft world to query surrounding
 * blocks for their mana levels. This may include checking for the presence of a connected
 * entropic core and updating its render state accordingly.
 *
 * Usage of this block entity includes rendering mana-related data onto the corresponding
 * block model in world space. The data displayed typically originates from linked or
 * nearby blocks capable of storing or interacting with mana.
 */
public class MateriaReadoutBlockEntity extends BlockEntity {
    public MateriaReadoutBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_READOUT_BE.get(), pos, state);
    }
}