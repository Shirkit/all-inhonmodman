package business.modactions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import utility.xml.converters.ActionEditFileFindUpConverter;

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

}
