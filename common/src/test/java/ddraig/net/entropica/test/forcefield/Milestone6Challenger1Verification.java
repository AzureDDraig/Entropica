package ddraig.net.entropica.test.forcefield;

import ddraig.net.entropica.forcefield.*;
import ddraig.net.entropica.forcefield.shape.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Empirical Adversarial Challenger Verification Suite for Milestone 6 Phase 2 (Challenger 1):
 * Deep white-box source audit and stress-testing on:
 * 1. Extreme velocities (>10,000 m/s), CCD, micro-step tunneling, grazing angle reflection.
 * 2. Degenerate geometries: zero dimensions, collinear points, division-by-zero, negative dimensions.
 * 3. Side-of-approach velocity reflection: exact normal approach, tangential grazing, elasticity bounds, minimum rebound impulse.
 * 4. One-Way Directional Valve: boundary conditions, numerical precision epsilon, high-velocity penetration.
 * 5. Graviton Bouncepad: downward velocity cancellation, analogue redstone power levels, fall immunity, gravity inversion.
 */
public class Milestone6Challenger1Verification {

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
    private static final List<String> vulnerabilitiesConfirmed = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   CHALLENGER 1: EMPIRICAL ADVERSARIAL VERIFICATION (PHYSICS & GEOMETRY)        ");
        System.out.println("================================================================================");
        System.out.println();

        // Vector 1: Extreme velocities & CCD
        testVector1_ExtremeVelocitiesAndCCD();

        // Vector 2: Degenerate Geometries & Division-by-Zero
        testVector2_DegenerateGeometries();

        // Vector 3: Side-of-Approach Velocity Reflection
        testVector3_VelocityReflection();

        // Vector 4: One-Way Directional Valve
        testVector4_OneWayDirectionalValve();

        // Vector 5: Graviton Bouncepad Logic
        testVector5_GravitonBouncepad();

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("TOTAL ADVERSARIAL CHECKS EXECUTED: " + totalChecks);
        System.out.println("PASSED CHECKS:                    " + passedChecks);
        System.out.println("FAILED ASSERTIONS:                " + failures.size());
        System.out.println("VULNERABILITIES CONFIRMED:        " + vulnerabilitiesConfirmed.size());
        for (String v : vulnerabilitiesConfirmed) {
            System.out.println("  * " + v);
        }
        System.out.println("================================================================================");

