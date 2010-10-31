/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import javax.swing.JOptionPane;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.w3c.dom.*;

import org.apache.log4j.Logger;


/**
 * Utilisation d'un schéma W3C
 */
public class SchemaW3C implements InterfaceSchema {
    
    private static final Logger LOG = Logger.getLogger(SchemaW3C.class);
    
    private static final ResourceBundle rb = JaxeResourceBundle.getRB();
    
    private static final String newline = Jaxe.newline;
    
    private static enum TypeObjetSchema { ELEMENT, ATTRIBUTE, COMPLEXTYPE, SIMPLETYPE, GROUP, ATTRIBUTEGROUP }
    
    private final Config cfg;
    
    private final Element schema; // élément racine du schéma WXS
    private String schemaNamespace; // espace de noms utilisé pour les éléments WXS
    private String targetNamespace; // espace de noms du langage défini dans le schéma
    
    // liste des éléments directement sous xs:schema
    private final ArrayList<Element> ltopelements;
    
    // listes de tous les element, group, complexType, simpleType et attributeGroup directement sous xs:schema
    // (ils doivent avoir un attribut name)
    // table hash name -> reference
    private final HashMap<String, ArrayList<Element>> htopelements;
    private final HashMap<String, ArrayList<Element>> htopattributs;
    private final HashMap<String, ArrayList<Element>> htopgroups;
    private final HashMap<String, ArrayList<Element>> htopcomptypes;
    private final HashMap<String, ArrayList<Element>> htopsimptypes;
    private final HashMap<String, ArrayList<Element>> htopattgroups;
    
    // liste de tous les éléments (pas forcément directement sous xs:schema)
    // (ils peuvent avoir un attribut name ou un attribut ref)
    private final ArrayList<Element> ltouselements;
    private final ArrayList<Element> ltousgroups;
    private final ArrayList<Element> ltousattgroups;
    private final ArrayList<Element> ltousextensions;
    
    private final HashMap<Element, ArrayList<Element>> substitutions; // liens head -> substitutionGroup
    
    private final HashMap<Element, Set<Element>> cacheSubst; // cache pour ajSubst
    private HashMap<String, String> hashPrefixes = null; // associations espaces de noms -> préfixes
    private HashMap<Element, ArrayList<Element>> cacheAttributs = null; // cache référence -> liste d'attributs
    private HashMap<Element, ArrayList<Element>> cacheSousElements = null; // cache référence -> liste de sous-éléments
    
