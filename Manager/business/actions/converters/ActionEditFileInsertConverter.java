package business.actions.converters;

import business.actions.ActionEditFileInsert;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileInsertConverter extends AbstractSingleValueConverter {

    @Override
    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileInsert.class);
    }

    @Override
    public Object fromString(String string) {
        ActionEditFileInsert action = new ActionEditFileInsert();
        action.setContent(string);
        return action;
    }




}
