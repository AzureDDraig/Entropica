package ddraig.net.entropica.client.worldgen;

import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.worldgen.biome.BiomeAtmosphereProfile;
import ddraig.net.entropica.worldgen.biome.BiomeTransitionMath;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomeAtmospheres;
import ddraig.net.entropica.worldgen.biome.SkyPhenomenonType;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Smooth client-side atmosphere state interpolator and wind-drifted ambient particle emitter.
 *
 * <p>Uses a continuous 2D 5x5 sub-block Gaussian spatial kernel ({@link BiomeTransitionMath}) paired
 * with frame-rate-independent exponential temporal smoothing and sub-tick interpolation so fog
 * distances, held-light fog relief, day/night fog &amp; sky colors, star brightness boosts, ambient
 * biome particles, and overhead sky phenomena transition seamlessly between vanilla/non-effect
 * biomes and custom Entropica biomes with zero staircase jumps or single-block pops, while remaining
 * 100% compatible with Sodium and Iris/Oculus shaderpacks.</p>
 */
public final class BiomeAtmosphereClientManager {
    // Exponential smoothing rates (per 20-TPS game tick)
    private static final float FOG_BLEND_SMOOTH_RATE = 0.12F;
    private static final float FOG_DIST_SMOOTH_RATE = 0.14F;
    private static final float LIGHT_RELIEF_SMOOTH_RATE = 0.16F;
    private static final float COLOR_SMOOTH_RATE = 0.14F;
    private static final float STAR_SMOOTH_RATE = 0.12F;
    private static final float PHENOMENON_IN_RATE = 0.10F;
    private static final float PHENOMENON_OUT_RATE = 0.11F;
    private static final float PHENOMENON_COLOR_RATE = 0.14F;

    // Frame-delta channels for render hooks without an explicit DeltaTracker parameter
    private static final int CHANNEL_FOG_COLOR = 0;
    private static final int CHANNEL_SKY_COLOR = 1;
    private static final int CHANNEL_STAR_BOOST = 2;
    private static final long[] LAST_CHANNEL_NANOS = new long[3];

    // Active client level tracker (resets stale atmosphere state across dimension/world changes)
    private static WeakReference<ClientLevel> activeLevelRef = null;

    // Reusable zero-allocation 5x5 spatial neighborhood cache on the client thread
    private static final int[] SAMPLE_BX = new int[BiomeTransitionMath.KERNEL_SAMPLE_COUNT];
    private static final int[] SAMPLE_BZ = new int[BiomeTransitionMath.KERNEL_SAMPLE_COUNT];
    private static final float[] SAMPLE_WEIGHTS = new float[BiomeTransitionMath.KERNEL_SAMPLE_COUNT];
    private static final BiomeAtmosphereProfile[] SAMPLE_PROFILES = new BiomeAtmosphereProfile[BiomeTransitionMath.KERNEL_SAMPLE_COUNT];
    private static final BlockPos.MutableBlockPos MUTABLE_SAMPLE_POS = new BlockPos.MutableBlockPos();
    private static long cachedSampleGameTime = Long.MIN_VALUE;
    private static int cachedSampleY = Integer.MIN_VALUE;
    private static double cachedSampleX = Double.NaN;
    private static double cachedSampleZ = Double.NaN;

    // Reusable zero-allocation SkyPhenomenonType aggregation buffers
    private static final SkyPhenomenonType[] PHENOMENON_VALUES = SkyPhenomenonType.values();
    private static final float[] TYPE_WEIGHT = new float[PHENOMENON_VALUES.length];
    private static final float[] TYPE_INTENSITY_SUM = new float[PHENOMENON_VALUES.length];
    private static final float[] TYPE_PRIM_R = new float[PHENOMENON_VALUES.length];
    private static final float[] TYPE_PRIM_G = new float[PHENOMENON_VALUES.length];
    private static final float[] TYPE_PRIM_B = new float[PHENOMENON_VALUES.length];
    private static final float[] TYPE_SEC_R = new float[PHENOMENON_VALUES.length];
    private static final float[] TYPE_SEC_G = new float[PHENOMENON_VALUES.length];
    private static final float[] TYPE_SEC_B = new float[PHENOMENON_VALUES.length];

    // Smoothed Fog Distance & Held-Light Relief State
    private static float smoothFogBlend = 0.0F;
    private static float smoothFogStart = 0.0F;
    private static float smoothFogEnd = 256.0F;
    private static float smoothLightRelief = 0.0F;

    // Smoothed Fog Color State
    private static float smoothFogColorWeight = 0.0F;
    private static float smoothFogColorR = 255.0F;
    private static float smoothFogColorG = 255.0F;
    private static float smoothFogColorB = 255.0F;

    // Smoothed Sky Color & Star Boost State
    private static float smoothDaySkyScale = 1.0F;
    private static float smoothNightSkyWeight = 0.0F;
    private static float smoothNightSkyR = 0.0F;
    private static float smoothNightSkyG = 0.0F;
    private static float smoothNightSkyB = 0.0F;
    private static float smoothStarBoost = 0.0F;

    // Smoothed Active Sky Phenomenon Slot (with sub-tick prev/curr alpha)
    private static SkyPhenomenonType activePhenomenon = SkyPhenomenonType.NONE;
    private static float prevPhenomenonAlpha = 0.0F;
    private static float smoothPhenomenonAlpha = 0.0F;
    private static float smoothPrimR = 1.0F, smoothPrimG = 1.0F, smoothPrimB = 1.0F;
    private static float smoothSecR = 1.0F, smoothSecG = 1.0F, smoothSecB = 1.0F;

    // Smoothed Fading-Out Sky Phenomenon Slot (for seamless cross-fades between two phenomenon types)
    private static SkyPhenomenonType fadingOutPhenomenon = SkyPhenomenonType.NONE;
    private static float prevFadeOutAlpha = 0.0F;
    private static float fadeOutPhenomenonAlpha = 0.0F;
    private static float fadeOutPrimR = 1.0F, fadeOutPrimG = 1.0F, fadeOutPrimB = 1.0F;
    private static float fadeOutSecR = 1.0F, fadeOutSecG = 1.0F, fadeOutSecB = 1.0F;

