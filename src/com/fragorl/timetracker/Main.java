package com.fragorl.timetracker;

import com.fragorl.timetracker.jobs.JobsManager;
import com.fragorl.timetracker.ui.GraphicsUtils;
import com.fragorl.timetracker.ui.MainBox;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 23/05/13 9:47 AM
 */
public class Main {

    public static void main(String[] args) {
        setLookAndFeelOrFail();

        JFrame mainFrame = new JFrame("TimeTracker v0.0.1");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        GraphicsUtils.setupInstance(mainFrame);

        // Get the screen size
        GraphicsConfiguration gc = mainFrame.getGraphicsConfiguration();
        Rectangle bounds = gc.getBounds();

        MainBox mainBox = new MainBox();
        mainBox.beginTiming();
        mainFrame.add(mainBox);

        // initialize and do JobsManager stuff
        JobsManager.getJobsChangedListeners().add(mainBox.getJobsChangedListener());
        JobsManager.getActiveJobChangedListeners().add(mainBox.getActiveJobChangedListener());
        JobsManager.syncJobs();
        JobsManager.syncActiveJob();

        mainFrame.pack();

        // Set the Location and Activate
        Dimension size = mainFrame.getPreferredSize();
        mainFrame.setLocation((int) ((bounds.width / 2) - (size.getWidth() / 2)),
                (int) ((bounds.height / 2) - (size.getHeight() / 2)));
        mainFrame.setVisible(true);
    }

    private static void setLookAndFeelOrFail() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't set default look and feel: ", e);
        }
    }
}
