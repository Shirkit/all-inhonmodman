/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
4 regular expression matching libraries have been tested.
code is left as comment since it could be reused
- jakarta-regexp (1.2) quickly gets StackOverflowException, and generates
    RESyntaxException: Syntax error: Closure operand can't be nullable
- jakarta-oro-awk (2.0.8) is the fastest but a bit big; limited to awk regular expressions;
    limited to 8-bit ASCII
- gnu.regexp (1.1.4) is a bit slow
- java.util.regex is included in the JDK 1.4+ but throws StackOverflowError when possessive quantifiers are not used
*/

//jakarta-regexp
//import org.apache.regexp.RE;
//import org.apache.regexp.RESyntaxException;

//jakarta-oro
//import org.apache.oro.text.regex.*;
//import org.apache.oro.text.awk.*;

//gnu.regexp
//import gnu.regexp.*;

/**
 * Gestion du fichier de configuration et du fichier de schéma XML
 */
public class Config {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(Config.class);

    private static final String newline = Jaxe.newline;
    private static final String typeAffichageParDefaut = "string";
    
    protected Element jaxecfg; // élément racine du fichier de config
    private boolean ancienneConfig; // true si le fichier utilise l'ancienne syntaxe
    
    protected HashMap<File, Map<String, String>> fichierXSL2Parametres ;
    public URL schemaURL; // URL du fichier du schéma
    
    private URL cfgdir; // URL du dossier config (dans lequel doit se trouver le fichier de config)
    private String namespacecfg; // espace de noms souhaité pour les documents générés
    private String prefixecfg; // préfixe correspondant à namespacecfg
    
    private HashMap<String, Element> cacheAffichageElements; // cache des associations nom -> AFFICHAGE_ELEMENT
    private HashMap<Element, String> cacheElementsVersNoms; // cache des associations référence élément -> nom
    private HashMap<Element, String> cacheTitresElements; // cache des associations référence élément -> titre
    private HashMap<Element, Pattern> cacheInsertion = null; // cache des expressions régulières pour les insertions
    private HashMap<Element, Pattern> validePatternCache = null;
    private HashMap<Element, HashMap<String, ArrayList<String>>> cacheParametres = null;
    private HashMap<Element, VerifTypeSimple> hashVerif = null; // associations éléments de définition d'éléments ou d'attributs -> VerifTypeSimple
    private ArrayList<String> cacheListeEspace = null; // liste d'espaces de noms, y compris dans les sous-configs
    
    private ArrayList<Config> autresConfigs;
    
    // jakarta-oro
//    PatternCompiler compiler;
//    PatternMatcher matcher;

    private ResourceBundle resourceTitres; // titres, pour les vieilles configs uniquement
    
    private InterfaceSchema schema; // toute la gestion du schéma (validité, ...)
    
    // noeuds des éléments principaux du fichier de config
    private Element noeudLangage;
    private Element noeudEnregistrement;
    private Element noeudMenus;
    private Element noeudAffichage;
    private Element noeudExports;
    private List<Element> listeStrings;

    
    // CONSTRUCTEURS ET INITIALISATION
    
    /**
     * Constructeur à partir d'un chemin de fichier de configuration sur le disque
     *
     * @param nomFichierCfg  chemin vers le fichier sur le disque
     * @param lireSchema  faire un chargement en mémoire du schéma (false uniquement pour chercher une Config
     *     en fonction de la racine d'un document)
     */
    public Config(final String nomFichierCfg, final boolean lireSchema) throws JaxeException {
        try {
            initialisation(new File(nomFichierCfg).toURI().toURL(), lireSchema);
        } catch (final MalformedURLException ex) {
            LOG.error("Config("+nomFichierCfg+","+lireSchema+") : MalformedURLException: " + ex.getMessage(), ex);
            throw new JaxeException("Erreur à la construction de l'URL pour " + nomFichierCfg, ex);
        }
    }
    
    /**
     * Constructeur à partir d'une URL (le fichier peut être sur le réseau ou sur un disque local)
     *
     * @param urlFichierCfg  URL du fichier de configuration
     * @param lireSchema  faire un chargement en mémoire du schéma (false uniquement pour chercher une Config
     *     en fonction de la racine d'un document)
     */
    public Config(final URL urlFichierCfg, final boolean lireSchema) throws JaxeException {
        initialisation(urlFichierCfg, lireSchema);
    }
    
    private void initialisation(final URL urlFichierCfg, final boolean lireSchema) throws JaxeException {
        if (urlFichierCfg == null) {
            jaxecfg = null;
            return;
        }
        fichierXSL2Parametres = new HashMap<File, Map<String, String>>() ;
        
        // jakarta-oro
//        compiler = new AwkCompiler();
//        matcher = new AwkMatcher();
        
        Document configdoc;
        try {
            final DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final String furl = urlFichierCfg.toExternalForm();
            configdoc = parser.parse(furl);
        } catch (final Exception ex) {
            LOG.error("Config: initialisation: lecture de " + urlFichierCfg.toExternalForm(), ex);
            throw new JaxeException("Erreur à la lecture de " + urlFichierCfg.toExternalForm(), ex);
        }
        
        final String resource;
        if ("CONFIG_JAXE".equals(configdoc.getDocumentElement().getNodeName()))
            resource = null;
        else
            resource = getResource(configdoc.getDocumentElement());
        
        cfgdir = getParentURL(urlFichierCfg);
        
        configdoc = transformationConfig(configdoc);
        if (configdoc == null) {
            LOG.error("Config: initialisation: erreur à la transformation du fichier de config "  + urlFichierCfg.toExternalForm());
            throw new JaxeException("Erreur à la transformation du fichier de config " + urlFichierCfg.getFile());
        }
        
        jaxecfg = configdoc.getDocumentElement();
        
        autresConfigs = new ArrayList<Config>();
        Element elconfig = findElement(getLangage(), "AUTRE_CONFIG");
        while (elconfig != null) {
            URL urlAutreConfig;
            try {
                if (cfgdir == null)
                    urlAutreConfig = new URL(elconfig.getAttribute("nom"));
                else
                    urlAutreConfig = new URL(cfgdir.toExternalForm() + "/" + elconfig.getAttribute("nom"));
            } catch (final MalformedURLException ex) {
                LOG.error("Config: initialisation: MalformedURLException: " + ex.getMessage(), ex);
                urlAutreConfig = null;
            }
            try {
                final Config autreConfig = new Config(urlAutreConfig, true);
                autresConfigs.add(autreConfig);
            } catch (JaxeException ex) {
            }
            elconfig = nextElement(elconfig, "AUTRE_CONFIG");
        }
        
        construireCacheAffichageElements();
        namespacecfg = chercherNamespace();
        prefixecfg = chercherPrefixe();
        
        // Getting the bundle according to locale for resolving labels
        if (!ancienneConfig || resource == null)
            resourceTitres = null;
        else
            resourceTitres = ResourceBundle.getBundle(resource);
        cacheTitresElements = new HashMap<Element, String>();
        
        if (!lireSchema) {
            schema = null;
            schemaURL = null;
            return;
        }
        
        final String noms = nomSchema();
        if (noms == null) {
            final Element schema_simple = findElement(getLangage(), "SCHEMA_SIMPLE");
            if (schema_simple == null) {
                LOG.error("Config: initialisation: Aucun schéma XML n'est défini dans le fichier de config " + urlFichierCfg.toExternalForm());
                throw new JaxeException("Erreur : aucun schéma XML n'est défini dans le fichier de config " + urlFichierCfg.getFile());
            }
            schema = new SchemaSimple(schema_simple, this);
            schemaURL = null;
            construireCacheRefElements();
            return;
        }
        
        try {
            if (cfgdir != null)
                schemaURL = new URL(cfgdir.toExternalForm() + "/" + noms);
            else
                schemaURL = new URL(noms);
        } catch (final MalformedURLException ex) {
            LOG.error("Config: initialisation: MalformedURLException: " + ex.getMessage());
        }
        if (noms.endsWith(".rng"))
            schema = new SchemaRelaxNG(schemaURL, this);
        else
            schema = new SchemaW3C(schemaURL, this);
        construireCacheRefElements();
    }
    
    /**
     * Transformation XSLT depuis l'ancienne version des fichiers de config,
     * avec conversion_config.xsl
     */
    private Document transformationConfig(final Document docCfg) {
        if ("CONFIG_JAXE".equals(docCfg.getDocumentElement().getNodeName())) {
            ancienneConfig = false;
            return(docCfg);
        }
        ancienneConfig = true;
        try {
            final TransformerFactory tFactory = TransformerFactory.newInstance();
            URL urlConversionXSL;
            if (cfgdir == null)
                urlConversionXSL = new URL("conversion_config.xsl");
            else
                urlConversionXSL = new URL(cfgdir.toExternalForm() + "/" + "conversion_config.xsl");
            final InputStream xslStream = urlConversionXSL.openStream();
            final Transformer transformer = tFactory.newTransformer(new StreamSource(xslStream));
            final DOMSource source = new DOMSource(docCfg);
            final DOMResult result = new DOMResult();
            transformer.transform(source, result);
            return((Document)result.getNode());
        } catch (final Exception ex) {
            LOG.error("Config.transformationConfig: " + ex.getMessage(), ex);
            return(null);
        }
    }
    
    /**
     * Renvoie l'url du répertoire parent du fichier ou répertoire correspondant à l'URL donnée,
     * ou null si l'on ne peut pas déterminer le répertoire parent.
     */
    private static URL getParentURL(final URL u) {
        final int index = u.toExternalForm().lastIndexOf("/");
        if (index >= 0) {
            try {
                return(new URL(u.toExternalForm().substring(0, index)));
            } catch (final MalformedURLException ex) {
                LOG.error("getParentURL(" + u + ") : MalformedURLException", ex);
                return(null);
            }
        }
        return(null);
    }
    
    
    // METHODES LIEES AU FICHIER DE CONFIG
    
    /**
     * Renvoie la définition de l'élément racine dans le fichier de config
     * (premier élément BALISE sous le premier élément RACINE du fichier de config)
     * @deprecated remplacé par premierElementRacine
     */
    @Deprecated
    public Element racine() {
        final String nom = nomPremierElementRacine();
        if (nom == null)
            return(null);
        return(referenceElement(nom));
    }
    
    /**
     * Renvoie le nom du premier élément utilisable comme racine, ou null si aucun n'est défini.
     * Cette méthode peut être utilisée quand le schéma n'est pas chargé.
     */
    public String nomPremierElementRacine() {
        final Element racine = findElement(getLangage(), "RACINE");
        if (racine == null)
            return(null);
        return(racine.getAttribute("element"));
    }
    
    /**
     * Renvoie la référence du premier élément utilisable comme racine, ou null si aucun n'est défini.
     */
    public Element premierElementRacine() {
        final String nom = nomPremierElementRacine();
        return(schema.referenceElement(nom));
    }
    
    /**
     * Retourne la liste des noms des éléments racines possibles
     */
    public ArrayList<String> listeRacines() {
        final ArrayList<String> liste = new ArrayList<String>();
        Element racine = findElement(getLangage(), "RACINE");
        while (racine != null) {
            liste.add(racine.getAttribute("element"));
            racine = nextElement(racine, "RACINE");
        }
        return(liste);
    }
    
    /**
     * Retourne la liste des références des éléments racines possibles
     */
    public ArrayList<Element> listeElementsRacines() {
        final ArrayList<Element> liste = new ArrayList<Element>();
        Element racine = findElement(getLangage(), "RACINE");
        while (racine != null) {
            final Element ref = referenceElement(racine.getAttribute("element"));
            if (ref != null)
                liste.add(ref);
            racine = nextElement(racine, "RACINE");
        }
        return(liste);
    }
    
    /**
     * Recherche l'espace de noms dans le fichier de config, renvoie null s'il n'y en a pas.
     * @deprecated
     */
    @Deprecated
    protected String chercherNamespace() {
        final Element espace = findElement(getEnregistrement(), "PREFIXE_ESPACE");
        if (espace == null)
            return(null);
        String uri = espace.getAttribute("uri");
        if ("".equals(uri))
            uri = null;
        return(uri);
    }
    
