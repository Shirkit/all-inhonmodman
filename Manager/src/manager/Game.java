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

    public Game(String path) throws FileNotFoundException, IOException {
        setVersion(getVersion(path));
        setPath(path);
    }

    /**
     * @return the game version.
     */
    private String getVersion(String path) throws FileNotFoundException, IOException {

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
     * needs implementation
     * @return
     */
    public boolean isGameOpen() {
        return false;
    }

    /**
     * @param path to the HoN folder
     */
    public void setPath(String path) {
        this.path = path;
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

    /**
     * @return the version of HoN.
     * @throws FileNotFoundException if HoN folder doesn't exist.
     * @throws IOException if happened some I/O exception.
     */
    public String getVersion() throws FileNotFoundException, IOException {
        if (this.version == null) {
            setVersion(getVersion(getPath()));
            return this.version;
        } else {
            return this.version;
        }
    }
}
