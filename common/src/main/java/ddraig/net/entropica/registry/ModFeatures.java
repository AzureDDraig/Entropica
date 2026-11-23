package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.worldgen.feature.AegisCrystalFormationFeature;
import ddraig.net.entropica.worldgen.feature.AmberWeepingWillowFeature;
import ddraig.net.entropica.worldgen.feature.BarrowDeadSpruceFeature;
import ddraig.net.entropica.worldgen.feature.BlueGlacierIceShelfFeature;
import ddraig.net.entropica.worldgen.feature.DustTableIcebergFeature;
import ddraig.net.entropica.worldgen.feature.FjordBasaltColumnFeature;
import ddraig.net.entropica.worldgen.feature.FjordEclipseCairnFeature;
import ddraig.net.entropica.worldgen.feature.FloatingObsidianShardFeature;
import ddraig.net.entropica.worldgen.feature.FrozenDeadSpruceFeature;
import ddraig.net.entropica.worldgen.feature.FulguriteIceSheetFeature;
import ddraig.net.entropica.worldgen.feature.FulguritePineTreeFeature;
import ddraig.net.entropica.worldgen.feature.GiantGlazeCapMushroomFeature;
import ddraig.net.entropica.worldgen.feature.GlazedAmberOakFeature;
import ddraig.net.entropica.worldgen.feature.GlimmerIceShorelineFeature;
import ddraig.net.entropica.worldgen.feature.GlimmerIceSpireFeature;
import ddraig.net.entropica.worldgen.feature.MirageIceArchFeature;
import ddraig.net.entropica.worldgen.feature.NarrowStarlightIcePillarFeature;
import ddraig.net.entropica.worldgen.feature.OasisAmberSpringFeature;
import ddraig.net.entropica.worldgen.feature.RedAlgaeMarginFeature;
import ddraig.net.entropica.worldgen.feature.RottenIceFloeSheetFeature;
import ddraig.net.entropica.worldgen.feature.SanguineIceShelfFeature;
import ddraig.net.entropica.worldgen.feature.SkerriesSteamVentFeature;
import ddraig.net.entropica.worldgen.feature.SmallBarrowCairnFeature;
import ddraig.net.entropica.worldgen.feature.SporeGlazedFloeFeature;
import ddraig.net.entropica.worldgen.feature.SporeWoodTreeFeature;
import ddraig.net.entropica.worldgen.feature.StardustCrystalSpireFeature;
import ddraig.net.entropica.worldgen.feature.SteamingGeyserPoolFeature;
import ddraig.net.entropica.worldgen.feature.StormIcebergFeature;
import ddraig.net.entropica.worldgen.feature.VaporWeaverLichenPatchFeature;
import ddraig.net.entropica.worldgen.feature.VitaeSpringFeature;
import ddraig.net.entropica.worldgen.feature.VoidCrystalBeachPatchFeature;
import ddraig.net.entropica.worldgen.feature.WindBlownDustPatchFeature;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Entropica.MODID, Registries.FEATURE);

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> BLUE_GLACIER_ICE_SHELF =
            FEATURES.register("blue_glacier_ice_shelf",
                    () -> new BlueGlacierIceShelfFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> AEGIS_CRYSTAL_FORMATION =
            FEATURES.register("aegis_crystal_formation",
                    () -> new AegisCrystalFormationFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SKERRIES_STEAM_VENT =
            FEATURES.register("skerries_steam_vent",
                    () -> new SkerriesSteamVentFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> GLIMMER_ICE_SHORELINE =
            FEATURES.register("glimmer_ice_shoreline",
                    () -> new GlimmerIceShorelineFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> ROTTEN_ICE_FLOE_SHEET =
            FEATURES.register("rotten_ice_floe_sheet",
                    () -> new RottenIceFloeSheetFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> FROZEN_DEAD_SPRUCE =
            FEATURES.register("frozen_dead_spruce",
                    () -> new FrozenDeadSpruceFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> GLIMMER_ICE_SPIRE =
            FEATURES.register("glimmer_ice_spire",
                    () -> new GlimmerIceSpireFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> FJORD_BASALT_COLUMNS =
            FEATURES.register("fjord_basalt_columns",
                    () -> new FjordBasaltColumnFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> FJORD_ECLIPSE_CAIRN =
            FEATURES.register("fjord_eclipse_cairn",
                    () -> new FjordEclipseCairnFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> STEAMING_GEYSER_POOL =
            FEATURES.register("steaming_geyser_pool",
                    () -> new SteamingGeyserPoolFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> PATCH_VAPOR_WEAVER_LICHEN =
            FEATURES.register("patch_vapor_weaver_lichen",
                    () -> new VaporWeaverLichenPatchFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> FULGURITE_PINE_TREE =
            FEATURES.register("fulgurite_pine_tree",
                    () -> new FulguritePineTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> FULGURITE_ICE_SHEET =
            FEATURES.register("fulgurite_ice_sheet",
                    () -> new FulguriteIceSheetFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SMALL_BARROW_CAIRN =
            FEATURES.register("small_barrow_cairn",
                    () -> new SmallBarrowCairnFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> BARROW_DEAD_SPRUCE =
            FEATURES.register("barrow_dead_spruce",
                    () -> new BarrowDeadSpruceFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> FLOATING_OBSIDIAN_SHARD =
            FEATURES.register("floating_obsidian_shard",
                    () -> new FloatingObsidianShardFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> VOID_CRYSTAL_BEACH_PATCH =
            FEATURES.register("void_crystal_beach_patch",
                    () -> new VoidCrystalBeachPatchFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> VITAE_SPRING =
            FEATURES.register("vitae_spring",
                    () -> new VitaeSpringFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> GLAZED_AMBER_OAK =
            FEATURES.register("glazed_amber_oak",
                    () -> new GlazedAmberOakFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> STORM_ICEBERG =
            FEATURES.register("storm_iceberg",
                    () -> new StormIcebergFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPORE_WOOD_TREE =
            FEATURES.register("spore_wood_tree",
                    () -> new SporeWoodTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> GIANT_GLAZE_CAP_MUSHROOM =
            FEATURES.register("giant_glaze_cap_mushroom",
                    () -> new GiantGlazeCapMushroomFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPORE_GLAZED_FLOE =
            FEATURES.register("spore_glazed_floe",
                    () -> new SporeGlazedFloeFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> MIRAGE_ICE_ARCH =
            FEATURES.register("mirage_ice_arch",
                    () -> new MirageIceArchFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> OASIS_AMBER_SPRING =
            FEATURES.register("oasis_amber_spring",
                    () -> new OasisAmberSpringFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> AMBER_WEEPING_WILLOW =
            FEATURES.register("amber_weeping_willow",
                    () -> new AmberWeepingWillowFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> DUST_TABLE_ICEBERG =
            FEATURES.register("dust_table_iceberg",
                    () -> new DustTableIcebergFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> WIND_BLOWN_DUST_PATCH =
            FEATURES.register("wind_blown_dust_patch",
                    () -> new WindBlownDustPatchFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SANGUINE_ICE_SHELF =
            FEATURES.register("sanguine_ice_shelf",
                    () -> new SanguineIceShelfFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> RED_ALGAE_MARGIN =
            FEATURES.register("red_algae_margin",
                    () -> new RedAlgaeMarginFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> NARROW_STARLIGHT_ICE_PILLAR =
            FEATURES.register("narrow_starlight_ice_pillar",
                    () -> new NarrowStarlightIcePillarFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> STARDUST_CRYSTAL_SPIRE =
            FEATURES.register("stardust_crystal_spire",
                    () -> new StardustCrystalSpireFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}

    public static void register() {
        FEATURES.register();
    }
}
