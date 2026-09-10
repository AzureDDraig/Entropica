package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import ddraig.net.entropica.forcefield.BarrierFilterMode;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.inventory.barrier.BarrierConfigMenu;
import ddraig.net.entropica.network.UpdateBarrierConfigPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Celestial interactive creator configuration GUI for Firmament Forcefield Barriers.
 * Renders an obsidian & Astral Brass UI for adjusting shapes, filters, dimensions,
 * bounce elasticity, one-way directional valves, redstone modes, and player whitelists.
 */
public class BarrierConfigScreen extends AbstractContainerScreen<BarrierConfigMenu> {

    private BarrierShape shape;
    private BarrierFilterMode filterMode;
    private ApexPredatorTheme theme;
    private float barrierWidth;
    private float barrierHeight;
    private float barrierRadius;
    private float elasticity;
    private boolean oneWay;
    private int redstoneMode;
    private int colorTint;
    private final List<String> whitelist = new ArrayList<>();

    private EditBox whitelistInput;
    private int selectedWhitelistIndex = -1;
    private Button oneWayButton;
    private Button redstoneButton;

    public BarrierConfigScreen(BarrierConfigMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 240;
        this.imageHeight = 220;

        // Initialize mutable state from menu/barrier
        this.shape = menu.getShape();
        this.filterMode = menu.getFilterMode();
        this.theme = menu.getTheme();
        this.barrierWidth = menu.getWidth();
        this.barrierHeight = menu.getHeight();
        this.barrierRadius = menu.getRadius();
        this.elasticity = menu.getElasticity();
        this.oneWay = menu.isOneWay();
        this.redstoneMode = menu.getRedstoneMode();
        this.colorTint = menu.getColorTintRaw();
        this.whitelist.addAll(menu.getWhitelistUsernames());
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // --- Row 2: Shape & Filter Mode CycleButtons ---
        CycleButton<BarrierShape> shapeButton = CycleButton.<BarrierShape>builder(s -> Component.literal(s.getDisplayName()))
                .withValues(BarrierShape.values())
                .withInitialValue(this.shape)
                .create(this.leftPos + 10, this.topPos + 20, 108, 20, Component.literal("Shape"), (btn, val) -> this.shape = val);
        this.addRenderableWidget(shapeButton);

        CycleButton<BarrierFilterMode> filterButton = CycleButton.<BarrierFilterMode>builder(f -> Component.literal(f.getDisplayName()))
                .withValues(BarrierFilterMode.values())
                .withInitialValue(this.filterMode)
                .create(this.leftPos + 122, this.topPos + 20, 108, 20, Component.literal("Filter"), (btn, val) -> this.filterMode = val);
        this.addRenderableWidget(filterButton);

        // --- Row 3: Width & Height Stepped Sliders (1.0m to 32.0m, step 0.5m) ---
        StepSlider widthSlider = new StepSlider(
                this.leftPos + 10, this.topPos + 44, 108, 20,
                "Width", "m", 1.0, 32.0, 0.5, this.barrierWidth,
                val -> this.barrierWidth = val.floatValue()
        );
        this.addRenderableWidget(widthSlider);

        StepSlider heightSlider = new StepSlider(
                this.leftPos + 122, this.topPos + 44, 108, 20,
                "Height", "m", 1.0, 32.0, 0.5, this.barrierHeight,
                val -> this.barrierHeight = val.floatValue()
        );
        this.addRenderableWidget(heightSlider);

        // --- Row 4: Radius & Elasticity Stepped Sliders ---
        StepSlider radiusSlider = new StepSlider(
                this.leftPos + 10, this.topPos + 68, 108, 20,
                "Radius", "m", 1.0, 32.0, 0.5, this.barrierRadius,
                val -> this.barrierRadius = val.floatValue()
        );
        this.addRenderableWidget(radiusSlider);

        StepSlider elasticitySlider = new StepSlider(
                this.leftPos + 122, this.topPos + 68, 108, 20,
                "Bounce", "x", 0.20, 2.00, 0.05, this.elasticity,
                val -> this.elasticity = val.floatValue()
        );
        this.addRenderableWidget(elasticitySlider);

        // --- Row 5: One-Way Valve Toggle & Redstone Mode Button ---
        this.oneWayButton = Button.builder(getOneWayComponent(), btn -> {
            this.oneWay = !this.oneWay;
            btn.setMessage(getOneWayComponent());
        }).bounds(this.leftPos + 10, this.topPos + 92, 108, 20).build();
        this.addRenderableWidget(this.oneWayButton);

        this.redstoneButton = Button.builder(getRedstoneComponent(), btn -> {
            this.redstoneMode = (this.redstoneMode + 1) % 3;
            btn.setMessage(getRedstoneComponent());
        }).bounds(this.leftPos + 122, this.topPos + 92, 108, 20).build();
        this.addRenderableWidget(this.redstoneButton);

        // --- Row 6: Whitelist Input & Add / Del Buttons ---
        this.whitelistInput = new EditBox(this.font, this.leftPos + 10, this.topPos + 126, 138, 18, Component.literal("Username"));
        this.whitelistInput.setMaxLength(16);
        this.whitelistInput.setHint(Component.literal("Enter player name..."));
        this.addRenderableWidget(this.whitelistInput);

        Button addButton = Button.builder(Component.literal("+ Add"), btn -> {
            String name = this.whitelistInput.getValue().trim();
            if (!name.isEmpty() && !this.whitelist.contains(name) && this.whitelist.size() < 32) {
                this.whitelist.add(name);
                this.whitelistInput.setValue("");
                this.selectedWhitelistIndex = -1;
            }
        }).bounds(this.leftPos + 152, this.topPos + 125, 36, 20).build();
        this.addRenderableWidget(addButton);

        Button delButton = Button.builder(Component.literal("- Del"), btn -> {
            if (this.selectedWhitelistIndex >= 0 && this.selectedWhitelistIndex < this.whitelist.size()) {
                this.whitelist.remove(this.selectedWhitelistIndex);
                this.selectedWhitelistIndex = -1;
            } else {
                String name = this.whitelistInput.getValue().trim();
                if (!name.isEmpty()) {
                    this.whitelist.remove(name);
                    this.whitelistInput.setValue("");
                }
            }
        }).bounds(this.leftPos + 192, this.topPos + 125, 38, 20).build();
        this.addRenderableWidget(delButton);

        // --- Row 8: Save & Apply Button ---
        Button saveButton = Button.builder(Component.literal("§6✦ Save & Apply Configuration ✦"), btn -> {
            applyAndSend();
            this.onClose();
        }).bounds(this.leftPos + 10, this.topPos + 194, 220, 20).build();
        this.addRenderableWidget(saveButton);
    }

