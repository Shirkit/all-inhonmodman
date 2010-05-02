package business;

import business.actions.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Shirkit
 */
public class XML {

    private DomDriver getDriver() {
        return new DomDriver("UTF-8", null);
    }

    public void saveXML(Mod what, File where) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        XStream xstream = new XStream(getDriver());
        xstream = updateAlias(xstream);

        // this part is to solve the < > printing bug. I didn't found out any other ways to do it
        String temp = xstream.toXML(what);
        temp = temp.replaceAll("&lt;", "<");
        temp = temp.replaceAll("&gt;", ">");
        FileOutputStream fos = new FileOutputStream(where);
        fos.write(temp.getBytes("UTF-8"));
    }

    public Mod loadXML(File file) throws FileNotFoundException {

        if (file.exists()) {
            XStream xstream = new XStream(getDriver());
            xstream = updateAlias(xstream);
            return (Mod) xstream.fromXML(new FileInputStream(file));
        } else {
            throw new FileNotFoundException();
        }

    }

    private XStream updateAlias(XStream xstream) {

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
        xstream.aliasField("find", ActionEditFileFind.class, "search");

        return xstream;

    }
}
