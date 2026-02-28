package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ManaReadoutBlockEntity extends BlockEntity {
    public ManaReadoutBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_READOUT_BE.get(), pos, state);
    }
}