    private final ArrayList<URI> fichiersInclus;
    
    
    public SchemaW3C(final URL schemaURL, final Config cfg) {
        this.cfg = cfg;
        
        hashPrefixes = new HashMap<String, String>();
        cacheAttributs = new HashMap<Element, ArrayList<Element>>();
        cacheSousElements = new HashMap<Element, ArrayList<Element>>();
        
        ltopelements = new ArrayList<Element>();
        
        htopelements = new HashMap<String, ArrayList<Element>>();
        htopattributs = new HashMap<String, ArrayList<Element>>();
        htopgroups = new HashMap<String, ArrayList<Element>>();
        htopcomptypes = new HashMap<String, ArrayList<Element>>();
        htopsimptypes = new HashMap<String, ArrayList<Element>>();
        htopattgroups = new HashMap<String, ArrayList<Element>>();
        
        ltouselements = new ArrayList<Element>();
        ltousgroups = new ArrayList<Element>();
        ltousextensions = new ArrayList<Element>();
        ltousattgroups = new ArrayList<Element>();
        
        substitutions = new HashMap<Element, ArrayList<Element>>();
        
        fichiersInclus = new ArrayList<URI>();
        schemaNamespace = null;
        targetNamespace = null;
        schema = inclusion1(schemaURL);
        
        cacheSubst = new HashMap<Element, Set<Element>>();
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
     */
    public Element referenceElement(final Element el, final Element refParent) {
        if (refParent == null)
            return(referenceElement(el)); // pour les éléments racine
        final ArrayList<Element> liste = listeSousElements(refParent);
        final String nom = el.getLocalName();
        final String espace = el.getNamespaceURI();
        for (final Element ref : liste) {
            if (ref.getAttribute("name").equals(nom)) {
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
        return(refElement.getAttribute("name"));
    }
    
    /**
     * Renvoie l'espace de nom de l'élément dont la référence est passée en paramètre,
     * ou null si l'espace de noms n'est pas défini.
     */
    public String espaceElement(final Element refElement) {
        final Element schemael = refElement.getOwnerDocument().getDocumentElement();
        boolean qualified;
        if (refElement.getParentNode() == schemael)
            qualified = true;
        else if (!"".equals(refElement.getAttribute("form")))
            qualified = "qualified".equals(refElement.getAttribute("form"));
        else
            qualified = "qualified".equals(schemael.getAttribute("elementFormDefault"));
        if (qualified) {
            final String tn = schemael.getAttribute("targetNamespace");
            if ("".equals(tn))
                return(null);
            else
                return(tn);
        } else
            return(null);
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
     * Renvoie la documentation d'un élément dont on donne la référence
     * (sous forme de texte simple, ou de HTML 3 pour faire des sauts de lignes)
     */
    public String documentationElement(final Element refElement) {
        return(sDocumentation(refElement));
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un élément, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles ou si l'élément n'a pas un type simple.
     */
    public ArrayList<String> listeValeursElement(final Element refElement) {
        if (!"".equals(refElement.getAttribute("fixed"))) {
            final ArrayList<String> fixedval = new ArrayList<String>();
            fixedval.add(refElement.getAttribute("fixed"));
            return(fixedval);
        }
        final ArrayList<Element> lst = enfants(refElement, "simpleType");
        if (lst.size() > 0)
            return(enumerationDuType(lst.get(0)));
        else if (!"".equals(refElement.getAttribute("type"))) {
            final String type = refElement.getAttribute("type");
            final String tns = refElement.lookupNamespaceURI(prefixeString(type));
            return(enumerationDuType(type, tns));
        } else
            return(null);
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
            // cas des éléments locaux sans espace de noms :
            final boolean qualified = "qualified".equals(schema.getAttribute("elementFormDefault"));
            if (!qualified)
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
        for (final Element el : ltouselements) {
            if (!"".equals(el.getAttribute("name")) && !"true".equals(localValue(el.getAttribute("abstract")))) {
                final String tns = espaceElement(el);
                if (tns != null && !tns.equals(espace))
                    liste.add(el);
            }
        }
        return(liste);
    }
    
    /**
     * Renvoie les références des éléments qui sont dans les espaces de noms passés en paramètre
     */
    public ArrayList<Element> listeElementsDansEspaces(final Set<String> espaces) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        for (final Element el : ltouselements) {
            if (!"".equals(el.getAttribute("name")) && !"true".equals(localValue(el.getAttribute("abstract")))) {
                final String tns = espaceElement(el);
                if (tns != null && espaces.contains(tns))
                    liste.add(el);
            }
        }
        return(liste);
    }
    
    /**
     * Renvoie les références de tous les éléments du schéma
     */
    public ArrayList<Element> listeTousElements() {
        final ArrayList<Element> liste = new ArrayList<Element>();
        for (final Element el : ltouselements) {
            if (!"".equals(el.getAttribute("name")) && !"true".equals(localValue(el.getAttribute("abstract"))))
                liste.add(el);
        }
        return(liste);
    }
    
    /**
     * Renvoit true si l'enfant est obligatoire sous le parent.
     */
    public boolean elementObligatoire(final Element refParent, final Element refEnfant) {
        final String expreg = expressionReguliere(refParent, true, false);
        if (expreg == null)
            return(false);
        final String expnom1 = cfg.titreElement(refEnfant);
        final String expnom2 = "(" + cfg.titreElement(refEnfant) + ")";
        return(expreg.indexOf(expnom1 + "?") == -1 && expreg.indexOf(expnom1 + "*") == -1 &&
            expreg.indexOf(expnom2 + "?") == -1 && expreg.indexOf(expnom2 + "*") == -1);
    }
    
    /**
     * Renvoit true si le parent peut avoir des enfants multiples avec la référence refEnfant.
     */
    public boolean enfantsMultiples(final Element refParent, final Element refEnfant) {
        final String expreg = expressionReguliere(refParent, true, false);
        if (expreg == null)
            return(true);
        final String expnom1 = cfg.titreElement(refEnfant);
        final String expnom2 = "(" + cfg.titreElement(refEnfant) + ")";
        return(expreg.indexOf(expnom1 + "+") != -1 || expreg.indexOf(expnom1 + "*") != -1 ||
            expreg.indexOf(expnom2 + "+") != -1 || expreg.indexOf(expnom2 + "*") != -1);
    }
    
    /**
     * Renvoie les références des éléments enfants de l'élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeSousElements(final Element refParent) {
        ArrayList<Element> al = cacheSousElements.get(refParent);
        if (al != null)
            return(al);
        
        final Set<Element> liste = new LinkedHashSet<Element>();
        
        final String nombalise = refParent.getLocalName();
        if (nombalise.equals("element") && !"".equals(refParent.getAttribute("type"))) {
            final String type = refParent.getAttribute("type");
            final String stype = localValue(type);
            final String tns = refParent.lookupNamespaceURI(prefixeString(type));
            final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, stype, tns);
            if (ct != null)
                liste.addAll(listeSousElements(ct));
        } else if (nombalise.equals("element") && !"".equals(refParent.getAttribute("substitutionGroup")) &&
                enfants(refParent, "simpleType").size() == 0 && enfants(refParent, "complexType").size() == 0) {
            final String substitutionGroup = refParent.getAttribute("substitutionGroup");
            final String tns = refParent.lookupNamespaceURI(prefixeString(substitutionGroup));
            final Element refel = chercherPremier(TypeObjetSchema.ELEMENT, localValue(substitutionGroup), tns);
            if (refel != null)
                liste.addAll(listeSousElements(refel));
        } else if (nombalise.equals("group") && !"".equals(refParent.getAttribute("ref"))) {
            final String ref = refParent.getAttribute("ref");
            final String sref = localValue(ref);
            final String tns = refParent.lookupNamespaceURI(prefixeString(ref));
            final Element g = chercherPremier(TypeObjetSchema.GROUP, sref, tns);
            if (g != null)
                liste.addAll(listeSousElements(g));
        } else if (nombalise.equals("any")) {
            final String namespace = localValue(refParent.getAttribute("namespace"));
            if ("".equals(namespace) || "##any".equals(namespace)) {
                for (final Element el : ltouselements)
                    if (!"".equals(el.getAttribute("name")) && !"true".equals(localValue(el.getAttribute("abstract"))))
                        liste.add(el);
            } else if ("##local".equals(namespace)) {
                for (final Element el : ltouselements) {
                    if (!"".equals(el.getAttribute("name")) && !"true".equals(localValue(el.getAttribute("abstract")))) {
                        final String tns = espaceElement(el);
                        if (tns == null || tns.equals(targetNamespace))
                            liste.add(el);
                    }
                }
            } else if ("##other".equals(namespace)) {
                final String tns = espaceElement(refParent);
                liste.addAll(cfg.listeElementsHorsEspace(tns));
            } else {
                // liste d'espaces de noms séparés par des espaces
                final HashSet<String> espaces = new HashSet<String>(Arrays.asList(namespace.split("\\s")));
                if (espaces.contains("##targetNamespace")) {
                    espaces.remove("##targetNamespace");
                    espaces.add(targetNamespace);
                }
                if (espaces.contains("##local")) {
                    espaces.remove("##local");
                    espaces.add("");
                }
                liste.addAll(cfg.listeElementsDansEspaces(espaces));
            }
        } else {
            if (nombalise.equals("extension") && !"".equals(refParent.getAttribute("base"))) {
                final String base = refParent.getAttribute("base");
                final String sbase = localValue(base);
                final String tns = refParent.lookupNamespaceURI(prefixeString(base));
                final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, sbase, tns);
                if (ct != null)
                    liste.addAll(listeSousElements(ct));
            }
            Node item = refParent.getFirstChild();
            while (item != null) {
                if (item instanceof Element) {
                    final Element sousb = (Element)item;
                    if (sousb.getLocalName().equals("element")) {
                        final String sname = sousb.getAttribute("name");
                        if (!"".equals(sname)) {
                            ajSubst(sousb, sname, liste);
                        } else if (!"".equals(sousb.getAttribute("ref"))) {
                            final String ref = sousb.getAttribute("ref");
                            final String sref = localValue(ref);
                            final String tns = sousb.lookupNamespaceURI(prefixeString(ref));
                            final Element refel = chercherPremier(TypeObjetSchema.ELEMENT, sref, tns);
                            if (refel != null)
                                ajSubst(refel, sref, liste);
                        }
                        // sinon cas bizarre
                    } else if (!sousb.getLocalName().equals("attribute") && !sousb.getLocalName().equals("simpleType"))
                        liste.addAll(listeSousElements(sousb));
                }
                item = item.getNextSibling();
            }
        }
        al = new ArrayList<Element>(liste);
        cacheSousElements.put(refParent, al);
        return(al);
    }
    
    /**
     * Expression régulière correspondant au schéma pour un élément parent donné
     * @param modevisu  True si on cherche une expression régulière à afficher pour l'utilisateur
     * @param modevalid  Pour obtenir une validation stricte au lieu de chercher si une insertion est possible
     */
    public String expressionReguliere(final Element refParent, final boolean modevisu, final boolean modevalid) {
        final boolean[] annulable = new boolean[1];
        return(expressionReguliere(refParent, 1, false, modevisu, false, 0, modevalid, annulable));
    }
    
    /**
     * Renvoie la liste des références des parents possibles pour un élément dont la référence est passée en paramètre
     */
    public ArrayList<Element> listeElementsParents(final Element refElement) {
        final Set<Element> liste = new LinkedHashSet<Element>();
        final String bdefname = refElement.getAttribute("name");
        final String espaceEnfant = espaceElement(refElement);
        for (final Element sousb : ltouselements) {
            boolean corresp = false;
            if (bdefname.equals(sousb.getAttribute("name"))) {
                final String tns = espaceElement(sousb);
                if ((tns == null && espaceEnfant == null) || (tns != null && tns.equals(espaceEnfant)))
                    corresp = true;
            } else if (bdefname.equals(localValue(sousb.getAttribute("ref")))) {
                final String tns = sousb.lookupNamespaceURI(prefixeString(sousb.getAttribute("ref")));
                if ((tns == null && espaceEnfant == null) || (tns != null && tns.equals(espaceEnfant)))
                    corresp = true;
            }
            if (corresp) {
                final Element parent = (Element)sousb.getParentNode();
                if (parent.getLocalName().equals("element"))
                    liste.add(parent);
                else
                    liste.addAll(sParents(parent));
                if (!"".equals(refElement.getAttribute("substitutionGroup"))) {
                    final String substitutionGroup = refElement.getAttribute("substitutionGroup");
                    final String tns = refElement.lookupNamespaceURI(prefixeString(substitutionGroup));
                    final Element refel = chercherPremier(TypeObjetSchema.ELEMENT, localValue(substitutionGroup), tns);
                    if (refel != null)
                        liste.addAll(listeElementsParents(refel));
                }
            }
        }
        return(new ArrayList<Element>(liste));
    }
    
    /**
     * Renvoie la liste des références des attributs possibles pour un élément dont
     * on donne la référence en paramètre.
     * Les références sont des élément "attribute" avec un attribut name ou un attribut ref.
     * Dans le cas c'est une référence, on peut obtenir l'élément de définition de l'attribut
     * avec la méthode definitionAttribut.
     */
    public ArrayList<Element> listeAttributs(final Element refElement) {
        ArrayList<Element> liste = cacheAttributs.get(refElement);
        if (liste != null)
            return(liste);
        liste = new ArrayList<Element>();
        final String nombalise = refElement.getLocalName();
        if (nombalise.equals("element") && !"".equals(refElement.getAttribute("type"))) {
            final String type = refElement.getAttribute("type");
            final String stype = localValue(type);
            final String tns = refElement.lookupNamespaceURI(prefixeString(type));
            final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, stype, tns);
            if (ct != null)
                liste.addAll(sCtAttributs(ct, null));
        } else if (nombalise.equals("element") && !"".equals(refElement.getAttribute("substitutionGroup")) &&
                enfants(refElement, "simpleType").size() == 0 && enfants(refElement, "complexType").size() == 0) {
            final String substitutionGroup = refElement.getAttribute("substitutionGroup");
            final String tns = refElement.lookupNamespaceURI(prefixeString(substitutionGroup));
            final Element refel = chercherPremier(TypeObjetSchema.ELEMENT, localValue(substitutionGroup), tns);
            if (refel != null)
                liste.addAll(listeAttributs(refel));
        } else {
            Node n = refElement.getFirstChild();
            while (n != null) {
                if (n instanceof Element && n.getLocalName().equals("complexType"))
                    liste.addAll(sCtAttributs((Element)n, null));
                n = n.getNextSibling();
            }
        }
        cacheAttributs.put(refElement, liste);
        return(liste);
    }
    
    /**
     * Renvoie l'élément de définition d'un attribut à partir de sa référence
     */
    public Element definitionAttribut(final Element refAttribut) {
        if (!"".equals(refAttribut.getAttribute("ref"))) {
            final String ref = refAttribut.getAttribute("ref");
            String tns = refAttribut.lookupNamespaceURI(prefixeString(ref));
            if (tns == null && "xml".equals(prefixeString(ref)))
                tns = "http://www.w3.org/XML/1998/namespace";
            final Element att = chercherPremier(TypeObjetSchema.ATTRIBUTE, localValue(ref), tns);
            if (att != null)
                return(att);
        }
        return(refAttribut);
    }
    
    /**
     * Renvoie le nom d'un attribut à partir de sa référence (sans le préfixe)
     */
    public String nomAttribut(final Element refAttribut) {
        if (!"".equals(refAttribut.getAttribute("name")))
            return(refAttribut.getAttribute("name"));
        return(localValue(refAttribut.getAttribute("ref")));
    }
    
    /**
     * Renvoie l'espace de noms d'un attribut à partir de sa référence, ou null si aucun n'est défini
     */
    public String espaceAttribut(final Element refAttribut) {
        if (!"".equals(refAttribut.getAttribute("ref"))) {
            final String ref = refAttribut.getAttribute("ref");
            final String prefixe = prefixeString(ref);
            if (prefixe != null) {
                final String ns = refAttribut.lookupNamespaceURI(prefixe);
                if (ns != null)
                    return(ns);
                if ("xml".equals(prefixe))
                    return("http://www.w3.org/XML/1998/namespace");
                return(null);
            }
        }
        final Element schemael = refAttribut.getOwnerDocument().getDocumentElement();
        boolean qualified;
        if (refAttribut.getParentNode() == schemael)
            qualified = true;
        else if (!"".equals(refAttribut.getAttribute("form")))
            qualified = "qualified".equals(refAttribut.getAttribute("form"));
        else
            qualified = "qualified".equals(schemael.getAttribute("attributeFormDefault"));
        if (qualified) {
            final String tn = schemael.getAttribute("targetNamespace");
            if ("".equals(tn))
                return(null);
            else
                return(tn);
        } else
            return(null);
    }
    
    /**
     * Renvoie la documentation d'un attribut à partir de sa référence
     */
    public String documentationAttribut(final Element refAttribut) {
        return(sDocumentation(refAttribut));
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
        final String presence = refAttribut.getAttribute("use");
        return("required".equals(presence));
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un type simple ou complexe.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     * Paramètres : nom du type (potentiellement avec le préfixe), et espace de noms
     */
    private ArrayList<String> enumerationDuType(final String type, final String tns) {
        final String stype = localValue(type);
        if (schemaNamespace.equals(tns)) {
            if ("boolean".equals(stype)) {
                final String[] tvalbool = {"true", "false", "1", "0"};
                final ArrayList<String> valbool = new ArrayList<String>(Arrays.asList(tvalbool));
                return(valbool);
            } else if (!schemaNamespace.equals(targetNamespace))
                return(null);
        }
        final Element st = chercherPremier(TypeObjetSchema.SIMPLETYPE, stype, tns);
        if (st != null)
            return(enumerationDuType(st));
        final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, stype, tns);
        if (ct != null) {
            final ArrayList<Element> lsc = enfants(ct, "simpleContent");
            if (lsc.size() > 0) {
                final Element simpleContent = lsc.get(0);
                final ArrayList<Element> lext = enfants(simpleContent, "extension");
                if (lext.size() > 0) {
                    final Element extension = lext.get(0);
                    final String type_ext = extension.getAttribute("base");
                    final String tns_ext = extension.lookupNamespaceURI(prefixeString(type_ext));
                    return(enumerationDuType(type_ext, tns_ext));
                } else
                    return(enumerationDuType(simpleContent));
            } else
                return(null);
        } else {
            LOG.error("enumerationDuType(String, String): type introuvable: " + type + " " + tns);
            return(null);
        }
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un type simple.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     */
    private ArrayList<String> enumerationDuType(final Element simpleType) {
        final ArrayList<Element> lrest = enfants(simpleType, "restriction");
        if (lrest.size() > 0) {
            final Element restriction = lrest.get(0);
            final ArrayList<Element> lenum = enfants(restriction, "enumeration");
            if (lenum.size() > 0) {
                final ArrayList<String> liste = new ArrayList<String>();
                for (Element enumeration : lenum) {
                    final String sval = enumeration.getAttribute("value");
                    liste.add(sval);
                }
                return(liste);
            } else
                return(null);
        }
        final ArrayList<Element> lunion = enfants(simpleType, "union");
        if (lunion.size() > 0) {
            final Element union = lunion.get(0);
            final ArrayList<String> liste = new ArrayList<String>();
            final ArrayList<Element> lst = enfants(union, "simpleType");
            for (Element st : lst) {
                final ArrayList<String> listest = enumerationDuType(st);
                if (listest == null)
                    return(null);
                liste.addAll(listest);
            }
            final String memberTypes = union.getAttribute("memberTypes");
            if (!"".equals(memberTypes)) {
                final String[] types = memberTypes.split("\\s");
                for (String type : types) {
                    final String tns = simpleType.lookupNamespaceURI(prefixeString(type));
                    final ArrayList<String> listest = enumerationDuType(type, tns);
                    if (listest == null)
                        return(null);
                    liste.addAll(listest);
                }
            }
            return(liste);
        }
        return(null);
    }
    
    /**
     * Renvoie la liste des valeurs possibles pour un attribut, à partir de sa référence.
     * Renvoie null s'il y a un nombre infini de valeurs possibles.
     */
    public ArrayList<String> listeValeursAttribut(final Element refAttribut) {
        if (!"".equals(refAttribut.getAttribute("fixed"))) {
            final ArrayList<String> fixedval = new ArrayList<String>();
            fixedval.add(refAttribut.getAttribute("fixed"));
            return(fixedval);
        }
        final ArrayList<Element> lst = enfants(refAttribut, "simpleType");
        if (lst.size() > 0)
            return(enumerationDuType(lst.get(0)));
        else if (!"".equals(refAttribut.getAttribute("type"))) {
            final String type = refAttribut.getAttribute("type");
            final String tns = refAttribut.lookupNamespaceURI(prefixeString(type));
            return(enumerationDuType(type, tns));
        } else {
            if (!"".equals(refAttribut.getAttribute("ref"))) {
                final Element defAttribut = definitionAttribut(refAttribut);
                if (defAttribut != refAttribut)
                    return(listeValeursAttribut(defAttribut));
            }
            return(null);
        }
    }
    
    /**
     * Renvoie la valeur par défaut d'un attribut dont la référence est donnée en paramètre
     */
    public String valeurParDefaut(final Element refAttribut) {
        if ("".equals(refAttribut.getAttribute("default")))
            return(null);
        return(refAttribut.getAttribute("default"));
    }
    
    /**
     * Renvoie true si la valeur donnée est une valeur valide pour l'attribut
     */
    public boolean attributValide(final Element refAttribut, final String valeur) {
        Element defAttribut = refAttribut;
        if (!"".equals(refAttribut.getAttribute("ref")))
            defAttribut = definitionAttribut(refAttribut);
        final VerifTypeSimple verif = cfg.getVerifTypeSimple(defAttribut);
        return(verif.estValide(valeur));
    }
    
    /**
     * Renvoie la référence de du premier élément parent d'un attribut à partir de sa référence
     */
    public Element parentAttribut(final Element refAttribut) {
        final Element p = (Element)refAttribut.getParentNode();
        if ("schema".equals(p.getLocalName())) {
            final String nom = refAttribut.getAttribute("name");
            final NodeList latt = schema.getElementsByTagName("attribute");
            for (int i=0; i<latt.getLength(); i++) {
                final Element att = (Element)latt.item(i);
                if (att.getAttribute("ref").equals(nom))
                    return(parentAttribut(att));
            }
        } else if ("complexType".equals(p.getLocalName()))
            return(parentTypeComplexe(p));
        else if ("extension".equals(p.getLocalName()))
            return(parentExtension(p));
        else if ("attributeGroup".equals(p.getLocalName()))
            return(parentGroupeAttributs(p));
        return(null);
    }
    
    /**
     * Renvoie le premier élément parent du type complexe
     */
    private Element parentTypeComplexe(final Element ct) {
        final Element p = (Element)ct.getParentNode();
        if ("element".equals(p.getLocalName()))
            return(p);
        else {
            final String nomct = ct.getAttribute("name");
            final String espacect = ct.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
            for (final Element el : ltouselements) {
                final String type = el.getAttribute("type");
                if (!"".equals(type) && localValue(type).equals(nomct)) {
                    final String espace = el.lookupNamespaceURI(prefixeString(type));
                    if ((espace == null && espacect.equals("")) || (espace != null && espace.equals(espacect)))
                        return(el);
                }
            }
            return(null);
        }
    }
    
    /**
     * Renvoie le premier élément parent de l'extension
     */
    private Element parentExtension(final Element ext) {
        final Element p1 = (Element)ext.getParentNode();
        final Element p2 = (Element)p1.getParentNode(); // complexType
        return(parentTypeComplexe(p2));
    }
    
    /**
     * Renvoie le premier élément parent du groupe d'attributs
     */
    private Element parentGroupeAttributs(final Element attg) {
        final Element p1 = (Element)attg.getParentNode();
        if ("complexType".equals(p1.getLocalName()))
            return(parentTypeComplexe(p1));
        else if ("schema".equals(p1.getLocalName())) {
            final String nomattg = attg.getAttribute("name");
            final String espaceattg = attg.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
            for (final Element attg2 : ltousattgroups) {
                final String ref = attg2.getAttribute("ref");
                if (!"".equals(ref) && localValue(ref).equals(nomattg)) {
                    final String espace = attg2.lookupNamespaceURI(prefixeString(ref));
                    if ((espace == null && espaceattg.equals("")) || (espace != null && espace.equals(espaceattg))) {
                        final Element p2 = (Element)attg2.getParentNode();
                        if ("complexType".equals(p2.getLocalName()))
                            return(parentTypeComplexe(p2));
                    }
                }
            }
        } else if ("extension".equals(p1.getLocalName()))
            return(parentExtension(p1));
        else if ("attributeGroup".equals(p1.getLocalName()))
            return(parentGroupeAttributs(p1));
        return(null);
    }
    
    /**
     * Renvoie true si l'élément dont on donne la référence peut contenir du texte
     */
    public boolean contientDuTexte(final Element refElement) {
        if ("element".equals(refElement.getLocalName()) && !refElement.getAttribute("type").equals("")) {
            final String type = refElement.getAttribute("type");
            final String stype = localValue(type);
            final String tns = refElement.lookupNamespaceURI(prefixeString(type));
            // si le type fait partie des schémas XML (comme "string" ou "anyURI")
            // on considère que c'est du texte (sauf si le schéma est le schéma des schémas)
            if (!schemaNamespace.equals(targetNamespace) && schemaNamespace.equals(tns))
                return(true);
            // complexType
            final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, stype, tns);
            if (ct != null) {
                if ("true".equals(ct.getAttribute("mixed")))
                    return(true);
                final NodeList sc = ct.getElementsByTagNameNS(schemaNamespace, "simpleContent");
                if (sc.getLength() > 0 && sc.item(0) instanceof Element)
                    return(true);
            }
            // simpleType
            final Element st = chercherPremier(TypeObjetSchema.SIMPLETYPE, stype, tns);
            if (st != null)
                return(true);
        }
        Node n = refElement.getFirstChild();
        while (n != null) {
            if (n instanceof Element && n.getLocalName().equals("complexType")) {
                if ("true".equals(((Element)n).getAttribute("mixed")))
                    return(true);
                final ArrayList<Element> sc = enfants((Element)n, "simpleContent");
                return (sc.size() > 0);
            } else if (n instanceof Element && n.getLocalName().equals("simpleType"))
                return(true);
            n = n.getNextSibling();
        }
        return(false);
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
            LOG.error("Config.inclusion1(URL) : URISyntaxException for " + urls.toString(), ex);
        }
        Document schemadoc;
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder parser = dbf.newDocumentBuilder();
            parser.setEntityResolver(Jaxe.getEntityResolver());
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
        if (targetNamespace == null) {
            targetNamespace = schema2.getAttribute("targetNamespace");
            /*
            if (!"".equals(targetNamespace) && !targetNamespace.equals(namespacecfg))
                System.err.println(targetNamespace + " != " + namespacecfg + " !");
            */ // warning retiré parce-qu'il est maintenant possible de mélanger les espaces de noms
        }
        final URL schemadir = getParentURL(urls);
        inclusion2(schema2, schemadir);
        return(schema2);
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
     * Ajoute tous les éléments d'une NodeList à une ArrayList de Element, en supposant que
     * tous les éléments de la NodeList sont des org.w3c.dom.Element.
     */
    private static void addNodeList(final ArrayList<Element> l, final NodeList nl) {
        for (int i=0; i<nl.getLength(); i++)
            l.add((Element)nl.item(i)); // attention au cast
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
    
    /*
     * Utilise le CatalogResolver d'Apache pour résoudre des URI en URL
     */
    private URL resolveURI(final URL schemadir, final String uri) throws MalformedURLException, TransformerException {
        if (uri.startsWith("http://"))
            return(new URL(uri));
        else if (schemadir != null && !uri.startsWith("urn:"))
            return(new URL(schemadir.toExternalForm() + "/" + uri));
        else {
            final URIResolver resolver = Jaxe.getURIResolver();
            if (resolver != null) {
                Source src;
                if (schemadir != null)
                    src = resolver.resolve(uri, schemadir.toString());
                else
                    src = resolver.resolve(uri, null);
                final URL surl = new URL(src.getSystemId());
                try {
                    // pour éviter un bug de CatalogResolver qui n'encode pas l'URI correctement...
                    final URI suri = new URI(surl.getProtocol(), surl.getHost(), surl.getPath(), surl.getQuery(), null);
                    return(suri.toURL());
                } catch (URISyntaxException ex) {
                    LOG.error("resolveURI", ex);
                    return(surl);
                } catch (MalformedURLException ex) {
                    LOG.error("resolveURI", ex);
                    return(surl);
                }
            } else
                return(new URL(uri));
        }
    }
    
    /**
     * Chargement d'un schéma en mémoire. Appelé par inclusion1.
     *
     * @param sch  L'élément racine du schéma WXS
     * @param schemadir  URL du répertoire de référence (dans lequel se trouve le fichier du schéma)
     */
    private void inclusion2(final Element sch, final URL schemadir) {
        final ArrayList<Element> lelements = enfants(sch, "element");
        for (final Element el : lelements) {
            final String name = el.getAttribute("name");
            ArrayList<Element> liste = htopelements.get(name);
            if (liste == null) {
                liste = new ArrayList<Element>();
                htopelements.put(name, liste);
            }
            liste.add(el);
        }
        ltopelements.addAll(lelements);
        final ArrayList<Element> lattributs = enfants(sch, "attribute");
        for (final Element att : lattributs) {
            final String name = att.getAttribute("name");
            ArrayList<Element> liste = htopattributs.get(name);
            if (liste == null) {
                liste = new ArrayList<Element>();
                htopattributs.put(name, liste);
            }
            liste.add(att);
        }
        final ArrayList<Element> lgroups = enfants(sch, "group");
        for (final Element g : lgroups) {
            final String name = g.getAttribute("name");
            ArrayList<Element> liste = htopgroups.get(name);
            if (liste == null) {
                liste = new ArrayList<Element>();
                htopgroups.put(name, liste);
            }
            liste.add(g);
        }
        final ArrayList<Element> lcomptypes = enfants(sch, "complexType");
        for (final Element ct : lcomptypes) {
            final String name = ct.getAttribute("name");
            ArrayList<Element> liste = htopcomptypes.get(name);
            if (liste == null) {
                liste = new ArrayList<Element>();
                htopcomptypes.put(name, liste);
            }
            liste.add(ct);
        }
        final ArrayList<Element> lsimptypes = enfants(sch, "simpleType");
        for (final Element st : lsimptypes) {
            final String name = st.getAttribute("name");
            ArrayList<Element> liste = htopsimptypes.get(name);
            if (liste == null) {
                liste = new ArrayList<Element>();
                htopsimptypes.put(name, liste);
            }
            liste.add(st);
        }
        final ArrayList<Element> lattgroups = enfants(sch, "attributeGroup");
        for (final Element attg : lattgroups) {
            final String name = attg.getAttribute("name");
            ArrayList<Element> liste = htopattgroups.get(name);
            if (liste == null) {
                liste = new ArrayList<Element>();
                htopattgroups.put(name, liste);
            }
            liste.add(attg);
        }
        
        final ArrayList<Element> nouveauxElements = listeTous(sch, "element");
        ltouselements.addAll(nouveauxElements);
        ltousgroups.addAll(listeTous(sch, "group"));
        ltousattgroups.addAll(listeTous(sch, "attributeGroup"));
        ltousextensions.addAll(listeTous(sch, "extension"));
        
        final ArrayList<Element> linc = enfants(sch, "include");
        for (final Element inc : linc) {
            final String noms = inc.getAttribute("schemaLocation");
            try {
                URL urls = resolveURI(schemadir, noms);
                inclusion1(urls);
            } catch (final MalformedURLException ex) {
                LOG.error("include : MalformedURLException: " + ex.getMessage(), ex);
            } catch (final TransformerException ex) {
                LOG.error("include : TransformerException: " + ex.getMessage(), ex);
            }
        }
        
        final ArrayList<Element> limp = enfants(sch, "import");
        for (final Element imp : limp) {
            final String noms = imp.getAttribute("schemaLocation");
            final String namespace = imp.getAttribute("namespace");
            final NamedNodeMap latt = sch.getAttributes();
            String prefixe = null;
            for (int j=0; j<latt.getLength(); j++) {
                final Node n = latt.item(j);
                final String nomatt = n.getNodeName();
                final String valatt= n.getNodeValue();
                if (namespace.equals(valatt)) {
                    if (nomatt.startsWith("xmlns:")) {
                        prefixe = nomatt.substring(6);
                        break;
                    }
                }
            }
            if (prefixe != null)
                hashPrefixes.put(namespace, prefixe);
            if (noms != null && !noms.equals("")) {
                try {
                    URL urls = resolveURI(schemadir, noms);
                    inclusion1(urls);
                } catch (final MalformedURLException ex) {
                    LOG.error("import : MalformedURLException: " + ex.getMessage(), ex);
                } catch (final TransformerException ex) {
                    LOG.error("import : TransformerException: " + ex.getMessage(), ex);
                }
            }
        }
        
        // on doit ajouter un préfixe pour targetNamespace à la liste hashPrefixes
        // si au moins un attribut doit être qualifié
        if (targetNamespace != null && !"".equals(targetNamespace)) {
            boolean qualifiedAttr = "qualified".equals(sch.getAttribute("attributeFormDefault"));
            if (!qualifiedAttr) {
                final ArrayList<Element> tousLesAttributs = listeTous(sch, "attribute");
                for (final Element att : tousLesAttributs)
                    if ("qualified".equals(att.getAttribute("form"))) {
                        qualifiedAttr = true;
                        break;
                    }
            }
            if (qualifiedAttr) {
                final NamedNodeMap latt = sch.getAttributes();
                for (int i=0; i<latt.getLength(); i++) {
                    final Node n = latt.item(i);
                    final String nomatt = n.getNodeName();
                    final String valatt= n.getNodeValue();
                    if (targetNamespace.equals(valatt)) {
                        if (nomatt.startsWith("xmlns:")) {
                            final String prefixe = nomatt.substring(6);
                            hashPrefixes.put(targetNamespace, prefixe);
                            break;
                        }
                    }
                }
            }
        }
        
        final ArrayList<Element> lred = enfants(sch, "redefine");
        // on remplace les groupes et les groupes d'attributs par les nouvelles versions
        // pour les types simples et complexes, on renomme les types du schéma pointé par redefine par redefined_...
        // et on remplace les éventuelles références dans restriction/@base et extension/@base
        // idem pour group/choice/group/@ref, group/sequence/group/@ref et attributeGroup/attributeGroup/@ref
        for (final Element red : lred) {
            final String noms = red.getAttribute("schemaLocation");
            Element schemaRed;
            try {
                URL urls = resolveURI(schemadir, noms);
                schemaRed = inclusion1(urls);
            } catch (final MalformedURLException ex) {
                LOG.error("redefine : MalformedURLException: " + ex.getMessage(), ex);
                continue;
            } catch (final TransformerException ex) {
                LOG.error("redefine : TransformerException: " + ex.getMessage(), ex);
                continue;
            }
            final String targetRed = schemaRed.getAttribute("targetNamespace");
            
            final ArrayList<Element> lstRed = enfants(red, "simpleType");
            for (final Element stRed : lstRed) {
                final String nom = stRed.getAttribute("name");
                final Element vst = chercherPremier(TypeObjetSchema.SIMPLETYPE, nom, targetRed);
                if (vst == null) {
                    LOG.error("redefine " + nom + " : introuvable !");
                    continue;
                }
                final ArrayList<Element> selsimptypes = htopsimptypes.get(nom);
                final int indexvst = selsimptypes.indexOf(vst);
                if (indexvst == -1) {
                    LOG.error("redefine " + nom + " : introuvable au premier niveau !");
                    continue;
                }
                selsimptypes.set(indexvst, stRed);
                final String nnom = "redefined_" + nom;
                vst.setAttribute("name", nnom);
                ArrayList<Element> selsimptypes2 = htopsimptypes.get(nnom);
                if (selsimptypes2 == null) {
                    selsimptypes2 = new ArrayList<Element>();
                    htopsimptypes.put(nnom, selsimptypes2);
                }
                selsimptypes2.add(vst);
            }
            
            final ArrayList<Element> lctRed = enfants(red, "complexType");
            for (final Element ctRed : lctRed) {
                final String nom = ctRed.getAttribute("name");
                final Element vct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, nom, targetRed);
                if (vct == null) {
                    LOG.error("redefine " + nom + " : introuvable !");
                    continue;
                }
                final ArrayList<Element> selcomptypes = htopcomptypes.get(nom);
                final int indexvct = selcomptypes.indexOf(vct);
                if (indexvct == -1) {
                    LOG.error("redefine " + nom + " : introuvable au premier niveau !");
                    continue;
                }
                selcomptypes.set(indexvct, ctRed);
                final String nnom = "redefined_" + nom;
                vct.setAttribute("name", nnom);
                ArrayList<Element> selcomptypes2 = htopcomptypes.get(nnom);
                if (selcomptypes2 == null) {
                    selcomptypes2 = new ArrayList<Element>();
                    htopcomptypes.put(nnom, selcomptypes2);
                }
                selcomptypes2.add(vct);
            }
            
            final ArrayList<Element> lgrRed = enfants(red, "group");
            for (final Element grRed : lgrRed) {
                final String nom = grRed.getAttribute("name");
                final Element vgr = chercherPremier(TypeObjetSchema.GROUP, nom, targetRed);
                if (vgr == null) {
                    LOG.error("redefine " + nom + " : introuvable !");
                    continue;
                }
                final ArrayList<Element> selgroups = htopgroups.get(nom);
                final int indexvgr = selgroups.indexOf(vgr);
                if (indexvgr == -1) {
                    LOG.error("redefine " + nom + " : introuvable au premier niveau !");
                    continue;
                }
                
                selgroups.set(indexvgr, grRed);
                final String nnom = "redefined_" + nom;
                vgr.setAttribute("name", nnom);
                ArrayList<Element> selgroups2 = htopgroups.get(nnom);
                if (selgroups2 == null) {
                    selgroups2 = new ArrayList<Element>();
                    htopgroups.put(nnom, selgroups2);
                }
                selgroups2.add(vgr);
            }
            
            final ArrayList<Element> lattgrRed = enfants(red, "attributeGroup");
            for (final Element attgrRed : lattgrRed) {
                final String nom = attgrRed.getAttribute("name");
                final Element vattgr = chercherPremier(TypeObjetSchema.ATTRIBUTEGROUP, nom, targetRed);
                if (vattgr == null) {
                    LOG.error("redefine " + nom + " : introuvable !");
                    continue;
                }
                final ArrayList<Element> selattgroups = htopattgroups.get(nom);
                final int indexvattgr = selattgroups.indexOf(vattgr);
                if (indexvattgr == -1) {
                    LOG.error("redefine " + nom + " : introuvable au premier niveau !");
                    continue;
                }
                selattgroups.set(indexvattgr, attgrRed);
                final String nnom = "redefined_" + nom;
                vattgr.setAttribute("name", nnom);
                ArrayList<Element> selattgroups2 = htopattgroups.get(nnom);
                if (selattgroups2 == null) {
                    selattgroups2 = new ArrayList<Element>();
                    htopattgroups.put(nnom, selattgroups2);
                }
                selattgroups2.add(vattgr);
            }
            // liste les éléments du schéma d'origine redéfinis
            final ArrayList<Element> lredef = new ArrayList<Element>();
            Node item = schemaRed.getFirstChild();
            while (item != null) {
                if (item instanceof Element) {
                    final Element el = (Element)item;
                    if (el.getAttribute("name").startsWith("redefined_"))
                        lredef.add(el);
                }
                item = item.getNextSibling();
            }
            // appelle redefinirReferences pour les enfants de cet élément redéfini
            item = red.getFirstChild();
            while (item != null) {
                if (item instanceof Element) {
                    final Element el = (Element)item;
                    if (!"".equals(el.getAttribute("name")))
                        redefinirReferences(lredef, el);
                }
                item = item.getNextSibling();
            }
        }
        
    }
    
    /**
     * Modifie récursivement les références vers les éléments redéfinis
     * lredef: liste des éléments du schéma d'origine redéfinis
     * elverif: élément à vérifier pour modifier les références
     */
    private void redefinirReferences(final ArrayList<Element> lredef, final Element elverif) {
        final String nom = elverif.getLocalName();
        if ("restriction".equals(nom) || "extension".equals(nom)) {
            final String base = elverif.getAttribute("base");
            final String prefixe = prefixeString(base);
            final String sbase = "redefined_" + localValue(base);
            boolean redefini = false;
            for (final Element redef : lredef) {
                final String nomredef = redef.getLocalName();
                if (redef.getAttribute("name").equals(sbase) &&
                        (nomredef.equals("simpleType") || nomredef.equals("complexType"))) {
                    redefini = true;
                    break;
                }
            }
            if (redefini)
                elverif.setAttribute("base", prefixe + ":" + sbase);
        } else if ("group".equals(nom) || "attributeGroup".equals(nom)) {
            final String base = elverif.getAttribute("ref");
            final String prefixe = prefixeString(base);
            final String sbase = "redefined_" + localValue(base);
            boolean redefini = false;
            for (final Element redef : lredef) {
                final String nomredef = redef.getLocalName();
                if (redef.getAttribute("name").equals(sbase) && redef.getLocalName().equals(nom)) {
                    redefini = true;
                    break;
                }
            }
            if (redefini)
                elverif.setAttribute("ref", prefixe + ":" + sbase);
        }
        Node item = elverif.getFirstChild();
        while (item != null) {
            if (item instanceof Element) {
                final Element el = (Element)item;
                redefinirReferences(lredef, el);
            }
            item = item.getNextSibling();
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
    
    private void ajSubst(final Element el, final String nomel, final Set<Element> liste) {
        Set<Element> l = cacheSubst.get(el);
        if (l != null)
            liste.addAll(l);
        else {
            l = new LinkedHashSet<Element>();
            ajSubst2(el, nomel, l);
            cacheSubst.put(el, l);
            liste.addAll(l);
        }
    }
    
    private void ajSubst2(final Element el, final String nomel, final Set<Element> liste) {
        if (!"true".equals(localValue(el.getAttribute("abstract"))))
            liste.add(el);
        final String espaceEl = espaceElement(el);
        ArrayList<Element> lsubst = substitutions.get(el);
        if (lsubst == null) {
            lsubst = new ArrayList<Element>();
            for (final Element el2 : ltopelements) {
                final String substitutionGroup = el2.getAttribute("substitutionGroup");
                if (!substitutionGroup.equals("") && nomel.equals(localValue(substitutionGroup))) {
                    final String espace = el2.lookupNamespaceURI(prefixeString(substitutionGroup));
                    if ((espace == null && espaceEl == null) || (espace != null && espace.equals(espaceEl)))
                        lsubst.add(el2);
                }
            }
        }
        for (final Element el2 : lsubst) {
            final String nom2 = el2.getAttribute("name");
            ajSubst2(el2, nom2, liste);
        }
    }
    
    /**
     * Expression régulière correspondant au schéma pour un élément parent donné
     * problème 1: l'utilisateur entre les données au fur et à mesure, tout doit donc être facultatif
     * problème 2: jakarta-regexp et les possessive quantifiers n'acceptent pas des expressions comme (a?|b?)?
     * on fait donc des transformations:
     * (a+|b)c -> (a*|b?)?c?
     * (a?|b?)* -> (a|b)*
     * (a?b?c?)* -> (a|b|c)*    (modechoice=true)
     * ((a?b*c*)|d?)? -> (((ab*c*)|(b+c*)|(b*c+))|d)?     (modepasnul=true)
     * ((a|b)*|c)* -> (a|b|c)*
     *
     * @param sparent  Element parent dont on cherche l'expression régulière pour le contenu
     * @param niveau   Niveau d'appel (doit être égal à 1 quand l'appel n'est pas récursif)
     * @param modechoice  Utilisation d'un choice au lieu d'une sequence pour éviter les doubles annulations
     * @param modevisu  True si on cherche une expression régulière à afficher pour l'utilisateur
     * @param modepasnul  Eviter les expressions vides
     * @param imodepasnul  position d'évaluation, utilisée avec modepasnul
     * @param modevalid  Pour obtenir une validation stricte au lieu de chercher si une insertion est possible
     * @param annulable Tableau de taille 1 dans lequel on renvoie true si une chaîne vide matche
     */
    private String expressionReguliere(final Element sparent, final int niveau, final boolean modechoice, final boolean modevisu,
        final boolean modepasnul, final int imodepasnul, final boolean modevalid, final boolean[] annulable) {
        //System.out.println("expressionReguliere " + sparent.getNodeName() + " " + niveau +
        //    " modechoice=" + modechoice + " modevisu=" + modevisu + " modepasnul=" +
        //    modepasnul + " " + imodepasnul + " " + modevalid);
        annulable[0] = false;
        final StringBuilder builder = new StringBuilder();
        final String nombalise = sparent.getLocalName();
        if (niveau == 1 && nombalise.equals("element") && !"".equals(sparent.getAttribute("type"))) {
            final String type = sparent.getAttribute("type");
            final String stype = localValue(type);
            final String tns = sparent.lookupNamespaceURI(prefixeString(type));
            final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, stype, tns);
            if (ct != null)
                builder.append(expressionReguliere(ct, 2, modechoice, modevisu, modepasnul, 0, modevalid, annulable));
        } else if (niveau == 1 && nombalise.equals("element") && !"".equals(sparent.getAttribute("substitutionGroup")) &&
                enfants(sparent, "simpleType").size() == 0 && enfants(sparent, "complexType").size() == 0) {
            final String substitutionGroup = sparent.getAttribute("substitutionGroup");
            final String tns = sparent.lookupNamespaceURI(prefixeString(substitutionGroup));
            final Element refel = chercherPremier(TypeObjetSchema.ELEMENT, localValue(substitutionGroup), tns);
            if (refel != null)
                builder.append(expressionReguliere(refel, 1, modechoice, modevisu, modepasnul, 0, modevalid, annulable));
        } else if (nombalise.equals("group") && !"".equals(sparent.getAttribute("ref"))) {
            final String ref = sparent.getAttribute("ref");
            final String sref = localValue(ref);
            final String tns = sparent.lookupNamespaceURI(prefixeString(ref));
            final String min = sparent.getAttribute("minOccurs");
            final String max = sparent.getAttribute("maxOccurs");
            final Element gr = chercherPremier(TypeObjetSchema.GROUP, sref, tns);
            if (gr != null) {
                boolean nouveaumodechoice = !modevisu && !modepasnul;
                if (!modechoice && nouveaumodechoice && ("".equals(max) || "1".equals(max)))
                    nouveaumodechoice = false;
                final boolean nouveaumodepasnul = ( modepasnul ||
                    (!modevisu && !nouveaumodechoice && "0".equals(min)) );
                builder.append(expressionReguliere(gr, 2, nouveaumodechoice, modevisu, nouveaumodepasnul, 0, modevalid, annulable));
                if ("0".equals(min))
                    annulable[0] = true;
            }
            if ("0".equals(min) && !modechoice && !modepasnul) {
                builder.insert(0, "(");
                if ("".equals(max) || "1".equals(max))
                    builder.append(")?");
                else
                    builder.append(")*");
                if (!modevisu)
                    builder.append("+");
            } else {
                if (!"".equals(max) && !"1".equals(max)) {
                    builder.insert(0, "(");
                    builder.append(")+");
                    if (!modevisu)
                        builder.append("+");
                }
            }
        } else if (nombalise.equals("group") && !"".equals(sparent.getAttribute("name"))) {
            Node item = sparent.getFirstChild();
            final boolean[] annulable2 = new boolean[1];
            annulable[0] = true;
            while (item != null) {
                if (item instanceof Element) {
                    final Element sousb = (Element)item;
                    final String r = expressionReguliere(sousb, 2, modechoice, modevisu, modepasnul, 0, modevalid, annulable2);
                    annulable[0] = annulable[0] && annulable2[0];
                    if (r != null && !"".equals(r)) {
                        builder.append(r);
                        break;
                    }
                }
                item = item.getNextSibling();
            }
        } else if (nombalise.equals("choice") ||
            (!(modevisu || modevalid) && nombalise.equals("sequence") &&
                !"".equals(sparent.getAttribute("maxOccurs")) &&
                !"1".equals(sparent.getAttribute("maxOccurs")))) {
            final String min = sparent.getAttribute("minOccurs");
            final String max = sparent.getAttribute("maxOccurs");
            final boolean max1 = ("".equals(max) || "1".equals(max));
            final boolean nouveaumodechoice = !modevisu && !modepasnul &&
                (!max1 || modechoice) && (max1 || !modevalid);
            final boolean nouveaumodepasnul = ( modepasnul ||
                (!modevisu && !nouveaumodechoice && nombalise.equals("choice") && "0".equals(min)) ||
                (!modevisu && !max1 && modevalid));
            annulable[0] = "0".equals(min);
            final boolean[] annulable2 = new boolean[1];
            Node item = sparent.getFirstChild();
            while (item != null) {
                if (item instanceof Element) {
                    final Element sousb = (Element)item;
                    final String r = expressionReguliere(sousb, 2, nouveaumodechoice, modevisu, nouveaumodepasnul, 0, modevalid, annulable2);
                    annulable[0] = annulable[0] || annulable2[0];
                    if (r != null && !"".equals(r)) {
                        if (builder.length() > 0) {
                            builder.append("|");
                        }
                        builder.append(r);
                    }
                }
                item = item.getNextSibling();
            }
            if (!modechoice && builder.length() > 0) {
                builder.insert(0, "(");
                if (("0".equals(min) || ((nouveaumodepasnul || nouveaumodechoice) && annulable[0])) && !modepasnul) {
                    if ("".equals(max) || "1".equals(max))
                        builder.append(")?");
                    else
                        builder.append(")*");
                    if (!modevisu)
                        builder.append("+");
                } else {
                    if ("".equals(max) || "1".equals(max))
                        builder.append(")");
                    else {
                        builder.append(")+");
                        if (!modevisu)
                            builder.append("+");
                    }
                }
            }
        } else if (nombalise.equals("sequence")) {
            final NodeList lsousb = sparent.getChildNodes();
            if (modepasnul) {
                final boolean[] annulable2 = new boolean[1];
                annulable[0] = true;
                for (int i=imodepasnul; i<lsousb.getLength(); i++) {
                    final Node item = lsousb.item(i);
                    if (item instanceof Element) {
                        final Element sousb = (Element)item;
                        final String r = expressionReguliere(sousb, 2, modechoice, modevisu, false, 0, modevalid, annulable2);
                        annulable[0] = annulable[0] && annulable2[0];
                        builder.append(r);
                    }
                }
                if (annulable[0]) {
                    builder.delete(0, builder.length());
                    for (int i=imodepasnul; i<lsousb.getLength(); i++) {
                        final Node item = lsousb.item(i);
                        if (item instanceof Element) {
                            final Element sousb = (Element)item;
                            final String r1 = expressionReguliere(sousb, 2, modechoice, modevisu, true, 0, modevalid, annulable2);
                            final String r2 = expressionReguliere(sousb, 2, modechoice, modevisu, false, 0, modevalid, annulable2);
                            String r3 = null;
                            String r4 = null;
                            for (int i2=i+1; i2<lsousb.getLength(); i2++) {
                                final Node item2 = lsousb.item(i2);
                                if (item2 instanceof Element) {
                                    final Element sousb2 = (Element)item2;
                                    r3 = expressionReguliere(sparent, 2, modechoice, modevisu, true, i2, modevalid, annulable2);
                                    r4 = expressionReguliere(sparent, 2, modechoice, modevisu, false, i2, modevalid, annulable2);
                                    break;
                                }
                            }
                            if (r3 != null && !"".equals(r3))
                                builder.append("(").append(r1).append(r4).append("|").append(r2).append(r3).append(")");
                            else
                                builder.append(r1);
                            break;
                        }
                    }
                }
            } else {
                annulable[0] = true;
                final boolean[] annulable2 = new boolean[1];
                for (int i=imodepasnul; i<lsousb.getLength(); i++) {
                    final Node item = lsousb.item(i);
                    if (item instanceof Element) {
                        final Element sousb = (Element)item;
                        final String r = expressionReguliere(sousb, 2, modechoice, modevisu, false, 0, modevalid, annulable2);
                        if (modechoice) {
                            if (builder.length() > 0)
                                builder.append("|");
                            builder.append(r);
                        } else {
                            if (!(modevisu || modevalid) && r != null && !annulable2[0] && !"".equals(r)) {
                                annulable2[0] = true;
                                if (r.endsWith(")")) {
                                    builder.append(r);
                                    builder.append("?");
                                } else {
                                    builder.append("(");
                                    builder.append(r);
                                    builder.append(")?");
                                }
                                if (!modevisu)
                                    builder.append("+");
                            } else {
                                if (builder.length() > 0 && modevisu)
                                    builder.append(", ");
                                builder.append(r);
                            }
                        }
                        annulable[0] = annulable[0] && annulable2[0];
                    }
                }
            }
            final String min = sparent.getAttribute("minOccurs");
            if ("0".equals(min))
                annulable[0] = true;
            if (modevisu || modevalid) {
                final String max = sparent.getAttribute("maxOccurs");
                if ("0".equals(min)) {
                    builder.insert(0, "(");
                    if ("".equals(max) || "1".equals(max))
                        builder.append(")?");
                    else
                        builder.append(")*");
                    if (!modevisu)
                        builder.append("+");
                } else {
                    if (!"".equals(max) && !"1".equals(max)) {
                        builder.insert(0, "(");
                        builder.append(")+");
                        if (!modevisu)
                            builder.append("+");
                    }
                }
            }
        } else if (nombalise.equals("complexType") || nombalise.equals("complexContent")) {
            Node item = sparent.getFirstChild();
            while (item != null) {
                if (item instanceof Element) {
                    final Element sousb = (Element)item;
                    final String r = expressionReguliere(sousb, 2, modechoice, modevisu, modepasnul, 0, modevalid, annulable);
                    if (r != null && !"".equals(r)) {
                        builder.append(r);
                    }
                }
                item = item.getNextSibling();
            }
        } else if (nombalise.equals("element") && niveau == 2) {
            final String sname = sparent.getAttribute("name");
            if (!"".equals(sname))
                builder.append(substExpr(sparent, sname, modevisu, modechoice));
            else if (!"".equals(sparent.getAttribute("ref"))) {
                final String ref = sparent.getAttribute("ref");
                final String sref = localValue(ref);
                final String tns = sparent.lookupNamespaceURI(prefixeString(ref));
                final Element refel = chercherPremier(TypeObjetSchema.ELEMENT, sref, tns);
                if (refel != null)
                    builder.append(substExpr(refel, sref, modevisu, modechoice));
                else
                    LOG.error(
                            "expressionReguliere(Element, int, boolean, boolean, boolean, int, boolean) - référence non trouvée: "
                                    + sref);
            }
            if (builder.length() > 0) {
                final String min = sparent.getAttribute("minOccurs");
                final String max = sparent.getAttribute("maxOccurs");
                annulable[0] = "0".equals(min);
                if ("0".equals(min) && !modechoice && !modepasnul) {
                    if (!modevisu) {
                        builder.insert(0, "(");
                        builder.append(")");
                    }
                    if ("".equals(max) || "1".equals(max))
                        builder.append("?");
                    else
                        builder.append("*");
                    if (!modevisu)
                        builder.append("+");
                } else {
                    if (!"".equals(max) && !"1".equals(max)) {
                        if (!modevisu) {
                            builder.insert(0, "(");
                            builder.append(")");
                        }
                        builder.append("+");
                        if (!modevisu)
                            builder.append("+");
                    }
                }
            }
        } else if (nombalise.equals("any")) {
            // impossible de sélectionner des espaces de noms particuliers => on autorise tout
            final String min = sparent.getAttribute("minOccurs");
            final String max = sparent.getAttribute("maxOccurs");
            if (!"".equals(max) && !"1".equals(max)) {
                if ("0".equals(min))
                    builder.append(".*");
                else
                    builder.append(".+");
            } else {
                if (!modevisu)
                    builder.append("[^,]+,");
                else
                    builder.append(".+");
            }
            annulable[0] = "0".equals(min);
        } else if (nombalise.equals("all")) {
            // impossible de faire une expression régulière correspondante => on autorise (a|b|...|z)+
            final boolean nouveaumodechoice = !modevisu && !modepasnul;
            annulable[0] = true;
            final boolean[] annulable2 = new boolean[1];
            Node item = sparent.getFirstChild();
            while (item != null) {
                if (item instanceof Element) {
                    final Element sousb = (Element)item;
                    final String r = expressionReguliere(sousb, 2, nouveaumodechoice, modevisu, modepasnul, 0, modevalid, annulable2);
                    annulable[0] = annulable[0] && annulable2[0];
                    if (r != null && !"".equals(r)) {
                        if (builder.length() > 0) {
                            builder.append("|");
                        }
                        builder.append(r);
                    }
                }
                item = item.getNextSibling();
            }
            builder.insert(0, "(");
            builder.append(")+");
            if (!modevisu)
                builder.append("+");
            
        } else {
            final boolean[] annulable2 = new boolean[1];
            annulable[0] = true;
            if (nombalise.equals("extension") && !"".equals(sparent.getAttribute("base"))) {
                final String base = sparent.getAttribute("base");
                final String tns = sparent.lookupNamespaceURI(prefixeString(base));
                final String sbase = localValue(base);
                final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, sbase, tns);
                if (ct != null) {
                    builder.append(expressionReguliere(ct, 2, modechoice, modevisu, modepasnul, 0, modevalid, annulable2));
                    annulable[0] = annulable[0] && annulable2[0];
                }
            }
            String regexp2 = null;
            Node item = sparent.getFirstChild();
            while (item != null) {
                if (item instanceof Element) {
                    final Element sousb = (Element)item;
                    if (!"annotation".equals(sousb.getLocalName())) {
                        regexp2 = expressionReguliere(sousb, 2, modechoice, modevisu, modepasnul, 0, modevalid, annulable2);
                        if (regexp2 != null && !"".equals(regexp2)) {
                            if (modechoice) {
                                if (builder.length() > 0)
                                    builder.append("|");
                                builder.append(regexp2);
                            } else {
                                if (!(modevisu || modevalid) && !annulable2[0]) {
                                    annulable2[0] = true;
                                    if (regexp2.endsWith(")")) {
                                        builder.append(regexp2);
                                        builder.append("?");
                                    } else {
                                        builder.append("(");
                                        builder.append(regexp2);
                                        builder.append(")?");
                                    }
                                    if (!modevisu)
                                        builder.append("+");
                                } else {
                                    if (builder.length() > 0 && modevisu)
                                        builder.append(", ");
                                    builder.append(regexp2);
                                }
                            }
                        }
                        annulable[0] = annulable[0] && annulable2[0];
                        break;
                    }
                }
                item = item.getNextSibling();
            }
        }
//        System.out.println("-> " + builder.toString());
        return(builder.toString());
    }
    
    /**
     * utilisé dans expressionReguliere pour les substitutionGroup
     */
    private String substExpr(final Element el, final String nomel, final boolean modevisu, final boolean modechoice) {
        final StringBuilder builder = new StringBuilder();
        if (!"true".equals(localValue(el.getAttribute("abstract")))) {
            if (modevisu)
                builder.append(cfg.titreElement(el));
            else
                builder.append(nomel);
            if (!modevisu)
                builder.append(",");
        }
        boolean bliste = false;
        final String espaceEl = espaceElement(el);
        ArrayList<Element> lsubst = substitutions.get(el);
        if (lsubst == null) {
            lsubst = new ArrayList<Element>();
            for (final Element el2 : ltopelements) {
                final String substitutionGroup = el2.getAttribute("substitutionGroup");
                if (!substitutionGroup.equals("") && nomel.equals(localValue(substitutionGroup))) {
                    final String espace = el2.lookupNamespaceURI(prefixeString(substitutionGroup));
                    if ((espace == null && espaceEl == null) || (espace != null && espace.equals(espaceEl)))
                        lsubst.add(el2);
                }
            }
        }
        for (final Element el2 : lsubst) {
            final String nom2 = el2.getAttribute("name");
            if (builder.length() > 0)
                builder.append("|");
            bliste = true;
            builder.append(substExpr(el2, nom2, modevisu, true));
        }
        if (bliste && (modevisu || !modechoice)) {
            builder.insert(0, "(");
            builder.append(")");
        }
        return(builder.toString());
    }
    
    private ArrayList<Element> sParents(final Element refElement) {
        final Set<Element> liste = new LinkedHashSet<Element>();
        if (refElement.getLocalName().equals("schema"))
            return(new ArrayList<Element>(liste));
        final String nom = refElement.getAttribute("name");
        final String espace = refElement.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
        if (refElement.getLocalName().equals("complexType") && !"".equals(nom)) {
            for (final Element ext : ltousextensions) {
                if (nom.equals(localValue(ext.getAttribute("base")))) {
                    final String tns = ext.lookupNamespaceURI(prefixeString(ext.getAttribute("base")));
                    if ((tns == null && espace.equals("")) || (tns != null && tns.equals(espace))) {
                        final Element parent = (Element)ext.getParentNode();
                        liste.addAll(sParents(parent));
                    }
                }
            }
            for (final Element el : ltouselements) {
                final String type = el.getAttribute("type");
                if (!"".equals(type) && localValue(type).equals(nom)) {
                    final String tns = el.lookupNamespaceURI(prefixeString(type));
                    if ((tns == null && espace.equals("")) || (tns != null && tns.equals(espace)))
                        liste.add(el);
                }
            }
        } else if (refElement.getLocalName().equals("group") && !"".equals(nom)) {
            for (final Element el : ltousgroups) {
                final String ref = el.getAttribute("ref");
                if (!"".equals(ref) && localValue(ref).equals(nom)) {
                    final String tns = el.lookupNamespaceURI(prefixeString(ref));
                    if ((tns == null && espace.equals("")) || (tns != null && tns.equals(espace)))
                        liste.addAll(sParents(el));
                }
            }
        } else {
            final Element parent = (Element)refElement.getParentNode();
            if (parent.getLocalName().equals("element"))
                ajSubst(parent, parent.getAttribute("name"), liste);
            else
                liste.addAll(sParents(parent));
        }
        return(new ArrayList<Element>(liste));
    }
    
    // Liste de éléments attribute sous complexType ou attributeGroup ou extension
    // (avec attribut name ou attribut ref)
    private ArrayList<Element> sCtAttributs(final Element ctdef, ArrayList<Element> pile) {
        final ArrayList<Element> liste = new ArrayList<Element>();
        
        if (pile != null && pile.contains(ctdef))
            return(liste);
        if (pile == null)
            pile = new ArrayList<Element>();
        pile.add(ctdef);
        
        Node item = ctdef.getFirstChild();
        while (item != null) {
            if (item instanceof Element) {
                final Element sousb = (Element)item;
                final String localname = sousb.getLocalName();
                if ("attribute".equals(localname))
                    liste.add(sousb);
                else if ("attributeGroup".equals(localname)) {
                    final String ref = sousb.getAttribute("ref");
                    if (!"".equals(ref)) {
                        final String tns = sousb.lookupNamespaceURI(prefixeString(ref));
                        final Element agj = chercherPremier(TypeObjetSchema.ATTRIBUTEGROUP, localValue(ref), tns);
                        if (agj != null)
                            liste.addAll(sCtAttributs(agj, pile));
                    } else
                        liste.addAll(sCtAttributs(sousb, pile));
                } else if ("simpleContent".equals(localname) || "complexContent".equals(localname)) {
                    final ArrayList<Element> extl = enfants(sousb, "extension");
                    for (final Element ext : extl) {
                        final String base = ext.getAttribute("base");
                        if (!"".equals(base)) {
                            final String tns = ext.lookupNamespaceURI(prefixeString(base));
                            final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, localValue(base), tns);
                            if (ct != null)
                                liste.addAll(sCtAttributs(ct, pile));
                        }
                        //liste.addAll(sCtAttributs(ext, pile));
                        // cas des attributs redéfinis : priorié aux attributs de l'extension
                        final ArrayList<Element> listePrioritaire = sCtAttributs(ext, pile);
                        final ArrayList<Element> aretirer = new ArrayList<Element>();
                        for (final Element att : listePrioritaire) {
                            final String nomatt = att.getAttribute("name");
                            for (final Element att2 : liste) {
                                final String nomatt2 = att2.getAttribute("name");
                                if (nomatt2.equals(nomatt))
                                    aretirer.add(att2);
                            }
                        }
                        for (final Element att : aretirer)
                            liste.remove(att);
                        liste.addAll(listePrioritaire);
                    }
                    final ArrayList<Element> restl = enfants(sousb, "restriction");
                    for (final Element rest : restl) {
                        final String base = rest.getAttribute("base");
                        if (!"".equals(base)) {
                            final String tns = rest.lookupNamespaceURI(prefixeString(base));
                            final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, localValue(base), tns);
                            if (ct != null)
                                liste.addAll(sCtAttributs(ct, pile));
                        }
                        final ArrayList<Element> restattr = sCtAttributs(rest, pile);
                        for (int i=0; i<liste.size(); i++) {
                            final Element att = liste.get(i);
                            final String nomatt = att.getAttribute("name");
                            for (final Element att2 : restattr) {
                                final String nomatt2 = att2.getAttribute("name");
                                if (nomatt2.equals(nomatt)) {
                                    if ("prohibited".equals(att2.getAttribute("use"))) {
                                        liste.remove(i);
                                        i--;
                                    } else
                                        liste.set(i, att2);
                                }
                            }
                        }
                    }
                }
            }
            item = item.getNextSibling();
        }
                
        return(liste);
    }
    
    /**
     * Renvoie la documentation d'un élément ou attribut dont on donne la définition dans le fichier de schéma WXS
     */
    private String sDocumentation(final Element balisedef) {
        if (balisedef == null)
            return(null);
        Node n = balisedef.getFirstChild();
        while (n != null) {
            if (n instanceof Element && n.getLocalName().equals("annotation")) {
                final NodeList ldoc = ((Element)n).getElementsByTagNameNS(schemaNamespace, "documentation");
                String sdoc = null;
                for (int j=0; j<ldoc.getLength(); j++) {
                    final Element doc = (Element)ldoc.item(j);
                    if (doc.getFirstChild() != null) {
                        if (sdoc == null)
                            sdoc = "";
                        else
                            sdoc += newline;
                        sdoc += doc.getFirstChild().getNodeValue();
                    }
                }
                if (sdoc != null)
                    sdoc = sdoc.trim();
                return(sdoc);
            }
            n = n.getNextSibling();
        }
        return(null);
    }
    
    /**
     * Renvoie l'élément simpleType ou complexType avec le nom et l'espace de noms donnés.
     * Renvoie null si aucun type correspondant n'est trouvé.
     */
    public Element getSchemaTypeElement(final String nomType, final String tns) {
        final Element st = chercherPremier(TypeObjetSchema.SIMPLETYPE, nomType, tns);
        if (st != null)
            return(st);
        final Element ct = chercherPremier(TypeObjetSchema.COMPLEXTYPE, nomType, tns);
        if (ct != null)
            return(ct);
        return(null);
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
                // on cherche d'abord dans les éléments déclarés sous la racine
                final ArrayList<Element> lelements = htopelements.get(nom);
                if (lelements != null)
                    for (final Element ref : lelements) {
                        final String espaceRef = espaceElement(ref);
                        if ((espace == null && espaceRef == null) || (espace != null && espace.equals(espaceRef)))
                            return(ref);
                    }
                for (final Element ref : ltouselements) {
                    if (nom.equals(ref.getAttribute("name"))) {
                        final String espaceRef = espaceElement(ref);
                        if ((espace == null && espaceRef == null) || (espace != null && espace.equals(espaceRef)))
                            return(ref);
                    }
                }
                return(null);
            case ATTRIBUTE:
                final ArrayList<Element> lattributs = htopattributs.get(nom);
                if (lattributs != null)
                    for (final Element att : lattributs) {
                        final String espaceRef = att.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
                        if ((espace == null && espaceRef.equals("")) || (espace != null && espace.equals(espaceRef)))
                            return(att);
                    }
                return(null);
            case GROUP:
                final ArrayList<Element> lgroups = htopgroups.get(nom);
                if (lgroups != null)
                    for (final Element g : lgroups) {
                        final String espaceRef = g.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
                        if ((espace == null && espaceRef.equals("")) || (espace != null && espace.equals(espaceRef)))
                            return(g);
                    }
                return(null);
            case COMPLEXTYPE:
                final ArrayList<Element> lcomptypes = htopcomptypes.get(nom);
                if (lcomptypes != null)
                    for (final Element ct : lcomptypes) {
                        final String espaceRef = ct.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
                        if ((espace == null && espaceRef.equals("")) || (espace != null && espace.equals(espaceRef)))
                            return(ct);
                    }
                return(null);
            case SIMPLETYPE:
                final ArrayList<Element> lsimptypes = htopsimptypes.get(nom);
                if (lsimptypes != null)
                    for (final Element st : lsimptypes) {
                        final String espaceRef = st.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
                        if ((espace == null && espaceRef.equals("")) || (espace != null && espace.equals(espaceRef)))
                            return(st);
                    }
                return(null);
            case ATTRIBUTEGROUP:
                final ArrayList<Element> lattgroups = htopattgroups.get(nom);
                if (lattgroups != null)
                    for (final Element attg : lattgroups) {
                        final String espaceRef = attg.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
                        if ((espace == null && espaceRef.equals("")) || (espace != null && espace.equals(espaceRef)))
                            return(attg);
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
                // on cherche d'abord dans les éléments déclarés sous la racine
                final ArrayList<Element> lelements = htopelements.get(nom);
                if (lelements != null && lelements.size() > 0)
                    return(lelements.get(0));
                for (final Element ref : ltouselements) {
                    if (nom.equals(ref.getAttribute("name")))
                        return(ref);
                }
                return(null);
            case GROUP:
                final ArrayList<Element> lgroups = htopgroups.get(nom);
                if (lgroups != null && lgroups.size() > 0)
                    return(lgroups.get(0));
                return(null);
            case COMPLEXTYPE:
                final ArrayList<Element> lcomptypes = htopcomptypes.get(nom);
                if (lcomptypes != null && lcomptypes.size() > 0)
                    return(lcomptypes.get(0));
                return(null);
            case SIMPLETYPE:
                final ArrayList<Element> lsimptypes = htopsimptypes.get(nom);
                if (lsimptypes != null && lsimptypes.size() > 0)
                    return(lsimptypes.get(0));
                return(null);
            case ATTRIBUTEGROUP:
                final ArrayList<Element> lattgroups = htopattgroups.get(nom);
                if (lattgroups != null && lattgroups.size() > 0)
                    return(lattgroups.get(0));
                return(null);
            default :
                return(null);
        }
    }
    
}
