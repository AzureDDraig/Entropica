package ddraig.net.entropica.spell.vfx;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.network.SyncSpellWardPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Environment-safe state manager for persistent entity-attached 3D Ward & Aegis Barrier Shells.
 * Server methods broadcast {@link SyncSpellWardPayload} on activation, periodic refresh, hit absorption,
 * and shatter; client methods maintain smooth deploy/fade, shield-ratio interpolation, directional
 * impact shockwave ripples, and hit-flare opacity surges read by {@code SpellWorldVfxRenderer}.
 */
public final class SpellWardClientManager {

    private static final double BROADCAST_RANGE_SQ = 112.0D * 112.0D;
    private static final int DEPLOY_TICKS = 10;
    private static final int FADE_OUT_TICKS = 22;
    private static final int SHATTER_TICKS = 18;
    private static final int RIPPLE_DURATION_TICKS = 26;

    public static final class WardInstance {
        public final int entityId;
        public volatile EssenceType primaryMateria;
        @Nullable
        public volatile EssenceType secondaryMateria;
        public volatile int potencyTier;
        public volatile int remainingTicks;

        public float prevShieldRatio;
        public float shieldRatio;

        public float prevFlare;
        public float flare;

        public volatile float hitDirX;
        public volatile float hitDirY;
        public volatile float hitDirZ;
        public int prevRippleAge;
        public int rippleAge;

        public int prevActiveTicks;
        public int activeTicks;
        public int fadeOutTicks;
        public boolean expiring;

        public boolean shattering;
        public int prevShatterTicks;
        public int shatterTicks;

        public WardInstance(
                int entityId,
                EssenceType primaryMateria,
                @Nullable EssenceType secondaryMateria,
                int potencyTier,
                int remainingTicks,
                float shieldRatio,
                float initialFlare,
                float hitDirX,
                float hitDirY,
                float hitDirZ
        ) {
            this.entityId = entityId;
            this.primaryMateria = primaryMateria;
            this.secondaryMateria = secondaryMateria;
            this.potencyTier = Math.clamp(potencyTier, 1, 10);
            this.remainingTicks = Math.max(1, remainingTicks);
            float clampedRatio = Mth.clamp(shieldRatio, 0.05F, 1.0F);
            this.prevShieldRatio = clampedRatio;
            this.shieldRatio = clampedRatio;
            float clampedFlare = Mth.clamp(initialFlare, 0.0F, 1.35F);
            this.prevFlare = clampedFlare;
            this.flare = clampedFlare;
            this.hitDirX = hitDirX;
            this.hitDirY = hitDirY;
            this.hitDirZ = hitDirZ;
            boolean hasDir = (hitDirX * hitDirX + hitDirY * hitDirY + hitDirZ * hitDirZ) > 1.0E-4F;
            this.prevRippleAge = hasDir ? 0 : RIPPLE_DURATION_TICKS;
            this.rippleAge = hasDir ? 0 : RIPPLE_DURATION_TICKS;
            this.prevActiveTicks = 0;
            this.activeTicks = 1;
            this.fadeOutTicks = 0;
            this.expiring = false;
            this.shattering = false;
            this.prevShatterTicks = 0;
            this.shatterTicks = 0;
        }

        public float getEnvelope(float partialTick) {
            if (this.shattering) {
                float sProg = getShatterProgress(partialTick);
                return Mth.clamp(1.0F - sProg * sProg, 0.0F, 1.0F);
            }
            if (this.expiring) {
                float prog = (this.fadeOutTicks + partialTick) / (float) FADE_OUT_TICKS;
                return Mth.clamp(1.0F - prog, 0.0F, 1.0F);
            }
            float open = (this.prevActiveTicks + (this.activeTicks - this.prevActiveTicks) * partialTick) / (float) DEPLOY_TICKS;
            if (this.remainingTicks <= FADE_OUT_TICKS) {
                float rem = Math.max(0.0F, this.remainingTicks - partialTick) / (float) FADE_OUT_TICKS;
                return Math.min(Mth.clamp(open, 0.0F, 1.0F), Mth.clamp(rem, 0.0F, 1.0F));
            }
            return Mth.clamp(open, 0.0F, 1.0F);
        }

        public float getFlare(float partialTick) {
            if (this.shattering) {
                float sProg = getShatterProgress(partialTick);
                return Mth.clamp(1.35F * (1.0F - sProg * 0.65F), 0.0F, 1.35F);
            }
            return Mth.clamp(Mth.lerp(partialTick, this.prevFlare, this.flare), 0.0F, 1.35F);
        }

