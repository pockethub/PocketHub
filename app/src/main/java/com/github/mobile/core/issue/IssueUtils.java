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
package com.github.mobile.core.issue;

import android.text.TextUtils;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;

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
    public static boolean isPullRequest(Issue issue) {
        return issue != null && issue.getPullRequest() != null
                && !TextUtils.isEmpty(issue.getPullRequest().getHtmlUrl());
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
        issue.setAssignee(pullRequest.getAssignee());
        issue.setBody(pullRequest.getBody());
        issue.setBodyHtml(pullRequest.getBodyHtml());
        issue.setBodyText(pullRequest.getBodyText());
        issue.setClosedAt(pullRequest.getClosedAt());
        issue.setComments(pullRequest.getComments());
        issue.setCreatedAt(pullRequest.getCreatedAt());
        issue.setHtmlUrl(pullRequest.getHtmlUrl());
        issue.setId(pullRequest.getId());
        issue.setMilestone(pullRequest.getMilestone());
        issue.setNumber(pullRequest.getNumber());
        issue.setPullRequest(pullRequest);
        issue.setState(pullRequest.getState());
        issue.setTitle(pullRequest.getTitle());
        issue.setUpdatedAt(pullRequest.getUpdatedAt());
        issue.setUrl(pullRequest.getUrl());
        issue.setUser(pullRequest.getUser());
        return issue;
    }
}
