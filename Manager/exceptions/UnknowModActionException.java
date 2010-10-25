/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package exceptions;

/**
 * If a unsupported action was found, this exception is thrown. You can retrieve the mod's name and the action's name.
 * @author Shirkit
 */
public class UnknowModActionException extends Exception {

    private String action;
    private String name;

    /**
     *
     * @param action name that is unknown.
     * @param name of the mod that has this action.
     */
    public UnknowModActionException(String action, String name) {
        super();
        this.action = action;
        this.action = name;
    }

    /**
     * @return the action's name.
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the mod's name.
     */
    public String getName() {
        return name;
    }



}
