package com.fragorl.timetracker.util;

import com.fragorl.timetracker.time.TimeSegment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 9/06/13 2:58 AM
 */
public class TimeUtils {
    private TimeUtils(){}

    public static long getTotalTime(List<TimeSegment> timeSegments) {
        long totalTime = 0L;
        for (TimeSegment timeSegment : timeSegments) {
            totalTime += RangeUtils.getLength(timeSegment.asRange());
        }
        return totalTime;
    }

    // nicked from stackoverflow user "Software Monkey"
    public static String convertTimeToOurFormat(long val) {
        StringBuilder                       buf=new StringBuilder(20);
        String                              sgn="";

        if(val<0) { sgn="-"; val=Math.abs(val); }

        append(buf,sgn,0,( val/3600000             ));
        append(buf,":",2,((val%3600000)/60000      ));
        append(buf,":",2,((val         %60000)/1000));
//        append(buf,".",3,( val                %1000));
        return buf.toString();
    }

    // nicked from stackoverflow user "Software Monkey"
    /** Append a right-aligned and zero-padded numeric value to a `StringBuilder`. */
    static private void append(StringBuilder tgt, String pfx, int dgt, long val) {
        tgt.append(pfx);
        if(dgt>1) {
            int pad=(dgt-1);
            for(long xa=val; xa>9 && pad>0; xa/=10) { pad--;           }
            for(int  xa=0;   xa<pad;        xa++  ) { tgt.append('0'); }
        }
        tgt.append(val);
    }
}
