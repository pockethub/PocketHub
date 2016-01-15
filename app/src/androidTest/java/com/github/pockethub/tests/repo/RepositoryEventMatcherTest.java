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

import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.dto.response.events.EventType;
import com.alorma.github.sdk.bean.dto.response.events.payload.Payload;
import com.github.pockethub.core.repo.RepositoryEventMatcher;

/**
 * Unit tests of {@link RepositoryEventMatcher}
 */
public class RepositoryEventMatcherTest extends AndroidTestCase {

    /**
     * Test fork event that has an incomplete forkee in the payload
     */
    public void testIncompleteRepositoryFork() {
        RepositoryEventMatcher matcher = new RepositoryEventMatcher();
        GithubEvent event = new GithubEvent();
        event.type = (EventType.ForkEvent);
        Payload payload = new Payload();
        event.payload = payload;
        assertNull(matcher.getRepository(event));

        Repo repository = new Repo();
        payload.forkee = repository;
        assertNull(matcher.getRepository(event));

        repository.name = "repo";
        assertNull(matcher.getRepository(event));

        repository.owner = new User();
        assertNull(matcher.getRepository(event));

        repository.owner.login = "owner";
        assertEquals(repository, matcher.getRepository(event));
    }

}
