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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EidolicLatheRecipeCategory implements IRecipeCategory<EidolicLatheRecipe> {

    // FIXED: Updated to the modern JEI RecipeType constructor to clear the deprecation warning
    public static final RecipeType<EidolicLatheRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eidolic_lathe"), EidolicLatheRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;
    private final IDrawable slotDrawable;

    public EidolicLatheRecipeCategory(IGuiHelper guiHelper) {
        // Expanded the canvas to 180x110 to fit up to 16 slots comfortably
        this.background = guiHelper.createBlankDrawable(180, 110);
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EidolicLatheRecipe recipe, IFocusGroup focuses) {
        int centerX = 55;
        int centerY = 55;

        // 1. The Central Focal Pedestal Inputs
        builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY - 10)
                .setBackground(slotDrawable, -1, -1)
                .addIngredients(recipe.etherealShape());

        builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY + 10)
                .setBackground(slotDrawable, -1, -1)
                .addIngredients(recipe.core());

        // 2. Dynamic Attunement Pedestal Modifiers (Trigonometry to arrange in a perfect circle)
        int numModifiers = recipe.modifiers().size();

        // If there are more than 8 items, we widen the circle's radius to prevent the slots from overlapping
        double radius = numModifiers > 8 ? 42.0 : 32.0;

        for (int i = 0; i < numModifiers; i++) {
            // Calculate angle starting from the top (-PI/2) and distributing evenly
            double angle = -Math.PI / 2.0 + (i * 2 * Math.PI / numModifiers);

            int slotX = centerX + (int) Math.round(Math.cos(angle) * radius);
            int slotY = centerY + (int) Math.round(Math.sin(angle) * radius);

            builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                    .setBackground(slotDrawable, -1, -1)
                    .addIngredients(recipe.modifiers().get(i));
        }

        // 3. The Final Forged Result (Pushed to the far right)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 145, centerY - 9)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(EidolicLatheRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // FIXED: Replaced the missing fuelType()/fuelAmount() calls with the dynamic rainbow text!
        long time = System.currentTimeMillis();
        float t = (time % 4000L) / 4000.0f * (float) Math.PI * 2;
        int r = (int) ((Math.sin(t) * 0.5 + 0.5) * 255);
        int g = (int) ((Math.sin(t + 2.094) * 0.5 + 0.5) * 255);
        int b = (int) ((Math.sin(t + 4.188) * 0.5 + 0.5) * 255);
        int dynamicColor = (0xFF << 24) | (r << 16) | (g << 8) | b;

        guiGraphics.drawString(font, "Fuel Requirement:", 115, 65, 0xFF888888, false);
        guiGraphics.drawString(font, "1 to 8000 Vis", 115, 75, dynamicColor, false);
        guiGraphics.drawString(font, "(Fumes or Ichor)", 115, 85, 0xFFAAAAAA, false);

        guiGraphics.drawString(font, "➔", 115, 50, 0xFFFFFFFF, true);
    }
}