    private BiomeAtmosphereClientManager() {}

    // =========================================================================
    // Public Getters (Active & Fading-Out Sky Phenomenon State)
    // =========================================================================

    public static SkyPhenomenonType getActivePhenomenon() {
        return activePhenomenon;
    }

    public static float getSmoothPhenomenonAlpha() {
        return smoothPhenomenonAlpha;
    }

    public static float getRenderPhenomenonAlpha(float partialTick) {
        if (activePhenomenon == SkyPhenomenonType.NONE) {
            return 0.0F;
        }
        float pt = Mth.clamp(partialTick, 0.0F, 1.0F);
        return Mth.lerp(pt, prevPhenomenonAlpha, smoothPhenomenonAlpha);
    }

    public static float getPrimR() { return smoothPrimR; }
    public static float getPrimG() { return smoothPrimG; }
    public static float getPrimB() { return smoothPrimB; }
    public static float getSecR() { return smoothSecR; }
    public static float getSecG() { return smoothSecG; }
    public static float getSecB() { return smoothSecB; }

    public static SkyPhenomenonType getFadingOutPhenomenon() {
        return fadingOutPhenomenon;
    }

    public static float getFadingOutPhenomenonAlpha(float partialTick) {
        if (fadingOutPhenomenon == SkyPhenomenonType.NONE) {
            return 0.0F;
        }
        float pt = Mth.clamp(partialTick, 0.0F, 1.0F);
        return Mth.lerp(pt, prevFadeOutAlpha, fadeOutPhenomenonAlpha);
    }

    public static float getFadeOutPrimR() { return fadeOutPrimR; }
    public static float getFadeOutPrimG() { return fadeOutPrimG; }
    public static float getFadeOutPrimB() { return fadeOutPrimB; }
    public static float getFadeOutSecR() { return fadeOutSecR; }
    public static float getFadeOutSecG() { return fadeOutSecG; }
    public static float getFadeOutSecB() { return fadeOutSecB; }

    // =========================================================================
    // Client Tick: Sky Phenomenon Cross-Fade & Per-Particle Biome Emission
    // =========================================================================

    /**
     * Client tick hook: updates spatial Gaussian sky phenomenon targets, dual-slot cross-fades,
     * sub-tick interpolation anchors, and per-particle biome ambient emission.
     */
    public static void tickClient(Minecraft mc) {
        if (mc.isPaused() || mc.level == null || mc.player == null || mc.gameRenderer == null) {
            return;
        }
        ClientLevel level = mc.level;
        ensureLevelConsistency(level);

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        BlockPos center = camera.getBlockPosition();
        sampleNeighborhoodOnClientThread(level, camPos.x, center.getY(), camPos.z);

        // 1. Update smoothed sky phenomenon targets & dual-slot cross-fade
        updateSkyPhenomenaTick(level, center);

        // 2. Spawn Wind-Drifted Ambient Biome Particles & Water-Surface Steam Columns
        if (EntropicaConfig.ENABLE_BIOME_ENVIRONMENTAL_EFFECTS.get()) {
            spawnWindDriftedParticles(level, camPos);
            spawnWaterSurfaceSteamColumns(level, camPos);
        }
    }

    private static void updateSkyPhenomenaTick(ClientLevel level, BlockPos center) {
        prevPhenomenonAlpha = smoothPhenomenonAlpha;
        prevFadeOutAlpha = fadeOutPhenomenonAlpha;

        if (!EntropicaConfig.ENABLE_BIOME_CUSTOM_SKY.get()) {
            resetSkyPhenomena();
            return;
        }

        // Aggregate continuous 2D Gaussian weights across all SkyPhenomenonTypes in the 5x5 neighborhood
        Arrays.fill(TYPE_WEIGHT, 0.0F);
        Arrays.fill(TYPE_INTENSITY_SUM, 0.0F);
        Arrays.fill(TYPE_PRIM_R, 0.0F);
        Arrays.fill(TYPE_PRIM_G, 0.0F);
        Arrays.fill(TYPE_PRIM_B, 0.0F);
        Arrays.fill(TYPE_SEC_R, 0.0F);
        Arrays.fill(TYPE_SEC_G, 0.0F);
        Arrays.fill(TYPE_SEC_B, 0.0F);

        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            BiomeAtmosphereProfile p = SAMPLE_PROFILES[i];
            if (p == null) {
                continue;
            }
            SkyPhenomenonType type = p.getSkyPhenomenon();
            if (type == null || type == SkyPhenomenonType.NONE) {
                continue;
            }
            float w = SAMPLE_WEIGHTS[i];
            if (w <= 0.0F) {
                continue;
            }
            int ord = type.ordinal();
            int pRgb = p.getPhenomenonPrimaryRgb();
            int sRgb = p.getPhenomenonSecondaryRgb();
            TYPE_WEIGHT[ord] += w;
            TYPE_INTENSITY_SUM[ord] += w * p.getPhenomenonIntensity();
            TYPE_PRIM_R[ord] += w * (((pRgb >> 16) & 0xFF) / 255.0F);
            TYPE_PRIM_G[ord] += w * (((pRgb >> 8) & 0xFF) / 255.0F);
            TYPE_PRIM_B[ord] += w * ((pRgb & 0xFF) / 255.0F);
            TYPE_SEC_R[ord] += w * (((sRgb >> 16) & 0xFF) / 255.0F);
            TYPE_SEC_G[ord] += w * (((sRgb >> 8) & 0xFF) / 255.0F);
            TYPE_SEC_B[ord] += w * ((sRgb & 0xFF) / 255.0F);
        }

        float skyExposure = level.getBrightness(LightLayer.SKY, center) / 15.0F;
        float exposureFactor = BiomeTransitionMath.smoothstep(0.15F, 0.70F, skyExposure);

        SkyPhenomenonType dominantType = SkyPhenomenonType.NONE;
        float dominantWeight = 0.0F;
        if (exposureFactor > 1.0e-4F) {
            for (int ord = 1; ord < PHENOMENON_VALUES.length; ord++) {
                if (TYPE_WEIGHT[ord] > dominantWeight) {
                    dominantWeight = TYPE_WEIGHT[ord];
                    dominantType = PHENOMENON_VALUES[ord];
                }
            }
        }

