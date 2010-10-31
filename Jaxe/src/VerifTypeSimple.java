/*
Jaxe - Editeur XML en Java

Copyright (C) 2006 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//jakarta-oro
//import org.apache.oro.text.regex.*;
//import org.apache.oro.text.awk.*;

import org.w3c.dom.*;


/**
 * Classe permettant de vérifier la validité d'un type simple (= simpleType dans les schémas XML)
 */
public class VerifTypeSimple {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(VerifTypeSimple.class);

    private final boolean required;
    private String fixed = null;
    private Contraintes contraintes;
    private static final HashMap<String, Pattern> PATTERN_CACHE = new HashMap<String, Pattern>();
//    protected PatternCompiler compiler;
//    protected PatternMatcher matcher;
    
    /**
     * Constructeur prenant en paramètre la config et un élément "element" ou "attribute" d'un schéma W3C
     */
    public VerifTypeSimple(final Config cfg, final Element snodedef) {
//        compiler = new AwkCompiler();
//        matcher = new AwkMatcher();
        final String stype = snodedef.getAttribute("type");
        final String sReq = snodedef.getAttribute("use");
        required = "required".equals(sReq);
        if (!"".equals(stype))
            contraintes = new Contraintes(cfg, stype, snodedef);
        else {
            Node n = snodedef.getFirstChild();
            while (n != null) {
                if (n instanceof Element && n.getLocalName().equals("simpleType")) {
                    contraintes = new Contraintes(cfg, (Element)n);
                } else if (n instanceof Element && n.getLocalName().equals("complexType")) {
                    Node n2 = n.getFirstChild();
                    while (n2 != null) {
                        if (n2 instanceof Element && n2.getLocalName().equals("simpleContent"))
                            contraintes = new Contraintes(cfg, (Element)n2);
                        n2 = n2.getNextSibling();
                    }
                }
                n = n.getNextSibling();
            }
            
        }
        final String fixedval = snodedef.getAttribute("fixed");
        if (!"".equals(fixedval))
            fixed = fixedval;
    }
    
    /**
     * Renvoit le type de base (sans préciser les éventuelles restrictions).
     */
    public String getBaseType() {
        if (contraintes == null)
            return(null);
        else
            return(contraintes.getBaseType());
    }
    
    /**
     * Renvoit la liste de valeurs possibles, ou null s'il n'y en a pas.
     */
    public ArrayList<String> getEnumeration() {
        if (contraintes == null) {
            if (fixed != null) {
                final ArrayList<String> enumeration = new ArrayList<String>();
                enumeration.add(fixed);
                enumeration.add("");
                return(enumeration);
            }
            return(null);
        } else {
            ArrayList<String> enumeration = contraintes.getEnumeration();
            if (fixed != null) {
                if (enumeration == null)
                    enumeration = new ArrayList<String>();
                if (!enumeration.contains(fixed))
                    enumeration.add(fixed);
                if (!enumeration.contains(""))
                    enumeration.add("");
            }
            return(enumeration);
        }
    }
    
    /**
     * Renvoit true si la valeur est valide par rapport à la définition du type simple.
     */
    public boolean estValide(final String valeur) {
        if (fixed != null && !fixed.equals(valeur))
            return(false);
        if (contraintes == null)
            return(true);
        return(contraintes.valide(valeur));
    }
    
    
    class Contraintes {
        private String baseType = null;
        private ArrayList<String> enumeration = null;
        private ArrayList<String> patterns = null;
        private ArrayList<Restriction> restrictions = null;
        private Union union = null;
        private Liste liste = null;
        
        // prend en paramètre un élément simpleType ou simpleContent
        public Contraintes(final Config cfg, final Element st) {
            lireTypeEtRestrictions(cfg, st);
        }
        
        // prend en paramètre le nom d'un type avec son préfixe, et un élément du schéma pour résoudre le préfixe en espace de noms
        public Contraintes(final Config cfg, final String stype, final Element nref) {
            lireTypeEtRestrictions(cfg, stype, nref);
        }
        
