/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import jaxe.JaxeDocument;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Fichier d'image comme Fichier, mais que l'on peut créer avec un dialogue affichant les images
 * se trouvant dans le dossier 'symboles' de Jaxe.
 * Type d'élément Jaxe: 'symbole'
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
        // et non par rapport à la base de la ligne pour obtenir un résulat parfait,
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
