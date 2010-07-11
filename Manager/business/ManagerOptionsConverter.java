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
        writer.addAttribute("lang", opt.getLanguage());
        writer.addAttribute("laf", opt.getLaf());
        writer.addAttribute("clargs", opt.getCLArgs());
        
        // TODO: This is not tested
        if (!(opt.getAppliedMods() == null)) {
	        Iterator<Mod> it = opt.getAppliedMods().iterator();
	        while (it.hasNext()) {
	            mc.convertAnother(it.next());
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
