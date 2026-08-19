package ddraig.net.entropica.astral;

import ddraig.net.entropica.api.EssenceType;

public enum StellarRemnantType {
    BLACK_HOLE(
            "Stellar Black Hole",
            "Gravitational Singularity with Accretion Disk & Relativistic Jets",
            SpectralClass.CLASS_V,
            new EssenceType[]{EssenceType.SINGULARITY, EssenceType.VOID, EssenceType.ESCHATON, EssenceType.NULL_U, EssenceType.ABYSS, EssenceType.OBLIVION, EssenceType.CHRONOS},
            0xFF110022,
            0.0f
    ),
    PULSAR(
            "Pulsar (Neutron Star)",
            "Hyper-Dense Rapidly Rotating Magnetized Core with Radiation Cones",
            SpectralClass.CLASS_B,
            new EssenceType[]{EssenceType.STATIC, EssenceType.LIGHTNING, EssenceType.VOLT, EssenceType.KINETIC, EssenceType.AXIOM},
            0xFF85B5FF,
            4.0f
    ),
    MAGNETAR(
            "Magnetar",
            "Ultra-Magnetic Core with High-Energy Coronal Flares & UV Emission",
            SpectralClass.CLASS_O,
            new EssenceType[]{EssenceType.PHOTON, EssenceType.AURA, EssenceType.ASTRAL, EssenceType.ECHO, EssenceType.FERVOR},
            0xFF387BFF,
            3.5f
    ),
    WHITE_DWARF(
            "White Dwarf",
            "Crystalline Electron-Degenerate Core with Intense Gravitational Field",
            SpectralClass.CLASS_A,
            new EssenceType[]{EssenceType.DENSITY, EssenceType.COHESION, EssenceType.RADIANT, EssenceType.APOTHEOSIS},
            0xFFFFFFFF,
            2.0f
    ),
    STRANGE_STAR(
            "Strange Quark Star",
            "Exotic Deconfined Quark Matter with Chromatic Equilibrium Aura",
            SpectralClass.CLASS_O,
            new EssenceType[]{EssenceType.ENTROPICA, EssenceType.AETHER, EssenceType.PRISMATIC, EssenceType.CHIMERA},
            0xFFE080FF,
            3.0f
    ),
    COLLAPSAR(
            "Hypergiant Collapsar",
            "Super-Heated Turbulent Core on the Verge of Equilibrium Rupture",
            SpectralClass.CLASS_M,
            new EssenceType[]{EssenceType.PYRE, EssenceType.CATACLYSM, EssenceType.GENESIS, EssenceType.EMPYREAN},
            0xFFFF4538,
            2.8f
    );

    private final String title;
    private final String description;
    private final SpectralClass spectralClass;
    private final EssenceType[] possibleEssences;
    private final int primaryColorRgb;
    private final float radianceFactor;

    StellarRemnantType(String title, String description, SpectralClass spectralClass, EssenceType[] possibleEssences, int primaryColorRgb, float radianceFactor) {
        this.title = title;
        this.description = description;
        this.spectralClass = spectralClass;
        this.possibleEssences = possibleEssences;
        this.primaryColorRgb = primaryColorRgb;
        this.radianceFactor = radianceFactor;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public SpectralClass getSpectralClass() {
        return spectralClass;
    }

    public EssenceType[] getPossibleEssences() {
        return possibleEssences;
    }

    public int getPrimaryColorRgb() {
        return primaryColorRgb;
    }

    public float getRadianceFactor() {
        return radianceFactor;
    }

    public EssenceType pickEssence(long seed) {
        int idx = (int) (Math.abs(seed) % possibleEssences.length);
        return possibleEssences[idx];
    }
}
