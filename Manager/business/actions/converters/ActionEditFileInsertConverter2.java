/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package business.actions.converters;

import business.actions.ActionEditFileInsert;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileInsertConverter2 implements Converter {

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ActionEditFileInsert value = (ActionEditFileInsert) o;
        writer.addAttribute("position", value.getPosition());
        writer.setValue(value.getContent());
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ActionEditFileInsert value = new ActionEditFileInsert();
        value.setContent("<![CDATA[" + reader.getValue() + "]]>");
        value.setPosition(reader.getAttribute("position"));
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileInsert.class);
    }

}
