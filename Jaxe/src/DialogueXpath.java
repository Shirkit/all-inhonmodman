/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.w3c.dom.Element;


public class DialogueXpath extends JFrame implements ActionListener, ItemListener, CaretListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueXpath.class);
    
    private static final ResourceBundle rb = JaxeResourceBundle.getRB();
    private JTextField textF;
    private ArrayList<Element> listeElements;
    private DialogueRechercher dialRech;
    private JaxeDocument doc;
    private final JComboBox choix;
    private Element refElementCourant;
    private String nomElementCourant = "";
    private JTextField textInterne;
    private ZoneAttribut[] attributs;
    private String typeRechTxt;
    private final JPanel pane;
    private final JPanel listPane;
    private boolean existNS = false;
    
    
    public DialogueXpath(final DialogueRechercher dr, final JaxeDocument doc) {
        super(rb.getString("xpath.ExprXpath"));
        dialRech = dr;
        this.doc = doc;
        final org.w3c.dom.Node DomRoot = doc.DOMdoc.getDocumentElement();
        if (DomRoot != null) {
            if (DomRoot.getNamespaceURI() != null)
                existNS = true;
        }
        
        pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        listPane = new JPanel(new FlowLayout());
        final JLabel textElt = new JLabel(rb.getString("xpath.TitreElt"));
        listeElements = triListingElt(doc.cfg.listeTousElements());
        final ArrayList<String> listeTitre = new ArrayList<String>();
        for (final Element ref : listeElements) {
            if (existNS) {
                final String prefixe = doc.cfg.prefixeElement(ref);
                if (prefixe == null)
                    listeTitre.add(doc.cfg.titreElement(ref));
                else
                    listeTitre.add(prefixe + ":" + doc.cfg.titreElement(ref));
            } else
                listeTitre.add(doc.cfg.titreElement(ref));
        }
        choix = new JComboBox(listeTitre.toArray());
        if ("".equals(nomElementCourant)) {
            refElementCourant = listeElements.get(0);
            nomElementCourant = doc.cfg.nomElement(refElementCourant);
        }
        choix.addItemListener(this);
        listPane.add(textElt);
        listPane.add(choix);
        
        majAttPanel();
        majExprXpath();
        setContentPane(pane);
        
        pack();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getSize().width)/2, (screen.height - getSize().height)/2);
        setVisible(true);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                dispose();
                dialRech.setVisible(true);
            }
        });
    }

    
    public void majAttPanel() {
        if (refElementCourant == null) {
            dispose();
            JOptionPane.showMessageDialog(this, rb.getString("xpath.ErrBalise"), rb.getString("xpath.Err"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final JPanel textPane = new JPanel();
        textPane.setLayout(new BoxLayout(textPane, BoxLayout.X_AXIS));
        final JLabel includingText = new JLabel(rb.getString("xpath.IncludingText"));
        textInterne = new JTextField("");
        textInterne.addCaretListener(this);
        typeRechTxt = "contient";
        final ButtonGroup groupe = new ButtonGroup();
        final JCheckBox boxExact = new JCheckBox(rb.getString("xpath.TexteExact"), false);
        groupe.add(boxExact);
        boxExact.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                typeRechTxt = "texte exact";
                majExprXpath();
            }
        } );
        final JCheckBox boxContient = new JCheckBox(rb.getString("xpath.Contient"), true);
        groupe.add(boxContient);
        boxContient.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                typeRechTxt = "contient";
                majExprXpath();
            }
        } );
        textPane.add(Box.createHorizontalStrut(5));
        textPane.add(includingText);
        textPane.add(Box.createHorizontalStrut(2));
        textPane.add(textInterne);
        textPane.add(boxExact);
        textPane.add(boxContient);
        textPane.add(Box.createHorizontalStrut(5));
        
        final JPanel attrPane = new JPanel();
        final JScrollPane scp = new JScrollPane(attrPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
            public Dimension getPreferredSize() {
                final Dimension dim = super.getPreferredSize();
                if (dim.height > 500)
                    dim.height = 500;
                return(dim);
            }
        };
        scp.getVerticalScrollBar().setUnitIncrement(10);
        scp.setOpaque(false);
        scp.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(1, Color.white,new Color(70,70,70)),
            rb.getString("xpath.TitleAtt") + " \" " + doc.cfg.nomElement(refElementCourant) + " \"", 1, 2));
        final ArrayList<Element> latt = doc.cfg.listeAttributs(refElementCourant);
        final int natt = latt.size();
        attributs = new ZoneAttribut[natt];
        attrPane.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        if (natt == 0) {
            final JLabel textAtt = new JLabel(rb.getString("xpath.NoneAtt"));
            textAtt.setForeground(Color.gray);
            attrPane.add(textAtt);
            attrPane.add(Box.createVerticalStrut(30));
        }
        for (int i=0; i<natt; i++) {
            final ZoneAttribut zatt = new ZoneAttribut();
            attributs[i] = zatt;
            final Element att = latt.get(i);
            zatt.nom = doc.cfg.nomAttribut(att);
            zatt.titre = doc.cfg.titreAttribut(refElementCourant, att);
            final JLabel textAtt = new JLabel(zatt.titre + " : ");
            final ArrayList<String> lval = doc.cfg.listeValeursAttribut(att);
            zatt.valeurs = lval;
            if (lval != null && lval.size() > 0) {
                zatt.valeurs.add(0, "");
                final String[] titres = new String[zatt.valeurs.size()];
                for (int j=0; j<zatt.valeurs.size(); j++)
                    titres[j] = doc.cfg.titreValeurAttribut(refElementCourant, att, zatt.valeurs.get(j));
                zatt.champ = new JComboBox(titres);
                ((JComboBox) zatt.champ).addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent e) {
                         majExprXpath();
                     }
                 } );
            } else {
                zatt.champ = new JTextField(42);
                ((JTextField) zatt.champ).addCaretListener(this);
            }
            c.gridx = 0;
            c.gridy = i;
            c.anchor = GridBagConstraints.EAST;
            attrPane.add(textAtt, c);
            c.gridx = 1;
            c.gridy = i;
            c.anchor = GridBagConstraints.WEST;
            attrPane.add(zatt.champ, c);
            if (zatt.champ instanceof JTextField) {
                zatt.typeRech = "contient";
                final ButtonGroup groupeAtt = new ButtonGroup();
                final JCheckBox boxExactAtt = new JCheckBox(rb.getString("xpath.MotExact"), false);
                groupeAtt.add(boxExactAtt);
                boxExactAtt.addItemListener(new ItemListener() {
                    public void itemStateChanged(final ItemEvent e) {
                        zatt.typeRech = "mot exact";
                        majExprXpath();
                    }
                } );
                c.gridx = 2;
                c.gridy = i;
                attrPane.add(boxExactAtt, c);
                final JCheckBox boxContientAtt = new JCheckBox(rb.getString("xpath.Contient"), true);
                groupeAtt.add(boxContientAtt);
                boxContientAtt.addItemListener(new ItemListener() {
                    public void itemStateChanged(final ItemEvent e) {
                        zatt.typeRech = "contient";
                        majExprXpath();
                    }
                } );
                c.gridx = 3;
                c.gridy = i;
                attrPane.add(boxContientAtt, c);
            } else {
                zatt.typeRech = "mot exact";
            }
        }
        
        final JPanel exprPane = new JPanel(new FlowLayout());
        final JLabel textRes = new JLabel(rb.getString("xpath.Expr"));
        textF = new JTextField("", 55);
        textF.setEditable(false);
        textF.setForeground(Color.red);
        exprPane.add(textRes);
        exprPane.add(textF);
        
        final JPanel buttonPane = new JPanel();
        final JButton butOk = new JButton(rb.getString("xpath.Ok"));
        butOk.addActionListener(this);
        butOk.setActionCommand("OK");
        getRootPane().setDefaultButton(butOk);
        final JButton butAnnuler = new JButton(rb.getString("xpath.Annuler"));
        butAnnuler.addActionListener(this);
        butAnnuler.setActionCommand("Annuler");
        buttonPane.add(butOk);
        buttonPane.add(butAnnuler);
        
        pane.add(Box.createVerticalStrut(10));
        pane.add(listPane);
        pane.add(Box.createVerticalStrut(10));
        pane.add(textPane);
        pane.add(Box.createVerticalStrut(10));
        pane.add(scp);
        pane.add(Box.createVerticalStrut(10));
        pane.add(exprPane);
        pane.add(Box.createVerticalStrut(5));
        pane.add(buttonPane);
    }
    
    
    public void caretUpdate(final CaretEvent e) {
        majExprXpath();
    }
    
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("OK".equals(cmd)) {
            dispose();
            dialRech.setTexteRecherche(textF.getText());
            dialRech.setVisible(true);
        } else if ("Annuler".equals(cmd)) {
            dispose();
            dialRech.setVisible(true);
        }
    }
    
    
    public void majExprXpath() {
        boolean existAtt = false;
        final StringBuilder expr = new StringBuilder();
        if (existNS) {
            final String prefix = doc.cfg.prefixeElement(refElementCourant);
            if (prefix != null) {
                expr.append("//");
                expr.append(prefix);
                expr.append(":");
                expr.append(nomElementCourant);
            } else {
                expr.append("//*[local-name()=\"");
                expr.append(nomElementCourant);
                expr.append("\"");
                existAtt = true;
            }
        } else {
            expr.append("//");
            expr.append(nomElementCourant);
        }
        final String text = textInterne.getText();
        if (!text.equals("")) {
            if (existAtt)
                expr.append(" and ");
            else {
                expr.append("[");
                existAtt = true;
            }
            if (typeRechTxt.equals("contient")) {
                expr.append("contains(.,\"");
                expr.append(text);
                expr.append("\")");
            } else {
                expr.append(".=\"");
                expr.append(text);
                expr.append("\"");
            }
        }
        for (final ZoneAttribut zatt : attributs) {
            String valAtt = "";
            if (zatt.champ instanceof JTextField)
                valAtt = ((JTextField)zatt.champ).getText();
            else if (zatt.champ instanceof JComboBox) {
                final int index = ((JComboBox)zatt.champ).getSelectedIndex();
                valAtt = zatt.valeurs.get(index);
            }
            final boolean nonvide = (!"".equals(valAtt));
            if (nonvide) {
                if (existAtt) {
                    expr.append(" and ");
                } else {
                    expr.append("[");
                    existAtt = true;
                }
                if (zatt.typeRech.equals("contient")) {
                    expr.append("contains(@");
                    expr.append(zatt.nom);
                    expr.append(",\"");
                    expr.append(valAtt);
                    expr.append("\")");
                } else {
                    expr.append("@");
                    expr.append(zatt.nom);
                    expr.append("=\"");
                    expr.append(valAtt);
                    expr.append("\"");
                }
            }
        }
        if (existAtt)
            expr.append("]");
        textF.setText(expr.toString());
    }

    
    public void itemStateChanged(final ItemEvent e) {
        final Object source = e.getSource();
        if ((source == choix) && (e.getStateChange() == ItemEvent.SELECTED)) {
            refElementCourant = listeElements.get(choix.getSelectedIndex());
            nomElementCourant = doc.cfg.nomElement(refElementCourant);
            pane.removeAll();
            majAttPanel();
            pane.validate();
            majExprXpath();
            pack();
        }
    }
    
    
    private ArrayList<Element> triListingElt(final ArrayList<Element> liste) {
        final ArrayList<Element> listeTriee = new ArrayList<Element>(liste);
        Collections.sort(listeTriee, new ComparateurDeTitres());
        final ArrayList<Element> listeSansDoublon = new ArrayList<Element>();
        Element refprecedent = null;
        for (final Element ref : listeTriee) {
            if (refprecedent == null || !doc.cfg.nomElement(refprecedent).equals(doc.cfg.nomElement(ref)))
                listeSansDoublon.add(ref);
            else {
                final String uriprecedent = doc.cfg.espaceElement(refprecedent);
                final String uri = doc.cfg.espaceElement(ref);
                if (!((uri == null && uriprecedent == null) || (uri != null && uri.equals(uriprecedent))))
                    listeSansDoublon.add(ref);
            }
            refprecedent = ref;
        }
        return listeSansDoublon;
    }
    
    class ComparateurDeTitres implements Comparator<Element> {
        final String maj1 = "ÔÏÎÀÂËÊÈÉ";
        final String maj2 = "OIIAAEEEE";
        public int compare(Element e1, Element e2) {
            final String titremaj1 = convertir(doc.cfg.titreElement(e1).toUpperCase().trim());
            final String titremaj2 = convertir(doc.cfg.titreElement(e2).toUpperCase().trim());
            return(titremaj1.compareToIgnoreCase(titremaj2));
        }
        private String convertir(final String s) {
            final StringBuilder sb = new StringBuilder(s);
            char c;
            int p;
            for (int i=0; i<sb.length(); i++) {
                c = sb.charAt(i);
                p = maj1.indexOf(c);
                if (p != -1)
                    sb.setCharAt(i, maj2.charAt(p));
            }
            return(sb.toString());
        }
        public boolean equals(Object obj) {
            return(obj instanceof ComparateurDeTitres);
        }
    }
    
    class ZoneAttribut {
        JComponent champ;
        String nom;
        String titre;
        ArrayList<String> valeurs;
        String typeRech;
    }
    
}
