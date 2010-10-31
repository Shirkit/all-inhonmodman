/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

/**
 * Fonction applicable au document XML, et lan�able � partir des menus.
 * Doit correspondre � un �l�ment FONCTION dans le fichier de configuration de Jaxe.
 *
 * Le constructeur peut avoir en param�tre "Element fctdef" pour avoir l'�l�ment correspondant
 * du fichier de config. Ceci permet ensuite d'appeler la m�thode de Config valeurParametreFonction()
 * pour obtenir les valeurs des param�tres.
 */
public interface Fonction {
    
    /**
     * Applique la fonction au document. La position de la s�lection du texte est donn�e par start et end.
     * start == end s'il n'y a pas de s�lection (dans ce cas c'est juste la position du curseur).
     */
    public boolean appliquer(JaxeDocument doc, int start, int end);
    
}
