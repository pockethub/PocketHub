package com.github.mobile.android.core.issue;

import static org.junit.Assert.assertEquals;

import com.github.mobile.android.core.issue.IssueUrlMatcher;

import org.junit.Test;

/**
 * Unit tests of {@link IssueUrlMatcher}
 */
public class IssueUrlMatcherTest {

    /**
     * Verify issue URL matching provides accurate issue numbers
     */
    @Test
    public void urlsWithoutNumbers() {
        IssueUrlMatcher matcher = new IssueUrlMatcher();

        assertEquals(-1, matcher.getNumber(null));
        assertEquals(-1, matcher.getNumber(""));
        assertEquals(-1, matcher.getNumber(" "));
        assertEquals(-1, matcher.getNumber("http://github.com/r/o/issues/abc"));
    }

    /**
     * Verify issue URL matching provides accurate issue numbers
     */
    @Test
    public void urlsWithNumbers() {
        IssueUrlMatcher matcher = new IssueUrlMatcher();

        assertEquals(5, matcher.getNumber("http://github.com/r/o/issues/5"));
        assertEquals(17, matcher.getNumber("https://github.com/r/o/issues/17"));
        assertEquals(75, matcher.getNumber("http://github.com/r/o/issues/issue/75"));
    }
}
