
import business.Mod;
import business.XML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Shirkit
 */
public class TestarMod {

    public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        File f = new File("C:\\2.xml");
        Mod m = XML.xmlToMod(f);
        XML.modToXml(m, new File("C:\\3.xml"));
    }
}