    private Component getOneWayComponent() {
        return Component.literal(this.oneWay ? "Flow: §eOne-Way" : "Flow: §bTwo-Way");
    }

    private Component getRedstoneComponent() {
        return switch (this.redstoneMode) {
            case 1 -> Component.literal("Redstone: §eInverted");
            case 2 -> Component.literal("Redstone: §aPowered");
            default -> Component.literal("Redstone: §bAlways On");
        };
    }

    private void applyAndSend() {
        UpdateBarrierConfigPayload payload = new UpdateBarrierConfigPayload(
                this.menu.getBarrierEntityId(),
                this.shape.ordinal(),
                this.barrierWidth,
                this.barrierHeight,
                this.barrierRadius,
                this.filterMode.ordinal(),
                this.theme.ordinal(),
                this.elasticity,
                this.oneWay,
                this.redstoneMode,
                this.colorTint,
                new ArrayList<>(this.whitelist)
        );
        NetworkManager.sendToServer(payload);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.whitelistInput != null && this.whitelistInput.isFocused()) {
                String name = this.whitelistInput.getValue().trim();
                if (!name.isEmpty() && !this.whitelist.contains(name) && this.whitelist.size() < 32) {
                    this.whitelist.add(name);
                    this.whitelistInput.setValue("");
                    this.selectedWhitelistIndex = -1;
                    return true;
                }
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            int panelX = this.leftPos + 10;
            int panelY = this.topPos + 148;
            int panelW = 220;
            int panelH = 40;

            if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
                int relY = (int) mouseY - panelY;
                int clickedRow = relY / 12;
                int relX = (int) mouseX - panelX;
                int clickedCol = Math.min(2, relX / 73);
                int index = clickedRow * 3 + clickedCol;
                if (index >= 0 && index < this.whitelist.size()) {
                    this.selectedWhitelistIndex = index;
                    this.whitelistInput.setValue(this.whitelist.get(index));
                    return true;
                } else {
                    this.selectedWhitelistIndex = -1;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Astral brass centered title
        guiGraphics.drawCenteredString(this.font, Component.literal("§6✦ Firmament Configuration ✦"), this.imageWidth / 2, 6, 0xFFD4AF37);
        // Whitelist section label
        guiGraphics.drawString(this.font, Component.literal("§bWhitelisted Players:"), 10, 116, 0xFF38BDF8, false);
        // Suppress super.renderLabels() so vanilla player inventory label is omitted
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // 1. Astral Brass outer border
        guiGraphics.fill(x, y, x + w, y + h, 0xFFD4AF37);

        // 2. Celestial dark navy background
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xF40B132B);

        // 3. Header separator
        guiGraphics.fill(x + 2, y + 2, x + w - 2, y + 17, 0xFF1C2541);
        guiGraphics.fill(x + 2, y + 17, x + w - 2, y + 18, 0xFF38BDF8);

        // 4. Starlight runic corner notches
        guiGraphics.fill(x + 3, y + 3, x + 7, y + 5, 0xFF38BDF8);
        guiGraphics.fill(x + 3, y + 3, x + 5, y + 7, 0xFF38BDF8);
        guiGraphics.fill(x + w - 7, y + 3, x + w - 3, y + 5, 0xFF38BDF8);
        guiGraphics.fill(x + w - 5, y + 3, x + w - 3, y + 7, 0xFF38BDF8);

        // 5. Whitelist box area outline
        int wlX = x + 10;
        int wlY = y + 148;
        int wlW = 220;
        int wlH = 40;
        guiGraphics.fill(wlX, wlY, wlX + wlW, wlY + wlH, 0x880B132B);
        drawOutline(guiGraphics, wlX, wlY, wlW, wlH, 0xFF38BDF8);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render entries within whitelist box
        renderWhitelistEntries(guiGraphics);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderWhitelistEntries(GuiGraphics guiGraphics) {
        int panelX = this.leftPos + 10;
        int panelY = this.topPos + 148;

        if (this.whitelist.isEmpty()) {
            guiGraphics.drawString(this.font, Component.literal("§7(No entries. All entities filtered by mode)"), panelX + 8, panelY + 16, 0x88AAAAAA, false);
            return;
        }

        int colW = 72;
        int rowH = 12;

        for (int i = 0; i < Math.min(9, this.whitelist.size()); i++) {
            int row = i / 3;
            int col = i % 3;
            int entryX = panelX + 4 + col * colW;
            int entryY = panelY + 3 + row * rowH;

            String name = this.whitelist.get(i);
            if (i == this.selectedWhitelistIndex) {
                guiGraphics.fill(entryX - 2, entryY - 1, entryX + colW - 4, entryY + 10, 0x6638BDF8);
                drawOutline(guiGraphics, entryX - 2, entryY - 1, colW - 2, 11, 0xFFD4AF37);
            }

            String display = name.length() > 10 ? name.substring(0, 9) + "…" : name;
            guiGraphics.drawString(this.font, Component.literal("§f" + display), entryX, entryY, 0xFFFFFFFF, false);
        }
    }

    /**
     * Stepped slider extending AbstractSliderButton with clamped step rounding.
     */
    public static class StepSlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        private final double step;
        private final String prefix;
        private final String suffix;
        private final Consumer<Double> onChange;

        public StepSlider(int x, int y, int width, int height, String prefix, String suffix,
                          double min, double max, double step, double initialValue,
                          Consumer<Double> onChange) {
            super(x, y, width, height, CommonComponents.EMPTY, toNormalized(initialValue, min, max));
            this.min = min;
            this.max = max;
            this.step = step;
            this.prefix = prefix;
            this.suffix = suffix;
            this.onChange = onChange;
            updateMessage();
        }

        private static double toNormalized(double val, double min, double max) {
            return Math.max(0.0, Math.min(1.0, (val - min) / (max - min)));
        }

        public double getActualValue() {
            double raw = min + (max - min) * this.value;
            double stepped = Math.round((raw - min) / step) * step + min;
            return Math.max(min, Math.min(max, stepped));
        }

        @Override
        protected void updateMessage() {
            double val = getActualValue();
            if (step >= 0.1) {
                this.setMessage(Component.literal(String.format(Locale.ROOT, "%s: %.1f%s", prefix, val, suffix)));
            } else {
                this.setMessage(Component.literal(String.format(Locale.ROOT, "%s: %.2f%s", prefix, val, suffix)));
            }
        }

        @Override
        protected void applyValue() {
            double val = getActualValue();
            this.value = toNormalized(val, min, max);
            if (this.onChange != null) {
                this.onChange.accept(val);
            }
        }
    }
}
