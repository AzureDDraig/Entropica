package ddraig.net.entropica.worldgen.biome;

import net.minecraft.world.level.biome.Climate;

/**
 * Vanilla-aligned 6D Multi-Noise Climate Parameter presets and fluent builder
 * for mapping Entropica's 108 biome specifications (Temperature, Humidity,
 * Continentalness, Erosion, Depth, Weirdness, Offset) into {@link Climate.ParameterPoint}s.
 */
public final class EntropicaClimatePresets {
    private EntropicaClimatePresets() {}

    public static final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);

    // --- Temperature Bands ---
    public static final Climate.Parameter TEMP_ICY = Climate.Parameter.span(-1.0F, -0.45F);
    public static final Climate.Parameter TEMP_COLD = Climate.Parameter.span(-0.45F, -0.15F);
    public static final Climate.Parameter TEMP_TEMPERATE = Climate.Parameter.span(-0.15F, 0.20F);
    public static final Climate.Parameter TEMP_WARM = Climate.Parameter.span(0.20F, 0.55F);
    public static final Climate.Parameter TEMP_HOT = Climate.Parameter.span(0.55F, 1.0F);

    // --- Humidity Bands ---
    public static final Climate.Parameter HUMIDITY_ARID = Climate.Parameter.span(-1.0F, -0.35F);
    public static final Climate.Parameter HUMIDITY_DRY = Climate.Parameter.span(-0.35F, -0.10F);
    public static final Climate.Parameter HUMIDITY_NEUTRAL = Climate.Parameter.span(-0.10F, 0.10F);
    public static final Climate.Parameter HUMIDITY_WET = Climate.Parameter.span(0.10F, 0.30F);
    public static final Climate.Parameter HUMIDITY_HUMID = Climate.Parameter.span(0.30F, 1.0F);

    // --- Continentalness Bands ---
    public static final Climate.Parameter CONT_DEEP_OCEAN = Climate.Parameter.span(-1.05F, -0.455F);
    public static final Climate.Parameter CONT_OCEAN = Climate.Parameter.span(-0.455F, -0.19F);
    public static final Climate.Parameter CONT_COAST = Climate.Parameter.span(-0.19F, -0.11F);
    /** Coastal shelf transition dividing freezing/open ocean waters from steep coastal cliffs. */
    public static final Climate.Parameter CONT_COASTAL_SHELF = Climate.Parameter.span(-0.22F, 0.08F);
    public static final Climate.Parameter CONT_NEAR_INLAND = Climate.Parameter.span(-0.11F, 0.03F);
    public static final Climate.Parameter CONT_MID_INLAND = Climate.Parameter.span(0.03F, 0.30F);
    public static final Climate.Parameter CONT_FAR_INLAND = Climate.Parameter.span(0.30F, 1.0F);
    public static final Climate.Parameter CONT_INLAND_ALL = Climate.Parameter.span(-0.11F, 1.0F);

    // --- Erosion Bands (Low = jagged peaks/sheer cliffs, High = flat plains/swamps) ---
    public static final Climate.Parameter EROSION_SHEER_CLIFFS = Climate.Parameter.span(-1.0F, -0.375F);
    public static final Climate.Parameter EROSION_PEAKS = Climate.Parameter.span(-1.0F, -0.78F);
    public static final Climate.Parameter EROSION_HIGH_RELIEF = Climate.Parameter.span(-0.78F, -0.375F);
    public static final Climate.Parameter EROSION_HILLS = Climate.Parameter.span(-0.375F, -0.2225F);
    public static final Climate.Parameter EROSION_PLATEAU = Climate.Parameter.span(-0.2225F, 0.05F);
    public static final Climate.Parameter EROSION_PLAINS = Climate.Parameter.span(0.05F, 0.45F);
    public static final Climate.Parameter EROSION_FLATS = Climate.Parameter.span(0.45F, 1.0F);

    // --- Depth Bands (0.0 = surface, >0.2 = underground caves) ---
    public static final Climate.Parameter DEPTH_SURFACE = Climate.Parameter.span(-0.05F, 0.12F);
    public static final Climate.Parameter DEPTH_SURFACE_AND_FLOOR = Climate.Parameter.span(-0.10F, 0.18F);
    public static final Climate.Parameter DEPTH_SHALLOW_CAVE = Climate.Parameter.span(0.20F, 0.55F);
    public static final Climate.Parameter DEPTH_DEEP_CAVE = Climate.Parameter.span(0.55F, 1.10F);
    public static final Climate.Parameter DEPTH_ALL_CAVES = Climate.Parameter.span(0.20F, 1.10F);
    public static final Climate.Parameter DEPTH_NETHER = FULL_RANGE;

    // --- Weirdness Bands ---
    public static final Climate.Parameter WEIRDNESS_VALLEY = Climate.Parameter.span(-0.05F, 0.05F);
    public static final Climate.Parameter WEIRDNESS_HIGH_POSITIVE = Climate.Parameter.span(0.40F, 1.0F);
    public static final Climate.Parameter WEIRDNESS_HIGH_NEGATIVE = Climate.Parameter.span(-1.0F, -0.40F);
    public static final Climate.Parameter WEIRDNESS_MID_POSITIVE = Climate.Parameter.span(0.05F, 0.40F);
    public static final Climate.Parameter WEIRDNESS_MID_NEGATIVE = Climate.Parameter.span(-0.40F, -0.05F);

    public static ParameterBuilder builder() {
        return new ParameterBuilder();
    }

    public static final class ParameterBuilder {
        private Climate.Parameter temperature = FULL_RANGE;
        private Climate.Parameter humidity = FULL_RANGE;
        private Climate.Parameter continentalness = CONT_INLAND_ALL;
        private Climate.Parameter erosion = FULL_RANGE;
        private Climate.Parameter depth = DEPTH_SURFACE;
        private Climate.Parameter weirdness = FULL_RANGE;
        private float offset = 0.0F;

        public ParameterBuilder temperature(Climate.Parameter temperature) {
            this.temperature = temperature;
            return this;
        }

        public ParameterBuilder temperature(float min, float max) {
            this.temperature = Climate.Parameter.span(min, max);
            return this;
        }

        public ParameterBuilder humidity(Climate.Parameter humidity) {
            this.humidity = humidity;
            return this;
        }

        public ParameterBuilder humidity(float min, float max) {
            this.humidity = Climate.Parameter.span(min, max);
            return this;
        }

        public ParameterBuilder continentalness(Climate.Parameter continentalness) {
            this.continentalness = continentalness;
            return this;
        }

        public ParameterBuilder continentalness(float min, float max) {
            this.continentalness = Climate.Parameter.span(min, max);
            return this;
        }

        public ParameterBuilder erosion(Climate.Parameter erosion) {
            this.erosion = erosion;
            return this;
        }

        public ParameterBuilder erosion(float min, float max) {
            this.erosion = Climate.Parameter.span(min, max);
            return this;
        }

        public ParameterBuilder depth(Climate.Parameter depth) {
            this.depth = depth;
            return this;
        }

        public ParameterBuilder depth(float min, float max) {
            this.depth = Climate.Parameter.span(min, max);
            return this;
        }

        public ParameterBuilder weirdness(Climate.Parameter weirdness) {
            this.weirdness = weirdness;
            return this;
        }

        public ParameterBuilder weirdness(float min, float max) {
            this.weirdness = Climate.Parameter.span(min, max);
            return this;
        }

        public ParameterBuilder offset(float offset) {
            this.offset = offset;
            return this;
        }

        public Climate.ParameterPoint build() {
            return Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, offset);
        }
    }
}
