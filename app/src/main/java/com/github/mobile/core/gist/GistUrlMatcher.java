package com.github.mobile.core.gist;


import com.github.mobile.core.UrlMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for gist URLS that provides the Gist id matched
 * <p>
 * This class is not thread-safe
 */
public class GistUrlMatcher extends UrlMatcher {

    private static final String REGEX = "https?://((gist.github.com)|([^/]+/gist))/([a-fA-F0-9]+)";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private final Matcher matcher = PATTERN.matcher("");

    /**
     * Get Gist id from URL
     *
     * @param url
     * @return gist id or null if the given URL is not to a Gist
     */
    public String getId(final String url) {
        return isMatch(url, matcher) ? matcher.group(4) : null;
    }
}
