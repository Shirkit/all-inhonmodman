/*
Jaxe - Editeur XML en Java

Copyright (C) Lexis Nexis Deutschland, 2004

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import javax.swing.text.Position;


/**
 * Listener for EditEvents
 * @author Kykal
 */
public interface JaxeEditListenerIf {
    
    /**
     * Method is called before inserting JaxeElements into the document
     * @param e
     */
    public Position prepareAddedElement(Position pos);
    
    /**
     * Method is called when inserting JaxeElements into the document
     * @param e
     */
    public Position elementAdded(JaxeEditEvent e, Position pos);
    
    /**
     * Method is called when text is inserted into the document
     * @param e
     */
    public void textAdded(JaxeEditEvent e);
    
    /**
     * Method is called when JaxeElements are removed in the document
     * @param e
     */
    public void elementRemoved(JaxeEditEvent e);
    
    /**
     * Method is called when text is removed in the document
     * @param e
     */
    public void textRemoved(JaxeEditEvent e);
}
