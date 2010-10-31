/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import jaxe.JaxeDocument;
import jaxe.JaxeResourceBundle;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;


public class DialogueFichier extends JDialog implements ActionListener, KeyListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueFichier.class);

    JComponent[] champs;
    String[] noms;
    String[] espaces;
    String[] defauts;
    boolean valide = false;
    Element refElement;
    Element el;
    JFrame jframe;
    JaxeDocument doc;
    String srcAttr;
    ArrayList<Element> latt;
    
    public DialogueFichier(final JFrame jframe, final JaxeDocument doc, final String titre, final Element refElement, final Element el, final String srcAttr) {
        super(jframe, titre, true);
        this.jframe = jframe;
        this.doc = doc;
        this.refElement = refElement;
        this.el = el;
        this.srcAttr = srcAttr;
        latt = doc.cfg.listeAttributs(refElement);
        final int natt = latt.size();
        noms = new String[natt];
        final String[] titres = new String[natt];
        espaces = new String[natt];
        champs = new JComponent[natt];
        defauts = new String[natt];
        for (int i=0; i<natt; i++) {
            final Element att = latt.get(i);
            noms[i] = doc.cfg.nomAttribut(att);
            titres[i] = doc.cfg.titreAttribut(refElement, att);
            espaces[i] = doc.cfg.espaceAttribut(att);
            if (espaces[i] != null) {
                final String prefixe = doc.cfg.prefixeAttribut(el, att);
                if (prefixe != null)
                    noms[i] = prefixe + ":" + noms[i];
            }
            String elval = el.getAttribute(noms[i]);
            defauts[i] = doc.cfg.valeurParDefaut(att);
            if ("".equals(elval) && defauts[i] != null && el.getAttributeNode(noms[i]) == null)
                elval = defauts[i];
            final ArrayList<String> lval = doc.cfg.listeValeursAttribut(att);
            if (lval != null && lval.size() > 0) {
                final JComboBox popup = new JComboBox();
                champs[i] = popup;
                if (defauts[i] == null)
                    popup.addItem("");
                for (int j=0; j<lval.size(); j++) {
                    final String sval = lval.get(j);
                    popup.addItem(doc.cfg.titreValeurAttribut(refElement, att, sval));
                    if (sval.equals(elval)) {
                        if (defauts[i] == null)
                            popup.setSelectedIndex(j+1);
                        else
                            popup.setSelectedIndex(j);
                    }
                }
            } else {
                final ArrayList<String> lvs = doc.cfg.listeValeursSuggereesAttribut(refElement, att);
                if (lvs != null && lvs.size() > 0) {
                    final JComboBox popup = new JComboBox();
                    popup.setEditable(true);
                    champs[i] = popup;
                    if (defauts[i] == null)
                        popup.addItem("");
                    int indexsel = -1;
                    for (int j=0; j<lvs.size(); j++) {
                        final String sval = lvs.get(j);
                        popup.addItem(doc.cfg.titreValeurAttribut(refElement, att, sval));
                        if (sval.equals(elval)) {
                            if (defauts[i] == null)
                                indexsel = j+1;
                            else
                                indexsel = j;
                        }
                    }
                    if (indexsel != -1)
                        popup.setSelectedIndex(indexsel);
                    else
                        popup.setSelectedItem(elval);
                } else {
                    champs[i] = new JTextField(elval, 40);
                }
            }
        }
        final JPanel cpane = new JPanel(new BorderLayout());
        setContentPane(cpane);
        final JPanel chpane = new JPanel(new BorderLayout());
        final JPanel qpane = new JPanel(new GridLayout(titres.length, 1));
        for (int i=0; i<titres.length; i++) {
            final JLabel label = new JLabel(titres[i]);
            final Element att = latt.get(i);
            if (doc.cfg.estObligatoire(att))
                label.setForeground(new Color(150, 0, 0)); // rouge foncé
            else
                label.setForeground(new Color(0, 100, 0)); // vert foncé
            qpane.add(label);
        }
        qpane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        final JPanel tfpane = new JPanel(new GridLayout(champs.length, 1));
        for (final JComponent comp : champs) {
            tfpane.add(comp);
        }
        chpane.add(qpane, BorderLayout.WEST);
        chpane.add(tfpane, BorderLayout.CENTER);
        cpane.add(chpane, BorderLayout.CENTER);
        
        final JPanel bpane0 = new JPanel(new GridLayout(2, 1));
        
        if (doc.furl == null || doc.fsave != null) {
            final JPanel bpane1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            final JButton boutonSelectionner = new JButton(JaxeResourceBundle.getRB().getString("fichier.Selectionner"));
            boutonSelectionner.addActionListener(this);
            boutonSelectionner.setActionCommand("Sélectionner");
            bpane1.add(boutonSelectionner);
            if (doc.fsave != null) {
                final JButton boutonCopier = new JButton(JaxeResourceBundle.getRB().getString("fichier.Copier"));
                boutonCopier.addActionListener(this);
                boutonCopier.setActionCommand("Copier");
                bpane1.add(boutonCopier);
            }
            bpane0.add(bpane1);
        }

        final JPanel bpane2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton boutonAnnuler = new JButton(JaxeResourceBundle.getRB().getString("bouton.Annuler"));
        boutonAnnuler.addActionListener(this);
        boutonAnnuler.setActionCommand("Annuler");
        bpane2.add(boutonAnnuler);
        final JButton boutonOK = new JButton(JaxeResourceBundle.getRB().getString("bouton.OK"));
        boutonOK.addActionListener(this);
        boutonOK.setActionCommand("OK");
        bpane2.add(boutonOK);
        bpane0.add(bpane2);
        
        cpane.add(bpane0, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(boutonOK);
        
        cpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField atf = null;
        for (int i=0; i<natt; i++)
            if (champs[i] instanceof JTextField)
                atf = (JTextField)champs[i];
        if (atf != null) {
            createActionTable(atf);
            //addMenus();
        }
        addKeyListener(this);
        pack();
        addWindowListener(new WindowAdapter() {
            boolean gotFocus = false;
            @Override
            public void windowActivated(final WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    champs[0].requestFocus();
                    gotFocus = true;
                }
            }
        });
        if (jframe != null) {
            final Rectangle r = jframe.getBounds();
            setLocation(r.x + r.width/4, r.y + r.height/4);
        } else {
            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((screen.width - getSize().width)/3, (screen.height - getSize().height)/3);
        }
    }
    
    HashMap<String, Action> actions;
    private void createActionTable(final JTextComponent textComponent) {
        actions = new HashMap<String, Action>();
        final Action[] actionsArray = textComponent.getActions();
        for (final Action a : actionsArray) {
            actions.put((String) a.getValue(Action.NAME), a);
        }
    }
    private Action getActionByName(final String name) {
        return (actions.get(name));
    }

    public void keyPressed(final KeyEvent e) {
        if (e.isMetaDown()/* || e.isControlDown()*/) {
            //System.out.println("cmd-"+e.getKeyChar());
            int modifiers = 0;
            if (e.isMetaDown())
                modifiers = ActionEvent.META_MASK;
            if ('C' == e.getKeyChar()) {
                //if (e.isControlDown())
                //    modifiers = ActionEvent.CTRL_MASK;
                final ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "copy", modifiers);
                getActionByName(DefaultEditorKit.copyAction).actionPerformed(ae);
            }
            if ('X' == e.getKeyChar()) {
                final ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "cut", modifiers);
                getActionByName(DefaultEditorKit.cutAction).actionPerformed(ae);
            }
            if ('V' == e.getKeyChar()) {
                final ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "paste", modifiers);
                getActionByName(DefaultEditorKit.pasteAction).actionPerformed(ae);
            }
        }
    }
    
    public void keyReleased(final KeyEvent e) {
    }
    
    public void keyTyped(final KeyEvent e) {
    }
    
    public boolean afficher() {
        setVisible(true);
        return valide;
    }

    public String[] lireReponses() {
        final String[] rep = new String[champs.length];
        for (int i=0; i<champs.length; i++) {
            if (champs[i] instanceof JTextComponent)
                rep[i] = ((JTextComponent)champs[i]).getText();
            else if (champs[i] instanceof JComboBox) {
                final JComboBox combo = (JComboBox)champs[i];
                final int index = combo.getSelectedIndex();
                final int indexval;
                if (defauts[i] == null)
                    indexval = index - 1;
                else
                    indexval = index;
                final String valeur;
                if (indexval >= 0) {
                    final Element att = latt.get(i);
                    ArrayList<String> lval = doc.cfg.listeValeursAttribut(att);
                    if (lval == null || lval.size() == 0)
                        lval = doc.cfg.listeValeursSuggereesAttribut(refElement, att);
                    valeur = lval.get(indexval);
                } else
                    valeur = (String)combo.getSelectedItem();
                rep[i] = valeur;
            } else
                rep[i] = null;
        }
        return rep;
    }
    
    public void enregistrerReponses() {
        final String[] rep = lireReponses();
        try {
            for (int i=0; i<rep.length; i++)
                if (rep[i] != null) {
                    if ("".equals(rep[i]) && !"".equals(el.getAttribute(noms[i])) &&
                            !el.getAttribute(noms[i]).equals(defauts[i]))
                        el.removeAttribute(noms[i]);
                    else if (rep[i].equals(defauts[i]))
                        el.removeAttribute(noms[i]);
                    else if (!"".equals(rep[i]) || defauts[i] != null)
                        el.setAttributeNS(espaces[i], noms[i], rep[i]);
                }
            doc.setModif(true);
        } catch (final DOMException ex) {
            LOG.error("enregistrerReponses() - DOMException", ex);
            return;
        }
    }
    
    protected boolean checkAtt() {
        final String[] rep = lireReponses();
        for (int i=0; i<latt.size(); i++) {
            final Element att = latt.get(i);
            if (doc.cfg.estObligatoire(att) && (rep[i] == null || "".equals(rep[i]))) {
                getToolkit().beep();
                if (champs[i] instanceof JTextComponent)
                    ((JTextComponent)champs[i]).selectAll();
                return false;
            }
        }
        return true;
    }
    
    public void setNomFichier(final String nom) {
        for (int i=0; i<noms.length; i++)
            if (srcAttr.equals(noms[i]))
                ((JTextComponent)champs[i]).setText(nom);
    }
    
    protected String cheminURI(final File f) {
        String spath = f.getPath();
        boolean abso = f.isAbsolute();
        if (abso && doc.fsave != null) {
            // transformation d'un chemin absolu en chemin relatif par rapport au fichier XML
            final String[] chemin1 = doc.fsave.getAbsolutePath().split("\\" + File.separator);
            final String[] chemin2 = f.getPath().split("\\" + File.separator);
            int i;
            for (i=0; (i<chemin1.length) && (i<chemin2.length) && (chemin1[i].equals(chemin2[i])); i++)
                ;
            if (i > 0) {
                final StringBuilder builder = new StringBuilder();
                for(int j=i, s = chemin1.length-1; j<(s); j++)
                    builder.append("..").append(File.separator);
                for(int j=i, s = chemin2.length-1; j<(s); j++)
                    builder.append(chemin2[j]).append(File.separator);
                builder.append(chemin2[chemin2.length-1]);
                abso = false;
                spath = builder.toString();
            }
        }
        if (!abso && File.separatorChar != '/') {
            // sur Windows, on transforme les \ en /
            spath = spath.replace(File.separatorChar, '/');
            
            // commment faire si le chemin est absolu ?
        }
        return spath;
    }
    
    public void selectionnerFichier() {
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            JFileChooser chooser;
            if (doc.fsave != null)
                chooser = new JFileChooser(doc.fsave.getParentFile());
            else
                chooser = new JFileChooser();
            final int resultat = chooser.showOpenDialog(jframe);
            if (resultat == JFileChooser.APPROVE_OPTION) {
                final File theFile = chooser.getSelectedFile();
                setNomFichier(cheminURI(theFile));
            }
        } else {
            final FileDialog fd = new FileDialog(jframe);
            if (doc.fsave != null)
                fd.setDirectory(doc.fsave.getParent());
            fd.setVisible(true);
            final String sf = fd.getFile();
            if (sf != null) {
                final File theFile = new File(fd.getDirectory(), sf);
                setNomFichier(cheminURI(theFile));
            }
        }
    }
    
    public void copierFichier() {
        File f = null;
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            JFileChooser chooser;
            if (doc.fsave != null)
                chooser = new JFileChooser(doc.fsave.getParentFile());
            else
                chooser = new JFileChooser();
            int resultat = chooser.showOpenDialog(this);
            if (resultat == JFileChooser.APPROVE_OPTION)
                f = chooser.getSelectedFile();
        } else {
            final FileDialog fd = new FileDialog(jframe);
            fd.setVisible(true);
            final String sf = fd.getFile();
            if (sf != null)
                f = new File(fd.getDirectory(), sf);
        }
        if (f != null) {
            copierFichier(f, doc.fsave.getParent() + File.separator + f.getName());
            setNomFichier(f.getName());
        }
    }
    
    public void copierFichier(final File inputFile, final String nomf2) {
        final File outputFile = new File(nomf2);
        
        try {
            final int bufSize = 1024; 
            final BufferedInputStream in  = new BufferedInputStream( 
                                        new FileInputStream(inputFile),bufSize); 
            final BufferedOutputStream out = new BufferedOutputStream( 
                                        new FileOutputStream(outputFile), bufSize); 
            int length = 256;  
            final byte[] ch = new byte[length]; 
            while((length = in.read(ch))!= -1) { 
                out.write(ch,0,length); 
            } 
            out.flush(); 
            in.close(); 
            out.close(); 
        } catch (final IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "IOException", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("OK".equals(cmd)) {
            if (checkAtt()) {
                valide = true;
                setVisible(false);
            }
        } else if ("Annuler".equals(cmd)) {
            valide = false;
            setVisible(false);
        } else if ("Sélectionner".equals(cmd)) {
            selectionnerFichier();
        } else if ("Copier".equals(cmd)) {
            copierFichier();
        }
    }

}
