package test;


import manager.Manager;
import business.Mod;
import utility.XML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Shirkit
 */
public class ManagerTest {
	public static String path = "/Users/penn/Library/Application Support/Heroes of Newerth/game/mods/";

    public static void main(String args[]) throws FileNotFoundException, IOException {
    	Manager m = Manager.getInstance();
    	
    	File f = new File(path);
    	if(f.isDirectory()) {
    		System.out.println("Searching in " + f.getName());
    		String[] files = f.list();
    		for(int i = 0; i < files.length; i++) {
    			if(files[i].endsWith(".honmod")) {
    				System.out.println("Adding file " + files[i]);
    				m.addHonmod(new File(path + files[i]));
    			}
    		}
    	}
    	
    	m.buildGraphs();
    	
    	m.enableMod("Automatic Ability Learner");
    	m.enableMod("Tiny UI");
    	m.enableMod("MiniUI");
    	m.enableMod("Mod Options Framework");
    	m.enableMod("Movable Frames");
    	
    	
    	Stack<Mod> test = m.sortMods();
    	
		while(!test.isEmpty()) {
			Mod tmp = test.pop();
			System.out.println("Applying mod " + tmp.getName());
		}
    }
}

