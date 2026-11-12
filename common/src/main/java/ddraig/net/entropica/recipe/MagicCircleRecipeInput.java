package ddraig.net.entropica.recipe;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;
import java.util.Map;

public record MagicCircleRecipeInput(
        List<ItemStack> inputs,
        List<ItemStack> runes,
        Map<EssenceType, Integer> essences,
        int circleTier
) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index < inputs.size()) {
            return inputs.get(index);
        }
        int runeIndex = index - inputs.size();
        if (runeIndex < runes.size()) {
            return runes.get(runeIndex);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return inputs.size() + runes.size();
    }
}
