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
package com.github.pockethub.android.core.issue;

import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.payload.IssueCommentPayload;
import com.meisolsson.githubsdk.model.payload.IssuesPayload;
import com.meisolsson.githubsdk.model.payload.PullRequestPayload;

import static com.github.pockethub.android.core.issue.IssueUtils.toIssue;

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
    public Issue getIssue(GitHubEvent event) {
        if (event == null || event.payload() == null) {
            return null;
        }

        switch (event.type()) {
            case IssuesEvent:
                return ((IssuesPayload) event.payload()).issue();
            case IssueCommentEvent:
                return ((IssueCommentPayload) event.payload()).issue();
            case PullRequestEvent:
                return toIssue(((PullRequestPayload) event.payload()).pullRequest());
            default:
                return null;
        }
    }
}
