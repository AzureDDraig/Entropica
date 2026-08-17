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

public class AstralMultiblockRecipeCategory implements IRecipeCategory<AstralMultiblockRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_multiblock");
    public static final RecipeType<AstralMultiblockRecipe> TYPE = RecipeType.create(Entropica.MODID, "astral_multiblock", AstralMultiblockRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;

    public AstralMultiblockRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(180, 135);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.CELESTIAL_ARMILLARY_CONTROLLER.get()));
        this.slotDrawable = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<AstralMultiblockRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.entropica.astral_multiblock");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AstralMultiblockRecipe recipe, IFocusGroup focuses) {
        // Required blocks slots on the right side
        int startX = 110;
        int startY = 22;
        List<ItemStack> mats = recipe.requiredMaterials();
        for (int i = 0; i < Math.min(mats.size(), 8); i++) {
            int slotX = startX + (i % 3) * 19;
            int slotY = startY + (i / 3) * 19;
            builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                    .setBackground(slotDrawable, -1, -1)
                    .addItemStack(mats.get(i));
        }

        // Core / Catalyst Block
        builder.addSlot(RecipeIngredientRole.OUTPUT, 148, 85)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.coreBlock());
    }

    @Override
    public void draw(AstralMultiblockRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        Font font = Minecraft.getInstance().font;

        // Title and Dimensions Header
        guiGraphics.drawString(font, recipe.title(), 5, 4, 0x38BDF8, false);
        guiGraphics.drawString(font, recipe.dimensions(), 130, 4, 0xAAAAAA, false);

        // Calculate active cycling layer (cycles every 2 seconds)
        List<List<String>> layers = recipe.layerBlueprints();
        int layerIndex = 0;
        if (!layers.isEmpty()) {
            layerIndex = (int) ((System.currentTimeMillis() / 2000L) % layers.size());
        }

        // Layer Indicator Badge
        String layerText = "Layer Y=" + (layerIndex + 1) + " / " + layers.size();
        guiGraphics.fill(5, 18, 95, 30, 0x33000000);
        guiGraphics.drawString(font, layerText, 10, 20, 0xFFD700, false);

        // Draw Layer Blueprint Grid (2D Matrix)
        if (!layers.isEmpty() && layerIndex < layers.size()) {
            List<String> grid = layers.get(layerIndex);
            int gridStartY = 33;
            int cellSize = 9;

            for (int r = 0; r < grid.size(); r++) {
                String row = grid.get(r);
                for (int c = 0; c < row.length(); c++) {
                    char ch = row.charAt(c);
                    int x = 8 + c * cellSize;
                    int y = gridStartY + r * cellSize;

                    // Box background
                    int bgColor = (ch == '.') ? 0x22111122 : 0x44224488;
                    int fgColor = switch (ch) {
                        case 'P' -> 0x67E8F9; // Mirror Pool (cyan)
                        case 'M' -> 0xFFFFFF; // Marble (white)
                        case 'S' -> 0x94A3B8; // Slate (slate)
                        case 'C' -> 0xFDE047; // Pillar (gold)
                        case 'A' -> 0xF59E0B; // Armillary (amber)
                        case 'R' -> 0xE0E7FF; // Refractive Lens
                        case 'B' -> 0x38BDF8; // Beacon (azure)
                        case 'K' -> 0xA855F7; // Crystal (purple)
                        default -> 0x666666;  // Air / Other
                    };

                    guiGraphics.fill(x, y, x + cellSize - 1, y + cellSize - 1, bgColor);
                    guiGraphics.drawString(font, String.valueOf(ch), x + 2, y + 1, fgColor, false);
                }
            }
        }

        // Materials Label
        guiGraphics.drawString(font, "Materials:", 110, 13, 0xDDDDDD, false);

        // Core Label
        guiGraphics.drawString(font, "Core:", 115, 89, 0xFFD700, false);

        // Description / Function at Bottom
        guiGraphics.drawString(font, recipe.description(), 5, 115, 0x888888, false);
    }
}
