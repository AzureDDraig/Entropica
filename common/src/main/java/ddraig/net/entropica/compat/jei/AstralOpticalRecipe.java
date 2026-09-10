package ddraig.net.entropica.compat.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record AstralOpticalRecipe(
        Component title,
        ItemStack input,
        String constellationRequired,
        ItemStack output,
        ItemStack substrateResult
) {}
