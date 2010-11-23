/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.views;

import business.Mod;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author George
 */
public class TilesView extends IconsView {
    public TilesView(ArrayList<Mod> _modsList) {
        super(_modsList);
        ((JList)component).setFixedCellWidth((Mod.ICON_WIDTH*5)/3);
        ((JList)component).setFixedCellHeight(2*Mod.ICON_HEIGHT);
        ((JList)component).setVisibleRowCount(-1);
    }

    @Override
    public void cellRendererExtension( JLabel label, Mod mod) {
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setVerticalAlignment(JLabel.TOP);
    }
}
