package ddraig.net.entropica.worldgen.biome;

/**
 * Pure, allocation-free mathematical helpers for continuous spatial and frame-rate-independent
 * temporal biome atmosphere and environment transitions.
 *
 * <p>Contains zero client or OpenGL imports so deterministic verification suites can invoke all
 * transition kernels headlessly.</p>
 */
public final class BiomeTransitionMath {
    /** Half-extent of the 5x5 world-aligned spatial sampling grid ({@code -2..+2} per axis). */
    public static final int KERNEL_GRID_RADIUS = 2;

    /** Number of sample points along one axis ({@code 5}). */
    public static final int KERNEL_AXIS_SIZE = KERNEL_GRID_RADIUS * 2 + 1;

    /** Total number of sample points in the 2D 5x5 neighborhood ({@code 25}). */
    public static final int KERNEL_SAMPLE_COUNT = KERNEL_AXIS_SIZE * KERNEL_AXIS_SIZE;

    /**
     * World-aligned block spacing between adjacent 5x5 kernel lattice nodes ({@code 7} blocks,
     * placing sample centers across roughly {@code -17.5..+17.5} blocks around the camera).
     */
    public static final int KERNEL_STRIDE_BLOCKS = 7;

    /** Block offset inside each stride cell so {@code gx = -1} (-3.5) and {@code gx = 0} (+3.5) straddle 0 symmetrically. */
    public static final int KERNEL_CELL_OFFSET_BLOCKS = 3;

    /**
     * Exact C2 compact-support cutoff distance ({@code 2.5 * stride = 17.5} blocks) at which a
     * lattice node's weight and first/second derivatives vanish as it enters or leaves the 5x5 stencil.
     */
    public static final double KERNEL_CUTOFF_BLOCKS = 2.5D * KERNEL_STRIDE_BLOCKS;

    /** Gaussian standard deviation in blocks controlling spatial blend softness. */
    public static final double KERNEL_SIGMA_BLOCKS = 8.5D;

    private static final double TWO_SIGMA_SQ = 2.0D * KERNEL_SIGMA_BLOCKS * KERNEL_SIGMA_BLOCKS;
    private static final double INV_CUTOFF_SQ = 1.0D / (KERNEL_CUTOFF_BLOCKS * KERNEL_CUTOFF_BLOCKS);
    private static final double CENTER_SHIFT = KERNEL_CELL_OFFSET_BLOCKS + 0.5D;

    private BiomeTransitionMath() {}

    /**
     * Allocation-free primitive block coordinate scalar sampler for 2D spatial blending.
     */
    @FunctionalInterface
    public interface BlockScalarSampler {
        float sample(int blockX, int blockZ);
    }

    /**
     * Clamps {@code value} into {@code [0.0f, 1.0f]}, treating {@code NaN} as {@code 0.0f}.
     */
    public static float clamp01(float value) {
        if (Float.isNaN(value) || value <= 0.0F) {
            return 0.0F;
        }
        return Math.min(1.0F, value);
    }

    /**
     * Clamps {@code value} into {@code [0.0, 1.0]}, treating {@code NaN} as {@code 0.0}.
     */
    public static double clamp01(double value) {
        if (Double.isNaN(value) || value <= 0.0D) {
            return 0.0D;
        }
        return Math.min(1.0D, value);
    }

    /**
     * Classic C1 Hermite smoothstep on {@code [0, 1]}: {@code t^2 * (3 - 2t)}.
     */
    public static float smoothstep(float t) {
        double c = clamp01((double) t);
        return (float) (c * c * (3.0D - 2.0D * c));
    }

    /**
     * Classic C1 Hermite smoothstep mapping {@code x} between {@code edge0} and {@code edge1} into {@code [0, 1]}.
     */
    public static float smoothstep(float edge0, float edge1, float x) {
        if (Math.abs(edge1 - edge0) < 1.0e-7F) {
            return x >= edge1 ? 1.0F : 0.0F;
        }
        return smoothstep((x - edge0) / (edge1 - edge0));
    }

