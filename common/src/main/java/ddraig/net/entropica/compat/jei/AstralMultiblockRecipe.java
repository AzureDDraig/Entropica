package ddraig.net.entropica.compat.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public record AstralMultiblockRecipe(
        Component title,
        String dimensions,
        ItemStack coreBlock,
        List<ItemStack> requiredMaterials,
        List<List<String>> layerBlueprints,
        Map<Character, ItemStack> symbolLegend,
        Component description
) {
}
