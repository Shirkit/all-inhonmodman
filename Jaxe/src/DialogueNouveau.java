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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

/**
 * Dialogue de création d'un nouveau document
 */
public class DialogueNouveau extends JDialog implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueNouveau.class);

    private static final ResourceBundle rb = JaxeResourceBundle.getRB();
    JList liste;
    ArrayList<String> configs;
    JaxeFrame frame;
    boolean bannulation = false;

    public DialogueNouveau(final JaxeFrame frame) {
        super(frame, true);
        this.frame = frame;
        setTitle(rb.getString("nouveau.Titre"));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                annuler();
            }
        });
        
        final Vector<String> v = new Vector<String>();
        configs = new ArrayList<String>();
        final File configdir = new File("config");
        final String[] lfichiers = configdir.list();
        if (lfichiers == null) {
            LOG.error("DialogueNouveau(JaxeFrame) - " + rb.getString("erreur.DossierConfig"), null);
            return;
        }
        for (final String nomFichier : lfichiers)
            if (nomFichier.endsWith("_cfg.xml") || nomFichier.endsWith("_config.xml")) {
                configs.add("config" + File.separator + nomFichier);
                String description;
                try {
                    final URL urlCfg = new File("config" + File.separator + nomFichier).toURI().toURL();
                    description = Config.descriptionDialogueNouveau(urlCfg);
                } catch (final MalformedURLException ex) {
                    LOG.error(ex);
                    description = null;
                }
                if (description == null)
                    v.add(" " + nomFichier.substring(0, nomFichier.indexOf('.')));
                else
                    v.add(" " + description);
            }
        
        final JPanel cpane = new JPanel(new BorderLayout());
        setContentPane(cpane);
        final JLabel labchoix = new JLabel(rb.getString("nouveau.Choisir"));
        labchoix.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        cpane.add(labchoix, BorderLayout.NORTH);
        
        liste = new JList(v);
        if (v.size() > 0)
            liste.setSelectedIndex(0);
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2)
                    nouveau();
            }
        };
        liste.addMouseListener(mouseListener);
        liste.setBorder(BorderFactory.createLoweredBevelBorder());
        cpane.add(liste, BorderLayout.CENTER);
        
        final JPanel bpane = new JPanel();
        final JButton bannuler = new JButton(rb.getString("nouveau.Annuler"));
        bannuler.setActionCommand("annuler");
        bannuler.addActionListener(this);
        bpane.add(bannuler);
        final JButton bnouveau = new JButton(rb.getString("nouveau.Nouveau"));
        bnouveau.setActionCommand("nouveau");
        bnouveau.addActionListener(this);
        bpane.add(bnouveau);
        cpane.add(bpane, BorderLayout.SOUTH);
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
        else if ("annuler".equals(cmd))
            annuler();
    }
    
    protected void nouveau() {
        final int ind = liste.getSelectedIndex();
        JaxeFrame jframe;
        if (frame != null && !(frame.doc.getModif() || frame.doc.fsave != null))
            jframe = frame;
        else {
            jframe = new JaxeFrame();
            Jaxe.allFrames.add(jframe);
        }
        // on ferme le dialogue modal pour éviter un bug sous MacOS X avec les menus grisés
        setVisible(false);
        jframe.initNew(configs.get(ind));
    }
    
    protected void annuler() {
        bannulation = true;
        setVisible(false);
    }
    
    /**
     * Renvoit true si le dialogue a été annulé
     */
    public boolean annulation() {
        return(bannulation);
    }
}
