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
package com.github.pockethub.tests.repo;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.core.repo.RepositoryUriMatcher;

/**
 * Unit tests of {@link RepositoryUriMatcher}
 */
public class RepositoryUriMatcherTest extends AndroidTestCase {

    /**
     * Verity empty uri
     */
    public void testEmptyUri() {
        assertNull(RepositoryUriMatcher.getRepository(Uri.parse("")));
    }

    /**
     * Verify URI with no owner
     */
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
    public void testUriWithNoName() {
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/defunkt")));
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/defunkt/")));
    }

    /**
     * Verify URI with owner but no name
     */
    public void testHttpUriWithOwnerAndName() {
        Repo repo = RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/defunkt/resque"));
        assertNotNull(repo);
        assertEquals("resque", repo.name);
        assertNotNull(repo.owner);
        assertEquals("defunkt", repo.owner.login);
    }

    /**
     * Verify URI with owner but no name
     */
    public void testHttpsUriWithOwnerAndName() {
        Repo repo = RepositoryUriMatcher.getRepository(Uri
                .parse("https://github.com/mojombo/jekyll"));
        assertNotNull(repo);
        assertEquals("jekyll", repo.name);
        assertNotNull(repo.owner);
        assertEquals("mojombo", repo.owner.login);
    }

    /**
     * Verify URI with white-listed owner
     */
    public void testInvalidOwner() {
        assertNull(RepositoryUriMatcher.getRepository(Uri
                .parse("http://github.com/blog/page1")));
    }
}
