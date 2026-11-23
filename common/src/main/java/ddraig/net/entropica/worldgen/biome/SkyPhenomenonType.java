package ddraig.net.entropica.worldgen.biome;

/**
 * High-altitude procedural sky phenomena rendered via standard emissive translucent
 * geometry ({@code RenderType.entityTranslucentEmissive}), ensuring 100% compatibility
 * with Sodium chunk rendering and Iris / Oculus / Complementary / BSL shaderpacks.
 */
public enum SkyPhenomenonType {
    NONE,
    /** Pale crystalline polar ice-halo ring & sun-dog diamond glints (Blue-Glacier Shelf, Crystalline Pack-Ice). */
    GLACIAL_ICE_HALO,
    /** Undulating multi-ribbon polar northern lights (Auroral Skerries, Auroral Grove, Aurora biomes). */
    AURORA_CURTAINS,
    /** Shimmering celestial nebula band & drifting starlight glints (Astral Skerries, Astral Valleys, Astral Grove). */
    ASTRAL_STARDUST_VEIL,
    /** Darkened umbral disc ringed by a glowing twilight/solar corona (Silent Fjord, Eclipsed Mangrove, Shadowed Woods). */
    ECLIPSE_CORONA,
    /** High-altitude dimensional violet rift fractures pulsing softly overhead (Null-Fringe Bay, Void-Cracked biomes). */
    VOID_RIFT_FRACTURES,
    /** Warm shimmering golden-amber horizon wave bands (Mirage Glacier, Mirage Crags, Shimmering Glade). */
    MIRAGE_HEAT_SHIMMER,
    /** High-altitude static lightning arcs jumping intermittently across charged clouds overhead (Static Ice-Plains, Lightning Peak, Storm-Swept biomes). */
    CLOUD_LIGHTNING_ARCS
}
