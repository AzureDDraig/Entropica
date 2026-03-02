package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.recipe.PressureChamberRecipe;
import ddraig.net.entropica.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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

import java.util.ArrayList;
import java.util.List;

public class PressureChamberRecipeCategory implements IRecipeCategory<PressureChamberRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "pressure_chamber");
    public static final RecipeType<PressureChamberRecipe> TYPE = new RecipeType<>(UID, PressureChamberRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public PressureChamberRecipeCategory(IGuiHelper helper) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/gui/jei/pressure_chamber.png");
        this.background = helper.createDrawable(texture, 0, 0, 150, 60);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER.get()));
    }

    @Override
    public RecipeType<PressureChamberRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.entropica.vis_fume_pressure_chamber_controller");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PressureChamberRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = new ArrayList<>();
        recipe.inputItem().items().forEach(holder -> {
            ItemStack copy = new ItemStack(holder);
            copy.setCount(recipe.inputCount());
            inputs.add(copy);
        });

        builder.addSlot(RecipeIngredientRole.INPUT, 10, 20)
                .addItemStacks(inputs);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 20)
                .addItemStacks(List.of(recipe.output()));
    }

    @Override
    public void draw(PressureChamberRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw background manually since getBackground() is deprecated
        background.draw(guiGraphics);

        Font font = Minecraft.getInstance().font;

        boolean requiresSpecific = recipe.requiresSpecificFume();
        String gasName = (requiresSpecific && recipe.requiredFume().isPresent())
                ? recipe.requiredFume().get().name()
                : "Any Fume";

        String costText = recipe.fumeAmount() + " mB " + gasName;
        String timeText = recipe.processingTime() + " Ticks";

        int textColor = 0xFF888888;
        if (requiresSpecific && recipe.requiredFume().isPresent()) {
            textColor = recipe.requiredFume().get().getColorInt();
        }

        guiGraphics.drawString(font, costText, 35, 15, textColor, false);
        guiGraphics.drawString(font, timeText, 35, 35, 0xFF888888, false);
    }
}