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

    public DownloadThread(String url) {
        this.url = url;
        this.file = null;
    }

    public DownloadThread call() throws UpdateModException {
        try {
            if (url != null) {
                URL urls = new URL(this.url);
                URLConnection connection = urls.openConnection();
                connection.setConnectTimeout(5000);
                InputStream is = urls.openStream();
                String filename = this.url.substring(this.url.lastIndexOf("/"));
                file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
                FileOutputStream fos = new FileOutputStream(file, false);
                FileUtils.copyInputStream(is, fos);
                is.close();
                fos.flush();
                fos.close();
            }
        } catch (MalformedURLException ex) {
            file = null;
            throw new UpdateModException(null, ex);
        } catch (ConnectException ex) {
            file = null;
            throw new UpdateModException(null, ex);
        } catch (NullPointerException ex) {
            file = null;
            throw new UpdateModException(null, ex);
        } catch (InvalidParameterException ex) {
            file = null;
            throw new UpdateModException(null, ex);
        } catch (FileNotFoundException ex) {
            file = null;
            throw new UpdateModException(null, ex);
        } catch (IOException ex) {
            file = null;
            throw new UpdateModException(null, ex);
        }
        return this;
    }

    public File getFile() {
        return file;
    }
}
