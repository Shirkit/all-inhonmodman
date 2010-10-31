/*
 * Created on 22.07.2005
 */
package jaxe.elements;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TableHelper {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TableHelper.class);

    int nblignes;

    int nbcolonnes;

    String TRtag;

    String TDtag;

    String THtag;

    String colspanAttr;

    String rowspanAttr;

    Element[][] grille;

    ArrayList<Element> tableRows;

    public TableHelper(final Node noeud, final String TRtag, final String TDtag, final String THtag,
            final String colspanAttr, final String rowspanAttr) {
        this.TRtag = TRtag;
        this.TDtag = TDtag;
        this.THtag = THtag;
        this.colspanAttr = colspanAttr;
        this.rowspanAttr = rowspanAttr;
        grille = creerGrille(noeud);
    }

    public Element[][] getGrille() {
        return grille;
    }
    
    public Element[][] updateGrille(final Node n) {
        return creerGrille(n);
    }
    
    private Element[][] creerGrille(final Node noeud) {
        tableRows = updateTableRows(noeud);

        nblignes = calculerNbLignes(noeud);
        nbcolonnes = calculerNbColonnes(noeud);
        grille = new Element[nblignes][nbcolonnes];
        for (int iil = 0; iil < nblignes; iil++)
            for (int iic = 0; iic < nbcolonnes; iic++)
                grille[iil][iic] = null;
        int il = 0;
        int ic = 0;
        for (Element tr = premiereLigne((Element) noeud); tr != null; tr = ligneSuivante(tr)) {
            for (Node n2 = tr.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
                if (n2.getNodeType() == Node.ELEMENT_NODE) {
                    final String bal2 = n2.getLocalName();
                    if (bal2.equals(TDtag) || bal2.equals(THtag)) {
                        while (ic < nbcolonnes && grille[il][ic] != null)
                            ic++;
                        int icolspan = 1;
                        if (colspanAttr != null) {
                            final String colspan = ((Element) n2).getAttribute(colspanAttr);
                            if (!"".equals(colspan)) {
                                try {
                                    icolspan = Math.max(Integer.parseInt(colspan), 1);
                                } catch (final NumberFormatException e) {
                                }
                            }
                        }
                        int irowspan = 1;
                        if (rowspanAttr != null) {
                            final String rowspan = ((Element) n2).getAttribute(rowspanAttr);
                            if (!"".equals(rowspan)) {
                                try {
                                    irowspan = Math.max(Integer.parseInt(rowspan), 1);
                                } catch (final NumberFormatException e) {
                                }
                            }
                        }
                        // System.out.println(il+" "+ic+" "+irowspan+" "+icolspan);
                        for (int iil = 0; iil < irowspan; iil++)
                            for (int iic = 0; iic < icolspan; iic++) {
                                if (ic + iic >= nbcolonnes || il + iil >= nblignes)
                                    LOG.error("creerGrille(Node) - Erreur: nombre de cellules dans la ligne " + il
                                            + " du tableau");
                                else {
                                    grille[il + iil][ic + iic] = (Element) n2;
                                    /*
                                     * if (n2.getFirstChild() != null) System.out.println((il + iil)+","+(ic + iic)+" = " +
                                     * n2.getFirstChild().getNodeValue()); else System.out.println((il + iil)+","+(ic +
                                     * iic)+" = null");
                                     */
                                }
                            }
                        ic += icolspan;
                    }
                }
            }
            il++;
            ic = 0;
        }
        return grille;
    }

    public int calculerNbLignes(final Node noeud) {
        int nb = 0;
        final Element el = (Element) noeud;
        for (Element tr = premiereLigne(el); tr != null; tr = ligneSuivante(tr)) {
            if (rowspanAttr != null) {
                final String rowspan = tr.getAttribute(rowspanAttr);
                if ("".equals(rowspan))
                    nb++;
                else {
                    int irowspan;
                    try {
                        irowspan = Math.max(Integer.parseInt(rowspan), 1);
                    } catch (final NumberFormatException e) {
                        irowspan = 1;
                    }
                    nb += irowspan;
                    }
            } else {
                nb++;
            }
        }
        return nb;
    }

    public int calculerNbColonnes(final Node noeud) {
        int nb = 0;
        for (int i = 0; i < nblignes; i++) {
            int cols = 0;
            final Element tr = trouverLigne(i, noeud);
            if (tr != null) {
                for (Node n = tr.getFirstChild(); n != null; n = n.getNextSibling())
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        final String bal = n.getLocalName();
                        if (bal.equals(TDtag) || bal.equals(THtag)) {
                            if (colspanAttr != null) {
                                final String colspan = ((Element) n).getAttribute(colspanAttr);
                                if ("".equals(colspan))
                                    cols++;
                                else {
                                    int icolspan;
                                    try {
                                        icolspan = Math.max(Integer.parseInt(colspan), 1);
                                    } catch (final NumberFormatException e) {
                                        icolspan = 1;
                                    }
                                    cols += icolspan;
                                }
                            } else {
                                cols++;
                            }
                        }
                    }
            }
            nb = Math.max(nb, cols);
        }
        return nb;
    }

    public Element premiereLigne(final Element table) {
        if (tableRows.size() > 0) {
            return tableRows.get(0);
        }
        return null;
    }

    public Element ligneSuivante(final Node n) {
        final int pos = tableRows.indexOf(n);

        if ((pos >= 0) && (pos < tableRows.size() - 1)) {
            return tableRows.get(pos + 1);
        }

        return null;
    }

    private ArrayList<Element> updateTableRows(final Node table) {
        final ArrayList<Element> list = new ArrayList<Element>();

        Node child = table.getFirstChild();

        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(TRtag)) {
                list.add((Element)child);
            }
            child = child.getNextSibling();
        }

        return list;
    }

    /**
     * Returns the List of Rows
     * @return List of Rows
     */
    public List<Element> getTableRows() {
        return tableRows;
    }

    public Element trouverLigne(final int lsel, final Node noeud) {
        final Element el = (Element) noeud;
        int l = 0;
        for (Element tr = premiereLigne(el); tr != null; tr = ligneSuivante(tr)) {
            if (l == lsel)
                return (tr);
            l++;
        }
        return (null);
    }

    public int numeroLigne(final Element tr) {
        return tableRows.indexOf(tr);
    }

    public Element trouverCellule(final Element tr, final int csel) {
        final int il = numeroLigne(tr);
        if (il >= nblignes || csel >= nbcolonnes)
            return (null);
        return (grille[il][csel]);
    }

    /**
     * Find out if the Table is within another Table
     * @param node Node
     * @return True if Node is in another Table
     */
    public boolean inTable(Node node) {

        while (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            if (node.getLocalName().equals(TDtag) || node.getLocalName().equals(THtag)) {
                return true;
            }
            node = node.getParentNode();
        }

        return false;
    }

    /**
     * Gibt alle Nodes in der Tabelle als Liste zurück
     * @return Nodes in der Tabelle
     */
    public List<Element> getAllNodes() {
        final ArrayList<Element> list = new ArrayList<Element>();

        for (final Element[] element : grille)
            for (final Element element0 : element) {
                list.add(element0);
            }

        return list;
    }
}