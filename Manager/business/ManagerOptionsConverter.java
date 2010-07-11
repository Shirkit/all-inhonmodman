/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import business.actions.ActionEditFileDelete;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Shirkit
 */
public class ManagerOptionsConverter implements Converter {

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ManagerOptions opt = ManagerOptions.getInstance();
        writer.addAttribute("hon_folder", opt.getGamePath());
        writer.addAttribute("mods_folder", opt.getModPath());
        Iterator<Mod> it = opt.getAppliedMods().iterator();
        while (it.hasNext()) {
            mc.convertAnother(it.next());
        }
        //writer.setValue(value.getContent());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ManagerOptions value = ManagerOptions.getInstance();
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ManagerOptions.class);
    }

}
