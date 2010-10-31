/*
 Jaxe - Editeur XML en Java

 Copyright (C) 2003 Observatoire de Paris

 Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

 Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

 Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
 */

package jaxe;

import org.apache.log4j.Logger;

import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.TextAction;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import jaxe.elements.JEStyle;
import jaxe.elements.JESwing;
import jaxe.elements.JETexte;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;


/**
 * Zone de texte éditable correspondant à un document XML. Peut être utilisée
 * indépendamment de JaxeFrame et JaxeMenuBar.
 */
public class JaxeTextPane extends JTextPane {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JaxeTextPane.class);

    static int cmdMenu;

    //undo helpers
    UndoManager undo;

    boolean ignorerEdition = false;

    private boolean editionSpeciale = false;

    private CompoundEdit editSpecial;

    private int niveauEditionSpeciale = 0;

    private final Stack<Boolean> ignorerEditionStack = new Stack<Boolean>();

    static String texteRecherche = null;

    private final ArrayList<EcouteurMAJ> ecouteursArbre = new ArrayList<EcouteurMAJ>();

    private final ArrayList<EcouteurMAJ> ecouteursAnnulation = new ArrayList<EcouteurMAJ>();

    private DialogueRechercher dlgRecherche = null;
    JaxeDocument doc;

    public JFrame jframe;
    
    public boolean iconeValide = true;
    
    private static final JaxeTransferHandler jth = new JaxeTransferHandler();
    
    
    public JaxeTextPane(final JaxeDocument doc, final JFrame jframe) {
        this(doc, jframe, true);
    }

    public JaxeTextPane(final JaxeDocument doc, final JFrame jframe, final boolean iconeValide) {
        super();
        setEditorKit(doc.createEditorKit());
        setStyledDocument(doc);
        this.undo = new JaxeUndoManager(doc);
        this.doc = doc;
        this.iconeValide = iconeValide;
        this.jframe = jframe;
        doc.setTextPane(this);
        cmdMenu = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        final Keymap kmap = getKeymap();
        // attention, cette Keymap n'est pas liée à l'objet, et la modifier peut provoquer des fuites de mémoire
        // (elle garderait des référence au textpane quand il n'est plus utilisé)
        // on utilise donc static pour ActionMenuContextuel
        final KeyStroke cmdsp = KeyStroke.getKeyStroke(KeyEvent.VK_D, cmdMenu);
        kmap.addActionForKeyStroke(cmdsp, new ActionMenuContextuel());

        doc.addUndoableEditListener(new MyUndoableEditListener());
        addCaretListener(new MyCaretListener());

        setTabs(4);

        setHighlighter(new JaxeHighlighter());
        
        setTransferHandler(jth);
        setDragEnabled(true);
    }

    class JaxeHighlighter extends DefaultHighlighter {

        @Override
        public Object addHighlight(final int p0, final int p1,
                final Highlighter.HighlightPainter p) throws BadLocationException {
            final Object o = super.addHighlight(p0, p1, p);
            selectZone(p0, p1, true, false);
            return (o);
        }

        @Override
        public void changeHighlight(final Object tag, final int p0, final int p1)
                throws BadLocationException {
            final Highlighter.Highlight highlight = (Highlighter.Highlight) tag;
            final int v0 = highlight.getStartOffset();
            final int v1 = highlight.getEndOffset();
            super.changeHighlight(tag, p0, p1);
            selectZone(v0, v1, false, false);
            selectZone(p0, p1, true, false);
            return;
        }

        @Override
        public void removeHighlight(final Object tag) {
            super.removeHighlight(tag);
            final Highlighter.Highlight highlight = (Highlighter.Highlight) tag;
            selectZone(highlight.getStartOffset(), highlight.getEndOffset(),
                    false, false);
        }
    }

    public UndoManager getUndo() {
        return (undo);
    }

    public void undo() {
        try {
            undo.undo();
        } catch (final CannotUndoException ex) {
            LOG.error("undo() - " + JaxeResourceBundle.getRB().getString("annulation.ImpossibleAnnuler"), ex);
        }
        miseAJourAnnulation();
    }

    public boolean getEditionSpeciale() {
        return (editionSpeciale);
    }

    public boolean getIgnorerEdition() {
        return (ignorerEdition);
    }

    protected static class ActionMenuContextuel extends TextAction {

        public ActionMenuContextuel() {
            super("menuContextuel");
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target instanceof JaxeTextPane)
                ((JaxeTextPane) target).menuContextuel(-1, null);
        }
    }

    @Override
    public void processMouseEvent(final MouseEvent e) {
        if (e.isPopupTrigger() && this.isEditable()) {
            showPopup(e);
        } else {
            super.processMouseEvent(e);
        }
    }

    private void showPopup(final MouseEvent e) {
        if (e.isPopupTrigger())
            menuContextuel(-1, e.getPoint());
    }

    protected void menuContextuel(final int pos, Point pt) {
        int debutSelection = getSelectionStart();
        int finSelection = getSelectionEnd();
        int posClick;
        if (pos == -1 && doc.rootJE != null && pt != null)
            posClick = viewToModel(pt);
        else
            posClick = -1;
        if (posClick != -1 && (debutSelection == finSelection || posClick < debutSelection || posClick > finSelection)) {
            debutSelection = posClick;
            finSelection = posClick;
            setCaretPosition(debutSelection);
            moveCaretPosition(finSelection);
        } else if (pos != -1 && debutSelection == finSelection) {
            debutSelection = pos;
            finSelection = pos;
            setCaretPosition(debutSelection);
            moveCaretPosition(finSelection);
        }
        if (pt == null) {
            try {
                final Rectangle r = modelToView(debutSelection);
                pt = r.getLocation();
            } catch (final BadLocationException ex) {
                LOG.error("menuContextuel(Point)", ex);
                return;
            }
        }
        final JPopupMenu popup = new JPopupMenu();
        ArrayList<Element> autorisees = null;
        Config conf;
        JaxeElement je;
        if (doc.rootJE == null) {
            je = null;
            conf = doc.cfg;
            autorisees = conf.listeElementsRacines();
        } else {
            je = doc.elementA(debutSelection);
            if (je == null)
                return;

            if (je instanceof JETexte)
                je = je.getParent();
            if (debutSelection == je.debut.getOffset() && !(je instanceof JESwing))
                je = je.getParent();

            if (je == null || !je.getEditionAutorisee())
                return;

            if (doc.cfg == null || !(je.noeud instanceof Element)) {
                conf = null;
                autorisees = new ArrayList<Element>();
            } else {
                conf = doc.cfg.getElementConf((Element) je.noeud);
                autorisees = conf.listeSousElements(je.refElement);
            }
        }
        for (final Element ref : autorisees) {
            if (!("style".equals(doc.cfg.typeAffichageElement(ref)))) {
                if (je == null || conf.insertionPossible(je, debutSelection, finSelection, ref)) {
                    final String nom = conf.nomElement(ref);
                    final String titre = conf.titreMenu(nom);
                    popup.add(new ActionInsertionBalise(doc, titre, ref, nom, "element"));
                }
            }
        }
        
        if (autorisees.size() > 0) { // Seperator between elements and
            // Copy'n'Paste
            popup.addSeparator();
        }
        
        if (debutSelection != finSelection) { // Copy allowed ?
            popup.add(new ActionCouper());
            popup.add(new ActionCopier());
        }
        
        popup.add(new ActionColler());
        
        if (je != null && conf != null) {
            popup.addSeparator();
            popup.add(new ActionAide(je.refElement));
        }
        
        popup.show(this, pt.x, pt.y);
    }
    
    // recopié de JaxeMenuBar (jth.getCutAction ne marche pas si l'origine de l'action est le popup,
    // et on évite d'utiliser JaxeMenuBar.ActionCouper pour que JaxeTextPane reste indépendant)
    protected static class ActionCouper extends TextAction {
        public ActionCouper() {
            super(JaxeResourceBundle.getRB().getString("menus.Couper"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target != null)
                target.cut();
        }
    }
    
    protected static class ActionCopier extends TextAction {
        public ActionCopier() {
            super(JaxeResourceBundle.getRB().getString("menus.Copier"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target != null)
                target.copy();
        }
    }
    
    protected static class ActionColler extends TextAction {
        public ActionColler() {
            super(JaxeResourceBundle.getRB().getString("menus.Coller"));
        }

        public void actionPerformed(final ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if (target != null)
                target.paste();
        }
    }
    
    class ActionAide extends AbstractAction {
        Element refElement;

        ActionAide(final Element refElement) {
            super(JaxeResourceBundle.getRB().getString("aide.element") + " "
                    + doc.cfg.titreElement(refElement));
            this.refElement = refElement;
        }

        public void actionPerformed(final ActionEvent e) {
            final DialogueAideElement dlg = new DialogueAideElement(refElement,
                    doc.cfg.getRefConf(refElement),
                    (JFrame) getTopLevelAncestor());
            dlg.setVisible(true);
        }
    }

    public void selectZone(final int debut, final int fin, final boolean select, final boolean modsel) {
        ArrayList<JaxeElement> tel = doc.rootJE.elementsDans(debut, fin - 1);
        if (select) {
            // on change la sélection pour ne pas inclure des moitié d'éléments
            // (sauf pour le texte)
            int ndebut = debut;
            int nfin = fin;
            
            boolean modif;
            JaxeElement jedebut = doc.elementA(debut);
            if (jedebut instanceof JETexte || jedebut instanceof JEStyle)
                jedebut = jedebut.getParent();
            JaxeElement jefin = doc.elementA(fin);
            if (jefin instanceof JETexte || jefin instanceof JEStyle)
                jefin = jefin.getParent();
            do {
                modif = false;
                if (jedebut != null && jedebut.debut.getOffset() < ndebut &&
                        nfin > jedebut.fin.getOffset() && !tel.contains(jedebut)) {
                    ndebut = jedebut.fin.getOffset() + 1;
                    modif = true;
                    jedebut = doc.elementA(ndebut);
                    if (jedebut instanceof JETexte || jedebut instanceof JEStyle)
                        jedebut = jedebut.getParent();
                }
                if (jefin != null && jefin.debut.getOffset() < nfin &&
                        ((jefin instanceof JESwing && ndebut < jefin.debut.getOffset()) ||
                        (!(jefin instanceof JESwing) && ndebut <= jefin.debut.getOffset())) && !tel.contains(jefin)) {
                    nfin = jefin.debut.getOffset();
                    modif = true;
                    jefin = doc.elementA(nfin);
                    if (jefin instanceof JETexte || jefin instanceof JEStyle)
                        jefin = jefin.getParent();
                }
            } while (modif);
            
            for (final JaxeElement je : tel) {
                if (je.debut.getOffset() >= ndebut && je.fin.getOffset() < nfin) {
                    final JaxeElement jeparent = je.getParent();
                    if (jeparent != null && !(jeparent instanceof JETexte || jeparent instanceof JEStyle) && !tel.contains(jeparent)) {
                        if (ndebut <= jeparent.debut.getOffset()) {
                            if (jeparent instanceof JESwing)
                                ndebut = jeparent.debut.getOffset();
                            else
                                ndebut = jeparent.debut.getOffset() + 1;
                        }
                        if (nfin > jeparent.fin.getOffset())
                            nfin = jeparent.fin.getOffset();
                    }
                    final Node nprecedent = je.noeud.getPreviousSibling();
                    if (nprecedent != null) {
                        final JaxeElement jeprecedent = doc.getElementForNode(nprecedent);
                        if (jeprecedent != null && jeprecedent.fin != null &&
                                !(jeprecedent instanceof JETexte || jeprecedent instanceof JEStyle) &&
                                !tel.contains(jeprecedent)) {
                            if (ndebut <= jeprecedent.fin.getOffset()) {
                                if (jeprecedent instanceof JESwing)
                                    ndebut = jeprecedent.fin.getOffset();
                                else
                                    ndebut = jeprecedent.fin.getOffset() + 1;
                            }
                        }
                    }
                    final Node nsuivant = je.noeud.getNextSibling();
                    if (nsuivant != null) {
                        final JaxeElement jesuivant = doc.getElementForNode(nsuivant);
                        if (jesuivant != null && jesuivant.debut != null &&
                                !(jesuivant instanceof JETexte || jesuivant instanceof JEStyle) &&
                                !tel.contains(jesuivant)) {
                            if (nfin > jesuivant.debut.getOffset())
                                nfin = jesuivant.debut.getOffset();
                        }
                    }
                }
            }
            
            if (modsel && (ndebut != debut || nfin != fin)) {
                if (nfin == ndebut)
                    nfin = ndebut = debut;
                SwingUtilities.invokeLater(new ChangementSelection(ndebut, nfin));
            }
            if (ndebut != debut || nfin != fin)
                tel = doc.rootJE.elementsDans(ndebut, nfin - 1);
        }
        for (final JaxeElement je : tel)
            je.selection(select);
    }
    
    class ChangementSelection implements Runnable {
        private int debut, fin;
        public ChangementSelection(int debut, int fin) {
            this.debut = debut;
            this.fin = fin;
        }
        public void run() {
            setCaretPosition(debut);
            moveCaretPosition(fin);
        }
    }
    
    /**
     * Sélectionne le noeud DOM donné en paramètre.
     */
    public void selectElement(final Node n) {
        JaxeElement je = doc.getElementForNode(n);
        if (je == null && n.getParentNode() != null)
            je = doc.getElementForNode(n.getParentNode());
        if (je == null)
            return;
        select(je.debut.getOffset(), je.fin.getOffset() + 1); //change la position de la vue :(
    }
    
    /**
     * Positionne le document à la ligne indiquée (la première ligne a le numéro
     * 1)
     */
    public void allerLigne(int ligne) {
        if (ligne > 0)
            ligne--;
        else
            ligne = 0;
        final int pos = doc.getDefaultRootElement().getElement(ligne).getStartOffset();
        // bidouille pour afficher la position en haut de la fenêtre
        try {
            scrollRectToVisible(modelToView(doc.getLength()));
            scrollRectToVisible(modelToView(pos));
        } catch (final BadLocationException ex) {
        }
    }
    
    /**
     * Positionne le document au début de l'élément DOM donné en paramètre.
     */
    public void allerElement(final Element el) {
        JaxeElement je = doc.getElementForNode(el);
        Point ptDebut; // position du début de l'élément dans la vue (point en haut à gauche)
        if (je != null) {
            final int placeCurseur = je.debut.getOffset();
            try {
                final Rectangle r = doc.textPane.modelToView(placeCurseur);
                ptDebut = new Point(r.x, r.y);
            } catch (final BadLocationException ex) {
                LOG.error("JaxeTextPane.allerElement modelToView", ex);
                return;
            }
        } else {
            Node p = el.getParentNode();
            je = doc.getElementForNode(p);
            while (je == null && p != null && p.getNodeType() == Node.ELEMENT_NODE) {
                p = p.getParentNode();
                je = doc.getElementForNode(p);
            }
            if (je != null)
                ptDebut = je.getPointEnfant(el);
            else
                ptDebut = null;
            if (ptDebut == null)
                return;
        }
        // bidouille pour afficher la position en haut de la fenêtre
        final Rectangle rDebut = new Rectangle(ptDebut);
        rDebut.height = doc.textPane.getVisibleRect().height - 1;
        doc.textPane.scrollRectToVisible(rDebut);
    }
    
    public void debutIgnorerEdition() {
        ignorerEdition = true;
    }

    public void finIgnorerEdition() {
        ignorerEdition = false;
    }

    class EditSpecial extends CompoundEdit {

        String titre;

        public EditSpecial(final String titre) {
            this.titre = titre;
        }

        @Override
        public String getPresentationName() {
            return (titre);
        }

        @Override
        public String getUndoPresentationName() {
            return (JaxeResourceBundle.getRB().getString("menus.Annuler") + " " + titre);
        }

        @Override
        public String getRedoPresentationName() {
            return (JaxeResourceBundle.getRB().getString("menus.Retablir")
                    + " " + titre);
        }
    }

    /**
     * Edition spéciale: combinaison d'un ensemble de JaxeUndoableEdit.
     */
    public void debutEditionSpeciale(final String titre, final boolean ignorerEdition) {
        if (niveauEditionSpeciale < 0)
            LOG.error("debutEditionSpeciale(String, boolean) - Erreur: niveauEditionSpeciale < 0 !", null);
        if (niveauEditionSpeciale == 0) {
            editSpecial = new EditSpecial(titre);
            editionSpeciale = true;
            this.ignorerEdition = ignorerEdition;
        } else {
            ignorerEditionStack.push(ignorerEdition);
            this.ignorerEdition = ignorerEdition;
        }
        niveauEditionSpeciale += 1;
    }

    public void finEditionSpeciale() {
        niveauEditionSpeciale -= 1;
        if (niveauEditionSpeciale < 0)
            LOG.error("finEditionSpeciale() - Erreur: niveauEditionSpeciale < 0 !", null);
        if (niveauEditionSpeciale == 0) {
            editSpecial.end();
            undo.addEdit(editSpecial);
            miseAJourAnnulation();
            editionSpeciale = false;
            ignorerEdition = false;
            editSpecial = null;
        } else {
            this.ignorerEdition = (ignorerEditionStack.pop())
                    .booleanValue();
        }
    }

    public void addEdit(final UndoableEdit edit) {
        if (editionSpeciale) {
            editSpecial.addEdit(edit);
        } else {
            getUndo().addEdit(edit);
            miseAJourAnnulation();
        }
        doc.setModif(true);
    }

    //This one listens for edits that can be undone.
    protected class MyUndoableEditListener implements UndoableEditListener {

        public void undoableEditHappened(final UndoableEditEvent e) {
            //Remember the edit and update the menus.
            if (!ignorerEdition) {
                undo.addEdit(e.getEdit());
                miseAJourAnnulation();
            }
        }
    }
    
    /**
     * @deprecated remplacé par cut()
     */
    @Deprecated
    public void couper() {
        cut();
    }
    
    /**
     * Coupe une zone du document et la place dans le presse-papier
     * les paramètres ne sont plus utilisés (la sélection est utilisée à la place)
     */
    @Deprecated
    public void couper(final int debut, final int fin) {
        cut();
    }

    /**
     * Copie la sélection dans le presse-papier
     * @deprecated remplacé par copy()
     */
    @Deprecated
    public void copier() {
        copy();
    }

    /**
     * Colle le contenu du presse-papier en remplaçant la sélection
     * @deprecated remplacé par paste()
     */
    @Deprecated
    public boolean coller() {
        paste();
        return(true);
    }
    
    /**
     * Ne fait plus rien. Etait appelé par JTPClipOwner.lostOwnership().
     */
    @Deprecated
    public static void effacerPressePapier() {
        
    }
    
    public void toutSelectionner() {
        setCaretPosition(0);
        moveCaretPosition(doc.getLength());
    }

    public void rechercher() {
        if (dlgRecherche == null)
            dlgRecherche = new DialogueRechercher(doc, this);
        dlgRecherche.setVisible(true);
    }

    public void rechercher(final String s) {
        texteRecherche = s;
        final int len = texteRecherche.length();
        int ind = -1;
        String text;
        // recherche bourrin
        try {
            for (int i = 0; i < doc.getLength() - len; i++) {
                text = doc.getText(i, len);
                if (text.equals(texteRecherche)) {
                    ind = i;
                    break;
                }
            }
        } catch (final BadLocationException ex) {
            LOG.error("rechercher(String) - BadLocationException", ex);
            return;
        }
        if (ind != -1) {
            setCaretPosition(ind);
            moveCaretPosition(ind + len);
        } else
            getToolkit().beep();
    }

    public void suivant() {
        if (dlgRecherche != null) {
            texteRecherche = dlgRecherche.getTexteRecherche();
            dlgRecherche.suivant(getSelectionStart());
        }
    }

    public void ajouterEcouteurArbre(final EcouteurMAJ ec) {
        ecouteursArbre.add(ec);
    }

    public void retirerEcouteurArbre(final EcouteurMAJ ec) {
        ecouteursArbre.remove(ec);
    }

    public void miseAJourArbre() {
        for (final EcouteurMAJ ema : ecouteursArbre)
            ema.miseAJour();
    }

    public void ajouterEcouteurAnnulation(final EcouteurMAJ ec) {
        ecouteursAnnulation.add(ec);
    }

    public void retirerEcouteurAnnulation(final EcouteurMAJ ec) {
        ecouteursAnnulation.remove(ec);
    }

    public void miseAJourAnnulation() {
        for (final EcouteurMAJ ema : ecouteursAnnulation)
            ema.miseAJour();
    }

    //This listens for and reports caret movements.
    protected class MyCaretListener implements CaretListener {

        boolean run = true;

        int vdot = 0;

        int vmark = 0;

        public void caretUpdate(final CaretEvent e) {
            if (run) {
                run = false;
                int dot = e.getDot();
                int mark = e.getMark();
                if (dot == mark) { // no selection
                    if (vmark - vdot > 0) // on déselectionne
                        selectZone(vdot, vmark, false, true);
                } else { // la sélection des images du texte n'est pas gérée par
                    // Swing !
                    if (dot > mark) {
                        dot += mark; // faut pas gâcher les variables
                        mark = dot - mark;
                        dot = dot - mark;
                    }
                    if (vdot != dot || vmark != mark)
                        selectZone(vdot, vmark, false, true);
                    selectZone(dot, mark, true, true);
                }
                vdot = dot;
                vmark = mark;
                run = true;
            }

        }
    }

    public void setTabs(final int charactersPerTab) {
        final FontMetrics fm = getFontMetrics(getFont());
        final int charWidth = fm.charWidth('w');
        final int tabWidth = charWidth * charactersPerTab;

        final TabStop[] tabs = new TabStop[10];

        for (int j = 0; j < tabs.length; j++) {
            final int tab = j + 1;
            tabs[j] = new TabStop(tab * tabWidth);
        }

        final TabSet tabSet = new TabSet(tabs);
        final SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setTabSet(attributes, tabSet);
        final int length = doc.getLength();
        debutIgnorerEdition();
        doc.setParagraphAttributes(0, length, attributes, false);
        finIgnorerEdition();
    }
    
    public void setIconeValide(final boolean iconeValide) {
        this.iconeValide = iconeValide;
        if (doc != null)
            doc.styleChanged();
        doLayout();
    }
    
}
