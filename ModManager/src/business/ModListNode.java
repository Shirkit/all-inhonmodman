/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 * @author Shirkit
 */
@XStreamAlias("mod")
public class ModListNode {
    
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String version;
    @XStreamAsAttribute
    private boolean compressed;
    private String url;
    private String path;

    private ModListNode() {
    }

    public ModListNode(String name, String version, String url, boolean compressed, String path) {
        this.name = name;
        this.version = version;
        this.url = url;
        this.compressed = compressed;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public String getPath() {
        return path;
    }

}
