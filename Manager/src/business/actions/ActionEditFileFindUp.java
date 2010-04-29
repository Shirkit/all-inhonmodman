package business.actions;

/**
 * synonyms for "findup" are "seekup" and "searchup"
 * <br/>Moves the "cursor" to the next occurrence of the source string, but searching backwards.
 * @author Shirkit
 */
public class ActionEditFileFindUp extends Action {

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
