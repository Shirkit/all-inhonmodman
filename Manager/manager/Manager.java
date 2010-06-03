/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import business.ManagerOptions;
import business.Mod;
import business.OuterMod;
import business.XML;
import business.ZIP;
import business.actions.Action;
import business.actions.ActionApplyAfter;
import business.actions.ActionApplyBefore;
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
    private ArrayList<OuterMod> list;
    private ManagerOptions options = null;
    private static String MANAGER_FOLDER = "C:\\Manager";
    private static String MODS_FOLDER = "temp";
    private static String HONMODS_FOLDER = "honmod";
    private ArrayList<OuterMod> lastMods;
    private int nextPriority;

    private Manager() {
        list = new ArrayList<OuterMod>();
        options = new ManagerOptions();
        lastMods = new ArrayList<OuterMod>();
        nextPriority = 0;
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
     * Adds a singleVersion to the list of mods. This list the real thing that the Manager uses, this is the main thing around here.
     * @param singleVersion to be added.
     */
    private void addMod(Mod mod) {
        list.add(new OuterMod(mod));
    }

    /**
     * Searches in the list of mods if the passed singleVersion is equal (using Mod.equals() method).
     * @param singleVersion to be removed.
     * @return the removed singleVersion if it was found.
     * @throws NoSuchFieldException if the passed singleVersion wasn't found in the list.
     * @see Mod.equals(Mod singleVersion)
     */
    public OuterMod removeMod(OuterMod mod) throws NoSuchFieldException {
        for (int i = 0; i < list.size(); i++) {
            OuterMod m = list.get(i);
            if (m.equals(mod)) {
                return list.remove(i);
            }
        }
        throw new NoSuchFieldException("Mod wasn't found");
    }

    public void addHonmod(File honmod) throws FileNotFoundException, IOException, IllegalAccessException {
        Random r = new Random();
        int id = 0;
        boolean test = true;

        // Generate a singleVersion ID. This probally will be replaced in some time.
        while (test) {
            id = r.nextInt();
            boolean pass = true;
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).getMod().getId() == id) {
                    pass = false;
                }
            }
            if (pass == true) {
                test = false;
            }
        }

        // Extract the content
        File modPath = ZIP.openZIP(honmod, MANAGER_FOLDER + File.separator + MODS_FOLDER + File.separator + id);
        File[] content = modPath.listFiles();
        for (int i = 0; i < content.length; i++) {
            // Check for the mod.xml file and loads the mod
            if (content[i].getName().equals(Mod.MOD_FILENAME)) {
                Mod m = XML.xmlToMod(content[i].getAbsoluteFile());
                m.setId(id);
                m.setPath(honmod.getAbsolutePath());
                m.setFolder(null);
                addMod(m);
            }
        }
    }

    /**
     * This function retrives ALL the mods, including the disabled ones in the local computer. Use the method isEnabled() to check if singleVersion is enabled.
     * @return An ArrayList of Mods containing all mods in the local computer.
     * @see Mod.isEnabled()
     */
    public ArrayList<OuterMod> getListMods() {
        return list;
    }

    /**
     * In developing.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void applyEnabledMods() throws FileNotFoundException, IOException {

        for (int i = 0; i < list.size(); i++) {
            OuterMod m = list.get(i);
            if (m.isEnabled()) {
                for (int j = 0; j < m.getMod().getActions().size(); j++) {
                    Action action = m.getMod().getActions().get(j);

                    if (action.getType().equals(Action.APPLY_AFTER)) {
                        ActionApplyAfter after = (ActionApplyAfter) action;
                        String otherModName = after.getName();
                        for (int k = 0; k < list.size(); k++) {
                            if (list.get(k).getMod().getName().equalsIgnoreCase(otherModName)) {
                                if (compareModsVersions(m.getMod().getVersion(), after.getVersion())) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * In developing
     * @param index
     * @return
     * @throws NumberFormatException
     */
    public int calculatePriority(int index) throws NumberFormatException {
        System.out.println(index);
        if (index == -1) {
            index = 0;
        } else {
            if (list.get(index).getPriority() != -999999999) {
                return list.get(index).getPriority();
            }
        }
        lastMods.add(list.get(index));
        if (checkLoop()) {
            if (list.get(index).getPriority() == -999999999) {
                list.get(index).setPriority(nextPriority);
                nextPriority++;
                
                return nextPriority;
            } else {
                throw new NumberFormatException("can't resolve priority");
            }
        }
        for (int i = 0; i < list.get(index).getMod().getActions().size(); i++) {
            if (list.get(index).getMod().getActions().get(i).getClass() == ActionApplyAfter.class) {
                ActionApplyAfter applyafter = (ActionApplyAfter) list.get(index).getMod().getActions().get(i);
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).getMod().getName().equals(applyafter.getName())) {
                        if (compareModsVersions(list.get(j).getMod().getVersion(), applyafter.getVersion())) {
                            list.get(index).setPriority(calculatePriority(j) + 1);
                        }
                    }
                }
            } else if (list.get(index).getMod().getActions().get(i).getClass() == ActionApplyBefore.class) {
                ActionApplyBefore applybefore = (ActionApplyBefore) list.get(index).getMod().getActions().get(i);
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).getMod().getName().equals(applybefore.getName())) {
                        if (compareModsVersions(list.get(j).getMod().getVersion(), applybefore.getVersion())) {
                            list.get(index).setPriority(calculatePriority(j) - 1);
                        }
                    }
                }
            }
        }

        if (list.get(index).getPriority() == -999999999) {
            list.get(index).setPriority(nextPriority);
            nextPriority++;
        }

        index++;
        if (index < list.size()) {
            calculatePriority(index);
        }
        return list.get(index - 1).getPriority();
    }

    /**
     * In developing.
     * @return
     */
    public boolean checkLoop() {
        boolean result = true;

        for (int i = 0; i < lastMods.size(); i++) {
            ArrayList<OuterMod> checkList = new ArrayList<OuterMod>(lastMods.subList(0, i));
            for (int j = 0; j < checkList.size(); j++) {
                if (list.contains(checkList.get(j))) {
                    if ((j + 1) < checkList.size()) {
                        if (list.get(list.indexOf(checkList.get(j + 1))) != checkList.get(j + 1)) {
                            result = false;
                        }
                    }
                }
            }
        }

        if (lastMods.size() >= 1) {
            return false;
        }

        return result;
    }

    /**
     * Compares the singleVersion of a Mod and another versionExpression.
     * @param singleVersion is the base version to be compared of. For example, a Mod's version go in here ('1.3', '3.2.57').
     * @param versionExpression generally you put the ApplyAfter, ApplyBefore, ConditionVersion here ('1.35-*').
     * @return true if the mods have the same versionExpression OR the singleVersion is in the range of the passed String versionExpression. False otherwise.
     */
    public boolean compareModsVersions(String singleVersion, String versionExpression) {

        boolean result = false;

        if (versionExpression.equals("*")) {
            result = true;
        } else if (versionExpression.equals(singleVersion)) {
            result = true;
        } else if (versionExpression.equals("*-*")) {
            result = true;
        } else if (versionExpression.equals("")) {
            result = true;
        } else if (versionExpression.contains("-")) {

            int check = 0;
            String v1 = versionExpression.substring(0, versionExpression.indexOf("-"));
            String v2 = versionExpression.substring(versionExpression.indexOf("-") + 1, versionExpression.length());

            // singleVersion's versionExpression but filled with '0' at the end, what causes a problem
            int[] iv = new int[singleVersion.length()];
            int j = 0;
            for (int i = 0; i < iv.length; i++) {
                iv[j] = -1;
                if (Character.isDigit(singleVersion.charAt(i))) {
                    iv[j] = singleVersion.charAt(i) - 48;
                    j++;
                }
            }

            if (!v1.equals("*")) {
                // first versionExpression
                int[] iv1 = new int[v1.length()];
                j = 0;
                for (int i = 0; i < iv1.length; i++) {
                    iv1[j] = 10;
                    if (Character.isDigit(v1.charAt(i))) {
                        iv1[j] = v1.charAt(i) - 48;
                        j++;
                    }
                }
                int i = 0;
                boolean working = true;
                while (working) {
                    if (iv[i] > iv1[i]) {
                        working = false;
                        check++;
                    }

                    i++;
                    if ((i >= iv.length) || (i >= iv1.length)) {
                        working = false;
                        if (iv[i - 1] == iv1[i - 1]) {
                            check++;
                        }
                    }
                }
            } else {
                check++;
            }

            if (!v2.equals("*")) {
                // first versionExpression
                int[] iv2 = new int[v2.length()];
                j = 0;
                for (int i = 0; i < iv2.length; i++) {
                    iv2[j] = -1;
                    if (Character.isDigit(v2.charAt(i))) {
                        iv2[j] = v2.charAt(i) - 48;
                        j++;
                    }
                }
                int i = 0;
                boolean working = true;
                while (working) {
                    if (iv[i] < iv2[i]) {
                        working = false;
                        check++;
                    }

                    i++;
                    if ((i >= iv.length) || (i >= iv2.length)) {
                        working = false;
                        if (iv[i - 1] == iv2[i - 1]) {
                            check++;
                        }
                    }
                }
            } else {
                check++;
            }
            if (check >= 2) {
                result = true;
            }
        }
        return result;
    }
}
