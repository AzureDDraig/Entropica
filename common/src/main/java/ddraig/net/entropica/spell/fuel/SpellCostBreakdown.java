package ddraig.net.entropica.spell.fuel;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.SpellPartSlotType;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Detailed per-part and per-Materia-type fuel cost breakdown for a {@link ddraig.net.entropica.spell.assembly.SpellAssembly}.
 *
 * <p>Every part of the spell ({@code Flow}, {@code Form}, {@code Primary Effect}, and optional
 * {@code Secondary Effect}) produces an individual {@link PartCostEntry} billed in that part's
 * assigned {@link EssenceType}, which are then summed by Materia type.</p>
 */
public record SpellCostBreakdown(
        int potencyTier,
        List<PartCostEntry> partEntries,
        Map<EssenceType, Long> totalMfumByType,
        Map<EssenceType, Long> upfrontMfumByType,
        Map<EssenceType, Long> dynamicTriggerMfumByType
) {
    public record PartCostEntry(
            int subSpellIndex,
            SpellPartSlotType partSlotType,
            String nodeId,
            EssenceType materiaType,
            boolean mutationEnabled,
            SpellMutationProfile resolvedMutation,
            long mfumCost,
            double nativeTierUnits
    ) {
    }

    public SpellCostBreakdown {
        partEntries = List.copyOf(partEntries);
        totalMfumByType = Collections.unmodifiableMap(new LinkedHashMap<>(totalMfumByType));
        upfrontMfumByType = Collections.unmodifiableMap(new LinkedHashMap<>(upfrontMfumByType));
        dynamicTriggerMfumByType = Collections.unmodifiableMap(new LinkedHashMap<>(dynamicTriggerMfumByType));
    }

    public long getTotalMfumAcrossAllTypes() {
        long sum = 0L;
        for (long v : this.totalMfumByType.values()) {
            sum += v;
        }
        return sum;
    }

    public double getNativeUnitsForType(EssenceType type) {
        long mfum = this.totalMfumByType.getOrDefault(type, 0L);
        return MateriaStageTable.toNativeUnits(mfum, this.potencyTier);
    }

    public String formatTypeCostSummary() {
        String unitCode = MateriaStageTable.getStageUnitCode(this.potencyTier);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<EssenceType, Long> entry : this.totalMfumByType.entrySet()) {
            if (sb.length() > 0) sb.append(" + ");
            double nativeVal = MateriaStageTable.toNativeUnits(entry.getValue(), this.potencyTier);
            String formattedNum = (Math.abs(nativeVal - Math.rint(nativeVal)) < 1e-4)
                    ? String.valueOf(Math.round(nativeVal))
                    : String.format("%.2f", nativeVal);
            sb.append(formattedNum).append(' ').append(unitCode).append(' ').append(entry.getKey().getDisplayName());
        }
        return sb.toString();
    }
}
