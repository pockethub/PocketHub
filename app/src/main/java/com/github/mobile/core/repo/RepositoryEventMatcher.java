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
package com.github.mobile.core.repo;

import static org.eclipse.egit.github.core.event.Event.TYPE_CREATE;
import static org.eclipse.egit.github.core.event.Event.TYPE_FORK;
import static org.eclipse.egit.github.core.event.Event.TYPE_PUBLIC;
import static org.eclipse.egit.github.core.event.Event.TYPE_WATCH;
import android.text.TextUtils;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.EventPayload;
import org.eclipse.egit.github.core.event.EventRepository;
import org.eclipse.egit.github.core.event.ForkPayload;

/**
 * Helper to find a {@link RepositoryEventMatcher} to open for an event
 */
public class RepositoryEventMatcher {

    /**
     * Get {@link Repository} from {@link EventRepository} and actor
     *
     * @param repo
     * @param actor
     * @param org
     * @return possibly null repository
     */
    public static Repository getRepository(final EventRepository repo,
            User actor, User org) {
        if (repo == null)
            return null;

        String id = repo.getName();
        int slash = id.indexOf('/');
        if (slash == -1 || slash + 1 >= id.length())
            return null;

        Repository full = new Repository();
        full.setId(repo.getId());
        full.setName(id.substring(slash + 1));
        String login = id.substring(0, slash);
        // Use actor if it matches login parsed from repository id
        if (actor != null && login.equals(actor.getLogin()))
            full.setOwner(actor);
        else if (org != null && login.equals(org.getLogin()))
            full.setOwner(org);
        else
            full.setOwner(new User().setLogin(id.substring(0, slash)));
        return full;
    }

    /**
     * Get {@link Repository} from event
     *
     * @param event
     * @return gist or null if event doesn't apply
     */
    public Repository getRepository(final Event event) {
        if (event == null)
            return null;

        EventPayload payload = event.getPayload();
        if (payload == null)
            return null;

        String type = event.getType();
        if (TYPE_FORK.equals(type)) {
            Repository repository = ((ForkPayload) payload).getForkee();
            // Verify repository has valid name and owner
            if (repository != null && !TextUtils.isEmpty(repository.getName())
                    && repository.getOwner() != null
                    && !TextUtils.isEmpty(repository.getOwner().getLogin()))
                return repository;
        }

        if (TYPE_CREATE.equals(type) || TYPE_WATCH.equals(type)
                || TYPE_PUBLIC.equals(type))
            return getRepository(event.getRepo(), event.getActor(),
                    event.getOrg());

        return null;
    }
}
