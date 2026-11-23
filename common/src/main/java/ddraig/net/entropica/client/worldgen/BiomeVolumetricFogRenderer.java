package ddraig.net.entropica.client.worldgen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.worldgen.biome.BiomeAtmosphereProfile;
import ddraig.net.entropica.worldgen.biome.BiomeTransitionMath;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomeAtmospheres;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.lang.ref.WeakReference;

/**
 * 100% TerraBlender, Sodium/Rubidium/Embeddium, and Iris/Oculus Shader-Compatible
 * Low-Lying Volumetric Biome Mist Renderer.
 *
 * <p>Renders three stacked, domain-warped undulating horizontal mist strata across a cached
 * world-aligned {@code 25x25} cell grid ({@code 4}-block spacing, {@code 48}-block outer radius)
 * using shared {@code 26x26} corner nodes filtered over a {@code 3x3} binomial neighborhood.
 * Uses standard {@link MultiBufferSource.BufferSource} with {@link RenderType#entityTranslucent}
 * and {@link RenderType#entityTranslucentEmissive} and zero raw OpenGL state calls so Materia-rich
 * coastal channels, steaming skerries, and glowing astral basins integrate cleanly with shader
 * G-buffers, deferred bloom, and Sodium chunk pipelines.</p>
 */
public final class BiomeVolumetricFogRenderer {
    /** Outer radial extent (in blocks) where volumetric mist C1-fades completely to {@code 0.0F}. */
    public static final float MAX_MIST_RADIUS = 48.0F;

    /** Inner near-plane safety exclusion radius (in blocks) inside which mist alpha is strictly {@code 0.0F}. */
    public static final float NEAR_CLIP_SAFE_RADIUS = 3.0F;

    private static final float NEAR_PEAK_START_BLOCKS = 7.0F;
    private static final float FAR_FADE_START_BLOCKS = 28.0F;

    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;

    // 3 stacked vertical mist strata inside [surfaceY + baseOffset .. surfaceY + baseOffset + thickness]
    private static final float[] STRATUM_Y_FRACTIONS = { 0.18F, 0.50F, 0.82F };
    private static final int STRATA_COUNT = 3;

    // World-aligned 4-block cell grid: 25x25 active cells, 26x26 shared corners, 28x28 raw column probes
    private static final int CELL_SIZE = 4;
    private static final int GRID_CELLS = 25;
    private static final int HALF_CELLS = GRID_CELLS / 2; // 12 -> [-48..+52] around snapped camera cell
    private static final int CELL_COUNT = GRID_CELLS * GRID_CELLS; // 625

    private static final int CORNER_AXIS = GRID_CELLS + 1; // 26
    private static final int CORNER_COUNT = CORNER_AXIS * CORNER_AXIS; // 676

    private static final int RAW_AXIS = CORNER_AXIS + 2; // 28 (provides full 3x3 tap around every 26x26 corner)
    private static final int RAW_COUNT = RAW_AXIS * RAW_AXIS; // 784

    // Cache refresh thresholds: at most once every 4 ticks or >= 2.0 blocks camera movement
    private static final long CACHE_REFRESH_TICKS = 4L;
    private static final double CACHE_MOVE_THRESHOLD_SQ = 4.0D;

    // Minimum sky brightness required above surface (prevents indoor / underground cave rendering)
    private static final int MIN_SKY_LIGHT = 4;

    // Per-sheet base opacity calibration (accumulates smoothly across 3 strata & oblique sightlines)
    private static final float TRANSLUCENT_SHEET_OPACITY = 0.28F;
    private static final float EMISSIVE_SHEET_OPACITY = 0.24F;

    // Separable 3x3 binomial Gaussian weights ([1, 2, 1; 2, 4, 2; 1, 2, 1] / 16)
    private static final float[] KERNEL_3X3 = {
            1.0F, 2.0F, 1.0F,
            2.0F, 4.0F, 2.0F,
            1.0F, 2.0F, 1.0F
    };

    // Precomputed radial Back-to-Front cell traversal order (exact along every camera sight ray, zero frame sort)
    private static final int[] BACK_TO_FRONT_CELLS = buildBackToFrontCellOrder();

    // =========================================================================
    // Pre-Allocated Zero-GC Spatial Cache Buffers (Client Render Thread Only)
    // =========================================================================

    private static final boolean[] RAW_VALID = new boolean[RAW_COUNT];
    private static final boolean[] RAW_HAS_PROFILE = new boolean[RAW_COUNT];
    private static final boolean[] RAW_EMISSIVE = new boolean[RAW_COUNT];
    private static final float[] RAW_SURFACE_Y = new float[RAW_COUNT];
    private static final float[] RAW_DENSITY = new float[RAW_COUNT];
    private static final float[] RAW_DAY_R = new float[RAW_COUNT];
    private static final float[] RAW_DAY_G = new float[RAW_COUNT];
    private static final float[] RAW_DAY_B = new float[RAW_COUNT];
    private static final float[] RAW_NIGHT_R = new float[RAW_COUNT];
    private static final float[] RAW_NIGHT_G = new float[RAW_COUNT];
    private static final float[] RAW_NIGHT_B = new float[RAW_COUNT];
    private static final float[] RAW_BASE_OFFSET = new float[RAW_COUNT];
    private static final float[] RAW_THICKNESS = new float[RAW_COUNT];
    private static final float[] RAW_DRIFT_SPEED = new float[RAW_COUNT];
    private static final int[] RAW_SKY_LIGHT = new int[RAW_COUNT];
    private static final int[] RAW_BLOCK_LIGHT = new int[RAW_COUNT];

    private static final boolean[] CORNER_HAS_PROFILE = new boolean[CORNER_COUNT];
    private static final float[] CORNER_SURFACE_Y = new float[CORNER_COUNT];
    private static final float[] CORNER_DENSITY = new float[CORNER_COUNT];
    private static final float[] CORNER_DAY_R = new float[CORNER_COUNT];
    private static final float[] CORNER_DAY_G = new float[CORNER_COUNT];
    private static final float[] CORNER_DAY_B = new float[CORNER_COUNT];
    private static final float[] CORNER_NIGHT_R = new float[CORNER_COUNT];
    private static final float[] CORNER_NIGHT_G = new float[CORNER_COUNT];
    private static final float[] CORNER_NIGHT_B = new float[CORNER_COUNT];
    private static final float[] CORNER_BASE_OFFSET = new float[CORNER_COUNT];
    private static final float[] CORNER_THICKNESS = new float[CORNER_COUNT];
    private static final float[] CORNER_DRIFT_SPEED = new float[CORNER_COUNT];
    private static final float[] CORNER_EMISSIVE_FRAC = new float[CORNER_COUNT];
    private static final int[] CORNER_PACKED_LIGHT = new int[CORNER_COUNT];

