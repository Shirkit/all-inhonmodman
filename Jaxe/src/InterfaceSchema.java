/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;


import java.util.ArrayList;
import java.util.Set;

import org.w3c.dom.Element;


/**
 * Interface pour un langage de schéma, comme celui du W3C, Relax NG, ou les schémas simples de Jaxe.
 * Utilise la notion de "référence d'élément" qui correspond à l'élément du schéma qui définit l'élément XML
 * (cela suppose que les schémas sont eux-mêmes des arbres XML).
 */
public interface InterfaceSchema {
    
    /**
     * Renvoie true si la référence vient de ce schéma
     */
    public boolean elementDansSchema(final Element refElement);
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom donné.
     */
    public Element referenceElement(final String nom);
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom et l'espace de noms de l'élément passé en paramètre.
     */
    public Element referenceElement(final Element el);
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom et l'espace de noms de l'élément passé en paramètre,
     * et avec le parent dont la référence est passée en paramètre.
     */
    public Element referenceElement(final Element el, final Element refParent);
    
    /**
     * Renvoie le nom de l'élément dont la référence est donnée.
     */
    public String nomElement(final Element refElement);
    
    /**
     * Renvoie l'espace de nom de l'élément dont la référence est passée en paramètre,
     * ou null si l'espace de noms n'est pas défini.
     */
    public String espaceElement(final Element refElement);
    
    /**
     * Renvoie le préfixe à utiliser pour créer un élément dont on donne la référence,
     * ou null s'il n'y en a pas.
     */
    public String prefixeElement(final Element refElement);
    
    /**
     * Renvoie la documentation d'un élément dont on donne la référence
     * (sous forme de texte simple, ou de HTML 3 pour faire des sauts de lignes)
     */
    public String documentationElement(final Element refElement);
    
    /**
     * Renvoie la liste des valeurs possibles pour un élément, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles ou si l'élément n'a pas un type simple.
     */
    public ArrayList<String> listeValeursElement(final Element refElement);
    
    /**
     * Renvoie la liste des espaces de noms (String) utilisés par ce schéma.
     */
    public ArrayList<String> listeEspaces();
    
    /**
     * Renvoie true si l'espace de nom est défini dans le schéma
     */
    public boolean aEspace(final String espace);
    
    /**
     * Renvoie un préfixe à utiliser pour l'espace de noms donné, ou null si aucune suggestion n'est possible
     */
    public String prefixeEspace(final String ns);
    
    /**
     * Renvoie l'espace de noms cible du schéma (attribut targetNamespace avec WXS)
     */
    public String espaceCible();
    
    /**
     * Renvoie les références des éléments qui ne sont pas dans l'espace de noms passé en paramètre
     */
    public ArrayList<Element> listeElementsHorsEspace(final String espace);
    
    /**
     * Renvoie les références des éléments qui sont dans les espaces de noms passés en paramètre
     */
    public ArrayList<Element> listeElementsDansEspaces(final Set<String> espaces);
    
    /**
     * Renvoie les références de tous les éléments du schéma
     */
    public ArrayList<Element> listeTousElements();
    
    /**
     * Renvoit true si l'enfant est obligatoire sous le parent.
     */
    public boolean elementObligatoire(final Element refParent, final Element refEnfant);
    
    /**
     * Renvoit true si le parent peut avoir des enfants multiples avec la référence refEnfant.
     */
    public boolean enfantsMultiples(final Element refParent, final Element refEnfant);
    
    /**
     * Renvoie les références des éléments enfants de l'élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeSousElements(final Element refParent);
    
    /**
     * Expression régulière correspondant au schéma pour un élément parent donné
     * @param modevisu  True si on cherche une expression régulière à afficher pour l'utilisateur
     * @param modevalid  Pour obtenir une validation stricte au lieu de chercher si une insertion est possible
     */
    public String expressionReguliere(final Element refParent, final boolean modevisu, final boolean modevalid);
    
    /**
     * Renvoie la liste des références des parents possibles pour un élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeElementsParents(final Element refElement);
    
    /**
     * Renvoie la liste des références des attributs possibles pour un élément dont
     * on donne la référence en paramètre
     */
    public ArrayList<Element> listeAttributs(final Element refElement);
    
    /**
     * Renvoie le nom d'un attribut à partir de sa référence
     */
    public String nomAttribut(final Element refAttribut);
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de sa référence, ou null si aucun n'est défini
     */
    public String espaceAttribut(final Element refAttribut);
    
    /**
     * Renvoie la documentation d'un attribut à partir de sa référence
     */
    public String documentationAttribut(final Element refAttribut);
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de son nom complet (avec le préfixe s'il y en a un)
     */
    public String espaceAttribut(final String nomAttribut);
    
    /**
     * Renvoie true si un attribut est obligatoire, à partir de sa définition
     */
    public boolean estObligatoire(final Element refAttribut);
    
    /**
     * Renvoie la liste des valeurs possibles pour un attribut, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     */
    public ArrayList<String> listeValeursAttribut(final Element refAttribut);
    
    /**
     * Renvoie la valeur par défaut d'un attribut dont la référence est donnée en paramètre
     */
    public String valeurParDefaut(final Element refAttribut);
    
    /**
     * Renvoie true si la valeur donnée est une valeur valide pour l'attribut
     */
    public boolean attributValide(final Element refAttribut, final String valeur);
    
    /**
     * Renvoie la référence du premier élément parent d'un attribut à partir de sa référence
     */
    public Element parentAttribut(final Element refAttribut);
    
    /**
     * Renvoie true si l'élément dont on donne la référence peut contenir du texte
     */
    public boolean contientDuTexte(final Element refElement);
    
}
