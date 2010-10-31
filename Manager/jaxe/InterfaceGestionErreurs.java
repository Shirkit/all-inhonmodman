/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/
package jaxe;

import org.w3c.dom.Element;

/**
 * Le gestionnaire d'erreurs affiche les erreurs aux utilisateurs.
 * Il peut être remplacé pour une meilleure gestion d'erreurs, comme
 * avec l'affichage de bulles d'aides.
 */
public interface InterfaceGestionErreurs {
    /**
     * L'utilisateur a essayé d'ajouter un élément avant ou après la racine
     * @param refElement Référence vers l'élément que l'utilisateur a essayé d'ajouter
     */
    public void pasSousLaRacine(Element refElement);
    
    /**
     * L'utilisateur a essayé d'ajouter un élément dans un élément qui n'est pas éditable
     * @param parent L'élément Jaxe édité
     * @param refElement Référence vers l'élément que l'utilisateur a essayé d'ajouter
     */
    public void editionInterdite(JaxeElement parent, Element refElement);
    
    /**
     * Un enfant n'a pas été inséré parce-qu'il n'est pas autorisé sous le parent
     * @param refParent L'élément Jaxe du parent
     * @param defbalise Référence vers l'élément que l'utilisateur a essayé d'insérer
     */
    public void enfantInterditSousParent(JaxeElement parent, Element refElement);
    
    /**
     * L'enfant est interdit à cet endroit d'après l'expression régulière de l'élément parent.
     * @param expr Expression régulière de l'élément parent
     * @param parent L'élément Jaxe du parent
     * @param refElement Référence vers l'élément que l'utilisateur a essayé d'insérer
     */
    public void insertionImpossible(String expr, JaxeElement parent, Element refElement);
    
    /**
     * Le texte n'est pas autorisé sous cet élément
     * @param je L'élément Jaxe sous lequel le texte n'est pas autorisé
     */
    public void texteInterdit(JaxeElement parent);
}
