package business.actions;

import business.actions.converters.ActionEditFileFindUpConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * synonyms for "findup" are "seekup" and "searchup"
 * <br/>Moves the "cursor" to the next occurrence of the source string, but searching backwards.
 * @author Shirkit
 */
@XStreamAlias("findup")
@XStreamConverter(ActionEditFileFindUpConverter.class)
public class ActionEditFileFindUp extends Action implements ActionEditFileActions {

    private String content;

    public ActionEditFileFindUp() {
        setType(FIND_UP);
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Replaced to be used by the XStreamConverter
     */
    @Override
    public String toString() {
        return "<![CDATA[" + getContent() + "]]>";
    }

}
