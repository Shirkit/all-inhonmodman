package utility;

import business.Mod;
import business.actions.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 *
 * @author Shirkit
 */
public class XML {

    private static DomDriver getDriver() {
        return new DomDriver("UTF-8");
    }

    /**
     * Save the XML in the passed parameter of the passed Mod.
     * @param what Mod to be saved.
     * @param where path to the File that the Mod's content will be saved.
     * @throws FileNotFoundException if the passed param 'where' was not found.
     * @throws UnsupportedEncodingException
     * @throws IOException random I/O exception.
     */
    public static void modToXml(Mod what, File where) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        XStream xstream = new XStream(getDriver());
        xstream = updateAlias(xstream);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        xstream.toXML(what, writer);
        String temp = outputStream.toString("UTF-8");

        // this part is to solve the HTML characters printing bug. I didn't found out any other ways to do it
        temp = temp.replaceAll("&lt;", "<");
        temp = temp.replaceAll("&gt;", ">");
        temp = temp.replaceAll("&quot;", "\"");
        temp = temp.replaceAll("&apos;", "\'");
        temp = temp.replaceAll("&amp;", "\'");

        temp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + temp;

        System.out.print(temp);
        FileOutputStream fos = new FileOutputStream(where);
        fos.write(temp.getBytes("UTF-8"));
    }

    /**
     * Loads the content of a XML file into a Mod.
     * @param file to be read.
     * @return the Mod with all fields already filled up.
     * @throws FileNotFoundException
     */
    public static Mod xmlToMod(File file) throws FileNotFoundException {

        if (file.exists()) {
            XStream xstream = new XStream(getDriver());
            xstream = updateAlias(xstream);
            return (Mod) xstream.fromXML(new FileInputStream(file));
        } else {
            throw new FileNotFoundException();
        }

    }

    /**
     *  This method is to help the XStream to find all the alias in the Classes and to input the synoms of the operations.
     * @param xstream
     * @return
     */
    private static XStream updateAlias(XStream xstream) {

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
        xstream.aliasField("findup", ActionEditFileFind.class, "searchup");
        xstream.aliasField("findup", ActionEditFileFind.class, "searchup");

        return xstream;

    }
}
