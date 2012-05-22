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
package com.github.mobile.core.user;

import static org.eclipse.egit.github.core.event.Event.TYPE_FOLLOW;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.EventPayload;
import org.eclipse.egit.github.core.event.FollowPayload;

/**
 * Matches a {@link User} in an {@link Event}
 */
public class UserEventMatcher {

    /**
     * Pair of users in an {@link Event}
     */
    public static class UserPair {

        /**
         * Actor in event
         */
        public final User from;

        /**
         * User being acted upon
         */
        public final User to;

        private UserPair(final User from, final User to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * Get {@link UserPair} from event
     *
     * @param event
     * @return user or null if event doesn't apply
     */
    public UserPair getUsers(final Event event) {
        if (event == null)
            return null;

        EventPayload payload = event.getPayload();
        if (payload == null)
            return null;

        String type = event.getType();
        if (TYPE_FOLLOW.equals(type)) {
            User from = event.getActor();
            User to = ((FollowPayload) event.getPayload()).getTarget();
            if (from != null && to != null)
                return new UserPair(from, to);
        }

        return null;
    }

}
