/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Dialogue de modification des attributs d'un élément
 */
public class DialogueAttributs extends JDialog implements ActionListener, KeyListener, DocumentListener, ItemListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueAttributs.class);

    static protected ImageIcon iconeValide = new ImageIcon(ImageKeeper.loadImage("images/valide.gif", true));
    static protected ImageIcon iconeInvalide = new ImageIcon(ImageKeeper.loadImage("images/invalide.gif", true));
    JComponent[] champs;
    String[] noms;
    String[] espaces;
    String[] defauts;
    boolean avecdoc = false;
    JLabel[] tbval;
    boolean valide = false;
    Element refElement;
    Element el;
    JFrame jframe;
    JaxeDocument doc;
    ArrayList<Element> latt;
    Config conf;
    
    public DialogueAttributs(final JFrame jframe, final JaxeDocument doc, final String titre, final Element refElement, final Element el) {
        super(jframe, titre, true);
        this.jframe = jframe;
        this.doc = doc;
        this.refElement = refElement;
        this.el = el;
        conf = doc.cfg.getRefConf(refElement);
        latt = conf.listeAttributs(refElement);
        final int natt = latt.size();
        noms = new String[natt];
        final String[] titres = new String[natt];
        espaces = new String[natt];
        champs = new JComponent[natt];
        defauts = new String[natt];
        final String[] documentation = new String[natt];
        tbval = new JLabel[natt];
        for (int i=0; i<natt; i++) {
            final Element att = latt.get(i);
            noms[i] = conf.nomAttribut(att);
            titres[i] = conf.titreAttribut(refElement, att);
            espaces[i] = conf.espaceAttribut(att);
            if (espaces[i] != null) {
                final String prefixe = conf.prefixeAttribut(el, att);
                if (prefixe != null)
                    noms[i] = prefixe + ":" + noms[i];
            }
            documentation[i] = conf.documentationAttribut(refElement, att);
            if (documentation[i] != null) {
                avecdoc = true;
                documentation[i] = conf.formatageDoc(documentation[i]);
            }
            String elval = el.getAttribute(noms[i]);
            defauts[i] = conf.valeurParDefaut(att);
            if ("".equals(elval) && defauts[i] != null && el.getAttributeNode(noms[i]) == null)
                elval = defauts[i];
            final ArrayList<String> lval = conf.listeValeursAttribut(att);
            if (lval != null && lval.size() > 0) {
                final JComboBox popup = new JComboBox();
                champs[i] = popup;
                if (defauts[i] == null)
                    popup.addItem("");
                for (int j=0; j<lval.size(); j++) {
                    final String sval = lval.get(j);
                    popup.addItem(conf.titreValeurAttribut(refElement, att, sval));
                    if (sval.equals(elval)) {
                        if (defauts[i] == null)
                            popup.setSelectedIndex(j+1);
                        else
                            popup.setSelectedIndex(j);
                    }
                }
                popup.addItemListener(this);
            } else {
                final ArrayList<String> lvs = conf.listeValeursSuggereesAttribut(refElement, att);
                if (lvs != null && lvs.size() > 0) {
                    final JComboBox popup = new JComboBox();
                    popup.setEditable(true);
                    champs[i] = popup;
                    if (defauts[i] == null)
                        popup.addItem("");
                    int indexsel = -1;
                    for (int j=0; j<lvs.size(); j++) {
                        final String sval = lvs.get(j);
                        popup.addItem(conf.titreValeurAttribut(refElement, att, sval));
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
                    popup.addItemListener(this);
                } else {
                    champs[i] = new JTextField(elval, 40);
                    ((JTextField)champs[i]).addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(final KeyEvent evt) {
                            if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
                                actionAnnuler();
                        }
                    });
                    ((JTextComponent)champs[i]).getDocument().addDocumentListener(this);
                }
            }
            final boolean attvalide;
            if ("".equals(elval) && !conf.estObligatoire(att))
                attvalide = true;
            else
                attvalide = conf.attributValide(att, elval);
            final ImageIcon icone;
            if (attvalide)
                icone = iconeValide;
            else
                icone = iconeInvalide;
            tbval[i] = new JLabel(icone);
        }
        final JPanel cpane = new JPanel(new BorderLayout());
        setContentPane(cpane);
        final JPanel chpane = new ScrollablePanel(new BorderLayout());
        final JPanel hqpane = new JPanel(new BorderLayout());
        if (avecdoc) {
            final JPanel hpane = new JPanel(new GridLayout(titres.length, 1));
            for (int i=0; i<titres.length; i++) {
                final Element att = latt.get(i);
                if (documentation[i] != null) {
                    final JButton baide = new JButton(new ActionAide(att));
                    baide.setFont(baide.getFont().deriveFont((float)9));
                    if (System.getProperty("os.name").startsWith("Mac OS")) {
                        baide.setText("?");
                        if ("10.5".compareTo(System.getProperty("os.version")) <= 0)
                            baide.putClientProperty("JButton.buttonType", "help");
                        else
                            baide.putClientProperty("JButton.buttonType", "toolbar");
                    } else {
                        baide.setIcon(new ImageIcon(ImageKeeper.loadImage("images/aide.png")));
                        baide.setMargin(new Insets(0, 0, 0, 0));
                        baide.setBorderPainted(false);
                        baide.setContentAreaFilled(false);
                    }
                    baide.setToolTipText(documentation[i]);
                    hpane.add(baide);
                } else
                    hpane.add(new JLabel());
            }
            hqpane.add(hpane, BorderLayout.WEST);
        }
        final JPanel qpane = new JPanel(new GridLayout(titres.length, 1));
        for (int i=0; i<titres.length; i++) {
            final JLabel label = new JLabel(titres[i]);
            final Element att = latt.get(i);
            if (conf.estObligatoire(att))
                label.setForeground(new Color(150, 0, 0)); // rouge foncé
            else
                label.setForeground(new Color(0, 100, 0)); // vert foncé
            qpane.add(label);
        }
        qpane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        final JPanel tfpane = new JPanel(new GridLayout(champs.length, 1));
        for (int i=0; i<champs.length; i++) {
            final JPanel tfbvpane = new JPanel(new BorderLayout());
            tfbvpane.add(champs[i], BorderLayout.CENTER);
            tfbvpane.add(tbval[i], BorderLayout.EAST);
            tfpane.add(tfbvpane);
        }
        hqpane.add(qpane, BorderLayout.CENTER);
        chpane.add(hqpane, BorderLayout.WEST);
        chpane.add(tfpane, BorderLayout.CENTER);
        if (natt <= 15)
            cpane.add(chpane, BorderLayout.CENTER);
        else {
            final JScrollPane scp = new JScrollPane(chpane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            cpane.add(scp, BorderLayout.CENTER);
        }
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
        positionner();
    }
    
    /*protected void addMenus() {
        JMenuBar mainMenuBar = new JMenuBar();
        JMenu editMenu = new JMenu("Edition");
        editMenu.add(getActionByName(DefaultEditorKit.cutAction));
        JMenuItem miCopy = editMenu.add(getActionByName(DefaultEditorKit.copyAction));
        miCopy.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.Event.META_MASK));
        editMenu.add(getActionByName(DefaultEditorKit.pasteAction));
        editMenu.addSeparator();
        editMenu.add(getActionByName(DefaultEditorKit.selectAllAction));
        mainMenuBar.add(editMenu);
        setJMenuBar(mainMenuBar);
    }*/
    
    protected void positionner() {
        final Rectangle zoneMax = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        final Dimension tailleFenetre = getSize();
        int posx, posy;
        if (jframe != null) {
            final Rectangle r = jframe.getBounds();
            posx = r.x + r.width/4;
            posy = r.y + r.height/4;
            if (posx + tailleFenetre.width > zoneMax.width) {
                if (zoneMax.width > tailleFenetre.width)
                    posx = zoneMax.x + zoneMax.width - tailleFenetre.width;
                else
                    posx = zoneMax.x;
            }
            if (posy + tailleFenetre.height > zoneMax.height) {
                if (zoneMax.height > tailleFenetre.height)
                    posy = zoneMax.y + zoneMax.height - tailleFenetre.height;
                else
                    posy = zoneMax.y;
            }
        } else {
            posx = zoneMax.x + (zoneMax.width - tailleFenetre.width)/3;
            posy = zoneMax.y + (zoneMax.height - tailleFenetre.height)/3;
        }
        setLocation(posx, posy);
        if (tailleFenetre.height > zoneMax.height) {
            setMaximumSize(new Dimension(zoneMax.width, zoneMax.height));
            setSize(tailleFenetre.width, zoneMax.height); // pour Windows
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
        return actions.get(name);
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
    
    public void insertUpdate(final DocumentEvent e) {
        verifChamp(e);
    }
    
    public void removeUpdate(final DocumentEvent e) {
        verifChamp(e);
    }
    
    public void changedUpdate(final DocumentEvent e) {
        verifChamp(e);
    }
    
    public void itemStateChanged(final ItemEvent e) {
        final Object src = e.getSource();
        if (!(src instanceof JComboBox))
            return;
        for (int i=0; i<champs.length; i++) {
            if (champs[i] == src) {
                final int index = ((JComboBox)src).getSelectedIndex();
                final Element att = latt.get(i);
                final int indexval;
                if (defauts[i] == null)
                    indexval = index - 1;
                else
                    indexval = index;
                final boolean attvalide = !(indexval < 0 && conf.estObligatoire(att));
                final ImageIcon icone;
                if (!attvalide)
                    icone = iconeInvalide;
                else
                    icone = iconeValide;
                tbval[i].setIcon(icone);
            }
        }
    }
    
    public void verifChamp(final DocumentEvent e) {
        final javax.swing.text.Document document = e.getDocument();
        for (int i=0; i<champs.length; i++) {
            if (champs[i] instanceof JTextComponent && document == ((JTextComponent)champs[i]).getDocument()) {
                final String valeur = ((JTextComponent)champs[i]).getText();
                final boolean attvalide;
                final Element att = latt.get(i);
                if ("".equals(valeur) && !conf.estObligatoire(att))
                    attvalide = true;
                else
                    attvalide = conf.attributValide(att, valeur);
                final ImageIcon icone;
                if (!attvalide) {
                    icone = iconeInvalide;
                    champs[i].setForeground(new Color(200, 0, 0));
                } else {
                    icone = iconeValide;
                    champs[i].setForeground(null);
                }
                tbval[i].setIcon(icone);
            }
        }
    }
    
    public boolean afficher() {
        setVisible(true);
        return(valide);
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
                    ArrayList<String> lval = conf.listeValeursAttribut(att);
                    if (lval == null || lval.size() == 0)
                        lval = conf.listeValeursSuggereesAttribut(refElement, att);
                    valeur = lval.get(indexval);
                } else
                    valeur = (String)combo.getSelectedItem();
                rep[i] = valeur;
            } else
                rep[i] = null;
        }
        return(rep);
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
            if (conf.estObligatoire(att) && (rep[i] == null ||
                "".equals(rep[i]))) {
                getToolkit().beep();
                if (champs[i] instanceof JTextComponent)
                    ((JTextComponent)champs[i]).selectAll();
                return false;
            }
        }
        return true;
    }
    
    public void actionOK() {
        if (checkAtt()) {
            valide = true;
            setVisible(false);
            doc.setModif(true);
        } else {
            JOptionPane.showMessageDialog(doc.jframe, JaxeResourceBundle.getRB().getString("erreur.AttributsManquants"), JaxeResourceBundle.getRB().getString("erreur.Erreur"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void actionAnnuler() {
        valide = false;
        setVisible(false);
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("OK".equals(cmd))
            actionOK();
        else if ("Annuler".equals(cmd))
            actionAnnuler();
    }
    
    class ActionAide extends AbstractAction {
        Element attref;
        ActionAide(final Element attref) {
            super();
            this.attref = attref;
        }
        public void actionPerformed(final ActionEvent e) {
            String attdoc = conf.documentationAttribut(refElement, attref);
            if (attdoc != null)
                attdoc = conf.formatageDoc(attdoc);
            JOptionPane.showMessageDialog(doc.jframe, attdoc,
                conf.titreAttribut(refElement, attref), JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(LayoutManager layout) {
            super(layout);
        }
        
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
            if (orientation == SwingConstants.HORIZONTAL)
                return visible.width / 10;
            else if (orientation == SwingConstants.VERTICAL) {
                for (int i=0; i<getComponentCount(); i++)
                    if (getComponent(i) instanceof Container &&
                            ((Container)getComponent(i)).getLayout() instanceof GridLayout) {
                        final GridLayout gd = (GridLayout)((Container)getComponent(i)).getLayout();
                        return(getHeight() / gd.getRows());
                    }
                return visible.height / 10;
            } else
                throw new IllegalArgumentException("orientation must be either " +
                    "javax.swing.SwingConstants.VERTICAL or " +
                    "javax.swing.SwingConstants.HORIZONTAL");
        }
        
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            if (orientation == SwingConstants.HORIZONTAL)
                return visible.width;
            else if (orientation == SwingConstants.VERTICAL)
                return visible.height;
            else
                throw new IllegalArgumentException("orientation must be either " +
                    "javax.swing.SwingConstants.VERTICAL or " +
                    "javax.swing.SwingConstants.HORIZONTAL");
        }
        
        public boolean getScrollableTracksViewportHeight() {
            if (getParent() instanceof JViewport)
                return ((JViewport) getParent()).getHeight() > getPreferredSize().height;
            
            return false;
        }
        
        public boolean getScrollableTracksViewportWidth() {
            if (getParent() instanceof JViewport)
                return ((JViewport) getParent()).getWidth() > getPreferredSize().width;
            
            return false;
        }
    }
}
