package manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.*;

/**
 * @author Shirkit
 *
 */
public class Mod {

    // Constants
    public static final String MOD_FILENAME = "mod.xml";
    public static final String ICON_FILENAME = "icon.png";

    // Info for Manager
    private String filename;
    private String modpath;

    // Attributes
    private String name;
    private String version;
    private String date;
    private String author;
    private String description;
    private String application;
    private String appversion;
    private String mmversion;
    private String weblink;
    private String updatecheckurl;
    private String updatedownloadurl;
    private boolean applied;

    /**
     * Mod constructor.
     */
    public Mod(String fn, String mp) {
        filename = fn;
        modpath = mp;
    }

    public String getFilename() {
        return filename;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getApplication() {
        return application;
    }

    public String getAppVersion() {
        return appversion;
    }

    public String getMmVersion() {
        return mmversion;
    }

    public String getWeblink() {
        return weblink;
    }

    public String getUpdateCheckUrl() {
        return updatecheckurl;
    }

    public String getUpdateDownloadUrl() {
        return updatedownloadurl;
    }

    public byte[] getIcon() throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(modpath+filename);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;

        while((entry = zis.getNextEntry()) != null) {
            System.out.println("Extracting: " + entry);

            if(entry.getName().equals("icon.png")) {
                byte data[] = new byte[entry.getSize()];
                zis.read(data, 0, entry.getSize());

                return data;
            }
        }
        throw new FileNotFoundException();
    }

    public Document getXML() throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(modpath+filename);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;

        while((entry = zis.getNextEntry()) != null) {
            System.out.println("Extracting: " + entry);

            if(entry.getName().equals("mod.xml")) {
                byte data[] = new byte[entry.getSize()];
                zis.read(data, 0, entry.getSize());
                System.out.println(data);
                Builder parser = new BUilder();
                Document d = parser.build();

                return data;
            }
        }
        throw new FileNotFoundException();
    }

}
