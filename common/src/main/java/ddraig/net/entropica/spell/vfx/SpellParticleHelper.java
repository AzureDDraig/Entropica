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
            case VOID, ABYSS, NULL_U, OBLIVION, ENTROPIC, ESCHATON, SINGULARITY, VACUUM, OBLIVIATE ->
                    ARCHETYPE_SINGULARITY_ORB;
            case FROZEN, GLACIAL, RIME, TAIGA, AURORA, TERMINUS, AETHER, AMBER, STASIS, FROST ->
                    ARCHETYPE_CRYSTALLINE_LANCE;
            case LIGHTNING, STATIC, STORM, VOLT, KINETIC ->
                    ARCHETYPE_VOLTAIC_JAVELIN;
            case RADIANT, DAWN, AUREOLE, CELESTIAL, APOTHEOSIS, EMPYREAN, PHOTON, ASTRAL ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_SOLAR_CROWN;
            case NETHER, MAGMA, PYRE, EARTH, CATACLYSM, RADIANT_FIRE, IGNIS, FERVOR, HEAT ->
                    fastLance ? ARCHETYPE_CRYSTALLINE_LANCE : ARCHETYPE_MOLTEN_METEOR;
            case NATURE, OVERGROWTH, SPORE, SYLVAN, OASIS, SPRING, VITAE, BLOOD, BLIGHT, MIASMA, GENESIS ->
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
        if (type.isDynamic()) {
            float hue = (float) (((timeTicks * 0.025D) % 1.0D + 1.0D) % 1.0D);
            return Mth.hsvToRgb(hue, 0.72F, 1.0F) & 0xFFFFFF;
        }
        return switch (type) {
            case VOID -> 0x7E3BFF;
            case UMBRAL, SHADOW, SILENCE, CONCEALMENT -> 0x6D4AFF;
            case ESCHATON -> 0xD926FF;
            case NULL_U -> 0x5936D9;
            case ABYSS, UMBRAL_WATER -> 0x1E88E5;
            case OBLIVION -> 0xE63E1C;
            case ENTROPIC -> 0xC61A52;
            case CATACLYSM -> 0xFF3D1F;
            case BLOOD -> 0xF01830;
            case NATURE, OVERGROWTH, GROWTH, PRIMAL -> 0x2CE668;
            case WATER, FLOW -> 0x24B6FF;
            case EARTH, GRAVITY -> 0xD97B38;
            case UNDEAD, BARROW -> 0x5EE088;
            case WRAITH -> 0x7CF2C2;
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
        if (primary.isDynamic()) {
            float hue = (float) ((((timeTicks * 0.025D) + 0.38D) % 1.0D + 1.0D) % 1.0D);
            return Mth.hsvToRgb(hue, 0.65F, 1.0F) & 0xFFFFFF;
        }
        return switch (primary) {
            case VOID, NULL_U, UMBRAL -> 0xC77DFF;
            case ESCHATON -> 0xFF4518;
            case APOTHEOSIS, RADIANT, DAWN, AUREOLE -> 0xFFF6C4;
            case FROZEN, GLACIAL, RIME, TERMINUS -> 0xE0FFFF;
            case NETHER, MAGMA, PYRE, CATACLYSM -> 0xFFD15C;
            case LIGHTNING, STATIC, STORM -> 0xB8F4FF;
            case NATURE, OVERGROWTH, SYLVAN, SPORE -> 0xB4FF78;
            case BLOOD -> 0xFF6B8B;
            case WATER, ABYSS -> 0x8CF4FF;
            case ASTRAL, ECLIPSE, PENUMBRA -> 0xFFE678;
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
            case WATER, OASIS, SPRING, ABYSS -> ModParticles.TINTABLE_DRIP.get();
            case SPORE, BLIGHT, MIASMA -> ModParticles.TINTABLE_ACID.get();
            case AIR, VAPOR, MIRAGE, UMBRAL, WRAITH, DUST, ARID -> ModParticles.TINTABLE_MIST.get();
            case VOID, NULL_U, ESCHATON, CATACLYSM, STORM -> ModParticles.TINTABLE_VORTEX.get();
            case AEGIS, RADIANT, CELESTIAL, APOTHEOSIS, ENTROPICA -> ModParticles.TINTABLE_SIGIL.get();
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
            case AEGIS, EARTH -> ModParticles.RUNE_ALGIZ.get();
            case NATURE, OVERGROWTH, SYLVAN, VITAE, SPRING, EMPYREAN -> ModParticles.RUNE_BERKANO.get();
            case WATER, ABYSS, OASIS -> ModParticles.RUNE_LAGUZ.get();
            case STORM, CATACLYSM, ESCHATON -> ModParticles.RUNE_HAGALAZ.get();
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
                orbital ? 10 : 14,
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
                    12,
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

        // 3. Lingering glowing Elder Futhark rune glyph every 4 ticks on non-split projectiles
        if (!splitChild && tickCount % 4 == 0) {
            ParticleType<TimedTintableParticleOption> runeType = resolveRuneParticleType(primary, tickCount / 4);
            TimedTintableParticleOption runeOpt = TimedTintableParticleOption.linear(
                    runeType,
                    aRgb,
                    16,
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
                TimedTintableParticleOption.linear(pType, pRgb, 14, 0.85F * scale, 0.16F),
                muzzlePos.x + lookDir.x * 0.35D,
                muzzlePos.y + lookDir.y * 0.35D,
                muzzlePos.z + lookDir.z * 0.35D,
                5 + tier / 2,
                0.16D, 0.16D, 0.16D,
                0.035D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(rType, aRgb, 18, 0.90F * scale, 0.05F),
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
                18,
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
                TimedTintableParticleOption.linear(accentType, aRgb, 20, 0.82F * scale, 0.12F),
                pos.x, pos.y + 0.25D, pos.z,
                accentCount,
                spread * 0.85D, spread * 0.5D, spread * 0.85D,
                0.04D
        );

        level.sendParticles(
                TimedTintableParticleOption.linear(runeType, aRgb, 24, 0.92F * scale, 0.04F),
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
                    18,
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
                    TimedTintableParticleOption.linear(runeType, aRgb, 24, 0.90F, 0.03F),
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
