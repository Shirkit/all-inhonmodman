/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jaxe.elements.JECommentaire;
import jaxe.elements.JECData;
import jaxe.elements.JESwing;
import jaxe.elements.JETexte;

import org.w3c.dom.Element;

/**
 * Creates a Panel that shows the Attributes of the Element
 * @author tasche
 */
public class AttributePanel extends JPanel implements EcouteurMAJ, CaretListener {
    /**
     * Logger for this class
     */
    static final Logger LOG = Logger.getLogger(AttributePanel.class);

    /** The JaxeDocument for this Panel */
    JaxeDocument _doc;
    /** The current Element */
    private JaxeElement _elem;

    /**
     * Creates the JPanel
     * @param doc the Document for this Panel
     */
    public AttributePanel(final JaxeDocument doc) {
        _doc = doc;
        miseAJour();
    }

    /**
     * Updates the Panel
     * @see jaxe.EcouteurMAJ#miseAJour()
     */
    public void miseAJour() {
        
        this.removeAll();
        this.setLayout(new BorderLayout());

        if (_doc.rootJE != null && _doc.cfg != null) {
            JPanel attribPanel = new JPanel();
            
            final int pos = _doc.textPane.getCaretPosition();
            _elem = _doc.rootJE.elementA(pos);
            
            if (_elem != null && !(_elem instanceof JECommentaire) && !(_elem instanceof JECData)) {
                if (_elem instanceof JETexte || (_elem.debut.getOffset() == pos &&
                        !(_elem instanceof JESwing)))
                    _elem = _elem.getParent();
                if (_elem != null)
                    attribPanel = createInputLists();
            }

            // The following lines are used to put the Buttons to the top of the Panel
            final JPanel tmp = new JPanel();
            tmp.setPreferredSize(new Dimension(0, 0));
            tmp.setMinimumSize(new Dimension(0, 0));

            final GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.NORTH;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.NONE;
            c.weighty = 1.0;

            attribPanel.add(tmp, c);

            final JScrollPane scroll = new JScrollPane(attribPanel);
            scroll.getVerticalScrollBar().setUnitIncrement(10);
            scroll.getHorizontalScrollBar().setUnitIncrement(10);
            this.add(scroll, BorderLayout.CENTER);

            validate();
        }
    }

    /**
     * Creates the Panel with Input-Elements
     * @param el the Element to be displayed
     * @return the Panel
     */
    private JPanel createInputLists() {
        final Element el = (Element)_elem.noeud;
        final Element ref = _elem.refElement;
        
        final JPanel attribPanel = new JPanel();
        attribPanel.setLayout(new GridBagLayout());

        if (ref != null) {
            final GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.NORTH;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0;

            final GridBagConstraints l = new GridBagConstraints();
            l.anchor = GridBagConstraints.CENTER;
            l.weightx = 0;
            l.fill = GridBagConstraints.HORIZONTAL;
            l.weighty = 0;
            l.insets = new Insets(0, 0, 0, 5);
            
            final Config conf = _doc.cfg.getRefConf(ref);
            final ArrayList<Element> attrlist = conf.listeAttributs(ref);
            for (final Element att : attrlist) {
                final String st = conf.nomAttribut(att);
                final String title = conf.titreAttribut(ref, att);
                final String ns = conf.espaceAttribut(att);
                String elval = el.getAttribute(st);
                
                String documentation = conf.documentationAttribut(ref, att);
                if (documentation != null) {
                    final JButton baide = new JButton(new ActionAide(att, ref));
                    baide.setFont(baide.getFont().deriveFont((float)9));
                    if (System.getProperty("os.name").startsWith("Mac OS")) {
                        baide.setText("?");
                        if ("10.5".compareTo(System.getProperty("os.version")) <= 0)
                            baide.putClientProperty("JButton.buttonType", "help");
                        else
                            baide.putClientProperty("JButton.buttonType", "toolbar");
                    } else {
                        baide.setIcon(new ImageIcon(ImageKeeper.loadImage("images/aide.png")));
                        baide.setMargin(new Insets(0, 0, 0, 0));
                        baide.setBorderPainted(false);
                        baide.setContentAreaFilled(false);
                    }
                    attribPanel.add( baide, l);
                    documentation = "<html><body>" + documentation.replaceAll("\n", "<br>") + "</body></html>";
                    baide.setToolTipText(documentation);
                }
                
                final JLabel label = new JLabel(title);
                if (conf.estObligatoire(att))
                    label.setForeground(new Color(150, 0, 0)); // rouge foncé
                else
                    label.setForeground(new Color(0, 100, 0)); // vert foncé
                
                attribPanel.add(label, l);                    
                
                final ArrayList<String> lval = conf.listeValeursAttribut(att);
                final String defaut = conf.valeurParDefaut(att);
                if ("".equals(elval) && defaut != null && el.getAttributeNode(st) == null)
                    elval = defaut;
                if (lval != null) {
                    final ElementComboBox popup = new ElementComboBox(el, st, ns);
                    if (!conf.estObligatoire(att))
                        popup.addValue("", "");
                    for (final String sval : lval)
                        popup.addValue(sval, conf.titreValeurAttribut(ref, att, sval));
                    popup.setValue(elval);
                    popup.startListener();
                    attribPanel.add(popup, c);
                } else {
                    final ArrayList<String> lvs = conf.listeValeursSuggereesAttribut(ref, att);
                    if (lvs != null && lvs.size() > 0) {
                        final ElementComboBox popup = new ElementComboBox(el, st, ns);
                        popup.setEditable(true);
                        if (!conf.estObligatoire(att))
                            popup.addValue("", "");
                        for (final String sval : lvs)
                            popup.addValue(sval, conf.titreValeurAttribut(ref, att, sval));
                        popup.setValue(elval);
                        popup.startListener();
                        attribPanel.add(popup, c);
                    } else {
                        final JTextField text = new JTextField(elval);
                        text.getDocument().addDocumentListener(new FieldListener(el, st, ns));
                        attribPanel.add(text, c);
                    }
                }

            }
        }
        return attribPanel;
    }

