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
package com.github.mobile.core.gist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.mobile.core.gist.GistUrlMatcher;

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
