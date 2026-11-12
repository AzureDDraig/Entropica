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

        // Dynamic trigger 25% / 75% split
        SpellAssembly delayedSpell = new SpellAssembly(
                "Delayed Trap",
                List.of(),
                false,
                SpellPrefix.COMBAT,
                2,
                new FlowPartBinding(SpellFlowModifier.DELAYED_BLOOM, EssenceType.LIGHTNING, true, 1, 0.0F),
                List.of(SubSpellSlot.of(formUnmutated, primMutated)),
                SpellFlawProfile.PERFECT,
                0
        );
        SpellCostBreakdown dynCost = SpellFuelCalculator.calculateCastCost(delayedSpell, null, 0.0F, EssenceType.REGULAR);
        check(counts, lines, "Dynamic Trigger Flow 25% Upfront / 75% Detonation Per-Materia Split",
                dynCost.upfrontMfumByType().getOrDefault(EssenceType.LIGHTNING, -1L) == 5L
                        && dynCost.dynamicTriggerMfumByType().getOrDefault(EssenceType.LIGHTNING, -1L) == 15L
                        && dynCost.upfrontMfumByType().getOrDefault(EssenceType.VOID, -1L) == 10L
                        && dynCost.dynamicTriggerMfumByType().getOrDefault(EssenceType.VOID, -1L) == 30L);

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
                && SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.PENUMBRA)
                && SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.OBLIVION)
                && SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.CHIMERA)
                && !SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.PYRE)
                && !SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.UMBRAL)
                && !SpellEidolonSpecs.isLifecycleRefreshMateria(EssenceType.REGULAR)
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

        boolean allFiftyEightRegisteredAndIsolated = SpellMutationHandlerRegistry.getAllRegistered().size() == 58;
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
            if (pRgb <= 0 || pRgb > 0xFFFFFF || aRgb <= 0 || aRgb > 0xFFFFFF || arch < 0 || arch > 7) {
                vfxPalettesAndArchetypesOk = false;
                break;
            }
        }
        boolean archetypeSpotChecksOk =
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.PRISMATIC, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_PRISMATIC_CHAKRAM
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.ESCHATON, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_SINGULARITY_ORB
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.TERMINUS, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_CRYSTALLINE_LANCE
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.LIGHTNING, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_VOLTAIC_JAVELIN
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.APOTHEOSIS, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_SOLAR_CROWN
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.MAGMA, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_MOLTEN_METEOR
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.BLOOD, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_BIO_HELIX
                && ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(EssenceType.AIR, false, false)
                        == ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_ARCANE_BOLT;

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

        return new VerificationReport(counts[0], counts[1], List.copyOf(lines));
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
