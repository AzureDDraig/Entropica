package ddraig.net.entropica.registry;

import ddraig.net.entropica.effect.MateriaToxicityEffect;
import ddraig.net.entropica.effect.VoidTearEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create("entropica", net.minecraft.resources.ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.withDefaultNamespace("mob_effect")));

    // Registers the effect as Harmful with a toxic cyan particle color
    public static final RegistrySupplier<MobEffect> MATERIA_TOXICITY = EFFECTS.register("materia_toxicity",
            () -> new MateriaToxicityEffect(MobEffectCategory.HARMFUL, 0x1AE5D4));

    // Registers the new Void Tear effect with a dark abyssal purple color
    public static final RegistrySupplier<MobEffect> VOID_TEAR = EFFECTS.register("void_tear",
            VoidTearEffect::new);

    // Registers the new Haze effect with a custom color
    public static final RegistrySupplier<MobEffect> HAZE = EFFECTS.register("haze",
            ddraig.net.entropica.effect.HazeEffect::new);

    // Registers the new Paralyzed effect with a purple electric color
    public static final RegistrySupplier<MobEffect> PARALYZED = EFFECTS.register("paralyzed",
            () -> new ddraig.net.entropica.effect.ParalyzedEffect(MobEffectCategory.HARMFUL, 0x9D4EDD));

    // Registers the new Bleeding effect with a deep blood crimson color
    public static final RegistrySupplier<MobEffect> BLEEDING = EFFECTS.register("bleeding",
            () -> new ddraig.net.entropica.effect.BleedingEffect(MobEffectCategory.HARMFUL, 0x991B1B));

    // --- Gravity Manipulation Status Effects ---
    public static final RegistrySupplier<MobEffect> WEIGHTLESSNESS = EFFECTS.register("weightlessness",
            ddraig.net.entropica.effect.WeightlessnessEffect::new);

    public static final RegistrySupplier<MobEffect> GRAVITATIONAL_CRUSH = EFFECTS.register("gravitational_crush",
            ddraig.net.entropica.effect.GravitationalCrushEffect::new);

    public static final RegistrySupplier<MobEffect> INERTIAL_ANCHOR = EFFECTS.register("inertial_anchor",
            ddraig.net.entropica.effect.InertialAnchorEffect::new);

    // Dust Materia (#17) Abrasive Armor Erosion (-5% Armor & Toughness per level, up to Level 20 = -100%)
    public static final RegistrySupplier<MobEffect> ARMOR_EROSION = EFFECTS.register("armor_erosion",
            ddraig.net.entropica.effect.ArmorErosionEffect::new);

    // Biome 6 (2.6 Steaming Skerries): Dampening — silences footsteps and acoustic vibrations
    public static final RegistrySupplier<MobEffect> DAMPENING = EFFECTS.register("dampening",
            ddraig.net.entropica.effect.DampeningEffect::new);

    // Biome 7 (2.7 Static Ice-Plains): Galvanic Surge — zaps melee attackers back with static + chill
    public static final RegistrySupplier<MobEffect> GALVANIC_SURGE = EFFECTS.register("galvanic_surge",
            ddraig.net.entropica.effect.GalvanicSurgeEffect::new);

    // Biome 8 (2.8 Barrow-Mound Tundra): Barrow Ward — purges Wither and shields against Undead & Barrow Hounds
    public static final RegistrySupplier<MobEffect> BARROW_WARD = EFFECTS.register("barrow_ward",
            ddraig.net.entropica.effect.BarrowWardEffect::new);

    // Biome 12 (2.12 Spore-Glazed Pack): Spore Clarity — purges Nausea and Poison and protects against spore haze
    public static final RegistrySupplier<MobEffect> SPORE_CLARITY = EFFECTS.register("spore_clarity",
            ddraig.net.entropica.effect.SporeClarityEffect::new);

    // Biome 13 (2.13 Mirage Glacier): Double Vision — refractive Mirage & Glacial disorientation
    public static final RegistrySupplier<MobEffect> DOUBLE_VISION = EFFECTS.register("double_vision",
            ddraig.net.entropica.effect.DoubleVisionEffect::new);

    // Biome 13 (2.13 Mirage Glacier): Mirage Phasing — beneficial Mirage & Glacial refraction aura
    public static final RegistrySupplier<MobEffect> MIRAGE_PHASING = EFFECTS.register("mirage_phasing",
            ddraig.net.entropica.effect.MiragePhasingEffect::new);

    // Biome 14 (2.14 Oasis Floes): Oasis Renewal — clears freeze buildup and extinguishes Wither chill
    public static final RegistrySupplier<MobEffect> OASIS_RENEWAL = EFFECTS.register("oasis_renewal",
            ddraig.net.entropica.effect.OasisRenewalEffect::new);

    // Biome 15 (2.15 Dust-Crusted Bergs): Zephyr Weight — buoyant wind-cushioning & dust blindness immunity
    public static final RegistrySupplier<MobEffect> ZEPHYR_WEIGHT = EFFECTS.register("zephyr_weight",
            ddraig.net.entropica.effect.ZephyrWeightEffect::new);

    // Biome 16 (2.16 Blood-Ice Shallows): Sanguine Vigor — +2 Hearts Max Health, frost-thawing & melee life-siphon
    public static final RegistrySupplier<MobEffect> SANGUINE_VIGOR = EFFECTS.register("sanguine_vigor",
            ddraig.net.entropica.effect.SanguineVigorEffect::new);

    // Biome 17 (2.17 Astral Skerries): Starlight Grace — +0.5 step height, cushioned falls, +12% speed, frost/darkness purge & steady Night Vision
    public static final RegistrySupplier<MobEffect> STARLIGHT_GRACE = EFFECTS.register("starlight_grace",
            ddraig.net.entropica.effect.StarlightGraceEffect::new);
}
