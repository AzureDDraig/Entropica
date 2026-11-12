package ddraig.net.entropica.spell.fuel;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.SpellPartSlotType;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes exact per-part and per-Materia-type fuel costs for casting a {@link SpellAssembly}
 * or binding it at a Magic Circle (10x cast cost).
 */
public final class SpellFuelCalculator {

    /**
     * Materia-Specific locked spells receive a 15% fuel efficiency discount (Section 1.0).
     */
    public static final double MATERIA_SPECIFIC_DISCOUNT_MULTIPLIER = 0.85D;

    private SpellFuelCalculator() {
    }

    public static SpellCostBreakdown calculateCastCost(
            SpellAssembly assembly,
            @Nullable ApparatusType apparatusType,
            float apparatusMaterialEfficiencyBonus,
            @Nullable EssenceType fallbackUniversalCellType
    ) {
        return calculateInternal(assembly, apparatusType, apparatusMaterialEfficiencyBonus, fallbackUniversalCellType, 1L);
    }

    public static SpellCostBreakdown calculateMagicCircleRitualCost(SpellAssembly blueprint, int targetPotencyTier) {
        SpellAssembly tieredView = blueprint.bindToPotencyTier(targetPotencyTier, blueprint.flawProfile());
        return calculateInternal(tieredView, null, 0.0F, EssenceType.REGULAR, 10L);
    }

