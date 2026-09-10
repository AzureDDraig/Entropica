package ddraig.net.entropica.test.forcefield;

import java.util.Objects;

/**
 * Lightweight, robust, self-contained assertion utilities for Entropica tests.
 * Throws standard AssertionError upon violation.
 */
public final class ForcefieldAssert {

    private ForcefieldAssert() {
        // Utility class
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            fail(message != null ? message : "Expected condition to be true, but was false");
        }
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, null);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            fail(message != null ? message : "Expected condition to be false, but was true");
        }
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, null);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            String prefix = message != null ? message + " - " : "";
            fail(prefix + "Expected: <" + expected + ">, but was: <" + actual + ">");
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, null);
    }

    public static void assertNotEquals(Object unexpected, Object actual, String message) {
        if (Objects.equals(unexpected, actual)) {
            String prefix = message != null ? message + " - " : "";
            fail(prefix + "Expected values to differ, but both were: <" + actual + ">");
        }
    }

    public static void assertNotEquals(Object unexpected, Object actual) {
        assertNotEquals(unexpected, actual, null);
    }

    public static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            String prefix = message != null ? message + " - " : "";
            fail(prefix + "Expected: " + expected + ", but was: " + actual);
        }
    }

    public static void assertEquals(long expected, long actual) {
        assertEquals(expected, actual, null);
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        assertNear(expected, actual, delta, message);
    }

    public static void assertEquals(double expected, double actual, double delta) {
        assertNear(expected, actual, delta, null);
    }

    public static void assertEquals(float expected, float actual, float delta, String message) {
        assertNear(expected, actual, delta, message);
    }

    public static void assertEquals(float expected, float actual, float delta) {
        assertNear(expected, actual, delta, null);
    }

    public static void assertNear(double expected, double actual, double epsilon, String message) {
        if (Double.isNaN(expected) || Double.isNaN(actual)) {
            fail((message != null ? message + " - " : "") + "NaN encountered (expected=" + expected + ", actual=" + actual + ")");
        }
        if (Math.abs(expected - actual) > epsilon) {
            String prefix = message != null ? message + " - " : "";
            fail(prefix + "Expected: " + expected + " (+/- " + epsilon + "), but was: " + actual + " (diff=" + Math.abs(expected - actual) + ")");
        }
    }

    public static void assertNear(double expected, double actual, double epsilon) {
        assertNear(expected, actual, epsilon, null);
    }

    public static void assertNear(float expected, float actual, float epsilon, String message) {
        assertNear((double) expected, (double) actual, (double) epsilon, message);
    }

    public static void assertNear(float expected, float actual, float epsilon) {
        assertNear(expected, actual, epsilon, null);
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            fail(message != null ? message : "Expected non-null object, but was null");
        }
    }

    public static void assertNotNull(Object obj) {
        assertNotNull(obj, null);
    }

    public static void assertNull(Object obj, String message) {
        if (obj != null) {
            fail(message != null ? message : "Expected null, but was: <" + obj + ">");
        }
    }

    public static void assertNull(Object obj) {
        assertNull(obj, null);
    }

    public static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            String prefix = message != null ? message + " - " : "";
            fail(prefix + "Expected same instance: <" + expected + ">, but was: <" + actual + ">");
        }
    }

    public static void assertSame(Object expected, Object actual) {
        assertSame(expected, actual, null);
    }

    public static void assertNotSame(Object unexpected, Object actual, String message) {
        if (unexpected == actual) {
            String prefix = message != null ? message + " - " : "";
            fail(prefix + "Expected different instances, but both were: <" + actual + ">");
        }
    }

    public static void assertNotSame(Object unexpected, Object actual) {
        assertNotSame(unexpected, actual, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return (T) t;
            }
            String prefix = message != null ? message + " - " : "";
            throw new AssertionError(prefix + "Expected exception of type " + expectedType.getName() + " to be thrown, but got " + t.getClass().getName(), t);
        }
        String prefix = message != null ? message + " - " : "";
        throw new AssertionError(prefix + "Expected exception of type " + expectedType.getName() + " to be thrown, but nothing was thrown.");
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingRunnable runnable) {
        return assertThrows(expectedType, runnable, null);
    }

    public static void assertDoesNotThrow(ThrowingRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable t) {
            String prefix = message != null ? message + " - " : "";
            throw new AssertionError(prefix + "Expected no exception, but threw: " + t.getClass().getName() + " (" + t.getMessage() + ")", t);
        }
    }

    public static void assertDoesNotThrow(ThrowingRunnable runnable) {
        assertDoesNotThrow(runnable, null);
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

    public static void fail() {
        fail("Assertion failed");
    }
}
