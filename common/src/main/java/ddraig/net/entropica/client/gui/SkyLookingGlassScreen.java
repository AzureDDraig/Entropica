package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.astral.*;
import ddraig.net.entropica.item.CompletedStarChartItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class SkyLookingGlassScreen extends Screen {

    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/looking_glass_overlay.png");

    // Camera view angles (Degrees)
    private float yaw = 0.0f;
    private float pitch = 30.0f;

    // Field of view in telescope lens (Degrees)
    private static final float FOV = 42.0f;

    // Constellations active tonight
    private final List<Constellation> visibleConstellations;

    // Map of drawn connections per constellation
    private final Map<Constellation, Set<ConstellationConnection>> drawnLines = new HashMap<>();

    // Active dragging state
    private Constellation dragConstellation = null;
    private int dragStarIndex = -1;
    private float currentMouseX = 0;
    private float currentMouseY = 0;

    // Background ambient star seed points (theta, phi, brightness)
    private final List<float[]> ambientStars = new ArrayList<>();

    // Last discovered banner notification
    private Constellation justDiscovered = null;
    private long discoveryTime = 0;

    public SkyLookingGlassScreen() {
        super(Component.literal("Celestial Looking Glass"));

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            this.yaw = Mth.wrapDegrees(mc.player.getYRot());
            if (this.yaw < 0) this.yaw += 360.0f;
            this.pitch = Mth.clamp(-mc.player.getXRot(), -10.0f, 85.0f);
        }

        Level level = mc.level;
        if (level != null) {
            if (level.dimension().equals(Level.END)) {
                this.visibleConstellations = new ArrayList<>(ModConstellations.getAllConstellations());
            } else {
                int moonPhase = level.getMoonPhase();
                this.visibleConstellations = ModConstellations.getVisibleConstellations(moonPhase);
            }
        } else {
            this.visibleConstellations = List.of(ModConstellations.VESPA_ACULEUS);
        }

        // Generate 160 deterministic ambient background stars across the celestial sphere
        Random rng = new Random(133742L);
        for (int i = 0; i < 160; i++) {
            float sTheta = rng.nextFloat() * 360.0f;
            float sPhi = -5.0f + rng.nextFloat() * 95.0f; // -5 to 90 degrees elevation
            float sBright = 0.3f + rng.nextFloat() * 0.7f;
            ambientStars.add(new float[]{sTheta, sPhi, sBright});
        }
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new SkyLookingGlassScreen());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isShiftDown() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isShiftKeyDown()) return true;
        return mc.options != null && mc.options.keyShift != null && mc.options.keyShift.isDown();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.currentMouseX = (float) mouseX;
        this.currentMouseY = (float) mouseY;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        this.currentMouseX = (float) event.x();
        this.currentMouseY = (float) event.y();

        // If shift is NOT held, dragging pans the telescope camera!
        if (!isShiftDown() && dragStarIndex < 0) {
            this.yaw = (this.yaw - (float) dragX * 0.18f + 360.0f) % 360.0f;
            this.pitch = Mth.clamp(this.pitch + (float) dragY * 0.18f, -10.0f, 85.0f);
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;

        int screenWidth = this.width;
        int screenHeight = this.height;

        int size = Math.min(screenWidth, screenHeight);
        int lensX = (screenWidth - size) / 2;
        int lensY = (screenHeight - size) / 2;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        float lensRadius = size * 0.42f;

        // 1. Fill deep cosmic space background
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF050811);

        // 2. Render Ambient Background Stars inside telescope aperture
        long gameTime = (mc.level != null) ? mc.level.getGameTime() : 0L;
        float timeAnim = (gameTime + partialTick) * 0.05f;

        for (float[] aStar : ambientStars) {
            float sTheta = aStar[0];
            float sPhi = aStar[1];
            float sBright = aStar[2];

            float dYaw = Mth.wrapDegrees(sTheta - this.yaw);
            float dPitch = sPhi - this.pitch;

            if (Math.abs(dYaw) <= FOV && Math.abs(dPitch) <= FOV) {
                float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.42f);
                float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.42f);

                float dist = (float) Math.hypot(sx - centerX, sy - centerY);
                if (dist < lensRadius - 2.0f) {
                    float twinkle = 0.7f + 0.3f * (float) Math.sin(timeAnim + sTheta);
                    int alpha = (int) (sBright * twinkle * 255);
                    int col = (alpha << 24) | 0xE0F0FF;
                    guiGraphics.fill((int) sx, (int) sy, (int) sx + 1, (int) sy + 1, col);
                }
            }
        }

        // 3. Render Constellations and Stars
        int totalVisible = visibleConstellations.size();
        ConstellationStar hoveredStar = null;
        Constellation hoveredConstellation = null;

        for (int i = 0; i < totalVisible; i++) {
            Constellation constellation = visibleConstellations.get(i);
            boolean isDiscovered = (player != null) && PlayerAstralProgress.isDiscovered(player, constellation);

            float baseAzimuth = (i * (360.0f / Math.max(1, totalVisible)));
            float baseAltitude = 32.0f + (float) Math.sin(i * 1.7) * 22.0f;

            List<ConstellationStar> stars = constellation.getStars();
            float[][] starScreenPos = new float[stars.size()][2];
            boolean[] starInView = new boolean[stars.size()];

            for (int s = 0; s < stars.size(); s++) {
                ConstellationStar star = stars.get(s);
                float starAzimuth = baseAzimuth + (star.x() - 50.0f) * 0.32f;
                float starAltitude = baseAltitude + (50.0f - star.y()) * 0.32f;

                float dYaw = Mth.wrapDegrees(starAzimuth - this.yaw);
                float dPitch = starAltitude - this.pitch;

                float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.42f);
                float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.42f);

                starScreenPos[s][0] = sx;
                starScreenPos[s][1] = sy;

                float dist = (float) Math.hypot(sx - centerX, sy - centerY);
                starInView[s] = (dist < lensRadius - 4.0f);
            }

            // A. Render Connecting Lines
            Set<ConstellationConnection> drawn = drawnLines.computeIfAbsent(constellation, k -> new HashSet<>());
            if (isDiscovered) {
                drawn.addAll(constellation.getConnections());
            }

            int edgeIdx = 0;
            for (ConstellationConnection conn : drawn) {
                if (conn.fromIndex() < stars.size() && conn.toIndex() < stars.size()) {
                    if (starInView[conn.fromIndex()] || starInView[conn.toIndex()]) {
                        float x1 = starScreenPos[conn.fromIndex()][0];
                        float y1 = starScreenPos[conn.fromIndex()][1];
                        float x2 = starScreenPos[conn.toIndex()][0];
                        float y2 = starScreenPos[conn.toIndex()][1];

                        int lineColor = isDiscovered ? 0xFFFFD700 : 0xFF00F0FF;
                        drawLine(guiGraphics, (int) x1, (int) y1, (int) x2, (int) y2, lineColor);

                        // Pulse bead traveling along line
                        if (isDiscovered) {
                            float tBead = ((System.currentTimeMillis() * 0.001f * 0.7f) + (edgeIdx * 0.3f)) % 1.0f;
                            float bx = x1 * (1.0f - tBead) + x2 * tBead;
                            float by = y1 * (1.0f - tBead) + y2 * tBead;
                            if (Math.hypot(bx - centerX, by - centerY) < lensRadius - 4.0f) {
                                guiGraphics.fill((int) bx - 1, (int) by - 1, (int) bx + 2, (int) by + 2, 0xFFFFFFFF);
                            }
                        }
                    }
                }
                edgeIdx++;
            }

            // B. Render Active In-Sky Drag Line (When Shift is held)
            if (dragConstellation == constellation && dragStarIndex >= 0 && dragStarIndex < stars.size()) {
                float startX = starScreenPos[dragStarIndex][0];
                float startY = starScreenPos[dragStarIndex][1];

                float targetX = mouseX;
                float targetY = mouseY;

                // 14px magnetic snap to other stars
                for (int s = 0; s < stars.size(); s++) {
                    if (s != dragStarIndex && starInView[s]) {
                        float sx = starScreenPos[s][0];
                        float sy = starScreenPos[s][1];
                        if (Math.hypot(mouseX - sx, mouseY - sy) <= 14.0f) {
                            targetX = sx;
                            targetY = sy;
                            break;
                        }
                    }
                }

                drawLine(guiGraphics, (int) startX, (int) startY, (int) targetX, (int) targetY, 0xFFFFF080);
            }

            // C. Render Star Vertices
            for (int s = 0; s < stars.size(); s++) {
                if (starInView[s]) {
                    ConstellationStar star = stars.get(s);
                    float sx = starScreenPos[s][0];
                    float sy = starScreenPos[s][1];

                    int rgb = star.spectralClass().getColorRgb();
                    int starColor = 0xFF000000 | rgb;

                    boolean isHover = isShiftDown() && Math.hypot(mouseX - sx, mouseY - sy) <= 10.0f;
                    if (isHover) {
                        hoveredStar = star;
                        hoveredConstellation = constellation;
                    }

                    int r = isHover ? 4 : (isDiscovered ? 3 : 2);

                    // Outer halo
                    int haloColor = isDiscovered ? 0x66FFD700 : 0x5500F0FF;
                    guiGraphics.fill((int) sx - r - 2, (int) sy - r - 2, (int) sx + r + 3, (int) sy + r + 3, haloColor);
                    // Core star
                    guiGraphics.fill((int) sx - r, (int) sy - r, (int) sx + r + 1, (int) sy + r + 1, isHover ? 0xFFFFFFFF : starColor);
                }
            }
        }

        // 4. Render 128x128 Brass Bezel Overlay & Letterbox Outer Mask
        // Solid black letterboxes
        if (lensX > 0) {
            guiGraphics.fill(0, 0, lensX, screenHeight, 0xFF000000);
            guiGraphics.fill(lensX + size, 0, screenWidth, screenHeight, 0xFF000000);
        }
        if (lensY > 0) {
            guiGraphics.fill(0, 0, screenWidth, lensY, 0xFF000000);
            guiGraphics.fill(0, lensY + size, screenWidth, screenHeight, 0xFF000000);
        }

        // 128x128 circular bezel scaled to screen bounds
        guiGraphics.blit(OVERLAY_TEXTURE, lensX, lensY, size, size, 0.0f, 1.0f, 0.0f, 1.0f);

        // 5. Header Coordinate HUD
        String dirName = getDirectionName(this.yaw);
        int moonPhase = (mc.level != null) ? mc.level.getMoonPhase() : 0;
        String phaseName = getMoonPhaseName(moonPhase);

        guiGraphics.drawCenteredString(this.font, "§6§lCELESTIAL LOOKING GLASS", centerX, 15, 0xFFFFD700);
        guiGraphics.drawCenteredString(this.font, String.format("§eAzimuth: §f%.1f° %s  §e|  Declination: §f%+.1f°  §e|  Moon: §b%s", this.yaw, dirName, this.pitch, phaseName), centerX, 27, 0xFFE0E0E0);

        // 6. Footer Controls & Status HUD
        if (isShiftDown()) {
            guiGraphics.drawCenteredString(this.font, "§e✦ SHIFT ACTIVE: VIEW LOCKED ✦", centerX, screenHeight - 32, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, "§7Click & drag between stars to trace  •  Right-Click to reset lines", centerX, screenHeight - 20, 0xFFA0C0D0);
        } else {
            guiGraphics.drawCenteredString(this.font, "§8[Click & Drag to pan sky  •  Hold SHIFT to lock view & trace  •  ESC to exit]", centerX, screenHeight - 22, 0xFF88A0B0);
        }

        // 7. Discovery Fanfare Banner
        if (justDiscovered != null && (System.currentTimeMillis() - discoveryTime) < 5000) {
            String title = Component.translatable(justDiscovered.getUnlocalizedName()).getString();
            int bannerY = centerY - 30;

            guiGraphics.fill(centerX - 150, bannerY, centerX + 150, bannerY + 60, 0xF2101624);
            guiGraphics.fill(centerX - 150, bannerY, centerX + 150, bannerY + 2, 0xFFFFD700);
            guiGraphics.fill(centerX - 150, bannerY + 58, centerX + 150, bannerY + 60, 0xFFFFD700);

            guiGraphics.drawCenteredString(this.font, "§6✦ CONSTELLATION DISCOVERED! ✦", centerX, bannerY + 8, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, "§a" + title + " §7[" + justDiscovered.getTier().getDisplayName() + "]", centerX, bannerY + 22, 0xFF80FF80);
            guiGraphics.drawCenteredString(this.font, "§eRitual: §f" + justDiscovered.getRitualEffect(), centerX, bannerY + 36, 0xFFFFF0A0);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();

        if (button == 1 && isShiftDown()) {
            // Right-click while holding Shift resets drawn uncompleted lines
            drawnLines.clear();
            dragStarIndex = -1;
            dragConstellation = null;
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.CHISELED_BOOKSHELF_PICKUP, 0.7f, 0.9f);
            }
            return true;
        }

        if (button == 0 && isShiftDown()) {
            int screenWidth = this.width;
            int screenHeight = this.height;
            int size = Math.min(screenWidth, screenHeight);
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            float lensRadius = size * 0.42f;

            int totalVisible = visibleConstellations.size();
            for (int i = 0; i < totalVisible; i++) {
                Constellation constellation = visibleConstellations.get(i);
                float baseAzimuth = (i * (360.0f / Math.max(1, totalVisible)));
                float baseAltitude = 32.0f + (float) Math.sin(i * 1.7) * 22.0f;

                List<ConstellationStar> stars = constellation.getStars();
                for (int s = 0; s < stars.size(); s++) {
                    ConstellationStar star = stars.get(s);
                    float starAzimuth = baseAzimuth + (star.x() - 50.0f) * 0.32f;
                    float starAltitude = baseAltitude + (50.0f - star.y()) * 0.32f;

                    float dYaw = Mth.wrapDegrees(starAzimuth - this.yaw);
                    float dPitch = starAltitude - this.pitch;

                    float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.42f);
                    float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.42f);

                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (distFromCenter < lensRadius - 4.0f) {
                        if (Math.hypot(mouseX - sx, mouseY - sy) <= 12.0f) {
                            dragConstellation = constellation;
                            dragStarIndex = star.index();
                            return true;
                        }
                    }
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if (button == 0 && dragConstellation != null && dragStarIndex >= 0) {
            int screenWidth = this.width;
            int screenHeight = this.height;
            int size = Math.min(screenWidth, screenHeight);
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            float lensRadius = size * 0.42f;

            int totalVisible = visibleConstellations.size();
            int cIndex = visibleConstellations.indexOf(dragConstellation);
            if (cIndex >= 0) {
                float baseAzimuth = (cIndex * (360.0f / Math.max(1, totalVisible)));
                float baseAltitude = 32.0f + (float) Math.sin(cIndex * 1.7) * 22.0f;

                List<ConstellationStar> stars = dragConstellation.getStars();
                int targetIndex = -1;

                for (int s = 0; s < stars.size(); s++) {
                    if (s != dragStarIndex) {
                        ConstellationStar star = stars.get(s);
                        float starAzimuth = baseAzimuth + (star.x() - 50.0f) * 0.32f;
                        float starAltitude = baseAltitude + (50.0f - star.y()) * 0.32f;

                        float dYaw = Mth.wrapDegrees(starAzimuth - this.yaw);
                        float dPitch = starAltitude - this.pitch;

                        float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.42f);
                        float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.42f);

                        if (Math.hypot(sx - centerX, sy - centerY) < lensRadius - 4.0f) {
                            if (Math.hypot(event.x() - sx, event.y() - sy) <= 16.0f) {
                                targetIndex = star.index();
                                break;
                            }
                        }
                    }
                }

                if (targetIndex >= 0) {
                    for (ConstellationConnection conn : dragConstellation.getConnections()) {
                        if ((conn.fromIndex() == dragStarIndex && conn.toIndex() == targetIndex) ||
                            (conn.fromIndex() == targetIndex && conn.toIndex() == dragStarIndex)) {
                            Set<ConstellationConnection> drawn = drawnLines.computeIfAbsent(dragConstellation, k -> new HashSet<>());
                            drawn.add(conn);

                            if (minecraft != null && minecraft.player != null) {
                                minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
                            }

                            // Check complete discovery
                            if (drawn.size() >= dragConstellation.getConnections().size()) {
                                Player player = minecraft != null ? minecraft.player : null;
                                if (player != null) {
                                    PlayerAstralProgress.discover(player, dragConstellation);
                                    player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
                                    player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2f, 1.4f);
                                    player.playSound(SoundEvents.BEACON_ACTIVATE, 0.8f, 1.2f);

                                    this.justDiscovered = dragConstellation;
                                    this.discoveryTime = System.currentTimeMillis();

                                    // Inscribe Blank Star Chart
                                    for (int inv = 0; inv < player.getInventory().getContainerSize(); inv++) {
                                        ItemStack stack = player.getInventory().getItem(inv);
                                        if (stack.is(ModItems.STAR_CHART_BLANK.get())) {
                                            stack.shrink(1);
                                            ItemStack completedChart = CompletedStarChartItem.createFor(ModItems.STAR_CHART_COMPLETED.get(), dragConstellation);
                                            if (!player.getInventory().add(completedChart)) {
                                                player.drop(completedChart, false);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }

            dragConstellation = null;
            dragStarIndex = -1;
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

    private String getDirectionName(float yaw) {
        if (yaw >= 337.5 || yaw < 22.5) return "S";
        if (yaw >= 22.5 && yaw < 67.5) return "SW";
        if (yaw >= 67.5 && yaw < 112.5) return "W";
        if (yaw >= 112.5 && yaw < 157.5) return "NW";
        if (yaw >= 157.5 && yaw < 202.5) return "N";
        if (yaw >= 202.5 && yaw < 247.5) return "NE";
        if (yaw >= 247.5 && yaw < 292.5) return "E";
        return "SE";
    }

    private String getMoonPhaseName(int phase) {
        String[] phaseNames = {
                "Full Moon", "Waning Gibbous", "Third Quarter", "Waning Crescent",
                "New Moon", "Waxing Crescent", "First Quarter", "Waxing Gibbous"
        };
        return phaseNames[phase % 8];
    }
}
