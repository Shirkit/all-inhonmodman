package business.modactions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * If the specified other mod is enabled, this mod will be applied after it.
 * @author Shirkit
 */
@XStreamAlias("applyafter")
public class ActionApplyAfter extends Action {

    @XStreamAlias("name")
    @XStreamAsAttribute
    private String name; // Other mod
    @XStreamAlias("version")
    @XStreamAsAttribute
    private String version; // Version of the other mod

    public ActionApplyAfter() {
        setType(APPLY_AFTER);
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

}
