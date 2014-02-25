package com.fragorl.timetracker.jobs;

/**
 * Called when the overall list of jobs has changed.
 *
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 29/05/13 8:23 PM
 */
public interface JobsChangedListener {
    public void jobsChanged();
}
