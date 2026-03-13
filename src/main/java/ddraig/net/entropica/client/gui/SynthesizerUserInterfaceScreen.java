package ddraig.net.entropica.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import ddraig.net.entropica.inventory.SynthesizerUserInterfaceMenu;
import ddraig.net.entropica.network.TerminalCraftPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public class SynthesizerUserInterfaceScreen extends AbstractContainerScreen<SynthesizerUserInterfaceMenu> {

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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
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
    }
}