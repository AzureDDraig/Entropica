package ddraig.net.entropica.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class EntropicaConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MANA_PER_ESSENCE;
    public static final ModConfigSpec.IntValue MANA_FURNACE_MAX_MANA;
    public static final ModConfigSpec.IntValue MANA_FURNACE_MAX_ESSENCE;

    // Multiblock Configs
    public static final ModConfigSpec.IntValue ENTROPIC_CORE_MAX_MANA;
    public static final ModConfigSpec.IntValue RECEPTACLE_MAX_ESSENCE;
    public static final ModConfigSpec.IntValue CORE_PROCESS_TICK_RATE;
    public static final ModConfigSpec.BooleanValue ENABLE_CORE_OVERLOAD;

    // Vis Vitae Network Configs
    public static final ModConfigSpec.IntValue ORBIS_CELL_MAX_MANA;

    // Vis Fume Network Configs
    public static final ModConfigSpec.IntValue COPPER_PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue COPPER_PIPE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue IRON_PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue IRON_PIPE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue DIAMOND_PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue DIAMOND_PIPE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue ARCANITE_PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue ARCANITE_PIPE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue RESONITE_PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue RESONITE_PIPE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue VISCANITE_PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue VISCANITE_PIPE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue CHARGED_ARCANITE_PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue CHARGED_ARCANITE_PIPE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue CHARGED_VISCANITE_PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue CHARGED_VISCANITE_PIPE_TRANSFER_RATE;

    public static final ModConfigSpec.IntValue VIS_FUME_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue VIS_FUME_TICK_RATE;
    public static final ModConfigSpec.IntValue VIS_FUME_DIVERTER_CAPACITY;
    public static final ModConfigSpec.IntValue VIS_FUME_OVERPRESSURE_LIMIT;

    // Vis Fume Pressure Vessel Configs
    public static final ModConfigSpec.IntValue VESSEL_GLASS_BASE_CAPACITY;
    public static final ModConfigSpec.IntValue VESSEL_GLASS_STRENGTHENED_CAPACITY;
    public static final ModConfigSpec.IntValue VESSEL_GLASS_ICHOR_CAPACITY;
    public static final ModConfigSpec.IntValue VESSEL_GLASS_LATTICE_CAPACITY;
    public static final ModConfigSpec.DoubleValue VESSEL_CAPACITY_DECAY_MULTIPLIER;
    public static final ModConfigSpec.IntValue VESSEL_DECAY_STARTING_BLOCK;

    // Vis Fume Pressure Chamber Configs
    public static final ModConfigSpec.DoubleValue CHAMBER_PARTICLE_DENSITY;

    // Diluted Essence Configs
    public static final ModConfigSpec.IntValue DILUTED_ESSENCE_BATCH_LIMIT;
    public static final ModConfigSpec.IntValue DILUTED_ESSENCE_MAX_CHARGE;

    // Essence Node Configs
    public static final ModConfigSpec.BooleanValue NODE_LIFETIME_ENABLED;
    public static final ModConfigSpec.IntValue NODE_LIFETIME_TICKS;
    public static final ModConfigSpec.BooleanValue NODE_RESPAWN_ENABLED;
    public static final ModConfigSpec.IntValue NODE_MIN_CAPACITY;
    public static final ModConfigSpec.IntValue NODE_MAX_CAPACITY;
    public static final ModConfigSpec.BooleanValue NODE_INFINITE_CAPACITY;
    public static final ModConfigSpec.IntValue NODE_SPAWN_SPACING;
    public static final ModConfigSpec.IntValue NODE_MAX_PER_AREA;

    static {
        // --- BASE MANA FURNACE SETTINGS ---
        BUILDER.push("mana_furnace_settings");

        MANA_PER_ESSENCE = BUILDER.comment("The amount of Mana produced per 1 Essence consumed every 5 ticks.")
                .translation("entropica.configuration.mana_furnace_settings.manaPerEssence")
                .defineInRange("manaPerEssence", 8, 1, 1000);

        MANA_FURNACE_MAX_MANA = BUILDER.comment("Maximum mana storage capacity for the standalone Mana Furnace.")
                .translation("entropica.configuration.mana_furnace_settings.manaFurnaceMaxMana")
                .defineInRange("manaFurnaceMaxMana", 1024, 1, Integer.MAX_VALUE);

        MANA_FURNACE_MAX_ESSENCE = BUILDER.comment("Maximum essence storage capacity for the standalone Mana Furnace. Capped at 32.")
                .translation("entropica.configuration.mana_furnace_settings.manaFurnaceMaxEssence")
                .defineInRange("manaFurnaceMaxEssence", 32, 1, 32);

        BUILDER.pop();

        // --- ENTROPIC MANA FURNACE (MULTIBLOCK) SETTINGS ---
        BUILDER.push("entropic_mana_furnace_settings");

        ENTROPIC_CORE_MAX_MANA = BUILDER.comment("Maximum mana storage capacity for the Entropic Mana Furnace multiblock.")
                .translation("entropica.configuration.entropic_mana_furnace_settings.entropicCoreMaxMana")
                .defineInRange("entropicCoreMaxMana", 100000, 1, Integer.MAX_VALUE);

        RECEPTACLE_MAX_ESSENCE = BUILDER.comment("The amount of essence storage capacity added per Essence Receptacle in the multiblock.")
                .translation("entropica.configuration.entropic_mana_furnace_settings.receptacleMaxEssence")
                .defineInRange("receptacleMaxEssence", 64, 1, Integer.MAX_VALUE);

        CORE_PROCESS_TICK_RATE = BUILDER.comment("How many ticks between each processing operation of the furnace (20 = 1 second, 5 = 1/4th second).")
                .translation("entropica.configuration.entropic_mana_furnace_settings.coreProcessTickRate")
                .defineInRange("coreProcessTickRate", 5, 1, 200);

        ENABLE_CORE_OVERLOAD = BUILDER.comment("If true, the Entropic Core will eventually explode if left active while its Mana Buffer is 100% full.")
                .translation("entropica.configuration.entropic_mana_furnace_settings.enableCoreOverload")
                .define("enableCoreOverload", true);

        BUILDER.pop();

        // --- VIS VITAE NETWORK SETTINGS ---
        BUILDER.push("vis_vitae_network_settings");

        ORBIS_CELL_MAX_MANA = BUILDER.comment("Maximum mana storage capacity for a single Orbis Cell.")
                .translation("entropica.configuration.vis_vitae_network_settings.orbisCellMaxMana")
                .defineInRange("orbisCellMaxMana", 10000, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        // --- VIS FUME NETWORK SETTINGS ---
        BUILDER.push("vis_fume_network_settings");

        COPPER_PIPE_CAPACITY = BUILDER.comment("Safe capacity (1.0 Pressure) for Copper Fume Pipes.").defineInRange("copperPipeCapacity", 20, 1, Integer.MAX_VALUE);
        COPPER_PIPE_TRANSFER_RATE = BUILDER.comment("Transfer rate for Copper Fume Pipes.").defineInRange("copperPipeTransferRate", 5, 1, Integer.MAX_VALUE);

        IRON_PIPE_CAPACITY = BUILDER.comment("Safe capacity (1.0 Pressure) for Iron Fume Pipes.").defineInRange("ironPipeCapacity", 40, 1, Integer.MAX_VALUE);
        IRON_PIPE_TRANSFER_RATE = BUILDER.comment("Transfer rate for Iron Fume Pipes.").defineInRange("ironPipeTransferRate", 10, 1, Integer.MAX_VALUE);

        DIAMOND_PIPE_CAPACITY = BUILDER.comment("Safe capacity (1.0 Pressure) for Diamond Fume Pipes.").defineInRange("diamondPipeCapacity", 80, 1, Integer.MAX_VALUE);
        DIAMOND_PIPE_TRANSFER_RATE = BUILDER.comment("Transfer rate for Diamond Fume Pipes.").defineInRange("diamondPipeTransferRate", 20, 1, Integer.MAX_VALUE);

        ARCANITE_PIPE_CAPACITY = BUILDER.comment("Safe capacity (1.0 Pressure) for Arcanite Fume Pipes.").defineInRange("arcanitePipeCapacity", 100, 1, Integer.MAX_VALUE);
        ARCANITE_PIPE_TRANSFER_RATE = BUILDER.comment("Transfer rate for Arcanite Fume Pipes.").defineInRange("arcanitePipeTransferRate", 25, 1, Integer.MAX_VALUE);

        RESONITE_PIPE_CAPACITY = BUILDER.comment("Safe capacity (1.0 Pressure) for Resonite Fume Pipes.").defineInRange("resonitePipeCapacity", 300, 1, Integer.MAX_VALUE);
        RESONITE_PIPE_TRANSFER_RATE = BUILDER.comment("Transfer rate for Resonite Fume Pipes.").defineInRange("resonitePipeTransferRate", 50, 1, Integer.MAX_VALUE);

        VISCANITE_PIPE_CAPACITY = BUILDER.comment("Safe capacity (1.0 Pressure) for Viscanite Fume Pipes.").defineInRange("viscanitePipeCapacity", 500, 1, Integer.MAX_VALUE);
        VISCANITE_PIPE_TRANSFER_RATE = BUILDER.comment("Transfer rate for Viscanite Fume Pipes.").defineInRange("viscanitePipeTransferRate", 100, 1, Integer.MAX_VALUE);

        CHARGED_ARCANITE_PIPE_CAPACITY = BUILDER.comment("Safe capacity (1.0 Pressure) for Charged Arcanite Fume Pipes.").defineInRange("chargedArcanitePipeCapacity", 200, 1, Integer.MAX_VALUE);
        CHARGED_ARCANITE_PIPE_TRANSFER_RATE = BUILDER.comment("Transfer rate for Charged Arcanite Fume Pipes.").defineInRange("chargedArcanitePipeTransferRate", 500, 1, Integer.MAX_VALUE);

        CHARGED_VISCANITE_PIPE_CAPACITY = BUILDER.comment("Safe capacity (1.0 Pressure) for Charged Viscanite Fume Pipes.").defineInRange("chargedViscanitePipeCapacity", 1000, 1, Integer.MAX_VALUE);
        CHARGED_VISCANITE_PIPE_TRANSFER_RATE = BUILDER.comment("Transfer rate for Charged Viscanite Fume Pipes.").defineInRange("chargedViscanitePipeTransferRate", 1000, 1, Integer.MAX_VALUE);

        VIS_FUME_TRANSFER_RATE = BUILDER.comment("Base transfer rate for generators and machines pushing Fumes into the network.")
                .translation("entropica.configuration.vis_fume_network_settings.transferRate")
                .defineInRange("visFumeTransferRate", 100, 1, Integer.MAX_VALUE);

        VIS_FUME_TICK_RATE = BUILDER.comment("How many ticks between each gas transfer operation in the pipe network.")
                .translation("entropica.configuration.vis_fume_network_settings.tickRate")
                .defineInRange("visFumeTickRate", 5, 1, 200);

        VIS_FUME_DIVERTER_CAPACITY = BUILDER.comment("Maximum amount of Vis Fumes a Diverter block can safely process.")
                .translation("entropica.configuration.vis_fume_network_settings.diverterCapacity")
                .defineInRange("visFumeDiverterCapacity", 2500, 1, Integer.MAX_VALUE);

        VIS_FUME_OVERPRESSURE_LIMIT = BUILDER.comment("The amount of Fumes inside a Diverter that will cause it to break from overpressure.")
                .translation("entropica.configuration.vis_fume_network_settings.overpressureLimit")
                .defineInRange("visFumeOverpressureLimit", 2000, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        // --- VIS FUME PRESSURE VESSEL SETTINGS ---
        BUILDER.push("vis_fume_vessel_settings");

        VESSEL_GLASS_BASE_CAPACITY = BUILDER.comment("Capacity added by standard Base Essence Enriched Glass.")
                .defineInRange("vesselGlassBaseCapacity", 1000, 1, Integer.MAX_VALUE);

        VESSEL_GLASS_STRENGTHENED_CAPACITY = BUILDER.comment("Capacity added by Vis Fume Strengthened Glass.")
                .defineInRange("vesselGlassStrengthenedCapacity", 2500, 1, Integer.MAX_VALUE);

        VESSEL_GLASS_ICHOR_CAPACITY = BUILDER.comment("Capacity added by Vis Ichor Enriched Glass.")
                .defineInRange("vesselGlassIchorCapacity", 5000, 1, Integer.MAX_VALUE);

        VESSEL_GLASS_LATTICE_CAPACITY = BUILDER.comment("Capacity added by Fragment Lattice Glass.")
                .defineInRange("vesselGlassLatticeCapacity", 10000, 1, Integer.MAX_VALUE);

        VESSEL_CAPACITY_DECAY_MULTIPLIER = BUILDER.comment("The diminishing returns multiplier applied to each subsequent glass block. (0.95 = 5% loss per block).")
                .defineInRange("vesselCapacityDecayMultiplier", 0.95, 0.01, 1.0);

        VESSEL_DECAY_STARTING_BLOCK = BUILDER.comment("The amount of glass blocks placed before the diminishing returns multiplier starts applying. (0 = applies immediately to the 2nd block placed).")
                .defineInRange("vesselDecayStartingBlock", 0, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        // --- VIS FUME PRESSURE CHAMBER SETTINGS ---
        BUILDER.push("vis_fume_pressure_chamber_settings");

        CHAMBER_PARTICLE_DENSITY = BUILDER.comment("(COSMETIC) Multiplier for the amount of gas particles inside the Pressure Chamber. 0.0 turns them off. (Default: 1.0)")
                .translation("entropica.configuration.vis_fume_pressure_chamber_settings.chamberParticleDensity")
                .defineInRange("chamberParticleDensity", 1.0, 0.0, 10.0);

        BUILDER.pop();

        // --- NEW: DILUTED ESSENCE FLUID SETTINGS ---
        BUILDER.push("diluted_essence_fluid_settings");

        DILUTED_ESSENCE_BATCH_LIMIT = BUILDER.comment("The maximum number of items the Diluted Essence fluid can process at one time. (Default: 4)")
                .translation("entropica.configuration.diluted_essence_settings.batchLimit")
                .defineInRange("dilutedEssenceBatchLimit", 4, 1, 16);

        DILUTED_ESSENCE_MAX_CHARGE = BUILDER.comment("The maximum amount of charge a Diluted Essence fluid block can hold. (Default: 32)")
                .translation("entropica.configuration.diluted_essence_settings.maxCharge")
                .defineInRange("dilutedEssenceMaxCharge", 32, 1, 128);

        BUILDER.pop();

        // --- ESSENCE NODE SETTINGS ---
        BUILDER.push("essence_node_settings");

        NODE_LIFETIME_ENABLED = BUILDER.comment("Should natural Essence Nodes fizzle out after a certain amount of time? (Ritual/Artificial ones might bypass this based on your logic)")
                .translation("entropica.configuration.essence_node_settings.lifetimeEnabled")
                .define("nodeLifetimeEnabled", true);

        NODE_LIFETIME_TICKS = BUILDER.comment("How long (in ticks) should a natural Essence Node live before fizzling? (Default: 24000 = 1 in-game day)")
                .translation("entropica.configuration.essence_node_settings.lifetimeTicks")
                .defineInRange("nodeLifetimeTicks", 24000, 20, Integer.MAX_VALUE);

        NODE_RESPAWN_ENABLED = BUILDER.comment("Should a new Essence Node naturally spawn in the same chunk when an old one fizzles out?")
                .translation("entropica.configuration.essence_node_settings.respawnEnabled")
                .define("nodeRespawnEnabled", true);

        NODE_MIN_CAPACITY = BUILDER.comment("Minimum amount of essence a natural node can spawn with.")
                .translation("entropica.configuration.essence_node_settings.minCapacity")
                .defineInRange("nodeMinCapacity", 50, 1, Integer.MAX_VALUE);

        NODE_MAX_CAPACITY = BUILDER.comment("Maximum amount of essence a natural node can spawn with.")
                .translation("entropica.configuration.essence_node_settings.maxCapacity")
                .defineInRange("nodeMaxCapacity", 500, 1, Integer.MAX_VALUE);

        NODE_INFINITE_CAPACITY = BUILDER.comment("If true, Essence Nodes never run out of essence from extraction.")
                .translation("entropica.configuration.essence_node_settings.infiniteCapacity")
                .define("nodeInfiniteCapacity", false);

        NODE_SPAWN_SPACING = BUILDER.comment("Minimum chunk distance between naturally spawning nodes.")
                .translation("entropica.configuration.essence_node_settings.spawnSpacing")
                .defineInRange("nodeSpawnSpacing", 6, 1, 100);

        NODE_MAX_PER_AREA = BUILDER.comment("Maximum number of nodes allowed in a 10x10 chunk area.")
                .translation("entropica.configuration.essence_node_settings.maxPerArea")
                .defineInRange("nodeMaxPerArea", 10, 1, 100);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}