package ddraig.net.entropica.config.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class EntropicaConfigImpl {

    public static class IntOption {
        public String description;
        public int min;
        public int max;
        public int defaultValue;
        public int value;

        public IntOption(String description, int min, int max, int defaultValue) {
            this.description = description;
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public void validate() {
            if (value < min) value = min;
            if (value > max) value = max;
        }
    }

    public static class DoubleOption {
        public String description;
        public double min;
        public double max;
        public double defaultValue;
        public double value;

        public DoubleOption(String description, double min, double max, double defaultValue) {
            this.description = description;
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public void validate() {
            if (value < min) value = min;
            if (value > max) value = max;
        }
    }

    public static class BooleanOption {
        public String description;
        public boolean defaultValue;
        public boolean value;

        public BooleanOption(String description, boolean defaultValue) {
            this.description = description;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public void validate() {
            // No validation needed for boolean
        }
    }

    public static class EntropicaConfigData {
        public IntOption materiaPerEssence = new IntOption("The amount of Materia produced per 1 Essence consumed every 5 ticks. Min: 1, Max: 1000, Default: 8", 1, 1000, 8);
        public IntOption materiaFurnaceMaxMateria = new IntOption("Maximum materia storage capacity for the standalone Materia Furnace. Min: 1, Max: 2147483647, Default: 1024", 1, Integer.MAX_VALUE, 1024);
        public IntOption materiaFurnaceMaxEssence = new IntOption("Maximum essence storage capacity for the standalone Materia Furnace. Capped at 32. Min: 1, Max: 32, Default: 32", 1, 32, 32);
        public IntOption entropicCoreMaxMateria = new IntOption("Maximum materia storage capacity for the Entropic Materia Furnace multiblock. Min: 1, Max: 2147483647, Default: 100000", 1, Integer.MAX_VALUE, 100000);
        public IntOption receptacleMaxEssence = new IntOption("The amount of essence storage capacity added per Essence Receptacle in the multiblock. Min: 1, Max: 2147483647, Default: 64", 1, Integer.MAX_VALUE, 64);
        public IntOption coreProcessTickRate = new IntOption("How many ticks between each processing operation of the furnace (20 = 1 second, 5 = 1/4th second). Min: 1, Max: 200, Default: 5", 1, 200, 5);
        public BooleanOption enableCoreOverload = new BooleanOption("If true, the Entropic Core will eventually explode if left active while its Materia Buffer is 100% full. Default: true", true);
        public BooleanOption requireResearchToCraft = new BooleanOption("If true, players must unlock research in the Entropic Codex to craft associated items. Default: true", true);
        public BooleanOption fancyMagicCircleProcessing = new BooleanOption("If true, enables complex raising, growing, fading, and cascading smash animations during magic circle ritual processing. Default: true", true);
        // Astral Mirror Settings
        public BooleanOption enableAstralMirrorSkyReflection = new BooleanOption("If true, Astral Mirror Blocks render real-time sky, nebula, and constellation reflections. Default: true", true);
        public BooleanOption enableAstralMirrorParallaxDepth = new BooleanOption("If true, reflections render with 3D multi-layer parallax cosmic well depth (Option 1). Default: true", true);
        public BooleanOption enableAstralMirror3DBillboardStars = new BooleanOption("If true, reflected stars render as camera-facing 3D spherical billboarding orbs (Option 2). Default: true", true);
        public BooleanOption enableAstralMirrorLiquidRefraction = new BooleanOption("If true, reflected starlight undergoes liquid ether fluid wave refraction and surface caustic ripples (Option 4). Default: true", true);
        public BooleanOption enableAstralMirrorNebulae = new BooleanOption("If true, reflected cosmic nebulae clouds are rendered in the mirror pool. Default: true", true);
        public BooleanOption enableAstralMirrorConstellationLines = new BooleanOption("If true, player-charted constellation connection lines are reflected in the mirror pool. Default: true", true);
        public IntOption orbisCellMaxMateria = new IntOption("Maximum materia storage capacity for a single Orbis Cell. Min: 1, Max: 2147483647, Default: 10000", 1, Integer.MAX_VALUE, 10000);

        // Materia Fume Network
        public IntOption copperPipeCapacity = new IntOption("Safe capacity (1.0 Pressure) for Copper Fume Pipes. Min: 1, Max: 2147483647, Default: 20", 1, Integer.MAX_VALUE, 20);
        public IntOption copperPipeTransferRate = new IntOption("Transfer rate for Copper Fume Pipes. Min: 1, Max: 2147483647, Default: 5", 1, Integer.MAX_VALUE, 5);
        public IntOption ironPipeCapacity = new IntOption("Safe capacity (1.0 Pressure) for Iron Fume Pipes. Min: 1, Max: 2147483647, Default: 40", 1, Integer.MAX_VALUE, 40);
        public IntOption ironPipeTransferRate = new IntOption("Transfer rate for Iron Fume Pipes. Min: 1, Max: 2147483647, Default: 10", 1, Integer.MAX_VALUE, 10);
        public IntOption diamondPipeCapacity = new IntOption("Safe capacity (1.0 Pressure) for Diamond Fume Pipes. Min: 1, Max: 2147483647, Default: 250", 1, Integer.MAX_VALUE, 250);
        public IntOption diamondPipeTransferRate = new IntOption("Transfer rate for Diamond Fume Pipes. Min: 1, Max: 2147483647, Default: 40", 1, Integer.MAX_VALUE, 40);
        public IntOption arcanitePipeCapacity = new IntOption("Safe capacity (1.0 Pressure) for Arcanite Fume Pipes. Min: 1, Max: 2147483647, Default: 100", 1, Integer.MAX_VALUE, 100);
        public IntOption arcanitePipeTransferRate = new IntOption("Transfer rate for Arcanite Fume Pipes. Min: 1, Max: 2147483647, Default: 25", 1, Integer.MAX_VALUE, 25);
        public IntOption resonitePipeCapacity = new IntOption("Safe capacity (1.0 Pressure) for Resonite Fume Pipes. Min: 1, Max: 2147483647, Default: 300", 1, Integer.MAX_VALUE, 300);
        public IntOption resonitePipeTransferRate = new IntOption("Transfer rate for Resonite Fume Pipes. Min: 1, Max: 2147483647, Default: 50", 1, Integer.MAX_VALUE, 50);
        public IntOption viscanitePipeCapacity = new IntOption("Safe capacity (1.0 Pressure) for Viscanite Fume Pipes. Min: 1, Max: 2147483647, Default: 300", 1, Integer.MAX_VALUE, 300);
        public IntOption viscanitePipeTransferRate = new IntOption("Transfer rate for Viscanite Fume Pipes. Min: 1, Max: 2147483647, Default: 100", 1, Integer.MAX_VALUE, 100);
        public IntOption chargedArcanitePipeCapacity = new IntOption("Safe capacity (1.0 Pressure) for Charged Arcanite Fume Pipes. Min: 1, Max: 2147483647, Default: 200", 1, Integer.MAX_VALUE, 200);
        public IntOption chargedArcanitePipeTransferRate = new IntOption("Transfer rate for Charged Arcanite Fume Pipes. Min: 1, Max: 2147483647, Default: 500", 1, Integer.MAX_VALUE, 500);
        public IntOption chargedViscanitePipeCapacity = new IntOption("Safe capacity (1.0 Pressure) for Charged Viscanite Fume Pipes. Min: 1, Max: 2147483647, Default: 500", 1, Integer.MAX_VALUE, 500);
        public IntOption chargedViscanitePipeTransferRate = new IntOption("Transfer rate for Charged Viscanite Fume Pipes. Min: 1, Max: 2147483647, Default: 1000", 1, Integer.MAX_VALUE, 1000);
        public IntOption goldPipeCapacity = new IntOption("Capacity for Gold Vapor Pneumatic Pipe. Min: 1, Max: 2147483647, Default: 80", 1, Integer.MAX_VALUE, 80);
        public IntOption goldPipeTransferRate = new IntOption("Transfer rate for Gold Vapor Pneumatic Pipe. Min: 1, Max: 2147483647, Default: 20", 1, Integer.MAX_VALUE, 20);
        public IntOption chargedResonitePipeCapacity = new IntOption("Capacity for Charged Resonite Vapor Pneumatic Pipe. Min: 1, Max: 2147483647, Default: 500", 1, Integer.MAX_VALUE, 500);
        public IntOption chargedResonitePipeTransferRate = new IntOption("Transfer rate for Charged Resonite Vapor Pneumatic Pipe. Min: 1, Max: 2147483647, Default: 5000", 1, Integer.MAX_VALUE, 5000);

        public IntOption conduitBaseSafeCapacity = new IntOption("Base safe capacity for all uncharged conduits. Min: 1, Default: 4000", 1, Integer.MAX_VALUE, 4000);
        public DoubleOption chargedConduitCapacityMultiplier = new DoubleOption("Capacity multiplier for all charged conduits. Default: 1.25", 0.01, 1000.0, 1.25);
        public IntOption conduitUnchargedTransferRate = new IntOption("Base transfer rate for uncharged conduits. Default: 20", 1, Integer.MAX_VALUE, 20);
        public IntOption conduitChargedTransferRate = new IntOption("Base transfer rate for charged conduits. Default: 200", 1, Integer.MAX_VALUE, 200);

        public IntOption caputitePipeCapacity = new IntOption("Safe capacity for Caputite Fume Pipes. Default: 8000", 1, Integer.MAX_VALUE, 8000);
        public IntOption caputitePipeTransferRate = new IntOption("Transfer rate for Caputite Fume Pipes. Default: 40", 1, Integer.MAX_VALUE, 40);
        public IntOption chargedCaputitePipeCapacity = new IntOption("Safe capacity for Charged Caputite Fume Pipes. Default: 24000", 1, Integer.MAX_VALUE, 24000);
        public IntOption chargedCaputitePipeTransferRate = new IntOption("Transfer rate for Charged Caputite Fume Pipes. Default: 400", 1, Integer.MAX_VALUE, 400);

        public IntOption sanguinitePipeCapacity = new IntOption("Safe capacity for Sanguinite Fume Pipes. Default: 16000", 1, Integer.MAX_VALUE, 16000);
        public IntOption sanguinitePipeTransferRate = new IntOption("Transfer rate for Sanguinite Fume Pipes. Default: 80", 1, Integer.MAX_VALUE, 80);
        public IntOption chargedSanguinitePipeCapacity = new IntOption("Safe capacity for Charged Sanguinite Fume Pipes. Default: 48000", 1, Integer.MAX_VALUE, 48000);
        public IntOption chargedSanguinitePipeTransferRate = new IntOption("Transfer rate for Charged Sanguinite Fume Pipes. Default: 800", 1, Integer.MAX_VALUE, 800);

        public IntOption mercuritePipeCapacity = new IntOption("Safe capacity for Mercurite Fume Pipes. Default: 32000", 1, Integer.MAX_VALUE, 32000);
        public IntOption mercuritePipeTransferRate = new IntOption("Transfer rate for Mercurite Fume Pipes. Default: 160", 1, Integer.MAX_VALUE, 160);
        public IntOption chargedMercuritePipeCapacity = new IntOption("Safe capacity for Charged Mercurite Fume Pipes. Default: 96000", 1, Integer.MAX_VALUE, 96000);
        public IntOption chargedMercuritePipeTransferRate = new IntOption("Transfer rate for Charged Mercurite Fume Pipes. Default: 1600", 1, Integer.MAX_VALUE, 1600);

        public IntOption eucliditePipeCapacity = new IntOption("Safe capacity for Euclidite Fume Pipes. Default: 64000", 1, Integer.MAX_VALUE, 64000);
        public IntOption eucliditePipeTransferRate = new IntOption("Transfer rate for Euclidite Fume Pipes. Default: 320", 1, Integer.MAX_VALUE, 320);
        public IntOption chargedEucliditePipeCapacity = new IntOption("Safe capacity for Charged Euclidite Fume Pipes. Default: 192000", 1, Integer.MAX_VALUE, 192000);
        public IntOption chargedEucliditePipeTransferRate = new IntOption("Transfer rate for Charged Euclidite Fume Pipes. Default: 3200", 1, Integer.MAX_VALUE, 3200);

        public IntOption athanoritePipeCapacity = new IntOption("Safe capacity for Athanorite Fume Pipes. Default: 128000", 1, Integer.MAX_VALUE, 128000);
        public IntOption athanoritePipeTransferRate = new IntOption("Transfer rate for Athanorite Fume Pipes. Default: 640", 1, Integer.MAX_VALUE, 640);
        public IntOption chargedAthanoritePipeCapacity = new IntOption("Safe capacity for Charged Athanorite Fume Pipes. Default: 384000", 1, Integer.MAX_VALUE, 384000);
        public IntOption chargedAthanoritePipeTransferRate = new IntOption("Transfer rate for Charged Athanorite Fume Pipes. Default: 6400", 1, Integer.MAX_VALUE, 6400);

        public IntOption materiaFumusTransferRate = new IntOption("Base transfer rate for generators and machines pushing Fumes into the network. Min: 1, Max: 2147483647, Default: 100", 1, Integer.MAX_VALUE, 100);
        public IntOption materiaFumusTickRate = new IntOption("How many ticks between each gas transfer operation in the pipe network. Min: 1, Max: 200, Default: 5", 1, 200, 5);
        public IntOption vaporPneumaticDiverterCapacity = new IntOption("Maximum amount of Materia Fumes a Diverter block can safely process. Min: 1, Max: 2147483647, Default: 2500", 1, Integer.MAX_VALUE, 2500);
        public IntOption visFumeOverpressureLimit = new IntOption("The amount of Fumes inside a Diverter that will cause it to break from overpressure. Min: 1, Max: 2147483647, Default: 2000", 1, Integer.MAX_VALUE, 2000);

        // Vessel & Chamber
        public IntOption vesselGlassBaseCapacity = new IntOption("Capacity added by standard Base Essence Enriched Glass. Min: 1, Max: 2147483647, Default: 1000", 1, Integer.MAX_VALUE, 1000);
        public IntOption vesselGlassStrengthenedCapacity = new IntOption("Capacity added by Materia Fume Strengthened Glass. Min: 1, Max: 2147483647, Default: 2500", 1, Integer.MAX_VALUE, 2500);
        public IntOption vesselGlassIchorCapacity = new IntOption("Capacity added by Materia Ichor Enriched Glass. Min: 1, Max: 2147483647, Default: 5000", 1, Integer.MAX_VALUE, 5000);
        public IntOption vesselGlassLatticeCapacity = new IntOption("Capacity added by Fragment Lattice Glass. Min: 1, Max: 2147483647, Default: 10000", 1, Integer.MAX_VALUE, 10000);
        public DoubleOption vesselCapacityDecayMultiplier = new DoubleOption("The diminishing returns multiplier applied to each subsequent glass block. (0.95 = 5% loss per block). Min: 0.01, Max: 1.0, Default: 0.95", 0.01, 1.0, 0.95);
        public IntOption vesselDecayStartingBlock = new IntOption("The amount of glass blocks placed before the diminishing returns multiplier starts applying. Min: 0, Max: 2147483647, Default: 0", 0, Integer.MAX_VALUE, 0);
        public DoubleOption chamberParticleDensity = new DoubleOption("(COSMETIC) Multiplier for the amount of gas particles inside the Pressure Chamber. 0.0 turns them off. Min: 0.0, Max: 10.0, Default: 1.0", 0.0, 10.0, 1.0);

        // Diluted Essence
        public IntOption dilutedEssenceBatchLimit = new IntOption("The maximum number of items the Diluted Essence fluid can process at one time. Min: 1, Max: 16, Default: 4", 1, 16, 4);
        public IntOption dilutedEssenceMaxCharge = new IntOption("The maximum amount of charge a Diluted Essence fluid block can hold. Min: 1, Max: 128, Default: 32", 1, 128, 32);

        // Essence Nodes
        public BooleanOption nodeLifetimeEnabled = new BooleanOption("Should natural Essence Nodes fizzle out after a certain amount of time? Default: true", true);
        public IntOption nodeLifetimeTicks = new IntOption("How long (in ticks) should a natural Essence Node live before fizzling? Min: 20, Max: 2147483647, Default: 24000", 20, Integer.MAX_VALUE, 24000);
        public BooleanOption nodeRespawnEnabled = new BooleanOption("Should a new Essence Node naturally spawn in the same chunk when an old one fizzles out? Default: true", true);
        public IntOption nodeMinCapacity = new IntOption("Minimum amount of essence a natural node can spawn with. Min: 1, Max: 2147483647, Default: 50", 1, Integer.MAX_VALUE, 50);
        public IntOption nodeMaxCapacity = new IntOption("Maximum amount of essence a natural node can spawn with. Min: 1, Max: 2147483647, Default: 500", 1, Integer.MAX_VALUE, 500);
        public BooleanOption nodeInfiniteCapacity = new BooleanOption("If true, Essence Nodes never run out of essence from extraction. Default: false", false);
        public IntOption nodeSpawnSpacing = new IntOption("Minimum chunk distance between naturally spawning nodes. Min: 1, Max: 100, Default: 6", 1, 100, 6);
        public IntOption nodeMaxPerArea = new IntOption("Maximum number of nodes allowed in a 10x10 chunk area. Min: 1, Max: 100, Default: 10", 1, 100, 10);
        public IntOption extractionNodeCooldown = new IntOption("Default cooldown in ticks for Extraction Nodes when transferring essence. Min: 1, Max: 1000, Default: 40", 1, 1000, 40);

        // Essence Repulsion Wards
        public IntOption wardConsumptionAmount = new IntOption("Amount of Fumus consumed per consumption tick. Min: 1, Default: 1", 1, Integer.MAX_VALUE, 1);
        public IntOption wardConsumptionIntervalSeconds = new IntOption("How many seconds between Fumus consumption operations. Min: 1, Default: 120", 1, Integer.MAX_VALUE, 120);
        public DoubleOption wardCircleRadiusBase = new DoubleOption("Base radius for magic circle wards (increases per circle tier). Min: 1.0, Default: 10.0", 1.0, 100.0, 10.0);
        public DoubleOption wardBlockRadius = new DoubleOption("Radius of the dedicated repulsion ward block. Min: 1.0, Default: 15.0", 1.0, 100.0, 15.0);
        public IntOption maxDelayTicks = new IntOption("Maximum delay ticks allowed for a Delay Node. Min: 1, Max: 100, Default: 6", 1, 100, 6);
        public IntOption astralCollectorTransferRate = new IntOption("Materia collection rate for Astral Collector under focused beam (Materia per second). Min: 1, Max: 100, Default: 8", 1, 100, 8);

        public void validateAll() {
            materiaPerEssence.validate();
            materiaFurnaceMaxMateria.validate();
            materiaFurnaceMaxEssence.validate();
            entropicCoreMaxMateria.validate();
            receptacleMaxEssence.validate();
            coreProcessTickRate.validate();
            enableCoreOverload.validate();
            orbisCellMaxMateria.validate();
            copperPipeCapacity.validate();
            copperPipeTransferRate.validate();
            ironPipeCapacity.validate();
            ironPipeTransferRate.validate();
            diamondPipeCapacity.validate();
            diamondPipeTransferRate.validate();
            arcanitePipeCapacity.validate();
            arcanitePipeTransferRate.validate();
            resonitePipeCapacity.validate();
            resonitePipeTransferRate.validate();
            viscanitePipeCapacity.validate();
            viscanitePipeTransferRate.validate();
            chargedArcanitePipeCapacity.validate();
            chargedArcanitePipeTransferRate.validate();
            chargedViscanitePipeCapacity.validate();
            chargedViscanitePipeTransferRate.validate();
            goldPipeCapacity.validate();
            goldPipeTransferRate.validate();
            chargedResonitePipeCapacity.validate();
            chargedResonitePipeTransferRate.validate();

            conduitBaseSafeCapacity.validate();
            chargedConduitCapacityMultiplier.validate();
            conduitUnchargedTransferRate.validate();
            conduitChargedTransferRate.validate();

            caputitePipeCapacity.validate();
            caputitePipeTransferRate.validate();
            chargedCaputitePipeCapacity.validate();
            chargedCaputitePipeTransferRate.validate();

            sanguinitePipeCapacity.validate();
            sanguinitePipeTransferRate.validate();
            chargedSanguinitePipeCapacity.validate();
            chargedSanguinitePipeTransferRate.validate();

            mercuritePipeCapacity.validate();
            mercuritePipeTransferRate.validate();
            chargedMercuritePipeCapacity.validate();
            chargedMercuritePipeTransferRate.validate();

            eucliditePipeCapacity.validate();
            eucliditePipeTransferRate.validate();
            chargedEucliditePipeCapacity.validate();
            chargedEucliditePipeTransferRate.validate();

            athanoritePipeCapacity.validate();
            athanoritePipeTransferRate.validate();
            chargedAthanoritePipeCapacity.validate();
            chargedAthanoritePipeTransferRate.validate();
            materiaFumusTransferRate.validate();
            materiaFumusTickRate.validate();
            vaporPneumaticDiverterCapacity.validate();
            visFumeOverpressureLimit.validate();
            vesselGlassBaseCapacity.validate();
            vesselGlassStrengthenedCapacity.validate();
            vesselGlassIchorCapacity.validate();
            vesselGlassLatticeCapacity.validate();
            vesselCapacityDecayMultiplier.validate();
            vesselDecayStartingBlock.validate();
            chamberParticleDensity.validate();
            dilutedEssenceBatchLimit.validate();
            dilutedEssenceMaxCharge.validate();
            nodeLifetimeEnabled.validate();
            nodeLifetimeTicks.validate();
            nodeRespawnEnabled.validate();
            nodeMinCapacity.validate();
            nodeMaxCapacity.validate();
            nodeInfiniteCapacity.validate();
            nodeSpawnSpacing.validate();
            nodeMaxPerArea.validate();
            extractionNodeCooldown.validate();
            wardConsumptionAmount.validate();
            wardConsumptionIntervalSeconds.validate();
            wardCircleRadiusBase.validate();
            wardBlockRadius.validate();
            maxDelayTicks.validate();
            astralCollectorTransferRate.validate();
            requireResearchToCraft.validate();
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static EntropicaConfigData data = new EntropicaConfigData();

    static {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("entropica.json");
        try {
            if (Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    EntropicaConfigData loaded = GSON.fromJson(reader, EntropicaConfigData.class);
                    if (loaded != null) {
                        data = loaded;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load Entropica config: " + e.getMessage());
        }
        data.validateAll();
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.err.println("Failed to save Entropica config: " + e.getMessage());
        }
    }

    public static int getMateriaPerEssence() { return data.materiaPerEssence.value; }
    public static int getMateriaFurnaceMaxMateria() { return data.materiaFurnaceMaxMateria.value; }
    public static int getMateriaFurnaceMaxEssence() { return data.materiaFurnaceMaxEssence.value; }
    public static int getEntropicCoreMaxMateria() { return data.entropicCoreMaxMateria.value; }
    public static int getReceptacleMaxEssence() { return data.receptacleMaxEssence.value; }
    public static int getCoreProcessTickRate() { return data.coreProcessTickRate.value; }
    public static boolean getEnableCoreOverload() { return data.enableCoreOverload.value; }
    public static boolean getRequireResearchToCraft() { return data.requireResearchToCraft.value; }
    public static int getOrbisCellMaxMateria() { return data.orbisCellMaxMateria.value; }


    public static int getCopperPipeCapacity() { return data.copperPipeCapacity.value; }
    public static int getCopperPipeTransferRate() { return data.copperPipeTransferRate.value; }
    public static int getIronPipeCapacity() { return data.ironPipeCapacity.value; }
    public static int getIronPipeTransferRate() { return data.ironPipeTransferRate.value; }
    public static int getDiamondPipeCapacity() { return data.diamondPipeCapacity.value; }
    public static int getDiamondPipeTransferRate() { return data.diamondPipeTransferRate.value; }
    public static int getArcanitePipeCapacity() { return data.arcanitePipeCapacity.value; }
    public static int getArcanitePipeTransferRate() { return data.arcanitePipeTransferRate.value; }
    public static int getResonitePipeCapacity() { return data.resonitePipeCapacity.value; }
    public static int getResonitePipeTransferRate() { return data.resonitePipeTransferRate.value; }
    public static int getViscanitePipeCapacity() { return data.viscanitePipeCapacity.value; }
    public static int getViscanitePipeTransferRate() { return data.viscanitePipeTransferRate.value; }
    public static int getChargedArcanitePipeCapacity() { return data.chargedArcanitePipeCapacity.value; }
    public static int getChargedArcanitePipeTransferRate() { return data.chargedArcanitePipeTransferRate.value; }
    public static int getChargedViscanitePipeCapacity() { return data.chargedViscanitePipeCapacity.value; }
    public static int getChargedViscanitePipeTransferRate() { return data.chargedViscanitePipeTransferRate.value; }
    public static int getGoldPipeCapacity() { return data.goldPipeCapacity.value; }
    public static int getGoldPipeTransferRate() { return data.goldPipeTransferRate.value; }
    public static int getChargedResonitePipeCapacity() { return data.chargedResonitePipeCapacity.value; }
    public static int getChargedResonitePipeTransferRate() { return data.chargedResonitePipeTransferRate.value; }

    public static int getMateriaFumusTransferRate() { return data.materiaFumusTransferRate.value; }
    public static int getMateriaFumusTickRate() { return data.materiaFumusTickRate.value; }
    public static int getVaporPneumaticDiverterCapacity() { return data.vaporPneumaticDiverterCapacity.value; }
    public static int getMateriaFumeOverpressureLimit() { return data.visFumeOverpressureLimit.value; }

    public static int getVesselGlassBaseCapacity() { return data.vesselGlassBaseCapacity.value; }
    public static int getVesselGlassStrengthenedCapacity() { return data.vesselGlassStrengthenedCapacity.value; }
    public static int getVesselGlassIchorCapacity() { return data.vesselGlassIchorCapacity.value; }
    public static int getVesselGlassLatticeCapacity() { return data.vesselGlassLatticeCapacity.value; }
    public static double getVesselCapacityDecayMultiplier() { return data.vesselCapacityDecayMultiplier.value; }
    public static int getVesselDecayStartingBlock() { return data.vesselDecayStartingBlock.value; }
    public static double getChamberParticleDensity() { return data.chamberParticleDensity.value; }

    public static int getDilutedEssenceBatchLimit() { return data.dilutedEssenceBatchLimit.value; }
    public static int getDilutedEssenceMaxCharge() { return data.dilutedEssenceMaxCharge.value; }

    public static boolean getNodeLifetimeEnabled() { return data.nodeLifetimeEnabled.value; }
    public static int getNodeLifetimeTicks() { return data.nodeLifetimeTicks.value; }
    public static boolean getNodeRespawnEnabled() { return data.nodeRespawnEnabled.value; }
    public static int getNodeMinCapacity() { return data.nodeMinCapacity.value; }
    public static int getNodeMaxCapacity() { return data.nodeMaxCapacity.value; }
    public static boolean getNodeInfiniteCapacity() { return data.nodeInfiniteCapacity.value; }
    public static int getNodeSpawnSpacing() { return data.nodeSpawnSpacing.value; }
    public static int getNodeMaxPerArea() { return data.nodeMaxPerArea.value; }

    public static int getConduitBaseSafeCapacity() { return data.conduitBaseSafeCapacity.value; }
    public static double getChargedConduitCapacityMultiplier() { return data.chargedConduitCapacityMultiplier.value; }
    public static int getConduitUnchargedTransferRate() { return data.conduitUnchargedTransferRate.value; }
    public static int getConduitChargedTransferRate() { return data.conduitChargedTransferRate.value; }

    public static int getCaputitePipeCapacity() { return data.caputitePipeCapacity.value; }
    public static int getCaputitePipeTransferRate() { return data.caputitePipeTransferRate.value; }
    public static int getChargedCaputitePipeCapacity() { return data.chargedCaputitePipeCapacity.value; }
    public static int getChargedCaputitePipeTransferRate() { return data.chargedCaputitePipeTransferRate.value; }

    public static int getSanguinitePipeCapacity() { return data.sanguinitePipeCapacity.value; }
    public static int getSanguinitePipeTransferRate() { return data.sanguinitePipeTransferRate.value; }
    public static int getChargedSanguinitePipeCapacity() { return data.chargedSanguinitePipeCapacity.value; }
    public static int getChargedSanguinitePipeTransferRate() { return data.chargedSanguinitePipeTransferRate.value; }

    public static int getMercuritePipeCapacity() { return data.mercuritePipeCapacity.value; }
    public static int getMercuritePipeTransferRate() { return data.mercuritePipeTransferRate.value; }
    public static int getChargedMercuritePipeCapacity() { return data.chargedMercuritePipeCapacity.value; }
    public static int getChargedMercuritePipeTransferRate() { return data.chargedMercuritePipeTransferRate.value; }

    public static int getEucliditePipeCapacity() { return data.eucliditePipeCapacity.value; }
    public static int getEucliditePipeTransferRate() { return data.eucliditePipeTransferRate.value; }
    public static int getChargedEucliditePipeCapacity() { return data.chargedEucliditePipeCapacity.value; }
    public static int getChargedEucliditePipeTransferRate() { return data.chargedEucliditePipeTransferRate.value; }

    public static int getAthanoritePipeCapacity() { return data.athanoritePipeCapacity.value; }
    public static int getAthanoritePipeTransferRate() { return data.athanoritePipeTransferRate.value; }
    public static int getChargedAthanoritePipeCapacity() { return data.chargedAthanoritePipeCapacity.value; }
    public static int getChargedAthanoritePipeTransferRate() { return data.chargedAthanoritePipeTransferRate.value; }

    public static int getWardConsumptionAmount() { return data.wardConsumptionAmount.value; }
    public static int getWardConsumptionIntervalSeconds() { return data.wardConsumptionIntervalSeconds.value; }
    public static double getWardCircleRadiusBase() { return data.wardCircleRadiusBase.value; }
    public static double getWardBlockRadius() { return data.wardBlockRadius.value; }
    public static int getExtractionNodeCooldown() { return data.extractionNodeCooldown.value; }
    public static int getMaxDelayTicks() { return data.maxDelayTicks.value; }
    public static boolean getFancyMagicCircleProcessing() { return data.fancyMagicCircleProcessing.value; }
    public static boolean getEnableAstralMirrorSkyReflection() { return data.enableAstralMirrorSkyReflection.value; }
    public static boolean getEnableAstralMirrorParallaxDepth() { return data.enableAstralMirrorParallaxDepth.value; }
    public static boolean getEnableAstralMirror3DBillboardStars() { return data.enableAstralMirror3DBillboardStars.value; }
    public static boolean getEnableAstralMirrorLiquidRefraction() { return data.enableAstralMirrorLiquidRefraction.value; }
    public static boolean getEnableAstralMirrorNebulae() { return data.enableAstralMirrorNebulae.value; }
    public static boolean getEnableAstralMirrorConstellationLines() { return data.enableAstralMirrorConstellationLines.value; }
    public static int getAstralCollectorTransferRate() { return data.astralCollectorTransferRate.value; }
}
