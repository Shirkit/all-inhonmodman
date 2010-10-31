/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.w3c.dom.Element;

/**
 * Un dialogue pour afficher une aide en ligne sur un élément XML et ses attributs.
 * Utilise les éléments documentation du schéma XML quand un schéma est utilisé.
 */
public class DialogueAideElement extends JDialog implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueAideElement.class);

    private static final ResourceBundle rb = JaxeResourceBundle.getRB();
    
    private Element refNoeud;
    private Element refElementParent; // pour un attribut, quand aideAttribut
    private final Config cfg;
    private JLabel labelTitre;
    private JLabel labeldoc;
    private JList listeParents;
    private JList listeEnfants;
    private JList listeAttributs;
    private JLabel labelexpr;
    private ArrayList<Element> refElementsParents;
    private ArrayList<Element> refElementsEnfants;
    private ArrayList<Element> refAttributs;
    private boolean aideAttribut;
    private JTabbedPane onglets;
    private JPanel ongletEnfants;
    private JPanel ongletAttributs;
    
    
    /**
     * Dialogue d'aide pour un élément dont on passe la référence en paramètre.
     */
    public DialogueAideElement(final Element refElement, final Config cfg, final JFrame frame) {
        super(frame, rb.getString("aide.element") + " " + cfg.nomElement(refElement), true);
        this.cfg = cfg;
        this.refElementParent = null;
        aideAttribut = false;
        initialisation(frame);
        remplissage(refElement);
    }
    
    /**
     * Dialogue d'aide pour un attribut dont on passe la référence et celle de l'élément parent en paramètre.
     */
    public DialogueAideElement(final Element refAttribut, final Element refElementParent, final Config cfg, final JFrame frame) {
        super(frame, rb.getString("aide.attribut") + " " + cfg.nomAttribut(refAttribut), true);
        this.cfg = cfg;
        this.refElementParent = refElementParent;
        aideAttribut = true;
        initialisation(frame);
        remplissage(refAttribut);
    }
    
    private void initialisation(final JFrame frame) {
        final JPanel cpane = new JPanel();
        cpane.setLayout(new BoxLayout(cpane, BoxLayout.Y_AXIS));
        setContentPane(cpane);
        
        // titre
        final JPanel panelTitre = new JPanel();
        labelTitre = new JLabel();
        labelTitre.setFont(new Font("SansSerif", Font.BOLD, 14));
        panelTitre.add(labelTitre);
        panelTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        cpane.add(panelTitre);
        cpane.add(Box.createRigidArea(new Dimension(1, 20)));
        
        // description
        final JLabel labeldesc = new JLabel(rb.getString("aide.description"));
        labeldesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        cpane.add(labeldesc);
        labeldoc = new JLabel("");
        final JPanel paneldoc = new JPanel();
        paneldoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        paneldoc.add(labeldoc);
        cpane.add(paneldoc);
        cpane.add(Box.createRigidArea(new Dimension(1, 20)));
        
        // onglets
        onglets = new JTabbedPane();
        onglets.setAlignmentX(Component.LEFT_ALIGNMENT);
        cpane.add(onglets);
        
        // cellules avec plus d'espace que par défaut
        final DefaultListCellRenderer myCellRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList list, final Object value, final int index,
                    final boolean isSelected, final boolean cellHasFocus) {
                final JLabel lab = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lab.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                return(lab);
            }
        };
        
        // parents
        final JPanel ongletParents = new JPanel();
        ongletParents.setLayout(new BoxLayout(ongletParents, BoxLayout.Y_AXIS));
        final JLabel labelparents = new JLabel(rb.getString("aide.parents"));
        labelparents.setAlignmentX(Component.LEFT_ALIGNMENT);
        ongletParents.add(labelparents);
        listeParents = new JList();
        listeParents.setCellRenderer(myCellRenderer);
        listeParents.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listeParents.setVisibleRowCount(-1);
        listeParents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final MouseListener listenParents = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final int index = listeParents.locationToIndex(e.getPoint());
                    if (index != -1) {
                        if (aideAttribut) {
                            onglets.setSelectedIndex(2);
                            aideAttribut = false;
                        }
                        remplissage(refElementsParents.get(index));
                    }
                }
            }
        };
        listeParents.addMouseListener(listenParents);
        final JScrollPane panelparents = new JScrollPane(listeParents,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelparents.setPreferredSize(new Dimension(400, 200));
        panelparents.setAlignmentX(Component.LEFT_ALIGNMENT);
        ongletParents.add(panelparents);
        onglets.add(rb.getString("aide.parents"), ongletParents);
        
        // enfants
        ongletEnfants = new JPanel();
        ongletEnfants.setLayout(new BoxLayout(ongletEnfants, BoxLayout.Y_AXIS));
        final JLabel labelenfants = new JLabel(rb.getString("aide.enfants"));
        labelenfants.setAlignmentX(Component.LEFT_ALIGNMENT);
        ongletEnfants.add(labelenfants);
        labelexpr = new JLabel("");
        labelexpr.setAlignmentX(Component.LEFT_ALIGNMENT);
        ongletEnfants.add(labelexpr);
        listeEnfants = new JList();
        listeEnfants.setCellRenderer(myCellRenderer);
        listeEnfants.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listeEnfants.setVisibleRowCount(-1);
        listeEnfants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final MouseListener listenEnfants = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final int index = listeEnfants.locationToIndex(e.getPoint());
                    if (index != -1) {
                        remplissage(refElementsEnfants.get(index));
                    }
                }
            }
        };
        listeEnfants.addMouseListener(listenEnfants);
        final JScrollPane panelenfants = new JScrollPane(listeEnfants,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelenfants.setPreferredSize(new Dimension(400, 200));
        panelenfants.setAlignmentX(Component.LEFT_ALIGNMENT);
        ongletEnfants.add(panelenfants);
        onglets.add(rb.getString("aide.enfants"), ongletEnfants);
        
        // attributs
        ongletAttributs = new JPanel();
        ongletAttributs.setLayout(new BoxLayout(ongletAttributs, BoxLayout.Y_AXIS));
        final JLabel labelattributs = new JLabel(rb.getString("aide.attributs"));
        labelattributs.setAlignmentX(Component.LEFT_ALIGNMENT);
        ongletAttributs.add(labelattributs);
        listeAttributs = new JList();
        listeAttributs.setCellRenderer(myCellRenderer);
        listeAttributs.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listeAttributs.setVisibleRowCount(-1);
        listeAttributs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final MouseListener listenAttributs = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final int index = listeAttributs.locationToIndex(e.getPoint());
                    if (index != -1) {
                        aideAttribut = true;
                        refElementParent = refNoeud;
                        onglets.setSelectedIndex(0);
                        remplissage(refAttributs.get(index));
                    }
                }
            }
        };
        listeAttributs.addMouseListener(listenAttributs);
        final JScrollPane panelattributs = new JScrollPane(listeAttributs,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelattributs.setPreferredSize(new Dimension(400, 200));
        panelattributs.setAlignmentX(Component.LEFT_ALIGNMENT);
        ongletAttributs.add(panelattributs);
        onglets.add(rb.getString("aide.attributs"), ongletAttributs);
        if (aideAttribut)
            ongletAttributs.setVisible(false);
        
        onglets.setSelectedIndex(1); // par défaut, on affiche les enfants
        
        // actions
        final JPanel actpane = new JPanel(new FlowLayout());
        actpane.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JButton bfermer = new JButton(rb.getString("aide.fermer"));
        bfermer.setActionCommand("fermer");
        bfermer.addActionListener(this);
        actpane.add(bfermer);
        cpane.add(actpane);
        getRootPane().setDefaultButton(bfermer);
        
        if (frame != null) {
            final Rectangle r = frame.getBounds();
            setLocation(r.x + r.width/4, r.y + r.height/4);
        } else {
            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((screen.width - getSize().width)/3, (screen.height - getSize().height)/3);
        }
    }
    
    public void remplissage(final Element refNoeud) {
        this.refNoeud = refNoeud;
        
        if (aideAttribut)
            setTitle(rb.getString("aide.attribut") + " " + cfg.nomAttribut(refNoeud));
        else
            setTitle(rb.getString("aide.element") + " " + cfg.nomElement(refNoeud));
        
        // titre
        if (aideAttribut)
            labelTitre.setText(cfg.titreAttribut(refElementParent, refNoeud));
        else
            labelTitre.setText(cfg.titreElement(refNoeud));
        
        // description
        String documentation;
        if (aideAttribut)
            documentation = cfg.documentationAttribut(refElementParent, refNoeud);
        else
            documentation = cfg.documentation(refNoeud);
        if (documentation != null)
            documentation = cfg.formatageDoc(documentation);
        labeldoc.setText(documentation);
        
        // parents
        listeParents.clearSelection();
        if (aideAttribut) {
            refElementsParents = new ArrayList<Element>();
            refElementsParents.add(refElementParent);
            final String[] titreParent = new String[1];
            titreParent[0] = cfg.titreElement(refElementParent);
            listeParents.setListData(titreParent);
        } else {
            refElementsParents = cfg.listeElementsParents(refNoeud);
            final String[] titres = new String[refElementsParents.size()];
            for (int i=0; i<refElementsParents.size(); i++)
                titres[i] = cfg.titreElement(refElementsParents.get(i));
            listeParents.setListData(titres);
        }
        
        // enfants
        onglets.setEnabledAt(1, !aideAttribut);
        if (!aideAttribut) {
            labelexpr.setText(versHTML(cfg.expressionReguliere(refNoeud)));
            listeEnfants.clearSelection();
            refElementsEnfants = cfg.listeSousElements(refNoeud);
            final String[] titres = new String[refElementsEnfants.size()];
            for (int i=0; i<refElementsEnfants.size(); i++)
                titres[i] = cfg.titreElement(refElementsEnfants.get(i));
            listeEnfants.setListData(titres);
        }
        
        // attributs
        onglets.setEnabledAt(2, !aideAttribut);
        if (!aideAttribut) {
            listeAttributs.clearSelection();
            refAttributs = cfg.listeAttributs(refNoeud);
            final String[] titres = new String[refAttributs.size()];
            for (int i=0; i<refAttributs.size(); i++)
                titres[i] = cfg.titreAttribut(refNoeud, refAttributs.get(i));
            listeAttributs.setListData(titres);
        }
        
        pack();
    }
    
    /**
     * tranformation en HTML (découpage en lignes pour expression régulière)
     */
    protected String versHTML(String s) {
        if (s != null) {
            s = s.trim();
            if (s.length() > 90) {
                int p = 0;
                for (int i=0; i<s.length(); i++) {
                    if (i-p > 80 && (s.charAt(i) == '|' || s.charAt(i) == ',')) {
                        s = s.substring(0,i) + "<br>" + s.substring(i);
                        p = i;
                    }
                }
                s = "<html><body>" + s + "</body></html>";
            }
        }
        return(s);
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("fermer".equals(cmd))
            fermer();
    }
    
    public void fermer() {
        setVisible(false);
        dispose();
    }
    
}
