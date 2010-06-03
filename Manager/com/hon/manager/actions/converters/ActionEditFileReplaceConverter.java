package business.actions.converters;

import business.actions.ActionEditFileReplace;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileReplaceConverter extends AbstractSingleValueConverter {

    @Override
    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileReplace.class);
    }

    @Override
    public Object fromString(String string) {
        ActionEditFileReplace action = new ActionEditFileReplace();
        action.setContent(string);
        return action;
    }




}