    /**
     * Classic C1 Hermite smoothstep mapping {@code x} between {@code edge0} and {@code edge1} into {@code [0, 1]}.
     */
    public static double smoothstep(double edge0, double edge1, double x) {
        if (Math.abs(edge1 - edge0) < 1.0e-12D) {
            return x >= edge1 ? 1.0D : 0.0D;
        }
        double t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3.0D - 2.0D * t);
    }

    /**
     * Perlin C2 quintic smootherstep on {@code [0, 1]}: {@code t^3 * (t * (t * 6 - 15) + 10)}.
     */
    public static float smootherstep(float t) {
        double c = clamp01((double) t);
        return (float) (c * c * c * (c * (c * 6.0D - 15.0D) + 10.0D));
    }

    /**
     * Perlin C2 quintic smootherstep mapping {@code x} between {@code edge0} and {@code edge1} into {@code [0, 1]}.
     */
    public static float smootherstep(float edge0, float edge1, float x) {
        if (Math.abs(edge1 - edge0) < 1.0e-7F) {
            return x >= edge1 ? 1.0F : 0.0F;
        }
        return smootherstep((x - edge0) / (edge1 - edge0));
    }

    /**
     * Perlin C2 quintic smootherstep mapping {@code x} between {@code edge0} and {@code edge1} into {@code [0, 1]}.
     */
    public static double smootherstep(double edge0, double edge1, double x) {
        if (Math.abs(edge1 - edge0) < 1.0e-12D) {
            return x >= edge1 ? 1.0D : 0.0D;
        }
        double t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
    }

    /**
     * Frame-rate-independent exponential decay interpolation factor:
     * {@code 1 - exp(-max(0, deltaSecondsOrTicks) * smoothingRate)}, clamped to {@code [0, 1]}.
     */
    public static float expDecayFactor(float deltaSecondsOrTicks, float smoothingRate) {
        if (!Float.isFinite(deltaSecondsOrTicks) || !Float.isFinite(smoothingRate)) {
            return 0.0F;
        }
        float dt = Math.max(0.0F, deltaSecondsOrTicks);
        float rate = Math.max(0.0F, smoothingRate);
        if (dt <= 0.0F || rate <= 0.0F) {
            return 0.0F;
        }
        float factor = 1.0F - (float) Math.exp(-((double) dt * (double) rate));
        return clamp01(factor);
    }

    /**
     * Advances {@code current} toward {@code target} using frame-rate-independent exponential smoothing.
     */
    public static float expSmooth(float current, float target, float deltaSecondsOrTicks, float smoothingRate) {
        float alpha = expDecayFactor(deltaSecondsOrTicks, smoothingRate);
        return current + (target - current) * alpha;
    }

    /**
     * Evaluates the C2-continuous windowed 2D Gaussian weight for sub-block offsets {@code (dx, dz)}
     * from {@code (camX, camZ)} to a sample block center {@code (bx + 0.5, bz + 0.5)}.
     *
     * <p>Combines {@code exp(-distSq / (2 * sigma^2))} with cubic boundary tapers so weights and
     * their derivatives vanish identically at the 5x5 stencil boundary, eliminating all grid-shift
     * staircase jumps while preserving strict log-concavity (monotonicity) across sharp borders.</p>
     */
    public static float gaussianWeight2D(double dx, double dz) {
        double dx2 = dx * dx;
        double dz2 = dz * dz;
        double ux = 1.0D - dx2 * INV_CUTOFF_SQ;
        double uz = 1.0D - dz2 * INV_CUTOFF_SQ;
        if (ux <= 0.0D || uz <= 0.0D) {
            return 0.0F;
        }
        double distSq = dx2 + dz2;
        double taperX = ux * ux * ux;
        double taperZ = uz * uz * uz;
        double gaussian = Math.exp(-distSq / TWO_SIGMA_SQ);
        return (float) (gaussian * taperX * taperZ);
    }

    /**
     * Populates the 25 world-aligned sample block coordinates and normalized continuous 2D Gaussian
     * weights (summing to {@code 1.0f}) around fractional camera coordinates {@code (camX, camZ)}
     * with zero heap allocations.
     *
     * @param camX                   continuous X coordinate (e.g. {@code camera.getPosition().x})
     * @param camZ                   continuous Z coordinate (e.g. {@code camera.getPosition().z})
     * @param outBlockX25            output array of length {@code >= 25} receiving integer block X coordinates
     * @param outBlockZ25            output array of length {@code >= 25} receiving integer block Z coordinates
     * @param outNormalizedWeights25 output array of length {@code >= 25} receiving normalized weights in {@code [0, 1]}
     */
    public static void computeGaussianGrid5x5(
            double camX,
            double camZ,
            int[] outBlockX25,
            int[] outBlockZ25,
            float[] outNormalizedWeights25) {
        int centerGx = (int) Math.floor((camX - CENTER_SHIFT) / KERNEL_STRIDE_BLOCKS + 0.5D);
        int centerGz = (int) Math.floor((camZ - CENTER_SHIFT) / KERNEL_STRIDE_BLOCKS + 0.5D);

        float weightSum = 0.0F;
        int idx = 0;
        for (int iz = -KERNEL_GRID_RADIUS; iz <= KERNEL_GRID_RADIUS; iz++) {
            int bz = (centerGz + iz) * KERNEL_STRIDE_BLOCKS + KERNEL_CELL_OFFSET_BLOCKS;
            double dz = camZ - (bz + 0.5D);
            for (int ix = -KERNEL_GRID_RADIUS; ix <= KERNEL_GRID_RADIUS; ix++) {
                int bx = (centerGx + ix) * KERNEL_STRIDE_BLOCKS + KERNEL_CELL_OFFSET_BLOCKS;
                double dx = camX - (bx + 0.5D);
                float w = gaussianWeight2D(dx, dz);
                outBlockX25[idx] = bx;
                outBlockZ25[idx] = bz;
                outNormalizedWeights25[idx] = w;
                weightSum += w;
                idx++;
            }
        }

        if (weightSum > 1.0e-8F) {
            float invSum = 1.0F / weightSum;
            for (int i = 0; i < KERNEL_SAMPLE_COUNT; i++) {
                outNormalizedWeights25[i] *= invSum;
            }
        } else {
            outNormalizedWeights25[12] = 1.0F;
        }
    }

    /**
     * Evaluates the normalized continuous 2D Gaussian-weighted spatial average over the 5x5
     * neighborhood around fractional coordinates {@code (camX, camZ)} with zero heap allocations.
     *
     * <p>When {@code sampler} returns {@code 0.0f} for non-effect biomes and {@code 1.0f} for an
     * effect biome, walking across a sharp biome boundary yields a C2-smooth monotonic blend weight
     * in {@code [0.0f, 1.0f]} with zero staircase jumps.</p>
     */
    public static float sampleGaussian2D(double camX, double camZ, BlockScalarSampler sampler) {
        int centerGx = (int) Math.floor((camX - CENTER_SHIFT) / KERNEL_STRIDE_BLOCKS + 0.5D);
        int centerGz = (int) Math.floor((camZ - CENTER_SHIFT) / KERNEL_STRIDE_BLOCKS + 0.5D);

        double weightedValueSum = 0.0D;
        double weightSum = 0.0D;

        for (int iz = -KERNEL_GRID_RADIUS; iz <= KERNEL_GRID_RADIUS; iz++) {
            int bz = (centerGz + iz) * KERNEL_STRIDE_BLOCKS + KERNEL_CELL_OFFSET_BLOCKS;
            double dz = camZ - (bz + 0.5D);
            double dz2 = dz * dz;
            double uz = 1.0D - dz2 * INV_CUTOFF_SQ;
            if (uz <= 0.0D) {
                continue;
            }
            double taperZ = uz * uz * uz;

            for (int ix = -KERNEL_GRID_RADIUS; ix <= KERNEL_GRID_RADIUS; ix++) {
                int bx = (centerGx + ix) * KERNEL_STRIDE_BLOCKS + KERNEL_CELL_OFFSET_BLOCKS;
                double dx = camX - (bx + 0.5D);
                double dx2 = dx * dx;
                double ux = 1.0D - dx2 * INV_CUTOFF_SQ;
                if (ux <= 0.0D) {
                    continue;
                }
                double taperX = ux * ux * ux;
                double w = Math.exp(-(dx2 + dz2) / TWO_SIGMA_SQ) * taperX * taperZ;
                if (w > 0.0D) {
                    weightedValueSum += w * (double) sampler.sample(bx, bz);
                    weightSum += w;
                }
            }
        }

        if (weightSum <= 1.0e-12D) {
            return sampler.sample((int) Math.floor(camX), (int) Math.floor(camZ));
        }
        return (float) (weightedValueSum / weightSum);
    }

    /**
     * Convenience wrapper applying {@link #smootherstep(float)} on top of a {@code [0, 1]} presence
     * weight sampled via {@link #sampleGaussian2D(double, double, BlockScalarSampler)}.
     */
    public static float sampleGaussianBlend01(double camX, double camZ, BlockScalarSampler presenceSampler) {
        return smootherstep(clamp01(sampleGaussian2D(camX, camZ, presenceSampler)));
    }
}
