package ddraig.net.entropica.worldgen.biome;

/**
 * Declarative water-surface optical phenomenon modes rendered via standard
 * {@code MultiBufferSource} translucent geometry without chunk-mesher mixins,
 * ensuring 100% compatibility with TerraBlender, Sodium/Embeddium/Rubidium,
 * and Iris/Oculus shaderpacks.
 */
public enum WaterOpticalMode {
    NONE,
    /** Grazing-angle Fresnel sky and Materia atmosphere reflection across calm coastal/spring water. */
    FRESNEL_SKY_REFLECTION,
    /** Ripple-warped overhead aurora curtain reflections across polar skerries water. */
    AURORA_RIBBON_REFLECTION,
    /** Multi-layer 3D camera-parallax sub-surface nebula and twinkling starlight portal depth. */
    STARLIGHT_PARALLAX,
    /** Warm iron-rose and garnet specular capillary glints across sanguine shallows. */
    CRIMSON_TIDE_GLINT,
    /** Shimmering heat-haze amber optical wave reflections across sunlit glacier meltwater. */
    MIRAGE_SHIMMER_REFLECTION
}
