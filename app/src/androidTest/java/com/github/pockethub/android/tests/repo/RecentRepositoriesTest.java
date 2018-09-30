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

import androidx.test.filters.SmallTest;
import com.github.pockethub.android.ui.repo.RecentRepositories;
import com.meisolsson.githubsdk.model.User;
import org.junit.Test;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.github.pockethub.android.ui.repo.RecentRepositories.MAX_SIZE;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Unit tests of {@link RecentRepositories}
 */
@SmallTest
public class RecentRepositoriesTest {

    /**
     * Verify bad input
     */
    @Test
    public void testBadInput() {
        User org = User.builder()
                .id(20L)
                .build();

        RecentRepositories recent = new RecentRepositories(getTargetContext(), org);
        assertFalse(recent.contains(null));
        assertFalse(recent.contains(-1));
    }

    /**
     * Verify eviction
     */
    @Test
    public void testMaxReached() {
        User org = User.builder()
                .id(20L)
                .build();

        RecentRepositories recent = new RecentRepositories(getTargetContext(), org);

        for (int i = 0; i < MAX_SIZE; i++) {
            recent.add(i);
            assertTrue(recent.contains(i));
        }

        recent.add(MAX_SIZE + 1);
        assertTrue(recent.contains(MAX_SIZE + 1));
        assertFalse(recent.contains(0));

        for (int i = 1; i < MAX_SIZE; i++) {
            assertTrue(recent.contains(i));
        }
    }

    /**
     * Verify input/output to disk of {@link RecentRepositories} state
     */
    @Test
    public void testIO() {
        User org = User.builder()
                .id(20L)
                .build();

        RecentRepositories recent1 = new RecentRepositories(getTargetContext(), org);
        long id = 1234;
        recent1.add(id);
        assertTrue(recent1.contains(id));
        recent1.save();
        RecentRepositories recent2 = new RecentRepositories(getTargetContext(), org);
        assertTrue(recent2.contains(id));
    }

    /**
     * Verify repositories are scoped to organization
     */
    @Test
    public void testScopedStorage() {
        User org1 = User.builder()
                .id(20L)
                .build();

        RecentRepositories recent1 = new RecentRepositories(getTargetContext(), org1);
        long id1 = 1234;
        recent1.add(id1);
        assertTrue(recent1.contains(id1));

        User org2 = User.builder()
                .id(40L)
                .build();

        RecentRepositories recent2 = new RecentRepositories(getTargetContext(), org2);
        assertFalse(recent2.contains(id1));
        long id2 = 2345;
        recent2.add(id2);
        assertTrue(recent2.contains(id2));

        recent2.save();
        recent1 = new RecentRepositories(getTargetContext(), org1);
        assertFalse(recent1.contains(id2));
    }
}
