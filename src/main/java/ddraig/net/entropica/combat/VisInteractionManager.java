package ddraig.net.entropica.combat;

import ddraig.net.entropica.api.EssenceType;
import java.util.*;

public class VisInteractionManager {

    private static final VisInteractionManager INSTANCE = new VisInteractionManager();

    // Mapped via String Profile ID to allow for altered Dominance states without bloating the EssenceType Enum
    private final Map<String, Set<EssenceType>> strongAgainstMap = new HashMap<>();
    private final Map<String, Set<EssenceType>> weakAgainstMap = new HashMap<>();

    private VisInteractionManager() {
        setupHardcodedDefaults();
    }

    public static VisInteractionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Compares an attacking profile against a defending element and returns the point value.
     * +1 = Strong Against | -1 = Weak Against | 0 = Neutral
     */
    public int getAdvantagePoints(String attackerProfileId, EssenceType defender) {
        int score = 0;
        if (strongAgainstMap.getOrDefault(attackerProfileId, Collections.emptySet()).contains(defender)) score += 1;
        if (weakAgainstMap.getOrDefault(attackerProfileId, Collections.emptySet()).contains(defender)) score -= 1;
        return score;
    }

    /**
     * Helper to compare multiple elements (for weapons with multiple slotted fragments/types)
     */
    public int calculateTotalVisScore(Collection<String> attackerProfiles, Collection<EssenceType> defenders) {
        int totalScore = 0;
        for (String atk : attackerProfiles) {
            for (EssenceType def : defenders) {
                totalScore += getAdvantagePoints(atk, def);
            }
        }
        return totalScore;
    }

    private void setupHardcodedDefaults() {
        strongAgainstMap.clear();
        weakAgainstMap.clear();

        // ==========================================
        // 1. PRIMORDIAL (BASE) MATCHUPS
        // ==========================================
        addBaseRule(EssenceType.AIR, List.of(EssenceType.EARTH), List.of(EssenceType.VOID));
        addBaseRule(EssenceType.EARTH, List.of(EssenceType.VOID), List.of(EssenceType.AIR));
        addBaseRule(EssenceType.VOID, List.of(EssenceType.AIR), List.of(EssenceType.EARTH));
        addBaseRule(EssenceType.NETHER, List.of(EssenceType.FROZEN), List.of(EssenceType.WATER));
        addBaseRule(EssenceType.FROZEN, List.of(EssenceType.WATER), List.of(EssenceType.NETHER, EssenceType.UNDEAD));
        addBaseRule(EssenceType.WATER, List.of(EssenceType.NETHER, EssenceType.ARID), List.of(EssenceType.FROZEN, EssenceType.ARID, EssenceType.NATURE));
        addBaseRule(EssenceType.NATURE, List.of(EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL));
        addBaseRule(EssenceType.UMBRAL, List.of(EssenceType.RADIANT), List.of(EssenceType.VOID));
        addBaseRule(EssenceType.RADIANT, List.of(EssenceType.UNDEAD), List.of(EssenceType.UMBRAL));
        addBaseRule(EssenceType.UNDEAD, List.of(EssenceType.FROZEN), List.of(EssenceType.RADIANT));
        addBaseRule(EssenceType.ARID, List.of(EssenceType.WATER), List.of(EssenceType.WATER)); // Mutually volatile

        // ==========================================
        // 2. TIER 1 FUSIONS (Base & Dominance Inverted)
        // ==========================================
        addBaseRule(EssenceType.MAGMA, List.of(EssenceType.FROZEN, EssenceType.NATURE), List.of(EssenceType.AIR, EssenceType.WATER));
        addRule("magma_earth_dom", List.of(EssenceType.VOID), List.of(EssenceType.AIR, EssenceType.WATER, EssenceType.NATURE));

        addBaseRule(EssenceType.GLACIAL, List.of(EssenceType.NETHER, EssenceType.ARID, EssenceType.NATURE), List.of(EssenceType.NATURE, EssenceType.NETHER, EssenceType.UNDEAD));
        addRule("glacial_water_dom", List.of(EssenceType.ARID), List.of(EssenceType.NATURE, EssenceType.UNDEAD)); // Nether neutralized

        addBaseRule(EssenceType.OVERGROWTH, List.of(EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL, EssenceType.AIR));
        addRule("overgrowth_earth_dom", List.of(EssenceType.VOID), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL, EssenceType.AIR));

