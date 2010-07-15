/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update;

import business.Mod;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import manager.Manager;
import utility.ZIP;

/**
 *
 * @author Shirkit
 */
public class UpdateThread  implements Callable<File> {

    Mod mod;

    public UpdateThread(Mod mod) {
        this.mod = mod;
    }    

    public void updateMod() throws MalformedURLException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(mod.getUpdateCheckUrl()).openStream()));
        String str;
        while ((str = in.readLine()) != null) {
            System.out.println(str);
        }

        in.close();
    }

    public File call() throws MalformedURLException, FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(mod.getUpdateCheckUrl().trim()).openStream()));
        String str = in.readLine();
        in.close();
        if (!Manager.getInstance().compareModsVersions(mod.getVersion(), str)) {
            InputStream is = new URL(mod.getUpdateDownloadUrl().trim()).openStream();
            File f = new File(System.getProperty("java.io.tmpdir") + new File(mod.getPath()).getName());
            FileOutputStream fos = new FileOutputStream(f, false);
            ZIP.copyInputStream(is, fos);
            is.close();
            fos.flush();
            fos.close();
            return f;
        } else {
            return null;
        }
    }

    public Mod getMod() {
        return mod;
    }


}
