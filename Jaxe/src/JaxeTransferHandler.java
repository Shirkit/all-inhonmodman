/*
 Jaxe - Editeur XML en Java

 Copyright (C) 2008 Observatoire de Paris

 Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

 Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

 Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
 */

package jaxe;

import java.awt.Image;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.im.InputContext;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.TextAction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;

import jaxe.elements.JECData;
import jaxe.elements.JECommentaire;
import jaxe.elements.JETexte;
import jaxe.elements.JEFichier;
import jaxe.elements.JESwing;


/**
 * TransferHandler qui gère les strings, les fragments XML et les images d'un JaxeTextPane.
 * Utilisé pour toutes les opérations sur le presse-papier de JaxeTextPane,
 * et pour les glisser-déposer.
 */
public class JaxeTransferHandler extends TransferHandler {
    
    private static final Logger LOG = Logger.getLogger(JaxeTransferHandler.class);
    
    private boolean shouldRemove;
    private Position p0, p1;
    private JaxeTextPane exportComp;
    //private static final JTPClipOwner clipOwner = new JTPClipOwner();
    private boolean editionSpeciale;
    
    
    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        if (!(comp instanceof JaxeTextPane))
            return(false);
        final JTextComponent c = (JTextComponent)comp;
        if (!(c.isEditable() && c.isEnabled()))
            return false;
        return(choisirUneSaveur(transferFlavors, ((JaxeTextPane)comp).doc.fsave != null) != null);
    }
    
    @Override
    protected Transferable createTransferable(JComponent comp) {
        if (!(comp instanceof JaxeTextPane))
            return(null);
        final JaxeTextPane tp = (JaxeTextPane)comp;
        exportComp = tp;
        shouldRemove = true;
        editionSpeciale = false;
        final JaxeDocument doc = (JaxeDocument)tp.getDocument();
        try {
            final int debut = tp.getSelectionStart();
            final int fin = tp.getSelectionEnd();
            p0 = doc.createPosition(debut);
            p1 = doc.createPosition(fin);
            if (debut == fin)
                return(null);
            JaxeElement firstel = doc.rootJE.elementA(debut);
            if ((firstel instanceof JECommentaire || firstel instanceof JECData) &&
                    firstel.debut.getOffset() == debut)
                firstel = firstel.getParent();
            final JaxeElement lastel = doc.rootJE.elementA(fin - 1);
            if (firstel == lastel && (firstel instanceof JETexte ||
                    firstel instanceof JECommentaire || firstel instanceof JECData)) {
                final String s = doc.getText(debut, fin - debut);
                final StringSelection strans = new StringSelection(s);
                return(strans);
            } else {
                //final FragmentXML fragment = new FragmentXML(tp.doc.removeProcessingInstructions(doc.copier(debut, fin)));
                //if (fragment != null)
                final FragmentXML fragment = new FragmentXML(doc.copier(debut, fin));
                final XMLTransferable xmlt = new XMLTransferable(fragment);
                return(xmlt);
            }
        } catch (BadLocationException ex) {
            LOG.error("JaxeTransferHandler.createTransferable", ex);
            return(null);
        }
    }
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (shouldRemove && action == MOVE) {
            if (!(source instanceof JaxeTextPane))
                return;
            final JaxeTextPane tp = (JaxeTextPane)source;
            if (p0.getOffset() != p1.getOffset()) {
                try {
                    tp.doc.remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
                } catch (BadLocationException ex) {
                    LOG.error("JaxeTransferHandler.exportDone", ex);
                }
            }
        }
        if (editionSpeciale) {
            editionSpeciale = false;
            if (!(source instanceof JaxeTextPane))
                return;
            ((JaxeTextPane)source).finEditionSpeciale();
            // si l'import a été annulé, l'edit sera vide, isSignificant renverra false,
            // et donc l'edit ne sera pas affiché dans le menu
        }
        exportComp = null;
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return ((JTextComponent)c).isEditable() ? COPY_OR_MOVE : COPY;
    }
    
    @Override
    public boolean importData(JComponent comp, Transferable t) {
        if (comp instanceof JaxeTextPane) {
            final JaxeTextPane tp = (JaxeTextPane)comp;
            final int pos = tp.getCaretPosition();
            
            if (comp == exportComp && pos >= p0.getOffset() && pos <= p1.getOffset()) {
                shouldRemove = false;
                return(true);
            }
            if (comp == exportComp) {
                tp.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("annulation.GlisserDeposer"), false);
                editionSpeciale = true;
            }
            final DataFlavor flavor = choisirUneSaveur(t.getTransferDataFlavors(), tp.doc.fsave != null);
            if (flavor != null) {
                final InputContext ic = comp.getInputContext();
                if (ic != null)
                    ic.endComposition();
                try {
                    final Object data = t.getTransferData(flavor);
                    if (data == null)
                        return(false);
                    final Position ppos = tp.doc.createPosition(pos);
                    DocumentFragment frag = null;
                    if (data instanceof FragmentXML)
                        frag = ((FragmentXML)data).getFragment();
                    else if (data instanceof String)
                        frag = analyseString(tp, (String)data);
                    Element reffichier;
                    if (tp.doc.cfg == null)
                        reffichier = null;
                    else
                        reffichier = tp.doc.cfg.premierElementAvecType("fichier");
                    JaxeElement parent = tp.doc.elementA(pos);
                    if (parent != null && (parent instanceof JETexte ||
                            (parent.debut.getOffset() == pos && !(parent instanceof JESwing))))
                        parent = parent.getParent();
                    if (frag != null && (parent == null || !(parent instanceof JECommentaire || parent instanceof JECData))) {
                        boolean effaceSelection = false;
                        if (tp.getSelectionStart() != tp.getSelectionEnd()) {
                            effaceSelection = true;
                            tp.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("menus.Coller"), false);
                            tp.doc.remove(tp.getSelectionStart(), tp.getSelectionEnd() - tp.getSelectionStart());
                        }
                        final boolean accept = tp.doc.coller(frag, ppos);
                        if (effaceSelection) {
                            tp.finEditionSpeciale();
                            if (!accept)
                                tp.undo();
                        }
                        return(accept);
                    } else if (reffichier != null && data instanceof Image && tp.doc.fsave != null)
                        JEFichier.collerImage((Image)data, tp.doc, ppos, reffichier);
                    else if (reffichier != null &&
                            flavor.getMimeType().startsWith("image/x-pict") &&
                            System.getProperty("os.name").startsWith("Mac OS") &&
                            data instanceof InputStream && tp.doc.fsave != null) {
                        // inutile à partir de Java for MacOS X v10.5 Update 4 (la JVM renvoie un objet Image au lieu de InputStream)
                        final Image img = Jaxe.mac.convertirPICT((InputStream)data);
                        JEFichier.collerImage(img, tp.doc, ppos, reffichier);
                    } else {
                        if (parent == null)
                            return(false);
                        String s;
                        if (data instanceof String) {
                            s = ((String)data).replace("\r\n", "\n");
                            s = s.replace("\r", "\n");
                        } else if (data instanceof FragmentXML)
                            s = ((FragmentXML)data).toString();
                        else
                            return(false);
                        
                        if (tp.doc.cfg != null) {
                            Element parentref;
                            if (parent == null || !(parent.noeud instanceof Element))
                                parentref = null;
                            else
                                parentref = parent.refElement;
                            if (parentref != null && ((!tp.doc.cfg.contientDuTexte(parentref) && !"".equals(s.trim())) ||
                                    !parent.getEditionAutorisee())) {
                                tp.doc.getGestionErreurs().texteInterdit(parent);
                                return(false);
                            }
                        }
                        ((JaxeTextPane)comp).replaceSelection(s);
                        return true;
                    }
                } catch (final BadLocationException ex) {
                    LOG.error("JaxeTransferHandler.importData", ex);
                } catch (UnsupportedFlavorException ex) {
                    LOG.error("JaxeTransferHandler.importData", ex);
                } catch (IOException ex) {
                    LOG.error("JaxeTransferHandler.importData", ex);
                }
            }
        }
        return false;
    }
    
    /**
     * Si bfsave est true, renvoie (dans l'ordre de préférence) un XMLFragmentFlavor, une image, un stringFlavor, ou null
     * parmi les éléments du tableau en paramètre.
     * Si bfsave est false (cas d'une applet ou d'un fichier non enregistré: on ne peut pas enregistrer d'image),
     * renvoie dans l'ordre un XMLFragmentFlavor, un stringFlavor, une image ou null.
     */
    private static DataFlavor choisirUneSaveur(final DataFlavor[] flavors, final boolean bfsave) {
        if (flavors != null) {
            for (final DataFlavor f : flavors)
                if (f.equals(XMLTransferable.XMLFragmentFlavor))
                    return f;
            if (!bfsave) {
                for (final DataFlavor f : flavors)
                    if (f.equals(DataFlavor.stringFlavor))
                        return f;
            }
            for (final DataFlavor f : flavors)
                if (f.getMimeType().startsWith("image/x-java-image"))
                    return f;
            for (final DataFlavor f : flavors)
                if (f.getMimeType().startsWith("image/"))
                    return f;
            if (bfsave) {
                for (final DataFlavor f : flavors)
                    if (f.equals(DataFlavor.stringFlavor))
                        return f;
            }
        }
        return null;
    }
    
    /**
     * teste si on a le droit d'accéder au presse-papier du système
     */
    protected static boolean canAccessSystemClipboard() {
        try {
            final SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkSystemClipboardAccess();
            return(true);
        } catch (final SecurityException ex) {
            return(false);
        }
    }
    
    private static DocumentFragment analyseString(final JaxeTextPane tp, final String g) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentFragment df = tp.doc.DOMdoc.createDocumentFragment();
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final StringBuilder baliseDoc = new StringBuilder();
            final Element racine = tp.doc.DOMdoc.getDocumentElement();
            if (racine == null) {
                baliseDoc.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><import>");
                baliseDoc.append(g);
                baliseDoc.append("</import>");
            } else {
                baliseDoc.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                baliseDoc.append("<");
                final String nodeName = racine.getNodeName();
                baliseDoc.append(nodeName);
                // on reprend tous les attributs pour pouvoir obtenir les espaces de noms
                // en fonctions des préfixes
                final NamedNodeMap attrs = racine.getAttributes();
                for (int i=0, l = attrs.getLength(); i < l; i++) {
                    final Node item = attrs.item(i);
                    baliseDoc.append(" ");
                    baliseDoc.append(item.getNodeName());
                    baliseDoc.append("=\"");
                    baliseDoc.append(item.getNodeValue());
                    baliseDoc.append("\"");
                }
                baliseDoc.append(">");
                baliseDoc.append(g);
                baliseDoc.append("</");
                baliseDoc.append(nodeName);
                baliseDoc.append(">");
            }
            final org.w3c.dom.Document parseddoc = builder.parse(new InputSource(new StringReader(baliseDoc.toString())));
            copieEnfants(tp.doc.DOMdoc, parseddoc.getDocumentElement(), df);
            if (hasOnlyTextnodes(df))
                df = null;
        } catch (final Exception ex) {
            //System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
            df = null;
        }
        return df;
    }
    
    private static boolean hasOnlyTextnodes(final Node n) {
        if (n.hasChildNodes()) {
            Node child = n.getFirstChild();
            while (child != null) {
                if (child.getNodeType() != Node.TEXT_NODE)
                    return false;
                child = child.getNextSibling();
            }
        }
        return true;
    }
    
    private static void copieEnfants(final org.w3c.dom.Document targetdoc, final Node source, final Node target) {
        Node child = source.getFirstChild();
        while (child != null) {
            target.appendChild(targetdoc.importNode(child, true));
            child = child.getNextSibling();
        }
    }

}
