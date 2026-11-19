package ddraig.net.entropica.worldgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Immutable, lock-free, zero-allocation multi-noise biome overlay layer attached to a
 * {@link net.minecraft.world.level.biome.MultiNoiseBiomeSource} instance for a specific world seed.
 *
 * <p>Evaluates 6D {@link Climate.TargetPoint} matches across primitive-packed climate spans,
 * resolves regional overlaps via Exponential Weighted Rendezvous Hashing, and cooperates
 * cleanly with TerraBlender according to {@link TerraBlenderCompatMode}.</p>
 */
public final class EntropicaMultiNoiseLayer {
    public record ResolvedRule(BiomePlacementRule rule, Holder<Biome> holder) {}

    private final DomainWarpedRegionSampler regionSampler;
    private final int entropicaRegionWeight;
    private final int vanillaPassthroughWeight;
    private final boolean terraBlenderLoaded;
    private final boolean baseAlreadyContainsEntropicaBiomes;
    private final TerraBlenderCompatMode compatMode;
    private final List<Holder<Biome>> activeBiomes;

    // Contiguous per-biome arrays (length = biomeCount)
    private final int biomeCount;
    private final Holder<Biome>[] biomeHolders;
    private final long[] biomeSalts;
    private final int[] biomeWeights;
    private final Predicate<Holder<Biome>>[] baseFilters;
    private final int[] biomePointStart;
    private final int[] biomePointEnd;

    // Flat primitive 6D climate point arrays (length = totalPointCount)
    private final long[] minT, maxT;
    private final long[] minH, maxH;
    private final long[] minC, maxC;
    private final long[] minE, maxE;
    private final long[] minD, maxD;
    private final long[] minW, maxW;
    private final long[] offsetSq;
    private final long[] maxFitnessDistSq;

    @SuppressWarnings("unchecked")
    public EntropicaMultiNoiseLayer(
            long worldSeed,
            int regionScale,
            int entropicaRegionWeight,
            int vanillaPassthroughWeight,
            boolean terraBlenderLoaded,
            boolean baseAlreadyContainsEntropicaBiomes,
            TerraBlenderCompatMode compatMode,
            List<ResolvedRule> resolvedRules) {
        this.regionSampler = new DomainWarpedRegionSampler(worldSeed, regionScale);
        this.entropicaRegionWeight = Math.max(0, entropicaRegionWeight);
        this.vanillaPassthroughWeight = Math.max(0, vanillaPassthroughWeight);
        this.terraBlenderLoaded = terraBlenderLoaded;
        this.baseAlreadyContainsEntropicaBiomes = baseAlreadyContainsEntropicaBiomes;
        this.compatMode = compatMode != null ? compatMode : TerraBlenderCompatMode.COOPERATE;

        this.biomeCount = resolvedRules.size();
        this.biomeHolders = (Holder<Biome>[]) new Holder<?>[biomeCount];
        this.biomeSalts = new long[biomeCount];
        this.biomeWeights = new int[biomeCount];
        this.baseFilters = (Predicate<Holder<Biome>>[]) new Predicate<?>[biomeCount];
        this.biomePointStart = new int[biomeCount];
        this.biomePointEnd = new int[biomeCount];

        List<Holder<Biome>> holdersList = new ArrayList<>(biomeCount);
        int totalPoints = 0;
        for (ResolvedRule rr : resolvedRules) {
            totalPoints += rr.rule().getParameterPoints().size();
        }

        this.minT = new long[totalPoints];
        this.maxT = new long[totalPoints];
        this.minH = new long[totalPoints];
        this.maxH = new long[totalPoints];
        this.minC = new long[totalPoints];
        this.maxC = new long[totalPoints];
        this.minE = new long[totalPoints];
        this.maxE = new long[totalPoints];
        this.minD = new long[totalPoints];
        this.maxD = new long[totalPoints];
        this.minW = new long[totalPoints];
        this.maxW = new long[totalPoints];
        this.offsetSq = new long[totalPoints];
        this.maxFitnessDistSq = new long[totalPoints];

        int cursor = 0;
        for (int b = 0; b < biomeCount; b++) {
            ResolvedRule rr = resolvedRules.get(b);
            BiomePlacementRule rule = rr.rule();
            this.biomeHolders[b] = rr.holder();
            this.biomeSalts[b] = rule.getBiomeSeedSalt();
            this.biomeWeights[b] = rule.getRarityWeight();
            this.baseFilters[b] = rule.getBaseBiomeFilter();
            this.biomePointStart[b] = cursor;
            holdersList.add(rr.holder());

            for (Climate.ParameterPoint pt : rule.getParameterPoints()) {
                this.minT[cursor] = pt.temperature().min();
                this.maxT[cursor] = pt.temperature().max();
                this.minH[cursor] = pt.humidity().min();
                this.maxH[cursor] = pt.humidity().max();
                this.minC[cursor] = pt.continentalness().min();
                this.maxC[cursor] = pt.continentalness().max();
                this.minE[cursor] = pt.erosion().min();
                this.maxE[cursor] = pt.erosion().max();
                this.minD[cursor] = pt.depth().min();
                this.maxD[cursor] = pt.depth().max();
                this.minW[cursor] = pt.weirdness().min();
                this.maxW[cursor] = pt.weirdness().max();
                long off = pt.offset();
                this.offsetSq[cursor] = off * off;
                this.maxFitnessDistSq[cursor] = rule.getMaxFitnessDistanceSq();
                cursor++;
            }
            this.biomePointEnd[b] = cursor;
        }

        this.activeBiomes = Collections.unmodifiableList(holdersList);
    }

