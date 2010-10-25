/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package exceptions;

import business.actions.Action;

/**
 * If a mod tried to do an action that required something to be selected, and nothing was, this exception is thrown.
 * @author Shirkit
 */
public class NothingSelectedModActionException extends Exception {

    private String name;
    private String version;
    private Action action;

    /**
     *
     * @param name of the mod.
     * @param version of the mod.
     * @param action that tried to do something without anything selected.
     */
    public NothingSelectedModActionException(String name, String version, Action action) {
        super();
        this.name = name;
        this.version = version;
        this.action = action;
    }

    /**
     * @return the mod's version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the mod's name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the action that tried to do something without anything selected.
     */
    public Action getAction() {
        return action;
    }



}
