/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modmanager.utility;

import java.io.PrintStream;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 *
 * @author Shirkit
 */
public class StdOutErrLog {

    private static final Logger logger = Logger.getLogger(StdOutErrLog.class);
    public static long i = new Random().nextLong();

    /*
     * This method is used for log fatal errors in a different file from the normal log file.
     */
    public static void tieSystemErrToLog() {
        //System.setOut(createLoggingProxy(System.out));
        System.setErr(createLoggingProxy(System.err));
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            @Override
            public void print(final String string) {
                realPrintStream.print(string);
                logger.fatal(string);
            }
        };
    }
}
