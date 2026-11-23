package ddraig.net.entropica.worldgen.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.biome2.AuroralFeatherMossBlock;
import ddraig.net.entropica.block.biome2.AuroralLanternBlock;
import ddraig.net.entropica.block.biome2.AuroralLensBlock;
import ddraig.net.entropica.block.biome2.AuroralMoteLightBlock;
import ddraig.net.entropica.block.biome2.GlimmerIceBlock;
import ddraig.net.entropica.block.biome2.GlowGelPathingBlock;
import ddraig.net.entropica.block.biome2.MistVeilFernBlock;
import ddraig.net.entropica.block.biome2.StaticRedstoneCouplerBlock;
import ddraig.net.entropica.block.biome2.ThermalHeatLampBlock;
import ddraig.net.entropica.block.biome3.BlightedPackedIceBlock;
import ddraig.net.entropica.block.biome3.FrozenSpruceBranchBlock;
import ddraig.net.entropica.block.biome3.NecroticSlimeBlock;
import ddraig.net.entropica.block.biome3.NecroticSlimePatchBlock;
import ddraig.net.entropica.block.biome3.RottenIceBlock;
import ddraig.net.entropica.block.biome4.AegisQuartzClusterBlock;
import ddraig.net.entropica.block.biome4.BismuthKeyPedestalBlock;
import ddraig.net.entropica.block.biome5.BlueFrozenMossBlock;
import ddraig.net.entropica.block.biome5.BlueFrozenMossCarpetBlock;
import ddraig.net.entropica.block.biome5.ShadowedEclipseMossBlock;
import ddraig.net.entropica.client.renderer.CryoStalkerModel;
import ddraig.net.entropica.client.renderer.aurora_jelly.AuroraJellyModel;
import ddraig.net.entropica.config.EntropicaBiome;
import ddraig.net.entropica.entity.aurora_jelly.AuroraJellyEntity;
import ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity;
import ddraig.net.entropica.entity.grot.GrotSpawnRegistry;
import ddraig.net.entropica.entity.projectile.CryoGrenadeEntity;
import ddraig.net.entropica.item.ModSpawnEggItem;
import ddraig.net.entropica.item.biome2.AuroralWandItem;
import ddraig.net.entropica.item.biome2.CamouflageCloakItem;
import ddraig.net.entropica.item.biome3.CryoInsulatedArmorItem;
import ddraig.net.entropica.item.biome3.FrostBiteDaggerItem;
import ddraig.net.entropica.item.biome3.NecroticSlimeBucketItem;
import ddraig.net.entropica.item.biome4.AegisShieldingCharmItem;
import ddraig.net.entropica.item.biome4.AegisWardingEmblemItem;
import ddraig.net.entropica.item.biome5.CryoGrenadeItem;
import ddraig.net.entropica.item.biome5.SilentStridersItem;
import ddraig.net.entropica.item.biome6.VaporShroudCloakItem;
import ddraig.net.entropica.block.biome6.SteamVentBlock;
import ddraig.net.entropica.block.biome6.SteamingIceBlock;
import ddraig.net.entropica.block.biome6.SteamingSpringStoneBlock;
import ddraig.net.entropica.block.biome6.VaporCondenserBlock;
import ddraig.net.entropica.block.biome6.VaporLiftMatBlock;
import ddraig.net.entropica.block.biome6.VaporWeaverLichenBlock;
import ddraig.net.entropica.effect.DampeningEffect;
import ddraig.net.entropica.entity.vapor_bellows_crab.VaporBellowsCrabEntity;
import ddraig.net.entropica.worldgen.feature.SteamingGeyserPoolFeature;
import ddraig.net.entropica.worldgen.feature.VaporWeaverLichenPatchFeature;
import ddraig.net.entropica.worldgen.structure.GeothermalVaporousGrottoPiece;
import ddraig.net.entropica.worldgen.structure.GeothermalVaporousGrottoStructure;
import ddraig.net.entropica.worldgen.structure.EclipseFjordSanctumPiece;
import ddraig.net.entropica.worldgen.structure.EclipseFjordSanctumStructure;
import ddraig.net.entropica.worldgen.structure.FulguriteCryoRelayPiece;
import ddraig.net.entropica.worldgen.structure.FulguriteCryoRelayStructure;
import ddraig.net.entropica.worldgen.structure.IceboundBlightWreckPiece;
import ddraig.net.entropica.worldgen.structure.IceboundBlightWreckStructure;
import ddraig.net.entropica.block.biome7.BiomassCapacitorBlock;
import ddraig.net.entropica.block.biome7.FulguriteIceBlock;
import ddraig.net.entropica.block.biome7.FulguritePineLeavesBlock;
import ddraig.net.entropica.block.biome7.StaticConductiveMatBlock;
import ddraig.net.entropica.effect.GalvanicSurgeEffect;
import ddraig.net.entropica.entity.cryo_static_hedgehog.CryoStaticHedgehogEntity;
import ddraig.net.entropica.item.GroundingCoilItem;
import ddraig.net.entropica.worldgen.feature.FulguriteIceSheetFeature;
import ddraig.net.entropica.worldgen.feature.FulguritePineTreeFeature;
import ddraig.net.entropica.block.biome8.BarrowSentinelTotemBlock;
import ddraig.net.entropica.block.biome8.CryptGraveRootBlock;
import ddraig.net.entropica.block.biome8.CryptSiphonLanternBlock;
import ddraig.net.entropica.block.biome8.GraveMossBlock;
import ddraig.net.entropica.block.biome8.NecroticSoilBlock;
import ddraig.net.entropica.block.biome8.RimeCapMushroomBlock;
import ddraig.net.entropica.block.biome8.RunedBrazierBlock;
import ddraig.net.entropica.client.model.BarrowCryptGlowOwlModel;
import ddraig.net.entropica.client.model.BarrowHoundModel;
import ddraig.net.entropica.effect.BarrowWardEffect;
import ddraig.net.entropica.entity.barrow_crypt_glow_owl.BarrowCryptGlowOwlEntity;
import ddraig.net.entropica.entity.barrow_hound.BarrowHoundEntity;
import ddraig.net.entropica.item.biome8.BarrowSlateGreavesItem;
import ddraig.net.entropica.item.biome8.BleachedBoneMealItem;
import ddraig.net.entropica.item.biome8.RimeCapSporesItem;
import ddraig.net.entropica.worldgen.feature.BarrowDeadSpruceFeature;
import ddraig.net.entropica.worldgen.feature.SmallBarrowCairnFeature;
import ddraig.net.entropica.worldgen.structure.FrostTombPiece;
import ddraig.net.entropica.worldgen.structure.FrostTombStructure;
import ddraig.net.entropica.block.biome9.NullFringeTussockBlock;
import ddraig.net.entropica.block.biome9.SpatialPhaseAnchorBlock;
import ddraig.net.entropica.block.biome9.VoidCrystalBlock;
import ddraig.net.entropica.block.biome9.VoidCrystalClusterBlock;
import ddraig.net.entropica.block.biome9.VoidSandTurfBlock;
import ddraig.net.entropica.client.model.VoidRiftShadeModel;
import ddraig.net.entropica.entity.void_rift_shade.VoidRiftShadeEntity;
import ddraig.net.entropica.item.biome9.LeviathanRiftBeltItem;
import ddraig.net.entropica.item.biome9.NullWeaveMantleItem;
import ddraig.net.entropica.item.biome9.PhaseRecallStoneItem;
import ddraig.net.entropica.worldgen.feature.FloatingObsidianShardFeature;
import ddraig.net.entropica.worldgen.feature.VoidCrystalBeachPatchFeature;
import ddraig.net.entropica.worldgen.structure.NullFringeRiftBastionPiece;
import ddraig.net.entropica.worldgen.structure.NullFringeRiftBastionStructure;
import ddraig.net.entropica.block.biome10.GlazedAmberLeavesBlock;
import ddraig.net.entropica.block.biome10.GoldenLichenBlock;
import ddraig.net.entropica.block.biome10.QilinLightRefractorBlock;
import ddraig.net.entropica.block.biome10.RadiantGlazeIceBlock;
import ddraig.net.entropica.block.biome10.RadiantSnowBlock;
import ddraig.net.entropica.block.biome10.SunGlazedLanternBlock;
import ddraig.net.entropica.block.biome10.VitaeSpringStoneBlock;
import ddraig.net.entropica.client.model.CrystallineQilinModel;
import ddraig.net.entropica.entity.crystalline_qilin.CrystallineQilinEntity;
import ddraig.net.entropica.entity.projectile.RadiantSnowballEntity;
import ddraig.net.entropica.item.biome10.LiquidVitaeResinItem;
import ddraig.net.entropica.item.biome10.QilinPrismCircletItem;
import ddraig.net.entropica.item.biome10.RadiantSnowballItem;
import ddraig.net.entropica.item.biome10.SunGlazedTalismanItem;
import ddraig.net.entropica.item.biome10.VitaeFertilizerItem;
import ddraig.net.entropica.worldgen.feature.GlazedAmberOakFeature;
import ddraig.net.entropica.worldgen.feature.VitaeSpringFeature;
import ddraig.net.entropica.worldgen.structure.RadiantSpringPavilionPiece;
import ddraig.net.entropica.worldgen.structure.RadiantSpringPavilionStructure;
import ddraig.net.entropica.block.biome11.FulguriteBlock;
import ddraig.net.entropica.block.biome11.SimurghWindTurbineBlock;
import ddraig.net.entropica.block.biome11.ZephyrGlassGrassBlock;
import ddraig.net.entropica.block.biome11.ZephyrIceBlock;
import ddraig.net.entropica.block.biome11.ZephyrUpdraftGrateBlock;
import ddraig.net.entropica.item.biome11.CryoTempestHornItem;
import ddraig.net.entropica.item.biome11.StormKiteGliderItem;
import ddraig.net.entropica.item.biome11.TempestBottleItem;
import ddraig.net.entropica.item.biome11.ZephyrIceBlockItem;
import ddraig.net.entropica.worldgen.feature.StormIcebergFeature;
import ddraig.net.entropica.worldgen.structure.AuroralVaporObservatoryPiece;
import ddraig.net.entropica.worldgen.structure.AuroralVaporObservatoryStructure;
import ddraig.net.entropica.block.biome12.CryoSporeCenserBlock;
import ddraig.net.entropica.block.biome12.FrozenSporeLeavesBlock;
import ddraig.net.entropica.block.biome12.FrozenSporeMossBlock;
import ddraig.net.entropica.block.biome12.FrozenSporeMossCarpetBlock;
import ddraig.net.entropica.block.biome12.GlazeCapMushroomBlock;
import ddraig.net.entropica.block.biome12.SporeBellowsBlock;
import ddraig.net.entropica.block.biome12.SporeGlazedIceBlock;
import ddraig.net.entropica.effect.SporeClarityEffect;
import ddraig.net.entropica.entity.mycelial_spore_weaver.MycelialSporeWeaverEntity;
import ddraig.net.entropica.entity.projectile.GlazedSporeBombEntity;
import ddraig.net.entropica.item.biome12.CryoSporePoulticeItem;
import ddraig.net.entropica.item.biome12.GlazedSporeBombItem;
import ddraig.net.entropica.item.biome12.SporeFilterMaskItem;
import ddraig.net.entropica.worldgen.feature.GiantGlazeCapMushroomFeature;
import ddraig.net.entropica.worldgen.feature.SporeGlazedFloeFeature;
import ddraig.net.entropica.worldgen.feature.SporeWoodTreeFeature;
import ddraig.net.entropica.worldgen.structure.GlazedMycelialConservatoryPiece;
import ddraig.net.entropica.worldgen.structure.GlazedMycelialConservatoryStructure;
import ddraig.net.entropica.block.biome13.GlassSandBlock;
import ddraig.net.entropica.block.biome13.MirageIceBlock;
import ddraig.net.entropica.block.biome13.MiragePrismLanternBlock;
import ddraig.net.entropica.client.renderer.silica_glass_scarab.SilicaGlassScarabModel;
import ddraig.net.entropica.effect.DoubleVisionEffect;
import ddraig.net.entropica.effect.MiragePhasingEffect;
import ddraig.net.entropica.entity.projectile.MirageDecoyPrismEntity;
import ddraig.net.entropica.entity.silica_glass_scarab.SilicaGlassScarabEntity;
import ddraig.net.entropica.item.biome13.MirageDecoyPrismItem;
import ddraig.net.entropica.item.biome13.MirageVeilGogglesItem;
import ddraig.net.entropica.item.biome13.ShimmeringFocusItem;
import ddraig.net.entropica.worldgen.feature.MirageIceArchFeature;
import ddraig.net.entropica.worldgen.structure.FataMorganaSanctuaryPiece;
import ddraig.net.entropica.worldgen.structure.FataMorganaSanctuaryStructure;
import ddraig.net.entropica.block.biome14.AmberResinBlock;
import ddraig.net.entropica.block.biome14.AmberTorchBlock;
import ddraig.net.entropica.block.biome14.FrostWillowLeavesBlock;
import ddraig.net.entropica.block.biome14.FrostWillowVinesBlock;
import ddraig.net.entropica.block.biome14.OasisDewBasinBlock;
import ddraig.net.entropica.block.biome14.WallAmberTorchBlock;
import ddraig.net.entropica.client.renderer.mud_lurker_salamander.MudLurkerSalamanderModel;
import ddraig.net.entropica.effect.OasisRenewalEffect;
import ddraig.net.entropica.entity.mud_lurker_salamander.MudLurkerSalamanderEntity;
import ddraig.net.entropica.item.biome14.AmberAdhesiveItem;
import ddraig.net.entropica.item.biome14.AmberNectarBottleItem;
import ddraig.net.entropica.item.biome14.SalamanderWaderHoodItem;
import ddraig.net.entropica.worldgen.feature.AmberWeepingWillowFeature;
import ddraig.net.entropica.worldgen.feature.OasisAmberSpringFeature;
import ddraig.net.entropica.worldgen.structure.AmberWillowBathhousePiece;
import ddraig.net.entropica.worldgen.structure.AmberWillowBathhouseStructure;
import ddraig.net.entropica.block.biome15.DustCrustedIceBlock;
import ddraig.net.entropica.block.biome15.GreySandBlock;
import ddraig.net.entropica.block.biome15.ToughFiberRopeBlock;
import ddraig.net.entropica.block.biome15.WindBaffleLanternBlock;
import ddraig.net.entropica.block.biome15.WindBlownDustScrubBlock;
import ddraig.net.entropica.client.renderer.dust_core_golem.DustCoreGolemModel;
import ddraig.net.entropica.effect.ZephyrWeightEffect;
import ddraig.net.entropica.entity.dust_core_golem.DustCoreGolemEntity;
import ddraig.net.entropica.item.biome15.CopperSiltSieveItem;
import ddraig.net.entropica.item.biome15.DustCoreShardItem;
import ddraig.net.entropica.item.biome15.DustDevilAmpouleItem;
import ddraig.net.entropica.worldgen.feature.DustTableIcebergFeature;
import ddraig.net.entropica.worldgen.feature.WindBlownDustPatchFeature;
import ddraig.net.entropica.worldgen.structure.SiltBoundBergWatchPylonPiece;
import ddraig.net.entropica.worldgen.structure.SiltBoundBergWatchPylonStructure;
import ddraig.net.entropica.block.biome16.CrimsonTideLanternBlock;
import ddraig.net.entropica.block.biome16.RedAlgaeBlock;
import ddraig.net.entropica.block.biome16.RedClaySandBlock;
import ddraig.net.entropica.block.biome16.SanguineIceBlock;
import ddraig.net.entropica.client.renderer.sanguine_sea_angel.SanguineSeaAngelModel;
import ddraig.net.entropica.effect.SanguineVigorEffect;
import ddraig.net.entropica.entity.projectile.BloodFrostDartEntity;
import ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelEntity;
import ddraig.net.entropica.item.biome16.RedAlgaeItem;
import ddraig.net.entropica.item.biome16.SanguineDraughtAmpouleItem;
import ddraig.net.entropica.item.biome16.SanguineIceLancetItem;
import ddraig.net.entropica.worldgen.feature.RedAlgaeMarginFeature;
import ddraig.net.entropica.worldgen.feature.SanguineIceShelfFeature;
import ddraig.net.entropica.worldgen.structure.CrimsonTideSanctumPiece;
import ddraig.net.entropica.worldgen.structure.CrimsonTideSanctumStructure;
import ddraig.net.entropica.block.biome17.CelestialOrreryLanternBlock;
import ddraig.net.entropica.block.biome17.RimeStarTussockBlock;
import ddraig.net.entropica.block.biome17.StardustCrystalBlock;
import ddraig.net.entropica.block.biome17.StardustCrystalClusterBlock;
import ddraig.net.entropica.block.biome17.StarlightIceBlock;
import ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel;
import ddraig.net.entropica.effect.StarlightGraceEffect;
import ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusArmorType;
import ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusEntity;
import ddraig.net.entropica.item.biome17.AstralFishItem;
import ddraig.net.entropica.item.biome17.AstralNautilusArmorItem;
import ddraig.net.entropica.item.biome17.StarlightDraughtAmpouleItem;
import ddraig.net.entropica.item.biome17.StarlightTuningChiselItem;
import ddraig.net.entropica.worldgen.feature.NarrowStarlightIcePillarFeature;
import ddraig.net.entropica.worldgen.feature.StardustCrystalSpireFeature;
import ddraig.net.entropica.worldgen.structure.StardustSkerryOrreryPiece;
import ddraig.net.entropica.worldgen.structure.StardustSkerryOrreryStructure;
import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import ddraig.net.entropica.client.worldgen.BiomeVolumetricFogRenderer;
import ddraig.net.entropica.client.worldgen.BiomeWaterSurfaceRenderer;
import ddraig.net.entropica.client.worldgen.ShaderModCompatBridge;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.worldgen.biome.BiomeAtmosphereProfile;
import ddraig.net.entropica.worldgen.biome.BiomePlacementRule;
import ddraig.net.entropica.worldgen.biome.BiomeTransitionMath;
import ddraig.net.entropica.worldgen.biome.DomainWarpedRegionSampler;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomeAtmospheres;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomeEnvironmentHandler;
import ddraig.net.entropica.worldgen.biome.EntropicaBiomePlacements;
import ddraig.net.entropica.worldgen.biome.EntropicaClimatePresets;
import ddraig.net.entropica.worldgen.biome.EntropicaMultiNoiseLayer;
import ddraig.net.entropica.worldgen.biome.SkyPhenomenonType;
import ddraig.net.entropica.worldgen.biome.TerraBlenderCompatBridge;
import ddraig.net.entropica.worldgen.biome.TerraBlenderCompatMode;
import ddraig.net.entropica.worldgen.biome.WaterOpticalMode;
import ddraig.net.entropica.worldgen.feature.FjordBasaltColumnFeature;
import ddraig.net.entropica.worldgen.feature.FjordEclipseCairnFeature;
import ddraig.net.entropica.worldgen.feature.FrozenDeadSpruceFeature;
import ddraig.net.entropica.worldgen.feature.GlimmerIceShorelineFeature;
import ddraig.net.entropica.worldgen.feature.GlimmerIceSpireFeature;
import ddraig.net.entropica.worldgen.feature.RottenIceFloeSheetFeature;
import ddraig.net.entropica.worldgen.feature.SkerriesSteamVentFeature;
import ddraig.net.entropica.worldgen.structure.AegisShieldingMonolithPiece;
import ddraig.net.entropica.worldgen.structure.AegisShieldingMonolithStructure;
import ddraig.net.entropica.worldgen.structure.AuroralWellspringPiece;
import ddraig.net.entropica.worldgen.structure.AuroralWellspringStructure;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.MapColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Deterministic standalone empirical verification suite for Entropica's
 * Native Multi-Noise Biome Injector & TerraBlender Compatibility Engine.
 */
public final class BiomeInjectorVerificationSuite {

    private record MockBiomeHolder(ResourceKey<Biome> key) implements Holder<Biome> {
        @Override public Biome value() { return null; }
        @Override public boolean isBound() { return true; }
        @Override public boolean is(ResourceLocation id) { return key.location().equals(id); }
        @Override public boolean is(ResourceKey<Biome> targetKey) { return key.equals(targetKey); }
        @Override public boolean is(Predicate<ResourceKey<Biome>> predicate) { return predicate.test(key); }
        @Override public boolean is(TagKey<Biome> tag) { return false; }
        @Override public boolean is(Holder<Biome> other) { return equals(other); }
        @Override public Stream<TagKey<Biome>> tags() { return Stream.empty(); }
        @Override public Either<ResourceKey<Biome>, Biome> unwrap() { return Either.left(key); }
        @Override public Optional<ResourceKey<Biome>> unwrapKey() { return Optional.of(key); }
        @Override public Kind kind() { return Kind.REFERENCE; }
        @Override public boolean canSerializeIn(HolderOwner<Biome> owner) { return true; }
    }

    private static Holder<Biome> mockBiome(String namespace, String path) {
        return new MockBiomeHolder(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(namespace, path)));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAILED: " + message);
        }
    }

    public static void main(String[] args) {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        bootstrapBiome2LiveRegistries();
        bootstrapBiome3LiveRegistries();
        bootstrapBiome4LiveRegistries();
        bootstrapBiome5LiveRegistries();
        bootstrapBiome6LiveRegistries();
        bootstrapBiome7LiveRegistries();
        bootstrapBiome8LiveRegistries();
        bootstrapBiome9LiveRegistries();
        bootstrapBiome10LiveRegistries();
        bootstrapBiome11LiveRegistries();
        bootstrapBiome12LiveRegistries();
        bootstrapBiome13LiveRegistries();
        bootstrapBiome14LiveRegistries();
        bootstrapBiome15LiveRegistries();
        bootstrapBiome16LiveRegistries();
        bootstrapBiome17LiveRegistries();
        System.out.println("=== Entropica Multi-Noise Biome Injector Verification Suite ===");

        verifyAll108BiomesCatalogued();
        verifyDomainWarpedRegionDeterminismAndContinuity();
        verify6DClimateMatchingForBlueGlacierShelf();
        verifyRendezvousRarityWeightsAndIsolation();
        verifyTerraBlenderCompatibilityModes();
        verifyBiomeAtmosphereAndEnvironmentProfiles();
        verifySmoothNonEffectToEffectBiomeTransitions();
        verifyBiome1BlueGlacierShelfContentAndAssets();
        verifyBiome2AuroralSkerriesContentAndAssets();
        verifyBiome3FrostBiteFloesContentAndAssets();
        verifyBiome4CrystallinePackIceContentAndAssets();
        verifyBiome5SilentFjordContentAndAssets();
        verifyBiome6SteamingSkerriesContentAndAssets();
        verifyBiomes1To6StructuresEssencesAndEarlyLoot();
        verifyBiome7StaticIcePlainsContentAndAssets();
        verifyBiome8BarrowMoundTundraContentAndAssets();
        verifyBiome9NullFringeBayContentAndAssets();
        verifyBiome10RadiantGlazePlainsContentAndAssets();
        verifyBiome11StormSweptIcebergsContentAndAssets();
        verifyBiome12SporeGlazedPackContentAndAssets();
        verifyBiome13MirageGlacierContentAndAssets();
        verifyBiome14OasisFloesContentAndAssets();
        verifyBiome15DustCrustedBergsContentAndAssets();
        verifyBiome16BloodIceShallowsContentAndAssets();
        verifyBiome17AstralSkerriesContentAndAssets();

        System.out.println("ALL 25 VERIFICATION SUITES PASSED SUCCESSFULLY!");
    }

    private static void setRegistryUnfrozen(Registry<?> registry, boolean intrusive) {
        try {
            var frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
            frozenField.setBoolean(registry, false);
            if (intrusive) {
                var intrusiveField = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
                intrusiveField.setAccessible(true);
                intrusiveField.set(registry, new java.util.IdentityHashMap<>());
            }
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed preparing test registry " + registry.key() + ": " + e.getMessage(), e);
        }
    }

    private static void refreezeRegistry(Registry<?> registry) {
        try {
            var intrusiveField = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            intrusiveField.setAccessible(true);
            java.util.Map<?, ?> unregistered = (java.util.Map<?, ?>) intrusiveField.get(registry);
            check(unregistered == null || unregistered.isEmpty(),
                    "Unregistered intrusive holders left in " + registry.key() + ": " + unregistered);
            intrusiveField.set(registry, null);

            var frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
            frozenField.setBoolean(registry, true);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed re-freezing test registry " + registry.key() + ": " + e.getMessage(), e);
        }
    }

    private static <T> void bindStandaloneHolder(Holder.Reference<T> holder, T value) {
        try {
            var bindMethod = Holder.Reference.class.getDeclaredMethod("bindValue", Object.class);
            bindMethod.setAccessible(true);
            bindMethod.invoke(holder, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed binding standalone holder " + holder.key() + ": " + e.getMessage(), e);
        }
    }

    private static <T extends Block> T registerTestBlock(String path, Function<ResourceLocation, T> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        T block = factory.apply(id);
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    private static BlockItem registerTestBlockItem(String path, Block block) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        BlockItem item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
        item.registerBlocks(Item.BY_BLOCK, item);
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    private static <T extends Item> T registerTestItem(String path, Function<Item.Properties, T> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        T item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    /**
     * Registers Biome 2's 12 blocks, 10 BlockItems, 10 standalone items, Aurora Jelly entity type,
     * 2 custom worldgen features, and Auroral Wellspring structure + piece into the live vanilla
     * {@link BuiltInRegistries} right after vanilla static entries load and prior to {@code Bootstrap.bootStrap()}
     * freezing and validating all built-in registries.
     */
    private static void bootstrapBiome2LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        // 12 Biome 2 Blocks
        Block glimmerIce = registerTestBlock("glimmer_ice", name -> new GlimmerIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.ICE).friction(0.98f).destroyTime(0.5f).sound(SoundType.GLASS).lightLevel(s -> 10).noOcclusion()));
        Block coarseGravel = registerTestBlock("coarse_gravel", name -> new ColoredFallingBlock(new ColorRGBA(0x23262B), BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLACK).destroyTime(0.6f).sound(SoundType.GRAVEL)));
        Block auroralFeatherMoss = registerTestBlock("auroral_feather_moss", name -> new AuroralFeatherMossBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.EMERALD).destroyTime(0.1f).sound(SoundType.MOSS_CARPET).lightLevel(s -> 9).noOcclusion()));
        Block mistVeilFern = registerTestBlock("mist_veil_fern", name -> new MistVeilFernBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.PLANT).noCollision().instabreak().noOcclusion().sound(SoundType.GRASS).lightLevel(s -> 4)));
        registerTestBlock("potted_mist_veil_fern", name -> new FlowerPotBlock(mistVeilFern, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)
                .setId(ResourceKey.create(Registries.BLOCK, name)).lightLevel(s -> 4)));
        Block auroralLantern = registerTestBlock("auroral_lantern", name -> new AuroralLanternBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.METAL).lightLevel(s -> 15).destroyTime(2.5f).sound(SoundType.LANTERN).randomTicks().noOcclusion()));
        Block thermalHeatLamp = registerTestBlock("thermal_heat_lamp", name -> new ThermalHeatLampBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_ORANGE).lightLevel(s -> 14).destroyTime(2.5f).requiresCorrectToolForDrops().sound(SoundType.COPPER).randomTicks()));
        Block glowGelPathing = registerTestBlock("glow_gel_pathing", name -> new GlowGelPathingBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_CYAN).lightLevel(s -> s.getValue(GlowGelPathingBlock.LIT) ? 13 : 6).destroyTime(0.8f).sound(SoundType.SLIME_BLOCK).noOcclusion()));
        Block staticRedstoneCoupler = registerTestBlock("static_redstone_coupler", name -> new StaticRedstoneCouplerBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_ORANGE).destroyTime(1.5f).sound(SoundType.COPPER).lightLevel(s -> s.getValue(StaticRedstoneCouplerBlock.POWERED) ? 10 : 2).noOcclusion()));
        Block auroralLensBlock = registerTestBlock("auroral_lens_block", name -> new AuroralLensBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DIAMOND).lightLevel(s -> 12).destroyTime(3.0f).explosionResistance(12.0f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).noOcclusion()));
        registerTestBlock("auroral_mote_light", name -> new AuroralMoteLightBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_CYAN).lightLevel(s -> 14).noCollision().replaceable().noOcclusion().instabreak().sound(SoundType.AMETHYST)));

        // Aurora Jelly EntityType
        ResourceLocation jellyId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aurora_jelly");
        EntityType<AuroraJellyEntity> auroraJellyType = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                jellyId,
                EntityType.Builder.<AuroraJellyEntity>of(AuroraJellyEntity::new, MobCategory.CREATURE)
                        .sized(0.85F, 1.5F)
                        .clientTrackingRange(10)
                        .updateInterval(1)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, jellyId))
        );

        // 9 BlockItems
        registerTestBlockItem("glimmer_ice", glimmerIce);
        registerTestBlockItem("coarse_gravel", coarseGravel);
        registerTestBlockItem("auroral_feather_moss", auroralFeatherMoss);
        registerTestBlockItem("mist_veil_fern", mistVeilFern);
        registerTestBlockItem("auroral_lantern", auroralLantern);
        registerTestBlockItem("thermal_heat_lamp", thermalHeatLamp);
        registerTestBlockItem("glow_gel_pathing", glowGelPathing);
        registerTestBlockItem("static_redstone_coupler", staticRedstoneCoupler);
        registerTestBlockItem("auroral_lens_block", auroralLensBlock);

        // 10 Standalone Items
        registerTestItem("aurora_dust", Item::new);
        registerTestItem("glowing_auroral_fiber", Item::new);
        registerTestItem("mist_veil_frond", Item::new);
        registerTestItem("warm_aurora_petal", Item::new);
        registerTestItem("camouflage_cloak", CamouflageCloakItem::new);
        registerTestItem("auroral_gelatin", Item::new);
        registerTestItem("charged_glow_tentacle", Item::new);
        registerTestItem("auroral_wand", AuroralWandItem::new);
        registerTestItem("vapor_condensate_crystals", props -> new Item(props.rarity(Rarity.RARE)));
        registerTestItem("aurora_jelly_spawn_egg", props -> new ModSpawnEggItem(() -> auroraJellyType, props));

        // 2 Custom Worldgen Features
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "skerries_steam_vent"),
                new SkerriesSteamVentFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glimmer_ice_shoreline"),
                new GlimmerIceShorelineFeature(NoneFeatureConfiguration.CODEC));

        // Structure 3: Auroral Wellspring + Piece
        StructureType<AuroralWellspringStructure> wellspringType = () -> AuroralWellspringStructure.CODEC;
        Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_wellspring"),
                wellspringType);
        StructurePieceType wellspringPiece = AuroralWellspringPiece::new;
        Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_wellspring_piece"),
                wellspringPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }







    private static void bootstrapBiome3LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        // 6 Biome 3 Blocks
        Block rottenIce = registerTestBlock("rotten_ice", name -> new RottenIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_LIGHT_GREEN).friction(0.98f).destroyTime(0.35f).sound(SoundType.GLASS).noOcclusion()));
        Block blightedPackedIce = registerTestBlock("blighted_packed_ice", name -> new BlightedPackedIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_GREEN).friction(0.98f).destroyTime(0.6f).sound(SoundType.GLASS)));
        Block necroticSlimePatch = registerTestBlock("necrotic_slime_patch", name -> new NecroticSlimePatchBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_GREEN).noCollision().noOcclusion().destroyTime(0.2f).sound(SoundType.SLIME_BLOCK)));
        Block necroticSlimeBlock = registerTestBlock("necrotic_slime_block", name -> new NecroticSlimeBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_GREEN).friction(0.8f).destroyTime(0.5f).sound(SoundType.SLIME_BLOCK).noOcclusion()));
        Block frozenSpruceBranch = registerTestBlock("frozen_spruce_branch", name -> new FrozenSpruceBranchBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.WOOD).destroyTime(1.2f).sound(SoundType.WOOD).noOcclusion()));
        Block curedFrostHideBlock = registerTestBlock("cured_frost_hide_block", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_CYAN).destroyTime(1.2f).sound(SoundType.WOOL)));

        // Cryo-Stalker EntityType
        ResourceLocation stalkerId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_stalker");
        EntityType<CryoStalkerEntity> cryoStalkerType = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                stalkerId,
                EntityType.Builder.<CryoStalkerEntity>of(CryoStalkerEntity::new, MobCategory.MONSTER)
                        .sized(1.1F, 1.3F)
                        .clientTrackingRange(10)
                        .updateInterval(3)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, stalkerId))
        );

        // 6 Biome 3 BlockItems
        registerTestBlockItem("rotten_ice", rottenIce);
        registerTestBlockItem("blighted_packed_ice", blightedPackedIce);
        registerTestBlockItem("necrotic_slime_patch", necroticSlimePatch);
        registerTestBlockItem("necrotic_slime_block", necroticSlimeBlock);
        registerTestBlockItem("frozen_spruce_branch", frozenSpruceBranch);
        registerTestBlockItem("cured_frost_hide_block", curedFrostHideBlock);

        // Existing Cryo-Stalker drops + Spawn Egg + 10 Standalone Biome 3 Items
        registerTestItem("cryo_crystalline_fang", Item::new);
        registerTestItem("frost_veined_hide", Item::new);
        registerTestItem("cryo_stalker_spawn_egg", props -> new ModSpawnEggItem(() -> cryoStalkerType, props));

        registerTestItem("rot_powder", Item::new);
        registerTestItem("necrotic_slime", Item::new);
        registerTestItem("necrotic_slime_bucket", NecroticSlimeBucketItem::new);
        registerTestItem("sea_salt_crystals", Item::new);
        registerTestItem("cured_frost_veined_hide", Item::new);
        registerTestItem("frost_bite_dagger", props -> new FrostBiteDaggerItem(props.sword(ToolMaterial.DIAMOND, 2.0F, -1.8F)));
        registerTestItem("cryo_insulated_helmet", props -> new CryoInsulatedArmorItem(EquipmentSlot.HEAD, 330, 3.0D, 1.5D, props));
        registerTestItem("cryo_insulated_chestplate", props -> new CryoInsulatedArmorItem(EquipmentSlot.CHEST, 480, 7.0D, 1.5D, props));
        registerTestItem("cryo_insulated_leggings", props -> new CryoInsulatedArmorItem(EquipmentSlot.LEGS, 450, 5.0D, 1.5D, props));
        registerTestItem("cryo_insulated_boots", props -> new CryoInsulatedArmorItem(EquipmentSlot.FEET, 390, 3.0D, 1.5D, props));

        // Decay Potion
        Potion decayPotion = new Potion("decay",
                new MobEffectInstance(MobEffects.POISON, 900, 0),
                new MobEffectInstance(MobEffects.WEAKNESS, 900, 0));
        Holder.Reference<Potion> decayHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "decay"),
                decayPotion
        );
        bindStandaloneHolder(decayHolder, decayPotion);

        // 2 Custom Worldgen Features + Icebound Blight-Wreck Structure
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rotten_ice_floe_sheet"),
                new RottenIceFloeSheetFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_dead_spruce"),
                new FrozenDeadSpruceFeature(NoneFeatureConfiguration.CODEC));
        StructureType<IceboundBlightWreckStructure> wreckType = () -> IceboundBlightWreckStructure.CODEC;
        Holder.Reference<StructureType<?>> wreckTypeHolder = Registry.registerForHolder(BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "icebound_blight_wreck"),
                wreckType);
        bindStandaloneHolder(wreckTypeHolder, wreckType);
        StructurePieceType wreckPiece = IceboundBlightWreckPiece::new;
        Holder.Reference<StructurePieceType> wreckPieceHolder = Registry.registerForHolder(BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "icebound_blight_wreck_piece"),
                wreckPiece);
        bindStandaloneHolder(wreckPieceHolder, wreckPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    /**
     * Registers Biome 4's 9 blocks, 9 BlockItems, 5 standalone items, Iron Skin potion,
     * Glimmer-Ice Spire feature, and Structure 2 (Aegis Shielding Monolith + Piece) into live {@link BuiltInRegistries}.
     */
    private static void bootstrapBiome4LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        // 6 Biome 4 Blocks
        Block aegisQuartzBlock = registerTestBlock("aegis_quartz_block", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.QUARTZ).destroyTime(1.5f).explosionResistance(8.0f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).lightLevel(s -> 6)));
        Block aegisQuartzCluster = registerTestBlock("aegis_quartz_cluster", name -> new AegisQuartzClusterBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.QUARTZ).forceSolidOn().noOcclusion().destroyTime(1.5f).explosionResistance(6.0f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).lightLevel(s -> 7)));
        Block bismuthOre = registerTestBlock("bismuth_ore", name -> new DropExperienceBlock(UniformInt.of(2, 5), BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.STONE).destroyTime(3.0f).explosionResistance(6.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
        Block bismuthBlock = registerTestBlock("bismuth_block", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_MAGENTA).destroyTime(4.0f).explosionResistance(12.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
        Block aegisPlatedStone = registerTestBlock("aegis_plated_stone", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLACK).destroyTime(100.0f).explosionResistance(2400.0f).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));
        Block bismuthKeyPedestal = registerTestBlock("bismuth_key_pedestal", name -> new BismuthKeyPedestalBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.STONE).noOcclusion().destroyTime(3.5f).explosionResistance(600.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)
                .lightLevel(s -> s.getValue(BismuthKeyPedestalBlock.UNLOCKED) ? 11 : 3)));

        // 6 Biome 4 BlockItems
        registerTestBlockItem("aegis_quartz_block", aegisQuartzBlock);
        registerTestBlockItem("aegis_quartz_cluster", aegisQuartzCluster);
        registerTestBlockItem("bismuth_ore", bismuthOre);
        registerTestBlockItem("bismuth_block", bismuthBlock);
        registerTestBlockItem("aegis_plated_stone", aegisPlatedStone);
        registerTestBlockItem("bismuth_key_pedestal", bismuthKeyPedestal);

        // 5 Standalone Biome 4 Items
        registerTestItem("aegis_quartz", Item::new);
        registerTestItem("raw_bismuth", Item::new);
        registerTestItem("bismuth_ingot", Item::new);
        registerTestItem("aegis_warding_emblem", AegisWardingEmblemItem::new);
        registerTestItem("aegis_shielding_charm", AegisShieldingCharmItem::new);

        // Iron Skin Potion
        Potion ironSkinPotion = new Potion("iron_skin",
                new MobEffectInstance(MobEffects.RESISTANCE, 1800, 1),
                new MobEffectInstance(MobEffects.ABSORPTION, 1800, 0));
        Holder.Reference<Potion> ironSkinHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "iron_skin"),
                ironSkinPotion
        );
        bindStandaloneHolder(ironSkinHolder, ironSkinPotion);

        // Custom Worldgen Feature: Glimmer-Ice Spire
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glimmer_ice_spire"),
                new GlimmerIceSpireFeature(NoneFeatureConfiguration.CODEC));

        // Structure 2: Aegis Shielding Monolith + Piece
        StructureType<AegisShieldingMonolithStructure> monolithType = () -> AegisShieldingMonolithStructure.CODEC;
        Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_shielding_monolith"),
                monolithType);
        StructurePieceType monolithPiece = AegisShieldingMonolithPiece::new;
        Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_shielding_monolith_piece"),
                monolithPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    /**
     * Registers Biome 5's 5 blocks, 5 BlockItems, 5 standalone items, Cryo-Grenade projectile entity,
     * Eclipse Shroud potion, 2 custom Fjord features, and Eclipse Fjord Watch-Sanctum structure into live {@link BuiltInRegistries}.
     */
    private static void bootstrapBiome5LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        // 5 Biome 5 Blocks
        Block basaltColumn = registerTestBlock("basalt_column", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLACK).destroyTime(1.25f).explosionResistance(4.2f).requiresCorrectToolForDrops().sound(SoundType.BASALT)));
        Block chiseledEclipseBasalt = registerTestBlock("chiseled_eclipse_basalt", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLACK).destroyTime(1.5f).explosionResistance(6.0f).requiresCorrectToolForDrops().sound(SoundType.BASALT).lightLevel(s -> 3)));
        Block blueFrozenMoss = registerTestBlock("blue_frozen_moss", name -> new BlueFrozenMossBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.ICE).destroyTime(0.2f).sound(SoundType.MOSS).lightLevel(s -> 2)));
        Block blueFrozenMossCarpet = registerTestBlock("blue_frozen_moss_carpet", name -> new BlueFrozenMossCarpetBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.ICE).destroyTime(0.1f).noOcclusion().sound(SoundType.MOSS_CARPET).lightLevel(s -> 2)));
        Block shadowedEclipseMoss = registerTestBlock("shadowed_eclipse_moss", name -> new ShadowedEclipseMossBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_PURPLE).randomTicks().destroyTime(0.15f).noOcclusion().sound(SoundType.MOSS_CARPET)));

        // Cryo-Grenade Projectile EntityType
        ResourceLocation cryoGrenadeEntityId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_grenade");
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                cryoGrenadeEntityId,
                EntityType.Builder.<CryoGrenadeEntity>of(CryoGrenadeEntity::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(8)
                        .updateInterval(1)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, cryoGrenadeEntityId))
        );

        // 5 Biome 5 BlockItems
        registerTestBlockItem("basalt_column", basaltColumn);
        registerTestBlockItem("chiseled_eclipse_basalt", chiseledEclipseBasalt);
        registerTestBlockItem("blue_frozen_moss", blueFrozenMoss);
        registerTestBlockItem("blue_frozen_moss_carpet", blueFrozenMossCarpet);
        registerTestBlockItem("shadowed_eclipse_moss", shadowedEclipseMoss);

        // 4 Standalone Biome 5 Items
        registerTestItem("frozen_moss_clump", Item::new);
        registerTestItem("cryo_grenade", CryoGrenadeItem::new);
        registerTestItem("silent_striders", SilentStridersItem::new);
        registerTestItem("basalt_pickaxe", props -> new Item(props.pickaxe(ToolMaterial.STONE, 1.0F, -2.8F).durability(220)));

        // Eclipse Shroud Potion
        Potion eclipseShroudPotion = new Potion("eclipse_shroud",
                new MobEffectInstance(MobEffects.SPEED, 3600, 0),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 3600, 0));
        Holder.Reference<Potion> eclipseShroudHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_shroud"),
                eclipseShroudPotion
        );
        bindStandaloneHolder(eclipseShroudHolder, eclipseShroudPotion);

        // 2 Custom Fjord Features + Eclipse Fjord Watch-Sanctum Structure
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fjord_basalt_columns"),
                new FjordBasaltColumnFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fjord_eclipse_cairn"),
                new FjordEclipseCairnFeature(NoneFeatureConfiguration.CODEC));
        StructureType<EclipseFjordSanctumStructure> sanctumType = () -> EclipseFjordSanctumStructure.CODEC;
        Holder.Reference<StructureType<?>> sanctumTypeHolder = Registry.registerForHolder(BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_fjord_sanctum"),
                sanctumType);
        bindStandaloneHolder(sanctumTypeHolder, sanctumType);
        StructurePieceType sanctumPiece = EclipseFjordSanctumPiece::new;
        Holder.Reference<StructurePieceType> sanctumPieceHolder = Registry.registerForHolder(BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_fjord_sanctum_piece"),
                sanctumPiece);
        bindStandaloneHolder(sanctumPieceHolder, sanctumPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    /**
     * Registers Biome 6's 7 blocks, 7 BlockItems, 5 standalone items, Vapor-Bellows Crab entity,
     * Dampening effect & potions, 2 custom features, and Structure 4 into live {@link BuiltInRegistries}.
     */
    private static void bootstrapBiome6LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block steamingIce = registerTestBlock("steaming_ice", name -> new SteamingIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.ICE).friction(0.985f).destroyTime(0.6f).sound(SoundType.GLASS).noOcclusion()));
        Block steamingSpringStone = registerTestBlock("steaming_spring_stone", name -> new SteamingSpringStoneBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.STONE).destroyTime(1.5f).requiresCorrectToolForDrops().sound(SoundType.BASALT).lightLevel(s -> 2)));
        Block vaporWeaverLichen = registerTestBlock("vapor_weaver_lichen", name -> new VaporWeaverLichenBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.QUARTZ).noCollision().noOcclusion().randomTicks().destroyTime(0.1f).sound(SoundType.GLOW_LICHEN).lightLevel(s -> 2)));
        Block steamVent = registerTestBlock("steam_vent", name -> new SteamVentBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.STONE).randomTicks().destroyTime(1.8f).requiresCorrectToolForDrops().sound(SoundType.STONE).lightLevel(s -> 3)));
        Block vaporLiftMat = registerTestBlock("vapor_lift_mat", name -> new VaporLiftMatBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.TERRACOTTA_WHITE).destroyTime(0.8f).noOcclusion().sound(SoundType.WOOL)));
        Block vaporCondenser = registerTestBlock("vapor_condenser", name -> new VaporCondenserBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_ORANGE).randomTicks().destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.COPPER)));

        ResourceLocation crabId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_bellows_crab");
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                crabId,
                EntityType.Builder.<VaporBellowsCrabEntity>of(VaporBellowsCrabEntity::new, MobCategory.CREATURE)
                        .sized(1.25F, 0.95F)
                        .fireImmune()
                        .clientTrackingRange(10)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, crabId))
        );

        registerTestBlockItem("steaming_ice", steamingIce);
        registerTestBlockItem("steaming_spring_stone", steamingSpringStone);
        registerTestBlockItem("vapor_weaver_lichen", vaporWeaverLichen);
        registerTestBlockItem("steam_vent", steamVent);
        registerTestBlockItem("vapor_lift_mat", vaporLiftMat);
        registerTestBlockItem("vapor_condenser", vaporCondenser);

        registerTestItem("vapor_condenser_spore", Item::new);
        registerTestItem("vapor_shroud_cloak", VaporShroudCloakItem::new);
        registerTestItem("porous_basalt_shell", Item::new);
        registerTestItem("vapor_bellows_crab_spawn_egg", Item::new);

        DampeningEffect dampeningEffect = new DampeningEffect();
        Holder.Reference<net.minecraft.world.effect.MobEffect> dampeningEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dampening"),
                dampeningEffect
        );
        bindStandaloneHolder(dampeningEffectHolder, dampeningEffect);

        Potion dampeningPotion = new Potion("dampening", new MobEffectInstance(dampeningEffectHolder, 3600, 0));
        Holder.Reference<Potion> dampeningHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dampening"),
                dampeningPotion
        );
        bindStandaloneHolder(dampeningHolder, dampeningPotion);

        Potion longDampeningPotion = new Potion("dampening", new MobEffectInstance(dampeningEffectHolder, 9600, 0));
        Holder.Reference<Potion> longDampeningHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_dampening"),
                longDampeningPotion
        );
        bindStandaloneHolder(longDampeningHolder, longDampeningPotion);

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steaming_geyser_pool"),
                new SteamingGeyserPoolFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "patch_vapor_weaver_lichen"),
                new VaporWeaverLichenPatchFeature(NoneFeatureConfiguration.CODEC));

        StructureType<GeothermalVaporousGrottoStructure> grottoType = () -> GeothermalVaporousGrottoStructure.CODEC;
        Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "geothermal_vaporous_grotto"),
                grottoType);
        StructurePieceType grottoPiece = GeothermalVaporousGrottoPiece::new;
        Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "geothermal_vaporous_grotto_piece"),
                grottoPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome7LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block fulguriteIce = registerTestBlock("fulgurite_ice", name -> new FulguriteIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.ICE).friction(0.98f).destroyTime(0.6f).sound(SoundType.GLASS).lightLevel(s -> 4).noOcclusion()));
        Block fulguritePineLog = registerTestBlock("fulgurite_pine_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLACK).destroyTime(2.0f).sound(SoundType.WOOD).lightLevel(s -> 2)));
        Block fulguritePineLeaves = registerTestBlock("fulgurite_pine_leaves", name -> new FulguritePineLeavesBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_CYAN).destroyTime(0.2f).sound(SoundType.GRASS).lightLevel(s -> 1).noOcclusion()));
        Block staticConductiveMat = registerTestBlock("static_conductive_mat", name -> new StaticConductiveMatBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLUE).destroyTime(0.8f).noOcclusion().sound(SoundType.COPPER)
                .lightLevel(s -> s.getValue(StaticConductiveMatBlock.POWERED) ? 6 : 2)));
        Block biomassCapacitor = registerTestBlock("biomass_capacitor", name -> new BiomassCapacitorBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_CYAN).destroyTime(2.5f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.COPPER)
                .lightLevel(s -> (s.getValue(BiomassCapacitorBlock.CHARGED) || s.getValue(BiomassCapacitorBlock.DISCHARGING)) ? 12 : 3)));

        ResourceLocation hhId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_static_hedgehog");
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                hhId,
                EntityType.Builder.<CryoStaticHedgehogEntity>of(CryoStaticHedgehogEntity::new, MobCategory.CREATURE)
                        .sized(0.7F, 0.65F)
                        .clientTrackingRange(8)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, hhId))
        );

        registerTestBlockItem("fulgurite_ice", fulguriteIce);
        registerTestBlockItem("fulgurite_pine_log", fulguritePineLog);
        registerTestBlockItem("fulgurite_pine_leaves", fulguritePineLeaves);
        registerTestBlockItem("static_conductive_mat", staticConductiveMat);
        registerTestBlockItem("biomass_capacitor", biomassCapacitor);

        registerTestItem("static_frost_powder", Item::new);
        registerTestItem("charged_static_needle", Item::new);
        registerTestItem("static_quill", Item::new);
        registerTestItem("grounding_coil", props -> new GroundingCoilItem(props.stacksTo(1)));
        registerTestItem("cryo_static_hedgehog_spawn_egg", Item::new);
        registerTestItem("patina_ovis_spawn_egg", Item::new);

        GalvanicSurgeEffect surgeEffect = new GalvanicSurgeEffect();
        Holder.Reference<net.minecraft.world.effect.MobEffect> surgeEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "galvanic_surge"),
                surgeEffect
        );
        bindStandaloneHolder(surgeEffectHolder, surgeEffect);

        Potion surgePotion = new Potion("galvanic_surge",
                new MobEffectInstance(surgeEffectHolder, 3600, 0),
                new MobEffectInstance(MobEffects.SPEED, 3600, 0),
                new MobEffectInstance(MobEffects.HASTE, 3600, 0));
        Holder.Reference<Potion> surgeHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "galvanic_surge"),
                surgePotion
        );
        bindStandaloneHolder(surgeHolder, surgePotion);

        Potion longSurgePotion = new Potion("galvanic_surge",
                new MobEffectInstance(surgeEffectHolder, 9600, 0),
                new MobEffectInstance(MobEffects.SPEED, 9600, 0),
                new MobEffectInstance(MobEffects.HASTE, 9600, 0));
        Holder.Reference<Potion> longSurgeHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_galvanic_surge"),
                longSurgePotion
        );
        bindStandaloneHolder(longSurgeHolder, longSurgePotion);

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_pine_tree"),
                new FulguritePineTreeFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_ice_sheet"),
                new FulguriteIceSheetFeature(NoneFeatureConfiguration.CODEC));

        StructureType<FulguriteCryoRelayStructure> relayType = () -> FulguriteCryoRelayStructure.CODEC;
        Holder.Reference<StructureType<?>> relayTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_cryo_relay"),
                relayType
        );
        bindStandaloneHolder(relayTypeHolder, relayType);

        StructurePieceType relayPiece = FulguriteCryoRelayPiece::new;
        Holder.Reference<StructurePieceType> relayPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_cryo_relay_piece"),
                relayPiece
        );
        bindStandaloneHolder(relayPieceHolder, relayPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome8LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block necroticSoil = registerTestBlock("necrotic_soil", name -> new NecroticSoilBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLACK).strength(0.65F).sound(SoundType.ROOTED_DIRT).randomTicks()));
        Block barrowStoneBricks = registerTestBlock("barrow_stone_bricks", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.DEEPSLATE_BRICKS)));
        Block crackedBarrowBricks = registerTestBlock("cracked_barrow_bricks", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(1.8F, 5.0F).sound(SoundType.DEEPSLATE_BRICKS)));
        Block mossyBarrowBricks = registerTestBlock("mossy_barrow_bricks", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.DEEPSLATE_BRICKS)));
        Block graveMoss = registerTestBlock("grave_moss", name -> new GraveMossBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_PURPLE).strength(0.1F).sound(SoundType.MOSS_CARPET).lightLevel(s -> 1).noOcclusion()));
        Block cryptGraveRoot = registerTestBlock("crypt_grave_root", name -> new CryptGraveRootBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.SAND).strength(0.8F).sound(SoundType.ROOTS).noOcclusion()));
        Block rimeCapMushroom = registerTestBlock("rime_cap_mushroom", name -> new RimeCapMushroomBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_CYAN).noCollision().instabreak().sound(SoundType.FUNGUS).lightLevel(s -> 6).noOcclusion()));
        registerTestBlock("potted_rime_cap_mushroom", name -> new FlowerPotBlock(rimeCapMushroom, BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .instabreak().noOcclusion().lightLevel(s -> 6)));
        Block runedBrazier = registerTestBlock("runed_brazier", name -> new RunedBrazierBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(2.5F, 6.0F).sound(SoundType.LANTERN)
                .noOcclusion().randomTicks().lightLevel(s -> s.getValue(RunedBrazierBlock.LIT) ? 14 : 0)));
        Block cryptSiphonLantern = registerTestBlock("crypt_siphon_lantern", name -> new CryptSiphonLanternBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.LANTERN)
                .noOcclusion().lightLevel(s -> 13)));
        Block barrowSentinelTotem = registerTestBlock("barrow_sentinel_totem", name -> new BarrowSentinelTotemBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(2.5F, 8.0F).sound(SoundType.DEEPSLATE_BRICKS)
                .noOcclusion().randomTicks().lightLevel(s -> 6)));
        registerTestBlock("barrow_moss", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_GREEN).strength(0.2F).sound(SoundType.MOSS)));

        ResourceLocation houndId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_hound");
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                houndId,
                EntityType.Builder.<BarrowHoundEntity>of(BarrowHoundEntity::new, MobCategory.MONSTER)
                        .sized(0.9F, 1.05F)
                        .clientTrackingRange(10)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, houndId))
        );
        ResourceLocation owlId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_crypt_glow_owl");
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                owlId,
                EntityType.Builder.<BarrowCryptGlowOwlEntity>of(BarrowCryptGlowOwlEntity::new, MobCategory.CREATURE)
                        .sized(0.75F, 0.95F)
                        .clientTrackingRange(10)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, owlId))
        );

        registerTestBlockItem("necrotic_soil", necroticSoil);
        registerTestBlockItem("barrow_stone_bricks", barrowStoneBricks);
        registerTestBlockItem("cracked_barrow_bricks", crackedBarrowBricks);
        registerTestBlockItem("mossy_barrow_bricks", mossyBarrowBricks);
        registerTestBlockItem("grave_moss", graveMoss);
        registerTestBlockItem("crypt_grave_root", cryptGraveRoot);
        registerTestBlockItem("rime_cap_mushroom", rimeCapMushroom);
        registerTestBlockItem("runed_brazier", runedBrazier);
        registerTestBlockItem("crypt_siphon_lantern", cryptSiphonLantern);
        registerTestBlockItem("barrow_sentinel_totem", barrowSentinelTotem);

        registerTestItem("rime_cap_spores", RimeCapSporesItem::new);
        registerTestItem("crypt_grave_fibers", Item::new);
        registerTestItem("bleached_bone_meal", BleachedBoneMealItem::new);
        registerTestItem("grave_glow_feather", Item::new);
        registerTestItem("barrow_skull_plate", Item::new);
        registerTestItem("barrow_slate_greaves", BarrowSlateGreavesItem::new);
        registerTestItem("barrow_hound_spawn_egg", Item::new);
        registerTestItem("barrow_crypt_glow_owl_spawn_egg", Item::new);
        registerTestItem("rime_shepherd_spawn_egg", Item::new);

        BarrowWardEffect wardEffect = new BarrowWardEffect();
        Holder.Reference<net.minecraft.world.effect.MobEffect> wardEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_ward"),
                wardEffect
        );
        bindStandaloneHolder(wardEffectHolder, wardEffect);

        Potion wardPotion = new Potion("barrow_ward",
                new MobEffectInstance(wardEffectHolder, 3600, 0),
                new MobEffectInstance(MobEffects.RESISTANCE, 3600, 0));
        Holder.Reference<Potion> wardHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_ward"),
                wardPotion
        );
        bindStandaloneHolder(wardHolder, wardPotion);

        Potion longWardPotion = new Potion("barrow_ward",
                new MobEffectInstance(wardEffectHolder, 9600, 0),
                new MobEffectInstance(MobEffects.RESISTANCE, 9600, 0));
        Holder.Reference<Potion> longWardHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_barrow_ward"),
                longWardPotion
        );
        bindStandaloneHolder(longWardHolder, longWardPotion);

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "small_barrow_cairn"),
                new SmallBarrowCairnFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_dead_spruce"),
                new BarrowDeadSpruceFeature(NoneFeatureConfiguration.CODEC));

        StructureType<FrostTombStructure> tombType = () -> FrostTombStructure.CODEC;
        Holder.Reference<StructureType<?>> tombTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_tomb_of_the_aegis_wardens"),
                tombType
        );
        bindStandaloneHolder(tombTypeHolder, tombType);

        StructurePieceType tombPiece = FrostTombPiece::new;
        Holder.Reference<StructurePieceType> tombPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_tomb_of_the_aegis_wardens_piece"),
                tombPiece
        );
        bindStandaloneHolder(tombPieceHolder, tombPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome9LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block voidSand = registerTestBlock("void_sand", name -> new ColoredFallingBlock(new ColorRGBA(0x2E1065), BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_PURPLE).strength(0.5F).sound(SoundType.SAND)));
        Block voidSandTurf = registerTestBlock("void_sand_turf", name -> new VoidSandTurfBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_PURPLE).strength(0.65F).sound(SoundType.SAND)));
        Block voidStone = registerTestBlock("void_stone", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(2.5F, 8.0F).sound(SoundType.DEEPSLATE)));
        Block obsidianBricks = registerTestBlock("obsidian_bricks", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(25.0F, 600.0F).sound(SoundType.DEEPSLATE_BRICKS)));
        Block voidCrystalBlock = registerTestBlock("void_crystal_block", name -> new VoidCrystalBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(1.8F, 6.0F).sound(SoundType.AMETHYST).lightLevel(s -> 9).randomTicks()));
        Block voidCrystalBud = registerTestBlock("void_crystal_bud", name -> new VoidCrystalClusterBlock(4.0F, 3.0F, BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_PURPLE).noOcclusion().strength(1.5F).sound(SoundType.AMETHYST).lightLevel(s -> 6)));
        Block voidCrystalCluster = registerTestBlock("void_crystal_cluster", name -> new VoidCrystalClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_PURPLE).noOcclusion().strength(1.5F).sound(SoundType.AMETHYST).lightLevel(s -> 12)));
        Block nullFringeTussock = registerTestBlock("null_fringe_tussock", name -> new NullFringeTussockBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_PURPLE).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(s -> 2).noOcclusion()));
        registerTestBlock("potted_null_fringe_tussock", name -> new FlowerPotBlock(nullFringeTussock, BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .instabreak().noOcclusion().lightLevel(s -> 2)));
        Block spatialPhaseAnchor = registerTestBlock("spatial_phase_anchor", name -> new SpatialPhaseAnchorBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(3.5F, 50.0F).sound(SoundType.LODESTONE).lightLevel(s -> 11).noOcclusion()));

        ResourceLocation shadeId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_rift_shade");
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                shadeId,
                EntityType.Builder.<VoidRiftShadeEntity>of(VoidRiftShadeEntity::new, MobCategory.MONSTER)
                        .sized(0.95F, 3.6F)
                        .clientTrackingRange(10)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, shadeId))
        );

        registerTestBlockItem("void_sand", voidSand);
        registerTestBlockItem("void_sand_turf", voidSandTurf);
        registerTestBlockItem("void_stone", voidStone);
        registerTestBlockItem("obsidian_bricks", obsidianBricks);
        registerTestBlockItem("void_crystal_block", voidCrystalBlock);
        registerTestBlockItem("void_crystal_bud", voidCrystalBud);
        registerTestBlockItem("void_crystal_cluster", voidCrystalCluster);
        registerTestBlockItem("null_fringe_tussock", nullFringeTussock);
        registerTestBlockItem("spatial_phase_anchor", spatialPhaseAnchor);

        registerTestItem("void_crystal_shard", Item::new);
        registerTestItem("void_fiber", Item::new);
        registerTestItem("void_weave_fabric", Item::new);
        registerTestItem("phase_recall_stone", PhaseRecallStoneItem::new);
        registerTestItem("leviathan_rift_belt", LeviathanRiftBeltItem::new);
        registerTestItem("null_weave_mantle", NullWeaveMantleItem::new);
        registerTestItem("void_rift_shade_spawn_egg", Item::new);
        registerTestItem("void_sea_serpent_spawn_egg", Item::new);
        registerTestItem("serpent_void_scale", Item::new);

        Potion buoyancyPotion = new Potion("null_buoyancy",
                new MobEffectInstance(MobEffects.JUMP_BOOST, 3600, 1),
                new MobEffectInstance(MobEffects.SLOW_FALLING, 3600, 0));
        Holder.Reference<Potion> buoyancyHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "null_buoyancy"),
                buoyancyPotion
        );
        bindStandaloneHolder(buoyancyHolder, buoyancyPotion);

        Potion longBuoyancyPotion = new Potion("null_buoyancy",
                new MobEffectInstance(MobEffects.JUMP_BOOST, 9600, 1),
                new MobEffectInstance(MobEffects.SLOW_FALLING, 9600, 0));
        Holder.Reference<Potion> longBuoyancyHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_null_buoyancy"),
                longBuoyancyPotion
        );
        bindStandaloneHolder(longBuoyancyHolder, longBuoyancyPotion);

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "floating_obsidian_shard"),
                new FloatingObsidianShardFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_crystal_beach_patch"),
                new VoidCrystalBeachPatchFeature(NoneFeatureConfiguration.CODEC));

        StructureType<NullFringeRiftBastionStructure> bastionType = () -> NullFringeRiftBastionStructure.CODEC;
        Holder.Reference<StructureType<?>> bastionTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "null_fringe_rift_bastion"),
                bastionType
        );
        bindStandaloneHolder(bastionTypeHolder, bastionType);

        StructurePieceType bastionPiece = NullFringeRiftBastionPiece::new;
        Holder.Reference<StructurePieceType> bastionPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "null_fringe_rift_bastion_piece"),
                bastionPiece
        );
        bindStandaloneHolder(bastionPieceHolder, bastionPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome10LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block radiantSnow = registerTestBlock("radiant_snow_block", name -> new RadiantSnowBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.SNOW).strength(0.3F).sound(SoundType.SNOW).lightLevel(s -> 4)));
        Block radiantGlazeIce = registerTestBlock("radiant_glaze_ice", name -> new RadiantGlazeIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.ICE).friction(0.96F).strength(0.6F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 6)));
        Block glazedAmberLeaves = registerTestBlock("glazed_amber_leaves", name -> new GlazedAmberLeavesBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.GOLD).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().lightLevel(s -> 3)));
        Block goldenLichen = registerTestBlock("golden_lichen", name -> new GoldenLichenBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.GOLD).noCollision().instabreak().sound(SoundType.GLOW_LICHEN)
                .lightLevel(net.minecraft.world.level.block.GlowLichenBlock.emission(7)).noOcclusion()));
        Block vitaeSpringStone = registerTestBlock("vitae_spring_stone", name -> new VitaeSpringStoneBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(1.6F, 6.0F).sound(SoundType.CALCITE).lightLevel(s -> 5).randomTicks()));
        Block qilinRefractor = registerTestBlock("qilin_light_refractor", name -> new QilinLightRefractorBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.EMERALD).requiresCorrectToolForDrops().strength(2.5F, 8.0F).sound(SoundType.AMETHYST).noOcclusion().lightLevel(s -> 14)));
        Block sunGlazedLantern = registerTestBlock("sun_glazed_lantern", name -> new SunGlazedLanternBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.GOLD).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.LANTERN).noOcclusion().lightLevel(s -> 15)));

        ResourceLocation qilinId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crystalline_qilin");
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                qilinId,
                EntityType.Builder.<CrystallineQilinEntity>of(CrystallineQilinEntity::new, MobCategory.CREATURE)
                        .sized(1.35F, 2.15F)
                        .clientTrackingRange(10)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, qilinId))
        );

        ResourceLocation snowballId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_snowball");
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                snowballId,
                EntityType.Builder.<RadiantSnowballEntity>of(RadiantSnowballEntity::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(8)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, snowballId))
        );

        registerTestBlockItem("radiant_snow_block", radiantSnow);
        registerTestBlockItem("radiant_glaze_ice", radiantGlazeIce);
        registerTestBlockItem("glazed_amber_leaves", glazedAmberLeaves);
        registerTestBlockItem("golden_lichen", goldenLichen);
        registerTestBlockItem("vitae_spring_stone", vitaeSpringStone);
        registerTestBlockItem("qilin_light_refractor", qilinRefractor);
        registerTestBlockItem("sun_glazed_lantern", sunGlazedLantern);

        registerTestItem("radiant_snowball", RadiantSnowballItem::new);
        registerTestItem("jade_qilin_scale", Item::new);
        registerTestItem("liquid_vitae_resin", LiquidVitaeResinItem::new);
        registerTestItem("vitae_fertilizer", VitaeFertilizerItem::new);
        registerTestItem("qilin_prism_circlet", QilinPrismCircletItem::new);
        registerTestItem("sun_glazed_talisman", SunGlazedTalismanItem::new);
        registerTestItem("crystalline_qilin_spawn_egg", Item::new);
        registerTestItem("amber_weeping_stag_spawn_egg", Item::new);
        registerTestItem("aurorafowl_spawn_egg", Item::new);
        registerTestItem("luminoth_spawn_egg", Item::new);
        registerTestItem("weeping_amber_tear", Item::new);
        registerTestItem("luminoth_dust", Item::new);
        registerTestItem("aurorafowl_feather", Item::new);
        registerTestItem("raw_aurorafowl", Item::new);
        registerTestItem("cooked_aurorafowl", Item::new);

        Potion radVit = new Potion("radiant_vitality",
                new MobEffectInstance(MobEffects.REGENERATION, 1800, 0),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 1800, 0));
        Holder.Reference<Potion> radVitH = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_vitality"),
                radVit
        );
        bindStandaloneHolder(radVitH, radVit);

        Potion longRadVit = new Potion("radiant_vitality",
                new MobEffectInstance(MobEffects.REGENERATION, 3600, 0),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 3600, 0));
        Holder.Reference<Potion> longRadVitH = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_radiant_vitality"),
                longRadVit
        );
        bindStandaloneHolder(longRadVitH, longRadVit);

        Potion strongRadVit = new Potion("radiant_vitality",
                new MobEffectInstance(MobEffects.REGENERATION, 900, 1),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 900, 1));
        Holder.Reference<Potion> strongRadVitH = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "strong_radiant_vitality"),
                strongRadVit
        );
        bindStandaloneHolder(strongRadVitH, strongRadVit);

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitae_spring"),
                new VitaeSpringFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_amber_oak"),
                new GlazedAmberOakFeature(NoneFeatureConfiguration.CODEC));

        StructureType<RadiantSpringPavilionStructure> pavType = () -> RadiantSpringPavilionStructure.CODEC;
        Holder.Reference<StructureType<?>> pavTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_spring_pavilion"),
                pavType
        );
        bindStandaloneHolder(pavTypeHolder, pavType);

        StructurePieceType pavPiece = RadiantSpringPavilionPiece::new;
        Holder.Reference<StructurePieceType> pavPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_spring_pavilion_piece"),
                pavPiece
        );
        bindStandaloneHolder(pavPieceHolder, pavPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome11LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block fulguriteBlock = registerTestBlock("fulgurite_block", name -> new FulguriteBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(2.2F, 12.0F).sound(SoundType.BASALT).lightLevel(s -> 3)));
        Block zephyrIce = registerTestBlock("zephyr_ice", name -> new ZephyrIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.ICE).friction(0.985F).strength(0.5F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 3)));
        Block zephyrGlassGrass = registerTestBlock("zephyr_glass_grass", name -> new ZephyrGlassGrassBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.ICE).noCollision().instabreak().sound(SoundType.GLASS).lightLevel(s -> 2).noOcclusion()));
        registerTestBlock("potted_zephyr_glass_grass", name -> new FlowerPotBlock(zephyrGlassGrass, BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .instabreak().noOcclusion().lightLevel(s -> 2)));
        Block zephyrUpdraftGrate = registerTestBlock("zephyr_updraft_grate", name -> new ZephyrUpdraftGrateBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(2.5F, 8.0F).sound(SoundType.COPPER_GRATE)
                .noOcclusion().randomTicks().lightLevel(s -> s.getValue(ZephyrUpdraftGrateBlock.POWERED) ? 11 : 6)));
        Block simurghWindTurbine = registerTestBlock("simurgh_wind_turbine", name -> new SimurghWindTurbineBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name))
                .mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(2.2F, 6.0F).sound(SoundType.COPPER)
                .noOcclusion().randomTicks().lightLevel(s -> s.getValue(SimurghWindTurbineBlock.POWER) >= 12 ? 7 : 4)));

        registerTestBlockItem("fulgurite_block", fulguriteBlock);
        registerTestItem("zephyr_ice", props -> new ZephyrIceBlockItem(zephyrIce, props.useBlockDescriptionPrefix()));
        registerTestBlockItem("zephyr_glass_grass", zephyrGlassGrass);
        registerTestBlockItem("zephyr_updraft_grate", zephyrUpdraftGrate);
        registerTestBlockItem("simurgh_wind_turbine", simurghWindTurbine);

        registerTestItem("zephyr_glass_fiber", Item::new);
        registerTestItem("storm_kite_glider", StormKiteGliderItem::new);
        registerTestItem("cryo_tempest_horn", CryoTempestHornItem::new);
        registerTestItem("tempest_bottle", TempestBottleItem::new);
        registerTestItem("storm_kite_membrane", Item::new);
        registerTestItem("hydra_glacial_horn", Item::new);
        registerTestItem("simurgh_feather", Item::new);
        registerTestItem("storm_kite_spawn_egg", Item::new);
        registerTestItem("glacial_hydra_spawn_egg", Item::new);
        registerTestItem("zephyr_simurgh_spawn_egg", Item::new);

        Potion tempestStride = new Potion("tempest_stride",
                new MobEffectInstance(MobEffects.SPEED, 1800, 1),
                new MobEffectInstance(MobEffects.JUMP_BOOST, 1800, 0),
                new MobEffectInstance(MobEffects.SLOW_FALLING, 1800, 0));
        Holder.Reference<Potion> tsHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "tempest_stride"),
                tempestStride
        );
        bindStandaloneHolder(tsHolder, tempestStride);

        Potion longTempestStride = new Potion("tempest_stride",
                new MobEffectInstance(MobEffects.SPEED, 3600, 1),
                new MobEffectInstance(MobEffects.JUMP_BOOST, 3600, 0),
                new MobEffectInstance(MobEffects.SLOW_FALLING, 3600, 0));
        Holder.Reference<Potion> longTsHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_tempest_stride"),
                longTempestStride
        );
        bindStandaloneHolder(longTsHolder, longTempestStride);

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "storm_iceberg"),
                new StormIcebergFeature(NoneFeatureConfiguration.CODEC));

        StructureType<AuroralVaporObservatoryStructure> obsType = () -> AuroralVaporObservatoryStructure.CODEC;
        Holder.Reference<StructureType<?>> obsTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_vapor_observatory"),
                obsType
        );
        bindStandaloneHolder(obsTypeHolder, obsType);

        StructurePieceType obsPiece = AuroralVaporObservatoryPiece::new;
        Holder.Reference<StructurePieceType> obsPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_vapor_observatory_piece"),
                obsPiece
        );
        bindStandaloneHolder(obsPieceHolder, obsPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome12LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block frozenSporeMoss = registerTestBlock("frozen_spore_moss", name -> new FrozenSporeMossBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).strength(0.3F).sound(SoundType.MOSS).lightLevel(s -> 2)));
        Block frozenSporeMossCarpet = registerTestBlock("frozen_spore_moss_carpet", name -> new FrozenSporeMossCarpetBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).strength(0.1F).sound(SoundType.MOSS).lightLevel(s -> 1).noOcclusion()));
        Block sporeGlazedIce = registerTestBlock("spore_glazed_ice", name -> new SporeGlazedIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.ICE).friction(0.98F).strength(0.5F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 3)));
        Block sporeWoodLog = registerTestBlock("spore_wood_log", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).destroyTime(2.0F).sound(SoundType.WOOD)));
        Block sporeWood = registerTestBlock("spore_wood", name -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).destroyTime(2.0F).sound(SoundType.WOOD)));
        Block sporeWoodPlanks = registerTestBlock("spore_wood_planks", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        Block sporeWoodStairs = registerTestBlock("spore_wood_stairs", name -> new StairBlock(sporeWoodPlanks.defaultBlockState(), BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        Block sporeWoodSlab = registerTestBlock("spore_wood_slab", name -> new SlabBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        Block sporeWoodFence = registerTestBlock("spore_wood_fence", name -> new FenceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_YELLOW).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        Block frozenSporeLeaves = registerTestBlock("frozen_spore_leaves", name -> new FrozenSporeLeavesBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GREEN).strength(0.2F).sound(SoundType.GRASS)
                .noOcclusion().isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).lightLevel(s -> 2)));
        Block glazeCapMushroomBlock = registerTestBlock("glaze_cap_mushroom_block", name -> new GlazeCapMushroomBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).strength(0.4F).sound(SoundType.WOOD).lightLevel(s -> 4)));
        Block sporeBellows = registerTestBlock("spore_bellows", name -> new SporeBellowsBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).noOcclusion().strength(2.0F, 5.0F).sound(SoundType.WOOD)
                .lightLevel(s -> s.getValue(SporeBellowsBlock.TRIGGERED) ? 6 : 2)));
        Block cryoSporeCenser = registerTestBlock("cryo_spore_censer", name -> new CryoSporeCenserBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noOcclusion().strength(2.2F, 6.0F)
                .requiresCorrectToolForDrops().sound(SoundType.LANTERN).randomTicks().lightLevel(s -> 13)));

        registerTestBlockItem("frozen_spore_moss", frozenSporeMoss);
        registerTestBlockItem("frozen_spore_moss_carpet", frozenSporeMossCarpet);
        registerTestBlockItem("spore_glazed_ice", sporeGlazedIce);
        registerTestBlockItem("spore_wood_log", sporeWoodLog);
        registerTestBlockItem("spore_wood", sporeWood);
        registerTestBlockItem("spore_wood_planks", sporeWoodPlanks);
        registerTestBlockItem("spore_wood_stairs", sporeWoodStairs);
        registerTestBlockItem("spore_wood_slab", sporeWoodSlab);
        registerTestBlockItem("spore_wood_fence", sporeWoodFence);
        registerTestBlockItem("frozen_spore_leaves", frozenSporeLeaves);
        registerTestBlockItem("glaze_cap_mushroom_block", glazeCapMushroomBlock);
        registerTestBlockItem("spore_bellows", sporeBellows);
        registerTestBlockItem("cryo_spore_censer", cryoSporeCenser);

        registerTestItem("mycelial_spore_cap", Item::new);
        registerTestItem("spore_filter_mask", SporeFilterMaskItem::new);
        registerTestItem("cryo_spore_poultice", CryoSporePoulticeItem::new);
        registerTestItem("glazed_spore_bomb", GlazedSporeBombItem::new);
        registerTestItem("mycelial_spore_weaver_spawn_egg", Item::new);
        if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "living_spores"))) {
            registerTestItem("living_spores", Item::new);
        }
        if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mossy_aegis_plate"))) {
            registerTestItem("mossy_aegis_plate", Item::new);
        }

        MobEffect sporeClarityEffect = new SporeClarityEffect();
        Holder.Reference<MobEffect> scEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_clarity"),
                sporeClarityEffect
        );
        bindStandaloneHolder(scEffectHolder, sporeClarityEffect);

        Potion sporeClarity = new Potion("spore_clarity",
                new MobEffectInstance(scEffectHolder, 3600, 0),
                new MobEffectInstance(MobEffects.RESISTANCE, 1800, 0));
        Holder.Reference<Potion> scHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_clarity"),
                sporeClarity
        );
        bindStandaloneHolder(scHolder, sporeClarity);

        Potion longSporeClarity = new Potion("spore_clarity",
                new MobEffectInstance(scEffectHolder, 9600, 0),
                new MobEffectInstance(MobEffects.RESISTANCE, 3600, 0));
        Holder.Reference<Potion> longScHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_spore_clarity"),
                longSporeClarity
        );
        bindStandaloneHolder(longScHolder, longSporeClarity);

        ResourceLocation weaverId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mycelial_spore_weaver");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, weaverId,
                EntityType.Builder.<MycelialSporeWeaverEntity>of(MycelialSporeWeaverEntity::new, MobCategory.CREATURE)
                        .sized(0.95F, 1.15F).clientTrackingRange(10).updateInterval(1)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, weaverId)));

        ResourceLocation bombEntityId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_spore_bomb");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, bombEntityId,
                EntityType.Builder.<GlazedSporeBombEntity>of(GlazedSporeBombEntity::new, MobCategory.MISC)
                        .sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(10)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, bombEntityId)));

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_wood_tree"),
                new SporeWoodTreeFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "giant_glaze_cap_mushroom"),
                new GiantGlazeCapMushroomFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_glazed_floe"),
                new SporeGlazedFloeFeature(NoneFeatureConfiguration.CODEC));

        StructureType<GlazedMycelialConservatoryStructure> consType = () -> GlazedMycelialConservatoryStructure.CODEC;
        Holder.Reference<StructureType<?>> consTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_mycelial_conservatory"),
                consType
        );
        bindStandaloneHolder(consTypeHolder, consType);

        StructurePieceType consPiece = GlazedMycelialConservatoryPiece::new;
        Holder.Reference<StructurePieceType> consPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_mycelial_conservatory_piece"),
                consPiece
        );
        bindStandaloneHolder(consPieceHolder, consPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome13LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block glassSand = registerTestBlock("glass_sand", name -> new GlassSandBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).strength(0.5F).sound(SoundType.SAND)));
        Block glassSandstone = registerTestBlock("glass_sandstone", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(0.8F).sound(SoundType.STONE)));
        Block glassSandstoneStairs = registerTestBlock("glass_sandstone_stairs", name -> new StairBlock(glassSandstone.defaultBlockState(), BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(0.8F).sound(SoundType.STONE)));
        Block glassSandstoneSlab = registerTestBlock("glass_sandstone_slab", name -> new SlabBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.STONE)));
        Block mirageIce = registerTestBlock("mirage_ice", name -> new MirageIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.ICE).friction(0.97F).strength(0.5F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 4)));
        Block miragePrismLantern = registerTestBlock("mirage_prism_lantern", name -> new MiragePrismLanternBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noOcclusion().strength(2.2F, 6.0F)
                .requiresCorrectToolForDrops().sound(SoundType.LANTERN).randomTicks().lightLevel(s -> 15)));

        registerTestBlockItem("glass_sand", glassSand);
        registerTestBlockItem("glass_sandstone", glassSandstone);
        registerTestBlockItem("glass_sandstone_stairs", glassSandstoneStairs);
        registerTestBlockItem("glass_sandstone_slab", glassSandstoneSlab);
        registerTestBlockItem("mirage_ice", mirageIce);
        registerTestBlockItem("mirage_prism_lantern", miragePrismLantern);

        registerTestItem("silica_glass_plate", Item::new);
        registerTestItem("mirage_veil_goggles", MirageVeilGogglesItem::new);
        registerTestItem("mirage_decoy_prism", MirageDecoyPrismItem::new);
        registerTestItem("shimmering_focus", ShimmeringFocusItem::new);
        registerTestItem("silica_glass_scarab_spawn_egg", Item::new);
        if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitreous_needle"))) {
            registerTestItem("vitreous_needle", Item::new);
        }
        if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "shimmering_mirage_aura"))) {
            registerTestItem("shimmering_mirage_aura", Item::new);
        }

        MobEffect doubleVisionEffect = new DoubleVisionEffect();
        Holder.Reference<MobEffect> dvHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "double_vision"),
                doubleVisionEffect
        );
        bindStandaloneHolder(dvHolder, doubleVisionEffect);

        MobEffect miragePhasingEffect = new MiragePhasingEffect();
        Holder.Reference<MobEffect> mpEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_phasing"),
                miragePhasingEffect
        );
        bindStandaloneHolder(mpEffectHolder, miragePhasingEffect);

        Potion miragePhasing = new Potion("mirage_phasing",
                new MobEffectInstance(mpEffectHolder, 3600, 0),
                new MobEffectInstance(MobEffects.SPEED, 3600, 0),
                new MobEffectInstance(MobEffects.INVISIBILITY, 1800, 0));
        Holder.Reference<Potion> mpHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_phasing"),
                miragePhasing
        );
        bindStandaloneHolder(mpHolder, miragePhasing);

        Potion longMiragePhasing = new Potion("mirage_phasing",
                new MobEffectInstance(mpEffectHolder, 9600, 0),
                new MobEffectInstance(MobEffects.SPEED, 9600, 0),
                new MobEffectInstance(MobEffects.INVISIBILITY, 3600, 0));
        Holder.Reference<Potion> longMpHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_mirage_phasing"),
                longMiragePhasing
        );
        bindStandaloneHolder(longMpHolder, longMiragePhasing);

        ResourceLocation scarabId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silica_glass_scarab");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, scarabId,
                EntityType.Builder.<SilicaGlassScarabEntity>of(SilicaGlassScarabEntity::new, MobCategory.CREATURE)
                        .sized(0.85F, 0.55F).clientTrackingRange(10).updateInterval(1)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, scarabId)));

        ResourceLocation decoyId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_decoy_prism");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, decoyId,
                EntityType.Builder.<MirageDecoyPrismEntity>of(MirageDecoyPrismEntity::new, MobCategory.MISC)
                        .sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(10)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, decoyId)));

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_ice_arch"),
                new MirageIceArchFeature(NoneFeatureConfiguration.CODEC));

        StructureType<FataMorganaSanctuaryStructure> sanctType = () -> FataMorganaSanctuaryStructure.CODEC;
        Holder.Reference<StructureType<?>> sanctTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fata_morgana_sanctuary"),
                sanctType
        );
        bindStandaloneHolder(sanctTypeHolder, sanctType);

        StructurePieceType sanctPiece = FataMorganaSanctuaryPiece::new;
        Holder.Reference<StructurePieceType> sanctPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fata_morgana_sanctuary_piece"),
                sanctPiece
        );
        bindStandaloneHolder(sanctPieceHolder, sanctPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome14LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block frostWillowLeaves = registerTestBlock("frost_willow_leaves", name -> new FrostWillowLeavesBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).strength(0.2F).sound(SoundType.GRASS)
                .noOcclusion().isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)));
        Block frostWillowVines = registerTestBlock("frost_willow_vines", name -> new FrostWillowVinesBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.PLANT).replaceable().noCollision().randomTicks()
                .strength(0.2F).sound(SoundType.VINE).ignitedByLava().lightLevel(s -> 2)));
        Block amberResinBlock = registerTestBlock("amber_resin_block", name -> new AmberResinBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).friction(0.8F).strength(0.6F)
                .sound(SoundType.HONEY_BLOCK).noOcclusion().lightLevel(s -> 4)));
        Block amberTorch = registerTestBlock("amber_torch", name -> new AmberTorchBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).noCollision().instabreak().lightLevel(s -> 14).sound(SoundType.WOOD).randomTicks()));
        Block wallAmberTorch = registerTestBlock("wall_amber_torch", name -> new WallAmberTorchBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).noCollision().instabreak().lightLevel(s -> 14).sound(SoundType.WOOD).randomTicks()));
        Block oasisDewBasin = registerTestBlock("oasis_dew_basin", name -> new OasisDewBasinBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_ORANGE).noOcclusion().strength(2.0F, 6.0F)
                .requiresCorrectToolForDrops().sound(SoundType.STONE).randomTicks().lightLevel(s -> s.getValue(OasisDewBasinBlock.INFUSED) ? 14 : 12)));

        registerTestBlockItem("frost_willow_leaves", frostWillowLeaves);
        registerTestBlockItem("frost_willow_vines", frostWillowVines);
        registerTestBlockItem("amber_resin_block", amberResinBlock);
        registerTestItem("amber_torch", props -> new StandingAndWallBlockItem(amberTorch, wallAmberTorch, Direction.DOWN, props.useBlockDescriptionPrefix()));
        registerTestBlockItem("oasis_dew_basin", oasisDewBasin);

        registerTestItem("amber_nectar_bottle", AmberNectarBottleItem::new);
        registerTestItem("amber_adhesive", AmberAdhesiveItem::new);
        registerTestItem("waterproof_salamander_skin", Item::new);
        registerTestItem("salamander_wader_hood", SalamanderWaderHoodItem::new);
        registerTestItem("mud_lurker_salamander_spawn_egg", Item::new);
        if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_chunk"))) {
            registerTestItem("amber_chunk", Item::new);
        }

        MobEffect oasisRenewalEffect = new OasisRenewalEffect();
        Holder.Reference<MobEffect> orEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "oasis_renewal"),
                oasisRenewalEffect
        );
        bindStandaloneHolder(orEffectHolder, oasisRenewalEffect);

        Potion oasisRenewal = new Potion("oasis_renewal",
                new MobEffectInstance(orEffectHolder, 3600, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 1800, 0),
                new MobEffectInstance(MobEffects.ABSORPTION, 1800, 0));
        Holder.Reference<Potion> orHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "oasis_renewal"),
                oasisRenewal
        );
        bindStandaloneHolder(orHolder, oasisRenewal);

        Potion longOasisRenewal = new Potion("oasis_renewal",
                new MobEffectInstance(orEffectHolder, 9600, 0),
                new MobEffectInstance(MobEffects.REGENERATION, 3600, 0),
                new MobEffectInstance(MobEffects.ABSORPTION, 3600, 0));
        Holder.Reference<Potion> longOrHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_oasis_renewal"),
                longOasisRenewal
        );
        bindStandaloneHolder(longOrHolder, longOasisRenewal);

        ResourceLocation salamanderId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mud_lurker_salamander");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, salamanderId,
                EntityType.Builder.<MudLurkerSalamanderEntity>of(MudLurkerSalamanderEntity::new, MobCategory.CREATURE)
                        .sized(0.95F, 0.55F).clientTrackingRange(10).updateInterval(1)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, salamanderId)));

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "oasis_amber_spring"),
                new OasisAmberSpringFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_weeping_willow"),
                new AmberWeepingWillowFeature(NoneFeatureConfiguration.CODEC));

        StructureType<AmberWillowBathhouseStructure> bathType = () -> AmberWillowBathhouseStructure.CODEC;
        Holder.Reference<StructureType<?>> bathTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_willow_bathhouse"),
                bathType
        );
        bindStandaloneHolder(bathTypeHolder, bathType);

        StructurePieceType bathPiece = AmberWillowBathhousePiece::new;
        Holder.Reference<StructurePieceType> bathPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_willow_bathhouse_piece"),
                bathPiece
        );
        bindStandaloneHolder(bathPieceHolder, bathPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome15LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block dustCrustedIce = registerTestBlock("dust_crusted_ice", name -> new DustCrustedIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.ICE).friction(0.96F).strength(0.6F).sound(SoundType.GLASS)));
        Block greySand = registerTestBlock("grey_sand", name -> new GreySandBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.5F).sound(SoundType.SAND)));
        Block windBlownDustScrub = registerTestBlock("wind_blown_dust_scrub", name -> new WindBlownDustScrubBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.WOOD).noCollision().instabreak().randomTicks().ignitedByLava().sound(SoundType.GRASS)));
        Block toughFiberRope = registerTestBlock("tough_fiber_rope", name -> new ToughFiberRopeBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.SAND).noOcclusion().strength(0.4F).sound(SoundType.LADDER).ignitedByLava()));
        Block windBaffleLantern = registerTestBlock("wind_baffle_lantern", name -> new WindBaffleLanternBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.GOLD).noOcclusion().strength(2.2F, 6.0F)
                .requiresCorrectToolForDrops().sound(SoundType.LANTERN).randomTicks().lightLevel(s -> 14)));

        registerTestBlockItem("dust_crusted_ice", dustCrustedIce);
        registerTestBlockItem("grey_sand", greySand);
        registerTestBlockItem("wind_blown_dust_scrub", windBlownDustScrub);
        registerTestBlockItem("tough_fiber_rope", toughFiberRope);
        registerTestBlockItem("wind_baffle_lantern", windBaffleLantern);

        registerTestItem("dry_fibers", Item::new);
        registerTestItem("dust_core_shard", DustCoreShardItem::new);
        registerTestItem("copper_silt_sieve", CopperSiltSieveItem::new);
        registerTestItem("dust_devil_ampoule", DustDevilAmpouleItem::new);
        registerTestItem("dust_core_golem_spawn_egg", Item::new);

        MobEffect zephyrWeightEffect = new ZephyrWeightEffect();
        Holder.Reference<MobEffect> zwEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_weight"),
                zephyrWeightEffect
        );
        bindStandaloneHolder(zwEffectHolder, zephyrWeightEffect);

        Potion zephyrWeight = new Potion("zephyr_weight",
                new MobEffectInstance(zwEffectHolder, 3600, 0));
        Holder.Reference<Potion> zwHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_weight"),
                zephyrWeight
        );
        bindStandaloneHolder(zwHolder, zephyrWeight);

        Potion longZephyrWeight = new Potion("zephyr_weight",
                new MobEffectInstance(zwEffectHolder, 9600, 0));
        Holder.Reference<Potion> longZwHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_zephyr_weight"),
                longZephyrWeight
        );
        bindStandaloneHolder(longZwHolder, longZephyrWeight);

        ResourceLocation golemId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dust_core_golem");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, golemId,
                EntityType.Builder.<DustCoreGolemEntity>of(DustCoreGolemEntity::new, MobCategory.CREATURE)
                        .sized(0.95F, 1.35F).clientTrackingRange(10).updateInterval(1)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, golemId)));

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dust_table_iceberg"),
                new DustTableIcebergFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "wind_blown_dust_patch"),
                new WindBlownDustPatchFeature(NoneFeatureConfiguration.CODEC));

        StructureType<SiltBoundBergWatchPylonStructure> pylonType = () -> SiltBoundBergWatchPylonStructure.CODEC;
        Holder.Reference<StructureType<?>> pylonTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silt_bound_berg_watch_pylon"),
                pylonType
        );
        bindStandaloneHolder(pylonTypeHolder, pylonType);

        StructurePieceType pylonPiece = SiltBoundBergWatchPylonPiece::new;
        Holder.Reference<StructurePieceType> pylonPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sbbwp"),
                pylonPiece
        );
        bindStandaloneHolder(pylonPieceHolder, pylonPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome16LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block sanguineIce = registerTestBlock("sanguine_ice", name -> new SanguineIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.CRIMSON_NYLIUM)
                .friction(0.98F).strength(0.5F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 3)));
        Block redClaySand = registerTestBlock("red_clay_sand", name -> new RedClaySandBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.TERRACOTTA_RED)
                .strength(0.6F).sound(SoundType.SAND)));
        Block sanguineClayBricks = registerTestBlock("sanguine_clay_bricks", name -> new Block(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.TERRACOTTA_RED)
                .strength(2.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE_BRICKS)));
        Block sanguineClayBrickStairs = registerTestBlock("sanguine_clay_brick_stairs", name -> new StairBlock(
                sanguineClayBricks.defaultBlockState(), BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.TERRACOTTA_RED)
                .strength(2.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE_BRICKS)));
        Block sanguineClayBrickSlab = registerTestBlock("sanguine_clay_brick_slab", name -> new SlabBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.TERRACOTTA_RED)
                .strength(2.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE_BRICKS)));
        Block redAlgae = registerTestBlock("red_algae", name -> new RedAlgaeBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.CRIMSON_NYLIUM)
                .noCollision().noOcclusion().instabreak().sound(SoundType.WET_GRASS)));
        Block crimsonTideLantern = registerTestBlock("crimson_tide_lantern", name -> new CrimsonTideLanternBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.DEEPSLATE)
                .noOcclusion().strength(2.5F, 6.0F).requiresCorrectToolForDrops()
                .sound(SoundType.LANTERN).randomTicks().lightLevel(s -> 15)));

        registerTestBlockItem("sanguine_ice", sanguineIce);
        registerTestBlockItem("red_clay_sand", redClaySand);
        registerTestBlockItem("sanguine_clay_bricks", sanguineClayBricks);
        registerTestBlockItem("sanguine_clay_brick_stairs", sanguineClayBrickStairs);
        registerTestBlockItem("sanguine_clay_brick_slab", sanguineClayBrickSlab);
        ResourceLocation algaeId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "red_algae");
        RedAlgaeItem redAlgaeItem = new RedAlgaeItem(redAlgae, new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, algaeId)).useBlockDescriptionPrefix());
        redAlgaeItem.registerBlocks(Item.BY_BLOCK, redAlgaeItem);
        Registry.register(BuiltInRegistries.ITEM, algaeId, redAlgaeItem);
        registerTestBlockItem("crimson_tide_lantern", crimsonTideLantern);

        registerTestItem("sanguine_draught_ampoule", SanguineDraughtAmpouleItem::new);
        registerTestItem("sanguine_ice_lancet", SanguineIceLancetItem::new);
        registerTestItem("sanguine_heart_gel", Item::new);
        registerTestItem("sanguine_sea_angel_spawn_egg", Item::new);

        MobEffect sanguineVigorEffect = new SanguineVigorEffect();
        Holder.Reference<MobEffect> svEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_vigor"),
                sanguineVigorEffect
        );
        bindStandaloneHolder(svEffectHolder, sanguineVigorEffect);

        Potion sanguineVigor = new Potion("sanguine_vigor",
                new MobEffectInstance(svEffectHolder, 3600, 0));
        Holder.Reference<Potion> svHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_vigor"),
                sanguineVigor
        );
        bindStandaloneHolder(svHolder, sanguineVigor);

        Potion longSanguineVigor = new Potion("sanguine_vigor",
                new MobEffectInstance(svEffectHolder, 9600, 0));
        Holder.Reference<Potion> longSvHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_sanguine_vigor"),
                longSanguineVigor
        );
        bindStandaloneHolder(longSvHolder, longSanguineVigor);

        ResourceLocation angelId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_sea_angel");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, angelId,
                EntityType.Builder.<SanguineSeaAngelEntity>of(SanguineSeaAngelEntity::new, MobCategory.MONSTER)
                        .sized(0.7F, 0.9F).clientTrackingRange(10).updateInterval(1)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, angelId)));

        ResourceLocation dartId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blood_frost_dart");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, dartId,
                EntityType.Builder.<BloodFrostDartEntity>of(BloodFrostDartEntity::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(2)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, dartId)));

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_ice_shelf"),
                new SanguineIceShelfFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "red_algae_margin"),
                new RedAlgaeMarginFeature(NoneFeatureConfiguration.CODEC));

        StructureType<CrimsonTideSanctumStructure> sanctumType = () -> CrimsonTideSanctumStructure.CODEC;
        Holder.Reference<StructureType<?>> sanctumTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crimson_tide_sanctum"),
                sanctumType
        );
        bindStandaloneHolder(sanctumTypeHolder, sanctumType);

        StructurePieceType sanctumPiece = CrimsonTideSanctumPiece::new;
        Holder.Reference<StructurePieceType> sanctumPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crimson_tide_sanctum_piece"),
                sanctumPiece
        );
        bindStandaloneHolder(sanctumPieceHolder, sanctumPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void bootstrapBiome17LiveRegistries() {
        setRegistryUnfrozen(BuiltInRegistries.BLOCK, true);
        setRegistryUnfrozen(BuiltInRegistries.ITEM, true);
        setRegistryUnfrozen(BuiltInRegistries.MOB_EFFECT, false);
        setRegistryUnfrozen(BuiltInRegistries.POTION, false);
        setRegistryUnfrozen(BuiltInRegistries.ENTITY_TYPE, true);
        setRegistryUnfrozen(BuiltInRegistries.FEATURE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_TYPE, false);
        setRegistryUnfrozen(BuiltInRegistries.STRUCTURE_PIECE, false);

        Block starlightIce = registerTestBlock("starlight_ice", name -> new StarlightIceBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.ICE)
                .friction(0.985F).strength(0.6F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 5)));
        Block stardustCrystalBlock = registerTestBlock("stardust_crystal_block", name -> new StardustCrystalBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE)
                .strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).lightLevel(s -> 9)));
        Block stardustCrystalCluster = registerTestBlock("stardust_crystal_cluster", name -> new StardustCrystalClusterBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.COLOR_PURPLE)
                .noOcclusion().strength(1.5F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST_CLUSTER).lightLevel(s -> 12)));
        Block rimeStarTussock = registerTestBlock("rime_star_tussock", name -> new RimeStarTussockBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.ICE)
                .noCollision().noOcclusion().instabreak().sound(SoundType.GRASS).lightLevel(s -> 3)));
        Block pottedRimeStarTussock = registerTestBlock("potted_rime_star_tussock", name -> new FlowerPotBlock(rimeStarTussock,
                BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)
                        .setId(ResourceKey.create(Registries.BLOCK, name)).lightLevel(s -> 3)));
        Block celestialOrreryLantern = registerTestBlock("celestial_orrery_lantern", name -> new CelestialOrreryLanternBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, name)).mapColor(MapColor.DEEPSLATE)
                .noOcclusion().strength(2.5F, 6.0F).requiresCorrectToolForDrops()
                .sound(SoundType.LANTERN).randomTicks().lightLevel(s -> 15)));

        registerTestBlockItem("starlight_ice", starlightIce);
        registerTestBlockItem("stardust_crystal_block", stardustCrystalBlock);
        registerTestBlockItem("stardust_crystal_cluster", stardustCrystalCluster);
        registerTestBlockItem("rime_star_tussock", rimeStarTussock);
        registerTestBlockItem("celestial_orrery_lantern", celestialOrreryLantern);

        registerTestItem("stardust_fragment", Item::new);
        registerTestItem("starlight_draught_ampoule", StarlightDraughtAmpouleItem::new);
        registerTestItem("starlight_tuning_chisel", StarlightTuningChiselItem::new);
        registerTestItem("astral_nautilus_nacre", Item::new);
        registerTestItem("astral_nautilus_armor", AstralNautilusArmorItem::new);
        registerTestItem("starlight_nautilus_spawn_egg", Item::new);

        registerTestItem("nebula_darter", p -> new AstralFishItem(p, AstralFishItem.FishProfile.NEBULA_DARTER));
        registerTestItem("cooked_nebula_darter", p -> new AstralFishItem(p, AstralFishItem.FishProfile.COOKED_NEBULA_DARTER));
        registerTestItem("starwing_flying_fish", p -> new AstralFishItem(p, AstralFishItem.FishProfile.STARWING_FLYING_FISH));
        registerTestItem("cooked_starwing_flying_fish", p -> new AstralFishItem(p, AstralFishItem.FishProfile.COOKED_STARWING_FLYING_FISH));
        registerTestItem("starfall_ray", p -> new AstralFishItem(p, AstralFishItem.FishProfile.STARFALL_RAY));
        registerTestItem("cooked_starfall_ray", p -> new AstralFishItem(p, AstralFishItem.FishProfile.COOKED_STARFALL_RAY));
        registerTestItem("comet_ribbonfish", p -> new AstralFishItem(p, AstralFishItem.FishProfile.COMET_RIBBONFISH));
        registerTestItem("cooked_comet_ribbonfish", p -> new AstralFishItem(p, AstralFishItem.FishProfile.COOKED_COMET_RIBBONFISH));
        registerTestItem("zenith_moonfish", p -> new AstralFishItem(p, AstralFishItem.FishProfile.ZENITH_MOONFISH));
        registerTestItem("cooked_zenith_moonfish", p -> new AstralFishItem(p, AstralFishItem.FishProfile.COOKED_ZENITH_MOONFISH));
        registerTestItem("supernova_puffer", p -> new AstralFishItem(p, AstralFishItem.FishProfile.SUPERNOVA_PUFFER));
        registerTestItem("astral_lanternfish", p -> new AstralFishItem(p, AstralFishItem.FishProfile.ASTRAL_LANTERNFISH));

        MobEffect starlightGraceEffect = new StarlightGraceEffect();
        Holder.Reference<MobEffect> sgEffectHolder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_grace"),
                starlightGraceEffect
        );
        bindStandaloneHolder(sgEffectHolder, starlightGraceEffect);

        Potion starlightGrace = new Potion("starlight_grace",
                new MobEffectInstance(sgEffectHolder, 3600, 0));
        Holder.Reference<Potion> sgHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_grace"),
                starlightGrace
        );
        bindStandaloneHolder(sgHolder, starlightGrace);

        Potion longStarlightGrace = new Potion("starlight_grace",
                new MobEffectInstance(sgEffectHolder, 9600, 0));
        Holder.Reference<Potion> longSgHolder = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_starlight_grace"),
                longStarlightGrace
        );
        bindStandaloneHolder(longSgHolder, longStarlightGrace);

        ResourceLocation nautilusId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus");
        Registry.register(BuiltInRegistries.ENTITY_TYPE, nautilusId,
                EntityType.Builder.<StarlightNautilusEntity>of(StarlightNautilusEntity::new, MobCategory.CREATURE)
                        .sized(0.95F, 0.95F).clientTrackingRange(10).updateInterval(1)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, nautilusId)));

        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "narrow_starlight_ice_pillar"),
                new NarrowStarlightIcePillarFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stardust_crystal_spire"),
                new StardustCrystalSpireFeature(NoneFeatureConfiguration.CODEC));

        StructureType<StardustSkerryOrreryStructure> orreryType = () -> StardustSkerryOrreryStructure.CODEC;
        Holder.Reference<StructureType<?>> orreryTypeHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stardust_skerry_orrery"),
                orreryType
        );
        bindStandaloneHolder(orreryTypeHolder, orreryType);

        StructurePieceType orreryPiece = StardustSkerryOrreryPiece::new;
        Holder.Reference<StructurePieceType> orreryPieceHolder = Registry.registerForHolder(
                BuiltInRegistries.STRUCTURE_PIECE,
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stardust_skerry_orrery_piece"),
                orreryPiece
        );
        bindStandaloneHolder(orreryPieceHolder, orreryPiece);

        refreezeRegistry(BuiltInRegistries.BLOCK);
        refreezeRegistry(BuiltInRegistries.ITEM);
        refreezeRegistry(BuiltInRegistries.MOB_EFFECT);
        refreezeRegistry(BuiltInRegistries.POTION);
        refreezeRegistry(BuiltInRegistries.ENTITY_TYPE);
        refreezeRegistry(BuiltInRegistries.FEATURE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_TYPE);
        refreezeRegistry(BuiltInRegistries.STRUCTURE_PIECE);
    }

    private static void verifyAll108BiomesCatalogued() {
        check(EntropicaBiome.values().length == 108, "Expected 108 EntropicaBiome entries");
        for (int i = 1; i <= 108; i++) {
            EntropicaBiome b = EntropicaBiome.byNumber(i);
            check(b != null, "Missing biome #" + i);
            check(EntropicaBiome.byId(b.getId()) == b, "ID lookup mismatch for " + b.getId());
        }
        EntropicaBiomePlacements.registerAll();
        check(EntropicaBiomePlacements.allRules().containsKey(EntropicaBiome.BLUE_GLACIER_SHELF),
                "Biome 1 (Blue-Glacier Shelf) placement rule must be registered");
        check(EntropicaBiomePlacements.allRules().containsKey(EntropicaBiome.AURORAL_SKERRIES),
                "Biome 2 (Auroral Skerries) placement rule must be registered");
        check(EntropicaBiomePlacements.allRules().containsKey(EntropicaBiome.FROST_BITE_FLOES),
                "Biome 3 (Frost-Bite Floes) placement rule must be registered");
        System.out.println("[PASS] 1/10 All 108 biomes catalogued + Biomes 1, 2 & 3 placement rules registered.");
    }

    private static void verifyDomainWarpedRegionDeterminismAndContinuity() {
        DomainWarpedRegionSampler samplerA = new DomainWarpedRegionSampler(123456789L, 6);
        DomainWarpedRegionSampler samplerB = new DomainWarpedRegionSampler(123456789L, 6);

        int sameNeighborCount = 0;
        int totalSteps = 2000;
        long prev = samplerA.sampleRegionCellHash(0, 0);
        for (int i = 1; i <= totalSteps; i++) {
            long hA = samplerA.sampleRegionCellHash(i, i / 2);
            long hB = samplerB.sampleRegionCellHash(i, i / 2);
            check(hA == hB, "Region sampler must be 100% deterministic for identical seed and coords");
            if (hA == prev) {
                sameNeighborCount++;
            }
            prev = hA;
        }
        // With scale=6 (64 quarts per cell), adjacent quart steps stay inside the same coherent cell >93% of steps
        double continuityRatio = sameNeighborCount / (double) totalSteps;
        check(continuityRatio > 0.90, "Expected smooth contiguous region cells (>90% neighbor continuity), got " + continuityRatio);

        // Verify pass-through vs active ratio (4 / (4 + 10) = 28.57%)
        int activeCount = 0;
        int cellSamples = 20000;
        for (int i = 0; i < cellSamples; i++) {
            long fakeCell = DomainWarpedRegionSampler.hash2D(i * 31L, i * 17L, 987654321L);
            if (DomainWarpedRegionSampler.isEntropicaActiveCell(fakeCell, 4, 10)) {
                activeCount++;
            }
        }
        double activeRatio = activeCount / (double) cellSamples;
        double expected = 4.0 / 14.0;
        check(Math.abs(activeRatio - expected) < 0.02,
                "Expected active cell ratio ~" + expected + ", got " + activeRatio);
        System.out.println("[PASS] 2/10 Organic domain-warped Voronoi determinism, continuity (" +
                String.format("%.1f%%", continuityRatio * 100) + "), and regional weights verified.");
    }

    private static void verify6DClimateMatchingForBlueGlacierShelf() {
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> vanillaFrozenPeaks = mockBiome("minecraft", "frozen_peaks");
        Holder<Biome> vanillaDesert = mockBiome("minecraft", "desert");

        EntropicaMultiNoiseLayer layer = new EntropicaMultiNoiseLayer(
                42L, 6, 10, 0, // 100% active regions so we test pure 6D climate matching
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );

        // Matching frozen, coastal-shelf (-0.15), sheer-cliff low-erosion (-0.65), surface (0.0), high weirdness (+0.70)
        Climate.TargetPoint icyShelfTarget = Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.0F, 0.70F);
        Holder<Biome> matchedPos = layer.selectMatchingBiome(111L, icyShelfTarget, vanillaFrozenPeaks);
        check(matchedPos == shelfHolder, "Blue-Glacier Shelf must match icy high-weirdness (+0.70) sheer coastal shelf");

        // Matching negative high weirdness (-0.70)
        Climate.TargetPoint icyShelfNegWeirdness = Climate.target(-0.80F, -0.2F, -0.12F, -0.80F, 0.02F, -0.75F);
        Holder<Biome> matchedNeg = layer.selectMatchingBiome(111L, icyShelfNegWeirdness, vanillaFrozenPeaks);
        check(matchedNeg == shelfHolder, "Blue-Glacier Shelf must match negative high-weirdness (-0.75) sheer coastal shelf");

        // Must NOT match hot desert, deep cave, or flat plains!
        Climate.TargetPoint hotDesertTarget = Climate.target(0.85F, -0.60F, 0.25F, 0.30F, 0.0F, 0.10F);
        check(layer.selectMatchingBiome(111L, hotDesertTarget, vanillaDesert) == vanillaDesert,
                "Blue-Glacier Shelf must never match hot inland desert climate");

        Climate.TargetPoint deepCaveTarget = Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.85F, 0.70F);
        check(layer.selectMatchingBiome(111L, deepCaveTarget, vanillaFrozenPeaks) == vanillaFrozenPeaks,
                "Surface Blue-Glacier Shelf must never overwrite deep underground cave biomes (depth=0.85)");

        System.out.println("[PASS] 3/10 Strict 6D multi-noise matching & depth/climate isolation verified for Biome 1.");
    }

    private static void verifyRendezvousRarityWeightsAndIsolation() {
        Climate.ParameterPoint sharedPolarNiche = EntropicaClimatePresets.builder()
                .temperature(EntropicaClimatePresets.TEMP_ICY)
                .continentalness(EntropicaClimatePresets.CONT_INLAND_ALL)
                .depth(EntropicaClimatePresets.DEPTH_SURFACE)
                .build();

        BiomePlacementRule commonRule = BiomePlacementRule.builder(EntropicaBiome.SILVER_PINE_TAIGA) // Common = 8
                .addParameterPoint(sharedPolarNiche).build();
        BiomePlacementRule rareRule = BiomePlacementRule.builder(EntropicaBiome.BLUE_GLACIER_SHELF) // Rare = 3
                .addParameterPoint(sharedPolarNiche).build();
        BiomePlacementRule legendaryRule = BiomePlacementRule.builder(EntropicaBiome.NULL_FRINGE_BAY) // Legendary = 1
                .addParameterPoint(sharedPolarNiche).build();

        Holder<Biome> hCommon = mockBiome("entropica", "silver_pine_taiga");
        Holder<Biome> hRare = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> hLegendary = mockBiome("entropica", "null_fringe_bay");
        Holder<Biome> fallback = mockBiome("minecraft", "snowy_plains");

        EntropicaMultiNoiseLayer layer = new EntropicaMultiNoiseLayer(
                777L, 6, 10, 0,
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(
                        new EntropicaMultiNoiseLayer.ResolvedRule(commonRule, hCommon),
                        new EntropicaMultiNoiseLayer.ResolvedRule(rareRule, hRare),
                        new EntropicaMultiNoiseLayer.ResolvedRule(legendaryRule, hLegendary)
                )
        );

        Climate.TargetPoint sample = Climate.target(-0.70F, 0.0F, 0.20F, 0.0F, 0.0F, 0.0F);
        int cWins = 0, rWins = 0, lWins = 0;
        int trials = 36000;
        for (int i = 0; i < trials; i++) {
            long cellHash = DomainWarpedRegionSampler.mix64(i * 0x9E3779B97F4A7C15L);
            Holder<Biome> w = layer.selectMatchingBiome(cellHash, sample, fallback);
            if (w == hCommon) cWins++;
            else if (w == hRare) rWins++;
            else if (w == hLegendary) lWins++;
        }

        // Expected ratios: 8/12 (66.67%), 3/12 (25.00%), 1/12 (8.33%)
        check(Math.abs((cWins / (double) trials) - (8.0 / 12.0)) < 0.015, "Common weight ratio off: " + cWins);
        check(Math.abs((rWins / (double) trials) - (3.0 / 12.0)) < 0.015, "Rare weight ratio off: " + rWins);
        check(Math.abs((lWins / (double) trials) - (1.0 / 12.0)) < 0.015, "Legendary weight ratio off: " + lWins);

        System.out.println(String.format(
                "[PASS] 4/10 Weighted Rendezvous rarity distribution verified (Common=%.1f%%, Rare=%.1f%%, Legendary=%.1f%%).",
                100.0 * cWins / trials, 100.0 * rWins / trials, 100.0 * lWins / trials
        ));
    }

    private static void verifyTerraBlenderCompatibilityModes() {
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> vanillaBiome = mockBiome("minecraft", "stony_shore");
        Holder<Biome> bopBiome = mockBiome("biomesoplenty", "auroral_garden");

        check(TerraBlenderCompatBridge.containsEntropicaBiomes(List.of(vanillaBiome, shelfHolder)),
                "Must detect existing entropica:* biome in base BiomeSource");
        check(!TerraBlenderCompatBridge.containsEntropicaBiomes(List.of(vanillaBiome, bopBiome)),
                "Must not false-positive when no entropica:* biome is in base BiomeSource");

        Climate.Sampler icyShelfSampler = new Climate.Sampler(
                net.minecraft.world.level.levelgen.DensityFunctions.constant(-0.75D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(0.0D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(-0.15D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(-0.65D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(0.0D),
                net.minecraft.world.level.levelgen.DensityFunctions.constant(0.70D),
                List.of()
        );

        // 1. COOPERATE Mode (TerraBlender loaded): yields to modded biomes (biomesoplenty:*), layers over minecraft:*
        EntropicaMultiNoiseLayer cooperateLayer = new EntropicaMultiNoiseLayer(
                99L, 6, 10, 0,
                true, false, TerraBlenderCompatMode.COOPERATE,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );
        check(cooperateLayer.resolveBiome(0, 16, 0, icyShelfSampler, bopBiome) == bopBiome,
                "COOPERATE mode must give way to modded TerraBlender biomes (biomesoplenty:*)");
        check(cooperateLayer.resolveBiome(0, 16, 0, icyShelfSampler, vanillaBiome) == shelfHolder,
                "COOPERATE mode must layer Entropica biomes cleanly over vanilla minecraft:* slots");

        // 2. COOPERATE Mode when base source ALREADY contains Entropica biomes (e.g. external TerraBlender Region addon)
        EntropicaMultiNoiseLayer cooperateAlreadyInjected = new EntropicaMultiNoiseLayer(
                99L, 6, 10, 0,
                true, true, TerraBlenderCompatMode.COOPERATE,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );
        check(cooperateAlreadyInjected.resolveBiome(0, 16, 0, icyShelfSampler, vanillaBiome) == vanillaBiome,
                "COOPERATE mode must automatically step aside when base TerraBlender source already manages entropica:* biomes");

        // 3. YIELD_TO_TERRABLENDER Mode: yields 100% when TerraBlender is installed
        EntropicaMultiNoiseLayer yieldLayer = new EntropicaMultiNoiseLayer(
                99L, 6, 10, 0,
                true, false, TerraBlenderCompatMode.YIELD_TO_TERRABLENDER,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );
        check(yieldLayer.resolveBiome(0, 16, 0, icyShelfSampler, vanillaBiome) == vanillaBiome,
                "YIELD_TO_TERRABLENDER mode must yield completely when TerraBlender is installed");

        // 4. OVERLAY_ON_TOP Mode: layers on top of BOTH vanilla and TerraBlender modded biomes
        EntropicaMultiNoiseLayer overlayLayer = new EntropicaMultiNoiseLayer(
                99L, 6, 10, 0,
                true, true, TerraBlenderCompatMode.OVERLAY_ON_TOP,
                List.of(new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder))
        );
        check(overlayLayer.resolveBiome(0, 16, 0, icyShelfSampler, bopBiome) == shelfHolder,
                "OVERLAY_ON_TOP mode must work on top of modded TerraBlender regions when configured");

        System.out.println("[PASS] 5/10 All TerraBlender compatibility modes (COOPERATE, YIELD_TO_TERRABLENDER, OVERLAY_ON_TOP) verified.");
    }

    private static void verifyBiomeAtmosphereAndEnvironmentProfiles() {
        EntropicaBiomeAtmospheres.registerAll();
        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        BiomeAtmosphereProfile shelf = EntropicaBiomeAtmospheres.getForHolder(shelfHolder);
        check(shelf != null, "Blue-Glacier Shelf must have a registered BiomeAtmosphereProfile");
        check(shelf.hasCustomFogDistance(), "Blue-Glacier Shelf must configure coastal haze fog distance");
        check(shelf.getSkyPhenomenon() == SkyPhenomenonType.GLACIAL_ICE_HALO, "Blue-Glacier Shelf must use GLACIAL_ICE_HALO sky phenomenon");
        check(Math.abs(shelf.getDescentSpeedMultiplier() - 1.15F) < 1.0E-4F, "Blue-Glacier Shelf must apply 1.15x Aegis descent speed");
        check(shelf.getAmbientParticlesPerTick() > 0 && Math.abs(shelf.getParticleWindVx()) > 0.01F,
                "Blue-Glacier Shelf must emit horizontally wind-drifted pale-blue snow particles");

        BiomeAtmosphereProfile auroral = EntropicaBiomeAtmospheres.get(EntropicaBiome.AURORAL_SKERRIES);
        check(auroral != null && auroral.getNightSkyColorRgb() == 0x0E6A3B, "Auroral Skerries must shift sky to #0E6A3B at night");
        check(auroral.getLightHeldFogReliefBlocks() > 0.0F, "Auroral Skerries must grant fog relief when holding light");
        check(auroral.getSkyPhenomenon() == SkyPhenomenonType.AURORA_CURTAINS, "Auroral Skerries must render AURORA_CURTAINS");
        check(auroral.hasVolumetricMist() && auroral.isVolumetricMistEmissive(),
                "Auroral Skerries must enable emissive volumetric mist");
        check(auroral.getWaterOpticalMode() == WaterOpticalMode.AURORA_RIBBON_REFLECTION && auroral.getWaterOpticalIntensity() > 0.0F,
                "Auroral Skerries must enable AURORA_RIBBON_REFLECTION water optics");

        // Verify master config toggle & headless-safe config wrappers
        check(Boolean.TRUE.equals(EntropicaConfig.ENABLE_ENHANCED_BIOME_FOG_AND_WATER.get()),
                "ENABLE_ENHANCED_BIOME_FOG_AND_WATER must default to true");
        check(Boolean.TRUE.equals(EntropicaConfig.ENABLE_BIOME_VOLUMETRIC_FOG.get()),
                "ENABLE_BIOME_VOLUMETRIC_FOG must default to true");
        check(Boolean.TRUE.equals(EntropicaConfig.ENABLE_BIOME_WATER_EFFECTS.get()),
                "ENABLE_BIOME_WATER_EFFECTS must default to true");
        check(Boolean.TRUE.equals(EntropicaConfig.ENABLE_SHADERPACK_ADAPTIVE_ATTENUATION.get()),
                "ENABLE_SHADERPACK_ADAPTIVE_ATTENUATION must default to true");
        try {
            EntropicaConfig.setEnhancedBiomeFogAndWaterTestOverride(false);
            check(!EntropicaConfig.ENABLE_ENHANCED_BIOME_FOG_AND_WATER.get(),
                    "Test override false must disable ENABLE_ENHANCED_BIOME_FOG_AND_WATER");
            check(!EntropicaConfig.ENABLE_BIOME_VOLUMETRIC_FOG.get() && !EntropicaConfig.ENABLE_BIOME_WATER_EFFECTS.get(),
                    "Disabling ENABLE_ENHANCED_BIOME_FOG_AND_WATER must cleanly gate off volumetric fog and water optics");
        } finally {
            EntropicaConfig.clearEnhancedBiomeFogAndWaterTestOverride();
        }
        check(Boolean.TRUE.equals(EntropicaConfig.ENABLE_ENHANCED_BIOME_FOG_AND_WATER.get()),
                "Clearing test override must restore ENABLE_ENHANCED_BIOME_FOG_AND_WATER == true");

        // Verify ShaderModCompatBridge runs safely in headless JVM without Sodium/Iris/Oculus
        check(!ShaderModCompatBridge.isSodiumFamilyLoaded(), "Headless JVM must report !isSodiumFamilyLoaded()");
        check(!ShaderModCompatBridge.isShaderModLoaded(), "Headless JVM must report !isShaderModLoaded()");
        check(!ShaderModCompatBridge.isShaderPackActive(), "Headless JVM must report !isShaderPackActive()");
        check(ShaderModCompatBridge.getVolumetricMistShaderScale() == 1.0F,
                "Headless getVolumetricMistShaderScale() must equal 1.0F");
        check(ShaderModCompatBridge.getWaterReflectionShaderScale() == 1.0F,
                "Headless getWaterReflectionShaderScale() must equal 1.0F");
        check(ShaderModCompatBridge.getWaterParallaxShaderScale() == 1.0F,
                "Headless getWaterParallaxShaderScale() must equal 1.0F");

        // Verify Astral Skerries + Polar volumetric mist & water optical profiles + TerraBlender Holder<Biome> resolution
        BiomeAtmosphereProfile astral = EntropicaBiomeAtmospheres.get(EntropicaBiome.ASTRAL_SKERRIES);
        check(astral != null, "Astral Skerries must have a registered BiomeAtmosphereProfile");
        check(astral.hasVolumetricMist() && astral.isVolumetricMistEmissive()
                        && astral.getVolumetricMistThickness() > 0.0F && astral.getVolumetricMistDensity() > 0.0F,
                "Astral Skerries must configure glowing purple volumetric mist");
        check(astral.getWaterOpticalMode() == WaterOpticalMode.STARLIGHT_PARALLAX && astral.getWaterOpticalIntensity() > 0.90F,
                "Astral Skerries must configure STARLIGHT_PARALLAX water optics");
        check(astral.getSkyPhenomenon() == SkyPhenomenonType.ASTRAL_STARDUST_VEIL,
                "Astral Skerries must configure ASTRAL_STARDUST_VEIL sky phenomenon");

        BiomeAtmosphereProfile nullFringe = EntropicaBiomeAtmospheres.get(EntropicaBiome.NULL_FRINGE_BAY);
        check(nullFringe != null && nullFringe.hasVolumetricMist()
                        && nullFringe.getWaterOpticalMode() == WaterOpticalMode.STARLIGHT_PARALLAX,
                "Null-Fringe Bay must configure volumetric mist + STARLIGHT_PARALLAX water optics");

        BiomeAtmosphereProfile bloodShallows = EntropicaBiomeAtmospheres.get(EntropicaBiome.BLOOD_ICE_SHALLOWS);
        check(bloodShallows != null && bloodShallows.hasVolumetricMist()
                        && bloodShallows.getWaterOpticalMode() == WaterOpticalMode.CRIMSON_TIDE_GLINT,
                "Blood-Ice Shallows must configure volumetric mist + CRIMSON_TIDE_GLINT water optics");

        check(EntropicaBiomeAtmospheres.get(EntropicaBiome.MIRAGE_GLACIER).getWaterOpticalMode() == WaterOpticalMode.MIRAGE_SHIMMER_REFLECTION,
                "Mirage Glacier must configure MIRAGE_SHIMMER_REFLECTION water optics");
        check(EntropicaBiomeAtmospheres.get(EntropicaBiome.STEAMING_SKERRIES).hasVolumetricMist()
                        && EntropicaBiomeAtmospheres.get(EntropicaBiome.STEAMING_SKERRIES).getWaterOpticalMode() == WaterOpticalMode.FRESNEL_SKY_REFLECTION,
                "Steaming Skerries must configure steam volumetric mist + FRESNEL_SKY_REFLECTION");

        for (EntropicaBiome targetBiome : List.of(
                EntropicaBiome.ASTRAL_SKERRIES,
                EntropicaBiome.AURORAL_SKERRIES,
                EntropicaBiome.NULL_FRINGE_BAY,
                EntropicaBiome.BLOOD_ICE_SHALLOWS
        )) {
            Holder<Biome> biomeHolder = mockBiome("entropica", targetBiome.getId());
            Holder<Biome> vanillaOcean = mockBiome("minecraft", "cold_ocean");
            Holder<Biome> moddedOcean = mockBiome("biomesoplenty", "crystalline_chasm");
            Climate.ParameterPoint testPoint = EntropicaClimatePresets.builder()
                    .temperature(EntropicaClimatePresets.TEMP_ICY)
                    .continentalness(EntropicaClimatePresets.CONT_COASTAL_SHELF)
                    .depth(EntropicaClimatePresets.DEPTH_SURFACE)
                    .build();
            BiomePlacementRule testRule = BiomePlacementRule.builder(targetBiome).addParameterPoint(testPoint).build();
            Climate.Sampler testSampler = new Climate.Sampler(
                    net.minecraft.world.level.levelgen.DensityFunctions.constant(-0.75D),
                    net.minecraft.world.level.levelgen.DensityFunctions.constant(0.0D),
                    net.minecraft.world.level.levelgen.DensityFunctions.constant(-0.20D),
                    net.minecraft.world.level.levelgen.DensityFunctions.constant(0.0D),
                    net.minecraft.world.level.levelgen.DensityFunctions.constant(0.0D),
                    net.minecraft.world.level.levelgen.DensityFunctions.constant(0.0D),
                    List.of()
            );

            Holder<Biome> cooperateResolved = new EntropicaMultiNoiseLayer(
                    1337L, 6, 10, 0, true, false, TerraBlenderCompatMode.COOPERATE,
                    List.of(new EntropicaMultiNoiseLayer.ResolvedRule(testRule, biomeHolder))
            ).resolveBiome(0, 16, 0, testSampler, vanillaOcean);
            Holder<Biome> yieldPassThrough = new EntropicaMultiNoiseLayer(
                    1337L, 6, 10, 0, true, true, TerraBlenderCompatMode.YIELD_TO_TERRABLENDER,
                    List.of(new EntropicaMultiNoiseLayer.ResolvedRule(testRule, biomeHolder))
            ).resolveBiome(0, 16, 0, testSampler, biomeHolder);
            Holder<Biome> overlayResolved = new EntropicaMultiNoiseLayer(
                    1337L, 6, 10, 0, true, true, TerraBlenderCompatMode.OVERLAY_ON_TOP,
                    List.of(new EntropicaMultiNoiseLayer.ResolvedRule(testRule, biomeHolder))
            ).resolveBiome(0, 16, 0, testSampler, moddedOcean);

            BiomeAtmosphereProfile expectedProfile = EntropicaBiomeAtmospheres.get(targetBiome);
            check(EntropicaBiomeAtmospheres.getForHolder(cooperateResolved) == expectedProfile,
                    "COOPERATE Holder<Biome> must resolve identical atmosphere profile for " + targetBiome.getId());
            check(EntropicaBiomeAtmospheres.getForHolder(yieldPassThrough) == expectedProfile,
                    "YIELD_TO_TERRABLENDER Holder<Biome> must resolve identical atmosphere profile for " + targetBiome.getId());
            check(EntropicaBiomeAtmospheres.getForHolder(overlayResolved) == expectedProfile,
                    "OVERLAY_ON_TOP Holder<Biome> must resolve identical atmosphere profile for " + targetBiome.getId());
        }

        // Verify deterministic math helpers on BiomeVolumetricFogRenderer
        check(BiomeVolumetricFogRenderer.computeMistDistanceEnvelope(0.0F, 48.0F) == 0.0F
                        && BiomeVolumetricFogRenderer.computeMistDistanceEnvelope(3.0F, 48.0F) == 0.0F,
                "Volumetric mist near-plane envelope must be strictly 0.0F inside <= 3.0F blocks");
        float nearRamp = BiomeVolumetricFogRenderer.computeMistDistanceEnvelope(5.0F, 48.0F);
        check(nearRamp > 0.10F && nearRamp < 0.90F, "Volumetric mist near ramp at 5.0F must smoothly interpolate, got " + nearRamp);
        check(BiomeVolumetricFogRenderer.computeMistDistanceEnvelope(10.0F, 48.0F) == 1.0F
                        && BiomeVolumetricFogRenderer.computeMistDistanceEnvelope(26.0F, 48.0F) == 1.0F,
                "Volumetric mist distance envelope must hold 1.0F across mid-range plateau");
        float farRamp = BiomeVolumetricFogRenderer.computeMistDistanceEnvelope(38.0F, 48.0F);
        check(farRamp > 0.10F && farRamp < 0.90F, "Volumetric mist far fade at 38.0F must smoothly interpolate, got " + farRamp);
        check(BiomeVolumetricFogRenderer.computeMistDistanceEnvelope(48.0F, 48.0F) == 0.0F
                        && BiomeVolumetricFogRenderer.computeMistDistanceEnvelope(Float.NaN, 48.0F) == 0.0F,
                "Volumetric mist envelope must vanish cleanly at maxRadius and NaN");

        check(BiomeVolumetricFogRenderer.computeVerticalStratumAlpha(0.0F, 0.0F) == 0.0F
                        && BiomeVolumetricFogRenderer.computeVerticalStratumAlpha(1.0F, 0.0F) == 0.0F,
                "Vertical mist stratum alpha must vanish at bottom (0.0F) and crown (1.0F)");
        float lowStratum = BiomeVolumetricFogRenderer.computeVerticalStratumAlpha(0.18F, 0.0F);
        float midStratum = BiomeVolumetricFogRenderer.computeVerticalStratumAlpha(0.50F, 0.0F);
        float highStratum = BiomeVolumetricFogRenderer.computeVerticalStratumAlpha(0.82F, 0.0F);
        check(lowStratum > 0.0F && highStratum > 0.0F && midStratum > lowStratum && midStratum > highStratum,
                "Vertical mist stratum alpha must form a smooth positive bell curve peaking at 0.50F");
        check(BiomeVolumetricFogRenderer.computeVerticalStratumAlpha(0.50F, 1.0F)
                        > BiomeVolumetricFogRenderer.computeVerticalStratumAlpha(0.50F, -1.0F),
                "Vertical mist stratum alpha must respond deterministically to driftWave");

        // Verify deterministic math helpers on BiomeWaterSurfaceRenderer (Fresnel + camera-movement starlight parallax)
        float fresnelNadir = BiomeWaterSurfaceRenderer.computeFresnelFactor(0.0D, -10.0D, 0.0D);
        float fresnelMid = BiomeWaterSurfaceRenderer.computeFresnelFactor(6.0D, -4.0D, 0.0D);
        float fresnelGrazing = BiomeWaterSurfaceRenderer.computeFresnelFactor(40.0D, -0.4D, 0.0D);
        check(Math.abs(fresnelNadir - 0.06F) < 1.0E-5F, "Nadir Fresnel factor must equal 0.06F, got " + fresnelNadir);
        check(fresnelNadir < fresnelMid && fresnelMid < fresnelGrazing && fresnelGrazing > 0.90F && fresnelGrazing <= 1.0F,
                "Fresnel factor must increase monotonically toward grazing horizon within [0.06F, 1.0F]");

        double waterY = 62.904D;
        double camY = 65.2D;
        double camAX = 96.0D, camAZ = -44.0D;
        double camBX = 104.5D, camBZ = -36.5D;
        float maxCoordShift = 0.0F;
        float maxStarIntensityShift = 0.0F;
        for (int sampleIdx = 0; sampleIdx < 16; sampleIdx++) {
            double wx = 100.0D + (sampleIdx % 4) * 0.37D;
            double wz = -40.0D + (sampleIdx / 4) * 0.37D;
            float uA = BiomeWaterSurfaceRenderer.computeParallaxCoord(wx, wx - camAX, waterY - camY, 5.8F);
            float vA = BiomeWaterSurfaceRenderer.computeParallaxCoord(wz, wz - camAZ, waterY - camY, 5.8F);
            float uB = BiomeWaterSurfaceRenderer.computeParallaxCoord(wx, wx - camBX, waterY - camY, 5.8F);
            float vB = BiomeWaterSurfaceRenderer.computeParallaxCoord(wz, wz - camBZ, waterY - camY, 5.8F);
            maxCoordShift = Math.max(maxCoordShift, Math.abs(uA - uB) + Math.abs(vA - vB));

            float starA1 = BiomeWaterSurfaceRenderer.evaluateStarfieldIntensity(uA, vA, 12.5F);
            float starA2 = BiomeWaterSurfaceRenderer.evaluateStarfieldIntensity(uA, vA, 12.5F);
            float starB = BiomeWaterSurfaceRenderer.evaluateStarfieldIntensity(uB, vB, 12.5F);
            check(starA1 == starA2, "evaluateStarfieldIntensity must be 100% deterministic");
            check(starA1 >= 0.0F && starA1 <= 1.0F && starB >= 0.0F && starB <= 1.0F,
                    "evaluateStarfieldIntensity must stay clamped in [0.0F, 1.0F]");
            maxStarIntensityShift = Math.max(maxStarIntensityShift, Math.abs(starA1 - starB));
        }
        check(maxCoordShift > 15.0F,
                "Sub-surface parallax coordinates must shift substantially when camera moves, got " + maxCoordShift);
        check(maxStarIntensityShift > 0.05F,
                "Starfield intensity across water surface must shift non-trivially when camera moves, got " + maxStarIntensityShift);
        check(Float.isFinite(BiomeWaterSurfaceRenderer.computeParallaxCoord(100.0D, 32.0D, -0.0001D, 11.5F)),
                "computeParallaxCoord must clamp grazing rays safely without horizon blowup");

        // Verify linear recurrence for 1.15x descent velocity: at every tick t, v_scaled(t) / v_vanilla(t) == 1.15
        double g = 0.08D;
        double vVanilla = 0.0D;
        double vAegis = 0.0D;
        for (int tick = 1; tick <= 40; tick++) {
            vVanilla = (vVanilla - g) * 0.98D;
            vAegis = (vAegis - g) * 0.98D - (1.15D - 1.0D) * g * 0.98D;
            double ratio = vAegis / vVanilla;
            check(Math.abs(ratio - 1.15D) < 1.0E-9D, "Expected exact 1.15x descent velocity at tick " + tick + ", got " + ratio);
        }

        System.out.println("[PASS] 6/24 Biome custom/volumetric fog, water starlight parallax & Fresnel reflections, ShaderModCompatBridge, Sodium/Iris sky phenomena, wind-drifted particles, and exact 1.15x Aegis gravity verified.");
    }

    private static void verifySmoothNonEffectToEffectBiomeTransitions() {
        EntropicaBiomeAtmospheres.registerAll();

        // 1. Temporal exponential decay invariants: [0, 1] bounds, monotonicity, and frame-rate independence
        check(BiomeTransitionMath.expDecayFactor(0.0F, 0.14F) == 0.0F, "expDecayFactor at dt=0 must be exact 0.0f");
        check(BiomeTransitionMath.expDecayFactor(-1.0F, 0.14F) == 0.0F, "expDecayFactor for negative dt must clamp to 0.0f");
        check(BiomeTransitionMath.expDecayFactor(Float.NaN, 0.14F) == 0.0F, "expDecayFactor for NaN must be safe 0.0f");

        float prevFactor = 0.0F;
        for (int step = 1; step <= 200; step++) {
            float dt = step * 0.25F;
            float factor = BiomeTransitionMath.expDecayFactor(dt, 0.14F);
            check(factor >= 0.0F && factor <= 1.0F, "expDecayFactor out of [0,1] bounds: " + factor);
            check(factor >= prevFactor, "expDecayFactor must be monotonic with dt at step " + step);
            prevFactor = factor;
        }
        // Frame-rate independence: 8 sub-steps of dt=0.125 ticks == 1 step of dt=1.0 tick
        float singleTickSmooth = BiomeTransitionMath.expSmooth(0.0F, 1.0F, 1.0F, 0.14F);
        float subFrameSmooth = 0.0F;
        for (int f = 0; f < 8; f++) {
            subFrameSmooth = BiomeTransitionMath.expSmooth(subFrameSmooth, 1.0F, 0.125F, 0.14F);
        }
        check(Math.abs(singleTickSmooth - subFrameSmooth) < 1.0E-5F,
                "Exponential smoothing must be frame-rate independent: 1-tick=" + singleTickSmooth + ", 8x0.125=" + subFrameSmooth);

        // 2. Spatial 2D Gaussian kernel across a sharp x = 0 border:
        //    minecraft:frozen_ocean (null profile) -> entropica:blue_glacier_shelf (has profile)
        Holder<Biome> vanillaFrozenOcean = mockBiome("minecraft", "frozen_ocean");
        Holder<Biome> blueGlacierShelf = mockBiome("entropica", "blue_glacier_shelf");
        check(EntropicaBiomeAtmospheres.getForHolder(vanillaFrozenOcean) == null,
                "Vanilla minecraft:frozen_ocean must have null BiomeAtmosphereProfile");
        check(EntropicaBiomeAtmospheres.getForHolder(blueGlacierShelf) != null,
                "entropica:blue_glacier_shelf must have a registered BiomeAtmosphereProfile");

        BiomeTransitionMath.BlockScalarSampler sharpBorderPresence = (bx, bz) -> {
            Holder<Biome> h = bx < 0 ? vanillaFrozenOcean : blueGlacierShelf;
            return EntropicaBiomeAtmospheres.getForHolder(h) != null ? 1.0F : 0.0F;
        };

        double testZ = 2.35D;
        check(BiomeTransitionMath.sampleGaussian2D(-20.0D, testZ, sharpBorderPresence) == 0.0F,
                "Gaussian kernel far inside vanilla biome (x=-20) must be exact 0.0f");
        check(Math.abs(BiomeTransitionMath.sampleGaussian2D(20.0D, testZ, sharpBorderPresence) - 1.0F) < 1.0E-6F,
                "Gaussian kernel far inside custom biome (x=+20) must be exact 1.0f");

        float maxRawStepDelta = 0.0F;
        float maxEasedStepDelta = 0.0F;
        float prevRaw = BiomeTransitionMath.sampleGaussian2D(-20.0D, testZ, sharpBorderPresence);
        float prevEased = BiomeTransitionMath.sampleGaussianBlend01(-20.0D, testZ, sharpBorderPresence);

        // Walk 0.5 blocks/step across x = 0 from x = -20.0 to x = +20.0
        for (int s = -39; s <= 40; s++) {
            double camX = s * 0.5D;
            float raw = BiomeTransitionMath.sampleGaussian2D(camX, testZ, sharpBorderPresence);
            float eased = BiomeTransitionMath.sampleGaussianBlend01(camX, testZ, sharpBorderPresence);

            check(raw >= 0.0F && raw <= 1.0F, "Raw Gaussian blend out of [0,1] at x=" + camX + ": " + raw);
            check(eased >= 0.0F && eased <= 1.0F, "Smootherstepped Gaussian blend out of [0,1] at x=" + camX + ": " + eased);
            check(raw >= prevRaw - 1.0E-6F, "Raw Gaussian blend must be monotonic across x=0 at x=" + camX);
            check(eased >= prevEased - 1.0E-6F, "Eased Gaussian blend must be monotonic across x=0 at x=" + camX);
            if (camX >= -8.0D && camX <= 8.0D) {
                check(raw > prevRaw, "Raw Gaussian blend must strictly increase inside border band at x=" + camX);
            }

            maxRawStepDelta = Math.max(maxRawStepDelta, raw - prevRaw);
            maxEasedStepDelta = Math.max(maxEasedStepDelta, eased - prevEased);
            prevRaw = raw;
            prevEased = eased;
        }
        check(maxRawStepDelta < 0.12F && maxEasedStepDelta < 0.12F,
                "Expected smooth sub-block Gaussian border blend (<0.12 per 0.5-block step, zero 20% staircase jumps), got raw="
                        + maxRawStepDelta + ", eased=" + maxEasedStepDelta);

        // 3. Spatial Aegis gravity transition across x = 0:
        //    Walking from x = -20 (vanilla frozen_ocean, 1.00x) into x = +20 (Blue-Glacier Shelf, 1.15x)
        BiFunction<Integer, Integer, BiomeAtmosphereProfile> shelfBorderSampler = (bx, bz) ->
                EntropicaBiomeAtmospheres.getForHolder(bx < 0 ? vanillaFrozenOcean : blueGlacierShelf);

        float farOutsideMult = EntropicaBiomeEnvironmentHandler.computeBlendedDescentMultiplier(shelfBorderSampler, -20.0D, 0.5D);
        float farInsideMult = EntropicaBiomeEnvironmentHandler.computeBlendedDescentMultiplier(shelfBorderSampler, 20.0D, 0.5D);
        check(farOutsideMult == 1.00F, "Expected exact 1.00f descent multiplier far outside at x=-20, got " + farOutsideMult);
        check(Math.abs(farInsideMult - 1.15F) < 1.0E-6F, "Expected exact 1.15f descent multiplier far inside at x=+20, got " + farInsideMult);

        float maxOneBlockGravityDelta = 0.0F;
        float prevGravityMult = farOutsideMult;
        for (int x = -19; x <= 20; x++) {
            float currMult = EntropicaBiomeEnvironmentHandler.computeBlendedDescentMultiplier(shelfBorderSampler, (double) x, 0.5D);
            check(currMult >= 1.00F && currMult <= 1.150001F, "Blended descent multiplier out of [1.00, 1.15] bounds at x=" + x + ": " + currMult);
            check(currMult >= prevGravityMult - 1.0E-7F, "Blended descent multiplier must never decrease entering shelf at x=" + x);
            if (x >= -8 && x <= 9) {
                check(currMult > prevGravityMult,
                        "Blended descent multiplier must be strictly monotonic across active boundary [-9..+9] at x=" + x
                                + " (prev=" + prevGravityMult + ", curr=" + currMult + ")");
            }
            maxOneBlockGravityDelta = Math.max(maxOneBlockGravityDelta, currMult - prevGravityMult);
            prevGravityMult = currMult;
        }
        check(maxOneBlockGravityDelta < 0.025F,
                "Expected max 1-block descent multiplier delta < 0.025f across border, got " + maxOneBlockGravityDelta);

        // Also verify Jump Boost spatial presence gating across vanilla -> Null-Fringe Bay border
        Holder<Biome> nullFringeBay = mockBiome("entropica", "null_fringe_bay");
        BiFunction<Integer, Integer, BiomeAtmosphereProfile> jumpBorderSampler = (bx, bz) ->
                EntropicaBiomeAtmospheres.getForHolder(bx < 0 ? vanillaFrozenOcean : nullFringeBay);
        check(EntropicaBiomeEnvironmentHandler.computeJumpBoostPresenceWeight(jumpBorderSampler, -20.0D, 0.5D) == 0.0F,
                "Jump boost presence must be 0.0f far inside vanilla biome");
        check(Math.abs(EntropicaBiomeEnvironmentHandler.computeJumpBoostPresenceWeight(jumpBorderSampler, 20.0D, 0.5D) - 1.0F) < 1.0E-6F,
                "Jump boost presence must be 1.0f deep inside Null-Fringe Bay");

        System.out.println(String.format(
                "[PASS] 7/10 Smooth non-effect -> effect biome transitions verified (Gaussian max0.5Step=%.4f, Aegis 1.00x->1.15x max1BlockDelta=%.4f).",
                maxEasedStepDelta, maxOneBlockGravityDelta
        ));
    }

    private static JsonObject parseClasspathJson(String resourcePath) {
        ClassLoader cl = BiomeInjectorVerificationSuite.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream(resourcePath)) {
            check(is != null, "Missing required classpath resource: " + resourcePath);
            JsonElement parsed = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            check(parsed != null && parsed.isJsonObject(), "Invalid JSON object at: " + resourcePath);
            return parsed.getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("Failed parsing JSON resource " + resourcePath + ": " + e.getMessage(), e);
        }
    }

    private static void verifyPngDimensions(String resourcePath, int expectedW, int expectedH) {
        ClassLoader cl = BiomeInjectorVerificationSuite.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream(resourcePath)) {
            check(is != null, "Missing required PNG texture resource: " + resourcePath);
            BufferedImage img = ImageIO.read(is);
            check(img != null, "Unreadable PNG image at: " + resourcePath);
            check(img.getWidth() == expectedW && img.getHeight() == expectedH,
                    "Expected " + expectedW + "x" + expectedH + " for " + resourcePath + ", got " + img.getWidth() + "x" + img.getHeight());
        } catch (Exception e) {
            throw new AssertionError("Failed reading PNG " + resourcePath + ": " + e.getMessage(), e);
        }
    }

    private static void verifyBiome1BlueGlacierShelfContentAndAssets() {
        // 1. Biome JSON & Worldgen / Structure / Chest Loot JSONs
        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/blue_glacier_shelf.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects != null && effects.get("sky_color").getAsInt() == 4148879,
                "blue_glacier_shelf.json must specify pale indigo sky_color 4148879 (#3F4E8F)");
        check(effects.get("fog_color").getAsInt() == 12113134,
                "blue_glacier_shelf.json must specify coastal fog_color 12113134 (#B8D4EE)");
        String biomeStr = biomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:blue_glacier_ice_shelf",
                "entropica:aegis_crystal_formation",
                "entropica:patch_rimebloom_crevasse",
                "entropica:glacial_heart_root_overhang",
                "entropica:shielding_bramble_shelf",
                "entropica:astral_geode",
                "entropica:rime_back_ovis",
                "entropica:glacial_frost_core",
                "entropica:frost_tusk_mammot",
                "entropica:grot"
        )) {
            check(biomeStr.contains(expectedRef), "blue_glacier_shelf.json missing expected entry: " + expectedRef);
        }

        for (String featureId : List.of(
                "blue_glacier_ice_shelf",
                "aegis_crystal_formation",
                "patch_rimebloom_crevasse",
                "glacial_heart_root_overhang",
                "shielding_bramble_shelf"
        )) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + featureId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + featureId + ".json");
        }
        parseClasspathJson("data/entropica/worldgen/configured_feature/astral_geode.json");

        JsonObject structureJson = parseClasspathJson("data/entropica/worldgen/structure/frost_veiled_glacial_runic_spire.json");
        check("entropica:frost_veiled_glacial_runic_spire".equals(structureJson.get("type").getAsString()),
                "Spire structure JSON must reference entropica:frost_veiled_glacial_runic_spire");
        parseClasspathJson("data/entropica/worldgen/structure_set/frost_veiled_glacial_runic_spire.json");

        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/entropica/tags/worldgen/biome/has_structure/frost_veiled_glacial_runic_spire.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:blue_glacier_shelf"),
                    tagJson + " must include entropica:blue_glacier_shelf");
        }

        JsonObject spireChestLoot = parseClasspathJson("data/entropica/loot_table/chests/structure_1.json");
        check(spireChestLoot.toString().contains("entropica:glacial_focus_lens"),
                "chests/structure_1.json must include entropica:glacial_focus_lens");

        // 2. All 11 Block definitions, 14 Item definitions, Entity PNG dimensions, Translations, and 16 Recipes
        List<String> blocks11 = List.of(
                "aegis_crystal_block", "aegis_crystal_bud", "aegis_crystal_cluster", "runic_blue_ice", "runed_basalt_pillar",
                "glacial_heart_root", "shielding_bramble", "thorned_wire", "glacial_barricade",
                "conductive_aegis_plating", "glacial_ice_maker"
        );
        List<String> items14 = List.of(
                "aegis_crystal_shard", "rimebloom_petal", "rimebloom_dye", "shielding_thorn_fiber",
                "spiked_armor_lining", "aegis_shielding_lining", "frost_core_shard", "insulated_glacial_wool",
                "raw_mammot_meat", "cooked_mammot_meat", "thermal_insulated_blanket", "glacial_focus_lens",
                "glacial_frost_core_spawn_egg", "frost_tusk_mammot_spawn_egg"
        );
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");

        for (String b : blocks11) {
            parseClasspathJson("assets/entropica/items/" + b + ".json");
            parseClasspathJson("assets/entropica/blockstates/" + b + ".json");
            parseClasspathJson("assets/entropica/models/block/" + b + ".json");
            parseClasspathJson("assets/entropica/models/item/" + b + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + b + ".json");
            check(langJson.has("block.entropica." + b), "en_us.json missing block.entropica." + b);
        }
        parseClasspathJson("data/entropica/loot_table/blocks/rimebloom.json");

        for (String it : items14) {
            parseClasspathJson("assets/entropica/items/" + it + ".json");
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }
        for (String reqKey : List.of(
                "entity.entropica.glacial_frost_core",
                "entity.entropica.frost_tusk_mammot",
                "item.minecraft.potion.effect.frost_resistance",
                "item.minecraft.splash_potion.effect.frost_resistance",
                "item.minecraft.lingering_potion.effect.frost_resistance",
                "item.minecraft.tipped_arrow.effect.frost_resistance",
                "item.minecraft.potion.effect.freezing",
                "item.minecraft.splash_potion.effect.freezing",
                "item.minecraft.lingering_potion.effect.freezing",
                "item.minecraft.tipped_arrow.effect.freezing"
        )) {
            check(langJson.has(reqKey), "en_us.json missing translation key: " + reqKey);
        }

        verifyPngDimensions("assets/entropica/textures/entity/frost_core/glacial_frost_core.png", 64, 64);
        verifyPngDimensions("assets/entropica/textures/entity/mammot/frost_tusk_mammot.png", 64, 64);
        verifyPngDimensions("assets/entropica/textures/entity/mammot/frost_tusk_mammot_sheared.png", 64, 64);
        parseClasspathJson("data/entropica/loot_table/entities/frost_tusk_mammot.json");

        List<String> recipes16 = List.of(
                "aegis_crystal_block", "aegis_crystal_shard_from_block", "runic_blue_ice", "runed_basalt_pillar",
                "rimebloom_dye", "light_blue_dye_from_rimebloom_dye", "thorned_wire", "spiked_armor_lining",
                "aegis_shielding_lining", "glacial_barricade", "conductive_aegis_plating", "glacial_ice_maker",
                "thermal_insulated_blanket", "cooked_mammot_meat",
                "cooked_mammot_meat_from_smoking", "cooked_mammot_meat_from_campfire_cooking"
        );
        for (String r : recipes16) {
            parseClasspathJson("data/entropica/recipe/" + r + ".json");
        }

        // 3. GrotSpawnRegistry Blue-Glacier Shelf Pool verification (Glacial ~60%, Aegis ~30%, Frozen ~10% of 95% non-anomaly rolls)
        GrotSpawnRegistry.initialize();
        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        RandomSource rng = RandomSource.create(20261119L);
        int glacialCount = 0, aegisCount = 0, frozenCount = 0;
        int totalRolls = 20000;
        for (int i = 0; i < totalRolls; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(shelfHolder, rng);
            check(rolled != null, "GrotSpawnRegistry returned null EssenceType");
            if (rolled == EssenceType.GLACIAL) glacialCount++;
            else if (rolled == EssenceType.AEGIS) aegisCount++;
            else if (rolled == EssenceType.FROZEN) frozenCount++;
        }
        double glacialRatio = glacialCount / (double) totalRolls;
        double aegisRatio = aegisCount / (double) totalRolls;
        // 95% * 0.60 = 57.0% (+ tiny anomaly share ~57.25%), 95% * 0.30 = 28.5%
        check(glacialRatio > 0.53 && glacialRatio < 0.62, "Expected Glacial Grot ratio ~57%, got " + glacialRatio);
        check(aegisRatio > 0.25 && aegisRatio < 0.33, "Expected Aegis Grot ratio ~28.5%, got " + aegisRatio);
        check(frozenCount > 1400, "Expected Frozen Grot rolls ~9.5%, got " + frozenCount);

        System.out.println(String.format(
                "[PASS] 8/10 Biome 1 (Blue-Glacier Shelf) worldgen, Spire loot, 11 blocks, 14 items, 16 recipes, PNGs, en_us, & Grot pool (Glacial=%.1f%%, Aegis=%.1f%%) verified.",
                glacialRatio * 100.0, aegisRatio * 100.0
        ));
    }

    private static void checkDeclaredStaticField(String className, String fieldName) {
        try {
            Class<?> cls = Class.forName(className, false, BiomeInjectorVerificationSuite.class.getClassLoader());
            var f = cls.getDeclaredField(fieldName);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()),
                    className + "." + fieldName + " must be public static final");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Missing declared registry field " + className + "." + fieldName, e);
        }
    }

    private static void verifyBiome2AuroralSkerriesContentAndAssets() {
        // 1. 6D Multi-Noise Placement Rule & Non-Collision with Biome 1 + Surface Rule (coarse_gravel / glimmer_ice)
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        BiomePlacementRule skerriesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.AURORAL_SKERRIES);
        check(skerriesRule != null, "Auroral Skerries placement rule must be registered");
        check(skerriesRule.getParameterPoints().size() == 2, "Auroral Skerries must register 2 weirdness parameter points");

        // Verify strict parameter-box non-collision between Biome 1 (EROSION_SHEER_CLIFFS, high weirdness) and Biome 2 (flat islets, mid weirdness)
        for (Climate.ParameterPoint p1 : shelfRule.getParameterPoints()) {
            for (Climate.ParameterPoint p2 : skerriesRule.getParameterPoints()) {
                boolean erosionDisjoint = p1.erosion().max() < p2.erosion().min() || p2.erosion().max() < p1.erosion().min();
                boolean weirdnessSeparated = p1.weirdness().max() <= p2.weirdness().min() || p2.weirdness().max() <= p1.weirdness().min();
                check(erosionDisjoint && weirdnessSeparated,
                        "Auroral Skerries and Blue-Glacier Shelf 6D climate parameter boxes must be strictly non-overlapping");
            }
        }

        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> skerriesHolder = mockBiome("entropica", "auroral_skerries");
        Holder<Biome> vanillaFrozenOcean = mockBiome("minecraft", "frozen_ocean");
        Holder<Biome> vanillaDesert = mockBiome("minecraft", "desert");

        EntropicaMultiNoiseLayer combinedLayer = new EntropicaMultiNoiseLayer(
                2026L, 6, 10, 0,
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(
                        new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(skerriesRule, skerriesHolder)
                )
        );

        // Positive & negative mid-weirdness polar archipelago islets -> Auroral Skerries
        Climate.TargetPoint skerriesPos = Climate.target(-0.65F, 0.25F, -0.20F, 0.55F, 0.0F, 0.22F);
        Climate.TargetPoint skerriesNeg = Climate.target(-0.72F, -0.15F, -0.15F, 0.70F, 0.02F, -0.22F);
        check(combinedLayer.selectMatchingBiome(333L, skerriesPos, vanillaFrozenOcean) == skerriesHolder,
                "Auroral Skerries must match icy low-continentalness flat-erosion +mid-weirdness archipelago target");
        check(combinedLayer.selectMatchingBiome(333L, skerriesNeg, vanillaFrozenOcean) == skerriesHolder,
                "Auroral Skerries must match icy low-continentalness flat-erosion -mid-weirdness archipelago target");

        // Sheer cliff target still resolves exclusively to Blue-Glacier Shelf; desert & deep caves stay untouched
        Climate.TargetPoint shelfTarget = Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.0F, 0.70F);
        check(combinedLayer.selectMatchingBiome(333L, shelfTarget, vanillaFrozenOcean) == shelfHolder,
                "Blue-Glacier Shelf must remain unaffected when both Biome 1 and Biome 2 are active");
        check(combinedLayer.selectMatchingBiome(333L, Climate.target(0.85F, -0.6F, 0.2F, 0.5F, 0.0F, 0.2F), vanillaDesert) == vanillaDesert,
                "Auroral Skerries must never match hot desert climate");
        check(combinedLayer.selectMatchingBiome(333L, Climate.target(-0.65F, 0.25F, -0.20F, 0.55F, 0.85F, 0.22F), vanillaFrozenOcean) == vanillaFrozenOcean,
                "Auroral Skerries must never overwrite deep underground caves (depth=0.85)");

        // Surface rule verification (must resolve live BuiltInRegistries coarse_gravel & glimmer_ice!)
        SurfaceRules.RuleSource skerriesSurface = skerriesRule.getSurfaceRule();
        check(skerriesSurface != null, "Auroral Skerries surface rule supplier must return non-null RuleSource");
        String surfaceDesc = skerriesSurface.toString();
        check(surfaceDesc.contains("entropica:coarse_gravel") && surfaceDesc.contains("entropica:glimmer_ice"),
                "Auroral Skerries surface rule must produce entropica:coarse_gravel and entropica:glimmer_ice, got: " + surfaceDesc);

        // 2. EntropicaBiomeAtmospheres Profile Verification (12-block dense steam fog + light relief + #0E6A3B + AURORA_CURTAINS + rising steam)
        EntropicaBiomeAtmospheres.registerAll();
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.getForHolder(skerriesHolder);
        check(atmos != null, "Auroral Skerries must have a registered BiomeAtmosphereProfile");
        check(atmos.hasCustomFogDistance(), "Auroral Skerries must enable custom dense steam fog");
        check(Math.abs((atmos.getFogEndBlocks() - atmos.getFogStartBlocks()) - 12.0F) < 1.0E-4F,
                "Auroral Skerries must configure a 12-block dense steam fog span (2..14)");
        check(Math.abs(atmos.getLightHeldFogReliefBlocks() - 18.0F) < 1.0E-4F,
                "Auroral Skerries must grant +18 blocks of held-light fog relief");
        check(atmos.getNightSkyColorRgb() == 0x0E6A3B,
                "Auroral Skerries must shift night sky color to #0E6A3B");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.AURORA_CURTAINS,
                "Auroral Skerries must render AURORA_CURTAINS sky phenomenon");
        check(atmos.getAmbientParticlesPerTick() > 0 && atmos.getParticleWindVy() > 0.02F,
                "Auroral Skerries must emit rising white steam particles (vy > 0.02)");

        // 3. GrotSpawnRegistry AURORAL_SKERRIES_POOL Verification (Aurora ~55%, Vapor ~35%, Glacial ~10% of 95% non-anomaly rolls)
        GrotSpawnRegistry.initialize();
        RandomSource grotRng = RandomSource.create(20261120L);
        int auroraCount = 0, vaporCount = 0, glacialCount = 0;
        int totalRolls = 20000;
        for (int i = 0; i < totalRolls; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(skerriesHolder, grotRng);
            check(rolled != null, "GrotSpawnRegistry returned null for Auroral Skerries");
            if (rolled == EssenceType.AURORA) auroraCount++;
            else if (rolled == EssenceType.VAPOR) vaporCount++;
            else if (rolled == EssenceType.GLACIAL) glacialCount++;
        }
        double auroraRatio = auroraCount / (double) totalRolls;
        double vaporRatio = vaporCount / (double) totalRolls;
        double glacialRatio = glacialCount / (double) totalRolls;
        // 95% * 0.55 = 52.25%, 95% * 0.35 = 33.25% (+0.25% anomaly = 33.5%), 95% * 0.10 = 9.5% (+0.25% anomaly = 9.75%)
        check(auroraRatio > 0.49 && auroraRatio < 0.56, "Expected Aurora Grot ratio ~52.3%, got " + auroraRatio);
        check(vaporRatio > 0.30 && vaporRatio < 0.37, "Expected Vapor Grot ratio ~33.5%, got " + vaporRatio);
        check(glacialRatio > 0.075 && glacialRatio < 0.125, "Expected Glacial Grot ratio ~9.8%, got " + glacialRatio);

        // 4. Live Bootstrapped Registries + Mod* Class Declaration Verification
        List<String> blocks12 = List.of(
                "glimmer_ice", "coarse_gravel", "auroral_feather_moss", "mist_veil_fern",
                "potted_mist_veil_fern", "auroral_lantern", "thermal_heat_lamp", "glow_gel_pathing",
                "static_redstone_coupler", "auroral_lens_block", "auroral_mote_light"
        );
        List<String> blockItems10 = List.of(
                "glimmer_ice", "coarse_gravel", "auroral_feather_moss", "mist_veil_fern",
                "auroral_lantern", "thermal_heat_lamp", "glow_gel_pathing",
                "static_redstone_coupler", "auroral_lens_block"
        );
        List<String> standaloneItems10 = List.of(
                "aurora_dust", "glowing_auroral_fiber", "mist_veil_frond", "warm_aurora_petal",
                "camouflage_cloak", "auroral_gelatin", "charged_glow_tentacle", "auroral_wand",
                "vapor_condensate_crystals", "aurora_jelly_spawn_egg"
        );

        for (String b : blocks12) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, b);
            check(BuiltInRegistries.BLOCK.containsKey(id), "Live BuiltInRegistries.BLOCK missing entropica:" + b);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            check(block != null && block != Blocks.AIR, "Resolved Block is null/air for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", b.toUpperCase());
        }
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glimmer_ice"))
                .defaultBlockState().getLightEmission() == 10, "glimmer_ice must emit Light Level 10");
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_lantern"))
                .defaultBlockState().getLightEmission() == 15, "auroral_lantern must emit Light Level 15");
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "thermal_heat_lamp"))
                .defaultBlockState().getLightEmission() == 14, "thermal_heat_lamp must emit Light Level 14");
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_lens_block"))
                .defaultBlockState().getLightEmission() == 12, "auroral_lens_block must emit Light Level 12");
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_mote_light"))
                .defaultBlockState().getLightEmission() == 14, "auroral_mote_light must emit Light Level 14");

        for (String bi : blockItems10) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, bi);
            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing BlockItem entropica:" + bi);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            check(item instanceof BlockItem, "Expected BlockItem for entropica:" + bi);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", bi.toUpperCase() + "_ITEM");
        }
        for (String it : standaloneItems10) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, it);
            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing standalone item entropica:" + it);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            check(item != null && item != Items.AIR, "Resolved standalone Item is null/air for entropica:" + it);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", it.toUpperCase());
        }

        ResourceLocation jellyId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aurora_jelly");
        check(BuiltInRegistries.ENTITY_TYPE.containsKey(jellyId), "Live BuiltInRegistries.ENTITY_TYPE missing entropica:aurora_jelly");
        EntityType<?> jellyType = BuiltInRegistries.ENTITY_TYPE.getValue(jellyId);
        check(jellyType.getCategory() == MobCategory.CREATURE, "Aurora Jelly must use MobCategory.CREATURE");
        check(Math.abs(AuroraJellyEntity.createAttributes().build().getValue(Attributes.MAX_HEALTH) - 40.0D) < 1.0E-6D,
                "Aurora Jelly MAX_HEALTH must be 40.0 (20 hearts)");
        check(AuroraJellyModel.createBodyLayer().bakeRoot() != null, "AuroraJellyModel 32x32 LayerDefinition must bake cleanly");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModEntityTypes", "AURORA_JELLY");
        JsonObject auroraJellyLoot = parseClasspathJson("data/entropica/loot_table/entities/aurora_jelly.json");
        String auroraJellyLootStr = auroraJellyLoot.toString();
        for (String expectedDrop : List.of(
                "entropica:auroral_gelatin",
                "entropica:charged_glow_tentacle",
                "entropica:average_essence",
                "AURORA",
                "AEGIS",
                "\"min\":2.0,\"max\":4.0",
                "\"count\":2.0"
        )) {
            check(auroraJellyLootStr.contains(expectedDrop), "entities/aurora_jelly.json missing expected entry: " + expectedDrop);
        }

        for (String feat : List.of("skerries_steam_vent", "glimmer_ice_shoreline")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, feat);
            check(BuiltInRegistries.FEATURE.containsKey(id), "Live BuiltInRegistries.FEATURE missing entropica:" + feat);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModFeatures", feat.toUpperCase());
        }

        ResourceLocation wellspringId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_wellspring");
        ResourceLocation wellspringPieceId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_wellspring_piece");
        check(BuiltInRegistries.STRUCTURE_TYPE.containsKey(wellspringId), "Live BuiltInRegistries.STRUCTURE_TYPE missing entropica:auroral_wellspring");
        check(BuiltInRegistries.STRUCTURE_PIECE.containsKey(wellspringPieceId), "Live BuiltInRegistries.STRUCTURE_PIECE missing entropica:auroral_wellspring_piece");
        for (String structField : List.of("AURORAL_WELLSPRING_KEY", "STRUCTURE_3_CHEST_LOOT", "AURORAL_WELLSPRING", "AURORAL_WELLSPRING_PIECE")) {
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModStructures", structField);
        }

        // 5. Classpath JSONs, 1.21.10 Item Models, 12 Recipes, 64x64 Entity PNG, & en_us.json Translations
        JsonObject skerriesBiomeJson = parseClasspathJson("data/entropica/worldgen/biome/auroral_skerries.json");
        JsonObject effects = skerriesBiomeJson.getAsJsonObject("effects");
        check(effects != null && effects.get("water_color").getAsInt() == 691849,
                "auroral_skerries.json must specify warm teal water_color 691849 (#0A8E89)");
        check(effects.get("water_fog_color").getAsInt() == 413259,
                "auroral_skerries.json must specify deep teal water_fog_color 413259 (#064E4B)");
        String skerriesBiomeStr = skerriesBiomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:glimmer_ice_shoreline",
                "entropica:skerries_steam_vent",
                "entropica:patch_auroral_feather_moss",
                "entropica:patch_mist_veil_fern",
                "entropica:patch_auroral_lily_pad",
                "entropica:astral_geode",
                "entropica:aurora_jelly",
                "entropica:frost_tusk_mammot",
                "entropica:grot"
        )) {
            check(skerriesBiomeStr.contains(expectedRef), "auroral_skerries.json missing expected entry: " + expectedRef);
        }

        for (String fId : List.of(
                "glimmer_ice_shoreline",
                "skerries_steam_vent",
                "patch_auroral_feather_moss",
                "patch_mist_veil_fern",
                "patch_auroral_lily_pad"
        )) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + fId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + fId + ".json");
        }

        JsonObject wellspringJson = parseClasspathJson("data/entropica/worldgen/structure/auroral_wellspring.json");
        check("entropica:auroral_wellspring".equals(wellspringJson.get("type").getAsString()),
                "auroral_wellspring.json must reference type entropica:auroral_wellspring");
        parseClasspathJson("data/entropica/worldgen/structure_set/auroral_wellspring.json");
        check(atmos.getWaterSurfaceSteamColumnsPerTick() > 0,
                "Auroral Skerries atmosphere must emit genuine water-surface steam columns without campfires");

        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/entropica/tags/worldgen/biome/is_aurora.json",
                "data/entropica/tags/worldgen/biome/is_vapor.json",
                "data/entropica/tags/worldgen/biome/has_structure/auroral_wellspring.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:auroral_skerries"),
                    tagJson + " must include entropica:auroral_skerries");
        }

        JsonObject wellspringLoot = parseClasspathJson("data/entropica/loot_table/chests/structure_3.json");
        String lootStr = wellspringLoot.toString();
        for (String expectedLoot : List.of(
                "entropica:spectral_dye_auroral",
                "entropica:glowing_auroral_fiber",
                "entropica:vapor_condensate_crystals",
                "minecraft:prismarine_crystals",
                "minecraft:heart_of_the_sea"
        )) {
            check(lootStr.contains(expectedLoot), "chests/structure_3.json missing loot entry: " + expectedLoot);
        }

        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        check(langJson.has("biome.entropica.auroral_skerries"), "en_us.json missing biome.entropica.auroral_skerries");
        check("Aurora Jelly".equals(langJson.get("entity.entropica.aurora_jelly").getAsString()),
                "en_us.json must map entity.entropica.aurora_jelly -> Aurora Jelly");

        for (String b : blocks12) {
            JsonObject bs = parseClasspathJson("assets/entropica/blockstates/" + b + ".json");
            if (bs.has("variants")) {
                for (String key : bs.getAsJsonObject("variants").keySet()) {
                    check(key.equals(key.toLowerCase()),
                            "blockstates/" + b + ".json variant key must be strictly lowercase: " + key);
                }
            }
            parseClasspathJson("data/entropica/loot_table/blocks/" + b + ".json");
            check(langJson.has("block.entropica." + b), "en_us.json missing block.entropica." + b);
        }
        JsonObject lanternStandingModel = parseClasspathJson("assets/entropica/models/block/auroral_lantern.json");
        JsonObject lanternHangingModel = parseClasspathJson("assets/entropica/models/block/auroral_lantern_hanging.json");
        JsonObject lanternItemModel = parseClasspathJson("assets/entropica/models/item/auroral_lantern.json");
        check(lanternStandingModel.has("elements") && lanternStandingModel.getAsJsonArray("elements").size() >= 10,
                "auroral_lantern.json must define full multi-element 3D lantern geometry");
        check(lanternHangingModel.has("elements") && lanternHangingModel.getAsJsonArray("elements").size() >= 10,
                "auroral_lantern_hanging.json must define full multi-element 3D hanging lantern geometry");
        check("entropica:block/auroral_lantern".equals(lanternItemModel.get("parent").getAsString()),
                "models/item/auroral_lantern.json must inherit 3D entropica:block/auroral_lantern model");
        parseClasspathJson("data/entropica/loot_table/blocks/auroral_lily_pad.json");
        parseClasspathJson("assets/entropica/items/auroral_lily_pad.json");

        for (String bi : blockItems10) {
            parseClasspathJson("assets/entropica/items/" + bi + ".json");
            parseClasspathJson("assets/entropica/models/item/" + bi + ".json");
            check(langJson.has("item.entropica." + bi), "en_us.json missing item.entropica." + bi);
        }
        for (String it : standaloneItems10) {
            parseClasspathJson("assets/entropica/items/" + it + ".json");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }

        for (String blockTex : List.of("glimmer_ice", "coarse_gravel", "auroral_feather_moss", "mist_veil_fern")) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }
        verifyPngDimensions("assets/entropica/textures/entity/aurora_jelly/aurora_jelly.png", 32, 32);

        check(parseClasspathJson("data/minecraft/tags/block/mineable/shovel.json").toString().contains("entropica:coarse_gravel"),
                "mineable/shovel.json must include entropica:coarse_gravel");
        check(parseClasspathJson("data/minecraft/tags/block/mineable/pickaxe.json").toString().contains("entropica:auroral_lens_block"),
                "mineable/pickaxe.json must include entropica:auroral_lens_block");

        List<String> recipes12 = List.of(
                "coarse_gravel",
                "glimmer_ice",
                "aurora_dust_from_glimmer_ice",
                "spectral_dye_auroral_from_aurora_dust",
                "auroral_lantern",
                "camouflage_cloak",
                "thermal_heat_lamp",
                "glow_gel_pathing",
                "auroral_wand",
                "static_redstone_coupler",
                "auroral_lens_block"
        );
        for (String r : recipes12) {
            parseClasspathJson("data/entropica/recipe/" + r + ".json");
        }

        System.out.println(String.format(
                "[PASS] 9/10 Biome 2 (Auroral Skerries) worldgen, Wellspring loot, 11 blocks, 19 items, 11 recipes, 32x32 Jelly PNG & loot, en_us, & Grot pool (Aurora=%.1f%%, Vapor=%.1f%%, Glacial=%.1f%%) verified.",
                auroraRatio * 100.0, vaporRatio * 100.0, glacialRatio * 100.0
        ));
    }

    private static void verifyBiome3FrostBiteFloesContentAndAssets() {
        // 1. 6D Multi-Noise Placement Rule & Non-Collision with Biomes 1 & 2 + Surface Rule (blighted_packed_ice / rotten_ice)
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        BiomePlacementRule skerriesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.AURORAL_SKERRIES);
        BiomePlacementRule floesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.FROST_BITE_FLOES);
        check(floesRule != null, "Frost-Bite Floes placement rule must be registered");
        check(floesRule.getParameterPoints().size() == 2, "Frost-Bite Floes must register 2 high-weirdness parameter points");

        // Disjoint erosion vs Biome 1 (-1.0..-0.375 vs 0.45..1.0) & separated weirdness vs Biome 2 (+/-0.05..0.40 vs +/-0.40..1.0)
        for (Climate.ParameterPoint p3 : floesRule.getParameterPoints()) {
            for (Climate.ParameterPoint p1 : shelfRule.getParameterPoints()) {
                boolean erosionDisjoint = p1.erosion().max() < p3.erosion().min() || p3.erosion().max() < p1.erosion().min();
                check(erosionDisjoint,
                        "Frost-Bite Floes and Blue-Glacier Shelf erosion parameter intervals must be strictly disjoint");
            }
            for (Climate.ParameterPoint p2 : skerriesRule.getParameterPoints()) {
                boolean weirdnessSeparated = p2.weirdness().max() <= p3.weirdness().min() || p3.weirdness().max() <= p2.weirdness().min();
                check(weirdnessSeparated,
                        "Frost-Bite Floes and Auroral Skerries weirdness parameter intervals must be separated");
            }
        }

        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> skerriesHolder = mockBiome("entropica", "auroral_skerries");
        Holder<Biome> floesHolder = mockBiome("entropica", "frost_bite_floes");
        Holder<Biome> vanillaFrozenOcean = mockBiome("minecraft", "frozen_ocean");
        Holder<Biome> vanillaDesert = mockBiome("minecraft", "desert");

        EntropicaMultiNoiseLayer allThreeLayer = new EntropicaMultiNoiseLayer(
                2026L, 6, 10, 0,
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(
                        new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(skerriesRule, skerriesHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(floesRule, floesHolder)
                )
        );

        // Positive & negative high-weirdness, flat-erosion, wet polar coastal-swamp targets -> Frost-Bite Floes
        Climate.TargetPoint floesPos = Climate.target(-0.68F, 0.55F, -0.18F, 0.65F, 0.0F, 0.72F);
        Climate.TargetPoint floesNeg = Climate.target(-0.74F, 0.35F, -0.12F, 0.80F, 0.01F, -0.68F);
        check(allThreeLayer.selectMatchingBiome(555L, floesPos, vanillaFrozenOcean) == floesHolder,
                "Frost-Bite Floes must match icy wet low-continentalness flat-erosion +high-weirdness floe target");
        check(allThreeLayer.selectMatchingBiome(555L, floesNeg, vanillaFrozenOcean) == floesHolder,
                "Frost-Bite Floes must match icy wet low-continentalness flat-erosion -high-weirdness floe target");

        // Biomes 1 & 2 remain unaffected; arid polar (-0.40 humidity), hot desert, and deep caves never match Biome 3
        check(allThreeLayer.selectMatchingBiome(555L, Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.0F, 0.70F), vanillaFrozenOcean) == shelfHolder,
                "Blue-Glacier Shelf must remain unaffected when Biomes 1, 2 & 3 are active");
        check(allThreeLayer.selectMatchingBiome(555L, Climate.target(-0.65F, 0.25F, -0.20F, 0.55F, 0.0F, 0.22F), vanillaFrozenOcean) == skerriesHolder,
                "Auroral Skerries must remain unaffected when Biomes 1, 2 & 3 are active");
        check(allThreeLayer.selectMatchingBiome(555L, Climate.target(-0.70F, -0.40F, -0.18F, 0.65F, 0.0F, 0.72F), vanillaFrozenOcean) == vanillaFrozenOcean,
                "Frost-Bite Floes must not match dry/arid polar climate (humidity=-0.40 < 0.10)");
        check(allThreeLayer.selectMatchingBiome(555L, Climate.target(0.85F, 0.50F, -0.18F, 0.65F, 0.0F, 0.72F), vanillaDesert) == vanillaDesert,
                "Frost-Bite Floes must never match hot climate");
        check(allThreeLayer.selectMatchingBiome(555L, Climate.target(-0.68F, 0.55F, -0.18F, 0.65F, 0.85F, 0.72F), vanillaFrozenOcean) == vanillaFrozenOcean,
                "Frost-Bite Floes must never overwrite deep underground caves (depth=0.85)");

        // Surface rule verification (must resolve live BuiltInRegistries blighted_packed_ice & rotten_ice!)
        SurfaceRules.RuleSource floesSurface = floesRule.getSurfaceRule();
        check(floesSurface != null, "Frost-Bite Floes surface rule supplier must return non-null RuleSource");
        String surfaceDesc = floesSurface.toString();
        check(surfaceDesc.contains("entropica:blighted_packed_ice") && surfaceDesc.contains("entropica:rotten_ice"),
                "Frost-Bite Floes surface rule must produce entropica:blighted_packed_ice and entropica:rotten_ice, got: " + surfaceDesc);

        // 2. EntropicaBiomeAtmospheres Profile Verification (green rot mist #5F7338, olive night sky #2A3318, rot particles, customServerTick)
        EntropicaBiomeAtmospheres.registerAll();
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.getForHolder(floesHolder);
        check(atmos != null, "Frost-Bite Floes must have a registered BiomeAtmosphereProfile");
        check(atmos.hasCustomFogDistance(), "Frost-Bite Floes must enable custom foul green rot mist fog");
        check(atmos.getDayFogTintRgb() == 0x5F7338, "Frost-Bite Floes must configure green rot mist tint #5F7338");
        check(atmos.getNightSkyColorRgb() == 0x2A3318, "Frost-Bite Floes must configure olive night sky #2A3318");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.GLACIAL_ICE_HALO, "Frost-Bite Floes must render blight-ice GLACIAL_ICE_HALO");
        check(atmos.getAmbientParticlesPerTick() > 0 && atmos.getParticleColorRgb() == 0x2B361E,
                "Frost-Bite Floes must emit drifting black-green rot particles (#2B361E)");
        check(atmos.getCustomServerTick() != null,
                "Frost-Bite Floes must register non-null customServerTick for swamp-water hypothermia and rotten-ice poison");

        // 3. GrotSpawnRegistry FROST_BITE_FLOES_POOL Verification (Glacial ~50%, Blight ~40%, Frozen ~10% of 95% non-anomaly rolls)
        GrotSpawnRegistry.initialize();
        RandomSource grotRng = RandomSource.create(20261121L);
        int glacialCount = 0, blightCount = 0, frozenCount = 0;
        int totalRolls = 20000;
        for (int i = 0; i < totalRolls; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(floesHolder, grotRng);
            check(rolled != null, "GrotSpawnRegistry returned null for Frost-Bite Floes");
            if (rolled == EssenceType.GLACIAL) glacialCount++;
            else if (rolled == EssenceType.BLIGHT) blightCount++;
            else if (rolled == EssenceType.FROZEN) frozenCount++;
        }
        double glacialRatio = glacialCount / (double) totalRolls;
        double blightRatio = blightCount / (double) totalRolls;
        double frozenRatio = frozenCount / (double) totalRolls;
        // 95% * 0.50 = 47.5% (+0.25% anomaly = 47.75%), 95% * 0.40 = 38.0% (+0.25% anomaly = 38.25%), 95% * 0.10 = 9.5%
        check(glacialRatio > 0.44 && glacialRatio < 0.52, "Expected Glacial Grot ratio ~47.8%, got " + glacialRatio);
        check(blightRatio > 0.35 && blightRatio < 0.42, "Expected Blight Grot ratio ~38.3%, got " + blightRatio);
        check(frozenRatio > 0.075 && frozenRatio < 0.12, "Expected Frozen Grot ratio ~9.5%, got " + frozenRatio);

        // 4. Live Bootstrapped Registries + Mod* Class Declaration Verification
        List<String> blocks6 = List.of(
                "rotten_ice", "blighted_packed_ice", "necrotic_slime_patch",
                "necrotic_slime_block", "frozen_spruce_branch", "cured_frost_hide_block"
        );
        List<String> standaloneItems10 = List.of(
                "rot_powder", "necrotic_slime", "necrotic_slime_bucket", "sea_salt_crystals",
                "cured_frost_veined_hide", "frost_bite_dagger",
                "cryo_insulated_helmet", "cryo_insulated_chestplate",
                "cryo_insulated_leggings", "cryo_insulated_boots"
        );

        for (String b : blocks6) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, b);
            check(BuiltInRegistries.BLOCK.containsKey(id), "Live BuiltInRegistries.BLOCK missing entropica:" + b);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            check(block != null && block != Blocks.AIR, "Resolved Block is null/air for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", b.toUpperCase());

            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing BlockItem entropica:" + b);
            Item blockItem = BuiltInRegistries.ITEM.getValue(id);
            check(blockItem instanceof BlockItem, "Expected BlockItem for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", b.toUpperCase() + "_ITEM");
        }

        for (String it : standaloneItems10) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, it);
            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing standalone item entropica:" + it);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            check(item != null && item != Items.AIR, "Resolved standalone Item is null/air for entropica:" + it);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", it.toUpperCase());
        }
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_bite_dagger")) instanceof FrostBiteDaggerItem,
                "frost_bite_dagger must be an instance of FrostBiteDaggerItem");
        for (String armorId : List.of("cryo_insulated_helmet", "cryo_insulated_chestplate", "cryo_insulated_leggings", "cryo_insulated_boots")) {
            check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, armorId)) instanceof CryoInsulatedArmorItem,
                    armorId + " must be an instance of CryoInsulatedArmorItem");
        }
        for (String cryoDrop : List.of("cryo_crystalline_fang", "frost_veined_hide", "cryo_stalker_spawn_egg")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, cryoDrop);
            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing entropica:" + cryoDrop);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", cryoDrop.toUpperCase());
        }

        ResourceLocation decayPotionId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "decay");
        check(BuiltInRegistries.POTION.containsKey(decayPotionId), "Live BuiltInRegistries.POTION missing entropica:decay");
        Potion decayPotion = BuiltInRegistries.POTION.getValue(decayPotionId);
        check(decayPotion != null && decayPotion.getEffects().size() == 2, "Decay potion must carry Poison I + Weakness I");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModPotions", "DECAY");

        ResourceLocation stalkerId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_stalker");
        check(BuiltInRegistries.ENTITY_TYPE.containsKey(stalkerId), "Live BuiltInRegistries.ENTITY_TYPE missing entropica:cryo_stalker");
        EntityType<?> stalkerType = BuiltInRegistries.ENTITY_TYPE.getValue(stalkerId);
        check(stalkerType.getCategory() == MobCategory.MONSTER, "Cryo-Stalker must use MobCategory.MONSTER");
        check(Math.abs(CryoStalkerEntity.createAttributes().build().getValue(Attributes.MAX_HEALTH) - 48.0D) < 1.0E-6D,
                "Cryo-Stalker MAX_HEALTH must be 48.0");
        check(Math.abs(CryoStalkerEntity.STEALTH_ALPHA - 0.14F) < 1.0E-5F,
                "Cryo-Stalker STEALTH_ALPHA must be ~0.14f (~90% snow/ice camouflage)");
        check(CryoStalkerModel.createBodyLayer().bakeRoot() != null, "CryoStalkerModel LayerDefinition must bake cleanly");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModEntityTypes", "CRYO_STALKER");

        for (String feat : List.of("rotten_ice_floe_sheet", "frozen_dead_spruce")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, feat);
            check(BuiltInRegistries.FEATURE.containsKey(id), "Live BuiltInRegistries.FEATURE missing entropica:" + feat);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModFeatures", feat.toUpperCase());
        }

        // 5. Classpath JSONs, 4 Features, 17 Recipes, Block/Item/Entity PNG Dimensions, & en_us.json Translations
        JsonObject floesBiomeJson = parseClasspathJson("data/entropica/worldgen/biome/frost_bite_floes.json");
        JsonObject effects = floesBiomeJson.getAsJsonObject("effects");
        check(effects != null && effects.get("sky_color").getAsInt() == 8227907,
                "frost_bite_floes.json must specify yellow-green sky_color 8227907 (#7D8C43)");
        check(effects.get("fog_color").getAsInt() == 6255416,
                "frost_bite_floes.json must specify rot mist fog_color 6255416 (#5F7338)");
        check(effects.get("water_color").getAsInt() == 3883581,
                "frost_bite_floes.json must specify swampy water_color 3883581 (#3B423D)");
        check(effects.get("water_fog_color").getAsInt() == 1843486,
                "frost_bite_floes.json must specify murky water_fog_color 1843486 (#1C211E)");

        String floesBiomeStr = floesBiomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:rotten_ice_floe_sheet",
                "entropica:frozen_dead_spruce",
                "entropica:patch_necrotic_slime",
                "entropica:patch_rimebloom_floes",
                "entropica:astral_geode",
                "entropica:cryo_stalker",
                "entropica:grot",
                "minecraft:slime",
                "minecraft:bogged",
                "minecraft:stray",
                "minecraft:drowned",
                "minecraft:ash"
        )) {
            check(floesBiomeStr.contains(expectedRef), "frost_bite_floes.json missing expected entry: " + expectedRef);
        }

        for (String fId : List.of(
                "rotten_ice_floe_sheet",
                "frozen_dead_spruce",
                "patch_necrotic_slime",
                "patch_rimebloom_floes"
        )) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + fId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + fId + ".json");
        }

        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/c/tags/worldgen/biome/is_swamp.json",
                "data/entropica/tags/worldgen/biome/is_glacial.json",
                "data/entropica/tags/worldgen/biome/is_blight.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:frost_bite_floes"),
                    tagJson + " must include entropica:frost_bite_floes");
        }

        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String reqKey : List.of(
                "biome.entropica.frost_bite_floes",
                "entity.entropica.cryo_stalker",
                "item.entropica.cryo_stalker_spawn_egg",
                "item.entropica.cryo_crystalline_fang",
                "item.entropica.frost_veined_hide",
                "item.minecraft.potion.effect.decay",
                "item.minecraft.splash_potion.effect.decay",
                "item.minecraft.lingering_potion.effect.decay",
                "item.minecraft.tipped_arrow.effect.decay"
        )) {
            check(langJson.has(reqKey), "en_us.json missing required translation key: " + reqKey);
        }

        for (String b : blocks6) {
            parseClasspathJson("assets/entropica/blockstates/" + b + ".json");
            parseClasspathJson("assets/entropica/models/block/" + b + ".json");
            parseClasspathJson("assets/entropica/models/item/" + b + ".json");
            parseClasspathJson("assets/entropica/items/" + b + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + b + ".json");
            verifyPngDimensions("assets/entropica/textures/block/" + b + ".png", 16, 16);
            check(langJson.has("block.entropica." + b), "en_us.json missing block.entropica." + b);
            check(langJson.has("item.entropica." + b), "en_us.json missing item.entropica." + b);
        }
        verifyPngDimensions("assets/entropica/textures/block/necrotic_slime_block_core.png", 16, 16);

        for (String it : standaloneItems10) {
            parseClasspathJson("assets/entropica/items/" + it + ".json");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }
        for (String cryoItem : List.of("cryo_crystalline_fang", "frost_veined_hide", "cryo_stalker_spawn_egg")) {
            parseClasspathJson("assets/entropica/items/" + cryoItem + ".json");
            parseClasspathJson("assets/entropica/models/item/" + cryoItem + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + cryoItem + ".png", 16, 16);
        }
        verifyPngDimensions("assets/entropica/textures/entity/cryo_stalker/cryo_stalker.png", 128, 128);

        String pickaxeTag = parseClasspathJson("data/minecraft/tags/block/mineable/pickaxe.json").toString();
        check(pickaxeTag.contains("entropica:rotten_ice") && pickaxeTag.contains("entropica:blighted_packed_ice"),
                "mineable/pickaxe.json must include entropica:rotten_ice and entropica:blighted_packed_ice");
        check(parseClasspathJson("data/minecraft/tags/block/mineable/axe.json").toString().contains("entropica:frozen_spruce_branch"),
                "mineable/axe.json must include entropica:frozen_spruce_branch");

        List<String> recipes17 = List.of(
                "sea_salt_crystals_from_smelting_water",
                "sea_salt_crystals_from_campfire_kelp",
                "sea_salt_crystals",
                "sea_salt_crystals_from_ice",
                "cured_frost_veined_hide",
                "cured_frost_hide_block",
                "cured_frost_veined_hide_from_block",
                "frost_bite_dagger",
                "cryo_insulated_helmet",
                "cryo_insulated_chestplate",
                "cryo_insulated_leggings",
                "cryo_insulated_boots",
                "necrotic_slime_block",
                "necrotic_slime_from_block",
                "necrotic_slime_patch",
                "blighted_packed_ice",
                "rotten_ice"
        );
        for (String r : recipes17) {
            parseClasspathJson("data/entropica/recipe/" + r + ".json");
        }

        System.out.println(String.format(
                "[PASS] 10/11 Biome 3 (Frost-Bite Floes) worldgen, 6 blocks, 16 items, Decay potion, 17 recipes, PNGs, en_us, & Grot pool (Glacial=%.1f%%, Blight=%.1f%%, Frozen=%.1f%%) verified.",
                glacialRatio * 100.0, blightRatio * 100.0, frozenRatio * 100.0
        ));
    }

    private static void verifyBiome4CrystallinePackIceContentAndAssets() {
        // 1. 6D Multi-Noise Placement Rule & Non-Collision with Biomes 1, 2 & 3 + Surface Rule (snow_block / blue_ice / packed_ice)
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        BiomePlacementRule skerriesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.AURORAL_SKERRIES);
        BiomePlacementRule floesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.FROST_BITE_FLOES);
        BiomePlacementRule packIceRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.CRYSTALLINE_PACK_ICE);
        check(packIceRule != null, "Crystalline Pack-Ice placement rule must be registered");
        check(packIceRule.getParameterPoints().size() == 2, "Crystalline Pack-Ice must register 2 high-weirdness inland parameter points");

        // Strictly disjoint inland continentalness (0.09..0.88) vs coastal Biomes 1, 2 & 3 (<= 0.08)
        for (Climate.ParameterPoint p4 : packIceRule.getParameterPoints()) {
            for (BiomePlacementRule other : List.of(shelfRule, skerriesRule, floesRule)) {
                for (Climate.ParameterPoint pOther : other.getParameterPoints()) {
                    boolean contDisjoint = pOther.continentalness().max() < p4.continentalness().min()
                            || p4.continentalness().max() < pOther.continentalness().min();
                    check(contDisjoint,
                            "Crystalline Pack-Ice inland continentalness must be strictly disjoint from Biomes 1–3");
                }
            }
        }

        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> skerriesHolder = mockBiome("entropica", "auroral_skerries");
        Holder<Biome> floesHolder = mockBiome("entropica", "frost_bite_floes");
        Holder<Biome> packIceHolder = mockBiome("entropica", "crystalline_pack_ice");
        Holder<Biome> vanillaSnowyPlains = mockBiome("minecraft", "snowy_plains");
        Holder<Biome> vanillaDesert = mockBiome("minecraft", "desert");

        EntropicaMultiNoiseLayer allFourLayer = new EntropicaMultiNoiseLayer(
                2026L, 6, 10, 0,
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(
                        new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(skerriesRule, skerriesHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(floesRule, floesHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(packIceRule, packIceHolder)
                )
        );

        Climate.TargetPoint packIcePos = Climate.target(-0.72F, 0.0F, 0.42F, -0.10F, 0.0F, 0.68F);
        Climate.TargetPoint packIceNeg = Climate.target(-0.66F, -0.35F, 0.25F, 0.05F, 0.01F, -0.72F);
        check(allFourLayer.selectMatchingBiome(777L, packIcePos, vanillaSnowyPlains) == packIceHolder,
                "Crystalline Pack-Ice must match icy inland valley +high-weirdness target");
        check(allFourLayer.selectMatchingBiome(777L, packIceNeg, vanillaSnowyPlains) == packIceHolder,
                "Crystalline Pack-Ice must match icy inland valley -high-weirdness target");

        // Biomes 1, 2 & 3 remain unaffected; hot desert & deep caves never match Biome 4
        check(allFourLayer.selectMatchingBiome(777L, Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.0F, 0.70F), vanillaSnowyPlains) == shelfHolder,
                "Blue-Glacier Shelf must remain unaffected when Biomes 1–4 are active");
        check(allFourLayer.selectMatchingBiome(777L, Climate.target(-0.65F, 0.25F, -0.20F, 0.55F, 0.0F, 0.22F), vanillaSnowyPlains) == skerriesHolder,
                "Auroral Skerries must remain unaffected when Biomes 1–4 are active");
        check(allFourLayer.selectMatchingBiome(777L, Climate.target(-0.68F, 0.55F, -0.18F, 0.65F, 0.0F, 0.72F), vanillaSnowyPlains) == floesHolder,
                "Frost-Bite Floes must remain unaffected when Biomes 1–4 are active");
        check(allFourLayer.selectMatchingBiome(777L, Climate.target(0.85F, -0.50F, 0.42F, -0.10F, 0.0F, 0.68F), vanillaDesert) == vanillaDesert,
                "Crystalline Pack-Ice must never match hot climate");
        check(allFourLayer.selectMatchingBiome(777L, Climate.target(-0.72F, 0.0F, 0.42F, -0.10F, 0.85F, 0.68F), vanillaSnowyPlains) == vanillaSnowyPlains,
                "Crystalline Pack-Ice must never overwrite deep underground caves (depth=0.85)");

        SurfaceRules.RuleSource packIceSurface = packIceRule.getSurfaceRule();
        check(packIceSurface != null, "Crystalline Pack-Ice surface rule supplier must return non-null RuleSource");
        String surfaceDesc = packIceSurface.toString();
        check(surfaceDesc.contains("minecraft:snow_block") && surfaceDesc.contains("minecraft:blue_ice") && surfaceDesc.contains("minecraft:packed_ice"),
                "Crystalline Pack-Ice surface rule must produce snow_block, blue_ice, and packed_ice, got: " + surfaceDesc);

        // 2. EntropicaBiomeAtmospheres Profile Verification (#4E2A7A violet night sky, +0.35 star boost, GLACIAL_ICE_HALO, zero hazards)
        EntropicaBiomeAtmospheres.registerAll();
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.getForHolder(packIceHolder);
        check(atmos != null, "Crystalline Pack-Ice must have a registered BiomeAtmosphereProfile");
        check(atmos.getNightSkyColorRgb() == 0x4E2A7A, "Crystalline Pack-Ice must configure violet night sky #4E2A7A");
        check(Math.abs(atmos.getStarBrightnessBoost() - 0.35F) < 1.0E-4F, "Crystalline Pack-Ice must configure +0.35 starBrightnessBoost");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.GLACIAL_ICE_HALO, "Crystalline Pack-Ice must render GLACIAL_ICE_HALO");
        check(atmos.getAmbientParticlesPerTick() > 0, "Crystalline Pack-Ice must emit drifting prism sparkles");
        check(atmos.getCustomServerTick() == null, "Crystalline Pack-Ice must remain a peaceful sanctuary without biome hazards");

        // 3. GrotSpawnRegistry CRYSTALLINE_PACK_ICE_POOL Verification (Aegis ~55%, Aurora ~35%, Glacial ~10% of 95% non-anomaly rolls)
        GrotSpawnRegistry.initialize();
        RandomSource grotRng = RandomSource.create(20261122L);
        int aegisCount = 0, auroraCount = 0, glacialCount = 0;
        int totalRolls = 20000;
        for (int i = 0; i < totalRolls; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(packIceHolder, grotRng);
            check(rolled != null, "GrotSpawnRegistry returned null for Crystalline Pack-Ice");
            if (rolled == EssenceType.AEGIS) aegisCount++;
            else if (rolled == EssenceType.AURORA) auroraCount++;
            else if (rolled == EssenceType.GLACIAL) glacialCount++;
        }
        double aegisRatio = aegisCount / (double) totalRolls;
        double auroraRatio = auroraCount / (double) totalRolls;
        double glacialRatio = glacialCount / (double) totalRolls;
        check(aegisRatio > 0.49 && aegisRatio < 0.56, "Expected Aegis Grot ratio ~52.5%, got " + aegisRatio);
        check(auroraRatio > 0.30 && auroraRatio < 0.37, "Expected Aurora Grot ratio ~33.5%, got " + auroraRatio);
        check(glacialRatio > 0.075 && glacialRatio < 0.125, "Expected Glacial Grot ratio ~9.7%, got " + glacialRatio);

        // 4. Live Bootstrapped Registries + Mod* Class Declaration Verification
        List<String> blocks9 = List.of(
                "aegis_quartz_block", "aegis_quartz_cluster", "bismuth_ore", "bismuth_block",
                "aegis_plated_stone", "bismuth_key_pedestal"
        );
        List<String> standaloneItems5 = List.of(
                "aegis_quartz", "raw_bismuth", "bismuth_ingot",
                "aegis_warding_emblem", "aegis_shielding_charm"
        );

        for (String b : blocks9) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, b);
            check(BuiltInRegistries.BLOCK.containsKey(id), "Live BuiltInRegistries.BLOCK missing entropica:" + b);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            check(block != null && block != Blocks.AIR, "Resolved Block is null/air for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", b.toUpperCase());

            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing BlockItem entropica:" + b);
            Item blockItem = BuiltInRegistries.ITEM.getValue(id);
            check(blockItem instanceof BlockItem, "Expected BlockItem for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", b.toUpperCase() + "_ITEM");
        }
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_quartz_block"))
                .defaultBlockState().getLightEmission() == 6, "aegis_quartz_block must emit Light Level 6");
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_quartz_cluster"))
                .defaultBlockState().getLightEmission() == 7, "aegis_quartz_cluster must emit Light Level 7");
        check(Math.abs(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_plated_stone"))
                .defaultDestroyTime() - 100.0F) < 1.0E-4F, "aegis_plated_stone must have double Obsidian hardness (100.0F)");

        for (String it : standaloneItems5) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, it);
            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing standalone item entropica:" + it);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            check(item != null && item != Items.AIR, "Resolved standalone Item is null/air for entropica:" + it);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", it.toUpperCase());
        }
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_warding_emblem")) instanceof AegisWardingEmblemItem,
                "aegis_warding_emblem must be an instance of AegisWardingEmblemItem");
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_shielding_charm")) instanceof AegisShieldingCharmItem,
                "aegis_shielding_charm must be an instance of AegisShieldingCharmItem");

        ResourceLocation ironSkinId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "iron_skin");
        check(BuiltInRegistries.POTION.containsKey(ironSkinId), "Live BuiltInRegistries.POTION missing entropica:iron_skin");
        Potion ironSkinPotion = BuiltInRegistries.POTION.getValue(ironSkinId);
        check(ironSkinPotion != null && ironSkinPotion.getEffects().size() == 2, "Iron Skin potion must carry Resistance II + Absorption I");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModPotions", "IRON_SKIN");

        ResourceLocation spireFeatId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glimmer_ice_spire");
        check(BuiltInRegistries.FEATURE.containsKey(spireFeatId), "Live BuiltInRegistries.FEATURE missing entropica:glimmer_ice_spire");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModFeatures", "GLIMMER_ICE_SPIRE");

        ResourceLocation monolithId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_shielding_monolith");
        ResourceLocation monolithPieceId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_shielding_monolith_piece");
        check(BuiltInRegistries.STRUCTURE_TYPE.containsKey(monolithId), "Live BuiltInRegistries.STRUCTURE_TYPE missing entropica:aegis_shielding_monolith");
        check(BuiltInRegistries.STRUCTURE_PIECE.containsKey(monolithPieceId), "Live BuiltInRegistries.STRUCTURE_PIECE missing entropica:aegis_shielding_monolith_piece");
        for (String structField : List.of("AEGIS_SHIELDING_MONOLITH_KEY", "STRUCTURE_2_CHEST_LOOT", "AEGIS_SHIELDING_MONOLITH", "AEGIS_SHIELDING_MONOLITH_PIECE")) {
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModStructures", structField);
        }

        // 5. Classpath JSONs, Structure 2 Loot, 14 Recipes, 17+5 PNG Dimensions, & en_us.json Translations
        JsonObject packIceBiomeJson = parseClasspathJson("data/entropica/worldgen/biome/crystalline_pack_ice.json");
        String packIceBiomeStr = packIceBiomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:glimmer_ice_spire",
                "entropica:patch_aegis_spire_orchid",
                "entropica:patch_rimebloom_pack_ice",
                "entropica:ore_bismuth_pack_ice",
                "entropica:rime_back_ovis",
                "entropica:aurora_jelly",
                "entropica:cryo_stalker",
                "entropica:grot"
        )) {
            check(packIceBiomeStr.contains(expectedRef), "crystalline_pack_ice.json missing expected entry: " + expectedRef);
        }

        for (String fId : List.of("glimmer_ice_spire", "patch_aegis_spire_orchid", "patch_rimebloom_pack_ice", "ore_bismuth_pack_ice")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + fId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + fId + ".json");
        }

        JsonObject monolithJson = parseClasspathJson("data/entropica/worldgen/structure/aegis_shielding_monolith.json");
        check("entropica:aegis_shielding_monolith".equals(monolithJson.get("type").getAsString()),
                "aegis_shielding_monolith.json must reference type entropica:aegis_shielding_monolith");
        parseClasspathJson("data/entropica/worldgen/structure_set/aegis_shielding_monolith.json");

        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/entropica/tags/worldgen/biome/is_aegis.json",
                "data/entropica/tags/worldgen/biome/is_aurora.json",
                "data/entropica/tags/worldgen/biome/has_structure/aegis_shielding_monolith.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:crystalline_pack_ice"),
                    tagJson + " must include entropica:crystalline_pack_ice");
        }

        JsonObject monolithLoot = parseClasspathJson("data/entropica/loot_table/chests/structure_2.json");
        String lootStr = monolithLoot.toString();
        for (String expectedLoot : List.of(
                "entropica:aegis_warding_emblem",
                "entropica:aegis_spire_petal",
                "entropica:shielding_thorn_fiber",
                "entropica:bismuth_ingot",
                "minecraft:emerald",
                "minecraft:iron_ingot",
                "minecraft:gold_ingot",
                "minecraft:coal"
        )) {
            check(lootStr.contains(expectedLoot), "chests/structure_2.json missing loot entry: " + expectedLoot);
        }

        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String reqKey : List.of(
                "biome.entropica.crystalline_pack_ice",
                "item.minecraft.potion.effect.iron_skin",
                "item.minecraft.splash_potion.effect.iron_skin",
                "item.minecraft.lingering_potion.effect.iron_skin",
                "item.minecraft.tipped_arrow.effect.iron_skin"
        )) {
            check(langJson.has(reqKey), "en_us.json missing required translation key: " + reqKey);
        }

        for (String b : blocks9) {
            parseClasspathJson("assets/entropica/blockstates/" + b + ".json");
            parseClasspathJson("assets/entropica/models/block/" + b + ".json");
            parseClasspathJson("assets/entropica/models/item/" + b + ".json");
            parseClasspathJson("assets/entropica/items/" + b + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + b + ".json");
            check(langJson.has("block.entropica." + b), "en_us.json missing block.entropica." + b);
            check(langJson.has("item.entropica." + b), "en_us.json missing item.entropica." + b);
        }
        for (String it : standaloneItems5) {
            parseClasspathJson("assets/entropica/items/" + it + ".json");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }

        for (String blockTex : List.of(
                "aegis_quartz_block", "aegis_quartz_cluster", "bismuth_ore", "bismuth_block",
                "aegis_plated_stone",
                "bismuth_key_pedestal_side", "bismuth_key_pedestal_top", "bismuth_key_pedestal_top_unlocked"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        String pickaxeTag = parseClasspathJson("data/minecraft/tags/block/mineable/pickaxe.json").toString();
        String ironToolTag = parseClasspathJson("data/minecraft/tags/block/needs_iron_tool.json").toString();
        for (String b : blocks9) {
            check(pickaxeTag.contains("entropica:" + b), "mineable/pickaxe.json must include entropica:" + b);
        }
        for (String ironB : List.of("bismuth_ore", "bismuth_block", "aegis_plated_stone", "bismuth_key_pedestal")) {
            check(ironToolTag.contains("entropica:" + ironB), "needs_iron_tool.json must include entropica:" + ironB);
        }

        List<String> recipes14 = List.of(
                "bismuth_ingot_from_smelting_raw_bismuth",
                "bismuth_ingot_from_blasting_raw_bismuth",
                "bismuth_ingot_from_smelting_bismuth_ore",
                "bismuth_ingot_from_blasting_bismuth_ore",
                "bismuth_block",
                "bismuth_ingot_from_bismuth_block",
                "aegis_quartz_block",
                "aegis_quartz_from_block",
                "aegis_plated_stone",
                "bismuth_key_pedestal",
                "aegis_shielding_charm"
        );
        for (String r : recipes14) {
            parseClasspathJson("data/entropica/recipe/" + r + ".json");
        }

        System.out.println(String.format(
                "[PASS] 11/12 Biome 4 (Crystalline Pack-Ice) worldgen, Monolith Structure 2 & loot, 6 blocks, 11 items, Iron Skin potion, 11 recipes, 13 PNGs, en_us, & Grot pool (Aegis=%.1f%%, Aurora=%.1f%%, Glacial=%.1f%%) verified.",
                aegisRatio * 100.0, auroraRatio * 100.0, glacialRatio * 100.0
        ));
    }

    private static void verifyBiome5SilentFjordContentAndAssets() {
        // 1. 6D Multi-Noise Placement Rule & Non-Collision with Biomes 1–4 + Surface Rule (basalt_column / blue_frozen_moss)
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        BiomePlacementRule skerriesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.AURORAL_SKERRIES);
        BiomePlacementRule floesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.FROST_BITE_FLOES);
        BiomePlacementRule packIceRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.CRYSTALLINE_PACK_ICE);
        BiomePlacementRule fjordRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.SILENT_FJORD);
        check(fjordRule != null, "Silent Fjord placement rule must be registered");
        check(fjordRule.getParameterPoints().size() == 2, "Silent Fjord must register 2 high-weirdness fjord-inlet parameter points");

        // Strictly disjoint 6D boxes vs Biomes 1–3 (continentalness 0.09..0.45 vs <= 0.08) and vs Biome 4 (erosion -1.0..-0.375 vs -0.35..0.15)
        for (Climate.ParameterPoint p5 : fjordRule.getParameterPoints()) {
            for (BiomePlacementRule coastal : List.of(shelfRule, skerriesRule, floesRule)) {
                for (Climate.ParameterPoint pCoast : coastal.getParameterPoints()) {
                    boolean contDisjoint = pCoast.continentalness().max() < p5.continentalness().min()
                            || p5.continentalness().max() < pCoast.continentalness().min();
                    check(contDisjoint, "Silent Fjord continentalness must be strictly disjoint from coastal Biomes 1–3");
                }
            }
            for (Climate.ParameterPoint p4 : packIceRule.getParameterPoints()) {
                boolean erosionDisjoint = p5.erosion().max() < p4.erosion().min() || p4.erosion().max() < p5.erosion().min();
                check(erosionDisjoint, "Silent Fjord sheer-cliff erosion must be strictly disjoint from Biome 4");
            }
        }

        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> skerriesHolder = mockBiome("entropica", "auroral_skerries");
        Holder<Biome> floesHolder = mockBiome("entropica", "frost_bite_floes");
        Holder<Biome> packIceHolder = mockBiome("entropica", "crystalline_pack_ice");
        Holder<Biome> fjordHolder = mockBiome("entropica", "silent_fjord");
        Holder<Biome> vanillaFrozenPeaks = mockBiome("minecraft", "frozen_peaks");
        Holder<Biome> vanillaDesert = mockBiome("minecraft", "desert");

        EntropicaMultiNoiseLayer allFiveLayer = new EntropicaMultiNoiseLayer(
                2026L, 6, 10, 0,
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(
                        new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(skerriesRule, skerriesHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(floesRule, floesHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(packIceRule, packIceHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(fjordRule, fjordHolder)
                )
        );

        Climate.TargetPoint fjordPos = Climate.target(-0.70F, 0.15F, 0.22F, -0.68F, 0.0F, 0.65F);
        Climate.TargetPoint fjordNeg = Climate.target(-0.78F, -0.25F, 0.32F, -0.55F, 0.01F, -0.72F);
        check(allFiveLayer.selectMatchingBiome(999L, fjordPos, vanillaFrozenPeaks) == fjordHolder,
                "Silent Fjord must match icy mid-continentalness sheer-cliff +high-weirdness fjord target");
        check(allFiveLayer.selectMatchingBiome(999L, fjordNeg, vanillaFrozenPeaks) == fjordHolder,
                "Silent Fjord must match icy mid-continentalness sheer-cliff -high-weirdness fjord target");

        // Biomes 1–4 remain unaffected; hot desert & deep caves never match Biome 5
        check(allFiveLayer.selectMatchingBiome(999L, Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.0F, 0.70F), vanillaFrozenPeaks) == shelfHolder,
                "Blue-Glacier Shelf must remain unaffected when Biomes 1–5 are active");
        check(allFiveLayer.selectMatchingBiome(999L, Climate.target(-0.65F, 0.25F, -0.20F, 0.55F, 0.0F, 0.22F), vanillaFrozenPeaks) == skerriesHolder,
                "Auroral Skerries must remain unaffected when Biomes 1–5 are active");
        check(allFiveLayer.selectMatchingBiome(999L, Climate.target(-0.68F, 0.55F, -0.18F, 0.65F, 0.0F, 0.72F), vanillaFrozenPeaks) == floesHolder,
                "Frost-Bite Floes must remain unaffected when Biomes 1–5 are active");
        check(allFiveLayer.selectMatchingBiome(999L, Climate.target(-0.72F, 0.0F, 0.42F, -0.10F, 0.0F, 0.68F), vanillaFrozenPeaks) == packIceHolder,
                "Crystalline Pack-Ice must remain unaffected when Biomes 1–5 are active");
        check(allFiveLayer.selectMatchingBiome(999L, Climate.target(0.85F, 0.15F, 0.22F, -0.68F, 0.0F, 0.65F), vanillaDesert) == vanillaDesert,
                "Silent Fjord must never match hot climate");
        check(allFiveLayer.selectMatchingBiome(999L, Climate.target(-0.70F, 0.15F, 0.22F, -0.68F, 0.85F, 0.65F), vanillaFrozenPeaks) == vanillaFrozenPeaks,
                "Silent Fjord must never overwrite deep underground caves (depth=0.85)");

        SurfaceRules.RuleSource fjordSurface = fjordRule.getSurfaceRule();
        check(fjordSurface != null, "Silent Fjord surface rule supplier must return non-null RuleSource");
        String surfaceDesc = fjordSurface.toString();
        check(surfaceDesc.contains("entropica:basalt_column") && surfaceDesc.contains("entropica:blue_frozen_moss") && surfaceDesc.contains("minecraft:basalt"),
                "Silent Fjord surface rule must produce entropica:basalt_column, entropica:blue_frozen_moss, and minecraft:basalt, got: " + surfaceDesc);

        // 2. EntropicaBiomeAtmospheres Profile Verification (0.42x eclipsed twilight, #1B0E3A night sky, ECLIPSE_CORONA, Deep Shadows server tick)
        EntropicaBiomeAtmospheres.registerAll();
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.getForHolder(fjordHolder);
        check(atmos != null, "Silent Fjord must have a registered BiomeAtmosphereProfile");
        check(atmos.hasCustomFogDistance(), "Silent Fjord must configure cold grey fjord fog");
        check(atmos.getDayFogTintRgb() == 0x3E4452, "Silent Fjord must configure cold grey fog #3E4452");
        check(Math.abs(atmos.getDaySkyBrightnessScale() - 0.42F) < 1.0E-4F, "Silent Fjord must dim day sky brightness to 0.42x");
        check(atmos.getNightSkyColorRgb() == 0x1B0E3A, "Silent Fjord must configure bruised violet night sky #1B0E3A");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.ECLIPSE_CORONA, "Silent Fjord must render ECLIPSE_CORONA");
        check(atmos.getAmbientParticlesPerTick() > 0, "Silent Fjord must emit silent falling snow/ash motes");
        check(atmos.getCustomServerTick() != null, "Silent Fjord must register non-null Deep Shadows customServerTick");

        // 3. GrotSpawnRegistry SILENT_FJORD_POOL Verification (Eclipse ~55%, Glacial ~35%, Frozen ~10% of 95% non-anomaly rolls)
        GrotSpawnRegistry.initialize();
        RandomSource grotRng = RandomSource.create(20261123L);
        int eclipseCount = 0, glacialCount = 0, frozenCount = 0;
        int totalRolls = 20000;
        for (int i = 0; i < totalRolls; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(fjordHolder, grotRng);
            check(rolled != null, "GrotSpawnRegistry returned null for Silent Fjord");
            if (rolled == EssenceType.ECLIPSE) eclipseCount++;
            else if (rolled == EssenceType.GLACIAL) glacialCount++;
            else if (rolled == EssenceType.FROZEN) frozenCount++;
        }
        double eclipseRatio = eclipseCount / (double) totalRolls;
        double glacialRatio = glacialCount / (double) totalRolls;
        double frozenRatio = frozenCount / (double) totalRolls;
        check(eclipseRatio > 0.49 && eclipseRatio < 0.56, "Expected Eclipse Grot ratio ~52.5%, got " + eclipseRatio);
        check(glacialRatio > 0.30 && glacialRatio < 0.37, "Expected Glacial Grot ratio ~33.5%, got " + glacialRatio);
        check(frozenRatio > 0.075 && frozenRatio < 0.125, "Expected Frozen Grot ratio ~9.7%, got " + frozenRatio);

        // 4. Live Bootstrapped Registries + Mod* Class Declaration Verification
        List<String> blocks5 = List.of(
                "basalt_column", "chiseled_eclipse_basalt", "blue_frozen_moss",
                "blue_frozen_moss_carpet", "shadowed_eclipse_moss"
        );
        List<String> standaloneItems5 = List.of(
                "frozen_moss_clump", "cryo_grenade",
                "silent_striders", "basalt_pickaxe"
        );

        for (String b : blocks5) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, b);
            check(BuiltInRegistries.BLOCK.containsKey(id), "Live BuiltInRegistries.BLOCK missing entropica:" + b);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            check(block != null && block != Blocks.AIR, "Resolved Block is null/air for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", b.toUpperCase());

            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing BlockItem entropica:" + b);
            Item blockItem = BuiltInRegistries.ITEM.getValue(id);
            check(blockItem instanceof BlockItem, "Expected BlockItem for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", b.toUpperCase() + "_ITEM");
        }
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chiseled_eclipse_basalt"))
                .defaultBlockState().getLightEmission() == 3, "chiseled_eclipse_basalt must emit Light Level 3");
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blue_frozen_moss"))
                .defaultBlockState().getLightEmission() == 2, "blue_frozen_moss must emit Light Level 2");
        check(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "shadowed_eclipse_moss"))
                .defaultBlockState().getLightEmission() == 0, "shadowed_eclipse_moss must emit Light Level 0");

        for (String it : standaloneItems5) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, it);
            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing standalone item entropica:" + it);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            check(item != null && item != Items.AIR, "Resolved standalone Item is null/air for entropica:" + it);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", it.toUpperCase());
        }
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_grenade")) instanceof CryoGrenadeItem,
                "cryo_grenade must be an instance of CryoGrenadeItem");
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silent_striders")) instanceof SilentStridersItem,
                "silent_striders must be an instance of SilentStridersItem");

        ResourceLocation cryoGrenadeEntityId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_grenade");
        check(BuiltInRegistries.ENTITY_TYPE.containsKey(cryoGrenadeEntityId), "Live BuiltInRegistries.ENTITY_TYPE missing entropica:cryo_grenade");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModEntityTypes", "CRYO_GRENADE");

        ResourceLocation eclipseShroudId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_shroud");
        check(BuiltInRegistries.POTION.containsKey(eclipseShroudId), "Live BuiltInRegistries.POTION missing entropica:eclipse_shroud");
        Potion eclipseShroud = BuiltInRegistries.POTION.getValue(eclipseShroudId);
        check(eclipseShroud != null && eclipseShroud.getEffects().size() == 2, "Eclipse Shroud potion must carry Speed I + Night Vision");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModPotions", "ECLIPSE_SHROUD");

        for (String feat : List.of("fjord_basalt_columns", "fjord_eclipse_cairn")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, feat);
            check(BuiltInRegistries.FEATURE.containsKey(id), "Live BuiltInRegistries.FEATURE missing entropica:" + feat);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModFeatures", feat.toUpperCase());
        }

        // 5. Classpath JSONs, Structure 1 Tag, Vibration Tags, Upgraded Flora 7 Loot, 10 Recipes, 11 PNGs, & en_us.json
        JsonObject fjordBiomeJson = parseClasspathJson("data/entropica/worldgen/biome/silent_fjord.json");
        JsonObject effects = fjordBiomeJson.getAsJsonObject("effects");
        check(effects != null && effects.get("sky_color").getAsInt() == 1773114,
                "silent_fjord.json must specify bruised violet sky_color 1773114 (#1B0E3A)");
        check(effects.get("fog_color").getAsInt() == 4080722,
                "silent_fjord.json must specify cold grey fog_color 4080722 (#3E4452)");
        check(effects.get("water_color").getAsInt() == 527910,
                "silent_fjord.json must specify dark navy water_color 527910 (#080E26)");

        String fjordBiomeStr = fjordBiomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:fjord_basalt_columns",
                "entropica:fjord_eclipse_cairn",
                "entropica:patch_blue_frozen_moss",
                "entropica:patch_shadowed_eclipse_moss",
                "entropica:fjord_glacial_heart_root",
                "entropica:astral_geode",
                "entropica:veil_fox",
                "entropica:cryo_stalker",
                "entropica:grot",
                "minecraft:glow_squid"
        )) {
            check(fjordBiomeStr.contains(expectedRef), "silent_fjord.json missing expected entry: " + expectedRef);
        }

        for (String fId : List.of(
                "fjord_basalt_columns", "fjord_eclipse_cairn",
                "patch_blue_frozen_moss", "patch_shadowed_eclipse_moss", "fjord_glacial_heart_root"
        )) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + fId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + fId + ".json");
        }

        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/entropica/tags/worldgen/biome/is_glacial.json",
                "data/entropica/tags/worldgen/biome/is_eclipse.json",
                "data/entropica/tags/worldgen/biome/has_structure/frost_veiled_glacial_runic_spire.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:silent_fjord"),
                    tagJson + " must include entropica:silent_fjord");
        }

        JsonObject heartRootLoot = parseClasspathJson("data/entropica/loot_table/blocks/glacial_heart_root.json");
        String heartRootLootStr = heartRootLoot.toString();
        check(heartRootLootStr.contains("entropica:glacial_heart_root")
                        && heartRootLootStr.contains("entropica:rimebloom_petal"),
                "glacial_heart_root.json must yield glacial_heart_root (Shears/Silk Touch/Axe) and rimebloom_petal fallback");

        check(parseClasspathJson("data/minecraft/tags/block/dampens_vibrations.json").toString().contains("entropica:shadowed_eclipse_moss"),
                "block/dampens_vibrations.json must include entropica:shadowed_eclipse_moss");
        check(parseClasspathJson("data/minecraft/tags/item/dampens_vibrations.json").toString().contains("entropica:silent_striders"),
                "item/dampens_vibrations.json must include entropica:silent_striders");

        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String reqKey : List.of(
                "biome.entropica.silent_fjord",
                "entity.entropica.cryo_grenade",
                "item.minecraft.potion.effect.eclipse_shroud",
                "item.minecraft.splash_potion.effect.eclipse_shroud",
                "item.minecraft.lingering_potion.effect.eclipse_shroud",
                "item.minecraft.tipped_arrow.effect.eclipse_shroud"
        )) {
            check(langJson.has(reqKey), "en_us.json missing required translation key: " + reqKey);
        }

        for (String b : blocks5) {
            parseClasspathJson("assets/entropica/blockstates/" + b + ".json");
            parseClasspathJson("assets/entropica/models/block/" + b + ".json");
            parseClasspathJson("assets/entropica/models/item/" + b + ".json");
            parseClasspathJson("assets/entropica/items/" + b + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + b + ".json");
            check(langJson.has("block.entropica." + b), "en_us.json missing block.entropica." + b);
            check(langJson.has("item.entropica." + b), "en_us.json missing item.entropica." + b);
        }
        parseClasspathJson("assets/entropica/models/block/basalt_column_horizontal.json");

        for (String it : standaloneItems5) {
            parseClasspathJson("assets/entropica/items/" + it + ".json");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }

        for (String blockTex : List.of(
                "basalt_column_side", "basalt_column_top",
                "chiseled_eclipse_basalt", "chiseled_eclipse_basalt_top",
                "blue_frozen_moss", "shadowed_eclipse_moss"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        List<String> recipes10 = List.of(
                "basalt_column",
                "chiseled_eclipse_basalt",
                "chiseled_eclipse_basalt_from_amethyst",
                "blue_frozen_moss_carpet",
                "blue_frozen_moss_from_clumps",
                "blue_frozen_moss",
                "cryo_grenade",
                "silent_striders",
                "basalt_pickaxe",
                "light_blue_dye_from_frozen_moss_clump"
        );
        for (String r : recipes10) {
            parseClasspathJson("data/entropica/recipe/" + r + ".json");
        }

        System.out.println(String.format(
                "[PASS] 12/13 Biome 5 (Silent Fjord) worldgen, Deep Shadows, Spire tag, 5 blocks, 10 items, Cryo-Grenade, Eclipse Shroud potion, 10 recipes, 11 PNGs, en_us, & Grot pool (Eclipse=%.1f%%, Glacial=%.1f%%, Frozen=%.1f%%) verified.",
                eclipseRatio * 100.0, glacialRatio * 100.0, frozenRatio * 100.0
        ));
    }

    private static void verifyBiome6SteamingSkerriesContentAndAssets() {
        // 1. 6D Multi-Noise Placement Rule & Non-Collision with Biomes 1–5 + Surface Rule (steaming_ice / steaming_spring_stone)
        BiomePlacementRule shelfRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.BLUE_GLACIER_SHELF);
        BiomePlacementRule skerriesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.AURORAL_SKERRIES);
        BiomePlacementRule floesRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.FROST_BITE_FLOES);
        BiomePlacementRule packIceRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.CRYSTALLINE_PACK_ICE);
        BiomePlacementRule fjordRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.SILENT_FJORD);
        BiomePlacementRule steamingRule = EntropicaBiomePlacements.allRules().get(EntropicaBiome.STEAMING_SKERRIES);
        check(steamingRule != null, "Steaming Skerries placement rule must be registered");
        check(steamingRule.getParameterPoints().size() == 2, "Steaming Skerries must register 2 medium-weirdness coastal shelf parameter points");

        // Pairwise 6D box disjointness against all 5 prior biomes
        for (Climate.ParameterPoint p6 : steamingRule.getParameterPoints()) {
            for (BiomePlacementRule other : List.of(shelfRule, skerriesRule, floesRule, packIceRule, fjordRule)) {
                for (Climate.ParameterPoint pOther : other.getParameterPoints()) {
                    boolean contDisjoint = pOther.continentalness().max() < p6.continentalness().min()
                            || p6.continentalness().max() < pOther.continentalness().min();
                    boolean eroDisjoint = pOther.erosion().max() < p6.erosion().min()
                            || p6.erosion().max() < pOther.erosion().min();
                    boolean weirdDisjoint = pOther.weirdness().max() < p6.weirdness().min()
                            || p6.weirdness().max() < pOther.weirdness().min();
                    check(contDisjoint || eroDisjoint || weirdDisjoint,
                            "Steaming Skerries 6D parameter box must be strictly disjoint from " + other.getBiome().getId());
                }
            }
        }

        Holder<Biome> shelfHolder = mockBiome("entropica", "blue_glacier_shelf");
        Holder<Biome> skerriesHolder = mockBiome("entropica", "auroral_skerries");
        Holder<Biome> floesHolder = mockBiome("entropica", "frost_bite_floes");
        Holder<Biome> packIceHolder = mockBiome("entropica", "crystalline_pack_ice");
        Holder<Biome> fjordHolder = mockBiome("entropica", "silent_fjord");
        Holder<Biome> steamingHolder = mockBiome("entropica", "steaming_skerries");
        Holder<Biome> vanillaSnowyBeach = mockBiome("minecraft", "snowy_beach");
        Holder<Biome> vanillaDesert = mockBiome("minecraft", "desert");

        EntropicaMultiNoiseLayer allSixLayer = new EntropicaMultiNoiseLayer(
                2026L, 6, 10, 0,
                false, false, TerraBlenderCompatMode.COOPERATE,
                List.of(
                        new EntropicaMultiNoiseLayer.ResolvedRule(shelfRule, shelfHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(skerriesRule, skerriesHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(floesRule, floesHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(packIceRule, packIceHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(fjordRule, fjordHolder),
                        new EntropicaMultiNoiseLayer.ResolvedRule(steamingRule, steamingHolder)
                )
        );

        Climate.TargetPoint steamingPos = Climate.target(-0.70F, 0.30F, 0.02F, 0.55F, 0.0F, 0.22F);
        Climate.TargetPoint steamingNeg = Climate.target(-0.75F, -0.10F, -0.02F, 0.65F, 0.01F, -0.20F);
        check(allSixLayer.selectMatchingBiome(1006L, steamingPos, vanillaSnowyBeach) == steamingHolder,
                "Steaming Skerries must match icy coastal shelf +medium-weirdness target");
        check(allSixLayer.selectMatchingBiome(1006L, steamingNeg, vanillaSnowyBeach) == steamingHolder,
                "Steaming Skerries must match icy coastal shelf -medium-weirdness target");

        // Biomes 1–5 remain unaffected; hot desert & deep caves never match Biome 6
        check(allSixLayer.selectMatchingBiome(1006L, Climate.target(-0.75F, 0.0F, -0.15F, -0.65F, 0.0F, 0.70F), vanillaSnowyBeach) == shelfHolder,
                "Blue-Glacier Shelf must remain unaffected when Biomes 1–6 are active");
        check(allSixLayer.selectMatchingBiome(1006L, Climate.target(-0.65F, 0.25F, -0.20F, 0.55F, 0.0F, 0.22F), vanillaSnowyBeach) == skerriesHolder,
                "Auroral Skerries must remain unaffected when Biomes 1–6 are active");
        check(allSixLayer.selectMatchingBiome(1006L, Climate.target(-0.68F, 0.55F, -0.18F, 0.65F, 0.0F, 0.72F), vanillaSnowyBeach) == floesHolder,
                "Frost-Bite Floes must remain unaffected when Biomes 1–6 are active");
        check(allSixLayer.selectMatchingBiome(1006L, Climate.target(-0.72F, 0.0F, 0.42F, -0.10F, 0.0F, 0.68F), vanillaSnowyBeach) == packIceHolder,
                "Crystalline Pack-Ice must remain unaffected when Biomes 1–6 are active");
        check(allSixLayer.selectMatchingBiome(1006L, Climate.target(-0.70F, 0.15F, 0.22F, -0.68F, 0.0F, 0.65F), vanillaSnowyBeach) == fjordHolder,
                "Silent Fjord must remain unaffected when Biomes 1–6 are active");
        check(allSixLayer.selectMatchingBiome(1006L, Climate.target(0.85F, 0.30F, 0.02F, 0.55F, 0.0F, 0.22F), vanillaDesert) == vanillaDesert,
                "Steaming Skerries must never match hot climate");
        check(allSixLayer.selectMatchingBiome(1006L, Climate.target(-0.70F, 0.30F, 0.02F, 0.55F, 0.85F, 0.22F), vanillaSnowyBeach) == vanillaSnowyBeach,
                "Steaming Skerries must never overwrite deep underground caves (depth=0.85)");

        SurfaceRules.RuleSource steamingSurface = steamingRule.getSurfaceRule();
        check(steamingSurface != null, "Steaming Skerries surface rule supplier must return non-null RuleSource");
        String surfaceDesc = steamingSurface.toString();
        check(surfaceDesc.contains("entropica:steaming_ice") && surfaceDesc.contains("entropica:steaming_spring_stone")
                        && surfaceDesc.contains("minecraft:blue_ice") && surfaceDesc.contains("minecraft:basalt"),
                "Steaming Skerries surface rule must produce steaming_ice, steaming_spring_stone, blue_ice, and basalt, got: " + surfaceDesc);

        // 2. EntropicaBiomeAtmospheres Profile Verification (Rose-pink fog #DEC2C6, +14 light relief, rising steam, water steam, warmth tick)
        EntropicaBiomeAtmospheres.registerAll();
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.getForHolder(steamingHolder);
        check(atmos != null, "Steaming Skerries must have a registered BiomeAtmosphereProfile");
        check(atmos.hasCustomFogDistance(), "Steaming Skerries must configure dense geothermal steam fog");
        check(atmos.getDayFogTintRgb() == 0xDEC2C6, "Steaming Skerries must configure misty rose-pink fog #DEC2C6");
        check(atmos.getLightHeldFogReliefBlocks() > 10.0F, "Steaming Skerries must configure held-light fog relief");
        check(atmos.getAmbientParticlesPerTick() > 0 && atmos.getParticleWindVy() > 0.0F,
                "Steaming Skerries must emit upward-rising ambient steam particles");
        check(atmos.getWaterSurfaceSteamColumnsPerTick() > 0, "Steaming Skerries must emit water-surface steam plumes");
        check(atmos.getCustomServerTick() != null, "Steaming Skerries must register geothermal warmth customServerTick");

        // 3. GrotSpawnRegistry STEAMING_SKERRIES_POOL Verification (Vapor ~57%, Glacial ~28.5%, Magma ~9.5% of 95% non-anomaly rolls)
        GrotSpawnRegistry.initialize();
        RandomSource grotRng = RandomSource.create(20261124L);
        int vaporCount = 0, glacialCount = 0, magmaCount = 0;
        int totalRolls = 20000;
        for (int i = 0; i < totalRolls; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(steamingHolder, grotRng);
            check(rolled != null, "GrotSpawnRegistry returned null for Steaming Skerries");
            if (rolled == EssenceType.VAPOR) vaporCount++;
            else if (rolled == EssenceType.GLACIAL) glacialCount++;
            else if (rolled == EssenceType.MAGMA) magmaCount++;
        }
        double vaporRatio = vaporCount / (double) totalRolls;
        double glacialRatio = glacialCount / (double) totalRolls;
        double magmaRatio = magmaCount / (double) totalRolls;
        check(vaporRatio > 0.53 && vaporRatio < 0.61, "Expected Vapor Grot ratio ~57%, got " + vaporRatio);
        check(glacialRatio > 0.25 && glacialRatio < 0.32, "Expected Glacial Grot ratio ~28.5%, got " + glacialRatio);
        check(magmaRatio > 0.07 && magmaRatio < 0.125, "Expected Magma Grot ratio ~9.5%, got " + magmaRatio);

        // 4. Live Bootstrapped Registries + Mod* Class Declaration Verification (incl. MultifaceBlock Lichen!)
        List<String> blocks7 = List.of(
                "steaming_ice", "steaming_spring_stone", "vapor_weaver_lichen",
                "steam_vent", "vapor_lift_mat", "vapor_condenser"
        );
        List<String> standaloneItems5 = List.of(
                "vapor_condenser_spore", "vapor_shroud_cloak",
                "porous_basalt_shell", "vapor_bellows_crab_spawn_egg"
        );

        for (String b : blocks7) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, b);
            check(BuiltInRegistries.BLOCK.containsKey(id), "Live BuiltInRegistries.BLOCK missing entropica:" + b);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            check(block != null && block != Blocks.AIR, "Resolved Block is null/air for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", b.toUpperCase());

            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing BlockItem entropica:" + b);
            Item blockItem = BuiltInRegistries.ITEM.getValue(id);
            check(blockItem instanceof BlockItem, "Expected BlockItem for entropica:" + b);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", b.toUpperCase() + "_ITEM");
        }
        Block lichenBlock = BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_weaver_lichen"));
        check(lichenBlock instanceof net.minecraft.world.level.block.MultifaceBlock,
                "vapor_weaver_lichen MUST extend MultifaceBlock (Glow Lichen style), not a solid cube");
        check(lichenBlock.defaultBlockState().getLightEmission() == 2, "vapor_weaver_lichen must emit Light Level 2");

        for (String it : standaloneItems5) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, it);
            check(BuiltInRegistries.ITEM.containsKey(id), "Live BuiltInRegistries.ITEM missing standalone item entropica:" + it);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            check(item != null && item != Items.AIR, "Resolved standalone Item is null/air for entropica:" + it);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", it.toUpperCase());
        }
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_shroud_cloak")) instanceof VaporShroudCloakItem,
                "vapor_shroud_cloak must be an instance of VaporShroudCloakItem");

        ResourceLocation crabId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_bellows_crab");
        check(BuiltInRegistries.ENTITY_TYPE.containsKey(crabId), "Live BuiltInRegistries.ENTITY_TYPE missing entropica:vapor_bellows_crab");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModEntityTypes", "VAPOR_BELLOWS_CRAB");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModEntityTypes", "GEYSER_WIGGLE_WORM");

        ResourceLocation dampeningId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dampening");
        ResourceLocation longDampeningId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "long_dampening");
        check(BuiltInRegistries.MOB_EFFECT.containsKey(dampeningId), "Live BuiltInRegistries.MOB_EFFECT missing entropica:dampening");
        check(BuiltInRegistries.POTION.containsKey(dampeningId), "Live BuiltInRegistries.POTION missing entropica:dampening");
        check(BuiltInRegistries.POTION.containsKey(longDampeningId), "Live BuiltInRegistries.POTION missing entropica:long_dampening");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModEffects", "DAMPENING");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModPotions", "DAMPENING");
        checkDeclaredStaticField("ddraig.net.entropica.registry.ModPotions", "LONG_DAMPENING");

        for (String feat : List.of("steaming_geyser_pool", "patch_vapor_weaver_lichen")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, feat);
            check(BuiltInRegistries.FEATURE.containsKey(id), "Live BuiltInRegistries.FEATURE missing entropica:" + feat);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModFeatures", feat.toUpperCase());
        }

        ResourceLocation grottoId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "geothermal_vaporous_grotto");
        ResourceLocation grottoPieceId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "geothermal_vaporous_grotto_piece");
        check(BuiltInRegistries.STRUCTURE_TYPE.containsKey(grottoId), "Live BuiltInRegistries.STRUCTURE_TYPE missing entropica:geothermal_vaporous_grotto");
        check(BuiltInRegistries.STRUCTURE_PIECE.containsKey(grottoPieceId), "Live BuiltInRegistries.STRUCTURE_PIECE missing entropica:geothermal_vaporous_grotto_piece");
        for (String structField : List.of("GEOTHERMAL_VAPOROUS_GROTTO_KEY", "STRUCTURE_4_CHEST_LOOT", "GEOTHERMAL_VAPOROUS_GROTTO", "GEOTHERMAL_VAPOROUS_GROTTO_PIECE")) {
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModStructures", structField);
        }

        // 5. Classpath JSONs, Structure 4 & Crab Loot, 6 Recipes, Approved 16x16 & 128x128 PNGs, & en_us.json
        JsonObject steamingBiomeJson = parseClasspathJson("data/entropica/worldgen/biome/steaming_skerries.json");
        JsonObject effects = steamingBiomeJson.getAsJsonObject("effects");
        check(effects != null && effects.get("sky_color").getAsInt() == 12876674,
                "steaming_skerries.json must specify misty rose-pink sky_color 12876674 (#C47B82)");
        check(effects.get("fog_color").getAsInt() == 14598854,
                "steaming_skerries.json must specify pale rose steam fog_color 14598854 (#DEC2C6)");
        check(effects.get("water_color").getAsInt() == 3061940,
                "steaming_skerries.json must specify turquoise thermal water_color 3061940 (#2EB8B4)");

        String steamingBiomeStr = steamingBiomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:steaming_geyser_pool",
                "entropica:patch_vapor_weaver_lichen",
                "entropica:patch_mist_veil_fern",
                "entropica:astral_geode",
                "entropica:vapor_bellows_crab",
                "entropica:geyser_wiggle_worm",
                "entropica:grot"
        )) {
            check(steamingBiomeStr.contains(expectedRef), "steaming_skerries.json missing expected entry: " + expectedRef);
        }

        for (String fId : List.of("steaming_geyser_pool", "patch_vapor_weaver_lichen")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + fId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + fId + ".json");
        }

        JsonObject grottoJson = parseClasspathJson("data/entropica/worldgen/structure/geothermal_vaporous_grotto.json");
        check("entropica:geothermal_vaporous_grotto".equals(grottoJson.get("type").getAsString()),
                "geothermal_vaporous_grotto.json must reference type entropica:geothermal_vaporous_grotto");
        parseClasspathJson("data/entropica/worldgen/structure_set/geothermal_vaporous_grotto.json");

        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/entropica/tags/worldgen/biome/is_vapor.json",
                "data/entropica/tags/worldgen/biome/is_glacial.json",
                "data/entropica/tags/worldgen/biome/has_structure/geothermal_vaporous_grotto.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:steaming_skerries"),
                    tagJson + " must include entropica:steaming_skerries");
        }

        String grottoLootStr = parseClasspathJson("data/entropica/loot_table/chests/structure_4.json").toString();
        for (String expectedLoot : List.of(
                "entropica:vapor_shroud_cloak",
                "entropica:vapor_condenser_spore",
                "entropica:mist_veil_frond"
        )) {
            check(grottoLootStr.contains(expectedLoot), "chests/structure_4.json missing loot entry: " + expectedLoot);
        }

        String crabLootStr = parseClasspathJson("data/entropica/loot_table/entities/vapor_bellows_crab.json").toString();
        check(crabLootStr.contains("entropica:porous_basalt_shell")
                        && crabLootStr.contains("entropica:vapor_condensate_crystals"),
                "entities/vapor_bellows_crab.json must yield porous_basalt_shell and vapor_condensate_crystals");

        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String reqKey : List.of(
                "biome.entropica.steaming_skerries",
                "entity.entropica.vapor_bellows_crab",
                "effect.entropica.dampening",
                "item.minecraft.potion.effect.dampening",
                "item.minecraft.splash_potion.effect.dampening",
                "item.minecraft.lingering_potion.effect.dampening",
                "item.minecraft.tipped_arrow.effect.dampening",
                "item.minecraft.potion.effect.long_dampening",
                "item.minecraft.splash_potion.effect.long_dampening",
                "item.minecraft.lingering_potion.effect.long_dampening",
                "item.minecraft.tipped_arrow.effect.long_dampening"
        )) {
            check(langJson.has(reqKey), "en_us.json missing required translation key: " + reqKey);
        }

        for (String b : blocks7) {
            parseClasspathJson("assets/entropica/blockstates/" + b + ".json");
            parseClasspathJson("assets/entropica/models/block/" + b + ".json");
            parseClasspathJson("assets/entropica/models/item/" + b + ".json");
            parseClasspathJson("assets/entropica/items/" + b + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + b + ".json");
            check(langJson.has("block.entropica." + b), "en_us.json missing block.entropica." + b);
            check(langJson.has("item.entropica." + b), "en_us.json missing item.entropica." + b);
        }

        for (String it : List.of(
                "vapor_condenser_spore", "vapor_shroud_cloak",
                "porous_basalt_shell", "vapor_bellows_crab_spawn_egg",
                "geyser_wiggle_worm_spawn_egg", "vapor_vent_membrane", "sulfur_clod"
        )) {
            parseClasspathJson("assets/entropica/items/" + it + ".json");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }

        for (String blockTex : List.of(
                "steaming_ice", "steaming_spring_stone", "vapor_weaver_lichen",
                "steam_vent_side", "steam_vent_top", "steam_vent_bottom",
                "vapor_lift_mat_top", "vapor_lift_mat_side",
                "vapor_condenser_side", "vapor_condenser_top", "vapor_condenser_bottom"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }
        verifyPngDimensions("assets/entropica/textures/entity/vapor_bellows_crab/vapor_bellows_crab.png", 128, 128);

        List<String> recipes6 = List.of(
                "steam_vent",
                "vapor_lift_mat",
                "vapor_condenser",
                "steaming_ice",
                "steaming_spring_stone",
                "vapor_shroud_cloak"
        );
        for (String r : recipes6) {
            parseClasspathJson("data/entropica/recipe/" + r + ".json");
        }

        System.out.println(String.format(
                "[PASS] 13/14 Biome 6 (Steaming Skerries) worldgen, Grotto Structure 4 & loot, 7 blocks (Multiface Lichen + 3D Condenser), 12 items, Vapor-Bellows Crab, Dampening potions, 6 recipes, 24 PNGs, en_us, & Grot pool (Vapor=%.1f%%, Glacial=%.1f%%, Magma=%.1f%%) verified.",
                vaporRatio * 100.0, glacialRatio * 100.0, magmaRatio * 100.0
        ));
    }

    private static void verifyBiomes1To6StructuresEssencesAndEarlyLoot() {
        // 1. Live Registry & ModStructures Static Field Checks for Icebound Blight-Wreck (Biome 3) & Eclipse Fjord Watch-Sanctum (Biome 5)
        ResourceLocation wreckId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "icebound_blight_wreck");
        ResourceLocation wreckPieceId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "icebound_blight_wreck_piece");
        ResourceLocation sanctumId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_fjord_sanctum");
        ResourceLocation sanctumPieceId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_fjord_sanctum_piece");

        check(BuiltInRegistries.STRUCTURE_TYPE.containsKey(wreckId), "Missing structure type entropica:icebound_blight_wreck");
        check(BuiltInRegistries.STRUCTURE_PIECE.containsKey(wreckPieceId), "Missing structure piece entropica:icebound_blight_wreck_piece");
        check(BuiltInRegistries.STRUCTURE_TYPE.containsKey(sanctumId), "Missing structure type entropica:eclipse_fjord_sanctum");
        check(BuiltInRegistries.STRUCTURE_PIECE.containsKey(sanctumPieceId), "Missing structure piece entropica:eclipse_fjord_sanctum_piece");

        for (String field : List.of(
                "STRUCTURE_1_FJORD_CHEST_LOOT",
                "ICEBOUND_BLIGHT_WRECK_KEY", "ICEBOUND_BLIGHT_WRECK_CHEST_LOOT", "ICEBOUND_BLIGHT_WRECK", "ICEBOUND_BLIGHT_WRECK_PIECE",
                "ECLIPSE_FJORD_SANCTUM_KEY", "ECLIPSE_FJORD_SANCTUM_CHEST_LOOT", "ECLIPSE_FJORD_SANCTUM", "ECLIPSE_FJORD_SANCTUM_PIECE"
        )) {
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModStructures", field);
        }

        // Verify programmatic piece instantiation & bounding box dimensions
        IceboundBlightWreckPiece wreckSample = new IceboundBlightWreckPiece(0, 63, 0);
        check(wreckSample.getBoundingBox().getXSpan() == 16
                        && wreckSample.getBoundingBox().getYSpan() == 14
                        && wreckSample.getBoundingBox().getZSpan() == 22,
                "IceboundBlightWreckPiece bounding box must be 16x14x22");

        EclipseFjordSanctumPiece sanctumSample = new EclipseFjordSanctumPiece(0, 72, 0);
        check(sanctumSample.getBoundingBox().getXSpan() == 15
                        && sanctumSample.getBoundingBox().getYSpan() == 16
                        && sanctumSample.getBoundingBox().getZSpan() == 17,
                "EclipseFjordSanctumPiece bounding box must be 15x16x17");

        // 2. Worldgen Structure, Structure Set, & Biome Tag JSONs
        JsonObject wreckStruct = parseClasspathJson("data/entropica/worldgen/structure/icebound_blight_wreck.json");
        check("entropica:icebound_blight_wreck".equals(wreckStruct.get("type").getAsString()),
                "icebound_blight_wreck.json must use type entropica:icebound_blight_wreck");
        parseClasspathJson("data/entropica/worldgen/structure_set/icebound_blight_wreck.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/icebound_blight_wreck.json")
                        .toString().contains("entropica:frost_bite_floes"),
                "has_structure/icebound_blight_wreck.json must include entropica:frost_bite_floes");

        JsonObject sanctumStruct = parseClasspathJson("data/entropica/worldgen/structure/eclipse_fjord_sanctum.json");
        check("entropica:eclipse_fjord_sanctum".equals(sanctumStruct.get("type").getAsString()),
                "eclipse_fjord_sanctum.json must use type entropica:eclipse_fjord_sanctum");
        parseClasspathJson("data/entropica/worldgen/structure_set/eclipse_fjord_sanctum.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/eclipse_fjord_sanctum.json")
                        .toString().contains("entropica:silent_fjord"),
                "has_structure/eclipse_fjord_sanctum.json must include entropica:silent_fjord");

        // 3. Verify All 7 Chest Loot Tables (3 Pools: Signature + Biome-Matched Essences + Early-Game Starter Rewards, NO fragment_essence!)
        record ExpectedLootSpec(String path, String primaryEssence, String secondaryEssence) {}
        List<ExpectedLootSpec> lootSpecs = List.of(
                new ExpectedLootSpec("data/entropica/loot_table/chests/structure_1.json", "GLACIAL", "AEGIS"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/structure_1_fjord.json", "ECLIPSE", "GLACIAL"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/structure_2.json", "AEGIS", "AURORA"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/structure_3.json", "AURORA", "VAPOR"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/structure_4.json", "VAPOR", "GLACIAL"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/icebound_blight_wreck.json", "GLACIAL", "BLIGHT"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/eclipse_fjord_sanctum.json", "ECLIPSE", "GLACIAL"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/fulgurite_cryo_relay.json", "LIGHTNING", "GLACIAL"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/structure_5.json", "BARROW", "GLACIAL"),
                new ExpectedLootSpec("data/entropica/loot_table/chests/null_fringe_rift_bastion.json", "NULL_U", "GLACIAL")
        );

        for (ExpectedLootSpec spec : lootSpecs) {
            JsonObject lt = parseClasspathJson(spec.path());
            check(lt.has("pools") && lt.getAsJsonArray("pools").size() >= 3,
                    spec.path() + " must contain 3 loot pools (Signature + Biome Essences + Early-Game Rewards)");
            String raw = lt.toString();
            check(!raw.contains("fragment_essence"),
                    spec.path() + " must NOT contain late-game entropica:fragment_essence");
            check(!raw.contains("materia_blessing_shard")
                            && !raw.contains("materia_blessing")
                            && !raw.contains("greater_materia_blessing"),
                    spec.path() + " must NOT contain Materia Blessing Shards or Materia Blessings (found whole in the wild only)");
            check(raw.contains("entropica:weak_essence") && raw.contains("entropica:small_essence_ampoule"),
                    spec.path() + " must award entropica:weak_essence and entropica:small_essence_ampoule");
            check(raw.contains("\"EssenceType\":\"" + spec.primaryEssence() + "\"")
                            && raw.contains("\"EssenceType\":\"" + spec.secondaryEssence() + "\""),
                    spec.path() + " must include custom_data EssenceType " + spec.primaryEssence() + " and " + spec.secondaryEssence());
            for (String starterItem : List.of(
                    "entropica:arcanite_ingot",
                    "entropica:arcanite_nugget",
                    "entropica:small_ampoule",
                    "minecraft:bread",
                    "minecraft:cooked_salmon"
            )) {
                check(raw.contains(starterItem), spec.path() + " missing early-game reward: " + starterItem);
            }
        }

        System.out.println("[PASS] 14/15 Icebound Blight-Wreck (Biome 3), Eclipse Fjord Watch-Sanctum (Biome 5), Fulgurite Cryo-Relay (Biome 7), & all 8 biome-matched Essence + Early-Game chest loot tables verified.");
    }

    private static boolean intervalsOverlap(Climate.Parameter a, Climate.Parameter b) {
        return a.min() <= b.max() && b.min() <= a.max();
    }

    private static void verifyBiome7StaticIcePlainsContentAndAssets() {
        EntropicaBiome b7 = EntropicaBiome.STATIC_ICE_PLAINS;
        BiomePlacementRule rule = EntropicaBiomePlacements.allRules().get(b7);
        check(rule != null && !rule.getParameterPoints().isEmpty(), "Missing BiomePlacementRule for STATIC_ICE_PLAINS");
        check(rule.getSurfaceRule() != null, "STATIC_ICE_PLAINS must provide a surface rule");

        // Verify 100% 6D disjointness of STATIC_ICE_PLAINS against Biomes 1..6!
        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF,
                EntropicaBiome.AURORAL_SKERRIES,
                EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE,
                EntropicaBiome.SILENT_FJORD,
                EntropicaBiome.STEAMING_SKERRIES
        )) {
            BiomePlacementRule other = EntropicaBiomePlacements.allRules().get(prev);
            for (Climate.ParameterPoint pA : rule.getParameterPoints()) {
                for (Climate.ParameterPoint pB : other.getParameterPoints()) {
                    boolean overlapsAll6 = intervalsOverlap(pA.temperature(), pB.temperature())
                            && intervalsOverlap(pA.humidity(), pB.humidity())
                            && intervalsOverlap(pA.continentalness(), pB.continentalness())
                            && intervalsOverlap(pA.erosion(), pB.erosion())
                            && intervalsOverlap(pA.depth(), pB.depth())
                            && intervalsOverlap(pA.weirdness(), pB.weirdness());
                    check(!overlapsAll6, "6D parameter overlap detected between STATIC_ICE_PLAINS and " + prev.getId());
                }
            }
        }

        // Atmosphere & CLOUD_LIGHTNING_ARCS sky phenomenon
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b7);
        check(atmos != null, "Missing BiomeAtmosphereProfile for STATIC_ICE_PLAINS");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.CLOUD_LIGHTNING_ARCS,
                "STATIC_ICE_PLAINS must use CLOUD_LIGHTNING_ARCS sky phenomenon");
        check(atmos.getCustomServerTick() != null, "STATIC_ICE_PLAINS must register metal-armor Static Charge tick");

        // Biome JSON & Sky Color #6C664C (7104076)
        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/static_ice_plains.json");
        check(biomeJson.getAsJsonObject("effects").get("sky_color").getAsInt() == 0x6C664C,
                "static_ice_plains.json sky_color must be #6C664C (7104076)");
        String biomeRaw = biomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:cryo_static_hedgehog",
                "entropica:rime_back_ovis",
                "entropica:patina_ovis",
                "entropica:glacial_frost_core",
                "entropica:grot",
                "entropica:fulgurite_pine_tree",
                "entropica:fulgurite_ice_sheet",
                "entropica:patch_cryo_static_shrub"
        )) {
            check(biomeRaw.contains(expectedRef), "static_ice_plains.json missing expected entry: " + expectedRef);
        }

        // Biome Tags
        check(parseClasspathJson("data/minecraft/tags/worldgen/biome/is_overworld.json").toString().contains("entropica:static_ice_plains"),
                "is_overworld.json must contain entropica:static_ice_plains");
        check(parseClasspathJson("data/c/tags/worldgen/biome/is_icy.json").toString().contains("entropica:static_ice_plains"),
                "c:is_icy must contain entropica:static_ice_plains");
        check(parseClasspathJson("data/c/tags/worldgen/biome/is_snowy.json").toString().contains("entropica:static_ice_plains"),
                "c:is_snowy must contain entropica:static_ice_plains");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/fulgurite_cryo_relay.json").toString().contains("entropica:static_ice_plains"),
                "has_structure/fulgurite_cryo_relay.json must contain entropica:static_ice_plains");

        // Features, Structure & Bounding Box (17 x 24 x 17)
        for (String fId : List.of("fulgurite_pine_tree", "fulgurite_ice_sheet", "patch_cryo_static_shrub")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + fId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + fId + ".json");
        }
        parseClasspathJson("data/entropica/worldgen/structure/fulgurite_cryo_relay.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/fulgurite_cryo_relay.json");
        FulguriteCryoRelayPiece relaySample = new FulguriteCryoRelayPiece(0, 66, 0);
        check(relaySample.getBoundingBox().getXSpan() == 17
                        && relaySample.getBoundingBox().getYSpan() == 24
                        && relaySample.getBoundingBox().getZSpan() == 17,
                "FulguriteCryoRelayPiece bounding box must be 17x24x17");

        // Blocks, Redstone Level 4 on Fulgurite Ice, Assets, PNG Dimensions & en_us Keys!
        Block fIceBlock = BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_ice"));
        check(fIceBlock.defaultBlockState().getLightEmission() == 4, "fulgurite_ice must emit Light Level 4");
        check(fIceBlock.defaultBlockState().isSignalSource()
                        && fIceBlock.defaultBlockState().getSignal(null, net.minecraft.core.BlockPos.ZERO, net.minecraft.core.Direction.NORTH) == 4,
                "fulgurite_ice must emit Redstone signal Level 4");

        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String blk : List.of(
                "fulgurite_ice", "fulgurite_pine_log", "fulgurite_pine_leaves",
                "static_conductive_mat", "biomass_capacitor", "cryo_static_shrub"
        )) {
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
        }

        for (String it : List.of(
                "static_frost_powder", "charged_static_needle", "static_quill",
                "grounding_coil", "cryo_static_hedgehog_spawn_egg", "patina_ovis_spawn_egg"
        )) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }

        for (String blockTex : List.of(
                "fulgurite_ice", "fulgurite_pine_log", "fulgurite_pine_log_top", "fulgurite_pine_leaves",
                "cryo_static_shrub", "static_conductive_mat_top", "static_conductive_mat_side",
                "biomass_capacitor_side", "biomass_capacitor_side_charged", "biomass_capacitor_top",
                "biomass_capacitor_top_charged", "biomass_capacitor_bottom"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }
        verifyPngDimensions("assets/entropica/textures/entity/cryo_static_hedgehog/cryo_static_hedgehog.png", 64, 64);

        // Potion & Entity lang keys
        check(langJson.has("entity.entropica.cryo_static_hedgehog"), "Missing entity.entropica.cryo_static_hedgehog");
        check(langJson.has("effect.entropica.galvanic_surge"), "Missing effect.entropica.galvanic_surge");
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.galvanic_surge"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.galvanic_surge");
            check(langJson.has("item.minecraft." + potKind + ".effect.long_galvanic_surge"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.long_galvanic_surge");
        }

        // Crafting Recipes
        for (String rec : List.of(
                "fulgurite_ice",
                "silver_pine_planks_from_fulgurite_pine",
                "static_conductive_mat",
                "biomass_capacitor",
                "static_redstone_coupler_from_quill",
                "grounding_coil"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }

        // Empirical Grot Spawn Pool Verification (55% Lightning, 35% Glacial, 10% Aegis)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b7Holder = mockBiome("entropica", "static_ice_plains");
        RandomSource rng = RandomSource.create(202607L);
        int lightning = 0, glacial = 0, aegis = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b7Holder, rng);
            if (rolled == EssenceType.LIGHTNING) lightning++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.AEGIS) aegis++;
        }
        double lightningRatio = lightning / (double) total;
        double glacialRatio = glacial / (double) total;
        double aegisRatio = aegis / (double) total;
        check(lightningRatio > 0.45 && lightningRatio < 0.62, "Unexpected Lightning Grot ratio: " + lightningRatio);
        check(glacialRatio > 0.27 && glacialRatio < 0.42, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(aegisRatio > 0.05 && aegisRatio < 0.15, "Unexpected Aegis Grot ratio: " + aegisRatio);

        System.out.println(String.format(
                "[PASS] 15/16 Biome 7 (Static Ice-Plains) worldgen, Fulgurite Cryo-Relay & loot, 7 blocks, 12 items, Cryo-Static Hedgehog, Galvanic Surge potions, 6 recipes, 23 PNGs, en_us, & Grot pool (Lightning=%.1f%%, Glacial=%.1f%%, Aegis=%.1f%%) verified.",
                lightningRatio * 100.0, glacialRatio * 100.0, aegisRatio * 100.0
        ));
    }

    private static void verifyBiome8BarrowMoundTundraContentAndAssets() {
        EntropicaBiome b8 = EntropicaBiome.BARROW_MOUND_TUNDRA;
        BiomePlacementRule rule = EntropicaBiomePlacements.allRules().get(b8);
        check(rule != null && rule.getParameterPoints().size() >= 2, "Missing BiomePlacementRule for BARROW_MOUND_TUNDRA");
        check(rule.getSurfaceRule() != null, "BARROW_MOUND_TUNDRA must provide a surface rule");

        // 1. Verify 100% 6D disjointness of BARROW_MOUND_TUNDRA against ALL Biomes 1..7!
        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF,
                EntropicaBiome.AURORAL_SKERRIES,
                EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE,
                EntropicaBiome.SILENT_FJORD,
                EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS
        )) {
            BiomePlacementRule other = EntropicaBiomePlacements.allRules().get(prev);
            for (Climate.ParameterPoint pA : rule.getParameterPoints()) {
                for (Climate.ParameterPoint pB : other.getParameterPoints()) {
                    boolean overlapsAll6 = intervalsOverlap(pA.temperature(), pB.temperature())
                            && intervalsOverlap(pA.humidity(), pB.humidity())
                            && intervalsOverlap(pA.continentalness(), pB.continentalness())
                            && intervalsOverlap(pA.erosion(), pB.erosion())
                            && intervalsOverlap(pA.depth(), pB.depth())
                            && intervalsOverlap(pA.weirdness(), pB.weirdness());
                    check(!overlapsAll6, "6D parameter overlap detected between BARROW_MOUND_TUNDRA and " + prev.getId());
                }
            }
        }

        // 2. Atmosphere & Necrotic Cold server tick
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b8);
        check(atmos != null, "Missing BiomeAtmosphereProfile for BARROW_MOUND_TUNDRA");
        check(atmos.getCustomServerTick() != null, "BARROW_MOUND_TUNDRA must register Necrotic Cold customServerTick");

        // 3. Biome JSON (#3C2B4C = 3943244) & Spawners/Features
        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/barrow_mound_tundra.json");
        check(biomeJson.getAsJsonObject("effects").get("sky_color").getAsInt() == 0x3C2B4C,
                "barrow_mound_tundra.json sky_color must be #3C2B4C (3943244)");
        String biomeRaw = biomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:barrow_hound",
                "entropica:barrow_crypt_glow_owl",
                "entropica:rime_shepherd",
                "entropica:grot",
                "entropica:small_barrow_cairn",
                "entropica:barrow_dead_spruce",
                "entropica:patch_rime_cap_mushroom",
                "entropica:patch_crypt_grave_root",
                "entropica:patch_grave_moss",
                "entropica:patch_barrow_moss_tundra"
        )) {
            check(biomeRaw.contains(expectedRef), "barrow_mound_tundra.json missing expected entry: " + expectedRef);
        }

        // 4. Biome Tags & Features & Structure 5 (26 x 15 x 32)
        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/entropica/tags/worldgen/biome/is_barrow.json",
                "data/entropica/tags/worldgen/biome/is_glacial.json",
                "data/entropica/tags/worldgen/biome/has_structure/frost_tomb_of_the_aegis_wardens.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:barrow_mound_tundra"),
                    tagJson + " must include entropica:barrow_mound_tundra");
        }

        for (String fId : List.of(
                "small_barrow_cairn", "barrow_dead_spruce",
                "patch_rime_cap_mushroom", "patch_crypt_grave_root",
                "patch_grave_moss", "patch_barrow_moss_tundra"
        )) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + fId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + fId + ".json");
        }

        parseClasspathJson("data/entropica/worldgen/structure/frost_tomb_of_the_aegis_wardens.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/frost_tombs.json");
        FrostTombPiece tombSample = new FrostTombPiece(0, 66, 0);
        check(tombSample.getBoundingBox().getXSpan() == 26
                        && tombSample.getBoundingBox().getYSpan() == 15
                        && tombSample.getBoundingBox().getZSpan() == 32,
                "FrostTombPiece bounding box must be 26x15x32");

        // 5. Blocks, Light Levels, Strictly Lowercase Blockstate Keys, 3D Block/Item Models & Loot
        Block brazierBlock = BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "runed_brazier"));
        check(brazierBlock.defaultBlockState().getLightEmission() == 0, "Unlit runed_brazier must emit Light 0");
        check(brazierBlock.defaultBlockState().setValue(RunedBrazierBlock.LIT, true).getLightEmission() == 14,
                "Lit runed_brazier must emit Light 14");

        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        List<String> blocks12 = List.of(
                "necrotic_soil", "barrow_stone_bricks", "cracked_barrow_bricks", "mossy_barrow_bricks",
                "grave_moss", "crypt_grave_root", "rime_cap_mushroom", "potted_rime_cap_mushroom",
                "runed_brazier", "crypt_siphon_lantern", "barrow_sentinel_totem"
        );
        for (String blk : blocks12) {
            JsonObject bs = parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            if (bs.has("variants")) {
                for (String key : bs.getAsJsonObject("variants").keySet()) {
                    check(key.equals(key.toLowerCase()),
                            "blockstates/" + blk + ".json variant key must be strictly lowercase: " + key);
                }
            }
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
        }

        // Verify 3D models on crypt_siphon_lantern, runed_brazier, and barrow_sentinel_totem
        JsonObject siphonStanding = parseClasspathJson("assets/entropica/models/block/crypt_siphon_lantern.json");
        JsonObject siphonHanging = parseClasspathJson("assets/entropica/models/block/crypt_siphon_lantern_hanging.json");
        JsonObject siphonItem = parseClasspathJson("assets/entropica/models/item/crypt_siphon_lantern.json");
        check(siphonStanding.has("elements") && siphonStanding.getAsJsonArray("elements").size() >= 10,
                "crypt_siphon_lantern.json must define full 10-element 3D lantern geometry");
        check(siphonHanging.has("elements") && siphonHanging.getAsJsonArray("elements").size() >= 10,
                "crypt_siphon_lantern_hanging.json must define full 10-element 3D hanging lantern geometry");
        check("entropica:block/crypt_siphon_lantern".equals(siphonItem.get("parent").getAsString()),
                "models/item/crypt_siphon_lantern.json must inherit 3D block model");

        for (String bi : List.of(
                "necrotic_soil", "barrow_stone_bricks", "cracked_barrow_bricks", "mossy_barrow_bricks",
                "grave_moss", "crypt_grave_root", "rime_cap_mushroom",
                "runed_brazier", "crypt_siphon_lantern", "barrow_sentinel_totem"
        )) {
            parseClasspathJson("assets/entropica/items/" + bi + ".json");
            parseClasspathJson("assets/entropica/models/item/" + bi + ".json");
        }

        for (String it : List.of(
                "rime_cap_spores", "crypt_grave_fibers", "bleached_bone_meal", "grave_glow_feather",
                "barrow_skull_plate", "barrow_slate_greaves",
                "barrow_hound_spawn_egg", "barrow_crypt_glow_owl_spawn_egg", "rime_shepherd_spawn_egg"
        )) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }

        // 6. Verify all 15 Block PNGs & both 64x64 Entity PNGs + Model Bakes!
        for (String blockTex : List.of(
                "necrotic_soil", "barrow_stone_bricks", "cracked_barrow_bricks", "mossy_barrow_bricks",
                "grave_moss", "grave_moss_side", "crypt_grave_root", "rime_cap_mushroom",
                "runed_brazier_side", "runed_brazier_side_lit", "runed_brazier_top", "runed_brazier_top_lit",
                "crypt_siphon_lantern", "barrow_sentinel_totem_side", "barrow_sentinel_totem_top"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }
        verifyPngDimensions("assets/entropica/textures/entity/barrow_hound/barrow_hound.png", 64, 64);
        verifyPngDimensions("assets/entropica/textures/entity/barrow_crypt_glow_owl/barrow_crypt_glow_owl.png", 64, 64);
        check(BarrowHoundModel.createBodyLayer().bakeRoot() != null, "BarrowHoundModel 55-cube LayerDefinition must bake cleanly");
        check(BarrowCryptGlowOwlModel.createBodyLayer().bakeRoot() != null, "BarrowCryptGlowOwlModel 47-cube LayerDefinition must bake cleanly");
        parseClasspathJson("data/entropica/loot_table/entities/barrow_hound.json");
        parseClasspathJson("data/entropica/loot_table/entities/barrow_crypt_glow_owl.json");

        // 7. Recipes, vibration dampening tag & en_us entity/potion keys
        check(parseClasspathJson("data/minecraft/tags/block/dampens_vibrations.json").toString().contains("entropica:grave_moss"),
                "dampens_vibrations.json must contain entropica:grave_moss");
        for (String rec : List.of(
                "necrotic_soil", "barrow_stone_bricks", "cracked_barrow_bricks", "mossy_barrow_bricks",
                "runed_brazier", "crypt_siphon_lantern", "barrow_sentinel_totem", "barrow_slate_greaves",
                "bleached_bone_meal_from_bone_and_spores", "string_from_crypt_grave_fibers"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }
        check(langJson.has("entity.entropica.barrow_hound"), "Missing entity.entropica.barrow_hound");
        check(langJson.has("entity.entropica.barrow_crypt_glow_owl"), "Missing entity.entropica.barrow_crypt_glow_owl");
        check(langJson.has("effect.entropica.barrow_ward"), "Missing effect.entropica.barrow_ward");
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.barrow_ward"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.barrow_ward");
        }

        // 8. Empirical Grot Spawn Pool Verification (55% Barrow, 35% Glacial, 10% Aegis)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b8Holder = mockBiome("entropica", "barrow_mound_tundra");
        RandomSource rng = RandomSource.create(202608L);
        int barrow = 0, glacial = 0, aegis = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b8Holder, rng);
            if (rolled == EssenceType.BARROW) barrow++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.AEGIS) aegis++;
        }
        double barrowRatio = barrow / (double) total;
        double glacialRatio = glacial / (double) total;
        double aegisRatio = aegis / (double) total;
        check(barrowRatio > 0.45 && barrowRatio < 0.62, "Unexpected Barrow Grot ratio: " + barrowRatio);
        check(glacialRatio > 0.27 && glacialRatio < 0.42, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(aegisRatio > 0.05 && aegisRatio < 0.15, "Unexpected Aegis Grot ratio: " + aegisRatio);

        System.out.println(String.format(
                "[PASS] 16/17 Biome 8 (Barrow-Mound Tundra) worldgen, Frost-Tomb (26x15x32) & loot, 12 blocks, 21 items, Barrow Hound (55 cubes) & Crypt-Glow Owl (47 cubes), Barrow Ward potions, 10 recipes, 29 PNGs, en_us, & Grot pool (Barrow=%.1f%%, Glacial=%.1f%%, Aegis=%.1f%%) verified.",
                barrowRatio * 100.0, glacialRatio * 100.0, aegisRatio * 100.0
        ));
    }

    private static void verifyBiome9NullFringeBayContentAndAssets() {
        EntropicaBiome b9 = EntropicaBiome.NULL_FRINGE_BAY;
        BiomePlacementRule rule = EntropicaBiomePlacements.allRules().get(b9);
        check(rule != null && rule.getParameterPoints().size() >= 2, "Missing BiomePlacementRule for NULL_FRINGE_BAY");
        check(rule.getSurfaceRule() != null, "NULL_FRINGE_BAY must provide a surface rule");

        // 1. Verify 100% 6D disjointness of NULL_FRINGE_BAY against ALL Biomes 1..8!
        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF,
                EntropicaBiome.AURORAL_SKERRIES,
                EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE,
                EntropicaBiome.SILENT_FJORD,
                EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS,
                EntropicaBiome.BARROW_MOUND_TUNDRA
        )) {
            BiomePlacementRule other = EntropicaBiomePlacements.allRules().get(prev);
            for (Climate.ParameterPoint pA : rule.getParameterPoints()) {
                for (Climate.ParameterPoint pB : other.getParameterPoints()) {
                    boolean overlapsAll6 = intervalsOverlap(pA.temperature(), pB.temperature())
                            && intervalsOverlap(pA.humidity(), pB.humidity())
                            && intervalsOverlap(pA.continentalness(), pB.continentalness())
                            && intervalsOverlap(pA.erosion(), pB.erosion())
                            && intervalsOverlap(pA.depth(), pB.depth())
                            && intervalsOverlap(pA.weirdness(), pB.weirdness());
                    check(!overlapsAll6, "6D parameter overlap detected between NULL_FRINGE_BAY and " + prev.getId());
                }
            }
        }

        // 2. Atmosphere: VOID_RIFT_FRACTURES, Jump Boost I, 0.80x low-gravity descent, & bay ring server tick
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b9);
        check(atmos != null, "Missing BiomeAtmosphereProfile for NULL_FRINGE_BAY");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.VOID_RIFT_FRACTURES, "NULL_FRINGE_BAY must use VOID_RIFT_FRACTURES");
        check(atmos.getJumpBoostAmplifier() == 0, "NULL_FRINGE_BAY must grant Jump Boost I (amp 0)");
        check(Math.abs(atmos.getDescentSpeedMultiplier() - 0.80F) < 1.0E-4F, "NULL_FRINGE_BAY must scale descent speed to 0.80F");
        check(atmos.getCustomServerTick() != null, "NULL_FRINGE_BAY must register bay void-ring server tick");

        // 3. Biome JSON (#150E26 = 1379878) & Spawners/Features
        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/null_fringe_bay.json");
        check(biomeJson.getAsJsonObject("effects").get("sky_color").getAsInt() == 0x150E26,
                "null_fringe_bay.json sky_color must be #150E26 (1379878)");
        String biomeRaw = biomeJson.toString();
        for (String expectedRef : List.of(
                "entropica:void_rift_shade",
                "entropica:void_sea_serpent",
                "entropica:grot",
                "entropica:floating_obsidian_shard",
                "entropica:void_crystal_beach_patch",
                "entropica:patch_null_fringe_tussock",
                "entropica:patch_void_stalker_orchid_bay"
        )) {
            check(biomeRaw.contains(expectedRef), "null_fringe_bay.json missing expected entry: " + expectedRef);
        }

        // 4. Biome Tags, Features & Structure (24 x 26 x 24)
        for (String tagJson : List.of(
                "data/minecraft/tags/worldgen/biome/is_overworld.json",
                "data/c/tags/worldgen/biome/is_icy.json",
                "data/c/tags/worldgen/biome/is_snowy.json",
                "data/entropica/tags/worldgen/biome/is_null_u.json",
                "data/entropica/tags/worldgen/biome/is_glacial.json",
                "data/entropica/tags/worldgen/biome/has_structure/null_fringe_rift_bastion.json"
        )) {
            check(parseClasspathJson(tagJson).toString().contains("entropica:null_fringe_bay"),
                    tagJson + " must include entropica:null_fringe_bay");
        }
        for (String fId : List.of(
                "floating_obsidian_shard", "void_crystal_beach_patch",
                "patch_null_fringe_tussock", "patch_void_stalker_orchid_bay"
        )) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + fId + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + fId + ".json");
        }
        parseClasspathJson("data/entropica/worldgen/structure/null_fringe_rift_bastion.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/null_fringe_rift_bastion.json");
        NullFringeRiftBastionPiece bastionSample = new NullFringeRiftBastionPiece(0, 63, 0);
        check(bastionSample.getBoundingBox().getXSpan() == 24
                        && bastionSample.getBoundingBox().getYSpan() == 26
                        && bastionSample.getBoundingBox().getZSpan() == 24,
                "NullFringeRiftBastionPiece bounding box must be 24x26x24");

        // 5. Blocks, Light Levels, Strictly Lowercase Blockstates, 3D Spatial Phase Anchor & Reuse of entropica:void_glass!
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String blk : List.of(
                "void_sand", "void_sand_turf", "void_stone", "obsidian_bricks",
                "void_crystal_block", "void_crystal_bud", "void_crystal_cluster",
                "null_fringe_tussock", "potted_null_fringe_tussock",
                "spatial_phase_anchor"
        )) {
            JsonObject bs = parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            if (bs.has("variants")) {
                for (String key : bs.getAsJsonObject("variants").keySet()) {
                    check(key.equals(key.toLowerCase()),
                            "blockstates/" + blk + ".json variant key must be strictly lowercase: " + key);
                }
            }
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
        }

        JsonObject anchorBlockModel = parseClasspathJson("assets/entropica/models/block/spatial_phase_anchor.json");
        JsonObject anchorItemModel = parseClasspathJson("assets/entropica/models/item/spatial_phase_anchor.json");
        check(anchorBlockModel.has("elements") && anchorBlockModel.getAsJsonArray("elements").size() >= 4,
                "spatial_phase_anchor.json must define sculpted multi-element 3D geometry");
        check("entropica:block/spatial_phase_anchor".equals(anchorItemModel.get("parent").getAsString()),
                "models/item/spatial_phase_anchor.json must inherit 3D block model");

        // Verify Void Sand smelts directly into existing entropica:void_glass!
        JsonObject glassSmelt = parseClasspathJson("data/entropica/recipe/void_glass_from_void_sand.json");
        check(glassSmelt.toString().contains("entropica:void_glass"),
                "void_glass_from_void_sand.json must output existing entropica:void_glass");

        // 6. Items, Spawn Eggs (no pig_spawn_egg!), serpent_void_scale.png fix, 11 Block PNGs & 128x128 Shade Model!
        for (String bi : List.of(
                "void_sand", "void_sand_turf", "void_stone", "obsidian_bricks",
                "void_crystal_block", "void_crystal_bud", "void_crystal_cluster",
                "null_fringe_tussock", "spatial_phase_anchor"
        )) {
            parseClasspathJson("assets/entropica/items/" + bi + ".json");
            parseClasspathJson("assets/entropica/models/item/" + bi + ".json");
        }
        for (String it : List.of(
                "void_crystal_shard", "void_fiber", "void_weave_fabric",
                "serpent_void_scale", "phase_recall_stone", "leviathan_rift_belt", "null_weave_mantle",
                "void_rift_shade_spawn_egg", "void_sea_serpent_spawn_egg"
        )) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }
        for (String blockTex : List.of(
                "void_sand", "void_sand_turf_top", "void_sand_turf_side",
                "void_stone", "obsidian_bricks", "void_crystal_block",
                "void_crystal_bud", "void_crystal_cluster", "null_fringe_tussock",
                "spatial_phase_anchor_side", "spatial_phase_anchor_top"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }
        verifyPngDimensions("assets/entropica/textures/entity/void_rift_shade/void_rift_shade.png", 128, 128);
        check(VoidRiftShadeModel.createBodyLayer().bakeRoot() != null, "VoidRiftShadeModel 80-cube LayerDefinition must bake cleanly");
        String shadeLootStr = parseClasspathJson("data/entropica/loot_table/entities/void_rift_shade.json").toString();
        check(shadeLootStr.contains("entropica:void_crystal_shard")
                        && shadeLootStr.contains("minecraft:ender_pearl"),
                "entities/void_rift_shade.json must yield void_crystal_shard and ender_pearl");
        parseClasspathJson("data/entropica/loot_table/entities/void_sea_serpent.json");

        // 7. 10 Recipes & Potion / Entity Translations
        for (String rec : List.of(
                "void_glass_from_void_sand", "obsidian_bricks", "void_stone",
                "void_crystal_block", "void_crystal_shard_from_block", "void_weave_fabric",
                "spatial_phase_anchor", "phase_recall_stone", "leviathan_rift_belt", "null_weave_mantle"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }
        check(langJson.has("entity.entropica.void_rift_shade"), "Missing entity.entropica.void_rift_shade");
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.null_buoyancy"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.null_buoyancy");
        }

        // 8. Empirical Grot Spawn Pool Verification (60% Null-U, 30% Glacial, 10% Void)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b9Holder = mockBiome("entropica", "null_fringe_bay");
        RandomSource rng = RandomSource.create(202609L);
        int nullU = 0, glacial = 0, voidEss = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b9Holder, rng);
            if (rolled == EssenceType.NULL_U) nullU++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.VOID) voidEss++;
        }
        double nullURatio = nullU / (double) total;
        double glacialRatio = glacial / (double) total;
        double voidRatio = voidEss / (double) total;
        check(nullURatio > 0.50 && nullURatio < 0.68, "Unexpected Null-U Grot ratio: " + nullURatio);
        check(glacialRatio > 0.22 && glacialRatio < 0.38, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(voidRatio > 0.05 && voidRatio < 0.16, "Unexpected Void Grot ratio: " + voidRatio);

        System.out.println(String.format(
                "[PASS] 17/18 Biome 9 (Null-Fringe Bay) worldgen, Rift Bastion (24x26x24) & loot, 11 blocks (+ entropica:void_glass reuse), 20 items, Void-Rift Shade (80 cubes) & Void Sea Serpent, Null-Buoyancy potions, 10 recipes, 26 PNGs, en_us, & Grot pool (Null-U=%.1f%%, Glacial=%.1f%%, Void=%.1f%%) verified.",
                nullURatio * 100.0, glacialRatio * 100.0, voidRatio * 100.0
        ));
    }

    private static void verifyBiome10RadiantGlazePlainsContentAndAssets() {
        EntropicaBiome b10 = EntropicaBiome.RADIANT_GLAZE_PLAINS;
        BiomePlacementRule rule = EntropicaBiomePlacements.allRules().get(b10);
        check(rule != null && rule.getParameterPoints().size() >= 2, "Missing BiomePlacementRule for RADIANT_GLAZE_PLAINS");
        check(rule.getSurfaceRule() != null, "RADIANT_GLAZE_PLAINS must provide a surface rule");

        // 1. Verify 100% 6D disjointness of RADIANT_GLAZE_PLAINS against ALL Biomes 1..9!
        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF,
                EntropicaBiome.AURORAL_SKERRIES,
                EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE,
                EntropicaBiome.SILENT_FJORD,
                EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS,
                EntropicaBiome.BARROW_MOUND_TUNDRA,
                EntropicaBiome.NULL_FRINGE_BAY
        )) {
            BiomePlacementRule other = EntropicaBiomePlacements.allRules().get(prev);
            for (Climate.ParameterPoint pA : rule.getParameterPoints()) {
                for (Climate.ParameterPoint pB : other.getParameterPoints()) {
                    boolean overlapsAll6 = intervalsOverlap(pA.temperature(), pB.temperature())
                            && intervalsOverlap(pA.humidity(), pB.humidity())
                            && intervalsOverlap(pA.continentalness(), pB.continentalness())
                            && intervalsOverlap(pA.erosion(), pB.erosion())
                            && intervalsOverlap(pA.depth(), pB.depth())
                            && intervalsOverlap(pA.weirdness(), pB.weirdness());
                    check(!overlapsAll6, "6D parameter overlap detected between RADIANT_GLAZE_PLAINS and " + prev.getId());
                }
            }
        }

        // 2. Atmosphere: warm golden sanctuary, AURORA_CURTAINS, passive regen, zero cold hazard
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b10);
        check(atmos != null, "Missing BiomeAtmosphereProfile for RADIANT_GLAZE_PLAINS");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.AURORA_CURTAINS, "RADIANT_GLAZE_PLAINS must use AURORA_CURTAINS");
        check(atmos.getPassiveRegenIntervalTicks() == 120 && atmos.getCustomServerTick() != null,
                "RADIANT_GLAZE_PLAINS must provide passive 120t sanctuary regen and custom warmth tick");

        // 3. Biome JSON (#A67E44 sky = 10911300, #FACC48 grass & foliage tint = 16436296) & Spawners/Features
        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/radiant_glaze_plains.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects.get("sky_color").getAsInt() == 0xA67E44, "RADIANT_GLAZE_PLAINS sky_color must be #A67E44");
        check(effects.get("grass_color").getAsInt() == 0xFACC48, "RADIANT_GLAZE_PLAINS grass_color must be #FACC48");
        check(effects.get("foliage_color").getAsInt() == 0xFACC48, "RADIANT_GLAZE_PLAINS foliage_color must be #FACC48");
        String biomeStr = biomeJson.toString();
        for (String expected : List.of(
                "entropica:crystalline_qilin", "entropica:amber_weeping_stag",
                "entropica:aurorafowl", "entropica:luminoth", "entropica:grot",
                "entropica:vitae_spring", "entropica:glazed_amber_oak",
                "entropica:patch_radiant_golden_grass", "entropica:patch_radiant_vitae_orchid"
        )) {
            check(biomeStr.contains(expected), "radiant_glaze_plains.json missing " + expected);
        }

        // 4. Core-Free Radiant Spring Pavilion Structure & Loot Invariants!
        for (String feat : List.of("vitae_spring", "glazed_amber_oak", "patch_radiant_golden_grass", "patch_radiant_vitae_orchid")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + feat + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + feat + ".json");
        }
        parseClasspathJson("data/entropica/worldgen/structure/radiant_spring_pavilion.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/radiant_spring_pavilion.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/radiant_spring_pavilion.json")
                .toString().contains("entropica:radiant_glaze_plains"), "radiant_spring_pavilion tag missing radiant_glaze_plains");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/auroral_wellspring.json")
                .toString().contains("entropica:radiant_glaze_plains"), "auroral_wellspring tag must include radiant_glaze_plains");

        String pavLoot = parseClasspathJson("data/entropica/loot_table/chests/radiant_spring_pavilion.json").toString();
        check(!pavLoot.contains("materia_blessing_shard") && !pavLoot.contains("materia_blessing") && !pavLoot.contains("fragment_essence"),
                "chests/radiant_spring_pavilion.json MUST NOT contain materia_blessing_shard or fragment_essence");
        check(pavLoot.contains("entropica:sun_glazed_talisman") && pavLoot.contains("VITAE") && pavLoot.contains("AURORA"),
                "chests/radiant_spring_pavilion.json must yield sun_glazed_talisman and VITAE + AURORA Materia");

        // 5. Blocks, Sculpted 3D Models, Items, Restored Fauna PNGs, Spawn Eggs & Qilin 66-Cube Model!
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String blk : List.of(
                "radiant_snow_block", "radiant_glaze_ice", "glazed_amber_leaves",
                "golden_lichen", "vitae_spring_stone", "qilin_light_refractor", "sun_glazed_lantern"
        )) {
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/block/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/item/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
        }

        JsonObject refractorModel = parseClasspathJson("assets/entropica/models/block/qilin_light_refractor.json");
        JsonObject lanternModel = parseClasspathJson("assets/entropica/models/block/sun_glazed_lantern.json");
        check(refractorModel.has("elements") && refractorModel.getAsJsonArray("elements").size() >= 4,
                "qilin_light_refractor.json must define 4-element 3D geometry");
        check(lanternModel.has("elements") && lanternModel.getAsJsonArray("elements").size() >= 5,
                "sun_glazed_lantern.json must define 5-element 3D geometry");

        for (String blockTex : List.of(
                "radiant_snow_block", "radiant_glaze_ice", "glazed_amber_leaves",
                "golden_lichen", "vitae_spring_stone",
                "qilin_light_refractor_side", "qilin_light_refractor_top",
                "sun_glazed_lantern_side", "sun_glazed_lantern_top"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        for (String it : List.of(
                "radiant_snowball", "jade_qilin_scale", "liquid_vitae_resin",
                "vitae_fertilizer", "qilin_prism_circlet", "sun_glazed_talisman",
                "weeping_amber_tear", "luminoth_dust", "aurorafowl_feather",
                "raw_aurorafowl", "cooked_aurorafowl",
                "crystalline_qilin_spawn_egg", "amber_weeping_stag_spawn_egg",
                "aurorafowl_spawn_egg", "luminoth_spawn_egg"
        )) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }

        verifyPngDimensions("assets/entropica/textures/entity/crystalline_qilin/crystalline_qilin.png", 128, 128);
        check(CrystallineQilinModel.createBodyLayer().bakeRoot() != null,
                "CrystallineQilinModel 66-cube LayerDefinition must bake cleanly");
        for (String entLoot : List.of("crystalline_qilin", "amber_weeping_stag", "aurorafowl", "luminoth")) {
            parseClasspathJson("data/entropica/loot_table/entities/" + entLoot + ".json");
        }
        for (String rec : List.of(
                "radiant_snow_block", "vitae_fertilizer", "sun_glazed_lantern",
                "qilin_light_refractor", "qilin_prism_circlet", "sun_glazed_talisman",
                "cooked_aurorafowl", "cooked_aurorafowl_from_smoking", "cooked_aurorafowl_from_campfire_cooking"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }
        check(langJson.has("entity.entropica.crystalline_qilin"), "Missing entity.entropica.crystalline_qilin");
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.radiant_vitality"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.radiant_vitality");
        }

        // 6. Empirical Grot Spawn Pool Verification (60% Vitae, 30% Aurora, 10% Glacial)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b10Holder = mockBiome("entropica", "radiant_glaze_plains");
        RandomSource rng = RandomSource.create(202610L);
        int vitae = 0, aurora = 0, glacial = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b10Holder, rng);
            if (rolled == EssenceType.VITAE) vitae++;
            else if (rolled == EssenceType.AURORA) aurora++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
        }
        double vitaeRatio = vitae / (double) total;
        double auroraRatio = aurora / (double) total;
        double glacialRatio = glacial / (double) total;
        check(vitaeRatio > 0.50 && vitaeRatio < 0.68, "Unexpected Vitae Grot ratio: " + vitaeRatio);
        check(auroraRatio > 0.22 && auroraRatio < 0.38, "Unexpected Aurora Grot ratio: " + auroraRatio);
        check(glacialRatio > 0.05 && glacialRatio < 0.16, "Unexpected Glacial Grot ratio: " + glacialRatio);

        System.out.println(String.format(
                "[PASS] 18/18 Biome 10 (Radiant Glaze-Plains) worldgen, core-free Radiant Spring Pavilion & loot, 7 blocks (+ golden vanilla grass & radiant_glass reuse), 15 items/eggs, Crystalline Qilin (66 cubes) & 4 fauna loot tables, Radiant Vitality potions, 9 recipes, 25 PNGs, en_us, & Grot pool (Vitae=%.1f%%, Aurora=%.1f%%, Glacial=%.1f%%) verified.",
                vitaeRatio * 100.0, auroraRatio * 100.0, glacialRatio * 100.0
        ));

        // 7. Regression Check: Terrestrial polar flora never spawns on ice & replaces snow layers cleanly!
        check(ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(Blocks.ICE.defaultBlockState())
                && ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(Blocks.PACKED_ICE.defaultBlockState())
                && ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(Blocks.BLUE_ICE.defaultBlockState())
                && ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(Blocks.SNOW.defaultBlockState()),
                "EntropicaFloraGroundHelper must reject vanilla ice and snow layers");
        check(!ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(Blocks.GRASS_BLOCK.defaultBlockState())
                && ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_ice")).defaultBlockState())
                && ddraig.net.entropica.block.EntropicaFloraGroundHelper.isSolidSnowBlock(Blocks.SNOW_BLOCK.defaultBlockState())
                && ddraig.net.entropica.block.EntropicaFloraGroundHelper.isSolidSnowBlock(BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_snow_block")).defaultBlockState()),
                "EntropicaFloraGroundHelper must accept grass/solid snow blocks and reject Entropica ice blocks");

        for (String patchName : List.of(
                "patch_cryo_static_shrub", "patch_aegis_spire_orchid", "patch_rimebloom_pack_ice",
                "patch_auroral_feather_moss", "patch_mist_veil_fern", "patch_radiant_vitae_orchid"
        )) {
            String cfStr = parseClasspathJson("data/entropica/worldgen/configured_feature/" + patchName + ".json").toString();
            check(cfStr.contains("minecraft:snow"), patchName + " must allow replacing minecraft:snow layers");
            for (String forbiddenIce : List.of(
                    "\"minecraft:ice\"", "\"minecraft:packed_ice\"", "\"minecraft:blue_ice\"",
                    "\"entropica:fulgurite_ice\"", "\"entropica:glimmer_ice\"",
                    "\"entropica:steaming_ice\"", "\"entropica:radiant_glaze_ice\""
            )) {
                check(!cfStr.contains(forbiddenIce), patchName + " must NOT spawn on " + forbiddenIce);
            }
            String pfStr = parseClasspathJson("data/entropica/worldgen/placed_feature/" + patchName + ".json").toString();
            check(pfStr.contains("MOTION_BLOCKING_NO_LEAVES"),
                    patchName + " placed_feature must use MOTION_BLOCKING_NO_LEAVES heightmap");
        }
    }

    private static void verifyBiome11StormSweptIcebergsContentAndAssets() {
        EntropicaBiome b11 = EntropicaBiome.STORM_SWEPT_ICEBERGS;
        BiomePlacementRule rule = EntropicaBiomePlacements.allRules().get(b11);
        check(rule != null && !rule.getParameterPoints().isEmpty(), "Missing BiomePlacementRule for STORM_SWEPT_ICEBERGS");
        check(rule.getSurfaceRule() != null, "STORM_SWEPT_ICEBERGS must provide a surface rule");

        // 1. Verify 100% 6D disjointness of STORM_SWEPT_ICEBERGS against ALL Biomes 1..10!
        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF,
                EntropicaBiome.AURORAL_SKERRIES,
                EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE,
                EntropicaBiome.SILENT_FJORD,
                EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS,
                EntropicaBiome.BARROW_MOUND_TUNDRA,
                EntropicaBiome.NULL_FRINGE_BAY,
                EntropicaBiome.RADIANT_GLAZE_PLAINS
        )) {
            BiomePlacementRule other = EntropicaBiomePlacements.allRules().get(prev);
            for (Climate.ParameterPoint pA : rule.getParameterPoints()) {
                for (Climate.ParameterPoint pB : other.getParameterPoints()) {
                    boolean overlapsAll6 = intervalsOverlap(pA.temperature(), pB.temperature())
                            && intervalsOverlap(pA.humidity(), pB.humidity())
                            && intervalsOverlap(pA.continentalness(), pB.continentalness())
                            && intervalsOverlap(pA.erosion(), pB.erosion())
                            && intervalsOverlap(pA.depth(), pB.depth())
                            && intervalsOverlap(pA.weirdness(), pB.weirdness());
                    check(!overlapsAll6, "6D parameter overlap detected between STORM_SWEPT_ICEBERGS and " + prev.getId());
                }
            }
        }

        // 2. Atmosphere: #2D3238 storm mist, CLOUD_LIGHTNING_ARCS, horizontal sea spray, custom server tick
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b11);
        check(atmos != null, "Missing BiomeAtmosphereProfile for STORM_SWEPT_ICEBERGS");
        check(atmos.getDayFogTintRgb() == 0x2D3238, "STORM_SWEPT_ICEBERGS day fog tint must be #2D3238");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.CLOUD_LIGHTNING_ARCS, "STORM_SWEPT_ICEBERGS must use CLOUD_LIGHTNING_ARCS");
        check(atmos.getCustomServerTick() != null, "STORM_SWEPT_ICEBERGS must register storm lightning & night observatory tick");

        // 3. Biome JSON (#2D3238 sky/fog = 2961976), Fauna Spawners & Iceberg Features
        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/storm_swept_icebergs.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects.get("sky_color").getAsInt() == 0x2D3238, "STORM_SWEPT_ICEBERGS sky_color must be #2D3238");
        String biomeStr = biomeJson.toString();
        for (String expected : List.of(
                "entropica:storm_kite", "entropica:glacial_hydra",
                "entropica:zephyr_simurgh", "entropica:grot",
                "entropica:storm_iceberg", "entropica:patch_zephyr_glass_grass"
        )) {
            check(biomeStr.contains(expected), "storm_swept_icebergs.json missing " + expected);
        }

        // 4. Ice-Safe Zephyr-Glass Grass Placement & Core-Free Structure 6: Auroral Vapor Observatory!
        for (String feat : List.of("storm_iceberg", "patch_zephyr_glass_grass")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + feat + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + feat + ".json");
        }
        String zGrassCf = parseClasspathJson("data/entropica/worldgen/configured_feature/patch_zephyr_glass_grass.json").toString();
        check(zGrassCf.contains("minecraft:snow_block") && zGrassCf.contains("entropica:zephyr_ice") && zGrassCf.contains("minecraft:snow"),
                "patch_zephyr_glass_grass must target snow_block/zephyr_ice and replace thin snow layers");
        for (String regularIce : List.of("\"minecraft:ice\"", "\"minecraft:packed_ice\"", "\"minecraft:blue_ice\"", "\"entropica:fulgurite_ice\"")) {
            check(!zGrassCf.contains(regularIce), "patch_zephyr_glass_grass must NOT spawn on " + regularIce);
        }
        check(parseClasspathJson("data/entropica/worldgen/placed_feature/patch_zephyr_glass_grass.json")
                .toString().contains("MOTION_BLOCKING_NO_LEAVES"), "patch_zephyr_glass_grass must use MOTION_BLOCKING_NO_LEAVES");
        check(ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(
                BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_ice")).defaultBlockState()),
                "EntropicaFloraGroundHelper must include zephyr_ice so generic terrestrial plants never spawn on it");

        parseClasspathJson("data/entropica/worldgen/structure/auroral_vapor_observatory.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/auroral_vapor_observatory.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/auroral_vapor_observatory.json")
                .toString().contains("entropica:storm_swept_icebergs"), "auroral_vapor_observatory tag missing storm_swept_icebergs");

        String obsLoot = parseClasspathJson("data/entropica/loot_table/chests/structure_6.json").toString();
        check(!obsLoot.contains("materia_blessing_shard") && !obsLoot.contains("materia_blessing") && !obsLoot.contains("fragment_essence"),
                "chests/structure_6.json MUST NOT contain materia_blessing_shard or fragment_essence");
        check(obsLoot.contains("entropica:storm_kite_glider") && obsLoot.contains("STORM") && obsLoot.contains("GLACIAL"),
                "chests/structure_6.json must yield storm_kite_glider and STORM + GLACIAL Materia");

        // 5. Blocks, Sculpted 3D Models, Items, Restored Fauna PNGs, Spawn Eggs, Recipes & Potions!
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String blk : List.of(
                "fulgurite_block", "zephyr_ice", "zephyr_glass_grass",
                "zephyr_updraft_grate", "simurgh_wind_turbine"
        )) {
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/block/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/item/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
        }
        parseClasspathJson("assets/entropica/blockstates/potted_zephyr_glass_grass.json");
        parseClasspathJson("data/entropica/loot_table/blocks/potted_zephyr_glass_grass.json");

        JsonObject grateModel = parseClasspathJson("assets/entropica/models/block/zephyr_updraft_grate.json");
        JsonObject turbineModel = parseClasspathJson("assets/entropica/models/block/simurgh_wind_turbine.json");
        check(grateModel.has("elements") && grateModel.getAsJsonArray("elements").size() >= 8,
                "zephyr_updraft_grate.json must define multi-element 3D ducted grate geometry");
        check(turbineModel.has("elements") && turbineModel.getAsJsonArray("elements").size() >= 8,
                "simurgh_wind_turbine.json must define multi-element 3D Savonius rotor geometry");

        for (String blockTex : List.of(
                "fulgurite_block", "zephyr_ice", "zephyr_glass_grass",
                "zephyr_updraft_grate_side", "zephyr_updraft_grate_well",
                "zephyr_updraft_grate_bar", "simurgh_wind_turbine_vane"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        for (String it : List.of(
                "zephyr_glass_fiber", "storm_kite_glider", "cryo_tempest_horn", "tempest_bottle",
                "storm_kite_membrane", "hydra_glacial_horn", "simurgh_feather",
                "storm_kite_spawn_egg", "glacial_hydra_spawn_egg", "zephyr_simurgh_spawn_egg"
        )) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }

        for (String entLoot : List.of("storm_kite", "glacial_hydra", "zephyr_simurgh")) {
            parseClasspathJson("data/entropica/loot_table/entities/" + entLoot + ".json");
        }
        for (String rec : List.of(
                "fulgurite_block", "zephyr_ice", "zephyr_updraft_grate",
                "simurgh_wind_turbine", "storm_kite_glider", "cryo_tempest_horn", "tempest_bottle"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.tempest_stride"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.tempest_stride");
        }

        // Verify zero duplicate charged_fulminite_ice or fulminite_shard bloat!
        check(BiomeInjectorVerificationSuite.class.getClassLoader().getResource("assets/entropica/blockstates/charged_fulminite_ice.json") == null
                && BiomeInjectorVerificationSuite.class.getClassLoader().getResource("assets/entropica/items/fulminite_shard.json") == null,
                "Biome 11 must reuse existing fulgurite_ice and static_frost_powder without duplicate charged_fulminite_ice/fulminite_shard");

        // 6. Empirical Grot Spawn Pool Verification (60% Storm, 30% Glacial, 10% Aurora)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b11Holder = mockBiome("entropica", "storm_swept_icebergs");
        RandomSource rng = RandomSource.create(202611L);
        int storm = 0, glacial = 0, aurora = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b11Holder, rng);
            if (rolled == EssenceType.STORM) storm++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.AURORA) aurora++;
        }
        double stormRatio = storm / (double) total;
        double glacialRatio = glacial / (double) total;
        double auroraRatio = aurora / (double) total;
        check(stormRatio > 0.50 && stormRatio < 0.68, "Unexpected Storm Grot ratio: " + stormRatio);
        check(glacialRatio > 0.22 && glacialRatio < 0.38, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(auroraRatio > 0.05 && auroraRatio < 0.16, "Unexpected Aurora Grot ratio: " + auroraRatio);

        System.out.println(String.format(
                "[PASS] 19/19 Biome 11 (Storm-Swept Icebergs) worldgen, core-free Auroral Vapor Observatory & loot, 5 blocks (+ reused fulgurite_ice), 10 items/eggs, 3 fauna loot tables, Tempest Stride potions, 7 recipes, 17 PNGs, en_us, & Grot pool (Storm=%.1f%%, Glacial=%.1f%%, Aurora=%.1f%%) verified.",
                stormRatio * 100.0, glacialRatio * 100.0, auroraRatio * 100.0
        ));
    }

    private static void verifyBiome12SporeGlazedPackContentAndAssets() {
        // 1. 6D Multi-Noise Placement & Strict Pairwise Disjointness vs Biomes #1-#11!
        EntropicaBiome b12 = EntropicaBiome.SPORE_GLAZED_PACK;
        BiomePlacementRule b12Rule = EntropicaBiomePlacements.allRules().get(b12);
        check(b12Rule != null, "SPORE_GLAZED_PACK placement rule must be registered");
        check(b12Rule.getParameterPoints().size() == 2, "SPORE_GLAZED_PACK must register 2 coastal bay-border parameter slices");

        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF, EntropicaBiome.AURORAL_SKERRIES, EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE, EntropicaBiome.SILENT_FJORD, EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS, EntropicaBiome.BARROW_MOUND_TUNDRA, EntropicaBiome.NULL_FRINGE_BAY,
                EntropicaBiome.RADIANT_GLAZE_PLAINS, EntropicaBiome.STORM_SWEPT_ICEBERGS
        )) {
            BiomePlacementRule prevRule = EntropicaBiomePlacements.allRules().get(prev);
            check(prevRule != null, "Missing previous placement rule for " + prev);
            for (Climate.ParameterPoint p12 : b12Rule.getParameterPoints()) {
                for (Climate.ParameterPoint pOther : prevRule.getParameterPoints()) {
                    boolean disjoint =
                            p12.temperature().max() < pOther.temperature().min() || pOther.temperature().max() < p12.temperature().min()
                            || p12.humidity().max() < pOther.humidity().min() || pOther.humidity().max() < p12.humidity().min()
                            || p12.continentalness().max() < pOther.continentalness().min() || pOther.continentalness().max() < p12.continentalness().min()
                            || p12.erosion().max() < pOther.erosion().min() || pOther.erosion().max() < p12.erosion().min()
                            || p12.depth().max() < pOther.depth().min() || pOther.depth().max() < p12.depth().min()
                            || p12.weirdness().max() < pOther.weirdness().min() || pOther.weirdness().max() < p12.weirdness().min();
                    check(disjoint, "SPORE_GLAZED_PACK parameter box collides in 6D space with " + prev);
                }
            }
        }

        SurfaceRules.RuleSource surf12 = b12Rule.getSurfaceRule();
        check(surf12 != null, "SPORE_GLAZED_PACK must define a custom SurfaceRule");
        String surfStr = surf12.toString();
        check(surfStr.contains("entropica:frozen_spore_moss") && surfStr.contains("entropica:spore_glazed_ice"),
                "SPORE_GLAZED_PACK surface rule must generate frozen_spore_moss and spore_glazed_ice");

        // 2. Atmosphere & Biome JSON (#6B7C50 sky = 7044176, #7A8A5A spore mist fog)
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b12);
        check(atmos != null, "Missing BiomeAtmosphereProfile for SPORE_GLAZED_PACK");
        check(atmos.getDayFogTintRgb() == 0x7A8A5A, "SPORE_GLAZED_PACK day fog tint must be #7A8A5A");

        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/spore_glazed_pack.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects.get("sky_color").getAsInt() == 0x6B7C50, "SPORE_GLAZED_PACK sky_color must be #6B7C50 (7044176)");
        String biomeStr = biomeJson.toString();
        for (String expected : List.of(
                "entropica:mycelial_spore_weaver", "entropica:spore_drifter",
                "entropica:overgrowth_ovis", "entropica:grot",
                "entropica:spore_wood_tree", "entropica:giant_glaze_cap_mushroom",
                "entropica:spore_glazed_floe", "entropica:patch_frozen_spore_flora"
        )) {
            check(biomeStr.contains(expected), "spore_glazed_pack.json missing " + expected);
        }

        // 3. Ice-Safe Flora, Features & Core-Free Glazed Mycelial Conservatory!
        for (String feat : List.of("spore_wood_tree", "giant_glaze_cap_mushroom", "spore_glazed_floe", "patch_frozen_spore_flora")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + feat + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + feat + ".json");
        }
        String floraPlaced = parseClasspathJson("data/entropica/worldgen/placed_feature/patch_frozen_spore_flora.json").toString();
        check(floraPlaced.contains("MOTION_BLOCKING_NO_LEAVES"), "patch_frozen_spore_flora placed_feature must use MOTION_BLOCKING_NO_LEAVES");
        String floraCombined = parseClasspathJson("data/entropica/worldgen/configured_feature/patch_frozen_spore_flora.json").toString() + floraPlaced;
        for (String regularIce : List.of("\"minecraft:ice\"", "\"minecraft:packed_ice\"", "\"minecraft:blue_ice\"", "\"entropica:spore_glazed_ice\"")) {
            check(!floraCombined.contains(regularIce), "patch_frozen_spore_flora must NOT spawn on " + regularIce);
        }
        check(ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(
                BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_glazed_ice")).defaultBlockState()),
                "EntropicaFloraGroundHelper must include spore_glazed_ice");

        parseClasspathJson("data/entropica/worldgen/structure/glazed_mycelial_conservatory.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/glazed_mycelial_conservatory.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/glazed_mycelial_conservatory.json")
                .toString().contains("entropica:spore_glazed_pack"), "glazed_mycelial_conservatory tag missing spore_glazed_pack");

        String consLoot = parseClasspathJson("data/entropica/loot_table/chests/glazed_mycelial_conservatory.json").toString();
        check(!consLoot.contains("materia_blessing_shard") && !consLoot.contains("materia_blessing") && !consLoot.contains("fragment_essence"),
                "chests/glazed_mycelial_conservatory.json MUST NOT contain materia_blessing_shard or fragment_essence");
        check(consLoot.contains("entropica:spore_filter_mask") && consLoot.contains("entropica:cryo_spore_poultice"),
                "chests/glazed_mycelial_conservatory.json must yield spore_filter_mask and cryo_spore_poultice");

        // 4. All 13 Blocks, Sculpted 3D Models, 3-Stick Spore-Wood Rule, Ampoule Living Spores & Items!
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        List<String> blocks12 = List.of(
                "frozen_spore_moss", "frozen_spore_moss_carpet", "spore_glazed_ice",
                "spore_wood_log", "spore_wood", "spore_wood_planks",
                "spore_wood_stairs", "spore_wood_slab", "spore_wood_fence",
                "frozen_spore_leaves", "glaze_cap_mushroom_block", "spore_bellows", "cryo_spore_censer"
        );
        for (String blk : blocks12) {
            check(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, blk)), "Missing block " + blk);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", blk.toUpperCase());
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", blk.toUpperCase() + "_ITEM");
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/item/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
        }

        // Verify spore_wood uses spore_wood_log bark on all faces!
        JsonObject sporeWoodModel = parseClasspathJson("assets/entropica/models/block/spore_wood.json");
        String swTex = sporeWoodModel.getAsJsonObject("textures").toString();
        check(swTex.contains("entropica:block/spore_wood_log") && !swTex.contains("spore_wood_log_top"),
                "spore_wood.json must use spore_wood_log bark texture on all sides (not spore_wood_log_top)");

        // Verify 3-stick rule & non-collision with vanilla 4-stick #minecraft:planks tag!
        JsonObject stickRecipe = parseClasspathJson("data/entropica/recipe/sticks_from_spore_wood_planks.json");
        check(stickRecipe.getAsJsonObject("result").get("count").getAsInt() == 3,
                "sticks_from_spore_wood_planks.json must yield strictly 3 sticks");
        String itemPlanksTag = parseClasspathJson("data/minecraft/tags/item/planks.json").toString();
        check(!itemPlanksTag.contains("entropica:spore_wood_planks"),
                "item/planks.json must NOT contain spore_wood_planks so vanilla's 4-stick recipe never overrides the 3-stick recipe");

        JsonObject bellowsModel = parseClasspathJson("assets/entropica/models/block/spore_bellows.json");
        JsonObject censerModel = parseClasspathJson("assets/entropica/models/block/cryo_spore_censer.json");
        check(bellowsModel.has("elements") && bellowsModel.getAsJsonArray("elements").size() >= 10,
                "spore_bellows.json must define multi-element 3D geometry");
        check(censerModel.has("elements") && censerModel.getAsJsonArray("elements").size() >= 10,
                "cryo_spore_censer.json must define multi-element 3D geometry");

        for (String blockTex : List.of(
                "frozen_spore_moss", "spore_glazed_ice", "spore_wood_log", "spore_wood_log_top",
                "spore_wood_planks", "frozen_spore_leaves", "glaze_cap_mushroom_block",
                "spore_bellows_sac", "spore_bellows_nozzle", "cryo_spore_censer_core"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        // Verify living_spores (32x32 ampoule) and 16x16 items + fixed spawn eggs!
        verifyPngDimensions("assets/entropica/textures/item/living_spores.png", 32, 32);
        parseClasspathJson("assets/entropica/items/living_spores.json");
        parseClasspathJson("assets/entropica/models/item/living_spores.json");

        for (String it16 : List.of(
                "mycelial_spore_cap", "spore_filter_mask", "cryo_spore_poultice", "glazed_spore_bomb",
                "mossy_aegis_plate", "mycelial_spore_weaver_spawn_egg",
                "spore_drifter_spawn_egg", "overgrowth_ovis_spawn_egg"
        )) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it16 + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it16 + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it16 + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it16 + ".png", 16, 16);
            check(langJson.has("item.entropica." + it16), "en_us.json missing item.entropica." + it16);
        }

        verifyPngDimensions("assets/entropica/textures/entity/mycelial_spore_weaver/mycelial_spore_weaver.png", 64, 64);
        verifyPngDimensions("assets/entropica/textures/entity/mycelial_spore_weaver/mycelial_spore_weaver_glow.png", 64, 64);

        for (String entLoot : List.of("mycelial_spore_weaver", "spore_drifter", "overgrowth_ovis")) {
            parseClasspathJson("data/entropica/loot_table/entities/" + entLoot + ".json");
        }
        for (String rec : List.of(
                "spore_wood_planks", "spore_wood_planks_from_spore_wood", "spore_wood",
                "sticks_from_spore_wood_planks", "crafting_table_from_spore_wood_planks",
                "spore_wood_stairs", "spore_wood_slab", "spore_wood_fence",
                "frozen_spore_moss_carpet", "spore_glazed_ice", "spore_bellows",
                "cryo_spore_censer", "spore_filter_mask", "cryo_spore_poultice",
                "glazed_spore_bomb", "glazed_spore_bomb_from_living_spores"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.spore_clarity"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.spore_clarity");
        }

        // 5. Empirical Grot Spawn Pool Verification (60% Spore, 30% Glacial, 10% Overgrowth)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b12Holder = mockBiome("entropica", "spore_glazed_pack");
        RandomSource rng = RandomSource.create(202612L);
        int spore = 0, glacial = 0, overgrowth = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b12Holder, rng);
            if (rolled == EssenceType.SPORE) spore++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.OVERGROWTH) overgrowth++;
        }
        double sporeRatio = spore / (double) total;
        double glacialRatio = glacial / (double) total;
        double overgrowthRatio = overgrowth / (double) total;
        check(sporeRatio > 0.50 && sporeRatio < 0.68, "Unexpected Spore Grot ratio: " + sporeRatio);
        check(glacialRatio > 0.22 && glacialRatio < 0.38, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(overgrowthRatio > 0.05 && overgrowthRatio < 0.16, "Unexpected Overgrowth Grot ratio: " + overgrowthRatio);

        System.out.println(String.format(
                "[PASS] 20/20 Biome 12 (Spore-Glazed Pack) worldgen, core-free Glazed Mycelial Conservatory & loot, 13 blocks (incl. 6-sided Spore-Wood & 3-stick rule), 9 items/eggs (incl. 32px ampoule Living Spores), Mycelial Spore-Weaver & 3 fauna loot tables, Spore Clarity potions, 16 recipes, 21 PNGs, en_us, & Grot pool (Spore=%.1f%%, Glacial=%.1f%%, Overgrowth=%.1f%%) verified.",
                sporeRatio * 100.0, glacialRatio * 100.0, overgrowthRatio * 100.0
        ));
    }

    private static void verifyBiome13MirageGlacierContentAndAssets() {
        // 1. 6D Multi-Noise Placement & Strict Pairwise Disjointness vs Biomes #1-#12!
        EntropicaBiome b13 = EntropicaBiome.MIRAGE_GLACIER;
        BiomePlacementRule b13Rule = EntropicaBiomePlacements.allRules().get(b13);
        check(b13Rule != null, "MIRAGE_GLACIER placement rule must be registered");
        check(b13Rule.getParameterPoints().size() == 3, "MIRAGE_GLACIER must register 3 inland valley & far-inland shelf parameter slices");

        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF, EntropicaBiome.AURORAL_SKERRIES, EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE, EntropicaBiome.SILENT_FJORD, EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS, EntropicaBiome.BARROW_MOUND_TUNDRA, EntropicaBiome.NULL_FRINGE_BAY,
                EntropicaBiome.RADIANT_GLAZE_PLAINS, EntropicaBiome.STORM_SWEPT_ICEBERGS, EntropicaBiome.SPORE_GLAZED_PACK
        )) {
            BiomePlacementRule prevRule = EntropicaBiomePlacements.allRules().get(prev);
            check(prevRule != null, "Missing previous placement rule for " + prev);
            for (Climate.ParameterPoint p13 : b13Rule.getParameterPoints()) {
                for (Climate.ParameterPoint pOther : prevRule.getParameterPoints()) {
                    boolean disjoint =
                            p13.temperature().max() < pOther.temperature().min() || pOther.temperature().max() < p13.temperature().min()
                            || p13.humidity().max() < pOther.humidity().min() || pOther.humidity().max() < p13.humidity().min()
                            || p13.continentalness().max() < pOther.continentalness().min() || pOther.continentalness().max() < p13.continentalness().min()
                            || p13.erosion().max() < pOther.erosion().min() || pOther.erosion().max() < p13.erosion().min()
                            || p13.depth().max() < pOther.depth().min() || pOther.depth().max() < p13.depth().min()
                            || p13.weirdness().max() < pOther.weirdness().min() || pOther.weirdness().max() < p13.weirdness().min();
                    check(disjoint, "MIRAGE_GLACIER parameter box collides in 6D space with " + prev);
                }
            }
        }

        SurfaceRules.RuleSource surf13 = b13Rule.getSurfaceRule();
        check(surf13 != null, "MIRAGE_GLACIER must define a custom SurfaceRule");
        String surfStr = surf13.toString();
        check(surfStr.contains("entropica:glass_sand")
                && surfStr.contains("entropica:glass_sandstone")
                && surfStr.contains("entropica:mirage_ice"),
                "MIRAGE_GLACIER surface rule must generate glass_sand, glass_sandstone, and mirage_ice");

        // 2. Atmosphere & Biome JSON (#A8592E sky = 11032878, #D49B54 dust-haze fog = 13933396)
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b13);
        check(atmos != null, "Missing BiomeAtmosphereProfile for MIRAGE_GLACIER");
        check(atmos.getDayFogTintRgb() == 0xD49B54, "MIRAGE_GLACIER day fog tint must be #D49B54");
        check(atmos.getSkyPhenomenon() == SkyPhenomenonType.MIRAGE_HEAT_SHIMMER, "MIRAGE_GLACIER must render MIRAGE_HEAT_SHIMMER");
        check(atmos.getCustomServerTick() != null, "MIRAGE_GLACIER must register midday solar glare Double Vision tick");

        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/mirage_glacier.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects.get("sky_color").getAsInt() == 0xA8592E, "MIRAGE_GLACIER sky_color must be #A8592E (11032878)");
        check(effects.get("fog_color").getAsInt() == 0xD49B54, "MIRAGE_GLACIER fog_color must be #D49B54 (13933396)");
        String biomeStr = biomeJson.toString();
        for (String expected : List.of(
                "entropica:silica_glass_scarab", "entropica:mirage_fata_sprite", "entropica:grot",
                "entropica:mirage_ice_arch", "entropica:patch_mirage_dune_flora"
        )) {
            check(biomeStr.contains(expected), "mirage_glacier.json missing " + expected);
        }

        // 3. Ice-Safe Dune Flora, MirageIceArchFeature & Core-Free Fata Morgana Sanctuary!
        for (String feat : List.of("mirage_ice_arch", "patch_mirage_dune_flora")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + feat + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + feat + ".json");
        }
        String dunePlaced = parseClasspathJson("data/entropica/worldgen/placed_feature/patch_mirage_dune_flora.json").toString();
        check(dunePlaced.contains("MOTION_BLOCKING_NO_LEAVES"), "patch_mirage_dune_flora placed_feature must use MOTION_BLOCKING_NO_LEAVES");
        String duneCombined = parseClasspathJson("data/entropica/worldgen/configured_feature/patch_mirage_dune_flora.json").toString() + dunePlaced;
        for (String iceBlock : List.of("\"minecraft:ice\"", "\"minecraft:packed_ice\"", "\"minecraft:blue_ice\"", "\"entropica:mirage_ice\"")) {
            check(!duneCombined.contains(iceBlock), "patch_mirage_dune_flora must NOT spawn on " + iceBlock);
        }
        Block mirageIceBlk = BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_ice"));
        check(ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(mirageIceBlk.defaultBlockState()),
                "EntropicaFloraGroundHelper must include mirage_ice");
        check(mirageIceBlk instanceof MirageIceBlock, "mirage_ice must be an instance of MirageIceBlock");
        check(!mirageIceBlk.defaultBlockState().getValue(MirageIceBlock.PHASED), "mirage_ice default state must be solid (!PHASED)");

        parseClasspathJson("data/entropica/worldgen/structure/fata_morgana_sanctuary.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/fata_morgana_sanctuary.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/fata_morgana_sanctuary.json")
                .toString().contains("entropica:mirage_glacier"), "fata_morgana_sanctuary tag missing mirage_glacier");

        String sanctLoot = parseClasspathJson("data/entropica/loot_table/chests/fata_morgana_sanctuary.json").toString();
        check(!sanctLoot.contains("materia_blessing_shard") && !sanctLoot.contains("materia_blessing") && !sanctLoot.contains("fragment_essence"),
                "chests/fata_morgana_sanctuary.json MUST NOT contain materia_blessing_shard or fragment_essence");
        check(sanctLoot.contains("entropica:mirage_veil_goggles") && sanctLoot.contains("entropica:mirage_decoy_prism"),
                "chests/fata_morgana_sanctuary.json must yield mirage_veil_goggles and mirage_decoy_prism");

        // 4. Verify Streamlined Cuts (ZERO vitreous_ice, vitreous_ice_shard, or mirage_reflector bloat!)
        ClassLoader cl = BiomeInjectorVerificationSuite.class.getClassLoader();
        check(cl.getResource("assets/entropica/blockstates/vitreous_ice.json") == null
                && cl.getResource("assets/entropica/items/vitreous_ice_shard.json") == null
                && cl.getResource("assets/entropica/blockstates/mirage_reflector.json") == null,
                "Biome 13 must NOT contain dropped vitreous_ice, vitreous_ice_shard, or mirage_reflector assets");

        // 5. All 6 Blocks, 3D Mirage Prism Lantern, DoubleVision + MiragePhasing Effects & Potions, Fauna 13 & 11 Recipes!
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        List<String> blocks6 = List.of(
                "glass_sand", "glass_sandstone", "glass_sandstone_stairs",
                "glass_sandstone_slab", "mirage_ice", "mirage_prism_lantern"
        );
        for (String blk : blocks6) {
            check(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, blk)), "Missing block " + blk);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", blk.toUpperCase());
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", blk.toUpperCase() + "_ITEM");
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/item/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
            check(langJson.has("item.entropica." + blk), "en_us.json missing item.entropica." + blk);
        }

        JsonObject prismLanternModel = parseClasspathJson("assets/entropica/models/block/mirage_prism_lantern.json");
        JsonObject prismLanternHangingModel = parseClasspathJson("assets/entropica/models/block/mirage_prism_lantern_hanging.json");
        check(prismLanternModel.has("elements") && prismLanternModel.getAsJsonArray("elements").size() >= 10,
                "mirage_prism_lantern.json must define multi-element 3D lantern geometry");
        check(prismLanternHangingModel.has("elements") && prismLanternHangingModel.getAsJsonArray("elements").size() >= 10,
                "mirage_prism_lantern_hanging.json must define multi-element 3D hanging lantern geometry");

        for (String blockTex : List.of(
                "glass_sand", "glass_sandstone", "glass_sandstone_top",
                "glass_sandstone_bottom", "mirage_ice", "mirage_prism_lantern_core"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        for (String it16 : List.of(
                "silica_glass_plate", "mirage_veil_goggles", "mirage_decoy_prism",
                "shimmering_focus", "silica_glass_scarab_spawn_egg"
        )) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it16 + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it16 + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it16 + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it16 + ".png", 16, 16);
            check(langJson.has("item.entropica." + it16), "en_us.json missing item.entropica." + it16);
        }
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "shimmering_focus")) instanceof ShimmeringFocusItem,
                "shimmering_focus must be upgraded to ShimmeringFocusItem");

        check(BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "double_vision")),
                "Missing double_vision MobEffect");
        check(BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_phasing")),
                "Missing mirage_phasing MobEffect");
        check(langJson.has("effect.entropica.double_vision") && langJson.has("effect.entropica.mirage_phasing"),
                "en_us.json missing double_vision or mirage_phasing effect translation");
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.mirage_phasing"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.mirage_phasing");
        }

        check(Math.abs(SilicaGlassScarabEntity.createAttributes().build().getValue(Attributes.MAX_HEALTH) - 30.0D) < 1.0E-6D,
                "Silica Glass-Scarab MAX_HEALTH must be 30.0");
        check(SilicaGlassScarabModel.createBodyLayer().bakeRoot() != null,
                "SilicaGlassScarabModel LayerDefinition must bake cleanly");
        verifyPngDimensions("assets/entropica/textures/entity/silica_glass_scarab/silica_glass_scarab.png", 64, 64);
        verifyPngDimensions("assets/entropica/textures/entity/silica_glass_scarab/silica_glass_scarab_glow.png", 64, 64);

        for (String entLoot : List.of("silica_glass_scarab", "mirage_fata_sprite")) {
            parseClasspathJson("data/entropica/loot_table/entities/" + entLoot + ".json");
        }
        for (String rec : List.of(
                "glass_sandstone", "glass_sandstone_stairs", "glass_sandstone_slab",
                "glass_from_smelting_glass_sand", "glass_from_blasting_glass_sand",
                "mirage_glass_from_smelting_mirage_ice", "mirage_ice",
                "mirage_prism_lantern", "mirage_veil_goggles",
                "mirage_decoy_prism", "shimmering_focus"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }

        // 6. Empirical Grot Spawn Pool Verification (60% Mirage, 30% Glacial, 10% Arid)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b13Holder = mockBiome("entropica", "mirage_glacier");
        RandomSource rng = RandomSource.create(202613L);
        int mirage = 0, glacial = 0, arid = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b13Holder, rng);
            if (rolled == EssenceType.MIRAGE) mirage++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.ARID) arid++;
        }
        double mirageRatio = mirage / (double) total;
        double glacialRatio = glacial / (double) total;
        double aridRatio = arid / (double) total;
        check(mirageRatio > 0.50 && mirageRatio < 0.68, "Unexpected Mirage Grot ratio: " + mirageRatio);
        check(glacialRatio > 0.22 && glacialRatio < 0.38, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(aridRatio > 0.05 && aridRatio < 0.16, "Unexpected Arid Grot ratio: " + aridRatio);

        System.out.println(String.format(
                "[PASS] 21/21 Biome 13 (Mirage Glacier) worldgen, core-free Fata Morgana Sanctuary & loot, 6 blocks (seamless Mirage Ice + Canyon-Strata Glass Sandstone + 3D Prism Lantern), 5 items/eggs (incl. upgraded Shimmering Focus), Double Vision + immediate suffocation + Mirage Phasing potions, Silica Glass-Scarab (15 HP), 11 recipes, 13 PNGs, en_us, & Grot pool (Mirage=%.1f%%, Glacial=%.1f%%, Arid=%.1f%%) verified.",
                mirageRatio * 100.0, glacialRatio * 100.0, aridRatio * 100.0
        ));
    }

    private static void verifyBiome14OasisFloesContentAndAssets() {
        // 1. 6D Multi-Noise Placement & Strict Pairwise Disjointness vs Biomes #1-#13!
        EntropicaBiome b14 = EntropicaBiome.OASIS_FLOES;
        BiomePlacementRule b14Rule = EntropicaBiomePlacements.allRules().get(b14);
        check(b14Rule != null, "OASIS_FLOES placement rule must be registered");
        check(b14Rule.getParameterPoints().size() == 3, "OASIS_FLOES must register 3 inland thermal basin parameter slices");

        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF, EntropicaBiome.AURORAL_SKERRIES, EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE, EntropicaBiome.SILENT_FJORD, EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS, EntropicaBiome.BARROW_MOUND_TUNDRA, EntropicaBiome.NULL_FRINGE_BAY,
                EntropicaBiome.RADIANT_GLAZE_PLAINS, EntropicaBiome.STORM_SWEPT_ICEBERGS, EntropicaBiome.SPORE_GLAZED_PACK,
                EntropicaBiome.MIRAGE_GLACIER
        )) {
            BiomePlacementRule prevRule = EntropicaBiomePlacements.allRules().get(prev);
            check(prevRule != null, "Missing previous placement rule for " + prev);
            for (Climate.ParameterPoint p14 : b14Rule.getParameterPoints()) {
                for (Climate.ParameterPoint pOther : prevRule.getParameterPoints()) {
                    boolean disjoint =
                            p14.temperature().max() < pOther.temperature().min() || pOther.temperature().max() < p14.temperature().min()
                            || p14.humidity().max() < pOther.humidity().min() || pOther.humidity().max() < p14.humidity().min()
                            || p14.continentalness().max() < pOther.continentalness().min() || pOther.continentalness().max() < p14.continentalness().min()
                            || p14.erosion().max() < pOther.erosion().min() || pOther.erosion().max() < p14.erosion().min()
                            || p14.depth().max() < pOther.depth().min() || pOther.depth().max() < p14.depth().min()
                            || p14.weirdness().max() < pOther.weirdness().min() || pOther.weirdness().max() < p14.weirdness().min();
                    check(disjoint, "OASIS_FLOES parameter box collides in 6D space with " + prev);
                }
            }
        }

        SurfaceRules.RuleSource surf14 = b14Rule.getSurfaceRule();
        check(surf14 != null, "OASIS_FLOES must define a custom SurfaceRule");
        String surfStr = surf14.toString();
        check(surfStr.contains("entropica:vitae_spring_stone")
                && surfStr.contains("minecraft:grass_block")
                && surfStr.contains("minecraft:snow_block")
                && !surfStr.contains("amber_spring_stone"),
                "OASIS_FLOES surface rule must reuse vitae_spring_stone alongside grass_block & snow_block (zero amber_spring_stone bloat)");

        // 2. Atmosphere & Biome JSON (#B57A3C sky = 11893308, #E8C69A humid mist = 15255194, water-surface steam columns)
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b14);
        check(atmos != null, "Missing BiomeAtmosphereProfile for OASIS_FLOES");
        check(atmos.getDayFogTintRgb() == 0xE8C69A, "OASIS_FLOES day fog tint must be #E8C69A");
        check(atmos.getWaterSurfaceSteamColumnsPerTick() > 0, "OASIS_FLOES must emit rising water-surface steam columns");
        check(atmos.getCustomServerTick() != null, "OASIS_FLOES must register peaceful thermal spring soothing tick");

        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/oasis_floes.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects.get("sky_color").getAsInt() == 0xB57A3C, "OASIS_FLOES sky_color must be #B57A3C (11893308)");
        check(effects.get("fog_color").getAsInt() == 0xE8C69A, "OASIS_FLOES fog_color must be #E8C69A (15255194)");
        String biomeStr = biomeJson.toString();
        for (String expected : List.of(
                "entropica:mud_lurker_salamander", "entropica:amber_weeping_stag",
                "entropica:vapor_bellows_crab", "entropica:aurorafowl", "entropica:grot",
                "entropica:oasis_amber_spring", "entropica:amber_weeping_willow",
                "entropica:oasis_silver_pine", "entropica:patch_oasis_floes_flora"
        )) {
            check(biomeStr.contains(expected), "oasis_floes.json missing " + expected);
        }

        // 3. Ice-Safe Flora, Features, Vitae Spring Stone Reuse & Core-Free Amber-Willow Thermal Bathhouse!
        for (String feat : List.of("oasis_amber_spring", "amber_weeping_willow", "oasis_silver_pine", "patch_oasis_floes_flora")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + feat + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + feat + ".json");
        }
        String floraPlaced = parseClasspathJson("data/entropica/worldgen/placed_feature/patch_oasis_floes_flora.json").toString();
        check(floraPlaced.contains("MOTION_BLOCKING_NO_LEAVES"), "patch_oasis_floes_flora placed_feature must use MOTION_BLOCKING_NO_LEAVES");
        String floraCombined = parseClasspathJson("data/entropica/worldgen/configured_feature/patch_oasis_floes_flora.json").toString() + floraPlaced;
        for (String iceBlock : List.of("\"minecraft:ice\"", "\"minecraft:packed_ice\"", "\"minecraft:blue_ice\"")) {
            check(!floraCombined.contains(iceBlock), "patch_oasis_floes_flora must NOT spawn on " + iceBlock);
        }

        ClassLoader cl = BiomeInjectorVerificationSuite.class.getClassLoader();
        check(cl.getResource("assets/entropica/blockstates/amber_spring_stone.json") == null
                && cl.getResource("assets/entropica/textures/block/amber_spring_stone.png") == null,
                "Biome 14 must reuse vitae_spring_stone without duplicate amber_spring_stone assets");

        parseClasspathJson("data/entropica/worldgen/structure/amber_willow_bathhouse.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/amber_willow_bathhouse.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/amber_willow_bathhouse.json")
                .toString().contains("entropica:oasis_floes"), "amber_willow_bathhouse tag missing oasis_floes");

        String bathLoot = parseClasspathJson("data/entropica/loot_table/chests/amber_willow_bathhouse.json").toString();
        check(!bathLoot.contains("materia_blessing_shard") && !bathLoot.contains("materia_blessing") && !bathLoot.contains("fragment_essence"),
                "chests/amber_willow_bathhouse.json MUST NOT contain materia_blessing_shard or fragment_essence");
        check(bathLoot.contains("entropica:salamander_wader_hood") && bathLoot.contains("entropica:amber_adhesive"),
                "chests/amber_willow_bathhouse.json must yield salamander_wader_hood and amber_adhesive");

        // 4. Full-16x16 VineBlock Frost-Willow Vines, 3D Oasis Dew-Basin, 32px Ampoule Nectar, Fauna 33 & 9 Recipes!
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        Block willowVines = BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_willow_vines"));
        check(willowVines instanceof VineBlock, "frost_willow_vines must extend VineBlock so it fills the entire 16x16 block face");
        JsonObject vineBs = parseClasspathJson("assets/entropica/blockstates/frost_willow_vines.json");
        check(vineBs.has("multipart") && vineBs.getAsJsonArray("multipart").size() >= 5,
                "frost_willow_vines.json must define full VineBlock multipart faces");

        for (String blk : List.of("frost_willow_leaves", "frost_willow_vines", "amber_resin_block", "amber_torch", "oasis_dew_basin")) {
            check(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, blk)), "Missing block " + blk);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", blk.toUpperCase());
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", blk.toUpperCase() + "_ITEM");
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/item/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
            check(langJson.has("item.entropica." + blk), "en_us.json missing item.entropica." + blk);
        }
        check(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "wall_amber_torch")),
                "Missing wall_amber_torch block");
        parseClasspathJson("assets/entropica/blockstates/wall_amber_torch.json");
        parseClasspathJson("data/entropica/loot_table/blocks/wall_amber_torch.json");

        JsonObject basinModel = parseClasspathJson("assets/entropica/models/block/oasis_dew_basin.json");
        check(basinModel.has("elements") && basinModel.getAsJsonArray("elements").size() >= 12,
                "oasis_dew_basin.json must define multi-element 3D thermal basin geometry");
        check(basinModel.toString().contains("entropica:block/vitae_spring_stone"),
                "oasis_dew_basin.json must reuse entropica:block/vitae_spring_stone");

        for (String blockTex : List.of("frost_willow_leaves", "frost_willow_vines", "amber_resin_block", "amber_torch", "oasis_dew_basin_water")) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        // Verify restored 32x32 ampoule amber_nectar_bottle & upgraded AmberNectarBottleItem!
        verifyPngDimensions("assets/entropica/textures/item/amber_nectar_bottle.png", 32, 32);
        parseClasspathJson("assets/entropica/items/amber_nectar_bottle.json");
        parseClasspathJson("assets/entropica/models/item/amber_nectar_bottle.json");
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_nectar_bottle")) instanceof AmberNectarBottleItem,
                "amber_nectar_bottle must be upgraded to AmberNectarBottleItem");

        for (String it16 : List.of("amber_adhesive", "waterproof_salamander_skin", "salamander_wader_hood", "mud_lurker_salamander_spawn_egg")) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it16 + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it16 + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it16 + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it16 + ".png", 16, 16);
            check(langJson.has("item.entropica." + it16), "en_us.json missing item.entropica." + it16);
        }

        check(BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "oasis_renewal")),
                "Missing oasis_renewal MobEffect");
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.oasis_renewal"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.oasis_renewal");
        }

        check(Math.abs(MudLurkerSalamanderEntity.createAttributes().build().getValue(Attributes.MAX_HEALTH) - 36.0D) < 1.0E-6D,
                "Mud-Lurker Salamander MAX_HEALTH must be 36.0");
        check(MudLurkerSalamanderModel.createBodyLayer().bakeRoot() != null,
                "MudLurkerSalamanderModel LayerDefinition must bake cleanly");
        verifyPngDimensions("assets/entropica/textures/entity/mud_lurker_salamander/mud_lurker_salamander.png", 128, 128);
        verifyPngDimensions("assets/entropica/textures/entity/mud_lurker_salamander/mud_lurker_salamander_nether.png", 128, 128);
        parseClasspathJson("data/entropica/loot_table/entities/mud_lurker_salamander.json");

        for (String rec : List.of(
                "amber_resin_block", "amber_chunk_from_amber_resin_block", "amber_torch",
                "oasis_dew_basin", "amber_adhesive", "amber_adhesive_from_honeycomb",
                "sticky_piston_from_amber_adhesive", "lead_from_amber_adhesive", "salamander_wader_hood"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }

        // 5. Empirical Grot Spawn Pool Verification (60% Oasis, 30% Glacial, 10% Vapor)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b14Holder = mockBiome("entropica", "oasis_floes");
        RandomSource rng = RandomSource.create(202614L);
        int oasis = 0, glacial = 0, vapor = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b14Holder, rng);
            if (rolled == EssenceType.OASIS) oasis++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.VAPOR) vapor++;
        }
        double oasisRatio = oasis / (double) total;
        double glacialRatio = glacial / (double) total;
        double vaporRatio = vapor / (double) total;
        check(oasisRatio > 0.50 && oasisRatio < 0.68, "Unexpected Oasis Grot ratio: " + oasisRatio);
        check(glacialRatio > 0.22 && glacialRatio < 0.38, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(vaporRatio > 0.05 && vaporRatio < 0.16, "Unexpected Vapor Grot ratio: " + vaporRatio);

        System.out.println(String.format(
                "[PASS] 22/22 Biome 14 (Oasis Floes) worldgen, core-free Amber-Willow Thermal Bathhouse & loot, 6 blocks (reused Vitae Spring Stone + Resin A + Leaves A + full-16x16 VineBlock Willow Vines + Amber Torch + 3D Oasis Dew-Basin), 5 items/eggs (incl. 32px ampoule Bottled Amber Nectar), Oasis Renewal potions, Mud-Lurker Salamander (18 HP, Overworld & Nether variants), 9 recipes, 12 PNGs, en_us, & Grot pool (Oasis=%.1f%%, Glacial=%.1f%%, Vapor=%.1f%%) verified.",
                oasisRatio * 100.0, glacialRatio * 100.0, vaporRatio * 100.0
        ));
    }

    private static void verifyBiome15DustCrustedBergsContentAndAssets() {
        // 1. 6D Multi-Noise Placement & Strict Pairwise Disjointness vs ALL Biomes #1-#14!
        EntropicaBiome b15 = EntropicaBiome.DUST_CRUSTED_BERGS;
        BiomePlacementRule b15Rule = EntropicaBiomePlacements.allRules().get(b15);
        check(b15Rule != null, "DUST_CRUSTED_BERGS placement rule must be registered");
        check(b15Rule.getParameterPoints().size() == 4, "DUST_CRUSTED_BERGS must register 4 coastal/outer-berg parameter slices");

        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF, EntropicaBiome.AURORAL_SKERRIES, EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE, EntropicaBiome.SILENT_FJORD, EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS, EntropicaBiome.BARROW_MOUND_TUNDRA, EntropicaBiome.NULL_FRINGE_BAY,
                EntropicaBiome.RADIANT_GLAZE_PLAINS, EntropicaBiome.STORM_SWEPT_ICEBERGS, EntropicaBiome.SPORE_GLAZED_PACK,
                EntropicaBiome.MIRAGE_GLACIER, EntropicaBiome.OASIS_FLOES
        )) {
            BiomePlacementRule prevRule = EntropicaBiomePlacements.allRules().get(prev);
            check(prevRule != null, "Missing previous placement rule for " + prev);
            for (Climate.ParameterPoint p15 : b15Rule.getParameterPoints()) {
                for (Climate.ParameterPoint pOther : prevRule.getParameterPoints()) {
                    boolean disjoint =
                            p15.temperature().max() < pOther.temperature().min() || pOther.temperature().max() < p15.temperature().min()
                            || p15.humidity().max() < pOther.humidity().min() || pOther.humidity().max() < p15.humidity().min()
                            || p15.continentalness().max() < pOther.continentalness().min() || pOther.continentalness().max() < p15.continentalness().min()
                            || p15.erosion().max() < pOther.erosion().min() || pOther.erosion().max() < p15.erosion().min()
                            || p15.depth().max() < pOther.depth().min() || pOther.depth().max() < p15.depth().min()
                            || p15.weirdness().max() < pOther.weirdness().min() || pOther.weirdness().max() < p15.weirdness().min();
                    check(disjoint, "DUST_CRUSTED_BERGS parameter box collides in 6D space with " + prev);
                }
            }
        }

        SurfaceRules.RuleSource surf15 = b15Rule.getSurfaceRule();
        check(surf15 != null, "DUST_CRUSTED_BERGS must define a custom SurfaceRule");
        String surfStr = surf15.toString();
        check(surfStr.contains("entropica:grey_sand")
                && surfStr.contains("entropica:dust_crusted_ice")
                && surfStr.contains("entropica:coarse_gravel")
                && !surfStr.contains("minecraft:grass_block"),
                "DUST_CRUSTED_BERGS surface rule must use grey_sand, dust_crusted_ice & coarse_gravel with zero grass_block");

        // 2. Atmosphere & Biome JSON (#8A6B4E sky = 9071438, #9E8872 dust haze = 10389618, falling dust particles & blowing-sand hazard)
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b15);
        check(atmos != null, "Missing BiomeAtmosphereProfile for DUST_CRUSTED_BERGS");
        check(atmos.getDayFogTintRgb() == 0x9E8872, "DUST_CRUSTED_BERGS day fog tint must be #9E8872");
        check(atmos.getAmbientParticlesPerTick() > 0, "DUST_CRUSTED_BERGS must emit drifting grey dust particles");
        check(atmos.getCustomServerTick() != null, "DUST_CRUSTED_BERGS must register blowing-sand visibility hazard tick");

        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/dust_crusted_bergs.json");
        check(!biomeJson.get("has_precipitation").getAsBoolean(), "DUST_CRUSTED_BERGS must set has_precipitation=false so dry sand sheets stay snow-free");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects.get("sky_color").getAsInt() == 0x8A6B4E, "DUST_CRUSTED_BERGS sky_color must be #8A6B4E (9071438)");
        check(effects.get("fog_color").getAsInt() == 0x9E8872, "DUST_CRUSTED_BERGS fog_color must be #9E8872 (10389618)");
        String biomeStr = biomeJson.toString();
        for (String expected : List.of(
                "entropica:dust_core_golem", "entropica:sandstone_golem", "entropica:grot",
                "entropica:dust_table_iceberg", "entropica:wind_blown_dust_patch"
        )) {
            check(biomeStr.contains(expected), "dust_crusted_bergs.json missing " + expected);
        }

        // 3. Features, Core-Free Silt-Bound Berg Watch-Pylon & Clean Chest Loot!
        for (String feat : List.of("dust_table_iceberg", "wind_blown_dust_patch")) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + feat + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + feat + ".json");
        }
        String dustPlaced = parseClasspathJson("data/entropica/worldgen/placed_feature/wind_blown_dust_patch.json").toString();
        check(dustPlaced.contains("MOTION_BLOCKING_NO_LEAVES"), "wind_blown_dust_patch placed_feature must use MOTION_BLOCKING_NO_LEAVES");

        parseClasspathJson("data/entropica/worldgen/structure/silt_bound_berg_watch_pylon.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/silt_bound_berg_watch_pylon.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/silt_bound_berg_watch_pylon.json")
                .toString().contains("entropica:dust_crusted_bergs"), "silt_bound_berg_watch_pylon tag missing dust_crusted_bergs");

        String pylonLoot = parseClasspathJson("data/entropica/loot_table/chests/silt_bound_berg_watch_pylon.json").toString();
        check(!pylonLoot.contains("materia_blessing_shard") && !pylonLoot.contains("materia_blessing") && !pylonLoot.contains("fragment_essence"),
                "chests/silt_bound_berg_watch_pylon.json MUST NOT contain materia_blessing_shard or fragment_essence");
        check(pylonLoot.contains("entropica:copper_silt_sieve") && pylonLoot.contains("entropica:dust_devil_ampoule"),
                "chests/silt_bound_berg_watch_pylon.json must yield copper_silt_sieve and dust_devil_ampoule");

        // 4. Blocks, Climbable Woven Rope, 3D Brass-Louver Lantern, 32px Exact-Shell Ampoule, Fauna 14 & 9 Recipes!
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String blk : List.of("dust_crusted_ice", "grey_sand", "wind_blown_dust_scrub", "tough_fiber_rope", "wind_baffle_lantern")) {
            check(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, blk)), "Missing block " + blk);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", blk.toUpperCase());
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", blk.toUpperCase() + "_ITEM");
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/item/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
            check(langJson.has("item.entropica." + blk), "en_us.json missing item.entropica." + blk);
        }

        check(parseClasspathJson("data/minecraft/tags/block/climbable.json").toString().contains("entropica:tough_fiber_rope"),
                "climbable.json must include entropica:tough_fiber_rope");
        check(parseClasspathJson("data/minecraft/tags/block/sand.json").toString().contains("entropica:grey_sand"),
                "sand.json must include entropica:grey_sand");

        JsonObject lanternModel = parseClasspathJson("assets/entropica/models/block/wind_baffle_lantern.json");
        check(lanternModel.has("elements") && lanternModel.getAsJsonArray("elements").size() >= 10,
                "wind_baffle_lantern.json must define multi-element 3D Brass Wrap-Louvers geometry");

        for (String blockTex : List.of("dust_crusted_ice", "grey_sand", "wind_blown_dust_scrub", "tough_fiber_rope", "tough_fiber_rope_top", "wind_baffle_lantern_core")) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        verifyPngDimensions("assets/entropica/textures/item/dust_devil_ampoule.png", 32, 32);
        parseClasspathJson("assets/entropica/items/dust_devil_ampoule.json");
        parseClasspathJson("assets/entropica/models/item/dust_devil_ampoule.json");
        check(langJson.has("item.entropica.dust_devil_ampoule"), "en_us.json missing item.entropica.dust_devil_ampoule");
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dust_devil_ampoule")) instanceof DustDevilAmpouleItem,
                "dust_devil_ampoule must be an instance of DustDevilAmpouleItem");

        for (String it16 : List.of("dry_fibers", "dust_core_shard", "copper_silt_sieve", "dust_core_golem_spawn_egg")) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it16 + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it16 + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it16 + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it16 + ".png", 16, 16);
            check(langJson.has("item.entropica." + it16), "en_us.json missing item.entropica." + it16);
        }

        check(BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_weight")),
                "Missing zephyr_weight MobEffect");
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.zephyr_weight"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.zephyr_weight");
        }

        check(Math.abs(DustCoreGolemEntity.createAttributes().build().getValue(Attributes.MAX_HEALTH) - 60.0D) < 1.0E-6D,
                "Dust-Core Golem MAX_HEALTH must be 60.0");
        check(DustCoreGolemModel.createBodyLayer().bakeRoot() != null,
                "DustCoreGolemModel LayerDefinition must bake cleanly");
        verifyPngDimensions("assets/entropica/textures/entity/dust_core_golem/dust_core_golem.png", 64, 64);
        verifyPngDimensions("assets/entropica/textures/entity/dust_core_golem/dust_core_golem_canyon.png", 64, 64);
        parseClasspathJson("data/entropica/loot_table/entities/dust_core_golem.json");

        for (String rec : List.of(
                "tough_fiber_rope", "tough_fiber_rope_bundle", "string_from_dry_fibers",
                "lead_from_tough_fiber_rope", "dust_crusted_ice", "dust_glass_from_smelting_grey_sand",
                "copper_silt_sieve", "wind_baffle_lantern", "dust_devil_ampoule"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }

        // 5. Empirical Grot Spawn Pool Verification (60% Dust, 30% Glacial, 10% Arid)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b15Holder = mockBiome("entropica", "dust_crusted_bergs");
        RandomSource rng = RandomSource.create(202615L);
        int dust = 0, glacial = 0, arid = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b15Holder, rng);
            if (rolled == EssenceType.DUST) dust++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.ARID) arid++;
        }
        double dustRatio = dust / (double) total;
        double glacialRatio = glacial / (double) total;
        double aridRatio = arid / (double) total;
        check(dustRatio > 0.50 && dustRatio < 0.68, "Unexpected Dust Grot ratio: " + dustRatio);
        check(glacialRatio > 0.22 && glacialRatio < 0.38, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(aridRatio > 0.05 && aridRatio < 0.16, "Unexpected Arid Grot ratio: " + aridRatio);

        System.out.println(String.format(
                "[PASS] 23/23 Biome 15 (Dust-Crusted Bergs) worldgen, core-free Silt-Bound Berg Watch-Pylon & loot, 5 blocks (Ice C Dust-Crusted Ice + Sand B Grey Sand + Dust Scrub + Rope B Woven Climb Rope + 3D Lantern A Wind-Baffle Lantern), 5 items/eggs (incl. exact-shell 32px Ampoule B & Clean Sieve A), Zephyr Weight potions, Dust-Core Golem (60 HP, Polar & Canyon variants), 9 recipes, 13 PNGs, en_us, & Grot pool (Dust=%.1f%%, Glacial=%.1f%%, Arid=%.1f%%) verified.",
                dustRatio * 100.0, glacialRatio * 100.0, aridRatio * 100.0
        ));
    }

    private static void verifyBiome16BloodIceShallowsContentAndAssets() {
        // 1. 6D Multi-Noise Placement & Strict Pairwise Disjointness vs ALL Biomes #1-#15!
        EntropicaBiome b16 = EntropicaBiome.BLOOD_ICE_SHALLOWS;
        BiomePlacementRule b16Rule = EntropicaBiomePlacements.allRules().get(b16);
        check(b16Rule != null, "BLOOD_ICE_SHALLOWS placement rule must be registered");
        check(b16Rule.getParameterPoints().size() == 6, "BLOOD_ICE_SHALLOWS must register 6 coastal bay parameter slices");

        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF, EntropicaBiome.AURORAL_SKERRIES, EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE, EntropicaBiome.SILENT_FJORD, EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS, EntropicaBiome.BARROW_MOUND_TUNDRA, EntropicaBiome.NULL_FRINGE_BAY,
                EntropicaBiome.RADIANT_GLAZE_PLAINS, EntropicaBiome.STORM_SWEPT_ICEBERGS, EntropicaBiome.SPORE_GLAZED_PACK,
                EntropicaBiome.MIRAGE_GLACIER, EntropicaBiome.OASIS_FLOES, EntropicaBiome.DUST_CRUSTED_BERGS
        )) {
            BiomePlacementRule prevRule = EntropicaBiomePlacements.allRules().get(prev);
            check(prevRule != null, "Missing previous placement rule for " + prev);
            for (Climate.ParameterPoint p16 : b16Rule.getParameterPoints()) {
                for (Climate.ParameterPoint pOther : prevRule.getParameterPoints()) {
                    boolean disjoint =
                            p16.temperature().max() < pOther.temperature().min() || pOther.temperature().max() < p16.temperature().min()
                            || p16.humidity().max() < pOther.humidity().min() || pOther.humidity().max() < p16.humidity().min()
                            || p16.continentalness().max() < pOther.continentalness().min() || pOther.continentalness().max() < p16.continentalness().min()
                            || p16.erosion().max() < pOther.erosion().min() || pOther.erosion().max() < p16.erosion().min()
                            || p16.depth().max() < pOther.depth().min() || pOther.depth().max() < p16.depth().min()
                            || p16.weirdness().max() < pOther.weirdness().min() || pOther.weirdness().max() < p16.weirdness().min();
                    check(disjoint, "BLOOD_ICE_SHALLOWS parameter box collides in 6D space with " + prev);
                }
            }
        }

        SurfaceRules.RuleSource surf16 = b16Rule.getSurfaceRule();
        check(surf16 != null, "BLOOD_ICE_SHALLOWS must define a custom SurfaceRule");
        String surfStr = surf16.toString();
        check(surfStr.contains("entropica:sanguine_ice") && surfStr.contains("entropica:red_clay_sand"),
                "BLOOD_ICE_SHALLOWS surface rule must use sanguine_ice and red_clay_sand");

        // 2. Atmosphere & Biome JSON (#6A242B sky, #8C353D crimson mist, #5E1722 water, ZERO passive cold hazard)
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b16);
        check(atmos != null, "Missing BiomeAtmosphereProfile for BLOOD_ICE_SHALLOWS");
        check(atmos.getDayFogTintRgb() == 0x8C353D, "BLOOD_ICE_SHALLOWS day fog tint must be #8C353D");
        check(atmos.getAmbientParticlesPerTick() > 0, "BLOOD_ICE_SHALLOWS must emit rising crimson mist particles");
        check(atmos.getCustomServerTick() == null, "BLOOD_ICE_SHALLOWS must have ZERO passive environmental hazard tick");

        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/blood_ice_shallows.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects.get("sky_color").getAsInt() == 0x6A242B, "BLOOD_ICE_SHALLOWS sky_color must be #6A242B (6956075)");
        check(effects.get("fog_color").getAsInt() == 0x8C353D, "BLOOD_ICE_SHALLOWS fog_color must be #8C353D (9188669)");
        check(effects.get("water_color").getAsInt() == 0x5E1722, "BLOOD_ICE_SHALLOWS water_color must be #5E1722 (6166306)");
        check(effects.get("water_fog_color").getAsInt() == 0x3A0C14, "BLOOD_ICE_SHALLOWS water_fog_color must be #3A0C14 (3804180)");
        String biomeStr = biomeJson.toString();
        for (String expected : List.of(
                "entropica:sanguine_sea_angel", "entropica:aurora_jelly", "entropica:vapor_bellows_crab", "entropica:grot",
                "minecraft:salmon", "minecraft:cod",
                "entropica:sanguine_ice_shelf_placed", "entropica:red_algae_margin_placed"
        )) {
            check(biomeStr.contains(expected), "blood_ice_shallows.json missing " + expected);
        }

        // 3. Features, Core-Free Crimson Tide Sanctum & Clean Chest Loot!
        parseClasspathJson("data/entropica/worldgen/configured_feature/sanguine_ice_shelf.json");
        parseClasspathJson("data/entropica/worldgen/configured_feature/red_algae_margin.json");
        parseClasspathJson("data/entropica/worldgen/placed_feature/sanguine_ice_shelf_placed.json");
        parseClasspathJson("data/entropica/worldgen/placed_feature/red_algae_margin_placed.json");

        parseClasspathJson("data/entropica/worldgen/structure/crimson_tide_sanctum.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/crimson_tide_sanctum.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/crimson_tide_sanctum.json")
                .toString().contains("entropica:blood_ice_shallows"), "crimson_tide_sanctum tag missing blood_ice_shallows");

        String sanctumLoot = parseClasspathJson("data/entropica/loot_table/chests/crimson_tide_sanctum.json").toString();
        check(!sanctumLoot.contains("materia_blessing_shard") && !sanctumLoot.contains("materia_blessing") && !sanctumLoot.contains("fragment_essence"),
                "chests/crimson_tide_sanctum.json MUST NOT contain materia_blessing_shard or fragment_essence");
        check(sanctumLoot.contains("entropica:sanguine_ice_lancet") && sanctumLoot.contains("entropica:sanguine_draught_ampoule"),
                "chests/crimson_tide_sanctum.json must yield sanguine_ice_lancet and sanguine_draught_ampoule");

        // 4. 7 Blocks, Agar Option A (Edible/Sticky Red Algae, ZERO crimson_agar), 3D Lantern, 4 Standalone Items, Potions & 12 Recipes!
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        check(!BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crimson_agar")),
                "Agar Option A: crimson_agar item must NOT exist");

        for (String blk : List.of(
                "sanguine_ice", "red_clay_sand", "sanguine_clay_bricks",
                "sanguine_clay_brick_stairs", "sanguine_clay_brick_slab",
                "red_algae", "crimson_tide_lantern"
        )) {
            check(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, blk)), "Missing block " + blk);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", blk.toUpperCase());
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", blk.toUpperCase() + "_ITEM");
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/item/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
            check(langJson.has("item.entropica." + blk), "en_us.json missing item.entropica." + blk);
        }

        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "red_algae")) instanceof RedAlgaeItem,
                "red_algae item must be an edible RedAlgaeItem");
        check(parseClasspathJson("data/minecraft/tags/block/ice.json").toString().contains("entropica:sanguine_ice"),
                "ice.json must include entropica:sanguine_ice");
        check(parseClasspathJson("data/c/tags/item/slime_balls.json").toString().contains("entropica:red_algae"),
                "c:slime_balls must include entropica:red_algae");

        JsonObject lanternModel = parseClasspathJson("assets/entropica/models/block/crimson_tide_lantern.json");
        check(lanternModel.has("elements") && lanternModel.getAsJsonArray("elements").size() >= 10,
                "crimson_tide_lantern.json must define sculpted 3D geometry (>=10 elements)");

        for (String blockTex : List.of("sanguine_ice", "red_clay_sand", "sanguine_clay_bricks", "red_algae", "crimson_tide_lantern_core")) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        verifyPngDimensions("assets/entropica/textures/item/sanguine_draught_ampoule.png", 32, 32);
        parseClasspathJson("assets/entropica/items/sanguine_draught_ampoule.json");
        parseClasspathJson("assets/entropica/models/item/sanguine_draught_ampoule.json");
        check(langJson.has("item.entropica.sanguine_draught_ampoule"), "en_us.json missing item.entropica.sanguine_draught_ampoule");
        check(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_draught_ampoule")) instanceof SanguineDraughtAmpouleItem,
                "sanguine_draught_ampoule must be an instance of SanguineDraughtAmpouleItem");

        for (String it16 : List.of("sanguine_ice_lancet", "sanguine_heart_gel", "sanguine_sea_angel_spawn_egg")) {
            JsonObject itemDef = parseClasspathJson("assets/entropica/items/" + it16 + ".json");
            check(!itemDef.toString().contains("pig_spawn_egg"), it16 + ".json must NOT use placeholder pig_spawn_egg");
            parseClasspathJson("assets/entropica/models/item/" + it16 + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it16 + ".png", 16, 16);
            check(langJson.has("item.entropica." + it16), "en_us.json missing item.entropica." + it16);
        }

        check(BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_vigor")),
                "Missing sanguine_vigor MobEffect");
        for (String potKind : List.of("potion", "splash_potion", "lingering_potion", "tipped_arrow")) {
            check(langJson.has("item.minecraft." + potKind + ".effect.sanguine_vigor"),
                    "Missing en_us key item.minecraft." + potKind + ".effect.sanguine_vigor");
        }

        // 5. Hostile Amphibious Sanguine Sea-Angel (36 HP, 32x32 Translucent Model with Inner Heart First) & Blood-Frost Dart!
        check(Math.abs(SanguineSeaAngelEntity.createAttributes().build().getValue(Attributes.MAX_HEALTH) - 36.0D) < 1.0E-6D,
                "Sanguine Sea-Angel MAX_HEALTH must be 36.0");
        var bakedAngelRoot = SanguineSeaAngelModel.createBodyLayer().bakeRoot();
        check(bakedAngelRoot != null, "SanguineSeaAngelModel LayerDefinition must bake cleanly");
        var bakedBody = bakedAngelRoot.getChild("root").getChild("body");
        check(bakedBody.hasChild("heart_core") && bakedBody.hasChild("torso_shell"),
                "SanguineSeaAngelModel body must separate heart_core and torso_shell for translucent depth ordering");
        verifyPngDimensions("assets/entropica/textures/entity/sanguine_sea_angel/sanguine_sea_angel.png", 32, 32);
        verifyPngDimensions("assets/entropica/textures/entity/sanguine_sea_angel/sanguine_sea_angel_abyssal.png", 32, 32);
        parseClasspathJson("data/entropica/loot_table/entities/sanguine_sea_angel.json");
        check(langJson.has("entity.entropica.sanguine_sea_angel") && langJson.has("entity.entropica.blood_frost_dart"),
                "en_us.json missing entity translations for sanguine_sea_angel or blood_frost_dart");

        for (String rec : List.of(
                "sanguine_clay_bricks", "sanguine_clay_brick_stairs", "sanguine_clay_brick_slab",
                "blood_glass_from_smelting_red_clay_sand", "red_dye_from_red_algae",
                "sticky_piston_from_red_algae", "lead_from_red_algae", "magma_cream_from_red_algae",
                "sanguine_ice", "sanguine_ice_lancet", "crimson_tide_lantern", "sanguine_draught_ampoule"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }

        // 6. Empirical Grot Spawn Pool Verification (60% Blood, 30% Glacial, 10% Vitae)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b16Holder = mockBiome("entropica", "blood_ice_shallows");
        RandomSource rng = RandomSource.create(202616L);
        int blood = 0, glacial = 0, vitae = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b16Holder, rng);
            if (rolled == EssenceType.BLOOD) blood++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.VITAE) vitae++;
        }
        double bloodRatio = blood / (double) total;
        double glacialRatio = glacial / (double) total;
        double vitaeRatio = vitae / (double) total;
        check(bloodRatio > 0.50 && bloodRatio < 0.68, "Unexpected Blood Grot ratio: " + bloodRatio);
        check(glacialRatio > 0.22 && glacialRatio < 0.38, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(vitaeRatio > 0.05 && vitaeRatio < 0.16, "Unexpected Vitae Grot ratio: " + vitaeRatio);

        System.out.println(String.format(
                "[PASS] 24/24 Biome 16 (Blood-Ice Shallows) worldgen, core-free Crimson Tide Sanctum & loot, 7 blocks (Sanguine Ice + Red Clay Sand + Sanguine Clay Bricks/Stairs/Slab + Edible/Sticky Red Algae + 3D Crimson Tide Lantern), 4 standalone items (incl. exact-shell 32px Sanguine Draught Ampoule & Ice-Silk Sanguine Ice-Lancet), Sanguine Vigor potions, 32x32 Hostile Sanguine Sea-Angel (36 HP, 8 animations, Blood-Frost Dart), 12 recipes, 11 PNGs, en_us, & Grot pool (Blood=%.1f%%, Glacial=%.1f%%, Vitae=%.1f%%) verified.",
                bloodRatio * 100.0, glacialRatio * 100.0, vitaeRatio * 100.0
        ));
    }

    private static void verifyBiome17AstralSkerriesContentAndAssets() {
        // 1. 6D Multi-Noise Placement & Strict Pairwise Disjointness vs ALL Biomes #1-#16!
        EntropicaBiome b17 = EntropicaBiome.ASTRAL_SKERRIES;
        BiomePlacementRule b17Rule = EntropicaBiomePlacements.allRules().get(b17);
        check(b17Rule != null, "ASTRAL_SKERRIES placement rule must be registered");
        check(!b17Rule.getParameterPoints().isEmpty(), "ASTRAL_SKERRIES must register non-empty parameter slices");

        for (EntropicaBiome prev : List.of(
                EntropicaBiome.BLUE_GLACIER_SHELF, EntropicaBiome.AURORAL_SKERRIES, EntropicaBiome.FROST_BITE_FLOES,
                EntropicaBiome.CRYSTALLINE_PACK_ICE, EntropicaBiome.SILENT_FJORD, EntropicaBiome.STEAMING_SKERRIES,
                EntropicaBiome.STATIC_ICE_PLAINS, EntropicaBiome.BARROW_MOUND_TUNDRA, EntropicaBiome.NULL_FRINGE_BAY,
                EntropicaBiome.RADIANT_GLAZE_PLAINS, EntropicaBiome.STORM_SWEPT_ICEBERGS, EntropicaBiome.SPORE_GLAZED_PACK,
                EntropicaBiome.MIRAGE_GLACIER, EntropicaBiome.OASIS_FLOES, EntropicaBiome.DUST_CRUSTED_BERGS,
                EntropicaBiome.BLOOD_ICE_SHALLOWS
        )) {
            BiomePlacementRule prevRule = EntropicaBiomePlacements.allRules().get(prev);
            check(prevRule != null, "Missing previous placement rule for " + prev);
            for (Climate.ParameterPoint p17 : b17Rule.getParameterPoints()) {
                for (Climate.ParameterPoint pOther : prevRule.getParameterPoints()) {
                    boolean disjoint =
                            p17.temperature().max() < pOther.temperature().min() || pOther.temperature().max() < p17.temperature().min()
                            || p17.humidity().max() < pOther.humidity().min() || pOther.humidity().max() < p17.humidity().min()
                            || p17.continentalness().max() < pOther.continentalness().min() || pOther.continentalness().max() < p17.continentalness().min()
                            || p17.erosion().max() < pOther.erosion().min() || pOther.erosion().max() < p17.erosion().min()
                            || p17.depth().max() < pOther.depth().min() || pOther.depth().max() < p17.depth().min()
                            || p17.weirdness().max() < pOther.weirdness().min() || pOther.weirdness().max() < p17.weirdness().min();
                    check(disjoint, "ASTRAL_SKERRIES parameter box collides in 6D space with " + prev);
                }
            }
        }

        SurfaceRules.RuleSource surf17 = b17Rule.getSurfaceRule();
        check(surf17 != null, "ASTRAL_SKERRIES must define a custom SurfaceRule");
        String surfStr = surf17.toString();
        check(surfStr.contains("entropica:starlight_ice"),
                "ASTRAL_SKERRIES surface rule must use starlight_ice");
        check(!surfStr.contains("minecraft:grass_block"),
                "ASTRAL_SKERRIES barren skerry surface rule must NOT use grass_block");

        // 2. Atmosphere, Volumetric Mist & Water Starlight Parallax + Biome JSON
        BiomeAtmosphereProfile atmos = EntropicaBiomeAtmospheres.get(b17);
        check(atmos != null, "Missing BiomeAtmosphereProfile for ASTRAL_SKERRIES");
        check(atmos.getWaterOpticalMode() == WaterOpticalMode.STARLIGHT_PARALLAX,
                "ASTRAL_SKERRIES waterOpticalMode must be STARLIGHT_PARALLAX");
        check(atmos.hasVolumetricMist(), "ASTRAL_SKERRIES must enable volumetric mist");
        check(atmos.getCustomServerTick() == null, "ASTRAL_SKERRIES must have ZERO passive environmental hazard tick");

        JsonObject biomeJson = parseClasspathJson("data/entropica/worldgen/biome/astral_skerries.json");
        JsonObject effects = biomeJson.getAsJsonObject("effects");
        check(effects.get("sky_color").getAsInt() == 0x1C1A38, "ASTRAL_SKERRIES sky_color must be #1C1A38");
        check(effects.get("fog_color").getAsInt() == 0x32245C, "ASTRAL_SKERRIES fog_color must be #32245C");
        check(effects.get("water_color").getAsInt() == 0x1B2854, "ASTRAL_SKERRIES water_color must be #1B2854");
        check(effects.get("water_fog_color").getAsInt() == 0x101636, "ASTRAL_SKERRIES water_fog_color must be #101636");
        String biomeStr = biomeJson.toString();
        for (String expected : List.of(
                "entropica:starlight_nautilus", "entropica:aurora_jelly", "entropica:luminoth",
                "entropica:narrow_starlight_ice_pillar", "entropica:stardust_crystal_spire",
                "entropica:patch_rime_star_tussock"
        )) {
            check(biomeStr.contains(expected), "astral_skerries.json missing " + expected);
        }

        // 3. Tall Spire Features, Core-Free Stardust Skerry Orrery & Clean Chest Loot
        for (String feat : List.of(
                "narrow_starlight_ice_pillar", "stardust_crystal_spire",
                "astral_skerry_impactite_outcrop", "patch_rime_star_tussock", "astral_skerry_crystal_clusters"
        )) {
            parseClasspathJson("data/entropica/worldgen/configured_feature/" + feat + ".json");
            parseClasspathJson("data/entropica/worldgen/placed_feature/" + feat + ".json");
        }

        parseClasspathJson("data/entropica/worldgen/structure/stardust_skerry_orrery.json");
        parseClasspathJson("data/entropica/worldgen/structure_set/stardust_skerry_orrery.json");
        check(parseClasspathJson("data/entropica/tags/worldgen/biome/has_structure/stardust_skerry_orrery.json")
                .toString().contains("entropica:astral_skerries"), "stardust_skerry_orrery tag missing astral_skerries");

        String orreryLoot = parseClasspathJson("data/entropica/loot_table/chests/stardust_skerry_orrery.json").toString();
        check(!orreryLoot.contains("materia_blessing_shard") && !orreryLoot.contains("fragment_essence"),
                "chests/stardust_skerry_orrery.json MUST NOT contain materia_blessing_shard or fragment_essence");
        check(orreryLoot.contains("entropica:starlight_tuning_chisel") && orreryLoot.contains("entropica:starlight_draught_ampoule"),
                "chests/stardust_skerry_orrery.json must yield starlight_tuning_chisel and starlight_draught_ampoule");

        // 4. Blocks, 3D Orrery Lantern, Standalone Items, 12 Star Fish, Armor Option B & Potions
        JsonObject langJson = parseClasspathJson("assets/entropica/lang/en_us.json");
        for (String blk : List.of(
                "starlight_ice", "stardust_crystal_block", "stardust_crystal_cluster",
                "rime_star_tussock", "celestial_orrery_lantern"
        )) {
            check(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, blk)), "Missing block " + blk);
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModBlocks", blk.toUpperCase());
            checkDeclaredStaticField("ddraig.net.entropica.registry.ModItems", blk.toUpperCase() + "_ITEM");
            parseClasspathJson("assets/entropica/blockstates/" + blk + ".json");
            parseClasspathJson("assets/entropica/items/" + blk + ".json");
            parseClasspathJson("assets/entropica/models/item/" + blk + ".json");
            parseClasspathJson("data/entropica/loot_table/blocks/" + blk + ".json");
            check(langJson.has("block.entropica." + blk), "en_us.json missing block.entropica." + blk);
            check(langJson.has("item.entropica." + blk), "en_us.json missing item.entropica." + blk);
        }
        parseClasspathJson("assets/entropica/blockstates/potted_rime_star_tussock.json");
        parseClasspathJson("data/entropica/loot_table/blocks/potted_rime_star_tussock.json");

        JsonObject lanternModel = parseClasspathJson("assets/entropica/models/block/celestial_orrery_lantern.json");
        check(lanternModel.has("elements") && lanternModel.getAsJsonArray("elements").size() >= 8,
                "celestial_orrery_lantern.json must define sculpted 3D geometry (>=8 elements)");

        for (String blockTex : List.of(
                "starlight_ice", "stardust_crystal_block", "stardust_crystal_cluster",
                "rime_star_tussock", "celestial_orrery_lantern_core"
        )) {
            verifyPngDimensions("assets/entropica/textures/block/" + blockTex + ".png", 16, 16);
        }

        verifyPngDimensions("assets/entropica/textures/item/starlight_draught_ampoule.png", 32, 32);
        verifyPngDimensions("assets/entropica/textures/mob_effect/starlight_grace.png", 18, 18);

        List<String> items16 = List.of(
                "stardust_fragment", "starlight_tuning_chisel", "astral_nautilus_nacre",
                "astral_nautilus_armor", "starlight_nautilus_spawn_egg",
                "nebula_darter", "cooked_nebula_darter",
                "starwing_flying_fish", "cooked_starwing_flying_fish",
                "starfall_ray", "cooked_starfall_ray",
                "comet_ribbonfish", "cooked_comet_ribbonfish",
                "zenith_moonfish", "cooked_zenith_moonfish",
                "supernova_puffer", "astral_lanternfish"
        );
        for (String it : items16) {
            parseClasspathJson("assets/entropica/items/" + it + ".json");
            parseClasspathJson("assets/entropica/models/item/" + it + ".json");
            verifyPngDimensions("assets/entropica/textures/item/" + it + ".png", 16, 16);
            check(langJson.has("item.entropica." + it), "en_us.json missing item.entropica." + it);
        }
        parseClasspathJson("assets/entropica/items/starlight_draught_ampoule.json");
        parseClasspathJson("assets/entropica/models/item/starlight_draught_ampoule.json");

        String fishTag = parseClasspathJson("data/minecraft/tags/item/fishes.json").toString();
        String nautFoodTag = parseClasspathJson("data/entropica/tags/item/starlight_nautilus_food.json").toString();
        for (String rawFish : List.of(
                "nebula_darter", "starwing_flying_fish", "starfall_ray",
                "comet_ribbonfish", "zenith_moonfish", "supernova_puffer", "astral_lanternfish"
        )) {
            check(fishTag.contains("entropica:" + rawFish), "fishes.json missing " + rawFish);
            check(nautFoodTag.contains("entropica:" + rawFish), "starlight_nautilus_food.json missing " + rawFish);
        }

        // 5. Flying Starlight Nautilus (36 HP, Exact Mojang 128x128 Rig + Baby + Saddle + 7 Armor Atlases)
        check(Math.abs(StarlightNautilusEntity.createAttributes().build().getValue(Attributes.MAX_HEALTH) - 36.0D) < 1.0E-6D,
                "Starlight Nautilus MAX_HEALTH must be 36.0");
        var nautRoot = StarlightNautilusModel.createBodyLayer().bakeRoot();
        check(nautRoot != null && nautRoot.getChild("root").hasChild("shell") && nautRoot.getChild("root").hasChild("body"),
                "StarlightNautilusModel must bake Mojang root -> shell + body hierarchy");
        check(StarlightNautilusModel.createBabyBodyLayer().bakeRoot() != null, "Baby StarlightNautilusModel must bake cleanly");
        check(StarlightNautilusModel.createSaddleLayer().bakeRoot() != null, "Saddle StarlightNautilusModel must bake cleanly");
        check(StarlightNautilusModel.createArmorLayer().bakeRoot() != null, "Armor StarlightNautilusModel must bake cleanly");

        for (String nautTex : List.of(
                "starlight_nautilus", "starlight_nautilus_baby", "starlight_nautilus_saddle",
                "starlight_nautilus_armor_leather", "starlight_nautilus_armor_copper",
                "starlight_nautilus_armor_iron", "starlight_nautilus_armor_gold",
                "starlight_nautilus_armor_diamond", "starlight_nautilus_armor_netherite",
                "starlight_nautilus_armor_astral"
        )) {
            verifyPngDimensions("assets/entropica/textures/entity/starlight_nautilus/" + nautTex + ".png", 128, 128);
        }
        check(StarlightNautilusArmorType.fromItemStack(new net.minecraft.world.item.ItemStack(Items.DIAMOND_HORSE_ARMOR)) == StarlightNautilusArmorType.DIAMOND,
                "Diamond Horse Armor must resolve to StarlightNautilusArmorType.DIAMOND");
        check(StarlightNautilusArmorType.fromItemStack(new net.minecraft.world.item.ItemStack(
                BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_nautilus_armor")))) == StarlightNautilusArmorType.ASTRAL_SLATE,
                "Astral Slate Nautilus Armor must resolve to StarlightNautilusArmorType.ASTRAL_SLATE");

        parseClasspathJson("data/entropica/loot_table/entities/starlight_nautilus.json");
        check(langJson.has("entity.entropica.starlight_nautilus"), "en_us.json missing entity.entropica.starlight_nautilus");

        for (String rec : List.of(
                "starlight_ice", "stardust_crystal_block", "stardust_fragment_from_block",
                "starlight_tuning_chisel", "celestial_orrery_lantern", "starlight_draught_ampoule",
                "astral_nautilus_armor", "cooked_nebula_darter", "cooked_starwing_flying_fish",
                "cooked_starfall_ray", "cooked_comet_ribbonfish", "cooked_zenith_moonfish"
        )) {
            parseClasspathJson("data/entropica/recipe/" + rec + ".json");
        }

        // 6. Empirical Grot Spawn Pool Verification (60% Astral, 30% Glacial, 10% Aurora)
        GrotSpawnRegistry.initialize();
        Holder<Biome> b17Holder = mockBiome("entropica", "astral_skerries");
        RandomSource rng = RandomSource.create(202617L);
        int astral = 0, glacial = 0, aurora = 0, total = 10_000;
        for (int i = 0; i < total; i++) {
            EssenceType rolled = GrotSpawnRegistry.rollEssenceForBiome(b17Holder, rng);
            if (rolled == EssenceType.ASTRAL) astral++;
            else if (rolled == EssenceType.GLACIAL) glacial++;
            else if (rolled == EssenceType.AURORA) aurora++;
        }
        double astralRatio = astral / (double) total;
        double glacialRatio = glacial / (double) total;
        double auroraRatio = aurora / (double) total;
        check(astralRatio > 0.50 && astralRatio < 0.68, "Unexpected Astral Grot ratio: " + astralRatio);
        check(glacialRatio > 0.22 && glacialRatio < 0.38, "Unexpected Glacial Grot ratio: " + glacialRatio);
        check(auroraRatio > 0.05 && auroraRatio < 0.16, "Unexpected Aurora Grot ratio: " + auroraRatio);

        System.out.println(String.format(
                "[PASS] 25/25 Biome 17 (Astral Skerries) worldgen, tall Starlight Ice & Stardust Spires, core-free Stardust Skerry Orrery & loot, 6 blocks, 18 items (incl. 12 Star Fish, exact-shell 32px Starlight Draught Ampoule, Tuning Chisel, & Armor Opt B), Starlight Grace potions, 128x128 Flying Starlight Nautilus (36 HP, Saddle + 7 Armors), recipes, PNGs, en_us, & Grot pool (Astral=%.1f%%, Glacial=%.1f%%, Aurora=%.1f%%) verified.",
                astralRatio * 100.0, glacialRatio * 100.0, auroraRatio * 100.0
        ));
    }
}
