/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class ManagerOptions extends Observable  {

    @XStreamAlias("manager_folder")
    @XStreamAsAttribute
    private String MANAGER_FOLDER;
    @XStreamAlias("mods_folder")
    @XStreamAsAttribute
    private String MODS_FOLDER;
    @XStreamAlias("hon_folder")
    @XStreamAsAttribute
    private String HON_FOLDER;
    @XStreamAsAttribute
    @XStreamAlias("applied")
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

    public boolean saveOptions(File path) throws IOException {
        boolean success = true;

        XStream xstream = new XStream(XML.getDriver());
        XML.updateAlias(xstream);

        if (path.exists()) {
            if (!path.delete()) {
                success = false;
            }
        }

        FileOutputStream fos = new FileOutputStream(path);
        fos.write((XML.replaceInvalidHtmlChars(xstream.toXML(this))).getBytes("UTF-8"));

        return success;
    }

    public void loadOptions() throws FileNotFoundException {
            
        XStream xstream = new XStream(XML.getDriver());
        xstream = XML.updateAlias(xstream);

        ManagerOptions tmp = (ManagerOptions) xstream.fromXML(new FileInputStream(MANAGER_FOLDER + File.separator + OPTIONS_FILENAME));
        this.setGamePath(tmp.getGamePath());
        this.setManagerPath(tmp.getManagerPath());
        this.setModPath(tmp.getModPath());
        this.setAppliedMods(tmp.getAppliedMods());
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
    	MANAGER_FOLDER = check("Manager folder");
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
    	if (mods == null)
    		mods = new ArrayList<Mod>();
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
    	String path;
    	if (name.equalsIgnoreCase("HoN folder")) {
    		return Game.findHonFolder();
    	}
    	else if (name.equalsIgnoreCase("Mod folder")) {
    		return Game.findModFolder();
    	}
    	else if (name.equalsIgnoreCase("Manager folder")) {
    		return Game.findManagerFolder();
    	}
    	else {
    		return (String)JOptionPane.showInputDialog(
    				new JFrame("First Time?"),
    				"Please enter the path to " + name
					);
    	}
    }
}
