package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.recipe.DilutedEssenceRecipe;
import ddraig.net.entropica.recipe.PressureChamberRecipe;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModFluids;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
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
        registration.addRecipeCategories(new DilutedEssenceRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipes(IRecipeRegistration registration) {
        List<PressureChamberRecipe> pcRecipes = Collections.emptyList();
        List<DilutedEssenceRecipe> deRecipes = Collections.emptyList();

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            pcRecipes = server.getRecipeManager().getRecipes().stream()
                    .filter(holder -> holder.value() instanceof PressureChamberRecipe)
                    .map(holder -> (PressureChamberRecipe) holder.value())
                    .toList();

            deRecipes = server.getRecipeManager().getRecipes().stream()
                    .filter(holder -> holder.value() instanceof DilutedEssenceRecipe)
                    .map(holder -> (DilutedEssenceRecipe) holder.value())
                    .toList();
        }

        registration.addRecipes(PressureChamberRecipeCategory.TYPE, pcRecipes);
        registration.addRecipes(DilutedEssenceRecipeCategory.TYPE, deRecipes);

        // ==========================================
        // JEI INFORMATION TABS
        // ==========================================
        // CHANGED: Attaches the Info Tab directly to the actual Fluid Stack
        registration.addIngredientInfo(
                new FluidStack(ModFluids.DILUTED_ESSENCE_FLUID.get(), 1000),
                NeoForgeTypes.FLUID_STACK,
                Component.translatable("jei.entropica.info.diluted_essence")
        );
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER.get()), PressureChamberRecipeCategory.TYPE);

        // CHANGED: Sets the Fluid Stack itself as the catalyst for the recipe category!
        registration.addRecipeCatalyst(NeoForgeTypes.FLUID_STACK, new FluidStack(ModFluids.DILUTED_ESSENCE_FLUID.get(), 1000), DilutedEssenceRecipeCategory.TYPE);
    }
}