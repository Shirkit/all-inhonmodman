    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.io.StreamException;

import org.apache.log4j.Logger;

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

import controller.Manager;
import utility.FileUtils;
import utility.XML;
import utility.xml.converters.ManagerOptionsConverter;

/**
 * This is the Manager data holder. This class holds all data from the project.
 * @author Shirkit
 */
@XStreamAlias("options")
@XStreamConverter(ManagerOptionsConverter.class)
public class ManagerOptions extends Observable {

    // Fields that are going to be saved into XML File
    private String MODS_FOLDER;
    private String HON_FOLDER;
    private String CLARGS;
    private String LANG;
    private String LAF;
    private Set<Mod> applied;
    private Rectangle guiRectangle;
    private ArrayList<Integer> columnsWidth;
    private String columnsOrder;
    private boolean ignoreGameVersion;
    private boolean autoUpdate;
    private boolean autoEnableDependencies;
    private boolean developerMode;
    private boolean colorCheckboxInTable;
    private boolean showIconsInTable;
    private boolean useSmallIcons;
    private boolean deleteFolderTree;
    private String lastHonVersion;
    private ViewType viewType;
    private String developingMod;
    // Hidden fields from XML
    private ArrayList<Mod> mods;
    private static ManagerOptions instance;
    private static Manager controller;
    private boolean noOptionsFile;
    Logger logger;
    // Constants
    /**
     * Returns the relative path of the version file, wich contains the current version of the software.
     */
    public static final String MANAGER_VERSION_FILE = "resources/version.txt";
    /**
     * Retrieves a string with the current folder path of the Manager.
     *  String with the path. Example: 'C:\Users\User\Documents\ModManager'. The Manager.jar will be inside that folder.
     */
    public static final String MANAGER_FOLDER = new File(".").getAbsolutePath();
    /**
     * Returns the name of the file that contains the options of the software.
     */
    public static final String OPTIONS_FILENAME = "managerOptions.xml";
    public static final String MANAGER_CHECK_UPDATE_VERSIONS = "http://dl.dropbox.com/u/10303236/versions.txt";
    public static final String MANAGER_CHECK_UPDATE_ROOT_FOLDER = "http://dl.dropbox.com/u/10303236/versions/";
    public static final String HOMEPAGE = "http://sourceforge.net/projects/all-inhonmodman";
    public static final String PREFS_LOCALE = "locale";

    private ManagerOptions() {
        // Set default values
        MODS_FOLDER = "";
        HON_FOLDER = "";
        CLARGS = "";
        LANG = "";
        LAF = "default";
        developingMod = "";
        columnsOrder = "";
        applied = new HashSet<Mod>();
        mods = new ArrayList<Mod>();
        controller = Manager.getInstance();
        logger = Logger.getLogger(this.getClass().getPackage().getName());
        ignoreGameVersion = true;
        autoUpdate = true;
        autoEnableDependencies = true;
        developerMode = false;
        deleteFolderTree = false;
        colorCheckboxInTable = true;
        showIconsInTable = true;
        guiRectangle = null;
        columnsWidth = new ArrayList<Integer>();
        viewType = ViewType.DETAILS;
        noOptionsFile = true;
    }

    /**
     * The only way to access the class. Since this class should be unic, this method is necessary to control this.
     * Once you got the instace, don't have to worry, because the instance won't be disposed, but just uptaded, to avoid inconsisty.
     * @return a ManagerOptions instance.
     */
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

    /**
     * This method should only be called by the controller. This method saves the current ManagerOptions instance to the <b>path</b> passed by parameter.
     * @param path path to the file
     * @throws IOException If any error happens, this exception is thrown.
     */
    public void saveOptions(File path) throws IOException {
        XML.managerOptionsToXml(path);
    }

    /**
     * This retrieves the GUI class X, Y, Width and Height.
     */
    public Rectangle getGuiRectangle() {
        return guiRectangle;
    }

    /**
     * Stores the GUI's class X, Y, Width and Height.
     */
    public void setGuiRectangle(Rectangle guiRectangle) {
        this.guiRectangle = guiRectangle;
    }

    /**
     * Retrieves an ArrayList containing the width of the columns of the GUI class. This should be only called when program loads.
     */
    public ArrayList<Integer> getColumnsWidth() {
        return columnsWidth;
    }

    /**
     * Stores the GUI's columns width. Since the logic of the preferences has
     * been brought to this class, this should be called whenever the Column's width changes.
     */
    public void setColumnsWidth(ArrayList<Integer> columnsWidth) {
        this.columnsWidth = columnsWidth;
    }
    
    public String getColumnsOrder() {
        return columnsOrder;
    }

    public void setColumnsOrder(String colOrder) {
        this.columnsOrder = colOrder;
    }

