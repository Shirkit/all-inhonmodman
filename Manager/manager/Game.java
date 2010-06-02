package manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.stream.FileImageInputStream;

/**
 * Attributes and Methods related to the HoN
 * @author Usuário
 */
public class Game {

    private String path;
    private String version;
    private static Game instance = null;

    /**
     * @param path to the HoN folder.
     * @throws FileNotFoundException if HoN folder doesn't exist.
     * @throws IOException if happened some I/O exception.
     */
    private Game() {
        this.path = null;
        this.version = null;
        
    }
     /**
      * This method is used to get the only instance of this class that is running. Since the game is unique, there is no point of having more than one instance of this class.
      * @return the instance.
      */
    public static Game getInstance() {
        if(instance == null) {
            instance = new Game();
        }
        return instance;
    }

    /**
     * @return the version of HoN.
     * @throws FileNotFoundException if HoN folder doesn't exist. Possible values:
     * <br/>"Hon folder doesn't exist".
     * <br/>"Hon file wasn't found".
     * @throws IOException if happened some I/O exception.
     * @throws IllegalArgumentException if the attribute 'path' is null.
     */
    public String getVersion() throws IllegalArgumentException, FileNotFoundException, IOException {
        if(this.path == null) {
            throw new IllegalArgumentException("Attribute 'path' not set yet.");
        }
        if (this.version == null) {
            setVersion(getVersion(getPath()));
            return this.version;
        } else {
            return this.version;
        }
    }

    /**
     * @param path is a String with the path to the HoN folder.
     * @return the game version. This method checks for the path of the game, and from there he tries to get it's version.
     */
    private String getVersion(String path) throws FileNotFoundException, IOException {
        // Different folder for diffrent OS
        File folder = new File(path);
        File honWindows = new File(folder.getAbsolutePath() + File.separator + "hon.exe");
        File honLinux = new File(folder.getAbsolutePath() + File.separator + "hon-x86");
        File honLinux64 = new File(folder.getAbsolutePath() + File.separator + "hon-x86_64");
        File honMac = new File(folder.getAbsolutePath() + File.separator + "HoN");
        String gameVersion;
        if (!folder.exists()) {
            throw new FileNotFoundException("HoN folder doesn't exist");
        } else {
            if (honWindows.exists()) {
                gameVersion = getGameVersionWindows(honWindows);
            } else if (honLinux.exists()) {
                gameVersion = getGameVersionLinux(honLinux);
            } else if (honLinux64.exists()) {
                gameVersion = getGameVersionLinux(honLinux64);
            } else if (honMac.exists()) {
                gameVersion = getGameVersionLinux(honMac);
            } else {
                throw new FileNotFoundException("HoN file wasn't found");
            }
        }
        return gameVersion;

    }

    /**
     *
     * @param file of the 'hon.exe'. This algorithm was taken from the HoN ModManager.
     * @return a String with the game version.
     * @throws FileNotFoundException if the @param File hon was not found.
     * @throws IOException if occurred some I/O exception while reading/writing.
     */
    private String getGameVersionWindows(File hon) throws FileNotFoundException, IOException {
        FileImageInputStream fos = new FileImageInputStream(hon);
        byte[] buffer = new byte[(int) hon.length()];
        fos.read(buffer, 0, buffer.length);
        fos.close();
        int i = FindInByteStream(buffer, new byte[]{0x43, 0, 0x55, 0, 0x52, 0, 0x45, 0, 0x20, 0, 0x43, 0, 0x52, 0, 0x54, 0, 0x5D, 0, 0, 0});
        String gameVersion = "";
        if (i >= 0) {
            i += 20;
            int j;
            do {
                j = buffer[i] + 256 * buffer[i + 1];
                if (j > 0) {
                    gameVersion += Character.toString((char) j);
                }
                i += 2;
            } while ((j != 0) && (gameVersion.length() < 10));
        }
        return gameVersion;
    }

    /**
     *
     * @param file of the 'hon.exe'. This algorithm was taken from the HoN ModManager.
     * @return a String with the game version.
     * @throws FileNotFoundException if the @param File hon was not found.
     * @throws IOException if occurred some I/O exception while reading/writing.
     */
    private String getGameVersionLinux(File hon) throws FileNotFoundException, IOException {
        FileImageInputStream fos = new FileImageInputStream(hon);
        byte[] buffer = new byte[(int) hon.length()];
        fos.read(buffer, 0, buffer.length);
        fos.close();
        int i = FindInByteStream(buffer, new byte[]{0x43, 0, 0x55, 0, 0x52, 0, 0x45, 0, 0x20, 0, 0x43, 0, 0x52, 0, 0x54, 0, 0x5D, 0, 0, 0});
        String gameVersion = "";
        if (i >= 0) {
            i += 40;
            int j;
            do {
                j = buffer[i] + 256 * (buffer[i + 1] + 256 * (buffer[i + 2] + 256 * buffer[i + 3]));
                if (j > 0) {
                    gameVersion += Character.toString((char) j);
                }
                i += 4;
            } while ((j != 0) && (gameVersion.length() < 10));
        }
        return gameVersion;
    }

    /**
     *
     * @param buffer is the search place.
     * @param needle is the thing that is you are looking for.
     * @return
     * <br/><b>-1</b> if nothing was found.
     * <br/>A integer of the position found.
     */
    private int FindInByteStream(byte[] buffer, byte[] needle) {
        for (int i = 0; i < (buffer.length - needle.length); i++) {
            int j;
            for (j = 0; j < needle.length; j++) {
                if (buffer[i + j] != needle[j]) {
                    break;
                }
            }
            if (j >= needle.length) {
                return i;
            }
        }
        return -1;
    }

    /**
     * needs implementation?
     * @return
     */
    public boolean isGameOpen() {
        return false;
    }

    /**
     * This method is required to be used before using the other methods in this class.
     * @param path to the HoN folder.
     * @throws FileNotFoundException if HoN folder doesn't exist. Possible values:
     * <br/>"Hon folder doesn't exist".
     * <br/>"Hon file wasn't found".
     * @throws IOException if happened some I/O exception.
     */
    public void setPath(File path) throws FileNotFoundException, IOException {
        this.path = path.getAbsolutePath();
        setVersion(getVersion(path.getAbsolutePath()));
    }

    /**
     * @return Absoluth path to the HoN folder
     */
    public String getPath() {
        return path;
    }

    /**
     * @param version of the HoN.
     */
    private void setVersion(String version) {
        this.version = version;
    }
}
