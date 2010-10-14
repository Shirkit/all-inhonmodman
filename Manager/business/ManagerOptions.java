    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.io.StreamException;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import org.apache.log4j.Logger;

import manager.Manager;

import utility.XML;

/**
 *
 * @author Shirkit
 */
@XStreamAlias("options")
@XStreamConverter(ManagerOptionsConverter.class)
public class ManagerOptions extends Observable {

    // Saved fields
    private String MODS_FOLDER;
    private String HON_FOLDER;
    private String CLARGS;
    private String LANG;
    private String LAF;
    private Set<Mod> applied;
    private Rectangle guiRectangle;
    private ArrayList<Integer> columnsWidth;
    private boolean ignoreGameVersion;
    private boolean autoUpdate;
    private boolean autoEnableDependencies;
    private boolean developerMode;
    // Hidden fields
    private ArrayList<Mod> mods;
    public static final String MANAGER_VERSION = "version.txt";
    public static final String MANAGER_FOLDER = new File(".").getAbsolutePath();
    public static final String OPTIONS_FILENAME = "managerOptions.xml";
    public static final String MANAGER_CHECK_UPDATE = "http://dl.dropbox.com/u/10303236/version.txt";
    public static final String MANAGER_DOWNLOAD_URL = "http://dl.dropbox.com/u/10303236/Manager.jar";
    public static final String MANAGER_UPDATE_URL = "http://dl.dropbox.com/u/10303236/Updater.jar";
    public static final String HOMEPAGE = "http://sourceforge.net/projects/all-inhonmodman";
    public static final String PREFS_LOCALE = "locale";
    public static final String PREFS_LAF = "laf";
    public static final String PREFS_CLARGUMENTS = "clarguments";
    public static final String PREFS_HONFOLDER = "honfolder";
    private static ManagerOptions instance;
    private static Manager controller;
    Logger logger;

    private ManagerOptions() {
        MODS_FOLDER = "";
        HON_FOLDER = "";
        CLARGS = "";
        LANG = "";
        LAF = "default";
        applied = new HashSet<Mod>();
        mods = new ArrayList<Mod>();
        controller = Manager.getInstance();
        logger = Logger.getLogger(this.getClass().getPackage().getName());
        ignoreGameVersion = false;
        autoUpdate = false;
        autoEnableDependencies = false;
        developerMode = false;
        guiRectangle = null;
        columnsWidth = new ArrayList<Integer>();
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
        setChanged();
        notifyObservers();
    }

    public void saveOptions(File path) throws IOException {
        XML.managerOptionsToXml(path);
    }

    public Rectangle getGuiRectangle() {
        return guiRectangle;
    }

    public void setGuiRectangle(Rectangle guiRectangle) {
        this.guiRectangle = guiRectangle;
    }

    public ArrayList<Integer> getColumnsWidth() {
        return columnsWidth;
    }

    public void setColumnsWidth(ArrayList<Integer> columnsWidth) {
        this.columnsWidth = columnsWidth;
    }

    public void loadOptions() throws FileNotFoundException, StreamException {
        ManagerOptions temp = XML.xmlToManagerOptions(new File(getManagerPath() + File.separator + OPTIONS_FILENAME));
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
        if (temp.getGuiRectangle() != null) {
            instance.setGuiRectangle(temp.getGuiRectangle());
        }
        if (temp.getColumnsWidth() != null) {
            instance.setColumnsWidth(temp.getColumnsWidth());
        }
        instance.setIgnoreGameVersion(temp.isIgnoreGameVersion());
        instance.setAutoUpdate(temp.isAutoUpdate());
        instance.setDeveloperMode(temp.isDeveloperMode());
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

    public void setIgnoreGameVersion(boolean ignoreGameVersion) {
        this.ignoreGameVersion = ignoreGameVersion;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public void setAutoEnableDependencies(boolean autoEnableDependencies) {
        this.autoEnableDependencies = autoEnableDependencies;
    }

    public void setDeveloperMode(boolean developerMode) {
        this.developerMode = developerMode;
    }

    public boolean isIgnoreGameVersion() {
        return ignoreGameVersion;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public boolean isAutoEnableDependencies() {
        return autoEnableDependencies;
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    public String getVersion() {
        URL version = getClass().getClassLoader().getResource(MANAGER_VERSION);
        BufferedReader in;
        String pattern = "";
        try {
            in = new BufferedReader(new InputStreamReader(version.openStream()));
            pattern = in.readLine();
        } catch (IOException ex) {
        }
        //BufferedReader in = new BufferedReader(new FileReader(MANAGER_VERSION));
        return pattern;
    }

    public String getOptionsName() {
        return OPTIONS_FILENAME;
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

    public String getUpdateCheckUrl() {
        return MANAGER_CHECK_UPDATE;
    }

    public void addMod(Mod mod) {
        if (this.mods == null) {
            this.mods = new ArrayList<Mod>();
        }

        this.mods.add(mod);
    }

    public void addMod(Mod mod, boolean enabled) {
        if (this.mods == null) {
            this.mods = new ArrayList<Mod>();
        }

        this.mods.add(mod);
        if (enabled) {
            this.mods.get(this.mods.indexOf(mod)).enable();
        } else {
            this.mods.get(this.mods.indexOf(mod)).disable();
        }
    }

    public ArrayList<Mod> getModsWithName(String name) {
        ArrayList<Mod> list = new ArrayList<Mod>();
        for (int i = 0; i < mods.size(); i++) {
            if (mods.get(i).getName().equalsIgnoreCase(name)) {
                list.add(mods.get(i));
            }
        }

        return list;
    }

    public Mod getEnabledModWithName(String name) {
        ArrayList<Mod> list = new ArrayList<Mod>();
        for (int i = 0; i < mods.size(); i++) {
            if (mods.get(i).getName().equalsIgnoreCase(name) && mods.get(i).isEnabled()) {
                return mods.get(i);
            }
        }

        return null;
    }

    public Mod getMod(String name, String version) {
        for (int i = 0; i < mods.size(); i++) {
            if (mods.get(i).getName().equalsIgnoreCase(name) && mods.get(i).getVersion().equalsIgnoreCase(version)) {
                return mods.get(i);
            }
        }

        return null;
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
