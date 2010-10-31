/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import jaxe.elements.JESwing;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Fenêtre de Jaxe
 */
public class JaxeFrame extends JFrame implements ComponentListener, EcouteurMAJ {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JaxeFrame.class);

    private static final ResourceBundle rb = JaxeResourceBundle.getRB();
    
    JaxeTextPane textPane;
    private JScrollPane paneScrollPane;
    
    private JTabbedPane sidepane ;
    private ArbreXML arbrexml ;
    private AllowedElementsPanel allowed;
    private AttributePanel attpane;
    private JSplitPane split;
    private CaretListenerLabel caretListenerLabel;
    
    private boolean afficherSide = true;
    private boolean afficherArbre = true;
    private boolean afficherAllowed = true;
    private boolean afficherAttributs = true;
    
    private JaxeMenuBar menuBar;
    private String nomFichierCfg;
    
    private ValidationFrame validationFrame = null;
    private SourceFrame sourceFrame = null;
    
    private JMenuBar barreInsertion;
    
    public JaxeDocument doc;
    
    private File fichierAOuvrir;
    private File configAOuvrir;
    
    
    public JaxeFrame() {
        super("Jaxe");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                fermer(false);
            }
            /*public void windowActivated(WindowEvent e) {
                if (System.getProperty("os.name").startsWith("Mac"))
                    if (textPane != null)
                        textPane.requestFocus(); // gnaaaargh ah ah ! je l'ai eu mon focus !  8¬>
                    // le requestFocus provoque des activate de fenêtres en boucle sur Windows
                    // et maintenant sur Mac quand on fait Nouveau->Ouvrir
            }*/
        });
        
        final Properties prefs = Preferences.getPref();
        if (prefs != null) {
            final String prefArbre = prefs.getProperty("fenetreArbre");
            if (prefArbre != null)
                afficherArbre = "true".equals(prefArbre);
            final String prefInsertion = prefs.getProperty("fenetreInsertion");
            if (prefInsertion != null)
                afficherAllowed = "true".equals(prefInsertion);
            final String prefAttributs = prefs.getProperty("fenetreAttributs");
            if (prefAttributs != null)
                afficherAttributs = "true".equals(prefAttributs);
            afficherSide = afficherArbre || afficherAllowed || afficherAttributs;
        }
        
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        
        menuBar = new JaxeMenuBar(this);
        setJMenuBar(menuBar);

        final Dimension ecran = getToolkit().getScreenSize();
        int largeur = (ecran.width * 2) / 3;
        if (largeur < 750)
            largeur = ecran.width - 20;
        int hauteur = (ecran.height * 3) / 4;
        if (hauteur < 550)
            hauteur = ecran.height - 50;
        final int posx;
        final int posy;
        if (Jaxe.allFrames.size() < 1) {
            posx = 10;
            posy = 40;
        } else {
            final JaxeFrame lastj = Jaxe.allFrames.get(Jaxe.allFrames.size()-1);
            final Point jp = lastj.getLocationOnScreen();
            posx = jp.x + 20;
            if (posx + largeur > ecran.width && ecran.width - posx > 300)
                largeur = ecran.width - posx;
            posy = jp.y + 20;
        }
        setSize(new Dimension(largeur, hauteur));
        // la dimension pourrait être modifiée si pack était appelé dans initNew
        setLocation(posx, posy);
    }
    
    public JaxeTextPane getTextPane() {
        return(textPane);
    }
    
    public JaxeMenuBar getJaxeMenuBar() {
        return(menuBar);
    }
    
    public SourceFrame getSourceFrame() {
        return(sourceFrame);
    }
    
    public void setSourceFrame(final SourceFrame sourceFrame) {
        this.sourceFrame = sourceFrame;
    }
    
    public void setAffichageSide(final boolean visible) {
        if (afficherSide != visible) {
            if (afficherArbre)
                textPane.retirerEcouteurArbre(arbrexml);
            if (afficherAllowed)
                textPane.retirerEcouteurArbre(allowed);
            if (afficherAttributs)
                textPane.retirerEcouteurArbre(attpane);
            if (!afficherSide)
                getContentPane().remove(paneScrollPane);
            else
                getContentPane().remove(split);
            afficherSide = visible;
            modifierSide();
            validate();
            textPane.getCaret().setVisible(true);
        }
    }
    
    public boolean getAffichageSide() {
        return(afficherSide);
    }
    
    public void setAffichageArbre(final boolean visible) {
        if (afficherArbre != visible) {
            if (!visible)
                textPane.retirerEcouteurArbre(arbrexml);
            if (afficherAllowed)
                textPane.retirerEcouteurArbre(allowed);
            if (afficherAttributs)
                textPane.retirerEcouteurArbre(attpane);
            if (!afficherSide)
                getContentPane().remove(paneScrollPane);
            else
                getContentPane().remove(split);
            afficherArbre = visible;
            afficherSide = (afficherArbre || afficherAllowed || afficherAttributs);
            modifierSide();
            validate();
            textPane.getCaret().setVisible(true);
        }
    }
    
    public boolean getAffichageArbre() {
        return(afficherArbre);
    }
    
    public void setAffichageAllowed(final boolean visible) {
        if (afficherAllowed != visible) {
            if (afficherArbre)
                textPane.retirerEcouteurArbre(arbrexml);
            if (!visible)
                textPane.retirerEcouteurArbre(allowed);
            if (afficherAttributs)
                textPane.retirerEcouteurArbre(attpane);
            if (!afficherSide)
                getContentPane().remove(paneScrollPane);
            else
                getContentPane().remove(split);
            afficherAllowed = visible;
            afficherSide = (afficherArbre || afficherAllowed || afficherAttributs);
            modifierSide();
            validate();
            textPane.getCaret().setVisible(true);
        }
    }
    
    public boolean getAffichageAllowed() {
        return(afficherAllowed);
    }
    
    public void setAffichageAttributs(final boolean visible) {
        if (afficherAttributs != visible) {
            if (afficherArbre)
                textPane.retirerEcouteurArbre(arbrexml);
            if (afficherAllowed)
                textPane.retirerEcouteurArbre(allowed);
            if (!visible)
                textPane.retirerEcouteurArbre(attpane);
            if (!afficherSide)
                getContentPane().remove(paneScrollPane);
            else
                getContentPane().remove(split);
            afficherAttributs = visible;
            afficherSide = (afficherArbre || afficherAllowed || afficherAttributs);
            modifierSide();
            validate();
            textPane.getCaret().setVisible(true);
        }
    }
    
    public boolean getAffichageAttributs() {
        return(afficherAttributs);
    }
    
    public void modifierSide() {
        if (afficherSide) {
            sidepane = new JTabbedPane();
            if (afficherAllowed) {
                allowed = new AllowedElementsPanel((JaxeDocument)textPane.getDocument());
                textPane.addCaretListener(allowed);
                textPane.ajouterEcouteurArbre(allowed);
                sidepane.addTab(rb.getString("tabs.insertion"), allowed);
            } else
                allowed = null;
            
            if (afficherArbre) {
                arbrexml = new ArbreXML(doc) ;
                textPane.ajouterEcouteurArbre(arbrexml);
                sidepane.addTab(rb.getString("tabs.arbre"), arbrexml);
            } else
                arbrexml = null;
            
            if (afficherAttributs) {
                attpane = new AttributePanel((JaxeDocument)textPane.getDocument());
                textPane.addCaretListener(attpane);
                textPane.ajouterEcouteurArbre(attpane);
                sidepane.addTab(rb.getString("tabs.attributs"), attpane);
            } else
                attpane = null;
            
            split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            split.setLeftComponent(sidepane);
            split.setRightComponent(paneScrollPane);
            split.setDividerLocation(275);
            //split.addPropertyChangeListener(new EcouteurPropriete());
            getContentPane().add(split, BorderLayout.CENTER);
        } else {
            sidepane = null;
            arbrexml = null;
            allowed = null;
            attpane = null;
            getContentPane().add(paneScrollPane, BorderLayout.CENTER);
        }
    }
    
    // initialisation de l'objet Jaxe, appelé par le constructeur et doOpenConf et doNew
    public void initNew(final String nomFichierCfg) {
        this.nomFichierCfg = nomFichierCfg;
        getContentPane().removeAll() ;
        doc = new JaxeDocument(nomFichierCfg);
        
        if (nomFichierCfg == null || !((new File(nomFichierCfg)).exists())) {
            JOptionPane.showMessageDialog(this, rb.getString("erreur.ConfigIntrouvable"),
                rb.getString("erreur.Fatale"), JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        affichageDoc();
        
        doc.nouveau();
        caretListenerLabel.setText("");
        textPane.addCaretListener(caretListenerLabel);
        //if (doc.rootJE == null)
        majMenus(textPane.getCaretPosition());
        menuBar.setActivationMenuValider(doc.cfg != null && doc.cfg.schemaURL != null);
        ArrayList<Element> exportsHTML;
        if (doc.cfg == null)
            exportsHTML = null;
        else
            exportsHTML = doc.cfg.listeExports("HTML");
        menuBar.setActivationMenuHTML(exportsHTML != null && exportsHTML.size() > 0);
        menuBar.majExports(doc);
        // CaretListenerLabel.caretUpdate n'est pas appelé s'il n'y a pas de racine
        // ce n'est pas non plus appelé si windowActivated est désactivé
        setTitle(rb.getString("document.Nouveau"));
        //pack();
        if (afficherArbre)
            arbrexml.newdoc(doc) ;
        setVisible(true);
        
        if (validationFrame != null) {
            validationFrame.setVisible(false);
            validationFrame.dispose();
            validationFrame = null;
        }
        if (sourceFrame != null) {
            sourceFrame.setVisible(false);
            sourceFrame.dispose();
            sourceFrame = null;
        }
    }
    
    public void affichageDoc() {
        // zone de texte
        boolean affIconeValide = true;
        final Properties prefs = Preferences.getPref();
        if (prefs != null) {
            final String prefIconeValide = prefs.getProperty("iconeValide");
            if (prefIconeValide != null)
                affIconeValide = "true".equals(prefIconeValide);
        }
        textPane = new JaxeTextPane(doc, this, affIconeValide);
                
        doc.textPane = textPane;
        //textPane.setStyledDocument(doc);
        paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setPreferredSize(new Dimension(500, 400));
        paneScrollPane.setMinimumSize(new Dimension(100, 50));

        // barre des menus du document
        if (doc.cfg == null)
            barreInsertion = null;
        else
            barreInsertion = doc.cfg.makeMenus(doc);

        //Create the status area.
        final JPanel statusPane = new JPanel(new GridLayout(1, 1));
        caretListenerLabel =
                new CaretListenerLabel(rb.getString("status.Chargement"), doc);
        statusPane.add(caretListenerLabel);

        if (barreInsertion != null)
            getContentPane().add(barreInsertion, BorderLayout.NORTH);
        getContentPane().add(statusPane, BorderLayout.SOUTH);
        
        modifierSide();
        
        textPane.ajouterEcouteurAnnulation(menuBar);
        textPane.ajouterEcouteurArbre(this);

        removeComponentListener(this);
        addComponentListener(this);
        validate();
    }
    
    public void ouvrir(final File f) {
        ouvrirAvecConf(f, null);
    }
    
    public void ouvrirAvecConf(final File f, final File fconf) {
        fichierAOuvrir = f;
        configAOuvrir = fconf;
        (new ThreadOuverture()).start();
    }
    
    class ThreadOuverture extends Thread {
        @Override
        public void run() {
            ouvrirPlusTard();
        }
    }
    
    public void ouvrirPlusTard() {
        if (validationFrame != null) {
            validationFrame.setVisible(false);
            validationFrame.dispose();
            validationFrame = null;
        }
        if (sourceFrame != null) {
            sourceFrame.setVisible(false);
            sourceFrame.dispose();
            sourceFrame = null;
        }
        setVisible(true);
        
        final DialogueAttente attente = new DialogueAttente(this, rb.getString("status.Chargement"), 1, 100);
        attente.setVisible(true);
        doc = new JaxeDocument();
        
        getContentPane().removeAll() ;
        affichageDoc();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        getContentPane().setVisible(false); // to speed up loading with Java 1.4
        // (otherwise Container.validate calls Component.updateCursorImmediately)
        
        attente.setProgress(5);
        
        URL u ;
        try {
            u = fichierAOuvrir.toURI().toURL();
        } catch (final MalformedURLException ex) {
            LOG.error("ouvrirPlusTard() - MalformedURLException", ex);
            attente.dispose();
            setCursor(null);
            fermer(false);
            return;
        }
        final String cheminConfig;
        if (configAOuvrir == null)
            cheminConfig = null;
        else
            cheminConfig = configAOuvrir.getAbsolutePath();
        if (!doc.lire(u, cheminConfig)) {
            attente.dispose();
            setCursor(null);
            fermer(false);
            return;
        }
        
        attente.setProgress(60);
        
        nomFichierCfg = doc.nomFichierCfg;
        String nomFichier;
        try {
            nomFichier = URLDecoder.decode(u.getFile(), "UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            nomFichier = u.getFile();
            LOG.error("JaxeFrame.ouvrirPlusTard", ex);
        }
        setTitle(nomFichier);
        getRootPane().putClientProperty("Window.documentFile", fichierAOuvrir); // pour MacOS X
        caretListenerLabel.setText("");
        textPane.addCaretListener(caretListenerLabel);
        
        // il faut refaire les menus parce-qu'on a lu le type de document (UEL, IUFM, DEA)
        if (barreInsertion != null)
            getContentPane().remove(barreInsertion);
        if (doc.cfg != null) {
            barreInsertion = doc.cfg.makeMenus(doc);
            getContentPane().add(barreInsertion, BorderLayout.NORTH);
        } else
            barreInsertion = null;
        
        attente.setProgress(90);
        
        if (arbrexml != null)
            arbrexml.newdoc(doc) ;
        if (afficherArbre)
            sidepane.setSelectedComponent(arbrexml);
        
        getContentPane().validate();
        getContentPane().setVisible(true);
        textPane.setCaretPosition(0);
        majMenus(textPane.getCaretPosition());
        
        menuBar.setActivationMenuValider(doc.cfg != null && doc.cfg.schemaURL != null);
        ArrayList<Element> exportsHTML;
        if (doc.cfg == null)
            exportsHTML = null;
        else
            exportsHTML = doc.cfg.listeExports("HTML");
        menuBar.setActivationMenuHTML(exportsHTML != null && exportsHTML.size() > 0);
        menuBar.majExports(doc);
        
        attente.dispose();
        setCursor(null);
        toFront();
    }
    
    public boolean fermer(final boolean quit) {
        if (doc != null && doc.getModif()) {
            final int r = JOptionPane.showConfirmDialog(this, rb.getString("fermeture.EnregistrerAvant"),
                rb.getString("fermeture.Fermeture"), JOptionPane.YES_NO_CANCEL_OPTION);
            if (r == JOptionPane.YES_OPTION)
                enregistrer();
            else if (r == JOptionPane.CANCEL_OPTION)
                return(false);
        }
        setVisible(false);
        if (validationFrame != null) {
            validationFrame.setVisible(false);
            validationFrame.dispose();
            validationFrame = null;
        }
        if (sourceFrame != null) {
            sourceFrame.setVisible(false);
            sourceFrame.dispose();
            sourceFrame = null;
        }
        Jaxe.allFrames.remove(this);
        
        // aide pour le garbage collector
        dispose();
        
        if (!quit && Jaxe.allFrames.size() == 0)
            Jaxe.dialogueDepart();
        return(true);
    }
    
    public void enregistrer() {
        if (doc.fsave == null)
            enregistrerSous();
        else {
            try {
                doc.ecrire(doc.fsave);
            } catch (final IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "IOException",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            setTitle(doc.fsave.getName());
            getRootPane().putClientProperty("Window.documentFile", doc.fsave); // pour MacOS X
        }
    }
    
    public void enregistrerSous() {
        if (System.getProperty("os.name").startsWith("Mac OS")) // pour éviter un bug avec FileDialog et cmd-v
            menuBar.setEnabled(false);
        File f = null;
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            final JFileChooser chooser = new JFileChooser(JaxeMenuBar.dernierRepertoire);
            final int resultat = chooser.showSaveDialog(this);
            if (resultat == JFileChooser.APPROVE_OPTION) {
                JaxeMenuBar.dernierRepertoire = chooser.getCurrentDirectory();
                f = chooser.getSelectedFile();
            }
        } else {
            final FileDialog fd = new FileDialog(this, null, FileDialog.SAVE);
            fd.setVisible(true);
            if (System.getProperty("os.name").startsWith("Mac OS"))
                menuBar.setEnabled(true);
            final String sf = fd.getFile();
            if (sf != null)
                f = new File(fd.getDirectory(), sf);
        }
        if (f != null) {
            if (f.getName().indexOf('.') == -1) {
                f = new File(f.getPath() + ".xml");
                if (f.exists()) {
                    if (JOptionPane.showConfirmDialog(this, rb.getString("enregistrement.remplacer"), "",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        enregistrerSous();
                        return;
                    }
                }
            }
            try {
                doc.ecrire(f);
            } catch (final IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "IOException",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            setTitle(f.getName());
            getRootPane().putClientProperty("Window.documentFile", f); // pour MacOS X
        }
    }
    
    //Listener pour garder la cohérence entre le redimensionnement de la fenêtre de l'arbre à la main et le cocheMenu
    /*
    class EcouteurPropriete implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent pce) {
            if (pce.getPropertyName().equals("dividerLocation")) {
                // mise à jour de la position des composants dans le texte
                if (doc != null)
                    doc.styleChanged();
                if (((Integer)pce.getNewValue()).compareTo(new Integer(50)) <= 0) {
                    //menuBar.setSideMenu(false) ;
                    setAffichageSide(false);
                }
                else if (((Integer)pce.getOldValue()).compareTo(new Integer(50)) <= 0) {
                    //menuBar.setSideMenu(true) ;
                    setAffichageSide(true);
                }
            }
        }
    }
    */
    
    public void componentHidden(final ComponentEvent e) {
        //System.out.println("hidden");
    }

    public void componentMoved(final ComponentEvent e) {
        //System.out.println("moved");
    }

    public void componentResized(final ComponentEvent e) { // pour corriger un bug sur Windoze
        //System.out.println("resized");

        // modif pour garder une taille raisonnable pour la fenêtre de l'arbre
        if (sidepane != null)
            if (sidepane.getSize().width > 0 && sidepane.getSize().width < 100)
                split.setDividerLocation(275) ;
        
        // mise à jour de la position des composants dans le texte
        if (doc != null)
            doc.styleChanged();
    }

    public void componentShown(final ComponentEvent e) {
        //System.out.println("shown");
        if (textPane != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textPane.requestFocus();
                    textPane.getCaret().setVisible(true);
                    textPane.invalidate();
                }
            });
        }
    }
    
    //This listens for and reports caret movements.
    protected class CaretListenerLabel extends JLabel implements CaretListener {
        
        public CaretListenerLabel (final String label, final JaxeDocument doc) {
            super(label);
        }

        public void caretUpdate(final CaretEvent e) {
            final int dot = e.getDot();
            final int mark = e.getMark();
            if (dot == mark) {  // no selection
                setText(dot + ": " + doc.getPathAsString(dot));
            }
            if (dot < mark)
                majMenus(dot);
            else
                majMenus(mark);
        }
    }
    
    /**
     * Mise à jour des menus via EcouteurMAJ
     */
    public void miseAJour() {
        majMenus(textPane.getCaretPosition());
    }
    
    /**
     * Mise à jour des menus (grisé / non grisé) avec la liste des éléments autorisés
     */
    public void majMenus(final int pos) {
        if (doc.cfg == null || barreInsertion == null || textPane.getIgnorerEdition())
            return;
        if (textPane.getSelectionStart() != pos)
            return;
        JaxeElement parent = null;
        if (doc.rootJE != null)
            parent = doc.rootJE.elementA(pos);
        if (parent != null && parent.debut.getOffset() == pos &&
                !(parent instanceof JESwing))
            parent = parent.getParent() ;
        if (parent != null && parent.noeud.getNodeType() == Node.TEXT_NODE)
            parent = parent.getParent();
        ArrayList<Element> autorisees = null;
        Config parentconf = null;
        if (parent == null) {
            parentconf = doc.cfg;
            autorisees = doc.cfg.listeElementsRacines();
        } else if (parent.noeud.getNodeType() == Node.COMMENT_NODE) {
            parentconf = doc.cfg;
            autorisees = new ArrayList<Element>();
        } else {
            final Element parentref = parent.refElement;
            if (parentref == null)
                return;
            parentconf = doc.cfg.getRefConf(parentref);
            final ArrayList<Element> sousElements = parentconf.listeSousElements(parentref);
            if (sousElements != null) {
                autorisees = new ArrayList<Element>();
                final int debutSelection = textPane.getSelectionStart();
                final int finSelection = textPane.getSelectionEnd();
                for (final Element ref : sousElements) {
                    if (parentconf == null || parent == null ||
                            parentconf.insertionPossible(parent, debutSelection, finSelection, ref)) {
                        autorisees.add(ref);
                    }
                }
            }
        }
        for (int i=0; i<barreInsertion.getMenuCount(); i++) {
            final JMenu menu = barreInsertion.getMenu(i);
            majMenu(menu, parentconf, autorisees);
        }
    }
    
    protected boolean majMenu(final JMenu menu, final Config parentconf, final ArrayList<Element> autorisees) {
        boolean anyenab = false;
        for (int i=0; i<menu.getItemCount(); i++) {
            final JMenuItem item = menu.getItem(i);
            if (item != null) {
                final Action action = item.getAction();
                if (action instanceof ActionInsertionBalise) {
                    final Element refElement = ((ActionInsertionBalise)action).getRefElement();
                    if (refElement != null) {
                        final Config conf = doc.cfg.getRefConf(refElement);
                        final String nomElement = conf.nomElement(refElement);
                        if (conf == parentconf) {
                            boolean enable = false;
                            for (final Element ref : autorisees)
                                if (nomElement.equals(doc.cfg.nomElement(ref))) {
                                    enable = true;
                                    anyenab = true;
                                    if (refElement != ref) // cas de 2 éléments du schéma avec le même nom
                                        ((ActionInsertionBalise)action).setRefElement(ref);
                                    break;
                                }
                            action.setEnabled(enable);
                        } else
                            action.setEnabled(true);
                    }
                } else if (action instanceof ActionFonction) {
                    action.setEnabled(true);
                    anyenab = true;
                } else if (item instanceof JMenu)
                    anyenab = majMenu((JMenu)item, parentconf, autorisees) || anyenab;
            }
        }
        if (!menu.isTopLevelMenu())
            menu.setEnabled(anyenab);
        return(anyenab);
    }
    
    public void activerValidationFrame() {
        if (validationFrame == null)
            validationFrame = new ValidationFrame(doc, this);
        else
            validationFrame.miseAJour();
    }
    
    public void activerSourceFrame() {
        if (sourceFrame == null)
            sourceFrame = new SourceFrame(doc, this);
        else
            sourceFrame.miseAJour();
    }
    
    public void imprimer() {
        final DocumentRenderer renderer = new DocumentRenderer();
        renderer.print(textPane);
    }
}
