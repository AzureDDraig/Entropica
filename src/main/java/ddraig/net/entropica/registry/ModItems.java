package ddraig.net.entropica.registry;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

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

    // --- Crafting Components ---
    // Metals & Alloys
    public static final DeferredItem<Item> ARCANITE_INGOT = ITEMS.registerItem("arcanite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_ingot"));
    public static final DeferredItem<Item> VISCANITE_INGOT = ITEMS.registerItem("viscanite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_ingot"));
    public static final DeferredItem<Item> RESONITE_INGOT = ITEMS.registerItem("resonite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_ingot"));
    public static final DeferredItem<Item> EIDOLITE_INGOT = ITEMS.registerItem("eidolite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.eidolite_ingot"));

    public static final DeferredItem<Item> CHARGED_ARCANITE_INGOT = ITEMS.registerItem("charged_arcanite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_arcanite_ingot"));
    public static final DeferredItem<Item> CHARGED_VISCANITE_INGOT = ITEMS.registerItem("charged_viscanite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_viscanite_ingot"));
    public static final DeferredItem<Item> CHARGED_RESONITE_INGOT = ITEMS.registerItem("charged_resonite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_resonite_ingot"));
    public static final DeferredItem<Item> CHARGED_EIDOLITE_INGOT = ITEMS.registerItem("charged_eidolite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_eidolite_ingot"));

    public static final DeferredItem<Item> ARCANITE_NUGGET = ITEMS.registerItem("arcanite_nugget", Item::new);
    public static final DeferredItem<Item> VISCANITE_NUGGET = ITEMS.registerItem("viscanite_nugget", Item::new);
    public static final DeferredItem<Item> RESONITE_NUGGET = ITEMS.registerItem("resonite_nugget", Item::new);
    public static final DeferredItem<Item> EIDOLITE_NUGGET = ITEMS.registerItem("eidolite_nugget", Item::new);

    public static final DeferredItem<Item> CHARGED_ARCANITE_NUGGET = ITEMS.registerItem("charged_arcanite_nugget", Item::new);
    public static final DeferredItem<Item> CHARGED_VISCANITE_NUGGET = ITEMS.registerItem("charged_viscanite_nugget", Item::new);
    public static final DeferredItem<Item> CHARGED_RESONITE_NUGGET = ITEMS.registerItem("charged_resonite_nugget", Item::new);
    public static final DeferredItem<Item> CHARGED_EIDOLITE_NUGGET = ITEMS.registerItem("charged_eidolite_nugget", Item::new);

    // Wires, Filaments & Spools
    public static final DeferredItem<Item> ARCANITE_WIRE = ITEMS.registerItem("arcanite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_wire"));
    public static final DeferredItem<Item> VISCANITE_WIRE = ITEMS.registerItem("viscanite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_wire"));
    public static final DeferredItem<Item> RESONITE_WIRE = ITEMS.registerItem("resonite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_wire"));
    public static final DeferredItem<Item> EIDOLITE_WIRE = ITEMS.registerItem("eidolite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.eidolite_wire"));

    public static final DeferredItem<Item> COATED_ARCANITE_WIRE = ITEMS.registerItem("coated_arcanite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_arcanite_wire"));
    public static final DeferredItem<Item> COATED_VISCANITE_WIRE = ITEMS.registerItem("coated_viscanite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_viscanite_wire"));
    public static final DeferredItem<Item> COATED_RESONITE_WIRE = ITEMS.registerItem("coated_resonite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_resonite_wire"));
    public static final DeferredItem<Item> COATED_EIDOLITE_WIRE = ITEMS.registerItem("coated_eidolite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_eidolite_wire"));

    public static final DeferredItem<Item> ARCANITE_FILAMENT = ITEMS.registerItem("arcanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_filament"));
    public static final DeferredItem<Item> VISCANITE_FILAMENT = ITEMS.registerItem("viscanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_filament"));
    public static final DeferredItem<Item> RESONITE_FILAMENT = ITEMS.registerItem("resonite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_filament"));
    public static final DeferredItem<Item> EIDOLITE_FILAMENT = ITEMS.registerItem("eidolite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.eidolite_filament"));

    public static final DeferredItem<Item> ARCANITE_SPOOL = ITEMS.registerItem("arcanite_spool", properties -> new SpoolItem(properties.stacksTo(1), "tooltip.entropica.arcanite_spool", ARCANITE_WIRE, ARCANITE_FILAMENT));
    public static final DeferredItem<Item> VISCANITE_SPOOL = ITEMS.registerItem("viscanite_spool", properties -> new SpoolItem(properties.stacksTo(1), "tooltip.entropica.viscanite_spool", VISCANITE_WIRE, VISCANITE_FILAMENT));
    public static final DeferredItem<Item> RESONITE_SPOOL = ITEMS.registerItem("resonite_spool", properties -> new SpoolItem(properties.stacksTo(1), "tooltip.entropica.resonite_spool", RESONITE_WIRE, RESONITE_FILAMENT));
    public static final DeferredItem<Item> EIDOLITE_SPOOL = ITEMS.registerItem("eidolite_spool", properties -> new SpoolItem(properties.stacksTo(1), "tooltip.entropica.eidolite_spool", EIDOLITE_WIRE, EIDOLITE_FILAMENT));

    public static final DeferredItem<Item> CHARGED_ARCANITE_FILAMENT = ITEMS.registerItem("charged_arcanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_arcanite_filament"));
    public static final DeferredItem<Item> CHARGED_VISCANITE_FILAMENT = ITEMS.registerItem("charged_viscanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_viscanite_filament"));
    public static final DeferredItem<Item> CHARGED_RESONITE_FILAMENT = ITEMS.registerItem("charged_resonite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_resonite_filament"));
    public static final DeferredItem<Item> CHARGED_EIDOLITE_FILAMENT = ITEMS.registerItem("charged_eidolite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_eidolite_filament"));
    public static final DeferredItem<Item> COATED_ARCANITE_FILAMENT = ITEMS.registerItem("coated_arcanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_arcanite_filament"));
    public static final DeferredItem<Item> COATED_VISCANITE_FILAMENT = ITEMS.registerItem("coated_viscanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_viscanite_filament"));
    public static final DeferredItem<Item> COATED_RESONITE_FILAMENT = ITEMS.registerItem("coated_resonite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_resonite_filament"));
    public static final DeferredItem<Item> COATED_EIDOLITE_FILAMENT = ITEMS.registerItem("coated_eidolite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_eidolite_filament"));

    // Mechanical Parts
    public static final DeferredItem<Item> ARCANITE_FRAME = ITEMS.registerItem("arcanite_frame", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.reinforced_arcanite_frame"));
    public static final DeferredItem<Item> VISCANITE_FRAME = ITEMS.registerItem("viscanite_frame", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_frame"));

    // Gearsets
    public static final DeferredItem<Item> ARCANITE_GEARSET = ITEMS.registerItem("arcanite_gearset", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_gearset"));
    public static final DeferredItem<Item> VISCANITE_GEARSET = ITEMS.registerItem("viscanite_gearset", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_gearset"));
    public static final DeferredItem<Item> RESONITE_GEARSET = ITEMS.registerItem("resonite_gearset", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_gearset"));

    // Large Gears
    public static final DeferredItem<Item> ARCANITE_LARGE_GEAR = ITEMS.registerItem("arcanite_large_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_large_gear"));
    public static final DeferredItem<Item> VISCANITE_LARGE_GEAR = ITEMS.registerItem("viscanite_large_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_large_gear"));
    public static final DeferredItem<Item> RESONITE_LARGE_GEAR = ITEMS.registerItem("resonite_large_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_large_gear"));

    // Small Gears
    public static final DeferredItem<Item> ARCANITE_SMALL_GEAR = ITEMS.registerItem("arcanite_small_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_small_gear"));
    public static final DeferredItem<Item> VISCANITE_SMALL_GEAR = ITEMS.registerItem("viscanite_small_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_small_gear"));
    public static final DeferredItem<Item> RESONITE_SMALL_GEAR = ITEMS.registerItem("resonite_small_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_small_gear"));

    // Plates & Pistons
    public static final DeferredItem<Item> ARCANITE_PLATE = ITEMS.registerItem("arcanite_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_plate"));
    public static final DeferredItem<Item> VISCANITE_PLATE = ITEMS.registerItem("viscanite_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_plate"));
    public static final DeferredItem<Item> RESONITE_PLATE = ITEMS.registerItem("resonite_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_plate"));
    public static final DeferredItem<Item> VISCANITE_PISTON = ITEMS.registerItem("viscanite_piston", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_piston"));
    public static final DeferredItem<Item> RESONITE_PISTON = ITEMS.registerItem("resonite_piston", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_piston"));

    public static final DeferredItem<Item> BELLOWS_CRANK_ASSEMBLY = ITEMS.registerItem("bellows_crank_assembly", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.bellows_crank_assembly"));
    public static final DeferredItem<Item> PRESSURE_GRADED_GASKET = ITEMS.registerItem("pressure_graded_gasket", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.pressure_graded_gasket"));
    public static final DeferredItem<Item> ENTROPIC_HEAT_SINK = ITEMS.registerItem("entropic_heat_sink", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.entropic_heat_sink"));

    // Fluid & Gas Machine Components
    public static final DeferredItem<Item> ARCANITE_ICHOR_VALVE = ITEMS.registerItem("arcanite_ichor_valve", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_ichor_valve"));
    public static final DeferredItem<Item> VISCANITE_ICHOR_VALVE = ITEMS.registerItem("viscanite_ichor_valve", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_ichor_valve"));
    public static final DeferredItem<Item> ARCANITE_FUME_VALVE = ITEMS.registerItem("arcanite_fume_valve", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_fume_valve"));
    public static final DeferredItem<Item> VISCANITE_FUME_VALVE = ITEMS.registerItem("viscanite_fume_valve", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_fume_valve"));
    public static final DeferredItem<Item> AETHERIC_FLOW_REGULATOR = ITEMS.registerItem("aetheric_flow_regulator", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.aetheric_flow_regulator"));
    public static final DeferredItem<Item> VOID_SEALED_O_RING = ITEMS.registerItem("void_sealed_o_ring", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.void_sealed_o_ring"));
    public static final DeferredItem<Item> FUME_CONDENSER_COIL = ITEMS.registerItem("fume_condenser_coil", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.fume_condenser_coil"));
    public static final DeferredItem<Item> ICHOR_EVAPORATOR_COIL = ITEMS.registerItem("ichor_evaporator_coil", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ichor_evaporator_coil"));

    // Standard Weapon & Tool Cores
    public static final DeferredItem<Item> ARCANITE_WEAPON_CORE = ITEMS.registerItem("arcanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_weapon_core"));
    public static final DeferredItem<Item> CHARGED_ARCANITE_WEAPON_CORE = ITEMS.registerItem("charged_arcanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_arcanite_weapon_core"));
    public static final DeferredItem<Item> ANCIENT_ARCANITE_WEAPON_CORE = ITEMS.registerItem("ancient_arcanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_arcanite_weapon_core"));

    public static final DeferredItem<Item> VISCANITE_WEAPON_CORE = ITEMS.registerItem("viscanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_weapon_core"));
    public static final DeferredItem<Item> CHARGED_VISCANITE_WEAPON_CORE = ITEMS.registerItem("charged_viscanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_viscanite_weapon_core"));
    public static final DeferredItem<Item> ANCIENT_VISCANITE_WEAPON_CORE = ITEMS.registerItem("ancient_viscanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_viscanite_weapon_core"));

    public static final DeferredItem<Item> RESONITE_WEAPON_CORE = ITEMS.registerItem("resonite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_weapon_core"));
    public static final DeferredItem<Item> CHARGED_RESONITE_WEAPON_CORE = ITEMS.registerItem("charged_resonite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_resonite_weapon_core"));
    public static final DeferredItem<Item> ANCIENT_RESONITE_WEAPON_CORE = ITEMS.registerItem("ancient_resonite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_resonite_weapon_core"));

    public static final DeferredItem<Item> ARCANITE_TOOL_CORE = ITEMS.registerItem("arcanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_tool_core"));
    public static final DeferredItem<Item> CHARGED_ARCANITE_TOOL_CORE = ITEMS.registerItem("charged_arcanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_arcanite_tool_core"));
    public static final DeferredItem<Item> ANCIENT_ARCANITE_TOOL_CORE = ITEMS.registerItem("ancient_arcanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_arcanite_tool_core"));

    public static final DeferredItem<Item> VISCANITE_TOOL_CORE = ITEMS.registerItem("viscanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_tool_core"));
    public static final DeferredItem<Item> CHARGED_VISCANITE_TOOL_CORE = ITEMS.registerItem("charged_viscanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_viscanite_tool_core"));
    public static final DeferredItem<Item> ANCIENT_VISCANITE_TOOL_CORE = ITEMS.registerItem("ancient_viscanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_viscanite_tool_core"));

    public static final DeferredItem<Item> RESONITE_TOOL_CORE = ITEMS.registerItem("resonite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_tool_core"));
    public static final DeferredItem<Item> CHARGED_RESONITE_TOOL_CORE = ITEMS.registerItem("charged_resonite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_resonite_tool_core"));
    public static final DeferredItem<Item> ANCIENT_RESONITE_TOOL_CORE = ITEMS.registerItem("ancient_resonite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_resonite_tool_core"));

    // Eidolite Unified Cores (For Constructs/Weapons/Tools)
    public static final DeferredItem<Item> EIDOLITE_CORE = ITEMS.registerItem("eidolite_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.eidolite_core"));
    public static final DeferredItem<Item> CHARGED_EIDOLITE_CORE = ITEMS.registerItem("charged_eidolite_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_eidolite_core"));
    public static final DeferredItem<Item> ANCIENT_EIDOLITE_CORE = ITEMS.registerItem("ancient_eidolite_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_eidolite_core"));

    public static final DeferredItem<Item> SOUL_BOUND_COMMUTATOR = ITEMS.registerItem("soul_bound_commutator", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.soul_bound_commutator"));
    public static final DeferredItem<Item> VISCANITE_STATOR = ITEMS.registerItem("viscanite_stator", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_stator"));
    public static final DeferredItem<Item> RESONITE_STATOR = ITEMS.registerItem("resonite_stator", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_stator"));

    // Shape Concepts (Swords)
    public static final DeferredItem<Item> GLADIUS_SHAPE_CONCEPT = ITEMS.registerItem("gladius_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> LONGSWORD_SHAPE_CONCEPT = ITEMS.registerItem("longsword_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> SHORTSWORD_SHAPE_CONCEPT = ITEMS.registerItem("shortsword_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> AKRAFENA_SHAPE_CONCEPT = ITEMS.registerItem("akrafena_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Shape Concepts (Axes)
    public static final DeferredItem<Item> HAND_AXE_SHAPE_CONCEPT = ITEMS.registerItem("hand_axe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> WAR_AXE_SHAPE_CONCEPT = ITEMS.registerItem("war_axe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> POLE_AXE_SHAPE_CONCEPT = ITEMS.registerItem("pole_axe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> HALBERD_SHAPE_CONCEPT = ITEMS.registerItem("halberd_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> BEARD_AXE_SHAPE_CONCEPT = ITEMS.registerItem("beard_axe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Shape Concepts (Bows)
    public static final DeferredItem<Item> SHORTBOW_SHAPE_CONCEPT = ITEMS.registerItem("shortbow_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> LONGBOW_SHAPE_CONCEPT = ITEMS.registerItem("longbow_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> CROSSBOW_SHAPE_CONCEPT = ITEMS.registerItem("crossbow_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> REPEATER_SHAPE_CONCEPT = ITEMS.registerItem("repeater_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> WAR_BOW_SHAPE_CONCEPT = ITEMS.registerItem("war_bow_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Shape Concepts (Tools)
    public static final DeferredItem<Item> PICKAXE_SHAPE_CONCEPT = ITEMS.registerItem("pickaxe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> SHOVEL_SHAPE_CONCEPT = ITEMS.registerItem("shovel_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> ADZE_SHAPE_CONCEPT = ITEMS.registerItem("adze_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> PAXEL_SHAPE_CONCEPT = ITEMS.registerItem("paxel_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Shape Concepts (Others)
    public static final DeferredItem<Item> SPEAR_SHAPE_CONCEPT = ITEMS.registerItem("spear_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> MACE_SHAPE_CONCEPT = ITEMS.registerItem("mace_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> MORNING_STAR_SHAPE_CONCEPT = ITEMS.registerItem("morning_star_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final DeferredItem<Item> WARHAMMER_SHAPE_CONCEPT = ITEMS.registerItem("warhammer_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Standalone Crafting Item
    public static final DeferredItem<Item> ORBIS_ACCEPTOR = ITEMS.registerItem("orbis_acceptor", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.orbis_acceptor"));
    public static final DeferredItem<Item> SPELL_GEM = ITEMS.registerItem("spell_gem", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.spell_gem"));

    // Optical & Arcane Internals
    public static final DeferredItem<Item> FOCAL_LENS_ASSEMBLY = ITEMS.registerItem("focal_lens_assembly", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.focal_lens_assembly"));
    public static final DeferredItem<Item> AETHERIC_LOGIC_GATE = ITEMS.registerItem("aetheric_logic_gate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.aetheric_logic_gate"));
    public static final DeferredItem<Item> REFRACTION_GRID_MESH = ITEMS.registerItem("refraction_grid_mesh", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.refraction_grid_mesh"));
    public static final DeferredItem<Item> ICHOR_CAPILLARY_TUBE = ITEMS.registerItem("ichor_capillary_tube", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ichor_capillary_tube"));
    public static final DeferredItem<Item> VIS_CAPACITOR_PLATE = ITEMS.registerItem("vis_capacitor_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.vis_capacitor_plate"));
    public static final DeferredItem<Item> HARMONIC_FEEDBACK_LOOP = ITEMS.registerItem("harmonic_feedback_loop", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.harmonic_feedback_loop"));
    public static final DeferredItem<Item> AETHERIC_PITCH_PIPE = ITEMS.registerItem("aetheric_pitch_pipe", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.aetheric_pitch_pipe"));
    public static final DeferredItem<Item> FREQUENCY_SPLITTER_PRISM = ITEMS.registerItem("frequency_splitter_prism", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.frequency_splitter_prism"));
    public static final DeferredItem<Item> TUNING_FORK = ITEMS.registerItem("tuning_fork", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.tuning_fork"));
    public static final DeferredItem<Item> RESONITE_TUNING_FORK = ITEMS.registerItem("resonite_tuning_fork", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_tuning_fork"));

    // --- VOID LOGISTICS ---
    // FIXED: Now uses the custom VoidResonantTuningForkItem class!
    public static final DeferredItem<Item> VOID_RESONANT_TUNING_FORK = ITEMS.registerItem("void_resonant_tuning_fork", VoidResonantTuningForkItem::new);

    public static final DeferredItem<Item> SPECTRAL_GLYPH_PLATE = ITEMS.registerItem("spectral_glyph_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.spectral_glyph_plate"));
    public static final DeferredItem<Item> TETHER_ANCHOR_PIN = ITEMS.registerItem("tether_anchor_pin", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.tether_anchor_pin"));

    // ==========================================
    // --- RUNES (ELDER FUTHARK) ---
    // ==========================================
    public static final DeferredItem<Item> RUNE_FEHU = ITEMS.registerItem("rune_fehu", p -> new RuneItem(p, "ᚠ", "Fehu", "Wealth / Filtering"));
    public static final DeferredItem<Item> RUNE_URUZ = ITEMS.registerItem("rune_uruz", p -> new RuneItem(p, "ᚢ", "Uruz", "Strength / Speed"));
    public static final DeferredItem<Item> RUNE_THURISAZ = ITEMS.registerItem("rune_thurisaz", p -> new RuneItem(p, "ᚦ", "Thurisaz", "Gateway / Tearing"));
    public static final DeferredItem<Item> RUNE_ANSUZ = ITEMS.registerItem("rune_ansuz", p -> new RuneItem(p, "ᚨ", "Ansuz", "Signals / Redstone"));
    public static final DeferredItem<Item> RUNE_RAIDO = ITEMS.registerItem("rune_raido", p -> new RuneItem(p, "ᚱ", "Raido", "Journey / Routing"));
    public static final DeferredItem<Item> RUNE_KENAZ = ITEMS.registerItem("rune_kenaz", p -> new RuneItem(p, "ᚲ", "Kenaz", "Torch / Vision"));
    public static final DeferredItem<Item> RUNE_GEBO = ITEMS.registerItem("rune_gebo", p -> new RuneItem(p, "ᚷ", "Gebo", "Gift / Exchange"));
    public static final DeferredItem<Item> RUNE_WUNJO = ITEMS.registerItem("rune_wunjo", p -> new RuneItem(p, "ᚹ", "Wunjo", "Joy / Output"));
    public static final DeferredItem<Item> RUNE_HAGALAZ = ITEMS.registerItem("rune_hagalaz", p -> new RuneItem(p, "ᚺ", "Hagalaz", "Hail / Destruction"));
    public static final DeferredItem<Item> RUNE_NAUTHIZ = ITEMS.registerItem("rune_nauthiz", p -> new RuneItem(p, "ᚾ", "Nauthiz", "Need / Demand"));
    public static final DeferredItem<Item> RUNE_ISA = ITEMS.registerItem("rune_isa", p -> new RuneItem(p, "ᛁ", "Isa", "Ice / Stasis"));
    public static final DeferredItem<Item> RUNE_JERA = ITEMS.registerItem("rune_jera", p -> new RuneItem(p, "ᛃ", "Jera", "Harvest / Extraction"));
    public static final DeferredItem<Item> RUNE_EIHWAZ = ITEMS.registerItem("rune_eihwaz", p -> new RuneItem(p, "ᛇ", "Eihwaz", "Yew / Connection"));
    public static final DeferredItem<Item> RUNE_PERTHRO = ITEMS.registerItem("rune_perthro", p -> new RuneItem(p, "ᛈ", "Perthro", "Secret / Filtering"));
    public static final DeferredItem<Item> RUNE_ALGIZ = ITEMS.registerItem("rune_algiz", p -> new RuneItem(p, "ᛉ", "Algiz", "Protection / Warding"));
    public static final DeferredItem<Item> RUNE_SOWILO = ITEMS.registerItem("rune_sowilo", p -> new RuneItem(p, "ᛊ", "Sowilo", "Sun / Energy"));
    public static final DeferredItem<Item> RUNE_TIWAZ = ITEMS.registerItem("rune_tiwaz", p -> new RuneItem(p, "ᛏ", "Tiwaz", "Justice / Balancing"));
    public static final DeferredItem<Item> RUNE_BERKANO = ITEMS.registerItem("rune_berkano", p -> new RuneItem(p, "ᛒ", "Berkano", "Growth / Farming"));
    public static final DeferredItem<Item> RUNE_EHWAZ = ITEMS.registerItem("rune_ehwaz", p -> new RuneItem(p, "ᛖ", "Ehwaz", "Horse / Transit"));
    public static final DeferredItem<Item> RUNE_MANNAZ = ITEMS.registerItem("rune_mannaz", p -> new RuneItem(p, "ᛗ", "Mannaz", "Humanity / Player"));
    public static final DeferredItem<Item> RUNE_LAGUZ = ITEMS.registerItem("rune_laguz", p -> new RuneItem(p, "ᛚ", "Laguz", "Water / Fluid"));
    public static final DeferredItem<Item> RUNE_INGWAZ = ITEMS.registerItem("rune_ingwaz", p -> new RuneItem(p, "ᛜ", "Ingwaz", "Seed / Storage"));
    public static final DeferredItem<Item> RUNE_DAGAZ = ITEMS.registerItem("rune_dagaz", p -> new RuneItem(p, "ᛞ", "Dagaz", "Dawn / Splitting"));
    public static final DeferredItem<Item> RUNE_OTHALA = ITEMS.registerItem("rune_othala", p -> new RuneItem(p, "ᛟ", "Othala", "Heritage / Zoning"));

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
    public static final DeferredItem<BlockItem> ENTROPIC_AUTO_SMELTER_ITEM = ITEMS.registerItem("entropic_auto_smelter", properties -> new BlockItem(ModBlocks.ENTROPIC_AUTO_SMELTER.get(), properties));

    // Multiblock Components
    public static final DeferredItem<BlockItem> ENTROPIC_CORE_ITEM = ITEMS.registerItem("entropic_core", properties -> new BlockItem(ModBlocks.ENTROPIC_CORE.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_BRICK_ITEM = ITEMS.registerItem("arcane_brick", properties -> new BlockItem(ModBlocks.ARCANE_BRICK.get(), properties));
    public static final DeferredItem<BlockItem> ARCANITE_PLATING_ITEM = ITEMS.registerItem("arcanite_plating", properties -> new BlockItem(ModBlocks.ARCANITE_PLATING.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_CLAY_BLOCK_ITEM = ITEMS.registerItem("arcane_clay_block", properties -> new BlockItem(ModBlocks.ARCANE_CLAY_BLOCK.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_FORGE_BASE_ITEM = ITEMS.registerItem("arcane_forge_base", properties -> new BlockItem(ModBlocks.ARCANE_FORGE_BASE.get(), properties));

    public static final DeferredItem<BlockItem> MANA_PLUME_ITEM = ITEMS.registerItem("mana_plume", properties -> new BlockItem(ModBlocks.MANA_PLUME.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_RECEPTACLE_ITEM = ITEMS.registerItem("essence_receptacle", properties -> new BlockItem(ModBlocks.ESSENCE_RECEPTACLE.get(), properties));
    public static final DeferredItem<BlockItem> FURNACE_HATCH_ITEM = ITEMS.registerItem("furnace_hatch", properties -> new BlockItem(ModBlocks.FURNACE_HATCH.get(), properties));
    public static final DeferredItem<BlockItem> VIS_READOUT_ITEM = ITEMS.registerItem("vis_readout", properties -> new BlockItem(ModBlocks.VIS_READOUT.get(), properties));
    public static final DeferredItem<BlockItem> VIS_EXHAUST_ITEM = ITEMS.registerItem("vis_exhaust", properties -> new BlockItem(ModBlocks.VIS_EXHAUST.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_READOUT_ITEM = ITEMS.registerItem("essence_readout", properties -> new BlockItem(ModBlocks.ESSENCE_READOUT.get(), properties));
    public static final DeferredItem<BlockItem> CATALYST_RECEPTACLE_ITEM = ITEMS.registerItem("catalyst_receptacle", properties -> new BlockItem(ModBlocks.CATALYST_RECEPTACLE.get(), properties));

    // --- Extraction Multiblock & Machines ---
    public static final DeferredItem<BlockItem> VIS_SIMPLE_MACHINE_BLOCK_ITEM = ITEMS.registerItem("vis_simple_machine_block", properties -> new BlockItem(ModBlocks.VIS_SIMPLE_MACHINE_BLOCK.get(), properties));
    public static final DeferredItem<BlockItem> VIS_COMPLEX_MACHINE_BLOCK_ITEM = ITEMS.registerItem("vis_complex_machine_block", properties -> new BlockItem(ModBlocks.VIS_COMPLEX_MACHINE_BLOCK.get(), properties));
    public static final DeferredItem<BlockItem> VIS_FUME_INPUT_PORT_ITEM = ITEMS.registerItem("vis_fume_input_port", properties -> new BlockItem(ModBlocks.VIS_FUME_INPUT_PORT.get(), properties));
    public static final DeferredItem<BlockItem> VIS_ICHOR_INPUT_PORT_ITEM = ITEMS.registerItem("vis_ichor_input_port", properties -> new BlockItem(ModBlocks.VIS_ICHOR_INPUT_PORT.get(), properties));
    public static final DeferredItem<BlockItem> EXTRACTOR_OUTPUT_PORT_ITEM = ITEMS.registerItem("extractor_output_port", properties -> new BlockItem(ModBlocks.EXTRACTOR_OUTPUT_PORT.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_NODE_IDENTIFIER_BLOCK_ITEM = ITEMS.registerItem("essence_node_identifier_block", properties -> new BlockItem(ModBlocks.ESSENCE_NODE_IDENTIFIER_BLOCK.get(), properties));
    public static final DeferredItem<BlockItem> VIS_EXTRACTION_APPARATUS_ITEM = ITEMS.registerItem("vis_extraction_apparatus", properties -> new BlockItem(ModBlocks.VIS_EXTRACTION_APPARATUS.get(), properties));
    public static final DeferredItem<BlockItem> VIS_EXTRACTOR_BASE_ITEM = ITEMS.registerItem("vis_extractor_base", properties -> new BlockItem(ModBlocks.VIS_EXTRACTOR_BASE.get(), properties));
    public static final DeferredItem<BlockItem> VIS_MOTOR_ITEM = ITEMS.registerItem("vis_motor", properties -> new BlockItem(ModBlocks.VIS_MOTOR.get(), properties));

    // --- Dynamic Weapon Forging Blocks ---
    public static final DeferredItem<BlockItem> EIDOLIC_FOCAL_PEDESTAL_ITEM = ITEMS.registerItem("eidolic_focal_pedestal", properties -> new BlockItem(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get(), properties));
    public static final DeferredItem<BlockItem> ATTUNEMENT_PEDESTAL_ITEM = ITEMS.registerItem("attunement_pedestal", properties -> new BlockItem(ModBlocks.ATTUNEMENT_PEDESTAL.get(), properties));
    public static final DeferredItem<BlockItem> MORPHIC_LOOM_ITEM = ITEMS.registerItem("morphic_loom", properties -> new BlockItem(ModBlocks.MORPHIC_LOOM.get(), properties));

    // ==========================================
    // DYNAMIC EIDOLIC WEAPONS & TOOLS
    // ==========================================

    public static final DeferredItem<Item> DYNAMIC_SWORD = ITEMS.register("dynamic_sword",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_AXE = ITEMS.register("dynamic_axe",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_PICKAXE = ITEMS.register("dynamic_pickaxe",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_SHOVEL = ITEMS.register("dynamic_shovel",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_ADZE = ITEMS.register("dynamic_adze",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_PAXEL = ITEMS.register("dynamic_paxel",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_SPEAR = ITEMS.register("dynamic_spear",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_MACE = ITEMS.register("dynamic_mace",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_MORNING_STAR = ITEMS.register("dynamic_morning_star",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_WARHAMMER = ITEMS.register("dynamic_warhammer",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_SHORTBOW = ITEMS.register("dynamic_shortbow",
            name -> new DynamicVisBowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_LONGBOW = ITEMS.register("dynamic_longbow",
            name -> new DynamicVisBowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_WAR_BOW = ITEMS.register("dynamic_war_bow",
            name -> new DynamicVisBowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_CROSSBOW = ITEMS.register("dynamic_crossbow",
            name -> new DynamicVisCrossbowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final DeferredItem<Item> DYNAMIC_REPEATER = ITEMS.register("dynamic_repeater",
            name -> new DynamicVisCrossbowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

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
    public static final DeferredItem<BlockItem> CREATIVE_PARTICLE_GENERATOR_ITEM = ITEMS.registerItem("creative_particle_generator", properties -> new BlockItem(ModBlocks.CREATIVE_PARTICLE_GENERATOR.get(), properties));

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

    // --- Void Rifts Item ---
    public static final DeferredItem<BlockItem> VOID_RIFT_ITEM = ITEMS.registerItem("void_rift", properties -> new BlockItem(ModBlocks.VOID_RIFT.get(), properties));

    // Others
    public static final DeferredItem<BlockItem> VIS_VITAE_ANCHOR_ITEM = ITEMS.registerItem("vis_vitae_anchor", properties -> new BlockItem(ModBlocks.VIS_VITAE_ANCHOR.get(), properties));
    public static final DeferredItem<BlockItem> VIS_VITAE_CONDENSER_ITEM = ITEMS.registerItem("vis_vitae_condenser", properties -> new BlockItem(ModBlocks.VIS_VITAE_CONDENSER.get(), properties));
    public static final DeferredItem<BlockItem> VIS_VITAE_VACUUM_ITEM = ITEMS.registerItem("vis_vitae_vacuum", properties -> new BlockItem(ModBlocks.VIS_VITAE_VACUUM.get(), properties));
    public static final DeferredItem<BlockItem> VITAE_BARREL_ITEM = ITEMS.registerItem("vitae_barrel", properties -> new BlockItem(ModBlocks.VITAE_BARREL.get(), properties));
    public static final DeferredItem<BlockItem> MANA_FILTER_ITEM = ITEMS.registerItem("mana_filter", properties -> new BlockItem(ModBlocks.MANA_FILTER.get(), properties));

    public static final DeferredItem<BlockItem> AETHERIC_SYNTHESIZER_ITEM = ITEMS.registerItem("aetheric_synthesizer", properties -> new BlockItem(ModBlocks.AETHERIC_SYNTHESIZER.get(), properties));
    public static final DeferredItem<BlockItem> AETHERIC_AUTOMATOR_ITEM = ITEMS.registerItem("aetheric_automator", properties -> new BlockItem(ModBlocks.AETHERIC_AUTOMATOR.get(), properties));
    public static final DeferredItem<BlockItem> SYNTHESIZER_USER_INTERFACE_ITEM = ITEMS.registerItem("synthesizer_user_interface", properties -> new BlockItem(ModBlocks.SYNTHESIZER_USER_INTERFACE.get(), properties));

    public static final DeferredItem<BlockItem> ARCANE_LOOM_ITEM = ITEMS.registerItem("arcane_loom", properties -> new BlockItem(ModBlocks.ARCANE_LOOM.get(), properties));
    public static final DeferredItem<BlockItem> ESSENCE_FORGE_ITEM = ITEMS.registerItem("essence_forge", properties -> new BlockItem(ModBlocks.ESSENCE_FORGE.get(), properties));
    public static final DeferredItem<BlockItem> ARCANE_ANVIL_ITEM = ITEMS.registerItem("arcane_anvil", properties -> new BlockItem(ModBlocks.ARCANE_ANVIL.get(), properties));

    // ==========================================
    // DATAGEN ITEM LIST
    // Any basic item added to this list will automatically have a flat model generated.
    // ==========================================
    public static final List<DeferredItem<? extends Item>> SIMPLE_ITEMS = List.of(
            ARCANUM_FOCUS, VIS_VALUE_DETECTOR, ARCANE_BRICK_PIECE, ARCANE_CLAY,
            SMALL_AMPOULE, MEDIUM_AMPOULE, LARGE_AMPOULE, ESSENCE_HARVESTING_BLADE, SOULBOUND_BLADE,
            OBLIVION_BLADE, TIDAL_TRIDENT, VOID_SWORD, BASALT_PICKAXE, WHISPERWOOD_WAND, SHIMMERING_FOCUS,
            ARCANITE_INGOT, VISCANITE_INGOT, RESONITE_INGOT, EIDOLITE_INGOT, CHARGED_ARCANITE_INGOT, CHARGED_VISCANITE_INGOT, CHARGED_RESONITE_INGOT, CHARGED_EIDOLITE_INGOT,
            ARCANITE_NUGGET, VISCANITE_NUGGET, RESONITE_NUGGET, EIDOLITE_NUGGET, CHARGED_ARCANITE_NUGGET, CHARGED_VISCANITE_NUGGET, CHARGED_RESONITE_NUGGET, CHARGED_EIDOLITE_NUGGET,
            ARCANITE_WIRE, VISCANITE_WIRE, RESONITE_WIRE, EIDOLITE_WIRE, COATED_ARCANITE_WIRE, COATED_VISCANITE_WIRE, COATED_RESONITE_WIRE, COATED_EIDOLITE_WIRE, ARCANITE_SPOOL, VISCANITE_SPOOL, RESONITE_SPOOL, EIDOLITE_SPOOL,
            ARCANITE_FILAMENT, VISCANITE_FILAMENT, RESONITE_FILAMENT, EIDOLITE_FILAMENT, CHARGED_ARCANITE_FILAMENT, CHARGED_VISCANITE_FILAMENT, CHARGED_RESONITE_FILAMENT, CHARGED_EIDOLITE_FILAMENT, COATED_ARCANITE_FILAMENT, COATED_VISCANITE_FILAMENT, COATED_RESONITE_FILAMENT, COATED_EIDOLITE_FILAMENT,
            ARCANITE_FRAME, VISCANITE_FRAME, ARCANITE_GEARSET, VISCANITE_GEARSET, RESONITE_GEARSET,
            ARCANITE_LARGE_GEAR, VISCANITE_LARGE_GEAR, RESONITE_LARGE_GEAR, ARCANITE_SMALL_GEAR, VISCANITE_SMALL_GEAR, RESONITE_SMALL_GEAR,
            ARCANITE_PLATE, VISCANITE_PLATE, RESONITE_PLATE, VISCANITE_PISTON, RESONITE_PISTON,
            BELLOWS_CRANK_ASSEMBLY, PRESSURE_GRADED_GASKET, ENTROPIC_HEAT_SINK,
            ARCANITE_WEAPON_CORE, CHARGED_ARCANITE_WEAPON_CORE, ANCIENT_ARCANITE_WEAPON_CORE,
            VISCANITE_WEAPON_CORE, CHARGED_VISCANITE_WEAPON_CORE, ANCIENT_VISCANITE_WEAPON_CORE,
            RESONITE_WEAPON_CORE, CHARGED_RESONITE_WEAPON_CORE, ANCIENT_RESONITE_WEAPON_CORE,
            ARCANITE_TOOL_CORE, CHARGED_ARCANITE_TOOL_CORE, ANCIENT_ARCANITE_TOOL_CORE,
            VISCANITE_TOOL_CORE, CHARGED_VISCANITE_TOOL_CORE, ANCIENT_VISCANITE_TOOL_CORE,
            RESONITE_TOOL_CORE, CHARGED_RESONITE_TOOL_CORE, ANCIENT_RESONITE_TOOL_CORE,
            EIDOLITE_CORE, CHARGED_EIDOLITE_CORE, ANCIENT_EIDOLITE_CORE,
            SOUL_BOUND_COMMUTATOR, VISCANITE_STATOR, RESONITE_STATOR,
            GLADIUS_SHAPE_CONCEPT, LONGSWORD_SHAPE_CONCEPT, SHORTSWORD_SHAPE_CONCEPT, AKRAFENA_SHAPE_CONCEPT,
            HAND_AXE_SHAPE_CONCEPT, WAR_AXE_SHAPE_CONCEPT, POLE_AXE_SHAPE_CONCEPT, HALBERD_SHAPE_CONCEPT, BEARD_AXE_SHAPE_CONCEPT,
            SHORTBOW_SHAPE_CONCEPT, LONGBOW_SHAPE_CONCEPT, CROSSBOW_SHAPE_CONCEPT, REPEATER_SHAPE_CONCEPT, WAR_BOW_SHAPE_CONCEPT,
            PICKAXE_SHAPE_CONCEPT, SHOVEL_SHAPE_CONCEPT, ADZE_SHAPE_CONCEPT, PAXEL_SHAPE_CONCEPT,
            SPEAR_SHAPE_CONCEPT, MACE_SHAPE_CONCEPT, MORNING_STAR_SHAPE_CONCEPT, WARHAMMER_SHAPE_CONCEPT,
            ORBIS_ACCEPTOR, SPELL_GEM,
            FOCAL_LENS_ASSEMBLY, AETHERIC_LOGIC_GATE, REFRACTION_GRID_MESH, ICHOR_CAPILLARY_TUBE, VIS_CAPACITOR_PLATE, HARMONIC_FEEDBACK_LOOP, AETHERIC_PITCH_PIPE, FREQUENCY_SPLITTER_PRISM, TUNING_FORK, RESONITE_TUNING_FORK, VOID_RESONANT_TUNING_FORK, SPECTRAL_GLYPH_PLATE, TETHER_ANCHOR_PIN,
            ARCANITE_ICHOR_VALVE, VISCANITE_ICHOR_VALVE, ARCANITE_FUME_VALVE, VISCANITE_FUME_VALVE, AETHERIC_FLOW_REGULATOR, VOID_SEALED_O_RING, FUME_CONDENSER_COIL, ICHOR_EVAPORATOR_COIL,
            MORPHIC_LOOM_ITEM, VOID_RIFT_ITEM,
            RUNE_FEHU, RUNE_URUZ, RUNE_THURISAZ, RUNE_ANSUZ, RUNE_RAIDO, RUNE_KENAZ, RUNE_GEBO, RUNE_WUNJO,
            RUNE_HAGALAZ, RUNE_NAUTHIZ, RUNE_ISA, RUNE_JERA, RUNE_EIHWAZ, RUNE_PERTHRO, RUNE_ALGIZ, RUNE_SOWILO,
            RUNE_TIWAZ, RUNE_BERKANO, RUNE_EHWAZ, RUNE_MANNAZ, RUNE_LAGUZ, RUNE_INGWAZ, RUNE_DAGAZ, RUNE_OTHALA
    );
}