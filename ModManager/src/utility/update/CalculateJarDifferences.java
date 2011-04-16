/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update;

import business.ManagerOptions;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

/**
 *
 * @author Shirkit
 */
public class CalculateJarDifferences {

    /**
     * This method should only be run when a new version of the Manager will be released. It tries to make the process more automatic.
     * Currently:
     * - Writes in the Version file in LOCAL DISK (Dropbox folder, so Dropbox must be running!)
     * - Uploads the file to the correct folder tree in the SourceForge project, creating all needed folder on the way.
     */
    public static void main(String[] args) throws ZipException, IOException, JSchException {
        // First step is to get the version we want to release.
        String targetVersion = ManagerOptions.getInstance().getVersion();
        // Get the old jar
        File oldJarVersion = new File("C:\\Users\\Shirkit\\Dropbox\\Public\\versions\\Manager.jar");
        // And the newly generated one
        File newJarVersion = new File("store\\Manager.jar");
        // Target output for where the differences will be generated
        String verionsFile = "C:\\Users\\Shirkit\\Dropbox\\Public\\versions.txt";
        File rootVersionsFolder = new File("C:\\Users\\Shirkit\\Dropbox\\Public\\versions\\");
        File output = new File(rootVersionsFolder, targetVersion + ".jar");

        System.out.println("Version to be released=" + targetVersion);
        System.out.println("Old version file=" + oldJarVersion.getAbsolutePath());
        System.out.println("New version file=" + newJarVersion.getAbsolutePath());
        System.out.println();


        if (calculate(oldJarVersion, newJarVersion, output.getAbsolutePath())) {
            System.out.println("Output file generated.\nPath=" + output.getAbsolutePath() + "\nSize=" + output.length() / 1024 + "KB");

            // Read the current versions file and store it for later
            FileInputStream fis = new FileInputStream(verionsFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyInputStream(fis, baos);
            String s = baos.toString();
            fis.close();
            baos.close();

            System.out.println(s);

            if (s.contains("\n")) {
                System.out.println("Older version=" + s.substring(0, s.indexOf("\n")));
                s = targetVersion + "\n" + s;
            } else {
                System.out.println("First version!");
                s = targetVersion;
            }


            // Write new versions file with the new released version
            FileWriter fw = new FileWriter(verionsFile);
            fw.write(s);
            fw.flush();
            fw.close();
            System.out.println("Versions file written with sucess!");

            fis = new FileInputStream(newJarVersion);
            FileOutputStream fos = new FileOutputStream(rootVersionsFolder + File.separator + "Manager.jar");
            copyInputStream(fis, fos);
            fis.close();
            fos.close();
            System.out.println("Manager.jar file written!");

            System.out.println();
        } else {
            System.err.println("No differences file. Output file not generated.");
        }

        JSch jsch = new JSch();
        Session session = null;
        try {
            System.out.println("Connecting to SF");

            session = jsch.getSession(JOptionPane.showInputDialog("SourceForge Username") + ",all-inhonmodman", "frs.sourceforge.net", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(JOptionPane.showInputDialog("SourceForge Password"));
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            System.out.println("Connected!");
            String root = "/home/frs/project/a/al/all-inhonmodman";
            sftpChannel.cd(root);
            StringTokenizer versionTokens = new StringTokenizer(targetVersion, " ");
            boolean flag = true;
            while (versionTokens.hasMoreTokens()) {
                String s = versionTokens.nextToken();
                if (!cdExists(sftpChannel, s)) {
                    sftpChannel.mkdir(s);
                    flag = false;
                }
                sftpChannel.cd(s);
            }
            if (flag) {
                System.err.println("Version already exists!");
                sftpChannel.exit();
                session.disconnect();
                System.exit(0);
            }
            System.out.println("Uploading file");
            OutputStream out = sftpChannel.put("Manager.jar");
            FileInputStream fis = new FileInputStream(newJarVersion);
            copyInputStream(fis, out);
            out.close();
            fis.close();
            System.out.println("Upload complete");
            sftpChannel.exit();
            session.disconnect();
            System.out.println("SUCESS!");
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static boolean cdExists(ChannelSftp channel, String path) {
        try {
            channel.ls(path);
            return true;
        } catch (Exception e) {
            return false;
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
