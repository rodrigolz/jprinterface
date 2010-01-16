package org.cscs.jprinterface.client; 

import java.awt.Color; 
import java.awt.Component; 
import java.awt.Container; 
import java.awt.Dimension; 
import java.awt.Graphics2D; 
import java.awt.GraphicsConfiguration; 
import java.awt.GraphicsDevice; 
import java.awt.GraphicsEnvironment; 
import java.awt.IllegalComponentStateException; 
import java.awt.Insets; 
import java.awt.MouseInfo; 
import java.awt.Point; 
import java.awt.RadialGradientPaint; 
import java.awt.Rectangle; 
import java.awt.SystemTray; 
import java.awt.Toolkit; 
import java.awt.TrayIcon; 
import java.awt.Window; 
import java.awt.MultipleGradientPaint.CycleMethod; 
import java.awt.event.ActionEvent; 
import java.awt.event.MouseAdapter; 
import java.awt.event.MouseEvent; 
import java.awt.geom.Point2D; 
import java.awt.image.BufferedImage; 

import javax.swing.AbstractAction; 
import javax.swing.AbstractButton; 
import javax.swing.BoxLayout; 
import javax.swing.JButton; 
import javax.swing.JCheckBoxMenuItem; 
import javax.swing.JLabel; 
import javax.swing.JMenuItem; 
import javax.swing.JPopupMenu; 
import javax.swing.JSeparator; 
import javax.swing.JSlider; 
import javax.swing.JToggleButton; 
import javax.swing.JWindow; 
import javax.swing.UIManager; 

/** 
 * Swing based Tray with Java 6. 
 * 
 * @author Mac Systems, GPLv3 
 * @since 29.11.2007<br> 
 * @version 0.1 
 */ 
public class SwingTray 
{ 
    static final int SIZE = 32;// groesse des Tray Iocn 

    private final TrayIcon trayIcon; 

    final JPopupMenu menu = new JPopupMenu("A popupmenu"); 

    /** 
     * 
     */ 
    public SwingTray() throws Exception 
    { 
        /** 
         * Ueberpruefe ob das System eine Tray Area hat 
         */ 
        if(!SystemTray.isSupported()) 
        { 
            throw new IllegalComponentStateException("No System Tray supported!"); 
        } 
        /** 
         * erzeuge menu eintraege 
         */ 
        addMenus(menu); 
        /** 
         * Erzeuge einen Farbverlauf 
         */ 
        final SystemTray tray = SystemTray.getSystemTray(); 
        final Point2D center = new Point2D.Float(SIZE / 2, SIZE / 2); 
        final float radius = SIZE / 4; 
        final Point2D focus = new Point2D.Float(SIZE / 2, SIZE / 2); 
        final float[] dist = 
        { 0.00f, 0.25f, 0.5f, 1.0f }; 
        final Color[] colors = 
        { Color.black, Color.DARK_GRAY, Color.gray, Color.LIGHT_GRAY }; 
        final RadialGradientPaint p = new RadialGradientPaint(center, radius, focus, dist, colors, CycleMethod.REFLECT); 
        final BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB); 
        final Graphics2D imageGraphics = image.createGraphics(); 

        imageGraphics.setPaint(p); 
        imageGraphics.fillRect(0, 0, SIZE, SIZE); 
        // Graphic Object freigeben 
        imageGraphics.dispose(); 

        /** 
         * Das Tray Icon wird erzeugt 
         */ 
        trayIcon = new TrayIcon(image, "Ein Tray Icon"); 
        tray.add(trayIcon); 
        System.out.println("system tray icon created");

