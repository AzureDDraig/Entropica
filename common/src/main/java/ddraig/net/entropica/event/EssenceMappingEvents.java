package ddraig.net.entropica.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.ItemEssenceMap;
import ddraig.net.entropica.api.ItemEssenceMap.EssenceValue;
import ddraig.net.entropica.registry.ModItems;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class EssenceMappingEvents {
    public static void register() {
        LifecycleEvent.SETUP.register(() -> {
            registerBiome8Essences();
            registerBiome9Essences();
            registerBiome10Essences();
            registerBiome11Essences();
            registerBiome16Essences();
            registerBiome17Essences();
        });
        LifecycleEvent.SERVER_STARTED.register(server -> {
            registerBiome8Essences();
            registerBiome9Essences();
            registerBiome10Essences();
            registerBiome11Essences();
            registerBiome16Essences();
            registerBiome17Essences();
            ItemEssenceMap.buildEssenceGraph(server.getRecipeManager().getRecipes(), server.registryAccess());
        });
    }

    private static void registerIfBound(RegistrySupplier<? extends Item> supplier, EssenceValue... values) {
        if (supplier != null && supplier.isBound()) {
            ItemEssenceMap.register(supplier.get(), values);
        }
    }

    private static void registerByIdIfPresent(String itemId, EssenceValue... values) {
        BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, itemId))
                .ifPresent(item -> ItemEssenceMap.register(item, values));
    }

    /**
     * Registers Materia essence values (Barrow, Glacial, Aegis) for Biome #8 (2.8 Barrow-Mound Tundra)
     * blocks, flora, fauna reagents, armor, and relics.
     */
    public static void registerBiome8Essences() {
        // Natural Terrain, Masonry & Flora Blocks
        registerIfBound(ModItems.NECROTIC_SOIL_ITEM,
                new EssenceValue(EssenceType.BARROW, 1.0f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.BARROW_STONE_BRICKS_ITEM,
                new EssenceValue(EssenceType.BARROW, 1.0f),
                new EssenceValue(EssenceType.AEGIS, 0.25f));
        registerIfBound(ModItems.CRACKED_BARROW_BRICKS_ITEM,
                new EssenceValue(EssenceType.BARROW, 1.0f));
        registerIfBound(ModItems.MOSSY_BARROW_BRICKS_ITEM,
                new EssenceValue(EssenceType.BARROW, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));
        registerIfBound(ModItems.GRAVE_MOSS_ITEM,
                new EssenceValue(EssenceType.BARROW, 1.0f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));
        registerIfBound(ModItems.CRYPT_GRAVE_ROOT_ITEM,
                new EssenceValue(EssenceType.BARROW, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.RIME_CAP_MUSHROOM_ITEM,
                new EssenceValue(EssenceType.GLACIAL, 1.0f),
                new EssenceValue(EssenceType.BARROW, 1.0f));

        // Tomb Warding Architecture
        registerIfBound(ModItems.RUNED_BRAZIER_ITEM,
                new EssenceValue(EssenceType.BARROW, 3.0f),
                new EssenceValue(EssenceType.AEGIS, 1.5f));
        registerIfBound(ModItems.CRYPT_SIPHON_LANTERN_ITEM,
                new EssenceValue(EssenceType.BARROW, 3.0f),
                new EssenceValue(EssenceType.AEGIS, 2.0f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f));
        registerIfBound(ModItems.BARROW_SENTINEL_TOTEM_ITEM,
                new EssenceValue(EssenceType.BARROW, 4.0f),
                new EssenceValue(EssenceType.AEGIS, 2.5f));

        // Reagents, Fauna Drops & Armor
        registerIfBound(ModItems.RIME_CAP_SPORES,
                new EssenceValue(EssenceType.GLACIAL, 1.0f),
                new EssenceValue(EssenceType.BARROW, 0.75f));
        registerIfBound(ModItems.CRYPT_GRAVE_FIBERS,
                new EssenceValue(EssenceType.BARROW, 1.0f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));
        registerIfBound(ModItems.BLEACHED_BONE_MEAL,
                new EssenceValue(EssenceType.BARROW, 0.75f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));
        registerIfBound(ModItems.GRAVE_GLOW_FEATHER,
                new EssenceValue(EssenceType.BARROW, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f),
                new EssenceValue(EssenceType.AEGIS, 0.5f));
        registerIfBound(ModItems.BARROW_SKULL_PLATE,
                new EssenceValue(EssenceType.BARROW, 2.5f),
                new EssenceValue(EssenceType.AEGIS, 1.5f));
        registerIfBound(ModItems.BARROW_SLATE_GREAVES,
                new EssenceValue(EssenceType.BARROW, 14.0f),
                new EssenceValue(EssenceType.AEGIS, 8.0f),
                new EssenceValue(EssenceType.GLACIAL, 2.0f));
    }

    /**
     * Registers Materia essence values (Null-U, Void, Glacial) for Biome #9 (2.9 Null-Fringe Bay)
     * blocks, crystal formations, flora, fauna reagents, and spatial gear.
     */
    public static void registerBiome9Essences() {
        // Natural Terrain, Masonry & Crystal Blocks
        registerIfBound(ModItems.VOID_SAND_ITEM,
                new EssenceValue(EssenceType.VOID, 0.75f),
                new EssenceValue(EssenceType.NULL_U, 0.5f));
        registerIfBound(ModItems.VOID_SAND_TURF_ITEM,
                new EssenceValue(EssenceType.VOID, 1.0f),
                new EssenceValue(EssenceType.NULL_U, 0.75f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));
        registerIfBound(ModItems.VOID_STONE_ITEM,
                new EssenceValue(EssenceType.VOID, 1.0f),
                new EssenceValue(EssenceType.NULL_U, 0.5f));
        registerIfBound(ModItems.OBSIDIAN_BRICKS_ITEM,
                new EssenceValue(EssenceType.VOID, 2.0f),
                new EssenceValue(EssenceType.NULL_U, 1.0f));
        registerIfBound(ModItems.VOID_CRYSTAL_BLOCK_ITEM,
                new EssenceValue(EssenceType.NULL_U, 4.0f),
                new EssenceValue(EssenceType.VOID, 4.0f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f));
        registerIfBound(ModItems.VOID_CRYSTAL_BUD_ITEM,
                new EssenceValue(EssenceType.NULL_U, 1.0f),
                new EssenceValue(EssenceType.VOID, 1.0f));
        registerIfBound(ModItems.VOID_CRYSTAL_CLUSTER_ITEM,
                new EssenceValue(EssenceType.NULL_U, 3.0f),
                new EssenceValue(EssenceType.VOID, 3.0f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.NULL_FRINGE_TUSSOCK_ITEM,
                new EssenceValue(EssenceType.VOID, 1.0f),
                new EssenceValue(EssenceType.NULL_U, 0.5f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));

        // Spatial Architecture
        registerIfBound(ModItems.SPATIAL_PHASE_ANCHOR_ITEM,
                new EssenceValue(EssenceType.NULL_U, 8.0f),
                new EssenceValue(EssenceType.VOID, 6.0f),
                new EssenceValue(EssenceType.GLACIAL, 2.0f));

        // Reagents, Fauna Drops & Spatial Gear
        registerIfBound(ModItems.VOID_CRYSTAL_SHARD,
                new EssenceValue(EssenceType.NULL_U, 1.0f),
                new EssenceValue(EssenceType.VOID, 1.0f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));
        registerIfBound(ModItems.VOID_FIBER,
                new EssenceValue(EssenceType.VOID, 0.75f),
                new EssenceValue(EssenceType.NULL_U, 0.5f));
        registerIfBound(ModItems.VOID_WEAVE_FABRIC,
                new EssenceValue(EssenceType.VOID, 3.0f),
                new EssenceValue(EssenceType.NULL_U, 2.0f));
        registerIfBound(ModItems.SERPENT_VOID_SCALE,
                new EssenceValue(EssenceType.VOID, 3.5f),
                new EssenceValue(EssenceType.NULL_U, 2.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f));
        registerIfBound(ModItems.PHASE_RECALL_STONE,
                new EssenceValue(EssenceType.NULL_U, 8.0f),
                new EssenceValue(EssenceType.VOID, 6.0f),
                new EssenceValue(EssenceType.GLACIAL, 1.5f));
        registerIfBound(ModItems.LEVIATHAN_RIFT_BELT,
                new EssenceValue(EssenceType.VOID, 12.0f),
                new EssenceValue(EssenceType.NULL_U, 10.0f),
                new EssenceValue(EssenceType.GLACIAL, 3.0f));
        registerIfBound(ModItems.NULL_WEAVE_MANTLE,
                new EssenceValue(EssenceType.VOID, 14.0f),
                new EssenceValue(EssenceType.NULL_U, 10.0f),
                new EssenceValue(EssenceType.GLACIAL, 2.0f));
    }

    /**
     * Registers Materia essence values (Vitae, Radiant, Aurora, Glacial) for Biome #10
     * (2.10 Sun-Glazed Sanctuary) blocks, flora, fauna reagents, and sanctuary relics.
     */
    public static void registerBiome10Essences() {
        // Natural Terrain, Canopy & Spring Blocks
        registerIfBound(ModItems.RADIANT_SNOW_BLOCK_ITEM,
                new EssenceValue(EssenceType.RADIANT, 1.0f),
                new EssenceValue(EssenceType.VITAE, 0.5f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.RADIANT_GLAZE_ICE_ITEM,
                new EssenceValue(EssenceType.RADIANT, 1.5f),
                new EssenceValue(EssenceType.AURORA, 1.0f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f));
        registerIfBound(ModItems.GLAZED_AMBER_LEAVES_ITEM,
                new EssenceValue(EssenceType.VITAE, 0.75f),
                new EssenceValue(EssenceType.AMBER, 0.75f),
                new EssenceValue(EssenceType.RADIANT, 0.5f));
        registerIfBound(ModItems.GOLDEN_LICHEN_ITEM,
                new EssenceValue(EssenceType.VITAE, 1.0f),
                new EssenceValue(EssenceType.RADIANT, 0.75f));
        registerIfBound(ModItems.VITAE_SPRING_STONE_ITEM,
                new EssenceValue(EssenceType.VITAE, 2.0f),
                new EssenceValue(EssenceType.RADIANT, 1.0f),
                new EssenceValue(EssenceType.AURORA, 0.5f));

        // Sanctuary Architecture
        registerIfBound(ModItems.QILIN_LIGHT_REFRACTOR_ITEM,
                new EssenceValue(EssenceType.RADIANT, 6.0f),
                new EssenceValue(EssenceType.VITAE, 4.0f),
                new EssenceValue(EssenceType.AURORA, 3.0f));
        registerIfBound(ModItems.SUN_GLAZED_LANTERN_ITEM,
                new EssenceValue(EssenceType.RADIANT, 3.0f),
                new EssenceValue(EssenceType.VITAE, 1.5f),
                new EssenceValue(EssenceType.AURORA, 1.0f));

        // Reagents, Fauna Drops & Sanctuary Relics
        registerIfBound(ModItems.RADIANT_SNOWBALL,
                new EssenceValue(EssenceType.RADIANT, 0.25f),
                new EssenceValue(EssenceType.VITAE, 0.125f),
                new EssenceValue(EssenceType.GLACIAL, 0.125f));
        registerIfBound(ModItems.JADE_QILIN_SCALE,
                new EssenceValue(EssenceType.VITAE, 3.5f),
                new EssenceValue(EssenceType.RADIANT, 3.0f),
                new EssenceValue(EssenceType.AURORA, 2.0f));
        registerIfBound(ModItems.LIQUID_VITAE_RESIN,
                new EssenceValue(EssenceType.VITAE, 4.0f),
                new EssenceValue(EssenceType.AMBER, 2.0f),
                new EssenceValue(EssenceType.RADIANT, 1.5f));
        registerIfBound(ModItems.VITAE_FERTILIZER,
                new EssenceValue(EssenceType.VITAE, 1.5f),
                new EssenceValue(EssenceType.RADIANT, 0.5f));
        registerIfBound(ModItems.QILIN_PRISM_CIRCLET,
                new EssenceValue(EssenceType.VITAE, 12.0f),
                new EssenceValue(EssenceType.RADIANT, 10.0f),
                new EssenceValue(EssenceType.AURORA, 6.0f));
        registerIfBound(ModItems.SUN_GLAZED_TALISMAN,
                new EssenceValue(EssenceType.VITAE, 12.0f),
                new EssenceValue(EssenceType.RADIANT, 10.0f),
                new EssenceValue(EssenceType.AURORA, 4.0f));
        registerIfBound(ModItems.WEEPING_AMBER_TEAR,
                new EssenceValue(EssenceType.VITAE, 2.0f),
                new EssenceValue(EssenceType.AMBER, 2.0f),
                new EssenceValue(EssenceType.RADIANT, 1.0f));
        registerIfBound(ModItems.LUMINOTH_DUST,
                new EssenceValue(EssenceType.RADIANT, 1.5f),
                new EssenceValue(EssenceType.AURORA, 1.5f));
        registerIfBound(ModItems.AURORAFOWL_FEATHER,
                new EssenceValue(EssenceType.AURORA, 1.5f),
                new EssenceValue(EssenceType.AIR, 0.5f));
        registerIfBound(ModItems.RAW_AURORAFOWL,
                new EssenceValue(EssenceType.VITAE, 0.75f),
                new EssenceValue(EssenceType.AURORA, 0.75f));
        registerIfBound(ModItems.COOKED_AURORAFOWL,
                new EssenceValue(EssenceType.VITAE, 1.25f),
                new EssenceValue(EssenceType.AURORA, 1.0f));

        // Fauna Bestiary Pass: Materia Mappings
        registerIfBound(ModItems.HARD_SANDSTONE_SHARD,
                new EssenceValue(EssenceType.DUST, 4.0f),
                new EssenceValue(EssenceType.EARTH, 2.5f),
                new EssenceValue(EssenceType.ARID, 2.0f));
        registerIfBound(ModItems.KELPIE_WATER_PELT,
                new EssenceValue(EssenceType.ABYSS, 4.0f),
                new EssenceValue(EssenceType.WATER, 2.5f),
                new EssenceValue(EssenceType.UMBRAL, 2.0f));
        registerIfBound(ModItems.SPHINX_SAND_STONE,
                new EssenceValue(EssenceType.BARROW, 4.5f),
                new EssenceValue(EssenceType.EARTH, 2.5f),
                new EssenceValue(EssenceType.ARID, 2.0f));
        registerIfBound(ModItems.SIMURGH_FEATHER,
                new EssenceValue(EssenceType.STORM, 4.5f),
                new EssenceValue(EssenceType.AIR, 2.5f),
                new EssenceValue(EssenceType.LIGHTNING, 2.0f));
        registerIfBound(ModItems.SHIMMERING_MIRAGE_AURA,
                new EssenceValue(EssenceType.MIRAGE, 4.5f),
                new EssenceValue(EssenceType.RADIANT, 2.5f),
                new EssenceValue(EssenceType.ARID, 2.0f));
    }

    /**
     * Registers Materia essence values (Storm, Glacial, Air, Lightning) for Biome #11
     * (2.11 Storm-Swept Icebergs) blocks, flora, fauna drops, and gale equipment.
     */
    public static void registerBiome11Essences() {
        registerIfBound(ModItems.FULGURITE_BLOCK_ITEM,
                new EssenceValue(EssenceType.STORM, 1.5f),
                new EssenceValue(EssenceType.LIGHTNING, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.ZEPHYR_ICE_ITEM,
                new EssenceValue(EssenceType.STORM, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.5f),
                new EssenceValue(EssenceType.AIR, 1.0f));
        registerIfBound(ModItems.ZEPHYR_GLASS_GRASS_ITEM,
                new EssenceValue(EssenceType.STORM, 1.0f),
                new EssenceValue(EssenceType.AIR, 0.75f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.ZEPHYR_UPDRAFT_GRATE_ITEM,
                new EssenceValue(EssenceType.STORM, 4.0f),
                new EssenceValue(EssenceType.AIR, 3.0f),
                new EssenceValue(EssenceType.GLACIAL, 1.5f));
        registerIfBound(ModItems.SIMURGH_WIND_TURBINE_ITEM,
                new EssenceValue(EssenceType.STORM, 8.0f),
                new EssenceValue(EssenceType.AIR, 5.0f),
                new EssenceValue(EssenceType.LIGHTNING, 3.0f));
        registerIfBound(ModItems.ZEPHYR_GLASS_FIBER,
                new EssenceValue(EssenceType.STORM, 1.0f),
                new EssenceValue(EssenceType.AIR, 0.75f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));
        registerIfBound(ModItems.STORM_KITE_MEMBRANE,
                new EssenceValue(EssenceType.STORM, 2.5f),
                new EssenceValue(EssenceType.AIR, 1.5f),
                new EssenceValue(EssenceType.LIGHTNING, 1.0f));
        registerIfBound(ModItems.HYDRA_GLACIAL_HORN,
                new EssenceValue(EssenceType.GLACIAL, 3.5f),
                new EssenceValue(EssenceType.STORM, 2.5f),
                new EssenceValue(EssenceType.FROZEN, 1.5f));
        registerIfBound(ModItems.STORM_KITE_GLIDER,
                new EssenceValue(EssenceType.STORM, 10.0f),
                new EssenceValue(EssenceType.AIR, 6.0f),
                new EssenceValue(EssenceType.LIGHTNING, 3.0f));
        registerIfBound(ModItems.CRYO_TEMPEST_HORN,
                new EssenceValue(EssenceType.GLACIAL, 8.0f),
                new EssenceValue(EssenceType.STORM, 6.0f),
                new EssenceValue(EssenceType.AIR, 3.0f));
        registerIfBound(ModItems.TEMPEST_BOTTLE,
                new EssenceValue(EssenceType.STORM, 2.5f),
                new EssenceValue(EssenceType.AIR, 2.0f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f));
    }

    /**
     * Registers Materia essence values (Blood, Glacial, Vitae) for Biome #16
     * (2.16 Blood-Ice Shallows) blocks, flora, fauna drops, and equipment.
     */
    public static void registerBiome16Essences() {
        registerIfBound(ModItems.SANGUINE_ICE_ITEM,
                new EssenceValue(EssenceType.BLOOD, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.5f));
        registerIfBound(ModItems.RED_CLAY_SAND_ITEM,
                new EssenceValue(EssenceType.BLOOD, 0.75f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.SANGUINE_CLAY_BRICKS_ITEM,
                new EssenceValue(EssenceType.BLOOD, 0.75f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.SANGUINE_CLAY_BRICK_STAIRS_ITEM,
                new EssenceValue(EssenceType.BLOOD, 0.75f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f));
        registerIfBound(ModItems.SANGUINE_CLAY_BRICK_SLAB_ITEM,
                new EssenceValue(EssenceType.BLOOD, 0.375f),
                new EssenceValue(EssenceType.GLACIAL, 0.25f));
        registerIfBound(ModItems.RED_ALGAE_ITEM,
                new EssenceValue(EssenceType.BLOOD, 1.0f),
                new EssenceValue(EssenceType.GLACIAL, 0.5f),
                new EssenceValue(EssenceType.VITAE, 0.75f));
        registerIfBound(ModItems.CRIMSON_TIDE_LANTERN_ITEM,
                new EssenceValue(EssenceType.BLOOD, 6.0f),
                new EssenceValue(EssenceType.GLACIAL, 4.0f));
        registerIfBound(ModItems.SANGUINE_DRAUGHT_AMPOULE,
                new EssenceValue(EssenceType.BLOOD, 4.0f),
                new EssenceValue(EssenceType.GLACIAL, 2.0f),
                new EssenceValue(EssenceType.VITAE, 2.5f));
        registerIfBound(ModItems.SANGUINE_ICE_LANCET,
                new EssenceValue(EssenceType.BLOOD, 6.0f),
                new EssenceValue(EssenceType.GLACIAL, 3.0f));
        registerIfBound(ModItems.SANGUINE_HEART_GEL,
                new EssenceValue(EssenceType.BLOOD, 4.0f),
                new EssenceValue(EssenceType.GLACIAL, 2.5f));
    }

    /**
     * Registers Materia essence values (Astral, Glacial, Aurora) for Biome #17
     * (2.17 Astral Skerries) Starlight Nautilus, Star-Fish catches, and cosmic skerries items.
     */
    public static void registerBiome17Essences() {
        // Starlight Nautilus Mount, Nacre & Armor
        registerIfBound(ModItems.STARLIGHT_NAUTILUS_SPAWN_EGG,
                new EssenceValue(EssenceType.ASTRAL, 4.5f),
                new EssenceValue(EssenceType.GLACIAL, 3.0f),
                new EssenceValue(EssenceType.AURORA, 2.0f));
        registerIfBound(ModItems.ASTRAL_NAUTILUS_NACRE,
                new EssenceValue(EssenceType.ASTRAL, 4.0f),
                new EssenceValue(EssenceType.GLACIAL, 2.5f),
                new EssenceValue(EssenceType.AURORA, 1.5f));
        registerIfBound(ModItems.ASTRAL_NAUTILUS_ARMOR,
                new EssenceValue(EssenceType.ASTRAL, 16.0f),
                new EssenceValue(EssenceType.GLACIAL, 10.0f),
                new EssenceValue(EssenceType.AURORA, 6.0f));
        registerIfBound(ModItems.STARDUST_FRAGMENT,
                new EssenceValue(EssenceType.ASTRAL, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 0.75f),
                new EssenceValue(EssenceType.AURORA, 0.5f));

        // 7 Biome-Exclusive Star-Fish Catches
        registerIfBound(ModItems.NEBULA_DARTER,
                new EssenceValue(EssenceType.ASTRAL, 1.0f),
                new EssenceValue(EssenceType.GLACIAL, 0.75f));
        registerIfBound(ModItems.STARWING_FLYING_FISH,
                new EssenceValue(EssenceType.ASTRAL, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f),
                new EssenceValue(EssenceType.AURORA, 0.5f));
        registerIfBound(ModItems.COMET_RIBBONFISH,
                new EssenceValue(EssenceType.ASTRAL, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f),
                new EssenceValue(EssenceType.AURORA, 0.5f));
        registerIfBound(ModItems.STARFALL_RAY,
                new EssenceValue(EssenceType.ASTRAL, 2.0f),
                new EssenceValue(EssenceType.GLACIAL, 1.25f),
                new EssenceValue(EssenceType.AURORA, 0.75f));
        registerIfBound(ModItems.ZENITH_MOONFISH,
                new EssenceValue(EssenceType.ASTRAL, 2.25f),
                new EssenceValue(EssenceType.GLACIAL, 1.5f),
                new EssenceValue(EssenceType.AURORA, 0.75f));
        registerIfBound(ModItems.SUPERNOVA_PUFFER,
                new EssenceValue(EssenceType.ASTRAL, 3.0f),
                new EssenceValue(EssenceType.GLACIAL, 1.5f),
                new EssenceValue(EssenceType.AURORA, 1.25f));
        registerIfBound(ModItems.ASTRAL_LANTERNFISH,
                new EssenceValue(EssenceType.ASTRAL, 4.0f),
                new EssenceValue(EssenceType.GLACIAL, 2.0f),
                new EssenceValue(EssenceType.AURORA, 2.0f));

        // Biome 17 Terrain, Crystal & Orrery Block Items
        registerByIdIfPresent("starlight_ice",
                new EssenceValue(EssenceType.ASTRAL, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.5f),
                new EssenceValue(EssenceType.AURORA, 0.5f));
        registerByIdIfPresent("astral_impactite",
                new EssenceValue(EssenceType.ASTRAL, 1.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.0f));
        registerByIdIfPresent("stardust_crystal_block",
                new EssenceValue(EssenceType.ASTRAL, 6.0f),
                new EssenceValue(EssenceType.GLACIAL, 3.0f),
                new EssenceValue(EssenceType.AURORA, 2.0f));
        registerByIdIfPresent("stardust_crystal_cluster",
                new EssenceValue(EssenceType.ASTRAL, 3.5f),
                new EssenceValue(EssenceType.GLACIAL, 1.5f),
                new EssenceValue(EssenceType.AURORA, 1.0f));
        registerByIdIfPresent("rime_star_tussock",
                new EssenceValue(EssenceType.ASTRAL, 1.0f),
                new EssenceValue(EssenceType.GLACIAL, 0.75f),
                new EssenceValue(EssenceType.AURORA, 0.5f));
        registerByIdIfPresent("celestial_orrery_lantern",
                new EssenceValue(EssenceType.ASTRAL, 6.0f),
                new EssenceValue(EssenceType.GLACIAL, 3.0f),
                new EssenceValue(EssenceType.AURORA, 2.5f));
    }
}
