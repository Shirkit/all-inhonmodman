/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
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
            // on essaye d'abord un constructeur avec "Element fctdef" en paramètre
            // pour permettre à la fonction d'obtenir les paramètres
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
            LOG.error("ActionFonction(JaxeDocument, String, String, Element) - Erreur à la création de : " + classe,
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
