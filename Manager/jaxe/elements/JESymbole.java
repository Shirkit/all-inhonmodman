/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import jaxe.JaxeDocument;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Fichier d'image comme Fichier, mais que l'on peut cr�er avec un dialogue affichant les images
 * se trouvant dans le dossier 'symboles' de Jaxe.
 * Type d'�l�ment Jaxe: 'symbole'
 */
public class JESymbole extends JEFichier {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JESymbole.class);

    public JESymbole(final JaxeDocument doc) {
        super(doc);
        alignementY = (float)0.70;
        // 70% du composant au-dessus de la base de la ligne
        // donc pas parfait (il faudrait se placer par rapport au milieu vertical d'un 'x'
        // et non par rapport � la base de la ligne pour obtenir un r�sulat parfait,
        // mais ce n'est pas possible avec Swing)
    }

    @Override
    public Node nouvelElement(final Element refElement) {
        
        srcAttr = doc.cfg.valeurParametreElement(refElement, "srcAtt", defaultSrcAttr);
        
        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null)
            return null;

        final DialogueSymbole dlg = new DialogueSymbole(doc.jframe, newel, srcAttr);
        if (!dlg.afficher())
            return null;
        final String sf = dlg.fichierChoisi();
        modifierAttributs(newel, refElement, sf);
        
        return newel;
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
        final Element el = (Element)noeud;

        final DialogueSymbole dlg = new DialogueSymbole(doc.jframe, el, srcAttr);
        if (!dlg.afficher())
            return;
        final String sf = dlg.fichierChoisi();
        modifierAttributs(el, refElement, sf);
        
        majAffichage();
    }
    
    private void modifierAttributs(final Element el, final Element refElement, final String sf) {
        try {
            el.setAttributeNS(doc.cfg.espaceAttribut(srcAttr), srcAttr, sf);
        } catch (final DOMException ex) {
            LOG.error("nouvelElement(Element) - DOMException", ex);
            return;
        }
    }
}
