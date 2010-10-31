/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryDichoDisk;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.swing.JTextComponentSpellChecker;

import org.w3c.dom.Element;


/**
 * Barre de menus de Jaxe et actions associées
 */
public class JaxeMenuBar extends JMenuBar implements ActionListener, EcouteurMAJ {
    /**
     * Logger for this class
     */
    static final Logger LOG = Logger.getLogger(JaxeMenuBar.class);

    static ResourceBundle rb;
    
    private AboutBox aboutBox;
    
    private JMenu fileMenu;
    private JMenuItem miNew;
    private JMenuItem miOpen;
    private JMenuItem miOpenConf;
    private JMenuItem miClose;
    private JMenuItem miSave;
    private JMenuItem miSaveAs;
    private JMenuItem miPrint;
    private JMenuItem miPref;
    private JMenuItem miQuitter;
    private JMenuItem[] menusExport;
    
    private JMenu editMenu;
    private JMenuItem miUndo;
    private JMenuItem miRedo;
    private JMenuItem miCut;
    private JMenuItem miCopy;
    private JMenuItem miPaste;
    private JMenuItem miSelectAll;
    private JMenuItem miFind;
    private JMenuItem miAgain;
    private JMenuItem miSpelling;
    
    private JMenu windowMenu;
    private JMenuItem miHTML;
    private JMenuItem miValider;
    private JMenu helpMenu;

    //private JRadioButtonMenuItem sideMenu;
    private JRadioButtonMenuItem menuArbre;
    private JRadioButtonMenuItem menuAllowed;
    private JRadioButtonMenuItem menuAttributs;

    private static int cmdMenu;
    
    private TextAction aColler;
    private TextAction aCopier;
    private TextAction aCouper;
    UndoAction undoAction;
    private final RedoAction redoAction;
    
    JaxeFrame jaxeframe;
    JFrame jframe;
    
    static File dernierRepertoire = null;
    
    
    public JaxeMenuBar(final JaxeFrame jaxeframe) {
        super();
        this.jaxeframe = jaxeframe;
        jframe = jaxeframe;
        rb = JaxeResourceBundle.getRB();
        cmdMenu = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        aboutBox = null;
        undoAction = new UndoAction();
        redoAction = new RedoAction();
        addMenus();
    }
    
    /**
     * Menubar for a jframe associated to a jaxeframe
     */
    public JaxeMenuBar(final JFrame jframe, final JaxeFrame jaxeframe) {
        super();
        this.jaxeframe = jaxeframe;
        this.jframe = jframe;
        rb = JaxeResourceBundle.getRB();
        cmdMenu = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        aboutBox = null;
        undoAction = new UndoAction();
        redoAction = new RedoAction();
        addMenus();
    }
    
    public void addMenus() {
        addFileMenuItems();
        addEditMenuItems();
        addWindowMenuItems();
        if(!System.getProperty("os.name").startsWith("Mac OS"))
            addHelpMenuItems();
    }

