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
}