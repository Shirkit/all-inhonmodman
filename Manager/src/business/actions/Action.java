package business.actions;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Action of what the mod does.
 * @author Shirkit
 */
public class Action {

    // Constants
    /**
     * A Action type.
     */
    public static final String INCOMPATIBILITY = "incompatibility";
    /**
     * A Action type.
     */
    public static final String REQUIREMENT = "requirement";
    /**
     * A Action type.
     */
    public static final String APPLY_AFTER = "applyAfter";
    /**
     * A Action type.
     */
    public static final String APPLY_BEFORE = "applyBefore";
    /**
     * A Action type.
     */
    public static final String COPY_FILE = "copyFile";
    /**
     * A Action type.
     */
    public static final String EDIT_FILE = "editFile";
    /**
     * A Action type.
     */
    public static final String FIND = "find";
    /**
     * A Action type.
     */
    public static final String FIND_UP = "findup";
    /**
     * A Action type.
     */
    public static final String INSERT = "insert";
    /**
     * A Action type.
     */
    public static final String REPLACE = "replace";
    /**
     * A Action type.
     */
    public static final String DELETE = "delete";
    // Attributes
    @XStreamOmitField
    private String type;

    /**
     *
     * @return the type of the action. See list of constants
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
