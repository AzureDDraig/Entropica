package ddraig.net.entropica.astral;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.network.ModNetwork;
import ddraig.net.entropica.network.SyncSupernovaPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SupernovaManager {

    public record OverriddenStar(
            String name,
            float azimuth,
            float altitude,
            float size,
            SpectralClass spectralClass,
            EssenceType essenceType,
            StellarRemnantType remnantType,
            SupernovaPhase phase,
            float progress,
            float bipolarAngleRad
    ) {}

    public record FigureEightPuff(
            float dAzim,
            float dAlt,
            float size,
            float r,
            float g,
            float b,
            float a,
            float rotSpeed,
            float phase
    ) {}

    private static final ConcurrentHashMap<UUID, SupernovaEvent> ACTIVE_EVENTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, SupernovaEvent> EVENTS_BY_STAR = new ConcurrentHashMap<>();

    // Duration constants (in Minecraft ticks):
    // Precursor: 24,000 ticks (1 in-game day)
    // Flash: 6,000 ticks (0.25 in-game days)
    // Expanding Nebula: 72,000 ticks (3 in-game days)
    // Remnant: Infinite / Permanent
    public static final int DURATION_PRECURSOR = 24000;
    public static final int DURATION_FLASH = 6000;
    public static final int DURATION_EXPANSION = 72000;

    public static Collection<SupernovaEvent> getAllEvents() {
        return Collections.unmodifiableCollection(ACTIVE_EVENTS.values());
    }

    public static SupernovaEvent getEventForStar(String starNodeId) {
        if (starNodeId == null) return null;
        return EVENTS_BY_STAR.get(starNodeId);
    }

    public static OverriddenStar getOverriddenStar(String starNodeId) {
        SupernovaEvent event = getEventForStar(starNodeId);
        if (event == null) return null;

        float baseSize = 7.0f;
        String name = event.remnantTitle();

        SpectralClass sc = (event.phase() == SupernovaPhase.REMNANT) ? event.remnantType().getSpectralClass() : event.progenitorSpectralClass();
        EssenceType ess = (event.phase() == SupernovaPhase.REMNANT) ? event.remnantEssence() : event.progenitorEssence();

        if (event.phase() == SupernovaPhase.PRECURSOR) {
            // Rapid throbbing size during precursor
            baseSize = 9.0f;
        } else if (event.phase() == SupernovaPhase.FLASH) {
            baseSize = 22.0f; // Blinding flash
        } else if (event.phase() == SupernovaPhase.EXPANDING_NEBULA) {
            baseSize = 14.0f;
        } else if (event.phase() == SupernovaPhase.REMNANT) {
            baseSize = switch (event.remnantType()) {
                case BLACK_HOLE -> 16.0f; // Accretion disc width
                case PULSAR -> 10.0f;
                case MAGNETAR -> 11.0f;
                case WHITE_DWARF -> 6.0f;
                case STRANGE_STAR -> 8.5f;
                case COLLAPSAR -> 13.0f;
            };
        }

        return new OverriddenStar(
                name,
                event.azimuthDeg(),
                event.altitudeDeg(),
                baseSize,
                sc,
                ess,
                event.remnantType(),
                event.phase(),
                event.progress(),
                event.bipolarAngleRad()
        );
    }

    public static void setClientEvents(List<SupernovaEvent> events) {
        ACTIVE_EVENTS.clear();
        EVENTS_BY_STAR.clear();
        for (SupernovaEvent e : events) {
            ACTIVE_EVENTS.put(e.eventId(), e);
            EVENTS_BY_STAR.put(e.starNodeId(), e);
        }
    }

    public static void syncToPlayer(ServerPlayer player) {
        List<SupernovaEvent> list = new ArrayList<>(ACTIVE_EVENTS.values());
        NetworkManager.sendToPlayer(player, new SyncSupernovaPayload(list));
    }

    public static void syncToAll(ServerLevel level) {
        List<SupernovaEvent> list = new ArrayList<>(ACTIVE_EVENTS.values());
        for (ServerPlayer sp : level.players()) {
            NetworkManager.sendToPlayer(sp, new SyncSupernovaPayload(list));
        }
    }

    public static boolean triggerSupernova(ServerLevel level, String starNodeId, StellarRemnantType preferredRemnant) {
        if (starNodeId == null || starNodeId.isEmpty()) return false;
        if (EVENTS_BY_STAR.containsKey(starNodeId)) return false;

        // Progenitor lookup
        float azim = 0.0f;
        float alt = 45.0f;
        SpectralClass progSc = SpectralClass.CLASS_A;
        EssenceType progEss = EssenceType.ASTRAL;
        String baseName = "Celestial Star";

        if (starNodeId.startsWith("a:")) {
            try {
                int idx = Integer.parseInt(starNodeId.substring(2));
                if (idx >= 0 && idx < CelestialStarHelper.AMBIENT_STARS.size()) {
                    CelestialStarHelper.AmbientStar as = CelestialStarHelper.AMBIENT_STARS.get(idx);
                    azim = as.azimuth();
                    alt = as.altitude();
                    progSc = as.spectralClass();
                    progEss = as.essenceType();
                    baseName = as.name();
                }
            } catch (Exception e) {
                return false;
            }
        } else if (starNodeId.startsWith("l:")) {
            String name = starNodeId.substring(2);
            for (CelestialStarHelper.LandmarkStar ls : CelestialStarHelper.LANDMARK_STARS) {
                if (ls.name().equals(name)) {
                    azim = ls.azimuth();
                    alt = ls.altitude();
                    progSc = ls.spectralClass();
                    progEss = ls.essenceType();
                    baseName = ls.name();
                    break;
                }
            }
        } else {
            return false; // Cannot detonate constellation stars
        }

        Random rng = new Random(level.getSeed() ^ starNodeId.hashCode());
        float bAngle = rng.nextFloat() * (float) Math.PI;

        StellarRemnantType remnantType = (preferredRemnant != null) ? preferredRemnant : pickRandomRemnant(rng);
        EssenceType remnantEss = remnantType.pickEssence(rng.nextLong());
        String title = baseName + " (" + remnantType.getTitle() + ")";

        SupernovaEvent event = new SupernovaEvent(
                UUID.randomUUID(),
                starNodeId,
                azim,
                alt,
                bAngle,
                SupernovaPhase.PRECURSOR,
                level.getGameTime(),
                DURATION_PRECURSOR,
                0.0f,
                progSc,
                progEss,
                remnantType,
                remnantEss,
                title
        );

        ACTIVE_EVENTS.put(event.eventId(), event);
        EVENTS_BY_STAR.put(event.starNodeId(), event);

        SupernovaSavedData data = SupernovaSavedData.get(level);
        data.setEvents(new ArrayList<>(ACTIVE_EVENTS.values()));

        syncToAll(level);
        return true;
    }

    public static void clearAllEvents(ServerLevel level) {
        ACTIVE_EVENTS.clear();
        EVENTS_BY_STAR.clear();
        SupernovaSavedData data = SupernovaSavedData.get(level);
        data.setEvents(new ArrayList<>());
        syncToAll(level);
    }

    public static void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        boolean changed = false;

        for (Map.Entry<UUID, SupernovaEvent> entry : ACTIVE_EVENTS.entrySet()) {
            SupernovaEvent ev = entry.getValue();
            long elapsed = gameTime - ev.startTick();

            if (ev.phase() == SupernovaPhase.PRECURSOR) {
                float prog = Math.min(1.0f, (float) elapsed / ev.durationTicks());
                if (elapsed >= ev.durationTicks()) {
                    ev = new SupernovaEvent(ev.eventId(), ev.starNodeId(), ev.azimuthDeg(), ev.altitudeDeg(), ev.bipolarAngleRad(),
                            SupernovaPhase.FLASH, gameTime, DURATION_FLASH, 0.0f,
                            ev.progenitorSpectralClass(), ev.progenitorEssence(), ev.remnantType(), ev.remnantEssence(), ev.remnantTitle());
                    ACTIVE_EVENTS.put(ev.eventId(), ev);
                    EVENTS_BY_STAR.put(ev.starNodeId(), ev);
                    changed = true;
                } else if (Math.abs(prog - ev.progress()) > 0.02f) {
                    ev = new SupernovaEvent(ev.eventId(), ev.starNodeId(), ev.azimuthDeg(), ev.altitudeDeg(), ev.bipolarAngleRad(),
                            ev.phase(), ev.startTick(), ev.durationTicks(), prog,
                            ev.progenitorSpectralClass(), ev.progenitorEssence(), ev.remnantType(), ev.remnantEssence(), ev.remnantTitle());
                    ACTIVE_EVENTS.put(ev.eventId(), ev);
                    EVENTS_BY_STAR.put(ev.starNodeId(), ev);
                }
            } else if (ev.phase() == SupernovaPhase.FLASH) {
                float prog = Math.min(1.0f, (float) elapsed / ev.durationTicks());
                if (elapsed >= ev.durationTicks()) {
                    ev = new SupernovaEvent(ev.eventId(), ev.starNodeId(), ev.azimuthDeg(), ev.altitudeDeg(), ev.bipolarAngleRad(),
                            SupernovaPhase.EXPANDING_NEBULA, gameTime, DURATION_EXPANSION, 0.0f,
                            ev.progenitorSpectralClass(), ev.progenitorEssence(), ev.remnantType(), ev.remnantEssence(), ev.remnantTitle());
                    ACTIVE_EVENTS.put(ev.eventId(), ev);
                    EVENTS_BY_STAR.put(ev.starNodeId(), ev);
                    changed = true;
                }
            } else if (ev.phase() == SupernovaPhase.EXPANDING_NEBULA) {
                float prog = Math.min(1.0f, (float) elapsed / ev.durationTicks());
                if (elapsed >= ev.durationTicks()) {
                    ev = new SupernovaEvent(ev.eventId(), ev.starNodeId(), ev.azimuthDeg(), ev.altitudeDeg(), ev.bipolarAngleRad(),
                            SupernovaPhase.REMNANT, gameTime, 1, 1.0f,
                            ev.progenitorSpectralClass(), ev.progenitorEssence(), ev.remnantType(), ev.remnantEssence(), ev.remnantTitle());
                    ACTIVE_EVENTS.put(ev.eventId(), ev);
                    EVENTS_BY_STAR.put(ev.starNodeId(), ev);
                    changed = true;
                } else if (Math.abs(prog - ev.progress()) > 0.01f) {
                    ev = new SupernovaEvent(ev.eventId(), ev.starNodeId(), ev.azimuthDeg(), ev.altitudeDeg(), ev.bipolarAngleRad(),
                            ev.phase(), ev.startTick(), ev.durationTicks(), prog,
                            ev.progenitorSpectralClass(), ev.progenitorEssence(), ev.remnantType(), ev.remnantEssence(), ev.remnantTitle());
                    ACTIVE_EVENTS.put(ev.eventId(), ev);
                    EVENTS_BY_STAR.put(ev.starNodeId(), ev);
                }
            }
        }

        // Extremely rare natural cosmic cycle roll (Every 24,000 ticks = 1 in-game day, ~0.8% chance if < 3 active events)
        if (gameTime % 24000 == 0 && ACTIVE_EVENTS.size() < 3) {
            Random rng = new Random(gameTime ^ level.getSeed());
            if (rng.nextFloat() < 0.008f && !CelestialStarHelper.AMBIENT_STARS.isEmpty()) {
                int randomStarIdx = rng.nextInt(CelestialStarHelper.AMBIENT_STARS.size());
                String nodeId = "a:" + randomStarIdx;
                if (!EVENTS_BY_STAR.containsKey(nodeId)) {
                    triggerSupernova(level, nodeId, null);
                }
            }
        }

        if (changed) {
            SupernovaSavedData data = SupernovaSavedData.get(level);
            data.setEvents(new ArrayList<>(ACTIVE_EVENTS.values()));
            syncToAll(level);
        }
    }

    private static StellarRemnantType pickRandomRemnant(Random rng) {
        StellarRemnantType[] types = StellarRemnantType.values();
        return types[rng.nextInt(types.length)];
    }

    /**
     * Procedurally generates the Figure-8 (Hourglass / Bipolar Lobes) nebula cloud puffs
     * centered on the progenitor position, rotated along the bipolar axis angle.
     */
    public static List<FigureEightPuff> getFigureEightPuffs(SupernovaEvent event) {
        List<FigureEightPuff> puffs = new ArrayList<>();
        float progress = (event.phase() == SupernovaPhase.REMNANT) ? 1.0f : event.progress();
        float expansionScale = (event.phase() == SupernovaPhase.FLASH) ? 0.3f : (0.4f + 0.6f * progress);

        float tilt = event.bipolarAngleRad();
        float cosT = Mth.cos(tilt);
        float sinT = Mth.sin(tilt);

        EssenceType ess = event.remnantEssence();
        float er = ess.getR() / 255.0f;
        float eg = ess.getG() / 255.0f;
        float eb = ess.getB() / 255.0f;

        // 1. Dual Bipolar Lobes (North Lobe: +U, South Lobe: -U)
        int puffsPerLobe = 16;
        for (int lobe = -1; lobe <= 1; lobe += 2) {
            for (int p = 0; p < puffsPerLobe; p++) {
                float dist = (1.5f + (p / (float) puffsPerLobe) * 4.5f) * expansionScale;
                float spread = (1.2f + (p / (float) puffsPerLobe) * 2.8f) * expansionScale;
                float sideSign = (p % 2 == 0) ? 1.0f : -1.0f;

                float u = lobe * dist;
                float v = sideSign * spread * 0.75f;

                // Rotate by bipolar tilt angle
                float dAzim = (u * cosT - v * sinT) * 1.3f;
                float dAlt = (u * sinT + v * cosT) * 1.3f;

                float size = (3.5f + (p / (float) puffsPerLobe) * 5.0f) * expansionScale;
                float phase = p * 0.4f + lobe * 1.5f;

                // Outer ionized hydrogen red / inner oxygen-synchrotron cyan-violet gradient
                float r = Mth.lerp(p / (float) puffsPerLobe, er * 0.8f, 0.95f);
                float g = Mth.lerp(p / (float) puffsPerLobe, eg, 0.35f);
                float b = Mth.lerp(p / (float) puffsPerLobe, eb * 1.2f, 0.65f);
                float a = (event.phase() == SupernovaPhase.FLASH) ? 0.95f : 0.70f;

                puffs.add(new FigureEightPuff(dAzim, dAlt, size, r, g, b, a, 0.05f, phase));
            }
        }

        // 2. Equatorial Shockwave Pinch Ring (The center of the Figure-8)
        int ringPuffs = 10;
        for (int rIdx = 0; rIdx < ringPuffs; rIdx++) {
            float angle = (rIdx / (float) ringPuffs) * (float) Math.PI * 2.0f;
            float rRad = 2.0f * expansionScale;
            float u = Mth.sin(angle) * (rRad * 0.4f);
            float v = Mth.cos(angle) * (rRad * 1.6f);

            float dAzim = (u * cosT - v * sinT) * 1.3f;
            float dAlt = (u * sinT + v * cosT) * 1.3f;

            float size = 4.0f * expansionScale;
            puffs.add(new FigureEightPuff(dAzim, dAlt, size, 1.0f, 0.85f, 0.4f, 0.85f, 0.08f, rIdx * 0.6f));
        }

        return puffs;
    }
}
