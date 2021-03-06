package modmanager.utility;

import modmanager.business.modactions.ActionEditFileFindAll;
import modmanager.business.ManagerOptions;
import modmanager.business.Mod;
import modmanager.business.ModList;
import modmanager.utility.xml.ShirkitDriver;
import modmanager.business.statistics.Statistics;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import modmanager.business.modactions.Action;
import modmanager.business.modactions.ActionApplyAfter;
import modmanager.business.modactions.ActionApplyBefore;
import modmanager.business.modactions.ActionCopyFile;
import modmanager.business.modactions.ActionEditFile;
import modmanager.business.modactions.ActionEditFileDelete;
import modmanager.business.modactions.ActionEditFileFind;
import modmanager.business.modactions.ActionEditFileFindUp;
import modmanager.business.modactions.ActionEditFileInsert;
import modmanager.business.modactions.ActionEditFileReplace;
import modmanager.business.modactions.ActionIncompatibility;
import modmanager.business.modactions.ActionRequirement;

/**
 *
 * @author Shirkit
 */
public class XML {

    public static ShirkitDriver getDriver() {
        return new ShirkitDriver("UTF-8");
    }

    public static ShirkitDriver getAlternativeDriver() {
        return new ShirkitDriver("UTF-16");
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

    private static Mod removeRequiredMods(Mod m) {
        Iterator<Action> actions = m.getActions().iterator();
        ActionRequirement movableframe = null;
        ActionRequirement modoptionsframework = null;
        while (actions.hasNext()) {
            Action action = actions.next();
            if (action.getClass().equals(ActionRequirement.class)) {
                ActionRequirement require = (ActionRequirement) action;
                if (require.getName().contains("Mod Options Framework")) {
                    movableframe = require;
                } else if (require.getName().contains("Movable Frames")) {
                    modoptionsframework = require;
                }
            }
        }
        if (modoptionsframework != null) {
            m.getActions().remove(modoptionsframework);
        }
        if (movableframe != null) {
            m.getActions().remove(movableframe);
        }
        return m;
    }

    /**
     * Loads the content of a XML file into a Mod.
     * @param is - InputStream to be read.
     * @return the Mod with all fields already filled up.
     */
    private static Mod xmlToMod(InputStream is, ShirkitDriver driver) throws StreamException {

        XStream xstream = new XStream(driver);
        updateAlias(xstream);
        Mod m = (Mod) xstream.fromXML(is);
        return removeRequiredMods(m);

    }

    /**
     * Loads the content of a XML String into a Mod. This method should be called to read Strings that contains a file already read in it.
     * @param fileString to be read.
     * @return the Mod with all fields already filled up.
     */
    public static Mod xmlToMod(String fileString) throws StreamException {
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(fileString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }
        Mod m = null;

        try {
            // UTF-8
            m = xmlToMod(is, getDriver());
        } catch (StreamException e1) {
            try {
                // UTF-16
                m = xmlToMod(is, getAlternativeDriver());
            } catch (StreamException e2) {
                try {
                    // Remove the BOM
                    is = new ByteArrayInputStream(fileString.substring(1).getBytes("UTF-8"));
                    try {
                        // UTF-8
                        m = xmlToMod(is, getDriver());
                    } catch (StreamException e3) {
                        // UTF-16
                        m = xmlToMod(is, getAlternativeDriver());
                    }

                } catch (UnsupportedEncodingException ex) {
                }
            }
        }

        return m;

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
        Set<Mod> applied = new HashSet<Mod>();
        try {
            ObjectInputStream in = xstream.createObjectInputStream(new FileInputStream(path));
            while (true) {
                try {
                    Object o = in.readObject();
                    if (o.getClass().equals(Mod.class)) {
                        applied.add((Mod) o);
                    }
                } catch (CannotResolveClassException e) {
                } catch (ClassNotFoundException e) {
                }
            }

        } catch (IOException ex) {
//            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        }
        ManagerOptions temp = (ManagerOptions) xstream.fromXML(new FileInputStream(path));
        temp.setAppliedMods(applied);
        return temp;
    }

    /**
     * Save the XML in the passed parameter of the ManagerOptions.
     * @param where path to the File that the ManagerOptions's content will be saved.
     * @throws FileNotFoundException if the passed param 'where' was not found.
     * @throws IOException random I/O exception.
     */
    public static void managerOptionsToXml(File where) throws IOException {

        XStream xstream = new XStream(getDriver());
        updateAlias(xstream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        xstream.toXML(ManagerOptions.getInstance(), writer);
        String temp = outputStream.toString("UTF-8");

        temp = replaceInvalidHtmlChars(temp);

        temp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + temp;

        FileOutputStream fos = new FileOutputStream(where, false);
        fos.write(temp.getBytes("UTF-8"));
        fos.flush();
        fos.close();
    }
    
    public static void statsToXml(Statistics stats, File where) throws IOException {
        
        XStream xstream = new XStream(getDriver());
        updateAlias(xstream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        xstream.toXML(stats, writer);
        String temp = outputStream.toString("UTF-8");

        temp = replaceInvalidHtmlChars(temp);

        temp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + temp;

        FileOutputStream fos = new FileOutputStream(where, false);
        fos.write(temp.getBytes("UTF-8"));
        fos.flush();
        fos.close();
    }

    public static void modListToXml(File destination, ModList modlist) throws IOException {
        XStream xstream = new XStream(getDriver());
        updateAlias(xstream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        xstream.toXML(modlist, writer);
        String temp = outputStream.toString("UTF-8");

        temp = replaceInvalidHtmlChars(temp);

        temp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + temp;

        FileOutputStream fos = new FileOutputStream(destination, false);
        fos.write(temp.getBytes("UTF-8"));
        fos.flush();
        fos.close();
    }

    public static ModList xmlToModList(byte[] byteFile) throws FileNotFoundException {
        XStream xstream = new XStream(getDriver());
        xstream = updateAlias(xstream);
        return (ModList) xstream.fromXML(new ByteArrayInputStream(byteFile));
    }

    public static ModList xmlToModList(File file) throws FileNotFoundException {
        XStream xstream = new XStream(getDriver());
        xstream = updateAlias(xstream);
        return (ModList) xstream.fromXML(new FileInputStream(file));
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
        xstream.processAnnotations(ActionEditFileFindAll.class);
        xstream.processAnnotations(ActionEditFileFindUp.class);
        xstream.processAnnotations(ActionEditFileInsert.class);
        xstream.processAnnotations(ActionEditFileReplace.class);
        xstream.processAnnotations(ActionIncompatibility.class);
        xstream.processAnnotations(ActionRequirement.class);
        xstream.processAnnotations(ManagerOptions.class);
        xstream.processAnnotations(ModList.class);
        xstream.processAnnotations(Statistics.class);

        xstream.aliasField("find", ActionEditFileFind.class, "seek");
        xstream.aliasField("find", ActionEditFileFind.class, "search");
        xstream.aliasField("findup", ActionEditFileFind.class, "seekup");
        xstream.aliasField("findup", ActionEditFileFind.class, "searchup");
        xstream.aliasField("insert", ActionEditFileFind.class, "add");

        return xstream;

    }
}
