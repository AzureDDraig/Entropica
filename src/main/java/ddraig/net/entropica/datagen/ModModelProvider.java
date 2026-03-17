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
        blockModels.createNonTemplateModelBlock(ModBlocks.MANA_FURNACE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_AUTO_SMELTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_CORE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CATALYST_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ESSENCE_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ORBIS_CELL.get());

        // Custom 3D Crafters
        blockModels.createNonTemplateModelBlock(ModBlocks.AETHERIC_SYNTHESIZER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.AETHERIC_AUTOMATOR.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ATTUNEMENT_PEDESTAL.get());

        // Vis Fume Network Blocks
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_DIVERTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_PIPE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_VALVE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_ONE_WAY_VALVE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CREATIVE_VIS_FUME_GENERATOR.get());

        // Controllers & Ports
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_VESSEL_CONTROLLER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_VESSEL_PORT.get());

        blockModels.createNonTemplateModelBlock(ModBlocks.ENRICHMENT_TABLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ARCANE_FORGE_BASE.get());

        // Custom Glass Blocks
        blockModels.createNonTemplateModelBlock(ModBlocks.ESSENCE_ENRICHED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_STRENGTHENED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_ICHOR_ENRICHED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.FRAGMENT_LATTICE_GLASS.get());

        // Fluids
        blockModels.createNonTemplateModelBlock(ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get());

        // Orientable Blocks (Front/Side textures)
        createFrontSideBlock(blockModels, ModBlocks.FURNACE_HATCH.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/furnace_hatch"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcanite_plating"));

        createFrontSideBlock(blockModels, ModBlocks.VIS_READOUT.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/mana_readout"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcanite_plating"));

        createFrontSideBlock(blockModels, ModBlocks.ESSENCE_READOUT.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/mana_readout"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcanite_plating"));

        createFrontSideBlock(blockModels, ModBlocks.SYNTHESIZER_USER_INTERFACE.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/synthesizer_user_interface_face"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/synthesizer_user_interface_side"));

        createFrontSideBlock(blockModels, ModBlocks.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/vis_fume_pressure_chamber_controller_face"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/vis_fume_pressure_chamber_side"));

        // Pipe Models
        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_PIPE.get(), "vis_fume_pipe");
        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_VALVE.get(), "vis_fume_valve_open");
        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_VALVE.get(), "vis_fume_valve_closed");

        // ==========================================
        // MANUAL BLOCK ITEM REGISTRATION
        // ==========================================
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_PIPE_ITEM.get(), "vis_fume_pipe_core");
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_VALVE_ITEM.get(), "vis_fume_valve_open_core");
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_DIVERTER_ITEM.get(), "vis_fume_diverter_closed");

        // Extraction Multiblock & Machine Block Items
        generate3DBlockItem(itemModels, ModItems.VIS_SIMPLE_MACHINE_BLOCK_ITEM.get(), "vis_simple_machine_block");
        generate3DBlockItem(itemModels, ModItems.VIS_COMPLEX_MACHINE_BLOCK_ITEM.get(), "vis_complex_machine_block");
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_INPUT_PORT_ITEM.get(), "vis_fume_input_port");
        generate3DBlockItem(itemModels, ModItems.VIS_ICHOR_INPUT_PORT_ITEM.get(), "vis_ichor_input_port");
        generate3DBlockItem(itemModels, ModItems.EXTRACTOR_OUTPUT_PORT_ITEM.get(), "extractor_output_port");
        generate3DBlockItem(itemModels, ModItems.ESSENCE_NODE_IDENTIFIER_BLOCK_ITEM.get(), "essence_node_identifier_block");
        generate3DBlockItem(itemModels, ModItems.VIS_EXTRACTION_APPARATUS_ITEM.get(), "vis_extraction_apparatus");
        generate3DBlockItem(itemModels, ModItems.VIS_EXTRACTOR_BASE_ITEM.get(), "vis_extractor_base");
        generate3DBlockItem(itemModels, ModItems.VIS_MOTOR_ITEM.get(), "vis_motor");

        // Custom 3D Crafter Block Items
        generate3DBlockItem(itemModels, ModItems.AETHERIC_SYNTHESIZER_ITEM.get(), "aetheric_synthesizer");
        generate3DBlockItem(itemModels, ModItems.AETHERIC_AUTOMATOR_ITEM.get(), "aetheric_automator");
        generate3DBlockItem(itemModels, ModItems.SYNTHESIZER_USER_INTERFACE_ITEM.get(), "synthesizer_user_interface");
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER_ITEM.get(), "vis_fume_pressure_chamber_controller");

        // ==========================================
        // MANUAL: CUSTOM ITEM MODELS
        // ==========================================

        // This is a BlockItem that looks flat in the inventory
        generateFlatItem(itemModels, ModItems.ORBIS_CELL_ITEM.get());

        // Skip datagen for Monocle as it has a custom handwritten JSON
        itemModels.itemModelOutput.accept(
                ModItems.AETHERIC_MONOCLE.get(),
                new BlockModelWrapper.Unbaked(ResourceLocation.fromNamespaceAndPath("entropica", "item/custom/aetheric_monocle"), Collections.emptyList())
        );

        // Ampoule Bases
        generateFlatItemWithTexture(itemModels, ModItems.SMALL_AMPOULE_BASE.get(), "small_ampoule");
        generateFlatItemWithTexture(itemModels, ModItems.MEDIUM_AMPOULE_BASE.get(), "medium_ampoule");
        generateFlatItemWithTexture(itemModels, ModItems.LARGE_AMPOULE_BASE.get(), "large_ampoule");

        ModelTemplate twoLayerTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.withDefaultNamespace("item/generated")),
                Optional.empty(),
                TextureSlot.LAYER0, TextureSlot.LAYER1
        );

        // Vis Fume Ampoules
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.SMALL_VIS_FUME_AMPOULE.get(), "small_ampoule", "small_ampoule_gas", new ddraig.net.entropica.client.ModClientEvents.AmpouleTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.MEDIUM_VIS_FUME_AMPOULE.get(), "medium_ampoule", "medium_ampoule_gas", new ddraig.net.entropica.client.ModClientEvents.AmpouleTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.LARGE_VIS_FUME_AMPOULE.get(), "large_ampoule", "large_ampoule_gas", new ddraig.net.entropica.client.ModClientEvents.AmpouleTint());

        // Fragment Orbs (Tier 0)
        createSingleLayerTintedItem(itemModels, ModItems.FRAGMENT_ESSENCE.get(), "fragment_essence", new ddraig.net.entropica.client.ModClientEvents.EssenceTint());

        // Standard Essence Orbs (Tier 1-3)
        createSingleLayerTintedItem(itemModels, ModItems.WEAK_ESSENCE.get(), "weak_essence", new ddraig.net.entropica.client.ModClientEvents.EssenceTint());
        createSingleLayerTintedItem(itemModels, ModItems.AVERAGE_ESSENCE.get(), "average_essence", new ddraig.net.entropica.client.ModClientEvents.EssenceTint());
        createSingleLayerTintedItem(itemModels, ModItems.STRONG_ESSENCE.get(), "strong_essence", new ddraig.net.entropica.client.ModClientEvents.EssenceTint());

        // Essence Ampoules
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.SMALL_ESSENCE_AMPOULE.get(), "small_ampoule", "small_ampoule_essence", new ddraig.net.entropica.client.ModClientEvents.EssenceTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.MEDIUM_ESSENCE_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence", new ddraig.net.entropica.client.ModClientEvents.EssenceTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.LARGE_ESSENCE_AMPOULE.get(), "large_ampoule", "large_ampoule_essence", new ddraig.net.entropica.client.ModClientEvents.EssenceTint());
    }

    private void createVisFumePipeModels(BlockModelGenerators blockModels, Block block, String textureName) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName);

        ModelTemplate coreTemplate = new ModelTemplate(Optional.empty(), Optional.empty(), TextureSlot.PARTICLE, TextureSlot.TEXTURE);
        coreTemplate.create(
                ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName + "_core"),
                new TextureMapping().put(TextureSlot.PARTICLE, texture).put(TextureSlot.TEXTURE, texture),
                blockModels.modelOutput
        );

        ModelTemplate armTemplate = new ModelTemplate(Optional.empty(), Optional.empty(), TextureSlot.PARTICLE, TextureSlot.TEXTURE);
        armTemplate.create(
                ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName + "_arm"),
                new TextureMapping().put(TextureSlot.PARTICLE, texture).put(TextureSlot.TEXTURE, texture),
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
}