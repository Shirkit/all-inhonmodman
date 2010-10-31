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
 * This class presents a mathematical root
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathRoot extends MathElement
{

  /** The XML element from this class */
  public final static String ELEMENT = "mroot";

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
            if (getMathElementCount() == 2)
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
    if (getMathElementCount()<2)
      return;

        final MathElement e1 = getMathElement(0);
        final MathElement e2 = getMathElement(1);
        final int width = getWidth(true);

        final int width1 = e1.getWidth(true);
        final int height1 = e1.getHeight(true);
        final int width2 = e2.getWidth(true);
        final int height2 = e2.getHeight(true);

        final int aheight1 = e1.getAscentHeight(true);
        final int dheight1 = e1.getDescentHeight(true);

        g.drawLine(posX, posY, posX + width2, posY);
        g.drawLine(posX + width2, posY, posX + height1 / 2 + width2,
                             posY + dheight1);
        g.drawLine(posX + height1 / 2 + width2, posY + dheight1,
                             posX + height1 + width2, posY - (aheight1 + 2));
        g.drawLine(posX + height1 + width2, posY - (aheight1 + 2), posX + width,
                             posY - (aheight1 + 2));

        e1.paint(g, posX + height1 + width2, posY);
        e2.paint(g, posX, posY - e2.getDescentHeight(true));
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
    if (getMathElementCount()<2)
      return 0;

        return getMathElement(0).getWidth(dynamicParts)
                     + getMathElement(0).getHeight(true)
                     + getMathElement(1).getWidth(dynamicParts);
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
    if (getMathElementCount()<2)
      return 0;

        return getDescentHeight(true)
                     + Math.max(getMathElement(0).getAscentHeight(true) + 4,
                                            getMathElement(1).getHeight(true));
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
    if (getMathElementCount()<2)
      return 0;

        return Math.max(getMathElement(0).getAscentHeight(true) + 4,
                                        getMathElement(1).getHeight(true));
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
    if (getMathElementCount()<2)
      return 0;

        return getMathElement(0).getDescentHeight(true);
    }
}
