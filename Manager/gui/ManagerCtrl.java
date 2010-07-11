
package gui;

import manager.Manager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import gui.l10n.L10n;
import business.ManagerOptions;
import business.Mod;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import utility.FileDrop;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Controller for the ManagerGUI. This is the 'controller' part of the MVC framework
 * used for handling UI in ModManager. Controller has access to both model and view
 * and is responsible for handling events coming from view and calling appropriate
 * implementations for these events.
 *
 * @author Kovo
 */
public class ManagerCtrl {
    Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
    Manager controller;
    ManagerOptions model;
    ManagerGUI view;
    ListSelectionListener lsl;
    private Preferences prefs;

    /**
     * Initialize event listeners.
     *
     * @param model model of the MVC framework
     * @param view view of the MVC framework
     */
    public ManagerCtrl(Manager controller, ManagerGUI view) {
    	this.model = ManagerOptions.getInstance();
        this.controller = controller;
        this.view = view;

        // Add listeners to view components
        view.buttonAddModAddActionListener(new AddModListener());
        view.buttonEnableModAddActionListener(new EnableModListener());
        view.itemApplyModsAddActionListener(new ApplyModsListener());
        view.itemApplyAndLaunchAddActionListener(new ApplyAndLaunchListener());
        view.itemUnapplyAllModsAddActionListener(new UnapplyAllModsListener());
        view.itemOpenModFolderAddActionListener(new OpenModFolderListener());
        view.itemVisitForumThreadAddActionListener(new VisitForumThreadListener());
        view.itemExitAddActionListener(new ExitListener());
        lsl = new ModTableSelectionListener(view.getModListTable());
        view.tableAddListSelectionListener(lsl);
        view.tableAddModelListener(new TableEditListener());
        view.labelVisitWebsiteAddMouseListener(new VisitWebsiteListener());
        view.buttonApplyLafAddActionListener(new ApplyLafListener());
        view.buttonOkAddActionListener(new PrefsOkListener());
        view.buttonCancelAddActionListener(new PrefsCancelListener());
        view.buttonHonFolderAddActionListener(new ChooseFolderHonListener());
        // Add file drop functionality
        new FileDrop(view, new DropListener());
        // Load mods from mods folder (if any)
        // TODO: shouldn't this be somewhere else?
        try {
        	controller.loadOptions();
            controller.loadMods();
        } catch (IOException ex) {
        	ex.printStackTrace();
            logger.error("Unable to load mods from mod folder. Message: "+ex.getMessage());
            view.showMessage("error.loadmodfiles", "error.loadmodfiles.title", JOptionPane.ERROR_MESSAGE);
        }
        view.tableRemoveListSelectionListener(lsl);
        model.updateNotify();
        view.tableAddListSelectionListener(lsl);
    }

    /**
     * Listener for detecting changes of selection in the mods table. This is
     * used when user selects a row in the table.
     */
    class ModTableSelectionListener implements ListSelectionListener {
        JTable table;

        ModTableSelectionListener(JTable table) {
            this.table = table;
        }

