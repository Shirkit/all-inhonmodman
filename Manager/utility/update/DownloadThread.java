/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.concurrent.Callable;
import utility.FileUtils;
import exceptions.UpdateModException;

/**
 *
 * @author Shirkit
 */
public class DownloadThread implements Callable<DownloadThread> {

    File file;
    String url;
    String modName;
    String path;

    private DownloadThread() {
    }

    public DownloadThread(String url, String modName, String path) {
        this.url = url.replaceAll(" ", "%20");
        this.file = null;
        this.modName = modName;
        this.path = path;
    }

    public DownloadThread call() throws UpdateModException {
        try {
            if (url != null) {
                URL urls = new URL(this.url);
                URLConnection connection = urls.openConnection();
                connection.setConnectTimeout(7500);
                InputStream is = urls.openStream();
                String filename = null;
                if (path == null || path.isEmpty()) {
                    String pattern = "[^a-z,A-Z,0-9, ,.]";
                    filename = this.url.substring(this.url.lastIndexOf("/") + 1).replace("%20", " ");
                    filename = filename.replaceAll(pattern, "");
                } else {
                    filename = path;
                }
                FileOutputStream fos = null;
                file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
                fos = new FileOutputStream(file, false);
                FileUtils.copyInputStream(is, fos);
                is.close();
                fos.flush();
                fos.close();
            }
        } catch (MalformedURLException ex) {
            System.out.println(ex);
            file = null;
            throw new UpdateModException(null, ex);
        } catch (ConnectException ex) {
            System.out.println(ex);
            file = null;
            throw new UpdateModException(null, ex);
        } catch (NullPointerException ex) {
            System.out.println(ex);
            file = null;
            throw new UpdateModException(null, ex);
        } catch (InvalidParameterException ex) {
            System.out.println(ex);
            file = null;
            throw new UpdateModException(null, ex);
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            file = null;
            throw new UpdateModException(null, ex);
        } catch (IOException ex) {
            System.out.println(ex);
            file = null;
            throw new UpdateModException(null, ex);
        }
        return this;
    }

    public File getFile() {
        return file;
    }

    public String getModName() {
        return modName;
    }
}
