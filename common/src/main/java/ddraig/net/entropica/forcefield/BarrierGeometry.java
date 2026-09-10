package ddraig.net.entropica.forcefield;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * High-precision continuous collision mathematics for arbitrary-shape paper-thin barriers.
 * Supports Quads, Discs, Domes, Spheres, and Cylinders with zero thickness and side-of-arrival detection.
 */
public class BarrierGeometry {

    private static final double EPSILON = 1e-6;

    /**
     * Intersects a ray segment against a barrier based on its shape and orientation.
     */
    public static BarrierRaycastHit intersect(
            BarrierShape shape,
            Vec3 center,
            float yRot,
            float xRot,
            float width,
            float height,
            float radius,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius
    ) {
        return switch (shape) {
            case PLANAR_QUAD -> intersectPlanarQuad(center, yRot, xRot, width, height, rayStart, rayEnd, entityRadius);
            case CIRCULAR_DISC -> intersectCircularDisc(center, yRot, xRot, radius, rayStart, rayEnd, entityRadius);
            case HEMISPHERICAL_DOME -> intersectDome(center, radius, rayStart, rayEnd, entityRadius);
            case SPHERICAL_BUBBLE -> intersectSphere(center, radius, rayStart, rayEnd, entityRadius);
            case CYLINDER -> intersectCylinder(center, radius, height, rayStart, rayEnd, entityRadius);
            case CONVEX_POLYGON -> intersectPlanarQuad(center, yRot, xRot, width, height, rayStart, rayEnd, entityRadius);
        };
    }

    /**
     * Computes the orientation vectors for a planar surface from Minecraft yaw and pitch.
     */
    public static Vec3 getNormal(float yRot, float xRot) {
        float f = -xRot * ((float) Math.PI / 180F);
        float g = -yRot * ((float) Math.PI / 180F);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3(i * j, k, h * j).normalize();
    }

    public static Vec3 getTangent(float yRot) {
        float g = (-yRot + 90.0F) * ((float) Math.PI / 180F);
        return new Vec3(Mth.sin(g), 0.0, Mth.cos(g)).normalize();
    }

    /**
     * Planar Quad Intersection.
     */
    public static BarrierRaycastHit intersectPlanarQuad(
            Vec3 center,
            float yRot,
            float xRot,
            float width,
            float height,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius
    ) {
        Vec3 normal = getNormal(yRot, xRot);
        Vec3 tangent = getTangent(yRot);
        Vec3 bitangent = normal.cross(tangent).normalize();

        Vec3 d = rayEnd.subtract(rayStart);
        double denom = d.dot(normal);

        if (Math.abs(denom) < EPSILON) {
            return BarrierRaycastHit.MISS;
        }

        double t = center.subtract(rayStart).dot(normal) / denom;
        if (t < 0.0 || t > 1.0) {
            return BarrierRaycastHit.MISS;
        }

        Vec3 impact = rayStart.add(d.scale(t));
        Vec3 rel = impact.subtract(center);

        double u = rel.dot(tangent);
        double v = rel.dot(bitangent);

        double halfW = (width * 0.5) + entityRadius;
        double halfH = (height * 0.5) + entityRadius;

        if (Math.abs(u) <= halfW && Math.abs(v) <= halfH) {
            boolean fromFront = denom < 0.0;
            return new BarrierRaycastHit(true, t, impact, normal, fromFront);
        }

        return BarrierRaycastHit.MISS;
    }

    /**
     * Circular Disc Intersection.
     */
    public static BarrierRaycastHit intersectCircularDisc(
            Vec3 center,
            float yRot,
            float xRot,
            float radius,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius
    ) {
        Vec3 normal = getNormal(yRot, xRot);
        Vec3 d = rayEnd.subtract(rayStart);
        double denom = d.dot(normal);

        if (Math.abs(denom) < EPSILON) {
            return BarrierRaycastHit.MISS;
        }

        double t = center.subtract(rayStart).dot(normal) / denom;
        if (t < 0.0 || t > 1.0) {
            return BarrierRaycastHit.MISS;
        }

        Vec3 impact = rayStart.add(d.scale(t));
        double distSqr = impact.distanceToSqr(center);
        double effectiveRadius = radius + entityRadius;

        if (distSqr <= (effectiveRadius * effectiveRadius)) {
            boolean fromFront = denom < 0.0;
            return new BarrierRaycastHit(true, t, impact, normal, fromFront);
        }

        return BarrierRaycastHit.MISS;
    }

