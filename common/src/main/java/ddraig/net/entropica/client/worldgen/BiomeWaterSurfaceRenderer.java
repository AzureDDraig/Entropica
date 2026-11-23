package ddraig.net.entropica.client.worldgen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.worldgen.biome.BiomeAtmosphereProfile;
import ddraig.net.entropica.worldgen.biome.BiomeTransitionMath;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomeAtmospheres;
import ddraig.net.entropica.worldgen.biome.WaterOpticalMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * 100% TerraBlender, Sodium/Rubidium/Embeddium, and Iris/Oculus Compatible Biome Water-Surface
 * Optical Reflection and 3D Sub-Surface Starlight Parallax Renderer.
 *
 * <p>Avoids fluid chunk-meshing mixins entirely by rendering lightweight, zero-allocation cached
 * water-surface optical quads via {@link MultiBufferSource} +
 * {@link RenderType#entityTranslucentEmissive(ResourceLocation)} at {@code waterBlockY + 0.904F}
 * (cleanly above vanilla/Sodium's {@code 0.8889F} still-water top with zero z-fighting).</p>
 *
 * <p>Supports five Materia atmosphere water modes ({@link WaterOpticalMode}):
 * {@code STARLIGHT_PARALLAX} (three counter-gliding sub-surface cosmic nebula and twinkling
 * constellation depth planes with true 3D camera-movement stereopsis), {@code AURORA_RIBBON_REFLECTION},
 * {@code FRESNEL_SKY_REFLECTION}, {@code CRIMSON_TIDE_GLINT}, and {@code MIRAGE_SHIMMER_REFLECTION},
 * with seamless {@code 3x3} biome-border blending, shoreline/ice-shelf feathering, and automatic
 * shaderpack SSR/HDR bloom attenuation via {@link ShaderModCompatBridge}.</p>
 */
public final class BiomeWaterSurfaceRenderer {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;
    private static final int NO_OVERLAY = OverlayTexture.NO_OVERLAY;

    /** Rendered just above vanilla/Sodium still source water height (8/9 = 0.8888889F) to prevent z-fighting. */
    public static final float WATER_SURFACE_HEIGHT_OFFSET = 0.904F;
    private static final float STAR_GLINT_HEIGHT_OFFSET = 0.9052F;

    /** Outer visible radius in blocks where radial distance envelope reaches exact 0.0F. */
    public static final int SCAN_RADIUS = 34;
    private static final float MAX_RENDER_RADIUS = 34.0F;
    private static final float FADE_INNER_RADIUS = 15.0F;
    private static final double SUBDIVIDE_NEAR_DIST_SQ = 12.5D * 12.5D;
    private static final double TILE_CULL_RADIUS_SQ = (MAX_RENDER_RADIUS + 1.5D) * (MAX_RENDER_RADIUS + 1.5D);

    /**
     * Padded cache half-extent (34 + 2 movement slack + 1 shoreline neighbor ring = 37) so all 8
     * neighbors are valid even as the camera moves up to 1.99 blocks between 5-tick scans.
     */
    private static final int CACHE_HALF_EXTENT = SCAN_RADIUS + 3;
    private static final int CACHE_SPAN = CACHE_HALF_EXTENT * 2 + 1; // 75
    private static final int CACHE_CELL_COUNT = CACHE_SPAN * CACHE_SPAN; // 5625
    private static final int ACTIVE_TILE_SCAN_RADIUS = SCAN_RADIUS + 2; // 36
    private static final int CACHE_REFRESH_TICKS = 5;
    private static final int CAMERA_MOVE_RESCAN_THRESHOLD = 2;

    /** 3x3 biome tap stride (3 blocks -> 7x7 block footprint across 4x4 biome quarts) and normalized tent weights. */
    private static final int BIOME_TAP_STRIDE = 3;
    private static final float[] TAP_WEIGHTS_3X3 = {
            0.0625F, 0.1250F, 0.0625F,
            0.1250F, 0.2500F, 0.1250F,
            0.0625F, 0.1250F, 0.0625F
    };

    /** Three sub-surface parallax depth planes in blocks (multiplied by profile parallaxDepthScale). */
    private static final float PARALLAX_DEPTH_SHALLOW = 2.2F;
    private static final float PARALLAX_DEPTH_MID = 5.8F;
    private static final float PARALLAX_DEPTH_DEEP = 11.5F;

    // Fixed non-axis-aligned rotation cosines/sines for the 3 sub-surface cosmic planes
    private static final float PLANE0_COS = 0.92050485F; // ~23 deg
    private static final float PLANE0_SIN = 0.39073113F;
    private static final float PLANE1_COS = 0.75470958F; // ~-41 deg
    private static final float PLANE1_SIN = -0.65605903F;
    private static final float PLANE2_COS = 0.39073113F; // ~67 deg
    private static final float PLANE2_SIN = 0.92050485F;

    private static final WaterOpticalMode[] MODE_VALUES = WaterOpticalMode.values();
    private static final int MODE_COUNT = MODE_VALUES.length;

    // =========================================================================
    // Zero-Allocation Local Water-Surface Cache State (Client Thread Only)
    // =========================================================================

    private static WeakReference<ClientLevel> cachedLevelRef = null;
    private static long lastScanGameTime = Long.MIN_VALUE;
    private static int cachedAnchorX = Integer.MIN_VALUE;
    private static int cachedAnchorY = Integer.MIN_VALUE;
    private static int cachedAnchorZ = Integer.MIN_VALUE;
    private static int levelPrecisionOriginX = 0;
    private static int levelPrecisionOriginZ = 0;

    // Per-cell raw scan buffers (75 x 75 = 5625)
    private static final boolean[] GRID_HAS_WATER = new boolean[CACHE_CELL_COUNT];
    private static final short[] GRID_WATER_Y = new short[CACHE_CELL_COUNT];
    private static final float[] GRID_SKY_EXPOSURE = new float[CACHE_CELL_COUNT];
    private static final byte[] GRID_PRIMARY_MODE = new byte[CACHE_CELL_COUNT];
    private static final float[] GRID_PRIMARY_WEIGHT = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_PRIM_R = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_PRIM_G = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_PRIM_B = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_SEC_R = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_SEC_G = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_SEC_B = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_DEPTH_SCALE = new float[CACHE_CELL_COUNT];

    // Secondary cross-fade biome optical layer per cell (for borders between two distinct optical biomes)
    private static final byte[] GRID_SECONDARY_MODE = new byte[CACHE_CELL_COUNT];
    private static final float[] GRID_SECONDARY_WEIGHT = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_ALT_PRIM_R = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_ALT_PRIM_G = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_ALT_PRIM_B = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_ALT_SEC_R = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_ALT_SEC_G = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_ALT_SEC_B = new float[CACHE_CELL_COUNT];
    private static final float[] GRID_ALT_DEPTH_SCALE = new float[CACHE_CELL_COUNT];

    // Compact active water-tile arrays populated during the 5-tick scan (zero per-frame scanning)
    private static int activeTileCount = 0;
    private static boolean hasStarlightParallaxTiles = false;
    private static int referenceStarlightWaterY = 62;
    private static final int[] TILE_BX = new int[CACHE_CELL_COUNT];
    private static final short[] TILE_BY = new short[CACHE_CELL_COUNT];
    private static final int[] TILE_BZ = new int[CACHE_CELL_COUNT];
    private static final int[] TILE_GRID_INDEX = new int[CACHE_CELL_COUNT];
    private static final byte[] TILE_MODE = new byte[CACHE_CELL_COUNT];
    private static final float[] TILE_PRIM_R = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_PRIM_G = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_PRIM_B = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_SEC_R = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_SEC_G = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_SEC_B = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_DEPTH_SCALE = new float[CACHE_CELL_COUNT];
    // Pre-averaged 4-corner weights (combining shoreline/ice-shelf softening + sky exposure + 3x3 biome intensity)
    private static final float[] TILE_CORNER_W00 = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_CORNER_W10 = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_CORNER_W11 = new float[CACHE_CELL_COUNT];
    private static final float[] TILE_CORNER_W01 = new float[CACHE_CELL_COUNT];

    // Reusable scratch cursors & 3x3 biome tap accumulators
    private static final BlockPos.MutableBlockPos PROBE_POS = new BlockPos.MutableBlockPos();
    private static final BlockPos.MutableBlockPos ABOVE_POS = new BlockPos.MutableBlockPos();
    private static final BlockPos.MutableBlockPos BIOME_TAP_POS = new BlockPos.MutableBlockPos();
    private static final float[] TAP_MODE_PRESENCE = new float[MODE_COUNT];
    private static final float[] TAP_MODE_INTENSITY = new float[MODE_COUNT];
    private static final float[] TAP_MODE_DEPTH = new float[MODE_COUNT];
    private static final float[] TAP_MODE_PR = new float[MODE_COUNT];
    private static final float[] TAP_MODE_PG = new float[MODE_COUNT];
    private static final float[] TAP_MODE_PB = new float[MODE_COUNT];
    private static final float[] TAP_MODE_SR = new float[MODE_COUNT];
    private static final float[] TAP_MODE_SG = new float[MODE_COUNT];
    private static final float[] TAP_MODE_SB = new float[MODE_COUNT];

    // Per-vertex scratch output (avoids allocating color/alpha tuples during quad emission)
    private static float outR;
    private static float outG;
    private static float outB;
    private static float outA;

    private BiomeWaterSurfaceRenderer() {}

    // =========================================================================
    // Public Pure-Math Testable Static Helpers (Headless JVM Safe)
    // =========================================================================

    /**
     * Computes a physically-inspired clamped grazing-angle Fresnel weight in {@code [0.06F, 1.0F]}
     * using {@code 1 - |ny|} for a horizontal water surface normal {@code (0, 1, 0)}.
     *
     * @param viewDx X component of camera-to-surface (or surface-to-camera) view vector
     * @param viewDy Y component of camera-to-surface (or surface-to-camera) view vector
     * @param viewDz Z component of camera-to-surface (or surface-to-camera) view vector
     * @return grazing Fresnel weight in {@code [0.06F, 1.0F]} ({@code 0.06F} straight down, {@code 1.0F} grazing horizon)
     */
    public static float computeFresnelFactor(double viewDx, double viewDy, double viewDz) {
        double lenSq = viewDx * viewDx + viewDy * viewDy + viewDz * viewDz;
        if (!Double.isFinite(lenSq) || lenSq <= 1.0e-12D) {
            return 0.06F;
        }
        double invLen = 1.0D / Math.sqrt(lenSq);
        double absNy = Math.min(1.0D, Math.abs(viewDy * invLen));
        double oneMinusNy = 1.0D - absNy;
        // Smooth convex grazing curve: subtle overhead so sub-surface depth stays clear, luminous toward horizon
        double grazingCurve = oneMinusNy * (0.35D + 0.65D * oneMinusNy);
        float weight = (float) (0.06D + 0.94D * grazingCurve);
        return Mth.clamp(weight, 0.06F, 1.0F);
    }

    /**
     * Projects a camera-to-water ray onto a virtual horizontal plane {@code depthBlocks} beneath the
     * water surface ({@code (viewDelta / Math.max(0.22D, -viewDy)) * depthBlocks + worldCoord}),
     * producing genuine 3D sub-surface parallax shift when the camera moves while clamping shallow
     * grazing angles to prevent horizon blowup.
     *
     * @param worldCoord  world X or Z coordinate on the water surface
     * @param viewDelta   corresponding camera-to-water delta ({@code worldCoord - camCoord})
     * @param viewDy      vertical camera-to-water delta ({@code waterSurfaceY - camY}, negative when above water)
     * @param depthBlocks virtual sub-surface plane depth in blocks
     * @return projected sub-surface coordinate
     */
    public static float computeParallaxCoord(double worldCoord, double viewDelta, double viewDy, float depthBlocks) {
        if (!Double.isFinite(worldCoord) || !Double.isFinite(viewDelta) || !Double.isFinite(viewDy) || !Float.isFinite(depthBlocks)) {
            return Double.isFinite(worldCoord) ? (float) worldCoord : 0.0F;
        }
        double safeNegDy = Math.max(0.22D, -viewDy);
        double projected = (viewDelta / safeNegDy) * (double) depthBlocks + worldCoord;
        return (float) projected;
    }

    /**
     * Deterministic procedural multi-octave twinkling starlight and cosmic nebula evaluator
     * returning a continuous intensity in {@code [0.0F, 1.0F]}.
     *
     * <p>Combines two smooth Hermite-interpolated value-noise nebula octaves and sinusoidal Materia
     * dust filaments with two rotated cellular twinkling starlight octaves so both broad cosmic
     * clouds and pinpoint stars shift smoothly as parallax coordinates change.</p>
     *
     * @param u    projected horizontal parallax coordinate U
     * @param v    projected horizontal parallax coordinate V
     * @param time animation time parameter
     * @return deterministic cosmic starlight + nebula intensity in {@code [0.0F, 1.0F]}
     */
    public static float evaluateStarfieldIntensity(float u, float v, float time) {
        if (!Float.isFinite(u) || !Float.isFinite(v) || !Float.isFinite(time)) {
            return 0.0F;
        }

        // Octaves 1 & 2: Continuous multi-octave cosmic nebula veil & Materia filaments
        float n1 = valueNoise2D(u * 0.43F + time * 0.024F, v * 0.43F - time * 0.018F, 0x27D4EB2F);
        float n2 = valueNoise2D(
                u * 0.97F - v * 0.31F - time * 0.031F,
                v * 0.97F + u * 0.31F + time * 0.026F,
                0x165667B1
        );
        float filament = 0.5F + 0.5F * Mth.sin(u * 0.63F + v * 0.49F + n1 * 2.5F + time * 0.13F);
        float nebula = (0.52F * n1 + 0.33F * n2 + 0.15F * filament) * 0.38F;

        // Octave 3: Mid-scale rotated stardust clusters
        float c1u = u * 1.18F - v * 0.67F;
        float c1v = u * 0.67F + v * 1.18F;
        float clusterStars = sampleCellularStarOctave(c1u, c1v, time, 0x3C6EF372, 0.46F, 2.05F);

        // Octave 4: High-definition rotated twinkling constellation & guide stars
        float c2u = u * 1.93F + v * 1.11F + 13.7F;
        float c2v = -u * 1.11F + v * 1.93F - 7.9F;
        float crispStars = sampleCellularStarOctave(c2u, c2v, time, 0x9E3779B9, 0.38F, 3.35F);

        float total = nebula + clusterStars * 0.48F + crispStars * 0.68F;
        return Mth.clamp(total, 0.0F, 1.0F);
    }

    // =========================================================================
    // Main AFTER_TRANSLUCENT World Render Entrypoint
    // =========================================================================

    /**
     * Renders biome-specific water-surface optical reflections and 3D sub-surface starlight parallax.
     *
     * <p>Invoked during {@code AFTER_TRANSLUCENT}, where {@code (0, 0, 0)} in {@code poseStack}
     * corresponds to {@code camera.getPosition()}.</p>
     */
    public static void renderWaterOptics(PoseStack poseStack, Matrix4f projOrModelView, Camera camera, float deltaTicks) {
        if (!isWaterEffectsEnabled()) {
            activeTileCount = 0;
            lastScanGameTime = Long.MIN_VALUE;
            return;
        }
        if (poseStack == null || camera == null || !camera.isInitialized()) {
            return;
        }
        // Skip water-surface optics whenever the camera itself is submerged in water, lava, or powder snow
        if (camera.getFluidInCamera() != FogType.NONE) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }
        ClientLevel level = mc.level;

        Vec3 camPos = camera.getPosition();
        BlockPos camBlockPos = camera.getBlockPosition();
        FluidState camFluid = level.getFluidState(camBlockPos);
        if (!camFluid.isEmpty() && camPos.y < camBlockPos.getY() + camFluid.getHeight(level, camBlockPos) + 0.03D) {
            return;
        }

        ensureLevelConsistency(level, camBlockPos.getX(), camBlockPos.getZ());

        long gameTime = level.getGameTime();
        if (shouldRefreshCache(gameTime, camBlockPos.getX(), camBlockPos.getY(), camBlockPos.getZ())) {
            refreshWaterSurfaceCache(level, gameTime, camBlockPos.getX(), camBlockPos.getY(), camBlockPos.getZ());
        }

        if (activeTileCount <= 0) {
            return;
        }

        float partialTick = mc.getDeltaTracker() != null
                ? Mth.clamp(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), 0.0F, 1.0F)
                : Mth.clamp(deltaTicks, 0.0F, 1.0F);

        float timeOfDay = level.getTimeOfDay(partialTick);
        float dayBrightness = Mth.clamp(Mth.cos(timeOfDay * ((float) Math.PI * 2.0F)) * 2.0F + 0.5F, 0.0F, 1.0F);
        float nightFactor = 1.0F - dayBrightness;
        float rainAttenuation = 1.0F - level.getRainLevel(partialTick) * 0.42F;

        float animTime = (float) ((gameTime + (double) partialTick) * 0.045D);
        float celestialRad = timeOfDay * ((float) Math.PI * 2.0F);
        float celDriftX = Mth.cos(celestialRad);
        float celDriftZ = Mth.sin(celestialRad);

        float reflectionShaderScale = ShaderModCompatBridge.getWaterReflectionShaderScale();
        float parallaxShaderScale = ShaderModCompatBridge.getWaterParallaxShaderScale();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        RenderType emissiveType = RenderType.entityTranslucentEmissive(WHITE_TEXTURE);
        VertexConsumer vc = bufferSource.getBuffer(emissiveType);

        poseStack.pushPose();
        Matrix4f mat = poseStack.last().pose();

        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;

        // Pass 1: Continuous per-tile water-surface optical & sub-surface parallax mesh
        for (int i = 0; i < activeTileCount; i++) {
            int bx = TILE_BX[i];
            int bz = TILE_BZ[i];
            double centerDx = (bx + 0.5D) - camX;
            double centerDz = (bz + 0.5D) - camZ;
            double centerDistSq = centerDx * centerDx + centerDz * centerDz;
            if (centerDistSq > TILE_CULL_RADIUS_SQ) {
                continue;
            }

            double wy = TILE_BY[i] + (double) WATER_SURFACE_HEIGHT_OFFSET;
            if (camY <= wy + 0.02D) {
                // Only render water surfaces below the camera eye plane
                continue;
            }

            WaterOpticalMode mode = MODE_VALUES[TILE_MODE[i]];
            float pr = TILE_PRIM_R[i];
            float pg = TILE_PRIM_G[i];
            float pb = TILE_PRIM_B[i];
            float sr = TILE_SEC_R[i];
            float sg = TILE_SEC_G[i];
            float sb = TILE_SEC_B[i];
            float depthScale = TILE_DEPTH_SCALE[i];

            float w00 = TILE_CORNER_W00[i];
            float w10 = TILE_CORNER_W10[i];
            float w11 = TILE_CORNER_W11[i];
            float w01 = TILE_CORNER_W01[i];

            // Subdivide near-camera tiles into 2x2 sub-quads for fine starlight & capillary ripple detail
            if (centerDistSq <= SUBDIVIDE_NEAR_DIST_SQ) {
                for (int sz = 0; sz < 2; sz++) {
                    float fz0 = sz * 0.5F;
                    float fz1 = (sz + 1) * 0.5F;
                    for (int sx = 0; sx < 2; sx++) {
                        float fx0 = sx * 0.5F;
                        float fx1 = (sx + 1) * 0.5F;

                        float sw00 = bilerp(w00, w10, w01, w11, fx0, fz0);
                        float sw10 = bilerp(w00, w10, w01, w11, fx1, fz0);
                        float sw11 = bilerp(w00, w10, w01, w11, fx1, fz1);
                        float sw01 = bilerp(w00, w10, w01, w11, fx0, fz1);

                        emitOpticalWaterPatch(
                                mat, vc, mode,
                                bx + fx0, bx + fx1, wy, bz + fz0, bz + fz1,
                                sw00, sw10, sw11, sw01,
                                pr, pg, pb, sr, sg, sb, depthScale,
                                camX, camY, camZ,
                                animTime, nightFactor, rainAttenuation,
                                celDriftX, celDriftZ,
                                reflectionShaderScale, parallaxShaderScale
                        );
                    }
                }
            } else {
                emitOpticalWaterPatch(
                        mat, vc, mode,
                        bx, bx + 1.0D, wy, bz, bz + 1.0D,
                        w00, w10, w11, w01,
                        pr, pg, pb, sr, sg, sb, depthScale,
                        camX, camY, camZ,
                        animTime, nightFactor, rainAttenuation,
                        celDriftX, celDriftZ,
                        reflectionShaderScale, parallaxShaderScale
                );
            }

            // Optional secondary biome cross-fade layer across shared custom optical biome borders
            int gridIdx = TILE_GRID_INDEX[i];
            int altModeOrd = GRID_SECONDARY_MODE[gridIdx];
            float altWeight = GRID_SECONDARY_WEIGHT[gridIdx];
            if (altModeOrd > 0 && altWeight > 0.015F) {
                float avgPrimary = Math.max(0.05F, GRID_PRIMARY_WEIGHT[gridIdx]);
                float ratio = Mth.clamp(altWeight / avgPrimary, 0.0F, 1.0F);
                emitOpticalWaterPatch(
                        mat, vc, MODE_VALUES[altModeOrd],
                        bx, bx + 1.0D, wy + 0.0004D, bz, bz + 1.0D,
                        w00 * ratio, w10 * ratio, w11 * ratio, w01 * ratio,
                        GRID_ALT_PRIM_R[gridIdx], GRID_ALT_PRIM_G[gridIdx], GRID_ALT_PRIM_B[gridIdx],
                        GRID_ALT_SEC_R[gridIdx], GRID_ALT_SEC_G[gridIdx], GRID_ALT_SEC_B[gridIdx],
                        GRID_ALT_DEPTH_SCALE[gridIdx],
                        camX, camY, camZ,
                        animTime, nightFactor, rainAttenuation,
                        celDriftX, celDriftZ,
                        reflectionShaderScale, parallaxShaderScale
                );
            }
        }

        // Pass 2: Crisp 3-Plane Sub-Surface Projected Constellation & Starlight Glints (STARLIGHT_PARALLAX)
        if (hasStarlightParallaxTiles && parallaxShaderScale > 0.01F) {
            renderProjectedSubSurfaceStars(
                    mat, vc,
                    camX, camY, camZ,
                    animTime, nightFactor, rainAttenuation,
                    celDriftX, celDriftZ, parallaxShaderScale
            );
        }

        poseStack.popPose();
        bufferSource.endBatch(emissiveType);
    }

    // =========================================================================
    // Per-Vertex Optical Evaluation & Quad Emission
    // =========================================================================

    private static void emitOpticalWaterPatch(
            Matrix4f mat, VertexConsumer vc, WaterOpticalMode mode,
            double x0, double x1, double wy, double z0, double z1,
            float w00, float w10, float w11, float w01,
            float pr, float pg, float pb, float sr, float sg, float sb, float depthScale,
            double camX, double camY, double camZ,
            float animTime, float nightFactor, float rainAttenuation,
            float celDriftX, float celDriftZ,
            float reflectionShaderScale, float parallaxShaderScale) {

        evaluateWaterVertex(mode, x0, wy, z0, w00, pr, pg, pb, sr, sg, sb, depthScale,
                camX, camY, camZ, animTime, nightFactor, rainAttenuation, celDriftX, celDriftZ,
                reflectionShaderScale, parallaxShaderScale);
        float r00 = outR, g00 = outG, b00 = outB, a00 = outA;

        evaluateWaterVertex(mode, x0, wy, z1, w01, pr, pg, pb, sr, sg, sb, depthScale,
                camX, camY, camZ, animTime, nightFactor, rainAttenuation, celDriftX, celDriftZ,
                reflectionShaderScale, parallaxShaderScale);
        float r01 = outR, g01 = outG, b01 = outB, a01 = outA;

        evaluateWaterVertex(mode, x1, wy, z1, w11, pr, pg, pb, sr, sg, sb, depthScale,
                camX, camY, camZ, animTime, nightFactor, rainAttenuation, celDriftX, celDriftZ,
                reflectionShaderScale, parallaxShaderScale);
        float r11 = outR, g11 = outG, b11 = outB, a11 = outA;

        evaluateWaterVertex(mode, x1, wy, z0, w10, pr, pg, pb, sr, sg, sb, depthScale,
                camX, camY, camZ, animTime, nightFactor, rainAttenuation, celDriftX, celDriftZ,
                reflectionShaderScale, parallaxShaderScale);
        float r10 = outR, g10 = outG, b10 = outB, a10 = outA;

        if (a00 <= 0.0015F && a01 <= 0.0015F && a11 <= 0.0015F && a10 <= 0.0015F) {
            return;
        }

        float ry = (float) (wy - camY);
        float rx0 = (float) (x0 - camX);
        float rx1 = (float) (x1 - camX);
        float rz0 = (float) (z0 - camZ);
        float rz1 = (float) (z1 - camZ);

        // Upward CCW winding (+Y normal) for clean vanilla & Iris/Oculus G-buffer shading
        emitUpwardVertex(mat, vc, rx0, ry, rz0, r00, g00, b00, a00, 0.0F, 0.0F);
        emitUpwardVertex(mat, vc, rx0, ry, rz1, r01, g01, b01, a01, 0.0F, 1.0F);
        emitUpwardVertex(mat, vc, rx1, ry, rz1, r11, g11, b11, a11, 1.0F, 1.0F);
        emitUpwardVertex(mat, vc, rx1, ry, rz0, r10, g10, b10, a10, 1.0F, 0.0F);
    }

    private static void evaluateWaterVertex(
            WaterOpticalMode mode,
            double wx, double wy, double wz, float cornerWeight,
            float pr, float pg, float pb, float sr, float sg, float sb, float depthScale,
            double camX, double camY, double camZ,
            float animTime, float nightFactor, float rainAttenuation,
            float celDriftX, float celDriftZ,
            float reflectionShaderScale, float parallaxShaderScale) {
        if (cornerWeight <= 0.001F) {
            outR = pr;
            outG = pg;
            outB = pb;
            outA = 0.0F;
            return;
        }

        double viewDx = wx - camX;
        double viewDy = wy - camY;
        double viewDz = wz - camZ;
        double horizDist = Math.sqrt(viewDx * viewDx + viewDz * viewDz);
        float radialFade = computeRadialDistanceFade((float) horizDist);
        if (radialFade <= 0.001F) {
            outR = pr;
            outG = pg;
            outB = pb;
            outA = 0.0F;
            return;
        }

        float fresnel = computeFresnelFactor(viewDx, viewDy, viewDz);
        double localX = wx - levelPrecisionOriginX;
        double localZ = wz - levelPrecisionOriginZ;

        switch (mode) {
            case STARLIGHT_PARALLAX -> {
                float diurnalScale = Mth.lerp(nightFactor, 0.30F, 1.0F);
                float baseEnv = cornerWeight * radialFade * diurnalScale * rainAttenuation * parallaxShaderScale;
                if (baseEnv <= 0.001F) {
                    outA = 0.0F;
                    return;
                }

                // Subtle capillary liquid refraction over the sub-surface cosmic portal
                float capX = Mth.sin((float) (localX * 1.85D + animTime * 1.45D)) * 0.045F
                        + Mth.cos((float) (localZ * 2.35D - animTime * 1.15D)) * 0.025F;
                float capZ = Mth.cos((float) (localZ * 1.75D + animTime * 1.35D)) * 0.045F
                        + Mth.sin((float) (localX * 2.25D + animTime * 1.05D)) * 0.025F;

                float dScale = Math.max(0.25F, depthScale);
                float d0 = PARALLAX_DEPTH_SHALLOW * dScale;
                float d1 = PARALLAX_DEPTH_MID * dScale;
                float d2 = PARALLAX_DEPTH_DEEP * dScale;

                // Plane 0 (2.2 * scale): Shallow twinkling constellation & stardust plane
                float u0 = computeParallaxCoord(localX, viewDx, viewDy, d0) + capX + celDriftX * 1.8F;
                float v0 = computeParallaxCoord(localZ, viewDz, viewDy, d0) + capZ + celDriftZ * 1.8F;
                float ru0 = (u0 * PLANE0_COS - v0 * PLANE0_SIN) * 0.44F;
                float rv0 = (u0 * PLANE0_SIN + v0 * PLANE0_COS) * 0.44F;
                float s0 = evaluateStarfieldIntensity(ru0, rv0, animTime * 1.15F);

                // Plane 1 (5.8 * scale): Mid-depth cosmic veil & guide stars
                float u1 = computeParallaxCoord(localX, viewDx, viewDy, d1) + capX * 1.35F - celDriftZ * 3.4F;
                float v1 = computeParallaxCoord(localZ, viewDz, viewDy, d1) + capZ * 1.35F + celDriftX * 3.4F;
                float ru1 = (u1 * PLANE1_COS - v1 * PLANE1_SIN) * 0.30F + 19.4F;
                float rv1 = (u1 * PLANE1_SIN + v1 * PLANE1_COS) * 0.30F - 11.7F;
                float s1 = evaluateStarfieldIntensity(ru1, rv1, animTime * 0.85F + 3.7F);

                // Plane 2 (11.5 * scale): Deep abyssal nebula cloud layer
                float u2 = computeParallaxCoord(localX, viewDx, viewDy, d2) + capX * 1.70F + celDriftX * 5.6F;
                float v2 = computeParallaxCoord(localZ, viewDz, viewDy, d2) + capZ * 1.70F - celDriftZ * 5.6F;
                float ru2 = (u2 * PLANE2_COS - v2 * PLANE2_SIN) * 0.19F - 31.2F;
                float rv2 = (u2 * PLANE2_SIN + v2 * PLANE2_COS) * 0.19F + 24.8F;
                float s2 = evaluateStarfieldIntensity(ru2, rv2, animTime * 0.60F + 8.1F);

                // Blend primary & secondary Materia cosmic hues across the 3 parallax planes
                float nebulaMix = Mth.clamp(s2 * 0.95F + s1 * 0.45F - 0.12F, 0.0F, 1.0F);
                float baseR = Mth.lerp(nebulaMix, pr * 0.72F, sr);
                float baseG = Mth.lerp(nebulaMix, pg * 0.72F, sg);
                float baseB = Mth.lerp(nebulaMix, pb * 0.78F, sb);

                // Extract crisp starlight peaks across all 3 planes
                float peak0 = BiomeTransitionMath.smoothstep(0.34F, 0.88F, s0);
                float peak1 = BiomeTransitionMath.smoothstep(0.36F, 0.90F, s1) * 0.85F;
                float peak2 = BiomeTransitionMath.smoothstep(0.38F, 0.92F, s2) * 0.70F;
                float starSparkle = Math.max(peak0, Math.max(peak1, peak2));

                // Subtle grazing Fresnel sky-rim shimmer layered atop the sub-surface cosmic window
                float rimShimmer = fresnel * (0.20F + 0.12F * Mth.sin((float) (localX * 1.4D - localZ * 1.2D + animTime * 1.8D)));
                float highlight = Mth.clamp(starSparkle * 0.78F + rimShimmer * 0.35F, 0.0F, 1.0F);

                outR = Mth.lerp(highlight, baseR, 0.96F);
                outG = Mth.lerp(highlight, baseG, 0.97F);
                outB = Mth.lerp(highlight, baseB, 1.00F);

                float compositeDepthGlow = 0.16F + 0.34F * (0.45F * s2 + 0.35F * s1 + 0.20F * s0)
                        + 0.36F * starSparkle + 0.18F * rimShimmer;
                outA = Mth.clamp(baseEnv * compositeDepthGlow, 0.0F, 0.78F);
            }

            case AURORA_RIBBON_REFLECTION -> {
                float diurnalScale = Mth.lerp(nightFactor, 0.48F, 1.0F);
                float baseEnv = cornerWeight * radialFade * diurnalScale * rainAttenuation * reflectionShaderScale;
                if (baseEnv <= 0.001F) {
                    outA = 0.0F;
                    return;
                }

                // Reflected sky ray + capillary ripple warp matching overhead BiomeSkyPhenomenaRenderer aurora curtains
                double safeNegDy = Math.max(0.32D, -viewDy);
                float reflX = (float) (localX + (viewDx / safeNegDy) * 2.8D);
                float reflZ = (float) (localZ + (viewDz / safeNegDy) * 2.8D);

                float ripple = Mth.sin((float) (localX * 2.35D + animTime * 2.15D))
                        * Mth.cos((float) (localZ * 2.65D - animTime * 1.85D)) * 0.42F
                        + Mth.sin((float) ((localX + localZ) * 3.8D - animTime * 2.9D)) * 0.16F;

                float curtainCurve = Mth.sin(reflX * 0.14F + animTime * 0.55F) * 2.6F
                        + Mth.cos(reflX * 0.29F - animTime * 0.36F) * 1.1F;
                float bandCoord = (reflZ + ripple) * 0.38F - curtainCurve;
                float ribbon = 0.5F + 0.5F * Mth.sin(bandCoord);
                float ribbonCrest = ribbon * ribbon * (0.60F + 0.40F * Mth.sin(reflX * 0.35F + animTime * 1.25F));
                float secondaryBand = 0.5F + 0.5F * Mth.cos(bandCoord * 1.65F + animTime * 0.80F);

                float colorMix = Mth.clamp(secondaryBand * 0.72F + (1.0F - ribbonCrest) * 0.28F, 0.0F, 1.0F);
                float r = Mth.lerp(colorMix, pr, sr);
                float g = Mth.lerp(colorMix, pg, sg);
                float b = Mth.lerp(colorMix, pb, sb);

                float crestGlint = Math.max(0.0F, ripple) * ribbonCrest * 0.45F;
                outR = Mth.lerp(crestGlint, r, 0.95F);
                outG = Mth.lerp(crestGlint, g, 1.00F);
                outB = Mth.lerp(crestGlint, b, 0.98F);

                float fresnelWeight = 0.30F + 0.70F * fresnel;
                outA = Mth.clamp(baseEnv * fresnelWeight * (0.14F + 0.48F * ribbonCrest), 0.0F, 0.68F);
            }

            case CRIMSON_TIDE_GLINT -> {
                float diurnalScale = Mth.lerp(nightFactor, 0.82F, 1.0F);
                float baseEnv = cornerWeight * radialFade * diurnalScale * rainAttenuation * reflectionShaderScale;
                if (baseEnv <= 0.001F) {
                    outA = 0.0F;
                    return;
                }

                // Slow viscous sanguine bay swell paired with fine iron-rose capillary glints
                float swell = Mth.sin((float) (localX * 0.78D + localZ * 0.62D + animTime * 1.05D))
                        * Mth.cos((float) (localX * 0.45D - localZ * 0.88D - animTime * 0.82D));
                float capillary = Mth.sin((float) (localX * 3.1D - animTime * 2.4D))
                        * Mth.cos((float) (localZ * 3.4D + animTime * 2.1D));
                float crest = Mth.clamp(0.5F + 0.38F * swell + 0.28F * capillary, 0.0F, 1.0F);
                float glint = crest * crest * crest;

                outR = Mth.lerp(glint, pr * 0.78F, sr);
                outG = Mth.lerp(glint, pg * 0.72F, sg);
                outB = Mth.lerp(glint, pb * 0.75F, sb);

                float fresnelWeight = 0.22F + 0.78F * fresnel;
                outA = Mth.clamp(baseEnv * fresnelWeight * (0.12F + 0.44F * glint), 0.0F, 0.62F);
            }

            case MIRAGE_SHIMMER_REFLECTION -> {
                // Midday thermal shimmer peaks under bright sun and softens at night
                float solarGlare = Mth.lerp(nightFactor, 1.0F, 0.46F);
                float baseEnv = cornerWeight * radialFade * solarGlare * rainAttenuation * reflectionShaderScale;
                if (baseEnv <= 0.001F) {
                    outA = 0.0F;
                    return;
                }

                float heatWaveA = Mth.sin((float) (localX * 2.15D + localZ * 1.35D + animTime * 3.10D));
                float heatWaveB = Mth.cos((float) (localX * 1.55D - localZ * 2.55D - animTime * 2.65D));
                float shimmer = 0.5F + 0.5F * Mth.sin((heatWaveA + heatWaveB) * 1.55F + animTime * 1.9F);
                float prismSplit = 0.5F + 0.5F * heatWaveA;

                float r = Mth.lerp(prismSplit, pr, sr);
                float g = Mth.lerp(prismSplit, pg, sg);
                float b = Mth.lerp(prismSplit, pb, sb);
                float sunSpark = shimmer * shimmer * fresnel * 0.42F;

                outR = Mth.lerp(sunSpark, r, 1.0F);
                outG = Mth.lerp(sunSpark, g, 0.96F);
                outB = Mth.lerp(sunSpark, b, 0.86F);

                float fresnelWeight = 0.18F + 0.82F * fresnel;
                outA = Mth.clamp(baseEnv * fresnelWeight * (0.14F + 0.46F * shimmer), 0.0F, 0.64F);
            }

            case FRESNEL_SKY_REFLECTION -> {
                float diurnalScale = Mth.lerp(nightFactor, 0.80F, 0.95F);
                float baseEnv = cornerWeight * radialFade * diurnalScale * rainAttenuation * reflectionShaderScale;
                if (baseEnv <= 0.001F) {
                    outA = 0.0F;
                    return;
                }

                float waveA = Mth.sin((float) (localX * 1.95D + localZ * 0.85D + animTime * 1.65D));
                float waveB = Mth.cos((float) (localX * 0.90D - localZ * 2.15D - animTime * 1.40D));
                float ripple = 0.5F + 0.28F * waveA + 0.22F * waveB;
                float specGlint = Math.max(0.0F, waveA * waveB);
                specGlint = specGlint * specGlint;

                float colorMix = Mth.clamp(ripple * 0.65F + fresnel * 0.35F, 0.0F, 1.0F);
                float r = Mth.lerp(colorMix, pr, sr);
                float g = Mth.lerp(colorMix, pg, sg);
                float b = Mth.lerp(colorMix, pb, sb);

                outR = Mth.lerp(specGlint * 0.38F, r, 0.98F);
                outG = Mth.lerp(specGlint * 0.38F, g, 0.99F);
                outB = Mth.lerp(specGlint * 0.38F, b, 1.00F);

                outA = Mth.clamp(baseEnv * fresnel * (0.18F + 0.36F * ripple + 0.22F * specGlint), 0.0F, 0.58F);
            }

            default -> outA = 0.0F;
        }
    }

    /**
     * Emits crisp sub-surface twinkling diamond stars across the three {@code STARLIGHT_PARALLAX}
     * depth planes ({@code 2.2F, 5.8F, 11.5F * parallaxDepthScale}) using the exact analytic inverse
     * of {@link #computeParallaxCoord(double, double, double, float)}, clipped organically to open water.
     */
    private static void renderProjectedSubSurfaceStars(
            Matrix4f mat, VertexConsumer vc,
            double camX, double camY, double camZ,
            float animTime, float nightFactor, float rainAttenuation,
            float celDriftX, float celDriftZ, float parallaxShaderScale) {

        double approxWaterY = referenceStarlightWaterY + (double) WATER_SURFACE_HEIGHT_OFFSET;
        if (camY <= approxWaterY + 0.05D) {
            return;
        }

        float diurnalScale = Mth.lerp(nightFactor, 0.30F, 1.0F) * rainAttenuation * parallaxShaderScale;
        if (diurnalScale <= 0.01F) {
            return;
        }

        for (int plane = 0; plane < 3; plane++) {
            float baseDepth = (plane == 0) ? PARALLAX_DEPTH_SHALLOW
                    : (plane == 1) ? PARALLAX_DEPTH_MID : PARALLAX_DEPTH_DEEP;
            float cellSpacing = (plane == 0) ? 1.55F : (plane == 1) ? 2.15F : 2.95F;
            float starHalfSize = (plane == 0) ? 0.052F : (plane == 1) ? 0.044F : 0.038F;
            float planeAlphaScale = (plane == 0) ? 0.88F : (plane == 1) ? 0.72F : 0.56F;
            int planeSeed = 0x517CC1B7 ^ (plane * 0x27D4EB2F);

            double driftX = (plane == 0) ? (-celDriftX * 1.8D)
                    : (plane == 1) ? (celDriftZ * 3.4D) : (-celDriftX * 5.6D);
            double driftZ = (plane == 0) ? (-celDriftZ * 1.8D)
                    : (plane == 1) ? (-celDriftX * 3.4D) : (celDriftZ * 5.6D);

            // Virtual sub-surface span corresponding to a 22-block surface radius around the camera
            double approxNegDy = Math.max(0.22D, camY - approxWaterY);
            double maxSurfaceRadius = 21.0D;
            double maxVirtualRadius = Math.min(44.0D, maxSurfaceRadius * (1.0D + baseDepth / approxNegDy));

            double camLocalX = camX - levelPrecisionOriginX - driftX;
            double camLocalZ = camZ - levelPrecisionOriginZ - driftZ;
            int minCx = Mth.floor((camLocalX - maxVirtualRadius) / cellSpacing);
            int maxCx = Mth.floor((camLocalX + maxVirtualRadius) / cellSpacing);
            int minCz = Mth.floor((camLocalZ - maxVirtualRadius) / cellSpacing);
            int maxCz = Mth.floor((camLocalZ + maxVirtualRadius) / cellSpacing);

            for (int cz = minCz; cz <= maxCz; cz++) {
                for (int cx = minCx; cx <= maxCx; cx++) {
                    int h = mixHash(cx, cz, planeSeed);
                    int rarityMask = h & 0xFF;
                    if (rarityMask < 112) {
                        continue; // Sparse natural star distribution
                    }

                    double subLocalX = (cx + 0.16D + 0.68D * (((h >>> 8) & 0xFF) / 255.0D)) * cellSpacing + driftX;
                    double subLocalZ = (cz + 0.16D + 0.68D * (((h >>> 16) & 0xFF) / 255.0D)) * cellSpacing + driftZ;
                    double subWorldX = subLocalX + levelPrecisionOriginX;
                    double subWorldZ = subLocalZ + levelPrecisionOriginZ;

                    // Step 1: Approximate surface intersection to locate the exact water tile & height
                    double approxDenom = 1.0D + baseDepth / approxNegDy;
                    double approxSurfX = camX + (subWorldX - camX) / approxDenom;
                    double approxSurfZ = camZ + (subWorldZ - camZ) / approxDenom;
                    int probeIdx = toGridIndex(Mth.floor(approxSurfX), Mth.floor(approxSurfZ));
                    if (probeIdx < 0 || !GRID_HAS_WATER[probeIdx]
                            || GRID_PRIMARY_MODE[probeIdx] != WaterOpticalMode.STARLIGHT_PARALLAX.ordinal()) {
                        continue;
                    }

                    // Step 2: Exact projection using the tile's true water Y and parallaxDepthScale
                    int waterBlockY = GRID_WATER_Y[probeIdx];
                    double exactWy = waterBlockY + (double) WATER_SURFACE_HEIGHT_OFFSET;
                    if (camY <= exactWy + 0.04D) {
                        continue;
                    }
                    float exactDepth = baseDepth * Math.max(0.25F, GRID_DEPTH_SCALE[probeIdx]);
                    double exactDenom = 1.0D + exactDepth / Math.max(0.22D, camY - exactWy);
                    double surfX = camX + (subWorldX - camX) / exactDenom;
                    double surfZ = camZ + (subWorldZ - camZ) / exactDenom;

                    int bx = Mth.floor(surfX);
                    int bz = Mth.floor(surfZ);
                    int cellIdx = toGridIndex(bx, bz);
                    if (cellIdx < 0 || !GRID_HAS_WATER[cellIdx]
                            || GRID_WATER_Y[cellIdx] != waterBlockY
                            || GRID_PRIMARY_MODE[cellIdx] != WaterOpticalMode.STARLIGHT_PARALLAX.ordinal()) {
                        continue;
                    }

                    double dx = surfX - camX;
                    double dz = surfZ - camZ;
                    double distSq = dx * dx + dz * dz;
                    if (distSq > maxSurfaceRadius * maxSurfaceRadius) {
                        continue;
                    }

                    // Soft shoreline/border clip so stars fade organically before reaching land or ice shelves
                    float shoreFactor = sampleGridShorelineBilinear(bx, bz, waterBlockY,
                            (float) (surfX - bx), (float) (surfZ - bz));
                    if (shoreFactor <= 0.18F) {
                        continue;
                    }

                    // Apply tiny capillary liquid shimmer to projected star position
                    float shimmerX = Mth.sin((float) (surfX * 2.2D + animTime * 1.7D)) * 0.014F;
                    float shimmerZ = Mth.cos((float) (surfZ * 2.2D - animTime * 1.5D)) * 0.014F;

                    float distFade = 1.0F - BiomeTransitionMath.smoothstep(9.0F, (float) maxSurfaceRadius, (float) Math.sqrt(distSq));
                    float phase = ((h >>> 24) & 0xFF) * (6.2831855F / 255.0F);
                    float twinkleSpeed = 2.2F + (((h >>> 12) & 0xF) / 15.0F) * 2.6F;
                    float twinkle = 0.52F + 0.48F * Mth.sin(animTime * twinkleSpeed + phase);
                    float mag = (rarityMask - 112) / 143.0F;

                    float alpha = GRID_PRIMARY_WEIGHT[cellIdx] * shoreFactor * distFade * diurnalScale
                            * planeAlphaScale * twinkle * (0.45F + 0.55F * mag);
                    if (alpha <= 0.008F) {
                        continue;
                    }
                    alpha = Math.min(0.90F, alpha);

                    float colorMix = ((h >>> 4) & 0xF) / 15.0F;
                    float tintR = Mth.lerp(colorMix, GRID_PRIM_R[cellIdx], GRID_SEC_R[cellIdx]);
                    float tintG = Mth.lerp(colorMix, GRID_PRIM_G[cellIdx], GRID_SEC_G[cellIdx]);
                    float tintB = Mth.lerp(colorMix, GRID_PRIM_B[cellIdx], GRID_SEC_B[cellIdx]);
                    float coreR = Mth.lerp(0.62F, tintR, 1.0F);
                    float coreG = Mth.lerp(0.62F, tintG, 1.0F);
                    float coreB = Mth.lerp(0.68F, tintB, 1.0F);

                    float rx = (float) (surfX + shimmerX - camX);
                    float ry = (float) ((waterBlockY + (double) STAR_GLINT_HEIGHT_OFFSET) - camY);
                    float rz = (float) (surfZ + shimmerZ - camZ);
                    float half = starHalfSize * (0.75F + 0.55F * mag);

                    // Crisp 4-point horizontal diamond starlight quad (+Y normal)
                    emitUpwardVertex(mat, vc, rx, ry, rz - half, coreR, coreG, coreB, alpha, 0.5F, 0.0F);
                    emitUpwardVertex(mat, vc, rx - half, ry, rz, tintR, tintG, tintB, alpha * 0.85F, 0.0F, 0.5F);
                    emitUpwardVertex(mat, vc, rx, ry, rz + half, coreR, coreG, coreB, alpha, 0.5F, 1.0F);
                    emitUpwardVertex(mat, vc, rx + half, ry, rz, tintR, tintG, tintB, alpha * 0.85F, 1.0F, 0.5F);
                }
            }
        }
    }

    private static void emitUpwardVertex(
            Matrix4f mat, VertexConsumer vc,
            float x, float y, float z,
            float r, float g, float b, float a,
            float u, float v) {
        vc.addVertex(mat, x, y, z)
                .setColor(Mth.clamp(r, 0.0F, 1.0F), Mth.clamp(g, 0.0F, 1.0F), Mth.clamp(b, 0.0F, 1.0F), Mth.clamp(a, 0.0F, 1.0F))
                .setUv(u, v)
                .setOverlay(NO_OVERLAY)
                .setLight(FULL_LIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    // =========================================================================
    // Zero-Allocation 5-Tick Water-Surface Scan & 3x3 Biome Optical Tap
    // =========================================================================

    private static boolean shouldRefreshCache(long gameTime, int camBx, int camBy, int camBz) {
        if (lastScanGameTime == Long.MIN_VALUE || Math.abs(gameTime - lastScanGameTime) >= CACHE_REFRESH_TICKS) {
            return true;
        }
        return Math.abs(camBx - cachedAnchorX) >= CAMERA_MOVE_RESCAN_THRESHOLD
                || Math.abs(camBy - cachedAnchorY) >= CAMERA_MOVE_RESCAN_THRESHOLD
                || Math.abs(camBz - cachedAnchorZ) >= CAMERA_MOVE_RESCAN_THRESHOLD;
    }

    private static void ensureLevelConsistency(ClientLevel level, int camBx, int camBz) {
        ClientLevel prev = cachedLevelRef != null ? cachedLevelRef.get() : null;
        if (prev != level) {
            cachedLevelRef = new WeakReference<>(level);
            lastScanGameTime = Long.MIN_VALUE;
            activeTileCount = 0;
            hasStarlightParallaxTiles = false;
            levelPrecisionOriginX = camBx & ~15;
            levelPrecisionOriginZ = camBz & ~15;
        } else if (Math.abs(camBx - levelPrecisionOriginX) > 32768 || Math.abs(camBz - levelPrecisionOriginZ) > 32768) {
            // Re-center precision origin only across long-distance teleports (>32km)
            levelPrecisionOriginX = camBx & ~15;
            levelPrecisionOriginZ = camBz & ~15;
            lastScanGameTime = Long.MIN_VALUE;
        }
    }

    private static void refreshWaterSurfaceCache(ClientLevel level, long gameTime, int anchorX, int anchorY, int anchorZ) {
        lastScanGameTime = gameTime;
        cachedAnchorX = anchorX;
        cachedAnchorY = anchorY;
        cachedAnchorZ = anchorZ;

        Arrays.fill(GRID_HAS_WATER, false);
        Arrays.fill(GRID_PRIMARY_MODE, (byte) 0);
        Arrays.fill(GRID_PRIMARY_WEIGHT, 0.0F);
        Arrays.fill(GRID_SECONDARY_MODE, (byte) 0);
        Arrays.fill(GRID_SECONDARY_WEIGHT, 0.0F);

        ChunkSource chunkSource = level.getChunkSource();
        int lastChunkX = Integer.MIN_VALUE;
        int lastChunkZ = Integer.MIN_VALUE;
        LevelChunk cachedChunk = null;

        int minProbeY = Math.max(level.getMinY(), anchorY - 22);
        int maxProbeY = Math.min(level.getMaxY() - 2, anchorY + 14);
        int maxOuterSq = CACHE_HALF_EXTENT * CACHE_HALF_EXTENT;

        // Step 1: Locate exposed still top FluidTags.WATER blocks with sky light >= 4
        int cellIdx = 0;
        for (int dz = -CACHE_HALF_EXTENT; dz <= CACHE_HALF_EXTENT; dz++) {
            int wz = anchorZ + dz;
            int chunkZ = wz >> 4;
            for (int dx = -CACHE_HALF_EXTENT; dx <= CACHE_HALF_EXTENT; dx++, cellIdx++) {
                if (dx * dx + dz * dz > maxOuterSq) {
                    continue;
                }
                int wx = anchorX + dx;
                int chunkX = wx >> 4;
                if (chunkX != lastChunkX || chunkZ != lastChunkZ) {
                    lastChunkX = chunkX;
                    lastChunkZ = chunkZ;
                    cachedChunk = chunkSource.getChunkNow(chunkX, chunkZ);
                }
                if (cachedChunk == null) {
                    continue;
                }

                int waterY = findExposedStillWaterY(level, cachedChunk, wx, wz, minProbeY, maxProbeY, anchorY);
                if (waterY == Integer.MIN_VALUE) {
                    continue;
                }

                ABOVE_POS.set(wx, waterY + 1, wz);
                PROBE_POS.set(wx, waterY, wz);
                int skyLight = Math.max(
                        level.getBrightness(LightLayer.SKY, ABOVE_POS),
                        level.getBrightness(LightLayer.SKY, PROBE_POS)
                );
                if (skyLight < 4) {
                    continue;
                }

                GRID_HAS_WATER[cellIdx] = true;
                GRID_WATER_Y[cellIdx] = (short) waterY;
                GRID_SKY_EXPOSURE[cellIdx] = BiomeTransitionMath.smoothstep(3.5F, 10.5F, (float) skyLight);
                sampleBiomeOptics3x3Tap(level, wx, waterY + 1, wz, cellIdx);
            }
        }

        // Step 2: Build compact active water-tile list with 4-corner shoreline & biome-averaged weights
        activeTileCount = 0;
        hasStarlightParallaxTiles = false;
        int nearestStarlightDistSq = Integer.MAX_VALUE;
        int maxActiveSq = ACTIVE_TILE_SCAN_RADIUS * ACTIVE_TILE_SCAN_RADIUS;

        for (int dz = -ACTIVE_TILE_SCAN_RADIUS; dz <= ACTIVE_TILE_SCAN_RADIUS; dz++) {
            for (int dx = -ACTIVE_TILE_SCAN_RADIUS; dx <= ACTIVE_TILE_SCAN_RADIUS; dx++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > maxActiveSq) {
                    continue;
                }
                int gIdx = (dz + CACHE_HALF_EXTENT) * CACHE_SPAN + (dx + CACHE_HALF_EXTENT);
                if (!GRID_HAS_WATER[gIdx]) {
                    continue;
                }
                byte modeOrd = GRID_PRIMARY_MODE[gIdx];
                float baseW = GRID_PRIMARY_WEIGHT[gIdx];
                if (modeOrd == 0 || baseW <= 0.004F) {
                    continue;
                }

                int bx = anchorX + dx;
                int bz = anchorZ + dz;
                short by = GRID_WATER_Y[gIdx];

                float c00 = computeCornerCompositeWeight(dx, dz, -1, -1, by, modeOrd);
                float c10 = computeCornerCompositeWeight(dx, dz, 1, -1, by, modeOrd);
                float c11 = computeCornerCompositeWeight(dx, dz, 1, 1, by, modeOrd);
                float c01 = computeCornerCompositeWeight(dx, dz, -1, 1, by, modeOrd);

                if (c00 <= 0.002F && c10 <= 0.002F && c11 <= 0.002F && c01 <= 0.002F) {
                    continue;
                }

                int out = activeTileCount++;
                TILE_BX[out] = bx;
                TILE_BY[out] = by;
                TILE_BZ[out] = bz;
                TILE_GRID_INDEX[out] = gIdx;
                TILE_MODE[out] = modeOrd;
                TILE_PRIM_R[out] = GRID_PRIM_R[gIdx];
                TILE_PRIM_G[out] = GRID_PRIM_G[gIdx];
                TILE_PRIM_B[out] = GRID_PRIM_B[gIdx];
                TILE_SEC_R[out] = GRID_SEC_R[gIdx];
                TILE_SEC_G[out] = GRID_SEC_G[gIdx];
                TILE_SEC_B[out] = GRID_SEC_B[gIdx];
                TILE_DEPTH_SCALE[out] = GRID_DEPTH_SCALE[gIdx];
                TILE_CORNER_W00[out] = c00;
                TILE_CORNER_W10[out] = c10;
                TILE_CORNER_W11[out] = c11;
                TILE_CORNER_W01[out] = c01;

                if (modeOrd == WaterOpticalMode.STARLIGHT_PARALLAX.ordinal()) {
                    hasStarlightParallaxTiles = true;
                    if (distSq < nearestStarlightDistSq) {
                        nearestStarlightDistSq = distSq;
                        referenceStarlightWaterY = by;
                    }
                }
            }
        }
    }

    private static int findExposedStillWaterY(
            ClientLevel level, LevelChunk chunk,
            int wx, int wz, int minProbeY, int maxProbeY, int anchorY) {
        // Fast path 1: Top world surface (handles ~98% of open skerries, fjords, and bays in O(1))
        int topSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz) - 1;
        if (topSurfaceY >= minProbeY && topSurfaceY <= maxProbeY
                && isExposedStillTopWater(level, chunk, wx, topSurfaceY, wz)) {
            return topSurfaceY;
        }

        // Fast path 2: Motion-blocking-no-leaves surface (handles overhanging foliage above pools)
        int noLeavesY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz) - 1;
        if (noLeavesY != topSurfaceY && noLeavesY >= minProbeY && noLeavesY <= maxProbeY
                && isExposedStillTopWater(level, chunk, wx, noLeavesY, wz)) {
            return noLeavesY;
        }

        // Bounded local vertical scan for water beneath natural ice arches, bridges, or sea cliffs
        int scanTop = Math.min(maxProbeY, Math.min(topSurfaceY - 1, anchorY + 6));
        int scanBottom = Math.max(minProbeY, anchorY - 12);
        for (int y = scanTop; y >= scanBottom; y--) {
            if (isExposedStillTopWater(level, chunk, wx, y, wz)) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isExposedStillTopWater(BlockGetter level, LevelChunk chunk, int wx, int wy, int wz) {
        PROBE_POS.set(wx, wy, wz);
        FluidState fluid = chunk.getFluidState(PROBE_POS);
        if (!fluid.is(FluidTags.WATER) || !fluid.isSource()) {
            return false;
        }
        BlockState waterBlockState = chunk.getBlockState(PROBE_POS);
        if (Block.isFaceFull(waterBlockState.getCollisionShape(level, PROBE_POS), Direction.UP)) {
            // Reject waterlogged blocks with a solid top face (e.g. top slabs, upside-down stairs)
            return false;
        }

        ABOVE_POS.set(wx, wy + 1, wz);
        BlockState aboveState = chunk.getBlockState(ABOVE_POS);
        if (!aboveState.getFluidState().isEmpty()) {
            return false;
        }
        if (aboveState.isAir()) {
            return true;
        }
        if (aboveState.canOcclude()) {
            return false;
        }
        // Reject translucent full-bottom ice sheets (ice, glimmer_ice, rotten_ice) so ice shelves feather cleanly
        return !Block.isFaceFull(aboveState.getCollisionShape(level, ABOVE_POS), Direction.DOWN);
    }

    /**
     * Evaluates a smooth {@code 3x3} spatial biome tap around {@code (wx, sampleY, wz)} so biome
     * borders fade seamlessly without single-block or chunk-edge steps, compatible with TerraBlender.
     */
    private static void sampleBiomeOptics3x3Tap(ClientLevel level, int wx, int sampleY, int wz, int cellIdx) {
        Arrays.fill(TAP_MODE_PRESENCE, 0.0F);
        Arrays.fill(TAP_MODE_INTENSITY, 0.0F);
        Arrays.fill(TAP_MODE_DEPTH, 0.0F);
        Arrays.fill(TAP_MODE_PR, 0.0F);
        Arrays.fill(TAP_MODE_PG, 0.0F);
        Arrays.fill(TAP_MODE_PB, 0.0F);
        Arrays.fill(TAP_MODE_SR, 0.0F);
        Arrays.fill(TAP_MODE_SG, 0.0F);
        Arrays.fill(TAP_MODE_SB, 0.0F);

        int tapIdx = 0;
        for (int oz = -1; oz <= 1; oz++) {
            int tz = wz + oz * BIOME_TAP_STRIDE;
            for (int ox = -1; ox <= 1; ox++, tapIdx++) {
                int tx = wx + ox * BIOME_TAP_STRIDE;
                BIOME_TAP_POS.set(tx, sampleY, tz);
                BiomeAtmosphereProfile profile = EntropicaBiomeAtmospheres.getForHolder(level.getBiome(BIOME_TAP_POS));
                if (profile == null) {
                    continue;
                }
                WaterOpticalMode mode = profile.getWaterOpticalMode();
                if (mode == null || mode == WaterOpticalMode.NONE) {
                    continue;
                }
                float intensity = profile.getWaterOpticalIntensity();
                if (intensity <= 0.0F) {
                    continue;
                }

                float tw = TAP_WEIGHTS_3X3[tapIdx];
                int ord = mode.ordinal();
                int pRgb = profile.getWaterPrimaryRgb();
                int sRgb = profile.getWaterSecondaryRgb();

                TAP_MODE_PRESENCE[ord] += tw;
                TAP_MODE_INTENSITY[ord] += tw * intensity;
                TAP_MODE_DEPTH[ord] += tw * profile.getWaterParallaxDepthScale();
                TAP_MODE_PR[ord] += tw * (((pRgb >> 16) & 0xFF) / 255.0F);
                TAP_MODE_PG[ord] += tw * (((pRgb >> 8) & 0xFF) / 255.0F);
                TAP_MODE_PB[ord] += tw * ((pRgb & 0xFF) / 255.0F);
                TAP_MODE_SR[ord] += tw * (((sRgb >> 16) & 0xFF) / 255.0F);
                TAP_MODE_SG[ord] += tw * (((sRgb >> 8) & 0xFF) / 255.0F);
                TAP_MODE_SB[ord] += tw * ((sRgb & 0xFF) / 255.0F);
            }
        }

        int bestOrd = 0;
        float bestIntensity = 0.0F;
        int secondOrd = 0;
        float secondIntensity = 0.0F;

        for (int ord = 1; ord < MODE_COUNT; ord++) {
            float weighted = TAP_MODE_INTENSITY[ord];
            if (weighted > bestIntensity) {
                secondIntensity = bestIntensity;
                secondOrd = bestOrd;
                bestIntensity = weighted;
                bestOrd = ord;
            } else if (weighted > secondIntensity) {
                secondIntensity = weighted;
                secondOrd = ord;
            }
        }

        if (bestOrd > 0 && TAP_MODE_PRESENCE[bestOrd] > 1.0e-4F) {
            float invP = 1.0F / TAP_MODE_PRESENCE[bestOrd];
            float smoothPresence = BiomeTransitionMath.smoothstep(TAP_MODE_PRESENCE[bestOrd]);
            float avgIntensity = TAP_MODE_INTENSITY[bestOrd] * invP;

            GRID_PRIMARY_MODE[cellIdx] = (byte) bestOrd;
            GRID_PRIMARY_WEIGHT[cellIdx] = avgIntensity * smoothPresence;
            GRID_PRIM_R[cellIdx] = TAP_MODE_PR[bestOrd] * invP;
            GRID_PRIM_G[cellIdx] = TAP_MODE_PG[bestOrd] * invP;
            GRID_PRIM_B[cellIdx] = TAP_MODE_PB[bestOrd] * invP;
            GRID_SEC_R[cellIdx] = TAP_MODE_SR[bestOrd] * invP;
            GRID_SEC_G[cellIdx] = TAP_MODE_SG[bestOrd] * invP;
            GRID_SEC_B[cellIdx] = TAP_MODE_SB[bestOrd] * invP;
            GRID_DEPTH_SCALE[cellIdx] = TAP_MODE_DEPTH[bestOrd] * invP;
        }

        if (secondOrd > 0 && TAP_MODE_PRESENCE[secondOrd] > 1.0e-4F) {
            float invS = 1.0F / TAP_MODE_PRESENCE[secondOrd];
            float smoothPresence = BiomeTransitionMath.smoothstep(TAP_MODE_PRESENCE[secondOrd]);
            float avgIntensity = TAP_MODE_INTENSITY[secondOrd] * invS;

            GRID_SECONDARY_MODE[cellIdx] = (byte) secondOrd;
            GRID_SECONDARY_WEIGHT[cellIdx] = avgIntensity * smoothPresence;
            GRID_ALT_PRIM_R[cellIdx] = TAP_MODE_PR[secondOrd] * invS;
            GRID_ALT_PRIM_G[cellIdx] = TAP_MODE_PG[secondOrd] * invS;
            GRID_ALT_PRIM_B[cellIdx] = TAP_MODE_PB[secondOrd] * invS;
            GRID_ALT_SEC_R[cellIdx] = TAP_MODE_SR[secondOrd] * invS;
            GRID_ALT_SEC_G[cellIdx] = TAP_MODE_SG[secondOrd] * invS;
            GRID_ALT_SEC_B[cellIdx] = TAP_MODE_SB[secondOrd] * invS;
            GRID_ALT_DEPTH_SCALE[cellIdx] = TAP_MODE_DEPTH[secondOrd] * invS;
        }
    }

    /**
     * Computes a continuous shared-vertex weight across the {@code 2x2} grid cells touching a block
     * corner, seamlessly combining shoreline/ice-shelf edge softening, sky exposure, and {@code 3x3}
     * biome optical weights so adjacent water quads join with zero grid seams.
     */
    private static float computeCornerCompositeWeight(
            int dx, int dz, int stepX, int stepZ, short targetWaterY, byte modeOrd) {
        int openWaterNeighbors = 0;
        float weightAccum = 0.0F;

        for (int iz = 0; iz <= 1; iz++) {
            int nz = dz + iz * stepZ;
            for (int ix = 0; ix <= 1; ix++) {
                int nx = dx + ix * stepX;
                int idx = (nz + CACHE_HALF_EXTENT) * CACHE_SPAN + (nx + CACHE_HALF_EXTENT);
                if (GRID_HAS_WATER[idx] && GRID_WATER_Y[idx] == targetWaterY) {
                    openWaterNeighbors++;
                    float sky = GRID_SKY_EXPOSURE[idx];
                    if (GRID_PRIMARY_MODE[idx] == modeOrd) {
                        weightAccum += GRID_PRIMARY_WEIGHT[idx] * sky;
                    } else if (GRID_SECONDARY_MODE[idx] == modeOrd) {
                        weightAccum += GRID_SECONDARY_WEIGHT[idx] * sky;
                    }
                }
            }
        }

        if (openWaterNeighbors <= 0) {
            return 0.0F;
        }

        // Organic shoreline & ice-shelf feathering: 4/4 open water = 1.0F, tapering softly into shores
        float shorelineFactor = switch (openWaterNeighbors) {
            case 4 -> 1.00F;
            case 3 -> 0.64F;
            case 2 -> 0.20F;
            default -> 0.05F;
        };
        float avgBiomeOpticalWeight = weightAccum / (float) openWaterNeighbors;
        return avgBiomeOpticalWeight * shorelineFactor;
    }

    private static float sampleGridShorelineBilinear(int bx, int bz, int waterY, float fx, float fz) {
        int dx = bx - cachedAnchorX;
        int dz = bz - cachedAnchorZ;
        if (Math.abs(dx) > ACTIVE_TILE_SCAN_RADIUS || Math.abs(dz) > ACTIVE_TILE_SCAN_RADIUS) {
            return 0.0F;
        }
        float s00 = cornerOpenWaterFactor(dx, dz, -1, -1, waterY);
        float s10 = cornerOpenWaterFactor(dx, dz, 1, -1, waterY);
        float s11 = cornerOpenWaterFactor(dx, dz, 1, 1, waterY);
        float s01 = cornerOpenWaterFactor(dx, dz, -1, 1, waterY);
        return bilerp(s00, s10, s01, s11, Mth.clamp(fx, 0.0F, 1.0F), Mth.clamp(fz, 0.0F, 1.0F));
    }

    private static float cornerOpenWaterFactor(int dx, int dz, int stepX, int stepZ, int waterY) {
        int open = 0;
        for (int iz = 0; iz <= 1; iz++) {
            int nz = dz + iz * stepZ;
            for (int ix = 0; ix <= 1; ix++) {
                int nx = dx + ix * stepX;
                int idx = (nz + CACHE_HALF_EXTENT) * CACHE_SPAN + (nx + CACHE_HALF_EXTENT);
                if (idx >= 0 && idx < CACHE_CELL_COUNT && GRID_HAS_WATER[idx] && GRID_WATER_Y[idx] == waterY) {
                    open++;
                }
            }
        }
        return switch (open) {
            case 4 -> 1.0F;
            case 3 -> 0.65F;
            case 2 -> 0.18F;
            default -> 0.0F;
        };
    }

    private static int toGridIndex(int worldX, int worldZ) {
        int gx = worldX - cachedAnchorX + CACHE_HALF_EXTENT;
        int gz = worldZ - cachedAnchorZ + CACHE_HALF_EXTENT;
        if (gx < 0 || gx >= CACHE_SPAN || gz < 0 || gz >= CACHE_SPAN) {
            return -1;
        }
        return gz * CACHE_SPAN + gx;
    }

    // =========================================================================
    // Internal Pure-Math Noise, Envelope & Config Helpers
    // =========================================================================

    private static float computeRadialDistanceFade(float horizDist) {
        if (horizDist >= MAX_RENDER_RADIUS) {
            return 0.0F;
        }
        if (horizDist <= FADE_INNER_RADIUS) {
            return 1.0F;
        }
        float t = (horizDist - FADE_INNER_RADIUS) / (MAX_RENDER_RADIUS - FADE_INNER_RADIUS);
        return 1.0F - BiomeTransitionMath.smootherstep(t);
    }

    private static float bilerp(float v00, float v10, float v01, float v11, float tx, float tz) {
        float top = Mth.lerp(tx, v00, v10);
        float bot = Mth.lerp(tx, v01, v11);
        return Mth.lerp(tz, top, bot);
    }

    private static float valueNoise2D(float x, float z, int seed) {
        int ix = Mth.floor(x);
        int iz = Mth.floor(z);
        float fx = x - ix;
        float fz = z - iz;
        float ux = fx * fx * (3.0F - 2.0F * fx);
        float uz = fz * fz * (3.0F - 2.0F * fz);

        float n00 = hash01(ix, iz, seed);
        float n10 = hash01(ix + 1, iz, seed);
        float n01 = hash01(ix, iz + 1, seed);
        float n11 = hash01(ix + 1, iz + 1, seed);
        return bilerp(n00, n10, n01, n11, ux, uz);
    }

    /**
     * Evaluates a jittered cellular starlight octave across the 4 nearest cells (exact for star
     * centers inside {@code [0.18, 0.82]} and {@code radius <= 0.48F}, zero cell-edge clipping).
     */
    private static float sampleCellularStarOctave(
            float x, float z, float time, int seed, float radius, float baseTwinkleSpeed) {
        int cx = Mth.floor(x);
        int cz = Mth.floor(z);
        float fx = x - cx;
        float fz = z - cz;

        int startX = (fx < 0.5F) ? (cx - 1) : cx;
        int startZ = (fz < 0.5F) ? (cz - 1) : cz;
        float radiusSq = radius * radius;
        float best = 0.0F;

        for (int nz = startZ; nz <= startZ + 1; nz++) {
            for (int nx = startX; nx <= startX + 1; nx++) {
                int h = mixHash(nx, nz, seed);
                float magRaw = (h & 0xFF) / 255.0F;
                if (magRaw < 0.28F) {
                    continue;
                }

                float sx = nx + 0.18F + 0.64F * (((h >>> 8) & 0xFF) / 255.0F);
                float sz = nz + 0.18F + 0.64F * (((h >>> 16) & 0xFF) / 255.0F);
                float dx = x - sx;
                float dz = z - sz;
                float dSq = dx * dx + dz * dz;
                if (dSq >= radiusSq) {
                    continue;
                }

                float d = (float) Math.sqrt(dSq);
                float falloff = 1.0F - (d / radius);
                float kernel = falloff * falloff * (0.45F + 0.55F * falloff);
                float phase = ((h >>> 24) & 0xFF) * (6.2831855F / 255.0F);
                float speed = baseTwinkleSpeed * (0.75F + 0.50F * (((h >>> 12) & 0xF) / 15.0F));
                float twinkle = 0.58F + 0.42F * Mth.sin(time * speed + phase);
                float magnitude = (magRaw - 0.28F) / 0.72F;
                float starVal = kernel * twinkle * (0.38F + 0.62F * magnitude);
                if (starVal > best) {
                    best = starVal;
                }
            }
        }
        return best;
    }

    private static float hash01(int x, int z, int seed) {
        int h = mixHash(x, z, seed);
        return (h & 0xFFFF) / 65535.0F;
    }

    private static int mixHash(int x, int z, int seed) {
        int h = seed ^ (x * 0x27D4EB2D) ^ (z * 0x165667B1);
        h = (h ^ (h >>> 15)) * 0x85EBCA6B;
        h = (h ^ (h >>> 13)) * 0xC2B2AE35;
        return h ^ (h >>> 16);
    }

    private static boolean isWaterEffectsEnabled() {
        try {
            Boolean enabled = EntropicaConfig.ENABLE_BIOME_WATER_EFFECTS.get();
            return enabled == null || enabled;
        } catch (Throwable ignored) {
            return true;
        }
    }
}
