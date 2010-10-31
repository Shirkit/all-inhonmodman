/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import jaxe.elements.JECommentaire;
import jaxe.elements.JECData;
import jaxe.elements.JEDivision;
import jaxe.elements.JEEquation;
import jaxe.elements.JEFichier;
import jaxe.elements.JEInconnu;
import jaxe.elements.JEItem;
import jaxe.elements.JEListe;
import jaxe.elements.JEListeChamps;
import jaxe.elements.JESauf;
import jaxe.elements.JEString;
import jaxe.elements.JEStyle;
import jaxe.elements.JESymbole;
import jaxe.elements.JETable;
import jaxe.elements.JETableTexte;
import jaxe.elements.JEVide;
import jaxe.elements.JEZone;
import jaxe.elements.JETypeSimple;
import jaxe.elements.JEFormulaire;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Usine à éléments Jaxe
 */
public class JEFactory {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEFactory.class);
    
    private static final ClassLoader pluginClassLoader = getPluginClassLoader();
    
    /**
     * Chargeur de classes pour les plugins, utilisant tous les fichiers .jar dans le dossier plugins.
     * Utilise le chargeur de classes du contexte s'il n'y a pas de plugin.
     */
    public static ClassLoader getPluginClassLoader() {
        if (pluginClassLoader != null)
            return(pluginClassLoader);
        ClassLoader pcl = Thread.currentThread().getContextClassLoader();
        final File pluginDir = new File("plugins");
        try {
            if (pluginDir.exists() && pluginDir.isDirectory()) {
                final File[] pluginFiles = pluginDir.listFiles();
                final ArrayList<URL> jarURLsList = new ArrayList<URL>();
                for (final File pluginFile : pluginFiles)
                    if (pluginFile.getName().endsWith(".jar"))
                        jarURLsList.add(pluginFile.toURI().toURL());
                final URL[] jarURLs = jarURLsList.toArray(new URL[jarURLsList.size()]);
                pcl = new URLClassLoader(jarURLs, pcl);
            }
        } catch (final SecurityException ex) {
        } catch (final MalformedURLException ex) {
        }
        return(pcl);
    }
    
    /**
     * Création d'un JaxeElement à partir du type de balise (ignoré -
     * doc.cfg.typeBalise(eldef) est utilisé à la place), du document Jaxe,
     * de la définition de l'élément, et (pour une création à partir d'un élément DOM existant)
     * de l'élément DOM. el doit être null pour la création d'un nouvel élément.
     *
     * @deprecated     Utiliser createJE(doc, refElement, nom, typeNoeud, el) à la place
     */
    @Deprecated
    public static JaxeElement createJE(final String typebalise, final JaxeDocument doc, final Element eldef, final Element el) {
        return(createJE(doc, eldef, el));
    }
    
    /**
     * Création d'un JaxeElement à partir du type de balise, du document Jaxe,
     * de la définition de l'élément, et (pour une création à partir d'un élément DOM existant)
     * de l'élément DOM. el doit être null pour la création d'un nouvel élément.
     *
     * @deprecated     Utiliser createJE(doc, refElement, nom, typeNoeud, el) à la place
     */
    @Deprecated
    public static JaxeElement createJE(final JaxeDocument doc, final Element eldef, final Node el) {
        return(createJE(doc, doc.cfg.referenceElement(doc.cfg.nomBalise(eldef)), doc.cfg.nomBalise(eldef), doc.cfg.noeudtypeBalise(eldef), el));
    }
    
    /**
     * Création d'un JaxeElement à partir du type de balise, du document Jaxe,
     * de la référence de l'élément, et (pour une création à partir d'un élément DOM existant)
     * de l'élément DOM. el doit être null pour la création d'un nouvel élément.
     */
    public static JaxeElement createJE(final JaxeDocument doc, final Element refElement, final String nom,
            final String typeNoeud, final Node el) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("createJE(JaxeDocument, Element, String, String, Node) - doc="
                            + doc
                            + ", refElement="
                            + refElement
                            + ", nom="
                            + nom + ", typeNoeud=" + typeNoeud + ", el=" + el);
        }
        if (doc.cfg == null) {
            if ("instruction".equals(typeNoeud)) {
                JaxeElement newje = new JESauf(doc);
                ((JESauf)newje).setTarget(nom);
                return(newje);
            } else if ("commentaire".equals(typeNoeud))
                return(new JECommentaire(doc));
            else if ("cdata".equals(typeNoeud))
                return(new JECData(doc));
            else
                return(new JEInconnu(doc));
        }
        final String typeAffichage = doc.cfg.typeAffichageNoeud(refElement, nom, typeNoeud);
        JaxeElement newje;
        if ("plugin".equals(typeAffichage)) {
            final String classid = doc.cfg.valeurParametreElement(refElement, typeNoeud, nom, "classe", null);
            try {
                final Class<?> c = Class.forName(classid, true, pluginClassLoader);
                Constructor cons = null;
                try {
                    cons = c.getConstructor(JaxeDocument.class);
                } catch (final NoSuchMethodException ex) {
                    // cons sera null
                }
                if (cons != null) {
                    final Object[] initargs = new Object[1];
                    initargs[0] = doc;
                    newje = (JaxeElement) cons.newInstance(initargs);
                } else {
                    newje = (JaxeElement) c.newInstance();
                    newje.doc = doc;
                }
            } catch (final Exception ex) {
                LOG.error("JEFactory.createJE() - Plugin not found", ex);
                if (typeNoeud.equals("commentaire")) {
                    newje = new JECommentaire(doc);
                } else if (typeNoeud.equals("instruction")) {
                    newje = new JESauf(doc);
                    ((JESauf)newje).setTarget(nom);
                } else {
                    newje = new JEInconnu(doc);
                }
            }
        } else if (typeNoeud.equals("instruction")) {
            newje = new JESauf(doc);
            ((JESauf)newje).setTarget(nom);
        } else if (typeNoeud.equals("commentaire"))
            newje = new JECommentaire(doc);
        else if (typeNoeud.equals("cdata"))
            newje = new JECData(doc);
        else {
            if (typeAffichage.equals("division"))
                newje = new JEDivision(doc);
            else if (typeAffichage.equals("liste"))
                newje = new JEListe(doc);
            else if (typeAffichage.equals("listechamps"))
                newje = new JEListeChamps(doc);
            else if (typeAffichage.equals("item"))
                newje = new JEItem(doc);
            else if (typeAffichage.equals("tableau")) {
                if (el != null && el instanceof Element && JETable.preferreZone(doc, (Element) el))
                    newje = new JEZone(doc);
                else
                    newje = new JETable(doc);
            } else if (typeAffichage.equals("zone"))
                newje = new JEZone(doc);
            else if (typeAffichage.equals("string"))
                newje = new JEString(doc);
            else if (typeAffichage.equals("vide"))
                newje = new JEVide(doc);
            else if (typeAffichage.equals("fichier"))
                newje = new JEFichier(doc);
            else if (typeAffichage.equals("style"))
                newje = new JEStyle(doc);
            else if (typeAffichage.equals("symbole"))
                newje = new JESymbole(doc);
            else if (typeAffichage.equals("equation"))
                newje = new JEEquation(doc);
            else if (typeAffichage.equals("tabletexte"))
                newje = new JETableTexte(doc);
            else if (typeAffichage.equals("typesimple"))
                newje = new JETypeSimple(doc);
            else if (typeAffichage.equals("formulaire"))
                newje = new JEFormulaire(doc);
            else
                newje = new JEInconnu(doc);
        }
        newje.refElement = refElement;
        return(newje);
    }
}
