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
 * This class presents a row in MathTable
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathTableRow extends MathElement
{

  /** The XML element from this class */
  public final static String ELEMENT = "mtr";

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
        if (getMathBase().isDebug())
            debug(g, posX, posY);

        final int columnwidth = getMaxColumnWidth();
        int pos = posX;

        for (int i = 0; i < getMathElementCount(); i++)
        {
            getMathElement(i).paint(g, pos, posY);
            pos += columnwidth;
        }
    }

    /**
     * Returns the maximal width of a column for
   * all columns in this row
     *
     * @return
     */
    private int getMaxColumnWidth()
    {
        int width = 0;

        for (int i = 0; i < getMathElementCount(); i++)
            width = Math.max(width, getMathElement(i).getWidth(true));
        return width;
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
        return getMaxColumnWidth() * getMathElementCount();
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
            height = Math.max(height, getMathElement(i).getHeight(dynamicParts));
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
        int height = 0;

        for (int i = 0; i < getMathElementCount(); i++)
            height = Math.max(height,
                                                getMathElement(i).getAscentHeight(dynamicParts));
        return height;
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
        int height = 0;

        for (int i = 0; i < getMathElementCount(); i++)
            height = Math.max(height,
                                                getMathElement(i).getDescentHeight(dynamicParts));
        return height;
    }
}
