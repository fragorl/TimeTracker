package com.fragorl.timetracker.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 28/05/13 3:36 PM
 */
public class GraphicsUtils {

    public static Color LIGHT_BLUE = new Color(28, 191, 253);
    public static Color LIGHT_GREY = new Color(124, 124, 124);

    private static GraphicsUtils instance;

    private JFrame mainFrame;
    private GraphicsConfiguration graphicsConfiguration;

    private GraphicsUtils(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.graphicsConfiguration = mainFrame.getGraphicsConfiguration();
    }

    public static GraphicsUtils getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Not set up!");
        }
        return instance;
    }

    public static void setupInstance(JFrame mainFrame) {
        instance = new GraphicsUtils(mainFrame);
    }

    public static JFrame getMainFrame() {
        return getInstance().mainFrame;
    }

    public static Point getCentreOfScreenPositionFor(Dimension preferredSizeOfComponent) {
        Rectangle bounds = getInstance().graphicsConfiguration.getBounds();
        return new Point((int) ((bounds.width / 2) - (preferredSizeOfComponent.getWidth() / 2)),
                (int) ((bounds.height / 2) - (preferredSizeOfComponent.getHeight() / 2)));
    }
}
