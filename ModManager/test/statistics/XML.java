/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import business.statistics.Statistics;
import com.thoughtworks.xstream.XStream;
import utility.xml.ShirkitDriver;

/**
 *
 * @author pedro.torres
 */
public class XML {
    
    public static void main(String[] args) throws CloneNotSupportedException {
        XStream x = new XStream(new ShirkitDriver("UTF-8"));
        x.processAnnotations(Statistics.class);
        System.out.println(x.toXML(new Statistics()));
    }
    
}
