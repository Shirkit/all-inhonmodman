/*
Jaxe - Editeur XML en Java

Copyright (C) 2004 Observatoire de Paris

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * Dialogue affiché au lancement de l'application, quand aucun document n'est ouvert directement.
 */
public class DialogueDepart extends JDialog implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueDepart.class);

    private static final ResourceBundle rb = JaxeResourceBundle.getRB();

    public DialogueDepart() {
        super((Frame)null);
        setTitle("Jaxe");
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                quitter();
            }
        });
        
        final JPanel cpane = new JPanel();
        setContentPane(cpane);
        
        final JButton bnouveau = new JButton(rb.getString("nouveau.Nouveau"));
        bnouveau.setActionCommand("nouveau");
        bnouveau.addActionListener(this);
        cpane.add(bnouveau);
        final JButton bouvrir = new JButton(rb.getString("nouveau.Ouvrir"));
        bouvrir.setActionCommand("ouvrir");
        bouvrir.addActionListener(this);
        cpane.add(bouvrir);
        final JButton bquitter = new JButton(rb.getString("nouveau.Quitter"));
        bquitter.setActionCommand("quitter");
        bquitter.addActionListener(this);
        cpane.add(bquitter);
        cpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getRootPane().setDefaultButton(bnouveau);
        pack();
        
        final Dimension dim = getSize();
        final Dimension ecran = getToolkit().getScreenSize();
        setLocation((ecran.width - dim.width)/2, (ecran.height - dim.height)/2);
    }

    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("nouveau".equals(cmd))
            nouveau();
        else if ("ouvrir".equals(cmd))
            ouvrir();
        else if ("quitter".equals(cmd))
            quitter();
    }
    
    protected void nouveau() {
        Jaxe.dialogueNouveau(null);
    }
    
    protected void ouvrir() {
        final JaxeFrame jframe = new JaxeFrame();
        Jaxe.allFrames.add(jframe);
        File f = null;
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            final JFileChooser chooser = new JFileChooser(JaxeMenuBar.dernierRepertoire);
            final int resultat = chooser.showOpenDialog(jframe);
            if (resultat == JFileChooser.APPROVE_OPTION) {
                JaxeMenuBar.dernierRepertoire = chooser.getCurrentDirectory();
                f = chooser.getSelectedFile();
            }
        } else {
            final FileDialog fd = new FileDialog(jframe);
            fd.setVisible(true);
            final String sf = fd.getFile();
            if (sf != null)
                f = new File(fd.getDirectory(), sf);
        }
        if (f != null) {
            Jaxe.ouvrir(f, jframe);
            Jaxe.finDialogueDepart();
        } else
            jframe.fermer(true);
    }
    
    protected void quitter() {
        Jaxe.finDialogueDepart();
        Jaxe.quitter();
    }
}
