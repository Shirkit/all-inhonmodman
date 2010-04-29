package business.actions;

/**
 * synonym for "insert" is "add"
 * <br/>Inserts the source string at the "cursor", either before or after as the position attribute specifies.
 * @author Shirkit
 */
public class ActionEditFileInsert extends Action {

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
