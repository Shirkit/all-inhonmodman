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
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import jaxe.Balise;
import jaxe.DialogueAttributs;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import jaxe.JaxeResourceBundle;
import jaxe.Preferences;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Zone de division. Les balises sont affichées comme des bandes prenant toute la largeur de la page,
 * et le texte à l'intérieur est indenté.
 * Type d'élément Jaxe: 'division'
 * paramètre: titreAtt: un attribut pouvant servir de titre
 */
public class JEDivision extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEDivision.class);

    static String newline = "\n";
    Balise lstart = null;
    Balise lend = null;
    boolean valide = true;

    public JEDivision(final JaxeDocument doc) {
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
        
        //debut = null;
        final int offsetdebut = pos.getOffset();
        //fin = null;
        //try {
            //Position newpos = doc.createPosition(pos.getOffset());
            
            lstart = new Balise(this, true, Balise.DEBUT);
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
            lstart.setValidite(valide);
            Position newpos = insertComponent(pos, lstart);
            //if (newpos.getOffset() == 0) // bug fix with insertString
            //    newpos = doc.createPosition(newpos.getOffset() + 1);
            
            /*
            // on insère un \n si on ne peut pas utiliser celui de l'élément suivant pour changer le style
            Node suivant = null;
            JaxeElement jesuivant = null;
            if (noeud.getNextSibling() != null && noeud.getNextSibling().getNodeType() == Node.TEXT_NODE) {
                suivant = noeud.getNextSibling();
                if (suivant.getNodeValue() != null && suivant.getNodeValue().startsWith(newline)) {
                    jesuivant = doc.getElementForNode(suivant);
                }
            }
            
            if (jesuivant == null) {
                if (suivant != null)
                    suivant.setNodeValue(suivant.getNodeValue().substring(newline.length()));
                doc.insertString(newpos.getOffset(), newline, null);
                newpos = doc.createPosition(newpos.getOffset()-newline.length());
            }
            
            Style s = doc.textPane.addStyle(null, null);
            StyleConstants.setLeftIndent(s, (float)20.0*indentations());
            doc.setParagraphAttributes(newpos.getOffset(), newline.length(), s, false);
            
            creerEnfants(newpos);
            
            //doc.insertString(newpos.getOffset(), newline, null);
            lend = new MonBouton(titreBend);
            lend.addMouseListener(new MyMouseListener(this, doc.jframe, false));
            insertComponent(newpos, lend);
            */
            
            /* au lieu du \n potentiellement ajouté, il vaut mieux:
             - insérer le premier bouton
             - le décaler vers la droite
             - insérer les enfants
             - ajouter le bouton de fin
             - décaler les 2 boutons vers la gauche
             
             Ca marche bien avec les bons \n après les balises de division, et le programme
             ne se plante pas s'il n'y a pas les \n (les indentations ne sont pas très jolies dans ce cas)
            */
            Style s = null;
            final Properties prefs = Preferences.getPref();
            // prefs peut être null dans le cas où JaxeTextPane est inclus
            // dans une autre application que Jaxe
            if (prefs == null || !"true".equals(prefs.getProperty("consIndent"))) {
                s = doc.textPane.addStyle(null, null);
                StyleConstants.setLeftIndent(s, (float)20.0*(indentations()+1));
                doc.setParagraphAttributes(offsetdebut, 1, s, false);
            }
            
            creerEnfants(newpos);
            
            lend = new Balise(this, true, Balise.FIN);
            lend.setEnsembleCouleurs(ensCouleur);
            lend.setValidite(valide);
            newpos = insertComponent(newpos, lend);

            if (prefs == null || !"true".equals(prefs.getProperty("consIndent"))) {
                StyleConstants.setLeftIndent(s, (float)20.0*indentations());
                doc.setParagraphAttributes(offsetdebut, 1, s, false);
                doc.setParagraphAttributes(newpos.getOffset()-1, 1, s, false);
            }
            
            //debut = doc.createPosition(offsetdebut);
            //fin = doc.createPosition(newpos.getOffset() - 1);

        //} catch (BadLocationException ex) {
        //    System.err.println("BadLocationException: " + ex.getMessage());
        //}
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        this.refElement = refElement;
        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null)
            return null;
        if (testAffichageDialogue()) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                JaxeResourceBundle.getRB().getString("zone.NouvelleBalise") + " " + titreElement(), refElement, newel);
            if (!dlg.afficher())
                return null;
            dlg.enregistrerReponses();
        }

        final Node textnode = doc.DOMdoc.createTextNode(newline+newline);
        newel.appendChild(textnode);
        
        return newel;
    }
    
    @Override
    public boolean avecIndentation() {
        return true;
    }
    
    @Override
    public boolean avecSautsDeLigne() {
        return (true);
    }
    
    @Override
    public Position insPosition() {
        try {
            return doc.createPosition(debut.getOffset() + 1 + newline.length());
        } catch (final BadLocationException ex) {
            LOG.error("insPosition() - BadLocationException", ex);
            return null;
        }
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
                doc.textPane.miseAJourArbre();
            }
            dlg.dispose();
        }
    }
    
    @Override
    public void majAffichage() {
        if (lstart != null) {
            lstart.setValidite(valide);
            lstart.majAffichage();
            doc.imageChanged(lstart);
        }
        if (lend != null) {
            lend.setValidite(valide);
            lend.majAffichage();
            doc.imageChanged(lend);
        }
    }
    
    @Override
    public void majValidite() {
        final boolean valide2 = doc.cfg.elementValide(this, false, null);
        if (valide2 != valide) {
            valide = valide2;
            majAffichage();
        }
    }
    
    @Override
    public void selection(final boolean select) {
        super.selection(select);
        if (lstart != null)
            lstart.selection(select);
        if (lend != null)
            lend.selection(select);
    }
}
