/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations.element;

import java.awt.Font;
import java.awt.Graphics;

/**
 * This class presents a mathematical idenifier, like "x"
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathIdentifier extends MathText
{
  /** The XML element from this class */
  public final static String ELEMENT = "mi";

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
        final Font font = getMathBase().getFont(getFontSize());
        final String s = getText();
        final int upto = font.canDisplayUpTo(s);
        String fontname = font.getName();
        if (upto != -1 && upto != s.length())
            fontname = getMathBase().findFont(s, font);
        g.setFont(new Font(fontname, Font.ITALIC, getFontSize()));
        //g.setFont(getFont());
        g.drawString(s, posX, posY);
    }
    
    @Override
    public int getWidth(final boolean dynamicParts)
    {
        return super.getWidth(dynamicParts) + 1;
    }
}
