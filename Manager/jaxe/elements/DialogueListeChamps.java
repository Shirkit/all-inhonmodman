/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import jaxe.JaxeResourceBundle;

/**
 * Dialogue pour JEListeChamps
 */
public class DialogueListeChamps extends JDialog implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueListeChamps.class);

    public Vector<String> data;
    JList jliste;
    DefaultListModel lmodel;
    JFrame frame;
    boolean valide;
    
    public DialogueListeChamps(final JFrame frame, final String titre, final Vector<String> data) {
        super(frame, titre, true);
        this.frame = frame;
        this.data = data;
        final JPanel cpane = new JPanel(new BorderLayout());
        setContentPane(cpane);
        final JPanel chpane = new JPanel(new BorderLayout());
        lmodel = new DefaultListModel();
        for (int i=0; i<data.size(); i++)
            lmodel.addElement(data.get(i));
        jliste = new JList(lmodel);
        jliste.setCellRenderer(new MyCellRenderer());// pour éviter les items minuscules quand la valeur est ""
        final JScrollPane listScrollPane = new JScrollPane(jliste);
        chpane.add(listScrollPane, BorderLayout.CENTER);
        
        final JPanel modpane = new JPanel();
        modpane.setLayout(new BoxLayout(modpane, BoxLayout.Y_AXIS));
        final JButton boutonAjouter = new JButton(JaxeResourceBundle.getRB().getString("bouton.Ajouter"));
        boutonAjouter.addActionListener(this);
        boutonAjouter.setActionCommand("Ajouter");
        modpane.add(boutonAjouter);
        final JButton boutonModifier = new JButton(JaxeResourceBundle.getRB().getString("bouton.Modifier"));
        boutonModifier.addActionListener(this);
        boutonModifier.setActionCommand("Modifier");
        modpane.add(boutonModifier);
        final JButton boutonSupprimer = new JButton(JaxeResourceBundle.getRB().getString("bouton.Supprimer"));
        boutonSupprimer.addActionListener(this);
        boutonSupprimer.setActionCommand("Supprimer");
        modpane.add(boutonSupprimer);
        
        chpane.add(modpane, BorderLayout.EAST);
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
        if (frame != null) {
            final Rectangle r = frame.getBounds();
            setLocation(r.x + r.width/4, r.y + r.height/4);
        } else {
            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((screen.width - getSize().width)/3, (screen.height - getSize().height)/3);
        }
    }
    
    public boolean afficher() {
        setVisible(true);
        // m a j data
        data = new Vector<String>();
        for (final Enumeration e = lmodel.elements() ; e.hasMoreElements() ;)
            data.add((String)e.nextElement());
        return valide;
    }
    
    public void ajouter() {
        final String valeur = JOptionPane.showInputDialog(frame, JaxeResourceBundle.getRB().getString("liste.ValeurElement"),
            JaxeResourceBundle.getRB().getString("liste.NouvelElement"), JOptionPane.QUESTION_MESSAGE);
        lmodel.addElement(valeur);
    }
    
    public void modifier() {
        final int index = jliste.getSelectedIndex();
        if (index != -1) {
            String valeur = (String)jliste.getSelectedValue();
            valeur = (String)JOptionPane.showInputDialog(frame, JaxeResourceBundle.getRB().getString("liste.ValeurElement"),
                JaxeResourceBundle.getRB().getString("liste.ModifierElement"), JOptionPane.QUESTION_MESSAGE, null, null, valeur);
            if (valeur != null)
                lmodel.set(index, valeur);
        }
    }

    public void supprimer() {
        final int index = jliste.getSelectedIndex();
        if (index != -1)
            lmodel.remove(index);
    }

    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("OK".equals(cmd)) {
            valide = true;
            setVisible(false);
        } else if ("Annuler".equals(cmd)) {
            valide = false;
            setVisible(false);
        } else if ("Ajouter".equals(cmd))
            ajouter();
        else if ("Modifier".equals(cmd))
            modifier();
        else if ("Supprimer".equals(cmd))
            supprimer();
    }
    
    protected class MyCellRenderer extends JLabel implements ListCellRenderer {
        public MyCellRenderer() {
            setOpaque(true);
        }
        public Component getListCellRendererComponent(
            final JList list,
            final Object value,
            final int index,
            final boolean isSelected,
            final boolean cellHasFocus)
        {
            if (value != null)
                setText(value.toString());
            setBackground(isSelected ? Color.black : Color.white);
            setForeground(isSelected ? Color.white : Color.black);
            setBorder(BorderFactory.createLineBorder(Color.darkGray));
            if (value == null || "".equals(value.toString())) {
                final Dimension mini = new Dimension(50,12);
                setMinimumSize(mini);
                //Dimension pref = getPreferredSize();
                //if (pref.height < mini.height || pref.width < mini.width)
                setPreferredSize(mini);
            } else
                setPreferredSize(null);
            return this;
        }
    }
}
