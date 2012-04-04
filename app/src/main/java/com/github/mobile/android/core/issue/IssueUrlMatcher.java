package com.github.mobile.android.core.issue;

import com.github.mobile.android.core.UrlMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for issue URLS that provides the issue number matched
 * <p>
 * This class is not thread-safe
 */
public class IssueUrlMatcher extends UrlMatcher {

    private static final String REGEX = "https?://.+/[^/]+/[^/]+/issues/(issue/)?(\\d+)";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private final Matcher matcher = PATTERN.matcher("");

    /**
     * Get issue number from URL
     *
     * @param url
     * @return issue number of -1 if the given URL is not to an issue
     */
    public int getNumber(final String url) {
        if (!isMatch(url, matcher))
            return -1;

        try {
            return Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }
}
