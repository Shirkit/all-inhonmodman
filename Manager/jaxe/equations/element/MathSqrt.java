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
 * This class presents a mathematical square root
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathSqrt extends MathElement
{

  /** The XML element from this class */
  public final static String ELEMENT = "msqrt";

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
        final int width = getWidth(true);

        final int width1 = getMathElementsWidth();
        //int height1 = getMathElementsHeight(true);
        final int height1 = 8;
        final int aheight1 = getMathElementsAscentHeight(true);
        final int dheight1 = getMathElementsDescentHeight(true);

        g.drawLine(posX, posY, posX + 2, posY);
        g.drawLine(posX + 2, posY, posX + height1 / 2, posY + dheight1);
        g.drawLine(posX + height1 / 2, posY + dheight1, posX + height1 + 2,
                             posY - (aheight1 + 1));
        g.drawLine(posX + height1 + 2, posY - (aheight1 + 1), posX + width - 1,
                             posY - (aheight1 + 1));

        int pos = posX + height1 + 2;
        MathElement child;

        for (int i = 0; i < getMathElementCount(); i++)
        {
            child = getMathElement(i);
            child.paint(g, pos, posY);
            pos += child.getWidth(true);
        }
    }

    /**
     * Returns the width of the childs
     *
     * @return Width of childs
     */
    private int getMathElementsWidth()
    {
        int width = 0;

        for (int i = 0; i < getMathElementCount(); i++)
            width += getMathElement(i).getWidth(true);
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
        //return getMathElementsWidth() + getMathElementsHeight(true) + 2;
        return getMathElementsWidth() + 8 + 2;
    }

    /**
     * Returns the maximal height of the childs
     *
     * @param dynamicParts Should be true, if the calculation consider the elements,
   *                     which has not fixed sizes
     *
     * @return Maximal height of childs
     */
    private int getMathElementsHeight(final boolean dynamicParts)
    {
        int height = 0;

        for (int i = 0; i < getMathElementCount(); i++)
            height = Math.max(height, getMathElement(i).getHeight(true));
        return height;
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
        return getMathElementsHeight(true) + 4;
    }

    /**
     * Return the maximal height of the upper part
   * frm the childs 
     *
     * @param dynamicParts Should be true, if the calculation consider the elements,
   *                     which has not fixed sizes
     *
     * @return Maximal height of the upper parts from the childs
     */
    private int getMathElementsAscentHeight(final boolean dynamicParts)
    {
        int height = 0;

        for (int i = 0; i < getMathElementCount(); i++)
            height = Math.max(height, getMathElement(i).getAscentHeight(true));
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
        return getMathElementsAscentHeight(true) + 2;
    }

  /**
   * Return the maximal height of the lower part
   * frm the childs 
   *
   * @param dynamicParts Should be true, if the calculation consider the elements,
   *                     which has not fixed sizes
   *
   * @return Maximal height of the lower parts from the childs
   */
    private int getMathElementsDescentHeight(final boolean dynamicParts)
    {
        int height = 0;

        for (int i = 0; i < getMathElementCount(); i++)
            height = Math.max(height, getMathElement(i).getDescentHeight(true));
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
        return getMathElementsDescentHeight(true) + 2;
    }
}
