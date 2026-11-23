package ddraig.net.entropica.spell.test;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellPartSlotType;
import ddraig.net.entropica.spell.SpellPrefix;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellCostBreakdown;
import ddraig.net.entropica.spell.fuel.SpellFuelCalculator;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.SpellMutationRegistry;
import ddraig.net.entropica.spell.mutation.handler.SpellMutationHandlerRegistry;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Self-contained deterministic verification suite for Entropica's Spellcrafting Engine.
 * Callable both from {@code /entropica spell verify} in-game and via {@link #main(String[])}.
 */
public final class SpellEngineVerificationSuite {

    public record VerificationReport(int passed, int failed, List<String> lines) {
        public boolean allPassed() {
            return this.failed == 0;
        }
    }

    private SpellEngineVerificationSuite() {
    }

    public static VerificationReport runAllChecks() {
        List<String> lines = new ArrayList<>();
        int[] counts = new int[]{0, 0}; // [passed, failed]

        // 1. Per-Part Mutation ON/OFF Toggle Independence
        FlowPartBinding flowMutated = new FlowPartBinding(SpellFlowModifier.SINGLE, EssenceType.LIGHTNING, true, 1, 0.0F);
        FormPartBinding formUnmutated = FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.VOID, false);
        EffectPartBinding primMutated = EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.GLACIAL, true);
        EffectPartBinding secUnmutated = EffectPartBinding.of(SpellCoreEffect.AFFLICTION, EssenceType.NETHER, false);

        SpellMutationProfile flowProf = flowMutated.resolveMutation(true, null);
        SpellMutationProfile formProf = formUnmutated.resolveMutation(true, null);
        SpellMutationProfile primProf = primMutated.resolveMutation(SpellPartSlotType.CORE_EFFECT, true, null);
        SpellMutationProfile secProf = secUnmutated.resolveMutation(SpellPartSlotType.SECONDARY_EFFECT, true, null);

        check(counts, lines, "Per-Part Mutation Toggle: Mutated Flow & Primary active while Form & Secondary toggled OFF",
                !flowProf.isUnmutated()
                        && formProf.isUnmutated()
                        && !primProf.isUnmutated()
                        && secProf.isUnmutated()
                        && formUnmutated.materiaType() == EssenceType.VOID
                        && secUnmutated.materiaType() == EssenceType.NETHER);

        // 2. Multi-Materia Per-Part Fuel Split & 15% Materia-Specific Discount
        SubSpellSlot dualVerbSlot = SubSpellSlot.ofDualVerb(formUnmutated, primMutated, secUnmutated);
        SpellAssembly unspecificMulti = new SpellAssembly(
                "Fourfold Chimera",
                List.of("Lightning Flow, Void Form, Glacial Harm, Nether Hex"),
                false,
                SpellPrefix.COMBAT,
                2, // Tier 2 base = 100 Mfum
                flowMutated,
                List.of(dualVerbSlot),
                SpellFlawProfile.PERFECT,
                2
        );
        SpellCostBreakdown rawCost = SpellFuelCalculator.calculateCastCost(unspecificMulti, null, 0.0F, EssenceType.REGULAR);
        check(counts, lines, "Multi-Materia Unspecific T2 Cost Split (20 Lightning / 40 Void / 40 Glacial / 100 Nether Mfum)",
                rawCost.totalMfumByType().getOrDefault(EssenceType.LIGHTNING, -1L) == 20L
                        && rawCost.totalMfumByType().getOrDefault(EssenceType.VOID, -1L) == 40L
                        && rawCost.totalMfumByType().getOrDefault(EssenceType.GLACIAL, -1L) == 40L
                        && rawCost.totalMfumByType().getOrDefault(EssenceType.NETHER, -1L) == 100L
                        && rawCost.getTotalMfumAcrossAllTypes() == 200L);

        SpellAssembly specificMulti = new SpellAssembly(
                "Bound Fourfold Chimera",
                List.of(),
                true, // -15% discount
                SpellPrefix.COMBAT,
                2,
                flowMutated,
                List.of(dualVerbSlot),
                SpellFlawProfile.PERFECT,
                2
        );
        SpellCostBreakdown discountedCost = SpellFuelCalculator.calculateCastCost(specificMulti, null, 0.0F, null);
        check(counts, lines, "Multi-Materia Specific -15% Discount (17 Lightning / 34 Void / 34 Glacial / 85 Nether Mfum)",
                discountedCost.totalMfumByType().getOrDefault(EssenceType.LIGHTNING, -1L) == 17L
                        && discountedCost.totalMfumByType().getOrDefault(EssenceType.VOID, -1L) == 34L
                        && discountedCost.totalMfumByType().getOrDefault(EssenceType.GLACIAL, -1L) == 34L
                        && discountedCost.totalMfumByType().getOrDefault(EssenceType.NETHER, -1L) == 85L
                        && discountedCost.getTotalMfumAcrossAllTypes() == 170L);

        // Dynamic trigger 25% / 75% split + Void-Warp travel mutation heritage check
        SpellAssembly delayedSpell = new SpellAssembly(
                "Delayed Trap",
                List.of(),
                false,
                SpellPrefix.COMBAT,
                2,
                new FlowPartBinding(SpellFlowModifier.SINGLE, ddraig.net.entropica.spell.SpellTriggerType.ON_TIMER, EssenceType.LIGHTNING, true, 1, 1.5F),
                List.of(SubSpellSlot.of(formUnmutated, primMutated)),
                SpellFlawProfile.PERFECT,
                0
        );
        SpellCostBreakdown dynCost = SpellFuelCalculator.calculateCastCost(delayedSpell, null, 0.0F, EssenceType.REGULAR);
        check(counts, lines, "Dynamic Trigger Flow 25% Upfront / 75% Detonation Per-Materia Split",
                dynCost.upfrontMfumByType().getOrDefault(EssenceType.LIGHTNING, -1L) == 6L
                        && dynCost.dynamicTriggerMfumByType().getOrDefault(EssenceType.LIGHTNING, -1L) == 18L
                        && dynCost.upfrontMfumByType().getOrDefault(EssenceType.VOID, -1L) == 12L
                        && dynCost.dynamicTriggerMfumByType().getOrDefault(EssenceType.VOID, -1L) == 36L);

        FlowPartBinding unauthorizedWarp = new FlowPartBinding(SpellFlowModifier.VOID_WARP, ddraig.net.entropica.spell.SpellTriggerType.ON_ENTITY_HIT, EssenceType.AIR, true, 1, 0.0F);
        FlowPartBinding unmutatedVoidWarp = new FlowPartBinding(SpellFlowModifier.VOID_WARP, ddraig.net.entropica.spell.SpellTriggerType.ON_ENTITY_HIT, EssenceType.VOID, false, 1, 0.0F);
        FlowPartBinding authorizedAstralWarp = new FlowPartBinding(SpellFlowModifier.VOID_WARP, ddraig.net.entropica.spell.SpellTriggerType.ON_ENTITY_HIT, EssenceType.ASTRAL, true, 1, 0.0F);
        check(counts, lines, "Void-Warp Travel Mutation Gate (Requires Void or Void-Created Materia + Mutation ACTIVE)",
                !unauthorizedWarp.isVoidWarpAuthorized(true, EssenceType.REGULAR)
                        && !unmutatedVoidWarp.isVoidWarpAuthorized(true, EssenceType.REGULAR)
                        && authorizedAstralWarp.isVoidWarpAuthorized(true, EssenceType.REGULAR));

        // 3. 10-Tier Stage Table & Cross-Tier Markup
        check(counts, lines, "10-Tier Stage Table Ratios (T1=8, T2=1, T3=360, T6=69120, T10=34560000 Mfum)",
                MateriaStageTable.getMfumPerNativeUnit(1) == 8L
                        && MateriaStageTable.getMfumPerNativeUnit(2) == 1L
                        && MateriaStageTable.getMfumPerNativeUnit(3) == 360L
                        && MateriaStageTable.getMfumPerNativeUnit(6) == 69_120L
                        && MateriaStageTable.getMfumPerNativeUnit(10) == 34_560_000L);

        check(counts, lines, "Cross-Tier Conversion: 0% Down-Conversion Penalty & +25%/Tier Up-Conversion Markup",
                MateriaStageTable.adjustMfumForFuelTier(1000L, 3, 5) == 1000L
                        && MateriaStageTable.adjustMfumForFuelTier(1000L, 4, 2) == 1500L
                        && MateriaStageTable.adjustMfumForFuelTier(1000L, 5, 1) == 2000L);

        // 4. Under-Supplied Magic Circle Flaw Monte Carlo Distribution (37.5% / 37.5% / 25%)
        RandomSource seeded = RandomSource.create(42_2026L);
        int trials = 10_000;
        int unstableOnly = 0;
        int crippledOnly = 0;
        int bothFlaws = 0;
        for (int i = 0; i < trials; i++) {
            SpellFlawProfile f = SpellFlawProfile.rollUnderSuppliedFlaw(seeded, 3);
            boolean u = f.unstableCasting();
            boolean c = f.crippledParameter().isPresent();
            if (u && c) bothFlaws++;
            else if (u) unstableOnly++;
            else if (c) crippledOnly++;
        }
        double uPct = unstableOnly / (double) trials;
        double cPct = crippledOnly / (double) trials;
        double bPct = bothFlaws / (double) trials;
        check(counts, lines, String.format("Under-Supplied Flaw Distribution (Unstable=%.1f%%, Crippled=%.1f%%, Both=%.1f%%)",
                        uPct * 100.0, cPct * 100.0, bPct * 100.0),
                Math.abs(uPct - 0.375) < 0.025
                        && Math.abs(cPct - 0.375) < 0.025
                        && Math.abs(bPct - 0.250) < 0.025);

        // 5. Apparatus Capacities, Workstation vs Field Lockout & Cycling
        check(counts, lines, "Apparatus Capacities (Engraved=1, Focus=1, Wand=2, Staff=4, Grimoire=9)",
                ApparatusType.ENGRAVED_TOOL.getGemCapacity() == 1
                        && ApparatusType.ARCANUM_FOCUS.getGemCapacity() == 1
                        && ApparatusType.WAND.getGemCapacity() == 2
                        && ApparatusType.STAFF.getGemCapacity() == 4
                        && ApparatusType.GRIMOIRE.getGemCapacity() == 9);

        SocketedSpellgem safeGem = SocketedSpellgem.socketAtWorkstation(specificMulti);
        SocketedSpellgem fieldGem = SocketedSpellgem.socketInField(specificMulti, 5000L);
        CastingApparatusState grimoire = CastingApparatusState.emptyFor(ApparatusType.GRIMOIRE)
                .withSocket(0, safeGem)
                .withSocket(8, fieldGem)
                .cycleActiveSlot(-1);
        check(counts, lines, "Workstation (0s/0%) vs Field Swap (1200t/45%) & Grimoire Wrap-Around Cycling",
                !safeGem.isLockedOut(5000L)
                        && !safeGem.pendingFieldBackfireRoll()
                        && fieldGem.isLockedOut(6199L)
                        && !fieldGem.isLockedOut(6200L)
                        && fieldGem.pendingFieldBackfireRoll()
                        && !fieldGem.clearPendingBackfire().pendingFieldBackfireRoll()
                        && grimoire.activeSlotIndex() == 8);

        // 6. Mojang Codec JSON Round-Trip Serialization
        DataResult<JsonElement> encodedSpell = SpellAssembly.CODEC.encodeStart(JsonOps.INSTANCE, specificMulti);
        Optional<SpellAssembly> decodedSpell = encodedSpell.result()
                .flatMap(json -> SpellAssembly.CODEC.parse(JsonOps.INSTANCE, json).result());
        DataResult<JsonElement> encodedApp = CastingApparatusState.CODEC.encodeStart(JsonOps.INSTANCE, grimoire);
        Optional<CastingApparatusState> decodedApp = encodedApp.result()
                .flatMap(json -> CastingApparatusState.CODEC.parse(JsonOps.INSTANCE, json).result());

        check(counts, lines, "Mojang Codec JSON Round-Trip (SpellAssembly & 9-Slot Grimoire State)",
                decodedSpell.isPresent()
                        && decodedSpell.get().equals(specificMulti)
                        && decodedApp.isPresent()
                        && decodedApp.get().equals(grimoire));

        // 7. Air (#1) Per-Part Mutation Handler & Toggle Isolation
        boolean airHandlerGateOk = SpellMutationHandlerRegistry.resolveActive(EssenceType.AIR, true) != null
                && SpellMutationHandlerRegistry.resolveActive(EssenceType.AIR, false) == null;

        boolean airFormTitlesOk =
                "Turbulence Spiral".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.AIR, true).signatureTitle())
                && "Jetstream Propulsion".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.AIR, true).signatureTitle())
                && "Gust Shield".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.AIR, true).signatureTitle())
                && "Concussive Uppercut".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.AIR, true).signatureTitle())
                && "Slipstream Shield".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.AIR, true).signatureTitle())
                && "Geyser Mine (Air Trampoline)".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.AIR, true).signatureTitle())
                && "Howling Gale".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.AIR, true).signatureTitle())
                && "Tornado Vortex".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.AIR, true).signatureTitle())
                && "Zephyr Eidolon".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_AGENT, EssenceType.AIR, true).signatureTitle())
                && "Zephyr Updraft Sentry".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.AIR, true).signatureTitle())
                && "Zephyr Cyclone Decoy".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, EssenceType.AIR, true).signatureTitle())
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.AIR, false).isUnmutated();

        boolean airFlowAndEffectTitlesOk =
                "Sonic Leap Chains".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.AIR, true).signatureTitle())
                && "Vacuum Implosion".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.AIR, true).signatureTitle())
                && "Aeration & Dehydration".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.AIR, true).signatureTitle())
                && "Hypoxia Suffocation Hex".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.SECONDARY_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.AIR, true).signatureTitle())
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.SECONDARY_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.AIR, false).isUnmutated();

        check(counts, lines, "Air (#1) Per-Part Mutation Handler: All 9 Forms, Flow, Effects & OFF-Toggle Isolation",
                airHandlerGateOk && airFormTitlesOk && airFlowAndEffectTitlesOk);

        // 12. All 12 Primordial Base Materia (#1 Air through #12 Lightning) Complete Registration & Signature Resolution
        List<EssenceType> primordialTwelve = List.of(
                EssenceType.AIR, EssenceType.WATER, EssenceType.NATURE, EssenceType.EARTH,
                EssenceType.FROZEN, EssenceType.NETHER, EssenceType.RADIANT, EssenceType.UMBRAL,
                EssenceType.UNDEAD, EssenceType.VOID, EssenceType.ARID, EssenceType.LIGHTNING
        );

        boolean allTwelveRegisteredAndIsolated = SpellMutationHandlerRegistry.getAllRegistered().size() >= 12;
        for (EssenceType baseType : primordialTwelve) {
            var activeHandler = SpellMutationHandlerRegistry.resolveActive(baseType, true);
            var disabledHandler = SpellMutationHandlerRegistry.resolveActive(baseType, false);
            if (activeHandler == null || activeHandler.materiaType() != baseType || disabledHandler != null) {
                allTwelveRegisteredAndIsolated = false;
                break;
            }
            for (SpellDeliveryForm form : SpellDeliveryForm.values()) {
                var mutatedProf = SpellMutationRegistry.resolveForm(form, baseType, true);
                var unmutatedProf = SpellMutationRegistry.resolveForm(form, baseType, false);
                if (!mutatedProf.mutated() || mutatedProf.signatureTitle().isBlank() || !unmutatedProf.isUnmutated()) {
                    allTwelveRegisteredAndIsolated = false;
                    break;
                }
            }
            for (SpellCoreEffect eff : List.of(SpellCoreEffect.BLINK, SpellCoreEffect.TRANSMUTE, SpellCoreEffect.AFFLICTION)) {
                var mutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, baseType, true);
                var unmutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, baseType, false);
                if (!mutatedEff.mutated() || mutatedEff.signatureTitle().isBlank() || !unmutatedEff.isUnmutated()) {
                    allTwelveRegisteredAndIsolated = false;
                    break;
                }
            }
        }

        boolean signatureSpotChecksOk =
                "Hydro-Cannon Slug".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.WATER, true).signatureTitle())
                && "Verdant Growth Ray".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.NATURE, true).signatureTitle())
                && "Stone Ring Rampart".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.EARTH, true).signatureTitle())
                && "Flash-Freeze Stasis".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.FROZEN, true).signatureTitle())
                && "Immolation Shroud".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.NETHER, true).signatureTitle())
                && "Sunburst Confinement Sigil".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.RADIANT, true).signatureTitle())
                && "Shadow Dart Fan".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.UMBRAL, true).signatureTitle())
                && "Spectral Soul Swarm".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.UNDEAD, true).signatureTitle())
                && "Void-Stalker".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_AGENT, EssenceType.VOID, true).signatureTitle())
                && "Void Gravity Pylon".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.VOID, true).signatureTitle())
                && "Void Singularity Silhouette".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, EssenceType.VOID, true).signatureTitle())
                && "Petrofaction & Desiccation".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.ARID, true).signatureTitle())
                && "Magnetic Railgun".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.KINETIC, EssenceType.LIGHTNING, true).signatureTitle())
                && "Supercharged Forked Chains".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.LIGHTNING, true).signatureTitle());

        check(counts, lines, "Primordial Base #1-#12 Complete Per-Part Handlers, All 9 Forms, Core Effects & OFF-Toggle Isolation",
                allTwelveRegisteredAndIsolated && signatureSpotChecksOk);

        // 13. 4-Materia Hybrid Spell Assembly with Independent Per-Part Mutation Toggles & Separate Per-Type Fuel Billing
        SpellAssembly quadMateriaSpell = new SpellAssembly(
                "Galvanic Rime Breaker",
                List.of("4-Primordial Base Materia Mix"),
                true,
                SpellPrefix.COMBAT,
                4,
                new FlowPartBinding(SpellFlowModifier.CHAIN_LEAP, EssenceType.LIGHTNING, true, 1, 0.0F),
                List.of(SubSpellSlot.ofDualVerb(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.WATER, true),
                        EffectPartBinding.of(SpellCoreEffect.AFFLICTION, EssenceType.FROZEN, true),
                        EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.NETHER, false)
                )),
                SpellFlawProfile.PERFECT,
                2
        );
        SpellCostBreakdown quadBreakdown = SpellFuelCalculator.calculateCastCost(quadMateriaSpell, null, 0.0F, null);
        boolean quadBillingOk =
                quadBreakdown.totalMfumByType().containsKey(EssenceType.LIGHTNING)
                && quadBreakdown.totalMfumByType().containsKey(EssenceType.WATER)
                && quadBreakdown.totalMfumByType().containsKey(EssenceType.FROZEN)
                && quadBreakdown.totalMfumByType().containsKey(EssenceType.NETHER)
                && quadMateriaSpell.flowPart().resolveMutation(true, null).mutated()
                && quadMateriaSpell.subSpells().get(0).formPart().resolveMutation(true, null).mutated()
                && quadMateriaSpell.subSpells().get(0).primaryEffect().resolveMutation(SpellPartSlotType.CORE_EFFECT, true, null).mutated()
                && quadMateriaSpell.subSpells().get(0).secondaryEffect().orElseThrow().resolveMutation(SpellPartSlotType.SECONDARY_EFFECT, true, null).isUnmutated();

        check(counts, lines, "4-Materia Mixed Spell: Separate Per-Part Materia Costs + Selective Mutation ON/OFF", quadBillingOk);

        // 14. Ward 1.5x Shield Table & Restoration 0.5x Healing Table (Sections 5.3 & 5.7)
        boolean wardAndHealTablesOk =
                MateriaStageTable.getBaseWardShieldHp(1) == 24.0F
                && MateriaStageTable.getBaseWardShieldHp(5) == 96.0F
                && MateriaStageTable.getBaseWardShieldHp(10) == 3750.0F
                && MateriaStageTable.getBaseRestorationHealHp(1) == 8.0F
                && MateriaStageTable.getBaseRestorationHealHp(5) == 32.0F
                && MateriaStageTable.getBaseRestorationHealHp(10) == 1250.0F;
        check(counts, lines, "Ward Shield (1.5x: 24->3750 HP) & Restoration Healing (0.5x: 8->1250 HP) Scaling Tables",
                wardAndHealTablesOk);

        // 15. Eidolon Summon Triad Archetypes & Sub-Mode / Brightness Dial Serialization (Sections 4.9, 5.2, 5.7, 5.8)
        boolean triadArchetypesOk =
                SpellEidolonEntity.TriadArchetype.fromSubMode(null) == SpellEidolonEntity.TriadArchetype.AGENT
                && SpellEidolonEntity.TriadArchetype.fromSubMode("agent") == SpellEidolonEntity.TriadArchetype.AGENT
                && SpellEidolonEntity.TriadArchetype.fromSubMode("sentry") == SpellEidolonEntity.TriadArchetype.SENTRY
                && SpellEidolonEntity.TriadArchetype.fromSubMode("turret") == SpellEidolonEntity.TriadArchetype.SENTRY
                && SpellEidolonEntity.TriadArchetype.fromSubMode("decoy") == SpellEidolonEntity.TriadArchetype.DECOY
                && SpellEidolonEntity.TriadArchetype.fromSubMode("effigy") == SpellEidolonEntity.TriadArchetype.DECOY
                && "ᚫ".equals(SpellEidolonEntity.TriadArchetype.AGENT.getRuneGlyph())
                && "ᛉ".equals(SpellEidolonEntity.TriadArchetype.SENTRY.getRuneGlyph())
                && "ᚦ".equals(SpellEidolonEntity.TriadArchetype.DECOY.getRuneGlyph());

        SpellAssembly triadAndDialSpell = new SpellAssembly(
                "Umbral Sentry & Dim Bridge",
                List.of("Sentry Summon Form + Dialed Conjure"),
                true,
                SpellPrefix.UTILITY,
                3,
                new FlowPartBinding(SpellFlowModifier.SINGLE, EssenceType.UMBRAL, true, 1, 0.0F),
                List.of(SubSpellSlot.ofDualVerb(
                        FormPartBinding.of(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.UMBRAL, true),
                        new EffectPartBinding(SpellCoreEffect.CONJURE, EssenceType.UMBRAL, false, "bridge", 9),
                        new EffectPartBinding(SpellCoreEffect.RESTORATION, EssenceType.RADIANT, true, "mending", 15)
                )),
                SpellFlawProfile.PERFECT,
                1
        );
        Optional<SpellAssembly> decodedTriad = SpellAssembly.CODEC.encodeStart(JsonOps.INSTANCE, triadAndDialSpell)
                .result()
                .flatMap(json -> SpellAssembly.CODEC.parse(JsonOps.INSTANCE, json).result());

        boolean triadCodecOk = decodedTriad.isPresent()
                && decodedTriad.get().equals(triadAndDialSpell)
                && decodedTriad.get().subSpells().get(0).formPart().form() == SpellDeliveryForm.SUMMON_SENTRY
                && "sentry".equals(decodedTriad.get().subSpells().get(0).formPart().subMode())
                && "bridge".equals(decodedTriad.get().subSpells().get(0).primaryEffect().subMode())
                && decodedTriad.get().subSpells().get(0).primaryEffect().dialValue() == 9
                && "mending".equals(decodedTriad.get().subSpells().get(0).secondaryEffect().orElseThrow().subMode());

        check(counts, lines, "Eidolon Summon Triad (Agent/Sentry/Decoy) & Sub-Mode / Brightness Dial Codec Round-Trip",
                triadArchetypesOk && triadCodecOk);

        // 16. Next 12 Elemental & Vital Fusions (#13 Magma through #24 Amber) Complete Registration & Signature Resolution
        List<EssenceType> nextTwelveFusions = List.of(
                EssenceType.MAGMA, EssenceType.STORM, EssenceType.GLACIAL, EssenceType.OVERGROWTH,
                EssenceType.DUST, EssenceType.SPORE, EssenceType.TAIGA, EssenceType.OASIS,
                EssenceType.VITAE, EssenceType.BLOOD, EssenceType.AURA, EssenceType.AMBER
        );

        boolean allTwentyFourRegisteredAndIsolated = SpellMutationHandlerRegistry.getAllRegistered().size() >= 24;
        for (EssenceType fusionType : nextTwelveFusions) {
            var activeHandler = SpellMutationHandlerRegistry.resolveActive(fusionType, true);
            var disabledHandler = SpellMutationHandlerRegistry.resolveActive(fusionType, false);
            if (activeHandler == null || activeHandler.materiaType() != fusionType || disabledHandler != null) {
                allTwentyFourRegisteredAndIsolated = false;
                break;
            }
            for (SpellDeliveryForm form : SpellDeliveryForm.values()) {
                var mutatedProf = SpellMutationRegistry.resolveForm(form, fusionType, true);
                var unmutatedProf = SpellMutationRegistry.resolveForm(form, fusionType, false);
                if (!mutatedProf.mutated() || mutatedProf.signatureTitle().isBlank() || !unmutatedProf.isUnmutated()) {
                    allTwentyFourRegisteredAndIsolated = false;
                    break;
                }
            }
            for (SpellCoreEffect eff : List.of(SpellCoreEffect.BLINK, SpellCoreEffect.TRANSMUTE, SpellCoreEffect.AFFLICTION)) {
                var mutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, fusionType, true);
                var unmutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, fusionType, false);
                if (!mutatedEff.mutated() || mutatedEff.signatureTitle().isBlank() || !unmutatedEff.isUnmutated()) {
                    allTwentyFourRegisteredAndIsolated = false;
                    break;
                }
            }
        }

        boolean fusionSignatureSpotChecksOk =
                "Molten Magma Boulder".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.MAGMA, true).signatureTitle())
                && "Gale-Cutter Torrent Beam".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.STORM, true).signatureTitle())
                && "Glacial Monolith Cataclysm".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.GLACIAL, true).signatureTitle())
                && "Bramble Flurry & Phyto-Wave".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.OVERGROWTH, true).signatureTitle())
                && "Pneumatic Dust-Blast".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.KINETIC, EssenceType.DUST, true).signatureTitle())
                && "Spore-Tender".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_AGENT, EssenceType.SPORE, true).signatureTitle())
                && "Bio-Pollen Spire".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.SPORE, true).signatureTitle())
                && "Mushroom Decoy".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, EssenceType.SPORE, true).signatureTitle())
                && "Hardened Frost-Resin Spike".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TAIGA, true).signatureTitle())
                && "Succulent Bloom Split".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.DOUBLE_BIFURCATE, EssenceType.OASIS, true).signatureTitle())
                && "Spiraling Vitality Helix".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.VITAE, true).signatureTitle())
                && "Boiling Blood Mist Detonation".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.BLOOD, true).signatureTitle())
                && "Chakra Respiration Shroud".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.AURA, true).signatureTitle())
                && "Amber Preservation Vault Glyph".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.AMBER, true).signatureTitle())
                && "Resinite Viscous Linger Flow".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.SLOW_HEAVY, EssenceType.AMBER, true).signatureTitle())
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TAIGA, true).bonusPierceTargets() == 2
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TAIGA, false).bonusPierceTargets() == 0
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.BLOOD, true).bonusPierceTargets() == 2
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.AURA, true).bonusPierceTargets() == 2;

        check(counts, lines, "Elemental & Vital Fusions #13-#24 Complete Per-Part Handlers, All 9 Forms, Effects & OFF-Toggle Isolation",
                allTwentyFourRegisteredAndIsolated && fusionSignatureSpotChecksOk);

        // 17. 4-Fusion Chimera Assembly with Independent Per-Part Mutation Toggles & Separate Fusion Fuel Billing
        SpellAssembly quadFusionSpell = new SpellAssembly(
                "Sanguine Amber Mitosis",
                List.of("4-Fusion Materia Mix (#17/#21/#22/#24)"),
                true,
                SpellPrefix.COMBAT,
                5,
                new FlowPartBinding(SpellFlowModifier.DOUBLE_BIFURCATE, EssenceType.VITAE, true, 1, 0.0F),
                List.of(SubSpellSlot.ofDualVerb(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.AMBER, false),
                        EffectPartBinding.of(SpellCoreEffect.AFFLICTION, EssenceType.BLOOD, true),
                        EffectPartBinding.of(SpellCoreEffect.KINETIC, EssenceType.DUST, true)
                )),
                SpellFlawProfile.PERFECT,
                2
        );
        SpellCostBreakdown quadFusionCost = SpellFuelCalculator.calculateCastCost(quadFusionSpell, null, 0.0F, null);
        boolean quadFusionOk =
                quadFusionCost.totalMfumByType().containsKey(EssenceType.VITAE)
                && quadFusionCost.totalMfumByType().containsKey(EssenceType.AMBER)
                && quadFusionCost.totalMfumByType().containsKey(EssenceType.BLOOD)
                && quadFusionCost.totalMfumByType().containsKey(EssenceType.DUST)
                && quadFusionSpell.flowPart().resolveMutation(true, null).mutated()
                && quadFusionSpell.subSpells().get(0).formPart().resolveMutation(true, null).isUnmutated()
                && quadFusionSpell.subSpells().get(0).primaryEffect().resolveMutation(SpellPartSlotType.CORE_EFFECT, true, null).mutated()
                && quadFusionSpell.subSpells().get(0).secondaryEffect().orElseThrow().resolveMutation(SpellPartSlotType.SECONDARY_EFFECT, true, null).mutated();

        check(counts, lines, "4-Fusion Multi-Materia Spell (#17/#21/#22/#24): Per-Part Fuel Costs & Selective Mutation ON/OFF",
                quadFusionOk);

        // 18. Deep Audit Verification (#13-#24): Silk Touch / Auto-Smelt Inheritance, Lineage Ranges, Hex Flags & Stasis Tags
        boolean deepAuditTraitsOk =
                SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.MAGMA, true).autoSmeltInherited()
                && !SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.MAGMA, false).autoSmeltInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.GLACIAL, true).silkTouchInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.TAIGA, true).silkTouchInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.AMBER, true).silkTouchInherited()
                && !SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.AMBER, false).silkTouchInherited()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.AMBER, true).silkTouchInherited()
                && SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.STORM, true).velocityMultiplier() == 1.35F
                && SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.STORM, true).rangeMultiplier() == 1.25F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.GLACIAL, true).bonusPierceTargets() == 2
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.OVERGROWTH, true).rangeMultiplier() == 1.25F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.BLOOD, true).spawnLureHex()
                && !SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.BLOOD, false).spawnLureHex()
                && !SpellLingeringZoneManager.TAG_CRYO_COCOON_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_AMBER_FRAGILE_STATUE_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_VITAE_DISORIENTED_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_PACIFIED_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL.isBlank()
                && MateriaStageTable.getBaseCombatDamageHp(1) > 0.0F
                && MateriaStageTable.getBaseCombatDamageHp(10) > MateriaStageTable.getBaseCombatDamageHp(5)
                && MateriaStageTable.getBaseMfumCastCost(10) > MateriaStageTable.getBaseMfumCastCost(1);

        check(counts, lines, "Deep Audit (#13-#24): Magma Auto-Smelt, Glacial/Taiga/Amber Silk Touch, Storm Velocity, Blood Lure Hex & Stasis Tags",
                deepAuditTraitsOk);

        // 19. Ethereal & Paradox Fusions (#25 Eclipse through #36 Mirage) Complete Registration & Signature Resolution
        List<EssenceType> etherealParadoxTwelve = List.of(
                EssenceType.ECLIPSE, EssenceType.BLIGHT, EssenceType.ASTRAL, EssenceType.SOULFIRE,
                EssenceType.VAPOR, EssenceType.NULL_R, EssenceType.NULL_U, EssenceType.DAWN,
                EssenceType.ABYSS, EssenceType.AEGIS, EssenceType.AURORA, EssenceType.MIRAGE
        );

        boolean allThirtySixRegisteredAndIsolated = SpellMutationHandlerRegistry.getAllRegistered().size() >= 36;
        for (EssenceType epType : etherealParadoxTwelve) {
            var activeHandler = SpellMutationHandlerRegistry.resolveActive(epType, true);
            var disabledHandler = SpellMutationHandlerRegistry.resolveActive(epType, false);
            if (activeHandler == null || activeHandler.materiaType() != epType || disabledHandler != null) {
                allThirtySixRegisteredAndIsolated = false;
                break;
            }
            for (SpellFlowModifier flow : SpellFlowModifier.values()) {
                var mutatedFlow = SpellMutationRegistry.resolveFlow(flow, epType, true);
                var unmutatedFlow = SpellMutationRegistry.resolveFlow(flow, epType, false);
                if (!mutatedFlow.mutated() || mutatedFlow.signatureTitle().isBlank() || !unmutatedFlow.isUnmutated()) {
                    allThirtySixRegisteredAndIsolated = false;
                    break;
                }
            }
            for (SpellDeliveryForm form : SpellDeliveryForm.values()) {
                var mutatedProf = SpellMutationRegistry.resolveForm(form, epType, true);
                var unmutatedProf = SpellMutationRegistry.resolveForm(form, epType, false);
                if (!mutatedProf.mutated() || mutatedProf.signatureTitle().isBlank() || !unmutatedProf.isUnmutated()) {
                    allThirtySixRegisteredAndIsolated = false;
                    break;
                }
            }
            for (SpellCoreEffect eff : List.of(SpellCoreEffect.BLINK, SpellCoreEffect.TRANSMUTE, SpellCoreEffect.AFFLICTION)) {
                var mutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, epType, true);
                var unmutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, epType, false);
                if (!mutatedEff.mutated() || mutatedEff.signatureTitle().isBlank() || !unmutatedEff.isUnmutated()) {
                    allThirtySixRegisteredAndIsolated = false;
                    break;
                }
            }
        }

        boolean etherealParadoxSpotChecksOk =
                "Penumbra Crescent Scythe".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ECLIPSE, true).signatureTitle())
                && "Blight-Revenant".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_AGENT, EssenceType.BLIGHT, true).signatureTitle())
                && "Necro-Briar Spire".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.BLIGHT, true).signatureTitle())
                && "Cadaverous Effigy".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, EssenceType.BLIGHT, true).signatureTitle())
                && "Supercharged Void-Warp Chains Flow".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.ASTRAL, true).signatureTitle())
                && "Phantasmal Soul-Torch Jet".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.SOULFIRE, true).signatureTitle())
                && "Superheated Hydro-Gouge".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.VAPOR, true).signatureTitle())
                && "White Hole Singularity Orb".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_R, true).signatureTitle())
                && "Black Hole Singularity Orb".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_U, true).signatureTitle())
                && "Sunrise Horizon Nova".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.DAWN, true).signatureTitle())
                && "Crushing Trench Monolith".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.ABYSS, true).signatureTitle())
                && "Gilded Bastion Citadel Mantle".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.AEGIS, true).signatureTitle())
                && "Cryo-Refractive Mirror Trap".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.AURORA, true).signatureTitle())
                && "Delusional Phantasm Paranoia Hex".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.MIRAGE, true).signatureTitle());

        check(counts, lines, "Ethereal & Paradox Fusions #25-#36 Complete Per-Part Handlers, All 9 Forms, Effects & OFF-Toggle Isolation",
                allThirtySixRegisteredAndIsolated && etherealParadoxSpotChecksOk);

        // 20. 4-Materia Ethereal/Paradox Chimera Assembly (#27/#31/#35/#36)
        SpellAssembly quadParadoxSpell = new SpellAssembly(
                "Borealis Singularity Mirage",
                List.of("4-Materia Ethereal/Paradox Mix (#27/#31/#35/#36)"),
                true,
                SpellPrefix.COMBAT,
                6,
                new FlowPartBinding(SpellFlowModifier.CHAIN_LEAP, EssenceType.ASTRAL, true, 1, 0.0F),
                List.of(SubSpellSlot.ofDualVerb(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_U, false),
                        EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.AURORA, true),
                        EffectPartBinding.of(SpellCoreEffect.AFFLICTION, EssenceType.MIRAGE, true)
                )),
                SpellFlawProfile.PERFECT,
                2
        );
        SpellCostBreakdown quadParadoxCost = SpellFuelCalculator.calculateCastCost(quadParadoxSpell, null, 0.0F, null);
        boolean quadParadoxOk =
                quadParadoxCost.totalMfumByType().containsKey(EssenceType.ASTRAL)
                && quadParadoxCost.totalMfumByType().containsKey(EssenceType.NULL_U)
                && quadParadoxCost.totalMfumByType().containsKey(EssenceType.AURORA)
                && quadParadoxCost.totalMfumByType().containsKey(EssenceType.MIRAGE)
                && quadParadoxSpell.flowPart().resolveMutation(true, null).mutated()
                && quadParadoxSpell.subSpells().get(0).formPart().resolveMutation(true, null).isUnmutated()
                && quadParadoxSpell.subSpells().get(0).primaryEffect().resolveMutation(SpellPartSlotType.CORE_EFFECT, true, null).mutated()
                && quadParadoxSpell.subSpells().get(0).secondaryEffect().orElseThrow().resolveMutation(SpellPartSlotType.SECONDARY_EFFECT, true, null).mutated();

        check(counts, lines, "4-Materia Ethereal/Paradox Spell (#27/#31/#35/#36): Per-Part Fuel Costs & Selective Mutation ON/OFF",
                quadParadoxOk);

        // 21. Deep Audit (#25-#36): Pierce Tiers, Wall-Phasing, Form Armor Bypass, Lifesteal, Auto-Smelt, Spawn-Lure Hexes & Eidolon Tints
        boolean deepAudit25To36Ok =
                SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.DAWN, true).bonusPierceTargets() == 3
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.DAWN, false).bonusPierceTargets() == 0
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.AURORA, true).bonusPierceTargets() == 3
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ASTRAL, true).bonusPierceTargets() == 2
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ECLIPSE, true).bonusPierceTargets() == 1
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_R, true).bonusPierceTargets() == 1
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_U, true).bonusPierceTargets() == 1
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ASTRAL, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_R, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_U, true).wallPhasing()
                && !SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_U, false).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.NULL_R, true).armorBypassFraction() == 0.50F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.NULL_U, true).armorBypassFraction() == 0.50F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.NULL_U, false).armorBypassFraction() == 0.0F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.VAPOR, true).armorBypassFraction() == 0.40F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.SOULFIRE, true).armorBypassFraction() == 0.30F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.ECLIPSE, true).autoSmeltInherited()
                && !SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.ECLIPSE, false).autoSmeltInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.ECLIPSE, true).lifestealFraction() == 0.20F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.SOULFIRE, true).lifestealFraction() == 0.15F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ASTRAL, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.NULL_R, true).spawnLureHex()
                && !SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.NULL_R, false).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.BLIGHT, true).monsterEmpowermentHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.SOULFIRE, true).monsterEmpowermentHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ABYSS, true).monsterEmpowermentHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.AEGIS, true).monsterEmpowermentHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.MIRAGE, true).monsterEmpowermentHex()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_R, true).velocityMultiplier() == 0.65F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_U, true).velocityMultiplier() == 0.65F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NULL_U, false).velocityMultiplier() == 1.0F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.NULL_R, true).velocityMultiplier() == 1.0F
                && !SpellLingeringZoneManager.TAG_AURORA_MANTLE_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_MIRAGE_TRAITOR_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_CASTER_UUID.isBlank();

        check(counts, lines, "Deep Audit (#25-#36): Pierce Tiers, Wall-Phasing, Slow Singularity Orbs, Armor Bypass, Lifesteal, Auto-Smelt & Paranoia Tags",
                deepAudit25To36Ok);

        // 22. Ethereal (#37 Wraith, #38 Barrow), Tier 3 Trinity (#39 Pyre through #47 Oblivion) & Chimera Catalyst (Sec 6.49)
        List<EssenceType> trinityAndCatalystTwelve = List.of(
                EssenceType.WRAITH, EssenceType.BARROW, EssenceType.PYRE, EssenceType.PENUMBRA,
                EssenceType.RIME, EssenceType.SPRING, EssenceType.STATIC, EssenceType.SYLVAN,
                EssenceType.MIASMA, EssenceType.GENESIS, EssenceType.OBLIVION, EssenceType.CHIMERA
        );

        boolean allFortyEightRegisteredAndIsolated = SpellMutationHandlerRegistry.getAllRegistered().size() >= 48;
        for (EssenceType trType : trinityAndCatalystTwelve) {
            var activeHandler = SpellMutationHandlerRegistry.resolveActive(trType, true);
            var disabledHandler = SpellMutationHandlerRegistry.resolveActive(trType, false);
            if (activeHandler == null || activeHandler.materiaType() != trType || disabledHandler != null) {
                allFortyEightRegisteredAndIsolated = false;
                break;
            }
            for (SpellFlowModifier flow : SpellFlowModifier.values()) {
                var mutatedFlow = SpellMutationRegistry.resolveFlow(flow, trType, true);
                var unmutatedFlow = SpellMutationRegistry.resolveFlow(flow, trType, false);
                if (!mutatedFlow.mutated() || mutatedFlow.signatureTitle().isBlank() || !unmutatedFlow.isUnmutated()) {
                    allFortyEightRegisteredAndIsolated = false;
                    break;
                }
            }
            for (SpellDeliveryForm form : SpellDeliveryForm.values()) {
                var mutatedProf = SpellMutationRegistry.resolveForm(form, trType, true);
                var unmutatedProf = SpellMutationRegistry.resolveForm(form, trType, false);
                if (!mutatedProf.mutated() || mutatedProf.signatureTitle().isBlank() || !unmutatedProf.isUnmutated()) {
                    allFortyEightRegisteredAndIsolated = false;
                    break;
                }
            }
            for (SpellCoreEffect eff : List.of(SpellCoreEffect.BLINK, SpellCoreEffect.TRANSMUTE, SpellCoreEffect.AFFLICTION)) {
                var mutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, trType, true);
                var unmutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, trType, false);
                if (!mutatedEff.mutated() || mutatedEff.signatureTitle().isBlank() || !unmutatedEff.isUnmutated()) {
                    allFortyEightRegisteredAndIsolated = false;
                    break;
                }
            }
        }

        boolean trinitySpotChecksOk =
                "Spectral Wraith-Skull Missile".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.WRAITH, true).signatureTitle())
                && "Barrow Skeleton".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_AGENT, EssenceType.BARROW, true).signatureTitle())
                && "Cairn Beacon".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.BARROW, true).signatureTitle())
                && "Hollow Bone Effigy".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, EssenceType.BARROW, true).signatureTitle())
                && "Cremating Wildfire Jetstream Beam".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.PYRE, true).signatureTitle())
                && "Penumbral Corona-Umbra Binary Orbit Flow".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.PENUMBRA, true).signatureTitle())
                && "Glacial Rime Cataclysmic Blizzard".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.RIME, true).signatureTitle())
                && "Vernal Fountainhead Mantle".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.SPRING, true).signatureTitle())
                && "High-Voltage Static Arc-Fan Deluge".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.STATIC, true).signatureTitle())
                && "Canopy Solar-Mine".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.SYLVAN, true).signatureTitle())
                && "Virulent Septic Pestilence Hex".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.MIASMA, true).signatureTitle())
                && "Dawn-Tide Rebirth Nova".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.GENESIS, true).signatureTitle())
                && "Stygian Extinction Palm-Cleave".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.OBLIVION, true).signatureTitle())
                && "Unstable Quad-Elemental Mutation Flow".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.SINGLE, EssenceType.CHIMERA, true).signatureTitle());

        check(counts, lines, "Ethereal/Trinity/Catalyst #37-Chimera (48 Total Handlers): All 9 Forms, Flow, Effects & OFF-Toggle Isolation",
                allFortyEightRegisteredAndIsolated && trinitySpotChecksOk);

        // 23. 4-Materia Trinity & Catalyst Assembly (#37 Wraith / #41 Rime / #46 Genesis / Chimera)
        SpellAssembly quadTrinitySpell = new SpellAssembly(
                "Chimeric Rime Genesis Wraith",
                List.of("4-Materia Trinity & Catalyst Mix"),
                true,
                SpellPrefix.COMBAT,
                7,
                new FlowPartBinding(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.CHIMERA, true, 1, 0.0F),
                List.of(SubSpellSlot.ofDualVerb(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.WRAITH, true),
                        EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.RIME, false),
                        EffectPartBinding.of(SpellCoreEffect.RESTORATION, EssenceType.GENESIS, true)
                )),
                SpellFlawProfile.PERFECT,
                2
        );
        SpellCostBreakdown quadTrinityCost = SpellFuelCalculator.calculateCastCost(quadTrinitySpell, null, 0.0F, null);
        boolean quadTrinityOk =
                quadTrinityCost.totalMfumByType().containsKey(EssenceType.CHIMERA)
                && quadTrinityCost.totalMfumByType().containsKey(EssenceType.WRAITH)
                && quadTrinityCost.totalMfumByType().containsKey(EssenceType.RIME)
                && quadTrinityCost.totalMfumByType().containsKey(EssenceType.GENESIS)
                && quadTrinitySpell.flowPart().resolveMutation(true, null).mutated()
                && quadTrinitySpell.subSpells().get(0).formPart().resolveMutation(true, null).mutated()
                && quadTrinitySpell.subSpells().get(0).primaryEffect().resolveMutation(SpellPartSlotType.CORE_EFFECT, true, null).isUnmutated()
                && quadTrinitySpell.subSpells().get(0).secondaryEffect().orElseThrow().resolveMutation(SpellPartSlotType.SECONDARY_EFFECT, true, null).mutated();

        check(counts, lines, "4-Materia Trinity/Catalyst Spell (#37/#41/#46/Chimera): Per-Part Fuel Costs & Selective Mutation ON/OFF",
                quadTrinityOk);

        // 24. Deep Audit (#37-Chimera): Wall-Phasing, Pierce Tiers, Armor Bypass/Shred, Silk Touch, Auto-Smelt, Lifesteal & Hex Vectors
        boolean deepAudit37ToChimeraOk =
                SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.WRAITH, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.PENUMBRA, true).wallPhasing()
                && !SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.WRAITH, false).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.RIME, true).bonusPierceTargets() == 3
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.WRAITH, true).bonusPierceTargets() == 2
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.WRAITH, true).armorBypassFraction() == 0.50F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.PENUMBRA, true).armorBypassFraction() == 0.30F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.OBLIVION, true).armorShredPoints() == 3
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CHIMERA, true).armorShredPoints() == 4
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.RIME, true).silkTouchInherited()
                && !SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.RIME, false).silkTouchInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.PYRE, true).autoSmeltInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.OBLIVION, true).autoSmeltInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.WRAITH, true).lifestealFraction() == 0.20F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.BARROW, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.MIASMA, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.OBLIVION, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.CHIMERA, true).monsterEmpowermentHex()
                && !SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.CHIMERA, false).monsterEmpowermentHex()
                && !SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_CHIMERA_SHROUD_UNTIL.isBlank();

        check(counts, lines, "Deep Audit (#37-Chimera): Wraith/Penumbra Wall-Phase, Rime Silk Touch, Pyre/Oblivion Smelt, Shred & Hex Flags",
                deepAudit37ToChimeraOk);

        // 25. Canonical Eidolon Triad Audit (Sections 4.9, 6.1.2 & 6.2-6.49): Names, Exact Trinity/Chimera Base HP, Pure-Aura Spires & Death Radii
        boolean allEidolonNamesOk = true;
        for (EssenceType mat : SpellMutationHandlerRegistry.getAllRegistered().keySet()) {
            for (SpellEidolonEntity.TriadArchetype arch : SpellEidolonEntity.TriadArchetype.values()) {
                String cname = SpellEidolonSpecs.resolveCanonicalConstructName(mat, arch, true, "NETHER");
                String genericFallback = mat.getDisplayName() + " " + arch.getDisplayName();
                if (cname == null || cname.isBlank() || cname.equals(genericFallback)) {
                    allEidolonNamesOk = false;
                }
            }
        }
        boolean eidolonCanonicalAuditOk =
                allEidolonNamesOk
                && "Arcane Autonomous Agent".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.AIR, SpellEidolonEntity.TriadArchetype.AGENT, false, null))
                && "Solar Seraph".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.RADIANT, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Spore-Tender".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.SPORE, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Borealis-Archon".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.AURORA, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Penumbral Void-Bladewarden".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.PENUMBRA, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Dielectric Tesla Citadel Spire".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.STATIC, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Pyre of Nonexistence Obelisk".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.OBLIVION, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Zephyr-Beast".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.CHIMERA, SpellEidolonEntity.TriadArchetype.AGENT, true, "AIR"))
                && "Stone-Goliath".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.CHIMERA, SpellEidolonEntity.TriadArchetype.AGENT, true, "EARTH"))
                && "Abyssal-Hydra".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.CHIMERA, SpellEidolonEntity.TriadArchetype.AGENT, true, "WATER"))
                && "Hell-Chimera".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.CHIMERA, SpellEidolonEntity.TriadArchetype.AGENT, true, "NETHER"))
                && SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.PYRE)
                && SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.PENUMBRA)
                && SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.OBLIVION)
                && SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.CHIMERA)
                && !SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.UMBRAL)
                && !SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.REGULAR)
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.PYRE, SpellEidolonEntity.TriadArchetype.AGENT) == 70.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.PYRE, SpellEidolonEntity.TriadArchetype.SENTRY) == 85.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.PENUMBRA, SpellEidolonEntity.TriadArchetype.AGENT) == 60.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.PENUMBRA, SpellEidolonEntity.TriadArchetype.SENTRY) == 80.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.RIME, SpellEidolonEntity.TriadArchetype.AGENT) == 65.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.RIME, SpellEidolonEntity.TriadArchetype.SENTRY) == 85.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.SYLVAN, SpellEidolonEntity.TriadArchetype.AGENT) == 70.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.SYLVAN, SpellEidolonEntity.TriadArchetype.SENTRY) == 85.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.MIASMA, SpellEidolonEntity.TriadArchetype.AGENT) == 65.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.MIASMA, SpellEidolonEntity.TriadArchetype.SENTRY) == 80.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.GENESIS, SpellEidolonEntity.TriadArchetype.AGENT) == 70.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.GENESIS, SpellEidolonEntity.TriadArchetype.SENTRY) == 80.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.OBLIVION, SpellEidolonEntity.TriadArchetype.AGENT) == 75.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.OBLIVION, SpellEidolonEntity.TriadArchetype.SENTRY) == 85.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.CHIMERA, SpellEidolonEntity.TriadArchetype.AGENT) == 75.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.CHIMERA, SpellEidolonEntity.TriadArchetype.SENTRY) == 85.0D
                && SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.AIR)
                && SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.RADIANT)
                && SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.UMBRAL)
                && SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.GENESIS)
                && !SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.PENUMBRA)
                && !SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.OBLIVION)
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.AIR) == 4.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.VITAE) == 5.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.PYRE) == 6.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.OBLIVION) == 10.0D
                && !SpellLingeringZoneManager.TAG_STATIC_SENTRY_IMMUNE_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_STATIC_SENTRY_IMMUNE_UNTIL.equals(SpellLingeringZoneManager.TAG_STATIC_ORBIT_UNTIL);

        check(counts, lines, "Canonical Eidolon Triad Audit (#1-Chimera): Titles, Exact Trinity/Catalyst HP, Pure-Aura Spires & Death Radii",
                eidolonCanonicalAuditOk);

        // 26. Tier 4 Apex (#48-#50), Prismatic (#51), Tier 5 Eschaton (#52-#54) & Tier 6 Singularity (#55-#57) — All 58 Materia Suites!
        List<EssenceType> finalTenMateria = List.of(
                EssenceType.AETHER, EssenceType.ENTROPIC, EssenceType.CELESTIAL, EssenceType.PRISMATIC,
                EssenceType.EMPYREAN, EssenceType.CATACLYSM, EssenceType.TERMINUS,
                EssenceType.ESCHATON, EssenceType.APOTHEOSIS, EssenceType.ENTROPICA
        );

        boolean allFiftyEightRegisteredAndIsolated = SpellMutationHandlerRegistry.getAllRegistered().size() == 63;
        for (EssenceType apexType : finalTenMateria) {
            var activeHandler = SpellMutationHandlerRegistry.resolveActive(apexType, true);
            var disabledHandler = SpellMutationHandlerRegistry.resolveActive(apexType, false);
            if (activeHandler == null || activeHandler.materiaType() != apexType || disabledHandler != null) {
                allFiftyEightRegisteredAndIsolated = false;
                break;
            }
            for (SpellFlowModifier flow : SpellFlowModifier.values()) {
                var mutatedFlow = SpellMutationRegistry.resolveFlow(flow, apexType, true);
                var unmutatedFlow = SpellMutationRegistry.resolveFlow(flow, apexType, false);
                if (!mutatedFlow.mutated() || mutatedFlow.signatureTitle().isBlank() || !unmutatedFlow.isUnmutated()) {
                    allFiftyEightRegisteredAndIsolated = false;
                    break;
                }
            }
            for (SpellDeliveryForm form : SpellDeliveryForm.values()) {
                var mutatedProf = SpellMutationRegistry.resolveForm(form, apexType, true);
                var unmutatedProf = SpellMutationRegistry.resolveForm(form, apexType, false);
                if (!mutatedProf.mutated() || mutatedProf.signatureTitle().isBlank() || !unmutatedProf.isUnmutated()) {
                    allFiftyEightRegisteredAndIsolated = false;
                    break;
                }
            }
            for (SpellCoreEffect eff : List.of(SpellCoreEffect.BLINK, SpellCoreEffect.TRANSMUTE, SpellCoreEffect.BREAK, SpellCoreEffect.AFFLICTION)) {
                var mutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, apexType, true);
                var unmutatedEff = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, apexType, false);
                if (!mutatedEff.mutated() || mutatedEff.signatureTitle().isBlank() || !unmutatedEff.isUnmutated()) {
                    allFiftyEightRegisteredAndIsolated = false;
                    break;
                }
            }
        }

        boolean finalTenSpotChecksOk =
                "Apex Celestial Meteorite".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.AETHER, true).signatureTitle())
                && "Abyssal Sanguine-Void Chain Flow".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.ENTROPIC, true).signatureTitle())
                && "Holy Solar Singularity Dart".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CELESTIAL, true).signatureTitle())
                && "Holy White-Hole Star-Orbit Flow".equals(SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.CELESTIAL, true).signatureTitle())
                && "Holy Pranic Fusion Beam & Cataclysmic Supernova".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.CELESTIAL, true).signatureTitle())
                && "Holy Supernova Dawn-Shockwave".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.CELESTIAL, true).signatureTitle())
                && "Holy Supernova Cleave / White-Hole Touch".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.CELESTIAL, true).signatureTitle())
                && "Holy Celestial Supernova Shroud".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.CELESTIAL, true).signatureTitle())
                && "Holy White-Hole Sigil".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.CELESTIAL, true).signatureTitle())
                && "Holy Dawn-Deluge Cone".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.CELESTIAL, true).signatureTitle())
                && "Cataclysmic Supernova Starfall".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.CELESTIAL, true).signatureTitle())
                && "Celestial Supernova Ascendance Step".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.CELESTIAL, true).signatureTitle())
                && "Celestial Genesis & Consecration Transmutation".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.CELESTIAL, true).signatureTitle())
                && "Divine Genesis Brand / Holy Supernova Overload Hex".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.CELESTIAL, true).signatureTitle())
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.CELESTIAL, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_HOLY_SOLAR_SINGULARITY_DART
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantPrimaryRgb(EssenceType.CELESTIAL, 0.0D) == 0xFFEA78
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantAccentRgb(EssenceType.CELESTIAL, null, 0.0D) == 0xFF6B9D
                && "Prismatic Oscillating Light-Disc".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.PRISMATIC, true).signatureTitle())
                && "World-Tree Siphoning Seed-Pod".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.EMPYREAN, true).signatureTitle())
                && "Catastrophic Hellfire Railgun Slug".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CATACLYSM, true).signatureTitle())
                && "Absolute-Zero Permafrost Lance".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TERMINUS, true).signatureTitle())
                && "Black-Hole Railgun Dart".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ESCHATON, true).signatureTitle())
                && "Spacetime Fold Slip".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.ESCHATON, true).signatureTitle())
                && "Seeking Crown of Dawn".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Singularity Equilibrium Lance".equals(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPICA, true).signatureTitle())
                && "Grand Cosmic Equilibrium Transmutation".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.ENTROPICA, true).signatureTitle())
                && "Grand Equilibrium Paradox Hex".equals(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ENTROPICA, true).signatureTitle());

        check(counts, lines, "Apex/Prismatic/Eschaton/Singularity #48-#57 (All 58 Canonical Handlers): All 9 Forms, Flow, Effects & OFF Isolation",
                allFiftyEightRegisteredAndIsolated && finalTenSpotChecksOk);

        // 27. 4-Materia Apex/Prismatic/Singularity Assembly (#48 Aether / #51 Prismatic / #55 Eschaton / #57 Entropica) + Codec Round-Trip
        SpellAssembly quadSingularitySpell = new SpellAssembly(
                "Grand Paradox Prism-Lance",
                List.of("4-Materia Apex/Prismatic/Singularity Mix"),
                true,
                SpellPrefix.COMBAT,
                9,
                new FlowPartBinding(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.AETHER, true, 1, 0.0F),
                List.of(SubSpellSlot.ofDualVerb(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.PRISMATIC, true),
                        EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.ESCHATON, false),
                        EffectPartBinding.of(SpellCoreEffect.AFFLICTION, EssenceType.ENTROPICA, true)
                )),
                SpellFlawProfile.PERFECT,
                2
        );
        DataResult<JsonElement> encodedSingularity = SpellAssembly.CODEC.encodeStart(JsonOps.INSTANCE, quadSingularitySpell);
        Optional<SpellAssembly> decodedSingularity = encodedSingularity.result()
                .flatMap(j -> SpellAssembly.CODEC.parse(JsonOps.INSTANCE, j).result());
        SpellCostBreakdown quadSingularityCost = SpellFuelCalculator.calculateCastCost(quadSingularitySpell, null, 0.0F, null);
        boolean quadSingularityOk =
                decodedSingularity.isPresent()
                && "Grand Paradox Prism-Lance".equals(decodedSingularity.get().customName())
                && quadSingularityCost.totalMfumByType().containsKey(EssenceType.AETHER)
                && quadSingularityCost.totalMfumByType().containsKey(EssenceType.PRISMATIC)
                && quadSingularityCost.totalMfumByType().containsKey(EssenceType.ESCHATON)
                && quadSingularityCost.totalMfumByType().containsKey(EssenceType.ENTROPICA)
                && quadSingularitySpell.flowPart().resolveMutation(true, null).mutated()
                && quadSingularitySpell.subSpells().get(0).formPart().resolveMutation(true, null).mutated()
                && quadSingularitySpell.subSpells().get(0).primaryEffect().resolveMutation(SpellPartSlotType.CORE_EFFECT, true, null).isUnmutated()
                && quadSingularitySpell.subSpells().get(0).secondaryEffect().orElseThrow().resolveMutation(SpellPartSlotType.SECONDARY_EFFECT, true, null).mutated();

        check(counts, lines, "4-Materia Singularity/Apex Spell (#48/#51/#55/#57): Codec Round-Trip, Fuel Split & Selective Mutation ON/OFF",
                quadSingularityOk);

        // 28. Deep Audit (#48-#57): Prismatic Ricochet vs Wall-Phasing Isolation, Pierce Tiers, Bypass/Shred, Lifesteal & Hybrid Hex
        boolean deepAudit48To57Ok =
                !SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.PRISMATIC, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.PRISMATIC, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.PRISMATIC, true).wallPhasing()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.PRISMATIC, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.AETHER, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.EMPYREAN, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ESCHATON, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPICA, true).wallPhasing()
                && !SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPICA, false).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.AETHER, true).bonusPierceTargets() == 4
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPIC, true).bonusPierceTargets() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CELESTIAL, true).bonusPierceTargets() == 4
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.PRISMATIC, true).bonusPierceTargets() == 0
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.EMPYREAN, true).bonusPierceTargets() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CATACLYSM, true).bonusPierceTargets() == 10
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TERMINUS, true).bonusPierceTargets() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ESCHATON, true).bonusPierceTargets() == 12
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.APOTHEOSIS, true).bonusPierceTargets() == 10
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPICA, true).bonusPierceTargets() == 12
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.AETHER, true).armorBypassFraction() == 0.50F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.PRISMATIC, true).armorBypassFraction() == 0.50F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.EMPYREAN, true).armorBypassFraction() == 0.60F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.ESCHATON, true).armorBypassFraction() == 0.75F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.ENTROPICA, true).armorBypassFraction() == 0.75F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.AETHER, true).armorShredPoints() == 5
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.AETHER, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.CATACLYSM, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.CATACLYSM, true).armorShredPoints() == 7
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.ENTROPICA, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.ENTROPICA, true).armorShredPoints() == 3
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.ENTROPIC, true).lifestealFraction() == 0.30F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.CELESTIAL, true).lifestealFraction() == 0.35F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.EMPYREAN, true).lifestealFraction() == 0.35F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.APOTHEOSIS, true).lifestealFraction() == 0.25F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.ENTROPICA, true).lifestealFraction() == 0.50F
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.TERMINUS, true).silkTouchInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.PRISMATIC, true).autoSmeltInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.CATACLYSM, true).autoSmeltInherited()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.AETHER, true).monsterEmpowermentHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ENTROPIC, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.EMPYREAN, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.CATACLYSM, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.TERMINUS, true).monsterEmpowermentHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ESCHATON, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.APOTHEOSIS, true).monsterEmpowermentHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ENTROPICA, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ENTROPICA, true).monsterEmpowermentHex()
                && !SpellLingeringZoneManager.TAG_ARCANE_3D_FLIGHT_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_TERMINUS_ELIXIR_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_ESCHATON_STASIS_ELIXIR_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_PRISMATIC_DEFLECT_SENTRY_UNTIL.isBlank();

        check(counts, lines, "Deep Audit (#48-#57): Prismatic Ricochet Isolation, Pierce (4/6/8/10/12), Bypass/Shred, Lifesteal & Paradox Dual-Hex",
                deepAudit48To57Ok);

        // 29. Canonical Eidolon Triad Audit (#48-#57, Sections 6.50-6.59): Lifecycles, HP Tiers, Prismatic Dome & Death Radii
        boolean apexSingularityEidolonOk =
                finalTenMateria.stream().allMatch(SpellEidolonSpecs::isLifecycleRefreshMateria)
                && SpellEidolonSpecs.resolveCanonicalLifecycleTicks(EssenceType.AETHER, SpellEidolonEntity.TriadArchetype.AGENT) == 900
                && SpellEidolonSpecs.resolveCanonicalLifecycleTicks(EssenceType.PRISMATIC, SpellEidolonEntity.TriadArchetype.SENTRY) == 900
                && SpellEidolonSpecs.resolveCanonicalLifecycleTicks(EssenceType.AETHER, SpellEidolonEntity.TriadArchetype.DECOY) == 280
                && SpellEidolonSpecs.resolveCanonicalLifecycleTicks(EssenceType.EMPYREAN, SpellEidolonEntity.TriadArchetype.AGENT) == 1200
                && SpellEidolonSpecs.resolveCanonicalLifecycleTicks(EssenceType.TERMINUS, SpellEidolonEntity.TriadArchetype.DECOY) == 320
                && SpellEidolonSpecs.resolveCanonicalLifecycleTicks(EssenceType.ESCHATON, SpellEidolonEntity.TriadArchetype.AGENT) == 1800
                && SpellEidolonSpecs.resolveCanonicalLifecycleTicks(EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.SENTRY) == 1800
                && SpellEidolonSpecs.resolveCanonicalLifecycleTicks(EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.DECOY) == 400
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.AETHER, SpellEidolonEntity.TriadArchetype.AGENT) == 90.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.AETHER, SpellEidolonEntity.TriadArchetype.SENTRY) == 100.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.CELESTIAL, SpellEidolonEntity.TriadArchetype.AGENT) == 100.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.PRISMATIC, SpellEidolonEntity.TriadArchetype.AGENT) == 100.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.EMPYREAN, SpellEidolonEntity.TriadArchetype.AGENT) == 110.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.CATACLYSM, SpellEidolonEntity.TriadArchetype.SENTRY) == 120.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.ESCHATON, SpellEidolonEntity.TriadArchetype.AGENT) == 140.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.APOTHEOSIS, SpellEidolonEntity.TriadArchetype.SENTRY) == 150.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.AGENT) == 140.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.SENTRY) == 150.0D
                && SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.PRISMATIC)
                && !SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.AETHER)
                && !SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.ESCHATON)
                && !SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.ENTROPICA)
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.CELESTIAL) == 10.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.PRISMATIC) == 10.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.AETHER) == 12.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.ENTROPIC) == 12.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.EMPYREAN) == 14.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.ESCHATON) == 20.0D
                && SpellEidolonSpecs.resolveCanonicalDeathBurstRadius(EssenceType.ENTROPICA) == 20.0D
                && "Firmament Archon-Colossus".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.AETHER, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Refractive Mirror Monolith".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.PRISMATIC, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Sacrificial Solar Relic".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.PRISMATIC, SpellEidolonEntity.TriadArchetype.DECOY, true, null))
                && "Yggdrasil Arboreal Juggernaut".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.EMPYREAN, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Event-Horizon Leviathan".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ESCHATON, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Avatar of Entropica".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Altar of the Horizon".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Fractured Paradox Core".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.DECOY, true, null));

        check(counts, lines, "Canonical Eidolon Triad Audit (#48-#57): 45s/60s/90s Lifecycles, 90-150 HP, Prismatic Mirror Dome & 10-20m Radii",
                apexSingularityEidolonOk);

        // 30. Three Separate Summon Creation Forms (SUMMON_AGENT / SUMMON_SENTRY / SUMMON_DECOY), Single-Summon-Type Per Spell & Legacy Codec
        boolean all58SeparateSummonTitlesOk = true;
        for (EssenceType mat : SpellMutationHandlerRegistry.getAllRegistered().keySet()) {
            String agentTitle = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_AGENT, mat, true).signatureTitle();
            String sentryTitle = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, mat, true).signatureTitle();
            String decoyTitle = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, mat, true).signatureTitle();
            if (agentTitle.isBlank() || sentryTitle.isBlank() || decoyTitle.isBlank()
                    || agentTitle.equals(sentryTitle) || agentTitle.equals(decoyTitle) || sentryTitle.equals(decoyTitle)
                    || agentTitle.contains("Triad") || sentryTitle.contains("Triad") || decoyTitle.contains("Triad")
                    || agentTitle.contains("Suite") || sentryTitle.contains("Suite") || decoyTitle.contains("Suite")) {
                all58SeparateSummonTitlesOk = false;
                break;
            }
        }

        // Multi-slot spell attempting to mix SUMMON_SENTRY + SUMMON_DECOY must lock all summon slots to the single chosen summon type!
        SpellAssembly mixedSummonAttempt = new SpellAssembly(
                "Sentry Consistency Test",
                List.of(),
                true,
                SpellPrefix.COMBAT,
                5,
                new FlowPartBinding(SpellFlowModifier.TRIPLE_TRIFURCATE, EssenceType.PRISMATIC, true, 3, 0.0F),
                List.of(
                        SubSpellSlot.of(
                                FormPartBinding.of(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.PRISMATIC, true),
                                EffectPartBinding.of(SpellCoreEffect.WARD, EssenceType.PRISMATIC, true)),
                        SubSpellSlot.of(
                                FormPartBinding.of(SpellDeliveryForm.SUMMON_DECOY, EssenceType.ENTROPICA, true),
                                EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.ENTROPICA, true)),
                        SubSpellSlot.of(
                                FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.AETHER, true),
                                EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.AETHER, true))
                ),
                SpellFlawProfile.PERFECT,
                0
        );
        boolean singleSummonPerSpellEnforced =
                mixedSummonAttempt.getChosenSummonForm().orElse(null) == SpellDeliveryForm.SUMMON_SENTRY
                && mixedSummonAttempt.subSpells().get(0).formPart().form() == SpellDeliveryForm.SUMMON_SENTRY
                && mixedSummonAttempt.subSpells().get(1).formPart().form() == SpellDeliveryForm.SUMMON_SENTRY
                && mixedSummonAttempt.subSpells().get(1).formPart().resolveSummonArchetype() == SpellEidolonEntity.TriadArchetype.SENTRY
                && mixedSummonAttempt.subSpells().get(2).formPart().form() == SpellDeliveryForm.PROJECTILE;

        // Legacy JSON {"form":"summon","materia_type":"prismatic","mutation_enabled":true,"sub_mode":"decoy"} -> SUMMON_DECOY
        com.google.gson.JsonObject legacyDecoyJson = new com.google.gson.JsonObject();
        legacyDecoyJson.addProperty("form", "summon");
        legacyDecoyJson.addProperty("materia_type", "prismatic");
        legacyDecoyJson.addProperty("mutation_enabled", true);
        legacyDecoyJson.addProperty("sub_mode", "decoy");
        Optional<FormPartBinding> legacyDecoded = FormPartBinding.CODEC.parse(JsonOps.INSTANCE, legacyDecoyJson).result();
        boolean legacySummonCodecOk = legacyDecoded.isPresent()
                && legacyDecoded.get().form() == SpellDeliveryForm.SUMMON_DECOY
                && legacyDecoded.get().resolveSummonArchetype() == SpellEidolonEntity.TriadArchetype.DECOY
                && "Discordant Chimeric Juggernaut".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_AGENT, EssenceType.CHIMERA, true).signatureTitle())
                && "Chromatic Crucible Spire".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.CHIMERA, true).signatureTitle())
                && "Unstable Hybrid Effigy".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, EssenceType.CHIMERA, true).signatureTitle());

        check(counts, lines, "3 Separate Summon Creation Forms (Agent/Sentry/Decoy), 1 Chosen Summon Type Per Spell & Legacy Codec",
                all58SeparateSummonTitlesOk && singleSummonPerSpellEnforced && legacySummonCodecOk);

        // 31. Physical Workstations, Seed Maturation, Alloy Tempering & Weapon Engraving
        CastingApparatusState engravedSword = CastingApparatusState.emptyFor(ApparatusType.ENGRAVED_TOOL)
                .withSocket(0, SocketedSpellgem.socketAtWorkstation(quadSingularitySpell))
                .withMaterialEfficiencyBonus(0.30F);
        SpellCostBreakdown untemperedCost = SpellFuelCalculator.calculateCastCost(quadSingularitySpell, ApparatusType.ENGRAVED_TOOL, 0.0F, null);
        SpellCostBreakdown temperedCost = SpellFuelCalculator.calculateCastCost(quadSingularitySpell, ApparatusType.ENGRAVED_TOOL, engravedSword.materialEfficiencyBonus(), null);
        boolean workstationsAndItemsOk =
                ddraig.net.entropica.item.SpellgemSeedItem.MATURATION_TICKS == 12_000
                && ddraig.net.entropica.block.entity.ResearchBenchBlockEntity.STUDY_DURATION_TICKS == 100
                && ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity.MIN_INTERVAL_TICKS == 20
                && ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity.MAX_INTERVAL_TICKS == 1_200
                && ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity.DEFAULT_INTERVAL_TICKS == 100
                && ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity.MAX_LINKED_INVENTORIES == 8
                && ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity.MAX_LINK_DISTANCE_SQ == 256.0D
                && ddraig.net.entropica.block.entity.RunicAnvilBlockEntity.TOTAL_FORGE_TICKS == 68
                && ddraig.net.entropica.block.entity.RunicAnvilBlockEntity.SMASH_IMPACT_TICK == 48
                && ddraig.net.entropica.block.entity.RunicAnvilBlockEntity.SLOT_OUTPUT == 2
                && engravedSword.apparatusType() == ApparatusType.ENGRAVED_TOOL
                && engravedSword.sockets().size() == 1
                && !engravedSword.getActiveSocket().isLockedOut(1000L)
                && !engravedSword.getActiveSocket().pendingFieldBackfireRoll()
                && engravedSword.materialEfficiencyBonus() == 0.30F
                && temperedCost.getTotalMfumAcrossAllTypes() < untemperedCost.getTotalMfumAcrossAllTypes();

        check(counts, lines, "Workstations, Seed Maturation (12,000t), Research Study (100t), Pedestal Bounds & 30% Alloy Tempering",
                workstationsAndItemsOk);

        // 32. Custom 3D Blockbench Models & PNG Textures Integrity on Classpath
        boolean customModelsAndTexturesOk = verifyClasspathModelsAndTextures();
        check(counts, lines, "All 8 Custom 3D Blockbench Models (Named Gem/Port Groups) & 16 PNG Textures Present & Valid",
                customModelsAndTexturesOk);

        // 33. Custom 3D Projectile Archetypes, World-Space 3D VFX Lifecycle & 42 Custom Particle Assets
        boolean vfxPalettesAndArchetypesOk = true;
        for (EssenceType et : EssenceType.values()) {
            int pRgb = ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantPrimaryRgb(et, 40.0D);
            int aRgb = ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantAccentRgb(et, null, 40.0D);
            byte arch = ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(et, false, false);
            if (pRgb <= 0 || pRgb > 0xFFFFFF || aRgb <= 0 || aRgb > 0xFFFFFF
                    || arch < 0 || arch > ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_SINGULARITY_EQUILIBRIUM_LANCE) {
                vfxPalettesAndArchetypesOk = false;
                break;
            }
        }
        boolean archetypeSpotChecksOk =
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.ENTROPICA, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_SINGULARITY_EQUILIBRIUM_LANCE
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.CATACLYSM, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_HELLFIRE_RAILGUN_SLUG
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.EMPYREAN, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_WORLD_TREE_SEED_POD
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.PRISMATIC, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_PRISMATIC_CHAKRAM
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.ESCHATON, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_BLACK_HOLE_RAILGUN_DART
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.TERMINUS, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_PERMAFROST_LANCE
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.LIGHTNING, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_BALL_LIGHTNING
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.APOTHEOSIS, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_SEEKING_CROWN_OF_DAWN
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.MAGMA, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_MOLTEN_METEOR
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.BLOOD, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_BIO_HELIX
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.AMBER, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_AMBER_BUBBLE_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.ECLIPSE, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_PENUMBRA_CRESCENT_SCYTHE
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.BLIGHT, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_NECRO_SPORE_POD
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.ASTRAL, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_CELESTIAL_COMET
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.SOULFIRE, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_SOULFLAME_SKULL
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.VAPOR, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_STEAM_GEYSER_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.NULL_R, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_WHITE_HOLE_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.NULL_R, true, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_CRYSTALLINE_LANCE
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.NULL_U, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_BLACK_HOLE_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.NULL_U, true, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_CRYSTALLINE_LANCE
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.DAWN, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_SOLAR_HORIZON_JAVELIN
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.ABYSS, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_ABYSSAL_DEPTH_CHARGE_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.AEGIS, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_GILDED_PAVISE_SHIELD
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.AURORA, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_AURORA_RIBBON_LANCE
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.MIRAGE, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_MIRAGE_SAND_JAVELIN
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.WRAITH, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_WRAITH_SKULL
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.BARROW, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_TOMBSTONE_BOULDER
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.PYRE, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_COMBUSTION_HEARTH_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.PENUMBRA, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_PENUMBRAL_ECLIPSE_BOLT
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.RIME, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_HOARFROST_RIME_JAVELIN
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.SPRING, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_VERNAL_AQUIFER_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.STATIC, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_DIELECTRIC_MORTAR_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.SYLVAN, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_ELDERWOOD_SUN_JAVELIN
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.MIASMA, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_SWAMPLAND_GAS_POD
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.GENESIS, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_PRIMORDIAL_DAWN_TEAR
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.OBLIVION, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_STYGIAN_ANNIHILATION_SKULL
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.AETHER, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_APEX_CELESTIAL_METEORITE
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.ENTROPIC, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_ENTROPIC_SINGULARITY_SKULL_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.CELESTIAL, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_HOLY_SOLAR_SINGULARITY_DART
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.AIR, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_WIND_SPIRAL;

        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.tickClient(false);
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.enqueueClientEvent(new ddraig.net.entropica.network.SpellVfxEventPayload(
                ddraig.net.entropica.network.SpellVfxEventPayload.VFX_BEAM,
                0.0F, 64.0F, 0.0F, 0.0F, 64.0F, 12.0F,
                0x62E8FF, 0xDF78FF, EssenceType.ENTROPICA.ordinal(), 0.35F, 5, 10
        ));
        boolean vfxLifecycleOk = ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.snapshotClientEvents().size() == 1;
        for (int t = 0; t < 5; t++) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.tickClient(true);
        }
        vfxLifecycleOk = vfxLifecycleOk && ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.snapshotClientEvents().isEmpty();

        boolean customParticlesOnClasspathOk = verifyClasspathCustomParticles();
        check(counts, lines, "3D Spell VFX Engine: All 8 Projectile Archetypes, 11 World VFX Lifecycle & 42 Custom Particle Assets",
                vfxPalettesAndArchetypesOk && archetypeSpotChecksOk && vfxLifecycleOk && customParticlesOnClasspathOk);

        // 34. Arcane 3D Wings Lifecycle, Materia Palette Coverage & Open-Air Utility/Misc Verb Dispatch
        ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.clearClientCache();
        ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.handleSync(new ddraig.net.entropica.network.SyncArcaneWingsPayload(
                777, 200, EssenceType.NETHER.ordinal(), EssenceType.LIGHTNING.ordinal(), 8
        ));
        var wingInst = ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.getActiveWingState(777);
        boolean wingsInitialOk = wingInst != null
                && wingInst.primaryMateria == EssenceType.NETHER
                && wingInst.secondaryMateria == EssenceType.LIGHTNING
                && wingInst.potencyTier == 8
                && wingInst.getDeployProgress(0.5F) > 0.0F
                && wingInst.getDeployProgress(0.5F) < 1.0F;
        for (int t = 0; t < 10; t++) {
            ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.tickClient(true);
        }
        boolean wingsUnfurledOk = wingInst != null && Math.abs(wingInst.getDeployProgress(0.5F) - 1.0F) < 1.0E-4F;
        ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.handleSync(new ddraig.net.entropica.network.SyncArcaneWingsPayload(
                777, 0, EssenceType.NETHER.ordinal(), -1, 8
        ));
        boolean wingsFoldingOk = wingInst != null && wingInst.expiring;
        for (int t = 0; t < 15; t++) {
            ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.tickClient(true);
        }
        boolean wingsExpiredCleanlyOk = ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.getActiveWingState(777) == null;

        boolean openAirVerbsOk = true;
        for (SpellCoreEffect openVerb : List.of(
                SpellCoreEffect.FLIGHT, SpellCoreEffect.ATMOSPHERE, SpellCoreEffect.SONAR,
                SpellCoreEffect.ACOUSTIC, SpellCoreEffect.LOGISTICS, SpellCoreEffect.DIAGNOSTIC,
                SpellCoreEffect.CONJURE, SpellCoreEffect.BLINK, SpellCoreEffect.GRAVITY
        )) {
            SubSpellSlot testSlot = new SubSpellSlot(
                    FormPartBinding.of(SpellDeliveryForm.BEAM, EssenceType.REGULAR, false),
                    EffectPartBinding.of(openVerb, EssenceType.REGULAR, false),
                    Optional.empty()
            );
            if (!ddraig.net.entropica.spell.engine.SpellExecutionEngine.hasOpenAirBlockVerb(testSlot)) {
                openAirVerbsOk = false;
                break;
            }
        }
        check(counts, lines, "Arcane 3D Wings Client Lifecycle (Unfurl/Materia Sync/Fold) & Open-Air Utility/Misc Spell Dispatch",
                wingsInitialOk && wingsUnfurledOk && wingsFoldingOk && wingsExpiredCleanlyOk && openAirVerbsOk);

        // 35. Re-entrant Lingering Zone Mutation Safety (Water Whirlpool Geyser & Two-Stage Skyfall Child Zone Spawning)
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> testDim = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("entropica", "spell_zone_verify_dim")
        );
        SpellLingeringZoneManager.clearDimensionZones(testDim);
        boolean reentrantZonesOk = true;
        try {
            // Case A: Armed Water Whirlpool Geyser Glyph spawns a 60t Vortex Zone mid-tick AND returns false (self-removes)
            SpellLingeringZoneManager.registerDimensionZoneRaw(testDim, new SpellLingeringZoneManager.ActiveSpellZone(
                    null, new net.minecraft.world.phys.Vec3(0.0D, 64.0D, 0.0D), 3.0D, 100, 1000L,
                    null, null, EssenceType.WATER, 1.0F,
                    (srv, zone, elapsed) -> {
                        SpellLingeringZoneManager.registerDimensionZoneRaw(testDim, new SpellLingeringZoneManager.ActiveSpellZone(
                                null, zone.center, 3.0D, 5, 1025L,
                                null, null, EssenceType.WATER, 1.0F,
                                (csrv, czone, celapsed) -> celapsed <= czone.maxDurationTicks
                        ));
                        return false;
                    }
            ));
            // Case B: Expiring Two-Stage Skyfall spawns a lingering aftermath pool during final expiry cleanup (elapsed > maxDurationTicks)
            SpellLingeringZoneManager.registerDimensionZoneRaw(testDim, new SpellLingeringZoneManager.ActiveSpellZone(
                    null, new net.minecraft.world.phys.Vec3(12.0D, 64.0D, 0.0D), 6.0D, 20, 1000L,
                    null, null, EssenceType.SPRING, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) {
                            SpellLingeringZoneManager.registerDimensionZoneRaw(testDim, new SpellLingeringZoneManager.ActiveSpellZone(
                                    null, zone.center, 6.0D, 5, 1025L,
                                    null, null, EssenceType.SPRING, 1.0F,
                                    (csrv, czone, celapsed) -> celapsed <= czone.maxDurationTicks
                            ));
                        }
                        return true;
                    }
            ));
            SpellLingeringZoneManager.tickDimensionZones(null, testDim, 1025L);
            reentrantZonesOk = SpellLingeringZoneManager.getActiveZoneCount(testDim) == 2;
            SpellLingeringZoneManager.tickDimensionZones(null, testDim, 1035L);
            reentrantZonesOk = reentrantZonesOk && SpellLingeringZoneManager.getActiveZoneCount(testDim) == 0;
        } catch (Throwable ex) {
            reentrantZonesOk = false;
        } finally {
            SpellLingeringZoneManager.clearDimensionZones(testDim);
        }
        check(counts, lines, "Re-entrant Lingering Zone Tick Safety (Water Whirlpool Geyser & Two-Stage Skyfall Child Spawns)",
                reentrantZonesOk);

        // 36. Prismatic (#51 — Section 6.53) Complete Refractive Spectrum Overhaul Audit
        boolean prismatic51Ok = EssenceType.PRISMATIC.isDynamic()
                && "Prismatic Oscillating Light-Disc".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.PRISMATIC, true).signatureTitle())
                && "Bifurcated Rainbow Spectrum Ray & Prismatic Meltdown".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.PRISMATIC, true).signatureTitle())
                && "Rainbow Bloom Shockwave".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.PRISMATIC, true).signatureTitle())
                && "Prismatic Crystalline Palm-Shatter".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.PRISMATIC, true).signatureTitle())
                && "Chromatic Dispersion Aura".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.PRISMATIC, true).signatureTitle())
                && "Refractive Prism-Vault Glyph".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.PRISMATIC, true).signatureTitle())
                && "Chromatic Prismatic Deluge Cone".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.PRISMATIC, true).signatureTitle())
                && "Chromatic Star-Shower".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.PRISMATIC, true).signatureTitle())
                && !SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_AGENT, EssenceType.PRISMATIC, true).signatureTitle().isBlank()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.PRISMATIC, true).armorShredPoints() == 4
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.PRISMATIC, true).armorShredPoints() == 5
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.PRISMATIC, true).armorShredPoints() == 5
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.PRISMATIC, true).armorShredPoints() == 5
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.PRISMATIC, true).armorShredPoints() == 6
                && "Prismatic Kaleidoscope Orbit Flow".equals(
                        SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.PRISMATIC, true).signatureTitle())
                && "Prismatic Refractive Phase-Step".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.PRISMATIC, true).signatureTitle())
                && "Prismatic Ore Metamorphosis Transmutation".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.PRISMATIC, true).signatureTitle())
                && "Prismatic Refractive Dyschromia / Chromatic Overload Hex".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.PRISMATIC, true).signatureTitle())
                && "Prismatic Valkyrie".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.PRISMATIC, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && !SpellLingeringZoneManager.TAG_PRISMATIC_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_PRISMATIC_ORBIT_UNTIL.isBlank();
        check(counts, lines, "Prismatic (#51 — Section 6.53): 9 Refractive Forms, Kaleidoscope Orbit, Phase-Step, Ore Metamorphosis & Diamond Archon Triad",
                prismatic51Ok);

        // 37. Empyrean (#52 — Section 6.54) Complete World-Tree Eschaton Overhaul Audit
        boolean empyrean52Ok = "World-Tree Siphoning Seed-Pod".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.EMPYREAN, true).signatureTitle())
                && "Sustained Root-Lance Sunbeam & Canopy Cataclysm".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.EMPYREAN, true).signatureTitle())
                && "World-Tree Heartwood Bastion Nova".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.EMPYREAN, true).signatureTitle())
                && "Siphoning World-Tree Root-Cleave".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.EMPYREAN, true).signatureTitle())
                && "Living Titan Bark & Canopy Mantle".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.EMPYREAN, true).signatureTitle())
                && "World-Tree Siphoning Root-Vault Sigil".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.EMPYREAN, true).signatureTitle())
                && "Solar-Verdant Spore-Web Deluge".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.EMPYREAN, true).signatureTitle())
                && "Cataclysmic World-Tree Canopy Skyfall".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.EMPYREAN, true).signatureTitle())
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.EMPYREAN, true).bonusPierceTargets() == 8
                && Math.abs(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.EMPYREAN, true).armorBypassFraction() - 0.60F) < 1.0E-4F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.EMPYREAN, true).armorShredPoints() == 2
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.EMPYREAN, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.EMPYREAN, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.EMPYREAN, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.EMPYREAN, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.EMPYREAN, true).armorShredPoints() == 7
                && "World-Tree Siphoning Root-Web Orbit Flow".equals(
                        SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.EMPYREAN, true).signatureTitle())
                && SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.EMPYREAN, true).wallPhasing()
                && "Solar Slipstream Ascent".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.EMPYREAN, true).signatureTitle())
                && "World-Tree Genesis & Vitality Consecration".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.EMPYREAN, true).signatureTitle())
                && "Radiant Empyrean Brand Hex".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.EMPYREAN, true).signatureTitle())
                && "Yggdrasil Arboreal Juggernaut".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.EMPYREAN, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Heartwood Canopy Obelisk".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.EMPYREAN, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Golden Willow Effigy".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.EMPYREAN, SpellEidolonEntity.TriadArchetype.DECOY, true, null))
                && !SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_EMPYREAN_ORBIT_UNTIL.isBlank();
        check(counts, lines, "Empyrean (#52 — Section 6.54): 9 World-Tree Eschaton Forms, Siphoning Root-Web Orbit, Slipstream Ascent, Dew Consecration & Yggdrasil Triad",
                empyrean52Ok);

        // 38. Cataclysm (#53 — Section 6.55) Complete World-Breaker Eschaton Overhaul Audit
        boolean cataclysm53Ok = "Catastrophic Hellfire Railgun Slug".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CATACLYSM, true).signatureTitle())
                && "Tectonic Magma Fissure Ray & Seismic Cataclysm".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.CATACLYSM, true).signatureTitle())
                && "World-Breaker Seismic Nova".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.CATACLYSM, true).signatureTitle())
                && "Tectonic Calamity Sunder".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.CATACLYSM, true).signatureTitle())
                && "Mantle of the World-Breaker".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.CATACLYSM, true).signatureTitle())
                && "Abyssal Calamity Caldera Sigil".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.CATACLYSM, true).signatureTitle())
                && "Cataclysmic Magma-Ash Deluge".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.CATACLYSM, true).signatureTitle())
                && "Cataclysmic World-Breaker Skyfall".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.CATACLYSM, true).signatureTitle())
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CATACLYSM, true).bonusPierceTargets() == 10
                && Math.abs(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CATACLYSM, true).armorBypassFraction() - 0.60F) < 1.0E-4F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.CATACLYSM, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.CATACLYSM, true).armorShredPoints() == 2
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.CATACLYSM, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.CATACLYSM, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.CATACLYSM, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.CATACLYSM, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.CATACLYSM, true).armorShredPoints() == 7
                && "Singularity Orbit Flow".equals(
                        SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.CATACLYSM, true).signatureTitle())
                && SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.CATACLYSM, true).wallPhasing()
                && "Gravitational Void-Warp Collapse".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.CATACLYSM, true).signatureTitle())
                && "Tectonic Subduction & Magma Vitrification".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.CATACLYSM, true).signatureTitle())
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.CATACLYSM, true).autoSmeltInherited()
                && "Abyssal World-Breaker Singularity Hex".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.CATACLYSM, true).signatureTitle())
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.CATACLYSM, true).spawnLureHex()
                && "Stygian Magma-Void Serpent".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.CATACLYSM, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Volcanic Eruption Spire".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.CATACLYSM, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Molten Core Beacon".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.CATACLYSM, SpellEidolonEntity.TriadArchetype.DECOY, true, null))
                && !SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_CATACLYSM_ORBIT_UNTIL.isBlank();
        check(counts, lines, "Cataclysm (#53 — Section 6.55): 9 World-Breaker Eschaton Forms, Singularity Orbit, Silent Void-Warp, Void Slag Subduction & Stygian Leviathan Triad",
                cataclysm53Ok);

        // 39. Terminus (#54 — Section 6.56) Complete Absolute-Zero Eschaton Overhaul Audit
        var termStrong = ddraig.net.entropica.combat.VisInteractionManager.getInstance().getStrongAgainst(EssenceType.TERMINUS);
        var termWeak = ddraig.net.entropica.combat.VisInteractionManager.getInstance().getWeakAgainst(EssenceType.TERMINUS);
        boolean terminus54Ok = termStrong.size() == 9
                && termStrong.containsAll(List.of(
                        EssenceType.NETHER, EssenceType.ARID, EssenceType.EARTH, EssenceType.WATER, EssenceType.MAGMA,
                        EssenceType.PYRE, EssenceType.BLIGHT, EssenceType.SOULFIRE, EssenceType.MIASMA))
                && termWeak.size() == 1
                && termWeak.contains(EssenceType.UNDEAD)
                && "Absolute-Zero Permafrost Lance".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TERMINUS, true).signatureTitle())
                && "Sustained Cryogenic Vortex Ray & Zero-Point Shockwave".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.TERMINUS, true).signatureTitle())
                && "Absolute-Zero Cryo-Nova".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.TERMINUS, true).signatureTitle())
                && "Absolute-Zero Glacier Cleave".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.TERMINUS, true).signatureTitle())
                && "Zero-Point Kinetic Stasis Shield".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.TERMINUS, true).signatureTitle())
                && "Permafrost Glacial Abyss Sigil".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.TERMINUS, true).signatureTitle())
                && "Cryogenic Blizzard Deluge".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.TERMINUS, true).signatureTitle())
                && "Continuous Cryo-Stasis Hailstorm".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.TERMINUS, true).signatureTitle())
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TERMINUS, true).bonusPierceTargets() == 8
                && Math.abs(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TERMINUS, true).armorBypassFraction() - 0.60F) < 1.0E-4F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.TERMINUS, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.TERMINUS, true).armorShredPoints() == 2
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.TERMINUS, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.TERMINUS, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.TERMINUS, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.TERMINUS, true).armorShredPoints() == 6
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.TERMINUS, true).armorShredPoints() == 7
                && "Cryogenic Polar Vortex Orbit Flow".equals(
                        SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.TERMINUS, true).signatureTitle())
                && SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.TERMINUS, true).wallPhasing()
                && "Glacial Slipstream Flash-Slide".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.TERMINUS, true).signatureTitle())
                && "Zero-Point Crystallization & Permafrost Synthesis".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.TERMINUS, true).signatureTitle())
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BREAK, EssenceType.TERMINUS, true).silkTouchInherited()
                && "Absolute Zero Molecular Cessation Hex".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.TERMINUS, true).signatureTitle())
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.TERMINUS, true).monsterEmpowermentHex()
                && "Titanic Glacial Elemental".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.TERMINUS, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Anchored Permafrost Elemental Spire".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.TERMINUS, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Unstable Cryogenic Elemental Effigy".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.TERMINUS, SpellEidolonEntity.TriadArchetype.DECOY, true, null))
                && !SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_TERMINUS_ELIXIR_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_TERMINUS_FLASH_SLIDE_FALL_IMMUNE_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_TERMINUS_ORBIT_UNTIL.isBlank();
        check(counts, lines, "Terminus (#54 — Section 6.56): 9 Absolute-Zero Eschaton Forms, Polar Vortex Orbit, Slipstream Flash-Slide, Glacial Frost Elixirs & Titanic Glacial Triad",
                terminus54Ok);

        // 40. Eschaton (#55 — Section 6.57) Complete Omega Singularity Overhaul Audit
        var eschStrong = ddraig.net.entropica.combat.VisInteractionManager.getInstance().getStrongAgainst(EssenceType.ESCHATON);
        var eschWeak = ddraig.net.entropica.combat.VisInteractionManager.getInstance().getWeakAgainst(EssenceType.ESCHATON);
        var eschOmegaEssence = ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.createOmegaHorizonEssenceStack(1);
        var eschStasisElixir = ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.createGravitationalStasisElixir();
        boolean eschaton55Ok = "§5".equals(EssenceType.ESCHATON.getColorCode())
                && EssenceType.ESCHATON.getColorCycle().length == 3
                && EssenceType.ESCHATON.getColorCycle()[0][0] == 11
                && EssenceType.ESCHATON.getColorCycle()[1][0] == 112
                && EssenceType.ESCHATON.getColorCycle()[2][0] == 212
                && eschStrong.containsAll(List.of(
                        EssenceType.AIR, EssenceType.WATER, EssenceType.NATURE, EssenceType.EARTH,
                        EssenceType.FROZEN, EssenceType.NETHER, EssenceType.RADIANT, EssenceType.UMBRAL,
                        EssenceType.UNDEAD, EssenceType.VOID, EssenceType.ARID, EssenceType.LIGHTNING,
                        EssenceType.AETHER, EssenceType.ENTROPIC, EssenceType.CELESTIAL, EssenceType.PRISMATIC,
                        EssenceType.EMPYREAN, EssenceType.CATACLYSM, EssenceType.TERMINUS))
                && !eschStrong.contains(EssenceType.ESCHATON)
                && !eschStrong.contains(EssenceType.APOTHEOSIS)
                && !eschStrong.contains(EssenceType.ENTROPICA)
                && eschWeak.size() == 1
                && eschWeak.contains(EssenceType.APOTHEOSIS)
                && ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isOmegaHorizonBrewingIngredient(eschOmegaEssence)
                && "ESCHATON_STASIS".equals(ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.getElixirType(eschStasisElixir))
                && ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.canBrewGravitationalStasisPotion(
                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTION))
                && !ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.canBrewGravitationalStasisPotion(eschStasisElixir)
                && "Black-Hole Railgun Dart".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ESCHATON, true).signatureTitle())
                && "Dimensional Void-Fissure Beam & Omega Collapse".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.ESCHATON, true).signatureTitle())
                && "Cosmic Event-Horizon Nova".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.ESCHATON, true).signatureTitle())
                && "Reality-Cleaving Scythe".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.ESCHATON, true).signatureTitle())
                && "Mantle of the Omega Singularity".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.ESCHATON, true).signatureTitle())
                && "Singularity Anchor Sigil".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.ESCHATON, true).signatureTitle())
                && "Accretion Singularity Torrent".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.ESCHATON, true).signatureTitle())
                && "Dimensional Inversion Starfall".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.ESCHATON, true).signatureTitle())
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ESCHATON, true).bonusPierceTargets() == 12
                && Math.abs(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ESCHATON, true).armorBypassFraction() - 0.75F) < 1.0E-4F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ESCHATON, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.ESCHATON, true).armorShredPoints() == 3
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.ESCHATON, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.ESCHATON, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.ESCHATON, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.ESCHATON, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.ESCHATON, true).armorShredPoints() == 8
                && "Event Horizon Orbit Flow".equals(
                        SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.ESCHATON, true).signatureTitle())
                && SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.ESCHATON, true).wallPhasing()
                && "Spacetime Fold Slip".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.ESCHATON, true).signatureTitle())
                && "Dimensional Event Horizon Transmutation & Accretion Compression".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.ESCHATON, true).signatureTitle())
                && "Omega Event Horizon Dissolution Hex".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ESCHATON, true).signatureTitle())
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ESCHATON, true).spawnLureHex()
                && "Event-Horizon Leviathan".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ESCHATON, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Accretion Disk Singularity Spire".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ESCHATON, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Collapsing Micro-Nova Effigy".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ESCHATON, SpellEidolonEntity.TriadArchetype.DECOY, true, null))
                && !SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_ESCHATON_ORBIT_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_ESCHATON_STASIS_ELIXIR_UNTIL.isBlank();
        check(counts, lines, "Eschaton (#55 — Section 6.57): 9 Omega Singularity Forms, Event Horizon Orbit, Silent 24m Spacetime Fold Slip, Omega Horizon Brewing & Leviathan Triad",
                eschaton55Ok);

        // 41. Apotheosis (#56 — Section 6.58) Complete Alpha Genesis Singularity Overhaul Audit
        var apoStrong = ddraig.net.entropica.combat.VisInteractionManager.getInstance().getStrongAgainst(EssenceType.APOTHEOSIS);
        var apoWeak = ddraig.net.entropica.combat.VisInteractionManager.getInstance().getWeakAgainst(EssenceType.APOTHEOSIS);
        var apoNectar = ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.createTransmutedElixirStack(
                net.minecraft.util.RandomSource.create(56L), EssenceType.APOTHEOSIS);
        boolean apotheosis56Ok = "§e".equals(EssenceType.APOTHEOSIS.getColorCode())
                && EssenceType.APOTHEOSIS.getColorCycle().length == 3
                && EssenceType.APOTHEOSIS.getColorCycle()[0][0] == 255 && EssenceType.APOTHEOSIS.getColorCycle()[0][1] == 255 && EssenceType.APOTHEOSIS.getColorCycle()[0][2] == 255
                && EssenceType.APOTHEOSIS.getColorCycle()[1][0] == 255 && EssenceType.APOTHEOSIS.getColorCycle()[1][1] == 228 && EssenceType.APOTHEOSIS.getColorCycle()[1][2] == 132
                && EssenceType.APOTHEOSIS.getColorCycle()[2][0] == 114 && EssenceType.APOTHEOSIS.getColorCycle()[2][1] == 246 && EssenceType.APOTHEOSIS.getColorCycle()[2][2] == 255
                && apoStrong.containsAll(List.of(
                        EssenceType.AIR, EssenceType.WATER, EssenceType.NATURE, EssenceType.EARTH,
                        EssenceType.FROZEN, EssenceType.NETHER, EssenceType.RADIANT, EssenceType.UMBRAL,
                        EssenceType.UNDEAD, EssenceType.VOID, EssenceType.ARID, EssenceType.LIGHTNING,
                        EssenceType.AETHER, EssenceType.ENTROPIC, EssenceType.CELESTIAL, EssenceType.PRISMATIC,
                        EssenceType.EMPYREAN, EssenceType.CATACLYSM, EssenceType.TERMINUS))
                && !apoStrong.contains(EssenceType.APOTHEOSIS)
                && !apoStrong.contains(EssenceType.ESCHATON)
                && !apoStrong.contains(EssenceType.ENTROPICA)
                && apoWeak.size() == 1
                && apoWeak.contains(EssenceType.ESCHATON)
                && "APOTHEOSIS_NECTAR".equals(ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.getElixirType(apoNectar))
                && "Seeking Crown of Dawn".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Sustained Holy White-Hole Ray & Genesis Cataclysm".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Supernova Genesis Nova".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Ascendant Solar Genesis Cleave".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Mantle of the Alpha Singularity".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "White-Hole Genesis Sanctuary Sigil".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Prismatic Solar Fan".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Cataclysmic White-Hole Genesis Starfall".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.APOTHEOSIS, true).signatureTitle())
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.APOTHEOSIS, true).bonusPierceTargets() == 10
                && Math.abs(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.APOTHEOSIS, true).armorBypassFraction() - 0.75F) < 1.0E-4F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.APOTHEOSIS, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.APOTHEOSIS, true).armorShredPoints() == 3
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.APOTHEOSIS, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.APOTHEOSIS, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.APOTHEOSIS, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.APOTHEOSIS, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.APOTHEOSIS, true).armorShredPoints() == 8
                && "White-Hole Star-Orbit Flow".equals(
                        SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.APOTHEOSIS, true).signatureTitle())
                && SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.APOTHEOSIS, true).wallPhasing()
                && "Ascendant Solar Genesis Step".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Alpha Genesis Transmutation & Universal Consecration".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.APOTHEOSIS, true).signatureTitle())
                && "Divine Genesis Brand Hex".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.APOTHEOSIS, true).signatureTitle())
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.APOTHEOSIS, true).monsterEmpowermentHex()
                && "Celestial Seraph-Titan".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.APOTHEOSIS, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "White-Hole Dawn Obelisk".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.APOTHEOSIS, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Radiant Ascendance Mirage".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.APOTHEOSIS, SpellEidolonEntity.TriadArchetype.DECOY, true, null))
                && !SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_APOTHEOSIS_ORBIT_UNTIL.isBlank();
        check(counts, lines, "Apotheosis (#56 — Section 6.58): 9 Alpha Genesis Singularity Forms, White-Hole Star-Orbit, 24m Solar Genesis Step, Nectar of the Alpha Genesis & Seraph-Titan Triad",
                apotheosis56Ok);

        // 42. Entropica (#57 — Section 6.59): Grand Cosmic Equilibrium Singularity, Universal +1 Affinity, 9 Forms, Flow, +5 Enchant Transmute, Dual Paradox Hex & Primordial Demiurge Triad
        var entStrong = ddraig.net.entropica.combat.VisInteractionManager.getInstance().getStrongAgainst(EssenceType.ENTROPICA);
        var entWeak = ddraig.net.entropica.combat.VisInteractionManager.getInstance().getWeakAgainst(EssenceType.ENTROPICA);
        boolean entStrongAgainstAllNonRegular = true;
        for (EssenceType et : EssenceType.values()) {
            if (et == EssenceType.REGULAR) {
                if (entStrong.contains(et)) {
                    entStrongAgainstAllNonRegular = false;
                    break;
                }
            } else if (!entStrong.contains(et)) {
                entStrongAgainstAllNonRegular = false;
                break;
            }
        }
        boolean entropica57Ok =
                "§b".equals(EssenceType.ENTROPICA.getColorCode())
                && EssenceType.ENTROPICA.isDynamic()
                && entStrongAgainstAllNonRegular
                && entStrong.contains(EssenceType.ESCHATON)
                && entStrong.contains(EssenceType.APOTHEOSIS)
                && entStrong.contains(EssenceType.ENTROPICA)
                && entWeak.isEmpty()
                && ddraig.net.entropica.spell.mutation.element.EntropicaSpellMutationHandler.ENTROPICA_CHRONO_SILVER_HEX_COLOR == 0xE0E5EC
                && ddraig.net.entropica.spell.mutation.element.EntropicaSpellMutationHandler.ENTROPICA_EVENT_HORIZON_INDIGO_HEX_COLOR == 0x1A153B
                && ddraig.net.entropica.spell.mutation.element.EntropicaSpellMutationHandler.ENTROPICA_GENESIS_GOLD_HEX_COLOR == 0xFAD02C
                && "Singularity Equilibrium Lance".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPICA, true).signatureTitle())
                && "Twin Helix Singularity Stream & Annihilation Blast".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.ENTROPICA, true).signatureTitle())
                && "Cosmic Equilibrium Nova".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.ENTROPICA, true).signatureTitle())
                && "Blade of the Grand Paradox".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.ENTROPICA, true).signatureTitle())
                && "Mantle of Entropica".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.AURA, EssenceType.ENTROPICA, true).signatureTitle())
                && "Singularity Horizon Mandala".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.ENTROPICA, true).signatureTitle())
                && "Thermodynamic Heat-Death Wave".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.ENTROPICA, true).signatureTitle())
                && "Cataclysmic Singularity Equilibrium Starfall".equals(
                        SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.ENTROPICA, true).signatureTitle())
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPICA, true).bonusPierceTargets() == 12
                && Math.abs(SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPICA, true).armorBypassFraction() - 0.75F) < 1.0E-4F
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.ENTROPICA, true).wallPhasing()
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.ENTROPICA, true).armorShredPoints() == 3
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.RADIAL_BURST, EssenceType.ENTROPICA, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.TOUCH_STRIKE, EssenceType.ENTROPICA, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.ENTROPICA, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.CONE, EssenceType.ENTROPICA, true).armorShredPoints() == 8
                && SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.ENTROPICA, true).armorShredPoints() == 8
                && Math.abs(SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.HARM, EssenceType.ENTROPICA, true).lifestealFraction() - 0.50F) < 1.0E-4F
                && "Cosmic Parallax Singularity Orbit Flow".equals(
                        SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.ENTROPICA, true).signatureTitle())
                && SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.ENTROPICA, true).wallPhasing()
                && "Parallax Event-Horizon Fold".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.BLINK, EssenceType.ENTROPICA, true).signatureTitle())
                && "Grand Cosmic Equilibrium Transmutation".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.ENTROPICA, true).signatureTitle())
                && "Grand Equilibrium Paradox Hex".equals(
                        SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ENTROPICA, true).signatureTitle())
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ENTROPICA, true).spawnLureHex()
                && SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.ENTROPICA, true).monsterEmpowermentHex()
                && "Avatar of Entropica".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.AGENT, true, null))
                && "Altar of the Horizon".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.SENTRY, true, null))
                && "Fractured Paradox Core".equals(SpellEidolonSpecs.resolveCanonicalConstructName(
                        EssenceType.ENTROPICA, SpellEidolonEntity.TriadArchetype.DECOY, true, null))
                && !SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL.isBlank()
                && !SpellLingeringZoneManager.TAG_ENTROPICA_ORBIT_UNTIL.isBlank();
        check(counts, lines, "Entropica (#57 — Section 6.59): 9 Grand Cosmic Equilibrium Forms, Parallax Orbit, 24m Fold, +5 Enchant Transmute, Dual Paradox Hex & Demiurge Triad",
                entropica57Ok);

        // 43. Defender Parent-Tree Lineage Expansion & Mutual Volatile Clash (+1 Both Ways) Audit (S1/S2, A1 & A3)
        var advMgr = ddraig.net.entropica.combat.VisInteractionManager.getInstance();
        var pyreLineage = advMgr.getExpandedLineage(EssenceType.PYRE);
        var aetherLineage = advMgr.getExpandedLineage(EssenceType.AETHER);
        var equinoxLineage = advMgr.getExpandedLineage(EssenceType.EQUINOX);
        boolean defenderLineageAndClashesOk =
                pyreLineage.containsAll(List.of(EssenceType.PYRE, EssenceType.EARTH, EssenceType.NETHER, EssenceType.NATURE))
                && aetherLineage.containsAll(List.of(EssenceType.AETHER, EssenceType.AIR, EssenceType.EARTH, EssenceType.WATER, EssenceType.NETHER, EssenceType.CHIMERA))
                && equinoxLineage.containsAll(List.of(EssenceType.EQUINOX, EssenceType.RADIANT_FIRE, EssenceType.UMBRAL_WATER, EssenceType.RADIANT, EssenceType.NETHER, EssenceType.UMBRAL, EssenceType.WATER))
                // Defender lineage stacking: Water -> Magma [MAGMA(+1), EARTH(0), NETHER(+1)] = +2 (2.0x); Air -> Pyre [PYRE(+1), EARTH(+1)] = +2 (2.0x); Frozen -> Magma = -2 (0.25x)
                && advMgr.calculateLineageAdvantageScore(EssenceType.WATER, List.of(EssenceType.MAGMA)) == 2
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.WATER, List.of(EssenceType.MAGMA)) - 2.0F) < 1.0E-4F
                && advMgr.calculateLineageAdvantageScore(EssenceType.AIR, List.of(EssenceType.PYRE)) == 2
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.AIR, List.of(EssenceType.PYRE)) - 2.0F) < 1.0E-4F
                && advMgr.calculateLineageAdvantageScore(EssenceType.FROZEN, List.of(EssenceType.MAGMA)) == -2
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.FROZEN, List.of(EssenceType.MAGMA)) - 0.25F) < 1.0E-4F
                // Mutual volatile clashes deal +1 both ways instead of cancelling to 0
                && advMgr.getSingleNodeAdvantage(EssenceType.WATER, EssenceType.ARID) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.ARID, EssenceType.WATER) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.GLACIAL, EssenceType.NETHER) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.NETHER, EssenceType.GLACIAL) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.VAPOR, EssenceType.ARID) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.ARID, EssenceType.VAPOR) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.NULL_R, EssenceType.UMBRAL) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.UMBRAL, EssenceType.NULL_R) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.AETHER, EssenceType.VOID) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.VOID, EssenceType.AETHER) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.ENTROPIC, EssenceType.RADIANT) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.RADIANT, EssenceType.ENTROPIC) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.LIGHTNING, EssenceType.WATER) == 1
                && advMgr.getSingleNodeAdvantage(EssenceType.WATER, EssenceType.LIGHTNING) == 1;
        check(counts, lines, "Defender Parent-Tree Lineage Multiplication (+2->2.0x / -2->0.25x) & Mutual Volatile Clashes (+1 Both Ways)",
                defenderLineageAndClashesOk);

        // 44. Universal Scale, Mob->Player Survival Bounds, Tier 5 Divine Override (4.0x) & Tier 6 Absolute Overrides (0.0x / 5.0x) (A2)
        boolean overridesAndUniversalScaleOk =
                // Mob->Player bounds: 3.2x standard cap (2.5x Omega), 0.10x standard floor (0.0x Alpha)
                Math.abs(advMgr.calculateDamageMultiplier(EssenceType.WATER, List.of(EssenceType.NETHER), true, 8.0F, false, false) - 3.2F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.WATER, List.of(EssenceType.NETHER), true, 8.0F, false, true) - 2.5F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.FROZEN, List.of(EssenceType.MAGMA), true, -3.0F, false, false) - 0.10F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.FROZEN, List.of(EssenceType.MAGMA), false, -2.0F, false, false) - 0.0625F) < 1.0E-4F
                // Tier 5 Divine Override = 4.0x directly (even on Mob->Player!)
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.UMBRAL, List.of(EssenceType.EMPYREAN), false, 0.0F) - 4.0F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.UMBRAL, List.of(EssenceType.EMPYREAN), true, 0.0F) - 4.0F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.VITAE, List.of(EssenceType.CATACLYSM), false, 0.0F) - 4.0F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.UNDEAD, List.of(EssenceType.TERMINUS), false, 0.0F) - 4.0F) < 1.0E-4F
                // Tier 5 non-vulnerability dilution: sub-parents do not leak +1 weakness against non-override attackers
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.NETHER, List.of(EssenceType.EMPYREAN), false, 0.0F) - 0.5F) < 1.0E-4F
                // Tier 6 Absolute Immunity vs Tiers 1-5 = 0.0x (bypassing 0.10x player floor!)
                && advMgr.calculateDamageMultiplier(EssenceType.AIR, List.of(EssenceType.ESCHATON), true, 0.0F) == 0.0F
                && advMgr.calculateDamageMultiplier(EssenceType.PYRE, List.of(EssenceType.APOTHEOSIS), true, 0.0F) == 0.0F
                && advMgr.calculateDamageMultiplier(EssenceType.EMPYREAN, List.of(EssenceType.ESCHATON), false, 0.0F) == 0.0F
                && advMgr.calculateDamageMultiplier(EssenceType.CATACLYSM, List.of(EssenceType.APOTHEOSIS), false, 0.0F) == 0.0F
                && advMgr.calculateDamageMultiplier(EssenceType.TERMINUS, List.of(EssenceType.ENTROPICA), false, 0.0F) == 0.0F
                // Tier 6 Mirror Clash (Eschaton <-> Apotheosis) & Entropica -> T6 = 5.0x
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.ESCHATON, List.of(EssenceType.APOTHEOSIS), true, 0.0F) - 5.0F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.APOTHEOSIS, List.of(EssenceType.ESCHATON), true, 0.0F) - 5.0F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.ENTROPICA, List.of(EssenceType.ESCHATON), true, 0.0F) - 5.0F) < 1.0E-4F
                && Math.abs(advMgr.calculateDamageMultiplier(EssenceType.ENTROPICA, List.of(EssenceType.APOTHEOSIS), true, 0.0F) - 5.0F) < 1.0E-4F
                // Entropica True Absolute Immunity defense = 0.0x even against Eschaton, Apotheosis & Entropica
                && advMgr.calculateDamageMultiplier(EssenceType.ESCHATON, List.of(EssenceType.ENTROPICA), true, 0.0F) == 0.0F
                && advMgr.calculateDamageMultiplier(EssenceType.APOTHEOSIS, List.of(EssenceType.ENTROPICA), true, 0.0F) == 0.0F
                && advMgr.calculateDamageMultiplier(EssenceType.ENTROPICA, List.of(EssenceType.ENTROPICA), true, 0.0F) == 0.0F;
        check(counts, lines, "Universal Scale, Mob->Player [0.10x, 3.2x] Bounds, Tier 5 Divine 4.0x & Tier 6 Singularity 0.0x / 5.0x Overrides",
                overridesAndUniversalScaleOk);

        // 45. Caprine Secret (RADIANT_FIRE, UMBRAL_WATER, EQUINOX) & Ethereal (AUREOLE, VESPER) Handlers + Chimera Matchups (S1 & A3)
        List<EssenceType> caprineAndEtherealExtras = List.of(
                EssenceType.RADIANT_FIRE, EssenceType.UMBRAL_WATER, EssenceType.EQUINOX,
                EssenceType.AUREOLE, EssenceType.VESPER
        );
        boolean caprineHandlersOk = SpellMutationHandlerRegistry.getAllRegistered().size() == 63;
        for (EssenceType secType : caprineAndEtherealExtras) {
            var activeH = SpellMutationHandlerRegistry.resolveActive(secType, true);
            var offH = SpellMutationHandlerRegistry.resolveActive(secType, false);
            if (activeH == null || activeH.materiaType() != secType || offH != null) {
                caprineHandlersOk = false;
                break;
            }
            for (SpellDeliveryForm form : SpellDeliveryForm.values()) {
                var mf = SpellMutationRegistry.resolveForm(form, secType, true);
                var uf = SpellMutationRegistry.resolveForm(form, secType, false);
                if (!mf.mutated() || mf.signatureTitle().isBlank() || !uf.isUnmutated()) {
                    caprineHandlersOk = false;
                    break;
                }
            }
            for (SpellCoreEffect eff : List.of(SpellCoreEffect.BLINK, SpellCoreEffect.TRANSMUTE, SpellCoreEffect.AFFLICTION)) {
                var me = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, secType, true);
                var ue = SpellMutationRegistry.resolveEffect(SpellPartSlotType.CORE_EFFECT, eff, secType, false);
                if (!me.mutated() || me.signatureTitle().isBlank() || !ue.isUnmutated()) {
                    caprineHandlersOk = false;
                    break;
                }
            }
        }
        boolean caprineAndChimeraRulesOk = caprineHandlersOk
                && ddraig.net.entropica.spell.mutation.MateriaFamilyTier.of(EssenceType.RADIANT_FIRE)
                        == ddraig.net.entropica.spell.mutation.MateriaFamilyTier.SECRET_FUSION
                && Math.abs(ddraig.net.entropica.spell.mutation.MateriaFamilyTier.SECRET_FUSION.getBaseArmorBypass() - 0.35F) < 1.0E-4F
                && ddraig.net.entropica.spell.mutation.MateriaFamilyTier.SECRET_FUSION.getBaseArmorShred() == 3
                && advMgr.getStrongAgainst(EssenceType.CHIMERA).containsAll(List.of(
                        EssenceType.UNDEAD, EssenceType.BLIGHT, EssenceType.SOULFIRE, EssenceType.BLOOD))
                && advMgr.getWeakAgainst(EssenceType.CHIMERA).contains(EssenceType.UMBRAL)
                && advMgr.getStrongAgainst(EssenceType.RADIANT_FIRE).containsAll(List.of(
                        EssenceType.UNDEAD, EssenceType.FROZEN, EssenceType.NATURE))
                && advMgr.getWeakAgainst(EssenceType.RADIANT_FIRE).containsAll(List.of(EssenceType.UMBRAL, EssenceType.WATER))
                && advMgr.getStrongAgainst(EssenceType.UMBRAL_WATER).containsAll(List.of(
                        EssenceType.RADIANT, EssenceType.NATURE, EssenceType.NETHER))
                && advMgr.getWeakAgainst(EssenceType.UMBRAL_WATER).containsAll(List.of(EssenceType.VOID, EssenceType.FROZEN))
                && advMgr.getStrongAgainst(EssenceType.EQUINOX).containsAll(List.of(
                        EssenceType.UNDEAD, EssenceType.FROZEN, EssenceType.NATURE, EssenceType.NETHER, EssenceType.RADIANT))
                && advMgr.getWeakAgainst(EssenceType.EQUINOX).containsAll(List.of(EssenceType.VOID, EssenceType.UMBRAL))
                && advMgr.getStrongAgainst(EssenceType.ENTROPIC).containsAll(List.of(EssenceType.AEGIS, EssenceType.AURORA))
                && advMgr.getWeakAgainst(EssenceType.ENTROPIC).containsAll(List.of(EssenceType.NETHER, EssenceType.UMBRAL));
        check(counts, lines, "Caprine Secret & Extra Ethereal Handlers (63 Total) + Chimera / Entropic Canonical Outgoing Matchups",
                caprineAndChimeraRulesOk);

        // 46. FAST_DART Strict 0-Pierce Rule, ORBIT_SATELLITE Flow Gating & Persistent Orbit Duration (S7 & A5)
        boolean zeroFlowPierceOk = true;
        for (EssenceType et : SpellMutationHandlerRegistry.getAllRegistered().keySet()) {
            for (SpellFlowModifier fl : SpellFlowModifier.values()) {
                if (SpellMutationRegistry.resolveFlow(fl, et, true).bonusPierceTargets() != 0) {
                    zeroFlowPierceOk = false;
                    break;
                }
            }
            if (!zeroFlowPierceOk) break;
        }
        List<EssenceType> orbitSignatureTypes = List.of(
                EssenceType.PYRE, EssenceType.PENUMBRA, EssenceType.RIME, EssenceType.SPRING,
                EssenceType.STATIC, EssenceType.SYLVAN, EssenceType.GENESIS,
                EssenceType.AETHER, EssenceType.CELESTIAL, EssenceType.PRISMATIC,
                EssenceType.EMPYREAN, EssenceType.CATACLYSM, EssenceType.TERMINUS,
                EssenceType.ESCHATON, EssenceType.APOTHEOSIS, EssenceType.ENTROPICA
        );
        boolean orbitFlowGatedOk = true;
        for (EssenceType ot : orbitSignatureTypes) {
            String satTitle = SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, ot, true).signatureTitle();
            String singleTitle = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SINGLE, ot, true).signatureTitle();
            String dartTitle = SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, ot, true).signatureTitle();
            boolean satIsOrbitalSignature = satTitle.contains("Orbit") || satTitle.contains("Spiral");
            boolean nonOrbitClean = !singleTitle.equals(satTitle)
                    && !dartTitle.equals(satTitle)
                    && !singleTitle.contains("Orbit")
                    && !dartTitle.contains("Orbit")
                    && SpellMutationRegistry.isHandlerFlowMutationApplicable(ot, SpellFlowModifier.ORBIT_SATELLITE)
                    && !SpellMutationRegistry.isHandlerFlowMutationApplicable(ot, SpellFlowModifier.SINGLE)
                    && !SpellMutationRegistry.isHandlerFlowMutationApplicable(ot, SpellFlowModifier.FAST_DART);
            if (!satIsOrbitalSignature || !nonOrbitClean) {
                orbitFlowGatedOk = false;
                break;
            }
        }
        boolean persistentOrbitTicksOk =
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.resolveCanonicalOrbitDurationTicks(1) == 180
                && ddraig.net.entropica.entity.projectile.SpellProjectileEntity.resolveCanonicalOrbitDurationTicks(10) == 360;
        check(counts, lines, "Flow Strict 0-Pierce Rule (FAST_DART), ORBIT_SATELLITE Title/Hook Gating & Persistent Orbit Duration (180-360t)",
                zeroFlowPierceOk && orbitFlowGatedOk && persistentOrbitTicksOk);

        // 47. Eidolon PYRE (#39) Lifecycle Refresh, Tier 8-10 Summon Cap (8) & 15s (300t) 25%/100% Fuel Upkeep (S10 & A4)
        long t4BaseMfum = MateriaStageTable.getBaseMfumCastCost(4);
        boolean eidolonUpkeepAndCapOk =
                SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.PYRE)
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.PYRE, SpellEidolonEntity.TriadArchetype.AGENT) == 70.0D
                && SpellEidolonSpecs.resolveCanonicalLifecycleBaseHp(EssenceType.PYRE, SpellEidolonEntity.TriadArchetype.SENTRY) == 85.0D
                && SpellEidolonSpecs.UPKEEP_INTERVAL_TICKS == 300
                && SpellEidolonSpecs.resolveUpkeepCostMfum(4, false) == Math.max(1L, t4BaseMfum / 4L)
                && SpellEidolonSpecs.resolveUpkeepCostMfum(4, true) == t4BaseMfum
                && SpellEidolonSpecs.resolveMaxConcurrentSummons(1) == 1
                && SpellEidolonSpecs.resolveMaxConcurrentSummons(2) == 2
                && SpellEidolonSpecs.resolveMaxConcurrentSummons(5) == 4
                && SpellEidolonSpecs.resolveMaxConcurrentSummons(7) == 6
                && SpellEidolonSpecs.resolveMaxConcurrentSummons(8) == 8
                && SpellEidolonSpecs.resolveMaxConcurrentSummons(10) == 8;
        check(counts, lines, "Eidolon Pyre (#39) Lifecycle Refresh, Tier 8-10 Cap (8) & 15s (300t) 25%/100% Materia Upkeep Scaling",
                eidolonUpkeepAndCapOk);

        // 48. 2.5s (50-Tick) Continuous Held TRANSMUTE & Held Stream CONE Channel Gating (S4, S5, S8, A4 & A5)
        SpellAssembly transmuteHoldSpell = new SpellAssembly(
                "Held Rime Transmute",
                List.of(),
                true,
                SpellPrefix.UTILITY,
                6,
                new FlowPartBinding(SpellFlowModifier.SINGLE, EssenceType.RIME, true, 1, 0.0F),
                List.of(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, EssenceType.RIME, true),
                        EffectPartBinding.of(SpellCoreEffect.TRANSMUTE, EssenceType.RIME, true)
                )),
                SpellFlawProfile.PERFECT,
                0
        );
        SpellAssembly streamConeSpell = new SpellAssembly(
                "Channeled Stream Cone",
                List.of(),
                true,
                SpellPrefix.COMBAT,
                5,
                new FlowPartBinding(SpellFlowModifier.SINGLE, EssenceType.NETHER, true, 1, 0.0F),
                List.of(SubSpellSlot.of(
                        new FormPartBinding(SpellDeliveryForm.CONE, EssenceType.NETHER, true, "stream"),
                        EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.NETHER, true)
                )),
                SpellFlawProfile.PERFECT,
                0
        );
        SpellAssembly fanConeSpell = new SpellAssembly(
                "Instant Fan Cone",
                List.of(),
                true,
                SpellPrefix.COMBAT,
                5,
                new FlowPartBinding(SpellFlowModifier.SINGLE, EssenceType.NETHER, true, 1, 0.0F),
                List.of(SubSpellSlot.of(
                        new FormPartBinding(SpellDeliveryForm.CONE, EssenceType.NETHER, true, "fan"),
                        EffectPartBinding.of(SpellCoreEffect.HARM, EssenceType.NETHER, true)
                )),
                SpellFlawProfile.PERFECT,
                0
        );
        boolean channelGatesOk =
                ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.TRANSMUTE_CHANNEL_DURATION_TICKS == 50
                && !ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isTransmuteChannelSatisfied(null, null)
                && transmuteHoldSpell.hasTransmuteVerb()
                && !transmuteHoldSpell.isChanneledHoldBeam()
                && transmuteHoldSpell.requiresContinuousHoldUse()
                && SpellDeliveryForm.CONE.isChanneled("stream")
                && !SpellDeliveryForm.CONE.isChanneled("fan")
                && streamConeSpell.isChanneledHoldCone()
                && streamConeSpell.requiresContinuousHoldUse()
                && !fanConeSpell.isChanneledHoldCone()
                && !fanConeSpell.requiresContinuousHoldUse();
        check(counts, lines, "2.5s (50-Tick) Held Right-Click TRANSMUTE Gate & Held Channeled Stream vs Fan CONE Sub-Modes",
                channelGatesOk);

        // 49. Boss Hex Level I->III Stacking, Post-Expiry 10s (200t) Immunity, Death Contagion Gate & Max-HP Cut Flags (S9 & A6)
        net.minecraft.nbt.CompoundTag bossHexData = new net.minecraft.nbt.CompoundTag();
        boolean bossNotImmuneOnCast = !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isBossImmuneToHex(bossHexData, 1000L, EssenceType.OBLIVION);
        int bossStack1 = ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.computeNextHexStackLevel(bossHexData, 1000L, EssenceType.OBLIVION, true);
        bossHexData.putString(ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_HEX_TYPE, EssenceType.OBLIVION.name());
        bossHexData.putInt(ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_HEX_STACK, bossStack1);
        bossHexData.putLong(ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_HEX_UNTIL, 1600L); // 30s (600t) boss window
        int bossStack2 = ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.computeNextHexStackLevel(bossHexData, 1200L, EssenceType.OBLIVION, true);
        bossHexData.putInt(ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_HEX_STACK, bossStack2);
        int bossStack3 = ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.computeNextHexStackLevel(bossHexData, 1400L, EssenceType.OBLIVION, true);
        bossHexData.putInt(ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_HEX_STACK, bossStack3);
        int contagionAlwaysLevelOne = ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.computeNextHexStackLevel(bossHexData, 1450L, EssenceType.OBLIVION, false);
        ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.stampBossPostHexImmunity(bossHexData, 1600L, EssenceType.OBLIVION.name(), 1600L);
        boolean bossImmuneDuringPostWindow = ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isBossImmuneToHex(bossHexData, 1799L, EssenceType.OBLIVION)
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isBossImmuneToHex(bossHexData, 1799L, EssenceType.FROZEN)
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isBossImmuneToHex(bossHexData, 1800L, EssenceType.OBLIVION);

        net.minecraft.nbt.CompoundTag dyingData = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.CompoundTag casterData = new net.minecraft.nbt.CompoundTag();
        dyingData.putString(ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_HEX_TYPE, EssenceType.UNDEAD.name());
        dyingData.putLong(ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_HEX_UNTIL, 999999L);
        dyingData.putLong(ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_HEX_INITIAL_WINDOW, 2200L);
        boolean spreadsInWindow = ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.canSpreadHexOnDeath(dyingData, casterData, 2150L);
        boolean blockedOutsideWindow = !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.canSpreadHexOnDeath(dyingData, casterData, 2500L);
        casterData.putLong(SpellLingeringZoneManager.TAG_ACTIVE_SPELL_CHANNEL_UNTIL, 2520L);
        boolean spreadsWhileChanneled = ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.canSpreadHexOnDeath(dyingData, casterData, 2500L);

        boolean maxHpCutFlagsOk =
                ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.usesHexMaxHealthCut(EssenceType.MIASMA, true)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.usesHexMaxHealthCut(EssenceType.GENESIS, true)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.usesHexMaxHealthCut(EssenceType.OBLIVION, true)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.usesHexMaxHealthCut(EssenceType.CHIMERA, true)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.usesHexMaxHealthCut(EssenceType.ENTROPIC, true)
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.usesHexMaxHealthCut(EssenceType.ENTROPIC, false)
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.usesHexMaxHealthCut(EssenceType.NETHER, true);

        check(counts, lines, "Boss Hex Level I->III Stacking, Post-Expiry 10s Immunity, Active-Channel Contagion Gate & Max-HP Cut Cleanup",
                bossNotImmuneOnCast && bossStack1 == 1 && bossStack2 == 2 && bossStack3 == 3
                        && contagionAlwaysLevelOne == 1 && bossImmuneDuringPostWindow
                        && spreadsInWindow && blockedOutsideWindow && spreadsWhileChanneled && maxHpCutFlagsOk);

        // 50. Air (#1) Section 6.2 Conformance: Beam 8-Pierce, Deliberate Slow Flow Exemption, Flow Titles & Zephyr Constructs
        SpellMutationProfile airBeamMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.AIR, true);
        SpellMutationProfile airBeamOff = SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.AIR, false);
        SpellMutationProfile airProjMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.AIR, true);
        SpellMutationProfile airSkyfallMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.AIR, true);
        SpellMutationProfile airSentryMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.AIR, true);

        SpellMutationProfile airSlowFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SLOW_HEAVY, EssenceType.AIR, true);
        SpellMutationProfile airSingleFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SINGLE, EssenceType.AIR, true);
        SpellMutationProfile airFastFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.AIR, true);
        SpellMutationProfile airChainFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.AIR, true);
        SpellMutationProfile airOrbitFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.AIR, true);

        boolean airPierceAndArchetypeOk =
                airBeamMut.bonusPierceTargets() == 8
                && airBeamOff.bonusPierceTargets() == 0
                && airProjMut.bonusPierceTargets() == 0
                && airProjMut.velocityMultiplier() == 1.25F
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.AIR, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_WIND_SPIRAL;

        boolean airSlowAndFlowTitlesOk =
                !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.AIR, SpellFlowModifier.SLOW_HEAVY)
                && SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.AIR, SpellFlowModifier.SINGLE)
                && SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.AIR, SpellFlowModifier.CHAIN_LEAP)
                && SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.AIR, SpellFlowModifier.ORBIT_SATELLITE)
                && airSlowFlow.velocityMultiplier() == 1.0F
                && airSlowFlow.bonusPierceTargets() == 0
                && SpellFlowModifier.SLOW_HEAVY.getDisplayName().equals(airSlowFlow.signatureTitle())
                && airSingleFlow.velocityMultiplier() == 1.35F
                && "Aerodynamic Slipstream Flow".equals(airSingleFlow.signatureTitle())
                && airFastFlow.velocityMultiplier() == 1.35F
                && airFastFlow.bonusPierceTargets() == 0
                && airChainFlow.velocityMultiplier() == 1.35F
                && "Sonic Leap Chains".equals(airChainFlow.signatureTitle())
                && airOrbitFlow.velocityMultiplier() == 1.35F
                && "Zephyr Gale Orbit".equals(airOrbitFlow.signatureTitle());

        boolean airConstructAndZoneInvariantsOk =
                ddraig.net.entropica.spell.mutation.element.AirSpellMutationHandler.ZEPHYR_EIDOLON_HEX_COLOR == 0xA88654
                && SpellEidolonSpecs.isPureAuraOrFieldSentry(EssenceType.AIR)
                && "Zephyr Eidolon".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.AIR, SpellEidolonEntity.TriadArchetype.AGENT))
                && "Zephyr Updraft Sentry".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.AIR, SpellEidolonEntity.TriadArchetype.SENTRY))
                && "Zephyr Cyclone Decoy".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.AIR, SpellEidolonEntity.TriadArchetype.DECOY))
                && airSentryMut.mutationDescription() != null && airSentryMut.mutationDescription().contains("updraft gusts")
                && airSkyfallMut.mutationDescription() != null && airSkyfallMut.mutationDescription().contains("9s") && airSkyfallMut.mutationDescription().contains("45s")
                && !SpellLingeringZoneManager.TAG_AIR_TRAMPOLINE_CUSHION_UNTIL.isBlank()
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.TAG_AIR_HYPOXIA_MELEE_CD_UNTIL.isBlank();

        check(counts, lines, "Air (#1) Sec 6.2 Invariants: Beam 8-Pierce, Deliberate Slow 1.0x Exemption, Flow Titles & Zephyr Constructs",
                airPierceAndArchetypeOk && airSlowAndFlowTitlesOk && airConstructAndZoneInvariantsOk);

        // 51. Water (#2) Section 6.3 Conformance: Flow Gating (SLOW_HEAVY & CHAIN_LEAP Only), Water <-> Arid +1 Clash & Aqueous Constructs
        var visMgr = ddraig.net.entropica.combat.VisInteractionManager.getInstance();
        boolean waterAridClashOk =
                visMgr.getSingleNodeAdvantage(EssenceType.WATER, EssenceType.ARID) == 1
                && visMgr.getSingleNodeAdvantage(EssenceType.ARID, EssenceType.WATER) == 1
                && visMgr.calculateLineageAdvantageScore(EssenceType.WATER, List.of(EssenceType.ARID)) == 1
                && visMgr.calculateLineageAdvantageScore(EssenceType.ARID, List.of(EssenceType.WATER)) == 1
                && visMgr.getSingleNodeAdvantage(EssenceType.WATER, EssenceType.NETHER) == 1
                && visMgr.getSingleNodeAdvantage(EssenceType.WATER, EssenceType.FROZEN) == -1
                && visMgr.getSingleNodeAdvantage(EssenceType.WATER, EssenceType.NATURE) == -1;

        SpellMutationProfile waterSlowFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SLOW_HEAVY, EssenceType.WATER, true);
        SpellMutationProfile waterChainFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.WATER, true);
        SpellMutationProfile waterSingleFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SINGLE, EssenceType.WATER, true);
        SpellMutationProfile waterFastFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.WATER, true);
        SpellMutationProfile waterOrbitFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.WATER, true);

        boolean waterFlowGatingOk =
                SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.SLOW_HEAVY)
                && SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.CHAIN_LEAP)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.SINGLE)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.FAST_DART)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.DOUBLE_BIFURCATE)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.TRIPLE_TRIFURCATE)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.OCTUPLE_DOME)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.ORBIT_SATELLITE)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.WATER, SpellFlowModifier.VOID_WARP)
                && "Viscous Fluid Mass".equals(waterSlowFlow.signatureTitle())
                && waterSlowFlow.mutationDescription() != null
                && "Riptide Undertow Chains".equals(waterChainFlow.signatureTitle())
                && waterChainFlow.mutationDescription() != null
                && ("Riptide Torrent " + SpellFlowModifier.SINGLE.getDisplayName()).equals(waterSingleFlow.signatureTitle())
                && waterSingleFlow.mutationDescription() == null
                && ("Riptide Torrent " + SpellFlowModifier.FAST_DART.getDisplayName()).equals(waterFastFlow.signatureTitle())
                && waterFastFlow.mutationDescription() == null
                && ("Riptide Torrent " + SpellFlowModifier.ORBIT_SATELLITE.getDisplayName()).equals(waterOrbitFlow.signatureTitle())
                && waterOrbitFlow.mutationDescription() == null
                && waterSlowFlow.bonusPierceTargets() == 0
                && waterFastFlow.bonusPierceTargets() == 0;

        SpellMutationProfile waterBeamMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.WATER, true);
        SpellMutationProfile waterProjMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.WATER, true);
        SpellMutationProfile waterSkyfallMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.WATER, true);
        SpellMutationProfile waterSentryMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.WATER, true);
        SpellMutationProfile waterDecoyMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, EssenceType.WATER, true);

        boolean waterConstructsAndFormsOk =
                waterBeamMut.bonusPierceTargets() == 8
                && waterProjMut.bonusPierceTargets() == 0
                && ddraig.net.entropica.spell.mutation.element.WaterSpellMutationHandler.AQUEOUS_EIDOLON_HEX_COLOR == 0x225C7C
                && !ddraig.net.entropica.spell.mutation.element.WaterSpellMutationHandler.isTrueNetherOrFireEntity(null)
                && "Aqueous Eidolon".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.WATER, SpellEidolonEntity.TriadArchetype.AGENT))
                && "Aqueous Orb Sentry".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.WATER, SpellEidolonEntity.TriadArchetype.SENTRY))
                && "Aqueous Mirage Decoy".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.WATER, SpellEidolonEntity.TriadArchetype.DECOY))
                && waterSkyfallMut.mutationDescription() != null
                && waterSkyfallMut.mutationDescription().contains("8s") && waterSkyfallMut.mutationDescription().contains("36s")
                && waterSentryMut.mutationDescription() != null && waterSentryMut.mutationDescription().contains("passively dousing")
                && waterDecoyMut.mutationDescription() != null && waterDecoyMut.mutationDescription().contains("3.5s")
                && !SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL.isBlank();

        check(counts, lines, "Water (#2) Sec 6.3 Invariants: Flow Gating (SLOW_HEAVY & CHAIN_LEAP), Water <-> Arid +1 Clash & Aqueous Constructs",
                waterAridClashOk && waterFlowGatingOk && waterConstructsAndFormsOk);

        // 52. Nature (#3) Section 6.4 Conformance: Beam 8-Pierce, CHAIN_LEAP-Only Flow Gating, Advantage & Sylvan Constructs
        boolean natureAdvantageOk =
                visMgr.getSingleNodeAdvantage(EssenceType.NATURE, EssenceType.WATER) == 1
                && visMgr.getSingleNodeAdvantage(EssenceType.NATURE, EssenceType.EARTH) == 1
                && visMgr.getSingleNodeAdvantage(EssenceType.NATURE, EssenceType.NETHER) == -1
                && visMgr.getSingleNodeAdvantage(EssenceType.NATURE, EssenceType.FROZEN) == -1
                && visMgr.getSingleNodeAdvantage(EssenceType.NATURE, EssenceType.UMBRAL) == -1;

        SpellMutationProfile natureChainFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.NATURE, true);
        SpellMutationProfile natureSingleFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SINGLE, EssenceType.NATURE, true);
        SpellMutationProfile natureFastFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.NATURE, true);
        SpellMutationProfile natureSlowFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SLOW_HEAVY, EssenceType.NATURE, true);
        SpellMutationProfile natureOrbitFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.ORBIT_SATELLITE, EssenceType.NATURE, true);

        boolean natureFlowGatingOk =
                SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.CHAIN_LEAP)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.SINGLE)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.FAST_DART)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.SLOW_HEAVY)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.DOUBLE_BIFURCATE)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.TRIPLE_TRIFURCATE)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.OCTUPLE_DOME)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.ORBIT_SATELLITE)
                && !SpellMutationRegistry.isHandlerFlowMutationApplicable(EssenceType.NATURE, SpellFlowModifier.VOID_WARP)
                && "Entangling Briar Chains".equals(natureChainFlow.signatureTitle())
                && natureChainFlow.mutationDescription() != null
                && ("Verdant Briar " + SpellFlowModifier.SINGLE.getDisplayName()).equals(natureSingleFlow.signatureTitle())
                && natureSingleFlow.mutationDescription() == null
                && ("Verdant Briar " + SpellFlowModifier.FAST_DART.getDisplayName()).equals(natureFastFlow.signatureTitle())
                && natureFastFlow.mutationDescription() == null
                && ("Verdant Briar " + SpellFlowModifier.SLOW_HEAVY.getDisplayName()).equals(natureSlowFlow.signatureTitle())
                && natureSlowFlow.mutationDescription() == null
                && ("Verdant Briar " + SpellFlowModifier.ORBIT_SATELLITE.getDisplayName()).equals(natureOrbitFlow.signatureTitle())
                && natureOrbitFlow.mutationDescription() == null
                && natureChainFlow.bonusPierceTargets() == 0
                && natureFastFlow.bonusPierceTargets() == 0;

        SpellMutationProfile natureBeamMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.BEAM, EssenceType.NATURE, true);
        SpellMutationProfile natureProjMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.NATURE, true);
        SpellMutationProfile natureSkyfallMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SKYFALL, EssenceType.NATURE, true);
        SpellMutationProfile natureSentryMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_SENTRY, EssenceType.NATURE, true);
        SpellMutationProfile natureDecoyMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.SUMMON_DECOY, EssenceType.NATURE, true);
        SpellMutationProfile natureTransmuteMut = SpellMutationRegistry.resolveEffect(
                SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.TRANSMUTE, EssenceType.NATURE, true);
        SpellMutationProfile natureHexMut = SpellMutationRegistry.resolveEffect(
                SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.NATURE, true);

        boolean natureConstructsAndFormsOk =
                natureBeamMut.bonusPierceTargets() == 8
                && natureProjMut.bonusPierceTargets() == 0
                && ddraig.net.entropica.spell.mutation.element.NatureSpellMutationHandler.SYLVAN_EIDOLON_HEX_COLOR == 0x1A5923
                && !ddraig.net.entropica.spell.mutation.element.NatureSpellMutationHandler.isFriendlyOrWoodlandAlly(null, null)
                && "Sylvan Eidolon".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.NATURE, SpellEidolonEntity.TriadArchetype.AGENT))
                && "Sylvan Thorn Sentry".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.NATURE, SpellEidolonEntity.TriadArchetype.SENTRY))
                && "Sylvan Pollen Effigy".equals(SpellEidolonSpecs.resolveCanonicalBlueprintTitle(EssenceType.NATURE, SpellEidolonEntity.TriadArchetype.DECOY))
                && natureSkyfallMut.mutationDescription() != null
                && natureSkyfallMut.mutationDescription().contains("6s") && natureSkyfallMut.mutationDescription().contains("30s")
                && natureSentryMut.mutationDescription() != null && natureSentryMut.mutationDescription().contains("1.0s")
                && natureDecoyMut.mutationDescription() != null && natureDecoyMut.mutationDescription().contains("35%")
                && natureTransmuteMut.mutationDescription() != null && natureTransmuteMut.mutationDescription().contains("oak wood")
                && natureHexMut.mutationDescription() != null && natureHexMut.mutationDescription().contains("0 tick damage")
                && !SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL.isBlank();

        check(counts, lines, "Nature (#3) Sec 6.4 Invariants: Beam 8-Pierce, CHAIN_LEAP-Only Flow Gating, Advantage & Sylvan Constructs",
                natureAdvantageOk && natureFlowGatingOk && natureConstructsAndFormsOk);

        // Bind minimal vanilla block tags in headless Bootstrap mode so tag-driven block predicates execute live
        ensureHeadlessVanillaBlockTagsBound();

        // 53. Earth (#4) Section 6.5 Conformance: Magnetite Ore Config, 1.0x Non-Slow Flow, Exact 0.50x/1.50x Shroud, T1-3 Burrow Step & Sundering Hex
        net.minecraft.world.level.block.state.BlockState ironOreState = net.minecraft.world.level.block.Blocks.IRON_ORE.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState deepslateIronState = net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState deepslateCopperState = net.minecraft.world.level.block.Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState diamondOreState = net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState goldOreState = net.minecraft.world.level.block.Blocks.GOLD_ORE.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState stoneState = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState dirtState = net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState grassState = net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState oakPlanksState = net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState();

        boolean magnetiteConfigOk;
        try {
            ddraig.net.entropica.config.EntropicaConfig.clearMagnetiteBeamOreFilterTestOverride();
            boolean defaultOk = ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(ironOreState)
                    && ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(diamondOreState)
                    && !ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(stoneState)
                    && !ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());

            ddraig.net.entropica.config.EntropicaConfig.setMagnetiteBeamOreFilterTestOverride(
                    List.of("minecraft:iron_ore", "#c:ores/copper"), List.of());
            boolean whitelistOk = ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(ironOreState)
                    && ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(deepslateCopperState)
                    && !ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(diamondOreState);

            ddraig.net.entropica.config.EntropicaConfig.setMagnetiteBeamOreFilterTestOverride(
                    List.of(), List.of("minecraft:diamond_ore", "#c:ores/gold"));
            boolean blacklistOk = ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(ironOreState)
                    && !ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(diamondOreState)
                    && !ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(goldOreState);

            ddraig.net.entropica.config.EntropicaConfig.setMagnetiteBeamOreFilterTestOverride(
                    List.of("#c:ores/iron"), List.of("minecraft:deepslate_iron_ore"));
            boolean precedenceOk = ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(ironOreState)
                    && !ddraig.net.entropica.config.EntropicaConfig.isOreAllowedForMagnetiteBeam(deepslateIronState);

            magnetiteConfigOk = defaultOk && whitelistOk && blacklistOk && precedenceOk;
        } finally {
            ddraig.net.entropica.config.EntropicaConfig.clearMagnetiteBeamOreFilterTestOverride();
        }

        SpellMutationProfile earthFastFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.EARTH, true);
        SpellMutationProfile earthSingleFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SINGLE, EssenceType.EARTH, true);
        SpellMutationProfile earthSlowFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SLOW_HEAVY, EssenceType.EARTH, true);
        SpellMutationProfile earthHexMut = SpellMutationRegistry.resolveEffect(
                SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.EARTH, true);

        boolean earthFlowAndShroudOk =
                earthFastFlow.velocityMultiplier() == 1.0F
                && earthSingleFlow.velocityMultiplier() == 1.0F
                && earthSlowFlow.velocityMultiplier() == 1.0F
                && "Dense Tectonic Mass".equals(earthSlowFlow.signatureTitle())
                && earthSlowFlow.mutationDescription() != null
                && earthFastFlow.mutationDescription() == null
                && earthSingleFlow.mutationDescription() == null
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.calculateSpellAdvantageDamageMultiplier(
                        EssenceType.VOID, null, null, List.of(EssenceType.EARTH), 0.0F) == 0.50F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.calculateSpellAdvantageDamageMultiplier(
                        EssenceType.AIR, null, null, List.of(EssenceType.EARTH), 0.0F) == 1.50F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveConvertedTouchAttackMateria(
                        EssenceType.WATER, EssenceType.EARTH, true) == EssenceType.EARTH;

        boolean earthBurrowOk =
                ddraig.net.entropica.spell.engine.SpellExecutionEngine.canBlinkPhaseWalls(
                        null, net.minecraft.world.phys.Vec3.ZERO, net.minecraft.core.BlockPos.ZERO, 1, EssenceType.EARTH, true)
                && ddraig.net.entropica.spell.engine.SpellExecutionEngine.canBlinkPhaseWalls(
                        null, net.minecraft.world.phys.Vec3.ZERO, net.minecraft.core.BlockPos.ZERO, 2, EssenceType.EARTH, true)
                && ddraig.net.entropica.spell.engine.SpellExecutionEngine.canBlinkPhaseWalls(
                        null, net.minecraft.world.phys.Vec3.ZERO, net.minecraft.core.BlockPos.ZERO, 3, EssenceType.EARTH, true)
                && !ddraig.net.entropica.spell.engine.SpellExecutionEngine.canBlinkPhaseWalls(
                        null, net.minecraft.world.phys.Vec3.ZERO, net.minecraft.core.BlockPos.ZERO, 1, EssenceType.EARTH, false)
                && ddraig.net.entropica.spell.engine.SpellExecutionEngine.isStoneOrSoilStrata(stoneState)
                && ddraig.net.entropica.spell.engine.SpellExecutionEngine.isStoneOrSoilStrata(dirtState)
                && !ddraig.net.entropica.spell.engine.SpellExecutionEngine.isStoneOrSoilStrata(oakPlanksState)
                && Math.abs(ddraig.net.entropica.spell.engine.SpellExecutionEngine.resolveBurrowStepRangeBlocks(1, false, 1.0F) - 16.0D) < 1.0E-4D
                && Math.abs(ddraig.net.entropica.spell.engine.SpellExecutionEngine.resolveBurrowStepRangeBlocks(1, true, 1.0F) - 20.0D) < 1.0E-4D;

        String combatSrc = readRepoSource("src/main/java/ddraig/net/entropica/spell/engine/SpellCombatReactionEngine.java");
        String earthHandlerSrc = readRepoSource("src/main/java/ddraig/net/entropica/spell/mutation/element/EarthSpellMutationHandler.java");
        boolean earthHexAndSkyfallOk =
                ddraig.net.entropica.spell.mutation.element.EarthSpellMutationHandler.resolveSunderingHexArmorShred(1) == 5.0D
                && ddraig.net.entropica.spell.mutation.element.EarthSpellMutationHandler.resolveSunderingHexArmorShred(2) == 7.5D
                && ddraig.net.entropica.spell.mutation.element.EarthSpellMutationHandler.resolveSunderingHexArmorShred(3) == 10.0D
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveSunderingHexArmorShred(1) == 5.0D
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveSunderingHexArmorShred(2) == 7.5D
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveSunderingHexArmorShred(3) == 10.0D
                && earthHexMut.mutationDescription() != null && earthHexMut.mutationDescription().contains("0 tick damage")
                && combatSrc.contains("if (!sunderingHexActive && now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL)")
                && !combatSrc.contains("TAG_EARTH_DEFENSE_UNTIL).orElse(0L))\n                && MateriaFamilyTier.isVoidLineage")
                && !extractMethodWindow(earthHandlerSrc, "public void onPersistentHexTick", 25).contains("hurtWithPureMateria")
                && ddraig.net.entropica.spell.mutation.element.EarthSpellMutationHandler.resolveMeteoriteBarrageDurationTicks(1) == 120
                && ddraig.net.entropica.spell.mutation.element.EarthSpellMutationHandler.resolveMeteoriteBarrageDurationTicks(10) == 264
                && ddraig.net.entropica.spell.mutation.element.EarthSpellMutationHandler.isLooseExcavatableSediment(dirtState)
                && !ddraig.net.entropica.spell.mutation.element.EarthSpellMutationHandler.isLooseExcavatableSediment(grassState)
                && SpellEidolonEntity.shouldTriggerEarthShrapnelBurst(EssenceType.EARTH, SpellEidolonEntity.TriadArchetype.DECOY)
                && !SpellEidolonEntity.shouldTriggerEarthShrapnelBurst(EssenceType.EARTH, SpellEidolonEntity.TriadArchetype.AGENT)
                && !SpellEidolonEntity.shouldTriggerEarthShrapnelBurst(EssenceType.EARTH, SpellEidolonEntity.TriadArchetype.SENTRY);

        check(counts, lines, "Earth (#4) Sec 6.5 Invariants: Magnetite Ore Config, 1.0x Non-Slow Flow, 0.50x/1.50x Shroud, T1-3 Burrow Step & Sundering Hex",
                magnetiteConfigOk && earthFlowAndShroudOk && earthBurrowOk && earthHexAndSkyfallOk);

        // 54. Frozen (#5) Section 6.6 Conformance: Glacial Pitfall Rename, Non-Compounded 60% Slow, 30% Spikes Debuff, Shatter Hex & Decoy Shatter Gate
        SpellMutationProfile frozenGlyphMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.GLYPH, EssenceType.FROZEN, true);
        SpellMutationProfile frozenHexMut = SpellMutationRegistry.resolveEffect(
                SpellPartSlotType.CORE_EFFECT, SpellCoreEffect.AFFLICTION, EssenceType.FROZEN, true);
        SpellMutationProfile frozenSlowFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.SLOW_HEAVY, EssenceType.FROZEN, true);
        SpellMutationProfile frozenFastFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.FROZEN, true);

        String frozenHandlerSrc = readRepoSource("src/main/java/ddraig/net/entropica/spell/mutation/element/FrozenSpellMutationHandler.java");
        String projEntitySrc = readRepoSource("src/main/java/ddraig/net/entropica/entity/projectile/SpellProjectileEntity.java");

        boolean frozenRenameAndSlowOk =
                "Glacial Pitfall".equals(frozenGlyphMut.signatureTitle())
                && frozenGlyphMut.mutationDescription() != null
                && frozenGlyphMut.mutationDescription().contains("Glacial Pitfall")
                && !frozenGlyphMut.mutationDescription().contains("Rime Pitfall")
                && !frozenHandlerSrc.contains("Rime Pitfall")
                && !frozenHandlerSrc.contains("new MobEffectInstance(MobEffects.SLOWNESS")
                && !extractMethodWindow(frozenHandlerSrc, "public void onPersistentHexTick", 25).contains("hurtWithFrozenMateriaAdvantage")
                && !extractMethodWindow(projEntitySrc, "case FROZEN ->", 35).contains("MobEffects.SLOWNESS")
                && "Permafrost Creep".equals(frozenSlowFlow.signatureTitle())
                && frozenSlowFlow.mutationDescription() != null
                && frozenFastFlow.mutationDescription() == null
                && "entropica_frozen_ice_stasis_until".equals(
                        ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.TAG_FROZEN_ICE_STASIS_UNTIL);

        boolean frozenSpikesOk =
                ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveChilledMobSwingCooldownTicks(20L) == 26L
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveChilledMobSwingCooldownTicks(10L) == 13L
                && !extractMethodWindow(combatSrc, "TAG_FROZEN_SPIKES_UNTIL", 40).contains("DIG_SLOWDOWN")
                && !extractMethodWindow(combatSrc, "TAG_FROZEN_SPIKES_UNTIL", 40).contains("MINING_FATIGUE");

        boolean frozenHexAndDecoyOk =
                frozenHexMut.mutationDescription() != null && frozenHexMut.mutationDescription().contains("0 tick damage")
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveFrozenHexShatterMultiplier(1) == 1.50F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveFrozenHexShatterMultiplier(2) == 1.75F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveFrozenHexShatterMultiplier(3) == 2.00F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterDamageType(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterDamageType(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterDamageType(net.minecraft.world.damagesource.DamageTypes.MACE_SMASH)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterDamageType(net.minecraft.world.damagesource.DamageTypes.FALL)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterDamageType(net.minecraft.world.damagesource.DamageTypes.FALLING_ANVIL)
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterDamageType(net.minecraft.world.damagesource.DamageTypes.ARROW)
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterDamageType(net.minecraft.world.damagesource.DamageTypes.TRIDENT)
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterDamageType(net.minecraft.world.damagesource.DamageTypes.DROWN)
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterMateria(EssenceType.EARTH)
                && !ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.isFrozenHexShatterMateria(EssenceType.AIR)
                && SpellEidolonEntity.shouldTriggerFrozenShatterNova(EssenceType.FROZEN, SpellEidolonEntity.TriadArchetype.DECOY, true)
                && !SpellEidolonEntity.shouldTriggerFrozenShatterNova(EssenceType.FROZEN, SpellEidolonEntity.TriadArchetype.DECOY, false)
                && !SpellEidolonEntity.shouldTriggerFrozenShatterNova(EssenceType.FROZEN, SpellEidolonEntity.TriadArchetype.AGENT, true)
                && !SpellEidolonEntity.shouldTriggerFrozenShatterNova(EssenceType.FROZEN, SpellEidolonEntity.TriadArchetype.SENTRY, true);

        check(counts, lines, "Frozen (#5) Sec 6.6 Invariants: Glacial Pitfall Rename, Exact 60% Slow, 30% Spikes Debuff, Blunt Shatter Hex & Decoy Shatter Gate",
                frozenRenameAndSlowOk && frozenSpikesOk && frozenHexAndDecoyOk);

        // 55. Nether (#6) Section 6.7 Conformance: Immolation Shroud 0.50x/1.50x, PLANKS Charring, Hellfire Sear Hex & Brimstone 120t T1 Scaling
        net.minecraft.world.level.block.state.BlockState sprucePlanksState = net.minecraft.world.level.block.Blocks.SPRUCE_PLANKS.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState crimsonPlanksState = net.minecraft.world.level.block.Blocks.CRIMSON_PLANKS.defaultBlockState();
        SpellMutationProfile netherChainFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.NETHER, true);
        SpellMutationProfile netherFastFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.NETHER, true);
        String netherHandlerSrc = readRepoSource("src/main/java/ddraig/net/entropica/spell/mutation/element/NetherSpellMutationHandler.java");

        boolean netherShroudAndPlanksOk =
                ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.calculateSpellAdvantageDamageMultiplier(
                        EssenceType.FROZEN, null, null, List.of(EssenceType.NETHER), 0.0F) == 0.50F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.calculateSpellAdvantageDamageMultiplier(
                        EssenceType.WATER, null, null, List.of(EssenceType.NETHER), 0.0F) == 1.50F
                && !extractMethodWindow(combatSrc, "hurtWithFrozenMateriaAdvantage(", 70).contains("0.67F")
                && ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.isCharableOrganicBlock(oakPlanksState)
                && ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.isCharableOrganicBlock(sprucePlanksState)
                && ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.isCharableOrganicBlock(crimsonPlanksState)
                && !ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.isCharableOrganicBlock(stoneState);

        String counterCleanseWindow = extractMethodWindow(combatSrc, "public static boolean checkElementalHexCounterCleanse(", 30);
        boolean netherHexAndStormOk =
                ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.resolveHellfireSearArmorMeltFraction(1) == 0.30D
                && ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.resolveHellfireSearArmorMeltFraction(2) == 0.45D
                && ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.resolveHellfireSearArmorMeltFraction(3) == 0.60D
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveHellfireSearArmorMeltFraction(1) == 0.30D
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveHellfireSearArmorMeltFraction(2) == 0.45D
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveHellfireSearArmorMeltFraction(3) == 0.60D
                && combatSrc.contains("else if (mutated && resolved == EssenceType.NETHER)")
                && counterCleanseWindow.contains("case \"NETHER\" -> isWaterLine(incomingElement) || incomingElement == EssenceType.VITAE;")
                && !counterCleanseWindow.contains("case \"NETHER\", \"ARID\"")
                && ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.computeBrimstoneStormDurationTicks(1) == 120
                && ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.computeBrimstoneStormDurationTicks(10) == 264
                && "Arcing Fire-Whip Chains".equals(netherChainFlow.signatureTitle())
                && netherChainFlow.mutationDescription() != null
                && netherFastFlow.mutationDescription() == null
                && !netherHandlerSrc.contains("caster.heal(");

        check(counts, lines, "Nether (#6) Sec 6.7 Invariants: Immolation Shroud 0.50x/1.50x, PLANKS Charring, Hellfire Sear Hex & Brimstone 120t Scaling",
                netherShroudAndPlanksOk && netherHexAndStormOk);

        // 56. Radiant (#7) Section 6.8 Conformance: #FFE278 Palette, 0-Pierce Dawn Star, 0.50x/1.50x Shroud, 45%-Absorb Decoy, 4/6/8 Hex & Pseudoblock Pillar
        SpellMutationProfile radiantProjMut = SpellMutationRegistry.resolveForm(SpellDeliveryForm.PROJECTILE, EssenceType.RADIANT, true);
        SpellMutationProfile radiantChainFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.CHAIN_LEAP, EssenceType.RADIANT, true);
        SpellMutationProfile radiantFastFlow = SpellMutationRegistry.resolveFlow(SpellFlowModifier.FAST_DART, EssenceType.RADIANT, true);
        String radiantHandlerSrc = readRepoSource("src/main/java/ddraig/net/entropica/spell/mutation/element/RadiantSpellMutationHandler.java");
        String eidolonEntitySrc = readRepoSource("src/main/java/ddraig/net/entropica/entity/SpellEidolonEntity.java");

        boolean radiantColorAndPierceOk =
                EssenceType.RADIANT.getR() == 255
                && EssenceType.RADIANT.getG() == 226
                && EssenceType.RADIANT.getB() == 120
                && ((EssenceType.RADIANT.getR() << 16) | (EssenceType.RADIANT.getG() << 8) | EssenceType.RADIANT.getB()) == 0xFFE278
                && "Homing Dawn Star".equals(radiantProjMut.signatureTitle())
                && radiantProjMut.bonusPierceTargets() == 0
                && "Prismatic Starlight Chains".equals(radiantChainFlow.signatureTitle())
                && radiantChainFlow.mutationDescription() != null
                && radiantFastFlow.mutationDescription() == null;

        boolean radiantShroudAndTouchOk =
                ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.calculateSpellAdvantageDamageMultiplier(
                        EssenceType.UNDEAD, null, null, List.of(EssenceType.RADIANT), 0.0F) == 0.50F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.calculateSpellAdvantageDamageMultiplier(
                        EssenceType.UMBRAL, null, null, List.of(EssenceType.RADIANT), 0.0F) == 1.50F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveConvertedTouchAttackMateria(
                        EssenceType.WATER, EssenceType.RADIANT, true) == EssenceType.RADIANT
                && combatSrc.contains("TAG_RADIANT_SHROUD_UNTIL")
                && !extractMethodWindow(combatSrc, "hurtWithUndeadMateriaAdvantage(", 70).contains("0.67F");

        String radiantSummonWindow = extractMethodWindow(radiantHandlerSrc, "public void onSummonFormSpawned", 40);
        boolean radiantSeraphSuiteOk =
                !radiantSummonWindow.contains("caster.heal(")
                && Math.abs(SpellEidolonEntity.resolveSolarSeraphDecoyDamageTaken(20.0F) - 11.0F) < 1.0E-4F
                && Math.abs(SpellEidolonEntity.resolveSolarSeraphDecoyAbsorbedHeal(20.0F) - 9.0F) < 1.0E-4F
                && !SpellEidolonEntity.shouldTriggerRadiantDeathSmiteNova(EssenceType.RADIANT, SpellEidolonEntity.TriadArchetype.DECOY)
                && !SpellEidolonEntity.shouldTriggerRadiantDeathSmiteNova(EssenceType.RADIANT, SpellEidolonEntity.TriadArchetype.AGENT)
                && !SpellEidolonEntity.shouldTriggerRadiantDeathSmiteNova(EssenceType.RADIANT, SpellEidolonEntity.TriadArchetype.SENTRY)
                && (eidolonEntitySrc.contains("56.25") || eidolonEntitySrc.contains("7.5D * 7.5D"))
                && (eidolonEntitySrc.contains("64.0") || eidolonEntitySrc.contains("8.0D * 8.0D"));

        String radiantSkyfallWindow = extractMethodWindow(radiantHandlerSrc, "public boolean onSkyfallFormCast", 215);
        String radiantExcavateWindow = extractMethodWindow(radiantHandlerSrc, "public static void onRadiantLineageExcavateBlock", 55);
        List<int[]> t1QuartzPlan = ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.buildTierScaledCelestialQuartzColumns(1, 5);
        List<int[]> t10QuartzPlan = ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.buildTierScaledCelestialQuartzColumns(10, 14);
        boolean radiantSkyfallBreakAndHexOk =
                t1QuartzPlan.size() >= 13
                && t10QuartzPlan.size() > t1QuartzPlan.size()
                && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.resolveCelestialQuartzColumnState(2).is(net.minecraft.world.level.block.Blocks.QUARTZ_PILLAR)
                && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.resolveCelestialQuartzColumnState(1).is(net.minecraft.world.level.block.Blocks.CHISELED_QUARTZ_BLOCK)
                && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.resolveCelestialQuartzColumnState(0).is(net.minecraft.world.level.block.Blocks.SMOOTH_QUARTZ)
                && radiantSkyfallWindow.contains("broadcastSkyfallPseudoblockColumn")
                && radiantSkyfallWindow.contains("repelEntitiesFromPseudoblockColumn")
                && radiantHandlerSrc.contains("materializeSkyfallPseudoblockColumn")
                && !radiantHandlerSrc.contains("Blocks.LIGHT")
                && !radiantExcavateWindow.contains("Blocks.LIGHT")
                && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.resolveSunScorchedArmorMelt(1) == 4.0F
                && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.resolveSunScorchedArmorMelt(2) == 6.0F
                && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.resolveSunScorchedArmorMelt(3) == 8.0F
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveSunScorchedHexArmorMelt(1) == 4.0D
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveSunScorchedHexArmorMelt(2) == 6.0D
                && ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.resolveSunScorchedHexArmorMelt(3) == 8.0D
                && combatSrc.contains("else if (mutated && resolved == EssenceType.RADIANT)");

        check(counts, lines, "Radiant (#7) Sec 6.8 Invariants: #FFE278 Palette, 0-Pierce Dawn Star, 0.50x/1.50x Shroud, 45%-Absorb Decoy, 4/6/8 Hex & Pseudoblock Pillar",
                radiantColorAndPierceOk && radiantShroudAndTouchOk && radiantSeraphSuiteOk && radiantSkyfallBreakAndHexOk);

        return new VerificationReport(counts[0], counts[1], List.copyOf(lines));
    }

    @SuppressWarnings("unchecked")
    private static void ensureHeadlessVanillaBlockTagsBound() {
        if (net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState().is(net.minecraft.tags.BlockTags.PLANKS)) {
            return;
        }
        try {
            java.lang.reflect.Method bindTags = net.minecraft.core.Holder.Reference.class
                    .getDeclaredMethod("bindTags", java.util.Collection.class);
            bindTags.setAccessible(true);
            bindTags.invoke(net.minecraft.world.level.block.Blocks.OAK_PLANKS.builtInRegistryHolder(),
                    java.util.Set.of(net.minecraft.tags.BlockTags.PLANKS));
            bindTags.invoke(net.minecraft.world.level.block.Blocks.SPRUCE_PLANKS.builtInRegistryHolder(),
                    java.util.Set.of(net.minecraft.tags.BlockTags.PLANKS));
            bindTags.invoke(net.minecraft.world.level.block.Blocks.CRIMSON_PLANKS.builtInRegistryHolder(),
                    java.util.Set.of(net.minecraft.tags.BlockTags.PLANKS));
            bindTags.invoke(net.minecraft.world.level.block.Blocks.STONE.builtInRegistryHolder(),
                    java.util.Set.of(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD, net.minecraft.tags.BlockTags.STONE_ORE_REPLACEABLES));
            bindTags.invoke(net.minecraft.world.level.block.Blocks.DIRT.builtInRegistryHolder(),
                    java.util.Set.of(net.minecraft.tags.BlockTags.DIRT));
            bindTags.invoke(net.minecraft.world.level.block.Blocks.GRASS_BLOCK.builtInRegistryHolder(),
                    java.util.Set.of(net.minecraft.tags.BlockTags.DIRT));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to bind headless block tags for verification suite", ex);
        }
    }

    private static String readRepoSource(String relativeFromCommon) {
        try {
            java.nio.file.Path direct = java.nio.file.Path.of(relativeFromCommon);
            if (java.nio.file.Files.exists(direct)) {
                return java.nio.file.Files.readString(direct);
            }
            java.nio.file.Path fromRoot = java.nio.file.Path.of("common").resolve(relativeFromCommon);
            if (java.nio.file.Files.exists(fromRoot)) {
                return java.nio.file.Files.readString(fromRoot);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static String extractMethodWindow(String source, String anchor, int maxLines) {
        int idx = source.indexOf(anchor);
        if (idx < 0) {
            return "";
        }
        String tail = source.substring(idx);
        String[] lines = tail.split("\\R", maxLines + 1);
        StringBuilder sb = new StringBuilder();
        int count = Math.min(lines.length, maxLines);
        for (int i = 0; i < count; i++) {
            sb.append(lines[i]).append('\n');
        }
        return sb.toString();
    }

    private static boolean verifyClasspathModelsAndTextures() {
        Object[][] expectedModels = new Object[][]{
                {"arcane_scribing_table", 96, "Table_Legs_And_Frame"},
                {"arcanum_focus", 164, "Focus_Spellgem"},
                {"autocast_pedestal", 125, "Orbis_Connection_Port_Conditional"},
                {"grimoire", 891, "Right_Cover_Spellgem_9"},
                {"runic_anvil", 46, "Base_Plinth"},
                {"spellgem", 96, "Hex_Facet_0_0"},
                {"staff", 459, "Staff_Spellgem_4"},
                {"wand", 265, "Collar_Spellgem_2"}
        };
        ClassLoader cl = SpellEngineVerificationSuite.class.getClassLoader();
        for (Object[] entry : expectedModels) {
            String name = (String) entry[0];
            int expectedElements = (Integer) entry[1];
            String requiredToken = (String) entry[2];
            String resPath = "assets/entropica/custom_models/" + name + ".json";
            try (var in = cl.getResourceAsStream(resPath)) {
                if (in == null) return false;
                String raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                if (!raw.contains(requiredToken)) return false;
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(raw).getAsJsonObject();
                if (!root.has("elements") || root.getAsJsonArray("elements").size() != expectedElements) {
                    return false;
                }
            } catch (Exception ex) {
                return false;
            }
        }

        String[] requiredTextures = new String[]{
                "assets/entropica/textures/block/arcane_scribing_table.png",
                "assets/entropica/textures/block/runic_anvil.png",
                "assets/entropica/textures/block/autocast_pedestal_stone.png",
                "assets/entropica/textures/block/autocast_pedestal_marble.png",
                "assets/entropica/textures/block/autocast_pedestal_quartz.png",
                "assets/entropica/textures/block/autocast_pedestal_astral_slate.png",
                "assets/entropica/textures/item/arcanum_focus.png",
                "assets/entropica/textures/item/wand.png",
                "assets/entropica/textures/item/staff.png",
                "assets/entropica/textures/item/grimoire.png",
                "assets/entropica/textures/item/blank_spellgem.png",
                "assets/entropica/textures/item/inscribed_spellgem.png",
                "assets/entropica/textures/item/blank_parchment.png",
                "assets/entropica/textures/item/spell_parchment.png",
                "assets/entropica/textures/item/scribing_ink.png",
                "assets/entropica/textures/item/spellgem_seed.png"
        };
        for (String tex : requiredTextures) {
            try (var in = cl.getResourceAsStream(tex)) {
                if (in == null) return false;
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    private static boolean verifyClasspathCustomParticles() {
        String[] particleIds = new String[]{
                "tintable_sparkle", "tintable_spore", "tintable_mist", "tintable_drip", "tintable_ember", "tintable_rune",
                "tintable_blood", "tintable_vortex", "tintable_shield", "tintable_arc", "tintable_leaf", "tintable_wisp",
                "tintable_crystal", "tintable_bubble", "tintable_singularity", "tintable_sigil", "tintable_sunbeam", "tintable_acid",
                "rune_fehu", "rune_uruz", "rune_thurisaz", "rune_ansuz", "rune_raido", "rune_kenaz",
                "rune_gebo", "rune_wunjo", "rune_hagalaz", "rune_nauthiz", "rune_isa", "rune_jera",
                "rune_eihwaz", "rune_perthro", "rune_algiz", "rune_sowilo", "rune_tiwaz", "rune_berkano",
                "rune_ehwaz", "rune_mannaz", "rune_laguz", "rune_ingwaz", "rune_dagaz", "rune_othala"
        };
        ClassLoader cl = SpellEngineVerificationSuite.class.getClassLoader();
        for (String id : particleIds) {
            try (var jsonIn = cl.getResourceAsStream("assets/entropica/particles/" + id + ".json");
                 var pngIn = cl.getResourceAsStream("assets/entropica/textures/particle/" + id + ".png")) {
                if (jsonIn == null || pngIn == null) {
                    return false;
                }
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    private static void check(int[] counts, List<String> lines, String name, boolean ok) {
        if (ok) {
            counts[0]++;
            lines.add("PASS: " + name);
        } else {
            counts[1]++;
            lines.add("FAIL: " + name);
        }
    }

    public static void main(String[] args) {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        VerificationReport report = runAllChecks();
        for (String line : report.lines()) {
            System.out.println(line);
        }
        System.out.printf("Summary: %d passed, %d failed%n", report.passed(), report.failed());
        if (!report.allPassed()) {
            System.exit(1);
        }
    }
}
