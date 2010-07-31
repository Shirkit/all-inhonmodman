/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.net.URL;
import java.io.IOException;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

/**
 *
 * @author Shirkit
 */
public class JarFileLoader extends URLClassLoader {

    public JarFileLoader(URL[] urls) {
        super(urls);
    }

    public void addFile(String path) throws MalformedURLException {
        String urlPath = "jar:file:/" + path + "!/";
        addURL(new URL(urlPath));
    }
}
