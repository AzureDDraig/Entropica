package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Entropica.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaFurnaceBlockEntity>> MANA_FURNACE_BE =
            BLOCK_ENTITIES.register("mana_furnace", () ->
                    new BlockEntityType<>(ManaFurnaceBlockEntity::new, ModBlocks.MANA_FURNACE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EntropicCoreBlockEntity>> ENTROPIC_CORE_BE =
            BLOCK_ENTITIES.register("entropic_core", () ->
                    new BlockEntityType<>(EntropicCoreBlockEntity::new, ModBlocks.ENTROPIC_CORE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssenceReceptacleBlockEntity>> ESSENCE_RECEPTACLE_BE =
            BLOCK_ENTITIES.register("essence_receptacle", () ->
                    new BlockEntityType<>(EssenceReceptacleBlockEntity::new, ModBlocks.ESSENCE_RECEPTACLE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OrbisCellBlockEntity>> ORBIS_CELL_BE =
            BLOCK_ENTITIES.register("orbis_cell", () ->
                    new BlockEntityType<>(OrbisCellBlockEntity::new, ModBlocks.ORBIS_CELL.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CatalystReceptacleBlockEntity>> CATALYST_RECEPTACLE_BE =
            BLOCK_ENTITIES.register("catalyst_receptacle", () ->
                    new BlockEntityType<>(CatalystReceptacleBlockEntity::new, ModBlocks.CATALYST_RECEPTACLE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaReadoutBlockEntity>> MANA_READOUT_BE =
            BLOCK_ENTITIES.register("mana_readout", () ->
                    new BlockEntityType<>(ManaReadoutBlockEntity::new, ModBlocks.MANA_READOUT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssenceReadoutBlockEntity>> ESSENCE_READOUT_BE =
            BLOCK_ENTITIES.register("essence_readout", () ->
                    new BlockEntityType<>(EssenceReadoutBlockEntity::new, ModBlocks.ESSENCE_READOUT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaPlumeBlockEntity>> MANA_PLUME_BE =
            BLOCK_ENTITIES.register("mana_plume", () ->
                    new BlockEntityType<>(ManaPlumeBlockEntity::new, ModBlocks.MANA_PLUME.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaFilterBlockEntity>> MANA_FILTER_BE =
            BLOCK_ENTITIES.register("mana_filter", () ->
                    new BlockEntityType<>(ManaFilterBlockEntity::new, ModBlocks.MANA_FILTER.get()));

    // --- VIS FUME NETWORK ---

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VisFumePipeBlockEntity>> VIS_FUME_PIPE_BE =
            BLOCK_ENTITIES.register("vis_fume_pipe", () ->
                    new BlockEntityType<>(VisFumePipeBlockEntity::new, ModBlocks.VIS_FUME_PIPE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VisFumeValveBlockEntity>> VIS_FUME_VALVE_BE =
            BLOCK_ENTITIES.register("vis_fume_valve", () ->
                    new BlockEntityType<>(VisFumeValveBlockEntity::new, ModBlocks.VIS_FUME_VALVE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VisFumeOneWayValveBlockEntity>> VIS_FUME_ONE_WAY_VALVE_BE =
            BLOCK_ENTITIES.register("vis_fume_one_way_valve", () ->
                    new BlockEntityType<>(VisFumeOneWayValveBlockEntity::new, ModBlocks.VIS_FUME_ONE_WAY_VALVE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VisFumeDiverterBlockEntity>> VIS_FUME_DIVERTER_BE =
            BLOCK_ENTITIES.register("vis_fume_diverter", () ->
                    new BlockEntityType<>(VisFumeDiverterBlockEntity::new, ModBlocks.VIS_FUME_DIVERTER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeVisFumeGeneratorBlockEntity>> CREATIVE_VIS_FUME_GENERATOR_BE =
            BLOCK_ENTITIES.register("creative_vis_fume_generator", () ->
                    new BlockEntityType<>(CreativeVisFumeGeneratorBlockEntity::new, ModBlocks.CREATIVE_VIS_FUME_GENERATOR.get()));
}