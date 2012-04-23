package com.github.mobile.android.core.gist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests of {@link GistUrlMatcher}
 */
public class GistUrlMatcherTest {

    /**
     * Verify issue URL matching provides accurate Gist ids
     */
    @Test
    public void urlsWithoutIds() {
        GistUrlMatcher matcher = new GistUrlMatcher();

        assertNull(matcher.getId(null));
        assertNull(matcher.getId(""));
        assertNull(matcher.getId(" "));
        assertNull(matcher.getId("http://gist.github.com"));
        assertNull(matcher.getId("http://gist.github.com/"));
        assertNull(matcher.getId("http://gist.github.com/ga6"));

    }

    /**
     * Verify issue URL matching provides accurate Gist ids
     */
    @Test
    public void urlsWithIds() {
        GistUrlMatcher matcher = new GistUrlMatcher();

        assertEquals("a5", matcher.getId("http://gist.github.com/a5"));
        assertEquals("17", matcher.getId("https://gist.github.com/17"));
        assertEquals("abcd", matcher.getId("http://gist.github.com/abcd"));

        assertEquals("a5", matcher.getId("http://enter.prise.com/gist/a5"));
        assertEquals("17", matcher.getId("http://enter.prise.com/gist/17"));
        assertEquals("abcd", matcher.getId("http://enter.prise.com/gist/abcd"));
    }
}
