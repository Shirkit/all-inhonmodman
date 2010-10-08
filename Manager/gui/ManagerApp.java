/*
 * ManagerApp.java
 */
package gui;

import business.ManagerOptions;
import java.util.concurrent.ExecutionException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import gui.l10n.L10n;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import utility.FileUtils;
import utility.update.UpdateManager;

/**
 * @author Shirkit
 * The main class of the application.
 */
public class ManagerApp extends SingleFrameApplication {

    Logger logger;
    ManagerGUI view;    // View
    ManagerCtrl ctrl;   // Controller
    // File with log4j configuration
    private static final String LOGGER_PROPS = "utility/log4j.properties";

    /**
     * At startup create and show the main frame of the application. This is where
     * logging system and L10n framework is initialized.
     */
    @Override
    protected void startup() {
        // Checking java version
        if (System.getProperty("java.version").startsWith("1.5") || System.getProperty("java.version").startsWith("1.4")) {
            JOptionPane.showMessageDialog(null, "Please update your JRE environment to the latest version.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Initiate log4j logger
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(LOGGER_PROPS);
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Cannot initialize logging system", "Error", JOptionPane.ERROR_MESSAGE);
        }
        PropertyConfigurator.configure(props);
        // Load l10n
        logger = Logger.getLogger(this.getClass().getPackage().getName());
        logger.info("\n\n------------------------------------------------------------------------------------------------------------------------");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        Date date = new Date();
        logger.info("HonMod manager is starting.");
        logger.info("Local time: " + dateFormat.format(date));
        logger.info("Java version: " + System.getProperty("java.version"));
        logger.info("HonMod manager version: " + ManagerOptions.getInstance().getVersion());
        logger.info("Running on: " + System.getProperty("os.name") + "|" + System.getProperty("os.version") + "|" + System.getProperty("os.arch"));
        try {
            L10n.load();
        } catch (IOException ex) {
        }
        logger.info("\n------------------------------------------------------------------------------------------------------------------------\n");

        // Look for Manager update
        ExecutorService pool = Executors.newCachedThreadPool();
        Future<Boolean> hasUpdate = pool.submit(new UpdateManager());

        ctrl = new ManagerCtrl();

        File updaterJar = new File(System.getProperty("user.dir") + File.separator + "Updater.jar");
        if (updaterJar.exists()) {
            if (!updaterJar.delete()) {
                updaterJar.deleteOnExit();
            }
        }

        while (!hasUpdate.isDone()) {
        }

        try {
            if (hasUpdate.get().booleanValue()) {
                if (ManagerOptions.getInstance().isAutoUpdate() || JOptionPane.showConfirmDialog(null, L10n.getString("message.updateavaliabe"), L10n.getString("message.updateavaliabe.title"), JOptionPane.YES_NO_OPTION) == 0) {
                    try {
                        InputStream in = getClass().getResourceAsStream("/Updater");
                        FileOutputStream fos = new FileOutputStream(ManagerOptions.MANAGER_FOLDER + File.separator + "Updater.jar");
                        FileUtils.copyInputStream(in, fos);
                        in.close();
                        fos.close();
                        String currentJar = "";
                        try {
                            currentJar = (ManagerApp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                            currentJar = currentJar.replaceFirst("/", "");
                        } catch (URISyntaxException ex) {
                        }
                        String updaterPath = System.getProperty("user.dir") + File.separator + "Updater.jar";
                        Process updater = Runtime.getRuntime().exec("java -jar " + updaterPath + " " + currentJar + " " + ManagerOptions.MANAGER_DOWNLOAD_URL);
                        System.out.println("java -jar " + updaterPath + " " + currentJar + " " + ManagerOptions.MANAGER_DOWNLOAD_URL);
                        System.exit(0);
                    } catch (IOException ex) {
                    }

                }
            }
        } catch (InterruptedException ex) {
            // Job is never stopped
        } catch (ExecutionException ex) {
            // Exceptions are never thrown
        }
        pool.shutdown();
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    @Override
    protected void shutdown() {
        super.shutdown();

        logger.error("Shutting down!!");
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of ManagerApp
     */
    public static ManagerApp getApplication() {
        return Application.getInstance(ManagerApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        launch(ManagerApp.class, args);
    }
}
