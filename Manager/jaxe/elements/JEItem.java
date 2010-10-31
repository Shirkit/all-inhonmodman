/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.Position;

import jaxe.DialogueAttributs;
import jaxe.ImageKeeper;
import jaxe.JaxeDocument;
import jaxe.JaxeElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Elément d'une liste, affiché avec des puces ou des numéros. Ce type d'élément de liste,
 * inséré dans le texte, peut avoir n'importe quelle longueur, à la différence des éléments d'un JEListe.
 * Type d'élément Jaxe: 'item'
 */
public class JEItem extends JaxeElement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEItem.class);

    static String newline = "\n";
    String fichierPastille1 = "images/pastille1.gif";
    String fichierPastille2 = "images/pastille2.gif";
    boolean selectionne = false;
    Image imagePastille1 = null;
    Image imagePastille2 = null;
    Image imagePastille1sel = null;
    Image imagePastille2sel = null;
    ImageIcon iconePastille1 = null;
    ImageIcon iconePastille2 = null;
    int typeListe = 0;
    JLabel label = null;
    boolean mettreajour = false;

    public JEItem(final JaxeDocument doc) {
        this.doc = doc;
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
        Position newpos;
        
        JaxeElement jeparent = null;
        if (getParent() != null)
            jeparent = doc.getElementForNode(getParent().noeud);
        if (jeparent instanceof JEListe)
            typeListe = ((JEListe)jeparent).typeListe;
        if (typeListe == JEListe.NUMEROS) {
            final int lp = posDansListe();
            label = new JLabel(lp+".");
            label.setOpaque(true);
            label.setBackground(Color.white);
        } else {
            if (refElement != null)
                fichierPastille1 = doc.cfg.valeurParametreElement(refElement, "image1", fichierPastille1); 
            
            iconePastille1 = new ImageIcon(ImageKeeper.loadImage(fichierPastille1));
            label = new JLabel(iconePastille1);
        }
        label.addMouseListener(new MyMouseListener(this, doc.jframe));
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setOpaque(false);
        panel.add(label);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        final Dimension minimum = label.getMinimumSize();
        minimum.setSize(minimum.width + 4, minimum.height);
        panel.setPreferredSize(minimum);
        panel.setMaximumSize(minimum);
        if (typeListe == JEListe.NUMEROS)
            panel.setAlignmentY((float)0.9);
        else
            panel.setAlignmentY(1);
        newpos = insertComponent(pos, panel);
        
        creerEnfants(newpos);
        
        //doc.insertString(newpos.getOffset(), newline, null);
        if (refElement != null)
            fichierPastille2 = doc.cfg.valeurParametreElement(refElement, "image2", fichierPastille2); 
        
        iconePastille2 = new ImageIcon(ImageKeeper.loadImage(fichierPastille2));
        //newpos = insertIcon(newpos, iconePastille2);
        final JLabel label2 = new JLabel(iconePastille2);
        final JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel2.setOpaque(false);
        panel2.add(label2);
        panel2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        final Dimension minimum2 = label2.getMinimumSize();
        minimum2.setSize(minimum2.width + 4, minimum2.height);
        panel2.setPreferredSize(minimum2);
        panel2.setMaximumSize(minimum2);
        panel2.setAlignmentY(1);
        newpos = insertComponent(newpos, panel2);
        
        if (mettreajour) {
            majListe(false);
            mettreajour = false;
        }
    }
    
    public int posDansListe() {
        final Element parel = (Element)getParent().noeud;
        final NodeList lchildren = parel.getChildNodes();
        final String itemTag = noeud.getNodeName();
        int p = 1;
        for (int i=0; i<lchildren.getLength(); i++) {
            if (itemTag.equals(lchildren.item(i).getNodeName())) {
                if (lchildren.item(i) == noeud)
                    return p;
                p++;
            }
        }
        LOG.error("posDansListe() - Erreur: Impossible de retrouver le numéro dans la liste");
        return 0;
    }
    
    public void majNombre(int p) {
        if (p == 0)
            p = posDansListe();
        label.setText(p + ".");
    }
    
    public void majListe(final boolean pourEffacer) {
        if (typeListe == JEListe.NUMEROS) {
            final Element parel = (Element)getParent().noeud;
            final String itemTag = noeud.getNodeName();
            int p = 1;
            for (Node n=parel.getFirstChild(); n != null; n=n.getNextSibling()) {
                if (itemTag.equals(n.getNodeName()) && (!pourEffacer || n != noeud)) {
                    final JEItem je = (JEItem)doc.getElementForNode(n);
                    if (je != null)
                        je.majNombre(p);
                    p++;
                }
            }
        }
    }
    
    @Override
    public void effacer() {
        super.effacer();
        majListe(true);
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null) return null;

        mettreajour = true;
        return newel;
    }

    @Override
    public boolean avecSautsDeLigne() {
        return (true);
    }
    
    @Override
    public Position insPosition() {
        return fin;
    }
    
    @Override
    public void selection(final boolean select) {
        if (!selectionne && select) {
            if (iconePastille1 != null && imagePastille1 == null)
                imagePastille1 = iconePastille1.getImage();
            if (imagePastille2 == null)
                imagePastille2 = iconePastille2.getImage();
            if (imagePastille1sel == null)
                creerImagesSel();
            if (iconePastille1 != null)
                iconePastille1.setImage(imagePastille1sel);
            else
                label.setBackground(Color.lightGray);
            iconePastille2.setImage(imagePastille2sel);
        }
        if (selectionne && !select) {
            if (iconePastille1 != null)
                iconePastille1.setImage(imagePastille1);
            else
                label.setBackground(Color.white);
            iconePastille2.setImage(imagePastille2);
        }
        selectionne = select;
        doc.textPane.repaint();
        super.selection(select);
    }
    
    protected void creerImagesSel() {
        final ImageFilter filtre = new FiltreGris();
        if (imagePastille1 != null) {
            final ImageProducer producteur1 = new FilteredImageSource(imagePastille1.getSource(), filtre);
            imagePastille1sel = Toolkit.getDefaultToolkit().createImage(producteur1);
        }
        final ImageProducer producteur2 = new FilteredImageSource(imagePastille2.getSource(), filtre);
        imagePastille2sel = Toolkit.getDefaultToolkit().createImage(producteur2);
    }
    
    class FiltreGris extends RGBImageFilter {
        private static final int GRIS = 0xFFAFAFAF;
        public FiltreGris() {
            canFilterIndexColorModel = true;
        }
        @Override
        public int filterRGB(final int x, final int y, final int rgb) {
            return rgb & GRIS;
        }
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
        final Element el = (Element)noeud;

        final ArrayList<Element> latt = doc.cfg.listeAttributs(refElement);
        if (latt != null && latt.size() > 0) {
            final DialogueAttributs dlg = new DialogueAttributs(doc.jframe, doc,
                "item: " + el.getTagName(), refElement, el);
            if (dlg.afficher()) 
                dlg.enregistrerReponses();
            dlg.dispose();
        }
    }
    
    class MyMouseListener extends MouseAdapter {
        JEItem jei;
        JFrame jframe;
        public MyMouseListener(final JEItem obj, final JFrame jframe) {
            super();
            jei = obj;
            this.jframe = jframe;
        }
        @Override
        public void mouseClicked(final MouseEvent e) {
            jei.afficherDialogue(jframe);
        }
    }
}
