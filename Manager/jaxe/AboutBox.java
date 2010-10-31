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
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dialogue "à propos de Jaxe..."
 */
public class AboutBox extends JDialog implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(AboutBox.class);

    protected JButton okButton;
    protected JLabel aboutText;

    public AboutBox(final JFrame jframe) {
        super(jframe, JaxeResourceBundle.getRB().getString("menus.APropos"), true);
        this.getContentPane().setLayout(new BorderLayout(15, 15));
        this.setFont(new Font ("SansSerif", Font.BOLD, 14));

        aboutText = new JLabel (JaxeResourceBundle.getRB().getString("apropos.texte"));
        final JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        textPanel.add(aboutText);
        this.getContentPane().add (textPanel, BorderLayout.NORTH);
        
        okButton = new JButton("OK");
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add (okButton);
        okButton.addActionListener(this);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
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
    
    public void actionPerformed(final ActionEvent newEvent) {
        setVisible(false);
    }
    
}