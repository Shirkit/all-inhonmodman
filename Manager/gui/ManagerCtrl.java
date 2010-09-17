package gui;

import com.thoughtworks.xstream.io.StreamException;
import java.awt.event.ComponentEvent;
import java.util.Observable;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import manager.Manager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;

import com.mallardsoft.tuple.Pair;
import com.mallardsoft.tuple.Tuple;

import gui.l10n.L10n;
import business.ManagerOptions;
import business.Mod;
import com.thoughtworks.xstream.io.StreamException;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import utility.FileDrop;
import utility.OS;
import utility.exception.ModConflictException;
import utility.exception.ModEnabledException;
import utility.exception.ModNotEnabledException;
import utility.exception.ModNotFoundException;
import utility.exception.ModStreamException;
import utility.exception.ModVersionMissmatchException;
import utility.exception.ModVersionUnsatisfiedException;

import java.io.IOException;
import java.nio.channels.FileLockInterruptionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
import utility.Game;
import utility.exception.InvalidModActionParameterException;
import utility.exception.ModZipException;
import utility.exception.NothingSelectedModActionException;
import utility.exception.StringNotFoundModActionException;
import utility.exception.UnknowModActionException;
import utility.update.UpdateReturn;

/**
 * Controller for the ManagerGUI. This is the 'controller' part of the MVC framework
 * used for handling UI in ModManager. Controller has access to both model and view
 * and is responsible for handling events coming from view and calling appropriate
 * implementations for these events.
 *
 * @author Kovo
 */
public class ManagerCtrl implements Observer {

    Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
    Manager controller;
    ManagerOptions model;
    static ManagerGUI view;
    ListSelectionListener lsl;
    private Preferences prefs;

