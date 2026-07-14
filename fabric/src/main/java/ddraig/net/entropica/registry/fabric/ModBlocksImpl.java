package ddraig.net.entropica.registry.fabric;

import ddraig.net.entropica.registry.ModBlocks;

import ddraig.net.entropica.block.DilutedEssenceFluidBlock;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocksImpl {
    public static RegistrySupplier<Block> registerDilutedEssenceBlock() {
        return ModBlocks.BLOCKS.register("diluted_essence_fluid_block",
                name -> new DilutedEssenceFluidBlock(ModFluidsFabric.STILL, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(ResourceKey.create(Registries.BLOCK, name)).noLootTable().liquid()));
    }
}