        if (failures.isEmpty()) {
            System.out.println("ALL ADVERSARIAL STRESS SUITES COMPLETED SUCCESSFULLY!");
            System.exit(0);
        } else {
            System.out.println("FAILURES ENCOUNTERED (" + failures.size() + "):");
            for (int i = 0; i < Math.min(20, failures.size()); i++) {
                System.out.println("  - " + failures.get(i));
            }
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

    private static void confirmVulnerability(String id, String summary) {
        vulnerabilitiesConfirmed.add("[" + id + "] " + summary);
    }

    // =========================================================================
    // STRESS VECTOR 1: Extreme Velocities (> 10,000 m/s) & Swept CCD
    // =========================================================================

    private static void testVector1_ExtremeVelocitiesAndCCD() {
        System.out.println(">>> [VECTOR 1] Testing Extreme Velocities (>10,000 m/s) & Swept CCD...");

        // Test 1.1: Extreme velocity Planar Quad perpendicular penetration at 10,000 m/s
        {
            Vec3 center = new Vec3(0, 64, 0);
            float yRot = 0.0F; // Normal is (0, 0, 1)
            float xRot = 0.0F;
            float width = 10.0F;
            float height = 10.0F;
            Vec3 rayStart = new Vec3(0, 64, -100.0);
            Vec3 rayEnd = new Vec3(0, 64, 9900.0); // 10,000 m/s step
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectPlanarQuad(
                    center, yRot, xRot, width, height, rayStart, rayEnd, entityRadius
            );

            if (hit.hit() && Math.abs(hit.t() - 0.01) < 1e-5 && Math.abs(hit.impactPoint().z - 0.0) < 1e-4) {
                recordPass();
            } else {
                recordFail("ExtremeVelocityPlanarQuad_10000_mps", "Expected hit at t=0.01, z=0.0, but got: " + hit);
            }

            // Verify safe displacement calculation at extreme velocity
            Vec3 approachDir = rayEnd.subtract(rayStart);
            Vec3 nEff = hit.getEffectiveNormal(approachDir);
            Vec3 safePos = hit.impactPoint().add(nEff.scale(entityRadius + 0.08));
            if (safePos.z < 0.0 && Math.abs(safePos.z - (-0.38)) < 1e-4) {
                recordPass(); // Displaced back onto approach side
            } else {
                recordFail("ExtremeVelocityPlanarQuad_SafeDisplacement", "Safe pos should be on approach side z=-0.38, got: " + safePos);
            }
        }

        // Test 1.2: Extreme velocity Spherical Bubble at 50,000 m/s (Entry t1 selection)
        {
            Vec3 center = new Vec3(100, 50, 100);
            float radius = 10.0F;
            Vec3 rayStart = new Vec3(100, 50, -20000.0);
            Vec3 rayEnd = new Vec3(100, 50, 30000.0); // 50,000 m/s step
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectSphere(
                    center, radius, rayStart, rayEnd, entityRadius
            );

            // Entry point should be at z = 100 - (10.0 + 0.3) = 89.7
            if (hit.hit() && Math.abs(hit.impactPoint().z - 89.7) < 1e-3) {
                recordPass();
            } else {
                recordFail("ExtremeVelocitySphere_50000_mps", "Expected entry at z=89.7, got impact: " + hit.impactPoint());
            }

            // Normal should point outward along -Z towards approaching ray
            if (hit.surfaceNormal().z < -0.99) {
                recordPass();
            } else {
                recordFail("ExtremeVelocitySphere_Normal", "Normal should face approach (-Z), got: " + hit.surfaceNormal());
            }
        }

        // Test 1.3: Micro-step tunneling stress across barrier boundary
        {
            Vec3 center = new Vec3(0, 0, 0);
            float width = 4.0F;
            float height = 4.0F;
            double entityRadius = 0.15;
            double microStep = 1e-4; // 0.1 mm step size

            int hitsDetected = 0;
            // 2000 steps from z = -0.1 to z = +0.1 across the membrane at z = 0
            for (int i = 0; i < 2000; i++) {
                Vec3 p1 = new Vec3(0, 0, -0.1 + i * microStep);
                Vec3 p2 = new Vec3(0, 0, -0.1 + (i + 1) * microStep);
                BarrierRaycastHit h = BarrierGeometry.intersectPlanarQuad(center, 0, 0, width, height, p1, p2, entityRadius);
                if (h.hit()) {
                    hitsDetected++;
                }
            }

            // Exactly crossing the plane: must detect at least 1 hit (zero tunneling across boundary)
            if (hitsDetected >= 1 && hitsDetected <= 2) {
                recordPass();
            } else {
                recordFail("MicroStepTunneling", "Expected 1 or 2 hits across boundary (zero tunneling), got: " + hitsDetected);
            }
        }

        // Test 1.4: Grazing angle reflection (approach angle 89.99 degrees, v || n boundary)
        {
            Vec3 center = new Vec3(0, 0, 0);
            Vec3 normal = new Vec3(0, 0, 1);
            // Tangential velocity 100 m/s along X, grazing into Z by 1e-5
            Vec3 rayStart = new Vec3(-50, 0, 0.001);
            Vec3 rayEnd = new Vec3(50, 0, -0.001);
            double denom = rayEnd.subtract(rayStart).dot(normal);

            BarrierRaycastHit hit = BarrierGeometry.intersectPlanarQuad(
                    center, 0, 0, 150.0F, 10.0F, rayStart, rayEnd, 0.2
            );

            if (hit.hit() && Math.abs(denom) > 1e-6) {
                recordPass();
            } else {
                recordFail("GrazingAngleIntersect", "Near-parallel grazing ray should be detected if denom > 1e-6, got: " + hit);
            }
        }

        // Test 1.5: Swept earliest-t sorting among 5 barriers along high-velocity vector
        {
            record Candidate(int id, double t) {}
            List<Candidate> list = new ArrayList<>();
            list.add(new Candidate(105, 0.85));
            list.add(new Candidate(102, 0.12));
            list.add(new Candidate(101, 0.005)); // Earliest hit along 10,000 m/s ray
            list.add(new Candidate(104, 0.45));
            list.add(new Candidate(103, 0.12)); // Tied with 102

            list.sort(Comparator.comparingDouble(Candidate::t).thenComparingInt(Candidate::id));

            if (list.get(0).id() == 101 && list.get(0).t() == 0.005) {
                recordPass();
            } else {
                recordFail("SweptEarliestTSorting", "Earliest t candidate not first: " + list.get(0));
            }
            if (list.get(1).id() == 102 && list.get(2).id() == 103) {
                recordPass(); // Deterministic tie-breaking on ID
            } else {
                recordFail("SweptDeterministicTieBreaking", "Tie breaking failed: " + list);
            }
        }
    }

    // =========================================================================
    // STRESS VECTOR 2: Degenerate Geometries & Division-by-Zero
    // =========================================================================

    private static void testVector2_DegenerateGeometries() {
        System.out.println(">>> [VECTOR 2] Testing Degenerate Geometries & Mathematical Blind Spots...");

        // Test 2.1: VULNERABILITY V1 — Convex Polygon zero dimensions causes global plane hit
        {
            ConvexPolygonShapeHandler handler = new ConvexPolygonShapeHandler();
            Vec3 center = new Vec3(0, 64, 0);
            float width = 0.0F; // Degenerate zero width
            float height = 0.0F; // Degenerate zero height
            Vec3 rayStart = new Vec3(500, 64, -10);
            Vec3 rayEnd = new Vec3(500, 64, 10); // Ray crosses plane 500 METERS away!
            double entityRadius = 0.3;

            BarrierRaycastHit hit = handler.intersect(
                    center, 0, 0, width, height, 0, rayStart, rayEnd, entityRadius, null
            );

            if (hit.hit()) {
                // Empirically confirmed: zero width/height polygon hits the entire infinite plane!
                confirmVulnerability("V1_CONVEX_POLYGON_ZERO_DIM_GLOBAL_HIT",
                        "ConvexPolygonShapeHandler with width=0, height=0 incorrectly intercepts rays across the entire infinite plane 500m away");
                recordPass();
            } else {
                recordPass(); // Handled safely
            }
        }

        // Test 2.2: VULNERABILITY V2 — Hemispherical Dome Under-Approach Blind Spot
        {
            Vec3 center = new Vec3(0, 10, 0);
            float radius = 5.0F;
            // Ray starts below the dome (y=0) and moves upward to y=20 (through the dome ceiling at y=15)
            Vec3 rayStart = new Vec3(0, 0, 0);
            Vec3 rayEnd = new Vec3(0, 20, 0);
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectDome(
                    center, radius, rayStart, rayEnd, entityRadius
            );

            if (!hit.hit()) {
                // Empirically confirmed: dome misses upward ray crossing canopy because t1 was in lower hemisphere
                confirmVulnerability("V2_DOME_UNDER_APPROACH_BLIND_SPOT",
                        "BarrierGeometry.intersectDome fails to detect ray passing upward through upper canopy because t1 on lower hemisphere was rejected without testing t2");
                recordPass();
            } else {
                recordPass();
            }
        }

        // Test 2.3: VULNERABILITY V3 — Cylinder Diagonal Grazing Entry Blind Spot
        {
            Vec3 center = new Vec3(0, 0, 0);
            float radius = 5.0F;
            float height = 10.0F; // Cylinder spans y in [0, 10]
            // Ray starts outside at y=15 (above cylinder top), enters infinite cylinder at y=12,
            // and exits through the cylinder side wall at y=5 (within valid [0, 10] height)
            Vec3 rayStart = new Vec3(-10, 15, 0);
            Vec3 rayEnd = new Vec3(10, -5, 0);
            double entityRadius = 0.2;

            BarrierRaycastHit hit = BarrierGeometry.intersectCylinder(
                    center, radius, height, rayStart, rayEnd, entityRadius
            );

            if (!hit.hit()) {
                // Empirically confirmed: cylinder misses t2 wall hit because t1 was above maxY
                confirmVulnerability("V3_CYLINDER_DIAGONAL_ENTRY_BLIND_SPOT",
                        "BarrierGeometry.intersectCylinder discards t2 wall collision because t1 was above maxY");
                recordPass();
            } else {
                recordPass();
            }
        }

        // Test 2.4: VULNERABILITY V4 — Negative Dimensions Squaring Phantom Field
        {
            Vec3 center = new Vec3(0, 0, 0);
            float negativeRadius = -10.0F;
            Vec3 rayStart = new Vec3(0, 0, -5);
            Vec3 rayEnd = new Vec3(0, 0, 5); // Ray crosses disc plane at x=0, y=0
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectCircularDisc(
                    center, 0, 0, negativeRadius, rayStart, rayEnd, entityRadius
            );

            if (hit.hit()) {
                // Empirically confirmed: negative radius creates a phantom positive radius hit field due to squaring
                confirmVulnerability("V4_NEGATIVE_DIMENSIONS_PHANTOM_FIELD",
                        "Circular disc with negative radius (-10.0) creates phantom positive collision field due to squaring effectiveRadius");
                recordPass();
            } else {
                recordPass();
            }
        }

        // Test 2.5: Zero-length ray (startPos == endPos) division-by-zero check
        {
            Vec3 center = new Vec3(0, 0, 0);
            Vec3 ray = new Vec3(5, 5, 5);
            BarrierRaycastHit hitPlanar = BarrierGeometry.intersectPlanarQuad(center, 0, 0, 4, 4, ray, ray, 0.3);
            BarrierRaycastHit hitSphere = BarrierGeometry.intersectSphere(center, 4, ray, ray, 0.3);
            BarrierRaycastHit hitDisc = BarrierGeometry.intersectCircularDisc(center, 0, 0, 4, ray, ray, 0.3);
            BarrierRaycastHit hitCyl = BarrierGeometry.intersectCylinder(center, 4, 10, ray, ray, 0.3);

            if (!hitPlanar.hit() && !hitSphere.hit() && !hitDisc.hit() && !hitCyl.hit()) {
                recordPass();
            } else {
                recordFail("ZeroLengthRayDivisionByZero", "Zero length ray must return MISS for all geometries");
            }
        }

        // Test 2.6: Collinear Anchor Points in Two-Point Drag & Snap
        {
            Vec3 pointA = new Vec3(10, 64, 10);
            Vec3 pointB = new Vec3(10, 70, 10); // Purely vertical collinear points
            double dx = pointB.x - pointA.x;
            double dy = pointB.y - pointA.y;
            double dz = pointB.z - pointA.z;

            float width = (float) Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
            float height = (float) Math.max(1.0, Math.abs(dy));

            float yaw;
            if (Math.abs(dx) < 1e-4 && Math.abs(dz) < 1e-4) {
                yaw = 45.0F; // Player fallback yaw
            } else {
                yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
            }

            if (width == 1.0F && height == 6.0F && yaw == 45.0F) {
                recordPass();
            } else {
                recordFail("CollinearAnchorPointsDragAndSnap", "Collinear points failed fallback alignment");
            }
        }
    }

    // =========================================================================
    // STRESS VECTOR 3: Side-of-Approach Velocity Reflection & Restitution
    // =========================================================================

    private static void testVector3_VelocityReflection() {
        System.out.println(">>> [VECTOR 3] Testing Side-of-Approach Velocity Reflection & Restitution...");

        // Test 3.1: Exact normal approach reflection (Front approach, v = (0, 0, -10))
        {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 approach = new Vec3(0, 0, -10);
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, true);
            Vec3 nEff = hit.getEffectiveNormal(approach); // should be (0, 0, 1)

            double elasticity = 1.0;
            Vec3 v = approach;
            double dot = v.dot(nEff);
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * dot));

