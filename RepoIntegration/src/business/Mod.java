package business;
import java.io.Serializable;

import javax.persistence.Table;
import javax.persistence.Entity;


/**
 * @author Shirkit
 *
 */
@Entity
@Table(name="modification")
public class Mod extends dao.Entity implements Serializable {

    public static final String MOD_FILENAME = "mod.xml";
    public static final String ICON_FILENAME = "icon.png";
    public static final String CHANGELOG_FILENAME = "changelog.txt";
    public static final int ICON_WIDTH = 48;
    public static final int ICON_HEIGHT = 48;
    
    @javax.persistence.Id
    private String name;
    private String version;
    
    private String date_;
    private String author;
    private String description;
    private String application;
    private String appversion;
    private String mmversion;
    private String weblink;
    private String updatecheckurl;
    private String updatedownloadurl;
    private String path_;
    private byte[] icon;
    private String changelog;

    /**
     * Mod constructor.
     */
    public Mod() {
    }

    /**
     * This constructor should only be called by ManagerOptionsConverter.
     * @param name
     * @param version
     * @param author
     */
    public Mod(String name, String version, String author) {
        this.version = version;
        this.name = name;
        this.author = author;
    }

    /**
     * This methoud should only be called by ModListConverter.
     * @param name
     * @param version
     * @param author
     * @param updateDownloadUrl
     */
    public Mod(String name, String version, String author, String updateDownloadUrl, String path) {
        this.name = name;
        this.version = version;
        this.author = author;
        this.updatedownloadurl = updateDownloadUrl;
        this.path_ = path;
    }
    
    /**
     * This method checks if a Mod is equal to another one. To be equal, it's Name and Version must be equals, so that's what this checks.
     */
    @Override
    public boolean equals(Object o) {
        if (o != null && o.getClass().equals(Mod.class)) {
            Mod compare = (Mod) o;
            if (compare.getName().equals(this.getName()) && compare.getVersion().equals(this.getVersion())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode() + (int) this.getVersion().hashCode();
    }

    /**
     * Returns a String with the content of the mod's Changelog in it.
     * @return a String with the chanelog. If it's null or empty, returns an empty string.
     */
    public String getChangelog() {
        if (changelog != null) {
            return changelog;
        }
        return "";
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date_;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date_ = date;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the application
     */
    public String getApplication() {
        return application;
    }

    /**
     * @param application the application to set
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * @return the appversion
     */
    public String getAppversion() {
        return appversion;
    }

    /**
     * @param appversion the appversion to set
     */
    public void setAppversion(String appversion) {
        this.appversion = appversion;
    }

    /**
     * @return the mmversion
     */
    public String getMmversion() {
        return mmversion;
    }

    /**
     * @param mmversion the mmversion to set
     */
    public void setMmversion(String mmversion) {
        this.mmversion = mmversion;
    }

    /**
     * @return the weblink
     */
    public String getWeblink() {
        return weblink;
    }

    /**
     * @param weblink the weblink to set
     */
    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

    /**
     * @return the updatecheckurl
     */
    public String getUpdatecheckurl() {
        return updatecheckurl;
    }

    /**
     * @param updatecheckurl the updatecheckurl to set
     */
    public void setUpdatecheckurl(String updatecheckurl) {
        this.updatecheckurl = updatecheckurl;
    }

    /**
     * @return the updatedownloadurl
     */
    public String getUpdatedownloadurl() {
        return updatedownloadurl;
    }

    /**
     * @param updatedownloadurl the updatedownloadurl to set
     */
    public void setUpdatedownloadurl(String updatedownloadurl) {
        this.updatedownloadurl = updatedownloadurl;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path_;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path_ = path;
    }

    /**
     * @return the icon
     */
    public byte[] getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(byte[] icon) {
        this.icon = icon;
    }
}
