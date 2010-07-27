/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import business.ManagerOptions;

import com.mallardsoft.tuple.*;

/**
 * If a mod tried to be enabled but some enabled dependency versions are not satisfied, this exception is thrown.
 * @author Penn
 */
public class ModVersionUnsatisfiedException extends Exception {

	private ArrayList<Pair<String, String>> _vers;

    /**
     * @param name of the mod that was enabled.
     * @param version of the mod that was enabled.
     */
    public ModVersionUnsatisfiedException(ArrayList<Pair<String, String>> vers) {
        super();
        _vers = vers;
    }

    /**
     * @return the list of mods that depends on the mod that are not disabled
     */
    public ArrayList<Pair<String, String>> getVers() {
        return _vers;
    }
    
    /**
     * @return a string of mod names separated by comma in array _deps
     */
    public String toString() {
    	String ret = "";
    	Enumeration e = Collections.enumeration(_vers);
    	while (e.hasMoreElements()) {
    		ret += Tuple.get1((Pair<String, String>)e.nextElement());
    		ret += ", ";
    	}
    	
    	return ret.substring(0, ret.length()-2);
    }

}
