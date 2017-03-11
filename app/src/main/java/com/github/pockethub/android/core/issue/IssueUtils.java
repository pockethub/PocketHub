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

import android.text.TextUtils;

import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.PullRequest;

/**
 * Utilities for working with {@link Issue} models
 */
public class IssueUtils {

    /**
     * Is the given issue a pull request?
     *
     * @param issue
     * @return true if pull request, false otherwise
     */
    public static boolean isPullRequest(final Issue issue) {
        return issue != null && issue.pullRequest() != null
                && !TextUtils.isEmpty(issue.pullRequest().htmlUrl());
    }

    /**
     * Convert {@link PullRequest} model {@link Issue} model
     *
     * @param pullRequest
     * @return issue
     */
    public static Issue toIssue(final PullRequest pullRequest) {
        if (pullRequest == null) {
            return null;
        }

        return Issue.builder()
                .assignee(pullRequest.assignee())
                .body(pullRequest.body())
                .bodyHtml(pullRequest.bodyHtml())
                .closedAt(pullRequest.closedAt())
                .comments(pullRequest.comments())
                .createdAt(pullRequest.createdAt())
                .htmlUrl(pullRequest.htmlUrl())
                .number(pullRequest.number())
                .milestone(pullRequest.milestone())
                .id(pullRequest.id())
                .pullRequest(pullRequest)
                .state(pullRequest.state())
                .title(pullRequest.title())
                .updatedAt(pullRequest.updatedAt())
                .url(pullRequest.url())
                .user(pullRequest.user())
                .build();
    }
}
