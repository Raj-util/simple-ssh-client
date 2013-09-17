package fi.jpalomaki.ssh.util;

/**
 * Collection of static methods for making assertions.
 * 
 * @author jpalomaki
 */
public final class Assert {
    
    private Assert() {
        // Non-instantiable
    }
    
    /**
     * Asserts that the given string is not <code>null</code> and
     * that it contains at least one non-whitespace character.
     * 
     * @param string String to test
     * @param message Failure message, may be <code>null</code>
     * @throws IllegalArgumentException If the assertion fails
     */
    public static void hasText(String string, String message) throws IllegalArgumentException {
        if (string == null || string.trim().isEmpty()) {
            throwIllegalArgumentException(message);
        }
    }
    
    /**
     * Asserts that the given boolean value is <code>true</code>.
     * 
     * @param value Boolean value to test
     * @param message Failure message, may be <code>null</code>
     * @throws IllegalArgumentException If the assertion fails
     */
    public static void isTrue(boolean value, String message) throws IllegalArgumentException {
        if (!value) {
            throwIllegalArgumentException(message);
        }
    }
    
    /**
     * Asserts that the given object is not <code>null</code>.
     * 
     * @param object Object to test
     * @param message Failure message, may be <code>null</code>
     * @throws IllegalArgumentException If the assertion fails
     */
    public static void notNull(Object object, String message) throws IllegalArgumentException {
        if (object == null) {
            throwIllegalArgumentException(message);
        }
    }
    
    private static void throwIllegalArgumentException(String message) {
        throw message == null ? new IllegalArgumentException() : new IllegalArgumentException(message);
    }
}
