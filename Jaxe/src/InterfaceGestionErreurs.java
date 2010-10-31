/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/
package jaxe;

import org.w3c.dom.Element;

/**
 * Le gestionnaire d'erreurs affiche les erreurs aux utilisateurs.
 * Il peut �tre remplac� pour une meilleure gestion d'erreurs, comme
 * avec l'affichage de bulles d'aides.
 */
public interface InterfaceGestionErreurs {
    /**
     * L'utilisateur a essay� d'ajouter un �l�ment avant ou apr�s la racine
     * @param refElement R�f�rence vers l'�l�ment que l'utilisateur a essay� d'ajouter
     */
    public void pasSousLaRacine(Element refElement);
    
    /**
     * L'utilisateur a essay� d'ajouter un �l�ment dans un �l�ment qui n'est pas �ditable
     * @param parent L'�l�ment Jaxe �dit�
     * @param refElement R�f�rence vers l'�l�ment que l'utilisateur a essay� d'ajouter
     */
    public void editionInterdite(JaxeElement parent, Element refElement);
    
    /**
     * Un enfant n'a pas �t� ins�r� parce-qu'il n'est pas autoris� sous le parent
     * @param refParent L'�l�ment Jaxe du parent
     * @param defbalise R�f�rence vers l'�l�ment que l'utilisateur a essay� d'ins�rer
     */
    public void enfantInterditSousParent(JaxeElement parent, Element refElement);
    
    /**
     * L'enfant est interdit � cet endroit d'apr�s l'expression r�guli�re de l'�l�ment parent.
     * @param expr Expression r�guli�re de l'�l�ment parent
     * @param parent L'�l�ment Jaxe du parent
     * @param refElement R�f�rence vers l'�l�ment que l'utilisateur a essay� d'ins�rer
     */
    public void insertionImpossible(String expr, JaxeElement parent, Element refElement);
    
    /**
     * Le texte n'est pas autoris� sous cet �l�ment
     * @param je L'�l�ment Jaxe sous lequel le texte n'est pas autoris�
     */
    public void texteInterdit(JaxeElement parent);
}