    /**
     * Initialize event listeners.
     *
     * @param model model of the MVC framework
     * @param view view of the MVC framework
     */
    public ManagerCtrl() {
        this.model = ManagerOptions.getInstance();
        this.controller = Manager.getInstance();
        this.view = ManagerGUI.getInstance();
        this.controller.addObserver(this);
        this.model.addObserver(this);


        // Add listeners to view components
        view.buttonAddModAddActionListener(new AddModListener());
        view.buttonEnableModAddActionListener(new EnableModListener());
        view.popupMenuItemEnableDisableModAddActionListener(new EnableModListener());
        view.itemApplyModsAddActionListener(new ApplyModsListener());
        view.itemApplyAndLaunchAddActionListener(new ApplyAndLaunchListener());
        view.itemUnapplyAllModsAddActionListener(new UnapplyAllModsListener());
        view.itemOpenModFolderAddActionListener(new OpenModFolderListener());
        view.itemVisitForumThreadAddActionListener(new VisitForumThreadListener());
        view.itemExitAddActionListener(new ExitListener());
        lsl = new ModTableSelectionListener(view.getModListTable());
        view.tableAddListSelectionListener(lsl);
        view.tableAddModelListener(new TableEditListener());
        view.buttonVisitWebsiteAddActionListener(new VisitWebsiteListener());
        view.popupMenuItemVisitWebsiteAddActionListener(new VisitWebsiteListener());
        view.buttonApplyLafAddActionListener(new ApplyLafListener());
        view.buttonOkAddActionListener(new PrefsOkListener());
        view.buttonCancelAddActionListener(new PrefsCancelListener());
        view.buttonHonFolderAddActionListener(new ChooseFolderHonListener());
        view.buttonUpdateModActionListener(new UpdateModListener());
        view.popupMenuItemUpdateModAddActionListener(new UpdateModListener());
        view.buttonModsFolderAddActionListener(new ChooseFolderModsListener());
        view.itemDownloadModUpdates(new DownloadModUpdatesListener());
        view.getModListTable().getColumnModel().addColumnModelListener(new Columns2Listener());
        view.addComponentListener(new ComponentEventListener());
        view.getModListTable().addMouseListener(new MouseEnableModListener());
        view.getItemRefreshManager().addActionListener(new RefreshManagerListener());
        view.getButtonViewChagelog().addActionListener(new ButtonViewModChangelogListener());
        view.popupMenuItemViewChangelogAddActionListener(new ButtonViewModChangelogListener());
        view.getButtonViewModDetails().addActionListener(new ButtonViewModChangelogListener());
        view.getButtonLaunchHon().addActionListener(new LaunchHonButton());
        // Add file drop functionality
        new FileDrop(view, new DropListener());
        try {
            // Load Options and Mods and then Look and feel
            controller.loadOptions();
        } catch (StreamException e) {
            e.printStackTrace();
            logger.error("StreamException from loadOptions()", e);
            // Mod options is invalid, must be deleted
        } catch (FileNotFoundException e) {
            if (ManagerOptions.getInstance().getGamePath() == null || ManagerOptions.getInstance().getModPath() == null) {
                view.showMessage(L10n.getString("error.honmodsfolder"), L10n.getString("error.honmodsfolder.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
        loadMods();
        loadLaf();

        view.tableRemoveListSelectionListener(lsl);
        model.updateNotify();
        view.tableAddListSelectionListener(lsl);

        if (model.getGuiRectangle() != null) {
            view.setBounds(model.getGuiRectangle());
        }
        if (model.getColumnsWidth() != null) {
            if (model.getColumnsWidth().size() != view.getModListTable().getColumnModel().getColumnCount()) {
                // If we change the interfaece, nothing else will need to done =]
                model.setColumnsWidth(null);
            } else {
                int i = 0;
                Iterator<Integer> it = model.getColumnsWidth().iterator();
                while (it.hasNext()) {
                    Integer integer = it.next();
                    view.getModListTable().getColumnModel().getColumn(i).setWidth(integer);
                    view.getModListTable().getColumnModel().getColumn(i).setPreferredWidth(integer);
                    i++;
                }
            }
        }

        logger.info("ManagerCtrl started");
    }

    /**
     * This method is used to get the running instance of the ManagerGUI class.
     * @return the instance.
     * @see get()
     */
    public static ManagerGUI getGUI() {
        return view;
    }

    public void loadLaf() {
        // Get selected LaF and apply it
        String lafClass = ManagerOptions.getInstance().getLaf();
        try {
            if (lafClass.equals("default") || lafClass.isEmpty()) {
                logger.info("Changing LaF to Default");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                logger.info("Changing LaF to " + lafClass);
                UIManager.setLookAndFeel(lafClass);
            }
            // Update UI
            SwingUtilities.updateComponentTreeUI(view);
            SwingUtilities.updateComponentTreeUI(view.getPrefsDialog());
            view.pack();
            view.getPrefsDialog().pack();
        } catch (Exception ex) {
            logger.warn("Unable to change Look and feel: " + ex.getMessage());
            ex.printStackTrace();
            //TODO: some error message?
        }
    }

    public void update(Observable o, Object arg) {
        if (o.getClass().equals(Manager.class)) {
            int[] ints = (int[]) arg;
            view.getProgressBar().setValue(ints[0]);
            view.paint(view.getGraphics());
        }

    }

    private void loadMods() {
        try {
            ArrayList<ArrayList<Pair<String, String>>> exs = controller.loadMods();
            if (!exs.isEmpty()) {
                Enumeration en = Collections.enumeration(exs);
                String stream = "";
                String notfound = "";
                String zip = "";
                boolean before = false;

                while (en.hasMoreElements()) {
                    ArrayList<Pair<String, String>> e = (ArrayList<Pair<String, String>>) en.nextElement();
                    Enumeration ex = Collections.enumeration(e);
                    while (ex.hasMoreElements()) {
                        Pair<String, String> item = (Pair<String, String>) ex.nextElement();
                        if (Tuple.get2(item).equalsIgnoreCase("stream")) {
                            logger.error("ModStreamException: mod:" + Tuple.get1(item));
                            if (before) {
                                stream += ", ";
                            }
                            stream += Tuple.get1(item);
                            before = true;
                        }
                        if (Tuple.get2(item).equalsIgnoreCase("notfound")) {
                            logger.error("ModNotFoundException: mods:" + Tuple.get1(item));
                            if (before) {
                                notfound += ", ";
                            }
                            notfound += Tuple.get1(item);
                            before = true;
                        }
                        if (Tuple.get2(item).equalsIgnoreCase("zip")) {
                            logger.error("ModZipException: mod:" + Tuple.get1(item));
                            if (before) {
                                zip += ", ";
                            }
                            zip += Tuple.get1(item);
                            before = true;
                        }

                    }
                }
                if (!stream.isEmpty() || !zip.isEmpty()) {
                    view.showMessage("<html>"+L10n.getString("error.modcorrupt").replace("#mod#", "<strong>" + stream + zip +"</strong>")+"</html>", L10n.getString("error.modcorrupt.title"), JOptionPane.ERROR_MESSAGE);
                }

                if (!notfound.isEmpty()) {
                    view.showMessage(L10n.getString("error.modsnotfound").replace("#mod#", notfound), L10n.getString("error.modsnotfound.title"), JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("IOException from loadMods()", ex);
            view.showMessage(L10n.getString("error.loadmodfiles"), L10n.getString("error.loadmodfiles.title"), JOptionPane.ERROR_MESSAGE);
        }
        try {
            controller.buildGraphs();
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("IOException from buildGraphs()", ex);
        }
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
            if (dotIndex == -1) {
                return false;
            }
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
     * File filter for JFileChooser. Only displays files ending with
     * .honmod extension
     */
    class HoNFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            int dotIndex = f.getName().lastIndexOf(".");
            if (dotIndex == -1) {
                return false;
            }
            String extension = f.getName().substring(dotIndex);
            if ((extension != null) && (extension.equals(".app"))) {
                return true;
            } else {
                return false;
            }
        }

        //The description of this filter
        public String getDescription() {
            return L10n.getString("chooser.hondescription");
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
                for (int i = 0; i < files.length; i++) {
                    logger.info("Opening mod file: " + files[i].getName());
                    try {
                        controller.addHonmod(files[i], true);
                        // Save directory for future use
                        this.currentDir = files[i].getParentFile();
                    } catch (IOException ioe) {
                        logger.error("Cannot open honmod file: " + ioe.getMessage());
                        ioe.printStackTrace();
                    } catch (ModNotFoundException e1) {
                        logger.error("Honmod file not found: " + e1.toString());
                        e1.printStackTrace();
                    } catch (ModStreamException e1) {
                        logger.error("Honmod file corrupted: " + e1.toString());
                        e1.printStackTrace();
                    } catch (ModZipException ex) {
                        logger.error("Honmod file corrupted: " + ex.toString());
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
            ArrayList<Integer> temp = new ArrayList<Integer>();
            /*
            for (int rr = 0; rr < view.getModListTable().getColumnModel().getColumnCount(); rr++) {
            view.getModListTable().getColumnModel().getColumn(rr).setWidth(view.getModListTable().getColumnModel().getColumn(rr).getWidth());
            }
             */


            int row = e.getFirstRow();
            int column = e.getColumn();
            if ((row == -1) || (column == -1)) {
                return;
            }
            TableModel tableModel = (TableModel) e.getSource();
            Mod mod = controller.getMod(row);
            try {
				enableMod(mod);
			} catch (ModNotEnabledException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModVersionUnsatisfiedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchElementException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NullPointerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModConflictException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModEnabledException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModVersionMissmatchException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


            // Again, save and restore ListSelectionListener
            view.tableRemoveListSelectionListener(lsl);
            model.updateNotify();
            view.tableAddListSelectionListener(lsl);
        }
    }

    /**
     * Listener for clicks on 'Visit website' label in mod details
     */
    class VisitWebsiteListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            // TODO: Check if it's null
            if (controller.openWebsite(view.getSelectedMod().getWebLink()) == -1) {
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
            applyMods();
        }
    }

    /**
     * Listener for 'Apply mods and launch HoN' menu item
     */
    class ApplyAndLaunchListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            logger.info("Applying mods and launching HoN...");
            applyMods();
            try {
                Process game = Runtime.getRuntime().exec(Game.getInstance().getHonExecutable().getAbsolutePath());
            } catch (IOException ex) {
                view.showMessage(L10n.getString("message.honnotfound"),
                        L10n.getString("message.honnotfound.title"),
                        JOptionPane.ERROR_MESSAGE);
                logger.error("HoN couldn't be launched. Hon path=" + model.getGamePath(), ex);
            }
        }
    }

    /**
     * Listener for 'Unapply all mods' menu item
     */
    class UnapplyAllModsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            logger.info("Unapplying all mods...");
            // TODO: Test & implement
            try {
                controller.unapplyMods();
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            model.updateNotify();
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

    class UpdateModListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            logger.info("Ctrl: " + e.getActionCommand() + " is called to update");
            Mod mod = controller.getMod(e.getActionCommand());
            ArrayList<Mod> toUpdate = new ArrayList<Mod>();
            toUpdate.add(mod);

            view.getProgressBar().setMaximum(toUpdate.size());
            UpdateReturn things = null;
			try {
				things = controller.updateMod(toUpdate);
			} catch (StreamException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModNotEnabledException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModVersionUnsatisfiedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            if (things.getFailedModList().size() > 0) {
                view.showMessage("Update of mod is not successful.", "Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                view.showMessage("Update of mod is successful.", "Result", JOptionPane.INFORMATION_MESSAGE);
            }
            model.updateNotify();
            view.getProgressBar().setValue(0);
        }
    }

    class ButtonViewModChangelogListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("display changelog")) {
                view.getPanelModChangelog().setVisible(true);
                view.getPanelModDetails().setVisible(false);
            } else {
                view.getPanelModChangelog().setVisible(false);
                view.getPanelModDetails().setVisible(true);
            }
        }
    }

    class DownloadModUpdatesListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            view.getProgressBar().setVisible(true);
            view.getProgressBar().paint(view.getProgressBar().getGraphics());
            ArrayList<Mod> toUpdate = new ArrayList<Mod>();
            Iterator<Mod> it = ManagerOptions.getInstance().getMods().iterator();
            while (it.hasNext()) {
                toUpdate.add(it.next());
            }
            view.getProgressBar().setMaximum(toUpdate.size());
            UpdateReturn things = null;
			try {
				things = controller.updateMod(toUpdate);
			} catch (StreamException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModNotEnabledException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModVersionUnsatisfiedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            it = things.getUpdatedModList().iterator();
            String message = "";
            if (it.hasNext()) {
                message += L10n.getString("message.update.updatedmods") + " \n\n";
                while (it.hasNext()) {
                    Mod mod = it.next();
                    message += L10n.getString("message.update.updated").replace("#mod#", mod.getName()).replace("#olderversion#", things.getOlderVersion(mod)).replace("#newversion#", mod.getVersion()) + "\n";
                    //message += mod.getName() + " was updated from " + things.getOlderVersion(mod) + " to " + mod.getVersion() + "\n";
                }
                message += "\n\n";
            }
            it = things.getUpToDateModList().iterator();
            if (it.hasNext()) {
                /*while (it.hasNext()) {
                Mod mod = it.next();
                message += mod.getName() + " is up-to-date (" + mod.getVersion() + ")";
                }*/
                //message += "Up-to-date Mods aren't shown\n\n";
                message += L10n.getString("message.update.uptodate") + " \n\n";
            }

            it = things.getFailedModList().iterator();
            if (it.hasNext()) {
                //message += "Failed to update mods: \n\n";
                message += L10n.getString("message.update.failed") + "\n\n";
                while (it.hasNext()) {
                    Mod mod = it.next();
                    message += mod.getName() + " (" + things.getException(mod).getLocalizedMessage() + ")\n";
                }
            }
            view.showMessage(message, L10n.getString("message.update.title"), JOptionPane.INFORMATION_MESSAGE);
            view.getProgressBar().setValue(0);
            view.updateModTable();
        }
    }

    class MouseEnableModListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                try {
					enableMod(view.getSelectedMod());
				} catch (ModNotEnabledException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ModVersionUnsatisfiedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NoSuchElementException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NullPointerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ModConflictException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ModEnabledException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ModVersionMissmatchException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                model.updateNotify();
            }
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    /**
     * Listener for 'Enable/disable mod' button on mod details panel
     */
    class EnableModListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Mod mod = controller.getMod(e.getActionCommand());
            try {
				enableMod(mod);
			} catch (ModNotEnabledException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModVersionUnsatisfiedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchElementException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NullPointerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModConflictException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModEnabledException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ModVersionMissmatchException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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

        public void filesDropped(java.io.File[] files) {
            boolean updated = false;
            logger.info("Files dropped: " + files.length);
            for (int i = 0; i < files.length; i++) {
                File honmod = files[i];
                if (honmod.getName().endsWith(".honmod")) {
                    try {
                        controller.addHonmod(honmod, true);
                        updated = true;
                    } catch (IOException e) {
                        logger.info("Opening mod file failed. Message: " + e.getMessage());
                    } catch (ModNotFoundException e) {
                        logger.info("Mod file not found. Message: " + e.toString());
                        e.printStackTrace();
                    } catch (ModStreamException e) {
                        logger.info("Mod file failed (XML). Message: " + e.toString());
                        e.printStackTrace();
                    } catch (ModZipException ex) {
                        logger.error("Honmod file corrupted: " + ex.toString());
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
                    logger.info("Changing LaF to " + lafClass);
                    UIManager.setLookAndFeel(lafClass);
                }
                // Update UI
                SwingUtilities.updateComponentTreeUI(view);
                SwingUtilities.updateComponentTreeUI(view.getPrefsDialog());
                view.pack();
                view.getPrefsDialog().pack();
            } catch (Exception ex) {
                logger.warn("Unable to change Look and feel: " + ex.getMessage());
                //TODO: some error message?
            }
        }
    }

    /**
     * Listener for 'Ok' button on the preferences dialog
     */
    class PrefsOkListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            logger.info("Hon folder set to: " + view.getSelectedHonFolder());
            logger.info("Mods folder set to: " + view.getTextFieldModsFolder());
            ManagerOptions.getInstance().setGamePath(view.getSelectedHonFolder());
            ManagerOptions.getInstance().setCLArgs(view.getCLArguments());
            ManagerOptions.getInstance().setLaf(view.getSelectedLafClass());
            ManagerOptions.getInstance().setLanguage(view.getSelectedLanguage());
            ManagerOptions.getInstance().setModPath(view.getTextFieldModsFolder());

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

            // Hide dialog
            view.getPrefsDialog().setVisible(false);
            loadMods();
            model.updateNotify();
        }
    }

    class RefreshManagerListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            loadMods();
            model.updateNotify();
        }
    }

