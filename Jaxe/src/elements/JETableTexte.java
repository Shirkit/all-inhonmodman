/*
 Jaxe - Editeur XML en Java

 Copyright (C) 2002 Observatoire de Paris-Meudon

 Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

 Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

 Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
 */

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import jaxe.Balise;
import jaxe.DialogueAttributs;
import jaxe.DialogueChamps;
import jaxe.ImageKeeper;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;
import jaxe.JaxeResourceBundle;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Table dans le texte, permettant l'insertion de sous-éléments dans les cellules.
 * Type d'élément Jaxe: 'tabletexte'
 * paramètre: trTag: un attribut correspondant à une ligne de tableau
 * paramètre: tdTag: un attribut correspondant à une cellule de tableau
 * paramètre: thTag: un attribut correspondant à une cellule d'entête de tableau
 * paramètre: colspanAttr: Attributename for colspan
 * paramètre: rowspanAttr: Attributename for rowspan
 */
public class JETableTexte extends JaxeElement implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JETableTexte.class);

    static String newline = "\n";
    String tableTag = null;
    String TRtag = "tr";
    String TDtag = "td";
    String THtag = null;
    String colspanAttr = null;
    String rowspanAttr = null;
    String alignAttr = null;
    JTable jtable = null;
    boolean avecEntete;
    int nblignes;
    int nbcolonnes;
    Element[][] grille; // utile pour gérer colspan et rowspan
    boolean inTable = true;
    private JPanel pboutons;
    
    TableHelper helper;

    public JETableTexte(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    protected void obtenirTags(final Element refElement) {
        if (refElement != null) {
            tableTag = doc.cfg.nomElement(refElement);
            TRtag = doc.cfg.valeurParametreElement(refElement, "trTag", TRtag);
            TDtag = doc.cfg.valeurParametreElement(refElement, "tdTag", TDtag);
            THtag = doc.cfg.valeurParametreElement(refElement, "thTag", THtag);
            colspanAttr = doc.cfg.valeurParametreElement(refElement, "colspanAttr", colspanAttr);
            rowspanAttr = doc.cfg.valeurParametreElement(refElement, "rowspanAttr", rowspanAttr);
            alignAttr = doc.cfg.valeurParametreElement(refElement, "alignAttr", alignAttr);
        }
    }
    
    @Override
    public void init(Position pos, final Node noeud) {
        final Element el = (Element) noeud;
        if (refElement != null)
            obtenirTags(refElement);
        
        helper = new TableHelper(noeud, TRtag, TDtag, THtag, colspanAttr, rowspanAttr);

        grille = helper.getGrille();
        nblignes = grille.length;
        if (nblignes == 0)
            nbcolonnes = 0;
        else
            nbcolonnes = grille[0].length;

        Style s = doc.textPane.addStyle(null, null);

        if (!helper.inTable(noeud.getParentNode())) {
            inTable = false;
            pboutons = new JPanel();
            pboutons.setBackground(Color.lightGray);
            pboutons.setForeground(Color.black);
            pboutons.setCursor(Cursor.getDefaultCursor());
            pboutons.setLayout(new FlowLayout(FlowLayout.LEFT));
            final JButton bmodtable = new JButton(getString("table.Table"));
            bmodtable.addActionListener(this);
            bmodtable.setActionCommand("modtable");
            petitBouton(bmodtable);
            pboutons.add(bmodtable);
            pboutons.add(Box.createRigidArea(new Dimension(5, 0)));
            final JButton bmodligne = new JButton(getString("table.Ligne"));
            bmodligne.addActionListener(this);
            bmodligne.setActionCommand("modligne");
            petitBouton(bmodligne);
            pboutons.add(bmodligne);
            final JButton bajligne = new JButton("+");
            bajligne.addActionListener(this);
            bajligne.setActionCommand("ajligne");
            petitBouton(bajligne);
            pboutons.add(bajligne);
            final JButton bsupligne = new JButton("-");
            bsupligne.addActionListener(this);
            bsupligne.setActionCommand("supligne");
            petitBouton(bsupligne);
            pboutons.add(bsupligne);
            pboutons.add(Box.createRigidArea(new Dimension(5, 0)));
            final JLabel lcol = new JLabel(getString("table.Colonne"));
            lcol.setFont(lcol.getFont().deriveFont((float) 9));
            pboutons.add(lcol);
            final JButton bajcolonne = new JButton("+");
            bajcolonne.addActionListener(this);
            bajcolonne.setActionCommand("ajcolonne");
            petitBouton(bajcolonne);
            pboutons.add(bajcolonne);
            final JButton bsupcolonne = new JButton("-");
            bsupcolonne.addActionListener(this);
            bsupcolonne.setActionCommand("supcolonne");
            petitBouton(bsupcolonne);
            pboutons.add(bsupcolonne);
            pboutons.add(Box.createRigidArea(new Dimension(5, 0)));
            final JButton bmodcellule = new JButton(getString("table.Cellule"));
            bmodcellule.addActionListener(this);
            bmodcellule.setActionCommand("modcellule");
            petitBouton(bmodcellule);
            pboutons.add(bmodcellule);
            pboutons.add(Box.createRigidArea(new Dimension(5, 0)));
            if (THtag != null) {
                final Element tr = helper.trouverLigne(0, noeud);
                Element tdh = null;
                if (tr != null)
                    tdh = helper.trouverCellule(tr, 0);
                avecEntete = tdh != null && THtag.equals(tdh.getLocalName());
                final JCheckBox bcheck = new JCheckBox(getString("table.Entete"), avecEntete);
                bcheck.addActionListener(this);
                bcheck.setActionCommand("entête");
                bcheck.setFont(bcheck.getFont().deriveFont((float) 9));
                pboutons.add(bcheck);
            } else
                avecEntete = false;

            final Element reftd = doc.cfg.referenceElement(TDtag);
            ArrayList<Element> lattributs = null;
            if (reftd != null)
                lattributs = doc.cfg.listeAttributs(reftd);
            boolean avecRowspan = false;
            boolean avecColspan = false;
            if (lattributs != null)
                for (final Element el2 : lattributs) {
                    final String nomAtt = doc.cfg.nomAttribut(el2);
                    if (rowspanAttr != null && rowspanAttr.equals(nomAtt))
                        avecRowspan = true;
                    else if (colspanAttr != null && colspanAttr.equals(nomAtt))
                        avecColspan = true;
                }
            if (avecColspan) {
                final JButton concatColumns = new JButton(new ImageIcon(ImageKeeper.loadImage("images/concatcolumn.png")));
                petitBouton(concatColumns);
                concatColumns.setActionCommand("concatCols");
                concatColumns.setToolTipText(getString("table.ConcatColumns"));
                concatColumns.addActionListener(this);

                pboutons.add(concatColumns);

                final JButton splitColumns = new JButton(new ImageIcon(ImageKeeper.loadImage("images/splitcolumn.png")));
                petitBouton(splitColumns);
                splitColumns.setToolTipText(getString("table.SplitColumns"));
                splitColumns.setActionCommand("splitCols");
                splitColumns.addActionListener(this);

                pboutons.add(splitColumns);
            }
            if (avecRowspan) {
                final JButton concatRows = new JButton(new ImageIcon(ImageKeeper.loadImage("images/concatrow.png")));
                petitBouton(concatRows);
                concatRows.setToolTipText(getString("table.ConcatRows"));
                concatRows.setActionCommand("concatRows");
                concatRows.addActionListener(this);

                pboutons.add(concatRows);

                final JButton splitRows = new JButton(new ImageIcon(ImageKeeper.loadImage("images/splitrow.png")));
                petitBouton(splitRows);
                splitRows.setToolTipText(getString("table.SplitRows"));
                splitRows.setActionCommand("splitRows");
                splitRows.addActionListener(this);

                pboutons.add(splitRows);
            }

            pos = insertComponent(pos, pboutons);
            insertText(pos, "\n\n");
        } else {
            insertText(pos, "\n");
        }

        final int offsetdebut = pos.getOffset();

        final JaxeDocument.SwingElementSpec tableSpec = preparerSpecTable(el, offsetdebut);

        final javax.swing.text.Element elSwing = doc.insereSpec(tableSpec, offsetdebut);
        creerElementsTableJaxe(el, elSwing);

        // correction des indentations
        s = doc.textPane.addStyle(null, null);
        StyleConstants.setLeftIndent(s, 0);
        doc.setParagraphAttributes(debut.getOffset(), fin.getOffset() - debut.getOffset(), s, false);
    }
    
    /**
     * Réduit la taille d'un bouton pour qu'il tienne dans une barre d'outils
     */
    private void petitBouton(final JButton b) {
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
    
    /**
     * Converts a String to Int
     * @param str String
     * @param def Default-Value
     * @return Value, if Value was 0 it will be set to def
     */
    private int stringToInt(final String str, final int def) {
        int num = def;
        try {
            num = Integer.parseInt(str);
            if (num == 0) {
                num = def;
            }
        } catch (final Exception e) {
        }

        return Math.max(num, 1);
    }
    
    /**
     * Regroupe la cellule dans laquelle se trouve le curseur avec la cellule en-dessous
     */
    private void concatRows() {
        final JaxeElement jesel = cellulesel();
        if (jesel == null) // No element selected
            return;

        // get Current Number
        int num = stringToInt(((Element) jesel.noeud).getAttribute(rowspanAttr), 1);

        if (colspanAttr != null) {
            final int colnum = stringToInt(((Element) jesel.noeud).getAttribute(colspanAttr), 1);
            if (colnum > 1) {
                JOptionPane.showMessageDialog(doc.jframe, getString("table.noConcat"));
                return;
            }
        }


        // get Position in Grille
        final Point p = getPointInGrille(jesel);

        // Try to get next Cell in next Row and parse it's Rowspan
        if (grille.length > p.y + num) {
            final Element el = grille[p.y + num][p.x];

            if (el != null) {
                int addnum = 1;

                final String numstr = el.getAttribute(rowspanAttr);

                addnum = stringToInt(numstr, 1);

                if (colspanAttr != null) {
                    final int colnum = stringToInt(el.getAttribute(colspanAttr), 1);
                    if (colnum > 1) {
                        JOptionPane.showMessageDialog(doc.jframe, getString("table.noConcat"));
                        return;
                    }
                }

                final int result = JOptionPane.showConfirmDialog(doc.jframe, getString("table.BottomDeleteWarning"),
                        getString("table.Attention"), JOptionPane.YES_NO_OPTION);

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
                doc.textPane.getUndo().discardAllEdits();
                doc.textPane.miseAJourAnnulation();

                final ArrayList<Object> allcomp = recupererComposants();

                // Add new rowspan-Value
                num += addnum;
                ((Element) jesel.noeud).setAttributeNS(doc.cfg.espaceAttribut(rowspanAttr), rowspanAttr, Integer.toString(num));

                    // Remove the Cell in the next Row
                final Element parent = (Element) el.getParentNode();
                parent.removeChild(el);

                // If the Row is empty, decrease row-spans in the rows before
                // and remove the empty row
                if (!parent.hasChildNodes()) {

                    for (int y = 0; y <= p.y; y++) {
                        for (int x = 0; x < grille[y].length; x++) {
                            final Element ele = grille[y][x];

                            if (ele != null) {
                                int rownums = stringToInt(ele.getAttribute(rowspanAttr), 1);
                                final int colnums;
                                if (colspanAttr != null) {
                                    colnums = stringToInt(ele.getAttribute(colspanAttr), 1);
                                } else {
                                    colnums = 1;
                                }

                                for (int v = 0; v > rownums; v++) {
                                    for (int w = 0; w > colnums; w++) {
                                        grille[y + rownums][w + colnums] = null;
                                    }
                                }

                                if (rownums + y > p.y && rownums > 1) {
                                    rownums--;
                                    ele.setAttributeNS(doc.cfg.espaceAttribut(rowspanAttr), rowspanAttr, Integer.toString(rownums));
                                }
                            }
                        }
                    }

                    parent.getParentNode().removeChild(parent);
                }
                doc.setModif(true);

                // Recreate Table
                recreerTable(allcomp);
            } else {
                LOG.debug("Could not concat rows, cell was empty!");
            }

        }

    }
    
    /**
     * Sépare la cellule en deux verticalement (sur 2 lignes)
     */
    private void splitRows() {
        final JaxeElement jesel = cellulesel();
        if (jesel == null)
            return;

        // Get the Number of Rowspans
        int num = stringToInt(((Element) jesel.noeud).getAttribute(rowspanAttr), 1);

        if (colspanAttr != null) {
            final int colnum = stringToInt(((Element) jesel.noeud).getAttribute(colspanAttr), 1);
            if (colnum > 1) {
                JOptionPane.showMessageDialog(doc.jframe, getString("table.noSplit"));
                return;
            }
        }


        final ArrayList<Object> allcomp = recupererComposants();

        if (num > 1) {
            doc.textPane.getUndo().discardAllEdits();
            doc.textPane.miseAJourAnnulation();
            final Point p = getPointInGrille(jesel);

            final Element td = nouvelElementDOM(doc, TDtag, (Element) jesel.noeud);

            // Get TR-Node
            final Node node = helper.ligneSuivante(jesel.noeud.getParentNode());

            // If tr was found
            if (node.getLocalName().equals(TRtag)) {
                boolean added = false;

                // Decrease Number
                num--;
                ((Element) jesel.noeud).setAttributeNS(doc.cfg.espaceAttribut(rowspanAttr), rowspanAttr, Integer.toString(num));

                // find position in tr to insert it
                Node child = node.getFirstChild();
                while (child != null && !added) {

                    if (TDtag.equals(child.getLocalName())) {
                        final Point np = getPointInGrille(doc.getElementForNode(child));
                        if (np.y == p.y + num && np.x > p.x) {
                            node.insertBefore(td, child);
                            added = true;
                        }
                    }

                    child = child.getNextSibling();
                }

                if (!added) {
                    node.appendChild(td);
                }

                doc.setModif(true);
            }
        }

        recreerTable(allcomp);
    }
    
    /**
     * Regroupe la colonne dans laquelle se trouve le curseur avec la cellule à droite
     */
    private void concatColumns() {
        final JaxeElement jesel = cellulesel();
        if (jesel == null)
            return;

        // Get Number of Colspan
        int num = stringToInt(((Element) jesel.noeud).getAttribute(colspanAttr), 1);

        if (rowspanAttr != null) {
            final int rownum = stringToInt(((Element) jesel.noeud).getAttribute(rowspanAttr), 1);
            if (rownum > 1) {
                JOptionPane.showMessageDialog(doc.jframe, getString("table.noConcat"));
                return;
            }
        }


        // Get Position in Array
        final Point p = getPointInGrille(jesel);

        if (grille[p.y].length > p.x + num) {
            final Element el = grille[p.y][p.x + num];

            if (el != null) {
                final String numstr = el.getAttribute(colspanAttr);

                final int addnum = stringToInt(numstr, 1);

                if (rowspanAttr != null) {
                    final int rownum = stringToInt(el.getAttribute(rowspanAttr), 1);
                    if (rownum > 1) {
                        JOptionPane.showMessageDialog(doc.jframe, getString("table.noConcat"));
                        return;
                    }
                }

                final int result = JOptionPane.showConfirmDialog(doc.jframe, getString("table.RightDeleteWarning"),
                        getString("table.Attention"), JOptionPane.YES_NO_OPTION);

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
                doc.textPane.getUndo().discardAllEdits();
                doc.textPane.miseAJourAnnulation();

                final ArrayList<Object> allcomp = recupererComposants();

                // Remove unneeded Cell
                el.getParentNode().removeChild(el);
                num += addnum;

                // Set new Colspan
                ((Element) jesel.noeud).setAttributeNS(doc.cfg.espaceAttribut(colspanAttr), colspanAttr, Integer.toString(num));

                doc.setModif(true);

                recreerTable(allcomp);
            } else {
                LOG.debug("Could not concat columns, cell was empty!");
            }

        }

    }
    
    /**
     * Sépare la cellule en deux horizontalement (sur 2 colonnes)
     */
    private void splitColumns() {
        final JaxeElement jesel = cellulesel();
        if (jesel == null)
            return;

        // get Number of Colspans
        int num = stringToInt(((Element) jesel.noeud).getAttribute(colspanAttr), 1);

        if (rowspanAttr != null) {
            final int rownum = stringToInt(((Element) jesel.noeud).getAttribute(rowspanAttr), 1);
            if (rownum > 1) {
                JOptionPane.showMessageDialog(doc.jframe, getString("table.noSplit"));
                return;
            }
        }

        final ArrayList<Object> allcomp = recupererComposants();
        if (num > 1) {
            doc.textPane.getUndo().discardAllEdits();
            doc.textPane.miseAJourAnnulation();
            // Decrease Colspans
            num--;
            ((Element) jesel.noeud).setAttributeNS(doc.cfg.espaceAttribut(colspanAttr), colspanAttr, Integer.toString(num));

            // Create new Element
            final Element td = nouvelElementDOM(doc, TDtag, (Element) jesel.noeud);
            jesel.noeud.getParentNode().insertBefore(td, jesel.noeud.getNextSibling());
        }

        doc.setModif(true);
        
        recreerTable(allcomp);
    }
    
    /**
     * Returns the Position of a JaxeElement in the Grille
     * @param jesel Element to find the Position for
     */
    protected Point getPointInGrille(final JaxeElement jesel) {
        if (jesel == null) {
            return null;
        }

        for (int y = 0; y < grille.length; y++) {
            for (int x = 0; x < grille[y].length; x++) {
                if (grille[y][x] == jesel.noeud) {
                    return new Point(x, y);
                }
            }
        }

        return null;
    }
    
    /**
     * modif de JaxeElement.mettreAJourDOM pour éviter l'enregistrement de \n\n après <TABLE>
     */
    @Override
    public void mettreAJourDOM() {
        if (debut == null || fin == null)
            return;
        for (JaxeElement je = getFirstChild(); je != null; je = je.getNextSibling())
            je.mettreAJourDOM();
    }
    
    /**
     * Renvoit la spécification qui permettra de créer la table dans la zone de texte.
     */
    protected JaxeDocument.SwingElementSpec preparerSpecTable(final Element el, final int offset) {
        final JaxeDocument.SwingElementSpec tableSpec = doc.prepareSpec("table");
        int offc = offset;
        for (Element tr = helper.premiereLigne(el); tr != null; tr = helper.ligneSuivante(tr)) {
            final JaxeDocument.SwingElementSpec ligneSpec = doc.prepareSpec("tr");
            doc.sousSpec(tableSpec, ligneSpec);
            for (Node n2 = tr.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
                if (n2.getNodeType() == Node.ELEMENT_NODE) {
                    final String bal2 = n2.getLocalName();
                    if (bal2.equals(TDtag) || bal2.equals(THtag)) {
                        String colspan = null;
                        if (colspanAttr != null && ((Element) n2).hasAttribute(colspanAttr)) {
                            colspan = ((Element) n2).getAttribute(colspanAttr);
                            if ("".equals(colspan))
                                colspan = null;
                        }
                        String rowspan = null;
                        if (rowspanAttr != null && ((Element) n2).hasAttribute(rowspanAttr)) {
                            rowspan = ((Element) n2).getAttribute(rowspanAttr);
                            if ("".equals(rowspan))
                                rowspan = null;
                        }
                        String align = null;
                        if (alignAttr != null && ((Element) n2).hasAttribute(alignAttr)) {
                            align = ((Element) n2).getAttribute(alignAttr);
                            if ("".equals(align))
                                align = null;
                        }

                        final JaxeDocument.SwingElementSpec celluleSpec;
                        if (colspan != null || rowspan != null|| align != null) {
                            final SimpleAttributeSet att = new SimpleAttributeSet();
                            if (colspan != null)
                                att.addAttribute(javax.swing.text.html.HTML.Attribute.COLSPAN, colspan);
                            if (rowspan != null)
                                att.addAttribute(javax.swing.text.html.HTML.Attribute.ROWSPAN, rowspan);

                            if (align != null) {
                                if ("center".equalsIgnoreCase(align)) {
                                    StyleConstants.setAlignment(att, StyleConstants.ALIGN_CENTER);
                                } else if ("left".equalsIgnoreCase(align)) {
                                    StyleConstants.setAlignment(att, StyleConstants.ALIGN_LEFT);
                                } else if ("right".equalsIgnoreCase(align)) {
                                    StyleConstants.setAlignment(att, StyleConstants.ALIGN_RIGHT);
                                } else if ("justify".equalsIgnoreCase(align)) {
                                    StyleConstants.setAlignment(att, StyleConstants.ALIGN_JUSTIFIED);
                                }
                            }

                            celluleSpec = doc.prepareSpec("td", att);
                        } else
                            celluleSpec = doc.prepareSpec("td");
                        doc.sousSpec(ligneSpec, celluleSpec);
                        // Object contenuCelluleSpec = doc.prepareSpec("tdd");
                        final JaxeDocument.SwingElementSpec contenuCelluleSpec = doc
                                .prepareSpec(AbstractDocument.ParagraphElementName);
                        doc.sousSpec(celluleSpec, contenuCelluleSpec);
                        final String sval = "\n";
                        final JaxeDocument.SwingElementSpec contenuSpec = doc.prepareSpec("content", offc, sval);
                        offc += sval.length();
                        doc.sousSpec(contenuCelluleSpec, contenuSpec);
                    }
                }
            }
            // on ignore le reste
        }
        return tableSpec;
    }
    
    /**
     * Création des éléments Swing (JESwing) correspondants aux lignes et cellules de la table.
     */
    protected void creerElementsTableJaxe(final Element elDOM, final javax.swing.text.Element elSwing) {
        setEditionAutorisee(false);
        javax.swing.text.Element trSwing = null;
        int itrSwing = 0;
        Position dernierePos = fin;
        for (Element tr = helper.premiereLigne(elDOM); tr != null; tr = helper.ligneSuivante(tr)) {
            if (itrSwing >= elSwing.getElementCount())
                LOG.error(
                    "JETableTexte.creerElementsTableJaxe(): Erreur: arbre swing != arbre DOM (ligne)",
                    null);
            else {
                trSwing = elSwing.getElement(itrSwing++);
                dernierePos = creerElementsLigneJaxe(tr, trSwing);
            }
        }
        fin = dernierePos;
    }
    
    /**
     * Appelé par creerElementsTableJaxe pour créer les éléments Swing (JESwing) correspondants à une ligne de la table.
     */
    protected Position creerElementsLigneJaxe(final Element trDOM, final javax.swing.text.Element trSwing) {
        javax.swing.text.Element tdSwing = null;
        Position dernierePos = null;
        final JESwing trje = new JESwing(doc, trDOM, trSwing);
        trje.creer(trje.debut, trDOM);
        trje.setEffacementAutorise(false);
        trje.setEditionAutorisee(false);
        final int offsetdebutLigne = trje.debut.getOffset();
        int itdSwing = 0;
        for (Node n2 = trDOM.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
            if (n2.getNodeType() == Node.ELEMENT_NODE) {
                final String bal2 = n2.getLocalName();
                if (bal2.equals(TDtag) || bal2.equals(THtag)) {
                    if (itdSwing >= trSwing.getElementCount())
                        LOG.error(
                            "JETableTexte.creerElementsLigneJaxe(): Erreur: arbre swing != arbre DOM (cellule)");
                    else {
                        tdSwing = trSwing.getElement(itdSwing++);
                        final JESwingTD tdje = new JESwingTD(doc, (Element) n2, tdSwing);
                        final int offsetdebut = tdje.debut.getOffset();
                        tdje.creer(tdje.debut, n2);
                        tdje.setEffacementAutorise(false);
                        tdje.creerEnfants(tdje.debut);
                        tdje.fin = tdje.debut;
                        try {
                            tdje.debut = doc.createPosition(offsetdebut);
                        } catch (final BadLocationException ex) {
                            LOG.error("creerElementsLigneJaxe(Element, javax.swing.text.Element)", ex);
                        }
                        dernierePos = tdje.fin;
                        if (dernierePos.getOffset() - offsetdebut > 0) {
                            final SimpleAttributeSet style = tdje.attStyle(null);
                            if (style != null)
                                doc.setCharacterAttributes(offsetdebut, dernierePos.getOffset() - offsetdebut, style,
                                        false);
                        }
                    }
                }
            }
        }
        try {
            trje.debut = doc.createPosition(offsetdebutLigne);
        } catch (final BadLocationException ex) {
            LOG.error("creerElementsLigneJaxe(Element, javax.swing.text.Element)", ex);
        }
        trje.fin = dernierePos;
        return dernierePos;
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        final String[] titres = { JaxeResourceBundle.getRB().getString("table.NbLignes"),
                JaxeResourceBundle.getRB().getString("table.NbColonnes") };

        final JTextComponent[] champs = new JTextComponent[2];
        champs[0] = new JTextField(10);
        champs[1] = new JTextField(10);

        final DialogueChamps dlg_dim = new DialogueChamps(doc.jframe, JaxeResourceBundle.getRB()
                .getString("table.NouvelleBalise"), titres, champs);
        if (!dlg_dim.afficher())
            return null;

        final int nlignes;
        final int ncolonnes;

        try {
            nlignes = Integer.parseInt(champs[0].getText());
            ncolonnes = Integer.parseInt(champs[1].getText());
        } catch (final NumberFormatException ne) {
            return null;
        }
        
/*        try {
            nlignes = (Integer.valueOf(slignes)).intValue();
            ncolonnes = (Integer.valueOf(scolonnes)).intValue();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(doc.jframe, JaxeResourceBundle.getRB().getString("erreur.Conversion"),
                JaxeResourceBundle.getRB().getString("table.NouvelleBalise"), JOptionPane.ERROR_MESSAGE);
            return(null);
        } */
        if (nlignes <= 0 || ncolonnes <= 0)
            return null;

        obtenirTags(refElement);
        avecEntete = false;

        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null)
            return null;
        if (testAffichageDialogue()) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                JaxeResourceBundle.getRB().getString("zone.NouvelleBalise") + " " + doc.cfg.titreElement(refElement), refElement, newel);
            if (!dlg.afficher())
                return null;
            dlg.enregistrerReponses();
        }
        Node textnode = doc.DOMdoc.createTextNode(newline);
        newel.appendChild(textnode);
        for (int i = 0; i < nlignes; i++) {
            final Element ligneel = nouvelElementDOM(doc, TRtag, newel);
            newel.appendChild(ligneel);
            for (int j = 0; j < ncolonnes; j++) {
                final Element cellel = nouvelElementDOM(doc, TDtag, ligneel);
                ligneel.appendChild(cellel);
            }
            textnode = doc.DOMdoc.createTextNode(newline);
            newel.appendChild(textnode);
        }
        this.doc.setModif(true);

        return newel;
    }
    
    @Override
    public boolean avecSautsDeLigne() {
        return (true);
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
        final Element el = (Element) noeud;

        final ArrayList<Element> latt = doc.cfg.listeAttributs(refElement);
        if (latt != null && latt.size() > 0) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc, el.getTagName(), refElement, el);
            if (dlg.afficher()) {
                dlg.enregistrerReponses();
                doc.setModif(true);
            }
            dlg.dispose();
        }
    }
    
    protected void rechercherComposants(final JaxeElement je, final ArrayList<Object> al) {
        al.addAll(je.getComponents());
        for (Node n = je.noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            rechercheComposantsNoeud(n, al);
        }
    }
    
    /**
     * Search all Childs, a thead can have Nodes, too. BUT a thead is *not* visible
     * @param noeud Node to search in
     * @param al Arraylist of Components
     */
    protected void rechercheComposantsNoeud(final Node noeud, final ArrayList<Object> al) {
        if (noeud.getNodeType() == Node.ELEMENT_NODE || noeud.getNodeType() == Node.TEXT_NODE
                || noeud.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            final JaxeElement je2 = doc.getElementForNode(noeud);
            if (je2 != null) {
                rechercherComposants(je2, al);
            } else {
                for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
                    rechercheComposantsNoeud(n, al);
                }
            }
        }

    }
    
    /**
     * Renvoit la liste des composants graphiques (JComponent ou Icon) à l'intérieur de la table
     * (dans cet élément ou l'un de ses descendants).
     */
    protected ArrayList<Object> recupererComposants() {
        final int offsetdebut = getOffsetDebut();
        final ArrayList<JaxeElement> tel = elementsDans(offsetdebut, fin.getOffset());
        final ArrayList<Object> allcomp = new ArrayList<Object>();
        for (final JaxeElement je : tel) 
            rechercherComposants(je, allcomp);
        return allcomp;
    }
    
    /**
     * Renvoit les éléments se trouvant dans la zone du texte indiquée
     */
    @Override
    public ArrayList<JaxeElement> elementsDans(final int dpos, final int fpos) {
        final ArrayList<JaxeElement> l = new ArrayList<JaxeElement>();
        if (debut == null || fin == null)
            return l;
        if (debut.getOffset() > fpos || fin.getOffset() < dpos)
            return l;
        if (debut.getOffset() >= dpos && fin.getOffset() <= fpos)
            l.add(this);

        l.addAll(elementsDansNoeud(noeud, dpos, fpos));

        return l;

    }
    
    /**
     * Runs through every Node. If no JaxeElement is available for a node, in all Childs of the Node will be searched
     * @param noeud Node to search in
     * @param dpos Position
     * @param fpos Position
     * @return List of all JaxeElements
     */
    private ArrayList<JaxeElement> elementsDansNoeud(final Node noeud, final int dpos, final int fpos) {
        final ArrayList<JaxeElement> l = new ArrayList<JaxeElement>();

        for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE
                    || n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {

                final JaxeElement je = doc.getElementForNode(n);
                if (je != null) {
                    l.addAll(je.elementsDans(dpos, fpos));
                } else {
                    l.addAll(elementsDansNoeud(n, dpos, fpos));
                }
            }
        }

        return l;
    }
    
    protected void effacerComposants(final ArrayList<Object> allcomp) {
        // on utiliser parentContainer.remove pour retirer les composants, sinon un bug de Java 1.4 les affiche
        for (final Object obj : allcomp) {
            if (obj instanceof JComponent) {
                final Container parentContainer = ((JComponent) obj).getParent();
                if (parentContainer != null)
                    parentContainer.remove((JComponent) obj);
            }
        }
    }
    
    /**
     * Returns the OffsetDebut of the Table
     * @return OffsetDebut
     */
    public int getOffsetDebut() {
        final int offsetdebut;
        if (inTable) {
            offsetdebut = debut.getOffset() + 1;
        } else {
            offsetdebut = debut.getOffset() + 3;
        }
        return offsetdebut;
    }
    
    /**
     * Recrée l'affichage de la table. Les composants graphiques qui se trouvent à l'intérieur
     * sont passés en paramètres pour pouvoir être effacés correctement avant d'être reconstruits.
     */
    public void recreerTable(final ArrayList<Object> allcomp) {
        final int caretpos = doc.textPane.getCaretPosition();
        doc.textPane.debutIgnorerEdition();

        try {
            doc.remove(debut.getOffset() + 1, fin.getOffset() - debut.getOffset());
        } catch (final BadLocationException ex) {
            LOG.error("recreerTable(ArrayList)", ex);
        }

        effacerComposants(allcomp);

        if (!inTable) {
            try {
                final Position pos = doc.createPosition(debut.getOffset() + 1);
                insertText(pos, "\n\n");
            } catch (final BadLocationException ex) {
                LOG.error("recreerTable(ArrayList)", ex);
            }
        }

        final int offsetdebut = getOffsetDebut();
        final Element el = (Element) noeud;

        grille = helper.updateGrille(noeud);

        nblignes = grille.length;
        nbcolonnes = grille[0].length;

        final JaxeDocument.SwingElementSpec tableSpec = preparerSpecTable(el, offsetdebut);
        final javax.swing.text.Element elSwing = doc.insereSpec(tableSpec, offsetdebut);

        creerElementsTableJaxe(el, elSwing);

        doc.textPane.finIgnorerEdition();
        if (caretpos < doc.getLength()) {
            doc.textPane.setCaretPosition(caretpos);
            doc.textPane.requestFocus();
        }
    }
    
    /**
     * Ajoute une ligne à cette table. Si le curseur se trouve sur une ligne de cette table,
     * la ligne est ajoutée après la ligne sur laquelle se trouve le curseur. Sinon la ligne
     * est ajoutée à la fin de la table.
     */
    public void ajligne() {
        doc.textPane.getUndo().discardAllEdits();
        doc.textPane.miseAJourAnnulation();
        //final int pos = doc.textPane.getCaretPosition();
        final JaxeElement jetrsel = lignesel();
        final Element trsel;
        if (jetrsel != null)
            trsel = (Element) jetrsel.noeud;
        else
            trsel = null;
        final int rsel;
        if (trsel != null)
            rsel = helper.numeroLigne(trsel);
        else
            rsel = -1;
        Element trnext = null;
        final Element el = (Element) noeud;
        if (jetrsel != null) {
            trnext = helper.ligneSuivante(jetrsel.noeud);
        }
        final Element tr = nouvelElementDOM(doc, TRtag, el);
        final Node textnode = doc.DOMdoc.createTextNode(newline);
        for (int ic = 0; ic < nbcolonnes; ic++) {
            if (rsel != -1 && rsel + 1 < nblignes && grille[rsel][ic] == grille[rsel + 1][ic]) {
                final Element td = grille[rsel][ic];
                int irowspan = 1;
                if (rowspanAttr != null) {
                    final String rowspan = td.getAttribute(rowspanAttr);
                    if (!"".equals(rowspan)) {
                        try {
                            irowspan = Integer.parseInt(rowspan);
                        } catch (final NumberFormatException e) {
                        }
                    }
                    td.setAttributeNS(doc.cfg.espaceAttribut(rowspanAttr), rowspanAttr, Integer.toString(irowspan + 1));
                }
                while (ic + 1 < nbcolonnes && grille[rsel][ic + 1] == td)
                    ic++;
            } else {
                final Element td = nouvelElementDOM(doc, TDtag, tr);
                tr.appendChild(td);
            }
        }
        if (trnext == null) {
            el.appendChild(tr);
            el.appendChild(textnode);
        } else {
            el.insertBefore(tr, trnext);
            el.insertBefore(textnode, trnext);
        }
        /*
        int offset;
        if (trnext != null) {
            jetrsel = doc.getElementForNode(trnext);
            offset = jetrsel.fin.getOffset() + 1;
        } else
            offset = fin.getOffset() + 1;
        Object specLigne = preparerSpecLigne(tr, offset);
        javax.swing.text.Element trSwing = doc.insereSpec(specLigne, offset);
        creerElementsLigneJaxe(tr, trSwing);
        doc.textPane.setCaretPosition(pos);
        doc.textPane.requestFocus();
        */ // la ligne n'est pas insérée au bon endroit...
        recreerTable(recupererComposants());
        this.doc.setModif(true);
    }
    
    /**
     * Supprime la ligne de cette table où se trouve le curseur.
     */
    public void supligne() {
        final JaxeElement jetrsel = lignesel();
        if (jetrsel == null || nblignes == 1)
            return;

        final int result = JOptionPane.showConfirmDialog(doc.jframe, getString("table.RemoveRow"),
                getString("table.Attention"), JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        doc.textPane.getUndo().discardAllEdits();
        doc.textPane.miseAJourAnnulation();

        final ArrayList<Object> allcomp = recupererComposants();

        final Element trsel = (Element) jetrsel.noeud;
        final int rsel = helper.numeroLigne(trsel);
        for (int ic = 0; ic < nbcolonnes; ic++) {
            final Element td = grille[rsel][ic];
            if (td != null) {
                if (rsel > 0 && grille[rsel - 1][ic] == td) {
                    if (rowspanAttr != null) {
                        final String rowspan = td.getAttribute(rowspanAttr);
                        int irowspan = 1;
                        if (!"".equals(rowspan)) {
                            try {
                                irowspan = Integer.parseInt(rowspan);
                            } catch (final NumberFormatException e) {
                            }
                        }
                        td.setAttributeNS(doc.cfg.espaceAttribut(rowspanAttr), rowspanAttr, Integer.toString(irowspan - 1));
                    }
                    while (ic + 1 < nbcolonnes && grille[rsel][ic + 1] == td)
                        ic++;
                } else if (rsel + 1 < nblignes && grille[rsel + 1][ic] == td) {
                    // déplacement de td vers la ligne suivante + réduction rowspan
                    Element td2 = null;
                    int itd2 = 1;
                    while (ic + itd2 < nbcolonnes) {
                        if (grille[rsel + 1][ic + itd2] != td) {
                            td2 = grille[rsel + 1][ic + itd2];
                            break;
                        }
                        itd2++;
                    }
                    ((Element) td.getParentNode()).removeChild(td);
                    Element tr2 = null;
                    if (td2 == null) {
                        tr2 = helper.ligneSuivante(trsel);
                        if (tr2 != null)
                            tr2.appendChild(td);
                    } else {
                        tr2 = (Element) td2.getParentNode();
                        tr2.insertBefore(td, td2);
                    }
                    if (rowspanAttr != null) {
                        final String rowspan = td.getAttribute(rowspanAttr);
                        int irowspan = 1;
                        if (!"".equals(rowspan)) {
                            try {
                                irowspan = Integer.parseInt(rowspan);
                            } catch (final NumberFormatException e) {
                            }
                        }
                        td.setAttributeNS(doc.cfg.espaceAttribut(rowspanAttr), rowspanAttr, Integer.toString(irowspan - 1));
                    }
                }
            }
        }

        try {
            final Node parent = jetrsel.noeud.getParentNode();
            final Node nextSibling = jetrsel.noeud.getNextSibling();
            if (nextSibling != null && nextSibling.getNodeType() == Node.TEXT_NODE)
                parent.removeChild(nextSibling); // retire le \n aprés </TR>
            parent.removeChild(jetrsel.noeud);
        } catch (final DOMException ex) {
            LOG.error("supligne() - DOMException: " + ex.getMessage(), ex);
        }

        recreerTable(allcomp);
        this.doc.setModif(true);
    }
    
    /**
     * Ajoute une colonne à cette table. Si le curseur se trouve sur une colonne de cette table,
     * la colonne est ajoutée après la colonne sur laquelle se trouve le curseur. Sinon la colonne
     * est ajoutée à la droite de la table.
     */
    public void ajcolonne() {
        doc.textPane.getUndo().discardAllEdits();
        doc.textPane.miseAJourAnnulation();
        int csel = colonnesel();
        if (csel == -1)
            csel = nbcolonnes - 1;
        final Element el = (Element) noeud;
        int il = 0;
        for (Element tr = helper.premiereLigne(el); tr != null; tr = helper.ligneSuivante(tr)) {
            if (csel + 1 < nbcolonnes && grille[il][csel] == grille[il][csel + 1]) {
                final Element td = grille[il][csel];
                int icolspan = 1;
                if (colspanAttr != null) {
                    final String colspan = td.getAttribute(colspanAttr);
                    if (!"".equals(colspan)) {
                        try {
                            icolspan = Integer.parseInt(colspan);
                        } catch (final NumberFormatException e) {
                        }
                    }
                    td.setAttributeNS(doc.cfg.espaceAttribut(colspanAttr), colspanAttr, Integer.toString(icolspan + 1));
                }
                while (il + 1 < nblignes && grille[il + 1][csel] == td) {
                    il++;
                    tr = helper.ligneSuivante(tr);
                }
            } else {
                final Element td;
                if (tr == helper.premiereLigne(el) && avecEntete)
                    td = nouvelElementDOM(doc, THtag, tr);
                else
                    td = nouvelElementDOM(doc, TDtag, tr);
                if (csel == -1) {
                    tr.appendChild(td);
                } else {
                    Element tdsel = helper.trouverCellule(tr, csel + 1);

                    // Check if the Parent-Node is correct.
                    // If it's not correct, we got an Cell that spans
                    // over more than 1 Row
                    int i = 1;
                    while (tdsel != null && tdsel.getParentNode() != tr) {
                        i++;
                        tdsel = helper.trouverCellule(tr, csel + i);
                    }

                    if (tdsel == null)
                        tr.appendChild(td);
                    else
                        tr.insertBefore(td, tdsel);
                }
            }
            il++;
        }
        recreerTable(recupererComposants());
        this.doc.setModif(true);
    }
    
    /**
     * Supprime la colonne de cette table où se trouve le curseur.
     */
    public void supcolonne() {
        final int csel = colonnesel();
        if (csel == -1 || nbcolonnes == 1)
            return;

        final int result = JOptionPane.showConfirmDialog(doc.jframe, getString("table.RemoveColumn"),
                getString("table.Attention"), JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        doc.textPane.getUndo().discardAllEdits();
        doc.textPane.miseAJourAnnulation();

        final ArrayList<Object> allcomp = recupererComposants();

        for (int il = 0; il < nblignes; il++) {
            final Element td = grille[il][csel];
            if (td != null) {
                if (csel > 0 && grille[il][csel - 1] == td
                        || csel + 1 < nbcolonnes && grille[il][csel + 1] == td && (csel == 0 || grille[il][csel - 1] != td)) {
                    if (colspanAttr != null) {
                        final String colspan = td.getAttribute(colspanAttr);
                        int icolspan = 1;
                        if (!"".equals(colspan)) {
                            try {
                                icolspan = Integer.parseInt(colspan);
                            } catch (final NumberFormatException e) {
                            }
                        }
                        td.setAttributeNS(doc.cfg.espaceAttribut(colspanAttr), colspanAttr, Integer.toString(icolspan - 1));
                    }
                } else
                    ((Element) td.getParentNode()).removeChild(td);
                while (il + 1 < nblignes && grille[il + 1][csel] == td)
                    il++;
            }
        }

        recreerTable(allcomp);
        this.doc.setModif(true);
    }
    
    /**
     * Transforme les cellules normales de la première ligne en cellules entête, ou inversement.
     */
    public void modifEntete() {
        doc.textPane.getUndo().discardAllEdits();
        doc.textPane.miseAJourAnnulation();

        avecEntete = !avecEntete;
        final Element tr1 = helper.trouverLigne(0, noeud);
        if (tr1 == null)
            return;
        final ArrayList<Object> allcomp = recupererComposants();
        if (avecEntete) {
            for (Node n = tr1.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals(TDtag)) {
                    final Element td = (Element) n;
                    final Element th = nouvelElementDOM(doc, THtag, tr1);
                    final NamedNodeMap attributs = td.getAttributes();
                    for (int i=0; i<attributs.getLength(); i++)
                        th.setAttributeNodeNS((Attr)attributs.item(i).cloneNode(false));
                    Node frero = null;
                    for (Node n2 = td.getFirstChild(); n2 != null; n2 = frero) {
                        frero = n2.getNextSibling();
                        th.appendChild(n2);
                    }
                    tr1.replaceChild(th, td);
                    n = th;
                }
            }
        } else {
            for (Node n = tr1.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals(THtag)) {
                    final Element th = (Element) n;
                    final Element td = nouvelElementDOM(doc, TDtag, tr1);
                    final NamedNodeMap attributs = th.getAttributes();
                    for (int i=0; i<attributs.getLength(); i++)
                        td.setAttributeNodeNS((Attr)attributs.item(i).cloneNode(false));
                    Node frero = null;
                    for (Node n2 = th.getFirstChild(); n2 != null; n2 = frero) {
                        frero = n2.getNextSibling();
                        td.appendChild(n2);
                    }
                    tr1.replaceChild(td, th);
                    n = td;
                }
            }
        }
        recreerTable(allcomp);
        this.doc.setModif(true);
    }
    
    /**
     * Renvoit l'élément Jaxe correspondant à la ligne dans laquelle se trouve le curseur.
     */
    private JaxeElement lignesel() {
        // si on utilise elementA on risque de tomber sur des éléments d'une sous-table
        final int pos = doc.textPane.getCaretPosition();
        final Element el = (Element) noeud;
        for (Element tr = helper.premiereLigne(el); tr != null; tr = helper.ligneSuivante(tr)) {
            final JaxeElement je = doc.getElementForNode(tr);
            if (je.debut.getOffset() <= pos && je.fin.getOffset() >= pos) {
                return je;
            }
        }
        return null;
    }
    
    /**
     * Renvoit le numéro de la colonne dans laquelle se trouve le curseur.
     */
    private int colonnesel() {
        // si on utilise elementA on risque de tomber sur des éléments d'une sous-table
        final JaxeElement jecell = cellulesel();
        if (jecell == null)
            return -1;
        for (int il = 0; il < nblignes; il++)
            for (int ic = 0; ic < nbcolonnes; ic++) {
                if (grille[il][ic] == jecell.noeud)
                    return ic;
            }
        LOG.error("colonnesel() - colonnesel: noeud non trouvé dans la grille: " + jecell.noeud);
        return -1;
    }
    
    /**
     * Renvoit l'élément Jaxe correspondant à la cellule dans laquelle se trouve le curseur.
     */
    protected JaxeElement cellulesel() {
        // si on utilise elementA on risque de tomber sur des éléments d'une sous-table
        final int pos = doc.textPane.getCaretPosition();
        final JaxeElement lsel = lignesel();
        if (lsel == null)
            return null;
        final Element tr = (Element) lsel.noeud;
        for (Node n = tr.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final String bal = n.getLocalName();
                if (bal.equals(TDtag) || bal.equals(THtag)) {
                    final JaxeElement je = doc.getElementForNode(n);
                    if (je.debut.getOffset() <= pos && je.fin.getOffset() >= pos) {
                        return je;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Affiche le dialogue des attributs pour la ligne sélectionnée.
     */
    public void modligne() {
        final JaxeElement jsel = lignesel();
        if (jsel == null)
            return;
        jsel.afficherDialogue(doc.jframe);
        doc.setModif(true);
    }
    
    /**
     * Affiche le dialogue des attributs pour la cellule sélectionnée.
     */
    public void modcellule() {
        final JaxeElement jesel = cellulesel();
        if (jesel == null)
            return;
        jesel.afficherDialogue(doc.jframe);
        majCellule(jesel);
        doc.setModif(true);
    }
    
    /**
     * Mise à jour du modèle et de l'affichage après modification de colspan ou rowspan.
     */
    public void majCellule(final JaxeElement jesel) {
        int icolspan;
        int icolspan2;
        int irowspan;
        int irowspan2;

        if (jesel == null)
            return;
        final int csel = colonnesel();
        final Element el = (Element) jesel.noeud;
        final Element trsel = (Element) el.getParentNode();
        final int rsel = helper.numeroLigne(trsel);

        // obtention des colspan et rowspan d'avant la maj à partir de la grille
        icolspan = 0;
        while (csel + icolspan < nbcolonnes - 1 && grille[rsel][csel + icolspan] == grille[rsel][csel + icolspan + 1])
            icolspan++;
        icolspan++;
        irowspan = 0;
        while (rsel + irowspan < nblignes - 1 && grille[rsel + irowspan][csel] == grille[rsel + irowspan + 1][csel])
            irowspan++;
        irowspan++;

        // nouveaux colspan et rowspan à partir de l'élément DOM
        if (colspanAttr != null) {
            final String colspan2 = ((Element) jesel.noeud).getAttribute(colspanAttr);
            try {
                icolspan2 = Integer.parseInt(colspan2);
            } catch (final NumberFormatException e) {
                icolspan2 = 1;
            }
        } else {
            icolspan2 = 1;
        }
        if (rowspanAttr != null) {
            final String rowspan2 = ((Element) jesel.noeud).getAttribute(rowspanAttr);
            try {
                irowspan2 = Integer.parseInt(rowspan2);
            } catch (final NumberFormatException e) {
                irowspan2 = 1;
            }
        } else {
            irowspan2 = 1;
        }

        // maj du modèle
        if (icolspan != icolspan2 || irowspan != irowspan2) {

            if (icolspan2 > icolspan) {
                int ntd = icolspan2 - icolspan;
                Node nextsibling = jesel.noeud.getNextSibling();
                for (Node n = nextsibling; n != null && ntd > 0; n = nextsibling) {
                    nextsibling = n.getNextSibling();
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        final String bal = n.getLocalName();
                        if (bal.equals(TDtag) || bal.equals(THtag)) {
                            n.getParentNode().removeChild(n);
                            ntd--;
                        }
                    }
                }
            } else if (icolspan > icolspan2) {
                final Element tr = (Element) el.getParentNode();
                final int ntd = icolspan - icolspan2;
                for (int i = 0; i < ntd; i++) {
                    final Element td;
                    if (THtag.equals(el.getLocalName()))
                        td = nouvelElementDOM(doc, THtag, tr);
                    else
                        td = nouvelElementDOM(doc, TDtag, tr);
                    if (el.getNextSibling() == null)
                        tr.appendChild(td);
                    else
                        tr.insertBefore(td, el.getNextSibling());
                }
            }
            if (irowspan2 > irowspan || icolspan2 > icolspan) {
                final int nrow = irowspan2;
                int irow = 1;
                for (Element tr = helper.ligneSuivante(trsel); tr != null && irow < nrow; tr = helper.ligneSuivante(tr)) {
                    if (irowspan2 > irowspan && irow >= irowspan) {
                        Element elsup = helper.trouverCellule(tr, csel);
                        Node nextsibling;
                        final int colsup;
                        if (irowspan2 > irowspan || icolspan2 <= icolspan)
                            colsup = icolspan2;
                        else
                            colsup = icolspan;
                        for (int i = 0; i < colsup && elsup != null; i++) {
                            nextsibling = elsup.getNextSibling();
                            tr.removeChild(elsup);
                            elsup = (Element) nextsibling;
                        }
                    } else if (icolspan2 > icolspan) {
                        Element elsup = helper.trouverCellule(tr, csel + icolspan);
                        Node nextsibling;
                        for (int i = 0; i < icolspan2 - icolspan && elsup != null; i++) {
                            nextsibling = elsup.getNextSibling();
                            tr.removeChild(elsup);
                            elsup = (Element) nextsibling;
                        }
                    }
                    irow++;
                }
            }
            if (irowspan > irowspan2 || icolspan > icolspan2) {
                final int nrow = irowspan;
                int irow = 1;
                for (Element tr = helper.ligneSuivante(trsel); tr != null && irow < nrow; tr = helper.ligneSuivante(tr)) {
                    if (irowspan > irowspan2 && irow >= irowspan2) {
                        Element elsuiv = null;
                        for (int i = csel + 1; i < nbcolonnes; i++)
                            if (grille[rsel + irow][i] != grille[rsel + irow - 1][i]) {
                                elsuiv = grille[rsel + irow][i];
                                break;
                            }
                        final int colaj;
                        if (irowspan > irowspan2 || icolspan <= icolspan2)
                            colaj = icolspan;
                        else
                            colaj = icolspan2;
                        for (int i = 0; i < colaj; i++) {
                            final Element td = nouvelElementDOM(doc, TDtag, tr);
                            if (elsuiv != null)
                                tr.insertBefore(td, elsuiv);
                            else
                                tr.appendChild(td);
                        }
                    } else if (icolspan > icolspan2) {
                        Element elsuiv = null;
                        for (int i = csel + icolspan; i < nbcolonnes; i++)
                            if (grille[rsel + irow][i] != grille[rsel + irow - 1][i]) {
                                elsuiv = grille[rsel + irow][i];
                                break;
                            }
                        for (int i = 0; i < icolspan - icolspan2; i++) {
                            final Element td = nouvelElementDOM(doc, TDtag, tr);
                            if (elsuiv != null)
                                tr.insertBefore(td, elsuiv);
                            else
                                tr.appendChild(td);
                        }
                    }
                    irow++;
                }
            }
            final ArrayList<Object> allcomp = recupererComposants();
            recreerTable(allcomp);
        }
    }
    
    @Override
    public void effacer() {
        effacerComposants(recupererComposants());
        super.effacer();
    }
    
    @Override
    public void selection(final boolean select) {
        Color[][] couleursButtons = Balise.getCouleurs();
        if (select) {
            pboutons.setBackground(couleursButtons[0][1]);
            pboutons.setForeground(couleursButtons[0][0]);
        } else {
            pboutons.setBackground(Color.lightGray);
            pboutons.setForeground(Color.black);
        }
    }
    
    /**
     * Renvoit l'élément Jaxe JETableTexte correspondant à la table de plus bas niveau dans laquelle se trouve le curseur.
     * Permet de distinguer quelle table est sélectionnée quand il y a plusieurs tables imbriquées.
     */
    private JETableTexte getTable() {

        final JaxeElement el = doc.elementA(doc.textPane.getCaretPosition());
        if (el == null) return null;
        Node p = el.noeud;

        while (p != null && !tableTag.equals(p.getLocalName())) {
            p = p.getParentNode();
        }

        if (p != null) {
            return (JETableTexte) doc.getElementForNode(p);
        }

        return null;
    }
    
    public void actionPerformed(final ActionEvent e) {
        if (doc.textPane.isEditable()) {
            final String cmd = e.getActionCommand();
            JETableTexte jetable = getTable();
            if (jetable == null)
                jetable = this;
            if ("ajligne".equals(cmd))
                jetable.ajligne();
            else if ("ajcolonne".equals(cmd))
                jetable.ajcolonne();
            else if ("supligne".equals(cmd))
                jetable.supligne();
            else if ("supcolonne".equals(cmd))
                jetable.supcolonne();
            else if ("entête".equals(cmd))
                this.modifEntete();
            else if ("modtable".equals(cmd)) {
                jetable.afficherDialogue(doc.jframe);
                recreerTable(recupererComposants());
            } else if ("modligne".equals(cmd)) {
                jetable.modligne();
                recreerTable(recupererComposants());
            } else if ("modcellule".equals(cmd)) {
                jetable.modcellule();
                recreerTable(recupererComposants());
            } else if ("splitCols".equals(cmd)) {
                jetable.splitColumns();
            } else if ("splitRows".equals(cmd)) {
                jetable.splitRows();
            } else if ("concatCols".equals(cmd)) {
                jetable.concatColumns();
            } else if ("concatRows".equals(cmd)) {
                jetable.concatRows();
            }
        }
    }
    
    /**
     * Renvoit l'élément de plus bas niveau se trouvant à la position donnée dans le texte
     */
    @Override
    public JaxeElement elementA(final int pos) {
        if (debut == null || fin == null)
            return null;
        if (debut.getOffset() > pos || fin.getOffset() < pos)
            return null;

        if (grille == null) {
            if (helper == null) {
                helper = new TableHelper(noeud, TRtag, TDtag, THtag, colspanAttr, rowspanAttr);
            }
            grille = helper.updateGrille(noeud);
        }

        for (final Element[] element : grille) {
            for (final Node n : element) {
                if (n != null && (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE
                        || n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)) {
                    final JaxeElement je = doc.getElementForNode(n);
                    if (je != null) {
                        final JaxeElement nje = je.elementA(pos);
                        if (nje != null)
                            return nje;
                    }
                }
            }
        }
        return this;
    }
    
    
    class JESwingTD extends JESwing {
        public JESwingTD(final JaxeDocument doc, final Element elDOM, final javax.swing.text.Element elSwing) {
            super(doc, elDOM, elSwing);
        }

        @Override
        public void majAffichage() {
            JETableTexte.this.majCellule(this);
        }
    }
    
}
