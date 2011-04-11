
import business.Mod;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import utility.XML;

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
        File f = new File("C:\\mod.xml");
        Mod m = XML.xmlToMod(f);
    }
}
