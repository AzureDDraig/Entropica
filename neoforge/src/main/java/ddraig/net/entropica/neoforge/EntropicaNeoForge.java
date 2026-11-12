package ddraig.net.entropica.neoforge;

import ddraig.net.entropica.Entropica;

import ddraig.net.entropica.config.neoforge.EntropicaNeoForgeConfig;
import ddraig.net.entropica.registry.neoforge.ModAttachmentsNeoForge;
import ddraig.net.entropica.registry.neoforge.ModCapabilities;
import ddraig.net.entropica.registry.neoforge.ModFluidsNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;

@Mod(Entropica.MODID)
public class EntropicaNeoForge {
    public EntropicaNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // --- Call Common Initialization ---
        Entropica.init();

        // --- Register NeoForge-Specific Registries ---
        ModAttachmentsNeoForge.ATTACHMENT_TYPES.register(modEventBus);
        ModFluidsNeoForge.FLUID_TYPES.register(modEventBus);
        ModFluidsNeoForge.FLUIDS.register(modEventBus);

        // --- Register Capabilities ---
        modEventBus.addListener(ModCapabilities::registerCapabilities);

        // --- Register Common Setup Event for Registry Aliasing ---
        modEventBus.addListener(this::commonSetup);

        // --- Register Config ---
        modContainer.registerConfig(ModConfig.Type.COMMON, EntropicaNeoForgeConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(this::registerAliases);
    }

    private void registerAliases() {
        IRegistryExtension<Block> blockRegistry = (IRegistryExtension<Block>) BuiltInRegistries.BLOCK;
        IRegistryExtension<Item> itemRegistry = (IRegistryExtension<Item>) BuiltInRegistries.ITEM;

        // Block Mappings
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_pipe"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_pipe_copper"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_valve"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_valve"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_one_way_valve"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_one_way_valve"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_diverter"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_diverter"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "creative_vis_fume_generator"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "creative_materia_generator"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_vessel_controller"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_vessel_controller"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_vessel_port"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_vessel_port"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_strengthened_glass"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_fumus_strengthened_glass"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_ichor_enriched_glass"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_liquida_enriched_glass"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_pressure_chamber_controller"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_pressure_chamber_controller"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_input_port"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_input_port"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_ichor_input_port"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "hydraulic_input_port"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_simple_machine_block"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_simple_machine_block"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_complex_machine_block"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_complex_machine_block"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_exhaust"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_exhaust"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_motor"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_motor"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_extraction_apparatus"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_extraction_apparatus"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_extractor_base"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_extractor_base"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mana_filter"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_filter"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mana_readout"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_readout"));
        blockRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_readout"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_readout"));

        // Item Mappings
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_pipe"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_pipe_copper_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_valve"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_valve_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_one_way_valve"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_one_way_valve_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_diverter"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_diverter_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "creative_vis_fume_generator"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "creative_materia_generator_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_vessel_controller"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_vessel_controller_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_vessel_port"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_vessel_port_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_strengthened_glass"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_fumus_strengthened_glass_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_ichor_enriched_glass"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_liquida_enriched_glass_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_pressure_chamber_controller"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_pressure_chamber_controller_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fume_input_port"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_pneumatic_input_port_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_ichor_input_port"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "hydraulic_input_port_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_simple_machine_block"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_simple_machine_block_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_complex_machine_block"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_complex_machine_block_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_exhaust"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_exhaust_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_motor"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_motor_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_extraction_apparatus"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_extraction_apparatus_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_extractor_base"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_extractor_base_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mana_filter"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_filter_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_value_detector"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_value_detector"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_capacitor_plate"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_capacitor_plate"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "small_vis_fume_ampoule"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "small_materia_fumus_ampoule"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "medium_vis_fume_ampoule"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "medium_materia_fumus_ampoule"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "large_vis_fume_ampoule"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "large_materia_fumus_ampoule"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "viscanite_fume_valve"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "viscanite_vapor_valve"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "viscanite_ichor_valve"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "viscanite_hydraulic_valve"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_readout"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "materia_readout_item"));
        itemRegistry.addAlias(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_readout"), ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_readout_item"));
    }
}
