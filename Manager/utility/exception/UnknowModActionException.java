/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

/**
 *
 * @author Shirkit
 */
public class UnknowModActionException extends Exception {

    private String action;
    private String name;

    public UnknowModActionException(String action, String name) {
        super();
        this.action = action;
        this.action = name;
    }

    public String getAction() {
        return action;
    }

    public String getName() {
        return name;
    }



}
