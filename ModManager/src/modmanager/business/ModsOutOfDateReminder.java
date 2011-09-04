/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modmanager.business;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import modmanager.utility.FileUtils;
import modmanager.utility.Game;
import modmanager.utility.XML;

/**
 * This class represents the mod to remind people when they update HoN but don't re-apply the mods.
 * @author Shirkit
 */
public class ModsOutOfDateReminder {

    private static Mod mod = null;

    /**
     * This method loads a mod XML from the resoucers folder, located inside the Manager applicattion, retrieves it and generate a mod instance.
     * @return an instance of a the Mods Out of Date Reminder.
     */
    public static Mod getMod() {
        ModsOutOfDateReminder instance = new ModsOutOfDateReminder();
        try {
            InputStream in = null;
            ByteArrayOutputStream bos = null;
            try {
                in = instance.getClass().getResourceAsStream("/resources/moodrfe2.xml");
                bos = new ByteArrayOutputStream();
                FileUtils.copyInputStream(in, bos);
            } catch (Exception ex) {
            } finally {
                try {
                    in.close();
                    bos.close();
                } catch (Exception ex) {
                }
            }
            mod = (Mod) XML.xmlToMod(bos.toString("UTF-8").replace("%%CURRENT VERSION%%", Game.getInstance().getVersion()));
        } catch (Exception ex) {
        }
        
        return mod;
    }

}
