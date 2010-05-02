package business.actions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;

/**
 * Edits a file from resources0.s2z or one that has already been copied.
 * <br/>If condition is specified this editfile tag is only executed if the given condition is true; uses the same syntax as for copyfile.
 * <br/>Files are edited by executing a sequence of steps, each being represented by one of the four elements below.
 * <br/>All elements need a string as input, which can either be delivered as inner text node (between the <operation></operation> tags) or read from a file in the mod archive specified by a source attribute.
 * <br/>Every operation interacts with a "cursor" variable which points to a area in the file and starts out at the beginning of the file.
 * @author Shirkit
 */
@XStreamAlias("editfile")
public class ActionEditFile extends Action {

    @XStreamAlias("name")
    @XStreamAsAttribute
    private String name;
    @XStreamAlias("condition")
    @XStreamAsAttribute
    private String condition;
    @XStreamImplicit
    private ArrayList<ActionEditFileActions> actions = new ArrayList<ActionEditFileActions>();

    public ActionEditFile() {
        setType(EDIT_FILE);
    }

    public ArrayList<ActionEditFileActions> getActions() {
        return this.actions;
    }

}
