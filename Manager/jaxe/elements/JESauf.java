/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
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
