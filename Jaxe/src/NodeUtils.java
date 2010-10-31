/*
Jaxe - Editeur XML en Java

Copyright (C) Lexis Nexis Deutschland, 2005

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NodeUtils {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(NodeUtils.class);

    public static boolean isEqualNode(final Node n1, final Node n2) {
        if (n1 == n2) {
            return true;
        }
        if (n1.getNodeType() != n2.getNodeType()) {
            return false;
        }
        if (n2.getNodeName() == null) {
            if (n1.getNodeName() != null) {
                return false;
            }
        } else if (!n2.getNodeName().equals(n1.getNodeName())) {
            return false;
        }

        if (n2.getLocalName() == null) {
            if (n1.getLocalName() != null) {
                return false;
            }
        } else if (!n2.getLocalName().equals(n1.getLocalName())) {
            return false;
        }

        if (n2.getNamespaceURI() == null) {
            if (n1.getNamespaceURI() != null) {
                return false;
            }
        } else if (!n2.getNamespaceURI().equals(n1.getNamespaceURI())) {
            return false;
        }

        if (n2.getPrefix() == null) {
            if (n1.getPrefix() != null) {
                return false;
            }
        } else if (!n2.getPrefix().equals(n1.getPrefix())) {
            return false;
        }

        if (n2.getNodeValue() == null) {
            if (n1.getNodeValue() != null) {
                return false;
            }
        } else if (!n2.getNodeValue().equals(n1.getNodeValue())) {
            return false;
        }
        if (n1.getNodeType() == Node.ELEMENT_NODE) {
            final Element e1 = (Element) n1;
            final Element e2 = (Element) n2;
            final boolean hasAttrs = e2.hasAttributes();
            if (hasAttrs != (e1).hasAttributes()) {
                return false;
            }
            if (hasAttrs) {
                final NamedNodeMap map1 = e2.getAttributes();
                final NamedNodeMap map2 = (e1).getAttributes();
                final int len = map1.getLength();
                if (len != map2.getLength()) {
                    return false;
                }
                for (int i = 0; i < len; i++) {
                    final Node nc1 = map1.item(i);
                    if (nc1.getLocalName() == null) { // DOM Level 1 Node
                        final Node nc2 = map2.getNamedItem(nc1.getNodeName());
                        if (nc2 == null || !isEqualNode(nc1, nc2)) {
                            return false;
                        }
                    } else {
                        final Node nc2 = map2.getNamedItemNS(nc1.getNamespaceURI(), nc1.getLocalName());
                        if (nc2 == null || !isEqualNode(nc1, nc2)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
