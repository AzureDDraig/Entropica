package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.EidolicShadowEntity;
import ddraig.net.entropica.entity.EssenceNodeEntity;
import ddraig.net.entropica.entity.EssenceOrbEntity;
import ddraig.net.entropica.entity.grot.GrotEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxAfterimageEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import ddraig.net.entropica.entity.MagmaThornEntity;
import ddraig.net.entropica.entity.SporeProjectileEntity;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Entropica.MODID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<SporeProjectileEntity>> SPORE_PROJECTILE =
            ENTITY_TYPES.register("spore_projectile",
                    () -> EntityType.Builder.<SporeProjectileEntity>of(SporeProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_projectile"))));

    public static final RegistrySupplier<EntityType<MagmaThornEntity>> MAGMA_THORN =
            ENTITY_TYPES.register("magma_thorn",
                    () -> EntityType.Builder.<MagmaThornEntity>of(MagmaThornEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "magma_thorn"))));

    public static final RegistrySupplier<EntityType<EssenceOrbEntity>> ESSENCE_ORB =
            ENTITY_TYPES.register("essence_orb",
                    () -> EntityType.Builder.<EssenceOrbEntity>of(EssenceOrbEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .fireImmune()
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_orb"))));

    public static final RegistrySupplier<EntityType<EssenceNodeEntity>> ESSENCE_NODE =
            ENTITY_TYPES.register("essence_node",
                    () -> EntityType.Builder.<EssenceNodeEntity>of(EssenceNodeEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_node"))));

    public static final RegistrySupplier<EntityType<EidolicShadowEntity>> EIDOLIC_SHADOW =
            ENTITY_TYPES.register("eidolic_shadow",
                    () -> EntityType.Builder.<EidolicShadowEntity>of(EidolicShadowEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eidolic_shadow"))));

    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.SpellEidolonEntity>> SPELL_EIDOLON =
            ENTITY_TYPES.register("spell_eidolon",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.SpellEidolonEntity>of(ddraig.net.entropica.entity.SpellEidolonEntity::new, MobCategory.MISC)
                            .sized(0.8F, 1.9F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spell_eidolon"))));

    public static final RegistrySupplier<EntityType<GrotEntity>> GROT =
            ENTITY_TYPES.register("grot",
                    () -> EntityType.Builder.<GrotEntity>of(GrotEntity::new, MobCategory.MONSTER)
                            .sized(2.04F, 2.04F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grot"))));

    // --- VEIL FOX ENTITY ---
    public static final RegistrySupplier<EntityType<VeilFoxEntity>> VEIL_FOX =
            ENTITY_TYPES.register("veil_fox",
                    () -> EntityType.Builder.<VeilFoxEntity>of(VeilFoxEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "veil_fox"))));

    // --- VEIL FOX AFTERIMAGE ---
    public static final RegistrySupplier<EntityType<VeilFoxAfterimageEntity>> VEIL_FOX_AFTERIMAGE =
            ENTITY_TYPES.register("veil_fox_afterimage",
                    () -> EntityType.Builder.<VeilFoxAfterimageEntity>of(VeilFoxAfterimageEntity::new, MobCategory.MISC)
                            .sized(0.6F, 0.7F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "veil_fox_afterimage"))));

    // --- ASHEN STALKER ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.ashen_stalker.AshenStalkerEntity>> ASHEN_STALKER =
            ENTITY_TYPES.register("ashen_stalker",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.ashen_stalker.AshenStalkerEntity>of(ddraig.net.entropica.entity.ashen_stalker.AshenStalkerEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.4F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "ashen_stalker"))));

    // --- SPORE DRIFTER ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.SporeDrifterEntity>> SPORE_DRIFTER =
            ENTITY_TYPES.register("spore_drifter",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.SporeDrifterEntity>of(ddraig.net.entropica.entity.SporeDrifterEntity::new, MobCategory.AMBIENT)
                            .sized(0.8F, 1.8F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_drifter"))));

    // --- SPORE CLOUD ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.SporeCloudEntity>> SPORE_CLOUD =
            ENTITY_TYPES.register("spore_cloud",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.SporeCloudEntity>of(ddraig.net.entropica.entity.SporeCloudEntity::new, MobCategory.MISC)
                            .sized(1.5F, 1.5F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_cloud"))));

    // --- RIME-BACK OVIS ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.ovis.RimeBackOvisEntity>> RIME_BACK_OVIS =
            ENTITY_TYPES.register("rime_back_ovis",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.ovis.RimeBackOvisEntity>of(ddraig.net.entropica.entity.ovis.RimeBackOvisEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.3F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rime_back_ovis"))));

    // --- OVERGROWTH OVIS ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.ovis.OvergrowthOvisEntity>> OVERGROWTH_OVIS =
            ENTITY_TYPES.register("overgrowth_ovis",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.ovis.OvergrowthOvisEntity>of(ddraig.net.entropica.entity.ovis.OvergrowthOvisEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.3F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "overgrowth_ovis"))));

    // --- RIME SHEPHERD ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.RimeShepherdEntity>> RIME_SHEPHERD =
            ENTITY_TYPES.register("rime_shepherd",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.RimeShepherdEntity>of(ddraig.net.entropica.entity.RimeShepherdEntity::new, MobCategory.CREATURE)
                            .sized(1.2F, 2.2F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rime_shepherd"))));

    // --- BLOOM CRAWLER ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.BloomCrawlerEntity>> BLOOM_CRAWLER =
            ENTITY_TYPES.register("bloom_crawler",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.BloomCrawlerEntity>of(ddraig.net.entropica.entity.BloomCrawlerEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.5F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "bloom_crawler"))));

    // --- AURORAFOWL (FAUNA 3) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.aurorafowl.AurorafowlEntity>> AURORAFOWL =
            ENTITY_TYPES.register("aurorafowl",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.aurorafowl.AurorafowlEntity>of(ddraig.net.entropica.entity.aurorafowl.AurorafowlEntity::new, MobCategory.CREATURE)
                            .sized(1.2F, 2.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aurorafowl"))));

    // --- GEYSER WIGGLE-WORM (FAUNA 18) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.geyser_wiggle_worm.GeyserWiggleWormEntity>> GEYSER_WIGGLE_WORM =
            ENTITY_TYPES.register("geyser_wiggle_worm",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.geyser_wiggle_worm.GeyserWiggleWormEntity>of(ddraig.net.entropica.entity.geyser_wiggle_worm.GeyserWiggleWormEntity::new, MobCategory.MONSTER)
                            .sized(1.6F, 1.2F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "geyser_wiggle_worm"))));

    // --- CRYO-STALKER (FAUNA 2) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity>> CRYO_STALKER =
            ENTITY_TYPES.register("cryo_stalker",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity>of(ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 1.4F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_stalker"))));

    // --- PATINA GALVANIC OVIS ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.ovis.PatinaGalvanicOvisEntity>> PATINA_OVIS =
            ENTITY_TYPES.register("patina_ovis",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.ovis.PatinaGalvanicOvisEntity>of(ddraig.net.entropica.entity.ovis.PatinaGalvanicOvisEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.3F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "patina_ovis"))));

    // --- GEMINI GOAT MAGE (CAPRINE SPELLCASTER) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.gemini_goat_mage.GeminiGoatMageEntity>> GEMINI_GOAT_MAGE =
            ENTITY_TYPES.register("gemini_goat_mage",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.gemini_goat_mage.GeminiGoatMageEntity>of(ddraig.net.entropica.entity.gemini_goat_mage.GeminiGoatMageEntity::new, MobCategory.CREATURE)
                            .sized(1.2F, 2.5F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "gemini_goat_mage"))));

    // --- SPELL PROJECTILES ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.RadiantFireLanceEntity>> RADIANT_FIRE_LANCE =
            ENTITY_TYPES.register("radiant_fire_lance",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.RadiantFireLanceEntity>of(ddraig.net.entropica.entity.projectile.RadiantFireLanceEntity::new, MobCategory.MISC)
                            .sized(0.3F, 0.3F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_fire_lance"))));

    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.UmbralVortexEntity>> UMBRAL_VORTEX =
            ENTITY_TYPES.register("umbral_vortex",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.UmbralVortexEntity>of(ddraig.net.entropica.entity.projectile.UmbralVortexEntity::new, MobCategory.MISC)
                            .sized(1.2F, 1.2F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "umbral_vortex"))));

    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.SingularityGrenadeEntity>> SINGULARITY_GRENADE =
            ENTITY_TYPES.register("singularity_grenade",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.SingularityGrenadeEntity>of(ddraig.net.entropica.entity.projectile.SingularityGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.35F, 0.35F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "singularity_grenade"))));

    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.SpellProjectileEntity>> SPELL_PROJECTILE =
            ENTITY_TYPES.register("spell_projectile",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.SpellProjectileEntity>of(ddraig.net.entropica.entity.projectile.SpellProjectileEntity::new, MobCategory.MISC)
                            .sized(0.3F, 0.3F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spell_projectile"))));

    // --- VOID SEA SERPENT ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentEntity>> VOID_SEA_SERPENT =
            ENTITY_TYPES.register("void_sea_serpent",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentEntity>of(ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentEntity::new, MobCategory.MONSTER)
                            .sized(2.0F, 1.8F)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sea_serpent"))));

    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentSegmentEntity>> VOID_SEA_SERPENT_SEGMENT =
            ENTITY_TYPES.register("void_sea_serpent_segment",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentSegmentEntity>of(ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentSegmentEntity::new, MobCategory.MISC)
                            .sized(1.8F, 1.8F)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sea_serpent_segment"))));

    // --- GLACIAL HYDRA ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.glacial_hydra.GlacialHydraEntity>> GLACIAL_HYDRA =
            ENTITY_TYPES.register("glacial_hydra",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.glacial_hydra.GlacialHydraEntity>of(ddraig.net.entropica.entity.glacial_hydra.GlacialHydraEntity::new, MobCategory.MONSTER)
                            .sized(1.8F, 2.2F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glacial_hydra"))));

    // --- LUMINOTH ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.luminoth.LuminothEntity>> LUMINOTH =
            ENTITY_TYPES.register("luminoth",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.luminoth.LuminothEntity>of(ddraig.net.entropica.entity.luminoth.LuminothEntity::new, MobCategory.AMBIENT)
                            .sized(0.8F, 0.6F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "luminoth"))));

    // --- AMBER WEEPING STAG ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.amber_weeping_stag.AmberWeepingStagEntity>> AMBER_WEEPING_STAG =
            ENTITY_TYPES.register("amber_weeping_stag",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.amber_weeping_stag.AmberWeepingStagEntity>of(ddraig.net.entropica.entity.amber_weeping_stag.AmberWeepingStagEntity::new, MobCategory.CREATURE)
                            .sized(1.3F, 2.2F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_weeping_stag"))));

    // --- STORM KITE ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.storm_kite.StormKiteEntity>> STORM_KITE =
            ENTITY_TYPES.register("storm_kite",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.storm_kite.StormKiteEntity>of(ddraig.net.entropica.entity.storm_kite.StormKiteEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 0.8F)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "storm_kite"))));

    // --- FORCEFIELD BARRIER ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity>> FORCEFIELD_BARRIER =
            ENTITY_TYPES.register("forcefield_barrier",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity>of(ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "forcefield_barrier"))));

    // --- GLACIAL FROST-CORE (FAUNA 19) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.frost_core.GlacialFrostCoreEntity>> GLACIAL_FROST_CORE =
            ENTITY_TYPES.register("glacial_frost_core",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.frost_core.GlacialFrostCoreEntity>of(ddraig.net.entropica.entity.frost_core.GlacialFrostCoreEntity::new, MobCategory.CREATURE)
                            .sized(0.8F, 1.2F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glacial_frost_core"))));

    // --- FROST-TUSK MAMMOT (FAUNA 20) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.mammot.FrostTuskMammotEntity>> FROST_TUSK_MAMMOT =
            ENTITY_TYPES.register("frost_tusk_mammot",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.mammot.FrostTuskMammotEntity>of(ddraig.net.entropica.entity.mammot.FrostTuskMammotEntity::new, MobCategory.CREATURE)
                            .sized(1.95F, 2.55F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_tusk_mammot"))));

    // --- AURORA JELLY (FAUNA 4) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.aurora_jelly.AuroraJellyEntity>> AURORA_JELLY =
            ENTITY_TYPES.register("aurora_jelly",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.aurora_jelly.AuroraJellyEntity>of(ddraig.net.entropica.entity.aurora_jelly.AuroraJellyEntity::new, MobCategory.CREATURE)
                            .sized(0.85F, 1.5F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aurora_jelly"))));

    // --- CRYO-GRENADE (BIOME 5 PROJECTILE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.CryoGrenadeEntity>> CRYO_GRENADE =
            ENTITY_TYPES.register("cryo_grenade",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.CryoGrenadeEntity>of(ddraig.net.entropica.entity.projectile.CryoGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_grenade"))));

    // --- VAPOR-BELLOWS CRAB (FAUNA 22) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.vapor_bellows_crab.VaporBellowsCrabEntity>> VAPOR_BELLOWS_CRAB =
            ENTITY_TYPES.register("vapor_bellows_crab",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.vapor_bellows_crab.VaporBellowsCrabEntity>of(
                                    ddraig.net.entropica.entity.vapor_bellows_crab.VaporBellowsCrabEntity::new,
                                    MobCategory.CREATURE)
                            .sized(1.25F, 0.95F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_bellows_crab"))));

    // --- CRYO-STATIC HEDGEHOG (FAUNA 21) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.cryo_static_hedgehog.CryoStaticHedgehogEntity>> CRYO_STATIC_HEDGEHOG =
            ENTITY_TYPES.register("cryo_static_hedgehog",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.cryo_static_hedgehog.CryoStaticHedgehogEntity>of(
                                    ddraig.net.entropica.entity.cryo_static_hedgehog.CryoStaticHedgehogEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.7F, 0.65F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_static_hedgehog"))));

    // --- BARROW HOUND (FAUNA 25) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.barrow_hound.BarrowHoundEntity>> BARROW_HOUND =
            ENTITY_TYPES.register("barrow_hound",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.barrow_hound.BarrowHoundEntity>of(
                                    ddraig.net.entropica.entity.barrow_hound.BarrowHoundEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.9F, 1.05F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_hound"))));

    // --- BARROW CRYPT-GLOW OWL (FAUNA 9) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.barrow_crypt_glow_owl.BarrowCryptGlowOwlEntity>> BARROW_CRYPT_GLOW_OWL =
            ENTITY_TYPES.register("barrow_crypt_glow_owl",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.barrow_crypt_glow_owl.BarrowCryptGlowOwlEntity>of(
                                    ddraig.net.entropica.entity.barrow_crypt_glow_owl.BarrowCryptGlowOwlEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.75F, 0.95F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_crypt_glow_owl"))));

    // --- VOID-RIFT SHADE (FAUNA 42) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.void_rift_shade.VoidRiftShadeEntity>> VOID_RIFT_SHADE =
            ENTITY_TYPES.register("void_rift_shade",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.void_rift_shade.VoidRiftShadeEntity>of(
                                    ddraig.net.entropica.entity.void_rift_shade.VoidRiftShadeEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.95F, 3.6F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_rift_shade"))));

    // --- CRYSTALLINE QILIN (BIOME 10 GLOW GUARDIAN) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.crystalline_qilin.CrystallineQilinEntity>> CRYSTALLINE_QILIN =
            ENTITY_TYPES.register("crystalline_qilin",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.crystalline_qilin.CrystallineQilinEntity>of(
                                    ddraig.net.entropica.entity.crystalline_qilin.CrystallineQilinEntity::new,
                                    MobCategory.CREATURE)
                            .sized(1.35F, 2.15F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crystalline_qilin"))));

    // --- RADIANT SNOWBALL (BIOME 10 PROJECTILE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.RadiantSnowballEntity>> RADIANT_SNOWBALL =
            ENTITY_TYPES.register("radiant_snowball",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.RadiantSnowballEntity>of(
                                    ddraig.net.entropica.entity.projectile.RadiantSnowballEntity::new,
                                    MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_snowball"))));

    // --- SANDSTONE GOLEM (FAUNA 39 — DESERT ELEMENTAL) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.sandstone_golem.SandstoneGolemEntity>> SANDSTONE_GOLEM =
            ENTITY_TYPES.register("sandstone_golem",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.sandstone_golem.SandstoneGolemEntity>of(
                                    ddraig.net.entropica.entity.sandstone_golem.SandstoneGolemEntity::new,
                                    MobCategory.CREATURE)
                            .sized(1.45F, 1.55F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sandstone_golem"))));

    // --- PEAT-MUD KELPIE (FAUNA 37 — SWAMP KELPIE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.peat_mud_kelpie.PeatMudKelpieEntity>> PEAT_MUD_KELPIE =
            ENTITY_TYPES.register("peat_mud_kelpie",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.peat_mud_kelpie.PeatMudKelpieEntity>of(
                                    ddraig.net.entropica.entity.peat_mud_kelpie.PeatMudKelpieEntity::new,
                                    MobCategory.CREATURE)
                            .sized(1.15F, 1.55F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "peat_mud_kelpie"))));

    // --- SILT-SPHINX (FAUNA 40 — CANYON GUARDIAN) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.silt_sphinx.SiltSphinxEntity>> SILT_SPHINX =
            ENTITY_TYPES.register("silt_sphinx",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.silt_sphinx.SiltSphinxEntity>of(
                                    ddraig.net.entropica.entity.silt_sphinx.SiltSphinxEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.35F, 1.50F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silt_sphinx"))));

    // --- ZEPHYR-SIMURGH (FAUNA 41 — WIND LORD) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.zephyr_simurgh.ZephyrSimurghEntity>> ZEPHYR_SIMURGH =
            ENTITY_TYPES.register("zephyr_simurgh",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.zephyr_simurgh.ZephyrSimurghEntity>of(
                                    ddraig.net.entropica.entity.zephyr_simurgh.ZephyrSimurghEntity::new,
                                    MobCategory.CREATURE)
                            .sized(1.15F, 1.50F)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_simurgh"))));

    // --- MIRAGE FATA-SPRITE (FAUNA 43 — DESERT SYLPH) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataSpriteEntity>> MIRAGE_FATA_SPRITE =
            ENTITY_TYPES.register("mirage_fata_sprite",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataSpriteEntity>of(
                                    ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataSpriteEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.95F, 1.15F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_fata_sprite"))));

    // --- MIRAGE FATA-SPRITE AFTERIMAGE (MIRROR ILLUSION DUPLICATE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataAfterimageEntity>> MIRAGE_FATA_AFTERIMAGE =
            ENTITY_TYPES.register("mirage_fata_afterimage",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataAfterimageEntity>of(
                                    ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataAfterimageEntity::new,
                                    MobCategory.MISC)
                            .sized(0.95F, 1.15F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_fata_afterimage"))));

    // --- MAGMA CHERUFE (FAUNA 44 — LAVA LIZARD) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.magma_cherufe.MagmaCherufeEntity>> MAGMA_CHERUFE =
            ENTITY_TYPES.register("magma_cherufe",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.magma_cherufe.MagmaCherufeEntity>of(
                                    ddraig.net.entropica.entity.magma_cherufe.MagmaCherufeEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.55F, 1.05F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "magma_cherufe"))));

    // --- SOULFIRE PHENEX (FAUNA 45 — GOETIC FLYER) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.soulfire_phenex.SoulfirePhenexEntity>> SOULFIRE_PHENEX =
            ENTITY_TYPES.register("soulfire_phenex",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.soulfire_phenex.SoulfirePhenexEntity>of(
                                    ddraig.net.entropica.entity.soulfire_phenex.SoulfirePhenexEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.95F, 1.65F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "soulfire_phenex"))));

    // --- VOID ARACHNE (FAUNA 46 — NETHER SPINNER) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.void_arachne.VoidArachneEntity>> VOID_ARACHNE =
            ENTITY_TYPES.register("void_arachne",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.void_arachne.VoidArachneEntity>of(
                                    ddraig.net.entropica.entity.void_arachne.VoidArachneEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.35F, 1.78F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_arachne"))));

    // --- ASH BARROW-GNOME (FAUNA 47 — UNDERGROUND DIGGER) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.ash_barrow_gnome.AshBarrowGnomeEntity>> ASH_BARROW_GNOME =
            ENTITY_TYPES.register("ash_barrow_gnome",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.ash_barrow_gnome.AshBarrowGnomeEntity>of(
                                    ddraig.net.entropica.entity.ash_barrow_gnome.AshBarrowGnomeEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.80F, 1.32F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "ash_barrow_gnome"))));

    // --- ORTHRUS MAGMA-BEAST (FAUNA 48 — TWO-HEADED HOUND) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.orthrus_magma_beast.OrthrusMagmaBeastEntity>> ORTHRUS_MAGMA_BEAST =
            ENTITY_TYPES.register("orthrus_magma_beast",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.orthrus_magma_beast.OrthrusMagmaBeastEntity>of(
                                    ddraig.net.entropica.entity.orthrus_magma_beast.OrthrusMagmaBeastEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.65F, 2.25F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "orthrus_magma_beast"))));

    // --- ASH-CINDER WISP (FAUNA 49 — WILL-O'-WISP) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.ash_cinder_wisp.AshCinderWispEntity>> ASH_CINDER_WISP =
            ENTITY_TYPES.register("ash_cinder_wisp",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.ash_cinder_wisp.AshCinderWispEntity>of(
                                    ddraig.net.entropica.entity.ash_cinder_wisp.AshCinderWispEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.95F, 1.45F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "ash_cinder_wisp"))));

    // --- SOUL-SAND HYDRA (FAUNA 50 — MULTI-HEADED SERPENTINE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.soul_sand_hydra.SoulSandHydraEntity>> SOUL_SAND_HYDRA =
            ENTITY_TYPES.register("soul_sand_hydra",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.soul_sand_hydra.SoulSandHydraEntity>of(
                                    ddraig.net.entropica.entity.soul_sand_hydra.SoulSandHydraEntity::new,
                                    MobCategory.MONSTER)
                            .sized(2.15F, 2.55F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "soul_sand_hydra"))));

    // --- VOID SHADOW-GARGOYLE (FAUNA 51 — RIFT SENTINEL) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.void_shadow_gargoyle.VoidShadowGargoyleEntity>> VOID_SHADOW_GARGOYLE =
            ENTITY_TYPES.register("void_shadow_gargoyle",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.void_shadow_gargoyle.VoidShadowGargoyleEntity>of(
                                    ddraig.net.entropica.entity.void_shadow_gargoyle.VoidShadowGargoyleEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.15F, 1.95F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_shadow_gargoyle"))));

    // --- BARROW WIGHT GOLEM (FAUNA 52 — CRYPT SENTINEL) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.barrow_wight_golem.BarrowWightGolemEntity>> BARROW_WIGHT_GOLEM =
            ENTITY_TYPES.register("barrow_wight_golem",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.barrow_wight_golem.BarrowWightGolemEntity>of(
                                    ddraig.net.entropica.entity.barrow_wight_golem.BarrowWightGolemEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.35F, 2.35F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_wight_golem"))));

    // --- OBSIDIAN SCORPIOID (FAUNA 53 — LAVA BEAST) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.obsidian_scorpioid.ObsidianScorpioidEntity>> OBSIDIAN_SCORPIOID =
            ENTITY_TYPES.register("obsidian_scorpioid",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.obsidian_scorpioid.ObsidianScorpioidEntity>of(
                                    ddraig.net.entropica.entity.obsidian_scorpioid.ObsidianScorpioidEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.65F, 1.55F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_scorpioid"))));

    // --- BRIMSTONE WYVERN (FAUNA 54 — LAVA DRAKE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.brimstone_wyvern.BrimstoneWyvernEntity>> BRIMSTONE_WYVERN =
            ENTITY_TYPES.register("brimstone_wyvern",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.brimstone_wyvern.BrimstoneWyvernEntity>of(
                                    ddraig.net.entropica.entity.brimstone_wyvern.BrimstoneWyvernEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.65F, 2.05F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "brimstone_wyvern"))));

    // --- OBSIDIAN GOLEM (FAUNA 55 — NETHER SENTINEL) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.obsidian_golem.ObsidianGolemEntity>> OBSIDIAN_GOLEM =
            ENTITY_TYPES.register("obsidian_golem",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.obsidian_golem.ObsidianGolemEntity>of(
                                    ddraig.net.entropica.entity.obsidian_golem.ObsidianGolemEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.55F, 2.85F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_golem"))));

    // --- SOULFIRE DJINN (FAUNA 57 — DESERT EFREET) ---
    // --- SOULFIRE DJINN (FAUNA 57 — DESERT EFREET) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.soulfire_djinn.SoulfireDjinnEntity>> SOULFIRE_DJINN =
            ENTITY_TYPES.register("soulfire_djinn",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.soulfire_djinn.SoulfireDjinnEntity>of(
                                    ddraig.net.entropica.entity.soulfire_djinn.SoulfireDjinnEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.95F, 2.05F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "soulfire_djinn"))));

    // --- BRIMSTONE CHIMERA (FAUNA 58 — LAVA BEAST) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.brimstone_chimera.BrimstoneChimeraEntity>> BRIMSTONE_CHIMERA =
            ENTITY_TYPES.register("brimstone_chimera",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.brimstone_chimera.BrimstoneChimeraEntity>of(
                                    ddraig.net.entropica.entity.brimstone_chimera.BrimstoneChimeraEntity::new,
                                    MobCategory.MONSTER)
                            .sized(1.55F, 1.85F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "brimstone_chimera"))));

    // --- OBSIDIAN WENDIGO (FAUNA 59 — FOREST FIEND) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.obsidian_wendigo.ObsidianWendigoEntity>> OBSIDIAN_WENDIGO =
            ENTITY_TYPES.register("obsidian_wendigo",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.obsidian_wendigo.ObsidianWendigoEntity>of(
                                    ddraig.net.entropica.entity.obsidian_wendigo.ObsidianWendigoEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.95F, 3.25F)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_wendigo"))));

    // --- MYCELIAL SPORE-WEAVER (FAUNA 23 — BIOME 12 FUNGAL GRAZER) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.mycelial_spore_weaver.MycelialSporeWeaverEntity>> MYCELIAL_SPORE_WEAVER =
            ENTITY_TYPES.register("mycelial_spore_weaver",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.mycelial_spore_weaver.MycelialSporeWeaverEntity>of(
                                    ddraig.net.entropica.entity.mycelial_spore_weaver.MycelialSporeWeaverEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.95F, 1.15F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mycelial_spore_weaver"))));

    // --- GLAZED SPORE-BOMB (BIOME 12 PROJECTILE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.GlazedSporeBombEntity>> GLAZED_SPORE_BOMB =
            ENTITY_TYPES.register("glazed_spore_bomb",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.GlazedSporeBombEntity>of(
                                    ddraig.net.entropica.entity.projectile.GlazedSporeBombEntity::new,
                                    MobCategory.MISC)
                            .sized(0.3F, 0.3F)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_spore_bomb"))));

    // --- SILICA GLASS-SCARAB (FAUNA 13 — BIOME 13 SOLAR SCARAB) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.silica_glass_scarab.SilicaGlassScarabEntity>> SILICA_GLASS_SCARAB =
            ENTITY_TYPES.register("silica_glass_scarab",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.silica_glass_scarab.SilicaGlassScarabEntity>of(
                                    ddraig.net.entropica.entity.silica_glass_scarab.SilicaGlassScarabEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.85F, 0.55F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silica_glass_scarab"))));

    // --- MIRAGE DECOY PRISM (BIOME 13 PROJECTILE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.MirageDecoyPrismEntity>> MIRAGE_DECOY_PRISM =
            ENTITY_TYPES.register("mirage_decoy_prism",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.MirageDecoyPrismEntity>of(
                                    ddraig.net.entropica.entity.projectile.MirageDecoyPrismEntity::new,
                                    MobCategory.MISC)
                            .sized(0.3F, 0.3F)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_decoy_prism"))));

    // --- MUD-LURKER SALAMANDER (FAUNA 33 — BIOME 14 AMPHIBIOUS CRAWLER) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.mud_lurker_salamander.MudLurkerSalamanderEntity>> MUD_LURKER_SALAMANDER =
            ENTITY_TYPES.register("mud_lurker_salamander",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.mud_lurker_salamander.MudLurkerSalamanderEntity>of(
                                    ddraig.net.entropica.entity.mud_lurker_salamander.MudLurkerSalamanderEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.95F, 0.55F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mud_lurker_salamander"))));

    // --- DUST-CORE GOLEM (FAUNA 14 — BIOME 15 SILT EXCAVATOR) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.dust_core_golem.DustCoreGolemEntity>> DUST_CORE_GOLEM =
            ENTITY_TYPES.register("dust_core_golem",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.dust_core_golem.DustCoreGolemEntity>of(
                                    ddraig.net.entropica.entity.dust_core_golem.DustCoreGolemEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.95F, 1.35F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dust_core_golem"))));

    // --- SANGUINE SEA-ANGEL (BIOME 16 CRIMSON TIDE-SKERRIES PREDATOR) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelEntity>> SANGUINE_SEA_ANGEL =
            ENTITY_TYPES.register("sanguine_sea_angel",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelEntity>of(
                                    ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.7F, 0.9F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_sea_angel"))));

    // --- BLOOD-FROST DART (BIOME 16 PROJECTILE) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.projectile.BloodFrostDartEntity>> BLOOD_FROST_DART =
            ENTITY_TYPES.register("blood_frost_dart",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.projectile.BloodFrostDartEntity>of(
                                    ddraig.net.entropica.entity.projectile.BloodFrostDartEntity::new,
                                    MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blood_frost_dart"))));

    // --- STARLIGHT NAUTILUS (BIOME 17 ASTRAL SKERRIES FLYING MOUNT) ---
    public static final RegistrySupplier<EntityType<ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusEntity>> STARLIGHT_NAUTILUS =
            ENTITY_TYPES.register("starlight_nautilus",
                    () -> EntityType.Builder.<ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusEntity>of(
                                    ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusEntity::new,
                                    MobCategory.CREATURE)
                            .sized(0.95F, 0.95F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                     ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus"))));
}
