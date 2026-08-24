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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class SkyLookingGlassScreen extends Screen {

    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/gui/looking_glass_overlay.png");
    private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/star.png");
    private static final ResourceLocation COMET_HEAD_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/comet_head.png");
    private static final ResourceLocation METEOR_TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/meteor_trail.png");
    private static final ResourceLocation NEBULA_PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/nebula_puff.png");
    private static final ResourceLocation SUPERNOVA_RING_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/supernova_ring.png");

    private float yaw = 0.0f;
    private float pitch = 45.0f;
    private static final float BASE_FOV = 28.0f;
    private float zoom = 1.0f;
    private static final float PAN_SPEED = 0.045f;

    public enum InstrumentType {
        LOOKING_GLASS(4.0f, 28.0f, "HANDHELD LOOKING GLASS"),
        ASTROLABE(8.0f, 28.0f, "BRASS NAVIGATION ASTROLABE"),
        TELESCOPE(16.0f, 28.0f, "STATIONARY BRASS TELESCOPE"),
        ARMILLARY(32.0f, 28.0f, "GRAND OBSERVATORY ARMILLARY"),
        LENS(8.0f, 28.0f, "REFRACTIVE ASTRAL LENS");

        public final float maxZoom;
        public final float baseFOV;
        public final String displayName;

        InstrumentType(float maxZoom, float baseFOV, String displayName) {
            this.maxZoom = maxZoom;
            this.baseFOV = baseFOV;
            this.displayName = displayName;
        }
    }

    private InstrumentType instrument = InstrumentType.LOOKING_GLASS;

    public int getInstrumentOpticTier() {
        if (this.instrument == InstrumentType.ARMILLARY) return 4;
        if (this.instrument == InstrumentType.TELESCOPE) return 3;
        if (this.instrument == InstrumentType.ASTROLABE || this.instrument == InstrumentType.LENS) return 2;
        return 1;
    }

    public float getFOV() {
        return instrument.baseFOV / Math.max(1.0f, this.zoom);
    }

    private List<Constellation> visibleConstellations = new ArrayList<>();
    
    // Universal Star Tracing Graph: Edges formatted as "nodeA---nodeB"
    private final Set<String> drawnEdges = Collections.synchronizedSet(new HashSet<>());

    // Interaction & Tracing state
    private String dragStarId = null;
    private boolean isSneakHeld = false;
    private String hoveredEdgeKey = null;

    // Track all on-screen stars during the current frame for universal tracing
    public record StarNode(String id, float sx, float sy, float size, String label, EssenceType essence, SpectralClass spectralClass, Constellation constellation, int starIndex, boolean canSelect) {}
    private final Map<String, StarNode> onScreenStars = new HashMap<>();

    // Reticle Focus Aiming State
    private Object focusedTarget = null;
    private Object lastFocusedTarget = null;

    // Last discovered banner notification
    private Constellation justDiscovered = null;
    private long discoveryTime = 0;
    private long bannerDisplayStartTime = 0;

    // Reticle calibration feedback
    private String lensLockMessage = null;
    private long lensLockTime = 0;

    private BlockPos telescopePos = null;
    private boolean isLensMode = false;

    // Smooth Auto-Pan State
    private boolean isPanning = false;
    private float panStartYaw = 0.0f;
    private float panStartPitch = 0.0f;
    private float panTargetYaw = 0.0f;
    private float panTargetPitch = 0.0f;
    private long panStartTime = 0L;
    private static final long PAN_DURATION_MS = 1200L;

    public void panTo(float targetYaw, float targetPitch) {
        this.isPanning = true;
        this.panStartYaw = this.yaw;
        this.panStartPitch = this.pitch;
        this.panTargetYaw = targetYaw;
        this.panTargetPitch = Mth.clamp(targetPitch, -85.0f, 85.0f);
        this.panStartTime = System.currentTimeMillis();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.CHISELED_BOOKSHELF_INSERT, 0.6f, 1.4f);
        }
    }

    public SkyLookingGlassScreen() {
        this(null, false);
    }

    public SkyLookingGlassScreen(BlockPos targetPos, boolean isLens) {
        super(Component.literal("Celestial Looking Glass"));
        this.telescopePos = targetPos;
        this.isLensMode = isLens;

        Minecraft mc = Minecraft.getInstance();

        if (isLens) {
            this.instrument = InstrumentType.LENS;
        } else if (targetPos != null && mc.level != null) {
            if (mc.level.getBlockState(targetPos).getBlock() instanceof ddraig.net.entropica.block.CelestialArmillaryControllerBlock) {
                this.instrument = InstrumentType.ARMILLARY;
            } else {
                this.instrument = InstrumentType.TELESCOPE;
            }
        } else if (mc.player != null && (mc.player.getMainHandItem().is(ddraig.net.entropica.registry.ModItems.ASTROLABE.get()) || mc.player.getOffhandItem().is(ddraig.net.entropica.registry.ModItems.ASTROLABE.get()))) {
            this.instrument = InstrumentType.ASTROLABE;
        } else {
            this.instrument = InstrumentType.LOOKING_GLASS;
        }

        if (targetPos != null && mc.level != null) {
            if (isLens && mc.level.getBlockEntity(targetPos) instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity lens) {
                if (lens.isFocused()) {
                    this.yaw = lens.getYaw();
                    this.pitch = lens.getPitch();
                } else if (mc.player != null) {
                    this.yaw = Mth.wrapDegrees(mc.player.getYRot());
                    if (this.yaw < 0) this.yaw += 360.0f;
                    this.pitch = Mth.clamp(-mc.player.getXRot(), -85.0f, 90.0f);
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
            this.pitch = Mth.clamp(-mc.player.getXRot(), -85.0f, 90.0f);
        }
    }

    @Override
    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();

        this.visibleConstellations = new ArrayList<>(ModConstellations.getAllConstellations());

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

    public static void openForArmillary(BlockPos pos) {
        SkyLookingGlassScreen screen = new SkyLookingGlassScreen(pos, false);
        screen.instrument = InstrumentType.ARMILLARY;
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openForInstrument(InstrumentType type) {
        SkyLookingGlassScreen screen = new SkyLookingGlassScreen(null, false);
        screen.instrument = type;
        Minecraft.getInstance().setScreen(screen);
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
            return mc.level.dimension().equals(Level.END) ? ((mc.level.getGameTime() + partialTick) * 0.02f) : ((mc.level.getTimeOfDay(partialTick) * 360.0f) + 180.0f);
        }
        return 0.0f;
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
        Minecraft mc = Minecraft.getInstance();

        if (this.focusedTarget == null) {
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.DISPENSER_FAIL, 0.8f, 1.2f);
            }
            this.lensLockMessage = "§c✦ Calibrate Failed: Crosshair must be aimed at a Star or Astral Lens!";
            this.lensLockTime = System.currentTimeMillis();
            return;
        }

        if (this.focusedTarget instanceof CelestialEventHelper.CometDefinition ||
            this.focusedTarget instanceof CelestialEventHelper.PlanetDefinition) {
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.DISPENSER_FAIL, 0.8f, 1.2f);
            }
            this.lensLockMessage = "§c✦ Calibrate Failed: Astral Lenses cannot lock onto dynamic planets, comets, or meteors!";
            this.lensLockTime = System.currentTimeMillis();
            return;
        }

        String targetName = "Unknown Celestial Body";
        if (this.focusedTarget instanceof SupernovaManager.OverriddenStar ov) {
            targetName = ov.name();
        } else if (this.focusedTarget instanceof CelestialStarHelper.LandmarkStar ls) {
            targetName = ls.name();
        } else if (this.focusedTarget instanceof CelestialStarHelper.AmbientStar as) {
            targetName = as.name();
        } else if (this.focusedTarget instanceof CelestialEventHelper.ActiveSupernovaState sn) {
            targetName = sn.event().name();
        } else if (this.focusedTarget instanceof Object[] pair) {
            Constellation c = (Constellation) pair[0];
            ConstellationStar star = (ConstellationStar) pair[1];
            targetName = CelestialStarHelper.getConstellationStarName(c, c.getStars().indexOf(star));
        } else if (this.focusedTarget instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity targetLens) {
            targetName = "Relay Lens at [" + targetLens.getBlockPos().toShortString() + "]";
        }

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
            this.isPanning = false;
            this.yaw = Mth.wrapDegrees(this.yaw - (float) dragX * PAN_SPEED);
            if (this.yaw < 0) this.yaw += 360.0f;
            this.pitch = Mth.clamp(this.pitch + (float) dragY * PAN_SPEED, -85.0f, 90.0f);
            syncPlayerRotation();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            float oldZoom = this.zoom;
            this.zoom = Mth.clamp(this.zoom + (float) scrollY * 0.35f, 1.0f, instrument.maxZoom);
            if (this.zoom != oldZoom && minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.SPYGLASS_USE, 0.35f, 1.1f + (this.zoom / instrument.maxZoom) * 0.5f);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

        // Smooth Auto-Pan Interpolation
        if (this.isPanning) {
            long elapsed = System.currentTimeMillis() - this.panStartTime;
            float progress = Mth.clamp((float) elapsed / (float) PAN_DURATION_MS, 0.0f, 1.0f);
            // Smooth cubic ease-out curve: 1 - (1 - p)^3
            float ease = (float) (1.0 - Math.pow(1.0 - progress, 3));

            float dYaw = Mth.wrapDegrees(this.panTargetYaw - this.panStartYaw);
            this.yaw = Mth.wrapDegrees(this.panStartYaw + dYaw * ease);
            if (this.yaw < 0) this.yaw += 360.0f;

            this.pitch = Mth.clamp(this.panStartPitch + (this.panTargetPitch - this.panStartPitch) * ease, -85.0f, 85.0f);

            if (progress >= 1.0f) {
                this.isPanning = false;
                this.yaw = this.panTargetYaw;
                if (this.yaw < 0) this.yaw += 360.0f;
                this.pitch = this.panTargetPitch;
            }
            syncPlayerRotation();
        }

        float currentFOV = getFOV();
        float celestialAngle = getCelestialAngle(partialTick);

        // 1. Deep Space Cosmic Background
        guiGraphics.fill(lensX, lensY, lensX + size, lensY + size, 0xFF03050D);

        // Raycast Line of Sight check: Start above telescope/lens block to prevent self-collision
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        boolean isObstructed = false;
        float effectiveYaw = this.yaw;
        float effectivePitch = this.pitch;

        if (player != null && mc.level != null) {
            if (this.instrument == InstrumentType.ARMILLARY) {
                // Grand Observatory Optical Invariance: Dome apertures, lenses, and multiblock blocks do not obstruct celestial sightlines
                BlockPos checkSkyPos = (this.telescopePos != null) ? this.telescopePos.above(9) : player.blockPosition().above(9);
                if (!mc.level.canSeeSky(checkSkyPos) && !mc.level.getBlockState(checkSkyPos).isAir() && mc.level.getBlockState(checkSkyPos).isSolid()) {
                    isObstructed = true;
                }
            } else {
                Vec3 eyePos = (this.telescopePos != null)
                        ? Vec3.atCenterOf(this.telescopePos).add(0, 0.75, 0)
                        : player.getEyePosition(partialTick);

                float yawRad = (float) Math.toRadians(this.yaw);
                float pitchRad = (float) Math.toRadians(-this.pitch);
                Vec3 lookDir = new Vec3(
                        -Mth.sin(yawRad) * Mth.cos(pitchRad),
                        -Mth.sin(pitchRad),
                        Mth.cos(yawRad) * Mth.cos(pitchRad)
                ).normalize();

                Vec3 curPos = eyePos;
                Vec3 curDir = lookDir;

                for (int bounce = 0; bounce < 16; bounce++) {
                    Vec3 traceEnd = curPos.add(curDir.scale(256.0));
                    BlockHitResult hit = mc.level.clip(new ClipContext(curPos, traceEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

                    if (hit.getType() == HitResult.Type.MISS) {
                        // Ray escaped into empty air / sky without obstruction!
                        break;
                    }

                    BlockPos hitPos = hit.getBlockPos();
                    if (bounce == 0 && this.telescopePos != null && (hitPos.equals(this.telescopePos) || hitPos.equals(this.telescopePos.below()))) {
                        curPos = hit.getLocation().add(curDir.scale(0.05));
                        continue;
                    }

                    BlockState hitState = mc.level.getBlockState(hitPos);
                    if (hitState.getBlock() instanceof ddraig.net.entropica.block.AstralMirrorBlock) {
                        Direction hitFace = hit.getDirection();
                        Vec3 normal = Vec3.atLowerCornerOf(hitFace.getUnitVec3i());
                        double dot = curDir.dot(normal);
                        if (dot < -0.0001) { // Ray enters the front of this mirror face
                            curDir = curDir.subtract(normal.scale(2.0 * dot)).normalize();
                            curPos = hit.getLocation().add(normal.scale(0.05)); // Offset cleanly along face normal
                        } else {
                            // Grazing angle or seam transition into adjacent mirror block: step through
                            curPos = hit.getLocation().add(curDir.scale(0.05));
                        }
                    } else if (hitState.isAir() || !hitState.isSolid() || !hitState.blocksMotion()) {
                        // Pass through non-solid blocks
                        curPos = hit.getLocation().add(curDir.scale(0.05));
                    } else {
                        // If ray has bounced off a mirror and travels upwards towards open sky
                        if (bounce > 0 && curDir.y > 0.05 && mc.level.canSeeSky(hitPos.above())) {
                            break;
                        }
                        // Hit solid opaque obstacle
                        isObstructed = true;
                        break;
                    }
                }

                if (!isObstructed) {
                    // Convert exiting ray direction to true apparent effective spherical angles
                    effectivePitch = (float) Math.toDegrees(Math.asin(Mth.clamp(curDir.y, -1.0, 1.0)));
                    effectiveYaw = (float) Math.toDegrees(Math.atan2(-curDir.x, curDir.z));
                    if (effectiveYaw < 0) effectiveYaw += 360.0f;
                }
            }
        }

        if (!isObstructed) {
            // 2. Render Organic Multi-Puff Nebulae Clouds & Dark Dust Rifts using nebula_puff.png
            for (CelestialSkyRenderer.NebulaComplex complex : CelestialSkyRenderer.NEBULA_COMPLEXES) {
                for (CelestialSkyRenderer.NebulaPuff puff : complex.puffs) {
                    float azimDeg = complex.baseAzim + puff.dAzim();
                    float altDeg = complex.baseAlt + puff.dAlt();

                    float[] app = celestialToApparentAngles((float) Math.toRadians(azimDeg), (float) Math.toRadians(altDeg), celestialAngle);
                    float dYaw = Mth.wrapDegrees(app[0] - effectiveYaw);
                    float dPitch = app[1] - effectivePitch;

                    if (Math.abs(dYaw) <= currentFOV + 15.0f && Math.abs(dPitch) <= currentFOV + 15.0f) {
                        float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                        float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);

                        float zoomNebulaScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.70f;
                        int pSize = (int) (puff.size() * (size / 130.0f) * zoomNebulaScale);

                        float breathing = 0.80f + 0.20f * (float) Math.sin(timeSec * 0.5f + puff.phase());
                        float rotAngle = (timeSec * 2.0f * puff.rotSpeed() + puff.phase() * 10.0f) % 360.0f;

                        renderTintedTexturedQuad(guiGraphics, NEBULA_PUFF_TEXTURE, sx, sy, pSize, rotAngle, puff.r(), puff.g(), puff.b(), puff.a() * breathing);
                    }
                }
            }

            // 2.5. Render Supernova Figure-8 Bipolar Nebulae in Telescopic View
            for (SupernovaEvent se : SupernovaManager.getAllEvents()) {
                if (se.phase() == SupernovaPhase.EXPANDING_NEBULA || se.phase() == SupernovaPhase.REMNANT || se.phase() == SupernovaPhase.FLASH) {
                    List<SupernovaManager.FigureEightPuff> puffs = SupernovaManager.getFigureEightPuffs(se);
                    for (SupernovaManager.FigureEightPuff puff : puffs) {
                        float azimDeg = se.azimuthDeg() + puff.dAzim();
                        float altDeg = se.altitudeDeg() + puff.dAlt();

                        float[] app = celestialToApparentAngles((float) Math.toRadians(azimDeg), (float) Math.toRadians(altDeg), celestialAngle);
                        float dYaw = Mth.wrapDegrees(app[0] - effectiveYaw);
                        float dPitch = app[1] - effectivePitch;

                        if (Math.abs(dYaw) <= currentFOV + 15.0f && Math.abs(dPitch) <= currentFOV + 15.0f) {
                            float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                            float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);

                            float zoomSnScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.75f;
                            int pSize = (int) (puff.size() * (size / 110.0f) * zoomSnScale);
                            float breathing = 0.82f + 0.18f * (float) Math.sin(timeSec * 0.5f + puff.phase());
                            float rotAngle = (timeSec * 2.0f * puff.rotSpeed() + puff.phase() * 10.0f) % 360.0f;

                            renderTintedTexturedQuad(guiGraphics, NEBULA_PUFF_TEXTURE, sx, sy, pSize, rotAngle, puff.r(), puff.g(), puff.b(), puff.a() * breathing);
                        }
                    }
                }
            }

            // 3. Render Ambient Stars with Slow Shifting RGB & Per-Tier Dimming
            for (int idx = 0; idx < CelestialStarHelper.AMBIENT_STARS.size(); idx++) {
                CelestialStarHelper.AmbientStar star = CelestialStarHelper.AMBIENT_STARS.get(idx);

                float[] appAngles = celestialToApparentAngles((float) Math.toRadians(star.azimuth()), (float) Math.toRadians(star.altitude()), celestialAngle);
                float starYaw = appAngles[0];
                float starPitch = appAngles[1];

                float dYaw = Mth.wrapDegrees(starYaw - effectiveYaw);
                float dPitch = starPitch - effectivePitch;

                if (Math.abs(dYaw) <= currentFOV && Math.abs(dPitch) <= currentFOV) {
                    float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);

                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (distFromCenter < lensRadius - 4.0f) {
                        SupernovaManager.OverriddenStar ov = SupernovaManager.getOverriddenStar("a:" + idx);
                        SpectralClass starSc = (ov != null) ? ov.spectralClass() : star.spectralClass();
                        EssenceType starEss = (ov != null) ? ov.essenceType() : star.essenceType();
                        String starName = (ov != null) ? ov.name() : star.name();
                        float starBaseSize = (ov != null) ? ov.size() : star.size();

                        float zoomAmbScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.65f;
                        float twinkle = 0.75f + 0.25f * (float) Math.sin(timeSec * 2.2f + star.azimuth());
                        float drawSize = starBaseSize * twinkle * zoomAmbScale;
                        float rotAngle = (timeSec * 20.0f + idx * 15.0f) % 360.0f;

                        float ambTierDimmer = switch (star.minTier()) {
                            case 1 -> 1.0f;
                            case 2 -> 0.80f;
                            case 3 -> 0.60f;
                            case 4 -> 0.40f;
                            default -> 1.0f;
                        };

                        String starNodeId = "a:" + idx;
                        onScreenStars.put(starNodeId, new StarNode(starNodeId, sx, sy, drawSize, starName, starEss, starSc, null, -1, true));

                        if (distFromCenter <= 16.0f && this.focusedTarget == null) {
                            this.focusedTarget = (ov != null) ? ov : star;
                        }

                        float[] rgb = CelestialStarHelper.getShiftingStarRGB(starSc, starEss, timeSec, star.azimuth());
                        if (ov != null && ov.remnantType() == StellarRemnantType.BLACK_HOLE && ov.phase() == SupernovaPhase.REMNANT) {
                            // Swirling accretion disk
                            renderTintedTexturedQuad(guiGraphics, STAR_TEXTURE, sx, sy, drawSize * 1.6f, rotAngle * 1.5f, rgb[0] * 1.5f, rgb[1] * 1.2f, rgb[2] * 1.8f, 1.0f);
                            // Event horizon silhouette
                            renderTintedTexturedQuad(guiGraphics, STAR_TEXTURE, sx, sy, drawSize * 0.7f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
                        } else {
                            renderTintedTexturedQuad(guiGraphics, STAR_TEXTURE, sx, sy, drawSize, rotAngle, rgb[0] * ambTierDimmer, rgb[1] * ambTierDimmer, rgb[2] * ambTierDimmer, ambTierDimmer);
                        }

                        if (ov != null && distFromCenter < lensRadius * 0.8f) {
                            int nameCol = CelestialStarHelper.getShiftingStarRgbInt(starSc, starEss, timeSec, star.azimuth());
                            int half = Math.max(3, Math.round(drawSize * 0.5f));
                            guiGraphics.drawString(this.font, "§d✦ §f" + ov.name(), (int) sx + half + 2, (int) sy - 4, 0xFF000000 | nameCol, false);
                        }
                    }
                }
            }

            // 4. Render Landmark Guide Stars with Slow Shifting RGB
            for (CelestialStarHelper.LandmarkStar ls : CelestialStarHelper.LANDMARK_STARS) {
                float[] appAngles = celestialToApparentAngles((float) Math.toRadians(ls.azimuth()), (float) Math.toRadians(ls.altitude()), celestialAngle);
                float starYaw = appAngles[0];
                float starPitch = appAngles[1];

                float dYaw = Mth.wrapDegrees(starYaw - effectiveYaw);
                float dPitch = starPitch - effectivePitch;

                if (Math.abs(dYaw) <= currentFOV && Math.abs(dPitch) <= currentFOV) {
                    float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);

                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (distFromCenter < lensRadius - 4.0f) {
                        SupernovaManager.OverriddenStar ov = SupernovaManager.getOverriddenStar("l:" + ls.name());
                        SpectralClass starSc = (ov != null) ? ov.spectralClass() : ls.spectralClass();
                        EssenceType starEss = (ov != null) ? ov.essenceType() : ls.essenceType();
                        String starName = (ov != null) ? ov.name() : ls.name();
                        float starBaseSize = (ov != null) ? ov.size() : ls.size();

                        float zoomLmScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.80f;
                        float twinkle = 0.85f + 0.15f * (float) Math.sin(timeSec * 3.0f + ls.azimuth());
                        float drawSize = starBaseSize * twinkle * zoomLmScale;
                        float rotAngle = (timeSec * 15.0f) % 360.0f;

                        String starNodeId = "l:" + ls.name();
                        onScreenStars.put(starNodeId, new StarNode(starNodeId, sx, sy, drawSize, starName, starEss, starSc, null, -1, true));

                        if (distFromCenter <= 16.0f && this.focusedTarget == null) {
                            this.focusedTarget = (ov != null) ? ov : ls;
                        }

                        float[] rgb = CelestialStarHelper.getShiftingStarRGB(starSc, starEss, timeSec, ls.azimuth());
                        if (ov != null && ov.remnantType() == StellarRemnantType.BLACK_HOLE && ov.phase() == SupernovaPhase.REMNANT) {
                            renderTintedTexturedQuad(guiGraphics, STAR_TEXTURE, sx, sy, drawSize * 1.6f, rotAngle * 1.5f, rgb[0] * 1.5f, rgb[1] * 1.2f, rgb[2] * 1.8f, 1.0f);
                            renderTintedTexturedQuad(guiGraphics, STAR_TEXTURE, sx, sy, drawSize * 0.7f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
                        } else {
                            renderTintedTexturedQuad(guiGraphics, STAR_TEXTURE, sx, sy, drawSize, rotAngle, rgb[0], rgb[1], rgb[2], 1.0f);
                        }

                        if (distFromCenter < lensRadius * 0.8f) {
                            int nameCol = CelestialStarHelper.getShiftingStarRgbInt(starSc, starEss, timeSec, ls.azimuth());
                            int half = Math.max(3, Math.round(drawSize * 0.5f));
                            guiGraphics.drawString(this.font, "§b✦ §f" + starName, (int) sx + half + 2, (int) sy - 4, 0xFF000000 | nameCol, false);
                        }
                    }
                }
            }

            // 5. Render All Constellation Nodes with Slow Shifting RGB & Per-Tier Dimming
            for (Constellation constellation : visibleConstellations) {
                boolean isDiscovered = (player != null) && PlayerAstralProgress.isDiscovered(player, constellation);

                List<ConstellationStar> stars = constellation.getStars();
                float tierDimmer = switch (constellation.getTier()) {
                    case FUNDAMENTAL -> 1.0f;
                    case ADVANCED -> 0.82f;
                    case MASTER -> 0.65f;
                    case MYTHIC -> 0.48f;
                    case TRANSCENDENT -> 0.35f;
                };
                boolean canSelectConst = PlayerAstralProgress.canHardwareObserve(getInstrumentOpticTier(), constellation.getTier());

                for (int s = 0; s < stars.size(); s++) {
                    ConstellationStar star = stars.get(s);
                    float starSphereAzimuth = constellation.getStarSphereAzimuth(s);
                    float starSphereAltitude = constellation.getStarSphereAltitude(s);

                    float[] appAngles = celestialToApparentAngles(starSphereAzimuth, starSphereAltitude, celestialAngle);
                    float starYaw = appAngles[0];
                    float starPitch = appAngles[1];

                    float dYaw = Mth.wrapDegrees(starYaw - effectiveYaw);
                    float dPitch = starPitch - effectivePitch;

                    float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);

                    float dist = (float) Math.hypot(sx - centerX, sy - centerY);
                    if (dist < lensRadius - 4.0f) {
                        String cNodeId = "c:" + constellation.getId().toString() + ":" + s;
                        float zoomConstScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.85f;
                        float twinkle = 0.85f + 0.15f * (float) Math.sin(timeSec * 3.0f + s * 1.2f);
                        float sSize = Math.max(6.5f, star.brightness() * 7.5f) * twinkle * zoomConstScale;

                        onScreenStars.put(cNodeId, new StarNode(cNodeId, sx, sy, sSize, null, constellation.getEssenceType(), star.spectralClass(), constellation, s, true));

                        if (dist <= 16.0f && this.focusedTarget == null) {
                            this.focusedTarget = new Object[]{constellation, star};
                        }

                        float[] rgb = CelestialStarHelper.getShiftingStarRGB(star.spectralClass(), constellation.getEssenceType(), timeSec, (float) (star.x() + star.y()));
                        renderTintedTexturedQuad(guiGraphics, STAR_TEXTURE, sx, sy, sSize, (timeSec * 15.0f + s * 30.0f) % 360.0f, rgb[0] * tierDimmer, rgb[1] * tierDimmer, rgb[2] * tierDimmer, tierDimmer);

                        if (Math.hypot(mouseX - sx, mouseY - sy) <= Math.max(10.0f, sSize * 0.8f) && isShiftDown()) {
                            int half = Math.max(2, Math.round(sSize * 0.5f));
                            drawCircle(guiGraphics, (int) sx, (int) sy, half + 3, 0x88FFFFFF);
                        }
                    }
                }
            }

            // 5.2. Render Comets with Sweeping Ion & Dust Tails in Telescopic View
            long gameTime = (mc.level != null) ? mc.level.getGameTime() : 0;
            for (CelestialEventHelper.CometDefinition comet : CelestialEventHelper.COMETS) {
                if (comet.minTier() > getInstrumentOpticTier()) continue;

                float azim = (comet.baseAzim() + (gameTime + partialTick) * comet.orbitalSpeed() * 0.001f * 360.0f) % 360.0f;
                float alt = comet.baseAlt();

                int segments = 24;
                float prevX = 0, prevY = 0;
                boolean hasPrev = false;

                float dir = (comet.orbitalSpeed() >= 0.0f) ? 1.0f : -1.0f;
                for (int seg = 0; seg <= segments; seg++) {
                    float segT = seg / (float) segments;
                    float tailAzim = azim - dir * (segT * comet.tailLengthDeg()) + dir * Mth.sin(segT * (float) Math.PI) * comet.tailCurvature();
                    float tailAlt = Mth.clamp(alt - segT * (comet.tailLengthDeg() * 0.25f), 5.0f, 88.0f);

                    float[] app = celestialToApparentAngles((float) Math.toRadians(tailAzim), (float) Math.toRadians(tailAlt), celestialAngle);
                    float dYaw = Mth.wrapDegrees(app[0] - effectiveYaw);
                    float dPitch = app[1] - effectivePitch;

                    if (Math.abs(dYaw) <= currentFOV + 12.0f && Math.abs(dPitch) <= currentFOV + 12.0f) {
                        float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                        float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);

                        if (hasPrev) {
                            float tailFade = (1.0f - segT);
                            int r = (int) (comet.tailR() * 255);
                            int g = (int) (comet.tailG() * 255);
                            int b = (int) (comet.tailB() * 255);
                            int a = (int) (tailFade * 210);
                            int col = (a << 24) | (r << 16) | (g << 8) | b;
                            drawLine(guiGraphics, (int) prevX, (int) prevY, (int) sx, (int) sy, col);
                            drawLine(guiGraphics, (int) prevX, (int) prevY + 1, (int) sx, (int) sy + 1, ((a / 2) << 24) | (r << 16) | (g << 8) | b);
                        }
                        prevX = sx;
                        prevY = sy;
                        hasPrev = true;
                    } else {
                        hasPrev = false;
                    }
                }

                // Render Comet Nucleus / Coma
                float[] appHead = celestialToApparentAngles((float) Math.toRadians(azim), (float) Math.toRadians(alt), celestialAngle);
                float dYaw = Mth.wrapDegrees(appHead[0] - effectiveYaw);
                float dPitch = appHead[1] - effectivePitch;

                if (Math.abs(dYaw) <= currentFOV && Math.abs(dPitch) <= currentFOV) {
                    float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);
                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);

                    if (distFromCenter < lensRadius - 4.0f) {
                        float zoomCometScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.75f;
                        float comaSize = comet.comaSize() * (size / 220.0f) * zoomCometScale;
                        float breathing = 0.88f + 0.12f * (float) Math.sin(timeSec * 2.5f);
                        renderTintedTexturedQuad(guiGraphics, COMET_HEAD_TEXTURE, sx, sy, comaSize * breathing, timeSec * 5.0f, comet.r(), comet.g(), comet.b(), 1.0f);

                        if (!this.isLensMode && distFromCenter <= 18.0f && this.focusedTarget == null) {
                            this.focusedTarget = comet;
                        }

                        if (distFromCenter < lensRadius * 0.8f) {
                            int col = ((int) (comet.r() * 255) << 16) | ((int) (comet.g() * 255) << 8) | ((int) (comet.b() * 255));
                            guiGraphics.drawString(this.font, "§b☄ §f" + comet.name(), (int) sx + 14, (int) sy - 4, 0xFF000000 | col, false);
                        }
                    }
                }
            }

            // 5.3. Render Telescopic Meteor Swarms / Clusters (Group of 3-5 companion shooting stars)
            List<CelestialEventHelper.ActiveMeteor> telescopicMeteors = CelestialEventHelper.getTelescopicMeteorCluster(gameTime, partialTick);
            for (CelestialEventHelper.ActiveMeteor m : telescopicMeteors) {
                int segments = 16;
                float trailLen = 0.40f;
                float tHead = m.progress();
                float tTail = Math.max(0.0f, tHead - trailLen);

                float prevX = 0, prevY = 0;
                boolean hasPrev = false;

                for (int seg = 0; seg <= segments; seg++) {
                    float segParam = tTail + (tHead - tTail) * (seg / (float) segments);
                    org.joml.Vector3f p = CelestialEventHelper.getMeteorPosAt(m, segParam);

                    float pLen = p.length();
                    float alt = (float) Math.toDegrees(Math.asin(Mth.clamp(p.y / pLen, -1.0f, 1.0f)));
                    float azim = (float) Math.toDegrees(Math.atan2(p.x, p.z));
                    if (azim < 0) azim += 360.0f;

                    float[] app = celestialToApparentAngles((float) Math.toRadians(azim), (float) Math.toRadians(alt), celestialAngle);
                    float dYaw = Mth.wrapDegrees(app[0] - effectiveYaw);
                    float dPitch = app[1] - effectivePitch;

                    if (Math.abs(dYaw) <= currentFOV + 12.0f && Math.abs(dPitch) <= currentFOV + 12.0f) {
                        float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                        float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);

                        if (hasPrev) {
                            float trailFade = seg / (float) segments;
                            int r = (int) (m.r() * 255);
                            int g = (int) (m.g() * 255);
                            int b = (int) (m.b() * 255);
                            int a = (int) (m.intensity() * trailFade * 240);
                            int col = (a << 24) | (r << 16) | (g << 8) | b;
                            drawLine(guiGraphics, (int) prevX, (int) prevY, (int) sx, (int) sy, col);
                            drawLine(guiGraphics, (int) prevX, (int) prevY + 1, (int) sx, (int) sy + 1, ((a / 2) << 24) | (r << 16) | (g << 8) | b);
                        }
                        prevX = sx;
                        prevY = sy;
                        hasPrev = true;
                    } else {
                        hasPrev = false;
                    }
                }

                // Render Meteor Head
                org.joml.Vector3f headP = CelestialEventHelper.getMeteorPosAt(m, m.progress());
                float headLen = headP.length();
                float hAlt = (float) Math.toDegrees(Math.asin(Mth.clamp(headP.y / headLen, -1.0f, 1.0f)));
                float hAzim = (float) Math.toDegrees(Math.atan2(headP.x, headP.z));
                if (hAzim < 0) hAzim += 360.0f;

                float[] appHead = celestialToApparentAngles((float) Math.toRadians(hAzim), (float) Math.toRadians(hAlt), celestialAngle);
                float dYaw = Mth.wrapDegrees(appHead[0] - effectiveYaw);
                float dPitch = appHead[1] - effectivePitch;

                if (Math.abs(dYaw) <= currentFOV && Math.abs(dPitch) <= currentFOV) {
                    float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);
                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);

                    if (distFromCenter < lensRadius - 4.0f) {
                        float zoomMeteorScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.60f;
                        float headSize = (m.size() * 1.6f) * (size / 300.0f) * zoomMeteorScale;
                        renderTintedTexturedQuad(guiGraphics, COMET_HEAD_TEXTURE, sx, sy, headSize, timeSec * 50.0f, m.r(), m.g(), m.b(), m.intensity());
                    }
                }
            }

            // 5.4. Render The Wandering Spheres (Archon Planets & Dwarf Worlds) with Real-Time Orbiting Moons & Rings
            for (CelestialEventHelper.PlanetDefinition planet : CelestialEventHelper.PLANETS) {
                float planetTierDimmer = switch (planet.minTier()) {
                    case 1 -> 1.0f;
                    case 2 -> 0.82f;
                    case 3 -> 0.65f;
                    case 4 -> 0.48f;
                    case 5 -> 0.35f;
                    default -> 1.0f;
                };

                float[] skyPos = CelestialEventHelper.getPlanetSkyPos(planet, gameTime, partialTick);
                float[] app = celestialToApparentAngles((float) Math.toRadians(skyPos[0]), (float) Math.toRadians(skyPos[1]), celestialAngle);
                float dYaw = Mth.wrapDegrees(app[0] - effectiveYaw);
                float dPitch = app[1] - effectivePitch;

                if (Math.abs(dYaw) <= currentFOV && Math.abs(dPitch) <= currentFOV) {
                    float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);
                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);

                    if (distFromCenter < lensRadius - 4.0f) {
                        float zoomPlanetScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.90f;
                        float planetSize = planet.angularSize() * (size / 190.0f) * zoomPlanetScale;

                        // Draw Planetary Rings (if present, e.g. Cryos, Chronos, Chiron)
                        if (planet.hasRings()) {
                            float ringRx = planetSize * planet.ringRadius() * 0.55f;
                            float ringRy = planetSize * 0.22f;
                            int ringSegments = 32;
                            float prevRx = 0, prevRy = 0;
                            boolean hasPrevR = false;
                            for (int rSeg = 0; rSeg <= ringSegments; rSeg++) {
                                float ang = (float) (rSeg * 2.0 * Math.PI / ringSegments);
                                float rx = sx + (float) Math.cos(ang) * ringRx - (float) Math.sin(ang) * ringRy * 0.35f;
                                float ry = sy + (float) Math.sin(ang) * ringRy + (float) Math.cos(ang) * ringRx * 0.25f;
                                if (hasPrevR) {
                                    drawLine(guiGraphics, (int) prevRx, (int) prevRy, (int) rx, (int) ry, planet.ringColor());
                                }
                                prevRx = rx;
                                prevRy = ry;
                                hasPrevR = true;
                            }
                        }

                        // Draw Planet Disk
                        renderTintedTexturedQuad(guiGraphics, planet.texture(), sx, sy, planetSize, 0.0f, planetTierDimmer, planetTierDimmer, planetTierDimmer, planetTierDimmer);

                        // Draw Real-Time Orbiting Moons
                        if (planet.moonCount() > 0) {
                            for (int m = 0; m < planet.moonCount(); m++) {
                                float moonSpeed = 0.6f + m * 0.35f;
                                float moonAngle = timeSec * moonSpeed + (m * 2.1f);
                                float moonDistX = planetSize * (0.85f + m * 0.45f);
                                float moonDistY = planetSize * 0.30f;

                                float mx = sx + (float) Math.cos(moonAngle) * moonDistX;
                                float my = sy + (float) Math.sin(moonAngle) * moonDistY;

                                if (Math.hypot(mx - centerX, my - centerY) < lensRadius - 4.0f) {
                                    int moonColor = (m == 0) ? 0xFFE0F0FF : (m == 1 ? 0xFFD0D8E0 : 0xFFFFE8C0);
                                    drawCircle(guiGraphics, (int) mx, (int) my, 2, moonColor);

                                    // Display moon name on hover
                                    if (Math.hypot(mouseX - mx, mouseY - my) <= 6.0f && m < planet.moonNames().length) {
                                        guiGraphics.drawString(this.font, "§7☽ " + planet.moonNames()[m], (int) mx + 4, (int) my - 4, 0xFFE0F0FF, false);
                                    }
                                }
                            }
                        }

                        if (!this.isLensMode && distFromCenter <= 20.0f && this.focusedTarget == null) {
                            this.focusedTarget = planet;
                        }

                        if (distFromCenter < lensRadius * 0.8f) {
                            int col = ((int) (planet.r() * 255) << 16) | ((int) (planet.g() * 255) << 8) | ((int) (planet.b() * 255));
                            guiGraphics.drawString(this.font, "§6🪐 §f" + planet.name(), (int) sx + (int) (planetSize * 0.5f) + 4, (int) sy - 4, 0xFF000000 | col, false);
                        }
                    }
                }
            }

            // 5.5. Render Transient Supernovae & Expanding Shockwave Remnants
            List<CelestialEventHelper.ActiveSupernovaState> activeSupernovae = CelestialEventHelper.getActiveSupernovae(gameTime, partialTick);
            for (CelestialEventHelper.ActiveSupernovaState sn : activeSupernovae) {
                float[] app = celestialToApparentAngles((float) Math.toRadians(sn.event().azim()), (float) Math.toRadians(sn.event().alt()), celestialAngle);
                float dYaw = Mth.wrapDegrees(app[0] - effectiveYaw);
                float dPitch = app[1] - effectivePitch;

                if (Math.abs(dYaw) <= currentFOV && Math.abs(dPitch) <= currentFOV) {
                    float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                    float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);
                    float distFromCenter = (float) Math.hypot(sx - centerX, sy - centerY);

                    if (distFromCenter < lensRadius - 4.0f) {
                        float zoomSnShockScale = 1.0f + (float) Math.sqrt(Math.max(0.0f, this.zoom - 1.0f)) * 0.85f;
                        float shellSize = (sn.expandingRadiusDeg() * 12.0f) * (size / 240.0f) * zoomSnShockScale;
                        renderTintedTexturedQuad(guiGraphics, SUPERNOVA_RING_TEXTURE, sx, sy, shellSize, timeSec * 3.0f, sn.event().r(), sn.event().g(), sn.event().b(), sn.currentBrightness() * 0.85f);

                        // Pulsating central pulsar core
                        float coreSize = 14.0f * sn.currentBrightness() * sn.coreTwinkle() * zoomSnShockScale;
                        renderTintedTexturedQuad(guiGraphics, COMET_HEAD_TEXTURE, sx, sy, coreSize, timeSec * 25.0f, 1.0f, 1.0f, 1.0f, 1.0f);

                        if (distFromCenter <= 20.0f && this.focusedTarget == null) {
                            this.focusedTarget = sn;
                        }

                        if (distFromCenter < lensRadius * 0.8f) {
                            guiGraphics.drawString(this.font, "§e💥 §f" + sn.event().name(), (int) sx + 14, (int) sy - 4, 0xFFFFF0A0, false);
                        }
                    }
                }
            }

            // Find closest drawn line to cursor for targeted highlighting & erasing
            float minLineDist = 10.0f;
            List<String> currentEdgesSnapshot;
            synchronized (drawnEdges) {
                currentEdgesSnapshot = new ArrayList<>(drawnEdges);
            }

            if (!isShiftDown() && dragStarId == null) {
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
                            int essRgb = CelestialStarHelper.getShiftingStarRgbInt(nodeA.spectralClass(), nodeA.constellation().getEssenceType(), timeSec, 0.0f);
                            lineColor = 0xFF000000 | essRgb;
                        } else if (nodeA.essence() != null && nodeB.essence() != null) {
                            int colA = CelestialStarHelper.getShiftingStarRgbInt(nodeA.spectralClass(), nodeA.essence(), timeSec, 0.0f);
                            int colB = CelestialStarHelper.getShiftingStarRgbInt(nodeB.spectralClass(), nodeB.essence(), timeSec, 1.0f);
                            int r = (((colA >> 16) & 0xFF) + ((colB >> 16) & 0xFF)) / 2;
                            int g = (((colA >> 8) & 0xFF) + ((colB >> 8) & 0xFF)) / 2;
                            int b = ((colA & 0xFF) + (colB & 0xFF)) / 2;
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
                    int essRgb = CelestialStarHelper.getShiftingStarRgbInt(startNode.spectralClass(), startNode.essence(), timeSec, 0.0f);
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

                    if (Math.abs(dYaw) <= currentFOV && Math.abs(dPitch) <= currentFOV) {
                        float sx = centerX + (dYaw / (currentFOV * 0.5f)) * (size * 0.43f);
                        float sy = centerY - (dPitch / (currentFOV * 0.5f)) * (size * 0.43f);
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
                guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: §f" + ls.name() + "  §e| Class: §b" + ls.spectralClass().name() + "  §e| Essence: " + ls.essenceType().getFormattedName() + "  §e|  §b" + ls.info(), centerX, centerY + 28, 0xFFE0F0FF);
            } else if (focusedTarget instanceof CelestialStarHelper.AmbientStar as) {
                guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: §f" + as.name() + "  §e| Class: §b" + as.spectralClass().name() + "  §e| Essence: " + as.essenceType().getFormattedName() + "  §e| Mag: §f" + String.format("%.2fm", as.apparentMagnitude()), centerX, centerY + 28, 0xFFC0D0E0);
            } else if (focusedTarget instanceof Object[] pair) {
                Constellation c = (Constellation) pair[0];
                ConstellationStar star = (ConstellationStar) pair[1];
                boolean isDisc = (player != null) && PlayerAstralProgress.isDiscovered(player, c);
                int starIdx = c.getStars().indexOf(star);
                boolean canSelect = PlayerAstralProgress.canHardwareObserve(getInstrumentOpticTier(), c.getTier());

                if (isDisc) {
                    String starName = CelestialStarHelper.getConstellationStarName(c, starIdx);
                    String title = Component.translatable(c.getUnlocalizedName()).getString();
                    String essCode = c.getEssenceType().getColorCode();
                    boolean hasBlankChart = false;
                    if (player != null) {
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            if (player.getInventory().getItem(i).is(ddraig.net.entropica.registry.ModItems.STAR_CHART_BLANK.get())) {
                                hasBlankChart = true;
                                break;
                            }
                        }
                    }
                    String chartPrompt = hasBlankChart
                            ? "  §a[✦ Right-Click: Inscribe Chart]"
                            : "  §7(Need Blank Star Chart)";
                    guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: " + essCode + starName + " §e| Constellation: §f" + title + " §7[" + c.getTier().getDisplayName() + "§7]" + chartPrompt, centerX, centerY + 28, 0xFFFFFFFF);
                } else if (!canSelect) {
                    String reqInst = PlayerAstralProgress.getRequiredInstrumentName(c.getTier());
                    guiGraphics.drawCenteredString(this.font, "§8✦ FAINT NODE §8[" + c.getTier().getDisplayName() + "§8]  §e| Class: §b" + star.spectralClass().name() + "  §c[Requires: " + reqInst + "]", centerX, centerY + 28, 0xFFFFA0A0);
                } else {
                    guiGraphics.drawCenteredString(this.font, String.format("§6✦ TARGET: §fSpectral Node %d §e| Class: §b%s §e| Mag: §f%.2fm", (starIdx + 1), star.spectralClass().name(), star.brightness()), centerX, centerY + 28, 0xFFC0D0E0);
                }
            } else if (focusedTarget instanceof SupernovaManager.OverriddenStar ov) {
                String badge = (ov.phase() == SupernovaPhase.REMNANT) ? "§5✦ STELLAR REMNANT: §f" : "§c💥 SUPERNOVA INSTABILITY: §f";
                String desc = (ov.remnantType() != null) ? (" §7[" + ov.remnantType().getDescription() + "]") : "";
                guiGraphics.drawCenteredString(this.font, badge + ov.name() + " §e| Phase: §a" + ov.phase().getDisplayName() + " §e| Essence: " + ov.essenceType().getFormattedName() + desc, centerX, centerY + 28, 0xFFFFE080);
            } else if (focusedTarget instanceof CelestialEventHelper.PlanetDefinition planet) {
                String moonText = (planet.moonCount() > 0) ? ("  §e|  Moons: §b" + planet.moonCount()) : "";
                guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: §b🪐 §f" + planet.name() + " §7[" + planet.classification() + "]  §e|  Distance: §a" + String.format("%.1f AU", planet.distanceAU()) + moonText, centerX, centerY + 28, 0xFF80E0FF);
            } else if (focusedTarget instanceof CelestialEventHelper.ActiveSupernovaState sn) {
                guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: §e💥 §f" + sn.event().name() + "  §e|  Phase: §a" + sn.phaseName() + "  §e|  Remnant Radius: §b" + String.format("%.1f°", sn.expandingRadiusDeg()), centerX, centerY + 28, 0xFFFFE080);
            } else if (focusedTarget instanceof CelestialEventHelper.CometDefinition comet) {
                guiGraphics.drawCenteredString(this.font, "§6✦ TARGET: §b☄ §f" + comet.name() + " §e| Tail: §a" + String.format("%.1f°", comet.tailLengthDeg()), centerX, centerY + 28, 0xFF80E0FF);
            } else if (focusedTarget instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity targetLens) {
                BlockPos tPos = targetLens.getBlockPos();
                String starFocus = targetLens.getActiveStarName() != null ? targetLens.getActiveStarName() : targetLens.getTargetName();
                guiGraphics.drawCenteredString(this.font, "§a✦ OPTICAL RELAY: §fRefractive Lens §7[" + tPos.getX() + ", " + tPos.getY() + ", " + tPos.getZ() + "] §e| Focused: §b" + starFocus, centerX, centerY + 28, 0xFF80FF80);
            }
        }

        // 10. Draw Notification / Lock Message
        if (this.lensLockMessage != null && !this.lensLockMessage.isEmpty()) {
            long elapsed = System.currentTimeMillis() - this.lensLockTime;
            if (elapsed < 3500) {
                float alpha = (elapsed < 3000) ? 1.0f : (1.0f - (elapsed - 3000) / 500.0f);
                int aInt = Math.max(10, Math.min(255, (int) (alpha * 255.0f)));
                int boxW = this.font.width(this.lensLockMessage) + 16;
                int boxH = 18;
                int bx = centerX - boxW / 2;
                int by = centerY + 46;

                guiGraphics.fill(bx, by, bx + boxW, by + boxH, (aInt << 24) | 0x0B0E17);
                guiGraphics.fill(bx, by, bx + boxW, by + 1, (aInt << 24) | 0x38BDF8);
                guiGraphics.fill(bx, by + boxH - 1, bx + boxW, by + boxH, (aInt << 24) | 0x38BDF8);
                guiGraphics.drawString(this.font, this.lensLockMessage, bx + 8, by + 5, (aInt << 24) | 0xFFFFFF, false);
            } else {
                this.lensLockMessage = null;
            }
        }

        // 11. Constellation Discovery Toast / Banner Animation
        if (justDiscovered != null) {
            long elapsed = System.currentTimeMillis() - discoveryTime;
            if (elapsed < 4000) {
                float alpha = (elapsed < 3500) ? 1.0f : (1.0f - (elapsed - 3500) / 500.0f);
                int aInt = Math.max(10, Math.min(255, (int) (alpha * 255.0f)));
                int bannerY = 20;
                String title = Component.translatable(justDiscovered.getUnlocalizedName()).getString();
                String subtitle = "§bConstellation Discovered! §7[" + justDiscovered.getTier().getDisplayName() + "§7]";
                int bw = Math.max(this.font.width(title), this.font.width(subtitle)) + 30;
                int bh = 32;
                int bx = centerX - bw / 2;

                guiGraphics.fill(bx, bannerY, bx + bw, bannerY + bh, (aInt << 24) | 0x0B0F19);
                guiGraphics.fill(bx, bannerY, bx + bw, bannerY + 1, (aInt << 24) | 0xFFD700);
                guiGraphics.fill(bx, bannerY + bh - 1, bx + bw, bannerY + bh, (aInt << 24) | 0xFFD700);

                guiGraphics.drawCenteredString(this.font, subtitle, centerX, bannerY + 5, (aInt << 24) | 0x80E0FF);
                guiGraphics.drawCenteredString(this.font, "§6✦ §f" + title + " §6✦", centerX, bannerY + 17, (aInt << 24) | 0xFFFFE080);
            } else {
                justDiscovered = null;
            }
        }

        // 12. Horizon Monitor Left Tray
        if (this.width >= 380 && this.visibleConstellations != null && !this.visibleConstellations.isEmpty()) {
            int trayX = 10;
            int trayY = 36;
            int trayW = 135;
            int maxItems = Math.min(6, this.visibleConstellations.size());
            int trayH = 14 + (maxItems * 14) + 14;

            guiGraphics.fill(trayX, trayY, trayX + trayW, trayY + trayH, 0xDD0B0F19);
            guiGraphics.fill(trayX, trayY, trayX + trayW, trayY + 1, 0xFF38BDF8);
            guiGraphics.drawString(this.font, "§6✦ Horizon Monitor", trayX + 4, trayY + 3, 0xFFFFFFFF, false);

            int itemY = trayY + 14;
            for (int i = 0; i < maxItems; i++) {
                Constellation c = this.visibleConstellations.get(i);
                float baseAzimuth = c.getCelestialAzimuthRad();
                float baseAltitude = c.getCelestialAltitudeRad();
                float[] appAngles = celestialToApparentAngles(baseAzimuth, baseAltitude, celestialAngle);
                float curAlt = appAngles[1];

                boolean isHover = mouseX >= trayX && mouseX <= trayX + trayW && mouseY >= itemY && mouseY <= itemY + 13;
                if (isHover) {
                    guiGraphics.fill(trayX + 2, itemY, trayX + trayW - 2, itemY + 13, 0x4438BDF8);
                }

                boolean isDisc = (player != null) && PlayerAstralProgress.isDiscovered(player, c);
                String name = isDisc ? Component.translatable(c.getUnlocalizedName()).getString() : "Uncharted (" + c.getTier().getDisplayName() + ")";
                if (name.length() > 14) name = name.substring(0, 13) + "..";

                if (curAlt >= 0) {
                    guiGraphics.drawString(this.font, (isHover ? "§b▶ " : "§a▲ ") + (isDisc ? "§f" : "§7") + name + " §7(+" + (int)curAlt + "°)", trayX + 4, itemY + 2, isHover ? 0xFF00E5FF : (isDisc ? 0xFFE0E0E0 : 0xFFA0B0C0), false);
                } else {
                    float degBelow = -curAlt;
                    float minsToRise = degBelow / 18.0f; // 18 deg per real minute in Minecraft 20-min day cycle
                    guiGraphics.drawString(this.font, (isHover ? "§b▶ " : "§e▽ ") + "§7" + name + " §6(~" + String.format("%.0fm", minsToRise) + ")", trayX + 4, itemY + 2, isHover ? 0xFF00E5FF : 0xFFAAAAAA, false);
                }
                itemY += 14;
            }

            guiGraphics.drawString(this.font, "§8[Click star to pan view]", trayX + 4, itemY + 2, 0xFF88A0B0, false);
        }

        if (this.instrument == InstrumentType.ASTROLABE) {
            // Draw 24 Astrolabe Brass Angle Ticks around the circular aperture
            for (int a = 0; a < 360; a += 15) {
                float rad = (float) Math.toRadians(a);
                float rInner = lensRadius - 6.0f;
                float rOuter = lensRadius + 2.0f;
                float tx1 = centerX + (float) Math.cos(rad) * rInner;
                float ty1 = centerY + (float) Math.sin(rad) * rInner;
                float tx2 = centerX + (float) Math.cos(rad) * rOuter;
                float ty2 = centerY + (float) Math.sin(rad) * rOuter;
                drawLine(guiGraphics, (int) tx1, (int) ty1, (int) tx2, (int) ty2, (a % 90 == 0) ? 0xFFFFD700 : 0xFFB8860B);
            }
        }

        if (this.isLensMode) {
            guiGraphics.drawCenteredString(this.font, "§a[Drag to Aim  •  SPACEBAR: Lock Lens on Target  •  Scroll to Zoom  •  ESC to Exit]", centerX, screenHeight - 28, 0xFF80FF80);
        } else if (isShiftDown()) {
            if (this.hoveredEdgeKey != null) {
                guiGraphics.drawCenteredString(this.font, "§c✦ LINE SELECTED: Right-Click to Erase ✦", centerX, screenHeight - 28, 0xFFFF6666);
                guiGraphics.drawCenteredString(this.font, "§7Click & drag between stars to chart lines", centerX, screenHeight - 16, 0xFFA0C0D0);
            } else {
                guiGraphics.drawCenteredString(this.font, "§e✦ SNEAK ACTIVE: VIEW LOCKED ✦", centerX, screenHeight - 28, 0xFFFFD700);
                guiGraphics.drawCenteredString(this.font, "§7Click & drag between stars to chart  •  Hover line + Right-Click to erase", centerX, screenHeight - 16, 0xFFA0C0D0);
            }
        } else {
            guiGraphics.drawCenteredString(this.font, "§8[Click & Drag to pan sky  •  Scroll to zoom (" + String.format("%.1fx", this.zoom) + ")  •  Hold SNEAK to chart  •  ESC to exit]", centerX, screenHeight - 18, 0xFF88A0B0);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();

        // 1. Erase Line Connection (Right-Click only without Shift)
        if (button == 1 && !isShiftDown()) {
            String edgeToErase = this.hoveredEdgeKey;
            if (edgeToErase == null) {
                float minLineDist = 10.0f;
                for (String edge : drawnEdges) {
                    String[] parts = edge.split("---");
                    if (parts.length == 2) {
                        StarNode nodeA = onScreenStars.get(parts[0]);
                        StarNode nodeB = onScreenStars.get(parts[1]);
                        if (nodeA != null && nodeB != null) {
                            float d = pointToSegmentDistance((float) mouseX, (float) mouseY, nodeA.sx(), nodeA.sy(), nodeB.sx(), nodeB.sy());
                            if (d < minLineDist) {
                                minLineDist = d;
                                edgeToErase = edge;
                            }
                        }
                    }
                }
            }

            if (edgeToErase != null) {
                drawnEdges.remove(edgeToErase);
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
        }

        // Right-Click on discovered constellation to inscribe a completed star chart!
        if (button == 1 && !this.isLensMode && this.focusedTarget instanceof Object[] pair && pair[0] instanceof Constellation c) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null && PlayerAstralProgress.isDiscovered(player, c)) {
                boolean hasBlankChart = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (player.getInventory().getItem(i).is(ddraig.net.entropica.registry.ModItems.STAR_CHART_BLANK.get())) {
                        hasBlankChart = true;
                        break;
                    }
                }

                if (hasBlankChart) {
                    NetworkManager.sendToServer(new ConstellationDiscoveryPayload(c.getId()));
                    player.playSound(SoundEvents.CHISELED_BOOKSHELF_INSERT, 0.8f, 1.2f);
                    player.playSound(SoundEvents.VILLAGER_WORK_CARTOGRAPHER, 0.8f, 1.1f);
                    this.lensLockMessage = "§a✦ Inscribed Star Chart: §f" + Component.translatable(c.getUnlocalizedName()).getString();
                    this.lensLockTime = System.currentTimeMillis();
                    return true;
                } else {
                    player.playSound(SoundEvents.DISPENSER_FAIL, 0.8f, 1.2f);
                    this.lensLockMessage = "§c✦ Inscribe Failed: Requires a Blank Star Chart in your inventory!";
                    this.lensLockTime = System.currentTimeMillis();
                    return true;
                }
            }
        }

        // Left-Click on Horizon Monitor item to smoothly pan view over to that constellation!
        if (button == 0 && this.width >= 380 && this.visibleConstellations != null && !this.visibleConstellations.isEmpty()) {
            int trayX = 10;
            int trayY = 36;
            int trayW = 135;
            int maxItems = Math.min(6, this.visibleConstellations.size());
            int itemY = trayY + 14;
            float celestialAngle = getCelestialAngle(0.0f);

            for (int i = 0; i < maxItems; i++) {
                if (mouseX >= trayX && mouseX <= trayX + trayW && mouseY >= itemY && mouseY <= itemY + 13) {
                    Constellation c = this.visibleConstellations.get(i);
                    float baseAzimuth = c.getCelestialAzimuthRad();
                    float baseAltitude = c.getCelestialAltitudeRad();
                    float[] appAngles = celestialToApparentAngles(baseAzimuth, baseAltitude, celestialAngle);
                    panTo(appAngles[0], appAngles[1]);
                    return true;
                }
                itemY += 14;
            }
        }

        if (button == 0 && isShiftDown()) {
            for (StarNode node : onScreenStars.values()) {
                if (node.canSelect() && Math.hypot(mouseX - node.sx(), mouseY - node.sy()) <= Math.max(14.0f, node.size() * 0.7f + 4.0f)) {
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
                    if (node.canSelect() && Math.hypot(event.x() - node.sx(), event.y() - node.sy()) <= Math.max(16.0f, node.size() * 0.7f + 6.0f)) {
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

                    // Check if ANY constellation has all required connections satisfied in drawnEdges without extraneous lines
                    for (Constellation c : visibleConstellations) {
                        if (!PlayerAstralProgress.isDiscovered(player, c)) {
                            if (PlayerAstralProgress.matchesConstellationPattern(c, drawnEdges)) {
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

    private void renderTintedTexturedQuad(GuiGraphics guiGraphics, ResourceLocation texture, float sx, float sy, float size, float rotDeg, float r, float g, float b, float a) {
        int ia = Mth.clamp((int) (a * 255.0f), 0, 255);
        int ir = Mth.clamp((int) (r * 255.0f), 0, 255);
        int ig = Mth.clamp((int) (g * 255.0f), 0, 255);
        int ib = Mth.clamp((int) (b * 255.0f), 0, 255);
        int argb = (ia << 24) | (ir << 16) | (ig << 8) | ib;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(sx, sy);
        if (rotDeg != 0.0f) {
            guiGraphics.pose().rotate((float) Math.toRadians(rotDeg));
        }
        int half = Math.max(1, Math.round(size * 0.5f));
        int dSize = half * 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, -half, -half, 0.0f, 0.0f, dSize, dSize, dSize, dSize, argb);
        guiGraphics.pose().popMatrix();
    }
}
