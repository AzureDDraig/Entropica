package ddraig.net.entropica.api;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ItemEssenceMap {

    public record EssenceValue(EssenceType type, float amount) {}

    // The O(1) Master Cache that holds the generated Essence Graph!
    private static final Map<Item, List<EssenceValue>> CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, List<EssenceValue>> MANUAL_REGISTRY = new ConcurrentHashMap<>();
    private static volatile boolean modDefaultsInitialized = false;

    private static ItemStack[] ALL_ITEMS_CACHE = null;

    public static void register(Item item, EssenceValue... values) {
        if (item != null && item != Items.AIR) {
            MANUAL_REGISTRY.put(item, List.of(values));
        }
    }

    private static void registerIfBound(RegistrySupplier<? extends Item> supplier, EssenceValue... values) {
        if (supplier != null && supplier.isBound()) {
            register(supplier.get(), values);
        }
    }

    /**
     * Ensures Crucible / Essence mappings for Biome 1 (Blue-Glacier Shelf) flora, crystals,
     * fauna reagents, and relics are registered as soon asModItems are bound.
     */
    public static void ensureModDefaultMappings() {
        if (modDefaultsInitialized) {
            return;
        }
        if (!ModItems.AEGIS_CRYSTAL_SHARD.isBound()
                || !ModItems.AURORA_DUST.isBound()
                || !ModItems.ROT_POWDER.isBound()
                || !ModItems.AEGIS_QUARTZ.isBound()
                || !ModItems.FROZEN_MOSS_CLUMP.isBound()
                || !ModItems.VAPOR_CONDENSER_SPORE.isBound()) {
            return;
        }
        synchronized (ItemEssenceMap.class) {
            if (modDefaultsInitialized) {
                return;
            }
            // Biome 1: Flora & Botanicals
            registerIfBound(ModItems.RIMEBLOOM_ITEM, new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.RIMEBLOOM_PETAL, new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.GLACIAL_HEART_ROOT_ITEM, new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.SHIELDING_BRAMBLE_ITEM, new EssenceValue(EssenceType.AEGIS, 1.0f));
            registerIfBound(ModItems.SHIELDING_THORN_FIBER, new EssenceValue(EssenceType.AEGIS, 0.5f));

            // Biome 1: Aegis Crystals
            registerIfBound(ModItems.AEGIS_CRYSTAL_SHARD, new EssenceValue(EssenceType.AEGIS, 1.0f));
            registerIfBound(ModItems.AEGIS_CRYSTAL_BLOCK_ITEM, new EssenceValue(EssenceType.AEGIS, 4.0f));
            registerIfBound(ModItems.AEGIS_CRYSTAL_BUD_ITEM, new EssenceValue(EssenceType.AEGIS, 1.0f));
            registerIfBound(ModItems.AEGIS_CRYSTAL_CLUSTER_ITEM, new EssenceValue(EssenceType.AEGIS, 2.0f));

            // Biome 1: Fauna Drops & Spire Relics
            registerIfBound(ModItems.FROST_CORE_SHARD, new EssenceValue(EssenceType.GLACIAL, 2.0f));
            registerIfBound(ModItems.INSULATED_GLACIAL_WOOL, new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.GLACIAL_FOCUS_LENS,
                    new EssenceValue(EssenceType.GLACIAL, 4.0f),
                    new EssenceValue(EssenceType.AEGIS, 4.0f));
            registerIfBound(ModItems.RAW_MAMMOT_MEAT,
                    new EssenceValue(EssenceType.GLACIAL, 0.5f),
                    new EssenceValue(EssenceType.NATURE, 0.5f));
            registerIfBound(ModItems.GLACIAL_AEGIS_PLATE,
                    new EssenceValue(EssenceType.GLACIAL, 1.0f),
                    new EssenceValue(EssenceType.AEGIS, 1.0f));
            registerIfBound(ModItems.PATINA_AEGIS_PLATE,
                    new EssenceValue(EssenceType.AEGIS, 1.0f),
                    new EssenceValue(EssenceType.LIGHTNING, 0.5f));

            // Biome 2 (2.2 Auroral Skerries): Natural Terrain & Flora Blocks
            registerIfBound(ModItems.AURORAL_LILY_PAD_ITEM,
                    new EssenceValue(EssenceType.AURORA, 1.0f),
                    new EssenceValue(EssenceType.VAPOR, 1.0f));
            registerIfBound(ModItems.GLIMMER_ICE_ITEM,
                    new EssenceValue(EssenceType.AURORA, 2.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.COARSE_GRAVEL_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 0.5f),
                    new EssenceValue(EssenceType.EARTH, 0.5f));
            registerIfBound(ModItems.AURORAL_FEATHER_MOSS_ITEM,
                    new EssenceValue(EssenceType.AURORA, 1.0f),
                    new EssenceValue(EssenceType.VAPOR, 0.5f));
            registerIfBound(ModItems.MIST_VEIL_FERN_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 1.0f),
                    new EssenceValue(EssenceType.AURORA, 0.5f));

            // Biome 2: Crafted Devices & Wellspring Architecture
            registerIfBound(ModItems.AURORAL_LANTERN_ITEM,
                    new EssenceValue(EssenceType.AURORA, 2.0f),
                    new EssenceValue(EssenceType.VAPOR, 0.5f));
            registerIfBound(ModItems.THERMAL_HEAT_LAMP_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 2.0f),
                    new EssenceValue(EssenceType.AURORA, 1.5f));
            registerIfBound(ModItems.GLOW_GEL_PATHING_ITEM,
                    new EssenceValue(EssenceType.AURORA, 1.0f),
                    new EssenceValue(EssenceType.VAPOR, 0.5f));
            registerIfBound(ModItems.STATIC_REDSTONE_COUPLER_ITEM,
                    new EssenceValue(EssenceType.AURORA, 1.5f),
                    new EssenceValue(EssenceType.VAPOR, 0.5f),
                    new EssenceValue(EssenceType.LIGHTNING, 1.0f));
            registerIfBound(ModItems.AURORAL_LENS_BLOCK_ITEM,
                    new EssenceValue(EssenceType.AURORA, 4.0f),
                    new EssenceValue(EssenceType.VAPOR, 4.0f));

            // Biome 2: Botanicals, Fauna Reagents, Equipment & Relics
            registerIfBound(ModItems.AURORA_DUST,
                    new EssenceValue(EssenceType.AURORA, 1.0f));
            registerIfBound(ModItems.GLOWING_AURORAL_FIBER,
                    new EssenceValue(EssenceType.AURORA, 1.0f),
                    new EssenceValue(EssenceType.VAPOR, 0.25f));
            registerIfBound(ModItems.MIST_VEIL_FROND,
                    new EssenceValue(EssenceType.VAPOR, 1.0f));
            registerIfBound(ModItems.WARM_AURORA_PETAL,
                    new EssenceValue(EssenceType.VAPOR, 1.0f),
                    new EssenceValue(EssenceType.AURORA, 0.5f));
            registerIfBound(ModItems.CAMOUFLAGE_CLOAK,
                    new EssenceValue(EssenceType.VAPOR, 4.0f),
                    new EssenceValue(EssenceType.AURORA, 1.0f));
            registerIfBound(ModItems.AURORAL_GELATIN,
                    new EssenceValue(EssenceType.AURORA, 1.0f),
                    new EssenceValue(EssenceType.VAPOR, 0.5f));
            registerIfBound(ModItems.CHARGED_GLOW_TENTACLE,
                    new EssenceValue(EssenceType.AURORA, 1.5f),
                    new EssenceValue(EssenceType.VAPOR, 0.5f));
            registerIfBound(ModItems.AURORAL_WAND,
                    new EssenceValue(EssenceType.AURORA, 3.0f),
                    new EssenceValue(EssenceType.VAPOR, 1.0f));
            registerIfBound(ModItems.VAPOR_CONDENSATE_CRYSTALS,
                    new EssenceValue(EssenceType.VAPOR, 4.0f),
                    new EssenceValue(EssenceType.AURORA, 2.0f));

            // Biome 3 (2.3 Frost-Bite Floes): Natural Terrain & Flora Blocks
            registerIfBound(ModItems.ROTTEN_ICE_ITEM,
                    new EssenceValue(EssenceType.GLACIAL, 1.0f),
                    new EssenceValue(EssenceType.BLIGHT, 1.0f));
            registerIfBound(ModItems.BLIGHTED_PACKED_ICE_ITEM,
                    new EssenceValue(EssenceType.GLACIAL, 1.5f),
                    new EssenceValue(EssenceType.BLIGHT, 1.0f),
                    new EssenceValue(EssenceType.FROZEN, 0.5f));
            registerIfBound(ModItems.NECROTIC_SLIME_PATCH_ITEM,
                    new EssenceValue(EssenceType.BLIGHT, 0.5f),
                    new EssenceValue(EssenceType.GLACIAL, 0.25f));
            registerIfBound(ModItems.NECROTIC_SLIME_BLOCK_ITEM,
                    new EssenceValue(EssenceType.BLIGHT, 4.5f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f));
            registerIfBound(ModItems.FROZEN_SPRUCE_BRANCH_ITEM,
                    new EssenceValue(EssenceType.GLACIAL, 0.5f),
                    new EssenceValue(EssenceType.FROZEN, 0.5f));
            registerIfBound(ModItems.CURED_FROST_HIDE_BLOCK_ITEM,
                    new EssenceValue(EssenceType.GLACIAL, 6.0f),
                    new EssenceValue(EssenceType.FROZEN, 3.0f));

            // Biome 3: Fauna Drops, Reagents, Frost-Bite Dagger & Cryo-Insulated Armor
            registerIfBound(ModItems.CRYO_CRYSTALLINE_FANG,
                    new EssenceValue(EssenceType.GLACIAL, 2.0f),
                    new EssenceValue(EssenceType.FROZEN, 1.0f));
            registerIfBound(ModItems.FROST_VEINED_HIDE,
                    new EssenceValue(EssenceType.GLACIAL, 1.5f),
                    new EssenceValue(EssenceType.FROZEN, 0.5f));
            registerIfBound(ModItems.ROT_POWDER,
                    new EssenceValue(EssenceType.BLIGHT, 1.0f),
                    new EssenceValue(EssenceType.GLACIAL, 0.25f));
            registerIfBound(ModItems.NECROTIC_SLIME,
                    new EssenceValue(EssenceType.BLIGHT, 1.0f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.NECROTIC_SLIME_BUCKET,
                    new EssenceValue(EssenceType.BLIGHT, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.SEA_SALT_CRYSTALS,
                    new EssenceValue(EssenceType.GLACIAL, 0.5f),
                    new EssenceValue(EssenceType.FROZEN, 0.5f));
            registerIfBound(ModItems.CURED_FROST_VEINED_HIDE,
                    new EssenceValue(EssenceType.GLACIAL, 2.0f),
                    new EssenceValue(EssenceType.FROZEN, 1.0f));
            registerIfBound(ModItems.FROST_BITE_DAGGER,
                    new EssenceValue(EssenceType.GLACIAL, 4.0f),
                    new EssenceValue(EssenceType.FROZEN, 2.0f),
                    new EssenceValue(EssenceType.BLIGHT, 1.0f));
            registerIfBound(ModItems.CRYO_INSULATED_HELMET,
                    new EssenceValue(EssenceType.GLACIAL, 5.0f),
                    new EssenceValue(EssenceType.FROZEN, 2.5f));
            registerIfBound(ModItems.CRYO_INSULATED_CHESTPLATE,
                    new EssenceValue(EssenceType.GLACIAL, 8.0f),
                    new EssenceValue(EssenceType.FROZEN, 4.0f));
            registerIfBound(ModItems.CRYO_INSULATED_LEGGINGS,
                    new EssenceValue(EssenceType.GLACIAL, 7.0f),
                    new EssenceValue(EssenceType.FROZEN, 3.5f));
            registerIfBound(ModItems.CRYO_INSULATED_BOOTS,
                    new EssenceValue(EssenceType.GLACIAL, 4.0f),
                    new EssenceValue(EssenceType.FROZEN, 2.0f));

            // Biome 4 (2.4 Crystalline Pack-Ice & Aegis Shielding Monolith): Blocks & Items
            registerIfBound(ModItems.AEGIS_QUARTZ,
                    new EssenceValue(EssenceType.AEGIS, 1.5f),
                    new EssenceValue(EssenceType.AURORA, 0.5f));
            registerIfBound(ModItems.AEGIS_QUARTZ_BLOCK_ITEM,
                    new EssenceValue(EssenceType.AEGIS, 6.0f),
                    new EssenceValue(EssenceType.AURORA, 2.0f));
            registerIfBound(ModItems.AEGIS_QUARTZ_CLUSTER_ITEM,
                    new EssenceValue(EssenceType.AEGIS, 3.0f),
                    new EssenceValue(EssenceType.AURORA, 1.0f));
            registerIfBound(ModItems.BISMUTH_ORE_ITEM,
                    new EssenceValue(EssenceType.AURORA, 2.0f),
                    new EssenceValue(EssenceType.AEGIS, 1.0f));
            registerIfBound(ModItems.RAW_BISMUTH,
                    new EssenceValue(EssenceType.AURORA, 1.5f),
                    new EssenceValue(EssenceType.AEGIS, 0.5f));
            registerIfBound(ModItems.BISMUTH_INGOT,
                    new EssenceValue(EssenceType.AURORA, 2.0f),
                    new EssenceValue(EssenceType.AEGIS, 1.0f));
            registerIfBound(ModItems.BISMUTH_BLOCK_ITEM,
                    new EssenceValue(EssenceType.AURORA, 18.0f),
                    new EssenceValue(EssenceType.AEGIS, 9.0f));
            registerIfBound(ModItems.AEGIS_PLATED_STONE_ITEM,
                    new EssenceValue(EssenceType.AEGIS, 4.0f),
                    new EssenceValue(EssenceType.AURORA, 1.0f));
            registerIfBound(ModItems.BISMUTH_KEY_PEDESTAL_ITEM,
                    new EssenceValue(EssenceType.AURORA, 4.0f),
                    new EssenceValue(EssenceType.AEGIS, 4.0f));
            registerIfBound(ModItems.AEGIS_WARDING_EMBLEM,
                    new EssenceValue(EssenceType.AEGIS, 6.0f),
                    new EssenceValue(EssenceType.AURORA, 2.0f));
            registerIfBound(ModItems.AEGIS_SHIELDING_CHARM,
                    new EssenceValue(EssenceType.AEGIS, 8.0f),
                    new EssenceValue(EssenceType.AURORA, 4.0f));
            registerIfBound(ModItems.AEGIS_SPIRE_ORCHID_ITEM,
                    new EssenceValue(EssenceType.AEGIS, 1.0f),
                    new EssenceValue(EssenceType.AURORA, 0.25f));
            registerIfBound(ModItems.TALL_AEGIS_SPIRE_ORCHID_ITEM,
                    new EssenceValue(EssenceType.AEGIS, 2.0f),
                    new EssenceValue(EssenceType.AURORA, 0.5f));
            registerIfBound(ModItems.AEGIS_SPIRE_PETAL,
                    new EssenceValue(EssenceType.AEGIS, 0.5f));

            // Biome 5 (2.5 Silent Fjord): Natural Terrain, Flora, Reagents & Stealth Gear
            registerIfBound(ModItems.BASALT_COLUMN_ITEM,
                    new EssenceValue(EssenceType.ECLIPSE, 0.5f),
                    new EssenceValue(EssenceType.EARTH, 0.5f));
            registerIfBound(ModItems.CHISELED_ECLIPSE_BASALT_ITEM,
                    new EssenceValue(EssenceType.ECLIPSE, 2.0f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.BLUE_FROZEN_MOSS_ITEM,
                    new EssenceValue(EssenceType.GLACIAL, 1.5f),
                    new EssenceValue(EssenceType.ECLIPSE, 0.5f));
            registerIfBound(ModItems.BLUE_FROZEN_MOSS_CARPET_ITEM,
                    new EssenceValue(EssenceType.GLACIAL, 0.75f),
                    new EssenceValue(EssenceType.ECLIPSE, 0.25f));
            registerIfBound(ModItems.SHADOWED_ECLIPSE_MOSS_ITEM,
                    new EssenceValue(EssenceType.ECLIPSE, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.FROZEN_MOSS_CLUMP,
                    new EssenceValue(EssenceType.GLACIAL, 1.0f),
                    new EssenceValue(EssenceType.ECLIPSE, 0.25f));
            registerIfBound(ModItems.CRYO_GRENADE,
                    new EssenceValue(EssenceType.GLACIAL, 3.0f),
                    new EssenceValue(EssenceType.FROZEN, 1.5f));
            registerIfBound(ModItems.SILENT_STRIDERS,
                    new EssenceValue(EssenceType.ECLIPSE, 6.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f));
            registerIfBound(ModItems.BASALT_PICKAXE,
                    new EssenceValue(EssenceType.ECLIPSE, 1.5f),
                    new EssenceValue(EssenceType.EARTH, 1.5f));

            // Biome 6 (2.6 Steaming Skerries & Vaporous Grotto): Geothermal Blocks, Flora & Vapor Materia Gear
            registerIfBound(ModItems.STEAMING_ICE_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.5f));
            registerIfBound(ModItems.STEAMING_SPRING_STONE_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 1.5f),
                    new EssenceValue(EssenceType.EARTH, 0.5f));
            registerIfBound(ModItems.VAPOR_WEAVER_LICHEN_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 1.0f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.STEAM_VENT_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 2.5f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.VAPOR_LIFT_MAT_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 3.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.VAPOR_CONDENSER_ITEM,
                    new EssenceValue(EssenceType.VAPOR, 4.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f));
            registerIfBound(ModItems.VAPOR_CONDENSER_SPORE,
                    new EssenceValue(EssenceType.VAPOR, 1.0f),
                    new EssenceValue(EssenceType.GLACIAL, 0.25f));
            registerIfBound(ModItems.VAPOR_SHROUD_CLOAK,
                    new EssenceValue(EssenceType.VAPOR, 5.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.5f));
            registerIfBound(ModItems.POROUS_BASALT_SHELL,
                    new EssenceValue(EssenceType.VAPOR, 2.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));

            // Biome 7 (2.7 Static Ice-Plains & Fulgurite Cryo-Relay): Fulgurite Blocks, Flora & Lightning-Glacial Gear
            registerIfBound(ModItems.FULGURITE_ICE_ITEM,
                    new EssenceValue(EssenceType.LIGHTNING, 2.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.5f));
            registerIfBound(ModItems.FULGURITE_PINE_LOG_ITEM,
                    new EssenceValue(EssenceType.LIGHTNING, 1.0f),
                    new EssenceValue(EssenceType.NATURE, 1.0f));
            registerIfBound(ModItems.FULGURITE_PINE_LEAVES_ITEM,
                    new EssenceValue(EssenceType.LIGHTNING, 0.5f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.STATIC_FROST_POWDER,
                    new EssenceValue(EssenceType.LIGHTNING, 1.0f),
                    new EssenceValue(EssenceType.GLACIAL, 0.75f));
            registerIfBound(ModItems.CHARGED_STATIC_NEEDLE,
                    new EssenceValue(EssenceType.LIGHTNING, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 0.75f));
            registerIfBound(ModItems.STATIC_QUILL,
                    new EssenceValue(EssenceType.LIGHTNING, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.GROUNDING_COIL,
                    new EssenceValue(EssenceType.LIGHTNING, 5.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.5f));
            registerIfBound(ModItems.STATIC_CONDUCTIVE_MAT_ITEM,
                    new EssenceValue(EssenceType.LIGHTNING, 2.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.BIOMASS_CAPACITOR_ITEM,
                    new EssenceValue(EssenceType.LIGHTNING, 4.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f));

            // Biome 10 (2.10 Sun-Glazed Sanctuary): Vitae, Radiant, Aurora & Glacial Materia Mappings
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
            registerIfBound(ModItems.QILIN_LIGHT_REFRACTOR_ITEM,
                    new EssenceValue(EssenceType.RADIANT, 6.0f),
                    new EssenceValue(EssenceType.VITAE, 4.0f),
                    new EssenceValue(EssenceType.AURORA, 3.0f));
            registerIfBound(ModItems.SUN_GLAZED_LANTERN_ITEM,
                    new EssenceValue(EssenceType.RADIANT, 3.0f),
                    new EssenceValue(EssenceType.VITAE, 1.5f),
                    new EssenceValue(EssenceType.AURORA, 1.0f));
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

            // Biome 11 (2.11 Storm-Swept Icebergs): Storm, Glacial, Air & Lightning Materia Mappings
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
            registerIfBound(ModItems.CHERUFE_BASALT_CRUST,
                    new EssenceValue(EssenceType.MAGMA, 4.5f),
                    new EssenceValue(EssenceType.NETHER, 2.5f),
                    new EssenceValue(EssenceType.EARTH, 2.0f));
            registerIfBound(ModItems.PHENEX_EMBER_FEATHER,
                    new EssenceValue(EssenceType.SOULFIRE, 4.5f),
                    new EssenceValue(EssenceType.NETHER, 2.5f),
                    new EssenceValue(EssenceType.UNDEAD, 2.0f));
            registerIfBound(ModItems.VOID_WEB_SPINDLE,
                    new EssenceValue(EssenceType.VOID, 4.5f),
                    new EssenceValue(EssenceType.NETHER, 2.5f),
                    new EssenceValue(EssenceType.UMBRAL, 2.0f));
            registerIfBound(ModItems.BARROW_RUNE_STONE,
                    new EssenceValue(EssenceType.BARROW, 4.5f),
                    new EssenceValue(EssenceType.EARTH, 2.5f),
                    new EssenceValue(EssenceType.UNDEAD, 2.0f));
            registerIfBound(ModItems.MAGMA_ORTHRUS_PELT,
                    new EssenceValue(EssenceType.MAGMA, 4.5f),
                    new EssenceValue(EssenceType.NETHER, 2.5f),
                    new EssenceValue(EssenceType.EARTH, 2.0f));
            registerIfBound(ModItems.CINDER_WISP_CORE,
                    new EssenceValue(EssenceType.MAGMA, 4.5f),
                    new EssenceValue(EssenceType.NETHER, 2.5f),
                    new EssenceValue(EssenceType.RADIANT, 2.0f));
            registerIfBound(ModItems.HYDRA_SOUL_GLAND,
                    new EssenceValue(EssenceType.SOULFIRE, 5.0f),
                    new EssenceValue(EssenceType.NETHER, 3.0f),
                    new EssenceValue(EssenceType.UNDEAD, 2.0f));
            registerIfBound(ModItems.OBSIDIAN_GARGOYLE_WING,
                    new EssenceValue(EssenceType.VOID, 4.5f),
                    new EssenceValue(EssenceType.EARTH, 2.5f),
                    new EssenceValue(EssenceType.UMBRAL, 2.0f));
            registerIfBound(ModItems.RUNIC_CRYPT_STONE,
                    new EssenceValue(EssenceType.SOULFIRE, 4.5f),
                    new EssenceValue(EssenceType.BARROW, 3.0f),
                    new EssenceValue(EssenceType.EARTH, 2.5f));
            registerIfBound(ModItems.SCORPIOID_TAIL_SPIKE,
                    new EssenceValue(EssenceType.MAGMA, 4.5f),
                    new EssenceValue(EssenceType.VOID, 2.5f),
                    new EssenceValue(EssenceType.NETHER, 2.0f));
            registerIfBound(ModItems.WYVERN_SCALE,
                    new EssenceValue(EssenceType.MAGMA, 5.0f),
                    new EssenceValue(EssenceType.NETHER, 2.5f),
                    new EssenceValue(EssenceType.AIR, 2.0f));
            registerIfBound(ModItems.OBSIDIAN_CORE,
                    new EssenceValue(EssenceType.VOID, 5.5f),
                    new EssenceValue(EssenceType.NETHER, 3.0f),
                    new EssenceValue(EssenceType.EARTH, 2.5f));
            registerIfBound(ModItems.BRASS_DJINN_COLLAR,
                    new EssenceValue(EssenceType.SOULFIRE, 5.0f),
                    new EssenceValue(EssenceType.ARID, 2.5f),
                    new EssenceValue(EssenceType.NETHER, 2.0f));
            registerIfBound(ModItems.CHIMERA_VENOM_GLAND,
                    new EssenceValue(EssenceType.MAGMA, 4.5f),
                    new EssenceValue(EssenceType.CHIMERA, 3.5f),
                    new EssenceValue(EssenceType.BLIGHT, 2.0f));
            registerIfBound(ModItems.WENDIGO_SKULL_PLATE,
                    new EssenceValue(EssenceType.BARROW, 5.0f),
                    new EssenceValue(EssenceType.VOID, 2.5f),
                    new EssenceValue(EssenceType.UNDEAD, 2.0f));

            // Biome 12 (2.12 Spore-Glazed Pack): Spore, Glacial & Overgrowth Materia Mappings
            registerIfBound(ModItems.LIVING_SPORES,
                    new EssenceValue(EssenceType.SPORE, 1.5f),
                    new EssenceValue(EssenceType.OVERGROWTH, 0.75f));
            registerIfBound(ModItems.MOSSY_AEGIS_PLATE,
                    new EssenceValue(EssenceType.OVERGROWTH, 1.5f),
                    new EssenceValue(EssenceType.AEGIS, 1.5f),
                    new EssenceValue(EssenceType.SPORE, 0.5f));
            registerIfBound(ModItems.FROZEN_SPORE_MOSS_ITEM,
                    new EssenceValue(EssenceType.SPORE, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.FROZEN_SPORE_MOSS_CARPET_ITEM,
                    new EssenceValue(EssenceType.SPORE, 0.75f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.SPORE_GLAZED_ICE_ITEM,
                    new EssenceValue(EssenceType.GLACIAL, 1.5f),
                    new EssenceValue(EssenceType.SPORE, 1.5f));
            registerIfBound(ModItems.SPORE_WOOD_LOG_ITEM,
                    new EssenceValue(EssenceType.SPORE, 1.5f),
                    new EssenceValue(EssenceType.OVERGROWTH, 1.0f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.SPORE_WOOD_ITEM,
                    new EssenceValue(EssenceType.SPORE, 2.0f),
                    new EssenceValue(EssenceType.OVERGROWTH, 1.25f),
                    new EssenceValue(EssenceType.GLACIAL, 0.75f));
            registerIfBound(ModItems.SPORE_WOOD_PLANKS_ITEM,
                    new EssenceValue(EssenceType.SPORE, 0.5f),
                    new EssenceValue(EssenceType.OVERGROWTH, 0.25f));
            registerIfBound(ModItems.SPORE_WOOD_STAIRS_ITEM,
                    new EssenceValue(EssenceType.SPORE, 0.75f),
                    new EssenceValue(EssenceType.OVERGROWTH, 0.375f));
            registerIfBound(ModItems.SPORE_WOOD_SLAB_ITEM,
                    new EssenceValue(EssenceType.SPORE, 0.25f),
                    new EssenceValue(EssenceType.OVERGROWTH, 0.125f));
            registerIfBound(ModItems.SPORE_WOOD_FENCE_ITEM,
                    new EssenceValue(EssenceType.SPORE, 0.75f),
                    new EssenceValue(EssenceType.OVERGROWTH, 0.375f));
            registerIfBound(ModItems.FROZEN_SPORE_LEAVES_ITEM,
                    new EssenceValue(EssenceType.SPORE, 0.75f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f),
                    new EssenceValue(EssenceType.OVERGROWTH, 0.5f));
            registerIfBound(ModItems.GLAZE_CAP_MUSHROOM_BLOCK_ITEM,
                    new EssenceValue(EssenceType.SPORE, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f),
                    new EssenceValue(EssenceType.OVERGROWTH, 0.5f));
            registerIfBound(ModItems.MYCELIAL_SPORE_CAP,
                    new EssenceValue(EssenceType.SPORE, 2.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.5f),
                    new EssenceValue(EssenceType.OVERGROWTH, 1.0f));
            registerIfBound(ModItems.SPORE_BELLOWS_ITEM,
                    new EssenceValue(EssenceType.SPORE, 5.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f),
                    new EssenceValue(EssenceType.OVERGROWTH, 1.5f));
            registerIfBound(ModItems.CRYO_SPORE_CENSER_ITEM,
                    new EssenceValue(EssenceType.SPORE, 6.0f),
                    new EssenceValue(EssenceType.GLACIAL, 5.0f));
            registerIfBound(ModItems.SPORE_FILTER_MASK,
                    new EssenceValue(EssenceType.SPORE, 5.0f),
                    new EssenceValue(EssenceType.OVERGROWTH, 3.0f),
                    new EssenceValue(EssenceType.AEGIS, 2.5f));
            registerIfBound(ModItems.CRYO_SPORE_POULTICE,
                    new EssenceValue(EssenceType.SPORE, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f),
                    new EssenceValue(EssenceType.OVERGROWTH, 0.5f));
            registerIfBound(ModItems.GLAZED_SPORE_BOMB,
                    new EssenceValue(EssenceType.SPORE, 2.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f));

            // Biome 13 (2.13 Mirage Glacier): Mirage, Glacial & Arid Materia Mappings
            registerIfBound(ModItems.VITREOUS_NEEDLE,
                    new EssenceValue(EssenceType.MIRAGE, 1.0f),
                    new EssenceValue(EssenceType.ARID, 1.0f));
            registerIfBound(ModItems.GLASS_SAND_ITEM,
                    new EssenceValue(EssenceType.MIRAGE, 0.75f),
                    new EssenceValue(EssenceType.ARID, 0.75f),
                    new EssenceValue(EssenceType.GLACIAL, 0.25f));
            registerIfBound(ModItems.GLASS_SANDSTONE_ITEM,
                    new EssenceValue(EssenceType.MIRAGE, 3.0f),
                    new EssenceValue(EssenceType.ARID, 3.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.GLASS_SANDSTONE_STAIRS_ITEM,
                    new EssenceValue(EssenceType.MIRAGE, 2.25f),
                    new EssenceValue(EssenceType.ARID, 2.25f),
                    new EssenceValue(EssenceType.GLACIAL, 0.75f));
            registerIfBound(ModItems.GLASS_SANDSTONE_SLAB_ITEM,
                    new EssenceValue(EssenceType.MIRAGE, 1.5f),
                    new EssenceValue(EssenceType.ARID, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f));
            registerIfBound(ModItems.MIRAGE_ICE_ITEM,
                    new EssenceValue(EssenceType.MIRAGE, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.5f));
            registerIfBound(ModItems.MIRAGE_PRISM_LANTERN_ITEM,
                    new EssenceValue(EssenceType.MIRAGE, 6.0f),
                    new EssenceValue(EssenceType.GLACIAL, 3.0f),
                    new EssenceValue(EssenceType.ARID, 2.0f));
            registerIfBound(ModItems.SILICA_GLASS_PLATE,
                    new EssenceValue(EssenceType.MIRAGE, 2.5f),
                    new EssenceValue(EssenceType.ARID, 2.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.MIRAGE_VEIL_GOGGLES,
                    new EssenceValue(EssenceType.MIRAGE, 6.0f),
                    new EssenceValue(EssenceType.ARID, 4.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f));
            registerIfBound(ModItems.MIRAGE_DECOY_PRISM,
                    new EssenceValue(EssenceType.MIRAGE, 3.0f),
                    new EssenceValue(EssenceType.ARID, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.SHIMMERING_FOCUS,
                    new EssenceValue(EssenceType.MIRAGE, 8.0f),
                    new EssenceValue(EssenceType.GLACIAL, 3.0f),
                    new EssenceValue(EssenceType.ARID, 3.0f));

            // Biome 14 (2.14 Oasis Floes): Oasis, Glacial, Vapor & Amber Materia Mappings
            registerIfBound(ModItems.AMBER_CHUNK,
                    new EssenceValue(EssenceType.AMBER, 2.0f),
                    new EssenceValue(EssenceType.OASIS, 1.0f));
            registerIfBound(ModItems.AMBER_NECTAR_BOTTLE,
                    new EssenceValue(EssenceType.AMBER, 3.0f),
                    new EssenceValue(EssenceType.OASIS, 2.5f),
                    new EssenceValue(EssenceType.VAPOR, 1.0f));
            registerIfBound(ModItems.FROST_WILLOW_LEAVES_ITEM,
                    new EssenceValue(EssenceType.OASIS, 1.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.FROST_WILLOW_VINES_ITEM,
                    new EssenceValue(EssenceType.OASIS, 1.0f),
                    new EssenceValue(EssenceType.GLACIAL, 0.75f),
                    new EssenceValue(EssenceType.VAPOR, 0.5f));
            registerIfBound(ModItems.AMBER_RESIN_BLOCK_ITEM,
                    new EssenceValue(EssenceType.AMBER, 8.0f),
                    new EssenceValue(EssenceType.OASIS, 4.0f));
            registerIfBound(ModItems.AMBER_TORCH_ITEM,
                    new EssenceValue(EssenceType.AMBER, 0.75f),
                    new EssenceValue(EssenceType.OASIS, 0.5f));
            registerIfBound(ModItems.OASIS_DEW_BASIN_ITEM,
                    new EssenceValue(EssenceType.OASIS, 6.0f),
                    new EssenceValue(EssenceType.VAPOR, 4.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f),
                    new EssenceValue(EssenceType.AMBER, 3.0f));
            registerIfBound(ModItems.AMBER_ADHESIVE,
                    new EssenceValue(EssenceType.AMBER, 2.0f),
                    new EssenceValue(EssenceType.OASIS, 1.5f));
            registerIfBound(ModItems.WATERPROOF_SALAMANDER_SKIN,
                    new EssenceValue(EssenceType.OASIS, 2.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.5f),
                    new EssenceValue(EssenceType.VAPOR, 1.5f));
            registerIfBound(ModItems.SALAMANDER_WADER_HOOD,
                    new EssenceValue(EssenceType.OASIS, 8.0f),
                    new EssenceValue(EssenceType.GLACIAL, 4.0f),
                    new EssenceValue(EssenceType.VAPOR, 4.0f),
                    new EssenceValue(EssenceType.AMBER, 2.0f));

            // Biome 15 (2.15 Dust-Crusted Bergs): Dust, Glacial & Arid Materia Mappings
            registerIfBound(ModItems.DUST_CRUSTED_ICE_ITEM,
                    new EssenceValue(EssenceType.DUST, 1.5f),
                    new EssenceValue(EssenceType.GLACIAL, 1.5f));
            registerIfBound(ModItems.GREY_SAND_ITEM,
                    new EssenceValue(EssenceType.DUST, 0.75f),
                    new EssenceValue(EssenceType.GLACIAL, 0.5f),
                    new EssenceValue(EssenceType.ARID, 0.25f));
            registerIfBound(ModItems.WIND_BLOWN_DUST_SCRUB_ITEM,
                    new EssenceValue(EssenceType.DUST, 1.0f),
                    new EssenceValue(EssenceType.ARID, 0.75f),
                    new EssenceValue(EssenceType.GLACIAL, 0.25f));
            registerIfBound(ModItems.TOUGH_FIBER_ROPE_ITEM,
                    new EssenceValue(EssenceType.DUST, 1.0f),
                    new EssenceValue(EssenceType.ARID, 0.75f));
            registerIfBound(ModItems.WIND_BAFFLE_LANTERN_ITEM,
                    new EssenceValue(EssenceType.DUST, 6.0f),
                    new EssenceValue(EssenceType.GLACIAL, 4.0f),
                    new EssenceValue(EssenceType.ARID, 2.0f));
            registerIfBound(ModItems.DRY_FIBERS,
                    new EssenceValue(EssenceType.DUST, 0.5f),
                    new EssenceValue(EssenceType.ARID, 0.5f));
            registerIfBound(ModItems.DUST_CORE_SHARD,
                    new EssenceValue(EssenceType.DUST, 4.0f),
                    new EssenceValue(EssenceType.GLACIAL, 2.5f),
                    new EssenceValue(EssenceType.ARID, 1.5f));
            registerIfBound(ModItems.COPPER_SILT_SIEVE,
                    new EssenceValue(EssenceType.DUST, 3.0f),
                    new EssenceValue(EssenceType.ARID, 2.0f),
                    new EssenceValue(EssenceType.GLACIAL, 1.0f));
            registerIfBound(ModItems.DUST_DEVIL_AMPOULE,
                    new EssenceValue(EssenceType.DUST, 4.5f),
                    new EssenceValue(EssenceType.GLACIAL, 2.0f),
                    new EssenceValue(EssenceType.ARID, 2.0f));

            // Biome 16 (2.16 Blood-Ice Shallows): Blood, Glacial & Vitae Materia Mappings
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

            modDefaultsInitialized = true;
        }
    }

    /**
     * O(1) Instant Lookup. The graph is already built by the time the player joins the game!
     */
    public static List<EssenceValue> getEssenceFor(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof EssenceItem) {
            return List.of();
        }

        ensureModDefaultMappings();

        Item item = stack.getItem();
        if (MANUAL_REGISTRY.containsKey(item)) return MANUAL_REGISTRY.get(item);
        if (CACHE.containsKey(item)) return CACHE.get(item);

        List<EssenceValue> base = getBaseMaterialEssence(stack);
        if (!base.isEmpty()) return base;

        // Ultimate Fallback if an item is completely uncraftable and unmapped
        return List.of(new EssenceValue(EssenceType.EARTH, 0.03125f));
    }

    /**
     * The Iterative Essence Graph Generator.
     * Runs exactly once on Server Start to perfectly map the entire game!
     */
    public static void buildEssenceGraph(Iterable<RecipeHolder<?>> recipes, net.minecraft.core.HolderLookup.Provider registries) {
        CACHE.clear();
        ALL_ITEMS_CACHE = null;
        ensureModDefaultMappings();

        // 1. SEED THE GRAPH: Load base materials and manual registrations into the Cache
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            List<EssenceValue> base = getBaseMaterialEssence(stack);

            if (!base.isEmpty()) {
                CACHE.put(item, base);
            }
        }
        CACHE.putAll(MANUAL_REGISTRY);

        // 2. ITERATIVE CASCADE: Keep scanning recipes until the entire progression tree is mapped!
        boolean changed = true;
        int iterations = 0;

        while (changed && iterations < 100) { // Max 100 deep crafting steps to prevent infinite loop crashes
            changed = false;
            iterations++;

            for (RecipeHolder<?> holder : recipes) {
                Object recipe = holder.value();

                // Deep scan for the result item
                ItemStack result = extractResultSafe(recipe, registries);

                // If we already calculated this item's value, skip it
                if (result == null || result.isEmpty() || CACHE.containsKey(result.getItem())) continue;

                List<EssenceValue> recipeCost = new ArrayList<>();
                boolean canCraft = true;
                boolean hasIngredients = false;

                // Deep scan for ingredients hidden in patterns, lists, or fields
                for (Ingredient ingredient : extractIngredientsSafe(recipe)) {
                    if (ingredient == null || ingredient.isEmpty()) continue;
                    hasIngredients = true;

                    ItemStack rep = getKnownMatchingItemSafe(ingredient);

                    // If we don't know the essence value of an ingredient yet, we can't calculate this recipe!
                    if (rep.isEmpty() || !CACHE.containsKey(rep.getItem())) {
                        canCraft = false;
                        break;
                    }

                    // Add the ingredient's essence to the total recipe cost
                    recipeCost = combineEssences(recipeCost, CACHE.get(rep.getItem()));
                }

                // If all ingredients were known, we successfully calculated the result!
                if (canCraft && hasIngredients && !recipeCost.isEmpty()) {
                    int count = result.getCount();
                    if (count > 1) {
                        List<EssenceValue> divided = new ArrayList<>();
                        for (EssenceValue ev : recipeCost) {
                            divided.add(new EssenceValue(ev.type(), ev.amount() / count));
                        }
                        CACHE.put(result.getItem(), divided);
                    } else {
                        CACHE.put(result.getItem(), recipeCost);
                    }
                    changed = true; // We discovered a new item! Run the loop again!
                }
            }
        }

        Entropica.LOGGER.info("Entropica Essence Graph Generation Complete! Mapped " + CACHE.size() + " items in " + iterations + " iterations.");
    }

    // ==========================================
    // DEEP REFLECTION SCANNERS (Completely ignores missing API methods)
    // ==========================================

    private static ItemStack extractResultSafe(Object recipe, net.minecraft.core.HolderLookup.Provider registries) {
        // 1. Check direct methods (The polite way)
        try {
            for (java.lang.reflect.Method m : recipe.getClass().getMethods()) {
                if (m.getReturnType() == ItemStack.class) {
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == net.minecraft.core.HolderLookup.Provider.class) {
                        ItemStack res = (ItemStack) m.invoke(recipe, registries);
                        if (res != null && !res.isEmpty()) return res;
                    } else if (m.getParameterCount() == 0) {
                        String name = m.getName().toLowerCase();
                        if (name.contains("result") || name.contains("output")) {
                            ItemStack res = (ItemStack) m.invoke(recipe);
                            if (res != null && !res.isEmpty()) return res;
                        }
                    }
                }
            }
        } catch (Exception e) {}

        // 2. Scan internal memory fields (The forceful way)
        try {
            Class<?> current = recipe.getClass();
            while (current != null && current != Object.class) {
                for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                    if (f.getType() == ItemStack.class) {
                        f.setAccessible(true);
                        ItemStack res = (ItemStack) f.get(recipe);
                        if (res != null && !res.isEmpty()) return res;
                    }
                }
                current = current.getSuperclass();
            }
        } catch (Exception e) {}

        return ItemStack.EMPTY;
    }

    private static Iterable<Ingredient> extractIngredientsSafe(Object recipe) {
        List<Ingredient> found = new ArrayList<>();
        findIngredientsRecursive(recipe, found, 0, new HashSet<>());
        return found;
    }

    private static void findIngredientsRecursive(Object obj, List<Ingredient> found, int depth, Set<Integer> visited) {
        // Stop going too deep to prevent lag, 4 levels is enough to crack RecipePatterns and Lists
        if (obj == null || depth > 4) return;

        int hash = System.identityHashCode(obj);
        if (!visited.add(hash)) return; // Prevent circular references

        if (obj instanceof Ingredient ing) {
            try {
                if (!ing.isEmpty() && !ing.test(ItemStack.EMPTY)) {
                    found.add(ing);
                }
            } catch (Exception e) {}
            return;
        }

        if (obj instanceof Iterable<?> iter) {
            for (Object o : iter) findIngredientsRecursive(o, found, depth + 1, visited);
            return;
        }

        if (obj.getClass().isArray()) {
            if (!obj.getClass().getComponentType().isPrimitive()) {
                for (Object o : (Object[]) obj) findIngredientsRecursive(o, found, depth + 1, visited);
            }
            return;
        }

        // Do not scan core Java classes to save memory
        String className = obj.getClass().getName();
        if (className.startsWith("java.") || className.startsWith("jdk.")) return;

        // Recursively crack open all fields looking for ingredients
        Class<?> current = obj.getClass();
        while (current != null && current != Object.class) {
            for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                try {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    findIngredientsRecursive(val, found, depth + 1, visited);
                } catch (Exception e) {}
            }
            current = current.getSuperclass();
        }
    }

    private static ItemStack getKnownMatchingItemSafe(Ingredient ingredient) {
        if (ALL_ITEMS_CACHE == null) {
            ALL_ITEMS_CACHE = BuiltInRegistries.ITEM.stream().map(ItemStack::new).toArray(ItemStack[]::new);
        }

        ItemStack firstMatch = ItemStack.EMPTY;

        for (ItemStack testStack : ALL_ITEMS_CACHE) {
            try {
                if (ingredient.test(testStack)) {
                    if (firstMatch.isEmpty()) firstMatch = testStack; // Save the first valid item as a fallback

                    // Crucial: Prioritize items that are already mapped in the Cache!
                    // This prevents a generic tag like "c:ingots" from testing against an unmapped modded item and failing!
                    if (CACHE.containsKey(testStack.getItem())) {
                        return testStack;
                    }
                }
            } catch (Exception e) {}
        }

        return firstMatch;
    }

    private static List<EssenceValue> combineEssences(List<EssenceValue> a, List<EssenceValue> b) {
        Map<EssenceType, Float> map = new EnumMap<>(EssenceType.class);
        for (EssenceValue ev : a) map.put(ev.type(), map.getOrDefault(ev.type(), 0f) + ev.amount());
        for (EssenceValue ev : b) map.put(ev.type(), map.getOrDefault(ev.type(), 0f) + ev.amount());

        List<EssenceValue> result = new ArrayList<>();
        for (Map.Entry<EssenceType, Float> entry : map.entrySet()) {
            result.add(new EssenceValue(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    // ==========================================
    // BASE MATERIAL HARDCODES (The Seed Data)
    // ==========================================

    private static List<EssenceValue> getBaseMaterialEssence(ItemStack stack) {
        Item item = stack.getItem();
        List<EssenceValue> list = new ArrayList<>();

        // 1. Specific Vanilla Base Items (Placed FIRST to explicitly override generic tags!)
        if (item == Items.LAPIS_LAZULI) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.EARTH, 0.25f));
        if (item == Items.REDSTONE) return List.of(new EssenceValue(EssenceType.EARTH, 0.25f), new EssenceValue(EssenceType.LIGHTNING, 0.25f));
        if (item == Items.QUARTZ) return List.of(new EssenceValue(EssenceType.EARTH, 0.25f), new EssenceValue(EssenceType.NETHER, 0.25f));

        if (item == Items.ROTTEN_FLESH || item == Items.BONE) return List.of(new EssenceValue(EssenceType.UNDEAD, 0.25f));
        if (item == Items.SPIDER_EYE || item == Items.STRING) return List.of(new EssenceValue(EssenceType.UMBRAL, 0.25f), new EssenceValue(EssenceType.NATURE, 0.25f));
        if (item == Items.GUNPOWDER) return List.of(new EssenceValue(EssenceType.DUST, 0.25f), new EssenceValue(EssenceType.PYRE, 0.25f));
        if (item == Items.SLIME_BALL) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.NATURE, 0.25f));
        if (item == Items.ENDER_PEARL) return List.of(new EssenceValue(EssenceType.VOID, 1.0f));
        if (item == Items.GHAST_TEAR) return List.of(new EssenceValue(EssenceType.UMBRAL, 1.0f), new EssenceValue(EssenceType.NETHER, 1.0f));
        if (item == Items.BLAZE_ROD) return List.of(new EssenceValue(EssenceType.PYRE, 1.0f));
        if (item == Items.PHANTOM_MEMBRANE) return List.of(new EssenceValue(EssenceType.UMBRAL, 0.5f), new EssenceValue(EssenceType.AIR, 0.5f));
        if (item == Items.FEATHER) return List.of(new EssenceValue(EssenceType.AIR, 0.25f));
        if (item == Items.BREEZE_ROD) return List.of(new EssenceValue(EssenceType.AIR, 1.0f), new EssenceValue(EssenceType.STORM, 1.0f));

        if (item == Items.GLOW_INK_SAC) return List.of(new EssenceValue(EssenceType.RADIANT, 0.25f), new EssenceValue(EssenceType.WATER, 0.25f));
        if (item == Items.INK_SAC) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.UMBRAL, 0.25f));
        if (item == Items.LEATHER || item == Items.BEEF || item == Items.PORKCHOP || item == Items.MUTTON || item == Items.CHICKEN) return List.of(new EssenceValue(EssenceType.EARTH, 0.25f), new EssenceValue(EssenceType.NATURE, 0.25f));

        if (stack.is(ItemTags.LOGS)) return List.of(new EssenceValue(EssenceType.NATURE, 1.0f));
        if (item == Items.COAL) return List.of(new EssenceValue(EssenceType.PYRE, 0.5f), new EssenceValue(EssenceType.EARTH, 0.25f));

        if (item == Items.NETHERRACK || item == Items.BLACKSTONE || item == Items.BASALT) return List.of(new EssenceValue(EssenceType.NETHER, 0.25f), new EssenceValue(EssenceType.DUST, 0.1f));
        if (item == Items.MAGMA_BLOCK) return List.of(new EssenceValue(EssenceType.MAGMA, 1.0f));
        if (item == Items.MAGMA_CREAM) return List.of(new EssenceValue(EssenceType.MAGMA, 0.5f), new EssenceValue(EssenceType.PYRE, 0.25f));
        if (item == Items.OBSIDIAN || item == Items.CRYING_OBSIDIAN) return List.of(new EssenceValue(EssenceType.VOID, 0.5f), new EssenceValue(EssenceType.MAGMA, 0.5f));
        if (item == Items.END_STONE) return List.of(new EssenceValue(EssenceType.VOID, 0.25f), new EssenceValue(EssenceType.EARTH, 0.25f));

        if (item == Items.COBBLESTONE || item == Items.STONE || item == Items.DEEPSLATE) return List.of(new EssenceValue(EssenceType.EARTH, 0.25f));
        if (stack.is(ItemTags.SAND) || stack.is(ItemTags.DIRT) || item == Items.GRAVEL) return List.of(new EssenceValue(EssenceType.DUST, 0.25f));

        if (item == Items.ICE || item == Items.PACKED_ICE || item == Items.BLUE_ICE) return List.of(new EssenceValue(EssenceType.FROZEN, 1.0f));
        if (item == Items.KELP || item == Items.SEAGRASS || item == Items.SUGAR_CANE) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.NATURE, 0.125f));

        if (item == Items.PRISMARINE_SHARD) return List.of(new EssenceValue(EssenceType.WATER, 0.25f));
        if (item == Items.PRISMARINE_CRYSTALS) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.RADIANT, 0.25f));
        if (item == Items.PRISMARINE || item == Items.PRISMARINE_BRICKS || item == Items.DARK_PRISMARINE) return List.of(new EssenceValue(EssenceType.WATER, 1.0f));
        if (item == Items.SEA_LANTERN) return List.of(new EssenceValue(EssenceType.WATER, 1.0f), new EssenceValue(EssenceType.RADIANT, 1.0f));
        if (item == Items.COD || item == Items.SALMON || item == Items.PUFFERFISH || item == Items.TROPICAL_FISH) return List.of(new EssenceValue(EssenceType.WATER, 0.25f), new EssenceValue(EssenceType.NATURE, 0.25f));

        // 2. Dynamic Base Tags
        boolean matchedTag = false;
        for (var tagKey : stack.getTags().toList()) {
            String path = tagKey.location().getPath().toLowerCase();

            if (path.contains("ingots")) {
                list.add(new EssenceValue(EssenceType.EARTH, 0.5f));
                list.add(new EssenceValue(EssenceType.PYRE, 0.5f));
                matchedTag = true; break;
            } else if (path.contains("raw_materials")) {
                list.add(new EssenceValue(EssenceType.EARTH, 1.0f));
                matchedTag = true; break;
            } else if (path.contains("gems")) {
                list.add(new EssenceValue(EssenceType.EARTH, 2.0f));
                list.add(new EssenceValue(EssenceType.RADIANT, 1.0f));
                matchedTag = true; break;
            } else if (path.contains("dusts")) {
                list.add(new EssenceValue(EssenceType.DUST, 0.25f));
                list.add(new EssenceValue(EssenceType.LIGHTNING, 0.25f));
                matchedTag = true; break;
            }
        }
        if (matchedTag) return list;

        return List.of();
    }
}