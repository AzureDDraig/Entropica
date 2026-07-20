package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a block entity for the Materia Readout block. This block entity is responsible
 * for interacting with nearby blocks, particularly those that can store materia, to gather
 * and display materia information via a renderer.
 *
 * The ManaReadoutBlockEntity is registered and instanced within the Materia Readout block
 * setup and operates within defined parameters of the Minecraft world to query surrounding
 * blocks for their materia levels. This may include checking for the presence of a connected
 * entropic core and updating its render state accordingly.
 *
 * Usage of this block entity includes rendering materia-related data onto the corresponding
 * block model in world space. The data displayed typically originates from linked or
 * nearby blocks capable of storing or interacting with materia.
 */
public class VisReadoutBlockEntity extends BlockEntity {
    public VisReadoutBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_READOUT_BE.get(), pos, state);
    }
}