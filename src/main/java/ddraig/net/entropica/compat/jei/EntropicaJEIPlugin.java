package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.recipe.AethericSynthesizerRecipe;
import ddraig.net.entropica.recipe.DilutedEssenceRecipe;
import ddraig.net.entropica.recipe.EidolicLatheRecipe;
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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
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
        registration.addRecipeCategories(new EidolicLatheRecipeCategory(guiHelper));
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipes(IRecipeRegistration registration) {
        List<PressureChamberRecipe> pcRecipes = Collections.emptyList();
        List<DilutedEssenceRecipe> deRecipes = Collections.emptyList();
        List<FusionRecipe> fusionRecipes = Collections.emptyList();

        // Ensure these are mutable lists so we can add hardcoded recipes to them
        List<AethericSynthesizerRecipe> synthRecipes = new ArrayList<>();
        List<EidolicLatheRecipe> latheRecipes = new ArrayList<>();

        Collection<RecipeHolder<?>> allRecipes = Collections.emptyList();

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            server = Minecraft.getInstance().getSingleplayerServer();
        }

        if (server != null) {
            allRecipes = server.getRecipeManager().getRecipes();
        }

        if (!allRecipes.isEmpty()) {
            pcRecipes = allRecipes.stream()
                    .filter(holder -> holder.value() instanceof PressureChamberRecipe)
                    .map(holder -> (PressureChamberRecipe) holder.value())
                    .toList();

            deRecipes = allRecipes.stream()
                    .filter(holder -> holder.value() instanceof DilutedEssenceRecipe)
                    .map(holder -> (DilutedEssenceRecipe) holder.value())
                    .toList();

            fusionRecipes = allRecipes.stream()
                    .filter(holder -> holder.value() instanceof FusionRecipe)
                    .map(holder -> (FusionRecipe) holder.value())
                    .toList();

            synthRecipes.addAll(allRecipes.stream()
                    .filter(holder -> holder.value() instanceof AethericSynthesizerRecipe)
                    .map(holder -> (AethericSynthesizerRecipe) holder.value())
                    .toList());

            latheRecipes.addAll(allRecipes.stream()
                    .filter(holder -> holder.value() instanceof EidolicLatheRecipe)
                    .map(holder -> (EidolicLatheRecipe) holder.value())
                    .toList());
        }

        // ==========================================
        // INJECT HARDCODED FALLBACK RECIPES
        // ==========================================
        // FIXED: Point to the new dedicated HardcodedRecipes class!
        synthRecipes.addAll(ddraig.net.entropica.recipe.HardcodedRecipes.getSynthesizerRecipes());
        latheRecipes.addAll(ddraig.net.entropica.recipe.HardcodedRecipes.getLatheRecipes());

        registration.addRecipes(PressureChamberRecipeCategory.TYPE, pcRecipes);
        registration.addRecipes(DilutedEssenceRecipeCategory.TYPE, deRecipes);
        registration.addRecipes(VisFusionRecipeCategory.TYPE, fusionRecipes);
        registration.addRecipes(AethericSynthesizerRecipeCategory.TYPE, synthRecipes);
        registration.addRecipes(EidolicLatheRecipeCategory.TYPE, latheRecipes);

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

        // Add catalysts for the Lathe
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get()), EidolicLatheRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ATTUNEMENT_PEDESTAL.get()), EidolicLatheRecipeCategory.TYPE);
    }
}