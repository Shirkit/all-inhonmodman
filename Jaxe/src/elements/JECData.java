/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;

import jaxe.Balise;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Section CDATA
 */
public class JECData extends JaxeElement {
        
    private static final Logger LOG = Logger.getLogger(JECData.class);
    
    
    public JECData(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
        final Balise lstart = new Balise(this, "CDATA", false, Balise.DEBUT);
        final int offsetdebut = pos.getOffset();
        final Position newpos = insertComponent(pos, lstart);
        
        final String texte = noeud.getNodeValue();
        insertText(pos, texte);
        
        final Balise lend = new Balise(this, "CDATA", false, Balise.FIN);
        insertComponent(newpos, lend);
        if (newpos.getOffset() - offsetdebut - 1 > 0) {
            final SimpleAttributeSet style = attStyle(null);
            if (style != null)
                doc.setCharacterAttributes(offsetdebut, newpos.getOffset() - offsetdebut - 1, style, false);
        }
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        return(doc.DOMdoc.createCDATASection(""));
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
    
}
