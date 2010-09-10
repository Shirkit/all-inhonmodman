package manager;

import business.ManagerOptions;
import business.Mod;
import business.actions.*;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import utility.OS;
import utility.XML;
import utility.ZIP;
import utility.exception.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Random;

import com.mallardsoft.tuple.*;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.StreamException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.FileLockInterruptionException;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipException;

import org.apache.log4j.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import utility.FileUtils;

import utility.Game;
import utility.update.UpdateReturn;
import utility.update.UpdateThread;

/**
 * Implementation of the core functionality of HoN modification manager. This class is
 * the 'model' part of the MVC framework used for creating manager GUI. After any updates
 * that should result in UI changes (such as new mod added) it should call updateNotify
 * method which will notify observers to refresh. This class should never directly call
 * view or controller classes.
 *
 * @author Shirkit
 */
public class Manager extends Observable {

    private static Manager instance = null;
    private HashMap<Mod, HashMap<String, String>> deps;
    private HashSet<HashMap<String, String>> cons;
    private HashMap<Mod, HashMap<String, String>> after;
    private HashMap<Mod, HashMap<String, String>> before;
    private static Logger logger = Logger.getLogger(Manager.class.getPackage().getName());

    /**
     * It's private since only one isntance of the controller is allowed to exist.
     */
    private Manager() {
    	// Deps, After, and Before are all Map of Mod and ArrayList of Tuple, this way the key can query the mods requested and the value is the list
    	//
    	// Cons is an Arraylist of sets, this way each set has only 2 mods (should be), 
    	// then by checking for all items in the list to find the sets contains the mod, one can pinpoint the incompatible mods too
        deps = new HashMap<Mod, HashMap<String, String>>(); 
        cons = new HashSet<HashMap<String, String>>();
        after = new HashMap<Mod, HashMap<String, String>>();
        before = new HashMap<Mod, HashMap<String, String>>();
    }

    /**
     * This method is used to get the running instance of the Manager class.
     * @return the instance.
     * @see get()
     */
    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    /**
     * This should be called after adding all the honmod files to build and initialize the arrays
     * @throws IOException
     */
    public void buildGraphs() throws IOException {
        // First reset all new added mods from startup
        for (int i = 0; i < ManagerOptions.getInstance().getMods().size(); i++) {
        	ManagerOptions.getInstance().getMods().get(i).disable();
        }
        
        ArrayList<Mod> mods = ManagerOptions.getInstance().getMods();

        // Now building the graph
        for (int i = 0; i < mods.size(); i++) {
            for (int j = 0; j < mods.get(i).getActions().size(); j++) {
            	// ApplyAfter
                if (mods.get(i).getActions().get(j).getClass() == ActionApplyAfter.class) {
                    //Pair<String, String> afteri = Tuple.from(((ActionApplyAfter) mods.get(i).getActions().get(j)).getName(),
                    //        ((ActionApplyAfter) mods.get(i).getActions().get(j)).getVersion());

                    if (!after.containsKey(mods.get(i)))
                    	after.put(mods.get(i), new HashMap<String, String>());
                    after.get(mods.get(i)).put(((ActionApplyAfter) mods.get(i).getActions().get(j)).getName(), ((ActionApplyAfter) mods.get(i).getActions().get(j)).getVersion());
                // ApplyBefore
                } else if (mods.get(i).getActions().get(j).getClass() == ActionApplyBefore.class) {
                    //Pair<String, String> beforei = Tuple.from(((ActionApplyBefore) mods.get(i).getActions().get(j)).getName(),
                    //        ((ActionApplyBefore) mods.get(i).getActions().get(j)).getVersion());
                    
                	if (!before.containsKey(mods.get(i)))
                    	before.put(mods.get(i), new HashMap<String, String>());
                    before.get(mods.get(i)).put(((ActionApplyBefore) mods.get(i).getActions().get(j)).getName(), ((ActionApplyBefore) mods.get(i).getActions().get(j)).getVersion());                
                // ApplyIncompatibility
                } else if (mods.get(i).getActions().get(j).getClass() == ActionIncompatibility.class) {
                    //Pair<String, String> coni = Tuple.from(((ActionIncompatibility) mods.get(i).getActions().get(j)).getName(),
                    //        ((ActionIncompatibility) mods.get(i).getActions().get(j)).getVersion());
                    //Pair<String, String> myi = Tuple.from(mods.get(i).getName(), mods.get(i).getVersion()); 
                    //Set<Pair<String, String>> conSet = new HashSet<Pair<String, String>>();
                    
                	HashMap<String, String> mapping = new HashMap<String, String>();
                	mapping.put(mods.get(i).getName(), mods.get(i).getVersion());
                	mapping.put(((ActionIncompatibility) mods.get(i).getActions().get(j)).getName(), ((ActionIncompatibility) mods.get(i).getActions().get(j)).getVersion());
                    cons.add(mapping);
                 // ApplyRequirement
                } else if (mods.get(i).getActions().get(j).getClass() == ActionRequirement.class) {
                    //Pair<String, String> depi = Tuple.from(((ActionRequirement) mods.get(i).getActions().get(j)).getName(),
                    //        ((ActionRequirement) mods.get(i).getActions().get(j)).getVersion());

                	if (!deps.containsKey(mods.get(i)))
                    	deps.put(mods.get(i), new HashMap<String, String>());
                    deps.get(mods.get(i)).put(((ActionRequirement) mods.get(i).getActions().get(j)).getName(), ((ActionRequirement) mods.get(i).getActions().get(j)).getVersion());
                }
            }
        }
    }

    /**
     * This method saves the ManagerOptions attributes in a file. The file is located in the same folder of the Manager.
     * The filename can be get in the ManagerOptions.
     * @throws IOException if a random I/O exception happened.
     * 
     */
    public void saveOptions() throws IOException {
        String name = ManagerOptions.getInstance().getManagerPath() + File.separator + ManagerOptions.getInstance().getOptionsName();
        File f = new File(name);
        if (f.exists()) {
            f.delete();
        }
        ManagerOptions.getInstance().saveOptions(new File(name));
    }

    /**
     * check update the path of the Hon or Mod folder according to the string passed in
     * and prompt the user for input if the designate functions have failed.
     */
    public String check(String name) {
        String path = "";
        if (name.equalsIgnoreCase("HoN folder")) {
            path = Game.findHonFolder();
        } else if (name.equalsIgnoreCase("Mod folder")) {
            path = Game.findModFolder();
        }

        // TODO: This needs to be changed

        /*if (path == null || path.isEmpty()) {
        JFileChooser fc = new JFileChooser(new File("."));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.showOpenDialog(null);
        if (fc.getSelectedFile() != null) {
        path = fc.getSelectedFile().getAbsolutePath();
        } else {
        path = null;
        }
        }*/

        return path;
    }

