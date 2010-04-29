package business;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Operations with the .hondmod files.
 * @author Shirkit
 */
public class ZIP {

    /**
     * 
     * @param file of the .honmod file
     * @return the folder with the unzipped files.
     * @throws IOException
     */
    public File openZIP(File fileP) throws IOException {

        // Unzip the honmod and throws the files on 'file' and 'icon'
        ZipFile zipFile;
        Enumeration entries;
        zipFile = new ZipFile(fileP.getAbsolutePath());
        entries = zipFile.entries();
        File file;

        file = new File(System.getProperty("java.io.tmpdir") + "\\ModManager");
        if (!file.exists()) {
            file.mkdir();
        }

        int id = new Random().nextInt();
        file = new File(System.getProperty("java.io.tmpdir") + "\\ModManager\\" + id + "\\");
        if (!file.exists()) {
            file.mkdir();
        }

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            if (entry.getName().equals(Mod.MOD_FILENAME)) {
                copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(zipFile.getName())));
            }
        }
        zipFile.close();

        return file;
    }

    public void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }
}

/*if (entry.getName().equals(ICON_FILENAME)) {
copyInputStream(zipFile.getInputStream(entry),
new BufferedOutputStream(new FileOutputStream(getIcon())));
}*/