    private static final boolean[] CELL_ACTIVE_TRANSLUCENT = new boolean[CELL_COUNT];
    private static final boolean[] CELL_ACTIVE_EMISSIVE = new boolean[CELL_COUNT];
    // Fallback anchor values per cell used ONLY for corners whose CORNER_DENSITY == 0.0F (cliff wall / biome edge)
    private static final float[] CELL_FALLBACK_BASE_Y = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_THICKNESS = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_DAY_R = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_DAY_G = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_DAY_B = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_NIGHT_R = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_NIGHT_G = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_NIGHT_B = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_DRIFT = new float[CELL_COUNT];
    private static final float[] CELL_FALLBACK_EMISSIVE = new float[CELL_COUNT];
    private static final int[] CELL_FALLBACK_LIGHT = new int[CELL_COUNT];

    // Per-frame evaluated shared corner scratch arrays (zero allocations per frame)
    private static final float[] FRAME_CORNER_R = new float[CORNER_COUNT];
    private static final float[] FRAME_CORNER_G = new float[CORNER_COUNT];
    private static final float[] FRAME_CORNER_B = new float[CORNER_COUNT];
    private static final float[] FRAME_CORNER_ENV = new float[CORNER_COUNT];

    private static final BlockPos.MutableBlockPos MUTABLE_SAMPLE_POS = new BlockPos.MutableBlockPos();

    private static WeakReference<ClientLevel> cachedLevelRef = null;
    private static long lastCacheGameTime = Long.MIN_VALUE;
    private static double lastCacheCamX = Double.NaN;
    private static double lastCacheCamY = Double.NaN;
    private static double lastCacheCamZ = Double.NaN;
    private static int gridOriginX = 0;
    private static int gridOriginZ = 0;
    private static boolean hasAnyTranslucentCells = false;
    private static boolean hasAnyEmissiveCells = false;

    private BiomeVolumetricFogRenderer() {}

    // =========================================================================
    // Pure Headless-Safe Deterministic Math Helpers
    // =========================================================================

    /**
     * Evaluates the smooth radial distance envelope for volumetric mist quads around the camera.
     *
     * <ul>
     *     <li>Strictly {@code 0.0F} inside {@code <= 3.0F} blocks so mist quads never clip the camera near plane.</li>
     *     <li>C1 Hermite ramp across {@code 3.0F .. 7.0F} blocks.</li>
     *     <li>Full {@code 1.0F} plateau across {@code 7.0F .. 28.0F} blocks (for standard {@code maxRadius = 48.0F}).</li>
     *     <li>C1 Hermite fade to {@code 0.0F} at {@code maxRadius}.</li>
     * </ul>
     */
    public static float computeMistDistanceEnvelope(float horizontalDist, float maxRadius) {
        if (!Float.isFinite(horizontalDist) || !Float.isFinite(maxRadius)) {
            return 0.0F;
        }
        if (maxRadius <= NEAR_CLIP_SAFE_RADIUS || horizontalDist <= NEAR_CLIP_SAFE_RADIUS || horizontalDist >= maxRadius) {
            return 0.0F;
        }

        float nearFull = Math.min(NEAR_PEAK_START_BLOCKS,
                NEAR_CLIP_SAFE_RADIUS + (maxRadius - NEAR_CLIP_SAFE_RADIUS) * 0.25F);
        float farStart = Math.max(nearFull,
                Math.min(FAR_FADE_START_BLOCKS, maxRadius - Math.max(4.0F, (maxRadius - nearFull) * 0.35F)));

        if (horizontalDist < nearFull) {
            return BiomeTransitionMath.smoothstep(NEAR_CLIP_SAFE_RADIUS, nearFull, horizontalDist);
        }
        if (horizontalDist <= farStart) {
            return 1.0F;
        }
        return 1.0F - BiomeTransitionMath.smoothstep(farStart, maxRadius, horizontalDist);
    }

    /**
     * Evaluates the smooth C1 vertical bell-curve opacity across stacked mist strata {@code [0, 1]},
     * gently modulated by the local domain-warped {@code driftWave} in {@code [-1, 1]}.
     *
     * <p>Vanishes with zero value and zero derivative at both {@code 0.0F} (ground/water contact)
     * and {@code 1.0F} (upper mist crown), peaking smoothly at {@code 0.50F}.</p>
     */
    public static float computeVerticalStratumAlpha(float normalizedLayerHeight, float driftWave) {
        if (!Float.isFinite(normalizedLayerHeight) || normalizedLayerHeight <= 0.0F || normalizedLayerHeight >= 1.0F) {
            return 0.0F;
        }
        float wave = Float.isFinite(driftWave) ? Mth.clamp(driftWave, -1.0F, 1.0F) : 0.0F;
        float arch = 4.0F * normalizedLayerHeight * (1.0F - normalizedLayerHeight);
        float bell = BiomeTransitionMath.smoothstep(arch);
        float waveModulation = 0.84F + 0.16F * wave;
        return BiomeTransitionMath.clamp01(bell * waveModulation);
    }

    /**
     * Evaluates a continuous, large-coordinate-safe domain-warped sine/cosine drift wave in {@code [-1, 1]}
     * for world position {@code (worldX, worldZ)} and vertical stratum index {@code stratumIndex}.
     */
    public static float sampleDomainWarpedDriftWave(
            double worldX,
            double worldZ,
            double animTimeSeconds,
            float driftSpeed,
            int stratumIndex) {
        double safeSpeed = Float.isFinite(driftSpeed) ? Math.max(0.0D, driftSpeed) : 0.0D;
        double phaseTime = animTimeSeconds * (0.35D + safeSpeed * 1.15D);
        double layerPhase = stratumIndex * 1.93D;

        double warpX = worldX + 3.4D * Math.sin(wrapAngleRadians(worldZ * 0.075D + phaseTime * 0.42D + layerPhase));
        double warpZ = worldZ + 3.4D * Math.cos(wrapAngleRadians(worldX * 0.071D - phaseTime * 0.37D + layerPhase * 0.7D));

        float w1 = Mth.sin(wrapAngleRadians(warpX * 0.115D + warpZ * 0.085D - phaseTime * 0.85D + layerPhase));
        float w2 = Mth.cos(wrapAngleRadians(warpX * 0.062D - warpZ * 0.128D + phaseTime * 0.53D - layerPhase * 0.5D));
        float w3 = Mth.sin(wrapAngleRadians((warpX - warpZ) * 0.185D + phaseTime * 1.12D + stratumIndex * 1.1D));
        return Mth.clamp(w1 * 0.50F + w2 * 0.32F + w3 * 0.18F, -1.0F, 1.0F);
    }

