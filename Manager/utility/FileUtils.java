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

    public static void copyFolderToFolder(File source, File destination) throws FileNotFoundException, IOException {
        File[] fileList = source.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            File sourceFile = fileList[i];
            File destinationFile = new File(destination, sourceFile.getName());
            if (sourceFile.isDirectory()) {
                if (destinationFile.mkdirs()) {
                    copyFolderToFolder(sourceFile, destinationFile);
                }
            } else {
                copyFile(sourceFile, destinationFile);
            }
        }
    }

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
}
