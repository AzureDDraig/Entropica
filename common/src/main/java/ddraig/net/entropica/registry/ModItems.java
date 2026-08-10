package ddraig.net.entropica.registry;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static class ItemsWrapper {
        private final DeferredRegister<Item> parent = DeferredRegister.create("entropica", Registries.ITEM);

        public void register() {
            parent.register();
        }

        public void register(Object ignoredBus) {
            parent.register();
        }

        public <T extends Item> RegistrySupplier<T> registerItem(String name, java.util.function.Function<Item.Properties, T> factory) {
            return parent.register(name, () -> factory.apply(itemProperties(name)));
        }

        public <T extends Item> RegistrySupplier<T> register(String name, java.util.function.Function<ResourceLocation, T> factory) {
            return parent.register(name, () -> factory.apply(ResourceLocation.fromNamespaceAndPath("entropica", name)));
        }

        public java.util.Collection<RegistrySupplier<Item>> getEntries() {
            java.util.List<RegistrySupplier<Item>> list = new java.util.ArrayList<>();
            parent.forEach(list::add);
            return list;
        }
    }

    private static Item.Properties itemProperties(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("entropica", name)));
    }

    public static final ItemsWrapper ITEMS = new ItemsWrapper();

    // --- Standalone Items ---
    public static final RegistrySupplier<Item> ARCANUM_FOCUS = ITEMS.registerItem("arcanum_focus", Item::new);
    public static final RegistrySupplier<Item> MATERIA_VALUE_DETECTOR = ITEMS.registerItem("materia_value_detector", Item::new);
    public static final RegistrySupplier<Item> EIDOLON_PATHMARKER = ITEMS.registerItem("eidolon_pathmarker", properties -> new EidolonPathmarkerItem(properties.stacksTo(1)));
    public static final RegistrySupplier<Item> ENTROPIC_CODEX = ITEMS.registerItem("entropic_codex", properties -> new EntropicCodexItem(properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistrySupplier<Item> SACRIFICIAL_KNIFE = ITEMS.registerItem("sacrificial_knife", properties -> new SacrificialKnifeItem(properties.stacksTo(1).durability(100)));

    // Veil Fox Items
    public static final RegistrySupplier<Item> VEIL_SHARD = ITEMS.registerItem("veil_shard", Item::new);

    public static final RegistrySupplier<Item> MATERIA_BLESSING_SHARD = ITEMS.registerItem("materia_blessing_shard", Item::new);
    public static final RegistrySupplier<Item> RAW_RUBBER = ITEMS.registerItem("raw_rubber", Item::new);

    // --- Aetheric Vision Equipment ---
    public static final RegistrySupplier<Item> AETHERIC_MONOCLE = ITEMS.registerItem("aetheric_monocle", properties -> new ddraig.net.entropica.item.AethericVisionItem(
            properties.stacksTo(1).equippable(net.minecraft.world.entity.EquipmentSlot.HEAD)
    ));

    public static final RegistrySupplier<Item> ARCANE_BRICK_PIECE = ITEMS.registerItem("arcane_brick_piece", Item::new);
    public static final RegistrySupplier<Item> ARCANE_CLAY = ITEMS.registerItem("arcane_clay", Item::new);

    // --- Empty Ampoules ---
    public static final RegistrySupplier<Item> SMALL_AMPOULE = ITEMS.registerItem("small_ampoule", Item::new);
    public static final RegistrySupplier<Item> MEDIUM_AMPOULE = ITEMS.registerItem("medium_ampoule", Item::new);
    public static final RegistrySupplier<Item> LARGE_AMPOULE = ITEMS.registerItem("large_ampoule", Item::new);

    // --- Weapons & Tools ---
    public static final RegistrySupplier<Item> ESSENCE_HARVESTING_BLADE = ITEMS.registerItem("essence_harvesting_blade", properties -> new EssenceHarvestingBladeItem(
            properties.sword(ToolMaterial.DIAMOND, 3.0F, -2.4F)
    ));

    public static final RegistrySupplier<Item> SOULBOUND_BLADE = ITEMS.registerItem("soulbound_blade", properties -> new Item(
            properties.sword(ToolMaterial.DIAMOND, 0, -2.4F)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
                    .component(ModDataComponents.VIS_WEAPON_STATE.get(), new VisWeaponState.Builder(EssenceType.SOULFIRE, 6.0F, 1).build())
    ));

    public static final RegistrySupplier<Item> OBLIVION_BLADE = ITEMS.registerItem("oblivion_blade", properties -> new Item(
            properties.sword(ToolMaterial.NETHERITE, 0, -2.4F)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
                    .component(ModDataComponents.VIS_WEAPON_STATE.get(), new VisWeaponState.Builder(EssenceType.OBLIVION, 8.5F, 2).build())
    ));

    public static final RegistrySupplier<Item> TIDAL_TRIDENT = ITEMS.registerItem("tidal_trident", properties -> new Item(
            properties.sword(ToolMaterial.DIAMOND, 0, -2.4F)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
                    .component(ModDataComponents.VIS_WEAPON_STATE.get(), new VisWeaponState.Builder(EssenceType.WATER, 7.0F, 1).build())
    ));

    public static final RegistrySupplier<Item> VOID_SWORD = ITEMS.registerItem("void_sword", properties -> new Item(
            properties.sword(ToolMaterial.DIAMOND, 0, -2.4F)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
                    .component(ModDataComponents.VIS_WEAPON_STATE.get(), new VisWeaponState.Builder(EssenceType.VOID, 8.5F, 2).build())
    ));

    public static final RegistrySupplier<Item> BASALT_PICKAXE = ITEMS.registerItem("basalt_pickaxe", Item::new);
    public static final RegistrySupplier<Item> WHISPERWOOD_WAND = ITEMS.registerItem("whisperwood_wand", Item::new);
    public static final RegistrySupplier<Item> SHIMMERING_FOCUS = ITEMS.registerItem("shimmering_focus", Item::new);

    // --- Crafting Components ---
    // Metals & Alloys
    public static final RegistrySupplier<Item> ARCANITE_INGOT = ITEMS.registerItem("arcanite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_ingot"));
    public static final RegistrySupplier<Item> VISCANITE_INGOT = ITEMS.registerItem("viscanite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_ingot"));
    public static final RegistrySupplier<Item> RESONITE_INGOT = ITEMS.registerItem("resonite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_ingot"));
    public static final RegistrySupplier<Item> EIDOLITE_INGOT = ITEMS.registerItem("eidolite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.eidolite_ingot"));

    public static final RegistrySupplier<Item> CHARGED_ARCANITE_INGOT = ITEMS.registerItem("charged_arcanite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_arcanite_ingot"));
    public static final RegistrySupplier<Item> CHARGED_VISCANITE_INGOT = ITEMS.registerItem("charged_viscanite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_viscanite_ingot"));
    public static final RegistrySupplier<Item> CHARGED_RESONITE_INGOT = ITEMS.registerItem("charged_resonite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_resonite_ingot"));
    public static final RegistrySupplier<Item> CHARGED_EIDOLITE_INGOT = ITEMS.registerItem("charged_eidolite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_eidolite_ingot"));

    // T6-T10 Metals & Alloys
    public static final RegistrySupplier<Item> CAPUTITE_INGOT = ITEMS.registerItem("caputite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.caputite_ingot"));
    public static final RegistrySupplier<Item> CHARGED_CAPUTITE_INGOT = ITEMS.registerItem("charged_caputite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_caputite_ingot"));

    public static final RegistrySupplier<Item> SANGUINITE_INGOT = ITEMS.registerItem("sanguinite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.sanguinite_ingot"));
    public static final RegistrySupplier<Item> CHARGED_SANGUINITE_INGOT = ITEMS.registerItem("charged_sanguinite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_sanguinite_ingot"));

    public static final RegistrySupplier<Item> MERCURITE_INGOT = ITEMS.registerItem("mercurite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.mercurite_ingot"));
    public static final RegistrySupplier<Item> CHARGED_MERCURITE_INGOT = ITEMS.registerItem("charged_mercurite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_mercurite_ingot"));

    public static final RegistrySupplier<Item> EUCLIDITE_INGOT = ITEMS.registerItem("euclidite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.euclidite_ingot"));
    public static final RegistrySupplier<Item> CHARGED_EUCLIDITE_INGOT = ITEMS.registerItem("charged_euclidite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_euclidite_ingot"));

    public static final RegistrySupplier<Item> ATHANORITE_INGOT = ITEMS.registerItem("athanorite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.athanorite_ingot"));
    public static final RegistrySupplier<Item> CHARGED_ATHANORITE_INGOT = ITEMS.registerItem("charged_athanorite_ingot", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_athanorite_ingot"));

    public static final RegistrySupplier<Item> ARCANITE_NUGGET = ITEMS.registerItem("arcanite_nugget", Item::new);
    public static final RegistrySupplier<Item> VISCANITE_NUGGET = ITEMS.registerItem("viscanite_nugget", Item::new);
    public static final RegistrySupplier<Item> RESONITE_NUGGET = ITEMS.registerItem("resonite_nugget", Item::new);
    public static final RegistrySupplier<Item> EIDOLITE_NUGGET = ITEMS.registerItem("eidolite_nugget", Item::new);

    public static final RegistrySupplier<Item> CHARGED_ARCANITE_NUGGET = ITEMS.registerItem("charged_arcanite_nugget", Item::new);
    public static final RegistrySupplier<Item> CHARGED_VISCANITE_NUGGET = ITEMS.registerItem("charged_viscanite_nugget", Item::new);
    public static final RegistrySupplier<Item> CHARGED_RESONITE_NUGGET = ITEMS.registerItem("charged_resonite_nugget", Item::new);
    public static final RegistrySupplier<Item> CHARGED_EIDOLITE_NUGGET = ITEMS.registerItem("charged_eidolite_nugget", Item::new);

    // Wires, Filaments & Spools
    public static final RegistrySupplier<Item> ARCANITE_WIRE = ITEMS.registerItem("arcanite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_wire"));
    public static final RegistrySupplier<Item> VISCANITE_WIRE = ITEMS.registerItem("viscanite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_wire"));
    public static final RegistrySupplier<Item> RESONITE_WIRE = ITEMS.registerItem("resonite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_wire"));
    public static final RegistrySupplier<Item> EIDOLITE_WIRE = ITEMS.registerItem("eidolite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.eidolite_wire"));

    public static final RegistrySupplier<Item> COATED_ARCANITE_WIRE = ITEMS.registerItem("coated_arcanite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_arcanite_wire"));
    public static final RegistrySupplier<Item> COATED_VISCANITE_WIRE = ITEMS.registerItem("coated_viscanite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_viscanite_wire"));
    public static final RegistrySupplier<Item> COATED_RESONITE_WIRE = ITEMS.registerItem("coated_resonite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_resonite_wire"));
    public static final RegistrySupplier<Item> COATED_EIDOLITE_WIRE = ITEMS.registerItem("coated_eidolite_wire", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_eidolite_wire"));

    public static final RegistrySupplier<Item> ARCANITE_FILAMENT = ITEMS.registerItem("arcanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_filament"));
    public static final RegistrySupplier<Item> VISCANITE_FILAMENT = ITEMS.registerItem("viscanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_filament"));
    public static final RegistrySupplier<Item> RESONITE_FILAMENT = ITEMS.registerItem("resonite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_filament"));
    public static final RegistrySupplier<Item> EIDOLITE_FILAMENT = ITEMS.registerItem("eidolite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.eidolite_filament"));

    public static final RegistrySupplier<Item> ARCANITE_SPOOL = ITEMS.registerItem("arcanite_spool", properties -> new SpoolItem(properties.stacksTo(1), "tooltip.entropica.arcanite_spool", ARCANITE_WIRE, ARCANITE_FILAMENT));
    public static final RegistrySupplier<Item> VISCANITE_SPOOL = ITEMS.registerItem("viscanite_spool", properties -> new SpoolItem(properties.stacksTo(1), "tooltip.entropica.viscanite_spool", VISCANITE_WIRE, VISCANITE_FILAMENT));
    public static final RegistrySupplier<Item> RESONITE_SPOOL = ITEMS.registerItem("resonite_spool", properties -> new SpoolItem(properties.stacksTo(1), "tooltip.entropica.resonite_spool", RESONITE_WIRE, RESONITE_FILAMENT));
    public static final RegistrySupplier<Item> EIDOLITE_SPOOL = ITEMS.registerItem("eidolite_spool", properties -> new SpoolItem(properties.stacksTo(1), "tooltip.entropica.eidolite_spool", EIDOLITE_WIRE, EIDOLITE_FILAMENT));

    public static final RegistrySupplier<Item> CHARGED_ARCANITE_FILAMENT = ITEMS.registerItem("charged_arcanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_arcanite_filament"));
    public static final RegistrySupplier<Item> CHARGED_VISCANITE_FILAMENT = ITEMS.registerItem("charged_viscanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_viscanite_filament"));
    public static final RegistrySupplier<Item> CHARGED_RESONITE_FILAMENT = ITEMS.registerItem("charged_resonite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_resonite_filament"));
    public static final RegistrySupplier<Item> CHARGED_EIDOLITE_FILAMENT = ITEMS.registerItem("charged_eidolite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_eidolite_filament"));
    public static final RegistrySupplier<Item> COATED_ARCANITE_FILAMENT = ITEMS.registerItem("coated_arcanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_arcanite_filament"));
    public static final RegistrySupplier<Item> COATED_VISCANITE_FILAMENT = ITEMS.registerItem("coated_viscanite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_viscanite_filament"));
    public static final RegistrySupplier<Item> COATED_RESONITE_FILAMENT = ITEMS.registerItem("coated_resonite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_resonite_filament"));
    public static final RegistrySupplier<Item> COATED_EIDOLITE_FILAMENT = ITEMS.registerItem("coated_eidolite_filament", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.coated_eidolite_filament"));

    // Mechanical Parts
    public static final RegistrySupplier<Item> ARCANITE_FRAME = ITEMS.registerItem("arcanite_frame", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.reinforced_arcanite_frame"));
    public static final RegistrySupplier<Item> VISCANITE_FRAME = ITEMS.registerItem("viscanite_frame", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_frame"));

    // Gearsets
    public static final RegistrySupplier<Item> ARCANITE_GEARSET = ITEMS.registerItem("arcanite_gearset", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_gearset"));
    public static final RegistrySupplier<Item> VISCANITE_GEARSET = ITEMS.registerItem("viscanite_gearset", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_gearset"));
    public static final RegistrySupplier<Item> RESONITE_GEARSET = ITEMS.registerItem("resonite_gearset", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_gearset"));

    // Large Gears
    public static final RegistrySupplier<Item> ARCANITE_LARGE_GEAR = ITEMS.registerItem("arcanite_large_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_large_gear"));
    public static final RegistrySupplier<Item> VISCANITE_LARGE_GEAR = ITEMS.registerItem("viscanite_large_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_large_gear"));
    public static final RegistrySupplier<Item> RESONITE_LARGE_GEAR = ITEMS.registerItem("resonite_large_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_large_gear"));

    // Small Gears
    public static final RegistrySupplier<Item> ARCANITE_SMALL_GEAR = ITEMS.registerItem("arcanite_small_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_small_gear"));
    public static final RegistrySupplier<Item> VISCANITE_SMALL_GEAR = ITEMS.registerItem("viscanite_small_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_small_gear"));
    public static final RegistrySupplier<Item> RESONITE_SMALL_GEAR = ITEMS.registerItem("resonite_small_gear", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_small_gear"));

    // Plates & Pistons
    public static final RegistrySupplier<Item> ARCANITE_PLATE = ITEMS.registerItem("arcanite_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_plate"));
    public static final RegistrySupplier<Item> VISCANITE_PLATE = ITEMS.registerItem("viscanite_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_plate"));
    public static final RegistrySupplier<Item> RESONITE_PLATE = ITEMS.registerItem("resonite_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_plate"));
    public static final RegistrySupplier<Item> RESONITE_PISTON = ITEMS.registerItem("resonite_piston", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_piston"));

    public static final RegistrySupplier<Item> BELLOWS_CRANK_ASSEMBLY = ITEMS.registerItem("bellows_crank_assembly", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.bellows_crank_assembly"));
    public static final RegistrySupplier<Item> ENTROPIC_HEAT_SINK = ITEMS.registerItem("entropic_heat_sink", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.entropic_heat_sink"));

    // Fluid & Gas Machine Components
    public static final RegistrySupplier<Item> ARCANITE_ICHOR_VALVE = ITEMS.registerItem("arcanite_ichor_valve", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_ichor_valve"));
    public static final RegistrySupplier<Item> VISCANITE_HYDRAULIC_VALVE = ITEMS.registerItem("viscanite_hydraulic_valve", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_hydraulic_valve"));
    public static final RegistrySupplier<Item> ARCANITE_FUME_VALVE = ITEMS.registerItem("arcanite_fume_valve", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_fume_valve"));
    public static final RegistrySupplier<Item> VISCANITE_VAPOR_VALVE = ITEMS.registerItem("viscanite_vapor_valve", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_vapor_valve"));
    public static final RegistrySupplier<Item> AETHERIC_FLOW_REGULATOR = ITEMS.registerItem("aetheric_flow_regulator", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.aetheric_flow_regulator"));
    public static final RegistrySupplier<Item> VOID_SEALED_O_RING = ITEMS.registerItem("void_sealed_o_ring", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.void_sealed_o_ring"));
    public static final RegistrySupplier<Item> FUME_CONDENSER_COIL = ITEMS.registerItem("fume_condenser_coil", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.fume_condenser_coil"));
    public static final RegistrySupplier<Item> ICHOR_EVAPORATOR_COIL = ITEMS.registerItem("ichor_evaporator_coil", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ichor_evaporator_coil"));

    // Standard Weapon & Tool Cores
    public static final RegistrySupplier<Item> ARCANITE_WEAPON_CORE = ITEMS.registerItem("arcanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_weapon_core"));
    public static final RegistrySupplier<Item> CHARGED_ARCANITE_WEAPON_CORE = ITEMS.registerItem("charged_arcanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_arcanite_weapon_core"));
    public static final RegistrySupplier<Item> ANCIENT_ARCANITE_WEAPON_CORE = ITEMS.registerItem("ancient_arcanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_arcanite_weapon_core"));

    public static final RegistrySupplier<Item> VISCANITE_WEAPON_CORE = ITEMS.registerItem("viscanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_weapon_core"));
    public static final RegistrySupplier<Item> CHARGED_VISCANITE_WEAPON_CORE = ITEMS.registerItem("charged_viscanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_viscanite_weapon_core"));
    public static final RegistrySupplier<Item> ANCIENT_VISCANITE_WEAPON_CORE = ITEMS.registerItem("ancient_viscanite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_viscanite_weapon_core"));

    public static final RegistrySupplier<Item> RESONITE_WEAPON_CORE = ITEMS.registerItem("resonite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_weapon_core"));
    public static final RegistrySupplier<Item> CHARGED_RESONITE_WEAPON_CORE = ITEMS.registerItem("charged_resonite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_resonite_weapon_core"));
    public static final RegistrySupplier<Item> ANCIENT_RESONITE_WEAPON_CORE = ITEMS.registerItem("ancient_resonite_weapon_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_resonite_weapon_core"));

    public static final RegistrySupplier<Item> ARCANITE_TOOL_CORE = ITEMS.registerItem("arcanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.arcanite_tool_core"));
    public static final RegistrySupplier<Item> CHARGED_ARCANITE_TOOL_CORE = ITEMS.registerItem("charged_arcanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_arcanite_tool_core"));
    public static final RegistrySupplier<Item> ANCIENT_ARCANITE_TOOL_CORE = ITEMS.registerItem("ancient_arcanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_arcanite_tool_core"));

    public static final RegistrySupplier<Item> VISCANITE_TOOL_CORE = ITEMS.registerItem("viscanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_tool_core"));
    public static final RegistrySupplier<Item> CHARGED_VISCANITE_TOOL_CORE = ITEMS.registerItem("charged_viscanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_viscanite_tool_core"));
    public static final RegistrySupplier<Item> ANCIENT_VISCANITE_TOOL_CORE = ITEMS.registerItem("ancient_viscanite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_viscanite_tool_core"));

    public static final RegistrySupplier<Item> RESONITE_TOOL_CORE = ITEMS.registerItem("resonite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_tool_core"));
    public static final RegistrySupplier<Item> CHARGED_RESONITE_TOOL_CORE = ITEMS.registerItem("charged_resonite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_resonite_tool_core"));
    public static final RegistrySupplier<Item> ANCIENT_RESONITE_TOOL_CORE = ITEMS.registerItem("ancient_resonite_tool_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_resonite_tool_core"));

    // Eidolite Unified Cores (For Constructs/Weapons/Tools)
    public static final RegistrySupplier<Item> EIDOLITE_CORE = ITEMS.registerItem("eidolite_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.eidolite_core"));
    public static final RegistrySupplier<Item> CHARGED_EIDOLITE_CORE = ITEMS.registerItem("charged_eidolite_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.charged_eidolite_core"));
    public static final RegistrySupplier<Item> ANCIENT_EIDOLITE_CORE = ITEMS.registerItem("ancient_eidolite_core", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ancient_eidolite_core"));

    public static final RegistrySupplier<Item> SOUL_BOUND_COMMUTATOR = ITEMS.registerItem("soul_bound_commutator", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.soul_bound_commutator"));
    public static final RegistrySupplier<Item> VISCANITE_STATOR = ITEMS.registerItem("viscanite_stator", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.viscanite_stator"));
    public static final RegistrySupplier<Item> RESONITE_STATOR = ITEMS.registerItem("resonite_stator", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_stator"));

    // Shape Concepts (Swords)
    public static final RegistrySupplier<Item> GLADIUS_SHAPE_CONCEPT = ITEMS.registerItem("gladius_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> LONGSWORD_SHAPE_CONCEPT = ITEMS.registerItem("longsword_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> SHORTSWORD_SHAPE_CONCEPT = ITEMS.registerItem("shortsword_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> AKRAFENA_SHAPE_CONCEPT = ITEMS.registerItem("akrafena_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Shape Concepts (Axes)
    public static final RegistrySupplier<Item> HAND_AXE_SHAPE_CONCEPT = ITEMS.registerItem("hand_axe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> WAR_AXE_SHAPE_CONCEPT = ITEMS.registerItem("war_axe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> POLE_AXE_SHAPE_CONCEPT = ITEMS.registerItem("pole_axe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> HALBERD_SHAPE_CONCEPT = ITEMS.registerItem("halberd_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> BEARD_AXE_SHAPE_CONCEPT = ITEMS.registerItem("beard_axe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Shape Concepts (Bows)
    public static final RegistrySupplier<Item> SHORTBOW_SHAPE_CONCEPT = ITEMS.registerItem("shortbow_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> LONGBOW_SHAPE_CONCEPT = ITEMS.registerItem("longbow_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> CROSSBOW_SHAPE_CONCEPT = ITEMS.registerItem("crossbow_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> REPEATER_SHAPE_CONCEPT = ITEMS.registerItem("repeater_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> WAR_BOW_SHAPE_CONCEPT = ITEMS.registerItem("war_bow_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Shape Concepts (Tools)
    public static final RegistrySupplier<Item> PICKAXE_SHAPE_CONCEPT = ITEMS.registerItem("pickaxe_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> SHOVEL_SHAPE_CONCEPT = ITEMS.registerItem("shovel_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> ADZE_SHAPE_CONCEPT = ITEMS.registerItem("adze_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> PAXEL_SHAPE_CONCEPT = ITEMS.registerItem("paxel_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Shape Concepts (Others)
    public static final RegistrySupplier<Item> SPEAR_SHAPE_CONCEPT = ITEMS.registerItem("spear_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> MACE_SHAPE_CONCEPT = ITEMS.registerItem("mace_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> MORNING_STAR_SHAPE_CONCEPT = ITEMS.registerItem("morning_star_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));
    public static final RegistrySupplier<Item> WARHAMMER_SHAPE_CONCEPT = ITEMS.registerItem("warhammer_shape_concept", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.shape_concept"));

    // Standalone Crafting Item
    public static final RegistrySupplier<Item> ORBIS_ACCEPTOR = ITEMS.registerItem("orbis_acceptor", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.orbis_acceptor"));
    public static final RegistrySupplier<Item> SPELL_GEM = ITEMS.registerItem("spell_gem", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.spell_gem"));

    // Optical & Arcane Internals
    public static final RegistrySupplier<Item> FOCAL_LENS_ASSEMBLY = ITEMS.registerItem("focal_lens_assembly", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.focal_lens_assembly"));
    public static final RegistrySupplier<Item> AETHERIC_LOGIC_GATE = ITEMS.registerItem("aetheric_logic_gate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.aetheric_logic_gate"));
    public static final RegistrySupplier<Item> REFRACTION_GRID_MESH = ITEMS.registerItem("refraction_grid_mesh", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.refraction_grid_mesh"));
    public static final RegistrySupplier<Item> ICHOR_CAPILLARY_TUBE = ITEMS.registerItem("ichor_capillary_tube", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.ichor_capillary_tube"));
    public static final RegistrySupplier<Item> MATERIA_CAPACITOR_PLATE = ITEMS.registerItem("materia_capacitor_plate", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.materia_capacitor_plate"));
    public static final RegistrySupplier<Item> HARMONIC_FEEDBACK_LOOP = ITEMS.registerItem("harmonic_feedback_loop", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.harmonic_feedback_loop"));
    public static final RegistrySupplier<Item> AETHERIC_PITCH_PIPE = ITEMS.registerItem("aetheric_pitch_pipe", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.aetheric_pitch_pipe"));
    public static final RegistrySupplier<Item> FREQUENCY_SPLITTER_PRISM = ITEMS.registerItem("frequency_splitter_prism", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.frequency_splitter_prism"));
    public static final RegistrySupplier<Item> TUNING_FORK = ITEMS.registerItem("tuning_fork", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.tuning_fork"));
    public static final RegistrySupplier<Item> RESONITE_TUNING_FORK = ITEMS.registerItem("resonite_tuning_fork", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.resonite_tuning_fork"));

    // --- VOID LOGISTICS ---
    public static final RegistrySupplier<Item> VOID_RESONANT_TUNING_FORK = ITEMS.registerItem("void_resonant_tuning_fork", VoidResonantTuningForkItem::new);
    public static final RegistrySupplier<Item> TETHER_ANCHOR_PIN = ITEMS.registerItem("tether_anchor_pin", properties -> new EntropicaComponentItem(properties, "tooltip.entropica.tether_anchor_pin"));

    // ==========================================
    // --- RUNES (ELDER FUTHARK) ---
    // ==========================================
    public static final RegistrySupplier<Item> RUNE_FEHU = ITEMS.registerItem("rune_fehu", p -> new RuneItem(p, "ᚠ", "Fehu", "Wealth / Filtering"));
    public static final RegistrySupplier<Item> RUNE_URUZ = ITEMS.registerItem("rune_uruz", p -> new RuneItem(p, "ᚢ", "Uruz", "Strength / Speed"));
    public static final RegistrySupplier<Item> RUNE_THURISAZ = ITEMS.registerItem("rune_thurisaz", p -> new RuneItem(p, "ᚦ", "Thurisaz", "Gateway / Tearing"));
    public static final RegistrySupplier<Item> RUNE_ANSUZ = ITEMS.registerItem("rune_ansuz", p -> new RuneItem(p, "ᚨ", "Ansuz", "Signals / Redstone"));
    public static final RegistrySupplier<Item> RUNE_RAIDO = ITEMS.registerItem("rune_raido", p -> new RuneItem(p, "ᚱ", "Raido", "Journey / Routing"));
    public static final RegistrySupplier<Item> RUNE_KENAZ = ITEMS.registerItem("rune_kenaz", p -> new RuneItem(p, "ᚲ", "Kenaz", "Torch / Vision"));
    public static final RegistrySupplier<Item> RUNE_GEBO = ITEMS.registerItem("rune_gebo", p -> new RuneItem(p, "ᚷ", "Gebo", "Gift / Exchange"));
    public static final RegistrySupplier<Item> RUNE_WUNJO = ITEMS.registerItem("rune_wunjo", p -> new RuneItem(p, "ᚹ", "Wunjo", "Joy / Output"));
    public static final RegistrySupplier<Item> RUNE_HAGALAZ = ITEMS.registerItem("rune_hagalaz", p -> new RuneItem(p, "ᚺ", "Hagalaz", "Hail / Destruction"));
    public static final RegistrySupplier<Item> RUNE_NAUTHIZ = ITEMS.registerItem("rune_nauthiz", p -> new RuneItem(p, "ᚾ", "Nauthiz", "Need / Demand"));
    public static final RegistrySupplier<Item> RUNE_ISA = ITEMS.registerItem("rune_isa", p -> new RuneItem(p, "ᛁ", "Isa", "Ice / Stasis"));
    public static final RegistrySupplier<Item> RUNE_JERA = ITEMS.registerItem("rune_jera", p -> new RuneItem(p, "ᛃ", "Jera", "Harvest / Extraction"));
    public static final RegistrySupplier<Item> RUNE_EIHWAZ = ITEMS.registerItem("rune_eihwaz", p -> new RuneItem(p, "ᛇ", "Eihwaz", "Yew / Connection"));
    public static final RegistrySupplier<Item> RUNE_PERTHRO = ITEMS.registerItem("rune_perthro", p -> new RuneItem(p, "ᛈ", "Perthro", "Secret / Filtering"));
    public static final RegistrySupplier<Item> RUNE_ALGIZ = ITEMS.registerItem("rune_algiz", p -> new RuneItem(p, "ᛉ", "Algiz", "Protection / Warding"));
    public static final RegistrySupplier<Item> RUNE_SOWILO = ITEMS.registerItem("rune_sowilo", p -> new RuneItem(p, "ᛊ", "Sowilo", "Sun / Energy"));
    public static final RegistrySupplier<Item> RUNE_TIWAZ = ITEMS.registerItem("rune_tiwaz", p -> new RuneItem(p, "ᛏ", "Tiwaz", "Justice / Balancing"));
    public static final RegistrySupplier<Item> RUNE_BERKANO = ITEMS.registerItem("rune_berkano", p -> new RuneItem(p, "ᛒ", "Berkano", "Growth / Farming"));
    public static final RegistrySupplier<Item> RUNE_EHWAZ = ITEMS.registerItem("rune_ehwaz", p -> new RuneItem(p, "ᛖ", "Ehwaz", "Horse / Transit"));
    public static final RegistrySupplier<Item> RUNE_MANNAZ = ITEMS.registerItem("rune_mannaz", p -> new RuneItem(p, "ᛗ", "Mannaz", "Humanity / Player"));
    public static final RegistrySupplier<Item> RUNE_LAGUZ = ITEMS.registerItem("rune_laguz", p -> new RuneItem(p, "ᛚ", "Laguz", "Water / Fluid"));
    public static final RegistrySupplier<Item> RUNE_INGWAZ = ITEMS.registerItem("rune_ingwaz", p -> new RuneItem(p, "ᛜ", "Ingwaz", "Seed / Storage"));
    public static final RegistrySupplier<Item> RUNE_DAGAZ = ITEMS.registerItem("rune_dagaz", p -> new RuneItem(p, "ᛞ", "Dagaz", "Dawn / Splitting"));
    public static final RegistrySupplier<Item> RUNE_OTHALA = ITEMS.registerItem("rune_othala", p -> new RuneItem(p, "ᛟ", "Othala", "Heritage / Zoning"));

    // ==========================================
    // DYNAMIC ESSENCE ORBS & ESSENCE AMPOULES
    // ==========================================
    public static final RegistrySupplier<EssenceItem> FRAGMENT_ESSENCE = ITEMS.registerItem("fragment_essence", properties -> new EssenceItem(properties, 0));

    public static final RegistrySupplier<EssenceItem> WEAK_ESSENCE = ITEMS.registerItem("weak_essence", properties -> new EssenceItem(properties, 1));
    public static final RegistrySupplier<EssenceItem> AVERAGE_ESSENCE = ITEMS.registerItem("average_essence", properties -> new EssenceItem(properties, 2));
    public static final RegistrySupplier<EssenceItem> STRONG_ESSENCE = ITEMS.registerItem("strong_essence", properties -> new EssenceItem(properties, 3));

    public static final RegistrySupplier<EssenceAmpouleItem> SMALL_ESSENCE_AMPOULE = ITEMS.registerItem("small_essence_ampoule", properties -> new EssenceAmpouleItem(properties, 1));
    public static final RegistrySupplier<EssenceAmpouleItem> MEDIUM_ESSENCE_AMPOULE = ITEMS.registerItem("medium_essence_ampoule", properties -> new EssenceAmpouleItem(properties, 2));
    public static final RegistrySupplier<EssenceAmpouleItem> LARGE_ESSENCE_AMPOULE = ITEMS.registerItem("large_essence_ampoule", properties -> new EssenceAmpouleItem(properties, 3));

    public static final RegistrySupplier<Item> PROPAGATION_LENS = ITEMS.registerItem("propagation_lens", properties -> new ddraig.net.entropica.item.PropagationLensItem(properties));
    public static final RegistrySupplier<Item> AETHERIC_LENS = ITEMS.registerItem("aetheric_lens", properties -> new ddraig.net.entropica.item.AethericLensItem(properties));
    public static final RegistrySupplier<Item> VITAE_LENS = ITEMS.registerItem("vitae_lens", properties -> new ddraig.net.entropica.item.VitaeLensItem(properties));
    public static final RegistrySupplier<Item> MATERIA_LENS = ITEMS.registerItem("materia_lens", properties -> new ddraig.net.entropica.item.MateriaLensItem(properties));
    public static final RegistrySupplier<Item> ARKANIST_MONOCLE = ITEMS.registerItem("arkanist_monocle", properties -> new ddraig.net.entropica.item.ArkanistMonocleItem(properties));
    public static final RegistrySupplier<Item> RUBBER_TAP = ITEMS.registerItem("rubber_tap", properties -> new ddraig.net.entropica.item.RubberTapItem(properties));

    // --- Block Items ---
    public static final RegistrySupplier<BlockItem> ENTROPIC_ORE_ITEM = ITEMS.registerItem("entropic_ore", properties -> new BlockItem(ModBlocks.ENTROPIC_ORE.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_BLESSING_ITEM = ITEMS.registerItem("materia_blessing", properties -> new BlockItem(ModBlocks.MATERIA_BLESSING.get(), properties));
    public static final RegistrySupplier<BlockItem> GREATER_MATERIA_BLESSING_ITEM = ITEMS.registerItem("greater_materia_blessing", properties -> new BlockItem(ModBlocks.GREATER_MATERIA_BLESSING.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_FURNACE_ITEM = ITEMS.registerItem("materia_furnace", properties -> new BlockItem(ModBlocks.MATERIA_FURNACE.get(), properties));
    public static final RegistrySupplier<BlockItem> ENTROPIC_AUTO_SMELTER_ITEM = ITEMS.registerItem("entropic_auto_smelter", properties -> new BlockItem(ModBlocks.ENTROPIC_AUTO_SMELTER.get(), properties));

    // Multiblock Components
    public static final RegistrySupplier<BlockItem> ENTROPIC_CORE_ITEM = ITEMS.registerItem("entropic_core", properties -> new BlockItem(ModBlocks.ENTROPIC_CORE.get(), properties));
    public static final RegistrySupplier<BlockItem> ARCANE_BRICK_ITEM = ITEMS.registerItem("arcane_brick", properties -> new BlockItem(ModBlocks.ARCANE_BRICK.get(), properties));
    public static final RegistrySupplier<BlockItem> ARCANITE_PLATING_ITEM = ITEMS.registerItem("arcanite_plating", properties -> new BlockItem(ModBlocks.ARCANITE_PLATING.get(), properties));
    public static final RegistrySupplier<BlockItem> ARCANE_CLAY_BLOCK_ITEM = ITEMS.registerItem("arcane_clay_block", properties -> new BlockItem(ModBlocks.ARCANE_CLAY_BLOCK.get(), properties));
    public static final RegistrySupplier<BlockItem> ARCANE_FORGE_BASE_ITEM = ITEMS.registerItem("arcane_forge_base", properties -> new BlockItem(ModBlocks.ARCANE_FORGE_BASE.get(), properties));

    public static final RegistrySupplier<BlockItem> MATERIA_PLUME_ITEM = ITEMS.registerItem("materia_plume", properties -> new BlockItem(ModBlocks.MATERIA_PLUME.get(), properties));
    public static final RegistrySupplier<BlockItem> ESSENCE_RECEPTACLE_ITEM = ITEMS.registerItem("essence_receptacle", properties -> new BlockItem(ModBlocks.ESSENCE_RECEPTACLE.get(), properties));
    public static final RegistrySupplier<BlockItem> FURNACE_HATCH_ITEM = ITEMS.registerItem("furnace_hatch", properties -> new BlockItem(ModBlocks.FURNACE_HATCH.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_READOUT_ITEM = ITEMS.registerItem("materia_readout", properties -> new BlockItem(ModBlocks.VIS_READOUT.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_EXHAUST_ITEM = ITEMS.registerItem("materia_exhaust", properties -> new BlockItem(ModBlocks.MATERIA_EXHAUST.get(), properties));
    public static final RegistrySupplier<BlockItem> ESSENCE_READOUT_ITEM = ITEMS.registerItem("raw_materia_readout", properties -> new BlockItem(ModBlocks.ESSENCE_READOUT.get(), properties));
    public static final RegistrySupplier<BlockItem> CATALYST_RECEPTACLE_ITEM = ITEMS.registerItem("catalyst_receptacle", properties -> new BlockItem(ModBlocks.CATALYST_RECEPTACLE.get(), properties));

    // --- Extraction Multiblock & Machines ---
    public static final RegistrySupplier<BlockItem> MATERIA_SIMPLE_MACHINE_BLOCK_ITEM = ITEMS.registerItem("materia_simple_machine_block", properties -> new BlockItem(ModBlocks.MATERIA_SIMPLE_MACHINE_BLOCK.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_COMPLEX_MACHINE_BLOCK_ITEM = ITEMS.registerItem("materia_complex_machine_block", properties -> new BlockItem(ModBlocks.MATERIA_COMPLEX_MACHINE_BLOCK.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_INPUT_PORT_ITEM = ITEMS.registerItem("vapor_pneumatic_input_port", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_INPUT_PORT.get(), properties));
    public static final RegistrySupplier<BlockItem> HYDRAULIC_INPUT_PORT_ITEM = ITEMS.registerItem("hydraulic_input_port", properties -> new BlockItem(ModBlocks.HYDRAULIC_INPUT_PORT.get(), properties));
    public static final RegistrySupplier<BlockItem> EXTRACTOR_OUTPUT_PORT_ITEM = ITEMS.registerItem("extractor_output_port", properties -> new BlockItem(ModBlocks.EXTRACTOR_OUTPUT_PORT.get(), properties));
    public static final RegistrySupplier<BlockItem> ESSENCE_NODE_IDENTIFIER_BLOCK_ITEM = ITEMS.registerItem("essence_node_identifier_block", properties -> new BlockItem(ModBlocks.ESSENCE_NODE_IDENTIFIER_BLOCK.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_EXTRACTION_APPARATUS_ITEM = ITEMS.registerItem("materia_extraction_apparatus", properties -> new BlockItem(ModBlocks.MATERIA_EXTRACTION_APPARATUS.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_EXTRACTOR_BASE_ITEM = ITEMS.registerItem("materia_extractor_base", properties -> new BlockItem(ModBlocks.MATERIA_EXTRACTOR_BASE.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_MOTOR_ITEM = ITEMS.registerItem("materia_motor", properties -> new BlockItem(ModBlocks.MATERIA_MOTOR.get(), properties));

    // --- Dynamic Weapon Forging Blocks ---
    public static final RegistrySupplier<BlockItem> EIDOLIC_FOCAL_PEDESTAL_ITEM = ITEMS.registerItem("eidolic_focal_pedestal", properties -> new BlockItem(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get(), properties));
    public static final RegistrySupplier<BlockItem> ATTUNEMENT_PEDESTAL_ITEM = ITEMS.registerItem("attunement_pedestal", properties -> new BlockItem(ModBlocks.ATTUNEMENT_PEDESTAL.get(), properties));
    public static final RegistrySupplier<BlockItem> MORPHIC_LOOM_ITEM = ITEMS.registerItem("morphic_loom", properties -> new BlockItem(ModBlocks.MORPHIC_LOOM.get(), properties));

    // ==========================================
    // DYNAMIC EIDOLIC WEAPONS & TOOLS
    // ==========================================

    public static final RegistrySupplier<Item> DYNAMIC_SWORD = ITEMS.register("dynamic_sword",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_AXE = ITEMS.register("dynamic_axe",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_PICKAXE = ITEMS.register("dynamic_pickaxe",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_SHOVEL = ITEMS.register("dynamic_shovel",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_ADZE = ITEMS.register("dynamic_adze",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_PAXEL = ITEMS.register("dynamic_paxel",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_SPEAR = ITEMS.register("dynamic_spear",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_MACE = ITEMS.register("dynamic_mace",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_MORNING_STAR = ITEMS.register("dynamic_morning_star",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_WARHAMMER = ITEMS.register("dynamic_warhammer",
            name -> new DynamicVisWeaponItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_SHORTBOW = ITEMS.register("dynamic_shortbow",
            name -> new DynamicVisBowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_LONGBOW = ITEMS.register("dynamic_longbow",
            name -> new DynamicVisBowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_WAR_BOW = ITEMS.register("dynamic_war_bow",
            name -> new DynamicVisBowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_CROSSBOW = ITEMS.register("dynamic_crossbow",
            name -> new DynamicVisCrossbowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> DYNAMIC_REPEATER = ITEMS.register("dynamic_repeater",
            name -> new DynamicVisCrossbowItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, name))));

    public static final RegistrySupplier<Item> ORBIS_CELL_ITEM = ITEMS.registerItem("orbis_cell", properties -> new OrbisCellItem(ModBlocks.ORBIS_CELL.get(), properties));

    // Base Ampoules (Empty)
    public static final RegistrySupplier<Item> SMALL_AMPOULE_BASE = ITEMS.registerItem("small_ampoule_base", properties -> new Item(properties.stacksTo(64)));
    public static final RegistrySupplier<Item> MEDIUM_AMPOULE_BASE = ITEMS.registerItem("medium_ampoule_base", properties -> new Item(properties.stacksTo(64)));
    public static final RegistrySupplier<Item> LARGE_AMPOULE_BASE = ITEMS.registerItem("large_ampoule_base", properties -> new Item(properties.stacksTo(64)));

    // Materia Fume Ampoules (Filled) - RENAMED FROM MANA_AMPOULE
    public static final RegistrySupplier<VisFumeAmpouleItem> SMALL_MATERIA_FUMUS_AMPOULE = ITEMS.registerItem("small_materia_fumus_ampoule", properties -> new VisFumeAmpouleItem(properties.stacksTo(16), 4, SMALL_AMPOULE_BASE));
    public static final RegistrySupplier<VisFumeAmpouleItem> MEDIUM_MATERIA_FUMUS_AMPOULE = ITEMS.registerItem("medium_materia_fumus_ampoule", properties -> new VisFumeAmpouleItem(properties.stacksTo(16), 16, MEDIUM_AMPOULE_BASE));
    public static final RegistrySupplier<VisFumeAmpouleItem> LARGE_MATERIA_FUMUS_AMPOULE = ITEMS.registerItem("large_materia_fumus_ampoule", properties -> new VisFumeAmpouleItem(properties.stacksTo(16), 64, LARGE_AMPOULE_BASE));

    // Materia Fume Network Items
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_COPPER_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_copper", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_COPPER.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_VALVE_ITEM = ITEMS.registerItem("vapor_pneumatic_valve", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_VALVE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_ONE_WAY_VALVE_ITEM = ITEMS.registerItem("vapor_pneumatic_one_way_valve", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_ONE_WAY_VALVE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_DIVERTER_ITEM = ITEMS.registerItem("vapor_pneumatic_diverter", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_DIVERTER.get(), properties));
    public static final RegistrySupplier<BlockItem> CREATIVE_MATERIA_GENERATOR_ITEM = ITEMS.registerItem("creative_materia_generator", properties -> new BlockItem(ModBlocks.CREATIVE_MATERIA_GENERATOR.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_PUMP_ITEM = ITEMS.registerItem("materia_pump", properties -> new BlockItem(ModBlocks.MATERIA_PUMP.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_IRON_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_iron", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_IRON.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_GOLD_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_gold", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_GOLD.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_ARCANITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_arcanite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_ARCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_DIAMOND_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_diamond", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_DIAMOND.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_VISCANITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_viscanite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_VISCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_RESONITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_resonite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_RESONITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CHARGED_ARCANITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_charged_arcanite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_ARCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CHARGED_VISCANITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_charged_viscanite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_VISCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CHARGED_RESONITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_charged_resonite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_RESONITE.get(), properties));

    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CAPUTITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_caputite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CAPUTITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CHARGED_CAPUTITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_charged_caputite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_CAPUTITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_SANGUINITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_sanguinite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_SANGUINITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CHARGED_SANGUINITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_charged_sanguinite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_SANGUINITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_MERCURITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_mercurite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_MERCURITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CHARGED_MERCURITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_charged_mercurite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_MERCURITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_EUCLIDITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_euclidite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_EUCLIDITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CHARGED_EUCLIDITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_charged_euclidite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_EUCLIDITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_ATHANORITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_athanorite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_ATHANORITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VAPOR_PNEUMATIC_PIPE_CHARGED_ATHANORITE_ITEM = ITEMS.registerItem("vapor_pneumatic_pipe_charged_athanorite", properties -> new BlockItem(ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_ATHANORITE.get(), properties));
    public static final RegistrySupplier<BlockItem> HYDRAULIC_PIPELINE_IRON_ITEM = ITEMS.registerItem("hydraulic_pipeline_iron", properties -> new BlockItem(ModBlocks.HYDRAULIC_PIPELINE_IRON.get(), properties));
    public static final RegistrySupplier<BlockItem> HYDRAULIC_PIPELINE_ARCANITE_ITEM = ITEMS.registerItem("hydraulic_pipeline_arcanite", properties -> new BlockItem(ModBlocks.HYDRAULIC_PIPELINE_ARCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> HYDRAULIC_PIPELINE_VISCANITE_ITEM = ITEMS.registerItem("hydraulic_pipeline_viscanite", properties -> new BlockItem(ModBlocks.HYDRAULIC_PIPELINE_VISCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> HYDRAULIC_PIPELINE_RESONITE_ITEM = ITEMS.registerItem("hydraulic_pipeline_resonite", properties -> new BlockItem(ModBlocks.HYDRAULIC_PIPELINE_RESONITE.get(), properties));
    public static final RegistrySupplier<BlockItem> HYDRAULIC_PIPELINE_CHARGED_ARCANITE_ITEM = ITEMS.registerItem("hydraulic_pipeline_charged_arcanite", properties -> new BlockItem(ModBlocks.HYDRAULIC_PIPELINE_CHARGED_ARCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> HYDRAULIC_PIPELINE_CHARGED_VISCANITE_ITEM = ITEMS.registerItem("hydraulic_pipeline_charged_viscanite", properties -> new BlockItem(ModBlocks.HYDRAULIC_PIPELINE_CHARGED_VISCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> HYDRAULIC_PIPELINE_CHARGED_RESONITE_ITEM = ITEMS.registerItem("hydraulic_pipeline_charged_resonite", properties -> new BlockItem(ModBlocks.HYDRAULIC_PIPELINE_CHARGED_RESONITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VOLTAIC_CONDUIT_ARCANITE_ITEM = ITEMS.registerItem("voltaic_conduit_arcanite", properties -> new BlockItem(ModBlocks.VOLTAIC_CONDUIT_ARCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VOLTAIC_CONDUIT_VISCANITE_ITEM = ITEMS.registerItem("voltaic_conduit_viscanite", properties -> new BlockItem(ModBlocks.VOLTAIC_CONDUIT_VISCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VOLTAIC_CONDUIT_RESONITE_ITEM = ITEMS.registerItem("voltaic_conduit_resonite", properties -> new BlockItem(ModBlocks.VOLTAIC_CONDUIT_RESONITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VOLTAIC_CONDUIT_CHARGED_ARCANITE_ITEM = ITEMS.registerItem("voltaic_conduit_charged_arcanite", properties -> new BlockItem(ModBlocks.VOLTAIC_CONDUIT_CHARGED_ARCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VOLTAIC_CONDUIT_CHARGED_VISCANITE_ITEM = ITEMS.registerItem("voltaic_conduit_charged_viscanite", properties -> new BlockItem(ModBlocks.VOLTAIC_CONDUIT_CHARGED_VISCANITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VOLTAIC_CONDUIT_CHARGED_RESONITE_ITEM = ITEMS.registerItem("voltaic_conduit_charged_resonite", properties -> new BlockItem(ModBlocks.VOLTAIC_CONDUIT_CHARGED_RESONITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VISCOUS_AGITATOR_CAPUTITE_ITEM = ITEMS.registerItem("viscous_agitator_caputite", properties -> new BlockItem(ModBlocks.VISCOUS_AGITATOR_CAPUTITE.get(), properties));
    public static final RegistrySupplier<BlockItem> VISCOUS_AGITATOR_CHARGED_CAPUTITE_ITEM = ITEMS.registerItem("viscous_agitator_charged_caputite", properties -> new BlockItem(ModBlocks.VISCOUS_AGITATOR_CHARGED_CAPUTITE.get(), properties));
    public static final RegistrySupplier<BlockItem> SANGUINE_CONDUIT_SANGUINITE_ITEM = ITEMS.registerItem("sanguine_conduit_sanguinite", properties -> new BlockItem(ModBlocks.SANGUINE_CONDUIT_SANGUINITE.get(), properties));
    public static final RegistrySupplier<BlockItem> SANGUINE_CONDUIT_CHARGED_SANGUINITE_ITEM = ITEMS.registerItem("sanguine_conduit_charged_sanguinite", properties -> new BlockItem(ModBlocks.SANGUINE_CONDUIT_CHARGED_SANGUINITE.get(), properties));
    public static final RegistrySupplier<BlockItem> EQUILIBRIUM_CONDUIT_MERCURITE_ITEM = ITEMS.registerItem("equilibrium_conduit_mercurite", properties -> new BlockItem(ModBlocks.EQUILIBRIUM_CONDUIT_MERCURITE.get(), properties));
    public static final RegistrySupplier<BlockItem> EQUILIBRIUM_CONDUIT_CHARGED_MERCURITE_ITEM = ITEMS.registerItem("equilibrium_conduit_charged_mercurite", properties -> new BlockItem(ModBlocks.EQUILIBRIUM_CONDUIT_CHARGED_MERCURITE.get(), properties));
    public static final RegistrySupplier<BlockItem> PRISTINE_CONDUIT_EUCLIDITE_ITEM = ITEMS.registerItem("pristine_conduit_euclidite", properties -> new BlockItem(ModBlocks.PRISTINE_CONDUIT_EUCLIDITE.get(), properties));
    public static final RegistrySupplier<BlockItem> PRISTINE_CONDUIT_CHARGED_EUCLIDITE_ITEM = ITEMS.registerItem("pristine_conduit_charged_euclidite", properties -> new BlockItem(ModBlocks.PRISTINE_CONDUIT_CHARGED_EUCLIDITE.get(), properties));
    public static final RegistrySupplier<BlockItem> ATHANOR_CONDUIT_ATHANORITE_ITEM = ITEMS.registerItem("athanor_conduit_athanorite", properties -> new BlockItem(ModBlocks.ATHANOR_CONDUIT_ATHANORITE.get(), properties));
    public static final RegistrySupplier<BlockItem> ATHANOR_CONDUIT_CHARGED_ATHANORITE_ITEM = ITEMS.registerItem("athanor_conduit_charged_athanorite", properties -> new BlockItem(ModBlocks.ATHANOR_CONDUIT_CHARGED_ATHANORITE.get(), properties));
    public static final RegistrySupplier<OrbisCellItem> SUBLIMATED_ORBIS_CELL_ITEM = ITEMS.registerItem("sublimated_orbis_cell", properties -> new OrbisCellItem(ModBlocks.SUBLIMATED_ORBIS_CELL.get(), properties));
    public static final RegistrySupplier<OrbisCellItem> PNEUMATIC_CALIX_ITEM = ITEMS.registerItem("pneumatic_calix", properties -> new OrbisCellItem(ModBlocks.PNEUMATIC_CALIX.get(), properties));
    public static final RegistrySupplier<OrbisCellItem> VOLTAIC_CALIX_ITEM = ITEMS.registerItem("voltaic_calix", properties -> new OrbisCellItem(ModBlocks.VOLTAIC_CALIX.get(), properties));
    public static final RegistrySupplier<OrbisCellItem> MATRIX_CALIX_ITEM = ITEMS.registerItem("matrix_calix", properties -> new OrbisCellItem(ModBlocks.MATRIX_CALIX.get(), properties));
    public static final RegistrySupplier<OrbisCellItem> THECA_CELL_ITEM = ITEMS.registerItem("theca_cell", properties -> new OrbisCellItem(ModBlocks.THECA_CELL.get(), properties));
    public static final RegistrySupplier<OrbisCellItem> VAS_CELL_ITEM = ITEMS.registerItem("vas_cell", properties -> new OrbisCellItem(ModBlocks.VAS_CELL.get(), properties));
    public static final RegistrySupplier<OrbisCellItem> MONAD_CORE_ITEM = ITEMS.registerItem("monad_core", properties -> new OrbisCellItem(ModBlocks.MONAD_CORE.get(), properties));
    public static final RegistrySupplier<OrbisCellItem> ATHANOR_CORE_ITEM = ITEMS.registerItem("athanor_core", properties -> new OrbisCellItem(ModBlocks.ATHANOR_CORE.get(), properties));
    public static final RegistrySupplier<BlockItem> DECOMPRESSION_COUPLING_ITEM = ITEMS.registerItem("decompression_coupling", properties -> new BlockItem(ModBlocks.DECOMPRESSION_COUPLING.get(), properties));

    public static final RegistrySupplier<BlockItem> CREATIVE_PARTICLE_GENERATOR_ITEM = ITEMS.registerItem("creative_particle_generator", properties -> new BlockItem(ModBlocks.CREATIVE_PARTICLE_GENERATOR.get(), properties));

    // ---  Materia Fume Pressure Vessel Items ---
    public static final RegistrySupplier<BlockItem> MATERIA_VESSEL_CONTROLLER_ITEM = ITEMS.registerItem("materia_vessel_controller", properties -> new BlockItem(ModBlocks.MATERIA_VESSEL_CONTROLLER.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_VESSEL_PORT_ITEM = ITEMS.registerItem("materia_vessel_port", properties -> new BlockItem(ModBlocks.MATERIA_VESSEL_PORT.get(), properties));
    public static final RegistrySupplier<BlockItem> ESSENCE_ENRICHED_GLASS_ITEM = ITEMS.registerItem("essence_enriched_glass", properties -> new BlockItem(AestheticGlassRegistry.ESSENCE_ENRICHED_GLASS.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_FUMUS_STRENGTHENED_GLASS_ITEM = ITEMS.registerItem("materia_fumus_strengthened_glass", properties -> new BlockItem(AestheticGlassRegistry.MATERIA_FUMUS_STRENGTHENED_GLASS.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_LIQUIDA_ENRICHED_GLASS_ITEM = ITEMS.registerItem("materia_liquida_enriched_glass", properties -> new BlockItem(AestheticGlassRegistry.MATERIA_LIQUIDA_ENRICHED_GLASS.get(), properties));
    public static final RegistrySupplier<BlockItem> FRAGMENT_LATTICE_GLASS_ITEM = ITEMS.registerItem("fragment_lattice_glass", properties -> new BlockItem(AestheticGlassRegistry.FRAGMENT_LATTICE_GLASS.get(), properties));

    // --- Materia Fume Pressure Chamber Items ---
    public static final RegistrySupplier<BlockItem> MATERIA_PRESSURE_CHAMBER_CONTROLLER_ITEM = ITEMS.registerItem("materia_pressure_chamber_controller", properties -> new BlockItem(ModBlocks.MATERIA_PRESSURE_CHAMBER_CONTROLLER.get(), properties));
    public static final RegistrySupplier<BlockItem> ENRICHMENT_TABLE_ITEM = ITEMS.registerItem("enrichment_table", properties -> new BlockItem(ModBlocks.ENRICHMENT_TABLE.get(), properties));

    // --- Void Rifts Item ---
    public static final RegistrySupplier<BlockItem> VOID_RIFT_ITEM = ITEMS.registerItem("void_rift", properties -> new BlockItem(ModBlocks.VOID_RIFT.get(), properties));

    // --- Flora & Wood BlockItems ---
    public static final RegistrySupplier<BlockItem> RUBBER_LOG_ITEM = ITEMS.registerItem("rubber_log", properties -> new BlockItem(ModBlocks.RUBBER_LOG.get(), properties));
    public static final RegistrySupplier<BlockItem> RUBBER_WOOD_ITEM = ITEMS.registerItem("rubber_wood", properties -> new BlockItem(ModBlocks.RUBBER_WOOD.get(), properties));
    public static final RegistrySupplier<BlockItem> STRIPPED_RUBBER_LOG_ITEM = ITEMS.registerItem("stripped_rubber_log", properties -> new BlockItem(ModBlocks.STRIPPED_RUBBER_LOG.get(), properties));
    public static final RegistrySupplier<BlockItem> STRIPPED_RUBBER_WOOD_ITEM = ITEMS.registerItem("stripped_rubber_wood", properties -> new BlockItem(ModBlocks.STRIPPED_RUBBER_WOOD.get(), properties));
    public static final RegistrySupplier<BlockItem> RUBBER_PLANKS_ITEM = ITEMS.registerItem("rubber_planks", properties -> new BlockItem(ModBlocks.RUBBER_PLANKS.get(), properties));
    public static final RegistrySupplier<BlockItem> RUBBER_LEAVES_ITEM = ITEMS.registerItem("rubber_leaves", properties -> new BlockItem(ModBlocks.RUBBER_LEAVES.get(), properties));
    public static final RegistrySupplier<BlockItem> RUBBER_SAPLING_ITEM = ITEMS.registerItem("rubber_sapling", properties -> new BlockItem(ModBlocks.RUBBER_SAPLING.get(), properties));
    public static final RegistrySupplier<BlockItem> SHIMMERPETAL_ITEM = ITEMS.registerItem("shimmerpetal", properties -> new BlockItem(ModBlocks.SHIMMERPETAL.get(), properties));

    // --- Silver Pine Wood Set & Rimebloom Items ---
    public static final RegistrySupplier<BlockItem> SILVER_PINE_LOG_ITEM = ITEMS.registerItem("silver_pine_log", properties -> new BlockItem(ModBlocks.SILVER_PINE_LOG.get(), properties));
    public static final RegistrySupplier<BlockItem> SILVER_PINE_WOOD_ITEM = ITEMS.registerItem("silver_pine_wood", properties -> new BlockItem(ModBlocks.SILVER_PINE_WOOD.get(), properties));
    public static final RegistrySupplier<BlockItem> STRIPPED_SILVER_PINE_LOG_ITEM = ITEMS.registerItem("stripped_silver_pine_log", properties -> new BlockItem(ModBlocks.STRIPPED_SILVER_PINE_LOG.get(), properties));
    public static final RegistrySupplier<BlockItem> STRIPPED_SILVER_PINE_WOOD_ITEM = ITEMS.registerItem("stripped_silver_pine_wood", properties -> new BlockItem(ModBlocks.STRIPPED_SILVER_PINE_WOOD.get(), properties));
    public static final RegistrySupplier<BlockItem> SILVER_PINE_PLANKS_ITEM = ITEMS.registerItem("silver_pine_planks", properties -> new BlockItem(ModBlocks.SILVER_PINE_PLANKS.get(), properties));
    public static final RegistrySupplier<BlockItem> SILVER_PINE_LEAVES_ITEM = ITEMS.registerItem("silver_pine_leaves", properties -> new BlockItem(ModBlocks.SILVER_PINE_LEAVES.get(), properties));
    public static final RegistrySupplier<BlockItem> SILVER_PINE_SAPLING_ITEM = ITEMS.registerItem("silver_pine_sapling", properties -> new BlockItem(ModBlocks.SILVER_PINE_SAPLING.get(), properties));
    
    // --- Ores & Crystals BlockItems ---
    public static final RegistrySupplier<BlockItem> VORPALITE_ORE_ITEM = ITEMS.registerItem("vorpalite_ore", properties -> new BlockItem(ModBlocks.VORPALITE_ORE.get(), properties));
    public static final RegistrySupplier<BlockItem> SORROWSTONE_ORE_ITEM = ITEMS.registerItem("sorrowstone_ore", properties -> new BlockItem(ModBlocks.SORROWSTONE_ORE.get(), properties));
    public static final RegistrySupplier<BlockItem> UMBRALITE_ORE_ITEM = ITEMS.registerItem("umbralite_ore", properties -> new BlockItem(ModBlocks.UMBRALITE_ORE.get(), properties));
    public static final RegistrySupplier<BlockItem> SMALL_AETERIUM_BUD_ITEM = ITEMS.registerItem("small_aeterium_bud", properties -> new BlockItem(ModBlocks.SMALL_AETERIUM_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> MEDIUM_AETERIUM_BUD_ITEM = ITEMS.registerItem("medium_aeterium_bud", properties -> new BlockItem(ModBlocks.MEDIUM_AETERIUM_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> LARGE_AETERIUM_BUD_ITEM = ITEMS.registerItem("large_aeterium_bud", properties -> new BlockItem(ModBlocks.LARGE_AETERIUM_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> AETERIUM_CLUSTER_ITEM = ITEMS.registerItem("aeterium_cluster", properties -> new BlockItem(ModBlocks.AETERIUM_CLUSTER.get(), properties));
    public static final RegistrySupplier<BlockItem> BUDDING_AETERIUM_ITEM = ITEMS.registerItem("budding_aeterium", properties -> new BlockItem(ModBlocks.BUDDING_AETERIUM.get(), properties));
    public static final RegistrySupplier<BlockItem> AETERIUM_CRYSTAL_BLOCK_ITEM = ITEMS.registerItem("aeterium_crystal_block", properties -> new BlockItem(ModBlocks.AETERIUM_CRYSTAL_BLOCK.get(), properties));
    public static final RegistrySupplier<BlockItem> SMALL_IGNISITE_BUD_ITEM = ITEMS.registerItem("small_ignisite_bud", properties -> new BlockItem(ModBlocks.SMALL_IGNISITE_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> MEDIUM_IGNISITE_BUD_ITEM = ITEMS.registerItem("medium_ignisite_bud", properties -> new BlockItem(ModBlocks.MEDIUM_IGNISITE_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> LARGE_IGNISITE_BUD_ITEM = ITEMS.registerItem("large_ignisite_bud", properties -> new BlockItem(ModBlocks.LARGE_IGNISITE_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> IGNISITE_CLUSTER_ITEM = ITEMS.registerItem("ignisite_cluster", properties -> new BlockItem(ModBlocks.IGNISITE_CLUSTER.get(), properties));
    public static final RegistrySupplier<BlockItem> BUDDING_IGNISITE_ITEM = ITEMS.registerItem("budding_ignisite", properties -> new BlockItem(ModBlocks.BUDDING_IGNISITE.get(), properties));
    public static final RegistrySupplier<BlockItem> IGNISITE_CRYSTAL_BLOCK_ITEM = ITEMS.registerItem("ignisite_crystal_block", properties -> new BlockItem(ModBlocks.IGNISITE_CRYSTAL_BLOCK.get(), properties));
    public static final RegistrySupplier<BlockItem> SMALL_MORTISITE_BUD_ITEM = ITEMS.registerItem("small_mortisite_bud", properties -> new BlockItem(ModBlocks.SMALL_MORTISITE_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> MEDIUM_MORTISITE_BUD_ITEM = ITEMS.registerItem("medium_mortisite_bud", properties -> new BlockItem(ModBlocks.MEDIUM_MORTISITE_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> LARGE_MORTISITE_BUD_ITEM = ITEMS.registerItem("large_mortisite_bud", properties -> new BlockItem(ModBlocks.LARGE_MORTISITE_BUD.get(), properties));
    public static final RegistrySupplier<BlockItem> MORTISITE_CLUSTER_ITEM = ITEMS.registerItem("mortisite_cluster", properties -> new BlockItem(ModBlocks.MORTISITE_CLUSTER.get(), properties));
    public static final RegistrySupplier<BlockItem> BUDDING_MORTISITE_ITEM = ITEMS.registerItem("budding_mortisite", properties -> new BlockItem(ModBlocks.BUDDING_MORTISITE.get(), properties));
    public static final RegistrySupplier<BlockItem> MORTISITE_CRYSTAL_BLOCK_ITEM = ITEMS.registerItem("mortisite_crystal_block", properties -> new BlockItem(ModBlocks.MORTISITE_CRYSTAL_BLOCK.get(), properties));
    public static final RegistrySupplier<BlockItem> ESSENCE_REPULSION_WARD_ITEM = ITEMS.registerItem("essence_repulsion_ward", properties -> new BlockItem(ModBlocks.ESSENCE_REPULSION_WARD.get(), properties));
    public static final RegistrySupplier<BlockItem> RIMEBLOOM_ITEM = ITEMS.registerItem("rimebloom", properties -> new BlockItem(ModBlocks.RIMEBLOOM.get(), properties));
    public static final RegistrySupplier<BlockItem> AEGIS_ROSE_ITEM = ITEMS.registerItem("aegis_rose", properties -> new BlockItem(ModBlocks.AEGIS_ROSE.get(), properties));
    public static final RegistrySupplier<BlockItem> AMBER_NECTAR_BLOSSOM_ITEM = ITEMS.registerItem("amber_nectar_blossom", properties -> new BlockItem(ModBlocks.AMBER_NECTAR_BLOSSOM.get(), properties));
    public static final RegistrySupplier<BlockItem> SOUL_FLAME_ORCHID_ITEM = ITEMS.registerItem("soul_flame_orchid", properties -> new BlockItem(ModBlocks.SOUL_FLAME_ORCHID.get(), properties));
    public static final RegistrySupplier<BlockItem> AURORAL_BUTTERCUP_ITEM = ITEMS.registerItem("auroral_buttercup", properties -> new BlockItem(ModBlocks.AURORAL_BUTTERCUP.get(), properties));
    public static final RegistrySupplier<BlockItem> STARDUST_BELL_ITEM = ITEMS.registerItem("stardust_bell", properties -> new BlockItem(ModBlocks.STARDUST_BELL.get(), properties));
    public static final RegistrySupplier<BlockItem> FULGURITE_SWAMP_BLOOM_ITEM = ITEMS.registerItem("fulgurite_swamp_bloom", properties -> new BlockItem(ModBlocks.FULGURITE_SWAMP_BLOOM.get(), properties));
    public static final RegistrySupplier<BlockItem> GALE_BLOOM_DANDELION_ITEM = ITEMS.registerItem("gale_bloom_dandelion", properties -> new BlockItem(ModBlocks.GALE_BLOOM_DANDELION.get(), properties));
    public static final RegistrySupplier<BlockItem> CRYO_STATIC_SHRUB_ITEM = ITEMS.registerItem("cryo_static_shrub", properties -> new BlockItem(ModBlocks.CRYO_STATIC_SHRUB.get(), properties));

    // --- Flora & Botanical Items ---
    public static final RegistrySupplier<Item> AEGIS_ROSE_PETALS = ITEMS.registerItem("aegis_rose_petals", Item::new);
    public static final RegistrySupplier<Item> AMBER_NECTAR_CUP = ITEMS.registerItem("amber_nectar_cup", Item::new);
    public static final RegistrySupplier<Item> AMBER_NECTAR = ITEMS.registerItem("amber_nectar", Item::new);
    public static final RegistrySupplier<Item> SOUL_FLAME_PETAL = ITEMS.registerItem("soul_flame_petal", Item::new);
    public static final RegistrySupplier<Item> SOULFIRE_NECTAR = ITEMS.registerItem("soulfire_nectar", Item::new);
    public static final RegistrySupplier<Item> AURORAL_PETAL = ITEMS.registerItem("auroral_petal", Item::new);
    public static final RegistrySupplier<Item> AURORAL_POLLEN = ITEMS.registerItem("auroral_pollen", Item::new);
    public static final RegistrySupplier<Item> STARDUST_BELL_PETAL = ITEMS.registerItem("stardust_bell_petal", Item::new);
    public static final RegistrySupplier<Item> STARDUST_NECTAR = ITEMS.registerItem("stardust_nectar", Item::new);
    public static final RegistrySupplier<Item> FULGURITE_PETAL = ITEMS.registerItem("fulgurite_petal", Item::new);
    public static final RegistrySupplier<Item> FULGURITE_STIGMA = ITEMS.registerItem("fulgurite_stigma", Item::new);
    public static final RegistrySupplier<Item> GALE_SPORE_PUFF = ITEMS.registerItem("gale_spore_puff", Item::new);
    public static final RegistrySupplier<Item> GALE_POPPED_SPORE = ITEMS.registerItem("gale_popped_spore", Item::new);
    public static final RegistrySupplier<Item> CRYO_STATIC_TWIG = ITEMS.registerItem("cryo_static_twig", Item::new);
    public static final RegistrySupplier<Item> CRYO_STATIC_LEAF = ITEMS.registerItem("cryo_static_leaf", Item::new);
    public static final RegistrySupplier<Item> CRYO_STATIC_ROOTLING = ITEMS.registerItem("cryo_static_rootling", Item::new);

    public static final RegistrySupplier<BlockItem> VITREOUS_CACTUS_ITEM = ITEMS.registerItem("vitreous_cactus", properties -> new BlockItem(ModBlocks.VITREOUS_CACTUS.get(), properties));
    public static final RegistrySupplier<Item> VITREOUS_NEEDLE = ITEMS.registerItem("vitreous_needle", Item::new);
    public static final RegistrySupplier<Item> VITREOUS_CACTUS_FLESH = ITEMS.registerItem("vitreous_cactus_flesh", Item::new);

    public static final RegistrySupplier<BlockItem> BARROW_MOSS_ITEM = ITEMS.registerItem("barrow_moss", properties -> new BlockItem(ModBlocks.BARROW_MOSS.get(), properties));
    public static final RegistrySupplier<BlockItem> BARROW_MOSS_CARPET_ITEM = ITEMS.registerItem("barrow_moss_carpet", properties -> new BlockItem(ModBlocks.BARROW_MOSS_CARPET.get(), properties));
    public static final RegistrySupplier<Item> BARROW_MOSS_FIBER = ITEMS.registerItem("barrow_moss_fiber", Item::new);

    public static final RegistrySupplier<BlockItem> ABYSSAL_WEEPROOT_ITEM = ITEMS.registerItem("abyssal_weeproot", properties -> new BlockItem(ModBlocks.ABYSSAL_WEEPROOT.get(), properties));
    public static final RegistrySupplier<Item> ABYSSAL_TENDRIL = ITEMS.registerItem("abyssal_tendril", Item::new);

    public static final RegistrySupplier<BlockItem> BLOOD_ROOT_SUCCULENT_ITEM = ITEMS.registerItem("blood_root_succulent", properties -> new BlockItem(ModBlocks.BLOOD_ROOT_SUCCULENT.get(), properties));
    public static final RegistrySupplier<Item> BLOOD_ROOT_PULP = ITEMS.registerItem("blood_root_pulp", Item::new);

    public static final RegistrySupplier<BlockItem> VITAE_ORCHID_ITEM = ITEMS.registerItem("vitae_orchid", properties -> new BlockItem(ModBlocks.VITAE_ORCHID.get(), properties));
    public static final RegistrySupplier<Item> VITAE_PETAL = ITEMS.registerItem("vitae_petal", Item::new);
    public static final RegistrySupplier<Item> VITAE_NECTAR = ITEMS.registerItem("vitae_nectar", Item::new);



    public static final RegistrySupplier<BlockItem> SPORE_BURST_PUFFBALL_ITEM = ITEMS.registerItem("spore_burst_puffball", properties -> new BlockItem(ModBlocks.SPORE_BURST_PUFFBALL.get(), properties));
    public static final RegistrySupplier<Item> SPORE_PUFF = ITEMS.registerItem("spore_puff", Item::new);

    public static final RegistrySupplier<BlockItem> FULGURITE_REED_ITEM = ITEMS.registerItem("fulgurite_reed", properties -> new BlockItem(ModBlocks.FULGURITE_REED.get(), properties));

    public static final RegistrySupplier<BlockItem> GALE_THISTLE_ITEM = ITEMS.registerItem("gale_thistle", properties -> new BlockItem(ModBlocks.GALE_THISTLE.get(), properties));
    public static final RegistrySupplier<Item> GALE_SEED = ITEMS.registerItem("gale_seed", Item::new);

    public static final RegistrySupplier<BlockItem> MIST_VEIL_MARSHMALLOW_ITEM = ITEMS.registerItem("mist_veil_marshmallow", properties -> new BlockItem(ModBlocks.MIST_VEIL_MARSHMALLOW.get(), properties));
    public static final RegistrySupplier<Item> MIST_VEIL_MARSHMALLOW_POD = ITEMS.registerItem("mist_veil_marshmallow_pod", properties -> new Item(properties.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build())));

    public static final RegistrySupplier<BlockItem> AEGIS_SPIRE_ORCHID_ITEM = ITEMS.registerItem("aegis_spire_orchid", properties -> new BlockItem(ModBlocks.AEGIS_SPIRE_ORCHID.get(), properties));
    public static final RegistrySupplier<BlockItem> TALL_AEGIS_SPIRE_ORCHID_ITEM = ITEMS.registerItem("tall_aegis_spire_orchid", properties -> new BlockItem(ModBlocks.TALL_AEGIS_SPIRE_ORCHID.get(), properties));
    public static final RegistrySupplier<Item> AEGIS_SPIRE_PETAL = ITEMS.registerItem("aegis_spire_petal", Item::new);

    public static final RegistrySupplier<BlockItem> PYRE_SPROUT_ITEM = ITEMS.registerItem("pyre_sprout", properties -> new BlockItem(ModBlocks.PYRE_SPROUT.get(), properties));
    public static final RegistrySupplier<Item> PYRE_SPROUT_SEED = ITEMS.registerItem("pyre_sprout_seed", Item::new);
    public static final RegistrySupplier<Item> EMBER_PULP = ITEMS.registerItem("ember_pulp", Item::new);

    public static final RegistrySupplier<BlockItem> AURA_DRIFT_SEDGE_ITEM = ITEMS.registerItem("aura_drift_sedge", properties -> new BlockItem(ModBlocks.AURA_DRIFT_SEDGE.get(), properties));
    public static final RegistrySupplier<Item> AURA_DRIFT_FIBER = ITEMS.registerItem("aura_drift_fiber", Item::new);

    public static final RegistrySupplier<BlockItem> SPECTRAL_LANTERN_FLOWER_ITEM = ITEMS.registerItem("spectral_lantern_flower", properties -> new BlockItem(ModBlocks.SPECTRAL_LANTERN_FLOWER.get(), properties));
    public static final RegistrySupplier<Item> SPECTRAL_LANTERN_POD = ITEMS.registerItem("spectral_lantern_pod", Item::new);

    public static final RegistrySupplier<BlockItem> CINDER_SPORE_MUSHROOM_ITEM = ITEMS.registerItem("cinder_spore_mushroom", properties -> new BlockItem(ModBlocks.CINDER_SPORE_MUSHROOM.get(), properties));
    public static final RegistrySupplier<Item> CINDER_SPORE_CAP = ITEMS.registerItem("cinder_spore_cap", Item::new);













    // --- Dynamic Spectral Dye Category ---
    public static final java.util.Map<String, RegistrySupplier<Item>> SPECTRAL_DYES = registerSpectralDyes();

    private static java.util.Map<String, RegistrySupplier<Item>> registerSpectralDyes() {
        java.util.Map<String, RegistrySupplier<Item>> map = new java.util.LinkedHashMap<>();
        for (var dye : ddraig.net.entropica.api.SpectralDyeApi.getAllDyes().values()) {
            String itemId = "spectral_dye_" + dye.id();
            map.put(dye.id(), ITEMS.registerItem(itemId, properties -> new ddraig.net.entropica.item.SpectralDyeItem(properties, dye.id())));
        }
        return java.util.Collections.unmodifiableMap(map);
    }

    public static final RegistrySupplier<Item> SPECTRAL_DYE_SOULFIRE = SPECTRAL_DYES.get("soulfire");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_AEGIS = SPECTRAL_DYES.get("aegis");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_AMBER = SPECTRAL_DYES.get("amber");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_SHIMMER = SPECTRAL_DYES.get("shimmer");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_FROST = SPECTRAL_DYES.get("frost");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_AURORAL = SPECTRAL_DYES.get("auroral");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_STARDUST = SPECTRAL_DYES.get("stardust");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_FULGURITE = SPECTRAL_DYES.get("fulgurite");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_GALE = SPECTRAL_DYES.get("gale");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_CRYO_STATIC = SPECTRAL_DYES.get("cryo_static");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_VITREOUS = SPECTRAL_DYES.get("vitreous");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_BARROW = SPECTRAL_DYES.get("barrow");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_ABYSSAL = SPECTRAL_DYES.get("abyssal");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_SANGUINE = SPECTRAL_DYES.get("sanguine");
    public static final RegistrySupplier<Item> SPECTRAL_DYE_VITAE = SPECTRAL_DYES.get("vitae");










    // --- Crucible & Ritual Bowls Items ---
    public static final RegistrySupplier<BlockItem> CRUCIBLE_ITEM = ITEMS.registerItem("crucible", properties -> new BlockItem(ModBlocks.CRUCIBLE.get(), properties));
    public static final RegistrySupplier<BlockItem> MARBLE_RITUAL_BOWL_ITEM = ITEMS.registerItem("marble_ritual_bowl", properties -> new BlockItem(ModBlocks.MARBLE_RITUAL_BOWL.get(), properties));
    public static final RegistrySupplier<BlockItem> BASALT_RITUAL_BOWL_ITEM = ITEMS.registerItem("basalt_ritual_bowl", properties -> new BlockItem(ModBlocks.BASALT_RITUAL_BOWL.get(), properties));
    public static final RegistrySupplier<BlockItem> GRANITE_RITUAL_BOWL_ITEM = ITEMS.registerItem("granite_ritual_bowl", properties -> new BlockItem(ModBlocks.GRANITE_RITUAL_BOWL.get(), properties));

    // --- RITUAL CHALK ---
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> CHALK = ITEMS.registerItem("chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(32), 1, false));
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> DULL_CHALK = ITEMS.registerItem("dull_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(64), 1, false));
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> CONDUCTIVE_CHALK = ITEMS.registerItem("conductive_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(128), 2, false));
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> RESONANT_CHALK = ITEMS.registerItem("resonant_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(256), 3, false));
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> EIDOLIC_CHALK = ITEMS.registerItem("eidolic_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(512), 4, false));

    // --- ADVANCED CHALK (CIRCUITS) ---
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> ADVANCED_CHALK = ITEMS.registerItem("advanced_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(32), 1, true));
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> ADVANCED_DULL_CHALK = ITEMS.registerItem("advanced_dull_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(64), 1, true));
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> ADVANCED_CONDUCTIVE_CHALK = ITEMS.registerItem("advanced_conductive_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(128), 2, true));
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> ADVANCED_RESONANT_CHALK = ITEMS.registerItem("advanced_resonant_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(256), 3, true));
    public static final RegistrySupplier<ddraig.net.entropica.item.ChalkItem> ADVANCED_EIDOLIC_CHALK = ITEMS.registerItem("advanced_eidolic_chalk", properties -> new ddraig.net.entropica.item.ChalkItem(properties.durability(512), 4, true));


    public static final RegistrySupplier<ddraig.net.entropica.item.ArcaneStencilItem> ARCANE_STENCIL = ITEMS.registerItem("arcane_stencil", ddraig.net.entropica.item.ArcaneStencilItem::new);

    // --- SCRIBING ENGINE & RESEARCH MACHINE ITEMS ---
    public static final RegistrySupplier<net.minecraft.world.item.BlockItem> VISCANITE_SYNTHESIZER_ITEM = ITEMS.registerItem("viscanite_synthesizer", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.VISCANITE_SYNTHESIZER.get(), properties));
    public static final RegistrySupplier<net.minecraft.world.item.BlockItem> SCRIBING_CONTROLLER_ITEM = ITEMS.registerItem("scribing_controller", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.SCRIBING_CONTROLLER.get(), properties));
    public static final RegistrySupplier<net.minecraft.world.item.BlockItem> VISCANITE_PISTON_PRESS_ITEM = ITEMS.registerItem("viscanite_piston_press", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.VISCANITE_PISTON_PRESS.get(), properties));
    public static final RegistrySupplier<net.minecraft.world.item.BlockItem> DRAFTING_TABLE_ITEM = ITEMS.registerItem("drafting_table", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.DRAFTING_TABLE.get(), properties));
    public static final RegistrySupplier<net.minecraft.world.item.BlockItem> RESEARCH_BENCH_ITEM = ITEMS.registerItem("research_bench", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.RESEARCH_BENCH.get(), properties));

    // --- CRAFTING INGREDIENTS ---
    public static final RegistrySupplier<Item> SPECTRAL_GLYPH_PLATE = ITEMS.registerItem("spectral_glyph_plate", Item::new);
    public static final RegistrySupplier<Item> VISCANITE_PISTON = ITEMS.registerItem("viscanite_piston", Item::new);
    public static final RegistrySupplier<Item> PRESSURE_GRADED_GASKET = ITEMS.registerItem("pressure_graded_gasket", Item::new);
    public static final RegistrySupplier<Item> BLANK_STONE = ITEMS.registerItem("blank_stone", Item::new);

    // --- FAUNA DROPS ---
    public static final RegistrySupplier<Item> GLACIAL_AEGIS_PLATE = ITEMS.registerItem("glacial_aegis_plate", Item::new);
    public static final RegistrySupplier<Item> PATINA_AEGIS_PLATE = ITEMS.registerItem("patina_aegis_plate", Item::new);
    public static final RegistrySupplier<Item> MOSSY_AEGIS_PLATE = ITEMS.registerItem("mossy_aegis_plate", Item::new);
    public static final RegistrySupplier<Item> LIVING_SPORES = ITEMS.registerItem("living_spores", Item::new);
    public static final RegistrySupplier<Item> RIME_ANTLER_FRAGMENT = ITEMS.registerItem("rime_antler_fragment", Item::new);
    public static final RegistrySupplier<Item> SHEPHERDS_FROST = ITEMS.registerItem("shepherds_frost", Item::new);
    public static final RegistrySupplier<Item> CRAWLER_SHELL_FRAGMENT = ITEMS.registerItem("crawler_shell_fragment", Item::new);

    // Others
    public static final RegistrySupplier<BlockItem> VIS_VITAE_ANCHOR_ITEM = ITEMS.registerItem("vis_vitae_anchor", properties -> new BlockItem(ModBlocks.VIS_VITAE_ANCHOR.get(), properties));
    public static final RegistrySupplier<BlockItem> VIS_VITAE_CONDENSER_ITEM = ITEMS.registerItem("vis_vitae_condenser", properties -> new BlockItem(ModBlocks.VIS_VITAE_CONDENSER.get(), properties));
    public static final RegistrySupplier<BlockItem> VIS_VITAE_VACUUM_ITEM = ITEMS.registerItem("vis_vitae_vacuum", properties -> new BlockItem(ModBlocks.VIS_VITAE_VACUUM.get(), properties));
    public static final RegistrySupplier<BlockItem> VITAE_BARREL_ITEM = ITEMS.registerItem("vitae_barrel", properties -> new BlockItem(ModBlocks.VITAE_BARREL.get(), properties));
    public static final RegistrySupplier<BlockItem> MATERIA_FILTER_ITEM = ITEMS.registerItem("materia_filter", properties -> new BlockItem(ModBlocks.MATERIA_FILTER.get(), properties));

    public static final RegistrySupplier<BlockItem> AETHERIC_SYNTHESIZER_ITEM = ITEMS.registerItem("aetheric_synthesizer", properties -> new BlockItem(ModBlocks.AETHERIC_SYNTHESIZER.get(), properties));
    public static final RegistrySupplier<BlockItem> AETHERIC_AUTOMATOR_ITEM = ITEMS.registerItem("aetheric_automator", properties -> new BlockItem(ModBlocks.AETHERIC_AUTOMATOR.get(), properties));
    public static final RegistrySupplier<BlockItem> SYNTHESIZER_USER_INTERFACE_ITEM = ITEMS.registerItem("synthesizer_user_interface", properties -> new BlockItem(ModBlocks.SYNTHESIZER_USER_INTERFACE.get(), properties));

    public static final RegistrySupplier<BlockItem> ARCANE_LOOM_ITEM = ITEMS.registerItem("arcane_loom", properties -> new BlockItem(ModBlocks.ARCANE_LOOM.get(), properties));
    public static final RegistrySupplier<BlockItem> ESSENCE_FORGE_ITEM = ITEMS.registerItem("essence_forge", properties -> new BlockItem(ModBlocks.ESSENCE_FORGE.get(), properties));
    public static final RegistrySupplier<BlockItem> ARCANE_ANVIL_ITEM = ITEMS.registerItem("arcane_anvil", properties -> new BlockItem(ModBlocks.ARCANE_ANVIL.get(), properties));

    // --- FAUNA SPAWN EGGS ---
    public static final RegistrySupplier<Item> GROT_SPAWN_EGG = ITEMS.registerItem("grot_spawn_egg", properties -> new ddraig.net.entropica.item.ModSpawnEggItem(ModEntityTypes.GROT, properties));
    public static final RegistrySupplier<Item> VEIL_FOX_SPAWN_EGG = ITEMS.registerItem("veil_fox_spawn_egg", properties -> new ddraig.net.entropica.item.ModSpawnEggItem(ModEntityTypes.VEIL_FOX, properties));
    public static final RegistrySupplier<Item> ASHEN_STALKER_SPAWN_EGG = ITEMS.registerItem("ashen_stalker_spawn_egg", properties -> new ddraig.net.entropica.item.ModSpawnEggItem(ModEntityTypes.ASHEN_STALKER, properties));
    public static final RegistrySupplier<Item> SPORE_DRIFTER_SPAWN_EGG = ITEMS.registerItem("spore_drifter_spawn_egg", properties -> new ddraig.net.entropica.item.ModSpawnEggItem(ModEntityTypes.SPORE_DRIFTER, properties));
    public static final RegistrySupplier<Item> RIME_BACK_OVIS_SPAWN_EGG = ITEMS.registerItem("rime_back_ovis_spawn_egg", properties -> new ddraig.net.entropica.item.ModSpawnEggItem(ModEntityTypes.RIME_BACK_OVIS, properties));
    public static final RegistrySupplier<Item> OVERGROWTH_OVIS_SPAWN_EGG = ITEMS.registerItem("overgrowth_ovis_spawn_egg", properties -> new ddraig.net.entropica.item.ModSpawnEggItem(ModEntityTypes.OVERGROWTH_OVIS, properties));
    public static final RegistrySupplier<Item> RIME_SHEPHERD_SPAWN_EGG = ITEMS.registerItem("rime_shepherd_spawn_egg", properties -> new ddraig.net.entropica.item.ModSpawnEggItem(ModEntityTypes.RIME_SHEPHERD, properties));
    public static final RegistrySupplier<Item> BLOOM_CRAWLER_SPAWN_EGG = ITEMS.registerItem("bloom_crawler_spawn_egg", properties -> new ddraig.net.entropica.item.ModSpawnEggItem(ModEntityTypes.BLOOM_CRAWLER, properties));

    // ==========================================
    // DATAGEN ITEM LIST
    // Any basic item added to this list will automatically have a flat model generated.
    // ==========================================
    public static final List<RegistrySupplier<? extends Item>> SIMPLE_ITEMS = List.of(
            ARCANUM_FOCUS, MATERIA_VALUE_DETECTOR, ARCANE_BRICK_PIECE, ARCANE_CLAY, EIDOLON_PATHMARKER,
            VEIL_SHARD, CHALK, DULL_CHALK, CONDUCTIVE_CHALK, RESONANT_CHALK, EIDOLIC_CHALK,
            ADVANCED_CHALK, ADVANCED_DULL_CHALK, ADVANCED_CONDUCTIVE_CHALK, ADVANCED_RESONANT_CHALK, ADVANCED_EIDOLIC_CHALK,
            GLACIAL_AEGIS_PLATE, PATINA_AEGIS_PLATE, MOSSY_AEGIS_PLATE, LIVING_SPORES, RIME_ANTLER_FRAGMENT, SHEPHERDS_FROST, CRAWLER_SHELL_FRAGMENT, // Added Fauna Drops
            SMALL_AMPOULE, MEDIUM_AMPOULE, LARGE_AMPOULE, ESSENCE_HARVESTING_BLADE, SOULBOUND_BLADE,
            OBLIVION_BLADE, TIDAL_TRIDENT, VOID_SWORD, BASALT_PICKAXE, WHISPERWOOD_WAND, SHIMMERING_FOCUS,
            ARCANITE_INGOT, VISCANITE_INGOT, RESONITE_INGOT, EIDOLITE_INGOT, CHARGED_ARCANITE_INGOT, CHARGED_VISCANITE_INGOT, CHARGED_RESONITE_INGOT, CHARGED_EIDOLITE_INGOT,
            CAPUTITE_INGOT, CHARGED_CAPUTITE_INGOT, SANGUINITE_INGOT, CHARGED_SANGUINITE_INGOT, MERCURITE_INGOT, CHARGED_MERCURITE_INGOT, EUCLIDITE_INGOT, CHARGED_EUCLIDITE_INGOT, ATHANORITE_INGOT, CHARGED_ATHANORITE_INGOT,
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
            FOCAL_LENS_ASSEMBLY, AETHERIC_LOGIC_GATE, REFRACTION_GRID_MESH, ICHOR_CAPILLARY_TUBE, MATERIA_CAPACITOR_PLATE, HARMONIC_FEEDBACK_LOOP, AETHERIC_PITCH_PIPE, FREQUENCY_SPLITTER_PRISM, TUNING_FORK, RESONITE_TUNING_FORK, VOID_RESONANT_TUNING_FORK, SPECTRAL_GLYPH_PLATE, TETHER_ANCHOR_PIN,
            ARCANITE_ICHOR_VALVE, VISCANITE_HYDRAULIC_VALVE, ARCANITE_FUME_VALVE, VISCANITE_VAPOR_VALVE, AETHERIC_FLOW_REGULATOR, VOID_SEALED_O_RING, FUME_CONDENSER_COIL, ICHOR_EVAPORATOR_COIL,
            MORPHIC_LOOM_ITEM, VOID_RIFT_ITEM,
            RUNE_FEHU, RUNE_URUZ, RUNE_THURISAZ, RUNE_ANSUZ, RUNE_RAIDO, RUNE_KENAZ, RUNE_GEBO, RUNE_WUNJO,
            RUNE_HAGALAZ, RUNE_NAUTHIZ, RUNE_ISA, RUNE_JERA, RUNE_EIHWAZ, RUNE_PERTHRO, RUNE_ALGIZ, RUNE_SOWILO,
            RUNE_TIWAZ, RUNE_BERKANO, RUNE_EHWAZ, RUNE_MANNAZ, RUNE_LAGUZ, RUNE_INGWAZ, RUNE_DAGAZ, RUNE_OTHALA
    );
}