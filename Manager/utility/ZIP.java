package utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * Operations with the .hondmod files.
 * @author Shirkit
 */
public class ZIP {

    static Logger logger = Logger.getLogger(ZIP.class.getPackage().getName());

    /**
     * Retrives only one file given by it's relative path and name and retrives a byte array of it.
     * @param zip is the zip file to search in.
     * @param filename is the file to look for.
     * @return byte[] of the file.
     * @throws IOException if an I/O error has occurred.
     * @throws FileNotFoundException if a file is missing. Use the Exception.getMessage(). Or the zip file wasn't found, or the filename wasn't found inside the zip.
     * @throws ZipException if a random ZipException occourred.
     */
    public static byte[] getFile(File zip, String filename) throws FileNotFoundException, ZipException, IOException {
        if (!zip.exists()) {
            throw new FileNotFoundException(zip.getName());
        }
        while (filename.charAt(0) == '/') {
            filename = filename.substring(1);
        }

        if (filename.contains("\\")) {
            filename = filename.replace("\\", "/");
        }

        ZipFile zipFile = new ZipFile(zip);
        Enumeration entries = zipFile.entries();

        ByteArrayOutputStream output;
        byte[] result = null;

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            if (entry.getName().equalsIgnoreCase(filename)) {
                FileUtils.copyInputStream(zipFile.getInputStream(entry), output = new ByteArrayOutputStream());
                result = output.toByteArray();
                output.close();
                return result;
            }
        }

        throw new FileNotFoundException(filename);
    }

    public static long getLastModified(File zip, String filename) throws FileNotFoundException, ZipException, IOException {
        if (!zip.exists()) {
            throw new FileNotFoundException(zip.getName());
        }
        while (filename.charAt(0) == '/') {
            filename = filename.substring(1);
        }

        ZipFile zipFile = new ZipFile(zip);
        Enumeration entires = zipFile.entries();

        while (entires.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entires.nextElement();

            if (entry.getName().equalsIgnoreCase(filename)) {
                return entry.getTime();
            }
        }

        throw new FileNotFoundException(filename);
    }

    /**
     * This is the main method. It unzips the .honmod file.
     * @param honmod the file .honmod to be extracted.
     * @param folder the folder to where the .honmod file will be extracted.
     * @return folder with the files extracted.
     * @throws IOException if an I/O error has occurred
     * @throws FileNotFoundException if a file is missing. Use the Exception.getMessage(). Possible values:
     * <br/><b>honmod</b>
     */
    public static File openZIP(File honmod, String folder) throws FileNotFoundException, IOException, ZipException {

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
            //  Depending how the .honmod was zipped, one of these conditions will be satisfied. This is garantee that it handles all folders correclty. Some .honmod doesn't works with .isDirectory method.
            if (entry.isDirectory()) {
                new File(folder + File.separator + entry.getName().substring(0, entry.getName().lastIndexOf("/"))).mkdirs();
            } else if (entry.getName().contains("/")) {
                new File(folder + File.separator + entry.getName().substring(0, entry.getName().lastIndexOf("/"))).mkdirs();
            } else {
                FileUtils.copyInputStream(zipFile.getInputStream(entry), output = new FileOutputStream(file.getAbsolutePath() + File.separator + entry.getName()));
                output.close();
            }
        }
        zipFile.close();

        return file;

    }

    /**
     *
     * @param source Path to the folder to be compressed.
     * @param file Path to where the .zip file will be created.
     * @throws FileNotFoundException if coudln't create/open a extracted file.
     * @throws IOException if an I/O error has occurred
     */
    public static void createZIP(String source, String file) throws FileNotFoundException, IOException, ZipException {
        // creates the buffer to generate the zip
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
        zipDir(source, zos, source);
        zos.flush();
        zos.close();

    }

    /**
     * Method that actually does the zipping job.
     * @param dir2zip
     * @param zos
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void zipDir(String dir2zip, ZipOutputStream zos, String originalFolder) throws FileNotFoundException, IOException, ZipException {
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
                zipDir(filePath, zos, originalFolder);
                //loop again
                continue;
            }
            //if we reached here, the File object f was not a directory
            //create a FileInputStream on top of f
            FileInputStream fis = new FileInputStream(f);
            //create a  new zipentry
            String path = f.getPath();
            path = path.replace(originalFolder + File.separator, "");
            while (path.contains("\\")) {
                path = path.replace("\\", "/");
            }
            //logger.error("ZIP: " + path);
            ZipEntry anEntry = new ZipEntry(path);
            anEntry.setTime(f.lastModified());
            //place the zip entry in the ZipOutputStream object
            //now write the content of the file to the ZipOutputStream
            zos.putNextEntry(anEntry);
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            anEntry.setTime(f.lastModified());
            //close the Stream
            fis.close();

        }

    }
}
