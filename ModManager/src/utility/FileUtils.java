/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import business.ManagerOptions;
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
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFileChooser;

/**
 *
 * @author Shirkit
 */
public class FileUtils {

    /**
     * Loads a text file to a String.
     * @param f file to be read.
     * @param encoding encode to be used for loading the file.
     * @return a String containing the content of the read file.
     * @throws IOException if an I/O error occurs.
     */
    public static String loadFile(File f, String encoding) throws IOException {
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

    /**
     * Copies a source file to target destination
     * @param source file to be copied.
     * @param destination file where source will be copied to.
     * @throws FileNotFoundException if couldn't read/access the source file or if couldn't access(if exists)/create destination file.
     * @throws IOException if an I/O error occurs.
     */
    public static void copyFile(File source, File destination) throws FileNotFoundException, IOException {
        if (destination.exists()) {
            updatePermissions(destination);
        } else {
            destination.createNewFile();
        }
        FileInputStream in = new FileInputStream(source);
        FileOutputStream out = new FileOutputStream(destination);
        copyInputStream(in, out);
        in.close();
        out.close();
    }

    /**
     * Copies a source file to target destination
     * @param source file to be copied.
     * @param destination file where source will be copied to.
     * @throws FileNotFoundException if couldn't read/access the source file or if couldn't access(if exists)/create destination file.
     * @throws IOException if an I/O error occurs.
     */
    public static void copyFile(File source, String destination) throws FileNotFoundException, IOException {
        copyFile(source, new File(destination));
    }

    /**
     * Copies a source file to target destination
     * @param source file to be copied.
     * @param destination file where source will be copied to.
     * @throws FileNotFoundException if couldn't read/access the source file or if couldn't access(if exists)/create destination file.
     * @throws IOException if an I/O error occurs.
     */
    public static void copyFile(String source, String destination) throws FileNotFoundException, IOException {
        copyFile(new File(source), new File(destination));
    }

    /**
     * Writes the content of a byte array to a target file.
     * @param file byte array with the content of the file to be written.
     * @param destination destination file where the file will be written
     * @throws FileNotFoundException if couldn't access, write or create the <b>destination</b> param file.
     * @throws IOException if an I/O error occurs.
     */
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
     * Writes a file with the BOM bytes in the start of the file.
     * @param file byte array with the content of the file to be written.
     * @param destination destination file where the file will be written
     * @throws FileNotFoundException if couldn't access, write or create the <b>destination</b> param file.
     * @throws IOException - if an I/O error occurs.
     */
    public static void writeFileWithBom(byte[] file, File destination) throws FileNotFoundException, IOException {
        boolean writeBom = true;
        if (destination.exists()) {
            updatePermissions(destination);
            FileInputStream fis = new FileInputStream(destination);
            if (fis.available() > 3 && fis.read() == 239 && fis.read() == 187 && fis.read() == 191) {
                writeBom = false;
            }
        } else {
            destination.createNewFile();
            if (file[0] == 239 && file[1] == 187 && file[2] == 191) {
                writeBom = false;
            }
        }
        ByteArrayInputStream in = new ByteArrayInputStream(file);
        FileOutputStream out = new FileOutputStream(destination);
        if (writeBom) {
            out.write(239);
            out.write(187);
            out.write(191);
        }
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

    /**
     * Tries to update the permissions of the target file with the possible {@code File} permissions methods.
     * @param f file to be updated.
     */
    private static void updatePermissions(File f) {
        f.setWritable(true);
        f.setReadable(true);
        f.setExecutable(true);
    }
    private static ArrayList<File> temporaryFolders = new ArrayList<File>();

    /**
     * This method attemps to clean up all the temporary folder folders created during this execution by the method referenced in this doc.
     * @see FileUtils.#generateTempFolder(boolean)
     */
    public static void deleteTemporaryFolders() {
        for (int i = 0; i < temporaryFolders.size(); i++) {
            File file = temporaryFolders.get(i);
            deleteDir(file);
        }
    }

    /**
     * This method generates a temporary folder for the Manager operations. It attemps to create a folder in %TEMPDIR%\HoN Mod Manager\%RANDOM FOLDER% directory. If it fails, an exception is thrown.
     * @param deleteOnExit - wheter the temporary folder should be deleted on exit or not.
     * @return a File with the path for the created folder.
     * @throws SecurityException is failed to create a folder.
     */
    public static File generateTempFolder(boolean deleteOnExit) {
        Random r = new Random();
        File tempFolder = null;
        for (int i = 0; i < 100; i++) {
            tempFolder = new File(getManagerTempFolder() + File.separator + r.nextLong());
            if (tempFolder.exists()) {
                if (!tempFolder.delete()) {
                } else {
                    i = 100;
                }
            } else {
                i = 100;

            }
        }
        if (tempFolder == null) {
            throw new SecurityException();
        }
        tempFolder.mkdirs();
        if (deleteOnExit) {
            temporaryFolders.add(tempFolder);
            tempFolder.deleteOnExit();
        }

        return tempFolder;
    }

    /**
     * This method returns the folder located in Operational System's temporary folder {@code "tmpdir/Hon Mod Manager"}
     * @return the temp folder.
     */
    public static File getManagerTempFolder() {
        File tempFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + "HoN Mod Manager");
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }
        return tempFolder;
    }
    // Caching
    static File perpetualFolder = null;

    /**
     * This method returns the folder that it's content won't be clean by the operational systems from times to times. It is Operational System dependent.
     * @return the target folder.
     */
    public static File getManagerPerpetualFolder() {
        // Caching
        if (perpetualFolder != null) {
            return perpetualFolder;
        }
        File folder = null;

        if (OS.isLinux()) {
            folder = new File("~" + File.separator + ".Heroes of Newerth" + File.separator + "All-In HoN ModManager");
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return new File(ManagerOptions.MANAGER_FOLDER);
                }
            }
        }

        if (OS.isWindows()) {
            folder = new File(new JFileChooser().getFileSystemView().getDefaultDirectory(), "Heroes of Newerth" + File.separator + "All-In HoN ModManager");
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return new File(ManagerOptions.MANAGER_FOLDER);
                }
            }
        }

        if (OS.isMac()) {
            return new File(ManagerOptions.MANAGER_FOLDER);
        }

        perpetualFolder = folder;

        return folder;
    }
}