    public void addFileMenuItems() {
        fileMenu = new JMenu(rb.getString("menus.Fichier"));
        
        miNew = new JMenuItem (rb.getString("menus.Nouveau"));
        miNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, cmdMenu));
        fileMenu.add(miNew).setEnabled(true);
        miNew.addActionListener(this);

        miOpen = new JMenuItem (rb.getString("menus.Ouvrir"));
        miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, cmdMenu));
        fileMenu.add(miOpen).setEnabled(true);
        miOpen.addActionListener(this);
        
        miOpenConf = new JMenuItem (rb.getString("menus.OuvrirConf"));
        fileMenu.add(miOpenConf).setEnabled(true);
        miOpenConf.addActionListener(this);
        
        miClose = new JMenuItem (rb.getString("menus.Fermer"));
        miClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, cmdMenu));
        fileMenu.add(miClose).setEnabled(true);
        miClose.addActionListener(this);
        
        miSave = new JMenuItem (rb.getString("menus.Enregistrer"));
        miSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, cmdMenu));
        fileMenu.add(miSave).setEnabled(true);
        miSave.addActionListener(this);
        
        miSaveAs = new JMenuItem (rb.getString("menus.EnregistrerSous"));
        miSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | cmdMenu));
        fileMenu.add(miSaveAs).setEnabled(true);
        miSaveAs.addActionListener(this);
        
        miPrint = new JMenuItem (rb.getString("menus.Imprimer"));
        fileMenu.add(miPrint).setEnabled(true);
        miPrint.addActionListener(this);
        
        menusExport = null;
        
        if(!System.getProperty("os.name").startsWith("Mac OS")) {
            miPref = new JMenuItem (rb.getString("menus.Preferences"));
            fileMenu.add(miPref).setEnabled(true);
            miPref.addActionListener(this);
            
            miQuitter = new JMenuItem (rb.getString("menus.Quitter"));
            miQuitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, cmdMenu));
            fileMenu.add(miQuitter).setEnabled(true);
            miQuitter.addActionListener(this);
        }
        
        add(fileMenu);
    }
    
    
    public void addEditMenuItems() {
        editMenu = new JMenu(rb.getString("menus.Edition"));
        
        miUndo = editMenu.add(undoAction);
        miUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, cmdMenu));

        miRedo = editMenu.add(redoAction);
        miRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, cmdMenu));

        editMenu.addSeparator();
        
        aCouper = new ActionCouper();
        miCut = editMenu.add(aCouper);
        miCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, cmdMenu));
        if (jframe != jaxeframe)
            miCut.setEnabled(false);
        aCopier = new ActionCopier();
        miCopy = editMenu.add(aCopier);
        miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, cmdMenu));
        aColler = new ActionColler();
        miPaste = editMenu.add(aColler);
        miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, cmdMenu));
        if (jframe != jaxeframe)
            miPaste.setEnabled(false);
        miSelectAll = editMenu.add(new ActionToutSelectionner());
        miSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, cmdMenu));
        
        editMenu.addSeparator();

        miFind = editMenu.add(new ActionRechercher());
        miFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, cmdMenu));
        if (jframe instanceof ValidationFrame)
            miFind.setEnabled(false);
        miAgain = editMenu.add(new ActionSuivant());
        miAgain.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, cmdMenu));
        if (jframe instanceof ValidationFrame)
            miAgain.setEnabled(false);
        
        editMenu.addSeparator();
        
        miSpelling = editMenu.add(new ActionOrthographe());
        
        /*
        // ne marche pas avec un clavier AZERTY alors que ça devrait marcher, cf Java bug 4199399
        // bizarrement ça marche si on met VK_PERIOD au lieu de VK_COLON, mais l'affichage est incorrect
        if (System.getProperty("os.name").startsWith("Mac OS"))
            miSpelling.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, cmdMenu));
        */
        
        add(editMenu);
    }
    
    public void addWindowMenuItems() {
        windowMenu = new JMenu(rb.getString("menus.Fenetres"));
        
        miHTML = new JMenuItem(new ShowWindowAction(rb.getString("menus.FenetreHTML")));
        windowMenu.add(miHTML);
        windowMenu.add(new ShowWindowAction(rb.getString("menus.FenetreXML"))) ;
        miValider = new JMenuItem(new ShowWindowAction(rb.getString("menus.Validation")));
        windowMenu.add(miValider) ;
        windowMenu.add(new ShowWindowAction(rb.getString("menus.Source"))) ;
        
       // bidouille pour avoir un JCheckBoxMenuItem qui fonctionne : on le simule avec un JRadioButtonMenuItem
       // voir : http://developer.apple.com/qa/qa2001/qa1154.html
        UIManager.put("RadioButtonMenuItem.checkIcon", UIManager.get("CheckBoxMenuItem.checkIcon")) ;
        
        //menuSide = new JRadioButtonMenuItem(rb.getString("menus.FenetreSide"),true) ;
        //menuSide.addActionListener(this) ;
        //windowMenu.add(menuSide) ;
        
        menuArbre = new JRadioButtonMenuItem(rb.getString("menus.FenetreArbre"),
            jaxeframe.getAffichageArbre()) ;
        menuArbre.addActionListener(this) ;
        windowMenu.add(menuArbre) ;
        
        menuAllowed = new JRadioButtonMenuItem(rb.getString("menus.FenetreAllowed"),
            jaxeframe.getAffichageAllowed()) ;
        menuAllowed.addActionListener(this) ;
        windowMenu.add(menuAllowed) ;
        
        menuAttributs = new JRadioButtonMenuItem(rb.getString("menus.FenetreAttributs"),
            jaxeframe.getAffichageAttributs()) ;
        menuAttributs.addActionListener(this) ;
        windowMenu.add(menuAttributs) ;
        
        add(windowMenu) ;
    }
    
    public void addHelpMenuItems() {
        helpMenu = new JMenu("?");
        helpMenu.add(new AboutAction(rb.getString("menus.APropos")));
        add(helpMenu);
    }
    
    public void actionPerformed(final ActionEvent newEvent) {
        if (newEvent.getActionCommand().equals(miNew.getActionCommand()))
            doNew();
        else if (newEvent.getActionCommand().equals(miOpen.getActionCommand()))
            doOpen();
        else if (newEvent.getActionCommand().equals(miOpenConf.getActionCommand()))
            doOpenConf();
        else if (newEvent.getActionCommand().equals(miClose.getActionCommand()))
            doClose(false);
        else if (newEvent.getActionCommand().equals(miSave.getActionCommand()))
            doSave();
        else if (newEvent.getActionCommand().equals(miSaveAs.getActionCommand()))
            doSaveAs();
        else if (newEvent.getActionCommand().equals(miPrint.getActionCommand()))
            doPrint();
        else if (miPref != null && newEvent.getActionCommand().equals(miPref.getActionCommand()))
            doPreferences();
        else if (miQuitter != null && newEvent.getActionCommand().equals(miQuitter.getActionCommand()))
            doQuitter();
        
        //else if (newEvent.getActionCommand().equals(menuSide.getActionCommand()))
        //    jaxeframe.setAffichageSide(!jaxeframe.getAffichageSide());
        else if (newEvent.getActionCommand().equals(menuArbre.getActionCommand()))
            jaxeframe.setAffichageArbre(!jaxeframe.getAffichageArbre());
        else if (newEvent.getActionCommand().equals(menuAllowed.getActionCommand()))
            jaxeframe.setAffichageAllowed(!jaxeframe.getAffichageAllowed());
        else if (newEvent.getActionCommand().equals(menuAttributs.getActionCommand()))
            jaxeframe.setAffichageAttributs(!jaxeframe.getAffichageAttributs());
    }
    
    // menu Nouveau
    public void doNew() {
        Jaxe.dialogueNouveau(jaxeframe);
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
    
    /**
     * Filtre pour JFileChooser sélectionnant les fichiers avec l'extension .xml.
     * javax.swing.filechooser.FileNameExtensionFilter pourra remplacer ça, mais c'est du Java 1.6
     */
    class XMLFileFilter extends FileFilter {
        public boolean accept(File f) {
            if (f == null)
                return(false);
            if (f.isDirectory())
                return(true);
            return(f.getName().toLowerCase().endsWith(".xml"));
        }
        public String getDescription() {
            return(rb.getString("ouverture.FichiersXML"));
        }
    }
    
    public void doOpen() {
        /* JFileChooser: avantage: choix affichage fichiers XML/tous les fichiers sur Windows (pas de pb sur Mac)
           FileDialog: avantage: dialogue standard du système mais très moche sur Linux
           => utilisation de JFileChooser sur Linux, FileDialog sur Mac et Windows
        */
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            final JFileChooser chooser = new JFileChooser(dernierRepertoire);
            final int resultat = chooser.showOpenDialog(jframe);
            if (resultat == JFileChooser.APPROVE_OPTION) {
                dernierRepertoire = chooser.getCurrentDirectory();
                final File theFile = chooser.getSelectedFile();
                Jaxe.ouvrir(theFile, jaxeframe);
            }
        } else {
            final FileDialog fd = new FileDialog(jframe);
            fd.setVisible(true);
            final String sf = fd.getFile();
            if (sf != null) {
                final File theFile = new File(fd.getDirectory(), sf);
                Jaxe.ouvrir(theFile, jaxeframe);
            }
        }
    }
    
    public void doOpenConf() {
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir") + File.separator + "config");
            final FileFilter xmlFilter = new XMLFileFilter();
            chooser.addChoosableFileFilter(xmlFilter);
            final int resultat = chooser.showOpenDialog(jframe);
            if (resultat == JFileChooser.APPROVE_OPTION) {
                final File theFile = chooser.getSelectedFile();
                Jaxe.ouvrirConf(theFile, jaxeframe);
            }
        } else {
            final FileDialog fd = new FileDialog(jframe);
            fd.setFilenameFilter(new ExtFilter("xml"));
            fd.setDirectory(System.getProperty("user.dir") + File.separator + "config");
            fd.setVisible(true);
            final String sf = fd.getFile();
            if (sf != null) {
                final File theFile = new File(fd.getDirectory(), sf);
                Jaxe.ouvrirConf(theFile, jaxeframe);
            }
        }
    }
    
    public boolean doClose(final boolean quit) {
        if (jframe == jaxeframe)
            jaxeframe.fermer(quit);
        else
            jframe.setVisible(false);
        return(true);
    }
    
    public void doSave() {
        jaxeframe.enregistrer();
    }
    
    public void doSaveAs() {
        jaxeframe.enregistrerSous();
    }
    
    public void doPrint() {
        if (jframe == jaxeframe)
            jaxeframe.imprimer();
        else if (jframe instanceof SourceFrame)
            ((SourceFrame)jframe).imprimer();
        else if (jframe instanceof ValidationFrame)
            ((ValidationFrame)jframe).imprimer();
    }
    
    public void doQuitter() {
        Jaxe.quitter();
    }
    
    public void setActivationMenuValider(final boolean actif) {
        miValider.setEnabled(actif);
    }
    
    public void setActivationMenuHTML(final boolean actif) {
        miHTML.setEnabled(actif);
    }
    
    public void majExports(final JaxeDocument doc) {
        if (doc.cfg == null)
            return;
        if (menusExport != null) {
            for (final JMenuItem mi : menusExport)
                fileMenu.remove(mi);
            fileMenu.remove(6);
            fileMenu.remove(6);
        }
        final ArrayList<Element> exports = doc.cfg.listeExports("XML");
        final ArrayList<Element> exportsPDF = doc.cfg.listeExports("PDF");
        if (exportsPDF != null)
            exports.addAll(exportsPDF);
        final ArrayList<Element> exportsHTML = doc.cfg.listeExports("HTML");
        if (exportsHTML != null && exportsHTML.size() > 1)
            exports.addAll(exportsHTML);
        if (exports != null && exports.size() > 0) {
            int pos = 6;
            fileMenu.insertSeparator(pos++);
            menusExport = new JMenuItem[exports.size()];
            for (final Element refExport : exports) {
                final Action action = new ActionExport(doc, refExport);
                final JMenuItem item = fileMenu.insert(action, pos);
                final String exportdoc = doc.cfg.documentationExport(refExport);
                if (exportdoc != null)
                    item.setToolTipText(exportdoc);
                // on pourrait utiliser Config.formatageDoc, sauf que le HTML n'est pas géré sur MacOS X pour la barre de menus de l'appli
                menusExport[pos-7] = fileMenu.getItem(pos);
                pos++;
            }
            fileMenu.insertSeparator(pos);
        }
    }
    
    public void premierExportHTML() {
        if (jaxeframe.doc.fsave == null) {
            JOptionPane.showMessageDialog(jaxeframe,
                JaxeResourceBundle.getRB().getString("html.SauverAvant"),
                JaxeResourceBundle.getRB().getString("erreur.Erreur"),
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        final ArrayList<Element> listeExportsHTML = jaxeframe.doc.cfg.listeExports("HTML");
        if (listeExportsHTML == null || listeExportsHTML.size() == 0)
            return;
        final Export exp = new Export(jaxeframe.doc, listeExportsHTML.get(0));
        exp.transformation(Export.fichierHTML(jaxeframe.doc.fsave), true); // ouvre le navigateur à la fin
    }
    
    
    protected class ShowWindowAction extends AbstractAction {
        String titre;
        public ShowWindowAction(final String titre) {
            super(titre);
            this.titre = titre;
        }

        public void actionPerformed(final ActionEvent e) {
            if (this.titre.equals(rb.getString("menus.FenetreHTML")))
                premierExportHTML();
            else if (this.titre.equals(rb.getString("menus.Validation")))
                jaxeframe.activerValidationFrame();
            else if (this.titre.equals(rb.getString("menus.Source"))) {
                jaxeframe.activerSourceFrame();
            } else
                jaxeframe.toFront();
        }
    }

    protected class AboutAction extends AbstractAction {
        public AboutAction(final String titre) {
            super(titre);
        }
        public void actionPerformed(final ActionEvent e) {
            doAbout();
        }
    }

    public void doAbout() {
        if (aboutBox == null)
            aboutBox = new AboutBox(jframe);
        aboutBox.setResizable(false);
        aboutBox.setVisible(true);
    }
    
    public void doPreferences() {
        final Preferences prefs = new Preferences(jframe);
        prefs.setVisible(true);
    }
    
    // inspiré de DefaultEditorKit.CutAction, mais avec la localisation du titre
    protected static class ActionCouper extends TextAction {
        public ActionCouper() {
            super(rb.getString("menus.Couper"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target != null)
                target.cut();
        }
    }
    
    protected static class ActionCopier extends TextAction {
        public ActionCopier() {
            super(rb.getString("menus.Copier"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target != null)
                target.copy();
        }
    }
    
    protected static class ActionColler extends TextAction {
        public ActionColler() {
            super(rb.getString("menus.Coller"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target != null)
                target.paste();
        }
    }
    
    static class ActionToutSelectionner extends TextAction {

        public ActionToutSelectionner() {
            super(rb.getString("menus.ToutSelectionner"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target instanceof JaxeTextPane)
                ((JaxeTextPane)target).toutSelectionner();
            else if (target != null)
                target.selectAll();
        }

    }

    class ActionRechercher extends TextAction {

        public ActionRechercher() {
            super(rb.getString("menus.Rechercher"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target instanceof JaxeTextPane)
                ((JaxeTextPane)target).rechercher();
            else if (target instanceof JTextPane && jframe instanceof SourceFrame)
                ((SourceFrame)jframe).rechercher();
            else
                jaxeframe.getTextPane().rechercher();
        }

    }

    class ActionSuivant extends TextAction {

        public ActionSuivant() {
            super(rb.getString("menus.RechercherSuivant"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target instanceof JaxeTextPane)
                ((JaxeTextPane)target).suivant();
            else if (target instanceof JTextPane && jframe instanceof SourceFrame)
                ((SourceFrame)jframe).suivant();
            else
                jaxeframe.getTextPane().suivant();
        }

    }

    class ActionOrthographe extends AbstractAction {

        public ActionOrthographe() {
            super(rb.getString("menus.Orthographe"));
        }
        public void actionPerformed(final ActionEvent e) {
            final String dico = Preferences.getPref().getProperty("dictionnaire");
            if (dico == null || "".equals(dico)) {
                JOptionPane.showMessageDialog(jframe, JaxeResourceBundle.getRB().getString("erreur.Dictionnaire"));
                return;
            }
            final int pp = dico.lastIndexOf('.');
            String phon;
            if (pp != -1)
                phon = dico.substring(0, pp+1) + "phon";
            else
                phon = dico + ".phon";
            SpellDictionary dictionary;
            final String encoding = "ISO-8859-1"; // for dictionary and phonetic file
            // user dictionary is using the default text encoding
            try {
                // build main dictionary
                final File phonFile = new File(phon);
                if (phonFile.exists())
                    dictionary = new SpellDictionaryDichoDisk(new File(dico), phonFile, encoding);
                else
                    dictionary = new SpellDictionaryDichoDisk(new File(dico));
                final JTextComponentSpellChecker sc = new JTextComponentSpellChecker(dictionary);
                
                // Locate the personal dictionary, create if necessary
                final String userHome = System.getProperty("user.home");
                final String osName = System.getProperty("os.name");
                String filename;
                if (osName.indexOf("Windows") != -1)
                    filename = "jaxe_pers_dict";
                else
                    filename = userHome + File.separator + ".jaxe_pers_dict";
                final File persoFile = new File(filename);
                if (!persoFile.exists())
                    persoFile.createNewFile();
                
                // set it
                final SpellDictionary persoDict = new SpellDictionaryHashMap(persoFile, phonFile, encoding);
                sc.setUserDictionary(persoDict);
                
                // start checking
                final JaxeTextPane textPane = jaxeframe.getTextPane();
                final int status = sc.spellCheck(textPane);
                if (status == SpellChecker.SPELLCHECK_OK)
                    JOptionPane.showMessageDialog(jaxeframe, JaxeResourceBundle.getRB().getString("orthographe.aucuneErreur"));
            } catch (final Exception ex) {
//                System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
                LOG.error("actionPerformed(ActionEvent)", ex);
            }
        }
    }

    class UndoAction extends AbstractAction {
        public UndoAction() {
            super(JaxeResourceBundle.getRB().getString("menus.Annuler"));
            setEnabled(false);
        }
          
        public void actionPerformed(final ActionEvent e) {
            jaxeframe.getTextPane().undo();
        }
        
        protected void updateUndoState() {
            final JaxeTextPane textPane = jaxeframe.getTextPane();
            final UndoManager undo = textPane.getUndo();
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, rb.getString("menus.Annuler"));
            }
        }      
    }
    
    class RedoAction extends AbstractAction {
        public RedoAction() {
            super(JaxeResourceBundle.getRB().getString("menus.Retablir"));
            setEnabled(false);
        }

        public void actionPerformed(final ActionEvent e) {
            final JaxeTextPane textPane = jaxeframe.getTextPane();
            final UndoManager undo = textPane.getUndo();
            try {
                undo.redo();
            } catch (final CannotRedoException ex) {
                LOG.error(rb.getString("annulation.ImpossibleRetablir") +": " + ex, ex);
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            final JaxeTextPane textPane = jaxeframe.getTextPane();
            final UndoManager undo = textPane.getUndo();
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, rb.getString("menus.Retablir"));
            }
        }
    }    
    
    protected class ActionExport extends AbstractAction {
        JaxeDocument doc;
        Element refExport;
        public ActionExport(final JaxeDocument doc, final Element refExport) {
            super(doc.cfg.titreExport(refExport));
            this.doc = doc;
            this.refExport = refExport;
        }
        public void actionPerformed(final ActionEvent e) {
            final Export exp = new Export(doc, refExport);
            exp.transformation();
        }
    }
    
    public void miseAJour() {
        undoAction.updateUndoState();
        redoAction.updateRedoState();
    }
}
