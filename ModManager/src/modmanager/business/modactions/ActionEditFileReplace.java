package modmanager.business.modactions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import modmanager.utility.xml.converters.ActionEditFileReplaceConverter;

/**
 * Replaces the string pointed to by the "cursor" with the source string.
 * @author Shirkit
 */
@XStreamAlias("replace")
@XStreamConverter(ActionEditFileReplaceConverter.class)
public class ActionEditFileReplace extends Action implements ActionEditFileActions {

    private String content;

    public ActionEditFileReplace() {
        setType(REPLACE);
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