        /** 
         * Um echte Plattform unabhaengigkeit zu erreichen muessen wir jeden der 
         * Events checken. Unter Mac ist dies sogar besonders wichtig (keine 
         * rechte maustaste vorhanden). 
         */ 
        trayIcon.addMouseListener(new MouseAdapter() 
        { 

            @Override 
            final public void mousePressed(final MouseEvent e) 
            { 
                showWhenTrigger(e); 
            } 

            @Override 
            final public void mouseReleased(final MouseEvent e) 
            { 
                showWhenTrigger(e); 
            } 

            /* 
             * (non-Javadoc) 
             * 
             * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent) 
             */ 
            @Override 
            final public void mouseClicked(final MouseEvent e) 
            { 
                showWhenTrigger(e); 
            } 

            /** 
             * Zeigt das Popup Menu falls der <code>MouseEvent</code> ein 
             * Trigger ist. 
             * 
             * @param e 
             * @see MouseEvent#isPopupTrigger() 
             */ 
            private final void showWhenTrigger(final MouseEvent e) 
            { 

                if(e.isPopupTrigger()) 
                { 
                    final Point point = MouseInfo.getPointerInfo().getLocation(); 
                    final PopupWindow window = new PopupWindow(); 
                    window.setLocation(point); 
                    window.setVisible(true); 
                } 
            } 

        }); 
    } 

    final public void addMenus(final Container c) 
    { 
        c.add(new JLabel("Grosses Kino")); 
        c.add(new JSeparator()); 
        c.add(makeRollover(new JMenuItem("Tralalla"))); 
        c.add(makeRollover(new JCheckBoxMenuItem("Walter Moers"))); 
        c.add(makeRollover(new JButton("VW Bus"))); 
        c.add(makeRollover(new JMenuItem("Gummiente"))); 
        c.add(makeRollover(new JMenuItem("Siebenstein"))); 
        c.add(new JSlider(0, 10, 5)); 
        c.add(makeRollover(new JToggleButton("Toggle me"))); 
        c.add(new JSeparator()); 
        c.add(makeRollover(new JMenuItem(new AbstractAction("Ende") 
        { 
            /** 
             * 
             */ 
            private static final long serialVersionUID = 1L; 

            /* 
             * (non-Javadoc) 
             * 
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) 
             */ 
            final public void actionPerformed(final ActionEvent e) 
            { 
                // ende, der Shutdown Hook wird laufen um das Tray zu entfernen 
                System.out.println("Ende"); 

                SystemTray.getSystemTray().remove(trayIcon); 
                System.exit(0); 
            } 
        }))); 

    } 

    public final static AbstractButton makeRollover(final AbstractButton aButton) 
    { 
        aButton.setRolloverEnabled(true); 
        return aButton; 
    } 

    /** 
     * @author Mac Systems, GPLv3 
     * @since 29.11.2007 
     */ 

    public class PopupWindow extends JWindow 
    { 
        /** 
         * 
         */ 
        private static final long serialVersionUID = 1L; 

        /** 
         * Berechnet die Position des Fensters neu. 
         */ 
        @Override 
        final public void setLocation(int x, int y) 
        { 
            /** 
             * Verschiebe den Punkt an der das Fenter gezeigt werden soll 
             * entsprechend dessen groesse. 
             */ 
            x -= getWidth(); 
            y -= getHeight(); 
            /** 
             * Berechne den Punkt genauer auf dem Screens 
             */ 
            final Point p = adjustPopupLocationToFitScreen(x, y); 
            super.setLocation(p.x, p.y); 
        } 

        /** 
         * Wir machen das Window sichtbar und nebenbei soll es nicht durch die 
         * Taskbar verdeckt werden. 
         * 
         * @see Window#isAlwaysOnTopSupported() 
         */ 
        @Override 
        final public void setVisible(final boolean visible) 
        { 
            super.setVisible(visible); 
            if(super.isAlwaysOnTopSupported() && visible) 
            { 
                super.setAlwaysOnTop(true); 
            } 
        } 

        public PopupWindow() 
        { 
            super(); 
            final BoxLayout layout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS); 
            getContentPane().setLayout(layout); 
            addMenus(this); 

            /** 
             * Vorsicht Falle: Fuege allen Componenten in der ContentPane einen 
             * MouseListener zu 
             */ 

            for (final Component c : getContentPane().getComponents()) 
            { 
                addMouseListener(c, this); 
            } 
            /** 
             * Das Window muss seine groesse berechnen... 
             */ 
            pack(); 
        } 

        private Point adjustPopupLocationToFitScreen(final int xposition, final int yposition) 
        { 
            final Point p = new Point(xposition, yposition); 
            final Toolkit toolkit = Toolkit.getDefaultToolkit(); 
            final Rectangle screenBounds; 
            final Insets screenInsets; 
            GraphicsConfiguration gc = null; 
            // Try to find GraphicsConfiguration, that includes mouse 
            // pointer position 
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
            final GraphicsDevice[] gd = ge.getScreenDevices(); 
            for (int i = 0; i < gd.length; i++) 
            { 
                if(gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) 
                { 
                    final GraphicsConfiguration dgc = gd[i].getDefaultConfiguration(); 
                    /** 
                     * Die Bounds entspricht der exakten auloesung (screensize) 
                     * Befinden wir uns auf diesem Screen (z.b bei einer 
                     * Multimonitor umgebung)? Wenn ja nutzen nutzen wir diese 
                     * GraphicsConfiguration. 
                     */ 
                    if(dgc.getBounds().contains(p)) 
                    { 
                        gc = dgc; 
                        break; 
                    } 
                } 
            } 

            if(gc != null) 
            { 
                /** 
                 * Wir ermitteln die Screen Insets und den nutzbaren bereich 
                 * fuer Fenster. Der nutzbare bereich ist aber gerade ein wert 
                 * den wir ignorieren wollen damit wir auch ueber der taskbar 
                 * das fenster darstellen koennen. 
                 */ 
                screenInsets = new Insets(0, 0, 0, 0); 
                screenBounds = gc.getBounds(); 
            } 
            else 
            { 
                /** 
                 * Nutze den Standard Screen und keine Insets 
                 */ 
                screenInsets = new Insets(0, 0, 0, 0); 
                screenBounds = new Rectangle(toolkit.getScreenSize()); 
            } 

            // System.out.println("Screen Insets:" + screenInsets); 

            final int scrWidth = screenBounds.width - Math.abs(screenInsets.left + screenInsets.right); 
            final int scrHeight = screenBounds.height - Math.abs(screenInsets.top + screenInsets.bottom); 

            final Dimension size = getPreferredSize(); 

            // Use long variables to prevent overflow 
            final long pw = (long) p.x + (long) size.width; 
            final long ph = (long) p.y + (long) size.height; 

            if(pw > screenBounds.x + scrWidth) 
            { 
                p.x = screenBounds.x + scrWidth - size.width; 
            } 
            if(ph > screenBounds.y + scrHeight) 
            { 
                p.y = screenBounds.y + scrHeight - size.height; 
            } 

            /* 
             * Change is made to the desired (X,Y) values, when the PopupMenu is 
             * too tall OR too wide for the screen 
             */ 
            if(p.x < screenBounds.x) 
            { 
                p.x = screenBounds.x; 
            } 
            if(p.y < screenBounds.y) 
            { 
                p.y = screenBounds.y; 
            } 

            return p; 
        } 

        /** 
         * Erzeugt einen <code>MouseListener</code> fuer eine 
         * <code>Component</code> die auf dem uebergebenden 
         * <code>JWindow</code> sitzt. 
         * 
         * @param aComponent 
         * @param aWindow 
         */ 
        public final void addMouseListener(final Component aComponent, final JWindow aWindow) 
        { 

            final MouseAdapter adapter = new MouseAdapter() 
            { 
                @Override 
                public void mouseMoved(MouseEvent e) 
                { 
                    final Point point = MouseInfo.getPointerInfo().getLocation(); 
                    System.out.println("Actual Mouse Pos:" + point); 
                } 

                @Override 
                public void mouseExited(MouseEvent e) 
                { 
                    final Point point = MouseInfo.getPointerInfo().getLocation(); 
                    // final Point point = e.getLocationOnScreen(); 

                    final Rectangle windowRect = new Rectangle(aWindow.getLocationOnScreen(), aWindow.getSize()); 
                    // System.out.println("window " + windowRect); 
                    // System.out.println("point " + point); 
                    // System.out.println("Contains :" + 
                    // windowRect.contains(point)); 
                    if(!windowRect.contains(point)) 
                    { 
                        aWindow.dispose(); 
                    } 
                } 
            }; 
            /** 
             * Vorsicht Falle: Ein Adapter der wie hier drei Interfaces 
             * adaptiert muss dennoch mehrmals als Listener registriert werden. 
             */ 
            aComponent.addMouseListener(adapter); 
            aComponent.addMouseMotionListener(adapter); 
        } 
    } 

    /** 
     * @param args 
     */ 
    final public static void main(final String[] args) throws Exception 
    {
    	try {
            System.out.println("system tray icon created");

    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
    		new SwingTray(); 
    		System.out.println("hello");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    } 

} 
