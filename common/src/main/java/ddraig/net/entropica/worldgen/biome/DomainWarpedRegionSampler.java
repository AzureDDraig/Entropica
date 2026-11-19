package ddraig.net.entropica.worldgen.biome;

/**
 * Thread-safe, lock-free, zero-allocation 2D domain-warped Voronoi region sampler
 * and Exponential Weighted Rendezvous Hashing (HRW) selector.
 *
 * <p>Produces smooth, organically curved regional territories from the world seed
 * without straight grid seams or salt-and-pepper biome noise.</p>
 */
public final class DomainWarpedRegionSampler {
    private static final double TWO_PI = Math.PI * 2.0;
    private static final long WARP_SALT_X1 = 0x6A09E667F3BCC908L;
    private static final long WARP_SALT_Z1 = 0xBB67AE8584CAA73BL;
    private static final long WARP_SALT_X2 = 0x3C6EF372FE94F82BL;
    private static final long WARP_SALT_Z2 = 0xA54FF53A5F1D36F1L;
    private static final long CELL_SALT = 0x510E527FADE682D1L;
    private static final long ACTIVE_ROLL_SALT = 0x517CC1B727220A95L;

    private final long seed;
    private final double cellSize;
    private final double invCellSize;
    private final double warpFreq1;
    private final double warpAmp1;
    private final double warpFreq2;
    private final double warpAmp2;

    public DomainWarpedRegionSampler(long worldSeed, int regionScale) {
        int clampedScale = Math.max(2, Math.min(12, regionScale));
        this.seed = mix64(worldSeed ^ 0x454E54524F504943L); // "ENTROPIC"
        this.cellSize = (double) (1 << clampedScale);
        this.invCellSize = 1.0 / this.cellSize;
        this.warpFreq1 = 1.0 / (this.cellSize * 1.25);
        this.warpAmp1 = this.cellSize * 0.42;
        this.warpFreq2 = 1.0 / (this.cellSize * 0.50);
        this.warpAmp2 = this.cellSize * 0.16;
    }

    /**
     * Samples the deterministic 64-bit region cell hash at quart coordinates (quartX, quartZ).
     * Allocates zero heap objects and is safe for concurrent chunk generation threads.
     */
    public long sampleRegionCellHash(int quartX, int quartZ) {
        double x = quartX;
        double z = quartZ;

        double dx = valueNoise2D(x * warpFreq1, z * warpFreq1, seed ^ WARP_SALT_X1) * warpAmp1
                + valueNoise2D(x * warpFreq2, z * warpFreq2, seed ^ WARP_SALT_X2) * warpAmp2;
        double dz = valueNoise2D(x * warpFreq1, z * warpFreq1, seed ^ WARP_SALT_Z1) * warpAmp1
                + valueNoise2D(x * warpFreq2, z * warpFreq2, seed ^ WARP_SALT_Z2) * warpAmp2;

        double wx = (x + dx) * invCellSize;
        double wz = (z + dz) * invCellSize;

        long baseCx = fastFloor(wx);
        long baseCz = fastFloor(wz);

        long bestCx = baseCx;
        long bestCz = baseCz;
        double bestDistSq = Double.MAX_VALUE;

        for (int ox = -1; ox <= 1; ox++) {
            long cx = baseCx + ox;
            for (int oz = -1; oz <= 1; oz++) {
                long cz = baseCz + oz;
                long h = hash2D(cx, cz, seed ^ CELL_SALT);
                // Jitter in [0.15, 0.85] per axis for organic Voronoi cells without degenerate slivers
                double jx = 0.15 + 0.70 * unitDoubleFromLow32(h);
                double jz = 0.15 + 0.70 * unitDoubleFromLow32(h >>> 32);
                double px = cx + jx;
                double pz = cz + jz;
                double ddx = wx - px;
                double ddz = wz - pz;
                double distSq = ddx * ddx + ddz * ddz;
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    bestCx = cx;
                    bestCz = cz;
                }
            }
        }

        return hash2D(bestCx, bestCz, seed);
    }

    /**
     * Determines whether a Voronoi region cell allows Entropica multi-noise biomes
     * or passes straight through to Vanilla / TerraBlender.
     */
    public static boolean isEntropicaActiveCell(long cellHash, int entropicaWeight, int passthroughWeight) {
        if (entropicaWeight <= 0) {
            return false;
        }
        if (passthroughWeight <= 0) {
            return true;
        }
        int total = entropicaWeight + passthroughWeight;
        long mixed = mix64(cellHash ^ ACTIVE_ROLL_SALT);
        int roll = (int) Long.remainderUnsigned(mixed, total);
        return roll < entropicaWeight;
    }

    /**
     * Computes an Exponential Weighted Rendezvous (HRW) score where LOWER score wins.
     * Among any competing rules {i} with weights {w_i}, rule i wins with exact probability
     * w_i / sum(w_j), while staying 100% constant across an entire Voronoi region cell.
     */
    public static double rendezvousScore(long cellHash, long biomeSalt, int weight) {
        int safeWeight = Math.max(1, weight);
        long h = mix64(cellHash ^ biomeSalt);
        // Map upper 53 bits strictly inside (0, 1) so Math.log(u) is always finite and negative
        double u = ((h >>> 11) + 0.5) * 0x1.0p-53;
        return -Math.log(u) / (double) safeWeight;
    }

    /**
     * Smooth quintic-interpolated 2D value noise in [-1.0, 1.0].
     */
    private static double valueNoise2D(double x, double z, long saltSeed) {
        long x0 = fastFloor(x);
        long z0 = fastFloor(z);
        double fx = x - x0;
        double fz = z - z0;

        double ux = quinticFade(fx);
        double uz = quinticFade(fz);

        double v00 = signedDouble(hash2D(x0, z0, saltSeed));
        double v10 = signedDouble(hash2D(x0 + 1, z0, saltSeed));
        double v01 = signedDouble(hash2D(x0, z0 + 1, saltSeed));
        double v11 = signedDouble(hash2D(x0 + 1, z0 + 1, saltSeed));

        double nx0 = v00 + ux * (v10 - v00);
        double nx1 = v01 + ux * (v11 - v01);
        return nx0 + uz * (nx1 - nx0);
    }

    private static double quinticFade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static long fastFloor(double d) {
        long i = (long) d;
        return d < i ? i - 1L : i;
    }

    private static double unitDoubleFromLow32(long bits) {
        return ((bits & 0xFFFFFFFFL) + 0.5) / 4294967296.0;
    }

    private static double signedDouble(long h) {
        return ((h >>> 11) * 0x1.0p-52) - 1.0;
    }

    public static long hash2D(long x, long z, long seed) {
        long h = seed;
        h ^= x * 0x9E3779B97F4A7C15L;
        h = mix64(h);
        h ^= z * 0xBF58476D1CE4E5B9L;
        return mix64(h);
    }

    public static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
