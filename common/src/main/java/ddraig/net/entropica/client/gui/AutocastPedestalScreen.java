package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity;
import ddraig.net.entropica.inventory.AutocastPedestalMenu;
import ddraig.net.entropica.network.UpdateAutocastPedestalPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Celestial configuration & inventory screen for the {@link ddraig.net.entropica.block.AutocastPedestalBlock}.
 * Exposes the 1.0s–60.0s Cast Interval slider, -90°..+90° Aim Pitch slider, Redstone control mode,
 * Linked Inventory readout/clear button, and Spellgem + Orbis Fuel slots.
 */
public class AutocastPedestalScreen extends AbstractContainerScreen<AutocastPedestalMenu> {

    private int intervalTicks;
    private float aimPitch;
    private int redstoneMode;
    private int linkedCount;

    private Button redstoneBtn;
    private Button clearLinksBtn;

    public AutocastPedestalScreen(AutocastPedestalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = 204;
        this.intervalTicks = menu.getInitialIntervalTicks();
        this.aimPitch = menu.getInitialAimPitch();
        this.redstoneMode = menu.getInitialRedstoneMode();
        this.linkedCount = menu.getInitialLinkedInventories().size();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int x = this.leftPos;
        int y = this.topPos;

        // Cast Interval Slider: 1.0s to 60.0s (step 0.5s = 10 ticks)
        BarrierConfigScreen.StepSlider timerSlider = new BarrierConfigScreen.StepSlider(
                x + 46, y + 22, 164, 18,
                "Cast Timer", "s", 1.0, 60.0, 0.5, this.intervalTicks / 20.0,
                val -> this.intervalTicks = Math.clamp((int) Math.round(val * 20.0),
                        AutocastPedestalBlockEntity.MIN_INTERVAL_TICKS,
                        AutocastPedestalBlockEntity.MAX_INTERVAL_TICKS)
        );
        this.addRenderableWidget(timerSlider);

        // Aim Pitch Slider: -90.0° to +90.0° (step 5.0°)
        BarrierConfigScreen.StepSlider pitchSlider = new BarrierConfigScreen.StepSlider(
                x + 46, y + 43, 164, 18,
                "Aim Pitch", "°", -90.0, 90.0, 5.0, this.aimPitch,
                val -> this.aimPitch = val.floatValue()
        );
        this.addRenderableWidget(pitchSlider);

        // Redstone Mode Toggle
        this.redstoneBtn = Button.builder(getRedstoneLabel(), btn -> {
            this.redstoneMode = (this.redstoneMode + 1) % 3;
            btn.setMessage(getRedstoneLabel());
        }).bounds(x + 46, y + 64, 164, 18).build();
        this.addRenderableWidget(this.redstoneBtn);

        // Clear Linked Inventories Button
        this.clearLinksBtn = Button.builder(getClearLinksLabel(), btn -> {
            this.linkedCount = 0;
            btn.setMessage(getClearLinksLabel());
            NetworkManager.sendToServer(new UpdateAutocastPedestalPayload(
                    this.menu.getPedestalPos(),
                    this.intervalTicks,
                    this.aimPitch,
                    this.redstoneMode,
                    true
            ));
        }).bounds(x + 10, y + 92, 96, 18)
                .tooltip(Tooltip.create(Component.literal("Sneak-Right-Click a Chest with any Casting Apparatus, then Sneak-Right-Click this Pedestal to link!")))
                .build();
        this.addRenderableWidget(this.clearLinksBtn);

        // Save & Apply Button
        this.addRenderableWidget(Button.builder(Component.literal("§6✦ Save & Apply ✦"), btn -> {
            NetworkManager.sendToServer(new UpdateAutocastPedestalPayload(
                    this.menu.getPedestalPos(),
                    this.intervalTicks,
                    this.aimPitch,
                    this.redstoneMode,
                    false
            ));
            this.onClose();
        }).bounds(x + 110, y + 92, 100, 18).build());
    }

    private Component getRedstoneLabel() {
        return switch (this.redstoneMode) {
            case AutocastPedestalBlockEntity.REDSTONE_PAUSE_WHEN_POWERED ->
                    Component.literal("Redstone: §ePause When Powered");
            case AutocastPedestalBlockEntity.REDSTONE_REQUIRE_POWER ->
                    Component.literal("Redstone: §aRequire Signal");
            default -> Component.literal("Redstone: §bAlways Active");
        };
    }

    private Component getClearLinksLabel() {
        return Component.literal("Links (" + this.linkedCount + "): §cClear");
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(this.font, Component.literal("§6✦ Autocast Pedestal ✦"), this.imageWidth / 2, 5, 0xFFD4AF37);
        guiGraphics.drawString(this.font, Component.literal("§bGem"), 16, 18, 0xFF38BDF8, false);
        guiGraphics.drawString(this.font, Component.literal("§dFuel"), 15, 75, 0xFFE879F9, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        g.fill(x, y, x + w, y + h, 0xFFD4AF37);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xF40B132B);
        g.fill(x + 2, y + 2, x + w - 2, y + 15, 0xFF1C2541);
        g.fill(x + 2, y + 15, x + w - 2, y + 16, 0xFF38BDF8);

        g.fill(x + 8, y + 114, x + w - 8, y + 115, 0x66D4AF37);

        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;
            int border = slot.index < 2 ? 0xFFD4AF37 : 0xFF3A4B6E;
            g.fill(sx, sy, sx + 18, sy + 18, border);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF162038);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
