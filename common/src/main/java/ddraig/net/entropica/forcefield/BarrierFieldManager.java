package ddraig.net.entropica.forcefield;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spatial manager for active forcefield barrier entities across loaded levels.
 * Provides rapid swept-path queries, continuous collision evaluation, earliest-t resolution, and boss death hooks.
 */
public class BarrierFieldManager {

    private static final Map<UUID, ForcefieldBarrierEntity> ACTIVE_BARRIERS = new ConcurrentHashMap<>();

    private record BarrierHitCandidate(ForcefieldBarrierEntity barrier, BarrierRaycastHit hit) {}

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
     * Evaluates swept trajectory [startPos -> endPos] against all candidate barriers,
     * sorts intersecting candidates by ascending swept parameter t in [0, 1],
     * and executes elastic reflection and safe displacement on the single earliest barrier hit.
     *
     * @return true if an impenetrable barrier was struck and bounced the entity
     */
    public static boolean checkMovementCollisions(Entity entity, Vec3 startPos, Vec3 endPos) {
        if (entity == null || !entity.isAlive() || ACTIVE_BARRIERS.isEmpty()) {
            return false;
        }

        Level level = entity.level();
        double entityRadius = Math.max(0.15, entity.getBbWidth() * 0.5);
        AABB entityBox = entity.getBoundingBox();
        Vec3 moveDelta = endPos.subtract(startPos);
        AABB sweptBox = entityBox.minmax(entityBox.move(moveDelta)).inflate(1.0);

        Vec3 approachDir = moveDelta;
        if (approachDir.lengthSqr() < 1e-6) {
            approachDir = entity.getDeltaMovement();
        }

        double entityHeight = Math.max(0.1, (double) entity.getBbHeight());
        Vec3[] sampleStarts = new Vec3[]{
                startPos, // feet
                startPos.add(0, entityHeight * 0.5, 0), // waist / center of mass
                startPos.add(0, Math.max(0.1, entityHeight - 0.1), 0) // head / eyes
        };

        List<BarrierHitCandidate> candidates = new ArrayList<>();

        Iterator<Map.Entry<UUID, ForcefieldBarrierEntity>> it = ACTIVE_BARRIERS.entrySet().iterator();
        while (it.hasNext()) {
            ForcefieldBarrierEntity barrier = it.next().getValue();
            if (barrier == null || barrier.isRemoved() || !barrier.isAlive()) {
                it.remove();
                continue;
            }
            if (barrier.level() != level || barrier == entity || !barrier.isActive()) {
                continue;
            }

            UUID owner = barrier.getOwnerUUID().orElse(null);
            if (!barrier.getFilterMode().isBlocked(entity, owner, barrier.getWhitelist())) {
                continue; // Permitted entity, passes freely
            }

            AABB bBox = barrier.getBoundingBox();
            if (bBox.getXsize() <= 1.05 && bBox.getYsize() <= 1.05 && bBox.getZsize() <= 1.05) {
                barrier.updateBoundingBox();
                bBox = barrier.getBoundingBox();
            }

            if (bBox.intersects(sweptBox)) {
                BarrierShapeHandler handler = BarrierShapeRegistry.get(barrier.getShape().ordinal());
                BarrierRaycastHit bestHitForBarrier = null;

                for (Vec3 sStart : sampleStarts) {
                    Vec3 sEnd = sStart.add(moveDelta);
                    BarrierRaycastHit hit;
                    if (handler != null) {
                        hit = handler.intersect(
                                barrier.position(), barrier.getYRot(), barrier.getXRot(),
                                barrier.getWidth(), barrier.getHeight(), barrier.getRadius(),
                                sStart, sEnd, entityRadius, barrier
                        );
                    } else {
                        hit = BarrierGeometry.intersect(
                                barrier.getShape(), barrier.position(), barrier.getYRot(), barrier.getXRot(),
                                barrier.getWidth(), barrier.getHeight(), barrier.getRadius(),
                                sStart, sEnd, entityRadius
                        );
                    }

                    if (hit.hit() && hit.t() >= 0.0 && hit.t() <= 1.0) {
                        if (bestHitForBarrier == null || hit.t() < bestHitForBarrier.t()) {
                            bestHitForBarrier = hit;
                        }
                    }
                }

                if (bestHitForBarrier != null) {
                    // One-Way Directional Valve Check:
                    // If one-way and forward approach (approachDir . surfaceNormal <= 0.0), entity passes through freely.
                    if (barrier.isOneWay()) {
                        if (approachDir.lengthSqr() < 1e-6) {
                            Vec3 posRel = startPos.subtract(barrier.position());
                            if (posRel.dot(bestHitForBarrier.surfaceNormal()) < 0.0) {
                                // Stationary entity on reverse side is blocked!
                            } else {
                                continue;
                            }
                        } else if (approachDir.dot(bestHitForBarrier.surfaceNormal()) <= 0.0) {
                            continue;
                        }
                    }
                    candidates.add(new BarrierHitCandidate(barrier, bestHitForBarrier));
                }
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        // Ascending t sorting with deterministic tie-breaking by entity ID
        candidates.sort(Comparator.comparingDouble((BarrierHitCandidate c) -> c.hit().t())
                .thenComparingInt(c -> c.barrier().getId()));

        BarrierHitCandidate earliest = candidates.get(0);
        return earliest.barrier().handleCollisionHit(entity, startPos, endPos, earliest.hit(), entityRadius);
    }

    /**
     * Identifies whether a living entity qualifies as a genuine boss for arena dissolution.
     */
    public static boolean isBossEntity(LivingEntity entity) {
        if (entity == null || entity instanceof net.minecraft.world.entity.player.Player) {
            return false;
        }
        if (entity.getType().getCategory().isFriendly()) {
            return false;
        }
        if (entity.getMaxHealth() < 100.0F) {
            return false;
        }
        if (entity.getType() == net.minecraft.world.entity.EntityType.WITHER
                || entity.getType() == net.minecraft.world.entity.EntityType.ENDER_DRAGON
                || entity.getType() == net.minecraft.world.entity.EntityType.WARDEN) {
            return true;
        }
        if (entity.getTags().contains("boss") || entity.getTags().contains("apex_predator") || entity.getTags().contains("entropica:boss")) {
            return true;
        }
        return entity.hasCustomName() || entity.getMaxHealth() >= 200.0F;
    }

    /**
     * Called when a living entity dies to trigger instantaneous 0-tick dissolution for bound boss arena barriers.
     */
    public static void onLivingDeath(LivingEntity dyingEntity) {
        if (dyingEntity == null || ACTIVE_BARRIERS.isEmpty()) {
            return;
        }

        UUID deadUUID = dyingEntity.getUUID();
        Level level = dyingEntity.level();
        Vec3 deathPos = dyingEntity.position();

        for (ForcefieldBarrierEntity barrier : ACTIVE_BARRIERS.values()) {
            if (barrier.level() == level && barrier.isBossEncounter() && barrier.isAlive()) {
                if (barrier.getBossEntityUUID() != null && barrier.getBossEntityUUID().equals(deadUUID)) {
                    barrier.dissolveInGlitter();
                } else if (barrier.getBossEntityUUID() == null) {
                    double distSqr = barrier.position().distanceToSqr(deathPos);
                    double maxDist = Math.max(barrier.getRadius() * 1.5, 32.0);
                    if (distSqr <= maxDist * maxDist) {
                        if (isBossEntity(dyingEntity)) {
                            barrier.dissolveInGlitter();
                        }
                    }
                }
            }
        }
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
                    b.dispelByCreator(null);
                    it.remove();
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Finds the optimal edge-fusing snap alignment against nearby existing barriers.
     * Evaluates coplanar extension, 90° corner joints, 45° angled joints, and vertical stacking.
     */
    public static SnapResult findSnapAlignment(
            Level level,
            Vec3 candidatePos,
            float candidateYaw,
            float width,
            float height,
            double snapThreshold
    ) {
        return findSnapAlignment(level, candidatePos, candidateYaw, 0.0F, width, height, BarrierShape.PLANAR_QUAD, snapThreshold);
    }

    public static SnapResult findSnapAlignment(
            Level level,
            Vec3 candidatePos,
            float candidateYaw,
            float candidatePitch,
            float width,
            float height,
            BarrierShape candidateShape,
            double snapThreshold
    ) {
        if (level == null || ACTIVE_BARRIERS.isEmpty()) {
            return SnapResult.unSnapped(candidatePos, candidateYaw, candidatePitch);
        }

        double searchRadius = Math.max(width, height) + snapThreshold + 2.0;
        List<ForcefieldBarrierEntity> nearby = getNearbyBarriers(level, candidatePos, searchRadius);
        if (nearby.isEmpty()) {
            return SnapResult.unSnapped(candidatePos, candidateYaw, candidatePitch);
        }

        SnapResult bestSnap = null;
        double bestDist = Double.MAX_VALUE;

        for (ForcefieldBarrierEntity neighbor : nearby) {
            if (!neighbor.isAlive() || !neighbor.isActive()) continue;

            SnapResult result = evaluateNeighborSnap(
                    neighbor, candidatePos, candidateYaw, candidatePitch, width, height, candidateShape, snapThreshold
            );

            if (result.isSnapped() && result.snapDistance() < bestDist) {
                bestDist = result.snapDistance();
                bestSnap = result;
            }
        }

        return (bestSnap != null) ? bestSnap : SnapResult.unSnapped(candidatePos, candidateYaw, candidatePitch);
    }

    private static SnapResult evaluateNeighborSnap(
            ForcefieldBarrierEntity neighbor,
            Vec3 candPos,
            float candYaw,
            float candPitch,
            float candW,
            float candH,
            BarrierShape candShape,
            double threshold
    ) {
        Vec3 nCenter = neighbor.position();
        float nYaw = neighbor.getYRot();
        float nW = neighbor.getWidth();
        float nH = neighbor.getHeight();

        // 1. Handle Planar Quad Snapping
        if (neighbor.getShape() == BarrierShape.PLANAR_QUAD && candShape == BarrierShape.PLANAR_QUAD) {
            Vec3 nTangent = BarrierGeometry.getTangent(nYaw);

            Vec3 mRight = nCenter.add(nTangent.scale(nW * 0.5));
            Vec3 mLeft = nCenter.subtract(nTangent.scale(nW * 0.5));

            float deltaYaw = Mth.wrapDegrees(candYaw - nYaw);
            float absDeltaYaw = Math.abs(deltaYaw);

            // --- Case A: Vertical Stacking ---
            double hDistSqr = (candPos.x - nCenter.x) * (candPos.x - nCenter.x) + (candPos.z - nCenter.z) * (candPos.z - nCenter.z);
            if (hDistSqr <= threshold * threshold) {
                double topY = nCenter.y + (nH * 0.5) + (candH * 0.5);
                double bottomY = nCenter.y - (nH * 0.5) - (candH * 0.5);

                if (Math.abs(candPos.y - topY) <= threshold) {
                    Vec3 snapped = new Vec3(nCenter.x, topY, nCenter.z);
                    return new SnapResult(true, snapped, nYaw, 0.0F, neighbor, SnapType.VERTICAL_STACK, candPos.distanceTo(snapped));
                } else if (Math.abs(candPos.y - bottomY) <= threshold) {
                    Vec3 snapped = new Vec3(nCenter.x, bottomY, nCenter.z);
                    return new SnapResult(true, snapped, nYaw, 0.0F, neighbor, SnapType.VERTICAL_STACK, candPos.distanceTo(snapped));
                }
            }

            // --- Case B: Coplanar Extension (Straight Wall) ---
            if (absDeltaYaw <= 35.0F || absDeltaYaw >= 145.0F) {
                double s = candPos.subtract(nCenter).dot(nTangent);
                Vec3 snappedCenter = (s >= 0.0)
                        ? nCenter.add(nTangent.scale((nW + candW) * 0.5))
                        : nCenter.subtract(nTangent.scale((nW + candW) * 0.5));

                // Flush height alignment if within threshold
                if (Math.abs(candPos.y - nCenter.y) <= threshold * 1.5) {
                    snappedCenter = new Vec3(snappedCenter.x, nCenter.y, snappedCenter.z);
                }

                double dist = candPos.distanceTo(snappedCenter);
                if (dist <= threshold) {
                    return new SnapResult(true, snappedCenter, nYaw, 0.0F, neighbor, SnapType.COPLANAR_EXTENSION, dist);
                }
            }

            // --- Case C: 90° Corner Joint ---
            if (absDeltaYaw >= 55.0F && absDeltaYaw <= 125.0F) {
                Vec3 jointAnchor = (candPos.distanceToSqr(mRight) <= candPos.distanceToSqr(mLeft)) ? mRight : mLeft;
                float snappedYaw = (deltaYaw > 0.0F) ? Mth.wrapDegrees(nYaw + 90.0F) : Mth.wrapDegrees(nYaw - 90.0F);
                Vec3 sTangent = BarrierGeometry.getTangent(snappedYaw);

                Vec3 pPos = jointAnchor.add(sTangent.scale(candW * 0.5));
                Vec3 pNeg = jointAnchor.subtract(sTangent.scale(candW * 0.5));
                Vec3 snappedPos = (candPos.distanceToSqr(pPos) <= candPos.distanceToSqr(pNeg)) ? pPos : pNeg;

                if (Math.abs(candPos.y - nCenter.y) <= threshold * 1.5) {
                    snappedPos = new Vec3(snappedPos.x, nCenter.y, snappedPos.z);
                }

                double dist = candPos.distanceTo(snappedPos);
                if (dist <= threshold) {
                    return new SnapResult(true, snappedPos, snappedYaw, 0.0F, neighbor, SnapType.CORNER_PERPENDICULAR, dist);
                }
            }

            // --- Case D: 45° Angled Corner Joint ---
            if ((absDeltaYaw > 35.0F && absDeltaYaw < 55.0F) || (absDeltaYaw > 125.0F && absDeltaYaw < 145.0F)) {
                Vec3 jointAnchor = (candPos.distanceToSqr(mRight) <= candPos.distanceToSqr(mLeft)) ? mRight : mLeft;
                float snappedYaw = (deltaYaw > 0.0F)
                        ? Mth.wrapDegrees(nYaw + (absDeltaYaw < 90.0F ? 45.0F : 135.0F))
                        : Mth.wrapDegrees(nYaw - (absDeltaYaw < 90.0F ? 45.0F : 135.0F));
                Vec3 sTangent = BarrierGeometry.getTangent(snappedYaw);

                Vec3 pPos = jointAnchor.add(sTangent.scale(candW * 0.5));
                Vec3 pNeg = jointAnchor.subtract(sTangent.scale(candW * 0.5));
                Vec3 snappedPos = (candPos.distanceToSqr(pPos) <= candPos.distanceToSqr(pNeg)) ? pPos : pNeg;

                if (Math.abs(candPos.y - nCenter.y) <= threshold * 1.5) {
                    snappedPos = new Vec3(snappedPos.x, nCenter.y, snappedPos.z);
                }

                double dist = candPos.distanceTo(snappedPos);
                if (dist <= threshold) {
                    return new SnapResult(true, snappedPos, snappedYaw, 0.0F, neighbor, SnapType.CORNER_ANGLED, dist);
                }
            }
        }

        // 2. Radial / Tangent Snapping (Discs, Bubbles)
        if (candShape == BarrierShape.CIRCULAR_DISC || candShape == BarrierShape.SPHERICAL_BUBBLE) {
            double targetDist = neighbor.getRadius() + candW; // candW serves as radius
            Vec3 delta = candPos.subtract(nCenter);
            double currentDist = delta.length();
            if (currentDist > 1e-4 && Math.abs(currentDist - targetDist) <= threshold) {
                Vec3 dir = delta.normalize();
                Vec3 snapped = nCenter.add(dir.scale(targetDist));
                return new SnapResult(true, snapped, candYaw, candPitch, neighbor, SnapType.TANGENT_RADIAL, candPos.distanceTo(snapped));
            }
        }

        return SnapResult.unSnapped(candPos, candYaw, candPitch);
    }
}
