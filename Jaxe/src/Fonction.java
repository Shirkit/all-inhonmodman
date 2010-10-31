/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

/**
 * Fonction applicable au document XML, et lançable à partir des menus.
 * Doit correspondre à un élément FONCTION dans le fichier de configuration de Jaxe.
 *
 * Le constructeur peut avoir en paramètre "Element fctdef" pour avoir l'élément correspondant
 * du fichier de config. Ceci permet ensuite d'appeler la méthode de Config valeurParametreFonction()
 * pour obtenir les valeurs des paramètres.
 */
public interface Fonction {
    
    /**
     * Applique la fonction au document. La position de la sélection du texte est donnée par start et end.
     * start == end s'il n'y a pas de sélection (dans ce cas c'est juste la position du curseur).
     */
    public boolean appliquer(JaxeDocument doc, int start, int end);
    
}
