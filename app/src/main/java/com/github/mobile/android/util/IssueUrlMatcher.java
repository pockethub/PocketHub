package com.github.mobile.android.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for issue URLS that provides the issue number matched
 * <p>
 * This class is not thread-safe
 */
public class IssueUrlMatcher {

    private static final String REGEX_ISSUE = "https?://.+/[^/]+/[^/]+/issues/(issue/)?(\\d+)";

    private static final Pattern PATTERN_ISSUE = Pattern.compile(REGEX_ISSUE);

    private final Matcher matcher = PATTERN_ISSUE.matcher("");

    /**
     * Get issue number from URL
     *
     * @param url
     * @return issue number of -1 if the given URL is not to an issue
     */
    public int getNumber(final String url) {
        if (url == null || url.length() == 0)
            return -1;

        if (!matcher.reset(url).matches())
            return -1;

        try {
            return Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }
}
