/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.TransferHandler;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Composant Swing représentant le début ou la fin d'un élément dans le texte
 */
public class Balise extends JComponent implements MouseListener, MouseMotionListener {
    
    private static final Logger LOG = Logger.getLogger(Balise.class);
    
    // balises de début ou de fin ?
    public final static int DEBUT = 0;
    public final static int FIN = 1;
    public final static int VIDE = 2;
    final static int fleche = 7; // taille de la flêche
    
    static protected ImageIcon iconeAttributs = new ImageIcon(ImageKeeper.loadImage("images/attributs.gif", true));
    static protected ImageIcon iconeValide = new ImageIcon(ImageKeeper.loadImage("images/valide.gif", true));
    static protected ImageIcon iconeInvalide = new ImageIcon(ImageKeeper.loadImage("images/invalide.gif", true));
    
    static Font police = (Font) UIManager.getDefaults().get("Button.font");
    final static Color jauneLeger = new Color(250, 250, 190);
    final static Color rougeFonce = new Color(160, 20, 20);
    final static Color orange = new Color(255, 220, 170);
    final static Color bleuClair = new Color(225, 245, 255);
    final static Color violet = new Color(220, 210, 255);
    final static Color vertClair = new Color(210, 250, 220);
    final static Color vertJaune = new Color(230, 250, 180);
    final static Color vertFonce = new Color(20, 80, 20);
    final static Color bleuFonce = new Color(20, 20, 160);
    
    static Color[][] couleurs = { { jauneLeger, rougeFonce, orange },
            { bleuClair, rougeFonce, violet }, { vertClair, rougeFonce, vertJaune } };
    
    static Border bordBalise = null;
    
    final static BaliseTransferHandler transferHandler = new BaliseTransferHandler();
    
    String texte;
    boolean valide = true;
    boolean selectionne = false;
    int noens = 0;
    boolean division;
    boolean attributs;
    int typeBalise;
    boolean evtpopup = false; // un menu popup a été affiché en réponse aux évènements de souris
    boolean select1; // élément sélectionné lors du clic
    boolean clicbtnattr = false; // clic sur le bouton d'attributs
    boolean attributsVisibles;
    JaxeElement je;
    
    
    /**
     * Constructeur pour un composant Balise qui affiche le titre de l'élément XML sur la balise.
     * @param je Elément Jaxe de la balise
     * @param division précise si la balise doit prendre tout l'espace restant sur la ligne de texte
     * @param typeBalise type de balise (Balise.DEBUT | Balise.FIN | Balise.VIDE)
     */
    public Balise(final JaxeElement je, final boolean division, final int typeBalise) {
        this.je = je;
        this.division = division;
        this.typeBalise = typeBalise;
        if (je.refElement == null)
            this.attributsVisibles = false;
        else
            this.attributsVisibles = "true".equals(je.doc.cfg.valeurParametreElement(je.refElement, "attributsVisibles", "false"));
        this.texte = calculerTitre();
        init();
    }
    
    /**
     * Constructeur pour un composant Balise qui affiche le texte donné en paramètre sur la balise.
     * @param je Elément Jaxe de la balise
     * @param texte texte à afficher sur la balise
     * @param division précise si la balise doit prendre tout l'espace restant sur la ligne de texte
     * @param typeBalise type de balise (Balise.DEBUT | Balise.FIN | Balise.VIDE)
     */
    public Balise(final JaxeElement je, final String texte, final boolean division, final int typeBalise) {
        this.je = je;
        this.texte = texte;
        this.division = division;
        this.typeBalise = typeBalise;
        this.attributsVisibles = false;
        init();
    }
    
    /**
     * Défini les ensembles de couleurs à utiliser pour les balises. Par défaut,
     * tableau de tableaux à 3 entrées: 1) normal 2) sélection 3) invalide.
     * Par exemple
     * {{jauneLeger, rougeFonce, orange}, {bleuClair, rougeFonce, violet}}
     */
    public static void setCouleurs(final Color[][] couleurs) {
        Balise.couleurs = couleurs;
    }
    
    /**
     * Renvoie les couleurs actuelles des balises
     * 
     * @return Couleurs des balises
     */
    public static Color[][] getCouleurs() {
        return couleurs;
    }
    
    /**
     * Définit le bord des balises
     * @deprecated
     * 
     * @param border le bord des balises
     */
    @Deprecated
    public static void setBord(final Border border) {
        bordBalise = border;
    }
    