    /**
     * Hemispherical Dome (Upper hemisphere oriented +Y).
     */
    public static BarrierRaycastHit intersectDome(
            Vec3 center,
            float radius,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius
    ) {
        BarrierRaycastHit sphereHit = intersectSphere(center, radius, rayStart, rayEnd, entityRadius);
        if (sphereHit.hit() && sphereHit.impactPoint().y >= center.y - entityRadius) {
            return sphereHit;
        }
        return BarrierRaycastHit.MISS;
    }

    /**
     * Complete Spherical Bubble Intersection.
     */
    public static BarrierRaycastHit intersectSphere(
            Vec3 center,
            float radius,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius
    ) {
        Vec3 d = rayEnd.subtract(rayStart);
        Vec3 oc = rayStart.subtract(center);

        double effRadius = radius + entityRadius;
        double a = d.lengthSqr();
        if (a < EPSILON) {
            return BarrierRaycastHit.MISS;
        }

        double b = 2.0 * oc.dot(d);
        double c = oc.lengthSqr() - (effRadius * effRadius);
        double discriminant = (b * b) - (4.0 * a * c);

        if (discriminant < 0.0) {
            return BarrierRaycastHit.MISS;
        }

        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2.0 * a);
        double t2 = (-b + sqrtDisc) / (2.0 * a);

        double t = -1.0;
        if (t1 >= 0.0 && t1 <= 1.0) {
            t = t1;
        } else if (t2 >= 0.0 && t2 <= 1.0) {
            t = t2;
        }

        if (t >= 0.0) {
            Vec3 impact = rayStart.add(d.scale(t));
            Vec3 normal = impact.subtract(center).normalize();
            boolean fromFront = d.dot(normal) < 0.0;
            return new BarrierRaycastHit(true, t, impact, normal, fromFront);
        }

        return BarrierRaycastHit.MISS;
    }

    /**
     * Cylinder Column Intersection.
     */
    public static BarrierRaycastHit intersectCylinder(
            Vec3 center,
            float radius,
            float height,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius
    ) {
        Vec3 d = rayEnd.subtract(rayStart);
        double dx = d.x;
        double dz = d.z;
        double ox = rayStart.x - center.x;
        double oz = rayStart.z - center.z;

        double effRadius = radius + entityRadius;
        double a = (dx * dx) + (dz * dz);
        if (a < EPSILON) {
            return BarrierRaycastHit.MISS;
        }

        double b = 2.0 * ((ox * dx) + (oz * dz));
        double c = (ox * ox) + (oz * oz) - (effRadius * effRadius);
        double disc = (b * b) - (4.0 * a * c);

        if (disc < 0.0) {
            return BarrierRaycastHit.MISS;
        }

        double sqrtDisc = Math.sqrt(disc);
        double t1 = (-b - sqrtDisc) / (2.0 * a);
        double t2 = (-b + sqrtDisc) / (2.0 * a);

        double t = -1.0;
        if (t1 >= 0.0 && t1 <= 1.0) {
            t = t1;
        } else if (t2 >= 0.0 && t2 <= 1.0) {
            t = t2;
        }

        if (t >= 0.0) {
            Vec3 impact = rayStart.add(d.scale(t));
            double minY = center.y;
            double maxY = center.y + height;
            if (impact.y >= minY && impact.y <= maxY) {
                Vec3 normal = new Vec3(impact.x - center.x, 0.0, impact.z - center.z).normalize();
                boolean fromFront = d.dot(normal) < 0.0;
                return new BarrierRaycastHit(true, t, impact, normal, fromFront);
            }
        }

        return BarrierRaycastHit.MISS;
    }
}
