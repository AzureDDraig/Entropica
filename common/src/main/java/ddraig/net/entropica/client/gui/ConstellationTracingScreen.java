package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ConstellationConnection;
import ddraig.net.entropica.astral.ConstellationStar;
import ddraig.net.entropica.astral.ModConstellations;
import ddraig.net.entropica.astral.PlayerAstralProgress;
import ddraig.net.entropica.item.CompletedStarChartItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class ConstellationTracingScreen extends Screen {

    private final List<Constellation> availableConstellations;
    private int currentConstellationIndex = 0;
    private final Set<ConstellationConnection> drawnConnections = new HashSet<>();
    private int draggingFromIndex = -1;
    private float currentDragX = 0;
    private float currentDragY = 0;
    private boolean isCompleted = false;

    // 12-pixel magnetic snap radius
    private static final float SNAP_DISTANCE = 12.0f;

    public ConstellationTracingScreen(List<Constellation> constellations, int initialIndex) {
        super(Component.literal("Celestial Star Chart"));
        this.availableConstellations = constellations.isEmpty() ? List.of(ModConstellations.VESPA_ACULEUS) : constellations;
        this.currentConstellationIndex = Math.max(0, Math.min(initialIndex, this.availableConstellations.size() - 1));
    }

    public static void openForCurrentNight(Player player) {
        if (player == null || player.level() == null) return;
        List<Constellation> visible;
        if (player.level().dimension().equals(Level.END)) {
            visible = new ArrayList<>(ModConstellations.getAllConstellations());
        } else {
            int moonPhase = player.level().getMoonPhase();
            visible = ModConstellations.getVisibleConstellations(moonPhase);
        }
        if (!visible.isEmpty()) {
            Minecraft.getInstance().setScreen(new ConstellationTracingScreen(visible, 0));
        }
    }

    private Constellation getCurrentConstellation() {
        return availableConstellations.get(currentConstellationIndex);
    }

    private void switchConstellation(int delta) {
        currentConstellationIndex = (currentConstellationIndex + delta + availableConstellations.size()) % availableConstellations.size();
        drawnConnections.clear();
        draggingFromIndex = -1;
        isCompleted = false;
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8f, 1.1f);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Constellation constellation = getCurrentConstellation();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int chartSize = 270;
        int chartX = centerX - chartSize / 2;
        int chartY = centerY - chartSize / 2;

        // 1. Render Deep Cosmic Parchment Background
        guiGraphics.fill(chartX, chartY, chartX + chartSize, chartY + chartSize, 0xF2080B16);
        // Fine Gold & Celestial Cyan Border
        guiGraphics.fill(chartX, chartY, chartX + chartSize, chartY + 2, 0xFFFFD700);
        guiGraphics.fill(chartX, chartY + chartSize - 2, chartX + chartSize, chartY + chartSize, 0xFFFFD700);
        guiGraphics.fill(chartX, chartY + 2, chartX + 2, chartY + chartSize, 0xFFFFD700);
        guiGraphics.fill(chartX + chartSize - 2, chartY, chartX + chartSize, chartY + chartSize, 0xFFFFD700);

        // Header Navigation & Title
        String title = Component.translatable(constellation.getUnlocalizedName()).getString();
        guiGraphics.drawCenteredString(this.font, "§6✦ " + title + " ✦ §7[" + constellation.getTier().getDisplayName() + "]", centerX, chartY + 10, 0xFFFFD700);
        guiGraphics.drawCenteredString(this.font, "§7Chart (" + (currentConstellationIndex + 1) + "/" + availableConstellations.size() + ")  •  Spectral Class: " + constellation.getPrimarySpectralClass().getTitle(), centerX, chartY + 22, 0xFFA0C0E0);

        // Navigation Buttons
        if (availableConstellations.size() > 1) {
            boolean prevHover = mouseX >= chartX + 8 && mouseX <= chartX + 32 && mouseY >= chartY + 8 && mouseY <= chartY + 24;
            boolean nextHover = mouseX >= chartX + chartSize - 32 && mouseX <= chartX + chartSize - 8 && mouseY >= chartY + 8 && mouseY <= chartY + 24;

            guiGraphics.fill(chartX + 8, chartY + 8, chartX + 32, chartY + 24, prevHover ? 0xFF2A3850 : 0xFF141C28);
            guiGraphics.fill(chartX + chartSize - 32, chartY + 8, chartX + chartSize - 8, chartY + 24, nextHover ? 0xFF2A3850 : 0xFF141C28);

            guiGraphics.drawCenteredString(this.font, "§e<", chartX + 20, chartY + 12, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, "§e>", chartX + chartSize - 20, chartY + 12, 0xFFFFD700);
        }

        // 2. Render Drawn Connections & Pulse Animation
        int edgeIdx = 0;
        for (ConstellationConnection conn : drawnConnections) {
            ConstellationStar s1 = constellation.getStars().get(conn.fromIndex());
            ConstellationStar s2 = constellation.getStars().get(conn.toIndex());

            float x1 = chartX + (s1.x() / 100.0f) * (chartSize - 60) + 30;
            float y1 = chartY + (s1.y() / 100.0f) * (chartSize - 80) + 40;
            float x2 = chartX + (s2.x() / 100.0f) * (chartSize - 60) + 30;
            float y2 = chartY + (s2.y() / 100.0f) * (chartSize - 80) + 40;

            drawLine(guiGraphics, (int) x1, (int) y1, (int) x2, (int) y2, isCompleted ? 0xFFFFE060 : 0xFF00F0FF);

            // Pulse bead traveling along line
            float tBead = ((System.currentTimeMillis() * 0.001f * 0.8f) + (edgeIdx * 0.3f)) % 1.0f;
            float bx = x1 * (1.0f - tBead) + x2 * tBead;
            float by = y1 * (1.0f - tBead) + y2 * tBead;
            guiGraphics.fill((int) bx - 1, (int) by - 1, (int) bx + 2, (int) by + 2, 0xFFFFFFFF);
            edgeIdx++;
        }

        // 3. Render Active Drag Line
        if (draggingFromIndex >= 0 && draggingFromIndex < constellation.getStars().size()) {
            ConstellationStar startStar = constellation.getStars().get(draggingFromIndex);
            float x1 = chartX + (startStar.x() / 100.0f) * (chartSize - 60) + 30;
            float y1 = chartY + (startStar.y() / 100.0f) * (chartSize - 80) + 40;

            // Check 12px magnetic snap
            float targetX = currentDragX;
            float targetY = currentDragY;

            for (ConstellationStar s : constellation.getStars()) {
                if (s.index() != draggingFromIndex) {
                    float sx = chartX + (s.x() / 100.0f) * (chartSize - 60) + 30;
                    float sy = chartY + (s.y() / 100.0f) * (chartSize - 80) + 40;
                    float dist = (float) Math.hypot(currentDragX - sx, currentDragY - sy);
                    if (dist <= SNAP_DISTANCE) {
                        targetX = sx;
                        targetY = sy;
                        break;
                    }
                }
            }

            drawLine(guiGraphics, (int) x1, (int) y1, (int) targetX, (int) targetY, 0xFFFFF080);
        }

        // 4. Render Star Vertices
        for (ConstellationStar star : constellation.getStars()) {
            float sx = chartX + (star.x() / 100.0f) * (chartSize - 60) + 30;
            float sy = chartY + (star.y() / 100.0f) * (chartSize - 80) + 40;

            int rgb = star.spectralClass().getColorRgb();
            int color = 0xFF000000 | rgb;

            boolean isHovered = Math.hypot(mouseX - sx, mouseY - sy) <= 8.0f;
            int starRadius = isHovered ? 5 : 3;

            // Outer glow circle
            guiGraphics.fill((int) sx - starRadius - 1, (int) sy - starRadius - 1, (int) sx + starRadius + 1, (int) sy + starRadius + 1, 0x8800E0FF);
            // Center star node
            guiGraphics.fill((int) sx - starRadius, (int) sy - starRadius, (int) sx + starRadius, (int) sy + starRadius, color);
        }

        // Footer: Ritual & Instruction
        guiGraphics.drawCenteredString(this.font, "§8" + constellation.getRitualEffect(), centerX, chartY + chartSize - 18, 0xFF88A0B0);

        // 5. Completion Banner
        if (isCompleted) {
            guiGraphics.fill(centerX - 130, centerY - 25, centerX + 130, centerY + 25, 0xEE101624);
            guiGraphics.fill(centerX - 130, centerY - 25, centerX + 130, centerY - 23, 0xFFFFD700);
            guiGraphics.fill(centerX - 130, centerY + 23, centerX + 130, centerY + 25, 0xFFFFD700);

            guiGraphics.drawCenteredString(this.font, "§6✦ CONSTELLATION CHARTED! ✦", centerX, centerY - 12, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, "§a" + title + " Scribed & Ignited in Sky", centerX, centerY + 1, 0xFF80FF80);
            guiGraphics.drawCenteredString(this.font, "§7[Press ESC to close or < / > to view others]", centerX, centerY + 12, 0xFFA0A0A0);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int chartSize = 270;
        int chartX = centerX - chartSize / 2;
        int chartY = centerY - chartSize / 2;

        // Navigation arrows click
        if (button == 0 && availableConstellations.size() > 1) {
            if (mouseX >= chartX + 8 && mouseX <= chartX + 32 && mouseY >= chartY + 8 && mouseY <= chartY + 24) {
                switchConstellation(-1);
                return true;
            }
            if (mouseX >= chartX + chartSize - 32 && mouseX <= chartX + chartSize - 8 && mouseY >= chartY + 8 && mouseY <= chartY + 24) {
                switchConstellation(1);
                return true;
            }
        }

        // Right click clears all drawn lines if not completed
        if (button == 1 && !isCompleted && !drawnConnections.isEmpty()) {
            drawnConnections.clear();
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.CHISELED_BOOKSHELF_PICKUP, 0.7f, 0.9f);
            }
            return true;
        }

        Constellation constellation = getCurrentConstellation();
        if (button == 0 && !isCompleted) {
            for (ConstellationStar star : constellation.getStars()) {
                float sx = chartX + (star.x() / 100.0f) * (chartSize - 60) + 30;
                float sy = chartY + (star.y() / 100.0f) * (chartSize - 80) + 40;
                if (Math.hypot(mouseX - sx, mouseY - sy) <= 10.0f) {
                    draggingFromIndex = star.index();
                    currentDragX = (float) mouseX;
                    currentDragY = (float) mouseY;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingFromIndex >= 0) {
            currentDragX = (float) event.x();
            currentDragY = (float) event.y();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if (button == 0 && draggingFromIndex >= 0) {
            Constellation constellation = getCurrentConstellation();
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            int chartSize = 270;
            int chartX = centerX - chartSize / 2;
            int chartY = centerY - chartSize / 2;

            int targetIndex = -1;
            for (ConstellationStar star : constellation.getStars()) {
                if (star.index() != draggingFromIndex) {
                    float sx = chartX + (star.x() / 100.0f) * (chartSize - 60) + 30;
                    float sy = chartY + (star.y() / 100.0f) * (chartSize - 80) + 40;
                    if (Math.hypot(event.x() - sx, event.y() - sy) <= SNAP_DISTANCE + 4.0f) {
                        targetIndex = star.index();
                        break;
                    }
                }
            }

            if (targetIndex >= 0) {
                for (ConstellationConnection conn : constellation.getConnections()) {
                    if ((conn.fromIndex() == draggingFromIndex && conn.toIndex() == targetIndex) ||
                        (conn.fromIndex() == targetIndex && conn.toIndex() == draggingFromIndex)) {
                        drawnConnections.add(conn);
                        if (minecraft != null && minecraft.player != null) {
                            minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
                        }
                        break;
                    }
                }

                // Check completion
                if (drawnConnections.size() >= constellation.getConnections().size()) {
                    isCompleted = true;
                    Player player = minecraft != null ? minecraft.player : null;
                    if (player != null) {
                        player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
                        PlayerAstralProgress.discover(player, constellation);

                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (stack.is(ModItems.STAR_CHART_BLANK.get())) {
                                stack.shrink(1);
                                ItemStack completedChart = CompletedStarChartItem.createFor(ModItems.STAR_CHART_COMPLETED.get(), constellation);
                                if (!player.getInventory().add(completedChart)) {
                                    player.drop(completedChart, false);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            draggingFromIndex = -1;
            return true;
        }
        return super.mouseReleased(event);
    }

    private void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            guiGraphics.fill(x1 - 1, y1 - 1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
