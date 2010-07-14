/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.update;

import business.Mod;
import java.util.ArrayList;

/**
 *
 * @author Shirkit
 */
public class UpdateReturn {

    private ArrayList<Mod> updated;
    private ArrayList<Mod> upToDate;
    private ArrayList<Mod> failed;

    public UpdateReturn() {
        upToDate = new ArrayList<Mod>();
        failed = new ArrayList<Mod>();
        updated = new ArrayList<Mod>();
    }

    public ArrayList<Mod> getFailed() {
        return failed;
    }

    public ArrayList<Mod> getUpToDate() {
        return upToDate;
    }

    public ArrayList<Mod> getUpdated() {
        return updated;
    }



}
