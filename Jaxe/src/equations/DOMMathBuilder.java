/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations;

import org.apache.log4j.Logger;

import jaxe.equations.element.MathElement;
import jaxe.equations.element.MathFrac;
import jaxe.equations.element.MathIdentifier;
import jaxe.equations.element.MathNumber;
import jaxe.equations.element.MathOperator;
import jaxe.equations.element.MathOver;
import jaxe.equations.element.MathPhantom;
import jaxe.equations.element.MathRoot;
import jaxe.equations.element.MathRootElement;
import jaxe.equations.element.MathRow;
import jaxe.equations.element.MathSqrt;
import jaxe.equations.element.MathSub;
import jaxe.equations.element.MathSubSup;
import jaxe.equations.element.MathSup;
import jaxe.equations.element.MathTable;
import jaxe.equations.element.MathTableData;
import jaxe.equations.element.MathTableRow;
import jaxe.equations.element.MathText;
import jaxe.equations.element.MathUnder;
import jaxe.equations.element.MathUnderOver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The builder for creating a MathElement tree
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @version %I%, %G%
 */
public class DOMMathBuilder
{
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DOMMathBuilder.class);

    private final MathRootElement rootElement;

    /**
     * Constructs a builder
     *
     * @param document The MathML document
     */
    public DOMMathBuilder(final Document document)
    {
        final Element documentElement = document.getDocumentElement();

        rootElement = new MathRootElement();

        final NamedNodeMap attributes = documentElement.getAttributes();

        rootElement.setMode(getAttribute(attributes, 
                        MathRootElement.ATTRIBUTE_MODE, "DISPLAY").equals("DISPLAY")
                                                ? MathRootElement.DISPLAY : MathRootElement.INLINE);

        traverse(documentElement, rootElement);
    }

    /**
     * Return the root  element of a math tree
     *
     * @return Root element
     */
    public MathRootElement getMathRootElement()
    {
        return rootElement;
    }

    /**
     * Help method to get the value of a attribute
     *
     * @param attributes Attribute list
     * @param attribute Name of the attribute
     * @param alt Alternative value
     *
     * @return String value
     */
    private String getAttribute(final NamedNodeMap attributes, final String attribute,
                                                            final String alt)
    {
        final Node node = attributes.getNamedItem(attribute);

        if (node != null)
            return node.getNodeValue();
        return alt;
    }

    /**
     * Help method to get the integer value of a attribute
     *
     * @param attributes Attribute list
     * @param attribute Name of the attribute
     * @param alt Alternative value
     *
     * @return Integer value
     */
    private int getInteger(final NamedNodeMap attributes, final String attribute,
                                                 final String alt)
    {
        final Node node = attributes.getNamedItem(attribute);

        if (node != null)
            return (new Integer(node.getNodeValue())).intValue();
        return (new Integer(alt)).intValue();
    }

    /**
     * Help method to get boolean value of a attribute
     *
     * @param attributes Attribute list
     * @param attribute Name of the attribute
     * @param alt Alternative value
     *
     * @return Integer value
     */
    private boolean getBoolean(final NamedNodeMap attributes, final String attribute,
                                                         final String alt)
    {
        final Node node = attributes.getNamedItem(attribute);

        if (node != null)
        {
            return "true".equals(node.getNodeValue());
        }
        return "true".equals(alt);
    }

    /**
     * Creates a MathElement through traversing the DOM tree
     *
     * @param node Current element of the DOM tree
     * @param parent Current element of the MathElement tree
     */
    private void traverse(final Node node, final MathElement parent)
    {
        if (node.getNodeType() != Node.ELEMENT_NODE)
            return;

        String tagname = node.getNodeName();
        int pos_seperator = -1;

        if ((pos_seperator = tagname.indexOf(":")) >= 0)
        {
            tagname = tagname.substring(pos_seperator + 1);
            // System.out.println("new tagname="+tagname);
        }
        MathElement element;
        final NamedNodeMap attributes = node.getAttributes();

        if (tagname.equals(MathFrac.ELEMENT))
        {
            element = new MathFrac();
            ((MathFrac) element).setLineThickness(getInteger(attributes,
                            "linethickness", "1"));
        }
        else if (tagname.equals(MathSup.ELEMENT))
            element = new MathSup();
        else if (tagname.equals(MathSub.ELEMENT))
            element = new MathSub();
        else if (tagname.equals(MathSubSup.ELEMENT))
            element = new MathSubSup();
        else if (tagname.equals(MathUnder.ELEMENT))
            element = new MathUnder();
        else if (tagname.equals(MathOver.ELEMENT))
            element = new MathOver();
        else if (tagname.equals(MathUnderOver.ELEMENT))
            element = new MathUnderOver();
        else if (tagname.equals(MathSqrt.ELEMENT))
            element = new MathSqrt();
        else if (tagname.equals(MathRoot.ELEMENT))
            element = new MathRoot();
        else if (tagname.equals(MathTable.ELEMENT))
            element = new MathTable();
        else if (tagname.equals(MathTableRow.ELEMENT))
            element = new MathTableRow();
        else if (tagname.equals(MathTableData.ELEMENT))
            element = new MathElement();
    else if (tagname.equals(MathPhantom.ELEMENT))
      element = new MathPhantom();
        else if (tagname.equals(MathOperator.ELEMENT))
        {
            element = new MathOperator();
            ((MathOperator) element).setStretchy(getBoolean(attributes,
                            MathOperator.ATTRIBUTE_STRETCHY, "true"));
        }
        else if (tagname.equals(MathIdentifier.ELEMENT))
            element = new MathIdentifier();
        else if (tagname.equals(MathNumber.ELEMENT))
            element = new MathNumber();
        else if (tagname.equals(MathText.ELEMENT))
            element = new MathText();
        else
            element = new MathRow();

        parent.addMathElement(element);

        final NodeList childs = node.getChildNodes();

        for (int i = 0; i < childs.getLength(); i++)
        {
            if (childs.item(i).getNodeType() == Node.ELEMENT_NODE)
                traverse(childs.item(i), element);
            else if (childs.item(i).getNodeType() == Node.TEXT_NODE)
                element.addText(childs.item(i).getNodeValue());
        }
    }
}