        if (dominantType != SkyPhenomenonType.NONE && dominantWeight > 1.0e-4F) {
            int domOrd = dominantType.ordinal();
            float invW = 1.0F / dominantWeight;
            float avgIntensity = TYPE_INTENSITY_SUM[domOrd] * invW;
            float targetAlpha = avgIntensity * BiomeTransitionMath.smootherstep(dominantWeight) * exposureFactor;
            float targetPR = TYPE_PRIM_R[domOrd] * invW;
            float targetPG = TYPE_PRIM_G[domOrd] * invW;
            float targetPB = TYPE_PRIM_B[domOrd] * invW;
            float targetSR = TYPE_SEC_R[domOrd] * invW;
            float targetSG = TYPE_SEC_G[domOrd] * invW;
            float targetSB = TYPE_SEC_B[domOrd] * invW;

            if (activePhenomenon == SkyPhenomenonType.NONE || (activePhenomenon != dominantType && smoothPhenomenonAlpha < 0.002F)) {
                // Entering from a non-phenomenon biome: initialize colors cleanly before ramping alpha from 0
                activePhenomenon = dominantType;
                prevPhenomenonAlpha = 0.0F;
                smoothPhenomenonAlpha = 0.0F;
                smoothPrimR = targetPR;
                smoothPrimG = targetPG;
                smoothPrimB = targetPB;
                smoothSecR = targetSR;
                smoothSecG = targetSG;
                smoothSecB = targetSB;
            } else if (activePhenomenon != dominantType) {
                // Seamless cross-fade between two distinct SkyPhenomenonTypes with zero blink gap
                if (fadingOutPhenomenon == dominantType) {
                    // Player stepped back into the outgoing biome mid-crossfade: swap slots seamlessly
                    swapActiveAndFadeOutSlots();
                } else {
                    // Move current active phenomenon into the fading-out slot and begin fading in the new type
                    fadingOutPhenomenon = activePhenomenon;
                    prevFadeOutAlpha = prevPhenomenonAlpha;
                    fadeOutPhenomenonAlpha = smoothPhenomenonAlpha;
                    fadeOutPrimR = smoothPrimR;
                    fadeOutPrimG = smoothPrimG;
                    fadeOutPrimB = smoothPrimB;
                    fadeOutSecR = smoothSecR;
                    fadeOutSecG = smoothSecG;
                    fadeOutSecB = smoothSecB;

                    activePhenomenon = dominantType;
                    prevPhenomenonAlpha = 0.0F;
                    smoothPhenomenonAlpha = 0.0F;
                    smoothPrimR = targetPR;
                    smoothPrimG = targetPG;
                    smoothPrimB = targetPB;
                    smoothSecR = targetSR;
                    smoothSecG = targetSG;
                    smoothSecB = targetSB;
                }
            }

            smoothPhenomenonAlpha = BiomeTransitionMath.expSmooth(smoothPhenomenonAlpha, targetAlpha, 1.0F, PHENOMENON_IN_RATE);
            smoothPrimR = BiomeTransitionMath.expSmooth(smoothPrimR, targetPR, 1.0F, PHENOMENON_COLOR_RATE);
            smoothPrimG = BiomeTransitionMath.expSmooth(smoothPrimG, targetPG, 1.0F, PHENOMENON_COLOR_RATE);
            smoothPrimB = BiomeTransitionMath.expSmooth(smoothPrimB, targetPB, 1.0F, PHENOMENON_COLOR_RATE);
            smoothSecR = BiomeTransitionMath.expSmooth(smoothSecR, targetSR, 1.0F, PHENOMENON_COLOR_RATE);
            smoothSecG = BiomeTransitionMath.expSmooth(smoothSecG, targetSG, 1.0F, PHENOMENON_COLOR_RATE);
            smoothSecB = BiomeTransitionMath.expSmooth(smoothSecB, targetSB, 1.0F, PHENOMENON_COLOR_RATE);
        } else {
            // Leaving into a non-phenomenon biome or underground: smoothly decay active alpha to 0
            smoothPhenomenonAlpha = BiomeTransitionMath.expSmooth(smoothPhenomenonAlpha, 0.0F, 1.0F, PHENOMENON_OUT_RATE);
            if (smoothPhenomenonAlpha < 0.002F && prevPhenomenonAlpha < 0.002F) {
                smoothPhenomenonAlpha = 0.0F;
                prevPhenomenonAlpha = 0.0F;
                activePhenomenon = SkyPhenomenonType.NONE;
            }
        }

