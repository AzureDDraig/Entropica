package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EssenceReadoutBlockEntity extends BlockEntity {
    public EssenceReadoutBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENCE_READOUT_BE.get(), pos, state);
    }
}