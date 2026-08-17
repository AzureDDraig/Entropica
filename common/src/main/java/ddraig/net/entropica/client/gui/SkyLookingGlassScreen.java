package ddraig.net.entropica.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.astral.*;
import ddraig.net.entropica.client.renderer.CelestialSkyRenderer;
import ddraig.net.entropica.item.CompletedStarChartItem;
import ddraig.net.entropica.network.AstralLensAimPayload;
import ddraig.net.entropica.network.TelescopeAimPayload;
import ddraig.net.entropica.registry.ModItems;
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
    private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/star.png");
    private static final ResourceLocation NEBULA_PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/nebula_puff.png");

    private float yaw = 0.0f;
    private float pitch = 45.0f;
    private static final float FOV = 28.0f;
    private static final float PAN_SPEED = 0.045f;

    private List<Constellation> visibleConstellations = new ArrayList<>();
    
    // Universal Star Tracing Graph: Edges formatted as "nodeA---nodeB"
    private final Set<String> drawnEdges = new HashSet<>();

    // Interaction & Tracing state
    private String dragStarId = null;
    private boolean isSneakHeld = false;

    // Track all on-screen stars during the current frame for universal tracing
    public record StarNode(String id, float sx, float sy, float size, String label, EssenceType essence, SpectralClass spectralClass, Constellation constellation, int starIndex) {}
    private final Map<String, StarNode> onScreenStars = new HashMap<>();

    public static class CelestialStar {
        public final float azimuth;
        public final float altitude;
        public final float baseSize;
        public final float rotSpeed;
        public final float twinkleFreq;
        public final float twinklePhase;
        public final SpectralClass spectralClass;
        public final EssenceType essenceType;
        public final String name;
        public final String info;

        public CelestialStar(float azimuth, float altitude, float baseSize, float rotSpeed, float twinkleFreq, float twinklePhase, SpectralClass spectralClass, EssenceType essenceType, String name, String info) {
            this.azimuth = azimuth;
            this.altitude = altitude;
            this.baseSize = baseSize;
            this.rotSpeed = rotSpeed;
            this.twinkleFreq = twinkleFreq;
            this.twinklePhase = twinklePhase;
            this.spectralClass = spectralClass;
            this.essenceType = essenceType;
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
                this.yaw = lens.getYaw();
                this.pitch = lens.getPitch();
            } else if (mc.level.getBlockEntity(targetPos) instanceof ddraig.net.entropica.block.entity.StationaryBrassTelescopeBlockEntity be) {
                this.yaw = be.getYaw();
                this.pitch = be.getPitch();
            }
        } else if (mc.player != null) {
            this.yaw = Mth.wrapDegrees(mc.player.getYRot());
            if (this.yaw < 0) this.yaw += 360.0f;
            this.pitch = Mth.clamp(-mc.player.getXRot(), -35.0f, 88.0f);
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

        // Initialize Major Named Celestial Landmark Stars with authentic Essence Typings
        celestialStars.add(new CelestialStar(25.0f, 55.0f, 14.0f, 12.0f, 2.5f, 0.0f, SpectralClass.CLASS_A, EssenceType.ASTRAL, "Sirius (Alpha Canis)", "Brightest Northern Guide Star | Mag: -1.46m"));
        celestialStars.add(new CelestialStar(78.0f, 72.0f, 12.0f, -8.0f, 3.1f, 1.2f, SpectralClass.CLASS_A, EssenceType.CELESTIAL, "Vega (Alpha Lyrae)", "Stellar Calibration Apex | Mag: 0.03m"));
        celestialStars.add(new CelestialStar(0.0f, 88.5f, 15.0f, 4.0f, 1.8f, 2.4f, SpectralClass.CLASS_F, EssenceType.COHESION, "Polaris (True Celestial Pole)", "True North Anchor | Mag: 1.98m"));
        celestialStars.add(new CelestialStar(145.0f, 38.0f, 14.0f, -15.0f, 2.0f, 3.1f, SpectralClass.CLASS_M, EssenceType.PYRE, "Betelgeuse (Alpha Orionis)", "Pulsing Red Supergiant | Mag: 0.50m"));
        celestialStars.add(new CelestialStar(162.0f, 28.0f, 13.0f, 18.0f, 3.6f, 0.7f, SpectralClass.CLASS_B, EssenceType.GLACIAL, "Rigel (Beta Orionis)", "Radiant Blue Supergiant | Mag: 0.13m"));
        celestialStars.add(new CelestialStar(205.0f, 44.0f, 12.0f, -10.0f, 2.7f, 1.9f, SpectralClass.CLASS_K, EssenceType.AMBER, "Aldebaran (Alpha Tauri)", "Eye of the Cosmic Taurus | Mag: 0.85m"));
        celestialStars.add(new CelestialStar(235.0f, 65.0f, 13.0f, 7.0f, 3.0f, 4.2f, SpectralClass.CLASS_G, EssenceType.RADIANT, "Capella (Alpha Aurigae)", "Quadruple Golden Star | Mag: 0.08m"));
        celestialStars.add(new CelestialStar(285.0f, 22.0f, 14.0f, -12.0f, 2.2f, 5.0f, SpectralClass.CLASS_M, EssenceType.BLOOD, "Antares (Heart of Scorpio)", "Heart of the Void | Mag: 0.96m"));
        celestialStars.add(new CelestialStar(315.0f, 34.0f, 12.0f, 14.0f, 3.4f, 2.8f, SpectralClass.CLASS_B, EssenceType.STORM, "Spica (Alpha Virginis)", "Eclipsing Blue Giant | Mag: 0.97m"));
        celestialStars.add(new CelestialStar(110.0f, 60.0f, 13.0f, -6.0f, 2.8f, 1.5f, SpectralClass.CLASS_A, EssenceType.AETHER, "Deneb (Alpha Cygni)", "Transmutation Vertex | Mag: 1.25m"));
        celestialStars.add(new CelestialStar(190.0f, 52.0f, 16.0f, 5.0f, 4.0f, 0.4f, SpectralClass.CLASS_O, EssenceType.ASTRAL, "Pleiades Astral Cluster", "Open Cosmic Stardust Cluster | Mag: 1.6m"));

        // Generate 260 deterministic ambient background stars with full Essence typings across the celestial sphere (-25° to 90°)
        Random rng = new Random(133742L);
        SpectralClass[] classes = SpectralClass.values();
        EssenceType[] essences = EssenceType.values();
        for (int i = 0; i < 260; i++) {
            float sTheta = rng.nextFloat() * 360.0f;
            float sPhi = -25.0f + rng.nextFloat() * 115.0f;
            float sSize = 5.0f + rng.nextFloat() * 6.5f;
            float sRotSpeed = (rng.nextFloat() - 0.5f) * 30.0f;
            float sFreq = 1.5f + rng.nextFloat() * 3.0f;
            float sPhase = rng.nextFloat() * 6.28f;
            SpectralClass sc = classes[rng.nextInt(classes.length)];
            EssenceType ess = essences[rng.nextInt(essences.length)];
            celestialStars.add(new CelestialStar(sTheta, sPhi, sSize, sRotSpeed, sFreq, sPhase, sc, ess, null, null));
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
        if (this.focusedTarget instanceof CelestialStar cs) {
            targetName = (cs.name != null) ? cs.name : (cs.essenceType.getDisplayName() + " Star");
        } else if (this.focusedTarget instanceof Object[] pair) {
            Constellation c = (Constellation) pair[0];
            targetName = Component.translatable(c.getUnlocalizedName()).getString();
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
        if (mc.player != null && !this.isLensMode) {
            mc.player.setYRot(this.yaw);
            mc.player.setXRot(-this.pitch);
            mc.player.yRotO = this.yaw;
            mc.player.xRotO = -this.pitch;
            mc.player.yHeadRot = this.yaw;
            mc.player.yHeadRotO = this.yaw;
            mc.player.yBodyRot = this.yaw;
            mc.player.yBodyRotO = this.yaw;
        }

        if (this.telescopePos != null) {
            if (this.isLensMode) {
                String targetName = (this.focusedTarget instanceof CelestialStar cs && cs.name != null) ? cs.name : "Calibrated Focus";
                NetworkManager.sendToServer(new AstralLensAimPayload(this.telescopePos, this.yaw, this.pitch, targetName, true));
            } else {
                if (mc.level != null && mc.level.getBlockEntity(this.telescopePos) instanceof ddraig.net.entropica.block.entity.StationaryBrassTelescopeBlockEntity be) {
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
            this.pitch = Mth.clamp(this.pitch + (float) dragY * PAN_SPEED, -35.0f, 88.0f);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    private static String makeEdgeKey(String a, String b) {
        return (a.compareTo(b) < 0) ? (a + "---" + b) : (b + "---" + a);
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

        float celestialAngle = getCelestialAngle(partialTick);

        // 1. Deep Space Cosmic Background
        guiGraphics.fill(lensX, lensY, lensX + size, lensY + size, 0xFF03050D);

        // Raycast Line of Sight check
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        boolean isObstructed = false;
        if (player != null && mc.level != null) {
            Vec3 eyePos = player.getEyePosition(partialTick);
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
                isObstructed = true;
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

            // 3. Render Ambient & Named Celestial Stars (Each with authentic Essence typing)
            for (int idx = 0; idx < celestialStars.size(); idx++) {
                CelestialStar star = celestialStars.get(idx);
                float[] appAngles = celestialToApparentAngles((float) Math.toRadians(star.azimuth), (float) Math.toRadians(star.altitude), celestialAngle);
                float starYaw = appAngles[0];
                float starPitch = appAngles[1];

                float dYaw = Mth.wrapDegrees(starYaw - this.yaw);
                float dPitch = starPitch - this.pitch;

                if (Math.abs(dYaw) <= FOV && Math.abs(dPitch) <= FOV) {
                    float sx = centerX + (dYaw / (FOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (FOV * 0.5f)) * (size * 0.43f);

                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (distFromCenter < lensRadius - 4.0f) {
                        float twinkle = 0.75f + 0.25f * (float) Math.sin(timeSec * star.twinkleFreq + star.twinklePhase);
                        float drawSize = star.baseSize * twinkle;
                        float rotAngle = (timeSec * star.rotSpeed * 20.0f) % 360.0f;

                        String starNodeId = (star.name != null) ? ("l:" + star.name) : ("a:" + idx);
                        onScreenStars.put(starNodeId, new StarNode(starNodeId, sx, sy, drawSize, star.name, star.essenceType, star.spectralClass, null, -1));

                        if (distFromCenter <= 16.0f && this.focusedTarget == null) {
                            this.focusedTarget = star;
                        }

                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().translate(sx, sy);
                        guiGraphics.pose().rotate((float) Math.toRadians(rotAngle));

                        int half = Math.max(2, Math.round(drawSize * 0.5f));
                        guiGraphics.blit(STAR_TEXTURE, -half, -half, half, half, 0.0f, 1.0f, 0.0f, 1.0f);
                        guiGraphics.pose().popMatrix();

                        if (star.name != null && distFromCenter < lensRadius * 0.8f) {
                            guiGraphics.drawString(this.font, "§b✦ §f" + star.name, (int) sx + half + 2, (int) sy - 4, 0xFFE0F0FF, false);
                        }
                    }
                }
            }

            // 4. Render Constellation Nodes (Situated at 48° to 72° Altitude)
            int totalVisible = visibleConstellations.size();

            for (int i = 0; i < totalVisible; i++) {
                Constellation constellation = visibleConstellations.get(i);
                boolean isDiscovered = (player != null) && PlayerAstralProgress.isDiscovered(player, constellation);

                float baseAzimuth = (float) Math.toRadians(i * (360.0f / Math.max(1, totalVisible)));
                float baseAltitude = (float) Math.toRadians(48.0f + ((i * 17) % 24));

                List<ConstellationStar> stars = constellation.getStars();

                for (int s = 0; s < stars.size(); s++) {
                    ConstellationStar star = stars.get(s);
                    float starSphereAzimuth = baseAzimuth + (float) Math.toRadians((50.0f - star.x()) * 0.28f);
                    float starSphereAltitude = baseAltitude + (float) Math.toRadians((star.y() - 50.0f) * 0.28f);

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

                        // Render Natural Star Billets (No giveaway outer aura circles)
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

                // If discovered, auto-add its required connections to drawnEdges
                if (isDiscovered) {
                    for (ConstellationConnection conn : constellation.getConnections()) {
                        String k1 = "c:" + constellation.getId().toString() + ":" + conn.fromIndex();
                        String k2 = "c:" + constellation.getId().toString() + ":" + conn.toIndex();
                        drawnEdges.add(makeEdgeKey(k1, k2));
                    }
                }
            }

            // 5. Universal Star Tracing: Render ALL User-Drawn Lines Between Any Connected Stars
            for (String edge : drawnEdges) {
                String[] parts = edge.split("---");
                if (parts.length == 2) {
                    StarNode nodeA = onScreenStars.get(parts[0]);
                    StarNode nodeB = onScreenStars.get(parts[1]);
                    if (nodeA != null && nodeB != null) {
                        int lineColor = 0xFF00F0FF;
                        if (nodeA.constellation() != null && nodeB.constellation() != null && nodeA.constellation().equals(nodeB.constellation())) {
                            int essRgb = (nodeA.constellation().getEssenceType().getR() << 16) | (nodeA.constellation().getEssenceType().getG() << 8) | nodeA.constellation().getEssenceType().getB();
                            lineColor = 0xFF000000 | essRgb;
                        }
                        drawLine(guiGraphics, (int) nodeA.sx(), (int) nodeA.sy(), (int) nodeB.sx(), (int) nodeB.sy(), lineColor);
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

        // 6. Reticle Crosshairs & Focus Aiming Target
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

        // 7. Render 128x128 Brass Bezel Overlay & Black Letterboxes
        if (lensX > 0) {
            guiGraphics.fill(0, 0, lensX, screenHeight, 0xFF000000);
            guiGraphics.fill(lensX + size, 0, screenWidth, screenHeight, 0xFF000000);
        }
        if (lensY > 0) {
            guiGraphics.fill(0, 0, screenWidth, lensY, 0xFF000000);
            guiGraphics.fill(0, lensY + size, screenWidth, screenHeight, 0xFF000000);
        }

        guiGraphics.blit(OVERLAY_TEXTURE, lensX, lensY, lensX + size, lensY + size, 0.0f, 1.0f, 0.0f, 1.0f);

        // 8. Text & HUD Readouts on top layer (No giveaway labels before charting)
        if (isObstructed) {
            guiGraphics.drawCenteredString(this.font, "§c⚠ LINE OF SIGHT OBSTRUCTED ⚠", centerX, centerY - 12, 0xFFFF5555);
            guiGraphics.drawCenteredString(this.font, "§7View is blocked by solid structure or ceiling", centerX, centerY + 2, 0xFFAAAAAA);
        } else if (focusedTarget != null) {
            if (focusedTarget instanceof CelestialStar cStar) {
                if (cStar.name != null) {
                    guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: §f" + cStar.name + "  §e| Essence: " + cStar.essenceType.getFormattedName() + "  §e|  §b" + cStar.info, centerX, centerY + 28, 0xFFE0F0FF);
                } else {
                    guiGraphics.drawCenteredString(this.font, String.format("§7✦ Stellar Resonance §e| Essence: %s §e| Class: §b%s §e| Mag: §f%.2fm", cStar.essenceType.getFormattedName(), cStar.spectralClass.name(), cStar.baseSize / 10.0f), centerX, centerY + 28, 0xFFC0D0E0);
                }
            } else if (focusedTarget instanceof Object[] pair) {
                Constellation c = (Constellation) pair[0];
                ConstellationStar star = (ConstellationStar) pair[1];
                boolean isDisc = (player != null) && PlayerAstralProgress.isDiscovered(player, c);
                if (isDisc) {
                    String title = Component.translatable(c.getUnlocalizedName()).getString();
                    String essCode = c.getEssenceType().getColorCode();
                    String essFormatted = c.getEssenceType().getFormattedName();
                    guiGraphics.drawCenteredString(this.font, "§6✦ CONSTELLATION NODE: " + essCode + title + " §7[" + c.getTier().getDisplayName() + " • " + essFormatted + "§7]", centerX, centerY + 28, 0xFFFFFFFF);
                } else {
                    // Uncharted constellation star appears with natural stellar resonance info matching all other stars
                    guiGraphics.drawCenteredString(this.font, String.format("§7✦ Stellar Resonance §e| Essence: %s §e| Class: §b%s §e| Mag: §f%.2fm", c.getEssenceType().getFormattedName(), star.spectralClass().name(), star.brightness()), centerX, centerY + 28, 0xFFC0D0E0);
                }
            }
        }

        String dirName = getDirectionName(this.yaw);
        int moonPhase = (mc.level != null) ? mc.level.getMoonPhase() : 0;
        String phaseName = getMoonPhaseName(moonPhase);

        String modeTitle = this.isLensMode ? "§6§lREFRACTIVE ASTRAL LENS CALIBRATION" : "§6§lCELESTIAL LOOKING GLASS";
        guiGraphics.drawCenteredString(this.font, modeTitle, centerX, 12, 0xFFFFD700);
        guiGraphics.drawCenteredString(this.font, String.format("§eAzimuth: §f%.1f° %s  §e|  Declination: §f%+.1f°  §e|  Moon: §b%s", this.yaw, dirName, this.pitch, phaseName), centerX, 24, 0xFFE0E0E0);

        if (this.isLensMode) {
            guiGraphics.drawCenteredString(this.font, "§a[SPACEBAR: Lock Lens on Current Target  •  Drag to Pan]", centerX, screenHeight - 28, 0xFF80FF80);
        } else if (isShiftDown()) {
            guiGraphics.drawCenteredString(this.font, "§e✦ SNEAK ACTIVE: VIEW LOCKED ✦", centerX, screenHeight - 28, 0xFFFFD700);
            guiGraphics.drawCenteredString(this.font, "§7Click & drag between ANY stars to trace  •  Right-Click to reset lines", centerX, screenHeight - 16, 0xFFA0C0D0);
        } else {
            guiGraphics.drawCenteredString(this.font, "§8[Click & Drag to pan sky  •  Aim crosshairs to inspect stars  •  Hold SNEAK to trace  •  ESC to exit]", centerX, screenHeight - 18, 0xFF88A0B0);
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

        if (button == 1 && isShiftDown()) {
            drawnEdges.clear();
            dragStarId = null;
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.CHISELED_BOOKSHELF_PICKUP, 0.7f, 0.9f);
            }
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

        if (button == 0 && isLensMode) {
            lockAstralLensFocus();
            return true;
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
                                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
                                player.playSound(SoundEvents.BEACON_ACTIVATE, 0.8f, 1.2f);

                                this.justDiscovered = c;
                                this.discoveryTime = System.currentTimeMillis();

                                for (int inv = 0; inv < player.getInventory().getContainerSize(); inv++) {
                                    ItemStack stack = player.getInventory().getItem(inv);
                                    if (stack.is(ModItems.STAR_CHART_BLANK.get())) {
                                        stack.shrink(1);
                                        ItemStack completedChart = CompletedStarChartItem.createFor(ModItems.STAR_CHART_COMPLETED.get(), c);
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
            guiGraphics.fill(cx - x, cy + y, cx - x + 1, cy + x + 1, color);
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
