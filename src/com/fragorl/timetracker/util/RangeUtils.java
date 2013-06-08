package com.fragorl.timetracker.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 23/05/13 12:05 PM
 */
public class RangeUtils {
    private RangeUtils() {}

    public static long getLength(Range<Long> range) throws InfiniteRangeException {
        if (!range.hasLowerBound() || !range.hasUpperBound()) {
            throw new InfiniteRangeException();
        }
        long closedLower = range.lowerEndpoint() + (range.lowerBoundType() == BoundType.OPEN ? 1 : 0);
        long closedUpper = range.upperEndpoint() - (range.upperBoundType() == BoundType.OPEN ? 1 : 0);
        return closedUpper - closedLower + 1;
    }
}
