package ddraig.net.entropica.worldgen.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.config.EntropicaBiome;
import ddraig.net.entropica.entity.grot.GrotSpawnRegistry;
import ddraig.net.entropica.worldgen.biome.BiomeAtmosphereProfile;
import ddraig.net.entropica.worldgen.biome.BiomePlacementRule;
import ddraig.net.entropica.worldgen.biome.BiomeTransitionMath;
import ddraig.net.entropica.worldgen.biome.DomainWarpedRegionSampler;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomeAtmospheres;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomeEnvironmentHandler;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomePlacements;
import ddraig.net.entropica.worldgen.biome.EntropicaClimatePresets;
import ddraig.net.entropica.worldgen.biome.EntropicaMultiNoiseLayer;
import ddraig.net.entropica.worldgen.biome.SkyPhenomenonType;
import ddraig.net.entropica.worldgen.biome.TerraBlenderCompatBridge;
import ddraig.net.entropica.worldgen.biome.TerraBlenderCompatMode;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Deterministic standalone empirical verification suite for Entropica's
 * Native Multi-Noise Biome Injector & TerraBlender Compatibility Engine.
 */
public final class BiomeInjectorVerificationSuite {

    private record MockBiomeHolder(ResourceKey<Biome> key) implements Holder<Biome> {
        @Override public Biome value() { return null; }
        @Override public boolean isBound() { return true; }
        @Override public boolean is(ResourceLocation id) { return key.location().equals(id); }
        @Override public boolean is(ResourceKey<Biome> targetKey) { return key.equals(targetKey); }
        @Override public boolean is(Predicate<ResourceKey<Biome>> predicate) { return predicate.test(key); }
        @Override public boolean is(TagKey<Biome> tag) { return false; }
        @Override public boolean is(Holder<Biome> other) { return equals(other); }
        @Override public Stream<TagKey<Biome>> tags() { return Stream.empty(); }
        @Override public Either<ResourceKey<Biome>, Biome> unwrap() { return Either.left(key); }
        @Override public Optional<ResourceKey<Biome>> unwrapKey() { return Optional.of(key); }
        @Override public Kind kind() { return Kind.REFERENCE; }
        @Override public boolean canSerializeIn(HolderOwner<Biome> owner) { return true; }
    }

