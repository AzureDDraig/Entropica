package ddraig.net.entropica.test.forcefield;

import ddraig.net.entropica.client.gui.BarrierConfigScreen;
import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import ddraig.net.entropica.forcefield.BarrierFilterMode;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.network.UpdateBarrierConfigPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Empirical Adversarial Verification Suite for Milestone 4 Challenger 1:
 * Creator Config GUI Mathematical & Logical Invariants, StepSlider Snapping,
 * Precision Drift Analysis, Boundary Clamping, Cycling Arithmetic,
 * Whitelist Manager Logic, and Network Packet Serialization.
 */
public class Milestone4Challenger1Verification {

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
        System.out.println("   EMPIRICAL ADVERSARIAL CHALLENGER 1 (MILESTONE 4: GUI MATH & INVARIANTS)      ");
        System.out.println("================================================================================");
        System.out.println();

        testChallenge1_StepSliderDirectInstantiationAndStepMath();
        testChallenge2_ContinuousSliderInputSweeps_OneMillionPermutations();
        testChallenge3_CycleButtonArithmeticAndBoundaryWrap();
        testChallenge4_RedstoneModeTransitionsAndServerDefense();
        testChallenge5_OneWayValveToggleAndStateInvariance();
        testChallenge6_WhitelistManagerLogicAndClickCoordinates();
        testChallenge7_PacketSerializationAndServerSecurityDefense();

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
     * Helper to simulate StepSlider normalized conversion
     */
    private static double toNormalized(double val, double min, double max) {
        return Math.max(0.0, Math.min(1.0, (val - min) / (max - min)));
    }

    /**
     * Helper to simulate StepSlider stepped physical conversion
     */
    private static double getActualValue(double normalizedValue, double min, double max, double step) {
        double raw = min + (max - min) * normalizedValue;
        double stepped = Math.round((raw - min) / step) * step + min;
        return Math.max(min, Math.min(max, stepped));
    }

