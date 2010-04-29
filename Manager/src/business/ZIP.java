package business;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Operations with the .hondmod files.
 * @author Shirkit
 */
public class ZIP {

    /**
     * Files are stored in OS temporary folder.
     * @param file of the .honmod file.
     * @return the folder with the unzipped files.
     * @throws IOException
     */
    public File openZIP(File honmod) throws IOException {

        ZipFile zipFile = new ZipFile(honmod.getAbsolutePath());
        Enumeration entries = zipFile.entries();
        File file;

        int id = new Random().nextInt();
        file = new File(System.getProperty("java.io.tmpdir") + "\\ModManager\\");
        if (!file.exists()) {
            file.mkdirs();
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

    public void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    /**
     *
     * @param source Path to the folder to be compressed.
     * @param destination Path to where the .zip file will be created.
     */
    public void createZIP(String source, String file) throws FileNotFoundException, IOException {

        // creates the buffer to generate the zip
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));

        zipDir(source, zos);
        zos.close();
    }

    /**
     * Method that actually does the zipping job.
     * @param dir2zip
     * @param zos
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void zipDir(String dir2zip, ZipOutputStream zos) throws FileNotFoundException, IOException {
        File zipDir = new File(dir2zip);

        //get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[2156];
        int bytesIn = 0;

        //loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; i++) {
            File f = new File(zipDir, dirList[i]);
            if (f.isDirectory()) {
                //if the File object is a directory, call this
                //function again to add its content recursively
                String filePath = f.getPath();
                zipDir(filePath, zos);
                //loop again
                continue;
            }
            //if we reached here, the File object f was not a directory
            //create a FileInputStream on top of f
            FileInputStream fis = new FileInputStream(f);
            //create a  new zipentry
            String path = f.getPath();
            path = path.replace(dir2zip + File.separator, "");
            ZipEntry anEntry = new ZipEntry(path);
            //place the zip entry in the ZipOutputStream object
            zos.putNextEntry(anEntry);
            //now write the content of the file to the ZipOutputStream
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            //close the Stream
            fis.close();
        }

    }
}
