package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.config.EntropicaBiome;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Immutable multi-noise placement specification for a single {@link EntropicaBiome}.
 */
public final class BiomePlacementRule {
    private final EntropicaBiome biome;
    private final ResourceKey<LevelStem> dimensionStem;
    private final List<Climate.ParameterPoint> parameterPoints;
    private final int rarityWeight;
    private final long maxFitnessDistanceSq;
    private final Predicate<Holder<Biome>> baseBiomeFilter;
    private final Supplier<SurfaceRules.RuleSource> surfaceRuleSupplier;
    private volatile SurfaceRules.RuleSource cachedSurfaceRule;
    private final long biomeSeedSalt;

    private BiomePlacementRule(
            EntropicaBiome biome,
            ResourceKey<LevelStem> dimensionStem,
            List<Climate.ParameterPoint> parameterPoints,
            int rarityWeight,
            long maxFitnessDistanceSq,
            Predicate<Holder<Biome>> baseBiomeFilter,
            Supplier<SurfaceRules.RuleSource> surfaceRuleSupplier) {
        this.biome = Objects.requireNonNull(biome, "biome");
        this.dimensionStem = Objects.requireNonNull(dimensionStem, "dimensionStem");
        if (parameterPoints == null || parameterPoints.isEmpty()) {
            throw new IllegalArgumentException("BiomePlacementRule for " + biome.getId() + " requires at least 1 Climate.ParameterPoint");
        }
        this.parameterPoints = Collections.unmodifiableList(new ArrayList<>(parameterPoints));
        this.rarityWeight = Math.max(1, rarityWeight);
        this.maxFitnessDistanceSq = Math.max(0L, maxFitnessDistanceSq);
        this.baseBiomeFilter = baseBiomeFilter;
        this.surfaceRuleSupplier = surfaceRuleSupplier;
        this.biomeSeedSalt = computeStableSalt(biome.getId());
    }

    public static int defaultWeightForRarity(String rarity) {
        if (rarity == null) return 4;
        return switch (rarity.trim().toLowerCase()) {
            case "common" -> 8;
            case "uncommon" -> 5;
            case "rare" -> 3;
            case "legendary" -> 1;
            default -> 4;
        };
    }

    public static ResourceKey<LevelStem> defaultStemForDimension(String dimensionLabel) {
        if (dimensionLabel != null && dimensionLabel.toLowerCase().contains("nether")) {
            return LevelStem.NETHER;
        }
        return LevelStem.OVERWORLD;
    }

    private static long computeStableSalt(String id) {
        long h = 0xCBF29CE484222325L;
        byte[] bytes = id.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            h ^= (b & 0xFFL);
            h *= 0x100000001B3L;
        }
        return DomainWarpedRegionSampler.mix64(h);
    }

    public EntropicaBiome getBiome() {
        return biome;
    }

    public ResourceKey<LevelStem> getDimensionStem() {
        return dimensionStem;
    }

    public List<Climate.ParameterPoint> getParameterPoints() {
        return parameterPoints;
    }

    public int getRarityWeight() {
        return rarityWeight;
    }

    public long getMaxFitnessDistanceSq() {
        return maxFitnessDistanceSq;
    }

    public Predicate<Holder<Biome>> getBaseBiomeFilter() {
        return baseBiomeFilter;
    }

    public SurfaceRules.RuleSource getSurfaceRule() {
        if (surfaceRuleSupplier == null) {
            return null;
        }
        SurfaceRules.RuleSource local = cachedSurfaceRule;
        if (local == null) {
            SurfaceRules.RuleSource built = surfaceRuleSupplier.get();
            if (built != null) {
                local = SurfaceRules.ifTrue(SurfaceRules.isBiome(biome.resourceKey()), built);
                cachedSurfaceRule = local;
            }
        }
        return local;
    }

    public long getBiomeSeedSalt() {
        return biomeSeedSalt;
    }

    public static Builder builder(EntropicaBiome biome) {
        return new Builder(biome);
    }

    public static final class Builder {
        private final EntropicaBiome biome;
        private ResourceKey<LevelStem> dimensionStem;
        private final List<Climate.ParameterPoint> points = new ArrayList<>();
        private int rarityWeight;
        private long maxFitnessDistanceSq = 0L;
        private Predicate<Holder<Biome>> baseBiomeFilter = null;
        private Supplier<SurfaceRules.RuleSource> surfaceRuleSupplier = null;

        private Builder(EntropicaBiome biome) {
            this.biome = Objects.requireNonNull(biome, "biome");
            this.dimensionStem = defaultStemForDimension(biome.getDimension());
            this.rarityWeight = defaultWeightForRarity(biome.getRarity());
        }

        public Builder dimension(ResourceKey<LevelStem> dimensionStem) {
            this.dimensionStem = Objects.requireNonNull(dimensionStem, "dimensionStem");
            return this;
        }

        public Builder addParameterPoint(Climate.ParameterPoint point) {
            this.points.add(Objects.requireNonNull(point, "point"));
            return this;
        }

        public Builder rarityWeight(int rarityWeight) {
            this.rarityWeight = Math.max(1, rarityWeight);
            return this;
        }

        /**
         * Optional tolerance radius in floating climate units (0.0f = strict 6D box match).
         */
        public Builder climateTolerance(float tolerance) {
            if (tolerance <= 0.0F) {
                this.maxFitnessDistanceSq = 0L;
            } else {
                long q = Climate.quantizeCoord(tolerance);
                this.maxFitnessDistanceSq = q * q;
            }
            return this;
        }

        public Builder baseBiomeFilter(Predicate<Holder<Biome>> filter) {
            this.baseBiomeFilter = filter;
            return this;
        }

        /**
         * Registers a biome-specific SurfaceRule (automatically scoped via SurfaceRules.isBiome).
         */
        public Builder surfaceRule(SurfaceRules.RuleSource surfaceRule) {
            this.surfaceRuleSupplier = surfaceRule != null ? () -> surfaceRule : null;
            return this;
        }

        /**
         * Registers a lazy biome-specific SurfaceRule supplier (safe for ModBlocks.*.get() before RegisterEvent).
         */
        public Builder surfaceRule(Supplier<SurfaceRules.RuleSource> surfaceRuleSupplier) {
            this.surfaceRuleSupplier = surfaceRuleSupplier;
            return this;
        }

        public BiomePlacementRule build() {
            return new BiomePlacementRule(
                    biome,
                    dimensionStem,
                    points,
                    rarityWeight,
                    maxFitnessDistanceSq,
                    baseBiomeFilter,
                    surfaceRuleSupplier
            );
        }
    }
}
