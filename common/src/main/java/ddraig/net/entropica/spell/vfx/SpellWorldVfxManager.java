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

    private static final int MAX_CLIENT_VFX_EVENTS = 1536;
    private static final double BROADCAST_RANGE_SQ = 160.0D * 160.0D;

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
        SpellWardClientManager.tickClient(hasActiveLevel);
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

        boolean endIsWorldPos = vfxType == SpellVfxEventPayload.VFX_BEAM
                || vfxType == SpellVfxEventPayload.VFX_CHAIN_ARC
                || vfxType == SpellVfxEventPayload.VFX_BLINK_RIFT
                || vfxType == SpellVfxEventPayload.VFX_SKYFALL_PSEUDOBLOCK;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(start) <= BROADCAST_RANGE_SQ
                    || (endIsWorldPos && player.distanceToSqr(endOrDir) <= BROADCAST_RANGE_SQ)) {
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
        broadcast(level, SpellVfxEventPayload.VFX_BEAM, start, end, primary, secondary, beamRadius, 36, tier);
    }

    public static void broadcastChanneledBeamSlice(
            ServerLevel level,
            Vec3 start,
            Vec3 end,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier,
            int durationTicks
    ) {
        float beamRadius = 0.09F + Math.clamp(tier, 1, 10) * 0.015F;
        broadcast(level, SpellVfxEventPayload.VFX_BEAM, start, end, primary, secondary, beamRadius, Math.max(2, durationTicks), tier);
    }

    public static void broadcastChainArc(
            ServerLevel level,
            Vec3 anchorCenter,
            Vec3 targetCenter,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_CHAIN_ARC, anchorCenter, targetCenter, primary, secondary, 0.14F, 38, tier);
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
                primary, secondary, (float) Math.max(1.2D, radius), 52, tier);
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
                primary, secondary, Math.max(0.65F, scale), 38, tier);
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
                primary, secondary, (float) Math.max(1.0D, radius), Math.clamp(durationTicks, 44, 1200), tier);
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
                primary, secondary, (float) Math.max(1.6D, reach * 0.72D), 32, tier);
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
                primary, secondary, (float) Math.max(2.0D, range), 50, tier);
    }

    public static void broadcastSkyfallPillar(
            ServerLevel level,
            Vec3 impactGround,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        broadcastSkyfallPillar(level, impactGround, radius, primary, secondary, 80, tier);
    }

    public static void broadcastSkyfallPillar(
            ServerLevel level,
            Vec3 impactGround,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int durationTicks,
            int tier
    ) {
        double skyH = ddraig.net.entropica.spell.SpellDeliveryForm.probeCaveSafeSkyfallHeight(
                level, net.minecraft.core.BlockPos.containing(impactGround.x, impactGround.y + 0.2D, impactGround.z), tier
        );
        Vec3 skyTop = impactGround.add(0.0D, skyH, 0.0D);
        broadcast(level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR, impactGround, skyTop,
                primary, secondary, (float) Math.max(1.2D, radius), Math.clamp(durationTicks, 40, 800), tier);
    }

    /**
     * Broadcasts a falling &amp; standing 3D Skyfall Pseudoblock column ({@link SpellVfxEventPayload#VFX_SKYFALL_PSEUDOBLOCK}):
     * <ul>
     *     <li>{@code start} = overhead sky-rift drop origin ({@code x + 0.5, skyY, z + 0.5})</li>
     *     <li>{@code endOrDir} = exact world lower-corner {@code (targetBase.x, targetBase.y, targetBase.z)} where real blocks will materialize when the spell completes</li>
     *     <li>{@code primaryRgb} = {@code Block.getId(blockState)}</li>
     *     <li>{@code accentRgb} = {@code fallTicks} (number of ticks spent accelerating downward before locking into standing pseudoblock form)</li>
     *     <li>{@code radiusOrScale} = {@code columnHeight} (1..16 stacked blocks in this column)</li>
     *     <li>{@code durationTicks} = {@code fallTicks + holdTicks} (total lifetime until real world blocks replace the pseudoblocks!)</li>
     * </ul>
     */
    public static void broadcastSkyfallPseudoblockColumn(
            ServerLevel level,
            Vec3 skyOrigin,
            net.minecraft.core.BlockPos targetBase,
            net.minecraft.world.level.block.state.BlockState blockState,
            int columnHeight,
            int fallTicks,
            int holdTicks,
            @Nullable EssenceType materia,
            int tier
    ) {
        EssenceType pMat = materia != null ? materia : EssenceType.REGULAR;
        int stateId = net.minecraft.world.level.block.Block.getId(blockState);
        int clampedFall = Math.max(1, fallTicks);
        int totalTicks = Math.max(clampedFall + 1, clampedFall + Math.max(0, holdTicks));
        Vec3 targetCorner = new Vec3(targetBase.getX(), targetBase.getY(), targetBase.getZ());

        SpellVfxEventPayload payload = new SpellVfxEventPayload(
                SpellVfxEventPayload.VFX_SKYFALL_PSEUDOBLOCK,
                (float) skyOrigin.x, (float) skyOrigin.y, (float) skyOrigin.z,
                (float) targetCorner.x, (float) targetCorner.y, (float) targetCorner.z,
                stateId,
                clampedFall,
                pMat.ordinal(),
                (float) Math.clamp(columnHeight, 1, 20),
                totalTicks,
                tier
        );

        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(targetCorner) <= BROADCAST_RANGE_SQ
                    || player.distanceToSqr(skyOrigin) <= BROADCAST_RANGE_SQ) {
                NetworkManager.sendToPlayer(player, payload);
            }
        }
    }

    public static void broadcastBlinkRift(
            ServerLevel level,
            Vec3 departurePos,
            Vec3 arrivalPos,
            @Nullable EssenceType primary,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_BLINK_RIFT, departurePos, arrivalPos,
                primary, null, 1.05F, 46, tier);
    }

    public static void broadcastWardShell(
            ServerLevel level,
            Vec3 center,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int tier
    ) {
        broadcastWardShell(level, center, radius, primary, secondary, 58, tier);
    }

    public static void broadcastWardShell(
            ServerLevel level,
            Vec3 center,
            double radius,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int durationTicks,
            int tier
    ) {
        broadcast(level, SpellVfxEventPayload.VFX_WARD_SHELL, center, new Vec3(0.0D, 1.0D, 0.0D),
                primary, secondary, (float) Math.max(1.1D, radius), Math.clamp(durationTicks, 40, 800), tier);
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
                primary, secondary, (float) Math.max(1.35D, radius), 64, tier);
    }

    /**
     * Broadcasts a multi-second 3D swirling funnel tornado (e.g. Air #1 Tornado Vortex, Storm Tempest).
     */
    public static void broadcastTornadoVortex(
            ServerLevel level,
            Vec3 baseCenter,
            double radius,
            double height,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int durationTicks,
            int tier
    ) {
        Vec3 crown = baseCenter.add(0.0D, Math.max(4.5D, height), 0.0D);
        broadcast(level, SpellVfxEventPayload.VFX_TORNADO_VORTEX, baseCenter, crown,
                primary, secondary, (float) Math.max(1.8D, radius), Math.clamp(durationTicks, 60, 800), tier);
    }

    /**
     * Broadcasts an explosive vertical 3D geyser column (e.g. Air #1 14-block Geyser Mine & Concussive Uppercut).
     */
    public static void broadcastGeyserPlume(
            ServerLevel level,
            Vec3 baseCenter,
            double radius,
            double height,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int durationTicks,
            int tier
    ) {
        Vec3 top = baseCenter.add(0.0D, Math.max(2.5D, height), 0.0D);
        broadcast(level, SpellVfxEventPayload.VFX_GEYSER_PLUME, baseCenter, top,
                primary, secondary, (float) Math.max(0.8D, radius), Math.clamp(durationTicks, 40, 240), tier);
    }

    /**
     * Broadcasts a 3D spherical wind-wall or vacuum implosion shell ({@code inwardImplosion == true} collapses inward;
     * {@code false} expands outward as a protective spherical Gust Shield).
     */
    public static void broadcastVacuumSphere(
            ServerLevel level,
            Vec3 center,
            double radius,
            boolean inwardImplosion,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int durationTicks,
            int tier
    ) {
        Vec3 modeVec = new Vec3(inwardImplosion ? -1.0D : 1.0D, 0.0D, 0.0D);
        broadcast(level, SpellVfxEventPayload.VFX_VACUUM_SPHERE, center, modeVec,
                primary, secondary, (float) Math.max(1.2D, radius), Math.clamp(durationTicks, 36, 240), tier);
    }

    /**
     * Server-side obstacle helper for landed Skyfall Pseudoblock columns: gently pushes overlapping entities
     * outside the {@code 1x1xheight} column footprint so pseudoblocks function as physical barricades while
     * the Skyfall spell is running and prevent suffocation when the real world blocks materialize at completion!
     */
    public static void repelEntitiesFromPseudoblockColumn(
            ServerLevel level,
            net.minecraft.core.BlockPos basePos,
            int columnHeight,
            boolean teleportClearForSolidPlacement
    ) {
        int h = Math.max(1, columnHeight);
        net.minecraft.world.phys.AABB colBox = new net.minecraft.world.phys.AABB(
                basePos.getX() - 0.15D, basePos.getY(), basePos.getZ() - 0.15D,
                basePos.getX() + 1.15D, basePos.getY() + h + 0.15D, basePos.getZ() + 1.15D
        );
        double cx = basePos.getX() + 0.5D;
        double cz = basePos.getZ() + 0.5D;
        for (net.minecraft.world.entity.LivingEntity ent : level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class, colBox, net.minecraft.world.entity.LivingEntity::isAlive)) {
            double dx = ent.getX() - cx;
            double dz = ent.getZ() - cz;
            double dSq = dx * dx + dz * dz;
            Vec3 outDir = dSq > 1.0E-4D ? new Vec3(dx, 0.0D, dz).normalize() : new Vec3(1.0D, 0.0D, 0.0D);
            if (teleportClearForSolidPlacement) {
                ent.teleportTo(cx + outDir.x * 1.18D, ent.getY() + 0.06D, cz + outDir.z * 1.18D);
            } else {
                Vec3 curV = ent.getDeltaMovement();
                ent.setDeltaMovement(outDir.x * 0.36D, Math.max(curV.y, 0.04D), outDir.z * 0.36D);
                ent.hasImpulse = true;
            }
        }
    }

    /**
     * Computes the exact zone duration ticks after accounting for Amber's +50% Viscous Linger Flow extension,
     * keeping 3D Skyfall Pseudoblock {@code holdTicks} synchronized with {@code zone.maxDurationTicks}.
     */
    public static int resolveEffectiveZoneDurationTicks(
            @Nullable ddraig.net.entropica.spell.assembly.SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            int baseDurationTicks
    ) {
        if (spell != null
                && spell.flowPart().mutationEnabled()
                && spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria) == EssenceType.AMBER
                && baseDurationTicks < 1_000_000_000) {
            return Math.max(baseDurationTicks + 10, (int) Math.ceil(baseDurationTicks * 1.5D));
        }
        return baseDurationTicks;
    }

    /**
     * Resolves the first air/replaceable block directly atop solid ground near {@code nearPos},
     * ensuring Skyfall Pseudoblocks land cleanly on top of terrain rather than buried inside stone/dirt.
     */
    public static net.minecraft.core.BlockPos findSkyfallSurfaceBase(
            ServerLevel level,
            net.minecraft.core.BlockPos nearPos
    ) {
        net.minecraft.core.BlockPos cursor = nearPos;
        if (level.getBlockState(cursor).isSolidRender()) {
            for (int up = 1; up <= 8; up++) {
                net.minecraft.core.BlockPos above = nearPos.above(up);
                if (!level.getBlockState(above).isSolidRender()) {
                    return above;
                }
            }
            return nearPos.above();
        }
        for (int down = 0; down <= 8; down++) {
            net.minecraft.core.BlockPos candidate = nearPos.below(down);
            if (!level.getBlockState(candidate).isSolidRender()
                    && level.getBlockState(candidate.below()).isSolidRender()) {
                return candidate;
            }
        }
        return nearPos;
    }

    /**
     * Permanently places the exact {@code blockState} column ({@code basePos .. basePos.above(columnHeight - 1)})
     * represented by a 3D Skyfall Pseudoblock once the Skyfall spell finishes, safely pushing overlapping
     * entities clear and skipping protected blocks or containers.
     */
    public static void materializeSkyfallPseudoblockColumn(
            ServerLevel level,
            net.minecraft.core.BlockPos basePos,
            net.minecraft.world.level.block.state.BlockState blockState,
            int columnHeight
    ) {
        int h = Math.max(1, columnHeight);
        repelEntitiesFromPseudoblockColumn(level, basePos, h, true);
        boolean placedAny = false;
        for (int y = 0; y < h; y++) {
            net.minecraft.core.BlockPos p = basePos.above(y);
            net.minecraft.world.level.block.state.BlockState cur = level.getBlockState(p);
            if (ddraig.net.entropica.spell.engine.SpellExecutionEngine.isProtectedBlockOrContainer(level, p, cur)) {
                if (y > 0) {
                    break;
                }
                continue;
            }
            if (cur.isAir()
                    || cur.canBeReplaced()
                    || cur.is(net.minecraft.world.level.block.Blocks.WATER)
                    || cur.is(net.minecraft.world.level.block.Blocks.LAVA)
                    || cur.is(net.minecraft.world.level.block.Blocks.SNOW)
                    || cur.is(net.minecraft.world.level.block.Blocks.MOSS_CARPET)
                    || cur.is(net.minecraft.world.level.block.Blocks.FERN)
                    || cur.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS)
                    || cur.is(net.minecraft.world.level.block.Blocks.TALL_GRASS)
                    || cur.is(net.minecraft.world.level.block.Blocks.DEAD_BUSH)
                    || cur.is(net.minecraft.world.level.block.Blocks.AZALEA)
                    || cur.is(net.minecraft.world.level.block.Blocks.FLOWERING_AZALEA)
                    || cur.is(net.minecraft.world.level.block.Blocks.ICE)
                    || cur.is(net.minecraft.world.level.block.Blocks.PACKED_ICE)
                    || cur.is(net.minecraft.world.level.block.Blocks.FROSTED_ICE)) {
                level.setBlockAndUpdate(p, blockState);
                placedAny = true;
            } else if (y > 0) {
                break;
            }
        }
        if (placedAny) {
            net.minecraft.world.level.block.SoundType soundType = blockState.getSoundType();
            level.playSound(
                    null,
                    basePos,
                    soundType.getPlaceSound(),
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) * 0.45F,
                    soundType.getPitch() * 0.90F
            );
        }
    }
}
