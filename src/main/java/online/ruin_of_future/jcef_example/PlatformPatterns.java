/*
 * This file is copied and modified from https://github.com/jcefmaven/jcefmaven/blob/8350ce27dd33ded92c6ea798f96caad162913a8f/jcefmaven/src/main/java/me/friwi/jcefmaven/impl/platform/PlatformPatterns.java
 * */
package online.ruin_of_future.jcef_example;

/**
 * Defined patterns for different platforms.
 * Used to detect the current platform from the system properties.
 *
 * @author Fritz Windisch
 */
public class PlatformPatterns {
    public static String[] OS_MACOSX = new String[]{"mac", "darwin"};
    public static String[] OS_LINUX = new String[]{"nux"};
    public static String[] OS_WINDOWS = new String[]{"win"};

    public static String[] ARCH_AMD64 = new String[]{"amd64", "x86_64"};
    public static String[] ARCH_I386 = new String[]{"x86", "i386", "i486", "i586", "i686", "i786"};
    public static String[] ARCH_ARM64 = new String[]{"arm64", "aarch64"};
    public static String[] ARCH_ARM = new String[]{"arm"};
}
