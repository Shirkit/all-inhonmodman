/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;


import java.util.ArrayList;
import java.util.Set;

import org.w3c.dom.Element;


/**
 * Interface pour un langage de sch�ma, comme celui du W3C, Relax NG, ou les sch�mas simples de Jaxe.
 * Utilise la notion de "r�f�rence d'�l�ment" qui correspond � l'�l�ment du sch�ma qui d�finit l'�l�ment XML
 * (cela suppose que les sch�mas sont eux-m�mes des arbres XML).
 */
public interface InterfaceSchema {
    
    /**
     * Renvoie true si la r�f�rence vient de ce sch�ma
     */
    public boolean elementDansSchema(final Element refElement);
    
    /**
     * Renvoie la r�f�rence du premier �l�ment du sch�ma avec le nom donn�.
     */
    public Element referenceElement(final String nom);
    
    /**
     * Renvoie la r�f�rence du premier �l�ment du sch�ma avec le nom et l'espace de noms de l'�l�ment pass� en param�tre.
     */
    public Element referenceElement(final Element el);
    
    /**
     * Renvoie la r�f�rence du premier �l�ment du sch�ma avec le nom et l'espace de noms de l'�l�ment pass� en param�tre,
     * et avec le parent dont la r�f�rence est pass�e en param�tre.
     */
    public Element referenceElement(final Element el, final Element refParent);
    
    /**
     * Renvoie le nom de l'�l�ment dont la r�f�rence est donn�e.
     */
    public String nomElement(final Element refElement);
    
    /**
     * Renvoie l'espace de nom de l'�l�ment dont la r�f�rence est pass�e en param�tre,
     * ou null si l'espace de noms n'est pas d�fini.
     */
    public String espaceElement(final Element refElement);
    
    /**
     * Renvoie le pr�fixe � utiliser pour cr�er un �l�ment dont on donne la r�f�rence,
     * ou null s'il n'y en a pas.
     */
    public String prefixeElement(final Element refElement);
    
    /**
     * Renvoie la documentation d'un �l�ment dont on donne la r�f�rence
     * (sous forme de texte simple, ou de HTML 3 pour faire des sauts de lignes)
     */
    public String documentationElement(final Element refElement);
    
    /**
     * Renvoie la liste des valeurs possibles pour un �l�ment, � partir de sa r�f�rence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles ou si l'�l�ment n'a pas un type simple.
     */
    public ArrayList<String> listeValeursElement(final Element refElement);
    
    /**
     * Renvoie la liste des espaces de noms (String) utilis�s par ce sch�ma.
     */
    public ArrayList<String> listeEspaces();
    
    /**
     * Renvoie true si l'espace de nom est d�fini dans le sch�ma
     */
    public boolean aEspace(final String espace);
    
    /**
     * Renvoie un pr�fixe � utiliser pour l'espace de noms donn�, ou null si aucune suggestion n'est possible
     */
    public String prefixeEspace(final String ns);
    
    /**
     * Renvoie l'espace de noms cible du sch�ma (attribut targetNamespace avec WXS)
     */
    public String espaceCible();
    
    /**
     * Renvoie les r�f�rences des �l�ments qui ne sont pas dans l'espace de noms pass� en param�tre
     */
    public ArrayList<Element> listeElementsHorsEspace(final String espace);
    
    /**
     * Renvoie les r�f�rences des �l�ments qui sont dans les espaces de noms pass�s en param�tre
     */
    public ArrayList<Element> listeElementsDansEspaces(final Set<String> espaces);
    
    /**
     * Renvoie les r�f�rences de tous les �l�ments du sch�ma
     */
    public ArrayList<Element> listeTousElements();
    
    /**
     * Renvoit true si l'enfant est obligatoire sous le parent.
     */
    public boolean elementObligatoire(final Element refParent, final Element refEnfant);
    
    /**
     * Renvoit true si le parent peut avoir des enfants multiples avec la r�f�rence refEnfant.
     */
    public boolean enfantsMultiples(final Element refParent, final Element refEnfant);
    
    /**
     * Renvoie les r�f�rences des �l�ments enfants de l'�l�ment dont la r�f�rence est pass�e en param�tre
     */
    public ArrayList<Element> listeSousElements(final Element refParent);
    
    /**
     * Expression r�guli�re correspondant au sch�ma pour un �l�ment parent donn�
     * @param modevisu  True si on cherche une expression r�guli�re � afficher pour l'utilisateur
     * @param modevalid  Pour obtenir une validation stricte au lieu de chercher si une insertion est possible
     */
    public String expressionReguliere(final Element refParent, final boolean modevisu, final boolean modevalid);
    
    /**
     * Renvoie la liste des r�f�rences des parents possibles pour un �l�ment dont la r�f�rence est pass�e en param�tre
     */
    public ArrayList<Element> listeElementsParents(final Element refElement);
    
    /**
     * Renvoie la liste des r�f�rences des attributs possibles pour un �l�ment dont
     * on donne la r�f�rence en param�tre
     */
    public ArrayList<Element> listeAttributs(final Element refElement);
    
    /**
     * Renvoie le nom d'un attribut � partir de sa r�f�rence
     */
    public String nomAttribut(final Element refAttribut);
    
    /**
     * Renvoie l'espace de noms d'un attribut � partir de sa r�f�rence, ou null si aucun n'est d�fini
     */
    public String espaceAttribut(final Element refAttribut);
    
    /**
     * Renvoie la documentation d'un attribut � partir de sa r�f�rence
     */
    public String documentationAttribut(final Element refAttribut);
    
    /**
     * Renvoie l'espace de noms d'un attribut � partir de son nom complet (avec le pr�fixe s'il y en a un)
     */
    public String espaceAttribut(final String nomAttribut);
    
    /**
     * Renvoie true si un attribut est obligatoire, � partir de sa d�finition
     */
    public boolean estObligatoire(final Element refAttribut);
    
    /**
     * Renvoie la liste des valeurs possibles pour un attribut, � partir de sa r�f�rence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     */
    public ArrayList<String> listeValeursAttribut(final Element refAttribut);
    
    /**
     * Renvoie la valeur par d�faut d'un attribut dont la r�f�rence est donn�e en param�tre
     */
    public String valeurParDefaut(final Element refAttribut);
    
    /**
     * Renvoie true si la valeur donn�e est une valeur valide pour l'attribut
     */
    public boolean attributValide(final Element refAttribut, final String valeur);
    
    /**
     * Renvoie la r�f�rence du premier �l�ment parent d'un attribut � partir de sa r�f�rence
     */
    public Element parentAttribut(final Element refAttribut);
    
    /**
     * Renvoie true si l'�l�ment dont on donne la r�f�rence peut contenir du texte
     */
    public boolean contientDuTexte(final Element refElement);
    
}
