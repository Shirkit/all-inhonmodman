    package business.modactions;

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
    public static String INCOMPATIBILITY = "incompatibility";
    /**
     * A Action type.
     */
    public static String REQUIREMENT = "requirement";
    /**
     * A Action type.
     */
    public static String APPLY_AFTER = "applyAfter";
    /**
     * A Action type.
     */
    public static String APPLY_BEFORE = "applyBefore";
    /**
     * A Action type.
     */
    public static String COPY_FILE = "copyFile";
    /**
     * A Action type.
     */
    public static String EDIT_FILE = "editFile";
    /**
     * A Action type.
     */
    public static String FIND = "find";
    /**
     * A Action type.
     */
    public static String FIND_UP = "findup";
    /**
     * A Action type.
     */
    public static String INSERT = "insert";
    /**
     * A Action type.
     */
    public static String REPLACE = "replace";
    /**
     * A Action type.
     */
    public static String DELETE = "delete";
    // Attributes
    @XStreamOmitField
    private String type;

    /**
     * @return the type of the action. See list of constants
     * @deprecated This is already working properly =D
     * @see Use getClass() or instanceof instead
     */
    public String getType() {
        return type;
    }

    /**
     * @deprecated This isn't going to work. Check wich class it is with the getClass method if (action.getClass() == ActionCopyFile.class)
     * @see Use getClass() or instanceof instead
     */
    public void setType(String type) {
        this.type = type;
    }
    }
