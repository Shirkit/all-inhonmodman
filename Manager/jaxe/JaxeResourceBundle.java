/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/
package jaxe;

import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * This class holds the ResourceBundle for Jaxe
 * @author tasche
 */
public class JaxeResourceBundle {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JaxeResourceBundle.class);

    /** The ResourceBundle for Jaxe */
    static ResourceBundle rb  = ResourceBundle.getBundle("jaxe/LocalStrings");

    /**
     * Gets the ResourceBundle
     * @return ResourceBundle
     */
    public static ResourceBundle getRB() {
       return rb;
    }

    /**
     * Sets the ResourceBundle
     * @param newrb the new ResourceBundle
     */
    public static void setRB(final ResourceBundle newrb) {
       rb = newrb;
    } 
}
