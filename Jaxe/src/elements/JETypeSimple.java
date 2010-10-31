/*
Jaxe - Editeur XML en Java

Copyright (C) 2006 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Position;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jaxe.Balise;
import jaxe.DialogueAttributs;
import jaxe.ImageKeeper;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import jaxe.JaxeResourceBundle;
import jaxe.VerifTypeSimple;


/**
 * Element Jaxe pour représenter un type simple de schéma WXS.
 * Utilise le schéma XML pour savoir de quel type il s'agit.
 * Tout n'est pas géré (facettes liste, union, limitations des caractères, ...).
 * Type d'élément Jaxe: 'typesimple'
 */
public class JETypeSimple extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JETypeSimple.class);

    static protected ImageIcon iconeAttributs = new ImageIcon(ImageKeeper.loadImage("images/attributs.gif", true));
    static protected ImageIcon iconeValide = new ImageIcon(ImageKeeper.loadImage("images/valide.gif", true));
    static protected ImageIcon iconeInvalide = new ImageIcon(ImageKeeper.loadImage("images/invalide.gif", true));
    protected PanelTypeSimple panel = null;
    protected VerifTypeSimple verif = null;

    public JETypeSimple(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    /**
     * Renvoit le titre qui sera affiché sur les balises de début et de fin :
     * nom de l'élément ou titre, en fonction des options d'affichage.
     */
    public String titreSurBalise() {
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
        
        if (doc.cfg != null) {
            if (refElement == null)
                LOG.error("init(Position, Node) - erreur: élément non défini dans le schéma: "
                        + el.getTagName());
            else
                verif = doc.cfg.getVerifTypeSimple(refElement);
        }
        
        // lecture de la valeur de l'élément
        final Node child = noeud.getFirstChild();
        String valeur;
        if (child != null)
            valeur = child.getNodeValue();
        else
            valeur = "";
        
        // création du composant Swing
        String baseType = null;
        ArrayList<String> enumeration = null;
        if (verif != null) {
            baseType = verif.getBaseType();
            enumeration = verif.getEnumeration();
        }
        panel = new PanelTypeSimple(titreSurBalise(), valeur, baseType, enumeration);
        final String ns = noeud.getNamespaceURI();
        int ensCouleur;
        if (ns == null)
            ensCouleur = 0;
        else
            ensCouleur = doc.cfg.numeroEspace(ns);
        if (ensCouleur == -1)
            // espace non géré
            ensCouleur = 0;
        panel.setEnsembleCouleurs(ensCouleur);
        
        // insertion du composant dans le texte (insertComponent est une méthode de JaxeElement)
        insertComponent(pos, panel);
    }
    
    // création d'un nouvel élément DOM
    @Override
    public Node nouvelElement(final Element refElement) {
        this.refElement = refElement;
        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null)
            return null;
        if (testAffichageDialogue()) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                JaxeResourceBundle.getRB().getString("zone.NouvelleBalise") + " " + titreSurBalise(), refElement, newel);
            if (!dlg.afficher())
                return null;
            dlg.enregistrerReponses();
        }
        final Node textnode = doc.DOMdoc.createTextNode("");
        newel.appendChild(textnode);
        return newel;
    }
    
    // mise à jour de l'affichage des attributs en fonction du DOM: rien à faire pour JEBooleen
    //public void majAffichage() { }
    
    public void changementTexte() {
        setValeur(panel.getValeur());
        if (verif != null)
            panel.setValidite(verif.estValide(panel.getValeur()));
        doc.setModif(true);
    }
    
    public void setValeur(final String valeur) {
        final Element el = (Element)noeud;
        final Node child = noeud.getFirstChild();
        if (child != null)
            child.setNodeValue(valeur);
        else {
            final Node textnode = doc.DOMdoc.createTextNode(valeur);
            el.appendChild(textnode);
        }
    }
    
    @Override
    public void selection(final boolean select) {
        super.selection(select);
        panel.selection(select);
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
        final Element el = (Element)noeud;

        final ArrayList<Element> latt = doc.cfg.listeAttributs(refElement);
        if (latt != null && latt.size() > 0) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                titreSurBalise(), refElement, el);
            if (dlg.afficher()) {
                dlg.enregistrerReponses();
                doc.textPane.miseAJourArbre();
                majAffichage();
            }
            dlg.dispose();
        }
    }
    
    @Override
    public void setFocus() {
        panel.setFocus();
    }
    
    class PanelTypeSimple extends JPanel implements ItemListener, DocumentListener, ActionListener {
        JPanel pp;
        JComponent comp;
        JLabel label;
        boolean valide = true;
        boolean selectionne = false;
        int noens = 0;
        Color[][] couleursButtons = null;
        JButton battr = null;
        JLabel labvalid;
        ArrayList<String> listeValeurs = null;
        
        public PanelTypeSimple(final String titre, final String valeur, final String type, final ArrayList<String> enumeration) {
            super();
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
            pp = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
            if (couleursButtons == null)
                couleursButtons = Balise.getCouleurs();
            if (Balise.getBord() != null)
                pp.setBorder(Balise.getBord());
            else
                pp.setBorder(BorderFactory.createLineBorder(Color.gray));
            if (aDesAttributs()) {
                battr = new JButton(iconeAttributs);
                if (System.getProperty("os.name").startsWith("Mac OS") &&
                        "10.5".compareTo(System.getProperty("os.version")) > 0)
                    battr.putClientProperty("JButton.buttonType", "toolbar");
                else
                    battr.setMargin( new java.awt.Insets(0, 0, 0, 0) );
                battr.addActionListener(this);
                battr.setActionCommand("attributs");
                pp.add(battr);
            }
            if ("boolean".equals(type)) {
                label = null;
                comp = new JCheckBox(titre);
                ((JCheckBox)comp).setOpaque(false);
                pp.add(comp);
                if (valeur != null && ("true".equals(valeur.trim()) || "1".equals(valeur.trim())))
                    ((JCheckBox)comp).setSelected(true);
                ((JCheckBox)comp).addItemListener(this);
            } else if (enumeration != null) {
                listeValeurs = new ArrayList<String>(enumeration);
                label = new JLabel(titre);
                pp.add(label);
                if (valeur != null && !listeValeurs.contains(valeur))
                    listeValeurs.add(valeur);
                final String[] titresValeurs = new String[listeValeurs.size()];
                for (int i=0; i<listeValeurs.size(); i++)
                    titresValeurs[i] = doc.cfg.titreValeurElement(refElement, listeValeurs.get(i));
                comp = new JComboBox(titresValeurs);
                ((JComboBox)comp).setOpaque(false);
                pp.add(comp);
                ((JComboBox)comp).setSelectedItem(doc.cfg.titreValeurElement(refElement, valeur));
                ((JComboBox)comp).addItemListener(this);
            } else {
                final ArrayList<String> listeValeursSuggerees = doc.cfg.listeValeursSuggereesElement(refElement);
                if (listeValeursSuggerees != null && listeValeursSuggerees.size() > 0) {
                    listeValeurs = new ArrayList<String>(listeValeursSuggerees);
                    label = new JLabel(titre);
                    pp.add(label);
                    if (valeur != null && !listeValeurs.contains(valeur))
                        listeValeurs.add(valeur);
                    final String[] titresValeurs = new String[listeValeurs.size()];
                    for (int i=0; i<listeValeurs.size(); i++)
                        titresValeurs[i] = doc.cfg.titreValeurElement(refElement, listeValeurs.get(i));
                    comp = new JComboBox(titresValeurs);
                    ((JComboBox)comp).setEditable(true);
                    ((JComboBox)comp).setOpaque(false);
                    pp.add(comp);
                    ((JComboBox)comp).setSelectedItem(doc.cfg.titreValeurElement(refElement, valeur));
                    ((JComboBox)comp).addItemListener(this);
                } else {
                    label = new JLabel(titre);
                    pp.add(label);
                    final int taille;
                    if ("string".equals(type) || "anyURI".equals(type))
                        taille = 40;
                    else
                        taille = 20;
                    comp = new JTextField(valeur, taille);
                    pp.add(comp);
                    ((JTextField)comp).getDocument().addDocumentListener(this);
                }
            }
            //comp.setNextFocusableComponent(doc.textPane);
            // see Java bug 4632352
            // trying a workaround that might not work :
            setFocusCycleRoot(true);
            setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
                @Override
                public Component getComponentAfter(final Container aContainer, final Component aComponent) {
                    if (aComponent == comp)
                        return(doc.textPane);
                    return(super.getComponentAfter(aContainer, aComponent));
                }
            });
            
            add(pp);
            valide = (verif == null || verif.estValide(getValeur()));
            if (doc.textPane.iconeValide) {
                final ImageIcon icone;
                if (valide)
                    icone = iconeValide;
                else
                    icone = iconeInvalide;
                labvalid = new JLabel(icone);
                pp.add(labvalid);
            }
        }
        
        public String getValeur() {
            if (comp instanceof JCheckBox) {
                if (((JCheckBox)comp).isSelected())
                    return "true";
                return "false";
            } else if (comp instanceof JComboBox) {
                final int index = ((JComboBox)comp).getSelectedIndex();
                if (index == -1)
                    return(null);
                else
                    return listeValeurs.get(index);
            }
            return ((JTextField)comp).getText();
        }
        
        public void setValidite(final boolean valide) {
            this.valide = valide;
            pp.setBackground(getBackground());
            if (doc.textPane.iconeValide) {
                final ImageIcon icone;
                if (valide)
                    icone = iconeValide;
                else
                    icone = iconeInvalide;
                labvalid.setIcon(icone);
                pp.repaint();
            }
        }
        
        public void selection(final boolean select) {
            selectionne = select;
            pp.setForeground(getForeground());
            pp.setBackground(getBackground());
            if (label != null)
                label.setForeground(getForeground());
            pp.repaint();
        }
        
        public void setEnsembleCouleurs(final int noens) {
            this.noens = noens - noens / couleursButtons.length
                    * couleursButtons.length;
            pp.setForeground(getForeground());
            pp.setBackground(getBackground());
            if (label != null)
                label.setForeground(getForeground());
        }
        
        @Override
        public Color getBackground() {
            if (couleursButtons == null)
                couleursButtons = Balise.getCouleurs();
            if (selectionne)
                return couleursButtons[noens][1];
            else if (valide)
                return couleursButtons[noens][0];
            else
                return couleursButtons[noens][2];
        }
        
        @Override
        public Color getForeground() {
            if (couleursButtons == null)
                couleursButtons = Balise.getCouleurs();
            if (selectionne)
                return couleursButtons[noens][0];
            return couleursButtons[noens][1];
        }
        
        public void setFocus() {
            comp.requestFocus();
        }
        
        public void actionPerformed(final ActionEvent e) {
            final String cmd = e.getActionCommand();
            if ("attributs".equals(cmd))
                afficherDialogue(doc.textPane.jframe);
        }
        
        public void itemStateChanged(final ItemEvent e) {
            changementTexte();
        }
        
        public void insertUpdate(final DocumentEvent e) {
            changementTexte();
        }
        
        public void removeUpdate(final DocumentEvent e) {
            changementTexte();
        }
        
        public void changedUpdate(final DocumentEvent e) {
            changementTexte();
        }
        
        private void changementTexte() {
            JETypeSimple.this.changementTexte();
        }
        
        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
        
        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }
    
}
