/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update;

import business.ManagerOptions;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 *
 * @author Shirkit
 */
public class UpdateManager implements Callable<Boolean> {

    public Boolean call() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(business.ManagerOptions.getInstance().getUpdateCheckUrl().trim()).openStream()));
            String str = in.readLine();
            in.close();
            if (str.equalsIgnoreCase(ManagerOptions.getInstance().getVersion())) {
                return new Boolean(false);
            }
            return new Boolean(true);
        } catch (Exception e) {
            return new Boolean(false);
        }
    }
}