    /**
     * Renvoie le bord actuel des balises
     * 
     * @return le bord des balises
     */
    public static Border getBord() {
        return bordBalise;
    }
    
    /**
     * Renvoie la police de caractères utilisée pour les balises
     * 
     * @return la police actuelle des balises
     */
    public static Font getPolice() {
        return police;
    }
    
    /**
     * Définit la police de caractères à utiliser pour les balises
     * 
     * @param font Police à utiliser
     */
    public static void setPolice(final Font font) {
        police = font;
    }
    
    private String calculerTitre() {
        String texteElement;
        if (je.refElement != null)
            texteElement = je.doc.cfg.titreElement(je.refElement);
        else if (je.noeud != null)
            texteElement = je.noeud.getNodeName();
        else if (je.refElement != null)
            texteElement = je.doc.cfg.nomElement(je.refElement);
        else
            texteElement = null;
        String titre = texteElement;
        if (!attributsVisibles && je.refElement != null) {
            final ArrayList<String> attributsTitre = je.doc.cfg.getParametresElement(je.refElement).get("titreAtt");
            if (attributsTitre != null) {
                for (final String attr : attributsTitre) {
                    final String attribute = ((Element)je.noeud).getAttribute(attr);
                    if (!"".equals(attribute)) {
                        titre += " '" + attribute + "'";
                        break;
                    }
                }
            }
        }
        return(titre);
    }
    
