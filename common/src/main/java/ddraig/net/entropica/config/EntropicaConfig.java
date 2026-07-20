package ddraig.net.entropica.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import java.util.function.Supplier;

public class EntropicaConfig {
    public static final Supplier<Integer> MATERIA_PER_ESSENCE = EntropicaConfig::getMateriaPerEssence;
    public static final Supplier<Integer> MATERIA_FURNACE_MAX_MANA = EntropicaConfig::getMateriaFurnaceMaxMateria;
    public static final Supplier<Integer> MATERIA_FURNACE_MAX_ESSENCE = EntropicaConfig::getMateriaFurnaceMaxEssence;
    public static final Supplier<Integer> ENTROPIC_CORE_MAX_MATERIA = EntropicaConfig::getEntropicCoreMaxMateria;
    public static final Supplier<Integer> RECEPTACLE_MAX_ESSENCE = EntropicaConfig::getReceptacleMaxEssence;
    public static final Supplier<Integer> CORE_PROCESS_TICK_RATE = EntropicaConfig::getCoreProcessTickRate;
    public static final Supplier<Boolean> ENABLE_CORE_OVERLOAD = EntropicaConfig::getEnableCoreOverload;
    public static final Supplier<Integer> ORBIS_CELL_MAX_MATERIA = EntropicaConfig::getOrbisCellMaxMateria;

    // Vis Fume Network
    public static final Supplier<Integer> COPPER_PIPE_CAPACITY = EntropicaConfig::getCopperPipeCapacity;
    public static final Supplier<Integer> COPPER_PIPE_TRANSFER_RATE = EntropicaConfig::getCopperPipeTransferRate;
    public static final Supplier<Integer> IRON_PIPE_CAPACITY = EntropicaConfig::getIronPipeCapacity;
    public static final Supplier<Integer> IRON_PIPE_TRANSFER_RATE = EntropicaConfig::getIronPipeTransferRate;
    public static final Supplier<Integer> DIAMOND_PIPE_CAPACITY = EntropicaConfig::getDiamondPipeCapacity;
    public static final Supplier<Integer> DIAMOND_PIPE_TRANSFER_RATE = EntropicaConfig::getDiamondPipeTransferRate;
    public static final Supplier<Integer> ARCANITE_PIPE_CAPACITY = EntropicaConfig::getArcanitePipeCapacity;
    public static final Supplier<Integer> ARCANITE_PIPE_TRANSFER_RATE = EntropicaConfig::getArcanitePipeTransferRate;
    public static final Supplier<Integer> RESONITE_PIPE_CAPACITY = EntropicaConfig::getResonitePipeCapacity;
    public static final Supplier<Integer> RESONITE_PIPE_TRANSFER_RATE = EntropicaConfig::getResonitePipeTransferRate;
    public static final Supplier<Integer> VISCANITE_PIPE_CAPACITY = EntropicaConfig::getViscanitePipeCapacity;
    public static final Supplier<Integer> VISCANITE_PIPE_TRANSFER_RATE = EntropicaConfig::getViscanitePipeTransferRate;
    public static final Supplier<Integer> CHARGED_ARCANITE_PIPE_CAPACITY = EntropicaConfig::getChargedArcanitePipeCapacity;
    public static final Supplier<Integer> CHARGED_ARCANITE_PIPE_TRANSFER_RATE = EntropicaConfig::getChargedArcanitePipeTransferRate;
    public static final Supplier<Integer> CHARGED_VISCANITE_PIPE_CAPACITY = EntropicaConfig::getChargedViscanitePipeCapacity;
    public static final Supplier<Integer> CHARGED_VISCANITE_PIPE_TRANSFER_RATE = EntropicaConfig::getChargedViscanitePipeTransferRate;
    public static final Supplier<Integer> GOLD_PIPE_CAPACITY = EntropicaConfig::getGoldPipeCapacity;
    public static final Supplier<Integer> GOLD_PIPE_TRANSFER_RATE = EntropicaConfig::getGoldPipeTransferRate;
    public static final Supplier<Integer> CHARGED_RESONITE_PIPE_CAPACITY = EntropicaConfig::getChargedResonitePipeCapacity;
    public static final Supplier<Integer> CHARGED_RESONITE_PIPE_TRANSFER_RATE = EntropicaConfig::getChargedResonitePipeTransferRate;

