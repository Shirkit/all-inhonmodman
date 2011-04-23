/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.io.FileNotFoundException;
import java.io.IOException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipException;
import utility.FileUtils;
import utility.XML;
import utility.ZIP;

/**
 *
 * @author Administrador
 */
@XStreamAlias("mod-list")
public class ModList {

    @XStreamAsAttribute
    private String listVersion = "1.0";
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String description;
    @XStreamImplicit
    ArrayList<ModListNode> modList;
    @XStreamOmitField
    public static final String MODLIST_FILENAME = "list.xml";

    public ModList(ArrayList<ModListNode> modList) {
        if (modList == null) {
            this.modList = new ArrayList<ModListNode>();
        } else {
            this.modList = modList;
        }
    }

    public ModList() {
        modList = new ArrayList<ModListNode>();
    }

    public void setModList(ArrayList<ModListNode> modList) {
        this.modList = modList;
    }

    public Iterator<ModListNode> getModList() {
        return modList.iterator();
    }

    /**
     * 
     * @param mod
     * @return true if the mod needs to be compressed; false if not.
     */
    public boolean addMod(Mod mod) {
        boolean bool = mod.getUpdateDownloadUrl() == null || mod.getUpdateDownloadUrl().isEmpty();
        ModListNode add = new ModListNode(mod.getName(), mod.getVersion(), mod.getUpdateDownloadUrl(), bool, bool ? new File(mod.getPath()).getName() : null);
        modList.add(add);
        return bool;
    }

    /**
     * 
     * @param mod
     * @return true if the mod was found and removed; false otherwise.
     */
    public boolean removeMod(Mod mod) {
        Iterator<ModListNode> it = getModList();
        while (it.hasNext()) {
            ModListNode modListNode = it.next();
            if (modListNode.getName().equals(mod.getName()) && modListNode.getVersion().equals(mod.getVersion())) {
                modList.remove(modListNode);
                return true;
            }
        }
        return false;
    }

    public void clearModList() {
        modList.clear();
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

    public void exportToFile(File destination) throws FileNotFoundException, IOException {
        if (destination.exists()) {
            FileUtils.updatePermissions(destination);
        }
        File tempFolder = FileUtils.generateTempFolder(true);
        Iterator<ModListNode> it = getModList();

        while (it.hasNext()) {
            ModListNode next = it.next();
            if (next.isCompressed()) {
                Mod m = ManagerOptions.getInstance().getMod(next.getName(), next.getVersion()+"-*");
                FileUtils.copyFile(new File(m.getPath()), new File(tempFolder, new File(m.getPath()).getName()));
            }
        }
        XML.modListToXml(new File(tempFolder, ModList.MODLIST_FILENAME), this);
        ZIP.createZIP(tempFolder.getAbsolutePath(), destination.getAbsolutePath());
    }
    
    public static ModList importFromFile(File source) throws FileNotFoundException, ZipException, IOException {
        if (!source.exists()) {
            throw new FileNotFoundException(source.getAbsolutePath());
        }
        byte[] file = ZIP.getFile(source, MODLIST_FILENAME);
        
        return XML.xmlToModList(file);
    }
}
