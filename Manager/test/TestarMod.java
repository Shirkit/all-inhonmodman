package test;


import business.Mod;
import business.actions.Action;
import business.actions.ActionApplyAfter;
import utility.XML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Shirkit
 */
public class TestarMod {

    public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        File f = new File("/Users/penn/Documents/Development/HoNMoDMan/Manager/test/2.xml");
        Mod m = XML.xmlToMod(f);
        ArrayList<Action> test = m.getActions();
        System.out.println(test.get(1).getClass());
        XML.modToXml(m, new File("/Users/penn/Documents/Development/HoNMoDMan/Manager/test/out.xml"));
    }
}
