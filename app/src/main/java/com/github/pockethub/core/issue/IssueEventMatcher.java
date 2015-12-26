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
package com.github.pockethub.core.issue;

import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.events.payload.IssueCommentEventPayload;
import com.alorma.github.sdk.bean.dto.response.events.payload.IssueEventPayload;
import com.alorma.github.sdk.bean.dto.response.events.payload.PullRequestEventPayload;
import com.google.gson.Gson;

/**
 * Helper to find an issue to open for an event
 */
public class IssueEventMatcher {

    /**
     * Get issue from event
     *
     * @param event
     * @return issue or null if event doesn't apply
     */
    public Issue getIssue(GithubEvent event) {
        if (event == null)
            return null;
        if (event.payload == null)
            return null;

        Gson gson = new Gson();
        String json = gson.toJson(event.payload);

        switch (event.type) {
            case IssuesEvent:
                return gson.fromJson(json, IssueEventPayload.class).issue;
            case IssueCommentEvent:
                return gson.fromJson(json, IssueCommentEventPayload.class).issue;
            case PullRequestEvent:
                return gson.fromJson(json, PullRequestEventPayload.class).pull_request;
            default:
                return null;
        }
    }
}
