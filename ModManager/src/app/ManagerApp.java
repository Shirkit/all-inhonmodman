/*
 * ManagerApp.java
 */
package app;

import com.thoughtworks.xstream.io.StreamException;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.application.SingleFrameApplication;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import business.ManagerOptions;
import business.statistics.Statistics;
import controller.Manager;
import gui.ManagerCtrl;
import gui.ManagerGUI;
import gui.l10n.L10n;
import utility.FileUtils;
import utility.OS;
import utility.SplashScreenMain;
import utility.update.UpdateManager;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.util.concurrent.ExecutionException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
import utility.XML;

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
        // Check for lock file.
        if (!lockInstance(FileUtils.getManagerTempFolder() + File.separator + "Manager.lock")) {
            JOptionPane.showMessageDialog(null, "Another instance of the Manager is already running.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        // Checking java version
        if (System.getProperty("java.version").startsWith("1.5") || System.getProperty("java.version").startsWith("1.4")) {
            JOptionPane.showMessageDialog(null, "Please update your JRE environment to the latest version.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        System.setProperty("http.agent", "All-In HoN ModManager");
        
        // A separated thread to run the SplashScreen
        Task<Void, Void> task = new Task<Void, Void>(Application.getInstance()) {

            @Override
            protected Void doInBackground() throws Exception {
                SplashScreenMain splashScreen = new SplashScreenMain(new ImageIcon(getClass().getResource("/gui/resources/splash.jpg")));
                return null;
            }
        };
        task.execute();

        // And a another thread to continue loading the manager
        Task<Void, Void> task2 = new Task<Void, Void>(Application.getInstance()) {

            @Override
            protected Void doInBackground() throws Exception {

                // Look for Manager update
                final ExecutorService pool = Executors.newCachedThreadPool();
                final Future<Boolean> hasUpdate = pool.submit(new UpdateManager());

                // Initiate log4j logger and the fatal logger
                ClassLoader cl = this.getClass().getClassLoader();
                InputStream is = cl.getResourceAsStream(LOGGER_PROPS);
                Properties props = new Properties();
                try {
                    props.load(is);
                    // Change path but don't change the filename
                    props.setProperty("log4j.appender.file.File", FileUtils.getManagerPerpetualFolder().getAbsolutePath() + File.separator + props.getProperty("log4j.appender.file.File"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Cannot initialize logging system", "Error", JOptionPane.ERROR_MESSAGE);
                }
                PropertyConfigurator.configure(props);
                //StdOutErrLog.tieSystemErrToLog();
                // Log procedure operations
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
                    // Try load options. They must be loaded now for language loading
                    try {
                        Manager.getInstance().loadOptions();
                    } catch (StreamException e) {
                        logger.error("StreamException from loadOptions(), couldn't load options file.", e);
                        // Mod options is invalid, just ignore and it will be deleted.
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    // Load language, if any
                    if (ManagerOptions.getInstance().getLanguage() != null && !ManagerOptions.getInstance().getLanguage().isEmpty()) {
                        L10n.load(ManagerOptions.getInstance().getLanguage());
                    } else {
                        L10n.load();
                    }
                } catch (IOException ex) {
                }
                logger.info("------------------------------------------------------------------------------------------------------------------------");
                
                // Load the interface
                try {
                    ctrl = new ManagerCtrl();
                } catch (Exception e) {
                    logger.error("Error while starting the manager. " + e.getClass() + " | " + e.getCause() + " | " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(null, " Critical error while starting the manager. " + e.getClass() + " | " + e.getCause() + " | " + e.getMessage(), "Critical Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }


                // Firing a new thread so the Manager can load faster
                Task<Void, Void> t = new Task(Application.getInstance()) {

                    @Override
                    protected Object doInBackground() throws Exception {
                        // Look for Updater.jar file, and try to delete it. If this file is found, it means the last run was an update. We could do some update actions here.
                        File updaterJar = new File(System.getProperty("user.dir") + File.separator + "Updater.jar");
                        if (updaterJar.exists()) {
                            if (!updaterJar.delete()) {
                                updaterJar.deleteOnExit();
                            }
                        }

                        // Wait until the searching for the update is done. This can delay the start of the manager, this should be changed to fire a thread.
                        while (!hasUpdate.isDone()) {
                        }

                        try {
                            if (hasUpdate.get().booleanValue()) {
                                // If there is a new update
                                if (ManagerOptions.getInstance().isAutoUpdate() || JOptionPane.showConfirmDialog(ManagerGUI.getInstance(), L10n.getString("message.updateavaliabe"), L10n.getString("message.updateavaliabe.title"), JOptionPane.YES_NO_OPTION) == 0) {
                                    // And user accepts it
                                    try {
                                        // Extract the Updater jar file
                                        InputStream in = getClass().getResourceAsStream("/resources/Updater");
                                        FileOutputStream fos = new FileOutputStream(ManagerOptions.MANAGER_FOLDER + File.separator + "Updater.jar");
                                        FileUtils.copyInputStream(in, fos);
                                        in.close();
                                        fos.close();
                                        String currentJar = "";
                                        // Get current jar path. Since user may rename this file, we need to do this way
                                        try {
                                            currentJar = (ManagerApp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                                            if ((OS.isWindows() && currentJar.startsWith("/")) || (currentJar.startsWith("//") && !OS.isWindows())) {
                                                currentJar = currentJar.replaceFirst("/", "");
                                            }
                                        } catch (URISyntaxException ex) {
                                        }
                                        String updaterPath = System.getProperty("user.dir") + File.separator + "Updater.jar";
                                        // Run with an String array to avoid errors with blank spaces and uncommon characters
                                        // Gcommer: TODO: Not all users will have "java" in their PATH.
                                        String[] cmd = {"java", "-jar", updaterPath, currentJar, ManagerOptions.getInstance().getVersion(), ManagerOptions.MANAGER_CHECK_UPDATE_VERSIONS, ManagerOptions.MANAGER_CHECK_UPDATE_ROOT_FOLDER, FileUtils.generateTempFolder(false).getAbsolutePath()};
                                        String s = "";
                                        for (int i = 0; i < cmd.length; i++) {
                                            s += " " + cmd[i];
                                        }
                                        logger.info("Updating manager." + s);
                                        Runtime.getRuntime().exec(cmd);
                                        shutdown();
                                    } catch (IOException ex) {
                                        // Failed to launch process
                                        logger.fatal(ex);
                                    }

                                }
                            }
                        } catch (InterruptedException ex) {
                            // Job is never stopped
                        } catch (ExecutionException ex) {
                            // Exceptions are never thrown
                        }
                        pool.shutdown();
                        return null;
                    }
                };
                t.execute();
                SplashScreenMain.getInstance().splashScreenDestruct();
                return null;
            }
        };
        task2.execute();
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
        logger.info("Shutting down!");
        FileUtils.deleteTemporaryFolders(); // This can slow down closing speed if the user applied the mods lots and lots of times
        System.exit(0);
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of ManagerApp
     */
    public static ManagerApp getApplication() {
        return Application.getInstance(ManagerApp.class);
    }

    public static void requestShutdown() {
        getApplication().shutdown();
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
    private boolean lockInstance(final String lockFile) {
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
                            logger.error("Unable to remove lock file: " + lockFile, e);
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            logger.error("Unable to create and/or lock file: " + lockFile, e);
        }
        return false;
    }
}
