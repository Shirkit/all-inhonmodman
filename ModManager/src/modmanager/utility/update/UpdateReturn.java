/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modmanager.utility.update;

import modmanager.business.Mod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shirkit
 */
public class UpdateReturn {

    private ArrayList<Mod> updated;
    private ArrayList<Mod> upToDate;
    private ArrayList<Mod> failed;
    private Map<Mod,Exception> exception;
    private Map<Mod,String> olderVersion;

    public UpdateReturn() {
        upToDate = new ArrayList<Mod>();
        failed = new ArrayList<Mod>();
        updated = new ArrayList<Mod>();
        exception = new HashMap<Mod, Exception>();
        olderVersion = new HashMap<Mod, String>();
    }

    /**
     * This method retrieves the list of mods that failed to update (any reason). You can get the exception that was thrown by using the getException() method.
     * @return the list of mods that failed to update.
     */
    public ArrayList<Mod> getFailedModList() {
        return failed;
    }

    /**
     * This method retrieves the list of mods that were up-to-date.
     * @return the list of mods that were already up-to-date
     */
    public ArrayList<Mod> getUpToDateModList() {
        return upToDate;
    }

    /**
     * This method retrieves the list of mods that were updated with success. You can check the older version of it by using the getOlderVersion() method.
     * @return the list of mods that updated.
     */
    public ArrayList<Mod> getUpdatedModList() {
        return updated;
    }

    /**
     * Adds a mod that failed to update.
     * @param mod to be added.
     * @param e Exception that caused the fail.
     */
    public void addModFailed(Mod mod, Exception e) {
        failed.add(mod);
        exception.put(mod, e);
    }

    /**
     * Adds a mod that was successfully updated.
     * @param mod to be added.
     * @param olderVersion of the mod.
     */
    public void addUpdated(Mod mod, String olderVersion) {
        updated.add(mod);
        this.olderVersion.put(mod, olderVersion);
    }

    /**
     * Adds a mod that was up-to-date.
     * @param mod to be added.
     */
    public void addUpToDate(Mod mod) {
        upToDate.add(mod);
    }

    /**
     * This method retrieves the exception thrown while updating a mod that caused it to fail.
     * @param mod that you want to retrieve the exception.
     * @return the exception that was thrown.
     */
    public Exception getException(Mod mod) {
        return exception.get(mod);
    }

    public String getOlderVersion(Mod mod) {
        return olderVersion.get(mod);
    }
}
