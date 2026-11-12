package ddraig.net.entropica.client.input;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.BarrierGeometry;
import ddraig.net.entropica.forcefield.BarrierRaycastHit;
import ddraig.net.entropica.forcefield.BarrierShapeHandler;
import ddraig.net.entropica.forcefield.BarrierShapeRegistry;
import ddraig.net.entropica.item.FirmamentWeaverItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side helper that manages crosshair selection for ForcefieldBarrierEntity.
 * Ensures the barrier only intercepts crosshair picking when the player is holding
 * the Firmament Weaver or a dyeing item and aiming directly at the barrier's membrane,
 * allowing full unobstructed interaction with blocks, chests, and levers behind/inside it.
 */
public class BarrierPickHelper {

    public static boolean canPickBarrier(ForcefieldBarrierEntity barrier) {
        if (barrier == null || !barrier.isAlive() || !barrier.isActive()) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return false;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean holdsWeaver = mainHand.getItem() instanceof FirmamentWeaverItem || offHand.getItem() instanceof FirmamentWeaverItem;
        boolean holdsTintItem = ForcefieldBarrierEntity.resolveColorTint(mainHand) != null || ForcefieldBarrierEntity.resolveColorTint(offHand) != null;

        // If not holding the Weaver or a dye/cleanser item, completely ignore the barrier
        if (!holdsWeaver && !holdsTintItem) {
            return false;
        }

        // If holding the Weaver with an active Drag & Snap anchor, do not pick the barrier
        // so the player can cleanly select Point B on adjacent blocks
        if (holdsWeaver && (FirmamentWeaverItem.hasAnchor(mainHand) || FirmamentWeaverItem.hasAnchor(offHand))) {
            return false;
        }

        // Raycast against the barrier with generous 3D picking tolerance for 2D shapes
        double reach = Math.max(player.blockInteractionRange(), player.entityInteractionRange());
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 rayEnd = eyePos.add(lookVec.scale(reach));

        BarrierRaycastHit hit = pickIntersect(barrier, eyePos, rayEnd, reach);
        if (!hit.hit() || hit.t() < 0.0 || hit.t() > 1.0) {
            return false;
        }

        // If there is an obstructing solid block between the player and the barrier membrane,
        // let the block take precedence so players can place blocks or open chests freely
        HitResult blockHit = player.pick(reach, 1.0F, false);
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            double blockDistSqr = blockHit.getLocation().distanceToSqr(eyePos);
            double barrierDistSqr = hit.impactPoint().distanceToSqr(eyePos);
            if (blockDistSqr < barrierDistSqr - 1e-4) {
                return false;
            }
        }

