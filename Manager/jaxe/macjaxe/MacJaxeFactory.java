/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.macjaxe;

import java.lang.reflect.Constructor;

/** Construit une instance de MacJaxe appropriée pour la plateforme */
public class MacJaxeFactory {
    private static MacJaxeFactory m_instance;
    
    /** singleton pattern */
    private MacJaxeFactory() {}
    
    /** singleton pattern */
    public static MacJaxeFactory getInstance() {
        synchronized(MacJaxeFactory.class) {
            if(m_instance == null) {
                m_instance = new MacJaxeFactory();
            }
        }
        return m_instance;
    }
    
    /** build a platform-specific MacJaxe */
    public MacJaxe buildMacJaxe()
    throws Exception
    {
        MacJaxe result = null;
        
        // possible class names in order of preference
        final String className [] = {
            "jaxe.macjaxe.macos.MacJaxeMacOS",
            "jaxe.macjaxe.generic.MacJaxeGeneric"
        };
        
        // use first class that is available
        // (assuming MacOS version will not be compiled in other environments)
        for (final String currentClassName : className) {
            try {
                final Class<?> mjimpl = Class.forName(currentClassName);
                //final Class[] tc = { Jaxe.class };
                //Constructor cons = mjimpl.getConstructor(tc);
                final Constructor cons = mjimpl.getConstructor((Class[])null);
                //final Object[] to = { owner };
                //result = (MacJaxe)cons.newInstance(to);
                result = (MacJaxe)cons.newInstance((Object[])null);
                
                // ok, if we get here we have our MacJaxe
                break;
            } catch(final ClassNotFoundException cnfe) {
                // TODO: better logging?
                //System.err.println("MacJaxeFactory: erreur au chargement de " + currentClassName + ": " + cnfe);
                continue;
            }
        }
        
        if(result == null) {
            throw new ClassNotFoundException(
                "MacJaxeFactory: classe MacJaxe introuvable, essayé: "
                + className
                );
        }
        
        return result;
    }
}
