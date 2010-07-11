/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import utility.Game;
import utility.XML;

/**
 *
 * @author Shirkit
 */
@XStreamAlias("options")
public class ManagerOptions extends Observable {

    @XStreamOmitField
    private String MANAGER_FOLDER;
    @XStreamAlias("mods_folder")
    @XStreamAsAttribute
    private String MODS_FOLDER;
    @XStreamAlias("hon_folder")
    @XStreamAsAttribute
    private String HON_FOLDER;
    @XStreamImplicit
    private Set<Mod> applied;
    @XStreamOmitField
    private ArrayList<Mod> mods;
    @XStreamOmitField
    private String OPTIONS_FILENAME = "managerOptions.xml";
    @XStreamOmitField
    private String HOMEPAGE = "http://sourceforge.net/projects/all-inhonmodman";
    @XStreamOmitField
    private String VERSION = "0.1 BETA";
    @XStreamOmitField
    public static final String PREFS_LOCALE = "locale";
    @XStreamOmitField
    public static final String PREFS_LAF = "laf";
    @XStreamOmitField
    public static final String PREFS_CLARGUMENTS = "clarguments";
    @XStreamOmitField
    public static final String PREFS_HONFOLDER = "honfolder";
    private static ManagerOptions instance;

    private ManagerOptions() {
        setManagerPath(new File(".").getAbsolutePath());
    }

    public static ManagerOptions getInstance() {
        if (instance == null) {
            instance = new ManagerOptions();
        }
        return instance;
    }

    /**
     * Set status of the MVC model to changed and notify all registered observers.
     * Call this method when the data model changed and UI has to be updated.
     */
    public void updateNotify() {
        // Notify observers that model has been updated
        setChanged();
        notifyObservers();
    }

    public void saveOptions(File path) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        XML.managerOptionsToXml(path);
    }

    public void loadOptions() throws FileNotFoundException {
        instance = XML.xmlToManagerOptions(getManagerPath() + File.separator + OPTIONS_FILENAME);
    }

    /**
     *
     * @param p
     */
    public void setModPath(String p) {
        MODS_FOLDER = p;
        updateNotify();
    }

    /**
     *
     * @param p
     */
    public void setGamePath(String p) {
        HON_FOLDER = p;
        updateNotify();
    }

    /**
     *
     * @param p
     */
    public void setManagerPath(String p) {
        MANAGER_FOLDER = p;
        updateNotify();
    }
    
    /**
     * 
     */
    public String getOptionsName() {
    	return OPTIONS_FILENAME;
    }

    /**
     * 
     * @return
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * 
     * @return
     */
    public String getHomepage() {
        return HOMEPAGE;
    }

    /**
     *
     * @return
     */
    public String getModPath() {
        MODS_FOLDER = check("Mod folder");
        return MODS_FOLDER;
    }

    /**
     *
     * @return
     */
    public String getGamePath() {
        HON_FOLDER = check("HoN folder");
        return HON_FOLDER;
    }

    /**
     * 
     * @return
     */
    public String getManagerPath() {
        return MANAGER_FOLDER;
    }

    /**
     *
     * @param list
     */
    public void setAppliedMods(Set<Mod> list) {
        applied = list;
        updateNotify();
    }

    /**
     *
     * @return
     */
    public Set<Mod> getAppliedMods() {
        return applied;
    }

    /**
     *
     * @return
     */
    public ArrayList<Mod> getMods() {
        if (mods == null) {
            mods = new ArrayList<Mod>();
        }
        return mods;
    }

    /**
     * 
     * @param mods
     */
    public void setMods(ArrayList<Mod> mods) {
        this.mods = mods;
        updateNotify();
    }

    public String check(String name) {
        String path = "";
        if (name.equalsIgnoreCase("HoN folder")) {
            path = Game.findHonFolder();
        } else if (name.equalsIgnoreCase("Mod folder")) {
            path = Game.findModFolder();
        }
        if (path == null || path.isEmpty()) {
            return (String) JOptionPane.showInputDialog(
                    new JFrame("First Time?"),
                    "Please enter the path to " + name);
        }

        return path;
    }
}
