package ddraig.net.entropica.astral;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles visual celestial phenomena:
 * 1. Dynamic shooting stars / meteor showers & telescopic micro-swarms
 * 2. Deep space comets with curved ion & dust tails
 * 3. The 5 Wandering Spheres (Archon Planets with orbiting moons & ring systems)
 * 4. Transient Supernovae & expanding planetary nebula shockwave remnants
 */
public class CelestialEventHelper {

    // ----------------------------------------------------
    // METEORS & SHOOTING STARS
    // ----------------------------------------------------
    public record ActiveMeteor(
            long seed,
            float startAzim, float startAlt,
            float endAzim, float endAlt,
            float curveOffsetAzim, float curveOffsetAlt,
            float size,
            float r, float g, float b,
            float progress, // 0.0 to 1.0
            float intensity // 0.0 to 1.0 fade envelope
    ) {}

    // ----------------------------------------------------
    // COMETS
    // ----------------------------------------------------
    public record CometDefinition(
            String name,
            float baseAzim, float baseAlt,
            float orbitalSpeed, // degrees per 1000 ticks
            float comaSize,
            float tailLengthDeg,
            float tailCurvature, // curve lateral offset in degrees
            float r, float g, float b,
            float tailR, float tailG, float tailB,
            int minTier
    ) {
        public CometDefinition(String name, float baseAzim, float baseAlt, float orbitalSpeed, float comaSize, float tailLengthDeg, float tailCurvature, float r, float g, float b, float tailR, float tailG, float tailB) {
            this(name, baseAzim, baseAlt, orbitalSpeed, comaSize, tailLengthDeg, tailCurvature, r, g, b, tailR, tailG, tailB, 1);
        }
    }

    public static final List<CometDefinition> COMETS = new ArrayList<>();

    // ----------------------------------------------------
    // THE WANDERING SPHERES (ARCHON PLANETS & DWARF WORLDS)
    // ----------------------------------------------------
    public record PlanetDefinition(
            String name,
            String classification,
            ResourceLocation texture,
            float baseAzim, float baseAlt,
            float orbitalPeriodDays, // in-game days per 360 deg orbit
            float angularSize, // size on sky dome
            float distanceAU,
            float r, float g, float b,
            boolean hasRings,
            float ringRadius,
            int ringColor,
            int moonCount,
            String[] moonNames,
            int minTier
    ) {
        public PlanetDefinition(String name, String classification, ResourceLocation texture, float baseAzim, float baseAlt, float orbitalPeriodDays, float angularSize, float distanceAU, float r, float g, float b, boolean hasRings, float ringRadius, int ringColor, int moonCount, String[] moonNames) {
            this(name, classification, texture, baseAzim, baseAlt, orbitalPeriodDays, angularSize, distanceAU, r, g, b, hasRings, ringRadius, ringColor, moonCount, moonNames, 1);
        }
    }

    public static final List<PlanetDefinition> PLANETS = new ArrayList<>();

    // ----------------------------------------------------
    // TRANSIENT SUPERNOVAE
    // ----------------------------------------------------
    public record SupernovaEvent(
            String name,
            float azim, float alt,
            int cyclePeriodDays, // repeats every N in-game days
            int startDayInCycle,
            int peakDurationDays,
            int remnantDurationDays,
            float r, float g, float b
    ) {}

    public static final List<SupernovaEvent> SUPERNOVAE = new ArrayList<>();

    public record ActiveSupernovaState(
            SupernovaEvent event,
            float currentBrightness, // 0.0 to 1.0+
            float expandingRadiusDeg, // shell angular radius
            float coreTwinkle,
            String phaseName
    ) {}