    /**
     * This method runs the ManagerOptions.loadOptions method to load the options located in a file.
     */
    public void loadOptions() throws StreamException, FileNotFoundException {
        try {
            ManagerOptions.getInstance().loadOptions();
            logger.info("Options loaded.");
        } catch (FileNotFoundException e) {
            // Put a logger here
            logger.error("Failed loading options file.", e);
            ManagerOptions.getInstance().setGamePath(Game.findHonFolder());
            logger.info("HoN folder set to=" + ManagerOptions.getInstance().getGamePath());
            ManagerOptions.getInstance().setModPath(Game.findModFolder());
            logger.info("Mods folder set to=" + ManagerOptions.getInstance().getModPath());
            throw e;
        }

        logger.error("MAN: finished loading options: " + ManagerOptions.getInstance().getAppliedMods());
    }

    /**
     * Adds a Mod to the list of mods. This adds the mod to the Model list of mods.
     * @param Mod to be added.
     */
    private void addMod(Mod mod) {
        // Disable the mod to make sure it is consistent
        ManagerOptions.getInstance().addMod(mod, false);
    }

    /**
     * Load all mods from the mods folder (set in Model) and put them into the Model array of mods.
     * @throws Exception 
     */
    public ArrayList<ArrayList<Pair<String, String>>> loadMods() throws IOException {
        ManagerOptions.getInstance().getMods().clear();
        File modsFolder;
        try {
            modsFolder = new File(ManagerOptions.getInstance().getModPath());
        } catch (NullPointerException ex) {
            return new ArrayList<ArrayList<Pair<String, String>>>();
        }
        // Get mod files from the directory
        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File file) {
                String fileName = file.getName();
                if ((!file.isDirectory())
                        && /* Filter out directories */ (!fileName.startsWith("."))
                        && /* Filter out hidden files and current dir */ (fileName.endsWith(".honmod"))) /* Filter only .honmod files */ {
                    return true;
                } else {
                    return false;
                }
            }
        };
        File[] files = modsFolder.listFiles(fileFilter);

        // Exit if no file is found
        if (files == null || files.length == 0) {
            return new ArrayList<ArrayList<Pair<String, String>>>();
        }
        // Go through all the mods and load them
        ArrayList<Pair<String, String>> stream = new ArrayList<Pair<String, String>>();
        ArrayList<Pair<String, String>> notfound = new ArrayList<Pair<String, String>>();
        ArrayList<Pair<String, String>> zip = new ArrayList<Pair<String, String>>();
        ArrayList<ArrayList<Pair<String, String>>> problems = new ArrayList<ArrayList<Pair<String, String>>>();
        for (int i = 0; i < files.length; i++) {
            try {
                logger.error("Adding file - " + files[i].getName() + " from loadMods().");
                //ManagerCtrl.getGUI().showMessage(L10n.getString("error.loadmodfile").replace("#mod#", files[i].getName()), "TESTING", JOptionPane.ERROR_MESSAGE);
                addHonmod(files[i], false);
            } catch (ModStreamException e) {
                logger.error("StreamException from loadMods(): file - " + files[i].getName() + " - is corrupted.", e);
                stream.addAll(e.getMods());
                //ManagerCtrl.getGUI().showMessage(L10n.getString("error.loadmodfile").replace("#mod#", files[i].getName()), "error.loadmodfile.title", JOptionPane.ERROR_MESSAGE);
            } catch (ModNotFoundException e) {
                logger.error("FileNotFoundException from loadMods(): file - " + files[i].getName() + " - is corrupted.", e);
                notfound.addAll(e.getMods());
                //ManagerCtrl.getGUI().showMessage(L10n.getString("error.loadmodfile").replace("#mod#", files[i].getName()), "error.loadmodfile.title", JOptionPane.ERROR_MESSAGE);
            } catch (ConversionException e) {
                logger.error("Conversion from loadMods(): file - " + files[i].getName() + " - is corrupted.", e);
            } catch (ModZipException e) {
                logger.error("ZipException from loadsMods(): file - " + files[i].getName() + " - is corrupted.", e);
                zip.addAll(e.getMods());
            }
        }
        problems.add(stream);
        problems.add(notfound);
        problems.add(zip);

        return problems;
    }

    /**
     * This function is used internally from the GUI itself automatically when launch to initiate existing mods.
     * @param honmod is the file (.honmod) to be add.
     * @param copy flag to indicate whether to copy the file to mods folder
     * @throws FileNotFoundException if the file wasn't found.
     * @throws IOException if a random I/O exception has happened.
     */
    public void addHonmod(File honmod, boolean copy) throws ModNotFoundException, ModStreamException, IOException, ModZipException {
        ArrayList<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        if (!honmod.exists()) {
            list.add(Tuple.from(honmod.getName(), "notfound"));
            throw new ModNotFoundException(list);
        }
        String xml = null;
        try {
            xml = new String(ZIP.getFile(honmod, Mod.MOD_FILENAME));
        } catch (ZipException ex) {
            list.add(Tuple.from(honmod.getName(), "zip"));
            throw new ModZipException(list);
        }
        Mod m = null;
        try {
            m = XML.xmlToMod(xml);
        } catch (StreamException ex) {
            list.add(Tuple.from(honmod.getName(), "stream"));
            throw new ModStreamException(list);
        }
        Icon icon;
        try {
            icon = new ImageIcon(ZIP.getFile(honmod, Mod.ICON_FILENAME));
        } catch (FileNotFoundException e) {
            icon = new javax.swing.ImageIcon(getClass().getResource("/gui/resources/icon.png"));
        }
        String changelog = null;
        try {
            changelog = new String(ZIP.getFile(honmod, "changelog.txt"));
        } catch (IOException e) {
            changelog = null;
        }
        m.setChangelog(changelog);
        m.setIcon(icon);
        logger.info("Mod file opened. Mod name: " + m.getName());
        m.setPath(honmod.getAbsolutePath());
        m.setId(0);
        if (copy) {
            // Copy the honmod file to mods directory
            copyFile(honmod, new File(ManagerOptions.getInstance().getModPath() + File.separator + honmod.getName()), false);
            logger.info("Mod file copied to mods older");
        }
        addMod(m);
    }

    /**
     * Copies a file (source) into another file (target), appending the file or not.
     * @param source the file to be copied.
     * @param target the file where the source is going to be written.
     * @param append if wants to append the file. If true, the file source will be copied to the end of the target, if target exists.
     * If false, the file target will be ignore (if exists or not, only the source content will exist).
     * @throws FileNotFoundException if one the files doesn't exist. If the directory tree doesn't exist, the file target can't be written.
     * @throws IOException if a random I/O exception happened.
     */
    private static void copyFile(File source, File target, boolean append) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(target, append);
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }
    }

    /**
     * Open website of the selected mod
     *
     * @param modIndex index of the mod in the list of mods
     * @return 0 on success, -1 if opening websites is not supported on this platform
     * @throws IndexOutOfBoundsException in case index is not in the mod list
     */
    public int openModWebsite(int modIndex) throws IndexOutOfBoundsException {
        // TODO: This isn't working
        Mod mod = ManagerOptions.getInstance().getMods().get(modIndex);
        String url = mod.getWebLink();

        return openWebsite(url);
    }

    /**
     * Open specified website in the default browser. This method is using java
     * Desktop API and therefore requires Java 1.6. Also, this operation might not
     * be supported on all platforms.
     *
     * @param url url of the website to open
     * @return 0 on success, -1 in case the operation is not supported on this platform
     */
    public int openWebsite(String url) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            logger.info("Opening websites is not supported");
            return -1;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            logger.info("Opening websites is not supported");
            return -1;
        }

        try {
            System.out.println(url);
            java.net.URI uri = new java.net.URI(url);
            desktop.browse(uri);
        } catch (Exception e) {
            logger.error("Unable to open website: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Open folder containing mods. The folder is opened using OS specific explorer
     * and therefore might not be supported on all platforms. This operation uses java
     * Desktop API and requires Java 1.6
     *
     * @return 0 on succuess, -1 in case the operation is not supported on this platform
     */
    public int openModFolder() {
        if (!java.awt.Desktop.isDesktopSupported()) {
            logger.info("Opening local folders is not supported");
            return -1;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        try {
            desktop.open(new File(ManagerOptions.getInstance().getModPath()));
        } catch (Exception e) {
            logger.error("Unable to open local folder: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Get mod at specified index
     *
     * @param index index of the mod in the list of mods
     * @return mod at the given index
     * @throws IndexOutOfBoundsException in case index does not exist in the list of mods
     */
    public Mod getMod(int index) throws IndexOutOfBoundsException {
        return (Mod) ManagerOptions.getInstance().getMods().get(index);
    }

    /**
     * This function returns the mod from the arraylist mods given it's name.
     * @param name of the mod.
     * @return the found Mod.
     * @throws NoSuchElementException if the mod wasn't found
     */
    public Mod getMod(String name) {
        // get the enumration object for ArrayList mods
        Enumeration e = Collections.enumeration(ManagerOptions.getInstance().getMods());

        // enumerate through the ArrayList to find the mod
        while (e.hasMoreElements()) {
            Mod m = (Mod) e.nextElement();
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }

        return null;
    }

    /**
     * This function returns the mod from the arraylist mods, but only if it's enabled.
     * @param name of the mod.
     * @return the found Mod.
     * @throws NoSuchElementException if the mod wasn't found.
     */
    public Mod getEnabledMod(String name) throws NoSuchElementException {
        Mod m = getMod(name);
        if (m == null) {
            throw new NoSuchElementException();
        }
        if (m.isEnabled()) {
            return m;
        }
        throw new NoSuchElementException();
    }

    /**
     * This function returns the mod from the arraylist mods, but only if it's disabled.
     * @param name of the mod.
     * @return the found Mod.
     * @throws NoSuchElementException if the mod wasn't found.
     */
    public Mod getDisabledMod(String name) throws NoSuchElementException {
        Mod m = getMod(name);
        if (!m.isEnabled()) {
            return m;
        }

        // Unreacheable
        throw new NoSuchElementException();
    }

    public void updateManager() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(new Mod().getUpdateCheckUrl().trim()).openStream()));
        } catch (MalformedURLException ex) {
        } catch (IOException ex) {
        }
    }

    /**
     * This method updates the given mods. It handles all exceptions that can exist, and take the needed actions to complete the task,
     * without needing any external influence.
     * @param mods to be updated.
     * @return a instance of a UpdateReturn containing the result of the method. Updated, failed and already up-to-date mods can be easily found there.
     * @throws StreamException This exception is thrown in a serius error.
     * @throws ModVersionUnsatisfiedException 
     * @throws ModNotEnabledException 
     */
    public UpdateReturn updateMod(ArrayList<Mod> mods) throws StreamException, ModNotEnabledException, ModVersionUnsatisfiedException {
        // Prepare the pool
        ExecutorService pool = Executors.newCachedThreadPool();
        Iterator<Mod> it = mods.iterator();
        HashSet<Future<UpdateThread>> temp = new HashSet<Future<UpdateThread>>();
        // Submit to the pool
        while (it.hasNext()) {
            Mod tempMod = it.next();
            temp.add(pool.submit(new UpdateThread(tempMod)));
            logger.info("Started update on: " + tempMod.getName());
        }
        // Transfer from the pool
        HashSet<Future<UpdateThread>> result = new HashSet<Future<UpdateThread>>();
        while (temp.size() != result.size()) {
            Iterator<Future<UpdateThread>> ite = temp.iterator();
            while (ite.hasNext()) {
                Future<UpdateThread> ff = ite.next();
                if (!result.contains(ff) && ff.isDone()) {
                    result.add(ff);
                    logger.info("Finished " + result.size() + " out of " + temp.size() + " URL check");
                    setChanged();
                    int[] ints = new int[2];
                    ints[0] = result.size();
                    ints[1] = temp.size();
                    notifyObservers(ints);
                }
            }
        }
        // Prepare for results
        Iterator<Future<UpdateThread>> ite = result.iterator();
        UpdateReturn returnValue = new UpdateReturn();
        while (ite.hasNext()) {
            Future<UpdateThread> ff = ite.next();
            try {
                UpdateThread mod = (UpdateThread) ff.get();
                File file = mod.getFile();
                if (file != null) {
                    FileInputStream fis = new FileInputStream(file);
                    new File(mod.getMod().getPath()).setWritable(true);
                    FileOutputStream fos = new FileOutputStream(mod.getMod().getPath());
                    ZIP.copyInputStream(fis, fos);
                    fis.close();
                    fos.flush();
                    fos.close();
                    Mod newMod = null;
                    String olderVersion = getMod(mod.getMod().getName()).getVersion();
                    try {
                        newMod = XML.xmlToMod(new String(ZIP.getFile(file, Mod.MOD_FILENAME)));
                    } catch (StreamException e) {
                        // Couldn't load new mod, XStream was unabled to parse the XML file.
                        logger.error("Updated mod failed to load. Some problem happened while reading the mod. Critical error.");
                        throw e;
                        // Can't continue, since the mod could be required by another. This should never happens, unless a developer really messes up.
                        // TODO : This can be improved, but I don't know how.
                    } catch (ZipException e) {
                        System.out.println(file.getAbsolutePath());
                    }
                    newMod.setPath(mod.getMod().getPath());
                    Mod oldMod = getMod(mod.getMod().getName());
                    boolean wasEnabled = oldMod.isEnabled();
                    HashSet<Mod> gotDisable = new HashSet<Mod>();
                    gotDisable.add(oldMod);
                    while (!gotDisable.isEmpty()) {
                        Iterator<Mod> iter = gotDisable.iterator();
                        while (iter.hasNext()) {
                            try {
                                Mod next = iter.next();
                                disableMod(next);
                                // If he got under this, so disabling was successfull.
                                gotDisable.remove(next);
                            } catch (ModEnabledException ex) {
                                // Couldn't disable, we need who didn't let disable him
                                // TODO: Changed the behavior of the exception, need to be fixed
                                Iterator<Pair<String, String>> itera = ex.getDeps().iterator();
                                while (itera.hasNext()) {
                                    Pair<String, String> pair = itera.next();
                                    if (!gotDisable.contains(getMod(Tuple.get1(pair)))) {
                                        gotDisable.add(getMod(Tuple.get1(pair)));
                                    }

                                }
                            }
                        }
                    }
                    oldMod.copy(newMod);
                    if (wasEnabled) {
                        try {
                            enableMod(newMod, false);
                        } catch (Exception ex) {
                            // Couldn't enable mod, just log it
                            logger.error("Could not enable mod " + newMod.getName());
                        }
                    }
                    returnValue.addUpdated(mod.getMod(), olderVersion);
                    logger.info(mod.getMod().getName() + "was updated to " + newMod.getVersion() + " from " + olderVersion);
                } else {
                    logger.info(mod.getMod().getName() + " is up-to-date");
                    returnValue.addUpToDate(mod.getMod());
                }
            } catch (SecurityException ex) {
                logger.info("Couldn't write on the file.");
            } catch (InterruptedException ex) {
                // Nothing can get here
            } catch (ExecutionException ex) {
                UpdateModException ex2 = (UpdateModException) ex.getCause();
                logger.info("Failed to update: " + ex2.getMod().getName() + " - " + ex2.getCause().getMessage());
                returnValue.addModFailed(ex2.getMod(), (Exception) ex2.getCause());
            } catch (FileNotFoundException ex) {
                // Can't get here
            } catch (IOException ex) {
                logger.error("Random I/O Exception happened", ex);

                // Random IO Exception
            }
        }
        return returnValue;
    }

    /**
     * This function checks to see if all dependencies of a given mod are satisfied. If a dependency isn't satisfied, throws exceptions.
     * @param mod to be checked which is guaranteed to be disabled.
     * @throws ModNotEnabledException if the mod given by parameter requires another mod to be enabled.
     */
    private void checkdeps(Mod mod) throws ModNotEnabledException, ModEnabledException, ModVersionUnsatisfiedException {
    	// From disableMod
    	if (mod.isEnabled()) {
    		Iterator it = deps.entrySet().iterator();
    		HashSet<Pair<String, String>> modEnabledEx = new HashSet<Pair<String, String>>();
    		Pair<String, String> match = Tuple.from(mod.getName(), mod.getVersion());
    		
    		while (it.hasNext()) {
    			// Make sure all mods depending on mod is disabled first
    			Map.Entry entry = (Map.Entry)it.next();
    			Mod m = (Mod)entry.getKey();
    			if (m.isEnabled() && ((HashMap<String, String>)entry.getValue()).containsKey(mod.getName()) && compareModsVersions(mod.getVersion(), ((HashMap<String, String>)entry.getValue()).get(mod.getName()))) {
    				modEnabledEx.add(Tuple.from(m.getName(), m.getVersion()));
    			}
    		}
            
            if (!modEnabledEx.isEmpty())
            	throw new ModNotEnabledException(modEnabledEx);
    	// From enableMod
    	} else if (deps.containsKey(mod)) {
    		Iterator it = deps.get(mod).entrySet().iterator();
    		HashSet<Pair<String, String>> modDisabledEx = new HashSet<Pair<String, String>>();
            HashSet<Pair<String, String>> modUnsatisfiedEx = new HashSet<Pair<String, String>>();
            
    		while (it.hasNext()) {
    			Map.Entry entry = (Map.Entry)it.next();
            	
            	// Make sure all dep exist and enabled
            	ArrayList<Mod> allModWithName = ManagerOptions.getInstance().getModsWithName((String)entry.getKey());
            	if (allModWithName == null || allModWithName.isEmpty()) {
            		modDisabledEx.add(Tuple.from((String)entry.getKey(), (String)entry.getValue()));
            	} else {
                	// Make sure all exist and enabled mods are satisfied
            		Enumeration e = Collections.enumeration(allModWithName);
            		while (e.hasMoreElements()) {
            			Mod m = (Mod)e.nextElement();
            			if (!m.isEnabled() && compareModsVersions(m.getVersion(), (String)entry.getValue()))
            				modDisabledEx.add(Tuple.from(m.getName(), m.getVersion()));
            			
            			if (m.isEnabled() && !compareModsVersions(m.getVersion(), (String)entry.getValue()))
                			modUnsatisfiedEx.add(Tuple.from(m.getName(), m.getVersion()));
            		}
            	}
    		}
    		
            if (!modDisabledEx.isEmpty())
            	throw new ModNotEnabledException(modDisabledEx);
            
            // We place ModNotEnabledException higher priority than ModVersionUnsatisfied
            if (!modUnsatisfiedEx.isEmpty())
            	throw new ModVersionUnsatisfiedException(modUnsatisfiedEx);

    	}    	
    }

    /**
     * This function checks to see if there is any conflict by the given mod with other enabled mods.
     * @param mod to be checked.
     * @throws ModEnabledException if another mod that is already enabled has a conflict with the mod given by parameter.
     */
    private void checkcons(Mod mod) throws ModConflictException {
    	Iterator it = cons.iterator();
    	HashSet<Pair<String, String>> list = new HashSet<Pair<String, String>>();
    	
    	while (it.hasNext()) {
    		HashMap<String, String> mapping = (HashMap<String, String>)it.next();
    		
    		// If this mapping has this mod name
    		if (mapping.containsKey(mod.getName())) {
    			
				// Then it is probably saying this mod mod
				if (compareModsVersions(mod.getVersion(), mapping.get(mod.getName()))) {
					Iterator itt = mapping.entrySet().iterator();
					while (itt.hasNext()) {
						Map.Entry entry = (Map.Entry)itt.next();
						if (!entry.getKey().equals(mod.getName())) {
							Enumeration modsConflict = Collections.enumeration(ManagerOptions.getInstance().getModsWithName((String)entry.getKey()));
							while (modsConflict.hasMoreElements()) {
								Mod compare = (Mod)modsConflict.nextElement();
								if (compareModsVersions(compare.getVersion(), (String)entry.getValue()) && compare.isEnabled())
									list.add(Tuple.from((String)entry.getKey(), (String)entry.getValue()));
							}
						}
					}
				}
    		}
    	}
    	
    	if (!list.isEmpty()) {
            throw new ModConflictException(list);
        }        
    }

    /**
     * This function trys to enable the mod with the name given. Throws exceptions if didn't no success while enabling the mod.
     * ignoreVersion should be always false, unless the user especifically says so.
     * @param name of the mod
     * @throws ModEnabledException if a mod was enabled and caused an incompatibility with the Mod that is being tryied to apply.
     * @throws ModNotEnabledException if a mod that was required by this mod wasn't enabled.
     * @throws NoSuchElementException if the mod doesn't exist
     * @throws ModVersionMissmatchException if the mod's version is imcompatible with the game version.
     * @throws NullPointerException if there is a problem with the game path (maybe the path was not set in the game class,
     * or hon.exe wasn't found, or happened a random I/O error).
     * @throws FileNotFoundException if the Hon.exe file wasn't found
     * @throws IOException if a random I/O Exception happened.
     * @throws IllegalArgumentException if a mod used a invalid parameter to compare the mods version.
     */
    public void enableMod(Mod m, boolean ignoreVersion) throws ModConflictException, ModVersionUnsatisfiedException, ModEnabledException, ModNotEnabledException, NoSuchElementException, ModVersionMissmatchException, NullPointerException, FileNotFoundException, IllegalArgumentException, IOException {
        if (!ignoreVersion) {
            if (m.getAppVersion() != null) {
                if (!m.getAppVersion().contains("-") && !m.getAppVersion().contains("*")) {
                    if (!compareModsVersions(Game.getInstance().getVersion(), m.getAppVersion() + ".*")) {
                        throw new ModVersionMissmatchException(m.getName(), m.getVersion(), m.getAppVersion());
                    }
                } else if (m.getAppVersion().contains("*") && !m.getAppVersion().contains("-")) {
                    if (!compareModsVersions(Game.getInstance().getVersion(), "-" + m.getAppVersion())) {
                        throw new ModVersionMissmatchException(m.getName(), m.getVersion(), m.getAppVersion());
                    }
                } else {
                    if (!compareModsVersions(Game.getInstance().getVersion(), m.getAppVersion())) {
                        throw new ModVersionMissmatchException(m.getName(), m.getVersion(), m.getAppVersion());
                    }
                }
            }
        }
        if (!m.isEnabled()) {
            checkcons(m);
            checkdeps(m);
            // enable it
            ManagerOptions.getInstance().getMods().get(ManagerOptions.getInstance().getMods().indexOf(m)).enable();
        }
    }

    /**
     * Tries to disable a mod given by it's name. Throws exception if an error occoured.
     * @param name of the mod.
     * @throws ModEnabledException if another mod is enabled and requires the given by parameter mod to continue enabled.
     * @throws ModVersionUnsatisfiedException 
     * @throws ModNotEnabledException 
     */
    public void disableMod(Mod m) throws ModEnabledException, ModNotEnabledException, ModVersionUnsatisfiedException {
        if (m.isEnabled()) {
            checkdeps(m);
            // disable it
            ManagerOptions.getInstance().getAppliedMods().remove(m);
            ManagerOptions.getInstance().getMods().get(ManagerOptions.getInstance().getMods().indexOf(m)).disable();
        }
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public ArrayList<Mod> sortMods() throws IOException {
        ArrayList<Mod> left = new ArrayList<Mod>();
        
        // Filling left queue with unordered list of mods to apply
        for (int i = 0; i < ManagerOptions.getInstance().getMods().size(); i++) {
            if (ManagerOptions.getInstance().getMods().get(i).isEnabled()) {
                left.add(ManagerOptions.getInstance().getMods().get(i));
            }
        }
        
        // Sorting by deps
        for (int i = 0; i < left.size(); i++) {
        	Mod m = left.get(i);
        	int j = i - 1;
        	while (j >= 0 && deps.containsKey(left.get(j)) && deps.get(left.get(j)).containsKey(m.getName())) {
        		// Swap
        		left.set(j + 1, left.get(j));
        		j--;
        		left.set(j + 1, m);
        	}
        }
        
        // Sorting by before after
        for (int i = 0; i < left.size(); i++) {
        	Mod m = left.get(i);
        	int j = i - 1;
        	while (j >= 0 && after.containsKey(left.get(j)) && after.get(left.get(j)).containsKey(m.getName())) {
        		// Swap
        		left.set(j + 1, left.get(j));
        		j--;
        		left.set(j + 1, m);
        	}
        }
        for (int i = 0; i < left.size(); i++) {
        	Mod m = left.get(i);
        	int j = i - 1;
        	while (j >= 0 && before.containsKey(m) && before.get(m).containsKey(left.get(j).getName())) {
        		// Swap
        		left.set(j + 1, left.get(j));
        		j--;
        		left.set(j + 1, m);
        	}
        }
        
        // Print out the result
        for (int i = 0; i < left.size(); i++) {
        	logger.error("applying order #" + i + " = " + left.get(i).getName());
        }
        
        return left;
    }

    /**
     * Tries to apply the currently enabled mods. They can be found in the Model class.
     * @throws IOException if a random I/O error happened.
     * @throws UnknowModActionException if a unkown Action was found. Actions that aren't know by the program can't be applied.
     * @throws NothingSelectedModActionException if a action tried to do a action that involves a string, but no string was selected.
     * @throws StringNotFoundModActionException if a search for a string was made, but that string wasn't found. Probally,
     * imcompatibility or a error by the mod's author.
     * @throws InvalidModActionParameterException if a action had a invalid parameter. Only the position of actions 'insert' and 'find' can throw this exception.
     * @throws SecurityException if the Manager couldn't do a action because of security business.
     * @throws FileLockInterruptionException if the Manager couldn't open the resources999.s2z file.
     */
    public void applyMods() throws IOException,
            UnknowModActionException,
            NothingSelectedModActionException,
            StringNotFoundModActionException,
            InvalidModActionParameterException,
            SecurityException,
            FileLockInterruptionException {
        ArrayList<Mod> applyOrder = sortMods();
        File tempFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + "HoN Mod Manager");
        // This generates a temp folder. If it isn't possible, generates a random folder inside the OS's temp folder.
        if (tempFolder.exists()) {
            if (!tempFolder.delete()) {
                Random r = new Random();
                tempFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + r.nextLong());
                if (tempFolder.exists()) {
                    if (!tempFolder.delete()) {
                        throw new SecurityException();
                    }
                }
            }
        }
        logger.info("Started mod applying. Folder=" + tempFolder.getAbsolutePath());
        tempFolder.mkdirs();
        Enumeration<Mod> list = Collections.enumeration(applyOrder);
        while (list.hasMoreElements()) {
            Mod mod = list.nextElement();
            logger.info("Applying Mod=" + mod.getName() + " | Version=" + mod.getVersion());
            for (int j = 0; j < mod.getActions().size(); j++) {
                Action action = mod.getActions().get(j);
                if (action.getClass().equals(ActionCopyFile.class)) {
                    ActionCopyFile copyfile = (ActionCopyFile) action;
                    if (!isValidCondition(action)) {
                        // condition isn't valid, can't apply
                        // No need to throw execption, since if condition isn't valid, this action won't be applied
                    } else {
                        // if path2 is not specified, path1 is copied
                        String toCopy;
                        if (copyfile.getSource() == null || copyfile.getSource().isEmpty() || copyfile.getSource().equals("")) {
                            toCopy = copyfile.getName();
                        } else {
                            // path2 is copied and renamed to path1
                            toCopy = copyfile.getSource();
                        }

                        if (copyfile.overwrite() == -1) {
                            throw new InvalidModActionParameterException(mod.getName(), mod.getVersion(), (Action) copyfile);
                        }

                        File temp = new File(tempFolder.getAbsolutePath() + File.separator + copyfile.getName());
                        if (temp.exists()) {
                            if (copyfile.overwrite() == 0) {
                                // Don't overwrite, do nothing
                            } else if (copyfile.overwrite() == 1) {
                                // Overwrite if newer
                                if (ZIP.getLastModified(new File(mod.getPath()), toCopy) > temp.lastModified()) {
                                    byte[] file = ZIP.getFile(new File(mod.getPath()), toCopy);
                                    FileOutputStream fos = new FileOutputStream(temp);
                                    if (temp.delete() && temp.createNewFile()) {
                                        ByteArrayInputStream bais = new ByteArrayInputStream(file);
                                        ZIP.copyInputStream(bais, fos);
                                        bais.close();
                                        fos.flush();
                                        fos.close();
                                    } else {
                                        throw new SecurityException();
                                    }
                                }
                            } else if (copyfile.overwrite() == 2) {
                                // overwrite file
                                byte[] file = ZIP.getFile(new File(mod.getPath()), toCopy);
                                FileOutputStream fos = new FileOutputStream(temp);
                                if (temp.delete() && temp.createNewFile()) {
                                    ByteArrayInputStream bais = new ByteArrayInputStream(file);
                                    ZIP.copyInputStream(bais, fos);
                                    bais.close();
                                    fos.flush();
                                    fos.close();
                                } else {
                                    throw new SecurityException();
                                }
                            }
                        } else {
                            // if temporary file doesn't exists
                            if (!temp.getParentFile().exists() && !temp.getParentFile().mkdirs()) {
                                throw new SecurityException();
                            }
                            byte[] file = ZIP.getFile(new File(mod.getPath()), toCopy);
                            FileOutputStream fos = new FileOutputStream(temp);
                            ByteArrayInputStream bais = new ByteArrayInputStream(file);
                            ZIP.copyInputStream(bais, fos);
                            fos.flush();
                            bais.close();
                        }
                        temp.setLastModified(ZIP.getLastModified(new File(mod.getPath()), toCopy));
                    }
                } else if (action.getClass().equals(ActionEditFile.class)) {
                    ActionEditFile editfile = (ActionEditFile) action;
                    if (!isValidCondition(action)) {
                        // condition isn't valid, can't apply
                        // No need to throw execption, since if condition isn't valid, this action won't be applied
                    } else {

                        // the selection is stored here
                        int cursor[] = new int[]{0};
                        int cursor2[] = new int[]{0};

                        // check if something is selected
                        boolean isSelected = false;

                        // Check for file
                        File f = new File(tempFolder.getAbsolutePath() + File.separator + editfile.getName());
                        String afterEdit = "";
                        if (f.exists()) {
                            // Load file from temp folder. If any other mod changes the file, it's actions won't be lost.
                            afterEdit = FileUtils.loadFile(f);
                        } else {
                            // Load file from resources0.s2z if no other mod edited this file
                            String path = ManagerOptions.getInstance().getGamePath() + File.separator + "game" + File.separator + "resources0.s2z";
                            afterEdit = new String(ZIP.getFile(new File(path), editfile.getName()), "UTF-8");
                        }
                        for (int k = 0; k < editfile.getActions().size(); k++) {
                            ActionEditFileActions editFileAction = editfile.getActions().get(k);

                            // Delete Action
                            if (editFileAction.getClass().equals(ActionEditFileDelete.class)) {
                                if (isSelected) {
                                    for (int i = 0; i < cursor.length; i++) {
                                        afterEdit = afterEdit.substring(0, cursor[i]) + afterEdit.substring(cursor2[i]);
                                        cursor2[i] = cursor[i];
                                        for (int l = i + 1; k < cursor.length; k++) {
                                            cursor[l] = cursor[l] - (cursor2[l] - cursor[l]);
                                            cursor2[l] = cursor2[l] - (cursor2[l] - cursor[l]);
                                        }
                                        isSelected = false;
                                    }
                                } else {
                                    ActionEditFileDelete delete = (ActionEditFileDelete) editFileAction;
                                    throw new NothingSelectedModActionException(mod.getName(), mod.getVersion(), (Action) delete);
                                }
                                // Find Action
                            } else if (editFileAction.getClass().equals(ActionEditFileFind.class)) {
                                ActionEditFileFind find = (ActionEditFileFind) editFileAction;
                                cursor = new int[]{cursor[0]};
                                cursor2 = new int[]{cursor2[0]};
                                if (find.getPosition() != null) {
                                    if (find.isPositionAtEnd()) {
                                        cursor[0] = afterEdit.length();
                                        cursor2[0] = cursor[0];
                                        isSelected = true;
                                    } else if (find.isPositionAtStart()) {
                                        cursor[0] = 0;
                                        cursor2[0] = 0;
                                        isSelected = true;
                                    } else {
                                        try {
                                            cursor[0] = cursor[0] + Integer.parseInt(find.getPosition());
                                            cursor2[0] = cursor[0];
                                            isSelected = true;
                                        } catch (NumberFormatException e) {
                                            // it isn't a valid number or word, can't apply
                                            throw new InvalidModActionParameterException(mod.getName(), mod.getVersion(), (Action) find);
                                        }
                                    }
                                } else {
                                    cursor[0] = afterEdit.toLowerCase().indexOf(find.getContent().toLowerCase(), cursor[0]);
                                    if (cursor[0] == -1) {
                                        // couldn't find the string, can't apply
                                        throw new StringNotFoundModActionException(mod.getName(), mod.getVersion(), (Action) find, find.getContent());
                                    }
                                    cursor2[0] = cursor[0] + find.getContent().length();
                                    isSelected = true;
                                }

                                // FindUp Action
                            } else if (editFileAction.getClass().equals(ActionEditFileFindUp.class)) {
                                ActionEditFileFindUp findup = (ActionEditFileFindUp) editFileAction;
                                cursor = new int[1];
                                cursor2 = new int[1];
                                cursor[0] = afterEdit.toLowerCase().lastIndexOf(findup.getContent().toLowerCase(), cursor[0]);
                                if (cursor[0] == -1) {
                                    // couldn't find the string, can't apply
                                    throw new StringNotFoundModActionException(mod.getName(), mod.getVersion(), (Action) findup, findup.getContent());
                                }
                                cursor2[0] = cursor[0] + findup.getContent().length();
                                isSelected = true;
                                // FindAll Action
                            } else if (editFileAction.getClass().equals(ActionEditFileFindAll.class)) {
                                ActionEditFileFindAll findall = (ActionEditFileFindAll) editFileAction;
                                // Preparation
                                ArrayList<Integer> firstPosition = new ArrayList<Integer>();
                                ArrayList<Integer> lastPosition = new ArrayList<Integer>();
                                int index = -1;
                                int lastIndex = 0;
                                while ((index = afterEdit.toLowerCase().indexOf(findall.getContent().toLowerCase(), lastIndex)) != -1) {
                                    firstPosition.add(index);
                                    lastPosition.add(index + findall.getContent().length());
                                    lastIndex = index + findall.getContent().length();
                                }
                                if (firstPosition.isEmpty()) {
                                    // no string was found, can't apply
                                    throw new StringNotFoundModActionException(mod.getName(), mod.getVersion(), (Action) findall, findall.getContent());
                                }
                                // Insert into the array
                                cursor = new int[firstPosition.size()];
                                cursor2 = new int[firstPosition.size()];
                                for (int i = 0; i < cursor.length; i++) {
                                    cursor[i] = firstPosition.get(i);
                                    cursor2[i] = lastPosition.get(i);
                                }
                                isSelected = true;
                                // Insert Action
                            } else if (editFileAction.getClass().equals(ActionEditFileInsert.class)) {
                                ActionEditFileInsert insert = (ActionEditFileInsert) editFileAction;
                                if (isSelected) {
                                    for (int i = 0; i < cursor.length; i++) {
                                        if (insert.isPositionAfter()) {
                                            afterEdit = afterEdit.substring(0, cursor2[i]) + insert.getContent() + afterEdit.substring(cursor2[i]);
                                            for (int l = i + 1; k < cursor.length; k++) {
                                                cursor[l] = cursor[l] + insert.getContent().length();
                                                cursor2[l] = cursor2[l] + insert.getContent().length();
                                            }
                                        } else if (insert.isPositionBefore()) {
                                            afterEdit = afterEdit.substring(0, cursor[i]) + insert.getContent() + afterEdit.substring(cursor[i]);
                                            cursor[i] = cursor2[i];
                                            cursor2[i] = cursor[i] + insert.getContent().length();
                                            for (int l = i + 1; k < cursor.length; k++) {
                                                cursor[l] = cursor[l] + insert.getContent().length();
                                                cursor2[l] = cursor2[l] + insert.getContent().length();
                                            }
                                        } else {
                                            // position is invalid, can't apply
                                            throw new InvalidModActionParameterException(mod.getName(), mod.getVersion(), (Action) insert);
                                        }
                                    }
                                } else {
                                    // the guy didn't select anything yet, can't apply
                                    throw new NothingSelectedModActionException(mod.getName(), mod.getVersion(), (Action) insert);
                                }

                                // Replace Action
                            } else if (editFileAction.getClass().equals(ActionEditFileReplace.class)) {
                                ActionEditFileReplace replace = (ActionEditFileReplace) editFileAction;
                                if (isSelected) {
                                    for (int i = 0; i < cursor.length; i++) {
                                        afterEdit = afterEdit.substring(0, cursor[i]) + replace.getContent() + afterEdit.substring(cursor2[i]);
                                        for (int l = i + 1; k < cursor.length; k++) {
                                            cursor[l] = cursor[l] + replace.getContent().length();
                                            cursor2[l] = cursor2[l] + replace.getContent().length();
                                        }
                                    }
                                    isSelected = false;
                                } else {
                                    throw new NothingSelectedModActionException(mod.getName(), mod.getVersion(), (Action) replace);
                                }
                            } else {
                                // Unknow action, can't apply
                                throw new UnknowModActionException(editFileAction.getClass().getName(), mod.getName());
                            }
                        }
                        File temp = new File(tempFolder.getAbsolutePath() + File.separator + editfile.getName());
                        File folder = new File(temp.getAbsolutePath().replace(temp.getName(), "") + File.separator);
                        if (!folder.getAbsolutePath().equalsIgnoreCase(tempFolder.getAbsolutePath())) {
                            if (!folder.exists()) {
                                if (!folder.mkdirs()) {
                                    // Can't crete folders to path
                                    throw new SecurityException(folder.getAbsolutePath());
                                }
                            }
                        }

                        // Write String afterEdit to a file
                        FileOutputStream fos = new FileOutputStream(temp);
                        ByteArrayInputStream in = new ByteArrayInputStream(afterEdit.getBytes("UTF-8"));
                        //fos.write(afterEdit.getBytes("UTF-8"));
                        ZIP.copyInputStream(in, fos);
                        fos.flush();
                        fos.close();
                        in.close();

                    }
                    // ApplyAfter, ApplyBefore, Incompatibility, Requirement Action
                } else if (action.getClass().equals(ActionApplyAfter.class) || action.getClass().equals(ActionApplyBefore.class)
                        || action.getClass().equals(ActionIncompatibility.class) || action.getClass().equals(ActionRequirement.class)) {
                    // nothing to do
                } else {
                    // Unknow action, can't apply
                    throw new UnknowModActionException(action.getClass().getName(), mod.getName());
                }
            }
        }

        String dest = "";

        if (OS.isWindows() || OS.isLinux()) {
            dest = ManagerOptions.getInstance().getGamePath() + File.separator + "game" + File.separator + "resources999.s2z";
        } else if (OS.isMac()) {
            dest = System.getProperty("user.home") + File.separator + "Library/Application Support/Heroes of Newerth/game/resources999.s2z";
        }

        File targetZip = new File(dest);
        if (targetZip.exists()) {
            if (!targetZip.delete()) {
                throw new FileLockInterruptionException();
            }
        }

        if (!applyOrder.isEmpty()) {
            ZIP.createZIP(tempFolder.getAbsolutePath(), targetZip.getAbsolutePath());
        } else {
            targetZip.createNewFile();
        }
        ManagerOptions.getInstance().setAppliedMods(new HashSet<Mod>(applyOrder));
        saveOptions();
    }

    /**
     * Unapplies all currently enabled mods. After that, the method calls the saveOptions().
     * @throws SecurityException if a security issue happened, and the action couldn't be completed.
     * @throws IOException if a random I/O exception happened.
     */
    public void unapplyMods() throws SecurityException, IOException {
        ManagerOptions.getInstance().getAppliedMods().clear();
        Iterator<Mod> i = ManagerOptions.getInstance().getMods().iterator();
        while (i.hasNext()) {
            i.next().disable();
        }
        try {
            applyMods();
        } catch (IOException ex) {
            throw ex;
        } catch (UnknowModActionException ex) {
        } catch (NothingSelectedModActionException ex) {
        } catch (StringNotFoundModActionException ex) {
        } catch (InvalidModActionParameterException ex) {
        } catch (SecurityException ex) {
            throw ex;
        }
        try {
            saveOptions();
        } catch (IOException ex) {
            throw ex;
        }
    }

    /**
     * Compares the singleVersion of a Mod and another expressionVersion. Letters are ignored (they are removed before the test) and commas (,) are replaced by dots (.)
     * @param singleVersion is the base version to be compared of. For example, a Mod's version go in here ('1.3', '3.2.57').
     * @param expressionVersion generally you put the ApplyAfter, ApplyBefore, ConditionVersion here ('1.35-*').
     * @return true if the mods have the same expressionVersion OR the singleVersion is in the range of the passed String expressionVersion. False otherwise.
     * @throws InvalidParameterException if can't compare the versions for some reason (out of format).
     */
    public boolean compareModsVersions(String singleVersion, String expressionVersion) throws InvalidParameterException {

        boolean result = false;

        if (expressionVersion == null || expressionVersion.isEmpty()) {
            expressionVersion = "*";
        }

        for (int i = 0; i < expressionVersion.length(); i++) {
            if (Character.isLetter(expressionVersion.charAt(i))) {
                expressionVersion = expressionVersion.replaceFirst(Character.toString(expressionVersion.charAt(i)), "");
            }
        }
        for (int i = 0; i < singleVersion.length(); i++) {
            if (Character.isLetter(singleVersion.charAt(i))) {
                singleVersion = singleVersion.replaceFirst(Character.toString(singleVersion.charAt(i)), "");
            }
        }
        expressionVersion = expressionVersion.replace(",", ".");
        singleVersion = singleVersion.replace(",", ".");

        if (expressionVersion.equals("*-*") || expressionVersion.equals("*") || expressionVersion.equals(singleVersion)) {
            result = true;
        } else if (expressionVersion.contains("-")) {

            int check = 0;
            String vEx1 = expressionVersion.substring(0, expressionVersion.indexOf("-"));
            if (vEx1 == null || vEx1.isEmpty()) {
                vEx1 = "*";
            }
            String vEx2 = expressionVersion.substring(expressionVersion.indexOf("-") + 1, expressionVersion.length());
            if (vEx2 == null || vEx2.isEmpty()) {
                vEx2 = "*";
            }

            result = checkVersion(vEx1, singleVersion) && checkVersion(singleVersion, vEx2);
        } else {
            result = singleVersion.equals(expressionVersion);
        }
        return result;
    }

    /**
     * ??
     * @param action
     * @return
     */
    private boolean isValidCondition(Action action) {
        String condition = null;
        if (action.getClass().equals(ActionEditFile.class)) {
            ActionEditFile editfile = (ActionEditFile) action;
            condition = editfile.getCondition();
        } else if (action.getClass().equals(ActionCopyFile.class)) {
            ActionCopyFile copyfile = (ActionCopyFile) action;
            condition = copyfile.getCondition();
        }
        if (condition == null || condition.isEmpty()) {
            return true;
        }

        return isValidCondition(condition);
    }

    /**
     * findNextExpression returns the next expression from the given StringTokenizer
     * @param st
     * @return
     */
    private String findNextExpression(String previous, StringTokenizer st) {
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equalsIgnoreCase("\'")) {
                String mod = token;
                do {
                    String next = st.nextToken();
                    if (next.equalsIgnoreCase("\'")) {
                        mod += next;
                        break;
                    }
                    mod += next;
                } while (st.hasMoreTokens());

                return mod;
            } else if (token.equalsIgnoreCase("(")) {
                String cond = "";
                boolean done = false;
                int level = 0;
                do {
                    String next = st.nextToken();
                    if (next.equalsIgnoreCase("(")) {
                        level++;
                    } else if (next.equalsIgnoreCase(")")) {
                        if (level == 0 && !done) {
                            break;
                        } else {
                            level--;
                        }
                    }

                    cond += next;
                } while (!done && st.hasMoreTokens());

                return cond;
            } else if (token.equalsIgnoreCase(" ")) {
                continue;
            } else {
                String ret = "";
                do {
                    ret += token;
                    try {
                        token = st.nextToken();
                    } catch (Exception e) {
                        break;
                    }
                } while (st.hasMoreTokens());
                return ret;
            }
        }

        return "";
    }

    /**
     * checkVersion checks to see if first argument <= second argument is true
     * @param lower
     * @param higher
     * @return
     */
    public boolean checkVersion(String lower, String higher) {
        // boolean ret = true;

        if (lower.equalsIgnoreCase("*") || higher.equalsIgnoreCase("*")) {
            return true;
        }

        StringTokenizer lowst = new StringTokenizer(lower, ".", false);
        StringTokenizer highst = new StringTokenizer(higher, ".", false);

        while (lowst.hasMoreTokens() && highst.hasMoreTokens()) {
            String firsttk = lowst.nextToken();
            String secondtk = highst.nextToken();

            if (firsttk.contains("*") || secondtk.contains("*")) {
                return true;
            }

            int first = Integer.parseInt(firsttk);
            int second = Integer.parseInt(secondtk);

            if (first < second) {
                return true;
            } else if (first > second) {
                return false;
            } else if (first == second) {
                continue;
            }

            /*
            if (ret) {
            ret = (first <= second);
            } else if (!ret) {
            return ret;
            }
             */
        }
        if (lowst.hasMoreTokens()) {
            return false;
        }
        return true;
    }

    /**
     * validVesion checks to see if the version for m is within the condition set by version on the other parameter
     * in development
     * @param m
     * @param version
     * @return
     */
    public boolean validVersion(Mod m, String version) throws NumberFormatException {
        String target = m.getVersion().trim();

        if (version.contains("-")) {
            String low, high;
            low = version.substring(1, version.indexOf("-")).trim();
            high = version.substring(version.indexOf("-") + 1).trim();

            // checkVersion checks to see if first argument <= second argument is true
            return checkVersion(low, target) && checkVersion(target, high);

        } else if (version.isEmpty()) {
            return true;
        } else {
            String compare = version.trim();
            return compare.equalsIgnoreCase(target);
        }
    }

    /**
     * isValidCondition evaluates the condition string and return the result of it
     * Looks like it's working now, should work ;)
     * @param condition
     * @return
     */
    public boolean isValidCondition(String condition) {
        boolean valid = true;

        StringTokenizer st = new StringTokenizer(condition, "\' ()", true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (token.equalsIgnoreCase("\'")) {
                String mod = "";
                do {
                    String next = st.nextToken();
                    if (next.equalsIgnoreCase("\'")) {
                        break;
                    }
                    mod += next;
                } while (st.hasMoreTokens());

                try {
                    String version = "";
                    if (mod.endsWith("]")) {
                        version = mod.substring(mod.indexOf('[') + 1, mod.length() - 1);
                        mod = mod.substring(0, mod.indexOf('['));
                    }

                    Mod m = getMod(mod);

                    try {
                        valid = (m.isEnabled() && validVersion(m, version));
                    } catch (Exception e) {
                        valid = false;
                    }
                } catch (NoSuchElementException e) {
                    valid = false;
                }
            } else if (token.equalsIgnoreCase(" ")) {
                continue;
            } else if (token.equalsIgnoreCase("(")) {
                String cond = "";
                boolean done = false;
                int level = 0;
                do {
                    String next = st.nextToken();
                    if (next.equalsIgnoreCase("(")) {
                        level++;
                    } else if (next.equalsIgnoreCase(")")) {
                        if (level == 0 && !done) {
                            break;
                        } else {
                            level--;
                        }
                    }

                    cond += next;
                } while (!done && st.hasMoreTokens());

                return isValidCondition(cond);
            } else if (token.equalsIgnoreCase(")")) {
                return false;
            } else {
                String next = findNextExpression(token, st);

                if (token.equalsIgnoreCase("not")) {
                    valid = !isValidCondition(next);
                } else if (token.equalsIgnoreCase("and")) {
                    boolean compare = isValidCondition(next);
                    valid = (valid && compare);
                } else if (token.equalsIgnoreCase("or")) {
                    boolean compare = isValidCondition(next);
                    valid = (valid || compare);
                } else {
                    String mod = token + " " + next;
                }
            }
        }

        return valid;
    }
}
