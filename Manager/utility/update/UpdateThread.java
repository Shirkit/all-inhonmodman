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
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.concurrent.Callable;
import controller.Manager;
import utility.FileUtils;
import exceptions.UpdateModException;

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

    public UpdateThread call() throws UpdateModException {
        try {
            if (mod.getUpdateCheckUrl() != null && mod.getUpdateDownloadUrl() != null) {
                URL url = new URL(mod.getUpdateCheckUrl().trim());
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String str = in.readLine();
                in.close();
                if (str != null && !str.toLowerCase().trim().contains("error") && !Manager.getInstance().compareModsVersions(mod.getVersion(), str)) {
                    InputStream is = new URL(mod.getUpdateDownloadUrl().trim()).openStream();
                    file = new File(System.getProperty("java.io.tmpdir") + File.separator + new File(mod.getPath()).getName());
                    FileOutputStream fos = new FileOutputStream(file, false);
                    FileUtils.copyInputStream(is, fos);
                    is.close();
                    fos.flush();
                    fos.close();
                }
            }
        } catch (MalformedURLException ex) {
            throw new UpdateModException(mod, ex);
        } catch (ConnectException ex) {
            throw new UpdateModException(mod, ex);
        } catch (NullPointerException ex) {
            throw new UpdateModException(mod, ex);
        } catch (InvalidParameterException ex) {
            throw new UpdateModException(mod, ex);
        } catch (FileNotFoundException ex) {
            throw new UpdateModException(mod, ex);
        } catch (IOException ex) {
            throw new UpdateModException(mod, ex);
        }
        return this;
    }

    public Mod getMod() {
        return mod;
    }

    public File getFile() {
        return file;
    }
}
