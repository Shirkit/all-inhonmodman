/*
Jaxe - Editeur XML en Java

Copyright (C) 2006 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import jaxe.Balise;
import jaxe.Config;
import jaxe.DialogueAideElement;
import jaxe.DialogueAttributs;
import jaxe.ImageKeeper;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import jaxe.VerifTypeSimple;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Affichage pour JEFormulaire (affichage d'un élément sous forme d'un formulaire).
 * Attention, le nombre de descendants possibles ne doit pas être infini !
 */
public class AffichageFormulaire implements DocumentListener, ItemListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(AffichageFormulaire.class);
    
    private static final Color couleurErreur = new Color(200, 0, 0);
    private static final Color couleurObligatoire = new Color(150, 0, 0);
    private static final Color couleurFacultatif = new Color(0, 100, 0);
    
    private static final ImageIcon iconeAttributs = new ImageIcon(ImageKeeper.loadImage("images/attributs.gif", true));
    private static final int profondeurMax = 10;
    
    final private Element refNoeud; // référence de l'élément ou de l'attribut
    private Node noeud; // élément ou attribut affiché (null si c'est un nouveau)
    final private boolean attribut; // true s'il s'agit d'un attribut
    final private AffichageFormulaire affParent; // affichage de l'élément parent (obligatoire pour un attribut)
    final private JaxeDocument doc;
    final private Config cfg;
    private ArrayList<Node> enfants;
    private JComponent comp = null;
    private final VerifTypeSimple verif;
    private final ArrayList<Element> refEnfantsPossibles;
    private final ArrayList<Element> refAttributsPossibles;
    private JLabel labelTitre = null;
    private JPanel panelElement = null;
    private JPanel panelEnfants = null;
    private JPanel panelGauche = null;
    private JPanel panelDroite = null;
    private int profondeur;
    private ArrayList<AffichageFormulaire> affEnfants = null;
    private ArrayList<String> listeValeurs = null;
    
    
    public AffichageFormulaire(final Element refElement, final Element el, final AffichageFormulaire affParent,
            final JaxeDocument doc) {
        this(refElement, el, affParent, doc, false);
    }
    
    public AffichageFormulaire(final Element refNoeud, final Node noeud, final AffichageFormulaire affParent,
            final JaxeDocument doc, final boolean attribut) {
        this.refNoeud = refNoeud;
        this.noeud = noeud;
        this.affParent = affParent;
        this.doc = doc;
        this.cfg = doc.cfg;
        this.attribut = attribut;
        if (affParent == null)
            profondeur = 0;
        else
            profondeur = affParent.getProfondeur() + 1;
        if (!attribut) {
            refAttributsPossibles = cfg.listeAttributs(refNoeud);
            refEnfantsPossibles = cfg.listeSousElements(refNoeud);
        } else {
            refAttributsPossibles = null;
            refEnfantsPossibles = null;
        }
        
        lireEnfants();
        if (avecEnfants())
            verif = null;
        else
            verif = cfg.getVerifTypeSimple(refNoeud); // sera null si ce n'est pas un schéma W3C
    }
    
    /**
     * Lecture du tableau des attributs et des éléments enfants.
     */
    private void lireEnfants() {
        if (attribut || noeud == null) {
            enfants = null;
            return;
        }
        enfants = new ArrayList<Node>();
        final NamedNodeMap listeAttr = noeud.getAttributes();
        for (int i=0; i<listeAttr.getLength(); i++)
            enfants.add(listeAttr.item(i));
        final NodeList nl = ((Element)noeud).getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            final Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE)
                enfants.add(n);
        }
    }
    
    /**
     * Renvoit true si le nombre d'attributs et d'enfants possible est supérieur à 0.
     */
    private boolean avecEnfants() {
        return((refAttributsPossibles != null && refAttributsPossibles.size() > 0) ||
            (refEnfantsPossibles != null && refEnfantsPossibles.size() > 0));
    }
    
    /**
     * Renvoit le JPanel à afficher.
     */
    public JPanel getPanel() {
        return(getPanel(false));
    }
    
    /**
     * Renvoit le JPanel à afficher, avec en paramètre un booléen indiquant s'il s'agit du dernier
     * élément à afficher dans une liste d'enfants (auquel cas un bouton + peut être affiché)
     */
    private JPanel getPanel(final boolean dernier) {
        panelEnfants = new JPanel(new GridBagLayout());
        panelElement = new JPanel(new BorderLayout());
        if (!attribut && affParent != null && affParent.enfantsMultiples(refNoeud)) {
            final JPanel panelBoutons = new JPanel(new BorderLayout());
            if (dernier) {
                final JButton boutonPlus = new JButton("+");
                boutonPlus.setAction(new AbstractAction("+") {
                    public void actionPerformed(final ActionEvent e) {
                        affParent.ajouterAffichageEnfant(AffichageFormulaire.this);
                    }
                });
                panelBoutons.add(boutonPlus, BorderLayout.WEST);
            }
            final JButton boutonMoins = new JButton("-");
            boutonMoins.setAction(new AbstractAction("-") {
                public void actionPerformed(final ActionEvent e) {
                    affParent.retirerAffichageEnfant(AffichageFormulaire.this);
                }
            });
            panelBoutons.add(boutonMoins, BorderLayout.EAST);
            panelElement.add(panelBoutons, BorderLayout.EAST);
            panelElement.add(panelEnfants, BorderLayout.CENTER);
        } else
            panelElement.add(panelEnfants, BorderLayout.CENTER);
        if (affParent != null) {
            panelElement.add(getPanelTitre(), BorderLayout.NORTH);
            panelEnfants.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            panelElement.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
        majPanel(null);
        if (affParent == null) {
            panelEnfants.setFocusCycleRoot(true);
            panelElement = new JPanel(new BorderLayout());
            panelElement.setOpaque(false);
            panelElement.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            panelEnfants.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(getTitre()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            panelElement.add(panelEnfants, BorderLayout.CENTER);
        }
        return(panelElement);
    }
    
    /**
     * Panel avec le titre de l'élément, et les boutons d'aide et d'attributs de l'élément
     */
    private JPanel getPanelTitre() {
        final JButton baide = new JButton(new ActionAide(refNoeud));
        baide.setFont(baide.getFont().deriveFont((float)9));
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
        String documentation = getDocumentation();
        if (documentation != null)
            baide.setToolTipText(documentation);
        final JPanel panelTitre = new JPanel();
        panelTitre.add(baide);
        final JLabel labelTitre = new JLabel(getTitre());
        Color couleurTitre;
        if (affParent != null) {
            if (obligatoire())
                couleurTitre = couleurObligatoire;
            else
                couleurTitre = couleurFacultatif;
        } else
            couleurTitre = panelEnfants.getForeground();
        labelTitre.setForeground(couleurTitre);
        panelTitre.add(labelTitre);
        panelTitre.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        final JPanel panelNord = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        panelNord.add(panelTitre);
        return(panelNord);
    }
    
    /**
     * Met à jour l'affichage quand on ajoute un élément (donné en paramètre)
     */
    private void majPanel(final Element refAjout) {
        lireEnfants();
        if (profondeur > profondeurMax)
            return;
        panelEnfants.removeAll();
        affEnfants = new ArrayList<AffichageFormulaire>();
        final GridBagConstraints c = new GridBagConstraints();
        int pos = 0;
        if (!attribut) {
            for (final Element refAttributPossible : refAttributsPossibles) {
                Node nEnfant = null;
                if (enfants != null) {
                    for (Node enfanti : enfants) {
                        if (enfanti instanceof Attr) {
                            final String nomAtt = enfanti.getNodeName();
                            final String espaceAtt = enfanti.getNamespaceURI();
                            boolean match = nomAtt.equals(cfg.nomAttribut(refAttributPossible));
                            final String espace2 = cfg.espaceAttribut(refAttributPossible);
                            match = match && ((espaceAtt == null && espace2 == null) || (espaceAtt != null && espaceAtt.equals(espace2)));
                            if (match)
                                nEnfant = enfanti;
                        }
                    }
                }
                final AffichageFormulaire affEnfant = new AffichageFormulaire(refAttributPossible, nEnfant, this, doc, true);
                affEnfants.add(affEnfant);
                placerAffichage(affEnfant, panelEnfants, c, pos++, false);
            }
            for (final Element refEnfantPossible : refEnfantsPossibles) {
                boolean unEnfant = false;
                if (enfants != null) {
                    final int enfantsSize = enfants.size();
                    for (int i=0; i<enfantsSize; i++) {
                        final Node nEnfant = enfants.get(i);
                        if (nEnfant instanceof Element) {
                            final Element refEnfant;
                            refEnfant = cfg.getElementRef((Element)nEnfant);
                            if (refEnfant == refEnfantPossible) {
                                final AffichageFormulaire affEnfant = new AffichageFormulaire(refEnfant, nEnfant, this, doc, false);
                                affEnfants.add(affEnfant);
                                final Element refip1;
                                if (i+1 < enfantsSize) {
                                    final Node enfantip1 = enfants.get(i+1);
                                    if (enfantip1 instanceof Element)
                                        refip1 = cfg.getElementRef((Element)enfantip1);
                                    else
                                        refip1 = null;
                                } else
                                    refip1 = null;
                                final boolean dernier = (refAjout == null && refip1 != refEnfant);
                                placerAffichage(affEnfant, panelEnfants, c, pos++, dernier);
                                unEnfant = true;
                            }
                        }
                    }
                }
                if (!unEnfant || refEnfantPossible == refAjout) {
                    final AffichageFormulaire affEnfant = new AffichageFormulaire(refEnfantPossible, null, this, doc, false);
                    affEnfants.add(affEnfant);
                    placerAffichage(affEnfant, panelEnfants, c, pos++, true);
                }
            }
        }
    }
    
    /**
     * Place le JPanel d'un enfant dans le JPanel courant (elpane).
     */
    private void placerAffichage(final AffichageFormulaire affEnfant, final JPanel elpane, final GridBagConstraints c, final int pos, final boolean dernier) {
        if (affEnfant.avecEnfants()) {
            final JPanel panelEnfant = affEnfant.getPanel(dernier);
            c.weightx = 1;
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy = pos;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            elpane.add(panelEnfant, c);
        } else {
            final JPanel panelGaucheEnfant = affEnfant.getPanelGauche();
            c.weightx = 0;
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = pos;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.WEST;
            elpane.add(panelGaucheEnfant, c);
            
            final JPanel panelDroiteEnfant = affEnfant.getPanelDroite(dernier);
            c.weightx = 1;
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = pos;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            elpane.add(panelDroiteEnfant, c);
        }
    }
    
    /**
     * Renvoit le JPanel de gauche, avec le titre du champ.
     * Utilisé dans le cas où il n'y a pas d'enfants.
     */
    private JPanel getPanelGauche() {
        panelGauche = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JButton baide = new JButton(new ActionAide(refNoeud));
        baide.setFont(baide.getFont().deriveFont((float)9));
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
        String documentation = getDocumentation();
        if (documentation != null)
            baide.setToolTipText(documentation);
        if (documentation == null && attribut)
            baide.setEnabled(false);
        panelGauche.add(baide);
        labelTitre = new JLabel(getTitre());
        if (affParent != null) {
            if (obligatoire())
                labelTitre.setForeground(couleurObligatoire);
            else
                labelTitre.setForeground(couleurFacultatif);
        }
        panelGauche.add(labelTitre);
        return(panelGauche);
    }
    
    /**
     * Renvoit le JPanel de droite, avec la valeur modifiable.
     * Utilisé dans le cas où il n'y a pas d'enfants.
     */
    private JPanel getPanelDroite(final boolean dernier) {
        panelDroite = new JPanel(new BorderLayout());
        String valeur = "";
        if (noeud != null) {
            if (attribut)
                valeur = noeud.getNodeValue();
            else {
                final Node firstChild = ((Element)noeud).getFirstChild();
                if (firstChild != null && firstChild.getNodeType() == Node.TEXT_NODE)
                    valeur = firstChild.getNodeValue();
            }
        }
        if (attribut || cfg.contientDuTexte(refNoeud)) {
            String baseType = null;
            ArrayList<String> enumeration = null;
            if (verif != null) {
                baseType = verif.getBaseType();
                enumeration = verif.getEnumeration();
            }
            if ("boolean".equals(baseType)) {
                final String titre = getTitre();
                final JCheckBox cb = new JCheckBox(titre);
                if ("true".equals(valeur.trim()) || "1".equals(valeur.trim()))
                    cb.setSelected(true);
                cb.addItemListener(this);
                comp = cb;
            } else if (enumeration != null) {
                listeValeurs = new ArrayList<String>(enumeration);
                if (!listeValeurs.contains(valeur))
                    listeValeurs.add(valeur);
                if (!obligatoire() && !listeValeurs.contains(""))
                    listeValeurs.add("");
                final List<String> titresValeurs = new ArrayList<String>(listeValeurs.size());
                for (final String val : listeValeurs) {
                    final String titreValeur;
                    if (attribut) {
                        final Element refParent = affParent.getNoeudRef();
                        titreValeur = cfg.titreValeurAttribut(refParent, refNoeud, val);
                    } else
                        titreValeur = cfg.titreValeurElement(refNoeud, val);
                    titresValeurs.add(titreValeur);
                }
                final JComboBox cb = new JComboBox(titresValeurs.toArray());
                final String titreValeur;
                if (attribut) {
                    final Element refParent = affParent.getNoeudRef();
                    titreValeur = cfg.titreValeurAttribut(refParent, refNoeud, valeur);
                } else
                    titreValeur = cfg.titreValeurElement(refNoeud, valeur);
                cb.setSelectedItem(titreValeur);
                cb.addItemListener(this);
                comp = cb;
            } else {
                final ArrayList<String> listeValeursSuggerees;
                if (attribut) {
                    final Element refParent = affParent.getNoeudRef();
                    listeValeursSuggerees = doc.cfg.listeValeursSuggereesAttribut(refParent, refNoeud);
                } else
                    listeValeursSuggerees = doc.cfg.listeValeursSuggereesElement(refNoeud);
                if (listeValeursSuggerees != null && listeValeursSuggerees.size() > 0) {
                    listeValeurs = new ArrayList<String>(listeValeursSuggerees);
                    if (!listeValeurs.contains(valeur))
                        listeValeurs.add(valeur);
                    final List<String> titresValeurs = new ArrayList<String>(listeValeurs.size());
                    for (final String val : listeValeurs) {
                        final String titreValeur;
                        if (attribut) {
                            final Element refParent = affParent.getNoeudRef();
                            titreValeur = cfg.titreValeurAttribut(refParent, refNoeud, val);
                        } else
                            titreValeur = cfg.titreValeurElement(refNoeud, val);
                        titresValeurs.add(titreValeur);
                    }
                    final JComboBox cb = new JComboBox(titresValeurs.toArray());
                    cb.setEditable(true);
                    final String titreValeur;
                    if (attribut) {
                        final Element refParent = affParent.getNoeudRef();
                        titreValeur = cfg.titreValeurAttribut(refParent, refNoeud, valeur);
                    } else
                        titreValeur = cfg.titreValeurElement(refNoeud, valeur);
                    cb.setSelectedItem(titreValeur);
                    cb.addItemListener(this);
                    comp = cb;
                } else {
                    final JTextField tf = new JTextField(valeur);
                    tf.getDocument().addDocumentListener(this);
                    comp = tf;
                }
            }
            panelDroite.add(comp, BorderLayout.CENTER);
            if (verif != null && !"".equals(valeur))
                setValidite(verif.estValide(valeur));
        }
        if (!attribut && affParent != null && affParent.enfantsMultiples(refNoeud)) {
            final JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            if (dernier) {
                final JButton boutonPlus = new JButton("+");
                boutonPlus.setAction(new AbstractAction("+") {
                    public void actionPerformed(final ActionEvent e) {
                        affParent.ajouterAffichageEnfant(AffichageFormulaire.this);
                    }
                });
                panelBoutons.add(boutonPlus);
            }
            final JButton boutonMoins = new JButton("-");
            boutonMoins.setAction(new AbstractAction("-") {
                public void actionPerformed(final ActionEvent e) {
                    affParent.retirerAffichageEnfant(AffichageFormulaire.this);
                }
            });
            panelBoutons.add(boutonMoins);
            panelDroite.add(panelBoutons, BorderLayout.EAST);
        }
        panelDroite.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        return(panelDroite);
    }
    
    public void insertUpdate(final DocumentEvent e) {
        enregistrerChangement();
    }
    
    public void removeUpdate(final DocumentEvent e) {
        enregistrerChangement();
    }
    
    public void changedUpdate(final DocumentEvent e) {
        enregistrerChangement();
    }
    
    /**
     * Enregistre un changement dans le DOM.
     */
    private void enregistrerChangement() {
        String valeur = getValeur();
        if (valeur == null)
            valeur = "";
        doc.setModif(true);
        MyCompoundEdit cedit = null;
        if (noeud == null && !"".equals(valeur)) {
            cedit = new MyCompoundEdit();
            creerNoeud(cedit);
        } else if (noeud != null && "".equals(valeur) && affParent != null) {
            effacerNoeud(false, null);
            return;
        }
        if (!attribut) {
            final Element el = (Element)noeud;
            final Node firstChild = el.getFirstChild();
            if (firstChild == null) {
                final FormUndoableEdit fedit = new FormUndoableEdit(this, "", valeur);
                if (cedit != null)
                    cedit.addEdit(fedit);
                else
                    doc.textPane.addEdit(fedit);
                final Node textnode = el.getOwnerDocument().createTextNode(valeur);
                el.appendChild(textnode);
            } else if (firstChild.getNodeType() == Node.TEXT_NODE) {
                final FormUndoableEdit fedit = new FormUndoableEdit(this, firstChild.getNodeValue(), valeur);
                if (cedit != null)
                    cedit.addEdit(fedit);
                else
                    doc.textPane.addEdit(fedit);
                firstChild.setNodeValue(valeur);
            } else
                LOG.error("AffichageFormulaire.enregistrerChangement : pas de noeud texte pour enregistrer le champ");
        } else {
            final FormUndoableEdit fedit = new FormUndoableEdit(this, noeud.getNodeValue(), valeur);
            if (cedit != null)
                cedit.addEdit(fedit);
            else
                doc.textPane.addEdit(fedit);
            final Element elparent = (Element)affParent.getNoeud();
            final String nom = cfg.nomAttribut(refNoeud);
            final String espace = cfg.espaceAttribut(refNoeud);
            elparent.setAttributeNS(espace, nom, valeur);
        }
        if (cedit != null) {
            cedit.end();
            doc.textPane.addEdit(cedit);
        }
        if (verif != null)
            setValidite("".equals(valeur) || verif.estValide(valeur));
    }
    
    /**
     * Renvoit la valeur du composant d'édition
     */
    private String getValeur() {
        final String valeur;
        if (comp instanceof JTextField) {
            final javax.swing.text.Document document = ((JTextField)comp).getDocument();
            try {
                valeur = document.getText(0, document.getLength());
            } catch (final BadLocationException ex) {
                LOG.error("lecture du champ texte", ex);
                return(null);
            }
        } else if (comp instanceof JCheckBox) {
            valeur = Boolean.toString(((JCheckBox)comp).isSelected());
        } else if (comp instanceof JComboBox) {
            final JComboBox cb = (JComboBox)comp;
            final int index = cb.getSelectedIndex();
            if (index != -1)
                valeur = listeValeurs.get(cb.getSelectedIndex());
            else
                valeur = (String)cb.getSelectedItem();
        } else
            valeur = null;
        return(valeur);
    }
    
    /**
     * Change l'affichage du composant d'édition en fonction de la validité de sa valeur
     */
    private void setValidite(final boolean valide) {
        if (comp == null)
            LOG.error("AffichageFormulaire.setValidite : pas de champ ?");
        if (valide)
            comp.setForeground(Color.black);
        else
            comp.setForeground(couleurErreur);
    }
    
    /**
     * Renvoit le noeud DOM.
     */
    private Node getNoeud() {
        return(noeud);
    }
    
    /**
     * Renvoit la référence de l'élément dans le schéma.
     */
    private Element getNoeudRef() {
        return(refNoeud);
    }
    
    private String getTitre() {
        if (attribut) {
            final Element refParent = affParent.getNoeudRef();
            return(cfg.titreAttribut(refParent, refNoeud));
        } else
            return(cfg.titreElement(refNoeud));
    }
    
    private String getDocumentation() {
        String documentation;
        if (attribut) {
            final Element refParent = affParent.getNoeudRef();
            documentation = cfg.documentationAttribut(refParent, refNoeud);
        } else
            documentation = cfg.documentation(refNoeud);
        if (documentation != null)
            documentation = "<html><body>" + documentation.replaceAll("\n", "<br>") + "</body></html>";
        return(documentation);
    }
    
    /**
     * Renvoit la profondeur de l'affichage (utile pour éviter les dépassements de pile).
     */
    private int getProfondeur() {
        return(profondeur);
    }
    
    /**
     * Crée l'élément DOM correspondant à l'affichage, si nécessaire en créant l'élément DOM parent.
     */
    private void creerNoeud(final MyCompoundEdit cedit) {
        if (attribut) {
            Element elparent = (Element)affParent.getNoeud();
            if (elparent == null) {
                affParent.creerNoeud(cedit);
                elparent = (Element)affParent.getNoeud();
            }
            final String nom = cfg.nomAttribut(refNoeud);
            final String espace = cfg.espaceAttribut(refNoeud);
            elparent.setAttributeNS(espace, nom, "");
            noeud = elparent.getAttributeNodeNS(espace, nom);
            cedit.addEdit(new FormUndoableEdit(FormUndoableEdit.TypeEdition.AJOUTER, this));
        } else {
            noeud = JaxeElement.nouvelElementDOM(doc, refNoeud);
            Element elparent = (Element)affParent.getNoeud();
            if (elparent == null) {
                affParent.creerNoeud(cedit);
                elparent = (Element)affParent.getNoeud();
                elparent.appendChild( noeud.getOwnerDocument().createTextNode("\n") );
            }
            final Element suivant = affParent.trouverSuivant(refNoeud);
            final Node textnode = noeud.getOwnerDocument().createTextNode("\n");
            if (suivant == null) {
                elparent.appendChild(noeud);
                elparent.appendChild(textnode);
            } else {
                elparent.insertBefore(noeud, suivant);
                elparent.insertBefore(textnode, suivant);
            }
            cedit.addEdit(new FormUndoableEdit(FormUndoableEdit.TypeEdition.AJOUTER, this));
        }
        affParent.lireEnfants();
        doc.textPane.miseAJourArbre();
    }
    
    /**
     * Renvoie l'élément enfant se trouvant après la place où se trouverait l'enfant de référence ref.
     * (permet d'insérer un futur élément de référence ref par rapport à l'élément suivant)
     */
    private Element trouverSuivant(final Element ref) {
        final int ind = refEnfantsPossibles.indexOf(ref);
        if (ind == refEnfantsPossibles.size() - 1)
            return(null);
        if (enfants == null)
            return(null);
        for (final Node nEnfant : enfants) {
            if (nEnfant instanceof Element) {
                final Element refEnfant = cfg.getElementRef((Element)nEnfant);
                final int ind2 = refEnfantsPossibles.indexOf(refEnfant);
                if (ind2 > ind)
                    return((Element)nEnfant);
            }
        }
        return(null);
    }
    
    /**
     * Efface le noeud DOM, et son parent s'il devient vide.
     */
    private void effacerNoeud(final boolean annulation, final CompoundEdit cedit1) {
        CompoundEdit cedit = null;
        if (!annulation) {
            final FormUndoableEdit fedit = new FormUndoableEdit(FormUndoableEdit.TypeEdition.SUPPRIMER, this);
            if (cedit1 != null)
                cedit1.addEdit(fedit);
            else {
                cedit = new CompoundEdit();
                cedit.addEdit(fedit);
            }
        }
        final Element elparent = (Element)affParent.getNoeud();
        if (attribut) {
            final String nom = cfg.nomAttribut(refNoeud);
            final String espace = cfg.espaceAttribut(refNoeud);
            elparent.removeAttributeNS(espace, nom);
        } else {
            final Node textNode = noeud.getNextSibling();
            elparent.removeChild(noeud);
            if (textNode.getNodeType() == Node.TEXT_NODE)
                elparent.removeChild(textNode);
        }
        noeud = null;
        if (!annulation)
            affParent.testEffacementParent(cedit1 != null ? cedit1 : cedit);
        else
            lireEnfants();
        doc.textPane.miseAJourArbre();
        if (cedit1 == null && cedit != null) {
            cedit.end();
            doc.textPane.addEdit(cedit);
        }
    }
    
    /**
     * Efface l'élément s'il est vide.
     */
    private void testEffacementParent(final CompoundEdit cedit) {
        lireEnfants();
        if (enfants.size() == 0 && noeud != null && affParent != null)
            effacerNoeud(false, cedit);
    }
    
    /**
     * Renvoit true si l'élément peut avoir des enfants multiples avec la référence ref (sequence ou choice).
     */
    private boolean enfantsMultiples(final Element ref) {
        if (attribut)
            return(false);
        if (cfg.getSchema() == null)
            return(true);
        return(cfg.getSchema().enfantsMultiples(refNoeud, ref));
    }
    
    /**
     * Renvoit true si l'élément ou l'attribut est obligatoire
     */
    private boolean obligatoire() {
        if (attribut)
            return(cfg.estObligatoire(refNoeud));
        return(affParent.elementObligatoire(refNoeud));
    }
    
    /**
     * Renvoit true si l'enfant de référence ref est obligatoire.
     */
    private boolean elementObligatoire(final Element ref) {
        if (cfg.getSchema() == null)
            return(false);
        return(cfg.getSchema().elementObligatoire(refNoeud, ref));
    }
    
    /**
     * Mise à jour de l'affichage causée par le changement dans un sous-affichage.
     */
    private void ajouterAffichageEnfant(final AffichageFormulaire aff) {
        majPanel(aff.getNoeudRef());
    }
    
    /**
     * Retire un élément et son affichage ( bouton - )
     */
    private void retirerAffichageEnfant(final AffichageFormulaire aff) {
        if (aff.getNoeud() != null)
            aff.effacerNoeud(false, null);
        majPanel(null);
    }
    
    public void itemStateChanged(final ItemEvent e) {
        enregistrerChangement();
    }
    
    public Point getPointEnfant(final Element enfant) {
        if (noeud == enfant) {
            return(getPoint());
        } else if (affEnfants != null) {
            for (final AffichageFormulaire affEnfant : affEnfants) {
                final Point pt = affEnfant.getPointEnfant(enfant);
                if (pt != null) {
                    final Point monPoint = getPoint();
                    if (monPoint != null)
                        pt.translate(monPoint.x, monPoint.y);
                    final Point ptEnfants = panelEnfants.getLocation();
                    pt.translate(ptEnfants.x, ptEnfants.y);
                    return(pt);
                }
            }
        }
        return(null);
    }
    
    public Point getPoint() {
        if (panelElement != null)
            return(panelElement.getLocation());
        else if (panelGauche != null)
            return(panelGauche.getLocation());
        else
            return(null);
    }
    
    private AffichageFormulaire chercherAffichage(final Node n) {
        if (n == noeud)
            return(this);
        if (affEnfants == null)
            return(null);
        for (AffichageFormulaire aff : affEnfants) {
            final AffichageFormulaire a = aff.chercherAffichage(n);
            if (a != null)
                return(a);
        }
        return(null);
    }
    
    private JaxeDocument getJaxeDocument() {
        return(doc);
    }
    
    public void selection(final boolean select) {
        if (select)
            panelEnfants.setBackground(Balise.getCouleurs()[0][1]);
        else
            panelEnfants.setBackground(null);
    }
    
    public void majAffichage() {
        // à faire
    }
    
    
    class ActionAide extends AbstractAction {
        Element refNoeud;
        ActionAide(final Element refNoeud) {
            super();
            this.refNoeud = refNoeud;
        }
        public void actionPerformed(final ActionEvent e) {
            final DialogueAideElement dlg;
            if (attribut) {
                final Element refParent = affParent.getNoeudRef();
                dlg = new DialogueAideElement(refNoeud, refParent, cfg.getRefConf(refParent),
                    (JFrame)doc.textPane.getTopLevelAncestor());
            } else {
                dlg = new DialogueAideElement(refNoeud, cfg.getRefConf(refNoeud),
                    (JFrame)doc.textPane.getTopLevelAncestor());
            }
            dlg.setVisible(true);
        }
    }
    
    static class MyCompoundEdit extends CompoundEdit {
        public MyCompoundEdit() {
            super();
        }
        public boolean addEdit(UndoableEdit anEdit) {
            final boolean absorbed = super.addEdit(anEdit);
            if (!absorbed && !isInProgress() && anEdit instanceof FormUndoableEdit) {
                final UndoableEdit ledit = lastEdit();
                if (ledit != null && ledit instanceof FormUndoableEdit)
                    return(ledit.addEdit(anEdit));
            }
            return(absorbed);
        }
    }
    
    static class FormUndoableEdit extends AbstractUndoableEdit {
        public enum TypeEdition { AJOUTER, SUPPRIMER, MODIFIER };
        private TypeEdition ajsup;
        private String ancienTexte, nouveauTexte;
        private Node noeud, parent;
        private AffichageFormulaire premierAffichage;
        private Node suivant;
        
        public FormUndoableEdit(final TypeEdition ajsup, final AffichageFormulaire aff) {
            this.ajsup = ajsup;
            noeud = aff.getNoeud();
            if (noeud instanceof Attr)
                parent = ((Attr)noeud).getOwnerElement();
            else
                parent = noeud.getParentNode();
            premierAffichage = aff;
            while (premierAffichage.affParent != null)
                premierAffichage = premierAffichage.affParent;
            suivant = noeud.getNextSibling();
            if (suivant != null)
                suivant = suivant.getNextSibling();
        }
        public FormUndoableEdit(final AffichageFormulaire aff, final String ancienTexte, final String nouveauTexte) {
            ajsup = TypeEdition.MODIFIER;
            noeud = aff.getNoeud();
            if (noeud instanceof Attr)
                parent = ((Attr)noeud).getOwnerElement();
            else
                parent = noeud.getParentNode();
            premierAffichage = aff;
            while (premierAffichage.affParent != null)
                premierAffichage = premierAffichage.affParent;
            this.ancienTexte = ancienTexte;
            this.nouveauTexte = nouveauTexte;
            suivant = null;
            if (noeud instanceof Element) {
                suivant = noeud.getNextSibling();
                if (suivant != null)
                    suivant = suivant.getNextSibling();
            }
        }
        
        private void recreerElement() {
            final Node textnode = noeud.getOwnerDocument().createTextNode("\n");
            final Element elparent = (Element)parent;
            if (suivant == null) {
                elparent.appendChild(noeud);
                elparent.appendChild(textnode);
            } else {
                elparent.insertBefore(noeud, suivant);
                elparent.insertBefore(textnode, suivant);
            }
        }
        private void ajouter() {
            final AffichageFormulaire affP = premierAffichage.chercherAffichage(parent);
            if (noeud instanceof Element)
                recreerElement();
            else {
                final Element elparent = (Element)parent;
                elparent.setAttributeNodeNS((Attr)noeud);
            }
            affP.lireEnfants();
            affP.getJaxeDocument().textPane.miseAJourArbre();
            affP.majPanel(null);
        }
        private void supprimer() {
            final AffichageFormulaire aff = premierAffichage.chercherAffichage(noeud);
            final AffichageFormulaire affP = aff.affParent;
            suivant = noeud.getNextSibling();
            if (suivant != null)
                suivant = suivant.getNextSibling();
            aff.effacerNoeud(true, null);
            affP.majPanel(null);
        }
        private void changerTexte(final String s) {
            final AffichageFormulaire affP = premierAffichage.chercherAffichage(parent);
            if (noeud instanceof Element) {
                final AffichageFormulaire aff = affP.chercherAffichage(noeud);
                Node noeudTexte = noeud.getFirstChild();
                if (aff == null && !"".equals(s))
                    recreerElement();
                else if (noeudTexte != null)
                    noeudTexte.setNodeValue(s);
                else {
                    noeudTexte = noeud.getOwnerDocument().createTextNode(s);
                    noeud.appendChild(noeudTexte);
                }
                affP.majPanel(null);
            } else { // attribut
                final Element elparent = (Element)parent;
                if (!elparent.hasAttributeNS(noeud.getNamespaceURI(), noeud.getLocalName()))
                    elparent.setAttributeNodeNS((Attr)noeud);
                noeud.setNodeValue(s);
                affP.majPanel(null);
            }
        }
        private void refaireModifTexte() {
            changerTexte(nouveauTexte);
        }
        private void defaireModifTexte() {
            changerTexte(ancienTexte);
        }
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof FormUndoableEdit) {
                final FormUndoableEdit fedit = (FormUndoableEdit)anEdit;
                if (ajsup == TypeEdition.MODIFIER && noeud == fedit.noeud && fedit.ajsup == TypeEdition.MODIFIER) {
                    nouveauTexte = fedit.nouveauTexte;
                    fedit.die();
                    return(true);
                }
            }
            return(false);
        }
        public void undo() throws CannotUndoException {
            super.undo();
            if (ajsup == TypeEdition.AJOUTER)
                supprimer();
            else if (ajsup == TypeEdition.SUPPRIMER)
                ajouter();
            else
                defaireModifTexte();
        }
        public void redo() throws CannotUndoException {
            super.redo();
            if (ajsup == TypeEdition.AJOUTER)
                ajouter();
            else if (ajsup == TypeEdition.SUPPRIMER)
                supprimer();
            else
                refaireModifTexte();
        }
    }
    
}
