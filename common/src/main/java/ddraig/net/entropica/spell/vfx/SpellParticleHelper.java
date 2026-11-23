package ddraig.net.entropica.spell.vfx;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.registry.ModParticles;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Server- and client-safe coordinator for Materia-matched custom tintable particles,
 * Elder Futhark rune glyphs, and high-luminance emissive spell color palettes across all 58+ Materia types.
 */
public final class SpellParticleHelper {

    private static final class ParticleLists {
        static final List<RegistrySupplier<ParticleType<TimedTintableParticleOption>>> ALL_TINTABLE = List.of(
                ModParticles.TINTABLE_SPARKLE,
                ModParticles.TINTABLE_SPORE,
                ModParticles.TINTABLE_MIST,
                ModParticles.TINTABLE_DRIP,
                ModParticles.TINTABLE_EMBER,
                ModParticles.TINTABLE_RUNE,
                ModParticles.TINTABLE_BLOOD,
                ModParticles.TINTABLE_VORTEX,
                ModParticles.TINTABLE_SHIELD,
                ModParticles.TINTABLE_ARC,
                ModParticles.TINTABLE_LEAF,
                ModParticles.TINTABLE_WISP,
                ModParticles.TINTABLE_CRYSTAL,
                ModParticles.TINTABLE_BUBBLE,
                ModParticles.TINTABLE_SINGULARITY,
                ModParticles.TINTABLE_SIGIL,
                ModParticles.TINTABLE_SUNBEAM,
                ModParticles.TINTABLE_ACID
        );

        static final List<RegistrySupplier<ParticleType<TimedTintableParticleOption>>> ALL_RUNES = List.of(
                ModParticles.RUNE_FEHU,
                ModParticles.RUNE_URUZ,
                ModParticles.RUNE_THURISAZ,
                ModParticles.RUNE_ANSUZ,
                ModParticles.RUNE_RAIDO,
                ModParticles.RUNE_KENAZ,
                ModParticles.RUNE_GEBO,
                ModParticles.RUNE_WUNJO,
                ModParticles.RUNE_HAGALAZ,
                ModParticles.RUNE_NAUTHIZ,
                ModParticles.RUNE_ISA,
                ModParticles.RUNE_JERA,
                ModParticles.RUNE_EIHWAZ,
                ModParticles.RUNE_PERTHRO,
                ModParticles.RUNE_ALGIZ,
                ModParticles.RUNE_SOWILO,
                ModParticles.RUNE_TIWAZ,
                ModParticles.RUNE_BERKANO,
                ModParticles.RUNE_EHWAZ,
                ModParticles.RUNE_MANNAZ,
                ModParticles.RUNE_LAGUZ,
                ModParticles.RUNE_INGWAZ,
                ModParticles.RUNE_DAGAZ,
                ModParticles.RUNE_OTHALA
        );
    }

    public static List<RegistrySupplier<ParticleType<TimedTintableParticleOption>>> getAllTintableParticles() {
        return ParticleLists.ALL_TINTABLE;
    }

    public static List<RegistrySupplier<ParticleType<TimedTintableParticleOption>>> getAllRuneParticles() {
        return ParticleLists.ALL_RUNES;
    }

    /**
     * Visual geometry archetypes used by {@code SpellProjectileRenderer}.
     */
    public static final byte ARCHETYPE_ARCANE_BOLT = 0;
    public static final byte ARCHETYPE_CRYSTALLINE_LANCE = 1;
    public static final byte ARCHETYPE_PRISMATIC_CHAKRAM = 2;
    public static final byte ARCHETYPE_SINGULARITY_ORB = 3;
    public static final byte ARCHETYPE_SOLAR_CROWN = 4;
    public static final byte ARCHETYPE_MOLTEN_METEOR = 5;
    public static final byte ARCHETYPE_BIO_HELIX = 6;
    public static final byte ARCHETYPE_VOLTAIC_JAVELIN = 7;
    public static final byte ARCHETYPE_WIND_SPIRAL = 8;
    public static final byte ARCHETYPE_WIND_SLASH = 9;
    public static final byte ARCHETYPE_HYDRO_ORB = 10;
    public static final byte ARCHETYPE_BRIAR_SEED = 11;
    public static final byte ARCHETYPE_QUAKE_SPIRE = 12;
    public static final byte ARCHETYPE_GLACIAL_SHARD = 13;
    public static final byte ARCHETYPE_NETHERFIRE_COMET = 14;
    public static final byte ARCHETYPE_DAWN_STAR = 15;
    public static final byte ARCHETYPE_UMBRAL_SNARE = 16;
    public static final byte ARCHETYPE_UNDEAD_SKULL = 17;
    public static final byte ARCHETYPE_DUST_DEVIL = 18;
    public static final byte ARCHETYPE_BALL_LIGHTNING = 19;
    public static final byte ARCHETYPE_SQUALL_VORTEX = 20;
    public static final byte ARCHETYPE_GLACIAL_ICE_LANCE = 21;
    public static final byte ARCHETYPE_OVERGROWTH_BRIAR_POD = 22;
    public static final byte ARCHETYPE_CHOKING_DUST_POD = 23;
    public static final byte ARCHETYPE_FUNGAL_PUFFBALL = 24;
    public static final byte ARCHETYPE_FROST_RESIN_SPIKE = 25;
    public static final byte ARCHETYPE_OASIS_SUCCULENT_SEED = 26;
    public static final byte ARCHETYPE_VITAE_HELIX = 27;
    public static final byte ARCHETYPE_AMBER_BUBBLE_ORB = 28;
    public static final byte ARCHETYPE_PENUMBRA_CRESCENT_SCYTHE = 29;
    public static final byte ARCHETYPE_NECRO_SPORE_POD = 30;
    public static final byte ARCHETYPE_CELESTIAL_COMET = 31;
    public static final byte ARCHETYPE_SOULFLAME_SKULL = 32;
    public static final byte ARCHETYPE_STEAM_GEYSER_ORB = 33;
    public static final byte ARCHETYPE_WHITE_HOLE_ORB = 34;
    public static final byte ARCHETYPE_BLACK_HOLE_ORB = 35;
    public static final byte ARCHETYPE_SOLAR_HORIZON_JAVELIN = 36;
    public static final byte ARCHETYPE_ABYSSAL_DEPTH_CHARGE_ORB = 37;
    public static final byte ARCHETYPE_GILDED_PAVISE_SHIELD = 38;
    public static final byte ARCHETYPE_AURORA_RIBBON_LANCE = 39;
    public static final byte ARCHETYPE_MIRAGE_SAND_JAVELIN = 40;
    public static final byte ARCHETYPE_WRAITH_SKULL = 41;
    public static final byte ARCHETYPE_TOMBSTONE_BOULDER = 42;
    public static final byte ARCHETYPE_COMBUSTION_HEARTH_ORB = 43;
    public static final byte ARCHETYPE_PENUMBRAL_ECLIPSE_BOLT = 44;
    public static final byte ARCHETYPE_HOARFROST_RIME_JAVELIN = 45;
    public static final byte ARCHETYPE_VERNAL_AQUIFER_ORB = 46;
    public static final byte ARCHETYPE_DIELECTRIC_MORTAR_ORB = 47;
    public static final byte ARCHETYPE_ELDERWOOD_SUN_JAVELIN = 48;
    public static final byte ARCHETYPE_SWAMPLAND_GAS_POD = 49;
    public static final byte ARCHETYPE_PRIMORDIAL_DAWN_TEAR = 50;
    public static final byte ARCHETYPE_STYGIAN_ANNIHILATION_SKULL = 51;
    public static final byte ARCHETYPE_APEX_CELESTIAL_METEORITE = 52;
    public static final byte ARCHETYPE_ENTROPIC_SINGULARITY_SKULL_ORB = 53;
    public static final byte ARCHETYPE_HOLY_SOLAR_SINGULARITY_DART = 54;
    public static final byte ARCHETYPE_WORLD_TREE_SEED_POD = 55;
    public static final byte ARCHETYPE_HELLFIRE_RAILGUN_SLUG = 56;
    public static final byte ARCHETYPE_PERMAFROST_LANCE = 57;
    public static final byte ARCHETYPE_BLACK_HOLE_RAILGUN_DART = 58;
    public static final byte ARCHETYPE_SEEKING_CROWN_OF_DAWN = 59;
    public static final byte ARCHETYPE_SINGULARITY_EQUILIBRIUM_LANCE = 60;

    private SpellParticleHelper() {
    }

