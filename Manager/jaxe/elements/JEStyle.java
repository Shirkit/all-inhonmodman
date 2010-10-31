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
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import jaxe.FonctionAjStyle;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Elément de style (B ou I ou SUB ou SUP). Modifie l'aspect du texte en conséquence.
 * Type d'élément Jaxe: 'style'
 * paramètre: style: GRAS | ITALIQUE | EXPOSANT | INDICE | SOULIGNE | BARRE |
 *                   PCOULEUR[###,###,###] | FCOULEUR[###,###,###]
 *            (plusieurs styles peuvent être combinés avec un caractère ';')
 *
 * NORMAL ne doit plus être utilisé (il faut utiliser FONCTION à la place, avec
 * classe="jaxe.FonctionNormal")
 */
public class JEStyle extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEStyle.class);

    public String ceStyle;
    public List<Element> _styles = new ArrayList<Element>();
    
    public JEStyle(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
        _styles.clear();
        final String valeurStyle = doc.cfg.valeurParametreElement(refElement, "style", null);
        if (valeurStyle == null)
            return;
        final StringBuilder styleBuilder = new StringBuilder();
        styleBuilder.append(valeurStyle);

        cutNode(noeud);
        _styles.add(0, (Element)noeud);
        
        Node node = noeud.getFirstChild();
        Node textnode = noeud.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.TEXT_NODE) {
                textnode = node;
            } else {
                _styles.add((Element)node);
                final Element refElement2 = doc.cfg.getElementRef((Element)node);
                if (refElement2 != null) {
                    final String style = doc.cfg.valeurParametreElement(refElement2, "style", null);
                    if (style != null) {
                        if (styleBuilder.length() > 0) {
                            styleBuilder.append(";");
                        }
                        styleBuilder.append(style);
                    }
                }
            }
            node = node.getFirstChild();
        }
        ceStyle = styleBuilder.toString();
        String texte = null;
        if (textnode != null) {
            texte = textnode.getNodeValue();
            final Node next = textnode.getNextSibling();
            while (next != null && next.getNodeType() == Node.TEXT_NODE) {
                texte = texte + next.getNodeValue();
            }
        }
            
        final int offsetdebut = pos.getOffset();
        Position newpos = pos;
        if (texte != null)
            newpos = insertText(newpos, texte);

        if (texte != null)
            changerStyle(ceStyle, offsetdebut, newpos.getOffset() - offsetdebut);
    }
    
    public String getText() {
        return getTextNode().getNodeValue();
    }

    private Node getTextNode() {
        Node n = noeud;
        while (n != null && n.getNodeType() != Node.TEXT_NODE) {
            n = n.getFirstChild();
        }
        return n;
    }
    
    /**
     * @param noeud
     */
    private void cutNode(final Node node) {
        int count = 1;
        Node child = node.getFirstChild();

        Node insNode = node;
        while (child != null) {
            cutNode(child);
            if (count > 1) {
                final Node add = child;
                child = child.getPreviousSibling();
                count--;
                final Node n = node.cloneNode(false);
                n.appendChild(add);
                insNode.getParentNode().insertBefore(n, insNode.getNextSibling());
                insNode = n;
            }
            count++;
            child = child.getNextSibling();
        }
        
    }

    @Override
    public Node nouvelElement(final Element refElement) {
        return null;
    }
    
    public static JEStyle nouveau(final JaxeDocument doc, final int start, final int end, final Element refElement) {
        final String ceStyle = doc.cfg.valeurParametreElement(refElement, "style", null);
        if (ceStyle == null || ceStyle.equals("")) {
            LOG.error("nouveau(JaxeDocument, int, int, Element) - Pas d'attribut param pour le style");
            return null;
        }
        
        if (doc.elementA(start) instanceof JEStyle || doc.elementA(start) != doc.elementA(end)) {
            final Element newel = nouvelElementDOM(doc, refElement);
            final FonctionAjStyle fct = new FonctionAjStyle(newel);
            if (fct.appliquer(doc, start, end)) {
                return null;
            }
            
        }
        
        JaxeElement p1 = doc.rootJE.elementA(start);
        JaxeElement p2 = doc.rootJE.elementA(end - 1);

        if (p1 == p2) {
            p1 = doc.rootJE.elementA(start);
            p2 = doc.rootJE.elementA(end - 1);
        }
        if (p1 != p2 || !(p1 instanceof JETexte))
            return null;
        
        try {
            final String texte = doc.textPane.getText(start, end-start);
            
            final JEStyle newje = new JEStyle(doc);
            
            final Node textnode = doc.DOMdoc.createTextNode(texte);
            final Element newel = nouvelElementDOM(doc, refElement);
            newel.appendChild(textnode);
            newje.noeud = newel;
            newje.refElement = refElement;
            newje.doc = doc;
            doc.dom2JaxeElement.put(newel, newje);
            
            newje.debut = doc.createPosition(start);
            newje.fin = doc.createPosition(end - 1);
            return newje;
        } catch (final BadLocationException ex) {
            LOG.error("nouveau(JaxeDocument, int, int, Element) - BadLocationException", ex);
            return null;
        }
    }
    
    @Override
    public JaxeElement couper(final Position pos) {
        final Node textNode = getTextNode();
        final String t = textNode.getNodeValue();
        final String t1 = t.substring(0, pos.getOffset() - debut.getOffset());
        final String t2 = t.substring(pos.getOffset() - debut.getOffset());
        textNode.setNodeValue(t1);
        
        Node firstNode = null;
        Element elem = null;
        final List<Element> styles = new ArrayList<Element>();
        for (final Element el : _styles) {
            if (elem != null) {
                final Element n = (Element) el.cloneNode(false);
                elem.appendChild(n);
                styles.add(n);
                elem = n;
            } else {
                elem = (Element)el.cloneNode(false);
                styles.add(elem);
                firstNode = elem;
            }
        }
        final Node textnode2 = doc.DOMdoc.createTextNode(t2);
        elem.appendChild(textnode2);
        final Node nextnode = noeud.getNextSibling();
        final JaxeElement parent = getParent();
        if (nextnode == null)
            parent.noeud.appendChild(firstNode);
        else
            parent.noeud.insertBefore(firstNode, nextnode);
        final JEStyle jst = new JEStyle(doc);
        jst.noeud = firstNode;
        jst.refElement = refElement;
        jst.doc = parent.doc;
        jst._styles = styles;
        jst.ceStyle = new String(ceStyle);
        try {
            jst.debut = doc.createPosition(pos.getOffset());
            jst.fin = fin;
            fin = doc.createPosition(pos.getOffset() - 1);
        } catch (final BadLocationException ex) {
            LOG.error("couper() - BadLocationException", ex);
        }
        doc.dom2JaxeElement.put(jst.noeud, jst);
        return (jst);
    }

    /**
     * fusionne cet élément avec celui donné, dans le DOM (aucun changement du
     * texte)
     */
    @Override
    public void fusionner(final JaxeElement el) {
        if (!(el instanceof JEStyle)) return;
        final JEStyle jes = (JEStyle) el;
        if (sameStyle(this, el)) {
            final Node textNode = getTextNode();
            if (noeud.getNextSibling() == el.noeud) {
                final String t = jes.getText();
                textNode.setNodeValue(textNode.getNodeValue() + t);
                fin = el.fin;
                el.getParent().supprimerEnfantDOM(el);
            } else if (el.noeud.getNextSibling() == noeud) {
                final String t = jes.getText();
                textNode.setNodeValue(t + textNode.getNodeValue());
                debut = el.debut;
                el.getParent().supprimerEnfantDOM(el);
            }
        }
    }
}
