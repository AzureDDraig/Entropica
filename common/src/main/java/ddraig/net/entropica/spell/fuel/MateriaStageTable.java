package ddraig.net.entropica.spell.fuel;

/**
 * Canonical 10-Stage Materia Conversion, Fuel Cost, Combat Damage, Ward Shield,
 * and Healing Scaling Table (Section 7.1 & DecompressionCouplerBlockEntity).
 *
 * <p>All internal fuel arithmetic is expressed in exact {@code long} T2 Materia Fumus
 * equivalents ({@code Mfum}) so multi-part multi-Materia splits across Tiers 1-10
 * suffer zero rounding distortion.</p>
 */
public final class MateriaStageTable {

    /**
     * Exact conversion value of 1 native unit of Stage [tier] in T2 Fumus (Mfum):
     * <ul>
     *     <li>T1 Raw Essence = 8 Mfum</li>
     *     <li>T2 Fumus = 1 Mfum</li>
     *     <li>T3 Sublimata = 360 Mfum</li>
     *     <li>T4 Liquida = 6 * 360 = 2,160 Mfum</li>
     *     <li>T5 Volatilis = 2 * 2,160 = 4,320 Mfum</li>
     *     <li>T6 Coagulata = 16 * 4,320 = 69,120 Mfum</li>
     *     <li>T7 Ichor = 4 * 69,120 = 276,480 Mfum</li>
     *     <li>T8 Transmutata = 5 * 276,480 = 1,382,400 Mfum</li>
     *     <li>T9 Perfecta = 5 * 1,382,400 = 6,912,000 Mfum</li>
     *     <li>T10 Liminalia = 5 * 6,912,000 = 34,560,000 Mfum</li>
     * </ul>
     */
    private static final long[] MFUM_PER_NATIVE_UNIT = {
            8L,           // Tier 1: Raw Essence (Mraw)
            1L,           // Tier 2: Materia Fumus (Mfum)
            360L,         // Tier 3: Materia Sublimata (Msub)
            2_160L,       // Tier 4: Materia Liquida (Mliq)
            4_320L,       // Tier 5: Materia Volatilis (Mvol)
            69_120L,      // Tier 6: Materia Coagulata (Mcoa)
            276_480L,     // Tier 7: Materia Ichor (Mich)
            1_382_400L,   // Tier 8: Materia Transmutata (Mtra)
            6_912_000L,   // Tier 9: Materia Perfecta (Mper)
            34_560_000L   // Tier 10: Materia Liminalia (Mlim)
    };

    private static final int[] BASE_NATIVE_CAST_COST = {
            4,    // Tier 1: 4 Mraw (32 Mfum)
            100,  // Tier 2: 100 Mfum
            5,    // Tier 3: 5 Msub (1,800 Mfum)
            5,    // Tier 4: 5 Mliq (10,800 Mfum)
            5,    // Tier 5: 5 Mvol (21,600 Mfum)
            2,    // Tier 6: 2 Mcoa (138,240 Mfum)
            2,    // Tier 7: 2 Mich (552,960 Mfum)
            1,    // Tier 8: 1 Mtra (1,382,400 Mfum)
            1,    // Tier 9: 1 Mper (6,912,000 Mfum)
            1     // Tier 10: 1 Mlim (34,560,000 Mfum)
    };

    private static final float[] BASE_COMBAT_DAMAGE_HP = {
            16.0F,    // Tier 1 (4^2)
            25.0F,    // Tier 2 (5^2)
            36.0F,    // Tier 3 (6^2)
            49.0F,    // Tier 4 (7^2)
            64.0F,    // Tier 5 (8^2)
            150.0F,   // Tier 6
            300.0F,   // Tier 7
            600.0F,   // Tier 8
            1250.0F,  // Tier 9
            2500.0F   // Tier 10
    };

    private static final String[] STAGE_CODE = {
            "Mraw", "Mfum", "Msub", "Mliq", "Mvol",
            "Mcoa", "Mich", "Mtra", "Mper", "Mlim"
    };

    private MateriaStageTable() {
    }

    public static long getMfumPerNativeUnit(int tier) {
        int idx = Math.clamp(tier, 1, 10) - 1;
        return MFUM_PER_NATIVE_UNIT[idx];
    }

    public static int getBaseNativeCastCost(int tier) {
        int idx = Math.clamp(tier, 1, 10) - 1;
        return BASE_NATIVE_CAST_COST[idx];
    }

    public static long getBaseMfumCastCost(int tier) {
        return getBaseNativeCastCost(tier) * getMfumPerNativeUnit(tier);
    }

    public static long getMagicCircleRitualBaseMfum(int tier) {
        return getBaseMfumCastCost(tier) * 10L;
    }

    public static float getBaseCombatDamageHp(int tier) {
        int idx = Math.clamp(tier, 1, 10) - 1;
        return BASE_COMBAT_DAMAGE_HP[idx];
    }

    public static float getBaseWardShieldHp(int tier) {
        return getBaseCombatDamageHp(tier) * 1.5F;
    }

    public static float getBaseRestorationHealHp(int tier) {
        return getBaseCombatDamageHp(tier) * 0.5F;
    }

    public static String getStageUnitCode(int tier) {
        int idx = Math.clamp(tier, 1, 10) - 1;
        return STAGE_CODE[idx];
    }

    public static double toNativeUnits(long mfumAmount, int tier) {
        return (double) mfumAmount / (double) getMfumPerNativeUnit(tier);
    }

    /**
     * Applies Section 7.3 Cross-Tier Spell Fueling rules:
     * <ul>
     *     <li>If {@code sourceFuelTier >= spellTier}: 0% bonus, 0% penalty (1.0x exact table conversion).</li>
     *     <li>If {@code sourceFuelTier < spellTier}: +25% markup per tier jumped!</li>
     * </ul>
     */
    public static long adjustMfumForFuelTier(long baseSpellMfum, int spellTier, int sourceFuelTier) {
        int clampedSpell = Math.clamp(spellTier, 1, 10);
        int clampedSource = Math.clamp(sourceFuelTier, 1, 10);
        if (clampedSource >= clampedSpell) {
            return baseSpellMfum;
        }
        int deltaTiers = clampedSpell - clampedSource;
        double markup = 1.0D + (0.25D * deltaTiers);
        return Math.max(1L, Math.round(baseSpellMfum * markup));
    }
}
