package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
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

public class AstralOpticalRecipeCategory implements IRecipeCategory<AstralOpticalRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_optical_infusion");
    public static final RecipeType<AstralOpticalRecipe> TYPE = RecipeType.create(Entropica.MODID, "astral_optical_infusion", AstralOpticalRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;

    public AstralOpticalRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(176, 86);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.ASTRAL_INFUSION_PEDESTAL.get()));
        this.slotDrawable = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<AstralOpticalRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Dry Optical Infusion");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AstralOpticalRecipe recipe, IFocusGroup focuses) {
        // Pedestal Input Item
        builder.addSlot(RecipeIngredientRole.INPUT, 28, 30)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.input());

        // Transmuted Output Item
        builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 30)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.output());

        // Substrate result (if any)
        if (!recipe.substrateResult().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 58)
                    .setBackground(slotDrawable, -1, -1)
                    .addItemStack(recipe.substrateResult());
        }
    }

    @Override
    public void draw(AstralOpticalRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        Font font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, "✦ " + recipe.title().getString(), 8, 4, 0xFF38BDF8, false);

        // Beam Arrow & Star label
        guiGraphics.drawString(font, "═══ ✧ ═══➔", 56, 34, 0xFF67E8F9, false);
        guiGraphics.drawString(font, "§b" + recipe.constellationRequired(), 88 - (font.width(recipe.constellationRequired()) / 2), 20, 0xFFFFFFFF, false);

        if (!recipe.substrateResult().isEmpty()) {
            guiGraphics.drawString(font, "§7Substrate:", 20, 62, 0xFFAAAAAA, false);
        } else {
            guiGraphics.drawString(font, "§8Auto-ejects downward into chest below.", 8, 72, 0xFFAAAAAA, false);
        }
    }
}
