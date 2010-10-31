/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dialogue permettant d'entrer une liste de champs texte.
 */
public class DialogueChamps extends JDialog implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueChamps.class);

    JComponent[] champs;
    boolean valide;
    
    public DialogueChamps(final JFrame frame, final String titre, final String[] titresChamps, final JComponent[] champs1) {
        super(frame, titre, true);
        this.champs = champs1;
        final JPanel cpane = new JPanel(new BorderLayout());
        setContentPane(cpane);
        final JPanel chpane = new JPanel(new BorderLayout());
        final JPanel qpane = new JPanel(new GridLayout(titresChamps.length, 1));
        for (final String element : titresChamps) {
            final JLabel label = new JLabel(element);
            qpane.add(label);
        }
        qpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final JPanel tfpane = new JPanel(new GridLayout(champs.length, 1));
        for (final JComponent element : champs) {
            tfpane.add(element);
        }
        chpane.add(qpane, BorderLayout.WEST);
        chpane.add(tfpane, BorderLayout.CENTER);
        cpane.add(chpane, BorderLayout.CENTER);
        final JPanel bpane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton boutonAnnuler = new JButton(JaxeResourceBundle.getRB().getString("bouton.Annuler"));
        boutonAnnuler.addActionListener(this);
        boutonAnnuler.setActionCommand("Annuler");
        bpane.add(boutonAnnuler);
        final JButton boutonOK = new JButton(JaxeResourceBundle.getRB().getString("bouton.OK"));
        boutonOK.addActionListener(this);
        boutonOK.setActionCommand("OK");
        bpane.add(boutonOK);
        cpane.add(bpane, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(boutonOK);
        cpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        if (frame != null) {
            final Rectangle r = frame.getBounds();
            setLocation(r.x + r.width/4, r.y + r.height/4);
        } else {
            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((screen.width - getSize().width)/3, (screen.height - getSize().height)/3);
        }
        //champs[0].requestFocus();
    }
    
    public boolean afficher() {
        setVisible(true);
        return(valide);
    }

    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("OK".equals(cmd)) {
            valide = true;
            setVisible(false);
        } else if ("Annuler".equals(cmd)) {
            valide = false;
            setVisible(false);
        }
    }

}
