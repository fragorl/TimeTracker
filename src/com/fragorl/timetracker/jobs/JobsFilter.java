package com.fragorl.timetracker.jobs;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 19/06/13 1:29 PM
 */
public interface JobsFilter {
    public boolean satisfies(Job job);

    public static final JobsFilter TOP_LEVEL_JOBS_ONLY = job -> job.getParentTaskId() == null;
}
