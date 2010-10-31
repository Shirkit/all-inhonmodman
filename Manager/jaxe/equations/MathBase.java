/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations;

import org.apache.log4j.Logger;

//import org.w3c.dom.*;
//import org.apache.xerces.parsers.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.util.Vector;

import jaxe.equations.element.MathRootElement;

/**
 * The base for creating a MathElement tree
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathBase
{
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MathBase.class);

    private String fontname = "Default";
    private int fontstyle = Font.PLAIN;

    private int inlinefontsize = 12;
    private int displayfontsize = 14;

    private final int minfontsize = 8;
    private final int maxfontsize = 60;

    private final Font[] fonts = new Font[maxfontsize];

    private FontMetrics[] fontmetrics = null;

    private final Font[] symbolFonts = new Font[maxfontsize];

    private FontMetrics[] symbolFontmetrics = null;

    private boolean debug = false;

    /** Inline mathematical expression */
    public final static int INLINE = 0;

    /** Non inline mathematical expression */
    public final static int DISPLAY = 1;

    private final int mode = INLINE;

    private MathRootElement rootElement;
    
    // mega-bug: Windows does not include any Unicode equivalence for its Symbol font: we have to do it by hand...
    public boolean windaube;
    private final char[] Symbol_chars = {'\u239B', '\u239C', '\u239D',
                    '\u239E', '\u239F', '\u23A0',
                    '\u23A1', '\u23A2', '\u23A3',
                    '\u23A4', '\u23A5', '\u23A6',
                    '\u23A7', '\u23A8', '\u23AA', '\u23A9',
                    '\u23AB', '\u23AC', '\u23AD',
                    '\u2320', '\u2321',
                    '\u2211', '\u220F'};
    
    private final int[] MS_Symbol_codes = {167, 168, 169,
                    183, 184, 185,
                    170, 171, 172,
                    186, 187, 188,
                    173, 174, 176, 175,
                    189, 190, 191,
                    180, 182,
                    166, 150};
    
    private final Vector<Font> goodFonts; // cache for findfont
    
    /**
     * Creates a MathBase
     *
     * @param element Root element of a math tree
     * @param fontname Name of the preferred font
     * @param fontstyle Style of the preferred font, see java.awt.Font
     * @param inlinefontsize Size of the preferred font used by inline equations
     * @param displayfontsize Size of the preferred font used by non inline equations
     * @param gcalc Graphics object to use to calculate character sizes (nothing will be painted on it)
     */
    public MathBase(final MathRootElement element, final String fontname, final int fontstyle,
                                    final int inlinefontsize, final int displayfontsize, final Graphics gcalc)
    {
        this(fontname, fontstyle, inlinefontsize, displayfontsize, gcalc);
        setRootElement(element);
    }

    /**
     * Creates a MathBase
     *
     * @param element Root element of a math tree
     * @param gcalc Graphics object to use to calculate character sizes (nothing will be painted on it)
     */
    public MathBase(final MathRootElement element, final Graphics gcalc)
    {
        this(element, "Default", Font.PLAIN, 12, 14, gcalc);
    }

    /**
     * Creates a MathBase
     *
     * @param fontname Name of the preferred font
     * @param fontstyle Style of the preferred font, see java.awt.Font
     * @param inlinefontsize Size of the preferred font used by inline equations
     * @param displayfontsize Size of the preferred font used by non inline equations
     * @param gcalc Graphics object to use to calculate character sizes (nothing will be painted on it)
     */
    public MathBase(final String fontname, final int fontstyle, final int inlinefontsize,
                                    final int displayfontsize, final Graphics gcalc)
    {
        this.fontname = fontname;
        this.fontstyle = fontstyle;
        this.inlinefontsize = inlinefontsize;
        this.displayfontsize = displayfontsize;
        
        windaube = System.getProperty("os.name").startsWith("Windows");
        
        for (int i = 0; i < maxfontsize; i++)
            fonts[i] = new Font(fontname, fontstyle, i);
        
        goodFonts = new Vector<Font>();
        String symbolFontName = "Symbol";
        if (!windaube) {
            // check if Symbol has all the glyphs we use
            Font testfont = new Font("Symbol", fontstyle, displayfontsize);
            boolean testok = true;
            for (final char element : Symbol_chars)
                if (!testfont.canDisplay(element)) {
                    testok = false;
                    break;
                }
            if (!testok) {
                LOG.error("MathBase(String, int, int, int) - Warning: Symbol does not have all the necessary glyphs");
                final Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
                Font perfectfont = null;
                Font bestfont = null;
                int bestscore = 1000;
                int score;
                for (int i=0; i<allfonts.length && perfectfont == null; i++) {
                    testfont = allfonts[i];
                    score = 0;
                    for (final char element : Symbol_chars)
                        if (!testfont.canDisplay(element))
                            score++;
                    if (score == 0)
                        perfectfont = testfont;
                    if (score < bestscore) {
                        bestfont = testfont;
                        bestscore = score;
                    }
                }
                if (perfectfont == null) {
                    LOG
                            .error(
                                    "MathBase(String, int, int, int) - Error: no font on this system has all the necessary glyphs");
                    symbolFontName = bestfont.getName();
                } else
                    symbolFontName = perfectfont.getName();
                LOG.error("MathBase(String, int, int, int) - Using a different font: " + symbolFontName);
            }
        }
        goodFonts.add(new Font(symbolFontName, Font.PLAIN, displayfontsize));
        
        for (int i = 0; i < maxfontsize; i++)
            symbolFonts[i] = new Font(symbolFontName, fontstyle, i);
        
        if (gcalc != null)
            setupFontMetrics(gcalc);

        /*System.out.println("fontname="+fonts[10].getFontName()+" name="+fonts[10].getName()+
                                                                                                                                                                                                                                                                                                                                        " family="+fonts[10].getFamily());

        String[] names = Toolkit.getDefaultToolkit().getFontList();//getAvailableFontFamilyNames()
        System.out.print("Available fonts: ");
        for(int i=0; i<names.length; i++)
                                                                        System.out.print(names[i]+" ");
        System.out.println();*/

        /*Font[] fontlist= GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        System.out.println("Available fonts: ");
        for(int i=0; i<fontlist.length; i++)
                                                                        System.out.println("fontname="+fontlist[i].getFontName()+" name="+fontlist[i].getName()+
                                                                                                                                                                                                                                                                                                                                        " family="+fontlist[i].getFamily());
        System.out.println();*/
    }
    
    private void setupFontMetrics(final Graphics gcalc) {
        fontmetrics = new FontMetrics[maxfontsize];
        for (int i = 0; i < maxfontsize; i++)
            fontmetrics[i] = gcalc.getFontMetrics(fonts[i]);
        
        symbolFontmetrics = new FontMetrics[maxfontsize];
        for (int i = 0; i < maxfontsize; i++)
            symbolFontmetrics[i] = gcalc.getFontMetrics(symbolFonts[i]);
    }
    
    /**
     * Set the root element of a math tree
     *
     * @param element Root element of a math tree
     */
    public void setRootElement(final MathRootElement element)
    {
        if (element == null)
            return;
        
        rootElement = element;
        
        rootElement.setMathBase(this);
        
        if (element.getMode() == MathRootElement.DISPLAY)
            rootElement.setFontSize(displayfontsize);
        else
            rootElement.setFontSize(inlinefontsize);
        
        rootElement.setDebug(isDebug());
    }

    /**
     * Enables, or disables the debug mode
     *
     * @param debug Debug mode
     */
    public void setDebug(final boolean debug)
    {
        this.debug = debug;
    if (rootElement!=null)
      rootElement.setDebug(debug);
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
     * Sets the default font size, which used for the root element
     *
     * @param fontsize Font size
     */
    public void setDefaultFontSize(final int fontsize)
    {
        if (fontsize >= minfontsize || fontsize < maxfontsize)
            this.inlinefontsize = fontsize;
    }

    /**
     * Get the default font size
     *
     * @return Default font size
     */
    public int getDefaultInlineFontSize()
    {
        return inlinefontsize;
    }

    /**
     * Sets the default font size for non inline equations
     *
     * @param fontsize Default font size
     */
    public void setDefaultDisplayFontSize(final int fontsize)
    {
        if (fontsize >= minfontsize || fontsize < maxfontsize)
            this.displayfontsize = fontsize;
    }

    /**
     * Get the default font size for non inline equations
     *
     * @return Default display font size
     */
    public int getDefaultDisplayFontSize()
    {
        return displayfontsize;
    }

    /**
     * Get a font specified by the font size
     *
     * @param fontsize Font size
     *
     * @return Font
     */
    public Font getFont(final int fontsize)
    {
        if (fontsize < minfontsize)
            return fonts[minfontsize];
        if (fontsize > maxfontsize)
            return fonts[maxfontsize - 1];
        return fonts[fontsize];
    }

    /**
     * Get a symbol font specified by the font size
     *
     * @param fontsize Font Size
     *
     * @return Font
     */
    public Font getSymbolFont(final int fontsize)
    {
        if (fontsize < minfontsize)
            return symbolFonts[minfontsize];
        if (fontsize > maxfontsize)
            return symbolFonts[maxfontsize - 1];
        return symbolFonts[fontsize];
    }
    
    public String findFont(final String s, final Font defaultFont) {
        if (goodFonts != null) {
            for (final Font f : goodFonts) {
                final int upto = f.canDisplayUpTo(s);
                if (upto == -1 || upto == s.length())
                    return f.getName();
            }
        }
        final Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (final Font f : allfonts) {
            final int upto = f.canDisplayUpTo(s);
            if (upto == -1 || upto == s.length()) {
                goodFonts.add(f);
                return f.getName();
            }
        }
        return defaultFont.getName();
    }
    
    /**
     * Get the font metrics specified by the font size
     *
     * @param fontsize Font size
     *
     * @return Font metrics
     */
    public FontMetrics getFontMetrics(final int fontsize)
    {
        if (fontsize < minfontsize)
            return fontmetrics[minfontsize];
        if (fontsize > maxfontsize)
            return fontmetrics[maxfontsize - 1];
        return fontmetrics[fontsize];
    }

    /**
     * Get the font metrics of the symbol font specified by the font size
     *
     * @param fontsize Font size
     *
     * @return Font metrics
     */
    public FontMetrics getSymbolFontMetrics(final int fontsize)
    {
        if (fontsize < minfontsize)
            return symbolFontmetrics[minfontsize];
        if (fontsize > maxfontsize)
            return symbolFontmetrics[maxfontsize - 1];
        return symbolFontmetrics[fontsize];
    }

    /**
     * Get a glyph vector of the symbol font
     *
     * @param g2d The graphic context presented by a Graphics2D
     * @param fontsize Font size
     * @param index Index of the glyph vector
     *
     * @return Glyph vector
     */
    public GlyphVector getSymbolGlyphVector(final Graphics2D g2d, int fontsize, final char c)
    {
        if (windaube) {
            int code = 0;
            for (int i=0; i<Symbol_chars.length; i++)
                if (Symbol_chars[i] == c) {
                    code = MS_Symbol_codes[i];
                    break;
                }
            if (code == 0) {
                if (fontsize < minfontsize)
                    return symbolFonts[minfontsize].createGlyphVector(g2d.getFontRenderContext(),
                                    new char[]{ c });
                if (fontsize > maxfontsize)
                    return symbolFonts[maxfontsize - 1].createGlyphVector(g2d.getFontRenderContext(),
                                    new char[]{ c });
                return symbolFonts[fontsize].createGlyphVector(g2d.getFontRenderContext(),
                                new char[]{ c });
            }
            if (fontsize < minfontsize)
                return symbolFonts[minfontsize].createGlyphVector(g2d.getFontRenderContext(),
                                new int[]{ code });
            if (fontsize > maxfontsize)
                return symbolFonts[maxfontsize - 1].createGlyphVector(g2d.getFontRenderContext(),
                                new int[]{ code });
            return symbolFonts[fontsize].createGlyphVector(g2d.getFontRenderContext(),
                            new int[]{ code });
        }
        if (fontsize < minfontsize)
            fontsize = minfontsize;
        else if (fontsize > maxfontsize)
            fontsize = maxfontsize - 1;
        Font font = symbolFonts[fontsize];
        if (!font.canDisplay(c)) {
            final String fontname = findFont(Character.toString(c), font);
            if (!fontname.equals(font.getName()))
                font = new Font(fontname, Font.PLAIN, fontsize);
        }
        return font.createGlyphVector(g2d.getFontRenderContext(), new char[]{ c });
    }

    /**
     * Paints this component and all of its elements
     *
     * @param g The graphics context to use for painting
     */
    public void paint(final Graphics g) {
        if (fontmetrics == null)
            setupFontMetrics(g);
        
        final Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // looks much better !
        // note: RenderingHints on MacOS X with Java 1.3.1 works only with hardware acceleration off !
        // com.apple.hwaccel = false
        
        if (rootElement != null)
            rootElement.paint(g);
    }

    /**
     * Return the current width of this component
     *
     * @return Width
     */
    public int getWidth()
    {
        if (rootElement != null)
          return rootElement.getWidth();
        return 0;
    }

    /**
     * Return the current height of this component
     *
     * @return Height
     */
    public int getHeight()
    {
        if (rootElement != null)
            return rootElement.getHeight();
        return 0;
    }
}