    /**
     * Renvoie l'espace de nom donné dans le fichier de config, ou null si aucun n'est défini
     * @deprecated
     */
    @Deprecated
    public String namespace() {
        return(namespacecfg);
    }
    
    @Deprecated
    protected String chercherPrefixe() {
        final Element espace = findElement(getEnregistrement(), "PREFIXE_ESPACE");
        if (espace == null)
            return(null);
        String pref = espace.getAttribute("prefixe");
        if ("".equals(pref))
            pref = null;
        return(pref);
    }
    
    /**
     * Renvoie le préfixe donné dans le fichier de config, ou null si aucun n'est défini
     */
    @Deprecated
    public String prefixe() {
        return(prefixecfg);
    }
    
    /**
     * Ajoute les attributs pour les espaces de nom à l'élément racine
     */
    public void ajouterAttributsEspaces(final Element rootel) {
        final ArrayList<String> espaces = listeEspaces();
        for (final String espace : espaces) {
            if (!"".equals(espace)) {
                final String prefixe = prefixeEspace(espace);
                String nomatt = "xmlns";
                if (prefixe != null && !"".equals(prefixe))
                    nomatt += ":" + prefixe;
                rootel.setAttributeNS("http://www.w3.org/2000/xmlns/", nomatt, espace);
            }
        }
        final String schemaLocation = getSchemaLocation();
        final String noNamespaceSchemaLocation = getNoNamespaceSchemaLocation();
        if (schemaLocation != null || noNamespaceSchemaLocation != null) {
            rootel.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            if (schemaLocation != null)
                rootel.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:schemaLocation", schemaLocation);
            if (noNamespaceSchemaLocation != null)
                rootel.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:noNamespaceSchemaLocation", noNamespaceSchemaLocation);
        }
    }
    
    /**
     * Renvoie le nom du fichier du schéma tel que donné dans le fichier de config
     * (attribut nom de l'élément FICHIER_SCHEMA du fichier de config).
     * Renvoie null si aucun n'est défini.
     */
    public String nomSchema() {
        final Element fichierschema = findElement(getLangage(), "FICHIER_SCHEMA");
        if (fichierschema == null)
            return(null);
        String nom = fichierschema.getAttribute("nom");
        if ("".equals(nom))
            nom = null;
        return(nom);
    }
    
    /**
     * Renvoie la table hash par nom des définitions des éléments dans le fichier de config
     * (éléments BALISE)
     */
    protected HashMap<String, Element> construireCacheAffichageElements() {
        cacheAffichageElements = new HashMap<String, Element>();
        if (jaxecfg == null)
            return(cacheAffichageElements);
        Element affel = findElement(getAffichageNoeuds(), "AFFICHAGE_ELEMENT");
        while (affel != null) {
            final String nom = affel.getAttribute("element");
            cacheAffichageElements.put(nom, affel);
            affel = nextElement(affel, "AFFICHAGE_ELEMENT");
        }
        return(cacheAffichageElements);
    }
    
    protected HashMap<Element, String> getCacheElementsVersNoms() {
        return(cacheElementsVersNoms);
    }
    
    public Element getAffichageElement(String nom) {
        return cacheAffichageElements.get(nom);
    }
    
    /**
     * Construit la table hash des associations références dans le schéma -> nom des éléments
     * pour cette config et les autres configs
     */
    protected void construireCacheRefElements() {
        cacheElementsVersNoms = new HashMap<Element, String>();
        if (jaxecfg == null)
            return;
        final ArrayList<Element> elements = schema.listeTousElements();
        for (final Element ref : elements) {
            final String nom = schema.nomElement(ref);
            if (nom != null)
                cacheElementsVersNoms.put(ref, nom);
        }
        for (final Config conf : autresConfigs)
            cacheElementsVersNoms.putAll(conf.getCacheElementsVersNoms());
    }
    
    /**
     * Return the name of the resource bundle to use.
     *
     * @return the name of the resource bundle, null if not defined.
     */
    protected String getResource(final Element racine) {
        final Element bundle = findElement(racine, "FICHIERTITRES");
        if (bundle == null)
            return(null);
        return(bundle.getAttribute("nom"));
    }
    