    class LaunchHonButton implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                if (Game.getInstance().getHonExecutable() != null) {
                    applyMods();
                    Process game = Runtime.getRuntime().exec(Game.getInstance().getHonExecutable().getAbsolutePath());
                }
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(ManagerCtrl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
            }
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
            if (OS.isMac()) {
                HoNFilter filter = new HoNFilter();
                fc.setFileFilter(filter);
                fc.setCurrentDirectory(new File("/Applications"));
            } else {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }


            int returnVal = fc.showOpenDialog(view.getPrefsDialog());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File directory = fc.getSelectedFile();
                view.setTextFieldHonFolder(directory.getPath());
                logger.info("Hon folder selected: " + directory.getPath());
            }
        }
    }

    class Columns2Listener implements TableColumnModelListener {

        public void columnAdded(TableColumnModelEvent e) {
        }

        public void columnRemoved(TableColumnModelEvent e) {
        }

        public void columnMoved(TableColumnModelEvent e) {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            for (int i = 0; i < view.getModListTable().getColumnModel().getColumnCount(); i++) {
                temp.add(new Integer(view.getModListTable().getColumnModel().getColumn(i).getWidth()));
            }
            ManagerOptions.getInstance().setColumnsWidth(temp);
            wantToSaveOptions();
        }

        public void columnMarginChanged(ChangeEvent e) {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            for (int i = 0; i < view.getModListTable().getColumnModel().getColumnCount(); i++) {
                temp.add(new Integer(view.getModListTable().getColumnModel().getColumn(i).getWidth()));
            }
            ManagerOptions.getInstance().setColumnsWidth(temp);
            wantToSaveOptions();
        }

        public void columnSelectionChanged(ListSelectionEvent e) {
        }
    }

    class ComponentEventListener implements ComponentListener {

        public void componentResized(ComponentEvent e) {
            ManagerOptions.getInstance().setGuiRectangle(view.getBounds());
            wantToSaveOptions();
        }

        public void componentMoved(ComponentEvent e) {
            ManagerOptions.getInstance().setGuiRectangle(view.getBounds());
            wantToSaveOptions();

        }

        public void componentShown(ComponentEvent e) {
        }

        public void componentHidden(ComponentEvent e) {
        }
    }

    class ChooseFolderModsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            if (OS.isMac()) {
                HoNFilter filter = new HoNFilter();
                fc.setFileFilter(filter);
                fc.setCurrentDirectory(new File("/Applications"));
            } else {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }


            int returnVal = fc.showOpenDialog(view.getPrefsDialog());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File directory = fc.getSelectedFile();
                view.setTextFieldModsFolder(directory.getPath());
                logger.info("Mods folder selected: " + directory.getPath());
            }
        }
    }
    long date;

    private void wantToSaveOptions() {
        Date d = new Date();
        if (date == 0) {
            date = d.getTime() + 1000;
        }
        if (date <= d.getTime()) {
            try {
                Manager.getInstance().saveOptions();
                date = d.getTime() + 1000;
            } catch (IOException ex) {
                date = d.getTime() + 1000;
            }
        }
    }

    private void enableMod(Mod mod) throws ModNotEnabledException, ModVersionUnsatisfiedException, NoSuchElementException, NullPointerException, IllegalArgumentException, ModConflictException, ModEnabledException, ModVersionMissmatchException {
        if (mod.isEnabled()) {
            try {
                controller.disableMod(mod);
                logger.info("Mod '" + mod.getName() + "' has been DISABLED");
            } catch (ModEnabledException ex) {
                view.showMessage(L10n.getString("error.modenabled").replace("#mod#", mod.getName()).replace("#mod2#", ex.toString()),
                        L10n.getString("error.modenabled.title"),
                        JOptionPane.WARNING_MESSAGE);
                logger.warn("Error disabling mod: " + mod.getName() + " because: " + ex.toString() + " is/are enabled.", ex);
            }
        } else {
            try {
                Mod m = controller.getMod(mod.getName());
                String gameVersion = Game.getInstance().getVersion();
                try {
                    controller.enableMod(m, false);
                    logger.info("Mod '" + mod.getName() + "' has been ENABLED");
                } catch (NoSuchElementException e1) {
                    view.showMessage(L10n.getString("error.modnotfound"),
                            L10n.getString("error.modnotfound.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.warn("Error enabling mod: " + m.getName() + " NoSuchElementException", e1);
                } catch (NullPointerException e1) {
                    view.showMessage(L10n.getString("error.pathnotset"),
                            L10n.getString("error.pathnotset.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.warn("Error enabling mod: " + m.getName() + " NullPointerException", e1);
                    logger.warn("Error enabling mod detail: " + e1.getCause().getMessage(), e1);
                } catch (ModEnabledException e1) {
                    view.showMessage(L10n.getString("error.modenabled").replace("#mod#", m.getName()).replace("#mod2#", e1.toString()),
                            L10n.getString("error.modenabled.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.warn("Error disabling mod: " + m.getName() + " because: " + e1.toString() + " is/are enabled.", e1);
                } catch (ModNotEnabledException e1) {
                	
                    view.showMessage(L10n.getString("error.modnotenabled").replace("#mod#", m.getName()).replace("#mod2#", e1.toString()),
                            L10n.getString("error.modnotenabled.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.warn("Error enabling mod: " + m.getName() + " because: " + e1.toString() + " is/are not enabled.", e1);
                    
                    HashSet<Pair<String, String>> enableMods = e1.getDeps();
                    
                    int response = view.confirmMessage(L10n.getString("suggest.suggestmodenable"), L10n.getString("suggest.suggestmodenable.title"), JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                    	Iterator it = enableMods.iterator();
                    	while (it.hasNext()) {
                    		Pair<String, String> element = (Pair<String, String>)it.next();
                    		Mod target = ManagerOptions.getInstance().getMod(Tuple.get1(element), Tuple.get2(element));
                    		controller.enableMod(target, false);
                    	}
                    	
                        // Enable the mod finally
                        controller.enableMod(m, false);
                    }
                    
                } catch (ModVersionMissmatchException e1) {
                    view.showMessage(L10n.getString("error.modversionmissmatch").replace("#mod#", m.getName()),
                            L10n.getString("error.modversionmissmatch.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.warn("Error enabling mod: " + m.getName() + " because: Game version = " + gameVersion + " - Mod app version = " + e1.getAppVersion() + " ModVersionMissmatchException", e1);
                } catch (ModConflictException e1) {
                    // TODO: haven't tested this yet
                    view.showMessage(L10n.getString("error.modconflict").replace("#mod#", m.getName()).replace("#mod2#", e1.toString()),
                            L10n.getString("error.modconflict.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.warn("Error enabling mod: " + m.getName() + " because there are conflict mods (" + e1.toString() + ") enabled", e1);
                } catch (ModVersionUnsatisfiedException e1) {
                    // TODO: haven't tested this yet
                    view.showMessage(L10n.getString("error.modversionunsatisfied").replace("#mod#", m.getName()).replace("#mod2#", e1.toString()),
                            L10n.getString("error.modversionunsatisfied.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.warn("Error enabling mod: " + m.getName() + " because some mods (" + e1.toString() + ") version(s) is/are not satisfied", e1);
                } catch (IllegalArgumentException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (FileNotFoundException e1) {
                view.showMessage(L10n.getString("error.incorrectpath"),
                        L10n.getString("error.incorrectpath.title"),
                        JOptionPane.WARNING_MESSAGE);
                logger.warn("Error enabling mod: " + mod.getName() + " FileNotFoundException", e1);
            } catch (IOException e1) {
            }
        }
    }

    public void applyMods() {
        try {
            controller.applyMods();
            view.showMessage(L10n.getString("message.modsapplied"), L10n.getString("message.modsapplied.title"), JOptionPane.ERROR_MESSAGE);
        } catch (FileLockInterruptionException ex) {
            logger.error("Error applying mods. Can't write on the resources999.s2z file", ex);
            view.showMessage(L10n.getString("error.resources999").replace("#file#", ManagerOptions.getInstance().getGamePath() + File.separator + "game" + File.separator + "resources999.s2z"), L10n.getString("error.resources999"), JOptionPane.ERROR_MESSAGE);
        } catch (NothingSelectedModActionException ex) {
            logger.error("Error applying mods. Nothing was selected and a operation that needs something to be selected was called. Mod=" + ex.getName() + " | Version=" + ex.getVersion() + " | ActionClass=" + ex.getAction().getClass(), ex);
            view.showMessage(L10n.getString("error.modcantapply").replace("#mod#", ex.getName()), L10n.getString("error.modcantapply.title"), JOptionPane.ERROR_MESSAGE);
        } catch (StringNotFoundModActionException ex) {
            logger.error("Error applying mods. A find operation didn't find it's string. Mod=" + ex.getName() + " | Version=" + ex.getVersion() + " | String=" + ex.getString(), ex);
            view.showMessage(L10n.getString("error.modcantapply").replace("#mod#", ex.getName()), L10n.getString("error.modcantapply.title"), JOptionPane.ERROR_MESSAGE);
        } catch (InvalidModActionParameterException ex) {
            logger.error("Error applying mods. A operation had a invalid parameter. Mod=" + ex.getName() + " | Version=" + ex.getVersion() + " | ActionClass" + ex.getAction().getClass(), ex);
            view.showMessage(L10n.getString("error.modcantapply").replace("#mod#", ex.getName()), L10n.getString("error.modcantapply.title"), JOptionPane.ERROR_MESSAGE);
        } catch (UnknowModActionException ex) {
            // In theory, this part can't be called
            logger.error("Error applying mods. A unknown action was found. This message should never be logged.", ex);
            view.showMessage(L10n.getString("error.modcantapply").replace("#mod#", ex.getName()), L10n.getString("error.modcantapply.title"), JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException ex) {
            logger.error("Error applying mods. Security exception found, couldn't do some operations that were needed.", ex);
            view.showMessage("Random error. Please, report it to the software developers", "Random error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            logger.error("Error applying mods. A random I/O exception was thrown, can't apply.", ex);
            view.showMessage("Random error. Please, report it to the software developers", "Random error", JOptionPane.ERROR_MESSAGE);
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
            try {
                Manager.getInstance().saveOptions();
            } catch (IOException e1) {
                logger.error("Unable to save options");
                e1.printStackTrace();
            }
            logger.info("Closing HonModManager...");
            System.exit(0);
        }
    }
}
