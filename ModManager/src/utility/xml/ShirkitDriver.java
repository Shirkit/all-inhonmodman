/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 07. March 2004 by Joe Walnes
 */
package utility.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import java.util.Stack;
import org.w3c.dom.Element;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This driver is used by XStream to load the ShirkitReader. Through that Reader I've solved the problem of getting blank spaces of Strings, solving one of the hardest problems I had to face. Without that Reader, it's not possible the Manager do it's job.
 * @author Shirkit
 */
public class ShirkitDriver extends AbstractXmlDriver {

    private final String encoding;
    private final DocumentBuilderFactory documentBuilderFactory;

    /**
     * Construct a ShirkitDriver.
     */
    public ShirkitDriver() {
        this(null);
    }

    /**
     * Construct a ShirkitDriver with a specified encoding. The created DomReader will ignore any
     * encoding attribute of the XML header though.
     */
    public ShirkitDriver(String encoding) {
        this(encoding, new XmlFriendlyReplacer());
    }

    /**
     * @since 1.2
     */
    public ShirkitDriver(String encoding, XmlFriendlyReplacer replacer) {
        super(replacer);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.encoding = encoding;
    }

    public HierarchicalStreamReader createReader(Reader xml) {
        return createReader(new InputSource(xml));
    }

    public HierarchicalStreamReader createReader(InputStream xml) {
        return createReader(new InputSource(xml));
    }

    private HierarchicalStreamReader createReader(InputSource source) {
        try {
            //DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            if (encoding != null) {
                source.setEncoding(encoding);
            }
            //Document document = documentBuilder.parse(source);
            // Disabled, it isn't working for now, white spaces bug
            if (source.getByteStream() != null && false) {
                Document document = readXML(source.getByteStream(), "lineStart", "lineEnd");
                return new ShirkitReader(document, xmlFriendlyReplacer());
            } else {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(source);
                return new ShirkitReader(document, xmlFriendlyReplacer());
            }
        } catch (FactoryConfigurationError e) {
            throw new StreamException(e);
        } catch (ParserConfigurationException e) {
            throw new StreamException(e);
        } catch (SAXException e) {
            throw new StreamException(e);
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    public static Document readXML(InputStream is, final String lineStartNumAttribName, final String lineEndNumAttribName) throws IOException, SAXException {
        final Document doc;
        SAXParser parser;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = factory.newSAXParser();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
        }

        final Stack<Element> elementStack = new Stack<Element>();
        final StringBuilder textBuffer = new StringBuilder();
        DefaultHandler handler = new DefaultHandler2() {
            //DefaultHandler handler2 = new DefaultHandler() {

            private Locator locator;
            boolean insideCdata = false;

            @Override
            public void endCDATA() throws SAXException {
                super.endCDATA();
                addTextIfNeeded();
            }

            @Override
            public void startCDATA() throws SAXException {
                super.startCDATA();
                addTextIfNeeded();

            }

            @Override
            public void setDocumentLocator(Locator locator) {
                this.locator = locator; //Save the locator, so that it can be used later for <b style="color:black;background-color:#99ff99">line</b> tracking when traversing nodes.
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                addTextIfNeeded();
                Element el = doc.createElement(qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    el.setAttribute(attributes.getQName(i), attributes.getValue(i));

                }
                el.setAttribute(lineStartNumAttribName, String.valueOf(locator.getLineNumber()));
                elementStack.push(el);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                addTextIfNeeded();
                Element closedEl = elementStack.pop();
                closedEl.setAttribute(lineEndNumAttribName, String.valueOf(locator.getLineNumber()));
                if (elementStack.isEmpty()) { // Is this the root element?
                    doc.appendChild(closedEl);
                } else {
                    Element parentEl = elementStack.peek();
                    parentEl.appendChild(closedEl);
                }
            }

            @Override
            public void characters(char ch[], int start, int length) throws SAXException {
                textBuffer.append(ch, start, length);
            }

            // Outputs text accumulated under the current node
            private void addTextIfNeeded() {
                if (textBuffer.toString().trim().length() > 0) {
                    Element el = elementStack.peek();
                    Node textNode = doc.createTextNode(textBuffer.toString());
                    el.appendChild(textNode);
                }
                textBuffer.delete(0, textBuffer.length());
            }
        };
        parser.getXMLReader().setContentHandler(handler);
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        parser.parse(is, handler);

        return doc;
    }

    public HierarchicalStreamWriter createWriter(Writer out) {
        return new PrettyPrintWriter(out, xmlFriendlyReplacer());
    }

    public HierarchicalStreamWriter createWriter(OutputStream out) {
        try {
            return createWriter(encoding != null
                    ? new OutputStreamWriter(out, encoding)
                    : new OutputStreamWriter(out));
        } catch (UnsupportedEncodingException e) {
            throw new StreamException(e);
        }
    }
}
