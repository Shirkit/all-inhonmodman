package business.actions;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Action of what the mod does.
 * @author Shirkit
 */
public class Action {

    // Constants

    public static final String INCOMPATIBILITY = "incompatibility";
    public static final String REQUIREMENT = "requirement";
    public static final String APPLY_AFTER = "applyAfter";
    public static final String APPLY_BEFORE = "applyBefore";
    public static final String COPY_FILE = "copyFile";
    public static final String EDIT_FILE = "editFile";
    public static final String FIND = "find";
    public static final String INSERT = "insert";
    public static final String REPLACE = "replace";
    public static final String DELETE = "delete";

    // Attributes
    @XStreamOmitField
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
