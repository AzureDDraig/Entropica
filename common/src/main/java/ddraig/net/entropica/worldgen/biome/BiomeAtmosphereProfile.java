package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.config.EntropicaBiome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Declarative atmosphere, custom fog, shader/Sodium-compatible sky rendering,
 * wind-drifted ambient particle, and gameplay environmental profile for an {@link EntropicaBiome}.
 */
public final class BiomeAtmosphereProfile {
    private final EntropicaBiome biome;

    // --- Custom Fog Settings ---
    private final boolean customFogDistance;
    private final float fogStartBlocks;
    private final float fogEndBlocks;
    private final float fogBlendStrength;
    private final float lightHeldFogReliefBlocks;
    private final int dayFogTintRgb;    // -1 if unused
    private final int nightFogColorRgb; // -1 if unused

    // --- Custom Sky & Shader/Sodium-Compatible Phenomenon Settings ---
    private final int nightSkyColorRgb; // -1 if unused (prevents vanilla's 0.0 midnight black-out!)
    private final float daySkyBrightnessScale; // 1.0f normal, < 1.0f for Eclipse twilight biomes
    private final float starBrightnessBoost;   // 0.0f..0.85f extra star visibility
    private final SkyPhenomenonType skyPhenomenon;
    private final int phenomenonPrimaryRgb;
    private final int phenomenonSecondaryRgb;
    private final float phenomenonIntensity;

    // --- Client Wind-Drifted Ambient Particles ---
    private final int ambientParticlesPerTick;
    private final int particleColorRgb;
    private final float particleScale;
    private final int particleLifetimeTicks;
    private final float particleWindVx;
    private final float particleWindVy;
    private final float particleWindVz;

    // --- Gameplay Environmental Physics & Regional Effects ---
    private final float descentSpeedMultiplier; // 1.0f = vanilla, 1.15f = +15% Aegis fall speed
    private final int jumpBoostAmplifier;       // -1 = none, 0 = Jump Boost I
    private final int passiveRegenIntervalTicks; // 0 = none
    private final BiConsumer<ServerLevel, LivingEntity> customServerTick;

    private BiomeAtmosphereProfile(Builder b) {
        this.biome = b.biome;
        this.customFogDistance = b.customFogDistance;
        this.fogStartBlocks = b.fogStartBlocks;
        this.fogEndBlocks = b.fogEndBlocks;
        this.fogBlendStrength = b.fogBlendStrength;
        this.lightHeldFogReliefBlocks = b.lightHeldFogReliefBlocks;
        this.dayFogTintRgb = b.dayFogTintRgb;
        this.nightFogColorRgb = b.nightFogColorRgb;

        this.nightSkyColorRgb = b.nightSkyColorRgb;
        this.daySkyBrightnessScale = b.daySkyBrightnessScale;
        this.starBrightnessBoost = b.starBrightnessBoost;
        this.skyPhenomenon = b.skyPhenomenon;
        this.phenomenonPrimaryRgb = b.phenomenonPrimaryRgb;
        this.phenomenonSecondaryRgb = b.phenomenonSecondaryRgb;
        this.phenomenonIntensity = b.phenomenonIntensity;

        this.ambientParticlesPerTick = b.ambientParticlesPerTick;
        this.particleColorRgb = b.particleColorRgb;
        this.particleScale = b.particleScale;
        this.particleLifetimeTicks = b.particleLifetimeTicks;
        this.particleWindVx = b.particleWindVx;
        this.particleWindVy = b.particleWindVy;
        this.particleWindVz = b.particleWindVz;

        this.descentSpeedMultiplier = b.descentSpeedMultiplier;
        this.jumpBoostAmplifier = b.jumpBoostAmplifier;
        this.passiveRegenIntervalTicks = b.passiveRegenIntervalTicks;
        this.customServerTick = b.customServerTick;
    }

    public EntropicaBiome getBiome() { return biome; }
    public boolean hasCustomFogDistance() { return customFogDistance; }
    public float getFogStartBlocks() { return fogStartBlocks; }
    public float getFogEndBlocks() { return fogEndBlocks; }
    public float getFogBlendStrength() { return fogBlendStrength; }
    public float getLightHeldFogReliefBlocks() { return lightHeldFogReliefBlocks; }
    public int getDayFogTintRgb() { return dayFogTintRgb; }
    public int getNightFogColorRgb() { return nightFogColorRgb; }

    public int getNightSkyColorRgb() { return nightSkyColorRgb; }
    public float getDaySkyBrightnessScale() { return daySkyBrightnessScale; }
    public float getStarBrightnessBoost() { return starBrightnessBoost; }
    public SkyPhenomenonType getSkyPhenomenon() { return skyPhenomenon; }
    public int getPhenomenonPrimaryRgb() { return phenomenonPrimaryRgb; }
    public int getPhenomenonSecondaryRgb() { return phenomenonSecondaryRgb; }
    public float getPhenomenonIntensity() { return phenomenonIntensity; }

    public int getAmbientParticlesPerTick() { return ambientParticlesPerTick; }
    public int getParticleColorRgb() { return particleColorRgb; }
    public float getParticleScale() { return particleScale; }
    public int getParticleLifetimeTicks() { return particleLifetimeTicks; }
    public float getParticleWindVx() { return particleWindVx; }
    public float getParticleWindVy() { return particleWindVy; }
    public float getParticleWindVz() { return particleWindVz; }

