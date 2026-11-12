package ddraig.net.entropica.spell.vfx;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Environment-safe manager for active 3D world-space spell VFX events.
 * Server methods broadcast compact {@link SpellVfxEventPayload} packets to nearby players;
 * client methods maintain active timed 3D spell meshes rendered by {@code SpellWorldVfxRenderer}.
 */
public final class SpellWorldVfxManager {

    private static final int MAX_CLIENT_VFX_EVENTS = 256;
    private static final double BROADCAST_RANGE_SQ = 96.0D * 96.0D;

    public static final class ActiveVfxInstance {
        public final byte vfxType;
        public final Vec3 start;
        public final Vec3 endOrDir;
        public final int primaryRgb;
        public final int accentRgb;
        public final EssenceType essence;
        public final float radiusOrScale;
        public final int maxTicks;
        public final int tier;
        public final int seed;
        public int ageTicks;

        public ActiveVfxInstance(SpellVfxEventPayload payload, int seed) {
            this.vfxType = payload.vfxType();
            this.start = new Vec3(payload.sx(), payload.sy(), payload.sz());
            this.endOrDir = new Vec3(payload.ex(), payload.ey(), payload.ez());
            this.primaryRgb = payload.primaryRgb();
            this.accentRgb = payload.accentRgb();
            this.essence = SpellParticleHelper.fromOrdinal(payload.essenceOrdinal());
            this.radiusOrScale = Math.max(0.1F, payload.radiusOrScale());
            this.maxTicks = Math.max(2, payload.durationTicks());
            this.tier = Math.clamp(payload.tier(), 1, 10);
            this.seed = seed;
            this.ageTicks = 0;
        }
    }

    private static final List<ActiveVfxInstance> CLIENT_EVENTS = new ArrayList<>();
    private static int nextSeed = 1;

    private SpellWorldVfxManager() {
    }

    // =========================================================================
    // CLIENT STORAGE & TICKING
    // =========================================================================

    public static synchronized void enqueueClientEvent(SpellVfxEventPayload payload) {
        if (CLIENT_EVENTS.size() >= MAX_CLIENT_VFX_EVENTS) {
            CLIENT_EVENTS.removeFirst();
        }
        CLIENT_EVENTS.add(new ActiveVfxInstance(payload, nextSeed++));
    }

    public static synchronized void tickClient(boolean hasActiveLevel) {
        if (!hasActiveLevel) {
            CLIENT_EVENTS.clear();
            return;
        }
        Iterator<ActiveVfxInstance> it = CLIENT_EVENTS.iterator();
        while (it.hasNext()) {
            ActiveVfxInstance inst = it.next();
            inst.ageTicks++;
            if (inst.ageTicks >= inst.maxTicks) {
                it.remove();
            }
        }
    }

    public static synchronized List<ActiveVfxInstance> snapshotClientEvents() {
        if (CLIENT_EVENTS.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(CLIENT_EVENTS);
    }

    // =========================================================================
    // SERVER DISPATCH HELPERS
    // =========================================================================

    public static void broadcast(
            ServerLevel level,
            byte vfxType,
            Vec3 start,
            Vec3 endOrDir,
            @Nullable EssenceType primaryMateria,
            @Nullable EssenceType secondaryMateria,
            float radiusOrScale,
            int durationTicks,
            int tier
    ) {
        EssenceType pMat = primaryMateria != null ? primaryMateria : EssenceType.REGULAR;
        long gameTime = level.getGameTime();
        int pRgb = SpellParticleHelper.resolveVibrantPrimaryRgb(pMat, gameTime);
        int aRgb = SpellParticleHelper.resolveVibrantAccentRgb(pMat, secondaryMateria, gameTime);

        SpellVfxEventPayload payload = new SpellVfxEventPayload(
                vfxType,
                (float) start.x, (float) start.y, (float) start.z,
                (float) endOrDir.x, (float) endOrDir.y, (float) endOrDir.z,
                pRgb,
                aRgb,
                pMat.ordinal(),
                radiusOrScale,
                durationTicks,
                tier
        );

        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(start) <= BROADCAST_RANGE_SQ) {
                NetworkManager.sendToPlayer(player, payload);
            }
        }
    }

    public static void broadcastBeam(
            ServerLevel level,
            Vec3 start,
            Vec3 end,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        float beamRadius = 0.09F + Math.clamp(tier, 1, 10) * 0.015F;
        broadcast(level, SpellVfxEventPayload.VFX_BEAM, start, end, primary, secondary, beamRadius, 12, tier);
    }

    public static void broadcastChainArc(
            ServerLevel level,
            Vec3 anchorCenter,
            Vec3 targetCenter,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_CHAIN_ARC, anchorCenter, targetCenter, primary, secondary, 0.14F, 11, tier);
    }

    public static void broadcastRadialNova(
            ServerLevel level,
            Vec3 center,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_RADIAL_NOVA, center, new Vec3(0.0D, 1.0D, 0.0D),
                primary, secondary, (float) Math.max(1.2D, radius), 16, tier);
    }

    public static void broadcastProjectileImpact(
            ServerLevel level,
            Vec3 impactPos,
            Vec3 normalOrDir,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            float scale,
            int tier
    ) {
        Vec3 n = normalOrDir.lengthSqr() > 1.0E-4D ? normalOrDir.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
        broadcast(level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT, impactPos, n,
                primary, secondary, Math.max(0.65F, scale), 12, tier);
    }

    public static void broadcastArcaneSeal(
            ServerLevel level,
            Vec3 center,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int durationTicks,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_ARCANE_SEAL, center, new Vec3(0.0D, 1.0D, 0.0D),
                primary, secondary, (float) Math.max(1.0D, radius), Math.clamp(durationTicks, 18, 220), tier);
    }

    public static void broadcastCrescentSlash(
            ServerLevel level,
            Vec3 origin,
            Vec3 direction,
            double reach,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        broadcast(level, SpellVfxEventPayload.VFX_CRESCENT_SLASH, origin, dir,
                primary, secondary, (float) Math.max(1.6D, reach * 0.72D), 10, tier);
    }

    public static void broadcastVortexCone(
            ServerLevel level,
            Vec3 origin,
            Vec3 direction,
            double range,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        broadcast(level, SpellVfxEventPayload.VFX_VORTEX_CONE, origin, dir,
                primary, secondary, (float) Math.max(2.0D, range), 15, tier);
    }

    public static void broadcastSkyfallPillar(
            ServerLevel level,
            Vec3 impactGround,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        Vec3 skyTop = impactGround.add(0.0D, 18.0D, 0.0D);
        broadcast(level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR, impactGround, skyTop,
                primary, secondary, (float) Math.max(1.2D, radius), 22, tier);
    }

    public static void broadcastBlinkRift(
            ServerLevel level,
            Vec3 departurePos,
            Vec3 arrivalPos,
            @Nullable EssenceType primary,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_BLINK_RIFT, departurePos, arrivalPos,
                primary, null, 1.05F, 16, tier);
    }

    public static void broadcastWardShell(
            ServerLevel level,
            Vec3 center,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_WARD_SHELL, center, new Vec3(0.0D, 1.0D, 0.0D),
                primary, secondary, (float) Math.max(1.1D, radius), 22, tier);
    }

    public static void broadcastSummonPillar(
            ServerLevel level,
            Vec3 spawnPos,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_SUMMON_PILLAR, spawnPos, spawnPos.add(0.0D, 4.5D, 0.0D),
                primary, secondary, (float) Math.max(1.35D, radius), 26, tier);
    }
}
