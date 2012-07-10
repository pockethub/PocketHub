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
package com.github.mobile.tests.tests.repo;

import android.test.AndroidTestCase;

import com.github.mobile.core.repo.RepositoryEventMatcher;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.ForkPayload;

/**
 * Unit tests of {@link RepositoryEventMatcher}
 */
public class RepositoryEventMatcherTest extends AndroidTestCase {

    /**
     * Test fork event that has an incomplete forkee in the payload
     */
    public void testIncompleteRepositoryFork() {
        RepositoryEventMatcher matcher = new RepositoryEventMatcher();
        Event event = new Event();
        event.setType(Event.TYPE_FORK);
        ForkPayload payload = new ForkPayload();
        event.setPayload(payload);
        assertNull(matcher.getRepository(event));
        Repository repository = new Repository();
        payload.setForkee(repository);
        assertNull(matcher.getRepository(event));
        repository.setName("repo");
        assertNull(matcher.getRepository(event));
        repository.setOwner(new User());
        assertNull(matcher.getRepository(event));
        repository.getOwner().setLogin("owner");
        assertEquals(repository, matcher.getRepository(event));
    }

}