    static {
        // --- 1. COMETS (Baseline & Additive Tiers) ---
        // Tier 1 (Baseline)
        COMETS.add(new CometDefinition(
                "Aethel-Halley Comet",
                45.0f, 62.0f,
                0.08f,
                16.0f,
                28.0f,
                8.5f,
                0.85f, 0.98f, 1.0f,
                0.15f, 0.85f, 0.95f,
                1
        ));

        COMETS.add(new CometDefinition(
                "Phoenix Comet",
                210.0f, 48.0f,
                -0.06f,
                14.0f,
                24.0f,
                -7.0f,
                1.0f, 0.90f, 0.65f,
                1.0f, 0.45f, 0.15f,
                1
        ));

        COMETS.add(new CometDefinition(
                "Vesper-IX Silver Comet",
                325.0f, 75.0f,
                0.05f,
                13.0f,
                22.0f,
                6.0f,
                0.90f, 0.85f, 1.0f,
                0.65f, 0.35f, 0.95f,
                1
        ));

        // Tier 2 (Additive Layer 1)
        COMETS.add(new CometDefinition(
                "Comet Zephyros-IV",
                155.0f, 40.0f,
                0.11f,
                12.0f,
                20.0f,
                5.0f,
                0.40f, 1.0f, 0.70f,
                0.10f, 0.90f, 0.50f,
                2
        ));

        // Tier 3 (Additive Layer 2)
        COMETS.add(new CometDefinition(
                "Comet Borealis-Prime",
                80.0f, 70.0f,
                -0.04f,
                15.0f,
                26.0f,
                -8.0f,
                0.30f, 0.95f, 0.90f,
                0.05f, 0.70f, 0.80f,
                3
        ));

        COMETS.add(new CometDefinition(
                "Comet Ouroboros-Omega",
                290.0f, 55.0f,
                0.03f,
                11.0f,
                18.0f,
                4.0f,
                0.95f, 0.80f, 1.0f,
                0.70f, 0.40f, 0.95f,
                3
        ));

        // --- 2. ARCHON PLANETS & DWARF WORLDS (Baseline & Additive Tiers) ---
        // Tier 1 (Baseline)
        // Aethelgard: Majestic Cyan Gas Giant with 3 orbiting moons
        PLANETS.add(new PlanetDefinition(
                "Aethelgard",
                "Archon Gas Giant",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_aethelgard.png"),
                35.0f, 42.0f,
                16.0f,
                18.0f,
                5.2f,
                0.3f, 0.85f, 0.98f,
                false, 0.0f, 0,
                3, new String[]{"Lumin", "Nox", "Aura"},
                1
        ));

        // Cryos: Pale Glacial Ice World with Razor-Thin Ice Rings
        PLANETS.add(new PlanetDefinition(
                "Cryos",
                "Glacial Ringed World",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_cryos.png"),
                125.0f, 68.0f,
                24.0f,
                15.0f,
                8.4f,
                0.75f, 0.92f, 1.0f,
                true, 1.9f, 0x90C8E8F8,
                1, new String[]{"Glacies"},
                1
        ));

        // Pyroth: Volcanic Rust Terrestrial World with glowing magma rifts
        PLANETS.add(new PlanetDefinition(
                "Pyroth",
                "Volcanic Terrestrial World",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_pyroth.png"),
                245.0f, 32.0f,
                11.0f,
                14.0f,
                1.6f,
                1.0f, 0.45f, 0.15f,
                false, 0.0f, 0,
                0, new String[0],
                1
        ));

        // Vespera: Shimmering Violet Sphere with golden cloud bands
        PLANETS.add(new PlanetDefinition(
                "Vespera",
                "Ethereal Amethyst World",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_vespera.png"),
                310.0f, 54.0f,
                19.0f,
                16.0f,
                3.1f,
                0.75f, 0.40f, 0.95f,
                false, 0.0f, 0,
                2, new String[]{"Phobos", "Deimos"},
                1
        ));

        // Chronos: Golden Gas Giant with wide double rings
        PLANETS.add(new PlanetDefinition(
                "Chronos",
                "Golden Ringed Gas Giant",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_chronos.png"),
                175.0f, 78.0f,
                32.0f,
                17.0f,
                9.8f,
                1.0f, 0.85f, 0.40f,
                true, 2.2f, 0xB0F0D880,
                2, new String[]{"Titan", "Rhea"},
                1
        ));

        // Tier 2 (Additive Layer 1)
        // Sylva: Verdant Bioluminescent Forest World with twin moons Dryas and Nyssa
        PLANETS.add(new PlanetDefinition(
                "Sylva",
                "Verdant Bioluminescent World",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_sylva.png"),
                65.0f, 30.0f,
                14.0f,
                15.0f,
                2.4f,
                0.2f, 0.95f, 0.50f,
                false, 0.0f, 0,
                2, new String[]{"Dryas", "Nyssa"},
                2
        ));

        // Tartarus: Heavy Iron-Silicate World with active sulphur rift volcanism
        PLANETS.add(new PlanetDefinition(
                "Tartarus",
                "Iron Sulphur Terrestrial World",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_tartarus.png"),
                280.0f, 45.0f,
                22.0f,
                13.0f,
                4.1f,
                0.85f, 0.55f, 0.20f,
                false, 0.0f, 0,
                0, new String[0],
                2
        ));

        // Tier 3 (Additive Layer 2)
        // Aurelia: High-Albedo Golden Core World
        PLANETS.add(new PlanetDefinition(
                "Aurelia",
                "High-Albedo Golden Core World",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_aurelia.png"),
                15.0f, 70.0f,
                28.0f,
                12.0f,
                6.8f,
                1.0f, 0.90f, 0.30f,
                false, 0.0f, 0,
                1, new String[]{"Solas"},
                3
        ));

        // Noxus: Trans-Neptunian Deep Space Void Ice Dwarf Planet
        PLANETS.add(new PlanetDefinition(
                "Noxus",
                "Trans-Neptunian Void Dwarf",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_noxus.png"),
                220.0f, 82.0f,
                48.0f,
                10.0f,
                28.5f,
                0.45f, 0.30f, 0.70f,
                false, 0.0f, 0,
                1, new String[]{"Charon"},
                3
        ));

        // Chiron Asteroid Swarm: Resonant Micro-Asteroid Belt
        PLANETS.add(new PlanetDefinition(
                "Chiron Asteroid Swarm",
                "Resonant Asteroid Belt Swarm",
                ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/planet_chiron.png"),
                140.0f, 52.0f,
                18.0f,
                11.0f,
                3.8f,
                0.70f, 0.70f, 0.75f,
                true, 1.6f, 0x70A0A8B0,
                0, new String[0],
                3
        ));

        // --- 3. TRANSIENT SUPERNOVAE ---
        SUPERNOVAE.add(new SupernovaEvent(
                "SN-Novara 1337",
                85.0f, 50.0f,
                12, // Appears every 12 in-game days
                2,  // Starts on day 2
                2,  // 2 days of peak blinding ignition
                4,  // 4 days of expanding planetary nebula remnant
                0.90f, 0.95f, 1.0f
        ));

        SUPERNOVAE.add(new SupernovaEvent(
                "SN-Pyralis Flare",
                275.0f, 62.0f,
                16,
                8,
                2,
                5,
                1.0f, 0.70f, 0.35f
        ));
    }

