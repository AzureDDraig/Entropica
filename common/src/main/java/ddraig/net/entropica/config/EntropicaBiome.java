package ddraig.net.entropica.config;

import ddraig.net.entropica.Entropica;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Canonical registry metadata and per-biome world-generation config identifiers
 * for all 108 planned Entropica biomes across the 6 climatic zones.
 */
public enum EntropicaBiome {
    // --- POLAR_FROZEN ---
    BLUE_GLACIER_SHELF(1, "2.1", "blue_glacier_shelf", "Blue-Glacier Shelf", ClimateZone.POLAR_FROZEN, "Overworld", "Glacial + Aegis", "Rare"),
    AURORAL_SKERRIES(2, "2.2", "auroral_skerries", "Auroral Skerries", ClimateZone.POLAR_FROZEN, "Overworld", "Aurora + Vapor", "Uncommon"),
    FROST_BITE_FLOES(3, "2.3", "frost_bite_floes", "Frost-Bite Floes", ClimateZone.POLAR_FROZEN, "Overworld", "Glacial + Blight", "Rare"),
    CRYSTALLINE_PACK_ICE(4, "2.4", "crystalline_pack_ice", "Crystalline Pack-Ice", ClimateZone.POLAR_FROZEN, "Overworld", "Aegis + Aurora", "Rare"),
    SILENT_FJORD(5, "2.5", "silent_fjord", "Silent Fjord", ClimateZone.POLAR_FROZEN, "Overworld", "Eclipse + Glacial", "Rare"),
    STEAMING_SKERRIES(6, "2.6", "steaming_skerries", "Steaming Skerries", ClimateZone.POLAR_FROZEN, "Overworld", "Vapor + Glacial", "Uncommon"),
    STATIC_ICE_PLAINS(7, "2.7", "static_ice_plains", "Static Ice-Plains", ClimateZone.POLAR_FROZEN, "Overworld", "Lightning + Glacial", "Uncommon"),
    BARROW_MOUND_TUNDRA(8, "2.8", "barrow_mound_tundra", "Barrow-Mound Tundra", ClimateZone.POLAR_FROZEN, "Overworld", "Barrow + Glacial", "Rare"),
    NULL_FRINGE_BAY(9, "2.9", "null_fringe_bay", "Null-Fringe Bay", ClimateZone.POLAR_FROZEN, "Overworld", "Null-U + Glacial", "Legendary"),
    RADIANT_GLAZE_PLAINS(10, "2.10", "radiant_glaze_plains", "Radiant Glaze-Plains", ClimateZone.POLAR_FROZEN, "Overworld", "Vitae + Aurora", "Rare"),
    STORM_SWEPT_ICEBERGS(11, "2.11", "storm_swept_icebergs", "Storm-Swept Icebergs", ClimateZone.POLAR_FROZEN, "Overworld", "Storm + Glacial", "Uncommon"),
    SPORE_GLAZED_PACK(12, "2.12", "spore_glazed_pack", "Spore-Glazed Pack", ClimateZone.POLAR_FROZEN, "Overworld", "Spore + Glacial", "Uncommon"),
    MIRAGE_GLACIER(13, "2.13", "mirage_glacier", "Mirage Glacier", ClimateZone.POLAR_FROZEN, "Overworld", "Mirage + Glacial", "Rare"),
    OASIS_FLOES(14, "2.14", "oasis_floes", "Oasis Floes", ClimateZone.POLAR_FROZEN, "Overworld", "Oasis + Glacial", "Uncommon"),
    DUST_CRUSTED_BERGS(15, "2.15", "dust_crusted_bergs", "Dust-Crusted Bergs", ClimateZone.POLAR_FROZEN, "Overworld", "Dust + Glacial", "Uncommon"),
    BLOOD_ICE_SHALLOWS(16, "2.16", "blood_ice_shallows", "Blood-Ice Shallows", ClimateZone.POLAR_FROZEN, "Overworld", "Blood + Glacial", "Rare"),
    ASTRAL_SKERRIES(17, "2.17", "astral_skerries", "Astral Skerries", ClimateZone.POLAR_FROZEN, "Overworld", "Astral + Glacial", "Rare"),
    AURA_GLAZED_PEAK(18, "2.18", "aura_glazed_peak", "Aura-Glazed Peak", ClimateZone.POLAR_FROZEN, "Overworld", "Aura + Glacial", "Rare"),
    // --- COLD_ALPINE_BOREAL ---
    SILVER_PINE_TAIGA(19, "3.19", "silver_pine_taiga", "Silver-Pine Taiga", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Overgrowth + Glacial", "Common"),
    CRAGGED_SCREE(20, "3.20", "cragged_scree", "Cragged Scree", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Arid + Aegis", "Uncommon"),
    SHADED_RIDGE(21, "3.21", "shaded_ridge", "Shaded Ridge", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Eclipse + Aegis", "Rare"),
    VAPOROUS_ALPINE(22, "3.22", "vaporous_alpine", "Vaporous Alpine", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Vapor + Aura", "Uncommon"),
    BOREAL_HEATH(23, "3.23", "boreal_heath", "Boreal Heath", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Blight + Taiga", "Uncommon"),
    LIGHTNING_PEAK(24, "3.24", "lightning_peak", "Lightning Peak", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Lightning + Aegis", "Rare"),
    FROST_TOMB_BARROW(25, "3.25", "frost_tomb_barrow", "Frost-Tomb Barrow", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Barrow + Taiga", "Rare"),
    VOID_CRACKED_ALPINE(26, "3.26", "void_cracked_alpine", "Void-Cracked Alpine", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Null-U + Aegis", "Legendary"),
    LUMINOUS_CRAGS(27, "3.27", "luminous_crags", "Luminous Crags", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Vitae + Aegis", "Rare"),
    STORM_SWEPT_RIDGES(28, "3.28", "storm_swept_ridges", "Storm-Swept Ridges", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Storm + Aegis", "Uncommon"),
    SPORE_WOOD_ALPINE(29, "3.29", "spore_wood_alpine", "Spore-Wood Alpine", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Spore + Taiga", "Uncommon"),
    MIRAGE_CRAGS(30, "3.30", "mirage_crags", "Mirage Crags", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Mirage + Aegis", "Rare"),
    AMBER_PINE_VALE(31, "3.31", "amber_pine_vale", "Amber Pine-Vale", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Oasis + Taiga", "Uncommon"),
    DUST_SCOURED_ALPINE(32, "3.32", "dust_scoured_alpine", "Dust-Scoured Alpine", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Dust + Aegis", "Uncommon"),
    BLOOD_STONE_RIDGES(33, "3.33", "blood_stone_ridges", "Blood-Stone Ridges", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Blood + Aegis", "Rare"),
    ASTRAL_VALLEYS(34, "3.34", "astral_valleys", "Astral Valleys", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Astral + Taiga", "Rare"),
    AURA_ALPINE(35, "3.35", "aura_alpine", "Aura Alpine", ClimateZone.COLD_ALPINE_BOREAL, "Overworld", "Aura + Aegis", "Rare"),
    COLD_ABYSSAL_CHASM(36, "3.36", "cold_abyssal_chasm", "Cold Abyssal Chasm", ClimateZone.COLD_ALPINE_BOREAL, "Overworld Caves", "Abyss + Taiga", "Rare"),
    // --- TEMPERATE_PLAINS_FORESTS ---
    MOSSY_MEADOW(37, "4.37", "mossy_meadow", "Mossy Meadow", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Overgrowth + Vitae", "Common"),
    WHISPERING_GROVE(38, "4.38", "whispering_grove", "Whispering Grove", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Abyss + Overgrowth", "Uncommon"),
    STATIC_STEPPE(39, "4.39", "static_steppe", "Static Steppe", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Lightning + Overgrowth", "Uncommon"),
    BLIGHTED_ORCHARD(40, "4.40", "blighted_orchard", "Blighted Orchard", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Blight + Overgrowth", "Rare"),
    BARROW_HEATH(41, "4.41", "barrow_heath", "Barrow Heath", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Barrow + Overgrowth", "Rare"),
    VOID_TOUCHED_GRASSLANDS(42, "4.42", "void_touched_grasslands", "Void-Touched Grasslands", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Null-U + Overgrowth", "Rare"),
    RADIANT_FIELDS(43, "4.43", "radiant_fields", "Radiant Fields", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Vitae + Overgrowth", "Rare"),
    STORM_SWEPT_PLAINS(44, "4.44", "storm_swept_plains", "Storm-Swept Plains", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Storm + Overgrowth", "Uncommon"),
    SPORE_ORCHARD(45, "4.45", "spore_orchard", "Spore Orchard", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Spore + Overgrowth", "Uncommon"),
    SHIMMERING_GLADE(46, "4.46", "shimmering_glade", "Shimmering Glade", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Mirage + Overgrowth", "Rare"),
    AMBER_GLADE(47, "4.47", "amber_glade", "Amber Glade", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Oasis + Overgrowth", "Uncommon"),
    DUST_SILT_HEATH(48, "4.48", "dust_silt_heath", "Dust-Silt Heath", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Dust + Overgrowth", "Uncommon"),
    CRIMSON_ORCHARD(49, "4.49", "crimson_orchard", "Crimson Orchard", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Blood + Overgrowth", "Rare"),
    ASTRAL_GROVE(50, "4.50", "astral_grove", "Astral Grove", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Astral + Overgrowth", "Rare"),
    WINDSWEPT_CANOPY(51, "4.51", "windswept_canopy", "Windswept Canopy", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Aura + Overgrowth", "Uncommon"),
    FROST_TOUCHED_MEADOW(52, "4.52", "frost_touched_meadow", "Frost-Touched Meadow", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Glacial + Overgrowth", "Uncommon"),
    AEGIS_MEADOW(53, "4.53", "aegis_meadow", "Aegis Meadow", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Aegis + Overgrowth", "Rare"),
    AURORAL_GROVE(54, "4.54", "auroral_grove", "Auroral Grove", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Aurora + Overgrowth", "Rare"),
    AURORA_GROVE(55, "4.55", "aurora_grove", "Aurora Grove", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Aurora + Overgrowth", "Rare"),
    STEAMING_MEADOW(56, "4.56", "steaming_meadow", "Steaming Meadow", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Vapor + Overgrowth", "Uncommon"),
    AEGIS_ORCHARD(57, "4.57", "aegis_orchard", "Aegis Orchard", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Aegis + Overgrowth", "Uncommon"),
    SHADOWED_WOODS(58, "4.58", "shadowed_woods", "Shadowed Woods", ClimateZone.TEMPERATE_PLAINS_FORESTS, "Overworld", "Eclipse + Overgrowth", "Rare"),
    // --- WARM_SWAMPS_JUNGLES ---
    CYPRESS_JUNGLE(59, "5.59", "cypress_jungle", "Cypress Jungle", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Overgrowth + Abyss", "Common"),
    STEAMING_LAGOON(60, "5.60", "steaming_lagoon", "Steaming Lagoon", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Vapor + Abyss", "Uncommon"),
    THUNDERING_BAYOU(61, "5.61", "thundering_bayou", "Thundering Bayou", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Storm + Abyss", "Uncommon"),
    BLIGHTED_SLUDGE(62, "5.62", "blighted_sludge", "Blighted Sludge", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Blight + Abyss", "Rare"),
    GRAVE_MOSS_BAYOU(63, "5.63", "grave_moss_bayou", "Grave-Moss Bayou", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Barrow + Abyss", "Rare"),
    VOID_FRINGE_MANGROVE(64, "5.64", "void_fringe_mangrove", "Void-Fringe Mangrove", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Null-U + Abyss", "Rare"),
    LUMINOUS_WETLANDS(65, "5.65", "luminous_wetlands", "Luminous Wetlands", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Vitae + Abyss", "Rare"),
    SPORE_BAYOU(66, "5.66", "spore_bayou", "Spore Bayou", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Spore + Abyss", "Uncommon"),
    MIRAGE_MARSH(67, "5.67", "mirage_marsh", "Mirage Marsh", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Mirage + Abyss", "Rare"),
    AMBER_MANGROVE(68, "5.68", "amber_mangrove", "Amber Mangrove", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Oasis + Abyss", "Uncommon"),
    DUST_SWEPT_BAYOU(69, "5.69", "dust_swept_bayou", "Dust-Swept Bayou", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Dust + Abyss", "Uncommon"),
    CRIMSON_MANGROVE(70, "5.70", "crimson_mangrove", "Crimson Mangrove", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Blood + Abyss", "Rare"),
    ASTRAL_BAYOU(71, "5.71", "astral_bayou", "Astral Bayou", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Astral + Abyss", "Rare"),
    AURA_MANGROVE(72, "5.72", "aura_mangrove", "Aura Mangrove", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Aura + Abyss", "Rare"),
    ECLIPSED_MANGROVE(73, "5.73", "eclipsed_mangrove", "Eclipsed Mangrove", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Eclipse + Abyss", "Rare"),
    AEGIS_BAYOU(74, "5.74", "aegis_bayou", "Aegis Bayou", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Aegis + Abyss", "Rare"),
    GLACIAL_FEN(75, "5.75", "glacial_fen", "Glacial Fen", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Glacial + Abyss", "Rare"),
    AURORA_MANGROVE(76, "5.76", "aurora_mangrove", "Aurora Mangrove", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld", "Aurora + Abyss", "Rare"),
    SUBMERGED_TRENCH(77, "5.77", "submerged_trench", "Submerged Trench", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld Caves", "Abyss + Abyss", "Rare"),
    VOID_RIFT_CAVERN(78, "5.78", "void_rift_cavern", "Void Rift Cavern", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld Caves", "Null-U + Null-U", "Rare"),
    VAPOR_SPRINGS(79, "5.79", "vapor_springs", "Vapor Springs", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld Caves", "Vapor + Vapor", "Rare"),
    AURORA_VAULT(80, "5.80", "aurora_vault", "Aurora Vault", ClimateZone.WARM_SWAMPS_JUNGLES, "Overworld Caves", "Aurora + Aurora", "Rare"),
    // --- HOT_ARID_DESERT ---
    RED_SAND_ARCHES(81, "6.81", "red_sand_arches", "Red-Sand Arches", ClimateZone.HOT_ARID_DESERT, "Overworld", "Arid + Aegis", "Uncommon"),
    GLASSIFIED_STEPPE(82, "6.82", "glassified_steppe", "Glassified Steppe", ClimateZone.HOT_ARID_DESERT, "Overworld", "Mirage + Arid", "Rare"),
    THUNDERING_DUNES(83, "6.83", "thundering_dunes", "Thundering Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Storm + Arid", "Uncommon"),
    BLIGHTED_DUNES(84, "6.84", "blighted_dunes", "Blighted Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Blight + Arid", "Rare"),
    TOMB_DUNES(85, "6.85", "tomb_dunes", "Tomb Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Barrow + Arid", "Rare"),
    VOID_FRINGE_DUNES(86, "6.86", "void_fringe_dunes", "Void-Fringe Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Null-U + Arid", "Rare"),
    LUMINOUS_DUNES(87, "6.87", "luminous_dunes", "Luminous Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Vitae + Arid", "Rare"),
    SPORE_DUNES(88, "6.88", "spore_dunes", "Spore Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Spore + Arid", "Uncommon"),
    DUST_SWEPT_CANYONS(89, "6.89", "dust_swept_canyons", "Dust-Swept Canyons", ClimateZone.HOT_ARID_DESERT, "Overworld", "Dust + Arid", "Common"),
    CRIMSON_DUNES(90, "6.90", "crimson_dunes", "Crimson Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Blood + Arid", "Rare"),
    ASTRAL_DUNES(91, "6.91", "astral_dunes", "Astral Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Astral + Arid", "Rare"),
    AURA_DUNES(92, "6.92", "aura_dunes", "Aura Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Aura + Arid", "Rare"),
    ECLIPSED_DUNES(93, "6.93", "eclipsed_dunes", "Eclipsed Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Eclipse + Arid", "Rare"),
    GLACIAL_DUNES(94, "6.94", "glacial_dunes", "Glacial Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Glacial + Arid", "Rare"),
    AURORA_DUNES(95, "6.95", "aurora_dunes", "Aurora Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Aurora + Arid", "Rare"),
    VAPOR_DUNES(96, "6.96", "vapor_dunes", "Vapor Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Vapor + Arid", "Rare"),
    OVERGROWTH_DUNES(97, "6.97", "overgrowth_dunes", "Overgrowth Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Overgrowth + Arid", "Rare"),
    ABYSS_DUNES(98, "6.98", "abyss_dunes", "Abyss Dunes", ClimateZone.HOT_ARID_DESERT, "Overworld", "Abyss + Arid", "Rare"),
    // --- NETHER_VOLCANIC_DEPTHS ---
    BRIMSTONE_CHIMNEYS(99, "7.99", "brimstone_chimneys", "Brimstone Chimneys", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Magma + Aegis", "Uncommon"),
    SANGUINE_LAKES(100, "7.100", "sanguine_lakes", "Sanguine Lakes", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Blood + Vapor", "Uncommon"),
    BASALT_SPIRELANDS(101, "7.101", "basalt_spirelands", "Basalt Spirelands", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Magma + Null-U", "Uncommon"),
    STARDUST_CAVERNS(102, "7.102", "stardust_caverns", "Stardust Caverns", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Null-R + Aurora", "Rare"),
    ECLIPSE_SPIRELANDS(103, "7.103", "eclipse_spirelands", "Eclipse Spirelands", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Eclipse + Aegis", "Rare"),
    SOULFIRE_BASALT_FJORDS(104, "7.104", "soulfire_basalt_fjords", "Soulfire Basalt-Fjords", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Soulfire + Magma", "Uncommon"),
    BLIGHTED_VOLCANIC(105, "7.105", "blighted_volcanic", "Blighted Volcanic", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Blight + Magma", "Rare"),
    GRAVE_STONE_SPIRES(106, "7.106", "grave_stone_spires", "Grave-Stone Spires", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Barrow + Aegis", "Rare"),
    VOID_CRACKED_NETHER(107, "7.107", "void_cracked_nether", "Void-Cracked Nether", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Null-U + Magma", "Rare"),
    RADIANT_BASALT_FLATS(108, "7.108", "radiant_basalt_flats", "Radiant Basalt-Flats", ClimateZone.NETHER_VOLCANIC_DEPTHS, "Nether", "Vitae + Magma", "Rare");

    public enum ClimateZone {
        POLAR_FROZEN("polar_frozen", "Polar / Frozen (Biomes 1-18)"),
        COLD_ALPINE_BOREAL("cold_alpine_boreal", "Cold / Alpine / Boreal (Biomes 19-36)"),
        TEMPERATE_PLAINS_FORESTS("temperate_plains_forests", "Temperate / Plains / Forests (Biomes 37-58)"),
        WARM_SWAMPS_JUNGLES("warm_swamps_jungles", "Warm / Swamps / Jungles (Biomes 59-80)"),
        HOT_ARID_DESERT("hot_arid_desert", "Hot / Arid / Desert (Biomes 81-98)"),
        NETHER_VOLCANIC_DEPTHS("nether_volcanic_depths", "Nether Caves & Volcanic Depths (Biomes 99-108)");

        private final String configKey;
        private final String displayName;

        ClimateZone(String configKey, String displayName) {
            this.configKey = configKey;
            this.displayName = displayName;
        }

        public String getConfigKey() {
            return configKey;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static final Map<String, EntropicaBiome> BY_ID;
    private static final Map<Integer, EntropicaBiome> BY_NUMBER;

    static {
        Map<String, EntropicaBiome> idMap = new HashMap<>();
        Map<Integer, EntropicaBiome> numMap = new HashMap<>();
        for (EntropicaBiome biome : values()) {
            idMap.put(biome.id, biome);
            numMap.put(biome.number, biome);
        }
        BY_ID = Collections.unmodifiableMap(idMap);
        BY_NUMBER = Collections.unmodifiableMap(numMap);
    }

    private final int number;
    private final String sectionCode;
    private final String id;
    private final String displayName;
    private final ClimateZone zone;
    private final String dimension;
    private final String essenceSummary;
    private final String rarity;

    EntropicaBiome(int number, String sectionCode, String id, String displayName, ClimateZone zone,
                   String dimension, String essenceSummary, String rarity) {
        this.number = number;
        this.sectionCode = sectionCode;
        this.id = id;
        this.displayName = displayName;
        this.zone = zone;
        this.dimension = dimension;
        this.essenceSummary = essenceSummary;
        this.rarity = rarity;
    }

    public int getNumber() {
        return number;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ClimateZone getZone() {
        return zone;
    }

    public String getDimension() {
        return dimension;
    }

    public String getEssenceSummary() {
        return essenceSummary;
    }

    public String getRarity() {
        return rarity;
    }

    public String getConfigDescription() {
        return String.format("[Biome #%d - %s | %s] Enable natural world generation of %s (%s, %s). Default: true",
                number, sectionCode, dimension, displayName, essenceSummary, rarity);
    }

    public ResourceLocation resourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(Entropica.MODID, id);
    }

    public ResourceKey<Biome> resourceKey() {
        return ResourceKey.create(Registries.BIOME, resourceLocation());
    }

    public boolean isEnabled() {
        return EntropicaConfig.isBiomeEnabled(this);
    }

    public static EntropicaBiome byId(String id) {
        return BY_ID.get(id);
    }

    public static EntropicaBiome byNumber(int number) {
        return BY_NUMBER.get(number);
    }
}
