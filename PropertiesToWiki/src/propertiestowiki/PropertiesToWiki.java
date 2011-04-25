/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package propertiestowiki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Shirkit
 */
public class PropertiesToWiki {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        if (args.length < 2) {
            System.out.println();
            System.out.println("Usage: prop2wiki SOURCE <DEST> <ENCODE>");
            System.out.println("       SOURCE = Source file");
            System.out.println("       DEST   = Destination file (Optional)");
            System.out.println("                Default output file is \"output.txt\"");
            System.out.println("       ENCODE (Optional) (UTF-8 is the Default)");
            System.out.println("              Check out on Google for Java Encoding for all possible values");
            System.out.println();
            System.out.println("If you want to input your own ENCODE, you must input the DEST parameter.");
            System.exit(0);
        }
        File source = new File(args[0]);
        String encoding = "UTF-8";
        File destination = new File("output.txt");
        if (args.length == 3) {
            destination = new File(args[1]);
            encoding = args[2];
        }
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(source), encoding));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destination), encoding));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (line.startsWith("#") || line.contains("<") || line.contains(">")) {
                    line = "<nowiki>" + line + "</nowiki>";
                }
                line += "<br>\n";
                bw.append(line);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }

    }
}
