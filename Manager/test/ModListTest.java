package test;


import manager.Manager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Shirkit
 */
public class ModListTest {

    public static void main(String args[]) throws FileNotFoundException, IOException, IllegalAccessException {

        Manager m = Manager.getInstance();
        m.addHonmod(new File("C:\\Mods\\peu.honmod"));
        m.addHonmod(new File("C:\\Mods\\spectator_logo.honmod"));
        m.addHonmod(new File("C:\\Mods\\MiniUI.honmod"));
        m.addHonmod(new File("C:\\Mods\\manacosts.honmod"));
        m.calculatePriority(-1);
        for (int i = 0; i < m.getListMods().size(); i++) {
            System.out.println(m.getListMods().get(i).getMod().getName() + ":" + m.getListMods().get(i).getPriority());
        }
    }
}
