package business.actions;

/**
 * synonyms for "find" are "seek" and "search"
 * <br/>Moves the "cursor" to the next occurrence of the source string
 * <br/>OR as specified by the position attribute, possible values being:
 * <br/>"start"     -> Beginning of the file (synonyms: "begin", "head", "before")
 * <br/>"end"       -> End of the file (synonyms: "tail", "after", "eof")
 * <br/>any integer -> Move forward the specified number of characters (negative values allowed)

 * @author Shirkit
 */
public class ActionEditFileFind extends Action {

    private String position;
    private String value;

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
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }


}
