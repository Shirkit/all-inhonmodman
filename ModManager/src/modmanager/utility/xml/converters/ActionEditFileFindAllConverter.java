/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modmanager.utility.xml.converters;

import modmanager.business.modactions.ActionEditFileFindAll;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileFindAllConverter implements Converter {

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ActionEditFileFindAll value = (ActionEditFileFindAll) o;
        writer.setValue("<![CDATA[" + value.getContent()  + "]]>");
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ActionEditFileFindAll value = new ActionEditFileFindAll();
        value.setContent(reader.getValue());
        value.setLineStart(reader.getAttribute("lineStart"));
        value.setLineEnd(reader.getAttribute("lineEnd"));
        return value;
    }

    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileFindAll.class);
    }
}
