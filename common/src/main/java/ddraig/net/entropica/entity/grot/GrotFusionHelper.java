package ddraig.net.entropica.entity.grot;

import ddraig.net.entropica.api.EssenceType;

public class GrotFusionHelper {

    /**
     * Checks if two Essence Types can form a valid Tier 1 or Tier 2 Fusion.
     * Returns the fused EssenceType, or null if they are incompatible.
     */
    public static EssenceType getFusion(EssenceType t1, EssenceType t2) {
        if (t1 == null || t2 == null) return null;

        // Order independence
        EssenceType a = t1.ordinal() < t2.ordinal() ? t1 : t2;
        EssenceType b = t1.ordinal() < t2.ordinal() ? t2 : t1;

        if (a == EssenceType.AIR && b == EssenceType.WATER) return EssenceType.STORM;
        if (a == EssenceType.WATER && b == EssenceType.FROZEN) return EssenceType.GLACIAL;
        if (a == EssenceType.NATURE && b == EssenceType.EARTH) return EssenceType.OVERGROWTH;
        if (a == EssenceType.AIR && b == EssenceType.EARTH) return EssenceType.DUST;
        if (a == EssenceType.AIR && b == EssenceType.NATURE) return EssenceType.SPORE;
        if (a == EssenceType.NATURE && b == EssenceType.FROZEN) return EssenceType.TAIGA;
        if (a == EssenceType.NATURE && b == EssenceType.ARID) return EssenceType.OASIS;
        if (a == EssenceType.EARTH && b == EssenceType.NETHER) return EssenceType.MAGMA;

        if (a == EssenceType.RADIANT && b == EssenceType.UMBRAL) return EssenceType.ECLIPSE;
        if (a == EssenceType.NATURE && b == EssenceType.UNDEAD) return EssenceType.BLIGHT;
        if (a == EssenceType.VOID && b == EssenceType.LIGHTNING) return EssenceType.ASTRAL;
        if (a == EssenceType.NETHER && b == EssenceType.UNDEAD) return EssenceType.SOULFIRE;
        if (a == EssenceType.WATER && b == EssenceType.NETHER) return EssenceType.VAPOR;
        if (a == EssenceType.AIR && b == EssenceType.RADIANT) return EssenceType.DAWN;
        if (a == EssenceType.WATER && b == EssenceType.UMBRAL) return EssenceType.ABYSS;
        if (a == EssenceType.EARTH && b == EssenceType.RADIANT) return EssenceType.AEGIS;
        if (a == EssenceType.FROZEN && b == EssenceType.RADIANT) return EssenceType.AURORA;
        if (a == EssenceType.RADIANT && b == EssenceType.ARID) return EssenceType.MIRAGE;
        if (a == EssenceType.UNDEAD && b == EssenceType.VOID) return EssenceType.WRAITH;
        if (a == EssenceType.EARTH && b == EssenceType.UNDEAD) return EssenceType.BARROW;

        return null;
    }
}