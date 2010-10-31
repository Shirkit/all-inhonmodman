/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.util.Vector;

import jaxe.equations.MathBase;

/**
 * The basic class the the math elements. From this class
 * elements inherits.
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version 1.5      
 */
public class MathElement
{

    /** The URI from MathML */
    public final static String URI = "http://www.w3.org/1998/Math/MathML";

    /** The URI from SVG */
    public final static String SVG_URI = "http://www.w3.org/2000/svg";

    /** The URI from FO */
    public final static String FO_URI = "http://www.w3.org/1999/XSL/Format";

    private MathBase base;
    private MathElement parent;
    private int fontsize = 14;
    private final Vector<MathElement> children = new Vector<MathElement>();

    private final StringBuilder text = new StringBuilder();

    /**
     * Creates a math element
     *
     * @param base The base for the math element tree
     * @param fontsize The font size for this element
     */
    public MathElement(final MathBase base, final int fontsize)
    {
        setMathBase(base);
        setParent(parent);
        setFontSize(fontsize);
    }

    /**
     * Creates a math element
     *
     * @param base The base for the math element tree
     */
    public MathElement(final MathBase base)
    {
        setMathBase(base);
    }

  /**
   * Creates a math element
   */
  public MathElement()
  {
  }

    /**
     * Add a math element as a child
     *
     * @param child Math element
     */
    public void addMathElement(final MathElement child)
    {
        if (child != null)
        {
            children.addElement(child);
            child.setMathBase(base);
            child.setParent(this);
            child.setFontSize(fontsize);
        }
    }

    /**
     * Gets a child from this element
     *
     * @param index Index of the child
     *
     * @return The child
     */
    public MathElement getMathElement(final int index)
    {
        if ((index>=0) && (index<children.size()))
            return children.elementAt(index);
        return null;
    }

    /**
     * Sets a child from this element
     *
     * @param index Index of the child
     *
     * @return The child
     */
    public void setMathElementAt(final MathElement child, final int index)
    {
        if ((index>=0) && (index<children.size()))
            children.setElementAt(child, index);
    }

    /**
     * Returns the count of children from this element
     *
     * @return Count of children
     */
    public int getMathElementCount()
    {
        return children.size();
    }

    /**
     * Add the content of a String to this element
     *
     * @param text Text
     */
    public void addText(final String text)
    {
        //String textdummy = text.trim();

        for (int i = 0; i < text.length(); i++)
            if (" \t\n\r".indexOf(text.charAt(i)) < 0)
                this.text.append(text.charAt(i));
            else if ((' ' == text.charAt(i)) && (i>0) && (' ' != text.charAt(i - 1)))
                this.text.append(text.charAt(i));
    }

    /**
     * Returns the text contentof this element
     *
     * @return Text content
     */
    public String getText()
    {
        return text.toString().trim();
    }

    /**
     * Sets the base for this element
     *
     * @param base Math base
     */
    public void setMathBase(final MathBase base)
    {
        this.base = base;
        for (final MathElement e : children)
          e.setMathBase(base);
    }

    /**
     * Gets the math base
     *
     * @return Math base
     */
    public MathBase getMathBase()
    {
        return base;
    }

    /**
     * Sets the parent of this element
     *
     * @param parent Parent element
     */
    public void setParent(final MathElement parent)
    {
        this.parent = parent;
    }

    /**
     * Returns get parent of this element
     *
     * @return Parent element
     */
    public MathElement getParent()
    {
        return parent;
    }

    /**
     * Sets the font size for this component
     *
     * @param fontsize Font size
     */
    public void setFontSize(final int fontsize)
    {
        this.fontsize = Math.max(fontsize, 8);
        for (final MathElement e : children)
            e.setFontSize(this.fontsize);
    }

    /**
     * Gets the used font size
     *
     * @return Font Size
     */
    public int getFontSize()
    {
        return fontsize;
    }

    /**
     * Gets the used font
     *
     * @return Font
     */
    public Font getFont()
    {
        if (base!=null)
              return base.getFont(fontsize);
        return null;
    }

