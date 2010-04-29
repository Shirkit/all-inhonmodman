package business.actions;

/**
 * States an incompatibility with certain versions of another mod to be abided by the Mod Manager; this mod cannot be enabled when the other mod is enabled.
 * @author Shirkit
 */
public class ActionIncompatibility extends Action {

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