    public static final Supplier<Integer> MATERIA_FUMUS_TRANSFER_RATE = EntropicaConfig::getMateriaFumusTransferRate;
    public static final Supplier<Integer> MATERIA_FUMUS_TICK_RATE = EntropicaConfig::getMateriaFumusTickRate;
    public static final Supplier<Integer> VAPOR_PNEUMATIC_DIVERTER_CAPACITY = EntropicaConfig::getVaporPneumaticDiverterCapacity;
    public static final Supplier<Integer> VIS_FUME_OVERPRESSURE_LIMIT = EntropicaConfig::getVisFumeOverpressureLimit;
    public static final Supplier<Integer> CONDUIT_BASE_SAFE_CAPACITY = EntropicaConfig::getConduitBaseSafeCapacity;
    public static final Supplier<Double> CHARGED_CONDUIT_CAPACITY_MULTIPLIER = EntropicaConfig::getChargedConduitCapacityMultiplier;
    public static final Supplier<Integer> CONDUIT_UNCHARGED_TRANSFER_RATE = EntropicaConfig::getConduitUnchargedTransferRate;
    public static final Supplier<Integer> CONDUIT_CHARGED_TRANSFER_RATE = EntropicaConfig::getConduitChargedTransferRate;

    public static final Supplier<Integer> CAPUTITE_PIPE_CAPACITY = EntropicaConfig::getCaputitePipeCapacity;
    public static final Supplier<Integer> CAPUTITE_PIPE_TRANSFER_RATE = EntropicaConfig::getCaputitePipeTransferRate;
    public static final Supplier<Integer> CHARGED_CAPUTITE_PIPE_CAPACITY = EntropicaConfig::getChargedCaputitePipeCapacity;
    public static final Supplier<Integer> CHARGED_CAPUTITE_PIPE_TRANSFER_RATE = EntropicaConfig::getChargedCaputitePipeTransferRate;

    public static final Supplier<Integer> SANGUINITE_PIPE_CAPACITY = EntropicaConfig::getSanguinitePipeCapacity;
    public static final Supplier<Integer> SANGUINITE_PIPE_TRANSFER_RATE = EntropicaConfig::getSanguinitePipeTransferRate;
    public static final Supplier<Integer> CHARGED_SANGUINITE_PIPE_CAPACITY = EntropicaConfig::getChargedSanguinitePipeCapacity;
    public static final Supplier<Integer> CHARGED_SANGUINITE_PIPE_TRANSFER_RATE = EntropicaConfig::getChargedSanguinitePipeTransferRate;

    public static final Supplier<Integer> MERCURITE_PIPE_CAPACITY = EntropicaConfig::getMercuritePipeCapacity;
    public static final Supplier<Integer> MERCURITE_PIPE_TRANSFER_RATE = EntropicaConfig::getMercuritePipeTransferRate;
    public static final Supplier<Integer> CHARGED_MERCURITE_PIPE_CAPACITY = EntropicaConfig::getChargedMercuritePipeCapacity;
    public static final Supplier<Integer> CHARGED_MERCURITE_PIPE_TRANSFER_RATE = EntropicaConfig::getChargedMercuritePipeTransferRate;

    public static final Supplier<Integer> EUCLIDITE_PIPE_CAPACITY = EntropicaConfig::getEucliditePipeCapacity;
    public static final Supplier<Integer> EUCLIDITE_PIPE_TRANSFER_RATE = EntropicaConfig::getEucliditePipeTransferRate;
    public static final Supplier<Integer> CHARGED_EUCLIDITE_PIPE_CAPACITY = EntropicaConfig::getChargedEucliditePipeCapacity;
    public static final Supplier<Integer> CHARGED_EUCLIDITE_PIPE_TRANSFER_RATE = EntropicaConfig::getChargedEucliditePipeTransferRate;