        protected void lireTypeEtRestrictions(final Config cfg, final Element st) {
            Element restel = null;
            Node n = st.getFirstChild();
            while (n != null) {
                if (n instanceof Element && (n.getLocalName().equals("restriction") || n.getLocalName().equals("extension") ||
                        n.getLocalName().equals("union") || n.getLocalName().equals("list"))) {
                    restel = (Element)n;
                    break;
                }
                n = n.getNextSibling();
            }
            if (restel == null)
                return;
            if (restel.getLocalName().equals("restriction")) {
                baseType = restel.getAttribute("base");
                if (baseType.indexOf(':') != -1)
                    baseType = baseType.substring(baseType.indexOf(':') + 1);
                Node n2 = restel.getFirstChild();
                while (n2 != null) {
                    if (n2 instanceof Element)
                        lireRestriction((Element)n2);
                    n2 = n2.getNextSibling();
                }
            } else if (restel.getLocalName().equals("extension")) {
                baseType = restel.getAttribute("base");
                lireTypeEtRestrictions(cfg, baseType, st);
            } else if (restel.getLocalName().equals("union")) {
                union = new Union(cfg, restel);
                enumeration = union.getEnumeration();
            } else if (restel.getLocalName().equals("list")) {
                liste = new Liste(cfg, restel);
                enumeration = null;
            }
        }
        
        protected void lireTypeEtRestrictions(final Config cfg, final String stype, final Element nref) {
            final String targetNamespace = cfg.espaceCible();
            final boolean metaschema = targetNamespace != null && targetNamespace.equals(nref.getNamespaceURI());
            final String schemaPrefix = nref.getPrefix();
            final int indp = stype.indexOf(':');
            if (!metaschema && ((indp == -1 && schemaPrefix == null) ||
                    (indp != -1 && stype.substring(0, indp).equals(schemaPrefix)))) {
                baseType = stype.substring(indp+1);
            } else {
                String nomType, tns;
                if (indp == -1) {
                    nomType = stype;
                    tns = nref.lookupNamespaceURI(null);
                } else {
                    nomType = stype.substring(indp+1);
                    tns = nref.lookupNamespaceURI(stype.substring(0, indp));
                }
                final Element eltype = cfg.getSchemaTypeElement(nomType, tns);
                if (eltype == null)
                    LOG.error("VerifTypeSimple(JaxeDocument, Element) - typesimple: attention: pas de définition de "
                            + nomType + " dans le schéma", null);
                else if (eltype.getLocalName().equals("complexType")) {
                    Node n = eltype.getFirstChild();
                    while (n != null) {
                        if (n instanceof Element && n.getLocalName().equals("simpleContent"))
                            lireTypeEtRestrictions(cfg, (Element)n);
                        n = n.getNextSibling();
                    }
                } else if (eltype.getLocalName().equals("simpleType"))
                    lireTypeEtRestrictions(cfg, eltype);
            }
        }
        
        // prend en paramètre un élément enfant de l'élément restriction
        protected void lireRestriction(final Element facet) {
            if (facet.getLocalName().equals("enumeration")) {
                final String val = facet.getAttribute("value");
                if (enumeration == null)
                    enumeration = new ArrayList<String>();
                enumeration.add(val);
            } else if (facet.getLocalName().equals("pattern")) {
                String val = facet.getAttribute("value");
                if (patterns == null)
                    patterns = new ArrayList<String>();
                // remplacements très approximatifs de \i, \I, \c et \C
                val = remplacer(val, "\\i", "[^<>&#!/?'\",0-9.\\-\\s]");
                val = remplacer(val, "\\I", "[^a-zA-Z]");
                val = remplacer(val, "\\c", "[^<>&#!/?'\",\\s]");
                val = remplacer(val, "\\C", "\\W");
                patterns.add(val);
            } else {
                final String val = facet.getAttribute("value");
                if (restrictions == null)
                    restrictions = new ArrayList<Restriction>();
                restrictions.add(new Restriction(facet.getLocalName(), val));
            }
        }
        
        protected String remplacer(String s, final String sremp, final String spar) {
            int ind = s.indexOf(sremp);
            while (ind != -1) {
                s = s.substring(0, ind) + spar + s.substring(ind + sremp.length());
                ind = s.indexOf(sremp);
            }
            return(s);
        }
        
