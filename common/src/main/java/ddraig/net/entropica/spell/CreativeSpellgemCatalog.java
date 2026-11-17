package ddraig.net.entropica.spell;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.SpellgemItem;
import ddraig.net.entropica.spell.assembly.EffectPartBinding;
import ddraig.net.entropica.spell.assembly.FlowPartBinding;
import ddraig.net.entropica.spell.assembly.FormPartBinding;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Builds the comprehensive Creative-mode Spellgem testing suite for {@code ModCreativeTabs.SPELLGEMS_TAB}.
 *
 * <ul>
 *     <li>Every generated Spellgem is strictly locked to a single {@link EssenceType}
 *         ({@code materiaSpecific = true}, with Flow, Form, and Core Effect all bound to that Materia type,
 *         and Signature Mutations enabled for all non-{@link EssenceType#REGULAR} types).</li>
 *     <li>Covers all 11 {@link SpellDeliveryForm}s, all {@link SpellFlowModifier}s (including {@code VOID_WARP}
 *         exclusively for Void & Void-heritage Materia), all {@link SpellTriggerType}s, Multi-Cast & Dual-Verb
 *         vectors, and all 19 {@link SpellCoreEffect}s across every non-fragment Materia type.</li>
 *     <li>Excludes travel length/distance permutations: every generated Spellgem uses standard
 *         {@link SpellTravelDistance#STANDARD_METERS} ({@code 16m}).</li>
 * </ul>
 */
public final class CreativeSpellgemCatalog {

    private static volatile List<SpellAssembly> cachedAssemblies;

    private CreativeSpellgemCatalog() {
    }

    public static ItemStack createTabIconStack() {
        return SpellgemItem.createInscribedGem(singleSubSpell(
                EssenceType.ENTROPICA,
                "Entropica • Form: Projectile",
                SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE,
                SpellTriggerType.ON_ENTITY_HIT,
                0.0F,
                SpellDeliveryForm.PROJECTILE,
                SpellCoreEffect.HARM,
                "default"
        ));
    }

    public static void populateCreativeTab(CreativeModeTab.Output output) {
        for (SpellAssembly spell : getOrBuildAllTestAssemblies()) {
            output.accept(SpellgemItem.createInscribedGem(spell));
        }
    }

    public static List<SpellAssembly> getOrBuildAllTestAssemblies() {
        List<SpellAssembly> local = cachedAssemblies;
        if (local != null) {
            return local;
        }
        synchronized (CreativeSpellgemCatalog.class) {
            if (cachedAssemblies == null) {
                List<SpellAssembly> built = new ArrayList<>(3100);
                for (EssenceType type : EssenceType.values()) {
                    if (type.isFragment()) {
                        continue;
                    }
                    appendSuiteForMateria(built, type);
                }
                cachedAssemblies = Collections.unmodifiableList(built);
            }
            return cachedAssemblies;
        }
    }

    private static void appendSuiteForMateria(List<SpellAssembly> out, EssenceType type) {
        String prefixTag = "[" + formatMateriaLabel(type) + "] ";
        boolean mut = type != EssenceType.REGULAR;

        // =========================================================================
        // 1. ALL 11 DELIVERY FORMS (Standard 16m, Combat Harm)
        // =========================================================================
        for (SpellDeliveryForm form : SpellDeliveryForm.values()) {
            out.add(singleSubSpell(
                    type,
                    prefixTag + "Form: " + form.getDisplayName(),
                    SpellPrefix.COMBAT,
                    SpellFlowModifier.SINGLE,
                    SpellTriggerType.ON_ENTITY_HIT,
                    0.0F,
                    form,
                    SpellCoreEffect.HARM,
                    "default"
            ));
        }

        // =========================================================================
        // 2. DELIVERY VECTORS: FLOW MODIFIERS, TRIGGERS, MULTI-CAST & DUAL-VERB
        // =========================================================================
        SpellFlowModifier[] standardVectorFlows = {
                SpellFlowModifier.DOUBLE_BIFURCATE,
                SpellFlowModifier.TRIPLE_TRIFURCATE,
                SpellFlowModifier.OCTUPLE_DOME,
                SpellFlowModifier.CHAIN_LEAP,
                SpellFlowModifier.ORBIT_SATELLITE,
                SpellFlowModifier.FAST_DART,
                SpellFlowModifier.SLOW_HEAVY
        };
        for (SpellFlowModifier flow : standardVectorFlows) {
            out.add(singleSubSpell(
                    type,
                    prefixTag + "Flow: " + flow.getDisplayName(),
                    SpellPrefix.COMBAT,
                    flow,
                    SpellTriggerType.ON_ENTITY_HIT,
                    0.0F,
                    SpellDeliveryForm.PROJECTILE,
                    SpellCoreEffect.HARM,
                    "default"
            ));
        }

        // Void-Warp Relay requires Void or Void-creation heritage + active Flow mutation
        if (mut && MateriaFamilyTier.hasVoidCreationHeritage(type)) {
            out.add(singleSubSpell(
                    type,
                    prefixTag + "Flow: " + SpellFlowModifier.VOID_WARP.getDisplayName(),
                    SpellPrefix.COMBAT,
                    SpellFlowModifier.VOID_WARP,
                    SpellTriggerType.ON_ENTITY_HIT,
                    0.0F,
                    SpellDeliveryForm.RADIAL_BURST,
                    SpellCoreEffect.HARM,
                    "default"
            ));
        }

        // Trigger Logic Vectors (at fixed 16m travel distance)
        out.add(singleSubSpell(
                type,
                prefixTag + "Trigger: On-Block-Collision",
                SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE,
                SpellTriggerType.ON_BLOCK_COLLISION,
                0.0F,
                SpellDeliveryForm.RADIAL_BURST,
                SpellCoreEffect.HARM,
                "default"
        ));
        out.add(singleSubSpell(
                type,
                prefixTag + "Trigger: Timed Fuse",
                SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE,
                SpellTriggerType.ON_TIMER,
                1.5F,
                SpellDeliveryForm.RADIAL_BURST,
                SpellCoreEffect.HARM,
                "default"
        ));
        out.add(singleSubSpell(
                type,
                prefixTag + "Trigger: On-Proximity",
                SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE,
                SpellTriggerType.ON_PROXIMITY,
                0.0F,
                SpellDeliveryForm.RADIAL_BURST,
                SpellCoreEffect.HARM,
                "default"
        ));
        out.add(singleSubSpell(
                type,
                prefixTag + "Trigger: On-Release",
                SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE,
                SpellTriggerType.ON_RELEASE,
                0.0F,
                SpellDeliveryForm.PROJECTILE,
                SpellCoreEffect.HARM,
                "default"
        ));
        out.add(singleSubSpell(
                type,
                prefixTag + "Trigger: Corpse Bloom",
                SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE,
                SpellTriggerType.ON_DEATH,
                0.0F,
                SpellDeliveryForm.RADIAL_BURST,
                SpellCoreEffect.HARM,
                "default"
        ));
        out.add(singleSubSpell(
                type,
                prefixTag + "Trigger: Self",
                SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE,
                SpellTriggerType.SELF,
                0.0F,
                SpellDeliveryForm.RADIAL_BURST,
                SpellCoreEffect.KINETIC,
                "default"
        ));
        out.add(singleSubSpell(
                type,
                prefixTag + "Trigger: Self-Timed",
                SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE,
                SpellTriggerType.SELF_TIMED,
                10.0F,
                SpellDeliveryForm.AURA,
                SpellCoreEffect.WARD,
                "default"
        ));

        // Multi-Cast (3x) & Dual-Verb Vectors (strictly single-Materia locked)
        int tier = resolveTestingTier(type);
        out.add(new SpellAssembly.Builder(prefixTag + "Vector: Multi-Cast (3x)", SpellPrefix.COMBAT)
                .materiaSpecific(true)
                .potencyTier(tier)
                .flowBinding(new FlowPartBinding(
                        SpellFlowModifier.SINGLE,
                        SpellTriggerType.ON_ENTITY_HIT,
                        type,
                        mut,
                        3,
                        0.0F,
                        SpellTravelDistance.STANDARD_METERS
                ))
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, type, mut),
                        EffectPartBinding.of(SpellCoreEffect.HARM, type, mut)
                ))
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.RADIAL_BURST, type, mut),
                        EffectPartBinding.of(SpellCoreEffect.KINETIC, type, mut)
                ))
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(SpellDeliveryForm.GLYPH, type, mut),
                        EffectPartBinding.of(SpellCoreEffect.AFFLICTION, type, mut)
                ))
                .build());

        out.add(new SpellAssembly.Builder(prefixTag + "Vector: Dual-Verb", SpellPrefix.COMBAT)
                .materiaSpecific(true)
                .potencyTier(tier)
                .flowBinding(new FlowPartBinding(
                        SpellFlowModifier.SINGLE,
                        SpellTriggerType.ON_ENTITY_HIT,
                        type,
                        mut,
                        1,
                        0.0F,
                        SpellTravelDistance.STANDARD_METERS
                ))
                .addSubSpell(SubSpellSlot.ofDualVerb(
                        FormPartBinding.of(SpellDeliveryForm.PROJECTILE, type, mut),
                        EffectPartBinding.of(SpellCoreEffect.HARM, type, mut),
                        EffectPartBinding.of(SpellCoreEffect.AFFLICTION, type, mut)
                ))
                .build());

        // =========================================================================
        // 3. CORE EFFECTS: COMBAT, UTILITY & MISC VERBS (Standard 16m)
        // =========================================================================
        // Combat Verbs
        out.add(singleSubSpell(type, prefixTag + "Effect: Kinetic", SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.KINETIC, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Ward", SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE, SpellTriggerType.SELF, 0.0F,
                SpellDeliveryForm.AURA, SpellCoreEffect.WARD, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Blink (Self)", SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.BLINK, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Blink (Swap)", SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.BLINK, "swap"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Affliction", SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.AFFLICTION, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Restoration (Self)", SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE, SpellTriggerType.SELF, 0.0F,
                SpellDeliveryForm.AURA, SpellCoreEffect.RESTORATION, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Restoration (Bolt)", SpellPrefix.COMBAT,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.RESTORATION, "default"));

        // Utility Verbs
        out.add(singleSubSpell(type, prefixTag + "Effect: Break", SpellPrefix.UTILITY,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.BEAM, SpellCoreEffect.BREAK, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Transmute", SpellPrefix.UTILITY,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_BLOCK_COLLISION, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.TRANSMUTE, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Harvest", SpellPrefix.UTILITY,
                SpellFlowModifier.SINGLE, SpellTriggerType.SELF, 0.0F,
                SpellDeliveryForm.RADIAL_BURST, SpellCoreEffect.HARVEST, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Construct", SpellPrefix.UTILITY,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_BLOCK_COLLISION, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.CONSTRUCT, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Logistics", SpellPrefix.UTILITY,
                SpellFlowModifier.SINGLE, SpellTriggerType.SELF, 0.0F,
                SpellDeliveryForm.RADIAL_BURST, SpellCoreEffect.LOGISTICS, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Flight", SpellPrefix.UTILITY,
                SpellFlowModifier.SINGLE, SpellTriggerType.SELF, 0.0F,
                SpellDeliveryForm.AURA, SpellCoreEffect.FLIGHT, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Grapple", SpellPrefix.UTILITY,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.GRAPPLE, "default"));

        // Misc / Field Verbs
        out.add(singleSubSpell(type, prefixTag + "Effect: Conjure", SpellPrefix.MISC,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_BLOCK_COLLISION, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.CONJURE, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Sonar", SpellPrefix.MISC,
                SpellFlowModifier.SINGLE, SpellTriggerType.SELF, 0.0F,
                SpellDeliveryForm.RADIAL_BURST, SpellCoreEffect.SONAR, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Atmosphere", SpellPrefix.MISC,
                SpellFlowModifier.SINGLE, SpellTriggerType.SELF, 0.0F,
                SpellDeliveryForm.RADIAL_BURST, SpellCoreEffect.ATMOSPHERE, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Acoustic", SpellPrefix.MISC,
                SpellFlowModifier.SINGLE, SpellTriggerType.SELF, 0.0F,
                SpellDeliveryForm.RADIAL_BURST, SpellCoreEffect.ACOUSTIC, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Diagnostic", SpellPrefix.MISC,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.PROJECTILE, SpellCoreEffect.DIAGNOSTIC, "default"));
        out.add(singleSubSpell(type, prefixTag + "Effect: Gravity", SpellPrefix.MISC,
                SpellFlowModifier.SINGLE, SpellTriggerType.ON_ENTITY_HIT, 0.0F,
                SpellDeliveryForm.GLYPH, SpellCoreEffect.GRAVITY, "default"));
    }

    private static SpellAssembly singleSubSpell(
            EssenceType type,
            String customName,
            SpellPrefix prefix,
            SpellFlowModifier flow,
            SpellTriggerType trigger,
            float delaySeconds,
            SpellDeliveryForm form,
            SpellCoreEffect effect,
            String effectSubMode
    ) {
        boolean mut = type != EssenceType.REGULAR;
        int tier = resolveTestingTier(type);
        return new SpellAssembly.Builder(customName, prefix)
                .materiaSpecific(true)
                .potencyTier(tier)
                .flowBinding(new FlowPartBinding(
                        flow,
                        trigger,
                        type,
                        mut,
                        1,
                        delaySeconds,
                        SpellTravelDistance.STANDARD_METERS
                ))
                .addSubSpell(SubSpellSlot.of(
                        FormPartBinding.of(form, type, mut),
                        new EffectPartBinding(effect, type, mut, effectSubMode, 15)
                ))
                .build();
    }

    private static int resolveTestingTier(EssenceType type) {
        return Math.max(3, MateriaFamilyTier.of(type).getFusionTier());
    }

    private static String formatMateriaLabel(EssenceType type) {
        if (type == EssenceType.NULL_R) return "Null-R";
        if (type == EssenceType.NULL_U) return "Null-U";
        String[] words = type.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }
}
