package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.config.EntropicaBiome;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Central registration table for multi-noise climate rules and surface rules
 * across Entropica's 108 planned biomes.
 *
 * <p>Rules registered here automatically check both {@link EntropicaBiome#isEnabled()}
 * and live {@code Registry<Biome>} datapack presence before activating in a world,
 * so rules can be registered safely ahead of or alongside biome JSON creation.</p>
 */
public final class EntropicaBiomePlacements {
    public static final ResourceKey<Biome> BLUE_GLACIER_SHELF = key("blue_glacier_shelf");
    public static final ResourceKey<Biome> AURORAL_SKERRIES = key("auroral_skerries");
    public static final ResourceKey<Biome> FROST_BITE_FLOES = key("frost_bite_floes");
    public static final ResourceKey<Biome> CRYSTALLINE_PACK_ICE = key("crystalline_pack_ice");
    public static final ResourceKey<Biome> SILENT_FJORD = key("silent_fjord");
    public static final ResourceKey<Biome> STEAMING_SKERRIES = key("steaming_skerries");
    public static final ResourceKey<Biome> STATIC_ICE_PLAINS = key("static_ice_plains");
    public static final ResourceKey<Biome> BARROW_MOUND_TUNDRA = key("barrow_mound_tundra");
    public static final ResourceKey<Biome> NULL_FRINGE_BAY = key("null_fringe_bay");
    public static final ResourceKey<Biome> RADIANT_GLAZE_PLAINS = key("radiant_glaze_plains");
    public static final ResourceKey<Biome> STORM_SWEPT_ICEBERGS = key("storm_swept_icebergs");
    public static final ResourceKey<Biome> SPORE_GLAZED_PACK = key("spore_glazed_pack");
    public static final ResourceKey<Biome> MIRAGE_GLACIER = key("mirage_glacier");
    public static final ResourceKey<Biome> OASIS_FLOES = key("oasis_floes");
    public static final ResourceKey<Biome> DUST_CRUSTED_BERGS = key("dust_crusted_bergs");
    public static final ResourceKey<Biome> BLOOD_ICE_SHALLOWS = key("blood_ice_shallows");
    public static final ResourceKey<Biome> ASTRAL_SKERRIES = key("astral_skerries");

    public static final List<ResourceKey<Biome>> ALL_ENTROPICA_BIOMES = List.of(
            BLUE_GLACIER_SHELF,
            AURORAL_SKERRIES,
            FROST_BITE_FLOES,
            CRYSTALLINE_PACK_ICE,
            SILENT_FJORD,
            STEAMING_SKERRIES,
            STATIC_ICE_PLAINS,
            BARROW_MOUND_TUNDRA,
            NULL_FRINGE_BAY,
            RADIANT_GLAZE_PLAINS,
            STORM_SWEPT_ICEBERGS,
            SPORE_GLAZED_PACK,
            MIRAGE_GLACIER,
            OASIS_FLOES,
            DUST_CRUSTED_BERGS,
            BLOOD_ICE_SHALLOWS,
            ASTRAL_SKERRIES
    );

    private static final Map<EntropicaBiome, BiomePlacementRule> RULES = new EnumMap<>(EntropicaBiome.class);
    private static boolean initialized = false;

    private EntropicaBiomePlacements() {}

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path));
    }

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

        // =========================================================================
        // BIOME #2 — 2.2 Auroral Skerries (Overworld | Aurora + Vapor | Uncommon)
        // Spec: Low coastal island clusters (-0.32..-0.08), High flat ice-worn islets (0.25..1.0),
        //       Medium fragmented coastline weirdness (+/-0.05..0.40), Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.AURORAL_SKERRIES)
                .dimension(LevelStem.OVERWORLD)
                // Positive medium-weirdness fragmented coastal archipelago islets
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.32F, -0.08F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_MID_POSITIVE)
                        .build())
                // Negative medium-weirdness fragmented coastal archipelago islets
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.32F, -0.08F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_MID_NEGATIVE)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource coarseGravel = SurfaceRules.state(
                            resolveRegisteredBlockState("coarse_gravel", () -> ModBlocks.COARSE_GRAVEL.get(), Blocks.GRAVEL));
                    SurfaceRules.RuleSource glimmerIce = SurfaceRules.state(
                            resolveRegisteredBlockState("glimmer_ice", () -> ModBlocks.GLIMMER_ICE.get(), Blocks.PACKED_ICE));
                    SurfaceRules.RuleSource basaltSubfloor = SurfaceRules.state(Blocks.BASALT.defaultBlockState());

                    SurfaceRules.ConditionSource aboveChannelBed = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(58), 0);
                    SurfaceRules.ConditionSource belowHighGround = SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(69), 0));
                    SurfaceRules.ConditionSource shorelineBand = SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(66), 0));

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.ifTrue(
                                    aboveChannelBed,
                                    SurfaceRules.ifTrue(
                                            belowHighGround,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.ON_FLOOR,
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(
                                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                                            SurfaceRules.ifTrue(
                                                                                    shorelineBand,
                                                                                    SurfaceRules.ifTrue(
                                                                                            SurfaceRules.noiseCondition(Noises.ICE, 0.35D, 1.0D),
                                                                                            glimmerIce
                                                                                    )
                                                                            )
                                                                    ),
                                                                    coarseGravel
                                                            )
                                                    ),
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.UNDER_FLOOR,
                                                            coarseGravel
                                                    ),
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                                            basaltSubfloor
                                                    )
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #3 — 2.3 Frost-Bite Floes (Overworld | Glacial + Blight | Rare)
        // Spec: Low coastal swamp/floe transition (-0.28..-0.06), Very High flat sea-level
        //       erosion (0.45..1.0), High blocky pack-ice weirdness (+/-0.40..1.0),
        //       Wet/Humid coastal swamp humidity (0.10..1.0), Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.FROST_BITE_FLOES)
                .dimension(LevelStem.OVERWORLD)
                // Positive high-weirdness blocky pack-ice floe sheets
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(0.10F, 1.0F)
                        .continentalness(-0.28F, -0.06F)
                        .erosion(EntropicaClimatePresets.EROSION_FLATS)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_POSITIVE)
                        .build())
                // Negative high-weirdness blocky pack-ice floe sheets
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(0.10F, 1.0F)
                        .continentalness(-0.28F, -0.06F)
                        .erosion(EntropicaClimatePresets.EROSION_FLATS)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_NEGATIVE)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource blightedPackedIce = SurfaceRules.state(
                            resolveBlock("blighted_packed_ice", Blocks.PACKED_ICE).defaultBlockState());
                    SurfaceRules.RuleSource rottenIce = SurfaceRules.state(
                            resolveBlock("rotten_ice", Blocks.ICE).defaultBlockState());
                    SurfaceRules.RuleSource mud = SurfaceRules.state(Blocks.MUD.defaultBlockState());
                    SurfaceRules.RuleSource clay = SurfaceRules.state(Blocks.CLAY.defaultBlockState());

                    SurfaceRules.ConditionSource atOrAbove61 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(61), 0);
                    SurfaceRules.ConditionSource below67 = SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(67), 0));
                    SurfaceRules.ConditionSource below61 = SurfaceRules.not(atOrAbove61);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            atOrAbove61,
                                            SurfaceRules.ifTrue(
                                                    below67,
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(
                                                                    SurfaceRules.ON_FLOOR,
                                                                    SurfaceRules.sequence(
                                                                            SurfaceRules.ifTrue(
                                                                                    SurfaceRules.noiseCondition(Noises.ICE, 0.25D, 1.0D),
                                                                                    rottenIce
                                                                            ),
                                                                            blightedPackedIce
                                                                    )
                                                            ),
                                                            SurfaceRules.ifTrue(
                                                                    SurfaceRules.UNDER_FLOOR,
                                                                    blightedPackedIce
                                                            )
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            below61,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.ON_FLOOR,
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(
                                                                            SurfaceRules.noiseCondition(Noises.ICE, 0.20D, 1.0D),
                                                                            clay
                                                                    ),
                                                                    mud
                                                            )
                                                    ),
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.UNDER_FLOOR,
                                                            mud
                                                    )
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #4 — 2.4 Crystalline Pack-Ice (Overworld | Aegis + Aurora | Rare)
        // Spec: High inland mountain valleys (0.09..0.88, strictly > 0.08 so 100% disjoint
        //       from Biomes 1, 2, and 3), Medium rolling snowfields (-0.35..0.15),
        //       High crystalline-spire weirdness (+/-0.40..1.0), Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.CRYSTALLINE_PACK_ICE)
                .dimension(LevelStem.OVERWORLD)
                // Positive high-weirdness inland crystalline snowfield valleys
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 0.88F)
                        .erosion(-0.35F, 0.15F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_POSITIVE)
                        .build())
                // Negative high-weirdness inland crystalline snowfield valleys
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 0.88F)
                        .erosion(-0.35F, 0.15F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_NEGATIVE)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource snowBlock = SurfaceRules.state(Blocks.SNOW_BLOCK.defaultBlockState());
                    SurfaceRules.RuleSource blueIce = SurfaceRules.state(Blocks.BLUE_ICE.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());
                    SurfaceRules.ConditionSource blueIceBand = SurfaceRules.noiseCondition(Noises.ICE, 0.30D, 1.0D);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.ifTrue(
                                                    SurfaceRules.waterBlockCheck(-1, 0),
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(blueIceBand, blueIce),
                                                            snowBlock
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(blueIceBand, blueIce),
                                                    packedIce
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

        // =========================================================================
        // BIOME #5 — 2.5 Silent Fjord (Overworld | Eclipse + Glacial | Rare)
        // Spec: Inland glacial fjord chasms (0.09..0.45, strictly > 0.08 so 100% disjoint
        //       from coastal Biomes 1–3), Sheer vertical basalt cliffs (-1.0..-0.375,
        //       100% disjoint from Biome 4's -0.35..0.15), High weirdness (+/-0.40..1.0),
        //       Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.SILENT_FJORD)
                .dimension(LevelStem.OVERWORLD)
                // Positive high-weirdness sheer basalt fjord cliffs
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 0.45F)
                        .erosion(EntropicaClimatePresets.EROSION_SHEER_CLIFFS)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_POSITIVE)
                        .build())
                // Negative high-weirdness sheer basalt fjord cliffs
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 0.45F)
                        .erosion(EntropicaClimatePresets.EROSION_SHEER_CLIFFS)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_NEGATIVE)
                        .build())
                .surfaceRule(() -> {
                    BlockState blueFrozenMoss = resolveBlock("blue_frozen_moss", Blocks.MOSS_BLOCK).defaultBlockState();
                    BlockState basaltColumn = resolveBlock("basalt_column", Blocks.BASALT).defaultBlockState();
                    BlockState basalt = Blocks.BASALT.defaultBlockState();
                    BlockState smoothBasalt = Blocks.SMOOTH_BASALT.defaultBlockState();
                    BlockState gravel = Blocks.GRAVEL.defaultBlockState();

                    SurfaceRules.RuleSource blueFrozenMossRule = SurfaceRules.state(blueFrozenMoss);
                    SurfaceRules.RuleSource basaltColumnRule = SurfaceRules.state(basaltColumn);
                    SurfaceRules.RuleSource basaltRule = SurfaceRules.state(basalt);
                    SurfaceRules.RuleSource smoothBasaltRule = SurfaceRules.state(smoothBasalt);
                    SurfaceRules.RuleSource gravelRule = SurfaceRules.state(gravel);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.steep(),
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.noiseCondition(Noises.ICE, -0.18D, 0.18D),
                                                            basaltRule
                                                    ),
                                                    basaltColumnRule
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(
                                                                            SurfaceRules.noiseCondition(Noises.ICE, 0.22D, 1.0D),
                                                                            blueFrozenMossRule
                                                                    ),
                                                                    basaltRule
                                                            )
                                                    ),
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.noiseCondition(Noises.ICE, 0.15D, 1.0D),
                                                            gravelRule
                                                    ),
                                                    smoothBasaltRule
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            smoothBasaltRule
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            basaltRule
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #6 — 2.6 Steaming Skerries (Overworld | Vapor + Glacial | Uncommon)
        // Spec: Coastal shelves & shallow hot pools (-0.055..0.08, strictly > -0.06 so
        //       100% disjoint from Biomes 2 & 3, and < 0.09 so disjoint from Biomes 4 & 5),
        //       Flat ice-worn coastal shelves (0.25..1.0, disjoint from Biome 1's -1.0..-0.375),
        //       Medium skerry weirdness (+/-0.05..0.39, disjoint from +/-0.40..1.0),
        //       Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.STEAMING_SKERRIES)
                .dimension(LevelStem.OVERWORLD)
                // Positive medium-weirdness coastal steaming ice shelves & thermal pools
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.055F, 0.08F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(0.05F, 0.39F)
                        .build())
                // Negative medium-weirdness coastal steaming ice shelves & thermal pools
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.055F, 0.08F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.39F, -0.05F)
                        .build())
                .surfaceRule(() -> {
                    BlockState steamingIce = resolveBlock("steaming_ice", Blocks.BLUE_ICE).defaultBlockState();
                    BlockState steamingSpringStone = resolveBlock("steaming_spring_stone", Blocks.BASALT).defaultBlockState();
                    BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
                    BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
                    BlockState basalt = Blocks.BASALT.defaultBlockState();

                    SurfaceRules.RuleSource steamingIceRule = SurfaceRules.state(steamingIce);
                    SurfaceRules.RuleSource steamingSpringStoneRule = SurfaceRules.state(steamingSpringStone);
                    SurfaceRules.RuleSource blueIceRule = SurfaceRules.state(blueIce);
                    SurfaceRules.RuleSource packedIceRule = SurfaceRules.state(packedIce);
                    SurfaceRules.RuleSource basaltRule = SurfaceRules.state(basalt);

                    SurfaceRules.ConditionSource atOrAbove58 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(58), 0);
                    SurfaceRules.ConditionSource below69 = SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(69), 0));
                    SurfaceRules.ConditionSource steamingIceBand = SurfaceRules.noiseCondition(Noises.ICE, 0.20D, 1.0D);
                    SurfaceRules.ConditionSource thermalSpringBand = SurfaceRules.noiseCondition(Noises.ICE, -1.0D, -0.32D);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            atOrAbove58,
                                            SurfaceRules.ifTrue(
                                                    below69,
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(
                                                                    SurfaceRules.ON_FLOOR,
                                                                    SurfaceRules.sequence(
                                                                            SurfaceRules.ifTrue(thermalSpringBand, steamingSpringStoneRule),
                                                                            SurfaceRules.ifTrue(steamingIceBand, steamingIceRule),
                                                                            blueIceRule
                                                                    )
                                                            ),
                                                            SurfaceRules.ifTrue(
                                                                    SurfaceRules.UNDER_FLOOR,
                                                                    packedIceRule
                                                            ),
                                                            SurfaceRules.ifTrue(
                                                                    SurfaceRules.DEEP_UNDER_FLOOR,
                                                                    basaltRule
                                                            )
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(atOrAbove58, blueIceRule),
                                                    basaltRule
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(atOrAbove58, packedIceRule),
                                                    basaltRule
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            basaltRule
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #7 — 2.7 Static Ice-Plains (Overworld | Lightning + Glacial | Uncommon)
        // Spec: Medium inland plains (0.09..0.65, strictly > 0.08 so 100% disjoint from
        //       coastal Biomes 1, 2, 3, and 6), High smooth wind-swept plains erosion
        //       (0.25..1.0, strictly > 0.15 so 100% disjoint from Biome 4's -0.35..0.15
        //       and Biome 5's -1.0..-0.375), Low uniform flat weirdness (-0.35..0.35,
        //       disjoint from +/-0.40..1.0), Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.STATIC_ICE_PLAINS)
                .dimension(LevelStem.OVERWORLD)
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 0.65F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.35F, 0.35F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource fulguriteIce = SurfaceRules.state(
                            resolveBlock("fulgurite_ice", Blocks.PACKED_ICE).defaultBlockState());
                    SurfaceRules.RuleSource snowBlock = SurfaceRules.state(Blocks.SNOW_BLOCK.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());
                    SurfaceRules.ConditionSource fulguriteIceBand = SurfaceRules.noiseCondition(Noises.ICE, 0.32D, 1.0D);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.ifTrue(
                                                    SurfaceRules.waterBlockCheck(-1, 0),
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(fulguriteIceBand, fulguriteIce),
                                                            snowBlock
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            packedIce
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            packedIce
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #8 — 2.8 Barrow-Mound Tundra (Overworld | Barrow + Glacial | Rare)
        // Spec: Rolling inland barrow hills (0.09..0.75, strictly > 0.08 so 100% disjoint
        //       from coastal Biomes 1, 2, 3, and 6), Rolling tundra erosion (0.18..1.0,
        //       strictly > 0.15 so 100% disjoint from Biome 4's -0.35..0.15 and Biome 5's
        //       -1.0..-0.375), High mound weirdness (+/-0.40..1.0, strictly disjoint from
        //       Biome 7's -0.35..0.35), Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.BARROW_MOUND_TUNDRA)
                .dimension(LevelStem.OVERWORLD)
                // Positive high-weirdness rolling barrow hills
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 0.75F)
                        .erosion(0.18F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_POSITIVE)
                        .build())
                // Negative high-weirdness rolling barrow hills
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 0.75F)
                        .erosion(0.18F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_NEGATIVE)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource necroticSoil = SurfaceRules.state(
                            resolveBlock("necrotic_soil", Blocks.PODZOL).defaultBlockState());
                    SurfaceRules.RuleSource barrowMoss = SurfaceRules.state(
                            resolveBlock("barrow_moss", Blocks.MOSS_BLOCK).defaultBlockState());
                    SurfaceRules.RuleSource snowBlock = SurfaceRules.state(Blocks.SNOW_BLOCK.defaultBlockState());
                    SurfaceRules.RuleSource coarseDirt = SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState());
                    SurfaceRules.RuleSource deepslate = SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState());

                    SurfaceRules.ConditionSource necroticSurfaceBand = SurfaceRules.noiseCondition(Noises.ICE, 0.18D, 1.0D);
                    SurfaceRules.ConditionSource barrowMossBand = SurfaceRules.noiseCondition(Noises.ICE, -1.0D, -0.52D);
                    SurfaceRules.ConditionSource necroticSubfloorBand = SurfaceRules.noiseCondition(Noises.ICE, 0.12D, 1.0D);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.ifTrue(
                                                    SurfaceRules.waterBlockCheck(-1, 0),
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(necroticSurfaceBand, necroticSoil),
                                                            SurfaceRules.ifTrue(barrowMossBand, barrowMoss),
                                                            snowBlock
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(necroticSubfloorBand, necroticSoil),
                                                    coarseDirt
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            deepslate
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #9 — 2.9 Null-Fringe Bay (Overworld | Null-U + Glacial | Legendary)
        // Spec: Coastal rift bays (-0.055..0.08, strictly > -0.06 so 100% disjoint
        //       from Biomes 2 & 3, and < 0.09 so disjoint from inland Biomes 4, 5, 7, 8),
        //       Smooth coastal bay erosion (0.25..1.0, strictly > -0.375 so disjoint from
        //       Biome 1), High rift weirdness (+/-0.40..1.0, strictly outside Biome 6's
        //       +/-0.05..0.39), Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.NULL_FRINGE_BAY)
                .dimension(LevelStem.OVERWORLD)
                // Positive high-weirdness coastal rift bays
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.055F, 0.08F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_POSITIVE)
                        .build())
                // Negative high-weirdness coastal rift bays
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.055F, 0.08F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_NEGATIVE)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource voidSand = SurfaceRules.state(
                            resolveBlock("void_sand", Blocks.SAND).defaultBlockState());
                    SurfaceRules.RuleSource voidSandTurf = SurfaceRules.state(
                            resolveBlock("void_sand_turf", Blocks.SNOW_BLOCK).defaultBlockState());
                    SurfaceRules.RuleSource voidStone = SurfaceRules.state(
                            resolveBlock("void_stone", Blocks.DEEPSLATE).defaultBlockState());
                    SurfaceRules.RuleSource blueIce = SurfaceRules.state(Blocks.BLUE_ICE.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());
                    SurfaceRules.RuleSource snowBlock = SurfaceRules.state(Blocks.SNOW_BLOCK.defaultBlockState());

                    SurfaceRules.ConditionSource atOrAbove66 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(66), 0);
                    SurfaceRules.ConditionSource below66 = SurfaceRules.not(atOrAbove66);
                    SurfaceRules.ConditionSource atOrAbove59 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(59), 0);
                    SurfaceRules.ConditionSource below59 = SurfaceRules.not(atOrAbove59);
                    SurfaceRules.ConditionSource atOrAbove64 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(64), 0);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    // Upper coastal shelf (Y >= 66)
                                    SurfaceRules.ifTrue(
                                            atOrAbove66,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.ON_FLOOR,
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(
                                                                            SurfaceRules.noiseCondition(Noises.ICE, 0.18D, 1.0D),
                                                                            blueIce
                                                                    ),
                                                                    SurfaceRules.ifTrue(
                                                                            SurfaceRules.noiseCondition(Noises.ICE, -0.25D, 0.18D),
                                                                            voidSandTurf
                                                                    ),
                                                                    snowBlock
                                                            )
                                                    ),
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.UNDER_FLOOR,
                                                            packedIce
                                                    ),
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                                            voidStone
                                                    )
                                            )
                                    ),
                                    // Shoreline & shallow bay (59 <= Y < 66)
                                    SurfaceRules.ifTrue(
                                            atOrAbove59,
                                            SurfaceRules.ifTrue(
                                                    below66,
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(
                                                                    SurfaceRules.ON_FLOOR,
                                                                    SurfaceRules.sequence(
                                                                            SurfaceRules.ifTrue(
                                                                                    SurfaceRules.waterBlockCheck(-1, 0),
                                                                                    SurfaceRules.ifTrue(
                                                                                            atOrAbove64,
                                                                                            SurfaceRules.sequence(
                                                                                                    SurfaceRules.ifTrue(
                                                                                                            SurfaceRules.noiseCondition(Noises.ICE, 0.25D, 1.0D),
                                                                                                            voidSandTurf
                                                                                                    ),
                                                                                                    voidSand
                                                                                            )
                                                                                    )
                                                                            ),
                                                                            voidSand
                                                                    )
                                                            ),
                                                            SurfaceRules.ifTrue(
                                                                    SurfaceRules.UNDER_FLOOR,
                                                                    voidSand
                                                            ),
                                                            SurfaceRules.ifTrue(
                                                                    SurfaceRules.DEEP_UNDER_FLOOR,
                                                                    voidStone
                                                            )
                                                    )
                                            )
                                    ),
                                    // Deep bay floor (Y < 59)
                                    SurfaceRules.ifTrue(
                                            below59,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, voidStone),
                                                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, voidStone),
                                                    SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, voidStone)
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #10 — 2.10 Radiant Glaze-Plains (Overworld | Vitae + Aurora | Rare)
        // Spec: Sun-lit inland polar oasis plains, 100% 6D-disjoint from Biomes 1–9:
        //   - Slice 1: continentalness 0.09..0.65 (> 0.08 coastal Biomes 1,2,3,6,9),
        //              erosion 0.16..0.24 (> 0.15 Biomes 4,5 and < 0.25 Biome 7),
        //              weirdness -0.39..0.39 (strictly inside +/-0.40..1.0 of Biome 8).
        //   - Slice 2: continentalness 0.66..1.0 (> 0.65 Biome 7 and coastal Biomes 1,2,3,5,6,9),
        //              erosion 0.25..1.0 (> 0.15 Biome 4),
        //              weirdness -0.39..0.39 (strictly inside +/-0.40..1.0 of Biome 8).
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.RADIANT_GLAZE_PLAINS)
                .dimension(LevelStem.OVERWORLD)
                // Slice 1: Gently rolling mid-inland sun-glazed polar plains
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 0.65F)
                        .erosion(0.16F, 0.24F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.39F, 0.39F)
                        .build())
                // Slice 2: Far-inland flat sun-glazed oasis plains
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.66F, 1.0F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.39F, 0.39F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource radiantSnow = SurfaceRules.state(
                            resolveBlock("radiant_snow_block", Blocks.SNOW_BLOCK).defaultBlockState());
                    SurfaceRules.RuleSource radiantGlazeIce = SurfaceRules.state(
                            resolveBlock("radiant_glaze_ice", Blocks.PACKED_ICE).defaultBlockState());
                    SurfaceRules.RuleSource vitaeSpringStone = SurfaceRules.state(
                            resolveBlock("vitae_spring_stone", Blocks.CALCITE).defaultBlockState());
                    SurfaceRules.RuleSource grassBlock = SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState());
                    SurfaceRules.RuleSource dirt = SurfaceRules.state(Blocks.DIRT.defaultBlockState());
                    SurfaceRules.RuleSource calcite = SurfaceRules.state(Blocks.CALCITE.defaultBlockState());

                    SurfaceRules.ConditionSource glazeIceStreakBand = SurfaceRules.noiseCondition(Noises.ICE, 0.34D, 1.0D);
                    SurfaceRules.ConditionSource oasisMeadowBand = SurfaceRules.noiseCondition(Noises.ICE, -0.22D, 0.34D);
                    SurfaceRules.ConditionSource springStoneVeinBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.48D, 1.0D);
                    SurfaceRules.ConditionSource deepSpringStoneBand = SurfaceRules.noiseCondition(Noises.ICE, 0.08D, 1.0D);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(glazeIceStreakBand, radiantGlazeIce),
                                                                    SurfaceRules.ifTrue(springStoneVeinBand, vitaeSpringStone),
                                                                    SurfaceRules.ifTrue(oasisMeadowBand, grassBlock),
                                                                    radiantSnow
                                                            )
                                                    ),
                                                    // Submerged shallow lake/spring floors
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(deepSpringStoneBand, vitaeSpringStone),
                                                            calcite
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(oasisMeadowBand, dirt),
                                                    SurfaceRules.ifTrue(glazeIceStreakBand, vitaeSpringStone),
                                                    calcite
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(deepSpringStoneBand, vitaeSpringStone),
                                                    calcite
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #11 — 2.11 Storm-Swept Icebergs (Overworld | Storm + Glacial | Uncommon)
        // Spec: Deep polar ocean (-0.85..-0.36, strictly < -0.32 so 100% disjoint from
        //       all Biomes #1-#10!), open wind-scoured ocean erosion (0.25..1.0),
        //       weirdness (-0.39..0.39), Polar/Icy temperature.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.STORM_SWEPT_ICEBERGS)
                .dimension(LevelStem.OVERWORLD)
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.85F, -0.36F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.39F, 0.39F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource fulguriteIce = SurfaceRules.state(
                            resolveBlock("fulgurite_ice", Blocks.BLUE_ICE).defaultBlockState());
                    SurfaceRules.RuleSource zephyrIce = SurfaceRules.state(
                            resolveBlock("zephyr_ice", Blocks.PACKED_ICE).defaultBlockState());
                    SurfaceRules.RuleSource fulguriteBlock = SurfaceRules.state(
                            resolveBlock("fulgurite_block", Blocks.DEEPSLATE).defaultBlockState());
                    SurfaceRules.RuleSource snowBlock = SurfaceRules.state(Blocks.SNOW_BLOCK.defaultBlockState());
                    SurfaceRules.RuleSource blueIce = SurfaceRules.state(Blocks.BLUE_ICE.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());
                    SurfaceRules.RuleSource gravel = SurfaceRules.state(Blocks.GRAVEL.defaultBlockState());
                    SurfaceRules.RuleSource deepslate = SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState());

                    SurfaceRules.ConditionSource fulguriteStreakBand = SurfaceRules.noiseCondition(Noises.ICE, 0.34D, 1.0D);
                    SurfaceRules.ConditionSource zephyrStreakBand = SurfaceRules.noiseCondition(Noises.ICE, 0.06D, 0.34D);
                    SurfaceRules.ConditionSource blueIceStreakBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.28D, 1.0D);
                    SurfaceRules.ConditionSource seabedFulguritePatch = SurfaceRules.noiseCondition(Noises.ICE, 0.32D, 1.0D);
                    SurfaceRules.ConditionSource seabedDeepslatePatch = SurfaceRules.noiseCondition(Noises.ICE, -1.0D, -0.25D);
                    SurfaceRules.ConditionSource atOrAbove62 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    // Above water: streaks of fulgurite_ice, zephyr_ice, blue_ice & snow_block
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(fulguriteStreakBand, fulguriteIce),
                                                                    SurfaceRules.ifTrue(zephyrStreakBand, zephyrIce),
                                                                    SurfaceRules.ifTrue(blueIceStreakBand, blueIce),
                                                                    snowBlock
                                                            )
                                                    ),
                                                    // Submerged ocean floor: fulgurite_block patches, deepslate & gravel
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(seabedFulguritePatch, fulguriteBlock),
                                                            SurfaceRules.ifTrue(seabedDeepslatePatch, deepslate),
                                                            gravel
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(atOrAbove62, packedIce),
                                                    gravel
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(atOrAbove62, blueIce),
                                                    deepslate
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #12 — 2.12 Spore-Glazed Pack (Overworld | Spore + Glacial | Uncommon)
        // Spec: Sheltered coastal bay borders & pack-ice mouths, 100% 6D-disjoint from Biomes #1-#11:
        //   - Slice 1: continentalness -0.35..0.08 (> -0.36 Biome #11 and < 0.09 Biomes #4,#5,#7,#8,#10),
        //              erosion -0.36..0.24 (> -0.375 Biome #1 and < 0.25 Biomes #2,#3,#6,#9),
        //              weirdness -0.39..0.39.
        //   - Slice 2: continentalness -0.35..0.08, erosion 0.25..1.0,
        //              weirdness -0.045..0.045 (strictly inside -0.05..0.05 so disjoint from Biomes #2,#6,#3,#9).
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.SPORE_GLAZED_PACK)
                .dimension(LevelStem.OVERWORLD)
                // Slice 1: Sheltered Coastal Bay Valleys & Lagoon Margins
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.35F, 0.08F)
                        .erosion(-0.36F, 0.24F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.39F, 0.39F)
                        .build())
                // Slice 2: Flat Sea-Level Coastal Bay Channels & Pack-Ice Mouths
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.35F, 0.08F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.045F, 0.045F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource frozenSporeMoss = SurfaceRules.state(
                            resolveBlock("frozen_spore_moss", Blocks.MOSS_BLOCK).defaultBlockState());
                    SurfaceRules.RuleSource sporeGlazedIce = SurfaceRules.state(
                            resolveBlock("spore_glazed_ice", Blocks.PACKED_ICE).defaultBlockState());
                    SurfaceRules.RuleSource blueIce = SurfaceRules.state(Blocks.BLUE_ICE.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());
                    SurfaceRules.RuleSource snowBlock = SurfaceRules.state(Blocks.SNOW_BLOCK.defaultBlockState());
                    SurfaceRules.RuleSource coarseDirt = SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState());
                    SurfaceRules.RuleSource mud = SurfaceRules.state(Blocks.MUD.defaultBlockState());
                    SurfaceRules.RuleSource clay = SurfaceRules.state(Blocks.CLAY.defaultBlockState());
                    SurfaceRules.RuleSource gravel = SurfaceRules.state(Blocks.GRAVEL.defaultBlockState());

                    SurfaceRules.ConditionSource frozenSporeMossBand = SurfaceRules.noiseCondition(Noises.ICE, -0.18D, 0.38D);
                    SurfaceRules.ConditionSource sporeGlazedIceBand = SurfaceRules.noiseCondition(Noises.ICE, 0.38D, 1.0D);
                    SurfaceRules.ConditionSource blueIceSurfaceBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.30D, 1.0D);

                    SurfaceRules.ConditionSource above61 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0);
                    SurfaceRules.ConditionSource atOrAbove59 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(59), 0);
                    SurfaceRules.ConditionSource bayGlazedIceShelfBand = SurfaceRules.noiseCondition(Noises.ICE, 0.42D, 1.0D);
                    SurfaceRules.ConditionSource bayClayBand = SurfaceRules.noiseCondition(Noises.ICE, 0.12D, 0.42D);
                    SurfaceRules.ConditionSource bayGravelBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.25D, 1.0D);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    // Above water: alternating cushions of frozen_spore_moss, spore_glazed_ice, blue_ice & snow_block
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(sporeGlazedIceBand, sporeGlazedIce),
                                                                    SurfaceRules.ifTrue(frozenSporeMossBand, frozenSporeMoss),
                                                                    SurfaceRules.ifTrue(blueIceSurfaceBand, blueIce),
                                                                    snowBlock
                                                            )
                                                    ),
                                                    // Submerged shallow bay floor: mud, clay, gravel & occasional spore_glazed_ice shelves
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(atOrAbove59, SurfaceRules.ifTrue(bayGlazedIceShelfBand, sporeGlazedIce)),
                                                            SurfaceRules.ifTrue(bayClayBand, clay),
                                                            SurfaceRules.ifTrue(bayGravelBand, gravel),
                                                            mud
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            above61,
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(frozenSporeMossBand, coarseDirt),
                                                                    packedIce
                                                            )
                                                    ),
                                                    SurfaceRules.ifTrue(bayClayBand, clay),
                                                    mud
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(above61, packedIce),
                                                    gravel
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #13 — 2.13 Mirage Glacier (Overworld | Mirage + Glacial | Rare)
        // Spec: Inland U-shaped desert-glacier valleys & far-inland shelf pockets,
        //       100% 6D-disjoint from Biomes #1-#12:
        //   - Point 1: continentalness 0.09..1.0 (> 0.08 coastal Biomes #1,2,3,6,9,11,12),
        //              erosion -0.35..0.15 (> -0.375 Biome #5 and < 0.16 Biomes #7,8,10),
        //              weirdness -0.39..0.39 (strictly inside +/-0.40..1.0 of Biome #4).
        //   - Point 2: continentalness 0.89..1.0 (> 0.88 Biome #4 & > 0.75 Biomes #5,8),
        //              erosion -0.35..1.0, weirdness 0.40..1.0 (outside Biome #10's -0.39..0.39).
        //   - Point 3: continentalness 0.89..1.0, erosion -0.35..1.0, weirdness -1.0..-0.40.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.MIRAGE_GLACIER)
                .dimension(LevelStem.OVERWORLD)
                // Point 1: Inland Glacier-Carved U-Shaped Valleys
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.09F, 1.0F)
                        .erosion(-0.35F, 0.15F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.39F, 0.39F)
                        .build())
                // Point 2: +High Weirdness Deep Far-Inland Desert-Glacier Shelf Pockets
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.89F, 1.0F)
                        .erosion(-0.35F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(0.40F, 1.0F)
                        .build())
                // Point 3: -High Weirdness Deep Far-Inland Desert-Glacier Shelf Pockets
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.89F, 1.0F)
                        .erosion(-0.35F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-1.0F, -0.40F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource glassSand = SurfaceRules.state(
                            resolveBlock("glass_sand", Blocks.SAND).defaultBlockState());
                    SurfaceRules.RuleSource glassSandstone = SurfaceRules.state(
                            resolveBlock("glass_sandstone", Blocks.SANDSTONE).defaultBlockState());
                    SurfaceRules.RuleSource mirageIce = SurfaceRules.state(
                            resolveBlock("mirage_ice", Blocks.BLUE_ICE).defaultBlockState());
                    SurfaceRules.RuleSource redSand = SurfaceRules.state(Blocks.RED_SAND.defaultBlockState());
                    SurfaceRules.RuleSource blueIce = SurfaceRules.state(Blocks.BLUE_ICE.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());

                    SurfaceRules.ConditionSource outerDuneSurfaceBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.18D, 1.0D);
                    SurfaceRules.ConditionSource outerDuneIceBand = SurfaceRules.noiseCondition(Noises.ICE, -1.0D, -0.24D);
                    SurfaceRules.ConditionSource redSandAccentBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.58D, 1.0D);
                    SurfaceRules.ConditionSource mirageIceTroughBand = SurfaceRules.noiseCondition(Noises.ICE, 0.24D, 1.0D);
                    SurfaceRules.ConditionSource deepTroughBlueIceBand = SurfaceRules.noiseCondition(Noises.ICE, 0.0D, 1.0D);

                    SurfaceRules.RuleSource duneTopSurface = SurfaceRules.sequence(
                            SurfaceRules.ifTrue(redSandAccentBand, redSand),
                            glassSand
                    );

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    // Steep valley walls or outer desert-glacier dune bands
                                                                    SurfaceRules.ifTrue(SurfaceRules.steep(), duneTopSurface),
                                                                    SurfaceRules.ifTrue(outerDuneSurfaceBand, duneTopSurface),
                                                                    SurfaceRules.ifTrue(outerDuneIceBand, duneTopSurface),
                                                                    // Central glacier trough: alternating mirage_ice & blue_ice
                                                                    SurfaceRules.ifTrue(mirageIceTroughBand, mirageIce),
                                                                    blueIce
                                                            )
                                                    ),
                                                    glassSandstone
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(SurfaceRules.steep(), glassSandstone),
                                                    SurfaceRules.ifTrue(outerDuneSurfaceBand, glassSandstone),
                                                    SurfaceRules.ifTrue(outerDuneIceBand, glassSandstone),
                                                    packedIce
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(SurfaceRules.steep(), glassSandstone),
                                                    SurfaceRules.ifTrue(outerDuneSurfaceBand, glassSandstone),
                                                    SurfaceRules.ifTrue(outerDuneIceBand, glassSandstone),
                                                    SurfaceRules.ifTrue(deepTroughBlueIceBand, blueIce),
                                                    glassSandstone
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.steep(),
                                            glassSandstone
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #14 — 2.14 Oasis Floes (Overworld | Oasis + Glacial | Uncommon)
        // Spec: Sheltered warm-spring inland polar basin valleys & carved spring valleys,
        //       100% 6D-disjoint from ALL Biomes #1-#13:
        //   - Slice 1 (+High Weirdness Inland Basin Valleys):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness 0.76..0.88 (> 0.75 Biome #8
        //       & < 0.89 Biome #13), erosion 0.16..1.0 (> 0.15 Biomes #4,#5,#13),
        //       DEPTH_SURFACE_AND_FLOOR, weirdness 0.40..1.0 (outside Biome #10's -0.39..0.39).
        //   - Slice 2 (-High Weirdness Inland Basin Valleys):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness 0.76..0.88, erosion 0.16..1.0,
        //       DEPTH_SURFACE_AND_FLOOR, weirdness -1.0..-0.40.
        //   - Slice 3 (Mid-to-Far Inland Carved Spring Valleys):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness 0.66..1.0 (> 0.65 Biomes #7,#10-S1),
        //       erosion 0.16..0.24 (> 0.15 Biomes #4,#13-S1 & < 0.25 Biome #10-S2),
        //       DEPTH_SURFACE_AND_FLOOR, weirdness -0.39..0.39 (strictly inside +/-0.40..1.0 of Biomes #8,#13).
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.OASIS_FLOES)
                .dimension(LevelStem.OVERWORLD)
                // Slice 1: +High Weirdness Inland Basin Valleys
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.76F, 0.88F)
                        .erosion(0.16F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(0.40F, 1.0F)
                        .build())
                // Slice 2: -High Weirdness Inland Basin Valleys
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.76F, 0.88F)
                        .erosion(0.16F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-1.0F, -0.40F)
                        .build())
                // Slice 3: Mid-to-Far Inland Carved Spring Valleys
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(0.66F, 1.0F)
                        .erosion(0.16F, 0.24F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.39F, 0.39F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource vitaeSpringStone = SurfaceRules.state(
                            resolveRegisteredBlockState("vitae_spring_stone", () -> ModBlocks.VITAE_SPRING_STONE.get(), Blocks.CALCITE));
                    SurfaceRules.RuleSource grassBlock = SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState());
                    SurfaceRules.RuleSource dirt = SurfaceRules.state(Blocks.DIRT.defaultBlockState());
                    SurfaceRules.RuleSource snowBlock = SurfaceRules.state(Blocks.SNOW_BLOCK.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());

                    SurfaceRules.ConditionSource centralSpringBasinBand = SurfaceRules.noiseCondition(Noises.ICE, 0.28D, 1.0D);
                    SurfaceRules.ConditionSource warmSpringMarginBand = SurfaceRules.noiseCondition(Noises.ICE, -0.24D, 0.28D);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    // Central spring basin floor: vitae_spring_stone
                                                                    SurfaceRules.ifTrue(centralSpringBasinBand, vitaeSpringStone),
                                                                    // Warm spring margin bands (-0.24..0.28): soft green grass_block
                                                                    SurfaceRules.ifTrue(warmSpringMarginBand, grassBlock),
                                                                    // Outer snowy pine forest slopes: snow_block
                                                                    snowBlock
                                                            )
                                                    ),
                                                    // Submerged / underwater floors: vitae_spring_stone
                                                    vitaeSpringStone
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(centralSpringBasinBand, vitaeSpringStone),
                                                    SurfaceRules.ifTrue(warmSpringMarginBand, dirt),
                                                    SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), dirt),
                                                    vitaeSpringStone
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(centralSpringBasinBand, vitaeSpringStone),
                                                    packedIce
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #15 — 2.15 Dust-Crusted Bergs (Overworld | Dust + Glacial | Uncommon)
        // Spec: Dry wind-scoured coastal berg inlets & outer table-iceberg shelves,
        //       100% 6D-disjoint from ALL Biomes #1-#14:
        //   - Slice 1 (+High Weirdness Dry Wind-Scoured Coastal Berg Inlets):
        //       TEMP_ICY, humidity -1.0..0.09 (< 0.10 Biome #3), continentalness -0.35..-0.06
        //       (< -0.055 Biomes #6,#9 & > -0.36 Biome #11), erosion 0.25..1.0 (> 0.24 Biome #12-S1
        //       & > -0.375 Biome #1), DEPTH_SURFACE_AND_FLOOR, weirdness 0.41..1.0 (> 0.40 Biome #2
        //       & > 0.045 Biome #12-S2).
        //   - Slice 2 (-High Weirdness Dry Wind-Scoured Coastal Berg Inlets):
        //       TEMP_ICY, humidity -1.0..0.09, continentalness -0.35..-0.06, erosion 0.25..1.0,
        //       DEPTH_SURFACE_AND_FLOOR, weirdness -1.0..-0.41.
        //   - Slice 3 (+High Weirdness Outer Table-Iceberg Shelf):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness -0.85..-0.36 (< -0.35 Biomes #1-#10,#12-#14),
        //       erosion 0.25..1.0, DEPTH_SURFACE_AND_FLOOR, weirdness 0.41..1.0 (> 0.39 Biome #11).
        //   - Slice 4 (-High Weirdness Outer Table-Iceberg Shelf):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness -0.85..-0.36, erosion 0.25..1.0,
        //       DEPTH_SURFACE_AND_FLOOR, weirdness -1.0..-0.41 (< -0.39 Biome #11).
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.DUST_CRUSTED_BERGS)
                .dimension(LevelStem.OVERWORLD)
                // Slice 1: +High Weirdness Dry Wind-Scoured Coastal Berg Inlets
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(-1.0F, 0.09F)
                        .continentalness(-0.35F, -0.06F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(0.41F, 1.0F)
                        .build())
                // Slice 2: -High Weirdness Dry Wind-Scoured Coastal Berg Inlets
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(-1.0F, 0.09F)
                        .continentalness(-0.35F, -0.06F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-1.0F, -0.41F)
                        .build())
                // Slice 3: +High Weirdness Outer Table-Iceberg Shelf
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.85F, -0.36F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(0.41F, 1.0F)
                        .build())
                // Slice 4: -High Weirdness Outer Table-Iceberg Shelf
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.85F, -0.36F)
                        .erosion(0.25F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-1.0F, -0.41F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource greySand = SurfaceRules.state(
                            resolveBlock("grey_sand", Blocks.SAND).defaultBlockState());
                    SurfaceRules.RuleSource dustCrustedIce = SurfaceRules.state(
                            resolveBlock("dust_crusted_ice", Blocks.PACKED_ICE).defaultBlockState());
                    SurfaceRules.RuleSource coarseGravel = SurfaceRules.state(
                            resolveBlock("coarse_gravel", Blocks.GRAVEL).defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());
                    SurfaceRules.RuleSource blueIce = SurfaceRules.state(Blocks.BLUE_ICE.defaultBlockState());
                    SurfaceRules.RuleSource tuff = SurfaceRules.state(Blocks.TUFF.defaultBlockState());

                    SurfaceRules.ConditionSource dustCrustedIceBand = SurfaceRules.noiseCondition(Noises.ICE, 0.42D, 1.0D);
                    SurfaceRules.ConditionSource greySandBand = SurfaceRules.noiseCondition(Noises.ICE, -0.22D, 0.42D);
                    SurfaceRules.ConditionSource coarseGravelScreeBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.38D, 1.0D);

                    SurfaceRules.ConditionSource seabedGreySandBand = SurfaceRules.noiseCondition(Noises.ICE, 0.18D, 1.0D);
                    SurfaceRules.ConditionSource seabedTuffBand = SurfaceRules.noiseCondition(Noises.ICE, -1.0D, -0.24D);
                    SurfaceRules.ConditionSource above61 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    // Above water: barren windblown sheets of grey_sand, dust_crusted_ice & coarse_gravel
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(dustCrustedIceBand, dustCrustedIce),
                                                                    SurfaceRules.ifTrue(coarseGravelScreeBand, coarseGravel),
                                                                    SurfaceRules.ifTrue(greySandBand, greySand),
                                                                    dustCrustedIce
                                                            )
                                                    ),
                                                    // Submerged seabed: grey_sand, coarse_gravel & tuff
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(seabedGreySandBand, greySand),
                                                            SurfaceRules.ifTrue(seabedTuffBand, tuff),
                                                            coarseGravel
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            above61,
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(dustCrustedIceBand, dustCrustedIce),
                                                                    packedIce
                                                            )
                                                    ),
                                                    SurfaceRules.ifTrue(seabedTuffBand, tuff),
                                                    coarseGravel
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(
                                                            above61,
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(dustCrustedIceBand, blueIce),
                                                                    packedIce
                                                            )
                                                    ),
                                                    tuff
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #16 — 2.16 Blood-Ice Shallows (Overworld | Blood + Glacial | Rare)
        // Spec: Stepped coastal bay ice margins, outer shallow bay mouths, and inner
        //       sheltered high-weirdness coastal coves, 100% 6D-disjoint from Biomes #1-#15:
        //   - Slices 1 & 2 (Stepped Coastal Bay Ice Margins):
        //       TEMP_ICY, humidity 0.10..1.0 (> 0.09 Biome #15), continentalness -0.35..-0.06
        //       (< -0.055 Biomes #6,#9 & > -0.36 Biome #11), erosion 0.25..0.44 (< 0.45 Biome #3
        //       & > 0.24 Biome #12-S1), DEPTH_SURFACE_AND_FLOOR, weirdness 0.41..1.0 / -1.0..-0.41
        //       (> 0.40 Biome #2 & > 0.045 Biome #12-S2).
        //   - Slices 3 & 4 (Outer Shallow Bay Mouths):
        //       TEMP_ICY, humidity 0.10..1.0, continentalness -0.35..-0.29 (< -0.28 Biome #3
        //       & > -0.36 Biome #11), erosion 0.45..1.0, DEPTH_SURFACE_AND_FLOOR,
        //       weirdness 0.41..1.0 / -1.0..-0.41.
        //   - Slices 5 & 6 (Inner Sheltered High-Weirdness Coastal Coves):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness -0.35..0.08 (< 0.09 Biomes
        //       #4,#5,#7,#8,#10,#13,#14 & > -0.36 Biome #11), erosion -0.36..0.24 (> -0.375
        //       Biome #1 & < 0.25 Biomes #2,#3,#6,#9,#15), DEPTH_SURFACE_AND_FLOOR,
        //       weirdness 0.41..1.0 / -1.0..-0.41 (> 0.39 Biome #12-S1).
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.BLOOD_ICE_SHALLOWS)
                .dimension(LevelStem.OVERWORLD)
                // Slice 1: +High Weirdness Stepped Coastal Bay Ice Margins
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(0.10F, 1.0F)
                        .continentalness(-0.35F, -0.06F)
                        .erosion(0.25F, 0.44F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(0.41F, 1.0F)
                        .offset(0.0F)
                        .build())
                // Slice 2: -High Weirdness Stepped Coastal Bay Ice Margins
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(0.10F, 1.0F)
                        .continentalness(-0.35F, -0.06F)
                        .erosion(0.25F, 0.44F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-1.0F, -0.41F)
                        .offset(0.0F)
                        .build())
                // Slice 3: +High Weirdness Outer Shallow Bay Mouths
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(0.10F, 1.0F)
                        .continentalness(-0.35F, -0.29F)
                        .erosion(0.45F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(0.41F, 1.0F)
                        .offset(0.0F)
                        .build())
                // Slice 4: -High Weirdness Outer Shallow Bay Mouths
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(0.10F, 1.0F)
                        .continentalness(-0.35F, -0.29F)
                        .erosion(0.45F, 1.0F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-1.0F, -0.41F)
                        .offset(0.0F)
                        .build())
                // Slice 5: +High Weirdness Inner Sheltered Coastal Coves
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.35F, 0.08F)
                        .erosion(-0.36F, 0.24F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(0.41F, 1.0F)
                        .offset(0.0F)
                        .build())
                // Slice 6: -High Weirdness Inner Sheltered Coastal Coves
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.35F, 0.08F)
                        .erosion(-0.36F, 0.24F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-1.0F, -0.41F)
                        .offset(0.0F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource sanguineIce = SurfaceRules.state(
                            resolveRegisteredBlockState("sanguine_ice", () -> ModBlocks.SANGUINE_ICE.get(), Blocks.PACKED_ICE));
                    SurfaceRules.RuleSource redClaySand = SurfaceRules.state(
                            resolveRegisteredBlockState("red_clay_sand", () -> ModBlocks.RED_CLAY_SAND.get(), Blocks.RED_SAND));
                    SurfaceRules.RuleSource sanguineClayBricks = SurfaceRules.state(
                            resolveRegisteredBlockState("sanguine_clay_bricks", () -> ModBlocks.SANGUINE_CLAY_BRICKS.get(), Blocks.MUD_BRICKS));
                    SurfaceRules.RuleSource deepslate = SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState());

                    // ~65% Sanguine Ice and ~35% Red Clay Sand on exposed coastal shelves above water
                    SurfaceRules.ConditionSource sanguineIceShelfBand = SurfaceRules.noiseCondition(Noises.ICE, -0.20D, 1.0D);
                    SurfaceRules.ConditionSource clayBricksFoundationBand = SurfaceRules.noiseCondition(Noises.ICE, 0.24D, 1.0D);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    // Above water on exposed coastal shelves: ~65% Sanguine Ice, ~35% Red Clay Sand
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, sanguineClayBricks),
                                                                    SurfaceRules.ifTrue(sanguineIceShelfBand, sanguineIce),
                                                                    redClaySand
                                                            )
                                                    ),
                                                    // Underwater shallow bay floor: Red Clay Sand
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, sanguineClayBricks),
                                                            redClaySand
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, sanguineClayBricks),
                                                    redClaySand
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(clayBricksFoundationBand, sanguineClayBricks),
                                                    deepslate
                                            )
                                    )
                            )
                    );
                })
                .build());

        // =========================================================================
        // BIOME #17 — 2.17 Astral Skerries (Overworld | Astral + Glacial | Rare)
        // Spec: Barren starlight-ice & dark impactite rocky skerry island chains in
        //       deep/mid polar waters & sheer coastal spire channels, 100% 6D-disjoint
        //       from ALL Biomes #1-#16:
        //   - Slices 1 & 2 (Deep/Mid Polar Skerry Archipelago, +/-High Weirdness):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness -0.95..-0.36 (< -0.35
        //       Biomes #1-#10,#12-#14,#16), erosion -1.0..0.24 (< 0.25 Biomes #11,#15),
        //       DEPTH_SURFACE_AND_FLOOR, weirdness 0.40..1.0 / -1.0..-0.40.
        //   - Slice 3 (Mid-Weirdness Polar Skerry Archipelago):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness -0.95..-0.36 (< -0.35
        //       Biomes #1-#10,#12-#14,#16), erosion -0.55..0.24 (< 0.25 Biomes #11,#15),
        //       DEPTH_SURFACE_AND_FLOOR, weirdness -0.39..0.39 (disjoint from Slices 1 & 2).
        //   - Slices 4 & 5 (Coastal Spire Skerries, +/-High Weirdness):
        //       TEMP_ICY, FULL_RANGE humidity, continentalness -0.35..-0.23 (< -0.22
        //       Biome #1 & > -0.36 Slices 1-3, Biomes #11,#15-S3/4), erosion -1.0..-0.38
        //       (< -0.36 Biomes #2,#3,#6,#9,#12,#15,#16), DEPTH_SURFACE_AND_FLOOR,
        //       weirdness 0.40..1.0 / -1.0..-0.40.
        // =========================================================================
        register(BiomePlacementRule.builder(EntropicaBiome.ASTRAL_SKERRIES)
                .dimension(LevelStem.OVERWORLD)
                // Slice 1: +High Weirdness Deep/Mid Polar Skerry Archipelago
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.95F, -0.36F)
                        .erosion(-1.0F, 0.24F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_POSITIVE)
                        .offset(0.0F)
                        .build())
                // Slice 2: -High Weirdness Deep/Mid Polar Skerry Archipelago
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.95F, -0.36F)
                        .erosion(-1.0F, 0.24F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_NEGATIVE)
                        .offset(0.0F)
                        .build())
                // Slice 3: Mid-Weirdness Polar Skerry Archipelago
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.95F, -0.36F)
                        .erosion(-0.55F, 0.24F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(-0.39F, 0.39F)
                        .offset(0.0F)
                        .build())
                // Slice 4: +High Weirdness Coastal Spire Skerries
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.35F, -0.23F)
                        .erosion(-1.0F, -0.38F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_POSITIVE)
                        .offset(0.0F)
                        .build())
                // Slice 5: -High Weirdness Coastal Spire Skerries
                .addParameterPoint(EntropicaClimatePresets.builder()
                        .temperature(EntropicaClimatePresets.TEMP_ICY)
                        .humidity(EntropicaClimatePresets.FULL_RANGE)
                        .continentalness(-0.35F, -0.23F)
                        .erosion(-1.0F, -0.38F)
                        .depth(EntropicaClimatePresets.DEPTH_SURFACE_AND_FLOOR)
                        .weirdness(EntropicaClimatePresets.WEIRDNESS_HIGH_NEGATIVE)
                        .offset(0.0F)
                        .build())
                .surfaceRule(() -> {
                    SurfaceRules.RuleSource starlightIce = SurfaceRules.state(
                            resolveRegisteredBlockState("starlight_ice", () -> ModBlocks.STARLIGHT_ICE.get(), Blocks.BLUE_ICE));
                    SurfaceRules.RuleSource astralImpactite = SurfaceRules.state(
                            resolveRegisteredBlockState("astral_impactite", () -> ModBlocks.ASTRAL_IMPACTITE.get(), Blocks.DEEPSLATE));
                    SurfaceRules.RuleSource coarseGravel = SurfaceRules.state(
                            resolveRegisteredBlockState("coarse_gravel", () -> ModBlocks.COARSE_GRAVEL.get(), Blocks.GRAVEL));
                    SurfaceRules.RuleSource blueIce = SurfaceRules.state(Blocks.BLUE_ICE.defaultBlockState());
                    SurfaceRules.RuleSource packedIce = SurfaceRules.state(Blocks.PACKED_ICE.defaultBlockState());
                    SurfaceRules.RuleSource stone = SurfaceRules.state(Blocks.STONE.defaultBlockState());
                    SurfaceRules.RuleSource deepslate = SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState());

                    SurfaceRules.ConditionSource cliffStarlightStripe = SurfaceRules.noiseCondition(Noises.ICE, 0.16D, 1.0D);
                    SurfaceRules.ConditionSource starlightShelfBand = SurfaceRules.noiseCondition(Noises.ICE, -0.10D, 1.0D);
                    SurfaceRules.ConditionSource impactiteOutcropBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.18D, 1.0D);
                    SurfaceRules.ConditionSource coastalBlueIceBand = SurfaceRules.noiseCondition(Noises.ICE, -0.42D, -0.10D);
                    SurfaceRules.ConditionSource coastalCoarseGravelBand = SurfaceRules.noiseCondition(Noises.SURFACE, -0.24D, 0.18D);

                    SurfaceRules.ConditionSource reefBlueIceBand = SurfaceRules.noiseCondition(Noises.ICE, 0.30D, 1.0D);
                    SurfaceRules.ConditionSource reefImpactiteBand = SurfaceRules.noiseCondition(Noises.SURFACE, 0.14D, 1.0D);
                    SurfaceRules.ConditionSource deepImpactiteBand = SurfaceRules.noiseCondition(Noises.ICE, 0.10D, 1.0D);
                    SurfaceRules.ConditionSource above60 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(60), 0);

                    return SurfaceRules.ifTrue(
                            SurfaceRules.abovePreliminarySurface(),
                            SurfaceRules.sequence(
                                    // Steep skerry cliffs: dark astral_impactite striped with glowing starlight_ice
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.steep(),
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(cliffStarlightStripe, starlightIce),
                                                    astralImpactite
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.sequence(
                                                    // Exposed barren skerry shelves above water (ZERO grass or dirt!)
                                                    SurfaceRules.ifTrue(
                                                            SurfaceRules.waterBlockCheck(-1, 0),
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, astralImpactite),
                                                                    SurfaceRules.ifTrue(starlightShelfBand, starlightIce),
                                                                    SurfaceRules.ifTrue(impactiteOutcropBand, astralImpactite),
                                                                    SurfaceRules.ifTrue(coastalBlueIceBand, blueIce),
                                                                    SurfaceRules.ifTrue(coastalCoarseGravelBand, coarseGravel),
                                                                    stone
                                                            )
                                                    ),
                                                    // Underwater cosmic channel floor: coarse_gravel, astral_impactite & blue_ice reefs
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, astralImpactite),
                                                            SurfaceRules.ifTrue(reefBlueIceBand, blueIce),
                                                            SurfaceRules.ifTrue(reefImpactiteBand, astralImpactite),
                                                            coarseGravel
                                                    )
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, astralImpactite),
                                                    SurfaceRules.ifTrue(
                                                            above60,
                                                            SurfaceRules.sequence(
                                                                    SurfaceRules.ifTrue(starlightShelfBand, packedIce),
                                                                    astralImpactite
                                                            )
                                                    ),
                                                    SurfaceRules.ifTrue(reefImpactiteBand, astralImpactite),
                                                    packedIce
                                            )
                                    ),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.DEEP_UNDER_FLOOR,
                                            SurfaceRules.sequence(
                                                    SurfaceRules.ifTrue(above60, packedIce),
                                                    SurfaceRules.ifTrue(deepImpactiteBand, astralImpactite),
                                                    deepslate
                                            )
                                    )
                            )
                    );
                })
                .build());
    }

    private static Block resolveBlock(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private static BlockState resolveRegisteredBlockState(String path, Supplier<Block> modSupplier, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        var regBlock = BuiltInRegistries.BLOCK.getOptional(id);
        if (regBlock.isPresent()) {
            return regBlock.get().defaultBlockState();
        }
        try {
            return modSupplier.get().defaultBlockState();
        } catch (Throwable ignored) {
            return fallback.defaultBlockState();
        }
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
