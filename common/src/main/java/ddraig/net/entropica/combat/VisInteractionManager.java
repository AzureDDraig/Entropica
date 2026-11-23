package ddraig.net.entropica.combat;

import ddraig.net.entropica.api.EssenceType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Central Materia Advantage, Full Defender Parent-Tree Lineage Inheritance,
 * Offensive vs. Defensive Clash Tally, and Universal Damage Multiplier engine.
 */
public class VisInteractionManager {

    private static final VisInteractionManager INSTANCE = new VisInteractionManager();

    /** Tier 5 Divine Vulnerability catastrophic multiplier (bypasses the 10% player Resistance Floor). */
    public static final float TIER_5_DIVINE_OVERRIDE_MULTIPLIER = 4.0f;

    /** Tier 6 Singularity Mirror Clash (Eschaton <-> Apotheosis) and Entropica -> T6 terminal multiplier. */
    public static final float TIER_6_TERMINAL_CLASH_MULTIPLIER = 5.0f;

    /** Standard Mob-to-Player Survival Cap (lowered to 2.5f under Omega Leaning). */
    public static final float MOB_TO_PLAYER_SURVIVAL_CAP = 3.2f;
    public static final float MOB_TO_PLAYER_OMEGA_SURVIVAL_CAP = 2.5f;

    /** Standard Mob-to-Player Resistance Floor (bypassed to 0.0f under Alpha Leaning). */
    public static final float MOB_TO_PLAYER_RESISTANCE_FLOOR = 0.10f;

    // Mapped via String Profile ID to allow for altered Dominance states without bloating the EssenceType Enum
    private final Map<String, Set<EssenceType>> strongAgainstMap = new HashMap<>();
    private final Map<String, Set<EssenceType>> weakAgainstMap = new HashMap<>();
    private final Map<String, EssenceType> serializedLookup = new HashMap<>();
    private final Map<EssenceType, List<EssenceType>> directParentsMap = new EnumMap<>(EssenceType.class);
    private final Map<EssenceType, Set<EssenceType>> expandedLineageCache = new EnumMap<>(EssenceType.class);

    private VisInteractionManager() {
        for (EssenceType et : EssenceType.values()) {
            serializedLookup.put(et.getSerializedName(), et);
        }
        setupParentLineages();
        setupHardcodedDefaults();
    }

    public static VisInteractionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the direct canonical parent Materia types of a fusion {@link EssenceType}
     * (empty list for {@code null}, {@code REGULAR}, and primordial base roots).
     */
    public List<EssenceType> getDirectParents(@Nullable EssenceType type) {
        if (type == null || type == EssenceType.REGULAR) {
            return Collections.emptyList();
        }
        return directParentsMap.getOrDefault(type, Collections.emptyList());
    }

    /**
     * Expands a defending {@link EssenceType} into an unmodifiable ordered set containing {@code type}
     * followed by all unique ancestors in its full transitive parent lineage tree.
     */
    public Set<EssenceType> getExpandedLineage(@Nullable EssenceType type) {
        if (type == null || type == EssenceType.REGULAR) {
            return Collections.emptySet();
        }
        Set<EssenceType> cached = expandedLineageCache.get(type);
        return cached != null ? cached : Collections.singleton(type);
    }

    /**
     * Evaluates single-node Offensive vs. Defensive Materia Advantage between an attacking profile and a single
     * defender lineage node {@code defenderNode}:
     * <ul>
     *     <li>{@code +1} if Attacker is Strong Against {@code defenderNode} (or {@code defenderNode} is Weak Against Attacker).</li>
     *     <li>{@code -1} if {@code defenderNode} is Strong Against Attacker (or Attacker is Weak Against {@code defenderNode})
     *         <b>ONLY IF</b> Attacker is NOT also Strong Against {@code defenderNode}, so mutual volatile clashes deal
     *         {@code +1} both ways instead of cancelling to {@code 0}.</li>
     * </ul>
     */
    public int getSingleNodeAdvantage(String attackerProfileId, @Nullable EssenceType attackerType, @Nullable EssenceType defenderNode) {
        if (defenderNode == null || defenderNode == EssenceType.REGULAR
                || attackerProfileId == null || attackerProfileId.isEmpty()
                || "regular".equals(attackerProfileId)) {
            return 0;
        }
        boolean atkStrongVsNode = strongAgainstMap.getOrDefault(attackerProfileId, Collections.emptySet()).contains(defenderNode);
        boolean nodeWeakVsAtk = attackerType != null
                && weakAgainstMap.getOrDefault(defenderNode.getSerializedName(), Collections.emptySet()).contains(attackerType);
        boolean attackerBeatsNode = atkStrongVsNode || nodeWeakVsAtk;
        if (attackerBeatsNode) {
            return 1;
        }

        boolean atkWeakVsNode = weakAgainstMap.getOrDefault(attackerProfileId, Collections.emptySet()).contains(defenderNode);
        boolean isBaseProfile = attackerType != null && attackerType.getSerializedName().equals(attackerProfileId);
        boolean nodeStrongVsAtk = isBaseProfile
                && strongAgainstMap.getOrDefault(defenderNode.getSerializedName(), Collections.emptySet()).contains(attackerType);
        if (atkWeakVsNode || nodeStrongVsAtk) {
            return -1;
        }
        return 0;
    }

