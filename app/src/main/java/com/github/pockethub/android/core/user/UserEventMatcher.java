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
package com.github.pockethub.android.core.user;

import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.GitHubEventType;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.model.payload.FollowPayload;

/**
 * Matches a {@link User} in an {@link GitHubEvent}
 */
public class UserEventMatcher {

    /**
     * Pair of users in an {@link GitHubEvent}
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
    public UserPair getUsers(final GitHubEvent event) {
        if (event == null || event.payload() == null) {
            return null;
        }

        GitHubEventType type = event.type();
        if (GitHubEventType.FollowEvent.equals(type)) {
            User from = event.actor();
            User to = ((FollowPayload) event.payload()).target();
            if (from != null && to != null) {
                return new UserPair(from, to);
            }
        }

        return null;
    }
}
