/*
 * This file is copied and modified from https://github.com/jcefmaven/jcefmaven/blob/8350ce27dd33ded92c6ea798f96caad162913a8f/jcefmaven/src/main/java/me/friwi/jcefmaven/EnumOS.java
 * */
package online.ruin_of_future.jcef_example;

/**
 * Enum representing all supported operating systems.
 * Fetch the current OS using: <pre>{@code EnumPlatform.getCurrentPlatform().getOs()}</pre>
 *
 * @author Fritz Windisch
 */
public enum EnumOS {
    MACOSX,
    LINUX,
    WINDOWS;

    public boolean isMacOSX() {
        return this == MACOSX;
    }

    public boolean isLinux() {
        return this == LINUX;
    }

    public boolean isWindows() {
        return this == WINDOWS;
    }
}
