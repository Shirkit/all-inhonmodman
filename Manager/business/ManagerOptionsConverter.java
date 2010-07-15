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
import java.util.Iterator;

/**
 *
 * @author Shirkit
 */
public class ManagerOptionsConverter implements Converter {

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ManagerOptions opt = ManagerOptions.getInstance();
        writer.addAttribute("honfolder", opt.getGamePath());
        writer.addAttribute("modsfolder", opt.getModPath());
        writer.addAttribute("lang", opt.getLanguage());
        writer.addAttribute("laf", opt.getLaf());
        writer.addAttribute("clargs", opt.getCLArgs());

        // TODO: This is not tested
        if (!(opt.getAppliedMods() == null)) {
            Iterator<Mod> it = opt.getAppliedMods().iterator();
            while (it.hasNext()) {
                Mod m = it.next();
                writer.startNode("modification");
                mc.convertAnother(m);
                writer.endNode();
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

        // TODO: Need to load appliedMods too

        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ManagerOptions.class);
    }
}
