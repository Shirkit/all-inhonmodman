package business;

import business.actions.*;
import business.actions.converters.ActionEditFileFindConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XStream11XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
    public DomDriver getDriver() {
        return new DomDriver("UTF-8", null);
    }

    public void saveXML(Mod what, File where) throws FileNotFoundException {

        XStream xstream = new XStream();
        xstream = updateAlias(xstream);
        FileOutputStream fos = new FileOutputStream(where);
        xstream.toXML(what, fos);
    }

    public Mod loadXML(File file) throws FileNotFoundException {

        if (file.exists()) {
            XStream xstream = new XStream(new DomDriver());
            xstream = updateAlias(xstream);
            return (Mod) xstream.fromXML(new FileInputStream(file));
        } else {
            throw new FileNotFoundException();
        }

    }

    public XStream updateAlias(XStream xstream) {

        xstream.processAnnotations(Mod.class);
        xstream.processAnnotations(Action.class);
        xstream.processAnnotations(ActionApplyAfter.class);
        xstream.processAnnotations(ActionApplyBefore.class);
        xstream.processAnnotations(ActionCopyFile.class);
        xstream.processAnnotations(ActionEditFile.class);
        xstream.processAnnotations(ActionEditFileDelete.class);
        xstream.processAnnotations(ActionEditFileFind.class);
        xstream.processAnnotations(ActionEditFileFindUp.class);
        xstream.processAnnotations(ActionEditFileInsert.class);
        xstream.processAnnotations(ActionEditFileReplace.class);
        xstream.processAnnotations(ActionIncompatibility.class);
        xstream.processAnnotations(ActionRequirement.class);

        xstream.aliasField("find", ActionEditFileFind.class, "seek");

        xstream.registerConverter(new ActionEditFileFindConverter());

        return xstream;

    }
}
