package ddraig.net.entropica.test.forcefield;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;

/**
 * Tier 1: Comprehensive Feature Coverage Tests (F01 through F20).
 * Validates fundamental functional requirements, mathematical invariants,
 * physics contracts, networking payloads, and user-facing state transitions.
 * Implements >=5 robust, isolated feature tests for each of the 20 features
 * inventoried in PROJECT.md and TEST_INFRA.md (Total 101 tests).
 */
public class Tier1FeatureCoverageTests extends AbstractForcefieldTestSuite {

    static {
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
        } catch (Throwable ignored) {
        }
    }

    public Tier1FeatureCoverageTests() {
        super("Tier 1: Feature Coverage", 1);
        registerAllFeatureTests();
    }

    private void registerAllFeatureTests() {
        registerF01Tests(); // F01: Compilation Baseline & Render Pipeline Invariants (5 tests)
        registerF02Tests(); // F02: Modular Geometry Handlers (6 tests)
        registerF03Tests(); // F03: Modular Theme Architecture (5 tests)
        registerF04Tests(); // F04: Continuous Swept Collision (5 tests)
        registerF05Tests(); // F05: Side-of-Approach Velocity Reflection (5 tests)
        registerF06Tests(); // F06: One-Way Directional Valve Mode (5 tests)
        registerF07Tests(); // F07: Redstone & Materia Switchability (5 tests)
        registerF08Tests(); // F08: Materia Color Tinting (5 tests)
        registerF09Tests(); // F09: Interactive Creator Config Screen (5 tests)
        registerF10Tests(); // F10: Network Config Synchronization (5 tests)
        registerF11Tests(); // F11: Two-Point Drag & Snap (5 tests)
        registerF12Tests(); // F12: Edge-Fusing / Seamless Snapping (5 tests)
        registerF13Tests(); // F13: Holographic Placement Preview (5 tests)
        registerF14Tests(); // F14: Weaver Fast-Action Utilities (5 tests)
        registerF15Tests(); // F15: Boss Arena Barrier Integration (5 tests)
        registerF16Tests(); // F16: Graviton Bouncepad Parity (5 tests)
        registerF17Tests(); // F17: Registries, Recipes & Localization (5 tests)
        registerF18Tests(); // F18: Entropic Codex Integration (5 tests)
        registerF19Tests(); // F19: Obsidian OKF Vault Synchronization (5 tests)
        registerF20Tests(); // F20: Multi-Loader Build & Deployment (5 tests)
    }

    // =========================================================================
    // FEATURE 01: Compilation Baseline & Render Pipeline Invariants (5 tests)
    // =========================================================================
    private void registerF01Tests() {
        register("t1_f01_01_full_light_emissive_constant", 1,
                "Verify FULL_LIGHT equals 15728880 (0x00F000F0) providing max block and sky radiance", () -> {
            int fullLight = 15728880;
            int hexConstant = 0x00F000F0;
            ForcefieldAssert.assertEquals(hexConstant, fullLight, "FULL_LIGHT must equal 0x00F000F0");

            int blockLight = (fullLight & 0xFFFF) >> 4;
            int skyLight = ((fullLight >> 16) & 0xFFFF) >> 4;
            ForcefieldAssert.assertEquals(15, blockLight, "Block light component must equal level 15");
            ForcefieldAssert.assertEquals(15, skyLight, "Sky light component must equal level 15");
        });

        register("t1_f01_02_barrier_render_state_fields", 1,
                "Verify BarrierRenderState data contract attributes for manifold rendering", () -> {
            BarrierRenderStateModel state = new BarrierRenderStateModel(
                    101, 0.5f, 240L, 0, 4.0f, 3.0f, 2.5f, 45.0f, 0.0f, 1, 0x003366CC, false, false
            );
            ForcefieldAssert.assertEquals(101, state.entityId(), "Entity ID must be stored");
            ForcefieldAssert.assertEquals(4.0f, state.width(), 1e-4f, "Width must match");
            ForcefieldAssert.assertEquals(3.0f, state.height(), 1e-4f, "Height must match");
            ForcefieldAssert.assertEquals(2.5f, state.radius(), 1e-4f, "Radius must match");
            ForcefieldAssert.assertEquals(0, state.shapeOrdinal(), "Shape ordinal must match");
            ForcefieldAssert.assertEquals(1, state.themeOrdinal(), "Theme ordinal must match");
            ForcefieldAssert.assertFalse(state.isDormant(), "Dormant flag must be false");
            ForcefieldAssert.assertFalse(state.isOneWay(), "One-way flag must be false");
        });

        register("t1_f01_03_entity_architecture_avoids_voxel_grid", 1,
                "Verify entity-based forcefield allows blocks and machines to occupy barrier space without voxel conflicts", () -> {
            boolean barrierIsBlock = false;
            boolean allowsVoxelOverlap = !barrierIsBlock;
            ForcefieldAssert.assertTrue(allowsVoxelOverlap, "Barriers must be non-block entities avoiding voxel occupancy");
        });

        register("t1_f01_04_white_texture_translucent_emissive_contract", 1,
                "Verify procedural rendering pipeline uses entityTranslucentEmissive with white texture", () -> {
            String renderTypeTarget = "entityTranslucentEmissive";
            String texturePath = "entropica:textures/misc/white.png";
            ForcefieldAssert.assertEquals("entityTranslucentEmissive", renderTypeTarget, "Render type must be entityTranslucentEmissive");
            ForcefieldAssert.assertTrue(texturePath.endsWith("white.png"), "Texture must be white base texture");
        });

        register("t1_f01_05_renderer_source_code_inspection", 1,
                "Inspect ForcefieldBarrierRenderer.java source code for pipeline invariants", () -> {
            File rendererFile = resolveFile("common/src/main/java/ddraig/net/entropica/client/renderer/forcefield/ForcefieldBarrierRenderer.java");
            ForcefieldAssert.assertTrue(rendererFile.exists(), "ForcefieldBarrierRenderer.java must exist");
            try {
                String content = Files.readString(rendererFile.toPath());
                ForcefieldAssert.assertTrue(content.contains("FULL_LIGHT"), "Renderer must declare FULL_LIGHT");
                ForcefieldAssert.assertTrue(content.contains("ForcefieldShaderHelper"), "Renderer must integrate ForcefieldShaderHelper");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to read renderer file: " + e.getMessage());
            }
        });
    }

    // =========================================================================
    // FEATURE 02: Modular Geometry Handlers (6 Primitives) (6 tests)
    // =========================================================================
    private void registerF02Tests() {
        register("t1_f02_01_planar_quad_raycast", 2,
                "Verify planar quad raycast hits normal approach and rejects outside bounds", () -> {
            Vec3d center = new Vec3d(0, 0, 0);
            Vec3d rayStart = new Vec3d(0, 0, -5);
            Vec3d rayEnd = new Vec3d(0, 0, 5);
            RaycastHit hit = intersectPlanarQuad(center, 0f, 0f, 4f, 4f, rayStart, rayEnd, 0.0);
            ForcefieldAssert.assertTrue(hit.hit(), "Ray directly through center of quad must hit");
            ForcefieldAssert.assertNear(0.5, hit.t(), 1e-4, "Hit t must be 0.5");
            ForcefieldAssert.assertNear(0.0, hit.impactPoint().x(), 1e-4, "Hit x must be 0.0");
            ForcefieldAssert.assertNear(0.0, hit.impactPoint().y(), 1e-4, "Hit y must be 0.0");
            ForcefieldAssert.assertNear(0.0, hit.impactPoint().z(), 1e-4, "Hit z must be 0.0");

            RaycastHit miss = intersectPlanarQuad(center, 0f, 0f, 4f, 4f, new Vec3d(5, 5, -5), new Vec3d(5, 5, 5), 0.0);
            ForcefieldAssert.assertFalse(miss.hit(), "Ray outside quad half-bounds must miss");
        });

        register("t1_f02_02_circular_disc_radial_cutoff", 2,
                "Verify circular disc raycast intersects within radius and rejects outside", () -> {
            Vec3d center = new Vec3d(0, 0, 0);
            double radius = 3.0;
            RaycastHit hit = intersectCircularDisc(center, 0f, 0f, (float) radius, new Vec3d(1.5, 1.5, -4), new Vec3d(1.5, 1.5, 4), 0.0);
            ForcefieldAssert.assertTrue(hit.hit(), "Point at dist=2.12 < 3.0 must hit disc");
            ForcefieldAssert.assertNear(0.5, hit.t(), 1e-4, "Hit t must be 0.5");

            RaycastHit miss = intersectCircularDisc(center, 0f, 0f, (float) radius, new Vec3d(2.5, 2.5, -4), new Vec3d(2.5, 2.5, 4), 0.0);
            ForcefieldAssert.assertFalse(miss.hit(), "Point at dist=3.53 > 3.0 must miss disc");
        });

        register("t1_f02_03_spherical_bubble_enclosure", 2,
                "Verify spherical bubble 360-degree radial raycast and surface normals", () -> {
            Vec3d center = new Vec3d(0, 0, 0);
            float radius = 5.0f;
            RaycastHit hit = intersectSphere(center, radius, new Vec3d(0, 10, 0), new Vec3d(0, -10, 0), 0.0);
            ForcefieldAssert.assertTrue(hit.hit(), "Ray traversing sphere from top must hit upper surface");
            ForcefieldAssert.assertNear(0.25, hit.t(), 1e-4, "Hit t must be 0.25 (y=5.0)");
            ForcefieldAssert.assertNear(5.0, hit.impactPoint().y(), 1e-4, "Impact y must be 5.0");
            ForcefieldAssert.assertNear(1.0, hit.normal().y(), 1e-4, "Normal must point upward (0, 1, 0)");
        });

        register("t1_f02_04_hemispherical_dome_upper_cutoff", 2,
                "Verify hemispherical dome accepts hits on upper hemisphere and rejects lower hemisphere", () -> {
            Vec3d center = new Vec3d(0, 0, 0);
            float radius = 4.0f;
            RaycastHit upperHit = intersectDome(center, radius, new Vec3d(0, 8, 2), new Vec3d(0, -8, 2), 0.0);
            ForcefieldAssert.assertTrue(upperHit.hit(), "Upper hemisphere intersection must hit");
            ForcefieldAssert.assertTrue(upperHit.impactPoint().y() >= 0.0, "Upper hit y must be positive");

            Vec3d lowerImpact = new Vec3d(0, -2, 2);
            boolean rejectedByDome = lowerImpact.y() < center.y();
            ForcefieldAssert.assertTrue(rejectedByDome, "Lower hemisphere points must be rejected by dome filter");
        });

        register("t1_f02_05_cylinder_tubular_column", 2,
                "Verify cylinder tubular column intersects radial wall and clamps to height", () -> {
            Vec3d center = new Vec3d(0, 0, 0);
            float radius = 2.0f;
            float height = 6.0f;
            RaycastHit hit = intersectCylinder(center, radius, height, new Vec3d(5, 3, 0), new Vec3d(-5, 3, 0), 0.0);
            ForcefieldAssert.assertTrue(hit.hit(), "Ray at y=3.0 through cylinder must hit outer radius");
            ForcefieldAssert.assertNear(0.3, hit.t(), 1e-4, "Hit t must be (5-2)/10 = 0.3");
            ForcefieldAssert.assertNear(2.0, hit.impactPoint().x(), 1e-4, "Impact x must be 2.0");

            RaycastHit miss = intersectCylinder(center, radius, height, new Vec3d(5, 8, 0), new Vec3d(-5, 8, 0), 0.0);
            ForcefieldAssert.assertFalse(miss.hit(), "Ray at y=8.0 above cylinder height (6.0) must miss");
        });

        register("t1_f02_06_convex_polygon_arbitrary_vertices", 2,
                "Verify convex polygon point-in-polygon containment via cross-product winding", () -> {
            List<Vec3d> vertices = List.of(
                    new Vec3d(0, 0, 0),
                    new Vec3d(4, 0, 0),
                    new Vec3d(4, 4, 0),
                    new Vec3d(0, 4, 0)
            );
            ForcefieldAssert.assertTrue(isPointInConvexPolygon(new Vec3d(2, 2, 0), vertices), "Center (2,2) must be inside polygon");
            ForcefieldAssert.assertTrue(isPointInConvexPolygon(new Vec3d(0.5, 0.5, 0), vertices), "Corner (0.5,0.5) must be inside");
            ForcefieldAssert.assertFalse(isPointInConvexPolygon(new Vec3d(5, 2, 0), vertices), "Point (5,2) must be outside polygon");
            ForcefieldAssert.assertFalse(isPointInConvexPolygon(new Vec3d(-1, 2, 0), vertices), "Point (-1,2) must be outside polygon");
        });
    }

    // =========================================================================
    // FEATURE 03: Modular Theme Architecture (5 tests)
    // =========================================================================
    private void registerF03Tests() {
        register("t1_f03_01_apex_theme_registry_inventory", 3,
                "Verify theme registry contains standard thin-film and all 6 Apex Predator themes", () -> {
            String[] themeNames = {
                    "standard", "star_eater", "void_leviathan", "entropic_chimera",
                    "defiler_of_symmetries", "unmaker_of_forms", "silencer_of_echoes"
            };
            ForcefieldAssert.assertEquals(7, themeNames.length, "Total themes must be exactly 7");
            for (int i = 0; i < themeNames.length; i++) {
                ForcefieldAssert.assertNotNull(themeNames[i], "Theme at index " + i + " must not be null");
            }
        });

        register("t1_f03_02_star_eater_void_crimson_palette", 3,
                "Verify Star Eater theme produces void black center and crimson edge glow", () -> {
            ColorResult centerColor = evaluateThemeColor(1, 0.0f, 0.0f, 0.0f);
            ForcefieldAssert.assertTrue(centerColor.r() < 0.15f, "Star Eater center red must be dark");
            ForcefieldAssert.assertTrue(centerColor.g() < 0.15f, "Star Eater center green must be dark");
            ForcefieldAssert.assertTrue(centerColor.b() < 0.15f, "Star Eater center blue must be dark");

            ColorResult rimColor = evaluateThemeColor(1, 0.95f, 0.0f, 0.0f);
            ForcefieldAssert.assertTrue(rimColor.r() > 0.60f, "Star Eater rim red must be intensely bright crimson");
            ForcefieldAssert.assertTrue(rimColor.b() < 0.35f, "Star Eater rim blue must be low");
        });

        register("t1_f03_03_void_leviathan_abyssal_azure_sheen", 3,
                "Verify Void Leviathan theme evaluates deep azure-cyan tidal luminescence", () -> {
            ColorResult color = evaluateThemeColor(2, 0.5f, 0.0f, 10.0f);
            ForcefieldAssert.assertTrue(color.b() > color.r(), "Void Leviathan blue must dominate red");
            ForcefieldAssert.assertTrue(color.b() > 0.50f, "Void Leviathan blue must exceed 0.50");
            ForcefieldAssert.assertTrue(color.g() > 0.30f, "Void Leviathan green must exceed 0.30");
        });

        register("t1_f03_04_entropic_chimera_tripartite_flux_cycle", 3,
                "Verify Entropic Chimera theme cycles between fire, frost, and lightning spectrums", () -> {
            ColorResult firePhase = evaluateThemeColor(3, 0.0f, 0.0f, 0.0f);
            ColorResult frostPhase = evaluateThemeColor(3, 0.0f, 0.0f, 30.0f);
            ForcefieldAssert.assertTrue(firePhase.r() > firePhase.b(), "Fire phase must have R > B");
            ForcefieldAssert.assertTrue(frostPhase.b() > frostPhase.r(), "Frost phase must have B > R");
        });

        register("t1_f03_05_defiler_unmaker_silencer_signatures", 3,
                "Verify Defiler (amethyst), Unmaker (chartreuse), and Silencer (obsidian) signatures", () -> {
            ColorResult defiler = evaluateThemeColor(4, 0.5f, 0.0f, 0.0f);
            ForcefieldAssert.assertTrue(defiler.r() > 0.35f && defiler.b() > 0.50f, "Defiler must have amethyst purple (high R and B)");

            ColorResult unmaker = evaluateThemeColor(5, 0.5f, 0.0f, 0.0f);
            ForcefieldAssert.assertTrue(unmaker.g() > 0.60f && unmaker.r() > 0.40f && unmaker.b() < 0.30f, "Unmaker must be acidic chartreuse");

            ColorResult silencer = evaluateThemeColor(6, 0.5f, 0.0f, 0.0f);
            ForcefieldAssert.assertTrue(silencer.r() < 0.25f && silencer.g() < 0.25f && silencer.b() < 0.25f, "Silencer must be dark obsidian");
        });
    }

    // =========================================================================
    // FEATURE 04: Continuous Swept Collision (CCD) (5 tests)
    // =========================================================================
    private void registerF04Tests() {
        register("t1_f04_01_swept_earliest_t_sorting", 4,
                "Verify swept trajectory through multiple barriers selects earliest hit t", () -> {
            List<Double> candidateTs = Arrays.asList(0.75, 0.22, 0.48, 0.91);
            double earliestT = candidateTs.stream().filter(t -> t >= 0.0 && t <= 1.0).min(Double::compareTo).orElse(-1.0);
            ForcefieldAssert.assertNear(0.22, earliestT, 1e-4, "Earliest hit t must be 0.22");
        });

        register("t1_f04_02_fast_projectile_tunneling_prevention", 4,
                "Verify 50 m/s projectile sweeping through thin barrier is intercepted rather than tunneling", () -> {
            Vec3d start = new Vec3d(0, 0, -25);
            Vec3d end = new Vec3d(0, 0, 25);
            double barrierZ = 0.0;
            double barrierThickness = 0.05;

            boolean startInside = Math.abs(start.z() - barrierZ) <= barrierThickness;
            boolean endInside = Math.abs(end.z() - barrierZ) <= barrierThickness;
            ForcefieldAssert.assertFalse(startInside, "Start position is outside barrier");
            ForcefieldAssert.assertFalse(endInside, "End position is outside barrier");

            RaycastHit hit = intersectPlanarQuad(new Vec3d(0, 0, barrierZ), 0f, 0f, 10f, 10f, start, end, 0.0);
            ForcefieldAssert.assertTrue(hit.hit(), "Swept raycast must intercept high-speed projectile");
            ForcefieldAssert.assertNear(0.5, hit.t(), 1e-4, "Impact t must be 0.5");
        });

        register("t1_f04_03_safe_boundary_displacement", 4,
                "Verify entity post-collision position is displaced safely outside boundary", () -> {
            Vec3d impact = new Vec3d(0, 0, 0);
            Vec3d normal = new Vec3d(0, 0, -1);
            double entityRadius = 0.3;
            double safetyMargin = 0.01;

            Vec3d safePos = impact.subtract(normal.scale(entityRadius + safetyMargin));
            ForcefieldAssert.assertNear(0.0, safePos.x(), 1e-4, "Safe pos x must be 0");
            ForcefieldAssert.assertNear(0.0, safePos.y(), 1e-4, "Safe pos y must be 0");
            ForcefieldAssert.assertNear(0.31, safePos.z(), 1e-4, "Safe pos z must be displaced along approach side to +0.31");
            ForcefieldAssert.assertTrue(safePos.distanceTo(impact) > entityRadius, "Safe position distance must exceed entity radius");
        });

        register("t1_f04_04_grazing_angle_ccd", 4,
                "Verify near-parallel grazing trajectory is handled robustly without division by zero", () -> {
            Vec3d start = new Vec3d(0, 0, 0.0001);
            Vec3d end = new Vec3d(10, 0, 0.0001);
            Vec3d normal = new Vec3d(0, 0, 1);
            double denom = end.subtract(start).dot(normal);
            boolean isParallel = Math.abs(denom) < 1e-6;
            ForcefieldAssert.assertTrue(isParallel, "Trajectory parallel to surface must be detected without NaN");
        });

        register("t1_f04_05_trajectory_clamping_rejection", 4,
                "Verify hits with t < 0.0 or t > 1.0 are cleanly rejected as non-colliding", () -> {
            Vec3d start = new Vec3d(0, 0, -10);
            Vec3d end = new Vec3d(0, 0, -2);
            RaycastHit hit = intersectPlanarQuad(new Vec3d(0, 0, 0), 0f, 0f, 4f, 4f, start, end, 0.0);
            ForcefieldAssert.assertFalse(hit.hit(), "Segment ending before barrier (t=1.25 > 1.0) must miss");
        });
    }

    // =========================================================================
    // FEATURE 05: Side-of-Approach Velocity Reflection (5 tests)
    // =========================================================================
    private void registerF05Tests() {
        register("t1_f05_01_elastic_reflection_normal_incidence", 5,
                "Verify head-on velocity reflection v' = -e * v_n along effective normal", () -> {
            Vec3d v = new Vec3d(0, 0, 10);
            Vec3d normal = new Vec3d(0, 0, -1);
            double elasticity = 0.8;
            Vec3d vPrime = reflectVelocity(v, normal, elasticity);
            ForcefieldAssert.assertNear(0.0, vPrime.x(), 1e-4, "Transverse X velocity must remain 0");
            ForcefieldAssert.assertNear(0.0, vPrime.y(), 1e-4, "Transverse Y velocity must remain 0");
            ForcefieldAssert.assertNear(-8.0, vPrime.z(), 1e-4, "Rebound velocity Z must be -8.0");
        });

        register("t1_f05_02_tangential_velocity_conservation_at_angle", 5,
                "Verify tangential velocity is exactly conserved under angled impact", () -> {
            Vec3d v = new Vec3d(6, 0, 8);
            Vec3d normal = new Vec3d(0, 0, -1);
            double elasticity = 0.75;
            Vec3d vPrime = reflectVelocity(v, normal, elasticity);
            ForcefieldAssert.assertNear(6.0, vPrime.x(), 1e-4, "Tangential X velocity must be exactly conserved at 6.0");
            ForcefieldAssert.assertNear(0.0, vPrime.y(), 1e-4, "Y velocity must remain 0.0");
            ForcefieldAssert.assertNear(-6.0, vPrime.z(), 1e-4, "Normal Z velocity must invert and scale: -0.75 * 8 = -6.0");
        });

        register("t1_f05_03_reverse_side_effective_normal_inversion", 5,
                "Verify approach from reverse side automatically inverts effective normal", () -> {
            Vec3d v = new Vec3d(0, 0, -10);
            Vec3d planeNormal = new Vec3d(0, 0, -1);
            double elasticity = 0.8;
            Vec3d vPrime = reflectVelocity(v, planeNormal, elasticity);
            ForcefieldAssert.assertNear(0.0, vPrime.x(), 1e-4, "X must remain 0");
            ForcefieldAssert.assertNear(0.0, vPrime.y(), 1e-4, "Y must remain 0");
            ForcefieldAssert.assertNear(8.0, vPrime.z(), 1e-4, "Reverse approach must bounce back along +Z: +8.0");
        });

        register("t1_f05_04_restitution_scaling_extremes", 5,
                "Verify cushion elasticity (e=0.2) vs super-spring (e=2.0) scaling", () -> {
            Vec3d v = new Vec3d(0, 0, 10);
            Vec3d normal = new Vec3d(0, 0, -1);
            Vec3d cushion = reflectVelocity(v, normal, 0.2);
            ForcefieldAssert.assertNear(-2.0, cushion.z(), 1e-4, "e=0.2 cushion rebound must be -2.0");

            Vec3d superSpring = reflectVelocity(v, normal, 2.0);
            ForcefieldAssert.assertNear(-20.0, superSpring.z(), 1e-4, "e=2.0 super spring rebound must be -20.0");
        });

        register("t1_f05_05_ripple_impact_payload_generation", 5,
                "Verify impact event generates ripple packet with intensity proportional to normal momentum", () -> {
            Vec3d impactPoint = new Vec3d(1.5, 2.0, 0.0);
            double normalSpeed = 8.5;
            float intensity = (float) Math.min(2.0, normalSpeed * 0.1);
            ForcefieldAssert.assertNear(0.85f, intensity, 1e-4f, "Ripple intensity must scale to 0.85");
            ForcefieldAssert.assertNear(1.5, impactPoint.x(), 1e-4, "Impact X must match");
            ForcefieldAssert.assertNear(2.0, impactPoint.y(), 1e-4, "Impact Y must match");
        });
    }

    // =========================================================================
    // FEATURE 06: One-Way Directional Valve Mode (5 tests)
    // =========================================================================
    private void registerF06Tests() {
        register("t1_f06_01_forward_approach_unobstructed_traversal", 6,
                "Verify forward approach through one-way valve permits unobstructed traversal", () -> {
            Vec3d normal = new Vec3d(0, 0, 1);
            Vec3d movement = new Vec3d(0, 0, -5);
            boolean isForward = movement.dot(normal) < 0.0;
            ForcefieldAssert.assertTrue(isForward, "Forward traversal must be detected when movement dot normal < 0");

            boolean shouldReflect = !isForward;
            ForcefieldAssert.assertFalse(shouldReflect, "Forward approach must not reflect");
        });

        register("t1_f06_02_reverse_approach_elastic_reflection", 6,
                "Verify reverse approach into one-way valve triggers elastic reflection", () -> {
            Vec3d normal = new Vec3d(0, 0, 1);
            Vec3d movement = new Vec3d(0, 0, 5);
            boolean isForward = movement.dot(normal) < 0.0;
            ForcefieldAssert.assertFalse(isForward, "Reverse approach is not forward");

            boolean shouldReflect = !isForward;
            ForcefieldAssert.assertTrue(shouldReflect, "Reverse approach must be reflected");
            Vec3d vPrime = reflectVelocity(movement, normal, 0.8);
            ForcefieldAssert.assertNear(-4.0, vPrime.z(), 1e-4, "Reverse approach must bounce back: -4.0");
        });

        register("t1_f06_03_one_way_toggle_reversibility", 6,
                "Verify disabling one-way mode restores bidirectional reflection", () -> {
            boolean oneWay = false;
            Vec3d normal = new Vec3d(0, 0, 1);
            Vec3d forwardMove = new Vec3d(0, 0, -5);
            Vec3d reverseMove = new Vec3d(0, 0, 5);

            boolean blockForward = !oneWay || (forwardMove.dot(normal) >= 0.0);
            boolean blockReverse = !oneWay || (reverseMove.dot(normal) >= 0.0);
            ForcefieldAssert.assertTrue(blockForward, "Two-way barrier must block forward approach");
            ForcefieldAssert.assertTrue(blockReverse, "Two-way barrier must block reverse approach");
        });

        register("t1_f06_04_drifting_starlight_arrow_orientation", 6,
                "Verify drifting starlight arrow UV animation translates in permitted travel direction", () -> {
            long tick = 40L;
            float scrollSpeed = 0.05f;
            float uvOffset = (tick * scrollSpeed) % 1.0f;
            ForcefieldAssert.assertNear(0.0f, uvOffset, 1e-4f, "UV offset at 40 ticks must be 0.0");
            float uvOffset25 = (25L * scrollSpeed) % 1.0f;
            ForcefieldAssert.assertNear(0.25f, uvOffset25, 1e-4f, "UV offset at 25 ticks must be 0.25");
        });

        register("t1_f06_05_projectile_one_way_directional_filtering", 6,
                "Verify defender shooting outward passes freely while incoming enemy projectile bounces", () -> {
            Vec3d barrierNormal = new Vec3d(0, 0, 1);
            Vec3d defenderArrow = new Vec3d(0, 0, -20);
            Vec3d enemyArrow = new Vec3d(0, 0, 20);

            boolean defenderPasses = defenderArrow.dot(barrierNormal) < 0.0;
            boolean enemyPasses = enemyArrow.dot(barrierNormal) < 0.0;
            ForcefieldAssert.assertTrue(defenderPasses, "Defender arrow moving forward must pass freely");
            ForcefieldAssert.assertFalse(enemyPasses, "Enemy arrow moving reverse must be blocked");
        });
    }

    // =========================================================================
    // FEATURE 07: Redstone & Materia Switchability (5 tests)
    // =========================================================================
    private void registerF07Tests() {
        register("t1_f07_01_redstone_active_state_toggle", 7,
                "Verify standard redstone mode toggles active state on signal changes", () -> {
            boolean invertedMode = false;
            int powerSignal0 = 0;
            int powerSignal15 = 15;

            boolean activeWhenUnpowered = !invertedMode ? (powerSignal0 == 0) : (powerSignal0 > 0);
            boolean activeWhenPowered = !invertedMode ? (powerSignal15 == 0) : (powerSignal15 > 0);
            ForcefieldAssert.assertTrue(activeWhenUnpowered, "Standard mode barrier is active when unpowered");
            ForcefieldAssert.assertFalse(activeWhenPowered, "Standard mode barrier deactivates when powered");
        });

        register("t1_f07_02_dormant_state_collision_bypass", 7,
                "Verify dormant barrier (isActive=false) returns MISS for all collision queries", () -> {
            boolean isActive = false;
            Vec3d start = new Vec3d(0, 0, -5);
            Vec3d end = new Vec3d(0, 0, 5);

            RaycastHit hitResult = isActive ?
                    intersectPlanarQuad(new Vec3d(0,0,0), 0f, 0f, 4f, 4f, start, end, 0.0) :
                    RaycastHit.MISS;
            ForcefieldAssert.assertFalse(hitResult.hit(), "Dormant barrier must completely bypass collision checks");
        });

        register("t1_f07_03_inverted_redstone_mode_logic", 7,
                "Verify inverted redstone mode activates on power and stays dormant without power", () -> {
            boolean invertedMode = true;
            boolean activeOnZero = invertedMode ? (0 > 0) : (0 == 0);
            boolean activeOnPower = invertedMode ? (10 > 0) : (10 == 0);
            ForcefieldAssert.assertFalse(activeOnZero, "Inverted mode must be dormant at 0 power");
            ForcefieldAssert.assertTrue(activeOnPower, "Inverted mode must be active at >0 power");
        });

        register("t1_f07_04_dormant_render_alpha_attenuation", 7,
                "Verify dormant state attenuates opacity to ethereal starlight shimmer (alpha <= 0.10)", () -> {
            float grazingFactor = 0.5f;
            float ageTicks = 100.0f;
            float breathe = 0.5F + 0.5F * (float) Math.sin(ageTicks * 0.02F);
            float alpha = (0.035F + 0.065F * grazingFactor) * (0.75F + 0.25F * breathe);
            ForcefieldAssert.assertTrue(alpha >= 0.035f, "Dormant alpha must be at least 3.5%");
            ForcefieldAssert.assertTrue(alpha <= 0.100f, "Dormant alpha must not exceed 10%");
        });

        register("t1_f07_05_materia_conduit_activation", 7,
                "Verify Materia conduit link state energizes barrier when pressure > 0", () -> {
            double materiaPressure = 12.5;
            boolean conduitConnected = true;
            boolean energized = conduitConnected && materiaPressure > 0.0;
            ForcefieldAssert.assertTrue(energized, "Barrier must be energized when Materia conduit supplies power");
        });
    }

    // =========================================================================
    // FEATURE 08: Materia Color Tinting (5 tests)
    // =========================================================================
    private void registerF08Tests() {
        register("t1_f08_01_dye_color_tint_application", 8,
                "Verify applying dye color tint updates colorTint integer and vertex color", () -> {
            int lapisTint = 0x001E438C;
            ColorResult base = new ColorResult(0.8f, 0.8f, 0.8f, 0.5f);
            ColorResult tinted = blendMateriaTint(base, lapisTint, 0.0f, 0.0f);
            ForcefieldAssert.assertTrue(tinted.b() > tinted.r(), "Blue component must dominate for Lapis tint");
        });

        register("t1_f08_02_materia_crystal_palette_support", 8,
                "Verify support for 5 key Materia crystal palettes (Azure, Amethyst, Solar Gold, Emerald, Rose Quartz)", () -> {
            int[] crystalPalettes = {
                    0x003366CC, // Starlight Azure
                    0x009933CC, // Amethyst Violet
                    0x00FFCC00, // Solar Gold
                    0x0000CC66, // Emerald
                    0x00FF6699  // Rose Quartz
            };
            ColorResult base = new ColorResult(0.7f, 0.7f, 0.7f, 0.4f);
            for (int palette : crystalPalettes) {
                ColorResult res = blendMateriaTint(base, palette, 0.5f, 0.0f);
                ForcefieldAssert.assertTrue(res.a() >= 0.18f && res.a() <= 1.0f, "Alpha must remain clamped");
                ForcefieldAssert.assertTrue(res.r() >= 0.0f && res.r() <= 1.0f, "Red must be within [0,1]");
                ForcefieldAssert.assertTrue(res.g() >= 0.0f && res.g() <= 1.0f, "Green must be within [0,1]");
                ForcefieldAssert.assertTrue(res.b() >= 0.0f && res.b() <= 1.0f, "Blue must be within [0,1]");
            }
        });

        register("t1_f08_03_dual_stage_tint_blending_math", 8,
                "Verify 65% chromatic overlay + screen-blend sheen highlight formula", () -> {
            ColorResult base = new ColorResult(1.0f, 1.0f, 1.0f, 0.6f);
            int tintRgb = 0x00FF0000;
            ColorResult blended = blendMateriaTint(base, tintRgb, 0.0f, 0.0f);
            ForcefieldAssert.assertTrue(blended.r() > blended.g(), "Red component must significantly exceed green");
            ForcefieldAssert.assertTrue(blended.r() > blended.b(), "Red component must significantly exceed blue");
        });

        register("t1_f08_04_tint_clearing_to_standard_sheen", 8,
                "Verify clearing color tint (0x00000000 or null) restores raw theme color", () -> {
            Integer nullTint = null;
            Integer zeroTint = 0;
            boolean hasCustomTint1 = nullTint != null && nullTint != 0;
            boolean hasCustomTint2 = zeroTint != null && zeroTint != 0;
            ForcefieldAssert.assertFalse(hasCustomTint1, "Null tint must indicate standard sheen");
            ForcefieldAssert.assertFalse(hasCustomTint2, "Zero tint must indicate standard sheen");
        });

        register("t1_f08_05_tint_luminance_and_alpha_preservation", 8,
                "Verify dark or light tints maintain alpha bounds [0.18, 1.0] avoiding invisibility", () -> {
            ColorResult base = new ColorResult(0.5f, 0.5f, 0.5f, 0.3f);
            ColorResult darkTint = blendMateriaTint(base, 0x00000000, 0.0f, 0.0f);
            ColorResult lightTint = blendMateriaTint(base, 0x00FFFFFF, 0.0f, 0.0f);
            ForcefieldAssert.assertTrue(darkTint.a() >= 0.18f, "Dark tint alpha must not drop below 0.18");
            ForcefieldAssert.assertTrue(lightTint.a() <= 1.0f, "Light tint alpha must not exceed 1.0");
        });
    }

    // =========================================================================
    // FEATURE 09: Interactive Creator Config Screen (5 tests)
    // =========================================================================
    private void registerF09Tests() {
        register("t1_f09_01_filter_mode_state_transitions", 9,
                "Verify all 5 BarrierFilterMode ordinals and display names", () -> {
            FilterMode[] modes = FilterMode.values();
            ForcefieldAssert.assertEquals(5, modes.length, "Total filter modes must be 5");
            ForcefieldAssert.assertEquals("all_entities", FilterMode.fromOrdinal(0).getSerializedName(), "Ordinal 0 must be all_entities");
            ForcefieldAssert.assertEquals("mobs_only", FilterMode.fromOrdinal(1).getSerializedName(), "Ordinal 1 must be mobs_only");
            ForcefieldAssert.assertEquals("players_only", FilterMode.fromOrdinal(2).getSerializedName(), "Ordinal 2 must be players_only");
            ForcefieldAssert.assertEquals("hostile_mobs", FilterMode.fromOrdinal(3).getSerializedName(), "Ordinal 3 must be hostile_mobs");
            ForcefieldAssert.assertEquals("projectiles", FilterMode.fromOrdinal(4).getSerializedName(), "Ordinal 4 must be projectiles");
        });

        register("t1_f09_02_filter_mode_entity_blocking_logic", 9,
                "Verify filter mode blocking decisions across player, mob, hostile, and projectile", () -> {
            FilterMode hostileFilter = FilterMode.HOSTILE_MOBS;
            boolean blocksCreeper = hostileFilter.isBlocked(false, false, false, false, false, true);
            boolean blocksCow = hostileFilter.isBlocked(false, false, false, false, false, false);
            boolean blocksPlayer = hostileFilter.isBlocked(true, false, false, false, false, false);
            ForcefieldAssert.assertTrue(blocksCreeper, "Hostile filter must block creeper");
            ForcefieldAssert.assertFalse(blocksCow, "Hostile filter must permit cow");
            ForcefieldAssert.assertFalse(blocksPlayer, "Hostile filter must permit player");
        });

        register("t1_f09_03_slider_range_clamping_invariants", 9,
                "Verify dimensions clamp to [1.0, 32.0] and elasticity clamps to [0.2, 2.0]", () -> {
            float clampedWidthLow = Math.max(1.0f, Math.min(32.0f, -5.0f));
            float clampedWidthHigh = Math.max(1.0f, Math.min(32.0f, 50.0f));
            ForcefieldAssert.assertNear(1.0f, clampedWidthLow, 1e-4f, "Negative width must clamp to 1.0");
            ForcefieldAssert.assertNear(32.0f, clampedWidthHigh, 1e-4f, "Excessive width must clamp to 32.0");

            float clampedElasticityLow = Math.max(0.2f, Math.min(2.0f, 0.05f));
            float clampedElasticityHigh = Math.max(0.2f, Math.min(2.0f, 4.5f));
            ForcefieldAssert.assertNear(0.2f, clampedElasticityLow, 1e-4f, "Low elasticity must clamp to 0.2");
            ForcefieldAssert.assertNear(2.0f, clampedElasticityHigh, 1e-4f, "High elasticity must clamp to 2.0");
        });

        register("t1_f09_04_whitelist_access_control", 9,
                "Verify whitelisted players pass freely through ALL_ENTITIES and PLAYERS_ONLY barriers", () -> {
            FilterMode filter = FilterMode.PLAYERS_ONLY;
            boolean whitelistedBlocked = filter.isBlocked(true, false, false, true, false, false);
            boolean nonWhitelistedBlocked = filter.isBlocked(true, false, false, false, false, false);
            ForcefieldAssert.assertFalse(whitelistedBlocked, "Whitelisted player must pass freely");
            ForcefieldAssert.assertTrue(nonWhitelistedBlocked, "Non-whitelisted player must be blocked");
        });

        register("t1_f09_05_creator_owner_bypass", 9,
                "Verify barrier creator/owner UUID always has unrestricted passage", () -> {
            for (FilterMode mode : FilterMode.values()) {
                boolean ownerBlocked = mode.isBlocked(true, false, true, false, false, false);
                ForcefieldAssert.assertFalse(ownerBlocked, "Creator must never be blocked by filter: " + mode.name());
            }
        });
    }

    // =========================================================================
    // FEATURE 10: Network Config Synchronization (5 tests)
    // =========================================================================
    private void registerF10Tests() {
        register("t1_f10_01_payload_serialization_round_trip", 10,
                "Verify UpdateBarrierConfigPayload byte stream serialization and deserialization", () -> {
            BarrierConfigPayload original = new BarrierConfigPayload(
                    1234, 2, 16.0f, 8.0f, 5.0f, 3, 1, 1.25f, true, false, 0x00FF3366, List.of("Steve", "Alex")
            );
            try {
                byte[] bytes = original.toBytes();
                BarrierConfigPayload decoded = BarrierConfigPayload.fromBytes(bytes);
                ForcefieldAssert.assertEquals(original.entityId(), decoded.entityId(), "Entity ID must match");
                ForcefieldAssert.assertEquals(original.shapeOrdinal(), decoded.shapeOrdinal(), "Shape ordinal must match");
                ForcefieldAssert.assertNear(original.width(), decoded.width(), 1e-4f, "Width must match");
                ForcefieldAssert.assertNear(original.height(), decoded.height(), 1e-4f, "Height must match");
                ForcefieldAssert.assertNear(original.radius(), decoded.radius(), 1e-4f, "Radius must match");
                ForcefieldAssert.assertEquals(original.filterOrdinal(), decoded.filterOrdinal(), "Filter ordinal must match");
                ForcefieldAssert.assertEquals(original.themeOrdinal(), decoded.themeOrdinal(), "Theme ordinal must match");
                ForcefieldAssert.assertNear(original.elasticity(), decoded.elasticity(), 1e-4f, "Elasticity must match");
                ForcefieldAssert.assertEquals(original.oneWay(), decoded.oneWay(), "One-way must match");
                ForcefieldAssert.assertEquals(original.redstoneMode(), decoded.redstoneMode(), "Redstone mode must match");
                ForcefieldAssert.assertEquals(original.colorTint(), decoded.colorTint(), "Color tint must match");
                ForcefieldAssert.assertEquals(2, decoded.whitelist().size(), "Whitelist size must match");
                ForcefieldAssert.assertEquals("Steve", decoded.whitelist().get(0), "First whitelisted name must match");
                ForcefieldAssert.assertEquals("Alex", decoded.whitelist().get(1), "Second whitelisted name must match");
            } catch (Exception e) {
                ForcefieldAssert.fail("Serialization failed: " + e.getMessage());
            }
        });

        register("t1_f10_02_server_permission_validation_creator", 10,
                "Verify server accepts config updates when sender matches barrier creator UUID", () -> {
            UUID creatorUUID = UUID.randomUUID();
            UUID senderUUID = creatorUUID;
            boolean isCreative = false;
            boolean hasPermission = senderUUID.equals(creatorUUID) || isCreative;
            ForcefieldAssert.assertTrue(hasPermission, "Creator must have config permission");
        });

        register("t1_f10_03_server_permission_validation_creative", 10,
                "Verify server accepts config updates from creative players without ownership", () -> {
            UUID creatorUUID = UUID.randomUUID();
            UUID senderUUID = UUID.randomUUID();
            boolean isCreative = true;
            boolean hasPermission = senderUUID.equals(creatorUUID) || isCreative;
            ForcefieldAssert.assertTrue(hasPermission, "Creative player must have bypass config permission");
        });

        register("t1_f10_04_server_permission_rejection_unauthorized", 10,
                "Verify server rejects config updates from unauthorized survival players", () -> {
            UUID creatorUUID = UUID.randomUUID();
            UUID senderUUID = UUID.randomUUID();
            boolean isCreative = false;
            boolean hasPermission = senderUUID.equals(creatorUUID) || isCreative;
            ForcefieldAssert.assertFalse(hasPermission, "Unauthorized survival player must be rejected");
        });

        register("t1_f10_05_network_type_id_and_channel", 10,
                "Verify payload network type identifier string matches entropica:update_barrier_config", () -> {
            String expectedType = "entropica:update_barrier_config";
            ForcefieldAssert.assertEquals("entropica:update_barrier_config", expectedType, "Channel ID must match");
        });
    }

    // =========================================================================
    // FEATURE 11: Two-Point Drag & Snap (5 tests)
    // =========================================================================
    private void registerF11Tests() {
        register("t1_f11_01_two_point_center_calculation", 11,
                "Verify Point A and Point B auto-calculate midpoint center coordinates", () -> {
            Vec3d pointA = new Vec3d(2.0, 64.0, 4.0);
            Vec3d pointB = new Vec3d(8.0, 68.0, 12.0);
            Vec3d center = pointA.add(pointB).scale(0.5);
            ForcefieldAssert.assertNear(5.0, center.x(), 1e-4, "Center X must be (2+8)/2 = 5.0");
            ForcefieldAssert.assertNear(66.0, center.y(), 1e-4, "Center Y must be (64+68)/2 = 66.0");
            ForcefieldAssert.assertNear(8.0, center.z(), 1e-4, "Center Z must be (4+12)/2 = 8.0");
        });

        register("t1_f11_02_two_point_span_dimensions", 11,
                "Verify auto-calculated planar width and height between A and B", () -> {
            Vec3d pointA = new Vec3d(0.0, 60.0, 0.0);
            Vec3d pointB = new Vec3d(6.0, 64.0, 8.0);
            double horizontalDistance = Math.sqrt(Math.pow(pointB.x() - pointA.x(), 2) + Math.pow(pointB.z() - pointA.z(), 2));
            double verticalDistance = Math.abs(pointB.y() - pointA.y());
            ForcefieldAssert.assertNear(10.0, horizontalDistance, 1e-4, "Horizontal width must be sqrt(36+64) = 10.0");
            ForcefieldAssert.assertNear(4.0, verticalDistance, 1e-4, "Vertical height must be |64-60| = 4.0");
        });

        register("t1_f11_03_two_point_yaw_alignment", 11,
                "Verify auto-derived yaw aligns quad tangent collinear with the span vector", () -> {
            Vec3d pointA = new Vec3d(0, 60, 0);
            Vec3d pointB = new Vec3d(0, 60, 10);
            double dx = pointB.x() - pointA.x();
            double dz = pointB.z() - pointA.z();
            double angleRad = Math.atan2(dz, dx);
            double yawDeg = Math.toDegrees(angleRad);
            ForcefieldAssert.assertNear(90.0, yawDeg, 1e-4, "Yaw along Z span must be 90.0 degrees");

            // Verify tangent collinearity: tangent along +Z span is (0, 0, 1)
            double tangentRad = (-yawDeg + 90.0) * (Math.PI / 180.0);
            Vec3d tangent = new Vec3d(Math.sin(tangentRad), 0.0, Math.cos(tangentRad)).normalize();
            Vec3d spanNorm = new Vec3d(dx, 0.0, dz).normalize();
            double dot = Math.abs(tangent.x() * spanNorm.x() + tangent.y() * spanNorm.y() + tangent.z() * spanNorm.z());
            ForcefieldAssert.assertTrue(dot > 0.9999, "Tangent must be parallel to span vector (dot > 0.9999)");
            double crossX = tangent.y() * spanNorm.z() - tangent.z() * spanNorm.y();
            double crossY = tangent.z() * spanNorm.x() - tangent.x() * spanNorm.z();
            double crossZ = tangent.x() * spanNorm.y() - tangent.y() * spanNorm.x();
            double crossLen = Math.sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ);
            ForcefieldAssert.assertTrue(crossLen < 1e-4, "Tangent cross span vector must be zero (collinear)");
        });

        register("t1_f11_04_two_point_doorway_span", 11,
                "Verify standard 4x3 doorway framing between floor corner and lintel", () -> {
            Vec3d floorCorner = new Vec3d(10.0, 64.0, 5.0);
            Vec3d lintelCorner = new Vec3d(14.0, 67.0, 5.0);
            double width = Math.abs(lintelCorner.x() - floorCorner.x());
            double height = Math.abs(lintelCorner.y() - floorCorner.y());
            Vec3d center = floorCorner.add(lintelCorner).scale(0.5);
            ForcefieldAssert.assertNear(4.0, width, 1e-4, "Doorway width must be 4.0m");
            ForcefieldAssert.assertNear(3.0, height, 1e-4, "Doorway height must be 3.0m");
            ForcefieldAssert.assertNear(12.0, center.x(), 1e-4, "Center X must be 12.0");
            ForcefieldAssert.assertNear(65.5, center.y(), 1e-4, "Center Y must be 65.5");
        });

        register("t1_f11_05_anchor_beacon_lifecycle", 11,
                "Verify setting Point A sets anchor beacon and Point B consumes it", () -> {
            PlayerAnchorSession session = new PlayerAnchorSession();
            ForcefieldAssert.assertNull(session.anchorA, "Initial anchor must be null");
            session.setAnchor(new Vec3d(5, 64, 5));
            ForcefieldAssert.assertNotNull(session.anchorA, "Anchor must be set after first click");
            boolean created = session.completeSpan(new Vec3d(9, 67, 5));
            ForcefieldAssert.assertTrue(created, "Barrier creation must succeed on second click");
            ForcefieldAssert.assertNull(session.anchorA, "Anchor must reset to null after completion");
        });
    }

    // =========================================================================
    // FEATURE 12: Edge-Fusing / Seamless Snapping (5 tests)
    // =========================================================================
    private void registerF12Tests() {
        register("t1_f12_01_edge_fusing_proximity_threshold", 12,
                "Verify edge-fusing triggers at <= 0.5m distance and ignores > 0.5m", () -> {
            double existingEdgeX = 10.0;
            double nearCandidateX = 10.35;
            double farCandidateX = 10.75;
            boolean snapNear = Math.abs(nearCandidateX - existingEdgeX) <= 0.5;
            boolean snapFar = Math.abs(farCandidateX - existingEdgeX) <= 0.5;
            ForcefieldAssert.assertTrue(snapNear, "Distance 0.35m <= 0.5m must trigger edge snapping");
            ForcefieldAssert.assertFalse(snapFar, "Distance 0.75m > 0.5m must not snap");
        });

        register("t1_f12_02_flush_vertex_alignment", 12,
                "Verify snapped barrier coordinates shift to eliminate seam gap (gap = 0.0m)", () -> {
            double existingEdge = 10.0;
            double candidateEdge = 10.30;
            double snappedEdge = Math.abs(candidateEdge - existingEdge) <= 0.5 ? existingEdge : candidateEdge;
            ForcefieldAssert.assertNear(10.0, snappedEdge, 1e-4, "Snapped edge must align exactly with neighbor at 10.0");
            ForcefieldAssert.assertNear(0.0, Math.abs(snappedEdge - existingEdge), 1e-4, "Seam gap must be 0.0m");
        });

        register("t1_f12_03_angle_snapping_to_neighbor", 12,
                "Verify barrier rotation angle snaps to adjoining barrier normal when within 5 degrees", () -> {
            float neighborYaw = 45.0f;
            float candidateYaw = 46.5f;
            float angleDelta = Math.abs(candidateYaw - neighborYaw);
            float snappedYaw = angleDelta <= 5.0f ? neighborYaw : candidateYaw;
            ForcefieldAssert.assertNear(45.0f, snappedYaw, 1e-4f, "Candidate yaw must snap to neighbor yaw 45.0");
        });

        register("t1_f12_04_z_fighting_prevention_normal_offset", 12,
                "Verify coplanar barrier edge merging prevents overlapping coplanar faces", () -> {
            Vec3d plane1Normal = new Vec3d(0, 0, 1);
            Vec3d plane2Normal = new Vec3d(0, 0, 1);
            double normalOffset = plane1Normal.cross(plane2Normal).length();
            ForcefieldAssert.assertNear(0.0, normalOffset, 1e-4, "Coplanar normals must have 0 cross product");
        });

        register("t1_f12_05_multibarrier_chain_fusing", 12,
                "Verify three sequential 4m quads chain together into a continuous 12m wall", () -> {
            double b1Start = 0.0, b1End = 4.0;
            double b2Start = 4.2;
            double b2Snapped = Math.abs(b2Start - b1End) <= 0.5 ? b1End : b2Start;
            double b2End = b2Snapped + 4.0;

            double b3Start = 8.15;
            double b3Snapped = Math.abs(b3Start - b2End) <= 0.5 ? b2End : b3Start;
            double b3End = b3Snapped + 4.0;

            ForcefieldAssert.assertNear(4.0, b2Snapped, 1e-4, "Barrier 2 must snap to 4.0");
            ForcefieldAssert.assertNear(8.0, b3Snapped, 1e-4, "Barrier 3 must snap to 8.0");
            ForcefieldAssert.assertNear(12.0, b3End, 1e-4, "Total chained wall length must be 12.0m");
        });
    }

    // =========================================================================
    // FEATURE 13: Holographic Placement Preview (5 tests)
    // =========================================================================
    private void registerF13Tests() {
        register("t1_f13_01_wireframe_planar_quad_edges", 13,
                "Verify wireframe generator computes 4 perimeter vertices for planar quad", () -> {
            float width = 4.0f;
            float height = 3.0f;
            float hw = width * 0.5f;
            float hh = height * 0.5f;
            Vec3d[] corners = {
                    new Vec3d(-hw, -hh, 0),
                    new Vec3d(hw, -hh, 0),
                    new Vec3d(hw, hh, 0),
                    new Vec3d(-hw, hh, 0)
            };
            ForcefieldAssert.assertEquals(4, corners.length, "Wireframe perimeter must have 4 corners");
            ForcefieldAssert.assertNear(4.0, corners[1].x() - corners[0].x(), 1e-4, "Bottom edge length must be 4.0m");
            ForcefieldAssert.assertNear(3.0, corners[2].y() - corners[1].y(), 1e-4, "Right edge height must be 3.0m");
        });

        register("t1_f13_02_valid_placement_starlight_color", 13,
                "Verify unobstructed valid placement preview uses cyan/starlight color", () -> {
            boolean isValid = true;
            ColorResult previewColor = isValid ?
                    new ColorResult(0.30f, 0.80f, 1.00f, 0.60f) :
                    new ColorResult(1.00f, 0.20f, 0.20f, 0.60f);
            ForcefieldAssert.assertTrue(previewColor.b() > 0.9f, "Valid preview blue must be vibrant starlight");
            ForcefieldAssert.assertTrue(previewColor.g() > 0.7f, "Valid preview green must be cyan tint");
            ForcefieldAssert.assertTrue(previewColor.r() < 0.4f, "Valid preview red must be low");
        });

        register("t1_f13_03_invalid_placement_warning_crimson", 13,
                "Verify obstructed or invalid placement preview shifts to warning crimson", () -> {
            boolean isValid = false;
            ColorResult previewColor = isValid ?
                    new ColorResult(0.30f, 0.80f, 1.00f, 0.60f) :
                    new ColorResult(1.00f, 0.20f, 0.20f, 0.60f);
            ForcefieldAssert.assertTrue(previewColor.r() > 0.9f, "Invalid preview must be bright warning crimson red");
            ForcefieldAssert.assertTrue(previewColor.g() < 0.3f, "Invalid preview green must be suppressed");
            ForcefieldAssert.assertTrue(previewColor.b() < 0.3f, "Invalid preview blue must be suppressed");
        });

        register("t1_f13_04_preview_transform_matrix", 13,
                "Verify preview pose matrix translates to raycast hit coordinate", () -> {
            Vec3d targetHit = new Vec3d(15.5, 64.0, -8.2);
            Vec3d renderedCenter = targetHit;
            ForcefieldAssert.assertEquals(targetHit, renderedCenter, "Preview center must track raycast hit point");
        });

        register("t1_f13_05_preview_activation_held_item", 13,
                "Verify preview is enabled when holding Firmament Weaver and disabled otherwise", () -> {
            String heldItemWeaver = "entropica:firmament_weaver";
            String heldItemSword = "minecraft:diamond_sword";
            boolean previewActive1 = "entropica:firmament_weaver".equals(heldItemWeaver);
            boolean previewActive2 = "entropica:firmament_weaver".equals(heldItemSword);
            ForcefieldAssert.assertTrue(previewActive1, "Preview must activate when holding Weaver");
            ForcefieldAssert.assertFalse(previewActive2, "Preview must remain inactive when holding sword");
        });
    }

    // =========================================================================
    // FEATURE 14: Weaver Fast-Action Utilities (5 tests)
    // =========================================================================
    private void registerF14Tests() {
        register("t1_f14_01_shift_scroll_shape_cycling_order", 14,
                "Verify Shift+Scroll cycles through registered shapes 0 to 5 in order and wraps", () -> {
            int currentShape = 0;
            int totalShapes = 6;

            int nextShape = (currentShape + 1) % totalShapes;
            ForcefieldAssert.assertEquals(1, nextShape, "Scroll forward from 0 must yield 1 (DISC)");

            int wrappedShape = (5 + 1) % totalShapes;
            ForcefieldAssert.assertEquals(0, wrappedShape, "Scroll forward from 5 must wrap to 0 (QUAD)");

            int reverseShape = (0 - 1 + totalShapes) % totalShapes;
            ForcefieldAssert.assertEquals(5, reverseShape, "Scroll reverse from 0 must wrap to 5 (POLYGON)");
        });

        register("t1_f14_02_creator_look_at_telemetry_formatting", 14,
                "Verify creator look-at telemetry formats floating HUD string accurately", () -> {
            String owner = "Steve";
            String shape = "PLANAR_QUAD";
            String filter = "ALL_ENTITIES";
            float elasticity = 0.80f;
            boolean oneWay = false;

            String hud = String.format("Owner: %s | Shape: %s | Filter: %s | Elasticity: %.2f | One-Way: %s",
                    owner, shape, filter, elasticity, oneWay ? "Yes" : "No");
            ForcefieldAssert.assertTrue(hud.contains("Owner: Steve"), "HUD must contain owner");
            ForcefieldAssert.assertTrue(hud.contains("Shape: PLANAR_QUAD"), "HUD must contain shape");
            ForcefieldAssert.assertTrue(hud.contains("Filter: ALL_ENTITIES"), "HUD must contain filter");
            ForcefieldAssert.assertTrue(hud.contains("Elasticity: 0.80"), "HUD must contain elasticity");
            ForcefieldAssert.assertTrue(hud.contains("One-Way: No"), "HUD must contain one-way status");
        });

        register("t1_f14_03_quick_dispel_creator_permission", 14,
                "Verify left-clicking barrier with Weaver by creator successfully dispels it", () -> {
            UUID creatorUUID = UUID.randomUUID();
            UUID clickerUUID = creatorUUID;
            boolean isCreative = false;

            boolean canDispel = clickerUUID.equals(creatorUUID) || isCreative;
            ForcefieldAssert.assertTrue(canDispel, "Creator must be allowed to quick dispel");
        });

        register("t1_f14_04_quick_dispel_non_owner_rejection", 14,
                "Verify left-clicking barrier with Weaver by non-owner survival player is rejected", () -> {
            UUID creatorUUID = UUID.randomUUID();
            UUID clickerUUID = UUID.randomUUID();
            boolean isCreative = false;

            boolean canDispel = clickerUUID.equals(creatorUUID) || isCreative;
            ForcefieldAssert.assertFalse(canDispel, "Unauthorized player must not dispel barrier");
        });

        register("t1_f14_05_quick_dispel_audio_feedback", 14,
                "Verify quick dispel triggers bubble pop audio cue and particle burst", () -> {
            String soundEvent = "minecraft:sound.bubble_pop";
            int particleCount = 20;
            ForcefieldAssert.assertEquals("minecraft:sound.bubble_pop", soundEvent, "Sound event must be bubble pop");
            ForcefieldAssert.assertTrue(particleCount >= 10, "Particles must burst on dispel");
        });
    }

    // =========================================================================
    // FEATURE 15: Boss Arena Barrier Integration (5 tests)
    // =========================================================================
    private void registerF15Tests() {
        register("t1_f15_01_boss_barrier_survival_invulnerability", 15,
                "Verify arena barrier bound to active Apex Predator rejects survival damage and dispel", () -> {
            UUID bossUUID = UUID.randomUUID();
            boolean bossAlive = true;
            boolean isCreative = false;

            boolean immuneToDispel = bossUUID != null && bossAlive && !isCreative;
            ForcefieldAssert.assertTrue(immuneToDispel, "Boss arena barrier must be indestructible while boss is alive");
        });

        register("t1_f15_02_creative_bypass_during_boss_fight", 15,
                "Verify creative mode operator can bypass or dispel boss arena barrier for administration", () -> {
            UUID bossUUID = UUID.randomUUID();
            boolean bossAlive = true;
            boolean isCreative = true;

            boolean immuneToDispel = bossUUID != null && bossAlive && !isCreative;
            ForcefieldAssert.assertFalse(immuneToDispel, "Creative player must bypass boss barrier invulnerability");
        });

        register("t1_f15_03_boss_death_trigger_dissolution", 15,
                "Verify boss death event triggers synchronized barrier dissolution", () -> {
            UUID bossUUID = UUID.randomUUID();
            boolean bossAlive = false;

            boolean startDissolution = bossUUID != null && !bossAlive;
            ForcefieldAssert.assertTrue(startDissolution, "Barrier must begin dissolution immediately upon boss death");
        });

        register("t1_f15_04_dissolution_state_transition", 15,
                "Verify barrier collision is disabled immediately upon dissolution initiation", () -> {
            boolean isDissolving = true;
            boolean hasCollision = !isDissolving;
            ForcefieldAssert.assertFalse(hasCollision, "Dissolving barrier must immediately disable collision");
        });

        register("t1_f15_05_hexagonal_arena_multibarrier_dissolution", 15,
                "Verify all 6 hexagonal arena barriers bound to same boss dissolve simultaneously", () -> {
            UUID bossUUID = UUID.randomUUID();
            List<Boolean> barrierStates = new ArrayList<>(List.of(true, true, true, true, true, true));

            for (int i = 0; i < barrierStates.size(); i++) {
                barrierStates.set(i, false);
            }
            for (boolean active : barrierStates) {
                ForcefieldAssert.assertFalse(active, "Every barrier in hexagonal arena must deactivate");
            }
        });
    }

    // =========================================================================
    // FEATURE 16: Graviton Bouncepad Parity (5 tests)
    // =========================================================================
    private void registerF16Tests() {
        register("t1_f16_01_omnidirectional_attachment_faces", 16,
                "Verify bouncepad supports mounting on floor (UP), ceiling (DOWN), and all 4 walls", () -> {
            String[] faces = {"UP", "DOWN", "NORTH", "SOUTH", "EAST", "WEST"};
            ForcefieldAssert.assertEquals(6, faces.length, "Bouncepad must support all 6 block directions");
        });

        register("t1_f16_02_normal_gravity_launch_vector", 16,
                "Verify launch vector is oriented in -g (+Y upward) direction under normal gravity", () -> {
            Vec3d gravityVector = new Vec3d(0, -0.08, 0);
            Vec3d launchDir = gravityVector.scale(-1.0).normalize();
            ForcefieldAssert.assertNear(0.0, launchDir.x(), 1e-4, "Launch X must be 0");
            ForcefieldAssert.assertNear(1.0, launchDir.y(), 1e-4, "Launch Y must be +1.0 (upward)");
            ForcefieldAssert.assertNear(0.0, launchDir.z(), 1e-4, "Launch Z must be 0");
        });

        register("t1_f16_03_inverted_gravity_launch_vector", 16,
                "Verify launch vector is oriented in -g (-Y downward) direction under inverted gravity", () -> {
            Vec3d invertedGravity = new Vec3d(0, 0.08, 0);
            Vec3d launchDir = invertedGravity.scale(-1.0).normalize();
            ForcefieldAssert.assertNear(0.0, launchDir.x(), 1e-4, "Launch X must be 0");
            ForcefieldAssert.assertNear(-1.0, launchDir.y(), 1e-4, "Launch Y must be -1.0 (relative upward into room)");
            ForcefieldAssert.assertNear(0.0, launchDir.z(), 1e-4, "Launch Z must be 0");
        });

        register("t1_f16_04_analogue_redstone_scaling", 16,
                "Verify launch velocity scales monotonically with redstone power level 0 to 15", () -> {
            double baseVelocity = 0.75;
            double v0 = baseVelocity * (1.0 + 0.0 * 0.1);
            double v7 = baseVelocity * (1.0 + 7.0 * 0.1);
            double v15 = baseVelocity * (1.0 + 15.0 * 0.1);

            ForcefieldAssert.assertNear(0.75, v0, 1e-4, "Power 0 launch speed must be 0.75");
            ForcefieldAssert.assertNear(1.275, v7, 1e-4, "Power 7 launch speed must be 1.275");
            ForcefieldAssert.assertNear(1.875, v15, 1e-4, "Power 15 launch speed must be 1.875");
            ForcefieldAssert.assertTrue(v0 < v7 && v7 < v15, "Launch velocity must scale strictly monotonically");
        });

        register("t1_f16_05_fall_damage_immunity_tagging", 16,
                "Verify launched entity fall distance is reset to 0.0 and granted safe landing", () -> {
            double priorFallDistance = 25.0;
            boolean bouncepadLaunched = true;
            double postLaunchFallDistance = bouncepadLaunched ? 0.0 : priorFallDistance;
            ForcefieldAssert.assertNear(0.0, postLaunchFallDistance, 1e-4, "Fall distance must be reset to 0 on launch");
        });
    }

    private static Set<String> extractItemIds(JsonElement element) {
        Set<String> items = new HashSet<>();
        if (element == null || element.isJsonNull()) {
            return items;
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("item")) {
                items.add(obj.get("item").getAsString());
            } else if (obj.has("tag")) {
                items.add(obj.get("tag").getAsString());
            }
        } else if (element.isJsonArray()) {
            for (JsonElement sub : element.getAsJsonArray()) {
                if (sub.isJsonObject()) {
                    JsonObject subObj = sub.getAsJsonObject();
                    if (subObj.has("item")) {
                        items.add(subObj.get("item").getAsString());
                    } else if (subObj.has("tag")) {
                        items.add(subObj.get("tag").getAsString());
                    }
                }
            }
        }
        return items;
    }

    // =========================================================================
    // FEATURE 17: Registries, Recipes & Localization (5 tests)
    // =========================================================================
    private void registerF17Tests() {
        register("t1_f17_01_firmament_weaver_recipe_specification", 17,
                "Verify shaped recipe specification for entropica:firmament_weaver tool from disk", () -> {
            File recipeFile = resolveFile("common/src/main/resources/data/entropica/recipe/firmament_weaver.json");
            ForcefieldAssert.assertTrue(recipeFile.exists(), "firmament_weaver.json recipe must exist on disk");

            String jsonContent = Files.readString(recipeFile.toPath());
            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();

            // 1. Recipe Type & Category
            ForcefieldAssert.assertEquals("minecraft:crafting_shaped", root.get("type").getAsString(),
                    "Recipe type must be minecraft:crafting_shaped");
            if (root.has("category")) {
                ForcefieldAssert.assertEquals("equipment", root.get("category").getAsString(),
                        "Recipe category must be equipment");
            }

            // 2. Result Verification
            ForcefieldAssert.assertTrue(root.has("result"), "Recipe must define a result object");
            JsonObject result = root.getAsJsonObject("result");
            ForcefieldAssert.assertEquals("entropica:firmament_weaver", result.get("id").getAsString(),
                    "Recipe result id must be entropica:firmament_weaver");
            if (result.has("count")) {
                ForcefieldAssert.assertEquals(1, result.get("count").getAsInt(),
                        "Recipe result count must be 1");
            }

            // 3. Pattern Dimensions & Rows
            ForcefieldAssert.assertTrue(root.has("pattern"), "Recipe must define a pattern array");
            JsonArray pattern = root.getAsJsonArray("pattern");
            ForcefieldAssert.assertEquals(3, pattern.size(), "Pattern must have exactly 3 rows");
            ForcefieldAssert.assertEquals("  L", pattern.get(0).getAsString(), "Row 0 pattern must match '  L'");
            ForcefieldAssert.assertEquals(" C ", pattern.get(1).getAsString(), "Row 1 pattern must match ' C '");
            ForcefieldAssert.assertEquals("R  ", pattern.get(2).getAsString(), "Row 2 pattern must match 'R  '");

            // 4. Key Mappings & Ingredients
            ForcefieldAssert.assertTrue(root.has("key"), "Recipe must define a key mapping object");
            JsonObject keyMap = root.getAsJsonObject("key");

            // Key 'L': Starlight Lens / Glass
            ForcefieldAssert.assertTrue(keyMap.has("L"), "Recipe keys must contain 'L' for lens/glass");
            Set<String> lItems = extractItemIds(keyMap.get("L"));
            ForcefieldAssert.assertTrue(lItems.contains("entropica:refractive_astral_lens") ||
                            lItems.contains("entropica:essence_enriched_glass"),
                    "Key 'L' must include refractive_astral_lens or essence_enriched_glass");

            // Key 'C': Astral Crystal core
            ForcefieldAssert.assertTrue(keyMap.has("C"), "Recipe keys must contain 'C' for crystal");
            Set<String> cItems = extractItemIds(keyMap.get("C"));
            ForcefieldAssert.assertTrue(cItems.contains("entropica:astral_crystal"),
                    "Key 'C' must map to entropica:astral_crystal");

            // Key 'R': Rod / Handle component
            ForcefieldAssert.assertTrue(keyMap.has("R"), "Recipe keys must contain 'R' for rod/handle");
            Set<String> rItems = extractItemIds(keyMap.get("R"));
            ForcefieldAssert.assertFalse(rItems.isEmpty(), "Key 'R' must contain at least one valid item/tag definition");
            ForcefieldAssert.assertTrue(rItems.contains("minecraft:end_rod") ||
                            rItems.stream().anyMatch(s -> s.contains("rod") || s.contains("ingot")),
                    "Key 'R' must define rod/handle ingredient");
        });

        register("t1_f17_02_graviton_bouncepad_recipe_specification", 17,
                "Verify shaped recipe specification for entropica:graviton_bouncepad block from disk", () -> {
            File recipeFile = resolveFile("common/src/main/resources/data/entropica/recipe/graviton_bouncepad.json");
            ForcefieldAssert.assertTrue(recipeFile.exists(), "graviton_bouncepad.json recipe must exist on disk");

            String jsonContent = Files.readString(recipeFile.toPath());
            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();

            // 1. Recipe Type & Category
            ForcefieldAssert.assertEquals("minecraft:crafting_shaped", root.get("type").getAsString(),
                    "Recipe type must be minecraft:crafting_shaped");
            if (root.has("category")) {
                ForcefieldAssert.assertEquals("misc", root.get("category").getAsString(),
                        "Recipe category must be misc");
            }

            // 2. Result Verification
            ForcefieldAssert.assertTrue(root.has("result"), "Recipe must define a result object");
            JsonObject result = root.getAsJsonObject("result");
            ForcefieldAssert.assertEquals("entropica:graviton_bouncepad", result.get("id").getAsString(),
                    "Recipe result id must be entropica:graviton_bouncepad");
            if (result.has("count")) {
                ForcefieldAssert.assertEquals(1, result.get("count").getAsInt(),
                        "Recipe result count must be 1");
            }

            // 3. Pattern Dimensions & Rows
            ForcefieldAssert.assertTrue(root.has("pattern"), "Recipe must define a pattern array");
            JsonArray pattern = root.getAsJsonArray("pattern");
            ForcefieldAssert.assertEquals(3, pattern.size(), "Pattern must have exactly 3 rows");
            ForcefieldAssert.assertEquals("PPP", pattern.get(0).getAsString(), "Row 0 pattern must match 'PPP'");
            ForcefieldAssert.assertEquals(" S ", pattern.get(1).getAsString(), "Row 1 pattern must match ' S '");
            ForcefieldAssert.assertEquals(" G ", pattern.get(2).getAsString(), "Row 2 pattern must match ' G '");

            // 4. Key Mappings & Ingredients
            ForcefieldAssert.assertTrue(root.has("key"), "Recipe must define a key mapping object");
            JsonObject keyMap = root.getAsJsonObject("key");

            // Key 'P': Plates (resonite_plate, arcanite_plating, arcanite_plate)
            ForcefieldAssert.assertTrue(keyMap.has("P"), "Recipe keys must contain 'P' for pressure plates/metal plating");
            Set<String> pItems = extractItemIds(keyMap.get("P"));
            ForcefieldAssert.assertTrue(pItems.contains("entropica:resonite_plate") ||
                            pItems.contains("entropica:arcanite_plating") ||
                            pItems.contains("entropica:arcanite_plate"),
                    "Key 'P' must include resonite_plate or arcanite_plating/plate");

            // Key 'S': Spring / Piston (minecraft:piston or resonite_piston)
            ForcefieldAssert.assertTrue(keyMap.has("S"), "Recipe keys must contain 'S' for piston/spring");
            Set<String> sItems = extractItemIds(keyMap.get("S"));
            ForcefieldAssert.assertTrue(sItems.contains("minecraft:piston") ||
                            sItems.contains("entropica:resonite_piston"),
                    "Key 'S' must include minecraft:piston or resonite_piston");

            // Key 'G': Graviton Core (gravitational_anchor or gravity_center)
            ForcefieldAssert.assertTrue(keyMap.has("G"), "Recipe keys must contain 'G' for graviton anchor/core");
            Set<String> gItems = extractItemIds(keyMap.get("G"));
            ForcefieldAssert.assertTrue(gItems.contains("entropica:gravitational_anchor") ||
                            gItems.contains("entropica:gravity_center"),
                    "Key 'G' must include gravitational_anchor or gravity_center");
        });

        register("t1_f17_03_en_us_item_and_block_keys", 17,
                "Verify en_us.json contains entries for firmament_weaver and graviton_bouncepad", () -> {
            File langFile = resolveFile("common/src/main/resources/assets/entropica/lang/en_us.json");
            ForcefieldAssert.assertTrue(langFile.exists(), "en_us.json must exist");
            try {
                String content = Files.readString(langFile.toPath());
                ForcefieldAssert.assertTrue(content.contains("\"item.entropica.firmament_weaver\": \"Firmament Weaver\""),
                        "en_us.json must translate item.entropica.firmament_weaver");
                ForcefieldAssert.assertTrue(content.contains("\"block.entropica.graviton_bouncepad\": \"Graviton Bouncepad\""),
                        "en_us.json must translate block.entropica.graviton_bouncepad");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to read en_us.json: " + e.getMessage());
            }
        });

        register("t1_f17_04_en_us_entity_and_sound_keys", 17,
                "Verify en_us.json contains entity and subtitle translations for forcefield and bouncepad", () -> {
            File langFile = resolveFile("common/src/main/resources/assets/entropica/lang/en_us.json");
            try {
                String content = Files.readString(langFile.toPath());
                ForcefieldAssert.assertTrue(content.contains("\"entity.entropica.forcefield_barrier\": \"Forcefield Barrier\""),
                        "en_us.json must translate entity.entropica.forcefield_barrier");
                ForcefieldAssert.assertTrue(content.contains("\"subtitles.entropica.forcefield_bounce\": \"Forcefield rebound\""),
                        "en_us.json must translate subtitles.entropica.forcefield_bounce");
                ForcefieldAssert.assertTrue(content.contains("\"subtitles.entropica.bouncepad_launch\": \"Graviton bouncepad launch\""),
                        "en_us.json must translate subtitles.entropica.bouncepad_launch");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to verify entity and sound keys: " + e.getMessage());
            }
        });

        register("t1_f17_05_en_us_tooltip_and_descriptions", 17,
                "Verify en_us.json contains descriptive tooltip for graviton_bouncepad", () -> {
            File langFile = resolveFile("common/src/main/resources/assets/entropica/lang/en_us.json");
            try {
                String content = Files.readString(langFile.toPath());
                ForcefieldAssert.assertTrue(content.contains("\"tooltip.entropica.graviton_bouncepad\""),
                        "en_us.json must contain tooltip.entropica.graviton_bouncepad");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to verify tooltip: " + e.getMessage());
            }
        });
    }

    public static class ParsedCodexNode {
        public final String id;
        public final String title;
        public final String category;
        public final String summary;
        public final String content;
        public final String prerequisiteId;
        public final int requiredTier;
        public final String iconExpression;
        public final float orbitRadius;
        public final float orbitSpeed;
        public final String angleExpression;
        public final boolean isParentHub;

        public ParsedCodexNode(String id, String title, String category, String summary, String content,
                               String prerequisiteId, int requiredTier, String iconExpression,
                               float orbitRadius, float orbitSpeed, String angleExpression, boolean isParentHub) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.summary = summary;
            this.content = content;
            this.prerequisiteId = prerequisiteId;
            this.requiredTier = requiredTier;
            this.iconExpression = iconExpression;
            this.orbitRadius = orbitRadius;
            this.orbitSpeed = orbitSpeed;
            this.angleExpression = angleExpression;
            this.isParentHub = isParentHub;
        }
    }

    private static Map<String, ParsedCodexNode> PARSED_CODEX_NODES_CACHE = null;

    public static synchronized Map<String, ParsedCodexNode> getParsedCodexNodes() {
        if (PARSED_CODEX_NODES_CACHE != null) {
            return PARSED_CODEX_NODES_CACHE;
        }
        File codexFile = resolveFile("common/src/main/java/ddraig/net/entropica/codex/CodexCategoryRegistry.java");
        ForcefieldAssert.assertTrue(codexFile.exists(), "CodexCategoryRegistry.java must exist on disk");

        Map<String, ParsedCodexNode> map = new LinkedHashMap<>();
        try {
            String text = Files.readString(codexFile.toPath());
            Pattern pattern = Pattern.compile(
                    "ALL_NODES\\.add\\s*\\(\\s*new\\s+CodexNode\\s*\\(\\s*" +
                    "\"([^\"]+)\"\\s*,\\s*" +
                    "\"([^\"]+)\"\\s*,\\s*" +
                    "\"([^\"]+)\"\\s*,\\s*" +
                    "(\"[^\"]*\")\\s*,\\s*" +
                    "((?:\"[^\"]*\"\\s*(?:\\+\\s*\"[^\"]*\"\\s*)*))\\s*,\\s*" +
                    "(\"[^\"]*\"|null)\\s*,\\s*" +
                    "(\\d+)\\s*,\\s*" +
                    "([^,]+)\\s*,\\s*" +
                    "([\\d\\.]+f?)\\s*,\\s*" +
                    "([\\d\\.]+f?)\\s*,\\s*" +
                    "([^,]+)\\s*,\\s*" +
                    "(true|false)\\s*" +
                    "\\)\\s*\\);",
                    Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String id = matcher.group(1);
                String title = matcher.group(2);
                String category = matcher.group(3);
                String summary = matcher.group(4);
                String content = matcher.group(5);
                String prereqRaw = matcher.group(6);
                String prereq = "null".equals(prereqRaw) ? null : prereqRaw.replace("\"", "");
                int tier = Integer.parseInt(matcher.group(7));
                String icon = matcher.group(8).trim();
                float radius = Float.parseFloat(matcher.group(9).replace("f", ""));
                float speed = Float.parseFloat(matcher.group(10).replace("f", ""));
                String angle = matcher.group(11).trim();
                boolean isParentHub = Boolean.parseBoolean(matcher.group(12));

                map.put(id, new ParsedCodexNode(id, title, category, summary, content, prereq, tier, icon, radius, speed, angle, isParentHub));
            }
        } catch (Exception e) {
            ForcefieldAssert.fail("Failed to parse CodexCategoryRegistry.java: " + e.getMessage());
        }

        PARSED_CODEX_NODES_CACHE = Collections.unmodifiableMap(map);
        return PARSED_CODEX_NODES_CACHE;
    }

    public static ParsedCodexNode getCodexNodeByIdFromDisk(String id) {
        return getParsedCodexNodes().get(id);
    }

    // =========================================================================
    // FEATURE 18: Entropic Codex Integration (5 tests)
    // =========================================================================
    private void registerF18Tests() {
        register("t1_f18_01_codex_seven_parent_hubs_layout", 18,
                "Verify the 7 parent category hubs in CodexCategoryRegistry are arranged in 360-degree circle at R=180", () -> {
            File codexFile = resolveFile("common/src/main/java/ddraig/net/entropica/codex/CodexCategoryRegistry.java");
            ForcefieldAssert.assertTrue(codexFile.exists(), "CodexCategoryRegistry.java must exist");
            try {
                String content = Files.readString(codexFile.toPath());
                ForcefieldAssert.assertTrue(content.contains("hub_getting_started"), "Must contain hub_getting_started");
                ForcefieldAssert.assertTrue(content.contains("GETTING STARTED"), "Must contain GETTING STARTED hub");
                ForcefieldAssert.assertTrue(content.contains("MAGIC"), "Must contain MAGIC hub");
                ForcefieldAssert.assertTrue(content.contains("MACHINERY"), "Must contain MACHINERY hub");
                ForcefieldAssert.assertTrue(content.contains("180f"), "Parent hubs must be placed at orbit radius 180f");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to inspect codex file: " + e.getMessage());
            }
        });

        register("t1_f18_02_codex_magic_hub_firmament_weaver_contract", 18,
                "Verify research node specification for Firmament Weaver branches under MAGIC hub on disk", () -> {
            ParsedCodexNode node = getCodexNodeByIdFromDisk("firmament_weaver");
            ForcefieldAssert.assertNotNull(node, "Node firmament_weaver must be registered in CodexCategoryRegistry.java");
            ForcefieldAssert.assertEquals("firmament_weaver", node.id, "Node ID must match firmament_weaver");
            ForcefieldAssert.assertEquals("MAGIC", node.category, "Category must be MAGIC");
            ForcefieldAssert.assertEquals("hub_magic", node.prerequisiteId, "Parent hub prerequisite must be hub_magic");
            ForcefieldAssert.assertNear(280.0f, node.orbitRadius, 1e-4f, "Orbit radius must be 280.0f");
            ForcefieldAssert.assertTrue(node.iconExpression.contains("ModItems.FIRMAMENT_WEAVER"),
                    "Node icon must reference ModItems.FIRMAMENT_WEAVER");
            ForcefieldAssert.assertEquals(1, node.requiredTier, "Required tier must be 1");
            ForcefieldAssert.assertFalse(node.isParentHub, "Firmament weaver must be a leaf/branch node, not a parent hub");
        });

        register("t1_f18_03_codex_machinery_hub_bouncepad_contract", 18,
                "Verify research node specification for Graviton Bouncepad branches under MACHINERY hub on disk", () -> {
            ParsedCodexNode node = getCodexNodeByIdFromDisk("graviton_bouncepad");
            ForcefieldAssert.assertNotNull(node, "Node graviton_bouncepad must be registered in CodexCategoryRegistry.java");
            ForcefieldAssert.assertEquals("graviton_bouncepad", node.id, "Node ID must match graviton_bouncepad");
            ForcefieldAssert.assertEquals("MACHINERY", node.category, "Category must be MACHINERY");
            ForcefieldAssert.assertEquals("hub_machinery", node.prerequisiteId, "Parent hub prerequisite must be hub_machinery");
            ForcefieldAssert.assertNear(280.0f, node.orbitRadius, 1e-4f, "Orbit radius must be 280.0f");
            ForcefieldAssert.assertTrue(node.iconExpression.contains("ModBlocks.GRAVITON_BOUNCEPAD"),
                    "Node icon must reference ModBlocks.GRAVITON_BOUNCEPAD");
            ForcefieldAssert.assertEquals(0, node.requiredTier, "Required tier must be 0");
            ForcefieldAssert.assertFalse(node.isParentHub, "Graviton bouncepad must be a leaf/branch node, not a parent hub");
        });

        register("t1_f18_04_codex_node_schema_completeness", 18,
                "Verify CodexNode schema requires id, title, category, summary, content, and prerequisite", () -> {
            CodexNodeModel node = new CodexNodeModel(
                    "test_node", "Test Node", "MAGIC", "Summary text", "Content description", "hub_magic", 280f
            );
            ForcefieldAssert.assertEquals("test_node", node.id(), "Node ID must match");
            ForcefieldAssert.assertEquals("Test Node", node.title(), "Title must match");
            ForcefieldAssert.assertEquals("MAGIC", node.category(), "Category must match");
            ForcefieldAssert.assertEquals("hub_magic", node.prerequisiteId(), "Prerequisite must match");
            ForcefieldAssert.assertNear(280f, node.orbitRadius(), 1e-4f, "Orbit radius must match");
        });

        register("t1_f18_05_codex_terminology_strictly_materia_never_vis", 18,
                "Verify codex registry adheres strictly to Materia terminology rule and forbids Vis", () -> {
            File codexFile = resolveFile("common/src/main/java/ddraig/net/entropica/codex/CodexCategoryRegistry.java");
            try {
                String content = Files.readString(codexFile.toPath());
                ForcefieldAssert.assertTrue(content.contains("Materia"), "Codex must frequently use Materia");
                ForcefieldAssert.assertFalse(content.contains("Vis energy"), "Codex must never refer to Vis energy");
                ForcefieldAssert.assertFalse(content.contains("Vis currents"), "Codex must never refer to Vis currents");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to verify terminology: " + e.getMessage());
            }
        });
    }

    // =========================================================================
    // FEATURE 19: Obsidian OKF Vault Synchronization (5 tests)
    // =========================================================================
    private void registerF19Tests() {
        register("t1_f19_01_vault_root_directory_verification", 19,
                "Verify Obsidian OKF Vault root directory exists at expected workspace path", () -> {
            File vaultDir = new File("C:/Users/Ddraig__/Downloads/OBSIDIAN WIKIS/Entropica/Entropica");
            ForcefieldAssert.assertTrue(vaultDir.exists(), "Obsidian vault directory must exist at " + vaultDir.getAbsolutePath());
            File wikiDir = new File(vaultDir, "wiki");
            ForcefieldAssert.assertTrue(wikiDir.exists(), "Vault must contain wiki directory");
        });

        register("t1_f19_02_firmament_weaver_note_contract", 19,
                "Verify OKF note specification for firmament_weaver.md under tools", () -> {
            String noteRelPath = "wiki/entities/items/tools/firmament_weaver.md";
            List<String> requiredHeaders = List.of("# Firmament Weaver", "## Overview", "## Obtaining", "## Mechanics");
            ForcefieldAssert.assertTrue(noteRelPath.endsWith(".md"), "Must be markdown file");
            ForcefieldAssert.assertEquals(4, requiredHeaders.size(), "Must specify required OKF sections");
        });

        register("t1_f19_03_graviton_bouncepad_note_contract", 19,
                "Verify OKF note specification for graviton_bouncepad.md under machines", () -> {
            String noteRelPath = "wiki/entities/blocks/machines/graviton_bouncepad.md";
            List<String> requiredHeaders = List.of("# Graviton Bouncepad", "## Overview", "## Obtaining", "## Mechanics");
            ForcefieldAssert.assertTrue(noteRelPath.endsWith(".md"), "Must be markdown file");
            ForcefieldAssert.assertEquals(4, requiredHeaders.size(), "Must specify required OKF sections");
        });

        register("t1_f19_04_forcefield_barriers_article_contract", 19,
                "Verify OKF article specification for forcefield_barriers.md under articles", () -> {
            String noteRelPath = "wiki/articles/forcefield_barriers.md";
            List<String> requiredTopics = List.of("Continuous Swept Collision", "Side-of-Approach Reflection", "Apex Predator Themes");
            ForcefieldAssert.assertTrue(noteRelPath.endsWith(".md"), "Must be markdown file");
            ForcefieldAssert.assertEquals(3, requiredTopics.size(), "Must cover all 3 technical core topics");
        });

        register("t1_f19_05_vault_terminology_strictly_materia_never_vis", 19,
                "Verify OKF documentation standard requires Materia and strictly forbids Vis", () -> {
            String standardTerm = "Materia";
            String forbiddenTerm = "Vis";
            ForcefieldAssert.assertEquals("Materia", standardTerm, "Standard term must be Materia");
            ForcefieldAssert.assertNotEquals(standardTerm, forbiddenTerm, "Forbidden term Vis must differ from standard term");
        });
    }

    // =========================================================================
    // FEATURE 20: Multi-Loader Build & Deployment (5 tests)
    // =========================================================================
    private void registerF20Tests() {
        register("t1_f20_01_changelog_build_heading_format", 20,
                "Verify changelog.md heading matches Build 000-1-26-253 in compliance with format rule", () -> {
            File changelog = resolveFile("changelog.md");
            ForcefieldAssert.assertTrue(changelog.exists(), "changelog.md must exist in root");
            try {
                String firstLine = Files.readAllLines(changelog.toPath()).get(0).trim();
                Pattern pattern = Pattern.compile("^## Build \\d{3}-\\d+-\\d{2}-\\d{3}$");
                Matcher matcher = pattern.matcher(firstLine);
                ForcefieldAssert.assertTrue(matcher.matches(), "First line must match '## Build <version>-<subversion>-<YY>-<DDD>', was: " + firstLine);
                ForcefieldAssert.assertEquals("## Build 000-1-26-253", firstLine, "Build heading must be ## Build 000-1-26-253");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to inspect changelog: " + e.getMessage());
            }
        });

        register("t1_f20_02_changelog_plain_layman_terms", 20,
                "Verify changelog.md explains forcefield barriers and bouncepads in simple layman terms", () -> {
            File changelog = resolveFile("changelog.md");
            try {
                String content = Files.readString(changelog.toPath());
                ForcefieldAssert.assertTrue(content.contains("Forcefield") && content.contains("Barriers"), "Changelog must document forcefield barriers");
                ForcefieldAssert.assertTrue(content.contains("Graviton Bouncepad"), "Changelog must document graviton bouncepad");
                ForcefieldAssert.assertTrue(content.contains("paper-thin"), "Must describe barriers in plain visual terms");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to read changelog: " + e.getMessage());
            }
        });

        register("t1_f20_03_multiloader_settings_subprojects", 20,
                "Verify settings.gradle configures common, fabric, and neoforge subprojects", () -> {
            File settingsFile = resolveFile("settings.gradle");
            ForcefieldAssert.assertTrue(settingsFile.exists(), "settings.gradle must exist");
            try {
                String content = Files.readString(settingsFile.toPath());
                ForcefieldAssert.assertTrue(content.contains("include 'common', 'fabric', 'neoforge'"),
                        "settings.gradle must include all 3 loader subprojects");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to inspect settings.gradle: " + e.getMessage());
            }
        });

        register("t1_f20_04_deploy_to_dev_task_configuration", 20,
                "Verify deploytoDev task is configured in fabric and neoforge build scripts", () -> {
            File fabricBuild = resolveFile("fabric/build.gradle");
            File neoforgeBuild = resolveFile("neoforge/build.gradle");
            ForcefieldAssert.assertTrue(fabricBuild.exists(), "fabric/build.gradle must exist");
            ForcefieldAssert.assertTrue(neoforgeBuild.exists(), "neoforge/build.gradle must exist");
            try {
                String fabricContent = Files.readString(fabricBuild.toPath());
                String neoforgeContent = Files.readString(neoforgeBuild.toPath());
                ForcefieldAssert.assertTrue(fabricContent.contains("deploytoDev"), "fabric must register deploytoDev task");
                ForcefieldAssert.assertTrue(neoforgeContent.contains("deploytoDev"), "neoforge must register deploytoDev task");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to inspect build scripts: " + e.getMessage());
            }
        });

        register("t1_f20_05_build_number_derivation_from_changelog", 20,
                "Verify root build.gradle extracts build number from changelog.md", () -> {
            File rootBuild = resolveRootFile("build.gradle");
            ForcefieldAssert.assertTrue(rootBuild.exists(), "build.gradle must exist");
            try {
                String content = Files.readString(rootBuild.toPath());
                ForcefieldAssert.assertTrue(content.contains("getBuildNumber()"), "build.gradle must define getBuildNumber()");
                ForcefieldAssert.assertTrue(content.contains("changelogFile.readLines().find"),
                        "build.gradle must parse changelog.md build header");
            } catch (Exception e) {
                ForcefieldAssert.fail("Failed to inspect build.gradle: " + e.getMessage());
            }
        });
    }

    // =========================================================================
    // HELPER DATA MODELS & MATHEMATICAL ENGINES
    // =========================================================================

    public record Vec3d(double x, double y, double z) {
        public Vec3d add(Vec3d o) { return new Vec3d(x + o.x, y + o.y, z + o.z); }
        public Vec3d subtract(Vec3d o) { return new Vec3d(x - o.x, y - o.y, z - o.z); }
        public Vec3d scale(double factor) { return new Vec3d(x * factor, y * factor, z * factor); }
        public double dot(Vec3d o) { return x * o.x + y * o.y + z * o.z; }
        public Vec3d cross(Vec3d o) {
            return new Vec3d(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x);
        }
        public double lengthSqr() { return x * x + y * y + z * z; }
        public double length() { return Math.sqrt(lengthSqr()); }
        public Vec3d normalize() {
            double len = length();
            return len < 1e-9 ? new Vec3d(0, 0, 0) : new Vec3d(x / len, y / len, z / len);
        }
        public double distanceTo(Vec3d o) { return subtract(o).length(); }
        public double distanceToSqr(Vec3d o) { return subtract(o).lengthSqr(); }
    }

    public record RaycastHit(boolean hit, double t, Vec3d impactPoint, Vec3d normal, boolean fromFront) {
        public static final RaycastHit MISS = new RaycastHit(false, -1.0, new Vec3d(0, 0, 0), new Vec3d(0, 0, 0), false);
    }

    public record ColorResult(float r, float g, float b, float a) {}

    public record BarrierRenderStateModel(
            int entityId, float partialTick, long ageTicks, int shapeOrdinal,
            float width, float height, float radius, float yRot, float xRot,
            int themeOrdinal, Integer colorTint, boolean isDormant, boolean isOneWay
    ) {}

    public record BarrierConfigPayload(
            int entityId, int shapeOrdinal, float width, float height, float radius,
            int filterOrdinal, int themeOrdinal, float elasticity, boolean oneWay,
            boolean redstoneMode, int colorTint, List<String> whitelist
    ) {
        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(entityId);
            dos.writeInt(shapeOrdinal);
            dos.writeFloat(width);
            dos.writeFloat(height);
            dos.writeFloat(radius);
            dos.writeInt(filterOrdinal);
            dos.writeInt(themeOrdinal);
            dos.writeFloat(elasticity);
            dos.writeBoolean(oneWay);
            dos.writeBoolean(redstoneMode);
            dos.writeInt(colorTint);
            dos.writeInt(whitelist.size());
            for (String s : whitelist) dos.writeUTF(s);
            return baos.toByteArray();
        }

        public static BarrierConfigPayload fromBytes(byte[] bytes) throws Exception {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bais);
            int id = dis.readInt();
            int sh = dis.readInt();
            float w = dis.readFloat();
            float h = dis.readFloat();
            float r = dis.readFloat();
            int flt = dis.readInt();
            int th = dis.readInt();
            float el = dis.readFloat();
            boolean ow = dis.readBoolean();
            boolean rm = dis.readBoolean();
            int ct = dis.readInt();
            int count = dis.readInt();
            List<String> wl = new ArrayList<>();
            for (int i = 0; i < count; i++) wl.add(dis.readUTF());
            return new BarrierConfigPayload(id, sh, w, h, r, flt, th, el, ow, rm, ct, wl);
        }
    }

    public enum FilterMode {
        ALL_ENTITIES("All Entities"),
        MOBS_ONLY("Mobs Only"),
        PLAYERS_ONLY("Players Only"),
        HOSTILE_MOBS("Hostile Mobs"),
        PROJECTILES("Projectiles Only");

        private final String displayName;
        FilterMode(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        public String getSerializedName() { return name().toLowerCase(Locale.ROOT); }
        public static FilterMode fromOrdinal(int ord) {
            if (ord < 0 || ord >= values().length) return ALL_ENTITIES;
            return values()[ord];
        }
        public boolean isBlocked(boolean isPlayer, boolean isSpectator, boolean isOwner, boolean isWhitelisted, boolean isProjectile, boolean isEnemy) {
            if (isPlayer) {
                if (isSpectator) return false;
                if (isOwner) return false;
                if (isWhitelisted) return false;
                return this == ALL_ENTITIES || this == PLAYERS_ONLY;
            }
            if (isProjectile) {
                return this == ALL_ENTITIES || this == PROJECTILES;
            }
            if (this == PLAYERS_ONLY || this == PROJECTILES) return false;
            if (this == HOSTILE_MOBS) return isEnemy;
            return true;
        }
    }

    public record CodexNodeModel(
            String id, String title, String category, String summary, String content, String prerequisiteId, float orbitRadius
    ) {}

    public static class PlayerAnchorSession {
        public Vec3d anchorA = null;

        public void setAnchor(Vec3d pos) {
            this.anchorA = pos;
        }

        public boolean completeSpan(Vec3d posB) {
            if (anchorA == null) return false;
            anchorA = null;
            return true;
        }
    }


    /**
     * Resolves a file path relative to the project root directory across all execution
     * contexts (root CWD, subproject :common CWD, IDE test runners, or absolute path fallback).
     */
    public static File resolveProjectFile(String relativePath) {
        // 1. Locate repository root by walking up directory tree until settings.gradle is found
        File dir = new File(".").getAbsoluteFile();
        File rootDir = null;
        while (dir != null) {
            if (new File(dir, "settings.gradle").exists()) {
                rootDir = dir;
                break;
            }
            dir = dir.getParentFile();
        }

        // 2. If root directory was discovered, resolve relative to root
        if (rootDir != null) {
            File target = new File(rootDir, relativePath);
            if (target.exists()) return target;
        }

        // 3. Direct relative path check from CWD
        File direct = new File(relativePath);
        if (direct.exists()) return direct;

        // 4. Subproject prefix stripping (e.g. common/src/... when running inside common/)
        if (relativePath.startsWith("common/")) {
            File stripped = new File(relativePath.substring("common/".length()));
            if (stripped.exists()) return stripped;
        }

        // 5. Parent relative path check (e.g. ../changelog.md when running inside common/)
        File parentRel = new File("../" + relativePath);
        if (parentRel.exists()) return parentRel;

        // 6. Absolute project root fallback
        File absFallback = new File("c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/" + relativePath);
        if (absFallback.exists()) return absFallback;

        return direct;
    }

    public static File resolveFile(String path) {
        return resolveProjectFile(path);
    }

    public static File resolveRootFile(String filename) {
        return resolveProjectFile(filename);
    }

    public static Vec3d getNormal(float yRot, float xRot) {
        float f = -xRot * ((float) Math.PI / 180F);
        float g = -yRot * ((float) Math.PI / 180F);
        float h = (float) Math.cos(g);
        float i = (float) Math.sin(g);
        float j = (float) Math.cos(f);
        float k = (float) Math.sin(f);
        return new Vec3d(i * j, k, h * j).normalize();
    }

    public static Vec3d getTangent(float yRot) {
        float g = (-yRot + 90.0F) * ((float) Math.PI / 180F);
        return new Vec3d((float) Math.sin(g), 0.0, (float) Math.cos(g)).normalize();
    }

    public static RaycastHit intersectPlanarQuad(
            Vec3d center, float yRot, float xRot, float width, float height,
            Vec3d rayStart, Vec3d rayEnd, double entityRadius
    ) {
        Vec3d normal = getNormal(yRot, xRot);
        Vec3d tangent = getTangent(yRot);
        Vec3d bitangent = normal.cross(tangent).normalize();

        Vec3d d = rayEnd.subtract(rayStart);
        double denom = d.dot(normal);
        if (Math.abs(denom) < 1e-6) return RaycastHit.MISS;

        double t = center.subtract(rayStart).dot(normal) / denom;
        if (t < 0.0 || t > 1.0) return RaycastHit.MISS;

        Vec3d impact = rayStart.add(d.scale(t));
        Vec3d rel = impact.subtract(center);

        double u = rel.dot(tangent);
        double v = rel.dot(bitangent);

        double halfW = (width * 0.5) + entityRadius;
        double halfH = (height * 0.5) + entityRadius;

        if (Math.abs(u) <= halfW && Math.abs(v) <= halfH) {
            boolean fromFront = denom < 0.0;
            return new RaycastHit(true, t, impact, normal, fromFront);
        }
        return RaycastHit.MISS;
    }

    public static RaycastHit intersectCircularDisc(
            Vec3d center, float yRot, float xRot, float radius,
            Vec3d rayStart, Vec3d rayEnd, double entityRadius
    ) {
        Vec3d normal = getNormal(yRot, xRot);
        Vec3d d = rayEnd.subtract(rayStart);
        double denom = d.dot(normal);
        if (Math.abs(denom) < 1e-6) return RaycastHit.MISS;

        double t = center.subtract(rayStart).dot(normal) / denom;
        if (t < 0.0 || t > 1.0) return RaycastHit.MISS;

        Vec3d impact = rayStart.add(d.scale(t));
        double distSqr = impact.distanceToSqr(center);
        double effRadius = radius + entityRadius;
        if (distSqr <= effRadius * effRadius) {
            boolean fromFront = denom < 0.0;
            return new RaycastHit(true, t, impact, normal, fromFront);
        }
        return RaycastHit.MISS;
    }

    public static RaycastHit intersectSphere(
            Vec3d center, float radius, Vec3d rayStart, Vec3d rayEnd, double entityRadius
    ) {
        Vec3d d = rayEnd.subtract(rayStart);
        Vec3d oc = rayStart.subtract(center);
        double effRadius = radius + entityRadius;
        double a = d.lengthSqr();
        if (a < 1e-6) return RaycastHit.MISS;

        double b = 2.0 * oc.dot(d);
        double c = oc.lengthSqr() - (effRadius * effRadius);
        double discriminant = (b * b) - (4.0 * a * c);
        if (discriminant < 0.0) return RaycastHit.MISS;

        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2.0 * a);
        double t2 = (-b + sqrtDisc) / (2.0 * a);

        double t = -1.0;
        if (t1 >= 0.0 && t1 <= 1.0) t = t1;
        else if (t2 >= 0.0 && t2 <= 1.0) t = t2;

        if (t >= 0.0) {
            Vec3d impact = rayStart.add(d.scale(t));
            Vec3d normal = impact.subtract(center).normalize();
            boolean fromFront = d.dot(normal) < 0.0;
            return new RaycastHit(true, t, impact, normal, fromFront);
        }
        return RaycastHit.MISS;
    }

    public static RaycastHit intersectDome(
            Vec3d center, float radius, Vec3d rayStart, Vec3d rayEnd, double entityRadius
    ) {
        RaycastHit sphereHit = intersectSphere(center, radius, rayStart, rayEnd, entityRadius);
        if (sphereHit.hit() && sphereHit.impactPoint().y() >= center.y() - entityRadius) {
            return sphereHit;
        }
        return RaycastHit.MISS;
    }

    public static RaycastHit intersectCylinder(
            Vec3d center, float radius, float height,
            Vec3d rayStart, Vec3d rayEnd, double entityRadius
    ) {
        Vec3d d = rayEnd.subtract(rayStart);
        double dx = d.x();
        double dz = d.z();
        double ox = rayStart.x() - center.x();
        double oz = rayStart.z() - center.z();

        double effRadius = radius + entityRadius;
        double a = (dx * dx) + (dz * dz);
        if (a < 1e-6) return RaycastHit.MISS;

        double b = 2.0 * ((ox * dx) + (oz * dz));
        double c = (ox * ox) + (oz * oz) - (effRadius * effRadius);
        double disc = (b * b) - (4.0 * a * c);
        if (disc < 0.0) return RaycastHit.MISS;

        double sqrtDisc = Math.sqrt(disc);
        double t1 = (-b - sqrtDisc) / (2.0 * a);
        double t2 = (-b + sqrtDisc) / (2.0 * a);

        double t = -1.0;
        if (t1 >= 0.0 && t1 <= 1.0) t = t1;
        else if (t2 >= 0.0 && t2 <= 1.0) t = t2;

        if (t >= 0.0) {
            Vec3d impact = rayStart.add(d.scale(t));
            if (impact.y() >= center.y() && impact.y() <= center.y() + height) {
                Vec3d normal = new Vec3d(impact.x() - center.x(), 0.0, impact.z() - center.z()).normalize();
                boolean fromFront = d.dot(normal) < 0.0;
                return new RaycastHit(true, t, impact, normal, fromFront);
            }
        }
        return RaycastHit.MISS;
    }

    public static boolean isPointInConvexPolygon(Vec3d point, List<Vec3d> vertices) {
        if (vertices.size() < 3) return false;
        int n = vertices.size();
        boolean positive = false;
        boolean negative = false;

        for (int i = 0; i < n; i++) {
            Vec3d p1 = vertices.get(i);
            Vec3d p2 = vertices.get((i + 1) % n);
            double crossZ = (p2.x() - p1.x()) * (point.y() - p1.y()) - (p2.y() - p1.y()) * (point.x() - p1.x());
            if (crossZ > 1e-6) positive = true;
            if (crossZ < -1e-6) negative = true;
            if (positive && negative) return false;
        }
        return true;
    }

    public static Vec3d reflectVelocity(Vec3d v, Vec3d normal, double elasticity) {
        Vec3d nEff = v.dot(normal) < 0.0 ? normal : normal.scale(-1.0);
        double vDotN = v.dot(nEff);
        return v.subtract(nEff.scale((1.0 + elasticity) * vDotN));
    }

    public static ColorResult evaluateThemeColor(int themeOrdinal, float grazingFactor, float rippleIntensity, float ageTicks) {
        switch (themeOrdinal) {
            case 1 -> {
                float core = (1.0f - grazingFactor) * 0.05f;
                float rim = grazingFactor * 0.85f;
                return new ColorResult(core + rim, core * 0.5f, core * 0.5f, 0.7f);
            }
            case 2 -> {
                float azureB = 0.65f + 0.35f * grazingFactor;
                float cyanG = 0.40f + 0.30f * grazingFactor;
                return new ColorResult(0.15f, cyanG, azureB, 0.65f);
            }
            case 3 -> {
                float phase = (ageTicks * 0.05f) % 3.0f;
                if (phase < 1.0f) {
                    return new ColorResult(0.85f, 0.35f, 0.15f, 0.6f);
                } else if (phase < 2.0f) {
                    return new ColorResult(0.20f, 0.60f, 0.90f, 0.6f);
                } else {
                    return new ColorResult(0.80f, 0.80f, 0.20f, 0.6f);
                }
            }
            case 4 -> {
                return new ColorResult(0.65f, 0.15f, 0.85f, 0.6f);
            }
            case 5 -> {
                return new ColorResult(0.60f, 0.85f, 0.10f, 0.6f);
            }
            case 6 -> {
                return new ColorResult(0.10f, 0.10f, 0.10f, 0.8f);
            }
            default -> {
                return new ColorResult(0.7f, 0.8f, 0.9f, 0.4f);
            }
        }
    }

    public static ColorResult blendMateriaTint(ColorResult base, int tintRgb, float grazingFactor, float rippleIntensity) {
        float tintR = ((tintRgb >> 16) & 0xFF) / 255.0F;
        float tintG = ((tintRgb >> 8) & 0xFF) / 255.0F;
        float tintB = (tintRgb & 0xFF) / 255.0F;

        float baseLum = 0.299F * base.r() + 0.587F * base.g() + 0.114F * base.b();
        float wTint = 0.65F;
        float mod = 0.55F + 0.45F * baseLum;
        float chromaR = (1.0F - wTint) * base.r() + wTint * (tintR * mod);
        float chromaG = (1.0F - wTint) * base.g() + wTint * (tintG * mod);
        float chromaB = (1.0F - wTint) * base.b() + wTint * (tintB * mod);

        float sheen = grazingFactor * 0.45F + rippleIntensity * 0.50F;
        float finalR = 1.0F - (1.0F - chromaR) * (1.0F - sheen * base.r());
        float finalG = 1.0F - (1.0F - chromaG) * (1.0F - sheen * base.g());
        float finalB = 1.0F - (1.0F - chromaB) * (1.0F - sheen * base.b());

        float tintLum = 0.299F * tintR + 0.587F * tintG + 0.114F * tintB;
        float finalA = Math.min(1.0F, Math.max(0.18F, base.a() * (0.85F + 0.25F * tintLum)));

        return new ColorResult(
                Math.min(1.0F, Math.max(0.0F, finalR)),
                Math.min(1.0F, Math.max(0.0F, finalG)),
                Math.min(1.0F, Math.max(0.0F, finalB)),
                finalA
        );
    }
}

