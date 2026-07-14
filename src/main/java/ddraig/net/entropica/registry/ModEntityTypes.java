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

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Entropica.MODID, Registries.ENTITY_TYPE);

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
}