    /**
     * Loads the options from a valid XML file. The path is indicated by the current running folder plus the <b>OPTIONS_FILENAME</b> constant. Load into the same instance, to avoid inconsisty.
     * This method should only be called by the Manager Controller class.
     * @throws FileNotFoundException this exception is thrown if the file contaning the options wasn't found.
     * @throws StreamException this exception is thrown if the file is not valid (a user changed, the file is corrupt etc).
     */
    public void loadOptions() throws FileNotFoundException, StreamException {
        if (!new File(FileUtils.getManagerPerpetualFolder() + File.separator + OPTIONS_FILENAME).exists()) {
            throw new FileNotFoundException(OPTIONS_FILENAME);
        }
        ManagerOptions temp = XML.xmlToManagerOptions(new File(FileUtils.getManagerPerpetualFolder() + File.separator + OPTIONS_FILENAME));
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
        if (temp.getColumnsOrder() != null) {
            instance.setColumnsOrder(temp.getColumnsOrder());
        }
        if (temp.getViewType() != null) {
            instance.setViewType(temp.getViewType());
        }
        if (temp.getDevelopingMod() != null) {
            instance.setDevelopingMod(temp.getDevelopingMod());
        }
        instance.setUseSmallIcons(temp.usingSmallIcons());
        instance.setColorCheckboxesInTable(temp.getCheckboxesInTableColored());
        instance.setShowIconsInTable(temp.iconsShownInTable());
        instance.setIgnoreGameVersion(temp.isIgnoreGameVersion());
        instance.setAutoUpdate(temp.isAutoUpdate());
        instance.setDeveloperMode(temp.isDeveloperMode());
        instance.setDeleteFolderTree(temp.isDeleteFolderTree());
    }

    /**
     * Set the path to the Mods folder. Example: '/~/Heroes of Newerth/game/mods'. This can be changed to any folder
     * @param p path to the folder.
     */
    public void setModPath(String p) {
        MODS_FOLDER = p;
    }

    /**
     * Set the path to HoN folder. Be sure to verify if the folder is a valid HoN folder.
     * @param p path to the folder.
     */
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

    public void setDevelopingMod(String developingMod) {
        this.developingMod = developingMod;
    }

    /**
     * A mod can't be applied if it's version specified is older than the Game's version. That means a mod got out of date. Turn this on to ignore those checks.
     * @param ignoreGameVersion true if want to ignore the game version, false otherwise.
     */
    public void setIgnoreGameVersion(boolean ignoreGameVersion) {
        this.ignoreGameVersion = ignoreGameVersion;
    }

    /**
     * When a new version of the Manager goes out, if this is true, the Manager shall update without asking the user.
     * @param autoUpdate true if want to auto-update the manager, false otherwise.
     */
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    /**
     * Mod A needs mod B to be enabled to work. If this is true, B will be enabled without notifying the user.
     * @param autoEnableDependencies true if want to enable dependencies automatically, false otherwise.
     */
    public void setAutoEnableDependencies(boolean autoEnableDependencies) {
        this.autoEnableDependencies = autoEnableDependencies;
    }

    public void setDeleteFolderTree(boolean deleteFolderTree) {
        this.deleteFolderTree = deleteFolderTree;
    }

    /**
     * Activates the option for the Developer Mode.
     * @param developerMode true if want to activate it, false otherwise.
     */
    public void setDeveloperMode(boolean developerMode) {
        this.developerMode = developerMode;
    }

    /**
     * A mod can't be applied if it's version specified is older than the Game's version. That means a mod got out of date. Turn this on to ignore those checks.
     * @return true if want to ignore the game version, false otherwise.
     */
    public boolean isIgnoreGameVersion() {
        return ignoreGameVersion;
    }

    /**
     * When a new version of the Manager goes out, if this is true, the Manager shall update without asking the user.
     * @return  autoUpdate true if want to auto-update the manager, false otherwise.
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Mod A needs mod B to be enabled to work. If this is true, B will be enabled without notifying the user.
     * @return  autoEnableDependencies true if want to enable dependencies automatically, false otherwise.
     */
    public boolean isAutoEnableDependencies() {
        return autoEnableDependencies;
    }

    public boolean isDeleteFolderTree() {
        return deleteFolderTree;
    }

    /**
     * Whether or not views use small icons.
     * @return useSmallIcons true if small icons are being used
     */
    public boolean usingSmallIcons() {
        return useSmallIcons;
    }

    /**
     * Set whether views should use small icons.
     * @param useSmallIcons true if views should use small icons, false otherwise.
     */
    public void setUseSmallIcons(boolean useSmallIcons) {
        this.useSmallIcons = useSmallIcons;
    }

    /**
     * Check if the option for Developer mode is active.
     * @return  developerMode true if it is, false otherwise
     */
    public boolean isDeveloperMode() {
        return developerMode;
    }

