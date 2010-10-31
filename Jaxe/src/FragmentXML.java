/*
 Jaxe - Editeur XML en Java

 Copyright (C) 2008 Observatoire de Paris-Meudon

 Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

 Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

 Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
 */

package jaxe;

import org.w3c.dom.DocumentFragment;


public class FragmentXML extends Object {
    private DocumentFragment fragment;
    
    public FragmentXML(DocumentFragment fragment) {
        this.fragment = fragment;
    }
    
    public DocumentFragment getFragment() {
        return(fragment);
    }
    
    @Override
    public String toString() {
        return(JaxeDocument.DOMVersXML(fragment));
    }
}
