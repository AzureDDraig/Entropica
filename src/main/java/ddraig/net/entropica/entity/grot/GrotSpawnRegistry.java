package ddraig.net.entropica.entity.grot;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrotSpawnRegistry {

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

    public static void initialize() {
        // --- SCORCHED & SANDY ---
        BIOME_WEIGHTS.put(BiomeTags.IS_NETHER, new WeightedList<EssenceType>()
                .add(EssenceType.NETHER, 75).add(EssenceType.EARTH, 10).add(EssenceType.MAGMA, 10));

        BIOME_WEIGHTS.put(Tags.Biomes.IS_DESERT, new WeightedList<EssenceType>()
                .add(EssenceType.EARTH, 35).add(EssenceType.AIR, 40).add(EssenceType.ARID, 10).add(EssenceType.DUST, 10));

        BIOME_WEIGHTS.put(Tags.Biomes.IS_BADLANDS, new WeightedList<EssenceType>()
                .add(EssenceType.EARTH, 50).add(EssenceType.AIR, 20).add(EssenceType.ARID, 10).add(EssenceType.DUST, 10).add(EssenceType.NETHER, 5));

        // --- TEMPERATE & OVERGROWN ---
        BIOME_WEIGHTS.put(Tags.Biomes.IS_PLAINS, new WeightedList<EssenceType>()
                .add(EssenceType.NATURE, 50).add(EssenceType.AIR, 30).add(EssenceType.EARTH, 10).add(EssenceType.RADIANT, 5));

        BIOME_WEIGHTS.put(BiomeTags.IS_FOREST, new WeightedList<EssenceType>()
                .add(EssenceType.NATURE, 60).add(EssenceType.EARTH, 20).add(EssenceType.OVERGROWTH, 10).add(EssenceType.SPORE, 5));

        BIOME_WEIGHTS.put(Tags.Biomes.IS_DARK_FOREST, new WeightedList<EssenceType>()
                .add(EssenceType.UMBRAL, 45).add(EssenceType.NATURE, 30).add(EssenceType.SPORE, 20));

        BIOME_WEIGHTS.put(BiomeTags.IS_JUNGLE, new WeightedList<EssenceType>()
                .add(EssenceType.NATURE, 40).add(EssenceType.WATER, 30).add(EssenceType.OVERGROWTH, 20).add(EssenceType.SPORE, 5));

        // --- COLD & HIGH ALTITUDE ---
        BIOME_WEIGHTS.put(Tags.Biomes.IS_SNOWY, new WeightedList<EssenceType>()
                .add(EssenceType.FROZEN, 60).add(EssenceType.EARTH, 20).add(EssenceType.TAIGA, 10).add(EssenceType.GLACIAL, 5));

        BIOME_WEIGHTS.put(BiomeTags.IS_MOUNTAIN, new WeightedList<EssenceType>()
                .add(EssenceType.AIR, 45).add(EssenceType.EARTH, 30).add(EssenceType.FROZEN, 10).add(EssenceType.LIGHTNING, 10));

        // --- WET & SUNKEN ---
        BIOME_WEIGHTS.put(BiomeTags.IS_OCEAN, new WeightedList<EssenceType>()
                .add(EssenceType.WATER, 70).add(EssenceType.AIR, 10).add(EssenceType.STORM, 10).add(EssenceType.REGULAR, 5));

        BIOME_WEIGHTS.put(Tags.Biomes.IS_SWAMP, new WeightedList<EssenceType>()
                .add(EssenceType.WATER, 40).add(EssenceType.NATURE, 30).add(EssenceType.UNDEAD, 15).add(EssenceType.SPORE, 10));

        // --- UNDERGROUND ---
        BIOME_WEIGHTS.put(Tags.Biomes.IS_CAVE, new WeightedList<EssenceType>()
                .add(EssenceType.EARTH, 75).add(EssenceType.REGULAR, 10).add(EssenceType.UMBRAL, 10));

        BIOME_WEIGHTS.put(BiomeTags.HAS_ANCIENT_CITY, new WeightedList<EssenceType>()
                .add(EssenceType.UMBRAL, 60).add(EssenceType.EARTH, 20).add(EssenceType.VOID, 15));

        // --- THE ANOMALOUS ---
        BIOME_WEIGHTS.put(BiomeTags.IS_END, new WeightedList<EssenceType>()
                .add(EssenceType.VOID, 95));
    }

    public static EssenceType rollEssenceForBiome(Holder<Biome> biomeHolder, RandomSource random) {
        // 5% Anomaly Chance intercepts the spawn completely
        if (random.nextFloat() < 0.05f) {
            return ANOMALY_POOL.get(random.nextInt(ANOMALY_POOL.size()));
        }

        // Added .getOrDefault protections to ensure we never throw a NullPointerException if a map key is somehow missing
        if (biomeHolder.is(BiomeTags.HAS_ANCIENT_CITY)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.HAS_ANCIENT_CITY, DEFAULT_POOL).getRandomValue(random, EssenceType.UMBRAL);
        if (biomeHolder.is(Tags.Biomes.IS_DARK_FOREST)) return BIOME_WEIGHTS.getOrDefault(Tags.Biomes.IS_DARK_FOREST, DEFAULT_POOL).getRandomValue(random, EssenceType.UMBRAL);
        if (biomeHolder.is(Tags.Biomes.IS_SWAMP)) return BIOME_WEIGHTS.getOrDefault(Tags.Biomes.IS_SWAMP, DEFAULT_POOL).getRandomValue(random, EssenceType.WATER);
        if (biomeHolder.is(Tags.Biomes.IS_CAVE)) return BIOME_WEIGHTS.getOrDefault(Tags.Biomes.IS_CAVE, DEFAULT_POOL).getRandomValue(random, EssenceType.EARTH);
        if (biomeHolder.is(BiomeTags.IS_NETHER)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_NETHER, DEFAULT_POOL).getRandomValue(random, EssenceType.NETHER);
        if (biomeHolder.is(BiomeTags.IS_END)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_END, DEFAULT_POOL).getRandomValue(random, EssenceType.VOID);
        if (biomeHolder.is(Tags.Biomes.IS_BADLANDS)) return BIOME_WEIGHTS.getOrDefault(Tags.Biomes.IS_BADLANDS, DEFAULT_POOL).getRandomValue(random, EssenceType.EARTH);
        if (biomeHolder.is(Tags.Biomes.IS_DESERT)) return BIOME_WEIGHTS.getOrDefault(Tags.Biomes.IS_DESERT, DEFAULT_POOL).getRandomValue(random, EssenceType.AIR);
        if (biomeHolder.is(BiomeTags.IS_JUNGLE)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_JUNGLE, DEFAULT_POOL).getRandomValue(random, EssenceType.NATURE);
        if (biomeHolder.is(Tags.Biomes.IS_SNOWY)) return BIOME_WEIGHTS.getOrDefault(Tags.Biomes.IS_SNOWY, DEFAULT_POOL).getRandomValue(random, EssenceType.FROZEN);
        if (biomeHolder.is(BiomeTags.IS_MOUNTAIN)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_MOUNTAIN, DEFAULT_POOL).getRandomValue(random, EssenceType.AIR);
        if (biomeHolder.is(BiomeTags.IS_FOREST)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_FOREST, DEFAULT_POOL).getRandomValue(random, EssenceType.NATURE);
        if (biomeHolder.is(Tags.Biomes.IS_PLAINS)) return BIOME_WEIGHTS.getOrDefault(Tags.Biomes.IS_PLAINS, DEFAULT_POOL).getRandomValue(random, EssenceType.NATURE);
        if (biomeHolder.is(BiomeTags.IS_OCEAN)) return BIOME_WEIGHTS.getOrDefault(BiomeTags.IS_OCEAN, DEFAULT_POOL).getRandomValue(random, EssenceType.WATER);

        return DEFAULT_POOL.getRandomValue(random, EssenceType.EARTH);
    }
}