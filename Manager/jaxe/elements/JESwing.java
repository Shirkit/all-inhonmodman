/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import jaxe.DialogueAttributs;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class JESwing extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JESwing.class);

    javax.swing.text.Element elSwing;
    
    public JESwing(final JaxeDocument doc, final Element elDOM, final javax.swing.text.Element elSwing) {
        this.doc = doc;
        this.elSwing = elSwing;
        try {
            debut = doc.createPosition(elSwing.getStartOffset());
            fin = doc.createPosition(elSwing.getEndOffset()-1);
        } catch (final BadLocationException ex) {
            LOG.error("JESwing(JaxeDocument, Element, javax.swing.text.Element)", ex);
        }
        noeud = elDOM;
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        return null;
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
        final Element el = (Element)noeud;

        final ArrayList<Element> latt = doc.cfg.listeAttributs(refElement);
        if (latt != null && latt.size() > 0) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                el.getTagName(), refElement, el);
            if (dlg.afficher()) {
                dlg.enregistrerReponses();
                majAffichage();
            }
            dlg.dispose();
        }
    }
}
