package ddraig.net.entropica.config.neoforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ddraig.net.entropica.config.EntropicaBiome;
import dev.architectury.platform.Platform;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public class EntropicaConfigImpl {
    public static int getMateriaPerEssence() { return EntropicaNeoForgeConfig.MATERIA_PER_ESSENCE.get(); }
    public static int getMateriaFurnaceMaxMateria() { return EntropicaNeoForgeConfig.MATERIA_FURNACE_MAX_MATERIA.get(); }
    public static int getMateriaFurnaceMaxEssence() { return EntropicaNeoForgeConfig.MATERIA_FURNACE_MAX_ESSENCE.get(); }
    public static int getEntropicCoreMaxMateria() { return EntropicaNeoForgeConfig.ENTROPIC_CORE_MAX_MATERIA.get(); }
    public static int getReceptacleMaxEssence() { return EntropicaNeoForgeConfig.RECEPTACLE_MAX_ESSENCE.get(); }
    public static int getCoreProcessTickRate() { return EntropicaNeoForgeConfig.CORE_PROCESS_TICK_RATE.get(); }
    public static boolean getEnableCoreOverload() { return EntropicaNeoForgeConfig.ENABLE_CORE_OVERLOAD.get(); }
    public static boolean getRequireResearchToCraft() { return EntropicaNeoForgeConfig.REQUIRE_RESEARCH_TO_CRAFT.get(); }
    public static int getOrbisCellMaxMateria() { return EntropicaNeoForgeConfig.ORBIS_CELL_MAX_MATERIA.get(); }
    public static boolean getFancyMagicCircleProcessing() { return EntropicaNeoForgeConfig.FANCY_MAGIC_CIRCLE_PROCESSING.get(); }

    public static int getCopperPipeCapacity() { return EntropicaNeoForgeConfig.COPPER_PIPE_CAPACITY.get(); }
    public static int getCopperPipeTransferRate() { return EntropicaNeoForgeConfig.COPPER_PIPE_TRANSFER_RATE.get(); }
    public static int getIronPipeCapacity() { return EntropicaNeoForgeConfig.IRON_PIPE_CAPACITY.get(); }
    public static int getIronPipeTransferRate() { return EntropicaNeoForgeConfig.IRON_PIPE_TRANSFER_RATE.get(); }
    public static int getDiamondPipeCapacity() { return EntropicaNeoForgeConfig.DIAMOND_PIPE_CAPACITY.get(); }
    public static int getDiamondPipeTransferRate() { return EntropicaNeoForgeConfig.DIAMOND_PIPE_TRANSFER_RATE.get(); }
    public static int getArcanitePipeCapacity() { return EntropicaNeoForgeConfig.ARCANITE_PIPE_CAPACITY.get(); }
    public static int getArcanitePipeTransferRate() { return EntropicaNeoForgeConfig.ARCANITE_PIPE_TRANSFER_RATE.get(); }
    public static int getResonitePipeCapacity() { return EntropicaNeoForgeConfig.RESONITE_PIPE_CAPACITY.get(); }
    public static int getResonitePipeTransferRate() { return EntropicaNeoForgeConfig.RESONITE_PIPE_TRANSFER_RATE.get(); }
    public static int getViscanitePipeCapacity() { return EntropicaNeoForgeConfig.VISCANITE_PIPE_CAPACITY.get(); }
    public static int getViscanitePipeTransferRate() { return EntropicaNeoForgeConfig.VISCANITE_PIPE_TRANSFER_RATE.get(); }
    public static int getChargedArcanitePipeCapacity() { return EntropicaNeoForgeConfig.CHARGED_ARCANITE_PIPE_CAPACITY.get(); }
    public static int getChargedArcanitePipeTransferRate() { return EntropicaNeoForgeConfig.CHARGED_ARCANITE_PIPE_TRANSFER_RATE.get(); }
    public static int getChargedViscanitePipeCapacity() { return EntropicaNeoForgeConfig.CHARGED_VISCANITE_PIPE_CAPACITY.get(); }
    public static int getChargedViscanitePipeTransferRate() { return EntropicaNeoForgeConfig.CHARGED_VISCANITE_PIPE_TRANSFER_RATE.get(); }
    public static int getGoldPipeCapacity() { return EntropicaNeoForgeConfig.GOLD_PIPE_CAPACITY.get(); }
    public static int getGoldPipeTransferRate() { return EntropicaNeoForgeConfig.GOLD_PIPE_TRANSFER_RATE.get(); }
    public static int getChargedResonitePipeCapacity() { return EntropicaNeoForgeConfig.CHARGED_RESONITE_PIPE_CAPACITY.get(); }
    public static int getChargedResonitePipeTransferRate() { return EntropicaNeoForgeConfig.CHARGED_RESONITE_PIPE_TRANSFER_RATE.get(); }

    public static int getMateriaFumusTransferRate() { return EntropicaNeoForgeConfig.MATERIA_FUMUS_TRANSFER_RATE.get(); }
    public static int getMateriaFumusTickRate() { return EntropicaNeoForgeConfig.MATERIA_FUMUS_TICK_RATE.get(); }
    public static int getVaporPneumaticDiverterCapacity() { return EntropicaNeoForgeConfig.VAPOR_PNEUMATIC_DIVERTER_CAPACITY.get(); }
    public static int getMateriaFumeOverpressureLimit() { return EntropicaNeoForgeConfig.MATERIA_FUME_OVERPRESSURE_LIMIT.get(); }

    public static int getVesselGlassBaseCapacity() { return EntropicaNeoForgeConfig.VESSEL_GLASS_BASE_CAPACITY.get(); }
    public static int getVesselGlassStrengthenedCapacity() { return EntropicaNeoForgeConfig.VESSEL_GLASS_STRENGTHENED_CAPACITY.get(); }
    public static int getVesselGlassIchorCapacity() { return EntropicaNeoForgeConfig.VESSEL_GLASS_ICHOR_CAPACITY.get(); }
    public static int getVesselGlassLatticeCapacity() { return EntropicaNeoForgeConfig.VESSEL_GLASS_LATTICE_CAPACITY.get(); }
    public static double getVesselCapacityDecayMultiplier() { return EntropicaNeoForgeConfig.VESSEL_CAPACITY_DECAY_MULTIPLIER.get(); }
    public static int getVesselDecayStartingBlock() { return EntropicaNeoForgeConfig.VESSEL_DECAY_STARTING_BLOCK.get(); }
    public static double getChamberParticleDensity() { return EntropicaNeoForgeConfig.CHAMBER_PARTICLE_DENSITY.get(); }

    public static int getDilutedEssenceBatchLimit() { return EntropicaNeoForgeConfig.DILUTED_ESSENCE_BATCH_LIMIT.get(); }
    public static int getDilutedEssenceMaxCharge() { return EntropicaNeoForgeConfig.DILUTED_ESSENCE_MAX_CHARGE.get(); }

    public static boolean getNodeLifetimeEnabled() { return EntropicaNeoForgeConfig.NODE_LIFETIME_ENABLED.get(); }
    public static int getNodeLifetimeTicks() { return EntropicaNeoForgeConfig.NODE_LIFETIME_TICKS.get(); }
    public static boolean getNodeRespawnEnabled() { return EntropicaNeoForgeConfig.NODE_RESPAWN_ENABLED.get(); }
    public static int getNodeMinCapacity() { return EntropicaNeoForgeConfig.NODE_MIN_CAPACITY.get(); }
    public static int getNodeMaxCapacity() { return EntropicaNeoForgeConfig.NODE_MAX_CAPACITY.get(); }
    public static boolean getNodeInfiniteCapacity() { return EntropicaNeoForgeConfig.NODE_INFINITE_CAPACITY.get(); }
    public static int getNodeSpawnSpacing() { return EntropicaNeoForgeConfig.NODE_SPAWN_SPACING.get(); }
    public static int getNodeMaxPerArea() { return EntropicaNeoForgeConfig.NODE_MAX_PER_AREA.get(); }

    public static int getConduitBaseSafeCapacity() { return EntropicaNeoForgeConfig.CONDUIT_BASE_SAFE_CAPACITY.get(); }
    public static double getChargedConduitCapacityMultiplier() { return EntropicaNeoForgeConfig.CHARGED_CONDUIT_CAPACITY_MULTIPLIER.get(); }
    public static int getConduitUnchargedTransferRate() { return EntropicaNeoForgeConfig.CONDUIT_UNCHARGED_TRANSFER_RATE.get(); }
    public static int getConduitChargedTransferRate() { return EntropicaNeoForgeConfig.CONDUIT_CHARGED_TRANSFER_RATE.get(); }

    public static int getCaputitePipeCapacity() { return EntropicaNeoForgeConfig.CAPUTITE_PIPE_CAPACITY.get(); }
    public static int getCaputitePipeTransferRate() { return EntropicaNeoForgeConfig.CAPUTITE_PIPE_TRANSFER_RATE.get(); }
    public static int getChargedCaputitePipeCapacity() { return EntropicaNeoForgeConfig.CHARGED_CAPUTITE_PIPE_CAPACITY.get(); }
    public static int getChargedCaputitePipeTransferRate() { return EntropicaNeoForgeConfig.CHARGED_CAPUTITE_PIPE_TRANSFER_RATE.get(); }

    public static int getSanguinitePipeCapacity() { return EntropicaNeoForgeConfig.SANGUINITE_PIPE_CAPACITY.get(); }
    public static int getSanguinitePipeTransferRate() { return EntropicaNeoForgeConfig.SANGUINITE_PIPE_TRANSFER_RATE.get(); }
    public static int getChargedSanguinitePipeCapacity() { return EntropicaNeoForgeConfig.CHARGED_SANGUINITE_PIPE_CAPACITY.get(); }
    public static int getChargedSanguinitePipeTransferRate() { return EntropicaNeoForgeConfig.CHARGED_SANGUINITE_PIPE_TRANSFER_RATE.get(); }

    public static int getMercuritePipeCapacity() { return EntropicaNeoForgeConfig.MERCURITE_PIPE_CAPACITY.get(); }
    public static int getMercuritePipeTransferRate() { return EntropicaNeoForgeConfig.MERCURITE_PIPE_TRANSFER_RATE.get(); }
    public static int getChargedMercuritePipeCapacity() { return EntropicaNeoForgeConfig.CHARGED_MERCURITE_PIPE_CAPACITY.get(); }
    public static int getChargedMercuritePipeTransferRate() { return EntropicaNeoForgeConfig.CHARGED_MERCURITE_PIPE_TRANSFER_RATE.get(); }

    public static int getEucliditePipeCapacity() { return EntropicaNeoForgeConfig.EUCLIDITE_PIPE_CAPACITY.get(); }
    public static int getEucliditePipeTransferRate() { return EntropicaNeoForgeConfig.EUCLIDITE_PIPE_TRANSFER_RATE.get(); }
    public static int getChargedEucliditePipeCapacity() { return EntropicaNeoForgeConfig.CHARGED_EUCLIDITE_PIPE_CAPACITY.get(); }
    public static int getChargedEucliditePipeTransferRate() { return EntropicaNeoForgeConfig.CHARGED_EUCLIDITE_PIPE_TRANSFER_RATE.get(); }

    public static int getAthanoritePipeCapacity() { return EntropicaNeoForgeConfig.ATHANORITE_PIPE_CAPACITY.get(); }
    public static int getAthanoritePipeTransferRate() { return EntropicaNeoForgeConfig.ATHANORITE_PIPE_TRANSFER_RATE.get(); }
    public static int getChargedAthanoritePipeCapacity() { return EntropicaNeoForgeConfig.CHARGED_ATHANORITE_PIPE_CAPACITY.get(); }
    public static int getChargedAthanoritePipeTransferRate() { return EntropicaNeoForgeConfig.CHARGED_ATHANORITE_PIPE_TRANSFER_RATE.get(); }

    public static int getWardConsumptionAmount() { return EntropicaNeoForgeConfig.WARD_CONSUMPTION_AMOUNT.get(); }
    public static int getWardConsumptionIntervalSeconds() { return EntropicaNeoForgeConfig.WARD_CONSUMPTION_INTERVAL_SECONDS.get(); }
    public static double getWardCircleRadiusBase() { return EntropicaNeoForgeConfig.WARD_CIRCLE_RADIUS_BASE.get(); }
    public static int getWardBlockRadius() { return EntropicaNeoForgeConfig.WARD_BLOCK_RADIUS.get().intValue(); }
    public static int getExtractionNodeCooldown() { return EntropicaNeoForgeConfig.EXTRACTION_NODE_COOLDOWN.get(); }
    public static int getMaxDelayTicks() { return EntropicaNeoForgeConfig.MAX_DELAY_TICKS.get(); }
    public static boolean getEnableAstralMirrorSkyReflection() { return EntropicaNeoForgeConfig.ENABLE_ASTRAL_MIRROR_SKY_REFLECTION.get(); }
    public static boolean getEnableAstralMirrorParallaxDepth() { return EntropicaNeoForgeConfig.ENABLE_ASTRAL_MIRROR_PARALLAX_DEPTH.get(); }
    public static boolean getEnableAstralMirror3DBillboardStars() { return EntropicaNeoForgeConfig.ENABLE_ASTRAL_MIRROR_3D_BILLBOARD_STARS.get(); }
    public static boolean getEnableAstralMirrorLiquidRefraction() { return EntropicaNeoForgeConfig.ENABLE_ASTRAL_MIRROR_LIQUID_REFRACTION.get(); }
    public static boolean getEnableAstralMirrorNebulae() { return EntropicaNeoForgeConfig.ENABLE_ASTRAL_MIRROR_NEBULAE.get(); }
    public static boolean getEnableAstralMirrorConstellationLines() { return EntropicaNeoForgeConfig.ENABLE_ASTRAL_MIRROR_CONSTELLATION_LINES.get(); }
    public static int getAstralCollectorTransferRate() { return EntropicaNeoForgeConfig.ASTRAL_COLLECTOR_TRANSFER_RATE.get(); }
    private static final Gson MAGNETITE_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile List<String> cachedMagnetiteOreWhitelist = null;
    private static volatile List<String> cachedMagnetiteOreBlacklist = null;

    private static final class MagnetiteBeamOreConfigData {
        public String description = "Earth Magnetite Tractor Beam Tier 2+ ore filter (supports block IDs like 'minecraft:iron_ore' and tags like '#c:ores/iron'). Empty whitelist allows all detected ores unless matched by blacklist.";
        public List<String> magnetiteBeamOreWhitelist = new ArrayList<>();
        public List<String> magnetiteBeamOreBlacklist = new ArrayList<>();
    }

    private static synchronized void ensureMagnetiteBeamOreConfigLoaded() {
        if (cachedMagnetiteOreWhitelist != null && cachedMagnetiteOreBlacklist != null) {
            return;
        }
        List<String> white = List.of();
        List<String> black = List.of();
        try {
            Path configPath = Platform.getConfigFolder().resolve("entropica-magnetite-beam.json");
            MagnetiteBeamOreConfigData cfg = new MagnetiteBeamOreConfigData();
            if (Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    MagnetiteBeamOreConfigData loaded = MAGNETITE_GSON.fromJson(reader, MagnetiteBeamOreConfigData.class);
                    if (loaded != null) {
                        cfg = loaded;
                    }
                }
            } else {
                Files.createDirectories(configPath.getParent());
                try (Writer writer = Files.newBufferedWriter(configPath)) {
                    MAGNETITE_GSON.toJson(cfg, writer);
                }
            }
            white = sanitizeConfigStringList(cfg.magnetiteBeamOreWhitelist);
            black = sanitizeConfigStringList(cfg.magnetiteBeamOreBlacklist);
        } catch (Throwable ignored) {
            // Uninitialized platform config dir or read-only environment: safely default to empty lists.
        }
        cachedMagnetiteOreWhitelist = white;
        cachedMagnetiteOreBlacklist = black;
    }

    private static List<String> sanitizeConfigStringList(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<String> cleaned = new ArrayList<>();
        for (String entry : raw) {
            if (entry != null && !entry.isBlank()) {
                cleaned.add(entry.trim());
            }
        }
        return Collections.unmodifiableList(cleaned);
    }

    public static int getSpellMutationUnlockMode() { return EntropicaNeoForgeConfig.SPELL_MUTATION_UNLOCK_MODE.get(); }
    public static boolean getEnableSpellMutationOpJoinNotice() { return EntropicaNeoForgeConfig.ENABLE_SPELL_MUTATION_OP_JOIN_NOTICE.get(); }
    public static List<String> getMagnetiteBeamOreWhitelist() {
        ensureMagnetiteBeamOreConfigLoaded();
        return cachedMagnetiteOreWhitelist;
    }
    public static List<String> getMagnetiteBeamOreBlacklist() {
        ensureMagnetiteBeamOreConfigLoaded();
        return cachedMagnetiteOreBlacklist;
    }
    public static boolean getEnableAllBiomes() { return EntropicaNeoForgeConfig.ENABLE_ALL_BIOMES.get(); }
    public static int getTerraBlenderCompatMode() { return EntropicaNeoForgeConfig.TERRABLENDER_COMPAT_MODE.get(); }
    public static int getEntropicaBiomeRegionWeight() { return EntropicaNeoForgeConfig.ENTROPICA_BIOME_REGION_WEIGHT.get(); }
    public static int getVanillaBiomePassthroughWeight() { return EntropicaNeoForgeConfig.VANILLA_BIOME_PASSTHROUGH_WEIGHT.get(); }
    public static int getBiomeRegionScale() { return EntropicaNeoForgeConfig.BIOME_REGION_SCALE.get(); }
    public static boolean getEnableBiomeCustomFog() { return EntropicaNeoForgeConfig.ENABLE_BIOME_CUSTOM_FOG.get(); }
    public static boolean getEnableBiomeCustomSky() { return EntropicaNeoForgeConfig.ENABLE_BIOME_CUSTOM_SKY.get(); }
    public static boolean getEnableBiomeEnvironmentalEffects() { return EntropicaNeoForgeConfig.ENABLE_BIOME_ENVIRONMENTAL_EFFECTS.get(); }
    public static boolean getEnableEnhancedBiomeFogAndWater() { return EntropicaNeoForgeConfig.ENABLE_ENHANCED_BIOME_FOG_AND_WATER.get(); }
    public static boolean getEnableBiomeVolumetricFog() {
        return getEnableEnhancedBiomeFogAndWater() && EntropicaNeoForgeConfig.ENABLE_BIOME_VOLUMETRIC_FOG.get();
    }
    public static boolean getEnableBiomeWaterEffects() {
        return getEnableEnhancedBiomeFogAndWater() && EntropicaNeoForgeConfig.ENABLE_BIOME_WATER_EFFECTS.get();
    }
    public static boolean getEnableShaderpackAdaptiveAttenuation() { return EntropicaNeoForgeConfig.ENABLE_SHADERPACK_ADAPTIVE_ATTENUATION.get(); }
    public static boolean getBiomeEnabled(EntropicaBiome biome) {
        if (biome == null) return false;
        ModConfigSpec.BooleanValue toggle = EntropicaNeoForgeConfig.BIOME_TOGGLES.get(biome);
        return toggle == null || toggle.get();
    }
}