    public static final Supplier<Integer> ATHANORITE_PIPE_CAPACITY = EntropicaConfig::getAthanoritePipeCapacity;
    public static final Supplier<Integer> ATHANORITE_PIPE_TRANSFER_RATE = EntropicaConfig::getAthanoritePipeTransferRate;
    public static final Supplier<Integer> CHARGED_ATHANORITE_PIPE_CAPACITY = EntropicaConfig::getChargedAthanoritePipeCapacity;
    public static final Supplier<Integer> CHARGED_ATHANORITE_PIPE_TRANSFER_RATE = EntropicaConfig::getChargedAthanoritePipeTransferRate;

    // Vessel & Chamber
    public static final Supplier<Integer> VESSEL_GLASS_BASE_CAPACITY = EntropicaConfig::getVesselGlassBaseCapacity;
    public static final Supplier<Integer> VESSEL_GLASS_STRENGTHENED_CAPACITY = EntropicaConfig::getVesselGlassStrengthenedCapacity;
    public static final Supplier<Integer> VESSEL_GLASS_ICHOR_CAPACITY = EntropicaConfig::getVesselGlassIchorCapacity;
    public static final Supplier<Integer> VESSEL_GLASS_LATTICE_CAPACITY = EntropicaConfig::getVesselGlassLatticeCapacity;
    public static final Supplier<Double> VESSEL_CAPACITY_DECAY_MULTIPLIER = EntropicaConfig::getVesselCapacityDecayMultiplier;
    public static final Supplier<Integer> VESSEL_DECAY_STARTING_BLOCK = EntropicaConfig::getVesselDecayStartingBlock;
    public static final Supplier<Double> CHAMBER_PARTICLE_DENSITY = EntropicaConfig::getChamberParticleDensity;

    // Diluted Essence
    public static final Supplier<Integer> DILUTED_ESSENCE_BATCH_LIMIT = EntropicaConfig::getDilutedEssenceBatchLimit;
    public static final Supplier<Integer> DILUTED_ESSENCE_MAX_CHARGE = EntropicaConfig::getDilutedEssenceMaxCharge;

    // Essence Nodes
    public static final Supplier<Boolean> NODE_LIFETIME_ENABLED = EntropicaConfig::getNodeLifetimeEnabled;
    public static final Supplier<Integer> NODE_LIFETIME_TICKS = EntropicaConfig::getNodeLifetimeTicks;
    public static final Supplier<Boolean> NODE_RESPAWN_ENABLED = EntropicaConfig::getNodeRespawnEnabled;
    public static final Supplier<Integer> NODE_MIN_CAPACITY = EntropicaConfig::getNodeMinCapacity;
    public static final Supplier<Integer> NODE_MAX_CAPACITY = EntropicaConfig::getNodeMaxCapacity;
    public static final Supplier<Boolean> NODE_INFINITE_CAPACITY = EntropicaConfig::getNodeInfiniteCapacity;
    public static final Supplier<Integer> NODE_SPAWN_SPACING = EntropicaConfig::getNodeSpawnSpacing;
    public static final Supplier<Integer> NODE_MAX_PER_AREA = EntropicaConfig::getNodeMaxPerArea;

    // Essence Repulsion Wards
    public static final Supplier<Integer> WARD_CONSUMPTION_AMOUNT = EntropicaConfig::getWardConsumptionAmount;
    public static final Supplier<Integer> WARD_CONSUMPTION_INTERVAL_SECONDS = EntropicaConfig::getWardConsumptionIntervalSeconds;
    public static final Supplier<Double> WARD_CIRCLE_RADIUS_BASE = EntropicaConfig::getWardCircleRadiusBase;
    public static final Supplier<Double> WARD_BLOCK_RADIUS = EntropicaConfig::getWardBlockRadius;