    public float getDescentSpeedMultiplier() { return descentSpeedMultiplier; }
    public int getJumpBoostAmplifier() { return jumpBoostAmplifier; }
    public int getPassiveRegenIntervalTicks() { return passiveRegenIntervalTicks; }
    public BiConsumer<ServerLevel, LivingEntity> getCustomServerTick() { return customServerTick; }

    public static Builder builder(EntropicaBiome biome) {
        return new Builder(biome);
    }

    public static final class Builder {
        private final EntropicaBiome biome;
        private boolean customFogDistance = false;
        private float fogStartBlocks = 0.0F;
        private float fogEndBlocks = 192.0F;
        private float fogBlendStrength = 1.0F;
        private float lightHeldFogReliefBlocks = 0.0F;
        private int dayFogTintRgb = -1;
        private int nightFogColorRgb = -1;

        private int nightSkyColorRgb = -1;
        private float daySkyBrightnessScale = 1.0F;
        private float starBrightnessBoost = 0.0F;
        private SkyPhenomenonType skyPhenomenon = SkyPhenomenonType.NONE;
        private int phenomenonPrimaryRgb = 0xFFFFFF;
        private int phenomenonSecondaryRgb = 0xA5F3FC;
        private float phenomenonIntensity = 0.65F;

        private int ambientParticlesPerTick = 0;
        private int particleColorRgb = 0xDCF0FF;
        private float particleScale = 0.7F;
        private int particleLifetimeTicks = 60;
        private float particleWindVx = 0.0F;
        private float particleWindVy = -0.02F;
        private float particleWindVz = 0.0F;

        private float descentSpeedMultiplier = 1.0F;
        private int jumpBoostAmplifier = -1;
        private int passiveRegenIntervalTicks = 0;
        private BiConsumer<ServerLevel, LivingEntity> customServerTick = null;

        private Builder(EntropicaBiome biome) {
            this.biome = Objects.requireNonNull(biome, "biome");
        }

        public Builder fogDistance(float startBlocks, float endBlocks, float blendStrength) {
            this.customFogDistance = true;
            this.fogStartBlocks = startBlocks;
            this.fogEndBlocks = Math.max(startBlocks + 4.0F, endBlocks);
            this.fogBlendStrength = Math.max(0.0F, Math.min(1.0F, blendStrength));
            return this;
        }

        public Builder lightHeldFogRelief(float bonusBlocksWhenHoldingLight) {
            this.lightHeldFogReliefBlocks = Math.max(0.0F, bonusBlocksWhenHoldingLight);
            return this;
        }

        public Builder fogColors(int dayFogTintRgb, int nightFogColorRgb) {
            this.dayFogTintRgb = dayFogTintRgb;
            this.nightFogColorRgb = nightFogColorRgb;
            return this;
        }

        public Builder nightSkyColor(int rgb) {
            this.nightSkyColorRgb = rgb & 0xFFFFFF;
            return this;
        }

        public Builder daySkyBrightnessScale(float scale) {
            this.daySkyBrightnessScale = Math.max(0.05F, Math.min(1.5F, scale));
            return this;
        }

        public Builder starBrightnessBoost(float boost) {
            this.starBrightnessBoost = Math.max(0.0F, Math.min(1.0F, boost));
            return this;
        }

        public Builder skyPhenomenon(SkyPhenomenonType type, int primaryRgb, int secondaryRgb, float intensity) {
            this.skyPhenomenon = Objects.requireNonNull(type, "type");
            this.phenomenonPrimaryRgb = primaryRgb & 0xFFFFFF;
            this.phenomenonSecondaryRgb = secondaryRgb & 0xFFFFFF;
            this.phenomenonIntensity = Math.max(0.0F, Math.min(1.0F, intensity));
            return this;
        }

        public Builder driftingParticles(int countPerTick, int colorRgb, float scale, int lifetimeTicks,
                                         float windVx, float windVy, float windVz) {
            this.ambientParticlesPerTick = Math.max(0, countPerTick);
            this.particleColorRgb = colorRgb & 0xFFFFFF;
            this.particleScale = scale;
            this.particleLifetimeTicks = Math.max(10, lifetimeTicks);
            this.particleWindVx = windVx;
            this.particleWindVy = windVy;
            this.particleWindVz = windVz;
            return this;
        }

        public Builder descentSpeedMultiplier(float multiplier) {
            this.descentSpeedMultiplier = Math.max(0.1F, Math.min(3.0F, multiplier));
            return this;
        }

        public Builder jumpBoost(int amplifier) {
            this.jumpBoostAmplifier = amplifier;
            return this;
        }

        public Builder passiveSanctuaryRegen(int intervalTicks) {
            this.passiveRegenIntervalTicks = Math.max(0, intervalTicks);
            return this;
        }

        public Builder customServerTick(BiConsumer<ServerLevel, LivingEntity> tickAction) {
            this.customServerTick = tickAction;
            return this;
        }

        public BiomeAtmosphereProfile build() {
            return new BiomeAtmosphereProfile(this);
        }
    }
}
