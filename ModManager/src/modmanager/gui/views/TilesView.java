/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modmanager.gui.views;

import modmanager.business.Mod;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * A tiles view, with the text beneath the icon.
 * @author Gcommer
 */
public class TilesView extends IconsView {
    public TilesView(ArrayList<Mod> _modsList) {
        super(_modsList);

        JList comp = (JList) getComponent();
        comp.setFixedCellWidth((Mod.ICON_WIDTH*5)/3);
        comp.setFixedCellHeight(2*Mod.ICON_HEIGHT);
        comp.setVisibleRowCount(-1);
    }

    @Override
    public void cellRendererExtension( JLabel label, Mod mod) {
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setVerticalAlignment(JLabel.TOP);
    }
}
