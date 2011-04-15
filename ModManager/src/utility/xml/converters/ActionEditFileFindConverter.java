/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utility.xml.converters;

import business.modactions.ActionEditFileFind;
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
        if (value.getPosition() != null) {
            writer.addAttribute("position", value.getPosition());
        }
        writer.setValue("<![CDATA[" + value.getContent()  + "]]>");
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ActionEditFileFind value = new ActionEditFileFind();
        value.setContent(reader.getValue());
        value.setPosition(reader.getAttribute("position"));
        value.setLineStart(reader.getAttribute("lineStart"));
        value.setLineEnd(reader.getAttribute("lineEnd"));
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileFind.class);
    }
}
