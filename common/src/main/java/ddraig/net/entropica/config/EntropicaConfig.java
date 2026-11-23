package ddraig.net.entropica.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class EntropicaConfig {
    public static final Supplier<Integer> MATERIA_PER_ESSENCE = EntropicaConfig::getMateriaPerEssence;
    public static final Supplier<Integer> MATERIA_FURNACE_MAX_MATERIA = EntropicaConfig::getMateriaFurnaceMaxMateria;
    public static final Supplier<Integer> MATERIA_FURNACE_MAX_ESSENCE = EntropicaConfig::getMateriaFurnaceMaxEssence;
    public static final Supplier<Integer> ENTROPIC_CORE_MAX_MATERIA = EntropicaConfig::getEntropicCoreMaxMateria;
    public static final Supplier<Integer> RECEPTACLE_MAX_ESSENCE = EntropicaConfig::getReceptacleMaxEssence;
    public static final Supplier<Integer> CORE_PROCESS_TICK_RATE = EntropicaConfig::getCoreProcessTickRate;
    public static final Supplier<Boolean> ENABLE_CORE_OVERLOAD = EntropicaConfig::getEnableCoreOverload;
    public static final Supplier<Boolean> REQUIRE_RESEARCH_TO_CRAFT = EntropicaConfig::getRequireResearchToCraft;
    public static final Supplier<Integer> ORBIS_CELL_MAX_MATERIA = EntropicaConfig::getOrbisCellMaxMateria;
    public static final Supplier<Boolean> FANCY_MAGIC_CIRCLE_PROCESSING = EntropicaConfig::getFancyMagicCircleProcessing;
    // Astral Mirror Settings
    public static final Supplier<Boolean> ENABLE_ASTRAL_MIRROR_SKY_REFLECTION = EntropicaConfig::getEnableAstralMirrorSkyReflection;
    public static final Supplier<Boolean> ENABLE_ASTRAL_MIRROR_PARALLAX_DEPTH = EntropicaConfig::getEnableAstralMirrorParallaxDepth;
    public static final Supplier<Boolean> ENABLE_ASTRAL_MIRROR_3D_BILLBOARD_STARS = EntropicaConfig::getEnableAstralMirror3DBillboardStars;
    public static final Supplier<Boolean> ENABLE_ASTRAL_MIRROR_LIQUID_REFRACTION = EntropicaConfig::getEnableAstralMirrorLiquidRefraction;
    public static final Supplier<Boolean> ENABLE_ASTRAL_MIRROR_NEBULAE = EntropicaConfig::getEnableAstralMirrorNebulae;
    public static final Supplier<Boolean> ENABLE_ASTRAL_MIRROR_CONSTELLATION_LINES = EntropicaConfig::getEnableAstralMirrorConstellationLines;
    public static final Supplier<Integer> ASTRAL_COLLECTOR_TRANSFER_RATE = EntropicaConfig::getAstralCollectorTransferRate;

    // Materia Fume Network
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
    public static final Supplier<Integer> MATERIA_FUME_OVERPRESSURE_LIMIT = EntropicaConfig::getMateriaFumeOverpressureLimit;
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
    public static final Supplier<Integer> EXTRACTION_NODE_COOLDOWN = EntropicaConfig::getExtractionNodeCooldown;

    // Essence Repulsion Wards
    public static final Supplier<Integer> WARD_CONSUMPTION_AMOUNT = EntropicaConfig::getWardConsumptionAmount;
    public static final Supplier<Integer> WARD_CONSUMPTION_INTERVAL_SECONDS = EntropicaConfig::getWardConsumptionIntervalSeconds;
    public static final Supplier<Double> WARD_CIRCLE_RADIUS_BASE = EntropicaConfig::getWardCircleRadiusBase;
    public static final Supplier<Double> WARD_BLOCK_RADIUS = EntropicaConfig::getWardBlockRadius;
    public static final Supplier<Integer> MAX_DELAY_TICKS = EntropicaConfig::getMaxDelayTicks;
    public static final Supplier<Integer> SPELL_MUTATION_UNLOCK_MODE = EntropicaConfig::getSpellMutationUnlockMode;
    public static final Supplier<Boolean> ENABLE_SPELL_MUTATION_OP_JOIN_NOTICE = EntropicaConfig::getEnableSpellMutationOpJoinNotice;
    public static final Supplier<List<String>> MAGNETITE_BEAM_ORE_WHITELIST = EntropicaConfig::safeGetMagnetiteBeamOreWhitelist;
    public static final Supplier<List<String>> MAGNETITE_BEAM_ORE_BLACKLIST = EntropicaConfig::safeGetMagnetiteBeamOreBlacklist;

    // Biome Generation Settings (Master Switch + Injector Controls + All 108 Biomes)
    public static final Supplier<Boolean> ENABLE_ALL_BIOMES = EntropicaConfig::getEnableAllBiomes;
    public static final Supplier<Integer> TERRABLENDER_COMPAT_MODE = EntropicaConfig::getTerraBlenderCompatMode;
    public static final Supplier<Integer> ENTROPICA_BIOME_REGION_WEIGHT = EntropicaConfig::getEntropicaBiomeRegionWeight;
    public static final Supplier<Integer> VANILLA_BIOME_PASSTHROUGH_WEIGHT = EntropicaConfig::getVanillaBiomePassthroughWeight;
    public static final Supplier<Integer> BIOME_REGION_SCALE = EntropicaConfig::getBiomeRegionScale;
    public static final Supplier<Boolean> ENABLE_BIOME_CUSTOM_FOG = EntropicaConfig::getEnableBiomeCustomFog;
    public static final Supplier<Boolean> ENABLE_BIOME_CUSTOM_SKY = EntropicaConfig::getEnableBiomeCustomSky;
    public static final Supplier<Boolean> ENABLE_BIOME_ENVIRONMENTAL_EFFECTS = EntropicaConfig::getEnableBiomeEnvironmentalEffects;
    public static final Supplier<Boolean> ENABLE_ENHANCED_BIOME_FOG_AND_WATER = EntropicaConfig::safeGetEnableEnhancedBiomeFogAndWater;
    public static final Supplier<Boolean> ENABLE_BIOME_VOLUMETRIC_FOG = EntropicaConfig::safeGetEnableBiomeVolumetricFog;
    public static final Supplier<Boolean> ENABLE_BIOME_WATER_EFFECTS = EntropicaConfig::safeGetEnableBiomeWaterEffects;
    public static final Supplier<Boolean> ENABLE_SHADERPACK_ADAPTIVE_ATTENUATION = EntropicaConfig::safeGetEnableShaderpackAdaptiveAttenuation;
    public static final Map<EntropicaBiome, Supplier<Boolean>> BIOME_ENABLED;

    static {
        EnumMap<EntropicaBiome, Supplier<Boolean>> map = new EnumMap<>(EntropicaBiome.class);
        for (EntropicaBiome biome : EntropicaBiome.values()) {
            map.put(biome, () -> isBiomeEnabled(biome));
        }
        BIOME_ENABLED = Collections.unmodifiableMap(map);
    }

    public static boolean isBiomeEnabled(EntropicaBiome biome) {
        return biome != null && getEnableAllBiomes() && getBiomeEnabled(biome);
    }

    public static boolean isBiomeEnabled(String biomeId) {
        EntropicaBiome biome = EntropicaBiome.byId(biomeId);
        return biome != null && isBiomeEnabled(biome);
    }

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
    @ExpectPlatform public static int getMateriaFumeOverpressureLimit() { throw new AssertionError(); }

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
    @ExpectPlatform public static int getExtractionNodeCooldown() { throw new AssertionError(); }

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
    @ExpectPlatform public static int getMaxDelayTicks() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getRequireResearchToCraft() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getFancyMagicCircleProcessing() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableAstralMirrorSkyReflection() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableAstralMirrorParallaxDepth() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableAstralMirror3DBillboardStars() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableAstralMirrorLiquidRefraction() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableAstralMirrorNebulae() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableAstralMirrorConstellationLines() { throw new AssertionError(); }
    @ExpectPlatform public static int getAstralCollectorTransferRate() { throw new AssertionError(); }
    private static volatile @Nullable List<String> testMagnetiteOreWhitelistOverride = null;
    private static volatile @Nullable List<String> testMagnetiteOreBlacklistOverride = null;
    private static volatile @Nullable Boolean testEnhancedBiomeFogAndWaterOverride = null;

    public static void setEnhancedBiomeFogAndWaterTestOverride(@Nullable Boolean enabledOverride) {
        testEnhancedBiomeFogAndWaterOverride = enabledOverride;
    }

    public static void clearEnhancedBiomeFogAndWaterTestOverride() {
        testEnhancedBiomeFogAndWaterOverride = null;
    }

    public static boolean safeGetEnableEnhancedBiomeFogAndWater() {
        Boolean override = testEnhancedBiomeFogAndWaterOverride;
        if (override != null) {
            return override;
        }
        try {
            return getEnableEnhancedBiomeFogAndWater();
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static boolean safeGetEnableBiomeVolumetricFog() {
        if (!safeGetEnableEnhancedBiomeFogAndWater()) {
            return false;
        }
        try {
            return getEnableBiomeVolumetricFog();
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static boolean safeGetEnableBiomeWaterEffects() {
        if (!safeGetEnableEnhancedBiomeFogAndWater()) {
            return false;
        }
        try {
            return getEnableBiomeWaterEffects();
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static boolean safeGetEnableShaderpackAdaptiveAttenuation() {
        try {
            return getEnableShaderpackAdaptiveAttenuation();
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void setMagnetiteBeamOreFilterTestOverride(
            @Nullable List<String> whitelistOverride,
            @Nullable List<String> blacklistOverride
    ) {
        testMagnetiteOreWhitelistOverride = whitelistOverride == null ? null : List.copyOf(whitelistOverride);
        testMagnetiteOreBlacklistOverride = blacklistOverride == null ? null : List.copyOf(blacklistOverride);
    }

    public static void clearMagnetiteBeamOreFilterTestOverride() {
        testMagnetiteOreWhitelistOverride = null;
        testMagnetiteOreBlacklistOverride = null;
    }

    public static List<String> safeGetMagnetiteBeamOreWhitelist() {
        List<String> override = testMagnetiteOreWhitelistOverride;
        if (override != null) {
            return override;
        }
        try {
            List<String> list = getMagnetiteBeamOreWhitelist();
            return list != null ? list : List.of();
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    public static List<String> safeGetMagnetiteBeamOreBlacklist() {
        List<String> override = testMagnetiteOreBlacklistOverride;
        if (override != null) {
            return override;
        }
        try {
            List<String> list = getMagnetiteBeamOreBlacklist();
            return list != null ? list : List.of();
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    /**
     * Checks whether an exposed block may be mined by Earth's Tier 2+ {@code Magnetite Tractor Beam}.
     * Supports block IDs ({@code "minecraft:iron_ore"}) and {@code #tag} entries ({@code "#c:ores/iron"}):
     * <ul>
     *     <li>If whitelist is empty, allows all detected ore blocks UNLESS matched by blacklist.</li>
     *     <li>If whitelist is non-empty, allows ONLY blocks matching whitelist AND NOT matching blacklist.</li>
     * </ul>
     */
    public static boolean isOreAllowedForMagnetiteBeam(@Nullable BlockState state) {
        return isOreAllowedForMagnetiteBeam(
                state,
                safeGetMagnetiteBeamOreWhitelist(),
                safeGetMagnetiteBeamOreBlacklist()
        );
    }

    public static boolean isOreAllowedForMagnetiteBeam(
            @Nullable BlockState state,
            @Nullable List<String> whitelist,
            @Nullable List<String> blacklist
    ) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (matchesMagnetiteOreRuleList(state, blacklist)) {
            return false;
        }
        if (hasNonBlankEntries(whitelist)) {
            return matchesMagnetiteOreRuleList(state, whitelist);
        }
        return isDetectedOreBlock(state);
    }

    public static boolean isDetectedOreBlock(@Nullable BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(Blocks.NETHER_QUARTZ_ORE) || state.is(Blocks.GILDED_BLACKSTONE)
                || state.is(Blocks.ANCIENT_DEBRIS)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = key.getPath().toLowerCase(Locale.ROOT);
        return path.endsWith("_ore") || (path.startsWith("raw_") && path.endsWith("_block"));
    }

    private static boolean hasNonBlankEntries(@Nullable List<String> rules) {
        if (rules == null || rules.isEmpty()) {
            return false;
        }
        for (String rule : rules) {
            if (rule != null && !rule.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesMagnetiteOreRuleList(BlockState state, @Nullable List<String> rules) {
        if (rules == null || rules.isEmpty()) {
            return false;
        }
        for (String raw : rules) {
            if (raw != null && matchesSingleMagnetiteOreRule(state, raw)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesSingleMagnetiteOreRule(BlockState state, String rawRule) {
        String rule = rawRule.trim().toLowerCase(Locale.ROOT);
        if (rule.isEmpty()) {
            return false;
        }
        if (rule.startsWith("#")) {
            String tagStr = rule.substring(1).trim();
            ResourceLocation tagLoc = ResourceLocation.tryParse(tagStr);
            if (tagLoc == null) {
                return false;
            }
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagLoc);
            if (state.is(tagKey)) {
                return true;
            }
            return matchesUnboundHeadlessOreTagFallback(state, tagLoc);
        }
        ResourceLocation targetId = ResourceLocation.tryParse(rule);
        if (targetId == null) {
            return false;
        }
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(targetId);
    }

    private static boolean matchesUnboundHeadlessOreTagFallback(BlockState state, ResourceLocation tagLoc) {
        String ns = tagLoc.getNamespace();
        String tagPath = tagLoc.getPath();
        String blockPath = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase(Locale.ROOT);

        if ("c".equals(ns) || "forge".equals(ns)) {
            TagKey<Block> probeCommonIron = TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ns, "ores/iron")
            );
            if (Blocks.IRON_ORE.defaultBlockState().is(probeCommonIron)) {
                return false;
            }
            if ("ores".equals(tagPath)) {
                return isDetectedOreBlock(state);
            }
            if (tagPath.startsWith("ores/")) {
                String oreName = tagPath.substring("ores/".length());
                if (oreName.isEmpty()) {
                    return false;
                }
                return blockPath.equals(oreName + "_ore")
                        || blockPath.equals("deepslate_" + oreName + "_ore")
                        || blockPath.equals("nether_" + oreName + "_ore")
                        || ("quartz".equals(oreName) && state.is(Blocks.NETHER_QUARTZ_ORE))
                        || ("netherite_scrap".equals(oreName) && state.is(Blocks.ANCIENT_DEBRIS))
                        || ("gold".equals(oreName) && state.is(Blocks.GILDED_BLACKSTONE));
            }
            return false;
        }

        if (ResourceLocation.DEFAULT_NAMESPACE.equals(ns) && !Blocks.IRON_ORE.defaultBlockState().is(BlockTags.IRON_ORES)) {
            if (tagPath.endsWith("_ores")) {
                String oreName = tagPath.substring(0, tagPath.length() - "_ores".length());
                return blockPath.equals(oreName + "_ore")
                        || blockPath.equals("deepslate_" + oreName + "_ore")
                        || ("gold".equals(oreName) && state.is(Blocks.NETHER_GOLD_ORE));
            }
        }
        return false;
    }

    @ExpectPlatform public static int getSpellMutationUnlockMode() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableSpellMutationOpJoinNotice() { throw new AssertionError(); }
    @ExpectPlatform public static List<String> getMagnetiteBeamOreWhitelist() { throw new AssertionError(); }
    @ExpectPlatform public static List<String> getMagnetiteBeamOreBlacklist() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableAllBiomes() { throw new AssertionError(); }
    @ExpectPlatform public static int getTerraBlenderCompatMode() { throw new AssertionError(); }
    @ExpectPlatform public static int getEntropicaBiomeRegionWeight() { throw new AssertionError(); }
    @ExpectPlatform public static int getVanillaBiomePassthroughWeight() { throw new AssertionError(); }
    @ExpectPlatform public static int getBiomeRegionScale() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableBiomeCustomFog() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableBiomeCustomSky() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableBiomeEnvironmentalEffects() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableEnhancedBiomeFogAndWater() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableBiomeVolumetricFog() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableBiomeWaterEffects() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getEnableShaderpackAdaptiveAttenuation() { throw new AssertionError(); }
    @ExpectPlatform public static boolean getBiomeEnabled(EntropicaBiome biome) { throw new AssertionError(); }
}
