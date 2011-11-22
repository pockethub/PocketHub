package com.github.mobile.android.util;

import java.util.Date;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class Time {
    public static CharSequence relativeTimeFor(Date date) {
        return getRelativeTimeSpanString(date.getTime());
    }
}
