/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

/**
 *
 * @author Shirkit
 */
public class ModVersionMissmatchException extends Exception {

    private String name;
    private String version;

    public ModVersionMissmatchException(String name, String version) {
        super();
        this.name = name;
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

}
