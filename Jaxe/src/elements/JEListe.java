/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import jaxe.Balise;
import jaxe.JaxeEditEvent;
import jaxe.JaxeElement;
import jaxe.JaxeDocument;
import jaxe.JaxeResourceBundle;
import jaxe.JaxeUndoableEdit;
import jaxe.JEFactory;
import jaxe.Preferences;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Liste d'éléments JEItem, à points ou numérotée.
 * Type d'élément Jaxe: 'liste'
 * paramètre: typeListe: POINTS | NUMEROS
 */
public class JEListe extends JEZone implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEListe.class);

    public int typeListe;
    public static final int POINTS = 1;
    public static final int NUMEROS = 2;

    public JEListe(final JaxeDocument doc) {
        super(doc);
    }

    @Override
    public void init(final Position pos, final Node noeud) {
        final Element el = (Element)noeud;
        
        if (refElement != null) {
            attributsTitre = doc.cfg.getParametresElement(refElement).get("titreAtt");
            final String param = doc.cfg.valeurParametreElement(refElement, "typeListe", null);
            if ("NUMEROS".equals(param))
                typeListe = NUMEROS;
            else
                typeListe = POINTS;
        }
        
        final int offsetdebut = pos.getOffset();
        
        final ArrayList<Element> enfants = doc.cfg.listeSousElements(refElement);
        if (enfants.size() == 1)
            lstart = new BoutonListe(this, Balise.DEBUT);
        else
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
        Position newpos = insertComponent(pos, lstart);
        
        Style s = null;
        final Properties prefs = Preferences.getPref();
        if (prefs == null || !"true".equals(prefs.getProperty("consIndent"))) {
            s = doc.textPane.addStyle(null, null);
            StyleConstants.setLeftIndent(s, (float)20.0*(indentations()+1));
            doc.setParagraphAttributes(offsetdebut, 1, s, false);
        }
        
        creerEnfants(newpos);
        
        if (enfants.size() == 1)
            lend = new BoutonListe(this, Balise.FIN);
        else
            lend = new Balise(this, false, Balise.FIN);
        lend.setEnsembleCouleurs(ensCouleur);
        newpos = insertComponent(newpos, lend);

        if (prefs == null || !"true".equals(prefs.getProperty("consIndent"))) {
            StyleConstants.setLeftIndent(s, (float)20.0*indentations());
            doc.setParagraphAttributes(offsetdebut, 1, s, false);
            doc.setParagraphAttributes(newpos.getOffset()-1, 1, s, false);
        }
    }
    
    @Override
    public boolean avecIndentation() {
        return true;
    }
    
    @Override
    public boolean avecSautsDeLigne() {
        return (true);
    }
    
    /**
     * Bouton pour les listes, comme Balise avec un bouton '+' en plus.
     */
    public class BoutonListe extends Balise {

        JButton bajitem;
        public BoutonListe(final JaxeElement je, final int typeBalise) {
            super(je, false, typeBalise);
            bajitem = new JButton("+");
            bajitem.addActionListener(JEListe.this);
            bajitem.setActionCommand("ajitem");
            petitBouton(bajitem);
            add(bajitem);
        }
        @Override
        public Dimension getPreferredSize() {
            final Dimension d = super.getPreferredSize();
            d.width += bajitem.getMinimumSize().width;
            return d;
        }
    }
    
    /**
     * Réduit la taille d'un bouton pour qu'il tienne dans une barre d'outils
     */
    private void petitBouton(final JButton b) {
        b.setFont(b.getFont().deriveFont((float) 9));
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            if ("10.5".compareTo(System.getProperty("os.version")) <= 0) {
                b.putClientProperty("JComponent.sizeVariant", "mini");
                b.putClientProperty("JButton.buttonType", "square");
            } else
                b.putClientProperty("JButton.buttonType", "toolbar");
        } else
            b.setMargin(new java.awt.Insets(1, 2, 1, 2));
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("ajitem".equals(cmd))
            ajouterItem();
    }
    
    protected void ajouterItem() {
        final ArrayList<Element> enfants = doc.cfg.listeSousElements(refElement);
        if (enfants.size() != 1) {
            LOG.error("ajouterItem() - ajouterItem: erreur: liste avec plus d'un élément enfant ?!?");
            return;
        }
        final Element itemref = enfants.get(0);
        final JaxeElement newje = JEFactory.createJE(doc, itemref, doc.cfg.nomElement(itemref), "element", (Element)null);
        
        Node newel = null;
        if (newje != null)
            newel = newje.nouvelElement(itemref);
        
        if (newel != null) { // null si annulation
            boolean inutileDajouterUnRetour = false;
            final Node texteavant = noeud.getLastChild();
            if (texteavant != null && texteavant.getNodeType() == Node.TEXT_NODE) {
                final String s = texteavant.getNodeValue();
                if (s != null && s.endsWith("\n\n"))
                    inutileDajouterUnRetour = true;
            }
            Position posInsertion;
            if (inutileDajouterUnRetour) {
                try {
                    posInsertion = doc.createPosition(fin.getOffset() - 1);
                } catch (final BadLocationException ble) {
                    LOG.error("ajouterItem() - BadLocationException", ble);
                    posInsertion = fin;
                }
            } else
                posInsertion = fin;
            doc.textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("annulation.Ajouter"), false);
            posInsertion = doc.firePrepareElementAddEvent(posInsertion);
            newje.inserer(posInsertion, newel);
            doc.textPane.addEdit(new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, newje));
            majValidite();
            newje.majValidite();
            doc.textPane.miseAJourArbre();
            
            if (!inutileDajouterUnRetour) {
                final JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, doc, "\n", fin.getOffset());
                jedit.doit();
            }
            doc.fireElementAddedEvent(new JaxeEditEvent(this, newje), posInsertion);
            doc.textPane.finEditionSpeciale();
        }
    }
}

