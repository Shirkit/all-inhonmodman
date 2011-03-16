/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import utility.FileUtils;
import utility.Game;
import utility.XML;

/**
 *
 * @author Shirkit
 */
public class ModsOutOfDateReminder {

    private static Mod mod = null;

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
