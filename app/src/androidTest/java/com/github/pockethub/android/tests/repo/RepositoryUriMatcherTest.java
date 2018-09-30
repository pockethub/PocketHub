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
package com.github.pockethub.android.tests.repo;

import android.net.Uri;
import com.github.pockethub.android.core.repo.RepositoryUriMatcher;
import com.meisolsson.githubsdk.model.Repository;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests of {@link RepositoryUriMatcher}
 */
public class RepositoryUriMatcherTest {

    /**
     * Verity empty uri
     */
    @Test
    public void testEmptyUri() {
        assertNull(RepositoryUriMatcher.getRepository(Uri.parse("")));
    }

    /**
     * Verify URI with no owner
     */
    @Test
    public void testUriWithNoOnwer() {
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com")));
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/")));
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com//")));
    }

    /**
     * Verify URI with owner but no name
     */
    @Test
    public void testUriWithNoName() {
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/defunkt")));
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/defunkt/")));
    }

    /**
     * Verify URI with owner but no name
     */
    @Test
    public void testHttpUriWithOwnerAndName() {
        Repository repo = RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/defunkt/resque"));
        assertNotNull(repo);
        assertEquals("resque", repo.name());
        assertNotNull(repo.owner());
        assertEquals("defunkt", repo.owner().login());
    }

    /**
     * Verify URI with owner but no name
     */
    @Test
    public void testHttpsUriWithOwnerAndName() {
        Repository repo = RepositoryUriMatcher.getRepository(Uri
                .parse("https://github.com/mojombo/jekyll"));
        assertNotNull(repo);
        assertEquals("jekyll", repo.name());
        assertNotNull(repo.owner());
        assertEquals("mojombo", repo.owner().login());
    }

    /**
     * Verify URI with white-listed owner
     */
    @Test
    public void testInvalidOwner() {
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/blog/page1")));
    }
}
