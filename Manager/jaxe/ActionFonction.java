/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;

import javax.swing.text.TextAction;

import org.w3c.dom.Element;

public class ActionFonction extends TextAction {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(ActionFonction.class);

    Fonction fct;
    final JaxeDocument doc;

    public ActionFonction(final JaxeDocument doc, final String titre, final String classe) {
        super(titre);
        this.doc = doc;
        try {
            final Class c = Class.forName(classe, true, JEFactory.getPluginClassLoader());
            fct = (Fonction) c.newInstance();
        } catch (final Exception ex) {
            LOG.error("ActionFonction(JaxeDocument, String, String) - Erreur: Classe introuvable : " + classe, ex);
        }
    }
    
    public ActionFonction(final JaxeDocument doc, final String titre, final String classe, final Element fctdef) {
        super(titre);
        this.doc = doc;
        Class<?> c;
        try {
            c = Class.forName(classe, true, JEFactory.getPluginClassLoader());
        } catch (final Exception ex) {
            // cas des applets avec erreur RuntimePermission getClassLoader "access denied" : essai sans classloader
            try {
                c = Class.forName(classe);
            } catch (final Exception ex2) {
                LOG.error("ActionFonction(JaxeDocument, String, String, Element) - Erreur : Classe introuvable : " + classe,
                        ex2);
                return;
            }
        }
        try {
            // on essaye d'abord un constructeur avec "Element fctdef" en param�tre
            // pour permettre � la fonction d'obtenir les param�tres
            // on utilise le constructeur vide sinon
            Constructor cons = null;
            try {
                final Class[] parameterTypes = new Class[1];
                parameterTypes[0] = Element.class;
                cons = c.getConstructor(parameterTypes);
            } catch (final NoSuchMethodException ex) {
                // cons sera null
            }
            if (cons != null) {
                final Object[] initargs = new Object[1];
                initargs[0] = fctdef;
                fct = (Fonction) cons.newInstance(initargs);
            } else {
                fct = (Fonction) c.newInstance();
            }
        } catch (final Exception ex) {
            LOG.error("ActionFonction(JaxeDocument, String, String, Element) - Erreur � la cr�ation de : " + classe,
                    ex);
        }
    }
    
    public void actionPerformed(final ActionEvent e) {
        if (doc == null || doc.textPane == null || fct == null) {
            return;
        }
        if (getTextComponent(e) == null && doc.textPane != null) {
            doc.textPane.requestFocus();
        }
        doc.setModif(true);
        final int start = doc.textPane.getSelectionStart();
        final int end = doc.textPane.getSelectionEnd();
        fct.appliquer(doc, start, end);
    }
    
}
