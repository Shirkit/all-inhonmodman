package business;

import business.actions.Action;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.io.File;
import java.io.FileNotFoundException;
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
    @XStreamImplicit
    private ArrayList<Action> actions = new ArrayList<Action>();
    // Extra
    @XStreamOmitField
    private File folder;
    @XStreamOmitField
    /**
     * path of the .honmod file
     */
    private String path;
    @XStreamOmitField
    private int id;
    @XStreamOmitField
    private boolean enabled;
    @XStreamOmitField
    private int priority;

    /**
     * Mod constructor.
     */
    public Mod() {

    }
    
    /**
     * Copies the passed mod by param to this mod.
     * @param mod
     * @deprecated no sense on this method.
     */
    private void copy(Mod mod) {
        this.actions = mod.actions;
        this.application = mod.getApplication();
        this.appversion = mod.getAppVersion();
        this.author = mod.getAuthor();
        this.date = mod.getDate();
        this.description = mod.getDescription();
        this.mmversion = mod.getMmVersion();
        this.name = mod.getName();
        this.updatecheckurl = mod.getUpdateCheckUrl();
        this.version = mod.getVersion();
        this.weblink = mod.getWebLink();
    }

    /**
     * @deprecated No sense on this.
     * @return
     * @throws NullPointerException
     * @throws FileNotFoundException
     */
    public File getXmlFile() throws NullPointerException, FileNotFoundException {
        if (getFolder() == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i > this.getFolder().listFiles().length; i++) {
            if (getFolder().listFiles()[i].getName().equals(MOD_FILENAME)) {
                return getFolder().listFiles()[i];
            }
        }
        throw new FileNotFoundException("mod.xml");
    }

    /**
     * This method compares 2 mods to check if they are equal. It currently tests for the mod's version, name and author.
     * @param mod to be compared to.
     * @return true if mods are equal. false otherwise.
     */
    public boolean equals(Mod mod) {
        if (this.getVersion().equals(mod.getVersion())) {
            if (this.getName().equals(mod.getName())) {
                if (this.getAuthor().equals(mod.getAuthor())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The actions (such as applyafter, insert, editfile) are stored in this array list.
     * @return the array list of the actions.
     */
    public ArrayList<Action> getActions() {
        return actions;
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
    public String getWebLink() {
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

    /**
     * @param path the path of the .honmod file.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     * @return the path of the .honmod file.
     */
    public String getPath() {
        return path;
    }

    /**
     * 
     * @param folder with the .honmod content inside it.
     */
    public void setFolder(File folder) {
        this.folder = folder;
    }

    /**
     * @return the folder with the .honmod content inside it.
     */
    public File getFolder() {
        return folder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @deprecated Wrong implementation.
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
     * Method to check if the mod is enabled.
     * @return true if the mod is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Method to enable the current mod/
     */
    public void enable() {
        enabled = true;
    }

    /**
     * Method to disable the current mod/
     */
    public void disable() {
        enabled = false;
    }

}
