/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.util;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
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
        long now = System.currentTimeMillis();
        if (Math.abs(now - date.getTime()) > 60000)
            return DateUtils.getRelativeTimeSpanString(date.getTime(), now, MINUTE_IN_MILLIS);
        else
            return "just now";
    }
}
