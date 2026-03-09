package ddraig.net.entropica.registry;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.item.EssenceAmpouleItem;
import ddraig.net.entropica.item.EssenceHarvestingBladeItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import ddraig.net.entropica.item.OrbisCellItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("entropica");

    // --- Standalone Items ---
    public static final DeferredItem<Item> ARCANUM_FOCUS = ITEMS.registerItem("arcanum_focus", Item::new);
    public static final DeferredItem<Item> VIS_VALUE_DETECTOR = ITEMS.registerItem("vis_value_detector", Item::new);

    // --- Aetheric Vision Equipment ---
    public static final DeferredItem<Item> AETHERIC_MONOCLE = ITEMS.registerItem("aetheric_monocle", properties -> new ddraig.net.entropica.item.AethericVisionItem(
            properties.stacksTo(1).equippable(net.minecraft.world.entity.EquipmentSlot.HEAD)
    ));

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

    public static final DeferredItem<Item> SOULBOUND_BLADE = ITEMS.registerItem("soulbound_blade", properties -> new Item(
            properties.sword(ToolMaterial.DIAMOND, 0, -2.4F)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
                    .component(ModDataComponents.VIS_WEAPON_STATE.get(), new VisWeaponState.Builder(EssenceType.SOULFIRE, 6.0F, 1).build())
    ));

    public static final DeferredItem<Item> OBLIVION_BLADE = ITEMS.registerItem("oblivion_blade", properties -> new Item(
            properties.sword(ToolMaterial.NETHERITE, 0, -2.4F)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
                    .component(ModDataComponents.VIS_WEAPON_STATE.get(), new VisWeaponState.Builder(EssenceType.OBLIVION, 8.5F, 2).build())
    ));

    public static final DeferredItem<Item> TIDAL_TRIDENT = ITEMS.registerItem("tidal_trident", properties -> new Item(
            properties.sword(ToolMaterial.DIAMOND, 0, -2.4F)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
                    .component(ModDataComponents.VIS_WEAPON_STATE.get(), new VisWeaponState.Builder(EssenceType.WATER, 7.0F, 1).build())
    ));

    public static final DeferredItem<Item> VOID_SWORD = ITEMS.registerItem("void_sword", properties -> new Item(
            properties.sword(ToolMaterial.DIAMOND, 0, -2.4F)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
                    .component(ModDataComponents.VIS_WEAPON_STATE.get(), new VisWeaponState.Builder(EssenceType.VOID, 8.5F, 2).build())
    ));

    public static final DeferredItem<Item> BASALT_PICKAXE = ITEMS.registerItem("basalt_pickaxe", Item::new);
    public static final DeferredItem<Item> WHISPERWOOD_WAND = ITEMS.registerItem("whisperwood_wand", Item::new);
    public static final DeferredItem<Item> SHIMMERING_FOCUS = ITEMS.registerItem("shimmering_focus", Item::new);

    // ==========================================
    // DYNAMIC ESSENCE ORBS & ESSENCE AMPOULES
    // ==========================================
    public static final DeferredItem<EssenceItem> FRAGMENT_ESSENCE = ITEMS.registerItem("fragment_essence", properties -> new EssenceItem(properties, 0));

    public static final DeferredItem<EssenceItem> WEAK_ESSENCE = ITEMS.registerItem("weak_essence", properties -> new EssenceItem(properties, 1));
    public static final DeferredItem<EssenceItem> AVERAGE_ESSENCE = ITEMS.registerItem("average_essence", properties -> new EssenceItem(properties, 2));
    public static final DeferredItem<EssenceItem> STRONG_ESSENCE = ITEMS.registerItem("strong_essence", properties -> new EssenceItem(properties, 3));

    public static final DeferredItem<EssenceAmpouleItem> SMALL_ESSENCE_AMPOULE = ITEMS.registerItem("small_essence_ampoule", properties -> new EssenceAmpouleItem(properties, 1));
    public static final DeferredItem<EssenceAmpouleItem> MEDIUM_ESSENCE_AMPOULE = ITEMS.registerItem("medium_essence_ampoule", properties -> new EssenceAmpouleItem(properties, 2));
    public static final DeferredItem<EssenceAmpouleItem> LARGE_ESSENCE_AMPOULE = ITEMS.registerItem("large_essence_ampoule", properties -> new EssenceAmpouleItem(properties, 3));

    // --- Block Items ---
    public static final DeferredItem<BlockItem> ENTROPIC_ORE_ITEM = ITEMS.registerItem("entropic_ore", properties -> new BlockItem(ModBlocks.ENTROPIC_ORE.get(), properties));
    public static final DeferredItem<BlockItem> MANA_FURNACE_ITEM = ITEMS.registerItem("mana_furnace", properties -> new BlockItem(ModBlocks.MANA_FURNACE.get(), properties));
    public static final DeferredItem<BlockItem> SOLAR_POWERED_FURNACE_ITEM = ITEMS.registerItem("solar_powered_furnace", properties -> new BlockItem(ModBlocks.SOLAR_POWERED_FURNACE.get(), properties));
    public static final DeferredItem<BlockItem> ENTROPIC_AUTO_SMELTER_ITEM = ITEMS.registerItem("entropic_auto_smelter", properties -> new BlockItem(ModBlocks.ENTROPIC_AUTO_SMELTER.get(), properties));
    public static final DeferredItem<BlockItem> VITAE_INCINERATOR_ITEM = ITEMS.registerItem("vitae_incinerator", properties -> new BlockItem(ModBlocks.VITAE_INCINERATOR.get(), properties));

    // Multiblock Components
    public static final DeferredItem<BlockItem> ENTROPIC_CORE_ITEM = ITEMS.registerItem("entropic_core", properties -> new BlockItem(ModBlocks.ENTROPIC_CORE.get(), properties));
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

    // Vis Fume Ampoules (Filled) - RENAMED FROM MANA_AMPOULE
    public static final DeferredItem<VisFumeAmpouleItem> SMALL_VIS_FUME_AMPOULE = ITEMS.registerItem("small_vis_fume_ampoule", properties -> new VisFumeAmpouleItem(properties.stacksTo(16), 4, SMALL_AMPOULE_BASE));
    public static final DeferredItem<VisFumeAmpouleItem> MEDIUM_VIS_FUME_AMPOULE = ITEMS.registerItem("medium_vis_fume_ampoule", properties -> new VisFumeAmpouleItem(properties.stacksTo(16), 16, MEDIUM_AMPOULE_BASE));
    public static final DeferredItem<VisFumeAmpouleItem> LARGE_VIS_FUME_AMPOULE = ITEMS.registerItem("large_vis_fume_ampoule", properties -> new VisFumeAmpouleItem(properties.stacksTo(16), 64, LARGE_AMPOULE_BASE));

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
    public static final DeferredItem<BlockItem> AETHERIC_SYNTHESIZER_ITEM = ITEMS.registerItem("aetheric_synthesizer", properties -> new BlockItem(ModBlocks.AETHERIC_SYNTHESIZER.get(), properties));
    public static final DeferredItem<BlockItem> AETHERIC_AUTOMATOR_ITEM = ITEMS.registerItem("aetheric_automator", properties -> new BlockItem(ModBlocks.AETHERIC_AUTOMATOR.get(), properties));

    public static final DeferredItem<BlockItem> SOLAR_ESSENCE_RECHARGE_STATION_ITEM = ITEMS.registerItem("solar_essence_recharge_station", properties -> new BlockItem(ModBlocks.SOLAR_ESSENCE_RECHARGE_STATION.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_LOOM_ITEM = ITEMS.registerItem("arcane_loom", properties -> new BlockItem(ModBlocks.ARCANE_LOOM.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_FORGE_ITEM = ITEMS.registerItem("essence_forge", properties -> new BlockItem(ModBlocks.ESSENCE_FORGE.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_ANVIL_ITEM = ITEMS.registerItem("arcane_anvil", properties -> new BlockItem(ModBlocks.ARCANE_ANVIL.get(), properties));
}