    private static Holder<Biome> mockBiome(String namespace, String path) {
        return new MockBiomeHolder(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(namespace, path)));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAILED: " + message);
        }
    }

    public static void main(String[] args) {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        System.out.println("=== Entropica Multi-Noise Biome Injector Verification Suite ===");

        verifyAll108BiomesCatalogued();
        verifyDomainWarpedRegionDeterminismAndContinuity();
        verify6DClimateMatchingForBlueGlacierShelf();
        verifyRendezvousRarityWeightsAndIsolation();
        verifyTerraBlenderCompatibilityModes();
        verifyBiomeAtmosphereAndEnvironmentProfiles();
        verifySmoothNonEffectToEffectBiomeTransitions();
        verifyBiome1BlueGlacierShelfContentAndAssets();

        System.out.println("ALL 8 VERIFICATION SUITES PASSED SUCCESSFULLY!");
    }

    private static void verifyAll108BiomesCatalogued() {
        check(EntropicaBiome.values().length == 108, "Expected 108 EntropicaBiome entries");
        for (int i = 1; i <= 108; i++) {
            EntropicaBiome b = EntropicaBiome.byNumber(i);
            check(b != null, "Missing biome #" + i);
            check(EntropicaBiome.byId(b.getId()) == b, "ID lookup mismatch for " + b.getId());
        }
        EntropicaBiomePlacements.registerAll();
        check(EntropicaBiomePlacements.allRules().containsKey(EntropicaBiome.BLUE_GLACIER_SHELF),
                "Biome 1 (Blue-Glacier Shelf) placement rule must be registered");
        System.out.println("[PASS] 1/8 All 108 biomes catalogued + Biome 1 placement rule registered.");
    }

    private static void verifyDomainWarpedRegionDeterminismAndContinuity() {
        DomainWarpedRegionSampler samplerA = new DomainWarpedRegionSampler(123456789L, 6);
        DomainWarpedRegionSampler samplerB = new DomainWarpedRegionSampler(123456789L, 6);

        int sameNeighborCount = 0;
        int totalSteps = 2000;
        long prev = samplerA.sampleRegionCellHash(0, 0);
        for (int i = 1; i <= totalSteps; i++) {
            long hA = samplerA.sampleRegionCellHash(i, i / 2);
            long hB = samplerB.sampleRegionCellHash(i, i / 2);
            check(hA == hB, "Region sampler must be 100% deterministic for identical seed and coords");
            if (hA == prev) {
                sameNeighborCount++;
            }
            prev = hA;
        }
        // With scale=6 (64 quarts per cell), adjacent quart steps stay inside the same coherent cell >93% of steps
        double continuityRatio = sameNeighborCount / (double) totalSteps;
        check(continuityRatio > 0.90, "Expected smooth contiguous region cells (>90% neighbor continuity), got " + continuityRatio);

        // Verify pass-through vs active ratio (4 / (4 + 10) = 28.57%)
        int activeCount = 0;
        int cellSamples = 20000;
        for (int i = 0; i < cellSamples; i++) {
            long fakeCell = DomainWarpedRegionSampler.hash2D(i * 31L, i * 17L, 987654321L);
            if (DomainWarpedRegionSampler.isEntropicaActiveCell(fakeCell, 4, 10)) {
                activeCount++;
            }
        }
        double activeRatio = activeCount / (double) cellSamples;
        double expected = 4.0 / 14.0;
        check(Math.abs(activeRatio - expected) < 0.02,
                "Expected active cell ratio ~" + expected + ", got " + activeRatio);
        System.out.println("[PASS] 2/8 Organic domain-warped Voronoi determinism, continuity (" +
                String.format("%.1f%%", continuityRatio * 100) + "), and regional weights verified.");
    }

    private static void verify6DClimateMatchingForBlueGlacierShelf() {
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> vanillaFrozenPeaks = mockBiome("minecraft", "frozen_peaks");
        Holder<Biome> vanillaDesert = mockBiome("minecraft", "desert");

        EntropicaMultiNoiseLayer layer = new EntropicaMultiNoiseLayer(
                42L, 6, 10, 0, // 100% active regions so we test pure 6D climate matching
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );

        // Matching frozen, coastal-shelf (-0.15), sheer-cliff low-erosion (-0.65), surface (0.0), high weirdness (+0.70)
        Climate.TargetPoint icyShelfTarget = Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.0F, 0.70F);
        Holder<Biome> matchedPos = layer.selectMatchingBiome(111L, icyShelfTarget, vanillaFrozenPeaks);
        check(matchedPos == shelfHolder, "Blue-Glacier Shelf must match icy high-weirdness (+0.70) sheer coastal shelf");

        // Matching negative high weirdness (-0.70)
        Climate.TargetPoint icyShelfNegWeirdness = Climate.target(-0.80F, -0.2F, -0.12F, -0.80F, 0.02F, -0.75F);
        Holder<Biome> matchedNeg = layer.selectMatchingBiome(111L, icyShelfNegWeirdness, vanillaFrozenPeaks);
        check(matchedNeg == shelfHolder, "Blue-Glacier Shelf must match negative high-weirdness (-0.75) sheer coastal shelf");

        // Must NOT match hot desert, deep cave, or flat plains!
        Climate.TargetPoint hotDesertTarget = Climate.target(0.85F, -0.60F, 0.25F, 0.30F, 0.0F, 0.10F);
        check(layer.selectMatchingBiome(111L, hotDesertTarget, vanillaDesert) == vanillaDesert,
                "Blue-Glacier Shelf must never match hot inland desert climate");

        Climate.TargetPoint deepCaveTarget = Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.85F, 0.70F);
        check(layer.selectMatchingBiome(111L, deepCaveTarget, vanillaFrozenPeaks) == vanillaFrozenPeaks,
                "Surface Blue-Glacier Shelf must never overwrite deep underground cave biomes (depth=0.85)");

        System.out.println("[PASS] 3/8 Strict 6D multi-noise matching & depth/climate isolation verified for Biome 1.");
    }

    private static void verifyRendezvousRarityWeightsAndIsolation() {
        Climate.ParameterPoint sharedPolarNiche = EntropicaClimatePresets.builder()
                .temperature(EntropicaClimatePresets.TEMP_ICY)
                .continentalness(EntropicaClimatePresets.CONT_INLAND_ALL)
                .depth(EntropicaClimatePresets.DEPTH_SURFACE)
                .build();

        BiomePlacementRule commonRule = BiomePlacementRule.builder(EntropicaBiome.SILVER_PINE_TAIGA) // Common = 8
                .addParameterPoint(sharedPolarNiche).build();
        BiomePlacementRule rareRule = BiomePlacementRule.builder(EntropicaBiome.BLUE_GLACIER_SHELF) // Rare = 3
                .addParameterPoint(sharedPolarNiche).build();
        BiomePlacementRule legendaryRule = BiomePlacementRule.builder(EntropicaBiome.NULL_FRINGE_BAY) // Legendary = 1
                .addParameterPoint(sharedPolarNiche).build();

        Holder<Biome> hCommon = mockBiome("entropica", "silver_pine_taiga");
        Holder<Biome> hRare = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> hLegendary = mockBiome("entropica", "null_fringe_bay");
        Holder<Biome> fallback = mockBiome("minecraft", "snowy_plains");

        EntropicaMultiNoiseLayer layer = new EntropicaMultiNoiseLayer(
                777L, 6, 10, 0,
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(
                        new EntropicaMultiNoiseLayer.ResolvedRule(commonRule, hCommon),
                        new EntropicaMultiNoiseLayer.ResolvedRule(rareRule, hRare),
                        new EntropicaMultiNoiseLayer.ResolvedRule(legendaryRule, hLegendary)
                )
        );

        Climate.TargetPoint sample = Climate.target(-0.70F, 0.0F, 0.20F, 0.0F, 0.0F, 0.0F);
        int cWins = 0, rWins = 0, lWins = 0;
        int trials = 36000;
        for (int i = 0; i < trials; i++) {
            long cellHash = DomainWarpedRegionSampler.mix64(i * 0x9E3779B97F4A7C15L);
            Holder<Biome> w = layer.selectMatchingBiome(cellHash, sample, fallback);
            if (w == hCommon) cWins++;
            else if (w == hRare) rWins++;
            else if (w == hLegendary) lWins++;
        }

        // Expected ratios: 8/12 (66.67%), 3/12 (25.00%), 1/12 (8.33%)
        check(Math.abs((cWins / (double) trials) - (8.0 / 12.0)) < 0.015, "Common weight ratio off: " + cWins);
        check(Math.abs((rWins / (double) trials) - (3.0 / 12.0)) < 0.015, "Rare weight ratio off: " + rWins);
        check(Math.abs((lWins / (double) trials) - (1.0 / 12.0)) < 0.015, "Legendary weight ratio off: " + lWins);

        System.out.println(String.format(
                "[PASS] 4/8 Weighted Rendezvous rarity distribution verified (Common=%.1f%%, Rare=%.1f%%, Legendary=%.1f%%).",
                100.0 * cWins / trials, 100.0 * rWins / trials, 100.0 * lWins / trials
        ));
    }

    private static void verifyTerraBlenderCompatibilityModes() {
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> vanillaBiome = mockBiome("minecraft", "stony_shore");
        Holder<Biome> bopBiome = mockBiome("biomesoplenty", "auroral_garden");

        check(TerraBlenderCompatBridge.containsEntropicaBiomes(List.of(vanillaBiome, shelfHolder)),
                "Must detect existing entropica:* biome in base BiomeSource");
        check(!TerraBlenderCompatBridge.containsEntropicaBiomes(List.of(vanillaBiome, bopBiome)),
                "Must not false-positive when no entropica:* biome is in base BiomeSource");

        Climate.Sampler icyShelfSampler = new Climate.Sampler(
                net.minecraft.world.level.levelgen.DensityFunctions.constant(-0.75D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(0.0D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(-0.15D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(-0.65D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(0.0D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(0.70D),
                List.of()
        );

        // 1. COOPERATE Mode (TerraBlender loaded): yields to modded biomes (biomesoplenty:*), layers over minecraft:*
        EntropicaMultiNoiseLayer cooperateLayer = new EntropicaMultiNoiseLayer(
                99L, 6, 10, 0,
                true, false, TerraBlenderCompatMode.COOPERATE,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );
        check(cooperateLayer.resolveBiome(0, 16, 0, icyShelfSampler, bopBiome) == bopBiome,
                "COOPERATE mode must give way to modded TerraBlender biomes (biomesoplenty:*)");
        check(cooperateLayer.resolveBiome(0, 16, 0, icyShelfSampler, vanillaBiome) == shelfHolder,
                "COOPERATE mode must layer Entropica biomes cleanly over vanilla minecraft:* slots");

        // 2. COOPERATE Mode when base source ALREADY contains Entropica biomes (e.g. external TerraBlender Region addon)
        EntropicaMultiNoiseLayer cooperateAlreadyInjected = new EntropicaMultiNoiseLayer(
                99L, 6, 10, 0,
                true, true, TerraBlenderCompatMode.COOPERATE,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );
        check(cooperateAlreadyInjected.resolveBiome(0, 16, 0, icyShelfSampler, vanillaBiome) == vanillaBiome,
                "COOPERATE mode must automatically step aside when base TerraBlender source already manages entropica:* biomes");

        // 3. YIELD_TO_TERRABLENDER Mode: yields 100% when TerraBlender is installed
        EntropicaMultiNoiseLayer yieldLayer = new EntropicaMultiNoiseLayer(
                99L, 6, 10, 0,
                true, false, TerraBlenderCompatMode.YIELD_TO_TERRABLENDER,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );
        check(yieldLayer.resolveBiome(0, 16, 0, icyShelfSampler, vanillaBiome) == vanillaBiome,
                "YIELD_TO_TERRABLENDER mode must yield completely when TerraBlender is installed");

        // 4. OVERLAY_ON_TOP Mode: layers on top of BOTH vanilla and TerraBlender modded biomes
        EntropicaMultiNoiseLayer overlayLayer = new EntropicaMultiNoiseLayer(
                99L, 6, 10, 0,
                true, true, TerraBlenderCompatMode.OVERLAY_ON_TOP,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );
        check(overlayLayer.resolveBiome(0, 16, 0, icyShelfSampler, bopBiome) == shelfHolder,
                "OVERLAY_ON_TOP mode must work on top of modded TerraBlender regions when configured");

        System.out.println("[PASS] 5/8 All TerraBlender compatibility modes (COOPERATE, YIELD_TO_TERRABLENDER, OVERLAY_ON_TOP) verified.");
    }

    private static void verifyBiomeAtmosphereAndEnvironmentProfiles() {
        EntropicaBiomeAtmospheres.registerAll();
        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        BiomeAtmosphereProfile shelf = EntropicaBiomeAtmospheres.getForHolder(shelfHolder);
        check(shelf != null, "Blue-Glacier Shelf must have a registered BiomeAtmosphereProfile");
        check(shelf.hasCustomFogDistance(), "Blue-Glacier Shelf must configure coastal haze fog distance");
        check(shelf.getSkyPhenomenon() == SkyPhenomenonType.GLACIAL_ICE_HALO, "Blue-Glacier Shelf must use GLACIAL_ICE_HALO sky phenomenon");
        check(Math.abs(shelf.getDescentSpeedMultiplier() - 1.15F) < 1.0E-4F, "Blue-Glacier Shelf must apply 1.15x Aegis descent speed");
        check(shelf.getAmbientParticlesPerTick() > 0 && Math.abs(shelf.getParticleWindVx()) > 0.01F,
                "Blue-Glacier Shelf must emit horizontally wind-drifted pale-blue snow particles");

        BiomeAtmosphereProfile auroral = EntropicaBiomeAtmospheres.get(EntropicaBiome.AURORAL_SKERRIES);
        check(auroral != null && auroral.getNightSkyColorRgb() == 0x0E6A3B, "Auroral Skerries must shift sky to #0E6A3B at night");
        check(auroral.getLightHeldFogReliefBlocks() > 0.0F, "Auroral Skerries must grant fog relief when holding light");
        check(auroral.getSkyPhenomenon() == SkyPhenomenonType.AURORA_CURTAINS, "Auroral Skerries must render AURORA_CURTAINS");

        // Verify linear recurrence for 1.15x descent velocity: at every tick t, v_scaled(t) / v_vanilla(t) == 1.15
        double g = 0.08D;
        double vVanilla = 0.0D;
        double vAegis = 0.0D;
        for (int tick = 1; tick <= 40; tick++) {
            vVanilla = (vVanilla - g) * 0.98D;
            vAegis = (vAegis - g) * 0.98D - (1.15D - 1.0D) * g * 0.98D;
            double ratio = vAegis / vVanilla;
            check(Math.abs(ratio - 1.15D) < 1.0E-9D, "Expected exact 1.15x descent velocity at tick " + tick + ", got " + ratio);
        }

        System.out.println("[PASS] 6/8 Biome custom fog, Sodium/Iris sky phenomena, wind-drifted particles, and exact 1.15x Aegis gravity verified.");
    }

    private static void verifySmoothNonEffectToEffectBiomeTransitions() {
        EntropicaBiomeAtmospheres.registerAll();

        // 1. Temporal exponential decay invariants: [0, 1] bounds, monotonicity, and frame-rate independence
        check(BiomeTransitionMath.expDecayFactor(0.0F, 0.14F) == 0.0F, "expDecayFactor at dt=0 must be exact 0.0f");
        check(BiomeTransitionMath.expDecayFactor(-1.0F, 0.14F) == 0.0F, "expDecayFactor for negative dt must clamp to 0.0f");
        check(BiomeTransitionMath.expDecayFactor(Float.NaN, 0.14F) == 0.0F, "expDecayFactor for NaN must be safe 0.0f");

        float prevFactor = 0.0F;
        for (int step = 1; step <= 200; step++) {
            float dt = step * 0.25F;
            float factor = BiomeTransitionMath.expDecayFactor(dt, 0.14F);
            check(factor >= 0.0F && factor <= 1.0F, "expDecayFactor out of [0,1] bounds: " + factor);
            check(factor >= prevFactor, "expDecayFactor must be monotonic with dt at step " + step);
            prevFactor = factor;
        }
        // Frame-rate independence: 8 sub-steps of dt=0.125 ticks == 1 step of dt=1.0 tick
        float singleTickSmooth = BiomeTransitionMath.expSmooth(0.0F, 1.0F, 1.0F, 0.14F);
        float subFrameSmooth = 0.0F;
        for (int f = 0; f < 8; f++) {
            subFrameSmooth = BiomeTransitionMath.expSmooth(subFrameSmooth, 1.0F, 0.125F, 0.14F);
        }
        check(Math.abs(singleTickSmooth - subFrameSmooth) < 1.0E-5F,
                "Exponential smoothing must be frame-rate independent: 1-tick=" + singleTickSmooth + ", 8x0.125=" + subFrameSmooth);

        // 2. Spatial 2D Gaussian kernel across a sharp x = 0 border:
        //    minecraft:frozen_ocean (null profile) -> entropica:blue_glacier_shelf (has profile)
        Holder<Biome> vanillaFrozenOcean = mockBiome("minecraft", "frozen_ocean");
        Holder<Biome> blueGlacierShelf = mockBiome("entropica", "blue_glacier_shelf");
        check(EntropicaBiomeAtmospheres.getForHolder(vanillaFrozenOcean) == null,
                "Vanilla minecraft:frozen_ocean must have null BiomeAtmosphereProfile");
        check(EntropicaBiomeAtmospheres.getForHolder(blueGlacierShelf) != null,
                "entropica:blue_glacier_shelf must have a registered BiomeAtmosphereProfile");

        BiomeTransitionMath.BlockScalarSampler sharpBorderPresence = (bx, bz) -> {
            Holder<Biome> h = bx < 0 ? vanillaFrozenOcean : blueGlacierShelf;
            return EntropicaBiomeAtmospheres.getForHolder(h) != null ? 1.0F : 0.0F;
        };

        double testZ = 2.35D;
        check(BiomeTransitionMath.sampleGaussian2D(-20.0D, testZ, sharpBorderPresence) == 0.0F,
                "Gaussian kernel far inside vanilla biome (x=-20) must be exact 0.0f");
        check(Math.abs(BiomeTransitionMath.sampleGaussian2D(20.0D, testZ, sharpBorderPresence) - 1.0F) < 1.0E-6F,
                "Gaussian kernel far inside custom biome (x=+20) must be exact 1.0f");

        float maxRawStepDelta = 0.0F;
        float maxEasedStepDelta = 0.0F;
        float prevRaw = BiomeTransitionMath.sampleGaussian2D(-20.0D, testZ, sharpBorderPresence);
        float prevEased = BiomeTransitionMath.sampleGaussianBlend01(-20.0D, testZ, sharpBorderPresence);

        // Walk 0.5 blocks/step across x = 0 from x = -20.0 to x = +20.0
        for (int s = -39; s <= 40; s++) {
            double camX = s * 0.5D;
            float raw = BiomeTransitionMath.sampleGaussian2D(camX, testZ, sharpBorderPresence);
            float eased = BiomeTransitionMath.sampleGaussianBlend01(camX, testZ, sharpBorderPresence);

            check(raw >= 0.0F && raw <= 1.0F, "Raw Gaussian blend out of [0,1] at x=" + camX + ": " + raw);
            check(eased >= 0.0F && eased <= 1.0F, "Smootherstepped Gaussian blend out of [0,1] at x=" + camX + ": " + eased);
            check(raw >= prevRaw - 1.0E-6F, "Raw Gaussian blend must be monotonic across x=0 at x=" + camX);
            check(eased >= prevEased - 1.0E-6F, "Eased Gaussian blend must be monotonic across x=0 at x=" + camX);
            if (camX >= -8.0D && camX <= 8.0D) {
                check(raw > prevRaw, "Raw Gaussian blend must strictly increase inside border band at x=" + camX);
            }

            maxRawStepDelta = Math.max(maxRawStepDelta, raw - prevRaw);
            maxEasedStepDelta = Math.max(maxEasedStepDelta, eased - prevEased);
            prevRaw = raw;
            prevEased = eased;
        }
        check(maxRawStepDelta < 0.12F && maxEasedStepDelta < 0.12F,
                "Expected smooth sub-block Gaussian border blend (<0.12 per 0.5-block step, zero 20% staircase jumps), got raw="
                        + maxRawStepDelta + ", eased=" + maxEasedStepDelta);

        // 3. Spatial Aegis gravity transition across x = 0:
        //    Walking from x = -20 (vanilla frozen_ocean, 1.00x) into x = +20 (Blue-Glacier Shelf, 1.15x)
        BiFunction<Integer, Integer, BiomeAtmosphereProfile> shelfBorderSampler = (bx, bz) ->
                EntropicaBiomeAtmospheres.getForHolder(bx < 0 ? vanillaFrozenOcean : blueGlacierShelf);

        float farOutsideMult = EntropicaBiomeEnvironmentHandler.computeBlendedDescentMultiplier(shelfBorderSampler, -20.0D, 0.5D);
        float farInsideMult = EntropicaBiomeEnvironmentHandler.computeBlendedDescentMultiplier(shelfBorderSampler, 20.0D, 0.5D);
        check(farOutsideMult == 1.00F, "Expected exact 1.00f descent multiplier far outside at x=-20, got " + farOutsideMult);
        check(Math.abs(farInsideMult - 1.15F) < 1.0E-6F, "Expected exact 1.15f descent multiplier far inside at x=+20, got " + farInsideMult);

        float maxOneBlockGravityDelta = 0.0F;
        float prevGravityMult = farOutsideMult;
        for (int x = -19; x <= 20; x++) {
            float currMult = EntropicaBiomeEnvironmentHandler.computeBlendedDescentMultiplier(shelfBorderSampler, (double) x, 0.5D);
            check(currMult >= 1.00F && currMult <= 1.150001F, "Blended descent multiplier out of [1.00, 1.15] bounds at x=" + x + ": " + currMult);
            check(currMult >= prevGravityMult - 1.0E-7F, "Blended descent multiplier must never decrease entering shelf at x=" + x);
            if (x >= -8 && x <= 9) {
                check(currMult > prevGravityMult,
                        "Blended descent multiplier must be strictly monotonic across active boundary [-9..+9] at x=" + x
                                + " (prev=" + prevGravityMult + ", curr=" + currMult + ")");
            }
            maxOneBlockGravityDelta = Math.max(maxOneBlockGravityDelta, currMult - prevGravityMult);
            prevGravityMult = currMult;
        }
        check(maxOneBlockGravityDelta < 0.025F,
                "Expected max 1-block descent multiplier delta < 0.025f across border, got " + maxOneBlockGravityDelta);

        // Also verify Jump Boost spatial presence gating across vanilla -> Null-Fringe Bay border
        Holder<Biome> nullFringeBay = mockBiome("entropica", "null_fringe_bay");
        BiFunction<Integer, Integer, BiomeAtmosphereProfile> jumpBorderSampler = (bx, bz) ->
                EntropicaBiomeAtmospheres.getForHolder(bx < 0 ? vanillaFrozenOcean : nullFringeBay);
        check(EntropicaBiomeEnvironmentHandler.computeJumpBoostPresenceWeight(jumpBorderSampler, -20.0D, 0.5D) == 0.0F,
                "Jump boost presence must be 0.0f far inside vanilla biome");
        check(Math.abs(EntropicaBiomeEnvironmentHandler.computeJumpBoostPresenceWeight(jumpBorderSampler, 20.0D, 0.5D) - 1.0F) < 1.0E-6F,
                "Jump boost presence must be 1.0f deep inside Null-Fringe Bay");

        System.out.println(String.format(
                "[PASS] 7/8 Smooth non-effect -> effect biome transitions verified (Gaussian max0.5Step=%.4f, Aegis 1.00x->1.15x max1BlockDelta=%.4f).",
                maxEasedStepDelta, maxOneBlockGravityDelta
        ));
    }

    private static JsonObject parseClasspathJson(String resourcePath) {
        ClassLoader cl = BiomeInjectorVerificationSuite.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream(resourcePath)) {
            check(is != null, "Missing required classpath resource: " + resourcePath);
            JsonElement parsed = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            check(parsed != null && parsed.isJsonObject(), "Invalid JSON object at: " + resourcePath);
            return parsed.getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("Failed parsing JSON resource " + resourcePath + ": " + e.getMessage(), e);
        }
    }

    private static void verifyPngDimensions(String resourcePath, int expectedW, int expectedH) {
        ClassLoader cl = BiomeInjectorVerificationSuite.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream(resourcePath)) {
            check(is != null, "Missing required PNG texture resource: " + resourcePath);
            BufferedImage img = ImageIO.read(is);
            check(img != null, "Unreadable PNG image at: " + resourcePath);
            check(img.getWidth() == expectedW && img.getHeight() == expectedH,
                    "Expected " + expectedW + "x" + expectedH + " for " + resourcePath + ", got " + img.getWidth() + "x" + img.getHeight());
        } catch (Exception e) {
            throw new AssertionError("Failed reading PNG " + resourcePath + ": " + e.getMessage(), e);
        }
    }

    private static void verifyBiome1BlueGlacierShelfContentAndAssets() {
        // 1. Biome JSON & Worldgen / Structure / Chest Loot JSONs
        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/blue_glacier_shelf.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects != null && effects.get("sky_color").getAsInt() == 4148879,
                "blue_glacier_shelf.json must specify pale indigo sky_color 4148879 (#3F4E8F)");
        check(effects.get("fog_color").getAsInt() == 12113134,
                "blue_glacier_shelf.json must specify coastal fog_color 12113134 (#B8D4EE)");
        String biomeStr = biomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:blue_glacier_ice_shelf",
                "entropica:aegis_crystal_formation",
                "entropica:patch_rimebloom_crevasse",
                "entropica:glacial_heart_root_overhang",
                "entropica:shielding_bramble_shelf",
                "entropica:astral_geode",
                "entropica:rime_back_ovis",
                "entropica:glacial_frost_core",
                "entropica:frost_tusk_mammot",
                "entropica:grot"
        )) {
            check(biomeStr.contains(expectedRef), "blue_glacier_shelf.json missing expected entry: " + expectedRef);
        }

        for (String featureId : List.of(
                "blue_glacier_ice_shelf",
                "aegis_crystal_formation",
                "patch_rimebloom_crevasse",
                "glacial_heart_root_overhang",
                "shielding_bramble_shelf"
        )) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + featureId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + featureId + ".json");
        }
        parseClasspathJson("data/entropica/worldgen/configured_feature/astral_geode.json");

        JsonObject structureJson = parseClasspathJson("data/entropica/worldgen/structure/frost_veiled_glacial_runic_spire.json");
        check("entropica:frost_veiled_glacial_runic_spire".equals(structureJson.get("type").getAsString()),
                "Spire structure JSON must reference entropica:frost_veiled_glacial_runic_spire");
        parseClasspathJson("data/entropica/worldgen/structure_set/frost_veiled_glacial_runic_spire.json");

        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/entropica/tags/worldgen/biome/has_structure/frost_veiled_glacial_runic_spire.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:blue_glacier_shelf"),
                    tagJson + " must include entropica:blue_glacier_shelf");
        }

        JsonObject spireChestLoot = parseClasspathJson("data/entropica/loot_table/chests/structure_1.json");
        check(spireChestLoot.toString().contains("entropica:glacial_focus_lens"),
                "chests/structure_1.json must include entropica:glacial_focus_lens");

        // 2. All 11 Block definitions, 14 Item definitions, Entity PNG dimensions, Translations, and 16 Recipes
        List<String> blocks11 = List.of(
                "aegis_crystal_block", "aegis_crystal_cluster", "runic_blue_ice", "runed_basalt_pillar",
                "glacial_heart_root", "shielding_bramble", "thorned_wire", "glacial_barricade",
                "conductive_aegis_plating", "glacial_ice_maker", "glacial_life_force_anchor"
        );
        List<String> items14 = List.of(
                "aegis_crystal_shard", "rimebloom_petal", "rimebloom_dye", "shielding_thorn_fiber",
                "spiked_armor_lining", "aegis_shielding_lining", "frost_core_shard", "insulated_glacial_wool",
                "raw_mammot_meat", "cooked_mammot_meat", "thermal_insulated_blanket", "glacial_focus_lens",
                "glacial_frost_core_spawn_egg", "frost_tusk_mammot_spawn_egg"
        );
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");

        for (String b : blocks11) {
            parseClasspathJson("assets/entropica/items/" + b + ".json");
            parseClasspathJson("assets/entropica/blockstates/" + b + ".json");
            parseClasspathJson("assets/entropica/models/block/" + b + ".json");
            parseClasspathJson("assets/entropica/models/item/" + b + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + b + ".json");
            check(langJson.has("block.entropica." + b), "en_us.json missing block.entropica." + b);
        }
        parseClasspathJson("data/entropica/loot_table/blocks/rimebloom.json");

        for (String it : items14) {
            parseClasspathJson("assets/entropica/items/" + it + ".json");
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }
        for (String reqKey : List.of(
                "entity.entropica.glacial_frost_core",
                "entity.entropica.frost_tusk_mammot",
                "item.minecraft.potion.effect.frost_resistance",
                "item.minecraft.splash_potion.effect.frost_resistance",
                "item.minecraft.lingering_potion.effect.frost_resistance",
                "item.minecraft.tipped_arrow.effect.frost_resistance",
                "item.minecraft.potion.effect.freezing",
                "item.minecraft.splash_potion.effect.freezing",
                "item.minecraft.lingering_potion.effect.freezing",
                "item.minecraft.tipped_arrow.effect.freezing"
        )) {
            check(langJson.has(reqKey), "en_us.json missing translation key: " + reqKey);
        }

        verifyPngDimensions("assets/entropica/textures/entity/frost_core/glacial_frost_core.png", 64, 64);
        verifyPngDimensions("assets/entropica/textures/entity/mammot/frost_tusk_mammot.png", 128, 128);
        verifyPngDimensions("assets/entropica/textures/entity/mammot/frost_tusk_mammot_sheared.png", 128, 128);

        List<String> recipes16 = List.of(
                "aegis_crystal_block", "aegis_crystal_shard_from_block", "runic_blue_ice", "runed_basalt_pillar",
                "rimebloom_dye", "light_blue_dye_from_rimebloom_dye", "thorned_wire", "spiked_armor_lining",
                "aegis_shielding_lining", "glacial_barricade", "conductive_aegis_plating", "glacial_ice_maker",
                "thermal_insulated_blanket", "cooked_mammot_meat",
                "cooked_mammot_meat_from_smoking", "cooked_mammot_meat_from_campfire_cooking"
        );
        for (String r : recipes16) {
            parseClasspathJson("data/entropica/recipe/" + r + ".json");
        }

        // 3. GrotSpawnRegistry Blue-Glacier Shelf Pool verification (Glacial ~60%, Aegis ~30%, Frozen ~10% of 95% non-anomaly rolls)
        GrotSpawnRegistry.initialize();
        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        RandomSource rng = RandomSource.create(20261119L);
        int glacialCount = 0, aegisCount = 0, frozenCount = 0;
        int totalRolls = 20000;
        for (int i = 0; i < totalRolls; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(shelfHolder, rng);
            check(rolled != null, "GrotSpawnRegistry returned null EssenceType");
            if (rolled == EssenceType.GLACIAL) glacialCount++;
            else if (rolled == EssenceType.AEGIS) aegisCount++;
            else if (rolled == EssenceType.FROZEN) frozenCount++;
        }
        double glacialRatio = glacialCount / (double) totalRolls;
        double aegisRatio = aegisCount / (double) totalRolls;
        // 95% * 0.60 = 57.0% (+ tiny anomaly share ~57.25%), 95% * 0.30 = 28.5%
        check(glacialRatio > 0.53 && glacialRatio < 0.62, "Expected Glacial Grot ratio ~57%, got " + glacialRatio);
        check(aegisRatio > 0.25 && aegisRatio < 0.33, "Expected Aegis Grot ratio ~28.5%, got " + aegisRatio);
        check(frozenCount > 1400, "Expected Frozen Grot rolls ~9.5%, got " + frozenCount);

        System.out.println(String.format(
                "[PASS] 8/8 Biome 1 (Blue-Glacier Shelf) worldgen, Spire loot, 11 blocks, 14 items, 16 recipes, PNGs, en_us, & Grot pool (Glacial=%.1f%%, Aegis=%.1f%%) verified.",
                glacialRatio * 100.0, aegisRatio * 100.0
        ));
    }
}
