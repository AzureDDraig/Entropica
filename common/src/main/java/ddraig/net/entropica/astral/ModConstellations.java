package ddraig.net.entropica.astral;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

public class ModConstellations {

    private static final Map<ResourceLocation, Constellation> CONSTELLATIONS = new LinkedHashMap<>();

    // Bitmask helpers for Minecraft Moon Phases (0 = Full, 1 = Waning Gibbous, 2 = Third Quarter, 3 = Waning Crescent, 4 = New Moon, 5 = Waxing Crescent, 6 = First Quarter, 7 = Waxing Gibbous)
    public static final int PHASE_ALL = 0xFF;
    public static final int PHASE_FULL = (1 << 0) | (1 << 7) | (1 << 1);
    public static final int PHASE_NEW = (1 << 4) | (1 << 3) | (1 << 5);
    public static final int PHASE_QUARTERS = (1 << 2) | (1 << 6);
    public static final int PHASE_CRESCENT = (1 << 3) | (1 << 5);
    public static final int PHASE_GIBBOUS = (1 << 1) | (1 << 7);

    // ==========================================
    // TIER 1: FUNDAMENTAL CONSTELLATIONS (8)
    // Crescent / Quarter Moons (Phases 2, 3, 5, 6)
    // ==========================================
    public static final Constellation VESPA_ACULEUS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "vespa_aculeus"),
            "constellation.entropica.vespa_aculeus",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_F,
            EssenceType.AIR,
            25.0f,
            PHASE_CRESCENT | PHASE_QUARTERS,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.4f, SpectralClass.CLASS_F), // Head/Stinger Apex
                    new ConstellationStar(1, 30, 40, 1.1f, SpectralClass.CLASS_F), // Left Wing
                    new ConstellationStar(2, 70, 40, 1.1f, SpectralClass.CLASS_F), // Right Wing
                    new ConstellationStar(3, 50, 60, 1.2f, SpectralClass.CLASS_F), // Thorax
                    new ConstellationStar(4, 50, 85, 1.5f, SpectralClass.CLASS_F)  // Tail / Stinger
            ),
            List.of(
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(1, 0),
                    new ConstellationConnection(2, 0)
            ),
            "Kinetic projectile velocity, arrow trajectory straightening, and wind currents.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "falx_aetheria")),
            Set.of(0)
    ));

    public static final Constellation LUCERNA_RADIALIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "lucerna_radialis"),
            "constellation.entropica.lucerna_radialis",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_A,
            EssenceType.RADIANT,
            24.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.4f, SpectralClass.CLASS_A),
                    new ConstellationStar(1, 35, 45, 1.1f, SpectralClass.CLASS_F),
                    new ConstellationStar(2, 65, 45, 1.1f, SpectralClass.CLASS_F),
                    new ConstellationStar(3, 50, 55, 1.6f, SpectralClass.CLASS_O),
                    new ConstellationStar(4, 35, 80, 1.0f, SpectralClass.CLASS_G),
                    new ConstellationStar(5, 65, 80, 1.0f, SpectralClass.CLASS_G)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(1, 4),
                    new ConstellationConnection(2, 5),
                    new ConstellationConnection(4, 5)
            ),
            "Hostile mob pacification, anti-spawning daylight fields, and luminous warding.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "noctua_lucis")),
            Set.of(3)
    ));

    public static final Constellation SERPENS_VENENI = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "serpens_veneni"),
            "constellation.entropica.serpens_veneni",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_V,
            EssenceType.UMBRAL,
            22.0f,
            PHASE_CRESCENT | PHASE_QUARTERS,
            List.of(
                    new ConstellationStar(0, 25, 20, 1.4f, SpectralClass.CLASS_V), // Fangs
                    new ConstellationStar(1, 45, 30, 1.1f, SpectralClass.CLASS_V),
                    new ConstellationStar(2, 75, 40, 1.2f, SpectralClass.CLASS_V), // Coil Upper
                    new ConstellationStar(3, 55, 65, 1.1f, SpectralClass.CLASS_V), // Coil Lower
                    new ConstellationStar(4, 30, 80, 1.3f, SpectralClass.CLASS_V)  // Tail Tip
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4)
            ),
            "Armor-piercing toxic starlight, shadow phasing, and umbral venom.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "aranea_tenebrae")),
            Set.of(1)
    ));

    public static final Constellation ARBOR_VITAE = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "arbor_vitae"),
            "constellation.entropica.arbor_vitae",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_A,
            EssenceType.VITAE,
            18.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 85, 1.2f, SpectralClass.CLASS_G), // Root
                    new ConstellationStar(1, 50, 55, 1.0f, SpectralClass.CLASS_G), // Trunk
                    new ConstellationStar(2, 28, 32, 1.1f, SpectralClass.CLASS_A), // Left Canopy
                    new ConstellationStar(3, 72, 32, 1.1f, SpectralClass.CLASS_A), // Right Canopy
                    new ConstellationStar(4, 50, 15, 1.5f, SpectralClass.CLASS_A)  // Apex Bloom
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(1, 4),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 4)
            ),
            "Crop acceleration, sapling maturation, botanical proliferation, and soil hydration.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "cervus_vitae")),
            Set.of(0)
    ));

    public static final Constellation SCUTUM_AEGIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "scutum_aegis"),
            "constellation.entropica.scutum_aegis",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_G,
            EssenceType.AEGIS,
            18.0f,
            PHASE_GIBBOUS | PHASE_QUARTERS,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.3f, SpectralClass.CLASS_G),
                    new ConstellationStar(1, 20, 35, 1.0f, SpectralClass.CLASS_G),
                    new ConstellationStar(2, 80, 35, 1.0f, SpectralClass.CLASS_G),
                    new ConstellationStar(3, 30, 75, 1.1f, SpectralClass.CLASS_A),
                    new ConstellationStar(4, 70, 75, 1.1f, SpectralClass.CLASS_A),
                    new ConstellationStar(5, 50, 90, 1.2f, SpectralClass.CLASS_G)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 5)
            ),
            "Resistance aura, armor hardening, blast dissipation, and physical warding."
    ));

    public static final Constellation ATHANOR_IGNIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "athanor_ignis"),
            "constellation.entropica.athanor_ignis",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_K,
            EssenceType.MAGMA,
            16.0f,
            PHASE_QUARTERS | PHASE_CRESCENT,
            List.of(
                    new ConstellationStar(0, 30, 20, 1.2f, SpectralClass.CLASS_K),
                    new ConstellationStar(1, 70, 20, 1.2f, SpectralClass.CLASS_K),
                    new ConstellationStar(2, 20, 60, 1.0f, SpectralClass.CLASS_K),
                    new ConstellationStar(3, 80, 60, 1.0f, SpectralClass.CLASS_K),
                    new ConstellationStar(4, 50, 85, 1.5f, SpectralClass.CLASS_M)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 4)
            ),
            "In-world ore cluster smelting without fuel consumption and magma distillation.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "calcar_vulcanis")),
            Set.of(4)
    ));

    public static final Constellation GLADIUS_IGNIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "gladius_ignis"),
            "constellation.entropica.gladius_ignis",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_K,
            EssenceType.PYRE,
            10.0f,
            PHASE_NEW | PHASE_CRESCENT,
            List.of(
                    new ConstellationStar(0, 50, 10, 1.5f, SpectralClass.CLASS_K), // Tip
                    new ConstellationStar(1, 50, 40, 1.0f, SpectralClass.CLASS_K),
                    new ConstellationStar(2, 50, 70, 1.1f, SpectralClass.CLASS_K),
                    new ConstellationStar(3, 35, 75, 1.0f, SpectralClass.CLASS_M), // Guard L
                    new ConstellationStar(4, 65, 75, 1.0f, SpectralClass.CLASS_M), // Guard R
                    new ConstellationStar(5, 50, 90, 1.2f, SpectralClass.CLASS_M)  // Pommel
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(2, 5)
            ),
            "Thermal furnace acceleration, pyre weapon charging, and blade lethality."
    ));

    public static final Constellation PENNA_AETHERIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "penna_aetheris"),
            "constellation.entropica.penna_aetheris",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_O,
            EssenceType.AETHER,
            6.0f,
            PHASE_QUARTERS | PHASE_CRESCENT,
            List.of(
                    new ConstellationStar(0, 20, 80, 1.0f, SpectralClass.CLASS_O),
                    new ConstellationStar(1, 35, 60, 1.1f, SpectralClass.CLASS_O),
                    new ConstellationStar(2, 50, 40, 1.2f, SpectralClass.CLASS_O),
                    new ConstellationStar(3, 65, 25, 1.3f, SpectralClass.CLASS_O),
                    new ConstellationStar(4, 85, 15, 1.5f, SpectralClass.CLASS_O) // Quill Tip
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4)
            ),
            "Slow falling, reduced gravity, fall damage negation, and elytra propulsion.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "arcus_sidereus")),
            Set.of(1)
    ));

    // ==========================================
    // TIER 2: ADVANCED CONSTELLATIONS (8)
    // Gibbous & Full Moons (Phases 0, 1, 7)
    // ==========================================
    public static final Constellation MINERALIS_GEODAE = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "mineralis_geodae"),
            "constellation.entropica.mineralis_geodae",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_G,
            EssenceType.EARTH,
            20.0f,
            PHASE_GIBBOUS,
            List.of(
                    new ConstellationStar(0, 50, 10, 1.3f, SpectralClass.CLASS_G),
                    new ConstellationStar(1, 25, 40, 1.1f, SpectralClass.CLASS_G),
                    new ConstellationStar(2, 75, 40, 1.1f, SpectralClass.CLASS_G),
                    new ConstellationStar(3, 30, 80, 1.0f, SpectralClass.CLASS_G),
                    new ConstellationStar(4, 70, 80, 1.0f, SpectralClass.CLASS_G),
                    new ConstellationStar(5, 50, 95, 1.5f, SpectralClass.CLASS_G)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 5),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(3, 4)
            ),
            "Transmutes raw stone into random native ores and earth geode proliferation.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "taurus_petrae")),
            Set.of(5)
    ));

    public static final Constellation GLACIES_CRYSTALLINE = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "glacies_crystalline"),
            "constellation.entropica.glacies_crystalline",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_B,
            EssenceType.GLACIAL,
            16.0f,
            PHASE_GIBBOUS | PHASE_QUARTERS,
            List.of(
                    new ConstellationStar(0, 50, 10, 1.5f, SpectralClass.CLASS_B), // Spire
                    new ConstellationStar(1, 30, 45, 1.1f, SpectralClass.CLASS_B),
                    new ConstellationStar(2, 70, 45, 1.1f, SpectralClass.CLASS_B),
                    new ConstellationStar(3, 20, 80, 1.2f, SpectralClass.CLASS_B),
                    new ConstellationStar(4, 80, 80, 1.2f, SpectralClass.CLASS_B),
                    new ConstellationStar(5, 50, 60, 1.0f, SpectralClass.CLASS_B)
            ),
            List.of(
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(1, 5),
                    new ConstellationConnection(2, 5),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 5)
            ),
            "60% enemy movement slowing, frost mist aura, and fire extinguishment.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "serpens_glacialis")),
            Set.of(0)
    ));

    public static final Constellation ULTERIA_VIATORIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "ulteria_viatoris"),
            "constellation.entropica.ulteria_viatoris",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_B,
            EssenceType.AURORA,
            15.0f,
            PHASE_GIBBOUS | PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.6f, SpectralClass.CLASS_B), // Polaris
                    new ConstellationStar(1, 20, 50, 1.0f, SpectralClass.CLASS_B),
                    new ConstellationStar(2, 80, 50, 1.0f, SpectralClass.CLASS_B),
                    new ConstellationStar(3, 50, 85, 1.2f, SpectralClass.CLASS_B),
                    new ConstellationStar(4, 50, 50, 1.4f, SpectralClass.CLASS_A)  // Pivot
            ),
            List.of(
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(1, 4),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 3)
            ),
            "100% movement speed distortion, frictionless ice traversal, and pathfinding navigation.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "iris_spectralis")),
            Set.of(4)
    ));

    public static final Constellation RESINA_SUCCINI = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "resina_succini"),
            "constellation.entropica.resina_succini",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_G,
            EssenceType.AMBER,
            13.0f,
            PHASE_GIBBOUS | PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 20, 1.3f, SpectralClass.CLASS_G),
                    new ConstellationStar(1, 25, 45, 1.1f, SpectralClass.CLASS_G),
                    new ConstellationStar(2, 75, 45, 1.1f, SpectralClass.CLASS_G),
                    new ConstellationStar(3, 35, 80, 1.2f, SpectralClass.CLASS_G),
                    new ConstellationStar(4, 65, 80, 1.2f, SpectralClass.CLASS_G)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(1, 2)
            ),
            "Item despawn prevention, amber stasis fields, and entity preservation.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "felis_succina")),
            Set.of(0)
    ));

    public static final Constellation SANGUIS_CHALYBIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "sanguis_chalybis"),
            "constellation.entropica.sanguis_chalybis",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_M,
            EssenceType.BLOOD,
            12.0f,
            PHASE_GIBBOUS | PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.5f, SpectralClass.CLASS_M), // Crown
                    new ConstellationStar(1, 25, 40, 1.2f, SpectralClass.CLASS_M),
                    new ConstellationStar(2, 75, 40, 1.2f, SpectralClass.CLASS_M),
                    new ConstellationStar(3, 50, 60, 1.0f, SpectralClass.CLASS_M),
                    new ConstellationStar(4, 30, 85, 1.1f, SpectralClass.CLASS_M),
                    new ConstellationStar(5, 70, 85, 1.1f, SpectralClass.CLASS_M)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(3, 5)
            ),
            "Converts nearby mob damage into pure player life essence and sanguine leech.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "draco_pyrotis")),
            Set.of(3)
    ));

    public static final Constellation FULGUR_TONITRUS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "fulgur_tonitrus"),
            "constellation.entropica.fulgur_tonitrus",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_O,
            EssenceType.LIGHTNING,
            12.0f,
            PHASE_QUARTERS | PHASE_CRESCENT,
            List.of(
                    new ConstellationStar(0, 65, 10, 1.5f, SpectralClass.CLASS_O),
                    new ConstellationStar(1, 40, 35, 1.2f, SpectralClass.CLASS_O),
                    new ConstellationStar(2, 60, 55, 1.3f, SpectralClass.CLASS_O),
                    new ConstellationStar(3, 35, 75, 1.1f, SpectralClass.CLASS_O),
                    new ConstellationStar(4, 45, 95, 1.4f, SpectralClass.CLASS_O)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4)
            ),
            "Wireless atmospheric charging of nearby Orbis Cells and galvanic arcs.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "ceraunius_sagitta")),
            Set.of(0)
    ));

    public static final Constellation HOROLOGIUM_CHRONI = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "horologium_chroni"),
            "constellation.entropica.horologium_chroni",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_B,
            EssenceType.ASTRAL,
            11.0f,
            PHASE_GIBBOUS | PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 25, 20, 1.3f, SpectralClass.CLASS_B),
                    new ConstellationStar(1, 75, 20, 1.3f, SpectralClass.CLASS_B),
                    new ConstellationStar(2, 50, 50, 1.6f, SpectralClass.CLASS_A), // Pivot
                    new ConstellationStar(3, 25, 80, 1.2f, SpectralClass.CLASS_B),
                    new ConstellationStar(4, 75, 80, 1.2f, SpectralClass.CLASS_B)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 4)
            ),
            "Accelerates tile entity tick rates by up to 1000% and chronomantic distortion.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "anulus_aeternus")),
            Set.of(2)
    ));

    public static final Constellation BOOTES_PASTORALIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "bootes_pastoralis"),
            "constellation.entropica.bootes_pastoralis",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_A,
            EssenceType.SYLVAN,
            8.0f,
            PHASE_GIBBOUS | PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 10, 1.4f, SpectralClass.CLASS_A),
                    new ConstellationStar(1, 30, 40, 1.0f, SpectralClass.CLASS_A),
                    new ConstellationStar(2, 70, 40, 1.0f, SpectralClass.CLASS_A),
                    new ConstellationStar(3, 50, 60, 1.2f, SpectralClass.CLASS_A),
                    new ConstellationStar(4, 40, 90, 1.1f, SpectralClass.CLASS_A),
                    new ConstellationStar(5, 60, 90, 1.1f, SpectralClass.CLASS_A)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(3, 5)
            ),
            "Automatic livestock breeding, wool/leather proliferation, and pastoral tranquility."
    ));

    // ==========================================
    // TIER 3: MASTER CONSTELLATIONS (4)
    // Peak Full Moon Zenith (Phase 0)
    // ==========================================
    public static final Constellation VORAGO_BLIGHTI = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "vorago_blighti"),
            "constellation.entropica.vorago_blighti",
            ConstellationTier.MASTER,
            SpectralClass.CLASS_V,
            EssenceType.BLIGHT,
            14.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.5f, SpectralClass.CLASS_V),
                    new ConstellationStar(1, 20, 45, 1.2f, SpectralClass.CLASS_V),
                    new ConstellationStar(2, 80, 45, 1.2f, SpectralClass.CLASS_V),
                    new ConstellationStar(3, 35, 75, 1.1f, SpectralClass.CLASS_V),
                    new ConstellationStar(4, 65, 75, 1.1f, SpectralClass.CLASS_V),
                    new ConstellationStar(5, 50, 50, 1.8f, SpectralClass.CLASS_V) // Singularity Core
            ),
            List.of(
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(1, 5),
                    new ConstellationConnection(2, 5),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 5),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4)
            ),
            "Pulls dropped items, XP orbs, and meteors into central hoppers and void maw suction.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "speculum_abyssi")),
            Set.of(5)
    ));

    public static final Constellation VELUM_UMBRATICUM = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "velum_umbraticum"),
            "constellation.entropica.velum_umbraticum",
            ConstellationTier.MASTER,
            SpectralClass.CLASS_V,
            EssenceType.PENUMBRA,
            7.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 30, 20, 1.3f, SpectralClass.CLASS_V),
                    new ConstellationStar(1, 70, 20, 1.3f, SpectralClass.CLASS_V),
                    new ConstellationStar(2, 50, 45, 1.6f, SpectralClass.CLASS_V),
                    new ConstellationStar(3, 20, 80, 1.1f, SpectralClass.CLASS_V),
                    new ConstellationStar(4, 80, 80, 1.1f, SpectralClass.CLASS_V),
                    new ConstellationStar(5, 50, 85, 1.4f, SpectralClass.CLASS_V)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(2, 5),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 5)
            ),
            "Intangibility ward protecting multiblock structures from explosion and damage.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "pallium_tenebrae")),
            Set.of(2)
    ));

    public static final Constellation ECHO_PRIMORDIALIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "echo_primordialis"),
            "constellation.entropica.echo_primordialis",
            ConstellationTier.MASTER,
            SpectralClass.CLASS_B,
            EssenceType.ENTROPIC,
            5.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.4f, SpectralClass.CLASS_B),
                    new ConstellationStar(1, 25, 35, 1.2f, SpectralClass.CLASS_B),
                    new ConstellationStar(2, 75, 35, 1.2f, SpectralClass.CLASS_B),
                    new ConstellationStar(3, 25, 70, 1.2f, SpectralClass.CLASS_B),
                    new ConstellationStar(4, 75, 70, 1.2f, SpectralClass.CLASS_B),
                    new ConstellationStar(5, 50, 90, 1.4f, SpectralClass.CLASS_B)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 5),
                    new ConstellationConnection(1, 4),
                    new ConstellationConnection(2, 3)
            ),
            "200% machine recipe duplication, feedback overdrive, and resonant echo cycling."
    ));

    public static final Constellation CORONA_SOLARIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "corona_solaris"),
            "constellation.entropica.corona_solaris",
            ConstellationTier.MASTER,
            SpectralClass.CLASS_G,
            EssenceType.CELESTIAL,
            5.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 10, 1.6f, SpectralClass.CLASS_G), // Crown Top
                    new ConstellationStar(1, 25, 25, 1.3f, SpectralClass.CLASS_G),
                    new ConstellationStar(2, 75, 25, 1.3f, SpectralClass.CLASS_G),
                    new ConstellationStar(3, 20, 60, 1.2f, SpectralClass.CLASS_G),
                    new ConstellationStar(4, 80, 60, 1.2f, SpectralClass.CLASS_G),
                    new ConstellationStar(5, 35, 85, 1.1f, SpectralClass.CLASS_G),
                    new ConstellationStar(6, 65, 85, 1.1f, SpectralClass.CLASS_G)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 6),
                    new ConstellationConnection(5, 6),
                    new ConstellationConnection(1, 2)
            ),
            "Permanent noon solar intensity, daytime starlight lock, and radiance boost.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "sol_invictus")),
            Set.of(0)
    ));

    // ==========================================
    // TIER 4: MYTHIC COSMIC ENTITIES (4)
    // New Moon (Phase 4) & The End
    // ==========================================
    public static final Constellation LEVIATHAN = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "leviathan"),
            "constellation.entropica.leviathan",
            ConstellationTier.MYTHIC,
            SpectralClass.CLASS_O,
            EssenceType.ABYSS,
            12.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 15, 30, 1.5f, SpectralClass.CLASS_O), // Abyssal Maw
                    new ConstellationStar(1, 35, 45, 1.3f, SpectralClass.CLASS_O),
                    new ConstellationStar(2, 60, 40, 1.4f, SpectralClass.CLASS_O),
                    new ConstellationStar(3, 85, 60, 1.6f, SpectralClass.CLASS_O), // Dorsal Fin
                    new ConstellationStar(4, 70, 85, 1.2f, SpectralClass.CLASS_O),
                    new ConstellationStar(5, 40, 80, 1.1f, SpectralClass.CLASS_O)  // Abyssal Tail
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 5),
                    new ConstellationConnection(5, 1)
            ),
            "Frictionless creative flight, void fall safety immunity, and deep ocean mastery.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "charybdis_profunda")),
            Set.of(1)
    ));

    public static final Constellation YGGDRASIL = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "yggdrasil"),
            "constellation.entropica.yggdrasil",
            ConstellationTier.MYTHIC,
            SpectralClass.CLASS_A,
            EssenceType.GENESIS,
            8.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 10, 1.7f, SpectralClass.CLASS_A), // High Canopy
                    new ConstellationStar(1, 25, 25, 1.4f, SpectralClass.CLASS_A),
                    new ConstellationStar(2, 75, 25, 1.4f, SpectralClass.CLASS_A),
                    new ConstellationStar(3, 50, 45, 1.5f, SpectralClass.CLASS_A), // World Heart
                    new ConstellationStar(4, 30, 75, 1.3f, SpectralClass.CLASS_A), // Deep Roots L
                    new ConstellationStar(5, 70, 75, 1.3f, SpectralClass.CLASS_A), // Deep Roots R
                    new ConstellationStar(6, 50, 95, 1.6f, SpectralClass.CLASS_A)  // Prime Root
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 6),
                    new ConstellationConnection(5, 6),
                    new ConstellationConnection(3, 6)
            ),
            "5-minute cooldown death cheat ward, tool durability auto-repair, and primordial rebirth.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "terranox_primus")),
            Set.of(3)
    ));

    public static final Constellation OUROBOROS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "ouroboros"),
            "constellation.entropica.ouroboros",
            ConstellationTier.MYTHIC,
            SpectralClass.CLASS_A,
            EssenceType.AETHER,
            6.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.6f, SpectralClass.CLASS_A), // Head
                    new ConstellationStar(1, 80, 35, 1.3f, SpectralClass.CLASS_A),
                    new ConstellationStar(2, 85, 65, 1.3f, SpectralClass.CLASS_A),
                    new ConstellationStar(3, 50, 85, 1.5f, SpectralClass.CLASS_A),
                    new ConstellationStar(4, 15, 65, 1.3f, SpectralClass.CLASS_A),
                    new ConstellationStar(5, 20, 35, 1.3f, SpectralClass.CLASS_A)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 5),
                    new ConstellationConnection(5, 0)
            ),
            "0% Materia transfer decay, thermal overpressure immunity, and perpetual energy equilibrium.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "aetherion_aeternus")),
            Set.of(0)
    ));

    public static final Constellation AZATHOTH = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "azathoth"),
            "constellation.entropica.azathoth",
            ConstellationTier.MYTHIC,
            SpectralClass.CLASS_V,
            EssenceType.OBLIVION,
            7.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 50, 2.0f, SpectralClass.CLASS_V), // Singularity Nucleus
                    new ConstellationStar(1, 50, 10, 1.4f, SpectralClass.CLASS_V), // North Rift
                    new ConstellationStar(2, 90, 50, 1.4f, SpectralClass.CLASS_V), // East Rift
                    new ConstellationStar(3, 50, 90, 1.4f, SpectralClass.CLASS_V), // South Rift
                    new ConstellationStar(4, 10, 50, 1.4f, SpectralClass.CLASS_V), // West Rift
                    new ConstellationStar(5, 25, 25, 1.2f, SpectralClass.CLASS_V),
                    new ConstellationStar(6, 75, 25, 1.2f, SpectralClass.CLASS_V),
                    new ConstellationStar(7, 75, 75, 1.2f, SpectralClass.CLASS_V),
                    new ConstellationStar(8, 25, 75, 1.2f, SpectralClass.CLASS_V)
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(1, 5),
                    new ConstellationConnection(1, 6),
                    new ConstellationConnection(2, 6),
                    new ConstellationConnection(2, 7),
                    new ConstellationConnection(3, 7),
                    new ConstellationConnection(3, 8),
                    new ConstellationConnection(4, 8),
                    new ConstellationConnection(4, 5)
            ),
            "Complete temporal stasis, inter-dimensional cosmic rift creation, and void dissolution.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "vorator_stellarum")),
            Set.of(0)
    ));

    // ==========================================
    // TIER 1 ADDITIONS (6)
    // ==========================================
    public static final Constellation FALX_AETHERIA = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "falx_aetheria"),
            "constellation.entropica.falx_aetheria",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_F,
            EssenceType.AIR,
            24.0f,
            PHASE_CRESCENT | PHASE_QUARTERS,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.4f, SpectralClass.CLASS_F), // Apex Sky Wing Star (Shared with Vespa Aculeus)
                    new ConstellationStar(1, 20, 35, 1.1f, SpectralClass.CLASS_F), // Blade Tip
                    new ConstellationStar(2, 35, 60, 1.2f, SpectralClass.CLASS_F), // Blade Curve
                    new ConstellationStar(3, 65, 75, 1.3f, SpectralClass.CLASS_F), // Shaft
                    new ConstellationStar(4, 80, 85, 1.1f, SpectralClass.CLASS_F)  // Grip
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(0, 2)
            ),
            "Slashes kinetic wind gusts, projectile deflection, and aerial speed.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "vespa_aculeus")),
            Set.of(0)
    ));

    public static final Constellation CERVUS_VITAE = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "cervus_vitae"),
            "constellation.entropica.cervus_vitae",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_A,
            EssenceType.VITAE,
            20.0f,
            PHASE_FULL | PHASE_GIBBOUS,
            List.of(
                    new ConstellationStar(0, 50, 85, 1.5f, SpectralClass.CLASS_A), // Vital Root Star (Shared with Arbor Vitae)
                    new ConstellationStar(1, 50, 55, 1.2f, SpectralClass.CLASS_A), // Body
                    new ConstellationStar(2, 35, 35, 1.3f, SpectralClass.CLASS_A), // Left Antler
                    new ConstellationStar(3, 65, 35, 1.3f, SpectralClass.CLASS_A), // Right Antler
                    new ConstellationStar(4, 25, 20, 1.0f, SpectralClass.CLASS_A), // Antler Crown Left
                    new ConstellationStar(5, 75, 20, 1.0f, SpectralClass.CLASS_A)  // Antler Crown Right
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 5)
            ),
            "Woodland creature swiftness, organic vitality, and forest animal bonding.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "arbor_vitae")),
            Set.of(0)
    ));

    public static final Constellation NOCTUA_LUCIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "noctua_lucis"),
            "constellation.entropica.noctua_lucis",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_A,
            EssenceType.RADIANT,
            22.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 55, 1.6f, SpectralClass.CLASS_O), // Beacon Focal Star (Shared with Lucerna Radialis)
                    new ConstellationStar(1, 25, 35, 1.2f, SpectralClass.CLASS_A), // Left Wingtip
                    new ConstellationStar(2, 75, 35, 1.2f, SpectralClass.CLASS_A), // Right Wingtip
                    new ConstellationStar(3, 35, 70, 1.1f, SpectralClass.CLASS_G), // Left Lower Wing
                    new ConstellationStar(4, 65, 70, 1.1f, SpectralClass.CLASS_G), // Right Lower Wing
                    new ConstellationStar(5, 50, 25, 1.3f, SpectralClass.CLASS_A)  // Antennae Apex
            ),
            List.of(
                    new ConstellationConnection(5, 0),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 0),
                    new ConstellationConnection(4, 0)
            ),
            "Warding luminescent dust, ambient starlight flares, and nighttime illumination.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "lucerna_radialis")),
            Set.of(0)
    ));

    public static final Constellation ARANEA_TENEBRAE = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "aranea_tenebrae"),
            "constellation.entropica.aranea_tenebrae",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_V,
            EssenceType.UMBRAL,
            21.0f,
            PHASE_CRESCENT | PHASE_QUARTERS,
            List.of(
                    new ConstellationStar(0, 45, 30, 1.1f, SpectralClass.CLASS_V), // Venom Node Star (Shared with Serpens Veneni)
                    new ConstellationStar(1, 50, 50, 1.5f, SpectralClass.CLASS_V), // Cephalothorax
                    new ConstellationStar(2, 20, 30, 1.0f, SpectralClass.CLASS_V), // Leg 1
                    new ConstellationStar(3, 80, 30, 1.0f, SpectralClass.CLASS_V), // Leg 2
                    new ConstellationStar(4, 15, 65, 1.0f, SpectralClass.CLASS_V), // Leg 3
                    new ConstellationStar(5, 85, 65, 1.0f, SpectralClass.CLASS_V), // Leg 4
                    new ConstellationStar(6, 50, 75, 1.3f, SpectralClass.CLASS_V)  // Abdomen Spinneret
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(1, 4),
                    new ConstellationConnection(1, 5),
                    new ConstellationConnection(1, 6)
            ),
            "Spun starlight shadow-webs, hostile mob entangling, and umbral traps.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "serpens_veneni")),
            Set.of(0)
    ));

    public static final Constellation CALCAR_VULCANIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "calcar_vulcanis"),
            "constellation.entropica.calcar_vulcanis",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_K,
            EssenceType.MAGMA,
            17.0f,
            PHASE_FULL | PHASE_GIBBOUS,
            List.of(
                    new ConstellationStar(0, 50, 55, 1.6f, SpectralClass.CLASS_K), // Crucible Heart Star (Shared with Athanor Ignis)
                    new ConstellationStar(1, 30, 35, 1.2f, SpectralClass.CLASS_K), // Left Horn
                    new ConstellationStar(2, 70, 35, 1.2f, SpectralClass.CLASS_K), // Right Horn
                    new ConstellationStar(3, 35, 80, 1.1f, SpectralClass.CLASS_K), // Slag Trough Left
                    new ConstellationStar(4, 65, 80, 1.1f, SpectralClass.CLASS_K)  // Slag Trough Right
            ),
            List.of(
                    new ConstellationConnection(1, 0),
                    new ConstellationConnection(2, 0),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(3, 4)
            ),
            "Slag-to-flux-glass automated refining, thermal heat generation, and molten glass synthesis.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "athanor_ignis")),
            Set.of(0)
    ));

    public static final Constellation ARCUS_SIDEREUS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "arcus_sidereus"),
            "constellation.entropica.arcus_sidereus",
            ConstellationTier.FUNDAMENTAL,
            SpectralClass.CLASS_O,
            EssenceType.AETHER,
            8.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 35, 60, 1.3f, SpectralClass.CLASS_O), // Fletching Feather Star (Shared with Penna Aetheris)
                    new ConstellationStar(1, 50, 15, 1.4f, SpectralClass.CLASS_O), // Upper Bow Tip
                    new ConstellationStar(2, 80, 50, 1.3f, SpectralClass.CLASS_O), // Arrowhead Apex
                    new ConstellationStar(3, 50, 85, 1.4f, SpectralClass.CLASS_O), // Lower Bow Tip
                    new ConstellationStar(4, 30, 50, 1.2f, SpectralClass.CLASS_O)  // Bow Grip Center
            ),
            List.of(
                    new ConstellationConnection(1, 4),
                    new ConstellationConnection(4, 3),
                    new ConstellationConnection(1, 0),
                    new ConstellationConnection(3, 0),
                    new ConstellationConnection(0, 2)
            ),
            "Zero-gravity starlight projectile bolts, 100% arrow retrieval, and pinpoint accuracy.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "penna_aetheris")),
            Set.of(0)
    ));

    // ==========================================
    // TIER 2 ADDITIONS (6)
    // ==========================================
    public static final Constellation TAURUS_PETRAE = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "taurus_petrae"),
            "constellation.entropica.taurus_petrae",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_G,
            EssenceType.EARTH,
            19.0f,
            PHASE_FULL | PHASE_GIBBOUS,
            List.of(
                    new ConstellationStar(0, 50, 85, 1.4f, SpectralClass.CLASS_G), // Bedrock Foundation Star (Shared with Mineralis Geodae)
                    new ConstellationStar(1, 50, 50, 1.5f, SpectralClass.CLASS_G), // Bull Skull
                    new ConstellationStar(2, 20, 25, 1.3f, SpectralClass.CLASS_G), // Left Granite Horn
                    new ConstellationStar(3, 80, 25, 1.3f, SpectralClass.CLASS_G), // Right Granite Horn
                    new ConstellationStar(4, 35, 70, 1.1f, SpectralClass.CLASS_G), // Left Shoulder
                    new ConstellationStar(5, 65, 70, 1.1f, SpectralClass.CLASS_G)  // Right Shoulder
            ),
            List.of(
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(4, 1),
                    new ConstellationConnection(5, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(1, 3)
            ),
            "Seismic ground slams, unyielding knockback resistance, and heavy impact melee.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "mineralis_geodae")),
            Set.of(0)
    ));

    public static final Constellation IRIS_SPECTRALIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "iris_spectralis"),
            "constellation.entropica.iris_spectralis",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_B,
            EssenceType.AURORA,
            16.0f,
            PHASE_FULL | PHASE_GIBBOUS,
            List.of(
                    new ConstellationStar(0, 50, 50, 1.6f, SpectralClass.CLASS_B), // Refraction Prism Star (Shared with Ulteria Viatoris)
                    new ConstellationStar(1, 20, 30, 1.2f, SpectralClass.CLASS_B), // Red Ray
                    new ConstellationStar(2, 50, 20, 1.2f, SpectralClass.CLASS_B), // Green Ray
                    new ConstellationStar(3, 80, 30, 1.2f, SpectralClass.CLASS_B), // Blue Ray
                    new ConstellationStar(4, 30, 80, 1.1f, SpectralClass.CLASS_B), // Base Left
                    new ConstellationStar(5, 70, 80, 1.1f, SpectralClass.CLASS_B)  // Base Right
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(4, 5)
            ),
            "Multi-spectral prismatic refraction aura, night vision, and radiant starlight dispersion.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "ulteria_viatoris")),
            Set.of(0)
    ));

    public static final Constellation FELIS_SUCCINA = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "felis_succina"),
            "constellation.entropica.felis_succina",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_G,
            EssenceType.AMBER,
            14.0f,
            PHASE_FULL | PHASE_GIBBOUS,
            List.of(
                    new ConstellationStar(0, 50, 45, 1.5f, SpectralClass.CLASS_G), // Amber Vault Node (Shared with Resina Succini)
                    new ConstellationStar(1, 30, 25, 1.2f, SpectralClass.CLASS_G), // Left Ear
                    new ConstellationStar(2, 70, 25, 1.2f, SpectralClass.CLASS_G), // Right Ear
                    new ConstellationStar(3, 25, 65, 1.1f, SpectralClass.CLASS_G), // Foreleg
                    new ConstellationStar(4, 75, 75, 1.2f, SpectralClass.CLASS_G), // Hindleg
                    new ConstellationStar(5, 85, 90, 1.1f, SpectralClass.CLASS_G)  // Tail
            ),
            List.of(
                    new ConstellationConnection(1, 0),
                    new ConstellationConnection(2, 0),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(4, 5)
            ),
            "Stealth stasis, evasion reflexes, and shadow cloaking.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "resina_succini")),
            Set.of(0)
    ));

    public static final Constellation DRACO_PYROTIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "draco_pyrotis"),
            "constellation.entropica.draco_pyrotis",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_M,
            EssenceType.PYRE,
            13.0f,
            PHASE_FULL | PHASE_GIBBOUS,
            List.of(
                    new ConstellationStar(0, 50, 50, 1.6f, SpectralClass.CLASS_M), // Vital Bleeding Core Star (Shared with Sanguis Chalybis)
                    new ConstellationStar(1, 20, 20, 1.3f, SpectralClass.CLASS_M), // Left Wing Flame
                    new ConstellationStar(2, 80, 20, 1.3f, SpectralClass.CLASS_M), // Right Wing Flame
                    new ConstellationStar(3, 50, 25, 1.4f, SpectralClass.CLASS_M), // Dragon Maw
                    new ConstellationStar(4, 35, 80, 1.1f, SpectralClass.CLASS_M), // Tail Clasp
                    new ConstellationStar(5, 65, 80, 1.1f, SpectralClass.CLASS_M)
            ),
            List.of(
                    new ConstellationConnection(3, 0),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(0, 5)
            ),
            "Converts sacrificed life essence into scorching pyre bursts and flame retribution.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "sanguis_chalybis")),
            Set.of(0)
    ));

    public static final Constellation CERAUNIUS_SAGITTA = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "ceraunius_sagitta"),
            "constellation.entropica.ceraunius_sagitta",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_O,
            EssenceType.LIGHTNING,
            14.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.5f, SpectralClass.CLASS_O), // Electrical Spear Apex (Shared with Fulgur Tonitrus)
                    new ConstellationStar(1, 50, 45, 1.2f, SpectralClass.CLASS_O), // Harpoon Shaft
                    new ConstellationStar(2, 25, 35, 1.1f, SpectralClass.CLASS_O), // Left Barb
                    new ConstellationStar(3, 75, 35, 1.1f, SpectralClass.CLASS_O), // Right Barb
                    new ConstellationStar(4, 50, 85, 1.3f, SpectralClass.CLASS_O)  // Harpoon Coil Base
            ),
            List.of(
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 4),
                    new ConstellationConnection(2, 1),
                    new ConstellationConnection(3, 1)
            ),
            "Targeted kinetic thunderbolts, ionization arcs, and lightning redirection.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "fulgur_tonitrus")),
            Set.of(0)
    ));

    public static final Constellation SERPENS_GLACIALIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "serpens_glacialis"),
            "constellation.entropica.serpens_glacialis",
            ConstellationTier.ADVANCED,
            SpectralClass.CLASS_B,
            EssenceType.GLACIAL,
            15.0f,
            PHASE_FULL | PHASE_GIBBOUS,
            List.of(
                    new ConstellationStar(0, 50, 15, 1.5f, SpectralClass.CLASS_B), // Rime Spire Apex Star (Shared with Glacies Crystalline)
                    new ConstellationStar(1, 30, 35, 1.2f, SpectralClass.CLASS_B), // Ice Coil 1
                    new ConstellationStar(2, 70, 50, 1.2f, SpectralClass.CLASS_B), // Ice Coil 2
                    new ConstellationStar(3, 30, 70, 1.2f, SpectralClass.CLASS_B), // Ice Coil 3
                    new ConstellationStar(4, 50, 90, 1.4f, SpectralClass.CLASS_B)  // Frost Fang
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4)
            ),
            "Flash-freezing frostbite strikes, permanent ice pathways, and absolute zero chilling.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "glacies_crystalline")),
            Set.of(0)
    ));

    // ==========================================
    // TIER 3 ADDITIONS (4)
    // ==========================================
    public static final Constellation SPECULUM_ABYSSI = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "speculum_abyssi"),
            "constellation.entropica.speculum_abyssi",
            ConstellationTier.MASTER,
            SpectralClass.CLASS_V,
            EssenceType.BLIGHT,
            13.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 50, 1.6f, SpectralClass.CLASS_V), // Singularity Vortex Core Star (Shared with Vorago Blighti)
                    new ConstellationStar(1, 50, 20, 1.2f, SpectralClass.CLASS_V), // Mirror Top
                    new ConstellationStar(2, 80, 50, 1.2f, SpectralClass.CLASS_V), // Mirror Right
                    new ConstellationStar(3, 50, 80, 1.2f, SpectralClass.CLASS_V), // Mirror Bottom
                    new ConstellationStar(4, 20, 50, 1.2f, SpectralClass.CLASS_V)  // Mirror Left
            ),
            List.of(
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 1),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 3)
            ),
            "Curse and projectile reflection, perception through solid matter, and void sight.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "vorago_blighti")),
            Set.of(0)
    ));

    public static final Constellation PALLIUM_TENEBRAE = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "pallium_tenebrae"),
            "constellation.entropica.pallium_tenebrae",
            ConstellationTier.MASTER,
            SpectralClass.CLASS_V,
            EssenceType.PENUMBRA,
            8.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 30, 1.5f, SpectralClass.CLASS_V), // Veil Boundary Star (Shared with Velum Umbraticum)
                    new ConstellationStar(1, 20, 50, 1.2f, SpectralClass.CLASS_V), // Left Shroud Clasp
                    new ConstellationStar(2, 80, 50, 1.2f, SpectralClass.CLASS_V), // Right Shroud Clasp
                    new ConstellationStar(3, 30, 85, 1.1f, SpectralClass.CLASS_V), // Left Mantle Hem
                    new ConstellationStar(4, 70, 85, 1.1f, SpectralClass.CLASS_V)  // Right Mantle Hem
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 4)
            ),
            "Invisibility shroud, hostile monster aggro dampening, and total shadow concealment.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "velum_umbraticum")),
            Set.of(0)
    ));

    public static final Constellation ANULUS_AETERNUS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "anulus_aeternus"),
            "constellation.entropica.anulus_aeternus",
            ConstellationTier.MASTER,
            SpectralClass.CLASS_B,
            EssenceType.ASTRAL,
            10.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 50, 1.6f, SpectralClass.CLASS_B), // Pivot Gyro Star (Shared with Horologium Chroni)
                    new ConstellationStar(1, 50, 15, 1.3f, SpectralClass.CLASS_B), // Outer Ring North
                    new ConstellationStar(2, 85, 50, 1.3f, SpectralClass.CLASS_B), // Outer Ring East
                    new ConstellationStar(3, 50, 85, 1.3f, SpectralClass.CLASS_B), // Outer Ring South
                    new ConstellationStar(4, 15, 50, 1.3f, SpectralClass.CLASS_B)  // Outer Ring West
            ),
            List.of(
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 1),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4)
            ),
            "Temporal entity stasis field, tile-entity freeze, and time dilation equilibrium.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "horologium_chroni")),
            Set.of(0)
    ));

    public static final Constellation SOL_INVICTUS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "sol_invictus"),
            "constellation.entropica.sol_invictus",
            ConstellationTier.MASTER,
            SpectralClass.CLASS_G,
            EssenceType.CELESTIAL,
            6.0f,
            PHASE_FULL,
            List.of(
                    new ConstellationStar(0, 50, 20, 1.6f, SpectralClass.CLASS_G), // Solar Zenith Apex Star (Shared with Corona Solaris)
                    new ConstellationStar(1, 20, 50, 1.3f, SpectralClass.CLASS_G), // Left Sun Ray
                    new ConstellationStar(2, 80, 50, 1.3f, SpectralClass.CLASS_G), // Right Sun Ray
                    new ConstellationStar(3, 35, 75, 1.2f, SpectralClass.CLASS_G), // Lower Left Ray
                    new ConstellationStar(4, 65, 75, 1.2f, SpectralClass.CLASS_G), // Lower Right Ray
                    new ConstellationStar(5, 50, 50, 1.8f, SpectralClass.CLASS_G)  // Solar Disc
            ),
            List.of(
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(1, 5),
                    new ConstellationConnection(2, 5),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 5),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2)
            ),
            "300% daylight starlight collector overcharge, solar flare blasts, and solar resurrection.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "corona_solaris")),
            Set.of(0)
    ));

    // ==========================================
    // TIER 4 ADDITIONS (4)
    // ==========================================
    public static final Constellation CHARYBDIS_PROFUNDA = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "charybdis_profunda"),
            "constellation.entropica.charybdis_profunda",
            ConstellationTier.MYTHIC,
            SpectralClass.CLASS_O,
            EssenceType.ABYSS,
            11.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 50, 1.8f, SpectralClass.CLASS_O), // Abyssal Rift Singularity (Shared with Leviathan)
                    new ConstellationStar(1, 25, 25, 1.3f, SpectralClass.CLASS_O), // Maelstrom Arm 1
                    new ConstellationStar(2, 75, 25, 1.3f, SpectralClass.CLASS_O), // Maelstrom Arm 2
                    new ConstellationStar(3, 75, 75, 1.3f, SpectralClass.CLASS_O), // Maelstrom Arm 3
                    new ConstellationStar(4, 25, 75, 1.3f, SpectralClass.CLASS_O)  // Maelstrom Arm 4
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 1)
            ),
            "Hydrostatic crushing field, deep water mastery, and vortex pull dynamics.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "leviathan")),
            Set.of(0)
    ));

    public static final Constellation TERRANOX_PRIMUS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "terranox_primus"),
            "constellation.entropica.terranox_primus",
            ConstellationTier.MYTHIC,
            SpectralClass.CLASS_V,
            EssenceType.GENESIS,
            9.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 50, 1.8f, SpectralClass.CLASS_A), // Primordial Trunk Star (Shared with Yggdrasil)
                    new ConstellationStar(1, 50, 15, 1.4f, SpectralClass.CLASS_V), // World Impactite Crown
                    new ConstellationStar(2, 20, 40, 1.3f, SpectralClass.CLASS_V), // Tectonic Plate Left
                    new ConstellationStar(3, 80, 40, 1.3f, SpectralClass.CLASS_V), // Tectonic Plate Right
                    new ConstellationStar(4, 30, 85, 1.3f, SpectralClass.CLASS_V), // Mantle Fault Left
                    new ConstellationStar(5, 70, 85, 1.3f, SpectralClass.CLASS_V)  // Mantle Fault Right
            ),
            List.of(
                    new ConstellationConnection(1, 0),
                    new ConstellationConnection(2, 0),
                    new ConstellationConnection(3, 0),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(3, 5)
            ),
            "Subterranean impactite metamorphism, bedrock shockwaves, and crystal metamorphism.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "yggdrasil")),
            Set.of(0)
    ));

    public static final Constellation VORATOR_STELLARUM = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "vorator_stellarum"),
            "constellation.entropica.vorator_stellarum",
            ConstellationTier.MYTHIC,
            SpectralClass.CLASS_V,
            EssenceType.OBLIVION,
            8.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 50, 2.0f, SpectralClass.CLASS_V), // Singularity Nucleus Star (Shared with Azathoth)
                    new ConstellationStar(1, 30, 20, 1.4f, SpectralClass.CLASS_V), // Upper Maw Left
                    new ConstellationStar(2, 70, 20, 1.4f, SpectralClass.CLASS_V), // Upper Maw Right
                    new ConstellationStar(3, 20, 70, 1.4f, SpectralClass.CLASS_V), // Lower Maw Left
                    new ConstellationStar(4, 80, 70, 1.4f, SpectralClass.CLASS_V), // Lower Maw Right
                    new ConstellationStar(5, 50, 85, 1.2f, SpectralClass.CLASS_V)  // Gullet Singularity
            ),
            List.of(
                    new ConstellationConnection(1, 0),
                    new ConstellationConnection(2, 0),
                    new ConstellationConnection(3, 0),
                    new ConstellationConnection(4, 0),
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(3, 4)
            ),
            "Devours Materia overpressures to prevent machine explosion, nullifying entropy.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "azathoth")),
            Set.of(0)
    ));

    public static final Constellation AETHERION_AETERNUS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "aetherion_aeternus"),
            "constellation.entropica.aetherion_aeternus",
            ConstellationTier.MYTHIC,
            SpectralClass.CLASS_A,
            EssenceType.AETHER,
            7.0f,
            PHASE_NEW,
            List.of(
                    new ConstellationStar(0, 50, 50, 1.8f, SpectralClass.CLASS_A), // Perpetual Cycle Center (Shared with Ouroboros)
                    new ConstellationStar(1, 25, 30, 1.3f, SpectralClass.CLASS_A), // Mobius Loop NW
                    new ConstellationStar(2, 75, 30, 1.3f, SpectralClass.CLASS_A), // Mobius Loop NE
                    new ConstellationStar(3, 75, 70, 1.3f, SpectralClass.CLASS_A), // Mobius Loop SE
                    new ConstellationStar(4, 25, 70, 1.3f, SpectralClass.CLASS_A)  // Mobius Loop SW
            ),
            List.of(
                    new ConstellationConnection(1, 0),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(2, 0),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(3, 4)
            ),
            "Zero flux attenuation across all optical conduits and perpetual starlight resonance.",
            Set.of(ResourceLocation.fromNamespaceAndPath("entropica", "ouroboros")),
            Set.of(0)
    ));

    // ==========================================
    // TIER 5: TRANSCENDENT CONSTELLATIONS (6)
    // STRICT NEW MOON ONLY (Moon Phase 4) & The End
    // ==========================================
    public static final Constellation UMBRA_GENESIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "umbra_genesis"),
            "constellation.entropica.umbra_genesis",
            ConstellationTier.TRANSCENDENT,
            SpectralClass.CLASS_V,
            EssenceType.GENESIS,
            3.0f,
            1 << 4, // Strict New Moon
            List.of(
                    new ConstellationStar(0, 50, 50, 2.0f, SpectralClass.CLASS_V), // Chrysalis Core
                    new ConstellationStar(1, 50, 15, 1.5f, SpectralClass.CLASS_V), // Upper Filament
                    new ConstellationStar(2, 85, 35, 1.3f, SpectralClass.CLASS_V), // Right Wing Spoke
                    new ConstellationStar(3, 75, 75, 1.4f, SpectralClass.CLASS_V), // Lower Right Pod
                    new ConstellationStar(4, 25, 75, 1.4f, SpectralClass.CLASS_V), // Lower Left Pod
                    new ConstellationStar(5, 15, 35, 1.3f, SpectralClass.CLASS_V), // Left Wing Spoke
                    new ConstellationStar(6, 50, 90, 1.5f, SpectralClass.CLASS_V)  // Root Sprout
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(0, 6),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 6),
                    new ConstellationConnection(4, 6),
                    new ConstellationConnection(5, 4),
                    new ConstellationConnection(1, 5)
            ),
            "Increases crystal bud growth on Budding Astral Impactite by +50% and hydrates farmland within 8 blocks."
    ));

    public static final Constellation NIHIL_CORONATUM = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "nihil_coronatum"),
            "constellation.entropica.nihil_coronatum",
            ConstellationTier.TRANSCENDENT,
            SpectralClass.CLASS_V,
            EssenceType.OBLIVION,
            2.0f,
            1 << 4, // Strict New Moon
            List.of(
                    new ConstellationStar(0, 50, 50, 2.2f, SpectralClass.CLASS_V), // Void Crown Null Point
                    new ConstellationStar(1, 30, 20, 1.4f, SpectralClass.CLASS_V), // Crown Spire 1
                    new ConstellationStar(2, 50, 10, 1.6f, SpectralClass.CLASS_V), // Crown Apex Spire
                    new ConstellationStar(3, 70, 20, 1.4f, SpectralClass.CLASS_V), // Crown Spire 3
                    new ConstellationStar(4, 20, 50, 1.3f, SpectralClass.CLASS_V), // Left Ring Horizon
                    new ConstellationStar(5, 80, 50, 1.3f, SpectralClass.CLASS_V), // Right Ring Horizon
                    new ConstellationStar(6, 35, 80, 1.3f, SpectralClass.CLASS_V), // Lower Stasis Anchor
                    new ConstellationStar(7, 65, 80, 1.3f, SpectralClass.CLASS_V)
            ),
            List.of(
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(1, 4),
                    new ConstellationConnection(3, 5),
                    new ConstellationConnection(4, 6),
                    new ConstellationConnection(5, 7),
                    new ConstellationConnection(6, 7),
                    new ConstellationConnection(0, 6),
                    new ConstellationConnection(0, 7)
            ),
            "Slows hostile projectile velocities by 40% and halves incoming negative status effect durations."
    ));

    public static final Constellation ASTRAPE_PRIMORDIALIS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "astrape_primordialis"),
            "constellation.entropica.astrape_primordialis",
            ConstellationTier.TRANSCENDENT,
            SpectralClass.CLASS_O,
            EssenceType.LIGHTNING,
            4.0f,
            1 << 4, // Strict New Moon
            List.of(
                    new ConstellationStar(0, 50, 10, 1.8f, SpectralClass.CLASS_O), // Prime Celestial Fulgurite
                    new ConstellationStar(1, 40, 30, 1.4f, SpectralClass.CLASS_O),
                    new ConstellationStar(2, 60, 45, 1.5f, SpectralClass.CLASS_O),
                    new ConstellationStar(3, 35, 65, 1.3f, SpectralClass.CLASS_O),
                    new ConstellationStar(4, 55, 80, 1.4f, SpectralClass.CLASS_O),
                    new ConstellationStar(5, 50, 95, 1.6f, SpectralClass.CLASS_O)  // Earth Ground Point
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 5)
            ),
            "Safely grounds natural lightning at altars, converting strikes into +250 mB pure Materia."
    ));

    public static final Constellation AEGIS_ECLIPTICA = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "aegis_ecliptica"),
            "constellation.entropica.aegis_ecliptica",
            ConstellationTier.TRANSCENDENT,
            SpectralClass.CLASS_G,
            EssenceType.AEGIS,
            3.0f,
            1 << 4, // Strict New Moon
            List.of(
                    new ConstellationStar(0, 50, 50, 2.0f, SpectralClass.CLASS_G), // Bastion Core
                    new ConstellationStar(1, 50, 15, 1.4f, SpectralClass.CLASS_G), // North Crest
                    new ConstellationStar(2, 85, 30, 1.4f, SpectralClass.CLASS_G), // NE Boss
                    new ConstellationStar(3, 80, 70, 1.4f, SpectralClass.CLASS_G), // SE Rim
                    new ConstellationStar(4, 50, 90, 1.6f, SpectralClass.CLASS_G), // South Keel
                    new ConstellationStar(5, 20, 70, 1.4f, SpectralClass.CLASS_G), // SW Rim
                    new ConstellationStar(6, 15, 30, 1.4f, SpectralClass.CLASS_G)  // NW Boss
            ),
            List.of(
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 5),
                    new ConstellationConnection(5, 6),
                    new ConstellationConnection(6, 1),
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(0, 6)
            ),
            "Grants Resistance I in beacon sanctuary and reduces explosion damage to multiblocks by 75%."
    ));

    public static final Constellation IGNIS_ESCHATON = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "ignis_eschaton"),
            "constellation.entropica.ignis_eschaton",
            ConstellationTier.TRANSCENDENT,
            SpectralClass.CLASS_K,
            EssenceType.PYRE,
            4.0f,
            1 << 4, // Strict New Moon
            List.of(
                    new ConstellationStar(0, 50, 50, 2.2f, SpectralClass.CLASS_K), // Dying Star Heart
                    new ConstellationStar(1, 30, 25, 1.3f, SpectralClass.CLASS_K), // Corona Flare 1
                    new ConstellationStar(2, 70, 25, 1.3f, SpectralClass.CLASS_K), // Corona Flare 2
                    new ConstellationStar(3, 85, 50, 1.4f, SpectralClass.CLASS_K), // Corona Flare 3
                    new ConstellationStar(4, 70, 75, 1.3f, SpectralClass.CLASS_K), // Corona Flare 4
                    new ConstellationStar(5, 30, 75, 1.3f, SpectralClass.CLASS_K), // Corona Flare 5
                    new ConstellationStar(6, 15, 50, 1.4f, SpectralClass.CLASS_K)  // Corona Flare 6
            ),
            List.of(
                    new ConstellationConnection(0, 1),
                    new ConstellationConnection(0, 2),
                    new ConstellationConnection(0, 3),
                    new ConstellationConnection(0, 4),
                    new ConstellationConnection(0, 5),
                    new ConstellationConnection(0, 6),
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 5),
                    new ConstellationConnection(5, 6),
                    new ConstellationConnection(6, 1)
            ),
            "Boosts altar dry-infusion speed by +25% and accelerates adjacent smelting by +30%."
    ));

    public static final Constellation AETHER_AETERNITAS = register(new Constellation(
            ResourceLocation.fromNamespaceAndPath("entropica", "aether_aeternitas"),
            "constellation.entropica.aether_aeternitas",
            ConstellationTier.TRANSCENDENT,
            SpectralClass.CLASS_A,
            EssenceType.AETHER,
            2.0f,
            1 << 4, // Strict New Moon
            List.of(
                    new ConstellationStar(0, 50, 50, 2.0f, SpectralClass.CLASS_A), // Celestial Loom Pivot
                    new ConstellationStar(1, 20, 20, 1.4f, SpectralClass.CLASS_A), // Warp Star NW
                    new ConstellationStar(2, 80, 20, 1.4f, SpectralClass.CLASS_A), // Warp Star NE
                    new ConstellationStar(3, 80, 80, 1.4f, SpectralClass.CLASS_A), // Weft Star SE
                    new ConstellationStar(4, 20, 80, 1.4f, SpectralClass.CLASS_A), // Weft Star SW
                    new ConstellationStar(5, 50, 15, 1.2f, SpectralClass.CLASS_A), // Aether Shuttle North
                    new ConstellationStar(6, 50, 85, 1.2f, SpectralClass.CLASS_A)  // Aether Shuttle South
            ),
            List.of(
                    new ConstellationConnection(1, 2),
                    new ConstellationConnection(2, 3),
                    new ConstellationConnection(3, 4),
                    new ConstellationConnection(4, 1),
                    new ConstellationConnection(1, 3),
                    new ConstellationConnection(2, 4),
                    new ConstellationConnection(5, 0),
                    new ConstellationConnection(6, 0)
            ),
            "Minimizes optical signal decay across Pure Optic Fibers from 1% down to 0.25% per 16 blocks."
    ));

    private static int registerIndex = 0;

    private static Constellation register(Constellation constellation) {
        float azim;
        float alt;
        if (registerIndex < 24) {
            // Original 24 Constellations: Spans azimuths (0°..345°) and declinations down to -10°
            azim = registerIndex * 15.0f; // 0°, 15°, 30°, ... 345°
            alt = -10.0f + ((registerIndex * 21.0f) % 86.0f);
        } else {
            // Additive 26 Constellations (indices 24 to 49): Interleaved azimuths and declinations down to -12°
            int offset = registerIndex - 24;
            azim = 7.5f + (offset * (360.0f / 26.0f));
            alt = -12.0f + ((offset * 25.5f) % 88.0f);
        }
        constellation.setCelestialCoordinates(azim, alt);
        registerIndex++;

        CONSTELLATIONS.put(constellation.getId(), constellation);
        return constellation;
    }

    public static Collection<Constellation> getAllConstellations() {
        return Collections.unmodifiableCollection(CONSTELLATIONS.values());
    }

    public static Optional<Constellation> getById(ResourceLocation id) {
        return Optional.ofNullable(CONSTELLATIONS.get(id));
    }

    public static Optional<Constellation> findByNameOrId(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        ResourceLocation id = ResourceLocation.tryParse(name.contains(":") ? name : "entropica:" + name.toLowerCase());
        if (id != null && CONSTELLATIONS.containsKey(id)) {
            return Optional.of(CONSTELLATIONS.get(id));
        }
        for (Constellation c : CONSTELLATIONS.values()) {
            if (c.getId().getPath().equalsIgnoreCase(name)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public static List<Constellation> getVisibleConstellations(int moonPhase) {
        List<Constellation> list = new ArrayList<>();
        for (Constellation c : CONSTELLATIONS.values()) {
            if (c.isVisibleDuringMoonPhase(moonPhase)) {
                list.add(c);
            }
        }
        return list;
    }

    public static List<Constellation> getByTier(ConstellationTier tier) {
        List<Constellation> list = new ArrayList<>();
        for (Constellation c : CONSTELLATIONS.values()) {
            if (c.getTier() == tier) {
                list.add(c);
            }
        }
        return list;
    }
}
