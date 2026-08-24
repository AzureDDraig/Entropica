package ddraig.net.entropica.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import ddraig.net.entropica.inventory.SynthesizerUserInterfaceMenu;
import ddraig.net.entropica.network.TerminalCraftPayload;
import ddraig.net.entropica.recipe.AethericSynthesizerRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class SynthesizerUserInterfaceScreen extends AbstractContainerScreen<SynthesizerUserInterfaceMenu> {

    // Tracks the grid state so we only scan recipes when an item is added/moved
    private final NonNullList<ItemStack> lastGridState = NonNullList.withSize(25, ItemStack.EMPTY);
    private ItemStack currentPreview = ItemStack.EMPTY;

    public SynthesizerUserInterfaceScreen(SynthesizerUserInterfaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 212;
        this.imageHeight = 204;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 26;
    }

    @Override
    protected void init() {
        super.init();

        int buttonX = this.leftPos + 106;
        int buttonY = this.topPos + 53;

        this.addRenderableWidget(Button.builder(Component.literal("Craft"), button -> {
            Window window = Minecraft.getInstance().getWindow();
            boolean bulk = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                    InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);

            if (this.minecraft != null && this.minecraft.getConnection() != null) {
                this.minecraft.getConnection().send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                        new TerminalCraftPayload(this.menu.getSynthesizerPos(), bulk)
                ));
            }
        }).bounds(buttonX, buttonY, 32, 20).build());
    }

    private void updatePreview() {
        boolean changed = false;
        SimpleContainer grid = new SimpleContainer(25);

        for (int i = 0; i < 25; i++) {
            ItemStack stack = this.menu.slots.get(i).getItem();
            grid.setItem(i, stack);

            // Check if the item type, data components, or count has changed since last frame
            if (!ItemStack.isSameItemSameComponents(stack, lastGridState.get(i)) || stack.getCount() != lastGridState.get(i).getCount()) {
                changed = true;
                lastGridState.set(i, stack.copy());
            }
        }

        // Only hit the recipe manager if the grid actually changed
        if (!changed) return;

        currentPreview = ItemStack.EMPTY;

        // Check Hardcoded Fallback Recipes
        // By bypassing the client RecipeManager entirely, we avoid all 1.21.4 mapping issues.
        // Because our hardcoded list mirrors the JSONs, the preview functions flawlessly!
        for (AethericSynthesizerRecipe recipe : ddraig.net.entropica.recipe.HardcodedRecipes.getSynthesizerRecipes()) {
            if (recipe.matchesGrid(grid)) {
                if (!recipe.getResults().isEmpty()) {
                    currentPreview = recipe.getResults().get(0);
                }
                return;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updatePreview(); // Poll for grid changes before drawing!

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Draw a custom tooltip if the player hovers their mouse over the ghost item!
        if (!currentPreview.isEmpty()) {
            int previewX = this.leftPos + 106 + 8;
            int previewY = this.topPos + 53 - 22;
            if (mouseX >= previewX && mouseX < previewX + 16 && mouseY >= previewY && mouseY < previewY + 16) {

                Component text = Component.literal("§bPreview: ").append(currentPreview.getHoverName());
                int width = this.font.width(text);

                // Manually drawing the tooltip box entirely bypasses the strict 1.21.4 renderTooltip requirements!
                // In 1.21.4, UI rendering is strictly 2D, so draw-order naturally handles overlapping.
                guiGraphics.fill(mouseX + 8, mouseY - 14, mouseX + 12 + width, mouseY + 2, 0xF0100010);
                guiGraphics.fill(mouseX + 9, mouseY - 13, mouseX + 11 + width, mouseY + 1, 0x505000FF);

                // FIXED: Used 0xFFFFFFFF (8 hex characters) to ensure the Alpha channel is fully opaque!
                guiGraphics.drawString(this.font, text, mouseX + 10, mouseY - 10, 0xFFFFFFFF, true);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // 1. Draw the main panel background (Standard Vanilla Gray)
        guiGraphics.fill(x, y, x + w, y + h, 0xFFC6C6C6);

        // 2. Draw the 3D vanilla bevel borders
        guiGraphics.fill(x, y, x + w, y + 1, 0xFFFFFFFF);         // Top White
        guiGraphics.fill(x, y, x + 1, y + h, 0xFFFFFFFF);         // Left White
        guiGraphics.fill(x, y + h - 1, x + w, y + h, 0xFF555555); // Bottom Dark Gray
        guiGraphics.fill(x + w - 1, y, x + w, y + h, 0xFF555555); // Right Dark Gray

        // 3. AUTO-GENERATE SLOTS (Procedural drawing!)
        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;

            // Dark shadow border (Top and Left)
            guiGraphics.fill(sx, sy, sx + 17, sy + 1, 0xFF373737);
            guiGraphics.fill(sx, sy, sx + 1, sy + 17, 0xFF373737);

            // White highlight border (Bottom and Right)
            guiGraphics.fill(sx + 1, sy + 17, sx + 18, sy + 18, 0xFFFFFFFF);
            guiGraphics.fill(sx + 17, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);

            // Inner slot background (Light Gray)
            guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }

        // 4. RENDER PREVIEW GHOST ITEM
        if (!currentPreview.isEmpty()) {
            // Centered perfectly above the 32px wide Craft button
            int previewX = x + 106 + 8;
            int previewY = y + 53 - 22;

            // Render the physical item
            guiGraphics.renderItem(currentPreview, previewX, previewY);

            // Draw a 50% opacity grey box perfectly over the top of the item.
            // This washes out the colors and simulates a "hologram / uncrafted ghost" style!
            guiGraphics.fill(previewX, previewY, previewX + 16, previewY + 16, 0x88C6C6C6);
        }
    }
}