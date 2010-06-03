package com.hon.test;

import nu.xom.*;
import com.hon.manager.Mod;

import java.io.*;


public class test {

    public static void main(String[] args) {
        Mod mod = new Mod("aal.honmod", "/Users/penn/Library/Application Support/Heroes of Newerth/game/mods");

        try {
            mod.getXML();
        } catch(Exception e) {
            System.out.println("Oh NOOOO!");
        }

        System.out.println(mod.getPath());
    }

}