    public static List<PlanetDefinition> getPlanetsForTier(int tier) {
        List<PlanetDefinition> list = new ArrayList<>();
        for (PlanetDefinition p : PLANETS) {
            if (p.minTier() <= tier) {
                list.add(p);
            }
        }
        return list;
    }

    public static List<CometDefinition> getCometsForTier(int tier) {
        List<CometDefinition> list = new ArrayList<>();
        for (CometDefinition c : COMETS) {
            if (c.minTier() <= tier) {
                list.add(c);
            }
        }
        return list;
    }

    /**
     * Calculates current wandering sky position for a planet.
     */
    public static float[] getPlanetSkyPos(PlanetDefinition planet, long gameTime, float partialTick) {
        float days = (gameTime + partialTick) / 24000.0f;
        float orbitAngle = (days / planet.orbitalPeriodDays()) * 360.0f;
        float azim = (planet.baseAzim() + orbitAngle) % 360.0f;
        float alt = planet.baseAlt() + Mth.sin((float) Math.toRadians(orbitAngle * 2.0f)) * 8.0f;
        alt = Mth.clamp(alt, 10.0f, 85.0f);
        return new float[]{azim, alt};
    }

    /**
     * Evaluates active supernova events for the current in-game time.
     */
    public static List<ActiveSupernovaState> getActiveSupernovae(long gameTime, float partialTick) {
        List<ActiveSupernovaState> active = new ArrayList<>();
        float days = (gameTime + partialTick) / 24000.0f;

        for (SupernovaEvent sn : SUPERNOVAE) {
            float cycleDay = days % sn.cyclePeriodDays();
            float relDay = cycleDay - sn.startDayInCycle();

            if (relDay >= 0.0f && relDay < (sn.peakDurationDays() + sn.remnantDurationDays())) {
                float brightness;
                float radiusDeg;
                String phase;

                if (relDay < sn.peakDurationDays()) {
                    // Ignition & Peak Flare
                    float t = relDay / (float) sn.peakDurationDays();
                    brightness = 1.0f + 0.5f * (1.0f - t);
                    radiusDeg = 0.5f + t * 1.5f;
                    phase = "Peak Ignition Flare";
                } else {
                    // Expanding Remnant Shell
                    float t = (relDay - sn.peakDurationDays()) / (float) sn.remnantDurationDays();
                    brightness = (1.0f - t * 0.75f);
                    radiusDeg = 2.0f + t * 5.0f;
                    phase = "Expanding Shockwave Nebula";
                }

                float timeSec = (gameTime + partialTick) * 0.05f;
                float twinkle = 0.85f + 0.15f * Mth.sin(timeSec * 6.0f);

                active.add(new ActiveSupernovaState(
                        sn,
                        brightness,
                        radiusDeg,
                        twinkle,
                        phase
                ));
            }
        }

        return active;
    }

