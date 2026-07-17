package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ResearchBenchBlockEntity extends BlockEntity {
    public ResearchBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_BENCH_BE.get(), pos, state);
    }
}
