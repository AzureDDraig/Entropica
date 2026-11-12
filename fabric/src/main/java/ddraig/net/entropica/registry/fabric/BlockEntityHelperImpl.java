package ddraig.net.entropica.registry.fabric;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityHelperImpl {
    public static <T extends BlockEntity> BlockEntityType<T> create(BlockEntityType.BlockEntitySupplier<T> supplier, Block... blocks) {
        return FabricBlockEntityTypeBuilder.create(supplier::create, blocks).build();
    }
}
