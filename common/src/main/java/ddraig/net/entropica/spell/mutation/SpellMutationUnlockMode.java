package ddraig.net.entropica.spell.mutation;

/**
 * Server configuration modes governing how players unlock the 57 Materia Elemental Mutations
 * in the Arcane Scribing Table and Research Bench (Section 8.6.3).
 */
public enum SpellMutationUnlockMode {
    /**
     * Mode 1: Casting a specific (Element + Node) combination marks that Signature Mutation
     * as Observed so it can be researched individually at the Research Bench.
     */
    PER_COMBO(1, "PER_COMBO"),

    /**
     * Mode 2 (Default): Researching a Form, Flow, or Core Effect node at the Research Bench
     * automatically unlocks its Signature Mutations for all Materia types the player knows.
     */
    AUTO_BY_NODE(2, "AUTO_BY_NODE"),

    /**
     * Mode 3: Unlocking a Materia Family Mastery node at the Research Bench unlocks mutations
     * for all elements within that family tier.
     */
    BY_FAMILY_TIER(3, "BY_FAMILY_TIER");

    private final int configCode;
    private final String configName;

    SpellMutationUnlockMode(int configCode, String configName) {
        this.configCode = configCode;
        this.configName = configName;
    }

    public int getConfigCode() {
        return this.configCode;
    }

    public String getConfigName() {
        return this.configName;
    }

    public int getId() {
        return this.configCode;
    }

    public String getDisplayName() {
        return this.configName;
    }

    public static SpellMutationUnlockMode fromId(int code) {
        return fromConfigInt(code);
    }

    public static SpellMutationUnlockMode fromConfigInt(int code) {
        return switch (code) {
            case 1 -> PER_COMBO;
            case 3 -> BY_FAMILY_TIER;
            default -> AUTO_BY_NODE;
        };
    }
}
