package com.fragorl.timetracker.util;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 28/05/13 4:55 PM
 */
public class SystemUtils {
    private static String OS = null;
    public static String getOsName() {
        if(OS == null) {
            OS = System.getProperty("os.name");
        }
        return OS;
    }

    public static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }

    public static boolean isMac() {
        return getOsName().startsWith("MacOS");
    }

    public static boolean isLinux() {
        return getOsName().startsWith("Linux");
    }

    public static String getEncodingToAlwaysUse() {
        return "utf-8";
    }
}


