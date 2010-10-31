/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;

import jaxe.macjaxe.MacJaxe;
import jaxe.macjaxe.MacJaxeFactory;

/**
 * Classe de départ de Jaxe, avec la gestion des évènements de l'application
 */
public class Jaxe {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(Jaxe.class);

    // tous les objets Jaxe
    public static ArrayList<JaxeFrame> allFrames = new ArrayList<JaxeFrame>();
    
    //static String newline = System.getProperty("line.separator");
    static String newline = "\n";

    static String nomFichierCfg; // par défaut pour un nouveau document
    static MacJaxe mac = null;
    private static final Object lock = new Object();
    static DialogueDepart dlgDepart = null;
    private static Object resolver;
    
    
    /**
     * Nouvel objet identifiant l'application
     */
    public Jaxe(final String nomFichierCfg) {
        Jaxe.nomFichierCfg = nomFichierCfg;
        
        Preferences.chargerPref();
        
        if (mac == null) {
            try {
                mac = MacJaxeFactory.getInstance().buildMacJaxe();
            } catch(final Exception e) {
                // TODO better logging?
                LOG.error("Jaxe(String)", e);
            }
        }
        
        //resolver = new org.apache.xml.resolver.tools.CatalogResolver(); // reflection ici pour les applets
        try {
            final Class<?> catalogResolver = Class.forName("org.apache.xml.resolver.tools.CatalogResolver");
            final Constructor cons = catalogResolver.getConstructor((Class[])null);
            resolver = cons.newInstance((Object[])null);
        } catch (Exception ex) {
            resolver = null;
        }
    }
    
    // dans les 3 méthodes suivantes, jframe indique la fenêtre active
    // jframe est facultatif, et peut être null
    // si jframe est spécifié, elle peut être utilisée au lieu d'ouvrir une nouvelle fenêtre
    
    /**
     * Ouverture d'un nouveau document dans une frame particulière, ou dans une nouvelle frame si jframe==null
     */
    public static void nouveau(JaxeFrame jframe) {
        synchronized (lock) {
            if (jframe == null || (jframe.doc.getModif() || jframe.doc.fsave != null)) {
                jframe = new JaxeFrame();
                allFrames.add(jframe);
            }
            try {
                jframe.initNew(nomFichierCfg);
            } catch (final Exception ex) {
                LOG.error("nouveau(JaxeFrame) - Java bug 4353673", ex);
                //JOptionPane.showMessageDialog(jframe, "Java bug 4353673",
                //    "fatal Java bug", JOptionPane.ERROR_MESSAGE);
                
                // deuxième chance !
                jframe.fermer(true);
                jframe = new JaxeFrame();
                allFrames.add(jframe);
                jframe.initNew(nomFichierCfg);
            }
        }
    }
    
    /**
     * Dialogue de choix d'une configuration pour créer un nouveau document
     */
    public static void dialogueNouveau(final JaxeFrame jframe) {
        final File configdir = new File("config");
        int nbconf = 0;
        final String[] lfichiers = configdir.list();
        if (lfichiers == null) {
            LOG.error("dialogueNouveau(JaxeFrame) - " + JaxeResourceBundle.getRB().getString("erreur.DossierConfig"),
                    null);
            return;
        }
        for (final String element : lfichiers)
            if (element.endsWith("cfg.xml") || element.endsWith("config.xml"))
                nbconf++;
        if (nbconf == 0)
            LOG.error("dialogueNouveau(JaxeFrame) - " + JaxeResourceBundle.getRB().getString("config.AucunFichier"),
                    null);
        if (nbconf == 1) {
            nouveau(jframe);
            if (dlgDepart != null)
                finDialogueDepart();
        } else {
            final DialogueNouveau dlgNouveau = new DialogueNouveau(jframe);
            // on ferme le dialogue modal pour éviter un bug sous MacOS X avec les menus grisés
            if (dlgDepart != null)
                finDialogueDepart();
            dlgNouveau.setVisible(true);
            if (dlgNouveau.annulation() && allFrames.size() == 0)
                dialogueDepart();
        }
    }
    
    /**
     * Dialogue Nouveau/Ouvrir/Quitter au lancement de l'application
     */
    public static void dialogueDepart() {
        dlgDepart = new DialogueDepart();
        dlgDepart.setVisible(true);
    }
    
    public static void finDialogueDepart() {
        if (dlgDepart != null) {
            dlgDepart.setVisible(false);
            dlgDepart = null;
        }
    }
    
    /**
     * Ouverture d'un fichier XML dans une frame particulière, ou dans une nouvelle frame si jframe==null
     */
    public static void ouvrir(final File f, final JaxeFrame jframe) {
        ouvrirAvecConf(f, null, jframe);
    }
    
