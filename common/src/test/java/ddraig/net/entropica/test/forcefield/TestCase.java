package ddraig.net.entropica.test.forcefield;

import java.util.Objects;

/**
 * Represents a single test case within the Forcefield E2E Test Suite.
 */
public class TestCase {

    @FunctionalInterface
    public interface TestRunnable {
        void run() throws Throwable;
    }

    private final String name;
    private final int tier;
    private final int featureId;
    private final String description;
    private final TestRunnable runnable;

    public TestCase(String name, int tier, int featureId, String description, TestRunnable runnable) {
        this.name = Objects.requireNonNull(name, "Test name cannot be null");
        this.tier = tier;
        this.featureId = featureId;
        this.description = description != null ? description : "";
        this.runnable = Objects.requireNonNull(runnable, "Test runnable cannot be null");
    }

    public TestCase(String name, int tier, int featureId, TestRunnable runnable) {
        this(name, tier, featureId, "", runnable);
    }

    public String getName() {
        return name;
    }

    public int getTier() {
        return tier;
    }

    public int getFeatureId() {
        return featureId;
    }

    public String getDescription() {
        return description;
    }

    public TestResult execute() {
        long start = System.nanoTime();
        try {
            runnable.run();
            long elapsedNanos = System.nanoTime() - start;
            return TestResult.passed(name, tier, featureId, elapsedNanos / 1_000_000.0);
        } catch (Throwable t) {
            long elapsedNanos = System.nanoTime() - start;
            return TestResult.failed(name, tier, featureId, elapsedNanos / 1_000_000.0, t);
        }
    }
}
