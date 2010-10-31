/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

/**
 * Fenêtre de source XML
 */
public class SourceFrame extends JFrame implements ActionListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(SourceFrame.class);

    private JaxeDocument jdoc;
    private final JaxeFrame jframe;
    private StyledDocument srcdoc;
    JTextPane textPane;
    private Style styleElement;
    private Style styleNomAttribut;
    private Style styleValeurAttribut;
    private Style styleTexte;
    private Style styleEntite;
    private Style styleCommentaire;
    private DialogueRechercher dlgRecherche = null;
    static String texteRecherche = null;

    public SourceFrame(final JaxeDocument jdoc, final JaxeFrame jframe) {
        this.jframe = jframe;
        newdoc(jdoc);
    }
    
    public void newdoc(final JaxeDocument jdoc) {
        this.jdoc = jdoc;
        final JaxeMenuBar menuBar = new JaxeMenuBar(this, jframe);
        setJMenuBar(menuBar);
        final Rectangle fr = jframe.getBounds();
        setLocation(fr.x + fr.width/3 + 50, fr.y + fr.height/3 + 50);
        final Dimension ecran = getToolkit().getScreenSize();
        int largeur = ecran.width / 2;
        if (largeur < 620)
            largeur = ecran.width - 20;
        int hauteur = (ecran.height * 2) / 3;
        if (hauteur < 460)
            hauteur = ecran.height - 50;
        setSize(new Dimension(largeur, hauteur));
        affichage();
        miseAJour();
    }
    
    protected void affichage() {
        textPane = new JTextPane(); // disabled horizontal scrolling doesn't work with JEditorPane
        textPane.setEditable(false);
        
        //editorPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        // setFont doesn't work for JTextPane
        
        final JScrollPane paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        final JPanel boutonsP = new JPanel();
        boutonsP.setLayout(new FlowLayout());
        final JButton boutonMAJ = new JButton(JaxeResourceBundle.getRB().getString("source.MiseAJour"));
        boutonMAJ.addActionListener(this);
        boutonsP.add(boutonMAJ);

        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(paneScrollPane, BorderLayout.CENTER);
        contentPane.add(boutonsP, BorderLayout.NORTH);
        setContentPane(contentPane);
        
        addWindowListener( new WindowAdapter() {
            @Override
            public void windowActivated(final WindowEvent e) {
                textPane.requestFocus(); // gnaaaargh ah ah ! je l'ai eu mon focus !  8¬>
            }
        });
    }
    
    public void miseAJour() {
        if (dlgRecherche != null) {
            if (dlgRecherche.isVisible())
                dlgRecherche.setVisible(false);
            dlgRecherche = null;
        }
        try {
            final BufferedReader in = new BufferedReader(jdoc.getReader());
            textPane.read(in, null);
        } catch (final IOException ex) {
            LOG.error("miseAJour()", ex);
            return;
        }
        
        srcdoc = (StyledDocument)textPane.getDocument();
        
        setTabs(4);
        // Monaco font looks much better than the default Courier on MacOS X with Java 1.4.1
        final String[] fontnames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        boolean trouv = false;
        for (final String element : fontnames)
            if ("Monaco".equals(element)) {
                trouv = true;
                break;
            }
        if (trouv) {
            final Style defaultStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setFontFamily(defaultStyle, "Monaco");
            StyleConstants.setFontSize(defaultStyle, 12);
        }
        
        final Style defaultStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
        styleElement = textPane.addStyle(null, defaultStyle);
        StyleConstants.setForeground(styleElement, new Color(150, 0, 0)); // rouge foncé
        styleNomAttribut = textPane.addStyle(null, defaultStyle);
        StyleConstants.setForeground(styleNomAttribut, new Color(0, 0, 150)); // bleu foncé
        styleValeurAttribut = textPane.addStyle(null, defaultStyle);
        StyleConstants.setForeground(styleValeurAttribut, new Color(0, 100, 0)); // vert foncé
        styleEntite = textPane.addStyle(null, defaultStyle);
        StyleConstants.setForeground(styleEntite, new Color(0, 100, 100)); // cyan foncé
        styleCommentaire = textPane.addStyle(null, defaultStyle);
        StyleConstants.setForeground(styleCommentaire, Color.gray); // gris
        styleTexte = textPane.addStyle(null, defaultStyle);
        StyleConstants.setForeground(styleTexte, Color.black); // noir
        
        toutColorier();
        
        if (jdoc.fsave != null)
            setTitle(jdoc.fsave.getName());
        else
            setTitle(JaxeResourceBundle.getRB().getString("menus.Source"));

        setVisible(true);
    }
    
    /**
     * Buffer de caractères du document
     */
    class Buffer {
        int tailleMax = 200;
        String sbuff;
        int debut, fin; // fin n'inclut pas le dernier caractère du buffer
        
        public Buffer() {
            lire(0);
        }
        
        public void lire(final int ind) {
            int lg = tailleMax;
            if (ind+lg > srcdoc.getLength())
                lg = srcdoc.getLength() - ind;
            try {
                sbuff = srcdoc.getText(ind, lg);
            } catch (final BadLocationException ex) {
                LOG.error("Buffer.lire: BadLocationException", ex);
            }
            if (sbuff.length() != lg)
                LOG.error("Buffer.lire: erreur: " + sbuff.length() + " != " + lg);
            debut = ind;
            fin = ind + lg;
        }
        
        public char getChar(final int p) {
            if (p >= srcdoc.getLength())
                return(' ');
            if (p >= fin)
                lire(p);
            else if (p < debut) {
                int p2 = p - tailleMax + 1;
                if (p2 < 0)
                    p2 = 0;
                lire(p2);
            }
            return(sbuff.charAt(p-debut));
        }
        
        public boolean subEquals(final String s, final int ind) {
            if (ind >= srcdoc.getLength())
                LOG.error("erreur dans Buffer.subEquals: ind >= srcdoc.getLength() : " + ind + " >= " + srcdoc.getLength());
            final int lg = s.length();
            if (ind + lg >= srcdoc.getLength())
                return(false);
            if (lg > tailleMax)
                LOG.error("erreur dans Buffer.subEquals: " + lg + " > taille maxi (" + tailleMax + ")");
            if (ind < debut || ind+lg > fin)
                lire(ind);
            for (int i=0, j=ind-debut; i<lg; i++,j++)
                if (s.charAt(i) != sbuff.charAt(j))
                    return(false);
            return(true);
        }
    }
    
    /**
     * Met à jour les couleurs dans l'intervalle indiqué
     */
    public void colorier(final int debut, final int fin) {
        final Buffer buff = new Buffer();
        if (buff.subEquals("\n", debut)) {
            if (fin - debut > 1)
                srcdoc.setCharacterAttributes(debut+1, fin-debut-1, styleTexte, false);
        } else
            srcdoc.setCharacterAttributes(debut, fin-debut, styleTexte, false);
        
        boolean dansNomElement = false;
        boolean dansNomAttribut = false;
        boolean avantValeurAttribut = false;
        char carValeurAttribut = '"';
        boolean dansValeurAttribut = false;
        boolean dansEntite = false;
        boolean dansCommentaire = false;
        int debutzone = debut;
        for (int ic=debut; ic<fin; ic++) {
            if (dansCommentaire) {
                if (buff.subEquals("-->", ic)) {
                    dansCommentaire = false;
                    ic += 2;
                    srcdoc.setCharacterAttributes(debutzone, ic-debutzone+1, styleCommentaire, false);
                }
            } else if (dansNomElement) {
                final char c = buff.getChar(ic);
                if (c == ' ' || c == '\n') {
                    dansNomElement = false;
                    srcdoc.setCharacterAttributes(debutzone, ic-debutzone, styleElement, false);
                    dansNomAttribut = true;
                    debutzone = ic+1;
                } else if (c == '>' || ic == fin-1) {
                    dansNomElement = false;
                    srcdoc.setCharacterAttributes(debutzone, ic-debutzone+1, styleElement, false);
                }
            } else if (dansNomAttribut) {
                final char c = buff.getChar(ic);
                if (c == '>' || ic == fin-1) {
                    dansNomAttribut = false;
                    srcdoc.setCharacterAttributes(debutzone, ic-debutzone+1, styleElement, false);
                } else if (c == '=') {
                    dansNomAttribut = false;
                    srcdoc.setCharacterAttributes(debutzone, ic-debutzone, styleNomAttribut, false);
                    avantValeurAttribut=true;
                }
            } else if (avantValeurAttribut) {
                final char c = buff.getChar(ic);
                if (c == '"' || c=='\'') {
                    avantValeurAttribut = false;
                    dansValeurAttribut = true;
                    carValeurAttribut = c;
                    debutzone = ic;
                }
            } else if (dansValeurAttribut) {
                final char c = buff.getChar(ic);
                if (c == carValeurAttribut || ic == fin-1) {
                    dansValeurAttribut = false;
                    srcdoc.setCharacterAttributes(debutzone, ic-debutzone+1, styleValeurAttribut, false);
                    dansNomAttribut = true;
                    debutzone = ic+1;
                }
            } else if (dansEntite) {
                final char c = buff.getChar(ic);
                if (c == ';' || c == ' ' || c == '\n' || ic == fin-1) {
                    dansEntite = false;
                    srcdoc.setCharacterAttributes(debutzone, ic-debutzone+1, styleEntite, false);
                }
            } else {
                final char c = buff.getChar(ic);
                if (c == '<') {
                    if (buff.subEquals("<!--", ic))
                        dansCommentaire = true;
                    else
                        dansNomElement = true;
                    debutzone = ic;
                } else if (c == '>') {
                    srcdoc.setCharacterAttributes(ic, 1, styleElement, false);
                } else if (c == '&' || c == '%') {
                    dansEntite = true;
                    debutzone = ic;
                }
                
            }
        }
        
        // si on est toujours dans un commentaire à la fin de la zone, on continue à colorier au-delà
        if (dansCommentaire) {
            for (int ic=fin; ic<srcdoc.getLength() && dansCommentaire; ic++) {
                if (buff.subEquals("-->", ic)) {
                    dansCommentaire = false;
                    ic += 2;
                    srcdoc.setCharacterAttributes(debutzone, ic-debutzone+1, styleCommentaire, false);
                }
            }
            if (dansCommentaire)
                srcdoc.setCharacterAttributes(debutzone, srcdoc.getLength()-debutzone, styleCommentaire, false);
        }
    }
    
    /**
     * Met à jour les couleurs dans tout le document
     */
    public void toutColorier() {
        colorier(0, srcdoc.getLength());
    }
    
    public void actionPerformed(final ActionEvent e) {
        miseAJour();
    }
    
    /**
     * Positionne le document à la ligne indiquée (la première ligne a le numéro 1)
     */
    public void allerLigne(int ligne) {
        if (ligne > 0)
            ligne--;
        else
            ligne = 0;
        final int pos = srcdoc.getDefaultRootElement().getElement(ligne).getStartOffset();
        // bidouille pour afficher la position en haut de la fenêtre
        try {
            textPane.scrollRectToVisible(textPane.modelToView(srcdoc.getLength()));
            textPane.scrollRectToVisible(textPane.modelToView(pos));
        } catch (final BadLocationException ex) {
        }
    }
    
    /**
     * Sélectionne la ligne indiquée (la première ligne a le numéro 1)
     */
    public void selectLigne(int ligne) {
        if (ligne > 0)
            ligne--;
        else
            ligne = 0;
        final Element ligneel = srcdoc.getDefaultRootElement().getElement(ligne);
        try {
            textPane.scrollRectToVisible(textPane.modelToView(ligneel.getStartOffset()));
        } catch (final BadLocationException ex) {
        }
        textPane.setCaretPosition(ligneel.getStartOffset());
        if (ligneel.getEndOffset() <= srcdoc.getLength())
            textPane.moveCaretPosition(ligneel.getEndOffset());
        else
            textPane.moveCaretPosition(srcdoc.getLength());
    }
    
    /**
     * Spécifie la taille des tabulations, en équivalent-caractères (on utilise la taille du 'w' comme référence)
     */
    public void setTabs(final int charactersPerTab) {
        final FontMetrics fm = textPane.getFontMetrics(textPane.getFont());
        final int charWidth = fm.charWidth('w');
        final int tabWidth = charWidth * charactersPerTab;
        
        final TabStop[] tabs = new TabStop[10];
        
        for (int j = 0; j < tabs.length; j++) {
            final int tab = j + 1;
            tabs[j] = new TabStop( tab * tabWidth );
        }
        
        final TabSet tabSet = new TabSet(tabs);
        final SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setTabSet(attributes, tabSet);
        final int length = srcdoc.getLength();
        srcdoc.setParagraphAttributes(0, length, attributes, false);
    }
    
    public void imprimer() {
        final DocumentRenderer renderer = new DocumentRenderer();
        renderer.print(textPane);
    }
    
    public void rechercher() {
        if (dlgRecherche == null)
            dlgRecherche = new DialogueRechercher(jframe.doc, textPane);
        dlgRecherche.setVisible(true);
    }
    
    public void suivant() {
        if (dlgRecherche != null) {
            texteRecherche = dlgRecherche.getTexteRecherche();
            dlgRecherche.suivant(textPane.getSelectionStart());
        }
    }
}
