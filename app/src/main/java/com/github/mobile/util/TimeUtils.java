package com.github.mobile.util;

import android.text.format.DateUtils;

import java.util.Date;

/**
 * Utilities for dealing with dates and times
 */
public class TimeUtils {

    /**
     * Get relative time for date
     *
     * @param date
     * @return relative time
     */
    public static CharSequence getRelativeTime(final Date date) {
        return DateUtils.getRelativeTimeSpanString(date.getTime());
    }
}
