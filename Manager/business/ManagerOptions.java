/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
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
    @XStreamOmitField
    private Set<Mod> mods;
    @XStreamOmitField
    private String OPTIONS_FILENAME = "managerOptions.xml";
    @XStreamOmitField
    private String HOMEPAGE = "http://sourceforge.net/projects/all-inhonmodman";
    @XStreamOmitField
    private String VERSION = "0.1 BETA";

    private static ManagerOptions instance;
    
    private ManagerOptions() {

    }

    public static ManagerOptions getInstance() {
        if (instance == null) {
            instance = new ManagerOptions();
        }
        return instance;
    }

    public boolean saveOptions(File path) throws IOException {
        boolean success = true;

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

    public void loadOptions() throws FileNotFoundException {
            
        XStream xstream = new XStream(XML.getDriver());
        xstream = XML.updateAlias(xstream);

        ManagerOptions tmp = (ManagerOptions) xstream.fromXML(new FileInputStream(MANAGER_FOLDER + File.separator + OPTIONS_FILENAME));
        this.setGamePath(tmp.getGamePath());
        this.setManagerPath(tmp.getManagerPath());
        this.setModPath(tmp.getModPath());
        this.setAppliedMods(tmp.getAppliedMods());
    }

    /**
     *
     * @param p
     */
    public void setModPath(String p) {
        MODS_FOLDER = p;
    }

    /**
     *
     * @param p
     */
    public void setGamePath(String p) {
        HON_FOLDER = p;
    }

    /**
     *
     * @param p
     */
    public void setManagerPath(String p) {
        MANAGER_FOLDER = p;
    }

    /**
     *
     * @return
     */
    public String getModPath() {
        return MODS_FOLDER;
    }

    /**
     *
     * @return
     */
    public String getGamePath() {
        return HON_FOLDER;
    }

    /**
     * 
     * @return
     */
    public String getManagerPath() {
        return MANAGER_FOLDER;
    }

    /**
     *
     * @param list
     */
    public void setAppliedMods(Set<Mod> list) {
    	applied = list;
    }

    /**
     *
     * @return
     */
    public Set<Mod> getAppliedMods() {
        return applied;
    }

    /**
     *
     * @return
     */
    public Set<Mod> getMods() {
        return mods;
    }

    /**
     * 
     * @param mods
     */
    public void setMods(Set<Mod> mods) {
        this.mods = mods;
    }
}
