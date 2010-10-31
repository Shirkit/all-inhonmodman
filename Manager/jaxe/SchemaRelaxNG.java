/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;


import java.io.IOException;
import java.io.StringReader;
import java.net.*;
import java.util.*;

import org.w3c.dom.*;

import javax.swing.JOptionPane;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Flag;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.xml.sax.DraconianErrorHandler;


/**
 * Schéma Relax NG : validation basée sur Jing
 */
public class SchemaRelaxNG implements InterfaceSchema {
    
    private static final Logger LOG = Logger.getLogger(SchemaRelaxNG.class);
    
    private static final ResourceBundle rb = JaxeResourceBundle.getRB();
    
    private static enum TypeObjetSchema { ELEMENT }
    
    private Config cfg;
    
    private final Element schema; // élément racine du schéma WXS
    private String schemaNamespace; // espace de noms utilisé pour les éléments WXS
    private String targetNamespace; // espace de noms du langage défini dans le schéma
    
    // liste de tous les éléments (pas forcément directement sous xs:schema)
    // (ils peuvent avoir un attribut name ou un attribut ref)
    private final ArrayList<Element> ltouselements;
    
    // éléments avec un nom
    private final ArrayList<Element> elementsNommes;
    
    private final ArrayList<Element> ltousref;
    private final ArrayList<Element> ltousdefine;
    private HashMap<String, String> hashPrefixes = null; // associations espaces de noms -> préfixes
    
    private final ArrayList<URI> fichiersInclus;
    
    private ValidationDriver jingValidation;
    private ValidationDriver jingInsertion;
    
    
    public SchemaRelaxNG(final URL schemaURL, final Config cfg) {
        this.cfg = cfg;
        
        ltouselements = new ArrayList<Element>();
        elementsNommes = new ArrayList<Element>();
        ltousref = new ArrayList<Element>();
        ltousdefine = new ArrayList<Element>();
        hashPrefixes = new HashMap<String, String>();
        fichiersInclus = new ArrayList<URI>();
        
        schema = inclusion1(schemaURL);
        
        for (final Element ref : elementsNommes) {
            final String espace = espaceElement(ref);
            final String prefixe = ref.lookupPrefix(espace);
            if (espace != null && prefixe != null)
                hashPrefixes.put(espace, prefixe);
        }
        
        InputSource inSchema;
        try {
            inSchema = ValidationDriver.uriOrFileInputSource(schemaURL.toExternalForm());
        } catch (MalformedURLException ex) {
            LOG.error("Erreur au chargement du schéma avec Jing", ex);
            inSchema = null;
        }
        
        if (inSchema != null) {
            
            // workaround for JDK 1.6 bug 6301903
            final PropertyMapBuilder prorietes = new PropertyMapBuilder();
            RngProperty.DATATYPE_LIBRARY_FACTORY.put(prorietes,
                new com.thaiopensource.datatype.xsd.DatatypeLibraryFactoryImpl());
            
            prorietes.put(ValidateProperty.ERROR_HANDLER, new DraconianErrorHandler());
            try {
                jingValidation = new ValidationDriver(prorietes.toPropertyMap());
                if (!jingValidation.loadSchema(inSchema))
                    LOG.error("Erreur au chargement du schéma avec Jing (jingValidation)");
                inSchema = ValidationDriver.uriOrFileInputSource(schemaURL.toExternalForm());
                
                prorietes.put(RngProperty.FEASIBLE, Flag.PRESENT);
                jingInsertion = new ValidationDriver(prorietes.toPropertyMap());
                if (!jingInsertion.loadSchema(inSchema))
                    LOG.error("Erreur au chargement du schéma avec Jing (jingInsertion)");
            } catch (SAXException ex) {
                LOG.error("Erreur au chargement du schéma avec Jing", ex);
            } catch (IOException ex) {
                LOG.error("Erreur au chargement du schéma avec Jing", ex);
            }
        }
    }
    
