/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.Color;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import jaxe.Balise;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Commentaire XML
 */
public class JECommentaire extends JaxeElement {
        
    private static final Logger LOG = Logger.getLogger(JECommentaire.class);
    
    
    public JECommentaire(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
        final Balise lstart = new Balise(this, "(", false, Balise.DEBUT);
        final int offsetdebut = pos.getOffset();
        final Position newpos = insertComponent(pos, lstart);
        
        final String texte = noeud.getNodeValue();
        insertText(pos, texte);
        
        final Balise lend = new Balise(this, ")", false, Balise.FIN);
        insertComponent(newpos, lend);
        if (newpos.getOffset() - offsetdebut - 1 > 0) {
            final SimpleAttributeSet style = attStyle(null);
            if (style != null)
                doc.setCharacterAttributes(offsetdebut, newpos.getOffset() - offsetdebut - 1, style, false);
        }
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        return(doc.DOMdoc.createComment(""));
    }
    
    @Override
    public Position insPosition() {
        try {
            return doc.createPosition(debut.getOffset() + 1);
        } catch (final BadLocationException ex) {
            LOG.error("insPosition() - BadLocationException", ex);
            return null;
        }
    }
    
    @Override
    public SimpleAttributeSet attStyle(final SimpleAttributeSet attorig) {
        SimpleAttributeSet att = attorig;
        if (att == null)
            att = new SimpleAttributeSet();
        StyleConstants.setForeground(att, Color.darkGray);
        return(att);
    }
}
