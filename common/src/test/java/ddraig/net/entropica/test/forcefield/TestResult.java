package ddraig.net.entropica.test.forcefield;

/**
 * Encapsulates the execution outcome, duration, and error diagnostics of a TestCase.
 */
public class TestResult {
    private final String name;
    private final int tier;
    private final int featureId;
    private final boolean passed;
    private final String errorMessage;
    private final Throwable throwable;
    private final double durationMs;

    private TestResult(String name, int tier, int featureId, boolean passed, String errorMessage, Throwable throwable, double durationMs) {
        this.name = name;
        this.tier = tier;
        this.featureId = featureId;
        this.passed = passed;
        this.errorMessage = errorMessage;
        this.throwable = throwable;
        this.durationMs = durationMs;
    }

    public static TestResult passed(String name, int tier, int featureId, double durationMs) {
        return new TestResult(name, tier, featureId, true, null, null, durationMs);
    }

    public static TestResult failed(String name, int tier, int featureId, double durationMs, Throwable throwable) {
        String msg = throwable != null ? throwable.getMessage() : "Unknown error";
        if (msg == null && throwable != null) {
            msg = throwable.getClass().getName();
        }
        return new TestResult(name, tier, featureId, false, msg, throwable, durationMs);
    }

    public static TestResult failed(String name, int tier, int featureId, double durationMs, String errorMessage) {
        return new TestResult(name, tier, featureId, false, errorMessage, null, durationMs);
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

    public boolean isPassed() {
        return passed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public double getDurationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        return String.format("[%s] Tier %d (F%02d) - %s (%.2f ms)%s",
                passed ? "PASS" : "FAIL",
                tier,
                featureId,
                name,
                durationMs,
                passed ? "" : " -> " + errorMessage);
    }
}
