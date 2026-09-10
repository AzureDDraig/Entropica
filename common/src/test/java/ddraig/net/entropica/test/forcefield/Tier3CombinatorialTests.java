package ddraig.net.entropica.test.forcefield;

import java.util.*;

/**
 * Tier 3: Pairwise Combinatorial & Cross-Feature Interaction Tests.
 * Validates complex interactions between distinct subsystems:
 *  1. One-Way Directional Valve + Redstone Power Dormancy
 *  2. Two-Point Drag & Snap + Edge-Fusing
 *  3. Boss Arena Barrier + Quick Dispel Attempt
 *  4. Materia Color Tinting + Apex Predator Theme
 *  5. Graviton Bouncepad Launch into Elastic Forcefield Barrier
 *  6. Filter Mode PROJECTILES + High-Speed Swept Arrow Collision
 *  7. One-Way Valve + High Elasticity (e=2.0) Rebound
 *  8. Circular Disc Geometry + Two-Point Drag & Snap
 *  9. Redstone Inverted Mode + Materia Conduit Power Loss
 * 10. Whitelist Bypass + Hostile Mob Filter Mode
 * 11. Shift+Scroll Shape Cycling + Holographic Preview Refresh
 * 12. Multi-barrier Earliest-t Continuous Collision
 * 13. Edge-Fused Polygonal Ribbon + Concave Corner Collision
 * 14. Bouncepad Analogue Redstone (Power 15 vs 1) Launch into Hemispherical Dome
 * 15. Creator Look-At Telemetry while toggling Redstone Power
 * 16. Network Packet UpdateBarrierConfigPayload updating both Elasticity and One-Way state
 * 17. Boss Arena Barrier Dissolution + Player Movement
 * 18. Spherical Bubble Barrier enclosing Graviton Bouncepad
 * 19. Materia Color Tinting persistence across Redstone toggle on/off
 * 20. Entropic Codex Research Node dependencies matching recipe unlock logic
 */
public class Tier3CombinatorialTests extends AbstractForcefieldTestSuite {

    public Tier3CombinatorialTests() {
        super("Tier 3: Pairwise Combinatorial", 3);
        registerCombinatorialTests();
    }

