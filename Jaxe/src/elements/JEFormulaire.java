/*
Jaxe - Editeur XML en Java

Copyright (C) 2006 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import jaxe.DialogueAttributs;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import jaxe.JaxeResourceBundle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Affichage récursif des éléments d'une séquence sous forme d'un formulaire.
 * Type d'élément Jaxe: 'formulaire'.
 */
public class JEFormulaire extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEFormulaire.class);
    
    private AffichageFormulaire affichage;

    public JEFormulaire(final JaxeDocument doc) {
        this.doc = doc;
        affichage = null;
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
        final Element el = (Element)noeud;
        if (affichage == null) // peut être != null en cas de suppression et d'annulation
            affichage = new AffichageFormulaire(refElement, el, null, doc);
        final JPanel panel = affichage.getPanel();
        insertComponent(pos, panel);
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null)
            return null;
        if (testAffichageDialogue()) {
            final String nombalise = doc.cfg.nomElement(refElement);
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                JaxeResourceBundle.getRB().getString("zone.NouvelleBalise") + " " + nombalise, refElement, newel);
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
    public void majAffichage() {
        affichage.majAffichage();
    }
    
    @Override
    public boolean avecSautsDeLigne() {
        return (true);
    }
    
    @Override
    public Point getPointEnfant(final Element el) {
        final Point pt = affichage.getPointEnfant(el);
        if (pt == null)
            return(null);
        final int placeCurseur = debut.getOffset();
        try {
            final Rectangle r = doc.textPane.modelToView(placeCurseur);
            final Point ptDebut = new Point(r.x + pt.x, r.y + pt.y);
            return(ptDebut);
        } catch (final BadLocationException ex) {
            LOG.error("JEFormulaire.getPointEnfant", ex);
            return(null);
        }
    }
    
    @Override
    public void selection(final boolean select) {
        affichage.selection(select);
    }
}
