package com.fragorl.timetracker.jobs;

import java.util.Map;

/**
 * Called when one or more jobs have had their details altered.
 *
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 19/06/13 12:20 PM
 */
public interface JobsAlteredListener {
    public void jobsAltered(Map<Job, Job> oldJobToNewJob);
}
