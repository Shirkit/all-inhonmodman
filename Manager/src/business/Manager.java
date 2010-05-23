/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Shirkit
 */
public class Manager {

    private static Manager instance = null;
    private ArrayList<Mod> list;
    private ManagerOptions options = null;
    private static String MANAGER_FOLDER;
    private static String MODS_FOLDER = "mods";

    private Manager() {
        list = new ArrayList<Mod>();
        options = new ManagerOptions();
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
     * This method is used to get the running instance of the Manager class. This method is equal to getInstance(), but it's shorter.
     * @return the instance.
     * @see getInstance()
     */
    public static Manager get() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    /**
     * Adds a mod to the list of mods.
     * @param mod to be added.
     */
    private void addMod(Mod mod) {
        list.add(mod);
    }

    /**
     * Searches in the list of mods if the passed mod is equal (using Mod.equals() method).
     * @param mod to be removed.
     * @return the removed mod if it was found.
     * @throws NoSuchFieldException if the passed mod wasn't found in the list.
     * @see Mod.equals(Mod mod)
     */
    public Mod removeMod(Mod mod) throws NoSuchFieldException {
        for (int i = 0; i < list.size(); i++) {
            Mod m = list.get(i);
            if (m.equals(mod)) {
                return list.remove(i);
            }
        }
        throw new NoSuchFieldException("Mod wasn't found");
    }

    public void addHonmod(File honmod) throws FileNotFoundException, IOException {
        Random r = new Random();
        int id = 0;
        boolean test = true;
        while (test) {
            id = r.nextInt();
            boolean pass = true;
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).getId() == id) {
                    pass = false;
                }
            }
            if (pass == true) {
                test = false;
            }
        }
        File modPath = ZIP.openZIP(honmod, MANAGER_FOLDER + File.separator + MODS_FOLDER + File.separator + id);
        File[] content = modPath.listFiles();
        for (int i = 0; i < content.length; i++) {
            if (content[i].getName().equals(Mod.MOD_FILENAME)) {
                Mod m = XML.xmlToMod(content[i].getAbsoluteFile());
                m.setId(id);
                m.setFolder(new File(MANAGER_FOLDER + File.separator + MODS_FOLDER + File.separator + id));
                m.setPath(honmod.getAbsolutePath());
                addMod(m);
            }
        }
    }

    public ArrayList<Mod> getModsList() {
        return this.list;
    }
    
}
