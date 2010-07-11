/*
 * ManagerApp.java
 */
package gui;

import manager.Manager;
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

/**
 * @author Shirkit
 * The main class of the application.
 */
public class ManagerApp extends SingleFrameApplication {
    Logger logger;
    Manager controller;      // Model
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
        // Initiate log4j logger
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(LOGGER_PROPS);
        Properties props = new Properties(); 
        try {
            props.load(is);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Cannot initialize logging system","Error",JOptionPane.ERROR_MESSAGE);
        }
        PropertyConfigurator.configure(props);
        // Load l10n
        try {
            L10n.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        logger = Logger.getLogger(this.getClass().getPackage().getName());
        logger.info("HonMod manager is starting up...");

        // Create the MVC framework
        controller = Manager.getInstance();
        view = new ManagerGUI(controller);
        ctrl = new ManagerCtrl(controller, view);

        view.setVisible(true);
        // show(new ManagerGUI());
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
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
