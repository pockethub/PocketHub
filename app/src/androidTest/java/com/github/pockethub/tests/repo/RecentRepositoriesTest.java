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

import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.ui.repo.RecentRepositories;

import static com.github.pockethub.ui.repo.RecentRepositories.MAX_SIZE;

/**
 * Unit tests of {@link RecentRepositories}
 */
public class RecentRepositoriesTest extends AndroidTestCase {

    /**
     * Verify bad input
     */
    public void testBadInput() {
        User org = new User();
        org.id = 20;
        RecentRepositories recent = new RecentRepositories(getContext(), org);
        assertFalse(recent.contains(null));
        assertFalse(recent.contains(-1));
    }

    /**
     * Verify eviction
     */
    public void testMaxReached() {
        User org = new User();
        org.id = 20;
        RecentRepositories recent = new RecentRepositories(getContext(), org);

        for (int i = 0; i < MAX_SIZE; i++) {
            recent.add(i);
            assertTrue(recent.contains(i));
        }

        recent.add(MAX_SIZE + 1);
        assertTrue(recent.contains(MAX_SIZE + 1));
        assertFalse(recent.contains(0));

        for (int i = 1; i < MAX_SIZE; i++)
            assertTrue(recent.contains(i));
    }

    /**
     * Verify input/output to disk of {@link RecentRepositories} state
     */
    public void testIO() {
        User org = new User();
        org.id = 20;
        RecentRepositories recent1 = new RecentRepositories(getContext(), org);
        long id = 1234;
        recent1.add(id);
        assertTrue(recent1.contains(id));
        recent1.save();
        RecentRepositories recent2 = new RecentRepositories(getContext(), org);
        assertTrue(recent2.contains(id));
    }

    /**
     * Verify repositories are scoped to organization
     */
    public void testScopedStorage() {
        User org1 = new User();
        org1.id = 20;
        RecentRepositories recent1 = new RecentRepositories(getContext(), org1);
        long id1 = 1234;
        recent1.add(id1);
        assertTrue(recent1.contains(id1));

        User org2 = new User();
        org2.id = 40;
        RecentRepositories recent2 = new RecentRepositories(getContext(), org2);
        assertFalse(recent2.contains(id1));
        long id2 = 2345;
        recent2.add(id2);
        assertTrue(recent2.contains(id2));

        recent2.save();
        recent1 = new RecentRepositories(getContext(), org1);
        assertFalse(recent1.contains(id2));
    }
}
