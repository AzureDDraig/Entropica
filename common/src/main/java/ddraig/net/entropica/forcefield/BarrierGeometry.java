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
        return BarrierShapeRegistry.get(shape.ordinal()).intersect(
                center, yRot, xRot, width, height, radius, rayStart, rayEnd, entityRadius, null
        );
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
        if (width <= 1e-4F || height <= 1e-4F) {
            return BarrierRaycastHit.MISS;
        }

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
        if (radius <= 1e-6F) {
            return BarrierRaycastHit.MISS;
        }

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
        double effectiveRadius = radius + entityRadius;

        if (impact.distanceToSqr(center) <= (effectiveRadius * effectiveRadius)) {
            boolean fromFront = denom < 0.0;
            return new BarrierRaycastHit(true, t, impact, normal, fromFront);
        }

        return BarrierRaycastHit.MISS;
    }

    /**
     * Hemispherical Dome (Upper hemisphere oriented +Y).
     * Hardened against under-approach blind spot by checking t2 when t1 is below equator (V2).
     */
    public static BarrierRaycastHit intersectDome(
            Vec3 center,
            float radius,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius
    ) {
        if (radius <= 1e-6F) {
            return BarrierRaycastHit.MISS;
        }

        Vec3 d = rayEnd.subtract(rayStart);
        Vec3 oc = rayStart.subtract(center);

        double startDist = oc.length();
        double minY = center.y - entityRadius;
        boolean isInside = startDist < radius && rayStart.y >= minY;

        double effRadius = isInside ? Math.max(0.1, radius - entityRadius) : (radius + entityRadius);
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

        // Overlap / Proximity check for entities currently touching or penetrating canopy surface
        if (entityRadius > 0.0 && rayStart.y >= minY) {
            double currentDist = startDist;
            if (isInside) {
                if (currentDist >= radius - entityRadius - 0.12) {
                    Vec3 outward = (currentDist > 1e-5) ? oc.normalize() : new Vec3(0, 1, 0);
                    if (d.dot(outward) >= -1e-4 || d.lengthSqr() < 1e-6) {
                        Vec3 normal = outward.scale(-1.0);
                        Vec3 impact = center.add(outward.scale(radius));
                        return new BarrierRaycastHit(true, 0.0, impact, normal, true);
                    }
                }
            } else {
                if (Math.abs(currentDist - radius) <= entityRadius + 0.12) {
                    Vec3 normal = (currentDist > 1e-5) ? oc.normalize() : new Vec3(0, 1, 0);
                    if (d.dot(normal) <= 1e-4 || d.lengthSqr() < 1e-6) {
                        Vec3 impact = center.add(normal.scale(radius));
                        boolean fromFront = currentDist >= radius;
                        return new BarrierRaycastHit(true, 0.0, impact, normal, fromFront);
                    }
                }
            }
        }

        if (isInside) {
            if (t2 >= 0.0 && t2 <= 1.0) {
                Vec3 impact2 = rayStart.add(d.scale(t2));
                if (impact2.y >= minY) {
                    Vec3 outward = impact2.subtract(center).normalize();
                    if (d.dot(outward) > 0.0) {
                        Vec3 normal = outward.scale(-1.0);
                        return new BarrierRaycastHit(true, t2, impact2, normal, true);
                    }
                }
            }
        } else {
            // Test earliest root t1 first
            if (t1 >= 0.0 && t1 <= 1.0) {
                Vec3 impact1 = rayStart.add(d.scale(t1));
                if (impact1.y >= minY) {
                    Vec3 normal = impact1.subtract(center).normalize();
                    boolean fromFront = d.dot(normal) < 0.0;
                    return new BarrierRaycastHit(true, t1, impact1, normal, fromFront);
                }
            }

            // If t1 did not hit upper hemisphere canopy, test root t2 (e.g. upward approach from below)
            if (t2 >= 0.0 && t2 <= 1.0) {
                Vec3 impact2 = rayStart.add(d.scale(t2));
                if (impact2.y >= minY) {
                    Vec3 normal = impact2.subtract(center).normalize();
                    boolean fromFront = d.dot(normal) < 0.0;
                    return new BarrierRaycastHit(true, t2, impact2, normal, fromFront);
                }
            }
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
        if (radius <= 1e-6F) {
            return BarrierRaycastHit.MISS;
        }

        Vec3 d = rayEnd.subtract(rayStart);
        Vec3 oc = rayStart.subtract(center);

        double startDist = oc.length();
        boolean isInside = startDist < radius;

        double effRadius = isInside ? Math.max(0.1, radius - entityRadius) : (radius + entityRadius);
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

        // Overlap / Proximity check for entities currently touching or penetrating sphere surface
        if (entityRadius > 0.0) {
            double currentDist = startDist;
            if (isInside) {
                if (currentDist >= radius - entityRadius - 0.12) {
                    Vec3 outward = (currentDist > 1e-5) ? oc.normalize() : new Vec3(0, 1, 0);
                    if (d.dot(outward) >= -1e-4 || d.lengthSqr() < 1e-6) {
                        Vec3 normal = outward.scale(-1.0);
                        Vec3 impact = center.add(outward.scale(radius));
                        return new BarrierRaycastHit(true, 0.0, impact, normal, true);
                    }
                }
            } else {
                if (Math.abs(currentDist - radius) <= entityRadius + 0.12) {
                    Vec3 normal = (currentDist > 1e-5) ? oc.normalize() : new Vec3(0, 1, 0);
                    if (d.dot(normal) <= 1e-4 || d.lengthSqr() < 1e-6) {
                        Vec3 impact = center.add(normal.scale(radius));
                        boolean fromFront = currentDist >= radius;
                        return new BarrierRaycastHit(true, 0.0, impact, normal, fromFront);
                    }
                }
            }
        }

        if (isInside) {
            if (t2 >= 0.0 && t2 <= 1.0) {
                Vec3 impact = rayStart.add(d.scale(t2));
                Vec3 outward = impact.subtract(center).normalize();
                if (d.dot(outward) > 0.0) {
                    Vec3 normal = outward.scale(-1.0);
                    return new BarrierRaycastHit(true, t2, impact, normal, true);
                }
            }
        } else {
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
        }

        return BarrierRaycastHit.MISS;
    }

    /**
     * Cylinder Column Intersection.
     * Hardened against diagonal entry blind spots by evaluating both roots against height bounds (V3).
     */
    public static BarrierRaycastHit intersectCylinder(
            Vec3 center,
            float radius,
            float height,
            Vec3 rayStart,
            Vec3 rayEnd,
            double entityRadius
    ) {
        if (radius <= 1e-6F || height <= 1e-6F) {
            return BarrierRaycastHit.MISS;
        }

        Vec3 d = rayEnd.subtract(rayStart);
        double dx = d.x;
        double dz = d.z;
        double ox = rayStart.x - center.x;
        double oz = rayStart.z - center.z;

        double startDistXZ = Math.sqrt(ox * ox + oz * oz);
        double minY = center.y - entityRadius;
        double maxY = center.y + height + entityRadius;
        boolean isInside = startDistXZ < radius && rayStart.y >= minY && rayStart.y <= maxY;

        double effRadius = isInside ? Math.max(0.1, radius - entityRadius) : (radius + entityRadius);
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

        // Overlap / Proximity check for entities currently on cylinder surface
        if (entityRadius > 0.0 && rayStart.y >= minY && rayStart.y <= maxY) {
            double currentDistXZ = startDistXZ;
            if (isInside) {
                if (currentDistXZ >= radius - entityRadius - 0.12) {
                    Vec3 outward = (currentDistXZ > 1e-5) ? new Vec3(ox / currentDistXZ, 0, oz / currentDistXZ) : new Vec3(1, 0, 0);
                    if (d.dot(outward) >= -1e-4 || d.lengthSqr() < 1e-6) {
                        Vec3 normal = outward.scale(-1.0);
                        Vec3 impact = new Vec3(center.x + outward.x * radius, rayStart.y, center.z + outward.z * radius);
                        return new BarrierRaycastHit(true, 0.0, impact, normal, true);
                    }
                }
            } else {
                if (Math.abs(currentDistXZ - radius) <= entityRadius + 0.12) {
                    Vec3 normal = (currentDistXZ > 1e-5) ? new Vec3(ox / currentDistXZ, 0, oz / currentDistXZ) : new Vec3(1, 0, 0);
                    if (d.dot(normal) <= 1e-4 || d.lengthSqr() < 1e-6) {
                        Vec3 impact = new Vec3(center.x + normal.x * radius, rayStart.y, center.z + normal.z * radius);
                        boolean fromFront = currentDistXZ >= radius;
                        return new BarrierRaycastHit(true, 0.0, impact, normal, fromFront);
                    }
                }
            }
        }

        if (isInside) {
            if (t2 >= 0.0 && t2 <= 1.0) {
                Vec3 impact2 = rayStart.add(d.scale(t2));
                if (impact2.y >= minY && impact2.y <= maxY) {
                    Vec3 outward = new Vec3(impact2.x - center.x, 0.0, impact2.z - center.z).normalize();
                    if (d.dot(outward) > 0.0) {
                        Vec3 normal = outward.scale(-1.0);
                        return new BarrierRaycastHit(true, t2, impact2, normal, true);
                    }
                }
            }
        } else {
            // Test earliest root t1 first
            if (t1 >= 0.0 && t1 <= 1.0) {
                Vec3 impact1 = rayStart.add(d.scale(t1));
                if (impact1.y >= minY && impact1.y <= maxY) {
                    Vec3 normal = new Vec3(impact1.x - center.x, 0.0, impact1.z - center.z).normalize();
                    boolean fromFront = d.dot(normal) < 0.0;
                    return new BarrierRaycastHit(true, t1, impact1, normal, fromFront);
                }
            }

            // If t1 was outside height bounds (e.g. diagonal grazing ray), test root t2
            if (t2 >= 0.0 && t2 <= 1.0) {
                Vec3 impact2 = rayStart.add(d.scale(t2));
                if (impact2.y >= minY && impact2.y <= maxY) {
                    Vec3 normal = new Vec3(impact2.x - center.x, 0.0, impact2.z - center.z).normalize();
                    boolean fromFront = d.dot(normal) < 0.0;
                    return new BarrierRaycastHit(true, t2, impact2, normal, fromFront);
                }
            }
        }

        return BarrierRaycastHit.MISS;
    }
}
