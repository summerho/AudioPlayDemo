package com.example.audioplaydemo.util;

public class Utils {

    /**
     * 把秒转成时分秒
     * @param totalSecs
     * @return
     */
    public static String secToTime(long totalSecs) {
        if (totalSecs < 0) {
            return "00:00";
        }
        String timeStr;
        long hour;
        long minute;
        long second;
        minute = totalSecs / 60;
        if (minute < 60) {
            second = totalSecs % 60;
            timeStr = unitFormat(minute) + ":" + unitFormat(second);
        } else {
            hour = minute / 60;
            minute = minute % 60;
            second = totalSecs - hour * 3600 - minute * 60;
            timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
        }
        return timeStr;
    }

    private static String unitFormat(long i) {
        String retStr;
        if (i >= 0 && i < 10) {
            retStr = "0" + i;
        } else {
            retStr = "" + i;
        }
        return retStr;
    }
}
