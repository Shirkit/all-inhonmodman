/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update;

import business.ManagerOptions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Shirkit
 */
public class CalculateJarDifferences {

    public static void main(String[] args) throws ZipException, IOException {
        // Method to release a new version
        File oldJarVersion = new File("c:\\jar\\old.jar");
        File newJarVersion = new File("c:\\jar\\new.jar");
        File output = new File("c:\\jar\\output.jar");
        String newVersion = ManagerOptions.getInstance().getVersion();
        String verionsFile = "C:\\Users\\Shirkit\\Dropbox\\Public\\versions.txt";
        


        if (calculate(oldJarVersion, newJarVersion, output.getAbsolutePath())) {
            putDifsInJar(newJarVersion, output);
        }
    }

    /**
     * This method calculates the differences between the files inside a Jar file and generates an output file.
     * The output only contains the files that have difference in their CRC.
     * @param olderVersionJar - file to be compared to.
     * @param newerVersionJar - source file of the comparisson.
     * @param outputDestination - path to the file that will contain the differences in those Jars.
     * @throws ZipException - if a ZIP error has occurred
     * @throws IOException - if an I/O error has occurred
     * @return true if the output file was generated AND has at least one entry inside it, false otherwise.
     */
    private static boolean calculate(File olderVersionJar, File newerVersionJar, String outputDestination) throws ZipException, IOException {
        ZipFile oldZip = new ZipFile(olderVersionJar);
        ZipFile newZip = new ZipFile(newerVersionJar);
        Enumeration oldEntries = oldZip.entries();
        Enumeration newEntries = newZip.entries();

        HashMap<String, Long> map = new HashMap<String, Long>();
        while (newEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) newEntries.nextElement();
            map.put(entry.getName(), entry.getCrc());
        }

        while (oldEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) oldEntries.nextElement();
            Long l = map.get(entry.getName());
            if (l != null && l.longValue() == entry.getCrc()) {
                map.remove(entry.getName());
            }
        }

        if (!map.isEmpty()) {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputDestination));

            Set<Entry<String, Long>> set = map.entrySet();
            Iterator<Entry<String, Long>> it = set.iterator();
            while (it.hasNext()) {
                Entry<String, Long> entry = it.next();
                ZipEntry zipEntry = newZip.getEntry(entry.getKey());
                InputStream is = newZip.getInputStream(zipEntry);
                zos.putNextEntry(zipEntry);
                copyInputStream(is, zos);
                zos.closeEntry();
                is.close();
            }

            zos.flush();
            zos.close();
        }

        oldZip.close();
        newZip.close();

        if (map.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method uses puts the new content of the differenceFile into the Jar targetFile, replacing the files as necessary.
     * @param targetFile
     * @param differenceFile - zip containing the files you want to add into the targetFile.
     * This differenceFile must exsit and have at least 1 entry, or a ZipException will be thrown.
     * @throws ZipException - if a ZIP error has occurred
     * @throws IOException - if an I/O error has occurred
     */
    public static void putDifsInJar(File targetFile, File differenceFile) throws ZipException, IOException {
        File temp = new File(targetFile.getAbsolutePath() + ".temp");
        ZipFile sourceZipFile = new ZipFile(targetFile);
        ZipFile diffZipFile = new ZipFile(differenceFile);

        HashMap<String, ZipEntry> map = new HashMap<String, ZipEntry>();
        Enumeration diffEntries = diffZipFile.entries();
        while (diffEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) diffEntries.nextElement();
            map.put(entry.getName(), entry);
        }

        Enumeration sourceEntries = sourceZipFile.entries();
        while (sourceEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) sourceEntries.nextElement();
            if (map.get(entry.getName()) == null) {
                map.put(entry.getName(), entry);
            }
        }

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(temp));

        Set<Entry<String, ZipEntry>> set = map.entrySet();
        Iterator<Entry<String, ZipEntry>> it = set.iterator();
        while (it.hasNext()) {
            Entry<String, ZipEntry> entry = it.next();
            zos.putNextEntry(entry.getValue());
            InputStream is = null;
            if (diffZipFile.getEntry(entry.getKey()) != null) {
                is = diffZipFile.getInputStream(entry.getValue());
            } else {
                is = sourceZipFile.getInputStream(entry.getValue());
            }
            copyInputStream(is, zos);
            is.close();
        }

        zos.close();
        zos.flush();

        sourceZipFile.close();
        diffZipFile.close();

        FileOutputStream fos = new FileOutputStream(targetFile);
        FileInputStream fis = new FileInputStream(temp);

        copyInputStream(fis, fos);

        fis.close();
        fos.close();

        temp.delete();
        temp.deleteOnExit();
    }

    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while (((len = in.read(buffer)) >= 0)) {
            out.write(buffer, 0, len);
        }
        out.flush();
    }
}
