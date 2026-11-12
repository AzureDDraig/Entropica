package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
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
import net.minecraft.world.item.Items;

public class FloraHarvestingCategory implements IRecipeCategory<FloraHarvestingRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "flora_harvesting");
    public static final RecipeType<FloraHarvestingRecipe> TYPE = new RecipeType<>(UID, FloraHarvestingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotBackground;

    public FloraHarvestingCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(120, 36);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.SHEARS));
        this.slotBackground = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<FloraHarvestingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.entropica.flora_harvesting");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FloraHarvestingRecipe recipe, IFocusGroup focuses) {
        // Plant input slot
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 9)
                .setBackground(this.slotBackground, -1, -1)
                .addItemStack(recipe.plantBlock());

        // Tool input slot (Shears)
        builder.addSlot(RecipeIngredientRole.INPUT, 36, 9)
                .setBackground(this.slotBackground, -1, -1)
                .addItemStack(recipe.toolItem());


        // Output Petals slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 9)
                .setBackground(this.slotBackground, -1, -1)
                .addItemStacks(recipe.petalOutputs());
    }
}
