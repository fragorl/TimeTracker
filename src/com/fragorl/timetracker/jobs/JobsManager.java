package com.fragorl.timetracker.jobs;

import com.fragorl.timetracker.persistence.PersistenceManager;
import com.fragorl.timetracker.time.TimeSegment;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 5:51 PM
 */
public class JobsManager {

    private static JobsManager instance;
    private static JobsManager getInstance() {
        if (instance == null) {
            instance = new JobsManager();
        }
        return instance;
    }

    private List<JobsChangedListener> jobsChangedListeners;
    private List<Job> allJobsAtLastSync;

    JobsManager() {
        jobsChangedListeners = Collections.synchronizedList(new ArrayList<>());
        allJobsAtLastSync = new ArrayList<>();
    }

    public static Job createJob(String name, String description) {
        return getInstance().createJobInternal(name, description);
    }

    private Job createJobInternal(String name, String description) {
        UUID uuid = UUID.randomUUID();
        Job job = new Job(name, uuid.toString(), description);
        PersistenceManager.saveJob(job);
        syncJobsInternal();
        jobsChanged();
        return job;
    }

    public static void setActiveJob(String jobId) {
        getInstance().setActiveJobInternal(jobId);
    }

    private void setActiveJobInternal(String jobId) {
        PersistenceManager.saveActiveJob(jobId);
    }

    public static @Nullable String getActiveJob() {
        return getInstance().getActiveJobInternal();
    }

    public @Nullable String getActiveJobInternal() {
        return PersistenceManager.getActiveJob();
    }

    public static List<JobsChangedListener> getJobsChangedListeners() {
        return getInstance().getJobsChangedListenersInternal();
    }

    private List<JobsChangedListener> getJobsChangedListenersInternal() {
        return jobsChangedListeners;
    }

    public static void syncJobs() {
        getInstance().syncJobsInternal();
    }

    public synchronized void syncJobsInternal() {
        allJobsAtLastSync.clear();
        allJobsAtLastSync.addAll(PersistenceManager.getJobs());
    }

    public static List<Job> getJobs() {
        return getInstance().getJobsInternal();
    }

    private synchronized List<Job> getJobsInternal() {
        syncJobs();
        return Collections.unmodifiableList(allJobsAtLastSync);
    }

    public static void jobsChanged() {
        getInstance().jobsChangedInternal();
    }

    private synchronized void jobsChangedInternal() {
        for (JobsChangedListener jobsChangedListener : jobsChangedListeners) {
            jobsChangedListener.jobsChanged();
            // hello
        }
    }

    public static void addTimeSegmentForJob(Job previouslyRunningJob, TimeSegment timeWorkedOnPreviousJob) {
        getInstance().addTimeSegentForJobInternal(previouslyRunningJob, timeWorkedOnPreviousJob);
    }

    private void addTimeSegentForJobInternal(Job previouslyRunningJob, TimeSegment timeWorkedOnPreviousJob) {

    }
}
