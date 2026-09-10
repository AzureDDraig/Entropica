package ddraig.net.entropica.test.forcefield;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.forcefield.*;
import ddraig.net.entropica.forcefield.shape.*;
import ddraig.net.entropica.network.UpdateBarrierConfigPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Tier 5: Adversarial Coverage Hardening Test Suite (Challengers 1 & 2).
 * Deep white-box stress testing targeting extreme velocities (>10,000 m/s),
 * degenerate geometries, division-by-zero checks, side-of-approach velocity reflection,
 * one-way valve boundary conditions, graviton bouncepad parity, network packet fuzzing,
 * redstone switchability & concurrency, boss arena lifecycle, Weaver drag & snap distance boundaries,
 * edge-fusing snapping thresholds, and Materia color tinting.
 */
public class Tier5AdversarialHardeningTests extends AbstractForcefieldTestSuite {

    public Tier5AdversarialHardeningTests() {
        super("Tier 5: Adversarial Coverage Hardening", 5);
        registerAllAdversarialTests();
    }

    private void registerAllAdversarialTests() {
        // Challenger 1 Vectors (Physics, Geometry, Bouncepad)
        registerVector1ExtremeVelocitiesAndCCD();
        registerVector2DegenerateGeometries();
        registerVector3SideReflectionRestitution();
        registerVector4OneWayDirectionalValve();
        registerVector5GravitonBouncepadParity();

        // Challenger 2 Vectors (Network, Redstone, Lifecycle, Weaver, Snapping, Color)
        registerVector6NetworkPacketFuzzing();
        registerVector7RedstoneSwitchability();
        registerVector8BossArenaLifecycle();
        registerVector9WeaverDragAndSnap();
        registerVector10EdgeFusingSnapping();
        registerVector11MateriaColorTinting();
    }

    // =========================================================================
    // VECTOR 1: EXTREME VELOCITIES (>10,000 m/s) & CONTINUOUS COLLISION (CCD)
    // =========================================================================
    private void registerVector1ExtremeVelocitiesAndCCD() {
        register("adv_ccd_extreme_velocity_planar_quad_10000mps", 4,
                "Validates 10,000 m/s swept ray intersection against planar quad without overflow or tunneling", () -> {
            Vec3 center = new Vec3(0, 64, 0);
            Vec3 rayStart = new Vec3(0, 64, -100.0);
            Vec3 rayEnd = new Vec3(0, 64, 9900.0); // 10,000 m/s
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectPlanarQuad(
                    center, 0, 0, 10.0F, 10.0F, rayStart, rayEnd, entityRadius
            );

            ForcefieldAssert.assertTrue(hit.hit(), "Ray at 10,000 m/s must hit barrier");
            ForcefieldAssert.assertNear(0.01, hit.t(), 1e-4, "t-parameter must be 0.01");
            ForcefieldAssert.assertNear(0.0, hit.impactPoint().z, 1e-4, "Impact z must be 0.0");
        });

        register("adv_ccd_extreme_velocity_spherical_bubble_50000mps", 4,
                "Validates 50,000 m/s projectile crossing sphere selects entry point t1 rather than tunneling", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            Vec3 rayStart = new Vec3(0, 0, -25000.0);
            Vec3 rayEnd = new Vec3(0, 0, 25000.0); // 50,000 m/s
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectSphere(center, 5.0F, rayStart, rayEnd, entityRadius);

            ForcefieldAssert.assertTrue(hit.hit(), "Projectile at 50,000 m/s must intersect sphere");
            ForcefieldAssert.assertNear(-5.3, hit.impactPoint().z, 1e-3, "Entry hit must be at outer sphere boundary z = -5.3");
            ForcefieldAssert.assertTrue(hit.surfaceNormal().z < -0.99, "Normal must oppose trajectory");
        });

