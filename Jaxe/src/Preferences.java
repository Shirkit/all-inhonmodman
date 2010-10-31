/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
// java.util.prefs.Preferences

import javax.swing.*;
import javax.swing.filechooser.FileFilter;


/**
 * Préférences:
 * 
 *     fenetreArbre
 *     fenetreInsertion
 *     fenetreAttributs
 *     navigateur
 *     consIndent
 *     iconeValide
 *     toujoursAfficherAttributs
 *     dictionnaire
 *     classeXSLT
 */
public class Preferences extends JDialog implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(Preferences.class);

    private static final String[] nomsPrefs = {"fenetreArbre", "fenetreInsertion", "fenetreAttributs",
        "navigateur", "consIndent", "iconeValide", "toujoursAfficherAttributs", "dictionnaire", "classeXSLT"};
    private static final String[] defPrefs = {"true", "true", "true",
        null, "false", "true", "true", null, "org.apache.xalan.processor.TransformerFactoryImpl"};
    private static Properties prefs = getPreferencesParDefaut();
    
    private final JCheckBox caseArbre;
    private final JCheckBox caseInsertion;
    private final JCheckBox caseAttributs;
    private final JLabel labelNav;
    private String prefNav;
    private final JCheckBox caseIndent;
    private final JCheckBox caseIconeValide;
    private final JCheckBox caseAfficherAttributs;
    private final JLabel labelDico;
    private String prefDico;
    private final JComboBox choixXSLT;
    private final static String[] classesXSLT = {
        "",
        "org.apache.xalan.processor.TransformerFactoryImpl",
        "org.apache.xalan.xsltc.trax.TransformerFactoryImpl",
        "net.sf.saxon.TransformerFactoryImpl"
    };
    private final static String[] nomsXSLT = {
        JaxeResourceBundle.getRB().getString("pref.defaut"),
        "Xalan",
        "XSLTC",
        "Saxon"
    };
    
    public static File trouverFichier() {
        try {
            final String userHome = System.getProperty("user.home");
            final String osName = System.getProperty("os.name");
            final File fichierPref;
            if (osName.indexOf("Windows") != -1)
                fichierPref = new File(userHome, "jaxepreferences");
            else if (osName.startsWith("Mac"))
                fichierPref = new File(userHome, "Library/Preferences/Jaxe Preferences");
            else
                fichierPref = new File(userHome, ".jaxe");
            return(fichierPref);
        } catch (final AccessControlException ex) {
            LOG.error("Jaxe Preferences.trouverFichier", ex);
            return(null);
        }
    }
    
    public static Properties getPreferencesParDefaut() {
        final Properties defauts = new Properties();
        for (int i=0; i<nomsPrefs.length; i++) {
            final String nom = nomsPrefs[i];
            final String valeur = defPrefs[i];
            if (valeur != null)
                defauts.setProperty(nom, valeur);
        }
        if (System.getProperty("os.name").startsWith("Mac"))
            defauts.setProperty("navigateur", "/Applications/Safari.app");
        return(defauts);
    }
    
    public static Properties chargerPref() {
        prefs = getPreferencesParDefaut();
        try {
            final java.util.prefs.Preferences juprefs = java.util.prefs.Preferences.userNodeForPackage(Jaxe.class);
            if (juprefs.get("fenetreArbre", null) != null) {
                for (String nomPref : nomsPrefs) {
                    final String valeur = juprefs.get(nomPref, null);
                    if (valeur != null)
                        prefs.setProperty(nomPref, valeur);
                }
            } else {
                try {
                    final File fpref = trouverFichier();
                    if (fpref != null && fpref.exists()) {
                        final FileInputStream fis = new FileInputStream(fpref);
                        prefs.load(fis);
                        fis.close();
                    }
                } catch (final IOException ex) {
                    LOG.error("chargerPref()", ex);
                    return(null);
                }
                enregistrerPref(prefs);
            }
        } catch (SecurityException ex) { // pour les applets
            LOG.error("chargerPref()", ex);
        }
        return(prefs);
    }
    
    public static Properties getPref() {
        return(prefs);
    }
    
    public static void enregistrerPref(final Properties prefs1) {
        if (prefs1 == null)
            return;
        prefs = prefs1;
        final java.util.prefs.Preferences juprefs = java.util.prefs.Preferences.userNodeForPackage(Jaxe.class);
        for (String nomPref : nomsPrefs) {
            final String valeur = prefs.getProperty(nomPref);
            if (valeur != null)
                juprefs.put(nomPref, valeur);
            else if (juprefs.get(nomPref, null) != null)
                juprefs.remove(nomPref);
        }
        try {
            juprefs.flush();
        } catch (BackingStoreException ex) {
            LOG.error("enregistrerPref(Properties)", ex);
        }
        /*
        try {
            final FileOutputStream fos = new FileOutputStream(fpref);
            prefs.store(fos, "Préférences de Jaxe");
            fos.close();
        } catch (final IOException ex) {
            LOG.error("enregistrerPref(Properties)", ex);
        }
        */
    }
    
    public Preferences(final JFrame jframe) {
        super(jframe, JaxeResourceBundle.getRB().getString("pref.Preferences"), true);
        this.getContentPane().setLayout(new BorderLayout());
        
        final Properties prefs = getPref();
        final String prefArbre = prefs.getProperty("fenetreArbre");
        final String prefInsertion = prefs.getProperty("fenetreInsertion");
        final String prefAttributs = prefs.getProperty("fenetreAttributs");
        prefNav = prefs.getProperty("navigateur");
        final String prefIndent = prefs.getProperty("consIndent");
        final String prefIconeValide = prefs.getProperty("iconeValide");
        final String prefAfficherAttributs = prefs.getProperty("toujoursAfficherAttributs");
        prefDico = prefs.getProperty("dictionnaire");
        final String prefClasseXSLT = prefs.getProperty("classeXSLT");
        
        final JPanel prefPanes = new JPanel();
        prefPanes.setLayout(new BoxLayout(prefPanes, BoxLayout.Y_AXIS));
        
        final JPanel fenPane = new JPanel();
        fenPane.setLayout(new BoxLayout(fenPane, BoxLayout.Y_AXIS));
        fenPane.setBorder(BorderFactory.createTitledBorder(
            JaxeResourceBundle.getRB().getString("pref.Fenetres")));
        caseArbre = new JCheckBox(
            JaxeResourceBundle.getRB().getString("pref.Arbre"));
        caseArbre.setSelected(Boolean.parseBoolean(prefArbre));
        fenPane.add(caseArbre);
        caseInsertion = new JCheckBox(
            JaxeResourceBundle.getRB().getString("pref.Insertion"));
        caseInsertion.setSelected(Boolean.parseBoolean(prefInsertion));
        fenPane.add(caseInsertion);
        caseAttributs = new JCheckBox(
            JaxeResourceBundle.getRB().getString("pref.Attributs"));
        caseAttributs.setSelected(Boolean.parseBoolean(prefAttributs));
        fenPane.add(caseAttributs);
        prefPanes.add(fenPane);
        fenPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        fenPane.setMaximumSize(new Dimension(Short.MAX_VALUE,Short.MAX_VALUE));
        
        final JPanel navPane = new JPanel(new FlowLayout());
        navPane.setBorder(BorderFactory.createTitledBorder(
            JaxeResourceBundle.getRB().getString("pref.Navigateur")));
        String nomNav;
        if (prefNav != null)
            nomNav = (new File(prefNav)).getName();
        else
            nomNav = "";
        labelNav = new JLabel(nomNav);
        navPane.add(labelNav);
        final JButton defNav = new JButton(
            JaxeResourceBundle.getRB().getString("pref.Definir"));
        defNav.addActionListener(this);
        defNav.setActionCommand("defNav");
        navPane.add(defNav);
        prefPanes.add(navPane);
        navPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        final JPanel enrPane = new JPanel(new FlowLayout());
        enrPane.setLayout(new BoxLayout(enrPane, BoxLayout.Y_AXIS));
        enrPane.setBorder(BorderFactory.createTitledBorder(
            JaxeResourceBundle.getRB().getString("pref.Affichage")));
        caseIndent = new JCheckBox(
            JaxeResourceBundle.getRB().getString("pref.consIndent"));
        caseIndent.setSelected(Boolean.parseBoolean(prefIndent));
        enrPane.add(caseIndent);
        caseIconeValide = new JCheckBox(
            JaxeResourceBundle.getRB().getString("pref.iconeValide"));
        caseIconeValide.setSelected(Boolean.parseBoolean(prefIconeValide));
        enrPane.add(caseIconeValide);
        caseAfficherAttributs = new JCheckBox(
            JaxeResourceBundle.getRB().getString("pref.toujoursAfficherAttributs"));
        caseAfficherAttributs.setSelected(Boolean.parseBoolean(prefAfficherAttributs));
        enrPane.add(caseAfficherAttributs);
        prefPanes.add(enrPane);
        enrPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        final JPanel dicoPane = new JPanel(new FlowLayout());
        dicoPane.setBorder(BorderFactory.createTitledBorder(
            JaxeResourceBundle.getRB().getString("pref.Dictionnaire")));
        String nomDico;
        if (prefDico != null) {
            nomDico = (new File(prefDico)).getName();
            final int pp = nomDico.lastIndexOf('.');
            if (pp != -1)
                nomDico = nomDico.substring(0, pp);
        } else
            nomDico = "";
        labelDico = new JLabel(nomDico);
        dicoPane.add(labelDico);
        final JButton defDico = new JButton(
            JaxeResourceBundle.getRB().getString("pref.Definir"));
        defDico.addActionListener(this);
        defDico.setActionCommand("defDico");
        dicoPane.add(defDico);
        prefPanes.add(dicoPane);
        dicoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        final JPanel xsltPane = new JPanel(new FlowLayout());
        xsltPane.setBorder(BorderFactory.createTitledBorder(
            JaxeResourceBundle.getRB().getString("pref.XSLT")));
        choixXSLT = new JComboBox(nomsXSLT);
        int indexXSLT = 0;
        for (int i=0; i<classesXSLT.length; i++) {
            if (classesXSLT[i].equals(prefClasseXSLT))
                indexXSLT = i;
        }
        choixXSLT.setSelectedIndex(indexXSLT);
        xsltPane.add(choixXSLT);
        prefPanes.add(xsltPane);
        xsltPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        this.getContentPane().add(prefPanes, BorderLayout.CENTER);
        
        final JPanel bPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        final JButton boutonAnnuler = new JButton(
            JaxeResourceBundle.getRB().getString("pref.Annuler"));
        boutonAnnuler.addActionListener(this);
        boutonAnnuler.setActionCommand("Annuler");
        bPane.add(boutonAnnuler);
        final JButton boutonOK = new JButton(
            JaxeResourceBundle.getRB().getString("pref.Enregistrer"));
        boutonOK.addActionListener(this);
        boutonOK.setActionCommand("Enregistrer");
        bPane.add(boutonOK);
        getRootPane().setDefaultButton(boutonOK);
        this.getContentPane().add(bPane, BorderLayout.SOUTH);
        this.pack();
        if (jframe != null) {
            final Rectangle r = jframe.getBounds();
            setLocation(r.x + r.width/4, r.y + r.height/4);
        } else {
            final Dimension dim = getSize();
            final Dimension ecran = getToolkit().getScreenSize();
            setLocation((ecran.width - dim.width)/2, (ecran.height - dim.height)/2);
        }
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        
        if ("Enregistrer".equals(cmd)) {
            final Properties prefs = getPref();
            
            prefs.setProperty("fenetreArbre", Boolean.toString(caseArbre.isSelected()));
            
            prefs.setProperty("fenetreInsertion", Boolean.toString(caseInsertion.isSelected()));
            
            prefs.setProperty("fenetreAttributs", Boolean.toString(caseAttributs.isSelected()));
             
            if (prefNav != null)
                prefs.setProperty("navigateur", prefNav);
            else if (prefs.getProperty("navigateur") != null)
                prefs.remove("navigateur");
            
            if (prefDico != null)
                prefs.setProperty("dictionnaire", prefDico);
            else if (prefs.getProperty("dictionnaire") != null)
                prefs.remove("dictionnaire");
            
            prefs.setProperty("consIndent", Boolean.toString(caseIndent.isSelected()));
            final String prefIconeValide = prefs.getProperty("iconeValide");
            prefs.setProperty("iconeValide", Boolean.toString(caseIconeValide.isSelected()));
            prefs.setProperty("toujoursAfficherAttributs", Boolean.toString(caseAfficherAttributs.isSelected()));
            
            prefs.setProperty("classeXSLT", classesXSLT[choixXSLT.getSelectedIndex()]);
            
            Preferences.enregistrerPref(prefs);
            
            if (Boolean.parseBoolean(prefIconeValide) != caseIconeValide.isSelected())
                for (final JaxeFrame jf : Jaxe.allFrames) {
                    if (jf.getTextPane() != null)
                        jf.getTextPane().setIconeValide(caseIconeValide.isSelected());
                }
            
        } else if ("defNav".equals(cmd)) {
            defNavigateur();
        } else if ("defDico".equals(cmd)) {
            defDictionnaire();
        }
        if ("Enregistrer".equals(cmd) || "Annuler".equals(cmd))
            setVisible(false);
    }
    
    public void defNavigateur() {
        String chemin = null;
        String nom = null;
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(JaxeResourceBundle.getRB().getString("pref.DefNavigateur"));
            final int resultat = chooser.showOpenDialog((Frame)getOwner());
            if (resultat == JFileChooser.APPROVE_OPTION) {
                final File f = chooser.getSelectedFile();
                chemin = f.getAbsolutePath();
                nom = f.getName();
            }
        } else {
            final FileDialog fdlg = new FileDialog((Frame)getOwner(),
                JaxeResourceBundle.getRB().getString("pref.DefNavigateur"), FileDialog.LOAD);
            fdlg.setVisible(true);
            String dir = fdlg.getDirectory();
            if (dir != null && dir.endsWith(File.separator))
                dir = dir.substring(0, dir.length()-1);
            nom = fdlg.getFile();
            if (dir == null)
                chemin = nom;
            else if (nom != null)
                chemin = dir + File.separator + nom;
        }
        if (chemin != null) {
            prefNav = chemin;
            labelNav.setText(nom);
        }
    }
    
    public void defDictionnaire() {
        String chemin = null;
        String nom = null;
        final String osName = System.getProperty("os.name");
        if (osName.indexOf("Linux") != -1 || osName.indexOf("Windows") != -1) {
            // FileDialog.setFilenameFilter() ne marche pas sur Windows -> on utilise JFileChooser à la place
            final JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir"), "dicos"));
            chooser.setDialogTitle(JaxeResourceBundle.getRB().getString("pref.Dictionnaire"));
            chooser.addChoosableFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f == null)
                        return(false);
                    if (f.isDirectory())
                        return(true);
                    return(f.getName().toLowerCase().endsWith(".dico"));
                }
                public String getDescription() {
                    return(".dico");
                }
            });
            final int resultat = chooser.showOpenDialog((Frame)getOwner());
            if (resultat == JFileChooser.APPROVE_OPTION) {
                final File f = chooser.getSelectedFile();
                chemin = f.getAbsolutePath();
                nom = f.getName();
            }
        } else {
            final FileDialog fdlg = new FileDialog((Frame)getOwner(),
                JaxeResourceBundle.getRB().getString("pref.Dictionnaire"), FileDialog.LOAD);
            fdlg.setFilenameFilter(new ExtFilter("dico"));
            fdlg.setDirectory(System.getProperty("user.dir") + File.separator + "dicos");
            fdlg.setVisible(true);
            String dir = fdlg.getDirectory();
            if (dir != null && dir.endsWith(File.separator))
                dir = dir.substring(0, dir.length()-1);
            nom = fdlg.getFile();
            if (dir == null)
                chemin = nom;
            else if (nom != null)
                chemin = dir + File.separator + nom;
        }
        if (chemin != null) {
            prefDico = chemin;
            if (nom != null) {
                final int pp = nom.lastIndexOf('.');
                if (pp != -1)
                    nom = nom.substring(0, pp);
            }
            labelDico.setText(nom);
        }
    }
    
    class ExtFilter implements FilenameFilter {
        String[] exta;
        public ExtFilter(final String ext) {
            exta = new String[1];
            exta[0] = ext;
        }
        public ExtFilter(final String[] exta) {
            this.exta = exta;
        }
        public boolean accept(final File dir, final String name) {
            for (final String element : exta)
                if (name.endsWith("." + element))
                    return(true);
            return(false);
        }
    }
}
