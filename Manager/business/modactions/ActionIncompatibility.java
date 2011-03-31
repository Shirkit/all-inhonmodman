package business.modactions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * States an incompatibility with certain versions of another mod to be abided by the Mod Manager; this mod cannot be enabled when the other mod is enabled.
 * @author Shirkit
 */
@XStreamAlias("incompatibility")
public class ActionIncompatibility extends Action {

    @XStreamAlias("name")
    @XStreamAsAttribute
    private String name;
    @XStreamAlias("version")
    @XStreamAsAttribute
    private String version;

    public ActionIncompatibility() {
        setType(INCOMPATIBILITY);
    }

    /**
     *
     * @return the name of ther Other mod that can't be enabled with this one.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the version of ther Other mod that can't be enabled with this one.
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @param name the name of the Other mod that can't be enabled with this one.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param version the name of the Other mod that can't be enabled with this one.
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
