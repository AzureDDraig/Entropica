package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.recipe.AethericSynthesizerRecipe;
import ddraig.net.entropica.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

public class AethericSynthesizerRecipeCategory implements IRecipeCategory<AethericSynthesizerRecipe> {

    public static final RecipeType<AethericSynthesizerRecipe> TYPE =
            RecipeType.create(Entropica.MODID, "aetheric_synthesizer", AethericSynthesizerRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;
    private final IDrawable arrow;
    private final IDrawable slotDrawable;

    public AethericSynthesizerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 100);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.AETHERIC_SYNTHESIZER.get()));
        this.localizedName = Component.translatable("block.entropica.aetheric_synthesizer");
        this.arrow = guiHelper.createDrawable(ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png"), 79, 35, 24, 17);
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<AethericSynthesizerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AethericSynthesizerRecipe recipe, IFocusGroup focuses) {
        int startX = 5;
        int startY = 5;

        // 5x5 Input Grid
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                int index = row * 5 + col;
                int x = startX + (col * 18);
                int y = startY + (row * 18);

                var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .setBackground(slotDrawable, -1, -1);

                Optional<Ingredient> optIng = recipe.getGridIngredients().get(index);
                optIng.ifPresent(slotBuilder::addIngredients);
            }
        }

        // Output Items (Up to 4 in a 2x2 grid)
        List<ItemStack> results = recipe.getResults();
        int[][] outPos = {
                {120, 32}, // Single / Top-Left
                {138, 32}, // Top-Right
                {120, 50}, // Bottom-Left
                {138, 50}  // Bottom-Right
        };

        for (int i = 0; i < results.size() && i < 4; i++) {
            int x = (results.size() == 1) ? 128 : outPos[i][0]; // Center it if there is only 1 output
            int y = (results.size() == 1) ? 41 : outPos[i][1];

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(slotDrawable, -1, -1)
                    .addItemStack(results.get(i));
        }
    }

    @Override
    public void draw(AethericSynthesizerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 95, 41);
    }
}