    // =========================================================================
    // Public World Render Entrypoint (AFTER_TRANSLUCENT)
    // =========================================================================

    /**
     * Renders low-lying volumetric mist strata in camera-centered world orientation.
     * Called during {@code AFTER_TRANSLUCENT} where {@code (0, 0, 0)} in {@code poseStack} is {@code camera.getPosition()}.
     */
    public static void renderVolumetricFog(PoseStack poseStack, Matrix4f projOrModelView, Camera camera, float deltaTicks) {
        if (poseStack == null || camera == null) {
            return;
        }
        if (!EntropicaConfig.ENABLE_BIOME_CUSTOM_FOG.get() || !EntropicaConfig.ENABLE_BIOME_VOLUMETRIC_FOG.get()) {
            invalidateCache();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }

        float shaderScale = ShaderModCompatBridge.getVolumetricMistShaderScale();
        if (shaderScale <= 1.0E-4F) {
            return;
        }

        ClientLevel level = mc.level;
        Vec3 camPos = camera.getPosition();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;

        long gameTime = level.getGameTime();
        if (needsCacheRefresh(level, gameTime, camX, camY, camZ)) {
            rebuildMistGrid(level, gameTime, camX, camY, camZ);
        }

        if (!hasAnyTranslucentCells && !hasAnyEmissiveCells) {
            return;
        }

        float partialTick = mc.getDeltaTracker() != null
                ? Mth.clamp(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), 0.0F, 1.0F)
                : Mth.clamp(deltaTicks, 0.0F, 1.0F);

        float nightFactor = computeNightFactor(level, partialTick);
        double animTimeSeconds = (gameTime + (double) partialTick) * 0.05D;

        // Pre-evaluate per-corner day/night color and camera distance/altitude envelope once per frame
        evaluateFrameCorners(camX, camY, camZ, nightFactor, shaderScale);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();
        Matrix4f mat = poseStack.last().pose();

        // Pass 1: Non-emissive biome mist (lit naturally by sky + block lightmap, e.g. Auroral Skerries steam mist)
        if (hasAnyTranslucentCells) {
            RenderType translucentType = RenderType.entityTranslucent(WHITE_TEXTURE);
            VertexConsumer vc = bufferSource.getBuffer(translucentType);
            boolean emitted = renderMistPass(mat, vc, camX, camY, camZ, animTimeSeconds, nightFactor, false, overlay);
            if (emitted) {
                bufferSource.endBatch(translucentType);
            }
        }

        // Pass 2: Emissive Materia/celestial biome mist (e.g. Astral Skerries glowing purple stardust mist)
        // Sequentially batched after Pass 1 so MultiBufferSource's shared ByteBufferBuilder never collides.
        if (hasAnyEmissiveCells) {
            RenderType emissiveType = RenderType.entityTranslucentEmissive(WHITE_TEXTURE);
            VertexConsumer vc = bufferSource.getBuffer(emissiveType);
            boolean emitted = renderMistPass(mat, vc, camX, camY, camZ, animTimeSeconds, nightFactor, true, overlay);
            if (emitted) {
                bufferSource.endBatch(emissiveType);
            }
        }

