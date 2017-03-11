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
package com.github.pockethub.android.core.gist;

import com.github.pockethub.android.ui.user.EventType;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.GitHubEventType;
import com.meisolsson.githubsdk.model.payload.GistPayload;


/**
 * Helper to find a {@link Gist} to open for an event
 */
public class GistEventMatcher {

    /**
     * Get gist from event
     *
     * @param event
     * @return gist or null if event doesn't apply
     */
    public Gist getGist(final GitHubEvent event) {
        if (event == null || event.payload() == null) {
            return null;
        }

        GitHubEventType type = event.type();
        if (EventType.GistEvent.equals(type)) {
            return ((GistPayload) event.payload()).gist();
        } else {
            return null;
        }
    }
}