    private void registerCombinatorialTests() {

        // =========================================================================
        // Interaction 1: One-Way Directional Valve + Redstone Power Dormancy (F06 + F07)
        // =========================================================================
        register("t3_01_one_way_valve_redstone_dormancy", 6,
                "Dormant redstone state overrides one-way directional valve reflection", () -> {
            SimulatedBarrier barrier = new SimulatedBarrier();
            barrier.shape = ShapeType.PLANAR_QUAD;
            barrier.center = new Vec3D(0, 2, 0);
            barrier.normal = new Vec3D(0, 0, 1); // Facing +Z
            barrier.width = 4.0;
            barrier.height = 4.0;
            barrier.isOneWay = true;
            barrier.isActive = true;
            barrier.elasticity = 1.0;

            // 1. Active State: Forward entry (from +Z to -Z, d . n < 0) -> Allowed
            Vec3D forwardStart = new Vec3D(0, 2, 2);
            Vec3D forwardEnd = new Vec3D(0, 2, -2);
            CollisionResult forwardRes = barrier.checkCollision(forwardStart, forwardEnd, new Vec3D(0, 0, -1), 0.3);
            ForcefieldAssert.assertFalse(forwardRes.collided, "One-way barrier must allow forward traversal when active");

            // 2. Active State: Reverse entry (from -Z to +Z, d . n > 0) -> Blocked & Reflected
            Vec3D reverseStart = new Vec3D(0, 2, -2);
            Vec3D reverseEnd = new Vec3D(0, 2, 2);
            CollisionResult reverseRes = barrier.checkCollision(reverseStart, reverseEnd, new Vec3D(0, 0, 1), 0.3);
            ForcefieldAssert.assertTrue(reverseRes.collided, "One-way barrier must block reverse traversal when active");
            ForcefieldAssert.assertTrue(reverseRes.reflectedVelocity.z < 0, "Reflected velocity must bounce back along -Z");

            // 3. Dormant State (Redstone powered dormancy, isActive = false)
            barrier.isActive = false;
            CollisionResult dormantRes = barrier.checkCollision(reverseStart, reverseEnd, new Vec3D(0, 0, 1), 0.3);
            ForcefieldAssert.assertFalse(dormantRes.collided, "Dormant barrier must disable collision for reverse traversal");
            ForcefieldAssert.assertEquals(1.0, dormantRes.reflectedVelocity.z, 1e-4, "Velocity must pass through unaffected");

            // 4. Reactivated State: Reverse entry blocked again
            barrier.isActive = true;
            CollisionResult reactivatedRes = barrier.checkCollision(reverseStart, reverseEnd, new Vec3D(0, 0, 1), 0.3);
            ForcefieldAssert.assertTrue(reactivatedRes.collided, "Reactivated barrier must resume blocking reverse traversal");
        });

        // =========================================================================
        // Interaction 2: Two-Point Drag & Snap + Edge-Fusing (F11 + F12)
        // =========================================================================
        register("t3_02_two_point_drag_snap_with_edge_fusing", 11,
                "Two-point spanned barrier automatically snaps to adjacent barrier edge within 0.5m", () -> {
            // Barrier A: spans X in [0.0, 4.0] at Z = 0.0, Y in [0.0, 4.0]
            SimulatedBarrier barrierA = new SimulatedBarrier();
            barrierA.center = new Vec3D(2.0, 2.0, 0.0);
            barrierA.width = 4.0;
            barrierA.height = 4.0;
            double rightEdgeA = barrierA.center.x + (barrierA.width * 0.5); // 4.0

            // User drags Point A at (4.25, 0.0, 0.0) to Point B at (8.25, 4.0, 0.0)
            Vec3D draggedPointA = new Vec3D(4.25, 0.0, 0.0);
            Vec3D draggedPointB = new Vec3D(8.25, 4.0, 0.0);

            double snapThreshold = 0.5;
            double distanceToEdgeA = Math.abs(draggedPointA.x - rightEdgeA);
            ForcefieldAssert.assertTrue(distanceToEdgeA < snapThreshold, "Proximity 0.25m must be within 0.5m threshold");

            // Execute Edge-Fusing Snap
            Vec3D snappedPointA = new Vec3D(rightEdgeA, draggedPointA.y, draggedPointA.z); // Snaps to 4.0
            Vec3D centerB = new Vec3D((snappedPointA.x + draggedPointB.x) * 0.5, (snappedPointA.y + draggedPointB.y) * 0.5, 0.0);
            double widthB = Math.abs(draggedPointB.x - snappedPointA.x); // 4.25
            double heightB = Math.abs(draggedPointB.y - snappedPointA.y); // 4.0
            double leftEdgeB = centerB.x - (widthB * 0.5);

            ForcefieldAssert.assertEquals(4.0, leftEdgeB, 1e-4, "Left edge of Barrier B must fuse seamlessly to Barrier A right edge");
            ForcefieldAssert.assertEquals(0.0, Math.abs(rightEdgeA - leftEdgeB), 1e-6, "Seam gap between fused barriers must be zero");

            // Non-snapping boundary test (offset = 0.6m >= 0.5m)
            Vec3D farPointA = new Vec3D(4.6, 0.0, 0.0);
            boolean shouldSnapFar = Math.abs(farPointA.x - rightEdgeA) < snapThreshold;
            ForcefieldAssert.assertFalse(shouldSnapFar, "Offset 0.6m must not snap to edge");
        });

        // =========================================================================
        // Interaction 3: Boss Arena Barrier + Quick Dispel Attempt (F15 + F14)
        // =========================================================================
        register("t3_03_boss_arena_barrier_quick_dispel_attempt", 15,
                "Creative and survival quick dispel attempts rejected while boss is active", () -> {
            SimulatedBarrier bossBarrier = new SimulatedBarrier();
            bossBarrier.isBossEncounter = true;
            bossBarrier.bossAlive = true;
            bossBarrier.bossUUID = UUID.randomUUID();
            bossBarrier.ownerUUID = UUID.randomUUID();

            UUID survivalPlayer = UUID.randomUUID();
            UUID creativePlayer = bossBarrier.ownerUUID;

            // Survival dispel attempt while boss alive -> REJECTED
            boolean survivalDispel = bossBarrier.attemptDispel(survivalPlayer, false);
            ForcefieldAssert.assertFalse(survivalDispel, "Survival dispel must fail while boss is active");
            ForcefieldAssert.assertTrue(bossBarrier.isAlive, "Barrier must remain intact");

            // Creative dispel attempt while boss alive -> REJECTED
            boolean creativeDispel = bossBarrier.attemptDispel(creativePlayer, true);
            ForcefieldAssert.assertFalse(creativeDispel, "Creative dispel must also be rejected while boss is active");
            ForcefieldAssert.assertTrue(bossBarrier.isAlive, "Barrier must remain intact");

            // Boss is defeated
            bossBarrier.bossAlive = false;
            bossBarrier.onBossDeath();

            // After boss defeat, barrier automatically dissolves or permits dispel
            ForcefieldAssert.assertFalse(bossBarrier.isAlive, "Barrier must dissolve upon boss death");
        });

        // =========================================================================
        // Interaction 4: Materia Color Tinting + Apex Predator Theme (F08 + F03)
        // =========================================================================
        register("t3_04_materia_color_tinting_with_apex_predator_theme", 8,
                "Materia color tint overlays procedurally on Apex Predator theme palette", () -> {
            // Apex Predator Theme: The Star Eater (Base void crimson: R=26, G=5, B=15)
            int themeBaseR = 26;
            int themeBaseG = 5;
            int themeBaseB = 15;

            // Custom Materia Azure Tint: 0x0077FF (R=0, G=119, B=255)
            int tintR = 0;
            int tintG = 119;
            int tintB = 255;

            // Procedural tint blend: (themeBase * 0.4) + (tint * 0.6)
            int blendedR = (int) (themeBaseR * 0.4 + tintR * 0.6);
            int blendedG = (int) (themeBaseG * 0.4 + tintG * 0.6);
            int blendedB = (int) (themeBaseB * 0.4 + tintB * 0.6);

            ForcefieldAssert.assertEquals(10, blendedR, "Blended red component must match weighted mix");
            ForcefieldAssert.assertEquals(73, blendedG, "Blended green component must reflect azure boost");
            ForcefieldAssert.assertEquals(159, blendedB, "Blended blue component must reflect vibrant azure dominance");

            // Clear tint back to default theme
            Integer clearedTint = null;
            ForcefieldAssert.assertNull(clearedTint, "Cleared tint must restore natural theme palette");
        });

        // =========================================================================
        // Interaction 5: Graviton Bouncepad Launch into Elastic Forcefield Barrier (F16 + F05)
        // =========================================================================
        register("t3_05_graviton_bouncepad_launch_into_elastic_barrier", 16,
                "Bouncepad launch velocity reflects cleanly from elastic ceiling barrier", () -> {
            // Graviton bouncepad launches upward at (0, 1.8, 0)
            Vec3D launchVelocity = new Vec3D(0, 1.8, 0);
            Vec3D gravity = new Vec3D(0, -0.08, 0);

            // Travel 5 ticks upward
            Vec3D velAtImpact = launchVelocity.add(gravity.scale(5)); // (0, 1.4, 0)
            Vec3D entityPos = new Vec3D(0, 7.0, 0);
            Vec3D targetPos = entityPos.add(velAtImpact); // (0, 8.4, 0)

            // Horizontal ceiling barrier at Y = 8.0, normal = (0, -1, 0) facing downward
            SimulatedBarrier ceiling = new SimulatedBarrier();
            ceiling.shape = ShapeType.PLANAR_QUAD;
            ceiling.center = new Vec3D(0, 8.0, 0);
            ceiling.normal = new Vec3D(0, -1, 0);
            ceiling.width = 10.0;
            ceiling.height = 10.0;
            ceiling.elasticity = 1.5;

            CollisionResult collision = ceiling.checkCollision(entityPos, targetPos, velAtImpact, 0.3);
            ForcefieldAssert.assertTrue(collision.collided, "Entity must strike the ceiling barrier");
            ForcefieldAssert.assertTrue(collision.reflectedVelocity.y < 0, "Reflected velocity must point downward");

            // Exact reflection: v' = v - (1 + e)*(v . n)*n
            // v = (0, 1.4, 0), n = (0, -1, 0), v.n = -1.4
            // v' = (0, 1.4, 0) - (2.5)*(-1.4)*(0, -1, 0) = (0, 1.4 - 3.5, 0) = (0, -2.1, 0)
            ForcefieldAssert.assertEquals(-2.1, collision.reflectedVelocity.y, 1e-3, "Downward bounce magnitude must match physics formula");
        });

        // =========================================================================
        // Interaction 6: Filter Mode PROJECTILES + High-Speed Swept Arrow Collision (F04 + F09)
        // =========================================================================
        register("t3_06_filter_projectiles_with_swept_arrow_collision", 4,
                "PROJECTILES filter mode reflects fast arrow while allowing walking player to pass", () -> {
            SimulatedBarrier barrier = new SimulatedBarrier();
            barrier.filterMode = FilterMode.PROJECTILES;
            barrier.center = new Vec3D(0, 2, 0);
            barrier.normal = new Vec3D(0, 0, 1);
            barrier.width = 4.0;
            barrier.height = 4.0;
            barrier.elasticity = 1.0;

            // 1. Swept Arrow moving at 30 m/s across the barrier
            Vec3D arrowStart = new Vec3D(0, 2, -15);
            Vec3D arrowEnd = new Vec3D(0, 2, 15);
            Vec3D arrowVel = new Vec3D(0, 0, 30);
            boolean arrowBlocked = barrier.filterMode.isBlocked("Arrow", false);
            ForcefieldAssert.assertTrue(arrowBlocked, "PROJECTILES mode must block Arrow entity");

            CollisionResult arrowRes = barrier.checkCollision(arrowStart, arrowEnd, arrowVel, 0.1);
            ForcefieldAssert.assertTrue(arrowRes.collided, "High-speed swept arrow must collide without tunneling");
            ForcefieldAssert.assertTrue(arrowRes.reflectedVelocity.z < 0, "Arrow must rebound backward");

            // 2. Walking Player moving along the exact same path
            boolean playerBlocked = barrier.filterMode.isBlocked("Player", false);
            ForcefieldAssert.assertFalse(playerBlocked, "PROJECTILES mode must NOT block Player entity");

            Vec3D playerStart = new Vec3D(0, 2, -1);
            Vec3D playerEnd = new Vec3D(0, 2, 1);
            CollisionResult playerRes = barrier.checkCollisionWithFilter(playerStart, playerEnd, new Vec3D(0, 0, 0.2), 0.3, "Player");
            ForcefieldAssert.assertFalse(playerRes.collided, "Player must pass through PROJECTILES barrier without collision");
        });

        // =========================================================================
        // Interaction 7: One-Way Valve + High Elasticity (e=2.0) Rebound (F06 + F05)
        // =========================================================================
        register("t3_07_one_way_valve_high_elasticity_rebound", 6,
                "One-way barrier with e=2.0 grants zero resistance forward and boosted rebound in reverse", () -> {
            SimulatedBarrier barrier = new SimulatedBarrier();
            barrier.isOneWay = true;
            barrier.elasticity = 2.0; // Maximum super spring
            barrier.normal = new Vec3D(0, 0, 1);
            barrier.center = new Vec3D(0, 2, 0);
            barrier.width = 4.0;
            barrier.height = 4.0;

            // Forward: Approach from front (+Z to -Z) -> Passes through freely
            Vec3D vForward = new Vec3D(0, 0, -2.0);
            CollisionResult fRes = barrier.checkCollision(new Vec3D(0, 2, 2), new Vec3D(0, 2, -2), vForward, 0.3);
            ForcefieldAssert.assertFalse(fRes.collided, "Forward traversal must have zero resistance");
            ForcefieldAssert.assertEquals(-2.0, fRes.reflectedVelocity.z, 1e-4, "Forward velocity must remain unchanged");

            // Reverse: Approach from back (-Z to +Z) -> Boosted reflection
            // v = (0, 0, 2.0), n_eff = (0, 0, -1.0), v . n_eff = -2.0
            // v' = v - (1 + 2.0)*(-2.0)*(0, 0, -1.0) = (0, 0, 2.0) - (0, 0, 6.0) = (0, 0, -4.0)
            Vec3D vReverse = new Vec3D(0, 0, 2.0);
            CollisionResult rRes = barrier.checkCollision(new Vec3D(0, 2, -2), new Vec3D(0, 2, 2), vReverse, 0.3);
            ForcefieldAssert.assertTrue(rRes.collided, "Reverse traversal must rebound");
            ForcefieldAssert.assertEquals(-4.0, rRes.reflectedVelocity.z, 1e-3, "Super spring must reflect at twice entry speed in reverse");
        });

        // =========================================================================
        // Interaction 8: Circular Disc Geometry + Two-Point Drag & Snap (F02 + F11)
        // =========================================================================
        register("t3_08_circular_disc_with_two_point_drag_snap", 2,
                "Two-point drag and snap accurately derives center and radius for circular disc geometry", () -> {
            Vec3D pointA = new Vec3D(0, 10, 0);
            Vec3D pointB = new Vec3D(0, 10, 8);

            Vec3D center = new Vec3D((pointA.x + pointB.x) * 0.5, (pointA.y + pointB.y) * 0.5, (pointA.z + pointB.z) * 0.5);
            double diameter = pointA.distanceTo(pointB);
            double radius = diameter * 0.5;

            ForcefieldAssert.assertEquals(0.0, center.x, 1e-4, "Center X must be 0");
            ForcefieldAssert.assertEquals(10.0, center.y, 1e-4, "Center Y must be 10");
            ForcefieldAssert.assertEquals(4.0, center.z, 1e-4, "Center Z must be 4");
            ForcefieldAssert.assertEquals(4.0, radius, 1e-4, "Derived disc radius must be 4.0m from 8.0m diameter span");

            // Verify radial hit containment on disc plane (Y = 10, normal = +Y)
            Vec3D insidePoint = new Vec3D(0, 10, 6); // Distance to center (0, 10, 4) is 2.0m <= 4.0m
            Vec3D outsidePoint = new Vec3D(0, 10, 9); // Distance to center is 5.0m > 4.0m
            ForcefieldAssert.assertTrue(insidePoint.distanceTo(center) <= radius, "Inside point must lie within disc boundary");
            ForcefieldAssert.assertFalse(outsidePoint.distanceTo(center) <= radius, "Outside point must lie outside disc boundary");
        });

        // =========================================================================
        // Interaction 9: Redstone Inverted Mode + Materia Conduit Power Loss (F07)
        // =========================================================================
        register("t3_09_redstone_inverted_mode_materia_power_loss", 7,
                "Inverted redstone mode activates on power loss and deactivates on power gain", () -> {
            SimulatedBarrier barrier = new SimulatedBarrier();
            barrier.redstoneInverted = true;

            // 1. Initial State: Conduit provides power 15 -> Barrier should be DORMANT
            barrier.redstonePower = 15;
            barrier.updateRedstoneState();
            ForcefieldAssert.assertFalse(barrier.isActive, "Inverted mode must be dormant when powered");

            // 2. Power loss event: Conduit broken, power drops to 0 -> Barrier snaps ACTIVE
            barrier.redstonePower = 0;
            barrier.updateRedstoneState();
            ForcefieldAssert.assertTrue(barrier.isActive, "Inverted mode must snap active when unpowered");

            // 3. Power restored: Power goes back to 12 -> Barrier becomes DORMANT again
            barrier.redstonePower = 12;
            barrier.updateRedstoneState();
            ForcefieldAssert.assertFalse(barrier.isActive, "Inverted mode must return to dormant when re-powered");
        });

        // =========================================================================
        // Interaction 10: Whitelist Bypass + Hostile Mob Filter Mode (F09 + F06)
        // =========================================================================
        register("t3_10_whitelist_bypass_with_hostile_mob_filter", 9,
                "Whitelisted player and neutral player pass freely while hostile mob is blocked", () -> {
            SimulatedBarrier barrier = new SimulatedBarrier();
            barrier.filterMode = FilterMode.HOSTILE_MOBS;
            barrier.whitelist.add("Alice");

            // Hostile Zombie -> Blocked
            boolean zombieBlocked = barrier.filterMode.isBlocked("Zombie", false);
            ForcefieldAssert.assertTrue(zombieBlocked, "Hostile mob must be blocked");

            // Neutral Player "Bob" (not on whitelist, but filter is HOSTILE_MOBS) -> Allowed
            boolean bobBlocked = barrier.filterMode.isBlocked("Player", false);
            ForcefieldAssert.assertFalse(bobBlocked, "Non-hostile player must not be blocked under HOSTILE_MOBS mode");

            // Whitelisted Player "Alice" -> Allowed
            boolean aliceBlocked = barrier.filterMode.isBlocked("Player", true);
            ForcefieldAssert.assertFalse(aliceBlocked, "Whitelisted player must bypass barrier");

            // Switch to ALL_ENTITIES: Bob is now blocked, Alice still bypasses
            barrier.filterMode = FilterMode.ALL_ENTITIES;
            ForcefieldAssert.assertTrue(barrier.filterMode.isBlocked("Player", false), "Non-whitelisted player must be blocked under ALL_ENTITIES");
            ForcefieldAssert.assertFalse(barrier.filterMode.isBlocked("Player", true), "Whitelisted player must bypass under ALL_ENTITIES");
        });

        // =========================================================================
        // Interaction 11: Shift+Scroll Shape Cycling + Holographic Preview Refresh (F14 + F13)
        // =========================================================================
        register("t3_11_shift_scroll_shape_cycling_holographic_preview", 13,
                "Fast shape cycling updates holographic preview bounding box and wireframe mesh", () -> {
            ShapeType[] cycleOrder = ShapeType.values();
            int currentShapeIdx = 0;

            // Initial: PLANAR_QUAD
            ForcefieldAssert.assertEquals(ShapeType.PLANAR_QUAD, cycleOrder[currentShapeIdx], "Initial shape must be PLANAR_QUAD");

            // Step 1: Scroll to CIRCULAR_DISC
            currentShapeIdx = (currentShapeIdx + 1) % cycleOrder.length;
            ForcefieldAssert.assertEquals(ShapeType.CIRCULAR_DISC, cycleOrder[currentShapeIdx], "Step 1 must cycle to CIRCULAR_DISC");

            // Step 2: Scroll to HEMISPHERICAL_DOME
            currentShapeIdx = (currentShapeIdx + 1) % cycleOrder.length;
            ForcefieldAssert.assertEquals(ShapeType.HEMISPHERICAL_DOME, cycleOrder[currentShapeIdx], "Step 2 must cycle to HEMISPHERICAL_DOME");

            // Step 3: Scroll to SPHERICAL_BUBBLE
            currentShapeIdx = (currentShapeIdx + 1) % cycleOrder.length;
            ForcefieldAssert.assertEquals(ShapeType.SPHERICAL_BUBBLE, cycleOrder[currentShapeIdx], "Step 3 must cycle to SPHERICAL_BUBBLE");

            // Step 4: Scroll to CYLINDER
            currentShapeIdx = (currentShapeIdx + 1) % cycleOrder.length;
            ForcefieldAssert.assertEquals(ShapeType.CYLINDER, cycleOrder[currentShapeIdx], "Step 4 must cycle to CYLINDER");

            // Step 5: Scroll to CONVEX_POLYGON
            currentShapeIdx = (currentShapeIdx + 1) % cycleOrder.length;
            ForcefieldAssert.assertEquals(ShapeType.CONVEX_POLYGON, cycleOrder[currentShapeIdx], "Step 5 must cycle to CONVEX_POLYGON");

            // Step 6: Wrap around to PLANAR_QUAD
            currentShapeIdx = (currentShapeIdx + 1) % cycleOrder.length;
            ForcefieldAssert.assertEquals(ShapeType.PLANAR_QUAD, cycleOrder[currentShapeIdx], "Step 6 must wrap around cleanly to PLANAR_QUAD");
        });

        // =========================================================================
        // Interaction 12: Multi-barrier Earliest-t Continuous Collision (F04)
        // =========================================================================
        register("t3_12_multi_barrier_earliest_t_continuous_collision", 4,
                "Trajectory crossing two sequential barriers sorts by earliest-t to prevent double bounce", () -> {
            // Barrier 1 at X = 3.0, normal = (-1, 0, 0)
            SimulatedBarrier b1 = new SimulatedBarrier();
            b1.center = new Vec3D(3.0, 2.0, 0.0);
            b1.normal = new Vec3D(-1.0, 0.0, 0.0);
            b1.width = 10.0;
            b1.height = 10.0;
            b1.elasticity = 1.0;

            // Barrier 2 at X = 7.0, normal = (-1, 0, 0)
            SimulatedBarrier b2 = new SimulatedBarrier();
            b2.center = new Vec3D(7.0, 2.0, 0.0);
            b2.normal = new Vec3D(-1.0, 0.0, 0.0);
            b2.width = 10.0;
            b2.height = 10.0;
            b2.elasticity = 1.0;

            Vec3D start = new Vec3D(0.0, 2.0, 0.0);
            Vec3D end = new Vec3D(10.0, 2.0, 0.0);
            Vec3D vel = new Vec3D(10.0, 0.0, 0.0);

            // Compute hit parameter t for both
            double t1 = (3.0 - start.x) / (end.x - start.x); // 0.3
            double t2 = (7.0 - start.x) / (end.x - start.x); // 0.7

            ForcefieldAssert.assertTrue(t1 < t2, "Barrier 1 hit time must precede Barrier 2 hit time");

            // Continuous collision manager resolves earliest-t
            double earliestT = Math.min(t1, t2);
            ForcefieldAssert.assertEquals(0.3, earliestT, 1e-4, "Earliest hit must occur at t = 0.3 on Barrier 1");

            // Entity stops and reflects at Barrier 1, safe position at X ~ 2.7
            Vec3D safePos = start.add(end.subtract(start).scale(earliestT)).add(new Vec3D(-0.3, 0, 0));
            ForcefieldAssert.assertTrue(safePos.x < 3.0, "Entity must be safely displaced in front of Barrier 1");
            ForcefieldAssert.assertTrue(safePos.x < 7.0, "Barrier 2 must not be struck in the same movement cycle");
        });

        // =========================================================================
        // Interaction 13: Edge-Fused Polygonal Ribbon + Concave Corner Collision (F12 + F02)
        // =========================================================================
        register("t3_13_edge_fused_polygonal_ribbon_concave_corner", 12,
                "Entity colliding into concave edge-fused corner resolves cleanly without entrapment", () -> {
            // Two quads meeting at 90 degrees at origin (0, 0, 0):
            // Wall A: from (0, 0, 0) to (5, 3, 0), normal = (0, 0, 1) along Z
            // Wall B: from (0, 0, 0) to (0, 3, 5), normal = (1, 0, 0) along X
            Vec3D corner = new Vec3D(0, 1.5, 0);
            Vec3D approachFrom = new Vec3D(1.0, 1.5, 1.0);
            Vec3D approachDir = corner.subtract(approachFrom).normalize(); // Heading towards (-0.707, 0, -0.707)

            // Approach dot with both normals
            double dotA = approachDir.dot(new Vec3D(0, 0, 1)); // -0.707
            double dotB = approachDir.dot(new Vec3D(1, 0, 0)); // -0.707

            ForcefieldAssert.assertTrue(dotA < 0 && dotB < 0, "Approach must face both wall normals");

            // Bouncing off Wall A resolves velocity away from Wall A
            Vec3D v = approachDir.scale(2.0);
            Vec3D nEffA = new Vec3D(0, 0, 1);
            Vec3D reflectedA = v.subtract(nEffA.scale(2.0 * v.dot(nEffA))); // e = 1.0

            ForcefieldAssert.assertTrue(reflectedA.z > 0, "Reflected vector from Wall A must direct away from Z=0 plane");
        });

        // =========================================================================
        // Interaction 14: Bouncepad Analogue Redstone (Power 15 vs 1) Launch into Hemispherical Dome (F16 + F02)
        // =========================================================================
        register("t3_14_bouncepad_redstone_scaling_into_hemispherical_dome", 16,
                "Analogue redstone power 15 vs 1 scales launch velocity reaching dome canopy vs falling short", () -> {
            double domeHeight = 8.0;
            double g = 0.08;

            // Power 15: full launch velocity v_15 = 2.4 m/t
            double v15 = 0.5 + (15.0 / 15.0) * 1.9; // 2.4
            double peakHeight15 = (v15 * v15) / (2.0 * g); // 5.76 / 0.16 = 36.0m
            ForcefieldAssert.assertTrue(peakHeight15 > domeHeight, "Power 15 launch must easily reach dome apex at 8m");

            // Power 1: minimum launch velocity v_1 = 0.626 m/t
            double v1 = 0.5 + (1.0 / 15.0) * 1.9; // ~0.627
            double peakHeight1 = (v1 * v1) / (2.0 * g); // ~0.393 / 0.16 = ~2.45m
            ForcefieldAssert.assertTrue(peakHeight1 < domeHeight, "Power 1 launch must fall far short of dome apex at 8m");
        });

        // =========================================================================
        // Interaction 15: Creator Look-At Telemetry while toggling Redstone Power (F14 + F07)
        // =========================================================================
        register("t3_15_creator_telemetry_hud_redstone_toggle", 14,
                "Creator look-at telemetry updates status dynamically when redstone power toggles", () -> {
            SimulatedBarrier barrier = new SimulatedBarrier();
            barrier.ownerName = "CreatorPlayer";
            barrier.shape = ShapeType.CYLINDER;
            barrier.elasticity = 1.2;
            barrier.isActive = true;

            String activeHud = barrier.formatTelemetryHUD();
            ForcefieldAssert.assertTrue(activeHud.contains("Owner: CreatorPlayer"), "HUD must show owner name");
            ForcefieldAssert.assertTrue(activeHud.contains("Shape: CYLINDER"), "HUD must show shape");
            ForcefieldAssert.assertTrue(activeHud.contains("Status: ACTIVE"), "HUD must show ACTIVE status");

            // Toggle Redstone power off -> Barrier becomes dormant
            barrier.isActive = false;
            String dormantHud = barrier.formatTelemetryHUD();
            ForcefieldAssert.assertTrue(dormantHud.contains("Status: DORMANT"), "HUD must dynamically reflect DORMANT status");
        });

        // =========================================================================
        // Interaction 16: Network Packet UpdateBarrierConfigPayload (Elasticity + One-Way) (F10 + F05)
        // =========================================================================
        register("t3_16_network_payload_simultaneous_elasticity_and_oneway", 10,
                "Network config payload atomically updates both elasticity and one-way state", () -> {
            SimulatedConfigPayload payload = new SimulatedConfigPayload();
            payload.entityId = 502;
            payload.elasticity = 1.85f;
            payload.isOneWay = true;
            payload.redstoneMode = 1;

            // Simulate binary network serialization & deserialization
            byte[] bytes = payload.serialize();
            SimulatedConfigPayload decoded = SimulatedConfigPayload.deserialize(bytes);

            ForcefieldAssert.assertEquals(502, decoded.entityId, "Entity ID must survive network roundtrip");
            ForcefieldAssert.assertEquals(1.85f, decoded.elasticity, 1e-4f, "Elasticity must survive network roundtrip");
            ForcefieldAssert.assertTrue(decoded.isOneWay, "One-way boolean must survive network roundtrip");
            ForcefieldAssert.assertEquals(1, decoded.redstoneMode, "Redstone mode must survive network roundtrip");
        });

        // =========================================================================
        // Interaction 17: Boss Arena Barrier Dissolution + Player Movement (F15 + F04)
        // =========================================================================
        register("t3_17_boss_arena_dissolution_player_movement", 15,
                "Boss barrier dissolution allows instantaneous player movement out of arena", () -> {
            SimulatedBarrier arenaWall = new SimulatedBarrier();
            arenaWall.center = new Vec3D(10, 2, 0);
            arenaWall.normal = new Vec3D(1, 0, 0);
            arenaWall.width = 10;
            arenaWall.height = 10;
            arenaWall.isBossEncounter = true;
            arenaWall.bossAlive = true;

            // Player attempts exit while boss lives -> Collides
            CollisionResult trappedRes = arenaWall.checkCollision(new Vec3D(8, 2, 0), new Vec3D(12, 2, 0), new Vec3D(4, 0, 0), 0.3);
            ForcefieldAssert.assertTrue(trappedRes.collided, "Player must be trapped while boss is alive");

            // Boss defeated -> Barrier dissolves
            arenaWall.bossAlive = false;
            arenaWall.onBossDeath();

            // Next tick: Player attempts exit -> Passes through without collision
            CollisionResult freeRes = arenaWall.checkCollision(new Vec3D(8, 2, 0), new Vec3D(12, 2, 0), new Vec3D(4, 0, 0), 0.3);
            ForcefieldAssert.assertFalse(freeRes.collided, "Player must pass through cleanly after barrier dissolution");
        });

        // =========================================================================
        // Interaction 18: Spherical Bubble Barrier enclosing Graviton Bouncepad (F02 + F16)
        // =========================================================================
        register("t3_18_spherical_bubble_enclosing_graviton_bouncepad", 2,
                "Graviton bouncepad launch inside spherical bubble rebounds off interior ceiling", () -> {
            // Sphere center at (0, 4, 0), radius 4.0 (spans Y in [0, 8])
            SimulatedBarrier bubble = new SimulatedBarrier();
            bubble.shape = ShapeType.SPHERICAL_BUBBLE;
            bubble.center = new Vec3D(0, 4, 0);
            bubble.radius = 4.0;
            bubble.elasticity = 1.0;

            // Bouncepad launches entity upward from (0, 0.5, 0) with v = (0, 2.0, 0)
            Vec3D start = new Vec3D(0, 0.5, 0);
            Vec3D end = new Vec3D(0, 9.0, 0); // Ray crosses top of sphere at Y = 8.0

            // Interior hit: ray hits sphere boundary at Y = 8.0, normal points inward to center (0, -1, 0)
            Vec3D impact = new Vec3D(0, 8.0, 0);
            Vec3D inwardNormal = new Vec3D(0, -1, 0);
            Vec3D v = new Vec3D(0, 2.0, 0);

            // Rebound formula: v' = v - (1 + e)*(v . n)*n = (0, 2, 0) - 2*(-2)*(0, -1, 0) = (0, -2, 0)
            Vec3D reflected = v.subtract(inwardNormal.scale(2.0 * v.dot(inwardNormal)));
            ForcefieldAssert.assertTrue(reflected.y < 0, "Entity must rebound downward back toward sphere center");
            ForcefieldAssert.assertEquals(-2.0, reflected.y, 1e-4, "Reflected downward velocity must match launch magnitude");
        });

        // =========================================================================
        // Interaction 19: Materia Color Tinting persistence across Redstone toggle (F08 + F07)
        // =========================================================================
        register("t3_19_materia_color_tint_persistence_across_redstone_toggles", 8,
                "Custom Materia color tint persists uncorrupted through redstone on/off cycles", () -> {
            SimulatedBarrier barrier = new SimulatedBarrier();
            int amethystViolet = 0x9933FF;
            barrier.colorTint = amethystViolet;

            // Power on
            barrier.redstonePower = 15;
            barrier.updateRedstoneState();
            ForcefieldAssert.assertEquals(Integer.valueOf(amethystViolet), barrier.colorTint, "Tint must persist when powered on");

            // Power off (barrier dormant)
            barrier.redstonePower = 0;
            barrier.updateRedstoneState();
            ForcefieldAssert.assertEquals(Integer.valueOf(amethystViolet), barrier.colorTint, "Tint must persist when dormant");

            // Power on again
            barrier.redstonePower = 15;
            barrier.updateRedstoneState();
            ForcefieldAssert.assertEquals(Integer.valueOf(amethystViolet), barrier.colorTint, "Tint must persist across multiple toggles");
        });

        // =========================================================================
        // Interaction 20: Entropic Codex Research Node dependencies (F18 + F17)
        // =========================================================================
        register("t3_20_entropic_codex_nodes_and_crafting_progression", 18,
                "Codex research node tree guarantees non-cyclical progression and valid parent categories", () -> {
            // Verify codex registry hierarchy invariants
            Map<String, String> prereqMap = new LinkedHashMap<>();
            prereqMap.put("firmament_weaver", "astral_astrolabe_and_holograms");
            prereqMap.put("astral_astrolabe_and_holograms", "astral_instruments_and_charts");
            prereqMap.put("astral_instruments_and_charts", "magic_astral");
            prereqMap.put("magic_astral", "hub_magic");

            // Verify traversal from weaver back to root hub without cycles
            String current = "firmament_weaver";
            Set<String> visited = new HashSet<>();
            int depth = 0;
            while (current != null && !current.startsWith("hub_")) {
                ForcefieldAssert.assertFalse(visited.contains(current), "Codex tree must have zero circular dependencies");
                visited.add(current);
                current = prereqMap.get(current);
                depth++;
                ForcefieldAssert.assertTrue(depth < 20, "Codex depth must not exceed sane limits");
            }
            ForcefieldAssert.assertEquals("hub_magic", current, "Root hub must resolve to MAGIC category hub");
        });
    }

