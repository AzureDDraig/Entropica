package ddraig.net.entropica.spell.mutation;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.SpellPartSlotType;

/**
 * Resolved mutation traits for a single modular Spell Part ({@code Flow}, {@code Form},
 * {@code Core Effect}, or {@code Secondary Effect}).
 *
 * <p>When a part's {@code mutationEnabled} toggle is {@code false} (or its {@code materiaType}
 * is {@link EssenceType#REGULAR}), {@code mutated} is strictly {@code false} and all mutation
 * behavior overrides revert to clean baseline defaults, while STILL preserving {@code materiaType}
 * for per-part fuel billing, visual color tinting, and elemental Materia Advantage!</p>
 */
public record SpellMutationProfile(
        SpellPartSlotType slotType,
        String nodeId,
        EssenceType materiaType,
        boolean mutated,
        String signatureTitle,
        boolean wallPhasing,
        float armorBypassFraction,
        int armorShredPoints,
        float rangeMultiplier,
        float radiusMultiplier,
        float velocityMultiplier,
        float potencyMultiplier,
        float lifestealFraction,
        int bonusPierceTargets,
        boolean silkTouchInherited,
        boolean autoSmeltInherited,
        boolean persistentHex,
        boolean spawnLureHex,
        boolean monsterEmpowermentHex
) {

    public boolean isUnmutated() {
        return !this.mutated;
    }

    public String mutationTitle() {
        return this.signatureTitle;
    }

    public String mutationName() {
        return this.signatureTitle;
    }

    public static SpellMutationProfile unmutated(SpellPartSlotType slotType, String nodeId, EssenceType materiaType) {
        EssenceType safeType = materiaType == null ? EssenceType.REGULAR : materiaType;
        String label = safeType == EssenceType.REGULAR
                ? "Unattuned " + formatNodeId(nodeId)
                : safeType.getDisplayName() + "-Tinted " + formatNodeId(nodeId) + " (Unmutated)";
        return new SpellMutationProfile(
                slotType,
                nodeId,
                safeType,
                false,
                label,
                false,
                0.0F,
                0,
                1.0F,
                1.0F,
                1.0F,
                1.0F,
                0.0F,
                0,
                false,
                false,
                false,
                false,
                false
        );
    }

    private static String formatNodeId(String id) {
        if (id == null || id.isEmpty()) return "Part";
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }
}
