package business;

import business.actions.Action;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Shirkit
 *
 */
@XStreamAlias("modification")
public class Mod {

    // Constants
    public static final String MOD_FILENAME = "mod.xml";
    public static final String ICON_FILENAME = "icon.png";
    // Attributes with Alias
    @XStreamAlias("name")
    @XStreamAsAttribute
    private String name;
    @XStreamAlias("version")
    @XStreamAsAttribute
    private String version;
    @XStreamAlias("date")
    @XStreamAsAttribute
    private String date;
    @XStreamAlias("author")
    @XStreamAsAttribute
    private String author;
    @XStreamAlias("description")
    @XStreamAsAttribute
    private String description;
    @XStreamAlias("application")
    @XStreamAsAttribute
    private String application;
    @XStreamAlias("appversion")
    @XStreamAsAttribute
    private String appversion;
    @XStreamAlias("mmversion")
    @XStreamAsAttribute
    private String mmversion;
    @XStreamAlias("weblink")
    @XStreamAsAttribute
    private String weblink;
    @XStreamAlias("updatecheckurl")
    @XStreamAsAttribute
    private String updatecheckurl;
    @XStreamAlias("updatedownloadurl")
    @XStreamAsAttribute
    private String updatedownloadurl;
    // Extra
    @XStreamOmitField
    private File folder;
    @XStreamOmitField
    /**
     * path of the .honmod file
     */
    private String path;
    @XStreamOmitField
    private ArrayList<Action> actions = new ArrayList<Action>();

    /**
     * Mod constructor
     * @param path to the .honmod file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Mod(String path) throws FileNotFoundException, IOException {

        setPath(path);
        ZIP zip = new ZIP();
        setFolder(zip.openZIP(new File(path)));


        /*Mod a = xml.loadXML(new File("C:\\ae.xml"));
        ActionEditFileFind b = (ActionEditFileFind) ((ActionEditFile) a.actions.get(0)).getActions().get(0);
        System.out.println(b.getContent());*/

        /*this.name = "nome";
        this.actions.add(new ActionEditFile());
        ((ActionEditFile) this.actions.get(0)).getActions().add((ActionEditFileActions) new ActionEditFileFind());
        xml.saveXML(this, new File("C:\\opa.xml"));*/


    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the application
     */
    public String getApplication() {
        return application;
    }

    /**
     * @return the appversion
     */
    public String getAppVersion() {
        return appversion;
    }

    /**
     * @return the mmversion
     */
    public String getMmVersion() {
        return mmversion;
    }

    /**
     * @return the weblink
     */
    public String getWeLiink() {
        return weblink;
    }

    /**
     * @return the updatecheckurl
     */
    public String getUpdateCheckUrl() {
        return updatecheckurl;
    }

    /**
     * @return the updatedownloadurl
     */
    public String getUpdateDownloadUrl() {
        return updatedownloadurl;
    }

    private void setPath(String path) {
        this.path = path;
    }

    /**
     *
     * @return the path of the .honmod file.
     */
    private String getPath() {
        return path;
    }

    /**
     * 
     * @param folder with the .honmod content inside it.
     */
    private void setFolder(File folder) {
        this.folder = folder;
    }

    /**
     * @return the folder with the .honmod content inside it.
     */
    public File getFolder() {
        return folder;
    }

    /**
     * @return File of the icon.
     * @throws FileNotFoundException if the icon wasn't found.
     */
    public File getIcon() throws FileNotFoundException {
        int i = 0;
        while (getFolder().listFiles()[i] != null) {
            if (getFolder().listFiles()[i].getName().equals(Mod.ICON_FILENAME)) {
                return getFolder().listFiles()[i];
            }
            i++;
        }
        throw new FileNotFoundException();
    }

    /**
     * @return File of the .honmod file.
     * @throws FileNotFoundException if the .honmod file wasn't found.
     * @see File
     */
    public File getHonmod() throws FileNotFoundException {
        int i = 0;
        while (getFolder().listFiles()[i] != null) {
            if (getFolder().listFiles()[i].getName().equals(Mod.MOD_FILENAME)) {
                return getFolder().listFiles()[i];
            }
            i++;
        }
        throw new FileNotFoundException();
    }
}
