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

import java.util.ArrayList;
import java.util.List;

public class AstralMultiblockRecipeCategory implements IRecipeCategory<AstralMultiblockRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_multiblock");
    public static final RecipeType<AstralMultiblockRecipe> TYPE = RecipeType.create(Entropica.MODID, "astral_multiblock", AstralMultiblockRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;

    public AstralMultiblockRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(184, 136);
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
        int startX = 104;
        int startY = 24;
        List<ItemStack> mats = recipe.requiredMaterials();
        for (int i = 0; i < Math.min(mats.size(), 8); i++) {
            int slotX = startX + (i % 4) * 19;
            int slotY = startY + (i / 4) * 19;
            builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                    .setBackground(slotDrawable, -1, -1)
                    .addItemStack(mats.get(i));
        }

        // Core / Catalyst Controller Block
        builder.addSlot(RecipeIngredientRole.OUTPUT, 150, 78)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.coreBlock());
    }

    @Override
    public void draw(AstralMultiblockRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        Font font = Minecraft.getInstance().font;

        // 1. Header Navigation & Dimensions
        guiGraphics.drawString(font, "✦ " + recipe.title().getString(), 5, 3, 0xFF38BDF8, false);
        guiGraphics.drawString(font, "§7" + recipe.dimensions(), 144, 3, 0xFFAAAAAA, false);

        List<List<String>> layers = recipe.layerBlueprints();
        int totalLayers = Math.max(1, layers.size());

        // 2. Interactive Layer Scrubbing & Auto-Cycle
        int layerIndex = (int) ((System.currentTimeMillis() / 2500L) % totalLayers);

        // Check if mouse is hovering over any layer indicator button
        int buttonStartX = 6;
        int buttonY = 14;
        int buttonWidth = Math.min(14, 88 / totalLayers);

        for (int l = 0; l < totalLayers; l++) {
            int bx = buttonStartX + l * (buttonWidth + 2);
            if (mouseX >= bx && mouseX <= bx + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + 11) {
                layerIndex = l;
                break;
            }
        }

        // Render Layer Selector Buttons [1] [2] [3] ...
        for (int l = 0; l < totalLayers; l++) {
            int bx = buttonStartX + l * (buttonWidth + 2);
            boolean isActive = (l == layerIndex);
            boolean isHovered = mouseX >= bx && mouseX <= bx + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + 11;

            int btnBg = isActive ? 0xFF0284C7 : (isHovered ? 0xFF1E293B : 0xFF0F172A);
            int btnBorder = isActive ? 0xFF38BDF8 : 0xFF334155;
            int textColor = isActive ? 0xFFFFFFFF : (isHovered ? 0xFFE2E8F0 : 0xFF94A3B8);

            guiGraphics.fill(bx, buttonY, bx + buttonWidth, buttonY + 11, btnBg);
            guiGraphics.fill(bx, buttonY, bx + buttonWidth, buttonY + 1, btnBorder);
            guiGraphics.fill(bx, buttonY + 10, bx + buttonWidth, buttonY + 11, btnBorder);
            guiGraphics.fill(bx, buttonY, bx + 1, buttonY + 11, btnBorder);
            guiGraphics.fill(bx + buttonWidth - 1, buttonY, bx + buttonWidth, buttonY + 11, btnBorder);

            String lbl = String.valueOf(l + 1);
            int textX = bx + (buttonWidth - font.width(lbl)) / 2;
            guiGraphics.drawString(font, lbl, textX, buttonY + 2, textColor, false);
        }

        // 3. 2D Layer Blueprint Matrix Rendering
        ItemStack hoveredStack = null;
        String hoveredPos = null;
        int hoveredCount = 0;

        if (!layers.isEmpty() && layerIndex < layers.size()) {
            List<String> grid = layers.get(layerIndex);
            int rows = grid.size();
            int cols = grid.get(0).length();

            int areaSize = 92;
            int cellSize = Math.min(areaSize / cols, areaSize / rows);
            int gridStartX = 5 + (areaSize - (cols * cellSize)) / 2;
            int gridStartY = 28 + (areaSize - (rows * cellSize)) / 2;

            for (int r = 0; r < rows; r++) {
                String row = grid.get(r);
                for (int c = 0; c < cols; c++) {
                    char ch = row.charAt(c);
                    int cx = gridStartX + c * cellSize;
                    int cy = gridStartY + r * cellSize;

                    boolean isCellHovered = (mouseX >= cx && mouseX < cx + cellSize && mouseY >= cy && mouseY < cy + cellSize);

                    if (ch == '.') {
                        // Empty air space
                        guiGraphics.fill(cx, cy, cx + cellSize, cy + cellSize, 0x440F172A);
                        guiGraphics.fill(cx, cy, cx + cellSize, cy + 1, 0x22334155);
                        guiGraphics.fill(cx, cy + cellSize - 1, cx + cellSize, cy + cellSize, 0x22334155);
                        guiGraphics.fill(cx, cy, cx + 1, cy + cellSize, 0x22334155);
                        guiGraphics.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize, 0x22334155);

                        if (isCellHovered) {
                            guiGraphics.fill(cx, cy, cx + cellSize, cy + 1, 0xFFFFD700);
                            guiGraphics.fill(cx, cy + cellSize - 1, cx + cellSize, cy + cellSize, 0xFFFFD700);
                            guiGraphics.fill(cx, cy, cx + 1, cy + cellSize, 0xFFFFD700);
                            guiGraphics.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize, 0xFFFFD700);

                            hoveredStack = ItemStack.EMPTY;
                            hoveredPos = "Layer Y=" + (layerIndex + 1) + " | Row=" + (r + 1) + ", Col=" + (c + 1);
                        }
                    } else {
                        // Structural Block Cell
                        ItemStack stack = recipe.symbolLegend().get(ch);

                        guiGraphics.fill(cx, cy, cx + cellSize, cy + cellSize, 0x991E293B);
                        int border = isCellHovered ? 0xFFFFD700 : 0xFF38BDF8;
                        guiGraphics.fill(cx, cy, cx + cellSize, cy + 1, border);
                        guiGraphics.fill(cx, cy + cellSize - 1, cx + cellSize, cy + cellSize, border);
                        guiGraphics.fill(cx, cy, cx + 1, cy + cellSize, border);
                        guiGraphics.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize, border);

                        if (stack != null && !stack.isEmpty()) {
                            float scale = (float) (cellSize - 2) / 16.0f;
                            guiGraphics.pose().pushMatrix();
                            guiGraphics.pose().translate((float)(cx + 1), (float)(cy + 1));
                            guiGraphics.pose().scale(scale, scale);
                            guiGraphics.renderItem(stack, 0, 0);
                            guiGraphics.pose().popMatrix();

                            if (isCellHovered) {
                                hoveredStack = stack;
                                hoveredPos = "Layer Y=" + (layerIndex + 1) + " | Row=" + (r + 1) + ", Col=" + (c + 1);
                                for (ItemStack req : recipe.requiredMaterials()) {
                                    if (ItemStack.isSameItem(req, stack)) {
                                        hoveredCount = req.getCount();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Materials & Core Labels
        guiGraphics.drawString(font, "Materials:", 104, 14, 0xFFFFD700, false);
        guiGraphics.drawString(font, "Core:", 104, 82, 0xFFFFAA00, false);

        // 5. Structure Description Footer
        guiGraphics.drawString(font, "§8" + recipe.description().getString(), 5, 124, 0xFF94A3B8, false);

        // 6. Floating Tooltip on Hover
        if (hoveredPos != null) {
            List<Component> tooltips = new ArrayList<>();
            if (hoveredStack != null && !hoveredStack.isEmpty()) {
                tooltips.add(Component.literal("§b✦ " + hoveredStack.getHoverName().getString()));
                tooltips.add(Component.literal("§ePosition: §f" + hoveredPos));
                if (hoveredCount > 0) {
                    tooltips.add(Component.literal("§7Total in Structure: §a" + hoveredCount));
                }
            } else {
                tooltips.add(Component.literal("§8Empty Space (Air)"));
                tooltips.add(Component.literal("§7Position: §f" + hoveredPos));
            }

            int maxW = 0;
            for (Component c : tooltips) {
                maxW = Math.max(maxW, font.width(c));
            }
            int boxH = tooltips.size() * 10 + 6;
            int boxX = (int) mouseX + 12;
            int boxY = (int) mouseY - 12;

            if (boxX + maxW + 8 > 184) {
                boxX = (int) mouseX - maxW - 14;
            }

            guiGraphics.fill(boxX, boxY, boxX + maxW + 8, boxY + boxH, 0xF0101624);
            guiGraphics.fill(boxX, boxY, boxX + maxW + 8, boxY + 1, 0xFF38BDF8);
            guiGraphics.fill(boxX, boxY + boxH - 1, boxX + maxW + 8, boxY + boxH, 0xFF38BDF8);
            guiGraphics.fill(boxX, boxY, boxX + 1, boxY + boxH, 0xFF38BDF8);
            guiGraphics.fill(boxX + maxW + 7, boxY, boxX + maxW + 8, boxY + boxH, 0xFF38BDF8);

            for (int t = 0; t < tooltips.size(); t++) {
                guiGraphics.drawString(font, tooltips.get(t), boxX + 4, boxY + 4 + t * 10, 0xFFFFFFFF, false);
            }
        }
    }
}
