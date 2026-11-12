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

import java.util.List;

public class AstralAltarRecipeCategory implements IRecipeCategory<AstralAltarRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_altar_infusion");
    public static final RecipeType<AstralAltarRecipe> TYPE = RecipeType.create(Entropica.MODID, "astral_altar_infusion", AstralAltarRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;

    public AstralAltarRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(176, 110);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.ASTRAL_ALTAR_CORE.get()));
        this.slotDrawable = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<AstralAltarRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Celestial Infusion Matrix");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AstralAltarRecipe recipe, IFocusGroup focuses) {
        int cx = 58;
        int cy = 46;

        // Center core item
        builder.addSlot(RecipeIngredientRole.INPUT, cx, cy)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.centerInput());

        // Surrounding 8 pedestal slots in an octagon
        int[][] offsets = {
                {0, -32},   // Top
                {23, -23},  // Top-Right
                {32, 0},    // Right
                {23, 23},   // Bottom-Right
                {0, 32},    // Bottom
                {-23, 23},  // Bottom-Left
                {-32, 0},   // Left
                {-23, -23}  // Top-Left
        };

        List<ItemStack> pInputs = recipe.pedestalInputs();
        for (int i = 0; i < 8; i++) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, cx + offsets[i][0], cy + offsets[i][1])
                    .setBackground(slotDrawable, -1, -1);
            if (i < pInputs.size() && !pInputs.get(i).isEmpty()) {
                slot.addItemStack(pInputs.get(i));
            }
        }

        // Output Slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 138, cy)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(AstralAltarRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        Font font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, "✦ " + recipe.title().getString(), 8, 4, 0xFF38BDF8, false);
        guiGraphics.drawString(font, "➔", 112, 50, 0xFF94A3B8, false);

        // Pedestals indicator
        int count = recipe.pedestalInputs().size();
        guiGraphics.drawString(font, "§7Pedestals: §f" + (count == 0 ? "None (Direct Starlight)" : (count + " Reagents")), 8, 98, 0xFFAAAAAA, false);
    }
}
