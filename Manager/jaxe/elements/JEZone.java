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
import javax.swing.text.SimpleAttributeSet;
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
 * Zone de texte. Le texte à l'intérieur est indenté.
 * Type d'élément Jaxe: 'zone'
 * paramètre: titreAtt: un attribut pouvant servir de titre
 * paramètre: style: comme dans JEStyle
 */
public class JEZone extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEZone.class);

    static String newline = "\n";
    /*JButton bstart = null;
    JButton bend = null;*/
    Balise lstart = null;
    Balise lend = null;
    ArrayList<String> attributsTitre = null;
    boolean valide = true;

    public JEZone(final JaxeDocument doc) {
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
        
        final int offsetdebut = pos.getOffset();
        
        lstart = new Balise(this, false, Balise.DEBUT);
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
        
        lend = new Balise(this, false, Balise.FIN);
        lend.setEnsembleCouleurs(ensCouleur);
        lend.setValidite(valide);
        newpos = insertComponent(newpos, lend);

        if (prefs == null || !"true".equals(prefs.getProperty("consIndent"))) {
            StyleConstants.setLeftIndent(s, (float)20.0*indentations());
            doc.setParagraphAttributes(offsetdebut, 1, s, false);
            doc.setParagraphAttributes(newpos.getOffset()-1, 1, s, false);
        }
        
        if (refElement != null && newpos.getOffset() - offsetdebut - 1 > 0) {
            final SimpleAttributeSet style = attStyle(null);
            if (style != null)
                doc.setCharacterAttributes(offsetdebut, newpos.getOffset() - offsetdebut - 1, style, false);
        }
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
                doc.textPane.miseAJourArbre();
                majAffichage();
            }
            dlg.dispose();
        }
    }
    
    @Override
    public void majAffichage() {
        lstart.setValidite(valide);
        lend.setValidite(valide);
        lstart.majAffichage();
        lend.majAffichage();
        doc.imageChanged(lstart);
        doc.imageChanged(lend);
    }
    
    @Override
    public void majValidite() {
        final boolean valide2 = doc.cfg.elementValide(this, false, null);
        if (valide2 != valide) {
            valide = valide2;
            majAffichage();
        }
    }
    
    /*class MyActionListener implements ActionListener {
        JEZone jei;
        JFrame jframe;
        public MyActionListener(JEZone obj, JFrame jframe) {
            super();
            jei = obj;
            this.jframe = jframe;
        }
        public void actionPerformed(ActionEvent e) {
            jei.afficherDialogue(jframe);
        }
    }*/

}
