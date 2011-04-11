/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

/**
 * This is just a copy of the Updater class for saving it inside our project. You must build this class outside and pack into jar for predicted behavior.
 * Get the .jar file, remove it's extension (new name will be "Updater") and when packing the Manager.jar, put it in the root folder.
 * You must call it with the 2 string arguments: [1] = Path to the current Manager Jar. [2] = URL to download the new manager.
 * @author Shirkit
 */
public class Updater {

    public static void main(String[] args) {
        /**
         * 0 - PATH_TO_MANAGER.JAR
         * 1 - CURRENT_MANAGER_VERSION
         * 2 - VERIONS.TXT_CONTROL_FILE
         * 3 - ROOT_WEBSERVER_FOLDER
         * 4 - LOCAL_TEMPORARY_FOLDER
         */
        Dialog dialog = new Dialog();
        try {
            // Validation of arguments
            if (args.length != 4) {
                throw new InvalidParameterException("\n\nInvalid nunmber of parameters.\n\nCall must be \"java  -jar  Updater.jar  PATH_TO_MANAGER.JAR  CURRENT_MANAGER_VERSION  VERIONS.TXT_CONTROL_FILE  ROOT_WEBSERVER_FOLDER  LOCAL_TEMPORARY_FOLDER\"");
            }
            File managerJar = new File(args[0]);
            if (!managerJar.exists()) {
                throw new FileNotFoundException(args[0]);
            }

            dialog.updateLabel("Calculating files");

            // Check in versions file how many files we need to download
            URL verionsFile = new URL(args[2]);
            URLConnection connection = verionsFile.openConnection();
            connection.setConnectTimeout(10000);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            ArrayList<String> versions = new ArrayList<String>();
            while (br.ready()) {
                versions.add(br.readLine());
            }
            br.close();

            int index = versions.indexOf(args[1]);

            // Make sure 'root' will be valid to work with
            String root = args[3];
            if (!root.endsWith("/")) {
                root += "/";
            }

            if (index != -1) {
                // If eveything goes normal
                ArrayList<File> filesDownloaded = new ArrayList<File>();
                // Need to download from the older to the newer and apply the patchs in this order
                int k = 0;
                for (int i = versions.size()-1; i >= 0; i--) {
                    dialog.updateLabel("Connecting " + ++k + "/" + index + " files");
                    URL url = new URL(root + versions.get(i) + ".jar");
                    URLConnection con = url.openConnection();
                    con.setConnectTimeout(10000);
                    dialog.progressBar.setValue(0);
                    dialog.progressBar.setMaximum(con.getContentLength());
                    File f = new File(args[4] + File.separator + versions.get(i) + ".jar");
                    InputStream in = con.getInputStream();
                    OutputStream out = new FileOutputStream(f);
                    dialog.updateLabel("Downloading " + i + "/" + index + " files");
                    copyInputStream(in, out, dialog.progressBar);
                    in.close();
                    out.close();
                    filesDownloaded.add(f);
                }

                // Now the order is already the correct one, just go normal
                Iterator<File> it = filesDownloaded.iterator();
                k = 0;
                while (it.hasNext()) {
                    dialog.updateLabel("Applying " + ++k + "/" + filesDownloaded.size() + " files");
                    File file = it.next();
                    dialog.progressBar.setValue(0);
                    FileInputStream fis = new FileInputStream(file);
                    dialog.progressBar.setMaximum(fis.available());
                    fis.close();
                    putDifsInJar(managerJar, file, dialog.progressBar);
                }
            } else {
                // If, for some reason, didn't go normal, just download the full Manager.jar
                dialog.updateLabel("Connecting");
                URL url = new URL(root + "Manager.jar");
                URLConnection con = url.openConnection();
                con.setConnectTimeout(10000);
                dialog.progressBar.setValue(0);
                dialog.progressBar.setMaximum(con.getContentLength());
                File f = new File(args[4] + File.separator + "Manager.jar");
                InputStream in = con.getInputStream();
                OutputStream out = new FileOutputStream(f);
                dialog.updateLabel("Downloading ");
                copyInputStream(in, out, dialog.progressBar);
                in.close();
                out.close();

                // Replace Manager.jar
                FileInputStream fis = new FileInputStream(f);
                FileOutputStream fos = new FileOutputStream(managerJar);
                copyInputStream(in, out, null);
                in.close();
                out.close();
            }

            dialog.updateLabel("Finishing");
            // Exit
            Process runManager = Runtime.getRuntime().exec("java -jar " + managerJar.getAbsolutePath());
            System.exit(0);
        } catch (Exception e) {
            dialog.updateLabel("Closing...");
            JOptionPane.showMessageDialog(null, "Failed to update manager\n\n" + e.toString(), "Update failed", JOptionPane.ERROR_MESSAGE);
            long t0, t1;
            t0 = System.currentTimeMillis();
            do {
                t1 = System.currentTimeMillis();
            } while ((t1 - t0) < (1 * 1500));
            System.exit(0);
        }
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

    /**
     * This method uses puts the new content of the differenceFile into the Jar targetFile, replacing the files as necessary.
     * @param targetFile
     * @param differenceFile - zip containing the files you want to add into the targetFile.
     * This differenceFile must exsit and have at least 1 entry, or a ZipException will be thrown.
     * @throws ZipException - if a ZIP error has occurred
     * @throws IOException - if an I/O error has occurred
     */
    public static void putDifsInJar(File targetFile, File differenceFile, JProgressBar bar) throws ZipException, IOException {
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
        bar.setMaximum(set.size());
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
            bar.setValue(bar.getValue() + 1);
            copyInputStream(is, zos, null);
            is.close();
        }

        zos.close();
        zos.flush();

        sourceZipFile.close();
        diffZipFile.close();

        FileOutputStream fos = new FileOutputStream(targetFile);
        FileInputStream fis = new FileInputStream(temp);

        copyInputStream(fis, fos, null);

        fis.close();
        fos.close();

        temp.delete();
        temp.deleteOnExit();
    }

    public static class Dialog {

        JProgressBar progressBar;
        JButton cancel;
        JLabel label;
        JFrame frame;

        public Dialog() {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
            }
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
            label.setPreferredSize(new Dimension(100, 35));
            cancel.addActionListener(new ButtonListener());
            JPanel panel = new JPanel(new BorderLayout(0, 5));
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
