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

    // Vis Vitae Network Configs
    public static final ModConfigSpec.IntValue ORBIS_CELL_MAX_MANA;

    // Vis Fume Network Configs
    public static final ModConfigSpec.IntValue VIS_FUME_PIPE_CAPACITY;
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

        BUILDER.pop();

        // --- VIS VITAE NETWORK SETTINGS ---
        BUILDER.push("vis_vitae_network_settings");

        ORBIS_CELL_MAX_MANA = BUILDER.comment("Maximum mana storage capacity for a single Orbis Cell.")
                .translation("entropica.configuration.vis_vitae_network_settings.orbisCellMaxMana")
                .defineInRange("orbisCellMaxMana", 10000, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        // --- VIS FUME NETWORK SETTINGS ---
        BUILDER.push("vis_fume_network_settings");

        VIS_FUME_PIPE_CAPACITY = BUILDER.comment("Maximum amount of Vis Fumes a single pipe or valve can hold.")
                .translation("entropica.configuration.vis_fume_network_settings.pipeCapacity")
                .defineInRange("visFumePipeCapacity", 200, 1, Integer.MAX_VALUE);

        VIS_FUME_TRANSFER_RATE = BUILDER.comment("How many Vis Fumes are moved per transfer tick.")
                .translation("entropica.configuration.vis_fume_network_settings.transferRate")
                .defineInRange("visFumeTransferRate", 20, 1, Integer.MAX_VALUE);

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

        SPEC = BUILDER.build();
    }
}