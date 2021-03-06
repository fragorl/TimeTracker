package com.fragorl.timetracker.ui;

import com.fragorl.timetracker.jobs.*;
import com.fragorl.timetracker.time.Stopwatch;
import com.fragorl.timetracker.time.TimeSegment;
import com.fragorl.timetracker.util.ImageUtils;
import com.fragorl.timetracker.util.TimeUtils;
import com.google.common.collect.ImmutableMap;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.annotation.Nullable;
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
public class MainPanel extends JPanel {

    private static final ImageIcon DELETE_BUTTON_ICON;
    private static final ImageIcon PLAY_BUTTON_ICON;
    private static final ImageIcon PAUSE_BUTTON_ICON;
    static {
        try {
            DELETE_BUTTON_ICON = ImageUtils.getSquareIcon("cross.png", 12);
            PLAY_BUTTON_ICON = ImageUtils.getSquareIcon("play.png", 64);
            PAUSE_BUTTON_ICON = ImageUtils.getSquareIcon("pause.png", 64);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Timer timer;
    private MainBoxJobsChangedListener jobsChangedListener;
    private MainBoxActiveJobChangedListener activeJobChangedListener;
    private MainBoxJobsAlteredListener jobsAlteredListener;
    private JobsPanel jobsPanel;
    private boolean isPaused;

    public MainPanel() {
        super(new BorderLayout());
        jobsChangedListener = new MainBoxJobsChangedListener();
        activeJobChangedListener = new MainBoxActiveJobChangedListener();
        jobsAlteredListener = new MainBoxJobsAlteredListener();
        jobsPanel = new JobsPanel();
        isPaused = false;
        add(jobsPanel, BorderLayout.NORTH);
        BottomMenuBox bottomMenuBox = new BottomMenuBox();
        add(bottomMenuBox, BorderLayout.SOUTH);

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
        if (!isPaused) {
            SwingUtilities.invokeLater(jobsPanel::updateActiveJobElapsedTime);
        }
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
            List<Job> jobs = new ArrayList<>(JobsManager.getJobs());
            jobsPanel.jobsChanged(jobs);
        }
    }
    public ActiveJobChangedListener getActiveJobChangedListener() {
        return activeJobChangedListener;
    }

    private class MainBoxActiveJobChangedListener implements ActiveJobChangedListener {
        @Override
        public void activeJobChanged() {
            @Nullable String activeJobId = JobsManager.getActiveJobId();
            jobsPanel.newActiveJobSelected(activeJobId);
        }
    }

    public JobsAlteredListener getJobsAlteredListener() {
        return jobsAlteredListener;
    }

    private class MainBoxJobsAlteredListener implements JobsAlteredListener {
        @Override
        public void jobsAltered(Map<Job, Job> oldJobToNewJob) {

        }
    }

    private class JobsPanel extends JPanel {
        private static final int PREFERRED_JOB_PANEL_HEIGHT = 76;
        private static final int DEFAULT_DIMENSION_X = 300;
        private static final int DEFAULT_DIMENSION_Y = 450;
        private static final int PADDING = 5;
        private @Nullable String activeJobId;

        private Map<String, Stopwatch> jobIdsToStopwatches = new LinkedHashMap<>();
        private Map<String, JobPanel> jobIdsToPanels = new LinkedHashMap<>();
        public JobsPanel() {
            super(new MigLayout(
                    new LC().wrapAfter(1), new AC().fill().grow(), null));
            setPreferredSize(new Dimension(DEFAULT_DIMENSION_X, DEFAULT_DIMENSION_Y));
            setOpaque(false);
        }

        public synchronized void jobsChanged(List<Job> newJobs) {
            removeAll();
            jobIdsToPanels.clear();
            ImmutableMap.Builder<String, Stopwatch> jobsToStopwatchesBuilder = ImmutableMap.builder();
            for (Job newJob : newJobs) {
                String id = newJob.getId();
                JobPanel panel = createComponentsForJobAndReturnItsPanel(newJob);
                add(panel);
                jobIdsToPanels.put(id, panel);
                @Nullable Stopwatch existingStopwatch = jobIdsToStopwatches.get(id);
                if (existingStopwatch != null) {
                    jobsToStopwatchesBuilder.put(id, existingStopwatch);
                } else {
                    jobsToStopwatchesBuilder.put(id, new Stopwatch());
                }
            }
            if (activeJobId != null) {
                setColorsForSelectedJob(jobIdsToPanels.get(activeJobId));
            }
            jobIdsToStopwatches = jobsToStopwatchesBuilder.build();
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
            return jobPanel;
        }

        private void jobPanelClickedOn(JobPanel jobPanelClickedOn) {
            Job job = jobPanelClickedOn.job;
            if (activeJobId == null || !activeJobId.equals(job.getId())) {
                // this event will filter back down through the ActiveJobChangedListener that this class registers with the JobsManager.
                JobsManager.setActiveJob(job.getId());
            }
        }

        private void newActiveJobSelected(@Nullable String newActiveJobId) {
            if (newActiveJobId != null) {
                JobPanel panelForNewActiveJob = null;
                for (JobsPanel.JobPanel panel : jobsPanel.jobIdsToPanels.values()) {
                    if (newActiveJobId.equals(panel.job.getId())) {
                        panelForNewActiveJob = panel;
                    }
                }
                if (panelForNewActiveJob == null) {
                    throw new IllegalStateException("Got a null panel for newActiveJobId "+newActiveJobId+", which was non null. This shouldn't happen.");
                }

                if (activeJobId == null || !activeJobId.equals(newActiveJobId)) {
                    setColorsForSelectedJob(panelForNewActiveJob);
                    if (!isPaused) {
                        startAndStopStopwatchesAndSaveProgress(panelForNewActiveJob);
                    }
                }
            }

            activeJobId = newActiveJobId;
        }

        private void pausedStateChanged() {
            if (activeJobId != null) {
                JobPanel activeJobPanel = jobIdsToPanels.get(activeJobId);
                activeJobPanel.setBackground(pausedOrPlayingColor());
                repaint();
            }
        }

        private Color pausedOrPlayingColor() {
            return isPaused ? GraphicsUtils.LIGHT_MAUVE : GraphicsUtils.LIGHT_BLUE;
        }

        private void setColorsForSelectedJob(JobPanel clickedOn) {
            for (JobPanel jobPanel : jobIdsToPanels.values()) {
                jobPanel.setBackground(GraphicsUtils.LIGHT_GREY);
            }
            clickedOn.setBackground(pausedOrPlayingColor());
            repaint();
        }

        private void startAndStopStopwatchesAndSaveProgress(JobPanel clickedOn) {
            String clickedOnId = clickedOn.job.getId();
            stopRunningStopwatchAndDumpToPersistence(clickedOnId);
            Stopwatch stopwatchForClickedOn = jobIdsToStopwatches.get(clickedOnId);
            stopwatchForClickedOn.start();
            clickedOn.setBackground(pausedOrPlayingColor());
        }

        private void startActiveJobAgainIfAny() {
            if (activeJobId != null) {
                Stopwatch stopwatchForActiveJob = jobIdsToStopwatches.get(activeJobId);
                stopwatchForActiveJob.start();
            }
        }

        /**
         * stops any running stopwatch (PROVIDED its id is not idToIgnore, which can be null), gets its running time, and adds that to persistence.
         * @param idToIgnore an id to ignore, can be null
         */
        private void stopRunningStopwatchAndDumpToPersistence(@Nullable String idToIgnore) {
            for (Map.Entry<String, Stopwatch> idToStopwatch : jobIdsToStopwatches.entrySet()) {
                String id = idToStopwatch.getKey();
                Stopwatch stopwatch = idToStopwatch.getValue();
                if (stopwatch.isRunning() && (idToIgnore == null || !idToIgnore.equals(id))) {
                    TimeSegment timeWorkedOnPreviousJob = stopwatch.stop();
                    JobsManager.addTimeSegmentForJob(id, timeWorkedOnPreviousJob);
                }
            }
        }

        public void updateActiveJobElapsedTime() {
            if (activeJobId != null) {
                long timeWorkedAlready = getTimeWorkedForJobId(activeJobId);
                JobsPanel.JobPanel panelForActiveJob = jobIdsToPanels.get(activeJobId);
                if (panelForActiveJob == null) {
                    throw new IllegalStateException("active job (which was not null) was somehow mapped to a null panel!");
                }
                TimeSegment elapsedTimeSegment = jobIdsToStopwatches.get(activeJobId).elapsedTime();
                timeWorkedAlready += TimeUtils.getTotalTime(Collections.singletonList(elapsedTimeSegment));
                panelForActiveJob.setNewElapsedTime(timeWorkedAlready);
            }
        }

        private void deleteJob(JobPanel panelWantingToDelete) {
            JobsManager.deleteJob(panelWantingToDelete.job.getId()); // will trigger a jobsChanged call eventually!
        }

        public class JobPanel extends JPanel {

            private static final int PADDING = 5;
            private Job job;

            private JLabel jobNameLabel;
            private JLabel jobDescriptionLabel;
            private JLabel elapsedTimeLabel;
            private JButton deleteButton;

            public JobPanel(Job job, Dimension preferredSize) {
                super(new OverlappingPanelsLayoutManager());
                this.setPreferredSize(preferredSize);
                this.job = job;
                JPanel mainPanel = setupMainPanel(preferredSize);
                add(mainPanel);
                JPanel mostlyTransparentExpandButtonPanel = new SubtasksBottomMenuActionsPanel();
                add(mostlyTransparentExpandButtonPanel);
            }

            private JPanel setupMainPanel(Dimension preferredSize) {
                JPanel mainPanel = new JPanel(new MigLayout());
                mainPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

                int height = (int)preferredSize.getHeight();
                int nameAndTimeBoxWidths = (int)(preferredSize.getWidth() * 0.25);
                int descriptionBoxWidth  = (int)preferredSize.getWidth() - nameAndTimeBoxWidths * 2;
                Box internalBox = new Box(BoxLayout.X_AXIS);
                mainPanel.add(internalBox, BorderLayout.CENTER);

                Box leftNameBox          = Box.createVerticalBox();
                Box middleDescriptionBox = Box.createVerticalBox();
                Box rightTimeAndStuffBox = Box.createVerticalBox();

                leftNameBox.setPreferredSize(         new Dimension(nameAndTimeBoxWidths, height));
                middleDescriptionBox.setPreferredSize(new Dimension(descriptionBoxWidth, height));
                rightTimeAndStuffBox.setPreferredSize(new Dimension(nameAndTimeBoxWidths, height));

                jobNameLabel = new JLabel(job.getName());
                leftNameBox.add(jobNameLabel);
                internalBox.add(leftNameBox);

                jobDescriptionLabel = new JLabel(job.getDescription());
                middleDescriptionBox.add(jobDescriptionLabel);
                internalBox.add(middleDescriptionBox);

                Box deleteButtonHorizontalBox = new Box(BoxLayout.X_AXIS);
                deleteButtonHorizontalBox.add(Box.createHorizontalGlue());
                deleteButton = new JButton();
                GraphicsUtils.setupButtonWithIcon(deleteButton, DELETE_BUTTON_ICON);
                deleteButtonHorizontalBox.add(deleteButton);
                deleteButton.addMouseListener(new MousePressedOnlyListener() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        deleteJob(JobPanel.this);
                    }
                });
                rightTimeAndStuffBox.add(deleteButtonHorizontalBox);
                elapsedTimeLabel = new JLabel(TimeUtils.convertTimeToOurFormat(getTimeWorkedForJobId(job.getId())));
                rightTimeAndStuffBox.add(elapsedTimeLabel);
                rightTimeAndStuffBox.add(Box.createVerticalGlue());
                internalBox.add(rightTimeAndStuffBox);

                addMouseListener(new MousePressedOnlyListener() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        jobPanelClickedOn(JobPanel.this);
                    }
                });
                mainPanel.setOpaque(false); // so that the colour of the "overall" panel (JobPanel) will show through
                return mainPanel;
            }

            public void setNewElapsedTime(long elapsedTime) {
                String labelValue = TimeUtils.convertTimeToOurFormat(elapsedTime);
                elapsedTimeLabel.setText(labelValue);
                repaint();
            }

            private class SubtasksBottomMenuActionsPanel extends JPanel {

//                private JButton addSubtasksButton;
//                private JButton expandButton;
//                private JButton collapseButton;
                private List<JButton> buttons;
                private Box boxForButtons;

                private SubtasksBottomMenuActionsPanel() {
                    buttons = Arrays.asList(
                            // subtasks
//                        addSubtasksButton = new JButton("Add subtasks..."),
//                        expandButton = new JButton("View subtasks"),
//                        collapseButton = new JButton("Collapse")
                    );
//                    addSubtasksButton.setOpaque(false);
//                    expandButton.setOpaque(false);
//                    collapseButton.setOpaque(false);
                    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                    add(Box.createHorizontalGlue());
                    boxForButtons = Box.createVerticalBox();
                    boxForButtons.add(Box.createVerticalGlue());
                    add(boxForButtons);
                    setOpaque(false);
                    initButtons();
                }

                private void initButtons() {
                    // once subtasks have been implemented...
//                    if (!job.getSubtaskIds().isEmpty()) {
//                        putInCollapsedState();
//                    } else {
//                        putInSubtasksCanBeAddedState();
//                    }
                }

                // subtasks
//                private void putInCollapsedState() {
//                    removeAllButtonsFromBox();
//                    boxForButtons.add(expandButton);
//                    panelChanged();
//                }
//
//                private void putInExpandedState() {
//                    removeAllButtonsFromBox();
//                    boxForButtons.add(collapseButton);
//                    panelChanged();
//                }
//
//                private void putInSubtasksCanBeAddedState() {
//                    removeAllButtonsFromBox();
//                    boxForButtons.add(addSubtasksButton);
//                    panelChanged();
//                }

//                private void removeAllButtonsFromBox() {
//                    for (JButton button : buttons) {
//                        boxForButtons.remove(button);
//                    }
//                }
//
//                private void panelChanged() {
//                    invalidate();
//                    repaint();
//                }

                // subtasks
//                private final ActionListener addSubtasksAction = new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        @Nullable NewJobDetails newSubtaskDetails = getNewJobFromDialog("New Subtask");
//                        if (newSubtaskDetails != null) {
//                            JobsManager.addSubtaskToJob();
//                        }
//                    }
//                };
//
//                private final ActionListener expandAction = new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        putInExpandedState();
//                    }
//                };
//
//                private final ActionListener collapseAction = new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        putInCollapsedState();
//                    }
//                };
            }
        }
    }

    private class BottomMenuBox extends JPanel {

        private PausePlayAction pausePlayAction = new PausePlayAction();
        private JButton pauseAndPlayButton;

        public BottomMenuBox() {
            setLayout(new BorderLayout());
            this.setOpaque(false);
            pauseAndPlayButton = new JButton();
            pauseAndPlayButton.addActionListener(pausePlayAction);
            GraphicsUtils.setupButtonWithIcon(pauseAndPlayButton, PAUSE_BUTTON_ICON);
            add(pauseAndPlayButton, BorderLayout.CENTER);
            JButton newJobButton = new JButton("+");
            newJobButton.setOpaque(false);
            newJobButton.addActionListener(new NewJobAction());
            add(newJobButton, BorderLayout.EAST);
        }

        private class PausePlayAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseAndPlayButton.setIcon(isPaused ? PAUSE_BUTTON_ICON : PLAY_BUTTON_ICON);
                if (isPaused) {
                    jobsPanel.startActiveJobAgainIfAny();
                } else {
                    jobsPanel.stopRunningStopwatchAndDumpToPersistence(null);
                }
                isPaused = !isPaused;
                jobsPanel.pausedStateChanged();
            }
        }
    }

    private class NewJobAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            @Nullable NewJobDetails newJobDetails = getNewJobFromDialog("New Job");
            if (newJobDetails != null) {
                JobsManager.createJob(newJobDetails.name, newJobDetails.description);
                MainPanel.this.validate();
                MainPanel.this.repaint();
            }
        }
    }

    private @Nullable NewJobDetails getNewJobFromDialog(String title) {
        NewJobBox newJobBox = new NewJobBox();
        final JComponent[] inputs = new JComponent[]{
                newJobBox
        };
        if (JOptionPane.CANCEL_OPTION != JOptionPane.showConfirmDialog(GraphicsUtils.getMainFrame(), inputs, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            String newJobName = newJobBox.getJobName();
            String newJobDescription = newJobBox.getDescription();
            return new NewJobDetails(newJobName, newJobDescription);
        }
        return null;
    }

    private static class NewJobDetails {
        public final String name;
        public final String description;

        private NewJobDetails(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public void doEmergencyQuitNonSwingThreadStuff() {
        jobsPanel.stopRunningStopwatchAndDumpToPersistence(null);
    }
}
