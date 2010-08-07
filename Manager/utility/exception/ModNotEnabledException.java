/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

import com.mallardsoft.tuple.*;

/**
 * If a mod tried to be enabled but the dependencies are not satisfied (a.k.a. not enabled), this exception is thrown.
 * @author Shirkit
 */
public class ModNotEnabledException extends Exception {

	private HashSet<Pair<String, String>> _deps;

    /**
     * @param name of the mod that wasn't enabled.
     * @param version of the mod that wasn't enabled.
     */
    public ModNotEnabledException(HashSet<Pair<String, String>> deps) {
        super();
        _deps = deps;
    }

    /**
     * @return the list of dependencies that are not enabled for the problematic mod
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
