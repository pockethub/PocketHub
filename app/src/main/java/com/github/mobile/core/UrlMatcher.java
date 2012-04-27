package com.github.mobile.core;

import java.util.regex.Matcher;

/**
 * Base URL matcher with utilities for sub-classes to use
 */
public abstract class UrlMatcher {

    /**
     * Is given input URL a match?
     * <p>
     * This method ignores null and empty URLs and does not reset the matcher with them
     *
     * @param url
     * @param matcher
     * @return true if matcher matches, false otherwise
     */
    protected boolean isMatch(final String url, final Matcher matcher) {
        if (url == null || url.length() == 0)
            return false;

        return matcher.reset(url).matches();
    }
}
