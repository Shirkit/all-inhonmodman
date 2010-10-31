/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations.element;

import java.awt.Graphics;

/**
 * This class presents a table
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathTable extends MathElement
{

  /** The XML element from this class */
  public final static String ELEMENT = "mtable";

    /**
     * Paints this element
     *
     * @param g The graphics context to use for painting
     * @param posX The first left position for painting
     * @param posY The position of the baseline
     */
    @Override
    public void paint(final Graphics g, final int posX, final int posY)
    {
        // debug(g, posX, posY);

        int i, j;
        final int[] maxrowascentheight = new int[getMathElementCount()];
        final int[] maxrowdescentheight = new int[getMathElementCount()];

        for (i = 0; i < getMathElementCount(); i++)
        {
            maxrowascentheight[i] = getMaxRowAscentHeight(i);
            maxrowdescentheight[i] = getMaxRowDescentHeight(i);
        }

        final int maxcolumns = getMaxColumnCount();
        final int[] maxcolumnwidth = new int[maxcolumns];

        for (i = 0; i < maxcolumns; i++)
            maxcolumnwidth[i] = getMaxColumnWidth(i);

        final int x1 = posX;
        final int y1 = -getHeight(true) / 2 + maxrowascentheight[0] - getMiddleShift();

        int x = x1;
        int y = y1;

        for (i = 0; i < getMathElementCount(); i++)
        {
            final MathElement row = getMathElement(i);

            x = x1;
            for (j = 0; (j < maxcolumns) && (j < row.getMathElementCount()); j++)
            {
                // row.getMathElement(j).paint(g, x, posY+y);
                final MathTableData mtd = (MathTableData)row.getMathElement(j);
                if ("left".equals(mtd.getColumnAlign()))
                    mtd.paint(g, x + 1, posY + y);
                else if ("right".equals(mtd.getColumnAlign()))
                    mtd.paint(g, x + maxcolumnwidth[j] - mtd.getWidth(true), posY + y);
                else
                    mtd.paint(g, x + maxcolumnwidth[j] / 2 - mtd.getWidth(true) / 2, posY + y);
                x += maxcolumnwidth[j];
            }

            y += maxrowdescentheight[i];
            if (i < getMathElementCount() - 1)
                y += maxrowascentheight[i + 1];
        }
    }

    /**
     * Returns the maximal ascent height of a row
   * in this table
     *
     * @param row Row     
     *
     * @return Maximal ascent height
     */
    private int getMaxRowAscentHeight(final int row)
    {
        if (row >= getMathElementCount())
            return 0;

        final MathElement child = getMathElement(row);
        int height = 0;

        for (int i = 0; i < child.getMathElementCount(); i++)
            height = Math.max(height,
                                                child.getMathElement(i).getAscentHeight(true));
        return height;
    }

    /**
     * Returns the maximal descent height of a row
   * in this table
     *
     * @param row Row
     *
     * @return Maximal descent height
     */
    private int getMaxRowDescentHeight(final int row)
    {
        if (row >= getMathElementCount())
            return 0;

        final MathElement child = getMathElement(row);
        int height = 0;

        for (int i = 0; i < child.getMathElementCount(); i++)
            height = Math.max(height,
                                                child.getMathElement(i).getDescentHeight(true));
        return height;
    }

    /**
     * Returns the maximal width of a column
   * in this table
     *
     * @param column Column
     *
     * @return Maximal width
     */
    private int getMaxColumnWidth(final int column)
    {
        int width = 0;

        for (int i = 0; i < getMathElementCount(); i++)
        {
            final MathElement child = getMathElement(i);

            if (column < child.getMathElementCount())
                width = Math.max(width, child.getMathElement(column).getWidth(true));
        }
        return width + 1;
    }

    /**
     * Returns the maximal count of columns
     *
     * @return Maximal count of columns
     */
    private int getMaxColumnCount()
    {
        int count = 0;

        for (int i = 0; i < getMathElementCount(); i++)
        {
            final MathElement child = getMathElement(i);

            count = Math.max(count, child.getMathElementCount());
        }
        return count;
    }

    /**
     * Return the current width of this element
     *
     * @param dynamicParts Should be true, if the calculation consider the elements,
     *                     which has not fixed sizes
     *
     * @return Width of this element
     */
    @Override
    public int getWidth(final boolean dynamicParts)
    {
        int width = 0;
        final int maxcolumns = getMaxColumnCount();

        for (int i = 0; i < maxcolumns; i++)
            width += getMaxColumnWidth(i);
        return width;
    }

    /**
     * Return the current height of this element
     *
     * @param dynamicParts Should be true, if the calculation consider the elements,
     *                     which has not fixed sizes
     *
     * @return Height of this element
     */
    @Override
    public int getHeight(final boolean dynamicParts)
    {
        int height = 0;

        for (int i = 0; i < getMathElementCount(); i++)
            height += getMaxRowAscentHeight(i) + getMaxRowDescentHeight(i);
        return height;
    }

    /**
     * Return the current height of the upper part
     * of this component from the baseline
     *
     * @param dynamicParts Should be true, if the calculation consider the elements,
     *                     which has not fixed sizes
     *
     * @return Height of the upper part
     */
    @Override
    public int getAscentHeight(final boolean dynamicParts)
    {
        return getHeight(true) / 2 + getMiddleShift();
    }

    /**
     * Return the current height of the lower part
     * of this component from the baseline
     *
     * @param dynamicParts Should be true, if the calculation consider the elements,
     *                     which has not fixed sizes
     *
     * @return Height of the lower part
     */
    @Override
    public int getDescentHeight(final boolean dynamicParts)
    {
        return getHeight(true) / 2 - getMiddleShift();
    }
}
