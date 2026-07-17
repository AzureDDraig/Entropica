package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DraftingTableBlockEntity extends BlockEntity {
    public DraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRAFTING_TABLE_BE.get(), pos, state);
    }
}
