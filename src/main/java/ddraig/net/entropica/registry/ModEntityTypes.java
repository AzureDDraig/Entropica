package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.EssenceNodeEntity;
import ddraig.net.entropica.entity.EssenceOrbEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Entropica.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EssenceOrbEntity>> ESSENCE_ORB =
            ENTITY_TYPES.register("essence_orb",
                    () -> EntityType.Builder.<EssenceOrbEntity>of(EssenceOrbEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_orb"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EssenceNodeEntity>> ESSENCE_NODE =
            ENTITY_TYPES.register("essence_node",
                    () -> EntityType.Builder.<EssenceNodeEntity>of(EssenceNodeEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_node"))));
}