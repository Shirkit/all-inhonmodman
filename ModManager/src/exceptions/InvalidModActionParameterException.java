/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

import business.modactions.Action;

/**
 * If a mod tried to do some action, but the parameter of that action is invalid (null and was required, it had to be a number and found a word, it had to be a specific word and found another), this exception is thrown.
 * @author Shirkit
 */
public class InvalidModActionParameterException extends Exception {

    private String name;
    private String version;
    private Action action;

    /**
     * @param name of the mod that has the invalid parameter.
     * @param version of the mod that has the invalid parameter.
     * @param action that tried to do something with a invalid parameter.
     */
    public InvalidModActionParameterException(String name, String version, Action action) {
        super();
        this.name = name;
        this.version = version;
        this.action = action;
    }

    /**
     * @return the version of the mod.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the name of the mod.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the action that tried to do something with a invalid parameter. Can be any action that extends business.actions.Action
     */
    public Action getAction() {
        return action;
    }
}
