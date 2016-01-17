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

import android.text.TextUtils;

import com.alorma.github.sdk.bean.dto.response.PullRequest;
import com.alorma.github.sdk.bean.dto.response.Issue;


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
        return issue != null && issue.pullRequest != null
                && !TextUtils.isEmpty(issue.pullRequest.html_url);
    }

    /**
     * Convert {@link PullRequest} model {@link Issue} model
     *
     * @param pullRequest
     * @return issue
     */
    public static Issue toIssue(final PullRequest pullRequest) {
        if (pullRequest == null)
            return null;

        Issue issue = new Issue();
        issue.assignee = pullRequest.assignee;
        issue.body = pullRequest.body;
        issue.body_html = pullRequest.body_html;
        issue.body = pullRequest.body;
        issue.closedAt = pullRequest.closedAt;
        issue.comments = pullRequest.comments;
        issue.created_at = pullRequest.created_at;
        issue.html_url = pullRequest.html_url;
        issue.number = pullRequest.number;
        issue.milestone = pullRequest.milestone;
        issue.id = pullRequest.id;
        issue.pullRequest = pullRequest;
        issue.state = pullRequest.state;
        issue.title = pullRequest.title;
        issue.updated_at = pullRequest.updated_at;
        issue.url = pullRequest.url;
        issue.user = pullRequest.user;
        return issue;
    }
}
