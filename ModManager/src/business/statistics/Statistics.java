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
    private ManagerOptions managerOptions;

    private class OSRelated {

        private String javaVersion = System.getProperty("java.version");
        private String osName = System.getProperty("os.name");
        private String osVersion = System.getProperty("os.version");
        private String osArch = System.getProperty("os.arch");
        private String region = System.getProperty("user.region");
        private String language = System.getProperty("user.language");
    }

    public Statistics() throws CloneNotSupportedException {
        managerOptions = (ManagerOptions) ManagerOptions.getInstance().clonar();
    }
}
