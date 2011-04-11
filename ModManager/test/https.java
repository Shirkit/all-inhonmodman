

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Shirkit
 */
public class https {

    public static void main(String[] args ) throws MalformedURLException, IOException{
        URL url = new URL("https://imo.im/");
        URLConnection con = url.openConnection();
        InputStream is = con.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while (((len = is.read(buffer)) >= 0)) {
            out.write(buffer, 0, len);
        }
        out.flush();
        System.out.println(out.toString());
    }

}
