package com.fragorl.timetracker.ui;

import com.fragorl.timetracker.jobs.Job;
import com.fragorl.timetracker.jobs.JobsChangedListener;
import com.fragorl.timetracker.jobs.JobsManager;
import com.fragorl.timetracker.time.Stopwatch;
import com.fragorl.timetracker.time.TimeSegment;
import com.google.common.collect.HashBiMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 2:52 PM
 */
public class MainBox extends Box {

    private MainBoxJobsChangedListener jobsChangedListener;
    private JobsBox jobsBox;
    private List<Job> jobs;

    public MainBox() {
        super(BoxLayout.Y_AXIS);
        jobsChangedListener = new MainBoxJobsChangedListener();
        jobsBox = new JobsBox();
        add(jobsBox);
        add(Box.createVerticalGlue());
        BottomMenuBox bottomMenuBox = new BottomMenuBox(jobsBox.getPreferredSize().width);
        add(bottomMenuBox);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();
        Color color1 = Color.BLACK.brighter();
        Color color2 = Color.GRAY.darker();
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }

    public JobsChangedListener getJobsChangedListener() {
        return jobsChangedListener;
    }

    public class MainBoxJobsChangedListener implements JobsChangedListener {
        @Override
        public void jobsChanged() {
            jobs = new ArrayList<>(JobsManager.getJobs()); // anything that's doing anything with jobs needs to watch out!
            jobsBox.jobsChanged(jobs);
        }
    }

    private class JobsBox extends Box {
        private static final int PREFERRED_JOB_PANEL_HEIGHT = 76;
        private static final int PADDING = 5;

        private HashBiMap<Job, JobPanel> jobsToBoxes = HashBiMap.create();

        public JobsBox() {
            super(BoxLayout.Y_AXIS);
            setPreferredSize(new Dimension(300, 450));
        }

        public synchronized void jobsChanged(List<Job> newJobs) {
            removeAll();
            jobsToBoxes.clear();
            for (Job newJob : newJobs) {
                add(Box.createVerticalStrut(PADDING));
                JobPanel jobPanel = createComponentsForJobAndReturnItsPanel(newJob);
                jobsToBoxes.put(newJob, jobPanel);
            }
            validate();
            repaint();
        }

        private JobPanel createComponentsForJobAndReturnItsPanel(Job job) {
            Box panelSurroundingBox = Box.createHorizontalBox();
            panelSurroundingBox.add(Box.createHorizontalGlue());
            JobPanel jobPanel = new JobPanel(job, new Dimension((int)Math.round(getPreferredSize().getWidth()) - PADDING - PADDING, PREFERRED_JOB_PANEL_HEIGHT));
            if (job.getId().equals(JobsManager.getActiveJob())) {
                jobPanel.setBackground(GraphicsUtils.LIGHT_BLUE);
            } else {
                jobPanel.setBackground(GraphicsUtils.LIGHT_GREY);
            }
            panelSurroundingBox.add(jobPanel);
            panelSurroundingBox.add(Box.createHorizontalGlue());
            add(panelSurroundingBox);
            return jobPanel;
        }

    }

    private void setColorsForSelectedJob(JobPanel clickedOn) {
        for (JobPanel jobPanel : jobsBox.jobsToBoxes.values()) {
            jobPanel.setBackground(GraphicsUtils.LIGHT_GREY);
        }
        clickedOn.setBackground(GraphicsUtils.LIGHT_BLUE);
        repaint();
    }

    private void startAndStopStopwatchesAndSaveProgress(JobPanel clickedOn) {
        for (JobPanel jobPanel : jobsBox.jobsToBoxes.values()) {
            Stopwatch stopwatch = jobPanel.stopwatch;
            if (jobPanel != clickedOn && stopwatch.isRunning()) {
                Job previouslyRunningJob = jobPanel.job;
                TimeSegment timeWorkedOnPreviousJob = stopwatch.stop();
                JobsManager.addTimeSegmentForJob(previouslyRunningJob, timeWorkedOnPreviousJob);
            }
        }
        clickedOn.stopwatch.start();
        clickedOn.setBackground(GraphicsUtils.LIGHT_BLUE);
    }

    public class JobPanel extends JPanel {

        private static final int PADDING = 5;

        private Job job;
        private Stopwatch stopwatch;
        private JLabel jobNameLabel;
        private JLabel elapsedTimeLabel;

        public JobPanel(Job job, Dimension preferredAndMaxSize) {
            setLayout(new BorderLayout());
            this.job = job;
            this.stopwatch = new Stopwatch();
            this.setPreferredSize(preferredAndMaxSize);
            this.setMaximumSize(preferredAndMaxSize);
            setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
            Box internalBox = new Box(BoxLayout.X_AXIS);
            add(internalBox, BorderLayout.CENTER);
            jobNameLabel = new JLabel(job.getName());
            internalBox.add(jobNameLabel);
            internalBox.add(Box.createHorizontalGlue());
            Box timeKeepingBox = new Box(BoxLayout.X_AXIS);
            elapsedTimeLabel = new JLabel("0:00:00");
            timeKeepingBox.add(elapsedTimeLabel);
            internalBox.add(timeKeepingBox);
            addMouseListener(new JobBoxMouseListener(this));
        }

        private class JobBoxMouseListener extends MousePressedOnlyListener {

            private JobPanel jobPanel;

            private JobBoxMouseListener(JobPanel jobPanel) {
                this.jobPanel = jobPanel;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                JobsManager.setActiveJob(job.getId());
                setColorsForSelectedJob(jobPanel);
                startAndStopStopwatchesAndSaveProgress(jobPanel);
            }
        }
    }

    private class BottomMenuBox extends Box {

        private static final int PREFERRED_HEIGHT = 30;

        public BottomMenuBox(int width) {
            super(BoxLayout.X_AXIS);
            this.setPreferredSize(new Dimension(width, PREFERRED_HEIGHT));
            this.setOpaque(false);
            add(Box.createHorizontalGlue());
            JButton newJobButton = new JButton("+");
            newJobButton.addActionListener(new NewJobAction());
            add(newJobButton);
        }
    }

    private class NewJobAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            NewJobBox newJobBox = new NewJobBox();
            final JComponent[] inputs = new JComponent[]{
                    newJobBox
            };
            JOptionPane.showConfirmDialog(GraphicsUtils.getMainFrame(), inputs, "New Job", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            String newJobName = newJobBox.getJobName();
            String newJobDescription = newJobBox.getDescription();
            JobsManager.createJob(newJobName, newJobDescription);
            MainBox.this.validate();
            MainBox.this.repaint();
        }
    }
}
