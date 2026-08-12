package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.ModChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class ModChestBlock extends ChestBlock {
    private final String woodType;

    public ModChestBlock(Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityTypeSupplier, Properties properties, String woodType) {
        super(blockEntityTypeSupplier, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
        this.woodType = woodType;
    }

    public String getWoodType() {
        return woodType;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModChestBlockEntity(pos, state);
    }
}
