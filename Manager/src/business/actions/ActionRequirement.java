package business.actions;

/**
 * States a dependence on another mod to be abided by the Mod Manager; this mod cannot be enabled when the other mod is not present and enabled.
 * @author Shirkit
 */
public class ActionRequirement extends Action {

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