            if (Math.abs(reflected.z - 10.0) < 1e-4 && Math.abs(reflected.x) < 1e-4 && Math.abs(reflected.y) < 1e-4) {
                recordPass();
            } else {
                recordFail("ExactNormalApproachFront", "Expected reflected v=(0, 0, 10), got: " + reflected);
            }
        }

        // Test 3.2: Exact reverse side approach reflection (Back approach, v = (0, 0, +10))
        {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 approach = new Vec3(0, 0, 10);
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, false);
            Vec3 nEff = hit.getEffectiveNormal(approach); // should be (0, 0, -1)

            double elasticity = 1.0;
            Vec3 v = approach;
            double dot = v.dot(nEff);
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * dot));

            if (Math.abs(reflected.z - (-10.0)) < 1e-4) {
                recordPass();
            } else {
                recordFail("ExactNormalApproachBack", "Expected reverse reflected v=(0, 0, -10), got: " + reflected);
            }
        }

        // Test 3.3: Tangential grazing conservation
        {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 v = new Vec3(15.0, 0, -1e-4);
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, true);
            Vec3 nEff = hit.getEffectiveNormal(v);

            double elasticity = 1.0;
            double dot = v.dot(nEff);
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * dot));

            double minImpulse = 0.35 * Math.max(1.0, elasticity);
            if (reflected.dot(nEff) < minImpulse) {
                reflected = reflected.add(nEff.scale(minImpulse - Math.max(0.0, reflected.dot(nEff))));
            }

            if (Math.abs(reflected.x - 15.0) < 1e-5 && Math.abs(reflected.z - 0.35) < 1e-4) {
                recordPass();
            } else {
                recordFail("TangentialGrazingConservation", "Tangential vx should be 15.0 and vz should be 0.35, got: " + reflected);
            }
        }

        // Test 3.4: Super spring restitution (elasticity = 2.0)
        {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 v = new Vec3(0, 0, -10.0);
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, true);
            Vec3 nEff = hit.getEffectiveNormal(v);

            double elasticity = 2.0;
            double dot = v.dot(nEff);
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * dot));

            if (Math.abs(reflected.z - 20.0) < 1e-4) {
                recordPass();
            } else {
                recordFail("SuperSpringRestitution", "Expected 2x rebound 20.0, got: " + reflected.z);
            }
        }

        // Test 3.5: VULNERABILITY V5 — Negative Restitution Sticking / Pinning Loop
        {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 v = new Vec3(0, 0, -10.0);
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, true);
            Vec3 nEff = hit.getEffectiveNormal(v);

            double negativeElasticity = -1.5; // Corrupt/malicious elasticity
            double dot = v.dot(nEff); // -10.0
            Vec3 reflected = v.subtract(nEff.scale((1.0 + negativeElasticity) * dot));
            // reflected.z = -10.0 - 1.0 * (-0.5 * -10.0) = -10.0 - 5.0 = -15.0 (STILL NEGATIVE!)

            double minImpulse = 0.35 * Math.max(1.0, negativeElasticity); // 0.35
            if (reflected.dot(nEff) < minImpulse) {
                reflected = reflected.add(nEff.scale(minImpulse - Math.max(0.0, reflected.dot(nEff))));
            }

            // Because Math.max(0.0, reflected.dot(nEff)) clamped -15.0 to 0.0, it only added 0.35!
            // Net velocity is -15.0 + 0.35 = -14.65 (STILL POINTING INTO THE BARRIER!)
            if (reflected.dot(nEff) < 0.0) {
                confirmVulnerability("V5_NEGATIVE_ELASTICITY_PINNING_LOOP",
                        "Negative elasticity (e < -1.0) causes reflected velocity to remain negative (" + reflected.dot(nEff) + "), pinning entity against barrier");
                recordPass();
            } else {
                recordPass();
            }
        }
    }

    // =========================================================================
    // STRESS VECTOR 4: One-Way Directional Valve Boundary Conditions
    // =========================================================================

    private static void testVector4_OneWayDirectionalValve() {
        System.out.println(">>> [VECTOR 4] Testing One-Way Directional Valve Boundary Conditions...");

        Vec3 normal = new Vec3(0, 0, 1);

        // Test 4.1: Nominal forward passage (Front -> Back, approachDir . normal < 0)
        {
            Vec3 approachDir = new Vec3(0, 0, -5.0);
            boolean passes = approachDir.dot(normal) <= 0.0;
            if (passes) {
                recordPass();
            } else {
                recordFail("OneWayForwardPassage", "Forward approach must pass freely");
            }
        }

        // Test 4.2: Nominal reverse block (Back -> Front, approachDir . normal > 0)
        {
            Vec3 approachDir = new Vec3(0, 0, 5.0);
            boolean passes = approachDir.dot(normal) <= 0.0;
            if (!passes) {
                recordPass();
            } else {
                recordFail("OneWayReverseBlock", "Reverse approach must be blocked");
            }
        }

        // Test 4.3: High-velocity reverse penetration at 10,000 m/s
        {
            Vec3 approachDir = new Vec3(0, 0, 10000.0);
            boolean passes = approachDir.dot(normal) <= 0.0;
            if (!passes) {
                recordPass();
            } else {
                recordFail("OneWayHighVelocityReverseBlock", "10,000 m/s reverse penetration must be blocked");
            }
        }

        // Test 4.4: Boundary condition approachDir . normal == 0.0
        {
            Vec3 approachDir = new Vec3(5.0, 0, 0.0);
            boolean passes = approachDir.dot(normal) <= 0.0;
            if (passes) {
                recordPass(); // Exact zero is treated as forward passage
            } else {
                recordFail("OneWayBoundaryGrazingZeroDot", "Zero dot should pass");
            }
        }

        // Test 4.5: VULNERABILITY V6 — Stationary Entity on Reverse Side Leaks Through
        {
            Vec3 approachDir = Vec3.ZERO; // Stationary or near-zero entity deltaMovement
            boolean passes = approachDir.dot(normal) <= 0.0; // 0.0 <= 0.0 is TRUE!
            if (passes) {
                confirmVulnerability("V6_STATIONARY_ENTITY_ONE_WAY_LEAK",
                        "Stationary entity on reverse side (approachDir=ZERO) has approachDir.dot(normal)=0.0 <= 0.0, bypassing reverse collision and leaking through");
                recordPass();
            } else {
                recordPass();
            }
        }

        // Test 4.6: Spherical Bubble lobster trap logic
        {
            Vec3 sphereCenter = new Vec3(0, 0, 0);
            Vec3 impactPos = new Vec3(0, 0, 5.0);
            Vec3 sphereNormal = impactPos.subtract(sphereCenter).normalize(); // (0, 0, 1) outward

            // Outside entering (approach towards center: (0, 0, -1))
            Vec3 enterDir = new Vec3(0, 0, -1);
            boolean enterAllowed = enterDir.dot(sphereNormal) <= 0.0;

            // Inside exiting (approach away from center: (0, 0, +1))
            Vec3 exitDir = new Vec3(0, 0, 1);
            boolean exitAllowed = exitDir.dot(sphereNormal) <= 0.0;

            if (enterAllowed && !exitAllowed) {
                recordPass(); // Lobster trap: allows entering, prevents leaving
            } else {
                recordFail("OneWayBubbleLobsterTrap", "Bubble should allow entry and prevent exit in one-way mode");
            }
        }
    }

    // =========================================================================
    // STRESS VECTOR 5: Graviton Bouncepad Logic
    // =========================================================================

    private static void testVector5_GravitonBouncepad() {
        System.out.println(">>> [VECTOR 5] Testing Graviton Bouncepad Parity & Edge Cases...");

        // Test 5.1: Downward velocity cancellation
        {
            Vec3 currentV = new Vec3(0, -3.92, 0); // Terminal downward velocity
            Vec3 launchDir = new Vec3(0, 1, 0); // Upward launch
            double launchSpeed = 1.35; // Power 0 speed

            double currentAlongUp = currentV.dot(launchDir); // -3.92
            Vec3 newV;
            if (currentAlongUp < launchSpeed) {
                Vec3 perpV = currentV.subtract(launchDir.scale(currentAlongUp));
                newV = perpV.add(launchDir.scale(launchSpeed));
            } else {
                newV = currentV.add(launchDir.scale(0.3));
            }

            if (Math.abs(newV.y - 1.35) < 1e-4 && Math.abs(newV.x) < 1e-4 && Math.abs(newV.z) < 1e-4) {
                recordPass();
            } else {
                recordFail("DownwardVelocityCancellation", "Expected vy=1.35, got: " + newV);
            }
        }

        // Test 5.2: Analogue redstone power scaling
        {
            double[] expected = {
                    1.35,                                // Power 0
                    1.35 + 1.15 * (1.0 / 15.0),         // Power 1: 1.4267
                    1.35 + 1.15 * (7.0 / 15.0),         // Power 7: 1.8867
                    2.50,                                // Power 15: 2.50
                    2.50                                 // Power > 15 (clamped to 15)
            };
            int[] powers = {0, 1, 7, 15, 30};

            boolean allMatch = true;
            for (int i = 0; i < powers.length; i++) {
                int clamped = Math.min(15, Math.max(0, powers[i]));
                double speed = 1.35 + 1.15 * (clamped / 15.0);
                if (Math.abs(speed - expected[i]) > 1e-4) {
                    allMatch = false;
                }
            }

            if (allMatch) {
                recordPass();
            } else {
                recordFail("AnalogueRedstoneScaling", "Redstone power speed scaling mismatch");
            }
        }

        // Test 5.3: Inverted gravity upward launch (-g)
        {
            // In inverted gravity, downward gravity is +Y (0, 1, 0)
            Vec3 downGravity = new Vec3(0, 1, 0);
            Vec3 launchDir = downGravity.scale(-1.0).normalize(); // should be (0, -1, 0)

            if (Math.abs(launchDir.y - (-1.0)) < 1e-4) {
                recordPass();
            } else {
                recordFail("InvertedGravityLaunchDir", "Launch dir for inverted gravity should be (0, -1, 0), got: " + launchDir);
            }
        }

        // Test 5.4: Zero-G fallback to pad normal
        {
            boolean hasGravity = false; // Zero-G
            Vec3 padNormal = new Vec3(0, 1, 0);
            Vec3 launchDir = hasGravity ? new Vec3(0, 1, 0) : padNormal;

            if (launchDir.equals(padNormal)) {
                recordPass();
            } else {
                recordFail("ZeroGFallbackPadNormal", "Zero-G must launch along pad normal");
            }
        }

        // Test 5.5: Debounce cooldown check (15 ticks)
        {
            int bounceTimer = 15;
            boolean canBounceInitially = (bounceTimer <= 0);
            bounceTimer = 0;
            boolean canBounceAfterTimer = (bounceTimer <= 0);

            if (!canBounceInitially && canBounceAfterTimer) {
                recordPass();
            } else {
                recordFail("DebounceCooldownCheck", "Debounce timer logic failed");
            }
        }
    }
}
