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
    final String listVersion = "1.0";
    /* Version 1.0
     * [][0] = Mod name
     * [][1] = File download url
     */


    public ModList(String[][] list) {
        setList(list);
    }

    public void setList(String[][] list) {
        this.list = list;
    }

    public String[][] getList() {
        return list;
    }

    public String getListVersion() {
        return listVersion;
    }
}
