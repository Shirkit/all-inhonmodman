/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.exception;

import business.actions.Action;

/**
 *
 * @author Shirkit
 */
public class StringNotFoundModActionException extends Exception {

    private String name;
    private String version;
    private Action action;
    private String string;

    public StringNotFoundModActionException(String name, String version, Action action, String string) {
        super();
        this.name = name;
        this.version = version;
        this.action = action;
        this.string = string;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public Action getAction() {
        return action;
    }

    public String getString() {
        return string;
    }
}
