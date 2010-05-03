package business.actions;

import business.actions.converters.ActionEditFileInsertConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * synonym for "insert" is "add"
 * <br/>Inserts the source string at the "cursor", either before or after as the position attribute specifies.
 * @author Shirkit
 */
@XStreamAlias("insert")
@XStreamConverter(ActionEditFileInsertConverter.class)
public class ActionEditFileInsert extends Action {

    @XStreamAlias("position")
    @XStreamAsAttribute
    private String position;
    private String content;

    public ActionEditFileInsert() {
        setType(INSERT);
    }

    /**
     * @return the position
     */
    public String getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(String position) {
        this.position = position;
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
