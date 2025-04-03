import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.singsurf.wallpaper.Wallpaper;
import org.singsurf.wallpaper.WallpaperFramed;

/*
 Created 10 Apr 2007 - Richard Morris
 */

public class WallpaperApplication {
    // This interface is used for objects defining now to do tessellation
    static JFrame mainFrame = null;
    static String args[] = null;
    static String image;

    static void createAndShowGUI() {
//        System.out.println("Created GUI on EDT? "
//                + SwingUtilities.isEventDispatchThread());

        JFrame mainFrame = new JFrame("Wallpaper patterns");
        int w = 800, h = 600;
        if (args.length >= 3) {
            w = Integer.parseInt(args[1]);
            h = Integer.parseInt(args[2]);
        }
        mainFrame.setBounds(0, 0, w, h);
        // mainFrame.resize(200,200);
        WallpaperFramed app = new WallpaperFramed();
        app.mainFrame = mainFrame;
        app.clickCount = 2;
        app.initialize(app.frameGetImage(image), w, h);
        app.setTitle(image);
        mainFrame.setJMenuBar(app.buildMenu());

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.add(app);
        mainFrame.pack();
        mainFrame.setVisible(true);

        mainFrame.addWindowListener(new WindowAdapter() {
            // @Override
            @Override
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }

            // @Override
            @Override
            public void windowDeiconified(WindowEvent arg0) {
                // System.out.println("de-icon");
                super.windowDeiconified(arg0);
            }

            // @Override
            @Override
            public void windowIconified(WindowEvent arg0) {
                // System.out.println("icon");
                super.windowIconified(arg0);
            }

        });
        mainFrame.setVisible(true);

    }

    /**
     * @param progargs
     */
    public static void main(String progargs[]) {

        // try {
        // for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        // if ("Nimbus".equals(info.getName())) {
        // UIManager.setLookAndFeel(info.getClassName());
        // break;
        // }
        // }
        // } catch (UnsupportedLookAndFeelException e) {
        // // handle exception
        // } catch (ClassNotFoundException e) {
        // // handle exception
        // } catch (InstantiationException e) {
        // // handle exception
        // } catch (IllegalAccessException e) {
        // // handle exception
        // }

        // System.getProperties().list(System.out);
        System.out.println(Wallpaper.programInfo());
        args = progargs;
        if (args.length == 0) {
            System.out.println(Wallpaper.helpInfo());
            // String path = System.getProperty(key)
            image = "tile.jpg";
        } else
            image = args[0];

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

    }

}
