package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.recipe.PressureChamberRecipe;
import ddraig.net.entropica.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collections;
import java.util.List;

@JeiPlugin
public class EntropicaJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PressureChamberRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PressureChamberRecipe> recipes = Collections.emptyList();

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            recipes = server.getRecipeManager().getRecipes().stream()
                    .filter(holder -> holder.value() instanceof PressureChamberRecipe)
                    .map(holder -> (PressureChamberRecipe) holder.value())
                    .toList();
        }

        registration.addRecipes(PressureChamberRecipeCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER.get()), PressureChamberRecipeCategory.TYPE);
    }
}