    @ExpectPlatform public static int getMateriaPerEssence() { throw new AssertionError(); }
    @ExpectPlatform public static int getMateriaFurnaceMaxMateria() { throw new AssertionError(); }
    @ExpectPlatform public static int getMateriaFurnaceMaxEssence() { throw new AssertionError(); }
    @ExpectPlatform public static int getEntropicCoreMaxMateria() { throw new AssertionError(); }
    @ExpectPlatform public static int getReceptacleMaxEssence() { throw new AssertionError(); }
    @ExpectPlatform public static int getCoreProcessTickRate() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableCoreOverload() { throw new AssertionError(); }
    @ExpectPlatform public static int getOrbisCellMaxMateria() { throw new AssertionError(); }

    @ExpectPlatform public static int getCopperPipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getCopperPipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getIronPipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getIronPipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getDiamondPipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getDiamondPipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getArcanitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getArcanitePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getResonitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getResonitePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getViscanitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getViscanitePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedArcanitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedArcanitePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedViscanitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedViscanitePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getGoldPipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getGoldPipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedResonitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedResonitePipeTransferRate() { throw new AssertionError(); }

    @ExpectPlatform public static int getMateriaFumusTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getMateriaFumusTickRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getVaporPneumaticDiverterCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getVisFumeOverpressureLimit() { throw new AssertionError(); }

    @ExpectPlatform public static int getVesselGlassBaseCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getVesselGlassStrengthenedCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getVesselGlassIchorCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getVesselGlassLatticeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static double getVesselCapacityDecayMultiplier() { throw new AssertionError(); }
    @ExpectPlatform public static int getVesselDecayStartingBlock() { throw new AssertionError(); }
    @ExpectPlatform public static double getChamberParticleDensity() { throw new AssertionError(); }

    @ExpectPlatform public static int getDilutedEssenceBatchLimit() { throw new AssertionError(); }
    @ExpectPlatform public static int getDilutedEssenceMaxCharge() { throw new AssertionError(); }

    @ExpectPlatform public static boolean getNodeLifetimeEnabled() { throw new AssertionError(); }
    @ExpectPlatform public static int getNodeLifetimeTicks() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getNodeRespawnEnabled() { throw new AssertionError(); }
    @ExpectPlatform public static int getNodeMinCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getNodeMaxCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getNodeInfiniteCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getNodeSpawnSpacing() { throw new AssertionError(); }
    @ExpectPlatform public static int getNodeMaxPerArea() { throw new AssertionError(); }

    @ExpectPlatform public static int getConduitBaseSafeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static double getChargedConduitCapacityMultiplier() { throw new AssertionError(); }
    @ExpectPlatform public static int getConduitUnchargedTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getConduitChargedTransferRate() { throw new AssertionError(); }

    @ExpectPlatform public static int getCaputitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getCaputitePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedCaputitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedCaputitePipeTransferRate() { throw new AssertionError(); }

    @ExpectPlatform public static int getSanguinitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getSanguinitePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedSanguinitePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedSanguinitePipeTransferRate() { throw new AssertionError(); }

    @ExpectPlatform public static int getMercuritePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getMercuritePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedMercuritePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedMercuritePipeTransferRate() { throw new AssertionError(); }

    @ExpectPlatform public static int getEucliditePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getEucliditePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedEucliditePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedEucliditePipeTransferRate() { throw new AssertionError(); }

    @ExpectPlatform public static int getAthanoritePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getAthanoritePipeTransferRate() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedAthanoritePipeCapacity() { throw new AssertionError(); }
    @ExpectPlatform public static int getChargedAthanoritePipeTransferRate() { throw new AssertionError(); }

    @ExpectPlatform public static int getWardConsumptionAmount() { throw new AssertionError(); }
    @ExpectPlatform public static int getWardConsumptionIntervalSeconds() { throw new AssertionError(); }
    @ExpectPlatform public static double getWardCircleRadiusBase() { throw new AssertionError(); }
    @ExpectPlatform public static double getWardBlockRadius() { throw new AssertionError(); }
}
