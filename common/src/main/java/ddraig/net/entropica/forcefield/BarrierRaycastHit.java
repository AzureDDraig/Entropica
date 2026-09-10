package ddraig.net.entropica.forcefield;

import net.minecraft.world.phys.Vec3;

/**
 * Result of a continuous swept-ray or raycast intersection against a barrier surface.
 *
 * @param hit           Whether the ray crossed the barrier manifold
 * @param t             Interpolation factor along the ray segment [0.0, 1.0]
 * @param impactPoint   Exact 3D point of contact on the membrane
 * @param surfaceNormal Unit normal vector of the barrier surface at the impact point
 * @param fromFront     True if approaching in the direction opposing the normal, false if from the reverse side
 */
public record BarrierRaycastHit(
        boolean hit,
        double t,
        Vec3 impactPoint,
        Vec3 surfaceNormal,
        boolean fromFront
) {
    public static final BarrierRaycastHit MISS = new BarrierRaycastHit(false, 1.0, Vec3.ZERO, Vec3.ZERO, true);

    /**
     * Calculates the effective normal pointing back toward the approaching entity,
     * ensuring proper side-of-approach bounce physics.
     */
    public Vec3 getEffectiveNormal(Vec3 approachDirection) {
        if (surfaceNormal == null || surfaceNormal.lengthSqr() < 1e-6) {
            return approachDirection.scale(-1).normalize();
        }
        // If approach velocity points opposite to normal (dot < 0), normal is already facing the entity.
        // If approach velocity points along normal (dot > 0), entity approached from behind the normal, so invert.
        double dot = approachDirection.dot(surfaceNormal);
        return dot > 0 ? surfaceNormal.scale(-1.0) : surfaceNormal;
    }
}