    /**
     * Ouverture d'un fichier XML avec une configuration particulière,
     * dans une frame particulière ou dans une nouvelle frame si jframe==null
     */
    public static void ouvrirAvecConf(final File f, final File fconf, JaxeFrame jframe) {
        synchronized (lock) {
            if (f == null)
                return;
            if (dlgDepart != null)
                finDialogueDepart();
            for (final JaxeFrame ji : allFrames) {
                if (ji.doc != null && f.equals(ji.doc.fsave)) {
                    ji.toFront();
                    return;
                }
            }
            if (jframe == null)
                jframe = premiereFrame();
            if (jframe == null || (jframe.doc != null && (jframe.doc.getModif() || jframe.doc.fsave != null))) {
                jframe = new JaxeFrame();
                allFrames.add(jframe);
            }
            jframe.ouvrirAvecConf(f, fconf);
        }
    }
    
    /**
     * Ouverture d'un nouveau document avec la configuration spécifiée
     */
    public static void ouvrirConf(final File f, final JaxeFrame jframe) {
        nomFichierCfg = f.getPath();
        nouveau(jframe);
    }
    
    /**
     * Quitte l'application
     */
    public static void quitter() {
        boolean vafermer = true;
        final ArrayList<JaxeFrame> frames = new ArrayList<JaxeFrame>(allFrames);
        Collections.reverse(frames);
        for (final JaxeFrame ji : frames) {
            if (!ji.fermer(true))
                vafermer = false;
        }
        if (vafermer)
            System.exit(0);
    }
    
    /**
     * Dialogue "à propos" de l'application
     */
    public static void aPropos() {
        final JaxeFrame jframe = premiereFrame();
        if (jframe != null)
            jframe.getJaxeMenuBar().doAbout();
        else {
            final AboutBox aboutBox = new AboutBox(null);
            aboutBox.setResizable(false);
            aboutBox.setVisible(true);
        }
    }
    
    /**
     * Dialogue des préférences de l'application
     */
    public static void preferences() {
        final JaxeFrame jframe = premiereFrame();
        if (jframe != null)
            jframe.getJaxeMenuBar().doPreferences();
        else {
            final Preferences prefs = new Preferences(null);
            prefs.setVisible(true);
        }
    }
    
    /**
     * Renvoit la première fenêtre ouverte (pas forcément celle au premier plan)
     */
    public static JaxeFrame premiereFrame() {
        if (allFrames.size() == 0)
            return(null);
        return(allFrames.get(0));
    }
    
    public static EntityResolver getEntityResolver() {
        return((EntityResolver)resolver);
    }
    
    public static URIResolver getURIResolver() {
        return((URIResolver)resolver);
    }
    
    /**
     * Méthode principale
     */
    public static void main(final String args[]) {
        if (System.getProperty("jaxe.config") != null) { // lancement sans argument
            new Jaxe(System.getProperty("jaxe.config"));
            nouveau(null);
        } else if (args.length == 1 && (args[0].endsWith("cfg.xml") || args[0].endsWith("config.xml"))) { // ouverture config
            new Jaxe(args[0]);
            nouveau(null);
        } else if (args.length == 2 && (args[1].endsWith("cfg.xml") || args[1].endsWith("config.xml"))) { // ouverture fichier avec config
            new Jaxe(args[1]);
            ouvrirAvecConf(new File(args[0]), new File(args[1]), null);
        } else { // ouverture d'un fichier sans préciser la config
            final File dir = new File("config");
            if (!dir.exists()) {
                JOptionPane.showMessageDialog(null, JaxeResourceBundle.getRB().getString("erreur.DossierConfig"),
                    JaxeResourceBundle.getRB().getString("config.ErreurLancement"), JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            int nbconf = 0;
            final File[] liste = dir.listFiles();
            for (final File element : liste)
                if (element.getName().endsWith("cfg.xml") || element.getName().endsWith("config.xml")) {
                    if (nbconf == 0)
                        new Jaxe(element.getPath());
                    nbconf++;
                }
            if (nbconf == 0) {
                JOptionPane.showMessageDialog(null,
                    JaxeResourceBundle.getRB().getString("config.AucunFichier"),
                    JaxeResourceBundle.getRB().getString("config.ErreurLancement"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            if (args.length > 0)
                ouvrir(new File(args[0]), null);
            else if (nbconf > 1) {
                synchronized (lock) { // synchronisation avec MacJaxe.ouvrir
                    if (allFrames.size() == 0)
                        dialogueDepart();
                }
            } else if (nbconf == 1)
                nouveau(null);
        }
    }

}
