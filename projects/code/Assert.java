/**
 * A tiny stand-in for JUnit's Assert class so the provided tests run with a
 * plain main method. PROVIDED -- do not modify, do not submit.
 */
public class Assert {
    public static void fail(String message) {
        throw new AssertionError(message);
    }

    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            fail(message);
        }
    }

    public static void assertTrue(boolean condition) {
        assertTrue("expected true", condition);
    }

    public static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    public static void assertFalse(boolean condition) {
        assertFalse("expected false", condition);
    }

    public static void assertNull(String message, Object o) {
        assertTrue(message + " -- expected null but was " + o, o == null);
    }

    public static void assertNotNull(String message, Object o) {
        assertTrue(message, o != null);
    }

    public static void assertNotNull(Object o) {
        assertTrue("expected a value but was null", o != null);
    }

    public static void assertEquals(String message, long expected, long actual) {
        assertTrue(message + " -- expected " + expected + " but was " + actual, expected == actual);
    }

    public static void assertEquals(long expected, long actual) {
        assertEquals("wrong value", expected, actual);
    }

    public static void assertEquals(String message, double expected, double actual, double delta) {
        assertTrue(message + " -- expected " + expected + " but was " + actual, Math.abs(expected - actual) <= delta);
    }

    public static void assertEquals(double expected, double actual, double delta) {
        assertEquals("wrong value", expected, actual, delta);
    }

    public static void assertEquals(String message, Object expected, Object actual) {
        assertTrue(message + " -- expected " + expected + " but was " + actual,
                expected == null ? actual == null : expected.equals(actual));
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals("wrong value", expected, actual);
    }

    public static void assertArrayEquals(String message, int[] expected, int[] actual) {
        assertTrue(message + " -- expected " + java.util.Arrays.toString(expected) + " but was "
                + java.util.Arrays.toString(actual), java.util.Arrays.equals(expected, actual));
    }

    public static void assertArrayEquals(Object[] expected, Object[] actual) {
        assertTrue("expected " + java.util.Arrays.toString(expected) + " but was "
                + java.util.Arrays.toString(actual), java.util.Arrays.equals(expected, actual));
    }
}
