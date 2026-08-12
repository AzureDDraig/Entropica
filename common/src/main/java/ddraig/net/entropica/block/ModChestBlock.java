package ddraig.net.entropica.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

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
}