    /**
     * Chargement d'un schéma en mémoire. Appelle inclusion2.
     *
     * @param urls  URL du fichier du schéma WXS
     */
    private Element inclusion1(final URL urls) {
        try {
            final URI uris = urls.toURI().normalize();
            if (fichiersInclus.indexOf(uris) != -1)
                return(null);
            fichiersInclus.add(uris);
        } catch (final URISyntaxException ex) {
            LOG.error("SchemaRelaxNG.inclusion1(URL) : URISyntaxException for " + urls.toString(), ex);
        }
        Document schemadoc;
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder parser = dbf.newDocumentBuilder();
            schemadoc = parser.parse(urls.toExternalForm());
        } catch (final Exception e) {
            LOG.error("inclusion1: lecture de " + urls.toExternalForm(), e);
            e.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null, e.getMessage(),
                rb.getString("erreur.Fatale"), JOptionPane.ERROR_MESSAGE);
            return(null);
        }
        final Element schema2 = schemadoc.getDocumentElement();
        if (schemaNamespace == null)
            schemaNamespace = schema2.getNamespaceURI();
        if (targetNamespace == null)
            targetNamespace = schema2.getAttribute("ns");
        final URL schemadir = getParentURL(urls);
        inclusion2(schema2, schemadir);
        return(schema2);
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
    
    /**
     * Chargement d'un schéma en mémoire. Appelé par inclusion1.
     *
     * @param sch  L'élément racine du schéma WXS
     * @param schemadir  URL du répertoire de référence (dans lequel se trouve le fichier du schéma)
     */
    private void inclusion2(final Element sch, final URL schemadir) {
        
        if ("element".equals(sch.getLocalName()))
            ltouselements.add(sch);
        final ArrayList<Element> nouveauxElements = listeTous(sch, "element");
        ltouselements.addAll(nouveauxElements);
        for (final Element ref : nouveauxElements) {
            if (nomElement(ref) != null)
                elementsNommes.add(ref);
        }
        ltousref.addAll(listeTous(sch, "ref"));
        ltousdefine.addAll(listeTous(sch, "define"));
        
        final ArrayList<Element> linc = listeTous(sch, "include");
        for (final Element inc : linc) {
            final String noms = inc.getAttribute("href");
            URL urls;
            try {
                if (schemadir != null && !noms.startsWith("http://"))
                    urls = new URL(schemadir.toExternalForm() + "/" + noms);
                else
                    urls = new URL(noms);
            } catch (final MalformedURLException ex) {
                LOG.error("include : MalformedURLException: " + ex.getMessage(), ex);
                urls = null;
            }
            inclusion1(urls);
        }
    }
    
    /**
     * Renvoie true si la référence vient de ce schéma
     */
    public boolean elementDansSchema(final Element refElement) {
        return(ltouselements.contains(refElement));
    }
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom donné.
     */
    public Element referenceElement(final String nom) {
        return(chercherPremier(TypeObjetSchema.ELEMENT, nom));
    }
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom et l'espace de noms de l'élément passé en paramètre.
     */
    public Element referenceElement(final Element el) {
        final String nom;
        if (el.getPrefix() == null)
            nom = el.getNodeName();
        else
            nom = el.getLocalName();
        final String espace = el.getNamespaceURI();
        return(referenceElement(nom, espace));
    }
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom et l'espace de noms passés en paramètre.
     */
    private Element referenceElement(final String nom, final String espace) {
        return(chercherPremier(TypeObjetSchema.ELEMENT, nom, espace));
    }
    
    /**
     * Renvoie la référence du premier élément du schéma avec le nom et l'espace de noms de l'élément passé en paramètre,
     * et avec le parent dont la référence est passée en paramètre.
     *
     * Bug avec RelaxNG et les modèles qui dépendent des valeurs des attributs...
     */
    public Element referenceElement(final Element el, final Element refParent) {
        if (refParent == null)
            return(referenceElement(el)); // pour les éléments racine
        final ArrayList<Element> liste = listeSousElements(refParent);
        final String nom = el.getLocalName();
        final String espace = el.getNamespaceURI();
        for (final Element ref : liste) {
            if (nomElement(ref).equals(nom)) {
                final String espaceRef = espaceElement(ref);
                if ((espace == null && espaceRef == null) || (espace != null && espace.equals(espaceRef)))
                    return(ref);
            }
        }
        return(null);
    }
    
    /**
     * Renvoie le nom de l'élément dont la référence est donnée.
     */
    public String nomElement(final Element refElement) {
        if (!"".equals(refElement.getAttribute("name")))
            return(localValue(refElement.getAttribute("name")));
        Node enfant = refElement.getFirstChild();
        while (enfant != null) {
            if (enfant.getNodeType() == Node.ELEMENT_NODE) {
                final String nom = enfant.getNodeName();
                if (nom.equals("name"))
                    return(enfant.getFirstChild().getNodeValue());
            }
            enfant = enfant.getNextSibling();
        }
        return(null);
    }
    
    /**
     * Renvoie l'espace de nom de l'élément dont la référence est passée en paramètre,
     * ou null si l'espace de noms n'est pas défini.
     */
    public String espaceElement(final Element refElement) {
        if (!"".equals(refElement.getAttribute("ns")))
            return(refElement.getAttribute("ns"));
        else if (prefixeString(refElement.getAttribute("name")) != null)
            return(refElement.lookupNamespaceURI(prefixeString(refElement.getAttribute("name"))));
        else if (refElement.getParentNode() != null)
            return(espaceElement((Element)refElement.getParentNode()));
        return(null);
    }
    
    /**
     * Renvoie la documentation d'un élément dont on donne la référence
     * (sous forme de texte simple, ou de HTML 3 pour faire des sauts de lignes)
     */
    public String documentationElement(final Element refElement) {
        Node enfant = refElement.getFirstChild();
        while (enfant != null) {
            if (enfant.getNodeType() == Node.ELEMENT_NODE) {
                final String nom = enfant.getNodeName();
                if (nom.equals("documentation"))
                    return(enfant.getFirstChild().getNodeValue());
            }
            enfant = enfant.getNextSibling();
        }
        return(null);
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un élément, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles ou si l'élément n'a pas un type simple.
     */
    public ArrayList<String> listeValeursElement(final Element refElement) {
        final LinkedHashSet<String> liste = new LinkedHashSet<String>();
        Node enfant = refElement.getFirstChild();
        while (enfant != null) {
            if (enfant.getNodeType() == Node.ELEMENT_NODE) {
                final String nom = enfant.getNodeName();
                if (nom.equals("choice")) {
                    ArrayList<String> liste2 = listeValeursAttribut((Element)enfant);
                    if (liste2 != null)
                        liste.addAll(liste2);
                } else if (nom.equals("value"))
                    liste.add(enfant.getFirstChild().getNodeValue());
            }
            enfant = enfant.getNextSibling();
        }
        if (liste.size() == 0)
            return(null);
        return(new ArrayList<String>(liste));
    }
    
    /**
     * Renvoie le préfixe à utiliser pour créer un élément dont on donne la référence,
     * ou null s'il n'y en a pas.
     */
    public String prefixeElement(final Element refElement) {
        final String espace = espaceElement(refElement);
        if (espace == null)
            return(null);
        return(hashPrefixes.get(espace));
    }
    
    /**
     * Renvoie la liste des espaces de noms (String) utilisés par ce schéma.
     */
    public ArrayList<String> listeEspaces() {
        final LinkedHashSet<String> liste = new LinkedHashSet<String>();
        if (targetNamespace != null)
            liste.add(targetNamespace);
        for (final String s : hashPrefixes.keySet())
            liste.add(s);
        return(new ArrayList<String>(liste));
    }
    
    /**
     * Renvoie true si l'espace de nom est défini dans le schéma
     */
    public boolean aEspace(final String espace) {
        if (espace == null) {
            if (targetNamespace == null || targetNamespace.equals(""))
                return(true);
            if (hashPrefixes.containsKey(""))
                return(true);
        } else {
            if (espace.equals(targetNamespace))
                return(true);
            if (hashPrefixes.containsKey(espace))
                return(true);
        }
        return(false);
    }
    
    /**
     * Renvoie un préfixe à utiliser pour l'espace de noms donné, ou null si aucune suggestion n'est possible
     */
    public String prefixeEspace(final String espace) {
        return(hashPrefixes.get(espace));
    }
    
    /**
     * Renvoie l'espace de noms cible du schéma (attribut targetNamespace avec WXS)
     */
    public String espaceCible() {
        return(targetNamespace);
    }
    
    /**
     * Renvoie les références des éléments qui ne sont pas dans l'espace de noms passé en paramètre
     */
    public ArrayList<Element> listeElementsHorsEspace(final String espace) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        for (final Element el : elementsNommes) {
            final String tns = espaceElement(el);
            if (tns != null && !tns.equals(espace))
                liste.add(el);
        }
        return(liste);
    }
    
    /**
     * Renvoie les références des éléments qui sont dans les espaces de noms passés en paramètre
     */
    public ArrayList<Element> listeElementsDansEspaces(final Set<String> espaces) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        for (final Element el : elementsNommes) {
            final String tns = espaceElement(el);
            if (tns != null && espaces.contains(tns))
                liste.add(el);
        }
        return(liste);
    }
    
    /**
     * Renvoie les références de tous les éléments du schéma
     */
    public ArrayList<Element> listeTousElements() {
        final ArrayList<Element> liste = new ArrayList<Element>();
        for (final Element el : elementsNommes)
            liste.add(el);
        return(liste);
    }
    
    /**
     * Renvoit true si l'enfant est obligatoire sous le parent.
     */
    public boolean elementObligatoire(final Element refParent, final Element refEnfant) {
        // à améliorer
        Node np = refEnfant;
        while (true) {
            np = np.getParentNode();
            if (np == null)
                return(false);
            if ("optional".equals(np.getLocalName()))
                return(false);
            if (np == refParent)
                return(true);
        }
    }
    
    /**
     * Renvoit true si le parent peut avoir des enfants multiples avec la référence refEnfant.
     */
    public boolean enfantsMultiples(final Element refParent, final Element refEnfant) {
        // à améliorer
        return(true);
    }
    
    /**
     * Renvoie les références des éléments enfants de l'élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeSousElements(final Element refParent) {
        final LinkedHashSet<Element> liste = new LinkedHashSet<Element>();
        
        Node enfant = refParent.getFirstChild();
        while (enfant != null) {
            if (enfant.getNodeType() == Node.ELEMENT_NODE) {
                final String nom = enfant.getNodeName();
                if (nom.equals("element") && !"".equals(((Element)enfant).getAttribute("name")))
                    liste.add((Element)enfant);
                else if (nom.equals("zeroOrMore") || nom.equals("oneOrMore") || nom.equals("choice") ||
                        nom.equals("group") || nom.equals("optional") || nom.equals("interleave"))
                    liste.addAll(listeSousElements((Element)enfant));
                else if (nom.equals("ref")) {
                    final String nomRef = ((Element)enfant).getAttribute("name");
                    for (final Element def : ltousdefine) {
                        if (def.getAttribute("name").equals(nomRef))
                            liste.addAll(listeSousElements(def));
                    }
                } else if (nom.equals("anyName")) {
                    final ArrayList<Element> listeAny = new ArrayList<Element>();
                    listeAny.addAll(elementsNommes);
                    final ArrayList<Element> lexcept = enfants((Element)enfant, "except");
                    for (final Element except : lexcept) {
                        final ArrayList<Element> l = listeSousElements(except);
                        listeAny.removeAll(l);
                    }
                    liste.addAll(listeAny);
                } else if (nom.equals("nsName")) {
                    String ns = ((Element)enfant).getAttribute("ns");
                    if ("".equals(ns)) {
                        Node nparent = enfant.getParentNode();
                        while (nparent != null) {
                            if (!"".equals(((Element)nparent).getAttribute("ns"))) {
                                ns = ((Element)nparent).getAttribute("ns");
                                break;
                            }
                            nparent = nparent.getParentNode();
                        }
                    }
                    final HashSet<String> espaces = new HashSet<String>();
                    espaces.add(ns);
                    liste.addAll(listeElementsDansEspaces(espaces));
                }
            }
            enfant = enfant.getNextSibling();
        }
        
        return(new ArrayList<Element>(liste));
    }
    
    /**
     * Expression régulière correspondant au schéma pour un élément parent donné
     * @param modevisu  True si on cherche une expression régulière à afficher pour l'utilisateur
     * @param modevalid  Pour obtenir une validation stricte au lieu de chercher si une insertion est possible
     */
    public String expressionReguliere(final Element refParent, final boolean modevisu, final boolean modevalid) {
        final ArrayList<Element> lsousb = listeSousElements(refParent);
        final StringBuilder expr = new StringBuilder();
        final int s = lsousb.size();
        for (int i=0; i < s; i++) {
            if (i != 0)
                expr.append("|");
            if (modevisu)
                expr.append(cfg.titreElement(lsousb.get(i)));
            else
                expr.append(nomElement(lsousb.get(i)));
            if (!modevisu)
                expr.append(",");
        }
        if (s != 0) {
            expr.insert(0, "(");
            expr.append(")*");
        }
        return(expr.toString());
    }
    
    /**
     * Renvoie true si le document DOM domdoc est valide.
     * Si insertion est true, teste juste la validité d'une insertion (tous les éléments sont optionnels).
     */
    public boolean documentValide(final Document domdoc, final boolean insertion) {
        final String sdoc = JaxeDocument.DOMVersXML(domdoc);
        final StringReader reader = new StringReader(sdoc);
        final InputSource in = new InputSource(reader);
        final ValidationDriver jingDriver;
        if (insertion)
            jingDriver = jingInsertion;
        else
            jingDriver = jingValidation;
        try {
            return(jingDriver.validate(in));
        } catch (SAXException ex) {
            //LOG.error("Validation Jing : SAXException", ex);
            return(false);
        } catch (IOException ex) {
            LOG.error("Validation Jing : IOException", ex);
            return(false);
        }
    }
    
    /**
     * Renvoie la liste des références des parents possibles pour un élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeElementsParents(final Element refElement) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        if (refElement.getParentNode() == null)
            return(liste);
        final Element parent = (Element)refElement.getParentNode();
        final String nomParent = parent.getNodeName();
        if ("element".equals(nomParent))
            liste.add(parent);
        else if (nomParent.equals("zeroOrMore") || nomParent.equals("oneOrMore") ||
                nomParent.equals("choice") || nomParent.equals("group")  ||
                nomParent.equals("optional") || nomParent.equals("interleave"))
            liste.addAll(listeElementsParents(parent));
        else if (nomParent.equals("define")) {
            final String nomdef = parent.getAttribute("name");
            for (final Element ref : ltousref) {
                if (nomdef.equals(ref.getAttribute("name")))
                    liste.addAll(listeElementsParents(ref));
            }
        }
        return(liste);
    }
    
    /**
     * Renvoie la liste des références des attributs possibles pour un élément dont
     * on donne la référence en paramètre
     */
    public ArrayList<Element> listeAttributs(final Element refElement) {
        final LinkedHashSet<Element> liste = new LinkedHashSet<Element>();
        Node enfant = refElement.getFirstChild();
        while (enfant != null) {
            if (enfant.getNodeType() == Node.ELEMENT_NODE) {
                final String nom = enfant.getNodeName();
                if (nom.equals("attribute"))
                    liste.add((Element)enfant);
                else if (nom.equals("optional") || nom.equals("choice"))
                    liste.addAll(listeAttributs((Element)enfant));
                else if (nom.equals("ref")) {
                    final String nomRef = ((Element)enfant).getAttribute("name");
                    for (final Element def : ltousdefine) {
                        if (def.getAttribute("name").equals(nomRef))
                            liste.addAll(listeAttributs(def));
                    }
                }
            }
            enfant = enfant.getNextSibling();
        }
        return(new ArrayList<Element>(liste));
    }
    
    /**
     * Renvoie le nom d'un attribut à partir de sa référence
     */
    public String nomAttribut(final Element refAttribut) {
        return(refAttribut.getAttribute("name"));
    }
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de sa référence, ou null si aucun n'est défini
     */
    public String espaceAttribut(final Element refAttribut) {
        if (!"".equals(refAttribut.getAttribute("ns")))
            return(refAttribut.getAttribute("ns"));
        return(null);
    }
    
    /**
     * Renvoie la documentation d'un attribut à partir de sa référence
     */
    public String documentationAttribut(final Element refAttribut) {
        return(null);
    }
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de son nom complet (avec le préfixe s'il y en a un)
     */
    public String espaceAttribut(final String nomAttribut) {
        if (nomAttribut == null)
            return(null);
        final String prefixe = prefixeString(nomAttribut);
        if (prefixe == null)
            return(null);
        if ("xml".equals(prefixe))
            return("http://www.w3.org/XML/1998/namespace");
        final String espace = schema.getAttributeNS("http://www.w3.org/2000/xmlns/", prefixe);
        return(espace);
    }
    
    /**
     * Renvoie true si un attribut est obligatoire, à partir de sa définition
     */
    public boolean estObligatoire(final Element refAttribut) {
        final Element parent = (Element)refAttribut.getParentNode();
        return(!"optional".equals(parent.getLocalName()));
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un attribut, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     */
    public ArrayList<String> listeValeursAttribut(final Element refAttribut) {
        final LinkedHashSet<String> liste = new LinkedHashSet<String>();
        Node enfant = refAttribut.getFirstChild();
        while (enfant != null) {
            if (enfant.getNodeType() == Node.ELEMENT_NODE) {
                final String nom = enfant.getNodeName();
                if (nom.equals("choice")) {
                    ArrayList<String> liste2 = listeValeursAttribut((Element)enfant);
                    if (liste2 != null)
                        liste.addAll(liste2);
                } else if (nom.equals("value"))
                    liste.add(enfant.getFirstChild().getNodeValue());
            }
            enfant = enfant.getNextSibling();
        }
        if (liste.size() == 0)
            return(null);
        return(new ArrayList<String>(liste));
    }
    
    /**
     * Renvoie la valeur par défaut d'un attribut dont la référence est donnée en paramètre
     */
    public String valeurParDefaut(final Element refAttribut) {
        return(null);
    }
    
    /**
     * Renvoie true si la valeur donnée est une valeur valide pour l'attribut
     */
    public boolean attributValide(final Element refAttribut, final String valeur) {
        if ((valeur == null || "".equals(valeur)) && estObligatoire(refAttribut))
            return(false);
        return(true);
    }
    
    /**
     * Renvoie la référence du premier élément parent d'un attribut à partir de sa référence
     */
    public Element parentAttribut(final Element refAttribut) {
        return((Element)refAttribut.getParentNode());
    }
    
    /**
     * Renvoie true si l'élément dont on donne la référence peut contenir du texte
     */
    public boolean contientDuTexte(final Element refElement) {
        Node enfant = refElement.getFirstChild();
        while (enfant != null) {
            if (enfant.getNodeType() == Node.ELEMENT_NODE) {
                final String nom = enfant.getNodeName();
                if (nom.equals("text") || nom.equals("data") || nom.equals("mixed"))
                    return(true);
                else if (nom.equals("ref")) {
                    final String nomRef = ((Element)enfant).getAttribute("name");
                    for (final Element def : ltousdefine) {
                        if (def.getAttribute("name").equals(nomRef))
                            if (contientDuTexte(def))
                                return(true);
                    }
                } else if (nom.equals("interleave") || nom.equals("choice") || nom.equals("group") ||
                        nom.equals("zeroOrMore") || nom.equals("oneOrMore"))
                    if (contientDuTexte((Element)enfant))
                        return(true);
            }
            enfant = enfant.getNextSibling();
        }
        return(false);
    }
    
    /**
     * Renvoie le premier objet du schéma avec le type, le nom et l'espace de noms donnés,
     * ou null si rien de correspondant n'est trouvé.
     */
    private Element chercherPremier(final TypeObjetSchema type, final String nom, final String espace) {
        if (nom == null)
            return(null);
        switch(type) {
            case ELEMENT:
                for (final Element ref : elementsNommes) {
                    if (nom.equals(nomElement(ref))) {
                        final String espaceRef = espaceElement(ref);
                        if ((espace == null && espaceRef == null) || (espace != null && espace.equals(espaceRef)))
                            return(ref);
                    }
                }
                return(null);
            default :
                return(null);
        }
    }
    
    /**
     * Renvoie le premier objet du schéma avec le type et le nom donnés,
     * ou null si rien de correspondant n'est trouvé.
     */
    private Element chercherPremier(final TypeObjetSchema type, final String nom) {
        if (nom == null)
            return(null);
        switch(type) {
            case ELEMENT:
                for (final Element ref : elementsNommes) {
                    if (nom.equals(nomElement(ref)))
                        return(ref);
                }
                return(null);
            default :
                return(null);
        }
    }
    
    /**
     * Renvoie la partie locale du nom d'un élément (en retirant le préfixe)
     */
    private static String localValue(final String s) {
        if (s == null)
            return(null);
        final int ind = s.indexOf(':');
        if (ind == -1)
            return(s);
        return(s.substring(ind + 1));
    }
    
    /**
     * Renvoie le préfixe d'un nom, ou null s'il n'en a pas.
     */
    private static String prefixeString(final String nom) {
        final int indp = nom.indexOf(':');
        if (indp == -1)
            return(null);
        else
            return(nom.substring(0, indp));
    }
    
    /**
     * Ajoute tous les éléments d'une NodeList à une ArrayList de Element, en supposant que
     * tous les éléments de la NodeList sont des org.w3c.dom.Element.
     */
    private static void addNodeList(final ArrayList<Element> l, final NodeList nl) {
        for (int i=0; i<nl.getLength(); i++)
            l.add((Element)nl.item(i)); // attention au cast
    }
    
    /**
     * Renvoie la liste des éléments enfants d'un élément donné ayant un nom donné
     * (getElementsByTagName renvoit tous les descendants, alors qu'ici on cherche juste les enfants de premier niveau)
     */
    private ArrayList<Element> enfants(final Element parent, final String tag) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        Node item = parent.getFirstChild();
        while (item != null) {
            if (item instanceof Element) {
                final Element sousb = (Element)item;
                if (tag.equals(sousb.getLocalName()))
                    liste.add(sousb);
            }
            item = item.getNextSibling();
        }
        return(liste);
    }
    
    /**
     * Renvoie une ArrayList avec tous les éléments descendants d'un élément parent ayant un nom donné.
     */
    private ArrayList<Element> listeTous(final Element parent, final String tag) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        final NodeList lbalises = parent.getElementsByTagNameNS(schemaNamespace, tag);
        addNodeList(liste, lbalises);
        return(liste);
    }
    
}
