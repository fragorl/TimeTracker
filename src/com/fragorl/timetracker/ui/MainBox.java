package com.fragorl.timetracker.ui;

import com.fragorl.timetracker.jobs.ActiveJobChangedListener;
import com.fragorl.timetracker.jobs.Job;
import com.fragorl.timetracker.jobs.JobsChangedListener;
import com.fragorl.timetracker.jobs.JobsManager;
import com.fragorl.timetracker.time.Stopwatch;
import com.fragorl.timetracker.time.TimeSegment;
import com.fragorl.timetracker.util.ImageUtils;
import com.fragorl.timetracker.util.TimeUtils;
import com.google.common.collect.HashBiMap;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 2:52 PM
 */
public class MainBox extends Box {

    private static final int DELETE_BUTTON_ICON_LENGTH = 12;
    private static final ImageIcon DELETE_BUTTON_ICON;
    static {
        try {
            DELETE_BUTTON_ICON = ImageUtils.getSquareIcon("cross.png", DELETE_BUTTON_ICON_LENGTH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Timer timer;
    private MainBoxJobsChangedListener jobsChangedListener;
    private MainBoxActiveJobChangedListener activeJobChangedListener;
    private JobsBox jobsBox;
    private List<Job> jobs;

    public MainBox() {
        super(BoxLayout.Y_AXIS);

        jobsChangedListener = new MainBoxJobsChangedListener();
        activeJobChangedListener = new MainBoxActiveJobChangedListener();
        jobsBox = new JobsBox();
        add(jobsBox);
        add(Box.createVerticalGlue());
        BottomMenuBox bottomMenuBox = new BottomMenuBox(jobsBox.getPreferredSize().width);
        add(bottomMenuBox);

        // create the timer, but don't schedule any tasks yet. Otherwise it may end up running on a potentially uninitialized object.
        timer = new Timer(true);
    }

    private boolean timerStarted = false;
    public void beginTiming() {
        if (timerStarted) {
            throw new IllegalStateException("Timer already started; can't start again");
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                eachSecond();
            }
        }, (long)0, (long)1000);
        timerStarted = true;
    }

    private void eachSecond() {
        SwingUtilities.invokeLater(jobsBox::updateActiveJobElapsedTime);
    }

    private long getTimeWorkedForJobId(String jobId) {
        long result = 0L;
        @Nullable List<TimeSegment> existingTimeSegments = JobsManager.getTimeWorkedSegments(jobId);
        if (existingTimeSegments != null) {
            result += TimeUtils.getTotalTime(existingTimeSegments);
        }
        return result;
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

    private class MainBoxJobsChangedListener implements JobsChangedListener {
        @Override
        public void jobsChanged() {
            jobs = new ArrayList<>(JobsManager.getJobs()); // anything that's doing anything with jobs needs to watch out!
            jobsBox.jobsChanged(jobs);
        }
    }

    public ActiveJobChangedListener getActiveJobChangedListener() {
        return activeJobChangedListener;
    }

    private class MainBoxActiveJobChangedListener implements ActiveJobChangedListener {
        @Override
        public void activeJobChanged() {
            @Nullable String activeJobId = JobsManager.getActiveJobId();
            if (activeJobId != null) {
                for (JobsBox.JobPanel panel : jobsBox.jobsToBoxes.values()) {
                    if (activeJobId.equals(panel.job.getId())) {
                        jobsBox.setActiveJobIfNotAlready(panel);
                    }
                }
            }
        }
    }

    private class JobsBox extends Box {
        private static final int PREFERRED_JOB_PANEL_HEIGHT = 76;
        private static final int PADDING = 5;

        private @Nullable Job activeJob;
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
            jobPanel.setBackground(GraphicsUtils.LIGHT_GREY);
            panelSurroundingBox.add(jobPanel);
            panelSurroundingBox.add(Box.createHorizontalGlue());
            add(panelSurroundingBox);
            return jobPanel;
        }

        private void setActiveJobIfNotAlready(JobPanel jobPanel) {
            Job job = jobPanel.job;
            if (activeJob == null || !activeJob.getId().equals(job.getId())) {
                JobsManager.setActiveJob(job.getId());
                setColorsForSelectedJob(jobPanel);
                startAndStopStopwatchesAndSaveProgress(jobPanel);
                activeJob = job;
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

        public void updateActiveJobElapsedTime() {
            if (activeJob != null) {
                Job activeJob = jobsBox.activeJob;
                long timeWorkedAlready = getTimeWorkedForJobId(activeJob.getId());
                JobsBox.JobPanel panelForActiveJob = jobsBox.jobsToBoxes.get(activeJob);
                if (panelForActiveJob == null) {
                    throw new IllegalStateException("active job (which was not null) was somehow mapped to a null panel!");
                }
                TimeSegment elapsedTimeSegment = panelForActiveJob.stopwatch.elapsedTime();
                timeWorkedAlready += TimeUtils.getTotalTime(Collections.singletonList(elapsedTimeSegment));
                panelForActiveJob.setNewElapsedTime(timeWorkedAlready);
            }
        }

        private void deleteJob(JobPanel panelWantingToDelete) {

        }

        public class JobPanel extends JPanel {
            private static final int PADDING = 5;

            private Job job;
            private Stopwatch stopwatch;
            private JLabel jobNameLabel;
            private JLabel elapsedTimeLabel;
            private JButton deleteButton;

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
                Box farRightBox = new Box(BoxLayout.Y_AXIS);
                Box deleteButtonHorizontalBox = new Box(BoxLayout.X_AXIS);
                deleteButtonHorizontalBox.add(Box.createHorizontalGlue());
                deleteButton = new JButton(DELETE_BUTTON_ICON);
                deleteButton.setOpaque(false);
                deleteButton.setContentAreaFilled(false);
                deleteButton.setBorderPainted(false);
                deleteButton.setFocusPainted(false);
                Dimension deleteButtonDimension = new Dimension(DELETE_BUTTON_ICON_LENGTH, DELETE_BUTTON_ICON_LENGTH);
                deleteButton.setPreferredSize(deleteButtonDimension);
                deleteButton.setMaximumSize(deleteButtonDimension);
                deleteButtonHorizontalBox.add(deleteButton);
                farRightBox.add(deleteButtonHorizontalBox);
                elapsedTimeLabel = new JLabel(TimeUtils.convertTimeToOurFormat(getTimeWorkedForJobId(job.getId())));
                farRightBox.add(elapsedTimeLabel);
                internalBox.add(farRightBox);
                addMouseListener(new MousePressedOnlyListener() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        setActiveJobIfNotAlready(JobPanel.this);
                    }
                });
            }

            public void setNewElapsedTime(long elapsedTime) {
                String labelValue = TimeUtils.convertTimeToOurFormat(elapsedTime);
                elapsedTimeLabel.setText(labelValue);
                repaint();
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
            newJobButton.setOpaque(false);
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
