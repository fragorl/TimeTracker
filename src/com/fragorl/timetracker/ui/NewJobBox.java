package com.fragorl.timetracker.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 28/05/13 3:10 PM
 */
public class NewJobBox extends Box {

    private JTextField jobName;
    private JTextField description;

    public NewJobBox() {
        super(BoxLayout.Y_AXIS);

        Box jobNameBox = Box.createHorizontalBox();
        jobNameBox.add(new JLabel("Job name:"));
        jobName = new JTextField("", 15);
        jobName.setMaximumSize(jobName.getPreferredSize());
        jobNameBox.add(Box.createHorizontalGlue());
        jobNameBox.add(jobName);
        add(jobNameBox);

        Box descriptionBox = Box.createHorizontalBox();
        descriptionBox.add(new JLabel("Description:"));
        description = new JTextField("", 30);
        description.setMaximumSize(description.getPreferredSize());
        descriptionBox.add(Box.createHorizontalGlue());
        descriptionBox.add(description);
        add(descriptionBox);
    }

    public String getJobName() {
        return jobName.getText();
    }

    public String getDescription() {
        return description.getText();
    }
}
