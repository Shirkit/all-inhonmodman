
import business.ManagerOptions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Shirkit
 */
public class Updater {

    public static void main(String[] args) throws MalformedURLException, IOException {
        URL url = new URL("http://www.play1080.com/newerthmods/abilityrange/version.txt");
        URLConnection connection = url.openConnection();
        System.out.println(connection.getContentType());
        System.out.println(connection.getContentEncoding());
        Map m = connection.getHeaderFields();
        Iterator it = m.values().iterator();
        while (it.hasNext()) {
            Object elem = it.next();
            System.out.println(elem);
        }

    }
}
