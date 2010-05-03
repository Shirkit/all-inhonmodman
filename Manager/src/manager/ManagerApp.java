/*
 * ManagerApp.java
 */

package manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * @author Shirkit
 * The main class of the application.
 */
public class ManagerApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new ManagerView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
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
        Game g = new Game("D:\\Jogos\\Heroes");
        try {
            g.getVersion();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ManagerApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ManagerApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        launch(ManagerApp.class, args);
    }
}
