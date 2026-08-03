package ddraig.net.entropica.client.gui.widget;

import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class MultiblockBlueprintWidget {

    public static class BlueprintLayer {
        public final int yLevel;
        public final String name;
        public final ItemStack[][] grid;

        public BlueprintLayer(int yLevel, String name, ItemStack[][] grid) {
            this.yLevel = yLevel;
            this.name = name;
            this.grid = grid;
        }
    }

    private final String multiblockName;
    private final List<BlueprintLayer> layers = new ArrayList<>();
    private int currentLayerIndex = 0;

    public MultiblockBlueprintWidget(String multiblockName) {
        this.multiblockName = multiblockName;
        setupBlueprintData(multiblockName);
    }

    private void setupBlueprintData(String name) {
        if (name.contains("scribing_engine") || name.contains("Runic")) {
            // Layer 1 (Base): 3x3 Viscanite Synthesizer / Base Blocks
            ItemStack[][] l1 = new ItemStack[3][3];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    l1[r][c] = new ItemStack(ModBlocks.AETHERIC_SYNTHESIZER.get());
                }
            }
            layers.add(new BlueprintLayer(1, "Base Layer (Synthesizers)", l1));

            // Layer 2 (Middle): Controller Front Center + Arcanite Frames
            ItemStack[][] l2 = new ItemStack[3][3];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (r == 2 && c == 1) {
                        l2[r][c] = new ItemStack(ModBlocks.ARCANE_BRICK.get()); // Scribing Controller
                    } else {
                        l2[r][c] = new ItemStack(ModBlocks.ARCANITE_PLATING.get()); // Arcanite Frame
                    }
                }
            }
            layers.add(new BlueprintLayer(2, "Middle Ring (Controller & Frame)", l2));

            // Layer 3 (Top): Viscanite Piston Press center
            ItemStack[][] l3 = new ItemStack[3][3];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (r == 1 && c == 1) {
                        l3[r][c] = new ItemStack(Items.PISTON); // Viscanite Piston Press
                    } else {
                        l3[r][c] = ItemStack.EMPTY;
                    }
                }
            }
            layers.add(new BlueprintLayer(3, "Top Layer (Piston Stamp)", l3));
        } else if (name.contains("Circle") || name.contains("Circle")) {
            // Magic Circle 3x3 Ritual Footprint
            ItemStack[][] l1 = new ItemStack[3][3];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (r == 1 && c == 1) {
                        l1[r][c] = new ItemStack(ModBlocks.MARBLE_RITUAL_BOWL.get());
                    } else {
                        l1[r][c] = new ItemStack(ModBlocks.SCRIBED_CHALK.get());
                    }
                }
            }
            layers.add(new BlueprintLayer(1, "Chalk Boundary & Focal Pedestal", l1));
        } else {
            // Materia Fission Reactor / Default 3x3 blueprint structure
            ItemStack[][] l1 = new ItemStack[3][3];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    l1[r][c] = new ItemStack(ModBlocks.ENTROPIC_CORE.get());
                }
            }
            layers.add(new BlueprintLayer(1, "Core Foundation", l1));

            ItemStack[][] l2 = new ItemStack[3][3];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (r == 1 && c == 1) {
                        l2[r][c] = new ItemStack(Items.NETHER_STAR);
                    } else {
                        l2[r][c] = new ItemStack(ModBlocks.ARCANITE_PLATING.get());
                    }
                }
            }
            layers.add(new BlueprintLayer(2, "Fission Chamber", l2));
        }
    }

    public void nextLayer() {
        if (!layers.isEmpty()) {
            currentLayerIndex = (currentLayerIndex + 1) % layers.size();
        }
    }

    public void prevLayer() {
        if (!layers.isEmpty()) {
            currentLayerIndex = (currentLayerIndex - 1 + layers.size()) % layers.size();
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int x, int y) {
        if (mouseY >= y + 18 && mouseY <= y + 32) {
            if (mouseX >= x + 8 && mouseX <= x + 40) {
                prevLayer();
                return true;
            }
            if (mouseX >= x + 115 && mouseX <= x + 152) {
                nextLayer();
                return true;
            }
        }
        return false;
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (layers.isEmpty()) return;

        BlueprintLayer layer = layers.get(currentLayerIndex);

        // Render 2D Frame Box
        guiGraphics.fill(x, y, x + 160, y + 140, 0xDD10121C);
        guiGraphics.fill(x + 1, y + 1, x + 159, y + 139, 0xFF1B1F33);

        // Header Title
        guiGraphics.drawString(Minecraft.getInstance().font, "2D Blueprint: " + multiblockName, x + 8, y + 6, 0xFF00FFCC);

        // Interactive Layer Switcher Bar: [< Prev] Layer X/Y: Name [Next >]
        boolean prevHover = mouseX >= x + 8 && mouseX <= x + 40 && mouseY >= y + 18 && mouseY <= y + 32;
        boolean nextHover = mouseX >= x + 115 && mouseX <= x + 152 && mouseY >= y + 18 && mouseY <= y + 32;

        guiGraphics.fill(x + 8, y + 18, x + 35, y + 30, prevHover ? 0xFF00D9FF : 0xFF2A2E47);
        guiGraphics.drawString(Minecraft.getInstance().font, "< Prev", x + 10, y + 20, prevHover ? 0xFF000000 : 0xFFFFFFFF);

        guiGraphics.drawString(Minecraft.getInstance().font, "L" + layer.yLevel + "/" + layers.size(), x + 44, y + 20, 0xFFFFFF00);

        guiGraphics.fill(x + 115, y + 18, x + 152, y + 30, nextHover ? 0xFF00D9FF : 0xFF2A2E47);
        guiGraphics.drawString(Minecraft.getInstance().font, "Next >", x + 117, y + 20, nextHover ? 0xFF000000 : 0xFFFFFFFF);

        // Render 3x3 Grid
        int gridStartX = x + 40;
        int gridStartY = y + 42;
        int cellSize = 26;

        for (int r = 0; r < layer.grid.length; r++) {
            for (int c = 0; c < layer.grid[r].length; c++) {
                int cx = gridStartX + c * cellSize;
                int cy = gridStartY + r * cellSize;

                guiGraphics.fill(cx, cy, cx + 24, cy + 24, 0xFF2A2E47);
                guiGraphics.fill(cx + 1, cy + 1, cx + 23, cy + 23, 0xFF141726);

                ItemStack stack = layer.grid[r][c];
                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack, cx + 4, cy + 4);

                    // Custom 2D Tooltip rendering (Bypasses mapping differences in 1.21.4)
                    if (mouseX >= cx && mouseX <= cx + 24 && mouseY >= cy && mouseY <= cy + 24) {
                        Component hoverName = stack.getHoverName();
                        int w = Minecraft.getInstance().font.width(hoverName);
                        guiGraphics.fill(mouseX + 8, mouseY - 14, mouseX + 12 + w, mouseY + 2, 0xF0100010);
                        guiGraphics.fill(mouseX + 9, mouseY - 13, mouseX + 11 + w, mouseY + 1, 0x505000FF);
                        guiGraphics.drawString(Minecraft.getInstance().font, hoverName, mouseX + 10, mouseY - 10, 0xFFFFFFFF, true);
                    }
                }
            }
        }
    }
}
