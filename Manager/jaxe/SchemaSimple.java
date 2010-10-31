/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;


import java.util.*;

import org.w3c.dom.*;


/**
 * Schéma simplifié pour Jaxe (inclut dans les fichiers de config)
 */
public class SchemaSimple implements InterfaceSchema {
    
    private Config cfg;
    
    private final Element racine_schema; // élément racine du fichier de config
    
    private HashMap<String, Element> cacheDefElement; // cache des associations nom élément -> définition
    private HashMap<Element, String> cacheNomsElements; // cache des associations définition -> nom élément
    
    
    public SchemaSimple(final Element racine_schema, final Config cfg) {
        this.cfg = cfg;
        this.racine_schema = racine_schema;
        construireCacheDefElement();
    }
    
    /**
     * Renvoie true si la référence vient de ce schéma
     */
    public boolean elementDansSchema(final Element refElement) {
        final Document domdoc = refElement.getOwnerDocument();
        return(domdoc == racine_schema.getOwnerDocument());
    }
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom donné.
     */
    public Element referenceElement(final String nom) {
        return(cacheDefElement.get(nom));
    }
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom et l'espace de noms de l'élément passé en paramètre.
     */
    public Element referenceElement(final Element el) {
        final String nom;
        if (el.getPrefix() == null)
            nom = el.getNodeName();
        else
            nom = el.getLocalName();
        return(referenceElement(nom));
    }
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom et l'espace de noms de l'élément passé en paramètre,
     * et avec le parent dont la référence est passée en paramètre.
     */
    public Element referenceElement(final Element el, final Element refParent) {
        return(referenceElement(el));
    }
    
    /**
     * Renvoie le nom de l'élément dont la référence est donnée.
     */
    public String nomElement(final Element refElement) {
        return(cacheNomsElements.get(refElement));
    }
    
    /**
     * Renvoie l'espace de nom de l'élément dont la référence est passée en paramètre,
     * ou null si l'espace de noms n'est pas défini.
     */
    public String espaceElement(final Element refElement) {
        return(null);
    }
    
