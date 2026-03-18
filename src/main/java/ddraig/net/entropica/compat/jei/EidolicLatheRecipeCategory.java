package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.recipe.EidolicLatheRecipe;
import ddraig.net.entropica.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("removal")
public class EidolicLatheRecipeCategory implements IRecipeCategory<EidolicLatheRecipe> {

    public static final RecipeType<EidolicLatheRecipe> TYPE =
            RecipeType.create(Entropica.MODID, "eidolic_lathe", EidolicLatheRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;
    private final IDrawable slotDrawable;

    public EidolicLatheRecipeCategory(IGuiHelper guiHelper) {
        // Increased the canvas size to comfortably fit a massive 16-slot outer ring!
        this.background = guiHelper.createBlankDrawable(180, 130);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get()));
        this.localizedName = Component.translatable("block.entropica.eidolic_focal_pedestal");
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<EidolicLatheRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EidolicLatheRecipe recipe, IFocusGroup focuses) {
        int centerX = 65;
        int centerY = 65;

        // 1. The Central Focal Pedestal Inputs
        builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY - 18)
                .setBackground(slotDrawable, -1, -1)
                .addIngredients(recipe.etherealShape());

        builder.addSlot(RecipeIngredientRole.INPUT, centerX + 18, centerY)
                .setBackground(slotDrawable, -1, -1)
                .addIngredients(recipe.core());

        builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY + 18)
                .setBackground(slotDrawable, -1, -1)
                .addIngredients(recipe.orbisAcceptor());

        // Base Material Slot
        builder.addSlot(RecipeIngredientRole.INPUT, centerX - 18, centerY)
                .setBackground(slotDrawable, -1, -1)
                .addIngredients(recipe.baseMaterial());

        // 2. Dynamic Attunement Pedestal Modifiers
        // Always renders a perfect circle of 16 slots!
        int numModifiers = recipe.modifiers().size();
        int totalSlots = 16;
        double radius = 46.0;

        for (int i = 0; i < totalSlots; i++) {
            double angle = -Math.PI / 2.0 + (i * 2 * Math.PI / totalSlots);

            int slotX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int slotY = centerY + (int) Math.round(Math.sin(angle) * radius);

            var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                    .setBackground(slotDrawable, -1, -1);

            // Populate the slots with ingredients if the recipe provides them.
            // Any unused slots will gracefully remain as empty guides!
            if (i < numModifiers) {
                slotBuilder.addIngredients(recipe.modifiers().get(i));
            }
        }

        // 3. The Final Forged Result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 145, centerY - 9)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(EidolicLatheRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);

        Font font = Minecraft.getInstance().font;

        long time = System.currentTimeMillis();
        float t = (time % 4000L) / 4000.0f * (float) Math.PI * 2;
        int r = (int) ((Math.sin(t) * 0.5 + 0.5) * 255);
        int g = (int) ((Math.sin(t + 2.094) * 0.5 + 0.5) * 255);
        int b = (int) ((Math.sin(t + 4.188) * 0.5 + 0.5) * 255);
        int dynamicColor = (0xFF << 24) | (r << 16) | (g << 8) | b;

        // Text positioned neatly in the bottom right corner, out of the way of the massive ring
        guiGraphics.drawString(font, "Fuel Requirement:", 90, 90, 0xFF888888, false);
        guiGraphics.drawString(font, "1 to 8000 Vis", 90, 100, dynamicColor, false);
        guiGraphics.drawString(font, "(Fumes or Ichor)", 90, 110, 0xFFAAAAAA, false);

        guiGraphics.drawString(font, "➔", 125, 60, 0xFFFFFFFF, true);

        int materialX = 65 - 18;
        int materialY = 65;
        guiGraphics.drawString(font, "4", materialX + 12, materialY + 9, 0xFFFFFF, true);
    }
}