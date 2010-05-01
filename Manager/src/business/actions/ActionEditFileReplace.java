package business.actions;

/**
 * Replaces the string pointed to by the "cursor" with the source string.
 * @author Shirkit
 */
public class ActionEditFileReplace extends Action {

    private String value;

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
