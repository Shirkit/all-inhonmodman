/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utility.xml.converters;

import business.modactions.ActionEditFileFindUp;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileFindUpConverter implements Converter {

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ActionEditFileFindUp value = (ActionEditFileFindUp) o;
        writer.setValue("<![CDATA[" + value.getContent()  + "]]>");
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ActionEditFileFindUp value = new ActionEditFileFindUp();
        value.setContent(reader.getValue());
        value.setLineStart(reader.getAttribute("lineStart"));
        value.setLineEnd(reader.getAttribute("lineEnd"));
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileFindUp.class);
    }
}
