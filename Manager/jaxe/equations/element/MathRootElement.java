/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations.element;

import java.awt.Color;
import java.awt.Graphics;

/**
 * The root element for creating a MathElement tree
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathRootElement extends MathElement
{
 
    /** The XML element from this class */
    public final static String ELEMENT = "math"; 

    /** Attribute name of the mode property */
    public final static String ATTRIBUTE_MODE = "mode";

    /** Inline mathematical expression */
    public final static int INLINE = 0;

    /** Non inline mathematical expression */
    public final static int DISPLAY = 1;

    private int mode = INLINE;

  private boolean debug = false;

    /**
     * Set the type of equation
     *
     * @param mode INLINE|DISPLAY
     */
    public void setMode(final int mode)
    {
    if ((mode==INLINE) || (mode==DISPLAY))
          this.mode = mode;
    }

  /**
   * Returns the mode
   *
   * @return Display mode
   */
  public int getMode()
  { 
    return mode;
  }

  /**
   * Enables, or disables the debug mode
   *
   * @param debug Debug mode
   */
  public void setDebug(final boolean debug)
  {
    this.debug = debug;
  }

  /**
   * Indicates, if the debug mode is enabled
   *
   * @return True, if the debug mode is enabled
   */
  public boolean isDebug()
  {
    return debug;
  }

  /**
   * Paints this component and all of its elements
   *
   * @param g The graphics context to use for painting
   */
  public void paint(final Graphics g)
  {
    if (debug)
    {
      g.setColor(Color.blue);
      g.drawLine(0, 0, getWidth() - 1, 0);
      g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);
      g.drawLine(0, 0, 0, getHeight() - 1);
      g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);

      g.setColor(Color.cyan);
      g.drawLine(0, getHeight() / 2, getWidth() - 1, getHeight() / 2);

      g.setColor(Color.black);
    }

    /*if (mode == DISPLAY)
      paint(g, 0, getAscentHeight(true));
    else
      paint(g, 0, getHeight() / 2 + 1);*/
    // see explanation for change in getHeight
    if (getMathElement(0) != null) {
        final int ha = getMathElement(0).getAscentHeight(true) - getMiddleShift();
        final int hb = getMathElement(0).getDescentHeight(true) + getMiddleShift();
        if (ha >= hb)
            paint(g, 0, getAscentHeight(true));
        else
            paint(g, 0, hb + getMiddleShift());
    }
  }

  /**
   * Return the current width of this component
   *
   * @return Width
   */
  public int getWidth()
  {
    return getWidth(true);
  }

  /**
   * Return the current height of this component
   *
   * @return Height
   */
  public int getHeight()
  {
    return getHeight(true);
  }

    /**
     * Paints this component and all of its elements
     *
     * @param g The graphics context to use for painting
     * @param posX The first left position for painting
     * @param posY The position of the baseline
     */
    @Override
    public void paint(final Graphics g, final int posX, final int posY)
    {
        if (getMathElement(0) != null)
      getMathElement(0).paint(g, posX, posY);
    }

    /**
     * Return the current width of this component
     *
     * @return Width
     */
    @Override
    public int getWidth(final boolean dynamicParts)
    {
        if (getMathElement(0) == null)
            return 0;

        return getMathElement(0).getWidth(true) + 1;
    }

    /**
     * Return the current height of this component
     *
     * @return Height
     */
    @Override
    public int getHeight(final boolean dynamicParts)
    {
        if (getMathElement(0) == null)
            return 0;

        if (mode == DISPLAY)
            return getMathElement(0).getHeight(true) + 2;
        /*return Math.max(getMathElement(0).getAscentHeight(true), 
                    getMathElement(0).getDescentHeight(true)) * 2;*/
        // HTML's align=middle is deprecated, and can be replaced by CSS vertical-align:middle
        // but the position is not the same: it's baseline + height('x')/2 instead of the baseline
        final int ha = getMathElement(0).getAscentHeight(true) - getMiddleShift();
        final int hb = getMathElement(0).getDescentHeight(true) + getMiddleShift();
        return Math.max(ha, hb) * 2;
    }

    /**
     * Return the current height of the upper part
   * of this component from the baseline
     *
     * @return Height of the upper part
     */
    @Override
    public int getAscentHeight(final boolean dynamicParts)
    {
        if (getMathElement(0) == null)
            return 0;

        if (mode == DISPLAY)
            return getMathElement(0).getAscentHeight(true);
        return Math.max(getMathElement(0).getAscentHeight(true),
                                        getMathElement(0).getDescentHeight(true));
    }

    /**
     * Return the current height of the lower part
   * of this component from the baseline
     *
     * @return Height of the lower part
     */
    @Override
    public int getDescentHeight(final boolean dynamicParts)
    {
        if (getMathElement(0) == null)
            return 0;

        if (mode == DISPLAY)
            return getMathElement(0).getDescentHeight(true);
        return Math.max(getMathElement(0).getAscentHeight(true),
                                        getMathElement(0).getDescentHeight(true));
    }
}
