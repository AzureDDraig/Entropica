package ddraig.net.entropica.entity.grot;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrotSpawnRegistry {

    private static TagKey<Biome> cTag(String path) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static final TagKey<Biome> IS_DESERT = cTag("is_desert");
    private static final TagKey<Biome> IS_BADLANDS = cTag("is_badlands");
    private static final TagKey<Biome> IS_PLAINS = cTag("is_plains");
    private static final TagKey<Biome> IS_DARK_FOREST = cTag("is_dark_forest");
    private static final TagKey<Biome> IS_SNOWY = cTag("is_snowy");
    private static final TagKey<Biome> IS_SWAMP = cTag("is_swamp");
    private static final TagKey<Biome> IS_CAVE = cTag("is_cave");

    public static final ResourceKey<Biome> BLUE_GLACIER_SHELF = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "blue_glacier_shelf")
    );

    public static final ResourceKey<Biome> AURORAL_SKERRIES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "auroral_skerries")
    );

    public static final ResourceKey<Biome> FROST_BITE_FLOES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "frost_bite_floes")
    );

    public static final ResourceKey<Biome> CRYSTALLINE_PACK_ICE = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "crystalline_pack_ice")
    );

    public static final ResourceKey<Biome> SILENT_FJORD = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "silent_fjord")
    );

    public static final ResourceKey<Biome> STEAMING_SKERRIES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "steaming_skerries")
    );

    public static final ResourceKey<Biome> STATIC_ICE_PLAINS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "static_ice_plains")
    );

    public static final ResourceKey<Biome> BARROW_MOUND_TUNDRA = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "barrow_mound_tundra")
    );

    public static final ResourceKey<Biome> NULL_FRINGE_BAY = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "null_fringe_bay")
    );

    public static final ResourceKey<Biome> RADIANT_GLAZE_PLAINS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "radiant_glaze_plains")
    );

    public static final ResourceKey<Biome> STORM_SWEPT_ICEBERGS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "storm_swept_icebergs")
    );

    public static final ResourceKey<Biome> SPORE_GLAZED_PACK = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "spore_glazed_pack")
    );

    public static final ResourceKey<Biome> MIRAGE_GLACIER = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "mirage_glacier")
    );

    public static final ResourceKey<Biome> OASIS_FLOES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "oasis_floes")
    );

    public static final ResourceKey<Biome> DUST_CRUSTED_BERGS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "dust_crusted_bergs")
    );

    public static final ResourceKey<Biome> BLOOD_ICE_SHALLOWS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "blood_ice_shallows")
    );

    public static final ResourceKey<Biome> ASTRAL_SKERRIES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "astral_skerries")
    );

    // ==========================================
    // CUSTOM WEIGHTED LIST UTILITY
    // Bypasses 1.21.x Mojang mapping breakages!
    // ==========================================
    public static class WeightedList<T> {
        private final List<Entry<T>> entries = new ArrayList<>();
        private int totalWeight = 0;

        public WeightedList<T> add(T item, int weight) {
            entries.add(new Entry<>(item, weight));
            totalWeight += weight;
            return this;
        }

        public T getRandomValue(RandomSource random, T fallback) {
            if (totalWeight <= 0 || entries.isEmpty()) return fallback;
            int r = random.nextInt(totalWeight);
            for (Entry<T> entry : entries) {
                r -= entry.weight;
                if (r < 0) return entry.item;
            }
            return fallback;
        }

        private record Entry<T>(T item, int weight) {}
    }

    private static final Map<Object, WeightedList<EssenceType>> BIOME_WEIGHTS = new HashMap<>();

    // The Anomaly Pool (Any Tier 1/2 Fusion + Some Wildcards)
    private static final List<EssenceType> ANOMALY_POOL = Arrays.asList(
            EssenceType.VOID, EssenceType.RADIANT, EssenceType.UMBRAL, EssenceType.UNDEAD, EssenceType.LIGHTNING,
            EssenceType.MAGMA, EssenceType.STORM, EssenceType.GLACIAL, EssenceType.OVERGROWTH, EssenceType.DUST,
            EssenceType.SPORE, EssenceType.TAIGA, EssenceType.OASIS, EssenceType.ECLIPSE, EssenceType.BLIGHT,
            EssenceType.ASTRAL, EssenceType.SOULFIRE, EssenceType.VAPOR, EssenceType.DAWN, EssenceType.ABYSS
    );

    private static final WeightedList<EssenceType> DEFAULT_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.EARTH, 40)
            .add(EssenceType.NATURE, 30)
            .add(EssenceType.WATER, 20)
            .add(EssenceType.AIR, 10);

    public static final WeightedList<EssenceType> BLUE_GLACIER_SHELF_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.GLACIAL, 60)
            .add(EssenceType.AEGIS, 30)
            .add(EssenceType.FROZEN, 10);

    public static final WeightedList<EssenceType> AURORAL_SKERRIES_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.AURORA, 55)
            .add(EssenceType.VAPOR, 35)
            .add(EssenceType.GLACIAL, 10);

    public static final WeightedList<EssenceType> FROST_BITE_FLOES_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.GLACIAL, 50)
            .add(EssenceType.BLIGHT, 40)
            .add(EssenceType.FROZEN, 10);

    public static final WeightedList<EssenceType> CRYSTALLINE_PACK_ICE_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.AEGIS, 55)
            .add(EssenceType.AURORA, 35)
            .add(EssenceType.GLACIAL, 10);

    public static final WeightedList<EssenceType> SILENT_FJORD_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.ECLIPSE, 55)
            .add(EssenceType.GLACIAL, 35)
            .add(EssenceType.FROZEN, 10);

    public static final WeightedList<EssenceType> STEAMING_SKERRIES_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.VAPOR, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.MAGMA, 10);

    public static final WeightedList<EssenceType> STATIC_ICE_PLAINS_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.LIGHTNING, 55)
            .add(EssenceType.GLACIAL, 35)
            .add(EssenceType.AEGIS, 10);

    public static final WeightedList<EssenceType> BARROW_MOUND_TUNDRA_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.BARROW, 55)
            .add(EssenceType.GLACIAL, 35)
            .add(EssenceType.AEGIS, 10);

    public static final WeightedList<EssenceType> NULL_FRINGE_BAY_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.NULL_U, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.VOID, 10);

    public static final WeightedList<EssenceType> RADIANT_GLAZE_PLAINS_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.VITAE, 60)
            .add(EssenceType.AURORA, 30)
            .add(EssenceType.GLACIAL, 10);

    public static final WeightedList<EssenceType> STORM_SWEPT_ICEBERGS_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.STORM, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.AURORA, 10);

    public static final WeightedList<EssenceType> SPORE_GLAZED_PACK_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.SPORE, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.OVERGROWTH, 10);

    public static final WeightedList<EssenceType> MIRAGE_GLACIER_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.MIRAGE, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.ARID, 10);

    public static final WeightedList<EssenceType> OASIS_FLOES_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.OASIS, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.VAPOR, 10);

    public static final WeightedList<EssenceType> DUST_CRUSTED_BERGS_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.DUST, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.ARID, 10);

    public static final WeightedList<EssenceType> BLOOD_ICE_SHALLOWS_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.BLOOD, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.VITAE, 10);

    public static final WeightedList<EssenceType> ASTRAL_SKERRIES_POOL = new WeightedList<EssenceType>()
            .add(EssenceType.ASTRAL, 60)
            .add(EssenceType.GLACIAL, 30)
            .add(EssenceType.AURORA, 10);

    public static void initialize() {
        // --- ENTROPICA CUSTOM BIOMES ---
        BIOME_WEIGHTS.put(BLUE_GLACIER_SHELF, BLUE_GLACIER_SHELF_POOL);
        BIOME_WEIGHTS.put(AURORAL_SKERRIES, AURORAL_SKERRIES_POOL);
        BIOME_WEIGHTS.put(FROST_BITE_FLOES, FROST_BITE_FLOES_POOL);
        BIOME_WEIGHTS.put(CRYSTALLINE_PACK_ICE, CRYSTALLINE_PACK_ICE_POOL);
        BIOME_WEIGHTS.put(SILENT_FJORD, SILENT_FJORD_POOL);
        BIOME_WEIGHTS.put(STEAMING_SKERRIES, STEAMING_SKERRIES_POOL);
        BIOME_WEIGHTS.put(STATIC_ICE_PLAINS, STATIC_ICE_PLAINS_POOL);
        BIOME_WEIGHTS.put(BARROW_MOUND_TUNDRA, BARROW_MOUND_TUNDRA_POOL);
        BIOME_WEIGHTS.put(NULL_FRINGE_BAY, NULL_FRINGE_BAY_POOL);
        BIOME_WEIGHTS.put(RADIANT_GLAZE_PLAINS, RADIANT_GLAZE_PLAINS_POOL);
        BIOME_WEIGHTS.put(STORM_SWEPT_ICEBERGS, STORM_SWEPT_ICEBERGS_POOL);
        BIOME_WEIGHTS.put(SPORE_GLAZED_PACK, SPORE_GLAZED_PACK_POOL);
        BIOME_WEIGHTS.put(MIRAGE_GLACIER, MIRAGE_GLACIER_POOL);
        BIOME_WEIGHTS.put(OASIS_FLOES, OASIS_FLOES_POOL);
        BIOME_WEIGHTS.put(DUST_CRUSTED_BERGS, DUST_CRUSTED_BERGS_POOL);
        BIOME_WEIGHTS.put(BLOOD_ICE_SHALLOWS, BLOOD_ICE_SHALLOWS_POOL);
        BIOME_WEIGHTS.put(ASTRAL_SKERRIES, ASTRAL_SKERRIES_POOL);
        // --- SCORCHED & SANDY ---
        BIOME_WEIGHTS.put(BiomeTags.IS_NETHER, new WeightedList<EssenceType>()
                .add(EssenceType.NETHER, 75).add(EssenceType.EARTH, 10).add(EssenceType.MAGMA, 10));

        BIOME_WEIGHTS.put(IS_DESERT, new WeightedList<EssenceType>()
                .add(EssenceType.EARTH, 35).add(EssenceType.AIR, 40).add(EssenceType.ARID, 10).add(EssenceType.DUST, 10));

        BIOME_WEIGHTS.put(IS_BADLANDS, new WeightedList<EssenceType>()
                .add(EssenceType.EARTH, 50).add(EssenceType.AIR, 20).add(EssenceType.ARID, 10).add(EssenceType.DUST, 10).add(EssenceType.NETHER, 5));

        // --- TEMPERATE & OVERGROWN ---
        BIOME_WEIGHTS.put(IS_PLAINS, new WeightedList<EssenceType>()
                .add(EssenceType.NATURE, 50).add(EssenceType.AIR, 30).add(EssenceType.EARTH, 10).add(EssenceType.RADIANT, 5));

        BIOME_WEIGHTS.put(BiomeTags.IS_FOREST, new WeightedList<EssenceType>()
                .add(EssenceType.NATURE, 60).add(EssenceType.EARTH, 20).add(EssenceType.OVERGROWTH, 10).add(EssenceType.SPORE, 5));

        BIOME_WEIGHTS.put(IS_DARK_FOREST, new WeightedList<EssenceType>()
                .add(EssenceType.UMBRAL, 45).add(EssenceType.NATURE, 30).add(EssenceType.SPORE, 20));

        BIOME_WEIGHTS.put(BiomeTags.IS_JUNGLE, new WeightedList<EssenceType>()
                .add(EssenceType.NATURE, 40).add(EssenceType.WATER, 30).add(EssenceType.OVERGROWTH, 20).add(EssenceType.SPORE, 5));

        // --- COLD & HIGH ALTITUDE ---
        BIOME_WEIGHTS.put(IS_SNOWY, new WeightedList<EssenceType>()
                .add(EssenceType.FROZEN, 60).add(EssenceType.EARTH, 20).add(EssenceType.TAIGA, 10).add(EssenceType.GLACIAL, 5));

        BIOME_WEIGHTS.put(BiomeTags.IS_MOUNTAIN, new WeightedList<EssenceType>()
                .add(EssenceType.AIR, 45).add(EssenceType.EARTH, 30).add(EssenceType.FROZEN, 10).add(EssenceType.LIGHTNING, 10));

        // --- WET & SUNKEN ---
        BIOME_WEIGHTS.put(BiomeTags.IS_OCEAN, new WeightedList<EssenceType>()
                .add(EssenceType.WATER, 70).add(EssenceType.AIR, 10).add(EssenceType.STORM, 10).add(EssenceType.REGULAR, 5));

        BIOME_WEIGHTS.put(IS_SWAMP, new WeightedList<EssenceType>()
                .add(EssenceType.WATER, 40).add(EssenceType.NATURE, 30).add(EssenceType.UNDEAD, 15).add(EssenceType.SPORE, 10));

        // --- UNDERGROUND ---
        BIOME_WEIGHTS.put(IS_CAVE, new WeightedList<EssenceType>()
                .add(EssenceType.EARTH, 75).add(EssenceType.REGULAR, 10).add(EssenceType.UMBRAL, 10));

        BIOME_WEIGHTS.put(BiomeTags.HAS_ANCIENT_CITY, new WeightedList<EssenceType>()
                .add(EssenceType.UMBRAL, 60).add(EssenceType.EARTH, 20).add(EssenceType.VOID, 15));

        // --- THE ANOMALOUS ---
        BIOME_WEIGHTS.put(BiomeTags.IS_END, new WeightedList<EssenceType>()
                .add(EssenceType.VOID, 95));
    }

    public static EssenceType getEssenceForBiome(Holder<Biome> biomeHolder, RandomSource random) {
        return rollEssenceForBiome(biomeHolder, random);
    }

    public static EssenceType rollEssenceForBiome(Holder<Biome> biomeHolder, RandomSource random) {
        // 5% Anomaly Chance intercepts the spawn completely
        if (random.nextFloat() < 0.05f) {
            return ANOMALY_POOL.get(random.nextInt(ANOMALY_POOL.size()));
        }

        // Dedicated Entropica Biome Pools (evaluated prior to broad climate tags)
        if (biomeHolder.is(BLUE_GLACIER_SHELF)) {
            return BIOME_WEIGHTS.getOrDefault(BLUE_GLACIER_SHELF, BLUE_GLACIER_SHELF_POOL).getRandomValue(random, EssenceType.GLACIAL);
        }
        if (biomeHolder.is(AURORAL_SKERRIES)) {
            return BIOME_WEIGHTS.getOrDefault(AURORAL_SKERRIES, AURORAL_SKERRIES_POOL).getRandomValue(random, EssenceType.AURORA);
        }
        if (biomeHolder.is(FROST_BITE_FLOES)) {
            return BIOME_WEIGHTS.getOrDefault(FROST_BITE_FLOES, FROST_BITE_FLOES_POOL).getRandomValue(random, EssenceType.GLACIAL);
        }
        if (biomeHolder.is(CRYSTALLINE_PACK_ICE)) {
            return BIOME_WEIGHTS.getOrDefault(CRYSTALLINE_PACK_ICE, CRYSTALLINE_PACK_ICE_POOL).getRandomValue(random, EssenceType.AEGIS);
        }
        if (biomeHolder.is(SILENT_FJORD)) {
            return BIOME_WEIGHTS.getOrDefault(SILENT_FJORD, SILENT_FJORD_POOL).getRandomValue(random, EssenceType.ECLIPSE);
        }
        if (biomeHolder.is(STEAMING_SKERRIES)) {
            return BIOME_WEIGHTS.getOrDefault(STEAMING_SKERRIES, STEAMING_SKERRIES_POOL).getRandomValue(random, EssenceType.VAPOR);
        }
        if (biomeHolder.is(STATIC_ICE_PLAINS)) {
            return BIOME_WEIGHTS.getOrDefault(STATIC_ICE_PLAINS, STATIC_ICE_PLAINS_POOL).getRandomValue(random, EssenceType.LIGHTNING);
        }
        if (biomeHolder.is(BARROW_MOUND_TUNDRA)) {
            return BIOME_WEIGHTS.getOrDefault(BARROW_MOUND_TUNDRA, BARROW_MOUND_TUNDRA_POOL).getRandomValue(random, EssenceType.BARROW);
        }
        if (biomeHolder.is(NULL_FRINGE_BAY)) {
            return BIOME_WEIGHTS.getOrDefault(NULL_FRINGE_BAY, NULL_FRINGE_BAY_POOL).getRandomValue(random, EssenceType.NULL_U);
        }
        if (biomeHolder.is(RADIANT_GLAZE_PLAINS)) {
            return BIOME_WEIGHTS.getOrDefault(RADIANT_GLAZE_PLAINS, RADIANT_GLAZE_PLAINS_POOL).getRandomValue(random, EssenceType.VITAE);
        }
        if (biomeHolder.is(STORM_SWEPT_ICEBERGS)) {
            return BIOME_WEIGHTS.getOrDefault(STORM_SWEPT_ICEBERGS, STORM_SWEPT_ICEBERGS_POOL).getRandomValue(random, EssenceType.STORM);
        }
        if (biomeHolder.is(SPORE_GLAZED_PACK)) {
            return BIOME_WEIGHTS.getOrDefault(SPORE_GLAZED_PACK, SPORE_GLAZED_PACK_POOL).getRandomValue(random, EssenceType.SPORE);
        }
        if (biomeHolder.is(MIRAGE_GLACIER)) {
            return BIOME_WEIGHTS.getOrDefault(MIRAGE_GLACIER, MIRAGE_GLACIER_POOL).getRandomValue(random, EssenceType.MIRAGE);
        }
        if (biomeHolder.is(OASIS_FLOES)) {
            return BIOME_WEIGHTS.getOrDefault(OASIS_FLOES, OASIS_FLOES_POOL).getRandomValue(random, EssenceType.OASIS);
        }
        if (biomeHolder.is(DUST_CRUSTED_BERGS)) {
            return BIOME_WEIGHTS.getOrDefault(DUST_CRUSTED_BERGS, DUST_CRUSTED_BERGS_POOL).getRandomValue(random, EssenceType.DUST);
        }
        if (biomeHolder.is(BLOOD_ICE_SHALLOWS)) {
            return BIOME_WEIGHTS.getOrDefault(BLOOD_ICE_SHALLOWS, BLOOD_ICE_SHALLOWS_POOL).getRandomValue(random, EssenceType.BLOOD);
        }
        if (biomeHolder.is(ASTRAL_SKERRIES)) {
            return BIOME_WEIGHTS.getOrDefault(ASTRAL_SKERRIES, ASTRAL_SKERRIES_POOL).getRandomValue(random, EssenceType.ASTRAL);
        }

        // Added .getOrDefault protections to ensure we never throw a NullPointerException if a map key is somehow missing
        if (biomeHolder.is(BiomeTags.HAS_ANCIENT_CITY)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.HAS_ANCIENT_CITY, DEFAULT_POOL).getRandomValue(random, EssenceType.UMBRAL);
        if (biomeHolder.is(IS_DARK_FOREST)) return BIOME_WEIGHTS.getOrDefault(IS_DARK_FOREST, DEFAULT_POOL).getRandomValue(random, EssenceType.UMBRAL);
        if (biomeHolder.is(IS_SWAMP)) return BIOME_WEIGHTS.getOrDefault(IS_SWAMP, DEFAULT_POOL).getRandomValue(random, EssenceType.WATER);
        if (biomeHolder.is(IS_CAVE)) return BIOME_WEIGHTS.getOrDefault(IS_CAVE, DEFAULT_POOL).getRandomValue(random, EssenceType.EARTH);
        if (biomeHolder.is(BiomeTags.IS_NETHER)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_NETHER, DEFAULT_POOL).getRandomValue(random, EssenceType.NETHER);
        if (biomeHolder.is(BiomeTags.IS_END)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_END, DEFAULT_POOL).getRandomValue(random, EssenceType.VOID);
        if (biomeHolder.is(IS_BADLANDS)) return BIOME_WEIGHTS.getOrDefault(IS_BADLANDS, DEFAULT_POOL).getRandomValue(random, EssenceType.EARTH);
        if (biomeHolder.is(IS_DESERT)) return BIOME_WEIGHTS.getOrDefault(IS_DESERT, DEFAULT_POOL).getRandomValue(random, EssenceType.AIR);
        if (biomeHolder.is(BiomeTags.IS_JUNGLE)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_JUNGLE, DEFAULT_POOL).getRandomValue(random, EssenceType.NATURE);
        if (biomeHolder.is(IS_SNOWY)) return BIOME_WEIGHTS.getOrDefault(IS_SNOWY, DEFAULT_POOL).getRandomValue(random, EssenceType.FROZEN);
        if (biomeHolder.is(BiomeTags.IS_MOUNTAIN)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_MOUNTAIN, DEFAULT_POOL).getRandomValue(random, EssenceType.AIR);
        if (biomeHolder.is(BiomeTags.IS_FOREST)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_FOREST, DEFAULT_POOL).getRandomValue(random, EssenceType.NATURE);
        if (biomeHolder.is(IS_PLAINS)) return BIOME_WEIGHTS.getOrDefault(IS_PLAINS, DEFAULT_POOL).getRandomValue(random, EssenceType.NATURE);
        if (biomeHolder.is(BiomeTags.IS_OCEAN)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_OCEAN, DEFAULT_POOL).getRandomValue(random, EssenceType.WATER);

        return DEFAULT_POOL.getRandomValue(random, EssenceType.EARTH);
    }
}