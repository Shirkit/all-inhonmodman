package jaxe;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Image loader and cache
 * @author Kykal
 */
public class ImageKeeper {

    /**
     * Creates an instance
     * @return Instance of this class
     */
    private static ImageKeeper getInstance() {
        if (_instance == null)
            synchronized (ImageKeeper.class) {
                if (_instance == null)
                    _instance = new ImageKeeper();
            }
        return _instance;
    }

    /**
     * Loads an image
     * @param file Path/filename
     * @return Image or <code>null</code>
     */
    public static Image loadImage(final String file) {
        return getInstance().load(file, false);
    }

    /**
     * Loads an image
     * @param file Path/filename
     * @return Image or <code>null</code>
     */
    public static Image loadImage(final String file, final boolean notNull) {
        return getInstance().load(file, notNull);
    }

    /**
     * Loads an image from a specific class
     * @param point Class to load from
     * @param file Path/filename
     * @return Image or <code>null</code>
     */
    public static Image loadImage(final Class point, final String file) {
        return getInstance().load(point, file, false);
    }

    /**
     * Loads an image from a specific class
     * @param point Class to load from
     * @param file Path/filename
     * @return Image or <code>null</code>
     */
    public static Image loadImage(final Class point, final String file, final boolean notNull) {
        return getInstance().load(point, file, notNull);
    }

    /**
     * Removes an image out of the cache
     * @param file Path/filename
     */
    public static void removeImage(final String file) {
        getInstance().remove(file);
    }

    /**
     * Internal constructor
     */
    private ImageKeeper() {
        _images = new HashMap<String, Image>();
    }

    /**
     * Loads an image
     * @param file Path/filename
     * @param notNull Flag to return a dummy image if image could not be loaded
     * @return Image or <code>null</code>
     */
    protected Image load(final String file, final boolean notNull) {
        return load(getClass(), file, notNull);
    }

    /**
     * Loads an image from a class or returns a cached image from the map
     * @param point Point to load from
     * @param file Path/filename
     * @param notNull Flag to return a dummy image if image could not be loaded
     * @return Image or <code>null</code>
     */
    protected Image load(final Class point, final String file, final boolean notNull) {
        if (file == null)
            return null;
        Image result = null;
        if (_images.containsKey(file))
            result = _images.get(file);
        else
            synchronized (ImageKeeper.class) {
                try {
                    URL url;
                    final File image = new File(file);
                    boolean test_exists;
                    try {
                        test_exists = image.exists();
                    } catch (final AccessControlException ex) {
                        test_exists = false;
                    }
                    if (test_exists)
                        url = image.toURI().toURL();
                    else
                        url = point.getResource(file);

                    if (url == null)
                        url = point.getClassLoader().getResource(file);

                    if (url == null) {
                    }

                    if (url != null) {
                        final Image img = javax.imageio.ImageIO.read(url);
                        result = img;
                        _images.put(file, img);
                    } else if (notNull) {
                        LOG.error("Image not found: " + file);
                        result = DUMMY_IMAGE;
                    }
                } catch (final IOException e) {
                    LOG.error("Error loading image: " + file, e);
                    if (notNull) {
                        result = DUMMY_IMAGE;
                    }
                }
            }
        return result;
    }

    /**
     * Removes an image from the map
     * @param file Pfad/Dateiname
     */
    protected void remove(final String file) {
        _images.remove(file);
    }

    /**
     * Map with images
     */
    protected Map<String, Image> _images;


    private static final BufferedImage DUMMY_IMAGE = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(ImageKeeper.class);

    /**
     * Instance of the ImageKeeper
     */
    private static ImageKeeper _instance;
}