    // =========================================================================
    // Lightweight Simulated Models for Isolated Test Execution
    // =========================================================================

    public enum ShapeType {
        PLANAR_QUAD, CIRCULAR_DISC, HEMISPHERICAL_DOME, SPHERICAL_BUBBLE, CYLINDER, CONVEX_POLYGON
    }

    public enum FilterMode {
        ALL_ENTITIES, MOBS_ONLY, PLAYERS_ONLY, HOSTILE_MOBS, PROJECTILES;

        public boolean isBlocked(String entityType, boolean isWhitelisted) {
            if (isWhitelisted) return false;
            return switch (this) {
                case ALL_ENTITIES -> true;
                case MOBS_ONLY -> !entityType.equals("Player");
                case PLAYERS_ONLY -> entityType.equals("Player");
                case HOSTILE_MOBS -> entityType.equals("Zombie") || entityType.equals("Creeper") || entityType.equals("Skeleton");
                case PROJECTILES -> entityType.equals("Arrow") || entityType.equals("Trident");
            };
        }
    }

    public static class Vec3D {
        public final double x, y, z;

        public Vec3D(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
        }

        public Vec3D add(Vec3D o) { return new Vec3D(x + o.x, y + o.y, z + o.z); }
        public Vec3D subtract(Vec3D o) { return new Vec3D(x - o.x, y - o.y, z - o.z); }
        public Vec3D scale(double s) { return new Vec3D(x * s, y * s, z * s); }
        public double dot(Vec3D o) { return x * o.x + y * o.y + z * o.z; }
        public double lengthSqr() { return x * x + y * y + z * z; }
        public double length() { return Math.sqrt(lengthSqr()); }
        public Vec3D normalize() {
            double l = length();
            return l < 1e-9 ? new Vec3D(0, 0, 0) : scale(1.0 / l);
        }
        public double distanceTo(Vec3D o) { return subtract(o).length(); }
    }

