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
 * This class arange a element lower, and a other
 * elements upper to an element
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathSubSup extends MathElement
{

  /** The XML element from this class */
  public final static String ELEMENT = "msubsup";

    /**
     * Add a math element as a child
     *
     * @param child Math element
     */
    @Override
    public void addMathElement(final MathElement child)
    {
        super.addMathElement(child);
        if (child != null)
        {
            if ((getMathElementCount() == 2) || (getMathElementCount() == 3))
                child.setFontSize(getFontSize() - 2);
            else
                child.setFontSize(getFontSize());
        }
    }

  /**
   * Sets the font size for this component
   *
   * @param fontsize Font size
   */
  @Override
public void setFontSize(final int fontsize)
  {
    super.setFontSize(fontsize);
    if (getMathElement(1)!=null)
      getMathElement(1).setFontSize(getFontSize()-2);
    if (getMathElement(2)!=null)
      getMathElement(2).setFontSize(getFontSize()-2);
  }

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
        final MathElement e1 = getMathElement(0);
        final MathElement e2 = getMathElement(1);
        final MathElement e3 = getMathElement(2);

        // int middleshift = getMiddleShift();
        final int childmiddleshift = e2.getMiddleShift();
        // int posY1 = Math.max(posY+e2.getAscentHeight(true)-middleshift,
        // posY+e1.getDescentHeight(true)+middleshiftchild);
        // int posY2 = Math.min(posY-(e3.getAscentHeight(true)+middleshift),
        // posY-e1.getAscentHeight(true)+middleshiftchild);
        final int posY1 = posY + e1.getDescentHeight(true) + childmiddleshift / 2;
        final int posY2 = posY - e1.getAscentHeight(true) + childmiddleshift;

        e1.paint(g, posX, posY);
        e2.paint(g, posX + e1.getWidth(true), posY1);
        e3.paint(g, posX + e1.getWidth(true), posY2);
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
        return getMathElement(0).getWidth(dynamicParts)
                     + Math.max(getMathElement(1).getWidth(dynamicParts),
                                            getMathElement(2).getWidth(dynamicParts));
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
        return getAscentHeight(true) + getDescentHeight(true);
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
        return Math.max(getMathElement(0).getAscentHeight(true),
                                        getMathElement(2).getHeight(true) + getMiddleShift());
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
        return Math.max(getMathElement(0).getDescentHeight(true),
                                        getMathElement(1).getHeight(true) - getMiddleShift());
    }
}
