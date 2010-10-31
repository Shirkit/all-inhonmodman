/*
Jaxe - Editeur XML en Java

Copyright (C) 2008 Observatoire de Paris

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.FileDialog;
import java.io.File;
import java.awt.FileDialog;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.sun.org.apache.xml.internal.utils.SAXSourceLocator;
import com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;

import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;


/**
 * Export XML ou HTML avec demande du nom de fichier en sortie
 */
public class Export {
    private static final Logger LOG = Logger.getLogger(Export.class);
    
    private final JaxeDocument doc;
    private final Element refExport;
    
    
    public Export(final JaxeDocument doc, final Element refExport) {
        this.doc = doc;
        this.refExport = refExport;
    }
    
    public void transformation() {
        if (doc.fsave == null) {
            JOptionPane.showMessageDialog(doc.jframe,
                JaxeResourceBundle.getRB().getString("html.SauverAvant"),
                JaxeResourceBundle.getRB().getString("erreur.Erreur"),
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        dialogueEnregistrement();
    }
    
    private void dialogueEnregistrement() {
        if (System.getProperty("os.name").startsWith("Mac OS") && doc.jframe instanceof JaxeFrame) // pour éviter un bug avec FileDialog et cmd-v
            ((JaxeFrame)doc.jframe).getJaxeMenuBar().setEnabled(false);
        FileDialog fd = new FileDialog(doc.jframe, null, FileDialog.SAVE);
        fd.setVisible(true);
        if (System.getProperty("os.name").startsWith("Mac OS") && doc.jframe instanceof JaxeFrame)
            ((JaxeFrame)doc.jframe).getJaxeMenuBar().setEnabled(true);
        final String sf = fd.getFile();
        if (sf != null) {
            File f = new File(fd.getDirectory(), sf);
            if (f.getName().indexOf('.') == -1) {
                final String extension = doc.cfg.sortieExport(refExport).toLowerCase();
                f = new File(f.getPath() + "." + extension);
                if (f.exists()) {
                    if (JOptionPane.showConfirmDialog(doc.jframe,
                        JaxeResourceBundle.getRB().getString("enregistrement.remplacer"), "",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                            fd = null;
                            dialogueEnregistrement();
                            return;
                        }
                }
            }
            transformation(f);
        }
    }
    
    /**
     * Lance la transformation XSLT, sans l'ouverture du fichier final à la fin.
     */
    public void transformation(final File fichierFinal) {
        transformation(fichierFinal, false);
    }
    
    /**
     * Transformation XSLT
     * @param fichierFinal fichier résultat de la transformation, ou de la suite de transformations XSLT.
     * @param ouvertureNavigateur  ouvre le navigateur avec le fichier résultat à la fin de la transformation.
     */
    public void transformation(final File fichierFinal, final boolean ouvertureNavigateur) {
        ThreadTransformation th = new ThreadTransformation(fichierFinal, ouvertureNavigateur);
        th.start();
    }
    
    class ThreadTransformation extends Thread {
        private File fichierFinal;
        private boolean ouvertureNavigateur;
        ThreadTransformation(final File fichierFinal, final boolean ouvertureNavigateur) {
            this.fichierFinal = fichierFinal;
            this.ouvertureNavigateur = ouvertureNavigateur;
        }
        @Override
        public void run() {
            transformation2(fichierFinal, ouvertureNavigateur);
        }
    }
    
    private void transformation2(final File fichierFinal, final boolean ouvertureNavigateur) {
        org.w3c.dom.Document XMLdoc = doc.DOMdoc;
        File outFile = null;
        // les feuilles de styles sont utilisées les unes à la suite des autres
        final ArrayList<File> listeFichiers = doc.cfg.listeFichiersExport(refExport) ;
        final DialogueAttente attente;
        if (listeFichiers.size() > 1)
            attente = new DialogueAttente(doc.jframe, doc.cfg.titreExport(refExport), 0, listeFichiers.size());
        else
            attente = new DialogueAttente(doc.jframe, doc.cfg.titreExport(refExport));
        attente.setVisible(true);
        try {
            for (int i = 0; i < listeFichiers.size(); i++) {
                final boolean premierFichier = (i == 0);
                final boolean dernierFichier = (i == listeFichiers.size() - 1);
                final File xslFile = listeFichiers.get(i);
                if (xslFile == null) {
                    JOptionPane.showMessageDialog(doc.jframe, JaxeResourceBundle.getRB().getString("erreur.XSLNonTrouve"),
                        JaxeResourceBundle.getRB().getString("erreur.Erreur"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    // pour les classes dans le dossier des plugins, qui peuvent être utilisées comme extensions,
                    // on utilise le ClassLoader avec les plugins
                    if (Thread.currentThread().getContextClassLoader() != JEFactory.getPluginClassLoader())
                        Thread.currentThread().setContextClassLoader(JEFactory.getPluginClassLoader());
                    
                    final Properties prefs = Preferences.getPref();
                    final String classeXSLT = (prefs == null ? null : prefs.getProperty("classeXSLT"));
                    if (classeXSLT != null && !"".equals(classeXSLT)) {
                        try {
                            System.setProperty("javax.xml.transform.TransformerFactory", classeXSLT);
                        } catch (Exception ex) {
                            LOG.error("System.setProperty javax.xml.transform.TransformerFactory " + classeXSLT, ex);
                        }
                    }
                    final TransformerFactory tFactory = TransformerFactory.newInstance() ;
                    tFactory.setErrorListener(new MyErrorListener());
                    tFactory.setURIResolver(Jaxe.getURIResolver());
                    final Transformer transformer = tFactory.newTransformer(new StreamSource(xslFile)) ;
                    final Map<String, String> parametres = doc.cfg.getXSLParam(xslFile) ;
                    for (final Entry<String, String> parametre : parametres.entrySet()) {
                        if ("jaxe-fichier-xml".equals(parametre.getKey()))
                            transformer.setParameter("jaxe-fichier-xml", doc.fsave.getAbsolutePath());
                        else if ("jaxe-uri-xml".equals(parametre.getKey()))
                            transformer.setParameter("jaxe-uri-xml", doc.fsave.toURI().toASCIIString());
                        else if ("jaxe-fichier-xsl".equals(parametre.getKey()))
                            transformer.setParameter("jaxe-fichier-xsl", xslFile.getAbsolutePath());
                        else if ("jaxe-uri-xsl".equals(parametre.getKey()))
                            transformer.setParameter("jaxe-uri-xsl", xslFile.toURI().toASCIIString());
                        else if ("jaxe-fichier-destination".equals(parametre.getKey()))
                            transformer.setParameter("jaxe-fichier-destination", fichierFinal.getAbsolutePath());
                        else if ("jaxe-uri-destination".equals(parametre.getKey()))
                            transformer.setParameter("jaxe-uri-destination", fichierFinal.toURI().toASCIIString());
                        else
                            transformer.setParameter(parametre.getKey(),parametre.getValue()) ;
                    }
                    if (dernierFichier)
                        outFile = fichierFinal;
                    else {
                        outFile = java.io.File.createTempFile("tmp",".xml") ;
                        outFile.deleteOnExit() ;
                    }
                    if (premierFichier && (doc.cfg.getPublicId() != null || doc.cfg.getSystemId() != null)) {
                        // il risque de manquer le doctype et les entités de la DTD : on est obligé de reparser le fichier :-(
                        // (c'est le cas avec DITA)
                        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setNamespaceAware(true);
                        final DocumentBuilder docbuilder = dbf.newDocumentBuilder();
                        docbuilder.setEntityResolver(Jaxe.getEntityResolver());
                        XMLdoc = docbuilder.parse(new InputSource(doc.getReader()));
                    }
                    final DOMSource ds = new DOMSource(XMLdoc);
                    if (doc.fsave != null)
                        ds.setSystemId(doc.fsave.toURI().toURL().toExternalForm());
                    final FileOutputStream fos = new FileOutputStream(outFile);
                    Result res;
                    if (dernierFichier && "PDF".equals(doc.cfg.sortieExport(refExport))) {
                        final FopFactory fopFactory = FopFactory.newInstance();
                        Fop fop;
                        if (doc.fsave != null) {
                            final FOUserAgent userAgent = fopFactory.newFOUserAgent();
                            userAgent.setBaseURL(doc.fsave.toURI().toURL().toExternalForm());
                            fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, fos);
                        } else
                            fop = fopFactory.newFop(MimeConstants.MIME_PDF, fos);
                        res = new SAXResult(fop.getDefaultHandler());
                    } else
                        res = new StreamResult(fos);
                    transformer.transform(ds, res);
                    fos.close();
                } catch (final Exception ex) {
                    afficherErreur(ex);
                    return;
                }
    
                if (!dernierFichier) {
                    try {
                        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setNamespaceAware(true);
                        final DocumentBuilder parser = dbf.newDocumentBuilder();
                        XMLdoc = parser.parse(outFile);
                    } catch (final Exception e) {
                        LOG.error("miseAJour()", e);
                        return;
                    }
                }
                if (listeFichiers.size() > 1)
                    attente.setProgress(i+1);
            }
        } finally {
            attente.setVisible(false);
            if (ouvertureNavigateur)
                lancerNavigateur(outFile);
        }
    }
    
    /**
     * nom du fichier HTML pour un fichier XML donné (utilisé par JaxeMenuBar)
     */
    public static File fichierHTML(final File fichierXML) {
        String nomFsave = fichierXML.getName();
        final int ie = nomFsave.lastIndexOf('.');
        if (ie != -1) {
            if (!"html".equals(nomFsave.substring(ie+1)))
                nomFsave = nomFsave.substring(0, ie);
//            else
                // pour terminer en .html.html
        }
        return(new File(fichierXML.getParent() + File.separatorChar + nomFsave + ".html"));
    }
    
    public void lancerNavigateur(final File htmlFile) {
        Properties prefs = Preferences.getPref();
        String cheminNav = prefs.getProperty("navigateur");
        if (cheminNav == null || "".equals(cheminNav)) {
            defNavigateur();
            prefs = Preferences.getPref();
            cheminNav = prefs.getProperty("navigateur");
            if (cheminNav == null)
                return;
        }
        String[] acmd;
        if (System.getProperty("os.name").startsWith("Mac")) {
            acmd = new String[4];
            acmd[0] = "/usr/bin/open";
            acmd[1] = "-a";
            acmd[2] = cheminNav;
            acmd[3] = htmlFile.getAbsolutePath();
        } else {
            acmd = new String[2];
            acmd[0] = cheminNav;
            acmd[1] = htmlFile.getAbsolutePath();
        }
        try {
            Runtime.getRuntime().exec(acmd);
        } catch (final IOException ex) {
            LOG.error("Export.lancerNavigateur", ex);
        }
    }
    
    public void defNavigateur() {
        String chemin = null;
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(JaxeResourceBundle.getRB().getString("html.DefNavigateur"));
            final int resultat = chooser.showOpenDialog(doc.jframe);
            if (resultat == JFileChooser.APPROVE_OPTION) {
                final File f = chooser.getSelectedFile();
                chemin = f.getAbsolutePath();
            }
        } else {
            final FileDialog fdlg = new FileDialog(doc.jframe,
                JaxeResourceBundle.getRB().getString("html.DefNavigateur"), FileDialog.LOAD);
            fdlg.setVisible(true);
            String dir = fdlg.getDirectory();
            if (dir != null && dir.endsWith(File.separator))
                dir = dir.substring(0, dir.length()-1);
            final String nom = fdlg.getFile();
            if (dir == null)
                chemin = nom;
            else if (nom != null)
                chemin = dir + File.separator + nom;
        }
        if (chemin != null) {
            final Properties prefs = Preferences.getPref();
            prefs.setProperty("navigateur", chemin);
            Preferences.enregistrerPref(prefs);
        }
    }
    
    private void afficherErreur(final Exception ex) {
        String msg = ex.getMessage();
        if (ex instanceof TransformerException) {
            SourceLocator loc = null;
            Throwable cause = ex;
            do {
                if (cause instanceof SAXParseException)
                    loc = new SAXSourceLocator((SAXParseException)cause);
                else if (cause instanceof TransformerException) {
                    final SourceLocator loc2 = ((TransformerException)cause).getLocator();
                    if (loc2 != null)
                        loc = loc2;
                }
                if (cause instanceof TransformerException)
                    cause = ((TransformerException)cause).getCause();
                else if (cause instanceof WrappedRuntimeException)
                    cause = ((WrappedRuntimeException)cause).getException();
                else if (cause instanceof SAXException)
                    cause = ((SAXException)cause).getException();
                else
                    cause = null;
            } while (cause != null);
            if (loc != null)
                msg += " at line " + loc.getLineNumber();
        }
        if (msg != null) {
            int ic = 0;
            for (int i=0; i<msg.length(); i++)
                if (i-ic > 40 && msg.charAt(i)==' ') {
                    ic = i;
                    msg = msg.substring(0,i) + "\n" + msg.substring(i+1);
                }
        }
        JOptionPane.showMessageDialog(doc.jframe,
            ex.getClass().getName() + ": " + msg,
            JaxeResourceBundle.getRB().getString("erreur.Erreur"),
            JOptionPane.ERROR_MESSAGE);
    }
    
    class MyErrorListener implements ErrorListener {
        public void warning(final TransformerException ex) throws TransformerException {
            LOG.warn("XSLT warning", ex);
        }
        
        public void error(final TransformerException ex) throws TransformerException {
            LOG.error("XSLT error", ex);
            afficherErreur(ex); // sinon on ne la voit pas
            throw(ex);
        }
        
        public void fatalError(final TransformerException ex) throws TransformerException {
            LOG.error("XSLT fatal", ex);
            throw(ex);
        }
    }
}
