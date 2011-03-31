package business.modactions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * States a dependence on another mod to be abided by the Mod Manager; this mod cannot be enabled when the other mod is not present and enabled.
 * @author Shirkit
 */
@XStreamAlias("requirement")
public class ActionRequirement extends Action {

    @XStreamAlias("name")
    @XStreamAsAttribute
    private String name;
    @XStreamAlias("version")
    @XStreamAsAttribute
    private String version;

    public ActionRequirement() {
        setType(REQUIREMENT);
    }

    /**
     *
     * @return the name of the Other mod that is required to be enabled to enable this mod.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the version of the Other mod that is required to be enabled to enable this mod.
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @param name of the of the Other mod that is required to be enabled to enable this mod.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param version of the of the Other mod that is required to be enabled to enable this mod.
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
