/*
 * This file is copied and modified from https://github.com/jcefmaven/jcefmaven/blob/8350ce27dd33ded92c6ea798f96caad162913a8f/jcefmaven/src/main/java/me/friwi/jcefmaven/CefInitializationException.java
 * */
package online.ruin_of_future.jcef_example;

/**
 * Thrown when JCef failed to initialize.
 *
 * @author Fritz Windisch
 */
public class CefInitializationException extends Exception {
    public CefInitializationException(String message) {
        super(message);
    }

    public CefInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
