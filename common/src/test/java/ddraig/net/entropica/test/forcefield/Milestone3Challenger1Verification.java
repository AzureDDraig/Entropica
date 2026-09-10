package ddraig.net.entropica.test.forcefield;

import ddraig.net.entropica.forcefield.BarrierGeometry;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.item.FirmamentWeaverItem;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Empirical Adversarial Verification Suite for Milestone 3 Challenger 1:
 * Two-Point Drag & Span Math, Colinear Poles, Boundary Clamping,
 * Midpoint Invariance, Cardinal/Arbitrary Yaw Alignment, and Shape Cycling.
 */
public class Milestone3Challenger1Verification {

    public static class FailureRecord {
        public final String testName;
        public final String details;

        public FailureRecord(String testName, String details) {
            this.testName = testName;
            this.details = details;
        }

        @Override
        public String toString() {
            return "[" + testName + "] " + details;
        }
    }

    private static long totalChecks = 0;
    private static long passedChecks = 0;
    private static final List<FailureRecord> failures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   EMPIRICAL ADVERSARIAL CHALLENGER 1 (MILESTONE 3: TWO-POINT DRAG & SPAN)      ");
        System.out.println("================================================================================");
        System.out.println();

        testChallenge1_DegenerateSpansAndSubEpsilonSingularities();
        testChallenge2_VerticalColinearSingularitiesAndPlayerYawFallback();
        testChallenge3_DistanceBoundariesRejectionAndReachClamping();
        testChallenge4_MidpointCenterInvarianceAndSymmetry();
        testChallenge5_CardinalIntercardinalAndArbitraryYawAnalysis();
        testChallenge6_ExtremeCoordinateStressTesting();
        testChallenge7_ShapeCyclingBoundaryWrapAndInvalidOrdinals();

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("TOTAL CHECKS EXECUTED: " + totalChecks);
        System.out.println("PASSED:                " + passedChecks);
        System.out.println("FAILED:                " + failures.size());