    public static EssenceType fromOrdinal(int ordinal) {
        EssenceType[] vals = EssenceType.values();
        if (ordinal < 0 || ordinal >= vals.length) {
            return EssenceType.REGULAR;
        }
        return vals[ordinal];
    }

    public static byte resolveProjectileArchetype(EssenceType type, boolean fastLance, boolean prismaticRicochet) {
        if (prismaticRicochet || type == EssenceType.PRISMATIC) {
            return ARCHETYPE_PRISMATIC_CHAKRAM;
        }
        return switch (type) {
            case AIR, AURA ->
                    ARCHETYPE_WIND_SPIRAL;
            case VAPOR ->
                    ARCHETYPE_STEAM_GEYSER_ORB;
            case WATER, FLOW, UMBRAL_WATER ->
                    ARCHETYPE_HYDRO_ORB;
            case VOID, SINGULARITY, VACUUM, OBLIVIATE ->
                    ARCHETYPE_SINGULARITY_ORB;
            case ESCHATON ->
                    ARCHETYPE_BLACK_HOLE_RAILGUN_DART;
            case ENTROPIC ->
                    ARCHETYPE_ENTROPIC_SINGULARITY_SKULL_ORB;
            case OBLIVION ->
                    ARCHETYPE_STYGIAN_ANNIHILATION_SKULL;
            case ABYSS ->
                    ARCHETYPE_ABYSSAL_DEPTH_CHARGE_ORB;
            case NULL_R ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_WHITE_HOLE_ORB;
            case NULL_U ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_BLACK_HOLE_ORB;
            case FROZEN ->
                    ARCHETYPE_GLACIAL_SHARD;
            case GLACIAL ->
                    ARCHETYPE_GLACIAL_ICE_LANCE;
            case TAIGA ->
                    ARCHETYPE_FROST_RESIN_SPIKE;
            case AURORA ->
                    ARCHETYPE_AURORA_RIBBON_LANCE;
            case RIME ->
                    ARCHETYPE_HOARFROST_RIME_JAVELIN;
            case AETHER ->
                    ARCHETYPE_APEX_CELESTIAL_METEORITE;
            case TERMINUS ->
                    ARCHETYPE_PERMAFROST_LANCE;
            case STASIS, FROST ->
                    ARCHETYPE_CRYSTALLINE_LANCE;
            case AMBER ->
                    ARCHETYPE_AMBER_BUBBLE_ORB;
            case ECLIPSE ->
                    ARCHETYPE_PENUMBRA_CRESCENT_SCYTHE;
            case LIGHTNING ->
                    fastLance ? ARCHETYPE_VOLTAIC_JAVELIN : ARCHETYPE_BALL_LIGHTNING;
            case STORM ->
                    ARCHETYPE_SQUALL_VORTEX;
            case STATIC ->
                    ARCHETYPE_DIELECTRIC_MORTAR_ORB;
            case VOLT, KINETIC ->
                    ARCHETYPE_VOLTAIC_JAVELIN;
            case RADIANT ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_DAWN_STAR;
            case UMBRAL ->
                    ARCHETYPE_UMBRAL_SNARE;
            case UNDEAD ->
                    ARCHETYPE_UNDEAD_SKULL;
            case ARID ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_DUST_DEVIL;
            case MIRAGE ->
                    ARCHETYPE_MIRAGE_SAND_JAVELIN;
            case WRAITH ->
                    ARCHETYPE_WRAITH_SKULL;
            case BARROW ->
                    ARCHETYPE_TOMBSTONE_BOULDER;
            case PYRE ->
                    ARCHETYPE_COMBUSTION_HEARTH_ORB;
            case PENUMBRA ->
                    ARCHETYPE_PENUMBRAL_ECLIPSE_BOLT;
            case DUST ->
                    ARCHETYPE_CHOKING_DUST_POD;
            case DAWN ->
                    ARCHETYPE_SOLAR_HORIZON_JAVELIN;
            case AEGIS ->
                    ARCHETYPE_GILDED_PAVISE_SHIELD;
            case CELESTIAL ->
                    ARCHETYPE_HOLY_SOLAR_SINGULARITY_DART;
            case EMPYREAN ->
                    ARCHETYPE_WORLD_TREE_SEED_POD;
            case CATACLYSM ->
                    ARCHETYPE_HELLFIRE_RAILGUN_SLUG;
            case APOTHEOSIS ->
                    ARCHETYPE_SEEKING_CROWN_OF_DAWN;
            case ENTROPICA ->
                    ARCHETYPE_SINGULARITY_EQUILIBRIUM_LANCE;
            case AUREOLE, PHOTON ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_SOLAR_CROWN;
            case ASTRAL ->
                    ARCHETYPE_CELESTIAL_COMET;
            case SOULFIRE ->
                    ARCHETYPE_SOULFLAME_SKULL;
            case EARTH, DENSITY, GRAVITY ->
                    ARCHETYPE_QUAKE_SPIRE;
            case NETHER ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_NETHERFIRE_COMET;
            case MAGMA, RADIANT_FIRE, IGNIS, FERVOR, HEAT ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_MOLTEN_METEOR;
            case NATURE ->
                    ARCHETYPE_BRIAR_SEED;
            case OVERGROWTH ->
                    ARCHETYPE_OVERGROWTH_BRIAR_POD;
            case SPORE ->
                    ARCHETYPE_FUNGAL_PUFFBALL;
            case OASIS ->
                    ARCHETYPE_OASIS_SUCCULENT_SEED;
            case VITAE ->
                    ARCHETYPE_VITAE_HELIX;
            case BLIGHT ->
                    ARCHETYPE_NECRO_SPORE_POD;
            case SPRING ->
                    ARCHETYPE_VERNAL_AQUIFER_ORB;
            case SYLVAN ->
                    ARCHETYPE_ELDERWOOD_SUN_JAVELIN;
            case MIASMA ->
                    ARCHETYPE_SWAMPLAND_GAS_POD;
            case GENESIS ->
                    ARCHETYPE_PRIMORDIAL_DAWN_TEAR;
            case BLOOD ->
                    ARCHETYPE_BIO_HELIX;
            default -> fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_ARCANE_BOLT;
        };
    }