        public float getShieldRatio(float partialTick) {
            return Mth.clamp(Mth.lerp(partialTick, this.prevShieldRatio, this.shieldRatio), 0.05F, 1.0F);
        }

        public boolean isShattering() {
            return this.shattering;
        }

        public float getShatterProgress(float partialTick) {
            float raw = Mth.lerp(partialTick, this.prevShatterTicks, this.shatterTicks) / (float) SHATTER_TICKS;
            return Mth.clamp(raw, 0.0F, 1.0F);
        }

        public boolean hasHitDirection() {
            return (this.hitDirX * this.hitDirX + this.hitDirY * this.hitDirY + this.hitDirZ * this.hitDirZ) > 1.0E-4F;
        }

        public boolean hasActiveRipple(float partialTick) {
            return hasHitDirection() && (this.prevRippleAge + partialTick) < RIPPLE_DURATION_TICKS;
        }

        public float getRippleProgress(float partialTick) {
            float raw = Mth.lerp(partialTick, this.prevRippleAge, this.rippleAge) / (float) RIPPLE_DURATION_TICKS;
            return Mth.clamp(raw, 0.0F, 1.0F);
        }
    }

    private static final Map<Integer, WardInstance> CLIENT_WARDS = new ConcurrentHashMap<>();

    private SpellWardClientManager() {
    }

    // =========================================================================
    // CLIENT SYNC & TICKING
    // =========================================================================

    public static void handleSync(SyncSpellWardPayload payload) {
        EssenceType primary = SpellParticleHelper.fromOrdinal(payload.primaryOrdinal());
        EssenceType secondary = payload.secondaryOrdinal() >= 0
                ? SpellParticleHelper.fromOrdinal(payload.secondaryOrdinal())
                : null;
        int tier = Math.clamp(payload.potencyTier(), 1, 10);

        if (payload.remainingTicks() < 0) {
            // Barrier shattered! Trigger rapid high-opacity outward shatter burst!
            WardInstance existing = CLIENT_WARDS.get(payload.entityId());
            if (existing != null) {
                existing.primaryMateria = primary;
                existing.secondaryMateria = secondary;
                existing.shattering = true;
                existing.prevShatterTicks = 0;
                existing.shatterTicks = 0;
                existing.prevFlare = 1.35F;
                existing.flare = 1.35F;
                existing.remainingTicks = 0;
                if ((payload.hitDirX() * payload.hitDirX() + payload.hitDirY() * payload.hitDirY() + payload.hitDirZ() * payload.hitDirZ()) > 1.0E-4F) {
                    existing.hitDirX = payload.hitDirX();
                    existing.hitDirY = payload.hitDirY();
                    existing.hitDirZ = payload.hitDirZ();
                    existing.prevRippleAge = 0;
                    existing.rippleAge = 0;
                }
            } else {
                WardInstance shatterInst = new WardInstance(
                        payload.entityId(), primary, secondary, tier, 1, 0.1F, 1.35F,
                        payload.hitDirX(), payload.hitDirY(), payload.hitDirZ()
                );
                shatterInst.prevActiveTicks = DEPLOY_TICKS;
                shatterInst.activeTicks = DEPLOY_TICKS;
                shatterInst.shattering = true;
                CLIENT_WARDS.put(payload.entityId(), shatterInst);
            }
            return;
        }

        if (payload.remainingTicks() == 0) {
            WardInstance existing = CLIENT_WARDS.get(payload.entityId());
            if (existing != null && !existing.shattering) {
                existing.expiring = true;
                existing.remainingTicks = 0;
            }
            return;
        }

        WardInstance existing = CLIENT_WARDS.get(payload.entityId());
        if (existing != null) {
            existing.primaryMateria = primary;
            existing.secondaryMateria = secondary;
            existing.potencyTier = tier;
            existing.remainingTicks = payload.remainingTicks();
            existing.shieldRatio = Mth.clamp(payload.shieldRatio(), 0.05F, 1.0F);
            existing.expiring = false;
            existing.shattering = false;
            existing.fadeOutTicks = 0;
            existing.shatterTicks = 0;

            if (payload.hitFlare() > 0.01F) {
                float boosted = Math.max(existing.flare * 0.65F, payload.hitFlare());
                existing.flare = Mth.clamp(boosted, 0.25F, 1.35F);
                existing.prevFlare = Math.max(existing.prevFlare, existing.flare * 0.85F);
                float dirLenSq = payload.hitDirX() * payload.hitDirX()
                        + payload.hitDirY() * payload.hitDirY()
                        + payload.hitDirZ() * payload.hitDirZ();
                if (dirLenSq > 1.0E-4F) {
                    existing.hitDirX = payload.hitDirX();
                    existing.hitDirY = payload.hitDirY();
                    existing.hitDirZ = payload.hitDirZ();
                    existing.prevRippleAge = 0;
                    existing.rippleAge = 0;
                }
            }
        } else {
            CLIENT_WARDS.put(payload.entityId(), new WardInstance(
                    payload.entityId(),
                    primary,
                    secondary,
                    tier,
                    payload.remainingTicks(),
                    payload.shieldRatio(),
                    payload.hitFlare() > 0.01F ? payload.hitFlare() : 0.45F,
                    payload.hitDirX(),
                    payload.hitDirY(),
                    payload.hitDirZ()
            ));
        }
    }

