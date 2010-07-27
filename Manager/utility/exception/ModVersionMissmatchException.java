/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

/**
 * If a mod was tried to be enabled, but it's version doesn't matches that HoN's version, this exception is thrown.
 * @author Shirkit
 */
public class ModVersionMissmatchException extends Exception {

    private String name;
    private String version;
    private String appVersion;

    /**
     * @param name of the mod.
     * @param version of the mod.
     */
    public ModVersionMissmatchException(String name, String version, String appVersion) {
        super();
        this.name = name;
        this.version = version;
        this.appVersion = appVersion;
    }

    /**
     * @return the mod's version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the mod's name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the game's version that the Mod can be enabled on.
     */
    public String getAppVersion() {
        return appVersion;
    }
}
