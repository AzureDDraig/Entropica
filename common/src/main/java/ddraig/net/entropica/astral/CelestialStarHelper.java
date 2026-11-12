package ddraig.net.entropica.astral;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.*;

public class CelestialStarHelper {

    public record StarSkyPos(float azimuthRad, float altitudeRad, float r, float g, float b) {}

    public record LandmarkStar(String name, float azimuth, float altitude, float size, SpectralClass spectralClass, EssenceType essenceType, String info) {}

    public record AmbientStar(String name, float azimuth, float altitude, float size, SpectralClass spectralClass, EssenceType essenceType, int minTier, float apparentMagnitude) {
        public AmbientStar(String name, float azimuth, float altitude, float size, SpectralClass spectralClass, EssenceType essenceType) {
            this(name, azimuth, altitude, size, spectralClass, essenceType, 1, 2.0f);
        }
    }

    public static final List<LandmarkStar> LANDMARK_STARS = new ArrayList<>();
    public static final List<AmbientStar> AMBIENT_STARS = new ArrayList<>();

    private static final String[] STAR_ROOTS = {
            "Aethel", "Zephyros", "Thalassa", "Astralis", "Caelum", "Vesper", "Aeternus", "Pyralis",
            "Novara", "Nyx", "Cor", "Alcoris", "Zenith", "Sanguinis", "Ignis", "Mortis",
            "Umbralis", "Borealis", "Australis", "Celestia", "Lyrae", "Orionis", "Draconis", "Hydrae",
            "Cygni", "Persei", "Cassiopeiae", "Aquilae", "Serpentis", "Pegasi", "Centauri", "Eridani",
            "Crucis", "Aurigae", "Tauri", "Lupi", "Phoenicis", "Velorum", "Carinae", "Puppis",
            "Fornacis", "Mensae", "Octantis", "Pictoris", "Volantis", "Pavonis", "Gruis", "Tucanae",
            "Indi", "Chamaeleontis", "Muscae", "Apodis", "Circini", "Normae", "Arae", "Telescopii",
            "Coronae", "Scuti", "Sagittae", "Vulpeculae", "Lacertae", "Lyncis", "Leonis", "Monocerotis"
    };

    private static final String[] STAR_DESIGNATIONS = {
            "Prime", "Major", "Minor", "Alpha", "Beta", "Gamma", "Delta", "Epsilon",
            "Zeta", "Eta", "Theta", "Iota", "Kappa", "Lambda", "Mu", "Nu",
            "Xi", "Omicron", "Pi", "Rho", "Sigma", "Tau", "Upsilon", "Phi",
            "Chi", "Psi", "Omega", "IV", "VII", "IX", "XII", "Secundus",
            "Tertius", "Nova", "Apex", "Aeterna", "V", "VI", "VIII", "XI"
    };

    private static final String[] GREEK_LETTERS = {
            "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta",
            "Iota", "Kappa", "Lambda", "Mu", "Nu", "Xi", "Omicron", "Pi",
            "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi", "Psi", "Omega"
    };

    public static final EssenceType[] STELLAR_ESSENCE_POOL = {
            // Base Types
            EssenceType.AIR, EssenceType.WATER, EssenceType.NATURE, EssenceType.EARTH,
            EssenceType.FROZEN, EssenceType.NETHER, EssenceType.RADIANT, EssenceType.UMBRAL,
            EssenceType.UNDEAD, EssenceType.VOID, EssenceType.ARID, EssenceType.LIGHTNING,

            // Tier 1 Elemental, Vital & Ethereal Fusions
            EssenceType.MAGMA, EssenceType.STORM, EssenceType.GLACIAL, EssenceType.OVERGROWTH,
            EssenceType.DUST, EssenceType.SPORE, EssenceType.TAIGA, EssenceType.OASIS,
            EssenceType.VITAE, EssenceType.ECLIPSE, EssenceType.BLIGHT, EssenceType.SOULFIRE,
            EssenceType.VAPOR, EssenceType.NULL_R, EssenceType.NULL_U, EssenceType.DAWN,
            EssenceType.ABYSS, EssenceType.AEGIS, EssenceType.AURORA,

            // Tier 2 Fusions
            EssenceType.BLOOD, EssenceType.AURA, EssenceType.AMBER, EssenceType.ASTRAL,
            EssenceType.MIRAGE, EssenceType.WRAITH, EssenceType.BARROW,

            // Tier 3 Trinity Fusions
            EssenceType.PYRE, EssenceType.PENUMBRA, EssenceType.RIME, EssenceType.SPRING,
            EssenceType.STATIC, EssenceType.SYLVAN, EssenceType.MIASMA, EssenceType.GENESIS,
            EssenceType.OBLIVION,

            // Tier 4 Apex Fusions
            EssenceType.AETHER, EssenceType.ENTROPIC, EssenceType.CELESTIAL
    };

