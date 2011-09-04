/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modmanager.exceptions;

import modmanager.business.Mod;
import modmanager.business.modactions.Action;

/**
 *
 * @author Shirkit
 */
public class ModFileNotFoundException extends Exception {

    private String name;
    private String version;
    private String file;
    private Action action;
    private Mod mod;

    public ModFileNotFoundException(String name, String version, String file, Action action, Mod mod) {
        this.name = name;
        this.version = version;
        this.file = file;
        this.action = action;
        this.mod = mod;
    }

    public Action getAction() {
        return action;
    }

    public String getFile() {
        return file;
    }

    public Mod getMod() {
        return mod;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