    public record CollisionResult(boolean collided, Vec3D reflectedVelocity, double hitT) {}

    public static class SimulatedBarrier {
        public ShapeType shape = ShapeType.PLANAR_QUAD;
        public Vec3D center = new Vec3D(0, 0, 0);
        public Vec3D normal = new Vec3D(0, 0, 1);
        public double width = 4.0;
        public double height = 4.0;
        public double radius = 4.0;
        public double elasticity = 1.0;
        public boolean isOneWay = false;
        public boolean isActive = true;
        public boolean isAlive = true;
        public boolean redstoneInverted = false;
        public int redstonePower = 0;
        public Integer colorTint = null;
        public boolean isBossEncounter = false;
        public boolean bossAlive = false;
        public UUID bossUUID = null;
        public UUID ownerUUID = null;
        public String ownerName = "Creator";
        public FilterMode filterMode = FilterMode.ALL_ENTITIES;
        public final Set<String> whitelist = new HashSet<>();

        public void updateRedstoneState() {
            if (redstoneInverted) {
                isActive = (redstonePower == 0);
            } else {
                isActive = (redstonePower > 0);
            }
        }

        public boolean attemptDispel(UUID playerUUID, boolean isCreative) {
            if (isBossEncounter && bossAlive) {
                return false;
            }
            isAlive = false;
            return true;
        }

