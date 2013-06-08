package com.fragorl.timetracker.util;

import com.google.common.collect.Range;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 23/05/13 12:12 PM
 */
public class RangeUtilsTest {

    public @Test void testRangeGetLength_OpenClosedOfLengthOne_GivesLengthOfOne() {
        assertEquals(1, RangeUtils.getLength(Range.<Long>openClosed((long) 0, (long) 1)));
    }

    public @Test void testRangeGetLength_EmptyRangeWithTwoOpenEndPoints_ReturnsZeroLengthNotNegative() {
        assertEquals(0, RangeUtils.getLength(Range.<Long>open((long) 0, (long) 1)));
    }
}
