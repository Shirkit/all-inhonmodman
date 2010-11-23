/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.views;

import business.Mod;
import gui.ManagerGUI;
import gui.ModsTable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * Class of the pop-up right click menu in the JTable
 */
public class PopupListener extends MouseAdapter {

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) {
        ManagerGUI view = ManagerGUI.getInstance();
        Mod mod;
        ModsTable modsTable = view.getModsTable();
        if (e.isPopupTrigger()) {
            try {
                mod = view.getModsTable().getModAtPoint(e.getPoint());
                view.preparePopupMenu(mod);

                // This is actually required, not just a design decision.  When
                // options are selected, they use the same handlers as the "normal"
                // buttons, and the actions apply themselves to the selected mod.
                modsTable.setSelectedMod(mod);
                
                view.getRightClickTableMenu().show(e.getComponent(), e.getX(), e.getY());
            } catch(IndexOutOfBoundsException ex) {
                // Expected exception here if the mouse was not released over a
                // mod on the table.
            }
        }
    }
}
