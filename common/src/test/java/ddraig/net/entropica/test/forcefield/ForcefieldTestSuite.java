package ddraig.net.entropica.test.forcefield;

import java.util.List;

/**
 * Base interface for all E2E test suites in the Entropica Forcefield & Firmament Barrier System.
 */
public interface ForcefieldTestSuite {

    /**
     * @return Unique human-readable title of this test suite.
     */
    String getName();

    /**
     * @return Numeric tier level: 1 (Feature), 2 (Boundary), 3 (Combinatorial), 4 (Workload).
     */
    int getTier();

    /**
     * Executes all registered tests in this suite and returns their outcomes.
     *
     * @return List of {@link TestResult} instances.
     */
    List<TestResult> runTests();
}
