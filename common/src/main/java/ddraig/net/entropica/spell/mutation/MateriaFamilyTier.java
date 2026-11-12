package ddraig.net.entropica.spell.mutation;

import ddraig.net.entropica.api.EssenceType;

/**
 * Classifies every {@link EssenceType} into its canonical progression family
 * for Tier 4-6 mutation scaling, Mode 3 Family Mastery unlocks, and lineage traits.
 */
public enum MateriaFamilyTier {
    REGULAR("regular", "Unattuned Regular", 0, false, 0.0F, 0),
    PRIMORDIAL_BASE("primordial_base", "Primordial Base", 1, false, 0.0F, 0),
    ELEMENTAL_FUSION("elemental_fusion", "Tier 1-2 Elemental Fusions", 2, false, 0.10F, 1),
    VITAL_FUSION("vital_fusion", "Vital Fusions", 2, false, 0.15F, 1),
    ETHEREAL_PARADOX("ethereal_paradox", "Ethereal & Paradox Fusions", 2, false, 0.20F, 2),
    TRINITY_FUSION("trinity_fusion", "Tier 3 Trinity Fusions", 3, false, 0.30F, 3),
    CATALYST("catalyst", "Catalytic Fusions", 4, false, 0.40F, 4),
    APEX_FUSION("apex_fusion", "Tier 4 Apex Fusions", 4, true, 0.50F, 6),
    ESCHATON_FUSION("eschaton_fusion", "Tier 5 Eschaton Fusions", 5, true, 0.60F, 7),
    SINGULARITY_FUSION("singularity_fusion", "Tier 6 Singularity Fusions", 6, true, 0.75F, 8),
    SECRET_FUSION("secret_fusion", "Caprine Secret Fusions", 3, false, 0.35F, 3),
    CONCEPTUAL_FRAGMENT("conceptual_fragment", "Conceptual Fragments", 1, false, 0.0F, 0);

    private final String id;
    private final String displayName;
    private final int fusionTier;
    private final boolean innateWallPhasing;
    private final float baseArmorBypass;
    private final int baseArmorShred;

    MateriaFamilyTier(String id, String displayName, int fusionTier,
                      boolean innateWallPhasing, float baseArmorBypass, int baseArmorShred) {
        this.id = id;
        this.displayName = displayName;
        this.fusionTier = fusionTier;
        this.innateWallPhasing = innateWallPhasing;
        this.baseArmorBypass = baseArmorBypass;
        this.baseArmorShred = baseArmorShred;
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getFusionTier() {
        return this.fusionTier;
    }

    public boolean hasInnateWallPhasing() {
        return this.innateWallPhasing;
    }

    public float getBaseArmorBypass() {
        return this.baseArmorBypass;
    }

    public int getBaseArmorShred() {
        return this.baseArmorShred;
    }

    public int getNumericTier() {
        return this.fusionTier;
    }

    public float getArmorBypassFraction() {
        return this.baseArmorBypass;
    }

    public int getBonusArmorShredPoints() {
        return this.baseArmorShred;
    }

    public static MateriaFamilyTier classify(EssenceType type) {
        return of(type);
    }

    public static boolean isNetherLineage(EssenceType type) {
        return isNetherSmeltLineage(type);
    }

    public static boolean isVoidLineage(EssenceType type) {
        return isVoidPhasingLineage(type);
    }

    public static boolean isMonsterEmpowermentHex(EssenceType type) {
        return isMonsterEmpowermentHexElement(type);
    }

    public String getFamilyMasteryResearchNodeId() {
        return "spell_family_mastery_" + this.id;
    }

    public static MateriaFamilyTier of(EssenceType type) {
        if (type == null || type == EssenceType.REGULAR) return REGULAR;
        if (type.isFragment()) return CONCEPTUAL_FRAGMENT;

        return switch (type) {
            case AIR, WATER, NATURE, EARTH, FROZEN, NETHER, RADIANT, UMBRAL, UNDEAD, VOID, ARID, LIGHTNING -> PRIMORDIAL_BASE;
            case MAGMA, STORM, GLACIAL, OVERGROWTH, DUST, SPORE, TAIGA, OASIS -> ELEMENTAL_FUSION;
            case VITAE, BLOOD, AURA, AMBER -> VITAL_FUSION;
            case ECLIPSE, BLIGHT, ASTRAL, SOULFIRE, VAPOR, NULL_R, NULL_U, DAWN, AUREOLE, VESPER, ABYSS, AEGIS, AURORA, MIRAGE, WRAITH, BARROW -> ETHEREAL_PARADOX;
            case PYRE, PENUMBRA, RIME, SPRING, STATIC, SYLVAN, MIASMA, GENESIS, OBLIVION -> TRINITY_FUSION;
            case CHIMERA, PRISMATIC -> CATALYST;
            case AETHER, ENTROPIC, CELESTIAL -> APEX_FUSION;
            case EMPYREAN, CATACLYSM, TERMINUS -> ESCHATON_FUSION;
            case ESCHATON, APOTHEOSIS, ENTROPICA -> SINGULARITY_FUSION;
            case RADIANT_FIRE, UMBRAL_WATER, EQUINOX -> SECRET_FUSION;
            default -> PRIMORDIAL_BASE;
        };
    }

    public static boolean isEarthLineage(EssenceType type) {
        if (type == null) return false;
        return switch (type) {
            case EARTH, ARID, MAGMA, OVERGROWTH, DUST, OASIS, AMBER, AEGIS, BARROW, PYRE, SYLVAN, AETHER, CATACLYSM, ENTROPICA -> true;
            default -> false;
        };
    }

    public static boolean isFrozenLineage(EssenceType type) {
        if (type == null) return false;
        return switch (type) {
            case FROZEN, GLACIAL, TAIGA, AURORA, RIME, TERMINUS -> true;
            default -> false;
        };
    }

    public static boolean isNetherSmeltLineage(EssenceType type) {
        if (type == null) return false;
        return switch (type) {
            case NETHER, MAGMA, SOULFIRE, PYRE, OBLIVION, RADIANT_FIRE, CATACLYSM -> true;
            default -> false;
        };
    }

    public static boolean isVoidPhasingLineage(EssenceType type) {
        if (type == null) return false;
        if (of(type).hasInnateWallPhasing()) return true;
        return switch (type) {
            case VOID, ASTRAL, NULL_R, NULL_U, WRAITH, PENUMBRA -> true;
            default -> false;
        };
    }

    public static boolean isSpawnLureHexElement(EssenceType type) {
        if (type == null) return false;
        return switch (type) {
            case UNDEAD, VOID, BLOOD, ASTRAL, NULL_R, BARROW, MIASMA, OBLIVION, ENTROPIC, EMPYREAN, CATACLYSM, ESCHATON, ENTROPICA -> true;
            default -> false;
        };
    }

    public static boolean isMonsterEmpowermentHexElement(EssenceType type) {
        if (type == null) return false;
        return switch (type) {
            case UMBRAL, ARID, BLIGHT, SOULFIRE, ABYSS, AEGIS, MIRAGE, CHIMERA, AETHER, TERMINUS, APOTHEOSIS, ENTROPICA -> true;
            default -> false;
        };
    }
}
