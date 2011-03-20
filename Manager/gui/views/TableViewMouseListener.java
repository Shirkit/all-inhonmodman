/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.views;

import business.Mod;
import gui.ManagerCtrl;
import gui.ManagerGUI;
import gui.ModsTable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * MouseAdapter for ModsTableViews to show the right-click menu, de/select mods
 * on double click, and potentially more.
 */
public class TableViewMouseListener extends MouseAdapter {

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) {
        ManagerGUI view = ManagerGUI.getInstance();
        ModsTable modsTable = view.getModsTable();
        Mod mod;

        try {
            mod = view.getModsTable().getModAtPoint(e.getPoint());
        } catch(IndexOutOfBoundsException ex) {
            // Expected exception here if the mouse was not released over a
            // mod on the table.
            return;
        }
        
        if (e.isPopupTrigger()) { // Show context menu
            view.preparePopupMenu(mod);

            // This is actually required, not just a design decision.  When
            // options are selected, they use the same handlers as the "normal"
            // buttons, and the actions apply themselves to the selected mod.
            modsTable.setSelectedMod(mod);

            view.getRightClickTableMenu().show(e.getComponent(), e.getX(), e.getY());
        } else if(e.getClickCount() == 2) { // Double click - toggle current mod
            ManagerCtrl.getInstance().enableMod(mod);
            view.displayModDetail(mod);
            modsTable.redraw();
        }
    }
}