        return true;
    }

    /**
     * Intersects the player's look ray against the barrier using a generous 3D picking volume.
     * 2D shapes (Planar Quad, Disc, Polygon) are treated as 3D oriented slabs (thickness 0.80m)
     * so they can be easily targeted from any angle, including directly edge-on.
     */
    public static BarrierRaycastHit pickIntersect(ForcefieldBarrierEntity barrier, Vec3 rayStart, Vec3 rayEnd, double reach) {
        Vec3 center = barrier.position();
        float yRot = barrier.getYRot();
        float xRot = barrier.getXRot();
        Vec3 normal = BarrierGeometry.getNormal(yRot, xRot);
        Vec3 tangent = BarrierGeometry.getTangent(yRot);
        Vec3 bitangent = normal.cross(tangent).normalize();

        Vec3 d = rayEnd.subtract(rayStart);

        switch (barrier.getShape()) {
            case PLANAR_QUAD -> {
                Vec3 p0 = rayStart.subtract(center);
                double ox = p0.dot(tangent);
                double oy = p0.dot(bitangent);
                double oz = p0.dot(normal);

                double dx = d.dot(tangent);
                double dy = d.dot(bitangent);
                double dz = d.dot(normal);

                double halfW = (barrier.getWidth() * 0.5) + 0.35;
                double halfH = (barrier.getHeight() * 0.5) + 0.35;
                double halfT = 0.40; // 0.80m thick slab along normal

                double tMin = 0.0;
                double tMax = 1.0;

                // X slab
                if (Math.abs(dx) > 1e-6) {
                    double t1 = (-halfW - ox) / dx;
                    double t2 = (halfW - ox) / dx;
                    if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                    tMin = Math.max(tMin, t1);
                    tMax = Math.min(tMax, t2);
                    if (tMin > tMax) return BarrierRaycastHit.MISS;
                } else if (Math.abs(ox) > halfW) {
                    return BarrierRaycastHit.MISS;
                }

                // Y slab
                if (Math.abs(dy) > 1e-6) {
                    double t1 = (-halfH - oy) / dy;
                    double t2 = (halfH - oy) / dy;
                    if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                    tMin = Math.max(tMin, t1);
                    tMax = Math.min(tMax, t2);
                    if (tMin > tMax) return BarrierRaycastHit.MISS;
                } else if (Math.abs(oy) > halfH) {
                    return BarrierRaycastHit.MISS;
                }

                // Z slab
                if (Math.abs(dz) > 1e-6) {
                    double t1 = (-halfT - oz) / dz;
                    double t2 = (halfT - oz) / dz;
                    if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                    tMin = Math.max(tMin, t1);
                    tMax = Math.min(tMax, t2);
                    if (tMin > tMax) return BarrierRaycastHit.MISS;
                } else if (Math.abs(oz) > halfT) {
                    return BarrierRaycastHit.MISS;
                }

                double tHit = Math.max(0.0, tMin);
                Vec3 impact = rayStart.add(d.scale(tHit));
                return new BarrierRaycastHit(true, tHit, impact, normal, dz < 0.0);
            }
            case CIRCULAR_DISC -> {
                Vec3 p0 = rayStart.subtract(center);
                double oz = p0.dot(normal);
                double dz = d.dot(normal);
                double halfT = 0.40;
                double rMax = barrier.getRadius() + 0.35;
                double rMaxSqr = rMax * rMax;

                double tMin = 0.0;
                double tMax = 1.0;

                if (Math.abs(dz) > 1e-6) {
                    double t1 = (-halfT - oz) / dz;
                    double t2 = (halfT - oz) / dz;
                    if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                    tMin = Math.max(tMin, t1);
                    tMax = Math.min(tMax, t2);
                    if (tMin > tMax) return BarrierRaycastHit.MISS;
                } else if (Math.abs(oz) > halfT) {
                    return BarrierRaycastHit.MISS;
                }

                // Test radial distance at entry
                double tHit = Math.max(0.0, tMin);
                Vec3 testImpact = rayStart.add(d.scale(tHit)).subtract(center);
                double u = testImpact.dot(tangent);
                double v = testImpact.dot(bitangent);
                if (u * u + v * v <= rMaxSqr) {
                    return new BarrierRaycastHit(true, tHit, rayStart.add(d.scale(tHit)), normal, dz < 0.0);
                }

                // Ray-cylinder body test in 2D
                double ox = p0.dot(tangent);
                double oy = p0.dot(bitangent);
                double dx = d.dot(tangent);
                double dy = d.dot(bitangent);
                double a = dx * dx + dy * dy;
                double b = 2.0 * (ox * dx + oy * dy);
                double c = (ox * ox + oy * oy) - rMaxSqr;
                double discr = b * b - 4.0 * a * c;
                if (discr >= 0.0 && Math.abs(a) > 1e-6) {
                    double sqrtD = Math.sqrt(discr);
                    double tCyl1 = (-b - sqrtD) / (2.0 * a);
                    double tCyl2 = (-b + sqrtD) / (2.0 * a);
                    if (tCyl1 > tCyl2) { double tmp = tCyl1; tCyl1 = tCyl2; tCyl2 = tmp; }
                    double tEntry = Math.max(tMin, tCyl1);
                    double tExit = Math.min(tMax, tCyl2);
                    if (tEntry <= tExit && tExit >= 0.0 && tEntry <= 1.0) {
                        double bestT = Math.max(0.0, tEntry);
                        return new BarrierRaycastHit(true, bestT, rayStart.add(d.scale(bestT)), normal, dz < 0.0);
                    }
                }
                return BarrierRaycastHit.MISS;
            }
            case CONVEX_POLYGON -> {
                BarrierShapeHandler handler = BarrierShapeRegistry.get(barrier.getShape().ordinal());
                if (handler != null) {
                    BarrierRaycastHit hit = handler.intersect(
                            center, yRot, xRot,
                            barrier.getWidth() + 0.7F, barrier.getHeight() + 0.7F, barrier.getRadius(),
                            rayStart, rayEnd, 0.40, barrier
                    );
                    if (hit.hit()) return hit;
                }
                // Fallback local slab test
                Vec3 p0 = rayStart.subtract(center);
                double oz = p0.dot(normal);
                double dz = d.dot(normal);
                double halfT = 0.40;
                double maxDim = Math.max(barrier.getWidth(), barrier.getHeight()) * 0.5 + 0.35;
                if (Math.abs(oz) <= halfT + 0.1) {
                    double distSqr = p0.subtract(normal.scale(oz)).lengthSqr();
                    if (distSqr <= maxDim * maxDim) {
                        return new BarrierRaycastHit(true, 0.0, rayStart, normal, true);
                    }
                }
                return BarrierGeometry.intersectPlanarQuad(center, yRot, xRot, barrier.getWidth() + 0.7F, barrier.getHeight() + 0.7F, rayStart, rayEnd, 0.40);
            }
            default -> {
                BarrierShapeHandler handler = BarrierShapeRegistry.get(barrier.getShape().ordinal());
                if (handler != null) {
                    return handler.intersect(
                            center, yRot, xRot,
                            barrier.getWidth(), barrier.getHeight(), barrier.getRadius(),
                            rayStart, rayEnd, 0.35, barrier
                    );
                } else {
                    return BarrierGeometry.intersect(
                            barrier.getShape(), center, yRot, xRot,
                            barrier.getWidth(), barrier.getHeight(), barrier.getRadius(),
                            rayStart, rayEnd, 0.35
                    );
                }
            }
        }
    }
}
