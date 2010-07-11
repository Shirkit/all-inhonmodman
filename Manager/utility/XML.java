package utility;

import business.ManagerOptions;
import business.ManagerOptionsConverter;
import business.Mod;
import business.actions.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.JDomDriver;
import com.thoughtworks.xstream.io.xml.XomDriver;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Observable;

/**
 *
 * @author Shirkit
 */
public class XML {

    public static DomDriver getDriver() {
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
        updateAlias(xstream);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        xstream.toXML(what, writer);
        String temp = outputStream.toString("UTF-8");

        temp = replaceInvalidHtmlChars(temp);

        temp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + temp;

        FileOutputStream fos = new FileOutputStream(where);
        fos.write(temp.getBytes("UTF-8"));
    }

    public static String replaceInvalidHtmlChars(String input) {
        // this part is to solve the HTML characters printing bug. I didn't found out any other ways to do it
        String temp = input;
        temp = temp.replaceAll("&lt;", "<");
        temp = temp.replaceAll("&gt;", ">");
        temp = temp.replaceAll("&quot;", "\"");
        temp = temp.replaceAll("&apos;", "\'");
        temp = temp.replaceAll("&amp;", "\'");
        return temp;
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
            updateAlias(xstream);
            return (Mod) xstream.fromXML(new FileInputStream(file));
        } else {
            throw new FileNotFoundException();
        }

    }
    
    /**
     * Loads the content of a XML String into a Mod. This method should be called to read Strings that contains a file already read in it.
     * @param fileString to be read.
     * @return the Mod with all fields already filled up.
     */
    public static Mod xmlToMod(String fileString) throws FileNotFoundException {

        XStream xstream = new XStream(getDriver());
        xstream = updateAlias(xstream);
        return (Mod) xstream.fromXML(fileString);

    }

    /**
     * Loads the content of a XML file into the ManagerOptions.
     * @param file to be read.
     * @return the Mod with all fields already filled up.
     * @throws FileNotFoundException
     */
    public static ManagerOptions xmlToManagerOptions(File path) throws FileNotFoundException {
        XStream xstream = new XStream(getDriver());
        xstream = updateAlias(xstream);
        return (ManagerOptions) xstream.fromXML(new FileInputStream(path));
    }

    /**
     * Save the XML in the passed parameter of the ManagerOptions.
     * @param where path to the File that the ManagerOptions's content will be saved.
     * @throws FileNotFoundException if the passed param 'where' was not found.
     * @throws UnsupportedEncodingException
     * @throws IOException random I/O exception.
     */
    public static void managerOptionsToXml(File where) throws UnsupportedEncodingException, IOException {

        XStream xstream = new XStream(getDriver());
        updateAlias(xstream);
        xstream.registerConverter(new ManagerOptionsConverter());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        xstream.toXML(ManagerOptions.getInstance(), writer);
        String temp = outputStream.toString("UTF-8");

        temp = replaceInvalidHtmlChars(temp);

        temp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + temp;
        
        FileOutputStream fos = new FileOutputStream(where);
        fos.write(temp.getBytes("UTF-8"));
    }

    /**
     *  This method is to help the XStream to find all the alias in the Classes and to input the synoms of the operations.
     * @param xstream
     * @return
     */
    public static XStream updateAlias(XStream xstream) {

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
        xstream.processAnnotations(ManagerOptions.class);

        // This is to remove the attributes from the parent class Observable from the ManagerOptions (fastest way ever, headache wins)
        //xstream.omitField(Observable.class, "obs");
        //xstream.omitField(Observable.class, "changed");

        xstream.aliasField("find", ActionEditFileFind.class, "seek");
        xstream.aliasField("find", ActionEditFileFind.class, "search");
        xstream.aliasField("findup", ActionEditFileFind.class, "seekup");
        xstream.aliasField("findup", ActionEditFileFind.class, "searchup");
        xstream.aliasField("insert", ActionEditFileFind.class, "add");

        return xstream;

    }
}
