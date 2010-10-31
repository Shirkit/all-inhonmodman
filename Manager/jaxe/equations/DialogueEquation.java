/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jaxe.JaxeDocument;
import jaxe.JaxeResourceBundle;


public class DialogueEquation extends JDialog implements ActionListener, DocumentListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DialogueEquation.class);

    JFrame jframe;
    boolean valide = false;
    MathComponent mathcomp;
    JTextArea zoneTexte;
    String texteEquation;
    JPanel epane;
    JaxeDocument doc;
    JTextField labelfield = null;
    String valeurLabel;

    public DialogueEquation(final JaxeDocument doc, final String texteEquation) {
        this(doc, texteEquation, null, null);
    }
    
    public DialogueEquation(final JaxeDocument doc, final String texteEquation,
            final String nomlabel, final String valeurLabel) {
        super(doc.jframe, JaxeResourceBundle.getRB().getString("equation.Equation"), true);
        this.doc = doc;
        jframe = doc.jframe;
        this.texteEquation = texteEquation;
        this.valeurLabel = valeurLabel;
        
        final JPanel cpane = new JPanel(new BorderLayout());
        setContentPane(cpane);
        epane = new JPanel(new BorderLayout());
        mathcomp = new MathComponent(texteEquation);
        epane.add(mathcomp, BorderLayout.CENTER);
        zoneTexte = new JTextArea(texteEquation, 2, 80);
        zoneTexte.setLineWrap(true);
        zoneTexte.setWrapStyleWord(true);
        zoneTexte.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        zoneTexte.getDocument().addDocumentListener(this);
        epane.add(zoneTexte, BorderLayout.SOUTH);
        cpane.add(epane, BorderLayout.CENTER);
        
        final JPanel southpane = new JPanel(new GridLayout((nomlabel==null)?1:2, 1));
        
        if (nomlabel != null) {
            final JPanel lpane = new JPanel(new FlowLayout(FlowLayout.LEFT));
            lpane.add(new JLabel(nomlabel));
            labelfield = new JTextField(valeurLabel, 30);
            lpane.add(labelfield);
            southpane.add(lpane);
        }
        
        final JPanel bpane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton boutonAnnuler = new JButton(JaxeResourceBundle.getRB().getString("bouton.Annuler"));
        boutonAnnuler.addActionListener(this);
        boutonAnnuler.setActionCommand("Annuler");
        bpane.add(boutonAnnuler);
        final JButton boutonOK = new JButton(JaxeResourceBundle.getRB().getString("bouton.OK"));
        boutonOK.addActionListener(this);
        boutonOK.setActionCommand("OK");
        bpane.add(boutonOK);
        southpane.add(bpane);
        
        cpane.add(southpane, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(boutonOK);
        setSize(new Dimension(500, 300));
        zoneTexte.requestFocus();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(final WindowEvent we) {
                javax.swing.SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        zoneTexte.requestFocus();
                    }
                } );
            }
        });
        final Rectangle r = jframe.getBounds();
        setLocation(r.x + r.width/4, r.y + r.height/4);
    }

    public boolean afficher() {
        setVisible(true);
        return valide;
    }

    public String getTexte() {
        return texteEquation;
    }
    
    public String getLabel() {
        return valeurLabel;
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("OK".equals(cmd))
            actionOK();
        else if ("Annuler".equals(cmd)) {
            valide = false;
            setVisible(false);
        }
    }
    
    protected void actionOK() {
        valide = true;
        if (labelfield != null)
            valeurLabel = labelfield.getText();
        setVisible(false);
    }
    
    protected void changementTexte() {
        if (zoneTexte.getText().indexOf('\n') != -1)
            actionOK();
        else {
            texteEquation = zoneTexte.getText();
            majAffichage();
        }
    }
    
    public void insertUpdate(final DocumentEvent e) {
        changementTexte();
    }
    
    public void removeUpdate(final DocumentEvent e) {
        changementTexte();
    }
    
    public void changedUpdate(final DocumentEvent e) {
        changementTexte();
    }
    
    protected void majAffichage() {
        mathcomp.setEquationString(texteEquation);
        mathcomp.repaint();
    }
    
    /**
     * Création de l'image correspondant au texte de l'équation
     */
    public static BufferedImage creerImage(final String texteEquation) {
        // reconstruction de l'image
        final BufferedImage imgcalc = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D gcalc = imgcalc.createGraphics();
        // on est obligé de créer un Graphics bidon pour calculer la taille de l'image depuis que la méthode
        // Toolkit.getFontMetrics(Font) est dépréciée
        final MathBase base = new MathBase((new StringMathBuilder(texteEquation)).getMathRootElement(), gcalc);
        final Dimension dim = new Dimension(base.getWidth(), base.getHeight());
        
        // IE ne gère pas les PNG avec une palette -> utilisation d'une échelle de gris avec transparence
        final ColorSpace grayColorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        final ColorModel cm = new ComponentColorModel(grayColorSpace, true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        final WritableRaster raster = cm.createCompatibleWritableRaster(dim.width, dim.height);
        final BufferedImage img2 = new BufferedImage(cm, raster, false, null);
        final Graphics2D g2 = img2.createGraphics();
        g2.setColor(Color.black);
        base.paint(g2);
        
        return img2;
    }
    
    /**
     * Enregistrement de l'image de l'équation vers une sortie donnée en paramètre.
     * Le paramètre sortie peut être de la classe File (pour enregistrer dans un fichier) ou OutputStream.
     * Les images peuvent être utilisées dans IE6 (PNG avec une échelle de gris et fond blanc, couche alpha
     * utilisable par les autres navigateurs).
     */
    public static void enregistrerImage(final BufferedImage img, final Object sortie) throws IOException {
        // IE met un fond gris si aucun bKGD n'est spécifié -> ajout de bKGD dans les métadonnées
        final ImageWriter premierPourPNG = ImageIO.getImageWritersByFormatName("PNG").next();
        final IIOMetadata metadata = premierPourPNG.getDefaultImageMetadata(new ImageTypeSpecifier(img), null);
        final Element racine = (Element)metadata.getAsTree("javax_imageio_png_1.0");
        final NodeList nl = racine.getElementsByTagName("bKGD");
        if (nl.getLength() == 0) {
            Node suivant = racine.getFirstChild();
            final String name = suivant.getNodeName();
            if (("IHDR".equals(name) || "PLTE".equals(name)))
                suivant = suivant.getNextSibling();
            if (suivant != null && "PLTE".equals(suivant.getNodeName()))
                suivant = suivant.getNextSibling();
            final Element bKGD = new IIOMetadataNode("bKGD");
            if (suivant == null)
                racine.appendChild(bKGD);
            else
                racine.insertBefore(bKGD, suivant);
            final Element bKGD_Grayscale = new IIOMetadataNode("bKGD_Grayscale");
            bKGD.appendChild(bKGD_Grayscale);
            bKGD_Grayscale.setAttribute("gray", "255");
            try {
                metadata.mergeTree("javax_imageio_png_1.0", racine);
            } catch (final IIOInvalidTreeException ex) {
                LOG.error("enregistrerImage(BufferedImage, Object)", ex);
            }
        }
        // enregistrement (peut lancer une IOException)
        premierPourPNG.setOutput(ImageIO.createImageOutputStream(sortie));
        premierPourPNG.write(new IIOImage(img, null, metadata));
    }
}
