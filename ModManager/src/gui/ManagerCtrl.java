package gui;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

import org.apache.log4j.Logger;

import com.mallardsoft.tuple.Pair;
import com.mallardsoft.tuple.Tuple;

import app.ManagerApp;
import business.ManagerOptions;
import business.Mod;
import controller.Manager;
import gui.l10n.L10n;
import gui.views.DetailsView;
import utility.FileDrop;
import utility.OS;
import utility.Game;
import utility.ZIP;
import utility.update.UpdateReturn;

import java.util.Observable;
import java.util.ResourceBundle;

import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;

import java.awt.event.ComponentEvent;
import java.awt.Component;
import java.awt.event.ComponentListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;

import java.nio.channels.FileLockInterruptionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Observer;
import java.util.Set;
import java.util.prefs.Preferences;

import exceptions.ModConflictException;
import exceptions.ModEnabledException;
import exceptions.ModNotEnabledException;
import exceptions.ModNotFoundException;
import exceptions.ModSameNameDifferentVersionsException;
import exceptions.ModStreamException;
import exceptions.ModVersionMissmatchException;
import exceptions.ModVersionUnsatisfiedException;
import exceptions.InvalidModActionParameterException;
import exceptions.ModDuplicateException;
import exceptions.ModZipException;
import exceptions.NothingSelectedModActionException;
import exceptions.StringNotFoundModActionException;
import exceptions.UnknowModActionException;

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
    private static ManagerCtrl instance = null;

    /**
     * Initialize event listeners.
     *
     * @param model model of the MVC framework
     * @param view view of the MVC framework
     */
    public ManagerCtrl() {
        this.model = ManagerOptions.getInstance();
        this.controller = Manager.getInstance();
        ManagerCtrl.view = ManagerGUI.getInstance();
        this.controller.addObserver(this);
        this.model.addObserver(this);

        // Set up look and feel
        loadLaf();

        initViewComponents(view);

        loadMods();

        if (model.getAppliedMods().isEmpty()) {
            importModsFromOldModManager();
        }

        // Display window
        view.fullyLoaded = true;
        view.setVisible(true);
        try {
            if (model.getLastHonVersion() != null && !model.getLastHonVersion().isEmpty() && !Game.getInstance().getVersion().equals(model.getLastHonVersion())) {
                if (JOptionPane.showConfirmDialog(view, L10n.getString("message.update.suggest"), L10n.getString("message.update.suggest.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
                    Task task = new Task<Void, Void>(Application.getInstance()) {

                        @Override
                        protected Void doInBackground() throws Exception {
                            view.setInputEnabled(false);
                            view.setStatusMessage("Updating mods", true);
                            view.getProgressBar().setVisible(true);
                            view.getProgressBar().setStringPainted(true);
                            ArrayList<Mod> toUpdate = new ArrayList<Mod>();
                            Iterator<Mod> it = model.getMods().iterator();
                            while (it.hasNext()) {
                                toUpdate.add(it.next());
                            }
                            view.getProgressBar().setMaximum(toUpdate.size());
                            view.getProgressBar().paint(view.getProgressBar().getGraphics());
                            view.paint(view.getGraphics());
                            UpdateReturn things = null;
                            things = controller.updateMod(toUpdate);
                            it = things.getUpdatedModList().iterator();
                            String message = "";
                            if (it.hasNext()) {
                                message += L10n.getString("message.update.updatedmods") + " \n\n";
                                while (it.hasNext()) {
                                    Mod mod = it.next();
                                    message += L10n.getString("message.update.updated").replace("#mod#", mod.getName()).replace("#olderversion#", things.getOlderVersion(mod)).replace("#newversion#", mod.getVersion()) + "\n";
                                }
                                message += "\n\n";
                            } else {
                                message = L10n.getString("message.update.uptodate");
                            }
                            it = things.getFailedModList().iterator();
                            if (it.hasNext()) {
                                if (!message.isEmpty()) {
                                    message += "\n\n";
                                }
                                message += L10n.getString("message.update.failed") + "\n";
                                while (it.hasNext()) {
                                    Mod mod = it.next();
                                    message += "- " + mod.getName() + " (" + things.getException(mod).getLocalizedMessage() + ")\n";
                                }
                            }
                            view.getProgressBar().setValue(0);
                            view.getProgressBar().setStringPainted(false);
                            view.updateModTable();
                            view.showMessage(message, L10n.getString("message.update.title"), JOptionPane.INFORMATION_MESSAGE);
                            view.setInputEnabled(true);
                            return null;
                        }
                    };
                    task.execute();
                }
            }
        } catch (Exception ex) {
        }
        try {
            model.setLastHonVersion(Game.getInstance().getVersion());
        } catch (Exception ex) {
        }

        if (model.getNoOptionsFile()) {
            if (model.getGamePath() == null || model.getModPath() == null || model.getGamePath().isEmpty() || model.getModPath().isEmpty()) {
                view.showMessage(L10n.getString("error.honmodsfolder"), L10n.getString("error.honmodsfolder.title"), JOptionPane.ERROR_MESSAGE);
                view.getItemOpenPreferences().doClick();
            } else {
                if (JOptionPane.showConfirmDialog(view, L10n.getString("message.importfromoldmodmanager"), L10n.getString("message.importfromoldmodmanager.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
                    importModsFromOldModManager();
                }
            }
        }

        ManagerCtrl.instance = this;
        logger.info("ManagerCtrl finished initialization.");
    }

    public static ManagerCtrl getInstance() {
        return instance;
    }

    private void initViewComponents(ManagerGUI view) {
        changeFonts(new FontUIResource("Dialog", Font.PLAIN, 14));

        // Set up last window position
        if (model.getGuiRectangle() != null) {
            view.setBounds(model.getGuiRectangle());
        }

        // Load last column order and widths for details view
        DetailsView detailsView = (DetailsView) view.getModsTable().getView(ModsTable.ViewType.DETAILS);
        if (detailsView != null && model.getColumnsOrder() != null) {
            detailsView.deserializeColumnOrder(model.getColumnsOrder());
        }
        if (detailsView != null && model.getColumnsWidth() != null) {
            int i = 0;
            Iterator<Integer> it = model.getColumnsWidth().iterator();
            while (it.hasNext()) {
                int width = it.next();
                detailsView.setColumnWidth(i, width);
                ++i;
            }
        }

        // Update table
        view.getModsTable().getCurrentView().getComponent().repaint();

        // Add listeners to view components
        view.buttonAddModAddActionListener(new AddModListener());
        view.buttonEnableModAddActionListener(new EnableModListener());
        view.popupMenuItemEnableDisableModAddActionListener(new EnableModListener());
        view.itemApplyModsAddActionListener(new ApplyModsListener());
        view.itemApplyAndLaunchAddActionListener(new ApplyAndLaunchListener());
        view.itemUnapplyAllModsAddActionListener(new UnapplyAllModsListener());
        view.itemOpenModFolderAddActionListener(new OpenModFolderListener());
        view.itemVisitForumThreadAddActionListener(new VisitForumThreadListener());
        // ----- DetailsView bug Disable ------
        view.itemViewDetailsAddActionListener(new ViewChangeListener(ModsTable.ViewType.DETAILS));
        view.itemViewIconsAddActionListener(new ViewChangeListener(ModsTable.ViewType.ICONS));
        view.itemViewTilesAddActionListener(new ViewChangeListener(ModsTable.ViewType.TILES));
        view.itemViewDetailedIconsAddActionListener(new ViewChangeListener(ModsTable.ViewType.DETAILED_ICONS));
        view.itemUseSmallIconsAddActionListener(new SmallIconsListener());
        view.itemExportOverviewAddActionListener(new ExportOverviewListener());
        view.itemExitAddActionListener(new ExitListener());

        view.buttonVisitWebsiteAddActionListener(new VisitWebsiteListener());
        view.popupMenuItemVisitWebsiteAddActionListener(new VisitWebsiteListener());
        view.buttonApplyLafAddActionListener(new ApplyLafListener());
        view.buttonApplyLanguageAddActionListener(new ApplyLanguageListener());
        view.buttonOkAddActionListener(new PrefsOkListener());
        view.buttonCancelAddActionListener(new PrefsCancelListener());
        view.buttonHonFolderAddActionListener(new ChooseFolderHonListener());
        view.buttonUpdateModActionListener(new UpdateModListener());
        view.popupMenuItemUpdateModAddActionListener(new UpdateModListener());
        view.buttonModsFolderAddActionListener(new ChooseFolderModsListener());
        view.itemDownloadModUpdates(new DownloadModUpdatesListener());
        // DetailsView bug Disable
        if (detailsView != null) {
            ((JTable) detailsView.getComponent()).getColumnModel().addColumnModelListener(new Columns2Listener());
        }
        view.addComponentListener(new ComponentEventListener());
        view.getItemRefreshManager().addActionListener(new RefreshManagerListener());
        view.getButtonViewChagelog().addActionListener(new ButtonViewModChangelogListener());
        view.popupMenuItemViewChangelogAddActionListener(new ButtonViewModChangelogListener());
        view.popupItemMenuDeleteModAddActionListener(new DeleteModListener());
        view.getButtonViewModDetails().addActionListener(new ButtonViewModChangelogListener());
        view.getButtonLaunchHon().addActionListener(new ApplyAndLaunchListener());
        view.itemImportFromOldModManagerAddActionListener(new ImportModsFromOldModManager());
        view.getModsTable().addKeyListener(new ModTableKeyListener());

        // Add file drop functionality
        new FileDrop(view, new DropListener());

        // Load the user's chosen view
        if (model.getViewType().equals(ManagerOptions.ViewType.DETAILED_ICONS)) {
            view.getItemViewDetailedIcons().doClick();
        } else if (model.getViewType().equals(ManagerOptions.ViewType.DETAILS)) {
            view.getItemViewDetails().doClick();
        } else if (model.getViewType().equals(ManagerOptions.ViewType.ICONS)) {
            view.getItemViewIcons().doClick();
        } else if (model.getViewType().equals(ManagerOptions.ViewType.TILES)) {
            view.getItemViewTiles().doClick();
        }

        // Load the user's small icons preference.
        if (model.usingSmallIcons()) {
            view.getItemUseSmallIcons().doClick();
        }

        try {
            view.setStatusMessage("<html><font color=#009900>" + (model.getAppliedMods().size()) + "</font>/<font color=#0033cc>" + (model.getMods().size()) + "</font> " + L10n.getString("status.modsenabled") + " - Version: " + Game.getInstance().getVersion() + "</html>", false);
        } catch (Exception ex) {
            view.setStatusMessage("<html><font color=#009900>" + (model.getAppliedMods().size()) + "</font>/<font color=#0033cc>" + (model.getMods().size()) + "</font> " + L10n.getString("status.modsenabled") + "</html>", false);
        }

    }

    public void changeFonts(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
        SwingUtilities.updateComponentTreeUI(view);
        SwingUtilities.updateComponentTreeUI(view.getPrefsDialog());
    }

    /**
     * @deprecated not currently used.
     */
    public class CheckBoxHeader extends JCheckBox implements TableCellRenderer, MouseListener {

        protected CheckBoxHeader rendererComponent;
        protected int column;
        protected boolean mousePressed = false;

        public CheckBoxHeader(ItemListener itemListener) {
            rendererComponent = this;
            rendererComponent.addItemListener(itemListener);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    rendererComponent.setForeground(header.getForeground());
                    rendererComponent.setBackground(header.getBackground());
                    rendererComponent.setFont(header.getFont());
                    header.addMouseListener(rendererComponent);
                }
            }
            setColumn(column);
            //setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
            return rendererComponent;
        }

        protected void setColumn(int column) {
            this.column = column;
        }

        public int getColumn() {
            return column;
        }

        protected void handleClickEvent(MouseEvent e) {
            if (mousePressed && !totalDisable) {
                mousePressed = false;
                JTableHeader header = (JTableHeader) (e.getSource());
                JTable tableView = header.getTable();
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);

                boolean isToDisable = true;
                if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {
                    Iterator<Mod> it = ManagerOptions.getInstance().getMods().iterator();
                    while (it.hasNext()) {
                        if (!it.next().isEnabled()) {
                            isToDisable = false;
                        }
                    }
                    for (int k = 0; k < 5; k++) {
                        for (int i = 0; i < ManagerOptions.getInstance().getMods().size(); i++) {
                            Mod m = ManagerOptions.getInstance().getMods().get(i);
                            if (!isToDisable) {
                                try {
                                    controller.enableMod(m, ManagerOptions.getInstance().isIgnoreGameVersion());
                                    //logger.info("Mod '" + m.getName() + "' has been ENABLED");
                                } catch (Exception ex) {
                                }
                            } else {
                                try {
                                    controller.disableMod(m);
                                    //logger.info("Mod '" + m.getName() + "' has been DISABLED");
                                } catch (Exception ex) {
                                }
                            }
                        }
                        view.updateModTable();
                    }
                }
                try {
                    this.finalize();
                } catch (Throwable ex) {
                }
            }
        }
        boolean totalDisable = false;

        @Override
        protected void finalize() throws Throwable {
            totalDisable = true;
            super.finalize();
        }

        public void mouseClicked(MouseEvent e) {
            handleClickEvent(e);
            ((JTableHeader) e.getSource()).repaint();
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            mousePressed = true;
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    /**
     * This method is used to get the running instance of the ManagerGUI class.
     * @return the instance.
     * @see get()
     * @deprecated currently not used.
     */
    public static ManagerGUI getGUI() {
        return view;
    }

    public void loadLaf() {
        // Get selected LaF and apply it
        String lafClass = model.getLaf();
        try {
            if (lafClass.equals("default") || lafClass.isEmpty()) {
                logger.info("Setting LaF to Default");
                if (OS.isWindows()) {
                    try {
                        UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
                        ManagerOptions.getInstance().setLaf("com.jgoodies.looks.windows.WindowsLookAndFeel");
                    } catch (Exception e) {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        ManagerOptions.getInstance().setLaf(UIManager.getSystemLookAndFeelClassName());
                    }
                } else {
                    ManagerOptions.getInstance().setLaf(UIManager.getSystemLookAndFeelClassName());
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                SwingUtilities.updateComponentTreeUI(view);
                SwingUtilities.updateComponentTreeUI(view.getPrefsDialog());
                view.pack();
                view.getPrefsDialog().pack();
            } else {
                logger.info("Changing LaF to " + lafClass);
                UIManager.setLookAndFeel(lafClass);
            }
            // Update UI. Probally not all those methods should be called, but Swing is so complex, that this combo fix all the problems.
            SwingUtilities.updateComponentTreeUI(view);
            SwingUtilities.updateComponentTreeUI(view.getPrefsDialog());
            view.pack();
            view.getPrefsDialog().pack();
        } catch (Exception ex) {
            logger.warn("Unable to change Look and feel: " + ex.getMessage());
            //TODO: some error message?
        }
    }

    /**
     * This method is called when the mods are being applied, to update the status bar fill percentage.
     */
    public void update(Observable o, Object arg) {
        if (o.getClass().equals(Manager.class)) {
            int[] ints = (int[]) arg;
            view.getProgressBar().setValue(ints[0]);
        }

    }

    private void loadMods() {
        view.setInputEnabled(false);
        try {
            ArrayList<ArrayList<Pair<String, String>>> exs = controller.loadMods();
            controller.buildGraphs();
            Set<Mod> newApplied = new HashSet<Mod>();
            Iterator<Mod> applied = model.getAppliedMods().iterator();
            while (applied.hasNext()) {
                Mod appliedMod = applied.next();
                Iterator<Mod> mods = model.getMods().iterator();
                while (mods.hasNext()) {
                    Mod mod = mods.next();
                    if (appliedMod.equals(mod)) {
                        newApplied.add(mod);
                        mod.enable();
                    }
                }
            }
            model.setAppliedMods(newApplied);
            if (!exs.isEmpty()) {
                Enumeration en = Collections.enumeration(exs);
                String stream = "";
                String notfound = "";
                String zip = "";
                String duplicate = "";
                String duplicate2 = "";
                boolean before = false;
                boolean before2 = false;
                boolean before3 = false;
                boolean foundMod = false;

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
                        if (Tuple.get2(item).equalsIgnoreCase("duplicate")) {
                            logger.error("ModDuplicateException: mod:" + Tuple.get1(item));
                            if (!foundMod) {
                                Iterator<Mod> it = model.getMods().iterator();
                                while (it.hasNext() && !foundMod) {
                                    Mod mod = it.next();
                                    if (new File(mod.getPath()).getName().equals(Tuple.get1(item))) {
                                        if (before2) {
                                            duplicate += ", ";
                                        }
                                        duplicate += mod.getName() + " " + mod.getVersion();
                                        before2 = true;
                                        foundMod = true;
                                    }
                                }
                            } else {
                                if (before3) {
                                    duplicate2 += ", ";
                                }
                                duplicate2 += Tuple.get1(item);
                                before3 = true;
                            }
                        }

                    }
                }
                if (!stream.isEmpty() || !zip.isEmpty()) {
                    view.showMessage(L10n.getString("error.modcorrupt").replace("#mod#", "<strong>" + stream + zip + "</strong>"), L10n.getString("error.modcorrupt.title"), JOptionPane.ERROR_MESSAGE);
                }

                if (!notfound.isEmpty()) {
                    view.showMessage(L10n.getString("error.modsnotfound").replace("#mod#", notfound), L10n.getString("error.modsnotfound.title"), JOptionPane.ERROR_MESSAGE);
                }

                if (!duplicate.isEmpty() || !duplicate2.isEmpty()) {
                    view.showMessage("<html>" + L10n.getString("error.modduplicate").replace("#mod#", "<strong>" + duplicate + "</strong>").replace("#mod2#", "<strong>" + duplicate2 + "</strong>") + "<br/><br/><strong>" + L10n.getString("error.modduplicate.complement") + "</strong></html>", L10n.getString("error.modduplicate.title"), JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (IOException ex) {
            logger.error("IOException from loadMods()", ex);
            view.showMessage(L10n.getString("error.loadmodfiles"), L10n.getString("error.loadmodfiles.title"), JOptionPane.ERROR_MESSAGE);
        }
        view.setInputEnabled(true);
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
     * Listener for detecting changes of selection in the mods table. This is
     * used when user selects a row in the table.
     */
    class ModListSelectionListener implements ListSelectionListener {

        JList list;

        ModListSelectionListener(JList _list) {
            this.list = _list;
        }

        /**
         * On selection change, display details of the newly selected mod
         */
        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == list.getSelectionModel()) {
                view.displayModDetail();
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
            if ((extension != null) && ((extension.equals(".honmod")) || (extension.equals(".zip")))) {
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

        private String lastfolder = null;

        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser(new File("."));
            if (lastfolder == null) {
                fc.setCurrentDirectory(new File("."));
            } else {
                fc.setCurrentDirectory(new File(lastfolder));
            }
            fc.setAcceptAllFileFilterUsed(false);
            fc.setMultiSelectionEnabled(true);
            ModFilter filter = new ModFilter();
            fc.setFileFilter(filter);
            int returnVal = fc.showOpenDialog(view);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                lastfolder = fc.getSelectedFiles()[0].getParent();
                File[] files = fc.getSelectedFiles();
                addHonmod(files);
                loadMods();
                view.getModsTable().redraw();
            }
        }
    }

    private void addHonmod(File[] files) {
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null && (files[i].getName().endsWith(".honmod") || (files[i].getName().endsWith(".zip")))) {
                try {
                    logger.info("Opening adding honmod: " + files[i].getAbsolutePath());
                    controller.addHonmod(files[i], true);
                } catch (ModNotFoundException ex) {
                    Pair<String, String> item = ex.getMods().get(0);
                    view.showMessage(L10n.getString("error.modsnotfound").replace("#mod#", Tuple.get1(item)), L10n.getString("error.modsnotfound.title"), JOptionPane.ERROR_MESSAGE);
                    logger.error("Cannot open honmod file: " + Tuple.get1(item));
                } catch (ModStreamException ex) {
                    Pair<String, String> item = ex.getMods().get(0);
                    view.showMessage(L10n.getString("error.modcorrupt").replace("#mod#", Tuple.get1(item)), L10n.getString("error.modcorrupt.title"), JOptionPane.ERROR_MESSAGE);
                    logger.error("Honmod file corrupt: " + Tuple.get1(item));
                } catch (ModZipException ex) {
                    Pair<String, String> item = ex.getMods().get(0);
                    view.showMessage(L10n.getString("error.modcorrupt").replace("#mod#", Tuple.get1(item)), L10n.getString("error.modcorrupt.title"), JOptionPane.ERROR_MESSAGE);
                    logger.error("Honmod file corrupt: " + Tuple.get1(item));
                } catch (ModDuplicateException ex) {
                    Pair<String, String> item = ex.getMods().get(0);
                    view.showMessage("<html>" + L10n.getString("error.modduplicate").replace("#mod#", "<strong>" + Tuple.get1(ex.getMods().get(0)) + "</strong>").replace("#mod2#", "<strong>" + Tuple.get1(ex.getMods().get(1)) + "</strong>") + "<br/><br/><strong>" + L10n.getString("error.modduplicate.complement") + "</strong></html>", L10n.getString("error.modduplicate.title"), JOptionPane.ERROR_MESSAGE);
                    logger.error("Mod duplicated: " + Tuple.get1(ex.getMods().get(0)) + " - " + Tuple.get1(ex.getMods().get(1)));
                } catch (IOException e) {
                }
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
            if ((row == -1) || (column == -1)) {
                return;
            }
            Mod mod = view.getSelectedMod();
            enableMod(mod);

            view.getModsTable().redraw();
        }
    }

    /**
     * Listener for clicks on 'Visit website' label in mod details
     */
    class VisitWebsiteListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            if (!controller.openWebsite(view.getModsTable().getSelectedMod().getWebLink())) {
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
            controller.openWebsite(model.HOMEPAGE);
        }
    }

    /**
     * Listener for 'Apply mods' button/menu item
     */
    class ApplyModsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Task task = new Task<Void, Void>(Application.getInstance()) {

                @Override
                protected Void doInBackground() throws Exception {
                    applyMods();
                    return null;
                }
            };
            task.execute();
        }
    }

    /**
     * Listener for 'Apply mods and launch HoN' menu item
     */
    class ApplyAndLaunchListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            logger.info("Applying mods and launching HoN...2");
            Task task = new Task<Void, Void>(Application.getInstance()) {

                @Override
                protected Void doInBackground() throws Exception {
                    if (applyMods()) {
                        try {
                            Process game = Runtime.getRuntime().exec(new String[]{Game.getInstance().getHonExecutable().getAbsolutePath()});
                            game = null;
                        } catch (IOException ex) {
                            view.showMessage(L10n.getString("message.honnotfound"),
                                    L10n.getString("message.honnotfound.title"),
                                    JOptionPane.ERROR_MESSAGE);
                            logger.error("HoN couldn't be launched. Hon path=" + model.getGamePath(), ex);
                        }
                        if (!model.isDeveloperMode()) {
                            exit();
                        }
                    }
                    return null;
                }
            };
            task.execute();
        }
    }

    /**
     * Listener for 'Unapply all mods' menu item
     */
    class UnapplyAllModsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            view.setInputEnabled(false);
            logger.info("Unapplying all mods...");
            try {
                controller.unapplyMods();
            } catch (SecurityException e1) {
            } catch (IOException e1) {
            }
            view.setInputEnabled(true);
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

    class ModTableKeyListener implements KeyListener {

        // Don't change this
        public void keyTyped(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
            }
        }

        // This can be edited
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                enableMod(view.getSelectedMod());
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedMod();
            }
        }

        // Don't change this
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
            }
        }
    }

    class ModListKeyListener implements KeyListener {

        // Don't change this
        public void keyTyped(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
            }
        }

        // This can be edited
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                enableMod(view.getSelectedMod());
                view.getModsTable().redraw();
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedMod();
            }
        }

        // Don't change this
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
            }
        }
    }

    /**
     * Listener for changes to the mod list view type preference.
     */
    class ViewChangeListener implements ActionListener {

        ModsTable.ViewType viewType;

        public ViewChangeListener(ModsTable.ViewType _viewType) {
            viewType = _viewType;
        }

        public void actionPerformed(ActionEvent e) {
            if (view.getModsTable().getViewMode() != viewType) {
                view.getModsTable().setViewMode(viewType);
                if (viewType.equals(viewType.DETAILED_ICONS)) {
                    model.setViewType(ManagerOptions.ViewType.DETAILED_ICONS);
                } else if (viewType.equals(viewType.DETAILS)) {
                    model.setViewType(ManagerOptions.ViewType.DETAILS);
                } else if (viewType.equals(viewType.ICONS)) {
                    model.setViewType(ManagerOptions.ViewType.ICONS);
                } else if (viewType.equals(viewType.TILES)) {
                    model.setViewType(ManagerOptions.ViewType.TILES);
                }
            }
        }
    }

    /**
     * Listener for changes to the icon size preference.
     */
    class SmallIconsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            ManagerOptions.getInstance().setUseSmallIcons(((JCheckBoxMenuItem) e.getSource()).isSelected());
            view.getModsTable().getCurrentView().applyOptions();
            view.getModsTable().redraw();
        }
    }

    /**
     * Listener for exporting an overview
     */
    class ExportOverviewListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JFileChooser saveDialog = new JFileChooser(".");
            saveDialog.addChoosableFileFilter(new FileNameExtensionFilter("TXT file", "txt"));
            if (saveDialog.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                File outFile = saveDialog.getSelectedFile();
                // TODO: Check if file already exists and warn user.

                try {
                    BufferedWriter outStream = new BufferedWriter(new FileWriter(outFile));
                    for (Mod mod : model.getMods()) {
                        outStream.write(mod.getName());
                        outStream.newLine();
                    }
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Could not open write to file.");
                    // TODO: Nicer message box here.
                }
            }
        }
    }

    class UpdateModListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //view.getProgressBar().setStringPainted(true);
            view.setInputEnabled(false);
            logger.info("Ctrl: " + e.getActionCommand() + " is called to update");
            Mod mod = view.getModsTable().getSelectedMod();
            ArrayList<Mod> toUpdate = new ArrayList<Mod>();
            toUpdate.add(mod);
            UpdateReturn things = null;
            things = controller.updateMod(toUpdate);
            if (!things.getFailedModList().isEmpty()) {
                view.showMessage(L10n.getString("message.update.failed.single").replace("#mod#", things.getFailedModList().get(0).getName()).replace("#reason#", things.getException(mod).getMessage()), L10n.getString("message.update.title"), JOptionPane.INFORMATION_MESSAGE);
            } else if (!things.getUpToDateModList().isEmpty()) {
                view.showMessage(L10n.getString("message.update.uptodate.single").replace("#mod#", things.getUpToDateModList().get(0).getName()), L10n.getString("message.update.title"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                view.showMessage(L10n.getString("message.update.updated").replace("#mod#", things.getUpdatedModList().get(0).getName()).replace("#olderversion#", things.getOlderVersion(mod)).replace("#newversion#", mod.getVersion()), L10n.getString("message.update.title"), JOptionPane.INFORMATION_MESSAGE);
            }
            view.setInputEnabled(true);
            view.getModsTable().redraw();
        }
    }

    class ImportModsFromOldModManager implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            importModsFromOldModManager();
        }
    }

    public void importModsFromOldModManager() {
        try {
            String extractZipComment = ZIP.extractZipComment(model.getGamePath() + File.separator + "game" + File.separator + "resources999.s2z");
            ArrayList<String> modArray = new ArrayList<String>();
            ArrayList<String> versionArray = new ArrayList<String>();

            BufferedReader br = new BufferedReader(new StringReader(extractZipComment));

            String str = null;
            boolean isMod = false;
            try {
                while ((str = br.readLine()) != null) {
                    if (str.length() > 0) {
                        if (isMod) {
                            int start = str.indexOf("(") + 2; // Jump the ( AND jump the 'v' [ModManager outputs in this format (v1.2.5) so we need to avoid that v also]
                            int end = str.indexOf(")");
                            versionArray.add(str.substring(start, end));
                            modArray.add(str.substring(0, start - 3).trim()); // -3 because 2 from the add up there, and 1 to avoid the (
                        } else if (str.contains("Applied Mods:")) {
                            isMod = true;
                        }
                    }
                }
            } catch (IOException ex) {
            }

            for (int i = 0; i < 5; i++) {
                Iterator<String> mods = modArray.iterator();
                Iterator<String> versions = versionArray.iterator();

                while (mods.hasNext() && versions.hasNext()) {
                    String stringMod = mods.next();
                    String stringVersion = versions.next();
                    Mod mod = model.getMod(stringMod, stringVersion);
                    try {
                        controller.enableMod(mod, model.isIgnoreGameVersion());
                        mods.remove();
                        versions.remove();
                    } catch (Exception ex) {
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (Exception ex) {
        }
        view.getModsTable().redraw();
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

    class DeleteModListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            deleteSelectedMod();
        }
    }

    public void deleteSelectedMod() {
        Mod m = view.getModsTable().getSelectedMod();
        if (JOptionPane.showConfirmDialog(view, L10n.getString("question.deletemod").replace("#mod#", m.getName()), L10n.getString("question.deletemod.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
            // Pressed Yes
            view.deleteSelectedMod();
            File f = new File(m.getPath());
            if (!f.delete()) {
                view.showMessage("Failed to delete file", "Failed", JOptionPane.ERROR_MESSAGE);
                model.addMod(m, m.isEnabled());
                view.updateModTable();
            }
            logger.info("Deleting mod " + m.getName());
        }
    }

    class DownloadModUpdatesListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Task task = new Task<Void, Void>(Application.getInstance()) {

                @Override
                protected Void doInBackground() throws Exception {
                    view.setInputEnabled(false);
                    view.setStatusMessage("Updating mods", true);
                    view.getProgressBar().setVisible(true);
                    view.getProgressBar().setStringPainted(true);
                    ArrayList<Mod> toUpdate = new ArrayList<Mod>();
                    Iterator<Mod> it = model.getMods().iterator();
                    while (it.hasNext()) {
                        toUpdate.add(it.next());
                    }
                    view.getProgressBar().setMaximum(toUpdate.size());
                    view.getProgressBar().paint(view.getProgressBar().getGraphics());
                    view.paint(view.getGraphics());
                    UpdateReturn things = null;
                    things = controller.updateMod(toUpdate);
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
                    } else {
                        message = L10n.getString("message.update.uptodate");
                    }
                    //            it = things.getUpToDateModList().iterator();
                    //            if (it.hasNext()) {
                    //                message += L10n.getString("message.update.uptodate") + " \n\n";
                    //            }
                    it = things.getFailedModList().iterator();
                    if (it.hasNext()) {
                        if (!message.isEmpty()) {
                            message += "\n\n";
                        }
                        message += L10n.getString("message.update.failed") + "\n";
                        while (it.hasNext()) {
                            Mod mod = it.next();
                            message += "- " + mod.getName() + " (" + things.getException(mod).getLocalizedMessage() + ")\n";
                        }
                    }
                    view.getProgressBar().setValue(0);
                    view.getProgressBar().setStringPainted(false);
                    view.updateModTable();
                    view.showMessage(message, L10n.getString("message.update.title"), JOptionPane.INFORMATION_MESSAGE);
                    view.setInputEnabled(true);
                    return null;
                }
            };
            task.execute();
        }
    }

    class MouseEnableModListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            // Only if it's the main button!
            if (view.getModsTable().isEnabled() && e.getClickCount() == 2 && e.getButton() == 1) {
                enableMod(view.getSelectedMod());
                view.getModsTable().redraw();
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
            if (view.getModsTable().isModSelected()) {
                Mod mod = view.getModsTable().getSelectedMod();
                enableMod(mod);
                // When we click this button, the focus is taken from the
                // ModsTable. Give it back!
                view.getModsTable().grabFocus();
                view.getModsTable().repaint();
                view.displayModDetail(mod);
            }
        }
    }

    /**
     * Listener for Drop action on the main form
     */
    class DropListener implements FileDrop.Listener {

        public void filesDropped(java.io.File[] files) {
            logger.info("Files dropped: " + files.length);
            addHonmod(files);
            controller.buildGraphs();
            view.getModsTable().redraw();
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
                    model.setLaf(UIManager.getSystemLookAndFeelClassName());
                } else {
                    logger.info("Changing LaF to " + lafClass);
                    UIManager.setLookAndFeel(lafClass);
                    model.setLaf(view.getSelectedLafClass());
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
     * Listener for 'Apply Language' button. Canges LaF of the application
     */
    class ApplyLanguageListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String selectedLanguage = view.getSelectedLanguage();
            ResourceBundle backup = L10n.getResource();
            try {
                if (L10n.load(selectedLanguage)) {
                    ManagerGUI newone = ManagerGUI.newInstance();
                    newone.pack();
                    newone.getPrefsDialog().pack();
                    initViewComponents(newone);
                    view.setVisible(false);
                    view.getPrefsDialog().setVisible(false);
                    newone.setVisible(true);
                    ManagerOptions.getInstance().setLanguage(selectedLanguage);
                    view = newone;
                } else {
                    L10n.setResource(backup);
                }
            } catch (Exception ex) {
                L10n.setResource(backup);
            }
        }
    }

    /**
     * Listener for 'Ok' button on the preferences dialog
     */
    class PrefsOkListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            File f = new File(view.getTextFieldModsFolder());
            if (!f.exists()) {
                if (!f.mkdirs()) {
                    view.showMessage(L10n.getString("error.honmodsfolder"), L10n.getString("error.honmodsfolder.title"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            String oldModsFolder = model.getModPath();
            model.setGamePath(view.getSelectedHonFolder());
            model.setCLArgs(view.getCLArguments());
            //model.setLaf(view.getSelectedLafClass()); This shall not be saved here, but when the user presses Apply button
            //model.setLanguage(view.getSelectedLanguage()); This shall not be saved here, but when the user presses Apply button
            model.setModPath(view.getTextFieldModsFolder());
            model.setIgnoreGameVersion(view.getIgnoreGameVersion());
            model.setAutoUpdate(view.getAutoUpdate());
            model.setDeveloperMode(view.getDeveloperMode());

            try {
                controller.saveOptions();
                logger.info("---- Options Saved ----");
                logger.info("HoN Folder=" + model.getGamePath());
                logger.info("Mods Folder=" + model.getModPath());
                logger.info("LaF=" + model.getLaf());
                logger.info("Language=" + model.getLanguage());
                logger.info("CL=" + model.getCLArgs());
                logger.info("AutoUpdate=" + model.isAutoUpdate() + " - IgnoreGameVersion=" + model.isIgnoreGameVersion() + " - DeveloperMode=" + model.isDeveloperMode());
                logger.info("----");
            } catch (FileNotFoundException e1) {
            } catch (UnsupportedEncodingException e1) {
            } catch (IOException e1) {
            }

            // Hide dialog
            view.getPrefsDialog().setVisible(false);
            if (!oldModsFolder.equals(model.getModPath())) {
                loadMods();
                view.updateModTable();
            }
            view.getModsTable().redraw();
        }
    }

    class RefreshManagerListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            loadMods();
            view.getModsTable().redraw();
        }
    }

    /**
     * Listener for 'Choose HoN folder' button. Lets user navigate through
     * filesystem and select folder where HoN is installed
     */
    class ChooseFolderHonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            view.setTextFieldHonFolder(Game.findHonFolder());
            fc.setAcceptAllFileFilterUsed(false);
            if (OS.isMac()) {
                HoNFilter filter = new HoNFilter();
                fc.setFileFilter(filter);
                fc.setCurrentDirectory(new File("/Applications"));
            } else {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }

            if (OS.isLinux() || OS.isWindows()) {
                if (model.getGamePath() != null && !model.getGamePath().isEmpty()) {
                    fc.setCurrentDirectory(new File(model.getGamePath()));
                }
            }
            int returnVal = fc.showOpenDialog(view.getPrefsDialog());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File directory = fc.getSelectedFile();
                view.setTextFieldHonFolder(directory.getPath());
                logger.info("Hon folder selected: " + directory.getPath());
            }
            if (model.getModPath() == null || model.getGamePath().isEmpty()) {
                if (fc.getSelectedFile() != null) {
                    String modPath = Game.findModFolder(fc.getSelectedFile().getAbsolutePath());
                    if (modPath != null) {
                        view.setTextFieldModsFolder(modPath);
                    }
                }
            }
        }
    }

    class Columns2Listener implements TableColumnModelListener {

        public void columnAdded(TableColumnModelEvent e) {
        }

        public void columnRemoved(TableColumnModelEvent e) {
        }

        public void columnSelectionChanged(ListSelectionEvent e) {
        }

        public void columnMoved(TableColumnModelEvent e) {
            saveColumnChanges();
        }

        public void columnMarginChanged(ChangeEvent e) {
            saveColumnChanges();
        }

        private void saveColumnChanges() {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            DetailsView detailsView = (DetailsView) view.getModsTable().getView(ModsTable.ViewType.DETAILS);
            int lim = ((JTable) detailsView.getComponent()).getColumnCount();
            for (int i = 0; i < lim; i++) {
                temp.add(detailsView.getColumnWidth(i));
            }
            model.setColumnsWidth(temp);
            model.setColumnsOrder(detailsView.serializeColumnOrder());
            wantToSaveOptions();
        }
    }

    class ComponentEventListener implements ComponentListener {

        public void componentResized(ComponentEvent e) {
            model.setGuiRectangle(view.getBounds());
            wantToSaveOptions();
        }

        public void componentMoved(ComponentEvent e) {
            model.setGuiRectangle(view.getBounds());
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


            if (OS.isLinux() || OS.isWindows()) {
                if (model.getModPath() != null && !model.getModPath().isEmpty()) {
                    fc.setCurrentDirectory(new File(model.getModPath()));
                } else if (model.getGamePath() != null && !model.getGamePath().isEmpty()) {
                    fc.setCurrentDirectory(new File(model.getGamePath()));
                }
            }
            int returnVal = fc.showOpenDialog(view.getPrefsDialog());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File directory = fc.getSelectedFile();
                view.setTextFieldModsFolder(directory.getPath());
                logger.info("Mods folder selected: " + directory.getPath());
            }
        }
    }
    private long date;

    private void wantToSaveOptions() {
        Date d = new Date();
        if (date == 0) {
            date = d.getTime() + 100;
        }
        if (date <= d.getTime()) {
            try {
                Manager.getInstance().saveOptionsNoLog();
                date = d.getTime() + 100;
            } catch (IOException ex) {
                date = d.getTime() + 100;
            }
        }
    }

    public void enableMod(Mod mod) {
        if (mod.isEnabled()) {
            try {
                controller.disableMod(mod);
                logger.info("Mod '" + mod.getName() + "' " + mod.getVersion() + " has been DISABLED");
            } catch (ModEnabledException ex) {
                // TODO: Add auto-disable dependencies for at least 2 levels.
                view.showMessage(L10n.getString("error.modenabled").replace("#mod#", mod.getName()).replace("#mod2#", ex.toString()),
                        L10n.getString("error.modenabled.title"),
                        JOptionPane.WARNING_MESSAGE);
                logger.error("Error disabling mod: " + mod.getName() + " because: " + ex.toString() + " is/are enabled.", ex);
            }
        } else {
            try {
                String gameVersion = Game.getInstance().getVersion();
                try {
                    controller.enableMod(mod, model.isIgnoreGameVersion());
                    logger.info("Mod '" + mod.getName() + "' " + mod.getVersion() + " has been ENABLED");
                } catch (NoSuchElementException e1) {
                    view.showMessage(L10n.getString("error.modnotfound"),
                            L10n.getString("error.modnotfound.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.error("Error enabling mod: " + mod.getName() + " NoSuchElementException", e1);
                } catch (NullPointerException e1) {
                    view.showMessage(L10n.getString("error.pathnotset"),
                            L10n.getString("error.pathnotset.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.error("Error enabling mod: " + mod.getName() + " NullPointerException", e1);
                    logger.error("Error enabling mod detail: " + e1.getCause().getMessage(), e1);
                } catch (ModNotEnabledException e1) {
                    int response = view.confirmMessage(L10n.getString("error.modnotenabled").replace("#mod#", mod.getName()).replace("#mod2#", e1.toString()) + "\n" + L10n.getString("suggest.suggestmodenable"), L10n.getString("suggest.suggestmodenable.title"), JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        HashSet<Pair<String, String>> enableMods = e1.getDeps();
                        Iterator it = enableMods.iterator();
                        while (it.hasNext()) {
                            Pair<String, String> element = (Pair<String, String>) it.next();
                            Mod target = model.getMod(Tuple.get1(element), Tuple.get2(element));
                            enableMod(target);
                        }
                        // Enable the mod (finally)
                        enableMod(mod);
                        view.getModsTable().redraw();
                    }
                } catch (ModVersionMissmatchException e1) {
                    view.showMessage(L10n.getString("error.modversionmissmatch").replace("#mod#", mod.getName()),
                            L10n.getString("error.modversionmissmatch.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.error("Error enabling mod: " + mod.getName() + " because: Game version = " + gameVersion + " - Mod app version = " + e1.getAppVersion() + " ModVersionMissmatchException", e1);
                } catch (ModConflictException e1) {
                    view.showMessage(L10n.getString("error.modconflict").replace("#mod#", mod.getName()).replace("#mod2#", e1.toString()),
                            L10n.getString("error.modconflict.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.error("Error enabling mod: " + mod.getName() + " because there are conflict mods (" + e1.toString() + ") enabled", e1);
                } catch (ModVersionUnsatisfiedException e1) {
                    view.showMessage(L10n.getString("error.modversionunsatisfied").replace("#mod#", mod.getName()).replace("#mod2#", e1.toString()),
                            L10n.getString("error.modversionunsatisfied.title"),
                            JOptionPane.WARNING_MESSAGE);
                    logger.error("Error enabling mod: " + mod.getName() + " because some mods (" + e1.toString() + ") version(s) is/are not satisfied", e1);
                } catch (IllegalArgumentException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (ModSameNameDifferentVersionsException e1) {
                    // TODO Auto-generated catch block
                    view.showMessage(L10n.getString("error.modsameversion").replace("#mod#", mod.getName()), L10n.getString("error.modsameversion.title"), JOptionPane.ERROR_MESSAGE);
                    logger.error("Error enabling mod: " + mod.getName() + " because another mod is already enabled.", e1);
                }
            } catch (FileNotFoundException e1) {
                view.showMessage(L10n.getString("error.incorrectpath"),
                        L10n.getString("error.incorrectpath.title"),
                        JOptionPane.WARNING_MESSAGE);
                logger.error("Error enabling mod: " + mod.getName() + " FileNotFoundException", e1);
            } catch (IOException e1) {
            }
        }
    }

    public boolean applyMods() {
        boolean sucess = false;
        view.setInputEnabled(false);
        try {
            int count = 0;
            Iterator<Mod> iterator = model.getMods().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().isEnabled()) {
                    count += 2;
                }
            }
            count = count + 3;
            view.setStatusMessage(L10n.getString("status.applyingmods"), true);
            view.getProgressBar().setStringPainted(true);
            view.getProgressBar().setMaximum(count);
            view.getProgressBar().paint(view.getProgressBar().getGraphics());
            controller.applyMods(model.isDeveloperMode());
            view.getModsTable().redraw();
            sucess = true;
            view.showMessage(L10n.getString("message.modsapplied"), L10n.getString("message.modsapplied.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (FileLockInterruptionException ex) {
            logger.error("Error applying mods. Can't write on the resources999.s2z file", ex);
            view.showMessage(L10n.getString("error.resources999").replace("#file#", model.getGamePath() + File.separator + "game" + File.separator + "resources999.s2z"), L10n.getString("error.resources999"), JOptionPane.ERROR_MESSAGE);
        } catch (NothingSelectedModActionException ex) {
            logger.error("Error applying mods. Nothing was selected and a operation that needs something to be selected was called. Mod=" + ex.getName() + " | Version=" + ex.getVersion() + " | ActionClass=" + ex.getAction().getClass(), ex);
            view.showMessage(L10n.getString("error.modcantapply").replace("#mod#", ex.getName()), L10n.getString("error.modcantapply.title"), JOptionPane.ERROR_MESSAGE);
        } catch (StringNotFoundModActionException ex) {
            logger.error("Error applying mods. A find operation didn't find it's string. Mod=" + ex.getName() + " | Version=" + ex.getVersion() + " | String=" + ex.getString(), ex);
            view.showMessage(L10n.getString("error.modcantapply").replace("#mod#", ex.getName()), L10n.getString("error.modcantapply.title"), JOptionPane.ERROR_MESSAGE);
        } catch (InvalidModActionParameterException ex) {
            logger.error("Error applying mods. A operation had a invalid parameter. Mod=" + ex.getName() + " | Version=" + ex.getVersion() + " | ActionClass" + ex.getAction().getClass(), ex);
            view.showMessage(L10n.getString("error.modcantapply").replace("#mod#", ex.getName()), L10n.getString("error.modcantapply.title"), JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException ex) {
            // TODO: Add this on the Strings file
            logger.error("Error applying mods. A file wasn't found, so it failed to apply.", ex);
            view.showMessage("The file" + ex.getLocalizedMessage() + " wasn't found, so the mods couldn't be applied", "File not found", JOptionPane.ERROR_MESSAGE);
        } catch (UnknowModActionException ex) {
            // In theory, this part can't be called
            logger.error("Error applying mods. A unknown action was found. This message should never be logged.", ex);
            view.showMessage(L10n.getString("error.modcantapply").replace("#mod#", ex.getName()), L10n.getString("error.modcantapply.title"), JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException ex) {
            logger.error("Error applying mods. Security exception found, couldn't do some operations that were needed. " + ex.getClass(), ex);
            view.showMessage("Random error. Please, report it to the software developers", "Random error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            logger.error("Error applying mods. A random I/O exception was thrown, can't apply. " + ex.getClass(), ex);
            view.showMessage("Random error. Please, report it to the software developers", "Random error", JOptionPane.ERROR_MESSAGE);
        } finally {
            view.getProgressBar().setValue(0);
            view.getProgressBar().setStringPainted(false);
        }
        view.setInputEnabled(true);
        //view.setEnabled(true);
        //view.requestFocus();
        /*try {
        controller.exportMods(new File("C:\\mods.xml"));
        } catch (IOException ex) {
        }*/
        return sucess;
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
            Iterator<Mod> it = model.getMods().iterator();
            boolean hasUnapplied = false;
            while (it.hasNext()) {
                Mod mod = it.next();
                if ((mod.isEnabled() && !model.getAppliedMods().contains(mod)) || (!mod.isEnabled() && model.getAppliedMods().contains(mod))) {
                    hasUnapplied = true;
                }
            }
            if (hasUnapplied) {
                int option = JOptionPane.showConfirmDialog(view, L10n.getString("message.unappliedmods"), L10n.getString("message.unappliedmods.title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (option == 0) {
                    // Yes
                    // This is blocking... so the animations (progress bar and text)
                    // won't work.  Big TODO
                    Task task = new Task<Void, Void>(Application.getInstance()) {

                        @Override
                        protected Void doInBackground() throws Exception {
                            if (applyMods()) {
                                exit();
                            }
                            return null;
                        }
                    };
                    task.execute();
                } else if (option == 1) {
                    // no
                    exit();
                } else {
                    return;
                }
            } else {
                exit();
            }
        }
    }

    public void exit() {
        try {
            Manager.getInstance().saveOptions();
        } catch (IOException e1) {
            logger.error("Unable to save options");
        }
        logger.info("Closing HonModManager...");
        ManagerApp.requestShutdown();
    }
}
