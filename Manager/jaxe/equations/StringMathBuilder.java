/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations;

import org.apache.log4j.Logger;

import java.util.regex.PatternSyntaxException;
import java.util.Vector;

import jaxe.equations.element.MathElement;
import jaxe.equations.element.MathFrac;
import jaxe.equations.element.MathIdentifier;
import jaxe.equations.element.MathNumber;
import jaxe.equations.element.MathOperator;
import jaxe.equations.element.MathOver;
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

/**
 * Analyseur d'équation avec la syntaxe particulière de Jaxe.
 * L'analyse se fait en 2 étape :
 * - parsing du string avec l'équation, avec la création de structures en mémoire représentant le sens mathématique de l'expression
 * - transformation en MathML présentationnel
 */
public class StringMathBuilder {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(StringMathBuilder.class);

    private final MathRootElement rootElement;
    
    // opérateurs remplacés en premier, avant l'analyse de la syntaxe
    private static final String[][] special = {{"<==", "\u21D0"}, {"==>", "\u21D2"}, {"<=>", "\u21D4"},
                                        {"!=", "\u2260"}, {"~=", "\u2248"}, {"<=", "\u2264"},
                                        {">=", "\u2265"}, {"<<", "\u226A"}, {">>", "\u226B"},
                                        //  "->" kepts for backward-compatibility
                                        {"-->", "\u2192"}, {"<->", "\u2194"}, {"->", "\u2192"},
                                        {"<--", "\u2190"},
                                        {"forall", "\u2200"}, {"quelquesoit", "\u2200"},
                                        {"exists", "\u2203"}, {"ilexiste", "\u2203"},
                                        {"part", "\u2202"}, {"drond", "\u2202"},
                                        {"nabla", "\u2207"}, {"prop", "\u221D"},
                                        {"cross", "×"}, {"croix", "×"},
                                        {"plusmn", "±"}, {"plusoumoins", "±"}, {"plusminus", "±"},
                                        {"...", "\u2026"}};
    
    // opérateurs
    private static final String sops = 
            "_^*/\u2207±\u2213-+\u2200\u2203×=\u2260\u2248<>\u2264\u2265\u226A\u226B\u221D" +
            "\u2190\u2192\u2194\u21D0\u21D2\u21D4";
    
    private static final String[][] symbols = {
                    // grec-minuscule
                    {"alpha", "\u03B1"}, {"beta", "\u03B2"}, {"gamma", "\u03B3"},
                    {"delta", "\u03B4"}, {"epsilon", "\u03B5"}, {"zeta", "\u03B6"},
                    {"eta", "\u03B7"}, {"theta", "\u03B8"}, {"iota", "\u03B9"},
                    {"kappa", "\u03BA"}, {"lambda", "\u03BB"}, {"mu", "\u03BC"},
                    {"nu", "\u03BD"}, {"xi", "\u03BE"}, {"omicron", "\u03BF"},
                    {"pi", "\u03C0"}, {"rho", "\u03C1"}, {"sigma", "\u03C3"},
                    {"tau", "\u03C4"}, {"upsilon", "\u03C5"}, {"phi", "\u03C6"},
                    {"chi", "\u03C7"}, {"psi", "\u03C8"}, {"omega", "\u03C9"},
                    // grec-majuscule
                    {"Alpha", "\u0391"}, {"Beta", "\u0392"}, {"Gamma", "\u0393"},
                    {"Delta", "\u0394"}, {"Epsilon", "\u0395"}, {"Zeta", "\u0396"},
                    {"Eta", "\u0397"}, {"Theta", "\u0398"}, {"Iota", "\u0399"},
                    {"Kappa", "\u039A"}, {"Lambda", "\u039B"}, {"Mu", "\u039C"},
                    {"Nu", "\u039D"}, {"Xi", "\u039E"}, {"Omicron", "\u039F"},
                    {"Pi", "\u03A0"}, {"Rho", "\u03A1"}, {"Sigma", "\u03A3"},
                    {"Tau", "\u03A4"}, {"Upsilon", "\u03A5"}, {"Phi", "\u03A6"},
                    {"Chi", "\u03A7"}, {"Psi", "\u03A8"}, {"Omega", "\u03A9"},
                    // autre grec
                    {"thetasym", "\u03D1"}, {"upsih", "\u03D2"}, {"piv", "\u03D6"},
                    // autres caractères
                    {"infin", "\u221E"}, {"infty", "\u221E"}, {"infini", "\u221E"},
                    {"sun", "\u2609"}, {"soleil", "\u2609"},
                    {"star", "\u2605"}, {"étoile", "\u2605"},
                    {"earth", "\u2641"}, {"terre", "\u2641"},
                    {"planck", "\u210F"},
                    {"angstrom", "\u212B"}, {"angström", "\u212B"},
                    {"petitl", "\u2113"},
                    {"asterisk", "*"}, {"astérisque", "*"}, // \uFF0A ?
                    /*{"grandg", "\u1D4A2"},*/
                };
    
