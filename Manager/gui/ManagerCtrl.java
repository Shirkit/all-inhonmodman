
package Manager.gui;

import Manager.manager.Manager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import Manager.gui.l10n.L10n;
import business.Mod;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import Manager.utility.FileDrop;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
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
    Manager model;
    ManagerGUI view;
    ListSelectionListener lsl;

    /**
     * Initialize event listeners.
     *
     * @param model model of the MVC framework
     * @param view view of the MVC framework
     */
    public ManagerCtrl(Manager model, ManagerGUI view) {
        this.model = model;
        this.view = view;

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
        // Add file drop functionality
        new FileDrop(view, new DropListener());
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
                        model.addHonmod(files[i]);
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
                    model.getMod(row).enable();
                } else {
                    logger.info("Mod at index "+row+" has been disabled");
                    model.getMod(row).disable();
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
            if (model.openModWebsite(selectedMod) == -1) {
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
            model.openWebsite(model.getHomepage());
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
                model.applyMods();
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
            if (model.openModFolder() == -1) {
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
            Mod mod = model.getMod(e.getActionCommand());
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

    class DropListener implements FileDrop.Listener {
        public void filesDropped( java.io.File[] files ) {
            boolean updated = false;
            logger.info("Files dropped: "+files.length);
            for (int i=0;i<files.length;i++) {
                File honmod = files[i];
                if (honmod.getName().endsWith(".honmod")) {
                    try {
                        model.addHonmod(honmod);
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
     * Listener for 'Exit' menu item
     */
    class ExitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Close the main window
            logger.info("Closing HonModManager...");
            view.dispose();
        }
    }
}