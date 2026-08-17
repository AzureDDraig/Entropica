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
            "Kinetic projectile velocity, arrow trajectory straightening, and wind currents."
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
            "Hostile mob pacification, anti-spawning daylight fields, and luminous warding."
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
            "Armor-piercing toxic starlight, shadow phasing, and umbral venom."
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
            "Crop acceleration, sapling maturation, botanical proliferation, and soil hydration."
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
            "In-world ore cluster smelting without fuel consumption and magma distillation."
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
            "Slow falling, reduced gravity, fall damage negation, and elytra propulsion."
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
            "Transmutes raw stone into random native ores and earth geode proliferation."
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
            "60% enemy movement slowing, frost mist aura, and fire extinguishment."
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
            "100% movement speed distortion, frictionless ice traversal, and pathfinding navigation."
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
            "Item despawn prevention, amber stasis fields, and entity preservation."
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
            "Converts nearby mob damage into pure player life essence and sanguine leech."
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
            "Wireless atmospheric charging of nearby Orbis Cells and galvanic arcs."
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
            "Accelerates tile entity tick rates by up to 1000% and chronomantic distortion."
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
            "Pulls dropped items, XP orbs, and meteors into central hoppers and void maw suction."
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
            "Intangibility ward protecting multiblock structures from explosion and damage."
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
            "Permanent noon solar intensity, daytime starlight lock, and radiance boost."
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
            "Frictionless creative flight, void fall safety immunity, and deep ocean mastery."
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
            "5-minute cooldown death cheat ward, tool durability auto-repair, and primordial rebirth."
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
            "0% Materia transfer decay, thermal overpressure immunity, and perpetual energy equilibrium."
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
            "Complete temporal stasis, inter-dimensional cosmic rift creation, and void dissolution."
    ));

    private static Constellation register(Constellation constellation) {
        CONSTELLATIONS.put(constellation.getId(), constellation);
        return constellation;
    }

    public static Collection<Constellation> getAllConstellations() {
        return Collections.unmodifiableCollection(CONSTELLATIONS.values());
    }

    public static Optional<Constellation> getById(ResourceLocation id) {
        return Optional.ofNullable(CONSTELLATIONS.get(id));
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
