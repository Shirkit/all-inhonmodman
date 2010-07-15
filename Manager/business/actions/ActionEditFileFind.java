package business.actions;

import business.actions.converters.ActionEditFileFindConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * synonyms for "find" are "seek" and "search"
 * <br/>Moves the "cursor" to the next occurrence of the source string
 * <br/>OR as specified by the position attribute, possible values being:
 * <br/>"start"     -> Beginning of the file (synonyms: "begin", "head", "before")
 * <br/>"end"       -> End of the file (synonyms: "tail", "after", "eof")
 * <br/>any integer -> Move forward the specified number of characters (negative values allowed)
 * @author Shirkit
 */
@XStreamAlias("find")
@XStreamConverter(ActionEditFileFindConverter.class)
public class ActionEditFileFind extends Action implements ActionEditFileActions {

    @XStreamAlias("position")
    @XStreamAsAttribute
    private String position;
    private String content;

    public ActionEditFileFind() {
        setType(FIND);
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

    public boolean isPositionAtStart() {
        if (this.position.equalsIgnoreCase("start") || this.position.equalsIgnoreCase("begin") || this.position.equalsIgnoreCase("head") || this.position.equalsIgnoreCase("before")) {
            return true;
        }
        return false;
    }

    public boolean isPositionAtEnd() {
        if (this.position.equalsIgnoreCase("end") || this.position.equalsIgnoreCase("tail") || this.position.equalsIgnoreCase("after") || this.position.equalsIgnoreCase("eof")) {
            return true;
        }
        return false;
    }
}