        protected boolean valide(final String valeur) {
            // vérif required
            if  ((valeur == null || "".equals(valeur)) && required)
                return false;
            
            // vérif union
            if (union != null)
                return(union.valide(valeur));
            
            // vérif liste
            if (liste != null)
                return(liste.valide(valeur));
            
            // vérif baseType
            if (baseType != null && !verifType(baseType, valeur))
                return(false);
            
            // vérif enumerations
            if (enumeration != null) {
                if (valeur == null)
                    return(false);
                boolean trouve = false;
                for (final String en : enumeration)
                    if (valeur.equals(en)) {
                        trouve = true;
                        break;
                    }
                if (!trouve)
                    return(false);
            }
            // vérif patterns
            if (patterns != null) {
                boolean trouve = false;
                for (final String pattern : patterns)
                    if (verifExpr(valeur, pattern)) {
                        trouve = true;
                        break;
                    }
                if (!trouve)
                    return(false);
            }
            // vérif restrictions
            if (restrictions != null) {
                for (final Restriction res : restrictions) {
                    if (!res.valide(valeur))
                        return(false);
                }
            }
            return(true);
        }
        
        protected boolean verifType(final String type, final String valeur) {
            if ("string".equals(type))
                return(true);
            else if ("normalizedString".equals(type))
                return(verifExpr(valeur, "[^\\t\\r\\n]*"));
            else if ("token".equals(type)) {
                if (valeur.indexOf('\n') != -1 || valeur.indexOf('\r') != -1 ||
                        valeur.indexOf('\t') != -1 || valeur.indexOf("  ") != -1)
                    return(false);
                return(!valeur.startsWith(" ") && !valeur.endsWith(" "));
            } else if ("base64Binary".equals(type))
                return(verifExpr(valeur, "(([a-zA-Z0-9+/=]\\s?){4})*"));
            else if ("hexBinary".equals(type))
                return(verifExpr(valeur, "(([0-9a-fA-F]){2})*"));
            else if ("integer".equals(type))
                return(verifExpr(valeur, "[+\\-]?\\d+"));
            else if ("positiveInteger".equals(type))
                return(verifExpr(valeur, "\\+?0*[1-9]\\d*"));
            else if ("negativeInteger".equals(type))
                return(verifExpr(valeur, "-0*[1-9]\\d*"));
            else if ("nonNegativeInteger".equals(type))
                return(verifExpr(valeur, "(-0+)|(\\+?\\d+)"));
            else if ("nonPositiveInteger".equals(type))
                return(verifExpr(valeur, "(\\+?0+)|(-\\d+)"));
            else if ("long".equals(type)) {
                if (!verifExpr(valeur, "[+\\-]?\\d+"))
                    return(false);
                try {
                    final BigInteger big = new BigInteger(valeur);
                    final BigInteger max = new BigInteger("9223372036854775807");
                    final BigInteger min = new BigInteger("-9223372036854775808");
                    if (big.compareTo(max) > 0)
                        return(false);
                    if (big.compareTo(min) < 0)
                        return(false);
                    return(true);
                } catch (final NumberFormatException ex) {
                    LOG.error("verifType(String, String) - NumberFormatException", ex);
                    return(false);
                }
            } else if ("unsignedLong".equals(type)) {
                if (!verifExpr(valeur, "\\d+"))
                    return(false);
                try {
                    final BigInteger big = new BigInteger(valeur);
                    final BigInteger max = new BigInteger("18446744073709551615");
                    return(big.compareTo(max) <= 0);
                } catch (final NumberFormatException ex) {
                    LOG.error("verifType(String, String) - NumberFormatException", ex);
                    return(false);
                }
            } else if ("int".equals(type)) {
                if (!verifExpr(valeur, "[+\\-]?\\d+"))
                    return(false);
                try {
                    final long val = Long.parseLong(valeur);
                    return(val <= 2147483647l && val >= -2147483648l);
                } catch (final NumberFormatException ex) {
                    LOG.error("verifType(String, String) - NumberFormatException", ex);
                    return(false);
                }
            } else if ("unsignedInt".equals(type)) {
                if (!verifExpr(valeur, "\\d+"))
                    return(false);
                try {
                    final long val = Long.parseLong(valeur);
                    return(val <= 4294967295l && val >= 0);
                } catch (final NumberFormatException ex) {
                    LOG.error("verifType(String, String) - NumberFormatException", ex);
                    return(false);
                }
            } else if ("short".equals(type)) {
                if (!verifExpr(valeur, "[+\\-]?\\d+"))
                    return(false);
                try {
                    final int val = Integer.parseInt(valeur);
                    return(val <= 32767 && val >= -32768);
                } catch (final NumberFormatException ex) {
                    LOG.error("verifType(String, String) - NumberFormatException", ex);
                    return(false);
                }
            } else if ("unsignedShort".equals(type)) {
                if (!verifExpr(valeur, "\\d+"))
                    return(false);
                try {
                    final int val = Integer.parseInt(valeur);
                    return(val <= 65535 && val >= 0);
                } catch (final NumberFormatException ex) {
                    LOG.error("verifType(String, String) - NumberFormatException", ex);
                    return(false);
                }
            } else if ("byte".equals(type)) {
                if (!verifExpr(valeur, "[+\\-]?\\d+"))
                    return(false);
                try {
                    final int val = Integer.parseInt(valeur);
                    return(val <= 127 && val >= -128);
                } catch (final NumberFormatException ex) {
                    LOG.error("verifType(String, String) - NumberFormatException", ex);
                    return(false);
                }
            } else if ("unsignedByte".equals(type)) {
                if (!verifExpr(valeur, "\\d+"))
                    return(false);
                try {
                    final int val = Integer.parseInt(valeur);
                    return(val <= 255 && val >= 0);
                } catch (final NumberFormatException ex) {
                    LOG.error("verifType(String, String) - NumberFormatException", ex);
                    return(false);
                }
            } else if ("decimal".equals(type)) {
                return(verifExpr(valeur, "[+\\-]?\\d+\\.?\\d*"));
            } else if ("float".equals(type)) {
                if (!verifExpr(valeur, "(-?INF)|(NaN)|([+\\-]?\\d+\\.?\\d*([eE][+\\-]?\\d{1,3})?)"))
                    return(false);
                if ("INF".equals(valeur) || "-INF".equals(valeur)) // "Infinity" en Java
                    return(true);
                try {
                    Float.parseFloat(valeur);
                    return(true);
                } catch (final NumberFormatException ex) {
                    return(false);
                }
            } else if ("double".equals(type)) {
                if (!verifExpr(valeur, "(-?INF)|(NaN)|([+\\-]?\\d+\\.?\\d*([eE][+\\-]?\\d{1,3})?)"))
                    return(false);
                if ("INF".equals(valeur) || "-INF".equals(valeur))
                    return(true);
                try {
                    Double.parseDouble(valeur);
                    return(true);
                } catch (final NumberFormatException ex) {
                    return(false);
                }
            } else if ("boolean".equals(type))
                return(verifExpr(valeur, "(true)|(false)|1|0"));
            else if ("duration".equals(type))
                return(verifExpr(valeur, "-?P(\\d{1,4}Y)?(\\d{1,2}M)?(\\d{1,2}D)?T?(\\d{1,2}H)?(\\d{1,2}M)?(\\d{1,2}(\\.\\d+)?S)?"));
            else if ("dateTime".equals(type))
                return(verifExpr(valeur, "-?\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d(\\.\\d+)?(([+\\-][01]\\d:\\d{2})|Z)?"));
            else if ("date".equals(type))
                return(verifExpr(valeur, "-?\\d{4}-[01]\\d-[0-3]\\d(([+\\-][01]\\d:\\d{2})|Z)?"));
            else if ("time".equals(type))
                return(verifExpr(valeur, "[0-2]\\d:[0-5]\\d:[0-5]\\d(\\.\\d+)?(([+\\-][01]\\d:\\d{2})|Z)?"));
            else if ("gYear".equals(type))
                return(verifExpr(valeur, "-?\\d{4}(([+\\-][01]\\d:\\d{2})|Z)?"));
            else if ("gYearMonth".equals(type))
                return(verifExpr(valeur, "-?\\d{4}-[01]\\d(([+\\-][01]\\d:\\d{2})|Z)?"));
            else if ("gMonth".equals(type))
                return(verifExpr(valeur, "--[01]\\d(([+\\-][01]\\d:\\d{2})|Z)?"));
            else if ("gMonthDay".equals(type))
                return(verifExpr(valeur, "--[01]\\d-[0-3]\\d(([+\\-][01]\\d:\\d{2})|Z)?"));
            else if ("gDay".equals(type))
                return(verifExpr(valeur, "---[0-3]\\d(([+\\-][01]\\d:\\d{2})|Z)?"));
            else if ("Name".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",0-9.\\-\\s][^<>&#!/?'\",\\s]*")); // en fait plus restrictif: \i\c*
            else if ("QName".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",0-9.\\-\\s][^<>&#!/?'\",\\s]*")); // en fait plus restrictif
            else if ("NCName".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",0-9.\\-\\s:][^<>&#!/?'\",:\\s]*")); // en fait plus restrictif: [\i-[:]][\c-[:]]*
            else if ("anyURI".equals(type))
                return(true);
                //return(verifExpr(valeur, "([^:/?#]+:)?(//[^/?#]*)?[^?#]*(\\?[^#]*)?(#.*)?"));
                // pb: cette expression autorise tout!
                // (mais les RFC 2396 et 2732 ne restreignent rien)
            else if ("language".equals(type))
                return(verifExpr(valeur, "[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*"));
            else if ("ID".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",0-9.\\-\\s:][^<>&#!/?'\",:\\s]*")); // comme NCName
            else if ("IDREF".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",0-9.\\-\\s:][^<>&#!/?'\",:\\s]*")); // comme NCName
            else if ("IDREFS".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",0-9.\\-\\s:][^<>&#!/?'\",:]*"));
            else if ("ENTITY".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",0-9.\\-\\s:][^<>&#!/?'\",:\\s]*")); // comme NCName
            else if ("ENTITIES".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",0-9.\\-\\s:][^<>&#!/?'\",:]*")); // comme IDREFS
            else if ("NOTATION".equals(type))
                return(verifExpr(valeur, "[^0-9.\\-\\s][^\\s]*(\\s[^0-9.\\-\\s][^\\s]*)*"));
                // la facette enumeration est obligatoire -> contrainte supplémentaire
            else if ("NMTOKEN".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",\\s]+")); // en fait plus restrictif: \c+
            else if ("NMTOKENS".equals(type))
                return(verifExpr(valeur, "[^<>&#!/?'\",]+")); // en fait plus restrictif
            else
                return(true);
        }
        
        protected boolean verifExpr(final String valeur, final String regexp) {
            try {
    //            final Pattern r = compiler.compile("^" + regexp + "$");
                // on pourrait utiliser un cache, comme dans Config
    //            final String regex = "^" + regex + "$";
                Pattern r = PATTERN_CACHE.get(regexp);
                if (r == null) {
                    r = Pattern.compile("^" + regexp + "$");
                    PATTERN_CACHE.put(regexp, r);
                    return r.matcher(valeur).matches();
                }
    //            return(matcher.matches(valeur, r));
    //        } catch (final MalformedPatternException ex) {
                return r.matcher(valeur).matches();
            } catch (final PatternSyntaxException ex) {
                LOG.error("verifExpr(String, String): " + regexp, ex);
                return(true);
            }
        }
        
        protected String getBaseType() {
            return(baseType);
        }
        
        protected ArrayList<String> getEnumeration() {
            return(enumeration);
        }
    }
    
    
    class Union {
        private ArrayList<Contraintes> contraintes = null;
        
        public Union(final Config cfg, final Element u) {
            contraintes = new ArrayList<Contraintes>();
            Node n = u.getFirstChild();
            while (n != null) {
                if (n instanceof Element && n.getLocalName().equals("simpleType"))
                    contraintes.add(new Contraintes(cfg, (Element)n));
                n = n.getNextSibling();
            }
            final String memberTypes = u.getAttribute("memberTypes");
            if (!"".equals(memberTypes)) {
                final String[] types = memberTypes.split("\\s");
                for (String type : types)
                    contraintes.add(new Contraintes(cfg, type, u));
            }
        }
        
        protected boolean valide(final String valeur) {
            if (contraintes == null)
                return(true);
            for (Contraintes c : contraintes)
                if (c.valide(valeur))
                    return(true);
            return(false);
        }
        
        protected ArrayList<String> getEnumeration() {
            ArrayList<String> enumeration = null;
            for (Contraintes c : contraintes) {
                ArrayList<String> ec = c.getEnumeration();
                if (ec == null)
                    return(null);
                else {
                    if (enumeration == null)
                        enumeration = new ArrayList<String>();
                    enumeration.addAll(ec);
                }
            }
            return(enumeration);
        }
    }
    
    
    class Liste {
        private Contraintes contraintesItem = null;
        
        public Liste(final Config cfg, final Element u) {
            contraintesItem = null;
            Node n = u.getFirstChild();
            while (n != null) {
                if (n instanceof Element && n.getLocalName().equals("simpleType")) {
                    contraintesItem = new Contraintes(cfg, (Element)n);
                    break;
                }
                n = n.getNextSibling();
            }
            if (contraintesItem == null) {
                final String itemType = u.getAttribute("itemType");
                if (!"".equals(itemType))
                    contraintesItem = new Contraintes(cfg, itemType, u);
            }
        }
        
        protected boolean valide(final String valeur) {
            if (contraintesItem == null)
                return(true);
            final String[] items = valeur.trim().split("\\s");
            for (String item : items)
                if (!contraintesItem.valide(item))
                    return(false);
            return(true);
        }
        
        protected ArrayList<String> getEnumeration() {
            return(null);
        }
    }
    
    
    class Restriction {
        private final String nom;
        private final String param;
        private int iparam;
        
        public Restriction(final String nom, final String param) {
            this.nom = nom;
            this.param = param;
            try {
                this.iparam = Integer.parseInt(param);
            } catch (final NumberFormatException ex) {
            }
        }
        
        public boolean valide(final String valeur) {
            if ("length".equals(nom))
                return(valeur.length() == iparam);
            else if ("minLength".equals(nom))
                return(valeur.length() >= iparam);
            else if ("maxLength".equals(nom))
                return(valeur.length() <= iparam);
            else if ("maxInclusive".equals(nom)) {
                try {
                    final double val = Double.parseDouble(valeur);
                    return(val <= iparam);
                } catch (final NumberFormatException ex) {
                    return(false);
                }
            } else if ("maxExclusive".equals(nom)) {
                try {
                    final double val = Double.parseDouble(valeur);
                    return(val < iparam);
                } catch (final NumberFormatException ex) {
                    return(false);
                }
            } else if ("minInclusive".equals(nom)) {
                try {
                    final double val = Double.parseDouble(valeur);
                    return(val >= iparam);
                } catch (final NumberFormatException ex) {
                    return(false);
                }
            } else if ("minExclusive".equals(nom)) {
                try {
                    final double val = Double.parseDouble(valeur);
                    return(val > iparam);
                } catch (final NumberFormatException ex) {
                    return(false);
                }
            } else if ("whiteSpace".equals(nom)) {
                if ("collapse".equals(param))
                    return(!"replace".equals(valeur) && !"preserve".equals(valeur));
                else if ("replace".equals(param))
                    return(!"preserve".equals(valeur));
                else
                    return(true);
            } else if ("totalDigits".equals(nom)) {
                int nb = 0;
                for (int i=0; i<valeur.length(); i++)
                    if (valeur.charAt(i) >= '0' && valeur.charAt(i) <= '9')
                        nb++;
                return(nb <= iparam);
            } else if ("fractionDigits".equals(nom)) {
                int nb = 0;
                boolean apres = false;
                for (int i=0; i<valeur.length(); i++) {
                    if (!apres) {
                        if (valeur.charAt(i) == '.')
                            apres = true;
                    } else if (valeur.charAt(i) >= '0' && valeur.charAt(i) <= '9')
                        nb++;
                }
                return(nb <= iparam);
            } else
                return(true);
        }
    }

}
