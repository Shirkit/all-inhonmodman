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
import javax.swing.text.Position;

import jaxe.Balise;
import jaxe.DialogueAttributs;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import jaxe.JaxeResourceBundle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * El�ment vide (est affich� comme un bouton)
 * Type d'�l�ment Jaxe: 'vide'
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
     * Renvoit le titre qui sera affich� pour les dialogues sur l'�l�ment :
     * nom de l'�l�ment ou titre, en fonction des options d'affichage.
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
            // espace non g�r�
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