        /**
         * On selection change, display details of the newly selected mod
         */
        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == table.getSelectionModel()
                  && table.getRowSelectionAllowed()) {
                view.displayModDetail();
            }
            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            }
        }
    }

    /**
     * File filter for JFileChooser. Only displays files ending with
     * .honmod extension
     */
    class ModFilter extends FileFilter {  
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            int dotIndex = f.getName().lastIndexOf(".");
            if (dotIndex == -1) return false;
            String extension = f.getName().substring(dotIndex);
            if ((extension != null) && (extension.equals(".honmod"))) {
                return true;                               
            } else {
                return false;
            }
        }
        
        //The description of this filter
        public String getDescription() {
            return L10n.getString("chooser.filedescription");
        }
    }

    /**
     * Listener for 'Add mod' button. Opens JFileChooser and lets user select
     * one or more honmod files
     */
    class AddModListener implements ActionListener {
        File currentDir = new File(".");
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser(currentDir);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setMultiSelectionEnabled(true);
            ModFilter filter = new ModFilter();
            fc.setFileFilter(filter);
            int returnVal = fc.showOpenDialog(view);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles();
                for (int i=0; i<files.length; i++) {
                    logger.info("Opening mod file: " + files[i].getName());
                    try {
                        controller.addHonmod(files[i], true);
                        // Save directory for future use
                        this.currentDir = files[i].getParentFile();
                    } catch (IOException ioe) {
                        logger.error("Cannot open honmod file: "+ioe.getMessage());
                        ioe.printStackTrace();
                    }
                }
                // We need to save and restore ListSelectionListener since
                // updateNotify() updates the model of the table
                // TODO: Can this be fixed?
                view.tableRemoveListSelectionListener(lsl);
                model.updateNotify();
                view.tableAddListSelectionListener(lsl);
            }
        }
    }

    /**
     * Listener for changes in the leftmost column of the mods table containing
     * checkboxes for enabling/disabling mods
     */
    class TableEditListener implements TableModelListener {        
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if ((row == -1) || (column == -1)) return;
            TableModel tableModel = (TableModel)e.getSource();
            Object data = tableModel.getValueAt(row, column);
            if (data instanceof Boolean) {
                if ((Boolean)data){
                    logger.info("Mod at index "+row+" has been enabled");
                    controller.getMod(row).enable();
                } else {
                    logger.info("Mod at index "+row+" has been disabled");
                    controller.getMod(row).disable();
                }
                // Again, save and restore ListSelectionListener
                view.tableRemoveListSelectionListener(lsl);
                model.updateNotify();
                view.tableAddListSelectionListener(lsl);
            }
        }
    }

    /**
     * Listener for clicks on 'Visit website' label in mod details
     */
    class VisitWebsiteListener implements MouseListener {
        public void mouseClicked(MouseEvent me) {}
        public void mouseEntered(MouseEvent me) {}
        public void mouseExited(MouseEvent me) {}
        public void mouseReleased(MouseEvent me) {}
        public void mousePressed(MouseEvent me) {
            // This is fired when mouse is clicked and released
            int selectedMod = view.getSelectedMod();
            if (selectedMod == -1) {
                logger.warn("Unable to open website, no mod selected");
                view.showMessage(L10n.getString("error.nomodselected"),
                                 L10n.getString("error.nomodselected.title"),
                                 JOptionPane.WARNING_MESSAGE);
            }
            if (controller.openModWebsite(selectedMod) == -1) {
                view.showMessage(L10n.getString("error.websitenotsupported"),
                                 L10n.getString("error.websitenotsupported.title"),
                                 JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Listener for 'Visit website' menu item
     */
    class VisitForumThreadListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            controller.openWebsite(model.getHomepage());
        }
    }

    /**
     * Listener for 'Apply mods' button/menu item
     */
    class ApplyModsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            logger.info("Applying mods...");
            // TODO: Test
            try {
                controller.applyMods();
            } catch (Exception ex) {
                logger.info("Exception: "+ex.getMessage());
                ex.printStackTrace();
            }
            view.showMessage(L10n.getString("message.modsapplied"),
                             L10n.getString("message.modsapplied.title"),
                             JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Listener for 'Apply mods and launch HoN' menu item
     */
    class ApplyAndLaunchListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            logger.info("Applying mods and launching HoN...");
            // TODO: Test & implement
            // model.applyMods();
            // model.runHoN();
            view.showMessage(L10n.getString("message.modsapplied"),
                             L10n.getString("message.modsapplied.title"),
                             JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Listener for 'Unapply all mods' menu item
     */
    class UnapplyAllModsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            logger.info("Unapplying all mods...");
            // TODO: Test & implement
            // model.unapplyMods();
            view.showMessage(L10n.getString("message.modsunapplied"),
                             L10n.getString("message.modsunapplied.title"),
                             JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Listener for 'Open mod folder' menu item
     */
    class OpenModFolderListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            logger.info("Opening mod folder...");
            if (controller.openModFolder() == -1) {
                view.showMessage(L10n.getString("error.openfoldernotsupported"),
                                 L10n.getString("error.openfoldernotsupported.title"),
                                 JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    /**
     * Listener for 'Enable/disable mod' button on mod details panel
     */
    class EnableModListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Mod mod = controller.getMod(e.getActionCommand());
            if (mod.isEnabled()) {
                logger.info("Mod '"+mod.getName()+"' is now DISABLED");
                mod.disable();
            } else {
                logger.info("Mod '"+mod.getName()+"' is now ENABLED");
                mod.enable();
            }
            // Update GUI
            view.tableRemoveListSelectionListener(lsl);
            model.updateNotify();
            view.tableAddListSelectionListener(lsl);
        }
    }

    /**
     * Listener for Drop action on the main form
     */
    class DropListener implements FileDrop.Listener {
        public void filesDropped( java.io.File[] files ) {
            boolean updated = false;
            logger.info("Files dropped: "+files.length);
            for (int i=0;i<files.length;i++) {
                File honmod = files[i];
                if (honmod.getName().endsWith(".honmod")) {
                    try {
                        controller.addHonmod(honmod, true);
                        updated = true;
                    } catch (IOException e) {
                        logger.info("Opening mod file failed. Message: "+e.getMessage());
                    }
                }
            }
            if (updated) {
                view.tableRemoveListSelectionListener(lsl);
                model.updateNotify();
                view.tableAddListSelectionListener(lsl);
            }
        }
    }

    /**
     * Listener for 'Apply LaF' button. Canges LaF of the application
     */
    class ApplyLafListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Get selected LaF and apply it
            String lafClass = view.getSelectedLafClass();
            try {
                if (lafClass.equals("default")) {
                    logger.info("Changing LaF to Default");
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } else {
                    logger.info("Changing LaF to "+lafClass);
                    UIManager.setLookAndFeel(lafClass);
                }
                // Update UI
                SwingUtilities.updateComponentTreeUI(view);
                SwingUtilities.updateComponentTreeUI(view.getPrefsDialog());
                view.pack();
                view.getPrefsDialog().pack();
            } catch (Exception ex) {
                logger.warn("Unable to change Look and feel: "+ex.getMessage());
                //TODO: some error message?
            }
        }
    }

    /**
     * Listener for 'Ok' button on the preferences dialog
     */
    class PrefsOkListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	try {
				controller.saveOptions();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
            // Save language choice
            prefs = Preferences.userNodeForPackage(L10n.class);
            String lang = view.getSelectedLanguage();
            if (lang.equals(L10n.getDefaultLocale())) {
                prefs.remove(model.PREFS_LOCALE);
            } else {
                prefs.put(model.PREFS_LOCALE, lang);
            }
            // Save HoN folder
            // TODO: check that selected folder contains HoN
            prefs = Preferences.userNodeForPackage(Manager.class);
            prefs.put(model.PREFS_HONFOLDER, view.getSelectedHonFolder());
            // Save CL arguments
            if (view.getCLArguments().equals("")) {
                prefs.remove(model.PREFS_CLARGUMENTS);
            } else {
                prefs.put(model.PREFS_CLARGUMENTS, view.getCLArguments());
            }
            // Save selected LaF
            if (view.getSelectedLafClass().equals("default")) {
                prefs.remove(model.PREFS_LAF);
            } else {
                prefs.put(model.PREFS_LAF, view.getSelectedLafClass());
            }
            // TODO: Check that LaF was applied
            // Hide dialog
            view.getPrefsDialog().setVisible(false);
        }
    }

    /**
     * Listener for 'Choose HoN folder' button. Lets user navigate through
     * filesystem and select folder where HoN is installed
     */
    class ChooseFolderHonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(view);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File directory = fc.getSelectedFile();
                view.setTextFieldHonFolder(directory.getPath());
                logger.info("Hon folder selected: "+directory.getPath());
            }
        }
    }

    /**
     * Listener for 'Cancel' button on preferences dialog
     */
    class PrefsCancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            view.getPrefsDialog().setVisible(false);
        }
    }

    /**
     * Listener for 'Exit' menu item
     */
    class ExitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Close the main window
            logger.info("Closing HonModManager...");
            System.exit(0);
        }
    }
}