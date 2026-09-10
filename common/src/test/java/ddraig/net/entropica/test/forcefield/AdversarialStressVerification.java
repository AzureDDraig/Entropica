package ddraig.net.entropica.test.forcefield;

import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper;
import ddraig.net.entropica.client.renderer.forcefield.ForcefieldShaderHelper.ColorResult;
import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import ddraig.net.entropica.forcefield.ApexPredatorThemeRegistry;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Empirical Adversarial Verification Harness for Milestone 1 Iteration 2.
 * Executes exhaustive boundary, extreme-value, and combinatorial stress tests
 * against ForcefieldShaderHelper.evaluateDormantColor and ForcefieldShaderHelper.evaluateColor
 * across all 7 registered Apex Predator themes.
 */
public class AdversarialStressVerification {

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

    private static int totalChecks = 0;
    private static int passedChecks = 0;
    private static final List<FailureRecord> failures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("      EMPIRICAL ADVERSARIAL CHALLENGER (M1 ITERATION 2 VERIFICATION)            ");
        System.out.println("================================================================================");
        System.out.println();

        testTask1_EvaluateDormantColorExtremeInputs();
        testTask2_EvaluateColorAll7Themes();
        testTask3_Verify42PreviouslyFailingPoints();

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
            System.out.println("Failures list:");
            for (FailureRecord f : failures) {
                System.out.println("  - " + f);
            }
            System.out.println("================================================================================");
            System.exit(1);
        }
    }

    private static void checkClamped(String label, ColorResult res) {
        totalChecks++;
        if (Float.isNaN(res.r()) || Float.isNaN(res.g()) || Float.isNaN(res.b()) || Float.isNaN(res.a())) {
            failures.add(new FailureRecord(label, "NaN detected: " + res));
            return;
        }
        if (res.r() < 0.0f || res.r() > 1.0f ||
            res.g() < 0.0f || res.g() > 1.0f ||
            res.b() < 0.0f || res.b() > 1.0f ||
            res.a() < 0.0f || res.a() > 1.0f) {
            failures.add(new FailureRecord(label, "Out of [0.0, 1.0] range: " + res));
            return;
        }
        passedChecks++;
    }

    /**
     * Challenge Task 1:
     * Empirically stress-test ForcefieldShaderHelper.evaluateDormantColor with extreme inputs:
     * grazingFactor = 1.5, -1.0, 100.0, -100.0, Float.MAX_VALUE, -Float.MAX_VALUE.
     * Verify that RGBA return values are strictly clamped in [0.0F, 1.0F].
     */
    private static void testTask1_EvaluateDormantColorExtremeInputs() {
        System.out.println("--- TASK 1: Stress-Testing evaluateDormantColor with Extreme Inputs ---");

        float[] requiredGrazing = {
                1.5f,
                -1.0f,
                100.0f,
                -100.0f,
                Float.MAX_VALUE,
                -Float.MAX_VALUE
        };

        float[] additionalGrazing = {
                0.0f, 0.5f, 1.0f, -0.0001f, 1.0001f, 2.0f, -2.0f,
                Float.MIN_VALUE, -Float.MIN_VALUE,
                1e5f, -1e5f
        };

        float[] testAges = {0.0f, 75.0f, 150.0f, 225.0f, 300.0f, 1000.0f, Float.MAX_VALUE, -100.0f};
        Integer[] testTints = {null, 0, 0x000000, 0xFFFFFF, 0x80FF0000, 0x00AABBCC, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};

        // 1A. Explicit mandatory tests
        for (float g : requiredGrazing) {
            for (float age : testAges) {
                for (Integer tint : testTints) {
                    String label = String.format("Task1_Required: grazing=%s, age=%s, tint=%s", g, age, tint);
                    ColorResult res = ForcefieldShaderHelper.evaluateDormantColor(g, age, tint);
                    checkClamped(label, res);
                }
            }
        }

        // 1B. Additional comprehensive matrix
        for (float g : additionalGrazing) {
            for (float age : testAges) {
                for (Integer tint : testTints) {
                    String label = String.format("Task1_Additional: grazing=%s, age=%s, tint=%s", g, age, tint);
                    ColorResult res = ForcefieldShaderHelper.evaluateDormantColor(g, age, tint);
                    checkClamped(label, res);
                }
            }
        }

        System.out.println("  Task 1 Completed: " + passedChecks + " checks passed so far.");
    }

    /**
     * Challenge Task 2:
     * Empirically stress-test ForcefieldShaderHelper.evaluateColor with all 7 themes.
     */
    private static void testTask2_EvaluateColorAll7Themes() {
        System.out.println("--- TASK 2: Stress-Testing evaluateColor with All 7 Themes ---");

        Collection<ApexPredatorTheme> themes = ApexPredatorThemeRegistry.getAll();
        System.out.println("  Loaded registered themes count: " + themes.size());

        float[] uValues = {-100.0f, -1.5f, -1.0f, 0.0f, 1.0f, 1.5f, 100.0f};
        float[] vValues = {-100.0f, -1.5f, -1.0f, 0.0f, 1.0f, 1.5f, 100.0f};
        Vec3[] normals = {
                new Vec3(0, 1, 0),
                new Vec3(0, 0, 1),
                new Vec3(1, 0, 0),
                new Vec3(0, -1, 0),
                new Vec3(0, 0, -1),
                new Vec3(-1, 0, 0),
                new Vec3(0, 0, 0),             // zero vector edge case
                new Vec3(10, 20, 30),          // unnormalized large vector
                new Vec3(1e-5, 1e-5, 1e-5)     // unnormalized tiny vector
        };
        Vec3[] viewDirs = {
                new Vec3(0, 1, 0),             // parallel
                new Vec3(0, -1, 0),            // antiparallel
                new Vec3(1, 0, 0),             // orthogonal
                new Vec3(0.7071, 0.7071, 0),   // 45 degrees
                new Vec3(0, 0, 0),             // zero vector edge case
                new Vec3(100, 200, 300)        // large viewDir
        };
        float[] ageValues = {0.0f, 10.0f, 150.0f, 300.0f, 99999.0f, -100.0f};
        float[] ripples = {0.0f, 0.5f, 1.0f, 2.0f, 10.0f, -1.0f};
        Integer[] tints = {null, 0x000000, 0xFFFFFF, 0x123456, -1, 0x80FF0000};
        boolean[] dormantStates = {false, true};

        for (ApexPredatorTheme theme : themes) {
            int themePassedBefore = passedChecks;
            int themeFailedBefore = failures.size();

            // Combinatorial stress test
            for (float u : uValues) {
                for (float v : vValues) {
                    for (Vec3 normal : normals) {
                        for (Vec3 viewDir : viewDirs) {
                            for (float age : ageValues) {
                                for (float ripple : ripples) {
                                    for (Integer tint : tints) {
                                        for (boolean isDormant : dormantStates) {
                                            String label = String.format("Task2_Theme[%s]: u=%.1f, v=%.1f, norm=%s, view=%s, age=%.0f, rip=%.1f, tint=%s, dormant=%b",
                                                    theme.getDisplayName(), u, v, normal, viewDir, age, ripple, tint, isDormant);
                                            ColorResult res = ForcefieldShaderHelper.evaluateColor(
                                                    u, v, normal, viewDir, age, theme, ripple, tint, isDormant
                                            );
                                            checkClamped(label, res);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int themePassed = passedChecks - themePassedBefore;
            int themeFailed = failures.size() - themeFailedBefore;
            System.out.printf("  [THEME PASS] %-25s: %d checks passed (failures: %d)%n",
                    theme.getDisplayName(), themePassed, themeFailed);
        }
    }

    /**
     * Challenge Task 3:
     * Verify that all 42 previously failing test points from Challenger 2 Iteration 1 now pass 100%.
     * In Challenger 2 Iteration 1:
     * evaluateDormantColor had 42 clamping violations where grazingFactor deviated outside [0.0, 1.0],
     * causing blue channel b > 1.0F or alpha a < 0.0F.
     */
    private static void testTask3_Verify42PreviouslyFailingPoints() {
        System.out.println("--- TASK 3: Verifying Resolution of the 42 Previously Failing Points ---");

        // The 7 out-of-bounds grazing factors that produce violations in unclamped arithmetic:
        // b = 0.95 + 0.05 * g > 1.0 whenever g > 1.0  (e.g., 1.1, 1.2, 1.3, 1.4, 1.5, 2.0)
        // a = (0.035 + 0.065 * g) * (...) < 0 whenever g < -0.538 (e.g., -1.0)
        float[] failingGrazingFactors = {
                -1.0f,  // causes negative alpha in unclamped formula
                -0.8f,  // causes negative alpha in unclamped formula
                -0.6f,  // causes negative alpha in unclamped formula
                1.1f,   // causes b = 1.005 > 1.0 in unclamped formula
                1.2f,   // causes b = 1.010 > 1.0 in unclamped formula
                1.5f,   // causes b = 1.025 > 1.0 in unclamped formula (explicitly cited by Challenger 2 It 1)
                2.0f    // causes b = 1.050 > 1.0 in unclamped formula
        };

        // 6 age sampling ticks across the 300-tick breathing cycle (7 * 6 = 42 combinations)
        float[] samplingAges = {
                0.0f,
                50.0f,
                100.0f,
                150.0f,
                200.0f,
                250.0f
        };

        int it1FailingPointsCount = 0;
        int it2PassingPointsCount = 0;

        for (float g : failingGrazingFactors) {
            for (float age : samplingAges) {
                it1FailingPointsCount++;

                // Verify unclamped behavior would have failed:
                float breathe = 0.5f + 0.5f * (float) Math.sin(age * 0.02f);
                float unclampedA = (0.035f + 0.065f * g) * (0.75f + 0.25f * breathe);
                float unclampedB = 0.95f + 0.05f * g;
                boolean wouldHaveViolated = (unclampedA < 0.0f || unclampedB > 1.0f);

                if (!wouldHaveViolated) {
                    System.err.println("Warning: test point did not violate unclamped formula: g=" + g + ", age=" + age);
                }

                // Verify that current implementation strictly passes:
                String label = String.format("Task3_Point_%02d: grazing=%.2f, age=%.1f", it1FailingPointsCount, g, age);
                ColorResult res = ForcefieldShaderHelper.evaluateDormantColor(g, age, null);

                if (res.r() >= 0.0f && res.r() <= 1.0f &&
                    res.g() >= 0.0f && res.g() <= 1.0f &&
                    res.b() >= 0.0f && res.b() <= 1.0f &&
                    res.a() >= 0.0f && res.a() <= 1.0f &&
                    !Float.isNaN(res.r()) && !Float.isNaN(res.g()) && !Float.isNaN(res.b()) && !Float.isNaN(res.a())) {
                    it2PassingPointsCount++;
                    passedChecks++;
                } else {
                    failures.add(new FailureRecord(label, "Failed clamping: " + res));
                }
                totalChecks++;
            }
        }

        System.out.printf("  Total previously failing test points evaluated: %d%n", it1FailingPointsCount);
        System.out.printf("  Points passing under current implementation:   %d / %d (%.1f%%)%n",
                it2PassingPointsCount, it1FailingPointsCount, (100.0 * it2PassingPointsCount / it1FailingPointsCount));
    }
}
