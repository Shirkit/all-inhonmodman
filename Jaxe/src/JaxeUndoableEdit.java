/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jaxe.elements.JEStyle;
import jaxe.elements.JESwing;
import jaxe.elements.JETexte;

public class JaxeUndoableEdit extends Object implements UndoableEdit {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JaxeUndoableEdit.class);

    public final static int ERREUR = 0;
    public final static int AJOUTER = 1;
    public final static int SUPPRIMER = 2;
    
    public int ajsup = ERREUR;
    public JaxeElement je;
    public int offsetdebut;
    private int longueur;
    boolean hasBeenDone;
    public String texte;
    JaxeDocument doc;
    public boolean texteDansSuivant;
    public boolean texteSansStyle;
    private final boolean regrouper;
    
    public JaxeUndoableEdit(final int ajsup, final JaxeElement je) {
        this(ajsup, je, true);
    }
    
    public JaxeUndoableEdit(final int ajsup, final JaxeElement je, final boolean regrouper) {
        this.ajsup = ajsup;
        this.je = je;
        this.regrouper = regrouper;
        offsetdebut = je.debut.getOffset();
        hasBeenDone = true;
        texte = null;
        doc = je.doc;
        texteDansSuivant = false;
        texteSansStyle = false;
        if (je instanceof JETexte) {
        // les textes ne doivent pas être utilisés avec je,
        // sinon pb d'annulation: un texte ajouté comme je ne pourrais pas être reajouté comme texte
        // ...
        // mais il faut ajouter comme je un texte sans style après un texte stylé,
        // sinon c'est ajouté à la suite du précédent !
        // -> on utilise un je si la sélection comprend tout l'élément
        // ...
        // mais encore un pb: si on utilise des je pour les textes, ils peuvent se retrouver
        // dans des éléments différents même quand ils sont à côté, et empêcher des opérations
        // -> finalement, on utilise du texte, en prenant soin de noter si on veut du texte simple
        // avec texteSansStyle
            texte = je.noeud.getNodeValue();
            //this.je = null;
            texteSansStyle = true;
        }
        if (je.debut != null && je.fin != null)
            longueur = je.fin.getOffset() - je.debut.getOffset() + 1;
        else if (texte != null)
            longueur = texte.length();
        else
            longueur = -1;
    }
    
    // constructeur utile pour insérer/supprimer du texte
    
    public JaxeUndoableEdit(final int ajsup, final JaxeDocument doc, final String texte, final int offset) {
        this(ajsup, doc, texte, offset, true);
    }

    public JaxeUndoableEdit(final int ajsup, final JaxeDocument doc, final String texte, final int offset, final boolean regrouper) {
        this.ajsup = ajsup;
        this.regrouper = regrouper;
        je = null;
        offsetdebut = offset;
        hasBeenDone = true;
        this.texte = texte;
        this.doc = doc;
        texteDansSuivant = false;
        texteSansStyle = false;
        if (ajsup == SUPPRIMER) {
            final JaxeElement jtexte = doc.elementA(offsetdebut);
            if (jtexte instanceof JETexte && jtexte.debut.getOffset() == offsetdebut &&
                    jtexte.fin.getOffset() == offsetdebut + texte.length() - 1) {
                je = jtexte;
                //this.texte = null;
                texteSansStyle = true;
            }
        }
        longueur = texte.length();
    }
    
    public boolean addEdit(final UndoableEdit anEdit) {
        if (anEdit instanceof JaxeUndoableEdit) {
            final JaxeUndoableEdit jedit = (JaxeUndoableEdit)anEdit;
            if (texte != null && jedit.texte != null && ajsup == jedit.ajsup &&
                !JaxeDocument.newline.equals(jedit.texte)) {
                if ((ajsup == AJOUTER && jedit.offsetdebut == offsetdebut + longueur) ||
                        (ajsup == SUPPRIMER && jedit.offsetdebut == offsetdebut && !jedit.texteDansSuivant)) {
                    // absorbé !
                    texte += jedit.texte;
                    longueur += jedit.longueur;
                    if (texteDansSuivant && jedit.texteSansStyle) {
                        texteDansSuivant = false;
                        texteSansStyle = true;
                        je = jedit.je;
                        je.noeud.setNodeValue(texte);
                    }
                    jedit.die();
                    return true;
                }
                if ((ajsup == AJOUTER && jedit.offsetdebut == offsetdebut) || (ajsup == SUPPRIMER &&
                        jedit.offsetdebut == offsetdebut - jedit.longueur && !texteDansSuivant)) {
                    // autre cas, absorbé aussi
                    texte = jedit.texte + texte;
                    longueur += jedit.longueur;
                    if (ajsup == SUPPRIMER)
                        offsetdebut -= jedit.longueur;
                    texteDansSuivant = jedit.texteDansSuivant;
                    if (jedit.texteSansStyle) {
                        texteSansStyle = true;
                        je = jedit.je;
                        je.noeud.setNodeValue(texte);
                    }
                    jedit.die();
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean canRedo() {
        return (ajsup != ERREUR && !hasBeenDone);
    }
    
    public boolean canUndo() {
        return (ajsup != ERREUR && hasBeenDone);
    }
    
    public void die() {
        ajsup = ERREUR;
        doc = null;
        je = null;
        texte = null;
    }
    
    private String getString(final String key) {
        return(JaxeResourceBundle.getRB().getString(key));
    }
    
    private String titreNoeud() {
        String titre;
        if (je != null && !(je instanceof JETexte)) {
            titre = null;
            if (je.noeud instanceof Element) {
                if (je.refElement != null)
                    titre = doc.cfg.titreElement(je.refElement);
            }
            if (titre == null)
                titre = je.noeud.getNodeName();
        } else if (texte != null || je instanceof JETexte)
            titre = getString("annulation.Texte");
        else
            titre = "";
        return(titre);
    }
    
    public String getPresentationName() {
        final String titre = titreNoeud();
        if (ajsup == AJOUTER)
            return(getString("annulation.Ajouter") + " " + titre);
        else if (ajsup == SUPPRIMER)
            return(getString("annulation.Supprimer") + " " + titre);
        return(null);
    }
    
    public String getRedoPresentationName() {
        final String titre = titreNoeud();
        if (ajsup == AJOUTER)
            return(getString("annulation.RefaireAjout") + " " + titre);
        else if (ajsup == SUPPRIMER)
            return(getString("annulation.RefaireSuppression") + " " + titre);
        return(null);
    }
    
    public String getUndoPresentationName() {
        final String titre = titreNoeud();
        if (ajsup == AJOUTER)
            return(getString("annulation.AnnulerAjout") + " " + titre);
        else if (ajsup == SUPPRIMER)
            return(getString("annulation.AnnulerSuppression") + " " + titre);
        return(null);
    }
    
    public boolean isSignificant() {
        return true;
    }
    
    private void ajouterNoeud() {
        if (doc.rootJE == null) {
            doc.rootJE = je;
            doc.DOMdoc.appendChild(je.noeud);
            try {
                je.creer(doc.createPosition(offsetdebut), je.noeud);
            } catch (final BadLocationException ex) {
                LOG.error("ajouterNoeud() - BadLocationException", ex);
            }
            return;
        }
        try {
            final Position newpos = doc.createPosition(offsetdebut);
            je.insererDOM(newpos, je.noeud);
            doc.dom2JaxeElement.put(je.noeud, je);
            if (je.noeud.getNodeType() == Node.ELEMENT_NODE && je.refElement == null && doc.cfg != null) {
                final Node nparent = je.noeud.getParentNode();
                if (nparent != null)
                    je.refElement = doc.cfg.getElementRef((Element)je.noeud, doc.getElementForNode(nparent).refElement);
                else
                    je.refElement = doc.cfg.getElementRef((Element)je.noeud, null);
            }
            je.init(newpos, je.noeud);
            je.debut = doc.createPosition(offsetdebut);
            je.fin = doc.createPosition(newpos.getOffset()-1);
            
            if (longueur == -1)
                longueur = je.fin.getOffset() - je.debut.getOffset() + 1;
            
            // JESwing: mise à jour du début des parents
            JaxeElement jeparent = je.getParent();
            while (jeparent instanceof JESwing && jeparent.debut.getOffset() > offsetdebut) {
                jeparent.debut = je.debut;
                jeparent = jeparent.getParent();
            }
            
            // cas des styles ou des textes à regrouper
            if ((je instanceof JEStyle || je instanceof JETexte) && jeparent != null && regrouper)
                jeparent.regrouperTextes();
        } catch (final BadLocationException ex) {
            LOG.error("ajouterNoeud() - BadLocationException", ex);
        }
    }
    
    private void ajouter() {
        if (je != null && !texteSansStyle) {
            ajouterNoeud();
            return;
        }
        JaxeElement jeparent = doc.elementA(offsetdebut);
        if (jeparent == null)
            return;
        if (texteSansStyle && jeparent instanceof JETexte)
            texteDansSuivant = true;
        if (je == null || (texteSansStyle && texteDansSuivant)) {
            // pb: comment savoir si un caractère effacé à la fin d'un string
            // avec un style se trouvait dans ce style ou à côté ?
            // -> utilisation de texteDansSuivant
            JaxeElement jesuiv = null;
            if (jeparent.debut.getOffset() == offsetdebut) {
                jesuiv = jeparent;
                jeparent = jeparent.getParent();
                if (jeparent == null)
                    return;
            }
            // le style du texte ajouté est celui de l'élément du caractère précédent
            // sauf si texteDansSuivant=true ou que l'élément précédent n'est pas du texte
            JaxeElement jestyle;
            if (jesuiv != null)
                jestyle = jesuiv;
            else
                jestyle = jeparent;
            if (!texteDansSuivant && offsetdebut > 0) {
                final JaxeElement jeprec = doc.elementA(offsetdebut-1);
                if (jeprec instanceof JETexte || jeprec instanceof JEStyle)
                    jestyle = jeprec;
                else
                    jestyle = jeparent;
            }
            final SimpleAttributeSet att = jestyle.attStyle(null);
            try {
                doc.insertString(offsetdebut, texte, att);
                if (texteDansSuivant && offsetdebut > 0 && jesuiv != null &&
                        (jesuiv instanceof JETexte || jesuiv instanceof JEStyle))
                    jesuiv.debut = doc.createPosition(offsetdebut);
                else if (jesuiv != null && jeparent instanceof JESwing) {
                    // JESwing: mise à jour du début des parents
                    if (jesuiv instanceof JESwing)
                        jesuiv.debut = doc.createPosition(offsetdebut);
                    JaxeElement jesuivparent = jeparent;
                    while (jesuivparent instanceof JESwing &&
                            jesuivparent.debut.getOffset() == offsetdebut+longueur) {
                        jesuivparent.debut = doc.createPosition(offsetdebut);
                        jesuivparent = jesuivparent.getParent();
                    }
                }
            } catch (final BadLocationException ex) {
                LOG.error("ajouter() - BadLocationException", ex);
            }
            jeparent.mettreAJourDOM();
            if (regrouper)
                jeparent.regrouperTextes();
            if ("\n".equals(texte))
                doc.majIndentAjout(offsetdebut);
        } else { // cas texteSansStyle && !texteDansSuivant
            // si l'élément a été spécifié comme un je, il faut l'ajouter comme je
            // pour éviter qu'il prenne le style de l'élément précédent
            // (sauf s'il y a du texte normal après car dans ce cas le texte
            // est ajouté là)
            ajouterNoeud();
        }
    }
    
    private void effacer() {
        JaxeElement jedebut = null;
        if ((je instanceof JETexte || je instanceof JEStyle) && je != doc.rootJE) {
            jedebut = doc.elementA(offsetdebut);
            // le je peut avoir été modifié à cause des regroupements de texte : on vérifie
            // et on coupe si nécessaire (le parent est null si le je a été regroupé avec un texte à sa gauche)
            if (je.getParent() == null) {
                if (jedebut.debut.getOffset() < offsetdebut) {
                    try {
                        je = jedebut.couper(doc.textPane.getDocument().createPosition(offsetdebut));
                        jedebut = je;
                    } catch (final BadLocationException ex) {
                        LOG.error("effacer() - BadLocationException", ex);
                    }
                } else if (jedebut.debut.getOffset() == offsetdebut)
                    je = jedebut;
                else {
                    LOG.error("effacer(): jedebut.debut.getOffset() > offsetdebut");
                    return;
                }
            } else {
                if (offsetdebut != je.debut.getOffset()) {
                    // cas improbable car parent devrait être null (l'élément de droite est mangé par celui de gauche)
                    LOG.error("effacer: offsetdebut != je.debut.getOffset()");
                }
            }
            if (longueur < je.fin.getOffset() - je.debut.getOffset() + 1) {
                try {
                    je.couper(doc.textPane.getDocument().createPosition(offsetdebut + longueur));
                } catch (final BadLocationException ex) {
                    LOG.error("effacer() - BadLocationException", ex);
                }
            }
            if (longueur > je.fin.getOffset() - je.debut.getOffset() + 1)
                LOG.error("effacer: longueur > je.fin.getOffset() - je.debut.getOffset() + 1");
        }
        if (texte != null) {
            if (jedebut == null)
                jedebut = doc.elementA(offsetdebut);
            if (jedebut.debut.getOffset() == offsetdebut &&
                    jedebut.fin.getOffset() > offsetdebut + longueur - 1)
                texteDansSuivant = true;
            if (jedebut instanceof JETexte || jedebut instanceof JEStyle) {
                if (offsetdebut <= jedebut.debut.getOffset() && jedebut.fin.getOffset() <
                    offsetdebut + longueur)
                    jedebut.getParent().supprimerEnfantDOM(jedebut);
                else if (offsetdebut + longueur > jedebut.fin.getOffset()) {
                    try {
                        jedebut.fin = doc.createPosition(offsetdebut - 1);
                    } catch (final BadLocationException ex) {
                        LOG.error("effacer() - BadLocationException", ex);
                    }
                }
            }
            try {
                doc.remove(offsetdebut, longueur, false);
            } catch (final BadLocationException ex) {
                LOG.error("effacer() - BadLocationException", ex);
            }
            JaxeElement jeparent = doc.elementA(offsetdebut);
            if (jeparent.debut.getOffset() == offsetdebut)
                jeparent = jeparent.getParent();
            jeparent.mettreAJourDOM();
            if ("\n".equals(texte))
                doc.majIndentSupp(offsetdebut);
        } else {
            JaxeElement parent = je.getParent();
            if (parent != null) {
                je.effacer();
                parent.supprimerEnfant(je);
                if (regrouper)
                    parent.regrouperTextes();
                parent.majValidite();
            } else if (je == doc.rootJE) {
                je.effacer();
                final int len = je.fin.getOffset() - je.debut.getOffset() + 1;
                final int idebut = je.debut.getOffset();
                try {
                    doc.remove(idebut, len, false);
                    doc.rootJE = null;
                    doc.DOMdoc.removeChild(je.noeud);
                } catch (final BadLocationException ex) {
                    LOG.error("effacer() - BadLocationException", ex);
                }
            } else
                LOG.error("effacer() - je.getParent() == null && je != doc.rootJE");
        }
        if (je != null)
            doc.dom2JaxeElement.remove(je.noeud);
    }
    
    public void redo() throws CannotRedoException {
        if (!canRedo())
            throw new CannotRedoException();
        if (ajsup == AJOUTER) {
            //doc.textPane.undoSpecial.redo(); // pb: newpos n'est pas modifié: comment savoir la position de fin ?
            //->remplacé par je.init
            doc.textPane.debutIgnorerEdition();
            ajouter();
            if (je != null) {
                final JaxeElement parent = je.getParent();
                if (parent != null)
                    parent.majValidite();
            }
            doc.textPane.finIgnorerEdition();
        } else if (ajsup == SUPPRIMER) {
            doc.textPane.debutIgnorerEdition();
            effacer();
            doc.textPane.finIgnorerEdition();
        }
        doc.textPane.miseAJourArbre();
        hasBeenDone = true;
    }
    
    public boolean replaceEdit(final UndoableEdit anEdit) {
        return false;
    }
    
    public void undo() throws CannotUndoException {
        if (!canUndo())
            throw new CannotUndoException();
        if (ajsup == AJOUTER) {
            doc.textPane.debutIgnorerEdition();
            effacer();
            doc.textPane.finIgnorerEdition();
        } else if (ajsup == SUPPRIMER) {
            doc.textPane.debutIgnorerEdition();
            ajouter();
            if (je != null) {
                final JaxeElement parent = je.getParent();
                if (parent != null)
                    parent.majValidite();
            }
            doc.textPane.finIgnorerEdition();
        }
        doc.textPane.miseAJourArbre();
        hasBeenDone = false;
    }

    public void doit() {
        if (ajsup == ERREUR) {
            LOG.error("doit() - erreur dans JaxeUndoableEdit");
            return;
        }
        doc.textPane.debutIgnorerEdition();
        if (ajsup == AJOUTER) {
            ajouter();
        } else if (ajsup == SUPPRIMER) {
            effacer();
        }
        doc.textPane.finIgnorerEdition();
        doc.textPane.addEdit(this);
        hasBeenDone = true;
    }
    
    public String toString() { // pour debug
        return("ajsup=" + ajsup + " je=" + je + " offsetdebut=" + offsetdebut + " longueur=" + longueur +
            " hasBeenDone=" + hasBeenDone + " texte=" + texte + " texteDansSuivant=" + texteDansSuivant +
            " texteSansStyle=" + texteSansStyle + " regrouper=" + regrouper);
    }
}
