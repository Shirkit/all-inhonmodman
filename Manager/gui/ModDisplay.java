/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import business.Mod;
import javax.swing.Icon;

/**
 *
 * @author Shirkit
 */
public class ModDisplay extends javax.swing.JLabel {
    
    private Mod mod = null;
    
    public ModDisplay(String text, Icon image, Mod mod) {
        super();
        setMod(mod);
        setText(text);
        setIcon(image);
    }

    public ModDisplay() {
        super();
    }

    public void setMod(Mod mod) {
        this.mod = mod;
    }

    public Mod getMod() {
        return mod;
    }

}
