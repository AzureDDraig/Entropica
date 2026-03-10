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
        // BLOCK MODELS
        // ==========================================
        createSimpleBlock(blockModels, ModBlocks.ENTROPIC_ORE.get());
        createSimpleBlock(blockModels, ModBlocks.ARCANE_BRICK.get());
        createSimpleBlock(blockModels, ModBlocks.ARCANE_PLATING.get());
        createSimpleBlock(blockModels, ModBlocks.ARCANE_CLAY_BLOCK.get());
        createSimpleBlock(blockModels, ModBlocks.MANA_PLUME.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_VITAE_ANCHOR.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_VITAE_CONDENSER.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_VITAE_VACUUM.get());
        createSimpleBlock(blockModels, ModBlocks.VITAE_BARREL.get());
        createSimpleBlock(blockModels, ModBlocks.MANA_VACUUM.get());
        createSimpleBlock(blockModels, ModBlocks.MANA_CABLE.get());
        createSimpleBlock(blockModels, ModBlocks.MANA_FILTER.get());
        createSimpleBlock(blockModels, ModBlocks.MANA_POWERED_LIGHTING.get());
        createSimpleBlock(blockModels, ModBlocks.MANA_CRAFTER.get());
        createSimpleBlock(blockModels, ModBlocks.ENTROPIC_ENCHANTER.get());
        createSimpleBlock(blockModels, ModBlocks.SOLAR_ESSENCE_RECHARGE_STATION.get());
        createSimpleBlock(blockModels, ModBlocks.ARCANE_LOOM.get());
        createSimpleBlock(blockModels, ModBlocks.ESSENCE_FORGE.get());
        createSimpleBlock(blockModels, ModBlocks.ARCANE_ANVIL.get());
        createSimpleBlock(blockModels, ModBlocks.MANA_EXHAUST.get());

        // --- NEW: Extraction Multiblock & Machines ---
        createSimpleBlock(blockModels, ModBlocks.VIS_SIMPLE_MACHINE_BLOCK.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_COMPLEX_MACHINE_BLOCK.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_FUME_INPUT_PORT.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_ICHOR_INPUT_PORT.get());
        createSimpleBlock(blockModels, ModBlocks.EXTRACTOR_OUTPUT_PORT.get());
        createSimpleBlock(blockModels, ModBlocks.ESSENCE_NODE_IDENTIFIER_BLOCK.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_EXTRACTION_APPARATUS.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_EXTRACTOR_BASE.get());
        createSimpleBlock(blockModels, ModBlocks.VIS_MOTOR.get());

        // Inform Datagen to skip these blocks as they have manually created JSON files
        blockModels.createNonTemplateModelBlock(ModBlocks.MANA_FURNACE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.SOLAR_POWERED_FURNACE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_AUTO_SMELTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VITAE_INCINERATOR.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_CORE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CATALYST_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ESSENCE_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ORBIS_CELL.get());

        // Vis Fume Network Blocks
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_DIVERTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_PIPE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_VALVE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_ONE_WAY_VALVE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CREATIVE_VIS_FUME_GENERATOR.get());

        // Pressure Vessel & Chamber Blocks
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_VESSEL_CONTROLLER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_VESSEL_PORT.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENRICHMENT_TABLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ARCANE_FORGE_BASE.get());

        // Custom Glass Blocks
        blockModels.createNonTemplateModelBlock(ModBlocks.ESSENCE_ENRICHED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_STRENGTHENED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_ICHOR_ENRICHED_GLASS.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.FRAGMENT_LATTICE_GLASS.get());

        // Fluids
        blockModels.createNonTemplateModelBlock(ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get());

        createFrontSideBlock(blockModels, ModBlocks.FURNACE_HATCH.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/furnace_hatch"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcane_plating"));

        createFrontSideBlock(blockModels, ModBlocks.MANA_READOUT.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/mana_readout"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcane_plating"));

        createFrontSideBlock(blockModels, ModBlocks.ESSENCE_READOUT.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/mana_readout"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcane_plating"));

        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_PIPE.get(), "vis_fume_pipe");
        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_VALVE.get(), "vis_fume_valve_open");
        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_VALVE.get(), "vis_fume_valve_closed");

        generate3DBlockItem(itemModels, ModItems.VIS_FUME_PIPE_ITEM.get(), "vis_fume_pipe_core");
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_VALVE_ITEM.get(), "vis_fume_valve_open_core");
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_DIVERTER_ITEM.get(), "vis_fume_diverter_closed");

        // --- NEW: Extraction Multiblock & Machine Block Items ---
        generate3DBlockItem(itemModels, ModItems.VIS_SIMPLE_MACHINE_BLOCK_ITEM.get(), "vis_simple_machine_block");
        generate3DBlockItem(itemModels, ModItems.VIS_COMPLEX_MACHINE_BLOCK_ITEM.get(), "vis_complex_machine_block");
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_INPUT_PORT_ITEM.get(), "vis_fume_input_port");
        generate3DBlockItem(itemModels, ModItems.VIS_ICHOR_INPUT_PORT_ITEM.get(), "vis_ichor_input_port");
        generate3DBlockItem(itemModels, ModItems.EXTRACTOR_OUTPUT_PORT_ITEM.get(), "extractor_output_port");
        generate3DBlockItem(itemModels, ModItems.ESSENCE_NODE_IDENTIFIER_BLOCK_ITEM.get(), "essence_node_identifier_block");
        generate3DBlockItem(itemModels, ModItems.VIS_EXTRACTION_APPARATUS_ITEM.get(), "vis_extraction_apparatus");
        generate3DBlockItem(itemModels, ModItems.VIS_EXTRACTOR_BASE_ITEM.get(), "vis_extractor_base");
        generate3DBlockItem(itemModels, ModItems.VIS_MOTOR_ITEM.get(), "vis_motor");

        ModelTemplate twoLayerTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.withDefaultNamespace("item/generated")),
                Optional.empty(),
                TextureSlot.LAYER0, TextureSlot.LAYER1
        );

        // ==========================================
        // ITEM MODELS - Standalone items
        // ==========================================
        generateFlatItem(itemModels, ModItems.ARCANUM_FOCUS.get());
        generateFlatItem(itemModels, ModItems.VIS_VALUE_DETECTOR.get());
        generateFlatItem(itemModels, ModItems.ARCANE_BRICK_PIECE.get());
        generateFlatItem(itemModels, ModItems.ARCANE_PLATE.get());
        generateFlatItem(itemModels, ModItems.ARCANE_CLAY.get());
        generateFlatItem(itemModels, ModItems.SMALL_AMPOULE.get());
        generateFlatItem(itemModels, ModItems.MEDIUM_AMPOULE.get());
        generateFlatItem(itemModels, ModItems.LARGE_AMPOULE.get());
        generateFlatItem(itemModels, ModItems.ESSENCE_HARVESTING_BLADE.get());
        generateFlatItem(itemModels, ModItems.SOULBOUND_BLADE.get());
        generateFlatItem(itemModels, ModItems.OBLIVION_BLADE.get());
        generateFlatItem(itemModels, ModItems.VOID_SWORD.get());
        generateFlatItem(itemModels, ModItems.TIDAL_TRIDENT.get());
        generateFlatItem(itemModels, ModItems.BASALT_PICKAXE.get());
        generateFlatItem(itemModels, ModItems.WHISPERWOOD_WAND.get());
        generateFlatItem(itemModels, ModItems.SHIMMERING_FOCUS.get());

        // Skip datagen for Monocle as it has a custom handwritten JSON
        itemModels.itemModelOutput.accept(
                ModItems.AETHERIC_MONOCLE.get(),
                new BlockModelWrapper.Unbaked(ResourceLocation.fromNamespaceAndPath("entropica", "item/custom/aetheric_monocle"), Collections.emptyList())
        );

        generateFlatItemWithTexture(itemModels, ModItems.SMALL_AMPOULE_BASE.get(), "small_ampoule");
        generateFlatItemWithTexture(itemModels, ModItems.MEDIUM_AMPOULE_BASE.get(), "medium_ampoule");
        generateFlatItemWithTexture(itemModels, ModItems.LARGE_AMPOULE_BASE.get(), "large_ampoule");

        // Vis Fume Ampoules
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.SMALL_VIS_FUME_AMPOULE.get(), "small_ampoule", "small_ampoule_gas", new ddraig.net.entropica.client.ModClientEvents.AmpouleTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.MEDIUM_VIS_FUME_AMPOULE.get(), "medium_ampoule", "medium_ampoule_gas", new ddraig.net.entropica.client.ModClientEvents.AmpouleTint());
        createTwoLayerTintedItem(itemModels, twoLayerTemplate, ModItems.LARGE_VIS_FUME_AMPOULE.get(), "large_ampoule", "large_ampoule_gas", new ddraig.net.entropica.client.ModClientEvents.AmpouleTint());

        generateFlatItem(itemModels, ModItems.ORBIS_CELL_ITEM.get());

        // ==========================================
        // DYNAMIC ESSENCES & AMPOULES
        // ==========================================

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