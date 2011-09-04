/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nadaHaver;

import modmanager.business.ManagerOptions;
import modmanager.business.Mod;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipException;
import modmanager.utility.ZIP;

/**
 *
 * @author Shirkit
 */
public class NewClass {

    public static void main(String[] args) throws ZipException, FileNotFoundException, IOException {
        String extractZipComment = ZIP.extractZipComment("D:\\Jogos\\Heroes of Newerth\\game\\resources999.s2z");
        ArrayList<String> modArray = new ArrayList<String>();
        ArrayList<String> versionArray = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new StringReader(extractZipComment));

        String str = null;
        boolean isMod = false;
        try {
            while ((str = br.readLine()) != null) {
                if (str.length() > 0) {
                    if (isMod) {
                        int start = str.indexOf("(") + 2; // Jump the ( AND jump the 'v' [ModManager outputs in this format (v1.2.5) so we need to avoid that v also]
                        int end = str.indexOf(")");
                        versionArray.add(str.substring(start, end));
                        modArray.add(str.substring(0, start - 3).trim()); // -3 because 2 from the add up there, and 1 to avoid the (
                    } else if (str.contains("Applied Mods:")) {
                        isMod = true;
                    }
                }
            }
        } catch (IOException e) {
        }

        for (int i = 0; i < 5; i++) {
            Iterator<String> mods = modArray.iterator();
            Iterator<String> versions = versionArray.iterator();

            while (mods.hasNext() && versions.hasNext()) {
                System.out.println(i);
                String stringMod = mods.next();
                String stringVersion = versions.next();
                Mod mod = ManagerOptions.getInstance().getMod(stringMod, stringVersion);
                try {
                    modmanager.controller.Manager.getInstance().enableMod(mod, ManagerOptions.getInstance().isIgnoreGameVersion());
                    mods.remove();
                    versions.remove();
                } catch (Exception ex) {
                }
            }
        }
    }
}