    /**
     * Retrieves all active primary meteors for wide-sky rendering.
     * Features extended atmospheric flight (3.5s - 5.5s) and ultra-smooth sub-tick progression.
     */
    public static List<ActiveMeteor> getActiveMeteors(long gameTime, float partialTick) {
        List<ActiveMeteor> meteors = new ArrayList<>();
        float exactTime = gameTime + partialTick;

        int[] periods = {180, 260, 340, 480};
        int[] durations = {70, 95, 85, 110};

        for (int slot = 0; slot < periods.length; slot++) {
            int p = periods[slot];
            int dur = durations[slot];
            long windowIndex = (long) (exactTime / p);
            float timeInWindow = exactTime % p;

            if (timeInWindow < dur) {
                long seed = windowIndex * 31337L + slot * 7919L;
                ActiveMeteor m = generateMeteor(seed, slot, (int) windowIndex, timeInWindow, dur);
                if (m != null) {
                    meteors.add(m);
                }
            }
        }

        return meteors;
    }

    /**
     * Retrieves active meteors for zoomed-in telescopic view (Looking Glass / Telescope).
     * Generates a radiant micro-swarm cluster (3 to 5 companion meteors)!
     */
    public static List<ActiveMeteor> getTelescopicMeteorCluster(long gameTime, float partialTick) {
        List<ActiveMeteor> primaryMeteors = getActiveMeteors(gameTime, partialTick);
        List<ActiveMeteor> cluster = new ArrayList<>();

        for (ActiveMeteor primary : primaryMeteors) {
            cluster.add(primary);

            for (int comp = 1; comp <= 3; comp++) {
                long cSeed = primary.seed() + comp * 9973L;
                int ch1 = (int) (cSeed ^ (cSeed >>> 16));
                ch1 = ch1 * 0x85ebca6b ^ (ch1 >>> 13);
                int ch2 = ch1 * 0xc2b2ae35 ^ (ch1 >>> 16);

                float dAzim = ((ch1 % 200) / 100.0f) - 1.0f;
                float dAlt = ((ch2 % 200) / 100.0f) - 1.0f;

                float timeOffset = ((ch1 >>> 8) % 24 - 12) / 100.0f;
                float cProgress = Mth.clamp(primary.progress() + timeOffset, 0.0f, 1.0f);

                if (cProgress >= 0.0f && cProgress <= 1.0f) {
                    float cIntensity = (float) Math.sin(cProgress * Math.PI);
                    if (cIntensity > 0.02f) {
                        float cSize = primary.size() * (0.65f + ((ch2 >>> 8) % 40) / 100.0f);

                        float cr = primary.r();
                        float cg = primary.g();
                        float cb = primary.b();
                        if (comp == 1) {
                            cr = Math.min(1.0f, cr * 0.7f + 0.3f);
                            cg = Math.min(1.0f, cg * 0.8f + 0.2f);
                            cb = 1.0f;
                        } else if (comp == 2) {
                            cr = 1.0f;
                            cg = Math.min(1.0f, cg * 0.9f + 0.1f);
                            cb = Math.max(0.4f, cb * 0.5f);
                        } else {
                            cr = Math.min(1.0f, cr * 0.85f + 0.25f);
                            cg = Math.max(0.4f, cg * 0.6f);
                            cb = 1.0f;
                        }

                        cluster.add(new ActiveMeteor(
                                cSeed,
                                primary.startAzim() + dAzim,
                                primary.startAlt() + dAlt,
                                primary.endAzim() + dAzim,
                                primary.endAlt() + dAlt,
                                primary.curveOffsetAzim() + dAzim * 0.3f,
                                primary.curveOffsetAlt() + dAlt * 0.3f,
                                cSize,
                                cr, cg, cb,
                                cProgress,
                                cIntensity
                        ));
                    }
                }
            }
        }

        return cluster;
    }

