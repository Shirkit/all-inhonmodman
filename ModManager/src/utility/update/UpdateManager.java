/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update;

import business.ManagerOptions;
import controller.Manager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

/**
 *
 * @author Shirkit
 */
public class UpdateManager implements Callable<Boolean> {

    private boolean work() throws MalformedURLException, IOException {
        URL url = new URL(ManagerOptions.MANAGER_CHECK_UPDATE_VERSIONS.trim());
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(5000);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String str = in.readLine();
        in.close();
        if (str == null || str.trim().toLowerCase().contains("error") || Manager.getInstance().compareModsVersions(str.trim().split(" ")[1], "*-" + ManagerOptions.getInstance().getVersion().split(" ")[1])) {
            return false;
        }
        return true;
    }

    public Boolean call() {
        try {
            return work();
        } catch (Exception e) {
            try {
                return work();
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
