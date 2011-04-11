package utility.xml.converters;

import business.Mod;
import business.ModList;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.io.File;
import java.util.Iterator;

/**
 * This class is used by the XStream to convert a ManagerOptions into a XML file, or the opposite.
 * @author Shirkit
 */
public class ModListConverter implements Converter {

    /**
     * XStream uses this method to convert an ManagerOptions instance into a XML String.
     */
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ModList list = (ModList) o;
        writer.addAttribute("list-version", list.getListVersion());
        if (!(list.getModList() == null)) {
            Iterator<Mod> it = list.getModList();
            while (it.hasNext()) {
                Mod m = it.next();
                if (m != null) {
                    Mod n = new Mod(m.getName(), m.getVersion(), m.getAuthor(), m.getUpdateDownloadUrl(), new File(m.getPath()).getName());
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
        ModList list = new ModList();
        list.setListVersion(reader.getAttribute("list-version"));
        return list;
    }

    public boolean canConvert(Class type) {
        return type.equals(ModList.class);
    }
}
