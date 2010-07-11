    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.io.StreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import manager.Manager;

import utility.Game;
import utility.XML;

/**
 *
 * @author Shirkit
 */
@XStreamAlias("options")
@XStreamConverter(ManagerOptionsConverter.class)
public class ManagerOptions extends Observable {

    @XStreamAlias("modsfolder")
    @XStreamAsAttribute
    private String MODS_FOLDER;
    @XStreamAlias("honfolder")
    @XStreamAsAttribute
    private String HON_FOLDER;
    @XStreamAlias("clargs")
    @XStreamAsAttribute
    private String CLARGS;
    @XStreamAlias("lang")
    @XStreamAsAttribute
    private String LANG;
    @XStreamAlias("laf")
    @XStreamAsAttribute
    private String LAF;
    @XStreamImplicit
    private Set<Mod> applied;
    
    // Hidden fields
    @XStreamOmitField
    private ArrayList<Mod> mods;
    @XStreamOmitField
    public static final String MANAGER_FOLDER = new File(".").getAbsolutePath();
    @XStreamOmitField
    public static final String OPTIONS_FILENAME = "managerOptions.xml";
    @XStreamOmitField
    public static final String HOMEPAGE = "http://sourceforge.net/projects/all-inhonmodman";
    @XStreamOmitField
    public static final String VERSION = "0.1 BETA";
    @XStreamOmitField
    public static final String PREFS_LOCALE = "locale";
    @XStreamOmitField
    public static final String PREFS_LAF = "laf";
    @XStreamOmitField
    public static final String PREFS_CLARGUMENTS = "clarguments";
    @XStreamOmitField
    public static final String PREFS_HONFOLDER = "honfolder";
    @XStreamOmitField
    private static ManagerOptions instance;
    @XStreamOmitField
    private static Manager controller;
    @XStreamOmitField
    Logger logger;

    private ManagerOptions() {
        MODS_FOLDER = "";
        HON_FOLDER = "";
        CLARGS = "";
        LANG = "";
        LAF = "";
        applied = new HashSet<Mod>();
        mods = new ArrayList<Mod>();
        controller = Manager.getInstance();
        logger = Logger.getLogger(this.getClass().getPackage().getName());
    }

    public static ManagerOptions getInstance() {
        if (instance == null) {
            instance = new ManagerOptions();
        }
        return instance;
    }
    
    public static void setInstance(Object o) {
    	instance = (ManagerOptions) o;
    }

    /**
     * Set status of the MVC model to changed and notify all registered observers.
     * Call this method when the data model changed and UI has to be updated.
     */
    public void updateNotify() {
        setChanged();
        notifyObservers();
    }

    public void saveOptions(File path) throws UnsupportedEncodingException, IOException {
        XML.managerOptionsToXml(path);
    }

    public void loadOptions() throws FileNotFoundException, StreamException {
    	ManagerOptions temp = XML.xmlToManagerOptions(new File(getManagerPath() + File.separator + OPTIONS_FILENAME));
        instance = new ManagerOptions();
        if (temp.getAppliedMods() != null) {
            instance.setAppliedMods(temp.getAppliedMods());
        }
        if (temp.getCLArgs() != null) {
            instance.setCLArgs(temp.getCLArgs());
        }
        if (temp.getGamePath() != null) {
            instance.setGamePath(temp.getGamePath());
        }
        if (temp.getLaf() != null) {
            instance.setLaf(temp.getLaf());
        }
        if (temp.getLanguage() != null) {
            instance.setLanguage(temp.getLanguage());
        }
        if (temp.getModPath() != null) {
            instance.setModPath(temp.getModPath());
        }
    }

    public void setModPath(String p) {
        MODS_FOLDER = p;
    }

    public void setGamePath(String p) {
        HON_FOLDER = p;
    }
    
    public void setLanguage(String p) {
    	LANG = p;
    }
    
    public void setLaf(String p) {
    	LAF = p;
    }
    
    public void setCLArgs(String p) {
    	CLARGS = p;
    }
    
    public String getOptionsName() {
    	return OPTIONS_FILENAME;
    }

    public String getVersion() {
        return VERSION;
    }

    public String getHomepage() {
        return HOMEPAGE;
    }
    
    public String getLanguage() {
    	return LANG;
    }
    
    public String getLaf() {
    	return LAF;
    }
    
    public String getCLArgs() {
    	return CLARGS;
    }

    public String getModPath() {
        return MODS_FOLDER;
    }

    public String getGamePath() {
        return HON_FOLDER;
    }

    public String getManagerPath() {
        return MANAGER_FOLDER;
    }

    public void setAppliedMods(Set<Mod> list) {
        applied = list;
        updateNotify();
    }

    public Set<Mod> getAppliedMods() {
        return applied;
    }
    
    public void addMod(Mod mod) {
    	if (this.mods == null) {
    		this.mods = new ArrayList<Mod>();
    	}
    	
    	this.mods.add(mod);
    }

    public ArrayList<Mod> getMods() {
        if (mods == null) {
            mods = new ArrayList<Mod>();
        }
        return mods;
    }

    public void setMods(ArrayList<Mod> mods) {
        this.mods = mods;
        updateNotify();
    }


}
