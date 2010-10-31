/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.io.File;

import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

/**
 * A class for displaying MathML content in a AWT Component
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @version %I%, %G%
 */
public class MathComponent extends JPanel
{
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MathComponent.class);

    private MathBase base;

    /**
     * Creates a MathComponent
     *
     * @param s the equation as a String
     */
    public MathComponent(final String s)
    {
        base = new MathBase((new StringMathBuilder(s)).getMathRootElement(), null);
        // we don't know the Graphics object yet, but gcalc will be setup when paintComponent() is called
    }

    /**
     * Creates a MathComponent
     *
     * @param document Root element of a MathML DOM tree
     * @param fontsize Name of the preferred font 
     */
    public MathComponent(final Document document, final int fontsize)
    {
        base = new MathBase((new DOMMathBuilder(document)).getMathRootElement(), "Default", Font.PLAIN, fontsize, fontsize, null);
    }

    /**
     * Creates a MathComponent
     *
     * @param document Root element of a MathML DOM tree
     */
    public MathComponent(final Document document)
    {
        base = new MathBase((new DOMMathBuilder(document)).getMathRootElement(), null);
    }

    /**
     * Changes the MathBase for the component
     *
     * @param base the MathBase
     */
    public void setMathBase(final MathBase base)
    {
        this.base = base;
        invalidate();
    }

    /**
     * Changes the string equation
     *
     * @param s the equation as a String
     */
    public void setEquationString(final String s)
    {
        base.setRootElement((new StringMathBuilder(s)).getMathRootElement());
    }

    /**
     * Paints this component 
     *
     * @param g The graphics context to use for painting 
     */
    @Override
    public void paintComponent(final Graphics g)
    {
        super.paintComponent(g); //paint background
        base.paint(g);
    }

    /**
     * Enables, or disables the debug mode 
     *
     * @param debug Debug mode  
     */
    public void setDebug(final boolean debug)
    {
        base.setDebug(debug);
    }

    /**
     * Gets the mininimum size of this component
     *
     * @return A dimension object indicating this component's minimum size
     */
    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(base.getWidth(), base.getHeight());
    }

    /**
     * Gets the preferred size of this component
     *
     * @return A dimension object indicating this component's preferred size
     */
    @Override
    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }

    /**
     * Main method
     *
     * @param args Command line arguments       
     */
    public static void main(final String[] args)
    {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(new File(args[0]));

            final Frame frame = new Frame("Test MathComponent");

            frame.setLayout(new BorderLayout());
            final MathComponent component = new MathComponent(document,    14);

            component.setDebug(false);
            frame.add(component, BorderLayout.CENTER);
            frame.setVisible(true);
            frame.pack();
            frame.invalidate();
            frame.validate();

            frame.addWindowListener(new java.awt.event.WindowAdapter()
            {
                @Override
                public void windowClosing(final java.awt.event.WindowEvent evt)
                {
                    System.exit(0);
                }
            });

        } catch (final org.xml.sax.SAXException e) {
            LOG.error("main(String[])", e);
        } catch (final java.io.IOException e) {
            LOG.error("main(String[])", e);
        } catch (final ParserConfigurationException e) {
            LOG.error("main(String[])", e);
        }
    }
}
