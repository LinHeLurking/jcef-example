/*
 * This file is copied and modified from https://github.com/jcefmaven/jcefmaven/blob/8350ce27dd33ded92c6ea798f96caad162913a8f/jcefmaven/src/main/java/me/friwi/jcefmaven/CefAppBuilder.java
 * */

package online.ruin_of_future.jcef_example;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.cef.CefApp;
import org.cef.CefSettings;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class used to configure the JCef environment. Specify
 * an installation directory, arguments to be passed to JCef
 * and configure the embedded {@link org.cef.CefSettings} to
 * your needs. When done, call {@link online.ruin_of_future.jcef_example.CefAppBuilder#build()}
 * to create an {@link org.cef.CefApp} instance.
 * <p>
 * Example use:
 * <pre>
 * {@code
 * //Create a new CefAppBuilder instance
 * CefAppBuilder builder = new CefAppBuilder();
 *
 * //Configure the builder instance
 * builder.setInstallDir(new File("jcef-bundle")); //Default
 * builder.setProgressHandler(new ConsoleProgressHandler()); //Default
 * builder.addJCefArgs("--disable-gpu"); //Just an example
 * builder.getCefSettings().windowless_rendering_enabled = true; //Default - select OSR mode
 *
 * //Set an app handler. Do not use CefApp.addAppHandler(...), it will break your code on MacOSX!
 * builder.setAppHandler(new MavenCefAppHandlerAdapter(){...});
 *
 * //Build a CefApp instance using the configuration above
 * CefApp app = builder.build();
 * }
 * </pre>
 *
 * @author Fritz Windisch, LinHeLurking
 */
public class CefAppBuilder {
    private static final Logger LOGGER = Logger.getLogger(CefAppBuilder.class.getName());

    private static final File DEFAULT_INSTALL_DIR = new File("jcef-bundle");
    private static final List<String> DEFAULT_JCEF_ARGS = new LinkedList<>();
    private static final CefSettings DEFAULT_CEF_SETTINGS = new CefSettings();
    private final Object lock = new Object();
    private final List<String> jcefArgs;
    private final CefSettings cefSettings;
    private File installDir;
    private CefApp instance = null;
    private boolean building = false;

    /**
     * Constructs a new CefAppBuilder instance.
     */
    public CefAppBuilder() {
        installDir = DEFAULT_INSTALL_DIR;
        jcefArgs = new LinkedList<>();
        jcefArgs.addAll(DEFAULT_JCEF_ARGS);
        cefSettings = DEFAULT_CEF_SETTINGS.clone();
    }

    /**
     * Sets the installation directory to use. Defaults to "./jcef-bundle".
     *
     * @param installDir the directory to install to
     */
    public void setInstallDir(File installDir) {
        Objects.requireNonNull(installDir, "installDir cannot be null");
        this.installDir = installDir;
    }

    /**
     * Retrieves a mutable list of arguments to pass to the JCef library.
     * Arguments may contain spaces.
     * <p>
     * Due to installation using maven some arguments may be overwritten
     * again depending on your platform. Make sure to not specify arguments
     * that break the installation process (e.g. subprocess path, resources path...)!
     *
     * @return A mutable list of arguments to pass to the JCef library
     */
    public List<String> getJcefArgs() {
        return jcefArgs;
    }

    /**
     * Add one or multiple arguments to pass to the JCef library.
     * Arguments may contain spaces.
     * <p>
     * Due to installation using maven some arguments may be overwritten
     * again depending on your platform. Make sure to not specify arguments
     * that break the installation process (e.g. subprocess path, resources path...)!
     *
     * @param args the arguments to add
     */
    public void addJcefArgs(String... args) {
        Objects.requireNonNull(args, "args cannot be null");
        jcefArgs.addAll(Arrays.asList(args));
    }

    /**
     * Retrieve the embedded {@link org.cef.CefSettings} instance to change
     * configuration parameters.
     * <p>
     * Due to installation using maven some settings may be overwritten
     * again depending on your platform.
     *
     * @return the embedded {@link org.cef.CefSettings} instance
     */
    public CefSettings getCefSettings() {
        return cefSettings;
    }

    private InputStream getNative() throws UnsupportedPlatformException {
        String platform = EnumPlatform.getCurrentPlatform().getIdentifier();
        InputStream inputStream = CefAppBuilder.class.getResourceAsStream("/jcef-release-tag");
        assert inputStream != null;
        String tag = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining()).trim();
        String nativeResource = "/jcef-natives-" + platform + "-" + tag + ".tar.gz";
        return CefAppBuilder.class.getResourceAsStream(nativeResource);
    }

    private void extractResource(InputStream inputStream) throws IOException {
        // Copied and modified from https://github.com/jcefmaven/jcefmaven/blob/8350ce27dd33ded92c6ea798f96caad162913a8f/jcefmaven/src/main/java/me/friwi/jcefmaven/impl/step/extract/TarGzExtractor.java

        Objects.requireNonNull(inputStream, "input cannot be null");

        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);

        TarArchiveEntry entry;

        while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            File f = new File(installDir, entry.getName());
            if (entry.isDirectory()) {
                boolean created = f.mkdir();
                if (!created) {
                    LOGGER.log(Level.SEVERE, "Unable to create directory '%s', during extraction of archive contents.\n",
                            f.getAbsolutePath());
                } else {
                    if ((entry.getMode() & 0111) != 0 && !f.setExecutable(true, false)) {
                        LOGGER.log(Level.SEVERE, "Unable to mark directory '%s' executable, during extraction of archive contents.\n",
                                f.getAbsolutePath());
                    }
                }
            } else {
                int count;
                int BUFFER_SIZE = 4096;
                byte[] data = new byte[BUFFER_SIZE];
                try (BufferedOutputStream dest = new BufferedOutputStream(
                        new FileOutputStream(f, false), BUFFER_SIZE)) {
                    while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                        dest.write(data, 0, count);
                    }
                }
                if ((entry.getMode() & 0111) != 0 && !f.setExecutable(true, false)) {
                    LOGGER.log(Level.SEVERE, "Unable to mark file '%s' executable, during extraction of archive contents.\n",
                            f.getAbsolutePath());
                }
            }
        }
    }

    private boolean checkInstallation() {
        return false;
    }

    /**
     * Builds a {@link org.cef.CefApp} instance. When called multiple times,
     * will return the previously built instance. This method is thread-safe.
     *
     * @return a built {@link org.cef.CefApp} instance
     * @throws IOException                  if an artifact could not be fetched or IO-actions on disk failed
     * @throws UnsupportedPlatformException if the platform is not supported
     * @throws InterruptedException         if the installation process got interrupted
     * @throws CefInitializationException   if the initialization of JCef failed
     */
    public CefApp build() throws IOException, UnsupportedPlatformException, InterruptedException, CefInitializationException {
        //Check if we already have built an instance
        if (this.instance != null) {
            return this.instance;
        }
        boolean installOk = checkInstallation();
        if (!installOk) {
            if (!installDir.mkdirs()) {
                throw new IOException("Cannot create installation directory");
            }
            // TODO: Extract CEF natives from resources.
            InputStream nativeResource = getNative();
            extractResource(nativeResource);
        }

        //Check if we are in the process of building an instance
        synchronized (lock) {
            if (building) {
                //Check if instance was not created in the meantime
                //to prevent race conditions
                if (this.instance == null) {
                    //Wait until building completed on another thread
                    lock.wait();
                }
                return this.instance;
            }
        }
        this.building = true;
        synchronized (lock) {
            //Setting the instance has to occur in the synchronized block
            //to prevent race conditions
            this.instance = CefInitializer.initialize(this.installDir, this.jcefArgs, this.cefSettings);
            //Add shutdown hook to attempt disposing our instance on jvm exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> this.instance.dispose()));
            //Notify progress handler
            //Resume waiting threads
            lock.notifyAll();
        }
        return this.instance;
    }
}