    private void init() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.TRAILING, typeBalise == DEBUT ? (fleche+1) : 0, 0)); // pour JEListe qui ajoute un composant
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        attributs = je.aDesAttributs();
        addMouseListener(this);
        addMouseMotionListener(this);
        setAlignmentY((float) 0.7);
        if (bordBalise != null)
            setBorder(bordBalise);
        else
            setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 2));
        setTransferHandler(transferHandler);
    }
    
    private void forwardEvent(final MouseEvent e) {
        final JaxeTextPane tp = je.doc.textPane;
        e.setSource(tp);
        final Point jeloc = getLocationOnScreen();
        final Point tploc = tp.getLocationOnScreen();
        e.translatePoint(jeloc.x - tploc.x, jeloc.y - tploc.y);
        if (e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED || e.getID() == MouseEvent.MOUSE_CLICKED) {
            final MouseListener[] mls = (MouseListener[])(tp.getListeners(MouseListener.class));
            for (MouseListener ml : mls) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED)
                    ml.mousePressed(e);
                else if (e.getID() == MouseEvent.MOUSE_RELEASED)
                    ml.mouseReleased(e);
                else if (e.getID() == MouseEvent.MOUSE_CLICKED)
                    ml.mouseClicked(e);
            }
        } else if (e.getID() == MouseEvent.MOUSE_DRAGGED || e.getID() == MouseEvent.MOUSE_MOVED) {
            final MouseMotionListener[] mls = (MouseMotionListener[])(tp.getListeners(MouseMotionListener.class));
            for (MouseMotionListener ml : mls)
                if (e.getID() == MouseEvent.MOUSE_DRAGGED)
                    ml.mouseDragged(e);
                else if (e.getID() == MouseEvent.MOUSE_MOVED)
                    ml.mouseMoved(e);
        }
    }
    
    public void mouseClicked(final MouseEvent e) {
        if (evtpopup)
            return;
        if (e.getClickCount() == 2) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    je.doc.textPane.requestFocus();
                    je.doc.textPane.selectElement(je.noeud);
                }
            });
        } else if (select1) {
            int cpos;
            if (typeBalise == FIN)
                cpos = je.fin.getOffset();
            else
                cpos = je.debut.getOffset();
            final JaxeTextPane tp = je.doc.textPane;
            if (e.getX() < getWidth() / 2)
                tp.setCaretPosition(cpos);
            else
                tp.setCaretPosition(cpos + 1);
            tp.requestFocus();
        } else if (!clicbtnattr)
            forwardEvent(e);
    }
    
    public void mousePressed(final MouseEvent e) {
        if (e.isPopupTrigger())
            popup(e);
        else {
            evtpopup = false;
            select1 = selectionne;
            clicbtnattr = clicSurBoutonAttributs(e);
            if (clicbtnattr)
                repaint();
            if (!select1 && !clicbtnattr)
                forwardEvent(e);
        }
    }
    
    private boolean clicSurBoutonAttributs(final MouseEvent e) {
        final Insets bords = getInsets();
        int xg;
        if (typeBalise == FIN)
            xg = bords.left + fleche + 1;
        else
            xg = bords.left + 1;
        return(attributs && e.getX() > xg && e.getX() < xg + iconeAttributs.getIconWidth() + 4 &&
            e.getY() > bords.top && e.getY() < 21 - bords.bottom &&
            je.doc.textPane.isEditable() && je.getEditionAutorisee());
    }
    
    public void mouseReleased(final MouseEvent e) {
        if (e.isPopupTrigger())
            popup(e);
        else if (!evtpopup && clicbtnattr && clicSurBoutonAttributs(e))
            je.afficherDialogue(je.doc.textPane.jframe);
        else if (!select1 && !clicbtnattr && !evtpopup)
            forwardEvent(e);
        if (clicbtnattr) {
            clicbtnattr = false;
            repaint();
        } else
            clicbtnattr = false;
    }
    
    private void popup(final MouseEvent e) {
        evtpopup = true;
        final Point pt = e.getPoint();
        final Point jeloc = getLocationOnScreen();
        final JaxeTextPane tp = je.doc.textPane;
        final Point tploc = tp.getLocationOnScreen();
        pt.translate(jeloc.x - tploc.x, jeloc.y - tploc.y);
        int pos;
        final Insets bords = getInsets();
        if (e.getX() < bords.left || e.getX() > getWidth() - bords.right)
            pos = tp.viewToModel(pt);
        else if (typeBalise == FIN)
            pos = je.fin.getOffset();
        else
            pos = je.debut.getOffset() + 1;
        tp.menuContextuel(pos, pt);
    }
    
    public void mouseEntered(final MouseEvent e) {
    }
    
    public void mouseExited(final MouseEvent e) {
    }
    
    public void mouseDragged(final MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e))
            return;
        if (select1) {
            clicbtnattr = false;
            // le drag & drop ne marche pas sur la partie droite de la balise de fin,
            // (qui est traitée comme une lettre) donc on est obligé de le reprogrammer
            final int ctrlMask = System.getProperty("os.name").startsWith("Mac OS") ?
                InputEvent.META_MASK : InputEvent.CTRL_DOWN_MASK;
            final int action = ((e.getModifiersEx() & ctrlMask) == ctrlMask) ?
                TransferHandler.COPY : TransferHandler.MOVE;
            getTransferHandler().exportAsDrag(this, e, action);
        } else if (!clicbtnattr)
            forwardEvent(e);
    }
    
    public void mouseMoved(final MouseEvent e) {
        if (!clicbtnattr)
            forwardEvent(e);
    }
    
    public void setText(final String texte) {
        this.texte = texte;
        repaint();
    }
    
    public void setValidite(final boolean valide) {
        this.valide = valide;
    }

    @Override
    public Color getBackground() {
        if (selectionne)
            return (couleurs[noens][1]);
        else if (valide)
            return (couleurs[noens][0]);
        else
            return (couleurs[noens][2]);
    }

    @Override
    public Color getForeground() {
        if (selectionne)
            return (couleurs[noens][0]);
        return (couleurs[noens][1]);
    }

    @Override
    public Dimension getPreferredSize() {
        final Insets bords = getInsets();
        final Dimension d = new Dimension(8 + bords.left + bords. right, 21);
        if (attributs)
            d.width += 20;
        if (typeBalise != VIDE)
            d.width += fleche;
        final FontMetrics fm = getFontMetrics(police);
        d.width += fm.stringWidth(texte);
        if (attributsVisibles && typeBalise != FIN && je.refElement != null) {
            final NamedNodeMap listeAttributs = je.noeud.getAttributes();
            final Config cfg = je.doc.cfg;
            final ArrayList<Element> latt = cfg.listeAttributs(je.refElement);
            for (Element refatt : latt) {
                final Node natt = listeAttributs.getNamedItemNS(cfg.espaceAttribut(refatt),
                    cfg.nomAttribut(refatt));
                if (natt != null) {
                    String titreAtt = natt.getNodeName();
                    String titreVal = natt.getNodeValue();
                    titreAtt = cfg.titreAttribut(je.refElement, refatt);
                    titreVal = cfg.titreValeurAttribut(je.refElement, refatt, titreVal);
                    d.width += fm.stringWidth(" " + titreAtt + "=" + titreVal);
                }
            }
        }
        if (je.doc.textPane.iconeValide)
            d.width += 20;
        return (d);
    }

    @Override
    public Dimension getMaximumSize() {
        if (division)
            return (super.getMaximumSize());
        return (getPreferredSize());
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d;
        if (division)
            d = super.getMinimumSize();
        else
            d = getPreferredSize();
        if (d.width > 150)
            d.width = 150;
        return(d);
    }
    
    /**
     * Sélectionne la balise en changeant ses couleurs.
     */
    public void selection(final boolean select) {
        selectionne = select;
        setForeground(getForeground());
        repaint();
    }
    
    /**
     * Donne le numéro de l'ensemble de couleurs à utiliser pour cette balise.
     */
    public void setEnsembleCouleurs(final int noens) {
        this.noens = noens - (noens / couleurs.length) * couleurs.length;
        setForeground(getForeground());
    }
    
    public void majAffichage() {
        texte = calculerTitre();
        invalidate();
    }
    
    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2d = (Graphics2D)g;
        final Object antialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final Insets bords = getInsets();
        final Dimension dim = getSize();
        final Polygon poly = new Polygon();
        if (typeBalise == DEBUT) {
            poly.addPoint(bords.left, bords.top);
            poly.addPoint(dim.width - 1 - bords.right - fleche, bords.top);
            poly.addPoint(dim.width - 1 - bords.right, dim.height / 2);
            poly.addPoint(dim.width - 1 - bords.right - fleche, dim.height - 1 - bords.bottom);
            poly.addPoint(bords.left, dim.height - 1 - bords.bottom);
        } else if (typeBalise == FIN) {
            poly.addPoint(bords.left + fleche, bords.top);
            poly.addPoint(dim.width - 1 - bords.right, bords.top);
            poly.addPoint(dim.width - 1 - bords.right, dim.height - 1 - bords.bottom);
            poly.addPoint(bords.left + fleche, dim.height - 1 - bords.bottom);
            poly.addPoint(bords.left, dim.height / 2);
        } else {
            poly.addPoint(bords.left, bords.top);
            poly.addPoint(dim.width - 1 - bords.right, bords.top);
            poly.addPoint(dim.width - 1 - bords.right, dim.height - 1 - bords.bottom);
            poly.addPoint(bords.left, dim.height - 1 - bords.bottom);
        }
        g.setColor(getBackground());
        g.fillPolygon(poly);
        g.setColor(Color.lightGray);
        g.drawPolygon(poly);
        
        // ombre légère
        g.setColor(new Color(100, 100, 100));
        if (typeBalise == DEBUT) {
            g.drawLine(dim.width - 1 - bords.right, dim.height / 2, dim.width - 1 - bords.right - fleche, dim.height - 1 - bords.bottom);
            g.drawLine(dim.width - 1 - bords.right - fleche, dim.height - 1 - bords.bottom, bords.left, dim.height - 1 - bords.bottom);
        } else if (typeBalise == FIN) {
            g.drawLine(dim.width - 1 - bords.right, bords.top, dim.width - 1 - bords.right, dim.height - 1 - bords.bottom);
            g.drawLine(dim.width - 1 - bords.right, dim.height - 1 - bords.bottom, bords.left + fleche, dim.height - 1 - bords.bottom);
            g.drawLine(bords.left + fleche, dim.height - 1 - bords.bottom, bords.left, dim.height / 2);
        } else {
            g.drawLine(dim.width - 1 - bords.right, bords.top, dim.width - 1 - bords.right, dim.height - 1 - bords.bottom);
            g.drawLine(dim.width - 1 - bords.right, dim.height - 1 - bords.bottom, bords.left, dim.height - 1 - bords.bottom);
        }
        
        int px = bords.left + 1;
        if (typeBalise == FIN)
            px += fleche + 1;
        
        // icône attributs
        if (attributs) {
            if (clicbtnattr)
                g.setColor(Color.gray);
            else
                g.setColor(Color.lightGray);
            final int bx1 = px;
            final int by1 = bords.top + 1;
            final int bx2 = px + iconeAttributs.getIconWidth() + 1;
            final int by2 = bords.top + 1 + iconeAttributs.getIconHeight() + 1;
            g.drawLine(bx1, by1, bx2, by1);
            g.drawLine(bx1, by1, bx1, by2);
            if (clicbtnattr)
                g.setColor(Color.lightGray);
            else
                g.setColor(Color.gray);
            g.drawLine(bx2, by2, bx1, by2);
            g.drawLine(bx2, by2, bx2, by1);
            iconeAttributs.paintIcon(this, g, px + 1, bords.top + 2);
            px += iconeAttributs.getIconWidth() + 4;
        }
        
        // texte
        px += 2;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);
        g.setColor(getForeground());
        final Font tmpFont = g.getFont();
        g.setFont(police);
        final FontMetrics fm = getFontMetrics(police);
        
        final int largeurTexte = fm.stringWidth(texte);
        int largeurSansTexte = 8 + bords.left + bords. right;
        if (attributs)
            largeurSansTexte += 20;
        if (typeBalise != VIDE)
            largeurSansTexte += fleche;
        if (je.doc.textPane.iconeValide)
            largeurSansTexte += 20;
        int largeurLibre = dim.width - largeurSansTexte;
        String texteAffiche;
        if (largeurLibre < largeurTexte) {
            final int nbc = (int)Math.round(texte.length() * (((double)largeurLibre)/largeurTexte)) - 3;
            if (nbc > 0)
                texteAffiche = texte.substring(0, nbc) + "...";
            else
                texteAffiche = "...";
        } else
            texteAffiche = texte;
        
        g.drawString(texteAffiche, px, dim.height - fm.getDescent() - 2 - bords.bottom);
        px += fm.stringWidth(texteAffiche);
        
        // attributs
        if (attributsVisibles && typeBalise != FIN && je.refElement != null) {
            int pxmax = dim.width - bords.right - 1;
            if (je.doc.textPane.iconeValide)
                pxmax -= 20;
            if (typeBalise == DEBUT)
                pxmax -= fleche;
            final int py = dim.height - fm.getDescent() - 2 - bords.bottom;
            final NamedNodeMap listeAttributs = je.noeud.getAttributes();
            final ArrayList<Element> latt = je.doc.cfg.listeAttributs(je.refElement);
            for (Element refatt : latt) {
                final Node natt = listeAttributs.getNamedItemNS(je.doc.cfg.espaceAttribut(refatt),
                    je.doc.cfg.nomAttribut(refatt));
                if (natt != null) {
                    px += fm.stringWidth(" ");
                    String titreAtt = je.doc.cfg.titreAttribut(je.refElement, refatt);
                    String titreVal = je.doc.cfg.titreValeurAttribut(je.refElement, refatt, natt.getNodeValue());
                    final String texteAtt = titreAtt + "=" + titreVal;
                    if (px + fm.stringWidth(texteAtt) > pxmax) {
                        g.drawString("...", px, py);
                        px += fm.stringWidth("...");
                        break;
                    }
                    g.setColor(bleuFonce);
                    g.drawString(titreAtt, px, py);
                    px += fm.stringWidth(titreAtt);
                    g.setColor(Color.black);
                    g.drawString("=", px, py);
                    px += fm.stringWidth("=");
                    g.setColor(vertFonce);
                    g.drawString(titreVal, px, py);
                    px += fm.stringWidth(titreVal);
                }
            }
        }
        g.setFont(tmpFont);
        
        // icône validité
        if (je.doc.textPane.iconeValide) {
            px += 4;
            final ImageIcon icone;
            if (valide)
                icone = iconeValide;
            else
                icone = iconeInvalide;
            g.drawImage(icone.getImage(), px, 1, null);
        }
    }
    
    // utilisé dans BaliseTransferHandler
    protected JaxeDocument getDoc() {
        if (je != null)
            return(je.doc);
        else
            return(null);
    }
    
    
    static class BaliseTransferHandler extends TransferHandler {
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }
        protected Transferable createTransferable(JComponent c) {
            if (!(c instanceof Balise))
                return(null);
            final JaxeDocument doc = ((Balise)c).getDoc();
            return(((JaxeTransferHandler)doc.textPane.getTransferHandler()).createTransferable(doc.textPane));
        }
        protected void exportDone(JComponent c, Transferable t, int action) {
            if (!(c instanceof Balise))
                return;
            final JaxeDocument doc = ((Balise)c).getDoc();
            ((JaxeTransferHandler)doc.textPane.getTransferHandler()).exportDone(doc.textPane, t, action);
        }
    }

}

