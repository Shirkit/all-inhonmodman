/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Shirkit
 */
public class ManagerOptionsConverter implements Converter {

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
                    writer.startNode("modification");
                    mc.convertAnother(m);
                    writer.endNode();
                }
            }
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ManagerOptions value = ManagerOptions.getInstance();

        value.setModPath(reader.getAttribute("modsfolder"));
        value.setGamePath(reader.getAttribute("honfolder"));
        value.setCLArgs(reader.getAttribute("clargs"));
        value.setLanguage(reader.getAttribute("lang"));
        value.setLaf(reader.getAttribute("laf"));
        value.setIgnoreGameVersion(Boolean.parseBoolean(reader.getAttribute("ignoregameversion")));
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
        s = reader.getAttribute("y");
        s = reader.getAttribute("height");
        s = reader.getAttribute("width");
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
