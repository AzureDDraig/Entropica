package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.worldgen.structure.AegisShieldingMonolithPiece;
import ddraig.net.entropica.worldgen.structure.AegisShieldingMonolithStructure;
import ddraig.net.entropica.worldgen.structure.AmberWillowBathhousePiece;
import ddraig.net.entropica.worldgen.structure.AmberWillowBathhouseStructure;
import ddraig.net.entropica.worldgen.structure.AuroralVaporObservatoryPiece;
import ddraig.net.entropica.worldgen.structure.AuroralVaporObservatoryStructure;
import ddraig.net.entropica.worldgen.structure.AuroralWellspringPiece;
import ddraig.net.entropica.worldgen.structure.AuroralWellspringStructure;
import ddraig.net.entropica.worldgen.structure.CrimsonTideSanctumPiece;
import ddraig.net.entropica.worldgen.structure.CrimsonTideSanctumStructure;
import ddraig.net.entropica.worldgen.structure.EclipseFjordSanctumPiece;
import ddraig.net.entropica.worldgen.structure.EclipseFjordSanctumStructure;
import ddraig.net.entropica.worldgen.structure.FataMorganaSanctuaryPiece;
import ddraig.net.entropica.worldgen.structure.FataMorganaSanctuaryStructure;
import ddraig.net.entropica.worldgen.structure.FrostTombPiece;
import ddraig.net.entropica.worldgen.structure.FrostTombStructure;
import ddraig.net.entropica.worldgen.structure.FrostVeiledRunicSpirePiece;
import ddraig.net.entropica.worldgen.structure.FrostVeiledRunicSpireStructure;
import ddraig.net.entropica.worldgen.structure.FulguriteCryoRelayPiece;
import ddraig.net.entropica.worldgen.structure.FulguriteCryoRelayStructure;
import ddraig.net.entropica.worldgen.structure.GeothermalVaporousGrottoPiece;
import ddraig.net.entropica.worldgen.structure.GeothermalVaporousGrottoStructure;
import ddraig.net.entropica.worldgen.structure.GlazedMycelialConservatoryPiece;
import ddraig.net.entropica.worldgen.structure.GlazedMycelialConservatoryStructure;
import ddraig.net.entropica.worldgen.structure.IceboundBlightWreckPiece;
import ddraig.net.entropica.worldgen.structure.IceboundBlightWreckStructure;
import ddraig.net.entropica.worldgen.structure.NullFringeRiftBastionPiece;
import ddraig.net.entropica.worldgen.structure.NullFringeRiftBastionStructure;
import ddraig.net.entropica.worldgen.structure.RadiantSpringPavilionPiece;
import ddraig.net.entropica.worldgen.structure.RadiantSpringPavilionStructure;
import ddraig.net.entropica.worldgen.structure.SiltBoundBergWatchPylonPiece;
import ddraig.net.entropica.worldgen.structure.SiltBoundBergWatchPylonStructure;
import ddraig.net.entropica.worldgen.structure.StardustSkerryOrreryPiece;
import ddraig.net.entropica.worldgen.structure.StardustSkerryOrreryStructure;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Entropica.MODID, Registries.STRUCTURE_TYPE);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Entropica.MODID, Registries.STRUCTURE_PIECE);

    public static final ResourceKey<Structure> FROST_VEILED_GLACIAL_RUNIC_SPIRE_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_veiled_glacial_runic_spire"));

    public static final ResourceKey<LootTable> STRUCTURE_1_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/structure_1"));

    public static final ResourceKey<LootTable> STRUCTURE_1_FJORD_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/structure_1_fjord"));

    public static final RegistrySupplier<StructureType<FrostVeiledRunicSpireStructure>> FROST_VEILED_GLACIAL_RUNIC_SPIRE =
            STRUCTURE_TYPES.register("frost_veiled_glacial_runic_spire",
                    () -> () -> FrostVeiledRunicSpireStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> FROST_VEILED_GLACIAL_RUNIC_SPIRE_PIECE =
            STRUCTURE_PIECES.register("frost_veiled_glacial_runic_spire_piece",
                    () -> FrostVeiledRunicSpirePiece::new);

    public static final ResourceKey<Structure> AEGIS_SHIELDING_MONOLITH_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_shielding_monolith"));

    public static final ResourceKey<LootTable> STRUCTURE_2_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/structure_2"));

    public static final RegistrySupplier<StructureType<AegisShieldingMonolithStructure>> AEGIS_SHIELDING_MONOLITH =
            STRUCTURE_TYPES.register("aegis_shielding_monolith",
                    () -> () -> AegisShieldingMonolithStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> AEGIS_SHIELDING_MONOLITH_PIECE =
            STRUCTURE_PIECES.register("aegis_shielding_monolith_piece",
                    () -> AegisShieldingMonolithPiece::new);

    public static final ResourceKey<Structure> AURORAL_WELLSPRING_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_wellspring"));

    public static final ResourceKey<LootTable> STRUCTURE_3_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/structure_3"));

    public static final RegistrySupplier<StructureType<AuroralWellspringStructure>> AURORAL_WELLSPRING =
            STRUCTURE_TYPES.register("auroral_wellspring",
                    () -> () -> AuroralWellspringStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> AURORAL_WELLSPRING_PIECE =
            STRUCTURE_PIECES.register("auroral_wellspring_piece",
                    () -> AuroralWellspringPiece::new);

    public static final ResourceKey<Structure> GEOTHERMAL_VAPOROUS_GROTTO_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "geothermal_vaporous_grotto"));

    public static final ResourceKey<LootTable> STRUCTURE_4_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/structure_4"));

    public static final RegistrySupplier<StructureType<GeothermalVaporousGrottoStructure>> GEOTHERMAL_VAPOROUS_GROTTO =
            STRUCTURE_TYPES.register("geothermal_vaporous_grotto",
                    () -> () -> GeothermalVaporousGrottoStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> GEOTHERMAL_VAPOROUS_GROTTO_PIECE =
            STRUCTURE_PIECES.register("geothermal_vaporous_grotto_piece",
                    () -> GeothermalVaporousGrottoPiece::new);

    public static final ResourceKey<Structure> ICEBOUND_BLIGHT_WRECK_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "icebound_blight_wreck"));

    public static final ResourceKey<LootTable> ICEBOUND_BLIGHT_WRECK_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/icebound_blight_wreck"));

    public static final RegistrySupplier<StructureType<IceboundBlightWreckStructure>> ICEBOUND_BLIGHT_WRECK =
            STRUCTURE_TYPES.register("icebound_blight_wreck",
                    () -> () -> IceboundBlightWreckStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> ICEBOUND_BLIGHT_WRECK_PIECE =
            STRUCTURE_PIECES.register("icebound_blight_wreck_piece",
                    () -> IceboundBlightWreckPiece::new);

    public static final ResourceKey<Structure> ECLIPSE_FJORD_SANCTUM_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_fjord_sanctum"));

    public static final ResourceKey<LootTable> ECLIPSE_FJORD_SANCTUM_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/eclipse_fjord_sanctum"));

    public static final RegistrySupplier<StructureType<EclipseFjordSanctumStructure>> ECLIPSE_FJORD_SANCTUM =
            STRUCTURE_TYPES.register("eclipse_fjord_sanctum",
                    () -> () -> EclipseFjordSanctumStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> ECLIPSE_FJORD_SANCTUM_PIECE =
            STRUCTURE_PIECES.register("eclipse_fjord_sanctum_piece",
                    () -> EclipseFjordSanctumPiece::new);

    public static final ResourceKey<Structure> FULGURITE_CRYO_RELAY_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_cryo_relay"));

    public static final ResourceKey<LootTable> FULGURITE_CRYO_RELAY_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/fulgurite_cryo_relay"));

    public static final RegistrySupplier<StructureType<FulguriteCryoRelayStructure>> FULGURITE_CRYO_RELAY =
            STRUCTURE_TYPES.register("fulgurite_cryo_relay",
                    () -> () -> FulguriteCryoRelayStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> FULGURITE_CRYO_RELAY_PIECE =
            STRUCTURE_PIECES.register("fulgurite_cryo_relay_piece",
                    () -> FulguriteCryoRelayPiece::new);

    public static final ResourceKey<Structure> FROST_TOMB_OF_THE_AEGIS_WARDENS_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_tomb_of_the_aegis_wardens"));

    public static final ResourceKey<LootTable> STRUCTURE_5_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/structure_5"));

    public static final RegistrySupplier<StructureType<FrostTombStructure>> FROST_TOMB_OF_THE_AEGIS_WARDENS =
            STRUCTURE_TYPES.register("frost_tomb_of_the_aegis_wardens",
                    () -> () -> FrostTombStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> FROST_TOMB_OF_THE_AEGIS_WARDENS_PIECE =
            STRUCTURE_PIECES.register("frost_tomb_of_the_aegis_wardens_piece",
                    () -> FrostTombPiece::new);

    public static final ResourceKey<Structure> NULL_FRINGE_RIFT_BASTION_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "null_fringe_rift_bastion"));

    public static final ResourceKey<LootTable> NULL_FRINGE_RIFT_BASTION_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/null_fringe_rift_bastion"));

    public static final RegistrySupplier<StructureType<NullFringeRiftBastionStructure>> NULL_FRINGE_RIFT_BASTION =
            STRUCTURE_TYPES.register("null_fringe_rift_bastion",
                    () -> () -> NullFringeRiftBastionStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> NULL_FRINGE_RIFT_BASTION_PIECE =
            STRUCTURE_PIECES.register("null_fringe_rift_bastion_piece",
                    () -> NullFringeRiftBastionPiece::new);

    public static final ResourceKey<Structure> RADIANT_SPRING_PAVILION_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_spring_pavilion"));

    public static final ResourceKey<LootTable> RADIANT_SPRING_PAVILION_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/radiant_spring_pavilion"));

    public static final RegistrySupplier<StructureType<RadiantSpringPavilionStructure>> RADIANT_SPRING_PAVILION =
            STRUCTURE_TYPES.register("radiant_spring_pavilion",
                    () -> () -> RadiantSpringPavilionStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> RADIANT_SPRING_PAVILION_PIECE =
            STRUCTURE_PIECES.register("radiant_spring_pavilion_piece",
                    () -> RadiantSpringPavilionPiece::new);

    public static final ResourceKey<Structure> AURORAL_VAPOR_OBSERVATORY_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_vapor_observatory"));

    public static final ResourceKey<LootTable> STRUCTURE_6_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/structure_6"));

    public static final RegistrySupplier<StructureType<AuroralVaporObservatoryStructure>> AURORAL_VAPOR_OBSERVATORY =
            STRUCTURE_TYPES.register("auroral_vapor_observatory",
                    () -> () -> AuroralVaporObservatoryStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> AURORAL_VAPOR_OBSERVATORY_PIECE =
            STRUCTURE_PIECES.register("auroral_vapor_observatory_piece",
                    () -> AuroralVaporObservatoryPiece::new);

    public static final ResourceKey<Structure> GLAZED_MYCELIAL_CONSERVATORY_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_mycelial_conservatory"));

    public static final ResourceKey<LootTable> GLAZED_MYCELIAL_CONSERVATORY_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/glazed_mycelial_conservatory"));

    public static final RegistrySupplier<StructureType<GlazedMycelialConservatoryStructure>> GLAZED_MYCELIAL_CONSERVATORY =
            STRUCTURE_TYPES.register("glazed_mycelial_conservatory",
                    () -> () -> GlazedMycelialConservatoryStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> GLAZED_MYCELIAL_CONSERVATORY_PIECE =
            STRUCTURE_PIECES.register("glazed_mycelial_conservatory_piece",
                    () -> GlazedMycelialConservatoryPiece::new);

    public static final ResourceKey<Structure> FATA_MORGANA_SANCTUARY_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fata_morgana_sanctuary"));

    public static final ResourceKey<LootTable> FATA_MORGANA_SANCTUARY_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/fata_morgana_sanctuary"));

    public static final RegistrySupplier<StructureType<FataMorganaSanctuaryStructure>> FATA_MORGANA_SANCTUARY =
            STRUCTURE_TYPES.register("fata_morgana_sanctuary",
                    () -> () -> FataMorganaSanctuaryStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> FATA_MORGANA_SANCTUARY_PIECE =
            STRUCTURE_PIECES.register("fata_morgana_sanctuary_piece",
                    () -> FataMorganaSanctuaryPiece::new);

    public static final ResourceKey<Structure> AMBER_WILLOW_BATHHOUSE_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_willow_bathhouse"));

    public static final ResourceKey<LootTable> AMBER_WILLOW_BATHHOUSE_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/amber_willow_bathhouse"));

    public static final RegistrySupplier<StructureType<AmberWillowBathhouseStructure>> AMBER_WILLOW_BATHHOUSE =
            STRUCTURE_TYPES.register("amber_willow_bathhouse",
                    () -> () -> AmberWillowBathhouseStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> AMBER_WILLOW_BATHHOUSE_PIECE =
            STRUCTURE_PIECES.register("amber_willow_bathhouse_piece",
                    () -> AmberWillowBathhousePiece::new);

    public static final ResourceKey<Structure> SILT_BOUND_BERG_WATCH_PYLON_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silt_bound_berg_watch_pylon"));

    public static final ResourceKey<LootTable> SILT_BOUND_BERG_WATCH_PYLON_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/silt_bound_berg_watch_pylon"));

    public static final RegistrySupplier<StructureType<SiltBoundBergWatchPylonStructure>> SILT_BOUND_BERG_WATCH_PYLON =
            STRUCTURE_TYPES.register("silt_bound_berg_watch_pylon",
                    () -> () -> SiltBoundBergWatchPylonStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> SILT_BOUND_BERG_WATCH_PYLON_PIECE =
            STRUCTURE_PIECES.register("sbbwp",
                    () -> SiltBoundBergWatchPylonPiece::new);

    public static final ResourceKey<Structure> CRIMSON_TIDE_SANCTUM_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crimson_tide_sanctum"));

    public static final ResourceKey<LootTable> CRIMSON_TIDE_SANCTUM_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/crimson_tide_sanctum"));

    public static final RegistrySupplier<StructureType<CrimsonTideSanctumStructure>> CRIMSON_TIDE_SANCTUM =
            STRUCTURE_TYPES.register("crimson_tide_sanctum",
                    () -> () -> CrimsonTideSanctumStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> CRIMSON_TIDE_SANCTUM_PIECE =
            STRUCTURE_PIECES.register("crimson_tide_sanctum_piece",
                    () -> CrimsonTideSanctumPiece::new);

    public static final ResourceKey<Structure> STARDUST_SKERRY_ORRERY_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stardust_skerry_orrery"));

    public static final ResourceKey<LootTable> STARDUST_SKERRY_ORRERY_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/stardust_skerry_orrery"));

    public static final RegistrySupplier<StructureType<StardustSkerryOrreryStructure>> STARDUST_SKERRY_ORRERY =
            STRUCTURE_TYPES.register("stardust_skerry_orrery",
                    () -> () -> StardustSkerryOrreryStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> STARDUST_SKERRY_ORRERY_PIECE =
            STRUCTURE_PIECES.register("stardust_skerry_orrery_piece",
                    () -> StardustSkerryOrreryPiece::new);

    private ModStructures() {}

    public static void register() {
        STRUCTURE_TYPES.register();
        STRUCTURE_PIECES.register();
    }
}
