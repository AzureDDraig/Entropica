package ddraig.net.entropica.recipe;

import ddraig.net.entropica.api.fumes.VisFumeStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record PressureChamberRecipeInput(ItemStack item, VisFumeStack fume) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}