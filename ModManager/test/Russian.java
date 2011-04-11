
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Shirkit
 */
public class Russian {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //FileInputStream fis = new FileInputStream("C:\\Users\\Shirkit\\Documents\\NetBeansProjects\\Manager\\src\\gui\\l10n\\HonModMan_ru.properties");
        BufferedReader reader = new BufferedReader(new UTF8Reader(new FileInputStream("C:\\Users\\Shirkit\\Documents\\NetBeansProjects\\Manager\\src\\gui\\l10n\\HonModMan_ru.properties")));
    }
}
