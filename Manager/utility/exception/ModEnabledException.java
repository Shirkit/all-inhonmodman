/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

/**
 * Let's say 2 possible situations:
 * <br/>The mod X was tried to be disabled, but a mod Y requires that the mod X to Y continues enabled. This exception is thrown.
 * <br/>The mod X was tried to be enabled, but a mod Y is currently enabled, and those 2 mods can't be enabled at the same time. This exceptoin is thrown.
 * @author Shirkit
 */
public class ModEnabledException extends Exception {

    private String name;
    private String version;

    /**
     * @param name of the mod that was enabled.
     * @param version of the mod that was enabled.
     */
    public ModEnabledException(String name, String version) {
        super();
        this.name = name;
        this.version = version;
    }

    /**
     * @return the version of the other mod that was enabled.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the name of the other mod that was enabled.
     */
    public String getName() {
        return name;
    }

}
