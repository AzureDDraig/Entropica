package ddraig.net.entropica.spell.vfx;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.network.SyncArcaneWingsPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Environment-safe state manager for Materia-colored 3D Arcane Wings.
 * Server methods broadcast {@link SyncArcaneWingsPayload} to tracking players (and self);
 * client methods maintain smooth deploy/fold animation state read by {@code ArcaneWingsRenderLayer}.
 */
public final class ArcaneWingsClientManager {

    private static final double BROADCAST_RANGE_SQ = 112.0D * 112.0D;
    private static final int DEPLOY_TICKS = 8;
    private static final int FADE_OUT_TICKS = 14;

    public static final class WingInstance {
        public final int entityId;
        public volatile EssenceType primaryMateria;
        @Nullable
        public volatile EssenceType secondaryMateria;
        public volatile int potencyTier;
        public volatile int remainingTicks;
        public int prevActiveTicks;
        public int activeTicks;
        public int fadeOutTicks;
        public boolean expiring;

        public WingInstance(
                int entityId,
                EssenceType primaryMateria,
                @Nullable EssenceType secondaryMateria,
                int potencyTier,
                int remainingTicks
        ) {
            this.entityId = entityId;
            this.primaryMateria = primaryMateria;
            this.secondaryMateria = secondaryMateria;
            this.potencyTier = Math.clamp(potencyTier, 1, 10);
            this.remainingTicks = Math.max(1, remainingTicks);
            this.prevActiveTicks = 0;
            this.activeTicks = 1;
            this.fadeOutTicks = 0;
            this.expiring = false;
        }

        /**
         * Returns smooth 0.0 -> 1.0 unfurl/fold scale factor interpolated by {@code partialTick}.
         */
        public float getDeployProgress(float partialTick) {
            if (this.expiring) {
                float prog = (this.fadeOutTicks + partialTick) / (float) FADE_OUT_TICKS;
                return Math.clamp(1.0F - prog, 0.0F, 1.0F);
            }
            float open = (this.prevActiveTicks + (this.activeTicks - this.prevActiveTicks) * partialTick) / (float) DEPLOY_TICKS;
            if (this.remainingTicks <= FADE_OUT_TICKS) {
                float rem = Math.max(0.0F, this.remainingTicks - partialTick) / (float) FADE_OUT_TICKS;
                return Math.min(Math.clamp(open, 0.0F, 1.0F), Math.clamp(rem, 0.0F, 1.0F));
            }
            return Math.clamp(open, 0.0F, 1.0F);
        }
    }

    private static final Map<Integer, WingInstance> CLIENT_WINGS = new ConcurrentHashMap<>();

    private ArcaneWingsClientManager() {
    }

    // =========================================================================
    // CLIENT SYNC & TICKING
    // =========================================================================

    public static void handleSync(SyncArcaneWingsPayload payload) {
        if (payload.remainingTicks() <= 0) {
            WingInstance existing = CLIENT_WINGS.get(payload.entityId());
            if (existing != null) {
                existing.expiring = true;
                existing.remainingTicks = 0;
            }
            return;
        }

        EssenceType primary = SpellParticleHelper.fromOrdinal(payload.primaryOrdinal());
        EssenceType secondary = payload.secondaryOrdinal() >= 0
                ? SpellParticleHelper.fromOrdinal(payload.secondaryOrdinal())
                : null;
        int tier = Math.clamp(payload.potencyTier(), 1, 10);

        WingInstance existing = CLIENT_WINGS.get(payload.entityId());
        if (existing != null) {
            existing.primaryMateria = primary;
            existing.secondaryMateria = secondary;
            existing.potencyTier = tier;
            existing.remainingTicks = payload.remainingTicks();
            existing.expiring = false;
            existing.fadeOutTicks = 0;
        } else {
            CLIENT_WINGS.put(payload.entityId(), new WingInstance(
                    payload.entityId(), primary, secondary, tier, payload.remainingTicks()
            ));
        }
    }

    public static void tickClient(boolean hasActiveLevel) {
        if (!hasActiveLevel) {
            CLIENT_WINGS.clear();
            return;
        }
        Iterator<Map.Entry<Integer, WingInstance>> it = CLIENT_WINGS.entrySet().iterator();
        while (it.hasNext()) {
            WingInstance inst = it.next().getValue();
            inst.prevActiveTicks = inst.activeTicks;
            inst.activeTicks++;
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

    @Nullable
    public static WingInstance getActiveWingState(int entityId) {
        return CLIENT_WINGS.get(entityId);
    }

    public static void clearClientCache() {
        CLIENT_WINGS.clear();
    }

    // =========================================================================
    // SERVER BROADCAST HELPERS
    // =========================================================================

    public static void broadcastWings(
            ServerLevel level,
            LivingEntity entity,
            int remainingTicks,
            @Nullable EssenceType primary,
            @Nullable EssenceType secondary,
            int potencyTier
    ) {
        EssenceType pMat = primary != null ? primary : EssenceType.REGULAR;
        int secOrd = (secondary != null && secondary != pMat) ? secondary.ordinal() : -1;
        SyncArcaneWingsPayload payload = new SyncArcaneWingsPayload(
                entity.getId(),
                Math.max(0, remainingTicks),
                pMat.ordinal(),
                secOrd,
                Math.clamp(potencyTier, 1, 10)
        );
        for (ServerPlayer player : level.players()) {
            if (player == entity || player.distanceToSqr(entity) <= BROADCAST_RANGE_SQ) {
                NetworkManager.sendToPlayer(player, payload);
            }
        }
    }
}
