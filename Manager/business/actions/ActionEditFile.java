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

    /**
     * The file name to be edited.
     * @return the name of the file to be edited
     */
    public String getName() {
        return name;
    }

    /**
     * If the condition attribute is specified the copying is only performed if the given condition is true. A condition can consist of another mod being enabled or disabled or a boolean expression combining multiple such conditions. Examples of valid condition strings:
     * <br/>'Tiny UI'
     * <br/>not 'Tiny UI'
     * <br/>'Tiny UI[v3.0]' and 'Automatic Ability Learner[v1.1-1.5]'
     * <br/>('BardUI' or ('Improved UI by Barter[v1.08]' and 'Improved UI Addon - Juking Map')) and not 'Tiny UI' -->
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
