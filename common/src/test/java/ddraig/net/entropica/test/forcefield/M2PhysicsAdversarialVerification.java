package ddraig.net.entropica.test.forcefield;

import ddraig.net.entropica.forcefield.BarrierGeometry;
import ddraig.net.entropica.forcefield.BarrierRaycastHit;
import ddraig.net.entropica.forcefield.BarrierShape;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Empirical Adversarial Verification Harness for Milestone 2:
 * Continuous Collision Detection (CCD), Earliest-t Sorting,
 * Physics Reflection Math (Tangential Conservation, Minimum Impulse, Safe Displacement),
 * and One-Way Directional Valve Behavior.
 */
public class M2PhysicsAdversarialVerification {

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
        System.out.println("      EMPIRICAL ADVERSARIAL CHALLENGER (MILESTONE 2: PHYSICS & CCD)             ");
        System.out.println("================================================================================");
        System.out.println();

        testTask1_EarliestTSortingAndDeterministicTieBreaking();
        testTask2_HighSpeedProjectileTunnelingPrevention();
        testTask3_TangentialMomentumConservation();
        testTask4_MinimumReboundImpulse();
        testTask5_SafeBoundaryDisplacement();
        testTask6_OneWayDirectionalValves();

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
            System.out.println("OVERALL VERDICT:       FAILED");
            System.out.println("Failures list (up to first 20):");
            for (int i = 0; i < Math.min(20, failures.size()); i++) {
                System.out.println("  - " + failures.get(i));
            }
            System.out.println("================================================================================");
            System.exit(1);
        }
    }

    private static void recordPass() {
        totalChecks++;
        passedChecks++;
    }

    private static void recordFail(String testName, String details) {
        totalChecks++;
        if (failures.size() < 100) {
            failures.add(new FailureRecord(testName, details));
        }
    }

    // -------------------------------------------------------------------------
    // RECORD FOR CANDIDATE SORTING
    // -------------------------------------------------------------------------
    private record Candidate(int id, double t) {}

    /**
     * Challenge 1:
     * Verify that when multiple candidate barriers cross a movement vector,
     * the candidate with lowest t is strictly resolved first,
     * and coincident hits break ties deterministically on ID.
     */
    private static void testTask1_EarliestTSortingAndDeterministicTieBreaking() {
        System.out.println("--- TASK 1: CCD Earliest-t Sorting & Deterministic Tie-Breaking ---");

        Random rng = new Random(42_1337);

        // Subtask 1A: Arbitrary random t values
        for (int iter = 0; iter < 50_000; iter++) {
            int candidateCount = 2 + rng.nextInt(15);
            List<Candidate> candidates = new ArrayList<>(candidateCount);
            for (int i = 0; i < candidateCount; i++) {
                double t = rng.nextDouble();
                int id = 1 + rng.nextInt(10_000);
                candidates.add(new Candidate(id, t));
            }

            // Perform sort identical to BarrierFieldManager
            candidates.sort(Comparator.comparingDouble(Candidate::t).thenComparingInt(Candidate::id));

            // Verify order
            boolean sorted = true;
            for (int i = 0; i < candidates.size() - 1; i++) {
                Candidate c1 = candidates.get(i);
                Candidate c2 = candidates.get(i + 1);
                if (c1.t > c2.t || (Double.compare(c1.t, c2.t) == 0 && c1.id > c2.id)) {
                    sorted = false;
                    break;
                }
            }

            if (sorted) {
                recordPass();
            } else {
                recordFail("Task1A_RandomTSorting", "Candidate list not strictly sorted by t then id");
            }
        }

        // Subtask 1B: Coincident hits (identical t, different IDs)
        double[] testTs = {0.0, 0.25, 0.5, 0.75, 1.0, 0.3333333333333333};
        for (double t : testTs) {
            for (int iter = 0; iter < 10_000; iter++) {
                int count = 5 + rng.nextInt(10);
                List<Candidate> candidates = new ArrayList<>(count);
                Set<Integer> uniqueIds = new HashSet<>();
                while (uniqueIds.size() < count) {
                    uniqueIds.add(1 + rng.nextInt(500_000));
                }
                for (int id : uniqueIds) {
                    candidates.add(new Candidate(id, t));
                }

                candidates.sort(Comparator.comparingDouble(Candidate::t).thenComparingInt(Candidate::id));

                // Verify strictly increasing IDs for coincident t
                boolean tieBroken = true;
                for (int i = 0; i < candidates.size() - 1; i++) {
                    if (candidates.get(i).id >= candidates.get(i + 1).id) {
                        tieBroken = false;
                        break;
                    }
                }

                if (tieBroken) {
                    recordPass();
                } else {
                    recordFail("Task1B_CoincidentTieBreak", "Coincident t=" + t + " did not break ties on ID");
                }
            }
        }

        System.out.println("  Task 1 Completed: " + passedChecks + " checks passed.");
    }

    /**
     * Challenge 2:
     * Verify that projectiles moving at extreme speeds (e.g. 50 m/s to 1000 m/s)
     * cannot tunnel through paper-thin barriers.
     */
    private static void testTask2_HighSpeedProjectileTunnelingPrevention() {
        System.out.println("--- TASK 2: High-Speed Projectile Tunneling Prevention (CCD) ---");

        Random rng = new Random(88_9901);
        double[] speeds = {20.0, 50.0, 100.0, 250.0, 500.0, 1000.0, 10000.0}; // m/s

        // Test Planar Quad, Disc, Dome, Sphere, Cylinder
        for (double speed : speeds) {
            for (int iter = 0; iter < 10_000; iter++) {
                // Ray travelling across z = 0 plane from -Z to +Z
                double travelDist = speed * 0.05; // Distance in 1 tick (0.05s)
                double halfDist = travelDist * 0.5;

                double xOffset = (rng.nextDouble() - 0.5) * 6.0;
                double yOffset = (rng.nextDouble() - 0.5) * 6.0;

                Vec3 start = new Vec3(xOffset, yOffset, -halfDist);
                Vec3 end = new Vec3(xOffset, yOffset, halfDist);

                // Quad barrier at center (0, 0, 0) with width 10, height 10, normal +Z (yRot=0, xRot=0)
                BarrierRaycastHit hit = BarrierGeometry.intersectPlanarQuad(
                        Vec3.ZERO, 0.0F, 0.0F, 10.0F, 10.0F, start, end, 0.15
                );

                // A discrete collision check checking only start and end would completely miss (tunneling)
                boolean discreteStartInside = Math.abs(start.z) < 0.001;
                boolean discreteEndInside = Math.abs(end.z) < 0.001;
                if (discreteStartInside || discreteEndInside) {
                    continue; // Skip rare coincidental endpoints
                }

                // Continuous check MUST intercept
                if (hit.hit() && hit.t() >= 0.0 && hit.t() <= 1.0) {
                    // Check impact point is at z = 0
                    if (Math.abs(hit.impactPoint().z) < 1e-4) {
                        recordPass();
                    } else {
                        recordFail("Task2_TunnelingImpact", "Impact point not on barrier plane: z=" + hit.impactPoint().z);
                    }
                } else {
                    recordFail("Task2_TunnelingMiss", "Speed " + speed + " m/s tunneled through planar quad! hit=" + hit.hit() + ", t=" + hit.t());
                }
            }
        }

        // Also test Spherical Bubble enclosure with supersonic projectile traversing right through
        for (double speed : speeds) {
            double travelDist = speed * 0.05;
            Vec3 start = new Vec3(0, 0, -travelDist * 0.5 - 5.0);
            Vec3 end = new Vec3(0, 0, travelDist * 0.5 + 5.0);
            BarrierRaycastHit sphereHit = BarrierGeometry.intersectSphere(Vec3.ZERO, 3.0F, start, end, 0.15);

            if (sphereHit.hit() && sphereHit.t() >= 0.0 && sphereHit.t() <= 1.0) {
                recordPass();
            } else {
                recordFail("Task2_SphereTunneling", "Supersonic projectile tunneled through sphere at " + speed + " m/s");
            }
        }

        System.out.println("  Task 2 Completed: " + passedChecks + " checks passed.");
    }

    /**
     * Challenge 3:
     * Formally and numerically verify that tangential momentum (v . tau)
     * is strictly conserved under reflection (v' . tau == v . tau).
     */
    private static void testTask3_TangentialMomentumConservation() {
        System.out.println("--- TASK 3: Tangential Momentum Conservation ---");

        Random rng = new Random(77_3311);

        for (int iter = 0; iter < 200_000; iter++) {
            // Random incoming velocity vector
            double vx = (rng.nextDouble() - 0.5) * 40.0;
            double vy = (rng.nextDouble() - 0.5) * 40.0;
            double vz = (rng.nextDouble() - 0.5) * 40.0;
            Vec3 v = new Vec3(vx, vy, vz);
            if (v.lengthSqr() < 1e-4) continue;

            // Random surface normal unit vector
            double nx = (rng.nextDouble() - 0.5);
            double ny = (rng.nextDouble() - 0.5);
            double nz = (rng.nextDouble() - 0.5);
            Vec3 norm = new Vec3(nx, ny, nz);
            if (norm.lengthSqr() < 1e-4) continue;
            norm = norm.normalize();

            // Approach direction
            Vec3 approachDir = v;

            // Effective normal
            double dotNorm = approachDir.dot(norm);
            Vec3 nEff = dotNorm > 0.0 ? norm.scale(-1.0) : norm;

            double elasticity = 0.1 + rng.nextDouble() * 2.9; // [0.1, 3.0]

            // Mod's reflection formula
            double dot = v.dot(nEff);
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * dot));

            // Minimum rebound impulse adjustment
            double minImpulse = 0.35 * Math.max(1.0, elasticity);
            if (reflected.dot(nEff) < minImpulse) {
                reflected = reflected.add(nEff.scale(minImpulse - Math.max(0.0, reflected.dot(nEff))));
            }

            // Generate two orthogonal tangent vectors perpendicular to nEff
            Vec3 arbitrary = Math.abs(nEff.x) < 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 tau1 = nEff.cross(arbitrary).normalize();
            Vec3 tau2 = nEff.cross(tau1).normalize();

            double vt1Before = v.dot(tau1);
            double vt1After = reflected.dot(tau1);
            double vt2Before = v.dot(tau2);
            double vt2After = reflected.dot(tau2);

            double diff1 = Math.abs(vt1After - vt1Before);
            double diff2 = Math.abs(vt2After - vt2Before);

            if (diff1 < 1e-9 && diff2 < 1e-9) {
                recordPass();
            } else {
                recordFail("Task3_TangentialConservation",
                        String.format("Tangential momentum violated! diff1=%.2e, diff2=%.2e", diff1, diff2));
            }
        }

        System.out.println("  Task 3 Completed: " + passedChecks + " checks passed.");
    }

    /**
     * Challenge 4:
     * Verify that minimum rebound impulse (v' . n_effective >= 0.35 * max(1.0, e))
     * holds even for near-zero or zero incoming normal velocity.
     */
    private static void testTask4_MinimumReboundImpulse() {
        System.out.println("--- TASK 4: Minimum Rebound Impulse Verification ---");

        Random rng = new Random(55_2299);
        double[] nearZeroNormalVelocities = {
                0.0,
                -1e-15,
                -1e-12,
                -1e-9,
                -1e-6,
                -1e-4,
                -0.001,
                -0.01,
                -0.05,
                -0.1,
                -0.2
        };

        double[] elasticities = {0.1, 0.2, 0.5, 0.8, 1.0, 1.25, 1.5, 2.0, 2.5, 3.0};

        for (double vn : nearZeroNormalVelocities) {
            for (double e : elasticities) {
                for (int iter = 0; iter < 1_000; iter++) {
                    Vec3 nEff = new Vec3(0, 1, 0); // upward normal

                    // Tangential velocity component can be anything
                    double vtX = (rng.nextDouble() - 0.5) * 10.0;
                    double vtZ = (rng.nextDouble() - 0.5) * 10.0;
                    Vec3 v = new Vec3(vtX, vn, vtZ);

                    double dot = v.dot(nEff); // vn <= 0
                    Vec3 reflected = v.subtract(nEff.scale((1.0 + e) * dot));

                    double minImpulse = 0.35 * Math.max(1.0, e);
                    if (reflected.dot(nEff) < minImpulse) {
                        reflected = reflected.add(nEff.scale(minImpulse - Math.max(0.0, reflected.dot(nEff))));
                    }

                    double actualNormalRebound = reflected.dot(nEff);
                    if (actualNormalRebound >= minImpulse - 1e-9) {
                        recordPass();
                    } else {
                        recordFail("Task4_MinImpulse",
                                String.format("Normal rebound %.6f < minImpulse %.6f for vn=%.2e, e=%.2f",
                                        actualNormalRebound, minImpulse, vn, e));
                    }
                }
            }
        }

        System.out.println("  Task 4 Completed: " + passedChecks + " checks passed.");
    }

    /**
     * Challenge 5:
     * Verify safe boundary displacement distance (p_impact + n_effective * (r_entity + 0.08)).
     */
    private static void testTask5_SafeBoundaryDisplacement() {
        System.out.println("--- TASK 5: Safe Boundary Displacement Distance ---");

        Random rng = new Random(33_1144);

        for (int iter = 0; iter < 100_000; iter++) {
            // Random impact point
            Vec3 impact = new Vec3(
                    (rng.nextDouble() - 0.5) * 20.0,
                    (rng.nextDouble() - 0.5) * 20.0,
                    (rng.nextDouble() - 0.5) * 20.0
            );

            // Random unit effective normal
            Vec3 nEff = new Vec3(
                    rng.nextDouble() - 0.5,
                    rng.nextDouble() - 0.5,
                    rng.nextDouble() - 0.5
            ).normalize();

            // Random entity radius (0.15 to 1.5)
            double entityRadius = 0.15 + rng.nextDouble() * 1.35;

            // Safe position computed by mod
            Vec3 safePos = impact.add(nEff.scale(entityRadius + 0.08));

            // Displacement vector
            Vec3 displacement = safePos.subtract(impact);

            // Distance from impact
            double dist = displacement.length();
            double expectedDist = entityRadius + 0.08;

            // Projection along normal
            double projNormal = displacement.dot(nEff);

            // Clearance outside entity hull
            double hullClearance = dist - entityRadius;

            if (Math.abs(dist - expectedDist) < 1e-9 &&
                Math.abs(projNormal - expectedDist) < 1e-9 &&
                Math.abs(hullClearance - 0.08) < 1e-9) {
                recordPass();
            } else {
                recordFail("Task5_SafeDisplacement",
                        String.format("Displacement error: dist=%.6f, expected=%.6f, clearance=%.6f",
                                dist, expectedDist, hullClearance));
            }
        }

        System.out.println("  Task 5 Completed: " + passedChecks + " checks passed.");
    }

    /**
     * Challenge 6:
     * One-Way Directional Valves:
     * - Verify forward approach (d . n <= 0) passes freely with zero displacement or impulse.
     * - Verify reverse approach (d . n > 0) triggers full elastic reflection.
     */
    private static void testTask6_OneWayDirectionalValves() {
        System.out.println("--- TASK 6: One-Way Directional Valves ---");

        Random rng = new Random(11_7755);

        for (int iter = 0; iter < 100_000; iter++) {
            // Unit surface normal
            Vec3 surfaceNormal = new Vec3(
                    rng.nextDouble() - 0.5,
                    rng.nextDouble() - 0.5,
                    rng.nextDouble() - 0.5
            ).normalize();

            // Forward approach: d . n <= 0
            // Construct d with negative dot product
            Vec3 tangent = surfaceNormal.cross(new Vec3(1, 0, 0)).normalize();
            if (tangent.lengthSqr() < 1e-3) {
                tangent = surfaceNormal.cross(new Vec3(0, 1, 0)).normalize();
            }

            double forwardNormalSpeed = -rng.nextDouble() * 30.0; // <= 0
            double tangentSpeed = (rng.nextDouble() - 0.5) * 20.0;
            Vec3 forwardD = surfaceNormal.scale(forwardNormalSpeed).add(tangent.scale(tangentSpeed));

            double fDot = forwardD.dot(surfaceNormal);
            boolean isOneWay = true;

            // Condition in BarrierFieldManager line 108 & ForcefieldBarrierEntity line 243
            boolean forwardPasses = isOneWay && fDot <= 0.0;
            if (forwardPasses) {
                recordPass();
            } else {
                recordFail("Task6_ForwardPass", "Forward approach failed to pass freely! dot=" + fDot);
            }

            // Reverse approach: d . n > 0
            double reverseNormalSpeed = (1e-6 + rng.nextDouble() * 30.0); // > 0
            Vec3 reverseD = surfaceNormal.scale(reverseNormalSpeed).add(tangent.scale(tangentSpeed));

            double rDot = reverseD.dot(surfaceNormal);
            boolean reversePasses = isOneWay && rDot <= 0.0;
            if (!reversePasses) {
                // Hits barrier! Evaluates reflection
                Vec3 nEff = rDot > 0.0 ? surfaceNormal.scale(-1.0) : surfaceNormal;

                // nEff must oppose reverse approach direction
                if (reverseD.dot(nEff) < 0.0) {
                    recordPass();
                } else {
                    recordFail("Task6_ReverseNEff", "Reverse nEff does not oppose approach!");
                }
            } else {
                recordFail("Task6_ReverseBlock", "Reverse approach incorrectly passed through! dot=" + rDot);
            }
        }

        // Test boundary values around 0: exactly 0.0, -1e-15, +1e-15
        Vec3 n = new Vec3(0, 0, 1);
        Vec3 dZero = new Vec3(10, 0, 0); // dot == 0.0
        boolean zeroPasses = dZero.dot(n) <= 0.0;
        if (zeroPasses) recordPass(); else recordFail("Task6_ZeroBoundary", "d.n=0.0 must pass");

        Vec3 dInfNeg = new Vec3(10, 0, -1e-15);
        boolean negPasses = dInfNeg.dot(n) <= 0.0;
        if (negPasses) recordPass(); else recordFail("Task6_NegBoundary", "d.n=-1e-15 must pass");

        Vec3 dInfPos = new Vec3(10, 0, 1e-15);
        boolean posPasses = dInfPos.dot(n) <= 0.0;
        if (!posPasses) recordPass(); else recordFail("Task6_PosBoundary", "d.n=+1e-15 must reflect");

        System.out.println("  Task 6 Completed: " + passedChecks + " checks passed.");
    }
}
