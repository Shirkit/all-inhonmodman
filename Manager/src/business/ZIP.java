package business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Operations with the .hondmod files.
 * @author Shirkit
 */
public class ZIP {

    /**
     * This is the main method. It unzips the .honmod file.
     * @param honmod the file .honmod to be extracted.
     * @param folder the folder to where the .honmod file will be extracted.
     * @return folder with the files extracted.
     * @throws IOException if an I/O error has occurred
     * @throws FileNotFoundException if a file is missing. Use the Exception.getMessage(). Possible values:
     * <br/><b>honmod</b>
     */
    public static File openZIP(File honmod, String folder) throws FileNotFoundException, IOException {

        if (!honmod.exists()) {
            throw new FileNotFoundException("honmod");
        }

        ZipFile zipFile = new ZipFile(honmod.getAbsolutePath());
        Enumeration entries = zipFile.entries();

        // creating the temp folder
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }

        // creating the temp file and it's Streammer
        FileOutputStream output;

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            copyInputStream(zipFile.getInputStream(entry), output = new FileOutputStream(file.getAbsolutePath() + File.separator + entry.getName()));
            output.close();
        }
        zipFile.close();

        return file;
    }

    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while (((len = in.read(buffer)) >= 0)) {
            out.write(buffer, 0, len);
        }
    }

    /**
     *
     * @param source Path to the folder to be compressed.
     * @param file Path to where the .zip file will be created.
     * @throws FileNotFoundException if coudln't create/open a extracted file.
     * @throws IOException if an I/O error has occurred
     */
    public static void createZIP(String source, String file) throws FileNotFoundException, IOException {

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
    private static void zipDir(String dir2zip, ZipOutputStream zos) throws FileNotFoundException, IOException {
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
