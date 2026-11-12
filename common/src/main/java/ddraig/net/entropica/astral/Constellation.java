package ddraig.net.entropica.astral;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class Constellation {
    private final ResourceLocation id;
    private final String unlocalizedName;
    private final ConstellationTier tier;
    private final SpectralClass primarySpectralClass;
    private final EssenceType essenceType;
    private final float fluxRate; // Calibrated flux generation per tick
    private final int moonPhaseMask; // 8-bit mask for 8 Minecraft moon phases (bit 0 = Full Moon, bit 4 = New Moon)
    private final List<ConstellationStar> stars;
    private final List<ConstellationConnection> connections;
    private final String ritualEffect;
    private final java.util.Set<ResourceLocation> conflictingConstellations;
    private final java.util.Set<Integer> sharedStarIndices;

    public Constellation(ResourceLocation id, String unlocalizedName, ConstellationTier tier,
                         SpectralClass primarySpectralClass, EssenceType essenceType, float fluxRate,
                         int moonPhaseMask, List<ConstellationStar> stars, List<ConstellationConnection> connections,
                         String ritualEffect) {
        this(id, unlocalizedName, tier, primarySpectralClass, essenceType, fluxRate, moonPhaseMask, stars, connections, ritualEffect, java.util.Set.of(), java.util.Set.of());
    }

    public Constellation(ResourceLocation id, String unlocalizedName, ConstellationTier tier,
                         SpectralClass primarySpectralClass, EssenceType essenceType, float fluxRate,
                         int moonPhaseMask, List<ConstellationStar> stars, List<ConstellationConnection> connections,
                         String ritualEffect, java.util.Set<ResourceLocation> conflictingConstellations, java.util.Set<Integer> sharedStarIndices) {
        this.id = id;
        this.unlocalizedName = unlocalizedName;
        this.tier = tier;
        this.primarySpectralClass = primarySpectralClass;
        this.essenceType = essenceType;
        this.fluxRate = fluxRate;
        this.moonPhaseMask = moonPhaseMask;
        this.stars = List.copyOf(stars);
        this.connections = List.copyOf(connections);
        this.ritualEffect = ritualEffect;
        this.conflictingConstellations = java.util.Set.copyOf(conflictingConstellations);
        this.sharedStarIndices = java.util.Set.copyOf(sharedStarIndices);
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public ConstellationTier getTier() {
        return tier;
    }

    public SpectralClass getPrimarySpectralClass() {
        return primarySpectralClass;
    }

    public EssenceType getEssenceType() {
        return essenceType;
    }

    public float getFluxRate() {
        return fluxRate;
    }

    public int getMoonPhaseMask() {
        return moonPhaseMask;
    }

    public boolean isVisibleDuringMoonPhase(int moonPhaseIndex) {
        if (moonPhaseIndex < 0 || moonPhaseIndex > 7) return true;
        return (moonPhaseMask & (1 << moonPhaseIndex)) != 0;
    }

    public String getLunarPhasesDescription() {
        if (moonPhaseMask == 0xFF) return "All Moon Phases";
        if (moonPhaseMask == 0x01) return "Full Moon Zenith";
        if (moonPhaseMask == 0x10) return "New Moon / The Void";
        if (tier == ConstellationTier.FUNDAMENTAL) return "Crescent & Quarter Moons";
        if (tier == ConstellationTier.ADVANCED) return "Gibbous & Full Moons";
        if (tier == ConstellationTier.MASTER) return "Full Moon Peak Zenith";
        return "New Moon & Cosmic Alignment";
    }

    public List<ConstellationStar> getStars() {
        return stars;
    }

    public List<ConstellationConnection> getConnections() {
        return connections;
    }

    public String getRitualEffect() {
        return ritualEffect;
    }

    public java.util.Set<ResourceLocation> getConflictingConstellations() {
        return conflictingConstellations;
    }

    public boolean isConflictingWith(Constellation other) {
        if (other == null) return false;
        return this.conflictingConstellations.contains(other.getId()) || other.getConflictingConstellations().contains(this.getId());
    }

    public java.util.Set<Integer> getSharedStarIndices() {
        return sharedStarIndices;
    }

    private float celestialAzimuthDeg = 0.0f;
    private float celestialAltitudeDeg = 0.0f;
    private float celestialAzimuthRad = 0.0f;
    private float celestialAltitudeRad = 0.0f;
    private float[] starSphereAzimuthRad = new float[0];
    private float[] starSphereAltitudeRad = new float[0];

    public void setCelestialCoordinates(float azimDeg, float altDeg) {
        this.celestialAzimuthDeg = azimDeg;
        this.celestialAltitudeDeg = altDeg;
        this.celestialAzimuthRad = (float) Math.toRadians(azimDeg);
        this.celestialAltitudeRad = (float) Math.toRadians(altDeg);

        int count = (stars != null) ? stars.size() : 0;
        this.starSphereAzimuthRad = new float[count];
        this.starSphereAltitudeRad = new float[count];

        float angularSpanRad = (float) Math.toRadians(14.0f);

        // Center unit normal vector
        float nx = net.minecraft.util.Mth.cos(celestialAltitudeRad) * net.minecraft.util.Mth.sin(celestialAzimuthRad);
        float ny = net.minecraft.util.Mth.sin(celestialAltitudeRad);
        float nz = net.minecraft.util.Mth.cos(celestialAltitudeRad) * net.minecraft.util.Mth.cos(celestialAzimuthRad);

        // East/Right unit vector (Tangent along latitude)
        float ux = net.minecraft.util.Mth.cos(celestialAzimuthRad);
        float uy = 0.0f;
        float uz = -net.minecraft.util.Mth.sin(celestialAzimuthRad);

        // North/Up unit vector (Tangent along meridian: N x U)
        float vx = -net.minecraft.util.Mth.sin(celestialAltitudeRad) * net.minecraft.util.Mth.sin(celestialAzimuthRad);
        float vy = net.minecraft.util.Mth.cos(celestialAltitudeRad);
        float vz = -net.minecraft.util.Mth.sin(celestialAltitudeRad) * net.minecraft.util.Mth.cos(celestialAzimuthRad);

        for (int i = 0; i < count; i++) {
            ConstellationStar s = stars.get(i);
            // u: horizontal offset (-half_span to +half_span)
            float u = ((s.x() - 50.0f) / 50.0f) * (angularSpanRad * 0.5f);
            // v: vertical offset (0 at top of chart -> +half_span, 100 at bottom of chart -> -half_span)
            float v = ((50.0f - s.y()) / 50.0f) * (angularSpanRad * 0.5f);

            float px = nx + u * ux + v * vx;
            float py = ny + u * uy + v * vy;
            float pz = nz + u * uz + v * vz;
            float pLen = (float) Math.sqrt(px * px + py * py + pz * pz);
            if (pLen > 1e-4f) {
                px /= pLen;
                py /= pLen;
                pz /= pLen;
            }

            float starAlt = (float) Math.asin(net.minecraft.util.Mth.clamp(py, -1.0f, 1.0f));
            float starAzim = (float) Math.atan2(px, pz);
            if (starAzim < 0.0f) starAzim += (float) (2.0 * Math.PI);

            this.starSphereAzimuthRad[i] = starAzim;
            this.starSphereAltitudeRad[i] = starAlt;
        }
    }

    public float getCelestialAzimuthDeg() {
        return celestialAzimuthDeg;
    }

    public float getCelestialAltitudeDeg() {
        return celestialAltitudeDeg;
    }

    public float getCelestialAzimuthRad() {
        return celestialAzimuthRad;
    }

    public float getCelestialAltitudeRad() {
        return celestialAltitudeRad;
    }

    public float getStarSphereAzimuth(int starIndex) {
        if (starIndex >= 0 && starIndex < starSphereAzimuthRad.length) {
            return starSphereAzimuthRad[starIndex];
        }
        return celestialAzimuthRad;
    }

    public float getStarSphereAltitude(int starIndex) {
        if (starIndex >= 0 && starIndex < starSphereAltitudeRad.length) {
            return starSphereAltitudeRad[starIndex];
        }
        return celestialAltitudeRad;
    }
}
