/*
 * Jaxe - Editeur XML en Java
 * 
 * Copyright (C) 2002 Observatoire de Paris-Meudon
 * 
 * Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le
 * modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU,
 * telle que publi�e par la Free Software Foundation ; version 2 de la licence,
 * ou encore (� votre choix) toute version ult�rieure.
 * 
 * Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE
 * GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou
 * D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence
 * Publique G�n�rale GNU .
 * 
 * Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en
 * m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free
 * Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
 */

package jaxe;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import jaxe.elements.JEStyle;
import jaxe.elements.JETexte;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Fonction permettant d'appliquer un style sur une zone du document.
 * L'�l�ment correspondant au style ajout� est pass� en param�tre du constructeur.
 */
public class FonctionAjStyle implements Fonction {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(FonctionAjStyle.class);
    
    private final Element _elStyle; // le noeud du nouvel �l�ment JEStyle
    
    
    public FonctionAjStyle(final Element elem) {
        _elStyle = elem;
    }
    
    public boolean appliquer(final JaxeDocument doc, final int start, final int end) {
        boolean done = false;
        try {
            // firstel est l'�l�ment du d�but de la s�lection
            final JaxeElement firstel = doc.rootJE.elementA(start);
            JaxeElement p1 = firstel;
            if (p1 instanceof JEStyle || p1 instanceof JETexte)
                p1 = p1.getParent();
            // lastel est l'�l�ment de la fin de la s�lection
            final JaxeElement lastel = doc.rootJE.elementA(end - 1);
            JaxeElement p2 = lastel;
            if (p2 instanceof JEStyle || p2 instanceof JETexte)
                p2 = p2.getParent();
            // si le parent de firstel est le parent de lastel, on ne fait rien
            if (p1 != p2)
                return true;
            
            doc.textPane.debutEditionSpeciale(
                JaxeResourceBundle.getRB().getString("style.Style"), false);
            
            final Element refElement = doc.cfg.getElementRef(_elStyle);
            Config conf = doc.cfg.getRefConf(refElement);
            if (conf == null)
                conf = doc.cfg;
            
            Node next = firstel.noeud.getNextSibling();
            
            // TRAITEMENT POUR L'ELEMENT DU DEBUT DE LA SELECTION
            if ((firstel instanceof JEStyle || firstel instanceof JETexte) &&
                    (firstel.debut.getOffset() <= start)) {
                
                final int firsteldebut = firstel.debut.getOffset();
                final int firstelfin = firstel.fin.getOffset();
                
                // texte0 : texte de firstel
                final String texte0;
                if (firstel instanceof JETexte)
                    texte0 = firstel.noeud.getNodeValue();
                else
                    texte0 = ((JEStyle) firstel).getText();
                // texte1 : texte de firstel avant la s�lection
                final String texte1 = texte0.substring(0, start - firsteldebut);
                // texte2 : texte de firstel dans la s�lection
                final String texte2;
                if (firstelfin >= end)
                    texte2 = texte0.substring(start - firsteldebut, end - firsteldebut);
                else
                    texte2 = texte0.substring(start - firsteldebut);
                
                // si firstel est un JEStyle
                if (firstel instanceof JEStyle) {
                    
                    // si le style n'est pas d�j� appliqu�
                    if (!dejaApplique((JEStyle)firstel, _elStyle)) {
                        
                        final Element parentref = refParentConfig(doc, firstel, conf);
                        
                        // si _elStyle est un enfant autoris� sous firstel ou le parent de m�me config
                        if (parentref != null && conf.estSousElement(parentref, refElement)) {
                            
                            // suppression de firstel
                            JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, firstel, false);
                            jedit.doit();
                            
                            // si firstel commence avant le d�but de la s�lection
                            if (firsteldebut < start) {
                                // cr�ation d'un nouveau JEStyle avec texte1
                                ajoutNouveauJEStyle(doc, doc.createPosition(firsteldebut),
                                    texte1, (JEStyle)firstel, null);
                            }
                            // ajout d'un nouveau JEStyle avec texte2
                            final Position debut = doc.createPosition(firsteldebut + texte1.length());
                            ajoutNouveauJEStyle(doc, debut, texte2, (JEStyle)firstel, _elStyle);
                            
                            // si firstel se termine apr�s la fin de la s�lection
                            if (firstelfin >= end) {
                                // cr�ation d'un nouveau JEStyle avec le texte qui suit la s�lection dans firstel
                                final String texte3 = texte0.substring(end - firsteldebut);
                                ajoutNouveauJEStyle(doc, doc.createPosition(end),
                                    texte3, (JEStyle)firstel, null);
                            }
                            
                            done = true;
                            
                        }
                    } else
                        done = true;
                    
                // si firstel est un JETexte
                } else if (firstel instanceof JETexte) {
                    
                    // suppression de firstel
                    JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, firstel, false);
                    jedit.doit();
                    
                    // si firstel commence avant la s�lection
                    if (firsteldebut < start) {
                        // ajout d'un nouveau JETexte avec le texte avant la s�lection
                        ajoutNouveauJETexte(doc, doc.createPosition(firsteldebut), texte1);
                    }
                    
                    // ajout d'un nouveau JEStyle avec texte2
                    final Position debut = doc.createPosition(firsteldebut + texte1.length());
                    ajoutNouveauJEStyle(doc, debut, texte2, null, _elStyle);
                    
                    // si firstel se termine apr�s la fin de la s�lection
                    if (firstelfin >= end) {
                        // ajout d'un JETexte avec le texte qui suit la s�lection dans firstel
                        final String texte3 = texte0.substring(end - firsteldebut);
                        ajoutNouveauJETexte(doc, doc.createPosition(end), texte3);
                    }
                    
                    done = true;
                    
                }
                
            } else if (firstel instanceof JEStyle || firstel instanceof JETexte) {
                // si firstel commence apr�s le d�but de la s�lection (cas improbable !)
                tostyle(firstel, _elStyle);
                done = true;
            }
            
