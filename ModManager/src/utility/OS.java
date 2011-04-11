package utility;

/**
 * This class handles the OS business.
 * @author Shirkit
 */
public class OS {

    /**
     * Check if we are on MS Windows OS
     * TODO: this needs to be tested on different systems
     *
     * @return true if the platform is MS Windows, false otherwise
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * Check if we are on Linux OS
     * TODO: this needs to be tested on different systems
     *
     * @return true if the platform is Linux
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    /**
     * Check if we are on Apple Mac OS
     * TODO: this needs to be tested on different systems
     *
     * @return true if the platform is Apple Mac OS
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

}
