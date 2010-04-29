package manager;

/**
 * Attributes and Methods related to the HoN
 * @author Usuário
 */
public class Game {

    private String path;

    /**
     * needs implementation
     * @return
     */
    public String getVersion() {
        return null;
    }

    /**
     * needs implementation
     * @return
     */
    public boolean isGameOpen() {
        return false;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return Absoluth path to the HoN folder
     */
    public String getPath() {
        return path;
    }



}
