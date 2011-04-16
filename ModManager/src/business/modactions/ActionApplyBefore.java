package business.modactions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * If the specified other mod is enabled, this mod will be applied before it.
 * @author Shirkit
 */
@XStreamAlias("applybefore")
public class ActionApplyBefore extends Action {

    @XStreamAlias("name")
    @XStreamAsAttribute
    private String name; // Other mod
    @XStreamAlias("version")
    @XStreamAsAttribute
    private String version; // Version of the other mod

    public ActionApplyBefore() {
        setType(APPLY_BEFORE);
    }

    /**
     *
     * @return the name of the Other mod to be applied before this mod.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the version of the Other mod to be applied before this mod.
     */
    public String getVersion() {
        if (version != null && !version.isEmpty()) {
        return version;
        } else {
            return "*";
        }
    }

    /**
     *
     * @param name of the Other mod to be applied before this mod.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param version of the Other mod to be applied before this mod.
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