    public int getSingleNodeAdvantage(@Nullable EssenceType attacker, @Nullable EssenceType defenderNode) {
        if (attacker == null || attacker == EssenceType.REGULAR) {
            return 0;
        }
        return getSingleNodeAdvantage(attacker.getSerializedName(), attacker, defenderNode);
    }

    /**
     * Compares an attacking profile against a single defending element node using Offensive vs. Defensive
     * clash resolution ({@code +1} Strong / Mutual Clash, {@code -1} Resisted, {@code 0} Neutral).
     */
    public int getAdvantagePoints(String attackerProfileId, EssenceType defender) {
        EssenceType atkType = resolveAttackerEssenceType(attackerProfileId);
        return getSingleNodeAdvantage(attackerProfileId, atkType, defender);
    }

    /**
     * Returns {@code true} when Tier 6 Singularity Absolute Immunity forces incoming damage to {@code 0.0x}:
     * <ul>
     *     <li>Defender has {@code ENTROPICA} (True Absolute Immunity against ALL attackers, including Eschaton &amp; Apotheosis).</li>
     *     <li>Defender has {@code ESCHATON} or {@code APOTHEOSIS} and Attacker is any Tier 1–5 / Regular element.</li>
     * </ul>
     */
    public boolean isTier6AbsoluteImmunity(@Nullable EssenceType attacker, @Nullable Collection<EssenceType> defenders) {
        if (defenders == null || defenders.isEmpty()) {
            return false;
        }
        if (defenders.contains(EssenceType.ENTROPICA)) {
            return true;
        }
        boolean hasLesserSingularity = defenders.contains(EssenceType.ESCHATON) || defenders.contains(EssenceType.APOTHEOSIS);
        if (!hasLesserSingularity) {
            return false;
        }
        return !isTier6Singularity(attacker);
    }

