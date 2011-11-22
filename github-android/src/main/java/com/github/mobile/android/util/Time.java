package com.github.mobile.android.util;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

import java.util.Date;

public class Time {
    public static CharSequence relativeTimeFor(Date date) {
        return getRelativeTimeSpanString(date.getTime());
    }
}
