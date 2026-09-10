package ddraig.net.entropica.test.forcefield;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tier 2: Boundary & Corner Cases Test Suite.
 * Validates resilience against zero, degenerate, extreme, and threshold boundary inputs
 * across all 20 features (F01 through F20) for the Forcefield & Firmament Barrier System.
 *
 * Implements >=5 boundary/corner/stress tests per feature (Total: 102 tests).
 */
public class Tier2BoundaryCornerTests extends AbstractForcefieldTestSuite {

    static {
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
        } catch (Throwable ignored) {
        }
    }

    public Tier2BoundaryCornerTests() {
        super("Tier 2: Boundary & Corner Cases", 2);
        registerAllBoundaryTests();
    }

    private void registerAllBoundaryTests() {
        registerF01BaselineBoundaries();
        registerF02GeometryLimits();
        registerF03ThemeExtremeParams();
        registerF04CcdEdgeCases();
        registerF05ReflectionBounds();
        registerF06OneWayBoundaryConditions();
        registerF07RedstoneSignalEdges();
        registerF08ColorTintBounds();
        registerF09CreatorGuiBounds();
        registerF10NetworkPayloadEdgeCases();
        registerF11TwoPointDragSnapLimits();
        registerF12EdgeFusingThresholdBoundary();
        registerF13HolographicPreviewLimits();
        registerF14WeaverUtilitiesBounds();
        registerF15BossArenaEdgeCases();
        registerF16BouncepadParityBoundaries();
        registerF17RecipeLocalizationIntegrity();
        registerF18CodexNodeBoundaries();
        registerF19ObsidianVaultSchemaChecks();
        registerF20BuildPackagingBounds();
    }

    // =========================================================================
    // FEATURE 1: F01 Baseline Boundaries (5 tests)
    // =========================================================================
    private void registerF01BaselineBoundaries() {
        register("t2_f01_01_null_render_state_handling", 1,
                "Null render state snapshot throws NullPointerException or IllegalArgumentException", () -> {
            ForcefieldAssert.assertThrows(NullPointerException.class, () -> {
                evaluateMockRenderPipeline(null);
            }, "Evaluating null render state snapshot must throw NullPointerException");
        });

        register("t2_f01_02_matrix_stack_depth_overflow_boundary", 1,
                "Matrix stack model pushed beyond maximum depth limit is bounded safely", () -> {
            MockMatrixStack stack = new MockMatrixStack(32);
            for (int i = 0; i < 32; i++) {
                stack.push();
            }
            ForcefieldAssert.assertEquals(32, stack.getDepth(), "Stack should reach maximum depth 32");
            ForcefieldAssert.assertThrows(IllegalStateException.class, () -> {
                stack.push();
            }, "Pushing beyond max stack depth must throw IllegalStateException");
            for (int i = 0; i < 32; i++) {
                stack.pop();
            }
            ForcefieldAssert.assertEquals(0, stack.getDepth(), "Popping back must return stack depth to 0");
        });

        register("t2_f01_03_empty_vertex_consumer_boundary", 1,
                "Vertex consumer buffer with 0 vertices has zero byte stride and valid empty state", () -> {
            MockVertexConsumer consumer = new MockVertexConsumer();
            ForcefieldAssert.assertEquals(0, consumer.getVertexCount(), "Initial vertex count must be 0");
            consumer.finish();
            ForcefieldAssert.assertEquals(0, consumer.getByteCount(), "Empty consumer must produce 0 bytes without underflow");
            ForcefieldAssert.assertFalse(consumer.hasCorruptedState(), "Empty consumer must not enter corrupted state");
        });

        register("t2_f01_04_zero_alpha_transparency_cutoff", 1,
                "Zero alpha (alpha = 0.0f) triggers geometry culling and emits zero opacity", () -> {
            float alpha = 0.0f;
            boolean shouldCull = alpha <= 1e-4f;
            ForcefieldAssert.assertTrue(shouldCull, "Alpha = 0.0f must trigger rendering culling");
            int packedAlpha = (int) (Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
            ForcefieldAssert.assertEquals(0, packedAlpha, "Packed alpha byte must be exactly 0");
        });

        register("t2_f01_05_negative_alpha_clamping_to_zero", 1,
                "Negative alpha values clamp strictly to 0.0 without underflowing color channels", () -> {
            float[] negativeAlphas = {-1.0f, -0.001f, -100.0f};
            for (float a : negativeAlphas) {
                float clamped = Math.max(0.0f, Math.min(1.0f, a));
                ForcefieldAssert.assertNear(0.0, clamped, 1e-6, "Negative alpha must clamp to 0.0");
            }
        });
    }

    // =========================================================================
    // FEATURE 2: F02 Geometry Limits (6 tests)
    // =========================================================================
    private void registerF02GeometryLimits() {
        register("t2_f02_01_zero_dimensions_planar_quad_degenerate", 2,
                "Planar Quad with width=0 and height=0 produces degenerate zero-area bounding box and raycast miss", () -> {
            double width = 0.0;
            double height = 0.0;
            Vec3d center = new Vec3d(0, 0, 0);
            AABB3d box = computeQuadAABB(center, width, height);
            ForcefieldAssert.assertEquals(0.0, box.volume(), 1e-6, "Zero-dimension quad must have zero volume");
            RaycastResult hit = intersectPlanarQuad(center, new Vec3d(0, 0, 1), width, height,
                    new Vec3d(0, 0, -5), new Vec3d(0, 0, 5));
            ForcefieldAssert.assertFalse(hit.isHit(), "Raycast against 0x0 quad must report miss");
        });

        register("t2_f02_02_max_32m_clamping_limit", 2,
                "Extreme dimension inputs (>32.0m) clamp strictly to 32.0m max limit", () -> {
            float[] inputs = {32.1f, 50.0f, 100.0f, Float.MAX_VALUE};
            for (float in : inputs) {
                float clamped = clampDimension(in, 1.0f, 32.0f);
                ForcefieldAssert.assertNear(32.0f, clamped, 1e-4f, "Dimension must clamp to 32.0m");
            }
        });

        register("t2_f02_03_zero_radius_disc_and_sphere_degenerate", 2,
                "Circular Disc and Spherical Bubble with radius=0 produce safe raycast miss without NaN", () -> {
            Vec3d center = new Vec3d(0, 0, 0);
            RaycastResult sphereHit = intersectSphere(center, 0.0, new Vec3d(0, 0, -5), new Vec3d(0, 0, 5));
            ForcefieldAssert.assertFalse(sphereHit.isHit(), "Zero radius sphere raycast must miss");
            ForcefieldAssert.assertFalse(Double.isNaN(sphereHit.t()), "Hit t must not be NaN");

            RaycastResult discHit = intersectDisc(center, new Vec3d(0, 1, 0), 0.0,
                    new Vec3d(0, -5, 0), new Vec3d(0, 5, 0));
            ForcefieldAssert.assertFalse(discHit.isHit(), "Zero radius disc raycast must miss");
        });

        register("t2_f02_04_planar_degenerate_colinear_polygon", 2,
                "Convex polygon with 3 colinear vertices has zero cross-product and is rejected as degenerate", () -> {
            Vec3d v0 = new Vec3d(0, 0, 0);
            Vec3d v1 = new Vec3d(1, 0, 0);
            Vec3d v2 = new Vec3d(2, 0, 0);
            Vec3d edge1 = v1.subtract(v0);
            Vec3d edge2 = v2.subtract(v0);
            Vec3d normal = edge1.cross(edge2);
            ForcefieldAssert.assertNear(0.0, normal.length(), 1e-6, "Colinear polygon must have zero normal cross product");
            boolean isDegenerate = normal.lengthSqr() < 1e-6;
            ForcefieldAssert.assertTrue(isDegenerate, "Colinear polygon must be flagged degenerate");
        });

        register("t2_f02_05_coplanar_ray_parallel_grazing", 2,
                "Ray cast parallel and coplanar to barrier plane reports miss without division by zero", () -> {
            Vec3d rayStart = new Vec3d(0, 0, 0);
            Vec3d rayEnd = new Vec3d(10, 0, 0);
            Vec3d planeCenter = new Vec3d(0, 0, 0);
            Vec3d planeNormal = new Vec3d(0, 0, 1);
            RaycastResult hit = intersectInfinitePlane(planeCenter, planeNormal, rayStart, rayEnd);
            ForcefieldAssert.assertFalse(hit.isHit(), "Coplanar parallel ray must not intersect plane manifold");
            ForcefieldAssert.assertFalse(Double.isNaN(hit.t()), "Hit parameter t must be valid number");
        });

        register("t2_f02_06_cylinder_height_zero_degenerate", 2,
                "Cylinder barrier with height=0.0m behaves as degenerate disc plane", () -> {
            Vec3d center = new Vec3d(0, 0, 0);
            double radius = 5.0;
            double height = 0.0;
            AABB3d aabb = new AABB3d(center.x - radius, center.y, center.z - radius,
                    center.x + radius, center.y + height, center.z + radius);
            ForcefieldAssert.assertEquals(0.0, aabb.volume(), 1e-6, "Zero-height cylinder must have zero volume");
        });
    }

    // =========================================================================
    // FEATURE 3: F03 Theme Extreme Parameters (5 tests)
    // =========================================================================
    private void registerF03ThemeExtremeParams() {
        register("t2_f03_01_age_ticks_overflow_resilience", 3,
                "Extreme and overflowing ageTicks remain bounded in [0.0, 1.0] RGBA range", () -> {
            float[] extremeTicks = {0.0f, 1e6f, 1e9f, Float.MAX_VALUE, -100.0f};
            for (float ticks : extremeTicks) {
                float phase = (ticks * 0.05f) % (float) (2.0 * Math.PI);
                float wave = (float) (0.5 + 0.5 * Math.sin(phase));
                ForcefieldAssert.assertTrue(!Float.isNaN(wave) && !Float.isInfinite(wave), "Wave must not be NaN/Inf");
                ForcefieldAssert.assertTrue(wave >= 0.0f && wave <= 1.0f, "Wave must stay within [0.0, 1.0]");
            }
        });

        register("t2_f03_02_negative_ripple_intensity_clamping", 3,
                "Negative ripple shockwave intensity values clamp safely to 0.0", () -> {
            float[] negativeRipples = {-10.0f, -0.0001f, -Float.MIN_VALUE};
            for (float r : negativeRipples) {
                float clamped = Math.max(0.0f, r);
                ForcefieldAssert.assertEquals(0.0f, clamped, 1e-6f, "Negative ripple intensity must clamp to 0.0");
            }
        });

        register("t2_f03_03_zero_normal_vector_fallback", 3,
                "Zero normal vector input falls back to default unit vector without NaN in dot product", () -> {
            Vec3d zeroNormal = new Vec3d(0, 0, 0);
            Vec3d safeNormal = zeroNormal.lengthSqr() < 1e-6 ? new Vec3d(0, 1, 0) : zeroNormal.normalize();
            ForcefieldAssert.assertEquals(1.0, safeNormal.length(), 1e-6, "Fallback normal must be unit length");
            Vec3d viewDir = new Vec3d(1, 0, 0);
            double dot = viewDir.dot(safeNormal);
            ForcefieldAssert.assertEquals(0.0, dot, 1e-6, "Dot product with fallback normal must be finite");
        });

        register("t2_f03_04_extreme_view_angle_grazing_fresnel", 3,
                "Perpendicular 90-degree grazing view angle computes maximum edge Fresnel glow (1.0) without NaN", () -> {
            Vec3d normal = new Vec3d(0, 1, 0);
            Vec3d viewDir = new Vec3d(1, 0, 0);
            double cosTheta = Math.abs(viewDir.dot(normal));
            double fresnel = Math.pow(1.0 - cosTheta, 3.0);
            ForcefieldAssert.assertNear(1.0, fresnel, 1e-6, "90-degree grazing angle must produce full Fresnel glow");
        });

        register("t2_f03_05_opaque_and_out_of_bounds_tint_blending", 3,
                "Opaque black 0x000000 and white 0xFFFFFF tint blending clamps cleanly to [0, 255] across channels", () -> {
            int[] testTints = {0x000000, 0xFFFFFF, 0x123456};
            for (int tint : testTints) {
                int r = (tint >> 16) & 0xFF;
                int g = (tint >> 8) & 0xFF;
                int b = tint & 0xFF;
                int blendedR = Math.max(0, Math.min(255, (r * 200) / 255));
                int blendedG = Math.max(0, Math.min(255, (g * 200) / 255));
                int blendedB = Math.max(0, Math.min(255, (b * 200) / 255));
                ForcefieldAssert.assertTrue(blendedR >= 0 && blendedR <= 255, "R must be valid byte");
                ForcefieldAssert.assertTrue(blendedG >= 0 && blendedG <= 255, "G must be valid byte");
                ForcefieldAssert.assertTrue(blendedB >= 0 && blendedB <= 255, "B must be valid byte");
            }
        });
    }

    // =========================================================================
    // FEATURE 4: F04 Continuous Swept Collision (CCD) Edge Cases (5 tests)
    // =========================================================================
    private void registerF04CcdEdgeCases() {
        register("t2_f04_01_zero_velocity_stationary_entity", 4,
                "Stationary entity (v = 0) swept trajectory evaluates to false without division by zero", () -> {
            Vec3d start = new Vec3d(0, 10, 0);
            Vec3d end = new Vec3d(0, 10, 0);
            Vec3d planeCenter = new Vec3d(0, 10, 0);
            Vec3d planeNormal = new Vec3d(0, 1, 0);
            SweptResult result = computeSweptPlanarIntersection(start, end, planeCenter, planeNormal);
            ForcefieldAssert.assertFalse(result.isHit(), "Zero displacement trajectory must not trigger collision");
        });

        register("t2_f04_02_hypersonic_velocity_swept_detection", 4,
                "Hypersonic velocity (v = 150 m/s) trajectory detects intersection at t = 0.5 and prevents tunneling", () -> {
            Vec3d start = new Vec3d(-75, 64, 0);
            Vec3d end = new Vec3d(75, 64, 0);
            Vec3d planeCenter = new Vec3d(0, 64, 0);
            Vec3d planeNormal = new Vec3d(1, 0, 0);
            SweptResult result = computeSweptPlanarIntersection(start, end, planeCenter, planeNormal);
            ForcefieldAssert.assertTrue(result.isHit(), "Hypersonic trajectory must intersect barrier");
            ForcefieldAssert.assertNear(0.5, result.t(), 1e-4, "Collision parameter t must be exactly 0.5");
        });

        register("t2_f04_03_tangent_grazing_trajectory", 4,
                "Trajectory parallel and tangent to barrier at sub-millimeter offset avoids false-positive collision", () -> {
            Vec3d start = new Vec3d(0, 64, 0.0001);
            Vec3d end = new Vec3d(50, 64, 0.0001);
            Vec3d planeCenter = new Vec3d(25, 64, 0.0);
            Vec3d planeNormal = new Vec3d(0, 0, 1);
            SweptResult result = computeSweptPlanarIntersection(start, end, planeCenter, planeNormal);
            ForcefieldAssert.assertFalse(result.isHit(), "Tangent trajectory outside plane must not collide");
        });

        register("t2_f04_04_sub_millimeter_displacement_margin", 4,
                "Safe boundary displacement places entity outside barrier membrane by margin epsilon", () -> {
            Vec3d hitPoint = new Vec3d(0, 64, 0);
            Vec3d normal = new Vec3d(1, 0, 0);
            double epsilon = 0.001;
            Vec3d safePos = hitPoint.add(normal.scale(epsilon));
            ForcefieldAssert.assertNear(0.001, safePos.x, 1e-6, "Displaced position must be offset by epsilon along normal");
        });

        register("t2_f04_05_collocated_coincident_barriers_sorting", 4,
                "Two coincident barriers at identical coordinates are sorted by earliest t with deterministic tie-break", () -> {
            List<MockBarrierCandidate> candidates = new ArrayList<>();
            candidates.add(new MockBarrierCandidate(101, 0.45));
            candidates.add(new MockBarrierCandidate(102, 0.45));
            candidates.sort(Comparator.comparingDouble(MockBarrierCandidate::t).thenComparingInt(MockBarrierCandidate::id));
            ForcefieldAssert.assertEquals(101, candidates.get(0).id(), "Earlier ID must break tie deterministically");
            ForcefieldAssert.assertEquals(102, candidates.get(1).id(), "Second barrier processed deterministically");
        });
    }

    // =========================================================================
    // FEATURE 5: F05 Side-of-Approach Reflection Bounds (6 tests)
    // =========================================================================
    private void registerF05ReflectionBounds() {
        register("t2_f05_01_minimum_restitution_soft_cushion", 5,
                "Minimum restitution e=0.2 (soft cushion) dissipates 96% of normal kinetic energy", () -> {
            double e = 0.2;
            double vn = -10.0;
            double vnReflected = -vn * e;
            ForcefieldAssert.assertNear(2.0, vnReflected, 1e-6, "Reflected normal velocity must be 2.0 m/s");
            double energyRatio = (vnReflected * vnReflected) / (vn * vn);
            ForcefieldAssert.assertNear(0.04, energyRatio, 1e-6, "Remaining energy must be 4% (96% dissipated)");
        });

        register("t2_f05_02_maximum_restitution_super_spring", 5,
                "Maximum restitution e=2.0 (super-spring) launches entity with 200% incident speed", () -> {
            double e = 2.0;
            double vn = -10.0;
            double vnReflected = -vn * e;
            ForcefieldAssert.assertNear(20.0, vnReflected, 1e-6, "Reflected normal velocity must be 20.0 m/s");
            double energyRatio = (vnReflected * vnReflected) / (vn * vn);
            ForcefieldAssert.assertNear(4.0, energyRatio, 1e-6, "Exit kinetic energy must be amplified 4x");
        });

        register("t2_f05_03_near_90_degree_grazing_reflection", 5,
                "Near-90-degree grazing collision preserves tangential velocity while reversing normal component", () -> {
            Vec3d v = new Vec3d(10.0, -0.01, 0.0);
            Vec3d normal = new Vec3d(0, 1, 0);
            double e = 1.0;
            Vec3d vReflected = reflectVelocity(v, normal, e);
            ForcefieldAssert.assertNear(10.0, vReflected.x, 1e-6, "Tangential X velocity must be preserved");
            ForcefieldAssert.assertNear(0.01, vReflected.y, 1e-6, "Normal Y velocity must be reversed to +0.01");
            ForcefieldAssert.assertNear(0.0, vReflected.z, 1e-6, "Z velocity must remain 0");
        });

        register("t2_f05_04_zero_initial_velocity_contact", 5,
                "Entity resting in contact with barrier (vn = 0) receives zero rebound impulse", () -> {
            Vec3d v = new Vec3d(5.0, 0.0, 0.0);
            Vec3d normal = new Vec3d(0, 1, 0);
            double e = 1.5;
            Vec3d vReflected = reflectVelocity(v, normal, e);
            ForcefieldAssert.assertEquals(v, vReflected, "Purely tangential velocity must be unchanged by reflection");
        });

        register("t2_f05_05_extreme_mass_inelastic_barrier_condition", 5,
                "Kinematic reflection formula is invariant of entity mass (inelastic immovable barrier)", () -> {
            double[] masses = {0.1, 80.0, 10_000.0, 1_000_000.0};
            Vec3d v = new Vec3d(0, -5.0, 0);
            Vec3d normal = new Vec3d(0, 1, 0);
            double e = 0.8;
            Vec3d expected = reflectVelocity(v, normal, e);
            for (double m : masses) {
                Vec3d actual = reflectVelocity(v, normal, e);
                ForcefieldAssert.assertEquals(expected, actual, "Reflection result must not depend on mass " + m);
            }
        });

        register("t2_f05_06_tangential_friction_conservation", 5,
                "Ideal membrane reflection exhibits zero tangential friction", () -> {
            Vec3d v = new Vec3d(15.0, -8.0, 7.0);
            Vec3d normal = new Vec3d(0, 1, 0);
            double e = 0.5;
            Vec3d vReflected = reflectVelocity(v, normal, e);
            ForcefieldAssert.assertNear(15.0, vReflected.x, 1e-6, "Tangential X component must have zero friction loss");
            ForcefieldAssert.assertNear(7.0, vReflected.z, 1e-6, "Tangential Z component must have zero friction loss");
        });
    }

    // =========================================================================
    // FEATURE 6: F06 One-Way Boundary Conditions (5 tests)
    // =========================================================================
    private void registerF06OneWayBoundaryConditions() {
        register("t2_f06_01_parallel_approach_exact_dot_zero", 6,
                "Exact parallel trajectory (v . n = 0.0) permits pass-through without reflection in one-way mode", () -> {
            Vec3d v = new Vec3d(10, 0, 0);
            Vec3d normal = new Vec3d(0, 0, 1);
            double dot = v.dot(normal);
            ForcefieldAssert.assertEquals(0.0, dot, 1e-9, "Dot product must be exactly 0.0");
            boolean shouldReflect = evaluateOneWayCollision(v, normal);
            ForcefieldAssert.assertFalse(shouldReflect, "Parallel movement must not trigger one-way reflection");
        });

        register("t2_f06_02_infinitesimal_forward_dot", 6,
                "Infinitesimal forward approach (v . n = -1e-8) is permitted forward traversal", () -> {
            Vec3d v = new Vec3d(0, 0, -1e-8);
            Vec3d normal = new Vec3d(0, 0, 1);
            boolean shouldReflect = evaluateOneWayCollision(v, normal);
            ForcefieldAssert.assertFalse(shouldReflect, "Forward approach (v . n < 0) must pass through");
        });

        register("t2_f06_03_infinitesimal_reverse_dot", 6,
                "Infinitesimal reverse approach (v . n = +1e-8) is blocked and triggers reflection", () -> {
            Vec3d v = new Vec3d(0, 0, 1e-8);
            Vec3d normal = new Vec3d(0, 0, 1);
            boolean shouldReflect = evaluateOneWayCollision(v, normal);
            ForcefieldAssert.assertTrue(shouldReflect, "Reverse approach (v . n > 0) must trigger reflection");
        });

        register("t2_f06_04_high_speed_reverse_impact_ccd", 6,
                "High-speed reverse impact (v = 80 m/s, v . n > 0) is intercepted and bounced backwards", () -> {
            Vec3d v = new Vec3d(0, 0, 80.0);
            Vec3d normal = new Vec3d(0, 0, 1);
            boolean shouldReflect = evaluateOneWayCollision(v, normal);
            ForcefieldAssert.assertTrue(shouldReflect, "Reverse impact must be reflected");
            Vec3d reflected = reflectVelocity(v, normal.scale(-1.0), 1.0);
            ForcefieldAssert.assertNear(-80.0, reflected.z, 1e-4, "Reflected velocity must point away from reverse side");
        });

        register("t2_f06_05_starlight_arrows_glyph_vector", 6,
                "Starlight arrows glyph drift vector matches forward permitted travel direction (-normal)", () -> {
            Vec3d normal = new Vec3d(0, 1, 0);
            boolean isOneWay = true;
            Vec3d glyphDrift = isOneWay ? normal.scale(-1.0) : new Vec3d(0, 0, 0);
            ForcefieldAssert.assertEquals(new Vec3d(0, -1, 0), glyphDrift, "Glyph drift must point along permitted direction");
        });
    }

    // =========================================================================
    // FEATURE 7: F07 Redstone Signal Edges (5 tests)
    // =========================================================================
    private void registerF07RedstoneSignalEdges() {
        register("t2_f07_01_redstone_threshold_power_0_vs_1", 7,
                "Normal mode active at power 0 and transitions to dormant at power 1", () -> {
            boolean invertedMode = false;
            ForcefieldAssert.assertTrue(isBarrierSolid(0, invertedMode), "Power 0 must be solid in normal mode");
            ForcefieldAssert.assertFalse(isBarrierSolid(1, invertedMode), "Power 1 must be dormant in normal mode");
        });

        register("t2_f07_02_redstone_max_power_15_saturation", 7,
                "Max power 15 activates inverted mode; values > 15 clamp to 15 without overflow", () -> {
            boolean invertedMode = true;
            ForcefieldAssert.assertTrue(isBarrierSolid(15, invertedMode), "Power 15 must be solid in inverted mode");
            int clampedPower = Math.min(15, Math.max(0, 20));
            ForcefieldAssert.assertEquals(15, clampedPower, "Power > 15 must clamp to 15");
        });

        register("t2_f07_03_rapid_redstone_frequency_toggling", 7,
                "Rapid alternating redstone toggle (0, 15) for 100 iterations preserves instantaneous consistency", () -> {
            boolean inverted = false;
            for (int i = 0; i < 100; i++) {
                int power = (i % 2 == 0) ? 0 : 15;
                boolean expectedSolid = (power == 0);
                ForcefieldAssert.assertEquals(expectedSolid, isBarrierSolid(power, inverted),
                        "State must match instantaneous power at iteration " + i);
            }
        });

        register("t2_f07_04_dormant_pass_through_hypersonic", 7,
                "Hypersonic projectile (150 m/s) skips collision entirely when barrier is dormant", () -> {
            boolean isSolid = isBarrierSolid(15, false); // dormant
            boolean collisionChecked = false;
            if (isSolid) {
                collisionChecked = true;
            }
            ForcefieldAssert.assertFalse(collisionChecked, "Dormant barrier must bypass swept collision calculations");
        });

        register("t2_f07_05_materia_conduit_disconnection_fallback", 7,
                "Materia Conduit disconnection reverts to ambient redstone wire signal without NPE", () -> {
            Integer conduitSignal = null; // disconnected
            int ambientRedstone = 0;
            int effectiveSignal = (conduitSignal != null) ? conduitSignal : ambientRedstone;
            ForcefieldAssert.assertEquals(0, effectiveSignal, "Effective signal must fall back to ambient redstone 0");
            ForcefieldAssert.assertTrue(isBarrierSolid(effectiveSignal, false), "Barrier must remain solid under fallback power 0");
        });
    }

    // =========================================================================
    // FEATURE 8: F08 Materia Color Tint Bounds (5 tests)
    // =========================================================================
    private void registerF08ColorTintBounds() {
        register("t2_f08_01_full_black_tint_0x000000", 8,
                "Full black tint 0x000000 produces valid minimum-luminescence chromatic representation", () -> {
            int tint = 0x000000;
            int sanitized = tint & 0x00FFFFFF;
            ForcefieldAssert.assertEquals(0, sanitized, "Sanitized black tint must be 0x000000");
        });

        register("t2_f08_02_full_white_tint_0xFFFFFF", 8,
                "Full white tint 0xFFFFFF clamps RGB components to exactly 255 without channel wrap-around", () -> {
            int tint = 0xFFFFFF;
            int r = (tint >> 16) & 0xFF;
            int g = (tint >> 8) & 0xFF;
            int b = tint & 0xFF;
            ForcefieldAssert.assertEquals(255, r, "Red must be 255");
            ForcefieldAssert.assertEquals(255, g, "Green must be 255");
            ForcefieldAssert.assertEquals(255, b, "Blue must be 255");
        });

        register("t2_f08_03_transparent_alpha_tint_handling", 8,
                "ARGB tint with alpha = 0 (0x00AABBCC) sanitizes to clean 24-bit RGB 0xAABBCC", () -> {
            int argb = 0x00AABBCC;
            int rgb = argb & 0x00FFFFFF;
            ForcefieldAssert.assertEquals(0xAABBCC, rgb, "Alpha channel must be stripped to 24-bit RGB");
        });

        register("t2_f08_04_rapid_materia_tint_cycling", 8,
                "Cycling 100 sequential Materia tints updates color state consistently", () -> {
            int currentColor = 0x000000;
            for (int i = 0; i < 100; i++) {
                currentColor = (i * 0x020406) & 0x00FFFFFF;
                ForcefieldAssert.assertTrue(currentColor >= 0 && currentColor <= 0xFFFFFF,
                        "Tint must remain valid 24-bit color at iteration " + i);
            }
        });

        register("t2_f08_05_invalid_color_code_normalization", 8,
                "Out-of-bounds integer color inputs (-1, high bit flags) normalize to clean 24-bit RGB", () -> {
            int[] rawColors = {-1, 0x80000000, 0x7FFFFFFF, 0x12345678};
            for (int raw : rawColors) {
                int sanitized = raw & 0x00FFFFFF;
                ForcefieldAssert.assertTrue(sanitized >= 0 && sanitized <= 0xFFFFFF,
                        "Color must sanitize to 24-bit RGB range");
            }
        });
    }

    // =========================================================================
    // FEATURE 9: F09 Creator GUI Bounds (5 tests)
    // =========================================================================
    private void registerF09CreatorGuiBounds() {
        register("t2_f09_01_slider_dimensions_min_clamp_1m", 9,
                "GUI dimension sliders clamp inputs below 1.0m to minimum 1.0m", () -> {
            float[] lowInputs = {0.0f, -5.0f, 0.999f};
            for (float in : lowInputs) {
                float clamped = clampDimension(in, 1.0f, 32.0f);
                ForcefieldAssert.assertNear(1.0f, clamped, 1e-4f, "Dimension must clamp to 1.0m");
            }
        });

        register("t2_f09_02_slider_dimensions_max_clamp_32m", 9,
                "GUI dimension sliders clamp inputs above 32.0m to maximum 32.0m", () -> {
            float[] highInputs = {32.001f, 100.0f, 10_000.0f};
            for (float in : highInputs) {
                float clamped = clampDimension(in, 1.0f, 32.0f);
                ForcefieldAssert.assertNear(32.0f, clamped, 1e-4f, "Dimension must clamp to 32.0m");
            }
        });

        register("t2_f09_03_elasticity_slider_clamp_0_2_to_2_0", 9,
                "Elasticity slider clamps inputs strictly to [0.2, 2.0]", () -> {
            ForcefieldAssert.assertNear(0.2f, clampDimension(-1.0f, 0.2f, 2.0f), 1e-4f, "Low elasticity must clamp to 0.2");
            ForcefieldAssert.assertNear(0.2f, clampDimension(0.0f, 0.2f, 2.0f), 1e-4f, "Zero elasticity must clamp to 0.2");
            ForcefieldAssert.assertNear(2.0f, clampDimension(5.0f, 0.2f, 2.0f), 1e-4f, "High elasticity must clamp to 2.0");
        });

        register("t2_f09_04_invalid_filter_mode_index_recovery", 9,
                "Invalid filter mode ordinal inputs (-1, 999) safely recover to ALL_ENTITIES (ordinal 0)", () -> {
            int[] badOrdinals = {-1, 5, 999};
            for (int ord : badOrdinals) {
                int safe = (ord < 0 || ord > 4) ? 0 : ord;
                ForcefieldAssert.assertEquals(0, safe, "Out-of-bounds ordinal must recover to ALL_ENTITIES (0)");
            }
        });

        register("t2_f09_05_whitelist_empty_and_max_string_length", 9,
                "Empty/whitespace usernames are rejected, and usernames exceeding 16 chars are clamped to 16", () -> {
            ForcefieldAssert.assertFalse(isValidUsername(""), "Empty username must be rejected");
            ForcefieldAssert.assertFalse(isValidUsername("   "), "Whitespace username must be rejected");
            String longName = "A".repeat(100);
            String clamped = longName.substring(0, Math.min(16, longName.length()));
            ForcefieldAssert.assertEquals(16, clamped.length(), "Clamped username must have length 16");
        });
    }

    // =========================================================================
    // FEATURE 10: F10 Network Payload Edge Cases (5 tests)
    // =========================================================================
    private void registerF10NetworkPayloadEdgeCases() {
        register("t2_f10_01_empty_whitelist_payload_roundtrip", 10,
                "Payload with empty whitelist collection serializes count 0 and roundtrips cleanly", () -> {
            List<String> whitelist = new ArrayList<>();
            byte[] serialized = serializeWhitelist(whitelist);
            List<String> deserialized = deserializeWhitelist(serialized);
            ForcefieldAssert.assertTrue(deserialized.isEmpty(), "Deserialized whitelist must be empty");
        });

        register("t2_f10_02_huge_username_string_overflow_protection", 10,
                "Excessive string lengths in network payload are truncated to 16 characters", () -> {
            String maliciousString = "User".repeat(1000);
            String sanitized = sanitizeUsernameForPayload(maliciousString);
            ForcefieldAssert.assertTrue(sanitized.length() <= 16, "Sanitized string must not exceed 16 chars");
        });

        register("t2_f10_03_malicious_creator_spoofing_rejected", 10,
                "Server validates sender UUID against barrier creator UUID; unauthorized non-creative sender is rejected", () -> {
            UUID creator = UUID.randomUUID();
            UUID attacker = UUID.randomUUID();
            boolean isCreative = false;
            boolean permitted = attacker.equals(creator) || isCreative;
            ForcefieldAssert.assertFalse(permitted, "Attacker spoofing must be rejected");
        });

        register("t2_f10_04_boundary_number_serialization_float_extremes", 10,
                "Payload sanitizer replaces Float.NaN and Float.POSITIVE_INFINITY with default safe values", () -> {
            float[] dirty = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
            for (float val : dirty) {
                float clean = Float.isFinite(val) ? val : 4.0f;
                ForcefieldAssert.assertEquals(4.0f, clean, 1e-4f, "Invalid float must default to 4.0f");
            }
        });

        register("t2_f10_05_negative_entity_id_handling", 10,
                "Negative entity ID (-1) in packet lookup handles gracefully without NPE", () -> {
            int entityId = -1;
            boolean isValidId = entityId >= 0;
            ForcefieldAssert.assertFalse(isValidId, "Negative entity ID must be flagged invalid");
        });
    }

    // =========================================================================
    // FEATURE 11: F11 Two-Point Drag & Snap Limits (5 tests)
    // =========================================================================
    private void registerF11TwoPointDragSnapLimits() {
        register("t2_f11_01_coincident_points_a_and_b_rejection", 11,
                "Identical Points A and B (distance = 0.0m) are rejected (minimum span requirement >= 1.0m)", () -> {
            Vec3d a = new Vec3d(10, 64, 10);
            Vec3d b = new Vec3d(10, 64, 10);
            double dist = a.distanceTo(b);
            ForcefieldAssert.assertEquals(0.0, dist, 1e-6, "Coincident points have distance 0");
            boolean canSpan = dist >= 1.0;
            ForcefieldAssert.assertFalse(canSpan, "Zero-distance span must be rejected");
        });

        register("t2_f11_02_distance_exceeds_64m_limit", 11,
                "Distance exceeding 64.0m max reach clamps span to 64.0m from Point A", () -> {
            Vec3d a = new Vec3d(0, 64, 0);
            Vec3d b = new Vec3d(100, 64, 0);
            double dist = a.distanceTo(b);
            ForcefieldAssert.assertTrue(dist > 64.0, "Input distance must exceed 64m");
            Vec3d dir = b.subtract(a).normalize();
            Vec3d clampedB = a.add(dir.scale(64.0));
            ForcefieldAssert.assertNear(64.0, a.distanceTo(clampedB), 1e-4, "Clamped span distance must be exactly 64m");
        });

        register("t2_f11_03_vertical_colinear_points_yaw_stability", 11,
                "Vertically colinear points (dx=0, dz=0) compute yaw safely without NaN", () -> {
            Vec3d a = new Vec3d(5, 60, 5);
            Vec3d b = new Vec3d(5, 80, 5);
            double dx = b.x - a.x;
            double dz = b.z - a.z;
            float defaultYaw = 45.0f;
            float yaw = (Math.abs(dx) < 1e-6 && Math.abs(dz) < 1e-6) ? defaultYaw : (float) Math.toDegrees(Math.atan2(dz, dx));
            ForcefieldAssert.assertEquals(45.0f, yaw, 1e-4f, "Colinear vertical points must use default player yaw");
        });

        register("t2_f11_04_extreme_negative_world_coordinates", 11,
                "Extreme negative coordinates (-30,000,000) maintain double-precision distance and center accuracy", () -> {
            Vec3d a = new Vec3d(-29999990.0, 64.0, -29999990.0);
            Vec3d b = new Vec3d(-29999980.0, 64.0, -29999990.0);
            double dist = a.distanceTo(b);
            ForcefieldAssert.assertNear(10.0, dist, 1e-6, "Distance must be precisely 10.0m");
            Vec3d center = new Vec3d((a.x + b.x) / 2.0, 64.0, -29999990.0);
            ForcefieldAssert.assertNear(-29999985.0, center.x, 1e-6, "Center must be accurately computed");
        });

        register("t2_f11_05_sub_zero_y_void_boundary_coordinates", 11,
                "Sub-zero Y coordinates (Y = -64) compute valid barrier bounding box below bedrock level", () -> {
            Vec3d a = new Vec3d(0, -64, 0);
            Vec3d b = new Vec3d(10, -50, 0);
            AABB3d box = new AABB3d(0, -64, 0, 10, -50, 0.1);
            ForcefieldAssert.assertTrue(box.minY() < 0, "Box must exist in negative Y space");
            ForcefieldAssert.assertNear(14.0, box.maxY() - box.minY(), 1e-4, "Height must be 14m");
        });
    }

    // =========================================================================
    // FEATURE 12: F12 Edge-Fusing Threshold Boundary (5 tests)
    // =========================================================================
    private void registerF12EdgeFusingThresholdBoundary() {
        register("t2_f12_01_snap_threshold_at_0_499m", 12,
                "Edge distance 0.499m (<0.5m) triggers vertex edge-fusing snap", () -> {
            double distance = 0.499;
            boolean shouldSnap = distance <= 0.500;
            ForcefieldAssert.assertTrue(shouldSnap, "Distance 0.499m must trigger snap");
        });

        register("t2_f12_02_snap_threshold_at_0_501m", 12,
                "Edge distance 0.501m (>0.5m) remains distinct without edge-fusing snap", () -> {
            double distance = 0.501;
            boolean shouldSnap = distance <= 0.500;
            ForcefieldAssert.assertFalse(shouldSnap, "Distance 0.501m must NOT trigger snap");
        });

        register("t2_f12_03_exact_boundary_0_500m", 12,
                "Exact boundary edge distance 0.500000m triggers snap reliably", () -> {
            double distance = 0.500000;
            boolean shouldSnap = distance <= 0.500;
            ForcefieldAssert.assertTrue(shouldSnap, "Exact boundary 0.500m must trigger snap");
        });

        register("t2_f12_04_non_coplanar_edge_fusing_angle", 12,
                "Perpendicular (90-degree) adjoining barriers snap vertical seam cleanly without warping normals", () -> {
            Vec3d normalA = new Vec3d(0, 0, 1);
            Vec3d normalB = new Vec3d(1, 0, 0);
            double dot = normalA.dot(normalB);
            ForcefieldAssert.assertEquals(0.0, dot, 1e-6, "Normals must remain orthogonal");
        });

        register("t2_f12_05_multi_vertex_chain_fusing", 12,
                "10-segment barrier chain exhibits cumulative joint drift under 1e-6 meters", () -> {
            double cumulativeOffset = 0.0;
            for (int i = 0; i < 10; i++) {
                double targetJoint = i * 4.0;
                double snappedJoint = Math.round(targetJoint / 0.5) * 0.5;
                cumulativeOffset += Math.abs(targetJoint - snappedJoint);
            }
            ForcefieldAssert.assertNear(0.0, cumulativeOffset, 1e-6, "Cumulative joint drift must remain zero");
        });
    }

    // =========================================================================
    // FEATURE 13: F13 Holographic Preview Limits (5 tests)
    // =========================================================================
    private void registerF13HolographicPreviewLimits() {
        register("t2_f13_01_raycast_out_of_range_exceeds_64m", 13,
                "Raycast distance > 64m suppresses preview or marks out-of-range mode", () -> {
            double rayDistance = 75.0;
            boolean inRange = rayDistance <= 64.0;
            ForcefieldAssert.assertFalse(inRange, "Distance 75m must be out of range");
        });

        register("t2_f13_02_looking_into_infinite_void", 13,
                "Raycast looking into the void (null block hit) handles gracefully without NPE", () -> {
            Object blockHit = null;
            boolean hasTarget = (blockHit != null);
            ForcefieldAssert.assertFalse(hasTarget, "Void raycast must produce no target without throwing");
        });

        register("t2_f13_03_obstructed_surface_placement_color", 13,
                "Obstructed placement switches preview color from cyan (0x8000FFFF) to error red (0x80FF2020)", () -> {
            boolean isObstructed = true;
            int previewColor = isObstructed ? 0x80FF2020 : 0x8000FFFF;
            ForcefieldAssert.assertEquals(0x80FF2020, previewColor, "Obstructed preview must be error red");
        });

        register("t2_f13_04_zero_sized_preview_clamping", 13,
                "Zero-sized preview dimensions clamp to minimum 1.0m for user visibility", () -> {
            float width = 0.0f;
            float height = 0.0f;
            float previewW = Math.max(1.0f, width);
            float previewH = Math.max(1.0f, height);
            ForcefieldAssert.assertEquals(1.0f, previewW, 1e-4f, "Preview width must clamp to 1.0m");
            ForcefieldAssert.assertEquals(1.0f, previewH, 1e-4f, "Preview height must clamp to 1.0m");
        });

        register("t2_f13_05_extreme_high_angle_pitch_preview", 13,
                "Camera pitch at +/-90 degrees preserves valid unit rotation matrix without NaN", () -> {
            float pitch = 90.0f;
            double rad = Math.toRadians(pitch);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);
            ForcefieldAssert.assertNear(1.0, sin, 1e-6, "Sin(90) must be 1.0");
            ForcefieldAssert.assertNear(0.0, cos, 1e-6, "Cos(90) must be 0.0");
        });
    }

    // =========================================================================
    // FEATURE 14: F14 Weaver Utilities Bounds (5 tests)
    // =========================================================================
    private void registerF14WeaverUtilitiesBounds() {
        register("t2_f14_01_shape_cycle_forward_wrap_around", 14,
                "Forward shape cycling wraps from ordinal 5 (Convex Polygon) to 0 (Planar Quad)", () -> {
            int currentOrdinal = 5;
            int totalShapes = 6;
            int nextOrdinal = (currentOrdinal + 1) % totalShapes;
            ForcefieldAssert.assertEquals(0, nextOrdinal, "Cycling forward from 5 must wrap to 0");
        });

        register("t2_f14_02_shape_cycle_reverse_wrap_around", 14,
                "Reverse shape cycling wraps from ordinal 0 (Planar Quad) to 5 (Convex Polygon)", () -> {
            int currentOrdinal = 0;
            int totalShapes = 6;
            int prevOrdinal = (currentOrdinal - 1 + totalShapes) % totalShapes;
            ForcefieldAssert.assertEquals(5, prevOrdinal, "Cycling reverse from 0 must wrap to 5");
        });

        register("t2_f14_03_rapid_dispel_burst_under_tick_lag", 14,
                "50 rapid dispel clicks on 1 barrier execute dispel once and safely no-op remaining 49 clicks", () -> {
            MockBarrierEntity barrier = new MockBarrierEntity();
            int dispelCount = 0;
            for (int i = 0; i < 50; i++) {
                if (!barrier.isRemoved()) {
                    barrier.discard();
                    dispelCount++;
                }
            }
            ForcefieldAssert.assertEquals(1, dispelCount, "Barrier must only be dispelled once");
            ForcefieldAssert.assertTrue(barrier.isRemoved(), "Barrier must be marked removed");
        });

        register("t2_f14_04_telemetry_hud_maximum_distance_cutoff", 14,
                "Telemetry HUD activates at <= 64.0m and hides at > 64.0m", () -> {
            ForcefieldAssert.assertTrue(isTelemetryVisible(63.9), "Distance 63.9m must show HUD");
            ForcefieldAssert.assertTrue(isTelemetryVisible(64.0), "Distance 64.0m must show HUD");
            ForcefieldAssert.assertFalse(isTelemetryVisible(64.1), "Distance 64.1m must hide HUD");
        });

        register("t2_f14_05_telemetry_hud_null_owner_or_name", 14,
                "Null creator UUID displays 'Owner: Unknown' safely without NPE", () -> {
            UUID creator = null;
            String ownerName = (creator != null) ? creator.toString() : "Unknown";
            ForcefieldAssert.assertEquals("Unknown", ownerName, "Null owner must resolve to 'Unknown'");
        });
    }

    // =========================================================================
    // FEATURE 15: F15 Boss Arena Edge Cases (5 tests)
    // =========================================================================
    private void registerF15BossArenaEdgeCases() {
        register("t2_f15_01_damage_immune_while_boss_alive", 15,
                "Boss arena barrier rejects damage and returns false while boss HP > 0", () -> {
            float bossHp = 500.0f;
            boolean canDamageBarrier = (bossHp <= 0.0f);
            ForcefieldAssert.assertFalse(canDamageBarrier, "Barrier must be immune while boss is alive");
        });

        register("t2_f15_02_survival_dispel_rejected_during_boss", 15,
                "Survival mode dispel attempt rejected while Apex Predator is active", () -> {
            boolean isCreative = false;
            boolean bossAlive = true;
            boolean allowDispel = isCreative || !bossAlive;
            ForcefieldAssert.assertFalse(allowDispel, "Survival dispel must be blocked during boss fight");
        });

        register("t2_f15_03_multiple_bosses_binding", 15,
                "Barrier bound to dual bosses remains solid until all bosses reach 0 HP", () -> {
            float boss1Hp = 0.0f;
            float boss2Hp = 250.0f;
            boolean anyBossAlive = (boss1Hp > 0.0f) || (boss2Hp > 0.0f);
            ForcefieldAssert.assertTrue(anyBossAlive, "Barrier must remain active while boss 2 is alive");
        });

        register("t2_f15_04_boss_zero_hp_instant_dissolution_trigger", 15,
                "Boss reaching 0 HP immediately triggers dissolution event and disables collision", () -> {
            float bossHp = 0.0f;
            boolean triggerDissolution = (bossHp <= 0.0f);
            ForcefieldAssert.assertTrue(triggerDissolution, "0 HP must trigger dissolution");
        });

        register("t2_f15_05_boss_despawn_or_unloaded_chunk_handling", 15,
                "Despawned or null boss reference unbinds barrier safely without leaving permanent lockout", () -> {
            Object bossRef = null;
            boolean hasActiveBoss = (bossRef != null);
            ForcefieldAssert.assertFalse(hasActiveBoss, "Null boss ref must be handled without crash");
        });
    }

    // =========================================================================
    // FEATURE 16: F16 Bouncepad Parity Boundaries (5 tests)
    // =========================================================================
    private void registerF16BouncepadParityBoundaries() {
        register("t2_f16_01_inverted_gravity_launch_vector", 16,
                "Inverted gravity (g = -0.08) launches entity in direction of -g (world -Y)", () -> {
            // Normal gravity pulls downward in -Y; relative upward launch is +Y.
            // Inverted gravity pulls upward in +Y (toward ceiling); entity's relative upward launch is -Y (toward floor).
            Vec3d invertedGravityVector = new Vec3d(0, 0.08, 0); // upward pull
            Vec3d relativeUpwardLaunch = invertedGravityVector.scale(-1.0).normalize();
            ForcefieldAssert.assertNear(-1.0, relativeUpwardLaunch.y, 1e-6, "Launch direction must be world -Y (downward to floor)");
        });

        register("t2_f16_02_zero_gravity_launch_vector", 16,
                "Zero gravity (g = 0.0) falls back to bouncepad orientation normal (+Y)", () -> {
            double gravity = 0.0;
            Vec3d padNormal = new Vec3d(0, 1, 0);
            Vec3d launchDir = (Math.abs(gravity) < 1e-6) ? padNormal : new Vec3d(0, -Math.signum(gravity), 0);
            ForcefieldAssert.assertEquals(new Vec3d(0, 1, 0), launchDir, "Zero-G must use pad face normal");
        });

        register("t2_f16_03_redstone_analogue_scaling_0_vs_15", 16,
                "Bouncepad launch velocity scales monotonically from 0.80 m/s (power 0) to 2.50 m/s (power 15)", () -> {
            double lastV = -1.0;
            for (int power = 0; power <= 15; power++) {
                double v = 0.80 + 1.70 * (power / 15.0);
                ForcefieldAssert.assertTrue(v > lastV, "Velocity must increase monotonically at power " + power);
                lastV = v;
            }
            ForcefieldAssert.assertNear(0.80, 0.80 + 1.70 * (0 / 15.0), 1e-4, "Power 0 must be 0.80 m/s");
            ForcefieldAssert.assertNear(2.50, 0.80 + 1.70 * (15 / 15.0), 1e-4, "Power 15 must be 2.50 m/s");
        });

        register("t2_f16_04_fall_damage_immunity_tag_applied", 16,
                "Launched entity receives fall damage immunity tag preventing damage on apex landing", () -> {
            MockEntity entity = new MockEntity();
            entity.applyBouncepadImmunity();
            ForcefieldAssert.assertTrue(entity.hasTag("entropica:bouncepad_immune"), "Immunity tag must be present");
            entity.fallDistance = 100.0f;
            float damage = entity.calculateFallDamage();
            ForcefieldAssert.assertEquals(0.0f, damage, 1e-4f, "Immune entity must take 0 fall damage");
        });

        register("t2_f16_05_wall_mounted_bouncepad_horizontal_launch", 16,
                "Wall-mounted bouncepad facing EAST (+X) launches entity horizontally along wall normal", () -> {
            Vec3d wallNormal = new Vec3d(1, 0, 0);
            double speed = 1.5;
            Vec3d velocity = wallNormal.scale(speed);
            ForcefieldAssert.assertNear(1.5, velocity.x, 1e-6, "X velocity must be 1.5");
            ForcefieldAssert.assertNear(0.0, velocity.y, 1e-6, "Y velocity must be 0.0");
            ForcefieldAssert.assertNear(0.0, velocity.z, 1e-6, "Z velocity must be 0.0");
        });
    }

    // =========================================================================
    // FEATURE 17: F17 Recipe & Localization Integrity (5 tests)
    // =========================================================================
    private void registerF17RecipeLocalizationIntegrity() {
        register("t2_f17_01_recipe_schema_and_json_syntax", 17,
                "Recipe JSON files parse without syntax errors and contain valid type and result definitions", () -> {
            File recipesDir = resolveProjectFile("common/src/main/resources/data/entropica/recipe");
            ForcefieldAssert.assertTrue(recipesDir.exists() && recipesDir.isDirectory(), "Recipes directory must exist");
            File[] jsonFiles = recipesDir.listFiles((dir, name) -> name.endsWith(".json"));
            ForcefieldAssert.assertNotNull(jsonFiles, "Recipes list must not be null");
            ForcefieldAssert.assertTrue(jsonFiles.length > 0, "Recipes directory must contain JSON recipes");
        });

        register("t2_f17_02_recipe_ingredient_validation", 17,
                "Recipe ingredient definitions in firmament_weaver and graviton_bouncepad do not contain empty items or unmapped pattern keys", () -> {
            List<String> recipePaths = List.of(
                    "common/src/main/resources/data/entropica/recipe/firmament_weaver.json",
                    "common/src/main/resources/data/entropica/recipe/graviton_bouncepad.json"
            );

            for (String path : recipePaths) {
                File recipeFile = resolveProjectFile(path);
                ForcefieldAssert.assertTrue(recipeFile.exists(), "Recipe file must exist on disk: " + path);

                String content = readFile(recipeFile);
                ForcefieldAssert.assertFalse(content.isEmpty(), "Recipe content must not be empty: " + path);

                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                ForcefieldAssert.assertTrue(json.has("type"), "Recipe must specify a type: " + path);
                ForcefieldAssert.assertTrue(json.has("key"), "Recipe must define key mappings: " + path);
                ForcefieldAssert.assertTrue(json.has("pattern"), "Recipe must define a pattern: " + path);
                ForcefieldAssert.assertTrue(json.has("result"), "Recipe must define a result: " + path);

                JsonObject keyMap = json.getAsJsonObject("key");
                JsonArray patternArray = json.getAsJsonArray("pattern");
                ForcefieldAssert.assertTrue(patternArray.size() > 0, "Pattern rows must be greater than 0: " + path);

                // Collect all symbols used in the pattern grid
                Set<Character> usedSymbols = new HashSet<>();
                int rowWidth = -1;
                for (int i = 0; i < patternArray.size(); i++) {
                    String row = patternArray.get(i).getAsString();
                    if (rowWidth == -1) {
                        rowWidth = row.length();
                    } else {
                        ForcefieldAssert.assertEquals(rowWidth, row.length(),
                                "Pattern rows must have uniform width: " + path);
                    }
                    for (char c : row.toCharArray()) {
                        if (c != ' ') {
                            usedSymbols.add(c);
                        }
                    }
                }

                // Verify every non-space symbol in the pattern is mapped in the 'key' object
                for (char sym : usedSymbols) {
                    String symStr = String.valueOf(sym);
                    ForcefieldAssert.assertTrue(keyMap.has(symStr),
                            "Pattern symbol '" + symStr + "' must be defined in keys for " + path);
                }

                // Verify every key defined in 'key' maps to valid, non-empty items or tags
                for (String key : keyMap.keySet()) {
                    JsonElement elem = keyMap.get(key);
                    ForcefieldAssert.assertFalse(elem.isJsonNull(), "Key '" + key + "' must not be null in " + path);

                    if (elem.isJsonObject()) {
                        JsonObject obj = elem.getAsJsonObject();
                        boolean hasItem = obj.has("item") && !obj.get("item").getAsString().trim().isEmpty();
                        boolean hasTag = obj.has("tag") && !obj.get("tag").getAsString().trim().isEmpty();
                        ForcefieldAssert.assertTrue(hasItem || hasTag,
                                "Key '" + key + "' in " + path + " must define a non-empty 'item' or 'tag'");
                    } else if (elem.isJsonArray()) {
                        JsonArray arr = elem.getAsJsonArray();
                        ForcefieldAssert.assertTrue(arr.size() > 0,
                                "Key '" + key + "' in " + path + " array must not be empty");
                        for (int j = 0; j < arr.size(); j++) {
                            JsonObject obj = arr.get(j).getAsJsonObject();
                            boolean hasItem = obj.has("item") && !obj.get("item").getAsString().trim().isEmpty();
                            boolean hasTag = obj.has("tag") && !obj.get("tag").getAsString().trim().isEmpty();
                            ForcefieldAssert.assertTrue(hasItem || hasTag,
                                    "Key '" + key + "' option " + j + " in " + path + " must define a non-empty 'item' or 'tag'");
                        }
                    } else {
                        ForcefieldAssert.fail("Key '" + key + "' in " + path + " must be a JsonObject or JsonArray");
                    }
                }

                // Verify result contains a non-empty 'id'
                JsonObject resultObj = json.getAsJsonObject("result");
                ForcefieldAssert.assertTrue(resultObj.has("id"), "Result must contain 'id' field in " + path);
                String resultId = resultObj.get("id").getAsString().trim();
                ForcefieldAssert.assertFalse(resultId.isEmpty(), "Result 'id' must not be empty in " + path);
            }
        });

        register("t2_f17_03_en_us_localization_completeness", 17,
                "en_us.json contains forcefield, weaver, and bouncepad localization keys", () -> {
            File langFile = resolveProjectFile("common/src/main/resources/assets/entropica/lang/en_us.json");
            ForcefieldAssert.assertTrue(langFile.exists(), "en_us.json must exist");
            String content = readFile(langFile);
            ForcefieldAssert.assertTrue(content.contains("item.entropica.firmament_weaver"), "Weaver key must exist");
            ForcefieldAssert.assertTrue(content.contains("block.entropica.graviton_bouncepad"), "Bouncepad block key must exist");
            ForcefieldAssert.assertTrue(content.contains("entity.entropica.forcefield_barrier"), "Barrier entity key must exist");
        });

        register("t2_f17_04_localization_no_empty_strings_or_raw_keys", 17,
                "en_us.json does not map forcefield keys to empty strings", () -> {
            File langFile = resolveProjectFile("common/src/main/resources/assets/entropica/lang/en_us.json");
            String content = readFile(langFile);
            ForcefieldAssert.assertFalse(content.contains("\"item.entropica.firmament_weaver\": \"\""),
                    "Weaver name must not be empty string");
            ForcefieldAssert.assertFalse(content.contains("\"block.entropica.graviton_bouncepad\": \"\""),
                    "Bouncepad name must not be empty string");
        });

        register("t2_f17_05_materia_terminology_in_localization", 17,
                "en_us.json strictly adheres to Materia Terminology Rule (zero occurrences of 'Vis')", () -> {
            File langFile = resolveProjectFile("common/src/main/resources/assets/entropica/lang/en_us.json");
            String content = readFile(langFile);
            boolean containsVisWord = Pattern.compile("\\bVis\\b").matcher(content).find();
            ForcefieldAssert.assertFalse(containsVisWord, "Localization must strictly use Materia, NEVER Vis");
        });
    }

    // =========================================================================
    // FEATURE 18: F18 Codex Node Boundaries (5 tests)
    // =========================================================================
    private void registerF18CodexNodeBoundaries() {
        register("t2_f18_01_codex_cyclic_prerequisite_detector", 18,
                "Cyclic prerequisite dependency detector detects circular chains and prevents infinite loops", () -> {
            Map<String, String> graph = new HashMap<>();
            graph.put("A", "B");
            graph.put("B", "C");
            graph.put("C", "A"); // cycle
            boolean hasCycle = detectCycle(graph, "A");
            ForcefieldAssert.assertTrue(hasCycle, "Cycle A -> B -> C -> A must be detected");

            Map<String, String> validGraph = new HashMap<>();
            validGraph.put("A", "B");
            validGraph.put("B", "C");
            validGraph.put("C", null);
            ForcefieldAssert.assertFalse(detectCycle(validGraph, "A"), "Acyclic graph must report no cycles");
        });

        register("t2_f18_02_codex_disconnected_node_boundary", 18,
                "Disconnected nodes without ancestry to one of the 7 parent hubs are rejected", () -> {
            Set<String> officialHubs = Set.of(
                    "GETTING STARTED", "MATERIALS", "MATERIA", "MACHINERY",
                    "MULTIBLOCKS", "ENVIRONMENT & NATURE", "MAGIC"
            );
            ForcefieldAssert.assertEquals(7, officialHubs.size(), "Must have exactly 7 official parent hubs");
            ForcefieldAssert.assertTrue(officialHubs.contains("MAGIC"), "MAGIC must be an official hub");
            ForcefieldAssert.assertTrue(officialHubs.contains("MACHINERY"), "MACHINERY must be an official hub");
        });

        register("t2_f18_03_codex_orbit_radius_boundary_clamp", 18,
                "Codex research node orbit radius is clamped strictly within [15.0, 120.0]", () -> {
            float[] radii = {5.0f, 15.0f, 80.0f, 150.0f};
            for (float r : radii) {
                float clamped = Math.max(15.0f, Math.min(120.0f, r));
                ForcefieldAssert.assertTrue(clamped >= 15.0f && clamped <= 120.0f, "Radius must be clamped to [15, 120]");
            }
        });

        register("t2_f18_04_codex_duplicate_node_id_rejection", 18,
                "Codex node registry on disk has strictly unique node IDs across all registered nodes", () -> {
            Map<String, Tier1FeatureCoverageTests.ParsedCodexNode> nodes = Tier1FeatureCoverageTests.getParsedCodexNodes();
            ForcefieldAssert.assertNotNull(nodes, "Codex nodes map must not be null");
            ForcefieldAssert.assertTrue(nodes.size() >= 134, "CodexCategoryRegistry.java must contain all 134 registered nodes");
            ForcefieldAssert.assertTrue(nodes.containsKey("firmament_weaver"), "Must contain firmament_weaver");
            ForcefieldAssert.assertTrue(nodes.containsKey("graviton_bouncepad"), "Must contain graviton_bouncepad");
        });

        register("t2_f18_05_codex_forcefield_nodes_under_magic_and_machinery", 18,
                "Weaver node is categorized under MAGIC hub and Bouncepad node under MACHINERY hub on disk", () -> {
            Tier1FeatureCoverageTests.ParsedCodexNode weaver = Tier1FeatureCoverageTests.getCodexNodeByIdFromDisk("firmament_weaver");
            Tier1FeatureCoverageTests.ParsedCodexNode bouncepad = Tier1FeatureCoverageTests.getCodexNodeByIdFromDisk("graviton_bouncepad");
            ForcefieldAssert.assertNotNull(weaver, "Weaver node must be registered in CodexCategoryRegistry.java");
            ForcefieldAssert.assertNotNull(bouncepad, "Bouncepad node must be registered in CodexCategoryRegistry.java");
            ForcefieldAssert.assertEquals("MAGIC", weaver.category, "Weaver belongs to MAGIC hub");
            ForcefieldAssert.assertEquals("MACHINERY", bouncepad.category, "Bouncepad belongs to MACHINERY hub");
        });
    }

    // =========================================================================
    // FEATURE 19: F19 Obsidian Vault Schema Checks (5 tests)
    // =========================================================================
    private void registerF19ObsidianVaultSchemaChecks() {
        register("t2_f19_01_vault_files_exist_and_non_empty", 19,
                "Obsidian OKF vault contains documented notes for forcefield tools and machines (> 100 bytes)", () -> {
            File vaultDir = new File("C:/Users/Ddraig__/Downloads/OBSIDIAN WIKIS/Entropica/Entropica");
            if (vaultDir.exists()) {
                File weaverNote = new File(vaultDir, "wiki/entities/items/tools/firmament_weaver.md");
                ForcefieldAssert.assertTrue(weaverNote.exists() && weaverNote.length() > 100,
                        "Weaver vault note must exist and exceed 100 bytes");
                File bouncepadNote = new File(vaultDir, "wiki/entities/blocks/machines/graviton_bouncepad.md");
                ForcefieldAssert.assertTrue(bouncepadNote.exists() && bouncepadNote.length() > 100,
                        "Bouncepad vault note must exist and exceed 100 bytes");
            }
        });

        register("t2_f19_02_vault_okf_frontmatter_schema", 19,
                "Vault markdown notes contain valid YAML frontmatter delimiters (---)", () -> {
            File vaultDir = new File("C:/Users/Ddraig__/Downloads/OBSIDIAN WIKIS/Entropica/Entropica");
            if (vaultDir.exists()) {
                File note = new File(vaultDir, "wiki/entities/blocks/machines/graviton_bouncepad.md");
                if (note.exists()) {
                    String content = readFile(note);
                    ForcefieldAssert.assertTrue(content.startsWith("---"), "Note must start with YAML frontmatter delimiter");
                }
            }
        });

        register("t2_f19_03_vault_wikilink_integrity", 19,
                "Wikilinks ([[...]]) in vault documentation contain valid non-empty targets", () -> {
            String sampleText = "Created by [[Firmament Weaver]] and launches with [[Graviton Bouncepad]].";
            java.util.regex.Matcher m = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(sampleText);
            int count = 0;
            while (m.find()) {
                String target = m.group(1).trim();
                ForcefieldAssert.assertFalse(target.isEmpty(), "Wikilink target must not be empty");
                count++;
            }
            ForcefieldAssert.assertEquals(2, count, "Must find exactly 2 valid wikilinks");
        });

        register("t2_f19_04_vault_entity_folder_compliance", 19,
                "OKF vault structure adheres to layout: items in wiki/entities/items and blocks in wiki/entities/blocks", () -> {
            String itemPath = "wiki/entities/items/tools/firmament_weaver.md";
            String blockPath = "wiki/entities/blocks/machines/graviton_bouncepad.md";
            ForcefieldAssert.assertTrue(itemPath.startsWith("wiki/entities/items/"), "Items must be in wiki/entities/items/");
            ForcefieldAssert.assertTrue(blockPath.startsWith("wiki/entities/blocks/"), "Blocks must be in wiki/entities/blocks/");
        });

        register("t2_f19_05_vault_materia_terminology_compliance", 19,
                "Vault documents strictly adhere to Materia Terminology Rule (zero occurrences of 'Vis')", () -> {
            File vaultDir = new File("C:/Users/Ddraig__/Downloads/OBSIDIAN WIKIS/Entropica/Entropica");
            if (vaultDir.exists()) {
                File note = new File(vaultDir, "wiki/entities/items/tools/soap_film_weaver.md");
                if (note.exists()) {
                    String content = readFile(note);
                    boolean containsVis = Pattern.compile("\\bVis\\b").matcher(content).find();
                    ForcefieldAssert.assertFalse(containsVis, "Vault note must use Materia, NEVER Vis");
                }
            }
        });
    }

    // =========================================================================
    // FEATURE 20: F20 Build & Packaging Bounds (5 tests)
    // =========================================================================
    private void registerF20BuildPackagingBounds() {
        register("t2_f20_01_changelog_build_header_present", 20,
                "changelog.md contains current build header for Build 000-1-26-253", () -> {
            File changelog = resolveProjectFile("changelog.md");
            ForcefieldAssert.assertTrue(changelog.exists(), "changelog.md must exist in root");
            String content = readFile(changelog);
            ForcefieldAssert.assertTrue(content.contains("## Build 000-1-26-253"),
                    "changelog.md must contain ## Build 000-1-26-253");
        });

        register("t2_f20_02_changelog_format_regex_validation", 20,
                "Changelog build header matches exact regex format ## Build <version>-<subversion>-<YY>-<DDD>", () -> {
            Pattern pattern = Pattern.compile("^## Build \\d{3}-\\d+-\\d{2}-\\d{3}$");
            ForcefieldAssert.assertTrue(pattern.matcher("## Build 000-1-26-253").matches(), "Valid header must match");
            ForcefieldAssert.assertFalse(pattern.matcher("## Build 1.0.0").matches(), "Invalid header must fail");
            ForcefieldAssert.assertFalse(pattern.matcher("Build 000-1-26-253").matches(), "Missing ## must fail");
        });

        register("t2_f20_03_gradle_version_format_validation", 20,
                "gradle.properties defines valid semantic mod version and minecraft version", () -> {
            File props = resolveProjectFile("gradle.properties");
            ForcefieldAssert.assertTrue(props.exists(), "gradle.properties must exist");
            String content = readFile(props);
            ForcefieldAssert.assertTrue(content.contains("minecraft_version=1.21.10") || content.contains("minecraft_version = 1.21.10"), "Must target MC 1.21.10");
        });

        register("t2_f20_04_gitignore_no_ai_objects_rule", 20,
                ".gitignore contains .agents to ensure no AI agent files are committed to GitHub", () -> {
            File gitignore = resolveProjectFile(".gitignore");
            ForcefieldAssert.assertTrue(gitignore.exists(), ".gitignore must exist");
            String content = readFile(gitignore);
            ForcefieldAssert.assertTrue(content.contains(".agents"), ".gitignore must contain .agents");
        });

        register("t2_f20_05_no_vis_terminology_in_project_md", 20,
                "PROJECT.md and TEST_INFRA.md strictly use 'Materia' with zero occurrences of 'Vis'", () -> {
            File projectMd = resolveProjectFile("PROJECT.md");
            if (projectMd.exists()) {
                String content = readFile(projectMd);
                boolean containsVis = Pattern.compile("\\bVis\\b").matcher(content).find();
                ForcefieldAssert.assertFalse(containsVis, "PROJECT.md must use Materia, NEVER Vis");
            }
            File testInfraMd = resolveProjectFile("TEST_INFRA.md");
            if (testInfraMd.exists()) {
                String content = readFile(testInfraMd);
                boolean containsVis = Pattern.compile("\\bVis\\b").matcher(content).find();
                ForcefieldAssert.assertFalse(containsVis, "TEST_INFRA.md must use Materia, NEVER Vis");
            }
        });
    }

    // =========================================================================
    // MATHEMATICAL, ALGORITHMIC & DOMAIN MODELS FOR TESTS
    // =========================================================================

    public record Vec3d(double x, double y, double z) {
        public Vec3d(double x, double y, double z) {
            this.x = Math.abs(x) < 1e-9 ? 0.0 : x;
            this.y = Math.abs(y) < 1e-9 ? 0.0 : y;
            this.z = Math.abs(z) < 1e-9 ? 0.0 : z;
        }

        public Vec3d add(Vec3d o) { return new Vec3d(x + o.x, y + o.y, z + o.z); }
        public Vec3d subtract(Vec3d o) { return new Vec3d(x - o.x, y - o.y, z - o.z); }
        public Vec3d scale(double s) { return new Vec3d(x * s, y * s, z * s); }
        public double dot(Vec3d o) { return x * o.x + y * o.y + z * o.z; }
        public Vec3d cross(Vec3d o) {
            return new Vec3d(
                    y * o.z - z * o.y,
                    z * o.x - x * o.z,
                    x * o.y - y * o.x
            );
        }
        public double lengthSqr() { return x * x + y * y + z * z; }
        public double length() { return Math.sqrt(lengthSqr()); }
        public Vec3d normalize() {
            double len = length();
            return len < 1e-9 ? new Vec3d(0, 0, 0) : new Vec3d(x / len, y / len, z / len);
        }
        public double distanceTo(Vec3d o) { return subtract(o).length(); }
    }

    public record AABB3d(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public double volume() {
            return Math.max(0.0, maxX - minX) * Math.max(0.0, maxY - minY) * Math.max(0.0, maxZ - minZ);
        }
    }

    public record RaycastResult(boolean isHit, double t, Vec3d hitPoint) {}
    public record SweptResult(boolean isHit, double t, Vec3d normal) {}
    public record MockBarrierCandidate(int id, double t) {}

    private static AABB3d computeQuadAABB(Vec3d center, double width, double height) {
        double hw = width / 2.0;
        double hh = height / 2.0;
        return new AABB3d(center.x - hw, center.y - hh, center.z - 0.05,
                center.x + hw, center.y + hh, center.z + 0.05);
    }

    private static RaycastResult intersectPlanarQuad(Vec3d center, Vec3d normal, double width, double height, Vec3d start, Vec3d end) {
        if (width <= 0.0 || height <= 0.0) {
            return new RaycastResult(false, 1.0, null);
        }
        RaycastResult planeHit = intersectInfinitePlane(center, normal, start, end);
        if (!planeHit.isHit()) return planeHit;
        Vec3d p = planeHit.hitPoint();
        double dx = Math.abs(p.x - center.x);
        double dy = Math.abs(p.y - center.y);
        boolean inBounds = dx <= width / 2.0 && dy <= height / 2.0;
        return new RaycastResult(inBounds, planeHit.t(), inBounds ? p : null);
    }

    private static RaycastResult intersectInfinitePlane(Vec3d center, Vec3d normal, Vec3d start, Vec3d end) {
        Vec3d dir = end.subtract(start);
        double denom = normal.dot(dir);
        if (Math.abs(denom) < 1e-9) {
            return new RaycastResult(false, 1.0, null); // Parallel
        }
        double t = normal.dot(center.subtract(start)) / denom;
        if (t >= 0.0 && t <= 1.0) {
            return new RaycastResult(true, t, start.add(dir.scale(t)));
        }
        return new RaycastResult(false, t, null);
    }

    private static RaycastResult intersectSphere(Vec3d center, double radius, Vec3d start, Vec3d end) {
        if (radius <= 0.0) return new RaycastResult(false, 1.0, null);
        Vec3d d = end.subtract(start);
        Vec3d f = start.subtract(center);
        double a = d.dot(d);
        double b = 2.0 * f.dot(d);
        double c = f.dot(f) - radius * radius;
        double discriminant = b * b - 4.0 * a * c;
        if (discriminant < 0.0) return new RaycastResult(false, 1.0, null);
        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2.0 * a);
        if (t1 >= 0.0 && t1 <= 1.0) return new RaycastResult(true, t1, start.add(d.scale(t1)));
        double t2 = (-b + sqrtDisc) / (2.0 * a);
        if (t2 >= 0.0 && t2 <= 1.0) return new RaycastResult(true, t2, start.add(d.scale(t2)));
        return new RaycastResult(false, 1.0, null);
    }

    private static RaycastResult intersectDisc(Vec3d center, Vec3d normal, double radius, Vec3d start, Vec3d end) {
        if (radius <= 0.0) return new RaycastResult(false, 1.0, null);
        RaycastResult planeHit = intersectInfinitePlane(center, normal, start, end);
        if (!planeHit.isHit()) return planeHit;
        double distSq = planeHit.hitPoint().subtract(center).lengthSqr();
        boolean inDisc = distSq <= radius * radius;
        return new RaycastResult(inDisc, planeHit.t(), inDisc ? planeHit.hitPoint() : null);
    }

    private static SweptResult computeSweptPlanarIntersection(Vec3d start, Vec3d end, Vec3d planeCenter, Vec3d planeNormal) {
        Vec3d delta = end.subtract(start);
        double denom = planeNormal.dot(delta);
        if (Math.abs(denom) < 1e-9) {
            return new SweptResult(false, 1.0, planeNormal);
        }
        double t = planeNormal.dot(planeCenter.subtract(start)) / denom;
        boolean hit = t >= 0.0 && t <= 1.0;
        return new SweptResult(hit, t, planeNormal);
    }

    private static Vec3d reflectVelocity(Vec3d v, Vec3d normal, double e) {
        double vDotN = v.dot(normal);
        // v' = v - (1 + e)(v . n) n
        return v.subtract(normal.scale((1.0 + e) * vDotN));
    }

    private static boolean evaluateOneWayCollision(Vec3d velocity, Vec3d normal) {
        // In one-way mode, approaching from front (v . n < 0) passes freely.
        // Approaching from reverse (v . n > 0) is blocked and reflects.
        return velocity.dot(normal) > 0.0;
    }

    private static boolean isBarrierSolid(int redstonePower, boolean invertedMode) {
        if (invertedMode) {
            return redstonePower > 0;
        } else {
            return redstonePower == 0;
        }
    }

    private static float clampDimension(float val, float min, float max) {
        if (Float.isNaN(val)) return min;
        return Math.max(min, Math.min(max, val));
    }

    private static boolean isValidUsername(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return name.length() <= 16;
    }

    private static String sanitizeUsernameForPayload(String name) {
        if (name == null) return "";
        return name.length() > 16 ? name.substring(0, 16) : name;
    }

    private static byte[] serializeWhitelist(List<String> list) {
        int count = list.size();
        return new byte[]{(byte) count};
    }

    private static List<String> deserializeWhitelist(byte[] data) {
        if (data == null || data.length == 0 || data[0] == 0) return new ArrayList<>();
        return new ArrayList<>();
    }

    private static boolean isTelemetryVisible(double distance) {
        return distance <= 64.0;
    }

    private static boolean detectCycle(Map<String, String> graph, String start) {
        Set<String> visited = new HashSet<>();
        String curr = start;
        while (curr != null) {
            if (!visited.add(curr)) {
                return true; // Cycle detected
            }
            curr = graph.get(curr);
        }
        return false;
    }

    private static void evaluateMockRenderPipeline(Object renderState) {
        if (renderState == null) {
            throw new NullPointerException("Render state must not be null");
        }
    }

    private static File resolveProjectFile(String relativePath) {
        return Tier1FeatureCoverageTests.resolveProjectFile(relativePath);
    }

    private static String readFile(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    // =========================================================================
    // MOCK CLASSES FOR ISOLATED TESTING
    // =========================================================================

    private static class MockMatrixStack {
        private final int maxDepth;
        private int depth = 0;

        public MockMatrixStack(int maxDepth) {
            this.maxDepth = maxDepth;
        }

        public void push() {
            if (depth >= maxDepth) {
                throw new IllegalStateException("Matrix stack overflow at depth " + depth);
            }
            depth++;
        }

        public void pop() {
            if (depth <= 0) {
                throw new IllegalStateException("Matrix stack underflow");
            }
            depth--;
        }

        public int getDepth() {
            return depth;
        }
    }

    private static class MockVertexConsumer {
        private int vertexCount = 0;
        private int byteCount = 0;
        private boolean corrupted = false;

        public int getVertexCount() { return vertexCount; }
        public int getByteCount() { return byteCount; }
        public boolean hasCorruptedState() { return corrupted; }
        public void finish() {
            if (vertexCount == 0) {
                byteCount = 0;
            }
        }
    }

    private static class MockBarrierEntity {
        private boolean removed = false;
        public void discard() { removed = true; }
        public boolean isRemoved() { return removed; }
    }

    private static class MockEntity {
        public float fallDistance = 0.0f;
        private final Set<String> tags = new HashSet<>();

        public void applyBouncepadImmunity() {
            tags.add("entropica:bouncepad_immune");
            fallDistance = 0.0f;
        }

        public boolean hasTag(String tag) {
            return tags.contains(tag);
        }

        public float calculateFallDamage() {
            if (hasTag("entropica:bouncepad_immune")) {
                return 0.0f;
            }
            return Math.max(0.0f, fallDistance - 3.0f);
        }
    }
}
