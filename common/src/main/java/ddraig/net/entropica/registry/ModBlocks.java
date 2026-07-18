package ddraig.net.entropica.registry;

import ddraig.net.entropica.block.*;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.Collection;
import java.util.List;

public class ModBlocks {
    
    public static class BlocksWrapper {
        private final DeferredRegister<Block> parent = DeferredRegister.create("entropica", Registries.BLOCK);

        public void register() {
            parent.register();
        }

        public void register(Object ignoredBus) {
            parent.register();
        }

        public <T extends Block> RegistrySupplier<T> register(String name, java.util.function.Function<ResourceLocation, T> factory) {
            return parent.register(name, () -> factory.apply(ResourceLocation.fromNamespaceAndPath("entropica", name)));
        }

        public Collection<RegistrySupplier<Block>> getEntries() {
            List<RegistrySupplier<Block>> list = new java.util.ArrayList<>();
            parent.forEach(list::add);
            return list;
        }
    }

    public static final BlocksWrapper BLOCKS = new BlocksWrapper();

    public static final RegistrySupplier<Block> DILUTED_ESSENCE_FLUID_BLOCK = registerDilutedEssenceBlock();

    // Ores
    public static final RegistrySupplier<Block> ENTROPIC_ORE = BLOCKS.register("entropic_ore", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> VORPALITE_ORE = BLOCKS.register("vorpalite_ore", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> SORROWSTONE_ORE = BLOCKS.register("sorrowstone_ore", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> UMBRALITE_ORE = BLOCKS.register("umbralite_ore", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(3.0f).requiresCorrectToolForDrops()));

    // Crystals: Aeterium
    public static final RegistrySupplier<Block> SMALL_AETERIUM_BUD = BLOCKS.register("small_aeterium_bud", name -> new AmethystClusterBlock(6, 3, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 1).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> MEDIUM_AETERIUM_BUD = BLOCKS.register("medium_aeterium_bud", name -> new AmethystClusterBlock(8, 2, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 2).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> LARGE_AETERIUM_BUD = BLOCKS.register("large_aeterium_bud", name -> new AmethystClusterBlock(10, 1, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 4).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> AETERIUM_CLUSTER = BLOCKS.register("aeterium_cluster", name -> new AmethystClusterBlock(12, 1, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 5).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> BUDDING_AETERIUM = BLOCKS.register("budding_aeterium", name -> new BuddingCrystalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).randomTicks().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST).requiresCorrectToolForDrops(), () -> SMALL_AETERIUM_BUD.get(), () -> MEDIUM_AETERIUM_BUD.get(), () -> LARGE_AETERIUM_BUD.get(), () -> AETERIUM_CLUSTER.get()));
    public static final RegistrySupplier<Block> AETERIUM_CRYSTAL_BLOCK = BLOCKS.register("aeterium_crystal_block", name -> new AmethystBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));

    // Crystals: Ignisite
    public static final RegistrySupplier<Block> SMALL_IGNISITE_BUD = BLOCKS.register("small_ignisite_bud", name -> new AmethystClusterBlock(6, 3, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 1).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> MEDIUM_IGNISITE_BUD = BLOCKS.register("medium_ignisite_bud", name -> new AmethystClusterBlock(8, 2, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 2).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> LARGE_IGNISITE_BUD = BLOCKS.register("large_ignisite_bud", name -> new AmethystClusterBlock(10, 1, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 4).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> IGNISITE_CLUSTER = BLOCKS.register("ignisite_cluster", name -> new AmethystClusterBlock(12, 1, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 5).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> BUDDING_IGNISITE = BLOCKS.register("budding_ignisite", name -> new BuddingCrystalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).randomTicks().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST).requiresCorrectToolForDrops(), () -> SMALL_IGNISITE_BUD.get(), () -> MEDIUM_IGNISITE_BUD.get(), () -> LARGE_IGNISITE_BUD.get(), () -> IGNISITE_CLUSTER.get()));
    public static final RegistrySupplier<Block> IGNISITE_CRYSTAL_BLOCK = BLOCKS.register("ignisite_crystal_block", name -> new AmethystBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));

    // Crystals: Mortisite
    public static final RegistrySupplier<Block> SMALL_MORTISITE_BUD = BLOCKS.register("small_mortisite_bud", name -> new AmethystClusterBlock(6, 3, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 1).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> MEDIUM_MORTISITE_BUD = BLOCKS.register("medium_mortisite_bud", name -> new AmethystClusterBlock(8, 2, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 2).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> LARGE_MORTISITE_BUD = BLOCKS.register("large_mortisite_bud", name -> new AmethystClusterBlock(10, 1, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 4).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> MORTISITE_CLUSTER = BLOCKS.register("mortisite_cluster", name -> new AmethystClusterBlock(12, 1, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 5).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> BUDDING_MORTISITE = BLOCKS.register("budding_mortisite", name -> new BuddingCrystalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).randomTicks().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST).requiresCorrectToolForDrops(), () -> SMALL_MORTISITE_BUD.get(), () -> MEDIUM_MORTISITE_BUD.get(), () -> LARGE_MORTISITE_BUD.get(), () -> MORTISITE_CLUSTER.get()));
    public static final RegistrySupplier<Block> MORTISITE_CRYSTAL_BLOCK = BLOCKS.register("mortisite_crystal_block", name -> new AmethystBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));

    // Core Machines
    public static final RegistrySupplier<ManaFurnaceBlock> MANA_FURNACE = BLOCKS.register("mana_furnace", name -> new ManaFurnaceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ENTROPIC_AUTO_SMELTER = BLOCKS.register("entropic_auto_smelter", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops()));

    // Mana Furnace Multiblock Components & Arcane Blocks
    public static final RegistrySupplier<EntropicCoreBlock> ENTROPIC_CORE = BLOCKS.register("entropic_core", name -> new EntropicCoreBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ARCANE_BRICK = BLOCKS.register("arcane_brick", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(2.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ARCANITE_PLATING = BLOCKS.register("arcanite_plating", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ARCANE_CLAY_BLOCK = BLOCKS.register("arcane_clay_block", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.CLAY).destroyTime(0.6f)));
    public static final RegistrySupplier<Block> ARCANE_FORGE_BASE = BLOCKS.register("arcane_forge_base", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(1.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<ManaPlumeBlock> MANA_PLUME = BLOCKS.register("mana_plume", name -> new ManaPlumeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistrySupplier<MateriaExhaustBlock> MATERIA_EXHAUST = BLOCKS.register("materia_exhaust", name -> new MateriaExhaustBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<CatalystReceptacleBlock> CATALYST_RECEPTACLE = BLOCKS.register("catalyst_receptacle", name -> new CatalystReceptacleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    // Component Blocks
    public static final RegistrySupplier<EssenceReceptacleBlock> ESSENCE_RECEPTACLE = BLOCKS.register("essence_receptacle", name -> new EssenceReceptacleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<FurnaceHatchBlock> FURNACE_HATCH = BLOCKS.register("furnace_hatch", name -> new FurnaceHatchBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));

    // Vis Vitae Network
    public static final RegistrySupplier<OrbisCellBlock> ORBIS_CELL = BLOCKS.register("orbis_cell", name -> new OrbisCellBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).requiresCorrectToolForDrops()));

    // Vapor Pneumatic Network (Gas)
    public static final RegistrySupplier<VaporPneumaticValveBlock> VAPOR_PNEUMATIC_VALVE = BLOCKS.register("vapor_pneumatic_valve", name -> new VaporPneumaticValveBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticOneWayValveBlock> VAPOR_PNEUMATIC_ONE_WAY_VALVE = BLOCKS.register("vapor_pneumatic_one_way_valve", name -> new VaporPneumaticOneWayValveBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticDiverterBlock> VAPOR_PNEUMATIC_DIVERTER = BLOCKS.register("vapor_pneumatic_diverter", name -> new VaporPneumaticDiverterBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<MateriaPumpBlock> MATERIA_PUMP = BLOCKS.register("materia_pump", name -> new MateriaPumpBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // VAPOR PNEUMATIC PIPES (T2/T3 — Mfum/Msub)
    // ==========================================
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_COPPER    = BLOCKS.register("vapor_pneumatic_pipe_copper",    name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_IRON       = BLOCKS.register("vapor_pneumatic_pipe_iron",       name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_GOLD       = BLOCKS.register("vapor_pneumatic_pipe_gold",       name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_ARCANITE   = BLOCKS.register("vapor_pneumatic_pipe_arcanite",   name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_DIAMOND    = BLOCKS.register("vapor_pneumatic_pipe_diamond",    name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(2.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_VISCANITE  = BLOCKS.register("vapor_pneumatic_pipe_viscanite",  name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GRAY).destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_RESONITE   = BLOCKS.register("vapor_pneumatic_pipe_resonite",   name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CHARGED_ARCANITE  = BLOCKS.register("vapor_pneumatic_pipe_charged_arcanite",  name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CHARGED_VISCANITE = BLOCKS.register("vapor_pneumatic_pipe_charged_viscanite", name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GRAY).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CHARGED_RESONITE  = BLOCKS.register("vapor_pneumatic_pipe_charged_resonite",  name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CAPUTITE         = BLOCKS.register("vapor_pneumatic_pipe_caputite",         name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CHARGED_CAPUTITE = BLOCKS.register("vapor_pneumatic_pipe_charged_caputite", name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_SANGUINITE        = BLOCKS.register("vapor_pneumatic_pipe_sanguinite",        name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CHARGED_SANGUINITE = BLOCKS.register("vapor_pneumatic_pipe_charged_sanguinite", name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(4.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_MERCURITE        = BLOCKS.register("vapor_pneumatic_pipe_mercurite",        name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CHARGED_MERCURITE = BLOCKS.register("vapor_pneumatic_pipe_charged_mercurite", name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_EUCLIDITE        = BLOCKS.register("vapor_pneumatic_pipe_euclidite",        name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(4.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CHARGED_EUCLIDITE = BLOCKS.register("vapor_pneumatic_pipe_charged_euclidite", name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(5.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_ATHANORITE       = BLOCKS.register("vapor_pneumatic_pipe_athanorite",       name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(5.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VaporPneumaticPipeBlock> VAPOR_PNEUMATIC_PIPE_CHARGED_ATHANORITE = BLOCKS.register("vapor_pneumatic_pipe_charged_athanorite", name -> new VaporPneumaticPipeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(5.5f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // HYDRAULIC PIPELINE (T4 — Mliq)
    // ==========================================
    public static final RegistrySupplier<HydraulicPipelineBlock> HYDRAULIC_PIPELINE_IRON      = BLOCKS.register("hydraulic_pipeline_iron",      name -> new HydraulicPipelineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<HydraulicPipelineBlock> HYDRAULIC_PIPELINE_ARCANITE  = BLOCKS.register("hydraulic_pipeline_arcanite",  name -> new HydraulicPipelineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<HydraulicPipelineBlock> HYDRAULIC_PIPELINE_VISCANITE = BLOCKS.register("hydraulic_pipeline_viscanite", name -> new HydraulicPipelineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GRAY).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<HydraulicPipelineBlock> HYDRAULIC_PIPELINE_RESONITE  = BLOCKS.register("hydraulic_pipeline_resonite",  name -> new HydraulicPipelineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<HydraulicPipelineBlock> HYDRAULIC_PIPELINE_CHARGED_ARCANITE  = BLOCKS.register("hydraulic_pipeline_charged_arcanite",  name -> new HydraulicPipelineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<HydraulicPipelineBlock> HYDRAULIC_PIPELINE_CHARGED_VISCANITE = BLOCKS.register("hydraulic_pipeline_charged_viscanite", name -> new HydraulicPipelineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GRAY).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<HydraulicPipelineBlock> HYDRAULIC_PIPELINE_CHARGED_RESONITE  = BLOCKS.register("hydraulic_pipeline_charged_resonite",  name -> new HydraulicPipelineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // VOLTAIC CONDUIT (T5 — Mvol)
    // ==========================================
    public static final RegistrySupplier<VoltaicConduitBlock> VOLTAIC_CONDUIT_ARCANITE          = BLOCKS.register("voltaic_conduit_arcanite",          name -> new VoltaicConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VoltaicConduitBlock> VOLTAIC_CONDUIT_VISCANITE         = BLOCKS.register("voltaic_conduit_viscanite",         name -> new VoltaicConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GRAY).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VoltaicConduitBlock> VOLTAIC_CONDUIT_RESONITE          = BLOCKS.register("voltaic_conduit_resonite",          name -> new VoltaicConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VoltaicConduitBlock> VOLTAIC_CONDUIT_CHARGED_ARCANITE  = BLOCKS.register("voltaic_conduit_charged_arcanite",  name -> new VoltaicConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VoltaicConduitBlock> VOLTAIC_CONDUIT_CHARGED_VISCANITE = BLOCKS.register("voltaic_conduit_charged_viscanite", name -> new VoltaicConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GRAY).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<VoltaicConduitBlock> VOLTAIC_CONDUIT_CHARGED_RESONITE  = BLOCKS.register("voltaic_conduit_charged_resonite",  name -> new VoltaicConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // T6 VISCOUS AGITATOR (Mcoa) — Caputite variants
    // ==========================================
    public static final RegistrySupplier<ViscousAgitatorBlock> VISCOUS_AGITATOR_CAPUTITE         = BLOCKS.register("viscous_agitator_caputite",         name -> new ViscousAgitatorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<ViscousAgitatorBlock> VISCOUS_AGITATOR_CHARGED_CAPUTITE = BLOCKS.register("viscous_agitator_charged_caputite", name -> new ViscousAgitatorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // T7 SANGUINE CONDUIT (Mich) — Sanguinite variants
    // ==========================================
    public static final RegistrySupplier<SanguineConduitBlock> SANGUINE_CONDUIT_SANGUINITE         = BLOCKS.register("sanguine_conduit_sanguinite",         name -> new SanguineConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<SanguineConduitBlock> SANGUINE_CONDUIT_CHARGED_SANGUINITE = BLOCKS.register("sanguine_conduit_charged_sanguinite", name -> new SanguineConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(4.0f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // T8 EQUILIBRIUM CONDUIT (Mtra) — Mercurite variants
    // ==========================================
    public static final RegistrySupplier<EquilibriumConduitBlock> EQUILIBRIUM_CONDUIT_MERCURITE         = BLOCKS.register("equilibrium_conduit_mercurite",         name -> new EquilibriumConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GRAY).destroyTime(4.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<EquilibriumConduitBlock> EQUILIBRIUM_CONDUIT_CHARGED_MERCURITE = BLOCKS.register("equilibrium_conduit_charged_mercurite", name -> new EquilibriumConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GRAY).destroyTime(4.5f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // T9 PRISTINE CONDUIT (Mper) — Euclidite variants
    // ==========================================
    public static final RegistrySupplier<PristineConduitBlock> PRISTINE_CONDUIT_EUCLIDITE         = BLOCKS.register("pristine_conduit_euclidite",         name -> new PristineConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.QUARTZ).destroyTime(4.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<PristineConduitBlock> PRISTINE_CONDUIT_CHARGED_EUCLIDITE = BLOCKS.register("pristine_conduit_charged_euclidite", name -> new PristineConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.QUARTZ).destroyTime(5.0f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // T10 ATHANOR CONDUIT (Mlim) — Athanorite variants
    // ==========================================
    public static final RegistrySupplier<AthanorConduitBlock> ATHANOR_CONDUIT_ATHANORITE         = BLOCKS.register("athanor_conduit_athanorite",         name -> new AthanorConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(5.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<AthanorConduitBlock> ATHANOR_CONDUIT_CHARGED_ATHANORITE = BLOCKS.register("athanor_conduit_charged_athanorite", name -> new AthanorConduitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(6.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistrySupplier<DecompressionCouplerBlock> DECOMPRESSION_COUPLING = BLOCKS.register("decompression_coupling", name -> new DecompressionCouplerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().noOcclusion()));

    // ==========================================
    // ADVANCED ORBIS CELLS (Vis Vitae Network — higher Materia stages)
    // ==========================================
    public static final RegistrySupplier<SublimatedOrbisCellBlock> SUBLIMATED_ORBIS_CELL = BLOCKS.register("sublimated_orbis_cell", name -> new SublimatedOrbisCellBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<PneumaticCalixBlock>      PNEUMATIC_CALIX       = BLOCKS.register("pneumatic_calix",       name -> new PneumaticCalixBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLUE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<VoltaicCalixBlock>        VOLTAIC_CALIX         = BLOCKS.register("voltaic_calix",         name -> new VoltaicCalixBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MatrixCalixBlock>         MATRIX_CALIX          = BLOCKS.register("matrix_calix",          name -> new MatrixCalixBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<ThecaCellBlock>           THECA_CELL            = BLOCKS.register("theca_cell",            name -> new ThecaCellBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(4.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<VasCellBlock>             VAS_CELL              = BLOCKS.register("vas_cell",              name -> new VasCellBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GRAY).destroyTime(4.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MonadCoreBlock>           MONAD_CORE            = BLOCKS.register("monad_core",            name -> new MonadCoreBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.QUARTZ).destroyTime(5.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<AthanorCoreBlock>         ATHANOR_CORE          = BLOCKS.register("athanor_core",          name -> new AthanorCoreBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(6.0f).requiresCorrectToolForDrops()));

    // Developer & Creative Blocks
    public static final RegistrySupplier<CreativeMateriaGeneratorBlock> CREATIVE_MATERIA_GENERATOR = BLOCKS.register("creative_materia_generator", name -> new CreativeMateriaGeneratorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<CreativeParticleGeneratorBlock> CREATIVE_PARTICLE_GENERATOR = BLOCKS.register("creative_particle_generator", name -> new CreativeParticleGeneratorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops()));

    // ---  Vis Fume Pressure Vessel ---
    public static final RegistrySupplier<MateriaVesselControllerBlock> MATERIA_VESSEL_CONTROLLER = BLOCKS.register("materia_vessel_controller", name -> new MateriaVesselControllerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MateriaVesselPortBlock> MATERIA_VESSEL_PORT = BLOCKS.register("materia_vessel_port", name -> new MateriaVesselPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<ManaEnrichedGlassBlock> ESSENCE_ENRICHED_GLASS = BLOCKS.register("essence_enriched_glass", name -> new ManaEnrichedGlassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.NONE).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<ManaEnrichedGlassBlock> MATERIA_FUMUS_STRENGTHENED_GLASS = BLOCKS.register("materia_fumus_strengthened_glass", name -> new ManaEnrichedGlassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.NONE).destroyTime(2.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<ManaEnrichedGlassBlock> MATERIA_LIQUIDA_ENRICHED_GLASS = BLOCKS.register("materia_liquida_enriched_glass", name -> new ManaEnrichedGlassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.NONE).destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<ManaEnrichedGlassBlock> FRAGMENT_LATTICE_GLASS = BLOCKS.register("fragment_lattice_glass", name -> new ManaEnrichedGlassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.NONE).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    // --- Vis Fume Pressure Chamber ---
    public static final RegistrySupplier<Block> MATERIA_PRESSURE_CHAMBER_CONTROLLER = BLOCKS.register("materia_pressure_chamber_controller", name -> new MateriaPressureChamberControllerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ENRICHMENT_TABLE = BLOCKS.register("enrichment_table", name -> new EnrichmentTableBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops()));

    //Vis Vitae Contraptions
    public static final RegistrySupplier<Block> VIS_VITAE_ANCHOR = BLOCKS.register("vis_vitae_anchor", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> VIS_VITAE_CONDENSER = BLOCKS.register("vis_vitae_condenser", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> VIS_VITAE_VACUUM = BLOCKS.register("vis_vitae_vacuum", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> VITAE_BARREL = BLOCKS.register("vitae_barrel", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f)));

    // Mana Network
    public static final RegistrySupplier<MateriaFilterBlock> MATERIA_FILTER = BLOCKS.register("materia_filter", name -> new MateriaFilterBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(1.5f).requiresCorrectToolForDrops().noOcclusion()));

    // Crafting & Magic Stations
    public static final RegistrySupplier<AethericSynthesizerBlock> AETHERIC_SYNTHESIZER = BLOCKS.register("aetheric_synthesizer", name -> new AethericSynthesizerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<AethericAutomatorBlock> AETHERIC_AUTOMATOR = BLOCKS.register("aetheric_automator", name -> new AethericAutomatorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<SynthesizerUserInterfaceBlock> SYNTHESIZER_USER_INTERFACE = BLOCKS.register("synthesizer_user_interface", name -> new SynthesizerUserInterfaceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistrySupplier<Block> ARCANE_LOOM = BLOCKS.register("arcane_loom", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f)));
    public static final RegistrySupplier<Block> ESSENCE_FORGE = BLOCKS.register("essence_forge", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ARCANE_ANVIL = BLOCKS.register("arcane_anvil", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(5.0f).requiresCorrectToolForDrops()));

    // --- Dynamic Weapon Forging Blocks ---
    public static final RegistrySupplier<EidolicFocalPedestalBlock> EIDOLIC_FOCAL_PEDESTAL = BLOCKS.register("eidolic_focal_pedestal", name -> new EidolicFocalPedestalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<AttunementPedestalBlock> ATTUNEMENT_PEDESTAL = BLOCKS.register("attunement_pedestal", name -> new AttunementPedestalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<Block> MORPHIC_LOOM = BLOCKS.register("morphic_loom", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f)));

    // Readout Blocks
    public static final RegistrySupplier<MateriaReadoutBlock> VIS_READOUT = BLOCKS.register("materia_readout", name -> new MateriaReadoutBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<EssenceReadoutBlock> ESSENCE_READOUT = BLOCKS.register("raw_materia_readout", name -> new EssenceReadoutBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));

    // --- Extraction Multiblock & Machines ---
    public static final RegistrySupplier<MateriaSimpleMachineBlock> MATERIA_SIMPLE_MACHINE_BLOCK = BLOCKS.register("materia_simple_machine_block", name -> new MateriaSimpleMachineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MateriaComplexMachineBlock> MATERIA_COMPLEX_MACHINE_BLOCK = BLOCKS.register("materia_complex_machine_block", name -> new MateriaComplexMachineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops()));

    public static final RegistrySupplier<VaporPneumaticInputPortBlock> VAPOR_PNEUMATIC_INPUT_PORT = BLOCKS.register("vapor_pneumatic_input_port", name -> new VaporPneumaticInputPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<HydraulicInputPortBlock> HYDRAULIC_INPUT_PORT = BLOCKS.register("hydraulic_input_port", name -> new HydraulicInputPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<ExtractorOutputPortBlock> EXTRACTOR_OUTPUT_PORT = BLOCKS.register("extractor_output_port", name -> new ExtractorOutputPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));

    public static final RegistrySupplier<EssenceNodeIdentifierBlock> ESSENCE_NODE_IDENTIFIER_BLOCK = BLOCKS.register("essence_node_identifier_block", name -> new EssenceNodeIdentifierBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(4.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MateriaExtractionApparatusBlock> MATERIA_EXTRACTION_APPARATUS = BLOCKS.register("materia_extraction_apparatus", name -> new MateriaExtractionApparatusBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(4.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MateriaExtractorBaseBlock> MATERIA_EXTRACTOR_BASE = BLOCKS.register("materia_extractor_base", name -> new MateriaExtractorBaseBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(4.0f).requiresCorrectToolForDrops()));

    public static final RegistrySupplier<MateriaMotorBlock> MATERIA_MOTOR = BLOCKS.register("materia_motor", name -> new MateriaMotorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));

    // --- Void Rifts Logistics ---
    public static final RegistrySupplier<VoidRiftBlock> VOID_RIFT = BLOCKS.register("void_rift", name -> new VoidRiftBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name))));

    // --- Crucible & Ritual Bowls ---
    public static final RegistrySupplier<CrucibleBlock> CRUCIBLE = BLOCKS.register("crucible", name -> new CrucibleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));

    public static final RegistrySupplier<RitualBowlBlock> MARBLE_RITUAL_BOWL = BLOCKS.register("marble_ritual_bowl", name -> new RitualBowlBlock(false, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));
    public static final RegistrySupplier<RitualBowlBlock> BASALT_RITUAL_BOWL = BLOCKS.register("basalt_ritual_bowl", name -> new RitualBowlBlock(true, BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT).setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));

    // --- Runic Scribing & Research ---
    public static final RegistrySupplier<ddraig.net.entropica.block.ScribedChalkBlock> SCRIBED_CHALK =
            BLOCKS.register("scribed_chalk", name -> new ddraig.net.entropica.block.ScribedChalkBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).noCollision().noOcclusion().instabreak().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
            
    public static final RegistrySupplier<net.minecraft.world.level.block.Block> VISCANITE_SYNTHESIZER =
            BLOCKS.register("viscanite_synthesizer", name -> new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
            
    public static final RegistrySupplier<ddraig.net.entropica.block.ScribingControllerBlock> SCRIBING_CONTROLLER =
            BLOCKS.register("scribing_controller", name -> new ddraig.net.entropica.block.ScribingControllerBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops().noOcclusion()));
            
    public static final RegistrySupplier<ddraig.net.entropica.block.ViscanitePistonPressBlock> VISCANITE_PISTON_PRESS =
            BLOCKS.register("viscanite_piston_press", name -> new ddraig.net.entropica.block.ViscanitePistonPressBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops().noOcclusion()));
            
    public static final RegistrySupplier<ddraig.net.entropica.block.DraftingTableBlock> DRAFTING_TABLE =
            BLOCKS.register("drafting_table", name -> new ddraig.net.entropica.block.DraftingTableBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f).noOcclusion()));
            
    public static final RegistrySupplier<ddraig.net.entropica.block.ResearchBenchBlock> RESEARCH_BENCH =
            BLOCKS.register("research_bench", name -> new ddraig.net.entropica.block.ResearchBenchBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.EssenceRepulsionWardBlock> ESSENCE_REPULSION_WARD =
            BLOCKS.register("essence_repulsion_ward", name -> new ddraig.net.entropica.block.EssenceRepulsionWardBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(net.minecraft.world.level.material.MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    public static final List<RegistrySupplier<?>> SIMPLE_BLOCKS = List.of(
            ENTROPIC_ORE, VORPALITE_ORE, SORROWSTONE_ORE, UMBRALITE_ORE,
            AETERIUM_CRYSTAL_BLOCK, BUDDING_AETERIUM, SMALL_AETERIUM_BUD, MEDIUM_AETERIUM_BUD, LARGE_AETERIUM_BUD, AETERIUM_CLUSTER,
            IGNISITE_CRYSTAL_BLOCK, BUDDING_IGNISITE, SMALL_IGNISITE_BUD, MEDIUM_IGNISITE_BUD, LARGE_IGNISITE_BUD, IGNISITE_CLUSTER,
            MORTISITE_CRYSTAL_BLOCK, BUDDING_MORTISITE, SMALL_MORTISITE_BUD, MEDIUM_MORTISITE_BUD, LARGE_MORTISITE_BUD, MORTISITE_CLUSTER,
            ARCANE_BRICK, ARCANITE_PLATING, ARCANE_CLAY_BLOCK, MANA_PLUME,
            VIS_VITAE_ANCHOR, VIS_VITAE_CONDENSER, VIS_VITAE_VACUUM, VITAE_BARREL,
            MATERIA_FILTER, ARCANE_LOOM, ESSENCE_FORGE,
            ARCANE_ANVIL, MORPHIC_LOOM, MATERIA_EXHAUST, MATERIA_SIMPLE_MACHINE_BLOCK, MATERIA_COMPLEX_MACHINE_BLOCK,
            VAPOR_PNEUMATIC_INPUT_PORT, HYDRAULIC_INPUT_PORT, EXTRACTOR_OUTPUT_PORT,
            ESSENCE_NODE_IDENTIFIER_BLOCK, MATERIA_EXTRACTION_APPARATUS, MATERIA_EXTRACTOR_BASE, MATERIA_MOTOR,
            CREATIVE_PARTICLE_GENERATOR,
            VISCANITE_SYNTHESIZER, SCRIBING_CONTROLLER, VISCANITE_PISTON_PRESS, DRAFTING_TABLE, RESEARCH_BENCH,
            ESSENCE_REPULSION_WARD
    );

    @ExpectPlatform
    public static RegistrySupplier<Block> registerDilutedEssenceBlock() {
        throw new AssertionError();
    }
}