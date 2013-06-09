package com.fragorl.timetracker.jobs;

import com.fragorl.timetracker.persistence.PersistenceManager;
import com.fragorl.timetracker.time.TimeSegment;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
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
    private List<ActiveJobChangedListener> activeJobChangedListeners;
    private List<Job> allJobsAtLastSync;
    private @Nullable String activeJobIdAtLastSync;

    JobsManager() {
        jobsChangedListeners = Collections.synchronizedList(new ArrayList<>());
        activeJobChangedListeners = Collections.synchronizedList(new ArrayList<>());
        allJobsAtLastSync = new ArrayList<>();
        activeJobIdAtLastSync = null;
    }

    public static Job createJob(String name, String description) {
        return getInstance().createJobInternal(name, description);
    }

    private Job createJobInternal(String name, String description) {
        UUID uuid = UUID.randomUUID();
        Job job = new Job(name, uuid.toString(), description);
        PersistenceManager.saveJob(job);
        syncJobsInternal();
        return job;
    }

    public static void setActiveJob(String jobId) {
        getInstance().setActiveJobInternal(jobId);
    }

    private void setActiveJobInternal(String jobId) {
        PersistenceManager.saveActiveJob(jobId);
    }

    public static @Nullable List<TimeSegment> getTimeWorkedSegments(String jobId) {
        return getInstance().getTimeWorkedSegmentsInternal(jobId);
    }

    private @Nullable List<TimeSegment> getTimeWorkedSegmentsInternal(String jobId) {
        return PersistenceManager.getTimeWorkedSegments(jobId);
    }

    public static @Nullable String getActiveJobId() {
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

    public static List<ActiveJobChangedListener> getActiveJobChangedListeners() {
        return getInstance().getActiveJobChangedListenersInternal();
    }

    private List<ActiveJobChangedListener> getActiveJobChangedListenersInternal() {
        return activeJobChangedListeners;
    }

    public static void syncJobs() {
        getInstance().syncJobsInternal();
    }

    public synchronized void syncJobsInternal() {
        List<Job> newJobs = PersistenceManager.getJobs();
        List<String> newJobIds = Lists.transform(newJobs, JOB_TO_ID_TRANSFORMER);
        List<String> oldJobIds = Lists.transform(allJobsAtLastSync, JOB_TO_ID_TRANSFORMER);
        boolean newJobsEqualOldJobs = newJobIds.equals(oldJobIds);
        allJobsAtLastSync.clear();
        allJobsAtLastSync.addAll(PersistenceManager.getJobs());
        if (!newJobsEqualOldJobs) {
            fireJobsChangedListeners();
        }
    }

    public static void syncActiveJob() {
        getInstance().syncActiveJobInternal();
    }

    public synchronized void syncActiveJobInternal() {
        @Nullable String oldActiveJobId = activeJobIdAtLastSync;
        activeJobIdAtLastSync = PersistenceManager.getActiveJob();
        if (oldActiveJobId == null && activeJobIdAtLastSync != null ||
            oldActiveJobId != null && activeJobIdAtLastSync == null ||
            (oldActiveJobId != null && !oldActiveJobId.equals(activeJobIdAtLastSync))) {
            fireActiveJobsChangedListeners();
        }
    }

    public static List<Job> getJobs() {
        return getInstance().getJobsInternal();
    }

    private synchronized List<Job> getJobsInternal() {
        return Collections.unmodifiableList(allJobsAtLastSync);
    }

    public static void deleteJob(String jobId) {
        getInstance().deleteJobInternal(jobId);
    }

    private void deleteJobInternal(String jobId) {
        @Nullable String activeJob = PersistenceManager.getActiveJob();
        boolean activeJobChanged = false;
        boolean jobsChanged;
        if (activeJob != null && activeJob.equals(jobId)) {
            activeJobChanged = PersistenceManager.saveActiveJob(null);
        }
        jobsChanged = PersistenceManager.deleteJob(jobId);
        if (activeJobChanged) {
            syncActiveJobInternal();
        }
        if (jobsChanged) {
            syncJobsInternal();
        }
    }

    private synchronized void fireJobsChangedListeners() {
        for (JobsChangedListener jobsChangedListener : jobsChangedListeners) {
            jobsChangedListener.jobsChanged();
        }
    }

    private synchronized void fireActiveJobsChangedListeners() {
        for (ActiveJobChangedListener activeJobChangedListener: activeJobChangedListeners) {
            activeJobChangedListener.activeJobChanged();
        }
    }

    public static void addTimeSegmentForJob(String jobId, TimeSegment timeWorked) {
        getInstance().addTimeSegentForJobInternal(jobId, timeWorked);
    }

    private void addTimeSegentForJobInternal(String jobId, TimeSegment timeWorked) {
        PersistenceManager.saveTimeWorkedSegment(jobId, timeWorked);
    }

    private static JobToIdTransformer JOB_TO_ID_TRANSFORMER = new JobToIdTransformer();
    private static class JobToIdTransformer implements Function<Job, String> {
        @Override
        public String apply(Job job) {
            return job.getId();
        }
    }
}
