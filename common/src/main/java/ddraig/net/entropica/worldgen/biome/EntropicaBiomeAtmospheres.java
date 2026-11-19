package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.config.EntropicaBiome;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry mapping {@link EntropicaBiome}s to their custom fog, shader/Sodium-compatible
 * sky phenomena, wind-drifted ambient particles, and environmental physics/hazards.
 */
public final class EntropicaBiomeAtmospheres {
    private static final Map<EntropicaBiome, BiomeAtmosphereProfile> BY_BIOME = new EnumMap<>(EntropicaBiome.class);
    private static final Map<String, BiomeAtmosphereProfile> BY_PATH = new HashMap<>();
    private static boolean initialized = false;

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
                .build());

        // 2.4 Crystalline Pack-Ice — Violet night sky (#4E2A7A) + shimmering crystalline ice halo
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.CRYSTALLINE_PACK_ICE)
                .nightSkyColor(0x4E2A7A)
                .starBrightnessBoost(0.35F)
                .skyPhenomenon(SkyPhenomenonType.GLACIAL_ICE_HALO, 0xC084FC, 0x67E8F9, 0.75F)
                .build());

        // 2.5 Silent Fjord — Eclipsed low-light twilight atmosphere + Eclipse Corona
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.SILENT_FJORD)
                .fogDistance(10.0F, 72.0F, 0.80F)
                .daySkyBrightnessScale(0.45F)
                .nightSkyColor(0x141229)
                .starBrightnessBoost(0.40F)
                .skyPhenomenon(SkyPhenomenonType.ECLIPSE_CORONA, 0x6366F1, 0xFDE047, 0.70F)
                .build());

        // 2.9 Null-Fringe Bay — Indigo void mist + Void Rift sky fractures + Weak Gravity (Jump Boost I)
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.NULL_FRINGE_BAY)
                .fogDistance(10.0F, 80.0F, 0.80F)
                .fogColors(0x3B235A, 0x1A0B2E)
                .nightSkyColor(0x1B0F36)
                .skyPhenomenon(SkyPhenomenonType.VOID_RIFT_FRACTURES, 0x9333EA, 0x38BDF8, 0.80F)
                .jumpBoost(0)
                .build());

        // 2.10 Radiant Glaze-Plains — Warm amber-yellow sky (#A67E44) + Passive Sanctuary Regeneration
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.RADIANT_GLAZE_PLAINS)
                .nightSkyColor(0x4A371C)
                .passiveSanctuaryRegen(100)
                .build());

        // 2.13 Mirage Glacier — Heat distortion visibility haze + Mirage Heat Shimmer ribbons
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.MIRAGE_GLACIER)
                .fogDistance(8.0F, 48.0F, 0.82F)
                .fogColors(0xFDE68A, 0x5C4522)
                .skyPhenomenon(SkyPhenomenonType.MIRAGE_HEAT_SHIMMER, 0xFDE047, 0xFB923C, 0.70F)
                .build());

        // 2.17 Astral Skerries — Deep starlight blue (#1C1A38) + Astral Stardust Veil + Star Boost
        register(BiomeAtmosphereProfile.builder(EntropicaBiome.ASTRAL_SKERRIES)
                .fogDistance(18.0F, 120.0F, 0.65F)
                .fogColors(0x3B2F63, 0x1C1A38)
                .nightSkyColor(0x1C1A38)
                .starBrightnessBoost(0.65F)
                .skyPhenomenon(SkyPhenomenonType.ASTRAL_STARDUST_VEIL, 0x818CF8, 0xFDE047, 0.85F)
                .driftingParticles(2, 0xC084FC, 0.45F, 80, 0.005F, 0.015F, -0.005F)
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
}