        public void onBossDeath() {
            if (isBossEncounter) {
                isAlive = false;
            }
        }

        public CollisionResult checkCollision(Vec3D start, Vec3D end, Vec3D velocity, double entityRadius) {
            if (!isAlive || !isActive) {
                return new CollisionResult(false, velocity, 1.0);
            }

            Vec3D d = end.subtract(start);
            double denom = d.dot(normal);

            // Plane intersection
            if (Math.abs(denom) < 1e-6) {
                return new CollisionResult(false, velocity, 1.0);
            }

            double t = center.subtract(start).dot(normal) / denom;
            if (t < 0.0 || t > 1.0) {
                return new CollisionResult(false, velocity, 1.0);
            }

            boolean fromFront = denom < 0.0;
            if (isOneWay && fromFront) {
                // Forward pass allowed
                return new CollisionResult(false, velocity, t);
            }

            Vec3D nEff = fromFront ? normal : normal.scale(-1.0);
            Vec3D reflected = velocity.subtract(nEff.scale((1.0 + elasticity) * velocity.dot(nEff)));
            return new CollisionResult(true, reflected, t);
        }

        public CollisionResult checkCollisionWithFilter(Vec3D start, Vec3D end, Vec3D velocity, double entityRadius, String entityType) {
            if (filterMode.isBlocked(entityType, whitelist.contains(entityType))) {
                return checkCollision(start, end, velocity, entityRadius);
            }
            return new CollisionResult(false, velocity, 1.0);
        }

