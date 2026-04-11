package ddraig.net.entropica.registry;

import ddraig.net.entropica.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("entropica");

    public static final DeferredBlock<DilutedEssenceFluidBlock> DILUTED_ESSENCE_FLUID_BLOCK = BLOCKS.register("diluted_essence_fluid_block",
            name -> new DilutedEssenceFluidBlock(ModFluids.getSource(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(ResourceKey.create(Registries.BLOCK, name)).noLootTable().liquid()));

    // Ores
    public static final DeferredBlock<Block> ENTROPIC_ORE = BLOCKS.register("entropic_ore", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(3.0f).requiresCorrectToolForDrops()));

    // Core Machines
    public static final DeferredBlock<ManaFurnaceBlock> MANA_FURNACE = BLOCKS.register("mana_furnace", name -> new ManaFurnaceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ENTROPIC_AUTO_SMELTER = BLOCKS.register("entropic_auto_smelter", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops()));

    // Mana Furnace Multiblock Components & Arcane Blocks
    public static final DeferredBlock<EntropicCoreBlock> ENTROPIC_CORE = BLOCKS.register("entropic_core", name -> new EntropicCoreBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ARCANE_BRICK = BLOCKS.register("arcane_brick", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(2.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ARCANITE_PLATING = BLOCKS.register("arcanite_plating", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ARCANE_CLAY_BLOCK = BLOCKS.register("arcane_clay_block", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.CLAY).destroyTime(0.6f)));
    public static final DeferredBlock<Block> ARCANE_FORGE_BASE = BLOCKS.register("arcane_forge_base", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(1.5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<ManaPlumeBlock> MANA_PLUME = BLOCKS.register("mana_plume", name -> new ManaPlumeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<VisExhaustBlock> VIS_EXHAUST = BLOCKS.register("vis_exhaust", name -> new VisExhaustBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<CatalystReceptacleBlock> CATALYST_RECEPTACLE = BLOCKS.register("catalyst_receptacle", name -> new CatalystReceptacleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    // Component Blocks
    public static final DeferredBlock<EssenceReceptacleBlock> ESSENCE_RECEPTACLE = BLOCKS.register("essence_receptacle", name -> new EssenceReceptacleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<FurnaceHatchBlock> FURNACE_HATCH = BLOCKS.register("furnace_hatch", name -> new FurnaceHatchBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));

    // Vis Vitae Network
    public static final DeferredBlock<OrbisCellBlock> ORBIS_CELL = BLOCKS.register("orbis_cell", name -> new OrbisCellBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).requiresCorrectToolForDrops()));

    // Vis Fume Network (Gas)
    public static final DeferredBlock<VisFumePipeBlock> VIS_FUME_PIPE = BLOCKS.register("vis_fume_pipe", name -> new VisFumePipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<VisFumeValveBlock> VIS_FUME_VALVE = BLOCKS.register("vis_fume_valve", name -> new VisFumeValveBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<VisFumeOneWayValveBlock> VIS_FUME_ONE_WAY_VALVE = BLOCKS.register("vis_fume_one_way_valve", name -> new VisFumeOneWayValveBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<VisFumeDiverterBlock> VIS_FUME_DIVERTER = BLOCKS.register("vis_fume_diverter", name -> new VisFumeDiverterBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().noOcclusion()));

    // Developer & Creative Blocks
    public static final DeferredBlock<CreativeVisFumeGeneratorBlock> CREATIVE_VIS_FUME_GENERATOR = BLOCKS.register("creative_vis_fume_generator", name -> new CreativeVisFumeGeneratorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<CreativeParticleGeneratorBlock> CREATIVE_PARTICLE_GENERATOR = BLOCKS.register("creative_particle_generator", name -> new CreativeParticleGeneratorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops()));

    // ---  Vis Fume Pressure Vessel ---
    public static final DeferredBlock<VisFumeVesselControllerBlock> VIS_FUME_VESSEL_CONTROLLER = BLOCKS.register("vis_fume_vessel_controller", name -> new VisFumeVesselControllerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<VisFumeVesselPortBlock> VIS_FUME_VESSEL_PORT = BLOCKS.register("vis_fume_vessel_port", name -> new VisFumeVesselPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<ManaEnrichedGlassBlock> ESSENCE_ENRICHED_GLASS = BLOCKS.register("essence_enriched_glass", name -> new ManaEnrichedGlassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.NONE).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<ManaEnrichedGlassBlock> VIS_FUME_STRENGTHENED_GLASS = BLOCKS.register("vis_fume_strengthened_glass", name -> new ManaEnrichedGlassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.NONE).destroyTime(2.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<ManaEnrichedGlassBlock> VIS_ICHOR_ENRICHED_GLASS = BLOCKS.register("vis_ichor_enriched_glass", name -> new ManaEnrichedGlassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.NONE).destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<ManaEnrichedGlassBlock> FRAGMENT_LATTICE_GLASS = BLOCKS.register("fragment_lattice_glass", name -> new ManaEnrichedGlassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.NONE).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    // --- Vis Fume Pressure Chamber ---
    public static final DeferredBlock<Block> VIS_FUME_PRESSURE_CHAMBER_CONTROLLER = BLOCKS.register("vis_fume_pressure_chamber_controller", name -> new VisFumePressureChamberControllerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ENRICHMENT_TABLE = BLOCKS.register("enrichment_table", name -> new EnrichmentTableBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops()));

    //Vis Vitae Contraptions
    public static final DeferredBlock<Block> VIS_VITAE_ANCHOR = BLOCKS.register("vis_vitae_anchor", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> VIS_VITAE_CONDENSER = BLOCKS.register("vis_vitae_condenser", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> VIS_VITAE_VACUUM = BLOCKS.register("vis_vitae_vacuum", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> VITAE_BARREL = BLOCKS.register("vitae_barrel", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f)));

    // Mana Network
    public static final DeferredBlock<ManaFilterBlock> MANA_FILTER = BLOCKS.register("mana_filter", name -> new ManaFilterBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));

    // Crafting & Magic Stations
    public static final DeferredBlock<AethericSynthesizerBlock> AETHERIC_SYNTHESIZER = BLOCKS.register("aetheric_synthesizer", name -> new AethericSynthesizerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<AethericAutomatorBlock> AETHERIC_AUTOMATOR = BLOCKS.register("aetheric_automator", name -> new AethericAutomatorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<SynthesizerUserInterfaceBlock> SYNTHESIZER_USER_INTERFACE = BLOCKS.register("synthesizer_user_interface", name -> new SynthesizerUserInterfaceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<Block> ARCANE_LOOM = BLOCKS.register("arcane_loom", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f)));
    public static final DeferredBlock<Block> ESSENCE_FORGE = BLOCKS.register("essence_forge", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ARCANE_ANVIL = BLOCKS.register("arcane_anvil", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(5.0f).requiresCorrectToolForDrops()));

    // --- Dynamic Weapon Forging Blocks ---
    public static final DeferredBlock<EidolicFocalPedestalBlock> EIDOLIC_FOCAL_PEDESTAL = BLOCKS.register("eidolic_focal_pedestal", name -> new EidolicFocalPedestalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<AttunementPedestalBlock> ATTUNEMENT_PEDESTAL = BLOCKS.register("attunement_pedestal", name -> new AttunementPedestalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MORPHIC_LOOM = BLOCKS.register("morphic_loom", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f)));

    // Readout Blocks
    public static final DeferredBlock<VisReadoutBlock> VIS_READOUT = BLOCKS.register("mana_readout", name -> new VisReadoutBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<EssenceReadoutBlock> ESSENCE_READOUT = BLOCKS.register("essence_readout", name -> new EssenceReadoutBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));

    // --- Extraction Multiblock & Machines ---
    public static final DeferredBlock<VisSimpleMachineBlock> VIS_SIMPLE_MACHINE_BLOCK = BLOCKS.register("vis_simple_machine_block", name -> new VisSimpleMachineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<VisComplexMachineBlock> VIS_COMPLEX_MACHINE_BLOCK = BLOCKS.register("vis_complex_machine_block", name -> new VisComplexMachineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<VisFumeInputPortBlock> VIS_FUME_INPUT_PORT = BLOCKS.register("vis_fume_input_port", name -> new VisFumeInputPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<VisIchorInputPortBlock> VIS_ICHOR_INPUT_PORT = BLOCKS.register("vis_ichor_input_port", name -> new VisIchorInputPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<ExtractorOutputPortBlock> EXTRACTOR_OUTPUT_PORT = BLOCKS.register("extractor_output_port", name -> new ExtractorOutputPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<EssenceNodeIdentifierBlock> ESSENCE_NODE_IDENTIFIER_BLOCK = BLOCKS.register("essence_node_identifier_block", name -> new EssenceNodeIdentifierBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(4.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<VisExtractionApparatusBlock> VIS_EXTRACTION_APPARATUS = BLOCKS.register("vis_extraction_apparatus", name -> new VisExtractionApparatusBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(4.0f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<VisExtractorBaseBlock> VIS_EXTRACTOR_BASE = BLOCKS.register("vis_extractor_base", name -> new VisExtractorBaseBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(4.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<VisMotorBlock> VIS_MOTOR = BLOCKS.register("vis_motor", name -> new VisMotorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));

    // --- Void Rifts Logistics ---
    public static final DeferredBlock<VoidRiftBlock> VOID_RIFT = BLOCKS.register("void_rift", name -> new VoidRiftBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name))));

    // --- Crucible & Ritual Bowls ---
    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = BLOCKS.register("crucible", name -> new CrucibleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));

    public static final DeferredBlock<RitualBowlBlock> MARBLE_RITUAL_BOWL = BLOCKS.register("marble_ritual_bowl", name -> new RitualBowlBlock(false, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));
    public static final DeferredBlock<RitualBowlBlock> BASALT_RITUAL_BOWL = BLOCKS.register("basalt_ritual_bowl", name -> new RitualBowlBlock(true, BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT).setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));


    // ==========================================
    // DATAGEN BLOCK LIST
    // Any basic block added to this list will automatically have a cube model generated.
    // (Void Rift is intentionally omitted as it is an invisible floating entity block!)
    // ==========================================
    public static final List<DeferredBlock<?>> SIMPLE_BLOCKS = List.of(
            ENTROPIC_ORE, ARCANE_BRICK, ARCANITE_PLATING, ARCANE_CLAY_BLOCK, MANA_PLUME,
            VIS_VITAE_ANCHOR, VIS_VITAE_CONDENSER, VIS_VITAE_VACUUM, VITAE_BARREL,
            MANA_FILTER, ARCANE_LOOM, ESSENCE_FORGE,
            ARCANE_ANVIL, MORPHIC_LOOM, VIS_EXHAUST, VIS_SIMPLE_MACHINE_BLOCK, VIS_COMPLEX_MACHINE_BLOCK,
            VIS_FUME_INPUT_PORT, VIS_ICHOR_INPUT_PORT, EXTRACTOR_OUTPUT_PORT,
            ESSENCE_NODE_IDENTIFIER_BLOCK, VIS_EXTRACTION_APPARATUS, VIS_EXTRACTOR_BASE, VIS_MOTOR,
            CREATIVE_PARTICLE_GENERATOR
    );
}