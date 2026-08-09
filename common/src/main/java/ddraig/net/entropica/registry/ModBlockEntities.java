package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Entropica.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<MateriaFurnaceBlockEntity>> MATERIA_FURNACE_BE =
            BLOCK_ENTITIES.register("materia_furnace", () ->
                    BlockEntityHelper.create(MateriaFurnaceBlockEntity::new, ModBlocks.MATERIA_FURNACE.get()));

    public static final RegistrySupplier<BlockEntityType<EntropicCoreBlockEntity>> ENTROPIC_CORE_BE =
            BLOCK_ENTITIES.register("entropic_core", () ->
                    BlockEntityHelper.create(EntropicCoreBlockEntity::new, ModBlocks.ENTROPIC_CORE.get()));

    public static final RegistrySupplier<BlockEntityType<EssenceReceptacleBlockEntity>> ESSENCE_RECEPTACLE_BE =
            BLOCK_ENTITIES.register("essence_receptacle", () ->
                    BlockEntityHelper.create(EssenceReceptacleBlockEntity::new, ModBlocks.ESSENCE_RECEPTACLE.get()));

    public static final RegistrySupplier<BlockEntityType<OrbisCellBlockEntity>> ORBIS_CELL_BE =
            BLOCK_ENTITIES.register("orbis_cell", () ->
                    BlockEntityHelper.create(OrbisCellBlockEntity::new, ModBlocks.ORBIS_CELL.get()));

    public static final RegistrySupplier<BlockEntityType<CatalystReceptacleBlockEntity>> CATALYST_RECEPTACLE_BE =
            BLOCK_ENTITIES.register("catalyst_receptacle", () ->
                    BlockEntityHelper.create(CatalystReceptacleBlockEntity::new, ModBlocks.CATALYST_RECEPTACLE.get()));

    public static final RegistrySupplier<BlockEntityType<MateriaReadoutBlockEntity>> MANA_READOUT_BE =
            BLOCK_ENTITIES.register("materia_readout", () ->
                    BlockEntityHelper.create(MateriaReadoutBlockEntity::new, ModBlocks.VIS_READOUT.get()));

    public static final RegistrySupplier<BlockEntityType<EssenceReadoutBlockEntity>> ESSENCE_READOUT_BE =
            BLOCK_ENTITIES.register("raw_materia_readout", () ->
                    BlockEntityHelper.create(EssenceReadoutBlockEntity::new, ModBlocks.ESSENCE_READOUT.get()));

    public static final RegistrySupplier<BlockEntityType<MateriaPlumeBlockEntity>> MATERIA_PLUME_BE =
            BLOCK_ENTITIES.register("materia_plume", () ->
                    BlockEntityHelper.create(MateriaPlumeBlockEntity::new, ModBlocks.MATERIA_PLUME.get()));

    public static final RegistrySupplier<BlockEntityType<MateriaFilterBlockEntity>> MATERIA_FILTER_BE =
            BLOCK_ENTITIES.register("materia_filter", () ->
                    BlockEntityHelper.create(MateriaFilterBlockEntity::new, ModBlocks.MATERIA_FILTER.get()));

    public static final RegistrySupplier<BlockEntityType<DecompressionCouplerBlockEntity>> DECOMPRESSION_COUPLER_BE =
            BLOCK_ENTITIES.register("decompression_coupler", () ->
                    BlockEntityHelper.create(DecompressionCouplerBlockEntity::new, ModBlocks.DECOMPRESSION_COUPLING.get()));

    // --- Vapor Pneumatic Network ---

    public static final RegistrySupplier<BlockEntityType<VaporPneumaticValveBlockEntity>> VAPOR_PNEUMATIC_VALVE_BE =
            BLOCK_ENTITIES.register("vapor_pneumatic_valve", () ->
                    BlockEntityHelper.create(VaporPneumaticValveBlockEntity::new, ModBlocks.VAPOR_PNEUMATIC_VALVE.get()));

    public static final RegistrySupplier<BlockEntityType<VaporPneumaticOneWayValveBlockEntity>> VAPOR_PNEUMATIC_ONE_WAY_VALVE_BE =
            BLOCK_ENTITIES.register("vapor_pneumatic_one_way_valve", () ->
                    BlockEntityHelper.create(VaporPneumaticOneWayValveBlockEntity::new, ModBlocks.VAPOR_PNEUMATIC_ONE_WAY_VALVE.get()));

    public static final RegistrySupplier<BlockEntityType<VaporPneumaticDiverterBlockEntity>> VAPOR_PNEUMATIC_DIVERTER_BE =
            BLOCK_ENTITIES.register("vapor_pneumatic_diverter", () ->
                    BlockEntityHelper.create(VaporPneumaticDiverterBlockEntity::new, ModBlocks.VAPOR_PNEUMATIC_DIVERTER.get()));

    public static final RegistrySupplier<BlockEntityType<CreativeMateriaGeneratorBlockEntity>> CREATIVE_MATERIA_GENERATOR_BE =
            BLOCK_ENTITIES.register("creative_materia_generator", () ->
                    BlockEntityHelper.create(CreativeMateriaGeneratorBlockEntity::new, ModBlocks.CREATIVE_MATERIA_GENERATOR.get()));

    public static final RegistrySupplier<BlockEntityType<MateriaPumpBlockEntity>> MATERIA_PUMP_BE =
            BLOCK_ENTITIES.register("materia_pump", () ->
                    BlockEntityHelper.create(MateriaPumpBlockEntity::new, ModBlocks.MATERIA_PUMP.get()));

    // --- VAPOR PNEUMATIC PIPES (T2/T3 — Mfum/Msub) ---
    // All 10 tiers share one BE class; instance type is resolved via registry path at runtime
    public static final RegistrySupplier<BlockEntityType<VaporPneumaticPipeBlockEntity>> VAPOR_PNEUMATIC_PIPE_BE =
            BLOCK_ENTITIES.register("vapor_pneumatic_pipe", () ->
                    BlockEntityHelper.create(VaporPneumaticPipeBlockEntity::new,
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_COPPER.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_IRON.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_GOLD.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_ARCANITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_DIAMOND.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_VISCANITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_RESONITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_ARCANITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_VISCANITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_RESONITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CAPUTITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_CAPUTITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_SANGUINITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_SANGUINITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_MERCURITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_MERCURITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_EUCLIDITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_EUCLIDITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_ATHANORITE.get(),
                            ModBlocks.VAPOR_PNEUMATIC_PIPE_CHARGED_ATHANORITE.get()
                    ));

    // --- HYDRAULIC PIPELINE (T4 — Mliq) ---
    public static final RegistrySupplier<BlockEntityType<HydraulicPipelineBlockEntity>> HYDRAULIC_PIPELINE_BE =
            BLOCK_ENTITIES.register("hydraulic_pipeline", () ->
                    BlockEntityHelper.create(HydraulicPipelineBlockEntity::new,
                            ModBlocks.HYDRAULIC_PIPELINE_IRON.get(),
                            ModBlocks.HYDRAULIC_PIPELINE_ARCANITE.get(),
                            ModBlocks.HYDRAULIC_PIPELINE_VISCANITE.get(),
                            ModBlocks.HYDRAULIC_PIPELINE_RESONITE.get(),
                            ModBlocks.HYDRAULIC_PIPELINE_CHARGED_ARCANITE.get(),
                            ModBlocks.HYDRAULIC_PIPELINE_CHARGED_VISCANITE.get(),
                            ModBlocks.HYDRAULIC_PIPELINE_CHARGED_RESONITE.get()
                    ));

    // --- VOLTAIC CONDUIT (T5 — Mvol) ---
    public static final RegistrySupplier<BlockEntityType<VoltaicConduitBlockEntity>> VOLTAIC_CONDUIT_BE =
            BLOCK_ENTITIES.register("voltaic_conduit", () ->
                    BlockEntityHelper.create(VoltaicConduitBlockEntity::new,
                            ModBlocks.VOLTAIC_CONDUIT_ARCANITE.get(),
                            ModBlocks.VOLTAIC_CONDUIT_VISCANITE.get(),
                            ModBlocks.VOLTAIC_CONDUIT_RESONITE.get(),
                            ModBlocks.VOLTAIC_CONDUIT_CHARGED_ARCANITE.get(),
                            ModBlocks.VOLTAIC_CONDUIT_CHARGED_VISCANITE.get(),
                            ModBlocks.VOLTAIC_CONDUIT_CHARGED_RESONITE.get()
                    ));

    // --- T6: VISCOUS AGITATOR (Mcoa) ---
    public static final RegistrySupplier<BlockEntityType<ViscousAgitatorBlockEntity>> VISCOUS_AGITATOR_BE =
            BLOCK_ENTITIES.register("viscous_agitator", () ->
                    BlockEntityHelper.create(ViscousAgitatorBlockEntity::new,
                            ModBlocks.VISCOUS_AGITATOR_CAPUTITE.get(),
                            ModBlocks.VISCOUS_AGITATOR_CHARGED_CAPUTITE.get()
                    ));

    // --- T7: SANGUINE CONDUIT (Mich) ---
    public static final RegistrySupplier<BlockEntityType<SanguineConduitBlockEntity>> SANGUINE_CONDUIT_BE =
            BLOCK_ENTITIES.register("sanguine_conduit", () ->
                    BlockEntityHelper.create(SanguineConduitBlockEntity::new,
                            ModBlocks.SANGUINE_CONDUIT_SANGUINITE.get(),
                            ModBlocks.SANGUINE_CONDUIT_CHARGED_SANGUINITE.get()
                    ));

    // --- T8: EQUILIBRIUM CONDUIT (Mtra) ---
    public static final RegistrySupplier<BlockEntityType<EquilibriumConduitBlockEntity>> EQUILIBRIUM_CONDUIT_BE =
            BLOCK_ENTITIES.register("equilibrium_conduit", () ->
                    BlockEntityHelper.create(EquilibriumConduitBlockEntity::new,
                            ModBlocks.EQUILIBRIUM_CONDUIT_MERCURITE.get(),
                            ModBlocks.EQUILIBRIUM_CONDUIT_CHARGED_MERCURITE.get()
                    ));

    // --- T9: PRISTINE CONDUIT (Mper) ---
    public static final RegistrySupplier<BlockEntityType<PristineConduitBlockEntity>> PRISTINE_CONDUIT_BE =
            BLOCK_ENTITIES.register("pristine_conduit", () ->
                    BlockEntityHelper.create(PristineConduitBlockEntity::new,
                            ModBlocks.PRISTINE_CONDUIT_EUCLIDITE.get(),
                            ModBlocks.PRISTINE_CONDUIT_CHARGED_EUCLIDITE.get()
                    ));

    // --- T10: ATHANOR CONDUIT (Mlim) ---
    public static final RegistrySupplier<BlockEntityType<AthanorConduitBlockEntity>> ATHANOR_CONDUIT_BE =
            BLOCK_ENTITIES.register("athanor_conduit", () ->
                    BlockEntityHelper.create(AthanorConduitBlockEntity::new,
                            ModBlocks.ATHANOR_CONDUIT_ATHANORITE.get(),
                            ModBlocks.ATHANOR_CONDUIT_CHARGED_ATHANORITE.get()
                    ));

    // --- ADVANCED ORBIS CELLS ---
    public static final RegistrySupplier<BlockEntityType<SublimatedOrbisCellBlockEntity>> SUBLIMATED_ORBIS_CELL_BE =
            BLOCK_ENTITIES.register("sublimated_orbis_cell", () ->
                    BlockEntityHelper.create(SublimatedOrbisCellBlockEntity::new, ModBlocks.SUBLIMATED_ORBIS_CELL.get()));

    public static final RegistrySupplier<BlockEntityType<PneumaticCalixBlockEntity>> PNEUMATIC_CALIX_BE =
            BLOCK_ENTITIES.register("pneumatic_calix", () ->
                    BlockEntityHelper.create(PneumaticCalixBlockEntity::new, ModBlocks.PNEUMATIC_CALIX.get()));

    public static final RegistrySupplier<BlockEntityType<VoltaicCalixBlockEntity>> VOLTAIC_CALIX_BE =
            BLOCK_ENTITIES.register("voltaic_calix", () ->
                    BlockEntityHelper.create(VoltaicCalixBlockEntity::new, ModBlocks.VOLTAIC_CALIX.get()));

    public static final RegistrySupplier<BlockEntityType<MatrixCalixBlockEntity>> MATRIX_CALIX_BE =
            BLOCK_ENTITIES.register("matrix_calix", () ->
                    BlockEntityHelper.create(MatrixCalixBlockEntity::new, ModBlocks.MATRIX_CALIX.get()));

    public static final RegistrySupplier<BlockEntityType<ThecaCellBlockEntity>> THECA_CELL_BE =
            BLOCK_ENTITIES.register("theca_cell", () ->
                    BlockEntityHelper.create(ThecaCellBlockEntity::new, ModBlocks.THECA_CELL.get()));

    public static final RegistrySupplier<BlockEntityType<VasCellBlockEntity>> VAS_CELL_BE =
            BLOCK_ENTITIES.register("vas_cell", () ->
                    BlockEntityHelper.create(VasCellBlockEntity::new, ModBlocks.VAS_CELL.get()));

    public static final RegistrySupplier<BlockEntityType<MonadCoreBlockEntity>> MONAD_CORE_BE =
            BLOCK_ENTITIES.register("monad_core", () ->
                    BlockEntityHelper.create(MonadCoreBlockEntity::new, ModBlocks.MONAD_CORE.get()));

    public static final RegistrySupplier<BlockEntityType<AthanorCoreBlockEntity>> ATHANOR_CORE_BE =
            BLOCK_ENTITIES.register("athanor_core", () ->
                    BlockEntityHelper.create(AthanorCoreBlockEntity::new, ModBlocks.ATHANOR_CORE.get()));

    public static final RegistrySupplier<BlockEntityType<CreativeParticleGeneratorBlockEntity>> CREATIVE_PARTICLE_GENERATOR_BE =
            BLOCK_ENTITIES.register("creative_particle_generator", () ->
                    BlockEntityHelper.create(CreativeParticleGeneratorBlockEntity::new, ModBlocks.CREATIVE_PARTICLE_GENERATOR.get()));

    public static final RegistrySupplier<BlockEntityType<VaporPneumaticInputPortBlockEntity>> VAPOR_PNEUMATIC_INPUT_PORT_BE =
            BLOCK_ENTITIES.register("vapor_pneumatic_input_port", () ->
                    BlockEntityHelper.create(VaporPneumaticInputPortBlockEntity::new, ModBlocks.VAPOR_PNEUMATIC_INPUT_PORT.get()));

    public static final RegistrySupplier<BlockEntityType<ddraig.net.entropica.block.entity.RubberLogBlockEntity>> RUBBER_LOG_BE =
            BLOCK_ENTITIES.register("rubber_log", () ->
                    BlockEntityHelper.create(ddraig.net.entropica.block.entity.RubberLogBlockEntity::new, ModBlocks.RUBBER_LOG.get()));

    // --- MATERIA FUME PRESSURE VESSEL ---
    public static final RegistrySupplier<BlockEntityType<MateriaVesselControllerBlockEntity>> MATERIA_VESSEL_CONTROLLER_BE =
            BLOCK_ENTITIES.register("materia_vessel_controller", () ->
                    BlockEntityHelper.create(MateriaVesselControllerBlockEntity::new, ModBlocks.MATERIA_VESSEL_CONTROLLER.get()));

    public static final RegistrySupplier<BlockEntityType<MateriaVesselPortBlockEntity>> MATERIA_VESSEL_PORT_BE =
            BLOCK_ENTITIES.register("materia_vessel_port", () ->
                    BlockEntityHelper.create(MateriaVesselPortBlockEntity::new, ModBlocks.MATERIA_VESSEL_PORT.get()));

    // --- Runic Scribing & Research Block Entities ---
    public static final RegistrySupplier<BlockEntityType<ddraig.net.entropica.block.entity.ScribedChalkBlockEntity>> SCRIBED_CHALK_BE =
            BLOCK_ENTITIES.register("scribed_chalk", () ->
                    BlockEntityHelper.create(ddraig.net.entropica.block.entity.ScribedChalkBlockEntity::new, ModBlocks.SCRIBED_CHALK.get()));

    public static final RegistrySupplier<BlockEntityType<ddraig.net.entropica.block.entity.ScribingControllerBlockEntity>> SCRIBING_CONTROLLER_BE =
            BLOCK_ENTITIES.register("scribing_controller", () ->
                    BlockEntityHelper.create(ddraig.net.entropica.block.entity.ScribingControllerBlockEntity::new, ModBlocks.SCRIBING_CONTROLLER.get()));

    public static final RegistrySupplier<BlockEntityType<ddraig.net.entropica.block.entity.ViscanitePistonPressBlockEntity>> VISCANITE_PISTON_PRESS_BE =
            BLOCK_ENTITIES.register("viscanite_piston_press", () ->
                    BlockEntityHelper.create(ddraig.net.entropica.block.entity.ViscanitePistonPressBlockEntity::new, ModBlocks.VISCANITE_PISTON_PRESS.get()));

    public static final RegistrySupplier<BlockEntityType<ddraig.net.entropica.block.entity.DraftingTableBlockEntity>> DRAFTING_TABLE_BE =
            BLOCK_ENTITIES.register("drafting_table", () ->
                    BlockEntityHelper.create(ddraig.net.entropica.block.entity.DraftingTableBlockEntity::new, ModBlocks.DRAFTING_TABLE.get()));

    public static final RegistrySupplier<BlockEntityType<ddraig.net.entropica.block.entity.ResearchBenchBlockEntity>> RESEARCH_BENCH_BE =
            BLOCK_ENTITIES.register("research_bench", () ->
                    BlockEntityHelper.create(ddraig.net.entropica.block.entity.ResearchBenchBlockEntity::new, ModBlocks.RESEARCH_BENCH.get()));

    public static final RegistrySupplier<BlockEntityType<ddraig.net.entropica.block.entity.EssenceRepulsionWardBlockEntity>> ESSENCE_REPULSION_WARD_BE =
            BLOCK_ENTITIES.register("essence_repulsion_ward", () ->
                    BlockEntityHelper.create(ddraig.net.entropica.block.entity.EssenceRepulsionWardBlockEntity::new, ModBlocks.ESSENCE_REPULSION_WARD.get()));

    // --- Glass tiers grouped into single Block Entity ---
    public static final RegistrySupplier<BlockEntityType<MateriaEnrichedGlassBlockEntity>> MATERIA_ENRICHED_GLASS_BE =
            BLOCK_ENTITIES.register("materia_enriched_glass", () ->
                    BlockEntityHelper.create(MateriaEnrichedGlassBlockEntity::new,
                            ModBlocks.ESSENCE_ENRICHED_GLASS.get(),
                            ModBlocks.MATERIA_FUMUS_STRENGTHENED_GLASS.get(),
                            ModBlocks.MATERIA_LIQUIDA_ENRICHED_GLASS.get(),
                            ModBlocks.FRAGMENT_LATTICE_GLASS.get()
                    ));

    // --- MATERIA FUME PRESSURE CHAMBER ---
    public static final RegistrySupplier<BlockEntityType<MateriaPressureChamberControllerBlockEntity>> MATERIA_PRESSURE_CHAMBER_CONTROLLER_BE =
            BLOCK_ENTITIES.register("materia_pressure_chamber_controller", () ->
                    BlockEntityHelper.create(MateriaPressureChamberControllerBlockEntity::new, ModBlocks.MATERIA_PRESSURE_CHAMBER_CONTROLLER.get()));

    public static final RegistrySupplier<BlockEntityType<EnrichmentTableBlockEntity>> ENRICHMENT_TABLE_BE =
            BLOCK_ENTITIES.register("enrichment_table", () ->
                    BlockEntityHelper.create(EnrichmentTableBlockEntity::new, ModBlocks.ENRICHMENT_TABLE.get()));

    // --- AETHERIC SYNTHESIZER & AUTOMATOR ---
    public static final RegistrySupplier<BlockEntityType<AethericSynthesizerBlockEntity>> AETHERIC_SYNTHESIZER_BE =
            BLOCK_ENTITIES.register("aetheric_synthesizer", () ->
                    BlockEntityHelper.create(AethericSynthesizerBlockEntity::new, ModBlocks.AETHERIC_SYNTHESIZER.get()));

    public static final RegistrySupplier<BlockEntityType<AethericAutomatorBlockEntity>> AETHERIC_AUTOMATOR_BE =
            BLOCK_ENTITIES.register("aetheric_automator", () ->
                    BlockEntityHelper.create(AethericAutomatorBlockEntity::new, ModBlocks.AETHERIC_AUTOMATOR.get()));

    // --- EIDOLIC LATHE MULTIBLOCK ---
    public static final RegistrySupplier<BlockEntityType<EidolicLatheBlockEntity>> EIDOLIC_FOCAL_PEDESTAL_BE =
            BLOCK_ENTITIES.register("eidolic_focal_pedestal", () ->
                    BlockEntityHelper.create(EidolicLatheBlockEntity::new, ModBlocks.EIDOLIC_FOCAL_PEDESTAL.get()));

    public static final RegistrySupplier<BlockEntityType<AttunementPedestalBlockEntity>> ATTUNEMENT_PEDESTAL_BE =
            BLOCK_ENTITIES.register("attunement_pedestal", () ->
                    BlockEntityHelper.create(AttunementPedestalBlockEntity::new, ModBlocks.ATTUNEMENT_PEDESTAL.get()));

    // --- DILUTED ESSENCE FLUID ---
    public static final RegistrySupplier<BlockEntityType<DilutedEssenceFluidBlockEntity>> DILUTED_ESSENCE_FLUID_BE =
            BLOCK_ENTITIES.register("diluted_essence_fluid", () ->
                    BlockEntityHelper.create(DilutedEssenceFluidBlockEntity::new, ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get()));

    // --- VOID RIFTS ---
    public static final RegistrySupplier<BlockEntityType<VoidRiftBlockEntity>> VOID_RIFT_BE =
            BLOCK_ENTITIES.register("void_rift", () ->
                    BlockEntityHelper.create(VoidRiftBlockEntity::new, ModBlocks.VOID_RIFT.get()));

    // --- CRUCIBLE & RITUAL BOWLS ---
    public static final RegistrySupplier<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE_BE =
            BLOCK_ENTITIES.register("crucible", () ->
                    BlockEntityHelper.create(CrucibleBlockEntity::new, ModBlocks.CRUCIBLE.get()));

    public static final RegistrySupplier<BlockEntityType<RitualBowlBlockEntity>> RITUAL_BOWL_BE =
            BLOCK_ENTITIES.register("ritual_bowl", () ->
                    BlockEntityHelper.create(RitualBowlBlockEntity::new,
                            ModBlocks.MARBLE_RITUAL_BOWL.get(),
                            ModBlocks.BASALT_RITUAL_BOWL.get(),
                            ModBlocks.GRANITE_RITUAL_BOWL.get()
                    ));

    // --- MATERIA BLESSING ---
    public static final RegistrySupplier<BlockEntityType<MateriaBlessingBlockEntity>> MATERIA_BLESSING_BE =
            BLOCK_ENTITIES.register("materia_blessing", () ->
                    BlockEntityHelper.create(MateriaBlessingBlockEntity::new,
                            ModBlocks.MATERIA_BLESSING.get(),
                            ModBlocks.GREATER_MATERIA_BLESSING.get()));
}