package com.github.mobile.util;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

import java.util.Date;

/**
 * Utilities for dealing with dates and times
 */
public class Time {

    /**
     * Get relative time for date
     *
     * @param date
     * @return relative time
     */
    public static CharSequence relativeTimeFor(Date date) {
        return getRelativeTimeSpanString(date.getTime());
    }
}