    public static void tickClient(boolean hasActiveLevel) {
        if (!hasActiveLevel) {
            CLIENT_WARDS.clear();
            return;
        }
        Iterator<Map.Entry<Integer, WardInstance>> it = CLIENT_WARDS.entrySet().iterator();
        while (it.hasNext()) {
            WardInstance inst = it.next().getValue();
            inst.prevActiveTicks = inst.activeTicks;
            inst.activeTicks++;

            inst.prevShieldRatio = inst.shieldRatio;
            inst.prevFlare = inst.flare;
            inst.flare = Math.max(0.0F, inst.flare * 0.88F - 0.015F);

            inst.prevRippleAge = inst.rippleAge;
            if (inst.rippleAge < RIPPLE_DURATION_TICKS) {
                inst.rippleAge++;
            }

            if (inst.shattering) {
                inst.prevShatterTicks = inst.shatterTicks;
                inst.shatterTicks++;
                if (inst.shatterTicks >= SHATTER_TICKS) {
                    it.remove();
                }
                continue;
            }

            if (inst.expiring) {
                inst.fadeOutTicks++;
                if (inst.fadeOutTicks >= FADE_OUT_TICKS) {
                    it.remove();
                }
            } else {
                inst.remainingTicks--;
                if (inst.remainingTicks <= 0) {
                    inst.expiring = true;
                    inst.fadeOutTicks = 0;
                }
            }
        }
    }

    public static List<WardInstance> snapshotActiveWards() {
        if (CLIENT_WARDS.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(CLIENT_WARDS.values());
    }

    public static void clearClientCache() {
        CLIENT_WARDS.clear();
    }

    // =========================================================================
    // SERVER BROADCAST HELPERS
    // =========================================================================

    public static void broadcastWardState(
            ServerLevel level,
            LivingEntity entity,
            int remainingTicks,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int potencyTier,
            float shieldRatio,
            float hitFlare,
            @Nullable Vec3 hitOrigin
    ) {
        EssenceType pMat = primary != null ? primary : EssenceType.AEGIS;
        int secOrd = (secondary != null && secondary != pMat) ? secondary.ordinal() : -1;

        float dx = 0.0F;
        float dy = 0.0F;
        float dz = 0.0F;
        if (hitOrigin != null) {
            Vec3 center = entity.position().add(0.0D, entity.getBbHeight() * 0.52D, 0.0D);
            Vec3 diff = hitOrigin.subtract(center);
            double len = diff.length();
            if (len > 1.0E-3D) {
                dx = (float) (diff.x / len);
                dy = (float) (diff.y / len);
                dz = (float) (diff.z / len);
            }
        }

        SyncSpellWardPayload payload = new SyncSpellWardPayload(
                entity.getId(),
                remainingTicks,
                pMat.ordinal(),
                secOrd,
                Math.clamp(potencyTier, 1, 10),
                Mth.clamp(shieldRatio, 0.05F, 1.0F),
                Mth.clamp(hitFlare, 0.0F, 1.35F),
                dx, dy, dz
        );

        for (ServerPlayer player : level.players()) {
            if (player == entity || player.distanceToSqr(entity) <= BROADCAST_RANGE_SQ) {
                NetworkManager.sendToPlayer(player, payload);
            }
        }
    }

    public static void broadcastWardShatter(
            ServerLevel level,
            LivingEntity entity,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int potencyTier,
            @Nullable Vec3 hitOrigin
    ) {
        broadcastWardState(level, entity, -1, primary, secondary, potencyTier, 0.05F, 1.35F, hitOrigin);
    }
}
