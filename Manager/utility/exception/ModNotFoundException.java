/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

/**
 *@deprecated 
 * @author Shirkit
 */
public class ModNotFoundException extends Exception {

    private String name;

    public ModNotFoundException(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
