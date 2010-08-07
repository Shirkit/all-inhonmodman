/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

import business.ManagerOptions;

import com.mallardsoft.tuple.*;

/**
 * If a mod tried to be disabled but those mods depend on it are not cleared (a.k.a. not disabled), this exception is thrown.
 * @author Shirkit
 */
public class ModEnabledException extends Exception {

	private HashSet<Pair<String, String>> _deps;

    /**
     * @param name of the mod that was enabled.
     * @param version of the mod that was enabled.
     */
    public ModEnabledException(HashSet<Pair<String, String>> deps) {
        super();
        _deps = deps;
    }

    /**
     * @return the list of mods that depends on the mod that are not disabled
     */
    public HashSet<Pair<String, String>> getDeps() {
        return _deps;
    }
    
    /**
     * @return a string of mod names separated by comma in array _deps
     */
    public String toString() {
    	String ret = "";
    	Enumeration e = Collections.enumeration(_deps);
    	while (e.hasMoreElements()) {
    		ret += Tuple.get1((Pair<String, String>)e.nextElement());
    		ret += ", ";
    	}
    	
    	return ret.substring(0, ret.length()-2);
    }

}
