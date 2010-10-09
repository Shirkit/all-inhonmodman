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
 * This exception is thrown when mods enabled with same name but different versions
 * @author Penn
 */
public class ModSameNameDifferentVersionsException extends Exception {

	private HashSet<Pair<String, String>> _mods;

    /**
     * @param name of the mod that was enabled.
     * @param version of the mod that was enabled.
     */
    public ModSameNameDifferentVersionsException(HashSet<Pair<String, String>> mods) {
        super();
        _mods = mods;
    }

    /**
     * @return the list of mods that depends on the mod that are not disabled
     */
    public HashSet<Pair<String, String>> getMods() {
        return _mods;
    }
    
    /**
     * @return a string of mod names separated by comma in array _deps
     */
    public String toString() {
    	String ret = "";
    	Enumeration e = Collections.enumeration(_mods);
    	while (e.hasMoreElements()) {
    		ret += Tuple.get1((Pair<String, String>)e.nextElement());
    		ret += ", ";
    	}
    	
    	return ret.substring(0, ret.length()-2);
    }

}
