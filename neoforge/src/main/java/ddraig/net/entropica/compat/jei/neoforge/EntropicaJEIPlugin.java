package ddraig.net.entropica.compat.jei.neoforge;

import ddraig.net.entropica.compat.jei.PressureChamberRecipeCategory;
import ddraig.net.entropica.compat.jei.MateriaFusionRecipeCategory;
import ddraig.net.entropica.compat.jei.AethericSynthesizerRecipeCategory;
import ddraig.net.entropica.compat.jei.EidolicLatheRecipeCategory;
import ddraig.net.entropica.compat.jei.EssenceExtractionCategory;
import ddraig.net.entropica.compat.jei.EssenceExtractionRecipe;
import ddraig.net.entropica.compat.jei.MagicCircleRecipeCategory;
import ddraig.net.entropica.compat.jei.FloraHarvestingCategory;
import ddraig.net.entropica.compat.jei.FloraHarvestingRecipe;

import ddraig.net.entropica.recipe.MagicCircleRecipe;
import net.minecraft.world.item.Items;


import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.ItemEssenceMap;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.recipe.AethericSynthesizerRecipe;
import ddraig.net.entropica.recipe.DilutedEssenceRecipe;
import ddraig.net.entropica.recipe.EidolicLatheRecipe;
import ddraig.net.entropica.recipe.FusionRecipe;
import ddraig.net.entropica.recipe.PressureChamberRecipe;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModFluids;
import ddraig.net.entropica.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@JeiPlugin
public class EntropicaJEIPlugin implements IModPlugin {

    // Capture the runtime so we can dynamically inject recipes!
    private static mezz.jei.api.runtime.IJeiRuntime jeiRuntime;
    private static final List<EssenceExtractionRecipe> dynamicRecipes = new ArrayList<>();
    private static Level lastLevel = null;

    public EntropicaJEIPlugin() {
        // We watch the Client Tick to trigger the exact moment the player is fully loaded into a world!
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    private void onClientTick(ClientTickEvent.Post event) {
        if (jeiRuntime == null) return;

        Minecraft mc = Minecraft.getInstance();
        Level currentLevel = mc.level;

        // Reset the tracker if we disconnect from the world
        if (currentLevel == null) {
            lastLevel = null;
            return;
        }

        // Wait until the player is fully spawned into the world
        if (mc.player == null) return;

        // Trigger only when transitioning into a new world
        if (currentLevel != lastLevel) {
            lastLevel = currentLevel;

            net.minecraft.server.MinecraftServer server = null;
            try {
                server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) server = mc.getSingleplayerServer();
            } catch (Exception e) {}

            if (server != null) {
                var recipes = server.getRecipeManager().getRecipes();

                // RACE CONDITION FIX: Wait if the server is instantiated but recipes haven't parsed yet!
                if (!recipes.iterator().hasNext()) {
                    lastLevel = null; // Revert tracking so it tries again next tick
                    return;
                }

                // Build the Graph! (This inherently clears the old caches)
                ItemEssenceMap.buildEssenceGraph(recipes, server.registryAccess());
            } else {
                // If the server isn't ready yet, safely revert tracking and try again next tick
                lastLevel = null;
                return;
            }

            // Clear any previously injected extraction recipes from JEI to prevent duplicates
            if (!dynamicRecipes.isEmpty()) {
                jeiRuntime.getRecipeManager().hideRecipes(EssenceExtractionCategory.TYPE, dynamicRecipes);
                dynamicRecipes.clear();
            }

            // Generate JEI Recipes based on the newly built graph
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack stack = new ItemStack(item);
                List<ItemEssenceMap.EssenceValue> values = ItemEssenceMap.getEssenceFor(stack);

                if (values != null && !values.isEmpty()) {
                    // Filter out the fallback so JEI isn't flooded with useless dirt recipes
                    if (values.size() == 1 && values.get(0).type() == EssenceType.EARTH && values.get(0).amount() == 0.03125f) continue;

                    List<ItemStack> outputs = new ArrayList<>();
                    List<Float> chances = new ArrayList<>();

                    for (ItemEssenceMap.EssenceValue val : values) {
                        int visualCount = Math.max(1, (int) Math.ceil(val.amount()));
                        ItemStack out = new ItemStack(ModItems.WEAK_ESSENCE.get(), visualCount);
                        EssenceItem.setEssenceType(out, val.type());

                        outputs.add(out);
                        chances.add(val.amount());
                    }
                    dynamicRecipes.add(new EssenceExtractionRecipe(stack, outputs, chances));
                }
            }

            // Inject into live JEI
            if (!dynamicRecipes.isEmpty()) {
                jeiRuntime.getRecipeManager().addRecipes(EssenceExtractionCategory.TYPE, dynamicRecipes);
                Entropica.LOGGER.info("Successfully injected " + dynamicRecipes.size() + " Essence Extraction recipes into JEI!");
            }
        }

