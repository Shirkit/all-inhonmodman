
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Shirkit
 */
public class Bytes {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        File manager = new File("D:\\Jogos\\Heroes of Newerth\\game\\resources999 manager\\heroes\\tremble\\hero.entity");
        File mine = new File("D:\\Jogos\\Heroes of Newerth\\game\\resources999 mine\\heroes\\tremble\\hero.entity");

        FileInputStream fis = new FileInputStream(manager);
        FileOutputStream fos = new FileOutputStream("C:\\manager.txt");
        while (fis.available() > 0) {
            int i = fis.read();
            fos.write(Integer.toString(i).getBytes());
            fos.write(10);
        }
        fos.flush();
        fos.close();
        fis.close();

        fis = new FileInputStream(mine);
        fos = new FileOutputStream("C:\\mine.txt");
        while (fis.available() > 0) {
            int i = fis.read();
            fos.write(Integer.toString(i).getBytes());
            fos.write(10);
        }
        fos.flush();
        fos.close();
        fis.close();
    }
}
