/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.Cursor;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.Position;

import jaxe.ImageKeeper;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Representation-element for processing instructions 
 */
public class JESauf extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JESauf.class);

    boolean mettreajour = false;
    String target;

    public JESauf() {
    }

    public JESauf(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    public void setTarget(final String target) {
        this.target = target;
    }
    
    /**
     * Adds a flag with nodecontent as tooltip for a processing instruction
     */
    @Override
    public void init(final Position pos, final Node noeud) {
        final JLabel l = new JLabel(new ImageIcon(ImageKeeper.loadImage("images/flag.png")));
        l.setCursor(Cursor.getDefaultCursor());
        final ProcessingInstruction p = (ProcessingInstruction) noeud;
        target = p.getTarget();
        l.setToolTipText(target + ": " + p.getData());
        insertComponent(pos, l);
    }

    @Override
    public Node nouvelElement(final Element refElement) {
        final Node newel = nouvelleInstructionDOM(doc, target);
        if (newel == null)
            return null;
        mettreajour = true;
        return newel;
    }
}