    /**
     * Resolves a vivid, high-contrast primary RGB color for emissive 3D spell meshes and custom particles.
     */
    public static int resolveVibrantPrimaryRgb(@Nullable EssenceType type, double timeTicks) {
        if (type == null || type == EssenceType.REGULAR) {
            return 0x62E8FF; // Arcane starlight cyan-magenta blend
        }
        if (type == EssenceType.ENTROPICA) {
            // Section 6.59 Shifting Chrono-Silver (#E0E5EC), Deep Event-Horizon Indigo (#1A153B), & Transcendent Genesis Gold (#FAD02C)!
            int[][] entCycle = {{0xE0, 0xE5, 0xEC}, {0xFA, 0xD0, 0x2C}, {0x1A, 0x15, 0x3B}};
            double scaled = (((timeTicks % 60.0D) + 60.0D) % 60.0D) / 20.0D;
            int idx0 = ((int) scaled) % 3;
            int idx1 = (idx0 + 1) % 3;
            double blend = scaled - Math.floor(scaled);
            int r = (int) Math.round(entCycle[idx0][0] + (entCycle[idx1][0] - entCycle[idx0][0]) * blend);
            int g = (int) Math.round(entCycle[idx0][1] + (entCycle[idx1][1] - entCycle[idx0][1]) * blend);
            int b = (int) Math.round(entCycle[idx0][2] + (entCycle[idx1][2] - entCycle[idx0][2]) * blend);
            return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        }
        if (type.isDynamic()) {
            float hue = (float) (((timeTicks * 0.025D) % 1.0D + 1.0D) % 1.0D);
            return Mth.hsvToRgb(hue, 0.72F, 1.0F) & 0xFFFFFF;
        }
        return switch (type) {
            case AIR -> 0xCFA86E; // Luminous golden-buff Air palette (#A88654 family)
            case AURA -> 0xFF6B9D; // Canonical Dual-Tone Living Pink (#FF6B9D) + Buff Air Gold (#A88654)
            case VOID -> 0x7E3BFF;
            case UMBRAL -> 0x3A2444; // Canonical Abyssal Shadow-Black (#211A21 family)
            case SHADOW, SILENCE, CONCEALMENT -> 0x6D4AFF;
            case ESCHATON -> 0x7012A5; // Canonical Accretion Disk Searing Violet (#7012A5) + Void Black (#0B0813) + Chrono-Silver (#D4C2FC)
            case APOTHEOSIS -> 0xFFE484; // Canonical Celestial Astral Gold (#FFE484) + Supernova White (#FFFFFF) + Ascendant Prismatic Cyan (#72F6FF)
            case NULL_U -> 0x110B17; // Canonical Abyssal Pitch Black (#110B17) + Cosmic Violet (#41304E)
            case ABYSS -> 0x211A21; // Canonical Pitch-Black Oceanic Ink (#211A21) + Deep Trench Navy (#225C7C)
            case UMBRAL_WATER -> 0x1E88E5;
            case OBLIVION -> 0xB83C08; // Canonical Nether Blazing Magma Rust (#B83C08) + Stygian Obsidian (#211A21) + Necrotic Grave Slate (#4D6453)
            case ENTROPIC -> 0x41304E; // Canonical Void Deep Indigo (#41304E) + Umbral Charcoal (#211A21) + Undead Slate (#4D6453) + Blood Crimson (#8A0303)
            case CATACLYSM -> 0xB83C08; // Canonical Nether Blazing Rust Crimson (#B83C08) + Void Deep Indigo (#41304E) + Umbral Shadow Charcoal (#211A21)
            case TERMINUS -> 0x98DCF2; // Canonical Frozen Cyan/Frost (#98DCF2) + Air Golden-Buff (#A88654) + Water Deep Azure (#225C7C)
            case BLOOD -> 0xB80614; // Canonical Deep Venous Crimson (#8A0303 family)
            case NATURE, OVERGROWTH, GROWTH, PRIMAL -> 0x2CE668;
            case SPORE -> 0x248F34; // Canonical Symbiotic Fungal Nature Green (#1A5923 family)
            case TAIGA -> 0x1F6E2B; // Canonical Boreal Evergreen Conifer Green (#1A5923 family)
            case OASIS -> 0x227830; // Canonical Lush Oasis Succulent Green (#1A5923 family)
            case VITAE -> 0xFF6B9D; // Canonical Vibrant Rose Life Energy (#FF6B9D)
            case AMBER -> 0xF29B2E; // Translucent Golden Resin (#773214 / #F29B2E family)
            case ECLIPSE -> 0x211A21; // Canonical Abyssal Void-Black (#211A21) encircled by Celestial Gold (#FFEA78)
            case BLIGHT -> 0x4D6453; // Canonical Tombstone Green-Grey (#4D6453) + Necrotic Green (#34CC48)
            case ASTRAL -> 0x41304E; // Canonical Cosmic Violet (#41304E) + Electric Gold-Yellow (#FFE600)
            case SOULFIRE -> 0x1DF0E5; // Canonical Cyan Soulflame (#1DF0E5) + Soul-Ember Teal (#0D6469)
            case VAPOR -> 0xE0F7FA; // Canonical Scalding Steam White (#E0F7FA) + Boiling Aquatic Cyan (#30A9B0)
            case NULL_R -> 0xFFFFFF; // Canonical Brilliant Antimatter White (#FFFFFF) + Cosmic Violet (#41304E)
            case DAWN -> 0xFFEA78; // Canonical Blazing Sunrise Gold (#FFEA78) + Buff Air Gold (#A88654)
            case AEGIS -> 0xFFEA78; // Canonical Holy Gold (#FFEA78) + Granite Brown (#773214)
            case AURORA -> 0xFFEA78; // Canonical Radiant Solar Gold (#FFEA78) + Crystalline Cryo Cyan (#98DCF2)
            case MIRAGE -> 0xFFEA78; // Canonical Sunlit Mirage Gold (#FFEA78) + Bleached Desert Sandstone (#C2B28F)
            case WATER, FLOW -> 0x24B6FF;
            case EARTH, GRAVITY -> 0xD97B38;
            case FROZEN, GLACIAL -> 0x98DCF2; // Canonical Pale Cryo-Blue / Glacial Cyan (#98DCF2)
            case NETHER -> 0xE24C0E; // Canonical Infernal Volcanic Red-Orange (#B83C08 family)
            case RADIANT -> 0xFFE278; // Canonical Solar Gold (#FFE278)
            case UNDEAD -> 0x5EE088;
            case BARROW -> 0x773214; // Canonical Granite Brown (#773214) + Necrotic Moss (#4D6453)
            case PYRE -> 0xFF4500; // Canonical Roaring Wildfire Red-Orange (#FF4500) + Charred Timber Coal (#773214)
            case PENUMBRA -> 0x281E3C; // Canonical Deep Twilight Indigo (#281E3C) + Blazing Corona Gold (#FFEA78)
            case RIME -> 0x6395B1; // Canonical Frostbite Slate Blue (#6395B1) + Hoarfrost White (#DCF0FF)
            case SPRING -> 0xFF6B9D; // Canonical Vernal Blossom Pink (#FF6B9D) + Aquifer Cyan (#64C8FF)
            case SYLVAN -> 0x1A5923; // Canonical Primeval Sylvan Emerald (#1A5923) + Ironwood (#773214) + Solar Gold (#FFEA78)
            case MIASMA -> 0x225C7C; // Canonical Stagnant Toxic Teal (#225C7C) + Virulent Green (#1A5923) + Necrotic Olive (#4D6453)
            case GENESIS -> 0xFFEA78; // Canonical Radiant Dawn Gold (#FFEA78) + Vitae Blossom Pink (#FF6B9D) + Sacred Oceanic Teal (#225C7C)
            case AETHER -> 0xA88654; // Canonical Air Chalk Gold (#A88654) + Earth Bronze (#773214) + Water Azure (#225C7C) + Nether Rust (#B83C08)
            case CELESTIAL -> 0xFFEA78; // Canonical Radiant Pure Gold (#FFEA78) + Vitae Rose Pink (#FF6B9D) + Nature Emerald (#1A5923) + Air Chalk Gold (#A88654)
            case ARID, DUST -> 0xC2B28F; // Canonical Bleached Dune Sandstone (#C2B28F)
            case LIGHTNING, STATIC, VOLT -> 0xFFE600; // Canonical Electric Gold-Yellow (#FFE600)
            case MAGMA -> 0xB83C08; // Canonical Molten Volcanic Red-Orange (#B83C08)
            case STORM -> 0x429BA3; // Canonical Tempest Teal (#429BA3)
            case WRAITH -> 0x41304E; // Canonical Abyssal Void Indigo (#41304E) + Necrotic Moss (#4D6453)
            default -> boostEmissiveLuminance(packRgb(type.getCurrentRGB(timeTicks)), 135);
        };
    }

