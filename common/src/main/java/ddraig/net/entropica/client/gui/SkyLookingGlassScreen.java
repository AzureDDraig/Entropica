package ddraig.net.entropica.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class SkyLookingGlassScreen extends Screen {

    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/looking_glass_overlay.png");
    private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/star.png");

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

    // Sneak key held state
    private boolean isSneakHeld = false;

    // Rich Ambient Celestial Star Catalog
    public static class CelestialStar {
        public final float azimuth;
        public final float altitude;
        public final float baseSize;
        public final float rotSpeed;
        public final float twinkleFreq;
        public final float twinklePhase;
        public final SpectralClass spectralClass;
        public final String name; // null if anonymous ambient star
        public final String info;

        public CelestialStar(float azimuth, float altitude, float baseSize, float rotSpeed, float twinkleFreq, float twinklePhase, SpectralClass spectralClass, String name, String info) {
            this.azimuth = azimuth;
            this.altitude = altitude;
            this.baseSize = baseSize;
            this.rotSpeed = rotSpeed;
            this.twinkleFreq = twinkleFreq;
            this.twinklePhase = twinklePhase;
            this.spectralClass = spectralClass;
            this.name = name;
            this.info = info;
        }
    }

    private final List<CelestialStar> celestialStars = new ArrayList<>();

    // Reticle Focus Aiming State
    private Object focusedTarget = null;
    private Object lastFocusedTarget = null;

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

        // Initialize Major Named Celestial Landmark Stars
        celestialStars.add(new CelestialStar(25.0f, 55.0f, 14.0f, 12.0f, 2.5f, 0.0f, SpectralClass.CLASS_A, "Sirius (Alpha Canis)", "Brightest Northern Guide Star | Mag: -1.46m"));
        celestialStars.add(new CelestialStar(78.0f, 72.0f, 12.0f, -8.0f, 3.1f, 1.2f, SpectralClass.CLASS_A, "Vega (Alpha Lyrae)", "Stellar Calibration Apex | Mag: 0.03m"));
        celestialStars.add(new CelestialStar(0.0f, 88.5f, 15.0f, 4.0f, 1.8f, 2.4f, SpectralClass.CLASS_F, "Polaris (True Celestial Pole)", "True North Anchor | Mag: 1.98m"));
        celestialStars.add(new CelestialStar(145.0f, 38.0f, 14.0f, -15.0f, 2.0f, 3.1f, SpectralClass.CLASS_M, "Betelgeuse (Alpha Orionis)", "Pulsing Red Supergiant | Mag: 0.50m"));
        celestialStars.add(new CelestialStar(162.0f, 28.0f, 13.0f, 18.0f, 3.6f, 0.7f, SpectralClass.CLASS_B, "Rigel (Beta Orionis)", "Radiant Blue Supergiant | Mag: 0.13m"));
        celestialStars.add(new CelestialStar(205.0f, 44.0f, 12.0f, -10.0f, 2.7f, 1.9f, SpectralClass.CLASS_K, "Aldebaran (Alpha Tauri)", "Eye of the Cosmic Taurus | Mag: 0.85m"));
        celestialStars.add(new CelestialStar(235.0f, 65.0f, 13.0f, 7.0f, 3.0f, 4.2f, SpectralClass.CLASS_G, "Capella (Alpha Aurigae)", "Quadruple Golden Star | Mag: 0.08m"));
        celestialStars.add(new CelestialStar(285.0f, 22.0f, 14.0f, -12.0f, 2.2f, 5.0f, SpectralClass.CLASS_M, "Antares (Heart of Scorpio)", "Heart of the Void | Mag: 0.96m"));
        celestialStars.add(new CelestialStar(315.0f, 34.0f, 12.0f, 14.0f, 3.4f, 2.8f, SpectralClass.CLASS_B, "Spica (Alpha Virginis)", "Eclipsing Blue Giant | Mag: 0.97m"));
        celestialStars.add(new CelestialStar(110.0f, 60.0f, 13.0f, -6.0f, 2.8f, 1.5f, SpectralClass.CLASS_A, "Deneb (Alpha Cygni)", "Transmutation Vertex | Mag: 1.25m"));
        celestialStars.add(new CelestialStar(190.0f, 52.0f, 16.0f, 5.0f, 4.0f, 0.4f, SpectralClass.CLASS_O, "Pleiades Astral Cluster", "Open Cosmic Stardust Cluster | Mag: 1.6m"));

        // Generate 240 deterministic ambient background stars across the celestial sphere
        Random rng = new Random(133742L);
        SpectralClass[] classes = SpectralClass.values();
        for (int i = 0; i < 240; i++) {
            float sTheta = rng.nextFloat() * 360.0f;
            float sPhi = -5.0f + rng.nextFloat() * 95.0f;
            float sSize = 5.0f + rng.nextFloat() * 6.5f;
            float sRotSpeed = (rng.nextFloat() - 0.5f) * 30.0f;
            float sFreq = 1.5f + rng.nextFloat() * 3.0f;
            float sPhase = rng.nextFloat() * 6.28f;
            SpectralClass sc = classes[rng.nextInt(classes.length)];
            celestialStars.add(new CelestialStar(sTheta, sPhi, sSize, sRotSpeed, sFreq, sPhase, sc, null, null));
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
        if (this.isSneakHeld) return true;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.key() == GLFW.GLFW_KEY_LEFT_SHIFT || event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT ||
            (mc.options != null && mc.options.keyShift != null && mc.options.keyShift.matches(event))) {
            this.isSneakHeld = true;
        }
        return super.keyPressed(event);
    }

    public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.key() == GLFW.GLFW_KEY_LEFT_SHIFT || event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT ||
            (mc.options != null && mc.options.keyShift != null && mc.options.keyShift.matches(event))) {
            this.isSneakHeld = false;
        }
        return super.keyReleased(event);
    }

    private void syncPlayerRotation() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.setYRot(this.yaw);
            mc.player.setXRot(-this.pitch);
            mc.player.yRotO = this.yaw;
            mc.player.xRotO = -this.pitch;
            mc.player.yHeadRot = this.yaw;
            mc.player.yHeadRotO = this.yaw;
            mc.player.yBodyRot = this.yaw;
            mc.player.yBodyRotO = this.yaw;
        }
    }

    @Override
    public void onClose() {
        syncPlayerRotation();
        super.onClose();
    }

    @Override
    public void removed() {
        syncPlayerRotation();
        super.removed();
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

        // When Shift is held, camera panning is locked! Shift activates drawing mode!
        if (isShiftDown()) {
            return true;
        }

        // If shift is NOT held, dragging pans the telescope camera!
        if (dragStarIndex < 0) {
            this.yaw = (this.yaw - (float) dragX * 0.14f + 360.0f) % 360.0f;
            this.pitch = Mth.clamp(this.pitch + (float) dragY * 0.14f, -10.0f, 85.0f);
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
        float lensRadius = size * 0.43f;

        // 1. Fill deep cosmic space background
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF050811);

        long gameTime = (mc.level != null) ? mc.level.getGameTime() : 0L;
        float timeSec = (gameTime + partialTick) * 0.05f;

        this.focusedTarget = null;

        // Check Line of Sight / Sky Occlusion from player's eye
        boolean isObstructed = false;
        if (player != null && mc.level != null) {
            Vec3 eyePos = player.getEyePosition();
            float yawRad = (float) Math.toRadians(this.yaw);
            float pitchRad = (float) Math.toRadians(-this.pitch);
            Vec3 lookDir = new Vec3(
                    -Math.sin(yawRad) * Math.cos(pitchRad),
                    -Math.sin(pitchRad),
                    Math.cos(yawRad) * Math.cos(pitchRad)
            );
            Vec3 endPos = eyePos.add(lookDir.scale(128.0));
            BlockHitResult hit = mc.level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.BLOCK) {
                isObstructed = true;
            }
        }

        if (!isObstructed) {
            // 2. Render Cosmic Nebulae Dust Clouds inside telescope view
            renderNebulaGlow(guiGraphics, centerX, centerY, size, timeSec, 45.0f, 30.0f, 0x330088FF);
            renderNebulaGlow(guiGraphics, centerX, centerY, size, timeSec, 180.0f, 60.0f, 0x2A9900FF);
            renderNebulaGlow(guiGraphics, centerX, centerY, size, timeSec, 270.0f, 40.0f, 0x22FF6600);

            // 3. Render Ambient & Named Celestial Stars using star.png
            for (CelestialStar star : celestialStars) {
                float dYaw = Mth.wrapDegrees(star.azimuth - this.yaw);
                float dPitch = star.altitude - this.pitch;

                if (Math.abs(dYaw) <= FOV && Math.abs(dPitch) <= FOV) {
                    float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (distFromCenter < lensRadius - 4.0f) {
                        float twinkle = 0.75f + 0.25f * (float) Math.sin(timeSec * star.twinkleFreq + star.twinklePhase);
                        float drawSize = star.baseSize * twinkle;
                        float rotAngle = (timeSec * star.rotSpeed * 20.0f) % 360.0f;

                        // Check center crosshair aiming alignment (within 16px of center)
                        if (distFromCenter <= 16.0f && this.focusedTarget == null) {
                            this.focusedTarget = star;
                        }

                        // Render star texture
                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().translate(sx, sy);
                        guiGraphics.pose().rotate((float) Math.toRadians(rotAngle));

                        int half = Math.max(2, Math.round(drawSize * 0.5f));
                        guiGraphics.blit(STAR_TEXTURE, -half, -half, half, half, 0.0f, 1.0f, 0.0f, 1.0f);
                        guiGraphics.pose().popMatrix();

                        // If named landmark star, render label when nearby
                        if (star.name != null && distFromCenter < lensRadius * 0.8f) {
                            guiGraphics.drawString(this.font, "§b✦ §f" + star.name, (int) sx + half + 2, (int) sy - 4, 0xFFE0F0FF, false);
                        }
                    }
                }
            }

            // 4. Render Active Constellations and Stars
            int totalVisible = visibleConstellations.size();

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

                    float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                    starScreenPos[s][0] = sx;
                    starScreenPos[s][1] = sy;

                    float dist = (float) Math.hypot(sx - centerX, sy - centerY);
                    starInView[s] = (dist < lensRadius - 4.0f);

                    // Check crosshair aiming alignment on constellation star
                    if (dist <= 16.0f && this.focusedTarget == null) {
                        this.focusedTarget = new Object[]{constellation, star};
                    }
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

                        boolean isHover = isShiftDown() && Math.hypot(mouseX - sx, mouseY - sy) <= 12.0f;
                        float drawSize = isHover ? 16.0f : (isDiscovered ? 13.0f : 10.0f);

                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().translate(sx, sy);
                        guiGraphics.pose().rotate((float) Math.toRadians((timeSec * 25.0f) % 360.0f));
                        int half = Math.round(drawSize * 0.5f);
                        guiGraphics.blit(STAR_TEXTURE, -half, -half, half, half, 0.0f, 1.0f, 0.0f, 1.0f);
                        guiGraphics.pose().popMatrix();

                        // Selection ring on hover
                        if (isHover) {
                            drawCircle(guiGraphics, (int) sx, (int) sy, half + 3, 0xFFFFD700);
                        }
                    }
                }
            }
        } else {
            // Obstructed Vision Warning Mask
            guiGraphics.fill(lensX, lensY, lensX + size, lensY + size, 0xDD0A0C14);
        }

        // 5. Optical Aiming Reticle Focus Feedback
        if (!isObstructed && focusedTarget != null) {
            if (lastFocusedTarget != focusedTarget) {
                if (mc.player != null) {
                    mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 1.8f);
                }
                lastFocusedTarget = focusedTarget;
            }

            // Draw focusing lock brackets around crosshair center
            float pulseRadius = 16.0f + 2.0f * (float) Math.sin(timeSec * 8.0f);
            drawCircle(guiGraphics, centerX, centerY, (int) pulseRadius, 0xFFFFD700);
            guiGraphics.fill(centerX - 1, centerY - (int) pulseRadius - 4, centerX + 1, centerY - (int) pulseRadius + 2, 0xFFFFD700);
            guiGraphics.fill(centerX - 1, centerY + (int) pulseRadius - 2, centerX + 1, centerY + (int) pulseRadius + 4, 0xFFFFD700);
            guiGraphics.fill(centerX - (int) pulseRadius - 4, centerY - 1, centerX - (int) pulseRadius + 2, centerY + 1, 0xFFFFD700);
            guiGraphics.fill(centerX + (int) pulseRadius - 2, centerY - 1, centerX + (int) pulseRadius + 4, centerY + 1, 0xFFFFD700);
        } else {
            lastFocusedTarget = null;
        }

        // 6. Render 128x128 Brass Bezel Overlay & Letterbox Outer Mask FIRST
        // Solid black letterboxes
        if (lensX > 0) {
            guiGraphics.fill(0, 0, lensX, screenHeight, 0xFF000000);
            guiGraphics.fill(lensX + size, 0, screenWidth, screenHeight, 0xFF000000);
        }
        if (lensY > 0) {
            guiGraphics.fill(0, 0, screenWidth, lensY, 0xFF000000);
            guiGraphics.fill(0, lensY + size, screenWidth, screenHeight, 0xFF000000);
        }

        // 128x128 circular bezel scaled to screen bounds (x1, y1, x2, y2, u0, u1, v0, v1)
        guiGraphics.blit(OVERLAY_TEXTURE, lensX, lensY, lensX + size, lensY + size, 0.0f, 1.0f, 0.0f, 1.0f);

        // 7. ALL TEXT & HUD READOUTS RENDERED ON TOP (Never cut off by overlay)
        if (isObstructed) {
            guiGraphics.drawCenteredString(this.font, "§c⚠ LINE OF SIGHT OBSTRUCTED ⚠", centerX, centerY - 12, 0xFFFF5555);
            guiGraphics.drawCenteredString(this.font, "§7Telescope view is blocked by solid structure or ceiling", centerX, centerY + 2, 0xFFAAAAAA);
        } else if (focusedTarget != null) {
            // Telemetry Readout
            if (focusedTarget instanceof CelestialStar cStar) {
                if (cStar.name != null) {
                    guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: §f" + cStar.name + "  §e|  §b" + cStar.info, centerX, centerY + 28, 0xFFE0F0FF);
                } else {
                    guiGraphics.drawCenteredString(this.font, String.format("§7✦ Stellar Guide Beacon §e| Class: §b%s §e| Mag: §f%.2fm", cStar.spectralClass.name(), cStar.baseSize / 10.0f), centerX, centerY + 28, 0xFFC0D0E0);
                }
            } else if (focusedTarget instanceof Object[] pair) {
                Constellation c = (Constellation) pair[0];
                boolean isDisc = (player != null) && PlayerAstralProgress.isDiscovered(player, c);
                if (isDisc) {
                    String title = Component.translatable(c.getUnlocalizedName()).getString();
                    guiGraphics.drawCenteredString(this.font, "§6✦ CONSTELLATION NODE: §a" + title + " §7[" + c.getTier().getDisplayName() + "]", centerX, centerY + 28, 0xFFFFD700);
                } else {
                    guiGraphics.drawCenteredString(this.font, "§b✦ UNCHARTED ASTRAL RESONANCE ✦ §7[Hold Shift to Chart]", centerX, centerY + 28, 0xFF80D0FF);
                }
            }
        }

        // Header Coordinate HUD
        String dirName = getDirectionName(this.yaw);
        int moonPhase = (mc.level != null) ? mc.level.getMoonPhase() : 0;
        String phaseName = getMoonPhaseName(moonPhase);

        guiGraphics.drawCenteredString(this.font, "§6§lCELESTIAL LOOKING GLASS", centerX, 12, 0xFFFFD700);
        guiGraphics.drawCenteredString(this.font, String.format("§eAzimuth: §f%.1f° %s  §e|  Declination: §f%+.1f°  §e|  Moon: §b%s", this.yaw, dirName, this.pitch, phaseName), centerX, 24, 0xFFE0E0E0);

        // Footer Controls & Status HUD
        if (isShiftDown()) {
            guiGraphics.drawCenteredString(this.font, "§e✦ SNEAK ACTIVE: VIEW LOCKED ✦", centerX, screenHeight - 28, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, "§7Click & drag between stars to trace  •  Right-Click to reset lines", centerX, screenHeight - 16, 0xFFA0C0D0);
        } else {
            guiGraphics.drawCenteredString(this.font, "§8[Click & Drag to pan sky  •  Aim crosshairs to inspect stars  •  Hold SNEAK to trace  •  ESC to exit]", centerX, screenHeight - 18, 0xFF88A0B0);
        }

        // Discovery Fanfare Banner
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

    private void renderNebulaGlow(GuiGraphics guiGraphics, int centerX, int centerY, int size, float timeSec, float azimDeg, float altDeg, int color) {
        float dYaw = Mth.wrapDegrees(azimDeg - this.yaw);
        float dPitch = altDeg - this.pitch;

        if (Math.abs(dYaw) <= FOV + 10.0f && Math.abs(dPitch) <= FOV + 10.0f) {
            float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
            float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

            int nSize = (int) (size * 0.25f);
            guiGraphics.fill((int) sx - nSize, (int) sy - nSize, (int) sx + nSize, (int) sy + nSize, color);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();

        if (button == 1 && isShiftDown()) {
            // Right-click while holding Sneak resets drawn lines
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
            float lensRadius = size * 0.43f;

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

                    float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (distFromCenter < lensRadius - 4.0f) {
                        if (Math.hypot(mouseX - sx, mouseY - sy) <= 14.0f) {
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
            float lensRadius = size * 0.43f;

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

                        float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                        float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                        if (Math.hypot(sx - centerX, sy - centerY) < lensRadius - 4.0f) {
                            if (Math.hypot(event.x() - sx, event.y() - sy) <= 16.0f) {
                                targetIndex = star.index();
                                break;
                            }
                        }
                    }
                }

                if (targetIndex >= 0) {
                    // Allow drawing ANY connection between stars (correct or incorrect)!
                    ConstellationConnection newConn = new ConstellationConnection(dragStarIndex, targetIndex);
                    Set<ConstellationConnection> drawn = drawnLines.computeIfAbsent(dragConstellation, k -> new HashSet<>());
                    drawn.add(newConn);

                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7f, 1.1f);
                    }

                    // Validate if ALL required constellation connections are drawn correctly
                    boolean isMatch = true;
                    if (drawn.size() < dragConstellation.getConnections().size()) {
                        isMatch = false;
                    } else {
                        for (ConstellationConnection req : dragConstellation.getConnections()) {
                            boolean found = false;
                            for (ConstellationConnection d : drawn) {
                                if ((d.fromIndex() == req.fromIndex() && d.toIndex() == req.toIndex()) ||
                                    (d.fromIndex() == req.toIndex() && d.toIndex() == req.fromIndex())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                isMatch = false;
                                break;
                            }
                        }
                    }

                    if (isMatch) {
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

    private void drawCircle(GuiGraphics guiGraphics, int cx, int cy, int radius, int color) {
        int x = radius;
        int y = 0;
        int err = 0;

        while (x >= y) {
            guiGraphics.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
            guiGraphics.fill(cx + y, cy + x, cx + y + 1, cy + x + 1, color);
            guiGraphics.fill(cx - y, cy + x, cx - y + 1, cy + x + 1, color);
            guiGraphics.fill(cx - x, cy + y, cx - x + 1, cy + y + 1, color);
            guiGraphics.fill(cx - x, cy - y, cx - x + 1, cy - y + 1, color);
            guiGraphics.fill(cx - y, cy - x, cx - y + 1, cy - x + 1, color);
            guiGraphics.fill(cx + y, cy - x, cx + y + 1, cy - x + 1, color);
            guiGraphics.fill(cx + x, cy - y, cx + x + 1, cy - x + 1, color);

            if (err <= 0) {
                y += 1;
                err += 2 * y + 1;
            }
            if (err > 0) {
                x -= 1;
                err -= 2 * x + 1;
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
