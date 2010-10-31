/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.TextAction;

import jaxe.elements.JEStyle;
import jaxe.elements.JESwing;
import jaxe.elements.JETexte;

import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ActionInsertionBalise extends TextAction {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(ActionInsertionBalise.class);
    
    static String newline = Jaxe.newline;
    JaxeDocument doc;
    Element refElement;
    String nomNoeud;
    String typeNoeud;
    
    @Deprecated
    public ActionInsertionBalise(final JaxeDocument doc, final Element balise) {
        super(doc.cfg.titreBalise(balise));
        this.doc = doc;
        this.nomNoeud = doc.cfg.nomBalise(balise);
        this.refElement = doc.cfg.referenceElement(nomNoeud);
        this.typeNoeud = doc.cfg.noeudtypeBalise(balise);
        if ("".equals(typeNoeud))
            typeNoeud = "element";
    }
    
    public ActionInsertionBalise(final JaxeDocument doc, final String titre, final Element refElement,
            final String nomNoeud, final String typeNoeud) {
        super(titre);
        this.doc = doc;
        this.refElement = refElement;
        this.nomNoeud = nomNoeud;
        this.typeNoeud = typeNoeud;
    }
    
    public void actionPerformed(final ActionEvent e) {
        final JTextComponent target = doc.textPane;
        if (target != null) {
            doc.setModif(true);
            final JaxeDocument doc = (JaxeDocument)target.getDocument();
            final String typeAffichage = doc.cfg.typeAffichageNoeud(refElement, nomNoeud, typeNoeud);
            int start = target.getSelectionStart();
            int end = target.getSelectionEnd();
            try {
                Position pos = doc.createPosition(start);
                JaxeElement parent = null;
                if (doc.rootJE != null)
                    parent = doc.rootJE.elementA(start);
                if (parent != null && parent.debut.getOffset() == start &&
                        !(parent instanceof JESwing))
                    parent = parent.getParent() ;
                if (parent != null && (parent.noeud.getNodeType() == Node.TEXT_NODE || (parent instanceof JEStyle && !typeAffichage.equals("style")))) {
                    final JaxeElement je1 = parent;
                    parent = parent.getParent();
                    if (start > je1.debut.getOffset() && start <= je1.fin.getOffset()) {
                        // couper la zone de texte en 2
                        je1.couper(pos);
                    }
                }
                if (end - start > 0 && doc.rootJE != null) {
                    JaxeElement parent2 = doc.rootJE.elementA(end);
                    if (parent2 != null && parent2.debut.getOffset() == end &&
                            !(parent2 instanceof JESwing))
                        parent2 = parent2.getParent() ;
                    if (parent2 != null && (parent2.noeud.getNodeType() == Node.TEXT_NODE || (parent2 instanceof JEStyle && !typeAffichage.equals("style")))) {
                        if (end > parent2.debut.getOffset() && end <= parent2.fin.getOffset()) {
                            // couper la zone de texte à la fin de la sélection
                            parent2.couper(doc.createPosition(end));
                        }
                    }
                }
                if (parent == null && doc.rootJE != null) {
                    doc.getGestionErreurs().pasSousLaRacine(refElement);
                    return;
                }
                if (parent != null && !parent.getEditionAutorisee()) {
                    doc.getGestionErreurs().editionInterdite(parent, refElement);
                    return;
                }
                if (parent != null && !typeNoeud.equals("instruction") && !typeNoeud.equals("commentaire") &&
                        !typeNoeud.equals("cdata")) {
                    Config conf = doc.cfg.getRefConf(refElement);
                    if (conf == null)
                        conf = doc.cfg;
                    Element refParent = null;
                    Element parentn = (Element)parent.noeud;
                    final Config pconf = doc.cfg.getElementConf(parentn);
                    if (pconf != null && pconf != conf)
                        parentn = doc.cfg.chercheParentConfig(parentn, conf);
                    if (parentn != null)
                        refParent = doc.dom2JaxeElement.get(parentn).refElement;
                    if (refParent != null && !conf.estSousElement(refParent, refElement))  {// && !(parent instanceof JEStyle)) {
                        doc.getGestionErreurs().enfantInterditSousParent(doc.getElementForNode(parentn), refElement);
                        return;
                    }
                    if (!doc.cfg.insertionPossible(parent, start, end, refElement)) { // && !typeAffichage.equals("style")) {
                        final String expr = doc.cfg.expressionReguliere(refParent);
                        doc.getGestionErreurs().insertionImpossible(expr, parent, refElement);
                        return ;
                    }
                }
                if (typeAffichage.equals("style")) {
                    if (end - start > 0) {
                        final JEStyle newje = JEStyle.nouveau(doc, start, end, refElement);
                        if (newje != null) { // pas null si ajout de balise sur la sélection
                            final String texte = doc.textPane.getText(start, end-start);
                            doc.textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("style.Style"), false);
                            JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER,
                                doc, texte, start);
                            jedit.doit();
                            jedit = new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, newje);
                            jedit.doit();
                            doc.textPane.finEditionSpeciale();
                        }
                        if (start != end) {
                            JaxeElement jeBeg = doc.elementA(start);
                            while (!((jeBeg instanceof JEStyle) ||(jeBeg instanceof JETexte))) {
                                start++;
                                end++;
                                jeBeg = doc.elementA(start);
                            }
                            doc.textPane.setSelectionStart(start);
                            doc.textPane.setSelectionEnd(end);
                        }
                    }
                } else {
                    boolean editionSpeciale = false;
//                    doc.textPane.debutEditionSpeciale(JaxeResourceBundle.getRB().getString("insertion.InsertionBalise"), false);
                    DocumentFragment frag = null;
                    if (end - start > 0 && doc.rootJE != null) {
                        doc.textPane.debutEditionSpeciale(
                            JaxeResourceBundle.getRB().getString("insertion.InsertionBalise"), false);
                        editionSpeciale = true;
                        doc.enableIgnore();
                    }
                    final JaxeElement newje = JEFactory.createJE(doc, refElement, nomNoeud, typeNoeud, null);
                    
                    Node newel = null;
                    if (newje != null)
                        newel = newje.nouvelElement(refElement);

                    if (newel == null) { // null si annulation
                        if (end - start > 0 && doc.rootJE != null) {
                            doc.textPane.finEditionSpeciale();
//                        if (end - start > 0 && doc.rootJE != null)
//                            doc.textPane.undo();
                        }
                        SwingUtilities.invokeLater(new FocusRunnable(null));

                    } else {
                        if (end - start > 0 && doc.rootJE != null) {
                            frag = doc.copier(start, end);
                            if (frag == null) {
//                                doc.textPane.finEditionSpeciale();
                                doc.textPane.finEditionSpeciale();
//                                doc.textPane.undo();
                                return;
                            }
                            doc.remove(start, end-start);
                        }
                        final boolean event = !("instruction".equals(typeNoeud));
                        if (event) pos = doc.firePrepareElementAddEvent(pos);
                        
                        final Properties prefs = Preferences.getPref();
                        final boolean consIndent = (prefs != null &&
                            "true".equals(prefs.getProperty("consIndent")));
                        if (consIndent && newel.getFirstChild() != null) {
                            // ajout d'espaces d'indentation
                            int i1 = pos.getOffset() - 255;
                            if (i1 < 0)
                                i1 = 0;
                            String extrait = doc.textPane.getText(i1, pos.getOffset()-i1);
                            i1 = extrait.lastIndexOf('\n');
                            if (i1 != -1) {
                                extrait = extrait.substring(i1+1);
                                for (i1=0; i1<extrait.length() &&
                                        (extrait.charAt(i1) == ' ' || extrait.charAt(i1) == '\t'); i1++)
                                    ;
                                final String sindent = extrait.substring(0, i1);
                                String texte = newel.getFirstChild().getNodeValue();
                                for (int i=0; i<texte.length(); i++)
                                    if (texte.charAt(i) == '\n') {
                                        texte = texte.substring(0, i+1) + sindent + texte.substring(i+1);
                                        i += sindent.length();
                                    }
                                newel.getFirstChild().setNodeValue(texte);
                            }
                        }
                        if (newje.avecSautsDeLigne() && parent != null && parent.avecSautsDeLigne()) {
                            // ajout automatique d'un saut de ligne dans certains cas
                            // pour des éléments comme JEZone et JEDivision
                            boolean sautdebut = false;
                            boolean sautfin = false;
                            if (pos.getOffset() == parent.debut.getOffset() + 1)
                                sautdebut = true;
                            else {
                                JaxeElement precedent = parent.elementA(pos.getOffset() - 1);
                                while (precedent instanceof JETexte || precedent instanceof JEStyle)
                                    precedent = precedent.getParent();
                                if (precedent != null && precedent.fin.getOffset() + 1 == pos.getOffset())
                                    sautdebut = true;
                            }
                            if (pos.getOffset() == parent.fin.getOffset())
                                sautfin = true;
                            else {
                                JaxeElement suivant = parent.elementA(pos.getOffset() + 1);
                                while (suivant instanceof JETexte || suivant instanceof JEStyle)
                                    suivant = suivant.getParent();
                                if (suivant != null && suivant.debut.getOffset() == pos.getOffset())
                                    sautfin = true;
                            }
                            if (sautdebut || sautfin) {
                                if (!editionSpeciale) {
                                    editionSpeciale = true;
                                    doc.textPane.debutEditionSpeciale(
                                        JaxeResourceBundle.getRB().getString("insertion.InsertionBalise"), false);
                                }
                                if (sautdebut) {
                                    final int offsetpos = pos.getOffset();
                                    final JaxeUndoableEdit edit = new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER,
                                        doc, "\n", offsetpos, true);
                                    edit.doit();
                                    pos = doc.createPosition(offsetpos + 1);
                                }
                                if (sautfin) {
                                    final int offsetpos = pos.getOffset();
                                    final JaxeUndoableEdit edit = new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER,
                                        doc, "\n", offsetpos, true);
                                    edit.doit();
                                    pos = doc.createPosition(offsetpos);
                                }
                            }
                        }
                        if (doc.rootJE == null) {
                            doc.cfg.ajouterAttributsEspaces((Element)newel);
                            doc.DOMdoc.appendChild(newel);
                            doc.textPane.debutIgnorerEdition();
                            newje.creer(pos, newel);
                            doc.textPane.finIgnorerEdition();
                            doc.rootJE = newje;
                        } else
                            newje.inserer(pos, newel);
                        Position inspos = newje.insPosition();
                        if (consIndent && newel.getFirstChild() != null) {
                            int lg = 255;
                            if (inspos.getOffset() + 255 > doc.getLength())
                                lg = doc.getLength() - inspos.getOffset();
                            final String suite = doc.getText(inspos.getOffset(), lg);
                            final int in = suite.indexOf('\n');
                            if (in != -1)
                                inspos = doc.createPosition(inspos.getOffset() + in);
                        }
                        target.setCaretPosition(inspos.getOffset());
                        doc.textPane.addEdit(new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, newje));
                        if (event) doc.fireElementAddedEvent(new JaxeEditEvent(this, newje), pos);
                        if (end - start > 0) {
                            if (!doc.testerInsertionFragment(frag, newje, inspos)) {
//                                if (end - start > 0 && doc.rootJE != null)
//                                    doc.textPane.finEditionSpeciale();
                                doc.textPane.finEditionSpeciale();
                                doc.textPane.undo();
                                return;
                            }
                            doc.coller(frag, inspos);
                            doc.textPane.finEditionSpeciale();
                        } else if (editionSpeciale)
                            doc.textPane.finEditionSpeciale();
                        if (parent != null)
                            parent.majValidite();
                        newje.majValidite();
                        SwingUtilities.invokeLater(new FocusRunnable(newje));
                    }
                    doc.textPane.miseAJourArbre();
//                    doc.textPane.finEditionSpeciale();

                }
            } catch (final BadLocationException ble) {
                LOG.error("actionPerformed(ActionEvent) - Impossible d'insérer une balise.", ble);
                //ble.printStackTrace();
            } catch (final DOMException ex) {
                LOG.error("actionPerformed(ActionEvent) - DOMException", ex);
            }
        }
    }
    
    class FocusRunnable implements Runnable {
        JaxeElement newje;
        public FocusRunnable(final JaxeElement newje) {
            this.newje = newje;
        }
        public void run() {
            if (newje != null)
                newje.setFocus();
            else
                doc.textPane.requestFocus();
        }
    }
    
    @Deprecated
    public Element getDefbalise() {
        return(doc.cfg.getBaliseDef(nomNoeud));
    }
    
    public Element getRefElement() {
        return(refElement);
    }
    
    /**
     * Utilisable pour changer la référence quand le nom et le type ne changent pas
     */
    public void setRefElement(final Element ref) {
        refElement = ref;
    }
}