    public List<Holder<Biome>> activeBiomes() {
        return activeBiomes;
    }

    public boolean isEmpty() {
        return biomeCount == 0;
    }

    /**
     * Resolves the final biome at quart coordinates (quartX, quartY, quartZ),
     * respecting TerraBlender compatibility mode, organic region pass-through weights,
     * 6D multi-noise matching, and weighted Rendezvous rarity selection.
     */
    public Holder<Biome> resolveBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler, Holder<Biome> baseBiome) {
        if (biomeCount == 0 || sampler == null) {
            return baseBiome;
        }

        // 1. TerraBlender Cooperation & Deferral Policy
        if (shouldYieldToBaseBiome(baseBiome)) {
            return baseBiome;
        }

        // 2. Seeded 2D Organic Domain-Warped Region Gate (evaluated BEFORE sampling 6D climate)
        long cellHash = regionSampler.sampleRegionCellHash(quartX, quartZ);
        if (!DomainWarpedRegionSampler.isEntropicaActiveCell(cellHash, entropicaRegionWeight, vanillaPassthroughWeight)) {
            return baseBiome;
        }

        // 3. Sample 6D Climate TargetPoint and match against active Entropica biome spans
        Climate.TargetPoint target = sampler.sample(quartX, quartY, quartZ);
        return selectMatchingBiome(cellHash, target, baseBiome);
    }

    /**
     * Pure-logic TerraBlender yield check exposed for deterministic unit verification.
     */
    public boolean shouldYieldToBaseBiome(Holder<Biome> baseBiome) {
        if (!terraBlenderLoaded) {
            return false;
        }
        if (compatMode == TerraBlenderCompatMode.YIELD_TO_TERRABLENDER) {
            return true;
        }
        if (baseAlreadyContainsEntropicaBiomes && compatMode != TerraBlenderCompatMode.OVERLAY_ON_TOP) {
            return true;
        }
        if (compatMode == TerraBlenderCompatMode.COOPERATE && baseBiome != null) {
            Optional<ResourceKey<Biome>> keyOpt = baseBiome.unwrapKey();
            if (keyOpt.isPresent()) {
                String namespace = keyOpt.get().location().getNamespace();
                // Give way to any modded biome placed by TerraBlender (Biomes O' Plenty, Regions Unexplored, etc.)
                if (!"minecraft".equals(namespace)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Evaluates all active biomes against a 6D {@link Climate.TargetPoint} and resolves overlaps
     * deterministically for {@code cellHash} via Exponential Weighted Rendezvous Hashing.
     */
    public Holder<Biome> selectMatchingBiome(long cellHash, Climate.TargetPoint target, Holder<Biome> fallback) {
        long t = target.temperature();
        long h = target.humidity();
        long c = target.continentalness();
        long e = target.erosion();
        long d = target.depth();
        long w = target.weirdness();

        Holder<Biome> winner = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (int b = 0; b < biomeCount; b++) {
            Predicate<Holder<Biome>> filter = baseFilters[b];
            if (filter != null && baseBiomeRejected(filter, fallback)) {
                continue;
            }

            int start = biomePointStart[b];
            int end = biomePointEnd[b];
            boolean matched = false;

            for (int p = start; p < end; p++) {
                long maxDist = maxFitnessDistSq[p];
                if (maxDist == 0L) {
                    // Fast exact 6D box containment branch (zero multiplications)
                    if (t >= minT[p] && t <= maxT[p]
                            && h >= minH[p] && h <= maxH[p]
                            && c >= minC[p] && c <= maxC[p]
                            && e >= minE[p] && e <= maxE[p]
                            && d >= minD[p] && d <= maxD[p]
                            && w >= minW[p] && w <= maxW[p]) {
                        matched = true;
                        break;
                    }
                } else {
                    long distSq = axisDistSq(t, minT[p], maxT[p])
                            + axisDistSq(h, minH[p], maxH[p])
                            + axisDistSq(c, minC[p], maxC[p])
                            + axisDistSq(e, minE[p], maxE[p])
                            + axisDistSq(d, minD[p], maxD[p])
                            + axisDistSq(w, minW[p], maxW[p])
                            + offsetSq[p];
                    if (distSq <= maxDist) {
                        matched = true;
                        break;
                    }
                }
            }

            if (matched) {
                double score = DomainWarpedRegionSampler.rendezvousScore(cellHash, biomeSalts[b], biomeWeights[b]);
                if (score < bestScore) {
                    bestScore = score;
                    winner = biomeHolders[b];
                }
            }
        }

        return winner != null ? winner : fallback;
    }

    private static boolean baseBiomeRejected(Predicate<Holder<Biome>> filter, Holder<Biome> baseBiome) {
        return baseBiome != null && !filter.test(baseBiome);
    }

    private static long axisDistSq(long val, long min, long max) {
        if (val < min) {
            long diff = min - val;
            return diff * diff;
        }
        if (val > max) {
            long diff = val - max;
            return diff * diff;
        }
        return 0L;
    }
}
