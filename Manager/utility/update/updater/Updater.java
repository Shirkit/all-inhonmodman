/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update.updater;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * This is just a copy of the Updater class for saving it inside our project. You must build this class outside and pack into jar for predicted behavior.
 * Get the .jar file, remove it's extension (new name will be "Updater") and when packing the Manager.jar, put it in the root folder.
 * You must call it with the 2 string arguments: [1] = Path to the current Manager Jar. [2] = URL to download the new manager.
 * @author Shirkit
 */
public class Updater {

    public static final String versionFilePath = "version.txt";

    public static void main(String[] args) {
        Dialog dialog = new Dialog();
        try {
            // Validation
            if (args.length <= 1 || args.length >= 3) {
                throw new InvalidParameterException("Invalid nunmber of parameters. Call must be \"java  -jar  Updater.jar  PATH_TO_MANAGER.JAR  URL_TO_DOWNLOAD_NEW_JAR\"");
            }
            File managerJar = new File(args[0]);
            if (!managerJar.exists()) {
                throw new FileNotFoundException(args[0]);
            }

            // Connection
            URL url = new URL(args[1]);
            dialog.updateLabel("Connecting");
            URLConnection connector = url.openConnection();
            dialog.progressBar.setMaximum(connector.getContentLength());
            // Download the file into a temp file
            dialog.updateLabel("Preparing");
            File downloadedFile = new File(managerJar.getParent() + File.separator + "Manager.temp");
            InputStream in = connector.getInputStream();
            OutputStream out = new FileOutputStream(downloadedFile);
            dialog.updateLabel("Downloading");
            copyInputStream(in, out, dialog.progressBar);
            in.close();
            out.flush();
            out.close();
            dialog.updateLabel("Finishing");
            // If any error happens, then we'll move to the older way
            url = new URL(args[2]);
            dialog.updateLabel("Connecting");
            connector = url.openConnection();
            dialog.progressBar.setMaximum(connector.getContentLength());
            // Download the file into a temp file
            dialog.updateLabel("Preparing");
            downloadedFile.delete();
            downloadedFile = new File(managerJar.getParent() + File.separator + "Manager.temp");
            in = connector.getInputStream();
            out = new FileOutputStream(downloadedFile);
            dialog.updateLabel("Downloading");
            copyInputStream(in, out, dialog.progressBar);
            in.close();
            out.flush();
            out.close();

            dialog.updateLabel("Finishing");
            // Replace older manager with the new one
            in = new FileInputStream(downloadedFile);
            out = new FileOutputStream(managerJar);
            copyInputStream(in, out, null);
            in.close();
            out.flush();
            out.close();
            // Delete temp file
            downloadedFile.delete();
            downloadedFile.deleteOnExit();
            // Exit
            Process runManager = Runtime.getRuntime().exec("java -jar " + managerJar.getAbsolutePath());
            System.exit(0);
        } catch (Exception e) {
            dialog.updateLabel("Closing...");
            JOptionPane.showMessageDialog(null, "Failed to update manager\n\n" + e.toString());
            long t0, t1;
            t0 = System.currentTimeMillis();
            do {
                t1 = System.currentTimeMillis();
            } while ((t1 - t0) < (1 * 1000));
            System.exit(0);
        }
    }

    public static byte[] getFile(File zip, String filename) throws FileNotFoundException, ZipException, IOException {
        if (!zip.exists()) {
            throw new FileNotFoundException(zip.getName());
        }

        ZipFile zipFile = new ZipFile(zip);
        Enumeration entries = zipFile.entries();

        ByteArrayOutputStream output;
        byte[] result = null;

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            if (entry.getName().equalsIgnoreCase(filename)) {
                copyInputStream(zipFile.getInputStream(entry), output = new ByteArrayOutputStream(), null);
                result = output.toByteArray();
                output.close();
                return result;
            }
        }

        throw new FileNotFoundException(filename);
    }

    public static void copyInputStream(InputStream in, OutputStream out, JProgressBar bar) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while (((len = in.read(buffer)) >= 0)) {
            if (bar != null) {
                bar.setValue(bar.getValue() + 1024);
            }
            out.write(buffer, 0, len);
        }
    }

    public static File putJarInJar(File source, File destination) throws FileNotFoundException, IOException {
        if (!source.exists() || !destination.exists()) {
            throw new FileNotFoundException();
        }

        JarFile sourceJar = new JarFile(source);
        JarFile destinationJar = new JarFile(destination);
        Enumeration entriesSource = sourceJar.entries();
        Enumeration entriesDestination = destinationJar.entries();
        File temp = new File(source.getAbsolutePath() + ".temp");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(temp), destinationJar.getManifest());

        while (entriesSource.hasMoreElements()) {
            JarEntry entry = (JarEntry) entriesSource.nextElement();
            if (!entry.getName().equals("META-INF/MANIFEST.MF")) {
                jos.putNextEntry(entry);
                InputStream in = sourceJar.getInputStream(entry);
                copyInputStream(in, jos, null);
                in.close();
            }
        }

        while (entriesDestination.hasMoreElements()) {
            JarEntry destEntry = (JarEntry) entriesDestination.nextElement();
            if (sourceJar.getJarEntry(destEntry.getName()) == null) {
                jos.putNextEntry(destEntry);
                InputStream in = destinationJar.getInputStream(destEntry);
                copyInputStream(in, jos, null);
                in.close();
            }
        }

        jos.flush();
        jos.finish();
        jos.close();

        return temp;
    }

    public static class Dialog {

        JProgressBar progressBar;
        JButton cancel;
        JLabel label;
        JFrame frame;

        public Dialog() {
            frame = new JFrame("All-In Hon ModManager Updater");
            // Try to center on screen
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setBounds(((int) dim.getWidth() / 100) * 45, ((int) dim.getHeight() / 10) * 4, 200, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Init values
            cancel = new JButton("Cancel");
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            label = new JLabel("Starting");
            cancel.addActionListener(new ButtonListener());
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(progressBar, BorderLayout.CENTER);
            panel.add(label, BorderLayout.NORTH);
            panel.add(cancel, BorderLayout.SOUTH);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            frame.setContentPane(panel);
            frame.pack();
            frame.setVisible(true);
        }

        public void updateLabel(String text) {
            label.setText(text);
            frame.paint(frame.getGraphics());
        }
    }

    public static class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int i = JOptionPane.showConfirmDialog(null, "Do you really want to cancel?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (i == 0) {
                System.exit(0);
            }
        }
    }
}
