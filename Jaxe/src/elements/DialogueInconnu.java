/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import jaxe.DialogueChamps;
import jaxe.JaxeDocument;
import jaxe.JaxeResourceBundle;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Dialogue pour JEInconnu
 */
public class DialogueInconnu extends DialogueListeChamps {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueInconnu.class);

    Element el;
    JaxeDocument doc;
    
    public DialogueInconnu(final JFrame frame, final JaxeDocument doc, final String titre, final Vector<String> data, final Element el) {
        super(frame, titre, data);
        this.doc = doc;
        this.el = el;
    }
    
    @Override
    public void ajouter() {
        final String[] titres = {JaxeResourceBundle.getRB().getString("nom"),
            JaxeResourceBundle.getRB().getString("valeur")};
        final JTextComponent[] champs = new JTextComponent[2];
        champs[0] = new JTextField(10);
        champs[1] = new JTextField(20);
        final DialogueChamps dlg = new DialogueChamps(frame,
            JaxeResourceBundle.getRB().getString("liste.NouvelElement"), titres, champs);
        if (!dlg.afficher())
            return;
        final String nom = champs[0].getText();
        final String valeur = champs[1].getText();
        lmodel.addElement(nom + "=" + valeur);
    }
    
    @Override
    public void modifier() {
        final int index = jliste.getSelectedIndex();
        if (index != -1) {
            final String nomvaleur = (String)jliste.getSelectedValue();
            final int ie = nomvaleur.indexOf('=');
            String nom = nomvaleur;
            String valeur = null;
            if (ie != -1) {
                nom = nomvaleur.substring(0, ie);
                valeur = nomvaleur.substring(ie+1);
            }
            valeur = (String)JOptionPane.showInputDialog(frame, JaxeResourceBundle.getRB().getString("valeur"),
                JaxeResourceBundle.getRB().getString("liste.ModifierElement"), JOptionPane.QUESTION_MESSAGE, null, null, valeur);
            if (valeur != null)
                lmodel.set(index, nom + "=" + valeur);
        }
    }

    public void enregistrerReponses() {
        // efface tous les attributs
        final NamedNodeMap attmap = el.getAttributes();
        for (int i=0; i<attmap.getLength(); i++) {
            final Node attn = attmap.item(i);
            final String name = attn.getNodeName();
            el.removeAttribute(name);
        }
        
        // ajoute les nouveaux attributs
        for (final String nomvaleur : data) {
            String nom = nomvaleur;
            String valeur = null;
            final int ie = nomvaleur.indexOf('=');
            if (ie != -1) {
                nom = nomvaleur.substring(0, ie);
                valeur = nomvaleur.substring(ie+1);
            }
            try {
                el.setAttribute(nom, valeur);
            } catch (final DOMException ex) {
                LOG.error("enregistrerReponses() - DOMException", ex);
            }
        }
        doc.setModif(true);
    }
}