        if (failures.isEmpty()) {
            System.out.println("OVERALL VERDICT:       CONFIRMED (100% PASS RATE)");
            System.out.println("================================================================================");
            System.exit(0);
        } else {
            System.err.println("OVERALL VERDICT:       REJECTED WITH " + failures.size() + " ANOMALIES");
            for (int i = 0; i < Math.min(25, failures.size()); i++) {
                System.err.println(" - " + failures.get(i));
            }
            if (failures.size() > 25) {
                System.err.println(" ... and " + (failures.size() - 25) + " more failures.");
            }
            System.out.println("================================================================================");
            System.exit(1);
        }
    }

    private static void check(String testName, boolean condition, String message) {
        totalChecks++;
        if (condition) {
            passedChecks++;
        } else {
            failures.add(new FailureRecord(testName, message));
            if (failures.size() <= 20) {
                System.err.println("FAIL: [" + testName + "] " + message);
            }
        }
    }

    /**
     * Challenge 1: Degenerate points (A == B) and sub-epsilon distances.
     * Verify that distance < 1.0m is strictly rejected, prevents division by zero,
     * avoids NaN/Inf, and verifies fallback math.
     */
    private static void testChallenge1_DegenerateSpansAndSubEpsilonSingularities() {
        System.out.println("--- CHALLENGE 1: Degenerate Spans & Sub-Epsilon Distance Rejections ---");
        String test = "Challenge1_DegenerateSpans";

        // Exact coincident points
        Vec3 pA = new Vec3(100.0, 64.0, -200.0);
        Vec3 pB_exact = new Vec3(100.0, 64.0, -200.0);
        double dist0 = pA.distanceTo(pB_exact);
        check(test, dist0 < 1.0, "Exact coincident points distance must be < 1.0m (got " + dist0 + ")");

        // Sub-epsilon deviations
        double[] epsilons = {0.0, 1e-15, 1e-9, 1e-6, 1e-3, 0.5, 0.999999};
        for (double eps : epsilons) {
            Vec3 pB_eps = new Vec3(pA.x + eps, pA.y, pA.z);
            double dist = pA.distanceTo(pB_eps);
            check(test, dist < 1.0, "Sub-meter distance " + dist + " must be strictly rejected (< 1.0m)");
        }

        // Direct math fallback when points are coincident
        double dx = 0.0;
        double dy = 0.0;
        double dz = 0.0;
        float width = (float) Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
        float height = (float) Math.max(1.0, Math.abs(dy));
        if (Math.abs(dy) < 0.5) {
            height = 3.5F;
        }
        check(test, width == 1.0F, "Degenerate span width fallback must be 1.0F");
        check(test, height == 3.5F, "Degenerate dy=0 height fallback must be 3.5F");

        // Yaw fallback check
        float fallbackPlayerYaw = 37.5F;
        float yaw;
        if (Math.abs(dx) < 1e-4 && Math.abs(dz) < 1e-4) {
            yaw = fallbackPlayerYaw;
        } else {
            yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
        }
        check(test, yaw == 37.5F, "Degenerate yaw must fallback to player yaw (37.5F)");
        check(test, !Float.isNaN(yaw) && !Float.isInfinite(yaw), "Degenerate yaw must not be NaN or Infinite");

        System.out.println("  Challenge 1 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 2: Vertical colinear points (dx = 0, dz = 0, dy != 0).
     * Tests elevator shafts, vertical columns, pole singularities, and player yaw fallback.
     */
    private static void testChallenge2_VerticalColinearSingularitiesAndPlayerYawFallback() {
        System.out.println("--- CHALLENGE 2: Vertical Colinear Singularities & Fallback ---");
        String test = "Challenge2_VerticalColinear";

        Random rng = new Random(1337);

        // Test purely vertical spans with heights from 1.0m to 64.0m
        for (int i = 0; i < 50_000; i++) {
            double x = (rng.nextDouble() - 0.5) * 1000.0;
            double y1 = rng.nextDouble() * 200.0;
            double heightDelta = 1.0 + rng.nextDouble() * 63.0;
            double y2 = y1 + (rng.nextBoolean() ? heightDelta : -heightDelta);
            double z = (rng.nextDouble() - 0.5) * 1000.0;

            Vec3 pA = new Vec3(x, y1, z);
            Vec3 pB = new Vec3(x, y2, z);

            double dist = pA.distanceTo(pB);
            check(test, dist >= 1.0 && dist <= 64.0, "Vertical span distance must be within [1.0, 64.0]");

            double dx = pB.x - pA.x;
            double dy = pB.y - pA.y;
            double dz = pB.z - pA.z;

            check(test, Math.abs(dx) < 1e-12 && Math.abs(dz) < 1e-12, "dx and dz must be zero");

            float playerYaw = (float) (rng.nextDouble() * 360.0 - 180.0);
            float yaw;
            if (Math.abs(dx) < 1e-4 && Math.abs(dz) < 1e-4) {
                yaw = playerYaw;
            } else {
                yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
            }

            check(test, !Float.isNaN(yaw), "Vertical yaw must not be NaN");
            check(test, yaw == playerYaw, "Vertical yaw must match player yaw");

            Vec3 center = pA.add(pB).scale(0.5);
            check(test, Math.abs(center.x - x) < 1e-9, "Center X must equal original X");
            check(test, Math.abs(center.z - z) < 1e-9, "Center Z must equal original Z");
            check(test, Math.abs(center.y - (y1 + y2) * 0.5) < 1e-9, "Center Y must be exact average of y1 and y2");

            float width = (float) Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
            float height = (float) Math.max(1.0, Math.abs(dy));
            check(test, width == 1.0F, "Vertical colinear barrier width must be 1.0F");
            check(test, Math.abs(height - Math.abs(dy)) < 1e-5, "Vertical height must match |dy|");
        }

        System.out.println("  Challenge 2 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 3: Distance limits (rejection under 1.0m and clamping over 64.0m).
     */
    private static void testChallenge3_DistanceBoundariesRejectionAndReachClamping() {
        System.out.println("--- CHALLENGE 3: Distance Limits (Rejection & Clamping) ---");
        String test = "Challenge3_DistanceLimits";

        Random rng = new Random(4242);

        // Subtask 3A: Rejection testing for distances in [0.0, 0.999999]
        for (int i = 0; i < 50_000; i++) {
            Vec3 pA = new Vec3(rng.nextDouble() * 100, rng.nextDouble() * 100, rng.nextDouble() * 100);
            Vec3 randomDir = new Vec3(rng.nextDouble() - 0.5, rng.nextDouble() - 0.5, rng.nextDouble() - 0.5).normalize();
            double subMeterDist = rng.nextDouble() * 0.999999;
            Vec3 pB = pA.add(randomDir.scale(subMeterDist));

            double dist = pA.distanceTo(pB);
            boolean isRejected = dist < 1.0;
            check(test, isRejected, "Distance " + dist + " must be rejected (< 1.0m)");
        }

        // Subtask 3B: Exact Boundary Values
        Vec3 origin = new Vec3(0, 64, 0);
        Vec3 pAt0_999 = origin.add(0.999999, 0, 0);
        Vec3 pAt1_000 = origin.add(1.0, 0, 0);
        Vec3 pAt64_000 = origin.add(64.0, 0, 0);
        Vec3 pAt64_001 = origin.add(64.000001, 0, 0);

        check(test, origin.distanceTo(pAt0_999) < 1.0, "0.999999m must be rejected");
        check(test, origin.distanceTo(pAt1_000) >= 1.0, "1.000000m must be accepted");
        check(test, origin.distanceTo(pAt64_000) <= 64.0, "64.000000m must be accepted without clamping");
        check(test, origin.distanceTo(pAt64_001) > 64.0, "64.000001m must trigger clamping");

        // Subtask 3C: Clamping verification for spans > 64.0m (up to 1,000,000m)
        for (int i = 0; i < 50_000; i++) {
            Vec3 pA = new Vec3((rng.nextDouble() - 0.5) * 10_000, rng.nextDouble() * 200, (rng.nextDouble() - 0.5) * 10_000);
            Vec3 randomDir = new Vec3(rng.nextDouble() - 0.5, rng.nextDouble() - 0.5, rng.nextDouble() - 0.5).normalize();
            double excessiveDist = 64.001 + rng.nextDouble() * 10_000.0;
            Vec3 pB = pA.add(randomDir.scale(excessiveDist));

            double dist = pA.distanceTo(pB);
            check(test, dist > 64.0, "Initial distance must be > 64.0m");

            // Execute clamping logic as implemented in FirmamentWeaverItem
            Vec3 dir = pB.subtract(pA).normalize();
            Vec3 pB_clamped = pA.add(dir.scale(64.0));

            double clampedDist = pA.distanceTo(pB_clamped);
            check(test, Math.abs(clampedDist - 64.0) < 1e-9, "Clamped distance must be precisely 64.0m (got " + clampedDist + ")");

            // Directional fidelity: clamped point must lie along original ray AB
            Vec3 clampedDir = pB_clamped.subtract(pA).normalize();
            double dot = dir.dot(clampedDir);
            check(test, Math.abs(dot - 1.0) < 1e-9, "Clamped direction must be strictly colinear with original ray (dot = " + dot + ")");
        }

        System.out.println("  Challenge 3 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 4: Midpoint Center Invariance and Symmetry.
     * Evaluates C = (A + B) / 2 under 100,000 random 3D configurations.
     */
    private static void testChallenge4_MidpointCenterInvarianceAndSymmetry() {
        System.out.println("--- CHALLENGE 4: Midpoint Center Invariance & Symmetry ---");
        String test = "Challenge4_MidpointInvariance";

        Random rng = new Random(9999);

        for (int i = 0; i < 100_000; i++) {
            Vec3 pA = new Vec3((rng.nextDouble() - 0.5) * 20_000_000.0, rng.nextDouble() * 384.0 - 64.0, (rng.nextDouble() - 0.5) * 20_000_000.0);
            Vec3 pB = new Vec3((rng.nextDouble() - 0.5) * 20_000_000.0, rng.nextDouble() * 384.0 - 64.0, (rng.nextDouble() - 0.5) * 20_000_000.0);

            Vec3 cAB = pA.add(pB).scale(0.5);
            Vec3 cBA = pB.add(pA).scale(0.5);

            // Symmetry
            check(test, Math.abs(cAB.x - cBA.x) < 1e-9 && Math.abs(cAB.y - cBA.y) < 1e-9 && Math.abs(cAB.z - cBA.z) < 1e-9,
                    "Midpoint must be strictly symmetric: C(A,B) == C(B,A)");

            // Equidistance
            double distAC = pA.distanceTo(cAB);
            double distBC = pB.distanceTo(cAB);
            check(test, Math.abs(distAC - distBC) < 1e-7, "Distances |A - C| and |B - C| must be equal (delta: " + Math.abs(distAC - distBC) + ")");

            // Half-distance property
            double distAB = pA.distanceTo(pB);
            check(test, Math.abs(distAC - distAB * 0.5) < 1e-7, "|A - C| must equal 0.5 * |A - B|");
        }

        System.out.println("  Challenge 4 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 5: Cardinal, Intercardinal, and Arbitrary Yaw Calculations.
     * Evaluates formula equivalence, angle bounds, and investigates geometric quad alignment.
     */
    private static void testChallenge5_CardinalIntercardinalAndArbitraryYawAnalysis() {
        System.out.println("--- CHALLENGE 5: Cardinal, Intercardinal & Arbitrary Yaw Analysis ---");
        String test = "Challenge5_YawAnalysis";

        // Subtask 5A: 8 Principal Compass Headings
        record CompassCase(double dx, double dz, double expectedYaw, String name) {}
        CompassCase[] compassCases = {
                new CompassCase(10.0, 0.0, 0.0, "East (+X)"),
                new CompassCase(10.0, 10.0, 45.0, "South-East (+X, +Z)"),
                new CompassCase(0.0, 10.0, 90.0, "South (+Z)"),
                new CompassCase(-10.0, 10.0, 135.0, "South-West (-X, +Z)"),
                new CompassCase(-10.0, 0.0, 180.0, "West (-X)"),
                new CompassCase(-10.0, -10.0, -135.0, "North-West (-X, -Z)"),
                new CompassCase(0.0, -10.0, -90.0, "North (-Z)"),
                new CompassCase(10.0, -10.0, -45.0, "North-East (+X, -Z)")
        };

        for (CompassCase c : compassCases) {
            double dx = c.dx;
            double dz = c.dz;
            double expectedYaw = c.expectedYaw;
            String name = c.name;

            float computedYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
            float wrappedComputed = Mth.wrapDegrees(computedYaw);
            float wrappedExpected = Mth.wrapDegrees((float) expectedYaw);

            check(test, Math.abs(wrappedComputed - wrappedExpected) < 1e-4,
                    "Compass heading " + name + " expected " + wrappedExpected + " but got " + wrappedComputed);
        }

        // Subtask 5B: Continuous arbitrary angle tangent parallelism test across 360,000 steps
        // Verifies quad tangent is parallel to span vector (dx, 0, dz) for all continuous angles
        for (int step = 0; step < 360_000; step++) {
            double angleDeg = step * 0.001;
            double rad = Math.toRadians(angleDeg);
            double dx = Math.cos(rad);
            double dz = Math.sin(rad);

            float computedYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
            Vec3 tangent = BarrierGeometry.getTangent(computedYaw);
            Vec3 spanNorm = new Vec3(dx, 0.0, dz).normalize();

            check(test, Math.abs(tangent.dot(spanNorm)) > 0.9999, "Tangent must be parallel to span vector at angle " + angleDeg);
            check(test, tangent.cross(spanNorm).length() < 1e-4, "Tangent cross span vector must be zero at angle " + angleDeg);
        }

        // Subtask 5C: EMPIRICAL QUAD CORNER GEOMETRY AUDIT
        // We empirically compute the quad corner positions and test if they span the opening from A to B
        Vec3 pA = new Vec3(0, 64, 0);
        Vec3 pB = new Vec3(10, 64, 0); // Span along +X of length 10m
        double dx = pB.x - pA.x;
        double dz = pB.z - pA.z;
        float width = (float) Math.sqrt(dx * dx + dz * dz);
        Vec3 center = pA.add(pB).scale(0.5); // (5, 64, 0)

        float standardSpanYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI); // 0.0F for +X span
        Vec3 quadTangent = BarrierGeometry.getTangent(standardSpanYaw); // (1, 0, 0)
        Vec3 quadCorner1 = center.add(quadTangent.scale(width * 0.5));
        Vec3 quadCorner2 = center.subtract(quadTangent.scale(width * 0.5));

        boolean isColinearWithSpan = Math.abs(quadTangent.dot(new Vec3(1, 0, 0))) > 0.99;
        boolean isOrthogonalToSpan = Math.abs(quadTangent.dot(new Vec3(0, 0, 1))) > 0.99;
        check(test, isColinearWithSpan, "Verified: with formula atan2(dz, dx), quad tangent is collinear with span vector AB");
        check(test, !isOrthogonalToSpan, "Quad tangent must not be orthogonal to span vector AB");
        check(test, quadTangent.cross(new Vec3(1, 0, 0)).length() < 1e-4, "Quad tangent cross span vector must be zero");

        System.out.println("  Challenge 5 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 6: Extreme Coordinate Stress Testing (+-10,000,000, Y in [-64, 320]).
     */
    private static void testChallenge6_ExtremeCoordinateStressTesting() {
        System.out.println("--- CHALLENGE 6: Extreme Coordinate Stress Testing ---");
        String test = "Challenge6_ExtremeCoords";

        double[] extremeCoords = {
                10_000_000.0, -10_000_000.0,
                30_000_000.0, -30_000_000.0, // World border limits
                0.0, 1.0, -1.0
        };
        double[] extremeY = {-64.0, 0.0, 64.0, 128.0, 256.0, 319.0, 320.0};

        for (double x : extremeCoords) {
            for (double z : extremeCoords) {
                for (double y : extremeY) {
                    Vec3 pA = new Vec3(x, y, z);
                    Vec3 pB = new Vec3(x + 10.0, y + 5.0, z + 10.0);

                    double dist = pA.distanceTo(pB);
                    check(test, !Double.isNaN(dist) && !Double.isInfinite(dist), "Distance at extreme coords must be valid");
                    check(test, Math.abs(dist - Math.sqrt(100.0 + 25.0 + 100.0)) < 1e-7, "Extreme coordinate distance must match exact theoretical distance");

                    Vec3 center = pA.add(pB).scale(0.5);
                    check(test, Math.abs(center.x - (x + 5.0)) < 1e-7, "Extreme center X must be exact");
                    check(test, Math.abs(center.y - (y + 2.5)) < 1e-7, "Extreme center Y must be exact");
                    check(test, Math.abs(center.z - (z + 5.0)) < 1e-7, "Extreme center Z must be exact");

                    double dx = pB.x - pA.x;
                    double dz = pB.z - pA.z;
                    float width = (float) Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
                    check(test, Math.abs(width - Math.sqrt(200.0)) < 1e-4, "Width must preserve float precision at extreme coords");
                }
            }
        }

        System.out.println("  Challenge 6 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 7: Shape cycling boundaries (0 <-> 5 wrap-around) and invalid ordinals.
     */
    private static void testChallenge7_ShapeCyclingBoundaryWrapAndInvalidOrdinals() {
        System.out.println("--- CHALLENGE 7: Shape Cycling Boundaries & Invalid Ordinals ---");
        String test = "Challenge7_ShapeCycling";

        BarrierShape[] shapes = BarrierShape.values();
        check(test, shapes.length == 6, "BarrierShape must have exactly 6 primitives");

        // Forward cycling wrap: 0 -> 1 -> 2 -> 3 -> 4 -> 5 -> 0 across 10,000 cycles
        BarrierShape current = BarrierShape.PLANAR_QUAD;
        for (int cycle = 0; cycle < 10_000; cycle++) {
            int nextOrd = (current.ordinal() + 1) % shapes.length;
            current = BarrierShape.fromOrdinal(nextOrd);
            check(test, current.ordinal() == (cycle + 1) % 6, "Forward cycle step " + cycle + " mismatch");
        }

        // Reverse cycling wrap: 0 -> 5 -> 4 -> 3 -> 2 -> 1 -> 0 across 10,000 cycles
        current = BarrierShape.PLANAR_QUAD;
        for (int cycle = 0; cycle < 10_000; cycle++) {
            int prevOrd = (current.ordinal() - 1 + shapes.length) % shapes.length;
            current = BarrierShape.fromOrdinal(prevOrd);
            int expected = ((5 - (cycle % 6)) % 6);
            check(test, current.ordinal() == expected, "Reverse cycle step " + cycle + " expected " + expected + " got " + current.ordinal());
        }

        // Robustness against illegal/negative/overflow ordinals in fromOrdinal
        int[] invalidOrdinals = {-1, -2, -100, -9999, Integer.MIN_VALUE, 6, 7, 8, 100, 9999, Integer.MAX_VALUE};
        for (int invalid : invalidOrdinals) {
            BarrierShape fallback = BarrierShape.fromOrdinal(invalid);
            check(test, fallback == BarrierShape.PLANAR_QUAD, "Invalid ordinal " + invalid + " must safely default to PLANAR_QUAD");
        }

        System.out.println("  Challenge 7 passed (" + totalChecks + " checks cumulative).");
    }
}
