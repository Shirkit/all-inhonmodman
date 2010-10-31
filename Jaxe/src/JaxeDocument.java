/*
 Jaxe - Editeur XML en Java

 Copyright (C) 2002 Observatoire de Paris-Meudon

 Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

 Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

 Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
 */

package jaxe;

import org.apache.log4j.Logger;

import java.awt.Container;
import java.awt.Toolkit;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.OverlayLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.EditorKit;
import javax.swing.text.IconView;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.undo.UndoableEdit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jaxe.elements.JECData;
import jaxe.elements.JECommentaire;
import jaxe.elements.JEDivision;
import jaxe.elements.JEInconnu;
import jaxe.elements.JEStyle;
import jaxe.elements.JESwing;
import jaxe.elements.JETableTexte;
import jaxe.elements.JETexte;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Classe représentant un document XML
 */
public class JaxeDocument extends DefaultStyledDocument {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JaxeDocument.class);

    private static final ResourceBundle rb = JaxeResourceBundle.getRB();
    static String newline = Jaxe.newline;
    public org.w3c.dom.Document DOMdoc = null;
    public HashMap<Node, JaxeElement> dom2JaxeElement = null;
    public JaxeElement rootJE = null;
    public JaxeTextPane textPane;
    public File fsave = null; // référence sur le disque vers le fichier XML
    //  (null par exemple si le fichier n'est pas sauvegardé ou si il est accédé par le web)
    public URL furl = null; // URL vers le fichier XML
    public String encodage = "ISO-8859-1"; // valeur par défaut
    public boolean modif = false; // utiliser getModif() et setModif() de préférence
    public Config cfg = null;
    public JFrame jframe;
    public String nomFichierCfg;

    final static String kPoliceParDefaut = "Serif";

    final static int kTailleParDefaut = 14;

    private InterfaceGestionErreurs gestionErreurs = new GestionErreurs(this);
    
    private final List<JaxeEditListenerIf> _editListener;
    
    private boolean _ignorer = false;
    
    
    public JaxeDocument() {
        super();
        setDefaultStyle();
        _editListener = new ArrayList<JaxeEditListenerIf>();
    }

    public JaxeDocument(final String nomFichierCfg) {
        super();
        this.nomFichierCfg = nomFichierCfg;
        if (nomFichierCfg != null) {
            try {
                cfg = new Config(nomFichierCfg, true);
            } catch (JaxeException ex) {
                JOptionPane.showMessageDialog(jframe, ex.getMessage(),
                    rb.getString("erreur.Erreur"), JOptionPane.ERROR_MESSAGE);
                cfg = null;
            }
        }
        setDefaultStyle();
        _editListener = new ArrayList<JaxeEditListenerIf>();
    }

    public JaxeDocument(final Config newconfig) {
        super();
        cfg = newconfig;
        setDefaultStyle();
        _editListener = new ArrayList<JaxeEditListenerIf>();
    }

    public JaxeDocument(final JaxeTextPane textPane, final String nomFichierCfg) {
        super();
        this.textPane = textPane;
        this.nomFichierCfg = nomFichierCfg;
        jframe = textPane.jframe;
        if (nomFichierCfg != null) {
            try {
                cfg = new Config(nomFichierCfg, true);
            } catch (JaxeException ex) {
                JOptionPane.showMessageDialog(jframe, ex.getMessage(),
                    rb.getString("erreur.Erreur"), JOptionPane.ERROR_MESSAGE);
                cfg = null;
            }
        }
        setDefaultStyle();
        _editListener = new ArrayList<JaxeEditListenerIf>();
    }

    /**
     * Définie le gestionnaire d'erreurs pour le document
     */
    public void setGestionErreurs(final InterfaceGestionErreurs gestionErreurs) {
        this.gestionErreurs = gestionErreurs;
    }

    /**
     * Renvoit le gestionnaire d'erreurs du document
     */
    public InterfaceGestionErreurs getGestionErreurs() {
        return gestionErreurs;
    }

    private void setDefaultStyle() {
        final Style defaultStyle = getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, kPoliceParDefaut);
        StyleConstants.setFontSize(defaultStyle, kTailleParDefaut);
    }

    public void setTextPane(final JaxeTextPane textPane) {
        this.textPane = textPane;
        jframe = textPane.jframe;
    }
    
    /**
     * Indique si le document a été modifié depuis la dernière sauvegarde ou pas.
     */
    public boolean getModif() {
        return(modif);
    }
    
    /**
     * Spécifie si le document a été modifié depuis la dernière sauvegarde ou pas.
     */
    public void setModif(final boolean modif) {
        if (this.modif != modif) {
            // on évite d'utiliser la classe JaxeFrame pour limiter les dépendances avec les applets
            if (jframe != null && "jaxe.JaxeFrame".equals(jframe.getClass().getName()))
                jframe.getRootPane().putClientProperty("Window.documentModified", new Boolean(modif)); // pour MacOS X
            this.modif = modif;
        }
    }
    
    /**
     * Initialise un document vide
     */
    public void nouveau() {
        if (cfg == null) {
            LOG.error("nouveau() - nouveau: pas de fichier de configuration en entrée");
            // cette erreur ne peut normalement pas arriver, donc pas de string
            // dans le ResourceBundle
            return;
        }
        fsave = null;
        furl = null;
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docbuilder = dbf.newDocumentBuilder();
            docbuilder.setEntityResolver(Jaxe.getEntityResolver());
            DOMdoc = docbuilder.newDocument();
        } catch (final ParserConfigurationException ex) {
            LOG.error("nouveau: ParserConfigurationException", ex);
        }
        if (cfg.getEncodage() != null)
            encodage = cfg.getEncodage();
        dom2JaxeElement = new HashMap<Node, JaxeElement>();
        final ArrayList<String> racines = cfg.listeRacines();
        if (racines.size() == 1) {
            final Element refracine = cfg.premierElementRacine();
            if (refracine == null) {
                JOptionPane.showMessageDialog(jframe, rb.getString("erreur.racineIncorrecte"),
                    rb.getString("document.Nouveau"), JOptionPane.ERROR_MESSAGE);
                rootJE = null;
            } else {
                final String typeAffichage = cfg.typeAffichageElement(refracine);
                if (!"".equals(typeAffichage))
                    rootJE = JEFactory.createJE(this, refracine, cfg.nomElement(refracine), "element", null);
                else
                    rootJE = new JEDivision(this);
                Element rootel = (Element) rootJE.nouvelElement(refracine);
                if (rootel != null) {
                    cfg.ajouterAttributsEspaces(rootel);
                    DOMdoc.appendChild(rootel);
                    textPane.debutIgnorerEdition();
                    try {
                        rootJE.creer(createPosition(0), rootel);
                    } catch (final BadLocationException ex) {
                        LOG.error("nouveau() - BadLocationException", ex);
                    }
                    textPane.finIgnorerEdition();
                    textPane.setCaretPosition(rootJE.insPosition().getOffset());
                    textPane.moveCaretPosition(rootJE.insPosition().getOffset());
                } else
                    rootJE = null;
            }
        } else
            rootJE = null;
    }

    /**
     * Initialise un document lu à partir d'une URL
     */
    public boolean lire(final URL url) {
        return(lire(url, (String)null));
    }
    
    private org.w3c.dom.Document lectureDocumentXML(final URL url) {
        org.w3c.dom.Document ddoc = null;
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            //dbf.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
            //dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // comment faire ça ? -> jaxp_feature_not_supported
            final DocumentBuilder docbuilder = dbf.newDocumentBuilder();
            
            //docbuilder.setEntityResolver(Jaxe.getEntityResolver());
            // on ignore les DTD pour ne pas gonfler le document DOM avec des valeurs d'attributs qui
            // n'étaient pas dans le document initial
            docbuilder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if (systemId.endsWith(".dtd") || systemId.endsWith(".DTD"))
                        return(new InputSource(new StringReader("")));
                    else if (Jaxe.getEntityResolver() != null)
                        return(Jaxe.getEntityResolver().resolveEntity(publicId, systemId));
                    else
                        return(null);
                }
            });
            
            //ddoc = docbuilder.parse(url.toExternalForm());
            // le cache peut poser problème dans certains cas
            final URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            ddoc = docbuilder.parse(conn.getInputStream(), url.toExternalForm());
            if (ddoc.getXmlEncoding() != null) // DOM 3, Java 1.5, old xerces must not be on the classpath
                encodage = ddoc.getXmlEncoding();
        } catch (final SAXException ex) {
            String infos = rb.getString("erreur.XML") + ":" + newline;
            infos += ex.getMessage();
            if (ex instanceof SAXParseException)
                infos += " " + rb.getString("erreur.ALaLigne") + " " +
                    ((SAXParseException)ex).getLineNumber();
            JOptionPane.showMessageDialog(jframe, infos,
                rb.getString("document.Lecture"), JOptionPane.ERROR_MESSAGE);
            return(null);
        } catch (final IOException ex) {
            String infos = rb.getString("erreur.ES") + ":" + newline;
            infos += ex.getMessage();
            JOptionPane.showMessageDialog(jframe, infos,
                rb.getString("document.Lecture"), JOptionPane.ERROR_MESSAGE);
            return(null);
        } catch (final ParserConfigurationException ex) {
            LOG.error("lire: ParserConfigurationException", ex);
            return(null);
        }
        
        furl = url;
        try {
            fsave = new File(url.toURI());
        } catch (final Exception ex) {
            fsave = null;
        }
        return(ddoc);
    }
    
    /**
     * Initialise un document lu à partir d'une URL, en utilisant un fichier de config donné par nom de fichier
     */
    public boolean lire(final URL url, final String cheminFichierCfg) {
        final org.w3c.dom.Document ddoc = lectureDocumentXML(url);
        if (ddoc == null)
            return(false);
        return(setDOMDoc(ddoc, cheminFichierCfg));
    }
    
    /**
     * Initialise un document lu à partir d'une URL, en utilisant un fichier de config donné par URL
     */
    public boolean lire(final URL url, final URL urlFichierCfg) {
        final org.w3c.dom.Document ddoc = lectureDocumentXML(url);
        if (ddoc == null)
            return(false);
        return (setDOMDoc(ddoc, urlFichierCfg));
    }

    /**
     * Spécifie le document DOM de ce document Jaxe
     */
    public boolean setDOMDoc(final org.w3c.dom.Document ddoc) {
        return(setDOMDoc(ddoc, (String)null));
    }
    
    /**
     * Spécifie le document DOM de ce document Jaxe, en utilisant un fichier de config donné par nom de fichier.
     * Si cheminFichierCfg est null, une config est cherchée en fonction de la racine du document.
     */
    public boolean setDOMDoc(final org.w3c.dom.Document ddoc, final String cheminFichierCfg) {
        final Element rootel = ddoc.getDocumentElement();
        if (cheminFichierCfg == null)
            nomFichierCfg = chercherConfig(rootel);
        else
            nomFichierCfg = cheminFichierCfg;
        if (nomFichierCfg == null)
            JOptionPane.showMessageDialog(jframe,
                rb.getString("erreur.ConfigPour") + " " +
                Config.localValue(rootel.getTagName()),
                rb.getString("erreur.Erreur"), JOptionPane.ERROR_MESSAGE);
        try {
            URL urlFichierCfg;
            if (nomFichierCfg == null)
                urlFichierCfg = null;
            else
                urlFichierCfg = (new File(nomFichierCfg)).toURI().toURL();
            return(setDOMDoc(ddoc, urlFichierCfg));
        } catch (final MalformedURLException ex) {
            LOG.error("setDOMDoc: MalformedURLException: " + ex.getMessage());
            return(false);
        }
    }
    
    /**
     * Spécifie le document DOM de ce document Jaxe, en utilisant un fichier de config donné par URL
     */
    public boolean setDOMDoc(final org.w3c.dom.Document ddoc, final URL urlFichierCfg) {
        DOMdoc = ddoc;
        dom2JaxeElement = new HashMap<Node, JaxeElement>();
        final Element rootel = DOMdoc.getDocumentElement();
        final Properties prefs = Preferences.getPref();
        final boolean consIndent = (prefs != null && "true".equals(prefs.getProperty("consIndent")));
        if (!consIndent)
            virerEspaces(rootel);
        
        if (urlFichierCfg != null) {
            nomFichierCfg = urlFichierCfg.getPath();
            try {
                cfg = new Config(urlFichierCfg, true);
            } catch (JaxeException ex) {
                JOptionPane.showMessageDialog(jframe, ex.getMessage(),
                    rb.getString("erreur.Erreur"), JOptionPane.ERROR_MESSAGE);
                cfg = null;
            }
        } else
            nomFichierCfg = null;
        
        if (urlFichierCfg == null || cfg == null)
            rootJE = new JEInconnu(this);
        else {
            final Element refracine = cfg.getElementRef(rootel);
            final String typeAffichage = cfg.typeAffichageElement(refracine);
            if (!"".equals(typeAffichage))
                rootJE = JEFactory.createJE(this, refracine, cfg.nomElement(refracine), "element", rootel);
            else
                rootJE = new JEDivision(this);
        }

        try {
            textPane.debutIgnorerEdition();
            rootJE.creer(createPosition(0), rootel);
            textPane.finIgnorerEdition();
        } catch (final BadLocationException ex) {
            LOG.error("setDOMDoc(org.w3c.dom.Document, String) - BadLocationException", ex);
            return false;
        }
        //DefaultDocumentEvent de = new DefaultDocumentEvent(0, getLength(), DocumentEvent.EventType.CHANGE);
        //fireChangedUpdate(de);
        // marche pas !

        setModif(false);
        return true;
    }

    /**
     * Sets the RootNode of the Document
     * 
     * @param node
     *            the Node
     * @return boolean successfull ?
     */
    public boolean setRootElement(final org.w3c.dom.Element node) {
        return setRootElement(node, node);
    }

    /**
     * Sets the RootNode of the Document with a Node that is used to search the
     * Config-File
     * 
     * @param node
     *            the Node
     * @param configNode
     *            the Node wich will be used as Config-File
     * @return boolean successfull ?
     */
    public boolean setRootElement(final org.w3c.dom.Element node,
            final org.w3c.dom.Element configNode) {
        DOMdoc = node.getOwnerDocument();
        dom2JaxeElement = new HashMap<Node, JaxeElement>();
        final Element rootel = node;
        final Properties prefs = Preferences.getPref();
        final String nomFichierCfg = chercherConfig(configNode);
        if (nomFichierCfg == null)
            LOG.error("setRootElement(org.w3c.dom.Element, org.w3c.dom.Element) - " + rb.getString("erreur.ConfigPour")
                    + " " + Config.localValue(rootel.getTagName()));

        final boolean consIndent = (prefs != null && "true".equals(prefs.getProperty("consIndent")));
        if (!consIndent)
            virerEspaces(rootel);

        if (nomFichierCfg == null)
            rootJE = new JEInconnu(this);
        else {
            final Element refracine = cfg.getElementRef(rootel, null);
            final String typeAffichage = cfg.typeAffichageElement(refracine);
            if (!"".equals(typeAffichage))
                rootJE = JEFactory.createJE(this, refracine, cfg.nomElement(refracine), "element", rootel);
            else
                rootJE = new JEDivision(this);
        }

        try {
            textPane.debutIgnorerEdition();
            rootJE.creer(createPosition(0), rootel);
            textPane.finIgnorerEdition();
        } catch (final BadLocationException ex) {
            LOG.error("setRootElement(org.w3c.dom.Element, org.w3c.dom.Element) - BadLocationException", ex);
            return false;
        }
        //DefaultDocumentEvent de = new DefaultDocumentEvent(0, getLength(),
        // DocumentEvent.EventType.CHANGE);
        //fireChangedUpdate(de);
        // marche pas !

        setModif(false);
        return true;
    }

    public Node getRootElement() {
        final Node result = rootJE.noeud.cloneNode(true);
        boolean changed = false;
        do {
            changed = false;
            Node child = result.getFirstChild();
            while (child != null) {
                if (child instanceof Element) {
                    final Element refElement =  cfg.getElementRef((Element)child);
                    
                    if (refElement != null) {
                        final String typeAffichage = cfg.typeAffichageElement(refElement);
                        if ("style".equals(typeAffichage)) {
                            if (child.getNextSibling() != null) {
                                final Node next = child.getNextSibling();
                                if (next instanceof Element) {
                                    final Element refElement2 = cfg.getElementRef((Element)next);
                                    if (refElement2 != null) {
                                        final String typeAffichage2 = cfg.typeAffichageElement(refElement2);
                                        if ("style".equals(typeAffichage2)) {
                                            final Node prev = child.getPreviousSibling();
                                            changed = changed | joinNodes(child, next);
                                            if (changed) {
                                                if (prev == null) {
                                                    child = result.getFirstChild();
                                                } else {
                                                    child = prev;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (!changed) {
                                changed = changed | goDeep(child);
                            }
                        
                        } else {
                            changed = changed | goDeep(child);
                        }
                    }
                    
                } else {
                    changed = changed | goDeep(child);
                }
                child = child.getNextSibling();
            }
        } while (changed);
        return result;
    }

    private int childCount(final Node n){
        return n.getChildNodes().getLength();
    }
    
    /**
     * @param child
     * @param nextSibling
     * @return
     */
    private boolean joinNodes(final Node child, final Node nextSibling) {
        if (NodeUtils.isEqualNode(child, nextSibling)) {
            Node c = nextSibling.getFirstChild();
            while (c != null) {
                child.appendChild(c);
                c = c.getNextSibling();
            }
            nextSibling.getParentNode().removeChild(nextSibling);
            return true;
        }
        return false;
    }

    private boolean goDeep(final Node n) {
        boolean changed = false;
        Node child = n.getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                final Element refElement =  cfg.getElementRef((Element)child);
                
                if (refElement != null) {
                    final String typeAffichage = cfg.typeAffichageElement(refElement);
                    if ("style".equals(typeAffichage)) {
                        if (child.getNextSibling() != null) {
                            final Node next = child.getNextSibling();
                            if (next instanceof Element) {
                                final Element refElement2 =  cfg.getElementRef((Element)next);
                                if (refElement2 != null) {
                                    final String typeAffichage2 = cfg.typeAffichageElement(refElement2);
                                    if ("style".equals(typeAffichage2)) {
                                        final Node prev = child.getPreviousSibling();
                                        if (joinNodes(child, next)) {
                                            changed = true;
                                            if (prev == null) {
                                                child = n.getFirstChild();
                                            } else {
                                                child = prev;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!changed) {
                            changed = changed | goDeep(child);
                        }
                    
                    } else {
                        changed = changed | goDeep(child);
                    }
                }
                
            } else {
                changed = changed | goDeep(child);
            }
            child = child.getNextSibling();
        }
        return changed;
    }

    protected String chercherConfig(final Element rootel) {
        String nomFichierCfg = null;
        final File configdir = new File("config");
        final String[] liste = configdir.list();
        if (liste == null) {
            LOG.error("chercherConfig(Element) - " + rb.getString("erreur.DossierConfig"));
            return (null);
        }
        for (final String nomFichier : liste) {
            if (nomFichier.endsWith("_cfg.xml") || nomFichier.endsWith("_config.xml")) {
                final File cfgFile = new File("config" + File.separator + nomFichier);
                try {
                    final URL cfgURL = cfgFile.toURI().toURL();
                    final ArrayList<String> noms = Config.nomsElementsRacine(cfgURL);
                    if (noms.contains(Config.localValue(rootel.getTagName()))) {
                        try {
                            final Config cfgTest = new Config(cfgURL, true);
                            final String rootns = rootel.getNamespaceURI();
                            final String cfgns = cfgTest.espaceCible();
                            if ((rootns != null && rootns.equals(cfgns)) ||
                                    (rootns == null && (cfgns == null || "".equals(cfgns)))) {
                                nomFichierCfg = cfgFile.getPath();
                                cfg = cfgTest;
                                break;
                            }
                        } catch (JaxeException ex) {
                        }
                    }
                } catch (final MalformedURLException ex) {
                    LOG.error("JaxeDocument.chercherConfig : MalformedURLException: " + ex.getMessage(), ex);
                }
            }
        }
        return (nomFichierCfg);
    }

    // retire les espaces gênants de cet élément, et récursivement
    public void virerEspaces(final Element el) {
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE)
                virerEspaces((Element) n);
            else if (n.getNodeType() == Node.TEXT_NODE && isFirstTextElement(n)) {
                final StringBuilder sBuilder = new StringBuilder(n.getNodeValue());

                // on ne retire pas les blancs s'il n'y a que du blanc dans
                // l'élément
                if (n.getNextSibling() == null
                        && n.getPreviousSibling() == null
                        && "".equals(sBuilder.toString().trim()))
                    break;

                if (n.getParentNode().getFirstChild() == n) {
                    // retire espaces au début si le texte est au début de l'élément
                    int ifin = 0;
                    while (ifin < sBuilder.length() && (sBuilder.charAt(ifin) == ' ' || sBuilder.charAt(ifin) == '\t'))
                        ifin++;
                    if (ifin > 0)
                        sBuilder.delete(0, ifin);
                }

                // retire les espaces après les retours à la ligne
                int idebut = sBuilder.indexOf(newline + " ");
                int idebuttab = sBuilder.indexOf(newline + "\t");
                if (idebuttab != -1 && (idebut == -1 || idebuttab < idebut))
                    idebut = idebuttab;
                while (idebut != -1) {
                    int ifin = idebut;
                    while (ifin + 1 < sBuilder.length()
                            && (sBuilder.charAt(ifin + 1) == ' ' || sBuilder.charAt(ifin + 1) == '\t'))
                        ifin++;
                    sBuilder.delete(idebut + 1, ifin + 1);
                    idebut = sBuilder.indexOf(newline + " ");
                    idebuttab = sBuilder.indexOf(newline + "\t");
                    if (idebuttab != -1 && (idebut == -1 || idebuttab < idebut))
                        idebut = idebuttab;
                }

                // condense les espaces partout
                idebut = sBuilder.indexOf("  ");
                while (idebut != -1) {
                    int ifin = idebut;
                    while (ifin + 1 < sBuilder.length() && sBuilder.charAt(ifin + 1) == ' ')
                        ifin++;
                    sBuilder.delete(idebut, ifin);
                    idebut = sBuilder.indexOf("  ");
                }
                if (sBuilder.length() == 0) {
                    Node n2 = n.getPreviousSibling();
                    el.removeChild(n);
                    if (n2 == null)
                        n2 = el.getFirstChild();
                    n = n2;
                    if (n == null)
                        break;
                } else
                    n.setNodeValue(sBuilder.toString());
            }
        }
    }
    
    private boolean isFirstTextElement(final Node n) {
        Element bref = null;
        final Element parentNode = (Element) n.getParentNode();
        if (cfg != null && parentNode != null)
            bref = cfg.getElementRef(parentNode);
        if (bref != null) {
            final String type = cfg.typeAffichageElement(bref);
            if ("style".equals(type)) {
                Node prevNode = parentNode.getPreviousSibling();
                boolean found = false;
                while (prevNode != null && !found) {
                    if (prevNode.getNodeType() == Node.TEXT_NODE) {
                        final String prevText = prevNode.getNodeValue();
                        if (!(prevText.endsWith(" ") || prevText.endsWith("\n")))
                            return false;
                        found = true;
                    } else if (prevNode.getNodeType() == Node.ELEMENT_NODE) {
                        bref = cfg.getElementRef((Element)prevNode);
                        if (bref != null && "style".equals(cfg.typeAffichageElement(bref)))
                            return true;
                        found = true;
                    }
                    prevNode = prevNode.getPreviousSibling();
                }
                if (prevNode == null && !found)
                    return isFirstTextElement(parentNode);
            }
        }
        return true;
    }

    
    public void sendToWriter(final Writer destination) {
        try {
            final DOMSource domSource = new DOMSource(DOMdoc);
            final StreamResult streamResult = new StreamResult(destination);
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.METHOD, "xml");
            serializer.setOutputProperty(OutputKeys.ENCODING, encodage);
            serializer.setOutputProperty(OutputKeys.INDENT, "no");
            if (cfg != null) {
                if (cfg.getPublicId() != null)
                    serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, cfg.getPublicId());
                if (cfg.getSystemId() != null)
                    serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, cfg.getSystemId());
            }
            serializer.transform(domSource, streamResult);
        } catch (final TransformerConfigurationException ex) {
            LOG.error("DOMVersXML: TransformerConfigurationException", ex);
        } catch (final TransformerException ex) {
            LOG.error("DOMVersXML: TransformerException", ex);
        }
    }
    
    class ReaderThread extends Thread {
        PipedWriter pipeout;
        public ReaderThread(final PipedWriter pipeout) {
            this.pipeout = pipeout;
        }
        @Override
        public void run() {
            sendToWriter(pipeout);
            try {
                pipeout.close();
            } catch (final IOException ex) {
                LOG.error("ReaderThread: pipeout.close", ex);
            }
        }
    }
    
    public Reader getReader() throws IOException {
        final PipedWriter pipeout = new PipedWriter();
        final PipedReader pipein = new PipedReader(pipeout);
        final ReaderThread rt = new ReaderThread(pipeout);
        rt.start();
        return pipein;
    }
    
    public void ecrire(final File f) throws IOException {
        final FileOutputStream fos = new FileOutputStream(f);
        final Writer fw = new OutputStreamWriter(fos, encodage);
        sendToWriter(fw);
        fw.close();
        fos.close();
        try {
            furl = f.toURI().toURL();
        } catch (final MalformedURLException ex) {
            LOG.error("ecrire(File) - MalformedURLException", ex);
            furl = null;
        }
        fsave = f;
        setModif(false);
    }

    public String getPathAsString(final int p) {
        if (rootJE == null)
            return null;
        final String chemin = rootJE.cheminA(p);
        return (chemin);
    }

    public void mettreAJourDOM() {
        rootJE.mettreAJourDOM();
    }

    public JaxeElement elementA(final int pos) {
        if (rootJE == null)
            return (null);
        return (rootJE.elementA(pos));
    }

    public DocumentFragment copier(final int debut, final int fin) {
        JaxeElement firstel = rootJE.elementA(debut);
        if (firstel == null) {
            Toolkit.getDefaultToolkit().beep();
            return null;
        }
        firstel = rootJE.elementA(debut);
        while (firstel.debut.getOffset() == debut && firstel.getParent() instanceof JESwing &&
                firstel.getParent().debut.getOffset() == debut && firstel.getParent().fin.getOffset() <= fin)
            firstel = firstel.getParent();
        JaxeElement p1 = firstel;
        if (p1 instanceof JETexte || p1 instanceof JEStyle || p1.debut.getOffset() == debut)
            p1 = p1.getParent();
        if (p1 == null && firstel == rootJE) {
            final DocumentFragment frag = DOMdoc.createDocumentFragment();
            frag.appendChild(firstel.noeud.cloneNode(true));
            return(frag);
        }
        JaxeElement lastel = rootJE.elementA(fin - 1);
        if (lastel == null) {
            Toolkit.getDefaultToolkit().beep();
            return null;
        }
        lastel = rootJE.elementA(fin - 1);
        if (lastel.fin.getOffset() == fin-1 && lastel.getParent() instanceof JESwing &&
                lastel.getParent().debut.getOffset() >= debut && lastel.getParent().fin.getOffset() == fin)
            lastel = lastel.getParent();
        if (lastel.fin.getOffset() == fin && lastel.getParent() instanceof JESwing &&
                lastel.getParent().debut.getOffset() >= debut && lastel.getParent().fin.getOffset() == fin)
            lastel = lastel.getParent();
        while (lastel.fin.getOffset() == fin-1 &&
                (lastel.getParent() instanceof JESwing || lastel.getParent() instanceof JETableTexte) &&
                lastel.getParent().debut.getOffset() >= debut && lastel.getParent().fin.getOffset() == fin-1)
            lastel = lastel.getParent();
        JaxeElement p2 = lastel;
        if (p2 instanceof JETexte || p2 instanceof JEStyle || p2.fin.getOffset() == fin - 1 ||
                (p2 instanceof JESwing && p2.fin.getOffset() == fin))
            p2 = p2.getParent();
        if (p1 != p2 || p1 == null) {
            return null;
        }
        if (firstel == lastel && firstel.getClass().getName().equals("jaxe.elements.JETableTexte$JESwingTD")) {
            // on ne copie pas la cellule entière si juste son contenu est sélectionné
            p1 = firstel;
            firstel = getElementForNode(p1.noeud.getFirstChild());
            lastel = getElementForNode(p1.noeud.getLastChild());
        }
        final DocumentFragment frag = DOMdoc.createDocumentFragment();
        if (firstel instanceof JETexte) {
            String texte = firstel.noeud.getNodeValue();
            if (fin - firstel.debut.getOffset() > texte.length()) {
                texte = texte.substring(debut - firstel.debut.getOffset());
            } else {
                texte = texte.substring(debut - firstel.debut.getOffset(), fin - firstel.debut.getOffset());
            }
            final Node tn = DOMdoc.createTextNode(texte);
            frag.appendChild(tn.cloneNode(true));
        } else if (firstel instanceof JEStyle) {
            String texte = ((JEStyle)firstel).getText();
            if (fin - firstel.debut.getOffset() > texte.length()) {
                texte = texte.substring(debut - firstel.debut.getOffset());
            } else {
                texte = texte.substring(debut - firstel.debut.getOffset(), fin - firstel.debut.getOffset());
            }
            Node tn = DOMdoc.createTextNode(texte);
            final Iterator<Element> style = ((JEStyle)firstel)._styles.iterator();
            while (style.hasNext()) {
                final Node node = style.next().cloneNode(false);
                node.appendChild(tn);
                tn = node;
            }
            frag.appendChild(tn.cloneNode(true));
        } else if ((firstel instanceof JECommentaire || firstel instanceof JECData) &&
                debut > firstel.debut.getOffset()) {
            String texte = firstel.noeud.getNodeValue();
            if (fin - firstel.debut.getOffset() - 1 > texte.length())
                texte = texte.substring(debut - firstel.debut.getOffset() - 1);
            else
                texte = texte.substring(debut - firstel.debut.getOffset() - 1, fin - firstel.debut.getOffset() - 1);
            final Node tn = DOMdoc.createTextNode(texte);
            frag.appendChild(tn.cloneNode(true));
        } else
            frag.appendChild(firstel.noeud.cloneNode(true));
        if (firstel == p1) p1 = firstel.getParent();
        Node n = p1.noeud.getFirstChild();
        while (n != null && n != firstel.noeud)
            n = n.getNextSibling();
        if (n == null) {
            LOG.error("copier(int, int) - erreur dans la copie de texte!");
            return null;
        }
        if (firstel != lastel) {
            n = n.getNextSibling();
            while (n != null && n != lastel.noeud) {
                frag.appendChild(n.cloneNode(true));
                n = n.getNextSibling();
            }
            if (n == null) {
                LOG.error("copier(int, int) - erreur dans la copie de texte!");
                return null;
            }
            if (lastel instanceof JETexte) {
                String texte = lastel.noeud.getNodeValue();
                texte = texte.substring(0, fin - lastel.debut.getOffset());
                final Node tn = DOMdoc.createTextNode(texte);
                frag.appendChild(tn.cloneNode(true));
            } else if (lastel instanceof JEStyle) {
                String texte = ((JEStyle)lastel).getText();
                texte = texte.substring(0, fin - lastel.debut.getOffset());
                Node tn = DOMdoc.createTextNode(texte);
                final Iterator<Element> style = ((JEStyle)lastel)._styles.iterator();
                while (style.hasNext()) {
                    final Node node = style.next().cloneNode(false);
                    node.appendChild(tn);
                    tn = node;
                }
                frag.appendChild(tn.cloneNode(true));
            } else
                frag.appendChild(lastel.noeud.cloneNode(true));
        }
//        removeProcessingInstructions(frag);
        return frag;
    }
    
    @Deprecated
    protected Node removeProcessingInstructions(final Node n) {
        if (n == null) return null;
        Node child = n.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                final Node prev = child.getPreviousSibling();
                child.getParentNode().removeChild(child);
                if (prev != null) {
                    child = prev;
                } else {
                    child = n.getFirstChild();
                    continue;
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                removeProcessingInstructions(child);
            }
            child = child.getNextSibling();
        }
        return n;
    }

    /**
     * Teste si l'insertion d'un fragment est autorisée sous un certain élément
     * parent à la position pos. Si elle n'est pas autorisée, affiche un message
     * d'erreur et renvoit false. Sinon renvoit true.
     */
    public boolean testerInsertionFragment(final DocumentFragment frag,
            final JaxeElement parent, final Position pos) {
        if (cfg != null) {
            if (parent == null) {
                if (frag.getChildNodes().getLength() == 1 && frag.getFirstChild() instanceof Element &&
                        pos.getOffset() == 0) {
                    Node nracine = frag.getFirstChild();
                    final String nomElement = nracine.getNodeName();
                    Config conf = cfg.getElementConf((Element) nracine);
                    if (conf == null)
                        conf = cfg;
                    final Element refElement = conf.referenceElement(nomElement);
                    final ArrayList<Element> racines = cfg.listeElementsRacines();
                    if (refElement == null)
                        LOG.error("testerInsertionFragment : pas de référence pour " + nomElement);
                    else if (racines.contains(refElement))
                        return(true);
                }
                JOptionPane.showMessageDialog(jframe, rb.getString("insertion.Expression"),
                    rb.getString("insertion.InsertionBalise"), JOptionPane.ERROR_MESSAGE);
                return(false);
            } else {
                Element parentref = parent.refElement;
                for (Node n=frag.getFirstChild(); n != null; n=n.getNextSibling()) {
                    if (n.getNodeType() == Node.TEXT_NODE && !"".equals(n.getNodeValue().trim()) &&
                            parentref != null && !cfg.contientDuTexte(parentref)) {
                        final String infos = rb.getString("erreur.InsertionInterdite") + " " +
                            parent.noeud.getNodeName();
                        JOptionPane.showMessageDialog(jframe, infos,
                            rb.getString("document.Insertion"), JOptionPane.ERROR_MESSAGE);
                        return (false);
                    } else if (n.getNodeType() == Node.ELEMENT_NODE) {
                        final String nomElement = n.getNodeName();
                        Config conf = cfg.getElementConf((Element) n);
                        if (conf == null)
                            conf = cfg;
                        final Element refElement = conf.referenceElement(nomElement);
                        if (refElement == null)
                            LOG.error("testerInsertionFragment : pas de référence pour " + nomElement);
                        parentref = null;
                        Element parentn = (Element) parent.noeud;
                        final Config pconf = cfg.getElementConf(parentn);
                        if (pconf != null && pconf != conf)
                            parentn = cfg.chercheParentConfig(parentn, conf);
                        if (parentn != null)
                            parentref = dom2JaxeElement.get(parentn).refElement;
                        if (parentref != null && !conf.estSousElement(parentref, nomElement)) {
                            gestionErreurs.enfantInterditSousParent(parent, refElement);
                            return (false);
                        }
                        if (!cfg.insertionPossible(parent, pos.getOffset(), pos.getOffset(), refElement)) {
                            String expr;
                            if (parentref == null)
                                expr = "";
                            else
                                expr = cfg.expressionReguliere(parentref);
                            gestionErreurs.insertionImpossible(expr, parent, refElement);
                            return (false);
                        }
                    }
                }
            }
        }
        return (true);
    }
    
    /** pour coller du XML */
    public boolean coller(final Object pp, final Position pos) {
        if (!(pp instanceof DocumentFragment)) return false;
        final DocumentFragment frag = (DocumentFragment) (((DocumentFragment) pp).cloneNode(true));
        
        return coller(frag, pos, true);
    }
    
    /**
     * Colle un fragment XML dans le document à la position pos
     * @param pos
     * @param frag
     */
    public boolean coller(DocumentFragment frag, Position pos, final boolean event) {
        JaxeElement parent = null;
        if (rootJE != null)
            parent = rootJE.elementA(pos.getOffset());
        if (parent != null && parent.debut.getOffset() == pos.getOffset() && !(parent instanceof JESwing))
            parent = parent.getParent() ;
        if (parent == null && rootJE != null) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }

        textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("menus.Coller"), false);
        
        if (parent != null && parent.noeud.getNodeType() == Node.TEXT_NODE || parent instanceof JEStyle) {
            final JaxeElement je1 = parent;
            parent = parent.getParent();
            if (pos.getOffset() > je1.debut.getOffset()
                    && pos.getOffset() <= je1.fin.getOffset()) {
                // couper la zone de texte en 2
                final JaxeElement je2 = je1.couper(pos);
            }
        }

        if (!testerInsertionFragment(frag, parent, pos)) {
            textPane.finEditionSpeciale();
//            textPane.undo();
            return false;
        }
        if (event)
            pos = firePrepareElementAddEvent(pos);

        if (DOMdoc != frag.getOwnerDocument())
            frag = (DocumentFragment) DOMdoc.importNode(frag, true);
        
        if (parent == null) {
            // si on colle un document entier
            final Element rootel = (Element) frag.getFirstChild();
            DOMdoc.appendChild(rootel);
            if (cfg == null)
                rootJE = new JEInconnu(this);
            else {
                final Element refracine = cfg.getElementRef(rootel);
                final String typeAffichage = cfg.typeAffichageElement(refracine);
                if (!"".equals(typeAffichage))
                    rootJE = JEFactory.createJE(this, refracine, cfg.nomElement(refracine), "element", rootel);
                else
                    rootJE = new JEDivision(this);
            }
            try {
                textPane.debutIgnorerEdition();
                rootJE.creer(createPosition(0), rootel);
                textPane.finIgnorerEdition();
            } catch (final BadLocationException ex) {
                LOG.error("JaxeDocument.coller - BadLocationException", ex);
                textPane.finEditionSpeciale();
                return(false);
            }
            final JaxeElement newje = getElementForNode(rootel);
            if (newje != null)
                textPane.addEdit(new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, newje));
            textPane.finEditionSpeciale();
            setModif(true);
            textPane.miseAJourArbre();
            return(true);
        }
        
        final ArrayList<Node> nl = new ArrayList<Node>();
        for (Node n = frag.getFirstChild(); n != null; n = n.getNextSibling())
            nl.add(n);

        parent.insererDOM(pos, frag);
        textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("menus.Coller"), true);
        JaxeElement last = null;
        for (final Node n : nl) {
            // creerEnfant modifie le ptr de fin, ce qui est utile à la création
            // du doc, mais pas ici
            final Position sfin = parent.fin;
            parent.creerEnfant(pos, n);
            parent.fin = sfin;
            final JaxeElement newje = getElementForNode(n);
            
            // on corrige la position du parent, qui peut être changée après creerEnfant si c'est un JESwing
            JaxeElement testparent = parent;
            while (testparent instanceof JESwing && testparent.debut.getOffset() > newje.debut.getOffset()) {
                try {
                    testparent.debut = createPosition(newje.debut.getOffset());
                } catch (final BadLocationException ex) {
                    LOG.error("coller(DocumentFragment, Position, boolean) - BadLocationException", ex);
                }
                testparent = testparent.getParent();
            }
            
            if (newje != null)
                textPane.addEdit(new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, newje));
            last = newje;
        }
        if (event)
            pos = fireElementAddedEvent(new JaxeEditEvent(this, last), pos);
        textPane.finEditionSpeciale();
        textPane.finEditionSpeciale();
        parent.regrouperTextes();
        parent.majValidite();
        setModif(true);
        textPane.miseAJourArbre();
        return true;
    }

    @Deprecated
    public void coller(final JTextComponent target) {
        LOG.error("coller(JTextComponent)");
    }

    @Deprecated
    public String pp2string(final Object pp) {
        if (!(pp instanceof DocumentFragment))
            return null;
        final DocumentFragment frag = (DocumentFragment) pp;
        return(DOMVersXML(frag));
    }
    
    public static String DOMVersXML(final Node xmldoc) {
        try {
            final DOMSource domSource = new DOMSource(xmldoc);
            final StringWriter sw = new StringWriter();
            final StreamResult streamResult = new StreamResult(sw);
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer serializer = tf.newTransformer();
            //serializer.setOutputProperty(OutputKeys.ENCODING, encodage);
            serializer.setOutputProperty(OutputKeys.INDENT, "no");
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.transform(domSource, streamResult);
            return(sw.toString());
        } catch (final TransformerConfigurationException ex) {
            LOG.error("DOMVersXML: TransformerConfigurationException", ex);
            return(null);
        } catch (final TransformerException ex) {
            LOG.error("DOMVersXML: TransformerException", ex);
            return(null);
        }
    }
    
    protected void removeText(final int offs, final int len, final boolean event) throws BadLocationException {
        final String str = getText(offs, len);
//        textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("annulation.AnnulerSuppression"), false);
        final JaxeUndoableEdit jedit = new JaxeUndoableEdit(
                JaxeUndoableEdit.SUPPRIMER, this, str, offs);
        jedit.doit();
        if (event) fireTextRemovedEvent(new JaxeEditEvent(this, offs, str));
//        textPane.finEditionSpeciale();
    }

    @Override
    public void remove(final int offs, final int len) throws BadLocationException {
        remove(offs, len, true);
    }

    /**
     * @param offs
     * @param len
     * @param event
     * @throws BadLocationException
     */
    public void remove(int offs, final int len, final boolean event) throws BadLocationException {
        if (textPane.getIgnorerEdition()) {
            super.remove(offs, len);
            return;
        }
        setModif(true);
        final JaxeElement firstel = rootJE.elementA(offs);
        final JaxeElement lastel = rootJE.elementA(offs + len - 1);
        if (firstel == lastel) {
            JaxeElement je = firstel;

            boolean avirer = false;
            if (je != null) {
            // si un JComponent est effacé, on efface tout le JaxeElement
                final ArrayList<Position> compos = je.getComponentPositions();
                for (final Position p : compos) {
                    final int cp = p.getOffset();
                    if (cp >= offs && cp < offs + len) {
                        avirer = true;
                        break;
                    }
                }
                // on efface aussi le JaxeElement s'il est entièrement dans la
                // sélection
                if (je.debut.getOffset() >= offs
                        && je.fin.getOffset() < offs + len) avirer = true;
                // ou si c'est un élément JESwing dont on efface le dernier
                // caractère
                if (je instanceof JESwing
                        && offs + len - 1 >= je.fin.getOffset()
                        && offs <= je.fin.getOffset()) {
                    while (je.getParent() != null
                            && je.getParent().fin.getOffset() == je.fin
                                    .getOffset())
                        je = je.getParent();
                    avirer = true;
                }
                if (avirer) {
                    if (!je.getEffacementAutorise()) { // SI c'est autorisé !
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    if (je instanceof JESwing) {
                        JaxeElement parent = je;
                        while (parent != null && parent instanceof JESwing)
                            parent = parent.getParent();
                        // on efface tout le parent si c'est aussi un JESwing
                        je = parent;
                    }
                }
            }
            if (avirer) {
//                textPane.debutEditionSpeciale(JaxeResourceBundle.getRB()
//                        .getString("annulation.Supprimer"), false);
                // effacer aussi le parent s'il est exactement à la même position
                if (je.getParent() != null &&
                        je.getParent().debut.getOffset() == je.debut.getOffset() &&
                        je.getParent().fin.getOffset() == je.fin.getOffset())
                    je = je.getParent();
                if (je.debut.getOffset() < offs)
                    offs = je.debut.getOffset()+1;
                final JaxeUndoableEdit e = new JaxeUndoableEdit(
                        JaxeUndoableEdit.SUPPRIMER, je);
                // on ne peut pas faire e.doit() tout de suite parce-que les
                // autres listeners doivent être invoqués avant la modif
                //SwingUtilities.invokeLater(new ChangeRunnable(e));
                // invoquer plus tard pause problème quand on veut faire un
                // insertString juste après: du coup il est fait avant...
                // finalement, ça a l'air de marcher avec e.doit(), alors on essaie...
                // maintenant ça plante avec la JVM d'Apple, on essaye de contourner le problème...
                appleBugWorkaround(je.debut.getOffset());
                e.doit();
                final JaxeEditEvent jee = new JaxeEditEvent(this, je);
                if (event) fireElementRemovedEvent(jee);
                if (jee.isConsumed()) textPane.setCaretPosition(offs);
//                textPane.finEditionSpeciale();
                textPane.miseAJourArbre();
            } else {
                /*if (je != null) {  retiré: fait dans JaxeUndoableEdit.effacer()
                    int finoff = je.fin.getOffset();
                    if (offs + len - 1 == finoff)
                        je.fin = createPosition(finoff - 1);
                }*/
                if (je instanceof JETexte || (je.debut.getOffset() == offs && !(je instanceof JESwing)))
                    je = je.getParent();
                if (!je.getEditionAutorisee()) {
                    gestionErreurs.texteInterdit(je);
                    return;
                }
                removeText(offs, len, event);
            }
        } else {
            //SwingUtilities.invokeLater(new SupRunnable(offs, len));
            // pour faire toutes les modifs (texte et élément) dans l'ordre, on est obligé de tout faire plus tard
            // tentative d'appel direct (c'est important pour ActionInsertionBalise,
            // qui doit insérer des éléments après en avoir supprimé)
            // question: sous quel environnement cela ne marche pas ?
            remove2(offs, len, event);
            textPane.miseAJourArbre();
        }
        _ignorer = false;
    }
    
    /**
     * Parfois un remove() provoque l'appel de DefaultCaret.setVisible() par apple.laf.AquaCaret dans le thread d'évènements.
     * Le résultat est une boucle infinie dans javax.swing.text.FlowView$FlowStrategy.layoutRow().
     * Cette bidouille déplace le curseur à l'avance.
     */
    private void appleBugWorkaround(final int dot) {
        if (System.getProperty("os.name").startsWith("Mac OS"))
            textPane.getCaret().setDot(dot);
    }
    
    public void remove2(final int offs, final int len, final boolean event) {
        appleBugWorkaround(offs);
        try {
            final JaxeElement firstel = rootJE.elementA(offs);
            final JaxeElement lastel = rootJE.elementA(offs + len - 1);
            final ArrayList<JaxeElement> l = rootJE.elementsDans(offs, offs + len - 1);
            if (l.size() == 1 && l.get(0) instanceof JESwing) {
                // cas des cellules de tableaux, on souhaite effacer tout l'intérieur mais pas la cellule
                final JaxeElement pswing = l.get(0);
                if (pswing.debut.getOffset() == offs && pswing.fin.getOffset() == offs+len &&
                        !pswing.getEffacementAutorise()) {
                    l.remove(0);
                    for (Node n = pswing.noeud.getFirstChild(); n != null; n = n.getNextSibling())
                        l.add(dom2JaxeElement.get(n));
                }
            }
            for (final JaxeElement je : l) {
                if (!_ignorer && !je.getEffacementAutorise() && (je.getParent() == null ||
                        !l.contains(je.getParent()) || !je.getParent().getEffacementAutorise())) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString(
                    "annulation.Supprimer"), true);
            final int lens2 = offs + len - lastel.debut.getOffset();
            if (firstel instanceof JETexte && l.indexOf(firstel) == -1) {
                String texte = firstel.noeud.getNodeValue();
                final int lt = texte.length();
                texte = texte.substring(0, offs - firstel.debut.getOffset());
                firstel.noeud.setNodeValue(texte);
                removeText(offs, lt - texte.length(), event);
            } else if (firstel instanceof JEStyle && l.indexOf(firstel) == -1) {
                final JaxeElement je = firstel.couper(textPane.getDocument().createPosition(offs));
                final JaxeUndoableEdit e = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, je);
                e.doit();
                if (event) fireElementRemovedEvent(new JaxeEditEvent(this, je));
            }
            for (final JaxeElement je : l) {
                // les textes peuvent être fusionnés et je.getParent devient null
                // -> utilisation de removeText
                if (je instanceof JETexte)
                    removeText(je.debut.getOffset(), je.fin.getOffset() - je.debut.getOffset() + 1, event);
                else if (je.getParent() != null) {
                    // option regrouper=false pour éviter les fusions avec JEStyle
                    final JaxeUndoableEdit e = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, je, false);
                    e.doit();
                    if (event) fireElementRemovedEvent(new JaxeEditEvent(this, je));
                }
            }
            if (lastel instanceof JETexte && l.indexOf(lastel) == -1) {
                String texte = lastel.noeud.getNodeValue();
                final int lt = texte.length();
                texte = texte.substring(lens2);
                lastel.noeud.setNodeValue(texte);
                removeText(lastel.debut.getOffset(), lt - texte.length(), event);
                if (firstel instanceof JETexte && l.indexOf(firstel) == -1) {
                    // rassembler les deux zones de texte
                    firstel.fusionner(lastel);
                }
            } else if (lastel instanceof JEStyle && l.indexOf(lastel) == -1) {
                final JaxeElement je = lastel.couper(textPane.getDocument().createPosition(lens2 + lastel.debut.getOffset()));
                final JaxeUndoableEdit e = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, lastel);
                e.doit();
                if (event) fireElementRemovedEvent(new JaxeEditEvent(this, lastel));
                if (firstel instanceof JEStyle && l.indexOf(firstel) == -1) {
                    // rassembler les deux zones de texte
                    firstel.fusionner(je);
                }
            }
            textPane.finEditionSpeciale();
        } catch (final BadLocationException ex) {
            LOG.error("remove2(int, int, boolean) - BadLocationException", ex);
        }
    }
    
    /**
     * Ignorer l'interdiction d'effacer des éléments. Utilisé par ActionInsertionBalise
     * quand l'utilisateur annule une insertion.
     */
    public void enableIgnore() {
        _ignorer = true;
    }
    
    /*
     * class ChangeRunnable implements Runnable { JaxeUndoableEdit edit; public
     * ChangeRunnable(JaxeUndoableEdit e) { this.edit = e; } public void run() {
     * edit.doit(); textPane.miseAJourArbre(); } }
     */
    @Override
    public void insertString(final int offset, final String str, final AttributeSet a)
    throws BadLocationException {
        insertString(offset, str, a, true);
    }

    /*class SupRunnable implements Runnable {
        int offs;
        int len;
        public SupRunnable(int offs, int len) {
            this.offs = offs;
            this.len = len;
        }
    public void run() {
            remove2(offs, len);
            textPane.miseAJourArbre();
        }
    }*/

    public void insertString(final int offset, String str, final AttributeSet a, final boolean event)
            throws BadLocationException {
        if (textPane.getIgnorerEdition()) {
            super.insertString(offset, str, a);
            return;
        }
        setModif(true);

        final int debut = textPane.getSelectionStart();
        final int fin = textPane.getSelectionEnd();
        /*
        Test retiré parce-qu'il n'y a plus d'appel à invokeLater dans remove.
        On peut maintenant faire des insertions quand il y a une sélection.
        Ca corrige un bug dans la correction d'orthographe (Jazzy faisait parfois des insertions
        avec insertString alors qu'il y avait une sélection)
        if (debut != fin) {
            // un appel à remove est généré automatiquement *après* l'appel à
            // insertString !
            // (probablement à cause du invokeLater dans remove)
            // on ne peut donc pas faire d'insertion quand il y a une
            // sélection...
            return;
        }
        */
        
        JaxeElement je = elementA(offset);
        if (je == null) return;
        if (je instanceof JETexte
                || (je.debut.getOffset() == offset && !(je instanceof JESwing)))
                je = je.getParent();

        if (cfg != null) {
            Element jeref;
            if (je == null || !(je.noeud instanceof Element))
                jeref = null;
            else
                jeref = je.refElement;
            if (jeref != null && ((!cfg.contientDuTexte(jeref) && !"".equals(str.trim())) ||
                    !je.getEditionAutorisee())) {
                gestionErreurs.texteInterdit(je);
                return;
            }
        }
        
        //super.insertString(offset, str, a);
        final Properties prefs = Preferences.getPref();
        final boolean consIndent = (prefs != null && "true".equals(prefs.getProperty("consIndent")));
        if (consIndent && newline.equals(str)) {
            // ajout d'un espace comme celui de la ligne précédente en début de ligne
            int i1 = offset - 255;
            if (i1 < 0)
                i1 = 0;
            String extrait = textPane.getText(i1, offset - i1);
            i1 = extrait.lastIndexOf('\n');
            if (i1 != -1) {
                extrait = extrait.substring(i1 + 1);
                for (i1 = 0; i1 < extrait.length()
                        && (extrait.charAt(i1) == ' ' || extrait.charAt(i1) == '\t'); i1++)
                    ;
                str += extrait.substring(0, i1);
            }
        }
//        if (event) textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString(
//        "annulation.AnnulerAjout"), false);
        final JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER,
            this, str, offset);
        jedit.doit();
        if (event) {
            fireTextAddedEvent(new JaxeEditEvent(this, offset, str));
//            textPane.finEditionSpeciale();
        }

    }
    
    /**
     * Mise à jour des indentations après une suppression de \n (appelé par JaxeUndoableEdit)
     */
    protected void majIndentSupp(final int offset) {
        final Properties prefs = Preferences.getPref();
        final boolean consIndent = (prefs != null && "true".equals(prefs.getProperty("consIndent")));
        if (consIndent)
            return;
        JaxeElement je = elementA(offset);
        if (je != null) {
            if (je instanceof JETexte)
                je = je.getParent();
            if (je.avecIndentation()) {
                if (je.fin.getOffset() == offset) {
                    final Style s = textPane.addStyle(null, null);
                    StyleConstants.setLeftIndent(s, (float)20.0*je.indentations());
                    setParagraphAttributes(offset, 1, s, false);
                    return;
                }
            }
        }
    }
    
    /**
     * Mise à jour des indentations après un ajout de \n (appelé par JaxeUndoableEdit)
     */
    protected void majIndentAjout(final int offset) {
        final Properties prefs = Preferences.getPref();
        final boolean consIndent = (prefs != null && "true".equals(prefs.getProperty("consIndent")));
        if (!consIndent) {
            JaxeElement je = elementA(offset-1);
            if (je != null) {
                if (je instanceof JETexte)
                    je = je.getParent();
                else if (!je.avecIndentation() && je.fin.getOffset() == offset - 1)
                    je = je.getParent();
                if (je.avecIndentation()) {
                    textPane.debutIgnorerEdition();
                    if (je.debut.getOffset() == offset-1 && je.fin.getOffset() > offset+1) {
                        final Style s = textPane.addStyle(null, null);
                        StyleConstants.setLeftIndent(s, (float)20.0*(je.indentations()+1));
                        setParagraphAttributes(offset+1, 1, s, false);
                    } else if (je.fin.getOffset()-1 == offset &&
                            getParagraphElement(offset).getStartOffset() > je.debut.getOffset()) {
                        final Style s = textPane.addStyle(null, null);
                        StyleConstants.setLeftIndent(s, (float)20.0*(je.indentations()+1));
                        setParagraphAttributes(offset, 1, s, false);
                    } else if (je.fin.getOffset() == offset-1 && je.getParent() != null &&
                            je.getParent().debut.getOffset() <
                            getParagraphElement(offset).getStartOffset()) {
                        final Style s = textPane.addStyle(null, null);
                        StyleConstants.setLeftIndent(s, (float)20.0*je.indentations());
                        setParagraphAttributes(offset-1, 1, s, false);
                    }
                    textPane.finIgnorerEdition();
                }
                je = elementA(offset+1);
                if (je != null) {
                    if (je instanceof JETexte)
                        je = je.getParent();
                    if (je.avecIndentation() && je.fin.getOffset() == offset+1) {
                        textPane.debutIgnorerEdition();
                        final Style s = textPane.addStyle(null, null);
                        StyleConstants.setLeftIndent(s, (float)20.0*je.indentations());
                        setParagraphAttributes(offset+1, 1, s, false);
                        textPane.finIgnorerEdition();
                    }
                }
            }
        }
    }

    /* ne marche pas :(
    public void myInsertStuff(javax.swing.text.AbstractDocument.DefaultDocumentEvent chng,
            AttributeSet attr, int off, String str) {
        writeLock();
        try {
            try {
                UndoableEdit u = getContent().insertString(off, str);
                DefaultDocumentEvent e = 
                    new DefaultDocumentEvent(off, str.length(), DocumentEvent.EventType.INSERT);
                if (u != null) {
                    chng.addEdit(u);
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
            //buffer.insert(off, str.length(), data, chng);
            super.insertUpdate(chng, attr);
            chng.end();
            fireInsertUpdate(chng);
        fireUndoableEditUpdate(new UndoableEditEvent(this, chng));
        } finally {
            writeUnlock();
        }
    }
    */
    
    public class SwingElementSpec {
        public String balise;
        public boolean branche;
        public String texte;
        int offset;
        public ArrayList<SwingElementSpec> enfants;
        SimpleAttributeSet att;

        public SwingElementSpec(final String balise) {
            this.balise = balise;
            branche = true;
            texte = null;
            enfants = new ArrayList<SwingElementSpec>();
            att = null;
        }

        public SwingElementSpec(final String balise, final SimpleAttributeSet att) {
            this.balise = balise;
            branche = true;
            texte = null;
            enfants = new ArrayList<SwingElementSpec>();
            this.att = att;
        }

        public SwingElementSpec(final String balise, final int offset, final String texte) {
            this.balise = balise;
            branche = false;
            this.offset = offset;
            this.texte = texte;
            enfants = null;
            att = null;
        }

        public void ajEnfant(final SwingElementSpec enfant) {
            enfants.add(enfant);
        }

        public ArrayList<ElementSpec> getElementSpecs() {
            final ArrayList<ElementSpec> specs = new ArrayList<ElementSpec>();
            if (!branche) {
                final SimpleAttributeSet attcontent = new SimpleAttributeSet();
                attcontent.addAttribute(AbstractDocument.ElementNameAttribute, "content");
                if (texte == null)
                    specs.add(new ElementSpec(attcontent,
                            ElementSpec.ContentType));
                else
                    specs.add(new ElementSpec(attcontent,
                            ElementSpec.ContentType, texte.toCharArray(),
                        offset, texte.length()));
            } else {
                SimpleAttributeSet att2 = new SimpleAttributeSet();
                if (att != null)
                    att2.addAttributes(att);
                att2.addAttribute(AbstractDocument.ElementNameAttribute, balise);
                specs.add(new ElementSpec(att2, ElementSpec.StartTagType));
                for (final SwingElementSpec enfant : enfants) {
                    specs.addAll(enfant.getElementSpecs());
                }
                if (att != null) {
                    att2 = new SimpleAttributeSet();
                    att2.addAttribute(AbstractDocument.ElementNameAttribute, balise);
                }
                specs.add(new ElementSpec(att2, ElementSpec.EndTagType));
            }
            return (specs);
        }

        public String getTexteArbre() {
            if (branche) {
                String atexte = "";
                for (final SwingElementSpec enfant : enfants) {
                    final String etexte = enfant.getTexteArbre();
                    if (etexte != null) atexte += etexte;
                }
                return (atexte);
            }
            return (texte);
        }
    }

    public SwingElementSpec prepareSpec(final String baliseSpec) {
        return (new SwingElementSpec(baliseSpec));
    }

    public SwingElementSpec prepareSpec(final String baliseSpec,
            final SimpleAttributeSet att) {
        return (new SwingElementSpec(baliseSpec, att));
    }

    public SwingElementSpec prepareSpec(final String baliseSpec, final int offset,
            final String texte) {
        return (new SwingElementSpec(baliseSpec, offset, texte));
    }

    public void sousSpec(final SwingElementSpec parentspec,
            final SwingElementSpec enfantspec) {
        parentspec.ajEnfant(enfantspec);
    }

    public javax.swing.text.Element insereSpec(final SwingElementSpec jspec,
            final int offset) {
        final ArrayList<ElementSpec> vspecs = jspec.getElementSpecs();
        ElementSpec[] es = new ElementSpec[vspecs.size()];
        es = vspecs.toArray(es);
        final String texte = jspec.getTexteArbre();

        writeLock();
        try {
            DefaultDocumentEvent evnt = null;
            try {
                final UndoableEdit cEdit = getContent().insertString(offset, texte);
                evnt = new DefaultDocumentEvent(offset, texte.length(),
                        DocumentEvent.EventType.INSERT);
                evnt.addEdit(cEdit);
            } catch (final BadLocationException ex) {
                LOG.error("insereSpec(SwingElementSpec, int)", ex);
            }
            buffer.insert(offset, texte.length(), es, evnt);
            // update bidi (possibly)
            //AbstractDocument.super.insertUpdate(evnt, null);
            // notify the listeners
            evnt.end();
            fireInsertUpdate(evnt);
            fireUndoableEditUpdate(new UndoableEditEvent(this, evnt));
        } finally {
            writeUnlock();
        }
        return (elementTexteA(jspec.balise, offset));
    }

    public javax.swing.text.Element elementTexteA(final String nom, final int offset) {
        BranchElement branche = (BranchElement) getDefaultRootElement();
        while (branche != null && branche.getStartOffset() != offset) {
            final javax.swing.text.Element el = branche.positionToElement(offset);
            if (el instanceof BranchElement)
                branche = (BranchElement) el;
            else
                branche = null;
        }
        return (branche);
    }

    public EditorKit createEditorKit() {
        return (new JaxeEditorKit());
    }

    class JaxeEditorKit extends StyledEditorKit {

        protected ViewFactory myViewFactory;

        public JaxeEditorKit() {
            super();
            myViewFactory = new JaxeViewFactory();
        }

        @Override
        public ViewFactory getViewFactory() {
            return (myViewFactory);
        }
    }

    class JaxeViewFactory implements ViewFactory {

        public View create(final javax.swing.text.Element elem) {
            final String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    //return new JaxeSpecialParagraph(elem);
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                } else if (kind.equals("table")) { return new JaxeTableView(
                        elem); }
            }

            // default to text display
            return new LabelView(elem);
        }
    }

    /*class JaxeSpecialParagraph extends ParagraphView {
        
        public JaxeSpecialParagraph(javax.swing.text.Element elem) {
            super(elem);
            setInsets((short)3, (short)3, (short)3, (short)3);
        }
        
        public void paint(Graphics g, Shape allocation) {
            super.paint(g, allocation);
            Rectangle alloc = (allocation instanceof Rectangle) ?
                       (Rectangle)allocation : allocation.getBounds();
            g.setColor(Color.red);
            g.drawRect(alloc.x, alloc.y, alloc.width-1, alloc.height-1);
            g.setColor(Color.black);
        }
    }*/
    
    /*class MyUndoableEditListener implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            if (e.getEdit() instanceof JaxeUndoableEdit) {
                ((JaxeUndoableEdit)(e.getEdit())).doit();
            }
        }
    }*/
    
    public void styleChanged() { // another bug fix (see Jaxe)
        styleChanged(null);
    }
    
    /*public void imageChanged(int offset) { // another UGLY (Windows/Linux) bug workaround
        // to force a ParagraphView update
        // problem 1: causes a ArrayIndexOutOfBoundsException with Sun's JVM on Linux
        // problem 2: moves the view to wherever the caret is
        textPane.debutIgnorerEdition();
        try {
            super.insertString(offset, "\n", null);
            super.remove(offset, 1);
        } catch (BadLocationException ex) {
            System.err.println("BadLocationException");
        }
        textPane.finIgnorerEdition();
    }*/
    
    public void imageChanged(final JComponent comp) { // yet another UGLY bug workaround
        final Container cont = comp.getParent();
        if (cont == null)
            return;
        if (cont.getLayout() == null)
            cont.setLayout(new OverlayLayout(cont));
        cont.validate();
    }

    /**
     * Returns the JaxeElement that represents the Node
     * @param node get the JaxeElement for this Node
     * @return The representation for the given Node
     */
    public JaxeElement getElementForNode(final Node node) {
        if (node == null)
            return null;
        return dom2JaxeElement.get(node);
    }
    
    /**
     * Adds a listener for editevents
     * @param edit Listener to add
     */
    public void addEditListener(final JaxeEditListenerIf edit) {
        _editListener.add(edit);
    }
    
    /**
     * Removes a listener for editevents
     * @param edit Listener to remove
     */
    public void removeEditListener(final JaxeEditListenerIf edit) {
        _editListener.remove(edit);
    }
    
    /**
     * Fires an event for removing text to all listeners
     * @param event Event to send
     */
    public void fireTextRemovedEvent(final JaxeEditEvent event) {
        for (final JaxeEditListenerIf l : _editListener) {
            l.textRemoved(event);
        }
    }
    
    /**
     * Fires an event for removing JaxeElements to all listeners
     * @param event Event to send
     */
    public void fireElementRemovedEvent(final JaxeEditEvent event) {
        for (final JaxeEditListenerIf l : _editListener) {
            l.elementRemoved(event);
        }
    }
    
    /**
     * Fires an event for adding text to all listeners
     * @param event Event to send
     */
    public void fireTextAddedEvent(final JaxeEditEvent event) {
        for (final JaxeEditListenerIf l : _editListener) {
            l.textAdded(event);
        }
    }
    
    /**
     * Fires an event for adding JaxeElements to all listeners an returns a possible new insert position
     * @param event Event to send
     * @param pos Position element will be added
     * @return New position of insert
     */
    public Position fireElementAddedEvent(final JaxeEditEvent event, Position pos) {
        for (final JaxeEditListenerIf l : _editListener) {
            pos = l.elementAdded(event, pos);
        }
        return pos;
    }

    /**
     * Fires an event to prepare the position a JaxeElement will be added and returns a possible
     * new instert position
     * @param pos Position to prepare
     * @return New position of insert
     */
    public Position firePrepareElementAddEvent(Position pos) {
        for (final JaxeEditListenerIf l : _editListener) {
            pos = l.prepareAddedElement(pos);
        }
        return pos;
    }
}
