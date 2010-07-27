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
 * If a mod tried to be enabled but the conflicts are not cleared, this exception is thrown.
 * @author Penn
 */
public class ModConflictException extends Exception {

	private ArrayList<Pair<String, String>> _cons;

    /**
     * @param name of the mod that was enabled.
     * @param version of the mod that was enabled.
     */
    public ModConflictException(ArrayList<Pair<String, String>> cons) {
        super();
        _cons = cons;
    }

    /**
     * @return the list of mods that depends on the mod that are not disabled
     */
    public ArrayList<Pair<String, String>> getCons() {
        return _cons;
    }
    
    /**
     * @return a string of mod names separated by comma in array _deps
     */
    public String toString() {
    	String ret = "";
    	Enumeration e = Collections.enumeration(_cons);
    	while (e.hasMoreElements()) {
    		ret += Tuple.get1((Pair<String, String>)e.nextElement());
    		ret += ", ";
    	}
    	
    	return ret.substring(0, ret.length()-2);
    }

}
