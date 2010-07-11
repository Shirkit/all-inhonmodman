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
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.AnnotationMapper;
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
        writer.addAttribute("honfolder", opt.getGamePath());
        writer.addAttribute("modsfolder", opt.getModPath());
        Iterator<Mod> it = opt.getAppliedMods().iterator();
        while (it.hasNext()) {
            mc.convertAnother(it.next());
        }
        //writer.setValue(value.getContent());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ManagerOptions value = ManagerOptions.getInstance();
        
        value.setModPath(reader.getAttribute("modsfolder"));
        value.setGamePath(reader.getAttribute("honfolder"));
        
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ManagerOptions.class);
    }

}