    /**
     * Resolves a complementary harmonic accent RGB for inner rings, dual-helix ribbons, and secondary particle bursts.
     */
    public static int resolveVibrantAccentRgb(@Nullable EssenceType primary, @Nullable EssenceType secondary, double timeTicks) {
        if (secondary != null && secondary != primary && secondary != EssenceType.REGULAR) {
            return resolveVibrantPrimaryRgb(secondary, timeTicks + 12.0D);
        }
        if (primary == null || primary == EssenceType.REGULAR) {
            return 0xDF78FF;
        }
        if (primary == EssenceType.ENTROPICA) {
            // Phase-shifted harmonic complement across Genesis Gold (#FAD02C), Event-Horizon Indigo (#1A153B), & Chrono-Silver (#E0E5EC)!
            return resolveVibrantPrimaryRgb(EssenceType.ENTROPICA, timeTicks + 20.0D);
        }
        if (primary.isDynamic()) {
            float hue = (float) ((((timeTicks * 0.025D) + 0.38D) % 1.0D + 1.0D) % 1.0D);
            return Mth.hsvToRgb(hue, 0.65F, 1.0F) & 0xFFFFFF;
        }
        return switch (primary) {
            case AIR -> 0xEEF9FF; // Crisp silver-white slipstream ivory
            case AURA -> 0xD6B478; // Canonical Buff Air Gold (#A88654 family)
            case UMBRAL -> 0x8F5CFF; // Twilight Umbral-Eclipse Violet Rim
            case UNDEAD -> 0xE6DFCF; // Bleached Ossuary Bone Ivory (#E6DFCF)
            case BARROW -> 0x4D6453; // Canonical Necrotic Moss / Tombstone Lichen (#4D6453)
            case ARID -> 0xF5E4BE; // Sunlit Desert Ivory (#F5E4BE)
            case DUST -> 0xF2ECE1; // Fine Abrasive Chalk-White Ivory (#F2ECE1)
            case VOID -> 0xC77DFF;
            case NULL_U -> 0x41304E; // Canonical Cosmic Event-Horizon Violet (#41304E)
            case ABYSS -> 0x225C7C; // Canonical Deep Trench Navy (#225C7C)
            case ESCHATON -> 0xD4C2FC; // Canonical Event Edge Chrono-Silver (#D4C2FC)
            case RADIANT -> 0xFFFDF0; // Incandescent Daylight Ivory-White (#FFFDF0)
            case DAWN -> 0xA88654; // Canonical Atmospheric Buff Air Gold (#A88654)
            case AEGIS -> 0x773214; // Canonical Granite Brown (#773214)
            case AURORA -> 0x98DCF2; // Canonical Crystalline Cryo Cyan (#98DCF2)
            case MIRAGE -> 0xC2B28F; // Canonical Bleached Desert Sandstone (#C2B28F)
            case WRAITH -> 0x4D6453; // Canonical Necrotic Moss / Tombstone Ectoplasm (#4D6453)
            case APOTHEOSIS -> 0x72F6FF; // Canonical Ascendant Prismatic Cyan (#72F6FF)
            case AUREOLE -> 0xFFF6C4;
            case FROZEN -> 0xEBF9FF; // Diamond-white permafrost highlight (#EBF9FF)
            case RIME -> 0xDCF0FF; // Canonical Hoarfrost White (#DCF0FF)
            case SPRING -> 0x64C8FF; // Canonical Aquifer Springwater Cyan (#64C8FF)
            case GLACIAL -> 0xE0FFFF;
            case TERMINUS -> 0x225C7C; // Canonical Water Deep Azure (#225C7C)
            case PYRE -> 0x773214; // Canonical Charred Cedar Timber & Granite Coal Brown (#773214)
            case NETHER, MAGMA -> 0xFFD15C;
            case CATACLYSM -> 0x41304E; // Canonical Void Deep Indigo (#41304E)
            case LIGHTNING -> 0xB8F4FF;
            case STATIC -> 0x878DA2; // Canonical Galvanized Slate Gray (#878DA2)
            case STORM -> 0xD8FAFF; // Crisp Seafoam Gale-Slipstream Ivory (#D8FAFF)
            case NATURE, OVERGROWTH -> 0xB4FF78;
            case SYLVAN -> 0xFFEA78; // Canonical Canopy Solar Gold (#FFEA78)
            case MIASMA -> 0x1A5923; // Canonical Sickly Virulent Green (#1A5923)
            case GENESIS -> 0xFF6B9D; // Canonical Vitae Blossom Pink (#FF6B9D)
            case OBLIVION -> 0x4D6453; // Canonical Necrotic Grave Slate (#4D6453)
            case AETHER -> 0x225C7C; // Canonical Water Tidal Azure (#225C7C)
            case ENTROPIC -> 0x8A0303; // Canonical Boiling Sanguine Blood Crimson (#8A0303)
            case CELESTIAL -> 0xFF6B9D; // Canonical Vitae Rose-Prana Pink (#FF6B9D)
            case SPORE -> 0xD6B478; // Canonical Buff Air Gold (#A88654 family)
            case TAIGA -> 0x98DCF2; // Canonical Boreal Frost Cyan (#98DCF2)
            case OASIS -> 0xDEC99A; // Canonical Sunlit Desert Sandstone Gold (#C2B28F family)
            case VITAE -> 0xFFD1E3; // Luminous Rose-Pearl Cellular Blush (#FFD1E3)
            case AMBER -> 0xFF6B9D; // Canonical Dual-Tone Living Pink (#FF6B9D) + Deep Earth Amber (#773214)
            case EARTH, GRAVITY, DENSITY -> 0xFFC278;
            case BLOOD -> 0xFF384C; // Luminous Arterial Scarlet Highlight (#FF384C)
            case WATER -> 0x8CF4FF;
            case ASTRAL -> 0xFFE600; // Canonical Electric Gold-Yellow (#FFE600)
            case SOULFIRE -> 0x0D6469; // Canonical Soul-Ember Teal (#0D6469)
            case VAPOR -> 0x30A9B0; // Canonical Boiling Aquatic Cyan (#30A9B0)
            case NULL_R -> 0x41304E; // Canonical Cosmic Event-Horizon Violet (#41304E)
            case PENUMBRA -> 0xFFEA78; // Canonical Blazing Solar Corona Gold (#FFEA78)
            case ECLIPSE -> 0xFFEA78; // Canonical Blazing Coronal Gold (#FFEA78)
            case BLIGHT -> 0x34CC48; // Canonical Virulent Necrotic Green (#34CC48)
            default -> {
                int[][] cycle = primary.getColorCycle();
                if (cycle.length > 1) {
                    int[] stage = cycle[1];
                    yield boostEmissiveLuminance((stage[0] << 16) | (stage[1] << 8) | stage[2], 165);
                }
                yield blendTowardWhite(resolveVibrantPrimaryRgb(primary, timeTicks), 0.45F);
            }
        };
    }

    public static ParticleType<TimedTintableParticleOption> resolvePrimaryParticleType(@Nullable EssenceType type) {
        if (type == null) {
            return ModParticles.TINTABLE_SPARKLE.get();
        }
        return switch (type) {
            case LIGHTNING, STATIC, STORM, ASTRAL, KINETIC, VOLT -> ModParticles.TINTABLE_ARC.get();
            case NETHER, MAGMA, PYRE, SOULFIRE, CATACLYSM, RADIANT_FIRE, IGNIS, FERVOR, HEAT -> ModParticles.TINTABLE_EMBER.get();
            case FROZEN, GLACIAL, RIME, TAIGA, AURORA, TERMINUS, PRISMATIC, AETHER, AMBER, STASIS, FROST -> ModParticles.TINTABLE_CRYSTAL.get();
            case BLOOD, ENTROPIC -> ModParticles.TINTABLE_BLOOD.get();
            case NATURE, OVERGROWTH, SYLVAN, OASIS, SPRING, EMPYREAN, GROWTH, PRIMAL -> ModParticles.TINTABLE_LEAF.get();
            case SPORE, BLIGHT, MIASMA -> ModParticles.TINTABLE_SPORE.get();
            case WATER, ABYSS, UMBRAL_WATER, FLOW -> ModParticles.TINTABLE_BUBBLE.get();
            case RADIANT, DAWN, AUREOLE, CELESTIAL, GENESIS, APOTHEOSIS, PHOTON, VITAE -> ModParticles.TINTABLE_SUNBEAM.get();
            case VOID, NULL_R, NULL_U, ECLIPSE, PENUMBRA, OBLIVION, ESCHATON, SINGULARITY, ENTROPICA, EQUINOX, GRAVITY, VACUUM, OBLIVIATE ->
                    ModParticles.TINTABLE_SINGULARITY.get();
            case AEGIS, EARTH, DENSITY, COHESION -> ModParticles.TINTABLE_SHIELD.get();
            case AIR, VAPOR, DUST, ARID, MIRAGE, AURA, VESPER, UMBRAL, UNDEAD, WRAITH, BARROW, CHIMERA, CHRONOS ->
                    ModParticles.TINTABLE_WISP.get();
            default -> ModParticles.TINTABLE_SPARKLE.get();
        };
    }

    public static ParticleType<TimedTintableParticleOption> resolveAccentParticleType(@Nullable EssenceType type) {
        if (type == null) {
            return ModParticles.TINTABLE_RUNE.get();
        }
        return switch (type) {
            case WATER, OASIS, SPRING, ABYSS, AMBER -> ModParticles.TINTABLE_DRIP.get();
            case SPORE, BLIGHT, MIASMA -> ModParticles.TINTABLE_ACID.get();
            case AIR, VAPOR, MIRAGE, UMBRAL, UNDEAD, WRAITH, DUST, ARID, BLOOD, AURA, STATIC -> ModParticles.TINTABLE_MIST.get();
            case VOID, NULL_R, NULL_U, ESCHATON, CATACLYSM, STORM -> ModParticles.TINTABLE_VORTEX.get();
            case AEGIS, RADIANT, CELESTIAL, APOTHEOSIS, ENTROPICA -> ModParticles.TINTABLE_SIGIL.get();
            case SYLVAN -> ModParticles.TINTABLE_SUNBEAM.get();
            case ECLIPSE, OBLIVION -> ModParticles.TINTABLE_EMBER.get();
            default -> ModParticles.TINTABLE_SPARKLE.get();
        };
    }

