/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/
package jaxe;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Le gestionnaire d'erreurs par défaut
 */
public class GestionErreurs implements InterfaceGestionErreurs {
    
    private static final Logger LOG = Logger.getLogger(GestionErreurs.class);

    private final JaxeDocument doc;

    public GestionErreurs(final JaxeDocument doc) {
        this.doc = doc;
    }

    public void pasSousLaRacine(final Element refElement) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(doc.textPane.jframe,
            JaxeResourceBundle.getRB().getString("insertion.SousRacine"),
            JaxeResourceBundle.getRB().getString("insertion.InsertionBalise"),
            JOptionPane.ERROR_MESSAGE);
    }

    public void editionInterdite(final JaxeElement parent, final Element refElement) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(doc.textPane.jframe,
            JaxeResourceBundle.getRB().getString("insertion.EditionInterdite") +
            " " + parent.noeud.getNodeName(),
            JaxeResourceBundle.getRB().getString("insertion.InsertionBalise"),
            JOptionPane.ERROR_MESSAGE);
    }
        
    public void enfantInterditSousParent(final JaxeElement parent, final Element refElement) {
        final Element refParent = parent.refElement;
        final ArrayList<String> autorisees = doc.cfg.nomsSousElements(refParent);
        final StringBuilder infos = new StringBuilder();
        infos.append(JaxeResourceBundle.getRB().getString("insertion.BalisesAutorisees"));
        infos.append(" ");
        infos.append(doc.cfg.nomElement(refParent));
        infos.append(":");
        infos.append(Jaxe.newline);
        final StringBuilder infos1 = new StringBuilder();
        int nbnl = 0;
        final Iterator<String> autIt = autorisees.iterator();
        while (autIt.hasNext()) {
            infos1.append(autIt.next());
            if (autIt.hasNext()) {
                infos1.append(", ");
            }
            if (nbnl < infos1.length()/80) {
                infos1.append(Jaxe.newline);
                nbnl++;
            }
        }
        infos.append(infos1);
        if (refElement != null) {
            infos.append(Jaxe.newline);
            infos.append(Jaxe.newline);
            infos.append(JaxeResourceBundle.getRB().getString("insertion.BalisesParents"));
            infos.append(" ");
            infos.append(doc.cfg.nomElement(refElement));
            infos.append(": ");
            infos.append(Jaxe.newline);
            final ArrayList<String> lparents = doc.cfg.nomsParents(refElement);
            final StringBuilder infos2 = new StringBuilder();
            nbnl = 0;
            final Iterator<String> parIt = lparents.iterator();
            while (parIt.hasNext()) {
                infos2.append(parIt.next());
                if (parIt.hasNext()) {
                    infos2.append(", ");
                }
                if (nbnl < infos2.length()/80) {
                    infos2.append(Jaxe.newline);
                    nbnl++;
                }
            }
            infos.append(infos2);
        }
        JOptionPane.showMessageDialog(doc.textPane.jframe, infos.toString(),
            JaxeResourceBundle.getRB().getString("insertion.InsertionBalise"), JOptionPane.ERROR_MESSAGE);
    }

    public void insertionImpossible(final String expr, final JaxeElement parent, final Element refElement) {
        final StringBuilder infos = new StringBuilder(); 
        infos.append(JaxeResourceBundle.getRB().getString("insertion.Expression"));
        infos.append(" ");
        infos.append(expr);
        
        if (infos.length() > 90) {
            int p=0;
            for (int i=0; i<infos.length(); i++) {
                if (i-p > 80 && (infos.charAt(i) == ' ' || infos.charAt(i) == '|')) {
                    infos.insert(i, "\n");
                    p = i;
                }
            }
        }
        JOptionPane.showMessageDialog(doc.textPane.jframe, infos.toString(),
            JaxeResourceBundle.getRB().getString("insertion.InsertionBalise"), JOptionPane.ERROR_MESSAGE);
    }

    public void texteInterdit(final JaxeElement parent) {
        final String infos = JaxeResourceBundle.getRB().getString("erreur.InsertionInterdite") + " " + parent.noeud.getNodeName();
        JOptionPane.showMessageDialog(doc.textPane.jframe, infos,
            JaxeResourceBundle.getRB().getString("document.Insertion"), JOptionPane.ERROR_MESSAGE);

    }

}
