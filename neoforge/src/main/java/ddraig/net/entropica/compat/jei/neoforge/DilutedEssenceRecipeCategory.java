package ddraig.net.entropica.compat.jei.neoforge;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.recipe.DilutedEssenceRecipe;
import ddraig.net.entropica.registry.ModFluids;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class DilutedEssenceRecipeCategory implements IRecipeCategory<DilutedEssenceRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "diluted_essence");
    public static final RecipeType<DilutedEssenceRecipe> TYPE = new RecipeType<>(UID, DilutedEssenceRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;

    public DilutedEssenceRecipeCategory(IGuiHelper helper) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/gui/jei/jei_diluted_essence.png");
        this.background = helper.createDrawable(texture, 0, 0, 150, 60);
        this.icon = helper.createDrawableIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(ModFluids.getSource(), 1000));
        this.slotDrawable = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<DilutedEssenceRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.entropica.diluted_essence");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DilutedEssenceRecipe recipe, IFocusGroup focuses) {
        int startX = 10;
        int startY = 20;

        for (int i = 0; i < recipe.inputs().size(); i++) {
            Ingredient ingredient = recipe.inputs().get(i);
            int xPos = startX + (i * 18);

            List<ItemStack> stacks = new ArrayList<>();
            ingredient.items().forEach(holder -> stacks.add(new ItemStack(holder)));

            builder.addSlot(RecipeIngredientRole.INPUT, xPos, startY)
                    .setBackground(this.slotDrawable, -1, -1)
                    .addItemStacks(stacks);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 20)
                .setBackground(this.slotDrawable, -1, -1)
                .addItemStacks(List.of(recipe.output()));
    }

    @Override
    public void draw(DilutedEssenceRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);

        Font font = Minecraft.getInstance().font;

        String costText = recipe.chargeCost() + " Charge";
        String timeText = recipe.processingTime() / 20 + " seconds";

        guiGraphics.drawString(font, costText, 10, 5, 0xFFB200FF, false);
        guiGraphics.drawString(font, timeText, 10, 42, 0xFF888888, false);
    }
}
