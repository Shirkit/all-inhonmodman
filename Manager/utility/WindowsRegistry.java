
package utility;

import java.lang.reflect.Method;
import java.util.prefs.Preferences;

/**
 * This class provides a way to access Windows registry data from Java. Original
 * source was posted by Tarun Elankath and is available at the following address:
 * http://lenkite.blogspot.com/2008/05/access-windows-registry-using-java.html
 * 
 * @author Tarun Elankath
 */
public class WindowsRegistry {
    private static final int HKEY_CURRENT_USER = 0x80000001;
    private static final int KEY_QUERY_VALUE = 1;
    private static final int KEY_SET_VALUE = 2;
    private static final int KEY_READ = 0x20019;

    public WindowsRegistry() {}

    public static String getRecord(String key, String value) {
        final Preferences userRoot = Preferences.userRoot();
        final Preferences systemRoot = Preferences.systemRoot();
        final Class clz = userRoot.getClass();

        try {
            final Method openKey = clz.getDeclaredMethod("openKey", byte[].class, int.class, int.class);
            openKey.setAccessible(true);

            final Method closeKey = clz.getDeclaredMethod("closeKey", int.class);
            closeKey.setAccessible(true);

            final Method winRegQueryValue = clz.getDeclaredMethod("WindowsRegQueryValueEx", int.class, byte[].class);
            winRegQueryValue.setAccessible(true);

            final Method winRegEnumValue = clz.getDeclaredMethod("WindowsRegEnumValue1", int.class, int.class, int.class);
            winRegEnumValue.setAccessible(true);

            final Method winRegQueryInfo = clz.getDeclaredMethod("WindowsRegQueryInfoKey1", int.class);
            winRegQueryInfo.setAccessible(true);

            byte[] valb = null;
            String vals = null;
            Integer handle = -1;

            handle = (Integer) openKey.invoke(systemRoot, toCstr(key), KEY_READ, KEY_READ);
            valb = (byte[]) winRegQueryValue.invoke(systemRoot, handle, toCstr(value));
            vals = (valb != null ? new String(valb).trim() : null);
            closeKey.invoke(Preferences.systemRoot(), handle);
            return vals;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] toCstr(String str) {
        byte[] result = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
           result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }
}
