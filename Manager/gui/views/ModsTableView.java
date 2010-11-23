/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.views;

import business.Mod;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionListener;

/**
 * @author George
 */
public abstract class ModsTableView {

    public ModsTableView(ArrayList<Mod> _modsList) { modsList = _modsList; }

    /**
     * @return the modsList
     */
    public ArrayList<Mod> getModsList() { return modsList; }

    /**
     * @param modsList the modsList to set
     */
    public void setModsList(ArrayList<Mod> modsList) {
        this.modsList = modsList;
    }

    /**
     * @param kl The key listener to add to this view's component
     */
    public void addKeyListener(KeyListener kl) {
        getComponent().addKeyListener(kl);
    }

    /**
     * @return the component
     */
    public JComponent getComponent() { return component; }

    /**
     * @param component the component to set
     */
    public void setComponent(JComponent component) {
        this.component = component;
    }

    public abstract Mod getModAt(int x, int y);
    public abstract Mod getModAt(Point p);
    public abstract Mod getSelectedMod();
    public abstract void setSelectedMod(Mod mod);
    public abstract boolean hasModSelected();
    public abstract void addListSelectionListener(ListSelectionListener lsl);
    public abstract void selectNextMod();
    public abstract void selectPrevMod();

    protected ArrayList<Mod> modsList;
    protected JComponent component;
}
