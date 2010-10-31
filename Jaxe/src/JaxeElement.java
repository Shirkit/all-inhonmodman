/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;


import jaxe.elements.JECommentaire;
import jaxe.elements.JECData;
import jaxe.elements.JEInconnu;
import jaxe.elements.JEStyle;
import jaxe.elements.JESwing;
import jaxe.elements.JETableTexte;
import jaxe.elements.JETexte;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;


/**
 * Elément Jaxe, représentant à la fois l'affichage graphique et l'arbre DOM
 * correspondant (noeud)
 */
public abstract class JaxeElement {
    private static final Pattern COLOR_PATTERN = Pattern.compile("^.*\\[(x[0-9a-fA-F]{2}|[0-9]{1,3}),(x[0-9a-fA-F]{2}|[0-9]{1,3}),(x[0-9a-fA-F]{2}|[0-9]{1,3})\\]$");

    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JaxeElement.class);

    public final static String kGras = "GRAS";
    public final static String kItalique = "ITALIQUE";
    public final static String kExposant = "EXPOSANT";
    public final static String kCouleur = "PCOULEUR";
    public final static String kCouleurDeFond = "FCOULEUR";
    public final static String kIndice = "INDICE";
    public final static String kSouligne = "SOULIGNE";
    public final static String kBarre = "BARRE";
    
    
    //static String newline = Jaxe.newline;
    public Position debut = null; // position du premier caractère de l'élément

    public Position fin = null; // position du dernier caractère de l'élément

    public Node noeud;

    public Element refElement = null; // référence de l'élément dans le schéma

    public JaxeDocument doc;

    public ArrayList<Object> jcomps = null; // de JComponent ou Icon

    public ArrayList<Position> compos = null; // de Position (positions des
    // composants)

    private boolean effacementAutorise = true;

    private boolean editionAutorisee = true;
    
    
    /**
     * Insère le texte de l'élément à partir de l'arbre DOM, à la position pos
     * dans le texte
     */
    public abstract void init(Position pos, Node noeud);

    /**
     * Initialise le champ noeud, met à jour dom2JaxeElement, et appelle
     * init(pos, noeud)
     */
    public void creer(final Position pos, final Node noeud) {
        this.noeud = noeud;
        doc.dom2JaxeElement.put(noeud, this);
        if (doc.cfg != null) {
            final String typeNoeud;
            final String nomNoeud = noeud.getNodeName();
            if (noeud.getNodeType() == Node.ELEMENT_NODE) {
                if (refElement == null) {
                    final Node nparent = noeud.getParentNode();
                    if (nparent != null)
                        refElement = doc.cfg.getElementRef((Element)noeud, doc.getElementForNode(nparent).refElement);
                    else
                        refElement = doc.cfg.getElementRef((Element)noeud, null);
                }
                typeNoeud = "element";
            } else if (noeud.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
                typeNoeud = "instruction";
            else if (noeud.getNodeType() == Node.COMMENT_NODE)
                typeNoeud = "commentaire";
            else if (noeud.getNodeType() == Node.CDATA_SECTION_NODE)
                typeNoeud = "cdata";
            else
                typeNoeud = null;
            if (refElement != null || noeud.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                final HashMap<String, ArrayList<String>> parametres = doc.cfg.getParametresNoeud(refElement, typeNoeud, nomNoeud);
                final ArrayList<String> paramsEffacement = parametres.get("effacementAutorise");
                String seffacement;
                if (paramsEffacement != null && paramsEffacement.size() > 0)
                    seffacement = paramsEffacement.get(0);
                else
                    seffacement = null;
                final ArrayList<String> paramsEdition = parametres.get("editionAutorisee");
                String sedition;
                if (paramsEdition != null && paramsEdition.size() > 0)
                    sedition = paramsEdition.get(0);
                else
                    sedition = null;
                effacementAutorise = !("false".equals(seffacement));
                editionAutorisee = !("false".equals(sedition));
            }
        }
        init(pos, noeud);
        majValidite();
    }

    public abstract Node nouvelElement(Element refElement);
    
    /**
     * Renvoie true s'il faut afficher le dialogue d'attributs à la création de l'élément
     */
    public boolean testAffichageDialogue() {
        final ArrayList<Element> latt = doc.cfg.listeAttributs(refElement);
        if (latt == null || latt.size() == 0)
            return(false);
        final Properties prefs = Preferences.getPref();
        final boolean toujoursAfficherAttributs = (prefs != null && "true".equals(prefs.getProperty("toujoursAfficherAttributs")));
        if (toujoursAfficherAttributs)
            return(true);
        else {
            for (Element attdef : latt)
                if (doc.cfg.estObligatoire(attdef))
                    return(true);
            return(false);
        }
    }

    /**
     * Affiche le dialogue correspondant à l'élément
     */
    public void afficherDialogue(final JFrame jframe) {
        // à remplacer dans les sousclasses
    }

    /**
     * Mise à jour de l'affichage par rapport à l'arbre XML
     */
    public void majAffichage() {
        // à remplacer dans les sousclasses
    }

    /**
     * Test et mise à jour de l'affichage de la validité
     */
    public void majValidite() {
        // à remplacer dans les sousclasses
    }

    /**
     * Renvoit la liste des composants graphiques utilisés dans l'affichage en
     * plus du texte
     */
    public ArrayList<Object> getComponents() {
        if (jcomps == null)
            return(new ArrayList<Object>());
        return jcomps;
    }

    /**
     * Renvoit la liste des positions dans le texte des composants graphiques
     */
    public ArrayList<Position> getComponentPositions() {
        if (compos == null)
            return(new ArrayList<Position>());
        return compos;
    }

    /**
     * Insère le texte dans le Textpane en mettant à jour debut et fin
     */
    public Position insertText(Position pos, final String texte, final AttributeSet attset) {
        try {
            final int offsetdebut = pos.getOffset();
            doc.insertString(pos.getOffset(), texte, attset);
            if (debut == null)
                debut = doc.createPosition(offsetdebut);
            if (pos.getOffset() == 0) // bug fix with insertString
                pos = doc.createPosition(1);
            fin = doc.createPosition(pos.getOffset() - 1);
        } catch (final BadLocationException ex) {
            LOG.error("insertText(Position, String, AttributeSet) - BadLocationException: " + ex.getMessage(), ex);
        }
        return (pos);
    }

    /**
     * Insère le texte dans le Textpane en mettant à jour debut et fin
     */
    public Position insertText(final Position pos, final String texte) {
        SimpleAttributeSet att = null;
        JaxeElement jeparent;
        if (debut == null) {
            final Node parentnode = noeud.getParentNode();
            if (parentnode != null)
                jeparent = doc.getElementForNode(parentnode);
            else
                jeparent = null;
        } else
            jeparent = this;
        if (jeparent != null) {
            if (jeparent.debut.getOffset() == pos.getOffset() && !(jeparent instanceof JESwing))
                jeparent = jeparent.getParent();
            if (jeparent != null)
                att = jeparent.attStyle(null);
        }
        return (insertText(pos, texte, att));
    }

    /**
     * Insère le composant graphique dans le texte, en l'ajoutant dans la liste
     * des composants et en mettant à jour debut et fin
     */
    public Position insertComponent(Position pos, final JComponent comp) {
        final int offsetdebut = pos.getOffset();
        final Style s = doc.textPane.addStyle(null, null);
        StyleConstants.setComponent(s, comp);
        try {
            doc.insertString(pos.getOffset(), "*", s, false);
            if (jcomps == null)
                jcomps = new ArrayList<Object>(2);
            jcomps.add(comp);
            if (compos == null)
                compos = new ArrayList<Position>(2);
            compos.add(doc.createPosition(pos.getOffset() - 1));
            if (debut == null) debut = doc.createPosition(offsetdebut);
            fin = doc.createPosition(offsetdebut);
            if (pos.getOffset() == 0) // bug fix with insertString
                pos = doc.createPosition(1);
        } catch (final BadLocationException ex) {
            LOG.error("insertComponent(Position, JComponent) - BadLocationException: " + ex.getMessage(), ex);
        }
        return (pos);
    }

    /**
     * Insère l'icône dans le texte, en l'ajoutant dans la liste des composants
     * et en mettant à jour debut et fin
     */
    public Position insertIcon(Position pos, final Icon icon) {
        final int offsetdebut = pos.getOffset();
        final Style s = doc.textPane.addStyle(null, null);
        StyleConstants.setIcon(s, icon);
        try {
            doc.insertString(pos.getOffset(), "*", s, false);
            if (jcomps == null)
                jcomps = new ArrayList<Object>(2);
            jcomps.add(icon);
            if (compos == null)
                compos = new ArrayList<Position>(2);
            compos.add(doc.createPosition(pos.getOffset() - 1));
            if (debut == null) debut = doc.createPosition(offsetdebut);
            fin = doc.createPosition(offsetdebut);
            if (pos.getOffset() == 0) // bug fix with insertString
                pos = doc.createPosition(1);
        } catch (final BadLocationException ex) {
            LOG.error("insertIcon(Position, Icon) - BadLocationException: " + ex.getMessage(), ex);
        }
        return (pos);
    }

    /**
     * Renvoit l'élément de plus bas niveau se trouvant à la position donnée
     * dans le texte
     */
    public JaxeElement elementA(final int pos) {
        if (debut == null || fin == null)
            return null;
        if (debut.getOffset() > pos || fin.getOffset() < pos)
            return null;
        for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            final short type = n.getNodeType();
            if (type == Node.ELEMENT_NODE || type == Node.TEXT_NODE ||
                    type == Node.PROCESSING_INSTRUCTION_NODE ||
                    type == Node.COMMENT_NODE || type == Node.CDATA_SECTION_NODE) {
                final JaxeElement je = doc.getElementForNode(n);
                if (je != null) {
                    final JaxeElement nje = je.elementA(pos);
                    if (nje != null)
                        return nje;
                }
            }
        }
        return this;
    }

    /**
     * Renvoit les éléments se trouvant dans la zone du texte indiquée
     */
    public ArrayList<JaxeElement> elementsDans(final int dpos, final int fpos) {
        final ArrayList<JaxeElement> l = new ArrayList<JaxeElement>();
        if (debut == null || fin == null)
            return l;
        if (debut.getOffset() > fpos || fin.getOffset() < dpos)
            return l;
        if (debut.getOffset() >= dpos && (fin.getOffset() <= fpos ||
                this instanceof JESwing && fin.getOffset() == fpos+1))
            l.add(this);
        else
            for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
                final short type = n.getNodeType();
                if (type == Node.ELEMENT_NODE || type == Node.TEXT_NODE ||
                        type == Node.PROCESSING_INSTRUCTION_NODE ||
                        type == Node.COMMENT_NODE || type == Node.CDATA_SECTION_NODE) {
                    final JaxeElement je = doc.getElementForNode(n);
                    if (je != null)
                        l.addAll(je.elementsDans(dpos, fpos));
                }
            }
        return l;
    }

    /**
     * Renvoit le nombre XPath (le numéro de l'élément dans la liste des
     * éléments avec ce nom), ou 0 si le noeud n'a pas de parent.
     */
    public int nombreXPath() {
        final JaxeElement p = getParent();
        if (p == null)
            return(0);
        int no = 0;
        final String nomel = noeud.getNodeName();
        for (Node n = p.noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (nomel.equals(n.getNodeName()))
                no++;
            if (n == noeud)
                break;
        }
        return (no);
    }

    /**
     * Renvoit le chemin XML pour la position pos
     */
    public String cheminA(final int pos) {
        if (debut == null || fin == null)
            return(null);
        if (debut.getOffset() > pos || fin.getOffset() < pos)
            return(null);
        if (noeud.getNodeType() == Node.TEXT_NODE)
            return("texte");
        if (noeud.getNodeType() == Node.COMMENT_NODE)
            return("commentaire");
        if (noeud.getNodeType() == Node.CDATA_SECTION_NODE)
            return("cdata");
        if (!(noeud.getNodeType() == Node.ELEMENT_NODE))
            return(null);
        Element el = (Element) noeud;
        final StringBuilder nomel = new StringBuilder(el.getTagName());
        if (this instanceof JEStyle) {
            nomel.setLength(0);
            final Iterator<Element> it = ((JEStyle)this)._styles.iterator();
            while (it.hasNext()) {
                final Element sel = it.next();
                if (getParent() != null)  {
                    nomel.append(sel.getNodeName());
                    nomel.append("[");
                    nomel.append(nombreXPath());
                    nomel.append("]");
                }
                if (it.hasNext()) nomel.append("/"); else el = sel;
            }
        } else if (getParent() != null) {
            nomel.append("[");
            nomel.append(nombreXPath());
            nomel.append("]");
        }
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            final short type = n.getNodeType();
            if (type == Node.ELEMENT_NODE || type == Node.TEXT_NODE ||
                    type == Node.PROCESSING_INSTRUCTION_NODE ||
                    type == Node.COMMENT_NODE || type == Node.CDATA_SECTION_NODE) {
                final JaxeElement je = doc.getElementForNode(n);
                if (je != null) {
                    final String chemin = je.cheminA(pos);
                    if (chemin != null)
                        return (nomel.append("/").append(chemin).toString());
                }
            }
        }
        return (nomel.toString());
    }

    /**
     * Renvoit le premier élément enfant de celui-ci dont la position est pos ou
     * après pos
     */
    public JaxeElement enfantApres(final int pos) {
        if (debut == null || fin == null)
            return null;
        if (debut.getOffset() > pos || fin.getOffset() < pos)
            return null;
        for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            final short type = n.getNodeType();
            if (type == Node.ELEMENT_NODE || type == Node.TEXT_NODE ||
                    type == Node.PROCESSING_INSTRUCTION_NODE ||
                    type == Node.COMMENT_NODE || type == Node.CDATA_SECTION_NODE) {
                final JaxeElement je = doc.getElementForNode(n);
                if (je != null) {
                    if (je.debut.getOffset() == pos)
                        return (je);
                    final JaxeElement nje = je.elementA(pos);
                    if (nje != null && n.getNextSibling() != null)
                        return(doc.getElementForNode(n.getNextSibling()));
                }
            }
        }
        return null;
    }

    /**
     * appelé juste avant que l'élément soit effacé
     */
    public void effacer() {
        for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            final short type = n.getNodeType();
            if (type == Node.ELEMENT_NODE || type == Node.TEXT_NODE ||
                    type == Node.PROCESSING_INSTRUCTION_NODE ||
                    type == Node.COMMENT_NODE || type == Node.CDATA_SECTION_NODE) {
                final JaxeElement je = doc.getElementForNode(n);
                if (je != null)
                    je.effacer();
            }
        }
        if (jcomps != null) {
            for (final Object o : jcomps) {
                if (o instanceof JComponent && ((JComponent) o).getParent() != null) {
                    final JComponent comp = (JComponent) o;
                    comp.getParent().remove(comp);
                }
            }
            jcomps = null;
        }
        compos = null;
    }

    /**
     * met à jour l'arbre de JaxeElement et l'arbre DOM à partir de modifs de la
     * zone de texte
     */
    public void mettreAJourDOM() {
        if (debut == null || fin == null)
            return;
        try {
            if (this instanceof JECommentaire || this instanceof JECData) {
                final String texte = doc.getText(debut.getOffset()+1, fin.getOffset()
                        - debut.getOffset() - 1);
                noeud.setNodeValue(texte);
            } else if (noeud.getNodeType() == Node.TEXT_NODE ||
                    this instanceof JEStyle) {
                final Node nsuivant = noeud.getNextSibling();
                if (nsuivant != null) {
                    final JaxeElement jesuivant = doc.getElementForNode(nsuivant);
                    if (jesuivant != null
                            && jesuivant.debut.getOffset() > fin.getOffset() + 1) {
                        // texte rajouté à la fin, avant un autre élément
                        fin = doc.createPosition(jesuivant.debut.getOffset() - 1);
                    }
                }
                final String texte = doc.getText(debut.getOffset(), fin.getOffset()
                        - debut.getOffset() + 1);
                if (texte == null || "".equals(texte))
                    getParent().supprimerEnfant(this);
                else {
                    if (noeud.getNodeType() == Node.TEXT_NODE)
                        noeud.setNodeValue(texte);
                    else {
                        Node n = noeud;
                        while (n != null && n.getNodeType() != Node.TEXT_NODE) {
                            n = n.getFirstChild();
                        }
                        if (n != null)
                            n.setNodeValue(texte);
                    }
                }
            } else {
                int offdebut = debut.getOffset();
                int debuttexte;
                if (this instanceof JESwing)
                    debuttexte = offdebut;
                else
                    debuttexte = offdebut + 1;
                for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
                    final JaxeElement je = doc.getElementForNode(n);
                    if (je != null) {
                        if (debuttexte < je.debut.getOffset()) {
                            JaxeElement jeprev = null;
                            if (n.getPreviousSibling() != null)
                                    jeprev = doc.getElementForNode(n.getPreviousSibling());
                            if (jeprev != null
                                    && (jeprev instanceof JEStyle || jeprev instanceof JETexte)) {
                                // texte ajouté à la fin du précédent noeud
                                jeprev.fin = doc.createPosition(je.debut.getOffset() - 1);
                            } else if (je instanceof JETexte)
                                // texte ajouté au début
                                je.debut = doc.createPosition(debuttexte);
                            else {
                                // nouvelle zone de texte avant ce noeud
                                final String texte = doc.getText(debuttexte, je.debut.getOffset()
                                        - debuttexte);
                                final JETexte newje = JETexte.nouveau(doc, doc.createPosition(debuttexte),
                                        doc.createPosition(je.debut.getOffset() - 1),
                                        texte);
                                noeud.insertBefore(newje.noeud, n);
                            }
                        }
                        offdebut = je.fin.getOffset();
                        debuttexte = offdebut + 1;
                    }
                }
                if (debuttexte < fin.getOffset()) { // texte à la fin, après le dernier enfant
                    JaxeElement pje = null;
                    if (noeud.getLastChild() != null)
                        pje = doc.getElementForNode(noeud.getLastChild());
                    if (pje instanceof JEStyle || pje instanceof JETexte)
                        // texte ajouté à la fin du dernier enfant
                        pje.fin = doc.createPosition(fin.getOffset() - 1);
                    else {
                        // nouvelle zone de texte à la fin
                        final String texte = doc.getText(debuttexte, fin.getOffset() - debuttexte);
                        final JETexte newje = JETexte.nouveau(doc, doc.createPosition(debuttexte),
                            doc.createPosition(fin.getOffset() - 1), texte);
                        noeud.appendChild(newje.noeud);
                    }
                }
                for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
                    final JaxeElement je = doc.getElementForNode(n);
                    if (je != null)
                        je.mettreAJourDOM();
                }
            }
        } catch (final BadLocationException ex) {
            LOG.error("mettreAJourDOM()" + ex.getMessage(), ex);
        }
    }

    /**
     * nouvel élément DOM. Attention: ambiguë quand des espaces de noms sont employés
     *
     * @deprecated  Utiliser nouvelElementDOM(JaxeDocument, Element) à la place
     */
    @Deprecated
    public static Node nouvelElementDOM(final JaxeDocument doc, final String type,
            String nomNoeud) {
        Node newel;
        if (type.equals("instruction"))
            newel = doc.DOMdoc.createProcessingInstruction(nomNoeud, "");
        else {
            if (doc.cfg.namespace() == null)
                newel = doc.DOMdoc.createElementNS(null, nomNoeud);
            else {
                Config conf = doc.cfg.getBaliseConf(nomNoeud);
                if (conf == null)
                    conf = doc.cfg;
                if (conf.prefixe() != null)
                    nomNoeud = conf.prefixe() + ":" + nomNoeud;
                newel = doc.DOMdoc.createElementNS(conf.namespace(), nomNoeud);
            }
        }
        return (newel);
    }

    /**
     * nouvel élément DOM. Espace de noms obtenu à partir de la définition de la configuration.
     * Attention, l'élément est maintenant une référence d'élément, ce n'est plus la définition du fichier de config.
     */
    public static Element nouvelElementDOM(final JaxeDocument doc, final Element refElement) {
        if ("BALISE".equals(refElement.getNodeName()) && doc.cfg.getSchema() instanceof SchemaW3C) {
            LOG.error("nouvelElementDOM utilisé avec un élément du fichier de config au lieu d'un élément du schéma");
            return(nouvelElementDOM(doc, doc.cfg.referenceElement(refElement.getAttribute("nom"))));
        }
        final Element newel;
        String nom = doc.cfg.nomElement(refElement);
        final String espace = doc.cfg.espaceElement(refElement);
        if (espace == null)
            newel = doc.DOMdoc.createElementNS(null, nom);
        else {
            String prefixe;
            if (doc.DOMdoc.getDocumentElement() != null)
                prefixe = doc.DOMdoc.lookupPrefix(espace);
            else
                prefixe = null;
            if (prefixe == null)
                prefixe = doc.cfg.prefixeElement(refElement);
            if (prefixe != null)
                nom = prefixe + ":" + nom;
            newel = doc.DOMdoc.createElementNS(espace, nom);
        }
        return (newel);
    }
    
    public static ProcessingInstruction nouvelleInstructionDOM(final JaxeDocument doc, final String cible) {
        final ProcessingInstruction newpi = doc.DOMdoc.createProcessingInstruction(cible, "");
        return newpi;
    }

    /**
     * nouvel élément DOM à partir Espace de noms obtenu à partir de l'élément
     * parent
     */
    public static Element nouvelElementDOM(final JaxeDocument doc, final String nomElement,
            final Element parent) {
        final String ns = parent.getNamespaceURI();
        final String prefixe = parent.getPrefix();
        final String nomElement2;
        if (prefixe != null)
            nomElement2 = prefixe + ':' + nomElement;
        else
            nomElement2 = nomElement;
        return (doc.DOMdoc.createElementNS(ns, nomElement2));
    }

    /**
     * initialise et insère cet élément dans le texte et l'arbre DOM
     */
    public void inserer(final Position pos, final Node newel) {
        doc.textPane.debutIgnorerEdition();
        insererDOM(pos, newel);
        creer(pos, newel);
        doc.textPane.finIgnorerEdition();

        // JESwing: mise à jour du début des parents
        JaxeElement jeparent = getParent();
        while (jeparent instanceof JESwing
                && jeparent.debut.getOffset() > debut.getOffset()) {
            jeparent.debut = debut;
            jeparent = jeparent.getParent();
        }
    }

    /**
     * insère newel dans l'arbre DOM
     */
    public void insererDOM(final Position pos, final Node newel) {
        JaxeElement parent = doc.rootJE.elementA(pos.getOffset());
        if (parent.debut.getOffset() == pos.getOffset()
                && !(parent instanceof JESwing)) parent = parent.getParent();
        if (parent instanceof JETexte || parent instanceof JEStyle) {
            final int ic = pos.getOffset() - parent.debut.getOffset();
            if (ic > 0) {
                // nouvelle zone de texte... à revoir
                /*
                 * String s = parent.noeud.getNodeValue(); String s1 =
                 * s.substring(0, ic); String s2 = s.substring(ic);
                 * parent.noeud.setNodeValue(s2); Node ns1 =
                 * doc.DOMdoc.createTextNode(s1); Node parent2 =
                 * parent.noeud.getParentNode(); parent2.insertBefore(ns1,
                 * parent.noeud); parent2.insertBefore(newel, parent.noeud);
                 */
                final JaxeElement je2 = parent.couper(pos);
                final Node parent2 = parent.noeud.getParentNode();
                parent2.insertBefore(newel, je2.noeud);
            } else {
                final Node parent2 = parent.noeud.getParentNode();
                parent2.insertBefore(newel, parent.noeud);
            }
        } else {
            final JaxeElement jelbef = parent.enfantApres(pos.getOffset());
            if (jelbef == null)
                parent.noeud.appendChild(newel);
            else
                parent.noeud.insertBefore(newel, jelbef.noeud);
        }
    }

    /**
     * creer les enfants de ce noeud, en supposant que c'est un élément DOM
     */
    public void creerEnfants(final Position newpos) {
        final Element el = (Element) noeud;
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling())
            n = creerEnfant(newpos, n);
    }

    /**
     * creer l'enfant n à la position newpos (avec JaxeElement.creer)
     */
    public Node creerEnfant(Position newpos, Node n) {
        final int offsetdebut = newpos.getOffset();
        if (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE
                || n.getNodeType() == Node.COMMENT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
            Element elref = null;
            String typeAffichage = null;
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (doc.cfg != null) {
                    final Node nparent = n.getParentNode();
                    if (nparent != null)
                        elref = doc.cfg.getElementRef((Element) n, doc.getElementForNode(nparent).refElement);
                    else
                        elref = doc.cfg.getElementRef((Element) n, null);
                }
                if (elref != null)
                    typeAffichage = doc.cfg.typeAffichageElement(elref);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE && elref == null) {
                final JEInconnu newje = new JEInconnu(doc);
                newje.creer(newpos, n);
            } else if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE || n.getNodeType() == Node.COMMENT_NODE ||
                    n.getNodeType() == Node.CDATA_SECTION_NODE ||
                    !"style".equals(typeAffichage) || hasText(n) || hasProcessing(n)) {
                // on ne crée pas de JEStyle vide, sinon debut = fin = null
                String typeNoeud;
                if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
                    typeNoeud = "instruction";
                else if (n.getNodeType() == Node.COMMENT_NODE)
                    typeNoeud = "commentaire";
                else if (n.getNodeType() == Node.CDATA_SECTION_NODE)
                    typeNoeud = "cdata";
                else if (typeAffichage.equals("style") && hasProcessing(n)) {
                    final Node prev = n.getPreviousSibling();
                    final Node parent = n.getParentNode();
                    final ProcessingInstruction p = getProcessing(n);
                    n.getParentNode().replaceChild(p, n);
                    if (prev == null)
                        n = parent.getFirstChild();
                    else
                        n = prev.getNextSibling();
                    elref = null;
                    typeNoeud = "instruction";
                } else
                    typeNoeud = "element";
                JaxeElement newje;
                final JaxeElement oldje = doc.getElementForNode(n);
                if (oldje != null) {
                    // il existe déjà un JaxeElement pour ce noeud,
                    // on va le réutiliser
                    // (il est peut-être pointé par un JaxeUndoableEdit)
                    newje = oldje;
                    newje.debut = null;
                    newje.fin = null;
                    newje.jcomps = null;
                    newje.compos = null;
                } else
                    newje = JEFactory.createJE(doc, elref, n.getNodeName(), typeNoeud, n);
                newje.creer(newpos, n);
            }
        } else if (n.getNodeType() == Node.TEXT_NODE) {
            final JETexte newje = new JETexte(doc);
            newje.creer(newpos, n);
        }
        try {
            if (debut == null)
                debut = doc.createPosition(offsetdebut);
            if (newpos.getOffset() == 0) // bug fix with insertString
                newpos = doc.createPosition(1);
            fin = doc.createPosition(newpos.getOffset() - 1);
        } catch (final BadLocationException ex) {
            LOG.error("creerEnfant(Position, Node) - BadLocationException: " + ex.getMessage(), ex);
        }
        return n;
    }
    
    /**
     * Met le focus dans un composant de l'élément après sa création.
     * Par défaut demande le focus pour la zone de texte globale de Jaxe.
     */
    public void setFocus() {
        doc.textPane.requestFocus();
    }
    
    /**
     * position pour setCaretPosition après création d'un nouvel élément
     */
    public Position insPosition() {
        try {
            final Position p = doc.createPosition(fin.getOffset() + 1);
            return (p);
        } catch (final BadLocationException ex) {
            LOG.error("insPosition() - BadLocationException: " + ex.getMessage(), ex);
            return (debut);
        }
    }

    public boolean hasText(final Node n) {
        boolean result = false;
        final Node child = n.getFirstChild();
        if (child != null) {
            if (child.getNodeType() == Node.TEXT_NODE) {
                result = true;
            } else {
                result = hasText(child);
            }
        }
        return result;
    }

    public static boolean hasProcessing(final Node n) {
        boolean result = false;
        final Node child = n.getFirstChild();
        if (child != null) {
            if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                result = true;
            } else {
                result = hasProcessing(child);
            }
        }
        return result;
    }

    public static ProcessingInstruction getProcessing(final Node n) {
        ProcessingInstruction result = null;
        final Node child = n.getFirstChild();
        if (child != null) {
            if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                result = (ProcessingInstruction) child;
            } else {
                result = getProcessing(child);
            }
        }
        return result;
    }

    /**
     * Renvoit l'élément parent, en utilisant l'arbre DOM
     */
    public JaxeElement getParent() {
        final Node parent = noeud.getParentNode();
        if (parent == null)
            return null;
        return (doc.getElementForNode(parent));
    }

    /**
     * Renvoit le premier élément enfant (ou null)
     */
    public JaxeElement getFirstChild() {
        final Node n = noeud.getFirstChild();
        if (n == null)
            return (null);
        return (doc.getElementForNode(n));
    }

    /**
     * Renvoit l'enfant suivant (ou null)
     */
    public JaxeElement getNextSibling() {
        final Node n = noeud.getNextSibling();
        if (n == null)
            return (null);
        return (doc.getElementForNode(n));
    }

    /**
     * supprime l'enfant je à la fois dans le texte et dans le DOM
     */
    public void supprimerEnfant(final JaxeElement je) {
        supprimerEnfantDOM(je); // placé avant doc.remove à cause de caretUpdate

        try {
            final int len = je.fin.getOffset() - je.debut.getOffset() + 1;
            /*
             * String cfin = doc.getText(je.fin.getOffset() + 1, 1); if
             * (newline.equals(cfin)) len++;
             */
            final int idebut = je.debut.getOffset();
            /*
             * javax.swing.text.Element pel = doc.getParagraphElement(idebut -
             * 1); javax.swing.text.Element pel2 =
             * doc.getParagraphElement(idebut); if (pel2 != pel) { AttributeSet
             * attavant = pel.getAttributes();
             * doc.setParagraphAttributes(pel2.getStartOffset(),
             * pel2.getEndOffset() - pel2.getStartOffset(), attavant, true); }
             */// bug avec jdk 1.4
            doc.remove(idebut, len, false);
        } catch (final BadLocationException ex) {
            LOG.error("supprimerEnfant() - BadLocationException", ex);
        }
    }

    /**
     * supprime l'enfant je dans le DOM
     */
    public void supprimerEnfantDOM(final JaxeElement je) {
        try {
            noeud.removeChild(je.noeud);
        } catch (final DOMException ex) {
            LOG.error("supprimerEnfantDOM() - DOMException", ex);
        }
    }

    /**
     * remplace l'enfant je à la fois dans le texte et dans le DOM
     */
    public void remplacerEnfant(final JaxeElement je, final JaxeElement newje) {
        try {
            doc.remove(je.debut.getOffset(), je.fin.getOffset()
                    - je.debut.getOffset() + 1);
        } catch (final BadLocationException ex) {
            LOG.error("remplacerEnfant() - BadLocationException", ex);
        }
        newje.creer(newje.debut, newje.noeud);

        remplacerEnfantDOM(je, newje);
    }

    /**
     * remplace l'enfant je dans le DOM
     */
    public void remplacerEnfantDOM(final JaxeElement je, final JaxeElement newje) {
        final Node parent = je.noeud.getParentNode();
        if (parent == null)
            LOG.error("remplacerEnfantDOM(JaxeElement, JaxeElement) - remplacerEnfantDOM: parent null !", null);
        try {
            parent.replaceChild(newje.noeud, je.noeud);
        } catch (final DOMException ex) {
            LOG.error("remplacerEnfantDOM(JaxeElement, JaxeElement) - DOMException: " + ex.getMessage(), ex);
        }
    }

    /**
     * Renvoit la profondeur dans l'arbre XML.
     */
    /*
     * public int profondeur() { JaxeElement p = getParent(); if (p == null)
     * return(0); else return(p.profondeur() + 1); }
     */

    /**
     * Indique si les descendants de l'élément doivent être indentés
     */
    public boolean avecIndentation() {
        return (false);
    }

    /**
     * Indique si des sauts de ligne doivent être ajoutés avant et après s'il n'y en a pas déjà
     */
    public boolean avecSautsDeLigne() {
        return (false);
    }

    /**
     * Renvoit les indentations dans l'arbre XML. 0 pour la racine de l'arbre et
     * JETableTexte.
     */
    public int indentations() {
        final JaxeElement p = getParent();
        if (p != null) {
            JaxeElement p2 = p.getParent();
            if (p2 != null) {
                p2 = p2.getParent();
                if (p2 instanceof JETableTexte)
                    return (0);
            }
        }
        if (p == null)
            return (0);
        else if (avecIndentation())
            return (p.indentations() + 1);
        else
            return (p.indentations());
    }

    /**
     * coupe la zone de texte en 2, retourne la nouvelle zone créée après
     * celle-ci
     */
    public JaxeElement couper(final Position pos) {
        final String t = noeud.getNodeValue();
        final String t1 = t.substring(0, pos.getOffset() - debut.getOffset());
        final String t2 = t.substring(pos.getOffset() - debut.getOffset());
        noeud.setNodeValue(t1);
        final Node textnode2 = doc.DOMdoc.createTextNode(t2);
        final Node nextnode = noeud.getNextSibling();
        final JaxeElement parent = getParent();
        if (nextnode == null)
            parent.noeud.appendChild(textnode2);
        else
            parent.noeud.insertBefore(textnode2, nextnode);
        final JETexte je2 = new JETexte(doc);
        je2.noeud = textnode2;
        je2.doc = parent.doc;
        try {
            je2.debut = doc.createPosition(pos.getOffset());
            je2.fin = fin;
            fin = doc.createPosition(pos.getOffset() - 1);
        } catch (final BadLocationException ex) {
            LOG.error("couper() - BadLocationException", ex);
        }
        doc.dom2JaxeElement.put(je2.noeud, je2);
        return (je2);
    }

    /**
     * fusionne cet élément avec celui donné, dans le DOM (aucun changement du
     * texte)
     */
    public void fusionner(final JaxeElement el) {
        if (!(this instanceof JETexte && el instanceof JETexte))
            return;
        if (noeud.getNextSibling() == el.noeud) {
            final String t = el.noeud.getNodeValue();
            noeud.setNodeValue(noeud.getNodeValue() + t);
            fin = el.fin;
            el.getParent().supprimerEnfantDOM(el);
        } else if (el.noeud.getNextSibling() == noeud) {
            final String t = el.noeud.getNodeValue();
            noeud.setNodeValue(t + noeud.getNodeValue());
            debut = el.debut;
            el.getParent().supprimerEnfantDOM(el);
        }
    }

    /**
     * regroupe les JETexte dans les enfants
     */
    public void regrouperTextes() {
        for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            final JaxeElement je1 = doc.getElementForNode(n);
            JaxeElement je2 = doc.getElementForNode(n.getNextSibling());
            while (je2 != null && ((je1 instanceof JETexte && je2 instanceof JETexte) || (je1 instanceof JEStyle && je2 instanceof JEStyle && sameStyle(je1, je2)))) {
                je1.fusionner(je2);
                je2 = je1.getNextSibling();
            }
        }
    }

    public void setEffacementAutorise(final boolean autorise) {
        effacementAutorise = autorise;
    }

    public boolean getEffacementAutorise() {
        return (effacementAutorise);
    }

    public void setEditionAutorisee(final boolean autorise) {
        editionAutorisee = autorise;
    }

    public boolean getEditionAutorisee() {
        return (editionAutorisee);
    }

    /**
     * Sélection de la zone de texte où se trouve cet élément
     */
    public void selection(final boolean select) {
        if (jcomps != null) {
            for (final Object comp : jcomps) {
                if (comp instanceof Balise)
                    ((Balise) comp).selection(select);
            }
        }
        for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            final JaxeElement je = doc.getElementForNode(n);
            if (je != null)
                je.selection(select);
        }
    }
    
    public static boolean sameStyle(final JaxeElement source, final JaxeElement target) {
        if (!(source instanceof JEStyle && target instanceof JEStyle))
            return false;
        final List<String> s1 = new ArrayList<String>();
        final JEStyle sourceJE = (JEStyle)source;
        final JEStyle targetJE = (JEStyle)target;
        Iterator<? extends Node> temp = sourceJE._styles.iterator();
        if (targetJE._styles.size() != sourceJE._styles.size()) {
            return false;
        }
        while (temp.hasNext()) {
            s1.add(temp.next().getNodeName());
        }
        final List<String> s2 = new ArrayList<String>();
        temp = targetJE._styles.iterator();
        while (temp.hasNext()) {
            s2.add(temp.next().getNodeName());
        }

        boolean result = true;
        Iterator<String> it = s1.iterator();
        while (it.hasNext() && result) {
            if (!s2.contains(it.next()))
                result = false;
        }
        it = s2.iterator();
        while (it.hasNext() && result) {
            if (!s1.contains(it.next()))
                result = false;
        }
        if (result) {
            final String[] sourceStyles = sourceJE.ceStyle.split(";");
            final String[] targetStyles = targetJE.ceStyle.split(";");
            Arrays.sort(sourceStyles);
            Arrays.sort(targetStyles);
            final List<String> soStList = Arrays.asList(sourceStyles);
            final List<String> taStList = Arrays.asList(targetStyles);
            result = soStList.containsAll(taStList) && taStList.containsAll(soStList); 
        }
        return result;
    }
    
    public boolean aDesAttributs() {
        if (noeud.getNodeType() != Node.ELEMENT_NODE)
            return(false);
        final Element el = (Element) noeud;
        if (doc.cfg == null)
            return(true);
        if (refElement == null)
            return(true);
        final ArrayList<Element> latt = doc.cfg.listeAttributs(refElement);
        return (latt != null && latt.size() > 0);
    }
    
    /**
     * Renvoit la position de l'élément descendant dans le JaxeTextPane.
     * A implémenter dans les sous-classes ayant des composants Swing
     * qui gèrent les éléments descendants, comme JEFormulaire ou JETable.
     * Appelé par JaxeTextPane.allerElement().
     */
    public Point getPointEnfant(final Element el) {
        return(null);
    }
    
    
    /**
     * Défini les ensembles de couleurs à utiliser pour les boutons. Par défaut,
     * tableau de tableaux à 3 entrées: 1) normal 2) sélection 3) invalide.
     * Par exemple
     * {{jauneLeger, rougeFonce, orange}, {bleuClair, rougeFonce, violet}}
     * @deprecated utiliser Balise.setCouleurs à la place
     */
    @Deprecated
    public static void setMonBoutonCouleurs(final Color[][] couleurs) {
        Balise.setCouleurs(couleurs);
    }
    
    /**
     * Returns the current Colors of the Buttons
     * 
     * @return Colors of the Buttons
     * @deprecated utiliser Balise.getCouleurs à la place
     */
    @Deprecated
    public static Color[][] getMonBoutonCouleurs() {
        return(Balise.getCouleurs());
    }
    
    /**
     * Sets the Border of the Button
     * @deprecated
     * 
     * @param border
     *            New Border of the Button
     */
    @Deprecated
    public static void setMonBoutonBorder(final Border border) {
        Balise.setBord(border);
    }
    
    /**
     * Returns the current border of the Buttons
     * 
     * @return Border of the Buttons
     * @deprecated utiliser Balise.getBord à la place
     */
    @Deprecated
    public static Border getMonBoutonBorder() {
        return(Balise.getBord());
    }
    
    /**
     * Gets the Font the MonBouton is using
     * 
     * @return Current Font of MonBouton
     * @deprecated utiliser Balise.getPolice à la place
     */
    @Deprecated
    public static Font getMonBoutonFont() {
        return(Balise.getPolice());
    }

    /**
     * Sets the Font the MonBouton should use
     * 
     * @param font
     *            Font to use
     * @deprecated utiliser Balise.setPolice à la place
     */
    @Deprecated
    public static void setMonBoutonFont(final Font font) {
        Balise.setPolice(font);
    }

    /**
     * Bouton représentant le début ou la fin d'un élément dans le texte
     * @deprecated utiliser Balise à la place
     */
    @Deprecated
    public class MonBouton extends Balise {
        
        public MonBouton(final JaxeElement je, final boolean division, final int typeBalise) {
            super(je, division, typeBalise);
        }
        
        @Deprecated
        public MonBouton(final String texte, final boolean division) {
            super(JaxeElement.this, texte, division, VIDE);
        }
        
        public MonBouton(final String texte, final boolean division, final int typeBalise) {
            super(JaxeElement.this, texte, division, typeBalise);
        }
        
    }
    
    
    protected static String getString(final String key) {
        return(JaxeResourceBundle.getRB().getString(key));
    }

    public void changerStyle(final String style, final int offset, final int longueur) {
        if (style != null) {

            final String[] styleSplit = style.split(";");

            final Style s = doc.textPane.addStyle(null, null);

            for (final String element : styleSplit) {
                if (element.indexOf(kExposant) > -1)
                    StyleConstants.setSuperscript(s, true);
                if (element.indexOf(kIndice) > -1)
                    StyleConstants.setSubscript(s, true);
                if (element.indexOf(kCouleur) > -1)
                    StyleConstants.setForeground(s, obtenezCouleur(
                            element, Color.red));
                if (element.indexOf(kCouleurDeFond) > -1)
                    StyleConstants.setBackground(s, obtenezCouleur(
                            element, Color.green));
                if (element.indexOf(kItalique) > -1)
                    StyleConstants.setItalic(s, true);
                if (element.indexOf(kGras) > -1)
                    StyleConstants.setBold(s, true);
                if (element.indexOf(kSouligne) > -1)
                    StyleConstants.setUnderline(s, true);
                if (element.indexOf(kBarre) > -1)
                    StyleConstants.setStrikeThrough(s, true);
                if (!element.equals("")) {
                    doc.setCharacterAttributes(offset, longueur, s, false);
                }

            }
        }
    }

    private static Color obtenezCouleur(final String arg, Color result) {
        final Matcher m = COLOR_PATTERN.matcher(arg);
        if (m.matches()) {
            boolean error = false;
            final int[] color = new int[3];
            for (int j = 0; j < 3; j++) {
                final String value = m.group(j + 1);
                try {
                    if (value.startsWith("x"))
                        color[j] = Integer.parseInt(value.substring(1), 16);
                    else
                        color[j] = Integer.parseInt(value);
                } catch (final NumberFormatException e) {
                    color[j] = 0;
                    error = true;
                }
            }
            final Color c = new Color(color[0], color[1], color[2]);
            if (!(c.equals(Color.black) && error)) {
                result = c;
            }
        }

        return result;
    }
    
    public SimpleAttributeSet attStyle(final SimpleAttributeSet attorig) {
        SimpleAttributeSet att = attorig;
        JaxeElement jel;
        if (noeud.getNodeType() != Node.ELEMENT_NODE)
            jel = getParent();
        else
            jel = this;
        final Element ref = jel.refElement;
        if (ref == null)
            return (att);
        
        String style = doc.cfg.valeurParametreElement(ref, "style", null);
        if (this instanceof JEStyle)
            style = ((JEStyle) this).ceStyle;
        if (style != null) {
            if (att == null)
                att = new SimpleAttributeSet();

            final String[] styleSplit = style.split(";");

            for (final String element : styleSplit) {
                if (element.indexOf(kExposant) > -1)
                    StyleConstants.setSuperscript(att, true);
                if (element.indexOf(kIndice) > -1)
                    StyleConstants.setSubscript(att, true);
                if (element.indexOf(kCouleur) > -1)
                    StyleConstants.setForeground(att, obtenezCouleur(
                            element, Color.red));
                if (element.indexOf(kCouleurDeFond) > -1)
                    StyleConstants.setBackground(att, obtenezCouleur(
                            element, Color.green));
                if (element.indexOf(kItalique) > -1)
                    StyleConstants.setItalic(att, true);
                if (element.indexOf(kGras) > -1)
                    StyleConstants.setBold(att, true);
                if (element.indexOf(kSouligne) > -1)
                    StyleConstants.setUnderline(att, true);
                if (element.indexOf(kBarre) > -1)
                    StyleConstants.setStrikeThrough(att, true);
            }

        }

        if (att == null || !att.isDefined(StyleConstants.FontFamily)) {
            final String police = doc.cfg.valeurParametreElement(ref, "police", null);
            if (police != null) {
                if (att == null)
                    att = new SimpleAttributeSet();
                StyleConstants.setFontFamily(att, police);
            }
        }
        if (att == null || !att.isDefined(StyleConstants.FontSize)) {
            final String staille = doc.cfg.valeurParametreElement(ref, "taille", null);
            if (staille != null) {
                try {
                    final int taille = Integer.parseInt(staille);
                    if (att == null)
                        att = new SimpleAttributeSet();
                    StyleConstants.setFontSize(att, taille);
                } catch (final NumberFormatException ex) {
                    LOG.error("attStyle(SimpleAttributeSet) - " + ex.getClass().getName() + ": " + ex.getMessage(),
                            ex);
                }
            }
        }

        final JaxeElement jp = getParent();
        if (jp != null)
            return (jp.attStyle(att));
        return (att);
    }
    
    @Override
    public String toString() {
        String s = "Element: " + getClass().getName();
        s += "  noeud: ";
        if (noeud == null)
            s += "null";
        else
            s += noeud.getNodeName();
        s += "  debut: ";
        if (debut == null)
            s += "null";
        else
            s += debut.getOffset();
        s += "  fin: ";
        if (fin == null)
            s += "null";
        else
            s += fin.getOffset();
        return(s);
    }
}
