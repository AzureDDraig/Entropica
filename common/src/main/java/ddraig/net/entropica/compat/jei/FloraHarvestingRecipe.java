package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record FloraHarvestingRecipe(ItemStack plantBlock, ItemStack toolItem, List<ItemStack> petalOutputs) {
    public static List<FloraHarvestingRecipe> createAllRecipes() {
        return List.of(
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.AEGIS_ROSE.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.AEGIS_ROSE_PETALS.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.TALL_AEGIS_ROSE.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.AEGIS_ROSE_PETALS.get(), 4))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.AEGIS_SPIRE_ORCHID.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.AEGIS_SPIRE_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.TALL_AEGIS_SPIRE_ORCHID.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.AEGIS_SPIRE_PETAL.get(), 4))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.SOUL_FLAME_ORCHID.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.SOUL_FLAME_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.TALL_SOUL_FLAME_ORCHID.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.SOUL_FLAME_PETAL.get(), 4))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.VITAE_ORCHID.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.VITAE_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.TALL_VITAE_ORCHID.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.VITAE_PETAL.get(), 4))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.VOID_STALKER_ORCHID.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.VOID_STALKER_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.TALL_VOID_STALKER_ORCHID.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.VOID_STALKER_PETAL.get(), 4))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.NECROTIC_ROSE_OF_JERICHO.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.NECROTIC_ROSE_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.TALL_NECROTIC_ROSE_OF_JERICHO.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.NECROTIC_ROSE_PETAL.get(), 4))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.SANGUINE_LILY.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.SANGUINE_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.AURORAL_BUTTERCUP.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.AURORAL_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.STARDUST_BELL.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.STARDUST_BELL_PETAL.get(), 3))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.FULGURITE_SWAMP_BLOOM.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.FULGURITE_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.GALE_BLOOM_DANDELION.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.GALE_BLOOM_PETAL.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.CINDER_SPORE_MUSHROOM.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.CINDER_SPORE_CAP.get(), 2))),
            new FloraHarvestingRecipe(new ItemStack(ModBlocks.SPECTRAL_LANTERN_FLOWER.get()), new ItemStack(Items.SHEARS), List.of(new ItemStack(ModItems.SPECTRAL_LANTERN_POD.get(), 2)))
        );
    }
}
