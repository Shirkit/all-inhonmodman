/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;

/**
 *
 * @author Administrador
 */
@XStreamAlias("modlist")
public class ModList {

    @XStreamImplicit
    String[][] list;

    public ModList(String[][] list) {
        setList(list);
    }

    public void setList(String[][] list) {
        this.list = list;
    }

    public String[][] getList() {
        return list;
    }
}
