/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.tests.gist;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.github.pockethub.core.gist.GistUriMatcher;

/**
 * Unit tests of {@link GistUriMatcher}
 */
public class GistUriMatcherTest extends AndroidTestCase {

    /**
     * Verify empty uri
     */
    public void testEmptyUri() {
        assertNull(GistUriMatcher.getGist(Uri.parse("")));
    }

    /**
     * Verify invalid Gist ids in URIs
     */
    public void testNonGistId() {
        assertNull(GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/TEST")));
        assertNull(GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/abc%20")));
        assertNull(GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/abcdefg")));
    }

    /**
     * Verify public Gist id
     */
    public void testPublicGist() {
        Gist gist = GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/1234"));
        assertNotNull(gist);
        assertEquals("1234", gist.id);
    }

    /**
     * Verify public Gist id
     */
    public void testPrivateGist() {
        Gist gist = GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/abcd1234abcd1234abcd"));
        assertNotNull(gist);
        assertEquals("abcd1234abcd1234abcd", gist.id);
    }
}
