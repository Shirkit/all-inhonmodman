/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modmanager.utility;

/**
 *
 * @author Shirkit
 */
public class Constants {

    public static final String APPLIED_COLOR = "339900";
    public static final String ENABLED_COLOR = "3300ff";
    public static final String DISABLED_COLOR = "cc0033";

    public static String putColor(String where, String color) {
        return "<html><font color=\"" + color + "\">" + where + "</font></html>";
    }

    public static String putColor(int where, String color) {
        return putColor(Integer.toString(where), color);
    }

    public static String putColor(double where, String color) {
        return putColor(Double.toString(where), color);
    }

    public static String putColor(float where, String color) {
        return putColor(Float.toString(where), color);
    }

    public static String putColorNoHtml(String where, String color) {
        return "<font color=\"" + color + "\">" + where + "</font>";
    }

    public static String putColorNoHtml(int where, String color) {
        return putColorNoHtml(Integer.toString(where), color);
    }

    public static String putColorNoHtml(double where, String color) {
        return putColorNoHtml(Double.toString(where), color);
    }

    public static String putColorNoHtml(float where, String color) {
        return putColorNoHtml(Float.toString(where), color);
    }
}
