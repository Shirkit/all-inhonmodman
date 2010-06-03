package business.actions;

import business.actions.converters.ActionEditFileDeleteConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * Deletes the string pointed to by the "cursor". Does not require a source string.
 * @author Shirkit
 */
@XStreamAlias("delete")
@XStreamConverter(ActionEditFileDeleteConverter.class)
public class ActionEditFileDelete extends Action implements ActionEditFileActions {

    public ActionEditFileDelete() {
        setType(DELETE);
    }

    private String content;

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
