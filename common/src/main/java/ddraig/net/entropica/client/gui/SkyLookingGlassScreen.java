package ddraig.net.entropica.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.astral.*;
import ddraig.net.entropica.client.renderer.CelestialSkyRenderer;
import ddraig.net.entropica.network.AstralLensAimPayload;
import ddraig.net.entropica.network.ConstellationDiscoveryPayload;
import ddraig.net.entropica.network.SyncChartedConnectionsPayload;
import ddraig.net.entropica.network.TelescopeAimPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class SkyLookingGlassScreen extends Screen {

    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/looking_glass_overlay.png");
    private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/star.png");
    private static final ResourceLocation NEBULA_PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/nebula_puff.png");

    private float yaw = 0.0f;
    private float pitch = 45.0f;
    private static final float FOV = 28.0f;
    private static final float PAN_SPEED = 0.045f;

    private List<Constellation> visibleConstellations = new ArrayList<>();
    
    // Universal Star Tracing Graph: Edges formatted as "nodeA---nodeB"
    private final Set<String> drawnEdges = Collections.synchronizedSet(new HashSet<>());

    // Interaction & Tracing state
    private String dragStarId = null;
    private boolean isSneakHeld = false;
    private String hoveredEdgeKey = null;

    // Track all on-screen stars during the current frame for universal tracing
    public record StarNode(String id, float sx, float sy, float size, String label, EssenceType essence, SpectralClass spectralClass, Constellation constellation, int starIndex) {}
    private final Map<String, StarNode> onScreenStars = new HashMap<>();

    // Reticle Focus Aiming State
    private Object focusedTarget = null;
    private Object lastFocusedTarget = null;

    // Last discovered banner notification
    private Constellation justDiscovered = null;
    private long discoveryTime = 0;
    private String lensLockMessage = null;
    private long lensLockTime = 0;

    private BlockPos telescopePos = null;
    private boolean isLensMode = false;

    public SkyLookingGlassScreen() {
        this(null, false);
    }

    public SkyLookingGlassScreen(BlockPos targetPos, boolean isLens) {
        super(Component.literal("Celestial Looking Glass"));
        this.telescopePos = targetPos;
        this.isLensMode = isLens;

        Minecraft mc = Minecraft.getInstance();
        if (targetPos != null && mc.level != null) {
            if (isLens && mc.level.getBlockEntity(targetPos) instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity lens) {
                if (lens.isFocused()) {
                    this.yaw = lens.getYaw();
                    this.pitch = lens.getPitch();
                } else if (mc.player != null) {
                    this.yaw = Mth.wrapDegrees(mc.player.getYRot());
                    if (this.yaw < 0) this.yaw += 360.0f;
                    this.pitch = Mth.clamp(-mc.player.getXRot(), 15.0f, 90.0f);
                } else {
                    this.yaw = lens.getYaw();
                    this.pitch = lens.getPitch();
                }
            } else if (mc.level.getBlockEntity(targetPos) instanceof ddraig.net.entropica.block.entity.StationaryBrassTelescopeBlockEntity be) {
                this.yaw = be.getYaw();
                this.pitch = be.getPitch();
            }
        } else if (mc.player != null) {
            this.yaw = Mth.wrapDegrees(mc.player.getYRot());
            if (this.yaw < 0) this.yaw += 360.0f;
            this.pitch = Mth.clamp(-mc.player.getXRot(), -35.0f, 90.0f);
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

        // Load all saved charted star connections for the player
        if (mc.player != null) {
            this.drawnEdges.addAll(PlayerAstralProgress.getChartedConnections(mc.player));
        }
    }

    public static void open() {
        openForTelescope(null);
    }

    public static void openForTelescope(BlockPos pos) {
        Minecraft.getInstance().setScreen(new SkyLookingGlassScreen(pos, false));
    }

    public static void openForLens(BlockPos pos) {
        Minecraft.getInstance().setScreen(new SkyLookingGlassScreen(pos, true));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static float[] celestialToApparentAngles(float sphereAzimuthRad, float sphereAltitudeRad, float celestialAngleDeg) {
        float x = Mth.cos(sphereAltitudeRad) * Mth.sin(sphereAzimuthRad);
        float y = Mth.sin(sphereAltitudeRad);
        float z = Mth.cos(sphereAltitudeRad) * Mth.cos(sphereAzimuthRad);

        float celestialRad = (float) Math.toRadians(celestialAngleDeg);
        float sinTheta = Mth.sin(celestialRad);
        float cosTheta = Mth.cos(celestialRad);

        float x1 = x;
        float y1 = y * cosTheta - z * sinTheta;
        float z1 = y * sinTheta + z * cosTheta;

        float xw = -z1;
        float yw = y1;
        float zw = x1;

        float appPitch = (float) Math.toDegrees(Math.asin(Mth.clamp(yw, -1.0f, 1.0f)));
        float appYaw = (float) Math.toDegrees(Math.atan2(-xw, zw));
        if (appYaw < 0) appYaw += 360.0f;

        return new float[]{appYaw, appPitch};
    }

    private float getCelestialAngle(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            return mc.level.dimension().equals(Level.END) ? ((mc.level.getGameTime() + partialTick) * 0.02f) : (mc.level.getTimeOfDay(partialTick) * 360.0f);
        }
        return 180.0f;
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

        // Spacebar locks/calibrates Astral Lens on focused target
        if (event.key() == GLFW.GLFW_KEY_SPACE && this.isLensMode && this.telescopePos != null) {
            lockAstralLensFocus();
            return true;
        }

        return super.keyPressed(event);
    }

    private void lockAstralLensFocus() {
        String targetName = "Sky Azimuth " + String.format("%.1f°", this.yaw);
        if (this.focusedTarget instanceof CelestialStarHelper.LandmarkStar ls) {
            targetName = ls.name();
        } else if (this.focusedTarget instanceof CelestialStarHelper.AmbientStar as) {
            targetName = as.name();
        } else if (this.focusedTarget instanceof Object[] pair) {
            Constellation c = (Constellation) pair[0];
            ConstellationStar star = (ConstellationStar) pair[1];
            targetName = CelestialStarHelper.getConstellationStarName(c, c.getStars().indexOf(star));
        } else if (this.focusedTarget instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity targetLens) {
            targetName = "Relay Lens at [" + targetLens.getBlockPos().toShortString() + "]";
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.BEACON_POWER_SELECT, 0.9f, 1.3f);
            mc.player.playSound(SoundEvents.CHISELED_BOOKSHELF_INSERT, 0.8f, 1.1f);
        }

        if (this.telescopePos != null) {
            if (mc.level != null && mc.level.getBlockEntity(this.telescopePos) instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity lens) {
                lens.setFocus(this.yaw, this.pitch, targetName, true);
            }
            NetworkManager.sendToServer(new AstralLensAimPayload(this.telescopePos, this.yaw, this.pitch, targetName, true));
        }

        this.lensLockMessage = "§a✦ ASTRAL LENS CALIBRATED & LOCKED ON: §f" + targetName;
        this.lensLockTime = System.currentTimeMillis();
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

        if (this.telescopePos != null && mc.level != null) {
            if (this.isLensMode) {
                boolean focused = false;
                String targetName = (this.focusedTarget instanceof CelestialStarHelper.LandmarkStar ls) ? ls.name() : "Calibrated Focus";
                if (mc.level.getBlockEntity(this.telescopePos) instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity lens) {
                    focused = lens.isFocused();
                    if (lens.getTargetName() != null && !lens.getTargetName().isBlank() && !lens.getTargetName().equals("Uncalibrated")) {
                        targetName = lens.getTargetName();
                    }
                    lens.setFocus(this.yaw, this.pitch, targetName, focused);
                }
                NetworkManager.sendToServer(new AstralLensAimPayload(this.telescopePos, this.yaw, this.pitch, targetName, focused));
            } else {
                if (mc.level.getBlockEntity(this.telescopePos) instanceof ddraig.net.entropica.block.entity.StationaryBrassTelescopeBlockEntity be) {
                    be.setAngles(this.yaw, this.pitch);
                }
                NetworkManager.sendToServer(new TelescopeAimPayload(this.telescopePos, this.yaw, this.pitch));
            }
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
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!isShiftDown()) {
            this.yaw = Mth.wrapDegrees(this.yaw - (float) dragX * PAN_SPEED);
            if (this.yaw < 0) this.yaw += 360.0f;
            this.pitch = Mth.clamp(this.pitch + (float) dragY * PAN_SPEED, -35.0f, 90.0f);
            syncPlayerRotation();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    private static String makeEdgeKey(String a, String b) {
        return (a.compareTo(b) < 0) ? (a + "---" + b) : (b + "---" + a);
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
        int screenWidth = this.width;
        int screenHeight = this.height;
        int size = Math.min(screenWidth, screenHeight);
        int lensX = (screenWidth - size) / 2;
        int lensY = (screenHeight - size) / 2;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        float lensRadius = size * 0.43f;

        float timeSec = (System.currentTimeMillis() % 10000000L) * 0.001f;
        this.focusedTarget = null;
        this.onScreenStars.clear();
        this.hoveredEdgeKey = null;

        float celestialAngle = getCelestialAngle(partialTick);

        // 1. Deep Space Cosmic Background
        guiGraphics.fill(lensX, lensY, lensX + size, lensY + size, 0xFF03050D);

        // Raycast Line of Sight check: Start above telescope/lens block to prevent self-collision
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        boolean isObstructed = false;
        if (player != null && mc.level != null) {
            Vec3 eyePos = (this.telescopePos != null)
                    ? Vec3.atCenterOf(this.telescopePos).add(0, 0.75, 0)
                    : player.getEyePosition(partialTick);

            float yawRad = (float) Math.toRadians(this.yaw);
            float pitchRad = (float) Math.toRadians(-this.pitch);
            Vec3 lookDir = new Vec3(
                    -Mth.sin(yawRad) * Mth.cos(pitchRad),
                    -Mth.sin(pitchRad),
                    Mth.cos(yawRad) * Mth.cos(pitchRad)
            );
            Vec3 endPos = eyePos.add(lookDir.scale(128.0));
            BlockHitResult hit = mc.level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos hitPos = hit.getBlockPos();
                if (this.telescopePos == null || (!hitPos.equals(this.telescopePos) && !hitPos.equals(this.telescopePos.below()))) {
                    isObstructed = true;
                }
            }
        }

        if (!isObstructed) {
            // 2. Render Organic Multi-Puff Nebulae Clouds using nebula_puff.png
            for (CelestialSkyRenderer.NebulaComplex complex : CelestialSkyRenderer.NEBULA_COMPLEXES) {
                for (CelestialSkyRenderer.NebulaPuff puff : complex.puffs) {
                    float azimDeg = complex.baseAzim + puff.dAzim();
                    float altDeg = complex.baseAlt + puff.dAlt();

                    float[] app = celestialToApparentAngles((float) Math.toRadians(azimDeg), (float) Math.toRadians(altDeg), celestialAngle);
                    float dYaw = Mth.wrapDegrees(app[0] - this.yaw);
                    float dPitch = app[1] - this.pitch;

                    if (Math.abs(dYaw) <= FOV + 10.0f && Math.abs(dPitch) <= FOV + 10.0f) {
                        float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                        float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                        int pSize = (int) (puff.size() * (size / 140.0f));
                        int half = pSize / 2;

                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().translate(sx, sy);
                        guiGraphics.pose().rotate((float) Math.toRadians((timeSec * 4.0f + puff.dAzim() * 10.0f) % 360.0f));

                        // Render soft nebula cloud puff
                        guiGraphics.blit(NEBULA_PUFF_TEXTURE, -half, -half, half, half, 0.0f, 1.0f, 0.0f, 1.0f);
                        guiGraphics.pose().popMatrix();
                    }
                }
            }

            // 3. Render Ambient Stars
            for (int idx = 0; idx < CelestialStarHelper.AMBIENT_STARS.size(); idx++) {
                CelestialStarHelper.AmbientStar star = CelestialStarHelper.AMBIENT_STARS.get(idx);
                float[] appAngles = celestialToApparentAngles((float) Math.toRadians(star.azimuth()), (float) Math.toRadians(star.altitude()), celestialAngle);
                float starYaw = appAngles[0];
                float starPitch = appAngles[1];

                float dYaw = Mth.wrapDegrees(starYaw - this.yaw);
                float dPitch = starPitch - this.pitch;

                if (Math.abs(dYaw) <= FOV && Math.abs(dPitch) <= FOV) {
                    float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (distFromCenter < lensRadius - 4.0f) {
                        float twinkle = 0.75f + 0.25f * (float) Math.sin(timeSec * 2.2f + star.azimuth());
                        float drawSize = star.size() * twinkle;
                        float rotAngle = (timeSec * 20.0f + idx * 15.0f) % 360.0f;

                        String starNodeId = "a:" + idx;
                        onScreenStars.put(starNodeId, new StarNode(starNodeId, sx, sy, drawSize, null, star.essenceType(), star.spectralClass(), null, -1));

                        if (distFromCenter <= 16.0f && this.focusedTarget == null) {
                            this.focusedTarget = star;
                        }

                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().translate(sx, sy);
                        guiGraphics.pose().rotate((float) Math.toRadians(rotAngle));

                        int half = Math.max(2, Math.round(drawSize * 0.5f));
                        guiGraphics.blit(STAR_TEXTURE, -half, -half, half, half, 0.0f, 1.0f, 0.0f, 1.0f);
                        guiGraphics.pose().popMatrix();
                    }
                }
            }

            // 4. Render Landmark Guide Stars
            for (CelestialStarHelper.LandmarkStar ls : CelestialStarHelper.LANDMARK_STARS) {
                float[] appAngles = celestialToApparentAngles((float) Math.toRadians(ls.azimuth()), (float) Math.toRadians(ls.altitude()), celestialAngle);
                float starYaw = appAngles[0];
                float starPitch = appAngles[1];

                float dYaw = Mth.wrapDegrees(starYaw - this.yaw);
                float dPitch = starPitch - this.pitch;

                if (Math.abs(dYaw) <= FOV && Math.abs(dPitch) <= FOV) {
                    float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (distFromCenter < lensRadius - 4.0f) {
                        float twinkle = 0.85f + 0.15f * (float) Math.sin(timeSec * 3.0f + ls.azimuth());
                        float drawSize = ls.size() * twinkle;
                        float rotAngle = (timeSec * 15.0f) % 360.0f;

                        String starNodeId = "l:" + ls.name();
                        onScreenStars.put(starNodeId, new StarNode(starNodeId, sx, sy, drawSize, ls.name(), ls.essenceType(), ls.spectralClass(), null, -1));

                        if (distFromCenter <= 16.0f && this.focusedTarget == null) {
                            this.focusedTarget = ls;
                        }

                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().translate(sx, sy);
                        guiGraphics.pose().rotate((float) Math.toRadians(rotAngle));

                        int half = Math.max(3, Math.round(drawSize * 0.5f));
                        guiGraphics.blit(STAR_TEXTURE, -half, -half, half, half, 0.0f, 1.0f, 0.0f, 1.0f);
                        guiGraphics.pose().popMatrix();

                        if (distFromCenter < lensRadius * 0.8f) {
                            guiGraphics.drawString(this.font, "§b✦ §f" + ls.name(), (int) sx + half + 2, (int) sy - 4, 0xFFE0F0FF, false);
                        }
                    }
                }
            }

            // 5. Render Constellation Nodes (Declinations spanning 0° to 90°)
            int totalVisible = visibleConstellations.size();

            for (int i = 0; i < totalVisible; i++) {
                Constellation constellation = visibleConstellations.get(i);
                boolean isDiscovered = (player != null) && PlayerAstralProgress.isDiscovered(player, constellation);

                float baseAzimuth = (float) Math.toRadians(i * (360.0f / Math.max(1, totalVisible)));
                float baseAltitude = (float) Math.toRadians(8.0f + ((i * 19.5f) % 76.0f));

                List<ConstellationStar> stars = constellation.getStars();

                for (int s = 0; s < stars.size(); s++) {
                    ConstellationStar star = stars.get(s);
                    float starSphereAzimuth = baseAzimuth + (float) Math.toRadians((50.0f - star.x()) * 0.28f);
                    float starSphereAltitude = Mth.clamp(baseAltitude + (float) Math.toRadians((star.y() - 50.0f) * 0.28f), 0.0f, (float) Math.toRadians(90.0f));

                    float[] appAngles = celestialToApparentAngles(starSphereAzimuth, starSphereAltitude, celestialAngle);
                    float starYaw = appAngles[0];
                    float starPitch = appAngles[1];

                    float dYaw = Mth.wrapDegrees(starYaw - this.yaw);
                    float dPitch = starPitch - this.pitch;

                    float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                    float dist = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (dist < lensRadius - 4.0f) {
                        String cNodeId = "c:" + constellation.getId().toString() + ":" + s;
                        float twinkle = 0.85f + 0.15f * (float) Math.sin(timeSec * 3.0f + s * 1.2f);
                        float sSize = Math.max(6.5f, star.brightness() * 7.5f) * twinkle;

                        onScreenStars.put(cNodeId, new StarNode(cNodeId, sx, sy, sSize, null, constellation.getEssenceType(), star.spectralClass(), constellation, s));

                        if (dist <= 16.0f && this.focusedTarget == null) {
                            this.focusedTarget = new Object[]{constellation, star};
                        }

                        // Render Natural Star Billets
                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().translate(sx, sy);
                        guiGraphics.pose().rotate((float) Math.toRadians((timeSec * 15.0f + s * 30.0f) % 360.0f));

                        int half = Math.max(2, Math.round(sSize * 0.5f));
                        guiGraphics.blit(STAR_TEXTURE, -half, -half, half, half, 0.0f, 1.0f, 0.0f, 1.0f);
                        guiGraphics.pose().popMatrix();

                        if (Math.hypot(mouseX - sx, mouseY - sy) <= 10.0f && isShiftDown()) {
                            drawCircle(guiGraphics, (int) sx, (int) sy, half + 3, 0x88FFFFFF);
                        }
                    }
                }
            }

            // Find closest drawn line to cursor for targeted highlighting & erasing
            float minLineDist = 8.0f;
            List<String> currentEdgesSnapshot;
            synchronized (drawnEdges) {
                currentEdgesSnapshot = new ArrayList<>(drawnEdges);
            }

            for (String edge : currentEdgesSnapshot) {
                String[] parts = edge.split("---");
                if (parts.length == 2) {
                    StarNode nodeA = onScreenStars.get(parts[0]);
                    StarNode nodeB = onScreenStars.get(parts[1]);
                    if (nodeA != null && nodeB != null) {
                        float d = pointToSegmentDistance((float) mouseX, (float) mouseY, nodeA.sx(), nodeA.sy(), nodeB.sx(), nodeB.sy());
                        if (d < minLineDist) {
                            minLineDist = d;
                            this.hoveredEdgeKey = edge;
                        }
                    }
                }
            }

            // 6. Universal Star Tracing: Render ALL User-Drawn Lines Between Any Connected Stars
            for (String edge : currentEdgesSnapshot) {
                String[] parts = edge.split("---");
                if (parts.length == 2) {
                    StarNode nodeA = onScreenStars.get(parts[0]);
                    StarNode nodeB = onScreenStars.get(parts[1]);
                    if (nodeA != null && nodeB != null) {
                        boolean isHighlighted = edge.equals(this.hoveredEdgeKey);

                        int lineColor = 0xFF00F0FF;
                        if (isHighlighted) {
                            lineColor = 0xFFFF3333; // Vibrant highlighted red for targeted erase candidate
                        } else if (nodeA.constellation() != null && nodeB.constellation() != null && nodeA.constellation().equals(nodeB.constellation())) {
                            int essRgb = (nodeA.constellation().getEssenceType().getR() << 16) | (nodeA.constellation().getEssenceType().getG() << 8) | nodeA.constellation().getEssenceType().getB();
                            lineColor = 0xFF000000 | essRgb;
                        } else if (nodeA.essence() != null && nodeB.essence() != null) {
                            int r = (nodeA.essence().getR() + nodeB.essence().getR()) / 2;
                            int g = (nodeA.essence().getG() + nodeB.essence().getG()) / 2;
                            int b = (nodeA.essence().getB() + nodeB.essence().getB()) / 2;
                            lineColor = 0xFF000000 | (r << 16) | (g << 8) | b;
                        }

                        drawLine(guiGraphics, (int) nodeA.sx(), (int) nodeA.sy(), (int) nodeB.sx(), (int) nodeB.sy(), lineColor);

                        if (isHighlighted) {
                            // Extra glow border around highlighted erase line
                            drawLine(guiGraphics, (int) nodeA.sx(), (int) nodeA.sy() - 1, (int) nodeB.sx(), (int) nodeB.sy() - 1, 0x88FFAAAA);
                            drawLine(guiGraphics, (int) nodeA.sx(), (int) nodeA.sy() + 1, (int) nodeB.sx(), (int) nodeB.sy() + 1, 0x88FFAAAA);
                        }
                    }
                }
            }

            // Render Active Drag Line Between ANY Two Stars
            if (dragStarId != null && onScreenStars.containsKey(dragStarId)) {
                StarNode startNode = onScreenStars.get(dragStarId);
                float startX = startNode.sx();
                float startY = startNode.sy();

                float targetX = mouseX;
                float targetY = mouseY;

                // 14px magnetic snap to ANY other star in the entire sky
                for (StarNode other : onScreenStars.values()) {
                    if (!other.id().equals(dragStarId)) {
                        if (Math.hypot(mouseX - other.sx(), mouseY - other.sy()) <= 14.0f) {
                            targetX = other.sx();
                            targetY = other.sy();
                            break;
                        }
                    }
                }

                int dragColor = 0xFFFFF080;
                if (startNode.essence() != null) {
                    int essRgb = (startNode.essence().getR() << 16) | (startNode.essence().getG() << 8) | startNode.essence().getB();
                    dragColor = 0xFF000000 | essRgb;
                }
                drawLine(guiGraphics, (int) startX, (int) startY, (int) targetX, (int) targetY, dragColor);
            }
        }

        // 6.5. Render Nearby Astral Lenses within 20 Blocks & Clear Line of Sight (< 60° Angle of Attack)
        if (this.isLensMode && this.telescopePos != null && mc.level != null && player != null) {
            Vec3 lensEye = Vec3.atCenterOf(this.telescopePos).add(0, 0.4375, 0);
            float yawRad = (float) Math.toRadians(this.yaw);
            float pitchRad = (float) Math.toRadians(-this.pitch);
            Vec3 lookDir = new Vec3(
                    -Mth.sin(yawRad) * Mth.cos(pitchRad),
                    -Mth.sin(pitchRad),
                    Mth.cos(yawRad) * Mth.cos(pitchRad)
            ).normalize();

            int searchRadius = 20;
            BlockPos minPos = this.telescopePos.offset(-searchRadius, -searchRadius, -searchRadius);
            BlockPos maxPos = this.telescopePos.offset(searchRadius, searchRadius, searchRadius);

            for (BlockPos targetPos : BlockPos.betweenClosed(minPos, maxPos)) {
                if (targetPos.equals(this.telescopePos)) continue;
                double distSq = this.telescopePos.distSqr(targetPos);
                if (distSq > 400.0 || distSq < 1.0) continue;

                if (mc.level.getBlockEntity(targetPos) instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity targetLens) {
                    Vec3 targetCenter = Vec3.atCenterOf(targetPos).add(0, 0.4375, 0);
                    Vec3 toTarget = targetCenter.subtract(lensEye);
                    double distance = toTarget.length();
                    Vec3 toTargetDir = toTarget.normalize();

                    // 1. Clear Line of Sight Check (must not hit intervening solid blocks)
                    BlockHitResult losHit = mc.level.clip(new ClipContext(lensEye, targetCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
                    if (losHit.getType() == HitResult.Type.BLOCK && !losHit.getBlockPos().equals(targetPos) && !losHit.getBlockPos().equals(this.telescopePos)) {
                        continue; // Obstructed by wall or obstacle
                    }

                    // 2. Angle of Attack Check (< 60.0 degrees from aimed view vector)
                    double dot = lookDir.dot(toTargetDir);
                    double angleDeg = Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
                    if (angleDeg > 60.0) {
                        continue; // Exceeds 60° angle of attack
                    }

                    // 3. Project to Screen Coordinates
                    float targetPitch = (float) Math.toDegrees(Math.asin(toTargetDir.y));
                    float targetYaw = (float) Math.toDegrees(Math.atan2(-toTargetDir.x, toTargetDir.z));
                    if (targetYaw < 0) targetYaw += 360.0f;

                    float dYaw = Mth.wrapDegrees(targetYaw - this.yaw);
                    float dPitch = targetPitch - this.pitch;

                    if (Math.abs(dYaw) <= FOV && Math.abs(dPitch) <= FOV) {
                        float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                        float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);
                        float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);

                        if (distFromCenter < lensRadius - 4.0f) {
                            boolean isTargeted = distFromCenter <= 18.0f;
                            if (isTargeted && this.focusedTarget == null) {
                                this.focusedTarget = targetLens;
                            }

                            // Visual Target Circle Marker
                            int circleColor = targetLens.isBeamActive() ? 0xFF00FFFF : (isTargeted ? 0xFF80FF80 : 0xFF38BDF8);
                            float ringR = isTargeted ? (14.0f + 2.0f * (float) Math.sin(timeSec * 8.0f)) : 10.0f;

                            drawCircle(guiGraphics, (int) sx, (int) sy, (int) ringR, circleColor);
                            drawCircle(guiGraphics, (int) sx, (int) sy, (int) ringR + 1, (circleColor & 0x00FFFFFF) | 0x66000000);

                            // Inner cross marker
                            guiGraphics.fill((int) sx - 3, (int) sy, (int) sx + 4, (int) sy + 1, circleColor);
                            guiGraphics.fill((int) sx, (int) sy - 3, (int) sx + 1, (int) sy + 4, circleColor);

                            // Distance Label below target circle
                            String label = String.format("§b✦ Lens §7[§e%.1fm§7]", distance);
                            guiGraphics.drawCenteredString(this.font, label, (int) sx, (int) (sy + ringR + 3), 0xFFE0F0FF);
                        }
                    }
                }
            }
        }

        // 7. Reticle Crosshairs & Focus Aiming Target
        guiGraphics.fill(centerX - 12, centerY, centerX - 3, centerY + 1, 0xAAFFFFFF);
        guiGraphics.fill(centerX + 3, centerY, centerX + 12, centerY + 1, 0xAAFFFFFF);
        guiGraphics.fill(centerX, centerY - 12, centerX + 1, centerY - 3, 0xAAFFFFFF);
        guiGraphics.fill(centerX, centerY + 3, centerX + 1, centerY + 12, 0xAAFFFFFF);

        if (focusedTarget != null) {
            if (lastFocusedTarget != focusedTarget) {
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.4f, 1.6f);
                }
                lastFocusedTarget = focusedTarget;
            }

            int reticleColor = 0xFFFFD700;
            if (focusedTarget instanceof Object[] pair && pair[0] instanceof Constellation fc) {
                boolean isDisc = (player != null) && PlayerAstralProgress.isDiscovered(player, fc);
                if (isDisc) {
                    int essRgb = (fc.getEssenceType().getR() << 16) | (fc.getEssenceType().getG() << 8) | fc.getEssenceType().getB();
                    reticleColor = 0xFF000000 | essRgb;
                }
            } else if (focusedTarget instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity) {
                reticleColor = 0xFF80FF80;
            }

            float pulseRadius = 16.0f + 2.0f * (float) Math.sin(timeSec * 8.0f);
            drawCircle(guiGraphics, centerX, centerY, (int) pulseRadius, reticleColor);
            guiGraphics.fill(centerX - 1, centerY - (int) pulseRadius - 4, centerX + 1, centerY - (int) pulseRadius + 2, reticleColor);
            guiGraphics.fill(centerX - 1, centerY + (int) pulseRadius - 2, centerX + 1, centerY + (int) pulseRadius + 4, reticleColor);
            guiGraphics.fill(centerX - (int) pulseRadius - 4, centerY - 1, centerX - (int) pulseRadius + 2, centerY + 1, reticleColor);
            guiGraphics.fill(centerX + (int) pulseRadius - 2, centerY - 1, centerX + (int) pulseRadius + 4, centerY + 1, reticleColor);
        } else {
            lastFocusedTarget = null;
        }

        // 8. Render 128x128 Brass Bezel Overlay & Black Letterboxes
        if (lensX > 0) {
            guiGraphics.fill(0, 0, lensX, screenHeight, 0xFF000000);
            guiGraphics.fill(lensX + size, 0, screenWidth, screenHeight, 0xFF000000);
        }
        if (lensY > 0) {
            guiGraphics.fill(0, 0, screenWidth, lensY, 0xFF000000);
            guiGraphics.fill(0, lensY + size, screenWidth, screenHeight, 0xFF000000);
        }

        guiGraphics.blit(OVERLAY_TEXTURE, lensX, lensY, lensX + size, lensY + size, 0.0f, 1.0f, 0.0f, 1.0f);

        // 9. Text & HUD Readouts on top layer
        if (isObstructed) {
            guiGraphics.drawCenteredString(this.font, "§c⚠ LINE OF SIGHT OBSTRUCTED ⚠", centerX, centerY - 12, 0xFFFF5555);
            guiGraphics.drawCenteredString(this.font, "§7View is blocked by solid structure or ceiling", centerX, centerY + 2, 0xFFAAAAAA);
        } else if (focusedTarget != null) {
            if (focusedTarget instanceof CelestialStarHelper.LandmarkStar ls) {
                guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: §f" + ls.name() + "  §e| Essence: " + ls.essenceType().getFormattedName() + "  §e|  §b" + ls.info(), centerX, centerY + 28, 0xFFE0F0FF);
            } else if (focusedTarget instanceof CelestialStarHelper.AmbientStar as) {
                guiGraphics.drawCenteredString(this.font, String.format("§6✦ TARGET: §f%s  §e| Essence: %s  §e| Class: §b%s  §e| Mag: §f%.2fm", as.name(), as.essenceType().getFormattedName(), as.spectralClass().name(), as.size() / 10.0f), centerX, centerY + 28, 0xFFC0D0E0);
            } else if (focusedTarget instanceof Object[] pair) {
                Constellation c = (Constellation) pair[0];
                ConstellationStar star = (ConstellationStar) pair[1];
                String starName = CelestialStarHelper.getConstellationStarName(c, c.getStars().indexOf(star));
                boolean isDisc = (player != null) && PlayerAstralProgress.isDiscovered(player, c);
                String title = Component.translatable(c.getUnlocalizedName()).getString();
                String essCode = c.getEssenceType().getColorCode();
                String essFormatted = c.getEssenceType().getFormattedName();
                if (isDisc) {
                    guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: " + essCode + starName + " §e| Constellation: §f" + title + " §7[" + c.getTier().getDisplayName() + " • " + essFormatted + "§7]", centerX, centerY + 28, 0xFFFFFFFF);
                } else {
                    guiGraphics.drawCenteredString(this.font, String.format("§6✦ TARGET: §f%s §e| Essence: %s §e| Class: §b%s §e| Mag: §f%.2fm", starName, essFormatted, star.spectralClass().name(), star.brightness()), centerX, centerY + 28, 0xFFC0D0E0);
                }
            } else if (focusedTarget instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity targetLens) {
                double dist = (this.telescopePos != null) ? Math.sqrt(this.telescopePos.distSqr(targetLens.getBlockPos())) : 0.0;
                guiGraphics.drawCenteredString(this.font, "§b✦ TARGET: §fRefractive Astral Lens Relayer  §e|  Distance: §a" + String.format("%.1fm", dist), centerX, centerY + 28, 0xFF80FF80);
            }
        }

        String dirName = getDirectionName(this.yaw);
        int moonPhase = (mc.level != null) ? mc.level.getMoonPhase() : 0;
        String phaseName = getMoonPhaseName(moonPhase);

        String modeTitle = this.isLensMode ? "§6§lREFRACTIVE ASTRAL LENS CALIBRATION" : "§6§lCELESTIAL LOOKING GLASS";
        guiGraphics.drawCenteredString(this.font, modeTitle, centerX, 12, 0xFFFFD700);
        guiGraphics.drawCenteredString(this.font, String.format("§eAzimuth: §f%.1f° %s  §e|  Declination: §f%+.1f°  §e|  Moon: §b%s", this.yaw, dirName, this.pitch, phaseName), centerX, 24, 0xFFE0E0E0);

        if (this.isLensMode) {
            guiGraphics.drawCenteredString(this.font, "§a[Drag to Aim  •  SPACEBAR: Lock Lens on Target  •  ESC to Exit]", centerX, screenHeight - 28, 0xFF80FF80);
        } else if (isShiftDown()) {
            if (this.hoveredEdgeKey != null) {
                guiGraphics.drawCenteredString(this.font, "§c✦ LINE SELECTED: Right-Click to Erase ✦", centerX, screenHeight - 28, 0xFFFF6666);
                guiGraphics.drawCenteredString(this.font, "§7Click & drag between stars to chart lines", centerX, screenHeight - 16, 0xFFA0C0D0);
            } else {
                guiGraphics.drawCenteredString(this.font, "§e✦ SNEAK ACTIVE: VIEW LOCKED ✦", centerX, screenHeight - 28, 0xFFFFD700);
                guiGraphics.drawCenteredString(this.font, "§7Click & drag between stars to chart  •  Hover line + Right-Click to erase", centerX, screenHeight - 16, 0xFFA0C0D0);
            }
        } else {
            guiGraphics.drawCenteredString(this.font, "§8[Click & Drag to pan sky  •  Hold SNEAK to chart lines  •  ESC to exit]", centerX, screenHeight - 18, 0xFF88A0B0);
        }

        // Discovery Fanfare Banner
        if (justDiscovered != null && (System.currentTimeMillis() - discoveryTime) < 5000) {
            String title = Component.translatable(justDiscovered.getUnlocalizedName()).getString();
            int bannerY = centerY - 30;

            int essRgb = (justDiscovered.getEssenceType().getR() << 16) | (justDiscovered.getEssenceType().getG() << 8) | justDiscovered.getEssenceType().getB();
            int borderCol = 0xFF000000 | essRgb;

            guiGraphics.fill(centerX - 150, bannerY, centerX + 150, bannerY + 60, 0xF2101624);
            guiGraphics.fill(centerX - 150, bannerY, centerX + 150, bannerY + 2, borderCol);
            guiGraphics.fill(centerX - 150, bannerY + 58, centerX + 150, bannerY + 60, borderCol);

            guiGraphics.drawCenteredString(this.font, "§6✦ CONSTELLATION DISCOVERED! ✦", centerX, bannerY + 8, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, justDiscovered.getEssenceType().getColorCode() + title + " §7[" + justDiscovered.getTier().getDisplayName() + " • " + justDiscovered.getEssenceType().getFormattedName() + "§7]", centerX, bannerY + 22, 0xFFFFFFFF);
            guiGraphics.drawCenteredString(this.font, "§eRitual: §f" + justDiscovered.getRitualEffect(), centerX, bannerY + 36, 0xFFFFF0A0);
        }

        // Astral Lens Lock Confirmation Banner
        if (lensLockMessage != null && (System.currentTimeMillis() - lensLockTime) < 4000) {
            int bannerY = 40;
            guiGraphics.fill(centerX - 160, bannerY, centerX + 160, bannerY + 20, 0xDD102010);
            guiGraphics.drawCenteredString(this.font, lensLockMessage, centerX, bannerY + 6, 0xFF80FF80);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();

        // Right-Click targeted erase: ONLY erases the selected / hovered line!
        if (button == 1 && this.hoveredEdgeKey != null) {
            String edgeToErase = this.hoveredEdgeKey;
            drawnEdges.remove(edgeToErase);
            dragStarId = null;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null) {
                PlayerAstralProgress.removeChartedConnection(player, edgeToErase);
                NetworkManager.sendToServer(new SyncChartedConnectionsPayload(edgeToErase, SyncChartedConnectionsPayload.ACTION_REMOVE));
                player.playSound(SoundEvents.CHISELED_BOOKSHELF_PICKUP, 0.7f, 1.2f);
            }
            this.hoveredEdgeKey = null;
            return true;
        }

        if (button == 0 && isShiftDown()) {
            for (StarNode node : onScreenStars.values()) {
                if (Math.hypot(mouseX - node.sx(), mouseY - node.sy()) <= Math.max(14.0f, node.size() * 0.7f + 4.0f)) {
                    dragStarId = node.id();
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if (button == 0 && dragStarId != null) {
            StarNode targetNode = null;
            for (StarNode node : onScreenStars.values()) {
                if (!node.id().equals(dragStarId)) {
                    if (Math.hypot(event.x() - node.sx(), event.y() - node.sy()) <= Math.max(16.0f, node.size() * 0.7f + 6.0f)) {
                        targetNode = node;
                        break;
                    }
                }
            }

            if (targetNode != null) {
                String edgeKey = makeEdgeKey(dragStarId, targetNode.id());
                drawnEdges.add(edgeKey);

                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;
                if (player != null) {
                    PlayerAstralProgress.addChartedConnection(player, edgeKey);
                    NetworkManager.sendToServer(new SyncChartedConnectionsPayload(edgeKey, SyncChartedConnectionsPayload.ACTION_ADD));

                    player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.7f, 1.4f);

                    // Check if ANY constellation has all required connections satisfied in drawnEdges
                    for (Constellation c : visibleConstellations) {
                        if (!PlayerAstralProgress.isDiscovered(player, c)) {
                            boolean allSatisfied = true;
                            for (ConstellationConnection conn : c.getConnections()) {
                                String k1 = "c:" + c.getId().toString() + ":" + conn.fromIndex();
                                String k2 = "c:" + c.getId().toString() + ":" + conn.toIndex();
                                if (!drawnEdges.contains(makeEdgeKey(k1, k2))) {
                                    allSatisfied = false;
                                    break;
                                }
                            }

                            if (allSatisfied) {
                                PlayerAstralProgress.discover(player, c);
                                NetworkManager.sendToServer(new ConstellationDiscoveryPayload(c.getId()));

                                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
                                player.playSound(SoundEvents.BEACON_ACTIVATE, 0.8f, 1.2f);

                                this.justDiscovered = c;
                                this.discoveryTime = System.currentTimeMillis();
                            }
                        }
                    }
                }
            }

            dragStarId = null;
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