    private static SpellCostBreakdown calculateInternal(
            SpellAssembly assembly,
            @Nullable ApparatusType apparatusType,
            float materialBonus,
            @Nullable EssenceType fallbackCellType,
            long ritualMultiplier
    ) {
        int tier = assembly.getEffectiveTier();
        long baseSubSpellMfum = MateriaStageTable.getBaseMfumCastCost(tier) * ritualMultiplier;

        double globalDiscount = assembly.materiaSpecific() ? MATERIA_SPECIFIC_DISCOUNT_MULTIPLIER : 1.0D;
        if (materialBonus > 0.0F) {
            globalDiscount *= Math.max(0.50D, 1.0D - materialBonus);
        }

        List<SubSpellSlot> slots = assembly.subSpells();
        int slotCount = slots.size();

        List<SpellCostBreakdown.PartCostEntry> entries = new ArrayList<>();
        Map<EssenceType, Long> totalByType = new LinkedHashMap<>();

        // 1. Overarching Flow Part: scales linearly by slotCount * 20% of baseSubSpellMfum
        FlowPartBinding flowPart = assembly.flowPart();
        EssenceType flowType = flowPart.resolveEffectiveMateria(assembly.materiaSpecific(), fallbackCellType);
        SpellMutationProfile flowMut = flowPart.resolveMutation(assembly.materiaSpecific(), fallbackCellType);
        double flowApparatusMult = apparatusType != null ? apparatusType.getBaseFuelMultiplier() : 1.0D;
        long flowMfum = Math.max(1L, Math.round(
                baseSubSpellMfum * slotCount * SpellPartSlotType.FLOW.getBaseCostWeight() * globalDiscount * flowApparatusMult
        ));
        entries.add(new SpellCostBreakdown.PartCostEntry(
                -1,
                SpellPartSlotType.FLOW,
                flowPart.flow().getSerializedName(),
                flowType,
                flowPart.mutationEnabled(),
                flowMut,
                flowMfum,
                MateriaStageTable.toNativeUnits(flowMfum, tier)
        ));
        totalByType.merge(flowType, flowMfum, Long::sum);

        // 2. Each Sub-Spell's Form (40%), Primary Core Effect (40%), and optional Dual-Verb Secondary Effect (+100%)
        for (int i = 0; i < slotCount; i++) {
            SubSpellSlot slot = slots.get(i);
            boolean channeled = slot.formPart().form().isChanneled();
            double appMult = 1.0D;
            if (apparatusType != null) {
                appMult = channeled ? apparatusType.getChanneledFuelMultiplier() : apparatusType.getBaseFuelMultiplier();
            }

            // 2a. Form Part
            FormPartBinding formBinding = slot.formPart();
            EssenceType formType = formBinding.resolveEffectiveMateria(assembly.materiaSpecific(), fallbackCellType);
            SpellMutationProfile formMut = formBinding.resolveMutation(assembly.materiaSpecific(), fallbackCellType);
            long formMfum = Math.max(1L, Math.round(
                    baseSubSpellMfum * SpellPartSlotType.FORM.getBaseCostWeight() * globalDiscount * appMult
            ));
            entries.add(new SpellCostBreakdown.PartCostEntry(
                    i,
                    SpellPartSlotType.FORM,
                    formBinding.form().getSerializedName(),
                    formType,
                    formBinding.mutationEnabled(),
                    formMut,
                    formMfum,
                    MateriaStageTable.toNativeUnits(formMfum, tier)
            ));
            totalByType.merge(formType, formMfum, Long::sum);

            // 2b. Primary Core Effect Part
            EffectPartBinding primary = slot.primaryEffect();
            EssenceType primType = primary.resolveEffectiveMateria(assembly.materiaSpecific(), fallbackCellType);
            SpellMutationProfile primMut = primary.resolveMutation(SpellPartSlotType.CORE_EFFECT, assembly.materiaSpecific(), fallbackCellType);
            long primMfum = Math.max(1L, Math.round(
                    baseSubSpellMfum * SpellPartSlotType.CORE_EFFECT.getBaseCostWeight() * globalDiscount * appMult
            ));
            entries.add(new SpellCostBreakdown.PartCostEntry(
                    i,
                    SpellPartSlotType.CORE_EFFECT,
                    primary.effect().getSerializedName(),
                    primType,
                    primary.mutationEnabled(),
                    primMut,
                    primMfum,
                    MateriaStageTable.toNativeUnits(primMfum, tier)
            ));
            totalByType.merge(primType, primMfum, Long::sum);

            // 2c. Optional Secondary Dual-Verb Core Effect Part (+100% sub-spell cost in its own Materia type)
            if (slot.secondaryEffect().isPresent()) {
                EffectPartBinding sec = slot.secondaryEffect().get();
                EssenceType secType = sec.resolveEffectiveMateria(assembly.materiaSpecific(), fallbackCellType);
                SpellMutationProfile secMut = sec.resolveMutation(SpellPartSlotType.SECONDARY_EFFECT, assembly.materiaSpecific(), fallbackCellType);
                long secMfum = Math.max(1L, Math.round(
                        baseSubSpellMfum * SpellPartSlotType.SECONDARY_EFFECT.getBaseCostWeight() * globalDiscount * appMult
                ));
                entries.add(new SpellCostBreakdown.PartCostEntry(
                        i,
                        SpellPartSlotType.SECONDARY_EFFECT,
                        sec.effect().getSerializedName(),
                        secType,
                        sec.mutationEnabled(),
                        secMut,
                        secMfum,
                        MateriaStageTable.toNativeUnits(secMfum, tier)
                ));
                totalByType.merge(secType, secMfum, Long::sum);
            }
        }

        // 3. Upfront (25%) vs Dynamic Trigger (75%) split when Flow uses dynamic trigger billing
        boolean dynamicBilling = ritualMultiplier == 1L && flowPart.flow().usesDynamicTriggerBilling();
        Map<EssenceType, Long> upfrontByType = new LinkedHashMap<>();
        Map<EssenceType, Long> triggerByType = new LinkedHashMap<>();

        for (Map.Entry<EssenceType, Long> e : totalByType.entrySet()) {
            long total = e.getValue();
            if (dynamicBilling) {
                long upfront = Math.max(1L, Math.round(total * 0.25D));
                long trigger = Math.max(0L, total - upfront);
                upfrontByType.put(e.getKey(), upfront);
                triggerByType.put(e.getKey(), trigger);
            } else {
                upfrontByType.put(e.getKey(), total);
                triggerByType.put(e.getKey(), 0L);
            }
        }

        return new SpellCostBreakdown(tier, entries, totalByType, upfrontByType, triggerByType);
    }
}
