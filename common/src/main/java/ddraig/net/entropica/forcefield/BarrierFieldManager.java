package ddraig.net.entropica.forcefield;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spatial manager for active forcefield barrier entities across loaded levels.
 * Provides rapid swept-path queries, continuous collision evaluation, and debug inspection.
 */
public class BarrierFieldManager {

    private static final Map<UUID, ForcefieldBarrierEntity> ACTIVE_BARRIERS = new ConcurrentHashMap<>();

    public static void registerBarrier(ForcefieldBarrierEntity barrier) {
        if (barrier != null && barrier.isAlive()) {
            ACTIVE_BARRIERS.put(barrier.getUUID(), barrier);
        }
    }

    public static void unregisterBarrier(ForcefieldBarrierEntity barrier) {
        if (barrier != null) {
            ACTIVE_BARRIERS.remove(barrier.getUUID());
        }
    }

    public static Collection<ForcefieldBarrierEntity> getAllBarriers() {
        return ACTIVE_BARRIERS.values();
    }

    public static List<ForcefieldBarrierEntity> getNearbyBarriers(Level level, Vec3 pos, double maxRadius) {
        List<ForcefieldBarrierEntity> list = new ArrayList<>();
        double rSqr = maxRadius * maxRadius;

        for (ForcefieldBarrierEntity barrier : ACTIVE_BARRIERS.values()) {
            if (barrier.isAlive() && barrier.level() == level) {
                if (barrier.position().distanceToSqr(pos) <= rSqr) {
                    list.add(barrier);
                }
            }
        }
        return list;
    }

    /**
     * Continuous collision resolution called during entity movement.
     * Checks if the swept segment [startPos -> endPos] crosses any active barrier in the level.
     *
     * @return true if an impenetrable barrier was struck and bounced the entity
     */
    public static boolean checkMovementCollisions(Entity entity, Vec3 startPos, Vec3 endPos) {
        if (entity == null || !entity.isAlive() || ACTIVE_BARRIERS.isEmpty()) {
            return false;
        }

        Level level = entity.level();
        AABB sweptBox = new AABB(startPos, endPos).inflate(1.5);

        for (ForcefieldBarrierEntity barrier : ACTIVE_BARRIERS.values()) {
            if (!barrier.isAlive() || barrier.level() != level || barrier == entity) {
                continue;
            }

            if (barrier.getBoundingBox().intersects(sweptBox)) {
                boolean collided = barrier.handleEntityCollision(entity, startPos, endPos);
                if (collided) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clears/dispels barriers in the level within the given radius (or all if radius <= 0).
     */
    public static int clearBarriers(Level level, Vec3 center, double radius) {
        int count = 0;
        double rSqr = radius * radius;

        Iterator<ForcefieldBarrierEntity> it = ACTIVE_BARRIERS.values().iterator();
        while (it.hasNext()) {
            ForcefieldBarrierEntity b = it.next();
            if (b.level() == level) {
                if (radius <= 0 || b.position().distanceToSqr(center) <= rSqr) {
                    b.dissolveInGlitter();
                    it.remove();
                    count++;
                }
            }
        }
        return count;
    }
}
