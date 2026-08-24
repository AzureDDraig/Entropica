package ddraig.net.entropica.registry.neoforge;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityHelperImpl {
    public static <T extends BlockEntity> BlockEntityType<T> create(BlockEntityType.BlockEntitySupplier<T> supplier, Block... blocks) {
        return new BlockEntityType<>(supplier, blocks);
    }
}
