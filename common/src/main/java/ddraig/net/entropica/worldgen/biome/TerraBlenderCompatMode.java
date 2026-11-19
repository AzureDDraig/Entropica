package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.config.EntropicaConfig;

/**
 * Defines how Entropica's native multi-noise biome injector coordinates with TerraBlender
 * when TerraBlender is present in the modpack.
 */
public enum TerraBlenderCompatMode {
    /**
     * Default (Mode 1): Gives way to any modded biome placed by TerraBlender
     * (and delegates surface rules to TerraBlender's SurfaceRuleManager),
     * while seamlessly layering enabled Entropica biomes over vanilla terrain slots.
     * Also automatically yields if an external TerraBlender region/datapack already
     * injected Entropica biomes into the base biome source.
     */
    COOPERATE(1),

    /**
     * Mode 2: Completely yields multi-noise biome placement whenever TerraBlender is installed,
     * allowing modpack authors to manage biome placement exclusively via external TerraBlender
     * regions or datapacks while keeping SurfaceRuleManager compatibility active.
     */
    YIELD_TO_TERRABLENDER(2),

    /**
     * Mode 3: Works directly on top of all TerraBlender regions (both vanilla and modded biomes)
     * according to Entropica's regional weights and 6D multi-noise climate rules.
     */
    OVERLAY_ON_TOP(3);

    private final int configCode;

    TerraBlenderCompatMode(int configCode) {
        this.configCode = configCode;
    }

    public int getConfigCode() {
        return configCode;
    }

    public static TerraBlenderCompatMode fromConfigCode(int code) {
        return switch (code) {
            case 2 -> YIELD_TO_TERRABLENDER;
            case 3 -> OVERLAY_ON_TOP;
            default -> COOPERATE;
        };
    }

    public static TerraBlenderCompatMode current() {
        return fromConfigCode(EntropicaConfig.TERRABLENDER_COMPAT_MODE.get());
    }
}
