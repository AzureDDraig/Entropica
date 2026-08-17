package ddraig.net.entropica.astral;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.*;

public class CelestialStarHelper {

    public record StarSkyPos(float azimuthRad, float altitudeRad, float r, float g, float b) {}

    public record LandmarkStar(String name, float azimuth, float altitude, float size, SpectralClass spectralClass, EssenceType essenceType, String info) {}

    public record AmbientStar(float azimuth, float altitude, float size, SpectralClass spectralClass, EssenceType essenceType) {}

    public static final List<LandmarkStar> LANDMARK_STARS = new ArrayList<>();
    public static final List<AmbientStar> AMBIENT_STARS = new ArrayList<>();

    static {
        // Landmark Stars
        LANDMARK_STARS.add(new LandmarkStar("Sirius (Alpha Canis)", 25.0f, 55.0f, 14.0f, SpectralClass.CLASS_A, EssenceType.ASTRAL, "Brightest Northern Guide Star | Mag: -1.46m"));
        LANDMARK_STARS.add(new LandmarkStar("Vega (Alpha Lyrae)", 78.0f, 72.0f, 12.0f, SpectralClass.CLASS_A, EssenceType.CELESTIAL, "Stellar Calibration Apex | Mag: 0.03m"));
        LANDMARK_STARS.add(new LandmarkStar("Polaris (True Celestial Pole)", 0.0f, 89.5f, 15.0f, SpectralClass.CLASS_F, EssenceType.COHESION, "True North Anchor | Mag: 1.98m"));
        LANDMARK_STARS.add(new LandmarkStar("Betelgeuse (Alpha Orionis)", 145.0f, 38.0f, 14.0f, SpectralClass.CLASS_M, EssenceType.PYRE, "Pulsing Red Supergiant | Mag: 0.50m"));
        LANDMARK_STARS.add(new LandmarkStar("Rigel (Beta Orionis)", 162.0f, 28.0f, 13.0f, SpectralClass.CLASS_B, EssenceType.GLACIAL, "Radiant Blue Supergiant | Mag: 0.13m"));
        LANDMARK_STARS.add(new LandmarkStar("Aldebaran (Alpha Tauri)", 205.0f, 44.0f, 12.0f, SpectralClass.CLASS_K, EssenceType.AMBER, "Eye of the Cosmic Taurus | Mag: 0.85m"));
        LANDMARK_STARS.add(new LandmarkStar("Capella (Alpha Aurigae)", 235.0f, 65.0f, 13.0f, SpectralClass.CLASS_G, EssenceType.RADIANT, "Quadruple Golden Star | Mag: 0.08m"));
        LANDMARK_STARS.add(new LandmarkStar("Antares (Heart of Scorpio)", 285.0f, 22.0f, 14.0f, SpectralClass.CLASS_M, EssenceType.BLOOD, "Heart of the Void | Mag: 0.96m"));
        LANDMARK_STARS.add(new LandmarkStar("Spica (Alpha Virginis)", 315.0f, 34.0f, 12.0f, SpectralClass.CLASS_B, EssenceType.STORM, "Eclipsing Blue Giant | Mag: 0.97m"));
        LANDMARK_STARS.add(new LandmarkStar("Deneb (Alpha Cygni)", 110.0f, 60.0f, 13.0f, SpectralClass.CLASS_A, EssenceType.AETHER, "Transmutation Vertex | Mag: 1.25m"));
        LANDMARK_STARS.add(new LandmarkStar("Pleiades Astral Cluster", 190.0f, 52.0f, 16.0f, SpectralClass.CLASS_O, EssenceType.ASTRAL, "Open Cosmic Stardust Cluster | Mag: 1.6m"));

        // 320 Ambient Stars with deterministic seed
        Random rng = new Random(133742L);
        SpectralClass[] classes = SpectralClass.values();
        EssenceType[] essences = EssenceType.values();
        for (int i = 0; i < 320; i++) {
            float sTheta = rng.nextFloat() * 360.0f;
            float sPhi = -25.0f + rng.nextFloat() * 115.0f; // -25° to +90°
            float sSize = 5.0f + rng.nextFloat() * 6.5f;
            SpectralClass sc = classes[rng.nextInt(classes.length)];
            EssenceType ess = essences[rng.nextInt(essences.length)];
            AMBIENT_STARS.add(new AmbientStar(sTheta, sPhi, sSize, sc, ess));
        }
    }