    // fonctions qui peuvent se passer de parenthèses quand il n'y a qu'un argument simple
    private static final String[] fctnopar = {"sin", "cos", "tan", "acos", "asin", "atan"};
    
    
    public StringMathBuilder(final String s) {
        rootElement = new MathRootElement();
        final String s2 = ajParentheses(replaceSpecial(s));
        if (!s.equals("")) {
            final JEQ jeq = parser(s2);
            final MathElement me;
            if (jeq == null)
                me = null;
            else
                me = jeq.versMathML();
            rootElement.addMathElement(me);
        }
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
    
    public String replaceSpecial(String s) {
        for (final String[] spec : special) {
            int ind = s.indexOf(spec[0]);
            while (ind != -1) {
                s = s.substring(0, ind) + spec[1] + s.substring(ind + spec[0].length());
                ind = s.indexOf(spec[0]);
            }
        }
        return s;
    }
    
    public static String ajParentheses(String s) {
        // d'abord ajouter des parenthèses pour séparer les éléments des fonctions
        // f(a+1;b;c) -> f((a+1);b;c)
        int indop = s.indexOf(';');
        while (indop != -1) {
            // vers la gauche du ;
            int pp = 0;
            boolean yaop = false;
            char c;
            for (int i=indop-1; i>=0 && pp>=0; i--) {
                c = s.charAt(i);
                if (c == ';' && pp == 0)
                    break; // les parenthèses sont déjà ajoutées
                if (c == '(')
                    pp--;
                else if (c == ')')
                    pp++;
                else if (sops.indexOf(c) != -1)
                    yaop = true;
                if (pp < 0 && yaop) {
                    s = s.substring(0,i) + '(' + s.substring(i,indop) + ')' + s.substring(indop);
                    indop += 2;
                }
            }
            // vers la droite du ;
            pp = 0;
            yaop = false;
            for (int i=indop+1; i<s.length() && pp>=0; i++) {
                c = s.charAt(i);
                if (c == '(')
                    pp++;
                else if (c == ')')
                    pp--;
                else if (sops.indexOf(c) != -1)
                    yaop = true;
                if ((pp < 0 || pp == 0 && c == ';') && yaop)
                    s = s.substring(0,indop+1) + '(' + s.substring(indop+1,i) + ')' + s.substring(i);
                if (c == ';' && pp == 0)
                    break;
            }
            final int indop2 = s.substring(indop+1).indexOf(';');
            if (indop2 == -1)
                indop = indop2;
            else
                indop += indop2 + 1;
        }
        
        // les autres parenthèses
        for (int iops=0; iops<sops.length(); iops++) {
            final char cops = sops.charAt(iops);
            indop = s.indexOf(cops);
            int nindop = indop;
            int im,ip;
            char cm=' ',cp=' ';
            int pp;
            boolean ajp;
            while (nindop != -1) {
                ajp = false;
                im = indop - 1;
                if (im >= 0)
                    cm = s.charAt(im);
                pp = 0;
                while (im >= 0 && (pp != 0 || cm != '(') &&
                    (pp != 0 || sops.indexOf(cm) == -1)) {
                    if (cm == ')')
                        pp++;
                    else if (cm == '(')
                        pp--;
                    im--;
                    if (im >= 0)
                        cm = s.charAt(im);
                }
                if (im < 0 || sops.indexOf(cm) != -1)
                    ajp = true;
                ip = indop + 1;
                if (ip >= 0 && ip <= s.length()-1)
                    cp = s.charAt(ip);
                pp = 0;
                while (ip < s.length() && (pp != 0 || cp != ')') &&
                    (pp != 0 || sops.indexOf(cp) == -1)) {
                    if (cp == '(')
                        pp++;
                    else if (cp == ')')
                        pp--;
                    ip++;
                    if (ip < s.length())
                        cp = s.charAt(ip);
                }
                if (ip >= s.length() || sops.indexOf(cp) != -1)
                    ajp = true;
                if (ajp) {
                    s = s.substring(0, im+1) + "(" + s.substring(im+1, ip) + ")" +
                        s.substring(ip);
                    indop++;
                }
                nindop = s.substring(indop+1).indexOf(cops);
                indop = nindop + indop+1;
            }
        }
        return s;
    }
    
    public JEQ parser(String s) {
        if (s == null || "".equals(s))
            return null;
        
        if (s.charAt(0) == '(' && s.charAt(s.length()-1) == ')') {
            int pp = 0;
            for (int i=1; i<s.length()-1; i++) {
                if (s.charAt(i) == '(')
                    pp++;
                else if (s.charAt(i) == ')')
                    pp--;
                if (pp == -1)
                    break;
            }
            if (pp != -1)
                s = s.substring(1, s.length()-1);
        }
        
        int indop = -1;
        int pp = 0;
        for (int i=0; i<s.length(); i++) {
            if (pp == 0 && sops.indexOf(s.charAt(i)) != -1) {
                indop = i;
                break;
            } else if (s.charAt(i) == '(')
                pp++;
            else if (s.charAt(i) == ')')
                pp--;
        }
        if (indop == -1) {
            boolean nb;
            try {
                nb = s.matches("\\s?([0-9]+([\\.,][0-9]+)?|[\\.,][0-9]+)(E[+-]?[0-9]+)?\\s?");
            } catch (final PatternSyntaxException ex) {
                nb = false;
            }
            if (nb)
                return(new JEQNombre(s));
            final int indf = s.indexOf('(');
            if (indf != -1 && s.charAt(s.length()-1) == ')') {
                // nomfct(p1; p2; ...) ou (nomfctcomplexe)(p1; p2; ...) ?
                // comme (sin^2)(alpha) ou (theta_f)(1)
                // recherche d'une deuxième parenthèse au même niveau que la première
                int indf2 = -1;
                pp = 0;
                for (int i=0; i<s.length(); i++) {
                    final char c = s.charAt(i);
                    if (c == '(' && pp == 0 && i != indf) {
                        indf2 = i;
                        break;
                    } else if (c == '(')
                        pp++;
                    else if (c == ')')
                        pp--;
                }
                String nomfct = null;
                JEQ nom = null;
                if (indf2 == -1) {
                    nom = new JEQVariable(s.substring(0,indf));
                    s = s.substring(indf+1, s.length()-1);
                } else {
                    nom = parser(s.substring(0, indf2));
                    s = s.substring(indf2+1, s.length()-1);
                }
                // recherche des paramètres
                final Vector<JEQ> vp = new Vector<JEQ>();
                //indv = s.indexOf(';'); marche pas avec f(g(a;b);c)
                int indv = -1;
                pp = 0;
                for (int i=0; i<s.length(); i++) {
                    final char c = s.charAt(i);
                    if (c == ';' && pp == 0 ) {
                        indv = i;
                        break;
                    } else if (c == '(')
                        pp++;
                    else if (c == ')')
                        pp--;
                }
                if (indv == -1)
                    vp.add(parser(s.trim()));
                else
                    while (indv != -1) {
                        vp.add(parser(s.substring(0,indv).trim()));
                        s = s.substring(indv+1);
                        indv = -1;
                        pp = 0;
                        for (int i=0; i<s.length(); i++) {
                            final char c = s.charAt(i);
                            if (c == ';' && pp == 0 ) {
                                indv = i;
                                break;
                            } else if (c == '(')
                                pp++;
                            else if (c == ')')
                                pp--;
                        }
                        if (indv == -1)
                            vp.add(parser(s.trim()));
                    }
                return(new JEQFonction(nom, vp));
            } else
                return(new JEQVariable(s));
        }
        
        final char op = s.charAt(indop);
        final String s1 = s.substring(0,indop).trim();
        JEQ p1;
        if (s1.equals(""))
            p1 = null;
        else
            p1 = parser(s1);
        final String s2 = s.substring(indop+1).trim();
        JEQ p2;
        if (s2.equals(""))
            p2 = null;
        else
            p2 = parser(s2);
        
        return(new JEQOperation(op, p1, p2));
    }
    
    private static MathElement elemOrQuestion(final JEQ jeq) {
        if (jeq != null)
            return jeq.versMathML();
        final MathText mtext = new MathText();
        mtext.addText("?");
        return mtext;
    }
    
    
    interface JEQ {
        public MathElement versMathML();
        public void setUnites();
    }
    
    class JEQFonction implements JEQ {
        JEQ nom;
        Vector<JEQ> vp;
        boolean unites;
        public JEQFonction(final JEQ nom, final Vector<JEQ> arguments) {
            this.nom = nom;
            vp = arguments;
            unites = ("unité".equals(getNomFct()) || "unit".equals(getNomFct()));
            if (unites)
                setUnites();
        }
        public void setUnites() {
            unites = true;
            for (JEQ param : vp)
                if (param != null)
                    param.setUnites();
        }
        public String getNomFct() {
            if (nom instanceof JEQVariable)
                return(((JEQVariable)nom).nom);
            else
                return(null);
        }
        public MathElement versMathML() {
            JEQ p1, p2, p3, p4;
            if (vp.size() > 0)
                p1 = vp.get(0);
            else
                p1 = null;
            if (vp.size() > 1)
                p2 = vp.get(1);
            else
                p2 = null;
            if (vp.size() > 2)
                p3 = vp.get(2);
            else
                p3 = null;
            if (vp.size() > 3)
                p4 = vp.get(3);
            else
                p4 = null;
            String nomfct = getNomFct();
            MathElement me;
            if ("sqrt".equals(nomfct) || "racine".equals(nomfct)) {
                me = new MathSqrt();
                me.addMathElement(elemOrQuestion(p1));
            } else if ("exp".equals(nomfct)) {
                me = new MathSup();
                final MathIdentifier mi = new MathIdentifier();
                mi.addText("e");
                me.addMathElement(mi);
                me.addMathElement(elemOrQuestion(p1));
            } else if ("abs".equals(nomfct)) {
                me = new MathRow();
                MathOperator mo = new MathOperator();
                mo.addText("|");
                me.addMathElement(mo);
                me.addMathElement(elemOrQuestion(p1));
                mo = new MathOperator();
                mo.addText("|");
                me.addMathElement(mo);
            } else if ("fact".equals(nomfct) || "factorielle".equals(nomfct)
                        || "factorial".equals(nomfct)) {
                me = new MathRow();
                MathOperator mo;
                if (!(p1 instanceof JEQVariable || p1 instanceof JEQNombre)) {
                    mo = new MathOperator();
                    mo.addText("(");
                    me.addMathElement(mo);
                }
                me.addMathElement(p1.versMathML());
                if (!(p1 instanceof JEQVariable || p1 instanceof JEQNombre)) {
                    mo = new MathOperator();
                    mo.addText(")");
                    me.addMathElement(mo);
                }
                mo = new MathOperator();
                mo.addText("!");
                me.addMathElement(mo);
            } else if ("int".equals(nomfct) || "intégrale".equals(nomfct)) {
                me = new MathRow();
                MathOperator mo = new MathOperator();
                mo.addText("\u222B");
                mo.setStretchy(true);
                if (p3 != null && p4 != null) {
                    final MathUnderOver munderover = new MathUnderOver();
                    munderover.addMathElement(mo);
                    munderover.addMathElement(elemOrQuestion(p3));
                    munderover.addMathElement(elemOrQuestion(p4));
                    me.addMathElement(munderover);
                } else if (p3 != null) {
                    final MathUnder munder = new MathUnder();
                    munder.addMathElement(mo);
                    munder.addMathElement(elemOrQuestion(p3));
                    me.addMathElement(munder);
                } else if (p4 != null) {
                    final MathOver mover = new MathOver();
                    mover.addMathElement(mo);
                    mover.addMathElement(elemOrQuestion(p4));
                    me.addMathElement(mover);
                } else
                    me.addMathElement(mo);
                final MathRow mrow = new MathRow();
                mrow.addMathElement(elemOrQuestion(p1));
                // <mo> &InvisibleTimes; </mo>
                if (p2 != null) {
                    mo = new MathOperator();
                    mo.addText("d"); // &DifferentialD;
                    mrow.addMathElement(mo);
                    mrow.addMathElement(p2.versMathML());
                }
                me.addMathElement(mrow);
            } else if ("prod".equals(nomfct) || "sum".equals(nomfct) ||
                    "produit".equals(nomfct) || "somme".equals(nomfct)) {
                me = new MathRow();
                final MathUnderOver munderover = new MathUnderOver();
                final MathOperator mo = new MathOperator();
                if ("prod".equals(nomfct) || "produit".equals(nomfct))
                    mo.addText("\u220F");
                else if ("sum".equals(nomfct) || "somme".equals(nomfct))
                    mo.addText("\u2211");
                mo.setStretchy(true);
                munderover.addMathElement(mo);
                munderover.addMathElement(elemOrQuestion(p2));
                munderover.addMathElement(elemOrQuestion(p3));
                me.addMathElement(munderover);
                final MathRow mrow = new MathRow();
                mrow.addMathElement(elemOrQuestion(p1));
                me.addMathElement(mrow);
            } else if ("over".equals(nomfct) || "dessus".equals(nomfct)) {
                final MathOver mover = new MathOver();
                mover.addMathElement(elemOrQuestion(p1));
                mover.addMathElement(elemOrQuestion(p2));
                me = mover;
            } else if ("subsup".equals(nomfct)) {
                final MathSubSup msubsup = new MathSubSup();
                msubsup.addMathElement(elemOrQuestion(p1));
                msubsup.addMathElement(elemOrQuestion(p2));
                msubsup.addMathElement(elemOrQuestion(p3));
                me = msubsup;
            } else if ("accent".equals(nomfct)) {
                final MathOver mover = new MathOver();
                mover.setAccent(true);
                mover.addMathElement(elemOrQuestion(p1));
                mover.addMathElement(elemOrQuestion(p2));
                me = mover;
            } else if ("matrix".equals(nomfct) || "matrice".equals(nomfct)) {
                me = new MathRow();
                MathOperator mo = new MathOperator();
                mo.addText("(");
                me.addMathElement(mo);
                final MathTable mtable = new MathTable();
                for (final JEQ mel : vp)
                    mtable.addMathElement(elemOrQuestion(mel));
                me.addMathElement(mtable);
                mo = new MathOperator();
                mo.addText(")");
                me.addMathElement(mo);
            } else if ("system".equals(nomfct) || "système".equals(nomfct)) {
                me = new MathRow();
                final MathOperator mo = new MathOperator();
                mo.addText("{");
                me.addMathElement(mo);
                final MathTable mtable = new MathTable();
                for (final JEQ mel : vp) {
                    final MathRow mrow = new MathRow();
                    final MathTableData mtd = new MathTableData();
                    mtd.setColumnAlign("left");
                    mtd.addMathElement(elemOrQuestion(mel));
                    mrow.addMathElement(mtd);
                    mtable.addMathElement(mrow);
                }
                me.addMathElement(mtable);
            } else if ("line".equals(nomfct) || "ligne".equals(nomfct)) {
                me = new MathTableRow();
                for (final JEQ mel : vp) {
                    final MathTableData mtd = new MathTableData();
                    mtd.addMathElement(elemOrQuestion(mel));
                    me.addMathElement(mtd);
                }
            } else if ("slash".equals(nomfct)) {
                me = new MathRow();
                me.addMathElement(elemOrQuestion(p1));
                final MathOperator mo = new MathOperator();
                mo.addText("/"); // could be \u2215
                me.addMathElement(mo);
                me.addMathElement(elemOrQuestion(p2));
            } else if ("frac".equals(nomfct) || "fraction".equals(nomfct)) {
                final MathFrac mfrac = new MathFrac();
                mfrac.addMathElement(elemOrQuestion(p1));
                mfrac.addMathElement(elemOrQuestion(p2));
                me = mfrac;
            } else if ("pscalaire".equals(nomfct) || "scalarp".equals(nomfct)) {
                final MathRow mrow = new MathRow();
                mrow.addMathElement(elemOrQuestion(p1));
                final MathOperator mo = new MathOperator();
                mo.addText(".");
                mrow.addMathElement(mo);
                mrow.addMathElement(elemOrQuestion(p2));
                return mrow;
            } else if ("dtemps".equals(nomfct) || "timed".equals(nomfct)) {
                final MathOver mover = new MathOver();
                mover.setAccent(true);
                mover.addMathElement(elemOrQuestion(p1));
                MathText mt = new MathText();
                if (p2 instanceof JEQNombre) {
                    try {
                        final int n = Integer.parseInt(((JEQNombre)p2).valeur);
                        String spts = "";
                        for (int i=0; i<n; i++)
                            spts = spts + '.';
                        mt.addText(spts);
                    } catch (final NumberFormatException ex) {
                        mt.addText("?");
                    }
                } else
                    mt.addText("?");
                mover.addMathElement(mt);
                me = mover;
            } else if ("unité".equals(nomfct) || "unit".equals(nomfct)) {
                final MathRow mrow = new MathRow();
                mrow.addMathElement(elemOrQuestion(p1));
                final MathOperator mo = new MathOperator();
                mo.addText(" ");
                mrow.addMathElement(mo);
                mrow.addMathElement(elemOrQuestion(p2));
                return mrow;
            } else {
                me = new MathRow();
                boolean par = true;
                if (nom instanceof JEQVariable) {
                    MathText mt = new MathText();
                    for (final String[] element : symbols)
                        if (element[0].equals(nomfct)) {
                            nomfct = element[1];
                            break;
                        }
                    mt.addText(nomfct);
                    if (p2 == null && p1 instanceof JEQVariable)
                        for (final String element : fctnopar)
                            if (element.equals(nomfct)) {
                                par = false;
                                break;
                            }
                    me.addMathElement(mt);
                } else
                    me.addMathElement(nom.versMathML());
                if (par) {
                    final MathOperator mo = new MathOperator();
                    mo.addText("("); // \u2061 = &ApplyFunction; serait mieux mais pas reconnu
                    me.addMathElement(mo);
                }
                me.addMathElement(p1.versMathML());
                for (int i=1; i<vp.size(); i++) { // ATTENTION, LA BOUCLE COMMENCE A 1
                    final MathOperator mo = new MathOperator();
                    mo.addText(";");
                    me.addMathElement(mo);
                    me.addMathElement(vp.get(i).versMathML());
                }
                if (par) {
                    final MathOperator mo = new MathOperator();
                    mo.addText(")");
                    me.addMathElement(mo);
                } else {
                    final MathText mtext = new MathText();
                    mtext.addText("\u00A0"); // nbsp, mspace serait mieux
                    me.addMathElement(mtext);
                }
            }
            return me;
        }
    }
    
    class JEQOperation implements JEQ {
        char op;
        JEQ p1, p2;
        boolean unites;
        public JEQOperation(final char operateur, final JEQ p1, final JEQ p2) {
            op = operateur;
            this.p1 = p1;
            this.p2 = p2;
            unites = false;
        }
        public void setUnites() {
            unites = true;
            if (p1 != null)
                p1.setUnites();
            if (p2 != null)
                p2.setUnites();
        }
        public MathElement versMathML() {
            if (op == '/') {
                final MathFrac mfrac = new MathFrac();
                mfrac.addMathElement(elemOrQuestion(p1));
                mfrac.addMathElement(elemOrQuestion(p2));
                return mfrac;
            } else if (op == '^') {
                final MathSup msup = new MathSup();
                boolean par;
                if (p1 instanceof JEQFonction) {
                    String nomfct = ((JEQFonction)p1).getNomFct();
                    if ("sqrt".equals(nomfct) || "racine".equals(nomfct))
                        par = false;
                    else if ("abs".equals(nomfct))
                        par = false;
                    else if ("matrice".equals(nomfct))
                        par = false;
                    else if ("dtemps".equals(nomfct) || "timed".equals(nomfct))
                        par = false;
                    else
                        par = true;
                } else if (p1 instanceof JEQOperation) {
                    char op = ((JEQOperation)p1).op;
                    if (op == '_')
                        par = false;
                    else
                        par = true;
                        
                } else
                    par = false;
                MathElement me1;
                if (par)
                    me1 = ajPar(p1.versMathML());
                else
                    me1 = elemOrQuestion(p1);
                msup.addMathElement(me1);
                msup.addMathElement(elemOrQuestion(p2));
                return msup;
            } else if (op == '_') {
                final MathSub msub = new MathSub();
                msub.addMathElement(elemOrQuestion(p1));
                msub.addMathElement(elemOrQuestion(p2));
                return msub;
            } else if (op == '*') {
                final MathRow mrow = new MathRow();
                MathElement me1 = elemOrQuestion(p1);
                if (p1 instanceof JEQOperation && "+-".indexOf(((JEQOperation)p1).op) != -1)
                    me1 = ajPar(me1);
                mrow.addMathElement(me1);
                
                JEQ dernierDansP1 = p1;
                if (p1 != null)
                    while (dernierDansP1 instanceof JEQOperation && ((JEQOperation)dernierDansP1).p2 != null) {
                        dernierDansP1 = ((JEQOperation)dernierDansP1).p2;
                    }
                final boolean p1nombre = dernierDansP1 instanceof JEQNombre;
                JEQ premierDansP2 = p2;
                if (p2 != null)
                    while (premierDansP2 instanceof JEQOperation && ((JEQOperation)premierDansP2).p1 != null) {
                        premierDansP2 = ((JEQOperation)premierDansP2).p1;
                    }
                final boolean p2nombre = premierDansP2 instanceof JEQNombre;
                boolean pscalaire1 = false;
                if (p1 instanceof JEQFonction) {
                    final String nomfct = ((JEQFonction)p1).getNomFct();
                    if ("pscalaire".equals(nomfct) || "scalarp".equals(nomfct))
                        pscalaire1 = true;
                }
                boolean pscalaire2 = false;
                if (p2 instanceof JEQFonction) {
                    final String nomfct = ((JEQFonction)p2).getNomFct();
                    if ("pscalaire".equals(nomfct) || "scalarp".equals(nomfct))
                        pscalaire2 = true;
                }
                if ((p1nombre && p2nombre) || (pscalaire1 && pscalaire2)) {
                    final MathOperator mo = new MathOperator();
                    mo.addText("×");
                    mrow.addMathElement(mo);
                } else {
                    if (unites) {
                        final MathOperator mo = new MathOperator();
                        mo.addText(".");
                        mrow.addMathElement(mo);
                    }
                    // else <mo> &InvisibleTimes; </mo>
                }
                
                MathElement me2 = elemOrQuestion(p2);
                if (p2 instanceof JEQOperation && "+-".indexOf(((JEQOperation)p2).op) != -1)
                    me2 = ajPar(me2);
                mrow.addMathElement(me2);
                return mrow;
            } else if (op == '-') {
                final MathRow mrow = new MathRow();
                if (p1 != null)
                    mrow.addMathElement(p1.versMathML());
                final MathOperator mo = new MathOperator();
                mo.addText("-");
                mrow.addMathElement(mo);
                if (p2 != null) {
                    MathElement me2 = p2.versMathML();
                    if (p2 instanceof JEQOperation && "+-".indexOf(((JEQOperation)p2).op) != -1)
                        me2 = ajPar(me2);
                    mrow.addMathElement(me2);
                }
                return mrow;
            } else {
                final MathRow mrow = new MathRow();
                if (p1 != null)
                    mrow.addMathElement(p1.versMathML());
                final MathOperator mo = new MathOperator();
                mo.addText(Character.toString(op));
                if ("=\u2260\u2248\u2264\u2265\u226A\u226B".indexOf(op) != -1) {
                    // espace autour des opérateurs d'égalité
                    mo.setLspace(0.5);
                    mo.setRspace(0.5);
                }
                mrow.addMathElement(mo);
                if (p2 != null)
                    mrow.addMathElement(p2.versMathML());
                return mrow;
            }
        }
        private MathElement ajPar(final MathElement me) {
            final MathRow mrow = new MathRow();
            MathOperator mo = new MathOperator();
            mo.addText("(");
            mrow.addMathElement(mo);
            mrow.addMathElement(me);
            mo = new MathOperator();
            mo.addText(")");
            mrow.addMathElement(mo);
            return mrow;
        }
    }
    
    class JEQNombre implements JEQ {
        String valeur;
        public JEQNombre(final String valeur) {
            this.valeur = valeur;
        }
        public void setUnites() {
        }
        public MathElement versMathML() {
            final MathNumber mn = new MathNumber();
            mn.addText(valeur);
            return mn;
        }
    }
    
    class JEQVariable implements JEQ {
        String nom;
        boolean unites;
        public JEQVariable(final String nom) {
            this.nom = nom;
            unites = false;
        }
        public void setUnites() {
            unites = true;
        }
        public MathElement versMathML() {
            if ("hat".equals(nom) || "chapeau".equals(nom)) {
                final MathOperator mo = new MathOperator();
                mo.setStretchy(true);
                mo.addText("^");
                return mo;
            } else if ("bar".equals(nom) || "barre".equals(nom)) {
                final MathOperator mo = new MathOperator();
                mo.setStretchy(true);
                mo.addText("\u00AF");
                return mo;
            } else {
                String s = nom;
                for (final String[] element : symbols)
                    if (element[0].equals(s)) {
                        s = element[1];
                        break;
                    }
                if (s.indexOf(',') != -1 || s.indexOf('.') != -1)
                    s = "?"; // les ',' et les '.' sont interdits dans les noms de variables
                MathElement me;
                if (unites)
                    me = new MathText();  // les unités ne doivent pas être en italique
                else
                    me = new MathIdentifier();
                me.addText(s);
                return me;
            }
        }
    }
}
