package business.actions.converters;

import business.actions.ActionEditFileFindUp;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 *
 * @author Shirkit
 */
public class ActionEditFileFindUpConverter extends AbstractSingleValueConverter {

    @Override
    public boolean canConvert(Class type) {
        return type.equals(ActionEditFileFindUp.class);
    }

    @Override
    public Object fromString(String string) {
        ActionEditFileFindUp action = new ActionEditFileFindUp();
        action.setContent(string);
        return action;
    }




}
