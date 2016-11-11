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

import android.test.AndroidTestCase;

import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.GitHubEventType;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.core.repo.RepositoryEventMatcher;
import com.meisolsson.githubsdk.model.payload.ForkPayload;

/**
 * Unit tests of {@link RepositoryEventMatcher}
 */
public class RepositoryEventMatcherTest extends AndroidTestCase {

    /**
     * Test fork event that has an incomplete forkee in the payload
     */
    public void testIncompleteRepositoryFork() {
        RepositoryEventMatcher matcher = new RepositoryEventMatcher();
        ForkPayload payload = ForkPayload.builder().build();

        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.ForkEvent)
                .payload(payload)
                .build();

        assertNull(matcher.getRepository(event));

        Repository repository = Repository.builder().build();
        payload = payload.toBuilder().forkee(repository).build();
        event = event.toBuilder().payload(payload).build();
        assertNull(matcher.getRepository(event));

        repository = repository.toBuilder().name("repo").build();
        payload = payload.toBuilder().forkee(repository).build();
        event = event.toBuilder().payload(payload).build();
        assertNull(matcher.getRepository(event));

        User user = User.builder().build();
        repository = repository.toBuilder().owner(user).build();
        payload = payload.toBuilder().forkee(repository).build();
        event = event.toBuilder().payload(payload).build();
        assertNull(matcher.getRepository(event));

        user = user.toBuilder().login("owner").build();
        repository = repository.toBuilder().owner(user).build();
        payload = payload.toBuilder().forkee(repository).build();
        event = event.toBuilder().payload(payload).build();
        assertEquals(repository, matcher.getRepository(event));
    }

}
