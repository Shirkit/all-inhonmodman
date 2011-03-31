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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

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

    public ProfileMenu(ModList profile, JMenu parent) {
        super(profile.getName());
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

    public void update(Observable o, Object arg) {
        if ("profile".equals(arg)) {
            if (!ManagerOptions.getInstance().getCurrentProfile().equals(profile)) {
                me.setName(getOriginalName());
                me.repaint();
            }
        }
    }

    class LoadListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
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
            me.setName("<html><strong>"+me.getName()+"</html></strong>");
            ManagerGUI.getInstance().getModsTable().redraw();
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
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public String getOriginalName() {
        return this.profile.getName();
    }
}
