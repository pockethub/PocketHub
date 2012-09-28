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
package com.github.mobile.tests.issue;

import android.test.AndroidTestCase;

import com.github.mobile.core.issue.IssueUrlMatcher;

/**
 * Unit tests of {@link IssueUrlMatcher}
 */
public class IssueUrlMatcherTest extends AndroidTestCase {

    /**
     * Verify issue URL matching provides accurate issue numbers
     */
    public void testUrlsWithoutNumbers() {
        IssueUrlMatcher matcher = new IssueUrlMatcher();

        assertEquals(-1, matcher.getNumber(null));
        assertEquals(-1, matcher.getNumber(""));
        assertEquals(-1, matcher.getNumber(" "));
        assertEquals(-1, matcher.getNumber("http://github.com/r/o/issues/abc"));
    }

    /**
     * Verify issue URL matching provides accurate issue numbers
     */
    public void testUrlsWithNumbers() {
        IssueUrlMatcher matcher = new IssueUrlMatcher();

        assertEquals(5, matcher.getNumber("http://github.com/r/o/issues/5"));
        assertEquals(17, matcher.getNumber("https://github.com/r/o/issues/17"));
        assertEquals(75,
                matcher.getNumber("http://github.com/r/o/issues/issue/75"));
        assertEquals(1234, matcher.getNumber("http://github.com/r/o/pull/1234"));
    }
}
