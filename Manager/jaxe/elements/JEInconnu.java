/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.text.Position;

import jaxe.JaxeDocument;
import jaxe.JaxeResourceBundle;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * El�ment dont la d�finition est inconnue
 */
public class JEInconnu extends JEString {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEInconnu.class);

    public JEInconnu(final JaxeDocument doc) {
        super(doc);
    }
    
    @Override
    public void init(final Position pos, final Node noeud) {
        super.init(pos, noeud);
        if (doc.cfg != null && refElement == null) {
            lstart.setValidite(false);
            lend.setValidite(false);
        }
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        // ajouter dialogue pour le nom de l'�l�ment et les attributs
        final String nom = JOptionPane.showInputDialog(doc.jframe,
            JaxeResourceBundle.getRB().getString("inconnu.NomElement"),
            JaxeResourceBundle.getRB().getString("inconnu.NouvelleBalise"), JOptionPane.QUESTION_MESSAGE);

        //final Node newel = nouvelElementDOM(doc, "element", nom); (deprecated method)
        final Node newel = doc.DOMdoc.createElementNS(null, nom);
        
        return newel;
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
        final Element el = (Element)noeud;
        
        final Vector<String> data = new Vector<String>();
        final NamedNodeMap attmap = el.getAttributes();
        for (int i=0; i<attmap.getLength(); i++) {
            final Node attn = attmap.item(i);
            final String name = attn.getNodeName();
            final String val = attn.getNodeValue();
            data.add(name + "=" + val);
        }
        final DialogueInconnu dlg = new DialogueInconnu(doc.jframe, doc,
            getString("inconnu.Balise") + " " + el.getTagName(), data, el);
        if (!dlg.afficher())
            return;
        dlg.enregistrerReponses();
        majAffichage();
        doc.textPane.miseAJourArbre();
    }
    
    @Override
    public void majAffichage() {
    }
    
    @Override
    public void majValidite() {
    }
}

