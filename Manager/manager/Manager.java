/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import business.ManagerOptions;
import business.Mod;
import business.OuterMod;
import utility.XML;
import utility.ZIP;
import business.actions.Action;
import business.actions.ActionApplyAfter;
import business.actions.ActionApplyBefore;
import business.actions.ActionIncompatibility;
import business.actions.ActionRequirement;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.mallardsoft.tuple.*;

/**
 *
 * @author Shirkit
 */
public class Manager {

    private static Manager instance = null;
    private ArrayList<Mod> mods;
    private ArrayList<ArrayList<Pair<String, String>>> deps;
    private ArrayList<ArrayList<Pair<String, String>>> cons;
    private ArrayList<Mod> applied;
    private ManagerOptions options = null; // What does this do?
    private static String MANAGER_FOLDER = "C:\\Manager"; // Is this necessary?
    private static String MODS_FOLDER = "";
    private static String HON_FOLDER = "hon"; // We need this
    private static String MODS_LIST = "list.txt";
    private ArrayList<Mod> lastMods;
    private int nextPriority;

    private Manager() {
        mods = new ArrayList<Mod>();
        deps = new ArrayList<ArrayList<Pair<String, String> > >();
        cons = new ArrayList<ArrayList<Pair<String, String> > >();
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
    
    public ArrayList<Mod> getAppliedMods() throws IOException {
    	for(int i = 0; i < mods.size(); i++) {
    		mods.get(i).disable();
    	}
    	
    	File list = new File(MODS_FOLDER + MODS_LIST); // the path needs to be take care of
    	ArrayList<Mod> tmp = new ArrayList<Mod>();
    	if(list.exists()) {
    		BufferedReader in = new BufferedReader(new FileReader(list));
    		try {
    			String line = null;
    			
    			while((line = in.readLine()) != null) {
    				tmp.add(mods.indexOf(getMod(line.trim())), getMod(line.trim()));
    				mods.get(mods.indexOf(getMod(line.trim()))).enable();
    			}
    		} catch(IOException e) {
    			throw e;
    		}
    	}
    	
    	return tmp;
    }
    
    /**
     * This should be called after adding all the honmod files to build and initialize the arrays
     * @throws IOException 
     */
    public void buildGraphs() throws IOException {
        applied = getAppliedMods();

        System.out.println("Size of deps " + deps.size());
        
    	for(int i = 0; i < mods.size(); i++) {
    		int length = mods.get(i).getActions().size();
    		for(int j = 0; j < length; j++) {
    			if(mods.get(i).getActions().get(j).getClass() == ActionApplyAfter.class) {
    				// do something
    			}
    			else if(mods.get(i).getActions().get(j).getClass() == ActionApplyBefore.class) {
    				// do something
    			}
    			else if(mods.get(i).getActions().get(j).getClass() == ActionIncompatibility.class) {
    				Pair<String, String> mod = Tuple.from(((ActionIncompatibility)mods.get(i).getActions().get(j)).getName(), ((ActionIncompatibility)mods.get(i).getActions().get(j)).getVersion());
    				if(cons.get(i) == null) {
    					ArrayList<Pair<String, String> > temp = new ArrayList<Pair<String, String> >();
    					temp.add(mod);
    					cons.add(i, temp);
    				}
    				else {
    					cons.get(i).add(mod);
    				}
    			}
    			else if(mods.get(i).getActions().get(j).getClass() == ActionRequirement.class) {
    				Pair<String, String> mod = Tuple.from(((ActionRequirement)mods.get(i).getActions().get(j)).getName(), ((ActionRequirement)mods.get(i).getActions().get(j)).getVersion());
    				if(deps.get(i) == null) {
    					ArrayList<Pair<String, String> > temp = new ArrayList<Pair<String, String> >();
    					temp.add(mod);
    					deps.add(i, temp);
    				}
    				else {
    					deps.get(i).add(mod);
    				}
    			}
    		}
    	}
    }

    /**
     * Adds a singleVersion to the list of mods. This list the real thing that the Manager uses, this is the main thing around here.
     * @param singleVersion to be added.
     */
    private void addMod(Mod mod) {
        mods.add(mod);
        deps.add(null);
        cons.add(null);
    }

    /**
     * Searches in the list of mods if the passed singleVersion is equal (using Mod.equals() method).
     * @param singleVersion to be removed.
     * @return the removed singleVersion if it was found.
     * @throws NoSuchFieldException if the passed singleVersion wasn't found in the list.
     * @see Mod.equals(Mod singleVersion)
     */
    /*
    public Mod removeMod(Mod mod) throws NoSuchFieldException {
        for (int i = 0; i < list.size(); i++) {
            OuterMod m = list.get(i);
            if (m.equals(mod)) {
                return list.remove(i);
            }
        }
        throw new NoSuchFieldException("Mod wasn't found");
    }
    */

    /**
     * This function is used internally from the GUI itself automatically when launch to initiate existing mods
     */
    public void addHonmod(File honmod) throws FileNotFoundException, IOException {
    	if(!honmod.exists())
    		throw new FileNotFoundException();
    	
        Random r = new Random();
        int id = 0;
        boolean test = true;

        // Generate a singleVersion ID. This probally will be replaced in some time.
        while (test) {
            id = r.nextInt();
            boolean pass = true;
            for (int j = 0; j < mods.size(); j++) {
                if (mods.get(j).getId() == id) {
                    pass = false;
                }
            }
            if (pass == true) {
                test = false;
            }
        }
        
        String xml = new String(ZIP.getFile(honmod, "mod.xml"));
        //System.out.println(xml);
        
        Mod m = XML.xmlToMod(xml);
        System.out.println("Name: " + m.getName());
        m.setPath(honmod.getAbsolutePath());
        m.setId(id);
//        if(zip)
//        	m.disable();
        addMod(m);


        /*
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
        */
    }

    /**
     * This function retrives ALL the mods, including the disabled ones in the local computer. Use the method isEnabled() to check if singleVersion is enabled.
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
    	while(e.hasMoreElements()) {
    		Mod m = (Mod)e.nextElement();
    		if(m.getName().equals(name)) {
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
    public Mod getEnabledMod(String name) throws NoSuchElementException {
    	Mod m = getMod(name);
    	if(m != null && m.isEnabled())
    		return m;
    	
		return null;
    }
    
    /**
     * This function checks to see if all dependencies are all satisfied    
     * @param m
     * @return True if all dependencies are satisfied else false
     */
    private boolean checkdeps(Mod m) {
    	// get a list of dependencies
    	ArrayList<Pair<String, String> > list = deps.get(mods.indexOf(m));
    	if(list == null || list.isEmpty())
    		return true;
    	else {
    		Enumeration e = Collections.enumeration(list);
    		
    		while(e.hasMoreElements()) {
    			Pair<String, String> dep = (Pair<String, String>)e.nextElement();
    			Mod d = getEnabledMod(Tuple.get1(dep));
    			if(d == null) {
    				return false;
    			}
    			
    			// compareModsVersion might not be working properly, ask him to fix it or something
    			/*
    			if(!compareModsVersions(Tuple.get2(dep), d.getVersion())) {
    				System.out.println("weird");
    				return false;
    			}
    			*/
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
    	ArrayList<Pair<String, String> > list = cons.get(mods.indexOf(m));
    	if(list == null || list.isEmpty()) {
    		return false;
    	}
    	Enumeration e = Collections.enumeration(list);
    	while(e.hasMoreElements()) {
    		Pair<String, String> check = (Pair<String, String>)e.nextElement();
    		if(getEnabledMod(Tuple.get1(check)) != null)
    			return true;
    	}
    	return false; 
    }
    
    private boolean revcheckdeps(Mod m) {
    	// get a list of dependencies on m
    	ArrayList list = new ArrayList();
	    Enumeration e = Collections.enumeration(deps);
	    while(e.hasMoreElements()) {
	    	ArrayList<Pair<String, String> > temp = (ArrayList<Pair<String, String> >)e.nextElement();
	    	Enumeration te = Collections.enumeration(temp);
	    	while(te.hasMoreElements()) {
	    		if(Tuple.get1((Pair<String, String>)te.nextElement()).equals(m.getName()))
	    			return true;
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
    	if(m.isEnabled())
    		return true;
    	if(checkdeps(m) && !checkcons(m)) {
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
    	if(!m.isEnabled())
    		return true;
    	if(!revcheckdeps(m)) {
    		// disable it
    		mods.get(mods.indexOf(m)).disable();
    		return true;
    	}
    	System.out.println("Can't disable mod " + name);
    	return false;
    }
    
    private Stack<Mod> BFS(Stack<Mod> stack, ArrayList<Pair<String, String> > dep) {
    	if(dep == null)
    		return stack;
    	if(dep.isEmpty())
    		return stack;
    	
    	LinkedList<Mod> queue = new LinkedList<Mod>();
    	queue.offer(stack.peek());
    	
    	while(stack.size() != mods.size()) {
    		Mod m = null;
    		try {
    			m = queue.remove();
    		} catch(NoSuchElementException e) {
    			break;
    		}
    		
    		if(stack.contains(m)) {
    			continue;
    		}
    		else {
    	    	Enumeration d = Collections.enumeration(dep);
    	    	
    			while(d.hasMoreElements()) {
    				Mod tmp = getMod(Tuple.get1((Pair<String, String>)d.nextElement()));
    				if(!stack.contains(tmp) && tmp != null) {
    					System.out.println("gmm: " + tmp.getName());
    					stack.push(tmp);
    				}
    				if(!queue.contains(tmp) && tmp != null)
    					queue.offer(tmp);
    			}			
    		}
    	}
		
		return stack;
    }
    
    public Stack<Mod> sortMods() {
    	Stack<Mod> stack = new Stack<Mod>();
    	
    	Enumeration e = Collections.enumeration(mods);
    	while(e.hasMoreElements()) {
    		Mod m = (Mod)e.nextElement();
    		if(!applied.contains(m) && m.isEnabled()) {
    			if(!stack.contains(m))
    				stack.add(m);
    			stack = BFS(stack, deps.get(mods.indexOf(m)));
    		}
    	}
    	
    	return stack;
    }

    /**
     * In developing.
     * @throws FileNotFoundException
     * @throws IOException
     */
    /*
    public void applyMods() throws FileNotFoundException, IOException {
    	ArrayList<Mod> applyOrder = sortMods();
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
    */

    /**
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

    /**
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
     * Not really working
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
