package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.astral.CelestialStarHelper;
import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ConstellationConnection;
import ddraig.net.entropica.astral.ConstellationStar;
import ddraig.net.entropica.astral.ModConstellations;
import ddraig.net.entropica.astral.PlayerAstralProgress;
import ddraig.net.entropica.network.ConstellationDiscoveryPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
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
    private ConstellationConnection hoveredConnection = null;

    // 12-pixel magnetic snap radius
    private static final float SNAP_DISTANCE = 12.0f;

    public ConstellationTracingScreen(List<Constellation> constellations, int initialIndex) {
        super(Component.literal("Celestial Star Chart"));
        this.availableConstellations = constellations.isEmpty() ? List.of(ModConstellations.VESPA_ACULEUS) : constellations;
        this.currentConstellationIndex = Math.max(0, Math.min(initialIndex, this.availableConstellations.size() - 1));
    }

    @Override
    protected void init() {
        super.init();
        loadConstellationState();
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
            float yaw = Mth.wrapDegrees(player.getYRot());
            if (yaw < 0) yaw += 360f;
            int targetIdx = (int) (yaw / (360.0f / Math.max(1, visible.size()))) % visible.size();
            Minecraft.getInstance().setScreen(new ConstellationTracingScreen(visible, targetIdx));
        }
    }

    private Constellation getCurrentConstellation() {
        return availableConstellations.get(currentConstellationIndex);
    }

    private void loadConstellationState() {
        drawnConnections.clear();
        draggingFromIndex = -1;
        hoveredConnection = null;
        Constellation constellation = getCurrentConstellation();
        Player player = minecraft != null ? minecraft.player : null;
        if (player != null && PlayerAstralProgress.isDiscovered(player, constellation)) {
            drawnConnections.addAll(constellation.getConnections());
            isCompleted = true;
        } else {
            isCompleted = false;
        }
    }

    private void switchConstellation(int delta) {
        currentConstellationIndex = (currentConstellationIndex + delta + availableConstellations.size()) % availableConstellations.size();
        loadConstellationState();
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8f, 1.1f);
        }
    }

    public static float pointToSegmentDistance(float px, float py, float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float lenSq = dx * dx + dy * dy;
        if (lenSq < 1e-4f) {
            return (float) Math.hypot(px - x1, py - y1);
        }
        float t = ((px - x1) * dx + (py - y1) * dy) / lenSq;
        t = Mth.clamp(t, 0.0f, 1.0f);
        float projX = x1 + t * dx;
        float projY = y1 + t * dy;
        return (float) Math.hypot(px - projX, py - projY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Constellation constellation = getCurrentConstellation();
        Player player = minecraft != null ? minecraft.player : null;
        boolean isDiscovered = player != null && PlayerAstralProgress.isDiscovered(player, constellation);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int chartSize = 270;
        int chartX = centerX - chartSize / 2;
        int chartY = centerY - chartSize / 2;

        this.hoveredConnection = null;

        // 1. Render Deep Cosmic Parchment Background
        guiGraphics.fill(chartX, chartY, chartX + chartSize, chartY + chartSize, 0xF2080B16);
        // Fine Gold & Celestial Border
        int borderCol = (isDiscovered || isCompleted) ? 0xFFFFD700 : 0xFF00E0FF;
        guiGraphics.fill(chartX, chartY, chartX + chartSize, chartY + 2, borderCol);
        guiGraphics.fill(chartX, chartY + chartSize - 2, chartX + chartSize, chartY + chartSize, borderCol);
        guiGraphics.fill(chartX, chartY + 2, chartX + 2, chartY + chartSize, borderCol);
        guiGraphics.fill(chartX + chartSize - 2, chartY, chartX + chartSize, chartY + chartSize, borderCol);

        // Header Navigation & Title
        String rawTitle = Component.translatable(constellation.getUnlocalizedName()).getString();
        String displayTitle = (isDiscovered || isCompleted) ? rawTitle : "Uncharted Astral Signature";
        int essRgb = (constellation.getEssenceType().getR() << 16) | (constellation.getEssenceType().getG() << 8) | constellation.getEssenceType().getB();
        String essColorCode = constellation.getEssenceType().getColorCode();
        String essFormatted = constellation.getEssenceType().getFormattedName();

        if (isDiscovered || isCompleted) {
            guiGraphics.drawCenteredString(this.font, essColorCode + "✦ " + displayTitle + " ✦ §7[" + constellation.getTier().getDisplayName() + " • " + essFormatted + "§7]", centerX, chartY + 10, 0xFFFFFFFF);
            guiGraphics.drawCenteredString(this.font, "§7Chart (" + (currentConstellationIndex + 1) + "/" + availableConstellations.size() + ")  •  §bDiscovered §7•  Spectral Class: " + constellation.getPrimarySpectralClass().getTitle(), centerX, chartY + 22, 0xFFA0C0E0);
        } else {
            guiGraphics.drawCenteredString(this.font, "§b✦ " + displayTitle + " ✦ §8[" + constellation.getTier().getDisplayName() + " • " + essFormatted + "§8]", centerX, chartY + 10, 0xFFA0D8EF);
            guiGraphics.drawCenteredString(this.font, "§7Chart (" + (currentConstellationIndex + 1) + "/" + availableConstellations.size() + ")  •  §eUncharted §7•  Spectral Class: " + constellation.getPrimarySpectralClass().getTitle(), centerX, chartY + 22, 0xFFA0C0E0);
        }

        // Navigation Buttons
        if (availableConstellations.size() > 1) {
            boolean prevHover = mouseX >= chartX + 8 && mouseX <= chartX + 32 && mouseY >= chartY + 8 && mouseY <= chartY + 24;
            boolean nextHover = mouseX >= chartX + chartSize - 32 && mouseX <= chartX + chartSize - 8 && mouseY >= chartY + 8 && mouseY <= chartY + 24;

            guiGraphics.fill(chartX + 8, chartY + 8, chartX + 32, chartY + 24, prevHover ? 0xFF2A3850 : 0xFF141C28);
            guiGraphics.fill(chartX + chartSize - 32, chartY + 8, chartX + chartSize - 8, chartY + 24, nextHover ? 0xFF2A3850 : 0xFF141C28);

            guiGraphics.drawCenteredString(this.font, "§e<", chartX + 20, chartY + 12, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, "§e>", chartX + chartSize - 20, chartY + 12, 0xFFFFD700);
        }

        // Find closest drawn connection to cursor for targeted highlighting & erasing
        float minLineDist = 8.0f;
        for (ConstellationConnection conn : drawnConnections) {
            if (conn.fromIndex() < constellation.getStars().size() && conn.toIndex() < constellation.getStars().size()) {
                ConstellationStar s1 = constellation.getStars().get(conn.fromIndex());
                ConstellationStar s2 = constellation.getStars().get(conn.toIndex());
                float x1 = chartX + (s1.x() / 100.0f) * (chartSize - 60) + 30;
                float y1 = chartY + (s1.y() / 100.0f) * (chartSize - 80) + 40;
                float x2 = chartX + (s2.x() / 100.0f) * (chartSize - 60) + 30;
                float y2 = chartY + (s2.y() / 100.0f) * (chartSize - 80) + 40;
                float d = pointToSegmentDistance((float) mouseX, (float) mouseY, x1, y1, x2, y2);
                if (d < minLineDist) {
                    minLineDist = d;
                    this.hoveredConnection = conn;
                }
            }
        }

        // 2. Render Drawn Connections & Pulse Animation
        int edgeIdx = 0;
        for (ConstellationConnection conn : drawnConnections) {
            if (conn.fromIndex() < constellation.getStars().size() && conn.toIndex() < constellation.getStars().size()) {
                ConstellationStar s1 = constellation.getStars().get(conn.fromIndex());
                ConstellationStar s2 = constellation.getStars().get(conn.toIndex());

                float x1 = chartX + (s1.x() / 100.0f) * (chartSize - 60) + 30;
                float y1 = chartY + (s1.y() / 100.0f) * (chartSize - 80) + 40;
                float x2 = chartX + (s2.x() / 100.0f) * (chartSize - 60) + 30;
                float y2 = chartY + (s2.y() / 100.0f) * (chartSize - 80) + 40;

                boolean isHighlighted = conn.equals(this.hoveredConnection);
                int lineColor = isHighlighted ? 0xFFFF3333 : ((isDiscovered || isCompleted) ? (0xFF000000 | essRgb) : 0xFF00F0FF);
                drawLine(guiGraphics, (int) x1, (int) y1, (int) x2, (int) y2, lineColor);

                if (isHighlighted) {
                    drawLine(guiGraphics, (int) x1, (int) y1 - 1, (int) x2, (int) y2 - 1, 0x88FFAAAA);
                    drawLine(guiGraphics, (int) x1, (int) y1 + 1, (int) x2, (int) y2 + 1, 0x88FFAAAA);
                }

                // Pulse bead traveling along line
                float tBead = ((System.currentTimeMillis() * 0.001f * 0.8f) + (edgeIdx * 0.3f)) % 1.0f;
                float bx = x1 * (1.0f - tBead) + x2 * tBead;
                float by = y1 * (1.0f - tBead) + y2 * tBead;
                guiGraphics.fill((int) bx - 1, (int) by - 1, (int) bx + 2, (int) by + 2, 0xFF000000 | essRgb);
                edgeIdx++;
            }
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

            drawLine(guiGraphics, (int) x1, (int) y1, (int) targetX, (int) targetY, 0xFF000000 | essRgb);
        }

        // 4. Render Star Vertices
        ConstellationStar hoveredStar = null;
        int hoveredStarIdx = -1;
        for (int s = 0; s < constellation.getStars().size(); s++) {
            ConstellationStar star = constellation.getStars().get(s);
            float sx = chartX + (star.x() / 100.0f) * (chartSize - 60) + 30;
            float sy = chartY + (star.y() / 100.0f) * (chartSize - 80) + 40;

            int rgb = star.spectralClass().getColorRgb();
            int color = 0xFF000000 | rgb;

            boolean isHovered = Math.hypot(mouseX - sx, mouseY - sy) <= 8.0f;
            if (isHovered) {
                hoveredStar = star;
                hoveredStarIdx = s;
            }
            int starRadius = isHovered ? 5 : 3;

            // Outer glow circle tinted by essence
            guiGraphics.fill((int) sx - starRadius - 1, (int) sy - starRadius - 1, (int) sx + starRadius + 1, (int) sy + starRadius + 1, (0x88 << 24) | essRgb);
            // Center star node
            guiGraphics.fill((int) sx - starRadius, (int) sy - starRadius, (int) sx + starRadius, (int) sy + starRadius, color);
        }

        // Subtitle / Hover Readout
        if (hoveredStar != null) {
            String sName = CelestialStarHelper.getConstellationStarName(constellation, hoveredStarIdx);
            guiGraphics.drawCenteredString(this.font, "§b✦ Star: §f" + sName + "  §e| Class: §7" + hoveredStar.spectralClass().name() + "  §e| Mag: §f" + hoveredStar.brightness() + "m", centerX, chartY + 18, 0xFFE0F0FF);
        }

        // Footer: Ritual & Instruction
        if (isDiscovered || isCompleted) {
            guiGraphics.drawCenteredString(this.font, "§aRitual: §7" + constellation.getRitualEffect(), centerX, chartY + chartSize - 18, 0xFFA0E0A0);
        } else if (this.hoveredConnection != null) {
            guiGraphics.drawCenteredString(this.font, "§c✦ LINE SELECTED: Right-Click to Erase ✦", centerX, chartY + chartSize - 18, 0xFFFF6666);
        } else {
            guiGraphics.drawCenteredString(this.font, "§8Click & drag between stars to map  •  Hover line + Right-Click to erase", centerX, chartY + chartSize - 18, 0xFF88A0B0);
        }

        // 5. Completion / Discovery Banner
        if (isCompleted) {
            guiGraphics.fill(centerX - 140, centerY - 30, centerX + 140, centerY + 30, 0xF5101624);
            guiGraphics.fill(centerX - 140, centerY - 30, centerX + 140, centerY - 28, 0xFF000000 | essRgb);
            guiGraphics.fill(centerX - 140, centerY + 28, centerX + 140, centerY + 30, 0xFF000000 | essRgb);

            guiGraphics.drawCenteredString(this.font, "§6✦ CONSTELLATION CHARTED! ✦", centerX, centerY - 16, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, essColorCode + rawTitle + " §7Scribed & Ignited in the Heavens", centerX, centerY - 3, 0xFFFFFFFF);
            guiGraphics.drawCenteredString(this.font, "§7[Press ESC to close or < / > to view other stars]", centerX, centerY + 12, 0xFFA0A0A0);
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

        // Targeted Right-Click line erase: ONLY erases the selected / hovered line!
        if (button == 1 && !isCompleted && this.hoveredConnection != null) {
            drawnConnections.remove(this.hoveredConnection);
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.CHISELED_BOOKSHELF_PICKUP, 0.7f, 1.2f);
            }
            this.hoveredConnection = null;
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
                        player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2f, 1.4f);
                        player.playSound(SoundEvents.BEACON_ACTIVATE, 0.8f, 1.2f);

                        PlayerAstralProgress.discover(player, constellation);
                        NetworkManager.sendToServer(new ConstellationDiscoveryPayload(constellation.getId()));
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
