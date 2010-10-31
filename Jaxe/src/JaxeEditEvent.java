/*
Jaxe - Editeur XML en Java

Copyright (C) Lexis Nexis Deutschland, 2004

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.util.EventObject;


/**
 * EditEvent for changes in the document
 * @author Kykal
 */
public class JaxeEditEvent extends EventObject {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JaxeEditEvent.class);

    /**
     * Creates an event with JaxeElements
     * @param source Eventsource
     * @param offs Offset in document
     * @param e JaxeElement
     */
    public JaxeEditEvent(final Object source,final JaxeElement e) {
        super(source);
        _offs = 0;
        _je = e;
        _text = null;
        _consume = false;
    }

    /**
     * Creates an event with plain text
     * @param source Eventsource
     * @param offs Offset in document
     * @param text Text
     */
    public JaxeEditEvent(final Object source, final int offs,final String text) {
        super(source);
        _offs = offs;
        _je = null;
        _text = text;
        _consume = false;
    }
    
    /**
     * Returns the JaxeElement or null if event is used with text
     * @return JaxeElement
     */
    public JaxeElement getJaxeElement() {
        return _je;
    }
    
    /**
     * Returns the offset in the document
     * @return Offset
     */
    public int getOffset() {
        return _offs;
    }
    
    /**
     * Status, if the event hast been uses;
     * @return true, if used
     */
    public boolean isConsumed() {
        return _consume;
    }
    
    /**
     * Sets the used status to true 
     */
    public void consume() {
        _consume = true;
    }
    
    /**
     * Returns the text or null is event is used with JaxeElement
     * @return Text
     */
    public String getText() {
        return _text;
    }
    
    /**
     * toString
     */
    @Override
    public String toString() {
        return getClass() + " je: " + _je + "  offset: " + _offs + "  text: " + _text;
    }
    
    /**
     * Offset in document
     */
    private final int _offs;
    /**
     * JaxeElement
     */
    private final JaxeElement _je;
    /**
     * Text
     */
    private final String _text;
    /**
     * Has event been used?
     */
    private boolean _consume;
}
