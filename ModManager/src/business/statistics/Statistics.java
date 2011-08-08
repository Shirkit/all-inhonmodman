package business.statistics;

import business.ManagerOptions;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Calendar;

/**
 * This class is for storing the data that will be colected by the Manager for one snapshot.
 * This contains data for helping the developers to have a better idea of the users
 * and what they do with the program. Data collected is anonymous and won't be
 * shared with anyone else beside the project administrators.
 * @author Shirkit
 */
@XStreamAlias("statistics")
public class Statistics {

    @XStreamAsAttribute
    private String time = "" + Calendar.getInstance().getTimeInMillis();
    private OSRelated osRelated = new OSRelated();
    private JavaRelated javaRelated = new JavaRelated();
    private ManagerRealted managerRealted = new ManagerRealted();

    private class OSRelated {

        private String osName = System.getProperty("os.name");
        private String osVersion = System.getProperty("os.version");
        private String osArch = System.getProperty("os.arch");
        private String region = System.getProperty("user.region");
        private String country  = System.getProperty("user.country");
        private String language = System.getProperty("user.language");
        private String patchLevel = System.getProperty("sun.os.patch.level");
        private String desktop = System.getProperty("sun.desktop");
    }

    private class JavaRelated {

        private String javaVersion = System.getProperty("java.version");
    }

    private class ManagerRealted {

        private ManagerOptions managerOptions = ManagerOptions.getInstance();
    }

    public Statistics() throws CloneNotSupportedException {
    }
}