            // TRAITEMENT POUR LES ELEMENTS ENTIEREMENT A L'INTERIEUR DE LA SELECTION
            // si le d�but de la s�lection n'est pas dans le m�me �l�ment que la fin de la s�lection
            if (lastel != firstel) {
                int pos = firstel.fin.getOffset();
                
                // pour tous les �l�ments dans la s�lection
                while (next != null && next != lastel.noeud && pos < end) {
                    final JaxeElement je = p1.elementA(pos);
                    next = je.noeud.getNextSibling();
                    pos = je.fin.getOffset() + 1;
                    
                    tostyle(je, _elStyle);
                    
                    done = true;
                    
                }
            }
            
            // TRAITEMENT POUR L'ELEMENT DE LA FIN DE LA SELECTION
            // attention, lastel pourrait avoir chang� ici si on
            // n'avait pas utilis� regrouper=false dans new JaxeUndoableEdit
            if (lastel != firstel && (lastel instanceof JETexte || lastel instanceof JEStyle)) {
                
                // uniquement si lastel se termine apr�s la fin de la s�lection (sinon il est trait� avant)
                if (lastel.fin.getOffset() >= end) {
                    
                    if (lastel instanceof JETexte || lastel instanceof JEStyle) {
                        final int lasteldebut = lastel.debut.getOffset();
                        final int lastelfin = lastel.fin.getOffset();
                        // texte0 : texte de lastel
                        final String texte0;
                        if (lastel instanceof JETexte)
                            texte0 = lastel.noeud.getNodeValue();
                        else
                            texte0 = ((JEStyle)lastel).getText();
                        // texte1 : texte de lastel avant la fin de la s�lection
                        final String texte1 = texte0.substring(0, end - lasteldebut);
                        // texte2 : texte de lastel apr�s la s�lection
                        final String texte2 = texte0.substring(end - lasteldebut);
                        
                        // si l'�l�ment de fin est un JEStyle
                        if (lastel instanceof JEStyle) {
                            
                            // si le style n'est pas d�j� appliqu�
                            if (!dejaApplique((JEStyle)lastel, _elStyle)) {
                                final Element parentref = refParentConfig(doc, lastel, conf);
                                
                                // si _elStyle est un enfant autoris� sous lastel ou le parent de m�me config
                                if (parentref != null && conf.estSousElement(parentref, refElement)) {
                                    
                                    // suppression de lastel
                                    JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, lastel, false);
                                    jedit.doit();
                                    
                                    // ajout d'un nouveau JEStyle avec texte1, avec le nouveau style en plus
                                    ajoutNouveauJEStyle(doc, doc.createPosition(lasteldebut), texte1, (JEStyle)lastel, _elStyle);
                                    
                                    // ajout d'un nouveau JEStyle avec texte2, avec les m�mes styles que lastel
                                    ajoutNouveauJEStyle(doc, doc.createPosition(end), texte2, (JEStyle)lastel, null);
                                    
                                    done = true;
                                }
                            } else
                                done = true;
                            
                        // si l'�l�ment de fin est un JETexte
                        } else if (lastel instanceof JETexte) {
                            
                            // suppression de lastel
                            JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, lastel, false);
                            jedit.doit();
                            
                            // ajout d'un nouveau JEStyle avec texte1
                            final Position debut = doc.createPosition(lasteldebut);
                            ajoutNouveauJEStyle(doc, debut, texte1, null, _elStyle);
                            
                            // ajout d'un nouveau JETexte avec le texte apr�s la s�lection
                            ajoutNouveauJETexte(doc, doc.createPosition(end), texte2);
                            
                            done = true;
                        }
                    }
                } else {
                    tostyle(lastel, _elStyle);
                    done = true;
                }
            }
        } catch (final BadLocationException ex) {
            LOG.error("appliquer(JaxeDocument, int, int) - BadLocationException", ex);
        }
        doc.textPane.finEditionSpeciale();
        return done;
    }
    
    /**
     * Applique le style � un JEStyle ou un JETexte
     */
    private static boolean tostyle(final JaxeElement je, final Element elStyle) throws BadLocationException {
        boolean done = false;
        final JaxeDocument doc = je.doc;
        
        // si l'�l�ment est un JEStyle
        if (je instanceof JEStyle) {
            final JEStyle js = (JEStyle) je;
            
            // si le style n'est pas d�j� appliqu�
            if (!dejaApplique(js, elStyle)) {
                final Element refElement = doc.cfg.getElementRef(elStyle);
                
                Config conf = doc.cfg.getRefConf(refElement);
                if (conf == null)
                    conf = doc.cfg;
                
                final Element parentref = refParentConfig(doc, js, conf);
                
                // si elStyle est un enfant autoris� sous js ou le parent de m�me config
                if (parentref != null && conf.estSousElement(parentref, refElement)) {
                    
                    // r�cup�ration du texte de l'�l�ment
                    final String texte = js.getText();
                    // r�cup�ration de la position de d�but
                    final int offsetdebut = js.debut.getOffset();
                    
                    // suppression de l'�l�ment
                    JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, js, false);
                    jedit.doit();
                    
                    // remplacement par un nouveau JEStyle avec le nouveau style en plus
                    ajoutNouveauJEStyle(doc, doc.createPosition(offsetdebut), texte, js, elStyle);
                    
                    done = true;
                }
            } else
                done = true;
            
        // si l'�l�ment est un JETexte
        } else if (je instanceof JETexte) {
            
            // r�cup�ration du texte de l'�l�ment
            final String texte = je.noeud.getNodeValue();
            // r�cup�ration de la position de d�but
            final int offsetdebut = je.debut.getOffset();
            
            // suppression de l'�l�ment
            JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.SUPPRIMER, je, false);
            jedit.doit();
            
            // remplacement par un nouveau JEStyle avec le nouveau style
            ajoutNouveauJEStyle(doc, doc.createPosition(offsetdebut), texte, null, elStyle);
            
            done = true;
        }
        return(done);
    }

    /**
     * Renvoie la r�f�rence du JaxeElement je s'il est dans la config conf, ou du parent de je dans conf sinon
     */
    private static Element refParentConfig(final JaxeDocument doc, final JaxeElement je, final Config conf) {
        Element parentref = null;
        Element parentns = (Element)je.noeud;
        // si la config de je est diff�rente de la config conf
        if (doc.cfg.getElementConf(parentns) != conf)
            // Cherche le premier �l�ment anc�tre de m�me config
            parentns = doc.cfg.chercheParentConfig(parentns, conf);
        if (parentns != null)
            parentref = doc.dom2JaxeElement.get(parentns).refElement;
        return(parentref);
    }
    
    /**
     * Ajout d'un nouveau JETexte.
     */
    private static void ajoutNouveauJETexte(final JaxeDocument doc, final Position debut,
            final String texte) {
        
        final JETexte newje = JETexte.nouveau(doc, debut, null, texte);
        final JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, newje, false);
        jedit.doit();
    }
    
    /**
     * Ajout d'un nouveau JEStyle,
     * �ventuellement en reprenant les m�mes styles qu'un autre JEStyle,
     * et �ventuellement en ajoutant un style en plus.
     */
    private static void ajoutNouveauJEStyle(final JaxeDocument doc, final Position debut,
            final String texte, final JEStyle baseJEStyle, final Element elStyle) {
        
        final JEStyle newje = new JEStyle(doc);
        Node newel = doc.DOMdoc.createTextNode(texte);
        if (baseJEStyle != null) {
            final List<Element> nPath = new ArrayList<Element>(baseJEStyle._styles);
            if (elStyle != null && !containsNode(nPath, elStyle))
                nPath.add(0, elStyle);
            Iterator<Element> it = nPath.iterator();
            while (it.hasNext()) {
                final Node node = ((Node) it.next()).cloneNode(false);
                node.appendChild(newel);
                newel = node;
            }
        } else if (elStyle != null) {
            final Node node = elStyle.cloneNode(false);
            node.appendChild(newel);
            newel = node;
        }
        newje.noeud = newel;
        newje.debut = debut;
        newje.fin = null;
        final JaxeUndoableEdit jedit = new JaxeUndoableEdit(JaxeUndoableEdit.AJOUTER, newje, false);
        jedit.doit();
    }
    
    private static boolean containsNode(final List<Element> list, final Node node) {
        final Iterator<Element> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().getNodeName().equals(node.getNodeName()))
                return(true);
        }
        return false;
    }
    
    /**
     * Renvoie true si l'�l�ment elStyle est dans le JEStyle
     */
    private static boolean dejaApplique(final JEStyle js, final Element elStyle) {
        final JaxeDocument doc = js.doc;
        final Element refStyle = doc.cfg.getElementRef(elStyle);
        // done <- true si le style est d�j� appliqu� dans firstel
        final Iterator<Element> it = js._styles.iterator();
        while (it.hasNext()) {
            final Element refStyleIt = doc.cfg.getElementRef(it.next());
            if (refStyleIt == refStyle)
                return(true);
        }
        return(false);
    }
}
