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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.Enumeration;
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
 * This is just a copy of the Updater class.
 * @author Shirkit
 */
public class Updater {
    public static final String versionFilePath = "version.txt";

    public static void main(String[] args) throws FileNotFoundException,InvalidParameterException, MalformedURLException, IOException {
        Dialog dialog = new Dialog();
        // Validation
        if (args.length >= 1) {
            throw new InvalidParameterException("No argument");
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
        File output = new File(managerJar.getParent() + File.separator + "Manager.temp");
        InputStream in = connector.getInputStream();
        OutputStream out = new FileOutputStream(output);
        dialog.updateLabel("Downloading");
        copyInputStream(in, out, dialog.progressBar);
        in.close();
        out.flush();
        out.close();
        dialog.updateLabel("Finishing");
        // Find version
        String version = new String(getFile(output, versionFilePath));
        // Rename file
        File finalOutput = new File(output.getParent() + File.separator + "All-In Hon ModManager alpha v" + version + ".jar");
        output.renameTo(finalOutput);
        // Delete older version
        managerJar.delete();
        managerJar.deleteOnExit();

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

    public static class Dialog {
        JProgressBar progressBar;
        JButton cancel;
        JLabel label;
        JFrame frame;

        public Dialog() {
            frame = new JFrame("All-In Hon ModManager Updater");
            // Try to center on screen
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setBounds(((int) dim.getWidth()/100)*45,((int)dim.getHeight()/10)*4,200,200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Init values
            cancel = new JButton("Cancel");
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            label = new JLabel("Starting");
            cancel.addActionListener(new ButtonListener());
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(progressBar,BorderLayout.CENTER);
            panel.add(label,BorderLayout.NORTH);
            panel.add(cancel,BorderLayout.SOUTH);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            frame.setContentPane(panel);
            frame.pack();
            frame.setVisible(true);
        }

        public void updateLabel(String text) {
            label.setText(text);
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