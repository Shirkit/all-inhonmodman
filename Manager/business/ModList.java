/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import utility.xml.converters.ModListConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Administrador
 */
@XStreamAlias("mod-list")
@XStreamConverter(ModListConverter.class)
public class ModList {

    ArrayList<Mod> modList;
    private String listVersion = "1.0";
    private String name;
    private String description;
    public static final String MODLIST_FILENAME = "list.xml";

    public ModList(ArrayList<Mod> modList) {
        if (modList == null) {
            this.modList = new ArrayList<Mod>();
        } else {
            this.modList = modList;
        }
    }

    public ModList() {
        modList = new ArrayList<Mod>();
    }

    public void setModList(ArrayList<Mod> modList) {
        this.modList = modList;
    }

    public Iterator<Mod> getModList() {
        return modList.iterator();
    }

    public void addMod(Mod mod) {
        modList.add(mod);
    }

    public String getListVersion() {
        return listVersion;
    }

    public void setListVersion(String listVersion) {
        this.listVersion = listVersion;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
