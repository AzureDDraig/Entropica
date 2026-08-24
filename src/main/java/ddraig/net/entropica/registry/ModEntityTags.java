package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class ModEntityTags {

    public static final TagKey<EntityType<?>> DROPS_UNDEAD_ESSENCE = create("drops_undead_essence");
    public static final TagKey<EntityType<?>> DROPS_WATER_ESSENCE = create("drops_water_essence");
    public static final TagKey<EntityType<?>> DROPS_NETHER_ESSENCE = create("drops_nether_essence");
    public static final TagKey<EntityType<?>> DROPS_CHIMERA_ESSENCE = create("drops_chimera_essence");
    public static final TagKey<EntityType<?>> DROPS_ARID_ESSENCE = create("drops_arid_essence");
    public static final TagKey<EntityType<?>> DROPS_FROZEN_ESSENCE = create("drops_frozen_essence");
    public static final TagKey<EntityType<?>> DROPS_VOID_ESSENCE = create("drops_void_essence");
    public static final TagKey<EntityType<?>> DROPS_AIR_ESSENCE = create("drops_air_essence");
    public static final TagKey<EntityType<?>> DROPS_EARTH_ESSENCE = create("drops_earth_essence");
    public static final TagKey<EntityType<?>> DROPS_NATURE_ESSENCE = create("drops_nature_essence");
    public static final TagKey<EntityType<?>> DROPS_RADIANT_ESSENCE = create("drops_radiant_essence");
    public static final TagKey<EntityType<?>> DROPS_LIGHTNING_ESSENCE = create("drops_lightning_essence");
    public static final TagKey<EntityType<?>> DROPS_UMBRAL_ESSENCE = create("drops_umbral_essence");

    private static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Entropica.MODID, name));
    }
}