    public static ParticleType<TimedTintableParticleOption> resolveRuneParticleType(@Nullable EssenceType type, int seed) {
        List<RegistrySupplier<ParticleType<TimedTintableParticleOption>>> runes = getAllRuneParticles();
        if (type == null) {
            return runes.get(Math.floorMod(seed, runes.size())).get();
        }
        return switch (type) {
            case FROZEN, GLACIAL, RIME, TAIGA, TERMINUS -> ModParticles.RUNE_ISA.get();
            case NETHER, MAGMA, PYRE, SOULFIRE, RADIANT_FIRE -> ModParticles.RUNE_KENAZ.get();
            case RADIANT, DAWN, AUREOLE, LIGHTNING, STATIC, APOTHEOSIS -> ModParticles.RUNE_SOWILO.get();
            case UNDEAD, BARROW, WRAITH, BLIGHT -> ModParticles.RUNE_EIHWAZ.get();
            case AEGIS -> ModParticles.RUNE_ALGIZ.get();
            case EARTH, DENSITY, GRAVITY -> ModParticles.RUNE_URUZ.get();
            case ARID, DUST, MIRAGE -> ModParticles.RUNE_FEHU.get();
            case NATURE, OVERGROWTH, SYLVAN, VITAE, SPRING, EMPYREAN -> ModParticles.RUNE_BERKANO.get();
            case WATER, ABYSS, OASIS -> ModParticles.RUNE_LAGUZ.get();
            case STORM, CATACLYSM, OBLIVION, ESCHATON -> ModParticles.RUNE_HAGALAZ.get();
            case VOID, NULL_R, NULL_U, ASTRAL, ECLIPSE -> ModParticles.RUNE_PERTHRO.get();
            case ENTROPICA, CELESTIAL, PRISMATIC, GENESIS -> ModParticles.RUNE_DAGAZ.get();
            default -> runes.get(Math.floorMod(type.ordinal() + seed, runes.size())).get();
        };
    }

