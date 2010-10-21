package business;

import business.actions.*;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.Icon;

/**
 * @author Shirkit
 *
 */
@XStreamAlias("modification")
public class Mod {

    // Constants
    public static final String MOD_FILENAME = "mod.xml";
    public static final String ICON_FILENAME = "icon.png";
    public static final String CHANGELOG_FILENAME = "changelog.txt";
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
    @XStreamOmitField
    private Icon icon;
    @XStreamOmitField
    private String changelog;

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
     * Copies the passed mod by param to this mod. Only it's content are copied, the instances continues to exist.
     * @param mod to be copied.
     */
    public void copy(Mod mod) {
        if (mod.actions != null) {
            this.actions = new ArrayList<Action>(mod.actions);
        }
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
        this.path = mod.getPath();
        this.enabled = mod.isEnabled();
        this.changelog = mod.getChangelog();
    }

    /**
     * The actions (such as applyafter, insert, editfile) are stored in this array list.
     * @return the array list of the actions.
     */
    public ArrayList<Action> getActions() {
        if (actions != null) {
            return actions;
        } else {
            return actions = new ArrayList<Action>();
        }
    }

    /**
     * Get list of actions of a specified type
     * 
     * @param type type of actions to return. See Action.java for action constants
     * @return list of actions of the given type (empty if there are none)
     */
    public ArrayList<Action> getActions(String type) {
        Action act;
        ArrayList<Action> typeActions = new ArrayList<Action>();
        if (actions == null) {
            actions = new ArrayList<Action>();
        }
        for (Iterator actionsIter = actions.iterator(); actionsIter.hasNext();) {
            act = (Action) actionsIter.next();
            if (act.getClass() == getTypeActionClass(type)) {
                typeActions.add(act);
            }
        }
        return typeActions;
    }

    private Class getTypeActionClass(String type) {
        if (type.equals(Action.APPLY_AFTER)) {
            return ActionApplyAfter.class;
        }
        if (type.equals(Action.APPLY_BEFORE)) {
            return ActionApplyBefore.class;
        }
        if (type.equals(Action.COPY_FILE)) {
            return ActionCopyFile.class;
        }
        if (type.equals(Action.DELETE)) {
            return ActionEditFileDelete.class;
        }
        if (type.equals(Action.EDIT_FILE)) {
            return ActionEditFile.class;
        }
        if (type.equals(Action.FIND)) {
            return ActionEditFileFind.class;
        }
        if (type.equals(Action.FIND_UP)) {
            return ActionEditFileFindUp.class;
        }
        if (type.equals(Action.INCOMPATIBILITY)) {
            return ActionIncompatibility.class;
        }
        if (type.equals(Action.INSERT)) {
            return ActionEditFileInsert.class;
        }
        if (type.equals(Action.REPLACE)) {
            return ActionEditFileReplace.class;
        }
        if (type.equals(Action.REQUIREMENT)) {
            return ActionRequirement.class;
        }
        return Action.class;
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
     * Sets the icon of a mod
     * @param icon Icon for this mod
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * Returns icon of a mod
     * @return icon for this mod
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Reurns a String with the content of the mod's Changelog in it.
     */
    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    /**
     * Override for HashSet comparison
     */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        Mod compare = (Mod) o;

        if (compare.getName().equals(this.getName()) && compare.getVersion().equals(this.getVersion())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode() + (int) this.getVersion().hashCode();
    }
}