        // Periodically update JEI ingredient visibility based on REQUIRE_RESEARCH_TO_CRAFT config
        if (mc.level.getGameTime() % 40 == 0) {
            updateResearchVisibility();
        }
    }

    private static final java.util.Set<Item> CURRENTLY_HIDDEN_ITEMS = new java.util.HashSet<>();

    public static void updateResearchVisibility() {
        if (jeiRuntime == null) return;

        boolean requireResearch = Boolean.TRUE.equals(ddraig.net.entropica.config.EntropicaConfig.REQUIRE_RESEARCH_TO_CRAFT.get());
        var ingredientManager = jeiRuntime.getIngredientManager();

        List<ItemStack> toHide = new ArrayList<>();
        List<ItemStack> toUnhide = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            var node = ddraig.net.entropica.event.CodexCraftingLockHandler.getRequiredNodeForStack(stack);
            if (node != null) {
                boolean isLocked = ddraig.net.entropica.data.CodexPlayerData.CLIENT_DATA.isNodeLocked(node.id)
                        || node.requiredTier > ddraig.net.entropica.data.CodexPlayerData.CLIENT_DATA.getResearchTierLevel()
                        || (node.prerequisiteId != null && ddraig.net.entropica.data.CodexPlayerData.CLIENT_DATA.isNodeLocked(node.prerequisiteId));

                boolean shouldHide = requireResearch && isLocked;
                boolean isHidden = CURRENTLY_HIDDEN_ITEMS.contains(item);

                if (shouldHide && !isHidden) {
                    toHide.add(stack);
                    CURRENTLY_HIDDEN_ITEMS.add(item);
                } else if (!shouldHide && isHidden) {
                    toUnhide.add(stack);
                    CURRENTLY_HIDDEN_ITEMS.remove(item);
                }
            }
        }

        if (!toHide.isEmpty()) {
            ingredientManager.removeIngredientsAtRuntime(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, toHide);
        }
        if (!toUnhide.isEmpty()) {
            ingredientManager.addIngredientsAtRuntime(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, toUnhide);
        }
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new PressureChamberRecipeCategory(guiHelper));
        registration.addRecipeCategories(new DilutedEssenceRecipeCategory(guiHelper));
        registration.addRecipeCategories(new MateriaFusionRecipeCategory(guiHelper));
        registration.addRecipeCategories(new AethericSynthesizerRecipeCategory(guiHelper));
        registration.addRecipeCategories(new EidolicLatheRecipeCategory(guiHelper));
        registration.addRecipeCategories(new EssenceExtractionCategory(guiHelper));
        registration.addRecipeCategories(new MagicCircleRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FloraHarvestingCategory(guiHelper));
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipes(IRecipeRegistration registration) {
        List<PressureChamberRecipe> pcRecipes = Collections.emptyList();
        List<DilutedEssenceRecipe> deRecipes = Collections.emptyList();
        List<FusionRecipe> fusionRecipes = Collections.emptyList();
        List<MagicCircleRecipe> magicCircleRecipes = new ArrayList<>();
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
            pcRecipes = allRecipes.stream().filter(holder -> holder.value() instanceof PressureChamberRecipe).map(holder -> (PressureChamberRecipe) holder.value()).toList();
            deRecipes = allRecipes.stream().filter(holder -> holder.value() instanceof DilutedEssenceRecipe).map(holder -> (DilutedEssenceRecipe) holder.value()).toList();
            fusionRecipes = allRecipes.stream().filter(holder -> holder.value() instanceof FusionRecipe).map(holder -> (FusionRecipe) holder.value()).toList();
            magicCircleRecipes.addAll(allRecipes.stream().filter(holder -> holder.value() instanceof MagicCircleRecipe).map(holder -> (MagicCircleRecipe) holder.value()).toList());
            synthRecipes.addAll(allRecipes.stream().filter(holder -> holder.value() instanceof AethericSynthesizerRecipe).map(holder -> (AethericSynthesizerRecipe) holder.value()).toList());
            latheRecipes.addAll(allRecipes.stream().filter(holder -> holder.value() instanceof EidolicLatheRecipe).map(holder -> (EidolicLatheRecipe) holder.value()).toList());
        }

        magicCircleRecipes.addAll(ddraig.net.entropica.recipe.HardcodedRecipes.getMagicCircleRecipes());
        synthRecipes.addAll(ddraig.net.entropica.recipe.HardcodedRecipes.getSynthesizerRecipes());
        latheRecipes.addAll(ddraig.net.entropica.recipe.HardcodedRecipes.getLatheRecipes());

        registration.addRecipes(PressureChamberRecipeCategory.TYPE, pcRecipes);
        registration.addRecipes(DilutedEssenceRecipeCategory.TYPE, deRecipes);
        registration.addRecipes(MateriaFusionRecipeCategory.TYPE, fusionRecipes);
        registration.addRecipes(MagicCircleRecipeCategory.TYPE, magicCircleRecipes);
        registration.addRecipes(AethericSynthesizerRecipeCategory.TYPE, synthRecipes);
        registration.addRecipes(EidolicLatheRecipeCategory.TYPE, latheRecipes);
        registration.addRecipes(FloraHarvestingCategory.TYPE, FloraHarvestingRecipe.createAllRecipes());
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.SHEARS), FloraHarvestingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MATERIA_PRESSURE_CHAMBER_CONTROLLER.get()), PressureChamberRecipeCategory.TYPE);

        registration.addRecipeCatalyst(NeoForgeTypes.FLUID_STACK, new FluidStack(ModFluids.getSource(), 1000), DilutedEssenceRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ENTROPIC_CORE.get()), MateriaFusionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.AETHERIC_SYNTHESIZER.get()), AethericSynthesizerRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get()), EidolicLatheRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ATTUNEMENT_PEDESTAL.get()), EidolicLatheRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRUCIBLE.get()), EssenceExtractionCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MARBLE_RITUAL_BOWL.get()), EssenceExtractionCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BASALT_RITUAL_BOWL.get()), EssenceExtractionCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModItems.CHALK.get()), MagicCircleRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DULL_CHALK.get()), MagicCircleRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.CONDUCTIVE_CHALK.get()), MagicCircleRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.RESONANT_CHALK.get()), MagicCircleRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.EIDOLIC_CHALK.get()), MagicCircleRecipeCategory.TYPE);
    }
}
