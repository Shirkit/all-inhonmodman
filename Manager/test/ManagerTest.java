
import business.Manager;
import business.Mod;
import business.XML;
import java.io.File;
import java.io.FileNotFoundException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Shirkit
 */
public class ManagerTest {

    public static void main(String args[]) throws FileNotFoundException {
        File f = new File("C:\\2.xml");
        Mod m = XML.xmlToMod(f);
        System.err.println(Manager.get().compareModsVersions(m.getVersion(), "0.0.0.0.1-1.27"));
    }
}

