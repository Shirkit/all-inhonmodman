/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Shirkit
 */
public class FileUtils {

    /**
     * This uses encoding 'UTF-8' for default.
     * @param f
     * @return
     * @throws IOException
     */
    public static String loadFile(File f) throws IOException {
        String encoding = "UTF-8";
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line).append("\n");
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return sb.toString();
    }
}
