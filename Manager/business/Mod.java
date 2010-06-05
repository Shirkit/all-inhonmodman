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
    /**
     * Absolute path of the .honmod file
     */
    private String path;
    @XStreamOmitField
    private int id;
    @XStreamOmitField
    private boolean enabled;

    /**
     * Mod constructor.
     */
    public Mod() {
    }

    public Mod(String name, String version, String author) {
        this.version = version;
        this.name = name;
        this.author = author;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Method to check if the mod is enabled.
     * @return true if the mod is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * <b>This should only be called by the Manager.</b><br/>
     * Method to enable the current mod
     */
    public void enable() {
        enabled = true;
    }

    /**
     * <b>This should only be called by the Manager.</b><br/>
     * Method to disable the current mod
     */
    public void disable() {
        enabled = false;
    }
    
    /**
     * Override for HashSet comparison
     */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
    	Mod compare = (Mod)o;
    	
    	if(compare.getName().equals(this.getName()) && compare.getVersion().equals(this.getVersion()))
    		return true;
    	else
    		return false;
    }
    
    @Override
    public int hashCode() {
    	return this.getName().hashCode() + (int)this.getVersion().hashCode();	
    }
}
