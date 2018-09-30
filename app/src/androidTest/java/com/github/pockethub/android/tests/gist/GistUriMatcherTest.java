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
package com.github.pockethub.android.tests.gist;

import android.net.Uri;
import androidx.test.filters.SmallTest;
import com.github.pockethub.android.core.gist.GistUriMatcher;
import com.meisolsson.githubsdk.model.Gist;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests of {@link GistUriMatcher}
 */
@SmallTest
public class GistUriMatcherTest {

    /**
     * Verify empty uri
     */
    @Test
    public void testEmptyUri() {
        assertNull(GistUriMatcher.getGist(Uri.parse("")));
    }

    /**
     * Verify invalid Gist ids in URIs
     */
    @Test
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
    @Test
    public void testPublicGist() {
        Gist gist = GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/1234"));
        assertNotNull(gist);
        assertEquals("1234", gist.id());
    }

    /**
     * Verify public Gist id
     */
    @Test
    public void testPrivateGist() {
        Gist gist = GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/abcd1234abcd1234abcd"));
        assertNotNull(gist);
        assertEquals("abcd1234abcd1234abcd", gist.id());
    }
}