    /**
     * Returns {@code true} when a Tier 5 Eschaton defender is struck by its single forced Divine Vulnerability
     * ({@code UMBRAL -> EMPYREAN}, {@code VITAE -> CATACLYSM}, {@code UNDEAD -> TERMINUS}).
     */
    public boolean hasDivineTier5Override(@Nullable EssenceType attacker, @Nullable Collection<EssenceType> defenders) {
        if (attacker == null || defenders == null || defenders.isEmpty()) {
            return false;
        }
        for (EssenceType def : defenders) {
            if (isTier5DivineVulnerability(attacker, def)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the full Materia Advantage Score by expanding each defending element into its complete
     * parent-tree lineage (while honoring Tier 5 Forced Single Weakness dilution and Tier 6 Singularity overrides).
     */
    public int calculateLineageAdvantageScore(@Nullable EssenceType attacker, @Nullable Collection<EssenceType> defenders) {
        if (attacker == null || attacker == EssenceType.REGULAR || defenders == null || defenders.isEmpty()) {
            return 0;
        }
        return calculateSingleProfileLineageScore(attacker.getSerializedName(), attacker, defenders);
    }

    /**
     * Multi-profile variant of {@link #calculateLineageAdvantageScore(EssenceType, Collection)} for weapons or
     * assemblies carrying multiple attacking Materia profiles (including altered Dominance profile IDs).
     */
    public int calculateLineageAdvantageScore(@Nullable Collection<String> attackerProfiles, @Nullable Collection<EssenceType> defenders) {
        if (attackerProfiles == null || attackerProfiles.isEmpty() || defenders == null || defenders.isEmpty()) {
            return 0;
        }
        int totalScore = 0;
        for (String atkProfile : attackerProfiles) {
            if (atkProfile == null || atkProfile.isEmpty() || "regular".equals(atkProfile)) {
                continue;
            }
            EssenceType atkType = resolveAttackerEssenceType(atkProfile);
            totalScore += calculateSingleProfileLineageScore(atkProfile, atkType, defenders);
        }
        return totalScore;
    }

    /**
     * Legacy multi-profile helper delegating to full Defender Lineage &amp; Offensive/Defensive clash scoring.
     */
    public int calculateTotalVisScore(Collection<String> attackerProfiles, Collection<EssenceType> defenders) {
        return calculateLineageAdvantageScore(attackerProfiles, defenders);
    }

    public float calculateDamageMultiplier(@Nullable EssenceType attacker, @Nullable Collection<EssenceType> defenders) {
        return calculateDamageMultiplier(attacker, defenders, false, 0.0f, false, false);
    }

    public float calculateDamageMultiplier(
            @Nullable EssenceType attacker,
            @Nullable Collection<EssenceType> defenders,
            boolean mobToPlayer,
            float bonusScore
    ) {
        return calculateDamageMultiplier(attacker, defenders, mobToPlayer, bonusScore, false, false);
    }

    /**
     * Computes the final Materia Advantage damage multiplier implementing Section 4's Universal Linear Scale,
     * Player Vulnerability bounds, Tier 5 Divine Override ({@code 4.0x}), and Tier 6 Absolute Override
     * ({@code 0.0x} True Immunity / {@code 5.0x} Mirror Clash &amp; Entropica Terminal Annihilation).
     */
    public float calculateDamageMultiplier(
            @Nullable EssenceType attacker,
            @Nullable Collection<EssenceType> defenders,
            boolean mobToPlayer,
            float bonusScore,
            boolean alphaLeaningDefender,
            boolean omegaLeaningDefender
    ) {
        boolean hasDefenders = false;
        boolean defEntropica = false;
        boolean defEschaton = false;
        boolean defApotheosis = false;

        if (defenders != null) {
            for (EssenceType d : defenders) {
                if (d == null || d == EssenceType.REGULAR) continue;
                hasDefenders = true;
                if (d == EssenceType.ENTROPICA) defEntropica = true;
                else if (d == EssenceType.ESCHATON) defEschaton = true;
                else if (d == EssenceType.APOTHEOSIS) defApotheosis = true;
            }
        }

        // 1. Tier 6 Absolute Override (A2): bypasses standard math and Resistance/Survival bounds
        if (defEntropica) {
            return 0.0f; // True Absolute Immunity even against Eschaton & Apotheosis
        }
        if (defEschaton || defApotheosis) {
            if (attacker == EssenceType.ENTROPICA) {
                return TIER_6_TERMINAL_CLASH_MULTIPLIER; // 5.0x terminal annihilation
            }
            if ((attacker == EssenceType.ESCHATON && defApotheosis)
                    || (attacker == EssenceType.APOTHEOSIS && defEschaton)) {
                return TIER_6_TERMINAL_CLASH_MULTIPLIER; // 5.0x Mirror Clash
            }
            if (attacker == EssenceType.ESCHATON || attacker == EssenceType.APOTHEOSIS) {
                return 1.0f; // Same-Singularity neutral clash
            }
            return 0.0f; // Tier 1-5 & Regular deal 0.0x to Tier 6 Singularities
        }

        // 2. Tier 5 Divine Override (A2): forced single catastrophic vulnerability = 4.0x directly
        if (hasDefenders && attacker != null && hasDivineTier5Override(attacker, defenders)) {
            return TIER_5_DIVINE_OVERRIDE_MULTIPLIER;
        }

        // 3. Standard Defender Parent-Tree Lineage Walk + Universal Linear Scale
        int lineageScore = (hasDefenders && attacker != null && attacker != EssenceType.REGULAR)
                ? calculateLineageAdvantageScore(attacker, defenders)
                : 0;
        float totalScore = lineageScore + bonusScore;

        float mult = totalScore >= 0.0f
                ? (1.0f + 0.5f * totalScore)
                : (float) Math.pow(0.5d, -totalScore);

        if (mobToPlayer) {
            float upperCap = omegaLeaningDefender ? MOB_TO_PLAYER_OMEGA_SURVIVAL_CAP : MOB_TO_PLAYER_SURVIVAL_CAP;
            float lowerFloor = alphaLeaningDefender ? 0.0f : MOB_TO_PLAYER_RESISTANCE_FLOOR;
            mult = Math.clamp(mult, lowerFloor, upperCap);
        }
        return mult;
    }

    private int calculateSingleProfileLineageScore(
            String attackerProfileId,
            @Nullable EssenceType attackerType,
            Collection<EssenceType> defenders
    ) {
        // Tier 6 Defender Override handling for score queries
        if (defenders.contains(EssenceType.ENTROPICA)) {
            return attackerType == EssenceType.ENTROPICA ? 0 : -1;
        }
        boolean defEschaton = defenders.contains(EssenceType.ESCHATON);
        boolean defApotheosis = defenders.contains(EssenceType.APOTHEOSIS);
        if (defEschaton || defApotheosis) {
            if (attackerType == EssenceType.ENTROPICA
                    || (attackerType == EssenceType.ESCHATON && defApotheosis)
                    || (attackerType == EssenceType.APOTHEOSIS && defEschaton)) {
                return 8; // 1.0 + 0.5 * 8 = 5.0x terminal clash
            }
            if (attackerType == EssenceType.ESCHATON || attackerType == EssenceType.APOTHEOSIS) {
                return 0;
            }
            return -1;
        }

        int score = 0;
        for (EssenceType def : defenders) {
            if (def == null || def == EssenceType.REGULAR) continue;

            // Tier 5 Eschaton Defenders: 75% Forced Single Weakness dilution
            // Only the single Divine Vulnerability grants catastrophic +6 (4.0x); diluted sub-parents do NOT grant +1.
            if (isTier5Eschaton(def)) {
                if (isTier5DivineVulnerability(attackerType, def)) {
                    score += 6; // 1.0 + 0.5 * 6 = 4.0x
                } else if (strongAgainstMap.getOrDefault(attackerProfileId, Collections.emptySet()).contains(def)) {
                    score += 1; // Tier 6 Singularities striking Tier 5
                } else if (attackerType != null
                        && strongAgainstMap.getOrDefault(def.getSerializedName(), Collections.emptySet()).contains(attackerType)) {
                    score -= 1; // Tier 5 defender Strong Against attacker
                }
                continue;
            }

            // Standard Defender Lineage Walk (A1 & A3): expand defender into D + all unique ancestors
            for (EssenceType node : getExpandedLineage(def)) {
                score += getSingleNodeAdvantage(attackerProfileId, attackerType, node);
            }
        }
        return score;
    }

    public static boolean isTier6Singularity(@Nullable EssenceType type) {
        return type == EssenceType.ESCHATON || type == EssenceType.APOTHEOSIS || type == EssenceType.ENTROPICA;
    }

    public static boolean isTier5Eschaton(@Nullable EssenceType type) {
        return type == EssenceType.EMPYREAN || type == EssenceType.CATACLYSM || type == EssenceType.TERMINUS;
    }

    public static boolean isTier5DivineVulnerability(@Nullable EssenceType attacker, @Nullable EssenceType defender) {
        return (defender == EssenceType.EMPYREAN && attacker == EssenceType.UMBRAL)
                || (defender == EssenceType.CATACLYSM && attacker == EssenceType.VITAE)
                || (defender == EssenceType.TERMINUS && attacker == EssenceType.UNDEAD);
    }

    @Nullable
    private EssenceType resolveAttackerEssenceType(@Nullable String attackerProfileId) {
        if (attackerProfileId == null || attackerProfileId.isEmpty()) {
            return null;
        }
        EssenceType direct = serializedLookup.get(attackerProfileId);
        if (direct != null) {
            return direct;
        }
        if (attackerProfileId.startsWith("null_r_")) return EssenceType.NULL_R;
        if (attackerProfileId.startsWith("null_u_")) return EssenceType.NULL_U;
        if (attackerProfileId.startsWith("radiant_fire_")) return EssenceType.RADIANT_FIRE;
        if (attackerProfileId.startsWith("umbral_water_")) return EssenceType.UMBRAL_WATER;
        int idx = attackerProfileId.indexOf('_');
        if (idx > 0) {
            return serializedLookup.get(attackerProfileId.substring(0, idx));
        }
        return null;
    }

    public Set<EssenceType> getStrongAgainst(EssenceType attacker) {
        if (attacker == null || attacker == EssenceType.REGULAR) return Collections.emptySet();
        Set<EssenceType> set = strongAgainstMap.get(attacker.getSerializedName());
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public Set<EssenceType> getWeakAgainst(EssenceType attacker) {
        if (attacker == null || attacker == EssenceType.REGULAR) return Collections.emptySet();
        Set<EssenceType> set = weakAgainstMap.get(attacker.getSerializedName());
        return set == null ? Collections.unmodifiableSet(Collections.emptySet()) : Collections.unmodifiableSet(set);
    }

    /**
     * Formats a concise, player-readable Materia Advantage/Weakness summary for tooltips.
     */
    public String formatMatchupSummary(EssenceType attacker) {
        if (attacker == null || attacker == EssenceType.REGULAR) {
            return "Neutral (Adapts to Dynamic Fuel)";
        }
        if (attacker == EssenceType.CHIMERA) {
            return "Shifting Quad-Elemental • Strong vs Undead, Blight, Soulfire, Blood (+1) • Weak vs Umbral (-1)";
        }
        Set<EssenceType> strong = getStrongAgainst(attacker);
        Set<EssenceType> weak = getWeakAgainst(attacker);
        if (strong.isEmpty() && weak.isEmpty()) {
            return "Neutral Matchup";
        }
        String strongPart = strong.size() >= 20
                ? "All Materia (+1)"
                : (strong.isEmpty() ? "None" : (formatTypeList(strong, 4) + " (+1)"));
        String weakPart = weak.isEmpty()
                ? "None (0 Weaknesses)"
                : (formatTypeList(weak, 4) + " (-1)");
        return "Strong vs " + strongPart + " • Weak vs " + weakPart;
    }

    private static String formatTypeList(Set<EssenceType> types, int maxShown) {
        List<String> names = new ArrayList<>(Math.min(types.size(), maxShown + 1));
        int idx = 0;
        for (EssenceType t : types) {
            if (t == EssenceType.REGULAR) continue;
            if (idx < maxShown) {
                names.add(t.getDisplayName());
            }
            idx++;
        }
        if (idx > maxShown) {
            return String.join(", ", names) + " +" + (idx - maxShown) + " more";
        }
        return String.join(", ", names);
    }

    private void setupParentLineages() {
        directParentsMap.clear();
        expandedLineageCache.clear();

        // Primordial Roots (no parents)
        for (EssenceType root : List.of(
                EssenceType.AIR, EssenceType.WATER, EssenceType.NATURE, EssenceType.EARTH,
                EssenceType.FROZEN, EssenceType.NETHER, EssenceType.RADIANT, EssenceType.UMBRAL,
                EssenceType.UNDEAD, EssenceType.VOID)) {
            directParentsMap.put(root, List.of());
        }

        // Tier 1 & Tier 2 Elemental, Vital & Ethereal Fusions
        setParents(EssenceType.ARID, EssenceType.AIR, EssenceType.EARTH);
        setParents(EssenceType.LIGHTNING, EssenceType.AIR, EssenceType.ARID);
        setParents(EssenceType.MAGMA, EssenceType.EARTH, EssenceType.NETHER);
        setParents(EssenceType.STORM, EssenceType.AIR, EssenceType.WATER);
        setParents(EssenceType.GLACIAL, EssenceType.WATER, EssenceType.FROZEN);
        setParents(EssenceType.OVERGROWTH, EssenceType.NATURE, EssenceType.EARTH);
        setParents(EssenceType.DUST, EssenceType.ARID, EssenceType.AIR);
        setParents(EssenceType.SPORE, EssenceType.NATURE, EssenceType.AIR);
        setParents(EssenceType.TAIGA, EssenceType.NATURE, EssenceType.FROZEN);
        setParents(EssenceType.OASIS, EssenceType.NATURE, EssenceType.ARID);

        setParents(EssenceType.VITAE, EssenceType.NATURE, EssenceType.RADIANT);
        setParents(EssenceType.BLOOD, EssenceType.VITAE, EssenceType.UNDEAD, EssenceType.NETHER);
        setParents(EssenceType.AURA, EssenceType.VITAE, EssenceType.AIR);
        setParents(EssenceType.AMBER, EssenceType.VITAE, EssenceType.EARTH);

        setParents(EssenceType.ECLIPSE, EssenceType.RADIANT, EssenceType.UMBRAL);
        setParents(EssenceType.BLIGHT, EssenceType.UNDEAD, EssenceType.NATURE);
        setParents(EssenceType.ASTRAL, EssenceType.VOID, EssenceType.LIGHTNING);
        setParents(EssenceType.SOULFIRE, EssenceType.NETHER, EssenceType.UNDEAD);
        setParents(EssenceType.VAPOR, EssenceType.WATER, EssenceType.NETHER);
        setParents(EssenceType.NULL_R, EssenceType.RADIANT, EssenceType.VOID);
        setParents(EssenceType.NULL_U, EssenceType.VOID, EssenceType.UMBRAL);
        setParents(EssenceType.DAWN, EssenceType.RADIANT, EssenceType.AIR);
        setParents(EssenceType.AUREOLE, EssenceType.RADIANT, EssenceType.AIR);
        setParents(EssenceType.VESPER, EssenceType.UMBRAL, EssenceType.AIR);
        setParents(EssenceType.ABYSS, EssenceType.UMBRAL, EssenceType.WATER);
        setParents(EssenceType.AEGIS, EssenceType.RADIANT, EssenceType.EARTH);
        setParents(EssenceType.AURORA, EssenceType.RADIANT, EssenceType.FROZEN);
        setParents(EssenceType.MIRAGE, EssenceType.RADIANT, EssenceType.ARID);
        setParents(EssenceType.WRAITH, EssenceType.VOID, EssenceType.UNDEAD);
        setParents(EssenceType.BARROW, EssenceType.EARTH, EssenceType.UNDEAD);

        // Tier 3 Trinity Fusions
        setParents(EssenceType.PYRE, EssenceType.EARTH, EssenceType.NETHER, EssenceType.NATURE);
        setParents(EssenceType.PENUMBRA, EssenceType.RADIANT, EssenceType.VOID, EssenceType.UMBRAL);
        setParents(EssenceType.RIME, EssenceType.AIR, EssenceType.WATER, EssenceType.FROZEN);
        setParents(EssenceType.SPRING, EssenceType.VITAE, EssenceType.NATURE, EssenceType.WATER);
        setParents(EssenceType.STATIC, EssenceType.LIGHTNING, EssenceType.AIR, EssenceType.ARID);
        setParents(EssenceType.SYLVAN, EssenceType.NATURE, EssenceType.EARTH, EssenceType.RADIANT);
        setParents(EssenceType.MIASMA, EssenceType.NATURE, EssenceType.UNDEAD, EssenceType.WATER);
        setParents(EssenceType.GENESIS, EssenceType.VITAE, EssenceType.RADIANT, EssenceType.WATER);
        setParents(EssenceType.OBLIVION, EssenceType.NETHER, EssenceType.UMBRAL, EssenceType.UNDEAD);

        // Catalysts & Caprine Secrets
        setParents(EssenceType.CHIMERA, EssenceType.AIR, EssenceType.EARTH, EssenceType.WATER, EssenceType.NETHER);
        setParents(EssenceType.PRISMATIC, EssenceType.CHIMERA, EssenceType.RADIANT);
        setParents(EssenceType.RADIANT_FIRE, EssenceType.RADIANT, EssenceType.NETHER);
        setParents(EssenceType.UMBRAL_WATER, EssenceType.UMBRAL, EssenceType.WATER);
        setParents(EssenceType.EQUINOX, EssenceType.RADIANT_FIRE, EssenceType.UMBRAL_WATER);

        // Tier 4 Apex Fusions
        setParents(EssenceType.AETHER, EssenceType.AIR, EssenceType.EARTH, EssenceType.WATER, EssenceType.NETHER, EssenceType.CHIMERA);
        setParents(EssenceType.ENTROPIC, EssenceType.VOID, EssenceType.UMBRAL, EssenceType.UNDEAD, EssenceType.BLOOD, EssenceType.CHIMERA);
        setParents(EssenceType.CELESTIAL, EssenceType.RADIANT, EssenceType.VITAE, EssenceType.NATURE, EssenceType.AIR, EssenceType.CHIMERA);

        // Tier 5 Eschaton Fusions
        setParents(EssenceType.EMPYREAN, EssenceType.CELESTIAL, EssenceType.GENESIS, EssenceType.SYLVAN);
        setParents(EssenceType.CATACLYSM, EssenceType.ENTROPIC, EssenceType.OBLIVION, EssenceType.PYRE);
        setParents(EssenceType.TERMINUS, EssenceType.AETHER, EssenceType.RIME, EssenceType.MIASMA);

        // Tier 6 Singularity Fusions
        setParents(EssenceType.ESCHATON, EssenceType.EMPYREAN, EssenceType.CATACLYSM, EssenceType.TERMINUS, EssenceType.ENTROPIC);
        setParents(EssenceType.APOTHEOSIS, EssenceType.EMPYREAN, EssenceType.CATACLYSM, EssenceType.TERMINUS, EssenceType.CELESTIAL);
        setParents(EssenceType.ENTROPICA, EssenceType.APOTHEOSIS, EssenceType.ESCHATON, EssenceType.ENTROPIC);

        // Conceptual Fragments & Resonance Co-Fission Types
        setParents(EssenceType.ETHER, EssenceType.AIR);
        setParents(EssenceType.RESONANCE, EssenceType.AIR);
        setParents(EssenceType.GRAVITY, EssenceType.EARTH);
        setParents(EssenceType.DENSITY, EssenceType.EARTH);
        setParents(EssenceType.FLOW, EssenceType.WATER);
        setParents(EssenceType.COHESION, EssenceType.WATER);
        setParents(EssenceType.IGNIS, EssenceType.NETHER);
        setParents(EssenceType.FERVOR, EssenceType.NETHER);
        setParents(EssenceType.HEAT, EssenceType.NETHER);
        setParents(EssenceType.VACUUM, EssenceType.VOID);
        setParents(EssenceType.OBLIVIATE, EssenceType.VOID);
        setParents(EssenceType.AXIOM, EssenceType.VOID);
        setParents(EssenceType.GROWTH, EssenceType.NATURE);
        setParents(EssenceType.PRIMAL, EssenceType.NATURE);
        setParents(EssenceType.SILENCE, EssenceType.UMBRAL);
        setParents(EssenceType.SHADOW, EssenceType.UMBRAL);
        setParents(EssenceType.CONCEALMENT, EssenceType.UMBRAL);
        setParents(EssenceType.MEMORY, EssenceType.UNDEAD);
        setParents(EssenceType.ECTO, EssenceType.UNDEAD);
        setParents(EssenceType.STASIS, EssenceType.FROZEN);
        setParents(EssenceType.FROST, EssenceType.FROZEN);
        setParents(EssenceType.KINETIC, EssenceType.LIGHTNING);
        setParents(EssenceType.VOLT, EssenceType.LIGHTNING);
        setParents(EssenceType.DESOLATION, EssenceType.ARID);
        setParents(EssenceType.ECHO, EssenceType.RADIANT);
        setParents(EssenceType.PHOTON, EssenceType.RADIANT);
        setParents(EssenceType.CHRONOS, EssenceType.AIR, EssenceType.VOID);
        setParents(EssenceType.SINGULARITY, EssenceType.RADIANT, EssenceType.VOID);

        // Precompute transitive lineage sets (BFS preserving deterministic insertion order)
        for (EssenceType et : EssenceType.values()) {
            if (et == EssenceType.REGULAR) continue;
            Set<EssenceType> visited = new LinkedHashSet<>();
            ArrayDeque<EssenceType> queue = new ArrayDeque<>();
            visited.add(et);
            queue.add(et);
            while (!queue.isEmpty()) {
                EssenceType curr = queue.poll();
                for (EssenceType parent : directParentsMap.getOrDefault(curr, List.of())) {
                    if (parent != null && parent != EssenceType.REGULAR && visited.add(parent)) {
                        queue.add(parent);
                    }
                }
            }
            expandedLineageCache.put(et, Collections.unmodifiableSet(visited));
        }
    }

    private void setParents(EssenceType child, EssenceType... parents) {
        directParentsMap.put(child, List.of(parents));
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
        addRule("arid_air_dom", List.of(EssenceType.EARTH, EssenceType.WATER), List.of(EssenceType.WATER, EssenceType.VOID));

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

        // Section 6.30 Vapor: Strong vs Nether, Arid, Frozen, Lightning | Weak vs Earth, Lightning, Arid (volatile clash)
        addBaseRule(EssenceType.VAPOR, List.of(EssenceType.NETHER, EssenceType.ARID, EssenceType.FROZEN, EssenceType.LIGHTNING),
                List.of(EssenceType.EARTH, EssenceType.LIGHTNING, EssenceType.ARID));
        addRule("vapor_nether_dom", List.of(EssenceType.FROZEN, EssenceType.NATURE), List.of(EssenceType.WATER, EssenceType.EARTH));

        // Section 6.31 Null_R & Section 6.32 Null_U
        addBaseRule(EssenceType.NULL_R, List.of(EssenceType.AIR, EssenceType.UNDEAD, EssenceType.UMBRAL), List.of(EssenceType.EARTH, EssenceType.UMBRAL));
        addRule("null_r_radiant_dom", List.of(EssenceType.UNDEAD), List.of(EssenceType.UMBRAL, EssenceType.EARTH));

        addBaseRule(EssenceType.NULL_U, List.of(EssenceType.RADIANT, EssenceType.NATURE), List.of(EssenceType.EARTH, EssenceType.VOID));
        addRule("null_u_void_dom", List.of(EssenceType.AIR), List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT));

        addBaseRule(EssenceType.SPORE, List.of(EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL, EssenceType.VOID));
        addRule("spore_air_dom", List.of(EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.FROZEN, EssenceType.UMBRAL, EssenceType.VOID));

        addBaseRule(EssenceType.TAIGA, List.of(EssenceType.WATER, EssenceType.NATURE), List.of(EssenceType.NETHER, EssenceType.UMBRAL, EssenceType.UNDEAD));
        addRule("taiga_nature_dom", List.of(EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.NETHER, EssenceType.UMBRAL, EssenceType.UNDEAD));

        addBaseRule(EssenceType.DAWN, List.of(EssenceType.UNDEAD), List.of(EssenceType.UMBRAL, EssenceType.VOID));
        addRule("dawn_air_dom", List.of(EssenceType.EARTH), List.of(EssenceType.UMBRAL, EssenceType.VOID));

        // Ethereal Halo & Dusk Fusions (Aureole & Vesper)
        addBaseRule(EssenceType.AUREOLE, List.of(EssenceType.UNDEAD, EssenceType.EARTH, EssenceType.VOID), List.of(EssenceType.UMBRAL));
        addRule("aureole_air_dom", List.of(EssenceType.EARTH, EssenceType.VOID), List.of(EssenceType.UMBRAL, EssenceType.VOID));

        addBaseRule(EssenceType.VESPER, List.of(EssenceType.RADIANT, EssenceType.WATER, EssenceType.EARTH), List.of(EssenceType.VOID));
        addRule("vesper_air_dom", List.of(EssenceType.EARTH), List.of(EssenceType.VOID));

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
        // 4. TIER 3 FUSIONS, CATALYSTS & CAPRINE SECRETS
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

        // Section 6.49 Chimera Catalyst & Caprine Secret Typings
        addBaseRule(EssenceType.CHIMERA, List.of(EssenceType.UNDEAD, EssenceType.BLIGHT, EssenceType.SOULFIRE, EssenceType.BLOOD), List.of(EssenceType.UMBRAL));
        addBaseRule(EssenceType.RADIANT_FIRE, List.of(EssenceType.UNDEAD, EssenceType.FROZEN, EssenceType.NATURE), List.of(EssenceType.UMBRAL, EssenceType.WATER));
        addBaseRule(EssenceType.UMBRAL_WATER, List.of(EssenceType.RADIANT, EssenceType.NATURE, EssenceType.NETHER), List.of(EssenceType.VOID, EssenceType.FROZEN));
        addBaseRule(EssenceType.EQUINOX, List.of(EssenceType.UNDEAD, EssenceType.FROZEN, EssenceType.NATURE, EssenceType.NETHER, EssenceType.RADIANT), List.of(EssenceType.VOID, EssenceType.UMBRAL));

        // ==========================================
        // 5. APEX FUSIONS (Tier 4)
        // ==========================================
        addBaseRule(EssenceType.AETHER, List.of(EssenceType.EARTH, EssenceType.NETHER, EssenceType.VOID, EssenceType.MAGMA, EssenceType.DUST, EssenceType.OVERGROWTH), List.of(EssenceType.VOID, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.ARID));
        addRule("aether_earth_dom", List.of(EssenceType.EARTH, EssenceType.NETHER, EssenceType.MAGMA, EssenceType.DUST, EssenceType.OVERGROWTH), List.of(EssenceType.NATURE, EssenceType.FROZEN, EssenceType.ARID)); // Neutral to Void cancellation
        addRule("aether_nether_rec", List.of(EssenceType.EARTH, EssenceType.NETHER, EssenceType.VOID, EssenceType.MAGMA, EssenceType.DUST, EssenceType.OVERGROWTH, EssenceType.FROZEN, EssenceType.NATURE), List.of(EssenceType.VOID, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.ARID, EssenceType.WATER));

        // Section 6.51 Entropic: Strong vs Air, Radiant, Nature, Frozen, Celestial, Aegis, Aurora | Weak vs Earth, Void, Radiant, Nether, Umbral
        addBaseRule(EssenceType.ENTROPIC,
                List.of(EssenceType.AIR, EssenceType.RADIANT, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.CELESTIAL, EssenceType.AEGIS, EssenceType.AURORA),
                List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT, EssenceType.NETHER, EssenceType.UMBRAL));
        addRule("entropic_undead_dom",
                List.of(EssenceType.AIR, EssenceType.RADIANT, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.CELESTIAL, EssenceType.AEGIS, EssenceType.AURORA),
                List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT));
        addRule("entropic_blood_rec",
                List.of(EssenceType.AIR, EssenceType.RADIANT, EssenceType.NATURE, EssenceType.FROZEN, EssenceType.CELESTIAL, EssenceType.AEGIS, EssenceType.AURORA),
                List.of(EssenceType.EARTH, EssenceType.VOID, EssenceType.RADIANT, EssenceType.NETHER, EssenceType.UMBRAL));

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
        addBaseRule(EssenceType.TERMINUS, List.of(EssenceType.NETHER, EssenceType.ARID, EssenceType.EARTH, EssenceType.WATER, EssenceType.MAGMA, EssenceType.PYRE, EssenceType.BLIGHT, EssenceType.SOULFIRE, EssenceType.MIASMA), List.of(EssenceType.UNDEAD));

        List<EssenceType> lowerTierElements = new ArrayList<>();
        List<EssenceType> allNonRegularElements = new ArrayList<>();
        for (EssenceType et : EssenceType.values()) {
            if (et != EssenceType.REGULAR) {
                allNonRegularElements.add(et);
                if (et != EssenceType.ESCHATON && et != EssenceType.APOTHEOSIS && et != EssenceType.ENTROPICA) {
                    lowerTierElements.add(et);
                }
            }
        }
        addBaseRule(EssenceType.ESCHATON, lowerTierElements, List.of(EssenceType.APOTHEOSIS));
        addBaseRule(EssenceType.APOTHEOSIS, lowerTierElements, List.of(EssenceType.ESCHATON));
        addBaseRule(EssenceType.ENTROPICA, allNonRegularElements, List.of()); // True Equilibrium (+1 vs EVERYTHING incl. Eschaton & Apotheosis, Weak vs NONE)
    }

    private void addBaseRule(EssenceType type, List<EssenceType> strong, List<EssenceType> weak) {
        addRule(type.getSerializedName(), strong, weak);
    }

    private void addRule(String profileId, List<EssenceType> strong, List<EssenceType> weak) {
        strongAgainstMap.computeIfAbsent(profileId, k -> EnumSet.noneOf(EssenceType.class)).addAll(strong);
        weakAgainstMap.computeIfAbsent(profileId, k -> EnumSet.noneOf(EssenceType.class)).addAll(weak);
    }
}
