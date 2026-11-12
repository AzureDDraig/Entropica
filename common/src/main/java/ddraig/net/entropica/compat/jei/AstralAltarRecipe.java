package ddraig.net.entropica.compat.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record AstralAltarRecipe(
        Component title,
        ItemStack centerInput,
        List<ItemStack> pedestalInputs,
        ItemStack output,
        String description
) {}
