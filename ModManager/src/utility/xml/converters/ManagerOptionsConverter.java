package utility.xml.converters;

import business.ManagerOptions;
import business.Mod;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is used by the XStream to convert a ManagerOptions into a XML file, or the opposite.
 * @author Shirkit
 */
public class ManagerOptionsConverter implements Converter {

    /**
     * XStream uses this method to convert an ManagerOptions instance into a XML String.
     */
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ManagerOptions opt = ManagerOptions.getInstance();
        try {
            writer.addAttribute("honfolder", opt.getGamePath());
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("modsfolder", opt.getModPath());
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("lang", opt.getLanguage());
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("laf", opt.getLaf());
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("clargs", opt.getCLArgs());
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("x", Integer.toString((int) opt.getGuiRectangle().getX()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("y", Integer.toString((int) opt.getGuiRectangle().getY()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("width", Integer.toString((int) opt.getGuiRectangle().getWidth()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("height", Integer.toString((int) opt.getGuiRectangle().getHeight()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("ignoregameversion", Boolean.toString(opt.isIgnoreGameVersion()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("autoupdate", Boolean.toString(opt.isAutoUpdate()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("developermode", Boolean.toString(opt.isDeveloperMode()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("deletefoldertree", Boolean.toString(opt.isDeleteFolderTree()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("viewtype", opt.getViewType().toString());
        } catch (NullPointerException ex) {
        }
        try {
            if (!opt.getLastHonVersion().isEmpty()) {
                writer.addAttribute("lasthonversion", opt.getLastHonVersion());
            }
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("colorcheckboxes",  Boolean.toString(opt.getCheckboxesInTableColored()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("showicons", Boolean.toString(opt.iconsShownInTable()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("smallicons", Boolean.toString(opt.usingSmallIcons()));
        } catch (NullPointerException ex) {
        }
        try {
            writer.addAttribute("columnsorder", opt.getColumnsOrder());
        } catch (NullPointerException ex) {
        }
        
        if (opt.getColumnsWidth() != null) {
            Iterator<Integer> it = opt.getColumnsWidth().iterator();
            int i = 0;
            while (it.hasNext()) {
                writer.addAttribute("columns-" + i, Integer.toString(it.next()));
                i++;
            }
        }

        if (!(opt.getAppliedMods() == null)) {
            Iterator<Mod> it = opt.getAppliedMods().iterator();
            while (it.hasNext()) {
                Mod m = it.next();
                if (m != null) {
                    Mod n = new Mod(m.getName(), m.getVersion(), m.getAuthor());
                    writer.startNode("modification");
                    mc.convertAnother(n);
                    writer.endNode();
                }
            }
        }
    }

    /**
     * XStream uses this method to convert a XML String into a  ManagerOptions instance.
     */
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ManagerOptions value = ManagerOptions.getInstance();

        try {
            value.setModPath(reader.getAttribute("modsfolder"));
        } catch (Exception e) {
        }
        try {
            value.setGamePath(reader.getAttribute("honfolder"));
        } catch (Exception e) {
        }
        try {
            value.setCLArgs(reader.getAttribute("clargs"));
        } catch (Exception e) {
        }
        try {
            value.setLanguage(reader.getAttribute("lang"));
        } catch (Exception e) {
        }
        try {
            value.setLaf(reader.getAttribute("laf"));
        } catch (Exception e) {
        }
        try {
            value.setColumnsOrder(reader.getAttribute("columnsorder"));
        } catch (Exception e) {
        }
        try {
            value.setLastHonVersion(reader.getAttribute("lasthonversion"));
        } catch (Exception e) {
        }
        try {
            value.setIgnoreGameVersion(Boolean.parseBoolean(reader.getAttribute("ignoregameversion")));
        } catch (Exception e) {
        }
        try {
            value.setAutoUpdate(Boolean.parseBoolean(reader.getAttribute("autoupdate")));
        } catch (Exception e) {
        }
        try {
            value.setDeveloperMode(Boolean.parseBoolean(reader.getAttribute("developermode")));
        } catch (Exception e) {
        }
        try {
            value.setDeleteFolderTree(Boolean.parseBoolean(reader.getAttribute("deletefoldertree")));
        } catch (Exception e) {
        }
        try {
            value.setColorCheckboxesInTable(Boolean.parseBoolean(reader.getAttribute("colorcheckboxes")));
        } catch (Exception e) {
        }
        try {
            value.setShowIconsInTable(Boolean.parseBoolean(reader.getAttribute("showicons")));
        } catch (Exception e) {
        }
        try {
            value.setUseSmallIcons(Boolean.parseBoolean(reader.getAttribute("smallicons")));
        } catch (Exception e) {
        }
        try {
            value.setDeveloperMode(Boolean.parseBoolean(reader.getAttribute("developermode")));
        } catch (Exception e) {
        }
        try {
            value.setViewType(ManagerOptions.ViewType.valueOf(reader.getAttribute("viewtype")));
        } catch (Exception e) {
        }
        int x = -9999999, y = -9999999, height = -9999999, width = -9999999;
        String s = reader.getAttribute("x");
        if (s != null) {
            x = Integer.valueOf(s);
        }
        s = reader.getAttribute("y");
        if (s != null) {
            y = Integer.valueOf(s);
        }
        s = reader.getAttribute("height");
        if (s != null) {
            height = Integer.valueOf(s);
        }
        s = reader.getAttribute("width");
        if (s != null) {
            width = Integer.valueOf(s);
        }
        if (x != -9999999 && y != -9999999 && height != -9999999 && width != -9999999) {
            value.setGuiRectangle(new Rectangle(x, y, width, height));
        }

        ArrayList<Integer> temp = new ArrayList<Integer>();
        boolean working = true;
        int i = 0;
        while (working) {
            s = reader.getAttribute("columns-" + i);
            i++;
            if (s != null) {
                temp.add(new Integer(s));
            } else {
                working = false;
            }
        }
        value.setColumnsWidth(temp);
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ManagerOptions.class);
    }
}
