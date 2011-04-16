/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import business.ManagerOptions;
import business.Mod;
import business.ModList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import utility.FileUtils;
import utility.XML;
import utility.ZIP;

/**
 *
 * @author Shirkit
 */
public class ProfileMenu extends JMenu implements Observer {

    ModList profile;
    JMenuItem loadItem;
    JMenuItem deleteItem;
    JMenuItem exportItem;
    JMenu parent;
    ProfileMenu me;
    boolean enabled;

    public ProfileMenu(ModList profile, JMenu parent) {
        super(profile.getName());
        enabled = false;
        loadItem = new JMenuItem("Load");
        deleteItem = new JMenuItem("Delete");
        exportItem = new JMenuItem("Export");
        loadItem.addActionListener(new LoadListener());
        deleteItem.addActionListener(new DeleteListener());
        exportItem.addActionListener(new ExportListener());
        this.add(loadItem);
        this.add(deleteItem);
        this.add(exportItem);
        me = this;
        parent.add(me);
        parent.repaint();
    }

    public void load() {
        loadItem.doClick();
    }

    public void update(Observable o, Object arg) {
        if (arg.equals(profile)) {
            if (enabled) {
                me.setName(getOriginalName());
                me.repaint();
                Iterator<Mod> list = ManagerOptions.getInstance().getMods().iterator();
                profile.clearModList();
                while (list.hasNext()) {
                    Mod mod = list.next();
                    if (mod.isEnabled()) {
                        profile.addMod(mod);
                    }
                }
                enabled = false;
            }
        }
    }

    class LoadListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (!ManagerOptions.getInstance().getCurrentProfile().equals(profile)) {
                Iterator<Mod> mods = profile.getModList();
                ArrayList<Mod> done = new ArrayList<Mod>();
                while (mods.hasNext()) {
                    Mod mod = mods.next();
                    Mod toEnable = ManagerOptions.getInstance().getMod(mod.getName(), mod.getVersion() + "-*");
                    if (toEnable != null) {
                        toEnable.enable();
                        done.add(mod);
                    }
                }
                mods = ManagerOptions.getInstance().getMods().iterator();
                while (mods.hasNext()) {
                    Mod mod = mods.next();
                    if (!done.contains(mod)) {
                        mod.disable();
                    }
                }
                ManagerOptions.getInstance().setCurrentProfile(profile);
                enabled = true;
                me.setName("<html><strong>" + me.getName() + "</html></strong>");
                ManagerGUI.getInstance().getModsTable().redraw();
            }
        }
    }

    class DeleteListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (!me.equals(ManagerOptions.getInstance().getCurrentProfile())) {
                parent.remove(me);
                ManagerOptions.getInstance().getProfiles().remove(profile);
            } else {
                JOptionPane.showMessageDialog(ManagerGUI.getInstance(), "Can't remove a profile that's being used.");
            }
        }
    }

    class ExportListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            File tempFolder = FileUtils.generateTempFolder(true);
            ModList modlist = new ModList();
            Iterator<Mod> mods = ManagerOptions.getInstance().getAppliedMods().iterator();
            JFileChooser jfc = new JFileChooser();
            jfc.setMultiSelectionEnabled(false);
            jfc.setDialogTitle("Please, indicate where you want to save");
            jfc.setDialogType(JFileChooser.SAVE_DIALOG);
            int response = jfc.showSaveDialog(ManagerGUI.getInstance());
            if (response == JFileChooser.APPROVE_OPTION) {
                File destination = jfc.getSelectedFile();
                while (mods.hasNext()) {
                Mod m = mods.next();
                if (m.getUpdateDownloadUrl() != null && !m.getUpdateDownloadUrl().isEmpty()) {
                    modlist.addMod(m);
                } else if (m.getPath() != null && !m.getPath().isEmpty()) {
                    Mod mod = new Mod();
                    mod.copy(m);
                    mod.setPath("%ZIP%/" + new File(mod.getPath()).getName());
                    modlist.addMod(mod);
                    try {
                        FileUtils.copyFile(new File(m.getPath()), new File(tempFolder, new File(m.getPath()).getName()));
                    } catch (FileNotFoundException ex) {
                    } catch (IOException ex) {
                    }
                }
            }
            try {
                XML.modListToXml(new File(tempFolder, ModList.MODLIST_FILENAME), modlist);
            } catch (IOException ex) {
                Logger.getLogger(ProfileMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
                try {
                    ZIP.createZIP(tempFolder.getAbsolutePath(), destination.getAbsolutePath());
                } catch (FileNotFoundException ex) {
                } catch (ZipException ex) {
                } catch (IOException ex) {
                }
            }
        }
    }

    public String getOriginalName() {
        return this.profile.getName();
    }
}
