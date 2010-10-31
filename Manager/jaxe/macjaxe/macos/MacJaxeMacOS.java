/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.macjaxe.macos;

import org.apache.log4j.Logger;

import jaxe.*;
import jaxe.macjaxe.*;

import java.awt.Image;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.*;

import com.apple.eawt.*; // java 1.4.1

//sur MacOS X, le chemin de l'archive pour quicktime est:
// /System/Library/Java/Extensions/QTJava.zip

// pour toutes versions de QuickTime :
import quicktime.QTSession;
import quicktime.QTException;
import quicktime.qd.Pict;
import quicktime.qd.QDRect;

// pour QuickTime 6.4+ :
import quicktime.app.view.GraphicsImporterDrawer;
import quicktime.std.image.GraphicsImporter;
import quicktime.std.StdQTConstants;
import quicktime.util.QTHandle;
import quicktime.app.view.QTImageProducer;

/**
 * Tout ce qui est spécifique à MacOS X: implémentation (retirer ce fichier de la compile 
 * pour les autres plates-formes).
 */
public class MacJaxeMacOS implements MacJaxe, ApplicationListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MacJaxeMacOS.class);

    public MacJaxeMacOS() {
        // java 1.4.1
        final Application app = new Application();
        app.setEnabledPreferencesMenu(true);
        app.addApplicationListener(this);
    }
    
    public void handleAbout() {
        Jaxe.aPropos();
    }
    
    public void handleQuit() {
        Jaxe.quitter();
    }
    
    public void handleOpenFile(final File f) {
        Jaxe.ouvrir(f, null);
    }
    
    public void handleOpenApplication() {
        // le nouveau fichier doit déjà être ouvert
    }
    
    public void handlePrefs() {
        Jaxe.preferences();
    }
    
    
    // java 1.4.1 (ApplicationListener)
    
    public void handleAbout(final ApplicationEvent event) {
        handleAbout();
        event.setHandled(true);
    }
    
    public void handleOpenApplication(final ApplicationEvent event) {
        handleOpenApplication();
    }
    
    public void handleReOpenApplication(final ApplicationEvent event) {
    }
    
    public void handleOpenFile(final ApplicationEvent event) {
        handleOpenFile(new File(event.getFilename()));
        event.setHandled(true);
    }
    
    public void handlePreferences(final ApplicationEvent event) {
        handlePrefs();
        event.setHandled(true);
    }
    
    public void handlePrintFile(final ApplicationEvent event) {
    }
    
    public void handleQuit(final ApplicationEvent event) {
        handleQuit();
    }
    
    
    // conversion d'images issues du presse-papier
    
    public static byte[] lireInputStream(final InputStream in) throws IOException {
        int lu = 0;
        final int buffsize = 1024;
        final byte[] buff = new byte[buffsize];
        int taillemax = buffsize*10;
        byte[] pictBytes = new byte[taillemax];
        int taille = 0;
        while (lu != -1) {
            lu = in.read(buff);
            if (lu != -1) {
                System.arraycopy(buff, 0, pictBytes, taille, lu);
                taille += lu;
                if (taille + buffsize > taillemax) {
                    final byte[] newPictBytes = new byte[taillemax + buffsize*10];
                    System.arraycopy(pictBytes, 0, newPictBytes, 0, taille);
                    pictBytes = newPictBytes;
                    taillemax += buffsize*10;
                }
            }
        }
        if (taille < taillemax) {
            final byte[] newPictBytes = new byte[taille];
            System.arraycopy(pictBytes, 0, newPictBytes, 0, taille);
            pictBytes = newPictBytes;
            taillemax = taille;
        }
        return pictBytes;
    }
    
    public Image convertirPICT(final InputStream in) {
        try {
            final byte[] pictBytes = lireInputStream(in);
            QTSession.open();
            final Pict laphoto = new Pict(pictBytes);
            final Image img = pictToImage(laphoto);
            QTSession.close();
            return img;
        } catch (final Exception ex) {
            LOG.error("convertirPICT(InputStream)", ex);
        }
        return null;
    }

    protected Image pictToImage(final Pict cur_pict) throws QTException {
        // QT 6.4+
        // obsolète avec MacOS X 10.6, ne marche qu'en 32 bits (QTJava est obsolète)
        
        final QTHandle pictHeader = new QTHandle( 512, true ); //add a header to make it look like a file instead of a handler
        pictHeader.concatenate( cur_pict ); //add the actual information
        final GraphicsImporter grip = new GraphicsImporter( StdQTConstants.kQTFileTypePicture );
        grip.setDataHandle( pictHeader );
        final GraphicsImporterDrawer gid = new GraphicsImporterDrawer(grip);
        final QDRect image_rect = gid.getDisplayBounds();
        
        final Dimension d = new Dimension(image_rect.getWidth(), image_rect.getHeight());
        final QTImageProducer qt_image_producer = new QTImageProducer(gid, d);
        final Image cur_image = Toolkit.getDefaultToolkit().createImage(qt_image_producer);
        
        return cur_image;
    }
}
