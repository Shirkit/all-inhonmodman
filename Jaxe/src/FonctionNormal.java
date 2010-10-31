/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import jaxe.elements.JEStyle;
import jaxe.elements.JETexte;

import org.w3c.dom.Node;


public class FonctionNormal implements Fonction {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(FonctionNormal.class);

    public boolean appliquer(final JaxeDocument doc, final int start, final int end) {
        JaxeElement firstel = doc.rootJE.elementA(start);
        JaxeElement p1 = firstel;
        if (p1 instanceof JEStyle || p1 instanceof JETexte)
            p1 = p1.getParent();
        final JaxeElement lastel = doc.rootJE.elementA(end - 1);
        JaxeElement p2 = lastel;
        if (p2 instanceof JEStyle || p2 instanceof JETexte)
            p2 = p2.getParent();
        if (p1 != p2)
            return true;
        
        doc.textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("style.normal"), false);
        try {
            if (firstel instanceof JEStyle && firstel.debut.getOffset() < start)
                firstel = firstel.couper(doc.createPosition(start));
            JaxeElement nextje = firstel;
            while (nextje != null) {
                if (nextje.debut.getOffset() >= end)
                    break;
                if (nextje instanceof JEStyle) {
                    if (nextje.fin.getOffset() >= end)
                        nextje.couper(doc.createPosition(end));
                    final int finje = nextje.fin.getOffset();
                    tonormal((JEStyle)nextje);
                    nextje = doc.elementA(finje + 1);
                } else {
                    final Node nextnode = nextje.noeud.getNextSibling();
                    if (nextnode == null)
                        nextje = null;
                    else
                        nextje = doc.getElementForNode(nextnode);
                }
            }
        } catch (final BadLocationException ex) {
            LOG.error("appliquer(JaxeDocument, int, int) - BadLocationException", ex);
        }
        doc.textPane.finEditionSpeciale();
        doc.textPane.select(start, end);
        return true;
    }
    
    public static void tonormal(final JEStyle je) throws BadLocationException {
        final String texte = je.getText();
        final int jedebut = je.debut.getOffset();
        final JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, je, true);
        jedit.doit();
        ajoutNouveauJETexte(je.doc, je.doc.createPosition(jedebut), texte);
    }
    
    private static void ajoutNouveauJETexte(final JaxeDocument doc, final Position debut,
            final String texte) {
        
        final JETexte newje = JETexte.nouveau(doc, debut, null, texte);
        final JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, newje, true);
        jedit.doit();
    }
    
}
