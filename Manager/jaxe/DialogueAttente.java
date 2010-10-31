/*
Jaxe - Editeur XML en Java

Copyright (C) 2004 Observatoire de Paris

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * ProgressMonitor n'est pas toujours la solution, étant donné qu'il
 * a un bouton annuler, et qu'on ne peut pas avec Java 1.3 spécifier un
 * texte sans barre de progression.
 */
public class DialogueAttente extends JDialog {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueAttente.class);

    JProgressBar progress;

    public DialogueAttente(final JFrame frame, final String message, final int min, final int max) {
        super(frame);
        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        
        final JPanel cpane = new JPanel(new BorderLayout());
        setContentPane(cpane);
        final Icon info = UIManager.getIcon("OptionPane.informationIcon");
        cpane.add(new JLabel(info), BorderLayout.WEST);
        final JLabel label = new JLabel(message);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cpane.add(label, BorderLayout.CENTER);
        if (min != 0 || max != 0) {
            progress = new JProgressBar(min, max);
            cpane.add(progress, BorderLayout.SOUTH);
        } else
            progress = null;
        
        pack();
        
        repositionner(frame);
    }
    
    public DialogueAttente(final JFrame frame, final String message) {
        this(frame, message, 0, 0);
    }
    
    private void repositionner(final JFrame frame) {
        final Dimension dim = getSize();
        if (frame != null && frame.isShowing()) {
            final Dimension dimf = frame.getSize();
            final Point fp = frame.getLocationOnScreen();
            setLocation(fp.x + (dimf.width - dim.width)/2, fp.y + (dimf.height - dim.height)/2);
        } else {
            final Dimension ecran = getToolkit().getScreenSize();
            setLocation((ecran.width - dim.width)/2, (ecran.height - dim.height)/2);
        }
    }
    
    public void setProgress(final int n) {
        if (progress != null)
            progress.setValue(n);
    }
}
