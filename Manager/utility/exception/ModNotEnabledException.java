/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

/**
 * If a mod tried to be enabled, but it required another mod to be enabled, and this other mod wasn't enabled, this exception is thrown.
 * @author Shirkit
 */
public class ModNotEnabledException extends Exception {

    private String name;
    private String version;

    /**
     * @param name of the mod that wasn't enabled.
     * @param version of the mod that wasn't enabled.
     */
    public ModNotEnabledException(String name, String version) {
        super();
        this.name = name;
        this.version = version;
    }

    /**
     * @return the version of the mod that was required to be enabled (can be a version expression 1.3-1.7.2)
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the name of the mod that was required to be enabled.
     */
    public String getName() {
        return name;
    }

}
