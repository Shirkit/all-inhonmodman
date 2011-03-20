package gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import gui.l10n.L10n;
import javax.swing.JPanel;
import controller.Manager;
import business.ManagerOptions;
import business.Mod;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JDialog;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import javax.swing.UIManager;
import business.actions.Action;
import business.actions.ActionRequirement;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import javax.swing.UIManager.LookAndFeelInfo;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import utility.BBCode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import utility.Game;

/**
 * Main form of the ModManager. This class is the 'view' part of the MVC framework
 *
 * @author Shirkit, Kovo
 */
public class ManagerGUI extends javax.swing.JFrame implements Observer {
    // Model for this View (part of MVC pattern)

    private static ManagerGUI instance = null;
    private Manager controller;
    private ManagerOptions model;
    private static Logger logger = Logger.getLogger(ManagerGUI.class.getPackage().getName());
    // Column names of the mod list table
    boolean animating = false;
    public boolean fullyLoaded = false;

    /**
     * Creates the main form
     * @param model model part of the MVC framework
     */
    private ManagerGUI() {
        logger.info("Initializing gui");
        this.model = ManagerOptions.getInstance();
        this.controller = Manager.getInstance();
        ManagerOptions.getInstance().addObserver(this);

        // Registration for Synthetica Look and Feel
        String[] li = {"Licensee=Pedro Torres", "LicenseRegistrationNumber=NCPT200729", "Product=Synthetica", "LicenseType=Non Commercial", "ExpireDate=--.--.----", "MaxVersion=2.999.999"};
        UIManager.put("Synthetica.license.info", li);
        UIManager.put("Synthetica.license.key", "644E94EB-97019D70-E7B56201-11EE0820-82B6C8DC");

        initComponents();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        // Set application icon
        try {
            URL urlImage = this.getClass().getResource("resources/icon.png");
            this.setIconImage(Toolkit.getDefaultToolkit().getImage(urlImage));
            dialogOptions.setIconImage(Toolkit.getDefaultToolkit().getImage(urlImage));
        } catch (Exception e) {
            logger.warn("Cannot find application icon");
        }
        // Set model of the language combo box. This will not be localized
        comboBoxChooseLanguage.addItem(new Language("English", "en"));
        comboBoxChooseLanguage.addItem(new Language("Português (Brasil)", "pt-br"));
        comboBoxChooseLanguage.addItem(new Language("Slovak", "sk"));
        // Set model of the LaF combobox. This will not be localized
        //comboBoxLafs.addItem(new LaF("Default", UIManager.getSystemLookAndFeelClassName()));
        //comboBoxLafs.addItem(new LaF("Metal", UIManager.getCrossPlatformLookAndFeelClassName()));
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            comboBoxLafs.addItem(new LaF(info.getName(), info.getClassName()));
        }
        comboBoxLafs.addItem(new LaF("Synthetica", "de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel"));
        comboBoxLafs.addItem(new LaF("JGoodies PlasticXP", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel"));
        comboBoxLafs.addItem(new LaF("JGoodies Plastic", "com.jgoodies.looks.plastic.PlasticLookAndFeel"));
        comboBoxLafs.addItem(new LaF("JGoodies Plastic3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel"));
        comboBoxLafs.addItem(new LaF("JGoodies Windows", "com.jgoodies.looks.windows.WindowsLookAndFeel"));

        // Components on the Mod details panel are not visible by default
        setDetailsVisible(false);
        // This thing here is working along with formComponentShown to solve the fucking bug of not showing the correct size when running the app
        this.setResizable(false);

        // Change default close operation to this
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                itemExit.doClick();
            }
        });
        
        getProgressBar().setStringPainted(false);
        updateModTable();
    }

    /**
     * This method is used to get the running instance of the ManagerGUI class.
     * @return the instance.
     * @see get()
     */
    private static boolean instanceCreated = false;
    public static ManagerGUI getInstance() {
        // Using just "instance == null" leads to issues when code called from
        // the constructor calls this method.  So, I've replaced it with a
        // static boolean.
        if (!instanceCreated) {
            instanceCreated = true;
            instance = new ManagerGUI();
        }
        return instance;
    }

    /**
     * Prepares the popup (right click) menu to appear for a given mod.
     * @param mod the mod to prepare the popup for.
     */
    public void preparePopupMenu(Mod mod) {
        if ( mod.isEnabled() ) {
            popupItemMenuEnableDisableMod.setText(L10n.getString("button.disablemod"));
        } else {
            popupItemMenuEnableDisableMod.setText(L10n.getString("button.enablemod"));
        }
        
        if ( mod.getUpdateCheckUrl() == null || mod.getUpdateDownloadUrl() == null
          || mod.getUpdateCheckUrl().isEmpty() || mod.getUpdateDownloadUrl().isEmpty() ) {
            popupItemMenuUpdateMod.setEnabled(false);
        } else {
            popupItemMenuUpdateMod.setEnabled(true);
            popupItemMenuUpdateMod.setActionCommand(mod.getName());
        }

        popupItemMenuEnableDisableMod.setActionCommand(mod.getName());

        if ( mod.getChangelog() == null || mod.getChangelog().isEmpty() ) {
            popupItemMenuViewChangelog.setEnabled(false);
        } else {
            popupItemMenuViewChangelog.setEnabled(true);
            popupItemMenuViewChangelog.setActionCommand("display changelog");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dialogOptions = new javax.swing.JDialog();
        labelHonFolder = new javax.swing.JLabel();
        textFieldHonFolder = new javax.swing.JTextField();
        buttonHonFolder = new javax.swing.JButton();
        comboBoxLafs = new javax.swing.JComboBox() {
            public void addItem(Object anObject) {
                int size = ((DefaultComboBoxModel) dataModel).getSize();
                Object obj;
                boolean added = false;
                for (int i=0; i<size; i++) {
                    obj = dataModel.getElementAt(i);
                    int compare = anObject.toString().compareToIgnoreCase(obj.toString());
                    if (compare <= 0) { // if anObject less than or equal obj
                        super.insertItemAt(anObject, i);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    super.addItem(anObject);
                }
            }

        };
        buttonApplyLaf = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();
        buttonOk = new javax.swing.JButton();
        labelChooseLanguage = new javax.swing.JLabel();
        comboBoxChooseLanguage = new javax.swing.JComboBox(){
            public void addItem(Object anObject) {
                int size = ((DefaultComboBoxModel) dataModel).getSize();
                Object obj;
                boolean added = false;
                for (int i=0; i<size; i++) {
                    obj = dataModel.getElementAt(i);
                    int compare = anObject.toString().compareToIgnoreCase(obj.toString());
                    if (compare <= 0) { // if anObject less than or equal obj
                        super.insertItemAt(anObject, i);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    super.addItem(anObject);
                }
            }

        };
        labelCLArguments = new javax.swing.JLabel();
        textFieldCLArguments = new javax.swing.JTextField();
        labelChangeLanguageImplication = new javax.swing.JLabel();
        buttonModsFolder = new javax.swing.JButton();
        labelModsFolder = new javax.swing.JLabel();
        textFieldModsFolder = new javax.swing.JTextField();
        labelChooseLookAndFeel = new javax.swing.JLabel();
        checkBoxIgnoreGameVersion = new javax.swing.JCheckBox();
        checkBoxAutoUpdate = new javax.swing.JCheckBox();
        checkBoxDeveloperMode = new javax.swing.JCheckBox();
        rightClickTableMenu = new javax.swing.JPopupMenu();
        popupItemMenuEnableDisableMod = new javax.swing.JMenuItem();
        popupItemMenuUpdateMod = new javax.swing.JMenuItem();
        popupItemMenuVisitWebsite = new javax.swing.JMenuItem();
        popupItemMenuViewChangelog = new javax.swing.JMenuItem();
        popupItemMenuDeleteMod = new javax.swing.JMenuItem();
        panelModList = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar(0,100);
        buttonApplyMods = new javax.swing.JButton();
        buttonAddMod = new javax.swing.JButton();
        panelModChangelog = new javax.swing.JPanel();
        labelModIcon1 = new javax.swing.JLabel();
        labelModName1 = new javax.swing.JLabel();
        labelModAuthor1 = new javax.swing.JLabel();
        buttonViewModDetails = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        panelModDetails = new javax.swing.JPanel();
        labelModIcon = new javax.swing.JLabel();
        labelModName = new javax.swing.JLabel();
        labelModAuthor = new javax.swing.JLabel();
        panelModDescription = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        areaModDesc = new javax.swing.JTextArea();
        labelRequirements = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listRequirements = new javax.swing.JList();
        buttonEnableMod = new javax.swing.JButton();
        buttonUpdateMod = new javax.swing.JButton();
        buttonVisitWebsite = new javax.swing.JButton();
        buttonViewChagelog = new javax.swing.JButton();
        buttonLaunchHon = new javax.swing.JButton();
        labelStatus = new javax.swing.JLabel();
        modsTable = new gui.ModsTable();
        mainMenu = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        itemApplyMods = new javax.swing.JMenuItem();
        itemApplyAndLaunch = new javax.swing.JMenuItem();
        itemUnapplyAllMods = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itemOpenModFolder = new javax.swing.JMenuItem();
        itemImportFromOldModManager = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        itemDownloadModUpdates = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        itemExit = new javax.swing.JMenuItem();
        menuOptions = new javax.swing.JMenu();
        itemOpenPreferences = new javax.swing.JMenuItem();
        itemRefresh = new javax.swing.JMenuItem();
        menuView = new javax.swing.JMenu();
        ButtonGroup viewModesGroup = new ButtonGroup();
        itemViewDetails = new javax.swing.JRadioButtonMenuItem();
        itemViewIcons = new javax.swing.JRadioButtonMenuItem();
        itemViewDetailedIcons = new javax.swing.JRadioButtonMenuItem();
        itemViewTiles = new javax.swing.JRadioButtonMenuItem();
        menuHelp = new javax.swing.JMenu();
        itemVisitForumThread = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        itemAbout = new javax.swing.JMenuItem();

        dialogOptions.setTitle(L10n.getString("prefs.dialog.title"));
        dialogOptions.setMinimumSize(new java.awt.Dimension(550, 300));
        dialogOptions.setModal(true);
        dialogOptions.setName("dialogOptions"); // NOI18N
        dialogOptions.setResizable(false);

        labelHonFolder.setText(L10n.getString("prefs.label.honfolder"));
        labelHonFolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        labelHonFolder.setName("labelHonFolder"); // NOI18N

        textFieldHonFolder.setName("textFieldHonFolder"); // NOI18N

        buttonHonFolder.setText(L10n.getString("prefs.button.change"));
        buttonHonFolder.setName("buttonHonFolder"); // NOI18N

        comboBoxLafs.setName("comboBoxLafs"); // NOI18N

        buttonApplyLaf.setText(L10n.getString("prefs.button.apply"));
        buttonApplyLaf.setName("buttonApplyLaf"); // NOI18N

        buttonCancel.setText(L10n.getString("button.cancel"));
        buttonCancel.setName("buttonCancel"); // NOI18N

        buttonOk.setText(L10n.getString("button.ok"));
        buttonOk.setName("buttonOk"); // NOI18N

        labelChooseLanguage.setText(L10n.getString("prefs.label.chooselanguage"));
        labelChooseLanguage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        labelChooseLanguage.setName("labelChooseLanguage"); // NOI18N

        comboBoxChooseLanguage.setName("comboBoxChooseLanguage"); // NOI18N

        labelCLArguments.setText(L10n.getString("prefs.label.clarguments"));
        labelCLArguments.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        labelCLArguments.setName("labelCLArguments"); // NOI18N

        textFieldCLArguments.setName("textFieldCLArguments"); // NOI18N

        labelChangeLanguageImplication.setFont(new java.awt.Font("Tahoma", 0, 10));
        labelChangeLanguageImplication.setText(L10n.getString("prefs.label.languagechanges"));
        labelChangeLanguageImplication.setName("labelChangeLanguageImplication"); // NOI18N

        buttonModsFolder.setText(L10n.getString("prefs.button.change"));
        buttonModsFolder.setName("buttonModsFolder"); // NOI18N

        labelModsFolder.setText(L10n.getString("prefs.label.modsfolder"));
        labelModsFolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        labelModsFolder.setName("labelModsFolder"); // NOI18N

        textFieldModsFolder.setName("textFieldModsFolder"); // NOI18N

        labelChooseLookAndFeel.setText(L10n.getString("prefs.label.lookandfeel"));
        labelChooseLookAndFeel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        labelChooseLookAndFeel.setName("labelChooseLookAndFeel"); // NOI18N

        checkBoxIgnoreGameVersion.setText(L10n.getString("prefs.label.ignoregameversion"));
        checkBoxIgnoreGameVersion.setToolTipText(L10n.getString("tooltip.prefs.ignoregameversion"));
        checkBoxIgnoreGameVersion.setName("checkBoxIgnoreGameVersion"); // NOI18N

        checkBoxAutoUpdate.setText(L10n.getString("prefs.label.autoupdate"));
        checkBoxAutoUpdate.setName("checkBoxAutoUpdate"); // NOI18N

        checkBoxDeveloperMode.setText(L10n.getString("prefs.label.developermode"));
        checkBoxDeveloperMode.setToolTipText(L10n.getString("tooltip.prefs.developermode"));
        checkBoxDeveloperMode.setName("checkBoxDeveloperMode"); // NOI18N

        javax.swing.GroupLayout dialogOptionsLayout = new javax.swing.GroupLayout(dialogOptions.getContentPane());
        dialogOptions.getContentPane().setLayout(dialogOptionsLayout);
        dialogOptionsLayout.setHorizontalGroup(
            dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dialogOptionsLayout.createSequentialGroup()
                .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dialogOptionsLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelChangeLanguageImplication, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(dialogOptionsLayout.createSequentialGroup()
                                .addGap(307, 307, 307)
                                .addComponent(buttonOk, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(dialogOptionsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(labelChooseLookAndFeel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelChooseLanguage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelHonFolder, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelModsFolder, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelCLArguments, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                            .addComponent(checkBoxIgnoreGameVersion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(checkBoxAutoUpdate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(checkBoxDeveloperMode, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(textFieldHonFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                            .addComponent(comboBoxChooseLanguage, javax.swing.GroupLayout.Alignment.LEADING, 0, 354, Short.MAX_VALUE)
                            .addComponent(textFieldModsFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                            .addComponent(comboBoxLafs, javax.swing.GroupLayout.Alignment.LEADING, 0, 354, Short.MAX_VALUE)
                            .addComponent(textFieldCLArguments, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(buttonModsFolder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonHonFolder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(buttonApplyLaf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        dialogOptionsLayout.setVerticalGroup(
            dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dialogOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelHonFolder)
                    .addComponent(textFieldHonFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonHonFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelModsFolder)
                    .addComponent(buttonModsFolder)
                    .addComponent(textFieldModsFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCLArguments)
                    .addComponent(textFieldCLArguments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelChooseLookAndFeel)
                    .addComponent(comboBoxLafs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonApplyLaf))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelChooseLanguage)
                    .addComponent(comboBoxChooseLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(checkBoxIgnoreGameVersion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkBoxAutoUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkBoxDeveloperMode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                .addComponent(labelChangeLanguageImplication)
                .addGap(7, 7, 7)
                .addGroup(dialogOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonOk)
                    .addComponent(buttonCancel))
                .addContainerGap())
        );

        rightClickTableMenu.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        rightClickTableMenu.setName("rightClickTableMenu"); // NOI18N

        popupItemMenuEnableDisableMod.setName("popupItemMenuEnableDisableMod"); // NOI18N
        rightClickTableMenu.add(popupItemMenuEnableDisableMod);

        popupItemMenuUpdateMod.setText(L10n.getString("button.updatemod"));
        popupItemMenuUpdateMod.setName("popupItemMenuUpdateMod"); // NOI18N
        rightClickTableMenu.add(popupItemMenuUpdateMod);

        popupItemMenuVisitWebsite.setText(L10n.getString("button.visitwebsite"));
        popupItemMenuVisitWebsite.setName("popupItemMenuVisitWebsite"); // NOI18N
        rightClickTableMenu.add(popupItemMenuVisitWebsite);

        popupItemMenuViewChangelog.setText(L10n.getString("button.viewchangelog"));
        popupItemMenuViewChangelog.setName("popupItemMenuViewChangelog"); // NOI18N
        rightClickTableMenu.add(popupItemMenuViewChangelog);

        popupItemMenuDeleteMod.setText(L10n.getString("button.deletemod"));
        popupItemMenuDeleteMod.setName("popupItemMenuDeleteMod"); // NOI18N
        rightClickTableMenu.add(popupItemMenuDeleteMod);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(L10n.getString("application.title"));
        setMinimumSize(new java.awt.Dimension(900, 650));
        setName("Form"); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        panelModList.setMinimumSize(new java.awt.Dimension(400, 250));
        panelModList.setName("panelModList"); // NOI18N
        panelModList.setPreferredSize(new java.awt.Dimension(925, 800));

        progressBar.setStringPainted(true);
        progressBar.setEnabled(false);
        progressBar.setName("progressBar"); // NOI18N

        buttonApplyMods.setText(L10n.getString("button.applymods"));
        buttonApplyMods.setToolTipText(L10n.getString("tooltip.button.apply"));
        buttonApplyMods.setName("buttonApplyMods"); // NOI18N

        buttonAddMod.setText(L10n.getString("button.addmod"));
        buttonAddMod.setToolTipText(L10n.getString("tooltip.button.addhonmod"));
        buttonAddMod.setName("buttonAddMod"); // NOI18N

        panelModChangelog.setBorder(javax.swing.BorderFactory.createTitledBorder(" "+L10n.getString("panel.details.label")+" "));
        panelModChangelog.setMinimumSize(new java.awt.Dimension(0, 250));
        panelModChangelog.setName("panelModChangelog"); // NOI18N
        panelModChangelog.setPreferredSize(new java.awt.Dimension(450, 420));

        labelModIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/resources/icon.png"))); // NOI18N
        labelModIcon1.setName("labelModIcon1"); // NOI18N

        labelModName1.setFont(labelModName1.getFont().deriveFont(labelModName1.getFont().getStyle() | java.awt.Font.BOLD, labelModName1.getFont().getSize()+1));
        labelModName1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelModName1.setText("mod name");
        labelModName1.setToolTipText("This is the Mod's name"); // NOI18N
        labelModName1.setName("labelModName1"); // NOI18N

        labelModAuthor1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelModAuthor1.setText("mod author");
        labelModAuthor1.setToolTipText("This is the Mod's author"); // NOI18N
        labelModAuthor1.setName("labelModAuthor1"); // NOI18N

        buttonViewModDetails.setText(L10n.getString("button.viewmoddetails"));
        buttonViewModDetails.setToolTipText("Return to the Mod details view");
        buttonViewModDetails.setName("buttonViewModDetails"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jEditorPane1.setContentType("text/html");
        jEditorPane1.setEditable(false);
        jEditorPane1.setName("jEditorPane1"); // NOI18N
        jScrollPane4.setViewportView(jEditorPane1);

        javax.swing.GroupLayout panelModChangelogLayout = new javax.swing.GroupLayout(panelModChangelog);
        panelModChangelog.setLayout(panelModChangelogLayout);
        panelModChangelogLayout.setHorizontalGroup(
            panelModChangelogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModChangelogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelModChangelogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                    .addGroup(panelModChangelogLayout.createSequentialGroup()
                        .addComponent(labelModIcon1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelModChangelogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelModAuthor1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelModName1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(buttonViewModDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelModChangelogLayout.setVerticalGroup(
            panelModChangelogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModChangelogLayout.createSequentialGroup()
                .addGroup(panelModChangelogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelModIcon1)
                    .addGroup(panelModChangelogLayout.createSequentialGroup()
                        .addComponent(labelModName1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelModAuthor1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonViewModDetails)
                .addContainerGap())
        );

        panelModDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(" "+L10n.getString("panel.details.label")+" "));
        panelModDetails.setMinimumSize(new java.awt.Dimension(0, 250));
        panelModDetails.setName("panelModDetails"); // NOI18N
        panelModDetails.setPreferredSize(new java.awt.Dimension(300, 420));

        labelModIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/resources/icon.png"))); // NOI18N
        labelModIcon.setName("labelModIcon"); // NOI18N

        labelModName.setFont(labelModName.getFont().deriveFont(labelModName.getFont().getStyle() | java.awt.Font.BOLD, labelModName.getFont().getSize()+1));
        labelModName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelModName.setText("mod name"); // NOI18N
        labelModName.setToolTipText(""); // NOI18N
        labelModName.setName("labelModName"); // NOI18N

        labelModAuthor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelModAuthor.setText("mod author");
        labelModAuthor.setToolTipText(""); // NOI18N
        labelModAuthor.setName("labelModAuthor"); // NOI18N

        panelModDescription.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        panelModDescription.setName("panelModDescription"); // NOI18N
        panelModDescription.setOpaque(false);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        areaModDesc.setBackground(new java.awt.Color(240, 240, 240));
        areaModDesc.setColumns(20);
        areaModDesc.setEditable(false);
        areaModDesc.setFont(buttonAddMod.getFont());
        areaModDesc.setLineWrap(true);
        areaModDesc.setRows(5);
        areaModDesc.setText("mod desc");
        areaModDesc.setWrapStyleWord(true);
        areaModDesc.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        areaModDesc.setMargin(new java.awt.Insets(5, 5, 5, 5));
        areaModDesc.setName("areaModDesc"); // NOI18N
        jScrollPane2.setViewportView(areaModDesc);

        labelRequirements.setFont(labelRequirements.getFont().deriveFont(labelRequirements.getFont().getStyle() | java.awt.Font.BOLD));
        labelRequirements.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelRequirements.setText(L10n.getString("label.requires"));

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        listRequirements.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        listRequirements.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listRequirements.setName("listRequirements"); // NOI18N
        listRequirements.setSelectionBackground(new java.awt.Color(212, 208, 200));
        listRequirements.setSelectionForeground(new java.awt.Color(51, 51, 51));
        jScrollPane3.setViewportView(listRequirements);

        javax.swing.GroupLayout panelModDescriptionLayout = new javax.swing.GroupLayout(panelModDescription);
        panelModDescription.setLayout(panelModDescriptionLayout);
        panelModDescriptionLayout.setHorizontalGroup(
            panelModDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelRequirements, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
        );
        panelModDescriptionLayout.setVerticalGroup(
            panelModDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelModDescriptionLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRequirements)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        buttonEnableMod.setText(L10n.getString("button.enablemod"));
        buttonEnableMod.setToolTipText(L10n.getString("tooltip.mod.button.enable"));
        buttonEnableMod.setName("buttonEnableMod"); // NOI18N

        buttonUpdateMod.setForeground(new java.awt.Color(60, 119, 207));
        buttonUpdateMod.setText(L10n.getString("button.updatemod"));
        buttonUpdateMod.setToolTipText(L10n.getString("tooltip.mod.button.update"));
        buttonUpdateMod.setName("buttonUpdateMod"); // NOI18N

        buttonVisitWebsite.setForeground(new java.awt.Color(60, 119, 207));
        buttonVisitWebsite.setText(L10n.getString("button.visitwebsite"));
        buttonVisitWebsite.setToolTipText(L10n.getString("tooltip.mod.button.website"));
        buttonVisitWebsite.setName("buttonVisitWebsite"); // NOI18N

        buttonViewChagelog.setText(L10n.getString("button.viewchangelog"));
        buttonViewChagelog.setToolTipText(L10n.getString("tooltip.mod.button.changelog"));
        buttonViewChagelog.setName("buttonViewChagelog"); // NOI18N

        javax.swing.GroupLayout panelModDetailsLayout = new javax.swing.GroupLayout(panelModDetails);
        panelModDetails.setLayout(panelModDetailsLayout);
        panelModDetailsLayout.setHorizontalGroup(
            panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelModDetailsLayout.createSequentialGroup()
                        .addComponent(labelModIcon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelModAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelModName, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelModDetailsLayout.createSequentialGroup()
                        .addGroup(panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(buttonVisitWebsite, 0, 0, Short.MAX_VALUE)
                            .addComponent(buttonEnableMod, javax.swing.GroupLayout.PREFERRED_SIZE, 123, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonUpdateMod, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonViewChagelog, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(panelModDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelModDetailsLayout.setVerticalGroup(
            panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModDetailsLayout.createSequentialGroup()
                .addGroup(panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelModIcon)
                    .addGroup(panelModDetailsLayout.createSequentialGroup()
                        .addComponent(labelModName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelModAuthor)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelModDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonEnableMod)
                    .addComponent(buttonUpdateMod))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelModDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonVisitWebsite)
                    .addComponent(buttonViewChagelog))
                .addContainerGap())
        );

        buttonLaunchHon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/resources/icon2.png"))); // NOI18N
        buttonLaunchHon.setToolTipText(L10n.getString("tooltip.button.applyandlaunch"));
        buttonLaunchHon.setMaximumSize(new java.awt.Dimension(26, 25));
        buttonLaunchHon.setMinimumSize(new java.awt.Dimension(26, 25));
        buttonLaunchHon.setName("buttonLaunchHon"); // NOI18N
        buttonLaunchHon.setPreferredSize(new java.awt.Dimension(26, 25));

        labelStatus.setFont(new java.awt.Font("Tahoma", 0, 15));
        labelStatus.setText("empty");
        labelStatus.setFocusable(false);
        labelStatus.setName("labelStatus"); // NOI18N

        modsTable.setName("modsTable"); // NOI18N

        javax.swing.GroupLayout panelModListLayout = new javax.swing.GroupLayout(panelModList);
        panelModList.setLayout(panelModListLayout);
        panelModListLayout.setHorizontalGroup(
            panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModListLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelModListLayout.createSequentialGroup()
                        .addComponent(buttonApplyMods)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonAddMod)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonLaunchHon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(modsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelModListLayout.createSequentialGroup()
                        .addComponent(panelModDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelModChangelog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, 0))
        );
        panelModListLayout.setVerticalGroup(
            panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelModListLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                    .addComponent(panelModDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                    .addComponent(panelModChangelog, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(panelModListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(buttonAddMod, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                                .addComponent(labelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(buttonApplyMods, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(buttonLaunchHon, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panelModDetails.getAccessibleContext().setAccessibleParent(panelModList);

        mainMenu.setName("mainMenu"); // NOI18N

        menuFile.setMnemonic(L10n.getMnemonic("menu.file"));
        menuFile.setText(L10n.getString("menu.file"));
        menuFile.setName("menuFile"); // NOI18N

        itemApplyMods.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        itemApplyMods.setMnemonic(L10n.getMnemonic("menu.file.applymods"));
        itemApplyMods.setText(L10n.getString("menu.file.applymods"));
        itemApplyMods.setName("itemApplyMods"); // NOI18N
        menuFile.add(itemApplyMods);

        itemApplyAndLaunch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        itemApplyAndLaunch.setMnemonic(L10n.getMnemonic("menu.file.applyandlaunch"));
        itemApplyAndLaunch.setText(L10n.getString("menu.file.applyandlaunch"));
        itemApplyAndLaunch.setName("itemApplyAndLaunch"); // NOI18N
        menuFile.add(itemApplyAndLaunch);

        itemUnapplyAllMods.setMnemonic(L10n.getMnemonic("menu.file.unapplymods"));
        itemUnapplyAllMods.setText(L10n.getString("menu.file.unapplymods"));
        itemUnapplyAllMods.setName("itemUnapplyAllMods"); // NOI18N
        menuFile.add(itemUnapplyAllMods);

        jSeparator1.setName("jSeparator1"); // NOI18N
        menuFile.add(jSeparator1);

        itemOpenModFolder.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        itemOpenModFolder.setMnemonic(L10n.getMnemonic("menu.file.openfolder"));
        itemOpenModFolder.setText(L10n.getString("menu.file.openfolder"));
        itemOpenModFolder.setName("itemOpenModFolder"); // NOI18N
        menuFile.add(itemOpenModFolder);

        itemImportFromOldModManager.setMnemonic(L10n.getMnemonic("menu.file.importoldmodmanager"));
        itemImportFromOldModManager.setText(L10n.getString("menu.file.importoldmodmanager"));
        itemImportFromOldModManager.setName("itemImportFromOldModManager"); // NOI18N
        menuFile.add(itemImportFromOldModManager);

        jSeparator3.setName("jSeparator3"); // NOI18N
        menuFile.add(jSeparator3);

        itemDownloadModUpdates.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        itemDownloadModUpdates.setMnemonic(L10n.getMnemonic("menu.file.downloadmodupdates"));
        itemDownloadModUpdates.setText(L10n.getString("menu.file.downloadmodupdates"));
        itemDownloadModUpdates.setName("itemDownloadModUpdates"); // NOI18N
        menuFile.add(itemDownloadModUpdates);

        jSeparator2.setName("jSeparator2"); // NOI18N
        menuFile.add(jSeparator2);

        itemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        itemExit.setMnemonic(L10n.getMnemonic("menu.file.exit"));
        itemExit.setText(L10n.getString("menu.file.exit"));
        itemExit.setName("itemExit"); // NOI18N
        menuFile.add(itemExit);

        mainMenu.add(menuFile);

        menuOptions.setMnemonic(L10n.getMnemonic("menu.options"));
        menuOptions.setText(L10n.getString("menu.options"));
        menuOptions.setName("menuOptions"); // NOI18N

        itemOpenPreferences.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        itemOpenPreferences.setText(L10n.getString("menu.options.preferences"));
        itemOpenPreferences.setName("itemOpenPreferences"); // NOI18N
        itemOpenPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemOpenPreferencesActionPerformed(evt);
            }
        });
        menuOptions.add(itemOpenPreferences);

        itemRefresh.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        itemRefresh.setText(L10n.getString("menu.options.refresh"));
        itemRefresh.setName("itemRefresh"); // NOI18N
        menuOptions.add(itemRefresh);

        mainMenu.add(menuOptions);

        menuView.setMnemonic(L10n.getMnemonic("menu.view"));
        menuView.setText(L10n.getString("menu.view"));
        menuView.setName("menuView"); // NOI18N

        viewModesGroup.add(itemViewDetails);
        itemViewDetails.setMnemonic(L10n.getMnemonic("menu.view.details"));
        itemViewDetails.setText(L10n.getString("menu.view.details"));
        itemViewDetails.setName("itemViewDetails"); // NOI18N
        itemViewDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemViewDetailsActionPerformed(evt);
            }
        });
        menuView.add(itemViewDetails);

        viewModesGroup.add(itemViewIcons);
        itemViewIcons.setMnemonic(L10n.getMnemonic("menu.view.icons"));
        itemViewIcons.setSelected(true);
        itemViewIcons.setText(L10n.getString("menu.view.icons"));
        itemViewIcons.setName("itemViewIcons"); // NOI18N
        itemViewIcons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemViewIconsActionPerformed(evt);
            }
        });
        menuView.add(itemViewIcons);

        viewModesGroup.add(itemViewDetailedIcons);
        itemViewDetailedIcons.setMnemonic(L10n.getMnemonic("menu.view.detailedicons"));
        itemViewDetailedIcons.setText(L10n.getString("menu.view.detailedicons"));
        menuView.add(itemViewDetailedIcons);

        viewModesGroup.add(itemViewTiles);
        itemViewTiles.setMnemonic(L10n.getMnemonic("menu.view.tiles"));
        itemViewTiles.setSelected(false);
        itemViewTiles.setText(L10n.getString("menu.view.tiles"));
        itemViewTiles.setName("itemViewTiles"); // NOI18N
        menuView.add(itemViewTiles);

        mainMenu.add(menuView);

        menuHelp.setMnemonic(L10n.getMnemonic("menu.help"));
        menuHelp.setText(L10n.getString("menu.help"));
        menuHelp.setName("menuHelp"); // NOI18N

        itemVisitForumThread.setMnemonic(L10n.getMnemonic("menu.help.website"));
        itemVisitForumThread.setText(L10n.getString("menu.help.website"));
        itemVisitForumThread.setName("itemVisitForumThread"); // NOI18N
        menuHelp.add(itemVisitForumThread);

        jSeparator4.setName("jSeparator4"); // NOI18N
        menuHelp.add(jSeparator4);

        itemAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        itemAbout.setMnemonic(L10n.getMnemonic("menu.help.about"));
        itemAbout.setText(L10n.getString("menu.help.about"));
        itemAbout.setName("itemAbout"); // NOI18N
        itemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemAboutActionPerformed(evt);
            }
        });
        menuHelp.add(itemAbout);

        mainMenu.add(menuHelp);

        setJMenuBar(mainMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelModList, javax.swing.GroupLayout.DEFAULT_SIZE, 956, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelModList, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-972)/2, (screenSize.height-733)/2, 972, 733);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Open Preferences dialog
     */
    private void itemOpenPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemOpenPreferencesActionPerformed
        // Set values in the options dialog
        //prefs = Preferences.userNodeForPackage(L10n.class);
        // Get selected language
        //String lang = prefs.get(model.PREFS_LOCALE, "DUMMY_DEFAULT");
        String lang = ManagerOptions.getInstance().getLanguage();
        if (lang.isEmpty()) {
            comboBoxChooseLanguage.setSelectedIndex(0);
        } else {
            comboBoxChooseLanguage.setSelectedItem(new Language("Language", lang));
        }
        // Get selected Laf
        //prefs = Preferences.userNodeForPackage(Manager.class);
        //String laf = prefs.get(model.PREFS_LAF, "DUMMY_DEFAULT");
        String laf = ManagerOptions.getInstance().getLaf();
        if (laf.isEmpty()) {
            comboBoxLafs.setSelectedIndex(0);
        } else {
            comboBoxLafs.setSelectedItem(new LaF("LaF", laf));
        }
        // Get CL arguments
        //String clArgs = prefs.get(model.PREFS_CLARGUMENTS, "DUMMY_DEFAULT");
        String clArgs = ManagerOptions.getInstance().getCLArgs();
        textFieldCLArguments.setText("");
        if (clArgs.isEmpty()) {
        } else {
            textFieldCLArguments.setText(clArgs);
        }
        // Get HoN folder
        //String honFolder = prefs.get(model.PREFS_HONFOLDER, "DUMMY_DEFAULT");
        String honFolder = ManagerOptions.getInstance().getGamePath();
        if (honFolder == null || honFolder.isEmpty()) {
            textFieldHonFolder.setText("");
        } else {
            textFieldHonFolder.setText(honFolder);
        }
        // Get Mods Folder
        String modsFolder = ManagerOptions.getInstance().getModPath();
        if (modsFolder == null || modsFolder.isEmpty()) {
            textFieldModsFolder.setText("");
        } else {
            textFieldModsFolder.setText(modsFolder);
        }
        checkBoxIgnoreGameVersion.setSelected(ManagerOptions.getInstance().isIgnoreGameVersion());
        checkBoxAutoUpdate.setSelected(ManagerOptions.getInstance().isAutoUpdate());
        checkBoxDeveloperMode.setSelected(ManagerOptions.getInstance().isDeveloperMode());
        dialogOptions.setSize(600, 410);
        dialogOptions.setLocationRelativeTo(this);
        dialogOptions.setVisible(true);
    }//GEN-LAST:event_itemOpenPreferencesActionPerformed

    /**
     * Open About dialog
     */
    private void itemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAboutActionPerformed
        ManagerAboutBox about = new ManagerAboutBox(this, ManagerOptions.getInstance());
        about.setLocation(this.getX() + 20, this.getY() + 20);
        about.setVisible(true);
    }//GEN-LAST:event_itemAboutActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        this.setResizable(true);
    }//GEN-LAST:event_formComponentShown

    private void itemViewIconsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemViewIconsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_itemViewIconsActionPerformed

    private void itemViewDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemViewDetailsActionPerformed
        
    }//GEN-LAST:event_itemViewDetailsActionPerformed

    /**
     * Display specified message to the user using JOptionPane
     * @param message message to be displayed
     * @param title title of the message dialog
     * @param type type of the mesage, see JOptionPane for list of types
     */
    public void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    public int confirmMessage(String message, String title, int type) {
        return JOptionPane.showConfirmDialog(this, message, title, type, JOptionPane.QUESTION_MESSAGE);
    }

    public void showDetailedMessage(String message, String title, String details, int type) {
        final JPanel panel = new JPanel(new BorderLayout(0, 10));

        final JToggleButton buttonDetails = new JToggleButton(L10n.getString("button.showdetails"), false);
        final JEditorPane detailsText = new JEditorPane();
        final JScrollPane scrollPane = new JScrollPane(detailsText);
        detailsText.setText(details);

        Object[] objs = {message, " ", panel};

        final JOptionPane pane = new JOptionPane(objs, type);
        final JDialog dialog = pane.createDialog(this, title);

        dialog.setResizable(true);

        buttonDetails.setMaximumSize(buttonDetails.getSize());

        panel.add(scrollPane, BorderLayout.PAGE_START);
        panel.add(buttonDetails, BorderLayout.LINE_END);

        detailsText.setVisible(false);
        scrollPane.setVisible(false);

        // TODO: Find a way to set a maximum size, and then the scrollpane will enter in action
        scrollPane.setMaximumSize(new Dimension(9999, 100));
        panel.setMaximumSize(new Dimension(dialog.getWidth(), 100));

        dialog.pack();

        buttonDetails.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (buttonDetails.isSelected()) {
                    buttonDetails.setText(L10n.getString("button.hidedetails"));
                    detailsText.setVisible(true);
                    scrollPane.setVisible(true);
                } else {
                    buttonDetails.setText(L10n.getString("button.showdetails"));
                    detailsText.setVisible(false);
                    scrollPane.setVisible(false);
                }
                // Double pack is necessary to work, don't know why
                dialog.pack();
                panel.revalidate();
                dialog.pack();
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Custom table model of the mod list table
     */
    private class ModTableModel extends DefaultTableModel {

        Class[] types = new Class[]{
            java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
        };
        boolean[] canEdit = new boolean[]{
            true, false, false, false, false
        };

        public ModTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (getRowCount() > 0 && getValueAt(0, columnIndex) != null) {
                return getValueAt(0, columnIndex).getClass();
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit[columnIndex];
        }
    }

    //TODO: Fix this for all views (icons, tiles and table).
    public void deleteSelectedMod() {
        Mod mod = ManagerGUI.getInstance().getModsTable().getSelectedMod();
        model.removeMod(mod);
        updateModTable();
    }

    /**
     * Update table with the list of mods
     */
    public void updateModTable() {
        animating = false;
        getModsTable().redraw();
        displayModDetail();
    }

    /**
     * Highlight the mods that is required to enable before or disable before the current select mod do
     * TODO: next release, right now don't bother
     */
    public void highlightRequiredMods() {
    }

    /**
     * Display details of the selected mod in the right panel
     */
    public void displayModDetail() {

        try {
            setStatusMessage("<html><font color=#009900>" + (model.getAppliedMods().size()) + "</font>/<font color=#0033cc>" + (model.getMods().size()) + "</font> " + L10n.getString("status.modsenabled") + " - Version: " + Game.getInstance().getVersion() + "</html>", false);
        } catch (Exception ex) {
            setStatusMessage("<html><font color=#009900>" + (model.getAppliedMods().size()) + "</font>/<font color=#0033cc>" + (model.getMods().size()) + "</font> " + L10n.getString("status.modsenabled") + "</html>", false);
        }
        
        if (modsTable.isEnabled()) {
            panelModChangelog.setVisible(false);
            panelModDetails.setVisible(true);
            // Make sure that items in the panel are visible
            setDetailsVisible(true);
            Mod mod = getSelectedMod();

            if (mod != null) {
                labelModName.setText(mod.getName());
                labelModAuthor.setText(mod.getAuthor());
                areaModDesc.setText(mod.getDescription());
                //labelVisitWebsite.setToolTipText(mod.getWebLink());
                if (mod.getUpdateCheckUrl() == null) {
                    buttonUpdateMod.setEnabled(false);
                } else {
                    buttonUpdateMod.setEnabled(true);
                }
                if (mod.getWebLink() != null && !mod.getWebLink().isEmpty()) {
                    buttonVisitWebsite.setEnabled(true);
                } else {
                    buttonVisitWebsite.setEnabled(false);
                }
                if (mod.getChangelog() != null && !mod.getChangelog().isEmpty()) {
                    buttonViewChagelog.setEnabled(true);
                    labelModAuthor1.setText(labelModAuthor.getText());
                    labelModName1.setText(labelModName.getText());
                    labelModIcon1.setIcon(labelModIcon.getIcon());
                    jEditorPane1.setText(BBCode.bbCodeToHtml(mod.getChangelog()));
                } else {
                    buttonViewChagelog.setEnabled(false);
                }
                buttonViewChagelog.setActionCommand("display changelog");
                buttonViewModDetails.setActionCommand("hide changelog");
                labelModIcon.setIcon(mod.getIcon());
                buttonUpdateMod.setActionCommand(mod.getName());
                buttonEnableMod.setActionCommand(mod.getName());
                if (mod.isEnabled()) {
                    buttonEnableMod.setForeground(Color.RED);
                    buttonEnableMod.setText(L10n.getString("button.disablemod"));
                    //labelModStatus.setText(L10n.getString("label.modstatus.enabled"));
                } else {
                    buttonEnableMod.setForeground(new Color(0, 175, 0));
                    buttonEnableMod.setText(L10n.getString("button.enablemod"));
                    //labelModStatus.setText(L10n.getString("label.modstatus.disabled"));
                }
                // Display mod incompatibility
                ArrayList<Action> reqs = new ArrayList<Action>();
                reqs.addAll(mod.getActions(Action.REQUIREMENT));
                DefaultListModel dlm = new DefaultListModel();
                String elem;
                for (Iterator actIter = reqs.iterator(); actIter.hasNext();) {
                    Action act = (Action) actIter.next();
                    if (act.getClass() == ActionRequirement.class) {
                        elem = ((ActionRequirement) act).getName();
                        if (((ActionRequirement) act).getVersion() != null) {
                            elem += " (ver. " + ((ActionRequirement) act).getVersion() + ")";
                        }
                        dlm.addElement(elem);
                    }
                }
                listRequirements.setModel(dlm);
            } else {
                setDetailsVisible(false);
            }
        }
    }



    /**
     * Display details of the given mod in the right panel
     * @param mod the mod to display in the details panel.
     */
    public void displayModDetail(Mod mod) {

        try {
            setStatusMessage("<html><font color=#009900>" + (model.getAppliedMods().size()) + "</font>/<font color=#0033cc>" + (model.getMods().size()) + "</font> " + L10n.getString("status.modsenabled") + " - Version: " + Game.getInstance().getVersion() + "</html>", false);
        } catch (Exception ex) {
            setStatusMessage("<html><font color=#009900>" + (model.getAppliedMods().size()) + "</font>/<font color=#0033cc>" + (model.getMods().size()) + "</font> " + L10n.getString("status.modsenabled") + "</html>", false);
        }
        if (getModsTable().isEnabled()) {
            panelModChangelog.setVisible(false);
            panelModDetails.setVisible(true);
            
            // Make sure that items in the panel are visible
            setDetailsVisible(true);

            labelModName.setText(mod.getName());
            labelModAuthor.setText(mod.getAuthor());
            areaModDesc.setText(mod.getDescription());
            //labelVisitWebsite.setToolTipText(mod.getWebLink());
            if (mod.getUpdateCheckUrl() == null) {
                buttonUpdateMod.setEnabled(false);
            } else {
                buttonUpdateMod.setEnabled(true);
            }
            if (mod.getWebLink() != null && !mod.getWebLink().isEmpty()) {
                buttonVisitWebsite.setEnabled(true);
            } else {
                buttonVisitWebsite.setEnabled(false);
            }
            if (mod.getChangelog() != null && !mod.getChangelog().isEmpty()) {
                buttonViewChagelog.setEnabled(true);
                labelModAuthor1.setText(labelModAuthor.getText());
                labelModName1.setText(labelModName.getText());
                labelModIcon1.setIcon(labelModIcon.getIcon());
                jEditorPane1.setText(BBCode.bbCodeToHtml(mod.getChangelog()));
            } else {
                buttonViewChagelog.setEnabled(false);
            }
            buttonViewChagelog.setActionCommand("display changelog");
            buttonViewModDetails.setActionCommand("hide changelog");
            labelModIcon.setIcon(mod.getIcon());
            buttonUpdateMod.setActionCommand(mod.getName());
            buttonEnableMod.setActionCommand(mod.getName());
            if (mod.isEnabled()) {
                buttonEnableMod.setForeground(Color.RED);
                buttonEnableMod.setText(L10n.getString("button.disablemod"));
                //labelModStatus.setText(L10n.getString("label.modstatus.enabled"));
            } else {
                buttonEnableMod.setForeground(new Color(0, 175, 0));
                buttonEnableMod.setText(L10n.getString("button.enablemod"));
                //labelModStatus.setText(L10n.getString("label.modstatus.disabled"));
            }
            // Display mod incompatibility
            ArrayList<Action> reqs = new ArrayList<Action>();
            reqs.addAll(mod.getActions(Action.REQUIREMENT));
            DefaultListModel dlm = new DefaultListModel();
            String elem;
            for (Iterator actIter = reqs.iterator(); actIter.hasNext();) {
                Action act = (Action) actIter.next();
                if (act.getClass() == ActionRequirement.class) {
                    elem = ((ActionRequirement) act).getName();
                    if (((ActionRequirement) act).getVersion() != null) {
                        elem += " (ver. " + ((ActionRequirement) act).getVersion() + ")";
                    }
                    dlm.addElement(elem);
                }
            }
            listRequirements.setModel(dlm);
        } else {
            setDetailsVisible(false);
        }
        
    }

    /**
     *  Method used for updating the view (called when the model has changed and
     *  notifyObservers() was called)
     */
    public void update(Observable obs, Object obj) {
        if (fullyLoaded) {
            logger.info("List of mods has changed, updating...");
            updateModTable();
            highlightRequiredMods();
        }
    }

    /**
     * Change visibility of components on the mod details panel
     * @param visible true to make them visible, false to make them invisible
     */
    public void setDetailsVisible(boolean visible) {
        labelModIcon.setVisible(visible);
        labelModName.setVisible(visible);
        labelModAuthor.setVisible(visible);
        areaModDesc.setVisible(visible);
        labelRequirements.setVisible(visible);
        listRequirements.setVisible(visible);
        buttonUpdateMod.setVisible(visible);
        buttonEnableMod.setVisible(visible);
        buttonViewChagelog.setVisible(visible);
        jScrollPane3.setVisible(visible);
    }

    /**
     * This method calls in all the components the setEanbled method to avoid input on the screen.
     */
    public void setInputEnabled(boolean enabled) {
        panelModList.setEnabled(enabled);
        mainMenu.setEnabled(enabled);
        buttonAddMod.setEnabled(enabled);
        buttonApplyMods.setEnabled(enabled);
        buttonEnableMod.setEnabled(enabled);
        buttonLaunchHon.setEnabled(enabled);
        buttonUpdateMod.setEnabled(enabled);
        buttonViewChagelog.setEnabled(enabled);
        buttonViewModDetails.setEnabled(enabled);
        buttonVisitWebsite.setEnabled(enabled);
        itemAbout.setEnabled(enabled);
        itemApplyAndLaunch.setEnabled(enabled);
        itemApplyMods.setEnabled(enabled);
        itemDownloadModUpdates.setEnabled(enabled);
        itemExit.setEnabled(enabled);
        itemImportFromOldModManager.setEnabled(enabled);
        itemOpenModFolder.setEnabled(enabled);
        itemOpenPreferences.setEnabled(enabled);
        itemRefresh.setEnabled(enabled);
        itemUnapplyAllMods.setEnabled(enabled);
        itemViewDetails.setEnabled(enabled);
        itemViewIcons.setEnabled(enabled);
        itemVisitForumThread.setEnabled(enabled);
        itemViewDetailedIcons.setEnabled(enabled);
        itemViewTiles.setEnabled(enabled);
        modsTable.setEnabled(enabled);
        if (enabled && fullyLoaded) {
            displayModDetail();
        }
    }

    public void setStatusMessage(String status, boolean animate) {
        animating = animate;
        labelStatus.setText(status);
        if (animate) {
            Task task = new Task<Void, Void>(Application.getInstance()) {
                private int dots = 0;
                private String originalMessage;

                @Override
                protected Void doInBackground() throws Exception {
                    originalMessage = labelStatus.getText();
                    while (animating) {
                        if (dots < 3) {
                            labelStatus.setText(labelStatus.getText() + ".");
                            dots++;
                        } else {
                            labelStatus.setText(originalMessage);
                            dots = 0;
                        }
                        Thread.sleep(700);
                    }
                    return null;
                }
            };
            task.execute();
        }
    }

    /*
     * The following methods add listeners to the UI components
     */
    public void buttonAddModAddActionListener(ActionListener al) {
        buttonAddMod.addActionListener(al);
    }

    public JMenuItem getItemRefreshManager() {
        return itemRefresh;
    }

    public JMenuItem getItemViewDetailedIcons() {
        return itemViewDetailedIcons;
    }

    public JMenuItem getItemViewDetails() {
        return itemViewDetails;
    }

    public JMenuItem getItemViewIcons() {
        return itemViewIcons;
    }

    public JMenuItem getItemViewTiles() {
        return itemViewTiles;
    }

    public void buttonEnableModAddActionListener(ActionListener al) {
        buttonEnableMod.addActionListener(al);
    }

    public void itemApplyModsAddActionListener(ActionListener al) {
        itemApplyMods.addActionListener(al);
        buttonApplyMods.addActionListener(al);
    }

    public void buttonVisitWebsiteAddActionListener(ActionListener al) {
        buttonVisitWebsite.addActionListener(al);
    }

    public void itemApplyAndLaunchAddActionListener(ActionListener al) {
        itemApplyAndLaunch.addActionListener(al);
    }

    public void itemUnapplyAllModsAddActionListener(ActionListener al) {
        itemUnapplyAllMods.addActionListener(al);
    }

    public void buttonUpdateModActionListener(ActionListener al) {
        buttonUpdateMod.addActionListener(al);
    }

    public void itemOpenModFolderAddActionListener(ActionListener al) {
        itemOpenModFolder.addActionListener(al);
    }

    public void itemDownloadModUpdates(ActionListener al) {
        itemDownloadModUpdates.addActionListener(al);
    }

    public void itemVisitForumThreadAddActionListener(ActionListener al) {
        itemVisitForumThread.addActionListener(al);
    }

    public void itemViewDetailsAddActionListener(ActionListener al) {
        itemViewDetails.addActionListener(al);
    }

    public void itemViewIconsAddActionListener(ActionListener al) {
        itemViewIcons.addActionListener(al);
    }

    void itemViewDetailedIconsAddActionListener(ActionListener al) {
        itemViewDetailedIcons.addActionListener(al);
    }

    public void itemViewTilesAddActionListener(ActionListener al) {
        itemViewTiles.addActionListener(al);
    }

    public ModsTable getModsTable() {
        return modsTable;
    }

    public void itemExitAddActionListener(ActionListener al) {
        itemExit.addActionListener(al);
    }

    public void buttonApplyLafAddActionListener(ActionListener al) {
        buttonApplyLaf.addActionListener(al);
    }

    public void buttonOkAddActionListener(ActionListener al) {
        buttonOk.addActionListener(al);
    }

    public void buttonCancelAddActionListener(ActionListener al) {
        buttonCancel.addActionListener(al);
    }

    public void buttonHonFolderAddActionListener(ActionListener al) {
        buttonHonFolder.addActionListener(al);
    }

    public void buttonModsFolderAddActionListener(ActionListener al) {
        buttonModsFolder.addActionListener(al);
    }

    public void popupMenuItemEnableDisableModAddActionListener(ActionListener al) {
        popupItemMenuEnableDisableMod.addActionListener(al);
    }

    public void popupMenuItemViewChangelogAddActionListener(ActionListener al) {
        popupItemMenuViewChangelog.addActionListener(al);
    }

    public void popupMenuItemUpdateModAddActionListener(ActionListener al) {
        popupItemMenuUpdateMod.addActionListener(al);
    }

    public void popupMenuItemVisitWebsiteAddActionListener(ActionListener al) {
        popupItemMenuVisitWebsite.addActionListener(al);
    }

    public void popupItemMenuDeleteModAddActionListener(ActionListener al) {
        popupItemMenuDeleteMod.addActionListener(al);
    }

    public void itemImportFromOldModManagerAddActionListener(ActionListener al) {
        itemImportFromOldModManager.addActionListener(al);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /*
     * Various getters and setters
     */
    public String getTextFieldHonFolder() {
        return textFieldHonFolder.getText();
    }

    public void setTextFieldHonFolder(String txt) {
        textFieldHonFolder.setText(txt);
    }

    public void setTextFieldModsFolder(String txt) {
        textFieldModsFolder.setText(txt);
    }

    public String getTextFieldModsFolder() {
        return textFieldModsFolder.getText();
    }

    public boolean getIgnoreGameVersion() {
        return checkBoxIgnoreGameVersion.isSelected();
    }

    public boolean getAutoUpdate() {
        return checkBoxAutoUpdate.isSelected();
    }

    public boolean getDeveloperMode() {
        return checkBoxDeveloperMode.isSelected();
    }

    public String getSelectedLafClass() {
        return ((LaF) comboBoxLafs.getSelectedItem()).getLafClass();
    }

    public String getSelectedLanguage() {
        return ((Language) comboBoxChooseLanguage.getSelectedItem()).getCode();
    }

    public JDialog getPrefsDialog() {
        return dialogOptions;
    }

    public JPanel getPanelModDetails() {
        return panelModDetails;
    }

    public JMenuItem getItemOpenPreferences() {
        return itemOpenPreferences;
    }

    public JPanel getPanelModChangelog() {
        return panelModChangelog;
    }

    public JPopupMenu getRightClickTableMenu() {
        return rightClickTableMenu;
    }

    public JButton getButtonViewModDetails() {
        return buttonViewModDetails;
    }

    public JButton getButtonViewChagelog() {
        return buttonViewChagelog;
    }

    public Mod getSelectedMod() {
        return getModsTable().getSelectedMod();
    }

    public String getSelectedHonFolder() {
        return textFieldHonFolder.getText();
    }

    public JButton getButtonLaunchHon() {
        return buttonLaunchHon;
    }

    public JButton getButtonApplyMods() {
        return buttonApplyMods;
    }

    public String getCLArguments() {
        return textFieldCLArguments.getText();
    }

    /**
     * Class of items in the Select LaF combo box on preferences dialog
     */
    private class LaF {

        private String name;
        private String lafClass;

        public LaF(String _name, String _lafClass) {
            name = _name;
            lafClass = _lafClass;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getLafClass() {
            return lafClass;
        }

        @Override
        public boolean equals(Object laf) {
            if (lafClass.equals(((LaF) laf).lafClass)) {
                return true;
            }
            return false;
        }
    }

    /**
     * Class to renderer cells in the Icons View list.
     */
    private class IconsListCellRenderer extends DefaultListCellRenderer {

        public IconsListCellRenderer() {}

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            setIcon(((Mod) value).getIcon());
            setText(((Mod) value).getName());

            // Grays out the icons
            setEnabled(((Mod) value).isEnabled());
            return this;
        }
    }

    /**
     * Class of items in the Select language combo box on preferences dialog
     */
    private class Language {

        private String name;
        private String code;

        public Language(String _name, String _code) {
            name = _name;
            code = _code;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getCode() {
            return code;
        }

        @Override
        public boolean equals(Object lang) {
            if (code.equals(((Language) lang).code)) {
                return true;
            }
            return false;
        }
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaModDesc;
    private javax.swing.JButton buttonAddMod;
    private javax.swing.JButton buttonApplyLaf;
    private javax.swing.JButton buttonApplyMods;
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonEnableMod;
    private javax.swing.JButton buttonHonFolder;
    private javax.swing.JButton buttonLaunchHon;
    private javax.swing.JButton buttonModsFolder;
    private javax.swing.JButton buttonOk;
    private javax.swing.JButton buttonUpdateMod;
    private javax.swing.JButton buttonViewChagelog;
    private javax.swing.JButton buttonViewModDetails;
    private javax.swing.JButton buttonVisitWebsite;
    private javax.swing.JCheckBox checkBoxAutoUpdate;
    private javax.swing.JCheckBox checkBoxDeveloperMode;
    private javax.swing.JCheckBox checkBoxIgnoreGameVersion;
    private javax.swing.JComboBox comboBoxChooseLanguage;
    private javax.swing.JComboBox comboBoxLafs;
    private javax.swing.JDialog dialogOptions;
    private javax.swing.JMenuItem itemAbout;
    private javax.swing.JMenuItem itemApplyAndLaunch;
    private javax.swing.JMenuItem itemApplyMods;
    private javax.swing.JMenuItem itemDownloadModUpdates;
    private javax.swing.JMenuItem itemExit;
    private javax.swing.JMenuItem itemImportFromOldModManager;
    private javax.swing.JMenuItem itemOpenModFolder;
    private javax.swing.JMenuItem itemOpenPreferences;
    private javax.swing.JMenuItem itemRefresh;
    private javax.swing.JMenuItem itemUnapplyAllMods;
    private javax.swing.JRadioButtonMenuItem itemViewDetailedIcons;
    private javax.swing.JRadioButtonMenuItem itemViewDetails;
    private javax.swing.JRadioButtonMenuItem itemViewIcons;
    private javax.swing.JRadioButtonMenuItem itemViewTiles;
    private javax.swing.JMenuItem itemVisitForumThread;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JLabel labelCLArguments;
    private javax.swing.JLabel labelChangeLanguageImplication;
    private javax.swing.JLabel labelChooseLanguage;
    private javax.swing.JLabel labelChooseLookAndFeel;
    private javax.swing.JLabel labelHonFolder;
    private javax.swing.JLabel labelModAuthor;
    private javax.swing.JLabel labelModAuthor1;
    private javax.swing.JLabel labelModIcon;
    private javax.swing.JLabel labelModIcon1;
    private javax.swing.JLabel labelModName;
    private javax.swing.JLabel labelModName1;
    private javax.swing.JLabel labelModsFolder;
    private javax.swing.JLabel labelRequirements;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JList listRequirements;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenu menuOptions;
    private javax.swing.JMenu menuView;
    private gui.ModsTable modsTable;
    private javax.swing.JPanel panelModChangelog;
    private javax.swing.JPanel panelModDescription;
    private javax.swing.JPanel panelModDetails;
    private javax.swing.JPanel panelModList;
    private javax.swing.JMenuItem popupItemMenuDeleteMod;
    private javax.swing.JMenuItem popupItemMenuEnableDisableMod;
    private javax.swing.JMenuItem popupItemMenuUpdateMod;
    private javax.swing.JMenuItem popupItemMenuViewChangelog;
    private javax.swing.JMenuItem popupItemMenuVisitWebsite;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPopupMenu rightClickTableMenu;
    private javax.swing.JTextField textFieldCLArguments;
    private javax.swing.JTextField textFieldHonFolder;
    private javax.swing.JTextField textFieldModsFolder;
    // End of variables declaration//GEN-END:variables
}
