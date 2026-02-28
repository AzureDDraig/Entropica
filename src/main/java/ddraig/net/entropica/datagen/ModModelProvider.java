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
        // BLOCK MODELS - Simple full-cube blocks
        // ==========================================
        createSimpleBlock(blockModels, ModBlocks.ENTROPIC_ORE.get());
        createSimpleBlock(blockModels, ModBlocks.ARCANE_BRICK.get());
        createSimpleBlock(blockModels, ModBlocks.ARCANE_PLATING.get());
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

        // Blocks with custom models already in resources
        blockModels.createNonTemplateModelBlock(ModBlocks.MANA_FURNACE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.SOLAR_POWERED_FURNACE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_AUTO_SMELTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VITAE_INCINERATOR.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ENTROPIC_CORE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.CATALYST_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ESSENCE_RECEPTACLE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.ORBIS_CELL.get());

        // Tell Datagen to look for our handwritten blockstates exactly ONCE!
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_DIVERTER.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_PIPE.get());
        blockModels.createNonTemplateModelBlock(ModBlocks.VIS_FUME_VALVE.get());

        // Directional blocks with front/side textures
        createFrontSideBlock(blockModels, ModBlocks.FURNACE_HATCH.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/furnace_hatch"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcane_plating"));

        createFrontSideBlock(blockModels, ModBlocks.MANA_READOUT.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/mana_readout"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcane_plating"));

        createFrontSideBlock(blockModels, ModBlocks.ESSENCE_READOUT.get(),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/mana_readout"),
                ResourceLocation.fromNamespaceAndPath("entropica", "block/arcane_plating"));

        // ==========================================
        // ---> GENERATE 3D MODELS FOR GAS NETWORK <---
        // ==========================================

        // Generates the base Pipe
        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_PIPE.get(), "vis_fume_pipe");
        // Generates the Open Valve (Updated to match your exact texture name!)
        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_VALVE.get(), "vis_fume_valve_open");
        // Generates the Closed Valve
        createVisFumePipeModels(blockModels, ModBlocks.VIS_FUME_VALVE.get(), "vis_fume_valve_closed");

        // 3D Items for the Gas Network
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_PIPE_ITEM.get(), "vis_fume_pipe_core");
        // Updated to use the open state for the inventory item!
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_VALVE_ITEM.get(), "vis_fume_valve_open_core");
        generate3DBlockItem(itemModels, ModItems.VIS_FUME_DIVERTER_ITEM.get(), "vis_fume_diverter_closed");

        // ==========================================
        // SHARED TEMPLATES
        // ==========================================
        ModelTemplate twoLayerTemplate = new ModelTemplate(
                Optional.of(ResourceLocation.withDefaultNamespace("item/generated")),
                Optional.empty(),
                TextureSlot.LAYER0, TextureSlot.LAYER1
        );

        // ==========================================
        // ITEM MODELS - Standalone items
        // ==========================================
        generateFlatItem(itemModels, ModItems.ARCANUM_FOCUS.get());
        generateFlatItem(itemModels, ModItems.SMALL_AMPOULE.get());
        generateFlatItem(itemModels, ModItems.MEDIUM_AMPOULE.get());
        generateFlatItem(itemModels, ModItems.LARGE_AMPOULE.get());
        generateFlatItem(itemModels, ModItems.SOULBOUND_BLADE.get());
        generateFlatItem(itemModels, ModItems.OBLIVION_BLADE.get());
        generateFlatItem(itemModels, ModItems.TIDAL_TRIDENT.get());
        generateFlatItem(itemModels, ModItems.BASALT_PICKAXE.get());
        generateFlatItem(itemModels, ModItems.WHISPERWOOD_WAND.get());
        generateFlatItem(itemModels, ModItems.SHIMMERING_FOCUS.get());

        // Empty Ampoule Bases
        generateFlatItemWithTexture(itemModels, ModItems.SMALL_AMPOULE_BASE.get(), "small_ampoule");
        generateFlatItemWithTexture(itemModels, ModItems.MEDIUM_AMPOULE_BASE.get(), "medium_ampoule");
        generateFlatItemWithTexture(itemModels, ModItems.LARGE_AMPOULE_BASE.get(), "large_ampoule");

        // Mana Ampoules (2-layer: glass + gas)
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_MANA_AMPOULE.get(), "small_ampoule", "small_ampoule_gas");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_MANA_AMPOULE.get(), "medium_ampoule", "medium_ampoule_gas");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_MANA_AMPOULE.get(), "large_ampoule", "large_ampoule_gas");

        // Orbis Cell
        generateFlatItem(itemModels, ModItems.ORBIS_CELL_ITEM.get());

        // ==========================================
        // ESSENCES
        // ==========================================
        createEssenceItem(itemModels, ModItems.WEAK_UNDEAD_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_WATER_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_NETHER_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_CHIMERA_ESSENCE.get(), "weak", true);
        createEssenceItem(itemModels, ModItems.WEAK_ARID_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_FROZEN_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_VOID_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_AIR_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_EARTH_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_NATURE_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_LIGHTNING_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_RADIANT_ESSENCE.get(), "weak", false);
        createEssenceItem(itemModels, ModItems.WEAK_UMBRAL_ESSENCE.get(), "weak", false);

        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_UNDEAD_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_WATER_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_NETHER_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_CHIMERA_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_ARID_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_FROZEN_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_VOID_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_AIR_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_EARTH_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_NATURE_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_LIGHTNING_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_RADIANT_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.SMALL_UMBRAL_AMPOULE.get(), "small_ampoule", "small_ampoule_essence");

        createEssenceItem(itemModels, ModItems.AVERAGE_UNDEAD_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_WATER_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_NETHER_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_CHIMERA_ESSENCE.get(), "average", true);
        createEssenceItem(itemModels, ModItems.AVERAGE_ARID_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_FROZEN_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_VOID_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_AIR_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_EARTH_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_NATURE_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_LIGHTNING_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_RADIANT_ESSENCE.get(), "average", false);
        createEssenceItem(itemModels, ModItems.AVERAGE_UMBRAL_ESSENCE.get(), "average", false);

        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_UNDEAD_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_WATER_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_NETHER_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_CHIMERA_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_ARID_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_FROZEN_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_VOID_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_AIR_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_EARTH_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_NATURE_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_LIGHTNING_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_RADIANT_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.MEDIUM_UMBRAL_AMPOULE.get(), "medium_ampoule", "medium_ampoule_essence");

        createEssenceItem(itemModels, ModItems.STRONG_UNDEAD_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_WATER_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_NETHER_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_CHIMERA_ESSENCE.get(), "strong", true);
        createEssenceItem(itemModels, ModItems.STRONG_ARID_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_FROZEN_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_VOID_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_AIR_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_EARTH_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_NATURE_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_LIGHTNING_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_RADIANT_ESSENCE.get(), "strong", false);
        createEssenceItem(itemModels, ModItems.STRONG_UMBRAL_ESSENCE.get(), "strong", false);

        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_UNDEAD_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_WATER_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_NETHER_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_CHIMERA_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_ARID_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_FROZEN_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_VOID_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_AIR_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_EARTH_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_NATURE_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_LIGHTNING_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_RADIANT_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
        createTintedAmpoule(itemModels, twoLayerTemplate, ModItems.LARGE_UMBRAL_AMPOULE.get(), "large_ampoule", "large_ampoule_essence");
    }

    // ==========================================
    // BULLETPROOF 3D MODEL GENERATOR (1.21.10)
    // ==========================================
    private void createVisFumePipeModels(BlockModelGenerators blockModels, Block block, String textureName) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName);

        // 1. Generate 6x6x6 Core Model using the strict 3-parameter signature
        ModelTemplate coreTemplate = new ModelTemplate(Optional.empty(), Optional.empty(), TextureSlot.PARTICLE, TextureSlot.TEXTURE);
        coreTemplate.create(
                ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName + "_core"),
                new TextureMapping().put(TextureSlot.PARTICLE, texture).put(TextureSlot.TEXTURE, texture),
                blockModels.modelOutput
        );

        // 2. Generate Arm Model using the strict 3-parameter signature
        ModelTemplate armTemplate = new ModelTemplate(Optional.empty(), Optional.empty(), TextureSlot.PARTICLE, TextureSlot.TEXTURE);
        armTemplate.create(
                ResourceLocation.fromNamespaceAndPath("entropica", "block/" + textureName + "_arm"),
                new TextureMapping().put(TextureSlot.PARTICLE, texture).put(TextureSlot.TEXTURE, texture),
                blockModels.modelOutput
        );
    }

    // ==========================================
    // HELPERS
    // ==========================================
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

    private void createTintedAmpoule(ItemModelGenerators itemModels, ModelTemplate template, Item item,
                                     String glassTexture, String overlayTexture) {
        ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(item);
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.LAYER0, ResourceLocation.fromNamespaceAndPath("entropica", "item/" + glassTexture))
                .put(TextureSlot.LAYER1, ResourceLocation.fromNamespaceAndPath("entropica", "item/" + overlayTexture));
        template.create(modelLocation, mapping, itemModels.modelOutput);

        itemModels.itemModelOutput.accept(
                item,
                new BlockModelWrapper.Unbaked(modelLocation, java.util.List.of(
                        new net.minecraft.client.color.item.Constant(-1),
                        new ddraig.net.entropica.client.ModClientEvents.AmpouleTint()
                ))
        );
    }

    private void createEssenceItem(ItemModelGenerators itemModels, Item item, String tier, boolean isChimera) {
        ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(item);
        String textureName;

        if (isChimera) {
            String chimeraPrefix = tier.equals("weak") ? "small" : tier.equals("average") ? "medium" : "large";
            textureName = chimeraPrefix + "_chimera_essence";
        } else {
            textureName = tier + "_essence";
        }

        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.LAYER0, ResourceLocation.fromNamespaceAndPath("entropica", "item/" + textureName));

        ModelTemplates.FLAT_ITEM.create(modelLocation, mapping, itemModels.modelOutput);

        if (!isChimera) {
            itemModels.itemModelOutput.accept(
                    item,
                    new BlockModelWrapper.Unbaked(modelLocation, java.util.List.of(
                            new ddraig.net.entropica.client.ModClientEvents.AmpouleTint()
                    ))
            );
        } else {
            itemModels.itemModelOutput.accept(
                    item,
                    new BlockModelWrapper.Unbaked(modelLocation, Collections.emptyList())
            );
        }
    }
}