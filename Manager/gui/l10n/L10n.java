
package gui.l10n;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import org.apache.log4j.Logger;

import business.ManagerOptions;
import manager.Manager;

/**
 * Takes care of HonModMan localization.
 *
 * Gets proper text strings from the resources according to the default or 
 * set locale.
 */
public class L10n
{
    // This is where property files with translations are
    private static final String RESOURCE_NAME="gui.l10n.HonModMan";
    private static final String DEFAULT_LOCALE = "en";
    private static ResourceBundle resource;
    private static Preferences prefs;
    private static Logger logger = Logger.getLogger(L10n.class.getPackage().getName());
    private static Locale currentLocale;
    private static String languageLocale;
    
    /** Creates a new instance of L10n */
    public L10n()
    {
    }
    
    /** 
     * Loads l10n resources
     *
     * If user didn't set language preference the resources are loaded according to
     * default locale. Otherwise according to the user preference.
     *
     * @throws IOException if the resource is not found.
     */
    public static void load() throws IOException {
        prefs = Preferences.userNodeForPackage(L10n.class);
        languageLocale = prefs.get(ManagerOptions.PREFS_LOCALE,"DUMMY_DEFAULT");
        load(languageLocale);
    }
    
    public static void load(String locale) throws IOException {        
        Locale loc;
        if (locale.equals("DUMMY_DEFAULT")) {
            loc = Locale.getDefault();
            logger.info("Using default locale "+loc.toString());
        } else { // parse the string of format language_country_variant
            String[] s = locale.split("_");
            switch (s.length) {
                case 1:
                    loc = new Locale(s[0]);
                    break;
                case 2:
                    loc = new Locale(s[0],s[1]);
                    break;
                case 3:
                    loc = new Locale(s[0],s[1],s[2]);
                    break;
                default: //this should never happen
                    loc = Locale.getDefault();
                    logger.error("Problem parsing stored language preference. Falling into default locale.");
            }
            if (s.length >= 1 && s.length <= 3)
                logger.info("Using user stored locale "+locale);
        }
        resource = ResourceBundle.getBundle(RESOURCE_NAME, loc);
        currentLocale = loc;
    }
    
    /**
     * Gets string for the given key
     *
     * Removes the first ampersand sign (&) because it is assumed that it is an indiaction of a mnemonic.
     *
     * @param key Key of the required value
     * @throws NullPointerException in case that <code>load()</code> wasn't called first or it failed.
     * @throws MissingResourceException in case the <code>key</code> is not defined!
     */
    public static String getString(String key) {

    	try {
    		StringBuffer sb = new StringBuffer(resource.getString(key));
    		int i = sb.indexOf("&");
    		if (i>=0)
    			sb.deleteCharAt(i);
    		return sb.toString();
    	} catch( MissingResourceException e ) {
    		logger.warn("The key \"" + key + "\" is not defined in the property file!");
    		return key; // nothing else we can do...
    	}
    }  

    /**
     * Returns mnemonic for the given key.
     *
     * If the mnemonic wasn't set in the string for the key then returns -1.
     *
     * @param key Key of the required value
     * @trhows NullPointerException in case that load() wasn't called first or it failed.
     */
    public static int getMnemonic(String key) {
        try {
            StringBuffer sb = new StringBuffer(resource.getString(key));
            int i = sb.indexOf("&");
            if (i < 0)
                return -1;
            if (i+1 == sb.length())
                return -1;
            Character c = sb.charAt(i+1);
            if (i>=0)
                sb.deleteCharAt(i);
            // return Character.toUpperCase(c);
            return c;
        } catch (MissingResourceException e) {
            logger.warn("The key \"" + key + "\" is not defined in the property file! Couldn't return mnemonic.");
            return 0;
        }
    }
    
    /**
     * Returns current locale.
     *
     * @return current locale.
     */
    public static Locale getCurrentLocale() {
        return (Locale) currentLocale.clone();
    }
    
    /**
     * Returns string representing current locale.
     *
     * @return String denoting the current locale.
     */
    public static String getLanguageLocale() {
    	return languageLocale;
    }

    public static String getDefaultLocale() {
        return DEFAULT_LOCALE;
    }
}
