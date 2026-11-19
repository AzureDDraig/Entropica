package ddraig.net.entropica.worldgen.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.mixin.BiomeSourceAccessor;
import ddraig.net.entropica.mixin.ChunkGeneratorAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Orchestrates Entropica's native multi-noise biome & surface rule injection across all
 * server {@link LevelStem}s at world startup (strictly after TerraBlender's ServerAboutToStart
 * initialization and strictly before {@code ServerLevel} / {@code ChunkGeneratorStructureState} construction).
 */
public final class EntropicaBiomeInjector {
    private EntropicaBiomeInjector() {}

    public static void injectServerLevels(MinecraftServer server) {
        EntropicaBiomePlacements.registerAll();

        RegistryAccess.Frozen registryAccess = server.registries().compositeAccess();
        Registry<LevelStem> stemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        Registry<Biome> biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);

        long worldSeed = server.getWorldData().worldGenOptions().seed();
        boolean masterEnabled = EntropicaConfig.ENABLE_ALL_BIOMES.get();
        boolean terraBlenderLoaded = TerraBlenderCompatBridge.isTerraBlenderLoaded();
        TerraBlenderCompatMode compatMode = TerraBlenderCompatMode.current();
        int entropicaWeight = EntropicaConfig.ENTROPICA_BIOME_REGION_WEIGHT.get();
        int passthroughWeight = EntropicaConfig.VANILLA_BIOME_PASSTHROUGH_WEIGHT.get();
        int regionScale = EntropicaConfig.BIOME_REGION_SCALE.get();

        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : stemRegistry.entrySet()) {
            ResourceKey<LevelStem> stemKey = entry.getKey();
            LevelStem stem = entry.getValue();
            ChunkGenerator generator = stem.generator();
            BiomeSource biomeSource = generator.getBiomeSource();

            if (!(biomeSource instanceof MultiNoiseBiomeSource multiNoiseSource)) {
                continue;
            }
            if (!(multiNoiseSource instanceof EntropicaBiomeSourceExtension ext)) {
                continue;
            }

            // Always reset layer first for idempotency across singleplayer world switches
            ext.entropica$setMultiNoiseLayer(null);
            clearCustomSurfaceRuleIfPresent(generator);

            if (!masterEnabled) {
                refreshPossibleBiomesAndFeatures(generator, multiNoiseSource, List.of());
                continue;
            }

            List<BiomePlacementRule> candidateRules = EntropicaBiomePlacements.rulesForDimension(stemKey);
            if (candidateRules.isEmpty()) {
                continue;
            }

            List<EntropicaMultiNoiseLayer.ResolvedRule> resolved = new ArrayList<>();
            List<SurfaceRules.RuleSource> activeSurfaceRules = new ArrayList<>();

            for (BiomePlacementRule rule : candidateRules) {
                if (!rule.getBiome().isEnabled()) {
                    continue;
                }
                Optional<Holder.Reference<Biome>> holderOpt = biomeRegistry.get(rule.getBiome().resourceKey());
                if (holderOpt.isEmpty()) {
                    Entropica.LOGGER.debug(
                            "[Entropica Worldgen] Skipping biome {} ({}) — biome JSON not yet present in registry.",
                            rule.getBiome().getId(), stemKey.location()
                    );
                    continue;
                }
                resolved.add(new EntropicaMultiNoiseLayer.ResolvedRule(rule, holderOpt.get()));
                if (rule.getSurfaceRule() != null) {
                    activeSurfaceRules.add(rule.getSurfaceRule());
                }
            }

            if (resolved.isEmpty()) {
                refreshPossibleBiomesAndFeatures(generator, multiNoiseSource, List.of());
                continue;
            }

            // Inspect base possibleBiomes BEFORE attaching our layer so we can detect if an external
            // TerraBlender region/datapack already placed Entropica biomes into the base source.
            BiomeSourceAccessor bsAccessor = (BiomeSourceAccessor) multiNoiseSource;
            Supplier<Set<Holder<Biome>>> baseBiomesSupplier =
                    ext.entropica$getOrCaptureOriginalPossibleBiomes(bsAccessor.entropica$getPossibleBiomesSupplier());
            Set<Holder<Biome>> existingBiomes = baseBiomesSupplier.get();
            boolean baseAlreadyHasEntropica = TerraBlenderCompatBridge.containsEntropicaBiomes(existingBiomes);

            EntropicaMultiNoiseLayer layer = new EntropicaMultiNoiseLayer(
                    worldSeed,
                    regionScale,
                    entropicaWeight,
                    passthroughWeight,
                    terraBlenderLoaded,
                    baseAlreadyHasEntropica,
                    compatMode,
                    resolved
            );

            ext.entropica$setMultiNoiseLayer(layer);
            refreshPossibleBiomesAndFeatures(generator, multiNoiseSource, layer.activeBiomes());

            // Attach native surface rules unless already delegated to TerraBlender's SurfaceRuleManager
            if (!activeSurfaceRules.isEmpty()
                    && !TerraBlenderCompatBridge.isSurfaceRuleDelegatedToTerraBlender(stemKey)) {
                SurfaceRules.RuleSource combinedSurface = activeSurfaceRules.size() == 1
                        ? activeSurfaceRules.get(0)
                        : SurfaceRules.sequence(activeSurfaceRules.toArray(new SurfaceRules.RuleSource[0]));
                attachCustomSurfaceRule(generator, combinedSurface);
            }

            Entropica.LOGGER.info(
                    "[Entropica Worldgen] Activated {} Entropica multi-noise biome(s) in {} (TerraBlender={}, Mode={}, BaseHasEntropica={}).",
                    resolved.size(),
                    stemKey.location(),
                    terraBlenderLoaded,
                    compatMode,
                    baseAlreadyHasEntropica
            );
        }
    }

    private static void refreshPossibleBiomesAndFeatures(
            ChunkGenerator generator,
            MultiNoiseBiomeSource multiNoiseSource,
            List<Holder<Biome>> injectedBiomes) {
        BiomeSourceAccessor bsAccessor = (BiomeSourceAccessor) multiNoiseSource;
        EntropicaBiomeSourceExtension ext = (EntropicaBiomeSourceExtension) multiNoiseSource;
        Supplier<Set<Holder<Biome>>> originalSupplier =
                ext.entropica$getOrCaptureOriginalPossibleBiomes(bsAccessor.entropica$getPossibleBiomesSupplier());

        Supplier<Set<Holder<Biome>>> mergedBiomes = Suppliers.memoize(() -> {
            Set<Holder<Biome>> base = originalSupplier.get();
            if (injectedBiomes.isEmpty()) {
                return base;
            }
            return ImmutableSet.<Holder<Biome>>builder()
                    .addAll(base)
                    .addAll(injectedBiomes)
                    .build();
        });
        bsAccessor.entropica$setPossibleBiomesSupplier(mergedBiomes);

        ChunkGeneratorAccessor cgAccessor = (ChunkGeneratorAccessor) generator;
        Function<Holder<Biome>, BiomeGenerationSettings> settingsGetter = cgAccessor.entropica$getGenerationSettingsGetter();
        Supplier<List<FeatureSorter.StepFeatureData>> rebuiltFeatures = Suppliers.memoize(() ->
                FeatureSorter.buildFeaturesPerStep(
                        List.copyOf(multiNoiseSource.possibleBiomes()),
                        biome -> settingsGetter.apply(biome).features(),
                        true
                )
        );
        cgAccessor.entropica$setFeaturesPerStep(rebuiltFeatures);
    }

    private static void clearCustomSurfaceRuleIfPresent(ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGenerator noiseGen) {
            NoiseGeneratorSettings settings = noiseGen.generatorSettings().value();
            EntropicaSurfaceRuleStore.setCustomSurfaceRule(settings, null);
        }
    }

    private static void attachCustomSurfaceRule(ChunkGenerator generator, SurfaceRules.RuleSource rule) {
        if (generator instanceof NoiseBasedChunkGenerator noiseGen) {
            NoiseGeneratorSettings settings = noiseGen.generatorSettings().value();
            EntropicaSurfaceRuleStore.setCustomSurfaceRule(settings, rule);
        }
    }
}
