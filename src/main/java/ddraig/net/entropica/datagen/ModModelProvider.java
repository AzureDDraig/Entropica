package ddraig.net.entropica.datagen;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, "entropica");
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        // ==========================================
        // AUTO-GENERATE SIMPLE BLOCKS
        // Iterates through the master list in ModBlocks
        // ==========================================
        for (var blockHolder : ModBlocks.SIMPLE_BLOCKS) {
            createSimpleBlock(blockModels, (Block) blockHolder.get());
        }

        // ==========================================
        // AUTO-GENERATE FLAT ITEMS
        // Iterates through the master list in ModItems
        // ==========================================
        for (var itemHolder : ModItems.SIMPLE_ITEMS) {
            generateFlatItem(itemModels, itemHolder.get());
        }

        // ==========================================
        // MANUAL: CUSTOM BLOCK MODELS
        // ==========================================

        // Generates a simple baseline BlockState mapping to satisfy the validation
        // You can manually overwrite the resulting JSON files if you need directional facing!
        blockModels.createNonTemplateModelBlock(ModBlocks.VOID_RIFT.get()); // Added to satisfy validation!
        blockModels.createNonTemplateModelBlock(ModBlocks.MATERIA_FURNACE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_AUTO_SMELTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_CORE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CATALYST_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ESSENCE_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VOID_RIFT.get()); // Added to satisfy validation!
        blockModels.createNonTemplateModelBlock(ModBlocks.MATERIA_FURNACE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_AUTO_SMELTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_CORE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CATALYST_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ESSENCE_RECEPTACLE.get());

        // Custom 3D Crafters
        blockModels.createNonTemplateModelBlock(ModBlocks.AETHERIC_SYNTHESIZER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.AETHERIC_AUTOMATOR.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ATTUNEMENT_PEDESTAL.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CRUCIBLE.get());

        // Materia Fume Network Blocks
        blockModels.createNonTemplateModelBlock(ModBlocks.VAPOR_PNEUMATIC_DIVERTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VAPOR_PNEUMATIC_VALVE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VAPOR_PNEUMATIC_ONE_WAY_VALVE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CREATIVE_MATERIA_GENERATOR.get());

        // Controllers & Ports
        blockModels.createNonTemplateModelBlock(ModBlocks.MATERIA_VESSEL_CONTROLLER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.MATERIA_VESSEL_PORT.get());

        blockModels.createNonTemplateModelBlock(ModBlocks.ENRICHMENT_TABLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ARCANE_FORGE_BASE.get());

        // Custom Glass Blocks
        blockModels.createNonTemplateModelBlock(ModBlocks.ESSENCE_ENRICHED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.MATERIA_FUMUS_STRENGTHENED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.MATERIA_LIQUIDA_ENRICHED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.FRAGMENT_LATTICE_GLASS.get());

        // Fluids
        blockModels.createNonTemplateModelBlock(ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get());

        // Orientable Blocks (Front/Side textures)
        createFrontSideBlock(blockModels, ModBlocks.FURNACE_HATCH.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/furnace_hatch"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcanite_plating"));

        createFrontSideBlock(blockModels, ModBlocks.VIS_READOUT.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/materia_readout"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcanite_plating"));

        createFrontSideBlock(blockModels, ModBlocks.ESSENCE_READOUT.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/materia_readout"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcanite_plating"));

        createFrontSideBlock(blockModels, ModBlocks.SYNTHESIZER_USER_INTERFACE.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/synthesizer_user_interface_face"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/synthesizer_user_interface_side"));

        createFrontSideBlock(blockModels, ModBlocks.MATERIA_PRESSURE_CHAMBER_CONTROLLER.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/materia_pressure_chamber_controller_face"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/vis_fume_pressure_chamber_side"));

        // Pipe Models
        createVisFumePipeModels(blockModels, ModBlocks.VAPOR_PNEUMATIC_VALVE.get(), "vapor_pneumatic_valve_open");
        createVisFumePipeModels(blockModels, ModBlocks.VAPOR_PNEUMATIC_VALVE.get(), "vapor_pneumatic_valve_closed");

        // Vapor Decompression Coupling
        ModelTemplates.CUBE.create(ModBlocks.VAPOR_DECOMPRESSION_COUPLING.get(), 
                TextureMapping.cube(ResourceLocation.fromNamespaceAndPath("entropica", "block/arcanite_plating")), blockModels.modelOutput);

        // Vapor Pneumatic Pipes (10 Tiers)
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_COPPER.get(), ModItems.VAPOR_PNEUMATIC_PIPE_COPPER_ITEM.get(), "vapor_pneumatic_pipe_copper", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_IRON.get(), ModItems.VAPOR_PNEUMATIC_PIPE_IRON_ITEM.get(), "vapor_pneumatic_pipe_iron", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_GOLD.get(), ModItems.VAPOR_PNEUMATIC_PIPE_GOLD_ITEM.get(), "vapor_pneumatic_pipe_gold", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_ARCANITE.get(), ModItems.VAPOR_PNEUMATIC_PIPE_ARCANITE_ITEM.get(), "vapor_pneumatic_pipe_arcanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_DIAMOND.get(), ModItems.VAPOR_PNEUMATIC_PIPE_DIAMOND_ITEM.get(), "vapor_pneumatic_pipe_diamond", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_VISCANITE.get(), ModItems.VAPOR_PNEUMATIC_PIPE_VISCANITE_ITEM.get(), "vapor_pneumatic_pipe_viscanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_RESONITE.get(), ModItems.VAPOR_PNEUMATIC_PIPE_RESONITE_ITEM.get(), "vapor_pneumatic_pipe_resonite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_ARCANITE.get(), ModItems.VAPOR_PNEUMATIC_PIPE_CHARGED_ARCANITE_ITEM.get(), "vapor_pneumatic_pipe_charged_arcanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_VISCANITE.get(), ModItems.VAPOR_PNEUMATIC_PIPE_CHARGED_VISCANITE_ITEM.get(), "vapor_pneumatic_pipe_charged_viscanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_RESONITE.get(), ModItems.VAPOR_PNEUMATIC_PIPE_CHARGED_RESONITE_ITEM.get(), "vapor_pneumatic_pipe_charged_resonite", "vapor_pneumatic_pipe");

        // Hydraulic Pipelines (7 Tiers)
        registerPipe(blockModels, itemModels, ModBlocks.HYDRAULIC_PIPELINE_IRON.get(), ModItems.HYDRAULIC_PIPELINE_IRON_ITEM.get(), "hydraulic_pipeline_iron", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.HYDRAULIC_PIPELINE_ARCANITE.get(), ModItems.HYDRAULIC_PIPELINE_ARCANITE_ITEM.get(), "hydraulic_pipeline_arcanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.HYDRAULIC_PIPELINE_VISCANITE.get(), ModItems.HYDRAULIC_PIPELINE_VISCANITE_ITEM.get(), "hydraulic_pipeline_viscanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.HYDRAULIC_PIPELINE_RESONITE.get(), ModItems.HYDRAULIC_PIPELINE_RESONITE_ITEM.get(), "hydraulic_pipeline_resonite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.HYDRAULIC_PIPELINE_CHARGED_ARCANITE.get(), ModItems.HYDRAULIC_PIPELINE_CHARGED_ARCANITE_ITEM.get(), "hydraulic_pipeline_charged_arcanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.HYDRAULIC_PIPELINE_CHARGED_VISCANITE.get(), ModItems.HYDRAULIC_PIPELINE_CHARGED_VISCANITE_ITEM.get(), "hydraulic_pipeline_charged_viscanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.HYDRAULIC_PIPELINE_CHARGED_RESONITE.get(), ModItems.HYDRAULIC_PIPELINE_CHARGED_RESONITE_ITEM.get(), "hydraulic_pipeline_charged_resonite", "vapor_pneumatic_pipe");

        // Voltaic Conduits (6 Tiers)
        registerPipe(blockModels, itemModels, ModBlocks.VOLTAIC_CONDUIT_ARCANITE.get(), ModItems.VOLTAIC_CONDUIT_ARCANITE_ITEM.get(), "voltaic_conduit_arcanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VOLTAIC_CONDUIT_VISCANITE.get(), ModItems.VOLTAIC_CONDUIT_VISCANITE_ITEM.get(), "voltaic_conduit_viscanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VOLTAIC_CONDUIT_RESONITE.get(), ModItems.VOLTAIC_CONDUIT_RESONITE_ITEM.get(), "voltaic_conduit_resonite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VOLTAIC_CONDUIT_CHARGED_ARCANITE.get(), ModItems.VOLTAIC_CONDUIT_CHARGED_ARCANITE_ITEM.get(), "voltaic_conduit_charged_arcanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VOLTAIC_CONDUIT_CHARGED_VISCANITE.get(), ModItems.VOLTAIC_CONDUIT_CHARGED_VISCANITE_ITEM.get(), "voltaic_conduit_charged_viscanite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VOLTAIC_CONDUIT_CHARGED_RESONITE.get(), ModItems.VOLTAIC_CONDUIT_CHARGED_RESONITE_ITEM.get(), "voltaic_conduit_charged_resonite", "vapor_pneumatic_pipe");

        // T6-T10 Conduits (5 Tiers)
        registerPipe(blockModels, itemModels, ModBlocks.VISCOUS_AGITATOR_CAPUTITE.get(), ModItems.VISCOUS_AGITATOR_CAPUTITE_ITEM.get(), "viscous_agitator_caputite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.VISCOUS_AGITATOR_CHARGED_CAPUTITE.get(), ModItems.VISCOUS_AGITATOR_CHARGED_CAPUTITE_ITEM.get(), "viscous_agitator_charged_caputite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.SANGUINE_CONDUIT_SANGUINITE.get(), ModItems.SANGUINE_CONDUIT_SANGUINITE_ITEM.get(), "sanguine_conduit_sanguinite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.SANGUINE_CONDUIT_CHARGED_SANGUINITE.get(), ModItems.SANGUINE_CONDUIT_CHARGED_SANGUINITE_ITEM.get(), "sanguine_conduit_charged_sanguinite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.EQUILIBRIUM_CONDUIT_MERCURITE.get(), ModItems.EQUILIBRIUM_CONDUIT_MERCURITE_ITEM.get(), "equilibrium_conduit_mercurite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.EQUILIBRIUM_CONDUIT_CHARGED_MERCURITE.get(), ModItems.EQUILIBRIUM_CONDUIT_CHARGED_MERCURITE_ITEM.get(), "equilibrium_conduit_charged_mercurite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.PRISTINE_CONDUIT_EUCLIDITE.get(), ModItems.PRISTINE_CONDUIT_EUCLIDITE_ITEM.get(), "pristine_conduit_euclidite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.PRISTINE_CONDUIT_CHARGED_EUCLIDITE.get(), ModItems.PRISTINE_CONDUIT_CHARGED_EUCLIDITE_ITEM.get(), "pristine_conduit_charged_euclidite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.ATHANOR_CONDUIT_ATHANORITE.get(), ModItems.ATHANOR_CONDUIT_ATHANORITE_ITEM.get(), "athanor_conduit_athanorite", "vapor_pneumatic_pipe");
        registerPipe(blockModels, itemModels, ModBlocks.ATHANOR_CONDUIT_CHARGED_ATHANORITE.get(), ModItems.ATHANOR_CONDUIT_CHARGED_ATHANORITE_ITEM.get(), "athanor_conduit_charged_athanorite", "vapor_pneumatic_pipe");

        // Orbis Cell Variants (9 Blocks)
        registerOrbisCell(blockModels, itemModels, ModBlocks.ORBIS_CELL.get(), ModItems.ORBIS_CELL_ITEM.get());
        registerOrbisCell(blockModels, itemModels, ModBlocks.SUBLIMATED_ORBIS_CELL.get(), ModItems.SUBLIMATED_ORBIS_CELL_ITEM.get());
        registerOrbisCell(blockModels, itemModels, ModBlocks.PNEUMATIC_CALIX.get(), ModItems.PNEUMATIC_CALIX_ITEM.get());
        registerOrbisCell(blockModels, itemModels, ModBlocks.VOLTAIC_CALIX.get(), ModItems.VOLTAIC_CALIX_ITEM.get());
        registerOrbisCell(blockModels, itemModels, ModBlocks.MATRIX_CALIX.get(), ModItems.MATRIX_CALIX_ITEM.get());
        registerOrbisCell(blockModels, itemModels, ModBlocks.THECA_CELL.get(), ModItems.THECA_CELL_ITEM.get());
        registerOrbisCell(blockModels, itemModels, ModBlocks.VAS_CELL.get(), ModItems.VAS_CELL_ITEM.get());
        registerOrbisCell(blockModels, itemModels, ModBlocks.MONAD_CORE.get(), ModItems.MONAD_CORE_ITEM.get());
        registerOrbisCell(blockModels, itemModels, ModBlocks.ATHANOR_CORE.get(), ModItems.ATHANOR_CORE_ITEM.get());

        // ==========================================
        // MANUAL BLOCK ITEM REGISTRATION
        // ==========================================
        generate3DBlockItem(itemModels, ModItems.VAPOR_DECOMPRESSION_COUPLING_ITEM.get(), "vapor_decompression_coupling");
        generate3DBlockItem(itemModels, ModItems.VAPOR_PNEUMATIC_VALVE_ITEM.get(), "vapor_pneumatic_valve_open_core");
        generate3DBlockItem(itemModels, ModItems.VAPOR_PNEUMATIC_DIVERTER_ITEM.get(), "vapor_pneumatic_diverter_closed");

        // Extraction Multiblock & Machine Block Items
        generate3DBlockItem(itemModels, ModItems.MATERIA_SIMPLE_MACHINE_BLOCK_ITEM.get(), "materia_simple_machine_block");
        generate3DBlockItem(itemModels, ModItems.MATERIA_COMPLEX_MACHINE_BLOCK_ITEM.get(), "materia_complex_machine_block");
        generate3DBlockItem(itemModels, ModItems.VAPOR_PNEUMATIC_INPUT_PORT_ITEM.get(), "vapor_pneumatic_input_port");
        generate3DBlockItem(itemModels, ModItems.HYDRAULIC_INPUT_PORT_ITEM.get(), "hydraulic_input_port");
        generate3DBlockItem(itemModels, ModItems.EXTRACTOR_OUTPUT_PORT_ITEM.get(), "extractor_output_port");
        generate3DBlockItem(itemModels, ModItems.ESSENCE_NODE_IDENTIFIER_BLOCK_ITEM.get(), "essence_node_identifier_block");
        generate3DBlockItem(itemModels, ModItems.MATERIA_EXTRACTION_APPARATUS_ITEM.get(), "materia_extraction_apparatus");
        generate3DBlockItem(itemModels, ModItems.MATERIA_EXTRACTOR_BASE_ITEM.get(), "materia_extractor_base");
        generate3DBlockItem(itemModels, ModItems.MATERIA_MOTOR_ITEM.get(), "materia_motor");

        // Custom 3D Crafter Block Items
        generate3DBlockItem(itemModels, ModItems.AETHERIC_SYNTHESIZER_ITEM.get(), "aetheric_synthesizer");
        generate3DBlockItem(itemModels, ModItems.AETHERIC_AUTOMATOR_ITEM.get(), "aetheric_automator");
        generate3DBlockItem(itemModels, ModItems.SYNTHESIZER_USER_INTERFACE_ITEM.get(), "synthesizer_user_interface");
        generate3DBlockItem(itemModels, ModItems.MATERIA_PRESSURE_CHAMBER_CONTROLLER_ITEM.get(), "materia_pressure_chamber_controller");
        generate3DBlockItem(itemModels, ModItems.CRUCIBLE_ITEM.get(), "crucible");

        // ==========================================
        // MANUAL: CUSTOM ITEM MODELS
        // ==========================================

        // Skip datagen for Monocle as it has a custom handwritten JSON
        itemModels.itemModelOutput.accept(
                ModItems.AETHERIC_MONOCLE.get(),
                new BlockModelWrapper.Unbaked(ResourceLocation.fromNamespaceAndPath("entropica", "item/custom/aetheric_monocle"), Collections.emptyList())
        );

        // Skip datagen for Dynamic Weapons as they use custom special model renderers and handwritten JSONs!
        List<Item> dynamicWeapons = List.of(
                ModItems.DYNAMIC_SWORD.get(), ModItems.DYNAMIC_AXE.get(), ModItems.DYNAMIC_PICKAXE.get(),
                ModItems.DYNAMIC_SHOVEL.get(), ModItems.DYNAMIC_ADZE.get(), ModItems.DYNAMIC_PAXEL.get(),
                ModItems.DYNAMIC_SPEAR.get(), ModItems.DYNAMIC_MACE.get(), ModItems.DYNAMIC_MORNING_STAR.get(),
                ModItems.DYNAMIC_WARHAMMER.get(), ModItems.DYNAMIC_SHORTBOW.get(), ModItems.DYNAMIC_LONGBOW.get(),
                ModItems.DYNAMIC_WAR_BOW.get(), ModItems.DYNAMIC_CROSSBOW.get(), ModItems.DYNAMIC_REPEATER.get()
        );

        for (Item weapon : dynamicWeapons) {
            ResourceLocation loc = ModelLocationUtils.getModelLocation(weapon);
            itemModels.itemModelOutput.accept(
                    weapon,
                    new BlockModelWrapper.Unbaked(ResourceLocation.fromNamespaceAndPath("entropica", "item/custom/" + loc.getPath().replace("item/", "")), Collections.emptyList())
            );
        }

        // Ampoule Bases
        generateFlatItemWithTexture(itemModels, ModItems.SMALL_AMPOULE_BASE.get(), "small_ampoule");
        generateFlatItemWithTexture(itemModels, ModItems.MEDIUM_AMPOULE_BASE.get(), "medium_ampoule");
        generateFlatItemWithTexture(itemModels, ModItems.LARGE_AMPOULE_BASE.get(), "large_ampoule");

        ModelTemplate twoLayerTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.withDefaultNamespace("item/generated")),
                Optional.empty(),
                TextureSlot.LAYER0, TextureSlot.LAYER1
        );

        // Materia Fume Ampoules
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.SMALL_MATERIA_FUMUS_AMPOULE.get(), "small_ampoule", "small_ampoule_gas", new ddraig.net.entropica.client.ModItemTintSources.AmpouleTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.MEDIUM_MATERIA_FUMUS_AMPOULE.get(), "medium_ampoule", "medium_ampoule_gas", new ddraig.net.entropica.client.ModItemTintSources.AmpouleTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.LARGE_MATERIA_FUMUS_AMPOULE.get(), "large_ampoule", "large_ampoule_gas", new ddraig.net.entropica.client.ModItemTintSources.AmpouleTint());

        // Fragment Orbs (Tier 0)
        createSingleLayerTintedItem(itemModels, ModItems.FRAGMENT_ESSENCE.get(), "fragment_essence", new ddraig.net.entropica.client.ModItemTintSources.EssenceTint());

        // Standard Essence Orbs (Tier 1-3)
        createSingleLayerTintedItem(itemModels, ModItems.WEAK_ESSENCE.get(), "weak_essence", new ddraig.net.entropica.client.ModItemTintSources.EssenceTint());
        createSingleLayerTintedItem(itemModels, ModItems.AVERAGE_ESSENCE.get(), "average_essence", new ddraig.net.entropica.client.ModItemTintSources.EssenceTint());
        createSingleLayerTintedItem(itemModels, ModItems.STRONG_ESSENCE.get(), "strong_essence", new ddraig.net.entropica.client.ModItemTintSources.EssenceTint());

        // Essence Ampoules
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.SMALL_ESSENCE_AMPOULE.get(), "small_ampoule", "small_ampoule_essence", new ddraig.net.entropica.client.ModItemTintSources.EssenceTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.MEDIUM_ESSENCE_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence", new ddraig.net.entropica.client.ModItemTintSources.EssenceTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.LARGE_ESSENCE_AMPOULE.get(), "large_ampoule", "large_ampoule_essence", new ddraig.net.entropica.client.ModItemTintSources.EssenceTint());
    }

    private void createVisFumePipeModels(BlockModelGenerators blockModels, Block block, String textureName) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName);
        TextureSlot pipeSlot = TextureSlot.create("pipe");

        ModelTemplate coreTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.fromNamespaceAndPath("entropica", "block/vapor_pneumatic_pipe_core")),
                Optional.empty(),
                TextureSlot.PARTICLE,
                pipeSlot
        );
        coreTemplate.create(
                ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName + "_core"),
                new TextureMapping().put(TextureSlot.PARTICLE, texture).put(pipeSlot, texture),
                blockModels.modelOutput
        );

        ModelTemplate armTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.fromNamespaceAndPath("entropica", "block/vapor_pneumatic_pipe_arm")),
                Optional.empty(),
                TextureSlot.PARTICLE,
                pipeSlot
        );
        armTemplate.create(
                ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName + "_arm"),
                new TextureMapping().put(TextureSlot.PARTICLE, texture).put(pipeSlot, texture),
                blockModels.modelOutput
        );
    }

    private void generate3DBlockItem(ItemModelGenerators itemModels, Item item, String blockModelName) {
        ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath("entropica", "block/" + blockModelName);
        itemModels.itemModelOutput.accept(
                item,
                new BlockModelWrapper.Unbaked(modelLocation, Collections.emptyList())
        );
    }

    private void createSimpleBlock(BlockModelGenerators blockModels, Block block) {
        blockModels.createTrivialCube(block);
    }

    // Safely builds the model from textures and provides a simple baseline BlockState to bypass validation
    private void createFrontSideBlock(BlockModelGenerators blockModels, Block block,
                                      ResourceLocation frontTexture, ResourceLocation sideTexture) {
        ModelTemplate orientableTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.withDefaultNamespace("block/orientable")),
                Optional.empty(),
                TextureSlot.FRONT, TextureSlot.SIDE, TextureSlot.TOP
        );

        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.FRONT, frontTexture)
                .put(TextureSlot.SIDE, sideTexture)
                .put(TextureSlot.TOP, sideTexture);

        orientableTemplate.create(block, mapping, blockModels.modelOutput);

        // Fulfills the validation requirement cleanly!
        blockModels.createNonTemplateModelBlock(block);
    }

    private void generateFlatItem(ItemModelGenerators itemModels, Item item) {
        itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    private void generateFlatItemWithTexture(ItemModelGenerators itemModels, Item item, String textureName) {
        ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(item);
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.LAYER0, ResourceLocation.fromNamespaceAndPath("entropica", "item/" + textureName));
        ModelTemplates.FLAT_ITEM.create(modelLocation, mapping, itemModels.modelOutput);

        itemModels.itemModelOutput.accept(
                item,
                new BlockModelWrapper.Unbaked(modelLocation, Collections.emptyList())
        );
    }

    private void createSingleLayerTintedItem(ItemModelGenerators itemModels, Item item, String textureName, net.minecraft.client.color.item.ItemTintSource tint) {
        ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(item);
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.LAYER0, ResourceLocation.fromNamespaceAndPath("entropica", "item/" + textureName));
        ModelTemplates.FLAT_ITEM.create(modelLocation, mapping, itemModels.modelOutput);

        itemModels.itemModelOutput.accept(
                item,
                new BlockModelWrapper.Unbaked(modelLocation, java.util.List.of(tint))
        );
    }

    private void createTwoLayerTintedItem(ItemModelGenerators itemModels, ModelTemplate template, Item item, String layer0Texture, String layer1Texture, net.minecraft.client.color.item.ItemTintSource tintLayer1) {
        ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(item);
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.LAYER0, ResourceLocation.fromNamespaceAndPath("entropica", "item/" + layer0Texture))
                .put(TextureSlot.LAYER1, ResourceLocation.fromNamespaceAndPath("entropica", "item/" + layer1Texture));
        template.create(modelLocation, mapping, itemModels.modelOutput);

        itemModels.itemModelOutput.accept(
                item,
                new BlockModelWrapper.Unbaked(modelLocation, java.util.List.of(
                        new net.minecraft.client.color.item.Constant(-1),
                        tintLayer1
                ))
        );
    }

    private void registerPipe(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
                              Block block, Item item, String blockName, String textureName) {
        blockModels.createNonTemplateModelBlock(block);
        createCustomPipeModels(blockModels, blockName, textureName);
        generate3DBlockItem(itemModels, item, blockName + "_core");
    }

    private void createCustomPipeModels(BlockModelGenerators blockModels, String blockName, String textureName) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName);
        TextureSlot pipeSlot = TextureSlot.create("pipe");

        ModelTemplate coreTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.fromNamespaceAndPath("entropica", "block/vapor_pneumatic_pipe_core")),
                Optional.empty(),
                TextureSlot.PARTICLE,
                pipeSlot
        );
        coreTemplate.create(
                ResourceLocation.fromNamespaceAndPath("entropica", "block/" + blockName + "_core"),
                new TextureMapping().put(TextureSlot.PARTICLE, texture).put(pipeSlot, texture),
                blockModels.modelOutput
        );

        ModelTemplate armTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.fromNamespaceAndPath("entropica", "block/vapor_pneumatic_pipe_arm")),
                Optional.empty(),
                TextureSlot.PARTICLE,
                pipeSlot
        );
        armTemplate.create(
                ResourceLocation.fromNamespaceAndPath("entropica", "block/" + blockName + "_arm"),
                new TextureMapping().put(TextureSlot.PARTICLE, texture).put(pipeSlot, texture),
                blockModels.modelOutput
        );
    }

    private void registerOrbisCell(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
                                   Block block, Item item) {
        ResourceLocation side = ResourceLocation.fromNamespaceAndPath("entropica", "block/orbis_cell_side");
        ResourceLocation end = ResourceLocation.fromNamespaceAndPath("entropica", "block/orbis_cell_top");
        TextureMapping mapping = TextureMapping.column(side, end);
        ModelTemplates.CUBE_COLUMN.create(block, mapping, blockModels.modelOutput);

        generateFlatItemWithTexture(itemModels, item, "orbis_cell");
    }
}