    private static ActiveMeteor generateMeteor(long seed, int slot, int windowIndex, float timeInWindow, int dur) {
        int h1 = (int) (seed ^ (seed >>> 16));
        h1 = h1 * 0x85ebca6b ^ (h1 >>> 13);
        int h2 = h1 * 0xc2b2ae35 ^ (h1 >>> 16);

        float startAzim = Math.abs(h1 % 360);
        float startAlt = 25.0f + (Math.abs(h2 % 5500) / 100.0f);

        float angle = ((h1 >>> 8) % 360);
        float dist = 20.0f + ((h2 >>> 8) % 2400) / 100.0f;
        float rad = (float) Math.toRadians(angle);

        float endAzim = startAzim + Mth.sin(rad) * dist;
        float endAlt = Mth.clamp(startAlt - Mth.cos(rad) * (dist * 0.7f), 10.0f, 88.0f);

        float curveAzim = (Mth.cos(rad) * (dist * 0.25f));
        float curveAlt = (Mth.sin(rad) * (dist * 0.20f));

        float rawT = timeInWindow / (float) dur;
        // Smoothstep cubic easing for continuous velocity transitions without sudden speed jumps
        float progress = rawT * rawT * (3.0f - 2.0f * rawT);
        float intensity = (float) Math.sin(progress * Math.PI);

        float r = 1.0f, g = 1.0f, b = 1.0f;
        int colType = (slot + windowIndex) % 4;
        if (colType == 1) {
            r = 0.5f; g = 0.95f; b = 1.0f;
        } else if (colType == 2) {
            r = 1.0f; g = 0.85f; b = 0.4f;
        } else if (colType == 3) {
            r = 0.85f; g = 0.5f; b = 1.0f;
        }

        float size = 5.5f + (slot % 2) * 2.0f;

        return new ActiveMeteor(
                seed,
                startAzim, startAlt,
                endAzim, endAlt,
                curveAzim, curveAlt,
                size,
                r, g, b,
                progress,
                intensity
        );
    }

    public static Vector3f getMeteorPosAt(ActiveMeteor m, float t) {
        float clampedT = Mth.clamp(t, 0.0f, 1.0f);
        float midAzim = (m.startAzim + m.endAzim) * 0.5f + m.curveOffsetAzim;
        float midAlt = (m.startAlt + m.endAlt) * 0.5f + m.curveOffsetAlt;

        float u = 1.0f - clampedT;
        float azim = u * u * m.startAzim + 2.0f * u * clampedT * midAzim + clampedT * clampedT * m.endAzim;
        float alt = u * u * m.startAlt + 2.0f * u * clampedT * midAlt + clampedT * clampedT * m.endAlt;

        float azimRad = (float) Math.toRadians(azim);
        float altRad = (float) Math.toRadians(alt);

        float cx = Mth.cos(altRad) * Mth.sin(azimRad);
        float cy = Mth.sin(altRad);
        float cz = Mth.cos(altRad) * Mth.cos(azimRad);

        return new Vector3f(cx, cy, cz);
    }
}
