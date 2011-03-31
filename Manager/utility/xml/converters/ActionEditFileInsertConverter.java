/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.xml.converters;

import business.modactions.ActionEditFileInsert;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileInsertConverter implements Converter {

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ActionEditFileInsert value = (ActionEditFileInsert) o;
        if (value.getPosition() != null) {
            writer.addAttribute("position", value.getPosition());
        }
        writer.setValue("<![CDATA[" + value.getContent()  + "]]>");
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ActionEditFileInsert value = new ActionEditFileInsert();
        value.setContent(reader.getValue());
        value.setPosition(reader.getAttribute("position"));
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileInsert.class);
    }
}
