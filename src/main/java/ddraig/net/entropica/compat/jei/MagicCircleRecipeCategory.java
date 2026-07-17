package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.recipe.MagicCircleRecipe;
import ddraig.net.entropica.registry.ModItems;
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

import java.util.Map;

@SuppressWarnings("removal")
public class MagicCircleRecipeCategory implements IRecipeCategory<MagicCircleRecipe> {

    public static final RecipeType<MagicCircleRecipe> TYPE =
            RecipeType.create(Entropica.MODID, "magic_circle", MagicCircleRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;
    private final IDrawable slotDrawable;

    public MagicCircleRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(180, 110);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.CHALK.get()));
        this.localizedName = Component.translatable("recipe.entropica.magic_circle");
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<MagicCircleRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, MagicCircleRecipe recipe, IFocusGroup focuses) {
        // Place inputs in a row at y = 15
        int inputXStart = 45;
        int inputY = 15;
        for (int i = 0; i < recipe.inputs().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputXStart + i * 18, inputY)
                    .setBackground(slotDrawable, -1, -1)
                    .addIngredients(recipe.inputs().get(i));
        }

        // Place runes in a row at y = 40
        int runeXStart = 45;
        int runeY = 40;
        for (int i = 0; i < recipe.runes().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, runeXStart + i * 18, runeY)
                    .setBackground(slotDrawable, -1, -1)
                    .addIngredients(recipe.runes().get(i));
        }

        // Place the output stack at the right
        builder.addSlot(RecipeIngredientRole.OUTPUT, 145, 27)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(MagicCircleRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);

        Font font = Minecraft.getInstance().font;

        // Draw headers/labels
        guiGraphics.drawString(font, "Inputs:", 5, 19, 0xFF888888, false);
        guiGraphics.drawString(font, "Runes:", 5, 44, 0xFF888888, false);
        guiGraphics.drawString(font, "➔", 125, 31, 0xFFFFFFFF, true);

        // Draw Required Tier info
        String tierText = "Min Tier: " + recipe.tier();
        guiGraphics.drawString(font, tierText, 5, 5, 0xFF55FF55, false);

        // Draw Essences required at the bottom (y = 65 and onwards)
        guiGraphics.drawString(font, "Required Essence:", 5, 65, 0xFF888888, false);

        int essenceY = 78;
        int count = 0;
        for (Map.Entry<EssenceType, Integer> entry : recipe.essences().entrySet()) {
            if (entry.getValue() > 0) {
                String name = entry.getKey().name().toLowerCase();
                if (name.length() > 0) {
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                }
                String essenceText = name + ": " + entry.getValue();
                int color = entry.getKey().getColorInt();
                int xOffset = (count % 2) * 90 + 5;
                int yOffset = essenceY + (count / 2) * 12;
                guiGraphics.drawString(font, essenceText, xOffset, yOffset, color, false);
                count++;
            }
        }
        if (count == 0) {
            guiGraphics.drawString(font, "None", 5, essenceY, 0xFFAAAAAA, false);
        }
    }
}
