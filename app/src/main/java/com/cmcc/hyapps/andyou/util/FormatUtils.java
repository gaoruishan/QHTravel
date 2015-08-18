/**
 * 
 */

package com.cmcc.hyapps.andyou.util;

import android.content.Context;

import com.cmcc.hyapps.andyou.R;

import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.Locale;

/**
 * @author kuloud
 */
public class FormatUtils {
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    private FormatUtils() {
    }

    public static String getTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss", Locale.CHINESE);
        String ms = formatter.format(time);
        return ms;
    }

    public static String makeTimeString(Context context, long secs) {
        String durationformat = context.getString(
                secs < 3600 ? R.string.duration_format_short : R.string.duration_format_long);

        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(durationformat, timeArgs).toString();
    }

    public static String cutStringStartBy(String text, int count) {
        if (text != null && text.length() > count) {
            return text.substring(0, count) + "…";
        } else {
            return text;
        }
    }
}
