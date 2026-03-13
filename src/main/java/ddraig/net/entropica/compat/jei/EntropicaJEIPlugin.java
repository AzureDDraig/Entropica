package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.recipe.AethericSynthesizerRecipe;
import ddraig.net.entropica.recipe.DilutedEssenceRecipe;
import ddraig.net.entropica.recipe.FusionRecipe;
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

import java.util.ArrayList;
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
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new PressureChamberRecipeCategory(guiHelper));
        registration.addRecipeCategories(new DilutedEssenceRecipeCategory(guiHelper));
        registration.addRecipeCategories(new VisFusionRecipeCategory(guiHelper));
        registration.addRecipeCategories(new AethericSynthesizerRecipeCategory(guiHelper));
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipes(IRecipeRegistration registration) {
        List<PressureChamberRecipe> pcRecipes = Collections.emptyList();
        List<DilutedEssenceRecipe> deRecipes = Collections.emptyList();
        List<FusionRecipe> fusionRecipes = Collections.emptyList();
        List<AethericSynthesizerRecipe> synthRecipes = new ArrayList<>();

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var recipeManager = server.getRecipeManager();

            pcRecipes = recipeManager.getRecipes().stream()
                    .filter(holder -> holder.value() instanceof PressureChamberRecipe)
                    .map(holder -> (PressureChamberRecipe) holder.value())
                    .toList();

            deRecipes = recipeManager.getRecipes().stream()
                    .filter(holder -> holder.value() instanceof DilutedEssenceRecipe)
                    .map(holder -> (DilutedEssenceRecipe) holder.value())
                    .toList();

            fusionRecipes = recipeManager.getRecipes().stream()
                    .filter(holder -> holder.value() instanceof FusionRecipe)
                    .map(holder -> (FusionRecipe) holder.value())
                    .toList();

            // Pull any JSON recipes that did successfully load
            synthRecipes.addAll(recipeManager.getRecipes().stream()
                    .filter(holder -> holder.value() instanceof AethericSynthesizerRecipe)
                    .map(holder -> (AethericSynthesizerRecipe) holder.value())
                    .toList());
        }

        // ==========================================
        // INJECT HARDCODED FALLBACK RECIPES
        // ==========================================
        synthRecipes.addAll(AethericSynthesizerRecipe.getHardcodedRecipes());

        registration.addRecipes(PressureChamberRecipeCategory.TYPE, pcRecipes);
        registration.addRecipes(DilutedEssenceRecipeCategory.TYPE, deRecipes);
        registration.addRecipes(VisFusionRecipeCategory.TYPE, fusionRecipes);
        registration.addRecipes(AethericSynthesizerRecipeCategory.TYPE, synthRecipes);

        // Information Tabs
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
        registration.addRecipeCatalyst(NeoForgeTypes.FLUID_STACK, new FluidStack(ModFluids.DILUTED_ESSENCE_FLUID.get(), 1000), DilutedEssenceRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ENTROPIC_CORE.get()), VisFusionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.AETHERIC_SYNTHESIZER.get()), AethericSynthesizerRecipeCategory.TYPE);
    }
}