    /**
     * If the Carret was moved, update the component
     * @see javax.swing.event.CaretListener#caretUpdate(CaretEvent)
     */
    public void caretUpdate(final CaretEvent e) {
        final int pos = _doc.textPane.getCaretPosition();
        JaxeElement el = null;
        if (_doc.rootJE != null)
            el = _doc.rootJE.elementA(pos);
        if (el != null) {
            if (el instanceof JETexte || (el.debut.getOffset() == pos && !(el instanceof JESwing)))
                el = el.getParent();
        }
        if (el != _elem)
            miseAJour();
    }

    /**
     * A ComboBox that changes the Element if the selected Item is changed
     * @author tasche
     */
    class ElementComboBox extends JComboBox {
        
        /** The Element that is displayed */
        private final Element _el;
        /** The Attribute that is displayed */
        private final String _attr;
        /** The namespace for the attribute */
        private final String _ns;
        /** If true, it start listening to changes */
        private boolean _listen;
        /** list of possible values (as opposed to displayed titles) */
        private final ArrayList<String> _values;
        
        /**
         * Creates a ComboBox for an Attribute
         * @param el the shown Element
         * @param attr the shown Attribute
         */
        public ElementComboBox(final Element el, final String attr, final String ns) {
            _el = el;
            _attr = attr;
            _ns = ns;
            _listen = false;
            _values = new ArrayList<String>();
        }
    
        /**
         * Starts to listen to changes in the JComboBox
         * and updates the Element.
         */
        public void startListener() {
            _listen = true;
        }
    
        /**
         * If the selected Item is changed, the Attribute is 
         * updated
         */
        @Override
        public void selectedItemChanged() {
            super.selectedItemChanged();
            if (_listen) {
                final int index = getSelectedIndex();
                final String value;
                if (index == -1)
                    value = (String)getSelectedItem();
                else
                    value = _values.get(index);
                _el.setAttributeNS(_ns, _attr, value);
            }
        }
        
        public void addValue(final String value, final String title) {
            _values.add(value);
            addItem(title);
        }
        
        public void setValue(final String value) {
            final int index = _values.indexOf(value);
            if (index != -1)
                setSelectedIndex(index);
            else if (isEditable())
                setSelectedItem(value);
        }
    }
    
    /**
     * A Listener that changes the Attribute of an Element, 
     * if the Text is changed
     * @author tasche
     */
    class FieldListener implements DocumentListener {
        private final Element _el;
        private final String _attr;
        private final String _ns;
        
        /**
         * Creates a Listener for a Document
         * @param el the shown Element
         * @param attr the shown Attribute
         */
        public FieldListener (final Element el, final String attr, final String ns) {
            _el = el;
            _attr = attr;            
            _ns = ns;            
        }
        
        /**
         * Changes the Attribute if the Document was changed 
         * @param e the DocumentEvent
         */
        public void changed(final DocumentEvent e) {
            try {
                _el.setAttributeNS(_ns, _attr, e.getDocument().getText(0, e.getDocument().getLength()));
                final JaxeElement jel = _doc.getElementForNode(_el);
                if (jel != null)
                    jel.majAffichage();
            } catch (final Exception ex) {
                LOG.error("changed(DocumentEvent)", ex);
            }
        }

        /**
         * Changes the Attribute if the Document was changed 
         * @param e the DocumentEvent
         */
        public void changedUpdate(final DocumentEvent e) {
            changed(e);
        }
        
        /**
         * Changes the Attribute if the Document was changed 
         * @param e the DocumentEvent
         */
        public void insertUpdate(final DocumentEvent e){
            changed(e);
        }
        
        /**
         * Changes the Attribute if the Document was changed 
         * @param e the DocumentEvent
         */
        public void removeUpdate(final DocumentEvent e){
            changed(e);
        }
     }
    
    class ActionAide extends AbstractAction {
        Element refAttribut, refElementParent;
        ActionAide(final Element refAttribut, final Element refElementParent) {
            super();
            this.refAttribut = refAttribut;
            this.refElementParent = refElementParent;
        }
        public void actionPerformed(final ActionEvent e) {
            final DialogueAideElement dlg = new DialogueAideElement(refAttribut, refElementParent,
                _doc.cfg.getRefConf(refElementParent), (JFrame)_doc.textPane.getTopLevelAncestor());
            dlg.setVisible(true);
        }
    }
}
