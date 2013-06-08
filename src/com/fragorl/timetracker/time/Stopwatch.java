package com.fragorl.timetracker.time;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 7/06/13 6:01 PM
 */
public class Stopwatch {

    private boolean isRunning;
    private long startTime = -1;

    /**
     *
     * @throws IllegalStateException if this was called when {@link #isRunning() returned true}
     */
    public void start() {
        if (isRunning) {
            throw new IllegalStateException("can't start stopwatch when already running");
        }
        isRunning = true;
        startTime = System.currentTimeMillis();
    }

    /**
     *
     * @throws IllegalStateException if this was called when {@link #isRunning() returned false}
     */
    public TimeSegment stop() {
        if (!isRunning) {
            throw new IllegalStateException("can't stop stopwatch when not running");
        }
        isRunning = false;
        return new TimeSegment(startTime, System.currentTimeMillis());
    }

    public boolean isRunning() {
        return isRunning;
    }
}