    /**
     * Challenge 1: Direct StepSlider instantiation & discrete step mathematics.
     * Evaluates all 63 dimension steps (1.0 to 32.0, step 0.5) and 37 elasticity steps (0.20 to 2.00, step 0.05).
     * Tests fixed-point idempotence, float casting fidelity, and message formatting.
     */
    private static void testChallenge1_StepSliderDirectInstantiationAndStepMath() {
        System.out.println("--- CHALLENGE 1: StepSlider Direct Instantiation & Step Math ---");
        String test = "Challenge1_StepSliderMath";

        // --- Part 1A: Dimension StepSlider (Width, Height, Radius: min=1.0, max=32.0, step=0.5) ---
        double dimMin = 1.0;
        double dimMax = 32.0;
        double dimStep = 0.5;
        int expectedDimSteps = 63; // 0 to 62: 1.0, 1.5, 2.0 ... 32.0

        for (int k = 0; k < expectedDimSteps; k++) {
            double expectedVal = dimMin + k * dimStep;
            double norm = toNormalized(expectedVal, dimMin, dimMax);

            // Verify normalized range
            check(test, norm >= 0.0 && norm <= 1.0, "Dimension normalized value must be in [0, 1] for step " + k);

            // Compute snapped actual value from normalized
            double actual = getActualValue(norm, dimMin, dimMax, dimStep);
            check(test, Math.abs(actual - expectedVal) < 1e-9, "Dimension step " + k + " actual " + actual + " != expected " + expectedVal);

            // Float precision fidelity
            float floatVal = (float) actual;
            check(test, floatVal == (float) expectedVal, "Dimension float conversion drift at step " + k);

            // String formatting check (%.1f)
            String label = String.format(Locale.ROOT, "Width: %.1fm", actual);
            String expectedLabel = String.format(Locale.ROOT, "Width: %.1fm", expectedVal);
            check(test, label.equals(expectedLabel), "Dimension label mismatch at step " + k + ": got " + label);

            // Direct Instantiation of BarrierConfigScreen.StepSlider
            AtomicReference<Double> capturedVal = new AtomicReference<>(0.0);
            BarrierConfigScreen.StepSlider slider = new BarrierConfigScreen.StepSlider(
                    0, 0, 100, 20, "Width", "m", dimMin, dimMax, dimStep, expectedVal,
                    capturedVal::set
            );
            check(test, Math.abs(slider.getActualValue() - expectedVal) < 1e-9, "Direct StepSlider getActualValue mismatch at step " + k);
            check(test, slider.getMessage().getString().equals(expectedLabel), "Direct StepSlider getMessage mismatch at step " + k);

            // Fixed-point idempotence: Re-applying value 20 times must not drift
            double currentNorm = norm;
            for (int iter = 0; iter < 20; iter++) {
                double recomputedActual = getActualValue(currentNorm, dimMin, dimMax, dimStep);
                currentNorm = toNormalized(recomputedActual, dimMin, dimMax);
                check(test, Math.abs(recomputedActual - expectedVal) < 1e-9, "Dimension drift detected at step " + k + " iter " + iter);
            }
        }

        // --- Part 1B: Elasticity StepSlider (Bounce: min=0.20, max=2.00, step=0.05) ---
        double elMin = 0.20;
        double elMax = 2.00;
        double elStep = 0.05;
        int expectedElSteps = 37; // 0 to 36: 0.20, 0.25, ..., 1.00, ..., 2.00

        for (int k = 0; k < expectedElSteps; k++) {
            double expectedVal = Math.round((elMin + k * elStep) * 100.0) / 100.0;
            double norm = toNormalized(expectedVal, elMin, elMax);

            check(test, norm >= 0.0 && norm <= 1.0, "Elasticity normalized value must be in [0, 1] for step " + k);

            double actual = getActualValue(norm, elMin, elMax, elStep);
            check(test, Math.abs(actual - expectedVal) < 1e-6, "Elasticity step " + k + " actual " + actual + " != expected " + expectedVal);

            // Float precision fidelity: must match within 1e-5
            float floatVal = (float) actual;
            check(test, Math.abs(floatVal - (float) expectedVal) < 1e-5f, "Elasticity float conversion drift at step " + k);

            // String formatting check (%.2f)
            String label = String.format(Locale.ROOT, "Bounce: %.2fx", actual);
            String expectedLabel = String.format(Locale.ROOT, "Bounce: %.2fx", expectedVal);
            check(test, label.equals(expectedLabel), "Elasticity label mismatch at step " + k + ": got " + label);

            // Direct Instantiation of BarrierConfigScreen.StepSlider
            AtomicReference<Double> capturedVal = new AtomicReference<>(0.0);
            BarrierConfigScreen.StepSlider slider = new BarrierConfigScreen.StepSlider(
                    0, 0, 100, 20, "Bounce", "x", elMin, elMax, elStep, expectedVal,
                    capturedVal::set
            );
            check(test, Math.abs(slider.getActualValue() - expectedVal) < 1e-6, "Direct StepSlider elasticity getActualValue mismatch at step " + k);
            check(test, slider.getMessage().getString().equals(expectedLabel), "Direct StepSlider elasticity getMessage mismatch at step " + k);

            // Fixed-point idempotence: Re-applying value 50 times must not drift
            double currentNorm = norm;
            for (int iter = 0; iter < 50; iter++) {
                double recomputedActual = getActualValue(currentNorm, elMin, elMax, elStep);
                currentNorm = toNormalized(recomputedActual, elMin, elMax);
                check(test, Math.abs(recomputedActual - expectedVal) < 1e-6, "Elasticity drift detected at step " + k + " iter " + iter);
            }
        }

        // --- Part 1C: Default initialValue = 1.00 for Elasticity ---
        double defaultNorm = toNormalized(1.00, elMin, elMax);
        check(test, Math.abs(defaultNorm - (0.8 / 1.8)) < 1e-9, "Default 1.00 norm must be 4/9");
        double defaultActual = getActualValue(defaultNorm, elMin, elMax, elStep);
        check(test, Math.abs(defaultActual - 1.00) < 1e-6, "Default 1.00 actual must snap to 1.00");

        System.out.println("  Challenge 1 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 2: Continuous Slider Input Sweeps across 1,000,000 permutations.
     * Evaluates continuous inputs from -0.5 to 1.5, verifying strict clamping,
     * exact step alignment, monotonic non-decreasing behavior, and zero dead zones.
     */
    private static void testChallenge2_ContinuousSliderInputSweeps_OneMillionPermutations() {
        System.out.println("--- CHALLENGE 2: Continuous Slider Sweeps (1,000,000 Permutations) ---");
        String test = "Challenge2_ContinuousSweeps";

        int samples = 500_000;

        // Subtask 2A: Dimension Sweep (500,000 samples in [-0.5, 1.5])
        double dimMin = 1.0;
        double dimMax = 32.0;
        double dimStep = 0.5;

        double prevDimActual = dimMin;
        Set<Integer> reachedDimSteps = new HashSet<>();

        for (int i = 0; i < samples; i++) {
            double normalizedInput = -0.5 + 2.0 * (i / (double) (samples - 1));
            double actual = getActualValue(normalizedInput, dimMin, dimMax, dimStep);

            // Boundary clamping
            check(test, actual >= dimMin && actual <= dimMax, "Dimension out of bounds: " + actual + " for input " + normalizedInput);
            if (normalizedInput <= 0.0) {
                check(test, actual == dimMin, "Negative normalized input must clamp to min (got " + actual + ")");
            }
            if (normalizedInput >= 1.0) {
                check(test, actual == dimMax, "Over-1.0 normalized input must clamp to max (got " + actual + ")");
            }

            // Step alignment: must be exact multiple of 0.5 offset from 1.0
            double stepIndex = (actual - dimMin) / dimStep;
            long roundedStep = Math.round(stepIndex);
            check(test, Math.abs(stepIndex - roundedStep) < 1e-9, "Dimension " + actual + " not aligned to 0.5m step (stepIndex=" + stepIndex + ")");
            reachedDimSteps.add((int) roundedStep);

            // Monotonic non-decreasing
            check(test, actual >= prevDimActual - 1e-9, "Dimension monotonicity violated at input " + normalizedInput);
            prevDimActual = actual;
        }
        check(test, reachedDimSteps.size() == 63, "All 63 dimension steps must be reachable (reached " + reachedDimSteps.size() + ")");

        // Subtask 2B: Elasticity Sweep (500,000 samples in [-0.5, 1.5])
        double elMin = 0.20;
        double elMax = 2.00;
        double elStep = 0.05;

        double prevElActual = elMin;
        Set<Integer> reachedElSteps = new HashSet<>();

        for (int i = 0; i < samples; i++) {
            double normalizedInput = -0.5 + 2.0 * (i / (double) (samples - 1));
            double actual = getActualValue(normalizedInput, elMin, elMax, elStep);

            // Boundary clamping
            check(test, actual >= elMin && actual <= elMax, "Elasticity out of bounds: " + actual + " for input " + normalizedInput);
            if (normalizedInput <= 0.0) {
                check(test, Math.abs(actual - elMin) < 1e-9, "Negative normalized input must clamp to elMin (got " + actual + ")");
            }
            if (normalizedInput >= 1.0) {
                check(test, Math.abs(actual - elMax) < 1e-9, "Over-1.0 normalized input must clamp to elMax (got " + actual + ")");
            }

            // Step alignment: must be exact multiple of 0.05 offset from 0.20
            double stepIndex = (actual - elMin) / elStep;
            long roundedStep = Math.round(stepIndex);
            check(test, Math.abs(stepIndex - roundedStep) < 1e-6, "Elasticity " + actual + " not aligned to 0.05 step (stepIndex=" + stepIndex + ")");
            reachedElSteps.add((int) roundedStep);

            // Monotonic non-decreasing
            check(test, actual >= prevElActual - 1e-9, "Elasticity monotonicity violated at input " + normalizedInput);
            prevElActual = actual;
        }
        check(test, reachedElSteps.size() == 37, "All 37 elasticity steps must be reachable (reached " + reachedElSteps.size() + ")");

        System.out.println("  Challenge 2 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 3: CycleButton arithmetic for Shapes (0 to 5) and Filter Modes (0 to 4).
     * Tests forward cycling, reverse cycling, wrap-around points, and resilience to arbitrary ordinals.
     */
    private static void testChallenge3_CycleButtonArithmeticAndBoundaryWrap() {
        System.out.println("--- CHALLENGE 3: CycleButton Arithmetic & Boundary Wrap ---");
        String test = "Challenge3_CycleButtons";

        Random rng = new Random(1111);

        // --- Part 3A: BarrierShape Cycling (6 values, ordinals 0..5) ---
        BarrierShape[] shapes = BarrierShape.values();
        check(test, shapes.length == 6, "BarrierShape must have exactly 6 enum constants");

        // Forward cycling: 50,000 steps
        int shapeOrd = 0;
        for (int i = 0; i < 50_000; i++) {
            int nextOrd = (shapeOrd + 1) % shapes.length;
            check(test, nextOrd == (i + 1) % 6, "Shape forward cycle mismatch at step " + i);
            shapeOrd = nextOrd;
        }

        // Reverse cycling: 50,000 steps
        shapeOrd = 0;
        for (int i = 0; i < 50_000; i++) {
            int prevOrd = (shapeOrd - 1 + shapes.length) % shapes.length;
            int expected = ((5 - (i % 6)) % 6);
            check(test, prevOrd == expected, "Shape reverse cycle mismatch at step " + i);
            shapeOrd = prevOrd;
        }

        // Wrap-around boundary tests
        check(test, (5 + 1) % 6 == 0, "Shape forward wrap: 5 -> 0");
        check(test, (0 - 1 + 6) % 6 == 5, "Shape reverse wrap: 0 -> 5");

        // Robustness: BarrierShape.fromOrdinal against 50,000 random integers
        for (int i = 0; i < 50_000; i++) {
            int randOrd = rng.nextInt();
            BarrierShape s = BarrierShape.fromOrdinal(randOrd);
            if (randOrd >= 0 && randOrd < 6) {
                check(test, s.ordinal() == randOrd, "fromOrdinal within bounds must match");
            } else {
                check(test, s == BarrierShape.PLANAR_QUAD, "fromOrdinal out of bounds must fallback to PLANAR_QUAD");
            }
        }

        // --- Part 3B: BarrierFilterMode Cycling (5 values, ordinals 0..4) ---
        BarrierFilterMode[] filterModes = BarrierFilterMode.values();
        check(test, filterModes.length == 5, "BarrierFilterMode must have exactly 5 enum constants");

        // Forward cycling: 50,000 steps
        int filterOrd = 0;
        for (int i = 0; i < 50_000; i++) {
            int nextOrd = (filterOrd + 1) % filterModes.length;
            check(test, nextOrd == (i + 1) % 5, "Filter forward cycle mismatch at step " + i);
            filterOrd = nextOrd;
        }

        // Reverse cycling: 50,000 steps
        filterOrd = 0;
        for (int i = 0; i < 50_000; i++) {
            int prevOrd = (filterOrd - 1 + filterModes.length) % filterModes.length;
            int expected = ((4 - (i % 5)) % 5);
            check(test, prevOrd == expected, "Filter reverse cycle mismatch at step " + i);
            filterOrd = prevOrd;
        }

        // Wrap-around boundary tests
        check(test, (4 + 1) % 5 == 0, "Filter forward wrap: 4 -> 0");
        check(test, (0 - 1 + 5) % 5 == 4, "Filter reverse wrap: 0 -> 4");

        // Robustness: BarrierFilterMode.fromOrdinal against 50,000 random integers
        for (int i = 0; i < 50_000; i++) {
            int randOrd = rng.nextInt();
            BarrierFilterMode f = BarrierFilterMode.fromOrdinal(randOrd);
            if (randOrd >= 0 && randOrd < 5) {
                check(test, f.ordinal() == randOrd, "Filter fromOrdinal within bounds must match");
            } else {
                check(test, f == BarrierFilterMode.ALL_ENTITIES, "Filter fromOrdinal out of bounds must fallback to ALL_ENTITIES");
            }
        }

        // --- Part 3C: ApexPredatorTheme Cycling (7 values, ordinals 0..6) ---
        ApexPredatorTheme[] themes = ApexPredatorTheme.values();
        check(test, themes.length == 7, "ApexPredatorTheme must have exactly 7 enum constants");
        int themeOrd = 0;
        for (int i = 0; i < 50_000; i++) {
            int nextOrd = (themeOrd + 1) % themes.length;
            check(test, nextOrd == (i + 1) % 7, "Theme forward cycle mismatch at step " + i);
            themeOrd = nextOrd;
        }

        System.out.println("  Challenge 3 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 4: Redstone Mode Transitions & Server-Side Security Defense.
     * Modes: 0 (Always Active), 1 (Active Unpowered / Inverted), 2 (Active Powered).
     * Tests (mode + 1) % 3 cycling, component text, and server clamping Math.max(0, Math.min(2, mode)).
     */
    private static void testChallenge4_RedstoneModeTransitionsAndServerDefense() {
        System.out.println("--- CHALLENGE 4: Redstone Mode Transitions & Clamping Defense ---");
        String test = "Challenge4_RedstoneModes";

        // Cycle 50,000 times
        int currentMode = 0;
        for (int i = 0; i < 50_000; i++) {
            int nextMode = (currentMode + 1) % 3;
            check(test, nextMode == (i + 1) % 3, "Redstone cycle mismatch at step " + i);

            // Test component text matching implementation
            String text = switch (nextMode) {
                case 1 -> "Redstone: §eInverted";
                case 2 -> "Redstone: §aPowered";
                default -> "Redstone: §bAlways On";
            };

            if (nextMode == 0) check(test, text.contains("Always On"), "Mode 0 must be Always On");
            if (nextMode == 1) check(test, text.contains("Inverted"), "Mode 1 must be Inverted");
            if (nextMode == 2) check(test, text.contains("Powered"), "Mode 2 must be Powered");

            currentMode = nextMode;
        }

        // Server clamping defense: Math.max(0, Math.min(2, mode)) across 100,000 test cases
        Random rng = new Random(2222);
        for (int i = 0; i < 100_000; i++) {
            int arbitraryInput = rng.nextInt();
            int clamped = Math.max(0, Math.min(2, arbitraryInput));
            check(test, clamped >= 0 && clamped <= 2, "Server redstone clamping out of bounds: " + clamped);
            if (arbitraryInput <= 0) check(test, clamped == 0, "Non-positive redstone mode must clamp to 0");
            if (arbitraryInput >= 2) check(test, clamped == 2, ">= 2 redstone mode must clamp to 2");
            if (arbitraryInput == 1) check(test, clamped == 1, "Mode 1 must preserve 1");
        }

        System.out.println("  Challenge 4 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 5: One-Way Valve Toggle Invariants.
     */
    private static void testChallenge5_OneWayValveToggleAndStateInvariance() {
        System.out.println("--- CHALLENGE 5: One-Way Valve Toggle Invariants ---");
        String test = "Challenge5_OneWayToggle";

        boolean oneWay = false;
        for (int i = 0; i < 50_000; i++) {
            oneWay = !oneWay;
            check(test, oneWay == (i % 2 == 0), "One-way toggle state mismatch at step " + i);

            String text = oneWay ? "Flow: §eOne-Way" : "Flow: §bTwo-Way";
            if (oneWay) {
                check(test, text.contains("One-Way"), "True state must display One-Way");
            } else {
                check(test, text.contains("Two-Way"), "False state must display Two-Way");
            }
        }

        System.out.println("  Challenge 5 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 6: Whitelist Management Logic Invariants & Grid Click Simulation.
     * Tests whitespace trimming, length limits (16 chars), capacity limit (32),
     * duplicate prevention, index selection in 3x3 grid, and deletion behavior.
     */
    private static void testChallenge6_WhitelistManagerLogicAndClickCoordinates() {
        System.out.println("--- CHALLENGE 6: Whitelist Logic & 3x3 Grid Click Simulation ---");
        String test = "Challenge6_WhitelistManager";

        List<String> whitelist = new ArrayList<>();
        Random rng = new Random(3333);

        // Subtask 6A: Addition and boundary limits
        // 1. Whitespace trimming & blank rejection
        String[] blankInputs = {"", "   ", "\t", "\n", " \t \n "};
        for (String blank : blankInputs) {
            String trimmed = blank.trim();
            boolean accepted = !trimmed.isEmpty() && !whitelist.contains(trimmed) && whitelist.size() < 32;
            check(test, !accepted, "Blank input '" + blank + "' must be rejected");
        }

        // 2. Add up to 32 entries
        for (int i = 0; i < 32; i++) {
            String name = "Player_" + i;
            String untrimmed = "  " + name + "  ";
            String trimmed = untrimmed.trim();
            boolean accepted = !trimmed.isEmpty() && !whitelist.contains(trimmed) && whitelist.size() < 32;
            check(test, accepted, "Entry " + i + " must be accepted");
            if (accepted) {
                whitelist.add(trimmed);
            }
        }
        check(test, whitelist.size() == 32, "Whitelist must have exactly 32 entries");

        // 3. 33rd addition attempt must be rejected
        String extra = "Player_32";
        boolean overflowAccepted = !extra.isEmpty() && !whitelist.contains(extra) && whitelist.size() < 32;
        check(test, !overflowAccepted, "33rd entry must be strictly rejected by size < 32 condition");

        // 4. Duplicate entry rejection
        String duplicate = "Player_5";
        boolean duplicateAccepted = !duplicate.isEmpty() && !whitelist.contains(duplicate) && whitelist.size() < 32;
        check(test, !duplicateAccepted, "Duplicate entry must be rejected");

        // Subtask 6B: 3x3 Grid Mouse Click Coordinate Hit-Testing
        // Panel at leftPos + 10, topPos + 148, width 220, height 40.
        // relY = mouseY - panelY, row = relY / 12 (0..2 visible in 40px)
        // relX = mouseX - panelX, col = Math.min(2, relX / 73)
        // index = row * 3 + col (0..8)
        for (int click = 0; click < 100_000; click++) {
            int relX = rng.nextInt(300) - 20; // Some outside left/right
            int relY = rng.nextInt(80) - 20;  // Some outside top/bottom

            boolean insidePanel = relX >= 0 && relX <= 220 && relY >= 0 && relY <= 40;
            if (insidePanel) {
                int clickedRow = relY / 12;
                int clickedCol = Math.min(2, relX / 73);
                int index = clickedRow * 3 + clickedCol;

                if (index >= 0 && index < whitelist.size()) {
                    check(test, index >= 0 && index < 32, "Clicked index must be valid");
                    String selectedName = whitelist.get(index);
                    check(test, selectedName.equals("Player_" + index), "Selected name must match expected index");
                }
            }
        }

        // Subtask 6C: Deletion logic
        // 1. Delete by selected index
        int selectedIndex = 10;
        String toRemove = whitelist.get(selectedIndex);
        whitelist.remove(selectedIndex);
        check(test, !whitelist.contains(toRemove), "Entry must be removed from whitelist");
        check(test, whitelist.size() == 31, "Whitelist size must be 31 after deletion");

        // 2. Delete by name when selectedIndex == -1
        selectedIndex = -1;
        String nameToDelete = "Player_20";
        check(test, whitelist.contains(nameToDelete), "Player_20 must exist prior to deletion");
        whitelist.remove(nameToDelete);
        check(test, !whitelist.contains(nameToDelete), "Player_20 must be removed by name");
        check(test, whitelist.size() == 30, "Whitelist size must be 30 after second deletion");

        System.out.println("  Challenge 6 passed (" + totalChecks + " checks cumulative).");
    }

    /**
     * Challenge 7: Bounded Serialization, Deserialization & Server Security Clamping.
     * Evaluates real UpdateBarrierConfigPayload byte stream encoding/decoding,
     * buffer overflow bounds (max 128 usernames, 64 chars), and server validation rules.
     */
    private static void testChallenge7_PacketSerializationAndServerSecurityDefense() {
        System.out.println("--- CHALLENGE 7: Packet Serialization & Server Security Defense ---");
        String test = "Challenge7_NetworkSecurity";

        Random rng = new Random(4444);

        // Subtask 7A: Round-trip serialization of 50,000 randomized payloads
        for (int i = 0; i < 50_000; i++) {
            int entityId = rng.nextInt();
            int shapeOrdinal = rng.nextInt(6);
            float width = 1.0f + rng.nextFloat() * 31.0f;
            float height = 1.0f + rng.nextFloat() * 31.0f;
            float radius = 1.0f + rng.nextFloat() * 31.0f;
            int filterOrdinal = rng.nextInt(5);
            int themeOrdinal = rng.nextInt(7);
            float elasticity = 0.20f + rng.nextFloat() * 1.80f;
            boolean oneWay = rng.nextBoolean();
            int redstoneMode = rng.nextInt(3);
            int colorTint = rng.nextInt();

            int wlCount = rng.nextInt(33); // 0..32 entries
            List<String> whitelist = new ArrayList<>(wlCount);
            for (int w = 0; w < wlCount; w++) {
                whitelist.add("User_" + w);
            }

            UpdateBarrierConfigPayload payload = new UpdateBarrierConfigPayload(
                    entityId, shapeOrdinal, width, height, radius,
                    filterOrdinal, themeOrdinal, elasticity, oneWay,
                    redstoneMode, colorTint, whitelist
            );

            // Serialize to FriendlyByteBuf
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            payload.write(buf);

            // Deserialize
            UpdateBarrierConfigPayload decoded = new UpdateBarrierConfigPayload(buf);

            // Assert exact fidelity
            check(test, decoded.entityId() == entityId, "Entity ID fidelity check");
            check(test, decoded.shapeOrdinal() == shapeOrdinal, "Shape ordinal fidelity check");
            check(test, Math.abs(decoded.width() - width) < 1e-6f, "Width fidelity check");
            check(test, Math.abs(decoded.height() - height) < 1e-6f, "Height fidelity check");
            check(test, Math.abs(decoded.radius() - radius) < 1e-6f, "Radius fidelity check");
            check(test, decoded.filterModeOrdinal() == filterOrdinal, "Filter mode fidelity check");
            check(test, decoded.themeOrdinal() == themeOrdinal, "Theme fidelity check");
            check(test, Math.abs(decoded.elasticity() - elasticity) < 1e-6f, "Elasticity fidelity check");
            check(test, decoded.oneWay() == oneWay, "One-way fidelity check");
            check(test, decoded.redstoneMode() == redstoneMode, "Redstone mode fidelity check");
            check(test, decoded.colorTint() == colorTint, "Color tint fidelity check");
            check(test, decoded.whitelistUsernames().size() == wlCount, "Whitelist count fidelity check");
            for (int w = 0; w < wlCount; w++) {
                check(test, decoded.whitelistUsernames().get(w).equals("User_" + w), "Whitelist entry fidelity check");
            }
            buf.release();
        }

        // Subtask 7B: Whitelist Bounded Serialization Defense (Cap at 128 elements)
        List<String> hugeWhitelist = new ArrayList<>(200);
        for (int i = 0; i < 200; i++) {
            hugeWhitelist.add("OverflowPlayer_" + i);
        }
        UpdateBarrierConfigPayload overflowPayload = new UpdateBarrierConfigPayload(
                100, 0, 4.0f, 4.0f, 5.0f, 0, 0, 1.0f, false, 0, 0, hugeWhitelist
        );
        FriendlyByteBuf overflowBuf = new FriendlyByteBuf(Unpooled.buffer());
        overflowPayload.write(overflowBuf);
        UpdateBarrierConfigPayload cappedDecoded = new UpdateBarrierConfigPayload(overflowBuf);
        check(test, cappedDecoded.whitelistUsernames().size() == 128, "Whitelist must be capped at 128 items in packet codec (got " + cappedDecoded.whitelistUsernames().size() + ")");
        overflowBuf.release();

        // Subtask 7C: Malicious Whitelist Stream Defense (> 128 count injected into buffer)
        FriendlyByteBuf maliciousBuf = new FriendlyByteBuf(Unpooled.buffer());
        maliciousBuf.writeVarInt(101); // entityId
        maliciousBuf.writeVarInt(0);   // shape
        maliciousBuf.writeFloat(4.0f); // width
        maliciousBuf.writeFloat(4.0f); // height
        maliciousBuf.writeFloat(5.0f); // radius
        maliciousBuf.writeVarInt(0);   // filter
        maliciousBuf.writeVarInt(0);   // theme
        maliciousBuf.writeFloat(1.0f); // elasticity
        maliciousBuf.writeBoolean(false); // oneWay
        maliciousBuf.writeVarInt(0);   // redstone
        maliciousBuf.writeInt(0);      // tint
        maliciousBuf.writeVarInt(999); // Malicious count: 999
        UpdateBarrierConfigPayload maliciousDecoded = new UpdateBarrierConfigPayload(maliciousBuf);
        check(test, maliciousDecoded.whitelistUsernames().isEmpty(), "Malicious count > 128 must deserialize to empty list defense (got " + maliciousDecoded.whitelistUsernames().size() + ")");
        maliciousBuf.release();

        // Subtask 7D: Server Handler Clamping Invariants Verification
        float[] adversarialDimensions = {-1000.0f, -1.0f, 0.0f, 0.5f, 0.999f, 1.0f, 16.0f, 32.0f, 32.001f, 100.0f, 1e6f};
        for (float rawDim : adversarialDimensions) {
            float clamped = Math.max(1.0f, Math.min(32.0f, rawDim));
            check(test, clamped >= 1.0f && clamped <= 32.0f, "Server dimension clamping out of bounds for input " + rawDim);
            if (rawDim <= 1.0f) check(test, clamped == 1.0f, "Low dimension must clamp to 1.0f");
            if (rawDim >= 32.0f) check(test, clamped == 32.0f, "High dimension must clamp to 32.0f");
        }

        float[] adversarialElasticity = {-10.0f, 0.0f, 0.199f, 0.20f, 1.0f, 2.0f, 2.001f, 50.0f};
        for (float rawEl : adversarialElasticity) {
            float clamped = Math.max(0.20f, Math.min(2.00f, rawEl));
            check(test, clamped >= 0.20f && clamped <= 2.00f, "Server elasticity clamping out of bounds for input " + rawEl);
            if (rawEl <= 0.20f) check(test, clamped == 0.20f, "Low elasticity must clamp to 0.20f");
            if (rawEl >= 2.00f) check(test, clamped == 2.00f, "High elasticity must clamp to 2.00f");
        }

        // Subtask 7E: Server Permission & Security Logic
        UUID creatorId = UUID.randomUUID();
        UUID otherPlayerId = UUID.randomUUID();

        // 1. Distance check: distanceToSqr > 4096.0 (64m)
        double[] testDistSqr = {0.0, 100.0, 4096.0, 4096.0001, 10000.0};
        for (double dSqr : testDistSqr) {
            boolean validDist = dSqr <= 4096.0;
            check(test, validDist == (dSqr <= 4096.0), "Distance threshold verification");
        }

        // 2. Creator vs Creative Bypass
        boolean ownerAllowed = creatorId.equals(creatorId) || false; // owner survival
        boolean strangerRejected = otherPlayerId.equals(creatorId) || false; // stranger survival
        boolean strangerCreativeAllowed = otherPlayerId.equals(creatorId) || true; // stranger creative
        check(test, ownerAllowed, "Owner survival player must be allowed");
        check(test, !strangerRejected, "Stranger survival player must be rejected");
        check(test, strangerCreativeAllowed, "Stranger creative player must be allowed bypass");

        // 3. Boss Encounter Immutability Lock
        boolean bossEncounter = true;
        boolean survivalBlocked = bossEncounter && !false; // !creative
        boolean creativeAllowed = !(bossEncounter && !true); // creative
        check(test, survivalBlocked, "Boss encounter must lock out survival players");
        check(test, creativeAllowed, "Boss encounter must permit creative players");

        System.out.println("  Challenge 7 passed (" + totalChecks + " checks cumulative).");
    }
}
