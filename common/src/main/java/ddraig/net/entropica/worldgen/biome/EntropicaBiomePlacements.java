package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.config.EntropicaBiome;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Central registration table for multi-noise climate rules and surface rules
 * across Entropica's 108 planned biomes.
 *
 * <p>Rules registered here automatically check both {@link EntropicaBiome#isEnabled()}
 * and live {@code Registry<Biome>} datapack presence before activating in a world,
 * so rules can be registered safely ahead of or alongside biome JSON creation.</p>
 */
public final class EntropicaBiomePlacements {
    private static final Map<EntropicaBiome, BiomePlacementRule> RULES = new EnumMap<>(EntropicaBiome.class);
    private static boolean initialized = false;

    private EntropicaBiomePlacements() {}

    public static synchronized void registerAll() {
        if (initialized) {
            return;
        }
        initialized = true;

        // =========================================================================
        // BIOME #1 — 2.1 Blue-Glacier Shelf (Overworld | Glacial + Aegis | Rare)
        // Spec: Very High coastal-shelf transition, Low Erosion (sheer vertical cliffs),
        //       High Weirdness (stepped ice terraces), Frozen/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.BLUE_GLACIER_SHELF)
                .dimension(LevelStem.OVERWORLD)
                // Positive high-weirdness stepped coastal ice cliffs
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(EntropicaClimatePresets.CONT_COASTAL_SHELF)
                        .erosion(EntropicaClimatePresets.EROSION_SHEER_CLIFFS)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_POSITIVE)
                        .build())
                // Negative high-weirdness stepped coastal ice cliffs
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(EntropicaClimatePresets.CONT_COASTAL_SHELF)
                        .erosion(EntropicaClimatePresets.EROSION_SHEER_CLIFFS)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_NEGATIVE)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource blueIce = SurfaceRules.state(Blocks.BLUE_ICE.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());
                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.ifTrue(
                                                    SurfaceRules.waterBlockCheck(-1, 0),
                                                    blueIce
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.steep(),
                                            blueIce
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.ifTrue(
                                                    SurfaceRules.yBlockCheck(VerticalAnchor.absolute(58), 0),
                                                    blueIce
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            packedIce
                                    )
                            )
                    );
                })
                .build());
    }

    public static synchronized void register(BiomePlacementRule rule) {
        RULES.put(rule.getBiome(), rule);
    }

    public static Map<EntropicaBiome, BiomePlacementRule> allRules() {
        registerAll();
        return Collections.unmodifiableMap(RULES);
    }

    public static List<BiomePlacementRule> rulesForDimension(ResourceKey<LevelStem> stemKey) {
        registerAll();
        List<BiomePlacementRule> list = new ArrayList<>();
        for (BiomePlacementRule rule : RULES.values()) {
            if (rule.getDimensionStem().equals(stemKey)) {
                list.add(rule);
            }
        }
        return list;
    }

    /**
     * Combines all biome-scoped surface rules for a given dimension stem.
     *
     * @param filterByConfig if true, includes only rules whose biome is currently enabled in EntropicaConfig.
     */
    public static SurfaceRules.RuleSource buildCombinedSurfaceRule(ResourceKey<LevelStem> stemKey, boolean filterByConfig) {
        registerAll();
        if (filterByConfig && !ddraig.net.entropica.config.EntropicaConfig.ENABLE_ALL_BIOMES.get()) {
            return null;
        }
        List<SurfaceRules.RuleSource> rules = new ArrayList<>();
        for (BiomePlacementRule rule : RULES.values()) {
            if (rule.getDimensionStem().equals(stemKey) && rule.getSurfaceRule() != null) {
                if (!filterByConfig || rule.getBiome().isEnabled()) {
                    rules.add(rule.getSurfaceRule());
                }
            }
        }
        if (rules.isEmpty()) {
            return null;
        }
        if (rules.size() == 1) {
            return rules.get(0);
        }
        return SurfaceRules.sequence(rules.toArray(new SurfaceRules.RuleSource[0]));
    }
}
