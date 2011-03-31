/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Random;

/**
 *
 * @author Shirkit
 */
public class FileUtils {

    /**
     * This uses encoding 'UTF-8' for default.
     * @param f
     * @return
     * @throws IOException
     */
    public static String loadFile(File f) throws IOException {
        String encoding = "UTF-8";
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line).append("\n");
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return sb.toString();
    }

    public static void copyFile(File file, File destination) throws FileNotFoundException, IOException {
        if (destination.exists()) {
            updatePermissions(destination);
        } else {
            destination.createNewFile();
        }
        FileInputStream in = new FileInputStream(file);
        FileOutputStream out = new FileOutputStream(destination);
        copyInputStream(in, out);
        in.close();
        out.close();
    }

    public static void copyFile(File file, String destination) throws FileNotFoundException, IOException {
        copyFile(file, new File(destination));
    }

    public static void writeFile(byte[] file, File destination) throws FileNotFoundException, IOException {
        if (destination.exists()) {
            updatePermissions(destination);
        } else {
            destination.createNewFile();
        }
        ByteArrayInputStream in = new ByteArrayInputStream(file);
        FileOutputStream out = new FileOutputStream(destination);
        copyInputStream(in, out);
        in.close();
        out.close();
    }

    /**
     * Copies the files inside a folder to destination folder. If destinationFolder doesn't exist, it will be created.
     * @param sourceFolder Folder with the files to be copied
     * @param destinationFolder Destination folder for the files to be copied on.
     * @throws FileNotFoundException if a file wasn't found or destination file already exists and couldn't be written on.
     * @throws IOException if a radom I/O occurred.
     */
    public static void copyFolderToFolder(File sourceFolder, File destinationFolder) throws FileNotFoundException, IOException {
        File[] fileList = sourceFolder.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            File sourceFile = fileList[i];
            File destinationFile = new File(destinationFolder, sourceFile.getName());
            if (sourceFile.isDirectory()) {
                if (destinationFile.mkdirs()) {
                    copyFolderToFolder(sourceFile, destinationFile);
                }
            } else {
                copyFile(sourceFile, destinationFile);
            }
        }
    }

    /**
     * Attemps to delete a directory recursively. It first must delete the files inside this folder, so if any of those files failed to delete, all process will fail.
     * @param dir Folder to be deleted.
     * @return true if deleted folder and all files within it, false otherwise.
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     *
     * @param in Generic InputStream for the content to be copied.
     * @param out Generic OutputStream for the content to be put on.
     * @throws IOException if a random I/O Exception occurred.
     */
    public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while (((len = in.read(buffer)) >= 0)) {
            out.write(buffer, 0, len);
        }
        out.flush();
    }

    private static void updatePermissions(File f) {
        f.setWritable(true);
        f.setReadable(true);
        f.setExecutable(true);
    }

    /**
     * This method generates a temporary folder for the Manager operations. It attemps to create a folder in %TEMPDIR%\HoN Mod Manager\%RANDOM FOLDER% directory. If it fails, an exception is thrown.
     * @return a File with the path for the created folder.
     * @throws SecurityException is failed to create a folder.
     */
    public static File generateTempFolder() {
        Random r = new Random();
        File tempFolder = null;
        for (int i = 0; i < 100; i++) {
            tempFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + "HoN Mod Manager" + File.separator + r.nextLong());
            if (tempFolder.exists()) {
                if (!tempFolder.delete()) {
                } else {
                    i = 100;
                }
            }
        }
        if (tempFolder == null) {
            throw new SecurityException();
        }
        tempFolder.mkdirs();
        tempFolder.deleteOnExit();

        return tempFolder;
    }
}