    static {
        // Landmark Stars with randomly chosen essence types from Base through Tier 4 pool
        Random landmarkRng = new Random(987654L);
        LANDMARK_STARS.add(new LandmarkStar("Sirius (Alpha Canis)", 25.0f, 55.0f, 14.0f, SpectralClass.CLASS_A, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Brightest Northern Guide Star | Mag: -1.46m"));
        LANDMARK_STARS.add(new LandmarkStar("Vega (Alpha Lyrae)", 78.0f, 72.0f, 12.0f, SpectralClass.CLASS_A, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Stellar Calibration Apex | Mag: 0.03m"));
        LANDMARK_STARS.add(new LandmarkStar("Polaris (True Celestial Pole)", 0.0f, 89.5f, 15.0f, SpectralClass.CLASS_F, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "True North Anchor | Mag: 1.98m"));
        LANDMARK_STARS.add(new LandmarkStar("Betelgeuse (Alpha Orionis)", 145.0f, 38.0f, 14.0f, SpectralClass.CLASS_M, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Pulsing Red Supergiant | Mag: 0.50m"));
        LANDMARK_STARS.add(new LandmarkStar("Rigel (Beta Orionis)", 162.0f, 28.0f, 13.0f, SpectralClass.CLASS_B, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Radiant Blue Supergiant | Mag: 0.13m"));
        LANDMARK_STARS.add(new LandmarkStar("Aldebaran (Alpha Tauri)", 205.0f, 44.0f, 12.0f, SpectralClass.CLASS_K, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Eye of the Cosmic Taurus | Mag: 0.85m"));
        LANDMARK_STARS.add(new LandmarkStar("Capella (Alpha Aurigae)", 235.0f, 65.0f, 13.0f, SpectralClass.CLASS_G, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Quadruple Golden Star | Mag: 0.08m"));
        LANDMARK_STARS.add(new LandmarkStar("Antares (Heart of Scorpio)", 285.0f, 22.0f, 14.0f, SpectralClass.CLASS_M, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Heart of the Void | Mag: 0.96m"));
        LANDMARK_STARS.add(new LandmarkStar("Spica (Alpha Virginis)", 315.0f, 34.0f, 12.0f, SpectralClass.CLASS_B, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Eclipsing Blue Giant | Mag: 0.97m"));
        LANDMARK_STARS.add(new LandmarkStar("Deneb (Alpha Cygni)", 110.0f, 60.0f, 13.0f, SpectralClass.CLASS_A, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Transmutation Vertex | Mag: 1.25m"));
        LANDMARK_STARS.add(new LandmarkStar("Pleiades Astral Cluster", 190.0f, 52.0f, 16.0f, SpectralClass.CLASS_O, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Open Cosmic Stardust Cluster | Mag: 1.6m"));

        // Southern & Low-Declination Landmark Stars (Down to -15°)
        LANDMARK_STARS.add(new LandmarkStar("Canopus (Alpha Carinae)", 185.0f, -12.0f, 14.5f, SpectralClass.CLASS_F, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Second Brightest Star in the Firmament | Mag: -0.74m"));
        LANDMARK_STARS.add(new LandmarkStar("Achernar (Alpha Eridani)", 242.0f, -14.5f, 13.5f, SpectralClass.CLASS_B, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Vibrant Blue Southern Beacon | Mag: 0.45m"));
        LANDMARK_STARS.add(new LandmarkStar("Fomalhaut (Alpha Piscis Austrini)", 332.0f, -9.5f, 13.0f, SpectralClass.CLASS_A, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "The Solitary Autumn Watcher | Mag: 1.17m"));
        LANDMARK_STARS.add(new LandmarkStar("Deneb Kaitos (Beta Ceti)", 302.0f, -11.5f, 12.0f, SpectralClass.CLASS_K, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Orange Giant of the Deep South | Mag: 2.04m"));
        LANDMARK_STARS.add(new LandmarkStar("Procyon (Alpha Canis Minoris)", 112.0f, 5.2f, 13.8f, SpectralClass.CLASS_F, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Golden-White Sub-Horizon Anchor | Mag: 0.34m"));
        LANDMARK_STARS.add(new LandmarkStar("Altair (Alpha Aquilae)", 62.0f, 8.5f, 13.2f, SpectralClass.CLASS_A, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "Rapid Rotator of the Summer Triangle | Mag: 0.77m"));
        LANDMARK_STARS.add(new LandmarkStar("Alphard (Alpha Hydrae)", 150.0f, -8.0f, 12.5f, SpectralClass.CLASS_K, STELLAR_ESSENCE_POOL[landmarkRng.nextInt(STELLAR_ESSENCE_POOL.length)], "The Solitary Serpent Heart | Mag: 1.98m"));

        // 960 Ambient Stars with randomly chosen essence types from Base through Tier 4 pool
        Random rng = new Random(133742L);
        SpectralClass[] classes = SpectralClass.values();
        Set<String> usedNames = new HashSet<>();

        for (int i = 0; i < 960; i++) {
            float sTheta = rng.nextFloat() * 360.0f;
            float sPhi;
            if (i < 380) {
                // Dense Southern & Equatorial Horizon band: -15.0° to +15.0°
                sPhi = -15.0f + rng.nextFloat() * 30.0f;
            } else {
                // Northern & Mid-Sky dome: +15.0° to +90.0°
                sPhi = 15.0f + rng.nextFloat() * 75.0f;
            }
            SpectralClass sc = classes[rng.nextInt(classes.length)];
            EssenceType ess = STELLAR_ESSENCE_POOL[rng.nextInt(STELLAR_ESSENCE_POOL.length)];

            int tier;
            float mag;
            float sSize;

            if (i < 300) {
                // Tier 1: Baseline Bright Ambient Stars (0.5m to 2.0m)
                tier = 1;
                mag = 0.5f + rng.nextFloat() * 1.5f;
                sSize = 5.0f + rng.nextFloat() * 6.5f;
            } else if (i < 550) {
                // Tier 2: Additive Medium Ambient Stars (2.0m to 2.6m)
                tier = 2;
                mag = 2.0f + rng.nextFloat() * 0.6f;
                sSize = 4.0f + rng.nextFloat() * 4.5f;
            } else if (i < 780) {
                // Tier 3: Additive Deep-Sky Ambient Stars (2.6m to 3.3m)
                tier = 3;
                mag = 2.6f + rng.nextFloat() * 0.7f;
                sSize = 3.0f + rng.nextFloat() * 3.5f;
            } else {
                // Tier 4: Additive Telescopic Background Stars (3.3m to 4.0m)
                tier = 4;
                mag = 3.3f + rng.nextFloat() * 0.7f;
                sSize = 2.2f + rng.nextFloat() * 2.8f;
            }

            String root = STAR_ROOTS[i % STAR_ROOTS.length];
            String desig = STAR_DESIGNATIONS[(i * 7 + (i / STAR_ROOTS.length)) % STAR_DESIGNATIONS.length];
            String starName = root + "-" + desig;
            if (usedNames.contains(starName)) {
                starName = root + " " + desig + " (" + (i + 1) + ")";
            }
            usedNames.add(starName);

            AMBIENT_STARS.add(new AmbientStar(starName, sTheta, sPhi, sSize, sc, ess, tier, mag));
        }
    }

    public static String getConstellationStarName(Constellation c, int starIndex) {
        if (c == null) return "Unknown Star";
        String greek = (starIndex >= 0 && starIndex < GREEK_LETTERS.length) ? GREEK_LETTERS[starIndex] : ("Star-" + (starIndex + 1));
        String path = c.getId().getPath();
        String latinGenitive = formatConstellationGenitive(path);
        return greek + " " + latinGenitive;
    }

    private static String formatConstellationGenitive(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (int p = 0; p < parts.length; p++) {
            String word = parts[p];
            if (word.isEmpty()) continue;
            String capitalized = Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            if (sb.length() > 0) sb.append(" ");
            sb.append(capitalized);
        }
        return sb.toString();
    }

    public static StarSkyPos getStarSkyPositionAndColor(String starNodeId) {
        return getStarSkyPositionAndColor(starNodeId, null);
    }

    public static StarSkyPos getStarSkyPositionAndColor(String starNodeId, Collection<Constellation> visibleConstellations) {
        if (starNodeId == null || starNodeId.isEmpty()) return null;

        SupernovaManager.OverriddenStar ov = SupernovaManager.getOverriddenStar(starNodeId);
        if (ov != null) {
            float azim = (float) Math.toRadians(ov.azimuth());
            float alt = (float) Math.toRadians(ov.altitude());
            float r = ov.essenceType().getR() / 255.0f;
            float g = ov.essenceType().getG() / 255.0f;
            float b = ov.essenceType().getB() / 255.0f;
            return new StarSkyPos(azim, alt, r, g, b);
        }

        if (starNodeId.startsWith("c:")) {
            // Constellation Star Node format: c:<namespace:path>:<star_index>
            try {
                int firstColon = starNodeId.indexOf(':');
                int lastColon = starNodeId.lastIndexOf(':');
                if (firstColon >= 0 && lastColon > firstColon) {
                    String resLocStr = starNodeId.substring(firstColon + 1, lastColon);
                    int starIdx = Integer.parseInt(starNodeId.substring(lastColon + 1));
                    ResourceLocation cId = ResourceLocation.tryParse(resLocStr);

                    if (cId != null) {
                        Constellation found = ModConstellations.getById(cId).orElse(null);

                        if (found != null && starIdx >= 0 && starIdx < found.getStars().size()) {
                            float starSphereAzimuth = found.getStarSphereAzimuth(starIdx);
                            float starSphereAltitude = found.getStarSphereAltitude(starIdx);

                            EssenceType ess = found.getEssenceType();
                            float r = ess.getR() / 255.0f;
                            float g = ess.getG() / 255.0f;
                            float b = ess.getB() / 255.0f;
                            return new StarSkyPos(starSphereAzimuth, starSphereAltitude, r, g, b);
                        }
                    }
                }
            } catch (Exception ignored) {}
        } else if (starNodeId.startsWith("l:")) {
            // Landmark Star: l:<name>
            String name = starNodeId.substring(2);
            for (LandmarkStar ls : LANDMARK_STARS) {
                if (ls.name().equals(name)) {
                    float azim = (float) Math.toRadians(ls.azimuth());
                    float alt = (float) Math.toRadians(ls.altitude());
                    float r = ls.essenceType().getR() / 255.0f;
                    float g = ls.essenceType().getG() / 255.0f;
                    float b = ls.essenceType().getB() / 255.0f;
                    return new StarSkyPos(azim, alt, r, g, b);
                }
            }
        } else if (starNodeId.startsWith("a:")) {
            // Ambient Star: a:<index>
            try {
                int idx = Integer.parseInt(starNodeId.substring(2));
                if (idx >= 0 && idx < AMBIENT_STARS.size()) {
                    AmbientStar as = AMBIENT_STARS.get(idx);
                    float azim = (float) Math.toRadians(as.azimuth());
                    float alt = (float) Math.toRadians(as.altitude());
                    float r = as.essenceType().getR() / 255.0f;
                    float g = as.essenceType().getG() / 255.0f;
                    float b = as.essenceType().getB() / 255.0f;
                    return new StarSkyPos(azim, alt, r, g, b);
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    /**
     * Calculates the slowly shifting RGB starlight hue for a given spectral class and essence type.
     * The color smoothly and subtly shifts between the spectral blackbody core and the essence palette
     * over a very slow sinusoidal period (~18-24 seconds), allowing players to visually identify a star's
     * celestial essence purely through its chromatic starlight without text labels.
     */
    public static float[] getShiftingStarRGB(SpectralClass spectralClass, EssenceType essenceType, float timeSec, float seed) {
        int specInt = (spectralClass != null) ? spectralClass.getColorRgb() : 0xFFFFFF;
        float sr = ((specInt >> 16) & 0xFF) / 255.0f;
        float sg = ((specInt >> 8) & 0xFF) / 255.0f;
        float sb = (specInt & 0xFF) / 255.0f;

        if (essenceType == null) {
            return new float[]{sr, sg, sb};
        }

        int[] essRgbInts;
        if (essenceType.isDynamic()) {
            float hue = ((timeSec * 0.04f + seed * 0.08f) % 1.0f);
            if (hue < 0) hue += 1.0f;
            int dynRgb = ddraig.net.entropica.registry.AestheticGlassRegistry.hsbToRgb(hue, 0.85f, 1.0f);
            essRgbInts = new int[]{(dynRgb >> 16) & 0xFF, (dynRgb >> 8) & 0xFF, dynRgb & 0xFF};
        } else {
            int[][] cycle = essenceType.getColorCycle();
            if (cycle == null || cycle.length == 0) {
                essRgbInts = new int[]{essenceType.getR(), essenceType.getG(), essenceType.getB()};
            } else if (cycle.length == 1) {
                essRgbInts = cycle[0];
            } else {
                // Very slow cycle progression through stages (~20s per full cycle)
                float cyclePos = Math.abs((timeSec * 0.045f + seed * 0.15f) % 1.0f) * cycle.length;
                int idx1 = (int) cyclePos;
                int idx2 = (idx1 + 1) % cycle.length;
                float blend = cyclePos - idx1;
                int[] c1 = cycle[idx1];
                int[] c2 = cycle[idx2];
                essRgbInts = new int[]{
                        (int) Mth.lerp(blend, c1[0], c2[0]),
                        (int) Mth.lerp(blend, c1[1], c2[1]),
                        (int) Mth.lerp(blend, c1[2], c2[2])
                };
            }
        }

        float er = essRgbInts[0] / 255.0f;
        float eg = essRgbInts[1] / 255.0f;
        float eb = essRgbInts[2] / 255.0f;

        // Very slow sinusoidal wave between spectral baseline and vibrant essence aura (period ~18s)
        float wave = 0.5f + 0.5f * Mth.sin(timeSec * 0.32f + seed);
        float shiftFactor = Mth.lerp(wave, 0.45f, 0.95f);

        float finalR = Mth.lerp(shiftFactor, sr, er);
        float finalG = Mth.lerp(shiftFactor, sg, eg);
        float finalB = Mth.lerp(shiftFactor, sb, eb);

        return new float[]{finalR, finalG, finalB};
    }

    public static int getShiftingStarRgbInt(SpectralClass spectralClass, EssenceType essenceType, float timeSec, float seed) {
        float[] rgb = getShiftingStarRGB(spectralClass, essenceType, timeSec, seed);
        int r = Mth.clamp((int) (rgb[0] * 255.0f), 0, 255);
        int g = Mth.clamp((int) (rgb[1] * 255.0f), 0, 255);
        int b = Mth.clamp((int) (rgb[2] * 255.0f), 0, 255);
        return (r << 16) | (g << 8) | b;
    }
}
