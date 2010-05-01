package business.actions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

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
public class ActionEditFileFind extends Action {

    @XStreamAlias("position")
    @XStreamAsAttribute
    private String position;
    private String content;

    public ActionEditFileFind() {
        setType(FIND);
        this.content = "conteudo";
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
     * @return the value
     */
    public String getContent() {
        return content;
    }

    /**
     * @param value the value to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return getContent();
    }


}
