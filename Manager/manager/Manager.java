/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import business.ManagerOptions;
import business.Mod;
import utility.XML;
import utility.ZIP;
import business.actions.ActionApplyAfter;
import business.actions.ActionApplyBefore;
import business.actions.ActionIncompatibility;
import business.actions.ActionRequirement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import com.mallardsoft.tuple.*;
import java.security.InvalidParameterException;

/**
 *
 * @author Shirkit
 */
public class Manager {

    private static Manager instance = null;
    private ArrayList<Mod> mods;
    private ArrayList<ArrayList<Pair<String, String>>> deps;
    private ArrayList<ArrayList<Pair<String, String>>> cons;
    private Set<Mod> applied;
    private ManagerOptions options = null; // What does this do?
    private static String MANAGER_FOLDER = "C:\\Manager"; // Is this necessary?
    private static String MODS_FOLDER = "";
    private static String HON_FOLDER = ""; // We need this
    private static String OPTIONS_PATH = ""; // Stores the absolute path to the option file
    private ArrayList<Mod> lastMods;
    private int nextPriority; // This is not necessary

    private Manager() {
        mods = new ArrayList<Mod>();
        deps = new ArrayList<ArrayList<Pair<String, String>>>();
        cons = new ArrayList<ArrayList<Pair<String, String>>>();
        options = new ManagerOptions();
        lastMods = new ArrayList<Mod>();
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

    public void setModPath(String p) {
        MODS_FOLDER = p;
    }

    public void setGamePath(String p) {
        HON_FOLDER = p;
    }

    public void setManagerPath(String p) {
        MANAGER_FOLDER = p;
    }
    
    public void setOptionsPath(String p) {
    	OPTIONS_PATH = p;
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
    
    public String getOptionsPath() {
    	return OPTIONS_PATH;
    }
    
    /**
     * This function is just for testing purpose
     * @param name
     */
    public void setAppliedMod(String name) {
    	if(applied.add(getMod(name)))
    		System.out.println("Setting mod " + name + " to applied successfully");
    	else
    		System.out.println("Setting mod " + name + " to applied NOT successfully");
    	
    }
    public void saveOptions() throws IOException {
    	options.saveOptions(new File(OPTIONS_PATH));
    }
    /**
     * This functions above are just for testing
     */
    
    /**
     * This will return the arraylist of applied mods
     * @return
     */
    public Set<Mod> getAppliedMods() {
    	return applied;
    }

    /**
     * This function will get the applied mods in resource999.s2z
     * It is not complete yet, will need saved settings from Manager Options
     * @return ArrayList<Mod> of Applied Mods
     * @throws IOException
     */
    public Set<Mod> loadAppliedMods() throws IOException {
    	return options.loadOptions(new File(OPTIONS_PATH)) ? options.getAppliedMods() : new HashSet<Mod>();
    	
    	/*
        File list = new File(MODS_FOLDER + MODS_LIST); // the path needs to be take care of
        ArrayList<Mod> tmp = new ArrayList<Mod>();
        if (list.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(list));
            try {
                String line = null;

                while ((line = in.readLine()) != null) {
                    tmp.add(mods.indexOf(getMod(line.trim())), getMod(line.trim()));
                    mods.get(mods.indexOf(getMod(line.trim()))).enable();
                }
            } catch (IOException e) {
                throw e;
            }
        }

        return tmp;
        */
    }

    /**
     * This should be called after adding all the honmod files to build and initialize the arrays
     * @throws IOException 
     */
    public void buildGraphs() throws IOException {
    	// First reset all new added mods from startup
        for (int i = 0; i < mods.size(); i++) {
            mods.get(i).disable();
        }
        
        // Get all applied mods from the ManagerOptions if there is any and update the lists
        applied = loadAppliedMods();

        // Now building the graph
        for (int i = 0; i < mods.size(); i++) {
            int length = mods.get(i).getActions().size();
            for (int j = 0; j < length; j++) {
                if (mods.get(i).getActions().get(j).getClass() == ActionApplyAfter.class) {
                    // do something
                } else if (mods.get(i).getActions().get(j).getClass() == ActionApplyBefore.class) {
                    // do something
                } else if (mods.get(i).getActions().get(j).getClass() == ActionIncompatibility.class) {
                    Pair<String, String> mod = Tuple.from(((ActionIncompatibility) mods.get(i).getActions().get(j)).getName(), ((ActionIncompatibility) mods.get(i).getActions().get(j)).getVersion());
                    if (cons.get(i) == null) {
                        ArrayList<Pair<String, String>> temp = new ArrayList<Pair<String, String>>();
                        temp.add(mod);
                        cons.add(i, temp);
                    } else {
                        cons.get(i).add(mod);
                    }
                } else if (mods.get(i).getActions().get(j).getClass() == ActionRequirement.class) {
                    Pair<String, String> mod = Tuple.from(((ActionRequirement) mods.get(i).getActions().get(j)).getName(), ((ActionRequirement) mods.get(i).getActions().get(j)).getVersion());
                    if (deps.get(i) == null) {
                        ArrayList<Pair<String, String>> temp = new ArrayList<Pair<String, String>>();
                        temp.add(mod);
                        deps.add(i, temp);
                    } else {
                        deps.get(i).add(mod);
                    }
                }
            }
        }
    }

    /**
     * Adds a Mod to the list of mods. This list is the real thing that the Manager uses.
     * @param Mod to be added.
     */
    private void addMod(Mod mod) {
        mods.add(mod);
        deps.add(null);
        cons.add(null);
    }

    /**
     * This function is used internally from the GUI itself automatically when launch to initiate existing mods.
     * @param honmod is the file (.honmod) to be add.
     */
    public void addHonmod(File honmod) throws FileNotFoundException, IOException {
        if (!honmod.exists()) {
            throw new FileNotFoundException();
        }

        Random r = new Random();
        boolean test = true;

        String xml = new String(ZIP.getFile(honmod, "mod.xml"));

        Mod m = XML.xmlToMod(xml);
        System.out.println("Name: " + m.getName());
        m.setPath(honmod.getAbsolutePath());
        m.setId(0);
        addMod(m);
    }

    /**
     * This function retrives ALL the mods, including the disabled ones in the local computer. Use the method isEnabled() to check if Mod is enabled.
     * @return An ArrayList of Mods containing all mods in the local computer.
     * @see Mod.isEnabled()
     */
    public ArrayList<Mod> getMods() {
        return mods;
    }

    /**
     * This function returns the mod from the arraylist mods
     * @param name
     * @return Mod
     */
    public Mod getMod(String name) throws NoSuchElementException {
        // get the enumration object for ArrayList mods
        Enumeration e = Collections.enumeration(mods);

        // enumerate through the ArrayList to find the mod
        while (e.hasMoreElements()) {
            Mod m = (Mod) e.nextElement();
            if (m.getName().equals(name)) {
                return m;
            }
        }

        return null;
    }

    /**
     * This function returns the mod from the arraylist mods
     * @param name
     * @return Mod
     */
    public Mod getEnabledMod(String name) {
        Mod m = getMod(name);
        if (m != null && m.isEnabled()) {
            return m;
        }

        return null;
    }

    /**
     * This function checks to see if all dependencies are all satisfied    
     * @param m
     * @return True if all dependencies are satisfied else false
     */
    private boolean checkdeps(Mod m) {
        // get a list of dependencies
        ArrayList<Pair<String, String>> list = deps.get(mods.indexOf(m));
        if (list == null || list.isEmpty()) {
            return true;
        } else {
            Enumeration e = Collections.enumeration(list);

            while (e.hasMoreElements()) {
                Pair<String, String> dep = (Pair<String, String>) e.nextElement();
                Mod d = getEnabledMod(Tuple.get1(dep));
                if (d == null) {
                    return false;
                }

                // compareModsVersion might not be working properly, ask him to fix it or something
                if(!compareModsVersions(d.getVersion(), Tuple.get2(dep))) {
                	System.out.println("weird");
                	return false;
                }
                
            }

            return true;
        }
    }

    /**
     * This function checks to see if there is any conflict
     * @param m
     * @return True if there is any, false otherwise
     */
    private boolean checkcons(Mod m) {
        // get a list of conflicts
        ArrayList<Pair<String, String>> list = cons.get(mods.indexOf(m));
        if (list == null || list.isEmpty()) {
            return false;
        }
        Enumeration e = Collections.enumeration(list);
        while (e.hasMoreElements()) {
            Pair<String, String> check = (Pair<String, String>) e.nextElement();
            if (getEnabledMod(Tuple.get1(check)) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean revcheckdeps(Mod m) {
        // get a list of dependencies on m
        ArrayList list = new ArrayList();
        Enumeration e = Collections.enumeration(deps);
        while (e.hasMoreElements()) {
            ArrayList<Pair<String, String>> temp = (ArrayList<Pair<String, String>>) e.nextElement();
            Enumeration te = Collections.enumeration(temp);
            while (te.hasMoreElements()) {
                if (Tuple.get1((Pair<String, String>) te.nextElement()).equals(m.getName())) {
                    return true;
                }
//	    			list.add(deps.indexOf(temp));
            }
        }

        return false;
    }

    /**
     * This function trys to enable the mod with the name given and return true if succeeded or false if not
     * @param name
     * @return Whether the function enable the mod successfully
     */
    public boolean enableMod(String name) {
        Mod m = getMod(name);
        if (m.isEnabled()) {
        	System.out.println("Mod " + name + " already enabled");
            return true;
        }
        if (checkdeps(m) && !checkcons(m)) {
            // enable it
            mods.get(mods.indexOf(m)).enable();
            return true;
        }
        System.out.println("Can't enable mod " + name);
        return false;
    }

    /**
     * This function is similar to enableMod in which it disable the mod instead
     * @param name
     * @return Whether the function disable the mod successfully
     */
    public boolean diableMod(String name) {
        Mod m = getMod(name);
        if (!m.isEnabled()) {
        	System.out.println("Mod " + name + " already disabled");
            return true;
        }
        if (!revcheckdeps(m)) {
            // disable it
            mods.get(mods.indexOf(m)).disable();
            return true;
        }
        System.out.println("Can't disable mod " + name);
        return false;
    }

    private Stack<Mod> BFS(Stack<Mod> stack, ArrayList<Pair<String, String>> dep) {
        if (dep == null) {
            return stack;
        }
        if (dep.isEmpty()) {
            return stack;
        }

        LinkedList<Mod> queue = new LinkedList<Mod>();
        queue.offer(stack.peek());

        while (stack.size() != mods.size()) {
            Mod m = null;
            try {
                m = queue.remove();
            } catch (NoSuchElementException e) {
                break;
            }

            if (stack.contains(m)) {
                continue;
            } else {
                Enumeration d = Collections.enumeration(dep);

                while (d.hasMoreElements()) {
                    Mod tmp = getMod(Tuple.get1((Pair<String, String>) d.nextElement()));
                    if (!stack.contains(tmp) && tmp != null) {
                        System.out.println("gmm: " + tmp.getName());
                        stack.push(tmp);
                    }
                    if (!queue.contains(tmp) && tmp != null) {
                        queue.offer(tmp);
                    }
                }
            }
        }

        return stack;
    }

    public Stack<Mod> sortMods() {
        Stack<Mod> stack = new Stack<Mod>();

        Enumeration e = Collections.enumeration(mods);
        while (e.hasMoreElements()) {
            Mod m = (Mod) e.nextElement();
            if (!applied.contains(m) && m.isEnabled()) {
                if (!stack.contains(m)) {
                    stack.add(m);
                }
                stack = BFS(stack, deps.get(mods.indexOf(m)));
            }
        }

        return stack;
    }

    public boolean saveOptions(File path) throws IOException {
        return this.options.saveOptions(path);
    }

    public void loadOptions(File path) throws FileNotFoundException {
        this.options.loadOptions(path);
    }

    /*
     * In developing.
     * @throws FileNotFoundException
     * @throws IOException
     */
	public void applyMods() throws FileNotFoundException, IOException {
		Stack<Mod> applyOrder = sortMods();
		while(!applyOrder.isEmpty()) {
			
		}
		/*
		for (int i = 0; i < list.size(); i++) {
			OuterMod m = list.get(i);
			if (m.isEnabled()) {
				for (int j = 0; j < m.getMod().getActions().size(); j++) {
					Action action = m.getMod().getActions().get(j);

					if (action.getType().equals(Action.APPLY_AFTER)) {
						ActionApplyAfter after = (ActionApplyAfter) action;
						String otherModName = after.getName();
						for (int k = 0; k < list.size(); k++) {
							if (list.get(k).getMod().getName()
									.equalsIgnoreCase(otherModName)) {
								if (compareModsVersions(
										m.getMod().getVersion(), after
												.getVersion())) {
								}
							}
						}
					}
				}
			}
		}
		*/
	}

    /*
     * In developing
     * @param index
     * @return
     * @deprecated Not using this anymore, no reason
     * @throws NumberFormatException
     */
    /*
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
     */
    /*
     * In developing.
     * @deprecated not using it
     * @return
     */
    /*
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
     */
    /**
     * Compares the singleVersion of a Mod and another expressionVersion.
     * @param singleVersion is the base version to be compared of. For example, a Mod's version go in here ('1.3', '3.2.57').
     * @param expressionVersion generally you put the ApplyAfter, ApplyBefore, ConditionVersion here ('1.35-*').
     * @return true if the mods have the same expressionVersion OR the singleVersion is in the range of the passed String expressionVersion. False otherwise.
     * @throws InvalidParameterException if can't compare the versions for some reason (out of format).
     */
    public boolean compareModsVersions(String singleVersion, String expressionVersion) throws InvalidParameterException {

        boolean result = false;

        
        if (expressionVersion == null) {
            expressionVersion = "*";
        }

        if ((expressionVersion.equals("*")) || (expressionVersion.equals(singleVersion)) || (expressionVersion.equals("*-*")) || (expressionVersion.equals(""))) {
            result = true;
        } else if (expressionVersion.contains("-")) {

            int check = 0;
            String vEx1 = expressionVersion.substring(0, expressionVersion.indexOf("-"));
            String vEx2 = expressionVersion.substring(expressionVersion.indexOf("-") + 1, expressionVersion.length());

            // singleVersion's expressionVersion but filled with '0' at the end, what causes a problem
            ArrayList<Integer> versionBase = new ArrayList<Integer>();
            String[] v1 = singleVersion.split("\\.");
            for (int i = 0; i < v1.length; i++) {
                if (!v1[i].equals("")) {
                    versionBase.add(new Integer(v1[i]));
                }
            }
            if (!vEx1.equals("*")) {
                ArrayList<Integer> versionExpression = new ArrayList<Integer>();
                String[] vExp = vEx1.split("\\.");
                for (int i = 0; i < vExp.length; i++) {
                    if (!vExp[i].equals("")) {
                        versionExpression.add(new Integer(vExp[i]));
                    }
                }
                for (int i = 0; i < versionExpression.size() && i < versionBase.size(); i++) {
                    if (versionExpression.get(i).compareTo(versionBase.get(i)) < 0) {
                        check++;
                        break;
                    }
                }
            } else {
                check++;
            }

            if (!vEx2.equals("*")) {
                ArrayList<Integer> versionExpression = new ArrayList<Integer>();
                String[] vExp = vEx2.split("\\.");
                for (int i = 0; i < vExp.length; i++) {
                    if (!vExp[i].equals("")) {
                        versionExpression.add(new Integer(vExp[i]));
                    }
                }
                for (int i = 0; i < versionExpression.size() && i < versionBase.size(); i++) {
                    if (versionExpression.get(i).compareTo(versionBase.get(i)) > 0) {
                        check++;
                        break;
                    }
                }
            } else {
                check++;
            }

            if (check >= 2) {
                result = true;
            }

        } else {
            throw new InvalidParameterException();
        }
        return result;
    }
}
