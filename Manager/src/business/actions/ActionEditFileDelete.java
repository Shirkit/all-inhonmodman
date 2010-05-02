package business.actions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Deletes the string pointed to by the "cursor". Does not require a source string.
 * @author Shirkit
 */
@XStreamAlias("delete")
public class ActionEditFileDelete extends Action {

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

    /**
     * Replaced to be used by the XStreamConverter
     */
    @Override
    public String toString() {
        return "<![CDATA[" + getContent() + "]]>";
    }

}
