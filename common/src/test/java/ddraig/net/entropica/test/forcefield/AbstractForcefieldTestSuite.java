package ddraig.net.entropica.test.forcefield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Convenient abstract base class for implementing {@link ForcefieldTestSuite}.
 * Provides registry collections and automatic test execution.
 */
public abstract class AbstractForcefieldTestSuite implements ForcefieldTestSuite {

    private final String name;
    private final int tier;
    private final List<TestCase> testCases = new ArrayList<>();

    protected AbstractForcefieldTestSuite(String name, int tier) {
        this.name = name;
        this.tier = tier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getTier() {
        return tier;
    }

    protected void register(String testName, int featureId, String description, TestCase.TestRunnable runnable) {
        testCases.add(new TestCase(testName, tier, featureId, description, runnable));
    }

    protected void register(String testName, int featureId, TestCase.TestRunnable runnable) {
        register(testName, featureId, "", runnable);
    }

    public List<TestCase> getTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    @Override
    public List<TestResult> runTests() {
        List<TestResult> results = new ArrayList<>();
        for (TestCase tc : testCases) {
            results.add(tc.execute());
        }
        return results;
    }
}