    /**
     * Loads from the file pointed by the constant <b>MANAGER_VERSION_FILE</b> inside the Manager.jar file. Example: ALPHA 0.7.5
     * @return a String in this format: RELEASE VERSION (BETA 1.5.3)
     */
    public String getVersion() {
        URL version = getClass().getClassLoader().getResource(MANAGER_VERSION_FILE);
        BufferedReader in;
        String pattern = "";
        try {
            in = new BufferedReader(new InputStreamReader(version.openStream()));
            pattern = in.readLine();
        } catch (IOException ex) {
        }
        return pattern;
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

    public String getDevelopingMod() {
        return developingMod;
    }

    /**
     * Get the path to the Mods folder. Example: '/~/Heroes of Newerth/game/mods'.
     * @return p path to the folder.
     */
    public String getModPath() {
        if (MODS_FOLDER != null) {
            return MODS_FOLDER;
        }
        return "";
    }

    /**
     * Get the path to HoN folder. Be sure to verify if the folder is a valid HoN folder.
     * @return  p path to the folder.
     */
    public String getGamePath() {
        if (HON_FOLDER != null) {
            return HON_FOLDER;
        }
        return "";
    }

    /**
     * Set's the list of applied mods. Don't create new instances of the mods, just retrieve them with the <b>getMods()</b> method and add to here.
     * @param list set of applied mods.
     */
    public void setAppliedMods(Set<Mod> list) {
        applied = list;
    }

    /**
     * Get the list of applied mods. The instances of mods inside here point to the same of the <b>getMods()</b> instances.
     */
    public Set<Mod> getAppliedMods() {
        return applied;
    }

    /**
     * Adds a mod to the list of mods.
     * @param mod instance of a mod.
     * @param enabled true if you want to enable the mod, false otherwise.
     */
    public void addMod(Mod mod, boolean enabled) {
        if (enabled) {
            mod.enable();
        } else {
            mod.disable();
        }
        this.mods.add(mod);
    }

    public void removeMod(Mod mod) {
        applied.remove(mod);
        mods.remove(mod);
    }

    /**
     * Retrives an ArrayList of mods that are equal to the <b>name</b> passed by parameter. This will be check with <b>String.equalIgnoreCase()</b> method.
     * @param name name of the mod.
     * @return the arraylist of mods. Check with <b>ArrayList.isEmpty()</b> method, since this method won't return null.
     */
    public ArrayList<Mod> getModsWithName(String name) {
        ArrayList<Mod> list = new ArrayList<Mod>();
        for (int i = 0; i < mods.size(); i++) {
            if (mods.get(i).getName().equalsIgnoreCase(name)) {
                list.add(mods.get(i));
            }
        }

        return list;
    }

    /**
     * Retrieves a mod with the name passed by paramether, and the mod must be enabled.
     * @param name name of the mod.
     * @return a instance of a Mod, null if no mod was found.
     * @deprecated Avoid using this method, because there is no Version check.
     * @see #getMod(java.lang.String, java.lang.String)
     */
    public Mod getEnabledModWithName(String name) {
        ArrayList<Mod> list = new ArrayList<Mod>();
        for (int i = 0; i < mods.size(); i++) {
            if (mods.get(i).getName().equalsIgnoreCase(name) && mods.get(i).isEnabled()) {
                return mods.get(i);
            }
        }

        return null;
    }

    /**
     * Retrives a mod with the name and version passed by parameter. Mod's name will be check with <b>String.equalsIgnoreCase</b> method, and the version with the Manager.compareModsVersions().
     * @param name name of the mod.
     * @param version version of the mod. This is an expressionVersion, as seen in Manager.compareModsVersions.
     * @return a instance of a Mod, null if no mod was found.
     */
    public Mod getMod(String name, String version) {
        for (int i = 0; i < mods.size(); i++) {
            if (mods.get(i).getName().equalsIgnoreCase(name) && controller.compareModsVersions(mods.get(i).getVersion(), version)) {
                return mods.get(i);
            }
        }

        return null;
    }

    /**
     * @return the viewType
     */
    public ViewType getViewType() {
        return viewType;
    }

    /**
     * @param viewType the viewType to set
     */
    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }

    public enum ViewType {

        DETAILS, ICONS, DETAILED_ICONS, TILES
    }

    /**
     * Retrieves the ArrayList contaning all loaded mods.
     */
    public ArrayList<Mod> getMods() {
        return mods;
    }

    /**
     * @deprecated Don't use this, use the <b>ManagerOptions.addMod()</b> if you want to add a mod.
     */
    public void setMods(ArrayList<Mod> mods) {
        if (mods != null) {
            this.mods = mods;
        } else {
            this.mods = new ArrayList<Mod>();
        }
    }

    /**
     * @return the colorCheckboxInTable
     */
    public boolean getCheckboxesInTableColored() {
        return colorCheckboxInTable;
    }

    /**
     * @param colorCheckboxInTable the colorCheckboxInTable to set
     */
    public void setColorCheckboxesInTable(boolean colorCheckboxInTable) {
        this.colorCheckboxInTable = colorCheckboxInTable;
    }

    /**
     * @return the showIconsInTable
     */
    public boolean iconsShownInTable() {
        return showIconsInTable;
    }

    /**
     * @param showIconsInTable the showIconsInTable to set
     */
    public void setShowIconsInTable(boolean showIconsInTable) {
        this.showIconsInTable = showIconsInTable;
    }

    public boolean getNoOptionsFile() {
        return noOptionsFile;
    }

    public void setNoOptionsFile(boolean noOptionsFile) {
        this.noOptionsFile = noOptionsFile;
    }

    public void setLastHonVersion(String lastHonVersion) {
        this.lastHonVersion = lastHonVersion;
    }

    public String getLastHonVersion() {
        return lastHonVersion;
    }
}