        // Decay secondary fading-out phenomenon slot independently
        if (fadingOutPhenomenon != SkyPhenomenonType.NONE) {
            fadeOutPhenomenonAlpha = BiomeTransitionMath.expSmooth(fadeOutPhenomenonAlpha, 0.0F, 1.0F, PHENOMENON_OUT_RATE);
            if (fadeOutPhenomenonAlpha < 0.002F && prevFadeOutAlpha < 0.002F) {
                fadeOutPhenomenonAlpha = 0.0F;
                prevFadeOutAlpha = 0.0F;
                fadingOutPhenomenon = SkyPhenomenonType.NONE;
            }
        }
    }

    private static void swapActiveAndFadeOutSlots() {
        SkyPhenomenonType tmpType = activePhenomenon;
        activePhenomenon = fadingOutPhenomenon;
        fadingOutPhenomenon = tmpType;

        float tmpPrev = prevPhenomenonAlpha;
        prevPhenomenonAlpha = prevFadeOutAlpha;
        prevFadeOutAlpha = tmpPrev;

        float tmpAlpha = smoothPhenomenonAlpha;
        smoothPhenomenonAlpha = fadeOutPhenomenonAlpha;
        fadeOutPhenomenonAlpha = tmpAlpha;

        float tPR = smoothPrimR, tPG = smoothPrimG, tPB = smoothPrimB;
        smoothPrimR = fadeOutPrimR; smoothPrimG = fadeOutPrimG; smoothPrimB = fadeOutPrimB;
        fadeOutPrimR = tPR;         fadeOutPrimG = tPG;         fadeOutPrimB = tPB;

        float tSR = smoothSecR, tSG = smoothSecG, tSB = smoothSecB;
        smoothSecR = fadeOutSecR; smoothSecG = fadeOutSecG; smoothSecB = fadeOutSecB;
        fadeOutSecR = tSR;        fadeOutSecG = tSG;        fadeOutSecB = tSB;
    }

    private static void spawnWindDriftedParticles(ClientLevel level, Vec3 camPos) {
        // Fast exit when no sample in the 5x5 neighborhood emits ambient particles
        int maxCandidateCount = 0;
        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            BiomeAtmosphereProfile p = SAMPLE_PROFILES[i];
            if (p != null && p.getAmbientParticlesPerTick() > maxCandidateCount) {
                maxCandidateCount = p.getAmbientParticlesPerTick();
            }
        }
        if (maxCandidateCount <= 0) {
            return;
        }

        RandomSource random = level.random;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < maxCandidateCount; i++) {
            double px = camPos.x + (random.nextDouble() - 0.5D) * 28.0D;
            double py = camPos.y + (random.nextDouble() - 0.3D) * 16.0D;
            double pz = camPos.z + (random.nextDouble() - 0.5D) * 28.0D;
            pos.set(Mth.floor(px), Mth.floor(py), Mth.floor(pz));

            // Check the actual biome at the candidate particle's spawn position so particles naturally
            // appear ahead inside custom biomes and never leak into adjacent non-effect biomes
            BiomeAtmosphereProfile profile = EntropicaBiomeAtmospheres.getForHolder(level.getBiome(pos));
            if (profile == null || i >= profile.getAmbientParticlesPerTick()) {
                continue;
            }
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            if (level.getBrightness(LightLayer.SKY, pos) < 4 && "Overworld".equals(profile.getBiome().getDimension())) {
                continue;
            }

            int rgb = profile.getParticleColorRgb();
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;
            float scale = profile.getParticleScale();
            int lifetime = profile.getParticleLifetimeTicks();
            float vx = profile.getParticleWindVx();
            float vy = profile.getParticleWindVy();
            float vz = profile.getParticleWindVz();

            double jitterVx = vx + (random.nextDouble() - 0.5D) * 0.015D;
            double jitterVy = vy + (random.nextDouble() - 0.5D) * 0.008D;
            double jitterVz = vz + (random.nextDouble() - 0.5D) * 0.015D;

            level.addParticle(
                    new TimedTintableParticleOption(
                            ModParticles.TINTABLE_MIST.get(),
                            r, g, b,
                            lifetime,
                            scale,
                            (float) (random.nextDouble() * Math.PI * 2.0),
                            0.015F,
                            TimedTintableParticleOption.MODE_NOISE,
                            vx, vy, vz
                    ),
                    px, py, pz,
                    jitterVx, jitterVy, jitterVz
            );
        }
    }

    /**
     * Emits genuine rising white steam columns directly from open water channels and
     * underwater bubble-column geothermal vents in steaming-water biomes (such as {@code 2.2 Auroral Skerries}),
     * without requiring any campfires or artificial smoke blocks in the world.
     */
    private static void spawnWaterSurfaceSteamColumns(ClientLevel level, Vec3 camPos) {
        int maxColumns = 0;
        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            BiomeAtmosphereProfile p = SAMPLE_PROFILES[i];
            if (p != null && p.getWaterSurfaceSteamColumnsPerTick() > maxColumns) {
                maxColumns = p.getWaterSurfaceSteamColumnsPerTick();
            }
        }
        // Even if the camera stands just outside the 5x5 kernel, probe occasional nearby water columns
        int candidateAttempts = Math.max(maxColumns, 2);

        RandomSource random = level.random;
        BlockPos.MutableBlockPos surfacePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos abovePos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < candidateAttempts; i++) {
            double px = camPos.x + (random.nextDouble() - 0.5D) * 44.0D;
            double pz = camPos.z + (random.nextDouble() - 0.5D) * 44.0D;
            int bx = Mth.floor(px);
            int bz = Mth.floor(pz);
            if (!level.hasChunk(bx >> 4, bz >> 4)) {
                continue;
            }

            int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz) - 1;
            surfacePos.set(bx, topY, bz);
            BlockState surfaceState = level.getBlockState(surfacePos);
            boolean isBubbleVent = surfaceState.is(Blocks.BUBBLE_COLUMN);
            if (!isBubbleVent && !surfaceState.is(Blocks.WATER)) {
                continue;
            }

            abovePos.set(bx, topY + 1, bz);
            if (!level.getBlockState(abovePos).isAir()) {
                continue;
            }

            BiomeAtmosphereProfile profile = EntropicaBiomeAtmospheres.getForHolder(level.getBiome(abovePos));
            if (profile == null || i >= profile.getWaterSurfaceSteamColumnsPerTick()) {
                continue;
            }

            int rgb = profile.getWaterSurfaceSteamColorRgb();
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;

            double waterTopY = topY + 0.92D;
            int plumePuffs = isBubbleVent ? 3 : 1;
            for (int p = 0; p < plumePuffs; p++) {
                double sx = bx + 0.2D + random.nextDouble() * 0.6D;
                double sz = bz + 0.2D + random.nextDouble() * 0.6D;
                double sy = waterTopY + p * (0.45D + random.nextDouble() * 0.35D);
                float scale = isBubbleVent
                        ? (2.1F + random.nextFloat() * 1.1F)
                        : (1.55F + random.nextFloat() * 0.85F);
                int lifetime = isBubbleVent ? (95 + random.nextInt(40)) : (75 + random.nextInt(35));
                float vy = isBubbleVent
                        ? (0.068F + random.nextFloat() * 0.036F)
                        : (0.042F + random.nextFloat() * 0.026F);
                float vx = (random.nextFloat() - 0.5F) * 0.012F;
                float vz = (random.nextFloat() - 0.5F) * 0.012F;

                level.addParticle(
                        new TimedTintableParticleOption(
                                ModParticles.TINTABLE_MIST.get(),
                                r, g, b,
                                lifetime,
                                scale,
                                (float) (random.nextDouble() * Math.PI * 2.0),
                                0.010F,
                                TimedTintableParticleOption.MODE_NOISE,
                                vx, vy, vz
                        ),
                        sx, sy, sz,
                        vx, vy, vz
                );
            }

            if (isBubbleVent) {
                level.addParticle(
                        ParticleTypes.BUBBLE_POP,
                        bx + 0.25D + random.nextDouble() * 0.5D,
                        waterTopY + 0.04D,
                        bz + 0.25D + random.nextDouble() * 0.5D,
                        0.0D, 0.03D, 0.0D
                );
                level.addParticle(
                        ParticleTypes.CLOUD,
                        bx + 0.3D + random.nextDouble() * 0.4D,
                        waterTopY + 0.25D,
                        bz + 0.3D + random.nextDouble() * 0.4D,
                        (random.nextDouble() - 0.5D) * 0.01D,
                        0.065D + random.nextDouble() * 0.03D,
                        (random.nextDouble() - 0.5D) * 0.01D
                );
            } else if (random.nextInt(3) == 0) {
                level.addParticle(
                        ParticleTypes.CLOUD,
                        bx + 0.2D + random.nextDouble() * 0.6D,
                        waterTopY + 0.12D,
                        bz + 0.2D + random.nextDouble() * 0.6D,
                        (random.nextDouble() - 0.5D) * 0.008D,
                        0.038D + random.nextDouble() * 0.02D,
                        (random.nextDouble() - 0.5D) * 0.008D
                );
            }
        }
    }

    // =========================================================================
    // Render Hooks: Fog Distance, Fog Color, Sky Color & Star Brightness
    // =========================================================================

    /**
     * Smoothly adjusts 1.21.10 {@link FogData} start/end distances for biomes with custom fog density.
     */
    public static void applyCustomFogDistances(
            FogData fogData,
            Entity cameraEntity,
            BlockPos cameraBlockPos,
            ClientLevel level,
            float renderDistanceBlocks,
            float deltaTicks) {
        if (!EntropicaConfig.ENABLE_BIOME_CUSTOM_FOG.get() || level == null || cameraBlockPos == null) {
            smoothFogBlend = 0.0F;
            smoothLightRelief = 0.0F;
            return;
        }
        ensureLevelConsistency(level);

        Vec3 camVec = resolveCameraPos(cameraEntity, cameraBlockPos);
        sampleNeighborhoodOnClientThread(level, camVec.x, cameraBlockPos.getY(), camVec.z);

        float rawFogPresence = 0.0F;
        float strengthWeighted = 0.0F;
        float customWeightSum = 0.0F;
        float weightedStart = 0.0F;
        float weightedBaseEnd = 0.0F;
        float weightedReliefCap = 0.0F;

        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            BiomeAtmosphereProfile p = SAMPLE_PROFILES[i];
            if (p != null && p.hasCustomFogDistance()) {
                float w = SAMPLE_WEIGHTS[i];
                if (w <= 0.0F) {
                    continue;
                }
                float strength = p.getFogBlendStrength();
                float cw = w * strength;
                rawFogPresence += w;
                strengthWeighted += cw;
                customWeightSum += cw;
                weightedStart += cw * p.getFogStartBlocks();
                weightedBaseEnd += cw * p.getFogEndBlocks();
                weightedReliefCap += cw * p.getLightHeldFogReliefBlocks();
            }
        }

        float baseStart = fogData.environmentalStart;
        float baseEnd = fogData.environmentalEnd;
        boolean holdingLight = isHoldingLightSource(cameraEntity);

        // Stale-state guard: seed smoothed start/end from live vanilla environmental fog before entering
        if (smoothFogBlend < 0.002F) {
            smoothFogStart = baseStart;
            smoothFogEnd = baseEnd;
            float initRelief = customWeightSum > 1.0e-5F ? (weightedReliefCap / customWeightSum) : 0.0F;
            smoothLightRelief = holdingLight ? initRelief : 0.0F;
        }

        Minecraft mc = Minecraft.getInstance();
        float dt = (mc != null && mc.isPaused()) ? 0.0F : Mth.clamp(deltaTicks, 0.0F, 4.0F);

        float avgStrength = rawFogPresence > 1.0e-5F ? (strengthWeighted / rawFogPresence) : 0.0F;
        float targetBlend = BiomeTransitionMath.smootherstep(rawFogPresence) * avgStrength;
        float blendDecay = BiomeTransitionMath.expDecayFactor(dt, FOG_BLEND_SMOOTH_RATE);
        smoothFogBlend = Mth.lerp(blendDecay, smoothFogBlend, targetBlend);

        float avgReliefCap = customWeightSum > 1.0e-5F ? (weightedReliefCap / customWeightSum) : 0.0F;
        float targetLightRelief = holdingLight ? avgReliefCap : 0.0F;
        float reliefDecay = BiomeTransitionMath.expDecayFactor(dt, LIGHT_RELIEF_SMOOTH_RATE);
        smoothLightRelief = Mth.lerp(reliefDecay, smoothLightRelief, targetLightRelief);

        float distDecay = BiomeTransitionMath.expDecayFactor(dt, FOG_DIST_SMOOTH_RATE);
        if (customWeightSum > 1.0e-5F) {
            float avgStart = weightedStart / customWeightSum;
            float avgEnd = Math.min(renderDistanceBlocks, (weightedBaseEnd / customWeightSum) + smoothLightRelief);
            avgEnd = Math.max(avgStart + 4.0F, avgEnd);
            smoothFogStart = Mth.lerp(distDecay, smoothFogStart, avgStart);
            smoothFogEnd = Mth.lerp(distDecay, smoothFogEnd, avgEnd);
        } else {
            smoothFogStart = Mth.lerp(distDecay, smoothFogStart, baseStart);
            smoothFogEnd = Mth.lerp(distDecay, smoothFogEnd, baseEnd);
        }

        if (smoothFogBlend > 0.001F) {
            fogData.environmentalStart = Mth.lerp(smoothFogBlend, baseStart, smoothFogStart);
            fogData.environmentalEnd = Mth.lerp(smoothFogBlend, baseEnd, smoothFogEnd);
        } else {
            smoothFogBlend = 0.0F;
        }
    }

    /**
     * Smoothly tints atmospheric/dimension fog colors across day &amp; night for Entropica biomes.
     */
    public static int adjustFogBaseColor(ClientLevel level, Camera camera, float partialTick, int baseColorArgb) {
        if (!EntropicaConfig.ENABLE_BIOME_CUSTOM_FOG.get() || level == null || camera == null) {
            smoothFogColorWeight = 0.0F;
            return baseColorArgb;
        }
        ensureLevelConsistency(level);

        Vec3 camPos = camera.getPosition();
        int sampleY = camera.getBlockPosition().getY();
        BiomeAtmosphereProfile[] profiles;
        float[] weights;
        if (isOnClientThread()) {
            sampleNeighborhoodOnClientThread(level, camPos.x, sampleY, camPos.z);
            profiles = SAMPLE_PROFILES;
            weights = SAMPLE_WEIGHTS;
        } else {
            NeighborhoodSnapshot snap = sampleNeighborhoodOffThread(level, camPos.x, sampleY, camPos.z);
            profiles = snap.profiles;
            weights = snap.weights;
        }

        float nightFactor = computeNightFactor(level, partialTick);
        float baseR = ARGB.red(baseColorArgb);
        float baseG = ARGB.green(baseColorArgb);
        float baseB = ARGB.blue(baseColorArgb);

        float colorPresence = 0.0F;
        float rWeighted = 0.0F, gWeighted = 0.0F, bWeighted = 0.0F;

        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            BiomeAtmosphereProfile p = profiles[i];
            if (p == null) {
                continue;
            }
            int dayRgb = p.getDayFogTintRgb();
            int nightRgb = p.getNightFogColorRgb();
            if (dayRgb < 0 && nightRgb < 0) {
                continue;
            }
            float w = weights[i];
            if (w <= 0.0F) {
                continue;
            }

            float dayR = dayRgb >= 0 ? ((dayRgb >> 16) & 0xFF) : baseR;
            float dayG = dayRgb >= 0 ? ((dayRgb >> 8) & 0xFF) : baseG;
            float dayB = dayRgb >= 0 ? (dayRgb & 0xFF) : baseB;

            float nightR = nightRgb >= 0 ? ((nightRgb >> 16) & 0xFF) : baseR;
            float nightG = nightRgb >= 0 ? ((nightRgb >> 8) & 0xFF) : baseG;
            float nightB = nightRgb >= 0 ? (nightRgb & 0xFF) : baseB;

            colorPresence += w;
            rWeighted += w * Mth.lerp(nightFactor, dayR, nightR);
            gWeighted += w * Mth.lerp(nightFactor, dayG, nightG);
            bWeighted += w * Mth.lerp(nightFactor, dayB, nightB);
        }

        float targetWeight = BiomeTransitionMath.smootherstep(colorPresence) * 0.75F;
        if (!isOnClientThread()) {
            if (targetWeight <= 0.001F || colorPresence <= 1.0e-5F) {
                return baseColorArgb;
            }
            float invP = 1.0F / colorPresence;
            return ARGB.color(
                    ARGB.alpha(baseColorArgb),
                    Mth.clamp(Math.round(Mth.lerp(targetWeight, baseR, rWeighted * invP)), 0, 255),
                    Mth.clamp(Math.round(Mth.lerp(targetWeight, baseG, gWeighted * invP)), 0, 255),
                    Mth.clamp(Math.round(Mth.lerp(targetWeight, baseB, bWeighted * invP)), 0, 255)
            );
        }

        if (smoothFogColorWeight < 0.002F && colorPresence > 1.0e-5F) {
            float invP = 1.0F / colorPresence;
            smoothFogColorR = rWeighted * invP;
            smoothFogColorG = gWeighted * invP;
            smoothFogColorB = bWeighted * invP;
        }

        float dt = consumeChannelDeltaTicks(CHANNEL_FOG_COLOR);
        float decay = BiomeTransitionMath.expDecayFactor(dt, COLOR_SMOOTH_RATE);
        smoothFogColorWeight = Mth.lerp(decay, smoothFogColorWeight, targetWeight);

        if (colorPresence > 1.0e-5F) {
            float invP = 1.0F / colorPresence;
            smoothFogColorR = Mth.lerp(decay, smoothFogColorR, rWeighted * invP);
            smoothFogColorG = Mth.lerp(decay, smoothFogColorG, gWeighted * invP);
            smoothFogColorB = Mth.lerp(decay, smoothFogColorB, bWeighted * invP);
        }

        if (smoothFogColorWeight <= 0.001F) {
            smoothFogColorWeight = 0.0F;
            return baseColorArgb;
        }

        return ARGB.color(
                ARGB.alpha(baseColorArgb),
                Mth.clamp(Math.round(Mth.lerp(smoothFogColorWeight, baseR, smoothFogColorR)), 0, 255),
                Mth.clamp(Math.round(Mth.lerp(smoothFogColorWeight, baseG, smoothFogColorG)), 0, 255),
                Mth.clamp(Math.round(Mth.lerp(smoothFogColorWeight, baseB, smoothFogColorB)), 0, 255)
        );
    }

    /**
     * Smoothly applies luminous night-sky colors (preventing vanilla's 0.0 midnight black-out)
     * and daytime eclipse darkening in a 100% Sodium &amp; Iris shader-compatible manner.
     */
    public static int adjustSkyColor(ClientLevel level, Vec3 cameraPos, float partialTick, int baseSkyArgb) {
        if (!EntropicaConfig.ENABLE_BIOME_CUSTOM_SKY.get() || level == null || cameraPos == null) {
            smoothDaySkyScale = 1.0F;
            smoothNightSkyWeight = 0.0F;
            return baseSkyArgb;
        }
        ensureLevelConsistency(level);

        int sampleY = Mth.floor(cameraPos.y);
        BiomeAtmosphereProfile[] profiles;
        float[] weights;
        if (isOnClientThread()) {
            sampleNeighborhoodOnClientThread(level, cameraPos.x, sampleY, cameraPos.z);
            profiles = SAMPLE_PROFILES;
            weights = SAMPLE_WEIGHTS;
        } else {
            NeighborhoodSnapshot snap = sampleNeighborhoodOffThread(level, cameraPos.x, sampleY, cameraPos.z);
            profiles = snap.profiles;
            weights = snap.weights;
        }

        float nightFactor = computeNightFactor(level, partialTick);
        float rainAttenuation = 1.0F - level.getRainLevel(partialTick) * 0.65F;

        float dayScaleWeighted = 0.0F;
        float nightPresence = 0.0F;
        float nrWeighted = 0.0F, ngWeighted = 0.0F, nbWeighted = 0.0F;

        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            float w = weights[i];
            if (w <= 0.0F) {
                continue;
            }
            BiomeAtmosphereProfile p = profiles[i];
            if (p != null) {
                dayScaleWeighted += w * p.getDaySkyBrightnessScale();
                int nightRgb = p.getNightSkyColorRgb();
                if (nightRgb >= 0) {
                    nightPresence += w;
                    nrWeighted += w * ((nightRgb >> 16) & 0xFF);
                    ngWeighted += w * ((nightRgb >> 8) & 0xFF);
                    nbWeighted += w * (nightRgb & 0xFF);
                }
            } else {
                dayScaleWeighted += w; // Non-effect biome baseline day brightness scale = 1.0f
            }
        }

        float easedNightPresence = BiomeTransitionMath.smootherstep(nightPresence);
        float useDayScale;
        float useNightWeight;
        float useNightR, useNightG, useNightB;

        if (isOnClientThread()) {
            if (smoothNightSkyWeight < 0.002F && nightPresence > 1.0e-5F) {
                float invN = 1.0F / nightPresence;
                smoothNightSkyR = nrWeighted * invN;
                smoothNightSkyG = ngWeighted * invN;
                smoothNightSkyB = nbWeighted * invN;
            }

            float dt = consumeChannelDeltaTicks(CHANNEL_SKY_COLOR);
            float decay = BiomeTransitionMath.expDecayFactor(dt, COLOR_SMOOTH_RATE);
            smoothDaySkyScale = Mth.lerp(decay, smoothDaySkyScale, dayScaleWeighted);
            smoothNightSkyWeight = Mth.lerp(decay, smoothNightSkyWeight, easedNightPresence);

            if (nightPresence > 1.0e-5F) {
                float invN = 1.0F / nightPresence;
                smoothNightSkyR = Mth.lerp(decay, smoothNightSkyR, nrWeighted * invN);
                smoothNightSkyG = Mth.lerp(decay, smoothNightSkyG, ngWeighted * invN);
                smoothNightSkyB = Mth.lerp(decay, smoothNightSkyB, nbWeighted * invN);
            }
            if (smoothNightSkyWeight <= 0.001F) {
                smoothNightSkyWeight = 0.0F;
            }

            useDayScale = smoothDaySkyScale;
            useNightWeight = smoothNightSkyWeight;
            useNightR = smoothNightSkyR;
            useNightG = smoothNightSkyG;
            useNightB = smoothNightSkyB;
        } else {
            useDayScale = dayScaleWeighted;
            useNightWeight = easedNightPresence;
            float invN = nightPresence > 1.0e-5F ? (1.0F / nightPresence) : 0.0F;
            useNightR = nrWeighted * invN;
            useNightG = ngWeighted * invN;
            useNightB = nbWeighted * invN;
        }

        float skyR = ARGB.red(baseSkyArgb);
        float skyG = ARGB.green(baseSkyArgb);
        float skyB = ARGB.blue(baseSkyArgb);

        float effectiveDayScale = Mth.lerp(nightFactor, useDayScale, 1.0F);
        if (Math.abs(effectiveDayScale - 1.0F) > 0.002F) {
            skyR *= effectiveDayScale;
            skyG *= effectiveDayScale;
            skyB *= effectiveDayScale;
        }

        float finalNightBlend = useNightWeight * nightFactor * rainAttenuation;
        if (finalNightBlend > 0.001F) {
            skyR = Mth.lerp(finalNightBlend, skyR, useNightR);
            skyG = Mth.lerp(finalNightBlend, skyG, useNightG);
            skyB = Mth.lerp(finalNightBlend, skyB, useNightB);
        }

        return ARGB.color(
                ARGB.alpha(baseSkyArgb),
                Mth.clamp(Math.round(skyR), 0, 255),
                Mth.clamp(Math.round(skyG), 0, 255),
                Mth.clamp(Math.round(skyB), 0, 255)
        );
    }

    /**
     * Smoothly blends custom star brightness boosts across biome boundaries using the continuous
     * 2D Gaussian kernel + frame-rate-independent temporal smoothing.
     */
    public static float adjustStarBrightness(ClientLevel level, float baseStarBrightness) {
        if (!EntropicaConfig.ENABLE_BIOME_CUSTOM_SKY.get() || level == null) {
            smoothStarBoost = 0.0F;
            return baseStarBrightness;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gameRenderer == null) {
            return baseStarBrightness;
        }
        ensureLevelConsistency(level);

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        sampleNeighborhoodOnClientThread(level, camPos.x, camera.getBlockPosition().getY(), camPos.z);

        float targetBoost = 0.0F;
        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            BiomeAtmosphereProfile p = SAMPLE_PROFILES[i];
            if (p != null && p.getStarBrightnessBoost() > 0.0F) {
                targetBoost += SAMPLE_WEIGHTS[i] * p.getStarBrightnessBoost();
            }
        }

        float dt = consumeChannelDeltaTicks(CHANNEL_STAR_BOOST);
        smoothStarBoost = BiomeTransitionMath.expSmooth(smoothStarBoost, targetBoost, dt, STAR_SMOOTH_RATE);
        if (smoothStarBoost <= 0.001F && targetBoost <= 1.0e-5F) {
            smoothStarBoost = 0.0F;
            return baseStarBrightness;
        }
        return Mth.clamp(baseStarBrightness + smoothStarBoost, 0.0F, 1.0F);
    }

    // =========================================================================
    // Internal Spatial Cache, Frame Timing & State Helpers
    // =========================================================================

    private static Vec3 resolveCameraPos(Entity cameraEntity, BlockPos cameraBlockPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gameRenderer != null) {
            Camera cam = mc.gameRenderer.getMainCamera();
            if (cam != null && cam.isInitialized() && (cameraEntity == null || cam.getEntity() == cameraEntity)) {
                return cam.getPosition();
            }
        }
        return cameraEntity != null ? cameraEntity.position() : Vec3.atCenterOf(cameraBlockPos);
    }

    private static boolean isOnClientThread() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null || mc.isSameThread();
    }

    private static void sampleNeighborhoodOnClientThread(ClientLevel level, double camX, int sampleY, double camZ) {
        long gameTime = level.getGameTime();
        if (gameTime == cachedSampleGameTime
                && sampleY == cachedSampleY
                && Math.abs(camX - cachedSampleX) < 1.0e-3D
                && Math.abs(camZ - cachedSampleZ) < 1.0e-3D) {
            return;
        }
        cachedSampleGameTime = gameTime;
        cachedSampleY = sampleY;
        cachedSampleX = camX;
        cachedSampleZ = camZ;

        BiomeTransitionMath.computeGaussianGrid5x5(camX, camZ, SAMPLE_BX, SAMPLE_BZ, SAMPLE_WEIGHTS);
        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            if (SAMPLE_WEIGHTS[i] > 0.0F) {
                MUTABLE_SAMPLE_POS.set(SAMPLE_BX[i], sampleY, SAMPLE_BZ[i]);
                SAMPLE_PROFILES[i] = EntropicaBiomeAtmospheres.getForHolder(level.getBiome(MUTABLE_SAMPLE_POS));
            } else {
                SAMPLE_PROFILES[i] = null;
            }
        }
    }

    private record NeighborhoodSnapshot(BiomeAtmosphereProfile[] profiles, float[] weights) {}

    private static NeighborhoodSnapshot sampleNeighborhoodOffThread(ClientLevel level, double camX, int sampleY, double camZ) {
        int[] bx = new int[BiomeTransitionMath.KERNEL_SAMPLE_COUNT];
        int[] bz = new int[BiomeTransitionMath.KERNEL_SAMPLE_COUNT];
        float[] weights = new float[BiomeTransitionMath.KERNEL_SAMPLE_COUNT];
        BiomeAtmosphereProfile[] profiles = new BiomeAtmosphereProfile[BiomeTransitionMath.KERNEL_SAMPLE_COUNT];
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        BiomeTransitionMath.computeGaussianGrid5x5(camX, camZ, bx, bz, weights);
        for (int i = 0; i < BiomeTransitionMath.KERNEL_SAMPLE_COUNT; i++) {
            if (weights[i] > 0.0F) {
                mpos.set(bx[i], sampleY, bz[i]);
                profiles[i] = EntropicaBiomeAtmospheres.getForHolder(level.getBiome(mpos));
            }
        }
        return new NeighborhoodSnapshot(profiles, weights);
    }

    private static float consumeChannelDeltaTicks(int channelIndex) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();
        if (mc != null && mc.isPaused()) {
            LAST_CHANNEL_NANOS[channelIndex] = now;
            return 0.0F;
        }
        long prev = LAST_CHANNEL_NANOS[channelIndex];
        LAST_CHANNEL_NANOS[channelIndex] = now;
        if (prev == 0L) {
            return 1.0F;
        }
        long elapsedNanos = now - prev;
        if (elapsedNanos <= 100_000L) {
            // Sub-0.1ms duplicate query within the same rendered frame (e.g. shader shadow/reflection pass)
            return 0.0F;
        }
        float deltaTicks = (float) (elapsedNanos * 1.0e-9D * 20.0D);
        return Mth.clamp(deltaTicks, 0.0F, 4.0F);
    }

    private static void ensureLevelConsistency(ClientLevel level) {
        ClientLevel prev = activeLevelRef != null ? activeLevelRef.get() : null;
        if (prev == level) {
            return;
        }
        activeLevelRef = new WeakReference<>(level);
        cachedSampleGameTime = Long.MIN_VALUE;
        cachedSampleY = Integer.MIN_VALUE;
        cachedSampleX = Double.NaN;
        cachedSampleZ = Double.NaN;
        Arrays.fill(SAMPLE_PROFILES, null);
        Arrays.fill(LAST_CHANNEL_NANOS, 0L);

        smoothFogBlend = 0.0F;
        smoothFogStart = 0.0F;
        smoothFogEnd = 256.0F;
        smoothLightRelief = 0.0F;

        smoothFogColorWeight = 0.0F;
        smoothDaySkyScale = 1.0F;
        smoothNightSkyWeight = 0.0F;
        smoothStarBoost = 0.0F;

        resetSkyPhenomena();
    }

    private static void resetSkyPhenomena() {
        activePhenomenon = SkyPhenomenonType.NONE;
        prevPhenomenonAlpha = 0.0F;
        smoothPhenomenonAlpha = 0.0F;
        fadingOutPhenomenon = SkyPhenomenonType.NONE;
        prevFadeOutAlpha = 0.0F;
        fadeOutPhenomenonAlpha = 0.0F;
    }

    private static float computeNightFactor(ClientLevel level, float partialTick) {
        float timeOfDay = level.getTimeOfDay(partialTick);
        float dayBrightness = Mth.clamp(Mth.cos(timeOfDay * ((float) Math.PI * 2.0F)) * 2.0F + 0.5F, 0.0F, 1.0F);
        return 1.0F - dayBrightness;
    }

    private static boolean isHoldingLightSource(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        return isLightEmittingStack(living.getMainHandItem()) || isLightEmittingStack(living.getOffhandItem());
    }

    private static boolean isLightEmittingStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState().getLightEmission() >= 7;
        }
        return stack.is(Items.GLOWSTONE_DUST) || stack.is(Items.BLAZE_ROD) || stack.is(Items.LAVA_BUCKET);
    }
}