    /**
     * Gets the font metrics of the used font
     *
     * @return Font metrics
     */
    public FontMetrics getFontMetrics()
    {
        if (base!=null)
              return base.getFontMetrics(fontsize);
        return null;
    }

    /**
     * Gets the used symbol font size
     *
     * @return Font
     */
    public Font getSymbolFont()
    {
        if (base!=null)
              return base.getSymbolFont(fontsize);
        return null;
    }

    /**
     * Gets the font metrics of the used symbol font
     *
     * @return Font metrics
     */
    public FontMetrics getSymbolFontMetrics()
    {
        if (base!=null)
              return base.getSymbolFontMetrics(fontsize);
        return null;
    }

    /**
     * Get a glyph vector of the symbol font
     *
     * @param g2d The graphic context presented by a Graphics2D
     * @param index Index of the glyph vector
     *
     * @return Glyph vector
     */
    public GlyphVector getSymbolGlyphVector(final Graphics2D g2d, final char c)
    {
        if (base!=null)
              return base.getSymbolGlyphVector(g2d, fontsize, c);
        return null;
    }

    /**
     * Paints a border around this element as debug information
     *
     * @param g The graphics context to use for painting
     * @param posX The first left position for painting
     * @param posY The position of the baseline
     */
    public void debug(final Graphics g, final int posX, final int posY)
    {
        g.setColor(Color.blue);
        g.drawLine(posX, posY - getAscentHeight(true), posX + getWidth(true),
                             posY - getAscentHeight(true));
        g.drawLine(posX + getWidth(true), posY - getAscentHeight(true),
                             posX + getWidth(true), posY + getDescentHeight(true));
        g.drawLine(posX, posY + getDescentHeight(true), posX + getWidth(true),
                             posY + getDescentHeight(true));
        g.drawLine(posX, posY - getAscentHeight(true), posX,
                             posY + getDescentHeight(true));
        g.setColor(Color.red);
        g.drawLine(posX, posY, posX + getWidth(true), posY);
        g.setColor(Color.black);
    }

    /**
     * Paints this element
     *
     * @param g The graphics context to use for painting
     * @param posX The first left position for painting
     * @param posY The position of the baseline
     */
    public void paint(final Graphics g, final int posX, final int posY)
    {
        if (base.isDebug())
            debug(g, posX, posY);

        int pos = posX;
        MathElement child;

        for (int i = 0; i < getMathElementCount(); i++)
        {
            child = getMathElement(i);
            child.paint(g, pos, posY);
            pos += child.getWidth(true) + 1;
        }
    }

    // public abstarct void toFO(ContentHandler handler) throws SAXException;

    /*public void toFO(ContentHandler handler) throws SAXException
    {
                    AttributesImpl attributes = new AttributesImpl();
                    handler.startElement(FO_URI, "block", "fo:block", attributes);
                    for (Enumeration e = children.elements() ; e.hasMoreElements() ;)
                                    ((MathRow)e.nextElement()).toSAX(handler);
                    handler.endElement(FO_URI, "block", "fo:block");
    }*/

    // public abstract void toSVG(ContentHandler handler) throws SAXException;

    /**
     * Return the current width of this element
     *
     * @param dynamicParts
     *
     * @return Width of this element
     */
    public int getWidth(final boolean dynamicParts)
    {
        int width = 0;
        
        for (final MathElement e : children)
            width += e.getWidth(dynamicParts) + 1;
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
    public int getHeight(final boolean dynamicParts)
    {
        int height = 0;

        for (final MathElement element : children)
            height = Math.max(height, element.getHeight(dynamicParts));
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
    public int getAscentHeight(final boolean dynamicParts)
    {
        int height = 0;

        for (final MathElement element : children)
            height = Math.max(height, element.getAscentHeight(dynamicParts));
        
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
    public int getDescentHeight(final boolean dynamicParts)
    {
        int height = 0;

        for (final MathElement element : children)
            height = Math.max(height, element.getDescentHeight(dynamicParts));
        
        return height;
    }

    /**
     * Returns the distance of the baseline and the middleline
     *
     * @return Distance
     */
    protected int getMiddleShift()
    {
        return (int) (base.getFontMetrics(getFontSize()).getAscent() * 0.38);
    }
}