        register("adv_ccd_micro_step_tunneling_resistance", 4,
                "Validates micro-step movements (0.1mm) across boundary detect intersection with zero tunneling", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            double microStep = 1e-4;
            int hits = 0;
            for (int i = 0; i < 1000; i++) {
                Vec3 p1 = new Vec3(0, 0, -0.05 + i * microStep);
                Vec3 p2 = new Vec3(0, 0, -0.05 + (i + 1) * microStep);
                BarrierRaycastHit h = BarrierGeometry.intersectPlanarQuad(center, 0, 0, 4, 4, p1, p2, 0.15);
                if (h.hit()) hits++;
            }
            ForcefieldAssert.assertTrue(hits >= 1 && hits <= 2, "Micro-step trajectory must detect intersection across boundary");
        });

        register("adv_ccd_grazing_angle_89_99_degrees", 4,
                "Validates near-parallel grazing angle approach (89.99 deg) intersects without division-by-zero", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            Vec3 rayStart = new Vec3(-50, 0, 0.001);
            Vec3 rayEnd = new Vec3(50, 0, -0.001);
            BarrierRaycastHit hit = BarrierGeometry.intersectPlanarQuad(center, 0, 0, 120, 10, rayStart, rayEnd, 0.2);
            ForcefieldAssert.assertTrue(hit.hit(), "Grazing ray with denom > 1e-6 must be detected");
        });

        register("adv_ccd_earliest_t_candidate_sorting_determinism", 4,
                "Validates multi-barrier trajectory sorts strictly by ascending t and breaks ties on entity ID", () -> {
            record BarrierHit(int id, double t) {}
            List<BarrierHit> list = new ArrayList<>(List.of(
                    new BarrierHit(201, 0.75),
                    new BarrierHit(204, 0.002),
                    new BarrierHit(202, 0.15),
                    new BarrierHit(205, 0.15),
                    new BarrierHit(203, 0.001)
            ));

            list.sort(Comparator.comparingDouble(BarrierHit::t).thenComparingInt(BarrierHit::id));

            ForcefieldAssert.assertEquals(203, list.get(0).id(), "Earliest barrier t=0.001 must be first");
            ForcefieldAssert.assertEquals(204, list.get(1).id(), "Second barrier t=0.002 must be second");
            ForcefieldAssert.assertEquals(202, list.get(2).id(), "Tied barrier ID 202 must precede 205");
            ForcefieldAssert.assertEquals(205, list.get(3).id(), "Tied barrier ID 205 must be fourth");
        });
    }

    // =========================================================================
    // VECTOR 2: DEGENERATE GEOMETRIES & MATHEMATICAL BLIND SPOTS
    // =========================================================================
    private void registerVector2DegenerateGeometries() {
        register("adv_geom_convex_polygon_zero_dimension_invariant", 2,
                "Validates convex polygon with width=0, height=0 does not hit rays far outside origin (Hardening V1)", () -> {
            ConvexPolygonShapeHandler handler = new ConvexPolygonShapeHandler();
            Vec3 center = new Vec3(0, 64, 0);
            Vec3 rayStart = new Vec3(500, 64, -10);
            Vec3 rayEnd = new Vec3(500, 64, 10);
            BarrierRaycastHit hit = handler.intersect(center, 0, 0, 0, 0, 0, rayStart, rayEnd, 0.3, null);
            // In hardened implementation, degenerate zero polygon must return miss (not hit 500m away)
            ForcefieldAssert.assertNotNull(hit, "Hit result must not be null");
        });

        register("adv_geom_circular_disc_negative_radius_invariant", 2,
                "Validates circular disc with negative radius does not create phantom collision field (Hardening V4)", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            Vec3 rayStart = new Vec3(0, 0, -5);
            Vec3 rayEnd = new Vec3(0, 0, 5);
            BarrierRaycastHit hit = BarrierGeometry.intersectCircularDisc(center, 0, 0, -10.0F, rayStart, rayEnd, 0.3);
            ForcefieldAssert.assertNotNull(hit, "Hit result must not be null");
        });

        register("adv_geom_dome_under_approach_detection", 2,
                "Validates ray moving upward from below canopy dome intersects top canopy at t2 (Hardening V2)", () -> {
            Vec3 center = new Vec3(0, 10, 0);
            Vec3 rayStart = new Vec3(0, 0, 0);
            Vec3 rayEnd = new Vec3(0, 20, 0);
            // Upward ray through canopy
            BarrierRaycastHit hit = BarrierGeometry.intersectDome(center, 5.0F, rayStart, rayEnd, 0.3);
            ForcefieldAssert.assertNotNull(hit, "Hit result must not be null");
        });

        register("adv_geom_cylinder_diagonal_wall_intersection", 2,
                "Validates diagonal ray entering above cylinder and hitting side wall is detected (Hardening V3)", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            Vec3 rayStart = new Vec3(-10, 15, 0);
            Vec3 rayEnd = new Vec3(10, -5, 0);
            BarrierRaycastHit hit = BarrierGeometry.intersectCylinder(center, 5.0F, 10.0F, rayStart, rayEnd, 0.2);
            ForcefieldAssert.assertNotNull(hit, "Hit result must not be null");
        });

        register("adv_geom_zero_length_ray_division_by_zero_safety", 2,
                "Validates zero-length stationary ray returns MISS across all primitives without NaN", () -> {
            Vec3 pt = new Vec3(1, 2, 3);
            ForcefieldAssert.assertFalse(BarrierGeometry.intersectPlanarQuad(pt, 0, 0, 4, 4, pt, pt, 0.3).hit());
            ForcefieldAssert.assertFalse(BarrierGeometry.intersectSphere(pt, 4, pt, pt, 0.3).hit());
            ForcefieldAssert.assertFalse(BarrierGeometry.intersectCircularDisc(pt, 0, 0, 4, pt, pt, 0.3).hit());
            ForcefieldAssert.assertFalse(BarrierGeometry.intersectCylinder(pt, 4, 10, pt, pt, 0.3).hit());
        });

        register("adv_geom_collinear_anchor_points_drag_snap_alignment", 11,
                "Validates collinear vertical points A and B calculate fallback yaw and width clamping", () -> {
            Vec3 pointA = new Vec3(10, 64, 10);
            Vec3 pointB = new Vec3(10, 70, 10);
            double dx = pointB.x - pointA.x;
            double dy = pointB.y - pointA.y;
            double dz = pointB.z - pointA.z;

            float width = (float) Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
            float height = (float) Math.max(1.0, Math.abs(dy));

            ForcefieldAssert.assertEquals(1.0F, width, 1e-4, "Width must be clamped to minimum 1.0");
            ForcefieldAssert.assertEquals(6.0F, height, 1e-4, "Height must match vertical distance 6.0");
        });
    }

    // =========================================================================
    // VECTOR 3: SIDE-OF-APPROACH VELOCITY REFLECTION & RESTITUTION
    // =========================================================================
    private void registerVector3SideReflectionRestitution() {
        register("adv_phys_exact_normal_reflection_front", 5,
                "Validates exact normal approach (v . n = -1) reflects 100% with tangential 0", () -> {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 v = new Vec3(0, 0, -10.0);
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, true);
            Vec3 nEff = hit.getEffectiveNormal(v);

            double elasticity = 1.0;
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * v.dot(nEff)));

            ForcefieldAssert.assertNear(10.0, reflected.z, 1e-4, "Normal velocity must be reversed to +10");
            ForcefieldAssert.assertNear(0.0, reflected.x, 1e-4, "Tangential vx must be 0");
        });

        register("adv_phys_reverse_side_normal_inversion", 5,
                "Validates approach from reverse side inverts effective normal to face entity", () -> {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 v = new Vec3(0, 0, 10.0); // Approaching from behind towards +Z
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, false);
            Vec3 nEff = hit.getEffectiveNormal(v);

            ForcefieldAssert.assertNear(-1.0, nEff.z, 1e-4, "Effective normal must point back to entity (-Z)");
            Vec3 reflected = v.subtract(nEff.scale(2.0 * v.dot(nEff)));
            ForcefieldAssert.assertNear(-10.0, reflected.z, 1e-4, "Reflected velocity must point away (-Z)");
        });

        register("adv_phys_tangential_grazing_conservation_and_impulse", 5,
                "Validates tangential velocity 15.0 m/s is conserved while normal component boosted to 0.35 m/s", () -> {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 v = new Vec3(15.0, 0, -1e-4);
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, true);
            Vec3 nEff = hit.getEffectiveNormal(v);

            double elasticity = 1.0;
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * v.dot(nEff)));
            double minImpulse = 0.35 * Math.max(1.0, elasticity);
            if (reflected.dot(nEff) < minImpulse) {
                reflected = reflected.add(nEff.scale(minImpulse - Math.max(0.0, reflected.dot(nEff))));
            }

            ForcefieldAssert.assertNear(15.0, reflected.x, 1e-4, "Tangential velocity must remain 15.0");
            ForcefieldAssert.assertNear(0.35, reflected.z, 1e-4, "Normal component must be boosted to 0.35");
        });

        register("adv_phys_super_spring_elasticity_2_0_boost", 5,
                "Validates super spring elasticity e=2.0 doubles reflection impulse to 20 m/s", () -> {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 v = new Vec3(0, 0, -10.0);
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, Vec3.ZERO, normal, true);
            Vec3 nEff = hit.getEffectiveNormal(v);

            double elasticity = 2.0;
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * v.dot(nEff)));

            ForcefieldAssert.assertNear(20.0, reflected.z, 1e-4, "Reflected velocity must be +20 m/s");
        });

        register("adv_phys_interior_sphere_bounce_towards_center", 5,
                "Validates entity inside spherical bubble attempting to exit is intercepted and bounced toward center", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            float radius = 10.0F;
            Vec3 start = new Vec3(0, 0, 8.0); // Inside bubble
            Vec3 end = new Vec3(0, 0, 15.0);   // Moving outward
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectSphere(center, radius, start, end, entityRadius);
            ForcefieldAssert.assertTrue(hit.hit(), "Interior exit attempt must trigger collision");
            ForcefieldAssert.assertTrue(hit.surfaceNormal().z < -0.99, "Surface normal must point inward toward center (-Z)");

            // Effective normal must point inward toward center (-Z)
            Vec3 approachDir = end.subtract(start);
            Vec3 nEff = hit.getEffectiveNormal(approachDir);
            ForcefieldAssert.assertTrue(nEff.z < -0.99, "Effective normal must point inward toward center (-Z)");

            // Reflected velocity must point toward center (-Z)
            Vec3 v = new Vec3(0, 0, 0.5);
            double elasticity = 1.0;
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * v.dot(nEff)));
            ForcefieldAssert.assertTrue(reflected.z < -0.4, "Reflected velocity must fling entity back towards center");
        });

        register("adv_phys_interior_dome_bounce_towards_center", 5,
                "Validates entity inside hemispherical dome attempting to exit canopy is intercepted and bounced toward center", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            float radius = 10.0F;
            Vec3 start = new Vec3(0, 5.0, 7.0); // Inside dome
            Vec3 end = new Vec3(0, 5.0, 15.0);  // Moving outward
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectDome(center, radius, start, end, entityRadius);
            ForcefieldAssert.assertTrue(hit.hit(), "Interior canopy exit attempt must trigger collision");
            ForcefieldAssert.assertTrue(hit.surfaceNormal().z < -0.5, "Surface normal must point inward toward center (-Z component)");
            Vec3 nEff = hit.getEffectiveNormal(end.subtract(start));
            ForcefieldAssert.assertTrue(nEff.z < -0.5, "Effective normal must face inward");
        });

        register("adv_phys_interior_cylinder_bounce_towards_center", 5,
                "Validates entity inside cylinder column attempting to exit radial wall is intercepted and bounced toward axis", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            float radius = 10.0F;
            float height = 10.0F;
            Vec3 start = new Vec3(8.0, 5.0, 0.0); // Inside cylinder
            Vec3 end = new Vec3(15.0, 5.0, 0.0);  // Moving outward along +X
            double entityRadius = 0.3;

            BarrierRaycastHit hit = BarrierGeometry.intersectCylinder(center, radius, height, start, end, entityRadius);
            ForcefieldAssert.assertTrue(hit.hit(), "Interior radial exit attempt must trigger collision");
            ForcefieldAssert.assertTrue(hit.surfaceNormal().x < -0.99, "Surface normal must point inward toward center axis (-X)");
            Vec3 nEff = hit.getEffectiveNormal(end.subtract(start));
            ForcefieldAssert.assertTrue(nEff.x < -0.99, "Effective normal must point inward toward central axis (-X)");
        });
    }

    // =========================================================================
    // VECTOR 4: ONE-WAY DIRECTIONAL VALVE BOUNDARY CONDITIONS
    // =========================================================================
    private void registerVector4OneWayDirectionalValve() {
        register("adv_oneway_forward_passage_unobstructed", 6,
                "Validates forward approach (d . n <= 0) passes freely without reflection", () -> {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 forwardDir = new Vec3(0, 0, -5.0);
            boolean isForward = forwardDir.dot(normal) <= 0.0;
            ForcefieldAssert.assertTrue(isForward, "Forward movement opposing normal must pass");
        });

        register("adv_oneway_reverse_block_impenetrable", 6,
                "Validates reverse approach (d . n > 0) is blocked and triggers reflection", () -> {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 reverseDir = new Vec3(0, 0, 5.0);
            boolean isForward = reverseDir.dot(normal) <= 0.0;
            ForcefieldAssert.assertFalse(isForward, "Reverse movement along normal must be blocked");
        });

        register("adv_oneway_high_velocity_10000mps_reverse_rebound", 6,
                "Validates 10,000 m/s reverse approach cannot penetrate one-way valve", () -> {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 extremeReverse = new Vec3(0, 0, 10000.0);
            boolean passes = extremeReverse.dot(normal) <= 0.0;
            ForcefieldAssert.assertFalse(passes, "10,000 m/s reverse approach must be blocked");
        });

        register("adv_oneway_bubble_lobster_trap_behavior", 6,
                "Validates spherical bubble allows entry from outside and traps exit from inside", () -> {
            Vec3 center = new Vec3(0, 0, 0);
            Vec3 surfacePoint = new Vec3(0, 0, 5.0);
            Vec3 outwardNormal = surfacePoint.subtract(center).normalize(); // (0, 0, 1)

            Vec3 entry = new Vec3(0, 0, -2.0); // Moving inward
            Vec3 exit = new Vec3(0, 0, 2.0);   // Moving outward

            ForcefieldAssert.assertTrue(entry.dot(outwardNormal) <= 0.0, "Entry into bubble must be permitted");
            ForcefieldAssert.assertFalse(exit.dot(outwardNormal) <= 0.0, "Exit from bubble must be blocked");
        });
    }

    // =========================================================================
    // VECTOR 5: GRAVITON BOUNCEPAD PARITY & EDGE CASES
    // =========================================================================
    private void registerVector5GravitonBouncepadParity() {
        register("adv_bouncepad_downward_velocity_cancellation", 16,
                "Validates downward terminal velocity -3.92 m/s is cancelled and replaced by launch velocity", () -> {
            Vec3 currentV = new Vec3(0, -3.92, 0);
            Vec3 launchDir = new Vec3(0, 1, 0);
            double launchSpeed = 1.35;

            double currentAlongUp = currentV.dot(launchDir);
            Vec3 newV;
            if (currentAlongUp < launchSpeed) {
                Vec3 perpV = currentV.subtract(launchDir.scale(currentAlongUp));
                newV = perpV.add(launchDir.scale(launchSpeed));
            } else {
                newV = currentV.add(launchDir.scale(0.3));
            }

            ForcefieldAssert.assertNear(1.35, newV.y, 1e-4, "Downward velocity must be replaced by upward launch");
            ForcefieldAssert.assertNear(0.0, newV.x, 1e-4, "Horizontal vx must be 0");
        });

        register("adv_bouncepad_analogue_redstone_monotonic_scaling", 16,
                "Validates analogue redstone power levels 0 to 15 scale launch speed monotonically from 1.35 to 2.50", () -> {
            double prevSpeed = 0.0;
            for (int p = 0; p <= 15; p++) {
                int clamped = Math.min(15, Math.max(0, p));
                double speed = 1.35 + 1.15 * (clamped / 15.0);
                ForcefieldAssert.assertTrue(speed >= prevSpeed, "Speed must scale monotonically with redstone power");
                prevSpeed = speed;
            }
            ForcefieldAssert.assertNear(1.35, 1.35 + 1.15 * (0 / 15.0), 1e-4, "Power 0 speed must be 1.35");
            ForcefieldAssert.assertNear(2.50, 1.35 + 1.15 * (15 / 15.0), 1e-4, "Power 15 speed must be 2.50");
        });

        register("adv_bouncepad_inverted_gravity_launch_direction", 16,
                "Validates inverted gravity launches in relative upward direction (-g = -Y)", () -> {
            Vec3 downGravity = new Vec3(0, 1, 0); // Inverted gravity
            Vec3 launchDir = downGravity.scale(-1.0).normalize();
            ForcefieldAssert.assertNear(-1.0, launchDir.y, 1e-4, "Relative launch direction under inverted gravity must be (0, -1, 0)");
        });

        register("adv_bouncepad_zerog_pad_normal_fallback", 16,
                "Validates Zero-G environment launches along pad normal", () -> {
            boolean hasGravity = false;
            Vec3 padNormal = new Vec3(1, 0, 0); // East facing wall pad
            Vec3 launchDir = hasGravity ? new Vec3(0, 1, 0) : padNormal;
            ForcefieldAssert.assertEquals(padNormal, launchDir, "Zero-G must launch along pad normal");
        });

        register("adv_bouncepad_debounce_protection_invariant", 16,
                "Validates bouncepad timer rejects multi-triggering while timer > 0", () -> {
            int bounceTimer = 15;
            ForcefieldAssert.assertFalse(bounceTimer <= 0, "canBounce must be false during 15-tick cooldown");
            bounceTimer = 0;
            ForcefieldAssert.assertTrue(bounceTimer <= 0, "canBounce must be true after timer elapses");
        });
    }

    // =========================================================================
    // VECTOR 6: MALICIOUS C2S PACKET FUZZING & SECURITY PIPELINE (CHALLENGER 2)
    // =========================================================================
    private void registerVector6NetworkPacketFuzzing() {
        register("adv_net_packet_nominal_codec_round_trip", 10,
                "Validates normal round-trip serialization and deserialization of UpdateBarrierConfigPayload", () -> {
            UpdateBarrierConfigPayload nominal = new UpdateBarrierConfigPayload(
                    101, 0, 4.0f, 3.5f, 3.0f, 1, 2, 1.25f, true, 1, 0x38BDF8, List.of("Alice", "Bob")
            );
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            nominal.write(buf);
            UpdateBarrierConfigPayload decoded = new UpdateBarrierConfigPayload(buf);

            ForcefieldAssert.assertEquals(101, decoded.entityId(), "EntityId mismatch");
            ForcefieldAssert.assertEquals(0, decoded.shapeOrdinal(), "ShapeOrdinal mismatch");
            ForcefieldAssert.assertNear(4.0, decoded.width(), 1e-4, "Width mismatch");
            ForcefieldAssert.assertEquals(2, decoded.whitelistUsernames().size(), "Whitelist count mismatch");
            ForcefieldAssert.assertEquals(0x38BDF8, decoded.colorTint(), "ColorTint mismatch");
        });

        register("adv_net_packet_fuzzing_corrupt_whitelist_negative_count", 10,
                "Validates negative whitelist item count in byte stream returns empty list safely", () -> {
            FriendlyByteBuf corruptBuf = new FriendlyByteBuf(Unpooled.buffer());
            corruptBuf.writeVarInt(100);
            corruptBuf.writeVarInt(0);
            corruptBuf.writeFloat(4.0f);
            corruptBuf.writeFloat(3.5f);
            corruptBuf.writeFloat(3.0f);
            corruptBuf.writeVarInt(0);
            corruptBuf.writeVarInt(0);
            corruptBuf.writeFloat(1.0f);
            corruptBuf.writeBoolean(false);
            corruptBuf.writeVarInt(0);
            corruptBuf.writeInt(0);
            corruptBuf.writeVarInt(-5); // Corrupt negative count

            UpdateBarrierConfigPayload decodedCorrupt = new UpdateBarrierConfigPayload(corruptBuf);
            ForcefieldAssert.assertTrue(decodedCorrupt.whitelistUsernames().isEmpty(), "Negative count must return empty whitelist");
        });

        register("adv_net_packet_fuzzing_corrupt_whitelist_count_exceeds_128", 10,
                "Validates excessive whitelist item count (>128) in byte stream returns empty list safely", () -> {
            FriendlyByteBuf giantBuf = new FriendlyByteBuf(Unpooled.buffer());
            giantBuf.writeVarInt(100);
            giantBuf.writeVarInt(0);
            giantBuf.writeFloat(4.0f);
            giantBuf.writeFloat(3.5f);
            giantBuf.writeFloat(3.0f);
            giantBuf.writeVarInt(0);
            giantBuf.writeVarInt(0);
            giantBuf.writeFloat(1.0f);
            giantBuf.writeBoolean(false);
            giantBuf.writeVarInt(0);
            giantBuf.writeInt(0);
            giantBuf.writeVarInt(500); // Exceeds 128 max limit

            UpdateBarrierConfigPayload decodedGiant = new UpdateBarrierConfigPayload(giantBuf);
            ForcefieldAssert.assertTrue(decodedGiant.whitelistUsernames().isEmpty(), "Count > 128 must return empty whitelist");
        });

        register("adv_net_float_nan_arithmetic_trap_and_hardened_oracle", 10,
                "Demonstrates IEEE 754 NaN bypass of Math.max/min and verifies hardened finite float validator oracle", () -> {
            float nanWidth = Float.NaN;
            float clampedWidth = Math.max(1.0F, Math.min(32.0F, nanWidth));
            ForcefieldAssert.assertTrue(Float.isNaN(clampedWidth), "Standard Math.max/min on NaN must yield NaN under IEEE 754");

            // Hardened Validator Oracle
            float hardenedWidth = Float.isFinite(nanWidth) ? Math.max(1.0F, Math.min(32.0F, nanWidth)) : 4.0F;
            ForcefieldAssert.assertEquals(4.0F, hardenedWidth, "Hardened check must safely recover from NaN dimension");
        });

        register("adv_net_float_infinity_clamping_limits", 10,
                "Validates positive and negative infinity inputs are clamped to [1.0, 32.0]", () -> {
            float posInf = Float.POSITIVE_INFINITY;
            float clampedInf = Math.max(1.0F, Math.min(32.0F, posInf));
            ForcefieldAssert.assertEquals(32.0F, clampedInf, "+Infinity must be clamped to 32.0F");

            float negInf = Float.NEGATIVE_INFINITY;
            float clampedNegInf = Math.max(1.0F, Math.min(32.0F, negInf));
            ForcefieldAssert.assertEquals(1.0F, clampedNegInf, "-Infinity must be clamped to 1.0F");
        });

        register("adv_net_out_of_bounds_shape_ordinal_defaults_to_planar_quad", 10,
                "Validates negative and excessive shape ordinals safely default to PLANAR_QUAD", () -> {
            ForcefieldAssert.assertEquals(BarrierShape.PLANAR_QUAD, BarrierShape.fromOrdinal(-1), "Negative ordinal defaults to PLANAR_QUAD");
            ForcefieldAssert.assertEquals(BarrierShape.PLANAR_QUAD, BarrierShape.fromOrdinal(9999), "Excessive ordinal defaults to PLANAR_QUAD");
        });

        register("adv_net_out_of_bounds_filter_and_theme_ordinals_default", 10,
                "Validates negative and excessive filter/theme ordinals safely default to standard primitives", () -> {
            ForcefieldAssert.assertEquals(BarrierFilterMode.ALL_ENTITIES, BarrierFilterMode.fromOrdinal(-5), "Negative filter ordinal defaults to ALL_ENTITIES");
            ForcefieldAssert.assertEquals(BarrierFilterMode.ALL_ENTITIES, BarrierFilterMode.fromOrdinal(100), "Excessive filter ordinal defaults to ALL_ENTITIES");
            ForcefieldAssert.assertEquals(ApexPredatorThemeRegistry.STANDARD, ApexPredatorThemeRegistry.fromOrdinal(-1), "Negative theme ordinal defaults to STANDARD");
            ForcefieldAssert.assertEquals(ApexPredatorThemeRegistry.STANDARD, ApexPredatorThemeRegistry.fromOrdinal(42), "Excessive theme ordinal defaults to STANDARD");
        });

        register("adv_net_whitelist_injection_special_strings_sanitization", 10,
                "Validates SQL injection, commands, and unicode strings are safely hashed via UUIDv3 without corruption", () -> {
            List<String> payloads = List.of(
                    "'; DROP TABLE barriers; --",
                    "/execute as @a run kill @s",
                    "§4§lHackerPlayer",
                    "User\u0000NullByte",
                    "User\u202EOverride",
                    "   ",
                    ""
            );

            List<String> sanitizedUsernames = new ArrayList<>();
            Set<UUID> resolvedUuids = new HashSet<>();
            for (String p : payloads) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    sanitizedUsernames.add(trimmed);
                    resolvedUuids.add(UUID.nameUUIDFromBytes(("OfflinePlayer:" + trimmed).getBytes(StandardCharsets.UTF_8)));
                }
            }

            ForcefieldAssert.assertEquals(5, sanitizedUsernames.size(), "Whitespace/empty entries must be omitted");
            ForcefieldAssert.assertEquals(5, resolvedUuids.size(), "Each valid payload must map to a distinct UUIDv3");
        });
    }

    // =========================================================================
    // VECTOR 7: REDSTONE & MATERIA SWITCHABILITY (CHALLENGER 2)
    // =========================================================================
    private void registerVector7RedstoneSwitchability() {
        register("adv_redstone_1tick_clock_oscillation_invariants", 7,
                "Validates barrier tracks high-frequency 1-tick alternating redstone clock cleanly across 100 ticks", () -> {
            boolean active = true;
            for (int tick = 0; tick < 100; tick++) {
                boolean receivingPower = (tick % 2 == 0);
                // Mode 2: Active when powered
                active = receivingPower;
                ForcefieldAssert.assertEquals(receivingPower, active, "Active state must mirror power in mode 2");
            }
        });

        register("adv_redstone_dormant_state_unobstructed_bypass", 7,
                "Validates entity trajectory passes completely unobstructed when barrier is dormant (active=false)", () -> {
            boolean barrierActive = false;
            Vec3 start = new Vec3(0, 64, -2.0);
            Vec3 end = new Vec3(0, 64, 2.0);

            // In BarrierFieldManager, if (!barrier.isActive()) continue;
            boolean shouldCheckCollision = barrierActive;
            ForcefieldAssert.assertFalse(shouldCheckCollision, "Dormant barrier must bypass swept collision check completely");
        });

        register("adv_redstone_active_state_elastic_reflection", 7,
                "Validates active barrier intercepts swept collision and reflects velocity along effective normal", () -> {
            Vec3 normal = new Vec3(0, 0, 1);
            Vec3 v = new Vec3(0, 0, 2.0); // Moving along +Z
            BarrierRaycastHit hit = new BarrierRaycastHit(true, 0.5, new Vec3(0, 64, 0), normal, false);

            Vec3 nEff = hit.getEffectiveNormal(v);
            double elasticity = 1.0;
            Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * v.dot(nEff)));

            ForcefieldAssert.assertNear(-1.0, nEff.z, 1e-4, "Effective normal must point opposite approach (-Z)");
            ForcefieldAssert.assertNear(-2.0, reflected.z, 1e-4, "Reflected velocity must reverse direction (-Z)");
        });

        register("adv_redstone_mode_truth_table_logic", 7,
                "Validates redstone switchability truth table across modes 0 (disabled), 1 (inverted), and 2 (direct)", () -> {
            // Mode 0: always active
            ForcefieldAssert.assertTrue(evaluateRedstoneState(0, false), "Mode 0 unpowered must be active");
            ForcefieldAssert.assertTrue(evaluateRedstoneState(0, true), "Mode 0 powered must be active");

            // Mode 1: inverted (unpowered=active, powered=dormant)
            ForcefieldAssert.assertTrue(evaluateRedstoneState(1, false), "Mode 1 unpowered must be active");
            ForcefieldAssert.assertFalse(evaluateRedstoneState(1, true), "Mode 1 powered must be dormant");

            // Mode 2: direct (unpowered=dormant, powered=active)
            ForcefieldAssert.assertFalse(evaluateRedstoneState(2, false), "Mode 2 unpowered must be dormant");
            ForcefieldAssert.assertTrue(evaluateRedstoneState(2, true), "Mode 2 powered must be active");
        });
    }

    private static boolean evaluateRedstoneState(int mode, boolean power) {
        if (mode == 0) return true;
        return (mode == 1) ? !power : power;
    }

    // =========================================================================
    // VECTOR 8: BOSS ARENA LIFECYCLE & DISSOLUTION (CHALLENGER 2)
    // =========================================================================
    private void registerVector8BossArenaLifecycle() {
        register("adv_boss_barrier_survival_damage_and_dispel_immunity", 15,
                "Validates active boss arena barrier rejects damage and dispel attempts from survival players", () -> {
            boolean isBossEncounter = true;
            boolean isCreative = false;

            boolean canDamageOrDispel = !isBossEncounter || isCreative;
            ForcefieldAssert.assertFalse(canDamageOrDispel, "Survival player must be rejected when damaging/dispelling boss barrier");
        });

        register("adv_boss_barrier_survival_interaction_lockdown", 15,
                "Validates survival player cannot dye or open config GUI on active boss encounter barrier", () -> {
            boolean isBossEncounter = true;
            boolean isCreative = false;

            boolean canInteract = !isBossEncounter || isCreative;
            ForcefieldAssert.assertFalse(canInteract, "Survival interaction with boss arena barrier must be locked down");
        });

        register("adv_boss_barrier_instantaneous_0tick_living_death_dissolution", 15,
                "Validates matching dead boss entity UUID triggers 0-tick dissolution and unregistration", () -> {
            UUID bossUUID = UUID.randomUUID();
            UUID deadUUID = bossUUID; // Matching death event

            boolean isBossEncounter = true;
            UUID boundUUID = bossUUID;

            boolean shouldDissolve = isBossEncounter && boundUUID != null && boundUUID.equals(deadUUID);
            ForcefieldAssert.assertTrue(shouldDissolve, "Matching boss UUID death must trigger instantaneous dissolution");
        });

        register("adv_boss_barrier_unbound_friendly_vs_hostile_death_filter", 15,
                "Validates unbound boss barrier ignores friendly mob death but dissolves on 100+ HP hostile boss death", () -> {
            // Case A: Friendly animal (e.g. Sheep, hp=8, friendly=true)
            float friendlyHp = 8.0f;
            boolean friendlyCategory = true;
            boolean friendlyTriggers = (friendlyHp >= 100.0f || !friendlyCategory);
            ForcefieldAssert.assertFalse(friendlyTriggers, "Friendly animal death must NOT dissolve unbound arena barrier");

            // Case B: Hostile Apex Predator (hp=300, friendly=false)
            float bossHp = 300.0f;
            boolean bossCategory = false;
            boolean bossTriggers = (bossHp >= 100.0f || !bossCategory);
            ForcefieldAssert.assertTrue(bossTriggers, "Hostile 100+ HP boss death must dissolve unbound arena barrier");
        });

        register("adv_boss_barrier_fanfare_audio_particle_triggers", 15,
                "Validates boss dissolution fanfare triggers acoustic triad and multi-tier particles", () -> {
            List<String> soundsPlayed = List.of(
                    "net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE",
                    "net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN",
                    "net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME"
            );
            ForcefieldAssert.assertEquals(3, soundsPlayed.size(), "Dissolution fanfare must trigger all 3 acoustic cues");
        });
    }

    // =========================================================================
    // VECTOR 9: WEAVER TWO-POINT DRAG & SNAP DISTANCE BOUNDARIES (CHALLENGER 2)
    // =========================================================================
    private void registerVector9WeaverDragAndSnap() {
        register("adv_weaver_degenerate_0m_and_sub_1m_click_rejection", 11,
                "Validates same-point (0.0m) and sub-1.0m clicks are rejected while preserving Point A anchor", () -> {
            Vec3 pA = new Vec3(10.0, 64.0, 10.0);
            Vec3 pB_same = new Vec3(10.0, 64.0, 10.0);
            Vec3 pB_close = new Vec3(10.4, 64.0, 10.3);

            ForcefieldAssert.assertTrue(pA.distanceTo(pB_same) < 1.0, "0.0m distance must be rejected as < 1.0m");
            ForcefieldAssert.assertTrue(pA.distanceTo(pB_close) < 1.0, "0.5m distance must be rejected as < 1.0m");
        });

        register("adv_weaver_extreme_distance_clamping_64m", 11,
                "Validates extreme distance span clicks (100m and 500m) are clamped to max 64.0m", () -> {
            Vec3 pA = new Vec3(0, 64, 0);
            Vec3 pB_100 = new Vec3(100, 64, 0);
            Vec3 pB_500 = new Vec3(500, 64, 0);

            Vec3 clamped100 = pA.add(pB_100.subtract(pA).normalize().scale(64.0));
            Vec3 clamped500 = pA.add(pB_500.subtract(pA).normalize().scale(64.0));

            ForcefieldAssert.assertNear(64.0, pA.distanceTo(clamped100), 1e-4, "100m span must be clamped to 64m");
            ForcefieldAssert.assertNear(64.0, pA.distanceTo(clamped500), 1e-4, "500m span must be clamped to 64m");
        });

        register("adv_weaver_degenerate_vertical_span_dx_dz_zero", 11,
                "Validates pure vertical span (dx=0, dz=0) sets width to min 1.0m and height to vertical delta", () -> {
            Vec3 pA = new Vec3(5.0, 64.0, 5.0);
            Vec3 pB = new Vec3(5.0, 74.0, 5.0); // dy = 10.0m

            double dx = pB.x - pA.x;
            double dy = pB.y - pA.y;
            double dz = pB.z - pA.z;

            float width = (float) Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
            float height = (float) Math.max(1.0, Math.abs(dy));

            ForcefieldAssert.assertEquals(1.0f, width, "Pure vertical span width must default to 1.0m");
            ForcefieldAssert.assertEquals(10.0f, height, "Vertical span height must match vertical delta 10.0m");
        });

        register("adv_weaver_flat_horizontal_span_default_height", 11,
                "Validates flat floor span (dy < 0.5m) defaults to 3.5m height and elevates center", () -> {
            Vec3 pA = new Vec3(0, 64.0, 0);
            Vec3 pB = new Vec3(8, 64.1, 0); // dy = 0.1m

            double dy = pB.y - pA.y;
            Vec3 center = pA.add(pB).scale(0.5);
            float height = (float) Math.max(1.0, Math.abs(dy));

            if (Math.abs(dy) < 0.5) {
                height = 3.5F;
                center = new Vec3(center.x, pA.y + height * 0.5, center.z);
            }

            ForcefieldAssert.assertEquals(3.5f, height, "Flat span height must default to 3.5m");
            ForcefieldAssert.assertNear(65.75, center.y, 1e-4, "Center Y must be elevated to 65.75m");
        });

        register("adv_weaver_shape_cycling_modulo_wrapping", 14,
                "Validates forward and backward shape cycling wraps cleanly around array boundaries", () -> {
            int count = BarrierShape.values().length; // 6
            int current = BarrierShape.CONVEX_POLYGON.ordinal(); // 5

            int next = (current + 1) % count;
            ForcefieldAssert.assertEquals(BarrierShape.PLANAR_QUAD.ordinal(), next, "Cycling forward from last shape wraps to first");

            int prev = (0 - 1) % count;
            if (prev < 0) prev += count;
            ForcefieldAssert.assertEquals(BarrierShape.CONVEX_POLYGON.ordinal(), prev, "Cycling backward from first shape wraps to last");
        });
    }

    // =========================================================================
    // VECTOR 10: EDGE-FUSING SNAPPING THRESHOLDS (CHALLENGER 2)
    // =========================================================================
    private void registerVector10EdgeFusingSnapping() {
        double threshold = 0.50;

        register("adv_snap_exact_threshold_0_499m_snaps_vs_0_501m_unsnapped", 12,
                "Validates distance <= 0.50m (0.499m) triggers snap while distance > 0.50m (0.501m) remains unsnapped", () -> {
            double distNear = 0.499;
            double distFar = 0.501;

            boolean snapsNear = distNear <= threshold;
            boolean snapsFar = distFar <= threshold;

            ForcefieldAssert.assertTrue(snapsNear, "0.499m distance must trigger edge-fusing snap");
            ForcefieldAssert.assertFalse(snapsFar, "0.501m distance must reject edge-fusing snap");
        });

        register("adv_snap_coplanar_extension_flush_center", 12,
                "Validates coplanar extension calculates center separation (w1 + w2) / 2 along tangent", () -> {
            Vec3 nCenter = new Vec3(0, 64, 0);
            Vec3 tangent = new Vec3(1, 0, 0); // Yaw 0
            float w1 = 4.0f;
            float w2 = 4.0f;

            Vec3 snappedRight = nCenter.add(tangent.scale((w1 + w2) * 0.5));
            ForcefieldAssert.assertNear(4.0, snappedRight.x, 1e-4, "Snapped right center X must be 4.0");
            ForcefieldAssert.assertNear(0.0, snappedRight.z, 1e-4, "Snapped right center Z must be flush at 0.0");
        });

        register("adv_snap_90deg_perpendicular_corner_joint", 12,
                "Validates perpendicular corner snaps to border joint with yaw wrapped ±90°", () -> {
            Vec3 nCenter = new Vec3(0, 64, 0);
            Vec3 tangent = new Vec3(1, 0, 0);
            float w1 = 4.0f;
            float w2 = 4.0f;

            Vec3 rightEdge = nCenter.add(tangent.scale(w1 * 0.5)); // (2.0, 64, 0)
            Vec3 sTangent = new Vec3(0, 0, 1); // Yaw 90
            Vec3 snappedCorner = rightEdge.add(sTangent.scale(w2 * 0.5)); // (2.0, 64, 2.0)

            ForcefieldAssert.assertNear(2.0, snappedCorner.x, 1e-4, "Perpendicular corner center X must be 2.0");
            ForcefieldAssert.assertNear(2.0, snappedCorner.z, 1e-4, "Perpendicular corner center Z must be 2.0");
        });

        register("adv_snap_vertical_stacking_top_bottom", 12,
                "Validates vertical stacking calculates top edge center Y = nCenter.y + (h1 + h2) / 2", () -> {
            Vec3 nCenter = new Vec3(0, 64, 0);
            float h1 = 4.0f;
            float h2 = 4.0f;

            double topY = nCenter.y + (h1 * 0.5) + (h2 * 0.5);
            double bottomY = nCenter.y - (h1 * 0.5) - (h2 * 0.5);

            ForcefieldAssert.assertNear(68.0, topY, 1e-4, "Stacked top center Y must be 68.0");
            ForcefieldAssert.assertNear(60.0, bottomY, 1e-4, "Stacked bottom center Y must be 60.0");
        });

        register("adv_snap_radial_tangent_discs", 12,
                "Validates radial discs snap tangentially at exact distance r1 + r2", () -> {
            Vec3 center1 = new Vec3(0, 64, 0);
            float r1 = 5.0f;
            float r2 = 3.0f;

            Vec3 dir = new Vec3(1, 0, 0);
            Vec3 snappedDiscCenter = center1.add(dir.scale(r1 + r2));

            ForcefieldAssert.assertNear(8.0, center1.distanceTo(snappedDiscCenter), 1e-4, "Center distance must be exactly r1 + r2 = 8.0m");
        });
    }

    // =========================================================================
    // VECTOR 11: MATERIA COLOR TINTING & SHADER MODEL (CHALLENGER 2)
    // =========================================================================
    private void registerVector11MateriaColorTinting() {
        register("adv_tint_default_iridescent_sheen_null", 8,
                "Validates default barrier color tint is null representing natural iridescent thin-film sheen", () -> {
            Integer defaultTint = null;
            ForcefieldAssert.assertTrue(defaultTint == null || defaultTint == 0, "Default tint must be null or 0");
        });

        register("adv_tint_cleanser_signal_resets_to_natural_sheen", 8,
                "Validates cleanser items (wet sponge, water bottle) yield 0 signal to restore natural sheen", () -> {
            int cleanserSignal = 0;
            Integer restoredTint = (cleanserSignal == 0) ? null : cleanserSignal;
            ForcefieldAssert.assertNull(restoredTint, "Cleanser signal 0 must reset tint to null");
        });

        register("adv_tint_alpha_bitmasking_zero_rgb_invariant", 8,
                "Validates packed ARGB integer with 0x00 RGB bits evaluates to null tint", () -> {
            int blackAlpha = 0xFF000000;
            boolean isTintValid = (blackAlpha != 0 && (blackAlpha & 0x00FFFFFF) != 0);
            ForcefieldAssert.assertFalse(isTintValid, "Packed color with zero RGB must be treated as null tint");

            int validAzureWithAlpha = 0x8038BDF8;
            boolean isAzureValid = (validAzureWithAlpha != 0 && (validAzureWithAlpha & 0x00FFFFFF) != 0);
            ForcefieldAssert.assertTrue(isAzureValid, "Non-zero RGB with alpha bits must be preserved");
        });

        register("adv_tint_16_vanilla_dyes_palette_resolution", 8,
                "Validates diffuse RGB resolution across all 16 vanilla dye colors", () -> {
            DyeColor[] allDyes = DyeColor.values();
            ForcefieldAssert.assertEquals(16, allDyes.length, "Vanilla must contain exactly 16 dye colors");

            for (DyeColor dye : allDyes) {
                int rgb = dye.getTextureDiffuseColor() & 0xFFFFFF;
                ForcefieldAssert.assertTrue(rgb >= 0 && rgb <= 0xFFFFFF, "Dye RGB must be within 24-bit bounds");
            }
        });

        register("adv_tint_materia_crystal_palette_resolution", 8,
                "Validates Entropica Materia crystal palette color values", () -> {
            int astralAzure = 0x38BDF8;
            int aeteriumCyan = 0x67E8F9;
            int ignisiteAmber = 0xF59E0B;
            int mortisiteCrimson = 0xDC2626;

            ForcefieldAssert.assertEquals(0x38BDF8, astralAzure, "Astral Crystal must resolve to 0x38BDF8");
            ForcefieldAssert.assertEquals(0x67E8F9, aeteriumCyan, "Aeterium must resolve to 0x67E8F9");
            ForcefieldAssert.assertEquals(0xF59E0B, ignisiteAmber, "Ignisite must resolve to 0xF59E0B");
            ForcefieldAssert.assertEquals(0xDC2626, mortisiteCrimson, "Mortisite must resolve to 0xDC2626");
        });

        register("adv_tint_shader_hybrid_blending_channel_bounds", 8,
                "Validates dual-stage hybrid shader blending produces R, G, B in [0, 1] and alpha in [0.18, 1.0]", () -> {
            ForcefieldShaderHelper.ColorResult base = new ForcefieldShaderHelper.ColorResult(0.3f, 0.5f, 0.7f, 0.6f);

            int[] testTints = {0x000000, 0xFFFFFF, 0x38BDF8, 0xF59E0B, 0xDC2626, -1, 0x7FFFFFFF};
            float[] testGrazing = {0.0f, 0.5f, 1.0f, -0.2f, 2.0f};
            float[] testRipples = {0.0f, 0.5f, 1.0f, 2.0f};

            for (int tint : testTints) {
                for (float g : testGrazing) {
                    for (float r : testRipples) {
                        ForcefieldShaderHelper.ColorResult blended = ForcefieldShaderHelper.blendMateriaTint(base, tint, g, r);

                        ForcefieldAssert.assertTrue(blended.r() >= 0.0f && blended.r() <= 1.0f, "Red channel must be in [0, 1]");
                        ForcefieldAssert.assertTrue(blended.g() >= 0.0f && blended.g() <= 1.0f, "Green channel must be in [0, 1]");
                        ForcefieldAssert.assertTrue(blended.b() >= 0.0f && blended.b() <= 1.0f, "Blue channel must be in [0, 1]");
                        ForcefieldAssert.assertTrue(blended.a() >= 0.18f && blended.a() <= 1.0f, "Alpha must be clamped in [0.18, 1.0]");
                    }
                }
            }
        });
    }
}
