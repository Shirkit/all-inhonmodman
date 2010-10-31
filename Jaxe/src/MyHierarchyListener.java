/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

// swing bug fix (idea from Java Bug Parade)

package jaxe;

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JTextPane;

public class MyHierarchyListener implements HierarchyListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MyHierarchyListener.class);

    private Container oldParent;
    Component comp;
    JTextPane textPane;
    
    public MyHierarchyListener(final Component comp, final JTextPane textPane) {
        this.comp = comp;
        this.textPane = textPane;
    }
    
    public void hierarchyChanged(final HierarchyEvent e) {
        final Container parent = comp.getParent();

        if (parent != oldParent) {
            if (oldParent != null) {
                textPane.remove(oldParent);
            }
            oldParent = parent;
        }
    }
}
