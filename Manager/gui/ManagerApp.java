/*
 * ManagerApp.java
 */
package gui;

import business.ManagerOptions;
import controller.Manager;
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
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import utility.FileUtils;
import utility.OS;
import utility.StdOutErrLog;
import utility.update.UpdateManager;

/**
 * @author Shirkit
 * The main class of the application.
 */
public class ManagerApp extends SingleFrameApplication {

    Logger logger;
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
        if(!lockInstance("Manager.lock")) {
            System.exit(0);
        }
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
        StdOutErrLog.tieSystemErrToLog();
        logger = Logger.getLogger(this.getClass().getPackage().getName());
        logger.info("------------------------------------------------------------------------------------------------------------------------");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        Date date = new Date();
        logger.info("HonMod manager is starting.");
        logger.info("Local time: " + dateFormat.format(date));
        logger.info("Java version: " + System.getProperty("java.version"));
        logger.info("HonMod manager version: " + ManagerOptions.getInstance().getVersion());
        logger.info("Running on: " + System.getProperty("os.name") + "|" + System.getProperty("os.version") + "|" + System.getProperty("os.arch"));
        try {
            Manager.getInstance().loadOptions();
            if (ManagerOptions.getInstance().getLanguage() != null && !ManagerOptions.getInstance().getLanguage().isEmpty()) {
                L10n.load(ManagerOptions.getInstance().getLanguage());
            } else {
                L10n.load();
            }
        } catch (IOException ex) {
        }
        logger.info("------------------------------------------------------------------------------------------------------------------------");

        // Look for Manager update
        ExecutorService pool = Executors.newCachedThreadPool();
        Future<Boolean> hasUpdate = pool.submit(new UpdateManager());

        try {
            ctrl = new ManagerCtrl();
        } catch (Exception e) {
            logger.error("Error while starting the manager. " + e.getClass() + " | " + e.getCause() + " | " + e.getMessage(), e);
        }

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
                if (ManagerOptions.getInstance().isAutoUpdate() || JOptionPane.showConfirmDialog(ManagerCtrl.getGUI(), L10n.getString("message.updateavaliabe"), L10n.getString("message.updateavaliabe.title"), JOptionPane.YES_NO_OPTION) == 0) {
                    try {
                        InputStream in = getClass().getResourceAsStream("/Updater");
                        FileOutputStream fos = new FileOutputStream(ManagerOptions.MANAGER_FOLDER + File.separator + "Updater.jar");
                        FileUtils.copyInputStream(in, fos);
                        in.close();
                        fos.close();
                        String currentJar = "";
                        try {
                            currentJar = (ManagerApp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                            if ((OS.isWindows() && currentJar.startsWith("/")) || (currentJar.startsWith("//") && !OS.isWindows())) {
                                currentJar = currentJar.replaceFirst("/", "");
                            }
                        } catch (URISyntaxException ex) {
                        }
                        String updaterPath = System.getProperty("user.dir") + File.separator + "Updater.jar";
                        logger.info("Updating manager. java -jar " + updaterPath + " " + currentJar + " " + ManagerOptions.MANAGER_DOWNLOAD_URL + " " + updaterPath);
                        Runtime.getRuntime().exec("java -jar \"" + updaterPath + "\" \"" + currentJar + "\" \"" + ManagerOptions.MANAGER_DOWNLOAD_URL + "\" \"" + updaterPath + "\"");
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

    /**
     *  Method to prevent multiple instances of the Manager running.
     * @param lockFile path a file. This should be constant.
     * @return true is there is no other instance running, false otehrwise.
     */
    private static boolean lockInstance(final String lockFile) {
        try {
            final File file = new File(lockFile);
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();
            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread() {

                    public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            file.delete();
                        } catch (Exception e) {
                            //log.error("Unable to remove lock file: " + lockFile, e);
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            //log.error("Unable to create and/or lock file: " + lockFile, e);
        }
        return false;
    }
}
