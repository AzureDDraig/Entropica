package ddraig.net.entropica.registry;

import ddraig.net.entropica.block.*;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.BushBlock;
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
    public static final RegistrySupplier<MateriaFurnaceBlock> MATERIA_FURNACE = BLOCKS.register("materia_furnace", name -> new MateriaFurnaceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ENTROPIC_AUTO_SMELTER = BLOCKS.register("entropic_auto_smelter", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops()));

    // Materia Furnace Multiblock Components & Arcane Blocks
    public static final RegistrySupplier<EntropicCoreBlock> ENTROPIC_CORE = BLOCKS.register("entropic_core", name -> new EntropicCoreBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ARCANE_BRICK = BLOCKS.register("arcane_brick", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(2.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ARCANITE_PLATING = BLOCKS.register("arcanite_plating", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ARCANE_CLAY_BLOCK = BLOCKS.register("arcane_clay_block", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.CLAY).destroyTime(0.6f)));
    public static final RegistrySupplier<Block> ARCANE_FORGE_BASE = BLOCKS.register("arcane_forge_base", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(1.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MateriaPlumeBlock> MATERIA_PLUME = BLOCKS.register("materia_plume", name -> new MateriaPlumeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistrySupplier<MateriaExhaustBlock> MATERIA_EXHAUST = BLOCKS.register("materia_exhaust", name -> new MateriaExhaustBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<CatalystReceptacleBlock> CATALYST_RECEPTACLE = BLOCKS.register("catalyst_receptacle", name -> new CatalystReceptacleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    // Component Blocks
    public static final RegistrySupplier<EssenceReceptacleBlock> ESSENCE_RECEPTACLE = BLOCKS.register("essence_receptacle", name -> new EssenceReceptacleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<FurnaceHatchBlock> FURNACE_HATCH = BLOCKS.register("furnace_hatch", name -> new FurnaceHatchBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));

    // Materia Vitae Network
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
    // ADVANCED ORBIS CELLS (Materia Vitae Network — higher Materia stages)
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

    // ---  Materia Fume Pressure Vessel ---
    public static final RegistrySupplier<MateriaVesselControllerBlock> MATERIA_VESSEL_CONTROLLER = BLOCKS.register("materia_vessel_controller", name -> new MateriaVesselControllerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MateriaVesselPortBlock> MATERIA_VESSEL_PORT = BLOCKS.register("materia_vessel_port", name -> new MateriaVesselPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    static {
        AestheticGlassRegistry.init();
    }
    public static final RegistrySupplier<Block> ESSENCE_ENRICHED_GLASS = AestheticGlassRegistry.ESSENCE_ENRICHED_GLASS;
    public static final RegistrySupplier<Block> MATERIA_FUMUS_STRENGTHENED_GLASS = AestheticGlassRegistry.MATERIA_FUMUS_STRENGTHENED_GLASS;
    public static final RegistrySupplier<Block> MATERIA_LIQUIDA_ENRICHED_GLASS = AestheticGlassRegistry.MATERIA_LIQUIDA_ENRICHED_GLASS;
    public static final RegistrySupplier<Block> FRAGMENT_LATTICE_GLASS = AestheticGlassRegistry.FRAGMENT_LATTICE_GLASS;

    // --- Materia Fume Pressure Chamber ---
    public static final RegistrySupplier<Block> MATERIA_PRESSURE_CHAMBER_CONTROLLER = BLOCKS.register("materia_pressure_chamber_controller", name -> new MateriaPressureChamberControllerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ENRICHMENT_TABLE = BLOCKS.register("enrichment_table", name -> new EnrichmentTableBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops()));

    //Materia Vitae Contraptions
    public static final RegistrySupplier<Block> VIS_VITAE_ANCHOR = BLOCKS.register("vis_vitae_anchor", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> VIS_VITAE_CONDENSER = BLOCKS.register("vis_vitae_condenser", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> VIS_VITAE_VACUUM = BLOCKS.register("vis_vitae_vacuum", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.5f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> VITAE_BARREL = BLOCKS.register("vitae_barrel", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f)));

    // Materia Network
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

    // ==========================================
    // GRAVITY MANIPULATION BLOCKS
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.GravitationalAnchorBlock> GRAVITATIONAL_ANCHOR = BLOCKS.register("gravitational_anchor", name -> new ddraig.net.entropica.block.GravitationalAnchorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<ddraig.net.entropica.block.GravLiftProjectorBlock> GRAV_LIFT_PROJECTOR = BLOCKS.register("grav_lift_projector", name -> new ddraig.net.entropica.block.GravLiftProjectorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<ddraig.net.entropica.block.TidalPulseResonatorBlock> TIDAL_PULSE_RESONATOR = BLOCKS.register("tidal_pulse_resonator", name -> new ddraig.net.entropica.block.TidalPulseResonatorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<ddraig.net.entropica.block.GravityCenterBlock> GRAVITY_CENTER = BLOCKS.register("gravity_center", name -> new ddraig.net.entropica.block.GravityCenterBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<ddraig.net.entropica.block.CreativeGravityCenterBlock> CREATIVE_GRAVITY_CENTER = BLOCKS.register("creative_gravity_center", name -> new ddraig.net.entropica.block.CreativeGravityCenterBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(-1.0f).explosionResistance(3600000.0f).noOcclusion()));
    public static final RegistrySupplier<ddraig.net.entropica.block.GravitonBouncepadBlock> GRAVITON_BOUNCEPAD = BLOCKS.register("graviton_bouncepad", name -> new ddraig.net.entropica.block.GravitonBouncepadBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    // Readout Blocks
    public static final RegistrySupplier<MateriaReadoutBlock> VIS_READOUT = BLOCKS.register("materia_readout", name -> new MateriaReadoutBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<EssenceReadoutBlock> ESSENCE_READOUT = BLOCKS.register("raw_materia_readout", name -> new EssenceReadoutBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));

    // --- Extraction Multiblock & Machines ---
    public static final RegistrySupplier<MateriaSimpleMachineBlock> MATERIA_SIMPLE_MACHINE_BLOCK = BLOCKS.register("materia_simple_machine_block", name -> new MateriaSimpleMachineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<MateriaComplexMachineBlock> MATERIA_COMPLEX_MACHINE_BLOCK = BLOCKS.register("materia_complex_machine_block", name -> new MateriaComplexMachineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops()));

    // --- Flora & Wood ---
    public static final RegistrySupplier<Block> RUBBER_LOG = BLOCKS.register("rubber_log", name -> new ddraig.net.entropica.block.RubberLogBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD).randomTicks()));
    public static final RegistrySupplier<Block> RUBBER_WOOD = BLOCKS.register("rubber_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_RUBBER_LOG = BLOCKS.register("stripped_rubber_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_RUBBER_WOOD = BLOCKS.register("stripped_rubber_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_PLANKS = BLOCKS.register("rubber_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_LEAVES = BLOCKS.register("rubber_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> RUBBER_SAPLING = BLOCKS.register("rubber_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> SHIMMERPETAL = BLOCKS.register("shimmerpetal", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 10).noOcclusion()));

    
    // --- Astral-Veil Willow Set ---
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_LOG = BLOCKS.register("astral_veil_willow_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD).lightLevel(state -> 3)));
    public static final RegistrySupplier<Block> STRIPPED_ASTRAL_VEIL_WILLOW_LOG = BLOCKS.register("stripped_astral_veil_willow_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD).lightLevel(state -> 4)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_PLANKS = BLOCKS.register("astral_veil_willow_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_LEAVES = BLOCKS.register("astral_veil_willow_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_SAPLING = BLOCKS.register("astral_veil_willow_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_VINES = BLOCKS.register("astral_veil_willow_vines", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).noCollision().instabreak().sound(SoundType.VINE).noOcclusion()));

    // --- Void-Blight Mangrove Set ---
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_LOG = BLOCKS.register("void_blight_mangrove_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_VOID_BLIGHT_MANGROVE_LOG = BLOCKS.register("stripped_void_blight_mangrove_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_PLANKS = BLOCKS.register("void_blight_mangrove_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_LEAVES = BLOCKS.register("void_blight_mangrove_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_ROOT = BLOCKS.register("void_blight_mangrove_root", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(0.7f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_SAPLING = BLOCKS.register("void_blight_mangrove_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));

    // --- Silver Pine Wood Set & Rimebloom ---
    public static final RegistrySupplier<Block> SILVER_PINE_LOG = BLOCKS.register("silver_pine_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_WOOD = BLOCKS.register("silver_pine_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_SILVER_PINE_LOG = BLOCKS.register("stripped_silver_pine_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_SILVER_PINE_WOOD = BLOCKS.register("stripped_silver_pine_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_PLANKS = BLOCKS.register("silver_pine_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_LEAVES = BLOCKS.register("silver_pine_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> SILVER_PINE_SAPLING = BLOCKS.register("silver_pine_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));

    // --- Amber-Wood Set ---
    public static final RegistrySupplier<Block> AMBER_LOG = BLOCKS.register("amber_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_WOOD = BLOCKS.register("amber_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_AMBER_LOG = BLOCKS.register("stripped_amber_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_AMBER_WOOD = BLOCKS.register("stripped_amber_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_PLANKS = BLOCKS.register("amber_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_LEAVES = BLOCKS.register("amber_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> AMBER_SAPLING = BLOCKS.register("amber_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));

        public static final RegistrySupplier<Block> AEGIS_ROSE = BLOCKS.register("aegis_rose", name -> new ddraig.net.entropica.block.EntropicaFlowerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLUE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 6).noOcclusion()));
    
    public static final RegistrySupplier<Block> SOUL_FLAME_ORCHID = BLOCKS.register("soul_flame_orchid", name -> new ddraig.net.entropica.block.SoulFlameOrchidBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 9).noOcclusion()));
    public static final RegistrySupplier<Block> AURORAL_BUTTERCUP = BLOCKS.register("auroral_buttercup", name -> new ddraig.net.entropica.block.AuroralButtercupBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 7).noOcclusion()));
    public static final RegistrySupplier<Block> STARDUST_BELL = BLOCKS.register("stardust_bell", name -> new ddraig.net.entropica.block.StardustBellBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLUE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 10).noOcclusion()));
    public static final RegistrySupplier<Block> FULGURITE_SWAMP_BLOOM = BLOCKS.register("fulgurite_swamp_bloom", name -> new ddraig.net.entropica.block.FulguriteSwampBloomBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> GALE_BLOOM_DANDELION = BLOCKS.register("gale_bloom_dandelion", name -> new ddraig.net.entropica.block.GaleBloomDandelionBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GRAY).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 4).noOcclusion()));
    public static final RegistrySupplier<Block> CRYO_STATIC_SHRUB = BLOCKS.register("cryo_static_shrub", name -> new ddraig.net.entropica.block.CryoStaticShrubBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 5).noOcclusion()));
    public static final RegistrySupplier<Block> VITREOUS_CACTUS = BLOCKS.register("vitreous_cactus", name -> new ddraig.net.entropica.block.VitreousCactusBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GREEN).noCollision().instabreak().sound(SoundType.WOOL).randomTicks().lightLevel(state -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> BARROW_MOSS = BLOCKS.register("barrow_moss", name -> new ddraig.net.entropica.block.BarrowMossBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.EMERALD).sound(SoundType.MOSS).lightLevel(state -> 7)));
    public static final RegistrySupplier<Block> BARROW_MOSS_CARPET = BLOCKS.register("barrow_moss_carpet", name -> new ddraig.net.entropica.block.BarrowMossCarpetBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.EMERALD).noCollision().instabreak().sound(SoundType.MOSS_CARPET).lightLevel(state -> 7).noOcclusion()));
    public static final RegistrySupplier<Block> ABYSSAL_WEEPROOT = BLOCKS.register("abyssal_weeproot", name -> new ddraig.net.entropica.block.AbyssalWeeprootBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLUE).noCollision().randomTicks().instabreak().sound(SoundType.WEEPING_VINES).lightLevel(state -> 5).noOcclusion()));
    public static final RegistrySupplier<Block> ABYSSAL_WEEPROOT_PLANT = BLOCKS.register("abyssal_weeproot_plant", name -> new ddraig.net.entropica.block.AbyssalWeeprootPlantBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLUE).noCollision().instabreak().sound(SoundType.WEEPING_VINES).lightLevel(state -> 5).noOcclusion()));
    public static final RegistrySupplier<Block> BLOOD_ROOT_SUCCULENT = BLOCKS.register("blood_root_succulent", name -> new ddraig.net.entropica.block.BloodRootSucculentBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> VITAE_ORCHID = BLOCKS.register("vitae_orchid", name -> new ddraig.net.entropica.block.VitaeOrchidBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> SPORE_BURST_PUFFBALL = BLOCKS.register("spore_burst_puffball", name -> new ddraig.net.entropica.block.SporeBurstPuffballBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GREEN).noCollision().instabreak().sound(SoundType.SLIME_BLOCK).noOcclusion()));
    public static final RegistrySupplier<Block> FULGURITE_REED = BLOCKS.register("fulgurite_reed", name -> new ddraig.net.entropica.block.FulguriteReedBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).lightLevel(state -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> GALE_THISTLE = BLOCKS.register("gale_thistle", name -> new ddraig.net.entropica.block.GaleThistleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GRAY).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> MIST_VEIL_MARSHMALLOW = BLOCKS.register("mist_veil_marshmallow", name -> new ddraig.net.entropica.block.MistVeilMarshmallowBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.WOOL).noOcclusion()));
    public static final RegistrySupplier<Block> AEGIS_SPIRE_ORCHID = BLOCKS.register("aegis_spire_orchid", name -> new ddraig.net.entropica.block.EntropicaFlowerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLUE).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).lightLevel(state -> 7).noOcclusion()));
    public static final RegistrySupplier<Block> TALL_AEGIS_SPIRE_ORCHID = BLOCKS.register("tall_aegis_spire_orchid", name -> new ddraig.net.entropica.block.AegisSpireOrchidBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLUE).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).lightLevel(state -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> PYRE_SPROUT = BLOCKS.register("pyre_sprout", name -> new ddraig.net.entropica.block.PyreSproutBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 10).noOcclusion()));
    public static final RegistrySupplier<Block> AURA_DRIFT_SEDGE = BLOCKS.register("aura_drift_sedge", name -> new ddraig.net.entropica.block.AuraDriftSedgeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PINK).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> SPECTRAL_LANTERN_FLOWER = BLOCKS.register("spectral_lantern_flower", name -> new ddraig.net.entropica.block.SpectralLanternFlowerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 12).noOcclusion()));
    public static final RegistrySupplier<Block> CINDER_SPORE_MUSHROOM = BLOCKS.register("cinder_spore_mushroom", name -> new ddraig.net.entropica.block.CinderSporeMushroomBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.EMERALD).noCollision().instabreak().sound(SoundType.FUNGUS).lightLevel(state -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> NECROTIC_ROSE_OF_JERICHO = BLOCKS.register("necrotic_rose_of_jericho", name -> new ddraig.net.entropica.block.NecroticRoseOfJerichoBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> VOID_STALKER_ORCHID = BLOCKS.register("void_stalker_orchid", name -> new ddraig.net.entropica.block.VoidStalkerOrchidBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> STARDUST_ALOE = BLOCKS.register("stardust_aloe", name -> new ddraig.net.entropica.block.StardustAloeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> SANGUINE_LILY = BLOCKS.register("sanguine_lily", name -> new ddraig.net.entropica.block.SanguineLilyBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).sound(SoundType.LILY_PAD).lightLevel(state -> 5).noOcclusion()));
    public static final RegistrySupplier<Block> TALL_SOUL_FLAME_ORCHID = BLOCKS.register("tall_soul_flame_orchid", name -> new net.minecraft.world.level.block.TallFlowerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 10).noOcclusion()));
    public static final RegistrySupplier<Block> TALL_VITAE_ORCHID = BLOCKS.register("tall_vitae_orchid", name -> new net.minecraft.world.level.block.TallFlowerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 10).noOcclusion()));
    public static final RegistrySupplier<Block> TALL_VOID_STALKER_ORCHID = BLOCKS.register("tall_void_stalker_orchid", name -> new net.minecraft.world.level.block.TallFlowerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> TALL_AEGIS_ROSE = BLOCKS.register("tall_aegis_rose", name -> new net.minecraft.world.level.block.TallFlowerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PINK).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> AURORAL_LILY_PAD = BLOCKS.register("auroral_lily_pad", name -> new ddraig.net.entropica.block.AuroralLilyPadBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GREEN).sound(SoundType.LILY_PAD).instabreak().lightLevel(state -> 11).randomTicks().noOcclusion()));
    public static final RegistrySupplier<Block> TALL_NECROTIC_ROSE_OF_JERICHO = BLOCKS.register("tall_necrotic_rose_of_jericho", name -> new net.minecraft.world.level.block.TallFlowerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> CINDER_GRIP_LICHEN = BLOCKS.register("cinder_grip_lichen", name -> new ddraig.net.entropica.block.CinderGripLichenBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noCollision().instabreak().sound(SoundType.GLOW_LICHEN).lightLevel(net.minecraft.world.level.block.GlowLichenBlock.emission(7)).noOcclusion()));
    public static final RegistrySupplier<Block> SOOT_SHROUD_FUNGI = BLOCKS.register("soot_shroud_fungi", name -> new ddraig.net.entropica.block.SootShroudFungiBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).noCollision().instabreak().sound(SoundType.FUNGUS).lightLevel(state -> 8).noOcclusion()));
        public static final RegistrySupplier<Block> STATIC_SPEAR_GRASS = BLOCKS.register("static_spear_grass", name -> new ddraig.net.entropica.block.StaticSpearGrassBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).sound(SoundType.GRASS).noCollision().instabreak().lightLevel(s -> 3).noOcclusion()));
    public static final RegistrySupplier<Block> AMBER_NECTAR_BLOSSOM = BLOCKS.register("amber_nectar_blossom", name -> new ddraig.net.entropica.block.AmberNectarBlossomBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).sound(SoundType.GRASS).noCollision().instabreak().lightLevel(s -> 5).noOcclusion()));
    public static final RegistrySupplier<Block> RIMEBLOOM = BLOCKS.register("rimebloom", name -> new ddraig.net.entropica.block.RimebloomBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.GRASS).noCollision().instabreak().lightLevel(s -> 3).noOcclusion()));
    public static final RegistrySupplier<Block> POTTED_AURA_DRIFT_SEDGE = BLOCKS.register("potted_aura_drift_sedge", name -> new FlowerPotBlock(ModBlocks.AURA_DRIFT_SEDGE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_SPECTRAL_LANTERN_FLOWER = BLOCKS.register("potted_spectral_lantern_flower", name -> new FlowerPotBlock(ModBlocks.SPECTRAL_LANTERN_FLOWER.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_CINDER_SPORE_MUSHROOM = BLOCKS.register("potted_cinder_spore_mushroom", name -> new FlowerPotBlock(ModBlocks.CINDER_SPORE_MUSHROOM.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_NECROTIC_ROSE_OF_JERICHO = BLOCKS.register("potted_necrotic_rose_of_jericho", name -> new FlowerPotBlock(ModBlocks.NECROTIC_ROSE_OF_JERICHO.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_VOID_STALKER_ORCHID = BLOCKS.register("potted_void_stalker_orchid", name -> new FlowerPotBlock(ModBlocks.VOID_STALKER_ORCHID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_STARDUST_ALOE = BLOCKS.register("potted_stardust_aloe", name -> new FlowerPotBlock(ModBlocks.STARDUST_ALOE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_BLOOD_ROOT_SUCCULENT = BLOCKS.register("potted_blood_root_succulent", name -> new FlowerPotBlock(ModBlocks.BLOOD_ROOT_SUCCULENT.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_AEGIS_SPIRE_ORCHID = BLOCKS.register("potted_aegis_spire_orchid", name -> new FlowerPotBlock(ModBlocks.AEGIS_SPIRE_ORCHID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_SPORE_BURST_PUFFBALL = BLOCKS.register("potted_spore_burst_puffball", name -> new FlowerPotBlock(ModBlocks.SPORE_BURST_PUFFBALL.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_VITAE_ORCHID = BLOCKS.register("potted_vitae_orchid", name -> new FlowerPotBlock(ModBlocks.VITAE_ORCHID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_SANGUINE_LILY = BLOCKS.register("potted_sanguine_lily", name -> new FlowerPotBlock(ModBlocks.SANGUINE_LILY.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_ABYSSAL_WEEPROOT = BLOCKS.register("potted_abyssal_weeproot", name -> new FlowerPotBlock(ModBlocks.ABYSSAL_WEEPROOT.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_FULGURITE_SWAMP_BLOOM = BLOCKS.register("potted_fulgurite_swamp_bloom", name -> new FlowerPotBlock(ModBlocks.FULGURITE_SWAMP_BLOOM.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_CRYO_STATIC_SHRUB = BLOCKS.register("potted_cryo_static_shrub", name -> new FlowerPotBlock(ModBlocks.CRYO_STATIC_SHRUB.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_VITREOUS_CACTUS = BLOCKS.register("potted_vitreous_cactus", name -> new FlowerPotBlock(ModBlocks.VITREOUS_CACTUS.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_SOOT_SHROUD_FUNGI = BLOCKS.register("potted_soot_shroud_fungi", name -> new FlowerPotBlock(ModBlocks.SOOT_SHROUD_FUNGI.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_STATIC_SPEAR_GRASS = BLOCKS.register("potted_static_spear_grass", name -> new FlowerPotBlock(ModBlocks.STATIC_SPEAR_GRASS.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_AMBER_NECTAR_BLOSSOM = BLOCKS.register("potted_amber_nectar_blossom", name -> new FlowerPotBlock(ModBlocks.AMBER_NECTAR_BLOSSOM.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_RIMEBLOOM = BLOCKS.register("potted_rimebloom", name -> new FlowerPotBlock(ModBlocks.RIMEBLOOM.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));

    
    public static final RegistrySupplier<Block> SPORE_CANNON_PUFFBALL = BLOCKS.register("spore_cannon_puffball", name -> new ddraig.net.entropica.block.SporeCannonPuffballBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).sound(SoundType.GRASS).noCollision().instabreak().lightLevel(s -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> PYRE_THORN_LAUNCHER = BLOCKS.register("pyre_thorn_launcher", name -> new ddraig.net.entropica.block.PyreThornLauncherBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).sound(SoundType.GRASS).noCollision().instabreak().lightLevel(s -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> SPORE_BEARING_PITCHER_PLUMP = BLOCKS.register("spore_bearing_pitcher_plump", name -> new ddraig.net.entropica.block.SporeBearingPitcherPlumpBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GREEN).sound(SoundType.GRASS).noCollision().instabreak().lightLevel(s -> 5).noOcclusion()));
    public static final RegistrySupplier<Block> BLOOD_TENDRIL_BRAMBLE = BLOCKS.register("blood_tendril_bramble", name -> new ddraig.net.entropica.block.BloodTendrilBrambleBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).sound(SoundType.GRASS).noCollision().instabreak().noOcclusion()));
    public static final RegistrySupplier<Block> SOOT_VEIL_BLIGHT_CAP = BLOCKS.register("soot_veil_blight_cap", name -> new ddraig.net.entropica.block.SootVeilBlightCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).sound(SoundType.FUNGUS).noCollision().instabreak().lightLevel(s -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> MAGMA_GRIP_TENDRILS = BLOCKS.register("magma_grip_tendrils", name -> new ddraig.net.entropica.block.MagmaGripTendrilsBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).sound(SoundType.GLOW_LICHEN).noCollision().instabreak().lightLevel(net.minecraft.world.level.block.GlowLichenBlock.emission(9)).noOcclusion()));

    public static final RegistrySupplier<Block> POTTED_SPORE_CANNON_PUFFBALL = BLOCKS.register("potted_spore_cannon_puffball", name -> new FlowerPotBlock(ModBlocks.SPORE_CANNON_PUFFBALL.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_PYRE_THORN_LAUNCHER = BLOCKS.register("potted_pyre_thorn_launcher", name -> new FlowerPotBlock(ModBlocks.PYRE_THORN_LAUNCHER.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_SPORE_BEARING_PITCHER_PLUMP = BLOCKS.register("potted_spore_bearing_pitcher_plump", name -> new FlowerPotBlock(ModBlocks.SPORE_BEARING_PITCHER_PLUMP.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_BLOOD_TENDRIL_BRAMBLE = BLOCKS.register("potted_blood_tendril_bramble", name -> new FlowerPotBlock(ModBlocks.BLOOD_TENDRIL_BRAMBLE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));
    public static final RegistrySupplier<Block> POTTED_SOOT_VEIL_BLIGHT_CAP = BLOCKS.register("potted_soot_veil_blight_cap", name -> new FlowerPotBlock(ModBlocks.SOOT_VEIL_BLIGHT_CAP.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(ResourceKey.create(Registries.BLOCK, name))));

    public static final RegistrySupplier<Block> BARROW_FUNGAL_SHELF_CAP = BLOCKS.register("barrow_fungal_shelf_cap", name -> new ddraig.net.entropica.block.BarrowFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GREEN).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 5).noOcclusion()));
    public static final RegistrySupplier<Block> SPORE_FUNGAL_SHELF_CAP = BLOCKS.register("spore_fungal_shelf_cap", name -> new ddraig.net.entropica.block.SporeFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GREEN).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> BLIGHT_FUNGAL_SHELF_CAP = BLOCKS.register("blight_fungal_shelf_cap", name -> new ddraig.net.entropica.block.BlightFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GREEN).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 4).noOcclusion()));
    public static final RegistrySupplier<Block> FROST_FUNGAL_SHELF_CAP = BLOCKS.register("frost_fungal_shelf_cap", name -> new ddraig.net.entropica.block.FrostFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 4).noOcclusion()));
    public static final RegistrySupplier<Block> CINDER_FUNGAL_SHELF_CAP = BLOCKS.register("cinder_fungal_shelf_cap", name -> new ddraig.net.entropica.block.CinderFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> ASTRAL_FUNGAL_SHELF_CAP = BLOCKS.register("astral_fungal_shelf_cap", name -> new ddraig.net.entropica.block.AstralFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 7).noOcclusion()));
    public static final RegistrySupplier<Block> DAWN_FUNGAL_SHELF_CAP = BLOCKS.register("dawn_fungal_shelf_cap", name -> new ddraig.net.entropica.block.DawnFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 12).noOcclusion()));
    public static final RegistrySupplier<Block> SANGUINE_FUNGAL_SHELF_CAP = BLOCKS.register("sanguine_fungal_shelf_cap", name -> new ddraig.net.entropica.block.SanguineFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> STATIC_FUNGAL_SHELF_CAP = BLOCKS.register("static_fungal_shelf_cap", name -> new ddraig.net.entropica.block.StaticFungalShelfCapBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).sound(SoundType.FUNGUS).instabreak().lightLevel(s -> 6).noOcclusion()));
    public static final RegistrySupplier<Block> PYROCYST_ALGAE = BLOCKS.register("pyrocyst_algae", name -> new ddraig.net.entropica.block.PyrocystAlgaeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).sound(SoundType.MOSS_CARPET).instabreak().randomTicks().lightLevel(state -> 11).noOcclusion()));
















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

    public static final RegistrySupplier<RitualBowlBlock> MARBLE_RITUAL_BOWL = BLOCKS.register("marble_ritual_bowl", name -> new RitualBowlBlock(RitualBowlBlock.BowlVariant.MARBLE, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));
    public static final RegistrySupplier<RitualBowlBlock> BASALT_RITUAL_BOWL = BLOCKS.register("basalt_ritual_bowl", name -> new RitualBowlBlock(RitualBowlBlock.BowlVariant.BASALT, BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT).setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));
    public static final RegistrySupplier<RitualBowlBlock> GRANITE_RITUAL_BOWL = BLOCKS.register("granite_ritual_bowl", name -> new RitualBowlBlock(RitualBowlBlock.BowlVariant.GRANITE, BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE).setId(ResourceKey.create(Registries.BLOCK, name)).noOcclusion()));

    // --- Runic Scribing & Research ---
    public static final RegistrySupplier<ddraig.net.entropica.block.ScribedChalkBlock> SCRIBED_CHALK =
            BLOCKS.register("scribed_chalk", name -> new ddraig.net.entropica.block.ScribedChalkBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).noCollision().noOcclusion().instabreak().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).lightLevel(state -> state.getValue(ddraig.net.entropica.block.ScribedChalkBlock.LIGHT))));
            
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

    // --- MATERIA BLESSING ---
    public static final RegistrySupplier<MateriaBlessingBlock> MATERIA_BLESSING =
            BLOCKS.register("materia_blessing", name -> new MateriaBlessingBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(3.0f).explosionResistance(6.0f).noOcclusion().sound(SoundType.AMETHYST).lightLevel(state -> 8)));

    public static final RegistrySupplier<ddraig.net.entropica.block.GreaterMateriaBlessingBlock> GREATER_MATERIA_BLESSING =
            BLOCKS.register("greater_materia_blessing", name -> new ddraig.net.entropica.block.GreaterMateriaBlessingBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(5.0f).explosionResistance(10.0f).noOcclusion().sound(SoundType.AMETHYST).lightLevel(state -> 12)));

    public static final List<RegistrySupplier<?>> SIMPLE_BLOCKS = List.of(
            ENTROPIC_ORE, VORPALITE_ORE, SORROWSTONE_ORE, UMBRALITE_ORE,
            AETERIUM_CRYSTAL_BLOCK, BUDDING_AETERIUM, SMALL_AETERIUM_BUD, MEDIUM_AETERIUM_BUD, LARGE_AETERIUM_BUD, AETERIUM_CLUSTER,
            IGNISITE_CRYSTAL_BLOCK, BUDDING_IGNISITE, SMALL_IGNISITE_BUD, MEDIUM_IGNISITE_BUD, LARGE_IGNISITE_BUD, IGNISITE_CLUSTER,
            MORTISITE_CRYSTAL_BLOCK, BUDDING_MORTISITE, SMALL_MORTISITE_BUD, MEDIUM_MORTISITE_BUD, LARGE_MORTISITE_BUD, MORTISITE_CLUSTER,
            ARCANE_BRICK, ARCANITE_PLATING, ARCANE_CLAY_BLOCK, MATERIA_PLUME,
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

    // --- 5 Wood Suite Expansion Blocks ---
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_WOOD = BLOCKS.register("astral_veil_willow_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_ASTRAL_VEIL_WILLOW_WOOD = BLOCKS.register("stripped_astral_veil_willow_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_STAIRS = BLOCKS.register("astral_veil_willow_stairs", name -> new StairBlock(ModBlocks.ASTRAL_VEIL_WILLOW_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_SLAB = BLOCKS.register("astral_veil_willow_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_VERTICAL_SLAB = BLOCKS.register("astral_veil_willow_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_FENCE = BLOCKS.register("astral_veil_willow_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_FENCE_GATE = BLOCKS.register("astral_veil_willow_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_BUTTON = BLOCKS.register("astral_veil_willow_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_PRESSURE_PLATE = BLOCKS.register("astral_veil_willow_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_WOOD = BLOCKS.register("void_blight_mangrove_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_VOID_BLIGHT_MANGROVE_WOOD = BLOCKS.register("stripped_void_blight_mangrove_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_STAIRS = BLOCKS.register("void_blight_mangrove_stairs", name -> new StairBlock(ModBlocks.VOID_BLIGHT_MANGROVE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_SLAB = BLOCKS.register("void_blight_mangrove_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_VERTICAL_SLAB = BLOCKS.register("void_blight_mangrove_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_FENCE = BLOCKS.register("void_blight_mangrove_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_FENCE_GATE = BLOCKS.register("void_blight_mangrove_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_BUTTON = BLOCKS.register("void_blight_mangrove_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_PRESSURE_PLATE = BLOCKS.register("void_blight_mangrove_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_STAIRS = BLOCKS.register("rubber_stairs", name -> new StairBlock(ModBlocks.RUBBER_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_SLAB = BLOCKS.register("rubber_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_VERTICAL_SLAB = BLOCKS.register("rubber_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_FENCE = BLOCKS.register("rubber_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_FENCE_GATE = BLOCKS.register("rubber_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_BUTTON = BLOCKS.register("rubber_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_PRESSURE_PLATE = BLOCKS.register("rubber_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_STAIRS = BLOCKS.register("silver_pine_stairs", name -> new StairBlock(ModBlocks.SILVER_PINE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_SLAB = BLOCKS.register("silver_pine_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_VERTICAL_SLAB = BLOCKS.register("silver_pine_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_FENCE = BLOCKS.register("silver_pine_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_FENCE_GATE = BLOCKS.register("silver_pine_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_BUTTON = BLOCKS.register("silver_pine_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_PRESSURE_PLATE = BLOCKS.register("silver_pine_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_STAIRS = BLOCKS.register("amber_stairs", name -> new StairBlock(ModBlocks.AMBER_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_SLAB = BLOCKS.register("amber_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_VERTICAL_SLAB = BLOCKS.register("amber_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_FENCE = BLOCKS.register("amber_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_FENCE_GATE = BLOCKS.register("amber_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_BUTTON = BLOCKS.register("amber_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_PRESSURE_PLATE = BLOCKS.register("amber_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));

    // --- 4 Tree Families Expansion (56 Blocks) ---
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_LOG = BLOCKS.register("pyre_ash_cedar_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_PYRE_ASH_CEDAR_LOG = BLOCKS.register("stripped_pyre_ash_cedar_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_WOOD = BLOCKS.register("pyre_ash_cedar_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_PYRE_ASH_CEDAR_WOOD = BLOCKS.register("stripped_pyre_ash_cedar_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_PLANKS = BLOCKS.register("pyre_ash_cedar_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_LEAVES = BLOCKS.register("pyre_ash_cedar_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_SAPLING = BLOCKS.register("pyre_ash_cedar_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_STAIRS = BLOCKS.register("pyre_ash_cedar_stairs", name -> new StairBlock(ModBlocks.PYRE_ASH_CEDAR_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_SLAB = BLOCKS.register("pyre_ash_cedar_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_VERTICAL_SLAB = BLOCKS.register("pyre_ash_cedar_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_FENCE = BLOCKS.register("pyre_ash_cedar_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_FENCE_GATE = BLOCKS.register("pyre_ash_cedar_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_BUTTON = BLOCKS.register("pyre_ash_cedar_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_PRESSURE_PLATE = BLOCKS.register("pyre_ash_cedar_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_LOG = BLOCKS.register("abyssal_spore_cypress_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_ABYSSAL_SPORE_CYPRESS_LOG = BLOCKS.register("stripped_abyssal_spore_cypress_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_WOOD = BLOCKS.register("abyssal_spore_cypress_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_ABYSSAL_SPORE_CYPRESS_WOOD = BLOCKS.register("stripped_abyssal_spore_cypress_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_PLANKS = BLOCKS.register("abyssal_spore_cypress_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_LEAVES = BLOCKS.register("abyssal_spore_cypress_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_SAPLING = BLOCKS.register("abyssal_spore_cypress_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_STAIRS = BLOCKS.register("abyssal_spore_cypress_stairs", name -> new StairBlock(ModBlocks.ABYSSAL_SPORE_CYPRESS_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_SLAB = BLOCKS.register("abyssal_spore_cypress_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_VERTICAL_SLAB = BLOCKS.register("abyssal_spore_cypress_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_FENCE = BLOCKS.register("abyssal_spore_cypress_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_FENCE_GATE = BLOCKS.register("abyssal_spore_cypress_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_BUTTON = BLOCKS.register("abyssal_spore_cypress_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_PRESSURE_PLATE = BLOCKS.register("abyssal_spore_cypress_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_LOG = BLOCKS.register("starlight_aether_birch_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_STARLIGHT_AETHER_BIRCH_LOG = BLOCKS.register("stripped_starlight_aether_birch_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_WOOD = BLOCKS.register("starlight_aether_birch_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_STARLIGHT_AETHER_BIRCH_WOOD = BLOCKS.register("stripped_starlight_aether_birch_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_PLANKS = BLOCKS.register("starlight_aether_birch_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_LEAVES = BLOCKS.register("starlight_aether_birch_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_SAPLING = BLOCKS.register("starlight_aether_birch_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_STAIRS = BLOCKS.register("starlight_aether_birch_stairs", name -> new StairBlock(ModBlocks.STARLIGHT_AETHER_BIRCH_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_SLAB = BLOCKS.register("starlight_aether_birch_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_VERTICAL_SLAB = BLOCKS.register("starlight_aether_birch_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_FENCE = BLOCKS.register("starlight_aether_birch_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_FENCE_GATE = BLOCKS.register("starlight_aether_birch_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_BUTTON = BLOCKS.register("starlight_aether_birch_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_PRESSURE_PLATE = BLOCKS.register("starlight_aether_birch_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_LOG = BLOCKS.register("blood_root_iron_oak_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_BLOOD_ROOT_IRON_OAK_LOG = BLOCKS.register("stripped_blood_root_iron_oak_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_WOOD = BLOCKS.register("blood_root_iron_oak_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_BLOOD_ROOT_IRON_OAK_WOOD = BLOCKS.register("stripped_blood_root_iron_oak_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_PLANKS = BLOCKS.register("blood_root_iron_oak_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_LEAVES = BLOCKS.register("blood_root_iron_oak_leaves", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(0.2f).sound(SoundType.GRASS).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_SAPLING = BLOCKS.register("blood_root_iron_oak_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_STAIRS = BLOCKS.register("blood_root_iron_oak_stairs", name -> new StairBlock(ModBlocks.BLOOD_ROOT_IRON_OAK_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_SLAB = BLOCKS.register("blood_root_iron_oak_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_VERTICAL_SLAB = BLOCKS.register("blood_root_iron_oak_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_FENCE = BLOCKS.register("blood_root_iron_oak_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_FENCE_GATE = BLOCKS.register("blood_root_iron_oak_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_BUTTON = BLOCKS.register("blood_root_iron_oak_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_PRESSURE_PLATE = BLOCKS.register("blood_root_iron_oak_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    // --- Materia-Echo Tree Suite (15 Blocks) ---
    public static final RegistrySupplier<Block> MATERIA_ECHO_LOG = BLOCKS.register("materia_echo_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_MATERIA_ECHO_LOG = BLOCKS.register("stripped_materia_echo_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_WOOD = BLOCKS.register("materia_echo_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STRIPPED_MATERIA_ECHO_WOOD = BLOCKS.register("stripped_materia_echo_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_PLANKS = BLOCKS.register("materia_echo_planks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_LEAVES = BLOCKS.register("materia_echo_leaves", name -> new ddraig.net.entropica.block.MateriaEchoLeavesBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).destroyTime(0.2f).sound(SoundType.GRASS).randomTicks().noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));

    public static final RegistrySupplier<Block> MATERIA_ECHO_SAPLING = BLOCKS.register("materia_echo_sapling", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<Block> MATERIA_ECHO_STAIRS = BLOCKS.register("materia_echo_stairs", name -> new StairBlock(ModBlocks.MATERIA_ECHO_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_SLAB = BLOCKS.register("materia_echo_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_VERTICAL_SLAB = BLOCKS.register("materia_echo_vertical_slab", name -> new ddraig.net.entropica.block.VerticalSlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_FENCE = BLOCKS.register("materia_echo_fence", name -> new FenceBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_FENCE_GATE = BLOCKS.register("materia_echo_fence_gate", name -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_BUTTON = BLOCKS.register("materia_echo_button", name -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_PRESSURE_PLATE = BLOCKS.register("materia_echo_pressure_plate", name -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).noCollision().destroyTime(0.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_FRUIT = BLOCKS.register("materia_echo_fruit", name -> new ddraig.net.entropica.block.MateriaEchoFruitBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).destroyTime(0.2f).sound(SoundType.SWEET_BERRY_BUSH).randomTicks().noOcclusion()));


    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_CHEST = BLOCKS.register("pyre_ash_cedar_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "pyre_ash_cedar_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "pyre_ash_cedar"));

    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_CHEST = BLOCKS.register("abyssal_spore_cypress_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "abyssal_spore_cypress_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "abyssal_spore_cypress"));

    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_CHEST = BLOCKS.register("starlight_aether_birch_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "starlight_aether_birch_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "starlight_aether_birch"));

    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_CHEST = BLOCKS.register("blood_root_iron_oak_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "blood_root_iron_oak_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "blood_root_iron_oak"));

    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_CHEST = BLOCKS.register("astral_veil_willow_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "astral_veil_willow_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "astral_veil_willow"));

    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_CHEST = BLOCKS.register("void_blight_mangrove_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "void_blight_mangrove_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "void_blight_mangrove"));

    public static final RegistrySupplier<Block> AMBER_CHEST = BLOCKS.register("amber_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "amber_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "amber"));

    public static final RegistrySupplier<Block> RUBBER_CHEST = BLOCKS.register("rubber_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "rubber_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "rubber"));

    public static final RegistrySupplier<Block> SILVER_PINE_CHEST = BLOCKS.register("silver_pine_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "silver_pine_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "silver_pine"));

    public static final RegistrySupplier<Block> MATERIA_ECHO_CHEST = BLOCKS.register("materia_echo_chest", loc ->
            new ddraig.net.entropica.block.ModChestBlock(() -> ModBlockEntities.MOD_CHEST.get(),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entropica", "materia_echo_chest")))
                            .mapColor(MapColor.WOOD)
                            .destroyTime(2.5f)
                            .sound(SoundType.WOOD),
                    "materia_echo"));

    // --- WOODEN BARRELS ---
    public static final RegistrySupplier<Block> PYRE_ASH_CEDAR_BARREL = BLOCKS.register("pyre_ash_cedar_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ABYSSAL_SPORE_CYPRESS_BARREL = BLOCKS.register("abyssal_spore_cypress_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STARLIGHT_AETHER_BIRCH_BARREL = BLOCKS.register("starlight_aether_birch_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BLOOD_ROOT_IRON_OAK_BARREL = BLOCKS.register("blood_root_iron_oak_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_RED).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ASTRAL_VEIL_WILLOW_BARREL = BLOCKS.register("astral_veil_willow_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> VOID_BLIGHT_MANGROVE_BARREL = BLOCKS.register("void_blight_mangrove_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_MAGENTA).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> AMBER_BARREL = BLOCKS.register("amber_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RUBBER_BARREL = BLOCKS.register("rubber_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SILVER_PINE_BARREL = BLOCKS.register("silver_pine_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MATERIA_ECHO_BARREL = BLOCKS.register("materia_echo_barrel", name -> new ddraig.net.entropica.block.ModBarrelBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f).sound(SoundType.WOOD)));


    // ==========================================
    // ASTRAL MATERIA & CELESTIAL SUITE
    // ==========================================
    // Architectural & Decorative
    public static final RegistrySupplier<Block> ASTRAL_MARBLE = BLOCKS.register("astral_marble", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> ASTRAL_MARBLE_BRICKS = BLOCKS.register("astral_marble_bricks", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<SlabBlock> ASTRAL_MARBLE_SLAB = BLOCKS.register("astral_marble_slab", name -> new SlabBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<StairBlock> ASTRAL_MARBLE_STAIRS = BLOCKS.register("astral_marble_stairs", name -> new StairBlock(ASTRAL_MARBLE.get().defaultBlockState(), BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<WallBlock> ASTRAL_MARBLE_WALL = BLOCKS.register("astral_marble_wall", name -> new WallBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> SOOTY_MARBLE = BLOCKS.register("sooty_marble", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> RUNED_ASTRAL_MARBLE = BLOCKS.register("runed_astral_marble", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).lightLevel(state -> 5).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> ENGRAVED_ASTRAL_SLATE = BLOCKS.register("engraved_astral_slate", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GRAY).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> CHISELED_ASTRAL_MARBLE = BLOCKS.register("chiseled_astral_marble", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<RotatedPillarBlock> STARLIGHT_PILLAR = BLOCKS.register("starlight_pillar", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<AstralMirrorBlock> ASTRAL_MIRROR_BLOCK = BLOCKS.register("astral_mirror_block", name -> new AstralMirrorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.GLASS)));

    // Extraterrestrial Host Stone & Crystals
    public static final RegistrySupplier<Block> ASTRAL_IMPACTITE = BLOCKS.register("astral_impactite", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(2.5f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> SMALL_ASTRAL_CRYSTAL_BUD = BLOCKS.register("small_astral_crystal_bud", name -> new ddraig.net.entropica.block.AstralCrystalClusterBlock(1, 6, 3, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 1).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> MEDIUM_ASTRAL_CRYSTAL_BUD = BLOCKS.register("medium_astral_crystal_bud", name -> new ddraig.net.entropica.block.AstralCrystalClusterBlock(2, 8, 2, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 2).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> LARGE_ASTRAL_CRYSTAL_BUD = BLOCKS.register("large_astral_crystal_bud", name -> new ddraig.net.entropica.block.AstralCrystalClusterBlock(3, 10, 1, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 4).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ASTRAL_CRYSTAL_CLUSTER = BLOCKS.register("astral_crystal_cluster", name -> new ddraig.net.entropica.block.AstralCrystalClusterBlock(4, 12, 1, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 5).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> BUDDING_ASTRAL_IMPACTITE = BLOCKS.register("budding_astral_impactite", name -> new BuddingCrystalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).randomTicks().destroyTime(2.0f).explosionResistance(2.0f).sound(SoundType.AMETHYST).requiresCorrectToolForDrops(), () -> SMALL_ASTRAL_CRYSTAL_BUD.get(), () -> MEDIUM_ASTRAL_CRYSTAL_BUD.get(), () -> LARGE_ASTRAL_CRYSTAL_BUD.get(), () -> ASTRAL_CRYSTAL_CLUSTER.get()));
    public static final RegistrySupplier<Block> ASTRAL_CRYSTAL_BLOCK = BLOCKS.register("astral_crystal_block", name -> new AmethystBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(1.5f).explosionResistance(1.5f).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));

    // Workstations, Altars & Devices
    public static final RegistrySupplier<Block> ASTRAL_ALTAR_CORE = BLOCKS.register("astral_altar_core", name -> new ddraig.net.entropica.block.AstralAltarCoreBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(3.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> RESONANCE_PYLON = BLOCKS.register("resonance_pylon", name -> new ddraig.net.entropica.block.ResonancePylonBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.5f).requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion()));
    public static final RegistrySupplier<Block> ASTRAL_PEDESTAL = BLOCKS.register("astral_pedestal", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> FOCAL_LENS_MOUNT = BLOCKS.register("focal_lens_mount", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final RegistrySupplier<Block> ASTRAL_INFUSION_PEDESTAL = BLOCKS.register("astral_infusion_pedestal", name -> new ddraig.net.entropica.block.AstralInfusionPedestalBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SNOW).destroyTime(2.5f).requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion()));
    public static final RegistrySupplier<Block> ASTRAL_COLLECTOR = BLOCKS.register("astral_collector", name -> new ddraig.net.entropica.block.AstralCollectorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> CELESTIAL_BEACON_CONTROLLER = BLOCKS.register("celestial_beacon_controller", name -> new ddraig.net.entropica.block.CelestialBeaconControllerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE).destroyTime(4.0f).requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion()));
    public static final RegistrySupplier<Block> DORMANT_CRYSTAL_RELIC = BLOCKS.register("dormant_crystal_relic", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_GRAY).destroyTime(3.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> STATIONARY_BRASS_TELESCOPE = BLOCKS.register("stationary_brass_telescope", name -> new ddraig.net.entropica.block.StationaryBrassTelescopeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> CELESTIAL_ARMILLARY_CONTROLLER = BLOCKS.register("celestial_armillary_controller", name -> new ddraig.net.entropica.block.CelestialArmillaryControllerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.5f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> STONE_HOPPER_BASIN = BLOCKS.register("stone_hopper_basin", name -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    // Optical Conduits & Logistics
    public static final RegistrySupplier<Block> REFRACTIVE_ASTRAL_LENS = BLOCKS.register("refractive_astral_lens", name -> new ddraig.net.entropica.block.RefractiveAstralLensBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().sound(SoundType.GLASS).noOcclusion()));
    public static final RegistrySupplier<Block> SECONDARY_ASTRAL_LENS = BLOCKS.register("secondary_astral_lens", name -> new ddraig.net.entropica.block.SecondaryAstralLensBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().sound(SoundType.GLASS).noOcclusion()));
    public static final RegistrySupplier<Block> BEAM_SPLITTER_PRISM = BLOCKS.register("beam_splitter_prism", name -> new ddraig.net.entropica.block.BeamSplitterPrismBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(1.5f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).noOcclusion()));
    public static final RegistrySupplier<Block> PURE_OPTIC_FIBER = BLOCKS.register("pure_optic_fiber", name -> new ddraig.net.entropica.block.PureOpticFiberBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLACK).destroyTime(1.0f).requiresCorrectToolForDrops().sound(SoundType.GLASS).noOcclusion()));
    public static final RegistrySupplier<Block> OPTICAL_TRANSMITTER_PORT = BLOCKS.register("optical_transmitter_port", name -> new ddraig.net.entropica.block.OpticalTransmitterPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final RegistrySupplier<Block> OPTICAL_RECEIVER_PORT = BLOCKS.register("optical_receiver_port", name -> new ddraig.net.entropica.block.OpticalReceiverPortBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final RegistrySupplier<Block> OPTICAL_BOOSTER_AMPLIFIER = BLOCKS.register("optical_booster_amplifier", name -> new ddraig.net.entropica.block.OpticalBoosterAmplifierBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.5f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final RegistrySupplier<Block> MATERIA_FLUX_DISTRIBUTOR = BLOCKS.register("materia_flux_distributor", name -> new ddraig.net.entropica.block.MateriaFluxDistributorBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(3.0f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> OPTIC_RECEIVER = BLOCKS.register("optic_receiver", name -> new ddraig.net.entropica.block.OpticReceiverBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> OPTIC_TRANSMITTER = BLOCKS.register("optic_transmitter", name -> new ddraig.net.entropica.block.OpticTransmitterBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.5f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> CAGED_OPTIC_BULB = BLOCKS.register("caged_optic_bulb", name -> new ddraig.net.entropica.block.CagedOpticBulbBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(1.0f).sound(SoundType.LANTERN).lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 15 : 0).noOcclusion()));
    public static final RegistrySupplier<Block> IRON_LAMP_POST = BLOCKS.register("iron_lamp_post", name -> new ddraig.net.entropica.block.LampPostBlock(ddraig.net.entropica.block.LampPostBlock.MaterialVariant.IRON, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> BRASS_LAMP_POST = BLOCKS.register("brass_lamp_post", name -> new ddraig.net.entropica.block.LampPostBlock(ddraig.net.entropica.block.LampPostBlock.MaterialVariant.BRASS, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> STEEL_LAMP_POST = BLOCKS.register("steel_lamp_post", name -> new ddraig.net.entropica.block.LampPostBlock(ddraig.net.entropica.block.LampPostBlock.MaterialVariant.STEEL, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.5f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final RegistrySupplier<Block> GLASS_LAMP_POST = BLOCKS.register("glass_lamp_post", name -> new ddraig.net.entropica.block.LampPostBlock(ddraig.net.entropica.block.LampPostBlock.MaterialVariant.GLASS, BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_CYAN).destroyTime(1.0f).requiresCorrectToolForDrops().sound(SoundType.GLASS).noOcclusion()));

    public static final RegistrySupplier<Block> RESONANCE_STARLIGHT_FOUNTAIN = BLOCKS.register("resonance_starlight_fountain", name -> new ddraig.net.entropica.block.ResonanceStarlightFountainBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.QUARTZ).destroyTime(2.5f).requiresCorrectToolForDrops().sound(SoundType.STONE).lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 14 : 0).noOcclusion()));
    public static final RegistrySupplier<Block> LUMINOUS_TRELLIS_ARBOR = BLOCKS.register("luminous_trellis_arbor", name -> new ddraig.net.entropica.block.LuminousTrellisArborBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.METAL).destroyTime(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 14 : 0).noOcclusion()));
    public static final RegistrySupplier<Block> ATTUNED_DISPLAY_VITRINE = BLOCKS.register("attuned_display_vitrine", name -> new ddraig.net.entropica.block.AttunedDisplayVitrineBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.QUARTZ).destroyTime(1.5f).requiresCorrectToolForDrops().sound(SoundType.GLASS).lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 14 : 0).noOcclusion()));
    public static final RegistrySupplier<Block> CELESTIAL_GRANDFATHER_CLOCK = BLOCKS.register("celestial_grandfather_clock", name -> new ddraig.net.entropica.block.CelestialGrandfatherClockBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.5f).sound(SoundType.WOOD).lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 14 : 0).noOcclusion()));
    public static final RegistrySupplier<Block> ILLUMINATED_BALUSTRADE = BLOCKS.register("illuminated_balustrade", name -> new ddraig.net.entropica.block.IlluminatedBalustradeBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.STONE).destroyTime(1.5f).requiresCorrectToolForDrops().sound(SoundType.STONE).lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 14 : 0).noOcclusion()));
    public static final RegistrySupplier<Block> STARLIGHT_CARPET = BLOCKS.register("starlight_carpet", name -> new ddraig.net.entropica.block.StarlightCarpetBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_BLUE).destroyTime(0.1f).sound(SoundType.WOOL).friction(0.985f).speedFactor(1.35f).lightLevel(state -> 8).noOcclusion()));
    public static final RegistrySupplier<Block> GLOWPEG = BLOCKS.register("glowpeg", name -> new ddraig.net.entropica.block.GlowpegBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(0.3f).sound(SoundType.GLASS).lightLevel(state -> 15).noOcclusion()));
    public static final RegistrySupplier<Block> SOUL_GLOWPEG = BLOCKS.register("soul_glowpeg", name -> new ddraig.net.entropica.block.GlowpegBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(0.3f).sound(SoundType.GLASS).lightLevel(state -> 10).noOcclusion(), true));

    // ==========================================
    // SPELLCRAFTING WORKSTATIONS & AUTOCAST PEDESTALS
    // ==========================================
    public static final RegistrySupplier<Block> ARCANE_SCRIBING_TABLE = BLOCKS.register("arcane_scribing_table",
            name -> new ddraig.net.entropica.block.ArcaneScribingTableBlock(
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.WOOD).destroyTime(2.5f).sound(SoundType.WOOD).lightLevel(s -> 4).noOcclusion()));

    public static final RegistrySupplier<Block> RUNIC_ANVIL = BLOCKS.register("runic_anvil",
            name -> new ddraig.net.entropica.block.RunicAnvilBlock(
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.METAL).destroyTime(4.0f).explosionResistance(1200.0f)
                            .requiresCorrectToolForDrops().sound(SoundType.ANVIL).lightLevel(s -> 3).noOcclusion()));

    public static final RegistrySupplier<Block> AUTOCAST_PEDESTAL_STONE = BLOCKS.register("autocast_pedestal_stone",
            name -> new ddraig.net.entropica.block.AutocastPedestalBlock(
                    ddraig.net.entropica.block.AutocastPedestalBlock.PedestalVariant.STONE,
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.STONE).destroyTime(2.5f).requiresCorrectToolForDrops()
                            .sound(SoundType.STONE).lightLevel(s -> 5).noOcclusion()));

    public static final RegistrySupplier<Block> AUTOCAST_PEDESTAL_MARBLE = BLOCKS.register("autocast_pedestal_marble",
            name -> new ddraig.net.entropica.block.AutocastPedestalBlock(
                    ddraig.net.entropica.block.AutocastPedestalBlock.PedestalVariant.MARBLE,
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.SNOW).destroyTime(2.5f).requiresCorrectToolForDrops()
                            .sound(SoundType.STONE).lightLevel(s -> 5).noOcclusion()));

    public static final RegistrySupplier<Block> AUTOCAST_PEDESTAL_QUARTZ = BLOCKS.register("autocast_pedestal_quartz",
            name -> new ddraig.net.entropica.block.AutocastPedestalBlock(
                    ddraig.net.entropica.block.AutocastPedestalBlock.PedestalVariant.QUARTZ,
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.QUARTZ).destroyTime(2.5f).requiresCorrectToolForDrops()
                            .sound(SoundType.STONE).lightLevel(s -> 5).noOcclusion()));

    // ==========================================
    // BIOME 1: 2.1 BLUE-GLACIER SHELF & RUNIC SPIRE
    // ==========================================
    public static final RegistrySupplier<Block> AEGIS_CRYSTAL_BLOCK = BLOCKS.register("aegis_crystal_block",
            name -> new ddraig.net.entropica.block.biome1.AegisCrystalBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DIAMOND)
                    .randomTicks()
                    .destroyTime(3.0f)
                    .explosionResistance(12.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 6)));

    public static final RegistrySupplier<Block> AEGIS_CRYSTAL_BUD = BLOCKS.register("aegis_crystal_bud",
            name -> new ddraig.net.entropica.block.biome1.AegisCrystalBudBlock(7.0F, 3.0F, BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DIAMOND)
                    .forceSolidOn()
                    .noOcclusion()
                    .randomTicks()
                    .destroyTime(1.5f)
                    .explosionResistance(4.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(s -> 3)));

    public static final RegistrySupplier<Block> AEGIS_CRYSTAL_CLUSTER = BLOCKS.register("aegis_crystal_cluster",
            name -> new AmethystClusterBlock(12.0F, 1.5F, BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DIAMOND)
                    .forceSolidOn()
                    .noOcclusion()
                    .destroyTime(1.5f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(s -> 5)));

    public static final RegistrySupplier<Block> RUNIC_BLUE_ICE = BLOCKS.register("runic_blue_ice",
            name -> new net.minecraft.world.level.block.TransparentBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.989f)
                    .destroyTime(2.8f)
                    .sound(SoundType.GLASS)
                    .lightLevel(s -> 5)
                    .noOcclusion()));

    public static final RegistrySupplier<RotatedPillarBlock> RUNED_BASALT_PILLAR = BLOCKS.register("runed_basalt_pillar",
            name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BLACK)
                    .destroyTime(2.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.BASALT)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome1.GlacialHeartRootBlock> GLACIAL_HEART_ROOT = BLOCKS.register("glacial_heart_root",
            name -> new ddraig.net.entropica.block.biome1.GlacialHeartRootBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .randomTicks()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.HANGING_ROOTS)
                    .noOcclusion()
                    .lightLevel(s -> 0)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome1.ShieldingBrambleBlock> SHIELDING_BRAMBLE = BLOCKS.register("shielding_bramble",
            name -> new ddraig.net.entropica.block.biome1.ShieldingBrambleBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.METAL)
                    .destroyTime(2.0f)
                    .explosionResistance(24.0f)
                    .noCollision()
                    .noOcclusion()
                    .sound(SoundType.VINE)
                    .lightLevel(s -> 1)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome1.ThornedWireBlock> THORNED_WIRE = BLOCKS.register("thorned_wire",
            name -> new ddraig.net.entropica.block.biome1.ThornedWireBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.METAL)
                    .destroyTime(1.5f)
                    .explosionResistance(18.0f)
                    .noCollision()
                    .noOcclusion()
                    .sound(SoundType.CHAIN)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome1.GlacialBarricadeBlock> GLACIAL_BARRICADE = BLOCKS.register("glacial_barricade",
            name -> new ddraig.net.entropica.block.biome1.GlacialBarricadeBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .destroyTime(2.5f)
                    .explosionResistance(15.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome1.ConductiveAegisPlatingBlock> CONDUCTIVE_AEGIS_PLATING = BLOCKS.register("conductive_aegis_plating",
            name -> new ddraig.net.entropica.block.biome1.ConductiveAegisPlatingBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_CYAN)
                    .destroyTime(3.0f)
                    .explosionResistance(12.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome1.ConductiveAegisPlatingBlock.POWERED) ? 9 : 0)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome1.GlacialIceMakerBlock> GLACIAL_ICE_MAKER = BLOCKS.register("glacial_ice_maker",
            name -> new ddraig.net.entropica.block.biome1.GlacialIceMakerBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .randomTicks()
                    .destroyTime(3.0f)
                    .explosionResistance(10.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .lightLevel(s -> 3)));

    public static final RegistrySupplier<Block> AUTOCAST_PEDESTAL_ASTRAL_SLATE = BLOCKS.register("autocast_pedestal_astral_slate",
            name -> new ddraig.net.entropica.block.AutocastPedestalBlock(
                    ddraig.net.entropica.block.AutocastPedestalBlock.PedestalVariant.ASTRAL_SLATE,
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.COLOR_GRAY).destroyTime(2.5f).requiresCorrectToolForDrops()
                            .sound(SoundType.STONE).lightLevel(s -> 5).noOcclusion()));

    // ==========================================
    // BIOME 2: 2.2 AURORAL SKERRIES & AURORAL WELLSPRING
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.GlimmerIceBlock> GLIMMER_ICE = BLOCKS.register("glimmer_ice",
            name -> new ddraig.net.entropica.block.biome2.GlimmerIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.98f)
                    .destroyTime(0.5f)
                    .sound(SoundType.GLASS)
                    .lightLevel(s -> 10)
                    .noOcclusion()));

    public static final RegistrySupplier<net.minecraft.world.level.block.ColoredFallingBlock> COARSE_GRAVEL = BLOCKS.register("coarse_gravel",
            name -> new net.minecraft.world.level.block.ColoredFallingBlock(
                    new net.minecraft.util.ColorRGBA(0x23262B),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.COLOR_BLACK)
                            .destroyTime(0.6f)
                            .sound(SoundType.GRAVEL)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.AuroralFeatherMossBlock> AURORAL_FEATHER_MOSS = BLOCKS.register("auroral_feather_moss",
            name -> new ddraig.net.entropica.block.biome2.AuroralFeatherMossBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.EMERALD)
                    .destroyTime(0.1f)
                    .sound(SoundType.MOSS_CARPET)
                    .lightLevel(s -> 9)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.MistVeilFernBlock> MIST_VEIL_FERN = BLOCKS.register("mist_veil_fern",
            name -> new ddraig.net.entropica.block.biome2.MistVeilFernBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .noOcclusion()
                    .sound(SoundType.GRASS)
                    .lightLevel(s -> 4)));

    public static final RegistrySupplier<Block> POTTED_MIST_VEIL_FERN = BLOCKS.register("potted_mist_veil_fern",
            name -> new FlowerPotBlock(ModBlocks.MIST_VEIL_FERN.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)
                            .setId(ResourceKey.create(Registries.BLOCK, name))
                            .lightLevel(s -> 4)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.AuroralLanternBlock> AURORAL_LANTERN = BLOCKS.register("auroral_lantern",
            name -> new ddraig.net.entropica.block.biome2.AuroralLanternBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.METAL)
                    .lightLevel(s -> 15)
                    .destroyTime(2.5f)
                    .sound(SoundType.LANTERN)
                    .randomTicks()
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.ThermalHeatLampBlock> THERMAL_HEAT_LAMP = BLOCKS.register("thermal_heat_lamp",
            name -> new ddraig.net.entropica.block.biome2.ThermalHeatLampBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_ORANGE)
                    .lightLevel(s -> 14)
                    .destroyTime(2.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
                    .randomTicks()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.GlowGelPathingBlock> GLOW_GEL_PATHING = BLOCKS.register("glow_gel_pathing",
            name -> new ddraig.net.entropica.block.biome2.GlowGelPathingBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_CYAN)
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome2.GlowGelPathingBlock.LIT) ? 13 : 6)
                    .destroyTime(0.8f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.StaticRedstoneCouplerBlock> STATIC_REDSTONE_COUPLER = BLOCKS.register("static_redstone_coupler",
            name -> new ddraig.net.entropica.block.biome2.StaticRedstoneCouplerBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_ORANGE)
                    .destroyTime(1.5f)
                    .sound(SoundType.COPPER)
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome2.StaticRedstoneCouplerBlock.POWERED) ? 10 : 2)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.AuroralLensBlock> AURORAL_LENS_BLOCK = BLOCKS.register("auroral_lens_block",
            name -> new ddraig.net.entropica.block.biome2.AuroralLensBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DIAMOND)
                    .lightLevel(s -> 12)
                    .destroyTime(3.0f)
                    .explosionResistance(12.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome2.AuroralMoteLightBlock> AURORAL_MOTE_LIGHT = BLOCKS.register("auroral_mote_light",
            name -> new ddraig.net.entropica.block.biome2.AuroralMoteLightBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_CYAN)
                    .lightLevel(s -> 14)
                    .noCollision()
                    .replaceable()
                    .noOcclusion()
                    .instabreak()
                    .sound(SoundType.AMETHYST)));

    // ==========================================
    // BIOME 3: 2.3 FROST-BITE FLOES
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome3.RottenIceBlock> ROTTEN_ICE = BLOCKS.register("rotten_ice",
            name -> new ddraig.net.entropica.block.biome3.RottenIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .friction(0.98f)
                    .destroyTime(0.35f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome3.BlightedPackedIceBlock> BLIGHTED_PACKED_ICE = BLOCKS.register("blighted_packed_ice",
            name -> new ddraig.net.entropica.block.biome3.BlightedPackedIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_GREEN)
                    .friction(0.98f)
                    .destroyTime(0.6f)
                    .sound(SoundType.GLASS)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome3.NecroticSlimePatchBlock> NECROTIC_SLIME_PATCH = BLOCKS.register("necrotic_slime_patch",
            name -> new ddraig.net.entropica.block.biome3.NecroticSlimePatchBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_GREEN)
                    .noCollision()
                    .noOcclusion()
                    .destroyTime(0.2f)
                    .sound(SoundType.SLIME_BLOCK)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome3.NecroticSlimeBlock> NECROTIC_SLIME_BLOCK = BLOCKS.register("necrotic_slime_block",
            name -> new ddraig.net.entropica.block.biome3.NecroticSlimeBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_GREEN)
                    .friction(0.8f)
                    .destroyTime(0.5f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome3.FrozenSpruceBranchBlock> FROZEN_SPRUCE_BRANCH = BLOCKS.register("frozen_spruce_branch",
            name -> new ddraig.net.entropica.block.biome3.FrozenSpruceBranchBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.WOOD)
                    .destroyTime(1.2f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> CURED_FROST_HIDE_BLOCK = BLOCKS.register("cured_frost_hide_block",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_CYAN)
                    .destroyTime(1.2f)
                    .sound(SoundType.WOOL)));

    // ==========================================
    // BIOME 4: 2.4 CRYSTALLINE PACK-ICE & AEGIS SHIELDING MONOLITH
    // ==========================================
    public static final RegistrySupplier<Block> AEGIS_QUARTZ_BLOCK = BLOCKS.register("aegis_quartz_block",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.QUARTZ)
                    .destroyTime(1.5f)
                    .explosionResistance(8.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 6)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome4.AegisQuartzClusterBlock> AEGIS_QUARTZ_CLUSTER = BLOCKS.register("aegis_quartz_cluster",
            name -> new ddraig.net.entropica.block.biome4.AegisQuartzClusterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.QUARTZ)
                    .forceSolidOn()
                    .noOcclusion()
                    .destroyTime(1.5f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 7)));

    public static final RegistrySupplier<net.minecraft.world.level.block.DropExperienceBlock> BISMUTH_ORE = BLOCKS.register("bismuth_ore",
            name -> new net.minecraft.world.level.block.DropExperienceBlock(
                    net.minecraft.util.valueproviders.UniformInt.of(2, 5),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.STONE)
                            .destroyTime(3.0f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final RegistrySupplier<Block> BISMUTH_BLOCK = BLOCKS.register("bismuth_block",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .destroyTime(4.0f)
                    .explosionResistance(12.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final RegistrySupplier<Block> AEGIS_PLATED_STONE = BLOCKS.register("aegis_plated_stone",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BLACK)
                    .destroyTime(100.0f)
                    .explosionResistance(2400.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome4.BismuthKeyPedestalBlock> BISMUTH_KEY_PEDESTAL = BLOCKS.register("bismuth_key_pedestal",
            name -> new ddraig.net.entropica.block.biome4.BismuthKeyPedestalBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.STONE)
                    .noOcclusion()
                    .destroyTime(3.5f)
                    .explosionResistance(600.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome4.BismuthKeyPedestalBlock.UNLOCKED) ? 11 : 3)));

    // ==========================================
    // BIOME 5: 2.5 SILENT FJORD
    // ==========================================
    public static final RegistrySupplier<RotatedPillarBlock> BASALT_COLUMN = BLOCKS.register("basalt_column",
            name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BLACK)
                    .destroyTime(1.25f)
                    .explosionResistance(4.2f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.BASALT)));

    public static final RegistrySupplier<Block> CHISELED_ECLIPSE_BASALT = BLOCKS.register("chiseled_eclipse_basalt",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BLACK)
                    .destroyTime(1.5f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.BASALT)
                    .lightLevel(s -> 3)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome5.BlueFrozenMossBlock> BLUE_FROZEN_MOSS = BLOCKS.register("blue_frozen_moss",
            name -> new ddraig.net.entropica.block.biome5.BlueFrozenMossBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .destroyTime(0.2f)
                    .sound(SoundType.MOSS)
                    .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome5.BlueFrozenMossCarpetBlock> BLUE_FROZEN_MOSS_CARPET = BLOCKS.register("blue_frozen_moss_carpet",
            name -> new ddraig.net.entropica.block.biome5.BlueFrozenMossCarpetBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .destroyTime(0.1f)
                    .noOcclusion()
                    .sound(SoundType.MOSS_CARPET)
                    .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome5.ShadowedEclipseMossBlock> SHADOWED_ECLIPSE_MOSS = BLOCKS.register("shadowed_eclipse_moss",
            name -> new ddraig.net.entropica.block.biome5.ShadowedEclipseMossBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_PURPLE)
                    .randomTicks()
                    .destroyTime(0.15f)
                    .noOcclusion()
                    .sound(SoundType.MOSS_CARPET)));

    // ==========================================
    // BIOME 6: 2.6 STEAMING SKERRIES & VAPOROUS GROTTO
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome6.SteamingIceBlock> STEAMING_ICE = BLOCKS.register("steaming_ice",
            name -> new ddraig.net.entropica.block.biome6.SteamingIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.985f)
                    .destroyTime(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome6.SteamingSpringStoneBlock> STEAMING_SPRING_STONE = BLOCKS.register("steaming_spring_stone",
            name -> new ddraig.net.entropica.block.biome6.SteamingSpringStoneBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BLACK)
                    .destroyTime(1.5f)
                    .explosionResistance(6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.BASALT)
                    .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome6.VaporWeaverLichenBlock> VAPOR_WEAVER_LICHEN = BLOCKS.register("vapor_weaver_lichen",
            name -> new ddraig.net.entropica.block.biome6.VaporWeaverLichenBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_CYAN)
                    .noCollision()
                    .noOcclusion()
                    .randomTicks()
                    .destroyTime(0.2f)
                    .sound(SoundType.GLOW_LICHEN)
                    .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome6.SteamVentBlock> STEAM_VENT = BLOCKS.register("steam_vent",
            name -> new ddraig.net.entropica.block.biome6.SteamVentBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BLACK)
                    .randomTicks()
                    .destroyTime(2.0f)
                    .explosionResistance(8.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.BASALT)
                    .lightLevel(s -> 3)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome6.VaporLiftMatBlock> VAPOR_LIFT_MAT = BLOCKS.register("vapor_lift_mat",
            name -> new ddraig.net.entropica.block.biome6.VaporLiftMatBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_CYAN)
                    .destroyTime(0.8f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome6.VaporCondenserBlock> VAPOR_CONDENSER = BLOCKS.register("vapor_condenser",
            name -> new ddraig.net.entropica.block.biome6.VaporCondenserBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_ORANGE)
                    .randomTicks()
                    .noOcclusion()
                    .destroyTime(2.5f)
                    .explosionResistance(8.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome6.VaporCondenserBlock.POWERED) ? 7 : 2)));

    // ==========================================
    // BIOME 7: 2.7 STATIC ICE-PLAINS
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome7.FulguriteIceBlock> FULGURITE_ICE = BLOCKS.register("fulgurite_ice",
            name -> new ddraig.net.entropica.block.biome7.FulguriteIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.98f)
                    .destroyTime(0.6f)
                    .sound(SoundType.GLASS)
                    .lightLevel(s -> 4)
                    .noOcclusion()));

    public static final RegistrySupplier<RotatedPillarBlock> FULGURITE_PINE_LOG = BLOCKS.register("fulgurite_pine_log",
            name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_CYAN)
                    .destroyTime(2.0f)
                    .sound(SoundType.WOOD)
                    .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome7.FulguritePineLeavesBlock> FULGURITE_PINE_LEAVES = BLOCKS.register("fulgurite_pine_leaves",
            name -> new ddraig.net.entropica.block.biome7.FulguritePineLeavesBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_CYAN)
                    .destroyTime(0.2f)
                    .sound(SoundType.GRASS)
                    .lightLevel(s -> 1)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome7.StaticConductiveMatBlock> STATIC_CONDUCTIVE_MAT = BLOCKS.register("static_conductive_mat",
            name -> new ddraig.net.entropica.block.biome7.StaticConductiveMatBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_ORANGE)
                    .destroyTime(0.8f)
                    .sound(SoundType.COPPER)
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome7.StaticConductiveMatBlock.POWERED) ? 6 : 2)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome7.BiomassCapacitorBlock> BIOMASS_CAPACITOR = BLOCKS.register("biomass_capacitor",
            name -> new ddraig.net.entropica.block.biome7.BiomassCapacitorBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DEEPSLATE)
                    .noOcclusion()
                    .destroyTime(2.5f)
                    .explosionResistance(8.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
                    .lightLevel(s -> (s.getValue(ddraig.net.entropica.block.biome7.BiomassCapacitorBlock.CHARGED)
                            || s.getValue(ddraig.net.entropica.block.biome7.BiomassCapacitorBlock.DISCHARGING)) ? 12 : 3)));

    // ==========================================
    // BIOME 8: 2.8 BARROW-MOUND TUNDRA
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome8.NecroticSoilBlock> NECROTIC_SOIL = BLOCKS.register("necrotic_soil",
            name -> new ddraig.net.entropica.block.biome8.NecroticSoilBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.65F)
                    .sound(SoundType.ROOTED_DIRT)
                    .randomTicks()));

    public static final RegistrySupplier<Block> BARROW_STONE_BRICKS = BLOCKS.register("barrow_stone_bricks",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)));

    public static final RegistrySupplier<Block> CRACKED_BARROW_BRICKS = BLOCKS.register("cracked_barrow_bricks",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(1.8F, 5.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)));

    public static final RegistrySupplier<Block> MOSSY_BARROW_BRICKS = BLOCKS.register("mossy_barrow_bricks",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome8.GraveMossBlock> GRAVE_MOSS = BLOCKS.register("grave_moss",
            name -> new ddraig.net.entropica.block.biome8.GraveMossBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.1F)
                    .sound(SoundType.MOSS_CARPET)
                    .lightLevel(s -> 1)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome8.CryptGraveRootBlock> CRYPT_GRAVE_ROOT = BLOCKS.register("crypt_grave_root",
            name -> new ddraig.net.entropica.block.biome8.CryptGraveRootBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.8F)
                    .sound(SoundType.ROOTS)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome8.RimeCapMushroomBlock> RIME_CAP_MUSHROOM = BLOCKS.register("rime_cap_mushroom",
            name -> new ddraig.net.entropica.block.biome8.RimeCapMushroomBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noCollision()
                    .instabreak()
                    .noOcclusion()
                    .sound(SoundType.FUNGUS)
                    .lightLevel(s -> 6)));

    public static final RegistrySupplier<Block> POTTED_RIME_CAP_MUSHROOM = BLOCKS.register("potted_rime_cap_mushroom",
            name -> new FlowerPotBlock(ModBlocks.RIME_CAP_MUSHROOM.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)
                            .setId(ResourceKey.create(Registries.BLOCK, name))
                            .lightLevel(s -> 6)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome8.RunedBrazierBlock> RUNED_BRAZIER = BLOCKS.register("runed_brazier",
            name -> new ddraig.net.entropica.block.biome8.RunedBrazierBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(2.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.LANTERN)
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome8.RunedBrazierBlock.LIT) ? 14 : 0)
                    .noOcclusion()
                    .randomTicks()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome8.CryptSiphonLanternBlock> CRYPT_SIPHON_LANTERN = BLOCKS.register("crypt_siphon_lantern",
            name -> new ddraig.net.entropica.block.biome8.CryptSiphonLanternBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.LANTERN)
                    .lightLevel(s -> 13)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome8.BarrowSentinelTotemBlock> BARROW_SENTINEL_TOTEM = BLOCKS.register("barrow_sentinel_totem",
            name -> new ddraig.net.entropica.block.biome8.BarrowSentinelTotemBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(2.5F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .lightLevel(s -> 6)
                    .noOcclusion()
                    .randomTicks()));

    // ==========================================
    // BIOME 9: 2.9 NULL-FRINGE BAY
    // ==========================================
    public static final RegistrySupplier<net.minecraft.world.level.block.ColoredFallingBlock> VOID_SAND = BLOCKS.register("void_sand",
            name -> new net.minecraft.world.level.block.ColoredFallingBlock(
                    new net.minecraft.util.ColorRGBA(0x2E1065),
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, name))
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(0.5F)
                            .sound(SoundType.SAND)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome9.VoidSandTurfBlock> VOID_SAND_TURF = BLOCKS.register("void_sand_turf",
            name -> new ddraig.net.entropica.block.biome9.VoidSandTurfBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.65F)
                    .sound(SoundType.SAND)));

    public static final RegistrySupplier<Block> VOID_STONE = BLOCKS.register("void_stone",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F, 8.0F)
                    .sound(SoundType.DEEPSLATE)));

    public static final RegistrySupplier<Block> OBSIDIAN_BRICKS = BLOCKS.register("obsidian_bricks",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()
                    .strength(25.0F, 600.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome9.VoidCrystalBlock> VOID_CRYSTAL_BLOCK = BLOCKS.register("void_crystal_block",
            name -> new ddraig.net.entropica.block.biome9.VoidCrystalBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_PURPLE)
                    .requiresCorrectToolForDrops()
                    .strength(1.8F, 6.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 9)
                    .randomTicks()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome9.VoidCrystalClusterBlock> VOID_CRYSTAL_BUD = BLOCKS.register("void_crystal_bud",
            name -> new ddraig.net.entropica.block.biome9.VoidCrystalClusterBlock(4.0F, 3.0F, BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_PURPLE)
                    .forceSolidOn()
                    .noOcclusion()
                    .strength(1.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 6)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome9.VoidCrystalClusterBlock> VOID_CRYSTAL_CLUSTER = BLOCKS.register("void_crystal_cluster",
            name -> new ddraig.net.entropica.block.biome9.VoidCrystalClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_PURPLE)
                    .forceSolidOn()
                    .noOcclusion()
                    .strength(1.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 12)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome9.NullFringeTussockBlock> NULL_FRINGE_TUSSOCK = BLOCKS.register("null_fringe_tussock",
            name -> new ddraig.net.entropica.block.biome9.NullFringeTussockBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(s -> 2)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> POTTED_NULL_FRINGE_TUSSOCK = BLOCKS.register("potted_null_fringe_tussock",
            name -> new FlowerPotBlock(ModBlocks.NULL_FRINGE_TUSSOCK.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)
                            .setId(ResourceKey.create(Registries.BLOCK, name))
                            .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome9.SpatialPhaseAnchorBlock> SPATIAL_PHASE_ANCHOR = BLOCKS.register("spatial_phase_anchor",
            name -> new ddraig.net.entropica.block.biome9.SpatialPhaseAnchorBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.5F, 50.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.LODESTONE)
                    .lightLevel(s -> 11)
                    .noOcclusion()));

    // ==========================================
    // BIOME 10: 2.10 SUN-GLAZED SANCTUARY
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome10.RadiantSnowBlock> RADIANT_SNOW_BLOCK = BLOCKS.register("radiant_snow_block",
            name -> new ddraig.net.entropica.block.biome10.RadiantSnowBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.SNOW)
                    .strength(0.3F)
                    .sound(SoundType.SNOW)
                    .lightLevel(s -> 4)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome10.RadiantGlazeIceBlock> RADIANT_GLAZE_ICE = BLOCKS.register("radiant_glaze_ice",
            name -> new ddraig.net.entropica.block.biome10.RadiantGlazeIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.96F)
                    .strength(0.6F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(s -> 6)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome10.GlazedAmberLeavesBlock> GLAZED_AMBER_LEAVES = BLOCKS.register("glazed_amber_leaves",
            name -> new ddraig.net.entropica.block.biome10.GlazedAmberLeavesBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.GOLD)
                    .strength(0.2F)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .lightLevel(s -> 3)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome10.GoldenLichenBlock> GOLDEN_LICHEN = BLOCKS.register("golden_lichen",
            name -> new ddraig.net.entropica.block.biome10.GoldenLichenBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.GOLD)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GLOW_LICHEN)
                    .lightLevel(net.minecraft.world.level.block.GlowLichenBlock.emission(7))
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome10.VitaeSpringStoneBlock> VITAE_SPRING_STONE = BLOCKS.register("vitae_spring_stone",
            name -> new ddraig.net.entropica.block.biome10.VitaeSpringStoneBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.QUARTZ)
                    .requiresCorrectToolForDrops()
                    .strength(1.6F, 6.0F)
                    .sound(SoundType.CALCITE)
                    .lightLevel(s -> 5)
                    .randomTicks()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome10.QilinLightRefractorBlock> QILIN_LIGHT_REFRACTOR = BLOCKS.register("qilin_light_refractor",
            name -> new ddraig.net.entropica.block.biome10.QilinLightRefractorBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.EMERALD)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F, 8.0F)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()
                    .lightLevel(s -> 14)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome10.SunGlazedLanternBlock> SUN_GLAZED_LANTERN = BLOCKS.register("sun_glazed_lantern",
            name -> new ddraig.net.entropica.block.biome10.SunGlazedLanternBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.GOLD)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()
                    .lightLevel(s -> 15)));

    // ==========================================
    // BIOME 11: 2.11 STORM-SWEPT ICEBERGS
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome11.FulguriteBlock> FULGURITE_BLOCK = BLOCKS.register("fulgurite_block",
            name -> new ddraig.net.entropica.block.biome11.FulguriteBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_GRAY)
                    .requiresCorrectToolForDrops()
                    .strength(2.2F, 12.0F)
                    .sound(SoundType.BASALT)
                    .lightLevel(s -> 3)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome11.ZephyrIceBlock> ZEPHYR_ICE = BLOCKS.register("zephyr_ice",
            name -> new ddraig.net.entropica.block.biome11.ZephyrIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.985F)
                    .strength(0.5F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(s -> 3)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome11.ZephyrGlassGrassBlock> ZEPHYR_GLASS_GRASS = BLOCKS.register("zephyr_glass_grass",
            name -> new ddraig.net.entropica.block.biome11.ZephyrGlassGrassBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DIAMOND)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GLASS)
                    .lightLevel(s -> 2)
                    .noOcclusion()
                    .offsetType(BlockBehaviour.OffsetType.XZ)));

    public static final RegistrySupplier<Block> POTTED_ZEPHYR_GLASS_GRASS = BLOCKS.register("potted_zephyr_glass_grass",
            name -> new FlowerPotBlock(ModBlocks.ZEPHYR_GLASS_GRASS.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)
                            .setId(ResourceKey.create(Registries.BLOCK, name))
                            .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome11.ZephyrUpdraftGrateBlock> ZEPHYR_UPDRAFT_GRATE = BLOCKS.register("zephyr_updraft_grate",
            name -> new ddraig.net.entropica.block.biome11.ZephyrUpdraftGrateBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_ORANGE)
                    .noOcclusion()
                    .strength(2.5F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER_GRATE)
                    .randomTicks()
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome11.ZephyrUpdraftGrateBlock.POWERED) ? 11 : 6)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome11.SimurghWindTurbineBlock> SIMURGH_WIND_TURBINE = BLOCKS.register("simurgh_wind_turbine",
            name -> new ddraig.net.entropica.block.biome11.SimurghWindTurbineBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.GOLD)
                    .noOcclusion()
                    .strength(2.2F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
                    .randomTicks()
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome11.SimurghWindTurbineBlock.POWER) >= 12 ? 7 : 4)));

    // ==========================================
    // BIOME 12: 2.12 SPORE-GLAZED PACK
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome12.FrozenSporeMossBlock> FROZEN_SPORE_MOSS = BLOCKS.register("frozen_spore_moss",
            name -> new ddraig.net.entropica.block.biome12.FrozenSporeMossBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.3F)
                    .sound(SoundType.MOSS)
                    .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome12.FrozenSporeMossCarpetBlock> FROZEN_SPORE_MOSS_CARPET = BLOCKS.register("frozen_spore_moss_carpet",
            name -> new ddraig.net.entropica.block.biome12.FrozenSporeMossCarpetBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.1F)
                    .sound(SoundType.MOSS)
                    .lightLevel(s -> 1)
                    .noOcclusion()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome12.SporeGlazedIceBlock> SPORE_GLAZED_ICE = BLOCKS.register("spore_glazed_ice",
            name -> new ddraig.net.entropica.block.biome12.SporeGlazedIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.98F)
                    .strength(0.5F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(s -> 3)));

    public static final RegistrySupplier<RotatedPillarBlock> SPORE_WOOD_LOG = BLOCKS.register("spore_wood_log",
            name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .destroyTime(2.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistrySupplier<RotatedPillarBlock> SPORE_WOOD = BLOCKS.register("spore_wood",
            name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.WOOD)
                    .destroyTime(2.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistrySupplier<Block> SPORE_WOOD_PLANKS = BLOCKS.register("spore_wood_planks",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistrySupplier<StairBlock> SPORE_WOOD_STAIRS = BLOCKS.register("spore_wood_stairs",
            name -> new StairBlock(ModBlocks.SPORE_WOOD_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistrySupplier<SlabBlock> SPORE_WOOD_SLAB = BLOCKS.register("spore_wood_slab",
            name -> new SlabBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistrySupplier<FenceBlock> SPORE_WOOD_FENCE = BLOCKS.register("spore_wood_fence",
            name -> new FenceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome12.FrozenSporeLeavesBlock> FROZEN_SPORE_LEAVES = BLOCKS.register("frozen_spore_leaves",
            name -> new ddraig.net.entropica.block.biome12.FrozenSporeLeavesBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.2F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)
                    .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome12.GlazeCapMushroomBlock> GLAZE_CAP_MUSHROOM_BLOCK = BLOCKS.register("glaze_cap_mushroom_block",
            name -> new ddraig.net.entropica.block.biome12.GlazeCapMushroomBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.GOLD)
                    .strength(0.4F)
                    .sound(SoundType.WOOD)
                    .lightLevel(s -> 4)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome12.SporeBellowsBlock> SPORE_BELLOWS = BLOCKS.register("spore_bellows",
            name -> new ddraig.net.entropica.block.biome12.SporeBellowsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.WOOD)
                    .noOcclusion()
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome12.SporeBellowsBlock.TRIGGERED) ? 6 : 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome12.CryoSporeCenserBlock> CRYO_SPORE_CENSER = BLOCKS.register("cryo_spore_censer",
            name -> new ddraig.net.entropica.block.biome12.CryoSporeCenserBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_ORANGE)
                    .noOcclusion()
                    .strength(2.2F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.LANTERN)
                    .randomTicks()
                    .lightLevel(s -> 13)));

    // ==========================================
    // BIOME 13: 2.13 MIRAGE GLACIER
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome13.GlassSandBlock> GLASS_SAND = BLOCKS.register("glass_sand",
            name -> new ddraig.net.entropica.block.biome13.GlassSandBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(0.5F)
                    .sound(SoundType.SAND)));

    public static final RegistrySupplier<Block> GLASS_SANDSTONE = BLOCKS.register("glass_sandstone",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(1.2F, 4.0F)
                    .sound(SoundType.STONE)));

    public static final RegistrySupplier<StairBlock> GLASS_SANDSTONE_STAIRS = BLOCKS.register("glass_sandstone_stairs",
            name -> new StairBlock(ModBlocks.GLASS_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(1.2F, 4.0F)
                    .sound(SoundType.STONE)));

    public static final RegistrySupplier<SlabBlock> GLASS_SANDSTONE_SLAB = BLOCKS.register("glass_sandstone_slab",
            name -> new SlabBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(1.2F, 4.0F)
                    .sound(SoundType.STONE)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome13.MirageIceBlock> MIRAGE_ICE = BLOCKS.register("mirage_ice",
            name -> new ddraig.net.entropica.block.biome13.MirageIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.985F)
                    .strength(0.5F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome13.MirageIceBlock.PHASED) ? 3 : 6)
                    .isSuffocating((s, l, p) -> !s.getValue(ddraig.net.entropica.block.biome13.MirageIceBlock.PHASED))
                    .isViewBlocking((s, l, p) -> !s.getValue(ddraig.net.entropica.block.biome13.MirageIceBlock.PHASED))));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome13.MiragePrismLanternBlock> MIRAGE_PRISM_LANTERN = BLOCKS.register("mirage_prism_lantern",
            name -> new ddraig.net.entropica.block.biome13.MiragePrismLanternBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.GOLD)
                    .noOcclusion()
                    .strength(2.2F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.LANTERN)
                    .randomTicks()
                    .lightLevel(s -> 15)));

    // ==========================================
    // BIOME 14: 2.14 OASIS FLOES
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome14.FrostWillowLeavesBlock> FROST_WILLOW_LEAVES = BLOCKS.register("frost_willow_leaves",
            name -> new ddraig.net.entropica.block.biome14.FrostWillowLeavesBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .ignitedByLava()
                    .isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome14.FrostWillowVinesBlock> FROST_WILLOW_VINES = BLOCKS.register("frost_willow_vines",
            name -> new ddraig.net.entropica.block.biome14.FrostWillowVinesBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .noOcclusion()
                    .randomTicks()
                    .strength(0.2F)
                    .sound(SoundType.VINE)
                    .ignitedByLava()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .lightLevel(s -> 2)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome14.AmberResinBlock> AMBER_RESIN_BLOCK = BLOCKS.register("amber_resin_block",
            name -> new ddraig.net.entropica.block.biome14.AmberResinBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_ORANGE)
                    .friction(0.8F)
                    .strength(0.6F)
                    .sound(SoundType.HONEY_BLOCK)
                    .noOcclusion()
                    .lightLevel(s -> 4)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome14.AmberTorchBlock> AMBER_TORCH = BLOCKS.register("amber_torch",
            name -> new ddraig.net.entropica.block.biome14.AmberTorchBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .noCollision()
                    .instabreak()
                    .lightLevel(s -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .randomTicks()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome14.WallAmberTorchBlock> WALL_AMBER_TORCH = BLOCKS.register("wall_amber_torch",
            name -> new ddraig.net.entropica.block.biome14.WallAmberTorchBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .overrideDescription(AMBER_TORCH.get().getDescriptionId())
                    .noCollision()
                    .instabreak()
                    .lightLevel(s -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .randomTicks()));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome14.OasisDewBasinBlock> OASIS_DEW_BASIN = BLOCKS.register("oasis_dew_basin",
            name -> new ddraig.net.entropica.block.biome14.OasisDewBasinBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.QUARTZ)
                    .noOcclusion()
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .randomTicks()
                    .lightLevel(s -> s.getValue(ddraig.net.entropica.block.biome14.OasisDewBasinBlock.INFUSED) ? 14 : 12)));

    // ==========================================
    // BIOME 15: 2.15 DUST-CRUSTED BERGS
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome15.DustCrustedIceBlock> DUST_CRUSTED_ICE = BLOCKS.register("dust_crusted_ice",
            name -> new ddraig.net.entropica.block.biome15.DustCrustedIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.96F)
                    .strength(0.6F)
                    .sound(SoundType.GLASS)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome15.GreySandBlock> GREY_SAND = BLOCKS.register("grey_sand",
            name -> new ddraig.net.entropica.block.biome15.GreySandBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.SNARE)
                    .strength(0.5F)
                    .sound(SoundType.SAND)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome15.WindBlownDustScrubBlock> WIND_BLOWN_DUST_SCRUB = BLOCKS.register("wind_blown_dust_scrub",
            name -> new ddraig.net.entropica.block.biome15.WindBlownDustScrubBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.WOOD)
                    .noCollision()
                    .noOcclusion()
                    .instabreak()
                    .randomTicks()
                    .ignitedByLava()
                    .sound(SoundType.GRASS)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .offsetType(BlockBehaviour.OffsetType.XZ)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome15.ToughFiberRopeBlock> TOUGH_FIBER_ROPE = BLOCKS.register("tough_fiber_rope",
            name -> new ddraig.net.entropica.block.biome15.ToughFiberRopeBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.SAND)
                    .noOcclusion()
                    .strength(0.4F)
                    .sound(SoundType.LADDER)
                    .ignitedByLava()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome15.WindBaffleLanternBlock> WIND_BAFFLE_LANTERN = BLOCKS.register("wind_baffle_lantern",
            name -> new ddraig.net.entropica.block.biome15.WindBaffleLanternBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.GOLD)
                    .noOcclusion()
                    .strength(2.2F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.LANTERN)
                    .randomTicks()
                    .lightLevel(s -> 14)));

    // ==========================================
    // BIOME 16: 2.16 BLOOD-ICE SHALLOWS
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome16.SanguineIceBlock> SANGUINE_ICE = BLOCKS.register("sanguine_ice",
            name -> new ddraig.net.entropica.block.biome16.SanguineIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.CRIMSON_NYLIUM)
                    .friction(0.98F)
                    .strength(0.5F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(s -> 3)
                    .isValidSpawn((s, g, p, e) -> true)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome16.RedClaySandBlock> RED_CLAY_SAND = BLOCKS.register("red_clay_sand",
            name -> new ddraig.net.entropica.block.biome16.RedClaySandBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.SNARE)
                    .strength(0.6F)
                    .sound(SoundType.SAND)));

    public static final RegistrySupplier<Block> SANGUINE_CLAY_BRICKS = BLOCKS.register("sanguine_clay_bricks",
            name -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE_BRICKS)));

    public static final RegistrySupplier<StairBlock> SANGUINE_CLAY_BRICK_STAIRS = BLOCKS.register("sanguine_clay_brick_stairs",
            name -> new StairBlock(ModBlocks.SANGUINE_CLAY_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE_BRICKS)));

    public static final RegistrySupplier<SlabBlock> SANGUINE_CLAY_BRICK_SLAB = BLOCKS.register("sanguine_clay_brick_slab",
            name -> new SlabBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE_BRICKS)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome16.RedAlgaeBlock> RED_ALGAE = BLOCKS.register("red_algae",
            name -> new ddraig.net.entropica.block.biome16.RedAlgaeBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.CRIMSON_NYLIUM)
                    .noCollision()
                    .noOcclusion()
                    .instabreak()
                    .sound(SoundType.WET_GRASS)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome16.CrimsonTideLanternBlock> CRIMSON_TIDE_LANTERN = BLOCKS.register("crimson_tide_lantern",
            name -> new ddraig.net.entropica.block.biome16.CrimsonTideLanternBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.CRIMSON_NYLIUM)
                    .noOcclusion()
                    .strength(2.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.LANTERN)
                    .randomTicks()
                    .lightLevel(s -> 15)));

    // ==========================================
    // BIOME 17: 2.17 ASTRAL SKERRIES
    // ==========================================
    public static final RegistrySupplier<ddraig.net.entropica.block.biome17.StarlightIceBlock> STARLIGHT_ICE = BLOCKS.register("starlight_ice",
            name -> new ddraig.net.entropica.block.biome17.StarlightIceBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.ICE)
                    .friction(0.985F)
                    .strength(0.6F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(s -> 5)
                    .isValidSpawn((s, g, p, e) -> true)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome17.StardustCrystalBlock> STARDUST_CRYSTAL_BLOCK = BLOCKS.register("stardust_crystal_block",
            name -> new ddraig.net.entropica.block.biome17.StardustCrystalBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DIAMOND)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 9)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome17.StardustCrystalClusterBlock> STARDUST_CRYSTAL_CLUSTER = BLOCKS.register("stardust_crystal_cluster",
            name -> new ddraig.net.entropica.block.biome17.StardustCrystalClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.DIAMOND)
                    .forceSolidOn()
                    .noOcclusion()
                    .strength(1.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .lightLevel(s -> 12)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome17.RimeStarTussockBlock> RIME_STAR_TUSSOCK = BLOCKS.register("rime_star_tussock",
            name -> new ddraig.net.entropica.block.biome17.RimeStarTussockBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noCollision()
                    .noOcclusion()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .lightLevel(s -> 3)));

    public static final RegistrySupplier<Block> POTTED_RIME_STAR_TUSSOCK = BLOCKS.register("potted_rime_star_tussock",
            name -> new FlowerPotBlock(ModBlocks.RIME_STAR_TUSSOCK.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)
                            .setId(ResourceKey.create(Registries.BLOCK, name))
                            .lightLevel(s -> 3)));

    public static final RegistrySupplier<ddraig.net.entropica.block.biome17.CelestialOrreryLanternBlock> CELESTIAL_ORRERY_LANTERN = BLOCKS.register("celestial_orrery_lantern",
            name -> new ddraig.net.entropica.block.biome17.CelestialOrreryLanternBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, name))
                    .mapColor(MapColor.GOLD)
                    .noOcclusion()
                    .strength(2.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.LANTERN)
                    .randomTicks()
                    .lightLevel(s -> 15)));
}

