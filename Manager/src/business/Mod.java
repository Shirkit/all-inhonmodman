package business;

import business.actions.Action;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Shirkit
 *
 */
public class Mod {

    // Constants
    public static final String MOD_FILENAME = "mod.xml";
    public static final String ICON_FILENAME = "mod.xml";
    // Attributes
    private String name;
    private String version;
    private String date;
    private String author;
    private String description;
    private File file;
    private File icon;
    private String path;
    private String iconPath;
    private ArrayList<File> files;
    private String application;
    private String appVersion;
    private String managerVersion;
    private String webLink;
    private String updateCheckURL;
    private String updateDownloadURL;
    private ArrayList<Action> actions;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public File getIcon() {
        return icon;
    }

    public void setIcon(File icon) {
        this.icon = icon;
    }

    /**
     * Mod constructor
     * @param path
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Mod(String path) throws FileNotFoundException, IOException {

        setPath(path);
        XML xml = new XML();
        ZIP zip = new ZIP();
        File retorno = zip.openZIP(new File("C:\\peu.honmod"));
        if (retorno.isDirectory()) {
            System.out.println(retorno);
        }
        //retorno = new File(retorno.getAbsolutePath() + "\\mod.xml");
        //xml.loadXML(retorno);





    }
}
