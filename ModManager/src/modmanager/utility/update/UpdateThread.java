/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modmanager.utility.update;

import modmanager.business.Mod;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.concurrent.Callable;
import modmanager.controller.Manager;
import java.util.logging.Level;
import java.util.logging.Logger;
import modmanager.utility.FileUtils;
import modmanager.exceptions.UpdateModException;
import java.util.StringTokenizer;

/**
 *
 * @author Shirkit
 */
public class UpdateThread implements Callable<UpdateThread> {

    Mod mod;
    File file;

    public UpdateThread(Mod mod) {
        this.mod = mod;
        this.file = null;
    }

    private void work(int timeout) throws Exception {
        if (mod.getUpdateCheckUrl() != null && mod.getUpdateDownloadUrl() != null) {
            URL url = new URL(mod.getUpdateCheckUrl().trim());
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str = in.readLine();
            in.close();
            if (str != null && !str.toLowerCase().trim().contains("error") && !Manager.getInstance().compareModsVersions(str, "*-" + mod.getVersion())) {
                InputStream is = new URL(mod.getUpdateDownloadUrl().trim()).openStream();
                file = new File(System.getProperty("java.io.tmpdir") + File.separator + new File(mod.getPath()).getName());
                FileOutputStream fos = new FileOutputStream(file, false);
                FileUtils.copyInputStream(is, fos);
                is.close();
                fos.flush();
                fos.close();
            }
        }
    }

    public UpdateThread call() throws UpdateModException {
        Exception e = null;
        // If timeout (and other errors, but that's ok) happens, just try again, this may fix problems for slow connections.
        for (int timeout = 3000; timeout < 10000; timeout += 2000) {
            if (file == null) {
                try {
                    work(timeout);
                    return this; // No error, just finish
                } catch (Exception ex) {
                    e = ex; // Error, let's grab the last error
                    file = null;
                }
            }
        }
        throw new UpdateModException(mod, e);
    }

    public Mod getMod() {
        return mod;
    }

    public File getFile() {
        return file;
    }
}
