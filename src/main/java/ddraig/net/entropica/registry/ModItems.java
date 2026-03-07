package ddraig.net.entropica.registry;

import ddraig.net.entropica.item.EssenceHarvestingBladeItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.ManaAmpouleItem;
import ddraig.net.entropica.item.OrbisCellItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("entropica");

    // --- Standalone Items ---
    public static final DeferredItem<Item> ARCANUM_FOCUS = ITEMS.registerItem("arcanum_focus", Item::new);
    public static final DeferredItem<Item> VIS_VALUE_DETECTOR = ITEMS.registerItem("vis_value_detector", Item::new);

    // THE NEW CRAFTING ITEMS (Given new IDs so blocks can keep the old ones)
    public static final DeferredItem<Item> ARCANE_BRICK_PIECE = ITEMS.registerItem("arcane_brick_piece", Item::new);
    public static final DeferredItem<Item> ARCANE_PLATE = ITEMS.registerItem("arcane_plate", Item::new);
    public static final DeferredItem<Item> ARCANE_CLAY = ITEMS.registerItem("arcane_clay", Item::new);

    // --- Empty Ampoules ---
    public static final DeferredItem<Item> SMALL_AMPOULE = ITEMS.registerItem("small_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_AMPOULE = ITEMS.registerItem("medium_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_AMPOULE = ITEMS.registerItem("large_ampoule", Item::new);

    // --- Weapons & Tools ---
    public static final DeferredItem<Item> ESSENCE_HARVESTING_BLADE = ITEMS.registerItem("essence_harvesting_blade", properties -> new EssenceHarvestingBladeItem(
            properties.sword(ToolMaterial.DIAMOND, 3.0F, -2.4F)
    ));

    public static final DeferredItem<Item> SOULBOUND_BLADE = ITEMS.registerItem("soulbound_blade", Item::new);
    public static final DeferredItem<Item> OBLIVION_BLADE = ITEMS.registerItem("oblivion_blade", Item::new);
    public static final DeferredItem<Item> TIDAL_TRIDENT = ITEMS.registerItem("tidal_trident", Item::new);
    public static final DeferredItem<Item> BASALT_PICKAXE = ITEMS.registerItem("basalt_pickaxe", Item::new);
    public static final DeferredItem<Item> WHISPERWOOD_WAND = ITEMS.registerItem("whisperwood_wand", Item::new);
    public static final DeferredItem<Item> SHIMMERING_FOCUS = ITEMS.registerItem("shimmering_focus", Item::new);

    // ==========================================
    // TIER 1: WEAK ESSENCE & SMALL AMPOULES
    // ==========================================
    public static final DeferredItem<EssenceItem> WEAK_UNDEAD_ESSENCE = ITEMS.registerItem("weak_undead_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_WATER_ESSENCE = ITEMS.registerItem("weak_water_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_NETHER_ESSENCE = ITEMS.registerItem("weak_nether_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_CHIMERA_ESSENCE = ITEMS.registerItem("weak_chimera_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_ARID_ESSENCE = ITEMS.registerItem("weak_arid_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_FROZEN_ESSENCE = ITEMS.registerItem("weak_frozen_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_VOID_ESSENCE = ITEMS.registerItem("weak_void_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_AIR_ESSENCE = ITEMS.registerItem("weak_air_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_EARTH_ESSENCE = ITEMS.registerItem("weak_earth_essence", EssenceItem::new);

    public static final DeferredItem<EssenceItem> WEAK_NATURE_ESSENCE = ITEMS.registerItem("weak_nature_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_LIGHTNING_ESSENCE = ITEMS.registerItem("weak_lightning_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_RADIANT_ESSENCE = ITEMS.registerItem("weak_radiant_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> WEAK_UMBRAL_ESSENCE = ITEMS.registerItem("weak_umbral_essence", EssenceItem::new);

    public static final DeferredItem<Item> SMALL_UNDEAD_AMPOULE = ITEMS.registerItem("small_undead_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_WATER_AMPOULE = ITEMS.registerItem("small_water_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_NETHER_AMPOULE = ITEMS.registerItem("small_nether_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_CHIMERA_AMPOULE = ITEMS.registerItem("small_chimera_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_ARID_AMPOULE = ITEMS.registerItem("small_arid_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_FROZEN_AMPOULE = ITEMS.registerItem("small_frozen_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_VOID_AMPOULE = ITEMS.registerItem("small_void_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_AIR_AMPOULE = ITEMS.registerItem("small_air_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_EARTH_AMPOULE = ITEMS.registerItem("small_earth_ampoule", Item::new);

    public static final DeferredItem<Item> SMALL_NATURE_AMPOULE = ITEMS.registerItem("small_nature_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_LIGHTNING_AMPOULE = ITEMS.registerItem("small_lightning_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_RADIANT_AMPOULE = ITEMS.registerItem("small_radiant_ampoule", Item::new);
    public static final DeferredItem<Item> SMALL_UMBRAL_AMPOULE = ITEMS.registerItem("small_umbral_ampoule", Item::new);

    // ==========================================
    // TIER 2: AVERAGE ESSENCE & MEDIUM AMPOULES
    // ==========================================
    public static final DeferredItem<EssenceItem> AVERAGE_UNDEAD_ESSENCE = ITEMS.registerItem("average_undead_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_WATER_ESSENCE = ITEMS.registerItem("average_water_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_NETHER_ESSENCE = ITEMS.registerItem("average_nether_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_CHIMERA_ESSENCE = ITEMS.registerItem("average_chimera_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_ARID_ESSENCE = ITEMS.registerItem("average_arid_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_FROZEN_ESSENCE = ITEMS.registerItem("average_frozen_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_VOID_ESSENCE = ITEMS.registerItem("average_void_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_AIR_ESSENCE = ITEMS.registerItem("average_air_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_EARTH_ESSENCE = ITEMS.registerItem("average_earth_essence", EssenceItem::new);

    public static final DeferredItem<EssenceItem> AVERAGE_NATURE_ESSENCE = ITEMS.registerItem("average_nature_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_LIGHTNING_ESSENCE = ITEMS.registerItem("average_lightning_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_RADIANT_ESSENCE = ITEMS.registerItem("average_radiant_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> AVERAGE_UMBRAL_ESSENCE = ITEMS.registerItem("average_umbral_essence", EssenceItem::new);

    public static final DeferredItem<Item> MEDIUM_UNDEAD_AMPOULE = ITEMS.registerItem("medium_undead_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_WATER_AMPOULE = ITEMS.registerItem("medium_water_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_NETHER_AMPOULE = ITEMS.registerItem("medium_nether_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_CHIMERA_AMPOULE = ITEMS.registerItem("medium_chimera_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_ARID_AMPOULE = ITEMS.registerItem("medium_arid_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_FROZEN_AMPOULE = ITEMS.registerItem("medium_frozen_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_VOID_AMPOULE = ITEMS.registerItem("medium_void_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_AIR_AMPOULE = ITEMS.registerItem("medium_air_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_EARTH_AMPOULE = ITEMS.registerItem("medium_earth_ampoule", Item::new);

    public static final DeferredItem<Item> MEDIUM_NATURE_AMPOULE = ITEMS.registerItem("medium_nature_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_LIGHTNING_AMPOULE = ITEMS.registerItem("medium_lightning_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_RADIANT_AMPOULE = ITEMS.registerItem("medium_radiant_ampoule", Item::new);
    public static final DeferredItem<Item> MEDIUM_UMBRAL_AMPOULE = ITEMS.registerItem("medium_umbral_ampoule", Item::new);

    // ==========================================
    // TIER 3: STRONG ESSENCE & LARGE AMPOULES
    // ==========================================
    public static final DeferredItem<EssenceItem> STRONG_UNDEAD_ESSENCE = ITEMS.registerItem("strong_undead_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_WATER_ESSENCE = ITEMS.registerItem("strong_water_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_NETHER_ESSENCE = ITEMS.registerItem("strong_nether_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_CHIMERA_ESSENCE = ITEMS.registerItem("strong_chimera_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_ARID_ESSENCE = ITEMS.registerItem("strong_arid_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_FROZEN_ESSENCE = ITEMS.registerItem("strong_frozen_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_VOID_ESSENCE = ITEMS.registerItem("strong_void_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_AIR_ESSENCE = ITEMS.registerItem("strong_air_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_EARTH_ESSENCE = ITEMS.registerItem("strong_earth_essence", EssenceItem::new);

    public static final DeferredItem<EssenceItem> STRONG_NATURE_ESSENCE = ITEMS.registerItem("strong_nature_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_LIGHTNING_ESSENCE = ITEMS.registerItem("strong_lightning_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_RADIANT_ESSENCE = ITEMS.registerItem("strong_radiant_essence", EssenceItem::new);
    public static final DeferredItem<EssenceItem> STRONG_UMBRAL_ESSENCE = ITEMS.registerItem("strong_umbral_essence", EssenceItem::new);

    public static final DeferredItem<Item> LARGE_UNDEAD_AMPOULE = ITEMS.registerItem("large_undead_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_WATER_AMPOULE = ITEMS.registerItem("large_water_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_NETHER_AMPOULE = ITEMS.registerItem("large_nether_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_CHIMERA_AMPOULE = ITEMS.registerItem("large_chimera_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_ARID_AMPOULE = ITEMS.registerItem("large_arid_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_FROZEN_AMPOULE = ITEMS.registerItem("large_frozen_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_VOID_AMPOULE = ITEMS.registerItem("large_void_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_AIR_AMPOULE = ITEMS.registerItem("large_air_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_EARTH_AMPOULE = ITEMS.registerItem("large_earth_ampoule", Item::new);

    public static final DeferredItem<Item> LARGE_NATURE_AMPOULE = ITEMS.registerItem("large_nature_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_LIGHTNING_AMPOULE = ITEMS.registerItem("large_lightning_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_RADIANT_AMPOULE = ITEMS.registerItem("large_radiant_ampoule", Item::new);
    public static final DeferredItem<Item> LARGE_UMBRAL_AMPOULE = ITEMS.registerItem("large_umbral_ampoule", Item::new);

    // --- Block Items ---
    public static final DeferredItem<BlockItem> ENTROPIC_ORE_ITEM = ITEMS.registerItem("entropic_ore", properties -> new BlockItem(ModBlocks.ENTROPIC_ORE.get(), properties));
    public static final DeferredItem<BlockItem> MANA_FURNACE_ITEM = ITEMS.registerItem("mana_furnace", properties -> new BlockItem(ModBlocks.MANA_FURNACE.get(), properties));
    public static final DeferredItem<BlockItem> SOLAR_POWERED_FURNACE_ITEM = ITEMS.registerItem("solar_powered_furnace", properties -> new BlockItem(ModBlocks.SOLAR_POWERED_FURNACE.get(), properties));
    public static final DeferredItem<BlockItem> ENTROPIC_AUTO_SMELTER_ITEM = ITEMS.registerItem("entropic_auto_smelter", properties -> new BlockItem(ModBlocks.ENTROPIC_AUTO_SMELTER.get(), properties));
    public static final DeferredItem<BlockItem> VITAE_INCINERATOR_ITEM = ITEMS.registerItem("vitae_incinerator", properties -> new BlockItem(ModBlocks.VITAE_INCINERATOR.get(), properties));

    // Multiblock Components
    public static final DeferredItem<BlockItem> ENTROPIC_CORE_ITEM = ITEMS.registerItem("entropic_core", properties -> new BlockItem(ModBlocks.ENTROPIC_CORE.get(), properties));

    // THE RESTORED BLOCK ITEMS (Matches the world-safe Block IDs perfectly)
    public static final DeferredItem<BlockItem> ARCANE_BRICK_ITEM = ITEMS.registerItem("arcane_brick", properties -> new BlockItem(ModBlocks.ARCANE_BRICK.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_PLATING_ITEM = ITEMS.registerItem("arcane_plating", properties -> new BlockItem(ModBlocks.ARCANE_PLATING.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_CLAY_BLOCK_ITEM = ITEMS.registerItem("arcane_clay_block", properties -> new BlockItem(ModBlocks.ARCANE_CLAY_BLOCK.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_FORGE_BASE_ITEM = ITEMS.registerItem("arcane_forge_base", properties -> new BlockItem(ModBlocks.ARCANE_FORGE_BASE.get(), properties));

    public static final DeferredItem<BlockItem> MANA_PLUME_ITEM = ITEMS.registerItem("mana_plume", properties -> new BlockItem(ModBlocks.MANA_PLUME.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_RECEPTACLE_ITEM = ITEMS.registerItem("essence_receptacle", properties -> new BlockItem(ModBlocks.ESSENCE_RECEPTACLE.get(), properties));
    public static final DeferredItem<BlockItem> FURNACE_HATCH_ITEM = ITEMS.registerItem("furnace_hatch", properties -> new BlockItem(ModBlocks.FURNACE_HATCH.get(), properties));
    public static final DeferredItem<BlockItem> MANA_READOUT_ITEM = ITEMS.registerItem("mana_readout", properties -> new BlockItem(ModBlocks.MANA_READOUT.get(), properties));
    public static final DeferredItem<BlockItem> MANA_EXHAUST = ITEMS.registerItem("mana_exhaust", properties -> new BlockItem(ModBlocks.MANA_EXHAUST.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_READOUT_ITEM = ITEMS.registerItem("essence_readout", properties -> new BlockItem(ModBlocks.ESSENCE_READOUT.get(), properties));
    public static final DeferredItem<BlockItem> CATALYST_RECEPTACLE_ITEM = ITEMS.registerItem("catalyst_receptacle", properties -> new BlockItem(ModBlocks.CATALYST_RECEPTACLE.get(), properties));

    public static final DeferredItem<Item> ORBIS_CELL_ITEM = ITEMS.registerItem("orbis_cell", properties -> new OrbisCellItem(ModBlocks.ORBIS_CELL.get(), properties));

    // Base Ampoules (Empty)
    public static final DeferredItem<Item> SMALL_AMPOULE_BASE = ITEMS.registerItem("small_ampoule_base", properties -> new Item(properties.stacksTo(64)));
    public static final DeferredItem<Item> MEDIUM_AMPOULE_BASE = ITEMS.registerItem("medium_ampoule_base", properties -> new Item(properties.stacksTo(64)));
    public static final DeferredItem<Item> LARGE_AMPOULE_BASE = ITEMS.registerItem("large_ampoule_base", properties -> new Item(properties.stacksTo(64)));

    // Mana Ampoules (Filled)
    public static final DeferredItem<ManaAmpouleItem> SMALL_MANA_AMPOULE = ITEMS.registerItem("small_mana_ampoule", properties -> new ManaAmpouleItem(properties.stacksTo(16), 4, SMALL_AMPOULE_BASE));
    public static final DeferredItem<ManaAmpouleItem> MEDIUM_MANA_AMPOULE = ITEMS.registerItem("medium_mana_ampoule", properties -> new ManaAmpouleItem(properties.stacksTo(16), 16, MEDIUM_AMPOULE_BASE));
    public static final DeferredItem<ManaAmpouleItem> LARGE_MANA_AMPOULE = ITEMS.registerItem("large_mana_ampoule", properties -> new ManaAmpouleItem(properties.stacksTo(16), 64, LARGE_AMPOULE_BASE));

    // Vis Fume Network Items
    public static final DeferredItem<BlockItem> VIS_FUME_PIPE_ITEM = ITEMS.registerItem("vis_fume_pipe", properties -> new BlockItem(ModBlocks.VIS_FUME_PIPE.get(), properties));
    public static final DeferredItem<BlockItem> VIS_FUME_VALVE_ITEM = ITEMS.registerItem("vis_fume_valve", properties -> new BlockItem(ModBlocks.VIS_FUME_VALVE.get(), properties));
    public static final DeferredItem<BlockItem> VIS_FUME_ONE_WAY_VALVE_ITEM = ITEMS.registerItem("vis_fume_one_way_valve", properties -> new BlockItem(ModBlocks.VIS_FUME_ONE_WAY_VALVE.get(), properties));
    public static final DeferredItem<BlockItem> VIS_FUME_DIVERTER_ITEM = ITEMS.registerItem("vis_fume_diverter", properties -> new BlockItem(ModBlocks.VIS_FUME_DIVERTER.get(), properties));
    public static final DeferredItem<BlockItem> CREATIVE_VIS_FUME_GENERATOR_ITEM = ITEMS.registerItem("creative_vis_fume_generator", properties -> new BlockItem(ModBlocks.CREATIVE_VIS_FUME_GENERATOR.get(), properties));

    // ---  Vis Fume Pressure Vessel Items ---
    public static final DeferredItem<BlockItem> VIS_FUME_VESSEL_CONTROLLER_ITEM = ITEMS.registerItem("vis_fume_vessel_controller", properties -> new BlockItem(ModBlocks.VIS_FUME_VESSEL_CONTROLLER.get(), properties));
    public static final DeferredItem<BlockItem> VIS_FUME_VESSEL_PORT_ITEM = ITEMS.registerItem("vis_fume_vessel_port", properties -> new BlockItem(ModBlocks.VIS_FUME_VESSEL_PORT.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_ENRICHED_GLASS_ITEM = ITEMS.registerItem("essence_enriched_glass", properties -> new BlockItem(ModBlocks.ESSENCE_ENRICHED_GLASS.get(), properties));
    public static final DeferredItem<BlockItem> VIS_FUME_STRENGTHENED_GLASS_ITEM = ITEMS.registerItem("vis_fume_strengthened_glass", properties -> new BlockItem(ModBlocks.VIS_FUME_STRENGTHENED_GLASS.get(), properties));
    public static final DeferredItem<BlockItem> VIS_ICHOR_ENRICHED_GLASS_ITEM = ITEMS.registerItem("vis_ichor_enriched_glass", properties -> new BlockItem(ModBlocks.VIS_ICHOR_ENRICHED_GLASS.get(), properties));
    public static final DeferredItem<BlockItem> FRAGMENT_LATTICE_GLASS_ITEM = ITEMS.registerItem("fragment_lattice_glass", properties -> new BlockItem(ModBlocks.FRAGMENT_LATTICE_GLASS.get(), properties));

    // --- Vis Fume Pressure Chamber Items ---
    public static final DeferredItem<BlockItem> VIS_FUME_PRESSURE_CHAMBER_CONTROLLER_ITEM = ITEMS.registerItem("vis_fume_pressure_chamber_controller", properties -> new BlockItem(ModBlocks.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER.get(), properties));
    public static final DeferredItem<BlockItem> ENRICHMENT_TABLE_ITEM = ITEMS.registerItem("enrichment_table", properties -> new BlockItem(ModBlocks.ENRICHMENT_TABLE.get(), properties));

    //Others
    public static final DeferredItem<BlockItem> VIS_VITAE_ANCHOR_ITEM = ITEMS.registerItem("vis_vitae_anchor", properties -> new BlockItem(ModBlocks.VIS_VITAE_ANCHOR.get(), properties));
    public static final DeferredItem<BlockItem> VIS_VITAE_CONDENSER_ITEM = ITEMS.registerItem("vis_vitae_condenser", properties -> new BlockItem(ModBlocks.VIS_VITAE_CONDENSER.get(), properties));
    public static final DeferredItem<BlockItem> VIS_VITAE_VACUUM_ITEM = ITEMS.registerItem("vis_vitae_vacuum", properties -> new BlockItem(ModBlocks.VIS_VITAE_VACUUM.get(), properties));
    public static final DeferredItem<BlockItem> VITAE_BARREL_ITEM = ITEMS.registerItem("vitae_barrel", properties -> new BlockItem(ModBlocks.VITAE_BARREL.get(), properties));
    public static final DeferredItem<BlockItem> MANA_VACUUM_ITEM = ITEMS.registerItem("mana_vacuum", properties -> new BlockItem(ModBlocks.MANA_VACUUM.get(), properties));
    public static final DeferredItem<BlockItem> MANA_CABLE_ITEM = ITEMS.registerItem("mana_cable", properties -> new BlockItem(ModBlocks.MANA_CABLE.get(), properties));
    public static final DeferredItem<BlockItem> MANA_FILTER_ITEM = ITEMS.registerItem("mana_filter", properties -> new BlockItem(ModBlocks.MANA_FILTER.get(), properties));
    public static final DeferredItem<BlockItem> MANA_POWERED_LIGHTING_ITEM = ITEMS.registerItem("mana_powered_lighting", properties -> new BlockItem(ModBlocks.MANA_POWERED_LIGHTING.get(), properties));
    public static final DeferredItem<BlockItem> MANA_CRAFTER_ITEM = ITEMS.registerItem("mana_crafter", properties -> new BlockItem(ModBlocks.MANA_CRAFTER.get(), properties));
    public static final DeferredItem<BlockItem> ENTROPIC_ENCHANTER_ITEM = ITEMS.registerItem("entropic_enchanter", properties -> new BlockItem(ModBlocks.ENTROPIC_ENCHANTER.get(), properties));
    public static final DeferredItem<BlockItem> SOLAR_ESSENCE_RECHARGE_STATION_ITEM = ITEMS.registerItem("solar_essence_recharge_station", properties -> new BlockItem(ModBlocks.SOLAR_ESSENCE_RECHARGE_STATION.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_LOOM_ITEM = ITEMS.registerItem("arcane_loom", properties -> new BlockItem(ModBlocks.ARCANE_LOOM.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_FORGE_ITEM = ITEMS.registerItem("essence_forge", properties -> new BlockItem(ModBlocks.ESSENCE_FORGE.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_ANVIL_ITEM = ITEMS.registerItem("arcane_anvil", properties -> new BlockItem(ModBlocks.ARCANE_ANVIL.get(), properties));

    //Diluted Essence Bucket
    //public static final DeferredItem<BucketItem> DILUTED_ESSENCE_BUCKET = ITEMS.registerItem("diluted_essence_bucket", properties -> new BucketItem(ModFluids.DILUTED_ESSENCE_FLUID.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));
}