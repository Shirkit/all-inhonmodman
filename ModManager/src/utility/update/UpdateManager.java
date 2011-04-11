/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.update;

import business.ManagerOptions;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

/**
 *
 * @author Shirkit
 */
public class UpdateManager implements Callable<Boolean> {

    public Boolean call() {
        try {
            URL url = new URL(ManagerOptions.MANAGER_CHECK_UPDATE_VERSIONS.trim());
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str = in.readLine();
            in.close();
            if (str == null || str.trim().toLowerCase().contains("error") || str.equalsIgnoreCase(ManagerOptions.getInstance().getVersion())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            try {
                URL url = new URL(ManagerOptions.MANAGER_CHECK_UPDATE_VERSIONS.trim());
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String str = in.readLine();
                in.close();
                if (str == null || str.trim().toLowerCase().contains("error") || str.equalsIgnoreCase(ManagerOptions.getInstance().getVersion())) {
                    return false;
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
