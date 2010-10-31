/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.text.Position;

import jaxe.Balise;
import jaxe.DialogueAttributs;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import jaxe.JaxeResourceBundle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Elément vide (est affiché comme un bouton)
 * Type d'élément Jaxe: 'vide'
 */
public class JEVide extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEVide.class);

    static String newline = "\n";
    Balise lstart = null;
    ArrayList<String> attributsTitre = null;

    public JEVide(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    /**
     * Renvoit le titre qui sera affiché pour les dialogues sur l'élément :
     * nom de l'élément ou titre, en fonction des options d'affichage.
     */
    public String titreElement() {
        if (refElement != null)
            return(doc.cfg.titreElement(refElement));
        else if (noeud != null)
            return(noeud.getNodeName());
        else if (refElement != null)
            return(doc.cfg.nomElement(refElement));
        else
            return(null);
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
        final Element el = (Element)noeud;
        if (refElement != null)
            attributsTitre = doc.cfg.getParametresElement(refElement).get("titreAtt");
        lstart = new Balise(this, false, Balise.VIDE);
        final String ns = noeud.getNamespaceURI();
        int ensCouleur;
        if (ns == null)
            ensCouleur = 0;
        else
            ensCouleur = doc.cfg.numeroEspace(ns);
        if (ensCouleur == -1)
            // espace non géré
            ensCouleur = 0;
        lstart.setEnsembleCouleurs(ensCouleur);
        insertComponent(pos, lstart);
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        this.refElement = refElement;
        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null) return null;
        if (testAffichageDialogue()) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                JaxeResourceBundle.getRB().getString("zone.NouvelleBalise") + " " + titreElement(), refElement, newel);
            if (!dlg.afficher())
                return null;
            try {
                dlg.enregistrerReponses();
            } catch (final Exception ex) {
                LOG.error("nouvelElement(Element)", ex);
                return null;
            }
        }
        
        return newel;
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
        final Element el = (Element)noeud;

        final ArrayList<Element> latt = doc.cfg.listeAttributs(refElement);
        if (latt != null && latt.size() > 0) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                titreElement(), refElement, el);
            if (dlg.afficher()) {
                dlg.enregistrerReponses();
                majAffichage();
            }
            dlg.dispose();
        }
    }
    
    @Override
    public void majAffichage() {
        doc.imageChanged(lstart);
        lstart.majAffichage();
    }
}
