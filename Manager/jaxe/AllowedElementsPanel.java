/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import jaxe.elements.JESwing;
import jaxe.elements.JETexte;
import jaxe.ImageKeeper;

import org.w3c.dom.Element;

/**
 * Creates a Panel that shows a Button-List of all allowed Elements
 * @author tasche
 */
public class AllowedElementsPanel  extends JPanel  implements EcouteurMAJ, CaretListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(AllowedElementsPanel.class);

    /** The JaxeDocument for this Panel */
    JaxeDocument _doc;
    private JaxeElement _parent;
    private ArrayList<Element> _listeEnfants; // réf des éléments affichés (sauf parent)
    private ArrayList<Element> _listeValide; // réf des éléments insérables (sauf parent)

    /**
     * Creates the JPanel
     * @param doc the Document for this Panel
     */
    public AllowedElementsPanel(final JaxeDocument doc) {
        _doc = doc;
        _parent = null;
        miseAJour();
    }

    /**
     * Updates the Panel
     * @see jaxe.EcouteurMAJ#miseAJour()
     */
    public void miseAJour() {
        if (_doc.cfg == null)
            return;
        
        final int pos = _doc.textPane.getSelectionStart();
        JaxeElement jeParent = null;
        if (_doc.rootJE != null)
            jeParent = _doc.rootJE.elementA(pos);
        if (jeParent != null) {
            if ((jeParent.debut.getOffset() == pos && !(jeParent instanceof JESwing)) ||
                    jeParent instanceof JETexte)
                jeParent = jeParent.getParent();
        }
        
        final ArrayList<Element> nouvelleListeEnfants = new ArrayList<Element>();
        final ArrayList<Element> nouvelleListeValide = new ArrayList<Element>();
        ArrayList<Element> autorisees = null;
        Config conf = null;
        Element refElement = null;
        if (jeParent != null && jeParent.noeud instanceof Element) {
            conf = _doc.cfg.getElementConf((Element)jeParent.noeud);
            if (conf != null)
                refElement = jeParent.refElement;
            if (refElement != null)
                autorisees = conf.listeSousElements(refElement);
        } else if (_doc.rootJE == null) {
            conf = _doc.cfg;
            autorisees = conf.listeElementsRacines();
        }
        if (autorisees != null) {
            final int debutSelection = _doc.textPane.getSelectionStart();
            final int finSelection = _doc.textPane.getSelectionEnd();
            for (final Element ref : autorisees) {
                nouvelleListeEnfants.add(ref);
                if (conf == null || jeParent == null || conf.insertionPossible(jeParent, debutSelection, finSelection, ref))
                    nouvelleListeValide.add(ref);
            }
        }
        
        if (jeParent != _parent || !nouvelleListeValide.equals(_listeValide) || !nouvelleListeEnfants.equals(_listeEnfants)) {
            _parent = jeParent;
            _listeEnfants = nouvelleListeEnfants;
            _listeValide = nouvelleListeValide;
            
            this.removeAll();
            this.setLayout(new BorderLayout());
            
            final JPanel panelBoutons = new JPanel();
            panelBoutons.setLayout(new BoxLayout(panelBoutons, BoxLayout.PAGE_AXIS));
            
            if (conf != null && refElement != null) {
                final JPanel panelParent = new JPanel();
                panelParent.setLayout(new BoxLayout(panelParent, BoxLayout.LINE_AXIS));
                final JButton baide = new JButton(new ActionAide(refElement));
                if (System.getProperty("os.name").startsWith("Mac OS")) {
                    baide.setText("?");
                    if ("10.5".compareTo(System.getProperty("os.version")) <= 0)
                        baide.putClientProperty("JButton.buttonType", "help");
                    else
                        baide.putClientProperty("JButton.buttonType", "toolbar");
                } else {
                    baide.setIcon(new ImageIcon(ImageKeeper.loadImage("images/aide.png")));
                    baide.setMargin(new Insets(0, 0, 0, 0));
                    baide.setBorderPainted(false);
                    baide.setContentAreaFilled(false);
                }
                String documentation = _doc.cfg.documentation(refElement);
                if (documentation != null) {
                    documentation = _doc.cfg.formatageDoc(documentation);
                    baide.setToolTipText(documentation);
                }
                panelParent.add(baide);
                final JLabel lab = new JLabel(conf.titreMenu(_doc.cfg.nomElement(refElement)));
                lab.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
                final Dimension bDim = lab.getPreferredSize();
                bDim.width = Short.MAX_VALUE;
                lab.setMaximumSize(bDim);
                panelParent.add(lab);
                panelBoutons.add(panelParent);
                final JSeparator separateur = new JSeparator();
                separateur.setMaximumSize(new Dimension(Short.MAX_VALUE, 10));
                panelBoutons.add(separateur);
            }
            
            String expr = null;
            if (!_listeValide.equals(_listeEnfants)) {
                expr = conf.expressionReguliere(refElement);
                if (expr != null && expr.length() > 100) {
                    int p = 0;
                    for (int i=0; i<expr.length(); i++) {
                        if (i-p > 90 && (expr.charAt(i) == ' ' || expr.charAt(i) == '|')) {
                            expr = expr.substring(0, i) + "<br>" + expr.substring(i);
                            p = i;
                        }
                    }
                }
            }
            for (final Element ref : _listeEnfants) {
                final JPanel panelElement = new JPanel();
                panelElement.setLayout(new BoxLayout(panelElement, BoxLayout.LINE_AXIS));
                if (conf != null) {
                    final JButton baide = new JButton(new ActionAide(ref));
                    if (System.getProperty("os.name").startsWith("Mac OS")) {
                        baide.setText("?");
                        if ("10.5".compareTo(System.getProperty("os.version")) <= 0)
                            baide.putClientProperty("JButton.buttonType", "help");
                        else
                            baide.putClientProperty("JButton.buttonType", "toolbar");
                    } else {
                        baide.setIcon(new ImageIcon(ImageKeeper.loadImage("images/aide.png")));
                        baide.setMargin(new Insets(0, 0, 0, 0));
                        baide.setBorderPainted(false);
                        baide.setContentAreaFilled(false);
                    }
                    String documentation = _doc.cfg.documentation(ref);
                    if (documentation != null) {
                        documentation = _doc.cfg.formatageDoc(documentation);
                        baide.setToolTipText(documentation);
                    }
                    panelElement.add(baide);
                }
                final String nom = _doc.cfg.nomElement(ref);
                final String titre = _doc.cfg.titreMenu(nom);
                final ActionInsertionBalise action = new ActionInsertionBalise(_doc, titre, ref, nom, "element");
                final JButton bInsertion = new JButton(action);
                final Dimension bDim = bInsertion.getPreferredSize();
                bInsertion.setMaximumSize(new Dimension(Short.MAX_VALUE, bDim.height));
                final boolean inserable = _listeValide.contains(ref);
                bInsertion.setEnabled(inserable);
                if (!inserable) {
                    final String infos = "<html><body><p>" +
                        JaxeResourceBundle.getRB().getString("insertion.Expression") +
                        "</p><p>" + expr + "</p></body></html>";
                    bInsertion.setToolTipText(infos);
                }
                panelElement.add(bInsertion);
                panelBoutons.add(panelElement);
            }
            
            panelBoutons.add(Box.createVerticalGlue());
            
            final JScrollPane scroll = new JScrollPane(panelBoutons);
            this.add(scroll, BorderLayout.CENTER);
    
            validate();
        }
    }

    /**
     * If the Carret was moved, update the component
     * @see javax.swing.event.CaretListener#caretUpdate(CaretEvent)
     */
    public void caretUpdate(final CaretEvent e) {
        if (!_doc.textPane.getIgnorerEdition())
            miseAJour();
    }
    
    class ActionAide extends AbstractAction {
        Element refElement;
        ActionAide(final Element refElement) {
            super();
            this.refElement = refElement;
        }
        public void actionPerformed(final ActionEvent e) {
            final DialogueAideElement dlg = new DialogueAideElement(refElement, _doc.cfg.getRefConf(refElement),
                (JFrame)_doc.textPane.getTopLevelAncestor());
            dlg.setVisible(true);
        }
    }
}