        addBaseRule(EssenceType.VITAE, List.of(EssenceType.UNDEAD), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL));
        addRule("vitae_nature_dom", List.of(EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL));

        addBaseRule(EssenceType.ECLIPSE, List.of(EssenceType.RADIANT, EssenceType.NATURE), List.of(EssenceType.VOID));
        addRule("eclipse_radiant_dom", List.of(EssenceType.UNDEAD), List.of(EssenceType.VOID));

        addBaseRule(EssenceType.BLIGHT, List.of(EssenceType.FROZEN), List.of(EssenceType.RADIANT, EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL));
        addRule("blight_nature_dom", List.of(EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.RADIANT, EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL));

        addBaseRule(EssenceType.SOULFIRE, List.of(EssenceType.FROZEN, EssenceType.NATURE), List.of(EssenceType.WATER, EssenceType.RADIANT));
        addRule("soulfire_undead_dom", List.of(EssenceType.FROZEN), List.of(EssenceType.WATER, EssenceType.RADIANT));

        addBaseRule(EssenceType.VAPOR, List.of(EssenceType.ARID, EssenceType.NETHER), List.of(EssenceType.FROZEN, EssenceType.NATURE, EssenceType.ARID));
        addRule("vapor_nether_dom", List.of(EssenceType.FROZEN, EssenceType.NATURE), List.of(EssenceType.FROZEN, EssenceType.NATURE, EssenceType.ARID, EssenceType.WATER));

        // Fixed NULL_R and NULL_U mappings
        addBaseRule(EssenceType.NULL_R, List.of(EssenceType.AIR, EssenceType.UMBRAL), List.of(EssenceType.UMBRAL, EssenceType.EARTH));
        addRule("null_r_radiant_dom", List.of(EssenceType.UNDEAD), List.of(EssenceType.UMBRAL, EssenceType.EARTH));

        addBaseRule(EssenceType.NULL_U, List.of(EssenceType.RADIANT, EssenceType.NATURE), List.of(EssenceType.EARTH, EssenceType.VOID));
        addRule("null_u_void_dom", List.of(EssenceType.AIR), List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT));

        addBaseRule(EssenceType.SPORE, List.of(EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL, EssenceType.VOID));
        addRule("spore_air_dom", List.of(EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL, EssenceType.VOID));

        addBaseRule(EssenceType.TAIGA, List.of(EssenceType.WATER, EssenceType.NATURE), List.of(EssenceType.NETHER, EssenceType.UMBRAL, EssenceType.UNDEAD));
        addRule("taiga_nature_dom", List.of(EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.UMBRAL, EssenceType.UNDEAD));

        addBaseRule(EssenceType.DAWN, List.of(EssenceType.UNDEAD), List.of(EssenceType.UMBRAL, EssenceType.VOID));
        addRule("dawn_air_dom", List.of(EssenceType.EARTH), List.of(EssenceType.UMBRAL, EssenceType.VOID));

        addBaseRule(EssenceType.ABYSS, List.of(EssenceType.RADIANT, EssenceType.NATURE), List.of(EssenceType.VOID, EssenceType.FROZEN));
        addRule("abyss_water_dom", List.of(EssenceType.NETHER, EssenceType.ARID), List.of(EssenceType.VOID, EssenceType.FROZEN, EssenceType.NATURE));

        addBaseRule(EssenceType.AEGIS, List.of(EssenceType.VOID), List.of(EssenceType.UMBRAL, EssenceType.AIR, EssenceType.NATURE));
        addRule("aegis_radiant_dom", List.of(EssenceType.UNDEAD), List.of(EssenceType.UMBRAL, EssenceType.AIR, EssenceType.NATURE));

        addBaseRule(EssenceType.AURORA, List.of(EssenceType.UNDEAD), List.of(EssenceType.UMBRAL, EssenceType.NETHER));
        addRule("aurora_frozen_dom", List.of(EssenceType.WATER, EssenceType.NATURE), List.of(EssenceType.UMBRAL, EssenceType.NETHER, EssenceType.UNDEAD));

        // ==========================================
        // 3. TIER 2 FUSIONS
        // ==========================================
        addBaseRule(EssenceType.LIGHTNING, List.of(EssenceType.EARTH, EssenceType.WATER), List.of(EssenceType.VOID, EssenceType.WATER));
        addRule("lightning_arid_dom", List.of(EssenceType.WATER, EssenceType.VOID), List.of(EssenceType.VOID, EssenceType.WATER, EssenceType.NATURE));

        addBaseRule(EssenceType.STORM, List.of(EssenceType.EARTH, EssenceType.GLACIAL), List.of(EssenceType.NATURE));
        addRule("storm_air_dom", List.of(EssenceType.EARTH), List.of(EssenceType.NATURE, EssenceType.VOID));

        addBaseRule(EssenceType.DUST, List.of(EssenceType.WATER, EssenceType.VOID, EssenceType.VAPOR), List.of(EssenceType.WATER, EssenceType.VOID));
        addRule("dust_air_dom", List.of(EssenceType.EARTH), List.of(EssenceType.WATER, EssenceType.VOID));

        addBaseRule(EssenceType.BLOOD, List.of(EssenceType.FROZEN, EssenceType.NATURE, EssenceType.SPRING), List.of(EssenceType.RADIANT, EssenceType.NETHER, EssenceType.UMBRAL));
        addRule("blood_vitae_dom", List.of(EssenceType.UNDEAD), List.of(EssenceType.RADIANT, EssenceType.NETHER, EssenceType.UMBRAL));

        addBaseRule(EssenceType.ASTRAL, List.of(EssenceType.AIR, EssenceType.UMBRAL, EssenceType.NULL_R, EssenceType.NULL_U), List.of(EssenceType.EARTH, EssenceType.NATURE));
        addRule("astral_lightning_dom", List.of(EssenceType.EARTH), List.of(EssenceType.EARTH, EssenceType.NATURE));

        addBaseRule(EssenceType.OASIS, List.of(EssenceType.WATER, EssenceType.EARTH, EssenceType.DUST), List.of(EssenceType.NETHER, EssenceType.UMBRAL));
        addRule("oasis_arid_dom", List.of(EssenceType.WATER, EssenceType.VOID), List.of(EssenceType.NETHER, EssenceType.UMBRAL));

        addBaseRule(EssenceType.MIRAGE, List.of(EssenceType.WATER, EssenceType.VOID, EssenceType.BLIGHT), List.of(EssenceType.UMBRAL, EssenceType.NATURE));
        addRule("mirage_radiant_dom", List.of(EssenceType.UNDEAD), List.of(EssenceType.UMBRAL, EssenceType.NATURE));

        addBaseRule(EssenceType.AURA, List.of(EssenceType.UNDEAD, EssenceType.WRAITH), List.of(EssenceType.UMBRAL, EssenceType.VOID, EssenceType.NETHER));
        addRule("aura_air_dom", List.of(EssenceType.EARTH), List.of(EssenceType.UMBRAL, EssenceType.VOID, EssenceType.NETHER));

        addBaseRule(EssenceType.AMBER, List.of(EssenceType.VOID, EssenceType.BARROW), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL));
        addRule("amber_vitae_dom", List.of(EssenceType.UNDEAD), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL));

        addBaseRule(EssenceType.WRAITH, List.of(EssenceType.AIR, EssenceType.UMBRAL, EssenceType.AURA), List.of(EssenceType.RADIANT, EssenceType.EARTH));
        addRule("wraith_undead_dom", List.of(EssenceType.FROZEN), List.of(EssenceType.RADIANT, EssenceType.EARTH));

        addBaseRule(EssenceType.BARROW, List.of(EssenceType.FROZEN, EssenceType.AMBER), List.of(EssenceType.RADIANT, EssenceType.AIR, EssenceType.NATURE));
        addRule("barrow_earth_dom", List.of(EssenceType.VOID), List.of(EssenceType.RADIANT, EssenceType.AIR, EssenceType.NATURE));

        // ==========================================
        // 4. TIER 3 FUSIONS (Base & Submissive Ascension)
        // ==========================================
        addBaseRule(EssenceType.PYRE, List.of(EssenceType.VOID, EssenceType.FROZEN, EssenceType.NATURE, EssenceType.GLACIAL, EssenceType.RIME), List.of(EssenceType.AIR));
        addRule("pyre_nature_dom", List.of(EssenceType.WATER, EssenceType.EARTH, EssenceType.FROZEN, EssenceType.NATURE, EssenceType.GLACIAL, EssenceType.RIME), List.of(EssenceType.AIR));

        addBaseRule(EssenceType.PENUMBRA, List.of(EssenceType.AIR, EssenceType.RADIANT, EssenceType.NATURE, EssenceType.CELESTIAL, EssenceType.AEGIS), List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.UNDEAD));
        addRule("penumbra_radiant_dom", List.of(EssenceType.UNDEAD, EssenceType.AIR, EssenceType.NATURE, EssenceType.CELESTIAL, EssenceType.AEGIS), List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT)); // Glass cannon override applies

        addBaseRule(EssenceType.RIME, List.of(EssenceType.EARTH, EssenceType.NATURE, EssenceType.MAGMA, EssenceType.OVERGROWTH), List.of(EssenceType.VOID, EssenceType.UNDEAD));
        addRule("rime_water_dom", List.of(EssenceType.NETHER, EssenceType.ARID, EssenceType.EARTH, EssenceType.NATURE, EssenceType.MAGMA, EssenceType.OVERGROWTH), List.of(EssenceType.VOID, EssenceType.UNDEAD));

        addBaseRule(EssenceType.SPRING, List.of(EssenceType.UNDEAD, EssenceType.WATER, EssenceType.EARTH, EssenceType.SOULFIRE, EssenceType.BLIGHT), List.of(EssenceType.UMBRAL));
        addRule("spring_water_dom", List.of(EssenceType.NETHER, EssenceType.ARID, EssenceType.UNDEAD, EssenceType.EARTH, EssenceType.SOULFIRE, EssenceType.BLIGHT), List.of(EssenceType.UMBRAL, EssenceType.FROZEN));

        addBaseRule(EssenceType.STATIC, List.of(EssenceType.EARTH, EssenceType.WATER, EssenceType.VOID, EssenceType.GLACIAL, EssenceType.VAPOR), List.of(EssenceType.NATURE));
        addRule("static_air_dom", List.of(EssenceType.EARTH, EssenceType.WATER, EssenceType.VOID, EssenceType.GLACIAL, EssenceType.VAPOR), List.of(EssenceType.NATURE, EssenceType.VOID));

        addBaseRule(EssenceType.SYLVAN, List.of(EssenceType.WATER, EssenceType.EARTH, EssenceType.UNDEAD, EssenceType.BARROW, EssenceType.WRAITH), List.of(EssenceType.NETHER, EssenceType.AIR));
        addRule("sylvan_earth_dom", List.of(EssenceType.VOID, EssenceType.UNDEAD, EssenceType.BARROW, EssenceType.WRAITH), List.of(EssenceType.NETHER, EssenceType.AIR));

        addBaseRule(EssenceType.MIASMA, List.of(EssenceType.FROZEN, EssenceType.EARTH, EssenceType.OASIS, EssenceType.TAIGA), List.of(EssenceType.UMBRAL, EssenceType.RADIANT, EssenceType.NETHER));
        addRule("miasma_water_dom", List.of(EssenceType.NETHER, EssenceType.ARID, EssenceType.FROZEN, EssenceType.OASIS, EssenceType.TAIGA), List.of(EssenceType.UMBRAL, EssenceType.RADIANT, EssenceType.NATURE));

        addBaseRule(EssenceType.GENESIS, List.of(EssenceType.UNDEAD, EssenceType.OBLIVION, EssenceType.WRAITH), List.of(EssenceType.UMBRAL));
        addRule("genesis_water_dom", List.of(EssenceType.NETHER, EssenceType.ARID, EssenceType.OBLIVION, EssenceType.WRAITH), List.of(EssenceType.UMBRAL, EssenceType.NATURE));

        addBaseRule(EssenceType.OBLIVION, List.of(EssenceType.RADIANT, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.GENESIS, EssenceType.AURA), List.of(EssenceType.WATER, EssenceType.VOID));
        addRule("oblivion_nether_dom", List.of(EssenceType.FROZEN, EssenceType.NATURE, EssenceType.GENESIS, EssenceType.AURA), List.of(EssenceType.VOID, EssenceType.WATER));

        // ==========================================
        // 5. APEX FUSIONS (Tier 4)
        // ==========================================
        addBaseRule(EssenceType.AETHER, List.of(EssenceType.EARTH, EssenceType.NETHER, EssenceType.VOID, EssenceType.MAGMA, EssenceType.DUST, EssenceType.OVERGROWTH), List.of(EssenceType.VOID, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.ARID));
        addRule("aether_earth_dom", List.of(EssenceType.EARTH, EssenceType.NETHER, EssenceType.MAGMA, EssenceType.DUST, EssenceType.OVERGROWTH), List.of(EssenceType.NATURE, EssenceType.FROZEN, EssenceType.ARID)); // Neutral to Void cancellation
        addRule("aether_nether_rec", List.of(EssenceType.EARTH, EssenceType.NETHER, EssenceType.VOID, EssenceType.MAGMA, EssenceType.DUST, EssenceType.OVERGROWTH, EssenceType.FROZEN, EssenceType.NATURE), List.of(EssenceType.VOID, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.ARID, EssenceType.WATER));

        addBaseRule(EssenceType.ENTROPIC, List.of(EssenceType.AIR, EssenceType.RADIANT, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.CELESTIAL), List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT));
        addRule("entropic_undead_dom", List.of(EssenceType.AIR, EssenceType.RADIANT, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.CELESTIAL, EssenceType.AEGIS, EssenceType.AURORA), List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT));
        addRule("entropic_blood_rec", List.of(EssenceType.AIR, EssenceType.RADIANT, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.CELESTIAL), List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT, EssenceType.NETHER, EssenceType.UMBRAL));

        addBaseRule(EssenceType.CELESTIAL, List.of(EssenceType.UNDEAD, EssenceType.WATER, EssenceType.EARTH, EssenceType.OBLIVION), List.of(EssenceType.UMBRAL, EssenceType.VOID));
        addRule("celestial_vitae_dom", List.of(EssenceType.UNDEAD, EssenceType.WATER, EssenceType.EARTH, EssenceType.OBLIVION, EssenceType.WRAITH, EssenceType.BARROW), List.of(EssenceType.UMBRAL, EssenceType.VOID));
        addRule("celestial_air_rec", List.of(EssenceType.UNDEAD, EssenceType.WATER, EssenceType.EARTH, EssenceType.OBLIVION), List.of(EssenceType.UMBRAL, EssenceType.VOID));

        addBaseRule(EssenceType.PRISMATIC, List.of(EssenceType.UNDEAD, EssenceType.BLIGHT, EssenceType.SOULFIRE, EssenceType.BLOOD), List.of(EssenceType.UMBRAL));
        addRule("prismatic_chimera_rec", List.of(EssenceType.UNDEAD, EssenceType.BLIGHT, EssenceType.SOULFIRE, EssenceType.BLOOD), List.of(EssenceType.UMBRAL));

        // ==========================================
        // 6. ESCHATON & SINGULARITY (Tier 5 & 6)
        // ==========================================
        addBaseRule(EssenceType.EMPYREAN, List.of(EssenceType.UNDEAD, EssenceType.WATER, EssenceType.EARTH, EssenceType.NETHER, EssenceType.VOID, EssenceType.OBLIVION, EssenceType.WRAITH, EssenceType.BARROW, EssenceType.BLIGHT), List.of(EssenceType.UMBRAL));
        addBaseRule(EssenceType.CATACLYSM, List.of(EssenceType.AIR, EssenceType.RADIANT, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.WATER, EssenceType.CELESTIAL, EssenceType.AEGIS, EssenceType.AURORA, EssenceType.GENESIS), List.of(EssenceType.VITAE));
        addBaseRule(EssenceType.TERMINUS, List.of(EssenceType.EARTH, EssenceType.NATURE, EssenceType.NETHER, EssenceType.ARID, EssenceType.VOID, EssenceType.MAGMA, EssenceType.DUST, EssenceType.OVERGROWTH, EssenceType.PYRE), List.of(EssenceType.RADIANT));

        addBaseRule(EssenceType.ESCHATON, List.of(EssenceType.values()), List.of(EssenceType.APOTHEOSIS));
        addBaseRule(EssenceType.APOTHEOSIS, List.of(EssenceType.values()), List.of(EssenceType.ESCHATON));
        addBaseRule(EssenceType.ENTROPICA, List.of(EssenceType.values()), List.of()); // True Equilibrium
    }

    private void addBaseRule(EssenceType type, List<EssenceType> strong, List<EssenceType> weak) {
        addRule(type.getSerializedName(), strong, weak);
    }

    private void addRule(String profileId, List<EssenceType> strong, List<EssenceType> weak) {
        strongAgainstMap.computeIfAbsent(profileId, k -> EnumSet.noneOf(EssenceType.class)).addAll(strong);
        weakAgainstMap.computeIfAbsent(profileId, k -> EnumSet.noneOf(EssenceType.class)).addAll(weak);
    }
}