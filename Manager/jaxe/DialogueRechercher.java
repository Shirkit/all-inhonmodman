/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;


public class DialogueRechercher extends JDialog implements ActionListener, ItemListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueRechercher.class);

    private static final ResourceBundle rb = JaxeResourceBundle.getRB();

    static String texteRecherche = null;
    static String texteXpathRecherche = null;
    
    private final JTextPane textPane;
    private final StyledDocument doc;
    private final JPanel bpane, cpane;
    private JTextField tfRechercher;
    private final JTextField tfRemplacer;
    private final JLabel textRech = new JLabel(rb.getString("rechercher.Rechercher"));
    private final JLabel textRemp = new JLabel(rb.getString("rechercher.RemplacerPar"));
    private final JButton bXpath, btout, bremplacer, bremplrech;
    private JRadioButton bfichier, bsel;
    private final JCheckBox typeXpath, chkmaj;
    private boolean dansSelection = false;
    private boolean ignorerCasse = false;
    private boolean rechXpath = false;
    private JaxeDocument jdoc;

    public DialogueRechercher(final JaxeDocument jdoc, final JTextPane textPane) {
        super((JFrame)textPane.getTopLevelAncestor(), rb.getString("rechercher.Rechercher"), false);
        this.textPane = textPane;
        this.jdoc = jdoc;
        doc = textPane.getStyledDocument();
        if (textPane.getSelectionEnd() != textPane.getSelectionStart())
            dansSelection = true;
        
        cpane = new JPanel();
        cpane.setLayout(new BoxLayout(cpane, BoxLayout.Y_AXIS));
        
        // rechercher / remplacer
        setContentPane(cpane);
        final JPanel chpane = new JPanel(new BorderLayout());
        final JPanel qpane = new JPanel(new GridLayout(2, 1));
        qpane.add(textRech);
        qpane.add(textRemp);
        qpane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        final JPanel tfpane = new JPanel(new GridLayout(2, 1)); // textfields
        tfRechercher = new JTextField("", 40);
        if (texteRecherche != null)
            tfRechercher.setText(texteRecherche);
        tfRechercher.selectAll();
        tfpane.add(tfRechercher);
        tfRemplacer = new JTextField("", 40);
        tfpane.add(tfRemplacer);
        
        bpane = new JPanel(new GridLayout(2,1));
        bpane.setPreferredSize(new Dimension(85,50));
        bXpath = new JButton(rb.getString("rechercher.Xpath"));
        bXpath.setActionCommand("xpath");
        bXpath.addActionListener(this);
        
        chpane.add(qpane, BorderLayout.WEST);
        chpane.add(tfpane, BorderLayout.CENTER);
        chpane.add(bpane, BorderLayout.EAST);
        cpane.add(chpane);
        
        // options
        final JPanel optpane = new JPanel(new FlowLayout());
        bfichier = new JRadioButton(rb.getString("rechercher.FichierEntier"));
        bfichier.setActionCommand("fichier");
        bfichier.setSelected(!dansSelection);
        bsel = new JRadioButton(rb.getString("rechercher.Selection"));
        bsel.setActionCommand("selection");
        bsel.setSelected(dansSelection);
        final ButtonGroup groupe = new ButtonGroup();
        groupe.add(bfichier);
        groupe.add(bsel);
        bfichier.addActionListener(this);
        bsel.addActionListener(this);
        optpane.add(bfichier);
        optpane.add(bsel);
        chkmaj = new JCheckBox(rb.getString("rechercher.IgnorerCasse"));
        chkmaj.addItemListener(this);
        optpane.add(chkmaj);
        optpane.add(Box.createHorizontalStrut(50));
        if (textPane instanceof JaxeTextPane) {
            typeXpath = new JCheckBox(rb.getString("rechercher.ExprXpath"));
            typeXpath.addItemListener(this);
            optpane.add(typeXpath);
        } else
            typeXpath = null;
        cpane.add(optpane);
        
        // actions
        final JPanel actpane = new JPanel(new FlowLayout());
        btout = new JButton(rb.getString("rechercher.ToutRemplacer"));
        btout.setActionCommand("tout");
        btout.addActionListener(this);
        actpane.add(btout);
        bremplacer = new JButton(rb.getString("rechercher.Remplacer"));
        bremplacer.setActionCommand("remplacer");
        bremplacer.addActionListener(this);
        actpane.add(bremplacer);
        bremplrech = new JButton(rb.getString("rechercher.RemplRech"));
        bremplrech.setActionCommand("remplrech");
        bremplrech.addActionListener(this);
        actpane.add(bremplrech);
        final JButton bprec = new JButton(rb.getString("rechercher.Precedent"));
        bprec.setActionCommand("precedent");
        bprec.addActionListener(this);
        actpane.add(bprec);
        final JButton bsuiv = new JButton(rb.getString("rechercher.Suivant"));
        bsuiv.setActionCommand("suivant");
        bsuiv.addActionListener(this);
        actpane.add(bsuiv);
        cpane.add(actpane);
        getRootPane().setDefaultButton(bsuiv);
        addWindowListener(new WindowAdapter() {
            boolean gotFocus = false;
            @Override
            public void windowActivated(final WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    tfRechercher.requestFocus();
                    gotFocus = true;
                }
            }
        });
        pack();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getSize().width)/2,(screen.height - getSize().height)/2);
        setVisible(true);
        setResizable(false);
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("suivant".equals(cmd)){
            if (!rechXpath)
                suivant();
            else 
                suivantXpath();
        }
        else if ("precedent".equals(cmd)){
            if (!rechXpath)
                precedent();
            else 
                precedentXpath();
        }
        else if ("remplacer".equals(cmd))
            remplacer();
        else if ("tout".equals(cmd))
            toutRemplacer();
        else if ("remplrech".equals(cmd))
            remplRech();
        else if ("fichier".equals(cmd))
            dansSelection = false;
        else if ("selection".equals(cmd))
            dansSelection = true;
        else if ("xpath".equals(cmd)) {
            setVisible(false);
            if (jdoc.cfg != null) {
                new DialogueXpath(this, jdoc);
            } else {
                JOptionPane.showMessageDialog(this, rb.getString("rechercher.ErrConfig"),
                    rb.getString("rechercher.ExprXpath"), JOptionPane.INFORMATION_MESSAGE);
                setVisible(true);
            }
        }
    }
    
    public void itemStateChanged(final ItemEvent e) {
        if (e.getSource() == chkmaj) {
            ignorerCasse = (e.getStateChange() == ItemEvent.SELECTED);
        }
        if (e.getSource() == typeXpath) {
            rechXpath = (e.getStateChange() == ItemEvent.SELECTED);
            if (rechXpath) {
                enableButton(false);
                bpane.add(bXpath);
            } else {
                enableButton(true);
                setTitle(rb.getString("rechercher.Rechercher"));
                bpane.remove(bXpath);
            }
            cpane.updateUI();
            pack();
        }
    }
    
    public void enableButton(final boolean b) {
        btout.setEnabled(b);
        bremplacer.setEnabled(b);
        bremplrech.setEnabled(b);
        tfRemplacer.setEditable(b);
        bfichier.setEnabled(b);
        bsel.setEnabled(b);
        chkmaj.setEnabled(b);
        if (b)
            tfRechercher.setText(texteRecherche);
        else
            tfRechercher.setText(texteXpathRecherche);
    }
    
    public void rechercher() {
        texteRecherche = tfRechercher.getText();
        if (texteRecherche == null || texteRecherche.length() == 0)
            return;
        final int len = texteRecherche.length();
        int ind = -1;
        String text;
        // recherche bourrin
        try {
            for (int i=0; i<doc.getLength()-len; i++) {
                text = doc.getText(i, len);
                if (text.equals(texteRecherche)) {
                    ind = i;
                    break;
                }
            }
        } catch (final BadLocationException ex) {
            LOG.error("rechercher() - BadLocationException", ex);
            return;
        }
        if (ind != -1) {
            textPane.setCaretPosition(ind);
            textPane.moveCaretPosition(ind+len);
        } else
            getToolkit().beep();
    }
    
    public void suivantXpath() {
        suivantXpath(textPane.getCaretPosition());
    }
    
    private void changerTitre(final int numero, final int total) {
        String titre = rb.getString("rechercher.nbXpath").replace("NUMRESULT", Integer.toString(numero));
        titre = titre.replace("NBTOTAL", Integer.toString(total));
        setTitle(rb.getString("rechercher.Rechercher") + " " + titre);
    }
    
    public void suivantXpath(final int rech_pos) {
        int goTo = rech_pos;
        texteXpathRecherche = tfRechercher.getText();
        if (texteXpathRecherche == null || texteXpathRecherche.length() == 0)
            return;
        final NodeList nodeOkXpath = getXpathNodeList(tfRechercher.getText());
        if (nodeOkXpath != null) {
            for (int i=0; i<nodeOkXpath.getLength(); i++) {
                final JaxeElement je = jdoc.getElementForNode(nodeOkXpath.item(i));
                if (je != null) {
                    final int debNode = je.debut.getOffset();
                    if (debNode > rech_pos) {
                        goTo = debNode;
                        changerTitre(i+1, nodeOkXpath.getLength());
                        break;
                    }
                }
            }
            if ((nodeOkXpath.getLength() > 0) && (goTo == rech_pos)) {
                changerTitre(1, nodeOkXpath.getLength());
                final JaxeElement je = jdoc.getElementForNode(nodeOkXpath.item(0));
                if (je != null)
                    goTo = je.debut.getOffset();
            }
            if (nodeOkXpath.getLength() != 0) {
                try {
                    textPane.scrollRectToVisible(textPane.modelToView(doc.getLength()));
                    textPane.scrollRectToVisible(textPane.modelToView(goTo));
                } catch (final BadLocationException ex) {
                    LOG.error("suivantXpath(int)", ex);
                    return;
                }
                textPane.setCaretPosition(goTo);
            } else {
                getToolkit().beep();
                setTitle(rb.getString("rechercher.Rechercher"));
            }
        }
        toFront();
    }
    
    public void precedentXpath() {
        precedentXpath(textPane.getCaretPosition());
    }
    
    public void precedentXpath(final int rech_pos) {
        int goTo = rech_pos;
        texteXpathRecherche = tfRechercher.getText();
        if (texteXpathRecherche == null || texteXpathRecherche.length() == 0)
            return;
        final NodeList nodeOkXpath = getXpathNodeList(tfRechercher.getText());
        if (nodeOkXpath != null) {
            for (int i=(nodeOkXpath.getLength()-1); i>=0; i--) {
                final JaxeElement je = jdoc.getElementForNode(nodeOkXpath.item(i));
                if (je != null) {
                    final int debNode = je.debut.getOffset();
                    if (debNode < rech_pos) {
                        goTo = debNode;
                        changerTitre(i+1, nodeOkXpath.getLength());
                        break;
                    }
                }
            }
            if ((nodeOkXpath.getLength() > 0) && (goTo == rech_pos)) {
                changerTitre(1, nodeOkXpath.getLength());
                final JaxeElement je = jdoc.getElementForNode(nodeOkXpath.item(nodeOkXpath.getLength()-1));
                if (je != null)
                    goTo = je.debut.getOffset();
            }
            if (nodeOkXpath.getLength() != 0) {
                try {
                    textPane.scrollRectToVisible(textPane.modelToView(doc.getLength()));
                    textPane.scrollRectToVisible(textPane.modelToView(goTo));
                } catch (final BadLocationException ex) {
                    LOG.error("precedentXpath(int)", ex);
                    return;
                }
                textPane.setCaretPosition(goTo);
            } else {
                getToolkit().beep();
                setTitle(rb.getString("rechercher.Rechercher"));
            }
        }
    }
    
    public NodeList getXpathNodeList(final String nodePath) {
        try {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    final ArrayList<String> espaces = jdoc.cfg.listeEspaces();
                    for (final String espace : espaces) {
                        if (!"".equals(espace)) {
                            if (prefix != null && prefix.equals(jdoc.cfg.prefixeEspace(espace)))
                                return(espace);
                        }
                    }
                    return(jdoc.DOMdoc.lookupNamespaceURI(prefix));
                }
                public String getPrefix(String namespaceURI) {
                    return(jdoc.cfg.prefixeEspace(namespaceURI));
                }
                public Iterator getPrefixes(String namespaceURI) {
                    final ArrayList<String> al = new ArrayList<String>();
                    al.add(jdoc.cfg.prefixeEspace(namespaceURI));
                    return(al.iterator());
                }
            });
            return( (NodeList)xpath.evaluate(nodePath, jdoc.DOMdoc, XPathConstants.NODESET) );
        } catch (final XPathExpressionException e) {
            String message = e.getMessage();
            if (message == null && e.getCause() != null)
                message = e.getCause().getMessage();
            JOptionPane.showMessageDialog(this, message, rb.getString("rechercher.ErrXpath"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    public void suivant() {
        suivant(textPane.getSelectionStart());
    }
    
    // recherche suivant a partir de rech_pos
    public void suivant(int rech_pos) {
        if (rechXpath) {
            suivantXpath(rech_pos);
            return;
        }
        texteRecherche = tfRechercher.getText();
        if (texteRecherche == null || texteRecherche.length() == 0)
            return;
        final int len = texteRecherche.length();
        if (len >= doc.getLength()) {
            getToolkit().beep();
            return;
        }
        if ((rech_pos + len) > doc.getLength()) 
            rech_pos = 0;
        int ind = -1;
        try {
            if ((textPane.getSelectionEnd() - textPane.getSelectionStart()) == len) {
                final String texte = doc.getText(rech_pos, len);
                if ((!ignorerCasse && texte.equals(texteRecherche)) ||
                        (ignorerCasse && texte.equalsIgnoreCase(texteRecherche))) 
                    rech_pos++;
            }
            for (int i=rech_pos; i<doc.getLength()-len; i++) {
                if (includeComponent(i, len) == -1) {
                    final String texte = doc.getText(i, len);
                    if ((!ignorerCasse && texte.equals(texteRecherche)) ||
                            (ignorerCasse && texte.equalsIgnoreCase(texteRecherche))) {
                        ind = i;
                        break;
                    }
                } else
                    i = includeComponent(i, len);
            }
        } catch (final BadLocationException ex) {
            LOG.error("suivant(int) - BadLocationException", ex);
            return;
        }
        if (ind != -1) {
            textPane.getCaret().setSelectionVisible(true); // for Windows (see bug 4273908)
            textPane.setCaretPosition(ind);
            textPane.moveCaretPosition(ind + len);
        } else if (rech_pos != 0)
            suivant(0);
        else
            getToolkit().beep();
        toFront();
    }

    public void precedent() {
        precedent (textPane.getSelectionStart());
    }
    
    // recherche precedent a partir de rech_pos
    public void precedent(int rech_pos) {
        texteRecherche = tfRechercher.getText();
        if (texteRecherche == null || texteRecherche.length() == 0)
            return;
        final int len = texteRecherche.length();
        if (len >= doc.getLength()) {
            getToolkit().beep();
            return;
        }
        int ind = -1;
        if (rech_pos + len > doc.getLength())
            rech_pos = doc.getLength() - len;
        if (rech_pos < 0)
            rech_pos = 0;
        try {
            if ((textPane.getSelectionEnd() - textPane.getSelectionStart()) == len) {
                final String texte = doc.getText(rech_pos, len);
                if ((!ignorerCasse && texte.equals(texteRecherche)) ||
                        (ignorerCasse && texte.equalsIgnoreCase(texteRecherche))) 
                    rech_pos--;
            }
            for (int i=rech_pos; i>=0; i--) {
                if (includeComponent(i, len) == -1) {
                    final String texte = doc.getText(i, len);
                    if ((!ignorerCasse && texte.equals(texteRecherche)) ||
                            (ignorerCasse && texte.equalsIgnoreCase(texteRecherche))) {
                        ind = i;
                        break;
                    }
                } else
                    i = includeComponent(i, len) - len + 1;
            }
        } catch (final BadLocationException ex) {
            LOG.error("precedent(int) - BadLocationException", ex);
            return;
        }
        if (ind != -1) {
            textPane.getCaret().setSelectionVisible(true);
            textPane.setCaretPosition(ind);
            textPane.moveCaretPosition(ind+len);
        } else if (rech_pos != (doc.getLength() - len))
            precedent(doc.getLength() - len);
        else
            getToolkit().beep();
        toFront();
    }

    public void remplacer() {
        final String texteRecherche = tfRechercher.getText();
        final String texteRemplacer = tfRemplacer.getText();
        if (textPane.getSelectionStart() == textPane.getSelectionEnd())
            return;
        final int start = textPane.getSelectionStart();
        final int end = textPane.getSelectionEnd();
        try {
            if (!texteRecherche.equals("")) {
                final int lenRech = texteRecherche.length();
                final int lenRemp = texteRemplacer.length();
                int fin = end-lenRech+1;
                for (int i=start; i<fin; i++) {
                    final String texte = doc.getText(i, lenRech);
                    if ((!ignorerCasse && texteRecherche.equals(texte)) ||
                            (ignorerCasse &&texteRecherche.equalsIgnoreCase(texte))) {
                        if (includeComponent(i, lenRech) == -1) {
                            doc.remove(i, lenRech);
                            doc.insertString(i, texteRemplacer, null);
                            fin = fin - lenRech + lenRemp;
                            i += lenRemp - 1;
                        } else
                            i = includeComponent(i, lenRech);
                    }
                }
                textPane.setSelectionStart(start);
                textPane.setSelectionEnd(fin + lenRech - 1);
            } else {
                if (start != end)
                    doc.remove(start, end - start);
                doc.insertString(start, texteRemplacer, null);
            }
        } catch (final BadLocationException ex) {
            LOG.error("remplacer() - BadLocationException", ex);
            return;
        }
    }

    public void toutRemplacer() {
        if (dansSelection) {
             remplacer();
             return;
        }
        texteRecherche = tfRechercher.getText();
        if (texteRecherche == null || texteRecherche.length() == 0)
            return;
        final String texteRemplacer = tfRemplacer.getText();
        textPane.setSelectionStart(0);
        textPane.setSelectionEnd(0);
        if (textPane instanceof JaxeTextPane)
            ((JaxeTextPane)textPane).debutEditionSpeciale(rb.getString("rechercher.Remplacer"), false);
        final int len = texteRecherche.length();
        int ind = -1;
        final int i0 = 0;
        int ifin = doc.getLength()-len;
        try {
            for (int i=i0; i<ifin; i++) {
                if (includeComponent(i, len) == -1) {
                    final String texte = doc.getText(i, len);
                    if ((!ignorerCasse && texte.equals(texteRecherche)) ||
                            (ignorerCasse && texte.equalsIgnoreCase(texteRecherche))) {
                        ind = i;
                        doc.remove(i, len);
                        doc.insertString(i, texteRemplacer, null);
                        ifin = doc.getLength() - len;
                    }
                } else
                    i = includeComponent(i, len);
            }
        } catch (final BadLocationException ex) {
            LOG.error("toutRemplacer() - BadLocationException", ex);
            if (textPane instanceof JaxeTextPane)
                ((JaxeTextPane)textPane).finEditionSpeciale();
            return;
        }
        if (ind == -1)
            getToolkit().beep();
        if (textPane instanceof JaxeTextPane)
            ((JaxeTextPane)textPane).finEditionSpeciale();
    }

    public void remplRech() {
        remplacer();
        suivant();
    }
    
    public String getTexteRecherche() {
        return(tfRechercher.getText());
    }
    
    public void setTexteRecherche(final String texte) {
        tfRechercher.setText(texte);
    }
    
    private int includeComponent(final int position, final int longueur) {
        int posResult = -1;
        for (int i=position; i<longueur+position; i++) {
            final Element element = doc.getCharacterElement(i);
            final Component component = StyleConstants.getComponent(element.getAttributes());
            final Icon icon = StyleConstants.getIcon(element.getAttributes());
            if ((component != null) || (icon != null))
                posResult = i;
        }
        return posResult;
    }
    
}
