package business.actions.converters;

import business.actions.ActionEditFileFind;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileFindConverter extends AbstractSingleValueConverter {

    @Override
    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileFind.class);
    }

    @Override
    public Object fromString(String string) {
        ActionEditFileFind find = new ActionEditFileFind();
        find.setContent(string);
        return find;
    }




}