    /**
     * Spawns zero-bandwidth client-side custom tintable trail particles & spiraling motes behind a flying spell projectile.
     */
    public static void spawnClientProjectileTrail(
            Level clientLevel,
            Vec3 pos,
            Vec3 vel,
            EssenceType primary,
            @Nullable EssenceType secondary,
            int tickCount,
            int tier,
            boolean orbital,
            boolean splitChild
    ) {
        RandomSource rand = clientLevel.random;
        double gameTime = clientLevel.getGameTime();
        int pRgb = resolveVibrantPrimaryRgb(primary, gameTime + tickCount);
        int aRgb = resolveVibrantAccentRgb(primary, secondary, gameTime + tickCount);

        float tierScale = 0.80F + Math.clamp(tier, 1, 10) * 0.06F;
        if (splitChild) {
            tierScale *= 0.72F;
        }

        // 1. Helical custom Materia particle wake along velocity vector
        double angle = tickCount * 0.65D;
        double helixRad = orbital ? 0.12D : 0.18D * tierScale;
        Vec3 fwd = vel.lengthSqr() > 1.0E-4D ? vel.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 upRef = Math.abs(fwd.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = fwd.cross(upRef).normalize();
        Vec3 up = right.cross(fwd).normalize();

        Vec3 helixOffset = right.scale(Math.cos(angle) * helixRad).add(up.scale(Math.sin(angle) * helixRad));
        ParticleType<TimedTintableParticleOption> mainType = resolvePrimaryParticleType(primary);
        TimedTintableParticleOption mainOpt = new TimedTintableParticleOption(
                mainType,
                ((pRgb >> 16) & 0xFF) / 255.0F,
                ((pRgb >> 8) & 0xFF) / 255.0F,
                (pRgb & 0xFF) / 255.0F,
                orbital ? 20 : 28,
                0.78F * tierScale,
                rand.nextFloat() * 6.28F,
                (rand.nextFloat() - 0.5F) * 0.22F,
                TimedTintableParticleOption.MODE_NOISE,
                0.02F, 0.0F, 0.0F
        );
        clientLevel.addParticle(
                mainOpt,
                pos.x + helixOffset.x,
                pos.y + helixOffset.y,
                pos.z + helixOffset.z,
                -vel.x * 0.08D + (rand.nextDouble() - 0.5D) * 0.015D,
                -vel.y * 0.08D + (rand.nextDouble() - 0.5D) * 0.015D,
                -vel.z * 0.08D + (rand.nextDouble() - 0.5D) * 0.015D
        );

        // 2. Counter-rotating accent mote every 2 ticks
        if ((tickCount & 1) == 0) {
            Vec3 counterOffset = right.scale(-Math.cos(angle) * helixRad * 0.85D).add(up.scale(-Math.sin(angle) * helixRad * 0.85D));
            ParticleType<TimedTintableParticleOption> accentType = resolveAccentParticleType(secondary != null ? secondary : primary);
            TimedTintableParticleOption accentOpt = TimedTintableParticleOption.linear(
                    accentType,
                    aRgb,
                    24,
                    0.62F * tierScale,
                    0.14F
            );
            clientLevel.addParticle(
                    accentOpt,
                    pos.x + counterOffset.x,
                    pos.y + counterOffset.y,
                    pos.z + counterOffset.z,
                    -vel.x * 0.05D,
                    -vel.y * 0.05D,
                    -vel.z * 0.05D
            );
        }

        // 3. Nature (#3) Thorned Briar Seed wake: fluttering emerald leaves & golden pollen/spore motes
        if (primary == EssenceType.NATURE && (tickCount & 1) == 0) {
            TimedTintableParticleOption pollenOpt = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SPORE.get(),
                    0xFFD84D,
                    22,
                    0.54F * tierScale,
                    0.03F
            );
            clientLevel.addParticle(
                    pollenOpt,
                    pos.x + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.y + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.z + (rand.nextDouble() - 0.5D) * 0.18D,
                    -vel.x * 0.04D,
                    -0.01D,
                    -vel.z * 0.04D
            );
        } else if (primary == EssenceType.EARTH && (tickCount & 1) == 0) {
            // 3b. Earth (#4) Quake Spire wake: fractured stone shards & warm amber seismic fault sparks
            TimedTintableParticleOption faultOpt = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_CRYSTAL.get(),
                    0x773214,
                    24,
                    0.62F * tierScale,
                    0.12F
            );
            clientLevel.addParticle(
                    faultOpt,
                    pos.x + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - 0.08D + (rand.nextDouble() - 0.5D) * 0.10D,
                    pos.z + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.04D,
                    0.04D,
                    -vel.z * 0.04D
            );
        } else if (primary == EssenceType.UNDEAD && (tickCount & 1) == 0) {
            // 3c. Undead (#9) Necrotic Bone-Shard / Skull Missile wake: desiccated #4D6453 tombstone grave-dust & ossuary mist
            TimedTintableParticleOption tombMist = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_MIST.get(),
                    0x4D6453,
                    26,
                    0.68F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    tombMist,
                    pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.y + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                    -vel.x * 0.03D,
                    -0.008D,
                    -vel.z * 0.03D
            );
        } else if (primary == EssenceType.ARID) {
            // 3d. Arid (#11) Dust Devil Missile wake: swirling #C2B28F desert vortex plume + real falling sand grains
            double sandSwirl = tickCount * 1.15D;
            Vec3 sandOff = right.scale(Math.cos(sandSwirl) * 0.22D * tierScale)
                    .add(up.scale(Math.sin(sandSwirl) * 0.22D * tierScale));
            clientLevel.addParticle(
                    new net.minecraft.core.particles.BlockParticleOption(
                            net.minecraft.core.particles.ParticleTypes.FALLING_DUST,
                            net.minecraft.world.level.block.Blocks.SAND.defaultBlockState()
                    ),
                    pos.x + sandOff.x,
                    pos.y + sandOff.y,
                    pos.z + sandOff.z,
                    -vel.x * 0.04D,
                    0.015D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption sandPlume = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_VORTEX.get(),
                        0xC2B28F,
                        24,
                        0.66F * tierScale,
                        0.18F
                );
                clientLevel.addParticle(
                        sandPlume,
                        pos.x - sandOff.x * 0.6D,
                        pos.y - sandOff.y * 0.6D,
                        pos.z - sandOff.z * 0.6D,
                        -vel.x * 0.03D,
                        0.01D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.LIGHTNING) {
            // 3e. Lightning (#12) Plasma Bolt (Ball Lightning) wake: crackling #FFE600 plasma arcs & snapping electric sparks!
            clientLevel.addParticle(
                    net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    pos.x + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.05D + (rand.nextDouble() - 0.5D) * 0.06D,
                    -vel.y * 0.05D + (rand.nextDouble() - 0.5D) * 0.06D,
                    -vel.z * 0.05D + (rand.nextDouble() - 0.5D) * 0.06D
            );
        } else if (primary == EssenceType.ASTRAL) {
            // 3f. Astral (#27) Celestial Comet Missile wake: dual-tone #41304E cosmic star-matter tail + #FFE600 electric pulsar sparks!
            TimedTintableParticleOption cometTail = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SPARKLE.get(),
                    0x41304E,
                    26,
                    0.78F * tierScale,
                    0.06F
            );
            clientLevel.addParticle(
                    cometTail,
                    pos.x - vel.x * 0.14D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.y - vel.y * 0.14D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.z - vel.z * 0.14D + (rand.nextDouble() - 0.5D) * 0.18D,
                    -vel.x * 0.08D,
                    -vel.y * 0.08D,
                    -vel.z * 0.08D
            );
            TimedTintableParticleOption goldSpark = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_ARC.get(),
                    0xFFE600,
                    18,
                    0.72F * tierScale,
                    0.08F
            );
            clientLevel.addParticle(
                    goldSpark,
                    pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                    -vel.x * 0.05D,
                    -vel.y * 0.05D,
                    -vel.z * 0.05D
            );
        } else if (primary == EssenceType.SOULFIRE) {
            // 3g. Soulfire (#28) Screaming Soul Skull wake: howling #1DF0E5 cyan soulfire flames & #0D6469 spectral mist!
            clientLevel.addParticle(
                    net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    -vel.x * 0.06D,
                    -vel.y * 0.06D,
                    -vel.z * 0.06D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption soulMist = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_MIST.get(),
                        0x0D6469,
                        24,
                        0.72F * tierScale,
                        0.05F
                );
                clientLevel.addParticle(
                        soulMist,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.18D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        0.01D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.VAPOR) {
            // 3h. Vapor (#29) Scalding Steam-Burst Geyser Orb wake: pressurized #E0F7FA steam plumes & #30A9B0 boiling droplets!
            TimedTintableParticleOption steamPlume = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_MIST.get(),
                    0xE0F7FA,
                    24,
                    0.82F * tierScale,
                    0.06F
            );
            clientLevel.addParticle(
                    steamPlume,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    -vel.x * 0.05D,
                    0.025D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                clientLevel.addParticle(
                        net.minecraft.core.particles.ParticleTypes.SPLASH,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.18D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.18D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.18D,
                        -vel.x * 0.04D,
                        0.01D,
                        -vel.z * 0.04D
                );
            }
        } else if (primary == EssenceType.NULL_R) {
            // 3i. Null_R (#30) White Hole Singularity Orb wake: incandescent #FFFFFF antimatter flares & #41304E cosmic violet gravity motes!
            TimedTintableParticleOption antimatterSpark = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SPARKLE.get(),
                    0xFFFFFF,
                    22,
                    0.80F * tierScale,
                    0.07F
            );
            clientLevel.addParticle(
                    antimatterSpark,
                    pos.x - vel.x * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.y - vel.y * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.z - vel.z * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    -vel.x * 0.05D,
                    -vel.y * 0.05D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption horizonVortex = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_SINGULARITY.get(),
                        0x41304E,
                        24,
                        0.74F * tierScale,
                        0.15F
                );
                clientLevel.addParticle(
                        horizonVortex,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                        -vel.x * 0.03D,
                        -vel.y * 0.03D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.NULL_U) {
            // 3j. Null_U (#31) Black Hole Singularity Orb wake: light-devouring #110B17 abyssal singularity & #41304E shadow smoke!
            TimedTintableParticleOption abyssCore = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SINGULARITY.get(),
                    0x110B17,
                    24,
                    0.82F * tierScale,
                    0.16F
            );
            clientLevel.addParticle(
                    abyssCore,
                    pos.x - vel.x * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.y - vel.y * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.z - vel.z * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    -vel.x * 0.04D,
                    -vel.y * 0.04D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption umbralMist = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_MIST.get(),
                        0x41304E,
                        24,
                        0.74F * tierScale,
                        0.05F
                );
                clientLevel.addParticle(
                        umbralMist,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                        -vel.x * 0.03D,
                        -vel.y * 0.03D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.DAWN) {
            // 3k. Dawn (#32) Solar Horizon Javelin wake: blazing #FFEA78 sunrise rays & #A88654 floating wind-feather slipstream!
            TimedTintableParticleOption sunRay = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SUNBEAM.get(),
                    0xFFEA78,
                    22,
                    0.80F * tierScale,
                    0.06F
            );
            clientLevel.addParticle(
                    sunRay,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    -vel.x * 0.06D,
                    -vel.y * 0.06D,
                    -vel.z * 0.06D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption windFeather = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_WISP.get(),
                        0xA88654,
                        24,
                        0.72F * tierScale,
                        0.05F
                );
                clientLevel.addParticle(
                        windFeather,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.04D,
                        0.012D,
                        -vel.z * 0.04D
                );
            }
        } else if (primary == EssenceType.ABYSS) {
            // 3l. Abyss (#33) Abyssal Depth Charge Orb wake: #211A21 cephalopod ink mist & #225C7C deep-trench pressure droplets!
            TimedTintableParticleOption inkMist = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_MIST.get(),
                    0x211A21,
                    22,
                    0.82F * tierScale,
                    0.03F
            );
            clientLevel.addParticle(
                    inkMist,
                    pos.x - vel.x * 0.10D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.y - vel.y * 0.10D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.z - vel.z * 0.10D + (rand.nextDouble() - 0.5D) * 0.18D,
                    -vel.x * 0.04D,
                    -vel.y * 0.04D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption trenchDrip = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_DRIP.get(),
                        0x225C7C,
                        24,
                        0.76F * tierScale,
                        -0.06F
                );
                clientLevel.addParticle(
                        trenchDrip,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        -0.02D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.AEGIS) {
            // 3m. Aegis (#34) Gilded Bastion Shield-Slug wake: #FFEA78 holy rune sigils & #773214 granite bastion shields!
            TimedTintableParticleOption holySigil = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SIGIL.get(),
                    0xFFEA78,
                    22,
                    0.82F * tierScale,
                    0.04F
            );
            clientLevel.addParticle(
                    holySigil,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    -vel.x * 0.05D,
                    -vel.y * 0.05D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption graniteShield = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_SHIELD.get(),
                        0x773214,
                        24,
                        0.76F * tierScale,
                        0.02F
                );
                clientLevel.addParticle(
                        graniteShield,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        0.0D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.AURORA) {
            // 3n. Aurora (#35) Prismatic Aurora Ribbon-Lance wake: #FFEA78 dancing solar ribbons & #98DCF2 crystalline cryo sparkles!
            TimedTintableParticleOption auroraRibbon = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SUNBEAM.get(),
                    0xFFEA78,
                    22,
                    0.82F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    auroraRibbon,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.18D,
                    -vel.x * 0.05D,
                    -vel.y * 0.05D,
                    -vel.z * 0.05D
            );
            TimedTintableParticleOption cryoSparkle = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SPARKLE.get(),
                    0x98DCF2,
                    24,
                    0.78F * tierScale,
                    0.06F
            );
            clientLevel.addParticle(
                    cryoSparkle,
                    pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                    -vel.x * 0.04D,
                    -vel.y * 0.04D,
                    -vel.z * 0.04D
            );
        } else if (primary == EssenceType.MIRAGE) {
            // 3o. Mirage (#36) Shimmering Mirage Sand-Dart wake: #FFEA78 sunlit heat-haze rays & #C2B28F swirling desert sand plumes!
            TimedTintableParticleOption mirageShimmer = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SUNBEAM.get(),
                    0xFFEA78,
                    20,
                    0.80F * tierScale,
                    0.06F
            );
            clientLevel.addParticle(
                    mirageShimmer,
                    pos.x - vel.x * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.y - vel.y * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.z - vel.z * 0.10D + (rand.nextDouble() - 0.5D) * 0.20D,
                    -vel.x * 0.05D,
                    -vel.y * 0.05D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption sandVortex = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_VORTEX.get(),
                        0xC2B28F,
                        22,
                        0.74F * tierScale,
                        0.14F
                );
                clientLevel.addParticle(
                        sandVortex,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                        -vel.x * 0.04D,
                        0.01D,
                        -vel.z * 0.04D
                );
            }
        } else if (primary == EssenceType.WRAITH) {
            // 3p. Wraith (#37) Spectral Wraith-Skull Missile wake: #41304E abyssal void singularity & #4D6453 ectoplasmic soul wisps!
            TimedTintableParticleOption voidMist = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SINGULARITY.get(),
                    0x41304E,
                    22,
                    0.82F * tierScale,
                    0.12F
            );
            clientLevel.addParticle(
                    voidMist,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    -vel.x * 0.05D,
                    -vel.y * 0.05D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption ectoWisp = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_WISP.get(),
                        0x4D6453,
                        24,
                        0.76F * tierScale,
                        0.05F
                );
                clientLevel.addParticle(
                        ectoWisp,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                        -vel.x * 0.04D,
                        0.015D,
                        -vel.z * 0.04D
                );
            }
        } else if (primary == EssenceType.BARROW) {
            // 3q. Barrow (#38) Tombstone Ossuary Boulder-Slug wake: #773214 grave-earth grit & #4D6453 necrotic lichen spores!
            TimedTintableParticleOption tombSlab = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SHIELD.get(),
                    0x773214,
                    22,
                    0.82F * tierScale,
                    0.04F
            );
            clientLevel.addParticle(
                    tombSlab,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.20D,
                    -vel.x * 0.05D,
                    -0.015D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption mossSpore = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_SPORE.get(),
                        0x4D6453,
                        24,
                        0.76F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        mossSpore,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                        -vel.x * 0.03D,
                        -0.01D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.PYRE) {
            // 3r. Pyre (#39) Combustion Hearth Orb wake: #FF4500 roaring wildfire sparks & #773214 charred cedar coal ash!
            TimedTintableParticleOption hearthEmber = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_EMBER.get(),
                    0xFF4500,
                    22,
                    0.85F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    hearthEmber,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    -vel.x * 0.05D,
                    0.025D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption coalSmoke = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_MIST.get(),
                        0x773214,
                        24,
                        0.80F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        coalSmoke,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                        -vel.x * 0.03D,
                        0.02D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.PENUMBRA) {
            // 3s. Penumbra (#40) Penumbral Twilight Eclipse-Bolt wake: #FFEA78 coronal rays & #281E3C twilight singularities!
            TimedTintableParticleOption coronaRay = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SUNBEAM.get(),
                    0xFFEA78,
                    22,
                    0.84F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    coronaRay,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    -vel.x * 0.05D,
                    0.01D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption twilightCore = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_SINGULARITY.get(),
                        0x281E3C,
                        24,
                        0.82F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        twilightCore,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        0.0D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.RIME) {
            // 3t. Rime (#41) Hoarfrost Rime-Spike Javelin wake: #DCF0FF hoarfrost needles & #6395B1 sub-zero gale vapor!
            TimedTintableParticleOption hoarfrostShard = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_CRYSTAL.get(),
                    0xDCF0FF,
                    22,
                    0.84F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    hoarfrostShard,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    -vel.x * 0.05D,
                    -0.01D,
                    -vel.z * 0.05D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption rimeVapor = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_MIST.get(),
                        0x6395B1,
                        24,
                        0.80F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        rimeVapor,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        0.005D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.SPRING) {
            // 3u. Spring (#42) Vernal Blossom Aquifer-Orb wake: #FF6B9D cherry blossom petals & #64C8FF aquifer droplets!
            TimedTintableParticleOption blossomPetal = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_LEAF.get(),
                    0xFF6B9D,
                    24,
                    0.82F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    blossomPetal,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.04D,
                    -0.015D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption aquiferDrop = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_DRIP.get(),
                        0x64C8FF,
                        22,
                        0.78F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        aquiferDrop,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        -0.01D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.STATIC) {
            // 3v. Static (#43) Dielectric Ball-Lightning Mortar wake: #FFE600 high-voltage arcs & #878DA2 polarized dielectric dust!
            TimedTintableParticleOption staticArc = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_ARC.get(),
                    0xFFE600,
                    22,
                    0.86F * tierScale,
                    0.06F
            );
            clientLevel.addParticle(
                    staticArc,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.22D,
                    -vel.x * 0.04D,
                    0.0D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption slateDust = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_MIST.get(),
                        0x878DA2,
                        24,
                        0.78F * tierScale,
                        0.05F
                );
                clientLevel.addParticle(
                        slateDust,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                        -vel.x * 0.03D,
                        0.005D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.SYLVAN) {
            // 3w. Sylvan (#44) Elderwood Sun-Bough Javelin wake: #1A5923 canopy leaves, #FFEA78 solar rays & #773214 ironwood bark!
            TimedTintableParticleOption sylvanLeaf = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_LEAF.get(),
                    0x1A5923,
                    24,
                    0.84F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    sylvanLeaf,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.04D,
                    -0.01D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption solarMote = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_SUNBEAM.get(),
                        0xFFEA78,
                        22,
                        0.80F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        solarMote,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        0.005D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.MIASMA) {
            // 3x. Miasma (#45) Putrid Swampland Gas-Pod wake: #1A5923 virulent spores & #225C7C stagnant toxic swamp acid!
            TimedTintableParticleOption miasmaSpore = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_SPORE.get(),
                    0x1A5923,
                    24,
                    0.85F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    miasmaSpore,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.04D,
                    -0.01D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption swampAcid = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_ACID.get(),
                        0x225C7C,
                        22,
                        0.80F * tierScale,
                        0.05F
                );
                clientLevel.addParticle(
                        swampAcid,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        -0.015D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.GENESIS) {
            // 3y. Genesis (#46) Primordial Dawn-Tear Globule wake: #FF6B9D vitae lotus petals & #225C7C sacred oceanic dew!
            TimedTintableParticleOption vitaePetal = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_LEAF.get(),
                    0xFF6B9D,
                    24,
                    0.84F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    vitaePetal,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.04D,
                    -0.01D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption sacredDew = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_DRIP.get(),
                        0x225C7C,
                        22,
                        0.80F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        sacredDew,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        -0.01D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.OBLIVION) {
            // 3z. Oblivion (#47) Stygian Hellfire Annihilation Skull wake: #B83C08 blazing magma rust embers & #211A21 stygian obsidian ash!
            TimedTintableParticleOption stygianEmber = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_EMBER.get(),
                    0xB83C08,
                    24,
                    0.86F * tierScale,
                    0.06F
            );
            clientLevel.addParticle(
                    stygianEmber,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.04D,
                    0.01D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption stygianAsh = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_MIST.get(),
                        0x211A21,
                        24,
                        0.84F * tierScale,
                        0.05F
                );
                clientLevel.addParticle(
                        stygianAsh,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.22D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.22D,
                        -vel.x * 0.03D,
                        -0.01D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.AETHER) {
            // 3za. Aether (#48) Apex Celestial Meteorite wake: 4 chromatic plasma ribbons (#A88654 Air, #773214 Earth, #225C7C Water, #B83C08 Nether)!
            int quadColor = switch (tickCount & 3) {
                case 0 -> 0xA88654;
                case 1 -> 0x773214;
                case 2 -> 0x225C7C;
                default -> 0xB83C08;
            };
            TimedTintableParticleOption firmamentRibbon = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_WISP.get(),
                    quadColor,
                    24,
                    0.88F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    firmamentRibbon,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.04D,
                    0.0D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption haloSparkle = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_SPARKLE.get(),
                        0xA88654,
                        22,
                        0.82F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        haloSparkle,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        0.005D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.ENTROPIC) {
            // 3zb. Entropic (#49) Entropic Singularity Skull-Orb wake: #41304E dark matter, #211A21 umbral ash, #4D6453 necrotic mist & #8A0303 sanguine droplets!
            int entColor = switch (tickCount & 3) {
                case 0 -> 0x41304E;
                case 1 -> 0x211A21;
                case 2 -> 0x4D6453;
                default -> 0x8A0303;
            };
            TimedTintableParticleOption entropicMist = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_MIST.get(),
                    entColor,
                    24,
                    0.88F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    entropicMist,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.04D,
                    -0.005D,
                    -vel.z * 0.04D
            );
            if ((tickCount & 1) == 0) {
                TimedTintableParticleOption sanguineDrop = TimedTintableParticleOption.linear(
                        ModParticles.TINTABLE_BLOOD.get(),
                        0x8A0303,
                        22,
                        0.82F * tierScale,
                        0.04F
                );
                clientLevel.addParticle(
                        sanguineDrop,
                        pos.x + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.y + (rand.nextDouble() - 0.5D) * 0.20D,
                        pos.z + (rand.nextDouble() - 0.5D) * 0.20D,
                        -vel.x * 0.03D,
                        -0.01D,
                        -vel.z * 0.03D
                );
            }
        } else if (primary == EssenceType.EMPYREAN) {
            // 3zd. Empyrean (#52) World-Tree Siphoning Seed-Pod wake: #FFEA78 living gold, #FF6B9D rose vitae & #1A5923 forest emerald petals!
            int empColor = switch (tickCount % 3) {
                case 0 -> 0xFFEA78;
                case 1 -> 0xFF6B9D;
                default -> 0x1A5923;
            };
            TimedTintableParticleOption worldTreeLeaf = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_LEAF.get(),
                    empColor,
                    24,
                    0.88F * tierScale,
                    0.05F
            );
            clientLevel.addParticle(
                    worldTreeLeaf,
                    pos.x - vel.x * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.y - vel.y * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    pos.z - vel.z * 0.12D + (rand.nextDouble() - 0.5D) * 0.24D,
                    -vel.x * 0.03D,
                    0.01D,
                    -vel.z * 0.03D
            );
        }

        // 4. Lingering glowing Elder Futhark rune glyph every 4 ticks on non-split projectiles
        if (!splitChild && tickCount % 4 == 0) {
            ParticleType<TimedTintableParticleOption> runeType = resolveRuneParticleType(primary, tickCount / 4);
            TimedTintableParticleOption runeOpt = TimedTintableParticleOption.linear(
                    runeType,
                    aRgb,
                    36,
                    0.70F * tierScale,
                    0.04F
            );
            clientLevel.addParticle(
                    runeOpt,
                    pos.x + (rand.nextDouble() - 0.5D) * 0.14D,
                    pos.y + (rand.nextDouble() - 0.5D) * 0.14D,
                    pos.z + (rand.nextDouble() - 0.5D) * 0.14D,
                    0.0D,
                    0.015D,
                    0.0D
            );
        }
    }

    /**
     * Spawns a crisp Materia-tinted muzzle flash & floating Elder Futhark runes when casting a spell.
     */
    public static void spawnCastMuzzleBurst(
            ServerLevel level,
            Vec3 muzzlePos,
            Vec3 lookDir,
            EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        long gameTime = level.getGameTime();
        int pRgb = resolveVibrantPrimaryRgb(primary, gameTime);
        int aRgb = resolveVibrantAccentRgb(primary, secondary, gameTime);
        float scale = 0.85F + Math.clamp(tier, 1, 10) * 0.05F;

        ParticleType<TimedTintableParticleOption> pType = resolvePrimaryParticleType(primary);
        ParticleType<TimedTintableParticleOption> rType = resolveRuneParticleType(primary, (int) (gameTime & 0x1F));

        level.sendParticles(
                TimedTintableParticleOption.linear(pType, pRgb, 28, 0.85F * scale, 0.16F),
                muzzlePos.x + lookDir.x * 0.35D,
                muzzlePos.y + lookDir.y * 0.35D,
                muzzlePos.z + lookDir.z * 0.35D,
                5 + tier / 2,
                0.16D, 0.16D, 0.16D,
                0.035D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(rType, aRgb, 38, 0.90F * scale, 0.05F),
                muzzlePos.x + lookDir.x * 0.45D,
                muzzlePos.y + lookDir.y * 0.45D,
                muzzlePos.z + lookDir.z * 0.45D,
                3,
                0.22D, 0.22D, 0.22D,
                0.02D
        );
    }

    /**
     * Spawns a multi-layered Materia-matched custom particle detonation on projectile impact, Nova, Skyfall, or Glyph trigger.
     */
    public static void spawnImpactBurst(
            ServerLevel level,
            Vec3 pos,
            EssenceType primary,
            @Nullable EssenceType secondary,
            double radius,
            int tier
    ) {
        long gameTime = level.getGameTime();
        int pRgb = resolveVibrantPrimaryRgb(primary, gameTime);
        int aRgb = resolveVibrantAccentRgb(primary, secondary, gameTime);
        float scale = (float) Mth.clamp(0.85D + tier * 0.07D + radius * 0.06D, 0.80D, 1.95D);
        double spread = Math.max(0.22D, Math.min(2.6D, radius * 0.35D));

        ParticleType<TimedTintableParticleOption> mainType = resolvePrimaryParticleType(primary);
        ParticleType<TimedTintableParticleOption> accentType = resolveAccentParticleType(secondary != null ? secondary : primary);
        ParticleType<TimedTintableParticleOption> runeType = resolveRuneParticleType(primary, (int) (gameTime & 0x3F));

        int mainCount = Math.clamp((int) (10 + radius * 3.5D + tier), 10, 36);
        int accentCount = Math.clamp((int) (6 + radius * 2.0D), 6, 22);

        TimedTintableParticleOption awayMain = new TimedTintableParticleOption(
                mainType,
                ((pRgb >> 16) & 0xFF) / 255.0F,
                ((pRgb >> 8) & 0xFF) / 255.0F,
                (pRgb & 0xFF) / 255.0F,
                36,
                0.95F * scale,
                0.0F,
                0.18F,
                TimedTintableParticleOption.MODE_AWAY,
                (float) pos.x,
                (float) pos.y,
                (float) pos.z
        );
        level.sendParticles(awayMain, pos.x, pos.y + 0.15D, pos.z, mainCount, spread, spread * 0.55D, spread, 0.06D);

        level.sendParticles(
                TimedTintableParticleOption.linear(accentType, aRgb, 42, 0.82F * scale, 0.12F),
                pos.x, pos.y + 0.25D, pos.z,
                accentCount,
                spread * 0.85D, spread * 0.5D, spread * 0.85D,
                0.04D
        );

        level.sendParticles(
                TimedTintableParticleOption.linear(runeType, aRgb, 52, 0.92F * scale, 0.04F),
                pos.x, pos.y + 0.35D, pos.z,
                4,
                spread * 0.65D, 0.25D, spread * 0.65D,
                0.025D
        );
    }

    /**
     * Emits swirling perimeter custom particles & rising runes on active lingering zones.
     */
    public static void spawnZonePerimeterParticles(
            ServerLevel level,
            Vec3 center,
            double radius,
            @Nullable EssenceType materia,
            long elapsedTicks
    ) {
        if (radius <= 0.4D) {
            return;
        }
        EssenceType eff = materia != null ? materia : EssenceType.REGULAR;
        long gameTime = level.getGameTime();
        int pRgb = resolveVibrantPrimaryRgb(eff, gameTime);
        int aRgb = resolveVibrantAccentRgb(eff, null, gameTime);

        ParticleType<TimedTintableParticleOption> mainType = resolvePrimaryParticleType(eff);
        int points = Math.clamp((int) Math.ceil(radius * 1.5D), 3, 8);
        double baseAngle = elapsedTicks * 0.18D;

        for (int i = 0; i < points; i++) {
            double ang = baseAngle + (Math.PI * 2.0D * i / points);
            double px = center.x + Math.cos(ang) * radius;
            double pz = center.z + Math.sin(ang) * radius;
            TimedTintableParticleOption spiralOpt = TimedTintableParticleOption.spiral(
                    mainType,
                    pRgb,
                    32,
                    0.82F,
                    (float) center.x,
                    0.03F,
                    (float) center.z
            );
            level.sendParticles(spiralOpt, px, center.y + 0.18D, pz, 1, 0.04D, 0.04D, 0.04D, 0.0D);
        }

        if (elapsedTicks % 20 == 0) {
            ParticleType<TimedTintableParticleOption> runeType = resolveRuneParticleType(eff, (int) (elapsedTicks / 20));
            level.sendParticles(
                    TimedTintableParticleOption.linear(runeType, aRgb, 44, 0.90F, 0.03F),
                    center.x, center.y + 0.35D, center.z,
                    2,
                    radius * 0.45D, 0.15D, radius * 0.45D,
                    0.015D
            );
        }
    }

    private static int packRgb(int[] rgb) {
        if (rgb == null || rgb.length < 3) {
            return 0x77DDFF;
        }
        return ((Mth.clamp(rgb[0], 0, 255)) << 16)
                | ((Mth.clamp(rgb[1], 0, 255)) << 8)
                | (Mth.clamp(rgb[2], 0, 255));
    }

    private static int boostEmissiveLuminance(int rgb, int minLum) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        double lum = 0.299D * r + 0.587D * g + 0.114D * b;
        if (lum >= minLum) {
            return rgb;
        }
        int boost = (int) Math.ceil(minLum - lum);
        r = Math.min(255, r + boost);
        g = Math.min(255, g + boost);
        b = Math.min(255, b + boost);
        return (r << 16) | (g << 8) | b;
    }

    private static int blendTowardWhite(int rgb, float amount) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = Mth.clamp((int) Mth.lerp(amount, r, 255), 0, 255);
        g = Mth.clamp((int) Mth.lerp(amount, g, 255), 0, 255);
        b = Mth.clamp((int) Mth.lerp(amount, b, 255), 0, 255);
        return (r << 16) | (g << 8) | b;
    }
}
