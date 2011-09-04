/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modmanager.exceptions;

import modmanager.business.Mod;

/**
 *
 * @author Shirkit
 */
public class UpdateModException extends Exception {

    private Mod mod;

    public UpdateModException(Mod mod, Throwable cause) {
        super(cause);
        this.mod = mod;
    }

    public Mod getMod() {
        return mod;
    }

}
