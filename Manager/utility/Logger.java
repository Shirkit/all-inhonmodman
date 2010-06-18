/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Shirkit
 */
public class Logger {

    java.util.logging.Logger logger;
    FileHandler fh;

    public Logger() throws IOException {
        logger = java.util.logging.Logger.getLogger("ManagerLogger");
        fh = new FileHandler("/Users/penn/Documents/Development/HoNMoDMan/Manager/mylog.log", true);
        logger.setLevel(Level.ALL);
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        logger.log(Level.WARNING, "My First log");
        
    }

    public void log(LogRecord message) {
        logger.log(message);
    }




}
