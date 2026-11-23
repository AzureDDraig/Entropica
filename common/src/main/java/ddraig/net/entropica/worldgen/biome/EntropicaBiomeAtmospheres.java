package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.config.EntropicaBiome;
import ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity;
import ddraig.net.entropica.entity.grot.GrotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry mapping {@link EntropicaBiome}s to their custom fog, shader/Sodium-compatible
 * sky phenomena, wind-drifted ambient particles, and environmental physics/hazards.
 */
public final class EntropicaBiomeAtmospheres {
    private static final Map<EntropicaBiome, BiomeAtmosphereProfile> BY_BIOME = new EnumMap<>(EntropicaBiome.class);
    private static final Map<String, BiomeAtmosphereProfile> BY_PATH = new HashMap<>();
    private static final Map<UUID, Integer> STATIC_CHARGE_SECONDS = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static final ResourceLocation ROTTEN_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rotten_ice");
    private static final ResourceLocation CRYO_HELMET_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_insulated_helmet");
    private static final ResourceLocation CRYO_CHESTPLATE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_insulated_chestplate");
    private static final ResourceLocation CRYO_LEGGINGS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_insulated_leggings");
    private static final ResourceLocation CRYO_BOOTS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_insulated_boots");
    private static final ResourceLocation SILENT_STRIDERS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silent_striders");
    private static final ResourceLocation STEAM_VENT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steam_vent");
    private static final ResourceLocation STEAMING_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steaming_spring_stone");
    private static final ResourceLocation GROUNDING_COIL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grounding_coil");
    private static final ResourceLocation GALVANIC_SURGE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "galvanic_surge");
    private static final ResourceLocation STATIC_CONDUCTIVE_MAT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "static_conductive_mat");
    private static final ResourceLocation VOID_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sand");
    private static final ResourceLocation VOID_SAND_TURF_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sand_turf");
    private static final ResourceLocation QILIN_PRISM_CIRCLET_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "qilin_prism_circlet");
    private static final ResourceLocation RADIANT_SNOW_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_snow_block");
    private static final ResourceLocation SILVER_PINE_PLANKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silver_pine_planks");
    private static final ResourceLocation ZEPHYR_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_ice");
    private static final ResourceLocation FULGURITE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_ice");
    private static final ResourceLocation FULGURITE_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_block");
    private static final ResourceLocation DOUBLE_VISION_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "double_vision");
    private static final ResourceLocation OASIS_RENEWAL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "oasis_renewal");
    private static final ResourceLocation ZEPHYR_WEIGHT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_weight");
    private static final ResourceLocation MIRAGE_VEIL_GOGGLES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_veil_goggles");
    private static final ResourceLocation SPORE_FILTER_MASK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_filter_mask");
    private static final ResourceLocation WIND_BAFFLE_LANTERN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "wind_baffle_lantern");

    private EntropicaBiomeAtmospheres() {}

    public static synchronized void registerAll() {
        if (initialized) {
            return;
        }
        initialized = true;

        // =========================================================================
        // BIOME #1 — 2.1 Blue-Glacier Shelf (Glacial + Aegis)
        // - Pale indigo sky (#3F4E8F) with subtle polar night glow (#1E2952)
        // - Thick pale-blue coastal haze (#B8D4EE)
        // - Crystalline polar ice-halo (parhelion) sky phenomenon
        // - Pale-blue snow particles drifting horizontally in cold coastal air
        // - Aegis Heavy Gravity: +15% faster descent velocity (normal fall damage)
        // =========================================================================
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.BLUE_GLACIER_SHELF)
                .fogDistance(12.0F, 96.0F, 0.75F)
                .fogColors(0xB8D4EE, 0x263B5E)
                .nightSkyColor(0x1E2952)
                .skyPhenomenon(SkyPhenomenonType.GLACIAL_ICE_HALO, 0xBCE3FF, 0x7DD3FC, 0.65F)
                .driftingParticles(3, 0xCFEAFE, 0.55F, 75, 0.085F, -0.022F, 0.035F)
                .descentSpeedMultiplier(1.15F)
                .build());

        // =========================================================================
        // Representative Archetypes Pre-Wired for Upcoming Biomes:
        // =========================================================================

        // 2.2 Auroral Skerries — 12-block dense steam fog (lights extend visibility!) + #0E6A3B emerald night aurora sky
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.AURORAL_SKERRIES)
                .fogDistance(2.0F, 14.0F, 0.92F)
                .lightHeldFogRelief(18.0F)
                .fogColors(0xCBE7E5, 0x164E3B)
                .nightSkyColor(0x0E6A3B)
                .starBrightnessBoost(0.25F)
                .skyPhenomenon(SkyPhenomenonType.AURORA_CURTAINS, 0x10B981, 0x38BDF8, 0.85F)
                .driftingParticles(3, 0xE6F7F5, 0.95F, 65, 0.01F, 0.045F, 0.01F)
                .waterSurfaceSteam(7, 0xECFDFB)
                .volumetricMist(0xCBE7E5, 0x10B981, 0.12F, 5.5F, 0.62F, true, 0.85F)
                .waterOptics(WaterOpticalMode.AURORA_RIBBON_REFLECTION, 0x10B981, 0x38BDF8, 0.88F, 1.0F)
                .build());

        // 2.3 Frost-Bite Floes — Foul green rot mist + olive atmosphere + blight-ice halo + swamp-water hypothermia & rotten-ice poison
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.FROST_BITE_FLOES)
                .fogDistance(6.0F, 44.0F, 0.86F)
                .fogColors(0x5F7338, 0x242E16)
                .daySkyBrightnessScale(0.84F)
                .nightSkyColor(0x2A3318)
                .skyPhenomenon(SkyPhenomenonType.GLACIAL_ICE_HALO, 0x94B84D, 0x4D7C3F, 0.45F)
                .driftingParticles(3, 0x2B361E, 0.72F, 65, 0.032F, -0.016F, 0.022F)
                .volumetricMist(0x6C8240, 0x2F3E1D, 0.08F, 4.0F, 0.54F, false, 0.65F)
                .customServerTick(EntropicaBiomeAtmospheres::tickFrostBiteFloesHazard)
                .build());

        // 2.4 Crystalline Pack-Ice — Peaceful snowfield haze + violet night sky (#4E2A7A) + shimmering crystalline ice halo + prism glints
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.CRYSTALLINE_PACK_ICE)
                .fogDistance(16.0F, 128.0F, 0.60F)
                .fogColors(0xDCE4F7, 0x2E1A4A)
                .nightSkyColor(0x4E2A7A)
                .starBrightnessBoost(0.35F)
                .skyPhenomenon(SkyPhenomenonType.GLACIAL_ICE_HALO, 0xC084FC, 0x67E8F9, 0.75F)
                .driftingParticles(2, 0xE9D5FF, 0.50F, 75, 0.012F, -0.014F, 0.012F)
                .build());

        // 2.5 Silent Fjord — Deep eclipsed twilight canopy + cold grey fog over dark water + Eclipse Corona + Deep Shadows
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.SILENT_FJORD)
                .fogDistance(10.0F, 68.0F, 0.85F)
                .fogColors(0x3E4452, 0x0F111A)
                .daySkyBrightnessScale(0.42F)
                .nightSkyColor(0x1B0E3A)
                .starBrightnessBoost(0.45F)
                .skyPhenomenon(SkyPhenomenonType.ECLIPSE_CORONA, 0x6366F1, 0xBAE6FD, 0.78F)
                .driftingParticles(3, 0xCBD5E1, 0.55F, 85, 0.004F, -0.018F, 0.004F)
                .volumetricMist(0x475063, 0x1E1B3A, 0.15F, 4.8F, 0.48F, false, 0.45F)
                .waterOptics(WaterOpticalMode.FRESNEL_SKY_REFLECTION, 0x6366F1, 0xBAE6FD, 0.76F, 1.0F)
                .customServerTick(EntropicaBiomeAtmospheres::tickSilentFjordDeepShadows)
                .build());

        // 2.6 Steaming Skerries — Dense warm rose-mist steam haze + rising steam plumes + warm shallow pool thaw
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.STEAMING_SKERRIES)
                .fogDistance(4.0F, 26.0F, 0.88F)
                .lightHeldFogRelief(14.0F)
                .fogColors(0xDEC2C6, 0x3D262C)
                .nightSkyColor(0x3A2028)
                .driftingParticles(4, 0xF8FAFC, 0.92F, 65, 0.008F, 0.042F, 0.008F)
                .waterSurfaceSteam(8, 0xFFF7ED)
                .volumetricMist(0xE8D2D6, 0x52343C, 0.10F, 5.8F, 0.64F, false, 1.15F)
                .waterOptics(WaterOpticalMode.FRESNEL_SKY_REFLECTION, 0xF5D0FE, 0xFDE68A, 0.72F, 1.0F)
                .customServerTick(EntropicaBiomeAtmospheres::tickSteamingSkerriesWarmth)
                .build());

        // 2.7 Static Ice-Plains — Charged grey-yellow / slate-blue fog + high-altitude cloud lightning arcs + blue static sparks + metal-armor static buildup
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.STATIC_ICE_PLAINS)
                .fogDistance(16.0F, 112.0F, 0.68F)
                .fogColors(0x8A8870, 0x222634)
                .nightSkyColor(0x252838)
                .skyPhenomenon(SkyPhenomenonType.CLOUD_LIGHTNING_ARCS, 0x38BDF8, 0xFDE047, 0.78F)
                .driftingParticles(4, 0x38BDF8, 0.75F, 55, 0.045F, 0.012F, 0.035F)
                .customServerTick(EntropicaBiomeAtmospheres::tickStaticIcePlainsHazard)
                .build());

        // 2.8 Barrow-Mound Tundra — Cold rolling grey-violet barrow mist + overcast purple sky (#3C2B4C day, #1F162B night) + Necrotic Cold undead empowerment
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.BARROW_MOUND_TUNDRA)
                .fogDistance(8.0F, 64.0F, 0.84F)
                .lightHeldFogRelief(10.0F)
                .fogColors(0x5B4B6E, 0x1E162B)
                .daySkyBrightnessScale(0.76F)
                .nightSkyColor(0x1F162B)
                .driftingParticles(3, 0xA78BFA, 0.65F, 75, 0.018F, -0.010F, 0.014F)
                .volumetricMist(0x6D5B82, 0x2A1F3D, 0.05F, 3.4F, 0.56F, false, 0.55F)
                .customServerTick(EntropicaBiomeAtmospheres::tickBarrowMoundTundraHazard)
                .build());

        // 2.9 Null-Fringe Bay — Deep indigo-violet sky (#150E26), Void Rift sky fractures, weak shoreline gravity
        //     (Jump Boost I + 0.80x gentle descent), drifting violet motes, and outward-expanding purple void rings over the bay
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.NULL_FRINGE_BAY)
                .fogDistance(10.0F, 80.0F, 0.80F)
                .lightHeldFogRelief(12.0F)
                .fogColors(0x3B235A, 0x150E26)
                .daySkyBrightnessScale(0.68F)
                .nightSkyColor(0x150E26)
                .starBrightnessBoost(0.45F)
                .skyPhenomenon(SkyPhenomenonType.VOID_RIFT_FRACTURES, 0x9333EA, 0x38BDF8, 0.85F)
                .driftingParticles(3, 0xC084FC, 0.65F, 80, 0.012F, 0.022F, 0.012F)
                .volumetricMist(0x5B358C, 0x7E22CE, 0.12F, 3.8F, 0.48F, true, 0.75F)
                .waterOptics(WaterOpticalMode.STARLIGHT_PARALLAX, 0x9333EA, 0x38BDF8, 0.88F, 1.15F)
                .jumpBoost(0)
                .descentSpeedMultiplier(0.80F)
                .customServerTick(EntropicaBiomeAtmospheres::tickNullFringeBayAnomaly)
                .build());

        // 2.10 Radiant Glaze-Plains — Warm golden-amber sky (#A67E44), golden mist fog (#FDE68A),
        //      champagne-gold & rose-vitae aurora ribbons, floating golden dust motes, & Sanctuary Warmth
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.RADIANT_GLAZE_PLAINS)
                .fogDistance(18.0F, 136.0F, 0.65F)
                .fogColors(0xFDE68A, 0x4A371C)
                .nightSkyColor(0x4A371C)
                .starBrightnessBoost(0.32F)
                .skyPhenomenon(SkyPhenomenonType.AURORA_CURTAINS, 0xFDE68A, 0xFF6B9D, 0.82F)
                .driftingParticles(3, 0xFACC48, 0.62F, 80, 0.008F, 0.014F, 0.008F)
                .waterSurfaceSteam(4, 0xFEF3C7)
                .volumetricMist(0xFEF08A, 0x78552B, 0.15F, 3.6F, 0.36F, true, 0.60F)
                .waterOptics(WaterOpticalMode.FRESNEL_SKY_REFLECTION, 0xFDE68A, 0xFF6B9D, 0.82F, 1.0F)
                .passiveSanctuaryRegen(120)
                .customServerTick(EntropicaBiomeAtmospheres::tickRadiantGlazePlainsSanctuary)
                .build());

        // 2.11 Storm-Swept Icebergs — Dark slate-storm sky (#2D3238), cloud-lightning arcs,
        //      horizontal wind-driven spray, night observatory deck clarity, and storm peak fulgurite strikes
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.STORM_SWEPT_ICEBERGS)
                .fogDistance(14.0F, 96.0F, 0.74F)
                .lightHeldFogRelief(10.0F)
                .fogColors(0x2D3238, 0x14181F)
                .daySkyBrightnessScale(0.74F)
                .nightSkyColor(0x14181F)
                .starBrightnessBoost(0.35F)
                .skyPhenomenon(SkyPhenomenonType.CLOUD_LIGHTNING_ARCS, 0x38BDF8, 0xF8FAFC, 0.86F)
                .driftingParticles(5, 0xF1F5F9, 0.80F, 60, 0.085F, 0.006F, 0.045F)
                .customServerTick(EntropicaBiomeAtmospheres::tickStormSweptIcebergs)
                .build());

        // 2.12 Spore-Glazed Pack — Pale yellow-green sky (#6B7C50), dense yellow-chartreuse spore mist fog (#7A8A5A),
        //      dark mossy-teal bay water (#355C52 / #1E3832), glacial spore-halo, and drifting golden-green spore motes
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.SPORE_GLAZED_PACK)
                .fogDistance(8.0F, 64.0F, 0.84F)
                .lightHeldFogRelief(12.0F)
                .fogColors(0x7A8A5A, 0x263119)
                .daySkyBrightnessScale(0.90F)
                .nightSkyColor(0x243018)
                .skyPhenomenon(SkyPhenomenonType.GLACIAL_ICE_HALO, 0xB8D860, 0x7ED4BC, 0.55F)
                .driftingParticles(4, 0xBCE258, 0.68F, 75, 0.014F, -0.008F, 0.012F)
                .volumetricMist(0xA3BE5A, 0x3F5224, 0.10F, 4.4F, 0.52F, false, 0.60F)
                .build());

        // 2.13 Mirage Glacier — White-hot orange sky (#A8592E), dry golden dust-haze fog (0xD49B54 day / 0x3E2416 night),
        //      close heat-distortion fog distance, Mirage Heat Shimmer ribbons, shimmering rising heat motes (0xFDBA74),
        //      and midday sun heat-mirage Double Vision shimmer
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.MIRAGE_GLACIER)
                .fogDistance(8.0F, 48.0F, 0.84F)
                .fogColors(0xD49B54, 0x3E2416)
                .nightSkyColor(0x3E2416)
                .starBrightnessBoost(0.30F)
                .skyPhenomenon(SkyPhenomenonType.MIRAGE_HEAT_SHIMMER, 0xFDBA74, 0xFB923C, 0.78F)
                .driftingParticles(4, 0xFDBA74, 0.65F, 70, 0.012F, 0.028F, 0.010F)
                .waterOptics(WaterOpticalMode.MIRAGE_SHIMMER_REFLECTION, 0xFDBA74, 0xFB923C, 0.84F, 1.0F)
                .customServerTick(EntropicaBiomeAtmospheres::tickMirageGlacierHeatGlare)
                .build());

        // 2.14 Oasis Floes — Warm humid mist (16..96 clear thin mist, 0xE8C69A day / 0x4A3320 night),
        //      0x4A3320 warm night sky glow, 3 gentle rising water-vapor plumes above warm Amber Springs,
        //      warm golden resin particles (0xFBBF24), and peaceful thermal sanctuary thawing & wading Regeneration!
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.OASIS_FLOES)
                .fogDistance(16.0F, 96.0F, 0.68F)
                .fogColors(0xE8C69A, 0x4A3320)
                .nightSkyColor(0x4A3320)
                .starBrightnessBoost(0.28F)
                .driftingParticles(3, 0xFBBF24, 0.62F, 75, 0.008F, 0.012F, 0.008F)
                .waterSurfaceSteam(3, 0xFFF7ED)
                .volumetricMist(0xF3D9B5, 0x6B4A2E, 0.12F, 4.2F, 0.44F, false, 0.70F)
                .waterOptics(WaterOpticalMode.FRESNEL_SKY_REFLECTION, 0xFDE68A, 0xFBBF24, 0.75F, 1.0F)
                .customServerTick(EntropicaBiomeAtmospheres::tickOasisFloesSanctuary)
                .build());

        // 2.15 Dust-Crusted Bergs — Dry dust haze (0x9E8872 day / 0x2E2824 night), 0x3A2E26 dusty night sky,
        //      dusty Glacial Ice Halo, constantly drifting/falling grey dust particles, and blowing-sand visibility hazard
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.DUST_CRUSTED_BERGS)
                .fogDistance(8.0F, 62.0F, 0.86F)
                .lightHeldFogRelief(14.0F)
                .fogColors(0x9E8872, 0x2E2824)
                .nightSkyColor(0x3A2E26)
                .skyPhenomenon(SkyPhenomenonType.GLACIAL_ICE_HALO, 0xC5CBD4, 0xD6B48A, 0.55F)
                .driftingParticles(5, 0x9CA3AF, 0.78F, 65, 0.095F, -0.025F, 0.045F)
                .customServerTick(EntropicaBiomeAtmospheres::tickDustCrustedBergsHazard)
                .build());

        // 2.16 Blood-Ice Shallows — Crimson twilight sky (#6A242B), warm iron-rose mist fog (#8C353D day / #3A0C14 night),
        //      blood-wine water (#5E1722) & deep garnet water fog (#3A0C14), crimson Glacial Ice Halo,
        //      gentle rising crimson mist/spore ambient particles, and benign coastal atmosphere (ZERO passive hazard)
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.BLOOD_ICE_SHALLOWS)
                .fogDistance(12.0F, 84.0F, 0.78F)
                .lightHeldFogRelief(12.0F)
                .fogColors(0x8C353D, 0x3A0C14)
                .daySkyBrightnessScale(0.86F)
                .nightSkyColor(0x6A242B)
                .starBrightnessBoost(0.24F)
                .skyPhenomenon(SkyPhenomenonType.GLACIAL_ICE_HALO, 0xE11D48, 0xFDA4AF, 0.58F)
                .driftingParticles(3, 0xC83E4D, 0.65F, 75, 0.006F, 0.018F, 0.006F)
                .waterSurfaceSteam(3, 0x9E2A38)
                .volumetricMist(0xA83E48, 0x5E1722, 0.10F, 4.2F, 0.50F, false, 0.65F)
                .waterOptics(WaterOpticalMode.CRIMSON_TIDE_GLINT, 0xE11D48, 0xFDA4AF, 0.86F, 1.0F)
                .build());

        // 2.17 Astral Skerries — Deep starlight night sky (#1C1A38), Astral Stardust Veil,
        //      thin glowing purple volumetric mist hovering over water/ice, and sub-surface Starlight Parallax water
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.ASTRAL_SKERRIES)
                .fogDistance(10.0F, 68.0F, 0.55F)
                .fogColors(0x4A3878, 0x2A1B4E)
                .nightSkyColor(0x1C1A38)
                .starBrightnessBoost(0.75F)
                .skyPhenomenon(SkyPhenomenonType.ASTRAL_STARDUST_VEIL, 0x9333EA, 0x7DD3FC, 0.85F)
                .driftingParticles(10, 0xA855F7, 0.65F, 80, 0.008F, -0.006F, 0.008F)
                .volumetricMist(0x7E57C2, 0x9333EA, 0.15F, 4.2F, 0.46F, true, 1.0F)
                .waterOptics(WaterOpticalMode.STARLIGHT_PARALLAX, 0xA855F7, 0x7DD3FC, 0.92F, 1.0F)
                .build());

        // 2.18 Aura-Glazed Peak — Zephyr Peaks weak gravity (Jump Boost I) + Aurora Curtains
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.AURA_GLAZED_PEAK)
                .skyPhenomenon(SkyPhenomenonType.AURORA_CURTAINS, 0x38BDF8, 0xC084FC, 0.70F)
                .jumpBoost(0)
                .build());
    }

    public static synchronized void register(BiomeAtmosphereProfile profile) {
        BY_BIOME.put(profile.getBiome(), profile);
        BY_PATH.put(profile.getBiome().getId(), profile);
    }

    public static BiomeAtmosphereProfile get(EntropicaBiome biome) {
        registerAll();
        return BY_BIOME.get(biome);
    }

    public static BiomeAtmosphereProfile getForHolder(Holder<Biome> holder) {
        if (holder == null) {
            return null;
        }
        registerAll();
        Optional<ResourceKey<Biome>> keyOpt = holder.unwrapKey();
        if (keyOpt.isEmpty()) {
            return null;
        }
        ResourceKey<Biome> key = keyOpt.get();
        if (!Entropica.MODID.equals(key.location().getNamespace())) {
            return null;
        }
        return BY_PATH.get(key.location().getPath());
    }

    public static Map<EntropicaBiome, BiomeAtmosphereProfile> allProfiles() {
        registerAll();
        return Collections.unmodifiableMap(BY_BIOME);
    }

    private static void tickFrostBiteFloesHazard(ServerLevel level, LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity.tickCount % 20 != 0) {
            return;
        }

        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return;
            }
        } else {
            if (entity instanceof CryoStalkerEntity || entity instanceof GrotEntity) {
                return;
            }
            if (entity.getType().is(EntityTypeTags.UNDEAD)
                    || entity.getType().is(EntityTypeTags.AQUATIC)
                    || entity.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER)
                    || entity instanceof WaterAnimal) {
                return;
            }
        }

        boolean cryoProtected = hasCryoBoots(entity) || hasFullCryoInsulation(entity) || hasRadiantFreezeImmunity(entity);
        if (cryoProtected) {
            return;
        }

        // 1. Swamp-Water Hypothermia
        if (entity.isInWater()) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 55, 0, true, true, true));
            if (entity.canFreeze()) {
                int maxFrozen = entity.getTicksRequiredToFreeze() + 40;
                entity.setTicksFrozen(Math.min(maxFrozen, entity.getTicksFrozen() + 25));
            }
            level.sendParticles(ParticleTypes.ASH,
                    entity.getX(), entity.getY() + 0.45D, entity.getZ(),
                    4, 0.28D, 0.18D, 0.28D, 0.01D);
            level.sendParticles(ParticleTypes.SQUID_INK,
                    entity.getX(), entity.getY() + 0.25D, entity.getZ(),
                    2, 0.22D, 0.12D, 0.22D, 0.005D);
        }

        // 2. Rotten Ice Poison
        BlockPos onPos = entity.getOnPos();
        if (isRottenIce(level.getBlockState(onPos))
                || (entity.onGround() && isRottenIce(level.getBlockState(entity.blockPosition().below())))) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 65, 0, true, true, true));
        }
    }

    private static boolean isRottenIce(BlockState state) {
        Optional<Block> rottenIceOpt = BuiltInRegistries.BLOCK.getOptional(ROTTEN_ICE_ID);
        return rottenIceOpt.isPresent() && state.is(rottenIceOpt.get());
    }

    private static boolean hasCryoBoots(LivingEntity entity) {
        return matchesItemSlot(entity, EquipmentSlot.FEET, CRYO_BOOTS_ID);
    }

    private static boolean hasFullCryoInsulation(LivingEntity entity) {
        return matchesItemSlot(entity, EquipmentSlot.HEAD, CRYO_HELMET_ID)
                && matchesItemSlot(entity, EquipmentSlot.CHEST, CRYO_CHESTPLATE_ID)
                && matchesItemSlot(entity, EquipmentSlot.LEGS, CRYO_LEGGINGS_ID)
                && matchesItemSlot(entity, EquipmentSlot.FEET, CRYO_BOOTS_ID);
    }

    private static boolean matchesItemSlot(LivingEntity entity, EquipmentSlot slot, ResourceLocation itemId) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }
        Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(itemId);
        return opt.isPresent() && stack.is(opt.get());
    }

    public static void tickSilentFjordDeepShadows(ServerLevel level, LivingEntity entity) {
        if (level == null || !(entity instanceof Player player) || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if ((level.getGameTime() + player.getId()) % 20L != 0L) {
            return;
        }

        // 1. Monster Deep-Shadow Breeding/Empowerment within 24 blocks of player
        AABB searchBox = player.getBoundingBox().inflate(24.0D);
        double maxDistSqr = 24.0D * 24.0D;
        ResourceKey<Biome> silentFjordKey = EntropicaBiome.SILENT_FJORD.resourceKey();

        for (Monster monster : level.getEntitiesOfClass(Monster.class, searchBox, LivingEntity::isAlive)) {
            if (monster.distanceToSqr(player) > maxDistSqr) {
                continue;
            }
            BlockPos monsterPos = monster.blockPosition();
            if (!level.getBiome(monsterPos).is(silentFjordKey)) {
                continue;
            }
            if (level.getBrightness(LightLayer.BLOCK, monsterPos) <= 5) {
                monster.addEffect(new MobEffectInstance(MobEffects.SPEED, 50, 0, true, false));
                if (monster.isOnFire() && level.isBrightOutside() && !monster.isInLava()) {
                    monster.clearFire();
                }
            }
        }

        // 2. Player Cliff-Shadow Chill (survival/adventure players in deep shadow without light or protective footwear)
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        BlockPos playerPos = player.blockPosition();
        if (level.getBrightness(LightLayer.BLOCK, playerPos) <= 2
                && level.getMaxLocalRawBrightness(playerPos) <= 6
                && !isHoldingFjordLightItem(player)
                && !hasFjordFootwearProtection(player)
                && !hasRadiantFreezeImmunity(player)) {
            int maxFrozen = player.getTicksRequiredToFreeze() + 20;
            player.setTicksFrozen(Math.min(maxFrozen, player.getTicksFrozen() + 12));
        }
    }

    private static boolean isHoldingFjordLightItem(Player player) {
        return isLightEmittingStack(player.getMainHandItem()) || isLightEmittingStack(player.getOffhandItem());
    }

    private static boolean isLightEmittingStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().getLightEmission() > 6) {
            return true;
        }
        return stack.is(Items.TORCH)
                || stack.is(Items.SOUL_TORCH)
                || stack.is(Items.REDSTONE_TORCH)
                || stack.is(Items.LANTERN)
                || stack.is(Items.SOUL_LANTERN)
                || stack.is(Items.LAVA_BUCKET)
                || stack.is(Items.GLOWSTONE_DUST)
                || stack.is(Items.BLAZE_ROD);
    }

    private static boolean hasFjordFootwearProtection(Player player) {
        ItemStack feetStack = player.getItemBySlot(EquipmentSlot.FEET);
        if (feetStack.isEmpty()) {
            return false;
        }
        ResourceLocation feetKey = BuiltInRegistries.ITEM.getKey(feetStack.getItem());
        return SILENT_STRIDERS_ID.equals(feetKey) || CRYO_BOOTS_ID.equals(feetKey);
    }

    private static void tickSteamingSkerriesWarmth(ServerLevel level, LivingEntity entity) {
        if (level == null || !(entity instanceof Player player) || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if ((level.getGameTime() + player.getId()) % 10L != 0L) {
            return;
        }
        int frozen = player.getTicksFrozen();
        if (frozen <= 0) {
            return;
        }

        boolean inWarmShallowPool = player.isInWater() && player.getY() >= 57.0D && player.getY() <= 70.0D;
        boolean nearGeothermalHeat = inWarmShallowPool || isNearSteamingHeatBlock(level, player.blockPosition());
        if (nearGeothermalHeat) {
            player.setTicksFrozen(Math.max(0, frozen - 30));
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.5D, player.getZ(),
                    2, 0.25D, 0.15D, 0.25D, 0.01D);
        }
    }

    private static boolean isNearSteamingHeatBlock(ServerLevel level, BlockPos center) {
        Optional<Block> steamVentOpt = BuiltInRegistries.BLOCK.getOptional(STEAM_VENT_ID);
        Optional<Block> springStoneOpt = BuiltInRegistries.BLOCK.getOptional(STEAMING_SPRING_STONE_ID);
        if (steamVentOpt.isEmpty() && springStoneOpt.isEmpty()) {
            return false;
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (steamVentOpt.isPresent() && state.is(steamVentOpt.get())) {
                        return true;
                    }
                    if (springStoneOpt.isPresent() && state.is(springStoneOpt.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void tickStaticIcePlainsHazard(ServerLevel level, LivingEntity entity) {
        if (level == null || !(entity instanceof Player player) || !player.isAlive()
                || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (entity.tickCount % 20 != 0) {
            return;
        }

        UUID playerId = player.getUUID();
        boolean wearingMetal = isWearingConductiveMetalArmor(player);
        boolean movingHorizontally = player.getDeltaMovement().horizontalDistanceSqr() > 0.0025D
                || player.getKnownMovement().horizontalDistanceSqr() > 0.0025D
                || player.isSprinting();

        // Exemption 1: Grounding Coil anywhere in inventory or offhand (charges "StoredStatic": true when moving in metal armor!)
        boolean hasGroundingCoil = checkAndChargeGroundingCoil(player, wearingMetal && movingHorizontally);

        // Exemption 2: Galvanic Surge effect, insulated footwear (cryo_insulated_boots or leather_boots), or standing on/above static_conductive_mat
        boolean groundedByGearOrFloor = hasGroundingCoil
                || hasGalvanicSurgeEffect(player)
                || hasStaticInsulatedBoots(player)
                || isStandingOnStaticConductiveMat(level, player);

        if (!wearingMetal || !movingHorizontally || groundedByGearOrFloor) {
            decayStaticCharge(playerId);
            return;
        }

        int currentSeconds = STATIC_CHARGE_SECONDS.getOrDefault(playerId, 0);
        int nextSeconds = currentSeconds + 1;
        if (nextSeconds >= 30) {
            STATIC_CHARGE_SECONDS.remove(playerId);
            player.hurtServer(level, level.damageSources().lightningBolt(), 2.0F);
            level.playSound(null, player.blockPosition(), SoundEvents.BEE_STING, SoundSource.PLAYERS, 0.55F, 1.35F);
            level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.25F, 1.65F);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 0.9D, player.getZ(),
                    18, 0.35D, 0.45D, 0.35D, 0.08D);
        } else {
            STATIC_CHARGE_SECONDS.put(playerId, nextSeconds);
            if (nextSeconds >= 25) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        player.getX(), player.getY() + 0.2D, player.getZ(),
                        3, 0.22D, 0.15D, 0.22D, 0.03D);
            }
        }
    }

    public static int getStaticChargeSeconds(UUID playerId) {
        return playerId == null ? 0 : STATIC_CHARGE_SECONDS.getOrDefault(playerId, 0);
    }

    private static void decayStaticCharge(UUID playerId) {
        Integer cur = STATIC_CHARGE_SECONDS.get(playerId);
        if (cur == null || cur <= 1) {
            STATIC_CHARGE_SECONDS.remove(playerId);
        } else {
            STATIC_CHARGE_SECONDS.put(playerId, cur - 1);
        }
    }

    private static boolean isWearingConductiveMetalArmor(Player player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String path = key.getPath();
            if (path.startsWith("iron_") || path.startsWith("golden_") || path.startsWith("chainmail_")) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkAndChargeGroundingCoil(Player player, boolean chargeIfUncharged) {
        boolean foundCoil = false;
        boolean chargedThisTick = false;

        ItemStack offhand = player.getOffhandItem();
        if (isGroundingCoilStack(offhand)) {
            foundCoil = true;
            if (chargeIfUncharged && tryStoreStaticInCoil(offhand)) {
                chargedThisTick = true;
            }
        }

        var inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (isGroundingCoilStack(stack)) {
                foundCoil = true;
                if (chargeIfUncharged && !chargedThisTick && tryStoreStaticInCoil(stack)) {
                    chargedThisTick = true;
                }
            }
        }
        return foundCoil;
    }

    private static boolean isGroundingCoilStack(ItemStack stack) {
        return !stack.isEmpty() && GROUNDING_COIL_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean tryStoreStaticInCoil(ItemStack coilStack) {
        CustomData customData = coilStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.getBoolean("StoredStatic").orElse(false)) {
            return false;
        }
        tag.putBoolean("StoredStatic", true);
        coilStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return true;
    }

    private static boolean hasGalvanicSurgeEffect(Player player) {
        Optional<MobEffect> surgeOpt = BuiltInRegistries.MOB_EFFECT.getOptional(GALVANIC_SURGE_ID);
        return surgeOpt.isPresent() && player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(surgeOpt.get()));
    }

    private static boolean hasStaticInsulatedBoots(Player player) {
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        if (feet.isEmpty()) {
            return false;
        }
        if (feet.is(Items.LEATHER_BOOTS)) {
            return true;
        }
        return CRYO_BOOTS_ID.equals(BuiltInRegistries.ITEM.getKey(feet.getItem()));
    }

    private static boolean isStandingOnStaticConductiveMat(ServerLevel level, Player player) {
        Optional<Block> matOpt = BuiltInRegistries.BLOCK.getOptional(STATIC_CONDUCTIVE_MAT_ID);
        if (matOpt.isEmpty()) {
            return false;
        }
        Block matBlock = matOpt.get();
        BlockPos feetPos = player.blockPosition();
        return level.getBlockState(feetPos).is(matBlock)
                || level.getBlockState(player.getOnPos()).is(matBlock)
                || level.getBlockState(feetPos.below()).is(matBlock)
                || level.getBlockState(feetPos.below(2)).is(matBlock);
    }

    public static void tickBarrowMoundTundraHazard(ServerLevel level, LivingEntity entity) {
        if (level == null || entity == null || !entity.isAlive()) {
            return;
        }
        if ((level.getGameTime() + entity.getId()) % 20L != 0L) {
            return;
        }

        ResourceKey<Biome> barrowTundraKey = EntropicaBiome.BARROW_MOUND_TUNDRA.resourceKey();

        // 1. Direct Necrotic Cold Undead Empowerment (Strength I + overcast purple tundra sun-shade)
        if (entity.getType().is(EntityTypeTags.UNDEAD)) {
            entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 55, 0, true, false));
            if (entity.isOnFire() && level.isBrightOutside() && !entity.isInLava()
                    && level.getBiome(entity.blockPosition()).is(barrowTundraKey)) {
                entity.clearFire();
            }
            return;
        }

        // 2. Player-centered Undead Empowerment scan within 24 blocks so nearby Undead are empowered immediately
        if (entity instanceof Player player && !player.isSpectator()) {
            AABB searchBox = player.getBoundingBox().inflate(24.0D);
            double maxDistSqr = 24.0D * 24.0D;
            for (Monster monster : level.getEntitiesOfClass(Monster.class, searchBox,
                    m -> m.isAlive() && m.getType().is(EntityTypeTags.UNDEAD))) {
                if (monster.distanceToSqr(player) > maxDistSqr) {
                    continue;
                }
                if (!level.getBiome(monster.blockPosition()).is(barrowTundraKey)) {
                    continue;
                }
                monster.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 55, 0, true, false));
                if (monster.isOnFire() && level.isBrightOutside() && !monster.isInLava()) {
                    monster.clearFire();
                }
            }
        }
    }

    public static void tickNullFringeBayAnomaly(ServerLevel level, LivingEntity entity) {
        if (level == null || !(entity instanceof Player player) || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if ((level.getGameTime() + player.getId()) % 60L != 0L) {
            return;
        }

        Optional<Block> voidSandOpt = BuiltInRegistries.BLOCK.getOptional(VOID_SAND_ID);
        Optional<Block> voidSandTurfOpt = BuiltInRegistries.BLOCK.getOptional(VOID_SAND_TURF_ID);
        RandomSource random = level.getRandom();
        BlockPos playerPos = player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 4; attempt++) {
            int dx = random.nextInt(25) - 12;
            int dz = random.nextInt(25) - 12;
            if (dx * dx + dz * dz > 144) {
                continue;
            }
            int tx = playerPos.getX() + dx;
            int tz = playerPos.getZ() + dz;
            if (!level.hasChunk(tx >> 4, tz >> 4)) {
                continue;
            }

            int foundSurfaceY = Integer.MIN_VALUE;
            int maxScanY = Math.min(level.getMaxY() - 2, playerPos.getY() + 5);
            int minScanY = Math.max(level.getMinY() + 1, playerPos.getY() - 9);
            for (int sy = maxScanY; sy >= minScanY; sy--) {
                cursor.set(tx, sy, tz);
                BlockState state = level.getBlockState(cursor);
                if (isNullBayRingSurface(state, voidSandOpt, voidSandTurfOpt)) {
                    BlockState above = level.getBlockState(cursor.above());
                    if (above.isAir() || above.is(Blocks.SNOW) || above.canBeReplaced() && above.getFluidState().isEmpty()) {
                        foundSurfaceY = sy;
                        break;
                    }
                }
            }

            if (foundSurfaceY == Integer.MIN_VALUE) {
                int hmY = level.getHeight(Heightmap.Types.WORLD_SURFACE, tx, tz) - 1;
                if (hmY > level.getMinY()) {
                    cursor.set(tx, hmY, tz);
                    BlockState hmState = level.getBlockState(cursor);
                    if (isNullBayRingSurface(hmState, voidSandOpt, voidSandTurfOpt)) {
                        foundSurfaceY = hmY;
                    }
                }
            }

            if (foundSurfaceY != Integer.MIN_VALUE) {
                double centerX = tx + 0.5D;
                double ringY = foundSurfaceY + 1.08D;
                double centerZ = tz + 0.5D;
                double radius = 2.2D;

                for (int i = 0; i < 12; i++) {
                    double angle = (Math.PI * 2.0D * i) / 12.0D;
                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    double px = centerX + cos * radius;
                    double pz = centerZ + sin * radius;
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, px, ringY, pz, 0, cos, 0.08D, sin, 0.06D);
                    level.sendParticles(ParticleTypes.PORTAL, px, ringY, pz, 1, 0.12D, 0.04D, 0.12D, 0.05D);
                }
                break;
            }
        }
    }

    private static boolean isNullBayRingSurface(
            BlockState state,
            Optional<Block> voidSandOpt,
            Optional<Block> voidSandTurfOpt
    ) {
        if (state.is(Blocks.WATER) || state.getFluidState().getType() == Fluids.WATER) {
            return true;
        }
        if (voidSandOpt.isPresent() && state.is(voidSandOpt.get())) {
            return true;
        }
        return voidSandTurfOpt.isPresent() && state.is(voidSandTurfOpt.get());
    }

    /**
     * Sanctuary tick for {@code 2.10 Radiant Glaze-Plains}: zero cold/freezing hazard (passively thaws
     * frozen entities to {@code 0}) and grants gentle sanctuary warmth every {@code 120} ticks for
     * non-starving injured players.
     */
    public static void tickRadiantGlazePlainsSanctuary(ServerLevel level, LivingEntity entity) {
        if (level == null || entity == null || !entity.isAlive() || entity.isSpectator()) {
            return;
        }
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
        if (entity instanceof Player player) {
            if ((level.getGameTime() + player.getId()) % 120L == 0L
                    && player.getFoodData().getFoodLevel() > 0
                    && player.getHealth() < player.getMaxHealth()) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false, true));
            }
        }
    }

    /**
     * Returns {@code true} if the entity is inside {@code Radiant Glaze-Plains}, wearing
     * {@code entropica:qilin_prism_circlet}, or standing on {@code entropica:radiant_snow_block}.
     */
    public static boolean hasRadiantFreezeImmunity(LivingEntity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (matchesItemSlot(entity, EquipmentSlot.HEAD, QILIN_PRISM_CIRCLET_ID)) {
            return true;
        }
        var level = entity.level();
        BlockPos feetPos = entity.blockPosition();
        if (level.getBiome(feetPos).is(EntropicaBiome.RADIANT_GLAZE_PLAINS.resourceKey())
                || level.getBiome(feetPos).is(EntropicaBiome.OASIS_FLOES.resourceKey())) {
            return true;
        }
        Optional<MobEffect> oasisRenewalOpt = BuiltInRegistries.MOB_EFFECT.getOptional(OASIS_RENEWAL_ID);
        if (oasisRenewalOpt.isPresent()
                && entity.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(oasisRenewalOpt.get()))) {
            return true;
        }
        Optional<Block> radiantSnowOpt = BuiltInRegistries.BLOCK.getOptional(RADIANT_SNOW_BLOCK_ID);
        if (radiantSnowOpt.isPresent()) {
            Block radiantSnow = radiantSnowOpt.get();
            return level.getBlockState(feetPos).is(radiantSnow)
                    || level.getBlockState(entity.getOnPos()).is(radiantSnow)
                    || level.getBlockState(feetPos.below()).is(radiantSnow);
        }
        return false;
    }

    /**
     * Atmosphere tick for {@code 2.11 Storm-Swept Icebergs}:
     * <ul>
     *   <li><b>Night Observatory Deck Clarity</b>: at night when {@code Y >= 74} and standing on/near
     *       {@code silver_pine_planks} or {@code zephyr_ice}, grants ambient {@code Night Vision}
     *       ({@code 280} ticks) and {@code Slow Falling} ({@code 100} ticks).</li>
     *   <li><b>Storm Peak Lightning &amp; Fulgurite Charging</b>: during thunderstorms, occasionally
     *       strikes exposed iceberg summits ({@code Y >= 68}) {@code 18..44} blocks from the player,
     *       converting {@code 1-4} nearby ice blocks into {@code entropica:fulgurite_ice} and stone
     *       or gravel blocks into {@code entropica:fulgurite_block}.</li>
     * </ul>
     */
    public static void tickStormSweptIcebergs(ServerLevel level, LivingEntity entity) {
        if (level == null || entity == null || !entity.isAlive() || entity.tickCount % 20 != 0) {
            return;
        }
        if (!(entity instanceof Player player) || player.isSpectator()) {
            return;
        }

        // 1. Night Observatory Deck Clarity
        if (!level.isBrightOutside() && player.getY() >= 74.0D && isStandingNearObservatoryDeckOrZephyrIce(level, player)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 280, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, false, true));
        }

        // 2. Storm Peak Lightning & Fulgurite Charging
        if (level.isThundering() && level.random.nextInt(14) == 0) {
            RandomSource random = level.random;
            double angle = random.nextDouble() * (Math.PI * 2.0D);
            double dist = 18.0D + random.nextDouble() * 26.0D; // 18..44 blocks from player
            int sx = player.getBlockX() + (int) Math.round(Math.cos(angle) * dist);
            int sz = player.getBlockZ() + (int) Math.round(Math.sin(angle) * dist);
            if (!level.hasChunk(sx >> 4, sz >> 4)) {
                return;
            }

            int strikeY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, sx, sz);
            BlockPos strikePos = new BlockPos(sx, strikeY, sz);
            if (strikePos.getY() >= 68) {
                LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
                bolt.setPos(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                level.addFreshEntity(bolt);

                Optional<Block> fulguriteIceOpt = BuiltInRegistries.BLOCK.getOptional(FULGURITE_ICE_ID);
                Optional<Block> fulguriteBlockOpt = BuiltInRegistries.BLOCK.getOptional(FULGURITE_BLOCK_ID);
                if (fulguriteIceOpt.isPresent() || fulguriteBlockOpt.isPresent()) {
                    int targetConversions = 1 + random.nextInt(4); // 1..4 blocks
                    int converted = 0;
                    BlockPos impactCenter = strikePos.below();
                    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

                    for (int attempt = 0; attempt < 16 && converted < targetConversions; attempt++) {
                        int ox = (attempt == 0) ? 0 : (random.nextInt(5) - 2);
                        int oy = (attempt == 0) ? 0 : (random.nextInt(3) - 1);
                        int oz = (attempt == 0) ? 0 : (random.nextInt(5) - 2);
                        cursor.set(impactCenter.getX() + ox, impactCenter.getY() + oy, impactCenter.getZ() + oz);
                        if (!level.isLoaded(cursor)) {
                            continue;
                        }
                        BlockState curState = level.getBlockState(cursor);
                        if (fulguriteIceOpt.isPresent()
                                && (curState.is(Blocks.ICE) || curState.is(Blocks.PACKED_ICE) || curState.is(Blocks.BLUE_ICE))) {
                            level.setBlock(cursor, fulguriteIceOpt.get().defaultBlockState(), 3);
                            converted++;
                        } else if (fulguriteBlockOpt.isPresent()
                                && (curState.is(Blocks.GRAVEL) || curState.is(Blocks.STONE) || curState.is(Blocks.COBBLESTONE))) {
                            level.setBlock(cursor, fulguriteBlockOpt.get().defaultBlockState(), 3);
                            converted++;
                        }
                    }
                }
            }
        }
    }

    private static boolean isStandingNearObservatoryDeckOrZephyrIce(ServerLevel level, Player player) {
        Optional<Block> planksOpt = BuiltInRegistries.BLOCK.getOptional(SILVER_PINE_PLANKS_ID);
        Optional<Block> zephyrIceOpt = BuiltInRegistries.BLOCK.getOptional(ZEPHYR_ICE_ID);
        if (planksOpt.isEmpty() && zephyrIceOpt.isEmpty()) {
            return false;
        }
        BlockPos feet = player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    cursor.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (planksOpt.isPresent() && state.is(planksOpt.get())) {
                        return true;
                    }
                    if (zephyrIceOpt.isPresent() && state.is(zephyrIceOpt.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Atmosphere tick for {@code 2.13 Mirage Glacier}: exposed players under open midday glare
     * without {@code entropica:mirage_veil_goggles} or a nearby {@code entropica:mirage_prism_lantern}
     * occasionally (every {@code 300} ticks in bright sun) catch a brief {@code 5s} ({@code 100} ticks)
     * {@code entropica:double_vision} heat-mirage shimmer.
     */
    public static void tickMirageGlacierHeatGlare(ServerLevel level, LivingEntity entity) {
        if (level == null || !(entity instanceof Player player) || !player.isAlive()
                || player.isCreative() || player.isSpectator()) {
            return;
        }
        if ((level.getGameTime() + player.getId()) % 300L != 0L) {
            return;
        }
        if (!level.isBrightOutside() || level.isRaining() || level.isThundering()) {
            return;
        }
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 1000L || dayTime > 11000L) {
            return;
        }

        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!level.canSeeSky(eyePos)) {
            return;
        }
        if (EntropicaBiomeEnvironmentHandler.isWearingMirageVeilGoggles(player)) {
            return;
        }
        Optional<MobEffect> doubleVisionOpt = BuiltInRegistries.MOB_EFFECT.getOptional(DOUBLE_VISION_ID);
        if (doubleVisionOpt.isEmpty()) {
            return;
        }
        if (EntropicaBiomeEnvironmentHandler.isNearMiragePrismLantern(level, eyePos, 12)) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(doubleVisionOpt.get()),
                100,
                0,
                true,
                true,
                true
        ));
    }

    /**
     * Peaceful thermal sanctuary tick for {@code 2.14 Oasis Floes}: clears {@code TicksFrozen}
     * ({@code setTicksFrozen(0)}) on players/entities in the biome and grants {@code 100t}
     * {@link MobEffects#REGENERATION} (every {@code 200} ticks) when wading in warm spring water.
     */
    public static void tickOasisFloesSanctuary(ServerLevel level, LivingEntity entity) {
        if (level == null || entity == null || !entity.isAlive() || entity.isSpectator()) {
            return;
        }
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
        if (entity instanceof Player player) {
            if (player.isInWater() && (level.getGameTime() + player.getId()) % 200L == 0L) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, true));
            }
        }
    }

    /**
     * Blowing-sand visibility hazard for {@code 2.15 Dust-Crusted Bergs}:
     * every {@code 120} ticks outdoors, non-creative/non-spectator players without
     * {@code entropica:zephyr_weight}, protective headgear ({@code mirage_veil_goggles} or
     * {@code spore_filter_mask}), sneaking posture, or a nearby {@code wind_baffle_lantern}
     * ({@code 20} blocks) can be caught in swirling grey-silt gusts ({@code 50t} Blindness + ash spray).
     */
    public static void tickDustCrustedBergsHazard(ServerLevel level, LivingEntity entity) {
        if (level == null || !(entity instanceof Player player) || !player.isAlive()
                || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (entity.tickCount % 120 != 0) {
            return;
        }
        if (player.isShiftKeyDown()) {
            return;
        }
        BlockPos feetPos = entity.blockPosition();
        if (!level.canSeeSky(feetPos.above())) {
            return;
        }

        // Protective status effect & eyewear/mask checks
        Optional<MobEffect> zephyrWeightOpt = BuiltInRegistries.MOB_EFFECT.getOptional(ZEPHYR_WEIGHT_ID);
        if (zephyrWeightOpt.isPresent()
                && player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(zephyrWeightOpt.get()))) {
            return;
        }
        if (matchesItemSlot(player, EquipmentSlot.HEAD, MIRAGE_VEIL_GOGGLES_ID)
                || matchesItemSlot(player, EquipmentSlot.HEAD, SPORE_FILTER_MASK_ID)) {
            return;
        }

        boolean activeWeather = level.isRaining() || level.isThundering();
        boolean polarDustGust = (player.isSprinting() || player.getY() > 66.0D) && level.random.nextInt(3) == 0;
        if (!activeWeather && !polarDustGust) {
            return;
        }

        if (isNearWindBaffleLantern(level, feetPos, 20)) {
            return;
        }

        double eyeY = player.getEyeY();
        level.sendParticles(
                ParticleTypes.ASH,
                player.getX(),
                eyeY,
                player.getZ(),
                24,
                0.65D,
                0.35D,
                0.65D,
                0.04D
        );
        level.playSound(
                null,
                feetPos,
                SoundEvents.BREEZE_IDLE_AIR,
                SoundSource.WEATHER,
                0.32F,
                0.85F + level.random.nextFloat() * 0.25F
        );
        level.playSound(
                null,
                feetPos,
                SoundEvents.SAND_STEP,
                SoundSource.WEATHER,
                0.28F,
                1.25F + level.random.nextFloat() * 0.20F
        );
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, true, true));
    }

    private static boolean isNearWindBaffleLantern(ServerLevel level, BlockPos center, int radius) {
        Optional<Block> lanternOpt = BuiltInRegistries.BLOCK.getOptional(WIND_BAFFLE_LANTERN_ID);
        if (lanternOpt.isEmpty()) {
            return false;
        }
        Block lanternBlock = lanternOpt.get();
        int radiusSqr = radius * radius;
        int minY = Math.max(level.getMinY(), center.getY() - 12);
        int maxY = Math.min(level.getMaxY() - 1, center.getY() + 16);
        int minChunkX = (center.getX() - radius) >> 4;
        int maxChunkX = (center.getX() + radius) >> 4;
        int minChunkZ = (center.getZ() - radius) >> 4;
        int maxChunkZ = (center.getZ() + radius) >> 4;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                int startX = Math.max(center.getX() - radius, cx << 4);
                int endX = Math.min(center.getX() + radius, (cx << 4) + 15);
                int startZ = Math.max(center.getZ() - radius, cz << 4);
                int endZ = Math.min(center.getZ() + radius, (cz << 4) + 15);

                for (int x = startX; x <= endX; x++) {
                    int dx = x - center.getX();
                    for (int z = startZ; z <= endZ; z++) {
                        int dz = z - center.getZ();
                        int horizSqr = dx * dx + dz * dz;
                        if (horizSqr > radiusSqr) {
                            continue;
                        }
                        for (int y = minY; y <= maxY; y++) {
                            int dy = y - center.getY();
                            if (horizSqr + dy * dy > radiusSqr) {
                                continue;
                            }
                            cursor.set(x, y, z);
                            if (level.getBlockState(cursor).is(lanternBlock)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
