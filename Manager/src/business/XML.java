package business;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Shirkit
 */
public class XML {

            /*FileInputStream fis = new FileInputStream(file);
        System.out.println(file.getAbsolutePath());
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("modification", Mod.class);
        System.out.println(xstream.toXML(this));
        Mod test = (Mod) xstream.fromXML(fis);*/

    public void saveXML(String path) {

        XStream xstream = new XStream();

    }

    public Mod loadXML(File file) {

        XStream xstream = new XStream(new DomDriver());
        return (Mod) xstream.fromXML(file.getAbsolutePath());
    }

    public XStream updateAlias(XStream xstream) {

        xstream.alias("modification", Mod.class);

        return xstream;

    }

}