    public static StarSkyPos getStarSkyPositionAndColor(String starNodeId, List<Constellation> visibleConstellations) {
        if (starNodeId == null || starNodeId.isEmpty()) return null;

        if (starNodeId.startsWith("c:")) {
            // Constellation Star Node: c:<constellation_id>:<star_index>
            String[] parts = starNodeId.split(":");
            if (parts.length >= 3) {
                ResourceLocation cId = ResourceLocation.tryParse(parts[1]);
                int starIdx = Integer.parseInt(parts[2]);

                Constellation found = null;
                int totalVis = visibleConstellations.size();
                int constIndex = 0;
                for (int i = 0; i < totalVis; i++) {
                    if (visibleConstellations.get(i).getId().equals(cId)) {
                        found = visibleConstellations.get(i);
                        constIndex = i;
                        break;
                    }
                }

                if (found == null) {
                    found = ModConstellations.getById(cId).orElse(null);
                }

                if (found != null && starIdx >= 0 && starIdx < found.getStars().size()) {
                    ConstellationStar star = found.getStars().get(starIdx);
                    float baseAzimuth = (float) Math.toRadians(constIndex * (360.0f / Math.max(1, totalVis)));
                    float baseAltitude = (float) Math.toRadians(8.0f + ((constIndex * 19.5f) % 76.0f));

                    float starSphereAzimuth = baseAzimuth + (float) Math.toRadians((50.0f - star.x()) * 0.28f);
                    float starSphereAltitude = Mth.clamp(baseAltitude + (float) Math.toRadians((star.y() - 50.0f) * 0.28f), 0.0f, (float) Math.toRadians(90.0f));

                    EssenceType ess = found.getEssenceType();
                    float r = ess.getR() / 255.0f;
                    float g = ess.getG() / 255.0f;
                    float b = ess.getB() / 255.0f;
                    return new StarSkyPos(starSphereAzimuth, starSphereAltitude, r, g, b);
                }
            }
        } else if (starNodeId.startsWith("l:")) {
            // Landmark Star: l:<name>
            String name = starNodeId.substring(2);
            for (LandmarkStar ls : LANDMARK_STARS) {
                if (ls.name.equals(name)) {
                    float azim = (float) Math.toRadians(ls.azimuth);
                    float alt = (float) Math.toRadians(ls.altitude);
                    float r = ls.essenceType.getR() / 255.0f;
                    float g = ls.essenceType.getG() / 255.0f;
                    float b = ls.essenceType.getB() / 255.0f;
                    return new StarSkyPos(azim, alt, r, g, b);
                }
            }
        } else if (starNodeId.startsWith("a:")) {
            // Ambient Star: a:<index>
            try {
                int idx = Integer.parseInt(starNodeId.substring(2));
                if (idx >= 0 && idx < AMBIENT_STARS.size()) {
                    AmbientStar as = AMBIENT_STARS.get(idx);
                    float azim = (float) Math.toRadians(as.azimuth);
                    float alt = (float) Math.toRadians(as.altitude);
                    float r = as.essenceType.getR() / 255.0f;
                    float g = as.essenceType.getG() / 255.0f;
                    float b = as.essenceType.getB() / 255.0f;
                    return new StarSkyPos(azim, alt, r, g, b);
                }
            } catch (Exception ignored) {}
        }

        return null;
    }
}
