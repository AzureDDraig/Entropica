package ddraig.net.entropica.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A lightweight record to hold the mapped values for the JEI display.
 */
public record EssenceExtractionRecipe(ItemStack input, List<ItemStack> outputs, List<Float> chances) {
}