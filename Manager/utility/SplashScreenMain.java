package utility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SplashScreenMain {

    SplashScreen screen;
    static SplashScreenMain instance;

    /**
     * ATTENTION: Only invoke this method AFTER using it's constructor!
     * @return
     */
    public static SplashScreenMain getInstance() {
        return instance;
    }

    public SplashScreenMain(ImageIcon icon) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        // initialize the splash screen
        splashScreenInit(icon);
        // do something here to simulate the program doing something that
        // is time consuming
        instance = this;
    }

    public void splashScreenDestruct() {
        screen.setScreenVisible(false);
        screen = null;
    }

    private void splashScreenInit(ImageIcon icon) {
        ImageIcon myImage = icon;
        screen = new SplashScreen(myImage);
        screen.setLocationRelativeTo(null);
        screen.setProgressMax(100);
        screen.setScreenVisible(true);
    }

    public void setProgressMax(int maxProgress) {
        screen.setProgressMax(maxProgress);
    }

    public int getProgressMax() {
        return screen.getProgressMax();
    }

    public void setProgress(int progress) {
        screen.setProgress(progress);
    }

    public void setProgress(String message, int progress) {
        screen.setProgress(message, progress);
    }

    public void setScreenVisible(boolean b) {
        screen.setScreenVisible(b);
    }

    private void setMessage(String message) {
        screen.setMessage(message);
    }

    class SplashScreen extends JWindow {

        BorderLayout borderLayout1 = new BorderLayout();
        JLabel imageLabel = new JLabel();
        JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        ImageIcon imageIcon;
        int progressMax;

        public SplashScreen(ImageIcon imageIcon) {
            this.imageIcon = imageIcon;
            try {
                jbInit();
            } catch (Exception ex) {
            }
        }

        // note - this class created with JBuilder
        void jbInit() throws Exception {
            imageLabel.setIcon(imageIcon);
            progressBar.setPreferredSize(new Dimension(600, 20));
            this.getContentPane().setLayout(borderLayout1);
            this.getContentPane().add(imageLabel, BorderLayout.CENTER);
            this.getContentPane().add(progressBar, BorderLayout.SOUTH);            
            this.pack();
        }

        public void setProgressMax(int maxProgress) {
            progressBar.setMaximum(maxProgress);
            this.progressMax = maxProgress;
        }

        public void setProgress(int progress) {
            final int theProgress = progress;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    progressBar.setValue(theProgress);
                }
            });
        }

        public void incrementProgress() {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    progressBar.setValue(progressBar.getValue() + 1);
                }
            });
        }

        public void setProgress(String message, int progress) {
            final int theProgress = progress;
            final String theMessage = message;
            setProgress(progress);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    progressBar.setValue(theProgress);
                    setMessage(theMessage);
                }
            });
        }

        public void setScreenVisible(boolean b) {
            final boolean boo = b;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    setVisible(boo);
                }
            });
        }

        private void setMessage(String message) {
            if (message == null) {
                message = "";
                progressBar.setStringPainted(false);
            } else {
                progressBar.setStringPainted(true);
            }
            progressBar.setString(message);
        }

        public int getProgressMax() {
            return progressMax;
        }
    }
}