    /**
     * Renvoie la documentation d'un élément dont on donne la référence
     * (sous forme de texte simple, ou de HTML 3 pour faire des sauts de lignes)
     */
    public String documentationElement(final Element refElement) {
        return(null);
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un élément, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles ou si l'élément n'a pas un type simple.
     */
    public ArrayList<String> listeValeursElement(final Element refElement) {
        return(null);
    }
    
    /**
     * Renvoie le préfixe à utiliser pour créer un élément dont on donne la référence,
     * ou null s'il n'y en a pas.
     */
    public String prefixeElement(final Element refElement) {
        return(null);
    }
    
    /**
     * Renvoie la liste des espaces de noms (String) utilisés par ce schéma.
     */
    public ArrayList<String> listeEspaces() {
        return(null);
    }
    
    /**
     * Renvoie true si l'espace de nom est défini dans le schéma
     */
    public boolean aEspace(final String espace) {
        return(espace == null);
    }
    
    /**
     * Renvoie un préfixe à utiliser pour l'espace de noms donné, ou null si aucune suggestion n'est possible
     */
    public String prefixeEspace(final String espace) {
        return(null);
    }
    
    /**
     * Renvoie l'espace de noms cible du schéma (attribut targetNamespace avec WXS)
     */
    public String espaceCible() {
        return(null);
    }
    
    /**
     * Renvoie les références des éléments qui ne sont pas dans l'espace de noms passé en paramètre
     */
    public ArrayList<Element> listeElementsHorsEspace(final String espace) {
        if (espace == null)
            return(new ArrayList<Element>());
        else
            return(listeTousElements());
    }
    
    /**
     * Renvoie les références des éléments qui sont dans les espaces de noms passés en paramètre
     */
    public ArrayList<Element> listeElementsDansEspaces(final Set<String> espaces) {
        return(new ArrayList<Element>());
    }
    
    /**
     * Renvoie les références de tous les éléments du schéma
     */
    public ArrayList<Element> listeTousElements() {
        return(new ArrayList<Element>(cacheNomsElements.keySet()));
    }
    
    /**
     * Renvoit true si l'enfant est obligatoire sous le parent.
     */
    public boolean elementObligatoire(final Element refParent, final Element refEnfant) {
        return(false);
    }
    
    /**
     * Renvoit true si le parent peut avoir des enfants multiples avec la référence refEnfant.
     */
    public boolean enfantsMultiples(final Element refParent, final Element refEnfant) {
        return(true);
    }
    
    /**
     * Renvoie les références des éléments enfants de l'élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeSousElements(final Element refParent) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        final NodeList lsousel = refParent.getElementsByTagName("SOUS-ELEMENT");
        for (int i=0; i<lsousel.getLength(); i++) {
            final Element sousel = (Element)lsousel.item(i);
            liste.add(cacheDefElement.get(sousel.getAttribute("element")));
        }
        final NodeList lsousens = refParent.getElementsByTagName("SOUS-ENSEMBLE");
        for (int i=0; i<lsousens.getLength(); i++) {
            final Element sousens = (Element)lsousens.item(i);
            final String nomens = sousens.getAttribute("ensemble");
            final NodeList lens = racine_schema.getElementsByTagName("ENSEMBLE");
            for (int j=0; j<lens.getLength(); j++) {
                final Element ensemble = (Element)lens.item(j);
                if (nomens.equals(ensemble.getAttribute("nom")))
                    liste.addAll(listeSousElements(ensemble));
            }
        }
        return(liste);
    }
    
    /**
     * Expression régulière correspondant au schéma pour un élément parent donné
     * @param modevisu  True si on cherche une expression régulière à afficher pour l'utilisateur
     * @param modevalid  Pour obtenir une validation stricte au lieu de chercher si une insertion est possible
     */
    public String expressionReguliere(final Element refParent, final boolean modevisu, final boolean modevalid) {
        final ArrayList<Element> lsousb = listeSousElements(refParent);
        final StringBuilder expr = new StringBuilder();
        final int s = lsousb.size();
        for (int i=0; i < s; i++) {
            if (i != 0)
                expr.append("|");
            if (modevisu)
                expr.append(cfg.titreElement(lsousb.get(i)));
            else
                expr.append(nomElement(lsousb.get(i)));
            if (!modevisu)
                expr.append(",");
        }
        if (s != 0) {
            expr.insert(0, "(");
            expr.append(")*");
        }
        return(expr.toString());
    }
    
    /**
     * Renvoie la liste des références des parents possibles pour un élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeElementsParents(final Element refElement) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        if ("ELEMENT".equals(refElement.getNodeName())) {
            final NodeList lsousel = racine_schema.getElementsByTagName("SOUS-ELEMENT");
            for (int i=0; i<lsousel.getLength(); i++) {
                final Element sousel = (Element)lsousel.item(i);
                if (refElement.getAttribute("nom").equals(sousel.getAttribute("element"))) {
                    final Element parent = (Element)sousel.getParentNode();
                    if ("ELEMENT".equals(parent.getNodeName()))
                        liste.add(parent);
                    else if ("ENSEMBLE".equals(parent.getNodeName()))
                        liste.addAll(listeElementsParents(parent));
                }
            }
        } else if ("ENSEMBLE".equals(refElement.getNodeName())) {
            final String nomens = refElement.getAttribute("nom");
            final NodeList lsousens = racine_schema.getElementsByTagName("SOUS-ENSEMBLE");
            for (int i=0; i<lsousens.getLength(); i++) {
                final Element sousens = (Element)lsousens.item(i);
                if (nomens.equals(sousens.getAttribute("ensemble"))) {
                    final Element parent = (Element)sousens.getParentNode();
                    if ("ELEMENT".equals(parent.getNodeName()))
                        liste.add(parent);
                    else if ("ENSEMBLE".equals(parent.getNodeName()))
                        liste.addAll(listeElementsParents(parent));
                }
            }
        }
        return(liste);
    }
    
    /**
     * Renvoie la liste des références des attributs possibles pour un élément dont
     * on donne la référence en paramètre
     */
    public ArrayList<Element> listeAttributs(final Element refElement) {
        final NodeList latt = refElement.getElementsByTagName("ATTRIBUT");
        final ArrayList<Element> l = new ArrayList<Element>();
        addNodeList(l, latt);
        return(l);
    }
    
    /**
     * Renvoie le nom d'un attribut à partir de sa référence
     */
    public String nomAttribut(final Element refAttribut) {
        return(refAttribut.getAttribute("nom"));
    }
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de sa référence, ou null si aucun n'est défini
     */
    public String espaceAttribut(final Element refAttribut) {
        return(null);
    }
    
    /**
     * Renvoie la documentation d'un attribut à partir de sa référence
     */
    public String documentationAttribut(final Element refAttribut) {
        return(null);
    }
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de son nom complet (avec le préfixe s'il y en a un)
     */
    public String espaceAttribut(final String nomAttribut) {
        return(null);
    }
    
    /**
     * Renvoie true si un attribut est obligatoire, à partir de sa définition
     */
    public boolean estObligatoire(final Element refAttribut) {
        final String presence = refAttribut.getAttribute("presence");
        return("obligatoire".equals(presence));
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un attribut, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     */
    public ArrayList<String> listeValeursAttribut(final Element refAttribut) {
        final NodeList lval = refAttribut.getElementsByTagName("VALEUR");
        if (lval.getLength() == 0)
            return(null);
        final ArrayList<String> liste = new ArrayList<String>();
        for (int i=0; i<lval.getLength(); i++) {
            final Element val = (Element)lval.item(i);
            final String sval = val.getFirstChild().getNodeValue().trim();
            liste.add(sval);
        }
        return(liste);
    }
    
    /**
     * Renvoie la valeur par défaut d'un attribut dont la référence est donnée en paramètre
     */
    public String valeurParDefaut(final Element refAttribut) {
        return(null);
    }
    
    /**
     * Renvoie true si la valeur donnée est une valeur valide pour l'attribut
     */
    public boolean attributValide(final Element refAttribut, final String valeur) {
        if ((valeur == null || "".equals(valeur)) && estObligatoire(refAttribut))
            return(false);
        return(true);
    }
    
    /**
     * Renvoie la référence du premier élément parent d'un attribut à partir de sa référence
     */
    public Element parentAttribut(final Element refAttribut) {
        return((Element)refAttribut.getParentNode());
    }
    
    /**
     * Renvoie true si l'élément dont on donne la référence peut contenir du texte
     */
    public boolean contientDuTexte(final Element refElement) {
        final String texte  = refElement.getAttribute("texte");
        return("autorise".equals(texte));
    }
    
    /**
     * Renvoie la table hash par nom des définitions des éléments dans le fichier de config
     * (éléments ELEMENT)
     */
    private HashMap<String, Element> construireCacheDefElement() {
        cacheDefElement = new HashMap<String, Element>();
        cacheNomsElements = new HashMap<Element, String>();
        final NodeList lelements = racine_schema.getElementsByTagName("ELEMENT");
        for (int i=0; i<lelements.getLength(); i++) {
            final Element el = (Element)lelements.item(i);
            final String nom = el.getAttribute("nom");
            cacheDefElement.put(nom, el);
            cacheNomsElements.put(el, nom);
        }
        return(cacheDefElement);
    }
    
    /**
     * Ajoute tous les éléments d'une NodeList à une ArrayList de Element, en supposant que
     * tous les éléments de la NodeList sont des org.w3c.dom.Element.
     */
    private static void addNodeList(final ArrayList<Element> l, final NodeList nl) {
        for (int i=0; i<nl.getLength(); i++)
            l.add((Element)nl.item(i)); // attention au cast
    }
    
}
