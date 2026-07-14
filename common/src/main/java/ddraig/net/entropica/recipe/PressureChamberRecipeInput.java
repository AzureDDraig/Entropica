package ddraig.net.entropica.recipe;

import ddraig.net.entropica.api.materia.MateriaFumusStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record PressureChamberRecipeInput(ItemStack item, MateriaFumusStack fume) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}