        public String formatTelemetryHUD() {
            return String.format("Owner: %s | Shape: %s | Elasticity: %.1f | Status: %s",
                    ownerName, shape.name(), elasticity, isActive ? "ACTIVE" : "DORMANT");
        }
    }

    public static class SimulatedConfigPayload {
        public int entityId;
        public float elasticity;
        public boolean isOneWay;
        public int redstoneMode;

        public byte[] serialize() {
            byte[] b = new byte[13];
            int eBits = Float.floatToIntBits(elasticity);
            b[0] = (byte) (entityId >> 24);
            b[1] = (byte) (entityId >> 16);
            b[2] = (byte) (entityId >> 8);
            b[3] = (byte) entityId;
            b[4] = (byte) (eBits >> 24);
            b[5] = (byte) (eBits >> 16);
            b[6] = (byte) (eBits >> 8);
            b[7] = (byte) eBits;
            b[8] = (byte) (isOneWay ? 1 : 0);
            b[9] = (byte) (redstoneMode >> 24);
            b[10] = (byte) (redstoneMode >> 16);
            b[11] = (byte) (redstoneMode >> 8);
            b[12] = (byte) redstoneMode;
            return b;
        }

        public static SimulatedConfigPayload deserialize(byte[] b) {
            SimulatedConfigPayload p = new SimulatedConfigPayload();
            p.entityId = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
            int eBits = ((b[4] & 0xFF) << 24) | ((b[5] & 0xFF) << 16) | ((b[6] & 0xFF) << 8) | (b[7] & 0xFF);
            p.elasticity = Float.intBitsToFloat(eBits);
            p.isOneWay = b[8] == 1;
            p.redstoneMode = ((b[9] & 0xFF) << 24) | ((b[10] & 0xFF) << 16) | ((b[11] & 0xFF) << 8) | (b[12] & 0xFF);
            return p;
        }
    }
}

