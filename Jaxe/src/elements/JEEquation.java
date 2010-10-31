/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.elements;

import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.text.Position;

import jaxe.JaxeDocument;
import jaxe.JaxeResourceBundle;
import jaxe.equations.DialogueEquation;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Equation
 * Type d'élément Jaxe: 'equation'
 * paramètre: texteAtt: le nom de l'attribut donnant le texte de l'équation
 * paramètre: srcAtt: le nom de l'attribut donnant le nom de l'image
 * paramètre: labelAtt: le nom de l'attribut donnant le label de l'image
 */
public class JEEquation extends JEFichier {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JEEquation.class);

    public final static String defTexteAtt = "texte";
    
    public JEEquation(final JaxeDocument doc) {
        super(doc);
        alignementY = (float)0.70;
        // 70% du composant au-dessus de la base de la ligne
        // donc pas parfait (il faudrait se placer par rapport au milieu vertical d'un 'x'
        // et non par rapport à la base de la ligne pour obtenir un résulat parfait,
        // mais ce n'est pas possible avec Swing)
    }

    @Override
    public void init(final Position pos, final Node noeud) {
        // l'image est créée si elle n'existe pas
        // (elle peut avoir été effacée lors d'un couper-coller)
        final Element el = (Element)noeud;
        File fimg;
        srcAttr = doc.cfg.valeurParametreElement(refElement, "srcAtt", defaultSrcAttr);
        final String nomf = el.getAttribute(srcAttr);
        if (doc.fsave == null)
            fimg = new File(nomf);
        else
            fimg = new File(doc.fsave.getParent() + File.separatorChar + nomf);
        if (fimg.exists()) {
            // le cas où deux équations utilisent le même fichier image peut se produire
            // après un copier-coller
            // il faut donc vérifier toutes les équations du fichier XML
            // et recréer l'image si nécessaire
            NodeList l;
            if (noeud.getNamespaceURI() == null)
                l = doc.DOMdoc.getElementsByTagName(noeud.getNodeName());
            else
                l = doc.DOMdoc.getElementsByTagNameNS(noeud.getNamespaceURI(), noeud.getLocalName());
            for (int i=0; i<l.getLength(); i++)
                if (l.item(i) != noeud)
                    if (nomf.equals(((Element)l.item(i)).getAttribute(srcAttr))) {
                        fimg = null;
                        break;
                    }
        }
        if (fimg == null || !fimg.exists()) {
            final String texteAtt = doc.cfg.valeurParametreElement(refElement, "texteAtt", defTexteAtt);
            final String texteEquation = el.getAttribute(texteAtt);
            String nomImage;
            final BufferedImage img = DialogueEquation.creerImage(texteEquation);
            if (fimg == null)
                nomImage = enregistrerImage(img, null);
            else
                nomImage = enregistrerImage(img, fimg.getPath());
            el.setAttributeNS(doc.cfg.espaceAttribut(srcAttr), srcAttr, nomImage);
            doc.setModif(true); // bug: sera ignoré à l'ouverture d'un fichier
        }
        super.init(pos, noeud);
    }
    
    @Override
    public Node nouvelElement(final Element refElement) {
        if (doc.fsave == null) {
            JOptionPane.showMessageDialog(doc.jframe,
                JaxeResourceBundle.getRB().getString("equation.SauverAvant"),
                JaxeResourceBundle.getRB().getString("erreur.Erreur"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        srcAttr = doc.cfg.valeurParametreElement(refElement, "srcAtt", defaultSrcAttr);
        final String texteAtt = doc.cfg.valeurParametreElement(refElement, "texteAtt", defTexteAtt);
        final String labelAtt = doc.cfg.valeurParametreElement(refElement, "labelAtt", null);
        
        final Element newel = nouvelElementDOM(doc, refElement);
        if (newel == null) return null;
        
        final DialogueEquation dlg = new DialogueEquation(doc, "", labelAtt, null);
        if (!dlg.afficher())
            return null;
        final String texte = dlg.getTexte();
        final String nomImage = enregistrerImage(DialogueEquation.creerImage(texte), null);
        final String valeurLabel = dlg.getLabel();
        
        try {
            newel.setAttributeNS(doc.cfg.espaceAttribut(texteAtt), texteAtt, texte);
            newel.setAttributeNS(doc.cfg.espaceAttribut(srcAttr), srcAttr, nomImage);
            if (labelAtt != null && valeurLabel != null && !"".equals(valeurLabel))
                newel.setAttributeNS(doc.cfg.espaceAttribut(labelAtt), labelAtt, valeurLabel);
        } catch (final DOMException ex) {
            LOG.error("nouvelElement(Element) - DOMException", ex);
            return null;
        }

        return newel;
    }
    
    @Override
    public void afficherDialogue(final JFrame jframe) {
        final Element el = (Element)noeud;

        final String texteAtt = doc.cfg.valeurParametreElement(refElement, "texteAtt", defTexteAtt);
        final String labelAtt = doc.cfg.valeurParametreElement(refElement, "labelAtt", null);
        String texteEquation = el.getAttribute(texteAtt);
        String nomImage = el.getAttribute(srcAttr);
        String valeurLabel1;
        if (labelAtt != null)
            valeurLabel1 = el.getAttribute(labelAtt);
        else
            valeurLabel1 = null;
        final DialogueEquation dlg = new DialogueEquation(doc, texteEquation, labelAtt, valeurLabel1);
        if (!dlg.afficher())
            return;
        texteEquation = dlg.getTexte();
        nomImage = enregistrerImage(DialogueEquation.creerImage(texteEquation), nomImage);
        final String valeurLabel2 = dlg.getLabel();
        
        try {
            el.setAttributeNS(doc.cfg.espaceAttribut(texteAtt), texteAtt, texteEquation);
            el.setAttributeNS(doc.cfg.espaceAttribut(srcAttr), srcAttr, nomImage);
            if (labelAtt != null) {
                if (valeurLabel1 != null && "".equals(valeurLabel2))
                    el.removeAttribute(labelAtt);
                else
                    el.setAttributeNS(doc.cfg.espaceAttribut(labelAtt), labelAtt, valeurLabel2);
            }
        } catch (final DOMException ex) {
            LOG.error("afficherDialogue(JFrame) - DOMException", ex);
            return;
        }
        doc.setModif(true);
        
        majAffichage();
    }
    
    @Override
    public void effacer() {
        File fimg;
        final String nomf = ((Element)noeud).getAttribute(srcAttr);
        if (doc.fsave == null)
            fimg = new File(nomf);
        else
            fimg = new File(doc.fsave.getParent() + File.separatorChar + nomf);
        if (fimg.exists() && fimg.isFile())
            fimg.delete();
    }
    
    /**
     * Enregistrement de l'image de l'équation dans un fichier PNG, dans un dossier "equations_[nom du fichier XML]"
     * placé au même endroit que le fichier XML.
     * Renvoit le chemin vers le fichier de l'image (relatif au fichier XML).
     */
    protected String enregistrerImage(final BufferedImage img, final String nomImage) {
        // effacement de la précédente
        if (nomImage != null && !"".equals(nomImage)) {
            File fimg;
            if (doc.fsave == null)
                fimg = new File(nomImage);
            else
                fimg = new File(doc.fsave.getParent() + File.separatorChar + nomImage);
            if (fimg.exists() && fimg.isFile())
                fimg.delete();
        }
        
        // recherche d'un nouveau nom
        if (doc.fsave == null)
            return null; // on pourrait afficher un message d'erreur
        File imgFile = null;
        final String baseNom = "equation";
        String nouveauNom = null;
        String nomfxml = doc.fsave.getName();
        if (nomfxml.indexOf('.') != -1)
            nomfxml = nomfxml.substring(0, nomfxml.lastIndexOf('.'));
        final String dossierEquations = "equations_" + nomfxml;
        final File dossier = new File(doc.fsave.getParent() + File.separator + dossierEquations);
        if (!dossier.exists())
            if (!dossier.mkdir()) {
                LOG.error("enregistrerImage(BufferedImage, String) - Erreur à la création du dossier des équations");
                return null;
            }
        int i = 1;
        while (imgFile == null || imgFile.exists()) {
            nouveauNom = baseNom + i + ".png";
            imgFile = new File(dossier.getPath() + File.separator + nouveauNom);
            i++;
        }
        // enregistrement
        try {
            DialogueEquation.enregistrerImage(img, imgFile);
        } catch (final IOException ex) {
            LOG.error("enregistrerImage(BufferedImage, String)", ex);
            JOptionPane.showMessageDialog(doc.jframe, JaxeResourceBundle.getRB().getString("erreur.Enregistrement") + ": " +
                ex.getMessage(), JaxeResourceBundle.getRB().getString("erreur.Erreur"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return(dossierEquations + "/" + nouveauNom);
    }
    
}