        poseStack.popPose();
    }

    // =========================================================================
    // Per-Frame Corner Evaluation & Back-to-Front Stratum Mesh Emission
    // =========================================================================

    private static void evaluateFrameCorners(
            double camX,
            double camY,
            double camZ,
            float nightFactor,
            float shaderScale) {
        for (int vz = 0; vz < CORNER_AXIS; vz++) {
            double wz = gridOriginZ + vz * CELL_SIZE;
            double dz = wz - camZ;
            int rowOffset = vz * CORNER_AXIS;

            for (int vx = 0; vx < CORNER_AXIS; vx++) {
                int cIdx = rowOffset + vx;
                float density = CORNER_DENSITY[cIdx];
                if (density <= 1.0E-4F) {
                    FRAME_CORNER_ENV[cIdx] = 0.0F;
                    continue;
                }

                double wx = gridOriginX + vx * CELL_SIZE;
                double dx = wx - camX;
                float horizDist = (float) Math.sqrt(dx * dx + dz * dz);
                float distEnv = computeMistDistanceEnvelope(horizDist, MAX_MIST_RADIUS);
                if (distEnv <= 1.0E-4F) {
                    FRAME_CORNER_ENV[cIdx] = 0.0F;
                    continue;
                }

                float midY = CORNER_SURFACE_Y[cIdx] + CORNER_BASE_OFFSET[cIdx] + CORNER_THICKNESS[cIdx] * 0.5F;
                float vertDist = Math.abs(midY - (float) camY);
                float vertFade = 1.0F - BiomeTransitionMath.smoothstep(22.0F, 52.0F, vertDist);
                if (vertFade <= 1.0E-4F) {
                    FRAME_CORNER_ENV[cIdx] = 0.0F;
                    continue;
                }

                FRAME_CORNER_ENV[cIdx] = density * distEnv * vertFade * shaderScale;
                FRAME_CORNER_R[cIdx] = Mth.lerp(nightFactor, CORNER_DAY_R[cIdx], CORNER_NIGHT_R[cIdx]);
                FRAME_CORNER_G[cIdx] = Mth.lerp(nightFactor, CORNER_DAY_G[cIdx], CORNER_NIGHT_G[cIdx]);
                FRAME_CORNER_B[cIdx] = Mth.lerp(nightFactor, CORNER_DAY_B[cIdx], CORNER_NIGHT_B[cIdx]);
            }
        }
    }

    private static boolean renderMistPass(
            Matrix4f mat,
            VertexConsumer vc,
            double camX,
            double camY,
            double camZ,
            double animTimeSeconds,
            float nightFactor,
            boolean emissivePass,
            int overlay) {
        boolean[] activeFlags = emissivePass ? CELL_ACTIVE_EMISSIVE : CELL_ACTIVE_TRANSLUCENT;
        float baseSheetOpacity = emissivePass ? EMISSIVE_SHEET_OPACITY : TRANSLUCENT_SHEET_OPACITY;
        boolean anyVertexEmitted = false;

        for (int orderIdx = 0; orderIdx < CELL_COUNT; orderIdx++) {
            int cellIdx = BACK_TO_FRONT_CELLS[orderIdx];
            if (!activeFlags[cellIdx]) {
                continue;
            }

            int cz = cellIdx / GRID_CELLS;
            int cx = cellIdx - cz * GRID_CELLS;

            int c00 = cz * CORNER_AXIS + cx;
            int c10 = c00 + 1;
            int c01 = c00 + CORNER_AXIS;
            int c11 = c01 + 1;

            float e00 = passCornerEnvelope(c00, cellIdx, emissivePass);
            float e10 = passCornerEnvelope(c10, cellIdx, emissivePass);
            float e11 = passCornerEnvelope(c11, cellIdx, emissivePass);
            float e01 = passCornerEnvelope(c01, cellIdx, emissivePass);

            if (e00 <= 1.0E-4F && e10 <= 1.0E-4F && e11 <= 1.0E-4F && e01 <= 1.0E-4F) {
                continue;
            }

            double wx0 = gridOriginX + cx * CELL_SIZE;
            double wx1 = wx0 + CELL_SIZE;
            double wz0 = gridOriginZ + cz * CELL_SIZE;
            double wz1 = wz0 + CELL_SIZE;

            float rx0 = (float) (wx0 - camX);
            float rx1 = (float) (wx1 - camX);
            float rz0 = (float) (wz0 - camZ);
            float rz1 = (float) (wz1 - camZ);

            // Resolve per-corner geometry & color: active corners use shared arrays (zero seams!),
            // while zero-alpha cliff/border corners anchor to the cell's active interior (zero cliff-wall slanting!).
            float baseY00 = resolveCornerBaseY(c00, cellIdx);
            float baseY10 = resolveCornerBaseY(c10, cellIdx);
            float baseY11 = resolveCornerBaseY(c11, cellIdx);
            float baseY01 = resolveCornerBaseY(c01, cellIdx);

            float thick00 = resolveCornerThickness(c00, cellIdx);
            float thick10 = resolveCornerThickness(c10, cellIdx);
            float thick11 = resolveCornerThickness(c11, cellIdx);
            float thick01 = resolveCornerThickness(c01, cellIdx);

            float drift00 = resolveCornerDrift(c00, cellIdx);
            float drift10 = resolveCornerDrift(c10, cellIdx);
            float drift11 = resolveCornerDrift(c11, cellIdx);
            float drift01 = resolveCornerDrift(c01, cellIdx);

            float r00 = resolveCornerR(c00, cellIdx, nightFactor, FRAME_CORNER_R[c00]);
            float g00 = resolveCornerG(c00, cellIdx, nightFactor, FRAME_CORNER_G[c00]);
            float b00 = resolveCornerB(c00, cellIdx, nightFactor, FRAME_CORNER_B[c00]);

            float r10 = resolveCornerR(c10, cellIdx, nightFactor, FRAME_CORNER_R[c10]);
            float g10 = resolveCornerG(c10, cellIdx, nightFactor, FRAME_CORNER_G[c10]);
            float b10 = resolveCornerB(c10, cellIdx, nightFactor, FRAME_CORNER_B[c10]);

            float r11 = resolveCornerR(c11, cellIdx, nightFactor, FRAME_CORNER_R[c11]);
            float g11 = resolveCornerG(c11, cellIdx, nightFactor, FRAME_CORNER_G[c11]);
            float b11 = resolveCornerB(c11, cellIdx, nightFactor, FRAME_CORNER_B[c11]);

            float r01 = resolveCornerR(c01, cellIdx, nightFactor, FRAME_CORNER_R[c01]);
            float g01 = resolveCornerG(c01, cellIdx, nightFactor, FRAME_CORNER_G[c01]);
            float b01 = resolveCornerB(c01, cellIdx, nightFactor, FRAME_CORNER_B[c01]);

            int light00 = emissivePass ? FULL_LIGHT : resolveCornerLight(c00, cellIdx);
            int light10 = emissivePass ? FULL_LIGHT : resolveCornerLight(c10, cellIdx);
            int light11 = emissivePass ? FULL_LIGHT : resolveCornerLight(c11, cellIdx);
            int light01 = emissivePass ? FULL_LIGHT : resolveCornerLight(c01, cellIdx);

            // Order the 3 vertical strata Back-to-Front relative to camera eye height
            float cellMidY = 0.25F * (baseY00 + baseY10 + baseY11 + baseY01)
                    + 0.125F * (thick00 + thick10 + thick11 + thick01);
            boolean cameraAboveMid = camY >= cellMidY;

            for (int step = 0; step < STRATA_COUNT; step++) {
                int sIdx = cameraAboveMid ? step : (STRATA_COUNT - 1 - step);
                float yFrac = STRATUM_Y_FRACTIONS[sIdx];

                float w00 = sampleDomainWarpedDriftWave(wx0, wz0, animTimeSeconds, drift00, sIdx);
                float w10 = sampleDomainWarpedDriftWave(wx1, wz0, animTimeSeconds, drift10, sIdx);
                float w11 = sampleDomainWarpedDriftWave(wx1, wz1, animTimeSeconds, drift11, sIdx);
                float w01 = sampleDomainWarpedDriftWave(wx0, wz1, animTimeSeconds, drift01, sIdx);

                float a00 = e00 * computeVerticalStratumAlpha(yFrac, w00) * baseSheetOpacity;
                float a10 = e10 * computeVerticalStratumAlpha(yFrac, w10) * baseSheetOpacity;
                float a11 = e11 * computeVerticalStratumAlpha(yFrac, w11) * baseSheetOpacity;
                float a01 = e01 * computeVerticalStratumAlpha(yFrac, w01) * baseSheetOpacity;

                if (a00 <= 1.0E-4F && a10 <= 1.0E-4F && a11 <= 1.0E-4F && a01 <= 1.0E-4F) {
                    continue;
                }

                // Undulate vertically while keeping yFrac=0.18 strictly above surfaceY (0.18 - 0.14 = +0.04 > 0)
                float ry00 = (float) ((baseY00 + thick00 * yFrac + w00 * Math.min(0.42F, thick00 * 0.14F)) - camY);
                float ry10 = (float) ((baseY10 + thick10 * yFrac + w10 * Math.min(0.42F, thick10 * 0.14F)) - camY);
                float ry11 = (float) ((baseY11 + thick11 * yFrac + w11 * Math.min(0.42F, thick11 * 0.14F)) - camY);
                float ry01 = (float) ((baseY01 + thick01 * yFrac + w01 * Math.min(0.42F, thick01 * 0.14F)) - camY);

                // Subtle crest highlight on billowing mist tops
                float cBoost00 = 1.0F + 0.06F * w00 + 0.04F * yFrac;
                float cBoost10 = 1.0F + 0.06F * w10 + 0.04F * yFrac;
                float cBoost11 = 1.0F + 0.06F * w11 + 0.04F * yFrac;
                float cBoost01 = 1.0F + 0.06F * w01 + 0.04F * yFrac;

                emitDoubleSidedHorizontalSheet(
                        mat, vc, overlay,
                        rx0, ry00, rz0, r00 * cBoost00, g00 * cBoost00, b00 * cBoost00, a00, light00,
                        rx0, ry01, rz1, r01 * cBoost01, g01 * cBoost01, b01 * cBoost01, a01, light01,
                        rx1, ry11, rz1, r11 * cBoost11, g11 * cBoost11, b11 * cBoost11, a11, light11,
                        rx1, ry10, rz0, r10 * cBoost10, g10 * cBoost10, b10 * cBoost10, a10, light10
                );
                anyVertexEmitted = true;
            }
        }

        return anyVertexEmitted;
    }

    private static float passCornerEnvelope(int cIdx, int cellIdx, boolean emissivePass) {
        float baseEnv = FRAME_CORNER_ENV[cIdx];
        if (baseEnv <= 1.0E-4F) {
            return 0.0F;
        }
        float emFrac = CORNER_DENSITY[cIdx] > 1.0E-4F ? CORNER_EMISSIVE_FRAC[cIdx] : CELL_FALLBACK_EMISSIVE[cellIdx];
        return emissivePass ? (baseEnv * emFrac) : (baseEnv * (1.0F - emFrac));
    }

    private static float resolveCornerBaseY(int cIdx, int cellIdx) {
        return CORNER_DENSITY[cIdx] > 1.0E-4F
                ? (CORNER_SURFACE_Y[cIdx] + CORNER_BASE_OFFSET[cIdx])
                : CELL_FALLBACK_BASE_Y[cellIdx];
    }

    private static float resolveCornerThickness(int cIdx, int cellIdx) {
        return CORNER_DENSITY[cIdx] > 1.0E-4F ? CORNER_THICKNESS[cIdx] : CELL_FALLBACK_THICKNESS[cellIdx];
    }

    private static float resolveCornerDrift(int cIdx, int cellIdx) {
        return CORNER_DENSITY[cIdx] > 1.0E-4F ? CORNER_DRIFT_SPEED[cIdx] : CELL_FALLBACK_DRIFT[cellIdx];
    }

    private static float resolveCornerR(int cIdx, int cellIdx, float nightFactor, float activeFrameR) {
        if (CORNER_DENSITY[cIdx] > 1.0E-4F) {
            return activeFrameR;
        }
        return Mth.lerp(nightFactor, CELL_FALLBACK_DAY_R[cellIdx], CELL_FALLBACK_NIGHT_R[cellIdx]);
    }

    private static float resolveCornerG(int cIdx, int cellIdx, float nightFactor, float activeFrameG) {
        if (CORNER_DENSITY[cIdx] > 1.0E-4F) {
            return activeFrameG;
        }
        return Mth.lerp(nightFactor, CELL_FALLBACK_DAY_G[cellIdx], CELL_FALLBACK_NIGHT_G[cellIdx]);
    }

    private static float resolveCornerB(int cIdx, int cellIdx, float nightFactor, float activeFrameB) {
        if (CORNER_DENSITY[cIdx] > 1.0E-4F) {
            return activeFrameB;
        }
        return Mth.lerp(nightFactor, CELL_FALLBACK_DAY_B[cellIdx], CELL_FALLBACK_NIGHT_B[cellIdx]);
    }

    private static int resolveCornerLight(int cIdx, int cellIdx) {
        return CORNER_DENSITY[cIdx] > 1.0E-4F ? CORNER_PACKED_LIGHT[cIdx] : CELL_FALLBACK_LIGHT[cellIdx];
    }

    /**
     * Emits both upward-visible and downward-visible faces using identical triangle diagonals
     * ({@code v0-v2}) and upward sky-scattering normals so crossing a mist plane never causes
     * diffuse-lighting jumps or diagonal z-fighting in vanilla, Sodium, or Iris G-buffer shaders.
     */
    private static void emitDoubleSidedHorizontalSheet(
            Matrix4f mat,
            VertexConsumer vc,
            int overlay,
            float x0, float y0, float z0, float r0, float g0, float b0, float a0, int l0,
            float x1, float y1, float z1, float r1, float g1, float b1, float a1, int l1,
            float x2, float y2, float z2, float r2, float g2, float b2, float a2, int l2,
            float x3, float y3, float z3, float r3, float g3, float b3, float a3, int l3) {
        // Top face (CCW looking down from +Y: v0 -> v1 -> v2 -> v3, diagonal v0-v2)
        vertex(mat, vc, overlay, x0, y0, z0, r0, g0, b0, a0, 0.0F, 0.0F, l0);
        vertex(mat, vc, overlay, x1, y1, z1, r1, g1, b1, a1, 0.0F, 1.0F, l1);
        vertex(mat, vc, overlay, x2, y2, z2, r2, g2, b2, a2, 1.0F, 1.0F, l2);
        vertex(mat, vc, overlay, x3, y3, z3, r3, g3, b3, a3, 1.0F, 0.0F, l3);

        // Bottom face (opposite winding sharing exact v0-v2 diagonal: v0 -> v3 -> v2 -> v1)
        vertex(mat, vc, overlay, x0, y0, z0, r0, g0, b0, a0, 0.0F, 0.0F, l0);
        vertex(mat, vc, overlay, x3, y3, z3, r3, g3, b3, a3, 1.0F, 0.0F, l3);
        vertex(mat, vc, overlay, x2, y2, z2, r2, g2, b2, a2, 1.0F, 1.0F, l2);
        vertex(mat, vc, overlay, x1, y1, z1, r1, g1, b1, a1, 0.0F, 1.0F, l1);
    }

    private static void vertex(
            Matrix4f mat,
            VertexConsumer vc,
            int overlay,
            float x, float y, float z,
            float r, float g, float b, float a,
            float u, float v,
            int packedLight) {
        vc.addVertex(mat, x, y, z)
                .setColor(
                        Mth.clamp(r, 0.0F, 1.0F),
                        Mth.clamp(g, 0.0F, 1.0F),
                        Mth.clamp(b, 0.0F, 1.0F),
                        Mth.clamp(a, 0.0F, 1.0F))
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(packedLight)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    // =========================================================================
    // Spatial Grid Cache Rebuild (Every 4 Ticks or >= 2 Blocks Camera Move)
    // =========================================================================

    private static boolean needsCacheRefresh(
            ClientLevel level,
            long gameTime,
            double camX,
            double camY,
            double camZ) {
        ClientLevel prevLevel = cachedLevelRef != null ? cachedLevelRef.get() : null;
        if (prevLevel != level || !Double.isFinite(lastCacheCamX)) {
            return true;
        }
        if (Math.abs(gameTime - lastCacheGameTime) >= CACHE_REFRESH_TICKS) {
            return true;
        }
        double dx = camX - lastCacheCamX;
        double dy = camY - lastCacheCamY;
        double dz = camZ - lastCacheCamZ;
        return (dx * dx + dy * dy + dz * dz) >= CACHE_MOVE_THRESHOLD_SQ;
    }

    private static void invalidateCache() {
        hasAnyTranslucentCells = false;
        hasAnyEmissiveCells = false;
        lastCacheGameTime = Long.MIN_VALUE;
        lastCacheCamX = Double.NaN;
    }

    private static void rebuildMistGrid(
            ClientLevel level,
            long gameTime,
            double camX,
            double camY,
            double camZ) {
        cachedLevelRef = new WeakReference<>(level);
        lastCacheGameTime = gameTime;
        lastCacheCamX = camX;
        lastCacheCamY = camY;
        lastCacheCamZ = camZ;

        int centerCellX = Mth.floor(camX / (double) CELL_SIZE);
        int centerCellZ = Mth.floor(camZ / (double) CELL_SIZE);
        gridOriginX = (centerCellX - HALF_CELLS) * CELL_SIZE;
        gridOriginZ = (centerCellZ - HALF_CELLS) * CELL_SIZE;

        // 1. Sample 28x28 raw terrain + fluid + skyLight + TerraBlender-safe biome mist profiles
        boolean anyRawMist = false;
        for (int sz = 0; sz < RAW_AXIS; sz++) {
            int bz = gridOriginZ + (sz - 1) * CELL_SIZE;
            int rowOffset = sz * RAW_AXIS;
            for (int sx = 0; sx < RAW_AXIS; sx++) {
                int bx = gridOriginX + (sx - 1) * CELL_SIZE;
                if (sampleRawColumn(level, bx, bz, rowOffset + sx)) {
                    anyRawMist = true;
                }
            }
        }

        if (!anyRawMist) {
            hasAnyTranslucentCells = false;
            hasAnyEmissiveCells = false;
            return;
        }

        // 2. Filter 26x26 shared corner vertices across each corner's 3x3 binomial neighborhood
        for (int vz = 0; vz < CORNER_AXIS; vz++) {
            int rowOffset = vz * CORNER_AXIS;
            for (int vx = 0; vx < CORNER_AXIS; vx++) {
                synthesizeSharedCorner(vx, vz, rowOffset + vx);
            }
        }

        // 3. Flag active 25x25 cells and populate fallback anchors for zero-alpha cliff/border corners
        boolean foundTranslucent = false;
        boolean foundEmissive = false;

        for (int cz = 0; cz < GRID_CELLS; cz++) {
            int cellRow = cz * GRID_CELLS;
            for (int cx = 0; cx < GRID_CELLS; cx++) {
                int cellIdx = cellRow + cx;
                int c00 = cz * CORNER_AXIS + cx;
                int c10 = c00 + 1;
                int c01 = c00 + CORNER_AXIS;
                int c11 = c01 + 1;

                float weightSum = 0.0F;
                float baseYSum = 0.0F;
                float thickSum = 0.0F;
                float dayRSum = 0.0F, dayGSum = 0.0F, dayBSum = 0.0F;
                float nightRSum = 0.0F, nightGSum = 0.0F, nightBSum = 0.0F;
                float driftSum = 0.0F;
                float emissiveSum = 0.0F;
                float maxEmissive = 0.0F;
                float maxNonEmissive = 0.0F;
                int bestLight = FULL_LIGHT;
                float bestLightWeight = -1.0F;

                for (int k = 0; k < 4; k++) {
                    int c = (k == 0) ? c00 : (k == 1) ? c10 : (k == 2) ? c11 : c01;
                    float w = CORNER_DENSITY[c];
                    if (w > 1.0E-4F) {
                        weightSum += w;
                        baseYSum += w * (CORNER_SURFACE_Y[c] + CORNER_BASE_OFFSET[c]);
                        thickSum += w * CORNER_THICKNESS[c];
                        dayRSum += w * CORNER_DAY_R[c];
                        dayGSum += w * CORNER_DAY_G[c];
                        dayBSum += w * CORNER_DAY_B[c];
                        nightRSum += w * CORNER_NIGHT_R[c];
                        nightGSum += w * CORNER_NIGHT_G[c];
                        nightBSum += w * CORNER_NIGHT_B[c];
                        driftSum += w * CORNER_DRIFT_SPEED[c];
                        float em = CORNER_EMISSIVE_FRAC[c];
                        emissiveSum += w * em;
                        maxEmissive = Math.max(maxEmissive, w * em);
                        maxNonEmissive = Math.max(maxNonEmissive, w * (1.0F - em));
                        if (w > bestLightWeight) {
                            bestLightWeight = w;
                            bestLight = CORNER_PACKED_LIGHT[c];
                        }
                    }
                }

                if (weightSum <= 0.002F) {
                    CELL_ACTIVE_TRANSLUCENT[cellIdx] = false;
                    CELL_ACTIVE_EMISSIVE[cellIdx] = false;
                    continue;
                }

                float invW = 1.0F / weightSum;
                CELL_FALLBACK_BASE_Y[cellIdx] = baseYSum * invW;
                CELL_FALLBACK_THICKNESS[cellIdx] = thickSum * invW;
                CELL_FALLBACK_DAY_R[cellIdx] = dayRSum * invW;
                CELL_FALLBACK_DAY_G[cellIdx] = dayGSum * invW;
                CELL_FALLBACK_DAY_B[cellIdx] = dayBSum * invW;
                CELL_FALLBACK_NIGHT_R[cellIdx] = nightRSum * invW;
                CELL_FALLBACK_NIGHT_G[cellIdx] = nightGSum * invW;
                CELL_FALLBACK_NIGHT_B[cellIdx] = nightBSum * invW;
                CELL_FALLBACK_DRIFT[cellIdx] = driftSum * invW;
                CELL_FALLBACK_EMISSIVE[cellIdx] = emissiveSum * invW;
                CELL_FALLBACK_LIGHT[cellIdx] = bestLight;

                boolean cellTrans = maxNonEmissive > 0.002F;
                boolean cellEmiss = maxEmissive > 0.002F;
                CELL_ACTIVE_TRANSLUCENT[cellIdx] = cellTrans;
                CELL_ACTIVE_EMISSIVE[cellIdx] = cellEmiss;
                if (cellTrans) {
                    foundTranslucent = true;
                }
                if (cellEmiss) {
                    foundEmissive = true;
                }
            }
        }

        hasAnyTranslucentCells = foundTranslucent;
        hasAnyEmissiveCells = foundEmissive;
    }

    private static boolean sampleRawColumn(ClientLevel level, int bx, int bz, int rawIdx) {
        if (!level.hasChunk(bx >> 4, bz >> 4)) {
            RAW_VALID[rawIdx] = false;
            RAW_HAS_PROFILE[rawIdx] = false;
            RAW_DENSITY[rawIdx] = 0.0F;
            return false;
        }

        int worldSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz);
        int minY = level.getMinY();
        if (worldSurface <= minY) {
            RAW_VALID[rawIdx] = false;
            RAW_HAS_PROFILE[rawIdx] = false;
            RAW_DENSITY[rawIdx] = 0.0F;
            return false;
        }

        int topY = worldSurface - 1;
        int scanFloor = Math.max(minY, topY - 12);
        while (topY > scanFloor) {
            MUTABLE_SAMPLE_POS.set(bx, topY, bz);
            BlockState st = level.getBlockState(MUTABLE_SAMPLE_POS);
            if (!st.getFluidState().isEmpty()) {
                break;
            }
            if (st.is(BlockTags.LEAVES) || (!st.blocksMotion() && !st.is(Blocks.SNOW))) {
                topY--;
            } else {
                break;
            }
        }

        MUTABLE_SAMPLE_POS.set(bx, topY, bz);
        BlockState surfaceState = level.getBlockState(MUTABLE_SAMPLE_POS);
        FluidState fluidState = surfaceState.getFluidState();
        float surfaceY;
        if (!fluidState.isEmpty()) {
            float fluidHeight = fluidState.getHeight(level, MUTABLE_SAMPLE_POS);
            surfaceY = topY + (fluidHeight > 0.0F ? fluidHeight : 0.8888889F);
        } else {
            VoxelShape collision = surfaceState.getCollisionShape(level, MUTABLE_SAMPLE_POS);
            double maxShapeY = collision.isEmpty() ? 1.0D : collision.max(Direction.Axis.Y);
            surfaceY = topY + (float) Mth.clamp(maxShapeY, 0.125D, 1.0D);
        }

        MUTABLE_SAMPLE_POS.set(bx, topY + 1, bz);
        BlockState aboveState = level.getBlockState(MUTABLE_SAMPLE_POS);
        if (aboveState.isSolidRender()) {
            RAW_VALID[rawIdx] = false;
            RAW_HAS_PROFILE[rawIdx] = false;
            RAW_DENSITY[rawIdx] = 0.0F;
            return false;
        }

        int skyLight = level.getBrightness(LightLayer.SKY, MUTABLE_SAMPLE_POS);
        int blockLight = level.getBrightness(LightLayer.BLOCK, MUTABLE_SAMPLE_POS);

        RAW_VALID[rawIdx] = true;
        RAW_SURFACE_Y[rawIdx] = surfaceY;
        RAW_SKY_LIGHT[rawIdx] = skyLight;
        RAW_BLOCK_LIGHT[rawIdx] = blockLight;

        // Never render underground or indoors (skyLight < 4)
        if (skyLight < MIN_SKY_LIGHT) {
            RAW_HAS_PROFILE[rawIdx] = false;
            RAW_DENSITY[rawIdx] = 0.0F;
            return false;
        }

        BiomeAtmosphereProfile profile = EntropicaBiomeAtmospheres.getForHolder(level.getBiome(MUTABLE_SAMPLE_POS));
        if (profile == null || !profile.hasVolumetricMist()) {
            RAW_HAS_PROFILE[rawIdx] = false;
            RAW_DENSITY[rawIdx] = 0.0F;
            return false;
        }

        float rawDensity = Mth.clamp(profile.getVolumetricMistDensity(), 0.0F, 1.0F);
        if (rawDensity <= 1.0E-4F) {
            RAW_HAS_PROFILE[rawIdx] = false;
            RAW_DENSITY[rawIdx] = 0.0F;
            return false;
        }

        float skyExposure = BiomeTransitionMath.smoothstep(3.5F, 10.5F, (float) skyLight);
        float channelBonus = !fluidState.isEmpty() ? 1.06F : 1.0F;

        int dayRgb = profile.getVolumetricMistDayRgb();
        int nightRgb = profile.getVolumetricMistNightRgb();
        if (dayRgb < 0 && nightRgb < 0) {
            dayRgb = 0xDCE8F5;
            nightRgb = 0x263B5E;
        } else if (dayRgb < 0) {
            dayRgb = nightRgb;
        } else if (nightRgb < 0) {
            nightRgb = dayRgb;
        }

        RAW_HAS_PROFILE[rawIdx] = true;
        RAW_EMISSIVE[rawIdx] = profile.isVolumetricMistEmissive();
        RAW_DENSITY[rawIdx] = Math.min(1.0F, rawDensity * skyExposure * channelBonus);
        RAW_DAY_R[rawIdx] = ((dayRgb >> 16) & 0xFF) / 255.0F;
        RAW_DAY_G[rawIdx] = ((dayRgb >> 8) & 0xFF) / 255.0F;
        RAW_DAY_B[rawIdx] = (dayRgb & 0xFF) / 255.0F;
        RAW_NIGHT_R[rawIdx] = ((nightRgb >> 16) & 0xFF) / 255.0F;
        RAW_NIGHT_G[rawIdx] = ((nightRgb >> 8) & 0xFF) / 255.0F;
        RAW_NIGHT_B[rawIdx] = (nightRgb & 0xFF) / 255.0F;
        RAW_BASE_OFFSET[rawIdx] = profile.getVolumetricMistBaseOffset();
        RAW_THICKNESS[rawIdx] = Math.max(0.40F, profile.getVolumetricMistThickness());
        RAW_DRIFT_SPEED[rawIdx] = Math.max(0.0F, profile.getVolumetricMistDriftSpeed());
        return true;
    }

    private static void synthesizeSharedCorner(int vx, int vz, int cornerIdx) {
        float validWeightSum = 0.0F;
        float surfaceYSum = 0.0F;
        float minSurfaceY = Float.POSITIVE_INFINITY;
        float maxSurfaceY = Float.NEGATIVE_INFINITY;
        float skyLightSum = 0.0F;
        float blockLightSum = 0.0F;

        float densityKernelSum = 0.0F;
        float mistWeightSum = 0.0F;
        float dayRSum = 0.0F, dayGSum = 0.0F, dayBSum = 0.0F;
        float nightRSum = 0.0F, nightGSum = 0.0F, nightBSum = 0.0F;
        float baseOffsetSum = 0.0F;
        float thicknessSum = 0.0F;
        float driftSpeedSum = 0.0F;
        float emissiveWeightSum = 0.0F;

        int kIdx = 0;
        for (int dz = -1; dz <= 1; dz++) {
            int sz = vz + 1 + dz;
            int rawRow = sz * RAW_AXIS;
            for (int dx = -1; dx <= 1; dx++) {
                float kw = KERNEL_3X3[kIdx++];
                int rIdx = rawRow + (vx + 1 + dx);
                if (!RAW_VALID[rIdx]) {
                    continue;
                }

                validWeightSum += kw;
                float sy = RAW_SURFACE_Y[rIdx];
                surfaceYSum += kw * sy;
                if (sy < minSurfaceY) minSurfaceY = sy;
                if (sy > maxSurfaceY) maxSurfaceY = sy;
                skyLightSum += kw * RAW_SKY_LIGHT[rIdx];
                blockLightSum += kw * RAW_BLOCK_LIGHT[rIdx];

                float dens = RAW_DENSITY[rIdx];
                if (RAW_HAS_PROFILE[rIdx] && dens > 1.0E-4F) {
                    float mw = kw * dens;
                    densityKernelSum += mw;
                    mistWeightSum += mw;
                    dayRSum += mw * RAW_DAY_R[rIdx];
                    dayGSum += mw * RAW_DAY_G[rIdx];
                    dayBSum += mw * RAW_DAY_B[rIdx];
                    nightRSum += mw * RAW_NIGHT_R[rIdx];
                    nightGSum += mw * RAW_NIGHT_G[rIdx];
                    nightBSum += mw * RAW_NIGHT_B[rIdx];
                    baseOffsetSum += mw * RAW_BASE_OFFSET[rIdx];
                    thicknessSum += mw * RAW_THICKNESS[rIdx];
                    driftSpeedSum += mw * RAW_DRIFT_SPEED[rIdx];
                    if (RAW_EMISSIVE[rIdx]) {
                        emissiveWeightSum += mw;
                    }
                }
            }
        }

        if (validWeightSum <= 0.0F || mistWeightSum <= 1.0E-5F) {
            CORNER_HAS_PROFILE[cornerIdx] = false;
            CORNER_DENSITY[cornerIdx] = 0.0F;
            return;
        }

        // Smoothly fade mist to 0.0F against steep cliffs/walls so sheets hug channels & shelves without climbing walls
        float heightSpan = maxSurfaceY - minSurfaceY;
        float cliffFade = 1.0F - BiomeTransitionMath.smoothstep(2.5F, 5.5F, heightSpan);
        float avgDensity = (densityKernelSum / 16.0F) * cliffFade;

        float invValid = 1.0F / validWeightSum;
        float invMist = 1.0F / mistWeightSum;
        int avgSky = Mth.clamp(Math.round(skyLightSum * invValid), 0, 15);
        int avgBlock = Mth.clamp(Math.round(blockLightSum * invValid), 0, 15);

        CORNER_HAS_PROFILE[cornerIdx] = true;
        CORNER_SURFACE_Y[cornerIdx] = surfaceYSum * invValid;
        CORNER_DENSITY[cornerIdx] = avgDensity;
        CORNER_DAY_R[cornerIdx] = dayRSum * invMist;
        CORNER_DAY_G[cornerIdx] = dayGSum * invMist;
        CORNER_DAY_B[cornerIdx] = dayBSum * invMist;
        CORNER_NIGHT_R[cornerIdx] = nightRSum * invMist;
        CORNER_NIGHT_G[cornerIdx] = nightGSum * invMist;
        CORNER_NIGHT_B[cornerIdx] = nightBSum * invMist;
        CORNER_BASE_OFFSET[cornerIdx] = baseOffsetSum * invMist;
        CORNER_THICKNESS[cornerIdx] = thicknessSum * invMist;
        CORNER_DRIFT_SPEED[cornerIdx] = driftSpeedSum * invMist;
        CORNER_EMISSIVE_FRAC[cornerIdx] = Mth.clamp(emissiveWeightSum * invMist, 0.0F, 1.0F);
        CORNER_PACKED_LIGHT[cornerIdx] = (avgBlock << 4) | (avgSky << 20);
    }

    // =========================================================================
    // Internal Math & Precomputed Traversal Helpers
    // =========================================================================

    private static float computeNightFactor(ClientLevel level, float partialTick) {
        float timeOfDay = level.getTimeOfDay(partialTick);
        float dayBrightness = Mth.clamp(Mth.cos(timeOfDay * ((float) Math.PI * 2.0F)) * 2.0F + 0.5F, 0.0F, 1.0F);
        return 1.0F - dayBrightness;
    }

    private static float wrapAngleRadians(double radians) {
        double twoPi = Math.PI * 2.0D;
        double wrapped = radians % twoPi;
        if (wrapped < 0.0D) {
            wrapped += twoPi;
        }
        return (float) wrapped;
    }

    private static int[] buildBackToFrontCellOrder() {
        int[] indices = new int[CELL_COUNT];
        int[] distSq = new int[CELL_COUNT];
        for (int cz = 0; cz < GRID_CELLS; cz++) {
            int dz = cz - HALF_CELLS;
            for (int cx = 0; cx < GRID_CELLS; cx++) {
                int dx = cx - HALF_CELLS;
                int idx = cz * GRID_CELLS + cx;
                indices[idx] = idx;
                distSq[idx] = dx * dx + dz * dz;
            }
        }
        // Simple insertion sort once at class load (625 ints, zero boxed Objects)
        for (int i = 1; i < CELL_COUNT; i++) {
            int keyIdx = indices[i];
            int keyDist = distSq[i];
            int j = i - 1;
            while (j >= 0 && distSq[j] < keyDist) {
                indices[j + 1] = indices[j];
                distSq[j + 1] = distSq[j];
                j--;
            }
            indices[j + 1] = keyIdx;
            distSq[j + 1] = keyDist;
        }
        return indices;
    }
}
