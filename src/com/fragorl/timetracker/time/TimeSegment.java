package com.fragorl.timetracker.time;

import com.fragorl.timetracker.util.RangeUtils;
import com.google.common.collect.Range;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 5:17 PM
 */
public class TimeSegment {
    public final long fromInclusive;
    public final long toExclusive;
    public final Range<Long> range;

    public TimeSegment(long fromInclusive, long toExclusive) {
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
        this.range = Range.closedOpen(fromInclusive, toExclusive);
    }

    public Range<Long> asRange() {
        return range;
    }

    @Override
    public String toString() {
        return "["+fromInclusive+", "+toExclusive+"), "+ RangeUtils.getLength(range)+"ms";
    }
}
