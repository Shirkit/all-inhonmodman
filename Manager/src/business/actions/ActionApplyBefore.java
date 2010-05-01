package business.actions;

import java.util.ArrayList;

/**
 * If the specified other mod is enabled, this mod will be applied after/before it.
 * @author Shirkit
 */
public class ActionApplyBefore extends Action {

    private String name;
    private String version;
    private String value;

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
