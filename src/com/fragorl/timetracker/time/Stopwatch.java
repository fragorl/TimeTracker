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
    public synchronized void start() {
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
    public synchronized TimeSegment stop() {
        if (!isRunning) {
            throw new IllegalStateException("can't stop stopwatch when not running");
        }
        TimeSegment elapsedTime = elapsedTime();
        isRunning = false;
        return elapsedTime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     *
     * @throws IllegalStateException if this was called when {@link #isRunning() returned false}
     */
    public synchronized TimeSegment elapsedTime() {
        if (!isRunning) {
            throw new IllegalStateException("can't get elapsed time when not running");
        }
        return new TimeSegment(startTime, System.currentTimeMillis());
    }
}
