/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package jaxe ;

import org.apache.log4j.Logger;

import java.util.HashMap;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Displays a DOM document in a tree control.
 *
 * @author  Andy Clark, IBM
 * @version
 */
public class DOMTree extends JTree {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DOMTree.class);

    //
    // Constructors
    //

    /** Default constructor. */
    public DOMTree() {
        this(null);
    }

    /** Constructs a tree with the specified document. */
    public DOMTree(final Document document) {
        super(new Model());

        // set tree properties
        setRootVisible(false);

        // set properties
        setDocument(document);

    } // <init>()

    //
    // Public methods
    //

    /** Sets the document. */
    public void setDocument(final Document document) {
        ((Model)getModel()).setDocument(document);
        expandRow(0);
    }

    /** Returns the document. */
    public Document getDocument() {
        return ((Model)getModel()).getDocument();
    }

    /** get the org.w3c.Node for a MutableTreeNode. */
    public Node getNode(final Object treeNode) {
        return ((Model)getModel()).getNode(treeNode);
    }
    
    /** get the MutableTreeNode for a org.w3c.Node. */
     public DefaultMutableTreeNode getMutNode(final Node node) {
        return ((Model)getModel()).getMutNode(node);
    }

    //
    // Classes
    //

    /**
     * DOM tree model.
     *
     * @author  Andy Clark, IBM
     * @version
     */
    static class Model extends DefaultTreeModel {

        //
        // Data
        //

        /** Document. */
        private Document document;
        /** Node Map. */
        private final HashMap<MutableTreeNode, Node> nodeMap = new HashMap<MutableTreeNode, Node>();
        private final HashMap<Node, MutableTreeNode> nodeMapInv = new HashMap<Node, MutableTreeNode>();
        

        //
        // Constructors
        //

        /** Default constructor. */
        public Model() {
            this(null);
        }

        /** Constructs a model from the specified document. */
        public Model(final Document document) {
            super(new DefaultMutableTreeNode());
            setDocument(document);
        }

        //
        // Public methods
        //

        /** Sets the document. */
        public synchronized void setDocument(final Document document) {

            // save document
            this.document = document;

            // clear tree and re-populate
            ((DefaultMutableTreeNode)getRoot()).removeAllChildren();
            nodeMap.clear();
            nodeMapInv.clear();
            buildTree();
            fireTreeStructureChanged(this, new Object[] { getRoot() }, new int[0], new Object[0]);

        } // setDocument(Document)

        /** Returns the document. */
        public Document getDocument() {
            return document;
        }

        /** get the org.w3c.Node for a MutableTreeNode. */
        public Node getNode(final Object treeNode) {
            return nodeMap.get(treeNode);
        }
        
        /** get the MutableTreeNode for a org.w3c.Node. */
        public DefaultMutableTreeNode getMutNode(final Node node) {
            return (DefaultMutableTreeNode)nodeMapInv.get(node);
        }

        //
        // Private methods
        //

        /** Builds the tree. */
        private void buildTree() {
            
            // is there anything to do?
            if (document == null) { return; }

            // iterate over children of this node
            final NodeList nodes = document.getChildNodes();
            final int len = (nodes != null) ? nodes.getLength() : 0;
            MutableTreeNode root = (MutableTreeNode)getRoot();
            for (int i = 0; i < len; i++) {
                final Node node = nodes.item(i);
                switch (node.getNodeType()) {
                    case Node.DOCUMENT_NODE: {
                        root = setDocumentNode(node);
                        break;
                    }

                    case Node.ELEMENT_NODE: {
                        insertElementNode(node, root);
                        break;
                    }

                    default: // ignore

                } // switch

            } // for 

        } // buildTree()

        /** Inserts a node and returns a reference to the new node. */
        private MutableTreeNode insertNode(final String what, final MutableTreeNode where) {

            final MutableTreeNode node = new DefaultMutableTreeNode(what);
            insertNodeInto(node, where, where.getChildCount());
            return node;

        } // insertNode(Node,MutableTreeNode):MutableTreeNode
            
        /** Inserts the document node. */
        private MutableTreeNode setDocumentNode(final Node what) {
            final MutableTreeNode treeNode = new DefaultMutableTreeNode("<"+what.getNodeName()+'>');
            setRoot(treeNode) ;
            //MutableTreeNode treeNode = setRoot("<"+what.getNodeName()+'>');
            nodeMap.put(treeNode, what);
            nodeMapInv.put(what,treeNode);
            return treeNode;
        }

        /** Inserts an element node. */
        private MutableTreeNode insertElementNode(final Node what, final MutableTreeNode where) {

            // build up name
            final StringBuilder name = new StringBuilder();
            name.append('<');
            final String nomNoeud = what.getNodeName() ;
            if (nomNoeud.equals("html")) {
                name.append(' ');
            }
            name.append(nomNoeud);
            final NamedNodeMap attrs = what.getAttributes();
            final int attrCount = (attrs != null) ? attrs.getLength() : 0;
            for (int i = 0; i < attrCount; i++) {
                final Node attr = attrs.item(i);
                name.append(' ');
                name.append(attr.getNodeName());
                name.append("=\"");
                name.append(attr.getNodeValue());
                name.append('"');
            }
            name.append('>');

            // insert element node
            
            final MutableTreeNode element = insertNode(name.toString(), where);
            nodeMap.put(element, what);
            nodeMapInv.put(what,element);
            
            // gather up attributes and children nodes
            final NodeList children = what.getChildNodes();
            final int len = (children != null) ? children.getLength() : 0;
            for (int i = 0; i < len; i++) {
                final Node node = children.item(i);
                switch (node.getNodeType()) {
                    case Node.CDATA_SECTION_NODE: { 
                       insertCDataSectionNode( node, element ); //Add a Section Node
                       break;
                      }
                    case Node.TEXT_NODE: {
                        insertTextNode(node, element);
                        break;
                    }
                    case Node.ELEMENT_NODE: {
                        insertElementNode(node, element);
                        break;
                    }
                }
            }

            return element;
        } // insertElementNode(Node,MutableTreeNode):MutableTreeNode

        /** Inserts a text node. */
        private MutableTreeNode insertTextNode(final Node what, final MutableTreeNode where) {
            final String value = what.getNodeValue().trim();
            if (value.length() > 0) {
                final MutableTreeNode treeNode = insertNode(value, where);
                nodeMap.put(treeNode, what);
                nodeMapInv.put(what,treeNode);           
                return treeNode;
                }
            return null;
            }

        
      /** Inserts a CData Section Node. */
      private MutableTreeNode insertCDataSectionNode(final Node what, final MutableTreeNode where) {
         final StringBuilder CSectionBfr = new StringBuilder();         
         //--- optional --- CSectionBfr.append( "<![CDATA[" );
         CSectionBfr.append( what.getNodeValue() );
         //--- optional --- CSectionBfr.append( "]]>" );
         if (CSectionBfr.length() > 0) {
            final MutableTreeNode treeNode = insertNode(CSectionBfr.toString(), where);
            nodeMap.put(treeNode, what); 
            nodeMapInv.put(what,treeNode);           
            return treeNode;
        }
         return null;
        }
    } // class Model
} // class DOMTree
