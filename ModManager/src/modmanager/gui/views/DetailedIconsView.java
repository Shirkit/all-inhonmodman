/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modmanager.gui.views;

import modmanager.business.Mod;
import java.util.ArrayList;
import javax.swing.JLabel;

/**
 * Detailed Icons view - like the normal Icons view, but with more text
 * @author Gcommer
 */
public class DetailedIconsView extends IconsView {
    public DetailedIconsView(ArrayList<Mod> _modsList) {
        super(_modsList);
    }

    @Override
    public void cellRendererExtension( JLabel label, Mod mod) {
        label.setText("<HTML>"+mod.getName()+"<BR><FONT COLOR=\"#777777\">"+mod.getAuthor()+"<BR>"+mod.getVersion()+"</FONT></HTML>");
    }
}
