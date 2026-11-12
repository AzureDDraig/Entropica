package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EssenceExtractionCategory implements IRecipeCategory<EssenceExtractionRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_extraction");
    public static final RecipeType<EssenceExtractionRecipe> TYPE = RecipeType.create(Entropica.MODID, "essence_extraction", EssenceExtractionRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotBackground;

    public EssenceExtractionCategory(IGuiHelper helper) {
        // Expanded the blank canvas slightly to accommodate the dynamic slot grids
        this.background = helper.createBlankDrawable(140, 44);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.CRUCIBLE.get()));

        // Grab the standard JEI 18x18 physical slot background!
        this.slotBackground = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<EssenceExtractionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.entropica.essence_extraction");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EssenceExtractionRecipe recipe, IFocusGroup focuses) {

        // 1. Create the fixed Input Slot on the left side
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 14)
                .setBackground(this.slotBackground, -1, -1) // Physically draws the slot behind the item!
                .addItemStack(recipe.input());

        // 2. Dynamically loop through the outputs and generate a physical slot for each one!
        int startX = 40;
        int startY = 14;

        for (int i = 0; i < recipe.outputs().size(); i++) {

            // MATH: If an item yields more than 5 distinct essences, this will automatically
            // wrap the slots down to a second row so they don't bleed off the JEI screen!
            int xOffset = startX + ((i % 5) * 18);
            int yOffset = startY + ((i / 5) * 18);

            final float amount = recipe.chances().get(i);

            // Generate the dynamic output slot
            builder.addSlot(RecipeIngredientRole.OUTPUT, xOffset, yOffset)
                    .setBackground(this.slotBackground, -1, -1) // Physically draws the slot behind the item!
                    .addItemStack(recipe.outputs().get(i))
                    .addTooltipCallback((recipeSlotView, tooltip) -> {

                        int guaranteed = (int) amount;
                        float chance = (amount - guaranteed) * 100.0f;

                        if (guaranteed > 0) {
                            tooltip.add(Component.literal("§7Yield: §a" + guaranteed));
                            if (chance > 0) {
                                String formattedChance = String.format("%.1f", chance).replace(".0", "");
                                tooltip.add(Component.literal("§8(+" + formattedChance + "% chance for extra)"));
                            }
                        } else {
                            String formattedChance = String.format("%.1f", chance).replace(".0", "");
                            tooltip.add(Component.literal("§7Chance: §e" + formattedChance + "%"));
                        }
                    });
        }
    }
}