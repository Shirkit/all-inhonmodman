/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import manager.Manager;
import utility.XML;

/**
 *
 * @author Shirkit
 */
@XStreamAlias("options")
public class ManagerOptions {

    @XStreamAlias("manager_folder")
    @XStreamAsAttribute
    private String MANAGER_FOLDER;
    @XStreamAlias("mods_folder")
    @XStreamAsAttribute
    private String MODS_FOLDER;
    @XStreamAlias("hon_folder")
    @XStreamAsAttribute
    private String HON_FOLDER;
    @XStreamAsAttribute
    @XStreamAlias("applied")
    private Set<Mod> applied;

    public boolean saveOptions(File path) throws IOException {
        boolean success = true;

        Manager manager = Manager.getInstance();

        this.setAppliedMods(manager.getAppliedMods());
        this.setGamePath(manager.getModPath());
        this.setManagerPath(getManagerPath());
        this.setModPath(manager.getModPath());

        /*
        ArrayList<Mod> list3 = manager.getMods(); // manager.getAppliedMods() in future

        // this is going to be shortened when manager.getAppliedMods() works
        for (int i = 0; i < list3.size(); i++) {
            if (list3.get(i).isEnabled()) {
                Mod m = new Mod(list3.get(i).getName(), list3.get(i).getVersion(), list3.get(i).getAuthor());
                this.applied.add(m);
            }
        }
        */

        XStream xstream = new XStream(XML.getDriver());
        XML.updateAlias(xstream);

        if (path.exists()) {
            if (!path.delete()) {
                success = false;
            }
        }

        FileOutputStream fos = new FileOutputStream(path);
        fos.write((XML.replaceInvalidHtmlChars(xstream.toXML(this))).getBytes("UTF-8"));

        return success;
    }

    public boolean loadOptions(File path) throws FileNotFoundException {
    	if(!path.exists())
    		return false;
    	
        XStream xstream = new XStream(XML.getDriver());
        xstream = XML.updateAlias(xstream);

        ManagerOptions tmp = (ManagerOptions) xstream.fromXML(new FileInputStream(path));
        this.setGamePath(tmp.getGamePath());
        this.setManagerPath(tmp.getManagerPath());
        this.setModPath(tmp.getModPath());
        this.setAppliedMods(tmp.getAppliedMods());

        Manager manager = Manager.getInstance();

        manager.setGamePath(tmp.getGamePath());
        manager.setManagerPath(tmp.getManagerPath());
        manager.setModPath(tmp.getModPath());

        return true;

    }

    public void setModPath(String p) {
        MODS_FOLDER = p;
    }

    public void setGamePath(String p) {
        HON_FOLDER = p;
    }

    public void setManagerPath(String p) {
        MANAGER_FOLDER = p;
    }
    
    public void setAppliedMods(Set<Mod> list) {
    	applied = list;
    }

    public String getModPath() {
        return MODS_FOLDER;
    }

    public String getGamePath() {
        return HON_FOLDER;
    }

    public String getManagerPath() {
        return MANAGER_FOLDER;
    }

    public Set<Mod> getAppliedMods() {
        return applied;
    }
}
