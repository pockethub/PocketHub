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
package com.github.pockethub.android.tests.issue;

import android.test.AndroidTestCase;

import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.core.issue.IssueFilter;

/**
 * Unit tests of {@link IssueFilter}
 */
public class IssueFilterTest extends AndroidTestCase {

    /**
     * Verify {@link IssueFilter#equals(Object)}
     */
    public void testEqualFilter() {
        Repository repo = Repository.builder()
                .id(1L)
                .build();

        IssueFilter filter1 = new IssueFilter(repo);

        assertFalse(filter1.equals(null));
        assertFalse(filter1.equals(""));
        assertTrue(filter1.equals(filter1));

        IssueFilter filter2 = new IssueFilter(repo);
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        User user = User.builder()
                .id(2)
                .build();

        filter1.setAssignee(user);
        assertFalse(filter1.equals(filter2));
        filter2.setAssignee(user);
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.setOpen(false);
        assertFalse(filter1.equals(filter2));
        filter2.setOpen(false);
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        Milestone milestone = Milestone.builder()
                .number(3)
                .build();
        filter1.setMilestone(milestone);
        assertFalse(filter1.equals(filter2));
        filter2.setMilestone(milestone);
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());
    }
}
