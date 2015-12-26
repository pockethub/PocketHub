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
package com.github.pockethub.core.gist;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.events.EventType;
import com.github.pockethub.api.GistEventPayload;
import com.google.gson.Gson;


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
    public Gist getGist(final GithubEvent event) {
        if (event == null)
            return null;
        if (event.payload == null)
            return null;

        Gson gson = new Gson();
        String json = gson.toJson(event.payload);

        EventType type = event.getType();
        if (EventType.GistEvent.equals(type))
            return (gson.fromJson(json, GistEventPayload.class)).gist;
        else
            return null;
    }
}
