package business.actions.converters;

import business.actions.ActionEditFileDelete;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileDeleteConverter extends AbstractSingleValueConverter {

    @Override
    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileDelete.class);
    }

    @Override
    public Object fromString(String string) {
        ActionEditFileDelete action = new ActionEditFileDelete();
        action.setContent(string);
        return action;
    }




}
