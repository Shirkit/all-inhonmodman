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
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Style;

import jaxe.DialogueChamps;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import jaxe.JaxeResourceBundle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Tableau affiché comme tel dans le texte. Les éléments du tableau ne peuvent être que
 * de courts textes.
 * Type d'élément Jaxe: 'tableau'
 * paramètre: trTag: un attribut correspondant à une ligne de tableau
 * paramètre: tdTag: un attribut correspondant à une cellule de tableau
 * paramètre: thTag: un attribut correspondant à une cellule d'entête de tableau
 */
public class JETable extends JaxeElement implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JETable.class);

    static String newline = "\n";
    String TRtag = "tr";
    String TDtag = "td";
    String THtag = null;
    JTable jtable = null;
    boolean avecEntete;

    /*
    Comme les tables JETable ne permettent pas de mettre autre chose que du texte dans
    les cases, il vaut mieux créer une zone à la place si le fichier ouvert contient
    autre chose que du texte dans les cases du tableau ou si des attributs sont utilisés
    dans les balises TD
    */
    public static boolean preferreZone(final JaxeDocument doc, final Element el) {
         // la méthode est statique, il faut utiliser des variables locales...
        String TRtag = "tr";
        String TDtag = "td";
        String THtag = null;
        
        final Element refElement = doc.cfg.premierElementAvecType("tableau");
        if (refElement != null) {
            TRtag = doc.cfg.valeurParametreElement(refElement, "trTag", TRtag);
            TDtag = doc.cfg.valeurParametreElement(refElement, "tdTag", TDtag);
            THtag = doc.cfg.valeurParametreElement(refElement, "thTag", THtag);
        }
        
        for (Node n=el.getFirstChild(); n != null; n=n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final String bal = n.getNodeName();
                if (bal.equals(TRtag)) {
                    for (Node n2=n.getFirstChild(); n2 != null; n2=n2.getNextSibling()) {
                        if (n2.getNodeType() == Node.ELEMENT_NODE) {
                            final String bal2 = n2.getNodeName();
                            if (bal2.equals(TDtag) || bal2.equals(THtag)) {
                                if (n2.getAttributes() != null && n2.getAttributes().getLength() > 0)
                                    return true;
                                for (Node n3=n2.getFirstChild(); n3 != null; n3=n3.getNextSibling()) {
                                    if (n3.getNodeType() == Node.ELEMENT_NODE)
                                        return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public JETable(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    protected void obtenirTags(final Element refElement) {
        if (refElement != null) {
            TRtag = doc.cfg.valeurParametreElement(refElement, "trTag", TRtag);
            TDtag = doc.cfg.valeurParametreElement(refElement, "tdTag", TDtag);
            THtag = doc.cfg.valeurParametreElement(refElement, "thTag", THtag);
        }
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
        final Element el = (Element)noeud;
        if (refElement != null)
            obtenirTags(refElement);
        
        final Style s = doc.textPane.addStyle(null, null);
        
        jtable = makeTable(el);
        
        //jtable.addMouseListener(new MyMouseListener(this, doc.jframe));
        
        final JPanel p = new JPanel(new BorderLayout());
        p.setCursor(Cursor.getDefaultCursor());
        p.add(jtable, BorderLayout.CENTER);
        final JPanel pboutons = new JPanel();
        pboutons.setLayout(new BoxLayout(pboutons, BoxLayout.Y_AXIS));
        if (THtag != null) {
            final NodeList thnl = el.getElementsByTagName(THtag);
            avecEntete = thnl != null && thnl.getLength() > 0;
            final JCheckBox bcheck = new JCheckBox(getString("table.Entete"), avecEntete);
            bcheck.addActionListener(this);
            bcheck.setActionCommand("entête");
            petitBouton(bcheck);
            pboutons.add(bcheck);
        } else
            avecEntete = false;
        final JButton bajligne = new JButton(getString("table.AjouterLigne"));
        bajligne.addActionListener(this);
        bajligne.setActionCommand("ajligne");
        petitBouton(bajligne);
        pboutons.add(bajligne);
        final JButton bajcolonne = new JButton(getString("table.AjouterColonne"));
        bajcolonne.addActionListener(this);
        bajcolonne.setActionCommand("ajcolonne");
        petitBouton(bajcolonne);
        pboutons.add(bajcolonne);
        final JButton bsupligne = new JButton(getString("table.SupprimerLigne"));
        bsupligne.addActionListener(this);
        bsupligne.setActionCommand("supligne");
        petitBouton(bsupligne);
        pboutons.add(bsupligne);
        final JButton bsupcolonne = new JButton(getString("table.SupprimerColonne"));
        bsupcolonne.addActionListener(this);
        bsupcolonne.setActionCommand("supcolonne");
        petitBouton(bsupcolonne);
        pboutons.add(bsupcolonne);
        p.add(pboutons, BorderLayout.EAST);

        insertComponent(pos, p);
    }
    
    /**
     * Réduit la taille d'un bouton pour qu'il tienne dans une barre d'outils
     */
    private void petitBouton(final AbstractButton b) {
        b.setFont(b.getFont().deriveFont((float) 9));
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            if ("10.5".compareTo(System.getProperty("os.version")) <= 0) {
                b.putClientProperty("JComponent.sizeVariant", "small");
                b.putClientProperty("JButton.buttonType", "square");
            } else
                b.putClientProperty("JButton.buttonType", "toolbar");
        } else
            b.setMargin(new java.awt.Insets(1, 2, 1, 2));
    }
    
    protected TableModel makeTableModel(final Element el) {
        final Vector<Vector<Object>> v = new Vector<Vector<Object>>();
        final Vector<String> ventete = new Vector<String>();
        int nligne = 0;
        for (Node n=el.getFirstChild(); n != null; n=n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final String bal = n.getNodeName();
                if (bal.equals(TRtag)) {
                    final Vector<Object> v2 = new Vector<Object>();
                    for (Node n2=n.getFirstChild(); n2 != null; n2=n2.getNextSibling()) {
                        if (n2.getNodeType() == Node.ELEMENT_NODE) {
                            final String bal2 = n2.getNodeName();
                            if (bal2.equals(TDtag) || bal2.equals(THtag)) {
                                final Node n3 = n2.getFirstChild();
                                String sval;
                                if (n3 != null && n3.getNodeValue() != null)
                                    sval = n3.getNodeValue().trim();
                                else
                                    sval = "";
                                v2.add(sval);
                                if (nligne == 0)
                                    ventete.add("");
                            }
                        }
                    }
                    if (nligne == 0 || v2.size() == (v.get(0)).size()) {
                        v.add(v2);
                        nligne++;
                    } else
                        LOG.error("makeTableModel(Element) - Erreur: nombre de <TD> incorrect dans la ligne");
                }
            }
            // on ignore le reste
        }
        return new MyTableModel(v, ventete);
    }
    
    class MyTableModel extends AbstractTableModel {
        Vector<Vector<Object>> rowData;
        Vector<String> columnNames;
        public MyTableModel(final Vector<Vector<Object>> rowData, final Vector<String> columnNames) {
            this.rowData = rowData;
            this.columnNames = columnNames;
        }
        public int getRowCount() {
            return rowData.size();
        }
        public int getColumnCount() {
            return columnNames.size();
        }
        public Object getValueAt(final int row, final int column) {
            return (rowData.elementAt(row)).elementAt(column);
        }
        @Override
        public String getColumnName(final int column) {
            return columnNames.get(column);
        }
        @Override
        public boolean isCellEditable(final int row, final int column) {
            return true;
        }
        @Override
        public void setValueAt(final Object aValue, final int row, final int column) {
            (rowData.elementAt(row)).setElementAt(aValue, column);
            final Element tr = findligne(row);
            final Element td = findcellule(tr, column);
            final String s = (String)aValue;
            if (td.getFirstChild() == null) {
                final Node textnode = doc.DOMdoc.createTextNode(s);
                td.appendChild(textnode);
            } else
                td.getFirstChild().setNodeValue(s);
        }
    }
    
    protected JTable makeTable(final Element el) {
        final JTable ntable = new JTable(makeTableModel(el));
        ntable.setShowGrid(true);
        ntable.setGridColor(Color.black);
        ntable.setDefaultRenderer(Object.class, new CustomCellRenderer());
        return ntable;
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        final String[] titres = {JaxeResourceBundle.getRB().getString("table.NbLignes"),
            JaxeResourceBundle.getRB().getString("table.NbColonnes")};
        final JTextComponent[] champs = new JTextComponent[2];
        champs[0] = new JTextField(10);
        champs[1] = new JTextField(10);
        final DialogueChamps dlg = new DialogueChamps(doc.jframe,
            JaxeResourceBundle.getRB().getString("table.NouvelleBalise"), titres, champs);
        if (!dlg.afficher())
            return null;
        final String slignes = champs[0].getText();
        final String scolonnes = champs[1].getText();
        
        int nlignes, ncolonnes;
        try {
            nlignes = Integer.valueOf(slignes).intValue();
            ncolonnes = Integer.valueOf(scolonnes).intValue();
        } catch (final NumberFormatException ex) {
            JOptionPane.showMessageDialog(doc.jframe, JaxeResourceBundle.getRB().getString("erreur.Conversion"),
                JaxeResourceBundle.getRB().getString("table.NouvelleBalise"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        obtenirTags(refElement);
        avecEntete = false;

        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null) return null;
        for (int i=0; i<nlignes; i++) {
            final Element ligneel = nouvelElementDOM(doc, TRtag, newel);
            newel.appendChild(ligneel);
            for (int j=0; j<ncolonnes; j++) {
                final Element cellel = nouvelElementDOM(doc, TDtag, ligneel);
                ligneel.appendChild(cellel);
            }
            final Node textnode = doc.DOMdoc.createTextNode(newline);
            newel.appendChild(textnode);
        }

        return newel;
    }
    
    @Override
    public boolean avecSautsDeLigne() {
        return (true);
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
    }
    
    @Override
    public void majAffichage() {
        jtable.setModel(makeTableModel((Element)noeud));
    }
    
    @Override
    public void mettreAJourDOM() {
        final Element el = (Element)noeud;
        Element tr = null;
        for (int l=0; l<jtable.getRowCount(); l++) {
            Node nr;
            if (tr == null)
                nr = el.getFirstChild();
            else
                nr = tr.getNextSibling();
            tr = null;
            for (; nr != null && tr == null; nr=nr.getNextSibling())
                if (nr.getNodeType() == Node.ELEMENT_NODE) {
                    final String bal = nr.getNodeName();
                    if (bal.equals(TRtag))
                        tr = (Element)nr;
                }
            if (tr == null) {
                LOG.error("mettreAJourDOM() - Erreur: balise TR non trouvée dans JETable.mettreAJourDOM()");
                return;
            }
            Element td = null;
            for (int c=0; c<jtable.getColumnCount(); c++) {
                Node nd;
                if (td == null)
                    nd = tr.getFirstChild();
                else
                    nd = td.getNextSibling();
                td = null;
                for (; nd != null && td == null; nd=nd.getNextSibling())
                    if (nd.getNodeType() == Node.ELEMENT_NODE) {
                        final String bal = nd.getNodeName();
                        if (bal.equals(TDtag) || bal.equals(THtag))
                            td = (Element)nd;
                    }
                if (td == null) {
                    LOG.error("mettreAJourDOM() - Erreur: balise TD non trouvée dans JETable.mettreAJourDOM()");
                    return;
                }
                final String s = (String)jtable.getValueAt(l, c);
                if (td.getFirstChild() == null) {
                    final Node textnode = doc.DOMdoc.createTextNode(s);
                    td.appendChild(textnode);
                } else
                    td.getFirstChild().setNodeValue(s);
            }
        }
    }
    
    protected Element findligne(final int lsel) {
        final Element el = (Element)noeud;
        int l = 0;
        for (Node n=el.getFirstChild(); n != null; n=n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final String bal = n.getNodeName();
                if (bal.equals(TRtag)) {
                    if (l == lsel) {
                        final Element tr = (Element)n;
                        return tr;
                    }
                    l++;
                }
            }
        }
        return null;
    }
    
    protected Element findcellule(final Element tr, final int csel) {
        int c = 0;
        for (Node n=tr.getFirstChild(); n != null; n=n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final String bal = n.getNodeName();
                if (bal.equals(TDtag) || bal.equals(THtag)) {
                    if (c == csel) {
                        final Element td = (Element)n;
                        return td;
                    }
                    c++;
                }
            }
        }
        return null;
    }
    
    public void ajligne() {
        final int lsel = jtable.getSelectedRow();
        mettreAJourDOM();
        final Element el = (Element)noeud;
        final Element tr = nouvelElementDOM(doc, TRtag, el);
        if (lsel == -1) {
            el.appendChild(tr);
        } else {
            final Element trsel = findligne(lsel+1);
            final Node textnode = doc.DOMdoc.createTextNode(newline);
            if (trsel == null) {
                el.appendChild(tr);
                el.appendChild(textnode);
            } else {
                el.insertBefore(tr, trsel);
                el.insertBefore(textnode, trsel);
            }
        }
        for (int j=0; j<jtable.getColumnCount(); j++) {
            final Element td = nouvelElementDOM(doc, TDtag, tr);
            tr.appendChild(td);
        }
        jtable.setModel(makeTableModel(el));
    }
    
    public void ajcolonne() {
        final int csel = jtable.getSelectedColumn();
        mettreAJourDOM();
        final Element el = (Element)noeud;
        for (Node n=el.getFirstChild(); n != null; n=n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final String bal = n.getNodeName();
                if (bal.equals(TRtag)) {
                    final Element tr = (Element)n;
                    Element td;
                    if (n == el.getFirstChild() && avecEntete)
                        td = nouvelElementDOM(doc, THtag, tr);
                    else
                        td = nouvelElementDOM(doc, TDtag, tr);
                    if (csel == -1) {
                        tr.appendChild(td);
                    } else {
                        final Element tdsel = findcellule(tr, csel+1);
                        if (tdsel == null)
                            tr.appendChild(td);
                        else
                            tr.insertBefore(td, tdsel);
                    }
                }
            }
        }
        jtable.setModel(makeTableModel(el));
    }
    
    public void supligne() {
        final int lsel = jtable.getSelectedRow();
        if (lsel == -1)
            return;
        mettreAJourDOM();
        final Element el = (Element)noeud;
        final Element tr = findligne(lsel);
        if (tr != null) {
            if (tr.getNextSibling() != null && tr.getNextSibling().getNodeType() == Node.TEXT_NODE)
                el.removeChild(tr.getNextSibling());
            el.removeChild(tr);
            jtable.setModel(makeTableModel(el));
        }
    }
    
    public void supcolonne() {
        final int csel = jtable.getSelectedColumn();
        if (csel == -1)
            return;
        mettreAJourDOM();
        final Element el = (Element)noeud;
        for (Node n=el.getFirstChild(); n != null; n=n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final String bal = n.getNodeName();
                if (bal.equals(TRtag)) {
                    final Element td = findcellule((Element)n, csel);
                    if (td != null)
                        n.removeChild(td);
                }
            }
        }
        jtable.setModel(makeTableModel(el));
    }
    
    public void modifEntete() {
        avecEntete = !avecEntete;
        final Element tr1 = findligne(0);
        if (tr1 == null)
            return;
        if (avecEntete) {
            for (Node n = tr1.getFirstChild(); n != null; n=n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(TDtag)) {
                    final Element td = (Element)n;
                    final Node nval = n.getFirstChild();
                    String sval;
                    if (nval != null && nval.getNodeValue() != null)
                        sval = nval.getNodeValue().trim();
                    else
                        sval = "";
                    final Element th = nouvelElementDOM(doc, THtag, tr1);
                    final Node textnode = doc.DOMdoc.createTextNode(sval);
                    th.appendChild(textnode);
                    tr1.replaceChild(th, td);
                    n = th;
                }
            }
        } else {
            for (Node n = tr1.getFirstChild(); n != null; n=n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(THtag)) {
                    final Element th = (Element)n;
                    final Node nval = n.getFirstChild();
                    String sval;
                    if (nval != null && nval.getNodeValue() != null)
                        sval = nval.getNodeValue().trim();
                    else
                        sval = "";
                    final Element td = nouvelElementDOM(doc, TDtag, tr1);
                    final Node textnode = doc.DOMdoc.createTextNode(sval);
                    td.appendChild(textnode);
                    tr1.replaceChild(td, th);
                    n = td;
                }
            }
        }
        jtable.repaint();
    }
    
    // pour avoir la première ligne en gras quand c'est un "entête"
    class CustomCellRenderer extends DefaultTableCellRenderer {
        public CustomCellRenderer() {
        }
    
    @Override
    public Component getTableCellRendererComponent( final JTable table, final Object value,
                final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
            
            if (avecEntete && row == 0)
                setFont(new Font("Helvetica", Font.BOLD, 13));
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );
        
            if (avecEntete && row == 0)
                setFont(new Font("Helvetica", Font.BOLD, 13));
            
            return this;
        }
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("ajligne".equals(cmd))
            ajligne();
        else if ("ajcolonne".equals(cmd))
            ajcolonne();
        else if ("supligne".equals(cmd))
            supligne();
        else if ("supcolonne".equals(cmd))
            supcolonne();
        else if ("entête".equals(cmd))
            modifEntete();
    }

    /*
    class MyMouseListener extends MouseAdapter {
        JETable jei;
        JFrame jframe;
        public MyMouseListener(JETable obj, JFrame jframe) {
            super();
            jei = obj;
            this.jframe = jframe;
        }
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                //int index = list.locationToIndex(e.getPoint());
                //System.out.println("Double clicked on Item " + index);
                jei.afficherDialogue(jframe);
            }
        }
    }
    */

}
