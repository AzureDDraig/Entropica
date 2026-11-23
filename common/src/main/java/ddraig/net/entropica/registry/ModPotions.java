package ddraig.net.entropica.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create("entropica", Registries.POTION);

    // Vitae Nectar Potion: Regeneration I (15s) + Health Boost I (30s)
    public static final RegistrySupplier<Potion> VITAE_NECTAR = POTIONS.register("vitae_nectar",
            () -> new Potion("vitae_nectar",
                    new MobEffectInstance(MobEffects.REGENERATION, 300, 0),
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 600, 0)
            )
    );

    // Pyre Nectar Potion: Fire Resistance I (3m)
    public static final RegistrySupplier<Potion> PYRE_NECTAR = POTIONS.register("pyre_nectar",
            () -> new Potion("pyre_nectar",
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0)
            )
    );

    // Frost Resistance Potion: Resistance I (3m = 3600t) + Fire Resistance (1m30s = 1800t)
    public static final RegistrySupplier<Potion> FROST_RESISTANCE = POTIONS.register("frost_resistance",
            () -> new Potion("frost_resistance",
                    new MobEffectInstance(MobEffects.RESISTANCE, 3600, 0),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1800, 0)
            )
    );

    // Freezing Potion: Slowness III (20s = 400t) + Weakness I (20s = 400t)
    public static final RegistrySupplier<Potion> FREEZING = POTIONS.register("freezing",
            () -> new Potion("freezing",
                    new MobEffectInstance(MobEffects.SLOWNESS, 400, 2),
                    new MobEffectInstance(MobEffects.WEAKNESS, 400, 0)
            )
    );

    // Decay Potion: Poison I (45s = 900t) + Weakness I (45s = 900t)
    public static final RegistrySupplier<Potion> DECAY = POTIONS.register("decay",
            () -> new Potion("decay",
                    new MobEffectInstance(MobEffects.POISON, 900, 0),
                    new MobEffectInstance(MobEffects.WEAKNESS, 900, 0)
            )
    );

    // Iron Skin Potion: Resistance II (1m30s = 1800t) + Absorption I (1m30s = 1800t)
    public static final RegistrySupplier<Potion> IRON_SKIN = POTIONS.register("iron_skin",
            () -> new Potion("iron_skin",
                    new MobEffectInstance(MobEffects.RESISTANCE, 1800, 1),
                    new MobEffectInstance(MobEffects.ABSORPTION, 1800, 0)
            )
    );

    // Eclipse Shroud Potion: Speed I (3m = 3600t) + Night Vision (3m = 3600t)
    public static final RegistrySupplier<Potion> ECLIPSE_SHROUD = POTIONS.register("eclipse_shroud",
            () -> new Potion("eclipse_shroud",
                    new MobEffectInstance(MobEffects.SPEED, 3600, 0),
                    new MobEffectInstance(MobEffects.NIGHT_VISION, 3600, 0)
            )
    );

    // Dampening Potion (Biome 6): Dampening (3m = 3600t) — silences footsteps and vibrations
    public static final RegistrySupplier<Potion> DAMPENING = POTIONS.register("dampening",
            () -> new Potion("dampening",
                    new MobEffectInstance(dampeningEffectHolder(), 3600, 0)
            )
    );

    // Long Dampening Potion (Biome 6): Dampening (8m = 9600t)
    public static final RegistrySupplier<Potion> LONG_DAMPENING = POTIONS.register("long_dampening",
            () -> new Potion("dampening",
                    new MobEffectInstance(dampeningEffectHolder(), 9600, 0)
            )
    );

    // Galvanic Surge Potion (Biome 7): Galvanic Surge (3m = 3600t) + Speed I (3600t) + Haste I (3600t)
    public static final RegistrySupplier<Potion> GALVANIC_SURGE = POTIONS.register("galvanic_surge",
            () -> new Potion("galvanic_surge",
                    new MobEffectInstance(galvanicSurgeEffectHolder(), 3600, 0),
                    new MobEffectInstance(MobEffects.SPEED, 3600, 0),
                    new MobEffectInstance(MobEffects.HASTE, 3600, 0)
            )
    );

    // Long Galvanic Surge Potion (Biome 7): Galvanic Surge (8m = 9600t) + Speed I (9600t) + Haste I (9600t)
    public static final RegistrySupplier<Potion> LONG_GALVANIC_SURGE = POTIONS.register("long_galvanic_surge",
            () -> new Potion("galvanic_surge",
                    new MobEffectInstance(galvanicSurgeEffectHolder(), 9600, 0),
                    new MobEffectInstance(MobEffects.SPEED, 9600, 0),
                    new MobEffectInstance(MobEffects.HASTE, 9600, 0)
            )
    );

    // Barrow Ward Potion (Biome 8): Barrow Ward (3m = 3600t) + Resistance I (3m = 3600t)
    public static final RegistrySupplier<Potion> BARROW_WARD = POTIONS.register("barrow_ward",
            () -> new Potion("barrow_ward",
                    new MobEffectInstance(barrowWardEffectHolder(), 3600, 0),
                    new MobEffectInstance(MobEffects.RESISTANCE, 3600, 0)
            )
    );

    // Long Barrow Ward Potion (Biome 8): Barrow Ward (8m = 9600t) + Resistance I (8m = 9600t)
    public static final RegistrySupplier<Potion> LONG_BARROW_WARD = POTIONS.register("long_barrow_ward",
            () -> new Potion("barrow_ward",
                    new MobEffectInstance(barrowWardEffectHolder(), 9600, 0),
                    new MobEffectInstance(MobEffects.RESISTANCE, 9600, 0)
            )
    );

    // Null Buoyancy Potion (Biome 9): Jump Boost II (3m = 3600t) + Slow Falling I (3m = 3600t)
    public static final RegistrySupplier<Potion> NULL_BUOYANCY = POTIONS.register("null_buoyancy",
            () -> new Potion("null_buoyancy",
                    new MobEffectInstance(MobEffects.JUMP_BOOST, 3600, 1),
                    new MobEffectInstance(MobEffects.SLOW_FALLING, 3600, 0)
            )
    );

    // Long Null Buoyancy Potion (Biome 9): Jump Boost II (8m = 9600t) + Slow Falling I (8m = 9600t)
    public static final RegistrySupplier<Potion> LONG_NULL_BUOYANCY = POTIONS.register("long_null_buoyancy",
            () -> new Potion("null_buoyancy",
                    new MobEffectInstance(MobEffects.JUMP_BOOST, 9600, 1),
                    new MobEffectInstance(MobEffects.SLOW_FALLING, 9600, 0)
            )
    );

    // Radiant Vitality Potion (Biome 10): Regeneration I (1m30s = 1800t) + Health Boost I (1m30s = 1800t)
    public static final RegistrySupplier<Potion> RADIANT_VITALITY = POTIONS.register("radiant_vitality",
            () -> new Potion("radiant_vitality",
                    new MobEffectInstance(MobEffects.REGENERATION, 1800, 0),
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 1800, 0)
            )
    );

    // Long Radiant Vitality Potion (Biome 10): Regeneration I (3m = 3600t) + Health Boost I (3m = 3600t)
    public static final RegistrySupplier<Potion> LONG_RADIANT_VITALITY = POTIONS.register("long_radiant_vitality",
            () -> new Potion("radiant_vitality",
                    new MobEffectInstance(MobEffects.REGENERATION, 3600, 0),
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 3600, 0)
            )
    );

    // Strong Radiant Vitality Potion (Biome 10): Regeneration II (45s = 900t) + Health Boost II (45s = 900t)
    public static final RegistrySupplier<Potion> STRONG_RADIANT_VITALITY = POTIONS.register("strong_radiant_vitality",
            () -> new Potion("radiant_vitality",
                    new MobEffectInstance(MobEffects.REGENERATION, 900, 1),
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 900, 1)
            )
    );

    // Tempest Stride Potion (Biome 11): Speed II (1m30s = 1800t) + Jump Boost I (1800t) + Slow Falling (1800t)
    public static final RegistrySupplier<Potion> TEMPEST_STRIDE = POTIONS.register("tempest_stride",
            () -> new Potion("tempest_stride",
                    new MobEffectInstance(MobEffects.SPEED, 1800, 1),
                    new MobEffectInstance(MobEffects.JUMP_BOOST, 1800, 0),
                    new MobEffectInstance(MobEffects.SLOW_FALLING, 1800, 0)
            )
    );

    // Long Tempest Stride Potion (Biome 11): Speed II (3m = 3600t) + Jump Boost I (3600t) + Slow Falling (3600t)
    public static final RegistrySupplier<Potion> LONG_TEMPEST_STRIDE = POTIONS.register("long_tempest_stride",
            () -> new Potion("tempest_stride",
                    new MobEffectInstance(MobEffects.SPEED, 3600, 1),
                    new MobEffectInstance(MobEffects.JUMP_BOOST, 3600, 0),
                    new MobEffectInstance(MobEffects.SLOW_FALLING, 3600, 0)
            )
    );

    // Spore Clarity Potion (Biome 12): Spore Clarity (3m = 3600t) + Resistance I (1m30s = 1800t)
    public static final RegistrySupplier<Potion> SPORE_CLARITY = POTIONS.register("spore_clarity",
            () -> new Potion("spore_clarity",
                    new MobEffectInstance(sporeClarityEffectHolder(), 3600, 0),
                    new MobEffectInstance(MobEffects.RESISTANCE, 1800, 0)
            )
    );

    // Long Spore Clarity Potion (Biome 12): Spore Clarity (8m = 9600t) + Resistance I (3m = 3600t)
    public static final RegistrySupplier<Potion> LONG_SPORE_CLARITY = POTIONS.register("long_spore_clarity",
            () -> new Potion("spore_clarity",
                    new MobEffectInstance(sporeClarityEffectHolder(), 9600, 0),
                    new MobEffectInstance(MobEffects.RESISTANCE, 3600, 0)
            )
    );

    // Mirage Phasing Potion (Biome 13): Mirage Phasing (3m = 3600t) + Speed I (3m = 3600t) + Invisibility (1m30s = 1800t)
    public static final RegistrySupplier<Potion> MIRAGE_PHASING = POTIONS.register("mirage_phasing",
            () -> new Potion("mirage_phasing",
                    new MobEffectInstance(miragePhasingEffectHolder(), 3600, 0),
                    new MobEffectInstance(MobEffects.SPEED, 3600, 0),
                    new MobEffectInstance(MobEffects.INVISIBILITY, 1800, 0)
            )
    );

    // Long Mirage Phasing Potion (Biome 13): Mirage Phasing (8m = 9600t) + Speed I (8m = 9600t) + Invisibility (3m = 3600t)
    public static final RegistrySupplier<Potion> LONG_MIRAGE_PHASING = POTIONS.register("long_mirage_phasing",
            () -> new Potion("mirage_phasing",
                    new MobEffectInstance(miragePhasingEffectHolder(), 9600, 0),
                    new MobEffectInstance(MobEffects.SPEED, 9600, 0),
                    new MobEffectInstance(MobEffects.INVISIBILITY, 3600, 0)
            )
    );

    // Oasis Renewal Potion (Biome 14): Oasis Renewal (3m = 3600t) + Regeneration I (1m30s = 1800t) + Absorption I (1m30s = 1800t)
    public static final RegistrySupplier<Potion> OASIS_RENEWAL = POTIONS.register("oasis_renewal",
            () -> new Potion("oasis_renewal",
                    new MobEffectInstance(oasisRenewalEffectHolder(), 3600, 0),
                    new MobEffectInstance(MobEffects.REGENERATION, 1800, 0),
                    new MobEffectInstance(MobEffects.ABSORPTION, 1800, 0)
            )
    );

    // Long Oasis Renewal Potion (Biome 14): Oasis Renewal (8m = 9600t) + Regeneration I (3m = 3600t) + Absorption I (3m = 3600t)
    public static final RegistrySupplier<Potion> LONG_OASIS_RENEWAL = POTIONS.register("long_oasis_renewal",
            () -> new Potion("oasis_renewal",
                    new MobEffectInstance(oasisRenewalEffectHolder(), 9600, 0),
                    new MobEffectInstance(MobEffects.REGENERATION, 3600, 0),
                    new MobEffectInstance(MobEffects.ABSORPTION, 3600, 0)
            )
    );

    // Zephyr Weight Potion (Biome 15): Zephyr Weight (3m = 3600t)
    public static final RegistrySupplier<Potion> ZEPHYR_WEIGHT = POTIONS.register("zephyr_weight",
            () -> new Potion("zephyr_weight",
                    new MobEffectInstance(zephyrWeightEffectHolder(), 3600, 0)
            )
    );

    // Long Zephyr Weight Potion (Biome 15): Zephyr Weight (8m = 9600t)
    public static final RegistrySupplier<Potion> LONG_ZEPHYR_WEIGHT = POTIONS.register("long_zephyr_weight",
            () -> new Potion("zephyr_weight",
                    new MobEffectInstance(zephyrWeightEffectHolder(), 9600, 0)
            )
    );

    // Sanguine Vigor Potion (Biome 16): Sanguine Vigor (3m = 3600t)
    public static final RegistrySupplier<Potion> SANGUINE_VIGOR = POTIONS.register("sanguine_vigor",
            () -> new Potion("sanguine_vigor",
                    new MobEffectInstance(sanguineVigorEffectHolder(), 3600, 0)
            )
    );

    // Long Sanguine Vigor Potion (Biome 16): Sanguine Vigor (8m = 9600t)
    public static final RegistrySupplier<Potion> LONG_SANGUINE_VIGOR = POTIONS.register("long_sanguine_vigor",
            () -> new Potion("sanguine_vigor",
                    new MobEffectInstance(sanguineVigorEffectHolder(), 9600, 0)
            )
    );

    // Starlight Grace Potion (Biome 17): Starlight Grace (3m = 3600t)
    public static final RegistrySupplier<Potion> STARLIGHT_GRACE = POTIONS.register("starlight_grace",
            () -> new Potion("starlight_grace",
                    new MobEffectInstance(starlightGraceEffectHolder(), 3600, 0)
            )
    );

    // Long Starlight Grace Potion (Biome 17): Starlight Grace (8m = 9600t)
    public static final RegistrySupplier<Potion> LONG_STARLIGHT_GRACE = POTIONS.register("long_starlight_grace",
            () -> new Potion("starlight_grace",
                    new MobEffectInstance(starlightGraceEffectHolder(), 9600, 0)
            )
    );

    private static Holder<MobEffect> dampeningEffectHolder() {
        if (ModEffects.DAMPENING.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.DAMPENING.get());
        }
        return ModEffects.DAMPENING;
    }

    private static Holder<MobEffect> galvanicSurgeEffectHolder() {
        if (ModEffects.GALVANIC_SURGE.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.GALVANIC_SURGE.get());
        }
        return ModEffects.GALVANIC_SURGE;
    }

    private static Holder<MobEffect> barrowWardEffectHolder() {
        if (ModEffects.BARROW_WARD.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BARROW_WARD.get());
        }
        return ModEffects.BARROW_WARD;
    }

    private static Holder<MobEffect> sporeClarityEffectHolder() {
        if (ModEffects.SPORE_CLARITY.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SPORE_CLARITY.get());
        }
        return ModEffects.SPORE_CLARITY;
    }

    private static Holder<MobEffect> miragePhasingEffectHolder() {
        if (ModEffects.MIRAGE_PHASING.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.MIRAGE_PHASING.get());
        }
        return ModEffects.MIRAGE_PHASING;
    }

    private static Holder<MobEffect> oasisRenewalEffectHolder() {
        if (ModEffects.OASIS_RENEWAL.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.OASIS_RENEWAL.get());
        }
        return ModEffects.OASIS_RENEWAL;
    }

    private static Holder<MobEffect> zephyrWeightEffectHolder() {
        if (ModEffects.ZEPHYR_WEIGHT.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ZEPHYR_WEIGHT.get());
        }
        return ModEffects.ZEPHYR_WEIGHT;
    }

    private static Holder<MobEffect> sanguineVigorEffectHolder() {
        if (ModEffects.SANGUINE_VIGOR.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SANGUINE_VIGOR.get());
        }
        return ModEffects.SANGUINE_VIGOR;
    }

    private static Holder<MobEffect> starlightGraceEffectHolder() {
        if (ModEffects.STARLIGHT_GRACE.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.STARLIGHT_GRACE.get());
        }
        return ModEffects.STARLIGHT_GRACE;
    }

    /**
     * Registers Entropica brewing mixes onto a vanilla {@link PotionBrewing.Builder}
     * across Polar biomes (Frost Resistance, Freezing, Decay, Iron Skin, Eclipse Shroud,
     * Dampening, Galvanic Surge, and Barrow Ward).
     */
    public static void registerBrewingMixes(PotionBrewing.Builder builder) {
        if (builder == null) {
            return;
        }
        if (FROST_RESISTANCE.isBound() && ModItems.RIMEBLOOM_PETAL.isBound()) {
            builder.addMix(Potions.AWKWARD, ModItems.RIMEBLOOM_PETAL.get(),
                    BuiltInRegistries.POTION.wrapAsHolder(FROST_RESISTANCE.get()));
        }
        if (FREEZING.isBound() && ModItems.FROST_CORE_SHARD.isBound()) {
            builder.addMix(Potions.AWKWARD, ModItems.FROST_CORE_SHARD.get(),
                    BuiltInRegistries.POTION.wrapAsHolder(FREEZING.get()));
        }
        if (DECAY.isBound() && ModItems.ROT_POWDER.isBound()) {
            builder.addMix(Potions.AWKWARD, ModItems.ROT_POWDER.get(),
                    BuiltInRegistries.POTION.wrapAsHolder(DECAY.get()));
        }
        if (DAMPENING.isBound() && ModItems.VAPOR_CONDENSER_SPORE.isBound()) {
            Holder<Potion> dampeningHolder = BuiltInRegistries.POTION.wrapAsHolder(DAMPENING.get());
            builder.addMix(Potions.AWKWARD, ModItems.VAPOR_CONDENSER_SPORE.get(), dampeningHolder);
            if (LONG_DAMPENING.isBound()) {
                Holder<Potion> longDampeningHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_DAMPENING.get());
                builder.addMix(dampeningHolder, Items.REDSTONE, longDampeningHolder);
            }
        }
        if (GALVANIC_SURGE.isBound() && ModItems.STATIC_FROST_POWDER.isBound()) {
            Holder<Potion> galvanicSurgeHolder = BuiltInRegistries.POTION.wrapAsHolder(GALVANIC_SURGE.get());
            builder.addMix(Potions.AWKWARD, ModItems.STATIC_FROST_POWDER.get(), galvanicSurgeHolder);
            if (LONG_GALVANIC_SURGE.isBound()) {
                Holder<Potion> longGalvanicSurgeHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_GALVANIC_SURGE.get());
                builder.addMix(galvanicSurgeHolder, Items.REDSTONE, longGalvanicSurgeHolder);
            }
        }
        if (BARROW_WARD.isBound() && ModItems.RIME_CAP_SPORES.isBound()) {
            Holder<Potion> barrowWardHolder = BuiltInRegistries.POTION.wrapAsHolder(BARROW_WARD.get());
            builder.addMix(Potions.AWKWARD, ModItems.RIME_CAP_SPORES.get(), barrowWardHolder);
            if (LONG_BARROW_WARD.isBound()) {
                Holder<Potion> longBarrowWardHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_BARROW_WARD.get());
                builder.addMix(barrowWardHolder, Items.REDSTONE, longBarrowWardHolder);
            }
        }
        if (NULL_BUOYANCY.isBound() && ModItems.VOID_CRYSTAL_SHARD.isBound()) {
            Holder<Potion> nullBuoyancyHolder = BuiltInRegistries.POTION.wrapAsHolder(NULL_BUOYANCY.get());
            builder.addMix(Potions.AWKWARD, ModItems.VOID_CRYSTAL_SHARD.get(), nullBuoyancyHolder);
            if (LONG_NULL_BUOYANCY.isBound()) {
                Holder<Potion> longNullBuoyancyHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_NULL_BUOYANCY.get());
                builder.addMix(nullBuoyancyHolder, Items.REDSTONE, longNullBuoyancyHolder);
            }
        }
        if (RADIANT_VITALITY.isBound() && ModItems.LIQUID_VITAE_RESIN.isBound()) {
            Holder<Potion> radiantVitalityHolder = BuiltInRegistries.POTION.wrapAsHolder(RADIANT_VITALITY.get());
            builder.addMix(Potions.AWKWARD, ModItems.LIQUID_VITAE_RESIN.get(), radiantVitalityHolder);
            if (LONG_RADIANT_VITALITY.isBound()) {
                Holder<Potion> longRadiantVitalityHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_RADIANT_VITALITY.get());
                builder.addMix(radiantVitalityHolder, Items.REDSTONE, longRadiantVitalityHolder);
            }
            if (STRONG_RADIANT_VITALITY.isBound()) {
                Holder<Potion> strongRadiantVitalityHolder = BuiltInRegistries.POTION.wrapAsHolder(STRONG_RADIANT_VITALITY.get());
                builder.addMix(radiantVitalityHolder, Items.GLOWSTONE_DUST, strongRadiantVitalityHolder);
            }
        }
        if (TEMPEST_STRIDE.isBound() && ModItems.TEMPEST_BOTTLE.isBound()) {
            Holder<Potion> tempestStrideHolder = BuiltInRegistries.POTION.wrapAsHolder(TEMPEST_STRIDE.get());
            builder.addMix(Potions.AWKWARD, ModItems.TEMPEST_BOTTLE.get(), tempestStrideHolder);
            if (LONG_TEMPEST_STRIDE.isBound()) {
                Holder<Potion> longTempestStrideHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_TEMPEST_STRIDE.get());
                builder.addMix(tempestStrideHolder, Items.REDSTONE, longTempestStrideHolder);
            }
        }
        if (SPORE_CLARITY.isBound() && ModItems.MYCELIAL_SPORE_CAP.isBound()) {
            Holder<Potion> sporeClarityHolder = BuiltInRegistries.POTION.wrapAsHolder(SPORE_CLARITY.get());
            builder.addMix(Potions.AWKWARD, ModItems.MYCELIAL_SPORE_CAP.get(), sporeClarityHolder);
            if (LONG_SPORE_CLARITY.isBound()) {
                Holder<Potion> longSporeClarityHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_SPORE_CLARITY.get());
                builder.addMix(sporeClarityHolder, Items.REDSTONE, longSporeClarityHolder);
            }
        }
        if (MIRAGE_PHASING.isBound() && ModItems.SHIMMERING_MIRAGE_AURA.isBound()) {
            Holder<Potion> miragePhasingHolder = BuiltInRegistries.POTION.wrapAsHolder(MIRAGE_PHASING.get());
            builder.addMix(Potions.AWKWARD, ModItems.SHIMMERING_MIRAGE_AURA.get(), miragePhasingHolder);
            if (LONG_MIRAGE_PHASING.isBound()) {
                Holder<Potion> longMiragePhasingHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_MIRAGE_PHASING.get());
                builder.addMix(miragePhasingHolder, Items.REDSTONE, longMiragePhasingHolder);
            }
        }
        if (OASIS_RENEWAL.isBound() && ModItems.AMBER_NECTAR_BOTTLE.isBound()) {
            Holder<Potion> oasisRenewalHolder = BuiltInRegistries.POTION.wrapAsHolder(OASIS_RENEWAL.get());
            builder.addMix(Potions.AWKWARD, ModItems.AMBER_NECTAR_BOTTLE.get(), oasisRenewalHolder);
            if (LONG_OASIS_RENEWAL.isBound()) {
                Holder<Potion> longOasisRenewalHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_OASIS_RENEWAL.get());
                builder.addMix(oasisRenewalHolder, Items.REDSTONE, longOasisRenewalHolder);
            }
        }
        if (ZEPHYR_WEIGHT.isBound() && ModItems.DUST_CORE_SHARD.isBound()) {
            Holder<Potion> zephyrWeightHolder = BuiltInRegistries.POTION.wrapAsHolder(ZEPHYR_WEIGHT.get());
            builder.addMix(Potions.AWKWARD, ModItems.DUST_CORE_SHARD.get(), zephyrWeightHolder);
            if (LONG_ZEPHYR_WEIGHT.isBound()) {
                Holder<Potion> longZephyrWeightHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_ZEPHYR_WEIGHT.get());
                builder.addMix(zephyrWeightHolder, Items.REDSTONE, longZephyrWeightHolder);
            }
        }
        if (SANGUINE_VIGOR.isBound() && ModItems.SANGUINE_HEART_GEL.isBound()) {
            Holder<Potion> sanguineVigorHolder = BuiltInRegistries.POTION.wrapAsHolder(SANGUINE_VIGOR.get());
            builder.addMix(Potions.AWKWARD, ModItems.SANGUINE_HEART_GEL.get(), sanguineVigorHolder);
            if (LONG_SANGUINE_VIGOR.isBound()) {
                Holder<Potion> longSanguineVigorHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_SANGUINE_VIGOR.get());
                builder.addMix(sanguineVigorHolder, Items.REDSTONE, longSanguineVigorHolder);
            }
        }
        if (STARLIGHT_GRACE.isBound() && ModItems.STARDUST_FRAGMENT.isBound()) {
            Holder<Potion> starlightGraceHolder = BuiltInRegistries.POTION.wrapAsHolder(STARLIGHT_GRACE.get());
            builder.addMix(Potions.AWKWARD, ModItems.STARDUST_FRAGMENT.get(), starlightGraceHolder);
            if (LONG_STARLIGHT_GRACE.isBound()) {
                Holder<Potion> longStarlightGraceHolder = BuiltInRegistries.POTION.wrapAsHolder(LONG_STARLIGHT_GRACE.get());
                builder.addMix(starlightGraceHolder, Items.REDSTONE, longStarlightGraceHolder);
            }
        }
        if (IRON_SKIN.isBound() && ModItems.AEGIS_SPIRE_PETAL.isBound()) {
            builder.addMix(Potions.AWKWARD, ModItems.AEGIS_SPIRE_PETAL.get(),
                    BuiltInRegistries.POTION.wrapAsHolder(IRON_SKIN.get()));
        }
        if (ECLIPSE_SHROUD.isBound() && ModBlocks.SHADOWED_ECLIPSE_MOSS.isBound()) {
            builder.addMix(Potions.AWKWARD, ModBlocks.SHADOWED_ECLIPSE_MOSS.get().asItem(),
                    BuiltInRegistries.POTION.wrapAsHolder(ECLIPSE_SHROUD.get()));
        }
    }
}