    /**
     * Renvoie le nom d'un élément à partir de sa définition dans le fichier de config
     * (attribut nom de l'élément BALISE)
     * @deprecated remplacé par nomElement
     */
    @Deprecated
    public String nomBalise(final Element balisedef) {
        LOG.error("Config.nomBalise utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(nomElement(balisedef));
    }
    
    /**
     * Renvoie le type d'un élément à partir de sa définition dans le fichier de config
     * (attribut type de l'élément BALISE)
     * @deprecated remplacé par typeAffichageElement et typeAffichageNoeud
     */
    @Deprecated
    public String typeBalise(final Element balisedef) {
        LOG.error("Config.typeBalise utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(typeAffichageNoeud(balisedef, balisedef.getAttribute("nom"), balisedef.getAttribute("noeudtype")));
    }
    
    /**
     * Renvoie la définition du premier élément du fichier de config dont le nom et le type sont ceux indiqués
     * @deprecated
     */
    @Deprecated
    public Element getBaliseNomType(final String nombalise, final String typebalise) {
        if (jaxecfg == null)
            return(null);
        final String nomlocal = localValue(nombalise);
        Element affel = findElement(getAffichageNoeuds(), "AFFICHAGE_ELEMENT");
        while (affel != null) {
            if (nomlocal.equals(affel.getAttribute("element")) && typebalise.equals(affel.getAttribute("type")))
                return(referenceElement(nombalise));
            affel = nextElement(affel, "AFFICHAGE_ELEMENT");
        }
        return(null);
    }
    
    /**
     * Renvoie la définition du premier élément du fichier de config dont le type est celui indiqué
     * @deprecated remplacé par premierElementAvecType
     */
    @Deprecated
    public Element getBaliseAvecType(final String typebalise) {
        if (jaxecfg == null)
            return(null);
        Element affel = findElement(getAffichageNoeuds(), "AFFICHAGE_ELEMENT");
        while (affel != null) {
            if (typebalise.equals(affel.getAttribute("type")))
                return(referenceElement(affel.getAttribute("element")));
            affel = nextElement(affel, "AFFICHAGE_ELEMENT");
        }
        return(null);
    }
    
    /**
     * Renvoie "instruction" si l'élément est une processing instruction
     * (attribut noeudtype de l'élément BALISE)
     * @deprecated
     */
    @Deprecated
    public String noeudtypeBalise(final Element balisedef) {
        return("element");
    }
    
    /**
     * Renvoie le titre d'un élément à partir de sa définition dans le fichier de config
     * (attribut titre de l'élément BALISE, ou attribut nom s'il n'y a pas d'attribut titre)
     * @deprecated remplacé par titreElement
     */
    @Deprecated
    public String titreBalise(final Element balisedef) {
        LOG.error("Config.titreBalise utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(titreElement(balisedef));
    }
    
    /**
     * Renvoie la définition du premier élément du fichier de config dont le nom est celui indiqué.
     * Attention: à n'utiliser que si on est sûr que l'élément est définit dans cette configuration.
     * @deprecated
     */
    @Deprecated
    public Element getBaliseDef(final String nombalise) {
        return(referenceElement(nombalise));
    }
    
    /**
     * Renvoie la définition du premier élément du fichier de config correspondant,
     * en regardant dans les autres configurations si nécessaire.
     * @deprecated
     */
    @Deprecated
    public Element getElementDef(final Element el) {
        final Config conf = getElementConf(el);
        if (conf == this) {
            String nom;
            if (el.getPrefix() == null)
                nom = el.getNodeName();
            else
                nom = el.getLocalName();
            return(referenceElement(nom));
        } else if (conf != null)
            return(conf.getElementDef(el));
        else
            return(null);
    }
    
    /**
     * Renvoie la définition de la première processing instruction du fichier de config,
     * en regardant dans les autres configurations si nécessaire.
     * @deprecated
     */
    @Deprecated
    public Element getProcessingDef(final ProcessingInstruction el) {
        return(null);
    }
    
    /**
     * Renvoie la config correspondant à un nom d'élément.
     * Attention: peut être ambiguë si le nom n'a pas de préfixe.
     * Il est donc préférable d'utiliser getDefConf et getElementConf à la place.
     *
     * @deprecated     Utiliser de préférence getRefConf et getElementConf à la place
     */
    @Deprecated
    public Config getBaliseConf(String nombalise) {
        if (autresConfigs.size() == 0)
            return(this);
        final int inds = nombalise.indexOf(':');
        if (inds != -1) {
            final String prefixe = nombalise.substring(0, inds);
            for (final Config conf : autresConfigs) {
                if (prefixe.equals(conf.prefixe()))
                    return(conf);
            }
            nombalise = nombalise.substring(inds+1);
        }
        if (schema.referenceElement(nombalise) != null)
            return(this);
        for (final Config conf : autresConfigs) {
            if (conf.getSchema().referenceElement(nombalise) != null)
                return(conf);
        }
        LOG.error("getBaliseConf(String) - erreur: config introuvable pour " + nombalise);
        return(null);
    }
    
    /**
     * Renvoie la config correspondant à une définition d'élément du fichier de config.
     * @deprecated
     */
    @Deprecated
    public Config getDefConf(final Element defbalise) {
        LOG.error("Config.getDefConf utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(getRefConf(defbalise));
    }
    
    /**
     * Renvoie la config correspondant à une définition d'élément du schéma.
     */
    public Config getRefConf(final Element refElement) {
        if (schema.elementDansSchema(refElement))
            return(this);
        for (final Config conf : autresConfigs) {
            if (conf.elementDansSchema(refElement))
                return(conf);
        }
        LOG.error("getRefConf(Element) - attention: pas de config trouvée pour " + refElement);
        return(null);
    }
    
    /**
     * Renvoie la config correspondant à un élément du document XML.
     */
    public Config getElementConf(final Element el) {
        final String ns = el.getNamespaceURI();
        if (aEspace(ns))
            return(this);
        for (final Config conf : autresConfigs) {
            if (conf.aEspace(ns))
                return(conf);
        }
        LOG.error("getElementConf(Element) - attention: pas de config trouvée pour " + el.getNodeName());
        LOG.error("getElementConf(Element) - espace élément: " + ns);
        //LOG.error("getElementConf(Element) - espace cible de la config: " + targetNamespace);
        return(null);
    }

    /**
     * Renvoie la config correspondant à une processing instruction du document XML.
     */
    public Config getProcessingConf(final ProcessingInstruction el) {
        final String ns = el.getNamespaceURI();
        if (aEspace(ns))
            return(this);
        for (final Config conf : autresConfigs) {
            if (conf.aEspace(ns))
                return(conf);
        }
        LOG.error("getProcessingConf(ProcessingInstruction) - attention: pas de config trouvée pour "
                + el.getNodeName());
        LOG.error("getProcessingConf(ProcessingInstruction) - espace élément: " + ns);
        //LOG.error("getProcessingConf(ProcessingInstruction) - espace cible de la config: " + targetNamespace);
        return(null);
    }
    
    /**
     * Renvoie les fichiers XSL attachés au premier export HTML de cette config,
     * et construit la table de hash des paramètres de ces fichiers.
     * Renvoie null si aucun export HTML est trouvé.
     * @deprecated utiliser listeExports et listeFichiersExport à la place
     */
    @Deprecated
    public File[] getXSLFiles() {
        if (jaxecfg == null)
            return(null);
        final ArrayList<Element> exports = listeExports("HTML");
        if (exports == null || exports.size() == 0)
            return(null);
        final Element premierExportHTML = exports.get(0);
        final ArrayList<File> listeFichiers = listeFichiersExport(premierExportHTML);
        return(listeFichiers.toArray(new File[listeFichiers.size()]));
    }
    
    public Map<String, String> getXSLParam(final File xslFile) {
        return fichierXSL2Parametres.get(xslFile) ;
    }
    
    /**
     * Renvoie la liste des références des exports, en fonction de la sortie (HTML ou XML)
     */
    public ArrayList<Element> listeExports(final String sortie) {
        if (jaxecfg == null)
            return(null);
        final ArrayList<Element> liste = new ArrayList<Element>();
        Element export = findElement(getExports(), "EXPORT");
        while (export != null) {
            if (sortie.equals(export.getAttribute("sortie")))
                liste.add(export);
            export = nextElement(export, "EXPORT");
        }
        return(liste);
    }
    
    /**
     * Renvoie le nom d'un export à partir de sa référence
     */
    public String nomExport(final Element export) {
        return(export.getAttribute("nom"));
    }
    
    /**
     * Renvoie la sortie d'un export à partir de sa référence
     */
    public String sortieExport(final Element export) {
        return(export.getAttribute("sortie"));
    }
    
    /**
     * Renvoie la liste des fichiers XSL d'un export à partir de sa référence,
     * et construit la table de hash des paramètres de ces fichiers.
     */
    public ArrayList<File> listeFichiersExport(final Element export) {
        final ArrayList<File> liste = new ArrayList<File>();
        Element xslel = findElement(export, "FICHIER_XSL");
        while (xslel != null) {
            final Map<String, String> parametres = new HashMap<String, String>();
            final String nom = xslel.getAttribute("nom");
            File f;
            if (nom.startsWith("/"))
                f = new File(nom);
            else {
                try {
                    f = new File(new URL(cfgdir + "/" + nom).toURI());
                } catch (final MalformedURLException e) {
                    LOG.error("Malformed URL", e);
                    f = null;
                } catch (final URISyntaxException e) {
                    LOG.error("URI Syntaxexeption", e);
                    f = null;
                }
            }
            if (f != null) {
                liste.add(f);
                Element parametre = findElement(xslel, "PARAMETRE");
                while (parametre != null) {
                    final String nomparam = parametre.getAttribute("nom") ;
                    final String valeurparam = parametre.getAttribute("valeur") ;
                    parametres.put(nomparam, valeurparam) ;
                    parametre = nextElement(parametre, "PARAMETRE");
                }
                fichierXSL2Parametres.put(f, parametres) ;
            }
            xslel = nextElement(xslel, "FICHIER_XSL");
        }
        return(liste);
    }
    
    /**
     * Renvoie l'encodage de caractères souhaité pour les documents XML
     */
    public String getEncodage() {
        final Element encodage = findElement(getEnregistrement(), "ENCODAGE");
        if (encodage == null)
            return(null);
        return(dom_valeurElement(encodage));
    }
    
    public String getPublicId() {
        final Element doctype = findElement(getEnregistrement(), "DOCTYPE");
        if (doctype != null)
            return doctype.getAttribute("publicId");
        
        return(null);
    }
    
    public String getSystemId() {
        final Element doctype = findElement(getEnregistrement(), "DOCTYPE");
        if (doctype != null)
            return doctype.getAttribute("systemId");
        return(null);
    }
    
    public String getSchemaLocation() {
        final Element sl = findElement(getEnregistrement(), "SCHEMALOCATION");
        if (sl != null) {
            final String schemaLocation = sl.getAttribute("schemaLocation");
            if (!"".equals(schemaLocation))
                return(schemaLocation);
        }
        return(null);
    }
    
    public String getNoNamespaceSchemaLocation() {
        final Element sl = findElement(getEnregistrement(), "SCHEMALOCATION");
        if (sl != null) {
            final String noNamespaceSchemaLocation = sl.getAttribute("noNamespaceSchemaLocation");
            if (!"".equals(noNamespaceSchemaLocation))
                return(noNamespaceSchemaLocation);
        }
        return(null);
    }
    
    /**
     * Renvoie un préfixe à utiliser pour l'espace de noms donné, ou null si aucune suggestion n'est possible
     */
    public String prefixeEspace(final String ns) {
        if ("http://www.w3.org/XML/1998/namespace".equals(ns))
            return("xml");
        Element pe = findElement(getEnregistrement(), "PREFIXE_ESPACE");
        while (pe != null) {
            if (ns.equals(pe.getAttribute("uri")))
                return(pe.getAttribute("prefixe"));
            pe = nextElement(pe, "PREFIXE_ESPACE");
        }
        return(schema.prefixeEspace(ns));
    }
    
    /**
     * Renvoie true si le fichier de config demande à ce que les titres des menus soient utilisés
     * pour afficher les balises au lieu des noms des éléments (false par défaut).
     * @deprecated utiliser titreElement ou titreMenu, suivant les cas.
     */
    @Deprecated
    public boolean affichageTitres() {
        return(false);
    }
    
    /**
     * Renvoie une ligne de description de la config, "racine - description"
     * sans que le fichier ne soit chargé en mémoire et analysé.
     * Utilisé dans DialogueNouveau pour afficher une description de chaque config.
     */
    public static String descriptionDialogueNouveau(final URL urlFichierCfg) {
        try {
            final SAXParserFactory usine = SAXParserFactory.newInstance();
            final SAXParser parser = usine.newSAXParser();
            final ConfigSAXHandler handler = new ConfigSAXHandler();
            parser.parse(urlFichierCfg.toString(), handler);
            return(handler.getDescription());
        } catch (final Exception ex) {
            LOG.error(ex);
            return(null);
        }
    }
    
    /**
     * Renvoie les noms des éléments racine possibles pour la config
     * sans que le fichier ne soit chargé en mémoire et analysé.
     * Utilisé dans JaxeDocument.chercherConfig().
     */
    public static ArrayList<String> nomsElementsRacine(final URL urlFichierCfg) {
        try {
            final SAXParserFactory usine = SAXParserFactory.newInstance();
            final SAXParser parser = usine.newSAXParser();
            final ConfigSAXHandler handler = new ConfigSAXHandler();
            parser.parse(urlFichierCfg.toString(), handler);
            return(handler.getNomsRacines());
        } catch (final Exception ex) {
            LOG.error(ex);
            return(null);
        }
    }
    
    static class ConfigSAXHandler extends DefaultHandler {
        boolean dansDescription, dansRacine;
        StringBuffer description;
        ArrayList<String> nomsRacines;
        String fichiertitres;
        String langue_strings, pays_strings;
        String langue_desc, pays_desc;
        String langue_pref, pays_pref;
        public ConfigSAXHandler() {
            final Locale defaut = Locale.getDefault();
            langue_pref = defaut.getLanguage();
            pays_pref = defaut.getCountry();
        }
        public void startDocument() throws SAXException {
            dansDescription = false;
            dansRacine = false;
            description = null;
            nomsRacines = new ArrayList<String>();
        }
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            if (dansDescription)
                description.append(ch, start, length);
        }
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            if ("STRINGS".equals(qName)) {
                langue_strings = attributes.getValue("langue");
                pays_strings = attributes.getValue("pays");
            } else if ("DESCRIPTION".equals(qName) || "DESCRIPTION_CONFIG".equals(qName)) {
                boolean meilleure_locale = false;
                if (description != null && "DESCRIPTION_CONFIG".equals(qName)) {
                    if (langue_strings != null && pays_strings != null &&
                            langue_strings.equals(langue_pref) && pays_strings.equals(pays_pref))
                        meilleure_locale = true;
                    else if ((langue_desc == null || !langue_desc.equals(langue_pref)) &&
                            langue_strings != null && langue_strings.equals(langue_pref))
                        meilleure_locale = true;
                }
                if (description == null || "DESCRIPTION".equals(qName) || meilleure_locale) {
                    dansDescription = true;
                    description = new StringBuffer();
                    langue_desc = langue_strings;
                    pays_desc = pays_strings;
                }
            } else if ("RACINE".equals(qName)) {
                dansRacine = true;
                final String attelement = attributes.getValue("element");
                if (attelement != null && !"".equals(attelement))
                    nomsRacines.add(attelement);
            } else if (dansRacine && "BALISE".equals(qName)) {
                final String attnom = attributes.getValue("nom");
                if (attnom != null && !"".equals(attnom))
                    nomsRacines.add(attnom);
            } else if ("FICHIERTITRES".equals(qName)) {
                fichiertitres = attributes.getValue("nom");
                final ResourceBundle resourceTitres = ResourceBundle.getBundle(fichiertitres);
                try {
                    description = new StringBuffer(resourceTitres.getString("description_config"));
                } catch (final MissingResourceException ex) {
                }
            }
        }
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if ("DESCRIPTION".equals(qName) || "DESCRIPTION_CONFIG".equals(qName))
                dansDescription = false;
            if ("RACINE".equals(qName))
                dansRacine = false;
        }
        public String getDescription() {
            if (description != null && nomsRacines.size() > 1)
                return(description.toString());
            else if (description != null && nomsRacines.size() == 1)
                return(nomsRacines.get(0) + " - " + description.toString());
            else if (description != null)
                return(description.toString());
            else if (nomsRacines.size() > 0)
                return(nomsRacines.get(0));
            else
                return(null);
        }
        public ArrayList<String> getNomsRacines() {
            return(nomsRacines);
        }
    }
    
    
    // METHODES POUR LES MENUS D'INSERTION DES ELEMENTS
    
    /**
     * Renvoie un JMenu correspondant à la définition d'un menu dans le fichier de config,
     * pour un document Jaxe donné.
     *
     * @param doc  Le document Jaxe pour lequel le menu est créé
     * @param menudef  L'élément MENU du fichier de config
     */
    protected JMenu creationMenu(final JaxeDocument doc, final Element menudef) {
        final String nomMenu = menudef.getAttribute("nom");
        String titreMenu = titreMenu(nomMenu);
        if (resourceTitres != null) {
            try {
                titreMenu = resourceTitres.getString(titreMenu);
            } catch (final MissingResourceException ex) {
            }
        }
        final JMenu jmenu = new JMenu(titreMenu);
        String docMenu = documentationMenu(nomMenu);
        if (docMenu != null) {
            docMenu = "<html><body>" + docMenu.replaceAll("\n", "<br>") + "</body></html>";
            jmenu.setToolTipText(docMenu);
        }
        Node menunode = menudef.getFirstChild();
        while (menunode != null) {
            JMenuItem item = null;
            final String nodename = menunode.getNodeName();
            if ("MENU_INSERTION".equals(nodename)) {
                final Element insnoeud = (Element)menunode;
                final String nom = insnoeud.getAttribute("nom");
                final String titre = titreMenu(nom);
                String typeNoeud = insnoeud.getAttribute("type_noeud");
                if ("".equals(typeNoeud))
                    typeNoeud = "element";
                final Element refElement;
                if ("element".equals(typeNoeud)) {
                    refElement = referenceElement(nom);
                    if (refElement == null)
                        LOG.error("Erreur: MENU_INSERTION: pas de référence pour '" + nom + "' dans le schéma");
                } else
                    refElement = null;
                item = jmenu.add(new ActionInsertionBalise(doc, titre, refElement, nom, typeNoeud));
                String itemdoc = documentation(refElement);
                if (itemdoc != null) {
                    itemdoc = formatageDoc(itemdoc);
                    item.setToolTipText(itemdoc);
                }
            } else if ("MENU_FONCTION".equals(nodename)) {
                final Element fonction = (Element)menunode;
                final String classe = fonction.getAttribute("classe");
                final String nom = fonction.getAttribute("nom");
                final String titre = titreMenu(nom);
                item = jmenu.add(new ActionFonction(doc, titre, classe, fonction));
                String itemdoc = documentationMenu(nom);
                if (itemdoc != null) {
                    itemdoc = formatageDoc(itemdoc);
                    item.setToolTipText(itemdoc);
                }
            } else if ("MENU".equals(nodename)) {
                item = creationMenu(doc, (Element)menunode);
                jmenu.add(item);
            } else if ("SEPARATEUR".equals(nodename))
                jmenu.addSeparator();
            
            if (item != null) {
                final String commande = ((Element)menunode).getAttribute("raccourci");
                if (commande != null && !"".equals(commande)) {
                    final char c = commande.toUpperCase().charAt(0);
                    final int cmdMenu = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
                    item.setAccelerator(KeyStroke.getKeyStroke(c, cmdMenu));
                }
            }
            menunode = menunode.getNextSibling();
        }
        return(jmenu);
    }
    
    /**
     * Renvoie une barre de menus d'insertion des éléments pour un document Jaxe donné
     *
     * @param doc  Le document Jaxe pour lequel la barre de menus est créée
     */
    public JMenuBar makeMenus(final JaxeDocument doc) {
        final JMenuBar barreBalises = new JMenuBar();
        
        final Element menus = getMenus();
        if (menus != null) {
            Element menudef = findElement(menus, "MENU");
            while (menudef != null) {
                final JMenu jmenu = creationMenu(doc, menudef);
                barreBalises.add(jmenu);
                menudef = nextElement(menudef, "MENU");
            }
        }
        for (final Config conf : autresConfigs) {
            final JMenuBar mbar = conf.makeMenus(doc);
            while (mbar.getMenuCount() > 0) {
                final JMenu menu = mbar.getMenu(0);
                mbar.remove(menu);
                barreBalises.add(menu);
            }
        }
        return(barreBalises);
    }
    
    
    // METHODES LIEES AU SCHEMA
    
    public InterfaceSchema getSchema() {
        return(schema);
    }
    
    /**
     * Renvoie les références de tous les éléments de la config
     */
    public ArrayList<Element> listeTousElements() {
        final ArrayList<Element> liste = schema.listeTousElements();
        for (final Config conf : autresConfigs)
            liste.addAll(conf.listeTousElements());
        return(liste);
    }
    
    protected boolean elementDansSchema(final Element refElement) {
        return(schema.elementDansSchema(refElement));
    }
    
    /**
     * Renvoie le nom de l'élément dont la référence est donnée.
     */
    public String nomElement(final Element refElement) {
        return(cacheElementsVersNoms.get(refElement));
    }
    
    /**
     * Renvoie la référence du premier élément correspondant dans le schéma,
     * en regardant dans les autres configurations si nécessaire.
     */
    public Element getElementRef(final Element el) {
        final Config conf = getElementConf(el);
        if (conf == this)
            return(schema.referenceElement(el));
        else if (conf != null)
            return(conf.getElementRef(el));
        else
            return(null);
    }
    
    /**
     * Renvoie la référence de l'élément correspondant dans le schéma,
     * à partir de l'élément et de la référence de son parent,
     * en regardant dans les autres configurations si nécessaire.
     */
    public Element getElementRef(final Element el, final Element refParent) {
        final Config conf = getElementConf(el);
        if (conf == this) {
            Element ref = schema.referenceElement(el, refParent);
            if (ref == null) // cas d'un enfant dans une autre config que le parent, ou d'un élément invalide
                ref = schema.referenceElement(el);
            return(ref);
        } else if (conf != null)
            return(conf.getElementRef(el, refParent));
        else
            return(null);
    }
    
    /**
     * Renvoie la référence schéma du premier élément avec le nom donné,
     * en regardant dans les sous-configs si nécessaire
     */
    public Element referenceElement(final String nom) {
        final Element el = schema.referenceElement(localValue(nom));
        if (el != null)
            return(el);
        for (final Config conf : autresConfigs) {
            final Element ref2 = conf.referenceElement(nom);
            if (ref2 != null)
                return(ref2);
        }
        return(null);
    }
    
    /**
     * Renvoie l'espace de nom correspondant à la référence de l'élément,
     * ou null si aucun schéma XML n'est utilisé ou que l'espace de noms n'est pas défini.
     * Attention, l'élément doit maintenant être un élément du schéma
     * (avant c'était un élément du fichier de config).
     */
    public String espaceElement(final Element refElement) {
        final Config conf = getRefConf(refElement);
        if (conf != null && conf != this)
            return(conf.espaceElement(refElement));
        final String espace = schema.espaceElement(refElement);
        return(espace);
    }
    
    /**
     * Renvoie le préfixe à utiliser pour créer un élément dont on donne la référence,
     * ou null s'il n'y en a pas.
     * Attention, l'élément doit maintenant être un élément du schéma
     * (avant c'était un élément du fichier de config).
     */
    public String prefixeElement(final Element refElement) {
        final Config conf = getRefConf(refElement);
        if (conf != null && conf != this)
            return(conf.prefixeElement(refElement));
        final String espace = espaceElement(refElement);
        if (espace == null)
            return(null);
        return(prefixeEspace(espace));
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un élément, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     */
    public ArrayList<String> listeValeursElement(final Element refElement) {
        final ArrayList<String> liste = schema.listeValeursElement(refElement);
        return(liste);
    }
    
    /**
     * Renvoie la liste des espaces de noms (String) gérés par cette config et ses sous-configs.
     */
    protected ArrayList<String> listeEspaces() {
        if (cacheListeEspace != null)
            return(cacheListeEspace);
        final LinkedHashSet<String> liste = new LinkedHashSet<String>();
        final ArrayList<String> espacesSchema = schema.listeEspaces();
        if (espacesSchema != null)
            liste.addAll(espacesSchema);
        for (final Config conf : autresConfigs) {
            liste.addAll(conf.listeEspaces());
        }
        final ArrayList<String> result = new ArrayList<String>(liste);
        cacheListeEspace = result;
        return(result);
    }
    
    /**
     * Renvoie un numéro pour l'espace de noms donné, à partir de 0.
     * Un numéro unique est attribué pour chaque espace de noms.
     * Renvoie -1 si l'espace de noms n'est pas trouvé dans la configuration.
     */
    public int numeroEspace(final String ns) {
        final ArrayList<String> liste = listeEspaces();
        return(liste.indexOf(ns));
    }
    
    /**
     * Renvoie true si l'espace de nom est défini dans la config
     */
    public boolean aEspace(final String ns) {
        return(schema.aEspace(ns));
    }
    
    /**
     * Renvoie l'espace de noms cible du schéma (attribut targetNamespace avec WXS)
     */
    public String espaceCible() {
        return(schema.espaceCible());
    }
    
    /**
     * Renvoie les références des éléments qui ne sont pas dans l'espace de noms passé en paramètre
     */
    public ArrayList<Element> listeElementsHorsEspace(final String espace) {
        final ArrayList<Element> liste = schema.listeElementsHorsEspace(espace);
        for (final Config conf : autresConfigs)
            liste.addAll(conf.listeElementsHorsEspace(espace));
        return(liste);
    }
    
    /**
     * Renvoie les références des éléments qui sont dans les espaces de noms passés en paramètre
     */
    public ArrayList<Element> listeElementsDansEspaces(final Set<String> espaces) {
        final ArrayList<Element> liste = schema.listeElementsDansEspaces(espaces);
        for (final Config conf : autresConfigs)
            liste.addAll(conf.listeElementsDansEspaces(espaces));
        return(liste);
    }
    
    /**
     * Renvoie true s'il existe une relation parent-enfant entre les 2 références d'éléments
     */
    public boolean estSousElement(final Element refParent, final Element refEnfant) {
        final ArrayList<Element> enfants = listeSousElements(refParent);
        return(enfants.contains(refEnfant));
    }
    
    /**
     * Renvoie true si le nom donné correspond à un enfant possible du parent dont on passe la référence en paramètre
     */
    public boolean estSousElement(final Element refParent, String nomEnfant) {
        final int inds = nomEnfant.indexOf(':');
        if (inds != -1)
            nomEnfant = nomEnfant.substring(inds+1);
        final ArrayList<String> noms = nomsSousElements(refParent);
        return(noms.contains(nomEnfant));
    }
    
    /**
     * Renvoie les références des éléments enfants de l'élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeSousElements(final Element refParent) {
        return(schema.listeSousElements(refParent));
    }
    
    /**
     * Renvoie les noms des éléments enfants de refParent (un élément du schéma).
     */
    public ArrayList<String> nomsSousElements(final Element refParent) {
        final Config conf = getRefConf(refParent);
        if (conf != null && conf != this)
            return(conf.nomsSousElements(refParent));
        final ArrayList<Element> listeReferences = listeSousElements(refParent);
        final ArrayList<String> listeNoms = new ArrayList<String>();
        for (final Element ref : listeReferences) {
            final String nom = cacheElementsVersNoms.get(ref);
            if (!listeNoms.contains(nom))
                listeNoms.add(nom);
        }
        return(listeNoms);
    }
    
    /**
     * Expression régulière correspondant au schéma pour un élément parent donné
     * @param modevisu  True si on cherche une expression régulière à afficher pour l'utilisateur
     * @param modevalid  Pour obtenir une validation stricte au lieu de chercher si une insertion est possible
     */
    protected String expressionReguliere(final Element refParent, final boolean modevisu, final boolean modevalid) {
        return(schema.expressionReguliere(refParent, modevisu, modevalid));
    }
    
    /**
     * Expression régulière correspondant au schéma pour un élément parent donné
     * Attention, le paramètre doit maintenant être un élément du schéma
     * (avant c'était un élément du fichier de config).
     */
    public String expressionReguliere(final Element refParent) {
        final Config conf = getRefConf(refParent);
        if (conf != null && conf != this)
            return(conf.expressionReguliere(refParent));
        return(schema.expressionReguliere(refParent, true, false));
    }
    
    /**
     * Cherche le premier élément ancêtre de même espace de nom
     */
    public Element chercheParentEspace(final Element el, final String namespace) {
        final Node np = el.getParentNode();
        if (!(np instanceof Element))
            return(null);
        final Element p = (Element)np;
        final String pns = p.getNamespaceURI();
        boolean egal = false;
        if (namespace == null && pns == null)
            egal = true;
        if (namespace != null && namespace.equals(pns))
            egal = true;
        if (egal)
            return(p);
        return(chercheParentEspace(p, namespace));
    }
    
    /**
     * Cherche le premier élément ancêtre de même config
     */
    public Element chercheParentConfig(final Element el, final Config conf) {
        final Node np = el.getParentNode();
        if (!(np instanceof Element))
            return(null);
        final Element p = (Element)np;
        final Config conf2 = getElementConf(p);
        if (conf2 == conf)
            return(p);
        return(chercheParentConfig(p, conf));
    }
    
    /**
     * Renvoie l'expression régulière correspondant aux enfants d'un élément,
     * en n'utilisant que les éléments ayant l'espace de noms du parent si testEspace est true,
     * et en ajoutant aInserer à la place de se qui se trouve entre debutSelection et finSelection.
     * Attention, aInserer doit maintenant être un élément du schéma
     * (avant c'était un élément du fichier de config).
     */
    protected String expressionEspace(final JaxeElement parent, final int debutSelection, final int finSelection,
            final Element aInserer, final boolean testEspace, final String espaceParent) {
        boolean danslazone = parent.debut.getOffset() < debutSelection &&
                parent.fin.getOffset() >= finSelection;
        JaxeElement jcadet = null;
        if (danslazone)
            jcadet = parent.enfantApres(finSelection);
        StringBuilder cettexp = null;
        boolean insere = false;
        Node sousb = parent.noeud.getFirstChild();
        while (sousb != null) {
            if (sousb.getNodeType() == Node.ELEMENT_NODE || sousb.getNodeType() == Node.TEXT_NODE ||
                    sousb.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ||
                    sousb.getNodeType() == Node.COMMENT_NODE ||
                    sousb.getNodeType() == Node.CDATA_SECTION_NODE)  {
                final JaxeElement je = parent.doc.getElementForNode(sousb);
                if (je != null && (je.debut.getOffset() < debutSelection || je.debut.getOffset() >= finSelection)) {
                    if (!testEspace || sousb.getNodeType() == Node.TEXT_NODE ||
                            sousb.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ||
                            sousb.getNodeType() == Node.COMMENT_NODE ||
                            sousb.getNodeType() == Node.CDATA_SECTION_NODE ||
                            (espaceParent == null && sousb.getNamespaceURI() == null) ||
                            (espaceParent != null && espaceParent.equals(sousb.getNamespaceURI()))) {
                        final StringBuilder nomb = new StringBuilder();
                        if (sousb.getNodeType() == Node.ELEMENT_NODE) {
                            nomb.append(localValue(sousb.getNodeName()));
                            nomb.append(",");
                        }
                        if (je == jcadet && danslazone) {
                            nomb.insert(0, ",");
                            nomb.insert(0, nomElement(aInserer));
                            insere = true;
                        }
                        if (cettexp == null)
                            cettexp = new StringBuilder();
                        cettexp.append(nomb);
                    } else {
                        if (je == jcadet && danslazone) {
                            if (cettexp == null)
                                cettexp = new StringBuilder();
                            cettexp.append(nomElement(aInserer));
                            cettexp.append(",");
                            insere = true;
                        }
                        final String ex2 = expressionEspace(je, debutSelection, finSelection, aInserer, testEspace, espaceParent);
                        if (ex2 != null) {
                            if (cettexp == null)
                                cettexp = new StringBuilder();
                            cettexp.append(ex2);
                        }
                        if (je.debut.getOffset() < debutSelection &&
                                je.fin.getOffset() >= finSelection) {
                            insere = true;
                            danslazone = false;
                        }
                    }
                }
            }
            sousb = sousb.getNextSibling();
        }
        if (!insere && danslazone) {
            if (cettexp == null)
                cettexp = new StringBuilder();
            cettexp.append(nomElement(aInserer));
            cettexp.append(",");
        }
        if (cettexp == null)
            return(null);
        return(cettexp.toString());
    }
    
    /**
     * Renvoie true si on peut insérer l'élement aInsérer sous la balise parent à la position pos.
     * Attention, aInserer doit maintenant être un élément du schéma
     * (avant c'était un élément du fichier de config).
     * @deprecated utiliser insertionPossible(JaxeElement, int, int, Element) à la place
     */
    @Deprecated
    public boolean insertionPossible(JaxeElement parent, final Position pos, final Element aInserer) {
        return(insertionPossible(parent, pos.getOffset(), pos.getOffset(), aInserer));
    }
    
    /**
     * Renvoie true si on peut insérer l'élement aInsérer sous la balise parent
     * sur la sélection définie par les positions debutSelection et finSelection.
     */
    public boolean insertionPossible(JaxeElement parent, final int debutSelection, final int finSelection, final Element aInserer) {
        if (schema instanceof SchemaSimple)
            return(true); // on suppose que le test de sous-élément a déjà été fait
        if (schema instanceof SchemaRelaxNG) {
            if (debutSelection < parent.debut.getOffset()) {
                LOG.error("Config.insertionPossible: debutSelection < parent.debut");
                return(false);
            }
            final Element elAInserer = parent.nouvelElementDOM(parent.doc, aInserer);
            Position pos;
            try {
                pos = parent.doc.createPosition(debutSelection);
            } catch (final BadLocationException ble) {
                LOG.error("Config.insertionPossible - BadLocationException", ble);
                return(false);
            }
            parent.insererDOM(pos, elAInserer);
            final boolean insertionOK = ((SchemaRelaxNG)schema).documentValide(parent.doc.DOMdoc, true);
            try {
                parent.noeud.removeChild(elAInserer);
            } catch (final DOMException ex) {
                LOG.error("Config.insertionPossible - DOMException", ex);
            }
            parent.regrouperTextes();
            return(insertionOK);
        }
        if (autresConfigs.size() > 0) {
            final Config conf = getRefConf(aInserer);
/*
    pb: on ne peut pas tester l'ordre des éléments dans certains cas, par exemple:
    <html>
        <head>
            <xsl:if test='truc'>
                <title>xxx</title>
            </xsl:if>
            <xsl:if test='not(truc)'>
                <title>yyy</title>
            </xsl:if>
        </head>
    </html>
    Ici on autorise deux éléments title sous head alors qu'un seul est normalement autorisé.
    Par contre on peut tester les imbrications (title est autorisé sous head).
*/
            if (conf != this)
                return(true);
            
            final Config pconf = getElementConf((Element)parent.noeud);
            if (conf != pconf) {
                final Element noeudparent = chercheParentEspace((Element)parent.noeud, conf.getSchema().espaceElement(aInserer));
                if (noeudparent == null)
                    return(true);
                parent = parent.doc.getElementForNode(noeudparent);
            }
        }
        final Element refParent = parent.refElement;
        final String espaceRacine = parent.noeud.getOwnerDocument().getDocumentElement().getNamespaceURI();
        final boolean xslt = "http://www.w3.org/1999/XSL/Transform".equals(espaceRacine);
        String cettexp = expressionEspace(parent, debutSelection, finSelection, aInserer, xslt, parent.noeud.getNamespaceURI());
        if (cettexp == null)
            cettexp = "";
        //System.out.println("cettexp: " + cettexp);
        
        if (cacheInsertion == null)
            cacheInsertion = new HashMap<Element, Pattern>();
        
        // jakarta-regexp
        //RE r = (RE)cacheInsertion.get(refParent);
        // jakarta-oro
        //Pattern r = cacheInsertion.get(refParent);
        // gnu-regexp
        //RE r = (RE)cacheInsertion.get(refParent);
        // java.util.regex
        Pattern r = cacheInsertion.get(refParent);
        
        if (r == null) {
            final String expr = "^" + schema.expressionReguliere(refParent, false, false) + "$";
            // jakarta-regexp
            /*
            try {
                r = new RE(expr);
            } catch (RESyntaxException ex) {
                LOG.error("insertionPossible(JaxeElement, Position, Element) - Malformed Pattern: " + expr, ex);
                return(true);
            }
            */
            
            // jakarta-oro
            /*
            try {
                r = compiler.compile(expr);
            } catch (final MalformedPatternException ex) {
                LOG.error("insertionPossible(JaxeElement, Position, Element) - Malformed Pattern: " + expr, ex);
                return(true);
            }
            */
            
            // gnu-regexp
            /*
            try {
                r = new RE(expr);
            } catch (REException ex) {
                LOG.error("insertionPossible(JaxeElement, Position, Element) - Malformed Pattern: " + expr, ex);
                return(true);
            }
            */
            
            // java.util.regex
            try {
                r = Pattern.compile(expr);
            } catch (final PatternSyntaxException ex) {
                LOG.error("insertionPossible(JaxeElement, Position, Element) :" + expr, ex);
                return(true);
            }
            
            cacheInsertion.put(refParent, r);
        }
        
        // jakarta-regexp
        //boolean matched = r.match(cettexp);
        // jakarta-oro
        //final boolean matched = matcher.matches(cettexp, r);
        // gnu-regexp
        //boolean matched = r.isMatch(cettexp);
        // java.util.regex
        final boolean matched = r.matcher(cettexp).matches();
        return(matched);
    }
    
    /**
     * Renvoie true si le nom donné correspond à un enfant possible du parent dont on passe la définition en paramètre
     * @deprecated remplacé par estSousElement
     */
    @Deprecated
    public boolean sousbalise(final Element parentdef, final String nombalise) {
        LOG.error("Config.sousbalise utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(estSousElement(parentdef, nombalise));
    }
    
    /**
     * Renvoie les noms des éléments enfants de parentdef (un élément du fichier de config).
     * @deprecated remplacé par listeSousElements et nomsSousElements
     */
    @Deprecated
    public ArrayList<String> listeSousbalises(final Element parentdef) {
        LOG.error("Config.listeSousbalises utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(nomsSousElements(parentdef));
    }
    
    /**
     * Renvoie true si l'élément parent est valide par rapport à ses enfants (au niveau 1).
     * + renvoie l'expression régulière utilisée pour le test dans texpr[0] si details=true
     */
    public boolean elementValide(final JaxeElement parent, final boolean details, final String[] texpr) {
        if (schema instanceof SchemaSimple)
            return(true); // on suppose que le test de sous-balise a déjà été fait
        if (autresConfigs.size() > 0) {
            final Config conf = getElementConf((Element)parent.noeud);
            if (conf != this)
                return(true); // on ne peut pas tester, cf commentaire dans insertionPossible
        }
        final Config conf = getElementConf((Element)parent.noeud);
        if (conf == null)
            return(true);
        final Element refParent = parent.refElement;
        final StringBuilder cettexp = new StringBuilder();
        if (validePatternCache == null)
            validePatternCache = new HashMap<Element, Pattern>();
        
        boolean avectexte = false;
        Node sousb = parent.noeud.getFirstChild();
        while (sousb != null) {
            if (sousb.getNodeType() == Node.ELEMENT_NODE)  {
                final JaxeElement je = parent.doc.getElementForNode(sousb);
                if (je != null) {
                    cettexp.append(localValue(sousb.getNodeName()));
                    cettexp.append(",");
                }
            } else if (sousb.getNodeType() == Node.TEXT_NODE || sousb.getNodeType() == Node.CDATA_SECTION_NODE) {
                if (!"".equals(sousb.getNodeValue().trim()))
                    avectexte = true;
            }
            sousb = sousb.getNextSibling();
        }
        if (avectexte && !schema.contientDuTexte(refParent))
            return(false);
        Pattern r = validePatternCache.get(refParent);
        if (r == null) {
            final String expr = conf.expressionReguliere(refParent, false, true);
            if (expr == null || expr.equals(""))
                return(true);
            //System.out.println("parent: "+parent.noeud.getNodeName()+" expression: '"+cettexp.toString()+"'");
            //System.out.println("test: " + expr);
            //System.out.println("visu: " + exprvisu);
            
            // jakarta-regexp
            /*
            RE r;
            try {
                r = new RE(expr);
            } catch (RESyntaxException ex) {
                LOG.error("elementValide(JaxeElement, boolean, String[]) - Malformed Pattern: " + expr, ex);
                return(true);
            }
            boolean matched = r.match(cettexp.toString());
            */
            
            // jakarta-oro
            /*
            Pattern r;
            try {
                r = compiler.compile(expr);
            } catch (final MalformedPatternException ex) {
                LOG.error("elementValide(JaxeElement, boolean, String[]) - Malformed Pattern: " + expr, ex);
                return(true);
            }
            final boolean matched = matcher.matches(cettexp.toString(), r);
            */
            
            // gnu-regexp
            /*
            RE r;
            try {
                r = new RE(expr);
            } catch (REException ex) {
                LOG.error("elementValide(JaxeElement, boolean, String[]) - Malformed Pattern: " + expr, ex);
                return(true);
            }
            boolean matched = r.isMatch(cettexp.toString());
            */
            
            // java.util.regex
            try {
                r = Pattern.compile("^" + expr + "$");
            } catch (final PatternSyntaxException ex) {
                LOG.error("elementValide(JaxeElement, boolean, String[]) - Malformed Pattern: ^" + expr + "$",
                        ex);
                return(true);
            }
            validePatternCache.put(refParent, r);
        }

        String exprvisu = null;
        if (details)
            exprvisu = conf.expressionReguliere(refParent, true, false);
        
        final boolean matched = r.matcher(cettexp.toString()).matches();
        
        if (matched)
            return(true);
        if (details)
            texpr[0] = exprvisu;
        return(false);
    }
    
    /**
     * Renvoie la liste des noms des parents possibles pour un élément dont
     * on donne la définition dans le fichier de config en paramètre
     * @deprecated remplacé par listeElementsParents et nomsParents
     */
    @Deprecated
    public ArrayList<String> listeParents(final Element balisedef) {
        LOG.error("Config.listeParents utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(nomsParents(balisedef));
    }
    
    /**
     * Renvoie la liste des références des éléments parents possibles pour un élément
     * dont on donne la référence dans le schéma en paramètre
     */
    public ArrayList<Element> listeElementsParents(final Element refElement) {
        final Config conf = getRefConf(refElement);
        if (conf != null && conf != this)
            return(conf.listeElementsParents(refElement));
        return(schema.listeElementsParents(refElement));
    }
    
    /**
     * Renvoie la liste des noms des parents possibles pour un élément dont
     * on donne la référence dans le schéma en paramètre
     */
    public ArrayList<String> nomsParents(final Element refElement) {
        final ArrayList<Element> listeReferences = listeElementsParents(refElement);
        final ArrayList<String> listeNoms = new ArrayList<String>();
        for (final Element ref : listeReferences) {
            final String nom = cacheElementsVersNoms.get(ref);
            if (!listeNoms.contains(nom))
                listeNoms.add(nom);
        }
        return(listeNoms);
    }
    
    /**
     * Renvoie true si l'élément dont on donne la définition dans le fichier de config peut contenir du texte
     * Attention, l'élément doit maintenant être un élément du schéma
     * (avant c'était un élément du fichier de config).
     */
    public boolean contientDuTexte(final Element refElement) {
        return(schema.contientDuTexte(refElement));
    }
    
    /**
     * Renvoie l'élément simpleType ou complexType avec le nom et l'espace de noms donnés.
     * Renvoie null si aucun type correspondant n'est trouvé.
     */
    public Element getSchemaTypeElement(final String nomType, final String espace) {
        if (schema instanceof SchemaW3C) {
            final Element el = ((SchemaW3C)schema).getSchemaTypeElement(nomType, espace);
            if (el != null)
                return(el);
        }
        for (final Config conf : autresConfigs) {
            final Element el = conf.getSchemaTypeElement(nomType, espace);
            if (el != null)
                return(el);
        }
        return(null);
    }
    
    /**
     * Renvoie un objet permettant de vérifier la validité d'un élément ou d'un attribut,
     * à partir du document Jaxe et de l'élément "element" ou "attribute" du schéma W3C
     */
    public VerifTypeSimple getVerifTypeSimple(final Element snodedef) {
        if (!(schema instanceof SchemaW3C))
            return(null);
        if (hashVerif == null)
            hashVerif = new HashMap<Element, VerifTypeSimple>();
        final Object res = hashVerif.get(snodedef);
        if (res != null)
            return((VerifTypeSimple)res);
        final VerifTypeSimple verif = new VerifTypeSimple(this, snodedef);
        hashVerif.put(snodedef, verif);
        return(verif);
    }
    
    /**
     * Renvoie la liste des attributs possibles pour un élément dont
     * on donne la référence en paramètre
     * Attention, l'élément doit maintenant être un élément du schéma
     * (avant c'était un élément du fichier de config).
     */
    public ArrayList<Element> listeAttributs(final Element refElement) {
        final Config conf = getRefConf(refElement);
        if (conf != null && conf != this)
            return(conf.listeAttributs(refElement));
        return(schema.listeAttributs(refElement));
    }
    
    /**
     * Renvoie le nom d'un attribut à partir de sa définition (dans le schéma s'il y en a un, ou sinon
     * dans le fichier de config)
     */
    public String nomAttribut(final Element attdef) {
        return(schema.nomAttribut(attdef));
    }
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de sa définition, ou null si aucun n'est défini
     */
    public String espaceAttribut(final Element attdef) {
        return(schema.espaceAttribut(attdef));
    }
    
    /**
     * Renvoie le préfixe à utiliser pour créer un attribut dont on donne l'élément parent et la référence,
     * ou null s'il n'y en a pas.
     */
    public String prefixeAttribut(final Element parent, final Element attdef) {
        final String espace = espaceAttribut(attdef);
        if (espace == null)
            return(null);
        if ("http://www.w3.org/XML/1998/namespace".equals(espace))
            return("xml");
        if ("http://www.w3.org/2000/xmlns/".equals(espace) && !"xmlns".equals(nomAttribut(attdef)))
            return("xmlns");
        // on essaye lookupPrefix avec le parent et avec son document
        // (cas d'un élément en cours de création, pas encore inséré dans le document)
        String prefixe = parent.lookupPrefix(espace);
        if (prefixe == null) {
            if (parent.getOwnerDocument().getDocumentElement() != null) // si l'élément racine existe
                prefixe = parent.getOwnerDocument().lookupPrefix(espace);
            else
                prefixe = prefixeEspace(espace); // on suppose que la racine sera créée avec ajouterAttributsEspaces
        }
        return(prefixe);
    }
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de son nom complet (avec le préfixe s'il y en a un)
     */
    public String espaceAttribut(final String nom) {
        return(schema.espaceAttribut(nom));
    }
    
    /**
     * Renvoie true si un attribut est obligatoire, à partir de sa définition (dans le schéma s'il y en a un, ou sinon
     * dans le fichier de config)
     */
    public boolean estObligatoire(final Element attdef) {
        return(schema.estObligatoire(attdef));
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un attribut, à partir de sa définition
     * @deprecated utiliser listeValeursAttribut à la place
     */
    @Deprecated
    public String[] listeValeurs(final Element attdef) {
        final ArrayList<String> liste = schema.listeValeursAttribut(attdef);
        if (liste == null)
            return(null);
        final String[] tableau = new String[liste.size()];
        return(liste.toArray(tableau));
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un attribut, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     */
    public ArrayList<String> listeValeursAttribut(final Element refAttribut) {
        final ArrayList<String> liste = schema.listeValeursAttribut(refAttribut);
        return(liste);
    }
    
    /**
     * Renvoie la valeur par défaut d'un attribut dont l'élément définition est donné en paramètre
     * (c'est la valeur de l'attribut "default")
     */
    public String valeurParDefaut(final Element attdef) {
        return(schema.valeurParDefaut(attdef));
    }
    
    /**
     * Renvoie true si la valeur donnée est une valeur valide pour l'attribut
     */
    public boolean attributValide(final Element refAttribut, final String valeur) {
        return(schema.attributValide(refAttribut, valeur));
    }
    
    /**
     * Renvoie la partie locale du nom d'un élément (en retirant le préfixe)
     */
    public static String localValue(final String s) {
        if (s == null)
            return(null);
        final int ind = s.indexOf(':');
        if (ind == -1)
            return(s);
        return(s.substring(ind + 1));
    }
    
    
    // METHODES POUR LES TYPES D'AFFICHAGES
    
    /**
     * Renvoie le type d'affichage d'un noeud à partir de la référence d'élément, le nom du noeud et le type de noeud
     */
    public String typeAffichageNoeud(final Element refElement, final String nom, final String typeNoeud) {
        if (refElement != null) {
            final Config conf = getRefConf(refElement);
            if (conf != null && conf != this)
                return(conf.typeAffichageNoeud(refElement, nom, typeNoeud));
        }
        if ("element".equals(typeNoeud)) {
            final Element affel = getAffichageElement(localValue(nom));
            if (affel == null)
                return(typeAffichageParDefaut);
            return(affel.getAttribute("type"));
        } else if ("instruction".equals(typeNoeud)) {
            Element elplug = findElement(getAffichageNoeuds(), "PLUGIN_INSTRUCTION");
            while (elplug != null) {
                if (nom != null && nom.equals(elplug.getAttribute("cible")))
                    return("plugin");
                elplug = nextElement(elplug, "PLUGIN_INSTRUCTION");
            }
            return("instruction");
        } else if ("commentaire".equals(typeNoeud)) {
            final Element elplug = findElement(getAffichageNoeuds(), "PLUGIN_COMMENTAIRE");
            if (elplug != null)
                return("plugin");
            return("commentaire");
        } else if ("cdata".equals(typeNoeud)) {
            final Element elplug = findElement(getAffichageNoeuds(), "PLUGIN_CDATA");
            if (elplug != null)
                return("plugin");
            return("cdata");
        }
        return(null);
    }

    /**
     * Renvoie le type d'affichage d'un élément à partir de sa référence
     */
    public String typeAffichageElement(final Element refElement) {
        final Config conf = getRefConf(refElement);
        if (conf != null && conf != this)
            return(conf.typeAffichageElement(refElement));
        final Element affel = getAffichageElement(nomElement(refElement));
        if (affel == null)
            return(typeAffichageParDefaut);
        return(affel.getAttribute("type"));
    }
    
    /**
     * Renvoie la référence du premier élément dont le type d'affichage dans le fichier de config est celui indiqué
     */
    public Element premierElementAvecType(final String typeAffichage) {
        if (jaxecfg == null)
            return(null);
        Element affel = findElement(getAffichageNoeuds(), "AFFICHAGE_ELEMENT");
        while (affel != null) {
            if (typeAffichage.equals(affel.getAttribute("type")))
                return(referenceElement(affel.getAttribute("element")));
            affel = nextElement(affel, "AFFICHAGE_ELEMENT");
        }
        return(null);
    }
    
    /**
     * Renvoie la première valeur pour un paramètre donné.
     * @param defbalise l'élément BALISE
     * @param parameter le nom du paramètre
     * @param defaultvalue la valeur par défaut, renvoyée si le paramètre n'est pas trouvé
     * @return la première valeur trouvée pour le paramètre
     *
     * @deprecated utiliser valeurParametreElement à la place
     */
    @Deprecated
    public String getParamFromDefinition(final Element defbalise, final String parameter, final String defaultvalue) {
        LOG.error("Config.getParamFromDefinition utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(valeurParametreElement(defbalise, parameter, defaultvalue));
    }
    
    /**
     * Renvoie la valeur d'un paramètre d'affichage pour un élément (pas un noeud d'un autre type)
     * @param refElement référence de l'élément (un élément du schéma)
     * @param nomParametre nom du paramètre
     * @param defaut valeur par défaut, utilisée si le paramètre n'est pas trouvé
     */
    public String valeurParametreElement(final Element refElement, final String nomParametre, final String defaut) {
        return valeurParametreElement(refElement, "element", null, nomParametre, defaut);
    }

    /**
     * Renvoie la valeur d'un paramètre d'affichage pour un élément (pas un noeud d'un autre type)
     * @param refElement référence de l'élément (un élément du schéma)
     * @param nomParametre nom du paramètre
     * @param defaut valeur par défaut, utilisée si le paramètre n'est pas trouvé
     */
    public String valeurParametreElement(final Element refElement, final String nodeType, final String nom, final String nomParametre, final String defaut) {
        final HashMap<String, ArrayList<String>> table = getParametresNoeud(refElement, nodeType, nom);
        final ArrayList<String> lval = table.get(nomParametre);
        String valeur;
        if (lval != null && lval.size() > 0)
            valeur = lval.get(0);
        else
            valeur = defaut;
        return valeur;
    }

    /**
     * Renvoie la valeur d'un paramètre de fonction
     * @param fctdef Elément du menu de la fonction dans le fichier de config
     * @param nomParametre nom du paramètre
     * @param defaut valeur par défaut, utilisée si le paramètre n'est pas trouvé
     */
    public String valeurParametreFonction(final Element fctdef, final String nomParametre, final String defaut) {
        Element parel = findElement(fctdef, "PARAMETRE");
        while (parel != null) {
            final String nom = parel.getAttribute("nom");
            if (nom.equals(nomParametre))
                return(parel.getAttribute("valeur"));
            parel = nextElement(parel, "PARAMETRE");
        }
        return(defaut);
    }

    protected HashMap<String, ArrayList<String>> construireCacheParams(final Element base) {
        final HashMap<String, ArrayList<String>> hashparams = new HashMap<String, ArrayList<String>>();
        Element parel = findElement(base, "PARAMETRE");
        while (parel != null) {
            final String nom = parel.getAttribute("nom");
            final String valeur = parel.getAttribute("valeur");
            ArrayList<String> lval = hashparams.get(nom);
            if (lval == null) {
                lval = new ArrayList<String>();
                lval.add(valeur);
                hashparams.put(nom, lval);
            } else
                lval.add(valeur);
            parel = nextElement(parel, "PARAMETRE");
        }
        cacheParametres.put(base, hashparams);
        return(hashparams);
    }
    
    /**
     * Renvoie la table des paramètres d'affichage d'un élément.
     * @return la table hash
     */
    public HashMap<String, ArrayList<String>> getParametresElement(final Element refElement) {
        return(getParametresNoeud(refElement, "element", null));
    }
    
    /**
     * Renvoie la table des paramètres d'affichage d'un noeud.
     * Le nom peut être null si typeNoeud vaut "element" et que refElement n'est pas null.
     * @return la table hash
     */
    public HashMap<String, ArrayList<String>> getParametresNoeud(final Element refElement, final String typeNoeud, final String nom) {
        if (refElement != null) {
            final Config conf = getRefConf(refElement);
            if (conf != null && conf != this)
                return(conf.getParametresNoeud(refElement, typeNoeud, nom));
        }
        Element base;
        if ("element".equals(typeNoeud))
            base = getAffichageElement(nomElement(refElement));
        else if ("instruction".equals(typeNoeud)) {
            base = null;
            Element elplug = findElement(getAffichageNoeuds(), "PLUGIN_INSTRUCTION");
            while (elplug != null) {
                if (nom != null && nom.equals(elplug.getAttribute("cible"))) {
                    base = elplug;
                    break;
                }
                elplug = nextElement(elplug, "PLUGIN_INSTRUCTION");
            }
        }else if ("commentaire".equals(typeNoeud)) {
            final Element elplug = findElement(getAffichageNoeuds(), "PLUGIN_COMMENTAIRE");
            if (elplug == null) {
                base = null;
            } else {
                base = elplug;
            }
        }else
            base = null;
        if (base == null)
            return(new HashMap<String, ArrayList<String>>());
        if (cacheParametres == null)
            cacheParametres = new HashMap<Element, HashMap<String, ArrayList<String>>>();
        HashMap<String, ArrayList<String>> hashparams = cacheParametres.get(base);
        if (hashparams == null)
            hashparams = construireCacheParams(base);
        return(hashparams);
    }
    
    /**
     * Renvoie une liste de valeurs pour un paramètre
     * @param defbalise la définition de l'élément
     * @param nomParam le paramètre
     * @return les valeurs pour le paramètre, sous forme d'une ArrayList qui peut être nulle
     * @deprecated utiliser getParametresElement ou getParametresNoeud à la place
     */
    @Deprecated
    public ArrayList<String> getValeursParam(final Element defbalise, final String nomParam) {
        LOG.error("Config.getValeursParam utilisé avec un élément du schéma au lieu d'un élément du fichier de config");
        return(getParametresElement(defbalise).get(nomParam));
    }
    
    /**
     * Renvoie la liste des valeurs suggérées dans le fichier de config pour un élément,
     * à partir de sa référence.
     * Renvoie null si aucun affichage n'est défini pour l'élément.
     */
    public ArrayList<String> listeValeursSuggereesElement(final Element refElement) {
        final Config conf = getRefConf(refElement);
        if (conf != null && conf != this)
            return(conf.listeValeursSuggereesElement(refElement));
        final Element affel = getAffichageElement(nomElement(refElement));
        if (affel == null)
            return(null);
        final ArrayList<String> liste = new ArrayList<String>();
        Element vs = findElement(affel, "VALEUR_SUGGEREE");
        while (vs != null) {
            final String v = dom_valeurElement(vs);
            if (v != null)
                liste.add(v);
            vs = nextElement(vs, "VALEUR_SUGGEREE");
        }
        return(liste);
    }
    
    /**
     * Renvoie la liste des valeurs suggérées dans le fichier de config pour un attribut,
     * à partir de la référence de l'élément parent et de la référence de l'attribut.
     * Renvoie null si aucun affichage n'est défini pour l'attribut.
     */
    public ArrayList<String> listeValeursSuggereesAttribut(final Element refParent, final Element refAttribut) {
        final Config conf = getRefConf(refParent);
        if (conf != null && conf != this)
            return(conf.listeValeursSuggereesAttribut(refParent, refAttribut));
        final Element affel = getAffichageElement(nomElement(refParent));
        if (affel == null)
            return(null);
        final ArrayList<String> liste = new ArrayList<String>();
        final String nomAttribut = nomAttribut(refAttribut);
        Element aa = findElement(affel, "AFFICHAGE_ATTRIBUT");
        while (aa != null) {
            if (aa.getAttribute("attribut").equals(nomAttribut)) {
                Element vs = findElement(aa, "VALEUR_SUGGEREE");
                while (vs != null) {
                    final String v = dom_valeurElement(vs);
                    if (v != null)
                        liste.add(v);
                    vs = nextElement(vs, "VALEUR_SUGGEREE");
                }
            }
            aa = nextElement(aa, "AFFICHAGE_ATTRIBUT");
        }
        return(liste);
    }
    
    
    // METHODES POUR LES STRINGS
    
    /**
     * Renvoie une liste des éléments STRINGS du fichier de config,
     * ordonnée en fonction de la langue et du pays de l'utilisateur
     * (par ordre de préférence).
     */
    protected ArrayList<Element> listeElementsStrings() {
        final Locale defaut = Locale.getDefault();
        final ArrayList<Element> liste = new ArrayList<Element>();
        
        final List<Element> lstrings = getStrings();
        for (final Element strings : lstrings) {
            final String langue = strings.getAttribute("langue");
            if (!"".equals(langue)) {
                final Locale strloc;
                if ("".equals(strings.getAttribute("pays")))
                    strloc = new Locale(langue);
                else
                    strloc = new Locale(langue, strings.getAttribute("pays"));
                if (defaut.equals(strloc) && !liste.contains(strings))
                    liste.add(strings);
            }
        }
        for (final Element strings : lstrings) {
            final String langue = strings.getAttribute("langue");
            if (!"".equals(langue)) {
                final Locale test = new Locale(defaut.getLanguage(), defaut.getCountry());
                final Locale strloc;
                if ("".equals(strings.getAttribute("pays")))
                    strloc = new Locale(langue);
                else
                    strloc = new Locale(langue, strings.getAttribute("pays"));
                if (test.equals(strloc) && !liste.contains(strings))
                    liste.add(strings);
            }
        }
        for (final Element strings : lstrings) {
            final String langue = strings.getAttribute("langue");
            if (!"".equals(langue)) {
                final Locale test = new Locale(defaut.getLanguage());
                if (test.equals(new Locale(langue)) && !liste.contains(strings))
                    liste.add(strings);
            }
        }
        for (final Element strings : lstrings) {
            if (!liste.contains(strings))
                liste.add(strings);
        }
        return(liste);
    }
    
    /**
     * Renvoie la description de la config (contenu de l'élément DESCRIPTION_CONFIG du fichier de config)
     */
    public String description() {
        String desc;
        if (resourceTitres != null) {
            try {
                desc = resourceTitres.getString("description_config");
            } catch (final MissingResourceException ex) {
                desc = null;
            }
            if (desc != null)
                return(desc);
        }
        final ArrayList<Element> lstrings = listeElementsStrings();
        for (final Element strings : lstrings) {
            final Element descel = findElement(strings, "DESCRIPTION_CONFIG");
            if (descel == null || descel.getFirstChild() == null)
                break;
            desc = dom_valeurElement(descel);
            return(desc);
        }
        return(null);
    }
    
    /**
     * Renvoie le titre d'un menu à partir de son nom
     */
    public String titreMenu(final String nom) {
        final ArrayList<Element> lstrings = listeElementsStrings();
        for (final Element strings : lstrings) {
            Element sm = findElementDeep(strings, "STRINGS_MENU");
            while (sm != null) {
                if (nom.equals(sm.getAttribute("menu"))) {
                    final Element eltitre = findElement(sm, "TITRE");
                    if (eltitre != null && eltitre.getFirstChild() != null) {
                        return(dom_valeurElement(eltitre));
                    }
                    break;
                }
                sm = nextElementDeep(strings, sm, "STRINGS_MENU");
            }
        }
        final Element refel = referenceElement(nom);
        if (refel != null)
            return(titreElement(refel));
        return(nom);
    }
    
    /**
     * Renvoie la documentation d'un menu à partir de son nom
     */
    public String documentationMenu(final String nom) {
        final ArrayList<Element> lstrings = listeElementsStrings();
        for (final Element strings : lstrings) {
            Element sm = findElementDeep(strings, "STRINGS_MENU");
            while (sm != null) {
                if (nom.equals(sm.getAttribute("menu"))) {
                    final Element eldoc = findElement(sm, "DOCUMENTATION");
                    if (eldoc != null && eldoc.getFirstChild() != null) {
                        return(dom_valeurElement(eldoc));
                    }
                    break;
                }
                sm = nextElementDeep(strings, sm, "STRINGS_MENU");
            }
        }
        return(null);
    }
    
    /**
     * Renvoie le titre d'un élément à partir de sa référence
     */
    public String titreElement(final Element refElement) {
        String titre = null;
        titre = cacheTitresElements.get(refElement);
        if (titre != null)
            return(titre);
        final Config conf = getRefConf(refElement);
        if (conf != null && conf != this)
            titre = conf.titreElement(refElement);
        else {
            final String nom = nomElement(refElement);
            if (nom == null) {
                LOG.error("Config.titreElement : pas de nom pour " + refElement);
                return(null);
            }
            if (titre == null) {
                final ArrayList<Element> lstrings = listeElementsStrings();
                for (final Element strings : lstrings) {
                    if (titre == null) {
                        Element sel = findElement(strings, "STRINGS_ELEMENT");
                        while (sel != null) {
                            if (sel.getAttribute("element").equals(nom)) {
                                final Element eltitre = findElement(sel, "TITRE");
                                if (eltitre != null && eltitre.getFirstChild() != null) {
                                    titre = dom_valeurElement(eltitre);
                                    break;
                                }
                                break;
                            }
                            sel = nextElement(sel, "STRINGS_ELEMENT");
                        }
                    }
                }
            }
            if (resourceTitres == null) {
                if (titre == null || "".equals(titre))
                    titre = nom;
            } else {
                if (titre == null || "".equals(titre)) {
                    try {
                        titre = resourceTitres.getString(nom);
                    } catch (final MissingResourceException ex) {
                        titre = nom;
                    }
                } else {
                    try {
                        titre = resourceTitres.getString(titre);
                    } catch (final MissingResourceException ex) {
                    }
                }
            }
        }
        cacheTitresElements.put(refElement, titre);
        return(titre);
    }
    
    /**
     * Renvoie la documentation d'un élément dont on donne la référence
     * Attention, l'élément doit maintenant être un élément du schéma
     * (avant c'était un élément du fichier de config).
     */
    public String documentation(final Element refElement) {
        if (refElement == null)
            return(null);
        final Config conf = getRefConf(refElement);
        if (conf != null && conf != this)
            return(conf.documentation(refElement));
        final String nom = nomElement(refElement);
        final ArrayList<Element> lstrings = listeElementsStrings();
        for (final Element strings : lstrings) {
            Element sel = findElement(strings, "STRINGS_ELEMENT");
            while (sel != null) {
                if (nom.equals(sel.getAttribute("element"))) {
                    final Element eldoc = findElement(sel, "DOCUMENTATION");
                    if (eldoc != null && eldoc.getFirstChild() != null)
                        return(dom_valeurElement(eldoc));
                    break;
                }
                sel = nextElement(sel, "STRINGS_ELEMENT");
            }
        }
        return(schema.documentationElement(refElement));
    }
    
    /**
     * Formatte la documentation en HTML avec des sauts de ligne pour éviter les lignes trop longues.
     */
    public static String formatageDoc(final String documentation) {
        String doc = documentation;
        doc = doc.replace("&", "&amp;");
        doc = doc.replace("<", "&lt;");
        doc = doc.replace(">", "&gt;");
        if (doc.length() > 100) {
            int p = 0;
            for (int i=0; i<doc.length(); i++) {
                if (i-p > 90 && doc.charAt(i) == ' ') {
                    doc = doc.substring(0, i) + "\n" + doc.substring(i+1);
                    p = i;
                } else if (doc.charAt(i) == '\n')
                    p = i;
            }
        }
        doc = "<html><body>" + doc.replaceAll("\n", "<br>") + "</body></html>";
        return(doc);
    }
    
    /**
     * Renvoie le titre pour une valeur d'élément à partir de la référence de l'élément et la valeur
     */
    public String titreValeurElement(final Element refElement, final String valeur) {
        final String nomElement = nomElement(refElement);
        final ArrayList<Element> lstrings = listeElementsStrings();
        final String langueSyst = Locale.getDefault().getLanguage();
        for (final Element strings : lstrings) {
            Element sel = findElement(strings, "STRINGS_ELEMENT");
            while (sel != null) {
                if (sel.getAttribute("element").equals(nomElement)) {
                    Element eltitrev = findElement(sel, "TITRE_VALEUR");
                    while (eltitrev != null) {
                            if (eltitrev.getAttribute("valeur").equals(valeur) &&
                                    eltitrev.getFirstChild() != null)
                                return(dom_valeurElement(eltitrev));
                        eltitrev = nextElement(eltitrev, "TITRE_VALEUR");
                    }
                    break;
                }
                sel = nextElement(sel, "STRINGS_ELEMENT");
            }
            // la langue est trouvée mais il n'y a pas de TITRE_VALEUR correspondant
            // -> on renvoie la vraie valeur plutôt que de chercher un titre
            // dans d'autres langues.
            final String langue = strings.getAttribute("langue");
            if (langue.equals(langueSyst))
                return(valeur);
        }
        return(valeur);
    }
    
    /**
     * Renvoie le titre d'un attribut à partir de la référence de l'élément parent et
     * de la référence de l'attribut
     */
    public String titreAttribut(final Element refParent, final Element refAttribut) {
        final String nomElement = nomElement(refParent);
        final String nomAttribut = nomAttribut(refAttribut);
        final ArrayList<Element> lstrings = listeElementsStrings();
        for (final Element strings : lstrings) {
            Element sel = findElement(strings, "STRINGS_ELEMENT");
            while (sel != null) {
                if (sel.getAttribute("element").equals(nomElement)) {
                    Element sat = findElement(sel, "STRINGS_ATTRIBUT");
                    while (sat != null) {
                        if (sat.getAttribute("attribut").equals(nomAttribut)) {
                            final Element eltitre = findElement(sat, "TITRE");
                            if (eltitre != null && eltitre.getFirstChild() != null)
                                return(dom_valeurElement(eltitre));
                            break;
                        }
                        sat = nextElement(sat, "STRINGS_ATTRIBUT");
                    }
                }
                sel = nextElement(sel, "STRINGS_ELEMENT");
            }
        }
        return(nomAttribut);
    }
    
    /**
     * Renvoie le titre d'un attribut à partir de sa référence.
     * L'élément parent de l'attribut doit être défini dans le schéma de cette configuration.
     *
     * @deprecated un attribut peut avoir plusieurs parents, utiliser
     * titreAttribut(final Element refParent, final Element refAttribut)
     * à la place.
     */
    @Deprecated
    public String titreAttribut(final Element refAttribut) {
        final Element refParent = schema.parentAttribut(refAttribut);
        return(titreAttribut(refParent, refAttribut));
    }
    
    /**
     * Renvoie le titre pour une valeur d'attribut à partir de la référence de l'élément parent,
     * la référence de l'attribut et la valeur
     */
    public String titreValeurAttribut(final Element refParent, final Element refAttribut, final String valeur) {
        final String nomElement = nomElement(refParent);
        final String nomAttribut = nomAttribut(refAttribut);
        final ArrayList<Element> lstrings = listeElementsStrings();
        final String langueSyst = Locale.getDefault().getLanguage();
        for (final Element strings : lstrings) {
            Element sel = findElement(strings, "STRINGS_ELEMENT");
            while (sel != null) {
                if (sel.getAttribute("element").equals(nomElement)) {
                    Element sat = findElement(sel, "STRINGS_ATTRIBUT");
                    while (sat != null) {
                        if (sat.getAttribute("attribut").equals(nomAttribut)) {
                            Element eltitrev = findElement(sat, "TITRE_VALEUR");
                            while (eltitrev != null) {
                                if (eltitrev.getAttribute("valeur").equals(valeur) &&
                                        eltitrev.getFirstChild() != null)
                                    return(dom_valeurElement(eltitrev));
                                eltitrev = nextElement(eltitrev, "TITRE_VALEUR");
                            }
                            break;
                        }
                        sat = nextElement(sat, "STRINGS_ATTRIBUT");
                    }
                }
                sel = nextElement(sel, "STRINGS_ELEMENT");
            }
            // la langue est trouvée mais il n'y a pas de TITRE_VALEUR correspondant
            // -> on renvoie la vraie valeur d'attribut plutôt que de chercher un titre
            // dans d'autres langues.
            final String langue = strings.getAttribute("langue");
            if (langue.equals(langueSyst))
                return(valeur);
        }
        return(valeur);
    }
    
    /**
     * Renvoie le titre pour une valeur d'attribut à partir de la référence d'attribut et de la valeur
     * L'élément parent de l'attribut doit être défini dans le schéma de cette configuration.
     *
     * @deprecated un attribut peut avoir plusieurs parents, utiliser
     * titreValeurAttribut(final Element refParent, final Element refAttribut, final String valeur)
     * à la place.
     */
    @Deprecated
    public String titreValeurAttribut(final Element refAttribut, final String valeur) {
        final Element refParent = schema.parentAttribut(refAttribut);
        return(titreValeurAttribut(refParent, refAttribut, valeur));
    }
    
    /**
     * Renvoie la documentation d'un attribut à partir de la référence de l'élément parent
     * et la référence de l'attribut.
     */
    public String documentationAttribut(final Element refParent, final Element refAttribut) {
        final String nomElement = nomElement(refParent);
        final String nomAttribut = nomAttribut(refAttribut);
        final ArrayList<Element> lstrings = listeElementsStrings();
        for (final Element strings : lstrings) {
            Element sel = findElement(strings, "STRINGS_ELEMENT");
            while (sel != null) {
                if (sel.getAttribute("element").equals(nomElement)) {
                    Element sat = findElement(sel, "STRINGS_ATTRIBUT");
                    while (sat != null) {
                        if (sat.getAttribute("attribut").equals(nomAttribut)) {
                            final Element eldoc = findElement(sat, "DOCUMENTATION");
                            if (eldoc != null &&eldoc.getFirstChild() != null)
                                return(dom_valeurElement(eldoc));
                            break;
                        }
                        sat = nextElement(sat, "STRINGS_ATTRIBUT");
                    }
                }
                sel = nextElement(sel, "STRINGS_ELEMENT");
            }
        }
        return(getRefConf(refParent).getSchema().documentationAttribut(refAttribut));
    }
    
    /**
     * Renvoie la documentation d'un attribut à partir de sa référence.
     * L'élément parent de l'attribut doit être défini dans le schéma de cette configuration.
     *
     * @deprecated un attribut peut avoir plusieurs parents, utiliser
     * documentationAttribut(final Element refParent, final Element refAttribut)
     * à la place.
     */
    @Deprecated
    public String documentationAttribut(final Element refAttribut) {
        final Element refParent = schema.parentAttribut(refAttribut);
        return(documentationAttribut(refParent, refAttribut));
    }
    
    /**
     * Renvoie le titre d'un export à partir de sa référence
     */
    public String titreExport(final Element refExport) {
        final String nom = nomExport(refExport);
        final ArrayList<Element> lstrings = listeElementsStrings();
        for (final Element strings : lstrings) {
            Element export = findElement(strings, "STRINGS_EXPORT");
            while (export != null) {
                if (nom.equals(export.getAttribute("export"))) {
                    final Element eltitre = findElement(export, "TITRE");
                    if (eltitre != null && eltitre.getFirstChild() != null)
                        return(dom_valeurElement(eltitre));
                    break;
                }
                export = nextElement(export, "STRINGS_EXPORT");
            }
        }
        return(nom);
    }
    
    /**
     * Renvoie la documentation d'un export à partir de sa référence
     */
    public String documentationExport(final Element refExport) {
        final String nom = nomExport(refExport);
        final ArrayList<Element> lstrings = listeElementsStrings();
        for (final Element strings : lstrings) {
            Element export = findElement(strings, "STRINGS_EXPORT");
            while (export != null) {
                if (nom.equals(export.getAttribute("export"))) {
                    final Element eldoc = findElement(export, "DOCUMENTATION");
                    if (eldoc != null && eldoc.getFirstChild() != null)
                        return(dom_valeurElement(eldoc));
                    break;
                }
                export = nextElement(export, "STRINGS_EXPORT");
            }
        }
        return(null);
    }
    
    
    // OUTILS
    
    /**
     * Renvoie la liste des éléments enfants de l'élément donné, avec le nom donné
     */
/*
    private ArrayList<Element> dom_enfantsAvecNom(final Element el, final String nom) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        if (nom == null)
            return(liste);
        Node child = el.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE && nom.equals(child.getNodeName()))
                liste.add((Element)child);
            child = child.getNextSibling();
        }
        return(liste);
    }
*/
    
    /**
     * Renvoie la valeur du premier noeud enfant, en retirant les blancs à gauche et à droite.
     * Renvoie null s'il n'y a pas de noeud enfant.
     */
    private static String dom_valeurElement(final Node el) {
        final Node fc = el.getFirstChild();
        if (fc == null)
            return(null);
        final String v = fc.getNodeValue();
        if (v == null)
            return(null);
        return(v.trim());
    }
    
    protected Element getLangage() {
        if (noeudLangage == null) {
            noeudLangage = findElement(jaxecfg, "LANGAGE");
        }
        return noeudLangage;
    }

    protected Element getEnregistrement() {
        if (noeudEnregistrement == null) {
            noeudEnregistrement = findElement(jaxecfg, "ENREGISTREMENT");
            if (noeudEnregistrement == null) {
                noeudEnregistrement = jaxecfg.getOwnerDocument().createElement("ENREGISTREMENT");
            }
        }
        return noeudEnregistrement;
    }

    protected Element getMenus() {
        if (noeudMenus == null) {
            noeudMenus = findElement(jaxecfg, "MENUS");
            if (noeudMenus == null) {
                noeudMenus = jaxecfg.getOwnerDocument().createElement("MENUS");
            }
        }
        return noeudMenus;
    }

    protected Element getAffichageNoeuds() {
        if (noeudAffichage == null) {
            noeudAffichage = findElement(jaxecfg, "AFFICHAGE_NOEUDS");
            if (noeudAffichage == null) {
                noeudAffichage = jaxecfg.getOwnerDocument().createElement("AFFICHAGE_NOEUDS");
            }
        }
        return noeudAffichage;
    }

    protected Element getExports() {
        if (noeudExports == null) {
            noeudExports = findElement(jaxecfg, "EXPORTS");
            if (noeudExports == null) {
                noeudExports = jaxecfg.getOwnerDocument().createElement("EXPORTS");
            }
        }
        return noeudExports;
    }

    protected List<Element> getStrings() {
        if (listeStrings == null) {
            listeStrings = new ArrayList<Element>();
            Node child = jaxecfg.getFirstChild();
            while (child != null) {
                if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("STRINGS")) {
                    listeStrings.add((Element) child);
                }
                child = child.getNextSibling();
            }
        }
        return listeStrings;
    }

    
    protected static Element findElement(final Node n, final String name) {
        final Node child = n.getFirstChild();
        return nextNode(child, name);
    }
    
    protected static Element nextElement(final Node n, final String name) {
        final Node child = n.getNextSibling();
        return nextNode(child, name);
    }

    private static Element nextNode(Node child, final String name) {
        if (name == null)
            return null;
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
                return (Element) child;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    protected static Element findElementDeep(final Node n, final String name) {
        return nextElementDeep(n, n, name);
    }
    
    protected static Element nextElementDeep(final Node parent, final Node n, final String name) {
        Node current = n;
        Node next;
        while (current != null) {
            if (current.hasChildNodes()) {
                current = (current.getFirstChild());
            } else if (current != parent && null != (next = current.getNextSibling())) {
                current = next;
            } else {
                next = null;
                while (current != parent) {

                    next = current.getNextSibling();
                    if (next != null)
                        break;
                    current = current.getParentNode();
                }
                current = next;
            }
            if (current != parent && current != null && current.getNodeType() == Node.ELEMENT_NODE
                    && current.getNodeName().equals(name)) {
                return (Element) current;
            }
        }
        return null;
    }
}
