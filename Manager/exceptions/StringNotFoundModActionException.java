/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

import business.modactions.Action;

/**
 * If a mod tries to find a String, and it isn't found, this exception is thrown.
 * @author Shirkit
 */
public class StringNotFoundModActionException extends Exception {

    private String name;
    private String version;
    private Action action;
    private String string;

    /**
     * @param name of the mod.
     * @param version of the mod.
     * @param action that thrown this exception.
     * @param string that wasn't found.
     */
    public StringNotFoundModActionException(String name, String version, Action action, String string) {
        super();
        this.name = name;
        this.version = version;
        this.action = action;
        this.string = string;
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
     * @return the action that tried to find the string. It can be ActionEditFileFind or ActionEditFileFindUp.
     */
    public Action getAction() {
        return action;
    }

    /**
     * @return the string that the action tried to find.
     */
    public String getString() {
        return string;
    }
}
