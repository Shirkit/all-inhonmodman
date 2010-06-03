/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package business.actions.converters;

import business.actions.ActionEditFileFind;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileFindConverter implements Converter {

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ActionEditFileFind value = (ActionEditFileFind) o;
        writer.addAttribute("position", value.getPosition());
        writer.setValue(value.getContent());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ActionEditFileFind value = new ActionEditFileFind();
        value.setContent("<![CDATA[" + reader.getValue() + "]]>");
        value.setPosition(reader.getAttribute("position"));
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileFind.class);
    }

}
